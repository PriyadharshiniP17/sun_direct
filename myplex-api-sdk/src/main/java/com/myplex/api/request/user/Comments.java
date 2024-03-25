package com.myplex.api.request.user;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.CardDataCommentsItem;
import com.myplex.model.ValuesResponse;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to get comments list Request API
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * Returns list of comments by the logged in user in reverse chronological order (latest first).
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  Comments commentsRequest = new Comments(getActivity(), new BundleRequest.Params("contentId","comment","count","startIndexcommentCount"), new APICallback<CardDataCommentsItem>() {
 *
 *  @Override public void onResponse(APIResponse<CardDataCommentsItem> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(commentsRequest);
 * }</pre>
 * Created by Srikanth on 12/10/2015.
 */
public class Comments extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String contentId;
        String fields;
        int count;
        int startIndex;

        public Params(String contentId, String fields, int count, int startIndex) {
            this.contentId = contentId;
            this.fields = fields;
            this.count = count;
            this.startIndex = startIndex;
        }
    }

    public Comments(Params params, APICallback<ValuesResponse<CardDataCommentsItem>> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute CommentsList
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG, "clientKey=" + clientKey);
        Call<ValuesResponse<CardDataCommentsItem>> commentsAPICall = myplexAPI.getInstance().myplexAPIService
                .commentsRequest(params.contentId, params.fields, clientKey,
                        params.count,
                        params.startIndex);

        commentsAPICall.enqueue(new Callback<ValuesResponse<CardDataCommentsItem>>() {

            @Override
            public void onResponse(Call<ValuesResponse<CardDataCommentsItem>> call, Response<ValuesResponse<CardDataCommentsItem>> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                Comments.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<ValuesResponse<CardDataCommentsItem>> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    Comments.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                Comments.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
