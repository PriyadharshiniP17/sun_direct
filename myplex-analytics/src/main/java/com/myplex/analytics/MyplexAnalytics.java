package com.myplex.analytics;



import android.app.Application;
import android.content.Context;


import com.myplex.api.myplexAPISDK;


import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Apalya on 2/2/2018.
 */

public class MyplexAnalytics implements IMyplexAnalytics{
    private static MyplexAnalytics sMyplexAnalytics;
    private final Context mContext;

    protected MyplexAnalytics(Context context) {
        Assert.assertNotNull(context);
        this.mContext = context;
    }

    @Override
    public void event(Event event){
        if (event != null
                && event.isValid()) {
            sendEvent(event);
        }
    }

    @Override
    public void createProfile(Map<String, Object> params) {
        if (myplexAnalyticsList == null
                || myplexAnalyticsList.isEmpty()) {
            return;
        }
        for (IMyplexAnalytics myplexAnalytics :
                myplexAnalyticsList) {
            myplexAnalytics.createProfile(params);
        }
    }

    @Override
    public void updateProfile(Map<String, Object> params) {
        if (myplexAnalyticsList == null
                || myplexAnalyticsList.isEmpty()) {
            return;
        }
        for (IMyplexAnalytics myplexAnalytics :
                myplexAnalyticsList) {
            myplexAnalytics.updateProfile(params);
        }
    }


    public void sendEvent(Event event) {
        if (myplexAnalyticsList == null
                || myplexAnalyticsList.isEmpty()) {
            return;
        }
        for (IMyplexAnalytics myplexAnalytics :
                myplexAnalyticsList) {
            myplexAnalytics.event(event);
        }
    }

    private static List<IMyplexAnalytics> myplexAnalyticsList = new ArrayList<>();

    public void registerAnalyticsLibs(IMyplexAnalytics myplexAnalytics) {
        if (myplexAnalytics == null || myplexAnalyticsList.contains(myplexAnalytics))
            return;
        myplexAnalyticsList.add(myplexAnalytics);
    }

    public static MyplexAnalytics getInstance() {
        if (sMyplexAnalytics == null) {
            sMyplexAnalytics = new MyplexAnalytics(myplexAPISDK.getApplicationContext());
        }
        return sMyplexAnalytics;
    }
}
