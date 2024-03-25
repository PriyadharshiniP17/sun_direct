package com.myplex.api.request.user;


import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.LanguageListResponse;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LanguageListRequest extends APIRequest {

    private static final String TAG = LanguageListRequest.class.getSimpleName();

    public LanguageListRequest(APICallback<LanguageListResponse> mListener) {
        super(mListener);
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<LanguageListResponse> genresListResponseCall = myplexAPI.getInstance().myplexAPIService
                .getLanguages(clientKey);

        genresListResponseCall.enqueue(new Callback<LanguageListResponse>() {


            @Override
            public void onResponse(Call<LanguageListResponse> call, Response<LanguageListResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                   // apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                LanguageListRequest.this.onResponse(apiResponse);


            }

            @Override
            public void onFailure(Call<LanguageListResponse> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    LanguageListRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                LanguageListRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
