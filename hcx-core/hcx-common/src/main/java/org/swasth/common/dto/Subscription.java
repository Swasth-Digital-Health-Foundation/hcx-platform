package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.swasth.common.utils.Constants.NOTIFICATION_ID;
import static org.swasth.common.utils.Constants.SUBSCRIPTION_ID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subscription {

    @JsonProperty(SUBSCRIPTION_ID)
    private String subscriptionId;
    @JsonProperty(NOTIFICATION_ID)
    private String notificationId;
    private String status;
    private String mode;

    public String getMode() { return mode; }

    public void setMode(String mode) { this.mode = mode; }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
