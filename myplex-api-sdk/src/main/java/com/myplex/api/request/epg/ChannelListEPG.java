package com.myplex.api.request.epg;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.CardResponseData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.util.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
public class ChannelListEPG extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String contentIds;
        String date;
        int startIndex;
        int count;
        boolean channelEpg = true;
        boolean currentProgram = false;

        public Params(String contentIds, String date, boolean channelEpg, boolean currentProgram) {
            this.contentIds = contentIds;
            this.date = date;
            this.startIndex = 1;
            this.count = 100;
            this.channelEpg = channelEpg;
            this.currentProgram = currentProgram;
        }
        public Params(String contentIds, String date, boolean channelEpg, boolean currentProgram,int count) {
            this.contentIds = contentIds;
            this.date = date;
            this.startIndex = 1;
            this.count = count;
            this.channelEpg = channelEpg;
            this.currentProgram = currentProgram;
        }
        public Params(String contentIds, String date) {
            this.contentIds = contentIds;
            this.date = date;
            this.startIndex = 1;
            this.count = 100;
        }
    }

    public ChannelListEPG(Params params, APICallback<ChannelsEPGResponseData> mListener) {
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
        Call<ChannelsEPGResponseData> channelEPGAPICall ;
        if(params.date != null && !params.date.isEmpty())
            channelEPGAPICall = myplexAPI.getInstance().myplexAPIService
                .channelListEPG(clientKey, params.contentIds,
                        params.date,params.count, params.startIndex, params.channelEpg, params.currentProgram);
        else
            channelEPGAPICall = myplexAPI.getInstance().myplexAPIService
                    .channelListEPG(clientKey, params.contentIds, params.channelEpg, params.currentProgram);

        channelEPGAPICall.enqueue(new Callback<ChannelsEPGResponseData>() {
            @Override
            public void onResponse(Call<ChannelsEPGResponseData> call, Response<ChannelsEPGResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().getMessage());
                }
                apiResponse.setSuccess(response.isSuccessful());
                ChannelListEPG.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<ChannelsEPGResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    ChannelListEPG.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                ChannelListEPG.this.onFailure(t,ERR_UN_KNOWN);
            }


        });

    }

}
