package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class OnboardResponse {
    private long timestamp = System.currentTimeMillis();

    @JsonProperty("applicant_code")
    private String applicantCode;

    @JsonProperty("verifier_code")
    private String verifierCode;

    private String result = "";

    public OnboardResponse(String applicantCode, String verifierCode) {
        this.applicantCode = applicantCode;
        this.verifierCode = verifierCode;
    }

    public void setResult(String resultStr){
        this.result = resultStr;
    }

    public String getResult(){
        return this.result;
    }


}
