package org.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;

import javax.annotation.Nonnull;

@Extension
public class JobListener extends RunListener<AbstractBuild> {

    public JobListener() {
        super(AbstractBuild.class);
    }

    @Override
    public void onStarted(AbstractBuild build, TaskListener listener) {
        // getService(r, listener).start();
        System.out.println("build started");
        WebHookPublisher publisher = GetWebHookPublisher(build);
        if(publisher!=null) {
            System.out.println(publisher.webHookUrl);
        }
    }

    @Override
    public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
        WebHookPublisher publisher = GetWebHookPublisher(build);
        if(publisher!=null) {
            System.out.println(publisher.webHookUrl);
        }
        Result result = build.getResult();
        if (null != result && result.equals(Result.SUCCESS)) {
            // getService(r, listener).success();
            System.out.println("build succeeded");
        } else {
            // getService(r, listener).failed();
            System.out.println("build failed");
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
