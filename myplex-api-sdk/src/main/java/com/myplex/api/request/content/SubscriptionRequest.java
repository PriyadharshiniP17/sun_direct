package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
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
 * This class is used to send the subcription request to server
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  SubscriptionRequest subscriptionRequest = new SubscriptionRequest(getActivity(), new SubscriptionRequest.Params("contentId","paymentChannel","packageId","msisdn","operatorName"), new APICallback<BaseReponseData>() {
 *
 *  @Override public void onResponse(APIResponse<BaseReponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(subscriptionRequest);
 * }</pre>
 * Created by phani on 2/2/2015.
 */
public class SubscriptionRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String contentId;
        String paymentChannel;
        String packageId;
        String mobile;
        String operator;


        public Params(String contentId,
                String paymentChannel,
                String packageId,
                String mobile,
                String operator){
            this.contentId = contentId;
            this.paymentChannel = paymentChannel;
            this.packageId = packageId;
            this.mobile = mobile;
            this.operator = operator;
        }
    }

    public SubscriptionRequest(Params params, APICallback<BaseResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to serve the subscription request
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        Call<BaseResponseData> subscriptionAPICall = myplexAPI.getInstance().myplexAPIService
                .subscriptionRequest(clientKey, params.contentId, params.paymentChannel, params.packageId,
                        params.contentId, params.paymentChannel, params.packageId,
                        params.mobile,
                        params.operator);

        subscriptionAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }

                apiResponse.setSuccess(response.isSuccessful());

                SubscriptionRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    SubscriptionRequest.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                SubscriptionRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
