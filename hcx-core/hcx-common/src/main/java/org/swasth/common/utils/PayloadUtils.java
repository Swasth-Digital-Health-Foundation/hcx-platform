package org.swasth.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.PAYLOAD;

@UtilityClass
public class PayloadUtils {

    public static String removeSensitiveData(Map<String, Object> payload) throws JsonProcessingException {
        if(payload.containsKey(PAYLOAD)) {
            List<String> modifiedPayload = new ArrayList<>(Arrays.asList(payload.get(PAYLOAD).toString().split("\\.")));
            // remove encryption key
            modifiedPayload.remove(1);
            // remove ciphertext
            modifiedPayload.remove(2);
            String[] payloadValues = modifiedPayload.toArray(new String[modifiedPayload.size()]);
            StringBuilder sb = new StringBuilder();
            for(String value: payloadValues) {
                sb.append(value).append(".");
            }
            return sb.deleteCharAt(sb.length()-1).toString();
        } else {
            return JSONUtils.serialize(payload);
        }
    }

}
