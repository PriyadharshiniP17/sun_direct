package com.myplex.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ManageWifiConnection {
	private static String TAG = "ManageWifiConnection";
	private Context mContext;
	private int mAction;
	private static final int WIFICONNECTED = 1;
    private static final int WIFIDISCONNECTED = 2;
    private static final int SWITCHOFFWIFI = 3;
    private static final int SWITCHONWIFI = 4;
    private boolean registered = true;
    private boolean mConnectionChanged = false; 		// addressing if the user is already using the mobile data
	private OnNetworkStateListener listener;
	public interface OnNetworkStateListener{
		public void networkStateChanged();
	}
	public ManageWifiConnection(Context context) {
		this.mContext = context;
		
	}
	public void resumeOldConnection(OnNetworkStateListener listener){
		if(listener == null){return;}
		this.listener = listener;
		Log.e(TAG, "resumeOldConnection");
		if(mConnectionChanged){
			switchonWifi();
		}else{
			sendCallBack();
		}
	}
	public void changeConnection(OnNetworkStateListener listener){
		try {
			Log.e(TAG, "changeConnection");
			if(listener == null){return;}
			this.listener = listener;
			mConnectionChanged = false;
			WifiManager wifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
			if(wifiManager.isWifiEnabled()){
				mConnectionChanged = true;
			}
			wifiManager.setWifiEnabled(false);
			mAction = SWITCHOFFWIFI;
			registerReceivers();
//			Toast.makeText(mContext, "switching off wifi", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "changeConnection exceprtion"+e.getMessage());
		}
		
	}
	private boolean switchonWifi(){
		try {
			WifiManager wifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
			wifiManager.setWifiEnabled(true);
			mAction = SWITCHONWIFI;
			registerReceivers();
//			Toast.makeText(mContext, "switching on wifi", Toast.LENGTH_SHORT).show();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
            	//Log.v(TAG,"BroadcastReceiver");
            	if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
					final Network network = ConnectivityUtil.getNetwork(context);
					if (network != null) {
						final NetworkCapabilities nc =ConnectivityUtil.getNetworkCapabilities(context,network);
						if(nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
								nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
							if(mAction == SWITCHONWIFI && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))){
								//Log.v(TAG,"Wifi connected");
								mConnectionHandler.sendEmptyMessage(WIFICONNECTED);
							}else if(mAction == SWITCHOFFWIFI && !(nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))){
								//Log.v(TAG,"Wifi disconnected");
								mConnectionHandler.sendEmptyMessage(WIFIDISCONNECTED);
							}
						}
					}
				}else {
					NetworkInfo currentNetworkInfo = ConnectivityUtil.getNetworkInfo(mContext);
					if(currentNetworkInfo != null){
						if(currentNetworkInfo.isConnected()){
							if(mAction == SWITCHONWIFI && (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI ||currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIMAX)){
								//Log.v(TAG,"Wifi connected");
								mConnectionHandler.sendEmptyMessage(WIFICONNECTED);
							}else if(mAction == SWITCHOFFWIFI && !(currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI ||currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIMAX)){
								//Log.v(TAG,"Wifi disconnected");
								mConnectionHandler.sendEmptyMessage(WIFIDISCONNECTED);
							}
						}
					}
				}

				startTimeOut();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
    };
    private void startTimeOut(){
    	stopTimeOut();
    	//Log.v(TAG,"startTimeOut");
    	mTimeOutHandler.sendEmptyMessageDelayed(TIMEOUT,timeoutInterval);
    }
    private void stopTimeOut(){
    	//Log.v(TAG,"stopTimeOut");
    	try {
    		mTimeOutHandler.removeMessages(TIMEOUT);
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
    private long timeoutInterval = 1000 *60; 
    private  Handler mTimeOutHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case TIMEOUT:{
					//Log.v(TAG,"mTimeOutHandler");
					sendCallBack();
					break;
				}
			}
		}
	};
	private static final int TIMEOUT = 1;
	public void deRegisterReceivers() {
		registered = false;
    	try {
    		mContext.unregisterReceiver(mConnReceiver); 
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	public void registerReceivers(){
		registered = true;
		try {
    		mContext.registerReceiver(mConnReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void sendCallBack(){
		Log.e(TAG, "sendCallBack");
		stopTimeOut();
		Handler h  = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				if(listener != null){
					listener.networkStateChanged();
				}
			}
		});
	}
    private Handler mConnectionHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case WIFICONNECTED:{
//					Toast.makeText(mContext, "wifi connected", Toast.LENGTH_SHORT).show();
					if(registered){
						sendCallBack();
					}
					deRegisterReceivers();
					break;
				}
				case WIFIDISCONNECTED:{
//					Toast.makeText(mContext, "wifi disconnected", Toast.LENGTH_SHORT).show();
					if(registered){
						sendCallBack();
					}
					deRegisterReceivers();
					break;
				}
			}
		}
	};

}
