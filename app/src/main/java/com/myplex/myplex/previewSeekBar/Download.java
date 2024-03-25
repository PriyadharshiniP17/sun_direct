package com.myplex.myplex.previewSeekBar;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class Download extends AsyncTask<Void, Void, Boolean> {
    private static final String TIME_MATCHER="(\\d+):(\\d+):(\\d+).(\\d+)\\s[-][-][>]\\s(\\d+):(\\d+):(\\d+).(\\d+)";
    private static final String SPLIT_TIME_MATCHER="-->";

    private File file;
    private final WebVTTModule webVTTModule;
    private  WebVttParserInterface listener;
    private HashSet<String> imageFileName =  new HashSet<>();;

    public Download(File file, final WebVTTModule webVTTModule,WebVttParserInterface completeListener) {
        this.file=file;
        this.webVTTModule=webVTTModule;
        this.listener = completeListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {

            FileInputStream input =new FileInputStream(file);
            InputStreamReader isr=new InputStreamReader(input);
            BufferedReader in = new BufferedReader(isr);
            String str=null;
            String[]table;
            Long startTime=null;
            Long endTime=null;
            String text=null;
            String cacheKey = null;
            while ((str = in.readLine()) != null) {
                if(str.contains("Img")) {
                    cacheKey = str;
                    if(cacheKey == null){
                        Log.e("PreviewImage","Cache Key null");
                    }
                    str = in.readLine();
                    if (!TextUtils.isEmpty(str) && str.matches(TIME_MATCHER)) {
                        table = str.split(SPLIT_TIME_MATCHER);
                        if (table.length == 2) {
                            startTime = getASSTime(table[0].trim());
                            endTime = getASSTime(table[1].trim());
                        }
                        text = in.readLine();
                        /*str = in.readLine();
                        if (str != "" && str != "\n") {
                            text = text + str;
                        }*/


                        webVTTModule.addWebVTTData(new WebVTTData(startTime, endTime, text,cacheKey));

                            if (!TextUtils.isEmpty(text)){
                                String [] splitArray = text.split("#");
                                if(splitArray != null && splitArray.length>1){
                                    if(!splitArray[0].contains(":")) {
                                        imageFileName.add(splitArray[0]);
                                    }
                                }
                            }

                    }
                }

            }
            in.close();
            webVTTModule.setReady();
            return true;

        } catch (IOException e) {
            Log.d("DownloadSubtitle", "SUBTITLE_DOWNLOAD_ERROR" + e.getMessage());
        }
        return false;
    }

    protected void onPostExecute(Boolean result) {
        if(result) {
            listener.webVttParserComplete(imageFileName);
        }else{
            listener.webVttParserFailed();
        }
    }

    private static long getASSTime(String ss)
    {
        //0:00:03.38;
        String[] start = ss.split(":");
        long h = Integer.parseInt(start[0]) * 60 * 60 * 1000;
        long m = Integer.parseInt(start[1]) * 60 * 1000;
        String[] seconds = start[2].split("\\.");
        long s = Integer.parseInt(seconds[0]) * 1000;
        long ms = 1000;
        try {
             ms = Integer.parseInt(seconds[1].replaceAll("[^0-9.]","")) * 10;
        }catch (Exception e){
            e.printStackTrace();
        }

        return h + m + s + ms;
    }
}
