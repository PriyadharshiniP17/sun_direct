package com.myplex.api.request.content;


import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.BaseResponseData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
public class PlayerEventsRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        @SerializedName("_id")
        public String _id;
        @SerializedName("title")
        public String title;
        @SerializedName("partnerId")
        public String partnerId;
        @SerializedName("partnerName")
        public String partnerName;
        @SerializedName("mediaUrl")
        public String mediaUrl;
        @SerializedName("bitrate")
        public String bitrate;
        @SerializedName("secDiff")
        public String secDiff;
        @SerializedName("action")
        public String action;
        @SerializedName("resolution")
        public String resolution;
        @SerializedName("contentType")
        public String contentType;
        @SerializedName("platform")
        public String platform = "Android";

        public Params(String _id,
                      String title,
                      String partnerId,
                      String partnerName,
                      String mediaUrl,
                      String action,
                      float bitrate,
                      long secDiff,
                      String contentType,
                      String resolution) {
            this._id = _id;
            this.title = title;
            this.partnerId = partnerId;
            this.partnerName = partnerName;
            this.mediaUrl = mediaUrl;
            this.action = action;
            this.bitrate = String.valueOf(bitrate);
            this.secDiff = String.valueOf(secDiff);
            this.contentType = contentType;
            this.resolution = resolution;
        }
    }

    public PlayerEventsRequest(Params params, APICallback mListener) {
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
/*

        JSONObject formBody = new JSONObject();
        try {
            formBody.put(PARAM_ID, params.partnerId);
            formBody.put(PARAM_MEDIA_URL, params.mediaUrl);
            formBody.put(PARAM_ACTION, params.action);
            formBody.put(PARAM_BITRATE, String.valueOf(params.bitrate));
            formBody.put(PARAM_SECDIFF, String.valueOf(params.secDiff));
            formBody.put(PARAM_PLATFORM, String.valueOf("Android"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/

/*
        Call<BaseResponseData> mouUpdateAPICall = myplexAPI.getInstance().myplexAPIService.playerLogRequest(clientKey,
                params._id,
                params.title,
                params.partnerId,
                params.partnerId,
                params.partnerName,
                params.action,
                params.secDiff,
                "Android",
                params.bitrate,
                params.resolution,
                params.contentType,
                APIConstants.HTTP_NO_CACHE);*/


        /*JsonArray datas = null;
        try {

            datas = new JsonArray();

            JsonObject object = new JsonObject();
            object.addProperty("_id",params._id);
            object.addProperty("title",params.title);
            object.addProperty("partnerId",params.partnerId);
            object.addProperty("partnerName",params.partnerName);
            object.addProperty("mediaUrl",params.mediaUrl);
            object.addProperty("action",params.action);
            object.addProperty("secDiff",params.secDiff);
            object.addProperty("platform",params.platform);
            object.addProperty("bitrate",params.bitrate);
            object.addProperty("contentType",params.contentType);
            object.addProperty("resolution",params.resolution);
            datas.add(object);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
/*        params._id,
                params.title,
                params.partnerId,
                params.partnerId,
                params.mediaUrl,
                params.action,
                params.secDiff,
                "Android",
                params.bitrate,
                params.resolution,
                params.contentType,*/

//        TypedInput in = new TypedByteArray("application/json", json.getBytes("UTF-8"));
        if (params.partnerId == null) {
            params.partnerId = "";
        }
        List<Params> paramsList = new ArrayList<>();
        paramsList.add(params);
        Call<BaseResponseData> mouUpdateAPICall = myplexAPI.getInstance().myplexAPIService.playerLogRequestWithJson(clientKey,
                paramsList,
                APIConstants.HTTP_NO_CACHE);

        mouUpdateAPICall.enqueue(new Callback<BaseResponseData>() {

            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                PlayerEventsRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    PlayerEventsRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                PlayerEventsRequest.this.onFailure(t, ERR_UN_KNOWN);
            }

        });


    }

}
