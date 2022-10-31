package org.swasth.hcx.aspect;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServiceUnavailbleException;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.managers.HealthCheckManager;

import static org.swasth.common.response.ResponseMessage.SERVICE_UNAVAILABLE;

@Aspect
@Component
public class HealthCheckAspect {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckAspect.class);

    @Autowired
    private HealthCheckManager healthCheckManager;

    @Before("execution(* org.swasth.hcx.controllers.v1.*.*(..))")
    public void healthCheckBeforeEachAPICall() throws JsonProcessingException, ServiceUnavailbleException {
        if (!HealthCheckManager.allSystemHealthResult) {
            Response healthResp = healthCheckManager.checkAllSystemHealth();
            if (!(boolean) healthResp.get(Constants.HEALTHY)) {
                logger.error("Health check is failed : " + JSONUtils.serialize(healthResp.get(Constants.CHECKS)));
                throw new ServiceUnavailbleException(ErrorCodes.ERR_SERVICE_UNAVAILABLE, "The server is temporarily unable to service your request. Please try again later.");
            } else getSuccessLogger();
        } else getSuccessLogger();
    }

    private void getSuccessLogger() {
        logger.debug("Health check is successful");
    }
}