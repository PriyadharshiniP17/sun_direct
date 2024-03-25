package com.myplex.api.request.user;


import android.app.Activity;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
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
import retrofit2.Retrofit;

/**
 * This class is used to register the device details to server
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * This API is intended for devices like smartphone, tablet, smart TV, etc. which integrate the APIs into their native Applications. The API returns a unique id which the device should use to identify itself. The device id returned is based on the device parameters, for the same parameters the server will always return same deviceId.
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
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
public class DeviceRegistration extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;
    private Activity mActivity;

    public static class Params {
        String clientSecret;
        String msisdn;

        public Params(String clientSecret){
            this.clientSecret = clientSecret;
        }
        public Params(String clientSecret,String msisdn){
            this.clientSecret = clientSecret;
            this.msisdn = msisdn;
        }
    }
    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute  device registration request
     *
     * */
    public DeviceRegistration(Activity mActivity, Params params, APICallback mListener) {
        super(mListener);
        this.mActivity = mActivity;
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send request

        final DisplayMetrics dm = new DisplayMetrics();
        (mActivity).getWindowManager().getDefaultDisplay().getMetrics(dm);

        final int height = dm.heightPixels;
        final int width = dm.widthPixels;
        //Log.d(TAG, String.valueOf(height));
        //Log.d(TAG, String.valueOf(width));
        String devRes=String.valueOf(width)+"x"+String.valueOf(height);
        String serialNo = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            serialNo = SDKUtils.getIMEINumber(mActivity);
//        String serialNo = SDKUtils.getInstanceID(mActivity);
            if (serialNo == null) {
                serialNo = android.os.Build.SERIAL;
            }
        }else{
            serialNo =  UUID.randomUUID().toString();
        }
//        String serialNo = SDKUtils.getInstanceID(mActivity);
        Call<DeviceRegData> registerDeviceAPICall;
        if(params.msisdn == null){
            registerDeviceAPICall = myplexAPI.getInstance().myplexAPIService
                    .registerDevice(serialNo,
                            myplexAPISDK.getApplicationContext().getString(R.string.osname),
                            android.os.Build.VERSION.RELEASE,
                            android.os.Build.MANUFACTURER,
                            android.os.Build.MODEL,
                            devRes,
                            myplexAPISDK.getApplicationContext().getString(R.string.profile),
                            params.clientSecret);
        }else{
            registerDeviceAPICall = myplexAPI.getInstance().myplexAPIService
                    .registerDevice(params.msisdn,
                            serialNo,
                            myplexAPISDK.getApplicationContext().getString(R.string.osname),
                            android.os.Build.VERSION.RELEASE,
                            android.os.Build.MANUFACTURER, android.os.Build.MODEL,
                            devRes,
                            myplexAPISDK.getApplicationContext().getString(R.string.profile), params
                                    .clientSecret);
        }
        registerDeviceAPICall.enqueue(new Callback<DeviceRegData>() {
            @Override
            public void onResponse(Call<DeviceRegData> call, Response<DeviceRegData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                apiResponse.setSuccess(response.isSuccessful());
                if(null != response.body()
                        && null != response.body().clientKey
                        && null != response.body().deviceId
                        && null != response.body().expiresAt){
                    apiResponse.setMessage(response.body().message);

                    //Log.d(TAG, "clientKey=" + response.body().clientKey);
                    PrefUtils.getInstance().setPrefClientkey( response.body().clientKey);
                    PrefUtils.getInstance().setPrefDeviceid(response.body().deviceId);
                    PrefUtils.getInstance().setPrefClientkeyExpiry(response.body().expiresAt);
                }
                DeviceRegistration.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<DeviceRegData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    DeviceRegistration.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                DeviceRegistration.this.onFailure(t, ERR_UN_KNOWN);
            }

        });



    }

}
