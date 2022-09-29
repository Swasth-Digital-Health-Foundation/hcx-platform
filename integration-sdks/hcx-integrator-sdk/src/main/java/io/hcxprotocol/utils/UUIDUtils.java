package io.hcxprotocol.utils;

/**
 * The UUID Util to validate uuid format.
 */
public class UUIDUtils {
    public static boolean isUUID(String s) {
        return s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}
