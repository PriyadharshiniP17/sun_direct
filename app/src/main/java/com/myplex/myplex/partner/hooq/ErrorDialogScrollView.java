/*
 * Copyright Quickplay Media Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of Quickplay Media Inc. and is protected by copyright
 * law. No license, implied or otherwise is granted by its use, unless licensed by Quickplay Media Inc.
 */
package com.myplex.myplex.partner.hooq;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ScrollView;

public class ErrorDialogScrollView extends ScrollView {
    public static final int maxHeight = 200; // 100dp

    public ErrorDialogScrollView(Context context) {
        super(context);
    }
    public ErrorDialogScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ErrorDialogScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(dpToPx(getResources(), maxHeight), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    private int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }
}
