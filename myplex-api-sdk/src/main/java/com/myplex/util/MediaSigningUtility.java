package com.myplex.util;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class MediaSigningUtility {

    protected static final String TAG = MediaSigningUtility.class.getSimpleName();
    private  String url;
	private  String serviceId;
	public MediaSigningUtility(String link, String _id) {
		url=link;
		serviceId=_id;
	}
    public interface MediaSigningUtilityListner{
    	void onTimeStampUrlReceived(String response);
    }
	public String getStreamingParamsWAP(long timeStamp) {
		String unsigned = new String();
		unsigned = url;
		if (url.contains("?"))
			unsigned += "&";
		else
			unsigned += "?";
		unsigned += "sid=" + serviceId;
		TimeStamp mTimeStamp=new TimeStamp(new Date(timeStamp*1000));
		unsigned += "&stamp=" + mTimeStamp.getSeconds();
		String sign = "";
		String key = "&key=" + "ALL";
		unsigned = unsigned.replaceAll(" ", "");
		sign = getMD5Encryption(unsigned + key);
		String signed = unsigned;
		if (sign != null) {
			signed += "&sign=" + sign;
		}

		return signed;
	}


	
	public static String getMD5Encryption(String url) {
		String MD5String = null;
		byte[] defaultBytes = url.getBytes();
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(defaultBytes);
			byte messageDigest[] = algorithm.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');

				hexString.append(hex);
			}
			MD5String = hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return MD5String;
	}
    private MediaSigningUtilityListner mMediaSigningUtilityListner;
	public void getTimeStampUrl(final MediaSigningUtilityListner mMediaSigningUtilityListner) {
		// TODO Auto-generated method stub
				String url="http://time.akamai.com";
        this.mMediaSigningUtilityListner = mMediaSigningUtilityListner;
        AsyncStringRequest asyncStringRequest = new AsyncStringRequest(url);
        asyncStringRequest.execute();

        asyncStringRequest.setOnResponseListener(new AsyncStringRequest.ResponseListener() {
            @Override
            public void setStringData(String stringResponse) {
                //Log.d(TAG, "timestamp received :" + stringResponse);
                try {
                    long timeStamp = Long.parseLong(stringResponse);

                    mMediaSigningUtilityListner.onTimeStampUrlReceived(getStreamingParamsWAP(timeStamp));
                } catch (Exception e) {
                    mMediaSigningUtilityListner.onTimeStampUrlReceived(getStreamingParamsWAP(0));
                }
            }
        });
	}
}
