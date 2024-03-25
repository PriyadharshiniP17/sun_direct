package com.myplex.myplex.events;

/**
 * Created by Samir on 12/12/2015.
 */
public class EventNetworkConnectionChange {
    public boolean isConnected;
    public EventNetworkConnectionChange(boolean isConnected){
        this.isConnected = isConnected;
    }
    public boolean isConnected(){
        return isConnected;
    }
}
