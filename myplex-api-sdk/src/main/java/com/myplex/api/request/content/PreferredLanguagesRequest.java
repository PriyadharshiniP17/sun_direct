package com.myplex.api.request.content;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.model.PreferredLanguageData;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PreferredLanguagesRequest extends APIRequest {
    private static final String TAG = "APIService";
    private Params params;

    public static class Params{
        String clientKey;
        public Params(String clientKey){
            this.clientKey=clientKey;
        }
    }

    public PreferredLanguagesRequest(Params params,APICallback<PreferredLanguageData> mListener) {
        super(mListener);
        this.params=params;
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        Call<PreferredLanguageData> preferredLanguageRequest=myplexAPI.getInstance().myplexAPIService.preferredLanguagesRequest(params.clientKey);
        preferredLanguageRequest.enqueue(new Callback<PreferredLanguageData>() {
            @Override
            public void onResponse(Call<PreferredLanguageData> call, Response<PreferredLanguageData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                PreferredLanguagesRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<PreferredLanguageData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    PreferredLanguagesRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                PreferredLanguagesRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }
}
