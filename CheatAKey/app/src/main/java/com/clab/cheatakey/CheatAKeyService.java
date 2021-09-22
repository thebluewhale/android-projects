package com.clab.cheatakey;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class CheatAKeyService extends InputMethodService {
    private InputView mInputView = null;
    private DataBaseHelper mDatabaseHelper;
    private int mCurrentKeyboardType;
    private boolean mInitialized = false;

    @Override
    public View onCreateInputView() {
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
        KeyboardJanKey mKeyboardJanKey;
        KeyboardPrinKey mKeyboardPrinKey;
        KeyboardJoKey mKeyboardJoKey;
        KeyboardSunKey mKeyboardSunKey;
        if (getCurrentKeyboardType() == Utils.SETTINGS_KEYBOARD_TYPE_PRINKEY) {
            System.out.println("MYLOG | Create PrinKey");
            mCurrentKeyboardType = Utils.SETTINGS_KEYBOARD_TYPE_PRINKEY;
            if (mDatabaseHelper.getBooleanSettingValue(Utils.SETTINGS_USE_NUMBER_ROW)) {
                mKeyboardPrinKey = KeyboardPrinKey.prinkey_num(this);
            } else {
                mKeyboardPrinKey = KeyboardPrinKey.prinkey(this);
            }
            mInputView.addView(mKeyboardPrinKey.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        } else if (getCurrentKeyboardType() == Utils.SETTINGS_KEYBOARD_TYPE_JANKEY) {
            System.out.println("MYLOG | Create JanKey");
            mCurrentKeyboardType = Utils.SETTINGS_KEYBOARD_TYPE_JANKEY;
            if (mDatabaseHelper.getBooleanSettingValue(Utils.SETTINGS_USE_NUMBER_ROW)) {
                mKeyboardJanKey = KeyboardJanKey.jankey_num(this);
            } else {
                mKeyboardJanKey = KeyboardJanKey.jankey(this);
            }
            mInputView.addView(mKeyboardJanKey.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        } else if (getCurrentKeyboardType() == Utils.SETTINGS_KEYBOARD_TYPE_SUNKEY) {
            System.out.println("MYLOG | Create SunKey");
            mCurrentKeyboardType = Utils.SETTINGS_KEYBOARD_TYPE_SUNKEY;
            if (mDatabaseHelper.getBooleanSettingValue(Utils.SETTINGS_USE_NUMBER_ROW)) {
                mKeyboardSunKey = KeyboardSunKey.sunkey_num(this);
            } else {
                mKeyboardSunKey = KeyboardSunKey.sunkey(this);
            }
            mInputView.addView(mKeyboardSunKey.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        } else {
            // getCurrentKeyboardType() == Utils.SETTINGS_KEYBOARD_TYPE_JOKEY
            System.out.println("MYLOG | Create JoKey");
            mCurrentKeyboardType = Utils.SETTINGS_KEYBOARD_TYPE_JOKEY;
            if (mDatabaseHelper.getBooleanSettingValue(Utils.SETTINGS_USE_NUMBER_ROW)) {
                mKeyboardJoKey = KeyboardJoKey.jokey_num(this);
            } else {
                mKeyboardJoKey = KeyboardJoKey.jokey(this);
            }
            mInputView.addView(mKeyboardJoKey.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        }
    }

    private int getCurrentKeyboardType() {
        return mDatabaseHelper.getIntegerSettingValue(Utils.SETTINGS_KEYBOARD_TYPE);
    }

    public Vibrator getVibratorService() {
        return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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
