package com.myplex.util;

/**
 * Created by Samir on 12/12/2015.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import java.io.IOException;

/**
 * Check device's network connectivity and speed
 *
 * @author emil http://stackoverflow.com/users/220710/emil
 */
public class ConnectivityUtil {

    /**
     * Get the network info
     *
     * @param context
     * @return
     */

    public static ConnectivityManager getConnectvityManager(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm;
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        return getConnectvityManager(context).getActiveNetworkInfo();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Network getNetwork(Context context){
        return getConnectvityManager(context).getActiveNetwork();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static NetworkCapabilities getNetworkCapabilities(Context context,Network network){
        return getConnectvityManager(context).getNetworkCapabilities(network);
    }

    /**
     * Check if there is any connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            final Network network = getNetwork(context);
            if (network != null) {
                final NetworkCapabilities nc =getNetworkCapabilities(context,network);
                return (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
            }
        }else {
            NetworkInfo info = getNetworkInfo(context);
            return (info != null && info.isConnected());
        }
        return false;*/
        NetworkInfo info = ConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context) {

        /*String network_type = "unknown";
        if(context == null){
            return network_type;
        }*/
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active_network = manager.getActiveNetworkInfo();

        if (active_network == null) {
            return false;
        }
        if (active_network.getType() == ConnectivityManager.TYPE_WIFI){
            return true;
        }else {
            return false;
        }

       // return false;

        /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            final Network network = getNetwork(context);
            if (network != null) {
                final NetworkCapabilities nc =getNetworkCapabilities(context,network);
                return nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        }else {
            NetworkInfo info = getNetworkInfo(context);
            return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
        }
        return false;*/
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context) {

        if (isConnectedWifi(context)){
            return false;
        }else {
            return true;
        }

        /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            final Network network = getNetwork(context);
            if (network != null) {
                final NetworkCapabilities nc =getNetworkCapabilities(context,network);
                return nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }else {
            NetworkInfo info = ConnectivityUtil.getNetworkInfo(context);
            return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
        }*/
       // return false;
    }

    /**
     * Check if there is fast connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = ConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected() && ConnectivityUtil.isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
            /*
			 * Above API level 7, make sure to set android:targetSdkVersion
			 * to appropriate level to use these
			 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static boolean pingIp() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
