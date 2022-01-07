package org.swasth.hcx.pojo;

import org.swasth.common.JsonUtils;
import org.swasth.hcx.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class RequestBody {

    private Map<String, Object> body;
    private Map<String, Object> reqProtected;

    public RequestBody(Map<String, Object> body) throws Exception {
        this.body = body;
        setProtected();

    }

    private void setProtected() throws Exception {
        reqProtected = JsonUtils.decodeBase64String((String) body.get(Constants.PROTECTED), HashMap.class);
    }

    public String getWorkflowId() {
        return (String) reqProtected.getOrDefault(Constants.WORKFLOW_ID, null);
    }
}
