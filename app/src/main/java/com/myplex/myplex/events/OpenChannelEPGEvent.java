package com.myplex.myplex.events;

/**
 * Created by Samir on 12/12/2015.
 */
public class OpenChannelEPGEvent {
    private int position;
    public OpenChannelEPGEvent(int position){
        this.position = position;

    }
    public int getPosition(){
        return position;
    }


}
