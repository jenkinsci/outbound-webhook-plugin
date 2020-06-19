package org.jenkins.plugins;

public class NotificationEvent {
    public NotificationEvent(String projectName, String buildName, String buildUrl, String buildVars, String event) {
        this.projectName = projectName;
        this.buildName = buildName;
        this.buildUrl = buildUrl;
        this.buildVars = buildVars;
        this.event = event;
    }

    public String projectName;
    public String buildName;
    public String buildUrl;
    public String buildVars;
    public String event;
}
