package com.clab.cheaboard;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class CheaBoardService extends InputMethodService {
    private InputView mInputView = null;
    private KeyboardEnglish mKeyboardEnglish;
    private KeyboardKorean mKeyboardKorean;
    private DataBaseHelper mDatabaseHelper;
    private int mKeyboardType;

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabaseHelper = new DataBaseHelper(this);
        mInputView = (InputView) LayoutInflater.from(this).inflate(R.layout.input_view, null);
        createKeyboardLayout(Utils.KEYBOARD_TYPE_ENGLISH);
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
//        reDrawKeyboard();
        createKeyboardLayout(mKeyboardType);
    }

    @Override
    public View onCreateInputView() {
        return mInputView;
    }

    private void reDrawKeyboard() {
        if (mKeyboardEnglish != null) {
            createKeyboardLayout(mKeyboardType);
//            mKeyboardEnglish.reDrawKeyboard();
        }
    }

    private void createKeyboardLayout(int type) {
        mKeyboardType = type;
        mInputView.removeAllViews();
        if (type == Utils.KEYBOARD_TYPE_ENGLISH) {
            mKeyboardEnglish = KeyboardEnglish.createQwertyKeyboard(this);
            mInputView.addView(mKeyboardEnglish.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        } else {
            mKeyboardKorean = mKeyboardKorean.createQwertyKeyboard(this);
            mInputView.addView(mKeyboardKorean.inflateKeyboardView(LayoutInflater.from(this), mInputView));
        }
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

    public void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void changeKeyboardType(int type) {
        createKeyboardLayout(type);
    }
}
