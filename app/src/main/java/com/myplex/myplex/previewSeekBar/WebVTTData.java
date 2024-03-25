package com.myplex.myplex.previewSeekBar;

import java.util.Comparator;

public class WebVTTData implements Comparable<WebVTTData> {
    private long startTimeMs = 0;
    private long endTimeMs = -1;
    private String text,cacheKey;


    public WebVTTData(long startTimeMs, long endTimeMs, String text,String cacheKey) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        if(text == null)
            text ="";
        this.text = text;
        this.cacheKey=cacheKey;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    protected void adjustOffsetMs(long timeOffsetMs) {
        startTimeMs += timeOffsetMs;
        endTimeMs += timeOffsetMs;
    }

    public String getText() {
        return text;
    }

    public  String getCacheKey(){ return cacheKey;}

    public int compreWithTime(long time){
        if(time<startTimeMs)return -1;
        if(time>endTimeMs)return 1;
        return 0;
    }

    private final static Comparator<WebVTTData> subtitleDataSetComparator = new Comparator<WebVTTData>() {
        public int compare(WebVTTData info1, WebVTTData info2)
        {
            long time1 = info1.startTimeMs;
            long time2 = info2.startTimeMs;
            if (time1 < time2) {
                return -1;
            } else if (time1 > time2) {
                return 1;
            }
            return 0;
        }
    };

    public final static Comparator<WebVTTData> getComparator() {
        return subtitleDataSetComparator;
    }

    private static String timestampMsToString(long timestampMs) {
        long hh = timestampMs/3600000;
        timestampMs = timestampMs % 3600000;
        long mm = timestampMs/60000;
        timestampMs = timestampMs % 60000;
        long ss = timestampMs / 1000;
        timestampMs = timestampMs % 1000;
        long uuu = timestampMs;
        return String.format("%d:%02d:%02d:%03d", hh, mm, ss, uuu);
    }

    @Override
    public String toString() {
        return "[" + timestampMsToString(startTimeMs) + "-" + timestampMsToString(endTimeMs) + "]" + text +" "+cacheKey;
    }

    @Override
    public int compareTo( WebVTTData arg0 )
    {
        return subtitleDataSetComparator.compare( this, arg0 );
    }


}
