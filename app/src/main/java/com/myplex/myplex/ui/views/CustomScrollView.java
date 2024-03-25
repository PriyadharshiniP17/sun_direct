package com.myplex.myplex.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView {
	private ScrollingDirection mDirectionListener;

    public interface ScrollingDirection {
        public void scrollDirection(boolean value);
    }


    private boolean mScrollable = true;

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }
    public boolean isScrollable() {
        return mScrollable;
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (mScrollable) return super.onTouchEvent(ev);
                // only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if 
        // we are not scrollable
        if (!mScrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }

    
    
    
    
    

	public void setDirectionListener(ScrollingDirection mListener){
		this.mDirectionListener = mListener;
	}
	public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CustomScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	private int mLastPosition = 0;
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mDirectionListener != null){
			mDirectionListener.scrollDirection((mLastPosition >= oldt)?true:false);
		}	
		mLastPosition = oldt;
	}
	

}
