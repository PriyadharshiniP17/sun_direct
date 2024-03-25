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

/**
 * Created by Srikanth on 12/10/2015.
 */
public class PartnerSignUpRequest extends APIRequest {

    private static final String TAG = PartnerSignUpRequest.class.getSimpleName();
    private Params params;

    public static class Params {
        String partnerId;

        public Params(String partnerName) {
            this.partnerId = partnerName;
        }
    }

    public PartnerSignUpRequest(Params params, APICallback<BaseResponseData> mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<BaseResponseData> signupAPICall = myplexAPI.getInstance().myplexAPIService
                .partnerSignupRequest(clientKey, params.partnerId);

        signupAPICall.enqueue(new Callback<BaseResponseData>() {


            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                PartnerSignUpRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    PartnerSignUpRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                PartnerSignUpRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
