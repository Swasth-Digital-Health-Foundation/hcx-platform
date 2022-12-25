package org.swasth.common.dto;

import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

public class OnboardRequest {

    private Map<String,Object> requestBody;

    public OnboardRequest(ArrayList<Map<String,Object>> body) {
        if (requestBody.containsKey(EMAIL_OTP) && requestBody.containsKey(PHONE_OTP)) {
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
