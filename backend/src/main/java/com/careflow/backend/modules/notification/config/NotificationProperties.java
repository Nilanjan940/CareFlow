package com.careflow.backend.modules.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "careflow.notifications")
public class NotificationProperties {

    private long reminderHoursBefore = 24;

    private long schedulerIntervalMs = 60000;

    public long getReminderHoursBefore() {
        return reminderHoursBefore;
    }

    public void setReminderHoursBefore(long reminderHoursBefore) {
        this.reminderHoursBefore = reminderHoursBefore;
    }

    public long getSchedulerIntervalMs() {
        return schedulerIntervalMs;
    }

    public void setSchedulerIntervalMs(long schedulerIntervalMs) {
        this.schedulerIntervalMs = schedulerIntervalMs;
    }
}