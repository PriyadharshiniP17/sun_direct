package com.myplex.api.request.user;

import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.CardResponseData;

import com.myplex.util.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistProfileContentList extends APIRequest {
    private static final String TAG = "ArtistProfileContentLis";
    private Params params;

    public static class Params {


        String contentType;
        int startIndex;
        int count;
        String tags;
        String orderBy;
        String orderMore;
        String person;
        String publishingHouse;
        String language;
        String genre;
        String query;
        String siblingOrder;
        String globalServiceId;
        String contentRights;
        String displayLanguage;


        public Params(String contentType, int startIndex, int count, String personName, String publishingHouse, String releaseDate,
                      String order, String language,String tags) {
            this.contentType = contentType;
            this.startIndex = startIndex;
            this.count = count;
            this.person = personName;
            this.orderBy = releaseDate;
            this.orderMore = order;
            this.publishingHouse = publishingHouse;
            this.language = language;
            this.tags = tags;
            this.genre = "";
            this.query = "";
            this.siblingOrder = "";
            this.globalServiceId = "";
            this.contentRights = "";
            this.displayLanguage = "";
        }
    }

    public ArtistProfileContentList(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;

    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        Call<CardResponseData> artistProfileListAPICall;

        // Send the request
        if (!TextUtils.isEmpty(params.publishingHouse)) {
            artistProfileListAPICall = com.myplex.api.myplexAPI.getInstance().myplexAPIService
                    .profileActorContentList(clientKey, params.contentType, params.language, params.orderMore, params.person,
                            "contents,images,generalInfo,stats,publishingHouse,relatedMedia,relatedCast,subtitles", params.startIndex, params.publishingHouse, params.orderBy, params.globalServiceId, params.tags,
                            params.genre, params.query, params.siblingOrder, params.contentRights, params.displayLanguage,params.count);


        } else {
            artistProfileListAPICall = com.myplex.api.myplexAPI.getInstance().myplexAPIService
                    .profileActorContentList(clientKey, params.contentType, params.language, params.orderMore, params.person,
                            "contents,images,generalInfo,stats,publishingHouse,relatedMedia,relatedCast,subtitles", params.startIndex,
                            "", params.orderBy, params.globalServiceId, params.tags,
                            params.genre, params.query, params.siblingOrder, params.contentRights, params.displayLanguage,params.count);
        }

        artistProfileListAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                ArtistProfileContentList.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (isNetworkError(t)) {
                    ArtistProfileContentList.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                ArtistProfileContentList.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }
}
