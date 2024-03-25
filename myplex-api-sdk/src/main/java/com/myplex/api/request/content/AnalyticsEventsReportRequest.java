package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.BaseResponseData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to update the player events to server
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * Calculate the played duration,stream  while play,pause,and resume states
 * <P></P>
 * Handle the Success and error cases.
 * <p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  MOUUpdateRequest mouRequest = new MOUUpdateRequest(getActivity(), new MOUUpdateRequest.Params("contentId","playedTimeInSecs","currentTime","stream"), new APICallback<CardResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<CardResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(mouRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class AnalyticsEventsReportRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        public String category;
        public String action;
        public String label;
        public String value;

        public Params(String category,
                String action,
                String label,
                String value) {
            this.category = category;
            this.action = action;
            this.label = label;
            this.value = value;
        }
    }

    public AnalyticsEventsReportRequest(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService of played duration in this cases (Play/pause/stop etc)
     *
     * @param myplexAPI Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG, "clientKey=" + clientKey);
        Call<BaseResponseData> mouUpdateAPICall = myplexAPI.getInstance().myplexAPIService
                .analyticsEventsRequest(clientKey,
                        params.category,
                        params.action,
                        params.label,
                        params.value,
                        APIConstants.HTTP_NO_CACHE);

        mouUpdateAPICall.enqueue(new Callback<BaseResponseData>() {


            /**
             * Create a successful response from {@code response}
             */
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                AnalyticsEventsReportRequest.this.onResponse(apiResponse);
            }

            /**
             * Create a synthetic error response from{@code t}
             */
            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    AnalyticsEventsReportRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                AnalyticsEventsReportRequest.this.onFailure(t, ERR_UN_KNOWN);
            }





        });


    }

}
