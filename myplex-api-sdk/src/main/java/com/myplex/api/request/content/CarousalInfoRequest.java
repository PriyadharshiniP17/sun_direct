package com.myplex.api.request.content;


import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CarouselInfoResponseData;
import com.myplex.model.PreferredLanguageItem;
import com.myplex.util.PrefUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;
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
public class CarousalInfoRequest extends APIRequest {

    private static final String TAG = "APIService";

    private Params params;

    public static class Params {
        String group;
        public int version;

        public Params(String group, int version) {
            this.group = group;
            this.version = version;
        }
    }

    public CarousalInfoRequest(Params params, APICallback<CarouselInfoResponseData> mListener) {
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
        List<PreferredLanguageItem> items = PrefUtils.getInstance().getPreferredLanguageItems();
        String preferredLanguages = "";
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                preferredLanguages = (i != items.size() - 1) ? preferredLanguages + items.get(i).getTerm() + "," : preferredLanguages + items.get(i).getTerm();
            }
        }
        String appLanguage = PrefUtils.getInstance().getAppLanguageToSendServer();
        if(PrefUtils.getInstance().getAppLanguageToShow() != null && !PrefUtils.getInstance().getAppLanguageToShow().isEmpty())
            appLanguage = PrefUtils.getInstance().getAppLanguageToShow();
        Call<CarouselInfoResponseData> contentListAPICall = null;
        String cacheControl = null;
        if (myplexAPISDK.ENABLE_FORCE_CACHE) {
            cacheControl = APIConstants.HTTP_NO_CACHE;
        }
        List<String> subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
        String packLanguage = "";
        if(subscribed_languages != null && subscribed_languages.size() > 0 && subscribed_languages.get(0) != null)
            packLanguage = subscribed_languages.get(0);

        if (params != null && params.group != null) {
            contentListAPICall = myplexAPI.getInstance().myplexAPIService
                    .carouselInfoRequest(clientKey, params.version, params.group, cacheControl,appLanguage,packLanguage,appLanguage);
        } else {
            contentListAPICall = myplexAPI.getInstance().myplexAPIService
                    .carouselInfoRequest(clientKey, params.version, cacheControl,appLanguage,packLanguage,appLanguage);
        }

        contentListAPICall.enqueue(new Callback<CarouselInfoResponseData>() {
            @Override
            public void onResponse(Call<CarouselInfoResponseData> call, Response<CarouselInfoResponseData> response) {
                APIResponse apiResponse = new APIResponse(response.body(),null);
                if(null != response.body()){
                    apiResponse.setMessage(response.body().message);
                }
                apiResponse.setSuccess(response.isSuccessful());
                CarousalInfoRequest.this.onResponse(apiResponse);

            }

            @Override
            public void onFailure(Call<CarouselInfoResponseData> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    CarousalInfoRequest.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                CarousalInfoRequest.this.onFailure(t, ERR_UN_KNOWN);
            }
        });



    }

}
