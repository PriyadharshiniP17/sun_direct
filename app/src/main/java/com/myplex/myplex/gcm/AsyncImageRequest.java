package com.myplex.myplex.gcm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.IDN;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by Srikanth on 02-Jul-16.
 */
public class AsyncImageRequest extends AsyncTask<String, String, Bitmap> {
    private static final String TAG = AsyncImageRequest.class.getSimpleName();
    private final String imageUrl;
    private ResponseListener mResponseListener;

    AsyncImageRequest(String imageUrl){
        this.imageUrl = imageUrl;
        //Log.d(TAG, "load image url- " + imageUrl);
    }

    public void setOnResponseListener(ResponseListener responseListener) {
        this.mResponseListener = responseListener;
    }

    public interface ResponseListener{
        void onBitmapResponse(Bitmap stringResponse);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }
    protected Bitmap doInBackground(String... args) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(convertUrlToPunycodeIfNeeded(imageUrl)).getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap image) {

        if(image == null){
            Log.e(TAG,"failed to load image url- " + imageUrl);
        }
        mResponseListener.onBitmapResponse(image);
    }

    //The translation of characters to their Latin equivalent
    public static String convertUrlToPunycodeIfNeeded(String url) {
        if (!Charset.forName("US-ASCII").newEncoder().canEncode(url)) {
            if (url.toLowerCase().startsWith("http://")) {
                url = "http://" + IDN.toASCII(url.substring(7));
            } else if (url.toLowerCase().startsWith("https://")) {
                url = "https://" + IDN.toASCII(url.substring(8));
            } else {
                url = IDN.toASCII(url);
            }
        }
        return url;
    }
}