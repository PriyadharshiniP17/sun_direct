package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CardResponseData;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to retrieve the program/channel detail data
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  ContentDetails contentDetailRequest = new ContentDetails(getActivity(), new BundleRequest.Params("contentId","), new APICallback<CardResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<CardResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(contentDetailRequest);
 * }</pre>
 * Created by Srikanth on 12/10/2015.
 */
public class ContentDetails extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String contentId;
        String imageProfile;
        String imageType;
        int count;
        String cacheControl;
        public String fields;
        public String consumptionType;

        public Params(String contentId, String imageProfile, String imageType, int count) {
            this.contentId = contentId;
            this.imageProfile = imageProfile;
            this.imageType = imageType;
            this.count = count;

        }

        public Params(String contentId, String imageProfile, String imageType, int count, String cacheControl) {
            this.contentId = contentId;
            this.imageProfile = imageProfile;
            this.imageType = imageType;
            this.count = count;
            this.cacheControl = cacheControl;

        }

        public Params(String contentId, String imageProfile, String imageType, int count, String cacheControl, String fields) {
            this.contentId = contentId;
            this.imageProfile = imageProfile;
            this.imageType = imageType;
            this.count = count;
            this.cacheControl = cacheControl;
            this.fields = fields;
        }
    }

    public ContentDetails(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute  retrieve the program/channel detail data
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        String mccAndMNCValues = SDKUtils.getMCCAndMNCValues(myplexAPISDK.getApplicationContext());
        String mccValue = "";
        String mncValue = "";

        if (mccAndMNCValues != null && mccAndMNCValues.length() > 0) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }
        Call<CardResponseData> mediaLinkAPICall;
        String fields = params != null && params.fields != null ? params.fields : APIConstants.PARAM_CONTENT_DETAILS_ALL_FIELDS;
        if(params.cacheControl == null){
            mediaLinkAPICall = myplexAPI.getInstance().myplexAPIService
                    .contentDetails(params.contentId, clientKey, fields,
                            params.imageProfile,
                            params.imageType,
                            params.count,
                            mccValue,
                            mncValue,
                            params.cacheControl);
        }else{
            mediaLinkAPICall = myplexAPI.getInstance().myplexAPIService
                    .contentDetails(params.contentId, clientKey, fields,
                            params.imageProfile,
                            params.imageType,
                            params.count,
                            mccValue,
                            mncValue);
        }
        mediaLinkAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                //TODO send the responsea
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                ContentDetails.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    ContentDetails.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                ContentDetails.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
