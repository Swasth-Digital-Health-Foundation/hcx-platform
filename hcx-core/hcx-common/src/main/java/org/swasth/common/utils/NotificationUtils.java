package org.swasth.common.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.swasth.common.utils.Constants.TOPIC_CODE;

public class NotificationUtils {

    public NotificationUtils(String networkPath, String participantPath, String workflowPath) throws IOException {
        loadNotifications(networkPath, participantPath, workflowPath);
    }

    public static List<Map<String, Object>> notificationList = null;

    public static List<String> topicCodeList = new ArrayList<>();

    private void loadNotifications(String networkPath, String participantPath, String workflowPath) throws IOException {
        List<Map<String, Object>> networkList = YamlUtils.convertYaml(new FileInputStream(networkPath), List.class);
        List<Map<String, Object>> participantList = YamlUtils.convertYaml(new FileInputStream(participantPath), List.class);
        List<Map<String, Object>> workflowList = YamlUtils.convertYaml(new FileInputStream(workflowPath), List.class);
        notificationList = Stream.of(networkList, participantList, workflowList).flatMap(Collection::stream).collect(Collectors.toList());
        notificationList.forEach(obj -> topicCodeList.add((String) obj.get(TOPIC_CODE)));
    }

    public static boolean isValidCode(String code) {
        return topicCodeList.contains(code);
    }

    public static Map<String, Object> getNotification(String code) {
        Map<String, Object> notification = new HashMap<>();
        Optional<Map<String, Object>> result = notificationList.stream().filter(obj -> obj.get(TOPIC_CODE).equals(code)).findFirst();
        if (result.isPresent()) notification = result.get();
        return notification;
    }

}
