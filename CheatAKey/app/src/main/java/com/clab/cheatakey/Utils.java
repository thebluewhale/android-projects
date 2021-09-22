package com.clab.cheatakey;

public class Utils {
    static int ALPHABET_SIZE = 26;
    static String BOOLEAN_SETTINGS_MENU_LIST[] = {
            "settings_use_swipe_popup", "settings_use_vibration_feedback",
            "settings_use_sound_feedback", "settings_use_auto_complete", "settings_longpress_time",
            "settings_use_number_row", "settings_use_auto_period", "settings_use_backkey_longpress"
    };
    static String INTEGER_SETTINGS_MENU_LIST[] = {
            "settings_keyboard_type"
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

    static String SETTINGS_KEYBOARD_TYPE = "settings_keyboard_type";
    static int SETTINGS_KEYBOARD_TYPE_JOKEY = 1;
    static int SETTINGS_KEYBOARD_TYPE_PRINKEY = 2;
    static int SETTINGS_KEYBOARD_TYPE_JANKEY = 3;
    static int SETTINGS_KEYBOARD_TYPE_SUNKEY = 4;

    static int GESTURE_GUIDE_VIEW_SIZE = 30;
    static int GESTURE_GUIDE_VIEW_DISTANCE = 45;

    static boolean isWordContainsAlphabetOnly(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!((str.charAt(i) >= 'a' && str.charAt(i) <= 'z') ||
                    (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z'))) {
                return false;
            }
        }
        return true;
    }
}
