package com.myplex.api.request.content;


import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.VernacularResponseNew;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VernacularRequestNew extends APIRequest {
    private static final String TAG = "VernacularRequest";

    public VernacularRequestNew(APICallback mListener) {
        super(mListener);
    }

    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
//        //Log.d(TAG,"clientKey=" + clientKey);

//            TODO  Add login check with fields


        String url = PrefUtils.getInstance().getVernacularLanguageURL();

        SDKLogger.debug("url:: " + url);
        Call<VernacularResponseNew> languageRequest = myplexAPI.getInstance().myplexAPIService
                .vernacularRequestNew(url, clientKey);

        languageRequest.enqueue(new Callback<VernacularResponseNew>() {
            @Override
            public void onResponse(Call<VernacularResponseNew> call, Response<VernacularResponseNew> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                if (null != response.body()) {
                    apiResponse.setMessage(response.body().message);
                }

                apiResponse.setSuccess(response.isSuccessful());
                VernacularRequestNew.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<VernacularResponseNew> call, Throwable t) {
                SDKLogger.debug(TAG + "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    VernacularRequestNew.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                VernacularRequestNew.this.onFailure(t, ERR_UN_KNOWN);
            }

        });

    }
}

