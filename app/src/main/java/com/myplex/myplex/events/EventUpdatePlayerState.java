package com.myplex.myplex.events;

/**
 * Created by Srikanth on 28-Aug-17.
 */

public class EventUpdatePlayerState {
    public static final int ACTION_PLAY = 0;
    public static final int ACTION_PAUSE = 1;
    public int action;

    public EventUpdatePlayerState(int action) {
        this.action = action;
    }
}
