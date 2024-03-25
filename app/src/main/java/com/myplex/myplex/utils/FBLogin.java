package com.myplex.myplex.utils;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.SocialLoginData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FBLogin extends APIRequest {

    private static final String TAG = "FBLogin";
    Map<String,String> params;
    public FBLogin(APICallback mListener, Map<String,String> params) {
        super(mListener);
        this.params = params;
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String authToken = params.get("authToken");
        String tokenExpiry = params.get("tokenExpiry");
        Call<SocialLoginData> fbRequest = myplexAPI.getInstance().myplexAPIService.fbRequest(clientKey,authToken,tokenExpiry);

        fbRequest.enqueue(new Callback<SocialLoginData>() {
            @Override
            public void onResponse(Call<SocialLoginData> call, Response<SocialLoginData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                apiResponse.setSuccess(response.isSuccessful());
                FBLogin.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<SocialLoginData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    FBLogin.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                FBLogin.this.onFailure(t, ERR_UN_KNOWN);
            }
        });

    }


}
