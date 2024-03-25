package com.myplex.api.request.epg;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.model.ChannelsCatchupEPGResponseData;
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
public class EpgCatchUpList extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String contentIds;
        String date;
        int startIndex;
        int count;
        boolean channelEpg = true;

        public Params(String contentIds, String date, boolean channelEpg) {
            this.contentIds = contentIds;
            this.date = date;
            this.startIndex = 1;
            this.count = 100;
            this.channelEpg = channelEpg;
        }
        public Params(String contentIds, String date) {
            this.contentIds = contentIds;
            this.date = date;
            this.startIndex = 1;
            this.count = 100;
        }
    }

    public EpgCatchUpList(Params params, APICallback<ChannelsCatchupEPGResponseData> mListener) {
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
        Call<ChannelsCatchupEPGResponseData> channelEPGAPICall = myplexAPI.getInstance().myplexAPIService
                .epgCatchup(clientKey, params.contentIds,
                        "past");

        channelEPGAPICall.enqueue(new Callback<ChannelsCatchupEPGResponseData>() {
            @Override
            public void onResponse(Call<ChannelsCatchupEPGResponseData> call, Response<ChannelsCatchupEPGResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().getMessage());
                }
                apiResponse.setSuccess(response.isSuccessful());
                EpgCatchUpList.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<ChannelsCatchupEPGResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(isNetworkError(t)){
                    EpgCatchUpList.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                EpgCatchUpList.this.onFailure(t,ERR_UN_KNOWN);
            }


        });

    }

}
