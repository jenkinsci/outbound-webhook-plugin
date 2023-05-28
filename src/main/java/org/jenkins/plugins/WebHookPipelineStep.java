package org.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.Set;

public class WebHookPipelineStep extends Step implements Serializable {
    public static final long serialVersionUID = 1L;
    private final String webHookUrl;

    @DataBoundConstructor
    public WebHookPipelineStep(String webHookUrl) {
        this.webHookUrl = webHookUrl;
    }

    public String getWebHookUrl() {
        return webHookUrl;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new WebHookPipelineExecution(this, context);
    }


    @Extension(optional = true)
    public static class DescriptorImpl extends StepDescriptor {
        public DescriptorImpl() {
            super();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(Run.class, EnvVars.class);
        }

        @Override
        public String getFunctionName() {
            return "webhookSend";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Send a message to a webhook with current build details";
        }
    }
}
