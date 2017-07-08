package org.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import com.alibaba.fastjson.JSON;

import javax.annotation.Nonnull;

@Extension
public class JobListener extends RunListener<AbstractBuild> {

    public JobListener() {
        super(AbstractBuild.class);
    }

    @Override
    public void onStarted(AbstractBuild build, TaskListener listener) {
        System.out.println("start");
        WebHookPublisher publisher = GetWebHookPublisher(build);
        if(publisher == null || !publisher.onStart) {
            return;
        }
        String webHookUrl = publisher.webHookUrl;
        String buildUrl = build.getAbsoluteUrl();
        String projectName = build.getProject().getDisplayName();
        String buildName = build.getDisplayName();
        NotificationEvent event = new NotificationEvent(projectName, buildName, buildUrl, "start");
        String jsonString = JSON.toJSONString(event);
        System.out.println(jsonString);
        // todo: send started notification
    }

    @Override
    public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
        WebHookPublisher publisher = GetWebHookPublisher(build);
        if(publisher == null) {
            return;
        }
        Result result = build.getResult();
        if(result == null) {
            return;
        }
        String webHookUrl = publisher.webHookUrl;
        String buildUrl = build.getAbsoluteUrl();
        String projectName = build.getProject().getDisplayName();
        String buildName = build.getDisplayName();
        if(publisher.onSuccess && result.equals(Result.SUCCESS)) {
            // todo: send success notification
        }
        if(publisher.onFailure && result.equals(Result.FAILURE)) {
            // todo: send failed notification
        }
    }

    private WebHookPublisher GetWebHookPublisher(AbstractBuild build) {
        for(Object publisher : build.getProject().getPublishersList().toMap().values()) {
            if (publisher instanceof WebHookPublisher) {
                return (WebHookPublisher)publisher;
            }
        }
        return null;
    }
}
