package com.myplex.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncStringRequest extends AsyncTask<String, String, String> {

    private static final String TAG = AsyncStringRequest.class.getSimpleName();
    private String url;

    public AsyncStringRequest(String s) {
        url = s;
    }


    public interface ResponseListener{
        void setStringData(String stringResponse);
    }
    ResponseListener mResponseListener;
    public void setOnResponseListener(ResponseListener mResponseListener) {
        this.mResponseListener = mResponseListener;
    }

    @Override
    protected String doInBackground(String... uri) {
        URL obj = null;
        //Log.d(TAG, "AsyncStringRequest  doInBackground: uri-" + uri);
        try {
            obj = new URL(url);
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(20*1000);
                con.setReadTimeout(20*1000);
                int responseCode = con.getResponseCode();
                 if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();

                    // print result
                    //System.out.println(response.toString());
                } else {
                    System.out.println("GET request not worked");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Log.d(TAG, "AsyncStringRequest  onPostExecute: response - " + result);
        mResponseListener.setStringData(result);
    }
}