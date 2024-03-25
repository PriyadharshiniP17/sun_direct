package com.myplex.myplex.branchIO;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.myplex.analytics.AnalyticsConstants;
import com.myplex.analytics.Event;
import com.myplex.analytics.IMyplexAnalytics;
import com.myplex.api.myplexAPISDK;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;


import org.junit.Assert;

import java.util.Iterator;
import java.util.Map;

import io.branch.referral.Branch;
import io.branch.referral.util.BranchEvent;

/**
 * Created by Apalya on 2/6/2018.
 */

public class BranchIOAnalytics implements IMyplexAnalytics, Application.ActivityLifecycleCallbacks {

    private final Context mContext;
    private Branch mBranch;
    private boolean isBranchInitialized = false;


    public BranchIOAnalytics() {
        mContext = myplexAPISDK.getApplicationContext();
        Assert.assertNotNull(mContext);
        ((Application) mContext).registerActivityLifecycleCallbacks(this);
        init();
    }

    private void init() {
        try {
            // Branch logging for debugging
            Branch.enableLogging();
            // Initialize the Branch object
            mBranch = Branch.getAutoInstance(mContext);
            isBranchInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // //Log.d(TAG, "init clevertap_account_id- " + mContext.getString(R.string.clevertap_account_id) + " clevertap_account_token- " + mContext.getString(R.string.clevertap_account_token));
    }


    @Override
    public void event(Event event) {
        SDKLogger.debug("event- " + event);
        if (event.priority <= PrefUtils.getInstance().getPrefBranchIOEEventPriority() || !PrefUtils.getInstance().getPrefEnableBranchIOAnalytics()) {
            return;
        }
        BranchEvent branchEvent = new BranchEvent(event.eventName);
        try {
            if (event.params != null) {
                for (String key :
                        event.params.keySet()) {
                    branchEvent.addCustomDataProperty(key, String.valueOf(event.params.get(key)));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // option 2: with metadata
        branchEvent.logEvent(mContext);
    }

    @Override
    public void createProfile(Map<String, Object> params) {
        // login
        if (mBranch == null) {
            return;
        }
        if (params.containsKey(AnalyticsConstants.USER_ID))
            mBranch.setIdentity(String.valueOf(params.get(AnalyticsConstants.USER_ID)));
    }

    @Override
    public void updateProfile(Map<String, Object> params) {

    }

    private void filterPropertiesForNA(Map<String, Object> params) {
        if (params == null) {
            return;
        }
        try {
            for (Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Object> entry = it.next();
                if ("NA".equalsIgnoreCase(String.valueOf(entry.getValue()))) {
                    it.remove();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }


}
