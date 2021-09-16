package com.clab.jankey;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.VibrationEffect;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/** Controls the visible virtual keyboard view. */
final class Keyboard {

    private static final int STATE_NORMAL = 0;
    private static final int STATE_SHIFT = 1;
    private static final int STATE_SYMBOL = 2;
    private static final int NUM_STATES = 4;

    private enum GESTURE_DIRECTION {
        UP(0), RIGHTUP(1), RIGHT(2), RIGHTDOWN(3),
        DOWN(4), LEFTDOWN(5), LEFT(6), LEFTUP(7), NONE(8);
        private final int index;
        private GESTURE_DIRECTION(int index) {
            this.index = index;
        }
        public int toInt() {
            return index;
        }
    }

    private enum GESTURE_LOCATION {
        UP(0), RIGHTUP(1), RIGHT(2), RIGHTDOWN(3),
        DOWN(4), LEFTDOWN(5), LEFT(6), LEFTUP(7), NONE(8);
        private final int index;
        private GESTURE_LOCATION(int index) {
            this.index = index;
        }
        public int toInt() {
            return index;
        }
    }

    private final JanKeyService mJanKeyService;
    private final int mViewResId;
    private final SparseArray<String> mKeyMapping;
    private View mKeyboardView;
    private View mGestureGuideView;
    private LayoutInflater mLayoutInflater;
    private PopupWindow mGestureGuideViewContainer = new PopupWindow();
    private CustomVariables mCustomVariables = new CustomVariables();
    private DataBaseHelper mDataBaseHelper;
    private int mState;
    private float mGestureCurrentX, mGestureCurrentY, mGestureBaseX, mGestureBaseY;
    private Queue<Float> mGestureXQueue = new LinkedList<>();
    private Queue<Float> mGestureYQueue = new LinkedList<>();
    private int[] mGestureDirectionUsedFlag = new int[9];
    private Timer mTimer;
    private TimerTask mTouchDownTimerTask, mContinueInputTimerTask;
    private String mCurrentClickedKey;
    private String[] mDirectionCharacterMap = new String[9];


    private Keyboard(JanKeyService janKeyService, int viewResId,
                     SparseArray<String> keyMapping) {
        this.mJanKeyService = janKeyService;
        this.mViewResId = viewResId;
        this.mKeyMapping = keyMapping;
        this.mState = 0;

        initializeDataBaseHelper();
        initializeGestureDirectionUsedFlag();
        initializeDirectionCharacterMap();
//        createKeyIdArray();
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

    static Keyboard jankey(JanKeyService janKeyService) {
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

        return new Keyboard(janKeyService, R.layout.keyboard_jankey, keyMapping);
    }

    static Keyboard jankey_num(JanKeyService janKeyService) {
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

        return new Keyboard(janKeyService, R.layout.keyboard_jankey_num, keyMapping);
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
        Drawable drawable = softkey.getBackground();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                float softkeyWidth = softkey.getWidth();
                float gestureGuideViewWidth = dpToPx(101);
                float locationX = outLocation[0] - ((gestureGuideViewWidth - softkeyWidth) / 2);
                float locationY = outLocation[1] - (gestureGuideViewWidth / 2);
                showGestureGuideIfNeeded(view, locationX, locationY, data);
                ((TransitionDrawable) drawable).startTransition(0);
                handleTouchDown(data);
                break;
            case MotionEvent.ACTION_UP:
                hideGestureGuide();
                ((TransitionDrawable) drawable).resetTransition();
                break;
            case MotionEvent.ACTION_MOVE:
                GESTURE_DIRECTION loc = getGestureLocation(softkey, evt.getX(), evt.getY());
                if (getGestureDirectionUsedFlag(loc)) {
                    // skip
                } else {
                    updateGestureDirectionUsedFlag(loc, 1);
                    handleTouchDown(getDirectionCharacter(loc));
//                    handleGestureInput(getDirectionCharacter(loc));
                }
                break;
            default:
        }
        return true;

//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mCurrentClickedKey = data;
//                float softkeyWidth = softkey.getWidth();
//                float gestureGuideViewWidth = dpToPx(101);
//                float locationX = outLocation[0] - ((gestureGuideViewWidth - softkeyWidth) / 2);
//                float locationY = outLocation[1] - (gestureGuideViewWidth / 2);
//                showGestureGuideIfNeeded(view, locationX, locationY, data);
//                mGestureBaseX = mGestureCurrentX = evt.getX();
//                mGestureBaseY = mGestureCurrentY = evt.getY();
//                initializeGestureEventQueue(mGestureBaseX, mGestureBaseY);
//                initializeGestureDirectionUsedFlag();
//                touchTimerHandler(true);
//                ((TransitionDrawable) drawable).startTransition(0);
//                handleTouchDown(data, index);
//                break;
//            case MotionEvent.ACTION_UP:
//                hideGestureGuide();
//                touchTimerHandler(false);
//                ((TransitionDrawable) drawable).resetTransition();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                touchTimerHandler(false);
//                float mGestureCurrentX = evt.getX();
//                float mGestureCurrentY = evt.getY();
//                addGestureEventIntoQueue(mGestureCurrentX, mGestureCurrentY);
//
//                float distX = mGestureCurrentX - mGestureBaseX;
//                float distY = mGestureCurrentY - mGestureBaseY;
//                float angle = (float) Math.toDegrees(Math.atan2(distY, distX));
//
//                if (Math.abs(distX) < dpToPx(30) && Math.abs(distY) < dpToPx(30)) return false;
//
//                if (angle < -67.5 && angle > -112.5) {
//                    // Up
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.UP)) {
//                        handleGestureInput(getDirectionCharacter(GESTURE_DIRECTION.UP));
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.UP, 1);
//                    }
//                } else if (angle >= -67.5 && angle <= -22.5) {
//                    // RightUp
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTUP)) {
//                        handleGestureInput(getDirectionCharacter(GESTURE_DIRECTION.RIGHTUP));
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTUP, 1);
//                    }
//                } else if (angle > -22.5 && angle < 22.5) {
//                    // Right
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT)) {
//                        handleGestureInput(getDirectionCharacter(GESTURE_DIRECTION.RIGHT));
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT, 1);
//                    }
//                } else if (angle >= 22.5 && angle <= 67.5) {
//                    // RightDown
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTDOWN)) {
//                        handleGestureInput(getDirectionCharacter(GESTURE_DIRECTION.RIGHTDOWN));
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTDOWN, 1);
//                    }
//                } else if (angle > 67.5 && angle < 112.5) {
//                    // Down
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN)) {
//                        handleGestureInput(getDirectionCharacter(GESTURE_DIRECTION.DOWN));
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN, 1);
//                    }
//                } else if (angle >= 112.5 && angle < 157.5) {
//                    // LeftDown
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTDOWN)) {
//                        handleGestureInput(getDirectionCharacter(GESTURE_DIRECTION.LEFTDOWN));
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTDOWN, 1);
//                    }
//                } else if (angle >= 157.5 && angle <= 180 || angle <= -157.5 && angle >= -180) {
//                    // Left
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFT)) {
//                        handleGestureInput(getDirectionCharacter(GESTURE_DIRECTION.LEFT));
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFT, 1);
//                    }
//                } else if (angle >= -157.5 && angle <= -112.5) {
//                    //  LeftUp
//                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTUP)) {
//                        handleGestureInput(getDirectionCharacter(GESTURE_DIRECTION.LEFTUP));
//                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTUP, 1);
//                    }
//                }
//                break;
//            default:
//                // do nothing
//        }
//        return true;
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

    private void showGestureGuideIfNeeded(View view, float x, float y, String data) {
        if (!mDataBaseHelper.getSettingValue(mCustomVariables.SETTINGS_USE_SWIPE_POPUP)) {
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
            TextView softkey = (TextView) view;
            float softkeyWidth = softkey.getWidth();
            float softkeyHeight = softkey.getHeight();
            float gestureGuideViewSize = dpToPx(124);
            int []outLocation = new int[2];
            softkey.getLocationInWindow(outLocation);
            float locationX = outLocation[0] - ((gestureGuideViewSize - softkeyWidth) / 2);
            float locationY = outLocation[1] - ((gestureGuideViewSize - softkeyHeight) / 2);

            mGestureGuideView = mLayoutInflater.inflate(R.layout.gesture_guide, null);

            ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_2_0)).setText(mState == STATE_SHIFT ? "A" : "a");
            ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_1_0)).setText(mState == STATE_SHIFT ? "E" : "e");
            ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_0_0)).setText(mState == STATE_SHIFT ? "E" : "e");
            ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_0_1)).setText(mState == STATE_SHIFT ? "O" : "o");
            ((TextView) mGestureGuideView.findViewById(R.id.gesture_guide_0_2)).setText(mState == STATE_SHIFT ? "U" : "u");

            if (mGestureGuideView.getParent() != null) {
                ((ViewGroup) mGestureGuideView.getParent()).removeView(mGestureGuideView);
            }
            mGestureGuideViewContainer.setContentView(mGestureGuideView);
            mGestureGuideViewContainer.setWidth(dpToPx(124));
            mGestureGuideViewContainer.setHeight(dpToPx(124));
            mGestureGuideViewContainer.showAtLocation(view, 0, Math.round(locationX), Math.round(locationY));
        }
    }

    private void hideGestureGuide() {
        mGestureGuideViewContainer.dismiss();
    }

    private void handleTouchDown(String data) {
        if (mDataBaseHelper.getSettingValue(mCustomVariables.SETTINGS_USE_VIBRATION_FEEDBACK)) {
            mJanKeyService.getVibratorService().vibrate(
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
        mJanKeyService.handleTouchDown(getTextFromFuncKey(data));
    }

    private void handleGestureInput(String data) {
        if (data.isEmpty()) return;
        if ((mState == STATE_SYMBOL) || (mState == STATE_SYMBOL + STATE_SHIFT)) {
            return;
        }
        if (mDataBaseHelper.getSettingValue(mCustomVariables.SETTINGS_USE_VIBRATION_FEEDBACK)) {
            mJanKeyService.getVibratorService().vibrate(
                    VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        mJanKeyService.handleTouchDown(
                mState == STATE_SHIFT ? data.toUpperCase(Locale.ROOT) : data);
    }

    void reset() {
        mapKeys();
        mState = 0;
    }

    void initializeDataBaseHelper() {
        mDataBaseHelper = mJanKeyService.getDataBaseHelper();
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
        float density = mJanKeyService.getResources().getDisplayMetrics().density;
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

    static int counter;
    private void touchTimerHandler(boolean down) {
        if (down) {
            counter = 0;
            mTimer = new Timer();
            if (mTouchDownTimerTask != null) {
                mTouchDownTimerTask.cancel();
                mTouchDownTimerTask = null;
            }
            mTouchDownTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (mContinueInputTimerTask != null) {
                        mContinueInputTimerTask.cancel();
                        mContinueInputTimerTask = null;
                    }
                    mContinueInputTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            continueInputHandler();
                        }
                    };
                    mTimer.schedule(mContinueInputTimerTask, 250, 250);
                }
            };
            mTimer.schedule(mTouchDownTimerTask, 1000, Long.MAX_VALUE);
        } else {
            if (mTimer != null) mTimer.cancel();
        }
    }

    private void continueInputHandler() {
        if (mCurrentClickedKey.equals("SHI") || mCurrentClickedKey.equals("SYM") || mCurrentClickedKey.equals("ENT")) {
            return;
        }
        mJanKeyService.handleTouchDown(getTextFromFuncKey(mCurrentClickedKey));
    }

    private void initializeDirectionCharacterMap() {
        mDirectionCharacterMap[GESTURE_DIRECTION.UP.toInt()] = "o";
        mDirectionCharacterMap[GESTURE_DIRECTION.RIGHTUP.toInt()] = "u";
        mDirectionCharacterMap[GESTURE_DIRECTION.RIGHT.toInt()] = "";
        mDirectionCharacterMap[GESTURE_DIRECTION.RIGHTDOWN.toInt()] = "";
        mDirectionCharacterMap[GESTURE_DIRECTION.DOWN.toInt()] = "";
        mDirectionCharacterMap[GESTURE_DIRECTION.LEFTDOWN.toInt()] = "a";
        mDirectionCharacterMap[GESTURE_DIRECTION.LEFT.toInt()] = "e";
        mDirectionCharacterMap[GESTURE_DIRECTION.LEFTUP.toInt()] = "i";
        mDirectionCharacterMap[GESTURE_DIRECTION.NONE.toInt()] = "";
    }

    private String getDirectionCharacter(GESTURE_DIRECTION direction) {
        return mDirectionCharacterMap[direction.toInt()];
    }

    private GESTURE_DIRECTION getGestureLocation(TextView softkey, float x, float y) {
        float keyWidth = softkey.getWidth();
        float keyHeight = softkey.getHeight();
        // softkey width 32dp, height 55dp
        if (x >= -keyWidth && x <= 0 && y >= 0 && y <= keyHeight) {
            return GESTURE_DIRECTION.LEFT;
        } else if (x >= keyWidth && x <= 0 && y >= -keyHeight && y <= 0) {
            return GESTURE_DIRECTION.LEFTUP;
        } else if (x >= 0 && x <= keyWidth && y >= -keyHeight && y <= 0) {
            return GESTURE_DIRECTION.UP;
        } else if (x >= keyWidth && x <= (keyWidth * 2) && y >= -keyHeight && y <= 0) {
            return GESTURE_DIRECTION.RIGHTUP;
        } else if (x >= keyWidth && x <= (keyWidth * 2) && y >= 0 && y <= keyHeight) {
            return GESTURE_DIRECTION.RIGHT;
        }
        return GESTURE_DIRECTION.NONE;

    }
}
