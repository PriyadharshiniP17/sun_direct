package com.myplex.api.request.epg;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.CardResponseData;
import com.myplex.util.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to call Individual channel EPG Request API
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <p></p>
 * Here we get the channel programs of Particular Channel
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  ChannelEPG channelEPGRequest = new ChannelEPG(getActivity(), new BundleRequest.Params("channelId","date","epgstatic","mdpi","count","pageIndex"), new APICallback<CardResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<CardResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(channelEPGRequest);
 * }</pre>
 * Created by Srikanth on 12/10/2015.
 */
public class ChannelEPG extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String contentId;
        String date;
        String level;
        String imageProfile;
        int count;
        int startIndex;


        public Params(String contentId, String date, String level, String imageProfile,
                      int count, int startIndex) {
            this.contentId = contentId;
            this.date = date;
            this.level = level;
            this.imageProfile = imageProfile;
            this.count = count;
            this.startIndex = startIndex;
        }
    }

    public ChannelEPG(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }
    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute ChannelEPG request
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request
        Call<CardResponseData> channelEPGAPICall = myplexAPI.getInstance().myplexAPIService
                .channelEPG(clientKey, params.contentId,
                        params.date,
                        params.level,
                        params.imageProfile,
                        params.count,
                        params.startIndex);

        channelEPGAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                ChannelEPG.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    ChannelEPG.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                ChannelEPG.this.onFailure(t,ERR_UN_KNOWN);
            }


        });

    }

}
