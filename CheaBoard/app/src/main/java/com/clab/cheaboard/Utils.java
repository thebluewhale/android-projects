package com.clab.cheaboard;

import java.util.HashMap;

public class Utils {
    static int ALPHABET_SIZE = 26;
    static HashMap<String, Integer> BOOLEAN_SETTINGS_MENU_MAP = new HashMap<String, Integer>() {{
        put("settings_use_swipe_popup", 0);
        put("settings_use_vibration_feedback", 0);
        put("settings_use_sound_feedback", 0);
        put("settings_use_auto_complete", 0);
        put("settings_longpress_time", 0);
        put("settings_use_number_row", 0);
        put("settings_use_auto_period", 0);
        put("settings_use_backkey_longpress", 0);
        put("settings_use_trie", 1);
    }};

    static String SETTINGS_USE_SWIPE_POPUP = "settings_use_swipe_popup";
    static String SETTINGS_USE_VIBRATION_FEEDBACK = "settings_use_vibration_feedback";
    static String SETTINGS_USE_AUTO_PERIOD = "settings_use_auto_period";
    static String SETTINGS_USE_BACKKEY_LONGPRESS = "settings_use_backkey_longpress";
    static String SETTINGS_USE_TRIE = "settings_use_trie";

    static int STATE_SHIFT = 1;
    static int STATE_SYMBOL = 2;
    static int STATE_NUMBER = 4;

    static int KEYBOARD_TYPE_ENGLISH = 0;
    static int KEYBOARD_TYPE_KOREAN = 1;

    static int LONGPRESS_TIMER_DELAY = 1000;
    static int LONGPRESS_TIMER_PERIOD = 100;

    static int GESTURE_QUEUE_SIZE = 20;
    static int GESTURE_GUIDE_SIZE = 145;

    static final int VIBRATION_DURATION = 50;
    static final int VIBRATION_AMPLITUDE = 30;

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

    static boolean isStringContainsAlphabetOnly(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Utils.isCharacterOrNumber(str.charAt(i)) == CHARACTER_TYPE.NUMBER ||
                    Utils.isCharacterOrNumber(str.charAt(i)) == CHARACTER_TYPE.SYMBOL) {
                return false;
            }
        }
        return true;
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


    static int charToInt(char c) {
        return (int)c;
    }

    static char intToChar(int i) {
        return (char)i;
    }

}
