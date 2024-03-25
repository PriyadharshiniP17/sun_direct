package com.google.android.exoplayer2.debug;

import android.util.Log;

/**
 * Created by Srikanth on 07-Oct-16.
 */
public class LoggerD {

    public static void debugLog(String msg) {
        //        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
        Throwable stack = new Throwable().fillInStackTrace();
        StackTraceElement[] trace = stack.getStackTrace();
        Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
//        }
    }
}
