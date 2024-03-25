package com.myplex.api.request.user;

import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.BaseResponseData;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to register the GCM ID in to server
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  GcmIdRequest gcmIdRequest = new GcmIdRequest(getActivity(), new GcmIdRequest.Params("token","client-key","mccandmncValues","appVersion"), new APICallback<BaseReponseData>() {
 *
 *  @Override public void onResponse(APIResponse<BaseReponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(gcmIdRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class GcmIdRequest extends APIRequest {

    private static final String TAG = GcmIdRequest.class.getSimpleName();

    private Params params;


    public static class Params {
        String gcmId;
        String appVersion;
        String clientKey;
        String mcc;
        String mnc;

        public Params(String gcmId,String appVersion,String clientKey,String mcc,String mnc){
            this.gcmId = gcmId;
            this.appVersion   = appVersion;
            this.clientKey   = clientKey;
            this.mcc  = mcc;
            this.mnc  = mnc;
        }
    }

    public GcmIdRequest(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute register gcmId
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send request

        Call<BaseResponseData> gcmIdReqAPICall = myplexAPI.getInstance().myplexAPIService.gcmIdRequest(params.clientKey, params.gcmId, params.appVersion,params.mcc,params.mnc);


        gcmIdReqAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                GcmIdRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    GcmIdRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                GcmIdRequest.this.onFailure(t, ERR_UN_KNOWN);
            }

        });



    }
}
