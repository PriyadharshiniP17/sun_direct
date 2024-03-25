package com.myplex.api.request.user;


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
public class MOUUpdateRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        private final String network;
        String contentId;
        long elapsedTime;
        long timeStamp;
        String consumptionType;
        public String nid;
        long bytes;
        String trackingId;
        private float averageBitrate;
        private float weightedAverageBitrate;
        private float weightedConnectionSpeed;
        private long bandWidthOfDevice;
        private long playbackStartUpTime;
        private int mBufferCount;
        private String mSource;
        private String mSourceDetails;
        private String sourceCarouselPosition ;
        private String sourceTab;


        public Params(String contentId,
                      long elapsedTime,
                      long timeStamp,
                      String internetConnectivity,
                      String consumptionType,
                      String nid,
                      long bytes,
                      String trackingId,
                      float averageBitrate,float weightedAverageBitrate,float weightedConnectionSpeed, long bandWidthOfDevice, long playbackStartUpTime, int mBufferCount
                        ,int sourceCarouselPosition,String mSource,String mSourceDetails,String sourceTab) {
            {
                this.contentId = contentId;
                this.elapsedTime = elapsedTime;
                this.timeStamp = timeStamp;
                this.consumptionType = consumptionType;
                this.network = internetConnectivity;
                this.nid = nid;
                this.bytes = bytes;
                this.trackingId = trackingId;
                this.averageBitrate = averageBitrate;
                this.weightedAverageBitrate = weightedAverageBitrate;
                this.weightedConnectionSpeed = weightedConnectionSpeed;
                this.bandWidthOfDevice = bandWidthOfDevice;
                this.playbackStartUpTime = playbackStartUpTime;
                this.mBufferCount = mBufferCount;
                this.sourceCarouselPosition = sourceCarouselPosition >= 0 ? sourceCarouselPosition + "" : "";
                this.mSource = mSource;
                this.mSourceDetails = mSourceDetails;
                this.sourceTab = sourceTab;
            }
        }
    }

    public MOUUpdateRequest(Params params, APICallback mListener) {
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
        String mediaSessionToken=PrefUtils.getInstance().getMediaSessionToken();
        //Log.d(TAG, "mediaSessionToken=" + mediaSessionToken);
        //Log.d(TAG, "clientKey=" + clientKey);
        Call<BaseResponseData> mouUpdateAPICall = myplexAPI.getInstance().myplexAPIService
                .mouUpdateRequest(clientKey,
                        params.contentId,
                        params.elapsedTime,
                        params.timeStamp,
                        params.network,
                        params.consumptionType,
                        params.nid,
                        params.bytes,
                        params.averageBitrate,
                        params.bandWidthOfDevice,
                        params.weightedAverageBitrate,
                        params.weightedConnectionSpeed,
                        params.playbackStartUpTime,
                        params.trackingId,
                        params.mBufferCount,
                        params.sourceCarouselPosition,
                        params.mSource,
                        params.mSourceDetails,
                        params.sourceTab,
                        APIConstants.PLATFORM_MOBILE,
                        mediaSessionToken,
                        APIConstants.OS_ANDROID
                        );

        mouUpdateAPICall.enqueue(new Callback<BaseResponseData>() {

            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                MOUUpdateRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    MOUUpdateRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                MOUUpdateRequest.this.onFailure(t, ERR_UN_KNOWN);
            }

        });


    }

}
