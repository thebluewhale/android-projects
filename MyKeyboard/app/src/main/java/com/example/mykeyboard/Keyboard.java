package com.example.mykeyboard;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GestureDetectorCompat;

/** Controls the visible virtual keyboard view. */
final class Keyboard {

    private static final int NUM_STATES = 4;
    private static final int STATE_SHIFT = 1;
    private static final int STATE_SYMBOL = 2;

    private final MyKeyboardService mMyKeyboardService;
    private final int mViewResId;
    private final SparseArray<String> mKeyMapping;
    private View mKeyboardView;
    private View mGestureGuideView;
    private CustomVariables mCustomVariables = new CustomVariables();
    private int mState;
    private int[] keyIdArr = new int[mCustomVariables.ALPHABET_SIZE];
    private boolean mGestureInputPossible = true;
    private float mGesturePrevX, mGesturePrevY, mGestureCurrentX, mGestureCurrentY, mGestureInitialX, mGestureInitialY;

    private Keyboard(MyKeyboardService myKeyboardService, int viewResId,
                     SparseArray<String> keyMapping) {
        this.mMyKeyboardService = myKeyboardService;
        this.mViewResId = viewResId;
        this.mKeyMapping = keyMapping;
        this.mState = 0;
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
        } else {
            return data;
        }
    }

    static Keyboard qwerty(MyKeyboardService myKeyboardService) {
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
        return new Keyboard(myKeyboardService, R.layout.keyboard_10_9_9, keyMapping);
    }

    View inflateKeyboardView(LayoutInflater inflater, InputView inputView) {
        mKeyboardView = inflater.inflate(mViewResId, inputView, false);
        mapKeys();
        return mKeyboardView;
    }

    View inflateGestureGuideView(LayoutInflater inflater, InputView inputView) {
        mGestureGuideView = inflater.inflate(R.layout.gesture_guide, inputView, false);
        hideGestureGuide();
        return mGestureGuideView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void mapKeys() {
        for (int i = 0; i < mKeyMapping.size(); i++) {
            TextView softkey = mKeyboardView.findViewById(mKeyMapping.keyAt(i));
            String rawData = mKeyMapping.valueAt(i);
            String data = rawData.length()  != NUM_STATES ? rawData : rawData.substring(mState, mState + 1);
            softkey.setText(getLabel(data));
            final int index = i;
            softkey.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent evt) {
                    int []outLocation = new int[2];
                    mKeyboardView.findViewById(mKeyMapping.keyAt(index)).getLocationInWindow(outLocation);
                    float density = mMyKeyboardService.getResources().getDisplayMetrics().density;
                    int action = evt.getActionMasked();

                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            mGestureGuideView.setX(outLocation[0] - Math.round((101 - 35) / 2 * density + 0.5));
                            mGestureGuideView.setY(outLocation[1] - Math.round((101 - 45) / 2 * density + 0.5));
                            showGestureGuideIfNeeded(data);
                            mGestureInitialX = mGestureCurrentX = evt.getX();
                            mGestureInitialY = mGestureCurrentY = evt.getY();
                            mGestureInputPossible = true;
                            handleTouchDown(data, index);
                            break;
                        case MotionEvent.ACTION_UP:
                            hideGestureGuide();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            mGesturePrevX = mGestureCurrentX;
                            mGesturePrevY = mGestureCurrentY;
                            mGestureCurrentX = evt.getX();
                            mGestureCurrentY = evt.getY();

                            float angle = (float) Math.toDegrees(Math.atan2(mGestureCurrentY - mGesturePrevY, mGestureCurrentX - mGesturePrevX));
                            float distX = mGestureCurrentX - mGestureInitialX;
                            float distY = mGestureCurrentY - mGestureInitialY;

                            if (Math.abs(distX) < 30 && Math.abs(distY) < 30) return false;

                            if (angle < -67.5 && angle > -112.5) {
                                // Up
                                if (mGestureInputPossible) {
                                    mMyKeyboardService.handleTouchDown("a");
                                    mGestureInputPossible = false;
                                }
                            } else if (angle >= -67.5 && angle <= -22.5) {
                                // RightUp
                                if (mGestureInputPossible) {
                                    mMyKeyboardService.handleTouchDown("e");
                                    mGestureInputPossible = false;
                                }
                            } else if (angle > -22.5 && angle < 22.5) {
                                // Right
                                if (mGestureInputPossible) {
                                    mMyKeyboardService.handleTouchDown("i");
                                    mGestureInputPossible = false;
                                }
                            } else if (angle >= 22.5 && angle <= 67.5) {
                                // RightDown
                                if (mGestureInputPossible) {
                                    mMyKeyboardService.handleTouchDown("o");
                                    mGestureInputPossible = false;
                                }
                            } else if (angle > 67.5 && angle < 112.5) {
                                // Down
                                if (mGestureInputPossible) {
                                    mMyKeyboardService.handleTouchDown("u");
                                    mGestureInputPossible = false;
                                }
                            } else if (angle >= 112.5 && angle < 157.5) {
                                // LeftDown
                            } else if (angle >= 157.5 && angle <= 180 || angle <= -157.5 && angle >= -180) {
                                // Left
                            } else if (angle >= -157.5 && angle <= -112.5) {
                                //  LeftUp
                            }
                            break;
                        default:
                            // do nothing
                    }
                    return true;
                }
            });
        }
    }

    private void showGestureGuideIfNeeded(String data) {
        if (!("SHIT".equals(data) || "SYM".equals(data) || "DEL".equals(data) || "SPA".equals(data))) {
            mGestureGuideView.setVisibility(View.VISIBLE);
        }
    }

    private void hideGestureGuide() {
        mGestureGuideView.setVisibility(View.INVISIBLE);
    }

    private void handleTouchDown(String data, int index) {
        if ("SHI".equals(data)) {
            mState = mState ^ STATE_SHIFT;
            mapKeys();
            return;
        } else if ("SYM".equals(data)) {
            mState = (mState ^ STATE_SYMBOL) & ~STATE_SHIFT;
            mapKeys();
            return;
        }
        mMyKeyboardService.handleTouchDown(data);
    }

    void reset() {
        mapKeys();
        mState = 0;
    }

    void enlargeKeys(int[] arr) {
        // default width 40dp, height 45dp
        float density = mMyKeyboardService.getResources().getDisplayMetrics().density;
        int MED_VAL = 35;
        int DEFAULT_GAP = 12;
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
        int data_gap = data_max - data_min;
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

    void createKeyIdArray() {
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
}
