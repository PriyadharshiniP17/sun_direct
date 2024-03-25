package com.myplex.myplex.utils;


import androidx.annotation.NonNull;

import java.io.Serializable;


/**
 * Created by apalya on 11/27/2018.
 */

public class MOUUpdateRequestStorage implements Serializable {
    long elapsedTime;
    long timeStamp;
    String internetConnectivity;
    String consumptionType;
    String nid;
    long bytes;
    private float averageBitrate;
    private long bandWidthOfDevice;

    public float getWeightedAverageBitrate() {
        return weightedAverageBitrate;
    }

    public float getWeightedConnectionSpeed() {
        return weightedConnectionSpeed;
    }

    private float weightedAverageBitrate;
    private float weightedConnectionSpeed;
    private long playbackStartUpTime;
    String trackingId;
    private String mSourceTab;
    private String mSource;
    private String mSourceDetails;
    private int sourceCarouselPosition = -1;



    private int mBufferCount;


    public String getmSourceTab() {
        return mSourceTab;
    }

    public void setmSourceTab(String mSourceTab) {
        this.mSourceTab = mSourceTab;
    }

    public MOUUpdateRequestStorage(long elapsedTime, long timeStamp, String internetConnectivity,
                                   String consumptionType, String nid, long bytes,
                                   int isStoredOnServer, String contentId, String trackingID,
                                   float averageBitrate, long bandWidthOfDevice, float weightedAverageBitrate, float weightedConnectionSpeed, long playbackStartUpTime, int mBufferCount
                                    , int sourceCarouselPosition, String mSource, String mSourceDetails, String sourcetab) {
        this.elapsedTime = elapsedTime;
        this.timeStamp = timeStamp;
        this.internetConnectivity = internetConnectivity;
        this.consumptionType = consumptionType;
        this.nid = nid;
        this.bytes = bytes;
        this.isStoredOnServer = isStoredOnServer;
        this.contentId = contentId;
        this.averageBitrate = averageBitrate;
        this.bandWidthOfDevice = bandWidthOfDevice;
        this.weightedAverageBitrate = weightedAverageBitrate;
        this.weightedConnectionSpeed = weightedConnectionSpeed;
        this.playbackStartUpTime = playbackStartUpTime;
        this.trackingId = trackingID;
        this.mBufferCount = mBufferCount;
        this.sourceCarouselPosition = sourceCarouselPosition;
        this.mSource = mSource;
        this.mSourceDetails = mSourceDetails;
        this.mSourceTab = sourcetab;
    }
    public MOUUpdateRequestStorage(long elapsedTime, long timeStamp, String internetConnectivity, String consumptionType, String nid, long bytes, int isStoredOnServer, String contentId,String trackingID) {
        this.elapsedTime = elapsedTime;
        this.timeStamp = timeStamp;
        this.internetConnectivity = internetConnectivity;
        this.consumptionType = consumptionType;
        this.nid = nid;
        this.bytes = bytes;
        this.isStoredOnServer = isStoredOnServer;
        this.contentId = contentId;
        this.trackingId = trackingID;
    }

    public int isStoredOnServer() {
        return isStoredOnServer;
    }

    public void setStoredOnServer(int storedOnServer) {
        isStoredOnServer = storedOnServer;
    }
    //mapping true to 1 and false to 0
    int isStoredOnServer;

    @NonNull
    public int getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(@NonNull int serialNum) {
        this.serialNum = serialNum;
    }


    @NonNull
    private int serialNum;
    private String contentId;

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getInternetConnectivity() {
        return internetConnectivity;
    }

    public void setInternetConnectivity(String internetConnectivity) {
        this.internetConnectivity = internetConnectivity;
    }

    public String getConsumptionType() {
        return consumptionType;
    }

    public void setConsumptionType(String consumptionType) {
        this.consumptionType = consumptionType;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    @NonNull
    public String getContentId() {
        return contentId;
    }

    public void setContentId(@NonNull String contentId) {
        this.contentId = contentId;
    }

    public float getAverageBitrate() {
        return averageBitrate;
    }

    public long getBandWidthOfDevice() {
        return bandWidthOfDevice;
    }

    public long getPlaybackStartUpTime() {
        return playbackStartUpTime;
    }

    public int getBufferCount() {
        return mBufferCount;
    }

    public String getmSource() {
        return mSource;
    }

    public void setmSource(String mSource) {
        this.mSource = mSource;
    }

    public String getmSourceDetails() {
        return mSourceDetails;
    }

    public void setmSourceDetails(String mSourceDetails) {
        this.mSourceDetails = mSourceDetails;
    }

    public int getSourceCarouselPosition() {
        return sourceCarouselPosition;
    }

    public void setSourceCarouselPosition(int sourceCarouselPosition) {
        this.sourceCarouselPosition = sourceCarouselPosition;
    }


}
