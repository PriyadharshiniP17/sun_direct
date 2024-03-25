package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.FavouriteResponse;
import com.myplex.util.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Srikanth on 12/10/2015.
 */
public class WatchListCheckRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {

//        String level;
        String contentId;
        String contentType;
//        public String genre;
//        public String language;

        public Params(String contentId,String contentType){
            this.contentId = contentId;
            this.contentType = contentType;
//            this.language = language;
//            this.genre = genre;
        }
    }

    public WatchListCheckRequest(Params params, APICallback<FavouriteResponse> mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request
        Call<FavouriteResponse> contentListAPICall = myplexAPI.getInstance().myplexAPIService
                .watchListCheckRequest(clientKey, params.contentId, params.contentType,clientKey, APIConstants.HTTP_NO_CACHE);

        contentListAPICall.enqueue(new Callback<FavouriteResponse>() {

            @Override
            public void onResponse(Call<FavouriteResponse> call, Response<FavouriteResponse> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                WatchListCheckRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<FavouriteResponse> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    WatchListCheckRequest.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                WatchListCheckRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }

}
