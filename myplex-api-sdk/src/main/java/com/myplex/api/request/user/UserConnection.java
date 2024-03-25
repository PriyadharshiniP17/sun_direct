package com.myplex.api.request.user;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.BaseResponseData;
import com.myplex.model.ConnectionResponseData;
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
public class UserConnection extends APIRequest {

    private static final String TAG = UserConnection.class.getSimpleName();

    private Params params;

    public static class Params {
        String name;
        String mobile;
        String email;
        String pincode;
        String connection;

        public Params(String name, String mobile, String email, String pincode, String connection){
            this.name = name;
            this.mobile = mobile;
            this.email = email;
            this.pincode = pincode;
            this.connection = connection;
        }
    }

    public UserConnection(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG,"clientKey=" + clientKey);
        Call<ConnectionResponseData> signupAPICall = myplexAPI.getInstance().myplexAPIService
                .connectionDetails(clientKey, params.name, params.mobile, params.email,
                        params.pincode,params.connection);

        signupAPICall.enqueue(new Callback<ConnectionResponseData>() {


            @Override
            public void onResponse(Call<ConnectionResponseData> call, Response<ConnectionResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                UserConnection.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<ConnectionResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    UserConnection.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                UserConnection.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
