package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.AvailableLoginsPropertiesData;
import com.myplex.model.BaseResponseData;
import com.myplex.util.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Srikanth on 12/10/2015.
 */
public class AvailableLoginsProperties extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {

        private final String clientSecrete;

        public Params(String clientSecrete) {
            this.clientSecrete = clientSecrete;
        }
    }

    public AvailableLoginsProperties(Params params, APICallback<AvailableLoginsPropertiesData> mListener) {
        super(mListener);
        this.params = params;
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG, "clientKey=" + clientKey);
        // Send the request


        Call<AvailableLoginsPropertiesData> contentListAPICall = myplexAPI.getInstance().myplexAPIService
                .loginsAvailable(params.clientSecrete, APIConstants.HTTP_NO_CACHE);

        contentListAPICall.enqueue(new Callback<AvailableLoginsPropertiesData>() {

            @Override
            public void onResponse(Call<AvailableLoginsPropertiesData> call, Response<AvailableLoginsPropertiesData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                AvailableLoginsProperties.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<AvailableLoginsPropertiesData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (isNetworkError(t)) {
                    AvailableLoginsProperties.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                AvailableLoginsProperties.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
