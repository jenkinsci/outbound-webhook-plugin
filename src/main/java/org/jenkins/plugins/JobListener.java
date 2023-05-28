package org.jenkins.plugins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Extension
public class JobListener extends RunListener<AbstractBuild> {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Logger log = LoggerFactory.getLogger(JobListener.class);
    private static final OkHttpClient client = new OkHttpClient();

    public JobListener() {
        super(AbstractBuild.class);
    }

    public static void httpPost(String url, Object object) {
        String jsonString = JSON.toJSONString(object, SerializerFeature.WriteEnumUsingToString);
        RequestBody body = RequestBody.create(jsonString, JSON_MEDIA_TYPE);
        Request request = new Request.Builder().url(url).post(body).build();
        try {
            Response response = client.newCall(request).execute();
            log.debug("Invocation of webhook {} successful", url);
            if (response.body() != null) log.debug("Response: {}", response.body().string());
            response.close();
        } catch (Exception e) {
            log.info("Invocation of webhook {} failed", url, e);
        }
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
        int buildNumber = build.getNumber();
        String buildVars = build.getBuildVariables().toString();
        NotificationEvent event = new NotificationEvent(projectName, buildName, buildNumber, buildUrl, buildVars, NotificationEvent.EventType.START);
        httpPost(webHookUrl, event);
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
        int buildNumber = build.getNumber();
        String buildVars = build.getBuildVariables().toString();
        NotificationEvent event = new NotificationEvent(projectName, buildName, buildNumber, buildUrl, buildVars, null);
        if (publisher.onSuccess && result.equals(Result.SUCCESS)) {
            event.event = NotificationEvent.EventType.SUCCESS;
            httpPost(webHookUrl, event);
        }
        if (publisher.onFailure && result.equals(Result.FAILURE)) {
            event.event = NotificationEvent.EventType.FAILURE;
            httpPost(webHookUrl, event);
        }
        if (publisher.onUnstable && result.equals(Result.UNSTABLE)) {
            event.event = NotificationEvent.EventType.UNSTABLE;
            httpPost(webHookUrl, event);
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
}
