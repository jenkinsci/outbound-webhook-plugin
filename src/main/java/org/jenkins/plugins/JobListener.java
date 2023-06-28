package org.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import javax.annotation.Nonnull;

@Extension
public class JobListener extends RunListener<AbstractBuild> {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;

    public JobListener() {
        super(AbstractBuild.class);
        client = new OkHttpClient();
    }

    @Override
    public void onStarted(AbstractBuild build, TaskListener listener) {
        WebHookPublisher publisher = GetWebHookPublisher(build);
        if (publisher == null || !publisher.onStart) {
            return;
        }
        String webHookUrl = publisher.webHookUrl;
        String buildUrl = build.getAbsoluteUrl();
        String projectName = build.getProject().getDisplayName();
        String buildName = build.getDisplayName();
        String buildVars = build.getBuildVariables().toString();
        NotificationEvent event = new NotificationEvent(projectName, buildName, buildUrl, buildVars, "start");
        httpPost(webHookUrl, event, listener);
    }

    @Override
    public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
        WebHookPublisher publisher = GetWebHookPublisher(build);
        if (publisher == null) {
            return;
        }
        Result result = build.getResult();
        if (result == null) {
            return;
        }
        String webHookUrl = publisher.webHookUrl;
        String buildUrl = build.getAbsoluteUrl();
        String projectName = build.getProject().getDisplayName();
        String buildName = build.getDisplayName();
        String buildVars = build.getBuildVariables().toString();
        NotificationEvent event = new NotificationEvent(projectName, buildName, buildUrl, buildVars, "");
        if (publisher.onSuccess && result.equals(Result.SUCCESS)) {
            event.event = "success";
            httpPost(webHookUrl, event, listener);
        }
        if (publisher.onFailure && result.equals(Result.FAILURE)) {
            event.event = "failure";
            httpPost(webHookUrl, event, listener);
        }
        if (publisher.onUnstable && result.equals(Result.UNSTABLE)) {
            event.event = "unstable";
            httpPost(webHookUrl, event, listener);
        }
    }

    private WebHookPublisher GetWebHookPublisher(AbstractBuild build) {
        for (Object publisher : build.getProject().getPublishersList().toMap().values()) {
            if (publisher instanceof WebHookPublisher) {
                return (WebHookPublisher) publisher;
            }
        }
        return null;
    }

    private void httpPost(String url, Object object, @Nonnull TaskListener listener) {
        String jsonString = JSON.toJSONString(object);
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonString);
        Request request = new Request.Builder().url(url).post(body).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                listener.getLogger().println("Invocation of webhook " + url + " successful");
            } else {
                listener.getLogger().println("Invocation of webhook " + url + " failed: HTTP " + response.code() + " " + response.message());
            }
            response.close();
        } catch (Exception e) {
            listener.getLogger().println("Invocation of webhook " + url + " failed: " + e);
        }
    }
}
