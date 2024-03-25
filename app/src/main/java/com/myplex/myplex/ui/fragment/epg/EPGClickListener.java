package com.myplex.myplex.ui.fragment.epg;

/**
 * Created by Kristoffer on 15-05-25.
 */
public interface EPGClickListener {

    void onChannelClicked(int channelPosition, EPG.EPGChannel epgChannel,int programPosition, String channel_id,EPG.EPGProgram epgEvent);

   // void onEventClicked(int channelPosition, int programPosition, EPG.EPGProgram epgEvent);
   void onEventClicked(int channelPosition, int programPosition, String channel_id,EPG.EPGProgram epgEvent);

    void onChannelClicked(int channelPosition, EPG.EPGChannel epgChannel);

    void onResetButtonClicked();

    void updateScroll(int value);

    void loadMore();

    void showToolTip();
}
