package com.myplex.myplex.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotoMediumTextView extends TextView {
    private static Typeface tf;

    public RobotoMediumTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoMediumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoMediumTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if(tf == null) {
             tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Medium.ttf");
        }
        setTypeface(tf);
    }

}