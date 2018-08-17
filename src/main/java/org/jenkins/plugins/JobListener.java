package org.jenkins.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import com.alibaba.fastjson.JSON;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URLEncodedUtils;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class JobListener extends RunListener<AbstractBuild> {

    private OkHttpClient client;

    private static final Logger log = LoggerFactory.getLogger(JobListener.class);

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
        Boolean payloadIsJson = publisher.publishedAsJson ? true : false;
        String buildUrl = build.getAbsoluteUrl();
        String projectName = build.getProject().getDisplayName();
        String buildName = build.getDisplayName();
        NotificationEvent event = new NotificationEvent(projectName, buildName, buildUrl, "start");
        httpPost(webHookUrl, event, payloadIsJson);
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
        Boolean payloadIsJson = publisher.publishedAsJson ? true : false;
        String buildUrl = build.getAbsoluteUrl();
        String projectName = build.getProject().getDisplayName();
        String buildName = build.getDisplayName();
        NotificationEvent event = new NotificationEvent(projectName, buildName, buildUrl, "");
        if (publisher.onSuccess && result.equals(Result.SUCCESS)) {
            event.event = "success";
            httpPost(webHookUrl, event, payloadIsJson);
        }
        if (publisher.onFailure && result.equals(Result.FAILURE)) {
            event.event = "failure";
            httpPost(webHookUrl, event, payloadIsJson);
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

    private void httpPost(String url, Object object, Boolean payloadIsJson) {
        String jsonString = JSON.toJSONString(object);
        RequestBody body;

        if (payloadIsJson) {
            body = RequestBody.create(PayloadType.JSON.getMediaType(), jsonString);
        } else {
            ArrayList params = generateParams(jsonString);
            body = RequestBody.create(PayloadType.STRING.getMediaType(), URLEncodedUtils.format(params, "ascii"));
        }

        Request request = new Request.Builder().url(url).post(body).build();
        try {
            Response response = client.newCall(request).execute();
            log.debug("Invocation of webhook {} successful", url);
        } catch (Exception e) {
        	log.info("Invocation of webhook {} failed", url, e);
        }
    }

    private ArrayList generateParams(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = new HashMap<>();
        try {
            map = mapper.readValue(jsonString, new TypeReference<Map<String, String>>(){});
        } catch (IOException e ) {
            e.printStackTrace();
        }

        ArrayList<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return params;
    }

    private enum PayloadType {
        JSON(MediaType.parse("application/json; charset=utf-8")),
        STRING(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"));

        private MediaType mediaType;

        PayloadType(MediaType mediaType) {
            this.mediaType = mediaType;
        }

        public MediaType getMediaType() {
            return mediaType;
        }
    }
}
