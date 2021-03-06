package com.clab.cheaboard;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.VibrationEffect;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/** Controls the visible virtual keyboard view. */
final class KeyboardKorean extends Keyboard{

    private char mChosung = '\u0000';
    private char mJungsung = '\u0000';
    private char mJongsung = '\u0000';
    private char mJongsungFlag = '\u0000';
    private char mDoubleJongsungFlag = '\u0000';
    private char mJungsungFlag = '\u0000';
    private final Integer[] mChosungArray = {0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};
    private final Integer[] mJungsungArray = {0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163};
    private final Integer[] mJongsungArray = {0x0000, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};
    ArrayList<Integer> mChosungList = new ArrayList<>(Arrays.asList(mChosungArray));
    ArrayList<Integer> mJungsungList = new ArrayList<>(Arrays.asList(mJungsungArray));
    ArrayList<Integer> mJongSungList = new ArrayList<>(Arrays.asList(mJongsungArray));
    private int mCommitState = 0;

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
        keyMapping.put(R.id.key_pos_0_0, "??????+`");
        keyMapping.put(R.id.key_pos_0_1, "???????????");
        keyMapping.put(R.id.key_pos_0_2, "??????+\\");
        keyMapping.put(R.id.key_pos_0_3, "??????=|");
        keyMapping.put(R.id.key_pos_0_4, "??????/???");
        keyMapping.put(R.id.key_pos_0_5, "??????_???");
        keyMapping.put(R.id.key_pos_0_6, "??????<{");
        keyMapping.put(R.id.key_pos_0_7, "??????>}");
        keyMapping.put(R.id.key_pos_0_8, "?????????[");
        keyMapping.put(R.id.key_pos_0_9, "?????????]");
        keyMapping.put(R.id.key_pos_1_0, "??????!???");
        keyMapping.put(R.id.key_pos_1_1, "??????@???");
        keyMapping.put(R.id.key_pos_1_2, "??????#???");
        keyMapping.put(R.id.key_pos_1_3, "??????~???");
        keyMapping.put(R.id.key_pos_1_4, "??????%???");
        keyMapping.put(R.id.key_pos_1_5, "??????^???");
        keyMapping.put(R.id.key_pos_1_6, "??????&$");
        keyMapping.put(R.id.key_pos_1_7, "??????(???");
        keyMapping.put(R.id.key_pos_1_8, "??????)??");
        keyMapping.put(R.id.key_pos_2_0, "??????-???");
        keyMapping.put(R.id.key_pos_2_1, "??????'???");
        keyMapping.put(R.id.key_pos_2_2, "??????\"???");
        keyMapping.put(R.id.key_pos_2_3, "??????:???");
        keyMapping.put(R.id.key_pos_2_4, "??????;???");
        keyMapping.put(R.id.key_pos_2_5, "??????,??");
        keyMapping.put(R.id.key_pos_2_6, "?????????");
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
    boolean onTextViewTouch(View view, MotionEvent evt, int index, String data) {
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
                terminateTimer();
        }
        return true;
    }

    @Override
    boolean onImageViewTouch(View view, MotionEvent evt, int index, String data) {
        int action = evt.getActionMasked();
        ImageView imagekey = (ImageView) view;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                handleInputEvent(data);
                createTimer(data);
                setKeyPressColor(imagekey);
                break;
            case MotionEvent.ACTION_UP:
                terminateTimer();
                resetKeyColor(imagekey);
                break;
            default:
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    void mapKeys() {
        for (int i = 0; i < mKeyMapping.size(); i++) {
            String value = mKeyMapping.valueAt(i);
            if (mImageViewList.contains(value)) {
                ImageView imagekey = mKeyboardView.findViewById(mKeyMapping.keyAt(i));
                if (imagekey != null) {
                    String rawData = mKeyMapping.valueAt(i);
                    String data = rawData.length() != Utils.STATE_NUMBER ? rawData : rawData.substring(mState, mState + 1);
                    final int index = i;
                    imagekey.setOnTouchListener((view, evt) -> onImageViewTouch(view, evt, index, data));
                }
            } else {
                TextView softkey = mKeyboardView.findViewById(mKeyMapping.keyAt(i));
                if (softkey != null) {
                    String rawData = mKeyMapping.valueAt(i);
                    String data = rawData.length() != Utils.STATE_NUMBER ? rawData : rawData.substring(mState, mState + 1);
                    softkey.setText(getLabelFromRawString(data));
                    final int index = i;
                    softkey.setOnTouchListener((view, evt) -> onTextViewTouch(view, evt, index, data));
                }
            }
        }
    }

    void handleInputEvent(String data) {
        if (mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_VIBRATION_FEEDBACK)) {
            mCheaBoardService.getVibratorService().vibrate(
                    VibrationEffect.createOneShot(Utils.VIBRATION_DURATION, Utils.VIBRATION_AMPLITUDE));
        }

        InputConnection inputConnection = mCheaBoardService.getInputConnection();
        switch (data) {
            case "DEL":
                delete();
                break;
            case "ENT":
                directlyCommit();
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case "SPA":
                directlyCommit();
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
                break;
            case "SET":
                directlyCommit();
                mCheaBoardService.startSettingsActivity();
                break;
            case "SHI":
                mState = mState ^ Utils.STATE_SHIFT;
                mapKeys();
                break;
            case "SYM":
                directlyCommit();
                mState = (mState ^ Utils.STATE_SYMBOL) & ~Utils.STATE_SHIFT;
                mapKeys();
                break;
            case "LNG":
                directlyCommit();
                mCheaBoardService.changeKeyboardType(Utils.KEYBOARD_TYPE_ENGLISH);
                break;
            default:
                char c = data.charAt(0);
                commit(c);
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
                return "???";
            case "DEL":
                return "???";
            case "SYM":
                if (mState == Utils.STATE_SYMBOL || mState == Utils.STATE_SYMBOL + Utils.STATE_SHIFT) {
                    return "???";
                }
                return "!#1";
            case "SPA":
                return "SPACE";
            case "ENT":
                return "???";
            case "SET":
                return "???";
            case "LNG":
                return "???/???";
            default:
                return data;
        }
    }

    private void initializeCommitData() {
        mChosung = mJungsung = mJongsung = mJongsungFlag
                = mDoubleJongsungFlag = mJungsungFlag = '\u0000';
    }

    private char makeHan() {
        if (mCommitState == 0) {
            return '\u0000';
        } else if (mCommitState == 1) {
            return mChosung;
        }
        int chosungIndex = mChosungList.indexOf(Utils.charToInt(mChosung));
        int jungsungIndex = mJungsungList.indexOf(Utils.charToInt(mJungsung));
        int jongsungIndex = mJongSungList.indexOf(Utils.charToInt(mJongsung));

        int makeResult = 0xAC00 + (28 * 21 * (chosungIndex)) + (28 * (jungsungIndex)) + jongsungIndex;
        return Utils.intToChar(makeResult);
    }

    private void commit(char c) {
        InputConnection inputConnection = mCheaBoardService.getInputConnection();
        if (!mChosungList.contains(Utils.charToInt(c)) &&
                !mJungsungList.contains(Utils.charToInt(c)) &&
                !mJongSungList.contains(Utils.charToInt(c))) {
            directlyCommit();
            inputConnection.commitText(Character.toString(c), 1);
            return;
        }
        switch (mCommitState) {
            case 0:
                if (mJungsungList.contains(Utils.charToInt(c))) {
                    inputConnection.commitText(Character.toString(c), 1);
                    initializeCommitData();
                } else {
                    mCommitState = 1;
                    mChosung = c;
                    inputConnection.setComposingText(Character.toString(c), 1);
                }
                break;
            case 1:
                if (mChosungList.contains(Utils.charToInt(c))) {
                    inputConnection.commitText(Character.toString(mChosung), 1);
                    initializeCommitData();
                    mChosung = c;
                    inputConnection.setComposingText(Character.toString(c), 1);
                } else {
                    mCommitState = 2;
                    mJungsung = c;
                    inputConnection.setComposingText(Character.toString(makeHan()), 1);
                }
                break;
            case 2:
                if (mJungsungList.contains(Utils.charToInt(c))) {
                    if (isDoubleJungsungEnable(c)) {
                        inputConnection.setComposingText(Character.toString(makeHan()), 1);
                    } else {
                        inputConnection.commitText(Character.toString(makeHan()), 1);
                        inputConnection.commitText(Character.toString(c), 1);
                        initializeCommitData();
                        mCommitState = 0;
                    }
                } else if (mJongSungList.contains(Utils.charToInt(c))) {
                    // ????????? ????????? ??????
                    mJongsung = c;
                    inputConnection.setComposingText(Character.toString(makeHan()), 1);
                    mCommitState = 3;
                } else {
                    directlyCommit();
                    mChosung = c;
                    mCommitState = 1;
                    inputConnection.setComposingText(Character.toString(makeHan()), 1);
                }
                break;
            case 3:
                if (mJongSungList.contains(Utils.charToInt(c))) {
                    if (isDoubleJongsungEnable(c)) {
                        inputConnection.setComposingText(Character.toString(makeHan()), 1);
                    } else {
                        inputConnection.commitText(Character.toString(makeHan()), 1);
                        initializeCommitData();
                        mCommitState = 1;
                        mChosung = c;
                        inputConnection.setComposingText(Character.toString(c), 1);
                    }
                } else if (mChosungList.contains(Utils.charToInt(c))) {
                    inputConnection.commitText(Character.toString(makeHan()), 1);
                    mCommitState = 1;
                    initializeCommitData();
                    mChosung = c;
                    inputConnection.setComposingText(Character.toString(c), 1);
                } else {
                    // ????????? ????????? ??????
                    char temp = '\u0000';
                    if (mDoubleJongsungFlag == '\u0000') {
                        temp = mJongsung;
                        mJongsung = '\u0000';
                        inputConnection.commitText(Character.toString(makeHan()), 1);
                    } else {
                        temp = mDoubleJongsungFlag;
                        mJongsung = mJongsungFlag;
                        inputConnection.commitText(Character.toString(makeHan()), 1);
                    }
                    mCommitState = 2;
                    initializeCommitData();
                    mChosung = temp;
                    mJungsung = c;
                    inputConnection.setComposingText(Character.toString(makeHan()), 1);
                }
                break;
        }
    }

    private void directlyCommit() {
        if (mCommitState == 0) {
            return;
        }
        mCheaBoardService.getInputConnection().commitText(Character.toString(makeHan()), 1);
        mCommitState = 0;
        initializeCommitData();
    }

    private boolean isDoubleJungsungEnable(char c) {
        if (mJungsung == '???') {
            if (c == '???') {
                mJungsungFlag = mJungsung;
                mJungsung = '???';
                return true;
            } else if (c == '???') {
                mJungsungFlag = mJungsung;
                mJungsung = '???';
                return true;
            } else if (c == '???') {
                mJungsungFlag = mJungsung;
                mJungsung = '???';
                return true;
            }
        } else if (mJungsung == '???') {
            if (c == '???') {
                mJungsungFlag = mJungsung;
                mJungsung = '???';
                return true;
            } else if (c == '???') {
                mJungsungFlag = mJungsung;
                mJungsung = '???';
                return true;
            } else if (c == '???') {
                mJungsungFlag = mJungsung;
                mJungsung = '???';
                return true;
            }
        } else if (mJungsung == '???') {
            if (c == '???') {
                mJungsungFlag = mJungsung;
                mJungsung = '???';
                return true;
            }
        }
        return false;
    }

    private boolean isDoubleJongsungEnable(char c) {
        mJongsungFlag = mJongsung;
        mDoubleJongsungFlag = c;
        if (mJongsung == '???') {
            if (c == '???') {
                mJongsung = '???';
                return true;
            }
        } else if (mJongsung == '???') {
            if (c == '???') {
                mJongsung = '???';
                return true;
            } else if (c == '???') {
                mJongsung = '???';
                return true;
            }
        } else if (mJongsung == '???') {
            if (c == '???') {
                mJongsung = '???';
                return true;
            } else if (c == '???') {
                mJongsung = '???';
                return true;
            } else if (c == '???') {
                mJongsung = '???';
                return true;
            } else if (c == '???') {
                mJongsung = '???';
                return true;
            } else if (c == '???') {
                mJongsung = '???';
                return true;
            } else if (c == '???') {
                mJongsung = '???';
                return true;
            } else if (c == '???') {
                mJongsung = '???';
                return true;
            }
        } else if (mJongsung == '???') {
            if (c == '???') {
                mJongsung = '???';
                return true;
            }
        }
        return false;
    }

    private void delete() {
        InputConnection inputConnection = mCheaBoardService.getInputConnection();
        switch (mCommitState) {
            case 0:
                inputConnection.deleteSurroundingText(1, 0);
                break;
            case 1:
                mChosung = '\u0000';
                mCommitState = 0;
                inputConnection.setComposingText("", 1);
                inputConnection.commitText("", 1);
                break;
            case 2:
                if (mJungsungFlag != '\u0000') {
                    mJungsung = mJungsungFlag;
                    mJungsungFlag = '\u0000';
                    mCommitState = 2;
                    inputConnection.setComposingText(Character.toString(makeHan()), 1);
                } else {
                    mJungsung = mJungsungFlag = '\u0000';
                    mCommitState = 1;
                    inputConnection.setComposingText(Character.toString(mChosung), 1);
                }
                break;
            case 3:
                if (mDoubleJongsungFlag == '\u0000') {
                    mJongsung = '\u0000';
                    mCommitState = 2;
                } else {
                    mJongsung = mJongsungFlag;
                    mJongsungFlag = mDoubleJongsungFlag = '\u0000';
                    mCommitState = 3;
                }
                inputConnection.setComposingText(Character.toString(makeHan()), 1);
                break;
        }
    }
}
