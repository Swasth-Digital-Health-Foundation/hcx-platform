package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class NotificationController extends BaseController {

    @Value("${kafka.topic.notification}")
    private String kafkaTopic;

    @PostMapping(Constants.NOTIFICATION_REQUEST)
    public ResponseEntity<Object> notificationRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        Request request = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            request.setApiAction(Constants.NOTIFICATION_REQUEST);
            //TODO load from MasterDataUtils
            if(notificationList == null) loadNotifications();
            if (!isValidNotificaitonId(request.getNotificationId())) {
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_ID, "Invalid NotificationId." + request.getNotificationId() + " is not present in the master list of notifications");
            }
            setResponseParams(request, response);
            processAndSendEvent(kafkaTopic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }

    @PostMapping(NOTIFICATION_SUBSCRIBE)
    public ResponseEntity<Object> notificationSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
       return processSubscription(requestBody, ACTIVE_CODE);
    }

    @PostMapping(NOTIFICATION_UNSUBSCRIBE)
    public ResponseEntity<Object> notificationUnSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
        return processSubscription(requestBody,INACTIVE_CODE);
    }

    @PostMapping(NOTIFICATION_SUBSCRIPTION_LIST)
    public ResponseEntity<Object> getSubscriptionList(@RequestBody Map<String, Object> requestBody) throws Exception {
        return getSubscriptions(requestBody);
    }

    @PostMapping(NOTIFICATION_LIST)
    public ResponseEntity<Object> getNotificationList(@RequestBody Map<String, Object> requestBody) throws Exception {
        return getNotifications(requestBody);
    }

}
