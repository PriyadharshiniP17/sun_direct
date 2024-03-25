package com.myplex.myplex.media;

import android.content.Context;
import android.util.AttributeSet;

public class VideoViewExtn extends VideoView {

	
	public int _overrideWidth = 240;
	public int _overrideHeight = 320;
	
    public VideoViewExtn(Context context) {
        super(context);
    }

    public VideoViewExtn(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoViewExtn(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void resizeVideo(int width, int height) {
        _overrideHeight = height;
        _overrideWidth = width;
        // not sure whether it is useful or not but safe to do so
        getHolder().setFixedSize(width, height);
        //getHolder().setSizeFromLayout();
        requestLayout();
        invalidate(); // very important, so that onMeasure will be triggered

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
    	int surfaceWidth = 1;
		int surfaceHeight = 1;
		surfaceWidth = resolveAdjustedSize(_overrideWidth, widthMeasureSpec);
		surfaceHeight = resolveAdjustedSize(_overrideHeight, heightMeasureSpec);
        setMeasuredDimension(_overrideWidth, _overrideHeight);
    }

}
