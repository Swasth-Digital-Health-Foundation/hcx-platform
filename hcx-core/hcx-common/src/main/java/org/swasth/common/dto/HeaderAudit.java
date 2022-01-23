package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HeaderAudit {

	private String eid;
	private Object error_details;
	private Object debug_details;
	@JsonProperty("x-hcx-recipient_code")
	private String recipient_code;
	@JsonProperty("x-hcx-sender_code")
	private String sender_code;
	@JsonProperty("x-hcx-api_call_id")
	private String api_call_id;
	@JsonProperty("x-hcx-workflow_id")
	private String workflow_id;
	@JsonProperty("x-hcx-correlation_id")
	private String correlation_id;
	@JsonProperty("x-hcx-timestamp")
	private String timestamp;
	private long requestTimeStamp;
	private long auditTimestamp;
	private long updatedTimestamp;
	private String action;
	private String mid;
	private String status;

	public HeaderAudit() {}

	public HeaderAudit(String eid, Object error_details, Object debug_details, String recipient_code, String sender_code, String api_call_id, String workflow_id, String correlation_id, String timestamp, long requestTimeStamp, long auditTimestamp, long updatedTimestamp, String action, String mid, String status) {
		this.eid = eid;
		this.error_details = error_details;
		this.debug_details = debug_details;
		this.recipient_code = recipient_code;
		this.sender_code = sender_code;
		this.api_call_id = api_call_id;
		this.workflow_id = workflow_id;
		this.correlation_id = correlation_id;
		this.timestamp = timestamp;
		this.requestTimeStamp = requestTimeStamp;
		this.auditTimestamp = auditTimestamp;
		this.updatedTimestamp = updatedTimestamp;
		this.action = action;
		this.mid = mid;
		this.status = status;
	}
	public String getEid() {
		return eid;
	}
	public void setEid(String eid) {
		this.eid = eid;
	}
	public Object getError_details() {
		return error_details;
	}
	public void setError_details(Object error_details) {
		this.error_details = error_details;
	}
	public Object getDebug_details() {
		return debug_details;
	}
	public void setDebug_details(Object debug_details) {
		this.debug_details = debug_details;
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
	public String getApi_call_id() {
		return api_call_id;
	}
	public void setApi_call_id(String api_call_id) {
		this.api_call_id = api_call_id;
	}
	public String getWorkflow_id() {
		return workflow_id;
	}
	public void setWorkflow_id(String workflow_id) {
		this.workflow_id = workflow_id;
	}
	public String getCorrelation_id() {
		return correlation_id;
	}
	public void setCorrelation_id(String correlation_id) {
		this.correlation_id = correlation_id;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public long getRequestTimeStamp() {
		return requestTimeStamp;
	}
	public void setRequestTimeStamp(long requestTimeStamp) {
		this.requestTimeStamp = requestTimeStamp;
	}
	public long getAuditTimestamp() {
		return auditTimestamp;
	}
	public void setAuditTimestamp(long auditTimestamp) {
		this.auditTimestamp = auditTimestamp;
	}
	public long getUpdatedTimestamp() {
		return updatedTimestamp;
	}
	public void setUpdatedTimestamp(long updatedTimestamp) {
		this.updatedTimestamp = updatedTimestamp;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
