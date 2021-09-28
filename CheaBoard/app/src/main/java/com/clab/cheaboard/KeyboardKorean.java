package com.clab.cheaboard;

import android.annotation.SuppressLint;
import android.os.VibrationEffect;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
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
                    // 종성이 들어온 경우
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
                    // 중성이 들어올 경우
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
        if (mJungsung == 'ㅗ') {
            if (c == 'ㅏ') {
                mJungsungFlag = mJungsung;
                mJungsung = 'ㅘ';
                return true;
            } else if (c == 'ㅐ') {
                mJungsungFlag = mJungsung;
                mJungsung = 'ㅙ';
                return true;
            } else if (c == 'ㅣ') {
                mJungsungFlag = mJungsung;
                mJungsung = 'ㅚ';
                return true;
            }
        } else if (mJungsung == 'ㅜ') {
            if (c == 'ㅓ') {
                mJungsungFlag = mJungsung;
                mJungsung = 'ㅝ';
                return true;
            } else if (c == 'ㅔ') {
                mJungsungFlag = mJungsung;
                mJungsung = 'ㅞ';
                return true;
            } else if (c == 'ㅣ') {
                mJungsungFlag = mJungsung;
                mJungsung = 'ㅟ';
                return true;
            }
        } else if (mJungsung == 'ㅡ') {
            if (c == 'ㅣ') {
                mJungsungFlag = mJungsung;
                mJungsung = 'ㅢ';
                return true;
            }
        }
        return false;
    }

    private boolean isDoubleJongsungEnable(char c) {
        mJongsungFlag = mJongsung;
        mDoubleJongsungFlag = c;
        if (mJongsung == 'ㄱ') {
            if (c == 'ㅅ') {
                mJongsung = 'ㄳ';
                return true;
            }
        } else if (mJongsung == 'ㄴ') {
            if (c == 'ㅈ') {
                mJongsung = 'ㄵ';
                return true;
            } else if (c == 'ㅎ') {
                mJongsung = 'ㄶ';
                return true;
            }
        } else if (mJongsung == 'ㄹ') {
            if (c == 'ㄱ') {
                mJongsung = 'ㄺ';
                return true;
            } else if (c == 'ㅁ') {
                mJongsung = 'ㄻ';
                return true;
            } else if (c == 'ㅂ') {
                mJongsung = 'ㄼ';
                return true;
            } else if (c == 'ㅅ') {
                mJongsung = 'ㄽ';
                return true;
            } else if (c == 'ㅌ') {
                mJongsung = 'ㄾ';
                return true;
            } else if (c == 'ㅍ') {
                mJongsung = 'ㄿ';
                return true;
            } else if (c == 'ㅎ') {
                mJongsung = 'ㅀ';
                return true;
            }
        } else if (mJongsung == 'ㅂ') {
            if (c == 'ㅅ') {
                mJongsung = 'ㅄ';
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
