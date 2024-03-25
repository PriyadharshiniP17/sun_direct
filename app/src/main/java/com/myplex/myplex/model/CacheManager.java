package com.myplex.myplex.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.CarouselRequest;
import com.myplex.api.request.content.ContentDetails;
import com.myplex.api.request.content.RequestRelatedVODList;
import com.myplex.api.request.epg.ChannelEPG;
import com.myplex.api.request.epg.ProgramDetails;
import com.myplex.model.CardData;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.analytics.Analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samir on 12/11/2015.
 */
public class CacheManager {

    private static final String TAG = "CacheManager";
    private static CardData sRelatedVODsData;
    private static List<CarouselInfoData> sCarouselInfoDataList;
    private static List<CardData> sCardDataList;
    private final String FAILED_MSG = "Failed: ";
    private static final int PAGE_INDEX_PROGRAM_GUIDE = 150;

    private static final Map<String, List<CardData>> sCardDetailsCache = new HashMap<>();
    private static final Map<String, List<CardData>> sRelatedVODsCache = new HashMap<>();
    private static final Map<String, List<CardData>> sCarouselCache = new HashMap<>();
    private static final HashMap<String, List<CardData>> sCacheCurrentUserData = new HashMap<>();
    private CacheManagerCallback mListener;
    private static CardData sSelectedData;

    private String _id;
    private String channelId;
    private String mNid;
    private String mNotificationTitle;
    private static CarouselInfoData sCarouselInfoData;

    public static void setCarouselInfoData(CarouselInfoData carouselInfoData) {
        CacheManager.sCarouselInfoData = carouselInfoData;
    }

    public static CarouselInfoData getCarouselInfoData() {
        return CacheManager.sCarouselInfoData;
    }

    public static void setCarouselInfoDataList(List<CarouselInfoData> carouselInfoDataList) {
        CacheManager.sCarouselInfoDataList = carouselInfoDataList;
    }

    public static List<CarouselInfoData> getCarouselInfoDataList() {
        return CacheManager.sCarouselInfoDataList;
    }

    public static void setCardDataList(List<CardData> mListMovies) {
        CacheManager.sCardDataList = mListMovies;
    }

    public static List<CardData> getCardDataList() {
        return sCardDataList;
    }


    public void getCarouseldata(Context mContext, String title, int startIndex,boolean isCacheRequest,String serverModifiedTime,
                                CacheManagerCallback cacheManagerCallback) {
        mListener = cacheManagerCallback;
        if (sCarouselCache.containsKey(title + "_" + startIndex) && isCacheRequest) {
            mListener.OnCacheResults(sCarouselCache.get(title + "_" + startIndex));
            return;
        }

        loadCarouselData(mContext, title, startIndex, isCacheRequest, serverModifiedTime);
    }

    private void loadCarouselData(Context mContext, final String title, final int startIndex, boolean isCacheRequest,String serverModifiedTime) {
        String mccAndMNCValues = SDKUtils.getMCCAndMNCValues(mContext);
        String mccValue = "";
        String mncValue = "";
        String cacheRequest = "";

        if(mccAndMNCValues!=null && mccAndMNCValues.length()>0){
            String []mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }
        CarouselRequest.Params carouselParams = null;
        carouselParams = new CarouselRequest.Params(title,startIndex,
                APIConstants.PAGE_INDEX_COUNT, mccValue, mncValue,serverModifiedTime);
        isCacheRequest = false;
        if(!isCacheRequest){
            carouselParams = new CarouselRequest.Params(title,startIndex,
                    APIConstants.PAGE_INDEX_COUNT, mccValue, mncValue, serverModifiedTime);
        }

        CarouselRequest carouselRequest = new CarouselRequest(carouselParams, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                if (mListener == null) {
                    onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                if (null == response || response.body() == null) {
                    onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                //Log.d(TAG, "status: " + response.body());
                if (response.body().results == null
                        || response.body().results.isEmpty()) {
                    mListener.OnOnlineResults(null);

                }
                sCarouselCache.put(title + "_" + startIndex, response.body().results);
                mListener.OnOnlineResults(response.body().results);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, FAILED_MSG + t);
                if(null != mListener){
                    mListener.OnOnlineError(t,errorCode);
                }
            }
        });
        APIService.getInstance().execute(carouselRequest);
    }

    public static void setVODSuperCardData(CardData mRelatedVODData) {
        sRelatedVODsData = mRelatedVODData;
    }

    public static CardData getVODSuperCardData() {
        return sRelatedVODsData;
    }

    public interface CacheManagerCallback {
        void OnCacheResults(List<CardData> dataList);

        void OnOnlineResults(List<CardData> dataList);

        void OnOnlineError(Throwable error, int errorCode);
    }

    public static void setSelectedCardData(CardData selectedData){
        sSelectedData = selectedData;
    }


    public static void setSelectedMoviesCarouselList(String title, List<CardData> moviesList){
        sCarouselCache.put(title, moviesList);
    }


    public void getCardDetails(String _ids, boolean isCacheRequest, final CacheManagerCallback listener){
        if (listener == null) return;
        this._id = _ids;
        this.mListener = listener;
//        isCacheRequest = false;
        if(isCacheRequest){
            if (sSelectedData != null) {
                if (isProgram(sSelectedData)
                        && sSelectedData.globalServiceId != null
                        && sSelectedData.globalServiceId.equals(_ids)) {
                    List<CardData> selectedData = new ArrayList<>();
                    selectedData.add(sSelectedData);
                    mListener.OnCacheResults(selectedData);
                    return;
                } else if (sSelectedData._id != null
                        && sSelectedData._id.equals(_ids)) {
                    List<CardData> selectedData = new ArrayList<>();
                    selectedData.add(sSelectedData);
                    mListener.OnCacheResults(selectedData);
                    return;
                }
            }
            if (_id != null) {
                if (sCardDetailsCache.containsKey(_id)) {
                    if (mListener != null) {
                        mListener.OnCacheResults(sCardDetailsCache.get(_id));
                        return;
                    }
                }
            }

        }

        ContentDetails.Params contentDetailsParams = new ContentDetails.Params(_id,"mdpi",
                "coverposter",10);
        executeContentDetailRequest(contentDetailsParams, false);
    }

    public void getSubscriptionPackages(String _id, boolean isCacheRequest, boolean isCurrentUserDataRequest, final CacheManagerCallback listener){
        if (listener == null || _id == null) return;
        this.mListener = listener;
        if(sCacheCurrentUserData != null && sCacheCurrentUserData.containsKey(_id) && isCacheRequest){
            if (mListener != null) {
                mListener.OnCacheResults(sCacheCurrentUserData.get(_id));
                return;
            }
        }

        ContentDetails.Params contentDetailsParams = new ContentDetails.Params
                (_id, "mdpi",
                        "coverposter", 10, APIConstants.HTTP_NO_CACHE, APIConstants.PARAM_CONTENT_DETAILS_CURRENT_USERDATA_FIELDS);

        executeContentDetailRequest(contentDetailsParams, isCurrentUserDataRequest);
    }

    private void executeContentDetailRequest(ContentDetails.Params contentDetailsParams, final boolean isCurrentUserDataRequest) {

        final ContentDetails contentDetails = new ContentDetails(contentDetailsParams,
                new APICallback<CardResponseData>() {
                    String mAPIErrorMessage = null;

                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        if(null == mListener){
                            onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        if(response == null || response.body() == null){
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }

                        mAPIErrorMessage = response.body().message;
                        if(response.body().results == null || response.body().results.size() <= 0){
                            onFailure(new Throwable(APIConstants.ERROR_EPMTY_RESULTS), APIRequest.ERR_UN_KNOWN);
                            return;
                        }

                        //Log.d(TAG, "status: " + response.body());
                        if(!isCurrentUserDataRequest){
                            sCardDetailsCache.put(_id, response.body().results);
                        }else{
                            sCacheCurrentUserData.put(_id, response.body().results);
                        }
                        mListener.OnOnlineResults(response.body().results);
                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, FAILED_MSG + t);
                        if(null != mListener){
                            mListener.OnOnlineError(t,errorCode);
                        }
                        String errorMessage = null;
                        String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                        if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                            reason = mAPIErrorMessage;
                        }
                        Analytics.mixPanelUnableToFetchVideoLink(null, null, _id == null ? APIConstants.NOT_AVAILABLE : _id, mNid, reason, mNotificationTitle);
                    }
                });

        APIService.getInstance().execute(contentDetails);

    }

    private boolean isProgram(CardData sSelectedData) {
        return sSelectedData != null
                && sSelectedData.generalInfo != null
                && sSelectedData.generalInfo.type != null
                && sSelectedData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM);
    }


    public void getEPGChannelData(final String _id, final String date, boolean isCacheRequest,
                                  CacheManagerCallback listener){
        //EPG list call
        if(listener == null){
            return;
        }
        this.channelId = _id;
        this.mListener = listener;
        if(isCacheRequest){
            if(mListener != null
                    && sRelatedVODsCache != null
                    && sRelatedVODsCache.containsKey(_id + date)){
                    mListener.OnCacheResults(sRelatedVODsCache.get(_id+date));
                    return;
            }
        }

        //Log.d(TAG,"getEPGChannelData() _id- " + channelId);
        ChannelEPG.Params channelEPGparams = new ChannelEPG.Params(channelId,date,
                "epgstatic",
                "mdpi",PAGE_INDEX_PROGRAM_GUIDE,1);

        final ChannelEPG channelEPG = new ChannelEPG(channelEPGparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        if(mListener == null){
                            return;
                        }
                        if(null == response
                                || response.body() == null) {
                            mListener.OnOnlineResults(null);
                            return;
                        }
                        //Log.d(TAG, "status: " + response.body().status);
                        if(response.body().results == null
                                || response.body().results.isEmpty()){
                            mListener.OnOnlineResults(null);
                            return;
                        }
                        sRelatedVODsCache.put(_id + date, response.body().results);
                        mListener.OnOnlineResults(response.body().results);

                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, FAILED_MSG + t);
                        if (mListener != null) {
                            mListener.OnOnlineError(t, errorCode);
                        }
                    }
                });
        APIService.getInstance().execute(channelEPG);
    }

    public void getProgramDetail(final String _id, boolean isCacheRequest,
                                 CacheManagerCallback listener){
        //EPG list call
        if(listener == null){
            return;
        }
        this.mListener = listener;
        if(isCacheRequest){
            if(sSelectedData != null
                    && isProgram(sSelectedData)){
                    if (mListener != null
                            && sSelectedData != null
                            && sSelectedData.globalServiceId != null
                            && sSelectedData.globalServiceId.equals(_id)) {
                            List<CardData> selectedData = new ArrayList<>();
                            selectedData.add(sSelectedData);
                            mListener.OnCacheResults(selectedData);
                            return;
                    }
            }
        }

        ProgramDetails.Params programDetailParams = new ProgramDetails.Params(_id,"static");

        final ProgramDetails programDetailsRequest = new ProgramDetails(programDetailParams,
                new APICallback<CardResponseData>() {
                    public String mAPIErrorMessage;
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        if(null == mListener){
                            onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        if(response == null || response.body() == null){
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        mAPIErrorMessage = response.body().message;
                        if(response.body().results == null){
                            onFailure(new Throwable(APIConstants.ERROR_EPMTY_RESULTS), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "status: " + response.body().status);
                        if(response.body().results == null
                                || response.body().results.isEmpty()){
                            mListener.OnOnlineResults(null);
                            return;
                        }
                        mListener.OnOnlineResults(response.body().results);

                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, FAILED_MSG + t);
                        if(mListener != null){
                            mListener.OnOnlineError(t,errorCode);
                        }
                        String errorMessage = null;
                        String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                        if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                            reason = mAPIErrorMessage;
                        }
                        Analytics.mixPanelUnableToFetchVideoLink(null, null, _id == null ? APIConstants.NOT_AVAILABLE : _id, mNid, reason, mNotificationTitle);
                    }
                });
        APIService.getInstance().execute(programDetailsRequest);
    }
    private int mResponseResultSize = 0;
    private RequestRelatedVODList mRequestRelatedVODList;
    public void getRelatedVODList(final String _id, final int startIndex, boolean isCacheRequest,
                                  CacheManagerCallback listener){
        LoggerD.debugHooqVstbLog("getRelatedVODList: _id- " +
                _id +" startIndex- "+startIndex);
        this.mListener = listener;
        if(isCacheRequest){
            if(mListener != null
                    && sRelatedVODsCache != null
                    && sRelatedVODsCache.containsKey(_id + "_" + startIndex)){
                   /* //Log.d(TAG, "getRelatedVODList: sRelatedVODsCache contains key- " +
                            _id +"_"+startIndex);*/
                    mResponseResultSize = sRelatedVODsCache.get(_id +"_"+startIndex).size();
                    mListener.OnCacheResults(sRelatedVODsCache.get(_id +"_"+startIndex));
                    return;
            }
        }

        //Log.d(TAG,"getRelatedVODList() _id- " + _id);
        RequestRelatedVODList.Params relatedVODListParams = new RequestRelatedVODList.Params(_id,
                startIndex,
                APIConstants.PAGE_INDEX_COUNT);
        mRequestRelatedVODList = new RequestRelatedVODList(relatedVODListParams, new APICallback<CardResponseData>() {
            String mAPIErrorMessage = null;
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                if(null == mListener){
                    onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                if(response == null || response.body() == null){
                    onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                mAPIErrorMessage = response.body().message;
                if(response.body().results == null){
                    onFailure(new Throwable(APIConstants.ERROR_EPMTY_RESULTS), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                mResponseResultSize = response.body().results.size();
               /* //Log.d(TAG, "getRelatedVODList: onResponse: results- " + response.body().results
                        .size()+"status- "+response.body().status);*/
                List<CardData> relatedVODList = new ArrayList<>();
                for (CardData vodCard : response.body().results) {
                    if (vodCard.generalInfo != null
                            && vodCard.generalInfo.type != null) {
                        if(APIConstants.TYPE_VODYOUTUBECHANNEL.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_VODCATEGORY.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_VODCHANNEL.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_TVSEASON.equals(vodCard.generalInfo.type)){
                            continue;
                        }
                    }
                    /*if(!vodCard.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)){
                        if (vodCard.videos != null
                                && vodCard.videos.values != null
                                && vodCard.videos.values.size() == 0) {
                            // listener.onErrorResponse(mContext.getResources().getString(R.string.no_related_video));
                            continue;
                        }

                    }*/
                    relatedVODList.add(vodCard);
                }
                sRelatedVODsCache.put(_id + "_" + startIndex, relatedVODList);
                mListener.OnOnlineResults(relatedVODList);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, FAILED_MSG + t);
                if(null != mListener){
                    mListener.OnOnlineError(t,errorCode);
                }
                String errorMessage = null;

                String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                    reason = mAPIErrorMessage;
                }
                Analytics.mixPanelUnableToFetchVideoLink(null, null, _id == null ? APIConstants.NOT_AVAILABLE : _id, mNid, reason, mNotificationTitle);

            }
        });
        APIService.getInstance().execute(mRequestRelatedVODList);
    }

    public void getRelatedVODList(final String _id, final int startIndex, boolean isCacheRequest,int count,
                                  CacheManagerCallback listener){
        LoggerD.debugHooqVstbLog("getRelatedVODList: _id- " +
                _id +" startIndex- "+startIndex);
        this.mListener = listener;
       // isCacheRequest = false;
        if(isCacheRequest){
            if(mListener != null
                    && sRelatedVODsCache != null
                    && sRelatedVODsCache.containsKey(_id + "_" + startIndex)){
              /*  //Log.d(TAG, "getRelatedVODList: sRelatedVODsCache contains key- " +
                        _id +"_"+startIndex);*/
                mResponseResultSize = sRelatedVODsCache.get(_id +"_"+startIndex).size();
                mListener.OnCacheResults(sRelatedVODsCache.get(_id +"_"+startIndex));
                return;
            }
        }

        //Log.d(TAG,"getRelatedVODList() _id- " + _id);
        RequestRelatedVODList.Params relatedVODListParams = new RequestRelatedVODList.Params(_id,
                startIndex,
                count,"1","siblingOrder");
        mRequestRelatedVODList = new RequestRelatedVODList(relatedVODListParams, new APICallback<CardResponseData>() {
            String mAPIErrorMessage = null;
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                if(null == mListener){
                    onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                if(response == null || response.body() == null){
                    onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                mAPIErrorMessage = response.body().message;
                if(response.body().results == null){
                    onFailure(new Throwable(APIConstants.ERROR_EPMTY_RESULTS), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                mResponseResultSize = response.body().results.size();
               /* //Log.d(TAG, "getRelatedVODList: onResponse: results- " + response.body().results
                        .size()+"status- "+response.body().status);*/
                List<CardData> relatedVODList = new ArrayList<>();
                for (CardData vodCard : response.body().results) {
                    if (vodCard.generalInfo != null
                            && vodCard.generalInfo.type != null) {
                        if(APIConstants.TYPE_VODYOUTUBECHANNEL.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_VODCATEGORY.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_VODCHANNEL.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_TVSEASON.equals(vodCard.generalInfo.type)){
                            continue;
                        }
                    }
                    /*if(!vodCard.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)){
                        if (vodCard.videos != null
                                && vodCard.videos.values != null
                                && vodCard.videos.values.size() == 0) {
                            // listener.onErrorResponse(mContext.getResources().getString(R.string.no_related_video));
                            continue;
                        }

                    }*/
                    relatedVODList.add(vodCard);
                }
                sRelatedVODsCache.put(_id + "_" + startIndex, relatedVODList);
                mListener.OnOnlineResults(relatedVODList);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, FAILED_MSG + t);
                if(null != mListener){
                    mListener.OnOnlineError(t,errorCode);
                }
                String errorMessage = null;

                String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                    reason = mAPIErrorMessage;
                }
                Analytics.mixPanelUnableToFetchVideoLink(null, null, _id == null ? APIConstants.NOT_AVAILABLE : _id, mNid, reason, mNotificationTitle);

            }
        });
        APIService.getInstance().execute(mRequestRelatedVODList);
    }

    public boolean isLastPage(){
        return mResponseResultSize < APIConstants.PAGE_INDEX_COUNT;
    }

    public void setNotifiationNid(String nid) {
        this.mNid = nid;
    }

    public void setNotifiationTitle(String notificationTitle) {
        this.mNotificationTitle = notificationTitle;
    }

    public void getRelatedVODListTypeExclusion(final String _id, final int startIndex, boolean isCacheRequest,
                                               final String typeExclusion, int count,
                                               CacheManagerCallback listener){
        LoggerD.debugHooqVstbLog("getRelatedVODList: _id- " + _id +
                " startIndex- " + startIndex +
                " typeExclusion- " + typeExclusion);
        this.mListener = listener;
        isCacheRequest = false;
        if (isCacheRequest) {
            if (mListener != null
                    && sRelatedVODsCache != null
                    && sRelatedVODsCache.containsKey(_id + "_" + startIndex)) {
                /*//Log.d(TAG, "getRelatedVODList: sRelatedVODsCache contains key- " +
                        _id + "_" + startIndex);*/
                mResponseResultSize = sRelatedVODsCache.get(_id + "_" + startIndex).size();
                mListener.OnCacheResults(sRelatedVODsCache.get(_id + "_" + startIndex));
                return;
            }
        }

        //Log.d(TAG,"getRelatedVODList() _id- " + _id);
        RequestRelatedVODList.Params relatedVODListParams = new RequestRelatedVODList.Params(_id,
                startIndex,
                count);
        mRequestRelatedVODList = new RequestRelatedVODList(relatedVODListParams, new APICallback<CardResponseData>() {
            String mAPIErrorMessage = null;
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                if(null == mListener){
                    onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                if(response == null || response.body() == null){
                    onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                mAPIErrorMessage = response.body().message;
                if(response.body().results == null){
                    onFailure(new Throwable(APIConstants.ERROR_EPMTY_RESULTS), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                mResponseResultSize = response.body().results.size();
              /*  //Log.d(TAG, "getRelatedVODList: onResponse: results- " + response.body().results
                        .size()+"status- "+response.body().status);*/
                List<CardData> relatedVODList = new ArrayList<>();
                for (CardData vodCard : response.body().results) {
                    if (vodCard.generalInfo != null
                            && vodCard.generalInfo.type != null) {
                        if((APIConstants.TYPE_VODYOUTUBECHANNEL.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_VODCATEGORY.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_VODCHANNEL.equals(vodCard.generalInfo.type)
                                || APIConstants.TYPE_TVSEASON.equals(vodCard.generalInfo.type))
                                && typeExclusion != null && !typeExclusion.equalsIgnoreCase(vodCard.generalInfo.type)){
                            continue;
                        }
                    }
                    /*if(!vodCard.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)){
                        if (vodCard.videos != null
                                && vodCard.videos.values != null
                                && vodCard.videos.values.size() == 0) {
                            // listener.onErrorResponse(mContext.getResources().getString(R.string.no_related_video));
                            continue;
                        }

                    }*/
                    relatedVODList.add(vodCard);
                }
                sRelatedVODsCache.put(_id+"_"+startIndex,relatedVODList);
                mListener.OnOnlineResults(relatedVODList);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, FAILED_MSG + t);
                if(null != mListener){
                    mListener.OnOnlineError(t,errorCode);
                }
                String errorMessage = null;

                String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                    reason = mAPIErrorMessage;
                }
                Analytics.mixPanelUnableToFetchVideoLink(null, null, _id == null ? APIConstants.NOT_AVAILABLE : _id, mNid, reason, mNotificationTitle);

            }
        });
        APIService.getInstance().execute(mRequestRelatedVODList);
    }

    public void getCardDetailsWithFields(String _id, boolean isCacheRequest, String fields, final CacheManagerCallback listener){
        if (listener == null || _id == null) return;
        this.mListener = listener;
        if(sCacheCurrentUserData != null && sCacheCurrentUserData.containsKey(_id) && isCacheRequest){
            if (mListener != null) {
                mListener.OnCacheResults(sCacheCurrentUserData.get(_id));
                return;
            }
        }

        ContentDetails.Params contentDetailsParams = new ContentDetails.Params
                (_id, "mdpi",
                        "coverposter", 10, APIConstants.HTTP_NO_CACHE, fields);

        executeContentDetailRequest(contentDetailsParams, false);
    }
}
