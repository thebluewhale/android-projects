package com.clab.cheaboard;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.Arrays;
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
    String[] mImageViewIdArray = {"SET", "SHI", "DEL", "ENT", "SPA"};
    ArrayList<String> mImageViewList = new ArrayList<>(Arrays.asList(mImageViewIdArray));

    abstract boolean onTextViewTouch(View view, MotionEvent evt, int index, String data);
    abstract boolean onImageViewTouch(View view, MotionEvent evt, int index, String data);
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

    final void setKeyPressColor(View softkey) {
        softkey.setPressed(true);
    }

    final void setKeyHighlightColor(View softkey) {
        softkey.setBackgroundResource(R.drawable.softkey_shape_highlight);
    }

    final void resetKeyColor(View softkey) {
        softkey.setBackgroundResource(R.drawable.softkey_shape_normal);
        softkey.setPressed(false);
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
