package org.swasth.common.utils;

import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class SlugUtils {

    public static String makeSlug(String input, String appender, String fieldSeparator, String instanceName) {
        // Remove extra spaces
        input = input.trim();
        String[] str = input.split("@");
        appender = appender.isEmpty() ? "" : "-" + appender;
        String output = str[0] + fieldSeparator + str[1].split("\\.")[0] + appender + "@" + instanceName;
        return output.toLowerCase(Locale.ENGLISH);
    }

}
