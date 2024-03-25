package com.myplex.myplex.utils;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;

import com.myplex.myplex.R;


public class DeviceUtils {
    public static final boolean IS_ALLOW_TAB_LANDSCAPE_ORIENTATION = false;
    public static boolean isTablet(@NonNull Context context){
        return context.getResources().getBoolean(R.bool.isTablet);
    }
    public static boolean isTabletOrientationEnabled(@NonNull Context context){
        return IS_ALLOW_TAB_LANDSCAPE_ORIENTATION && isTablet(context);
    }
    public static boolean isTabletAndLandscape(@NonNull Context context){
        return isTablet(context) && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    public static boolean isTabletSensor(@NonNull Context context){
        return false;
    }

    public static int getScreenOrientation(Context context) {
        Log.e("MINICARDVIDEO PLAYER::", "ORIENTATION METHOD");

        int orientation;

        DisplayMetrics dm = new DisplayMetrics();
        if(context!=null) {
            Display getOrient = ((Activity) context).getWindowManager().getDefaultDisplay();
            getOrient.getMetrics(dm);
        }

        if (dm.widthPixels < dm.heightPixels) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        } else {
            orientation = SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
        return orientation;

    }
}
