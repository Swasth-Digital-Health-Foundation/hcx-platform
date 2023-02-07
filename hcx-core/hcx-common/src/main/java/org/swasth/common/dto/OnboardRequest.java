package org.swasth.common.dto;

import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

public class OnboardRequest {

    private Map<String,Object> requestBody = new HashMap<>();

    private Map<String,Object> jwtPayload = new HashMap<>();

    private String applicantCode = "";

    private String verifierCode = "";


    public OnboardRequest(List<Map<String,Object>> body) throws Exception {
        requestBody.putAll(body.get(0));
        if (getType().equals(ONBOARD_THROUGH_JWT)) {
            jwtPayload.putAll(JSONUtils.decodeBase64String(getJWT().split("\\.")[1], Map.class));
            applicantCode = (String) jwtPayload.get(SUB);
            verifierCode = (String) jwtPayload.get(ISS);
        }  else if (getType().equals(ONBOARD_THROUGH_VERIFIER)) {
            applicantCode = (String) requestBody.get(APPLICANT_CODE);
            verifierCode = (String) requestBody.get(VERIFIERCODE);
        }
    }

    public Map<String,Object> getBody() {
        return this.requestBody;
    }

    public Map<String,Object> getParticipant() { return (Map<String, Object>) this.requestBody.getOrDefault(PARTICIPANT, new HashMap<>()); }

    public String getPrimaryEmail() { return (String) getParticipant().getOrDefault(PRIMARY_EMAIL, ""); }

    public String getPrimaryMobile() { return (String) getParticipant().getOrDefault(PRIMARY_MOBILE, ""); }

    public String getParticipantName() { return (String) getParticipant().getOrDefault(PARTICIPANT_NAME, ""); }

    public String getType() { return (String) requestBody.getOrDefault(TYPE, ""); }

    public String getJWT() { return (String) requestBody.getOrDefault(JWT, ""); }

    public String getApplicantCode() { return this.applicantCode; }

    public String getVerifierCode() { return this.verifierCode; }

    public List<Object> getAdditionalVerification() { return (List<Object>) requestBody.get(ADDITIONALVERIFICATION); }

}
