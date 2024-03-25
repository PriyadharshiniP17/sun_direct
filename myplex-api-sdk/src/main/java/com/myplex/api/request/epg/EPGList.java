package com.myplex.api.request.epg;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.model.CardResponseData;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to retrieve Epg for Channels
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  EpgList epgListRequest = new EpgList(getActivity(), new BundleRequest.Params("contentId","), new APICallback<CardResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<CardResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(epgListRequest);
 * }</pre>
 * Created by Srikanth on 12/10/2015.
 */
public class EPGList extends APIRequest {

    private static final String TAG = "APIService";
    private static final String PARAM_PAST_EPG_TRUE = "True";

    private Params params;

    public static class Params {
        String startDate;
        String level;
        String imageProfile;
        int count;
        int startIndex;
        String orderBy;
        String genre;
        String language;
        String mcc;
        String mnc;
        boolean isCircleBased;
        boolean flagEnablePastEpg;
        boolean useDynamicParams;
        String publishingHouseId;


        public Params(String startDate, String level, String imageProfile, int count, int startIndex, String orderBy, String genre, String language, String mcc, String mnc, boolean isCircleBased, boolean flagEnablePastEpg, boolean useDynamicParams) {
            this.startDate = startDate;
            this.level = level;
            this.imageProfile = imageProfile;
            this.count = count;
            this.startIndex = startIndex;
            this.orderBy = orderBy;
            this.genre = genre;
            this.language = language;
            this.mcc  = mcc;
            this.mnc  = mnc;
            this.isCircleBased = isCircleBased;
            this.flagEnablePastEpg = flagEnablePastEpg;
            this.useDynamicParams = useDynamicParams;
        }
        public Params(String startDate, String level, String imageProfile, int count, int startIndex, String orderBy, String genre, String language, String mcc, String mnc, boolean isCircleBased, boolean flagEnablePastEpg, boolean useDynamicParams, String publishingHouseId) {
            this.startDate = startDate;
            this.level = level;
            this.imageProfile = imageProfile;
            this.count = count;
            this.startIndex = startIndex;
            this.orderBy = orderBy;
            this.genre = genre;
            this.language = language;
            this.mcc  = mcc;
            this.mnc  = mnc;
            this.isCircleBased = isCircleBased;
            this.flagEnablePastEpg = flagEnablePastEpg;
            this.useDynamicParams = useDynamicParams;
            this.publishingHouseId = publishingHouseId;
        }
    }


    public EPGList(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link APIService}
     * this method is executed from APIService to execute epgList of channels
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {

        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        // Send the request

        Map<String, String> optionRequestParams = new HashMap<>();
        Call<CardResponseData> epgListAPICall = null;
        if (params.useDynamicParams && fillDynamicParamsIfAvailable(optionRequestParams, params)) {
            epgListAPICall = myplexAPI.getInstance().myplexAPIService.epgListWithDynamicParams(clientKey, optionRequestParams);
        } else {
            if (params.flagEnablePastEpg) {
                if (params.isCircleBased) {
                    epgListAPICall = myplexAPI.getInstance().myplexAPIService.epgCircleListWithPastEPG(clientKey, params.startDate, params.level, params.imageProfile, params.count, params.startIndex, params.orderBy, params.genre, params.language, params.mcc, params.mnc, PARAM_PAST_EPG_TRUE,params.publishingHouseId);
                } else {
                    epgListAPICall = myplexAPI.getInstance().myplexAPIService.epgListWithPastEpg(clientKey, params.startDate, params.level, params.imageProfile, params.count, params.startIndex, params.orderBy, params.genre, params.language, PARAM_PAST_EPG_TRUE,params.publishingHouseId);
                }
            } else {
                if (params.isCircleBased) {
                    epgListAPICall = myplexAPI.getInstance().myplexAPIService.epgCircleList(clientKey, params.startDate, params.level, params.imageProfile, params.count, params.startIndex, params.orderBy, params.genre, params.language, params.mcc, params.mnc,params.publishingHouseId);
                } else {
                    epgListAPICall = myplexAPI.getInstance().myplexAPIService.epgList(clientKey, params.startDate, params.level, params.imageProfile, params.count, params.startIndex, params.orderBy, params.genre, params.language,params.publishingHouseId);
                }
            }
        }


        epgListAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                EPGList.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    EPGList.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                EPGList.this.onFailure(t, ERR_UN_KNOWN);
            }
        });

    }

    /**
     * If there are request params available from properties. do use them and minimum default values are need to be maintained.
     * If not minimum values need to passed
     * 1. params.startDate
     * 2. params.level
     * 3. params.imageProfile
     * 4. params.count
     * 5. params.startIndex
     * 6. params.orderBy
     * 7. params.genre
     * 8. params.language
     * 9. params.mcc
     * 10. params.mnc
     * 11. PARAM_PAST_EPG_TRUE
     * 12. Other optional params
     * example structure: genre=#genre&language=#language&count=20&mcc=#mcc&mnc=#mnc&orderBy=siblingOrder,location
     * */

    private boolean fillDynamicParamsIfAvailable(Map<String, String> optionRequestParams, Params params) {
        if (PropertiesHandler.propertiesEPGListAPIParams == null
                || PropertiesHandler.propertiesEPGListAPIParams.isEmpty()
                || PropertiesHandler.propertiesEPGListAPIParams.entrySet().isEmpty()) {
            return false;
        }


        optionRequestParams.put("startDate", params.startDate);
        optionRequestParams.put("level", params.level);
        optionRequestParams.put("imageProfile", params.imageProfile);
        optionRequestParams.put("startIndex", String.valueOf(params.startIndex));
        if (params.flagEnablePastEpg) {
            optionRequestParams.put("dvr", PARAM_PAST_EPG_TRUE);
        }

        for (Map.Entry<String, String> entry : PropertiesHandler.propertiesEPGListAPIParams.entrySet()) {
            //Log.d(TAG, "fillDynamicParamsIfAvailable: entry.key- " + entry.getKey() + " entry.value- " + entry.getValue());
            if (entry.getValue() != null
                    && entry.getKey() != null) {
                if (entry.getValue().equals("#genre")) {
                    // return content genre
                    optionRequestParams.put(entry.getKey(), params.genre);
                } else if (entry.getValue().equals("#language")) {
                    // return content language
                    optionRequestParams.put(entry.getKey(), params.language);
                } else if (entry.getValue().equals("#count")) {
                    // return content language
                    optionRequestParams.put(entry.getKey(), String.valueOf(params.count));
                } else if (entry.getValue().equals("#mcc")) {
                    // return content language
                    optionRequestParams.put(entry.getKey(), params.mcc);
                } else if (entry.getValue().equals("#mnc")) {
                    // return content language
                    optionRequestParams.put(entry.getKey(), params.mnc);
                } else if (entry.getValue().equals("#orderBy")) {
                    // return content language
                    optionRequestParams.put(entry.getKey(), params.mnc);
                } else {
                    optionRequestParams.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return true;
    }

}
