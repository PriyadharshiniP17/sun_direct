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

public class CityListRequest extends APIRequest {

    private static final String TAG = CityListRequest.class.getSimpleName();

    private CityListRequest.Params params;

    public static class Params{
        public String code;

        public Params(String code){
            this.code=code;
        }
    }

    public CityListRequest(Params params, APICallback mListener) {
        super(mListener);
        this.params=params;
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String url = "https://qapaasapi.myplex.com/content/v2/properties/city/list/?code="+params.code;

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
                CityListRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CountriesResponse> call, Throwable t) {
                SDKLogger.debug(TAG + "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    CityListRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                CityListRequest.this.onFailure(t, ERR_UN_KNOWN);
            }

        });
    }
}
