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
 * Created by Srikanth on 18-02-2015.
 */
public class RequestRelatedVODList extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {

        String contentId;
        int count;
        int startIndex;
        String orderMode;
        String orderBy;

        public Params(String contentId,int startIndex, int count){
            this.contentId = contentId;
            this.startIndex = startIndex;
            this.count = count;
        }

        public Params(String contentId,int startIndex, int count,String orderMode,String orderBy){
            this.contentId = contentId;
            this.startIndex = startIndex;
            this.count = count;
            this.orderMode=orderMode;
            this.orderBy=orderBy;
        }
    }

    public RequestRelatedVODList(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request
//        ,user/currentdata,packages
        Call<CardResponseData> contentListAPICall;
        if (params.orderMode==null){
          contentListAPICall  = myplexAPI.getInstance().myplexAPIService.requestRelatedVODList(clientKey,
                  params.contentId, "contents,videos,images," +
                            APIConstants.PARAM_RELATED_VOD_API_FIELDS,params.startIndex, params.count,null,"myplex"
                    );
        }else {
            contentListAPICall = myplexAPI.getInstance().myplexAPIService.requestRelatedVODListWithOrderMode(clientKey,
                    params.contentId, "contents,videos,images," +
                            APIConstants.PARAM_RELATED_VOD_API_FIELDS,params.startIndex, params.count,null,
                    "myplex",
                    params.orderMode,params.orderBy);
        }

        //Log.d(TAG, "execute: contentListAPICall- params: contentId- " + params.contentId +" startIndex- " + params.startIndex);

        contentListAPICall.enqueue(new Callback<CardResponseData>() {

            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                RequestRelatedVODList.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    RequestRelatedVODList.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                RequestRelatedVODList.this.onFailure(t, ERR_UN_KNOWN);
            }

        });



    }

}
