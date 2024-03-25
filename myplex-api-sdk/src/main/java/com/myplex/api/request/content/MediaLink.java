package com.myplex.api.request.content;


import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CardResponseData;
import com.myplex.model.LocationInfo;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to retrieve the video related information.
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  MediaLinkRequest mediaLinkRequest = new MediaLinkRequest(getActivity(), new MediaLinkRequest.Params("id","internetConnectivity type"), new APICallback<CardResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<CardResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(mediaLinkRequest);
 * }</pre>
 * Created by Srikanth on 12/10/2015.
 */
public class MediaLink extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String contentId;
        String nId;
        String profile;
        String startDate;
        String endDate;
        LocationInfo locationInfo;
        String consumptionType;
        public Params(String contentId, String profile, String nId, LocationInfo locationInfo){
            this.contentId = contentId;
            this.profile = profile;
            this.nId = nId;
            this.locationInfo = locationInfo;
            //Log.d(TAG,"Params: contentId=" + contentId + " profile= " + profile + " nId= " + nId);
        }
        public Params(String contentId, String profile, String nId, LocationInfo locationInfo,String consumptionType){
            this.contentId = contentId;
            this.profile = profile;
            this.nId = nId;
            this.locationInfo = locationInfo;
            this.consumptionType = consumptionType;
            //Log.d(TAG,"Params: contentId=" + contentId + " profile= " + profile + " nId= " + nId);
        }

        public Params(String contentId, String profile, String nId, String startDate, String endDate, LocationInfo locationInfo){
            this.contentId = contentId;
            this.profile = profile;
            this.nId = nId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.locationInfo = locationInfo;
            //Log.d(TAG, "Params: contentId=" + contentId + " profile= " + profile + " nId= " + nId + " startDate= " + startDate + " endDate= " + endDate);
        }
    }

    public MediaLink(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute to retrieve video related information.
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        String fields = "videos,videoInfo,subtitles";
        String country = null,postalCode = null,area = null;
        if (params.locationInfo != null) {
            if (!TextUtils.isEmpty(params.locationInfo.postalCode)) {
                postalCode = params.locationInfo.postalCode;
            }
            if (!TextUtils.isEmpty(params.locationInfo.country)) {
                country = params.locationInfo.country;
            }
            if (!TextUtils.isEmpty(params.locationInfo.area)) {
                area = params.locationInfo.area;
            }
        }
        String mccAndMNCValues = SDKUtils.getMCCAndMNCValues(myplexAPISDK.getApplicationContext());
        String mccValue = "";
        String mncValue = "";

        if (mccAndMNCValues != null && mccAndMNCValues.length() > 0) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }
        Call<CardResponseData> mediaLinkAPICall = myplexAPI.getInstance().myplexAPIService
                .mediaLink(params.contentId, clientKey, fields, params.profile, params.nId, params.startDate, params.endDate, postalCode, country, area,
                        mccValue,
                        mncValue,
                        params.consumptionType,
                        APIConstants.HTTP_NO_CACHE);

        mediaLinkAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                MediaLink.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    MediaLink.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                MediaLink.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }
}
