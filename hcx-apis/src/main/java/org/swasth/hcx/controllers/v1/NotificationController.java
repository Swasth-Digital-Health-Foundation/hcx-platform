package org.swasth.hcx.controllers.v1;

import static org.swasth.common.utils.Constants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1/notification")
public class NotificationController extends BaseController {

    @Value("${kafka.topic.notification}")
    private String kafkaTopic;

    @PostMapping(value = "/notify")
    public ResponseEntity<Object> notificationRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        Request request = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            request.setApiAction(Constants.NOTIFICATION_REQUEST);
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

    @PostMapping(value = "/subscribe")
    public ResponseEntity<Object> notificationSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
       return processSubscription(requestBody, ACTIVE_CODE);
    }

    @PostMapping(value = "/unsubscribe")
    public ResponseEntity<Object> notificationUnSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
        return processSubscription(requestBody,INACTIVE_CODE);
    }

    @PostMapping(value = "/subscription/list")
    public ResponseEntity<Object> getSubscriptionList(@RequestBody Map<String, Object> requestBody) throws Exception {
        return getSubscriptions(requestBody);
    }

    @PostMapping(value = "/list")
    public ResponseEntity<Object> getNotificationList(@RequestBody Map<String, Object> requestBody) throws Exception {
        return getNotifications(requestBody);
    }

}
