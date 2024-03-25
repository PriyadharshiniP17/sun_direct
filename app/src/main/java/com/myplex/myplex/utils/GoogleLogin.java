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
public class GoogleLogin extends APIRequest {

    private static final String TAG = "GoogleLogin";
    Map<String, String> params;

    public GoogleLogin(APICallback mListener, Map<String, String> params) {
        super(mListener);
        this.params = params;
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String authToken = params.get("authToken");
        String tokenExpiry = params.get("tokenExpiry");
        String googleId = params.get("googleId");
        String idToken = params.get("idToken");
        Log.d("FragmentSignIn","clientKey"+clientKey+
                     "authToken"+authToken+
                     "tokenExpiry"+tokenExpiry+
                     "googleId"+googleId+
                     "idToken"+idToken);
        Call<SocialLoginData> googleRequest = myplexAPI.getInstance().myplexAPIService.googleRequest(clientKey,authToken,idToken,googleId,tokenExpiry);

        googleRequest.enqueue(new Callback<SocialLoginData>() {
            @Override
            public void onResponse(Call<SocialLoginData> call, Response<SocialLoginData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                apiResponse.setSuccess(response.isSuccessful());
                GoogleLogin.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<SocialLoginData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    GoogleLogin.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                GoogleLogin.this.onFailure(t, ERR_UN_KNOWN);
            }
        });

    }
    }


