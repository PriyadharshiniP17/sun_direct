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
public class SimilarContentRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {

//        String level;
        String level;
        int startIndex;
        int count;
        String contentId;
//        public String genre;
//        public String language;

        public Params(String contentId, String level, int startIndex, int count){
            this.contentId = contentId;
            this.level = level;
            this.startIndex = startIndex;
            this.count = count;
//            this.language = language;
//            this.genre = genre;
        }
    }

    public SimilarContentRequest(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request
        Call<CardResponseData> contentListAPICall = myplexAPI.getInstance().myplexAPIService
                .similarContentRequest(clientKey, params.contentId, params.level, params.count, APIConstants.ALLPACKAGES);

        contentListAPICall.enqueue(new Callback<CardResponseData>() {

            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                SimilarContentRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    SimilarContentRequest.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                SimilarContentRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
