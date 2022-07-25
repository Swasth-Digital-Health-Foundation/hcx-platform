package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private long timestamp = System.currentTimeMillis();
    @JsonProperty("correlation_id")
    private String correlationId;
    @JsonProperty("api_call_id")
    private String apiCallId;
    private ResponseError error;
    private Map<String, Object> result;
    private List<Subscription> subscriptions;
    @JsonProperty("count")
    private Integer count;
    private List<Map<String,Object>> notifications;
    @JsonProperty("notification_id")
    private String notificationId;
    private List<String> subscription_list;
    private String subscription_id;

    public Response() {}

    public Response(String correlationId, String apiCallId) {
        this.correlationId = correlationId;
        this.apiCallId = apiCallId;
    }

    public Response(Request request){
        this.setCorrelationId(request.getCorrelationId());
        this.setApiCallId(request.getApiCallId());
    }

    public Response(ResponseError error){
        this.error = error;
    }

    public Response(String key, Object val) {
        this.result = new HashMap<>();
        this.put(key, val);
    }

    public List<Map<String,Object>> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Map<String,Object>> notifications) {
        this.notifications = notifications;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNotificationId() { return notificationId; }

    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getApiCallId() {
        return apiCallId;
    }

    public void setApiCallId(String apiCallId) {
        this.apiCallId = apiCallId;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(ResponseError error) {
        this.error = error;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public Object get(String key) {
        return result.get(key);
    }

    public Response put(String key, Object vo) {
        result.put(key, vo);
        return this;
    }

    public List<String> getSubscription_list() {
        return subscription_list;
    }

    public void setSubscription_list(List<String> subscription_list) {
        this.subscription_list = subscription_list;
    }

    public String getSubscription_id() {
        return subscription_id;
    }

    public void setSubscription_id(String subscription_id) {
        this.subscription_id = subscription_id;
    }
}

