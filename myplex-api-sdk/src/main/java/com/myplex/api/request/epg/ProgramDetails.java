package com.myplex.api.request.epg;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.CardResponseData;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to display the program details
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  ProgramDetails programDetailsRequest = new ProgramDetails(getActivity(), new ProgramDetails.Params("content-type"), new APICallback<CardResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<CardResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(programDetailsRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class ProgramDetails extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String contentId;
        String level;


        public Params(String contentId, String level) {
            this.contentId = contentId;
            this.level = level;
        }
    }

    public ProgramDetails(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }
    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService fetch program details.
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request
        Call<CardResponseData> channelEPGAPICall = myplexAPI.getInstance().myplexAPIService
                .programDetail(clientKey, params.contentId, params.level);

        channelEPGAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                ProgramDetails.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    ProgramDetails.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                ProgramDetails.this.onFailure(t,ERR_UN_KNOWN);
            }
        });

    }

}
