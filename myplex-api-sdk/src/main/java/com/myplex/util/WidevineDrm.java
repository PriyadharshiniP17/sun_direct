/*
 * (c)Copyright 2011 Widevine Technologies, Inc
 */

package com.myplex.util;

import android.content.ContentValues;
import android.content.Context;
import android.drm.DrmErrorEvent;
import android.drm.DrmInfo;
import android.drm.DrmInfoEvent;
import android.drm.DrmInfoRequest;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.myplex.model.PlayerStatusUpdate;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.EventListener;
import java.util.Set;

//import java.util.HashMap;

public class WidevineDrm {

	public interface WidevineDrmLogEventListener extends EventListener {
		public void logUpdated(int typeConnectionStatus, int typeRightsInstalled);
	}

	private WidevineDrmLogEventListener logEventListener;
	private final static long DEVICE_IS_PROVISIONED = 0;
	private final static long DEVICE_IS_NOT_PROVISIONED = 1;
	private final static long DEVICE_IS_PROVISIONED_SD_ONLY = 2;
	private long mWVDrmInfoRequestStatusKey = -1;

	public StringBuffer logBuffer = new StringBuffer();

	/**
	 * Drm Manager Configuration Methods
	 */

	public static class Settings {
		public static String WIDEVINE_MIME_TYPE = "video/wvm";
		public static String DRM_SERVER_URI = SDKUtils.getDrmProxy();
		public static String WIDEVINE_KEY_ASSET_URI="WVAssetURIKey";
		public static String WIDEVINE_KEY_LASTERROR="WVLastErrorKey";
		public static final int WIDEVINE_AUTH_FAILED=401;
//		public static String DRM_SERVER_URI = "http://122.248.233.48/widevine/cypherpc/cgi-bin/GetEMMs.cgi";
		public static String DEVICE_ID = "device12345"; // use a unique device ID
		public static String PORTAL_NAME = "sotalapalya";
		//public static String OP_DATA="optdata";

		// test with a sizeable block of user data...
		public static String USER_DATA = "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789";
	};

	private DrmManagerClient mDrmManager;

	private Context mContext;

	public WidevineDrm(Context context) {

		mContext = context;
		mDrmManager = new DrmManagerClient(context);

		mDrmManager.setOnInfoListener(new DrmManagerClient.OnInfoListener() {
			// @Override
			public void onInfo(DrmManagerClient client, DrmInfoEvent event) {
				if (event.getType() == DrmInfoEvent.TYPE_RIGHTS_INSTALLED) {
					logMessage("Rights Installed");
					logMessage(0,DrmInfoEvent.TYPE_RIGHTS_INSTALLED);
				}
			}


		});

		/*mDrmManager.setOnEventListener(new DrmManagerClient.OnEventListener() {

			public void onEvent(DrmManagerClient client, DrmEvent event) {
				switch (event.getType()) {
				case DrmEvent.TYPE_DRM_INFO_PROCESSED:
					logMessage("Info Processed\n");
					break;
				case DrmEvent.TYPE_ALL_RIGHTS_REMOVED:
					logMessage("All rights removed\n");
					break;
				}
			}
		});*/

		mDrmManager.setOnErrorListener(new DrmManagerClient.OnErrorListener() {
			public void onError(DrmManagerClient client, DrmErrorEvent event) {
				
				DrmInfo localDrmInfo = (DrmInfo) event.getAttribute("drm_info_object");

				if (localDrmInfo == null) {
					// it seems the onError callback comes twice, ignore if DrmInfo not available.
					logMessage("localDrmInfo null");
					return ;
				}
				
				int error_code;
				
				try {
					error_code = parseErrorCode(event);
				} catch (Throwable e) {
					
					error_code=-1;
					logMessage("Falied to parse error code");
				}
				
				
				switch (event.getType()) {
				case DrmErrorEvent.TYPE_NO_INTERNET_CONNECTION:
					logMessage("No Internet Connection\n");
					logMessage(error_code,DrmErrorEvent.TYPE_NO_INTERNET_CONNECTION);
					break;
				case DrmErrorEvent.TYPE_NOT_SUPPORTED:
					logMessage("Not Supported\n");
					logMessage(error_code,DrmErrorEvent.TYPE_NOT_SUPPORTED);
					break;
				case DrmErrorEvent.TYPE_OUT_OF_MEMORY:
					logMessage("Out of Memory\n");
					logMessage(error_code,DrmErrorEvent.TYPE_OUT_OF_MEMORY);
					break;
				case DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED:
					logMessage("Process DRM Info failed\n");
					logMessage(error_code,DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED);
					break;
				case DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED:
					logMessage("Remove All Rights failed\n");
					logMessage(error_code,DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED);
					break;
				case DrmErrorEvent.TYPE_RIGHTS_NOT_INSTALLED:
					logMessage("Rights not installed\n");
					logMessage(error_code,DrmErrorEvent.TYPE_RIGHTS_NOT_INSTALLED);
					break;
				case DrmErrorEvent.TYPE_RIGHTS_RENEWAL_NOT_ALLOWED:
					logMessage("Rights renewal not allowed\n");
					logMessage(error_code,DrmErrorEvent.TYPE_NO_INTERNET_CONNECTION);
					break;
				}

			}
		});
	}

	public DrmInfoRequest getDrmInfoRequest(String assetUri) {
		DrmInfoRequest rightsAcquisitionInfo;
		rightsAcquisitionInfo = new DrmInfoRequest(DrmInfoRequest.TYPE_RIGHTS_ACQUISITION_INFO,
				Settings.WIDEVINE_MIME_TYPE);

		rightsAcquisitionInfo.put("WVDRMServerKey", Settings.DRM_SERVER_URI);
		rightsAcquisitionInfo.put("WVAssetURIKey", assetUri);
		rightsAcquisitionInfo.put("WVDeviceIDKey", Settings.DEVICE_ID);
		rightsAcquisitionInfo.put("WVPortalKey", Settings.PORTAL_NAME);
		rightsAcquisitionInfo.put("WVCAUserDataKey", Settings.USER_DATA);
		
		return rightsAcquisitionInfo;
	}
	
	public boolean isProvisionedDevice() {
		if(mWVDrmInfoRequestStatusKey == -1){
			registerPortal(WidevineDrm.Settings.PORTAL_NAME);
		}
		return ((mWVDrmInfoRequestStatusKey == DEVICE_IS_PROVISIONED) ||
				(mWVDrmInfoRequestStatusKey == DEVICE_IS_PROVISIONED_SD_ONLY));
	}
	
	public long getWVLeval() {
		return mWVDrmInfoRequestStatusKey;
	}

	public void registerPortal(String portal) {

		DrmInfoRequest request = new DrmInfoRequest(DrmInfoRequest.TYPE_REGISTRATION_INFO,
				Settings.WIDEVINE_MIME_TYPE);
		request.put("WVPortalKey", portal);
		DrmInfo response = mDrmManager.acquireDrmInfo(request);
		if(response == null)
		{
            Toast.makeText(mContext, "DRM cannot be supported", Toast.LENGTH_LONG).show();
			//        	Toast.makeText(mContext, "acquireDrmInfo failed. DRM cannot be supported", Toast.LENGTH_SHORT).show();
			return;
		}
		String drmInfoRequestStatusKey = (String)response.get("WVDrmInfoRequestStatusKey");
		if (null != drmInfoRequestStatusKey && !drmInfoRequestStatusKey.equals("")) {
			mWVDrmInfoRequestStatusKey = Long.parseLong(drmInfoRequestStatusKey);
		}
	}

	public int acquireRights(String assetUri) {

		int rights = 0;
		if(assetUri.contains("file:"))
		{
			int index=assetUri.lastIndexOf("/");
			String name=assetUri.substring(index+1, assetUri.length());
			assetUri=SDKUtils.downloadStoragePath+name;
			try {
	
				FileInputStream fis = new FileInputStream(assetUri);
	
				FileDescriptor fd = fis.getFD();
				DrmInfoRequest drmReq=getDrmInfoRequest(assetUri);
				if (fd.valid()) {
	
					drmReq.put("FileDescriptorKey", fd.toString());
				}
	
				rights=mDrmManager.acquireRights(drmReq);
	
				fis.close();
	
				}
	
				catch (java.io.IOException e) {
	
					logMessage("Unable to acquire rights for: " + assetUri);
				}
		}
		else
		{
			rights = mDrmManager.acquireRights(getDrmInfoRequest(assetUri));
		}
		logMessage("\n DRM SERVER URI: "+ Settings.DRM_SERVER_URI);
		logMessage("\n DRM assetUri: "+assetUri);
		logMessage("\n DRM DEVICE_ID: "+ Settings.DEVICE_ID);
		logMessage("\n DRM PORTAL_NAME: "+ Settings.PORTAL_NAME);
		logMessage("\n DRM USER_DATA: "+ Settings.USER_DATA);
		logMessage("\n acquireRights = " + rights + "\n");

		return rights;
	}

	public int checkRightsStatus(String assetUri) {
		if (assetUri.contains("file:")) {
			int index = assetUri.lastIndexOf("/");
			String name = assetUri.substring(index + 1, assetUri.length());
			assetUri = SDKUtils.downloadStoragePath + name;
		} else {
			// Need to use acquireDrmInfo prior to calling checkRightsStatus
			mDrmManager.acquireDrmInfo(getDrmInfoRequest(assetUri));
		}
		
		int status = mDrmManager.checkRightsStatus(assetUri);
		logMessage("checkRightsStatus  = " + status + "\n");
		
		showRights(assetUri);
		
		return status;
	}

	public void getConstraints(String assetUri) {

		ContentValues values = mDrmManager.getConstraints(assetUri, DrmStore.Action.PLAY);
		logContentValues(values, "No Contraints");
	}

	public void showRights(String assetUri) {
		logMessage("showRights\n");

		// Need to use acquireDrmInfo prior to calling getConstraints
		mDrmManager.acquireDrmInfo(getDrmInfoRequest(assetUri));
		ContentValues values = mDrmManager.getConstraints(assetUri, DrmStore.Action.PLAY);
		logContentValues(values, "No Rights");


	}

	private void logContentValues(ContentValues values, String defaultMessage) {
		if (values != null) {

			Set<String> keys = values.keySet();
			for (String key : keys) {
				if (key.toLowerCase().contains("time")) {
					logMessage(key + " = " + SecondsToDHMS(values.getAsLong(key)) + "\n");
				} else if (key.toLowerCase().contains("licensetype")) {
					logMessage(key + " = " + licenseType(values.getAsInteger(key)) + "\n");
				} else if (key.toLowerCase().contains("licensedresolution")) {
					logMessage(key + " = " + licenseResolution(values.getAsInteger(key)) + "\n");
				} else {
					logMessage(key + " = " + values.get(key) + "\n");
				}
			}
		} else {
			logMessage(defaultMessage + "\n");
		}
	}

	private static final long seconds_per_minute = 60;
	private static final long seconds_per_hour = 60 * seconds_per_minute;
	private static final long seconds_per_day = 24 * seconds_per_hour;

	private String SecondsToDHMS(long seconds) {
		int days = (int) (seconds / seconds_per_day);
		seconds -= days * seconds_per_day;
		int hours = (int) (seconds / seconds_per_hour);
		seconds -= hours * seconds_per_hour;
		int minutes = (int) (seconds / seconds_per_minute);
		seconds -= minutes * seconds_per_minute;
		return Integer.toString(days) + "d " + Integer.toString(hours) + "h "
		+ Integer.toString(minutes) + "m " + Long.toString(seconds)
		+ "s";
	}

	private String licenseType(int code) {
		switch (code) {
		case 1:
			return "Streaming";
		case 2:
			return "Offline";
		case 3:
			return "Both";
		default:
			return "Unknown";
		}
	}

	private String licenseResolution(int code) {
		switch (code) {
		case 1:
			return "SD only";
		case 2:
			return "HD or SD content";
		default:
			return "Unknown";
		}
	}

	public int removeRights(String assetUri) {

		// Need to use acquireDrmInfo prior to calling removeRights
		mDrmManager.acquireDrmInfo(getDrmInfoRequest(assetUri));
		int removeStatus = mDrmManager.removeRights(assetUri);
		logMessage("removeRights = " + removeStatus + "\n");

		return removeStatus;
	}

	public int removeAllRights() {
		int removeAllStatus = mDrmManager.removeAllRights();
		logMessage("removeAllRights = " + removeAllStatus + "\n");
		return removeAllStatus;
	}

	public void setLogListener(WidevineDrmLogEventListener logEventListener) {
		this.logEventListener = logEventListener;
	}
	public void unRegisterLogListener() {
		this.logEventListener = null;
	}

	private void logMessage( int typeConnectionStatus,int typeRightsInstalled) {
		// TODO Auto-generated method stub
		if (logEventListener != null) {
			logEventListener.logUpdated(typeConnectionStatus,typeRightsInstalled);
			logEventListener=null;
		}
	}
	private void logMessage(String message) {
		sendMessage(message);
		
		//logBuffer.append(message);

		/* if (logEventListener != null) {
            logEventListener.logUpdated(message);
        }*/
	}
	private void sendMessage(final String str){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				/*if(mContext.getResources().getBoolean(R.bool.isTablet) && ApplicationSettings.SHOW_PLAYER_LOGS)
					Util.showToast(mContext, str, Util.TOAST_TYPE_INFO);*/
				if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate(str);
				}
			}
		});
		
	}
	private PlayerStatusUpdate mPlayerStatusListener;
	public void setPlayerListener(PlayerStatusUpdate listener) {
		this.mPlayerStatusListener = listener;
	}
	
	private int parseErrorCode(DrmErrorEvent event) throws Throwable{

		if (event == null) {
			return -1;
		}

		DrmInfo localDrmInfo = (DrmInfo) event.getAttribute("drm_info_object");

		if (localDrmInfo == null) {
			logMessage("localDrmInfo null");
			return -1;
		}

		logMessage("parseErrorCode:" + event.getMessage());

//		for (Iterator iterator2 = localDrmInfo.keyIterator(); iterator2
//				.hasNext();) {
//			String type = (String) iterator2.next();
//			logMessage(type + ":" + localDrmInfo.get(type));
//
//		}

		if (localDrmInfo.get(Settings.WIDEVINE_KEY_ASSET_URI) != null) {

			ContentValues localContentValues = mDrmManager.getConstraints(
					(String) localDrmInfo.get(Settings.WIDEVINE_KEY_ASSET_URI),
					DrmStore.Action.DEFAULT);

			if (localContentValues == null) {
				return -1;
			}

			String str = localContentValues.getAsString(Settings.WIDEVINE_KEY_LASTERROR);
			
			if(str == null){
				return -1;
			}			

			
			logMessage("WVLastErrorKey is " + str);

			if (str.equalsIgnoreCase("ok")) {
				return 200;
			}
			
			/*if(ApplicationSettings.ENABLE_LOG_DRM_ERRORS)
			{

				String message = "acquireRights failed, code:"+str + " url:"+localDrmInfo.get(Settings.WIDEVINE_KEY_ASSET_URI);
			
				Crashlytics.logException(new DRMException(message));
			}*/
			
			String[] parts = str.split("=");

			if (parts == null || parts.length != 2) {
				logMessage("Error code missing" + str);
				return -1;
			}
			
			int code = Integer.parseInt(parts[1].trim());

			logMessage("Error code is " + code);
			
			return code;
		
		}

		return -1;
	}
}
