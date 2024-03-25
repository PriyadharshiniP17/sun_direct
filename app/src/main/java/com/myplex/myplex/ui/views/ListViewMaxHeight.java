package com.myplex.myplex.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ListView;

import com.myplex.myplex.R;

public class ListViewMaxHeight extends ListView {

    private final float maxHeight;

    public ListViewMaxHeight(Context context) {
        this(context, null);
    }

    public ListViewMaxHeight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListViewMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            //Retrieve styles attributes
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListViewMaxHeight, defStyleAttr, 0);
            maxHeight = a.getDimension(R.styleable.TitlePageIndicator_footerLineHeight, getContext().getResources().getDimension(R.dimen.margin_gap_192));
            a.recycle();
        } else {
            maxHeight = 0;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(MeasureSpec.getSize(heightMeasureSpec) > maxHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) maxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}