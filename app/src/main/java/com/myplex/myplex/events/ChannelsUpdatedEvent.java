package com.myplex.myplex.events;

import com.myplex.myplex.model.ChannelItem;

import java.util.HashMap;

/**
 * Created by samir on 1/5/2016.
 */
public class ChannelsUpdatedEvent {
    private HashMap<Integer, ChannelItem> channelList;

    public ChannelsUpdatedEvent(HashMap<Integer, ChannelItem> data){
        this.channelList = data;

    }

    public HashMap<Integer, ChannelItem> getData(){
        return channelList;
    }

}


