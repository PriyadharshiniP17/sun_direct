package com.myplex.util;

import android.util.Log;

import com.myplex.sdk.BuildConfig;

/**
 * Created by Srikanth on 09-Nov-16.
 */
public class SDKLogger {

    private static final String TAG_LOCATION = "myplexAPI Location";
    private static final boolean ENABLE = false;
    public static void debug(String msg) {
        if (ENABLE && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }

    }
}
