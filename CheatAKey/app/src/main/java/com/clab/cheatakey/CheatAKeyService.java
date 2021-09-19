package com.clab.cheatakey;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class CheatAKeyService extends InputMethodService {
    private InputView mInputView = null;
    private KeyboardJanKey mKeyboardJanKey = null;
    private KeyboardJoKey mKeyboardJoKey = null;
    private InputConnection mInputConnection;
    private DataBaseHelper mDatabaseHelper;
    private CustomVariables mCustomVariables;
    private int mCurrentKeyboardType;
    private boolean mInitialized = false;

    @Override
    public View onCreateInputView() {
        mCustomVariables = new CustomVariables();
        mDatabaseHelper = new DataBaseHelper(this);
        mInputView = (InputView) LayoutInflater.from(this).inflate(R.layout.input_view, null);
        createKeyboardLayout();
        mInitialized = true;
        return mInputView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        resetKeyboardLayout();
    }

    private void resetKeyboardLayout() {
        if (mInitialized && mCurrentKeyboardType != getCurrentKeyboardType()) {
            createKeyboardLayout();
        }
    }

    private void createKeyboardLayout() {
        if (getCurrentKeyboardType() == mCustomVariables.SETTINGS_KEYBOARD_TYPE_JOKEY) {
            mCurrentKeyboardType = mCustomVariables.SETTINGS_KEYBOARD_TYPE_JOKEY;
            if (mDatabaseHelper.getBooleanSettingValue(mCustomVariables.SETTINGS_USE_NUMBER_ROW)) {
                mKeyboardJoKey = KeyboardJoKey.jokey_num(this);
            } else {
                mKeyboardJoKey = KeyboardJoKey.jokey(this);
            }
            mInputView.addView(mKeyboardJoKey.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        } else if (getCurrentKeyboardType() == mCustomVariables.SETTINGS_KEYBOARD_TYPE_JANKEY) {
            mCurrentKeyboardType = mCustomVariables.SETTINGS_KEYBOARD_TYPE_JANKEY;
            if (mDatabaseHelper.getBooleanSettingValue(mCustomVariables.SETTINGS_USE_NUMBER_ROW)) {
                mKeyboardJanKey = KeyboardJanKey.jankey_num(this);
            } else {
                mKeyboardJanKey = KeyboardJanKey.jankey(this);
            }
            mInputView.addView(mKeyboardJanKey.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        }
    }

    private int getCurrentKeyboardType() {
        return mDatabaseHelper.getIntegerSettingValue(mCustomVariables.SETTINGS_KEYBOARD_TYPE);
    }

    public Vibrator getVibratorService() {
        return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    boolean checkDoubleSpaceToPeriod() {
        if (!mDatabaseHelper.getBooleanSettingValue(mCustomVariables.SETTINGS_USE_AUTO_PERIOD)) {
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

    public DataBaseHelper getDataBaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DataBaseHelper(this);
        }
        return mDatabaseHelper;
    }

    public InputConnection getInputConnection() {
        return getCurrentInputConnection();
    }
}
