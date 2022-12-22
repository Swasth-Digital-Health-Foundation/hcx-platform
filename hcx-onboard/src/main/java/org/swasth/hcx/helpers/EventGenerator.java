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
        event.put(ACTION, PARTICIPANT_SEND_OTP);
        event.put("status", "success");
        event.put(ETS, System.currentTimeMillis());
        return event;
    }

    public Map<String,Object> createVerifyAuditEvent(String email,String emailOtp,String phoneOtp,String participantCode) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put("primary_email", email);
        event.put("email_otp", emailOtp);
        event.put("phone_otp", phoneOtp);
        event.put("participant_code", participantCode);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ACTION, PARTICIPANT_VERIFY_OTP);
        event.put("status", "success");
        event.put(ETS, System.currentTimeMillis());
        return event;
    }

}
