package com.myplex.api.request.content;


import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CardData;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is used to show the inline search Results
 * <p></p>
 * Here we execute the API call, and add the response to listener
 * <P></P>
 * Handle the Success and error cases.
 *<p></p>
 * <p/>
 * For example,
 * <p/>
 * <pre>{@code
 *  InlineSearch inlineSearchRequest = new InlineSearch(getActivity(), new InlineSearch.Params("searchQuery","live","dynamic","count"), new APICallback<CardResponseData>() {
 *
 *  @Override public void onResponse(APIResponse<CardResponseData> response) {
 *
 *  }
 *  @Override public void onFailure(Throwable t, int errorCode) {
 *
 *  }
 *  });
 *  APIService.getInstance().execute(inlineSearchRequest);
 * }</pre>
 * Created by Srikanth on 1/29/2015.
 */
public class CarouselRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public boolean isPortraitBannerRequest() {
        return isPortraitBannerRequest;
    }

    public CarouselRequest setPortraitBannerRequest(boolean portraitBannerRequest) {
        isPortraitBannerRequest = portraitBannerRequest;
        return this;
    }

    private boolean isPortraitBannerRequest = false;

    public static class Params {

        String title;
        int count;
        int startIndex;
        public String mnc;
        public String mcc;
        String serverModifiedTime;
        String globalServiceId;

        public Params(String title,int startIndex, int count,String mcc,String mnc,String serverModifiedTime){
            this.title = title;
            this.count = count;
            this.startIndex = startIndex;
            this.mcc = mcc;
            this.mnc = mnc;
            this.serverModifiedTime = serverModifiedTime;
        }

        public Params(String title,int startIndex, int count,String mcc,String mnc,String serverModifiedTime, String globalServiceId){
            this.title = title;
            this.count = count;
            this.startIndex = startIndex;
            this.mcc = mcc;
            this.mnc = mnc;
            this.serverModifiedTime = serverModifiedTime;
            this.globalServiceId = globalServiceId;
        }

      /*  public Params(String title,int startIndex, int count,String mcc,String mnc, String cacheControl,String serverModifiedTime){
            this.title = title;
            this.count = count;
            this.startIndex = startIndex;
            this.mcc = mcc;
            this.mnc = mnc;
            this.serverModifiedTime = serverModifiedTime;
        }*/
    }

    public CarouselRequest(Params params, APICallback<CardResponseData> mListener) {
        super(mListener);
        this.params = params;
    }

    /**
     * {@link com.myplex.api.APIService}
     * this method is executed from APIService to execute fetch the search results.
     * @param myplexAPI  Instance of myplexAPI class
     */
    @Override
    protected void execute(myplexAPI myplexAPI) {
        // Send the request
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
//        //Log.d(TAG,"clientKey=" + clientKey);

//            TODO  Add login check with fields
        String fields = APIConstants.PARAM_CAROUSEL_API_FIELDS;
        if (isPortraitBannerRequest) {
            fields = APIConstants.PARAM_CAROUSEL_API_FIELDS_FOR_FAVORITES_INFO;
        }
        if (myplexAPISDK.PARAM_TO_SEND_ALL_PACKAGES_FIELD) {
            fields = fields + "," + APIConstants.ALLPACKAGES;
        }
        Call<CardResponseData> contentListAPICall;
       /* if(params.cacheControl == null){
//            ,user/currentdata,packages
            /*,"deviceMax"
            contentListAPICall = myplexAPI.getInstance().myplexAPIService
                    .carouselRequest(clientKey, params.title, fields,
                            params.count,params.startIndex, params.mcc, params.mnc,params.serverModifiedTime);
        }else{
            /*user/currentdata,packages*/
            /*,"deviceMax"
            contentListAPICall = myplexAPI.getInstance().myplexAPIService
                    .carouselRequest(clientKey, params.title, fields,
                            params.count,params.startIndex, params.mcc, params.mnc, params.cacheControl,params.serverModifiedTime);
        }
                            params.count,params.startIndex, params.mcc, params.mnc, params.cacheControl);
        }*/

        String url = APIConstants.getCarouselAPIUrl(myplexAPISDK.getApplicationContext(), params.title, fields, params.count, params.startIndex, params.mcc, params.mnc,params.serverModifiedTime, params.globalServiceId);

        SDKLogger.debug("url:: " + url);
        String appLanguage = PrefUtils.getInstance().getAppLanguageToSendServer();
        if(PrefUtils.getInstance().getAppLanguageToShow() != null && !PrefUtils.getInstance().getAppLanguageToShow().isEmpty())
            appLanguage = PrefUtils.getInstance().getAppLanguageToShow();
        List<String> subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
        String packLanguage = "";
        if(subscribed_languages != null && subscribed_languages.size() > 0 && subscribed_languages.get(0) != null)
            packLanguage = subscribed_languages.get(0);

        contentListAPICall = myplexAPI.getInstance().myplexAPIService
                .carouselRequest(url, appLanguage, clientKey,packLanguage);

        contentListAPICall.enqueue(new Callback<CardResponseData>() {
            @Override
            public void onResponse(Call<CardResponseData> call, Response<CardResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                if(isPortraitBannerRequest()) {
                    if (response != null && response.raw() != null && response.raw().cacheResponse() != null) {
                        // true: response was served from cache
                        SDKLogger.debug("response from retrofit cache");
//                        if (response.body() != null && response.body().results != null) {
//                            List<CardData> watchListItems = PrefUtils.getInstance().getWatchListItems();
//                            if (watchListItems != null && watchListItems.size() > 0) {
//                                if (response.body().results != null && response.body().results.size() > 0) {
//                                    for (CardData item : watchListItems) {
//                                        String item_id = item._id;
//                                        if (TextUtils.isEmpty(item_id))
//                                            continue;
//                                        for (int j = 0; j < response.body().results.size(); j++) {
//                                            String _id = response.body().results.get(j)._id;
//                                            if (TextUtils.isEmpty(_id))
//                                                break;
//                                            if (_id.equalsIgnoreCase(item_id)) {
//                                                if (response.body().results.get(j).currentUserData != null && item.currentUserData != null) {
//                                                    response.body().results.get(j).currentUserData.favorite = item.currentUserData.favorite;
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
                    }
                    if (response.raw().networkResponse() != null) {
                        // true: response was served from network/server
                        SDKLogger.debug("response from network/server");
                        //PrefUtils.getInstance().setWatchlistItems(new ArrayList<CardData>());
                        PrefUtils.getInstance().setWatchlistItemsFromServer(true);
                    }else{
                        PrefUtils.getInstance().setWatchlistItemsFromServer(false);
                    }
                }
                apiResponse.setSuccess(response.isSuccessful());
                CarouselRequest.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if(t instanceof UnknownHostException
                        || t instanceof ConnectException){
                    CarouselRequest.this.onFailure(t,ERR_NO_NETWORK);
                    return;
                }
                CarouselRequest.this.onFailure(t, ERR_UN_KNOWN);
            }

        });



    }

}
