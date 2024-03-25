package com.myplex.myplex.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by apalya on 3/10/2017.
 */

public class FontBoldTypeFace extends TextView {

    private static Typeface tf;

    public FontBoldTypeFace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FontBoldTypeFace(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FontBoldTypeFace(Context context) {
        super(context);
        init();
    }

    private void init() {
        if(tf == null) {
            tf = Typeface.createFromAsset(getContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
        }
        setTypeface(tf);
    }
}
