package com.clab.jankey;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class JanKeyService extends InputMethodService {
    private InputView mInputView = null;
    private Keyboard mKeyboard = null;
    private InputConnection mInputConnection;
    private boolean isCaps = false;
    private DataBaseHelper mDatabaseHelper;
    private CustomVariables mCustomVariables;

    @Override
    public View onCreateInputView() {
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
        if (mDatabaseHelper.getSettingValue(mCustomVariables.SETTINGS_USE_NUMBER_ROW)) {
            mKeyboard = Keyboard.jankey_num(this);
        } else {
            mKeyboard = Keyboard.jankey(this);
        }

        mInputView.addView(mKeyboard.inflateKeyboardView(LayoutInflater.from(this), mInputView));
    }

    public Vibrator getVibratorService() {
        return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    boolean checkDoubleSpaceToPeriod() {
        if ((mKeyboard.useDoubleSpaceToPeriod() == false)) {
            return false;
        }
        CharSequence lastText = mInputConnection.getTextBeforeCursor(1, 0);
        char last = lastText.length() > 0 ? lastText.charAt(0) : 'x';
        if (last != ' ') {
            return false;
        }
        mInputConnection.deleteSurroundingText(1, 0);
        mInputConnection.commitText(".", 1);
        return true;
    }

    void handleTouchDown(String data) {
        if (data.isEmpty()) return;

        mInputConnection = getCurrentInputConnection();
        if ("DEL".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        } else if ("ENT".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        } else if ("SPA".equals(data)) {
            if (checkDoubleSpaceToPeriod() == false) {
                mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
            }
        } else {
            char c = data.charAt(0);
            mInputConnection.commitText(data, 1);
        }
    }

    public DataBaseHelper getDataBaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DataBaseHelper(this);
        }
        return mDatabaseHelper;
    }
}
