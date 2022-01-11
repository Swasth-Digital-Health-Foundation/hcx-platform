package org.swasth.common.dto;

public class StatusResponse {

    private String request_id;
    private String correlation_id;
    private String workflow_id;
    private String entity_type;
    private String sender_code;
    private String recipient_code;
    private String status;

    public StatusResponse (String request_id, String correlation_id, String workflow_id, String entity_type, String sender_code, String recipient_code, String status) {
        this.request_id = request_id;
        this.correlation_id = correlation_id;
        this.workflow_id = workflow_id;
        this.entity_type = entity_type;
        this.sender_code = sender_code;
        this.recipient_code = recipient_code;
        this.status = status;
    }

    public String  getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getRequest_id() {
        return request_id;
    }
    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }
    public String getRecipient_code() {
        return recipient_code;
    }
    public void setRecipient_code(String recipient_code) {
        this.recipient_code = recipient_code;
    }
    public String getCorrelation_id() {
        return correlation_id;
    }
    public void setCorrelation_id(String correlation_id) {
        this.correlation_id = correlation_id;
    }
    public String getWorkflow_id() {
        return workflow_id;
    }
    public void setWorkflow_id(String workflow_id) {
        this.workflow_id = workflow_id;
    }
    public String getSender_code() {
        return sender_code;
    }
    public void setSender_code(String sender_code) {
        this.sender_code = sender_code;
    }
    public String getEntity_type() { return entity_type; }
    public void setEntity_type(String action) {
        this.entity_type = entity_type;
    }

}
