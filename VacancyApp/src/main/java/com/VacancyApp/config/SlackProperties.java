package com.VacancyApp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Slack settings bound from the "slack.*" properties. */
@ConfigurationProperties(prefix = "slack")
public class SlackProperties {

    /** Incoming webhook URL. Empty when Slack is disabled. */
    private String url = "";

    /** When false, messages are logged instead of sent (useful for local dev). */
    private boolean enabled = true;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
