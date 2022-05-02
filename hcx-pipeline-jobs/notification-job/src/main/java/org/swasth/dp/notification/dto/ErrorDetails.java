package org.swasth.dp.notification.dto;


import java.io.Serializable;
import java.util.Map;

public class ErrorDetails implements Serializable {

    private String recipientId;
    private boolean successStatus;
    private Map<String,Object> error;

    public ErrorDetails(String recipientId, boolean successStatus, Map<String,Object> error) {
        this.recipientId = recipientId;
        this.successStatus = successStatus;
        this.error = error;
    }

    public String getRecipientId(){
        return this.recipientId;
    }
    public void setRecipientId(String recipientId){
        this.recipientId = recipientId;
    }
    public boolean getSuccessStatus(){
        return this.successStatus;
    }
    public void setSuccessStatus(boolean successStatus){
        this.successStatus = successStatus;
    }
    public Map<String,Object> getError(){
        return this.error;
    }
    public void setError(Map<String,Object> error) {
        this.error = error;
    }

}
