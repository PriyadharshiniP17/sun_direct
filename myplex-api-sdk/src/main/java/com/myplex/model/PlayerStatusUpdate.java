package com.myplex.model;

/**
 * Created by Apalya on 21-Dec-15.
 */
public interface PlayerStatusUpdate{
    public void playerStatusUpdate(String value);
    public void onViewChanged(boolean isMinimized);
    public void onCloseFragment();
}