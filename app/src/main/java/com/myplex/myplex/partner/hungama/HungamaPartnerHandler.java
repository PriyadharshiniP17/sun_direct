package com.myplex.myplex.partner.hungama;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.PartnerSignUpRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.PublishingHouse;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.Util;

import java.util.Iterator;
import java.util.Map;
import static com.myplex.myplex.ApplicationController.getApplicationConfig;
import static com.myplex.myplex.ApplicationController.getDownloadData;


/**
 * Created by Srikanth on 02-Aug-16.
 */
public class HungamaPartnerHandler {

    private static final String TAG = HungamaPartnerHandler.class.getSimpleName();
    private static HungamaPartnerHandler hungamaPatnerHandler;
    private static Context mContext;
    private SignUpListener mSignUpListener;
    private FragmentCardDetailsDescription.DownloadStatusListener mDownloadStatusListener;
    private ContentDownloadEvent event;


    private HungamaPartnerHandler(Context context) {
        mContext = context;
    }

    public static synchronized HungamaPartnerHandler getInstance(Context context) {
        if (hungamaPatnerHandler == null) {
            hungamaPatnerHandler = new HungamaPartnerHandler(context);
        }
        if (isContextExpired(mContext, context)) {
            hungamaPatnerHandler = new HungamaPartnerHandler(context);
            LoggerD.debugDownload("over riding the instanse with Acitivity context");
        }
        return hungamaPatnerHandler;
    }

    private static boolean isContextExpired(Context mContext, Context newContext) {
        return (mContext != null
                && mContext instanceof Application
                && newContext instanceof Activity)
                || (mContext != null
                && mContext instanceof Activity
                && ((Activity) mContext).isFinishing()
                && newContext != null);
    }


    public void destroy() {
        hungamaPatnerHandler = null;
        LoggerD.debugDownload("destroy this");
    }

    public interface SignUpListener {
        void onSignUpSuccess(int code, String message);

        void onSignUpFailure();
    }

    public void doSignUP(String partnerName, SignUpListener signUpListener) {
        mSignUpListener = signUpListener;
        PartnerSignUpRequest.Params params = new PartnerSignUpRequest.Params(partnerName);
        PartnerSignUpRequest userProfileRequest = new PartnerSignUpRequest(params, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response == null || response.body() == null) {
                    //Log.d(TAG, "doSignUP onResponse null ");
                    mSignUpListener.onSignUpFailure();
                    return;
                }
                //Log.d(TAG, "doSignUP onResponse code- & message " + response.body().code + " & " + response.body().message);
                mSignUpListener.onSignUpSuccess(response.body().code, response.body().message);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "doSignUP onFailure errorCode " + errorCode);
                mSignUpListener.onSignUpFailure();
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }

    public static final void launchDetailsPage(final CardData cardData,
                                               final Context mContext, CarouselInfoData carouselInfoData, String nid,int parentPosition){
        if (cardData != null
                && cardData.generalInfo != null
                && !TextUtils.isEmpty(cardData.generalInfo.partnerId)) {
            /*CONTENT_TYPE contentType = CONTENT_TYPE.MOVIE;
            if (APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type)) {
                contentType = CONTENT_TYPE.TV;
            } else if (APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(cardData.generalInfo.type)) {
                contentType = CONTENT_TYPE.MUSIC_VIDEO;
            }*/
//            PlayUtils.SetNotification(mContext, cardData.generalInfo.partnerId, cardData.generalInfo.title, contentType);

            CacheManager.setSelectedCardData(cardData);
            Bundle args = new Bundle();
            if (cardData.generalInfo != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
                args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, cardData);
                args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
            }
            args.putString(CardDetails.PARAM_CARD_ID, cardData._id);
            args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
            args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
            String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
            args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);

            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_SIMILAR_CONTENT);
            args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
            if (nid != null) {
                if (!Analytics.VALUE_SOURCE_BANNER.equalsIgnoreCase(nid)) {
                    args.putString(APIConstants.NOTIFICATION_PARAM_NID, nid);
                    args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_NOTIFICATION);
                }
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, nid);
            }
            if (carouselInfoData != null)
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);

            ((BaseActivity) mContext).showDetailsFragment(args, cardData);
        }
    }

    public static final void launchDetailsPage(final CardData cardData, final Context mContext, CarouselInfoData carouselInfoData, String nid) {
        if (cardData != null
                && cardData.generalInfo != null
                && !TextUtils.isEmpty(cardData.generalInfo.partnerId)) {
            /*CONTENT_TYPE contentType = CONTENT_TYPE.MOVIE;
            if (APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type)) {
                contentType = CONTENT_TYPE.TV;
            } else if (APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(cardData.generalInfo.type)) {
                contentType = CONTENT_TYPE.MUSIC_VIDEO;
            }*/
//            PlayUtils.SetNotification(mContext, cardData.generalInfo.partnerId, cardData.generalInfo.title, contentType);

            CacheManager.setSelectedCardData(cardData);
            Bundle args = new Bundle();
            if (cardData.generalInfo != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
                args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, cardData);
                args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
            }
            args.putString(CardDetails.PARAM_CARD_ID, cardData._id);
            args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
            args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
            String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
            args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);

            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_SIMILAR_CONTENT);
            if (nid != null) {
                if (!Analytics.VALUE_SOURCE_BANNER.equalsIgnoreCase(nid)) {
                    args.putString(APIConstants.NOTIFICATION_PARAM_NID, nid);
                    args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_NOTIFICATION);
                }
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, nid);
            }
            if (carouselInfoData != null)
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);

            ((BaseActivity) mContext).showDetailsFragment(args, cardData);
        }
    }

    public synchronized void startDownload(final ContentDownloadEvent event, final FragmentCardDetailsDescription.DownloadStatusListener downloadStatusListener) {
        this.mDownloadStatusListener = downloadStatusListener;
        this.event = event;
    }

    private void notifyError(String msg) {
        if (mDownloadStatusListener != null)
            mDownloadStatusListener.onFailure(msg);
    }

    private void handleState(String contentId, int state, ContentDownloadEvent event) {
        if (event != null && contentId.equalsIgnoreCase(event.cardData.generalInfo.partnerId)) {
            switch (state) {

            }
            return;
        }


    }


    private void updateDownloadData(final String downloadKey, final float progress) {
        LoggerD.debugDownload("----------*****----------");
        if (downloadKey == null) {
            LoggerD.debugDownload("downloadKey is null");
            return;
        }
        final CardDownloadedDataList downloadlist = getDownloadData();
        CardData cardData = new CardData();
        //CardDataGeneralInfo for CardData
        CardDataGeneralInfo generalInfo = new CardDataGeneralInfo();
        generalInfo.partnerId = downloadKey;
        cardData.generalInfo = generalInfo;
        cardData.publishingHouse = new PublishingHouse();
        cardData.publishingHouse.publishingHouseName = APIConstants.TYPE_HUNGAMA;
        CardDownloadData cardDownloadData = DownloadUtil.getDownloadDataFromDownloads(cardData);
        if (cardDownloadData == null  || (cardDownloadData.mPercentage == 100 && cardDownloadData.mCompleted)) {
            LoggerD.debugDownload("carddownloadData is null or download completed downloadKey- " + downloadKey);
            return;
        }

        cardDownloadData.mPercentage = (int) progress;
        if (progress == 100) {
            cardDownloadData.mCompleted = true;
            cardDownloadData.mPercentage = 100;
            if (!cardDownloadData.isNotificationShown) {
                cardDownloadData.isNotificationShown = true;
                DownloadUtil.showNotification(mContext, cardDownloadData._id, cardDownloadData);
            }
            LoggerD.debugDownload("showNotification downloadedData- " + cardDownloadData);
            ApplicationController.setDownloadData(downloadlist);
            SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
        }
        downloadlist.mDownloadedList.put(cardDownloadData.downloadKey, cardDownloadData);
        LoggerD.debugDownload("put downloadKey- " + cardDownloadData.downloadKey + " progress- " + progress);
        LoggerD.debugDownload("----------*****----------");
    }



    public void deleteDownload(String downloadKey) {
        LoggerD.debugDownload("----------*****----------");
        if (downloadKey == null) {
            LoggerD.debugDownload("downloadKey is null");
            return;
        }
        final CardDownloadedDataList downloadlist = getDownloadData();
        CardData cardData = new CardData();
        //CardDataGeneralInfo for CardData
        CardDataGeneralInfo generalInfo = new CardDataGeneralInfo();
        generalInfo.partnerId = downloadKey;
        cardData.generalInfo = generalInfo;
        cardData.publishingHouse = new PublishingHouse();
        cardData.publishingHouse.publishingHouseName = APIConstants.TYPE_HOOQ;
        CardDownloadData cardDownloadData = DownloadUtil.getDownloadDataFromDownloads(cardData);
        if (cardDownloadData == null) {
            LoggerD.debugDownload("carddownloadData is null or download completed downloadKey- " + downloadKey);
            return;
        }
        LoggerD.debugDownload("downloadKey is removed from downloads");
        downloadlist.mDownloadedList.remove(cardDownloadData.downloadKey);
        ApplicationController.setDownloadData(downloadlist);
        SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
        LoggerD.debugDownload("put downloadKey- " + cardDownloadData.downloadKey);
        LoggerD.debugDownload("----------*****----------");
    }

    public void checkDownloadsProgress() {
        if (mContext == null) {
            return;
        }
        CardDownloadedDataList downloadedDataList = ApplicationController.getDownloadData();
        if (downloadedDataList == null) {
            return;
        }
        Iterator it = downloadedDataList.mDownloadedList.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            CardDownloadData cardDownloadData = (CardDownloadData) entry.getValue();
            if (APIConstants.isHungamaContent(downloadedDataList.mDownloadedList.get(cardDownloadData.downloadKey))) {
                int progress = 30;
                LoggerD.debugDownload("download tilte- " + cardDownloadData.title + " progress- " + progress);
                updateDownloadData(cardDownloadData.downloadKey, progress);
            }
        }
//        for (String key : downloadedDataList.mDownloadedList.keySet()) {
//            if (APIConstants.isHungamaContent(downloadedDataList.mDownloadedList.get(key))) {
//                int progress = PlayUtils.getDownloadProgress(mContext, key);
//                LoggerD.debugDownload("download tilte- " + downloadedDataList.mDownloadedList.get(key).title + " progress- " + progress);
//                updateDownloadData(key, progress);
//            }
//        }
    }
}
