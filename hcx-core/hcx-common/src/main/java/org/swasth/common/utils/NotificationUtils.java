package org.swasth.common.utils;

import java.io.IOException;
import java.util.*;

import static org.swasth.common.utils.Constants.TOPIC_CODE;

public class NotificationUtils {

    public NotificationUtils() throws IOException {
        loadNotifications();
    }

    public static List<Map<String, Object>> notificationList = null;

    public static List<String> topicCodeList = new ArrayList<>();

    private void loadNotifications() throws IOException {
        notificationList = JSONUtils.convertJson(getClass().getClassLoader().getResourceAsStream("notifications.json"), List.class);
        notificationList.forEach(obj -> topicCodeList.add((String) obj.get(TOPIC_CODE)));
    }

    public static boolean isValidCode(String code) {
         return topicCodeList.contains(code);
    }

    public static Map<String,Object> getNotification(String code) {
        Map<String,Object> notification = new HashMap<>();
        Optional<Map<String,Object>> result = notificationList.stream().filter(obj -> obj.get(TOPIC_CODE).equals(code)).findFirst();
        if(result.isPresent()) notification = result.get();
        return notification;
    }

}
