package com.myplex.api.request.user;


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
 * Created by Srikanth on 12/10/2015.
 */
public class MSISDNRequest extends APIRequest {

    private static final String TAG = MSISDNRequest.class.getSimpleName();

    private Params params;

    public static class Params {
        String userid;
        String password;

        public Params(String userid, String password){
            this.userid = userid;
            this.password = password;
        }
    }

    public MSISDNRequest(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG,"clientKey=" + clientKey);
        Call<BaseResponseData> loginAPICall = myplexAPI.getInstance().myplexAPIService
                .msisdnRequest(clientKey,
                        myplexAPISDK.getApplicationContext().getString(R.string.profile));

        loginAPICall.enqueue(new Callback<BaseResponseData>() {

            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                MSISDNRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    MSISDNRequest.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                MSISDNRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
