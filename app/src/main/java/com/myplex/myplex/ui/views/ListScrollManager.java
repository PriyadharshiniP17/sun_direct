package com.myplex.myplex.ui.views;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Samir on 12/19/2015.
 */
public class ListScrollManager {

    private static ListScrollManager _self = new ListScrollManager();

    private final boolean ENABLE_SYNC_SCROLL = true;

    public static ListScrollManager getInstance() {
        if (_self == null) {
            _self = new ListScrollManager();
        }
        return _self;
    }

    public interface ScrollNotifier {

        public void addScrollListener(AbsListView.OnScrollListener scrollListener);

        public AbsListView.OnScrollListener getScrollListener();

        public void removeScrollListener();
    }

    private static final int SCROLL_HORIZONTAL = 1;
    private static final int SCROLL_VERTICAL = 2;

    private ArrayList<WeakReference<ScrollNotifier>> clients = new ArrayList(10);

    private volatile boolean isSyncing = false;
    private int scrollType = SCROLL_HORIZONTAL;

    public void addScrollClient(ScrollNotifier client) {
        if (!ENABLE_SYNC_SCROLL) return;
        if(hasScrollClient(client)) return;
        if(client == null) return;
        clients.add(new WeakReference<ScrollNotifier>(client));
        client.addScrollListener(scrollListener);
    }

    public boolean hasScrollClient (ScrollNotifier client){
        for (WeakReference weakReference : clients) {
            if (weakReference.get() == client) {
                return true;
            }
        }
        return false;
    }

    public void removeScrollClient(ScrollNotifier client) {
        for (WeakReference weakReference : clients) {
            if (weakReference.get() == client) {
                clients.remove(weakReference);
                client.removeScrollListener();
                break;
            }
        }
    }

    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {


        int firstVisibleItem = 0;

        int y = 0;

        @Override
        public void onScrollStateChanged(AbsListView sender, int scrollState) {


        }

        @Override
        public void onScroll(AbsListView sender, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (!ENABLE_SYNC_SCROLL) return;

           // if (scrollState != SCROLL_STATE_IDLE) return;

            if (isSyncing)
                return;

            if (sender == null || sender.getChildCount() <= 0 ||
                    (y == sender.getChildAt(0).getTop() && firstVisibleItem == sender.getFirstVisiblePosition())) {
                return;
            }

            isSyncing = true;

            firstVisibleItem = sender.getFirstVisiblePosition();
            y = sender.getChildAt(0).getTop();

            for (WeakReference<ScrollNotifier> clientWeakRef : clients) {
                ScrollNotifier client = clientWeakRef.get();
                if (client == null) continue;
                View view = (View) client;
                // don't update sender
                if (view == sender)
                    continue;


                // scroll relevant views only

                ((ListView) view).setSelectionFromTop(firstVisibleItem, y);
            }
            isSyncing = false;
        }


    };
}
