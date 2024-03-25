package com.myplex.myplex.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.myplex.myplex.R;


public class VerticalLineRelativeLayout extends RelativeLayout {
	private Paint paint = new Paint();
	public VerticalLineRelativeLayout(Context context, AttributeSet attrs,
                                      int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		createPaint();
		// TODO Auto-generated constructor stub
	}

	public VerticalLineRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		createPaint();
		// TODO Auto-generated constructor stub
	}

	public VerticalLineRelativeLayout(Context context) {
		super(context);
		setWillNotDraw(false);
		createPaint();
		// TODO Auto-generated constructor stub
	}
	private void createPaint(){
		paint.setColor(Color.parseColor("#b4b4b5"));
		paint.setStrokeWidth(2f);
		paint.setStyle(Paint.Style.FILL);
	}
	Rect mRect = new Rect();
	int mStartX;
	@Override
	protected void onDraw(Canvas canvas) {
		if(mRect.isEmpty()){
			mStartX = getPaddingLeft()+(int)getContext().getResources().getDimension(R.dimen.margin_gap_4);
			View child = getChildAt(0);
			child.getGlobalVisibleRect(mRect);
		}
		canvas.drawLine((mStartX+(mRect.width()/2)), mRect.height(), (mStartX+(mRect.width()/2)), getHeight(), paint);
		super.onDraw(canvas);
	}

}
