package org.swasth.hcx.models;

import org.apache.commons.lang3.StringUtils;
import org.swasth.common.utils.SlugUtils;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

public class Participant {

    private final Map<String, Object> requestBody;

    public Participant(Map<String, Object> requestbody) {
        this.requestBody = requestbody;
    }

    public String getprimaryEmail() {
        return (String) requestBody.get(PRIMARY_EMAIL);
    }

    public String getParticipantName() {
        return (String) requestBody.get(PARTICIPANT_NAME);
    }

    public String getApplicantCode() {
        return (String) requestBody.getOrDefault(APPLICANT_CODE, "");
    }

    public List<String> getRoles() {
        return (List<String>) requestBody.get(ROLES);
    }

    public String generateCode(String fieldSeparator, String hcxInstanceName) {
        String nameWithoutSpaces = getParticipantName().replaceAll("\\s", "");
        String code = SlugUtils.makeSlug(nameWithoutSpaces.substring(0, Math.min(6, nameWithoutSpaces.length())), getRoleAppender(), getRandomSeq(), fieldSeparator, hcxInstanceName);
        requestBody.put(PARTICIPANT_CODE, code);
        return code;
    }

    private String getRoleAppender() {
        if (getRoles().contains("provider.hospital")) {
            return "hosp";
        } else if (getRoles().contains("provider.clinic")) {
            return "clinic";
        } else if (getRoles().contains("provider.practitioner")) {
            return "prac";
        } else if (getRoles().contains("provider.diagnostics")) {
            return "diag";
        } else if (getRoles().contains("provider.pharmacy")) {
            return "pharm";
        } else if (getRoles().contains("payor")) {
            return "payr";
        } else if (getRoles().contains("bsp")) {
            return "bsp";
        } else if (getRoles().contains("provider")) {
            return "hosp";
        }
        return getRoles().get(0);
    }

    private String getRandomSeq(){
        if (!StringUtils.isEmpty(getApplicantCode())) {
            return getApplicantCode();
        } else {
            // generate random 6 digit number
            int random = new SecureRandom().nextInt(999999 - 100000 + 1) + 100000;
            return String.valueOf(random);
        }
    }

    public String getParticipantCode() {
        return (String) requestBody.get(PARTICIPANT_CODE);
    }
}
