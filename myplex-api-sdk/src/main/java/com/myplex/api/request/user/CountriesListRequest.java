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

public class CountriesListRequest extends APIRequest {

    private static final String TAG = CountriesListRequest.class.getSimpleName();

    public CountriesListRequest(APICallback mListener) {
        super(mListener);
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        String url = "https://qapaasapi.myplex.com/content/v2/properties/country/list";

        Call<CountriesResponse> languageRequest = myplexAPI.getInstance().myplexAPIService
                .countriesListRequest(url, clientKey);

        languageRequest.enqueue(new Callback<CountriesResponse>() {
            @Override
            public void onResponse(Call<CountriesResponse> call, Response<CountriesResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }

                apiResponse.setSuccess(response.isSuccessful());
                CountriesListRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CountriesResponse> call, Throwable t) {
                SDKLogger.debug(TAG + "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    CountriesListRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                CountriesListRequest.this.onFailure(t, ERR_UN_KNOWN);
            }

        });

    }
}
