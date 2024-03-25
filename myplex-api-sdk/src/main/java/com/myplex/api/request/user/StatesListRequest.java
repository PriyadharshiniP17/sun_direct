package com.myplex.api.request.user;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.CountriesResponse;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatesListRequest extends APIRequest {

    private static final String TAG = StatesListRequest.class.getSimpleName();

    private Params params;

    public static class Params{
        public String code;

        public Params(String code){
            this.code=code;
        }
    }

    public StatesListRequest(Params params,APICallback mListener) {
        super(mListener);
        this.params=params;
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String url = "https://qapaasapi.myplex.com/content/v2/properties/state/list/?code="+params.code;

        Call<CountriesResponse> languageRequest = myplexAPI.getInstance().myplexAPIService
                .statesListRequest(url, clientKey);

        languageRequest.enqueue(new Callback<CountriesResponse>() {
            @Override
            public void onResponse(Call<CountriesResponse> call, Response<CountriesResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }

                apiResponse.setSuccess(response.isSuccessful());
                StatesListRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CountriesResponse> call, Throwable t) {
                SDKLogger.debug(TAG + "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    StatesListRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                StatesListRequest.this.onFailure(t, ERR_UN_KNOWN);
            }

        });
    }
}
