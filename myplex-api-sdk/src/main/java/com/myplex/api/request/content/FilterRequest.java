package com.myplex.api.request.content;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.GenreFilterData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
/**
 * This class is used to retrieve Filter data
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 * <p></p>
 * Here we get the filter data of genre based and language base.We fire the Request only once in a session mean time we get the results from hashMap
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  FilterRequest filterRequest = new FilterRequest(getActivity(), new BundleRequest.Params("live"), new APICallback<GenreFilterData>() {
 *
 *  @Override public void onResponse(APIResponse<GenreFilterData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(filterRequest);
 * }</pre>
 * Created by phani on 01/06/2015.
 */
public class FilterRequest extends APIRequest{
    private static final String TAG = "APIService";
    private static Response<GenreFilterData> responseDataMovies =null;
    private static Response<GenreFilterData> responseDataLive =null;

    private Params params;

    public static class Params {

        String contentType;


        public Params(String contentType) {
            this.contentType = contentType;

        }

        public Params() {

        }
    }

    public FilterRequest(Params params, APICallback<GenreFilterData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute Filter data
     * <p></p>
     * We need to call Filter Requset only once in a session thn stored in to hash map ,if any time request for the filter data we fetch the data from stored Hashmap instead of firing the request to server
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {

        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(params.contentType)) {
            if (responseDataLive != null) {
                onResponseDate(responseDataLive);
                return;
            }
        } else if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(params.contentType)) {
            if (responseDataMovies != null) {
                onResponseDate(responseDataMovies);
                return;
            }
        }
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        Call<GenreFilterData> filterReqAPICall = myplexAPI.getInstance().myplexAPIService
                .filterValuesRequest(clientKey, params.contentType);
        filterReqAPICall.enqueue(new Callback<GenreFilterData>() {
            @Override
            public void onResponse(Call<GenreFilterData> call, Response<GenreFilterData> response) {
                onResponseDate(response);
            }

            @Override
            public void onFailure(Call<GenreFilterData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    FilterRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                FilterRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });

    }

    public void onResponseDate(Response<GenreFilterData> response) {

        APIResponse apiResponse = new APIResponse(response.body(), null);
        if (null != response.body()) {
            apiResponse.setMessage(response.body().message);
        }
        apiResponse.setSuccess(response.isSuccessful());
        FilterRequest.this.onResponse(apiResponse);
        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(params.contentType)) {
            responseDataLive = response;
        } else if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(params.contentType)) {
            responseDataMovies = response;
        }
    }


    public void onFailure(Throwable t) {
        //Log.d(TAG, "Error :" + t.getMessage());
        t.printStackTrace();
        if(t instanceof UnknownHostException
                || t instanceof ConnectException){
            FilterRequest.this.onFailure(t, ERR_NO_NETWORK);
            return;
        }
        FilterRequest.this.onFailure(t, ERR_UN_KNOWN);
    }
}
