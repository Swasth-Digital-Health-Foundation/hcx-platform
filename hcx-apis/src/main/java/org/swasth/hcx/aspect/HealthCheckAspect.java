package org.swasth.hcx.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.hcx.managers.HealthCheckManager;

@Aspect
@Component
public class HealthCheckAspect {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckAspect.class);

    @Before("execution(* org.swasth.hcx.controllers.v1.*.*(..))")
    public ResponseEntity<Response> healthCheckBeforeEachAPICall() {
        if (!HealthCheckManager.allSystemHealthResult) {
            logger.info("Health check is failed");
            return new ResponseEntity<>(new Response(new ResponseError(ErrorCodes.ERR_SERVICE_UNAVAILABLE, "Service is unavailable", null)), HttpStatus.ACCEPTED);
        } else {
            logger.info("Health check is successful");
        }
        return null;
    }
}
