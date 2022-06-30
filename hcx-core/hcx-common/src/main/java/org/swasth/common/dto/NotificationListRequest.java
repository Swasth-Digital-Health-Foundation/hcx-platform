package org.swasth.common.dto;

import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;

import java.util.Map;

import static org.swasth.common.utils.Constants.FILTERS;

public class NotificationListRequest {

    private Map<String,Object> filters;

    public NotificationListRequest(Map<String,Object> requestBody) throws ClientException {
        if (!requestBody.containsKey(FILTERS))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Notification filters property is missing");
        this.filters = (Map<String, Object>) requestBody.get(Constants.FILTERS);
    }

    public Map<String,Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String,Object> filters) {
        this.filters = filters;
    }
}
