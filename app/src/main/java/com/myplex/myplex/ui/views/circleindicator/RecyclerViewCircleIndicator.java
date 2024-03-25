package com.myplex.myplex.ui.views.circleindicator;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.util.AttributeSet;
import android.view.View;

public class RecyclerViewCircleIndicator extends BaseCircleIndicator {

    private RecyclerView mRecyclerView;
    private SnapHelper mSnapHelper;
    private int childCount = 0;

    public RecyclerViewCircleIndicator(Context context) {
        super(context);
    }

    public RecyclerViewCircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewCircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecyclerViewCircleIndicator(Context context, AttributeSet attrs, int defStyleAttr,
                            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView,
                                     @NonNull SnapHelper snapHelper) {
        mRecyclerView = recyclerView;
        mSnapHelper = snapHelper;
        mLastPosition = -1;
        createIndicators();
        recyclerView.removeOnScrollListener(mInternalOnScrollListener);
        recyclerView.addOnScrollListener(mInternalOnScrollListener);
    }

    private void createIndicators() {
        removeAllViews();
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        int count;
        count=adapter != null ? adapter.getItemCount() : 0;
        if (adapter == null || (count <= 0)) {
            return;
        }
        childCount = count;
        createIndicators(count, getSnapPosition(mRecyclerView.getLayoutManager()));
    }

    public int getSnapPosition(@Nullable RecyclerView.LayoutManager layoutManager) {
        if (layoutManager == null) {
            return RecyclerView.NO_POSITION;
        }
        View snapView = mSnapHelper.findSnapView(layoutManager);
        if (snapView == null) {
            return RecyclerView.NO_POSITION;
        }
        int currentPos = layoutManager.getPosition(snapView);
        if (childCount > 0) {
           return currentPos % childCount;
        } else {
            return layoutManager.getPosition(snapView);
        }
    }

    private final RecyclerView.OnScrollListener mInternalOnScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int position = getSnapPosition(recyclerView.getLayoutManager());
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    if (mLastPosition == position) {
                        return;
                    }
                    if(position > childCount){
                        createIndicators();
                    }
                    internalPageSelected(position);
                    mLastPosition = position;
                }
            };

    private final RecyclerView.AdapterDataObserver mAdapterDataObserver =
            new RecyclerView.AdapterDataObserver() {
                @Override public void onChanged() {
                    super.onChanged();
                    if (mRecyclerView == null) {
                        return;
                    }
                    RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
                    int newCount = adapter != null ? adapter.getItemCount() : 0;
                    int currentCount = getChildCount();
                    if (newCount == currentCount) {
                        // No change
                        return;
                    } else if (mLastPosition < newCount) {
                        mLastPosition = getSnapPosition(mRecyclerView.getLayoutManager());
                    } else {
                        mLastPosition = RecyclerView.NO_POSITION;
                    }
                    createIndicators();
                }

                @Override public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    onChanged();
                }

                @Override public void onItemRangeChanged(int positionStart, int itemCount,
                                                         @Nullable Object payload) {
                    super.onItemRangeChanged(positionStart, itemCount, payload);
                    onChanged();
                }

                @Override public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    onChanged();
                }

                @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    onChanged();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    onChanged();
                }
            };

    public RecyclerView.AdapterDataObserver getAdapterDataObserver() {
        return mAdapterDataObserver;
    }
}
