package org.swasth.hcx.pojos;

import org.swasth.hcx.exception.ResponseCode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private String id;
    private String ver;
    private String ts;
    private ResponseParams params;
    private ResponseCode responseCode;
    private Map<String, Object> result = new HashMap<String, Object>();

    public Response() {
        this.ver = "1.0";
        this.ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
    }

    public Response(String apiId, ResponseCode responseCode, ResponseParams responseParams) {
        this.ver = "1.0";
        this.ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
        this.id = apiId;
        this.responseCode = responseCode;
        this.params = responseParams;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ets) {
        this.ts = ts;
    }

    public ResponseParams getParams() {
        return params;
    }

    public void setParams(ResponseParams params) {
        this.params = params;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public Object get(String key) {
        return result.get(key);
    }

    public Response put(String key, Object vo) {
        result.put(key, vo);
        return this;
    }

    public Response putAll(Map<String, Object> resultMap) {
        result.putAll(resultMap);
        return this;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public enum Status {
        SUCCESSFUL, UNSUCCESSFUL;
    }

}

