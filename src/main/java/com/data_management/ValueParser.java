package com.data_management;

/**
 * Parses the data string from a simulator message into a numeric value.
 * Handles the % suffix on Saturation values and converts the "triggered" /
 * "resolved" words on Alert records to 1.0 / 0.0.
 */
public final class ValueParser {

    private ValueParser() {}

    public static double parse(String label, String rawData) {
        if (rawData.endsWith("%")) {
            rawData = rawData.substring(0, rawData.length() - 1);
        }
        if ("Alert".equals(label)) {
            if (rawData.equalsIgnoreCase("triggered")) return 1.0;
            if (rawData.equalsIgnoreCase("resolved")) return 0.0;
        }
        return Double.parseDouble(rawData);
    }
}
