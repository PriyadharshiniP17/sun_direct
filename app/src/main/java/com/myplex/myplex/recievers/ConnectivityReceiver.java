package com.myplex.myplex.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.pedrovgs.LoggerD;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventNetworkConnectionChange;
import com.myplex.myplex.events.ScopedBus;

import java.util.HashMap;
import java.util.Map;

public class ConnectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            LoggerD.debugLog("ConnectivityReceiver: intent.getAction()- " + intent.getAction());
            if(intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")
                    || intent.getAction().equalsIgnoreCase("android.net.wifi.WIFI_STATE_CHANGED")) {
                if(ConnectivityUtil.isConnected(context)){
                    LoggerD.debugLog("ConnectivityReceiver: connected to a network");
                    ScopedBus.getInstance().post(new EventNetworkConnectionChange(true));

                    Map<String, Object> params = new HashMap<>();
                    params.put(Analytics.PROPERTY_DATA_CONNECTION, SDKUtils.getInternetConnectivity(context));
                    Analytics.setSuperProperties(params);
                } else {
                    LoggerD.debugLog("ConnectivityReceiver: disconnected to a network");
                    ScopedBus.getInstance().post(new EventNetworkConnectionChange(false));
                }
            }
        }
    }