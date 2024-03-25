package com.github.pedrovgs;

import android.util.Log;

/**
 * Created by Srikanth on 07-Oct-16.
 */
public class LoggerD {
    private static final boolean enable = true;

    public static void debugLogAdapter(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            if (msg == null) {
                return;
            }
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }

    public static void debugLog(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }

    public static void debugHooqVstbLog(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }

    public static void debugExoVideoViewResizable(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }

    public static void debugIMAads(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }

    public static void deebugFullScreen(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }

    public static void debugOTP(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }

    public static void debugAnalytics(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }

    public static void debugDownload(String msg) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] trace = stack.getStackTrace();
            Log.d("VFPLAY: " + trace[1].getClassName() + "." + trace[1].getMethodName() + ": " + trace[1].getLineNumber(), " " + msg);
        }
    }


    public static void vfPlayLogD(String msg, boolean entireStack) {
        if (enable && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            Throwable stack = new Throwable().fillInStackTrace();
            StackTraceElement[] traces = stack.getStackTrace();
            if (entireStack) {
                Log.d("-----VFPLayDownload----", msg);
                for (StackTraceElement trace : traces) {
                    Log.d("VFPLayDownload: " + trace.getClassName() + "." + trace.getMethodName() + ": " + trace.getLineNumber(), " " + msg);
                }
                return;
            }
            Log.d("VFPLayDownload: " + traces[1].getClassName() + "." + traces[1].getMethodName() + ": " + traces[1].getLineNumber(), " " + msg);
        }
    }

}
