package com.myplex.api.request.user;

import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.BaseResponseData;
import com.myplex.model.UserProfileResponseData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePasswordRequest extends APIRequest {

    private static final String TAG = UpdatePasswordRequest.class.getSimpleName();

    private UpdatePasswordRequest.Params params;


    public static class Params {
        private String currentPassword;
        private String newPassword;

        public Params(String currentPassword,String newPassword ) {
            this.currentPassword = currentPassword;
            this.newPassword=newPassword;
        }
    }

    public UpdatePasswordRequest(UpdatePasswordRequest.Params params, APICallback<BaseResponseData> mListener) {
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

        if(!TextUtils.isEmpty(params.currentPassword)) {
            if (!TextUtils.isEmpty(params.currentPassword)) {
                reqParams.append("&currentPassword=").append(params.currentPassword);
            }
        }else if(!TextUtils.isEmpty(params.newPassword)) {
            if (!TextUtils.isEmpty(params.newPassword)) {
                reqParams.append("&newPassword=").append(params.newPassword);
            }
        }else {
            return;
        }

        mAPICall = myplexAPIIn.getInstance().myplexAPIService
                    .updatePassword(clientKey,
                            params.currentPassword,params.newPassword);




        mAPICall.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call, Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                UpdatePasswordRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<BaseResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    UpdatePasswordRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                UpdatePasswordRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });


    }

}
