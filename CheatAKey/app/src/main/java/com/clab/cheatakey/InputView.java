package com.clab.cheatakey;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/** The root view for the Input Method. */
public final class InputView extends FrameLayout {

    // If true, this InputView will simulate Gboard's InputView behavior, which expands its
    // region to the entire window regardless of its content view's size.
    private static final boolean EXPAND_TO_WINDOW = false;

    public InputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (EXPAND_TO_WINDOW && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getMeasuredWidth(), MeasureSpec.getSize(heightMeasureSpec));
        }
    }
}
