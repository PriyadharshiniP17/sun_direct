package com.myplex.api.request.user;


import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.SectionsListResponse;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SectionsListRequest extends APIRequest {

    private static final String TAG = SectionsListRequest.class.getSimpleName();

    public SectionsListRequest(APICallback<SectionsListResponse> mListener) {
        super(mListener);
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();

        //Log.d(TAG, "clientKey=" + clientKey);
        Call<SectionsListResponse> genresListResponseCall = myplexAPI.getInstance().myplexAPIService
                .getSectionsList(clientKey);

        genresListResponseCall.enqueue(new Callback<SectionsListResponse>() {


            @Override
            public void onResponse(Call<SectionsListResponse> call, Response<SectionsListResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                   // apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                SectionsListRequest.this.onResponse(apiResponse);


            }

            @Override
            public void onFailure(Call<SectionsListResponse> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    SectionsListRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                SectionsListRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
