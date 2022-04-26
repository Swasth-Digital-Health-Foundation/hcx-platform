package org.swasth.hcx.controllers.v1;

import static org.swasth.common.utils.Constants.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1/notification")
public class NotificationController extends BaseController {

    @RequestMapping(value = "/subscribe", method = RequestMethod.POST)
    public ResponseEntity<Object> notificationSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
       return processNotification(requestBody, ACTIVE_CODE);
    }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
    public ResponseEntity<Object> notificationUnSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
        return processNotification(requestBody,INACTIVE_CODE);
    }

    @RequestMapping(value = "/subscription/list", method = RequestMethod.POST)
    public ResponseEntity<Object> getSubscriptionList(@RequestBody Map<String, Object> requestBody) throws Exception {
        return getSubscriptions(requestBody);
    }

}
