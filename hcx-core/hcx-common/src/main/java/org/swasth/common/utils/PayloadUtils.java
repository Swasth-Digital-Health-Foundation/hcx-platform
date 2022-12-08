package org.swasth.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;

import java.util.*;

import static org.swasth.common.utils.Constants.*;

@UtilityClass
public class PayloadUtils {

    public static String removeSensitiveData(Map<String, Object> payload, String apiAction) throws JsonProcessingException {
        if (payload.containsKey(PAYLOAD) && !apiAction.contains(NOTIFICATION_NOTIFY)) {
            List<String> modifiedPayload = new ArrayList<>(Arrays.asList(payload.get(PAYLOAD).toString().split("\\.")));
            // remove encryption key
            modifiedPayload.remove(1);
            // remove ciphertext
            modifiedPayload.remove(2);
            String[] payloadValues = modifiedPayload.toArray(new String[modifiedPayload.size()]);
            StringBuilder sb = new StringBuilder();
            for (String value : payloadValues) {
                sb.append(value).append(".");
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        } else {
            return JSONUtils.serialize(removeParticipantDetails(payload));
        }
    }

    public static Map<String,Object> removeParticipantDetails(Map<String,Object> payload){
        Map<String,Object> map = new HashMap<>(payload);
        if (map.containsKey(SENDERDETAILS) && map.containsKey(RECIPIENTDETAILS)) {
            map.remove(SENDERDETAILS);
            map.remove(RECIPIENTDETAILS);
        }
        return map;
    }
}
