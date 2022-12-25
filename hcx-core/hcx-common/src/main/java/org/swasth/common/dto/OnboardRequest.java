package org.swasth.common.dto;

import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

public class OnboardRequest {

    private Map<String,Object> requestBody = new HashMap<>();

    public OnboardRequest(ArrayList<Map<String,Object>> body) {
        if (!body.isEmpty() && body.get(0).containsKey(EMAIL_OTP)) {
            requestBody.putAll(body.get(0));
            requestBody.putAll(body.get(1));
        } else {
            requestBody.putAll(body.get(0));
        }
    }

    public Map<String,Object> getBody() {
        return this.requestBody;
    }
}
