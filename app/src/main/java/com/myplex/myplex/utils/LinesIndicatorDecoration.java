package com.myplex.myplex.utils;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager;
import com.jackandphantom.carouselrecyclerview.CarouselRecyclerview;

import org.jetbrains.annotations.NotNull;

public class LinesIndicatorDecoration extends RecyclerView.ItemDecoration {

    private final int indicatorHeight;
    private final int indicatorItemPadding;
    private final int radius;
    private final int inactive_radius;
    private static final float DP = Resources.getSystem().getDisplayMetrics().density;
    private final float mIndicatorItemLength =  10;
    private final float mIndicatorItemPadding = DP * 12;


    private final Paint inactivePaint = new Paint();
    private final Paint activePaint = new Paint();
    public LinesIndicatorDecoration(int radius, int padding, int indicatorHeight, @ColorInt int colorInactive, @ColorInt int colorActive, int inactiveRadius) {
        float strokeWidth = Resources.getSystem().getDisplayMetrics().density * 5;
        this.radius = radius;
        this.inactive_radius = inactiveRadius;
        inactivePaint.setStrokeCap(Paint.Cap.ROUND);
        inactivePaint.setStrokeWidth(strokeWidth);
        inactivePaint.setStyle(Paint.Style.STROKE);
        inactivePaint.setAntiAlias(true);
        inactivePaint.setColor(colorInactive);

        activePaint.setStrokeCap(Paint.Cap.ROUND);
        activePaint.setStrokeWidth(strokeWidth);
        activePaint.setStyle(Paint.Style.FILL);
        activePaint.setAntiAlias(true);
        activePaint.setColor(colorActive);

        this.indicatorItemPadding = padding;
        this.indicatorHeight = indicatorHeight;
    }

    @Override
    public void onDrawOver(@NotNull Canvas c, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        final RecyclerView.Adapter adapter = parent.getAdapter();
        Log.d("LinesIndicator", "onDrawOver: adapter : "+ adapter);

        if (adapter == null) {
            return;
        }

        int itemCount = adapter.getItemCount();
        float totalLength = mIndicatorItemLength * itemCount;
        Log.d("LinesIndicator", "onDrawOver: totalLength "+ totalLength + " itemCount "+itemCount);
        // center horizontally, calculate width and subtract half from center
        // float totalLength = this.radius * 2 * itemCount;
        //  float paddingBetweenItems = Math.max(0, itemCount - 1) * mIndicatorItemPadding;
        float paddingBetweenItems = Math.max(0, itemCount - 1) * mIndicatorItemPadding;
        float indicatorTotalWidth = totalLength + paddingBetweenItems;
        float indicatorStartX = (parent.getWidth() - indicatorTotalWidth) / 1.15f;

        // center vertically in the allotted space
        float indicatorPosY = parent.getHeight() - indicatorHeight ;

        drawInactiveDots(c, indicatorStartX, indicatorPosY, itemCount);

        final int activePosition;

        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            activePosition = ((GridLayoutManager) parent.getLayoutManager()).findFirstVisibleItemPosition();
        } else if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            activePosition = ((LinearLayoutManager) parent.getLayoutManager()).findFirstVisibleItemPosition();
        }else if (parent.getLayoutManager() instanceof CarouselLayoutManager) {
            CarouselLayoutManager layoutManager = ((CarouselLayoutManager) parent.getLayoutManager());
            int position = layoutManager.centerPosition();
            //for infinite loop center position is going beyond childcount hence take modulus of it
//            position = (position)%layoutManager.getItemCount();
            activePosition = Math.abs(position);
        } else {
            // not supported layout manager
            Log.d("LinesIndicator", "onDrawOver: not supported layout manager ");
            return;
        }

        if (activePosition == RecyclerView.NO_POSITION) {
            Log.d("LinesIndicator", "onDrawOver: RecyclerView.NO_POSITION ");
            return;
        }
        Log.d("LinesIndicator", "onDrawOver: activePosition "+ activePosition);

        // find offset of active page if the user is scrolling
        final View activeChild = parent.getLayoutManager().findViewByPosition(activePosition);
        if (activeChild == null) {
            Log.d("LinesIndicator", "onDrawOver: activeChild "+ activeChild);
            return;
        }

        drawActiveDot(c, indicatorStartX, indicatorPosY, activePosition);
    }

    private void drawInactiveDots(Canvas c, float indicatorStartX, float indicatorPosY, int itemCount) {
        // width of item indicator including padding
        //  final float itemWidth = this.radius * 2 + indicatorItemPadding;
        // width of item indicator including padding
        final float itemWidth = mIndicatorItemLength + mIndicatorItemPadding;

        float start = indicatorStartX ;
        for (int i = 0; i < itemCount; i++) {
            //  c.drawCircle(start, indicatorPosY, inactive_radius, inactivePaint);
            c.drawLine(start, indicatorPosY, start + mIndicatorItemLength+5, indicatorPosY, inactivePaint);
            start += itemWidth;
        }
    }

    private void drawActiveDot(Canvas c, float indicatorStartX, float indicatorPosY,
                               int highlightPosition) {
        // width of item indicator including padding
        // final float itemWidth = this.radius * 2 + indicatorItemPadding;
        final float itemWidth = mIndicatorItemLength + mIndicatorItemPadding;
        float highlightStart = indicatorStartX +  itemWidth * highlightPosition;
        //  c.drawCircle(highlightStart, indicatorPosY, radius, activePaint);
        c.drawLine(highlightStart, indicatorPosY, highlightStart + mIndicatorItemLength+5, indicatorPosY, activePaint);
    }

    @Override
    public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = -indicatorHeight;
    }
}
