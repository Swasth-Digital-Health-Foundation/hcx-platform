package org.swasth.hcx.pojos;

import java.util.List;
import java.util.UUID;

public class ResponseParams {
    public String resmsgid;
    private String msgid;
    private String err;
    private Response.Status status;
    private String errmsg;

    public ResponseParams() {
        this.msgid = UUID.randomUUID().toString();
        this.resmsgid = "";
        this.err = "";
        this.errmsg = "";
        this.status = Response.Status.SUCCESSFUL; // When there is no error, treat status as success
    }

    public String getResmsgid() {
        return resmsgid;
    }

    public void setResmsgid(String resmsgid) {
        this.resmsgid = resmsgid;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public Response.Status getStatus() {
        return status;
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

}
