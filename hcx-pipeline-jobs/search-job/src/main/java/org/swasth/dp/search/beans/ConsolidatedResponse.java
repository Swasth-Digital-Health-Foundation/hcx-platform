package org.swasth.dp.search.beans;

import java.io.Serializable;

public class ConsolidatedResponse implements Serializable {
    private int total_responses;
    private String expiry_time;
    private int response_index;
    private String sender_code;
    private SearchResponse summary;

    public int getTotal_responses() {
        return total_responses;
    }

    public void setTotal_responses(int total_responses) {
        this.total_responses = total_responses;
    }

    public String getExpiry_time() {
        return expiry_time;
    }

    public void setExpiry_time(String expiry_time) {
        this.expiry_time = expiry_time;
    }

    public int getResponse_index() {
        return response_index;
    }

    public void setResponse_index(int response_index) {
        this.response_index = response_index;
    }

    public String getSender_code() {
        return sender_code;
    }

    public void setSender_code(String sender_code) {
        this.sender_code = sender_code;
    }

    public SearchResponse getSummary() {
        return summary;
    }

    public void setSummary(SearchResponse summary) {
        this.summary = summary;
    }
}
