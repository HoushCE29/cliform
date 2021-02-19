package dev.houshce29.cliform.util;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    public static boolean equalsAny(String str, String... possibleMatches) {
        for (String possible : possibleMatches) {
            if (possible.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static String substringAfter(String source, String delimiter) {
        int index = source.indexOf(delimiter);
        if (index == -1) {
            return source;
        }
        if (index == source.length() - 1) {
            return "";
        }
        return source.substring(index + 1);
    }
}
