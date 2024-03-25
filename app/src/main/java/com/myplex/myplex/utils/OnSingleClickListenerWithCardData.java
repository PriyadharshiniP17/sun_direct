package com.myplex.myplex.utils;

import android.view.View;

import com.myplex.model.CardData;
import com.myplex.myplex.model.ItemClickListenerWithData;

/**
 * Created by Raja Reddy  on 9/16/2019.
 */

public abstract class OnSingleClickListenerWithCardData implements ItemClickListenerWithData {
    private static final long MIN_CLICK_INTERVAL = 0;
    private long mLastClickTime;

    public abstract void onSingleClick(View v, int position, int parentPosition, CardData moviedata);

    @Override
    public void onClick(View view, int position, int parentPosition, CardData movieData) {
        /*long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        mLastClickTime = currentClickTime;

        if (elapsedTime <= MIN_CLICK_INTERVAL)
            return;
*/
        onSingleClick(view,position,parentPosition,movieData);

    }
}
