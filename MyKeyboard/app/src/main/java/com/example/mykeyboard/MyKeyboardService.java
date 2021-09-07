package com.example.mykeyboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
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
    private DataBaseHelper mDatabaseHelper;
    private CustomVariables mCustomVariables;

    @Override
    public View onCreateInputView() {
        mTrie = new Trie(this);
        mCustomVariables = new CustomVariables();
        mDatabaseHelper = new DataBaseHelper(this);
        mInputView = (InputView) LayoutInflater.from(this).inflate(R.layout.input_view, null);
        createKeyboardLayout();
        return mInputView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        resetKeyboardLayout();
    }

    void resetKeyboardLayout() {
        if (mKeyboard != null) {
            createKeyboardLayout();
            mKeyboard.reset();
        }
    }

    private void createKeyboardLayout() {
        mKeyboard = Keyboard.qwerty(this);
        mInputView.addView(mKeyboard.inflateKeyboardView(LayoutInflater.from(this), mInputView));
    }

    public Vibrator getVibratorService() {
        return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    boolean checkDoubleSpaceToPeriod() {
        if ((mKeyboard.useDoubleSpaceToPeriod() == false) ||
                (mInputWord.length() == 0) ||
                (!Character.toString(mInputWord.charAt(mInputWord.length() - 1)).equals(" "))) {
            return false;
        }
        mInputConnection.deleteSurroundingText(1, 0);
        mInputConnection.commitText(".", 1);
        mInputWord.deleteCharAt(mInputWord.length() - 1);
        mInputWord.append(".");
        return true;
    }

    void handleTouchDown(String data) {
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

    public DataBaseHelper getDataBaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DataBaseHelper(this);
        }
        return mDatabaseHelper;
    }
}
