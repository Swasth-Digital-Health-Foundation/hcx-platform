package org.swasth.common.dto;

import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

public class OnboardRequest {

    private Map<String,Object> requestBody = new HashMap<>();

    public OnboardRequest(ArrayList<Map<String,Object>> body) {
        if (!body.isEmpty() && (body.get(0).containsKey(PRIMARY_EMAIL) || body.get(0).containsKey(PRIMARY_MOBILE))) {
            for(Map<String,Object> map: body) {
                if(map.containsKey(PRIMARY_EMAIL)) {
                    requestBody.put(PRIMARY_EMAIL, map.get(PRIMARY_EMAIL));
                    requestBody.put(EMAIL_OTP, map.get(OTP));
                } else if (map.containsKey(PRIMARY_MOBILE)) {
                    requestBody.put(PRIMARY_MOBILE, map.get(PRIMARY_MOBILE));
                    requestBody.put(PHONE_OTP, map.get(OTP));
                }
            }
        } else {
            requestBody.putAll(body.get(0));
        }
    }

    public Map<String,Object> getBody() {
        return this.requestBody;
    }
}
