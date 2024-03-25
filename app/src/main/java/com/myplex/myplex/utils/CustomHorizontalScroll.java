package com.myplex.myplex.utils;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * Created by apalya on 5/8/2018.
 */

public class CustomHorizontalScroll extends LinearLayoutManager {
    public CustomHorizontalScroll(Context context) {
        super(context);
    }

    public CustomHorizontalScroll(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomHorizontalScroll(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}
