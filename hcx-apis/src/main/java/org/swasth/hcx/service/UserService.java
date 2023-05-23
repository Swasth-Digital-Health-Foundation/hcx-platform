package org.swasth.hcx.service;

import kong.unirest.HttpResponse;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.common.exception.ClientException;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.SlugUtils;

import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${participantCode.fieldSeparator}")
    private String fieldSeparator;

    @Value("${hcx.instanceName}")
    private String hcxInstanceName;

    public String createUserId(Map<String, Object> requestBody) throws ClientException {
        if (requestBody.containsKey(EMAIL) || requestBody.containsKey(MOBILE)) {
            if (requestBody.containsKey(EMAIL) && EmailValidator.getInstance().isValid((String) requestBody.get(EMAIL) )) {
                return SlugUtils.makeSlug((String) requestBody.get(EMAIL), "", fieldSeparator, hcxInstanceName);
            } else if (requestBody.containsKey(MOBILE)) {
                return requestBody.get(MOBILE) + "@" + hcxInstanceName;
            }
        }
        throw new ClientException("Email or mobile are mandatory");
    }
}
