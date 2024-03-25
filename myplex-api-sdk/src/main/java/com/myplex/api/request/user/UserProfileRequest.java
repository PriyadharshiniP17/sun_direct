package com.myplex.api.request.user;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.UserProfileResponseData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Srikanth on 12/10/2015.
 */
public class UserProfileRequest extends APIRequest {

    private static final String TAG = UserProfileRequest.class.getSimpleName();

    public UserProfileRequest(APICallback<UserProfileResponseData> mListener) {
        super(mListener);
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<UserProfileResponseData> signupAPICall = myplexAPI.getInstance().myplexAPIService
                .userProfileRequest(clientKey, true);

        signupAPICall.enqueue(new Callback<UserProfileResponseData>() {


            @Override
            public void onResponse(Call<UserProfileResponseData> call, Response<UserProfileResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                UserProfileRequest.this.onResponse(apiResponse);


            }

            @Override
            public void onFailure(Call<UserProfileResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    UserProfileRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                UserProfileRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
