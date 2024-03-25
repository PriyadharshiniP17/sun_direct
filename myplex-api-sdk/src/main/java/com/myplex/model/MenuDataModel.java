package com.myplex.model;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.CarousalInfoRequest;
import com.myplex.api.request.content.CarouselRequest;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samir on 12/11/2015.
 */
public class MenuDataModel {


    private static final String TYPE_CACHE_GUEST = "in_guest";
    private static final String TYPE_CACHE_PREMIUM = "in_premium";
    private boolean isKeyRegenerateRequestMade;

    public static boolean isRefreshScreen = false;

    public boolean isPortraitBannerRequest() {
        return isPortraitBannerRequest;
    }

    public MenuDataModel setPortraitBannerRequest(boolean portraitBannerRequest) {
        isPortraitBannerRequest = portraitBannerRequest;
        return this;
    }

    private boolean isPortraitBannerRequest = false;

    public interface CarouselContentListCallback {

        void onCacheResults(List<CardData> dataList);

        void onOnlineResults(List<CardData> dataList);

        void onOnlineError(Throwable error, int errorCode);
    }

    public interface MenuDataModelCallback{

        void onCacheResults(List<CarouselInfoData> dataList);

        void onOnlineResults(List<CarouselInfoData> dataList);

        void onOnlineError(Throwable error, int errorCode);
    }

    private static final String TAG = MenuDataModel.class.getSimpleName();
    private final String FAILED_MSG = "Failed: ";
    private static final int PAGE_INDEX_PROGRAM_GUIDE = 150;
    private static final Map<String, List<CardData>> sCarouselCache = new HashMap<>();

    private MenuDataModelCallback mMenuDataModelCallback;
    private CarouselContentListCallback mCarouselContentListCallback;

    private void startAsyncTaskInParallel(AsyncTask<Void,Void,Void> task) {
        //Log.d(TAG,"startAsyncTaskInParallel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    public void fetchCarouseldataInAsynTask(final Context mContext, final String pageName, final int startIndex, final int count, final boolean isCacheRequest,
                                            final CarouselContentListCallback carouselContentListCallback,final String serverPublishTime) {
        //Log.d(TAG,"startAsyncTaskInParallel pageName, startIndex, count- " + pageName + " "+ startIndex +" "+ count);
        if(TextUtils.isEmpty(pageName)){
            return;
        }
        startAsyncTaskInParallel(new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                fetchCarouseldata(mContext, pageName, startIndex, count, isCacheRequest,serverPublishTime,carouselContentListCallback);
                return null;
            }
        });
    }
    public void fetchCarouseldata(Context mContext, final String pageName, final int startIndex, int count, boolean isCacheRequest,String serverPublishTime,
                                  CarouselContentListCallback carouselContentListCallback) {
        this.mCarouselContentListCallback = carouselContentListCallback;
        final String loginStatus = PrefUtils.getInstance().getPrefLoginStatus() == null ? TYPE_CACHE_GUEST : TYPE_CACHE_PREMIUM;
        isCacheRequest = false;
        if (isCacheRequest
                && sCarouselCache.containsKey(pageName + "_" + startIndex + loginStatus)) {
            SDKLogger.debug("cache results for carousel name- " + pageName + "_" + startIndex);
            mCarouselContentListCallback.onCacheResults(sCarouselCache.get(pageName + "_" + startIndex + loginStatus));
            return;
        }

        if (TextUtils.isEmpty(pageName)) {
            return;
        }
        String mccAndMNCValues = getMCCAndMNCValues(mContext);
        String mccValue = "";
        String mncValue = "";
        String cacheRequest = "";

        if (mccAndMNCValues != null && mccAndMNCValues.length() > 0) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }
        CarouselRequest.Params carouselParams = null;
        carouselParams = new CarouselRequest.Params(pageName, startIndex,
                count, mccValue, mncValue,serverPublishTime);
        fetchCarouselAPI(carouselParams, pageName, startIndex, loginStatus);
    }

    public void fetchCarouseldata(Context mContext, final String pageName, final int startIndex, int count, boolean isCacheRequest,String serverPublishTime, String globalServiceId,
                                  CarouselContentListCallback carouselContentListCallback) {
        this.mCarouselContentListCallback = carouselContentListCallback;
        final String loginStatus = PrefUtils.getInstance().getPrefLoginStatus() == null ? TYPE_CACHE_GUEST : TYPE_CACHE_PREMIUM;
         isCacheRequest = false;
        if (isCacheRequest
                && sCarouselCache.containsKey(pageName + "_" + startIndex + loginStatus)) {
            SDKLogger.debug("cache results for carousel name- " + pageName + "_" + startIndex);
            mCarouselContentListCallback.onCacheResults(sCarouselCache.get(pageName + "_" + startIndex + loginStatus));
            return;
        }

        if (TextUtils.isEmpty(pageName)) {
            return;
        }
        String mccAndMNCValues = getMCCAndMNCValues(mContext);
        String mccValue = "";
        String mncValue = "";
        String cacheRequest = "";

        if (mccAndMNCValues != null && mccAndMNCValues.length() > 0) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }
        CarouselRequest.Params carouselParams = null;
        if(globalServiceId != null)
            carouselParams = new CarouselRequest.Params(pageName, startIndex,
                count, mccValue, mncValue,serverPublishTime,globalServiceId);
        else
            carouselParams = new CarouselRequest.Params(pageName, startIndex,
                    count, mccValue, mncValue,serverPublishTime);
        fetchCarouselAPI(carouselParams, pageName, startIndex, loginStatus);

    }

    public void fetchCarouselAPI(CarouselRequest.Params carouselParams, final String pageName, final int startIndex, String loginStatus){
        CarouselRequest carouselRequest = new CarouselRequest(carouselParams, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                if (mCarouselContentListCallback == null) {
                    //Log.d(TAG, "fetchCarouseldata: callback reference is null");
                    return;
                }
                if (null == response || response.body() == null || response.body().results == null) {
                    //Log.d(TAG, "fetchCarouseldata: response is null or response body is null can't process response");
                    mCarouselContentListCallback.onOnlineResults(null);
                    return;
                }
                //Log.d(TAG, "fetchCarouseldata: status: " + response.body().status + " pageName- " + pageName);
                if( response.body().results.size()>0 && !TextUtils.isEmpty(response.body().trackingId)){
                    for(CardData cardData :  response.body().results){
                        cardData.trackingID = response.body().trackingId;
                    }
                }
                sCarouselCache.put(pageName + "_" + startIndex + loginStatus, response.body().results);
                mCarouselContentListCallback.onOnlineResults(response.body().results);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "fetchCarouseldata: " + FAILED_MSG + t + " pageName- " + pageName);
                if(errorCode == APIRequest.ERR_NO_NETWORK)
                    isRefreshScreen = true;

                if (null != mCarouselContentListCallback) {
                    mCarouselContentListCallback.onOnlineError(t, errorCode);
                }
            }
        }).setPortraitBannerRequest(isPortraitBannerRequest);
        APIService.getInstance().execute(carouselRequest);
    }



    public void fetchMenuList(final String group, final int startIndex, int menuCarouselInfoVer, MenuDataModelCallback menuDataModelCallback) {
        mMenuDataModelCallback = menuDataModelCallback;
        //Content list call
        CarousalInfoRequest.Params params = new CarousalInfoRequest.Params(group,menuCarouselInfoVer);
//        CarousalInfoRequest.Params params = new CarousalInfoRequest.Params(null);

        CarousalInfoRequest mRequestRequestContentList = new CarousalInfoRequest(params, new APICallback<CarouselInfoResponseData>() {
            @Override
            public void onResponse(APIResponse<CarouselInfoResponseData> response) {
                if (response == null || response.body() == null) {
                    mMenuDataModelCallback.onOnlineResults(null);
                    return;
                }
                if (mMenuDataModelCallback == null) {
                    return;
                }
                if (!APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                    mMenuDataModelCallback.onOnlineError(new Throwable(response.body().status), response.body().code);
                    return;
                }

                if (response.body().results == null) {
                    mMenuDataModelCallback.onOnlineResults(null);
                    return;
                }
                //Log.d(TAG, "fetchMenuList: status: " + response.body().status + " group- " + group);
                if (response.body().results != null
                        && response.body().results.size() < 0) {
                    mMenuDataModelCallback.onOnlineResults(null);

                }
//                sCarouselCache.put(group + "_" + startIndex, response.body().results);
                mMenuDataModelCallback.onOnlineResults(response.body().results);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "fetchMenuList: onResponse: t- " + t + " group- " + group);
                if (null != mMenuDataModelCallback) {
                    mMenuDataModelCallback.onOnlineError(t, errorCode);
                }
            }
        });
        APIService.getInstance().execute(mRequestRequestContentList);


    }


    public static void setSelectedMoviesCarouselList(String title, List<CardData> moviesList) {
        sCarouselCache.put(title, moviesList);
    }


    public String getMCCAndMNCValues(Context mContext) {
        String codes = "";
        if (mContext == null) {
            return codes;
        }
        TelephonyManager tel = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        if (networkOperator != null && !networkOperator.isEmpty()) {
            try {
                int mcc = Integer.parseInt(networkOperator.substring(0, 3));
                int mnc = Integer.parseInt(networkOperator.substring(3));
                codes = mcc + "," + mnc;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return codes;
    }

    public static void clearCache(){
        sCarouselCache.clear();
    }

}
