package com.myplex.myplex.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by apalya on 3/10/2017.
 */

public class RobotoLightTypeFace extends androidx.appcompat.widget.AppCompatTextView {

    private static Typeface tf;

    public RobotoLightTypeFace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoLightTypeFace(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoLightTypeFace(Context context) {
        super(context);
        init();
    }

    private void init() {
        if(tf == null) {
            tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");
        }
        setTypeface(tf);
    }
}
