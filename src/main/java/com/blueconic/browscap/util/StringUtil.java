package com.blueconic.browscap.util;

/** Util class for common String operations. */
public final class StringUtil {

    // Hide the constructor
    private StringUtil() {
    }

    /**
     * <p>
     * Checks if a value is whitespace, empty ("") or null.
     * </p>
     * @param value the value to check, may be null
     * @return {@code true} if the value is null, empty or whitespace, {@code false} otherwise.
     */
    public static boolean isBlank(final CharSequence value) {

        // Check the argument
        if (value == null) {
            return true;
        }

        // Check the length
        final int length = value.length();
        if (length == 0) {
            return true;
        }

        // Check for any non-whitespace
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }

        // Only whitespace characters found
        return true;
    }

    /**
     * <p>
     * Checks if a value is not empty (""), not null and not whitespace only.
     * </p>
     * @param value the value to check, may be null
     * @return {@code true} if the value is not empty and not null and not whitespace, {@code false} otherwise.
     */
    public static boolean isNotBlank(final CharSequence value) {
        return !isBlank(value);
    }
}
