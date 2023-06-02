package org.jenkins.plugins;

import hudson.EnvVars;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

public class WebHookPipelineExecution extends SynchronousNonBlockingStepExecution<Void> {
    private final WebHookPipelineStep step;

    public WebHookPipelineExecution(WebHookPipelineStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {
        String webHookUrl = this.step.getWebHookUrl();
        Run run = this.getContext().get(Run.class);
        EnvVars envVars = this.getContext().get(EnvVars.class);
        if (run == null) throw new Exception("Run is null");
        if (envVars == null) throw new Exception("EnvVars is null");
        String buildUrl = run.getAbsoluteUrl();
        String projectName = run.getParent().getFullName();
        String buildName = run.getDisplayName();
        int buildNumber = run.getNumber();
        String buildVars = envVars.toString();
        NotificationEvent event = new NotificationEvent(projectName, buildName, buildNumber, buildUrl, buildVars, NotificationEvent.EventType.PIPELINE);
        JobListener.httpPost(webHookUrl, event);
        return null;
    }
}
