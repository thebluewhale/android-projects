package com.prototype.cheatakey;

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

    private static final int NUM_STATES = 4;
    private static final int STATE_SHIFT = 1;
    private static final int STATE_SYMBOL = 2;

    private enum GESTURE_DIRECTION {
        UP(0), RIGHTUP(1), RIGHT(2), RIGHTDOWN(3),
        DOWN(4), LEFTDOWN(5), LEFT(6), LEFTUP(7);
        private final int index;
        private GESTURE_DIRECTION(int index) {
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
    private View mGestureGuideView;
    private LayoutInflater mLayoutInflater;
    private PopupWindow mGestureGuideViewContainer = new PopupWindow();
    private CustomVariables mCustomVariables = new CustomVariables();
    private DataBaseHelper mDataBaseHelper;
    private int mState;
    private float mGestureCurrentX, mGestureCurrentY, mGestureBaseX, mGestureBaseY;
    private Queue<Float> mGestureXQueue = new LinkedList<>();
    private Queue<Float> mGestureYQueue = new LinkedList<>();
    private int[] mGestureDirectionUsedFlag = new int[8];
    private Timer mTimer;
    private TimerTask mTouchDownTimerTask, mContinueInputTimerTask;
    private String mCurrentClickedKey;


    private Keyboard(CheatAKeyService cheatAKeyService, int viewResId,
                     SparseArray<String> keyMapping) {
        this.mCheatAKeyService = cheatAKeyService;
        this.mViewResId = viewResId;
        this.mKeyMapping = keyMapping;
        this.mState = 0;

        initializeDataBaseHelper();
        initializeGestureDirectionUsedFlag();
//        createKeyIdArray();
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
            return "Enter";
        } else if ("VOWEL".equals(data)) {
            return "●";
        } else {
            return data;
        }
    }

    static Keyboard cheatakey(CheatAKeyService cheatAKeyService) {
        SparseArray<String> keyMapping = new SparseArray<>();
        keyMapping.put(R.id.key_pos_wave_mark, "~");
        keyMapping.put(R.id.key_pos_0_1, "qQ1\u007E");
        keyMapping.put(R.id.key_pos_0_2, "rR2\u2022");
        keyMapping.put(R.id.key_pos_0_3, "tT3\u221A");
        keyMapping.put(R.id.key_pos_0_4, "kK4}");
        keyMapping.put(R.id.key_pos_0_5, "pP5\u2206");
        keyMapping.put(R.id.key_pos_exclamation_mark, "!");

        keyMapping.put(R.id.key_pos_caret_mark, "^");
        keyMapping.put(R.id.key_pos_1_1, "sS6#");
        keyMapping.put(R.id.key_pos_1_2, "dD7$");
        keyMapping.put(R.id.key_pos_1_3, "gG8&");
        keyMapping.put(R.id.key_pos_1_4, "jJ9{");
        keyMapping.put(R.id.key_pos_1_5, "lL0)");
        keyMapping.put(R.id.key_pos_question_mark, "?");

        keyMapping.put(R.id.key_pos_semicolon_mark, ";");
        keyMapping.put(R.id.key_pos_2_1, "cC'\u00AE");
        keyMapping.put(R.id.key_pos_2_2, "fF_\u00A5");
        keyMapping.put(R.id.key_pos_2_3, "vV:\u2122");
        keyMapping.put(R.id.key_pos_2_4, "bB;\u2713");
        keyMapping.put(R.id.key_pos_vowel, "VOWEL");
        keyMapping.put(R.id.key_pos_period_mark, ".");

        keyMapping.put(R.id.key_pos_asterisk_mark, "*");
        keyMapping.put(R.id.key_pos_3_1, "zZ*%");
        keyMapping.put(R.id.key_pos_3_2, "xX\"\u00A9");
        keyMapping.put(R.id.key_pos_3_3, "nN![");
        keyMapping.put(R.id.key_pos_3_4, "mM?]");
        keyMapping.put(R.id.key_pos_del, "DEL");

        keyMapping.put(R.id.key_pos_symbol, "SYM");
        keyMapping.put(R.id.key_pos_shift, "SHI");
        keyMapping.put(R.id.key_pos_comma_mark, ",");
        keyMapping.put(R.id.key_pos_space, "SPA");
        keyMapping.put(R.id.key_pos_enter, "ENT");

        return new Keyboard(cheatAKeyService, R.layout.keyboard_cheatakey, keyMapping);
    }

    static Keyboard cheatakey_num(CheatAKeyService cheatAKeyService) {
        SparseArray<String> keyMapping = new SparseArray<>();
        keyMapping.put(R.id.key_pos_wave_mark, "~");
        keyMapping.put(R.id.key_pos_0_1, "qQ1\u007E");
        keyMapping.put(R.id.key_pos_0_2, "rR2\u2022");
        keyMapping.put(R.id.key_pos_0_3, "tT3\u221A");
        keyMapping.put(R.id.key_pos_0_4, "kK(}");
        keyMapping.put(R.id.key_pos_0_5, "pP0\u2206");
        keyMapping.put(R.id.key_pos_exclamation_mark, "!");

        keyMapping.put(R.id.key_pos_caret_mark, "^");
        keyMapping.put(R.id.key_pos_1_1, "sS#\u00A2");
        keyMapping.put(R.id.key_pos_1_2, "dD$\u20AC");
        keyMapping.put(R.id.key_pos_1_3, "gG&\u005E");
        keyMapping.put(R.id.key_pos_1_4, "jJ+{");
        keyMapping.put(R.id.key_pos_1_5, "lL)\\");
        keyMapping.put(R.id.key_pos_question_mark, "?");

        keyMapping.put(R.id.key_pos_semicolon_mark, ";");
        keyMapping.put(R.id.key_pos_2_1, "cC'\u00AE");
        keyMapping.put(R.id.key_pos_2_2, "fF_\u00A5");
        keyMapping.put(R.id.key_pos_2_3, "vV:\u2122");
        keyMapping.put(R.id.key_pos_2_4, "bB;\u2713");
        keyMapping.put(R.id.key_pos_vowel, "VOWEL");
        keyMapping.put(R.id.key_pos_period_mark, ".");

        keyMapping.put(R.id.key_pos_asterisk_mark, "*");
        keyMapping.put(R.id.key_pos_3_1, "zZ*%");
        keyMapping.put(R.id.key_pos_3_2, "xX\"\u00A9");
        keyMapping.put(R.id.key_pos_3_3, "nN![");
        keyMapping.put(R.id.key_pos_3_4, "mM?]");
        keyMapping.put(R.id.key_pos_del, "DEL");

        keyMapping.put(R.id.key_pos_symbol, "SYM");
        keyMapping.put(R.id.key_pos_shift, "SHI");
        keyMapping.put(R.id.key_pos_comma_mark, ",");
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

        return new Keyboard(cheatAKeyService, R.layout.keyboard_cheatakey_num, keyMapping);
    }

    View inflateKeyboardView(LayoutInflater inflater, InputView inputView) {
        mLayoutInflater = inflater;
        mKeyboardView = mLayoutInflater.inflate(mViewResId, inputView, false);
        mapKeys();
        return mKeyboardView;
    }

    private boolean onSoftkeyTouch(View view, MotionEvent evt, TextView softkey, int index, String data) {
        int []outLocation = new int[2];
        mKeyboardView.findViewById(mKeyMapping.keyAt(index)).getLocationInWindow(outLocation);
        float density = mCheatAKeyService.getResources().getDisplayMetrics().density;
        int action = evt.getActionMasked();
        Drawable drawable = softkey.getBackground();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mCurrentClickedKey = data;
                float softkeyWidth = softkey.getWidth();
                float gestureGuideViewWidth = Math.round(101 * density + 0.5);
                float locationX = outLocation[0] - ((gestureGuideViewWidth - softkeyWidth) / 2);
                float locationY = outLocation[1] - gestureGuideViewWidth;
                showGestureGuideIfNeeded(view, locationX, locationY, data);
                mGestureBaseX = mGestureCurrentX = evt.getX();
                mGestureBaseY = mGestureCurrentY = evt.getY();
                initializeGestureEventQueue(mGestureBaseX, mGestureBaseY);
                initializeGestureDirectionUsedFlag();
                touchTimerHandler(true);
                ((TransitionDrawable) drawable).startTransition(0);
                handleTouchDown(data, index);
                break;
            case MotionEvent.ACTION_UP:
                hideGestureGuide();
                touchTimerHandler(false);
                ((TransitionDrawable) drawable).resetTransition();
                break;
            case MotionEvent.ACTION_MOVE:
                touchTimerHandler(false);
                float mGestureCurrentX = evt.getX();
                float mGestureCurrentY = evt.getY();
                addGestureEventIntoQueue(mGestureCurrentX, mGestureCurrentY);

                float distX = mGestureCurrentX - mGestureBaseX;
                float distY = mGestureCurrentY - mGestureBaseY;
                float angle = (float) Math.toDegrees(Math.atan2(distY, distX));

                if (Math.abs(distX) < dpToPx(30) && Math.abs(distY) < dpToPx(30)) return false;

                if (angle < -67.5 && angle > -112.5) {
                    // Up
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.UP)) {
                        handleGestureInput("o");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.UP, 1);
                    }
                } else if (angle >= -67.5 && angle <= -22.5) {
                    // RightUp
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTUP)) {
                        handleGestureInput("i");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTUP, 1);
                    }
                } else if (angle > -22.5 && angle < 22.5) {
                    // Right
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT)) {
                        handleGestureInput("a");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHT, 1);
                    }
                } else if (angle >= 22.5 && angle <= 67.5) {
                    // RightDown
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTDOWN)) {
                        handleGestureInput("y");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.RIGHTDOWN, 1);
                    }
                } else if (angle > 67.5 && angle < 112.5) {
                    // Down
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN)) {
                        handleGestureInput("u");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.DOWN, 1);
                    }
                } else if (angle >= 112.5 && angle < 157.5) {
                    // LeftDown
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTDOWN)) {
                        handleGestureInput("w");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTDOWN, 1);
                    }
                } else if (angle >= 157.5 && angle <= 180 || angle <= -157.5 && angle >= -180) {
                    // Left
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFT)) {
                        handleGestureInput("e");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFT, 1);
                    }
                } else if (angle >= -157.5 && angle <= -112.5) {
                    //  LeftUp
                    if (!getGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTUP)) {
                        handleGestureInput("h");
                        updateGestureDirectionUsedFlag(GESTURE_DIRECTION.LEFTUP, 1);
                    }
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
        mCheatAKeyService.handleTouchDown(getTextFromFuncKey(data));
    }

    private void handleGestureInput(String data) {
        if ((mState == STATE_SYMBOL) || (mState == STATE_SYMBOL + STATE_SHIFT)) {
            return;
        }
        mCheatAKeyService.handleTouchDown(
                mState == STATE_SHIFT ? data.toUpperCase(Locale.ROOT) : data);
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

    boolean useDoubleSpaceToPeriod() {
        return mDataBaseHelper.getSettingValue(mCustomVariables.SETTINGS_USE_AUTO_PERIOD);
    }
    
    private int dpToPx(float dp) {
        float density = mCheatAKeyService.getResources().getDisplayMetrics().density;
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
        mCheatAKeyService.handleTouchDown(getTextFromFuncKey(mCurrentClickedKey));
    }
}