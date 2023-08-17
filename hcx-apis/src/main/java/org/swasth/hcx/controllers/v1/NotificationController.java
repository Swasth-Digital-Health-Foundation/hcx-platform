package org.swasth.hcx.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.NotificationListRequest;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.service.NotificationService;

import java.util.Map;
import java.util.Objects;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class NotificationController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Value("${kafka.topic.notification}")
    private String kafkaTopic;

    @Autowired
    private NotificationService notificationService;

    @PostMapping(NOTIFICATION_LIST)
    public ResponseEntity<Object> getNotificationList(@RequestBody Map<String, Object> requestBody) {
        NotificationListRequest request = new NotificationListRequest(requestBody);
        Response response = new Response();
        try {
            logger.info(Constants.REQUEST_LOG, NOTIFICATION_LIST, request);
            notificationService.getNotifications(request, response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @PostMapping(NOTIFICATION_SUBSCRIBE)
    public ResponseEntity<Object> notificationSubscribe(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, NOTIFICATION_SUBSCRIBE, Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        Response response = new Response();
        try {
            logger.info(Constants.REQUEST_LOG, NOTIFICATION_SUBSCRIBE, requestBody);
            notificationService.processSubscription(request, ACTIVE, response);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @PostMapping(NOTIFICATION_UNSUBSCRIBE)
    public ResponseEntity<Object> notificationUnSubscribe(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, NOTIFICATION_UNSUBSCRIBE, Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        Response response = new Response();
        try {
            logger.info(Constants.REQUEST_LOG, NOTIFICATION_UNSUBSCRIBE, requestBody);
            notificationService.processSubscription(request, INACTIVE, response);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @PostMapping(NOTIFICATION_ON_SUBSCRIBE)
    public ResponseEntity<Object> notificationOnSubscribe(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, NOTIFICATION_ON_SUBSCRIBE, Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        Response response = new Response();
        try {
            logger.info(Constants.REQUEST_LOG, NOTIFICATION_ON_SUBSCRIBE, requestBody);
            notificationService.processOnSubscription(request, response);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @PostMapping(NOTIFICATION_SUBSCRIPTION_LIST)
    public ResponseEntity<Object> getSubscriptionList(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) {
        NotificationListRequest request = new NotificationListRequest(requestBody);
        Response response = new Response();
        try {
            logger.info(Constants.REQUEST_LOG, NOTIFICATION_SUBSCRIPTION_LIST, requestBody);
            notificationService.getSubscriptions(request, response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @PostMapping(Constants.NOTIFICATION_NOTIFY)
    public ResponseEntity<Object> notificationRequest(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, NOTIFICATION_NOTIFY, Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        Response response = new Response(request.getCorrelationId());
        try {
            logger.info(Constants.REQUEST_LOG, NOTIFICATION_NOTIFY, requestBody);
            notificationService.notify(request, kafkaTopic);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @PostMapping(Constants.NOTIFICATION_SUBSCRIPTION_UPDATE)
    public ResponseEntity<Object> notificationSubscriptionUpdate(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, NOTIFICATION_SUBSCRIPTION_UPDATE, Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        Response response = new Response();
        try {
            logger.info(Constants.REQUEST_LOG, NOTIFICATION_SUBSCRIPTION_UPDATE, requestBody);
            notificationService.subscriptionUpdate(request, response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

}
