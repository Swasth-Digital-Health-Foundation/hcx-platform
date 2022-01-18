package org.swasth.dp.search.utils;

import org.swasth.dp.search.beans.SearchEvent;
import org.swasth.dp.search.beans.SearchResponse;

import java.util.HashMap;
import java.util.Map;

public class EventUtils {

    public static void replaceSenderAndRecipientCodes(SearchEvent request, String recipientCode, boolean isRequest) {
        // implement the method properly
        Map<String, Object> protocol = request.getProtocolHeaders();
        // keep hcx registry code as sender here
        protocol.put(Constants.SENDER_CODE,request.getRecipientCode());
        // keep individual recipient from the Search Filters
        protocol.put(Constants.RECIPIENT_CODE, recipientCode);

        //If it is while request dispatching then remove the other search filters and update the protocol header
        if(isRequest) {
            //TODO keep only this recipient in the search filters, check whether we can keep all the recipients for this release
            request.getRecipientCodes().stream().forEach(payorCode -> {
                if (!payorCode.equals(recipientCode))
                    request.getRecipientCodes().remove(payorCode);
            });
        }

        request.setProtocolHeaders(protocol);
    }

    public static Map<String, Object> parsePayload(String encodedPayload) throws Exception {
        String[] strArray = encodedPayload.split("\\.");
        if (strArray.length > 0 && strArray.length == 6) {
            Map<String, Object> event = new HashMap<>();
            event.put(Constants.PROTECTED, BaseUtils.decodeBase64String(strArray[0],Map.class));
            event.put(Constants.ENCRYPTED_KEY, BaseUtils.decodeBase64String(strArray[1],Map.class));
            event.put(Constants.AAD, BaseUtils.decodeBase64String(strArray[2],Map.class));
            event.put(Constants.IV, BaseUtils.decodeBase64String(strArray[3],Map.class));
            event.put(Constants.CIPHERTEXT, BaseUtils.decodeBase64String(strArray[4],Map.class));
            event.put(Constants.TAG, BaseUtils.decodeBase64String(strArray[5],Map.class));
            return event;
        } else {
            throw new PipelineException("payload is not complete");
        }
    }

    public static String encodePayload(Map<String,Object> payload){
        String encodedString = "";
        for (Object objValue : payload.values()){
            try {
                encodedString = BaseUtils.encodeBase64String(objValue) + Constants.HEADER_SEPARATOR;
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        //Remove the last . after adding all the values
        encodedString = encodedString.substring(0,encodedString.length() - 1);
        return encodedString;
    }

    public static Map<String,Object> prepareErrorDetails(ErrorCode errorCode){
        Map<String,Object> errorMap = new HashMap<>();
        errorMap.put(Constants.ERROR_CODE,errorCode);
        errorMap.put(Constants.ERROR_MESSAGE, "Unable to process request due to internal server error");
        errorMap.put(Constants.ERROR_TRACE, "");
        return errorMap;
    }

    public static SearchResponse generateFailResponse() {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setCount(0);
        Map<String,Object> entityCountsMap = new HashMap<>();
        entityCountsMap.put(Constants.CLAIM,0);
        entityCountsMap.put(Constants.PREAUTH,0);
        entityCountsMap.put(Constants.PREDETERMINE,0);
        searchResponse.setEntity_counts(entityCountsMap);
        return searchResponse;
    }
}
