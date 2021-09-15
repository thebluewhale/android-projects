package com.clab.larvakey;

import android.annotation.SuppressLint;
import android.os.VibrationEffect;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/** Controls the visible virtual keyboard view. */
final class Keyboard {

    private final LarvaKeyService mLarvaKeyService;
    private final int mViewResId;
    private final SparseArray<String> mKeyMapping;
    private View mKeyboardView;
    private View mGestureGuideView;
    private LayoutInflater mLayoutInflater;
    private PopupWindow mGestureGuideViewContainer = new PopupWindow();
    private DataBaseHelper mDataBaseHelper;
    private int mState;
    private int[] keyIdArr = new int[Utils.ALPHABET_SIZE];
    private float mGestureInitialX, mGestureInitialY, mGestureCurrentX, mGestureCurrentY, mGestureBaseX, mGestureBaseY;
    private Queue<Float> mGestureXQueue = new LinkedList<>();
    private Queue<Float> mGestureYQueue = new LinkedList<>();
    private int[] mGestureDirectionUsedFlag = new int[10];
    private Timer mLongPressTimer;
    private TimerTask mLongPressTimerTask;

    private Keyboard(LarvaKeyService larvaKeyService, int viewResId,
                     SparseArray<String> keyMapping) {
        this.mLarvaKeyService = larvaKeyService;
        this.mViewResId = viewResId;
        this.mKeyMapping = keyMapping;
        this.mState = 0;

        initializeDataBaseHelper();
        initializeGestureDirectionUsedFlag();
        createKeyIdArray();
    }

    private String getLabel(String data) {
        if ("SHI".equals(data)) {
            if (mState == Utils.STATE_SYMBOL) {
                return "1/2";
            } else if (mState == Utils.STATE_SYMBOL + Utils.STATE_SHIFT) {
                return "2/2";
            }
            return "↑";
        } else if ("DEL".equals(data)) {
            return "←";
        } else if ("SYM".equals(data)) {
            if (mState == Utils.STATE_SYMBOL || mState == Utils.STATE_SYMBOL + Utils.STATE_SHIFT) {
                return "abc";
            }
            return "!#1";
        } else if ("SPA".equals(data)) {
            return "SPACE";
        } else if ("ENT".equals(data)) {
            return "↲";
        } else if ("SET".equals(data)) {
            return "≡";
        } else {
            return data;
        }
    }

    static Keyboard qwerty(LarvaKeyService larvaKeyService) {
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
        return new Keyboard(larvaKeyService, R.layout.keyboard_10_9_9, keyMapping);
    }

    public View inflateKeyboardView(LayoutInflater inflater, InputView inputView) {
        mLayoutInflater = inflater;
        mKeyboardView = mLayoutInflater.inflate(mViewResId, inputView, false);
        mapKeys();
        return mKeyboardView;
    }

    private boolean onSoftkeyTouch(View view, MotionEvent evt, TextView softkey, int index, String data) {
        int action = evt.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                showGestureGuideIfNeeded(view, softkey, data);
                initializeAllGestureDatas(evt.getX(), evt.getY());
                handleTouchDown(data);
                createTimer(data);
                setKeyPressColor(softkey);
                break;
            case MotionEvent.ACTION_UP:
                hideGestureGuide();
                terminateTimer();
                mLarvaKeyService.enlargeKeysIfNeeded();
                resetKeyColor(softkey);
                break;
            case MotionEvent.ACTION_MOVE:
                float mGestureCurrentX = evt.getX();
                float mGestureCurrentY = evt.getY();

                if (getGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK)) {
                    // TODO: gestureCameBack 플래그를 사용할 것이 아니라,
                    // 제스처가 softkey 밖으로 나가면 그때부터 gestureKeyEventQueue에 담아서 궤적을 계산하고
                    // softkey 안에서 움직이는 move 이벤트는 모두 무시하는 것으로 로직을 짜볼 필요가 있음.
                    if (isGestureCameBack(mGestureInitialX, mGestureInitialY, mGestureCurrentX, mGestureCurrentY)) {
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.STARTING_POINT, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 0);
                    } else {
                        return true;
                    }
                }

                addGestureEventIntoQueue(mGestureCurrentX, mGestureCurrentY);

                float distX = mGestureCurrentX - mGestureBaseX;
                float distY = mGestureCurrentY - mGestureBaseY;
                float angle = (float) Math.toDegrees(Math.atan2(distY, distX));

                if (Math.abs(distX) < dpToPx(30) && Math.abs(distY) < dpToPx(30)) {
                    return true;
                }

                terminateTimer();

                if (angle < -67.5 && angle > -112.5) {
                    // Up
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.UP)) {
                        handleTouchDown("i");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.UP, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= -67.5 && angle <= -22.5) {
                    // RightUp
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTUP)) {
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTUP, 1);
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
//                    }
                    updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                } else if (angle > -22.5 && angle < 22.5) {
                    // Right
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT)) {
                        handleTouchDown("e");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= 22.5 && angle <= 67.5) {
                    // RightDown
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTDOWN)) {
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTDOWN, 1);
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
//                    }
                    updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                } else if (angle > 67.5 && angle < 112.5) {
                    // Down
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN)) {
                        handleTouchDown("u");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= 112.5 && angle < 157.5) {
                    // LeftDown
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTDOWN)) {
                        handleTouchDown("o");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTDOWN, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= 157.5 && angle <= 180 || angle <= -157.5 && angle >= -180) {
                    // Left
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFT)) {
                        handleTouchDown("a");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFT, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= -157.5 && angle <= -112.5) {
                    //  LeftUp
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTUP)) {
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTUP, 1);
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
//                    }
                    updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                }
                break;
            default:
                // do nothing
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void mapKeys() {
        for (int i = 0; i < mKeyMapping.size(); i++) {
            TextView softkey = mKeyboardView.findViewById(mKeyMapping.keyAt(i));
            if (softkey != null) {
                String rawData = mKeyMapping.valueAt(i);
                String data = rawData.length() != Utils.STATE_NUMBER ? rawData : rawData.substring(mState, mState + 1);
                softkey.setText(getLabel(data));
                final int index = i;
                softkey.setOnTouchListener((view, evt) -> onSoftkeyTouch(view, evt, softkey, index, data));
            }
        }
    }

    private void showGestureGuideIfNeeded(View view, TextView softkey, String data) {
        float softkeyWidth = softkey.getWidth();
        float gestureGuideViewWidth = dpToPx(101);
        int []outLocation = new int[2];
        view.getLocationInWindow(outLocation);
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

        mGestureGuideView = mLayoutInflater.inflate(R.layout.gesture_guide, null);
        if (mGestureGuideView.getParent() != null) {
            ((ViewGroup) mGestureGuideView.getParent()).removeView(mGestureGuideView);
        }
        mGestureGuideViewContainer.setContentView(mGestureGuideView);
        mGestureGuideViewContainer.setWidth(dpToPx(101));
        mGestureGuideViewContainer.setHeight(dpToPx(101));
        mGestureGuideViewContainer.showAtLocation(view, 0, Math.round(locationX), Math.round(locationY));
    }

    private void hideGestureGuide() {
        mGestureGuideViewContainer.dismiss();
    }

    private void handleTouchDown(String data) {
        if (mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_VIBRATION_FEEDBACK)) {
            mLarvaKeyService.getVibratorService().vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        if ("SHI".equals(data)) {
            mState = mState ^ Utils.STATE_SHIFT;
            mapKeys();
        } else if ("SYM".equals(data)) {
            mState = (mState ^ Utils.STATE_SYMBOL) & ~Utils.STATE_SHIFT;
            mapKeys();
        }
        mLarvaKeyService.handleTouchDown(getTextFromFuncKey(data));
    }

    public void redrawKeyboard() {
        mapKeys();
        mState = 0;
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

    private void initializeDataBaseHelper() {
        mDataBaseHelper = mLarvaKeyService.getDataBaseHelper();
    }

    private String getTextFromFuncKey(String data) {
        if ("VOWEL".equals(data)) {
            return "";
        } else {
            return data;
        }
    }

    boolean useDoubleSpaceToPeriod() {
        return mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_AUTO_PERIOD);
    }

    private int dpToPx(float dp) {
        float density = mLarvaKeyService.getResources().getDisplayMetrics().density;
        return (int) Math.round(dp * density + 0.5);
    }

    private void initializeGestureEventQueue(float initX, float initY) {
        mGestureXQueue.clear();
        mGestureYQueue.clear();
        mGestureXQueue.add(initX);
        mGestureYQueue.add(initY);
    }

    private void addGestureEventIntoQueue(float x, float y) {
        if (mGestureXQueue.size() < 10) {
            mGestureXQueue.add(x);
        } else {
            mGestureBaseX = mGestureXQueue.poll();
            mGestureXQueue.add(x);
        }
        if (mGestureYQueue.size() < 10) {
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
        mGestureInitialX = mGestureBaseX = mGestureCurrentX = x;
        mGestureInitialY = mGestureBaseY = mGestureCurrentY = y;
    }

    private boolean isGestureCameBack(float initialX, float initialY, float currentX, float currentY) {
        float distX = (float) Math.pow(initialX - currentX, 2);
        float distY = (float) Math.pow(initialY - currentY, 2);
        return Math.sqrt(distX + distY) < dpToPx(5);
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
        if (!data.equals("DEL")) {
            return;
        }
        mLongPressTimer = new Timer();
        mLongPressTimerTask = new TimerTask() {
            @Override
            public void run() {
                handleTouchDown(data);
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
