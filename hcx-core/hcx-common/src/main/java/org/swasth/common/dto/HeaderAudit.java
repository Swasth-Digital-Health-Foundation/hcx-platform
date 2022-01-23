package org.swasth.common.dto;

public class HeaderAudit {
	
	private String request_id;
	private String recipient_code;
	private String correlation_id;
	private String workflow_id;
	private String timestamp;
	private String sender_code;
	private String mid;
	private String action;
	private Object log_details;
	private Object jose;
	private Object status;

	
	public Object getStatus() {
		return status;
	}
	public void setStatus(Object status) {
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
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getSender_code() {
		return sender_code;
	}
	public void setSender_code(String sender_code) {
		this.sender_code = sender_code;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Object getLog_details() {
		return log_details;
	}
	public void setLog_details(Object log_details) {
		this.log_details = log_details;
	}
	public Object getJose() {
		return jose;
	}
	public void setJose(Object jose) {
		this.jose = jose;
	}
}
