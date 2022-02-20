package org.swasth.apigateway.models;

import lombok.Data;
import org.swasth.apigateway.service.AuditService;
import org.swasth.apigateway.service.RegistryService;

import java.util.Map;

@Data
public class JWERequest extends BaseRequest{


    public JWERequest(RegistryService registryService, AuditService auditService, Map<String, Object> payload,boolean isJSONRequest,String apiAction) throws Exception{
        super(registryService, auditService, payload,isJSONRequest,apiAction);
    }

    //TODO write any validations for forward scenarios

}
