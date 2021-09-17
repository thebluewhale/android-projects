package com.clab.cheaboard;

import android.os.VibrationEffect;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/** Controls the visible virtual keyboard view. */
final class Keyboard {

    private final CheaBoardService mCheaBoardService;
    private final int mViewResId;
    private final SparseArray<String> mKeyMapping;
    private View mKeyboardView;
    private LayoutInflater mLayoutInflater;
    private final PopupWindow mGestureGuideViewContainer = new PopupWindow();
    private DataBaseHelper mDataBaseHelper;
    private int mState;
    private final int[] keyIdArr = new int[Utils.ALPHABET_SIZE];
    private float mGestureBaseX, mGestureBaseY;
    private final Queue<Float> mGestureXQueue = new LinkedList<>();
    private final Queue<Float> mGestureYQueue = new LinkedList<>();
    private final int[] mGestureDirectionUsedFlag = new int[10];
    private Timer mLongPressTimer;
    private int mLongPressTimerFlag;
    private final Trie mTrie;

    private Keyboard(CheaBoardService cheaBoardService, int viewResId,
                     SparseArray<String> keyMapping) {
        mCheaBoardService = cheaBoardService;
        mViewResId = viewResId;
        mKeyMapping = keyMapping;
        mState = 0;
        mTrie = new Trie(cheaBoardService);

        initializeDataBaseHelper();
        initializeGestureDirectionUsedFlag();
        createKeyIdArray();
    }

    private String getLabelFromRawString(String data) {
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
                    return "abc";
                }
                return "!#1";
            case "SPA":
                return "SPACE";
            case "ENT":
                return "↲";
            case "SET":
                return "≡";
            default:
                return data;
        }
    }

    static Keyboard createQwertyKeyboard(CheaBoardService cheaBoardService) {
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
        keyMapping.put(R.id.key_pos_0_0, "qQ+`");
        keyMapping.put(R.id.key_pos_0_1, "wW×₩");
        keyMapping.put(R.id.key_pos_0_2, "eE+\\");
        keyMapping.put(R.id.key_pos_0_3, "rR=|");
        keyMapping.put(R.id.key_pos_0_4, "tT/℃");
        keyMapping.put(R.id.key_pos_0_5, "yY_℉");
        keyMapping.put(R.id.key_pos_0_6, "uU<{");
        keyMapping.put(R.id.key_pos_0_7, "iI>}");
        keyMapping.put(R.id.key_pos_0_8, "oO♡[");
        keyMapping.put(R.id.key_pos_0_9, "pP☆]");
        keyMapping.put(R.id.key_pos_1_0, "aA!•");
        keyMapping.put(R.id.key_pos_1_1, "sS@○");
        keyMapping.put(R.id.key_pos_1_2, "dD#●");
        keyMapping.put(R.id.key_pos_1_3, "fF~□");
        keyMapping.put(R.id.key_pos_1_4, "gG%■");
        keyMapping.put(R.id.key_pos_1_5, "hH^◇");
        keyMapping.put(R.id.key_pos_1_6, "jJ&$");
        keyMapping.put(R.id.key_pos_1_7, "kK(₤");
        keyMapping.put(R.id.key_pos_1_8, "lL)¥");
        keyMapping.put(R.id.key_pos_2_0, "zZ-◦");
        keyMapping.put(R.id.key_pos_2_1, "xX'※");
        keyMapping.put(R.id.key_pos_2_2, "cC\"∞");
        keyMapping.put(R.id.key_pos_2_3, "vV:≪");
        keyMapping.put(R.id.key_pos_2_4, "bB;≫");
        keyMapping.put(R.id.key_pos_2_5, "nN,¡");
        keyMapping.put(R.id.key_pos_2_6, "mM?¿");
        keyMapping.put(R.id.key_pos_bottom_0, ",");
        keyMapping.put(R.id.key_pos_bottom_1, ".");
        keyMapping.put(R.id.key_pos_shift, "SHI");
        keyMapping.put(R.id.key_pos_del, "DEL");
        keyMapping.put(R.id.key_pos_symbol, "SYM");
        keyMapping.put(R.id.key_pos_space, "SPA");
        keyMapping.put(R.id.key_pos_enter, "ENT");
        keyMapping.put(R.id.key_pos_settings, "SET");
        return new Keyboard(cheaBoardService, R.layout.keyboard_10_9_9, keyMapping);
    }

    public View inflateKeyboardView(LayoutInflater inflater, InputView inputView) {
        mLayoutInflater = inflater;
        mKeyboardView = mLayoutInflater.inflate(mViewResId, inputView, false);
        mapKeys();
        return mKeyboardView;
    }

    private boolean onSoftkeyTouch(View view, MotionEvent evt, int index, String data) {
        int action = evt.getActionMasked();
        TextView softkey = (TextView) view;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                showGestureGuideIfNeeded(softkey, data);
                initializeAllGestureDatas(evt.getX(), evt.getY());
                handleInputEvent(data);
                createTimer(data);
                setKeyPressColor(softkey);
                break;
            case MotionEvent.ACTION_UP:
                hideGestureGuide();
                terminateTimer();
                enlargeKeysIfNeeded();
                resetKeyColor(softkey);
                break;
            case MotionEvent.ACTION_MOVE:
                if (Utils.isFunctionKey(data)) {
                    return true;
                }
                float mGestureCurrentX = evt.getX();
                float mGestureCurrentY = evt.getY();

                if (isGestureInsideOfKey(softkey, mGestureCurrentX, mGestureCurrentY)) {
                    initializeAllGestureDatas(mGestureCurrentX, mGestureCurrentY);
                    return true;
                }
                if (getGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK)) {
                    return true;
                }

                terminateTimer();
                addGestureEventIntoQueue(mGestureCurrentX, mGestureCurrentY);

                float distX = mGestureCurrentX - mGestureBaseX;
                float distY = mGestureCurrentY - mGestureBaseY;
                float angle = (float) Math.toDegrees(Math.atan2(distY, distX));

                if (Math.abs(distX) < dpToPx(30) && Math.abs(distY) < dpToPx(30)) {
                    return true;
                }

                if (angle < -67.5 && angle > -112.5) {
                    // Up
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.UP)) {
                        handleInputEvent(mState == Utils.STATE_SHIFT ? "I" : "i");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.UP, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= -67.5 && angle <= -22.5) {
                    // RightUp
                    updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                } else if (angle > -22.5 && angle < 22.5) {
                    // Right
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT)) {
                        handleInputEvent(mState == Utils.STATE_SHIFT ? "E" :"e");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= 22.5 && angle <= 67.5) {
                    // RightDown
                    updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                } else if (angle > 67.5 && angle < 112.5) {
                    // Down
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN)) {
                        handleInputEvent(mState == Utils.STATE_SHIFT ? "U" : "u");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= 112.5 && angle < 157.5) {
                    // LeftDown
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTDOWN)) {
                        handleInputEvent(mState == Utils.STATE_SHIFT ? "O" : "o");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTDOWN, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= 157.5 && angle <= 180 || angle <= -157.5 && angle >= -180) {
                    // Left
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFT)) {
                        handleInputEvent(mState == Utils.STATE_SHIFT ? "A" : "a");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFT, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= -157.5 && angle <= -112.5) {
                    //  LeftUp
                    updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                }
                break;
            default:
                // do nothing
        }
        return true;
    }

    private void mapKeys() {
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

    private void showGestureGuideIfNeeded(TextView softkey, String data) {
        float softkeyWidth = softkey.getWidth();
        float gestureGuideViewWidth = dpToPx(101);
        int []outLocation = new int[2];
        softkey.getLocationInWindow(outLocation);
        float locationX = outLocation[0] - ((gestureGuideViewWidth - softkeyWidth) / 2);
        float locationY = outLocation[1] - dpToPx(101);
        if (!mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_SWIPE_POPUP)) {
            return;
        }
        if (Utils.isFunctionKey(data) ||
                Utils.isCharacterOrNumber(data.charAt(0)) == CHARACTER_TYPE.SYMBOL ||
                Utils.isCharacterOrNumber(data.charAt(0)) == CHARACTER_TYPE.NUMBER) {
            return;
        }

        View mGestureGuideView = mLayoutInflater.inflate(R.layout.gesture_guide, null);

        ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_0_1)).setText(mState == Utils.STATE_SHIFT ? "I" : "i");
        ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_1_0)).setText(mState == Utils.STATE_SHIFT ? "A" : "a");
        ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_1_2)).setText(mState == Utils.STATE_SHIFT ? "E" : "e");
        ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_2_0)).setText(mState == Utils.STATE_SHIFT ? "O" : "o");
        ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_2_1)).setText(mState == Utils.STATE_SHIFT ? "U" : "u");

        if (mGestureGuideView.getParent() != null) {
            ((ViewGroup) mGestureGuideView.getParent()).removeView(mGestureGuideView);
        }
        mGestureGuideViewContainer.setContentView(mGestureGuideView);
        mGestureGuideViewContainer.setWidth(dpToPx(101));
        mGestureGuideViewContainer.setHeight(dpToPx(101));
        mGestureGuideViewContainer.showAtLocation(softkey, 0, Math.round(locationX), Math.round(locationY));
    }

    private void hideGestureGuide() {
        mGestureGuideViewContainer.dismiss();
    }

    private void handleInputEvent(String data) {
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
            default:
                char c = data.charAt(0);
                inputConnection.commitText(data, 1);
                InputWordController.get().appendWord(c);
        }
    }

    public void reDrawKeyboard() {
        mapKeys();
        mState = 0;
        checkPreInputWord();
    }

    public void resetKeyLayout() {
        int MED_VAL = 35;
        for (int i = 0; i < Utils.ALPHABET_SIZE; i++) {
            TextView softkey = mKeyboardView.findViewById(keyIdArr[i]);
            LinearLayout.LayoutParams lparam = (LinearLayout.LayoutParams)softkey.getLayoutParams();
            lparam.weight = MED_VAL;
            softkey.setLayoutParams(lparam);
            resetKeyColor(softkey);
        }
    }

    private void checkPreInputWord() {
        InputWordController.get().resetWord(
                mCheaBoardService.getInputConnection().getTextBeforeCursor(100, 0).toString());
    }

    private void initializeDataBaseHelper() {
        mDataBaseHelper = mCheaBoardService.getDataBaseHelper();
    }

    boolean useDoubleSpaceToPeriod() {
        return mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_AUTO_PERIOD);
    }

    private int dpToPx(float dp) {
        float density = mCheaBoardService.getResources().getDisplayMetrics().density;
        return (int) Math.round(dp * density + 0.5);
    }

    private void initializeGestureEventQueue(float initX, float initY) {
        mGestureXQueue.clear();
        mGestureYQueue.clear();
        mGestureXQueue.add(initX);
        mGestureYQueue.add(initY);
    }

    private void addGestureEventIntoQueue(float x, float y) {
        if (mGestureXQueue.size() < Utils.GESTURE_QUEUE_SIZE) {
            mGestureXQueue.add(x);
        } else {
            mGestureBaseX = mGestureXQueue.poll();
            mGestureXQueue.add(x);
        }
        if (mGestureYQueue.size() < Utils.GESTURE_QUEUE_SIZE) {
            mGestureYQueue.add(y);
        } else {
            mGestureBaseY = mGestureYQueue.poll();
            mGestureYQueue.add(y);
        }
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

    private void initializeAllGestureDatas(float x, float y) {
        initializeGestureDirectionUsedFlag();
        initializeGestureEventQueue(x, y);
        mGestureBaseX = x;
        mGestureBaseY = y;
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

    public void enlargeKeys(int[] arr) {
        // default weight 35 for softkey
        int MED_VAL = 35;
        int DEFAULT_GAP = 10;
        int HIGHLIGHT_VAL = MED_VAL;
        int data_max = Integer.MIN_VALUE;
        int data_min = Integer.MAX_VALUE;
        int data_sum = 0;

        for (int i = 0; i < Utils.ALPHABET_SIZE; i++) {
            if (data_max < arr[i]) data_max = arr[i];
            if (data_min > arr[i]) data_min = arr[i];
            data_sum += arr[i];
        }
        int data_average = (int) Math.round(data_sum * 1.0 / Utils.ALPHABET_SIZE);
        int data_gap = (int) Math.round(Utils.standardDeviation(arr));
        if (data_gap == 0) data_gap = 1;
        float converted_gap = (float) DEFAULT_GAP / data_gap;

        for (int i = 0; i < Utils.ALPHABET_SIZE; i++) {
            TextView softkey = mKeyboardView.findViewById(keyIdArr[i]);
            LinearLayout.LayoutParams lparam = (LinearLayout.LayoutParams)softkey.getLayoutParams();

            float converted_val = MED_VAL + ((arr[i] - data_average) * converted_gap);
            if (converted_val > MED_VAL + DEFAULT_GAP) {
                converted_val = MED_VAL + DEFAULT_GAP;
            } else if (converted_val < MED_VAL - DEFAULT_GAP) {
                converted_val = MED_VAL - DEFAULT_GAP;
            }
            lparam.weight = converted_val;
            softkey.setLayoutParams(lparam);

            if (lparam.weight > HIGHLIGHT_VAL) {
                setKeyHighlightColor(softkey);
            } else {
                resetKeyColor(softkey);
            }
        }
    }

    public void enlargeKeysIfNeeded() {
        if ((mState == Utils.STATE_SYMBOL) ||
                (mState == Utils.STATE_SYMBOL + Utils.STATE_SHIFT)) {
            resetKeyLayout();
            return;
        }
        if (InputWordController.get().getLength() == 0 ||
                InputWordController.get().getLastChar() == ' ') {
            resetKeyLayout();
            return;
        }

        String[] splitWord = InputWordController.get().getWord().split(" ");
        String lastWord = splitWord[splitWord.length - 1];
        for (int i = 0; i < lastWord.length(); i++) {
            if (Utils.isCharacterOrNumber(lastWord.charAt(i)) == CHARACTER_TYPE.NUMBER ||
                    Utils.isCharacterOrNumber(lastWord.charAt(i)) == CHARACTER_TYPE.SYMBOL) {
                resetKeyLayout();
                return;
            }
        }
        enlargeKeys(mTrie.find(lastWord));
    }

    private void setKeyPressColor(TextView softkey) {
        softkey.setBackgroundResource(R.drawable.softkey_shape_press);
    }

    private void setKeyHighlightColor(TextView softkey) {
        softkey.setBackgroundResource(R.drawable.softkey_shape_highlight);
    }

    private void resetKeyColor(TextView softkey) {
        softkey.setBackgroundResource(R.drawable.softkey_shape_normal);
    }

    private void createKeyIdArray() {
        keyIdArr[0] = R.id.key_pos_1_0;
        keyIdArr[1] = R.id.key_pos_2_4;
        keyIdArr[2] = R.id.key_pos_2_2;
        keyIdArr[3] = R.id.key_pos_1_2;
        keyIdArr[4] = R.id.key_pos_0_2;
        keyIdArr[5] = R.id.key_pos_1_3;
        keyIdArr[6] = R.id.key_pos_1_4;
        keyIdArr[7] = R.id.key_pos_1_5;
        keyIdArr[8] = R.id.key_pos_0_7;
        keyIdArr[9] = R.id.key_pos_1_6;
        keyIdArr[10] = R.id.key_pos_1_7;
        keyIdArr[11] = R.id.key_pos_1_8;
        keyIdArr[12] = R.id.key_pos_2_6;
        keyIdArr[13] = R.id.key_pos_2_5;
        keyIdArr[14] = R.id.key_pos_0_8;
        keyIdArr[15] = R.id.key_pos_0_9;
        keyIdArr[16] = R.id.key_pos_0_0;
        keyIdArr[17] = R.id.key_pos_0_3;
        keyIdArr[18] = R.id.key_pos_1_1;
        keyIdArr[19] = R.id.key_pos_0_4;
        keyIdArr[20] = R.id.key_pos_0_6;
        keyIdArr[21] = R.id.key_pos_2_3;
        keyIdArr[22] = R.id.key_pos_0_1;
        keyIdArr[23] = R.id.key_pos_2_1;
        keyIdArr[24] = R.id.key_pos_0_5;
        keyIdArr[25] = R.id.key_pos_2_0;
    }

    public int getState() {
        return mState;
    }

    private void createTimer(String data) {
        if (!data.equals("DEL") ||
                !mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_BACKKEY_LONGPRESS)) {
            return;
        }
        mLongPressTimerFlag = 0;
        mLongPressTimer = new Timer();
        TimerTask mLongPressTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mLongPressTimerFlag > 10) {
                    handleInputEvent(data);
                }
                if (mLongPressTimerFlag % 2 == 1) {
                    handleInputEvent(data);
                }
                mLongPressTimerFlag++;
            }
        };
        mLongPressTimer.schedule(mLongPressTimerTask, Utils.LONGPRESS_TIMER_DELAY, Utils.LONGPRESS_TIMER_PERIOD);
    }

    private void terminateTimer() {
        if (mLongPressTimer != null) {
            mLongPressTimer.cancel();
        }
    }
}
