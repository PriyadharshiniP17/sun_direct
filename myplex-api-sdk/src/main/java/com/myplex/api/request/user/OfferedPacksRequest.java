package com.myplex.api.request.user;


import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.OfferResponseData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * This class is used to check the offers to user ,if its available shown those offers to user.
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  OfferedPacksRequest offeredPackRequest = new MSISDNLogin(getActivity(), new OfferedPacksRequest.Params("client-key"), new APICallback<OfferResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<OfferResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(offeredPackRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class OfferedPacksRequest extends APIRequest {

    private static final String TAG = "APIService";
    public static final int TYPE_VERSION_3 = 3;
    public static final int TYPE_VERSION_5 = 5;
    public static final int TYPE_VERSION_1001=1001;
    public static final int TYPE_VERSION_1002=1002;
    public static final String PARAM_API_VERSION = "2";

    private Params params;

    public static class Params {
        public int version;
        public String source;
        public String contentId;
        public String mode;
        public String actionUrl;

        public Params(int version){
            this.version = version;
        }
        public Params(int version,String actionUrl){
            this.version = version;
            this.actionUrl = actionUrl;
        }
        public Params(int version, String source, String contentId,String mode){
            this.mode=mode;
            this.version = version;
            this.source = source;
            this.contentId = contentId;
        }
        public Params(int version, String source, String contentId){
            this.version = version;
            this.source = source;
            this.contentId = contentId;
        }
    }


    public OfferedPacksRequest(APICallback<OfferResponseData> mListener) {
        super(mListener);
    }

    public OfferedPacksRequest(Params params, APICallback<OfferResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService fetch the offers to user
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,this.getClass().getSimpleName() + " clientKey= " + clientKey);
        Call<OfferResponseData> offerPacksRequest = null;
        int version = 0;
        if (params != null) {
            version = params.version;
        }
        switch (version) {
            case 3:
                offerPacksRequest = myplexAPI.getInstance().myplexAPIService
                        .offeredPacksRequestV3(clientKey, "no-cache");
                break;
            case 5:
                if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW) {
                    String apiVersion = PARAM_API_VERSION;
                    TelephonyManager manager = (TelephonyManager) myplexAPISDK.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                    String operatorName = manager.getNetworkOperatorName();

                    if (params.mode!=null){
                        offerPacksRequest = myplexAPI.getInstance().myplexAPIService
                                .offeredPacksRequestForDynamicActionWithMode(clientKey, "no-cache", params.source, apiVersion,params.contentId,params.mode);
                    }else {
                        offerPacksRequest = myplexAPI.getInstance().myplexAPIService
                                .offeredPacksRequestForDynamicAction(clientKey, "no-cache", params.source, apiVersion,params.contentId);
                    }
                } else {
                    offerPacksRequest = myplexAPI.getInstance().myplexAPIService
                            .offeredPacksRequestV3(clientKey, "no-cache");
                }
                break;
            case 1001:
                offerPacksRequest = myplexAPI.getInstance().myplexAPIService
                        .offeredPacksPremium(clientKey, "no-cache");
                break;
            case 1002:
                offerPacksRequest = myplexAPI.getInstance().myplexAPIService
                        .offeredPacksPremiumSubscribe(clientKey, "no-cache", params.actionUrl);
                break;
            default:
                offerPacksRequest = myplexAPI.getInstance().myplexAPIService
                        .offeredPacksRequest(clientKey, "no-cache");
                break;
        }


        offerPacksRequest.enqueue(new Callback<OfferResponseData>() {

            @Override
            public void onResponse(Call<OfferResponseData> call, Response<OfferResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());

                OfferedPacksRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<OfferResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    OfferedPacksRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                OfferedPacksRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }


}
