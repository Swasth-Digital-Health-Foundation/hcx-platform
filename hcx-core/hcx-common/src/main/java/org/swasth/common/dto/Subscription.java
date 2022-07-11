package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subscription {
    //subscription_id,subscription_status,topic_code,sender_code,recipient_code,expiry,is_delegated
    private String subscription_id;
    private String topic_code;
    private int subscription_status;
    private String sender_code;
    private String recipient_code;
    private long expiry;
    private boolean is_delegated;

    public Subscription() {}

    public Subscription(String subscription_id, String topic_code, int subscription_status, String sender_code, String recipient_code, long expiry, boolean is_delegated) {
        this.subscription_id = subscription_id;
        this.topic_code = topic_code;
        this.subscription_status = subscription_status;
        this.sender_code = sender_code;
        this.recipient_code = recipient_code;
        this.expiry = expiry;
        this.is_delegated = is_delegated;
    }

    public String getSubscription_id() {
        return subscription_id;
    }

    public void setSubscription_id(String subscription_id) {
        this.subscription_id = subscription_id;
    }

    public String getTopic_code() {
        return topic_code;
    }

    public void setTopic_code(String topic_code) {
        this.topic_code = topic_code;
    }

    public int getSubscription_status() {
        return subscription_status;
    }

    public void setSubscription_status(int subscription_status) {
        this.subscription_status = subscription_status;
    }

    public String getSender_code() {
        return sender_code;
    }

    public void setSender_code(String sender_code) {
        this.sender_code = sender_code;
    }

    public String getRecipient_code() {
        return recipient_code;
    }

    public void setRecipient_code(String recipient_code) {
        this.recipient_code = recipient_code;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public boolean isIs_delegated() {
        return is_delegated;
    }

    public void setIs_delegated(boolean is_delegated) {
        this.is_delegated = is_delegated;
    }
}
