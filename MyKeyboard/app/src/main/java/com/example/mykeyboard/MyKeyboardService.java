package com.example.mykeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class MyKeyboardService extends InputMethodService {
    private InputView mInputView;
    private Keyboard mKeyboard;
    private InputConnection mInputConnection;
    private boolean isCaps = false;

    @Override
    public View onCreateInputView() {
        mInputView = (InputView) LayoutInflater.from(this).inflate(R.layout.input_view, null);
        mKeyboard = Keyboard.qwerty(this);
        mInputView.addView(mKeyboard.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        return mInputView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        if (mKeyboard != null) {
            // reset keyboard layout.
            // Need to check this is right way.
            mInputView.addView(mKeyboard.inflateKeyboardView(LayoutInflater.from(this), mInputView));
            mKeyboard.reset();
        }
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
    }

    void handle(String data) {
        mInputConnection = getCurrentInputConnection();
        if ("DEL".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        } else if ("END".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        } else if ("SPA".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        } else {
            mInputConnection.commitText(data, 1);
        }
    }
}
