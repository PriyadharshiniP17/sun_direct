package com.myplex.api.request.user;


import android.app.Activity;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.security.APIEncryption;
import com.myplex.model.DeviceRegData;
import com.myplex.sdk.R;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.myplex.api.myplexAPI.DEVICE_REG_SALT1;
import static com.myplex.api.myplexAPI.DEVICE_REG_SALT3;

/**
 * This class is used to register the device details to server
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * This API is intended for devices like smartphone, tablet, smart TV, etc. which integrate the APIs into their native Applications. The API returns a unique id which the device should use to identify itself. The device id returned is based on the device parameters, for the same parameters the server will always return same deviceId.
 * <P></P>
 * Handle the Success and error cases.
 * <p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  DeviceRegistration deviceRegRequest = new DeviceRegistration(getActivity(), new DeviceRegRequest.Params("contentId","), new APICallback<DeviceRegData>() {
 *
 *  @Override public void onResponse(APIResponse<DeviceRegData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(deviceRegRequest);
 * }</pre>
 * Created by Srikanth on 12/10/2015.
 */
public class DeviceRegistrationEncryptedShreyas extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;
    private Activity mActivity;
 /*   public static String DEVICE_REG_SALT2 = "sr5rtifP";
 */   //public static String DEVICE_REG_SALT2= "yszNKKxY";
    public static String DEVICE_REG_SALT2= APIConstants.DEVICE_REG_SALT2;


    public static class Params {
        String clientSecret;
        String msisdn;

        public Params(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public Params(String clientSecret, String msisdn) {
            this.clientSecret = clientSecret;
            this.msisdn = msisdn;
        }
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute  device registration request
     */
    public DeviceRegistrationEncryptedShreyas(Activity mActivity, Params params, APICallback mListener) {
        super(mListener);
        this.mActivity = mActivity;
        this.params = params;
    }


    @Override
    protected void execute(final myplexAPI myplexAPIInstance) {
        // Send request

        final DisplayMetrics dm = new DisplayMetrics();
        (mActivity).getWindowManager().getDefaultDisplay().getMetrics(dm);

        final int height = dm.heightPixels;
        final int width = dm.widthPixels;
        //Log.d(TAG, String.valueOf(height));
        //Log.d(TAG, String.valueOf(width));
        String devRes = String.valueOf(width) + "x" + String.valueOf(height);
        String serialNo = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            serialNo = SDKUtils.getIMEINumber(mActivity);
//        String serialNo = SDKUtils.getInstanceID(mActivity);
            if (serialNo == null) {
                serialNo = Build.SERIAL;
            }
        }else{
            serialNo =  UUID.randomUUID().toString();
        }
        String externalToken=PrefUtils.getInstance().getAccessToken("access_token");



        String deviceType = "Mobile";
        if (SDKUtils.isTablet(myplexAPISDK.getApplicationContext())) {
            deviceType = "Tablet";
        }
        final StringBuilder reqParams = new StringBuilder("serialNo=").append(serialNo)
                .append("&os=").append(myplexAPISDK.getApplicationContext().getString(R.string.osname))
                .append("&osVersion=").append(Build.VERSION.RELEASE)
                .append("&make=").append(Build.MANUFACTURER)
                .append("&model=").append(Build.MODEL)
                .append("&resolution=").append(devRes)
                .append("&profile=").append(myplexAPISDK.getApplicationContext().getString(R.string.profile))
                .append("&deviceType=").append(deviceType)
                .append("&clientSecret=").append(params.clientSecret)
                .append("&externalToken=").append(externalToken);
        if (params != null) {
            reqParams.append("&X-MSISDN=").append(params.msisdn);
        }
        String payload = null;
        try {
            payload = APIEncryption.encryptBase64(reqParams.toString(), DEVICE_REG_SALT1+ DEVICE_REG_SALT2 + DEVICE_REG_SALT3);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(myplexAPI.TAG, "Excryption: Exception encryptBase64.e- " + e.getMessage());
        }

        Call<DeviceRegData> registerDeviceAPICall = myplexAPIInstance.getInstance().myplexAPIService
                .registerDeviceEncryptedPayLoadShreyas(payload);
        registerDeviceAPICall.enqueue(new Callback<DeviceRegData>() {
            @Override
            public void onResponse(Call<DeviceRegData> call, Response<DeviceRegData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                apiResponse.setSuccess(response.isSuccessful());
                DeviceRegData encryptedResponseData = response.body();
                if (encryptedResponseData != null && encryptedResponseData.response != null) {
                    try {
                        String decryptedResponse = APIEncryption.decryptBase64(encryptedResponseData.response, DEVICE_REG_SALT1 + DEVICE_REG_SALT2 + DEVICE_REG_SALT3);
                        Gson gson = new Gson();
                        DeviceRegData deviceRegData = gson.fromJson(decryptedResponse, DeviceRegData.class);
                        apiResponse = new APIResponse(deviceRegData, null);
                        if (null != deviceRegData
                                && null != deviceRegData.clientKey
                                && null != deviceRegData.deviceId
                                && null != deviceRegData.expiresAt) {
                            apiResponse.setMessage(deviceRegData.message);
                            //Log.d(TAG, "clientKey=" + deviceRegData.clientKey);
                            PrefUtils.getInstance().setPrefClientkey(deviceRegData.clientKey);
                            PrefUtils.getInstance().setPrefDeviceid(deviceRegData.deviceId);
                            PrefUtils.getInstance().setPrefClientkeyExpiry(deviceRegData.expiresAt);
                        }
                    } catch (Exception e) {
                        Log.d(myplexAPI.TAG, "Excryption: reqParams- " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                DeviceRegistrationEncryptedShreyas.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<DeviceRegData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    DeviceRegistrationEncryptedShreyas.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                DeviceRegistrationEncryptedShreyas.this.onFailure(t, ERR_UN_KNOWN);
            }

        });


    }

}
