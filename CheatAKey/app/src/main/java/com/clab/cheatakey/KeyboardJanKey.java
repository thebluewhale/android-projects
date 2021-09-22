package com.clab.cheatakey;

import android.annotation.SuppressLint;
import android.os.VibrationEffect;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Locale;

/** Controls the visible virtual keyboard view. */
final class KeyboardJanKey {

    private static final int STATE_NORMAL = 0;
    private static final int STATE_SHIFT = 1;
    private static final int STATE_SYMBOL = 2;
    private static final int NUM_STATES = 4;

    private enum GESTURE_DIRECTION {
        GUIDE_A(0), GUIDE_E(1), GUIDE_I(2),
        GUIDE_O(3), GUIDE_U(4), GUIDE_NONE(5);
        private final int index;
        GESTURE_DIRECTION(int index) {
            this.index = index;
        }
        public int toInt() {
            return index;
        }
    }

    private final CheatAKeyService mCheatAKeyService;
    private final int mViewResId;
    private final SparseArray<String> mKeyMapping;
    private View mKeyboardView;
    private LayoutInflater mLayoutInflater;
    private final CustomVariables mCustomVariables = new CustomVariables();
    private DataBaseHelper mDataBaseHelper;
    private int mState;
    private final int[] mGestureDirectionUsedFlag = new int[6];
    private final String[] mDirectionCharacterMap = new String[6];
    private boolean mGestureGuideViewIsShown;
    private final PopupWindow mGestureGuideViewAContainer = new PopupWindow();
    private final PopupWindow mGestureGuideViewEContainer = new PopupWindow();
    private final PopupWindow mGestureGuideViewIContainer = new PopupWindow();
    private final PopupWindow mGestureGuideViewOContainer = new PopupWindow();
    private final PopupWindow mGestureGuideViewUContainer = new PopupWindow();

    private KeyboardJanKey(CheatAKeyService cheatAKeyService, int viewResId,
                     SparseArray<String> keyMapping) {
        this.mCheatAKeyService = cheatAKeyService;
        this.mViewResId = viewResId;
        this.mKeyMapping = keyMapping;
        this.mState = 0;

        initializeDataBaseHelper();
        initializeGestureDirectionUsedFlag();
        initializeDirectionCharacterMap();
    }

    private String getLabel(String data) {
        if ("SHI".equals(data)) {
            if (mState == STATE_SYMBOL) {
                return "1/2";
            } else if (mState == STATE_SYMBOL + STATE_SHIFT) {
                return "2/2";
            }
            return "↑";
        } else if ("DEL".equals(data)) {
            return "←";
        } else if ("SYM".equals(data)) {
            if (mState == STATE_NORMAL || mState == STATE_NORMAL + STATE_SHIFT) {
                return "#!1";
            } else {
                return "abc";
            }
        } else if ("SPA".equals(data)) {
            return "SPACE";
        } else if ("ENT".equals(data)) {
            return "Enter";
        } else if ("VOWEL".equals(data)) {
            return "●";
        } else {
            return data;
        }
    }

    static KeyboardJanKey jankey(CheatAKeyService cheatAKeyService) {
        SparseArray<String> keyMapping = new SparseArray<>();
        keyMapping.put(R.id.key_pos_wave_mark, "~");
        keyMapping.put(R.id.key_pos_0_1, "wW+`");
        keyMapping.put(R.id.key_pos_0_2, "rR×\\");
        keyMapping.put(R.id.key_pos_0_3, "tT÷|");
        keyMapping.put(R.id.key_pos_0_4, "yY=○");
        keyMapping.put(R.id.key_pos_0_5, "pP/●");
        keyMapping.put(R.id.key_pos_exclamation_mark, "!");

        keyMapping.put(R.id.key_pos_caret_mark, "^");
        keyMapping.put(R.id.key_pos_1_1, "sS<□");
        keyMapping.put(R.id.key_pos_1_2, "dD>■");
        keyMapping.put(R.id.key_pos_1_3, "fF_/");
        keyMapping.put(R.id.key_pos_1_4, "gG-◇");
        keyMapping.put(R.id.key_pos_1_5, "hH-◇");
        keyMapping.put(R.id.key_pos_question_mark, "?");

        keyMapping.put(R.id.key_pos_comma_mark, ",");
        keyMapping.put(R.id.key_pos_2_1, "jJ(≪");
        keyMapping.put(R.id.key_pos_2_2, "kK)≫");
        keyMapping.put(R.id.key_pos_2_3, "lL[℃");
        keyMapping.put(R.id.key_pos_2_4, "xX]℉");
        keyMapping.put(R.id.key_pos_2_5, "cC♡∞");
        keyMapping.put(R.id.key_pos_period_mark, ".");

        keyMapping.put(R.id.key_pos_symbol, "SYM");
        keyMapping.put(R.id.key_pos_3_1, "vV#¡");
        keyMapping.put(R.id.key_pos_3_2, "bB%¿");
        keyMapping.put(R.id.key_pos_3_3, "nN&$");
        keyMapping.put(R.id.key_pos_3_4, "mM@₤");
        keyMapping.put(R.id.key_pos_vowel, "VOWEL");
        keyMapping.put(R.id.key_pos_del, "DEL");

        keyMapping.put(R.id.key_pos_shift, "SHI");
        keyMapping.put(R.id.key_pos_4_1, "zZ≤◉");
        keyMapping.put(R.id.key_pos_4_2, "qQ≥◎");
        keyMapping.put(R.id.key_pos_space, "SPA");
        keyMapping.put(R.id.key_pos_enter, "ENT");

        return new KeyboardJanKey(cheatAKeyService, R.layout.keyboard_jankey, keyMapping);
    }

    static KeyboardJanKey jankey_num(CheatAKeyService cheatAKeyService) {
        SparseArray<String> keyMapping = new SparseArray<>();
        keyMapping.put(R.id.key_pos_wave_mark, "~");
        keyMapping.put(R.id.key_pos_0_1, "wW+`");
        keyMapping.put(R.id.key_pos_0_2, "rR×\\");
        keyMapping.put(R.id.key_pos_0_3, "tT÷|");
        keyMapping.put(R.id.key_pos_0_4, "yY=○");
        keyMapping.put(R.id.key_pos_0_5, "pP/●");
        keyMapping.put(R.id.key_pos_exclamation_mark, "!");

        keyMapping.put(R.id.key_pos_caret_mark, "^");
        keyMapping.put(R.id.key_pos_1_1, "sS<□");
        keyMapping.put(R.id.key_pos_1_2, "dD>■");
        keyMapping.put(R.id.key_pos_1_3, "fF_/");
        keyMapping.put(R.id.key_pos_1_4, "gG-◇");
        keyMapping.put(R.id.key_pos_1_5, "hH-◇");
        keyMapping.put(R.id.key_pos_question_mark, "?");

        keyMapping.put(R.id.key_pos_comma_mark, ",");
        keyMapping.put(R.id.key_pos_2_1, "jJ(≪");
        keyMapping.put(R.id.key_pos_2_2, "kK)≫");
        keyMapping.put(R.id.key_pos_2_3, "lL[℃");
        keyMapping.put(R.id.key_pos_2_4, "xX]℉");
        keyMapping.put(R.id.key_pos_2_5, "cC♡∞");
        keyMapping.put(R.id.key_pos_period_mark, ".");

        keyMapping.put(R.id.key_pos_symbol, "SYM");
        keyMapping.put(R.id.key_pos_3_1, "vV#¡");
        keyMapping.put(R.id.key_pos_3_2, "bB%¿");
        keyMapping.put(R.id.key_pos_3_3, "nN&$");
        keyMapping.put(R.id.key_pos_3_4, "mM@₤");
        keyMapping.put(R.id.key_pos_vowel, "VOWEL");
        keyMapping.put(R.id.key_pos_del, "DEL");

        keyMapping.put(R.id.key_pos_shift, "SHI");
        keyMapping.put(R.id.key_pos_4_1, "zZ≤◉");
        keyMapping.put(R.id.key_pos_4_2, "qQ≥◎");
        keyMapping.put(R.id.key_pos_space, "SPA");
        keyMapping.put(R.id.key_pos_enter, "ENT");

        keyMapping.put(R.id.key_pos_num_0, "0");
        keyMapping.put(R.id.key_pos_num_1, "1");
        keyMapping.put(R.id.key_pos_num_2, "2");
        keyMapping.put(R.id.key_pos_num_3, "3");
        keyMapping.put(R.id.key_pos_num_4, "4");
        keyMapping.put(R.id.key_pos_num_5, "5");
        keyMapping.put(R.id.key_pos_num_6, "6");
        keyMapping.put(R.id.key_pos_num_7, "7");
        keyMapping.put(R.id.key_pos_num_8, "8");
        keyMapping.put(R.id.key_pos_num_9, "9");

        return new KeyboardJanKey(cheatAKeyService, R.layout.keyboard_jankey_num, keyMapping);
    }

    View inflateKeyboardView(LayoutInflater inflater, InputView inputView) {
        mLayoutInflater = inflater;
        mKeyboardView = mLayoutInflater.inflate(mViewResId, inputView, false);
        mapKeys();
        return mKeyboardView;
    }

    private boolean onSoftkeyTouch(View view, MotionEvent evt, TextView softkey, int index, String data) {
        int []outLocation = new int[2];
        view.getLocationInWindow(outLocation);
        int action = evt.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                showGestureGuideIfNeeded(view, data);
                softkey.setBackgroundResource(R.drawable.softkey_shape_press);
                handleTouchDown(data);
                break;
            case MotionEvent.ACTION_UP:
                hideGestureGuide();
                softkey.setBackgroundResource(R.drawable.softkey_shape_normal);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mGestureGuideViewIsShown) {
                    return true;
                }
                GESTURE_DIRECTION loc = getGestureLocation(softkey, evt.getX(), evt.getY());
                if (getGestureDirectionUsedFlag(loc)) {
                    // skip
                } else {
                    updateGestureDirectionUsedFlag(loc, 1);
                    String gestureInput = getDirectionCharacter(loc);
                    handleTouchDown(mState == STATE_SHIFT ? gestureInput.toUpperCase(Locale.ROOT) : gestureInput);
                }
                break;
            default:
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void mapKeys() {
        for (int i = 0; i < mKeyMapping.size(); i++) {
            TextView softkey = mKeyboardView.findViewById(mKeyMapping.keyAt(i));
            if (softkey != null) {
                String rawData = mKeyMapping.valueAt(i);
                String data = rawData.length() != NUM_STATES ? rawData : rawData.substring(mState, mState + 1);
                softkey.setText(getLabel(data));
                final int index = i;
                softkey.setOnTouchListener((view, evt) -> onSoftkeyTouch(view, evt, softkey, index, data));
            }
        }
    }

    private void showGestureGuideIfNeeded(View view, String data) {
        mGestureGuideViewIsShown = false;
        if (!mDataBaseHelper.getBooleanSettingValue(mCustomVariables.SETTINGS_USE_SWIPE_POPUP)) {
            return;
        }
        if ((mState == STATE_SYMBOL) || (mState == STATE_SYMBOL + STATE_SHIFT)) {
            return;
        }
        if (!("SHI".equals(data) || "SYM".equals(data) || "DEL".equals(data) ||
                "SPA".equals(data) || "ENT".equals(data) || "~".equals(data) ||
                "!".equals(data) || "^".equals(data) || "?".equals(data) ||
                ";".equals(data) || ".".equals(data) ||
                "*".equals(data) || ",".equals(data))) {
            mGestureGuideViewIsShown = true;

            TextView softkey = (TextView) view;
            int []softkeyLocation = new int[2];
            softkey.getLocationInWindow(softkeyLocation);
            int[] newLocation = new int[2];

            // A
            getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                    dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -30, newLocation);
            View mGestureGuideViewA = mLayoutInflater.inflate(R.layout.gesture_guide_jankey, null);
            ((TextView) mGestureGuideViewA).setText(mState == STATE_SHIFT ? "A" : "a");

            if (mGestureGuideViewA.getParent() != null) {
                ((ViewGroup) mGestureGuideViewA.getParent()).removeView(mGestureGuideViewA);
            }
            mGestureGuideViewAContainer.setContentView(mGestureGuideViewA);
            mGestureGuideViewAContainer.setWidth(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewAContainer.setHeight(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewAContainer.showAtLocation(view, 0, newLocation[0], newLocation[1]);

            // E
            getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                    dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -80, newLocation);
            View mGestureGuideViewE = mLayoutInflater.inflate(R.layout.gesture_guide_jankey, null);
            ((TextView) mGestureGuideViewE).setText(mState == STATE_SHIFT ? "E" : "e");

            if (mGestureGuideViewE.getParent() != null) {
                ((ViewGroup) mGestureGuideViewE.getParent()).removeView(mGestureGuideViewE);
            }
            mGestureGuideViewEContainer.setContentView(mGestureGuideViewE);
            mGestureGuideViewEContainer.setWidth(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewEContainer.setHeight(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewEContainer.showAtLocation(view, 0, newLocation[0], newLocation[1]);

            // I
            getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                    dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -130, newLocation);
            View mGestureGuideViewI = mLayoutInflater.inflate(R.layout.gesture_guide_jankey, null);
            ((TextView) mGestureGuideViewI).setText(mState == STATE_SHIFT ? "I" : "i");

            if (mGestureGuideViewI.getParent() != null) {
                ((ViewGroup) mGestureGuideViewI.getParent()).removeView(mGestureGuideViewI);
            }
            mGestureGuideViewIContainer.setContentView(mGestureGuideViewI);
            mGestureGuideViewIContainer.setWidth(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewIContainer.setHeight(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewIContainer.showAtLocation(view, 0, newLocation[0], newLocation[1]);

            // O
            getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                    dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -180, newLocation);
            View mGestureGuideViewO = mLayoutInflater.inflate(R.layout.gesture_guide_jankey, null);
            ((TextView) mGestureGuideViewO).setText(mState == STATE_SHIFT ? "O" : "o");

            if (mGestureGuideViewO.getParent() != null) {
                ((ViewGroup) mGestureGuideViewO.getParent()).removeView(mGestureGuideViewO);
            }
            mGestureGuideViewOContainer.setContentView(mGestureGuideViewO);
            mGestureGuideViewOContainer.setWidth(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewOContainer.setHeight(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewOContainer.showAtLocation(view, 0, newLocation[0], newLocation[1]);

            // U
            getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                    dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -230, newLocation);
            View mGestureGuideViewU = mLayoutInflater.inflate(R.layout.gesture_guide_jankey, null);
            ((TextView) mGestureGuideViewU).setText(mState == STATE_SHIFT ? "U" : "u");

            if (mGestureGuideViewU.getParent() != null) {
                ((ViewGroup) mGestureGuideViewU.getParent()).removeView(mGestureGuideViewU);
            }
            mGestureGuideViewUContainer.setContentView(mGestureGuideViewU);
            mGestureGuideViewUContainer.setWidth(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewUContainer.setHeight(dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE));
            mGestureGuideViewUContainer.showAtLocation(view, 0, newLocation[0], newLocation[1]);
        }
    }

    private void hideGestureGuide() {
        mGestureGuideViewAContainer.dismiss();
        mGestureGuideViewEContainer.dismiss();
        mGestureGuideViewIContainer.dismiss();
        mGestureGuideViewOContainer.dismiss();
        mGestureGuideViewUContainer.dismiss();
    }

    private void handleTouchDown(String data) {
        if (mDataBaseHelper.getBooleanSettingValue(mCustomVariables.SETTINGS_USE_VIBRATION_FEEDBACK)) {
            mCheatAKeyService.getVibratorService().vibrate(
                    VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        if ("SHI".equals(data)) {
            mState = mState ^ STATE_SHIFT;
            mapKeys();
            return;
        } else if ("SYM".equals(data)) {
            mState = (mState ^ STATE_SYMBOL) & ~STATE_SHIFT;
            mapKeys();
            return;
        }
        data = getTextFromFuncKey(data);
        if (data.isEmpty()) return;

        InputConnection inputConnection = mCheatAKeyService.getInputConnection();
        if ("DEL".equals(data)) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        } else if ("ENT".equals(data)) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        } else if ("SPA".equals(data)) {
            if (!mCheatAKeyService.checkDoubleSpaceToPeriod()) {
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
            }
        } else {
            inputConnection.commitText(data, 1);
        }
    }

    void reset() {
        mapKeys();
        mState = 0;
    }

    void initializeDataBaseHelper() {
        mDataBaseHelper = mCheatAKeyService.getDataBaseHelper();
    }

    private String getTextFromFuncKey(String data) {
        if ("VOWEL".equals(data)) {
            return "";
        } else {
            return data;
        }
    }

    private int dpToPx(float dp) {
        float density = mCheatAKeyService.getResources().getDisplayMetrics().density;
        return (int) Math.round(dp * density + 0.5);
    }

    private void initializeGestureDirectionUsedFlag() {
        for (GESTURE_DIRECTION direction : GESTURE_DIRECTION.values()) {
            mGestureDirectionUsedFlag[direction.toInt()] = 0;
        }
    }

    private void updateGestureDirectionUsedFlag(GESTURE_DIRECTION direction, int flag) {
        initializeGestureDirectionUsedFlag();
        mGestureDirectionUsedFlag[direction.toInt()] = flag;
    }

    private boolean getGestureDirectionUsedFlag(GESTURE_DIRECTION direction) {
        return mGestureDirectionUsedFlag[direction.toInt()] == 1;
    }

    private void initializeDirectionCharacterMap() {
        mDirectionCharacterMap[GESTURE_DIRECTION.GUIDE_A.toInt()] = "a";
        mDirectionCharacterMap[GESTURE_DIRECTION.GUIDE_E.toInt()] = "e";
        mDirectionCharacterMap[GESTURE_DIRECTION.GUIDE_I.toInt()] = "i";
        mDirectionCharacterMap[GESTURE_DIRECTION.GUIDE_O.toInt()] = "o";
        mDirectionCharacterMap[GESTURE_DIRECTION.GUIDE_U.toInt()] = "u";
        mDirectionCharacterMap[GESTURE_DIRECTION.GUIDE_NONE.toInt()] = "";
    }

    private String getDirectionCharacter(GESTURE_DIRECTION direction) {
        return mDirectionCharacterMap[direction.toInt()];
    }

    private GESTURE_DIRECTION getGestureLocation(TextView softkey, float touchX, float touchY) {
        int[] softkeyLocation = new int[2];
        int[] guideViewALocation = new int[2];
        int[] guideViewELocation = new int[2];
        int[] guideViewILocation = new int[2];
        int[] guideViewOLocation = new int[2];
        int[] guideViewULocation = new int[2];
        softkey.getLocationInWindow(softkeyLocation);
        // convert coord to window coord
        touchX = softkeyLocation[0] + touchX;
        touchY = softkeyLocation[1] + touchY;

        getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -15, guideViewALocation);
        getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -65, guideViewELocation);
        getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -115, guideViewILocation);
        getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -165, guideViewOLocation);
        getPointFromAngle(softkeyLocation[0], softkeyLocation[1],
                dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_DISTANCE), -215, guideViewULocation);

        if (touchX >= guideViewALocation[0] && touchY >= guideViewALocation[1] &&
                touchX <= guideViewALocation[0] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE) &&
                touchY <= guideViewALocation[1] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE)) {
            return GESTURE_DIRECTION.GUIDE_A;
        } else if (touchX >= guideViewELocation[0] && touchY >= guideViewELocation[1] &&
                touchX <= guideViewELocation[0] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE) &&
                touchY <= guideViewELocation[1] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE)) {
            return GESTURE_DIRECTION.GUIDE_E;
        } else if (touchX >= guideViewILocation[0] && touchY >= guideViewILocation[1] &&
                touchX <= guideViewILocation[0] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE) &&
                touchY <= guideViewILocation[1] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE)) {
            return GESTURE_DIRECTION.GUIDE_I;
        } else if (touchX >= guideViewOLocation[0] && touchY >= guideViewOLocation[1] &&
                touchX <= guideViewOLocation[0] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE) &&
                touchY <= guideViewOLocation[1] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE)) {
            return GESTURE_DIRECTION.GUIDE_O;
        } else if (touchX >= guideViewULocation[0] && touchY >= guideViewULocation[1] &&
                touchX <= guideViewULocation[0] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE) &&
                touchY <= guideViewULocation[1] + dpToPx(mCustomVariables.GESTURE_GUIDE_VIEW_SIZE)) {
            return GESTURE_DIRECTION.GUIDE_U;
        }
        return GESTURE_DIRECTION.GUIDE_NONE;
    }

    private void getPointFromAngle(int baseX, int baseY, int r, int angle, int[] ret) {
        ret[0] = baseX + (int) Math.round(r * Math.cos(degreeToRadian(angle)));
        ret[1] = baseY + (int) Math.round(r * Math.sin(degreeToRadian(angle)));
    }

    private double degreeToRadian(int degree) {
        return (Math.PI/180)*degree;
    }
}
