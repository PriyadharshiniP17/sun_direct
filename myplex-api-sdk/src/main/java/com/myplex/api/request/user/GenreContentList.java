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

public class GenreContentList extends APIRequest {
    private static final String TAG = "ArtistProfileContentLis";
    private Params params;

    public static class Params {


        String contentType;
        int startIndex;
        int count;
        String orderBy;
        String orderMore;

        public Params(String contentType, int startIndex, int count, String releaseDate,
                      String order) {
            this.contentType = contentType;
            this.startIndex = startIndex;
            this.count = count;
            this.orderBy = releaseDate;
            this.orderMore = order;
        }
    }

    public GenreContentList(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;

    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        Call<CardResponseData> artistProfileListAPICall;

        artistProfileListAPICall = com.myplex.api.myplexAPI.getInstance().myplexAPIService
                .genreContentList(clientKey, params.contentType,  "-1",
                        "relatedCast,generalInfo,contents", params.startIndex,
                        "releaseDate",params.count);

        artistProfileListAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                GenreContentList.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (isNetworkError(t)) {
                    GenreContentList.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                GenreContentList.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }
}
