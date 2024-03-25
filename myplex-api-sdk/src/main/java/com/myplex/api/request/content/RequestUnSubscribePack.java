package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.BaseResponseData;
import com.myplex.util.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Srikanth on 18-02-2015.
 */
public class RequestUnSubscribePack extends APIRequest {

    private static final String TAG = "APIService";
    private Params params;

    public RequestUnSubscribePack(Params params, APICallback<BaseResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    public static class Params {

        String packageId;
        String operator;

        public Params(String packageId,String operator){
            this.packageId = packageId;
            this.operator = operator;
        }
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request
        Call<BaseResponseData> contentListAPICall = myplexAPI.getInstance().myplexAPIService.unSubscribeRequest(clientKey,params.packageId,params.operator);

        contentListAPICall.enqueue(new Callback<BaseResponseData>() {

            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                RequestUnSubscribePack.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    RequestUnSubscribePack.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                RequestUnSubscribePack.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
