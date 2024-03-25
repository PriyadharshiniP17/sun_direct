package com.myplex.api.request.user;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.BaseResponseData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SSOLoginRequest extends APIRequest {

    private static final String TAG=SSOLoginRequest.class.getName();

    private Params params;

    public static class Params{
        private String idToken;
        private String authToken;
        private String expiry;

        public Params(String idToken,String authToken,String expiry){
            this.idToken=idToken;
            this.authToken=authToken; 
            this.expiry=expiry;
        }
    }

    public SSOLoginRequest(APICallback mListener,Params params) {
        super(mListener);
        this.params=params;
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<BaseResponseData> mAPICall = null;
            mAPICall = myplexAPI.getInstance().myplexAPIService
                    .SSOLogInRequest(clientKey,params.idToken,params.authToken,params.expiry);
        mAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                SSOLoginRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    SSOLoginRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                SSOLoginRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });

    }
}
