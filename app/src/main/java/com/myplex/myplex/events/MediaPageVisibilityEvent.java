package com.myplex.myplex.events;

/**
 * Created by Samir on 12/12/2015.
 */
public class MediaPageVisibilityEvent {
    private boolean isPlaying;

    public MediaPageVisibilityEvent(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isMediaPageVisible() {
        return isPlaying;
    }

    @Override
    public String toString() {
        return "isPlaying::" + isPlaying;
    }
}
