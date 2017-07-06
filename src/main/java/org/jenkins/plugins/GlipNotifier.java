package org.jenkins.plugins;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.tasks.Notifier;
import hudson.tasks.BuildStepMonitor;

public class GlipNotifier extends Notifier {
    public String webHookUrl;
    public Boolean onStart;
    public Boolean onSuccess;
    public Boolean onFailed;

    @DataBoundConstructor
    public GlipNotifier(String webHookUrl, boolean onStart, boolean onSuccess, boolean onFailed) {
        super();
        this.webHookUrl = webHookUrl;
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
