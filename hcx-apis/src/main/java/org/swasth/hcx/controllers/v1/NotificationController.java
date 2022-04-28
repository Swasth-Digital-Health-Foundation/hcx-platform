package org.swasth.hcx.controllers.v1;

import static org.swasth.common.utils.Constants.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1/notification")
public class NotificationController extends BaseController {

    @PostMapping(value = "/subscribe")
    public ResponseEntity<Object> notificationSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
       return processNotification(requestBody, ACTIVE_CODE);
    }

    @PostMapping(value = "/unsubscribe")
    public ResponseEntity<Object> notificationUnSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
        return processNotification(requestBody,INACTIVE_CODE);
    }

    @PostMapping(value = "/subscription/list")
    public ResponseEntity<Object> getSubscriptionList(@RequestBody Map<String, Object> requestBody) throws Exception {
        return getSubscriptions(requestBody);
    }

}
