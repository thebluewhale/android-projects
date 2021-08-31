package com.example.mykeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.io.IOException;

public class MyKeyboardService extends InputMethodService {
    private InputView mInputView;
    private Keyboard mKeyboard;
    private InputConnection mInputConnection;
    private boolean isCaps = false;
    private Trie mTrie;
    private StringBuilder mInputWord = new StringBuilder();

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
            // reset keyboard layout.
            // Need to check this is right way.
            mKeyboard = Keyboard.qwerty(this);
            mInputView.addView(mKeyboard.inflateKeyboardView(LayoutInflater.from(this), mInputView));
            mKeyboard.reset();
        }
    }

    void handle(String data) {
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
