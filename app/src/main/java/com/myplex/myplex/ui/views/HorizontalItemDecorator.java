package com.myplex.myplex.ui.views;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
public class HorizontalItemDecorator extends RecyclerView.ItemDecoration {

    private final int spaceLeftWidth;
    private final int spaceRightWidth;

    public HorizontalItemDecorator(int horizontalSpaceHeight) {
        this.spaceLeftWidth = horizontalSpaceHeight;
        this.spaceRightWidth = horizontalSpaceHeight;
    }
    public HorizontalItemDecorator(int spaceLeftWidth, int spaceRightWidth) {
        this.spaceLeftWidth = spaceLeftWidth;
        this.spaceRightWidth = spaceRightWidth;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.left = spaceLeftWidth;
        outRect.right = spaceRightWidth;
    }
}
