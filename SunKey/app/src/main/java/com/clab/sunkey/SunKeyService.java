package com.clab.sunkey;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class SunKeyService extends InputMethodService {
    private InputView mInputView = null;
    private Keyboard mKeyboard = null;
    private InputConnection mInputConnection;
    private boolean isCaps = false;
    private StringBuilder mInputWord = new StringBuilder();
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
            mKeyboard = Keyboard.sunkey_num(this);
        } else {
            mKeyboard = Keyboard.sunkey(this);
        }

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

        mInputConnection = getCurrentInputConnection();
        if ("DEL".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            if (mInputWord.length() > 0) {
                mInputWord.deleteCharAt(mInputWord.length() - 1);
            }
        } else if ("ENT".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            mInputWord.append(" ");
        } else if ("SPA".equals(data)) {
            if (checkDoubleSpaceToPeriod() == false) {
                mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
                mInputWord.append(" ");
            }
            System.out.println("MYLOG | [" + mInputWord.toString() + "]");
        } else {
            char c = data.charAt(0);
            mInputConnection.commitText(data, 1);
            mInputWord.append(data);
        }
    }

    public DataBaseHelper getDataBaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DataBaseHelper(this);
        }
        return mDatabaseHelper;
    }
}
