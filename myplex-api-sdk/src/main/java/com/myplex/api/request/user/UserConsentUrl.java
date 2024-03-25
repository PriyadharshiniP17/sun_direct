package com.myplex.api.request.user;

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
 * Created by ramaraju on 18/5/18.
 */

public class UserConsentUrl extends APIRequest {


    private Params params;

    public static class Params {
        String type;


        public Params(String type) {
            this.type = type;
        }
    }

    public UserConsentUrl(Params params, APICallback<BaseResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        Call<BaseResponseData> userConsentUrl = myplexAPI.getInstance().myplexAPIService
                .getUserConsentUrl(params.type, clientKey);

        userConsentUrl.enqueue(new Callback<BaseResponseData>() {

            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                UserConsentUrl.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    UserConsentUrl.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                UserConsentUrl.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }
}
