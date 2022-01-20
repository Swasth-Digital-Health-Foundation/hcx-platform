package org.swasth.common.dto;

public class StatusResponse {

    private String entity_type;
    private String sender_code;
    private String recipient_code;
    private String protocol_status;

    public StatusResponse (String entity_type, String sender_code, String recipient_code, String protocol_status) {
        this.entity_type = entity_type;
        this.sender_code = sender_code;
        this.recipient_code = recipient_code;
        this.protocol_status = protocol_status;
    }

    public String  getProtocol_status() {
        return protocol_status;
    }
    public void setProtocol_status(String protocol_status) {
        this.protocol_status = protocol_status;
    }
    public String getRecipient_code() {
        return recipient_code;
    }
    public void setRecipient_code(String recipient_code) {
        this.recipient_code = recipient_code;
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
