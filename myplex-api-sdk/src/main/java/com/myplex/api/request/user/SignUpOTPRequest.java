package com.myplex.api.request.user;


import static com.myplex.api.myplexAPI.DEVICE_REG_SALT1;
import static com.myplex.api.myplexAPI.DEVICE_REG_SALT3;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.request.security.APIEncryption;
import com.myplex.model.BaseResponseData;
import com.myplex.model.DeviceRegData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * This class is used to login the user through the MSISDN
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 * <p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  MSISDNLogin msisdnRequest = new MSISDNLogin(getActivity(), new MSISDNLogin.Params("msisdn","imsi"), new APICallback<BaseReponseData>() {
 *
 *  @Override public void onResponse(APIResponse<BaseReponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(msisdnRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class SignUpOTPRequest extends APIRequest {

    private static final String TAG = SignUpOTPRequest.class.getSimpleName();

    private Params params;


    public static class Params {
        private String mobile;
        private String otp;
        private String newSMCRequest;
        private  String newMobileNumber;


        public Params(String mobile, String otp, String newSMCRequest) {
            this.mobile = mobile;
            this.otp = otp;
            this.newSMCRequest = newSMCRequest;
        }
        public Params(String mobile, String otp, String newSMCRequest, String newMobileNumber) {
            this.mobile = mobile;
            this.otp = otp;
            this.newSMCRequest = newSMCRequest;
            this.newMobileNumber = newMobileNumber;
        }

    }

    public SignUpOTPRequest(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to login the user
     *
     * @param myplexAPIIn Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPIIn) {
        // Send request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();


        //Log.d(TAG, "clientKey=" + clientKey);
        Call<BaseResponseData> loginAPICall = null;
        final StringBuilder reqParams = new StringBuilder();
        //reqParams.append("&Cache-Control=").append(APIConstants.HTTP_NO_CACHE);
        if (!TextUtils.isEmpty(params.mobile)) {
            reqParams.append("mobile=").append(params.mobile);
        }
        if (!TextUtils.isEmpty(params.otp)) {
            reqParams.append("&otp=").append(params.otp);
        }
        if (!TextUtils.isEmpty(params.newSMCRequest) && params.newSMCRequest.equalsIgnoreCase("true")) {
            reqParams.append("&newSMCRequest=").append(params.newSMCRequest);
        }
        if (!TextUtils.isEmpty(params.newMobileNumber)) {
            reqParams.append("&newMobile=").append(params.newMobileNumber);
        }
        String payload = null;
        try {
            String deviceId = PrefUtils.getInstance().getPrefDeviceid();
            Log.d(myplexAPI.TAG, "Excryption: deviceId last 8 chars- " + deviceId.substring(deviceId.length() - 8));
            Log.d(myplexAPI.TAG,reqParams.toString());
//            DEVICE_REG_SALT1 + deviceId.substring(deviceId.length() - 8) + DEVICE_REG_SALT3
            payload = APIEncryption.encryptBase64(reqParams.toString(), DEVICE_REG_SALT1 + deviceId.substring(deviceId.length() - 8) + DEVICE_REG_SALT3);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(myplexAPI.TAG, "Excryption: Exception encryptBase64.e- " + e.getMessage());
        }

        if (!TextUtils.isEmpty(params.newMobileNumber)) {
            loginAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .smcSignup(clientKey,
                            payload);
        } else {
            loginAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .signUpEncrypted(clientKey,
                            payload);

        }

        loginAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                if (response.body() != null
                        && response.body().response != null) {
                    try {
                        String deviceId = PrefUtils.getInstance().getPrefDeviceid();
                        String decryptedResponse = APIEncryption.decryptBase64(response.body().response, DEVICE_REG_SALT1 + deviceId.substring(deviceId.length() - 8) + DEVICE_REG_SALT3);
                        Log.d(myplexAPI.TAG,decryptedResponse);
                        Gson gson = new Gson();
                        BaseResponseData baseResponseData = gson.fromJson(decryptedResponse, DeviceRegData.class);
                        apiResponse = new APIResponse(baseResponseData, null);
                    } catch (Exception e) {
                        Log.d(myplexAPI.TAG, "Excryption: reqParams- " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                SignUpOTPRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    SignUpOTPRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                SignUpOTPRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
