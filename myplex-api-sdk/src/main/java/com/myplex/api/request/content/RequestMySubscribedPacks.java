package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.MySubscribedPacksResponseData;
import com.myplex.util.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Srikanth on 18-02-2015.
 */
public class RequestMySubscribedPacks extends APIRequest {

    private static final String TAG = "APIService";

    public RequestMySubscribedPacks(APICallback<MySubscribedPacksResponseData> mListener) {
        super(mListener);
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request
        Call<MySubscribedPacksResponseData> contentListAPICall = myplexAPI.getInstance().myplexAPIService.mySubscribedPacksRequest(clientKey, APIConstants.HTTP_NO_CACHE);

        contentListAPICall.enqueue(new Callback<MySubscribedPacksResponseData>() {

            @Override
            public void onResponse(Call<MySubscribedPacksResponseData> call, Response<MySubscribedPacksResponseData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                RequestMySubscribedPacks.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<MySubscribedPacksResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    RequestMySubscribedPacks.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                RequestMySubscribedPacks.this.onFailure(t, ERR_UN_KNOWN);
            }

        });



    }

}
