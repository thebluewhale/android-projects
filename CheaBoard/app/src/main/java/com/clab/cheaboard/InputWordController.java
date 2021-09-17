package com.clab.cheaboard;

import android.renderscript.ScriptGroup;

public class InputWordController {
    private static InputWordController instance;
    private StringBuilder mInputWord;

    private InputWordController() {
        mInputWord = new StringBuilder();
    }

    public static InputWordController get() {
        if (instance == null) {
            instance = new InputWordController();
        }
        return instance;
    }

    public void resetWord(String newString) {
        mInputWord = new StringBuilder(newString);
    }

    public String getWord() {
        return mInputWord.toString();
    }

    public void appendWord(char c) {
        mInputWord.append(c);
    }

    public void deleteLastWord() {
        if (mInputWord.length() > 0) {
            mInputWord.deleteCharAt(mInputWord.length() - 1);
        }
    }

    public char getLastChar() {
        return mInputWord.charAt(mInputWord.length() - 1);
    }

    public int getLength() {
        return mInputWord.length();
    }
}
