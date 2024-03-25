package com.myplex.api.request.user;

import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.BaseResponseData;
import com.myplex.model.SMCLIstResponse;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RequestSMCNumbers extends APIRequest {

    private static final String TAG = RequestSMCNumbers.class.getSimpleName();

    private RequestSMCNumbers.Params params;


    public static class Params {
        private String mobile;

        public Params(String mobile ) {
            this.mobile = mobile;
        }
    }

    public RequestSMCNumbers(RequestSMCNumbers.Params params, APICallback<SMCLIstResponse> mListener) {
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
        Call<SMCLIstResponse> mAPICall = null;
        final StringBuilder reqParams = new StringBuilder();

        if(!TextUtils.isEmpty(params.mobile)) {
            if (!TextUtils.isEmpty(params.mobile)) {
                reqParams.append("&mobile=").append(params.mobile);
            }
        }

        if(!TextUtils.isEmpty(params.mobile)) {
            mAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .getSMC(clientKey,
                            params.mobile);
        }



        mAPICall.enqueue(new Callback<SMCLIstResponse>() {
            @Override
            public void onResponse(Call<SMCLIstResponse> call, Response<SMCLIstResponse> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().getMessage());
                }
                apiResponse.setSuccess(response.isSuccessful());
                RequestSMCNumbers.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<SMCLIstResponse> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    RequestSMCNumbers.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                RequestSMCNumbers.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
