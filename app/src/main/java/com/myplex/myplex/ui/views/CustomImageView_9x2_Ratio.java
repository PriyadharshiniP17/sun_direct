package com.myplex.myplex.ui.views;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CustomImageView_9x2_Ratio extends androidx.appcompat.widget.AppCompatImageView {
    public CustomImageView_9x2_Ratio(Context context) {
        this(context, null);
    }

    public CustomImageView_9x2_Ratio(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView_9x2_Ratio(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        layoutParams.height = (int) (widthMeasureSpec * 2 / 9);
        setLayoutParams(layoutParams);
        invalidate();
    }
}
