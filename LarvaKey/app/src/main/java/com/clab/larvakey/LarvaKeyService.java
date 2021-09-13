package com.clab.larvakey;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class LarvaKeyService extends InputMethodService {
    private enum CHARACTER_TYPE {
        ALPHABET_LOWERCASE, ALPHABET_UPPERCASE, NUMBER, SYMBOL
    }
    private InputView mInputView = null;
    private Keyboard mKeyboard = null;
    private InputConnection mInputConnection;
    private boolean isCaps = false;
    private Trie mTrie = null;
    private StringBuilder mInputWord = new StringBuilder();
    private DataBaseHelper mDatabaseHelper;
    private CustomVariables mCustomVariables;

    @Override
    public void onCreate() {
        super.onCreate();
        mTrie = new Trie(this);
        mCustomVariables = new CustomVariables();
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
        if ((state == mCustomVariables.STATE_SYMBOL) ||
                (state == mCustomVariables.STATE_SYMBOL + mCustomVariables.STATE_SHIFT)) {
            enlargeKeys("-");
            return;
        }
        if (mInputWord.length() == 0 || mInputWord.charAt(mInputWord.length() - 1) == ' ') {
            resetKeyboardLayout();
            return;
        }
        String[] splitWord = mInputWord.toString().split(" ");
        enlargeKeys(splitWord[splitWord.length - 1]);
    }

    private void enlargeKeys(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (isCharacterOrNumber(word.charAt(i)) == CHARACTER_TYPE.NUMBER ||
                    isCharacterOrNumber(word.charAt(i)) == CHARACTER_TYPE.SYMBOL) {
                mKeyboard.enlargeKeys(new int[26]);
                return;
            }
        }
        mKeyboard.enlargeKeys(mTrie.find(word));
    }

    private CHARACTER_TYPE isCharacterOrNumber(char c) {
        if (c >= 'a' && c <= 'z') {
            return CHARACTER_TYPE.ALPHABET_LOWERCASE;
        } else if (c >= 'A' && c <= 'Z') {
            return CHARACTER_TYPE.ALPHABET_UPPERCASE;
        } else if (c >= '0' && c <= '9') {
            return CHARACTER_TYPE.NUMBER;
        } else {
            return CHARACTER_TYPE.SYMBOL;
        }
    }

    private void checkPreInputWord() {
        mInputWord = new StringBuilder(getCurrentInputConnection().getTextBeforeCursor(100, 0).toString());
    }
}
