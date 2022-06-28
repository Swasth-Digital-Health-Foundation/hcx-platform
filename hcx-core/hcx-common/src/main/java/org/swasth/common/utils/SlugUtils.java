package org.swasth.common.utils;

import lombok.experimental.UtilityClass;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@UtilityClass
public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-\\.]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DUPDASH = Pattern.compile("-+");

    public static String makeSlug(String input) {
        String origInput = input;
        // Validate the input
        if (input == null)
            throw new IllegalArgumentException("Input is null");
        // Remove extra spaces
        input = input.trim();
        // Replace all whitespace with dashes
        input = WHITESPACE.matcher(input).replaceAll("-");
        // Remove all accent chars
        input = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Remove all non-latin special characters
        input = NONLATIN.matcher(input).replaceAll("");
        // Remove any consecutive dashes
        input = normalizeDashes(input);
        // Validate before returning
        validateResult(input, origInput);
        // Slug is always lowercase
        return input.toLowerCase(Locale.ENGLISH);
    }

    private static void validateResult(String input, String origInput) {
        // Check if we are not left with a blank
        if (input.length() == 0)
            throw new IllegalArgumentException("Failed to cleanup the input " + origInput);
    }

    public static String normalizeDashes(String text) {
        String clean = DUPDASH.matcher(text).replaceAll("-");
        // Special case that only dashes remain
        if (clean.equals("-") || clean.equals("--"))
            return "";
        int startIdx = (clean.startsWith("-") ? 1 : 0);
        int endIdx = (clean.endsWith("-") ? 1 : 0);
        clean = clean.substring(startIdx, (clean.length() - endIdx));
        return clean;
    }

}
