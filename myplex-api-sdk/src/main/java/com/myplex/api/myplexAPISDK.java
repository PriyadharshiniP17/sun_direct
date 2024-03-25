package com.myplex.api;

import android.content.Context;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.myplex.model.LocationInfo;
import com.myplex.sdk.R;
import com.myplex.util.LocationUtil;
import com.myplex.util.Validate;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;


/**
 * Created by Samir on 12/10/2015.
 */
public class myplexAPISDK {

    public static final boolean ENABLE_WEB_SUBSCRIPTION_FLOW = true;
    public static boolean ENABLE_FORCE_CACHE = false;
    public static boolean PARAM_TO_SEND_ALL_PACKAGES_FIELD = false;
    private static Context applicationContext;
    private static Boolean sdkInitialized = false;
    private static LocationUtil sLocationUtil;

    public static FirebaseRemoteConfig getmRemoteConfig() {
        return mRemoteConfig;
    }

    public static void setmRemoteConfig(FirebaseRemoteConfig mRemoteConfig) {
        myplexAPISDK.mRemoteConfig = mRemoteConfig;
    }

    private static FirebaseRemoteConfig mRemoteConfig;

    public static synchronized void sdkInitialize(Context context) {
        Validate.notNull(context, "context");
        sdkInitialized = true;
        applicationContext = context.getApplicationContext();
        initializeLocationUtil(applicationContext);
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Roboto-Medium.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }

    /**
     * The getter for the context of the current application.
     *
     * @return The context of the current application.
     */
    public static Context getApplicationContext() {
        Validate.sdkInitialized();
        return applicationContext;
    }

    public static boolean isInitialized() {
        return sdkInitialized;
    }

    private static LocationUtil getLocationUtil(){
        if(sLocationUtil == null && isInitialized()){
            initializeLocationUtil(applicationContext);
        }
        return sLocationUtil;
    }

    private static void initializeLocationUtil(Context applicationContext) {
        sLocationUtil = LocationUtil.getInstance(applicationContext);
        sLocationUtil.init();
    }

    public static LocationInfo getLocationInfo(){
        LocationInfo locationInfo = null;
        if(myplexAPISDK.getLocationUtil() != null){
            locationInfo = myplexAPISDK.getLocationUtil().getLocationInfo();
        }
        return locationInfo;
    }
}
