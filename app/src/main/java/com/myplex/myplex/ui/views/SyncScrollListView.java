package com.myplex.myplex.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samir on 12/19/2015.
 */
public class SyncScrollListView extends ListView implements ListScrollManager.ScrollNotifier {


    private List<OnScrollListener> listenerList = new ArrayList<>(2);

    public SyncScrollListView(Context context) {
        super(context);
        init();
    }


    public SyncScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SyncScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    OnScrollListener onScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            for (OnScrollListener listener : listenerList) {
                listener.onScrollStateChanged(absListView, i);
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            for (OnScrollListener listener : listenerList) {
                listener.onScroll(absListView, i, i1, i2);
            }
        }
    };


    private void init() {
        super.setOnScrollListener(onScrollListener);
    }

    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        addScrollListener(onScrollListener);
    }

    @Override
    public void addScrollListener(OnScrollListener scrollListener) {
        listenerList.add(scrollListener);
    }

    @Override
    public OnScrollListener getScrollListener() {
        return null;
    }

    public void removeScrollListener(){
        listenerList.clear();
    }
}
