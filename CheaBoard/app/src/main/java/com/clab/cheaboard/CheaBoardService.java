package com.clab.cheaboard;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class CheaBoardService extends InputMethodService {
    private InputView mInputView = null;
    private Keyboard mKeyboard = null;
    private DataBaseHelper mDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabaseHelper = new DataBaseHelper(this);
        mInputView = (InputView) LayoutInflater.from(this).inflate(R.layout.input_view, null);
        createKeyboardLayout();
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        reDrawKeyboard();
    }

    @Override
    public View onCreateInputView() {
        return mInputView;
    }

    private void reDrawKeyboard() {
        if (mKeyboard != null) {
            createKeyboardLayout();
            mKeyboard.reDrawKeyboard();
        }
    }

    private void createKeyboardLayout() {
        mKeyboard = Keyboard.createQwertyKeyboard(this);
        mInputView.addView(mKeyboard.inflateKeyboardView(LayoutInflater.from(this), mInputView));
    }

    public Vibrator getVibratorService() {
        return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

//    boolean checkDoubleSpaceToPeriod() {
//        if ((!mKeyboard.useDoubleSpaceToPeriod()) || (mInputWord.length() == 0) ||
//                (!Character.toString(mInputWord.charAt(mInputWord.length() - 1)).equals(" "))) {
//            return false;
//        }
//        mInputConnection.deleteSurroundingText(1, 0);
//        mInputConnection.commitText(".", 1);
//        mInputWord.deleteCharAt(mInputWord.length() - 1);
//        mInputWord.append(".");
//        return true;
//    }

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
}
