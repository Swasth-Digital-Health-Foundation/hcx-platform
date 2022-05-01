package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class HeaderAudit {

	private String eid;
	@JsonProperty("x-hcx-error_details")
	private Object error_details;
	@JsonProperty("x-hcx-debug_details")
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
	private long auditTimeStamp;
	private long updatedTimestamp;
	private String action;
	private String mid;
	@JsonProperty("x-hcx-status")
	private String status;
	private List<String> senderRole;
	private List<String> recipientRole;
	private String payload;
	@JsonProperty("x-hcx-notification_id")
	private String notificationId;
	@JsonProperty("x-hcx-notification_data")
	private Object notificationData;
	private Object notificationDispatchResult;

	public HeaderAudit() {}

	public HeaderAudit(String eid, Object error_details, Object debug_details, String recipient_code, String sender_code, String api_call_id, String workflow_id, String correlation_id, String timestamp, long requestTimeStamp, long auditTimeStamp, long updatedTimestamp, String action, String mid, String status, List<String> senderRole, List<String> recipientRole, String payload) {
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
		this.auditTimeStamp = auditTimeStamp;
		this.updatedTimestamp = updatedTimestamp;
		this.action = action;
		this.mid = mid;
		this.status = status;
		this.senderRole = senderRole;
		this.recipientRole = recipientRole;
		this.payload =	payload;
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
	public long getAuditTimeStamp() {
		return auditTimeStamp;
	}
	public void setAuditTimeStamp(long auditTimestamp) {
		this.auditTimeStamp = auditTimeStamp;
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
	public List<String> getSenderRole() {
		return senderRole;
	}
	public void setSenderRole(List<String> senderRole) {
		this.senderRole = senderRole;
	}
	public List<String> getRecipientRole() {
		return recipientRole;
	}
	public void setRecipientRole(List<String> recipientRole) {
		this.recipientRole = recipientRole;
	}
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	public String getNotificationId() {
		return notificationId;
	}
	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}
	public Object getNotificationData() {
		return notificationData;
	}
	public void setNotificationData(Object notificationData) {
		this.notificationData = notificationData;
	}
	public Object getNotificationDispatchResult() {
		return notificationDispatchResult;
	}
	public void setNotificationDispatchResult(Object notificationDispatchResult) {
		this.notificationDispatchResult = notificationDispatchResult;
	}
}
