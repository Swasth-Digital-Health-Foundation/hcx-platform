package org.swasth.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.PAYLOAD;

public class PayloadUtils {

    public static String removeEncryptionKey(Map<String, Object> payload) throws JsonProcessingException {
        if(payload.containsKey(PAYLOAD)) {
            List<String> modifiedPayload = new ArrayList<>(Arrays.asList(payload.get(PAYLOAD).toString().split("\\.")));
            modifiedPayload.remove(1);
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
