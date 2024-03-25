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
import com.myplex.model.UserSigninResponse;
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
public class MSISDNLoginEncryptedShreyas extends APIRequest {

    private static final String TAG = MSISDNLoginEncryptedShreyas.class.getSimpleName();

    private Params params;


    public static class Params {
        private String otp;
        private String mobile;
        private String userid;
        private int version;
        private String pwd;

        public Params(String userid,String pwd ) {
            this.pwd = pwd;
            this.userid = userid;

        }

        public Params(String mobile,String otp,String userid ) {
            this.mobile = mobile;
            this.userid = userid;
            this.otp = otp;

        }
    }

    public MSISDNLoginEncryptedShreyas(Params params, APICallback mListener) {
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
        Call<UserSigninResponse> loginAPICall = null;
        final StringBuilder reqParams = new StringBuilder();

        if(!TextUtils.isEmpty(params.userid)) {
            if (!TextUtils.isEmpty(params.userid)) {
                reqParams.append("&userid=").append(params.userid);
            }
            if (!TextUtils.isEmpty(params.pwd)) {
                reqParams.append("&password=").append(params.pwd);
            }
        }else{
            if (!TextUtils.isEmpty(params.otp)) {
                reqParams.append("&otp=").append(params.otp);
            }
            if (!TextUtils.isEmpty(params.mobile)) {
                reqParams.append("&mobile=").append(params.mobile);
            }

        }
        String payload = null;
        try {
            String deviceId = PrefUtils.getInstance().getPrefDeviceid();
            Log.d(myplexAPI.TAG, "Excryption: deviceId last 8 chars- " + deviceId.substring(deviceId.length() - 8));
            Log.d(myplexAPI.TAG, "Excryption: input " + reqParams.toString());
//            DEVICE_REG_SALT1 + deviceId.substring(deviceId.length() - 8) + DEVICE_REG_SALT3
            payload = APIEncryption.encryptBase64(reqParams.toString(), DEVICE_REG_SALT1 + deviceId.substring(deviceId.length() - 8) + DEVICE_REG_SALT3);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(myplexAPI.TAG, "Excryption: Exception encryptBase64.e- " + e.getMessage());
        }
        if(!TextUtils.isEmpty(params.userid)) {
            loginAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .msisdnLoginEncryptedShreyas(clientKey,
                            payload);
        }else{
            loginAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .mobileSignInEncrypted(clientKey,
                            payload);
        }



        loginAPICall.enqueue(new Callback<UserSigninResponse>() {
            @Override
            public void onResponse(Call<UserSigninResponse> call, Response<UserSigninResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().getMessage());
                }
                apiResponse.setSuccess(response.isSuccessful());
                if (response.body() != null
                        && response.body() != null) {
                    try {
                        String deviceId = PrefUtils.getInstance().getPrefDeviceid();
                        String decryptedResponse = APIEncryption.decryptBase64(response.body().response, DEVICE_REG_SALT1 + deviceId.substring(deviceId.length() - 8) + DEVICE_REG_SALT3);
                        Gson gson = new Gson();
                      //  BaseResponseData baseResponseData = gson.fromJson(decryptedResponse, DeviceRegData.class);
                        UserSigninResponse baseResponseData = gson.fromJson(decryptedResponse, UserSigninResponse.class);
                        Log.d(myplexAPI.TAG, "Excryption: output " + decryptedResponse);
                        apiResponse = new APIResponse(baseResponseData, null);
                    } catch (Exception e) {
                        Log.d(myplexAPI.TAG, "Excryption: reqParams- " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                MSISDNLoginEncryptedShreyas.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<UserSigninResponse> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    MSISDNLoginEncryptedShreyas.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                MSISDNLoginEncryptedShreyas.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
