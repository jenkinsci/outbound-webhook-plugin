package org.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;

/**
 * Created by Marvin on 16/10/7.
 */
@Extension
public class JobListener extends RunListener<AbstractBuild> {

    public JobListener() {
        super(AbstractBuild.class);
    }

    @Override
    public void onStarted(AbstractBuild r, TaskListener listener) {
        // getService(r, listener).start();
    }

    @Override
    public void onCompleted(AbstractBuild r, @Nonnull TaskListener listener) {
        Result result = r.getResult();
        if (null != result && result.equals(Result.SUCCESS)) {
            // getService(r, listener).success();
        } else {
            // getService(r, listener).failed();
        }
    }
}
