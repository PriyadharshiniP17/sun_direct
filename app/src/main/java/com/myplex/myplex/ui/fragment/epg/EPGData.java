package com.myplex.myplex.ui.fragment.epg;

import java.util.List;
import java.util.Map;

/**
 * Interface to implement and pass to EPG containing data to be used.
 * Implementation can be a simple as simple as a Map/List or maybe an Adapter.
 * Created by Kristoffer on 15-05-23.
 */
public interface EPGData {

    EPG.EPGChannel getEPGChannel(int position);

    List<EPG.EPGProgram> getEPGPrograms(int channelPosition);

    EPG.EPGProgram getEPGProgram(int channelPosition, int programPosition);

    int getChannelCount();

    boolean hasData();

    public void updateData(Map<EPG.EPGChannel, List<EPG.EPGProgram>> data);
}
