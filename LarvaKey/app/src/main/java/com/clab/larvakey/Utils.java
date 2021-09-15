package com.clab.larvakey;

public class Utils {
    static int ALPHABET_SIZE = 26;

    static String BOOLEAN_SETTINGS_MENU_LIST[] = {
            "settings_use_swipe_popup", "settings_use_vibration_feedback",
            "settings_use_sound_feedback", "settings_use_auto_complete", "settings_longpress_time",
            "settings_use_number_row", "settings_use_auto_period", "settings_use_backkey_longpress"
    };
    static String SETTINGS_USE_SWIPE_POPUP = "settings_use_swipe_popup";
    static String SETTINGS_USE_VIBRATION_FEEDBACK = "settings_use_vibration_feedback";
    static String SETTINGS_USE_AUTO_PERIOD = "settings_use_auto_period";
    static String SETTINGS_USE_NUMBER_ROW = "settings_use_number_row";
    static String GESTURE_DIRECTION_UP = "GESTURE_DIRECTION_UP";
    static String GESTURE_DIRECTION_RIGHTUP = "GESTURE_DIRECTION_RIGHTUP";
    static String GESTURE_DIRECTION_RIGHT = "GESTURE_DIRECTION_RIGHT";
    static String GESTURE_DIRECTION_RIGHTDOWN = "GESTURE_DIRECTION_RIGHTDOWN";
    static String GESTURE_DIRECTION_DOWN = "GESTURE_DIRECTION_DOWN";
    static String GESTURE_DIRECTION_LEFTDOWN = "GESTURE_DIRECTION_LEFTDOWN";
    static String GESTURE_DIRECTION_LEFT = "GESTURE_DIRECTION_LEFT";
    static String GESTURE_DIRECTION_LEFTUP = "GESTURE_DIRECTION_LEFTUP";

    static int STATE_SHIFT = 1;
    static int STATE_SYMBOL = 2;
    static int STATE_NUMBER = 4;

    static int LONGPRESS_TIMER_DELAY = 1000;
    static int LONGPRESS_TIMER_PERIOD = 200;

    static CHARACTER_TYPE isCharacterOrNumber(char c) {
        if (c >= 'a' && c <= 'z') {
            return CHARACTER_TYPE.ALPHABET_LOWERCASE;
        } else if (c >= 'A' && c <= 'Z') {
            return CHARACTER_TYPE.ALPHABET_UPPERCASE;
        } else if (c >= '0' && c <= '9') {
            return CHARACTER_TYPE.NUMBER;
        } else {
            return CHARACTER_TYPE.SYMBOL;
        }
    }

    static boolean isFunctionKey(String data) {
        if ("SHI".equals(data) || "SYM".equals(data) || "DEL".equals(data) ||
                "SPA".equals(data) || "SET".equals(data) || "ENT".equals(data)) {
            return true;
        }
        return false;
    }

    static double mean(int[] arr) {
        int total = 0;
        for (int i = 0; i < arr.length; i++) {
            total = total + arr[i];
        }
        return total / arr.length;
    }

    static double variance(int[] arr) {
        double totalDev = 0;
        double totalMean = mean(arr);
        for (int i = 0; i < arr.length; i++) {
            totalDev = totalDev + Math.pow(totalMean - arr[i], 2);
        }
        return totalDev / (arr.length - 1);
    }

    static double standardDeviation(int[] arr) {
        return Math.sqrt(variance(arr));
    }

}
