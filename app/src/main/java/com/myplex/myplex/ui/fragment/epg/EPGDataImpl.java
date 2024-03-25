package com.myplex.myplex.ui.fragment.epg;


import android.util.Log;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add null check when fetching at position etc.
 * Created by Kristoffer on 15-05-23.
 */
public class EPGDataImpl implements EPGData {

    private List<EPG.EPGChannel> channels = Lists.newArrayList();
    private List<List<EPG.EPGProgram>> events = Lists.newArrayList();
    private Map<EPG.EPGChannel, List<EPG.EPGProgram>> data = new HashMap<EPG.EPGChannel, List<EPG.EPGProgram>>();

    public EPGDataImpl(Map<EPG.EPGChannel, List<EPG.EPGProgram>> data) {
        this.data =  data;
        channels = Lists.newArrayList(data.keySet());
        events = Lists.newArrayList(data.values());
        Log.e("EPGDataImpl","events "+events.size());
        Log.e("EPGDataImpl","channels "+channels.size());
    }

    public void updateData(Map<EPG.EPGChannel, List<EPG.EPGProgram>> data){
        if(this.data != null)
            this.data.clear();
        if(channels != null)
            channels.clear();

        if(events !=null)
            events.clear();

        this.data =  data;
        channels = Lists.newArrayList(data.keySet());
        events = Lists.newArrayList(data.values());
        Log.e("updateData","events "+events.size());
        Log.e("updateData","channels "+channels.size());
    }

    public EPG.EPGChannel getEPGChannel(int position) {
//        Log.e("Divya","channel count ++++++++"+channels.size() + " "+position);
        return position < channels.size() ? channels.get(position) : null;
    }

    public List<EPG.EPGProgram> getEPGPrograms(int channelPosition) {
//        Log.e("Divya","++++++++"+events.size());
        return ( channelPosition >= 0 && channelPosition < events.size() ) ? events.get(channelPosition) :  new ArrayList<>();
    }

    public EPG.EPGProgram getEPGProgram(int channelPosition, int programPosition) {
        return events.get(channelPosition).get(programPosition);
    }

    public int getChannelCount() {
        return channels.size();
    }


    public void clearValues(){
        if(channels != null)
            channels.clear();
        if(events != null)
            events.clear();
    }

    public void setEPGPrograms(int channelPosition,List<EPG.EPGProgram> epgPrograms){
        if(events != null && epgPrograms != null) {
            if(channelPosition > -1 && channelPosition < events.size()){
                events.remove(channelPosition);
                events.add(channelPosition,epgPrograms);
            }
        }
    }
    public Map<EPG.EPGChannel, List<EPG.EPGProgram>> getEpgData(){
        return data;
    }

    @Override
    public boolean hasData() {
        return !channels.isEmpty();
    }
}
