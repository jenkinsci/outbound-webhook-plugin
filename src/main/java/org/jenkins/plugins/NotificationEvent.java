package org.jenkins.plugins;

public class NotificationEvent {
    public NotificationEvent(String projectName, String buildName, int buildNumber, String buildUrl, String buildVars, EventType event) {
        this.projectName = projectName;
        this.buildName = buildName;
        this.buildNumber = buildNumber;
        this.buildUrl = buildUrl;
        this.buildVars = buildVars;
        this.event = event;
    }

    public String projectName;
    public String buildName;
    public int buildNumber;
    public String buildUrl;
    public String buildVars;
    public EventType event;

    public enum EventType {
        START("start"),
        SUCCESS("success"),
        FAILURE("failure"),
        UNSTABLE("unstable"),
        PIPELINE("pipeline");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
