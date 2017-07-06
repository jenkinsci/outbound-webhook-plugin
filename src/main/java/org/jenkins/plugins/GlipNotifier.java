package org.jenkins.plugins;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.tasks.Notifier;
import hudson.tasks.BuildStepMonitor;
import hudson.Extension;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.model.AbstractProject;

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

    @Override
    public GlipNotifierDescriptor getDescriptor() {
        return (GlipNotifierDescriptor) super.getDescriptor();
    }

    @Extension
    public static class GlipNotifierDescriptor extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Outbound WebHook notification";
        }
    }
}
