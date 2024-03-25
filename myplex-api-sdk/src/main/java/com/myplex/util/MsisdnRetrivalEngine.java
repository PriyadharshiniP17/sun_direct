package com.myplex.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APIConstants;
import com.myplex.model.MsisdnData;

import org.xmlpull.v1.XmlPullParser;

public class MsisdnRetrivalEngine  {
	private Context mContext;
	private static String TAG = "MsisdnRetrivalEngine";
	private String mUrl = "http://www.myplexnow.tv/SamsungBillingHub/MsisdnRetriever";
	public  static final String MSISDN_RETRIEVER_URL="http://115.112.238.47:8080/sundirect/MsisdnRetrieval";

	MsisdnData mData;
	private static int FETCHINGMSISDN = 1;
	private static int SENDINGCALLBACK = 2;
	private int mState;
	private MsisdnRetrivalEngineListener mListener;
	private ManageWifiConnection mNetworkManager;
	private boolean resumeOldConnection = true;
	private boolean useOnlyMobileData = false;


	public interface MsisdnRetrivalEngineListener{
		void onMsisdnData(MsisdnData data);
	}


	public MsisdnRetrivalEngine(Context context){
		this.mContext = context;
		mNetworkManager = new ManageWifiConnection(mContext);
	}
	public void deRegisterCallBacks(){
		this.mListener = null;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}
	
	public void setUseOnlyMobileData(boolean useOnlyMobileData) {
		this.useOnlyMobileData = useOnlyMobileData;
	}
	public void setResumeOldConnection(boolean resumeOldConnection) {
		this.resumeOldConnection = resumeOldConnection;
	}
	
	public void getMsisdnData(MsisdnRetrivalEngineListener listener){
		Log.e(TAG, "getMsisdnData");
		this.mListener = listener;
		String currentImsi = getIMSI();
		if(APIConstants.msisdnPath == null){
			APIConstants.msisdnPath =  mContext.getFilesDir()+"/"+"msisdn.bin";
		}
		mData = (MsisdnData) SDKUtils.loadObject(APIConstants.msisdnPath);
		if(mData != null ){
			Log.v("voda msisdn misudn ",mData.msisdn);
			Log.v("voda msisdn operator ", mData.operator);
			Log.e(TAG, "already available");
			sendCallback();
			return;
		}
		// check whether SIM has changed/first launch
		if(useOnlyMobileData || mData == null || mData.imsi == null || mData.imsi.length() == 0 ||!mData.imsi.equalsIgnoreCase(currentImsi)){
			Log.e(TAG, "sim has changed");
			mNetworkManager.changeConnection(new ManageWifiConnection.OnNetworkStateListener() {
				
				@Override
				public void networkStateChanged() {
					if(mData != null && useOnlyMobileData){
						Log.e(TAG, "already available");
						sendCallback();
						return ;
					}
					fetchMsisdn();
					
				}
			});
			
		}else{
			sendCallback();
		}
	}
	private String getIMSI(){
		try {
			TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			return mTelephonyMgr.getSubscriberId();
		} catch (Exception e) {
		}
		return null;
	}
	private void fetchMsisdn(){
		AsyncStringRequest asyncStringRequest = new AsyncStringRequest(MSISDN_RETRIEVER_URL);
		asyncStringRequest.execute();

		asyncStringRequest.setOnResponseListener(new AsyncStringRequest.ResponseListener() {
			@Override
			public void setStringData(String stringResponse) {
				MsisdnData data = parseMsisdn(stringResponse);
				mListener.onMsisdnData(data);
			}
		});
		/*Log.e(TAG, "fetchMsisdn");
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(mUrl, successListener(), errorListener());
		myReg.setRetryPolicy(new DefaultRetryPolicy(7 * 1000, 0, 0f));
		myReg.setShouldCache(false);
		//Log.d(TAG,"Min Request:"+mUrl);
		queue.add(myReg);*/
	}
	/*private Response.Listener<String> successListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.e(TAG, "successListener "+response);
				mData = parseMsisdn(response);
				if(mData != null){
					mData.imsi = getIMSI();
					Util.saveObject(mData,myplexapplication.getApplicationConfig().msisdnPath); 
				}
				sendCallback();
				//Log.d(TAG,"server response "+response);
			}
		};
	}*/

	private MsisdnData parseMsisdn(String response) {
		MsisdnData data = null;
		try {
			if(response == null)
				return data;
			String [] responseArray = response.split(":");

			if(responseArray[0].equals("null"))
				return null;
			data = new MsisdnData();
			data.msisdn = responseArray[0];
			data.imsi   = responseArray[1];


		} catch (Exception e) {
			e.printStackTrace();
			return data;
		}
		return data;
	}
	String parseStringAttribute(XmlPullParser parser, String attributeName) {
		String value = parser.getAttributeValue(null, attributeName);
		return (value == null) ? null : value.trim();
	}

/*	private Response.ErrorListener errorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				//Log.d(TAG,"Error from server "+error.networkResponse);
				sendCallback();
			}
		};
	}*/
	private void sendCallback(){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				
				if(!resumeOldConnection){
					if(mListener != null){
						mListener.onMsisdnData(mData);	
					}
					return;
				}
				
				mNetworkManager.resumeOldConnection(new ManageWifiConnection.OnNetworkStateListener() {
					
					@Override
					public void networkStateChanged() {
						if(mListener != null){
							mListener.onMsisdnData(mData);	
						}
					}
				});
			}
		});
	}
	
	public static String format(String msisdn){
		
		if(TextUtils.isEmpty(msisdn)){
			return msisdn;
		}
		
		return msisdn.substring(msisdn.length()>10?msisdn.length()-10:0);
	}
}
