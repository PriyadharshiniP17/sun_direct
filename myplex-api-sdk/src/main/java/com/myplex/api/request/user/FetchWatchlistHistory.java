package com.myplex.api.request.user;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.CarouselRequest;
import com.myplex.model.CardResponseData;
import com.myplex.util.PrefUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramraju on 2/21/2018.
 */

public class FetchWatchlistHistory extends APIRequest {

    private static final String TAG = "APIService";

    private FetchWatchlistHistory.Params params;

    public static class Params {

        String contentType;
        String fields;
        int startIndex;
        int count;
        String name;


        public Params(String contentType,String fields,int startIndex,int count,String name){
            this.contentType = contentType;
            this.fields = fields;
            this.startIndex = startIndex;
            this.count = count;
            this.name=name;

        }
    }

    public FetchWatchlistHistory(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        String fields = APIConstants.PARAM_CAROUSEL_API_FIELDS;
        if (myplexAPISDK.PARAM_TO_SEND_ALL_PACKAGES_FIELD) {
            fields = fields + "," + APIConstants.ALLPACKAGES;
        }
        Call<CardResponseData> contentListAPICall;
        String appLanguage = PrefUtils.getInstance().getAppLanguageToSendServer();
        List<String> subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
        String packLanguage = "";
        if(subscribed_languages != null && subscribed_languages.size() > 0 && subscribed_languages.get(0) != null)
            packLanguage = subscribed_languages.get(0);
        String url = APIConstants.getCarouselAPIUrl(myplexAPISDK.getApplicationContext(),params.name, fields, params.count, params.startIndex, "405", "868","1608739767", null);
        contentListAPICall = myplexAPI.getInstance().myplexAPIService
                .carouselRequest(url, appLanguage,clientKey,packLanguage);

        contentListAPICall.enqueue(new Callback<CardResponseData>() {

            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                FetchWatchlistHistory.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    FetchWatchlistHistory.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                FetchWatchlistHistory.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
