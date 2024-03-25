package com.myplex.api.request.user;

import android.text.TextUtils;
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


public class RequestResetPassword extends APIRequest {

    private static final String TAG = RequestResetPassword.class.getSimpleName();

    private RequestResetPassword.Params params;


    public static class Params {
        private String emailId;

        public Params(String emailId ) {
            this.emailId = emailId;
        }
    }

    public RequestResetPassword(RequestResetPassword.Params params, APICallback<BaseResponseData> mListener) {
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

        if(!TextUtils.isEmpty(params.emailId)) {
            if (!TextUtils.isEmpty(params.emailId)) {
                reqParams.append("&email=").append(params.emailId);
            }
        }

        if(!TextUtils.isEmpty(params.emailId)) {
            mAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .forgotPassword(clientKey,
                            params.emailId);
        }



        mAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                RequestResetPassword.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    RequestResetPassword.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                RequestResetPassword.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
