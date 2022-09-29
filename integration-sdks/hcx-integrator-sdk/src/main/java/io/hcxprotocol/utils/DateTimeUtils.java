package io.hcxprotocol.utils;

import org.joda.time.DateTime;

public class DateTimeUtils {

    public static boolean validTimestamp(String timestamp) throws Exception {
        try {
            DateTime requestTime = new DateTime(timestamp);
            DateTime currentTime = DateTime.now();
            return !requestTime.isAfter(currentTime);
        } catch (Exception e) {
            throw new Exception("Timestamp should be a valid ISO-8061 format, Exception msg: " + e.getMessage());
        }
    }

}
