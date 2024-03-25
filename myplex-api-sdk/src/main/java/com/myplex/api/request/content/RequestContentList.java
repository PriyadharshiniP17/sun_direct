package com.myplex.api.request.content;


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
import retrofit2.Retrofit;

/**
 * Created by Srikanth on 12/10/2015.
 */
public class RequestContentList extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {

        private String orderBy = "releasedate";
        String contentType;
        int startIndex;
        int count;
        public String genre;
        public String language;
        public String OrderBy;
        public String publishingHouseId;
        public String tags;

        public Params(String contentType, int startIndex, int count, String language, String genre){
            this.contentType = contentType;
            this.startIndex = startIndex;
            this.count = count;
            this.language = language;
            this.genre = genre;

        }
        public Params(String contentType, int startIndex, int count, String language, String genre,
                      String orderBy,String publishingHouseId){
            this.contentType = contentType;
            this.startIndex = startIndex;
            this.count = count;
            this.language = language;
            this.genre = genre;
            this.OrderBy = orderBy;
            this.publishingHouseId = publishingHouseId;
        }

        public Params(String contentType, int startIndex, int count, String language, String genre,
                      String orderBy,String publishingHouseId,String tags){
            this.contentType = contentType;
            this.startIndex = startIndex;
            this.count = count;
            this.language = language;
            this.genre = genre;
            this.OrderBy = orderBy;
            this.publishingHouseId = publishingHouseId;
            this.tags = tags;
        }


        public Params(String contentType, int startIndex, int count, String language, String genre, String orderBy){
            this.contentType = contentType;
            this.startIndex = startIndex;
            this.count = count;
            this.language = language;
            this.genre = genre;
            this.orderBy = orderBy;
        }
    }

    public RequestContentList(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        String orderBy;
        if(this.params.OrderBy != null){
            orderBy = params.OrderBy;
        }else {
            orderBy = "releasedate";
        }

        String tagData = "";
        if (this.params.tags != null) {
            tagData = params.tags;
        }

        // Send the request
        Call<CardResponseData> contentListAPICall = myplexAPI.getInstance().myplexAPIService
                .contentList(clientKey,params.contentType, null, APIConstants.PARAM_CAROUSEL_API_FIELDS,params.startIndex, params
                        .count,params.language,params.genre,orderBy,params.publishingHouseId,tagData);

        contentListAPICall.enqueue(new Callback<CardResponseData>() {

            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                RequestContentList.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    RequestContentList.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                RequestContentList.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
