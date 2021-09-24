package com.clab.cheaboard;

import android.annotation.SuppressLint;
import android.os.VibrationEffect;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;

/** Controls the visible virtual keyboard view. */
final class KeyboardKorean extends Keyboard{

    private KeyboardKorean(CheaBoardService cheaBoardService, int viewResId,
                     SparseArray<String> keyMapping) {
        super(cheaBoardService, viewResId, keyMapping);

        initializeDataBaseHelper();
    }

    static KeyboardKorean createQwertyKeyboard(CheaBoardService cheaBoardService) {
        SparseArray<String> keyMapping = new SparseArray<>();
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
        keyMapping.put(R.id.key_pos_0_0, "ㅂㅃ+`");
        keyMapping.put(R.id.key_pos_0_1, "ㅈㅉ×₩");
        keyMapping.put(R.id.key_pos_0_2, "ㄷㄸ+\\");
        keyMapping.put(R.id.key_pos_0_3, "ㄱㄲ=|");
        keyMapping.put(R.id.key_pos_0_4, "ㅅㅆ/℃");
        keyMapping.put(R.id.key_pos_0_5, "ㅛㅛ_℉");
        keyMapping.put(R.id.key_pos_0_6, "ㅕㅕ<{");
        keyMapping.put(R.id.key_pos_0_7, "ㅑㅑ>}");
        keyMapping.put(R.id.key_pos_0_8, "ㅐㅒ♡[");
        keyMapping.put(R.id.key_pos_0_9, "ㅔㅖ☆]");
        keyMapping.put(R.id.key_pos_1_0, "ㅁㅁ!•");
        keyMapping.put(R.id.key_pos_1_1, "ㄴㄴ@○");
        keyMapping.put(R.id.key_pos_1_2, "ㅇㅇ#●");
        keyMapping.put(R.id.key_pos_1_3, "ㄹㄹ~□");
        keyMapping.put(R.id.key_pos_1_4, "ㅎㅎ%■");
        keyMapping.put(R.id.key_pos_1_5, "ㅗㅗ^◇");
        keyMapping.put(R.id.key_pos_1_6, "ㅓㅓ&$");
        keyMapping.put(R.id.key_pos_1_7, "ㅏㅏ(₤");
        keyMapping.put(R.id.key_pos_1_8, "ㅣㅣ)¥");
        keyMapping.put(R.id.key_pos_2_0, "ㅋㅋ-◦");
        keyMapping.put(R.id.key_pos_2_1, "ㅌㅌ'※");
        keyMapping.put(R.id.key_pos_2_2, "ㅊㅊ\"∞");
        keyMapping.put(R.id.key_pos_2_3, "ㅍㅍ:≪");
        keyMapping.put(R.id.key_pos_2_4, "ㅠㅠ;≫");
        keyMapping.put(R.id.key_pos_2_5, "ㅜㅜ,¡");
        keyMapping.put(R.id.key_pos_2_6, "ㅡㅡ?¿");
        keyMapping.put(R.id.key_pos_bottom_0, ",");
        keyMapping.put(R.id.key_pos_bottom_1, ".");
        keyMapping.put(R.id.key_pos_shift, "SHI");
        keyMapping.put(R.id.key_pos_del, "DEL");
        keyMapping.put(R.id.key_pos_symbol, "SYM");
        keyMapping.put(R.id.key_pos_space, "SPA");
        keyMapping.put(R.id.key_pos_enter, "ENT");
        keyMapping.put(R.id.key_pos_settings, "SET");
        keyMapping.put(R.id.key_pos_change_lang, "LNG");
        return new KeyboardKorean(cheaBoardService, R.layout.keyboard_korean, keyMapping);
    }

    @Override
    boolean onSoftkeyTouch(View view, MotionEvent evt, int index, String data) {
        int action = evt.getActionMasked();
        TextView softkey = (TextView) view;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                handleInputEvent(data);
                createTimer(data);
                setKeyPressColor(softkey);
                break;
            case MotionEvent.ACTION_UP:
                terminateTimer();
                resetKeyColor(softkey);
                break;
            default:
                // do nothing
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    void mapKeys() {
        for (int i = 0; i < mKeyMapping.size(); i++) {
            TextView softkey = mKeyboardView.findViewById(mKeyMapping.keyAt(i));
            if (softkey != null) {
                String rawData = mKeyMapping.valueAt(i);
                String data = rawData.length() != Utils.STATE_NUMBER ? rawData : rawData.substring(mState, mState + 1);
                softkey.setText(getLabelFromRawString(data));
                final int index = i;
                softkey.setOnTouchListener((view, evt) -> onSoftkeyTouch(view, evt, index, data));
            }
        }
    }

    void handleInputEvent(String data) {
        if (mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_VIBRATION_FEEDBACK)) {
            mCheaBoardService.getVibratorService().vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        InputConnection inputConnection = mCheaBoardService.getInputConnection();
        switch (data) {
            case "DEL":
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                if (InputWordController.get().getLength() > 0) {
                    InputWordController.get().deleteLastWord();
                }
                break;
            case "ENT":
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case "SPA":
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
                InputWordController.get().appendWord(' ');
                break;
            case "SET":
                mCheaBoardService.startSettingsActivity();
                break;
            case "SHI":
                mState = mState ^ Utils.STATE_SHIFT;
                mapKeys();
                break;
            case "SYM":
                mState = (mState ^ Utils.STATE_SYMBOL) & ~Utils.STATE_SHIFT;
                mapKeys();
                break;
            case "LNG":
                mCheaBoardService.changeKeyboardType(Utils.KEYBOARD_TYPE_KOREAN);
                break;
            default:
                char c = data.charAt(0);
                inputConnection.commitText(data, 1);
                InputWordController.get().appendWord(c);
        }
    }

    String getLabelFromRawString(String data) {
        switch (data) {
            case "SHI":
                if (mState == Utils.STATE_SYMBOL) {
                    return "1/2";
                } else if (mState == Utils.STATE_SYMBOL + Utils.STATE_SHIFT) {
                    return "2/2";
                }
                return "↑";
            case "DEL":
                return "←";
            case "SYM":
                if (mState == Utils.STATE_SYMBOL || mState == Utils.STATE_SYMBOL + Utils.STATE_SHIFT) {
                    return "가";
                }
                return "!#1";
            case "SPA":
                return "SPACE";
            case "ENT":
                return "↲";
            case "SET":
                return "≡";
            case "LNG":
                return "한/영";
            default:
                return data;
        }
    }

    private boolean isGestureInsideOfKey(TextView softkey, float currentX, float currentY) {
        int[] location = new int[2];
        softkey.getLocationInWindow(location);
        float x1 = location[0];
        float y1 = location[1];
        float x2 = x1 + softkey.getWidth();
        float y2 = y1 + softkey.getHeight();
        float gestureX = currentX + x1;
        float gestureY = currentY + y1;
        if (gestureX >= x1 && gestureY >= y1 && gestureX <= x2 && gestureY <= y2) {
            return true;
        }
        return false;
    }
}
