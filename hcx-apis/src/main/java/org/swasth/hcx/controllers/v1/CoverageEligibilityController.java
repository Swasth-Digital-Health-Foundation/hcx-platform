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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class CoverageEligibilityController extends BaseController {

    @Value("${kafka.topic.coverageeligibility}")
    private String kafkaTopic;

    @Value("${notification.topicCodesForHIUs}")
    private List<String> topicCodes;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RegistryService registryService;

    @PostMapping(Constants.COVERAGE_ELIGIBILITY_CHECK)
    public ResponseEntity<Object> checkCoverageEligibility(@RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, Constants.COVERAGE_ELIGIBILITY_CHECK);
        Response response = new Response(request);
        try {
            // fetch the recipient roles,crate request body with filters for registry search
            List<Map<String,Object>> participantResponse = registryService.getDetails("{ \"filters\": { \"participant_code\": { \"eq\": \" " + request.getHcxSenderCode() + "\" } } }");
            List<String> roles = (List) (participantResponse.get(0)).get(ROLES);
            if(roles.contains(MEMBER_ISNP)){
                //create subscription requests based on configured topic codes
                for (String topicCode: topicCodes) {
                    request = new Request(createSubscriptionRequest(topicCode,request.getHcxSenderCode(),request.getHcxRecipientCode()), Constants.NOTIFICATION_SUBSCRIBE);
                    notificationService.processSubscription(request, ACTIVE_CODE, response);
                }
            }else{
                request = new Request(requestBody, Constants.COVERAGE_ELIGIBILITY_CHECK);
                eventHandler.processAndSendEvent(kafkaTopic, request);
            }
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandlerWithAudit(request, response, e);
        }
    }

    @PostMapping(Constants.COVERAGE_ELIGIBILITY_ONCHECK)
    public ResponseEntity<Object> onCheckCoverageEligibility(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.COVERAGE_ELIGIBILITY_ONCHECK, kafkaTopic);
    }

    private Map<String,Object> createSubscriptionRequest(String topicCode,String recipientCode,String senderCode) {
        Map<String, Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE, topicCode);
        obj.put(RECIPIENT_CODE, recipientCode);
        List<String> sendersList = new ArrayList<>() {{
            add(senderCode);
        }};
        obj.put(SENDER_LIST, sendersList);
        return obj;
    }
}
