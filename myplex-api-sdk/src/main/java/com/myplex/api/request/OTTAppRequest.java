package com.myplex.api.request;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.OTTAppData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to display the apps related to content type i.e movies or music
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * This apps will shown in music and Movies tab ,add maintain sibling order to show those app.
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  OTTAppRequest oTTAppRequest = new OTTAppRequest(getActivity(), new OTTAppRequest.Params("content-type"), new APICallback<OfferResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<OfferResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(oTTAppRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class OTTAppRequest extends APIRequest {

    private static final String TAG = "APIService";
    private static final int VERSION = 4;

    private Params params;

    public static class Params {

        private boolean isCache;
        String contentType;
        String language;
        int startIndex;
        int count;

        public Params(String contentType, int startIndex, int count, String language) {
            this.contentType = contentType;
            this.startIndex = startIndex;
            this.count = count;
            this.language = language;
        }

        public Params(String contentType) {
            this.contentType = contentType;
        }


        public Params(String contentType, boolean isCache) {
            this.contentType = contentType;
            this.isCache = isCache;
        }
    }

    public OTTAppRequest(Params params, APICallback<OTTAppData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService fetch appsList related to OTT
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        Call<OTTAppData> contentListAPICall = null;
        if(params != null
                && params.isCache){
            contentListAPICall = myplexAPI.getInstance().myplexAPIService
                    .ottAppRequest(clientKey, params.contentType, VERSION, APIConstants.HTTP_NO_CACHE);
        } else {
            contentListAPICall = myplexAPI.getInstance().myplexAPIService
                    .ottAppRequest(clientKey, params.contentType, VERSION);
        }

        contentListAPICall.enqueue(new Callback<OTTAppData>() {
            @Override
            public void onResponse(Call<OTTAppData> call, Response<OTTAppData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                OTTAppRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<OTTAppData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    OTTAppRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                OTTAppRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }

}
