package org.swasth.hcx.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SlugUtils {
    public static String makeEmailSlug(String primaryEmail, String role) {
        primaryEmail = primaryEmail.trim();
        String[] str = primaryEmail.split("@");
        return str[0] + "+" + role + "@" + str[1];
    }
}
