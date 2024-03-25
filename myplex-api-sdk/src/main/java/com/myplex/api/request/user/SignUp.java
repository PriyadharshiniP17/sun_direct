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
public class SignUp extends APIRequest {

    private static final String TAG = SignUp.class.getSimpleName();

    private Params params;

    public static class Params {
        String email;
        String password;
        String password2;

        public Params(String email, String password, String password2){
            this.email = email;
            this.password = password;
            this.password2 = password2;
        }
    }

    public SignUp(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG,"clientKey=" + clientKey);
        Call<BaseResponseData> signupAPICall = myplexAPI.getInstance().myplexAPIService
                .signUp(clientKey, params.email, params.password, params.password2,
                        myplexAPISDK.getApplicationContext().getString(R.string.profile));

        signupAPICall.enqueue(new Callback<BaseResponseData>() {


            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                SignUp.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    SignUp.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                SignUp.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
