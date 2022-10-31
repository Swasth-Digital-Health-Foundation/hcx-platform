package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.service.NotificationService;

import java.util.*;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class CoverageEligibilityController extends BaseController {

    @Value("${kafka.topic.coverageeligibility}")
    private String kafkaTopic;

    @Autowired
    private RegistryService registryService;

    @PostMapping(Constants.COVERAGE_ELIGIBILITY_CHECK)
    public ResponseEntity<Object> checkCoverageEligibility(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.COVERAGE_ELIGIBILITY_CHECK, kafkaTopic);
    }

    @PostMapping(Constants.COVERAGE_ELIGIBILITY_ONCHECK)
    public ResponseEntity<Object> onCheckCoverageEligibility(@RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, Constants.COVERAGE_ELIGIBILITY_ONCHECK);
        // fetch the recipient roles,create request body with filters for registry search
        List<Map<String,Object>> participantResponse = registryService.getDetails("{ \"filters\": { \"participant_code\": { \"eq\": \" " + request.getHcxRecipientCode() + "\" } } }");
        List<String> roles = (List) (participantResponse.get(0)).get(ROLES);
        if(roles.contains(MEMBER_ISNP)){
            //Create subscription audit event for on_check call for any HIU user
            auditIndexer.createDocument(eventGenerator.generateSubscriptionAuditEvent(request,QUEUED_STATUS, Arrays.asList(request.getHcxSenderCode())));
        }
        return validateReqAndPushToKafka(request, kafkaTopic);
    }
}
