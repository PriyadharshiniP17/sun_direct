package com.myplex.api.request.user;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.CardResponseData;
import com.myplex.util.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramraju on 2/21/2018.
 */

public class FetchWatchlistFavorites extends APIRequest {

    private static final String TAG = "APIService";

    private FetchWatchlistFavorites.Params params;

    public static class Params {

        String contentType;
        String fields;
        int startIndex;
        int count;
        int requestType;
        String genre;


        public Params(String contentType,String fields,int startIndex,String genre,int count, int requestType){
            this.contentType = contentType;
            this.fields = fields;
            this.startIndex = startIndex;
            this.count = count;
            this.requestType=requestType;
            this.genre = genre;
        }
    }

    public FetchWatchlistFavorites(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request
        Call<CardResponseData> contentListAPICall;
        if (params.requestType==APIConstants.FAVOURITES_FETCH_REQUEST){
            contentListAPICall= myplexAPI.getInstance().myplexAPIService
                    .fetchFavouritesList(params.contentType,params.fields,params.startIndex,params.count,params.genre, APIConstants.HTTP_NO_CACHE,clientKey,"static","true");
        } else if (params.requestType == APIConstants.FAVOURITES_CHANNEL_FETCH_REQUEST){
            contentListAPICall= myplexAPI.getInstance().myplexAPIService
                    .fetchFavouritesList(params.contentType,params.fields,params.startIndex,params.count, params.genre, APIConstants.HTTP_NO_CACHE,clientKey,"devicemax","true");
        }
        else {
            contentListAPICall= myplexAPI.getInstance().myplexAPIService
                    .fetchWatchListList(params.contentType,params.fields,params.startIndex,params.count, APIConstants.HTTP_NO_CACHE,clientKey,"devicemax");
        }
        contentListAPICall.enqueue(new Callback<CardResponseData>() {

            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                FetchWatchlistFavorites.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    FetchWatchlistFavorites.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                FetchWatchlistFavorites.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
