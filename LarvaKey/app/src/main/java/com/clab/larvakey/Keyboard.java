package com.clab.larvakey;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
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

/** Controls the visible virtual keyboard view. */
final class Keyboard {
    private enum GESTURE_DIRECTION {
        UP(0), RIGHTUP(1), RIGHT(2), RIGHTDOWN(3),
        DOWN(4), LEFTDOWN(5), LEFT(6), LEFTUP(7),
        STARTING_POINT(8), SHOULD_COME_BACK(9);
        private final int index;
        private GESTURE_DIRECTION(int index) {
            this.index = index;
        }
        public int toInt() {
            return index;
        }
    }
    private final LarvaKeyService mLarvaKeyService;
    private final int mViewResId;
    private final SparseArray<String> mKeyMapping;
    private View mKeyboardView;
    private View mGestureGuideView;
    private LayoutInflater mLayoutInflater;
    private PopupWindow mGestureGuideViewContainer = new PopupWindow();
    private CustomVariables mCustomVariables = new CustomVariables();
    private DataBaseHelper mDataBaseHelper;
    private int mState;
    private int[] keyIdArr = new int[mCustomVariables.ALPHABET_SIZE];
    private float mGestureInitialX, mGestureInitialY, mGestureCurrentX, mGestureCurrentY, mGestureBaseX, mGestureBaseY;
    private Queue<Float> mGestureXQueue = new LinkedList<>();
    private Queue<Float> mGestureYQueue = new LinkedList<>();
    private int[] mGestureDirectionUsedFlag = new int[10];

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

    private static String getLabel(String data) {
        if ("SHI".equals(data)) {
            return "↑";
        } else if ("DEL".equals(data)) {
            return "←";
        } else if ("SYM".equals(data)) {
            return "?123";
        } else if ("SPA".equals(data)) {
            return "[            ]";
        } else if ("ENT".equals(data)) {
            return "↩";
        } else if ("SET".equals(data)) {
            return "⁘";
        } else {
            return data;
        }
    }

    static Keyboard qwerty(LarvaKeyService larvaKeyService) {
        SparseArray<String> keyMapping = new SparseArray<>();
        keyMapping.put(R.id.key_pos_0_0, "qQ1\u007E");
        keyMapping.put(R.id.key_pos_0_1, "wW2\u0060");
        keyMapping.put(R.id.key_pos_0_2, "eE3\u007C");
        keyMapping.put(R.id.key_pos_0_3, "rR4\u2022");
        keyMapping.put(R.id.key_pos_0_4, "tT5\u221A");
        keyMapping.put(R.id.key_pos_0_5, "yY6\u03C0");
        keyMapping.put(R.id.key_pos_0_6, "uU7\u00F7");
        keyMapping.put(R.id.key_pos_0_7, "iI8\u00D7");
        keyMapping.put(R.id.key_pos_0_8, "oO9\u00B6");
        keyMapping.put(R.id.key_pos_0_9, "pP0\u2206");
        keyMapping.put(R.id.key_pos_1_0, "aA@\u00A3");
        keyMapping.put(R.id.key_pos_1_1, "sS#\u00A2");
        keyMapping.put(R.id.key_pos_1_2, "dD$\u20AC");
        keyMapping.put(R.id.key_pos_1_3, "fF_\u00A5");
        keyMapping.put(R.id.key_pos_1_4, "gG&\u005E");
        keyMapping.put(R.id.key_pos_1_5, "hH-=");
        keyMapping.put(R.id.key_pos_1_6, "jJ+{");
        keyMapping.put(R.id.key_pos_1_7, "kK(}");
        keyMapping.put(R.id.key_pos_1_8, "lL)\\");
        keyMapping.put(R.id.key_pos_2_0, "zZ*%");
        keyMapping.put(R.id.key_pos_2_1, "xX\"\u00A9");
        keyMapping.put(R.id.key_pos_2_2, "cC'\u00AE");
        keyMapping.put(R.id.key_pos_2_3, "vV:\u2122");
        keyMapping.put(R.id.key_pos_2_4, "bB;\u2713");
        keyMapping.put(R.id.key_pos_2_5, "nN![");
        keyMapping.put(R.id.key_pos_2_6, "mM?]");
        keyMapping.put(R.id.key_pos_bottom_0, ",,,<");
        keyMapping.put(R.id.key_pos_bottom_1, "...>");
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
        int []outLocation = new int[2];
        mKeyboardView.findViewById(mKeyMapping.keyAt(index)).getLocationInWindow(outLocation);
        float density = mLarvaKeyService.getResources().getDisplayMetrics().density;
        int action = evt.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                float softkeyWidth = softkey.getWidth();
                float gestureGuideViewWidth = Math.round(101 * density + 0.5);
                float locationX = outLocation[0] - ((gestureGuideViewWidth - softkeyWidth) / 2);
                float locationY = outLocation[1] - Math.round((101 - 45) / 2 * density + 0.5);
                showGestureGuideIfNeeded(view, locationX, locationY, data);
                mGestureInitialX = mGestureBaseX = mGestureCurrentX = evt.getX();
                mGestureInitialY = mGestureBaseY = mGestureCurrentY = evt.getY();
                initializeGestureEventQueue(mGestureBaseX, mGestureBaseY);
                initializeGestureDirectionUsedFlag();
                handleTouchDown(data, index);
                break;
            case MotionEvent.ACTION_UP:
                hideGestureGuide();
                break;
            case MotionEvent.ACTION_MOVE:
                float mGestureCurrentX = evt.getX();
                float mGestureCurrentY = evt.getY();

                if (getGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK)) {
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

                if (Math.abs(distX) < dpToPx(30) && Math.abs(distY) < dpToPx(30)) return false;

                if (angle < -67.5 && angle > -112.5) {
                    // Up
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.UP)) {
                        mLarvaKeyService.handleTouchDown("a");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.UP, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= -67.5 && angle <= -22.5) {
                    // RightUp
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTUP)) {
                        mLarvaKeyService.handleTouchDown("e");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTUP, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle > -22.5 && angle < 22.5) {
                    // Right
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT)) {
                        mLarvaKeyService.handleTouchDown("i");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= 22.5 && angle <= 67.5) {
                    // RightDown
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTDOWN)) {
                        mLarvaKeyService.handleTouchDown("o");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTDOWN, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle > 67.5 && angle < 112.5) {
                    // Down
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN)) {
                        mLarvaKeyService.handleTouchDown("u");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN, 1);
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                    }
                } else if (angle >= 112.5 && angle < 157.5) {
                    // LeftDown
                    updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
                } else if (angle >= 157.5 && angle <= 180 || angle <= -157.5 && angle >= -180) {
                    // Left
                    updateGestureDirectionUsedFlag(GESTURE_DIRECTION.SHOULD_COME_BACK, 1);
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

    @SuppressLint("ClickableViewAccessibility")
    private void mapKeys() {
        for (int i = 0; i < mKeyMapping.size(); i++) {
            TextView softkey = mKeyboardView.findViewById(mKeyMapping.keyAt(i));
            if (softkey != null) {
                String rawData = mKeyMapping.valueAt(i);
                String data = rawData.length() != mCustomVariables.STATE_NUMBER ? rawData : rawData.substring(mState, mState + 1);
                softkey.setText(getLabel(data));
                final int index = i;
                softkey.setOnTouchListener((view, evt) -> onSoftkeyTouch(view, evt, softkey, index, data));
            }
        }
    }

    private void showGestureGuideIfNeeded(View view, float x, float y, String data) {
        if (!mDataBaseHelper.getSettingValue(mCustomVariables.SETTINGS_USE_SWIPE_POPUP)) {
            return;
        }
        if (!("SHI".equals(data) || "SYM".equals(data) || "DEL".equals(data) ||
                "SPA".equals(data) || "ENT".equals(data) || "~".equals(data) ||
                "!".equals(data) || "^".equals(data) || "?".equals(data) ||
                ";".equals(data) || ".".equals(data) ||
                "*".equals(data) || ",".equals(data))) {
            mGestureGuideView = mLayoutInflater.inflate(R.layout.gesture_guide, null);
            if (mGestureGuideView.getParent() != null) {
                ((ViewGroup) mGestureGuideView.getParent()).removeView(mGestureGuideView);
            }
            mGestureGuideViewContainer.setContentView(mGestureGuideView);
            mGestureGuideViewContainer.setWidth(dpToPx(101));
            mGestureGuideViewContainer.setHeight(dpToPx(101));
            mGestureGuideViewContainer.showAtLocation(view, 0, (int)x, (int)y);
        }
    }

    private void hideGestureGuide() {
        mGestureGuideViewContainer.dismiss();
    }

    private void handleTouchDown(String data, int index) {
        if (mDataBaseHelper.getSettingValue(mCustomVariables.SETTINGS_USE_VIBRATION_FEEDBACK)) {
            mLarvaKeyService.getVibratorService().vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        if ("SHI".equals(data)) {
            mState = mState ^ mCustomVariables.STATE_SHIFT;
            mapKeys();
        } else if ("SYM".equals(data)) {
            mState = (mState ^ mCustomVariables.STATE_SYMBOL) & ~mCustomVariables.STATE_SHIFT;
            mapKeys();
        }
        mLarvaKeyService.handleTouchDown(getTextFromFuncKey(data));
        mLarvaKeyService.enlargeKeysIfNeeded();
    }

    public void reset() {
        mapKeys();
        mState = 0;
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
        return mDataBaseHelper.getSettingValue(mCustomVariables.SETTINGS_USE_AUTO_PERIOD);
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
        mGestureBaseX = mGestureInitialX;
        mGestureBaseY = mGestureInitialY;
    }

    private boolean isGestureCameBack(float initialX, float initialY, float currentX, float currentY) {
        float distX = (float) Math.pow(initialX - currentX, 2);
        float distY = (float) Math.pow(initialY - currentY, 2);
        return Math.sqrt(distX + distY) < dpToPx(5);
    }

    public void enlargeKeys(int[] arr) {
        // default width 40dp, height 45dp
        float density = mLarvaKeyService.getResources().getDisplayMetrics().density;
        int MED_VAL = 35;
        int DEFAULT_GAP = 8;
        int HIGHLIGHT_VAL = MED_VAL;
        int data_max = Integer.MIN_VALUE;
        int data_min = Integer.MAX_VALUE;
        int data_sum = 0;

        for (int i = 0; i < mCustomVariables.ALPHABET_SIZE; i++) {
            if (data_max < arr[i]) data_max = arr[i];
            if (data_min > arr[i]) data_min = arr[i];
            data_sum += arr[i];
        }
        int data_average = (int) Math.round(data_sum * 1.0 / mCustomVariables.ALPHABET_SIZE);
        int data_gap = (int) Math.round(standardDeviation(arr));
        if (data_gap == 0) data_gap = 1;
        float converted_gap = (float) DEFAULT_GAP / data_gap;

        for (int i = 0; i < mCustomVariables.ALPHABET_SIZE; i++) {
            TextView softkey = mKeyboardView.findViewById(keyIdArr[i]);
            LinearLayout.LayoutParams lparam = (LinearLayout.LayoutParams)softkey.getLayoutParams();

            float converted_val = MED_VAL + ((arr[i] - data_average) * converted_gap);
            if (converted_val > MED_VAL + DEFAULT_GAP) {
                converted_val = MED_VAL + DEFAULT_GAP;
            } else if (converted_val < MED_VAL - DEFAULT_GAP) {
                converted_val = MED_VAL - DEFAULT_GAP;
            }
            lparam.width = (int) Math.round(converted_val * density + 0.5);
            softkey.setLayoutParams(lparam);

            Drawable drawable = softkey.getBackground();
            if (lparam.width > (int) Math.round(HIGHLIGHT_VAL * density + 0.5)) {
                ((TransitionDrawable) drawable).startTransition(300);
            } else {
                ((TransitionDrawable) drawable).resetTransition();
            }
        }
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

    private double mean(int[] arr) {
        int total = 0;
        for (int i = 0; i < arr.length; i++) {
            total = total + arr[i];
        }
        return total / arr.length;
    }

    private double variance(int[] arr) {
        double totalDev = 0;
        double totalMean = mean(arr);
        for (int i = 0; i < arr.length; i++) {
            totalDev = totalDev + Math.pow(totalMean - arr[i], 2);
        }
        return totalDev / (arr.length - 1);
    }

    private double standardDeviation(int[] arr) {
        return Math.sqrt(variance(arr));
    }

    public int getState() {
        return mState;
    }
}
