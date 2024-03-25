package com.myplex.api.request;

import android.text.TextUtils;

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


public class RequestChangeMobile extends APIRequest {

    private static final String TAG = RequestChangeMobile.class.getSimpleName();

    private RequestChangeMobile.Params params;


    public static class Params {
        private String emailId, otp, newMobileNumber, newOTP;

        public Params(String emailId ) {
            this.emailId = emailId;
        }
        public Params(String emailId, String otp ) {
            this.emailId = emailId;
            this.otp = otp;
        }
        public Params(String oldMobileNumber, String newMobileNumber, boolean isNewNumber ) {
            this.emailId = oldMobileNumber;
            this.newMobileNumber = newMobileNumber;
        }
        public Params(String emailId, String newMobileNumber, String otp ) {
            this.emailId = emailId;
            this.newMobileNumber = newMobileNumber;
            this.newOTP = otp;
        }
    }

    public RequestChangeMobile(RequestChangeMobile.Params params, APICallback<BaseResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to login the user
     *
     * @param myplexAPIIn Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPIIn) {
        // Send request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();


        //Log.d(TAG, "clientKey=" + clientKey);
        Call<BaseResponseData> mAPICall = null;
        final StringBuilder reqParams = new StringBuilder();

        /*if(!TextUtils.isEmpty(params.emailId)) {
            if (!TextUtils.isEmpty(params.emailId)) {
                reqParams.append("&email=").append(params.emailId);
            }
        }
        if(!TextUtils.isEmpty(params.otp)) {
            if (!TextUtils.isEmpty(params.otp)) {
                reqParams.append("&otp=").append(params.otp);
            }
        }*/
        if(!TextUtils.isEmpty(params.otp)) {
            mAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .changeMobileNumber(clientKey,
                            params.emailId, params.otp);
        } else if(!TextUtils.isEmpty(params.newOTP)) {
            mAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .changeNewMobileNumber(clientKey,
                            params.emailId, params.newMobileNumber,params.newOTP);
        }
       else if(!TextUtils.isEmpty(params.newMobileNumber)) {
            mAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .changeNewMobileNumber(clientKey,
                            params.emailId, params.newMobileNumber);
        }

        else {
            if (!TextUtils.isEmpty(params.emailId)) {
                mAPICall = myplexAPIIn.getInstance().myplexAPIService
                        .changeMobileNumber(clientKey,
                                params.emailId);
            }
        }



        mAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                RequestChangeMobile.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    RequestChangeMobile.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                RequestChangeMobile.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
