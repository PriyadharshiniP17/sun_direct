package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.CardResponseData;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to show the inline search Results
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  InlineSearch inlineSearchRequest = new InlineSearch(getActivity(), new InlineSearch.Params("searchQuery","live","dynamic","count"), new APICallback<CardResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<CardResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(inlineSearchRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class InlineSearch extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;
    private boolean isCancelled = false;

    public void cancel() {
        isCancelled = true;
    }

    public static class Params {

        String query;
        String level;
        String type;
        int count;
        int startIndex;
        String searchFields;

        public Params(String query, String type, String level, int count, int startIndex){
            this.query = query;
            this.level = level;
            this.type = type;
            this.count = count;
            this.startIndex = startIndex;
            this.searchFields = "title";
        }

        public Params(String query, String type, String level, int count, int startIndex, String searchFields) {
            this.query = query;
            this.level = level;
            this.type = type;
            this.count = count;
            this.startIndex = startIndex;
            this.searchFields = searchFields;
        }
    }

    public InlineSearch(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute fetch the search results.
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
//        //Log.d(TAG,"clientKey=" + clientKey);
//        user/currentdata,packages,
        Call<CardResponseData> contentListAPICall = myplexAPI.getInstance().myplexAPIService
                //.inlineSearchRequest(clientKey, params.query, params.type, params.level, APIConstants.PARAM_SEARCH_API_FIELDS, params.count, params.startIndex,PrefUtils.getInstance().getPrefpublisherGroupIds_Android());
                .inlineSearchRequest(clientKey, params.query, params.type, params.level, APIConstants.PARAM_SEARCH_API_FIELDS, params.count,params.searchFields, params.startIndex, PrefUtils.getInstance().getPrefpublisherGroupIds_Android());
        SDKLogger.debug(contentListAPICall.request().url().toString());
        contentListAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                if(!isCancelled) {
                    APIResponse apiResponse = new APIResponse(response.body(), null);
                    if (null != response.body()) {
                        apiResponse.setMessage(response.body().message);
                    }
                    apiResponse.setSuccess(response.isSuccessful());
                    InlineSearch.this.onResponse(apiResponse);
                }

            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                if(!isCancelled) {
                    //Log.d(TAG, "Error :" + t.getMessage());
                    t.printStackTrace();
                    if (t instanceof UnknownHostException
                            || t instanceof ConnectException) {
                        InlineSearch.this.onFailure(t, ERR_NO_NETWORK);
                        return;
                    }
                    InlineSearch.this.onFailure(t, ERR_UN_KNOWN);
                }
            }

        });



    }

}
