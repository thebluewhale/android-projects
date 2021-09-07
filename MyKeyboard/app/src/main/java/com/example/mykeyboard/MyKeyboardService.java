package com.example.mykeyboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.provider.ContactsContract;
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
import java.util.HashMap;

public class MyKeyboardService extends InputMethodService {
    private InputView mInputView = null;
    private Keyboard mKeyboard = null;
    private InputConnection mInputConnection;
    private boolean isCaps = false;
//    private Trie mTrie = null;
    private StringBuilder mInputWord = new StringBuilder();
    private VelocityTracker mVelocityTracker = null;

    @Override
    public View onCreateInputView() {
//        mTrie = new Trie(this);
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
        mKeyboard = Keyboard.cheatakey(this);
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
        if (data.length() == 0) return;

        if ("LAUNCH_SETTINGS".equals(data)) {
            Intent settingsIntent = new Intent(Intent.ACTION_MAIN);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName componentName = new ComponentName("com.example.mykeyboard", "com.example.mykeybaord.SettingsMain");
            settingsIntent.setComponent(componentName);
            startActivity(settingsIntent);
            return;
        }

        mInputConnection = getCurrentInputConnection();
        if ("DEL".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            if (mInputWord.length() > 0) {
                mInputWord.deleteCharAt(mInputWord.length() - 1);
            }
//            if (mInputWord.length() == 0) {
//                resetKeyboardLayout();
//                mInputWord.setLength(0);
//            } else {
//                char c = mInputWord.charAt(mInputWord.length() - 1);
//                if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
//                    mKeyboard.enlargeKeys(mTrie.find(mInputWord.toString()));
//                } else {
//                    resetKeyboardLayout();
//                    mInputWord.setLength(0);
//                }
//            }
        } else if ("ENT".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            mInputWord.append(" ");
//            resetKeyboardLayout();
//            mInputWord.setLength(0);
        } else if ("SPA".equals(data)) {
            if (checkDoubleSpaceToPeriod() == false) {
                mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
                mInputWord.append(" ");
//            resetKeyboardLayout();
//            mInputWord.setLength(0);
            }
            System.out.println("MYLOG | [" + mInputWord.toString() + "]");
        } else {
            char c = data.charAt(0);
            mInputConnection.commitText(data, 1);
            mInputWord.append(data);
//            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                // only case of alphabet
//                mKeyboard.enlargeKeys(mTrie.find(mInputWord.toString()));
//            } else {
                // case of symbols
//                resetKeyboardLayout();
//            }
        }
    }
}
