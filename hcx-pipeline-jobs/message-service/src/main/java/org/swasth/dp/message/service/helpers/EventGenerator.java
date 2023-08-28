package org.swasth.dp.message.service.helpers;

import org.swasth.dp.core.util.Constants;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EventGenerator {

    public Map<String,Object> createMessageDispatchAudit(Map<String,Object> event, Map<String,Object> errorDetails){
        Map<String,Object> audit = new HashMap<>();
        audit.put(Constants.EID(), "MESSAGE");
        audit.put(Constants.MID(), event.get(Constants.MID()).toString());
        audit.put(Constants.ETS(), Calendar.getInstance().getTime());
        audit.put(Constants.CHANNEL(), event.get(Constants.CHANNEL()));
        if(!errorDetails.isEmpty()) {
            audit.put(Constants.ERROR_DETAILS(), errorDetails);
            audit.put(Constants.STATUS(), "error");
        } else {
            audit.put(Constants.STATUS(), "dispatched");
        }
        return audit;
    }
}
