package com.myplex.myplex.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.myplex.myplex.R;

public class StrikedTextView extends TextView {
    private int mColor;
    private Paint paint;

    public StrikedTextView(Context context) {
        super(context);
        init(context);
    }

    public StrikedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StrikedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        //Color
        mColor = resources.getColor(R.color.red_highlight_color);

        paint = new Paint();
        paint.setColor(mColor);
        //Width
        paint.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        float width = getWidth();
//        float heigh = getHeight();
//        canvas.drawLine(width/10, heigh/10, (width-width/10),(heigh-heigh/10), paint);
        canvas.drawLine(0, getHeight()/2, getWidth(), getHeight()/2, paint);
    }
}