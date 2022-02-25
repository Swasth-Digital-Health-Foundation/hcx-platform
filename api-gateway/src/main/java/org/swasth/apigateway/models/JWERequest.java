package org.swasth.apigateway.models;

import lombok.Data;
import org.swasth.apigateway.models.BaseRequest;

import java.util.Map;


@Data
public class JWERequest extends BaseRequest {


    public JWERequest(Map<String, Object> payload,boolean isJSONRequest,String apiAction) throws Exception{
        super(payload,isJSONRequest,apiAction);
    }

    //TODO write any validations for forward scenarios

}