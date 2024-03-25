package com.myplex.api.request.user;

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
 * Created by apalya on 12/26/2016.
 */

public class SignOut extends APIRequest {

    private static final String TAG = SignOut.class.getSimpleName();

    public SignOut(APICallback mListener) {
        super(mListener);
    }


    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();


        Call<BaseResponseData> signOutRequest = myplexAPI.getInstance().myplexAPIService.signOut(clientKey);
        signOutRequest.enqueue(new Callback<BaseResponseData>() {
            @Override
            public void onResponse(Call<BaseResponseData> call,Response<BaseResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                SignOut.this.onResponse(apiResponse);
            }



            @Override
            public void onFailure(Call<BaseResponseData> call,Throwable t) {

                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    SignOut.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                SignOut.this.onFailure(t, ERR_UN_KNOWN);

            }
        });

    }
}
