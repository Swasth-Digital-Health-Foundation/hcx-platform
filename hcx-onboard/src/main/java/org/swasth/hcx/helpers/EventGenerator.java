package org.swasth.hcx.helpers;

import org.swasth.common.utils.UUIDUtils;

import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;


public class EventGenerator {

    public EventGenerator() {
    }

    public Map<String, Object> createOtpAuditEvent(String phone, String phoneOtp) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put("primary_mobile", phone);
        event.put("phone_otp", phoneOtp);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ACTION, PARTICIPANT_OTP_SEND);
        event.put("status", "success");
        event.put(ETS, System.currentTimeMillis());
        return event;
    }

}
