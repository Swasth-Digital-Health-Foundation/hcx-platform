package org.swasth.common.utils;

import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class SlugUtils {
    public static String makeSlug(String name, String roleAppender, String randomSeq, String fieldSeparator, String instanceName) {
        String output = roleAppender + fieldSeparator + name + fieldSeparator + randomSeq + "@" + instanceName;
        return output.toLowerCase(Locale.ENGLISH);
    }

}
