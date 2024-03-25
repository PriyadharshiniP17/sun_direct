package com.myplex.analytics;

import android.app.Application;

import java.util.Map;

/**
 * Created by Srikanth on 20-06-2018.
 */

public interface IMyplexAnalytics {

    void event(Event event);
    void createProfile(Map<String, Object> params);
    void updateProfile(Map<String, Object> params);

}
