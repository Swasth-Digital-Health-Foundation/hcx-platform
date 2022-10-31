package org.swasth.apigateway.models;

import lombok.Data;

import java.util.Map;


@Data
public class JWERequest extends BaseRequest {


    public JWERequest(Map<String, Object> payload,boolean isJSONRequest,String apiAction, String hcxCode, String hcxRoles) throws Exception{
        super(payload,isJSONRequest,apiAction,hcxCode,hcxRoles);
    }

    //TODO write any validations for forward scenarios

}