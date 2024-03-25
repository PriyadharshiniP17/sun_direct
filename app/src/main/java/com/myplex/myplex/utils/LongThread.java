package com.myplex.myplex.utils;
 
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.pedrovgs.LoggerD;

import java.io.InputStream;
 
public class LongThread implements Runnable {
 
    int threadNo;
    Handler handler;
    String imageUrl;
    public static final String TAG = "LongThread";
 
    public LongThread() {
    }
 
    public LongThread(int threadNo, String imageUrl, Handler handler) {
        this.threadNo = threadNo;
        this.handler = handler;
        this.imageUrl = imageUrl;
    }
 
    @Override
    public void run() {
        //Log.i(TAG, "showOnBoardingScreen: Starting Thread : " + threadNo);
        sendMessage(threadNo, getBitmap(imageUrl));
        //Log.i(TAG, "showOnBoardingScreen: Thread Completed " + threadNo);
    }
 
 
    public void sendMessage(int what, Bitmap msg) {
        Message message = handler.obtainMessage(what, msg);
        message.sendToTarget();
    }
 
    private Bitmap getBitmap(String url) {
        Bitmap bitmap = null;
        try {
            url = url + "?timestamp=" + System.currentTimeMillis();
            // Download Image from URL
            InputStream input = new java.net.URL(url).openStream();
            LoggerD.debugLog("showOnBoardingScreen: url- " + url);
            // Decode Bitmap
            bitmap = BitmapFactory.decodeStream(input);
            // Do extra processing with the bitmap
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
 
}