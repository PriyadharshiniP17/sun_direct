package com.myplex.myplex.previewSeekBar;

import java.util.LinkedList;

public class WebVTTModule {
    private boolean isReady=false;

    private int currentPosition=0;
    public LinkedList<WebVTTData> subtitles=new LinkedList<WebVTTData>();


    public WebVTTModule(){
    }

    protected void clear(){
        currentPosition=0;
        subtitles.clear();
    }

    public boolean isEmpty(){
        return subtitles.isEmpty();
    }

    public void addWebVTTData(WebVTTData subtitle){
        subtitles.add(subtitle);
    }

    public WebVTTData getWebVTTData(){

        if(currentPosition>=subtitles.size()||currentPosition<0){
            return null;
        }
        currentPosition++;
        WebVTTData webVTTData = null;
        try{
            webVTTData = subtitles.get(currentPosition);
        }catch ( Exception e){
            e.printStackTrace();
        }
        return webVTTData;
    }


    public void updateToStartInTime(long time){
        if(subtitles == null || subtitles.size()==0){
            return;
        }
        int direction=0;
        int directionlast=0;
        if(currentPosition>=subtitles.size() || currentPosition<0 ){
            currentPosition= 0;
        }
        do{
            direction=subtitles.get(currentPosition).compreWithTime(time);
            if(direction==0){
                return;}
            currentPosition=currentPosition+direction;
            if(direction+directionlast==0){return;}
            directionlast=direction;
        }while(currentPosition>0&&currentPosition<subtitles.size());
    }

    public void setReady(){
        isReady=true;
    }

    public boolean isReady(){
        return isReady;
    }
}
