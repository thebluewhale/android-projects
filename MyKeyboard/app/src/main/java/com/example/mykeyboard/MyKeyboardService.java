package com.example.mykeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GestureDetectorCompat;

import java.io.IOException;

public class MyKeyboardService extends InputMethodService {
    private InputView mInputView = null;
    private Keyboard mKeyboard = null;
    private InputConnection mInputConnection;
    private boolean isCaps = false;
    private Trie mTrie = null;
    private StringBuilder mInputWord = new StringBuilder();
    private VelocityTracker mVelocityTracker = null;
    private float mInitialTouchX, mInitialTouchY;
    private float mPrevTouchX, mPrevTouchY;
    private float mTouchX, mTouchY;

    @Override
    public View onCreateInputView() {
        mTrie = new Trie(this);

        mInputView = (InputView) LayoutInflater.from(this).inflate(R.layout.input_view, null);
        mKeyboard = Keyboard.qwerty(this);
        mInputView.addView(mKeyboard.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        return mInputView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        resetKeyboardLayout();
    }

    void resetKeyboardLayout() {
        if (mKeyboard != null) {
            mKeyboard = Keyboard.qwerty(this);
            mInputView.addView(mKeyboard.inflateKeyboardView(LayoutInflater.from(this), mInputView));
            mKeyboard.reset();
        }
    }

//    void handleTouch(String data, MotionEvent evt) {
//        int index = evt.getActionIndex();
//        int action = evt.getActionMasked();
//        int pointerId = evt.getPointerId(index);
//        final int MIN_X_DISTANCE = 100;
//        final int MAX_Y_DISTANCE = 15;
//
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mInitialTouchX = evt.getX();
//                mInitialTouchY = evt.getY();
//                break;
//            case MotionEvent.ACTION_UP:
//                mGestureState = GestureState.GESTURE_STATE_NONE;
//                mInitialTouchX = mInitialTouchY = mPrevTouchX = mPrevTouchY = 0;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float angle = (float) Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
//                if(angle > 45 && angle <= 135) return UP;
//                else if(angle > 135 && angle <= 225) return LEFT;
//                else if(angle > 225 && angle <= 315) return DOWN;
//                else return RIGHT;
//                if (mPrevTouchX == 0) {
//                    mPrevTouchX = evt.getX();
//                    mPrevTouchY = evt.getY();
//                } else {
//                    mPrevTouchX = mTouchX;
//                    mPrevTouchY = mTouchY;
//                }
//                mTouchX = evt.getX();
//                mTouchY = evt.getY();
//                break;
//        }
//    }

    void handleClick(String data) {
        mInputConnection = getCurrentInputConnection();
        if ("DEL".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            if (mInputWord.length() > 0) {
                mInputWord.deleteCharAt(mInputWord.length() - 1);
            }

            if (mInputWord.length() == 0) {
                resetKeyboardLayout();
                mInputWord.setLength(0);
            } else {
                char c = mInputWord.charAt(mInputWord.length() - 1);
                if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                    mKeyboard.enlargeKeys(mTrie.find(mInputWord.toString()));
                } else {
                    resetKeyboardLayout();
                    mInputWord.setLength(0);
                }
            }
        } else if ("ENT".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            resetKeyboardLayout();
            mInputWord.setLength(0);
        } else if ("SPA".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
            resetKeyboardLayout();
            mInputWord.setLength(0);
        } else {
            char c = data.charAt(0);
            mInputConnection.commitText(data, 1);
            mInputWord.append(data);
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                // only case of alphabet
                mKeyboard.enlargeKeys(mTrie.find(mInputWord.toString()));
            } else {
                // case of symbols
                resetKeyboardLayout();
            }
        }
    }
}
