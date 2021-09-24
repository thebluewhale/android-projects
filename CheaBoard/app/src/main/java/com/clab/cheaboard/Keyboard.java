package com.clab.cheaboard;

import android.os.VibrationEffect;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintSet;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/** Controls the visible virtual keyboard view. */
abstract class Keyboard {

    CheaBoardService mCheaBoardService;
    int mViewResId;
    SparseArray<String> mKeyMapping;
    View mKeyboardView;
    LayoutInflater mLayoutInflater;
    DataBaseHelper mDataBaseHelper;
    int mState;
    Timer mLongPressTimer;
    int mLongPressTimerFlag;

    abstract boolean onSoftkeyTouch(View view, MotionEvent evt, int index, String data);
    abstract  void mapKeys();
    abstract void handleInputEvent(String data);
    abstract String getLabelFromRawString(String data);

    Keyboard(CheaBoardService cheaBoardService, int viewResId,
                     SparseArray<String> keyMapping) {
        mCheaBoardService = cheaBoardService;
        mViewResId = viewResId;
        mKeyMapping = keyMapping;
        mState = 0;
    }

    final View inflateKeyboardView(LayoutInflater inflater, InputView inputView) {
        mLayoutInflater = inflater;
        mKeyboardView = mLayoutInflater.inflate(mViewResId, inputView, false);
        mapKeys();
        return mKeyboardView;
    }

    final void initializeDataBaseHelper() {
        mDataBaseHelper = mCheaBoardService.getDataBaseHelper();
    }

    final boolean useDoubleSpaceToPeriod() {
        return mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_AUTO_PERIOD);
    }

    final int dpToPx(float dp) {
        float density = mCheaBoardService.getResources().getDisplayMetrics().density;
        return (int) Math.round(dp * density + 0.5);
    }

    final void setKeyPressColor(TextView softkey) {
        softkey.setBackgroundResource(R.drawable.softkey_shape_press);
    }

    final void setKeyHighlightColor(TextView softkey) {
        softkey.setBackgroundResource(R.drawable.softkey_shape_highlight);
    }

    final void resetKeyColor(TextView softkey) {
        softkey.setBackgroundResource(R.drawable.softkey_shape_normal);
    }

    final int getState() {
        return mState;
    }

    final void createTimer(String data) {
        if (!data.equals("DEL") ||
                !mDataBaseHelper.getSettingValue(Utils.SETTINGS_USE_BACKKEY_LONGPRESS)) {
            return;
        }
        mLongPressTimerFlag = 0;
        mLongPressTimer = new Timer();
        TimerTask mLongPressTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mLongPressTimerFlag > 10) {
                    handleInputEvent(data);
                }
                if (mLongPressTimerFlag % 2 == 1) {
                    handleInputEvent(data);
                }
                mLongPressTimerFlag++;
            }
        };
        mLongPressTimer.schedule(mLongPressTimerTask, Utils.LONGPRESS_TIMER_DELAY, Utils.LONGPRESS_TIMER_PERIOD);
    }

    final void terminateTimer() {
        if (mLongPressTimer != null) {
            mLongPressTimer.cancel();
        }
    }
}
