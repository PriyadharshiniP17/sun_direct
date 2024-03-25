package com.myplex.api.request.user;


import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.BaseResponseData;
import com.myplex.sdk.R;
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
 *<p></p>
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
public class MSISDNLoginV3 extends APIRequest {

    private static final String TAG = MSISDNLoginV3.class.getSimpleName();

    private Params params;


    public static class Params {
        String mobile;
        String imsi;
        String otp;

        public Params(String mobile,String imsi){
            this.mobile = mobile;
            this.imsi   = imsi;
        }

        public Params(String mobile,String imsi,String otp){
            this.mobile = mobile;
            this.imsi   = "";
            this.otp = otp;
        }
    }

    public MSISDNLoginV3(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to login the user
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();


        //Log.d(TAG,"clientKey=" + clientKey);
        Call<BaseResponseData> loginAPICall = null;
        if(params.mobile == null){
            loginAPICall = myplexAPI.getInstance().myplexAPIService
                    .msisdnLogInRequest(clientKey,
                            "",
                            myplexAPISDK.getApplicationContext().getString(R.string.profile));
        }else{
            if(params.imsi.isEmpty()){
                params.mobile="91"+params.mobile;
                if(TextUtils.isEmpty(params.otp)) {
                    loginAPICall = myplexAPI.getInstance().myplexAPIService
                            .msisdnLogInRequestV3(
                                    clientKey,
                                    params.mobile, myplexAPISDK.getApplicationContext().getString(R.string.mode));
                }else{
                    loginAPICall = myplexAPI.getInstance().myplexAPIService
                            .msisdnLogInRequestV3WithOtp(
                                    clientKey,
                                    params.mobile, myplexAPISDK.getApplicationContext().getString(R.string.mode),params.otp);
                }
            }else {
                loginAPICall = myplexAPI.getInstance().myplexAPIService
                        .maisonRetrievalLoginRequest(params.imsi,
                                params.mobile,
                                clientKey,
                                myplexAPISDK.getApplicationContext().getString(R.string.mode));
            }



        }

        loginAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                MSISDNLoginV3.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    MSISDNLoginV3.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                MSISDNLoginV3.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
