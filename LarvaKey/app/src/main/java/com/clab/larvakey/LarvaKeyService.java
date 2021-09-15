package com.clab.larvakey;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class LarvaKeyService extends InputMethodService {
    private InputView mInputView = null;
    private Keyboard mKeyboard = null;
    private InputConnection mInputConnection;
    private boolean isCaps = false;
    private Trie mTrie = null;
    private StringBuilder mInputWord = new StringBuilder();
    private DataBaseHelper mDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mTrie = new Trie(this);
        mDatabaseHelper = new DataBaseHelper(this);
        mInputView = (InputView) LayoutInflater.from(this).inflate(R.layout.input_view, null);
        createKeyboardLayout();
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        resetKeyboardLayout();
        checkPreInputWord();
        enlargeKeysIfNeeded();
    }

    @Override
    public View onCreateInputView() {
        return mInputView;
    }

    private void resetKeyboardLayout() {
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
        if ((!mKeyboard.useDoubleSpaceToPeriod()) || (mInputWord.length() == 0) ||
                (!Character.toString(mInputWord.charAt(mInputWord.length() - 1)).equals(" "))) {
            return false;
        }
        mInputConnection.deleteSurroundingText(1, 0);
        mInputConnection.commitText(".", 1);
        mInputWord.deleteCharAt(mInputWord.length() - 1);
        mInputWord.append(".");
        return true;
    }

    public void handleTouchDown(String data) {
        mInputConnection = getCurrentInputConnection();
        if ("DEL".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            if (mInputWord.length() > 0) {
                mInputWord.deleteCharAt(mInputWord.length() - 1);
            }
        } else if ("ENT".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        } else if ("SPA".equals(data)) {
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
            mInputWord.append(" ");
        } else if ("SET".equals(data)) {
            // show settings page
            Intent intent = new Intent(this, SettingsMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if ("SHI".equals(data) || "SYM".equals(data)) {
            // do nothing
        } else {
            char c = data.charAt(0);
            mInputConnection.commitText(data, 1);
            mInputWord.append(c);
        }
    }

    public DataBaseHelper getDataBaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DataBaseHelper(this);
        }
        return mDatabaseHelper;
    }

    public void enlargeKeysIfNeeded() {
        int state = mKeyboard.getState();
        if ((state == Utils.STATE_SYMBOL) ||
                (state == Utils.STATE_SYMBOL + Utils.STATE_SHIFT)) {
            enlargeKeys("-");
            return;
        }
        if (mInputWord.length() == 0 || mInputWord.charAt(mInputWord.length() - 1) == ' ') {
            mKeyboard.resetKeys();
            return;
        }
        String[] splitWord = mInputWord.toString().split(" ");
        enlargeKeys(splitWord[splitWord.length - 1]);
    }

    private void enlargeKeys(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (Utils.isCharacterOrNumber(word.charAt(i)) == CHARACTER_TYPE.NUMBER ||
                    Utils.isCharacterOrNumber(word.charAt(i)) == CHARACTER_TYPE.SYMBOL) {
                mKeyboard.enlargeKeys(new int[26]);
                return;
            }
        }
        mKeyboard.enlargeKeys(mTrie.find(word));
    }

    private void checkPreInputWord() {
        mInputWord = new StringBuilder(getCurrentInputConnection().getTextBeforeCursor(100, 0).toString());
    }
}
