package com.myplex.myplex.events;

/**
 * Created by Srikanth on 28-Aug-17.
 */

public class DownloadDataLoaded {
    public int type;
    public boolean isEmpty;

    public DownloadDataLoaded(int currentFragment, boolean b) {
        this.type = currentFragment;
        this.isEmpty = b;
    }
}
