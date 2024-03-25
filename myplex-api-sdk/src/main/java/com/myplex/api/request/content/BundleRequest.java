package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.Bundle;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to call subscribe Request API
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  BundleRequest bundleUpdateRequest = new BundleRequest(getActivity(), new BundleRequest.Params("packageId"), new APICallback<Bundle>() {
 *
 *  @Override public void onResponse(APIResponse<Bundle> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(bundleUpdateRequest);
 * }</pre>
 * Created by Srikanth on 12/10/2015.
 */

public class BundleRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String packageId;

        public Params(String packageId){
            this.packageId = packageId;
        }
    }

    public BundleRequest(Params params, APICallback<Bundle> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute Subscription request
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        Call<Bundle> bundleAPICall = myplexAPI.getInstance().myplexAPIService.bundleRequest(params.packageId, clientKey);

        bundleAPICall.enqueue(new Callback<Bundle>() {
            @Override
            public void onResponse(Call<Bundle> call, Response<Bundle> response) {
                //TODO send the response
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                BundleRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<Bundle> call, Throwable t) {
                //TODO file the error
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    BundleRequest.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                BundleRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }

}
