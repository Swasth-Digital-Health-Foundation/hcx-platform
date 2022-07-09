package org.swasth.common.dto;

import java.util.Set;

public class Notification {

    private String topicCode;
    private String title;
    private String description;
    private Set<String> allowedSenders;
    private Set<String> allowedRecipients;
    private String type;
    private String category;
    private String trigger;
    private String template;
    private String status;
    private int priority;

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getAllowedSenders() {
        return allowedSenders;
    }

    public void setAllowedSenders(Set<String> allowedSenders) {
        this.allowedSenders = allowedSenders;
    }

    public Set<String> getAllowedRecipients() {
        return allowedRecipients;
    }

    public void setAllowedRecipients(Set<String> allowedRecipients) {
        this.allowedRecipients = allowedRecipients;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
