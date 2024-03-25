package com.myplex.myplex.analytics;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.github.pedrovgs.LoggerD;
import com.myplex.analytics.AnalyticsConstants;
import com.myplex.analytics.Event;
import com.myplex.analytics.MyplexAnalytics;
import com.myplex.api.APIConstants;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CardData;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.LocationInfo;
import com.myplex.model.PreferredLanguageItem;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.BuildConfig;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.R.attr.action;
import static com.myplex.myplex.analytics.Analytics.EVENT_NOTIFICATION_PLAYED;
import static com.myplex.myplex.analytics.Analytics.EVENT_NOTIFICATION_UNABLE_TO_PLAY;
import static com.myplex.myplex.analytics.Analytics.EventPriority.HIGH;
import static com.myplex.myplex.analytics.Analytics.MAX_PLAYED_TIME_MIN;
import static com.myplex.myplex.analytics.Analytics.PARAM_EMAIL_ID;
import static com.myplex.myplex.analytics.Analytics.PARAM_OTP;
import static com.myplex.myplex.analytics.Analytics.PARAM_OTP_DETECTION;

import static com.myplex.myplex.analytics.Analytics.PARTNER_NAME;
import static com.myplex.myplex.analytics.Analytics.PROPERTY_CONTENT_ID;

import static com.myplex.myplex.analytics.Analytics.PROPERTY_CONTENT_NAME;

import static com.myplex.myplex.analytics.Analytics.PROPERTY_VIDEO_COMPLETED;
import static com.myplex.myplex.analytics.Analytics.REASON_FAILURE;
import static com.myplex.myplex.analytics.Analytics.mCarouselTitle;
import static com.myplex.myplex.analytics.Analytics.mVODCardData;

/**
 * Created by Srikanth on 15-May-17.
 */

public class CleverTap {
    public static final String PROPERTY_LENGTH_OF_VIDEO = "length of the video";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_MOBILE_NETWORK = "mobile network";
    public static final String PROPERTY_CONTENT_PARTNER_NAME = "content partner name";
    public static final String PROPERTY_PER_COMPLETED = "% completed";
    public static final String PROPERTY_DURATION_VIEWED = "duration viewed";
    public static final String PROPERTY_START_POINT = "start point";
    public static final String PROPERTY_CONTENT_TYPE = "content type";
    public static final String PROPERTY_ANDROID_APP_VERSION = "android app version";
    public static final String PROPERTY_ANDROID_MODEL = "android model";
    public static final String PROPERTY_STATE = "state";
    public static final String PAGE_SUPPORT = "support";
    private static final String PROPERTY_CATEGORY = "category";
    private static final String PROPERTY_CONSUMPTION_TYPE = "consumption type";
    private static final String PROPERTY_CG_PAGE_TYPE = "page type";
    private static final String PROPERTY_TYPE = "error type";
    private static final String PROPERTY_ERROR_MSG = "error message";
    private static final String PROPERTY_REASON = "error reason";
    private static final String PROPERTY_STATUS = "status";
    private static final String PROPERTY_DOWNLOAD_FILE_SIZE = "download file size";
    public static final String PROPERTY_DOWNLOAD_STARTED = "started";
    public static final String PROPERTY_DOWNLOAD_DOWNlOADED = "completed";
    public static final String PROPERTY_DOWNLOAD_DELETED = "deleted";
    public static final String PROPERTY_DOWNLOAD_CANCELED = "canceled";
    private static final String PROPERTY_TVSHOW_NAME = "tv show name";
    private static final String PROPERTY_DEVICE_TYPE = "device type";
    private static final String PROPERTY_DEVICE_OS = "os";
    private static final String PROPERTY_DEVICE_MODEL = "device model";
    private static final String USER_PROPERTY_APP_LANGUGAE = "app language";
    private static final String PROPERTY_REGISTERED_USER = "registered user";
    private static final String PROPERTY_HINT = "hint";
    private static final String PROPERTY_PIN = "pin";
    public static final String PROPERTY_TAB = "tab";
    public static final String PROPERTY_APP_LANGUAGE = "app language";
    public static final String PROPERTY_CAROUSEL_POSITION = "carousel position";
    public static final String PROPERTY_CONTENT_POSITION = "content position";
    public static final String EVENT_TAB_VIEWED = "tab viewed";
    private static final String PROPERTY_PREFERRED_LANGUAGE = "preferred language";
    private static final String PROPERTY_LOG_OUT = "logout";
    public static final String PROPERTY_ADD_TO_WATCHLIST = "add to watchlist";
    public static final String PROPERTY_PLAY = "Play";
    public static final String PROPERTY_PROFILE_API_LIST = "profile_api_list";


    public static final String INCORRECT_PIN = "incorrect pin";
    private static final String PROPERTY_SUBTITLE = "subtitle";
    private static final String PROPERTY_PROMO_ID = "promo id";
    private static final String PROPERTY_ITERATION = "iteration";
    private static final String EVENT_PROMO_VIDEO_SHOWN = "promo video showed";
    private static final String EVENT_PROMO_VIDEO_SKIPPED = "promo video skipped";
    private static final String EVENT_PROMO_VIDEO_COMPLETED = "promo video completed";
    private static final String EVENT_PROMO_VIDEO_ADDED_TO_WATCH_LIST = "added to watchlist";
    public static final String SOURCE_PROMO_VIDEO_AD = "promo video ad";
    public static final String SOURCE_DETAILS_SCREEN = "detail screen";
    public static final String SOURCE_BANNER = "banner";
    public static final String PROPERTY_FILTER = "filter";

    public static final String PROPERTY_BUFFER_COUNT = "buffer count";
    public static final String PROPERTY_BUFFER_TIME = "buffer time";
    public static final String PROPERTY_PAUSE_COUNT = "pause count";
    public static final String PROPERTY_PLAY_COUNT = "play count";
    public static final String PROPERTY_FORWARD_COUNT = "forward count";
    public static final String PROPERTY_BACKWARD_COUNT = "backward count";
    public static final String PROPERTY_BACKWARD_TIME = "backward time";
    public static final String PROPERTY_FORWARD_TIME = "forward time";


    // private static CleverTapAPI cleverTapAPI = null;

    private static final String EVENT_PARENTAL_CONTROL_STATUS = "parental control status";
    private static final String EVENT_PARENTAL_CONTROL_HINT = "parental control hint";
    private static final String EVENT_VIDEO_STARTED = "video started";
    private static final String EVENT_VIDEO_STOPPED = "video stopped";
    private static final String EVENT_VIDEO_PLAYED = "video played";
    private static final String EVENT_VIDEO_DETAILS_VIEWED = "video details viewed";
    private static final String EVENT_CATEGORY_VIEWED = "category viewed";
    private static final String EVENT_SEARCHED = "searched";
    private static final String EVENT_REGISTRATION_COMPLETED = "registration completed";
    private static final String EVENT_REGISTRATION_INITIATED = "registration initiated";
    private static final String EVENT_PAGE_VIEWED = "page viewed";
    private static final String EVENT_CLICKED = "clicked";
    private static final String EVENT_FILTER_APPLIED = "filter applied";
    private static final String EVENT_PLAN_SELECTED = "plan selected";
    private static final String EVENT_CONSENT_PAGE_VIEWED = "consent page viewed";
    private static final String EVENT_DOWNLOAD = "download";
    private static final String EVENT_DOWNLOAD_ERR = "download error";
    public static final String EVENT_REGISTRATION_FAILED = "registration failed";
    public static final String EVENT_OTP_ENTERED = "otp entered";
    public static final String EVENT_OTP_STATUS = "otp status";
    public static final String EVENT_REGISTRATION_PAGE_VIEWED = "registration page viewed";
    private static final String EVENT_PRIVACY_POLICY_UPDATE = "privacy policy update";
    private static final String EVENT_APP_IN_APP_CLICK = "App in App Click";
    private static final String EVENT_PREFERRED_LANGUAGE = "preferred language";
    private static final String EVENT_LOG_OUT = "logout";
    private static final String EVENT_VIDEO_BANNER_SHOWED = "video banner showed";


    private static final String TAG = CleverTap.class.getSimpleName();
    private static final String PROPERTY_KEYWORD = "keyword";
    static final String PROPERTY_EMAIL = "Email";
    static final String PROPERTY_PHONE = "Phone";
    static final String PROPERTY_IDENTITY = "Identity";
    private static final String PROPERTY_PAGE_NAME = "page name";
    private static final String PROPERTY_ACTION = "action";
    private static final String PROPERTY_GENRE = "genre";
    private static final String PROPERTY_LANGUAGE = "language";
    private static final String PROPERTY_PLAN = "plan";
    private static final String PROPERTY_PAYMENT_MODE_SELECTED = "payment mode selected";
    private static final String PROPERTY_AMOUNT = "amount";
    private static final String PROPERTY_DURATION = "duration";
    public static final String PROPERTY_CONTENT_CLICKED = "content clicked";
    public static final String PROPERTY_AUTO_LOGIN = "auto login";
    public static final String PROPERTY_CLICKED = "clicked";

    public static final String PROPERTY_CONTENT_MODEL = "content model";
    public static final String PREVIEW_BANNER = "previewBanner";

    public static final String VALUE_ACCEPT = "accept";
    public static final String VALUE_DECLINE = "decline";


    public static final String PAGE_ABOUT = "about";
    public static final String PAGE_TERMS_AND_CONDITIONS = "terms and conditions";
    public static final String PAGE_PRIVACY_POLICY = "privacy policy";
    public static final String PAGE_HELP = "help";
    public static final String PAGE_OFFER = "offer";
    public static final String PAGE_DETAILS = "details";
    public static final String PAGE_VIEW_ALL = "view all";
    public static final String PAGE_FILTER = "filter";
    public static final String PAGE_MYPACKS = "my packs";
    public static final String PAGE_MORE__APPS = "more apps";


    public static final String ACTION_CHROMECAST = "chrome cast";
    public static final String ACTION_FILTER = "filter";
    public static final String ACTION_DOWNLOAD = "download";
    //    public static final String PAGE_OTP = "otp";
    public static final String PROPERTY_SOURCE = "source";
    public static final String PROPERTY_SOURCE_DETAILS = "source details";
    public static final String PROPERTY_CONTENT_ID_IN_APP_RATING = "content ID";
    public static final String PROPERTY_CONTENT_NAME_IN_APP_RATING = "Content Name";
    public static final String PROPERTY_SOURCE_IN_APP_RATING = "Source";
    public static final String PROPERTY_SOURCE_DETAILS_IN_APP_RATING = "Source Detail";
    public static final String PROPERTY_DURATION_PLAYED_BEFORE_POP_UP_IN_APP_RATING = "duration Played before popup";
    public static final String PROPERTY_COUNT_OF_POPUP_IN_APP_RATING = "Count of Popup";
    public static final String PROPERTY_RATING_IN_APP_RATING = "Rating";
    public static final String PROPERTY_APP_OPENS_IN_APP_RATING = "App opens";
    public static final String PROPERTY_DEVICE_MODEL_IN_APP_RATING = "Device model";
    public static final String PROPERTY_RATING_DETAIL_IN_APP_RATING = "Rating detail";
    public static final String PROPERTY_INCREMENTAL_DURATION_IN_APP_RATING = "Incremental duration";
    public static final String SOURCE_IN_APP_RATING_SHOWN = "In-App Rating";
    public static final String PROPERTY_SERVICE_NAME = "service name";

    public static void setProperties(Map<String, Object> params) {
        //if (cleverTapAPI != null) {
        if (ApplicationController.IS_DEBUG_BUILD && params != null) {
            for (String key : params.keySet()) {
                //Log.d(TAG, "\ncreateProfile: key- " + key + " value- " + params.get(key));
            }
        }
        //Ignore if the property value is "NA"
        filterPropertiesForNA(params);
        //cleverTapAPI.pushProfile(params);
        //}
    }

    private static void filterPropertiesForNA(Map<String, Object> params) {
        if (params == null) {
            return;
        }
        try {
            for (Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Object> entry = it.next();
                if ("NA".equalsIgnoreCase(String.valueOf(entry.getValue()))) {
                    it.remove();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public static void addProperties(String key, ArrayList<String> values) {
        /*if (cleverTapAPI != null) {
            cleverTapAPI.addMultiValuesForKey(key, values);
            if (ApplicationController.IS_DEBUG_BUILD) {
                for (String value : values){
                    //Log.d(TAG, "\naddProperties: key- " + key + " value- " + value);
                }
            }
        }*/
    }


    public static void event(int priority, String event, Map<String, Object> params) {
        //if (cleverTapAPI != null) {
        if (PrefUtils.getInstance().getPrefEnableCleverTap() && priority >= PrefUtils.getInstance().getPrefCleverTapEventPriority()) {
            if (!TextUtils.isEmpty(PrefUtils.getInstance().getServiceName()) && params != null) {
                params.put(PROPERTY_SERVICE_NAME, PrefUtils.getInstance().getServiceName());
            } else if (params != null) {
                params.put(PROPERTY_SERVICE_NAME, PrefUtils.getInstance().getDefaultServiceName());
            }
            // cleverTapAPI.pushEvent(event, params);
            LoggerD.debugLog(TAG + " evevntName: " + event);
            if (ApplicationController.IS_DEBUG_BUILD && params != null) {
                for (String key : params.keySet()) {
                    LoggerD.debugLog(TAG + " key- " + key + " value- " + params.get(key));
                }
            }
        }
        //}
    }

    public static void eventVideoStarted(CardData mRelatedCardData, CardData mData, boolean isLocalPlayback,
                                         String mSource, String mSourceDetails, String tab, int sourceCarouselPosition, int savedContentPosition) {
        //Log.d(TAG, "eventVideoStarted- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title + " action: " + action);

        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
        String contentPartnerName = mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName;
        params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        if (mRelatedCardData != null && mRelatedCardData.generalInfo != null)
            params.put(PROPERTY_TVSHOW_NAME, mRelatedCardData.generalInfo.title);
        params.put(PROPERTY_TAB, tab != null ? tab : "");
        params.put(PROPERTY_CAROUSEL_POSITION, sourceCarouselPosition >= 0 ? sourceCarouselPosition + "" : "");
        String contentLanguage = "NA";
        if (mData.content != null
                && mData.content.language != null) {
            for (String language : mData.content.language) {
                if (!TextUtils.isEmpty(language)) {
                    contentLanguage = language;
                    break;
                }
            }
        }
        if (mData != null && !TextUtils.isEmpty(mData.trackingID)) {
            params.put(Analytics.PROPERTY_TRACKING_ID, mData.trackingID);
        }

        params.put(Analytics.PROPERTY_CONTENT_LANGUAGE, contentLanguage); //4

        String contentGenre = "NA";
        if (mData.content != null
                && mData.content.genre != null
                && mData.content.genre.size() > 0) {
            contentGenre = mData.content.genre.get(0).name;
            int duration = Util.calculateDurationInSeconds(mData.content.duration);
            params.put(PROPERTY_LENGTH_OF_VIDEO, duration);
        }
        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        String locationText = null;
        if (locationInfo != null) {
            if (!TextUtils.isEmpty(locationInfo.postalCode)) {
                locationText = locationInfo.postalCode;
            }
            if (!TextUtils.isEmpty(locationInfo.country)) {
                locationText += ", " + locationInfo.country;
            }
            if (!TextUtils.isEmpty(locationInfo.area)) {
                locationText += ", " + locationInfo.area;
            }
        }
        params.put(PROPERTY_LOCATION, locationText == null || TextUtils.isEmpty(locationText) ? "NA" : locationText);
        String network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
        params.put(PROPERTY_MOBILE_NETWORK, network == null || TextUtils.isEmpty(network) ? "NA" : network);
        if (!TextUtils.isEmpty(mSource))
            params.put(PROPERTY_SOURCE, mSource);
        if (!TextUtils.isEmpty(mSourceDetails))
            params.put(PROPERTY_SOURCE_DETAILS, mSourceDetails);
        params.put(PROPERTY_CONTENT_TYPE, mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null ? "NA" : mData.generalInfo.type);
        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                params.put(Analytics.PROPERTY_PROGRAM_NAME, mData.generalInfo.title); //3
                params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
            } else {
                params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
            }
            contentPartnerName = mData == null || mData.contentProvider == null ? contentPartnerName : mData.contentProvider;
            params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        } else {
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
            } else if (!TextUtils.isEmpty(mCarouselTitle)
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                if (mData.content != null
                        && mData.content.genre != null
                        && mData.content.genre.size() > 0) {
                    params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
                }
            }
            if (isLocalPlayback) {
                params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.VIDEO_TYPE_DOWNLOAD);
            } else {
                params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.STREAMING);
            }
        }
        //Adding PreferredLanguages to clevertapEvent;
        String preferredLanguage = getPreferredLanguagesString();
        params.put(PROPERTY_PREFERRED_LANGUAGE, preferredLanguage);
        event(HIGH, EVENT_VIDEO_STARTED, params);
        ComScoreAnalytics.getInstance().setEventContentStarted(mData, sourceCarouselPosition, sourceCarouselPosition,
                tab, mSource, mSourceDetails, savedContentPosition);
    }

    public static void eventTabViewed(String tabTitle) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_TAB, tabTitle);
        event(HIGH, EVENT_TAB_VIEWED, params);
    }

    public static void eventLangugaeChanged(String appLnaguage) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_APP_LANGUAGE, appLnaguage);
        event(Analytics.EventPriority.LOW, PROPERTY_APP_LANGUAGE, params);
        Map<String, Object> paramUser = new HashMap<>();
        params.put(USER_PROPERTY_APP_LANGUGAE, appLnaguage);
        setProperties(paramUser);
    }

    public static void eventVideoStopped(String contentModel, CardData mTVShowCardData, CardData mData, long ptimeInSec,
                                         long percentCompleted, boolean isLocalPlayback, String mSource, String mSourceDetails,
                                         String subtitle, String tab, int sourceCarouselPosition, int sourceContentPosition,
                                         int bufferCount, int bufferTimeinsec, int pauseCount, int playCount, int forwardCount,
                                         int backwardCount, int backwardTime, int forwardTime, String previewContentType) {
        //Log.d(TAG, "eventVideoStopped- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title);

        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
        String contentPartnerName = mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName;
        params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        params.put(PROPERTY_PER_COMPLETED, percentCompleted); //3
        params.put(PROPERTY_DURATION_VIEWED, ptimeInSec); //3
        params.put(PROPERTY_SUBTITLE, subtitle); //3
        params.put(PROPERTY_TAB, tab != null ? tab : "");
        params.put(PROPERTY_CAROUSEL_POSITION, sourceCarouselPosition >= 0 ? sourceCarouselPosition + "" : "");
        params.put(PROPERTY_CONTENT_POSITION, sourceContentPosition >= 0 ? sourceContentPosition + 1 + "" : "");
        params.put(PROPERTY_BUFFER_COUNT, bufferCount);
        params.put(PROPERTY_BUFFER_TIME, bufferTimeinsec);
        params.put(PROPERTY_PAUSE_COUNT, pauseCount);
        params.put(PROPERTY_PLAY_COUNT, playCount);
        params.put(PROPERTY_FORWARD_COUNT, forwardCount);
        params.put(PROPERTY_BACKWARD_COUNT, backwardCount);
        params.put(PROPERTY_BACKWARD_TIME, backwardTime);
        params.put(PROPERTY_FORWARD_TIME, forwardTime);

        if (!TextUtils.isEmpty(contentModel))
            params.put(PROPERTY_CONTENT_MODEL, contentModel);

        if (mTVShowCardData != null && !TextUtils.isEmpty(mTVShowCardData.trackingID)) {
            params.put(Analytics.PROPERTY_TRACKING_ID, mTVShowCardData.trackingID);
        } else if (mData != null && !TextUtils.isEmpty(mData.trackingID)) {
            params.put(Analytics.PROPERTY_TRACKING_ID, mData.trackingID);
        }
        String contentLanguage = "NA";
        if (mData.content != null
                && mData.content.language != null) {
            for (String language : mData.content.language) {
                if (!TextUtils.isEmpty(language)) {
                    contentLanguage = language;
                    break;
                }
            }
        }

        params.put(Analytics.PROPERTY_CONTENT_LANGUAGE, contentLanguage); //4

        String contentGenre = "NA";
        if (mData.content != null
                && mData.content.genre != null
                && mData.content.genre.size() > 0) {
            contentGenre = mData.content.genre.get(0).name;
            int duration = Util.calculateDurationInSeconds(mData.content.duration);
            params.put(PROPERTY_LENGTH_OF_VIDEO, duration);
        }
        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        String locationText = null;
        if (locationInfo != null) {
            if (!TextUtils.isEmpty(locationInfo.postalCode)) {
                locationText = locationInfo.postalCode;
            }
            if (!TextUtils.isEmpty(locationInfo.country)) {
                locationText += ", " + locationInfo.country;
            }
            if (!TextUtils.isEmpty(locationInfo.area)) {
                locationText += ", " + locationInfo.area;
            }
        }
        if (!TextUtils.isEmpty(mSource))
            params.put(PROPERTY_SOURCE, mSource);
        if (!TextUtils.isEmpty(mSourceDetails))
            params.put(PROPERTY_SOURCE_DETAILS, mSourceDetails);
        if (mTVShowCardData != null && mTVShowCardData.generalInfo != null)
            params.put(PROPERTY_TVSHOW_NAME, mTVShowCardData.generalInfo.title);
        params.put(PROPERTY_LOCATION, locationText == null || TextUtils.isEmpty(locationText) ? "NA" : locationText);
        String network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
        params.put(PROPERTY_MOBILE_NETWORK, network == null || TextUtils.isEmpty(network) ? "NA" : network);
        if (previewContentType != null) {
            params.put(PROPERTY_CONTENT_TYPE, previewContentType);
        } else {
            params.put(PROPERTY_CONTENT_TYPE, mData == null
                    || mData.generalInfo == null
                    || mData.generalInfo.type == null ? "NA" : mData.generalInfo.type);
        }

        if (isLocalPlayback) {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.VIDEO_TYPE_DOWNLOAD);
        } else if (mSourceDetails != null && APIConstants.SOURCE_DETAIL_AUTOPLAY.equalsIgnoreCase(mSourceDetails)) {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.AUTOPLAY_CONSUMPTION_TYPE);
        } else {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.STREAMING);
        }
        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                params.put(Analytics.PROPERTY_PROGRAM_NAME, mData.generalInfo.title); //3
                params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
            } else {
                params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
            }
            contentPartnerName = mData == null || mData.contentProvider == null ? contentPartnerName : mData.contentProvider;
            params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        } else {
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
            } else if (!TextUtils.isEmpty(mCarouselTitle)
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                if (mData.content != null
                        && mData.content.genre != null
                        && mData.content.genre.size() > 0) {
                    params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
                }
            }
        }
        //Adding PreferredLanguages to clevertapEvent;
        String preferredLanguage = getPreferredLanguagesString();
        params.put(PROPERTY_PREFERRED_LANGUAGE, preferredLanguage);
        event(HIGH, EVENT_VIDEO_STOPPED, params);
    }


    public static void eventVideoStopped(CardData mTVShowCardData, CardData mData, long ptimeInSec, long percentCompleted,
                                         boolean isLocalPlayback, String mSource, String mSourceDetails, String subtitle, String tab,
                                         int sourceCarouselPosition) {
        //Log.d(TAG, "eventVideoStopped- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title);

        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
        String contentPartnerName = mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName;
        params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        params.put(PROPERTY_PER_COMPLETED, percentCompleted); //3
        params.put(PROPERTY_DURATION_VIEWED, ptimeInSec); //3
        params.put(PROPERTY_SUBTITLE, subtitle); //3
        params.put(PROPERTY_TAB, tab != null ? tab : "");
        params.put(PROPERTY_CAROUSEL_POSITION, sourceCarouselPosition >= 0 ? sourceCarouselPosition + "" : "");
        if (mData != null && !TextUtils.isEmpty(mData.trackingID)) {
            params.put(Analytics.PROPERTY_TRACKING_ID, mData.trackingID);
        }
        String contentLanguage = "NA";
        if (mData.content != null
                && mData.content.language != null) {
            for (String language : mData.content.language) {
                if (!TextUtils.isEmpty(language)) {
                    contentLanguage = language;
                    break;
                }
            }
        }

        params.put(Analytics.PROPERTY_CONTENT_LANGUAGE, contentLanguage); //4

        String contentGenre = "NA";
        if (mData.content != null
                && mData.content.genre != null
                && mData.content.genre.size() > 0) {
            contentGenre = mData.content.genre.get(0).name;
            int duration = Util.calculateDurationInSeconds(mData.content.duration);
            params.put(PROPERTY_LENGTH_OF_VIDEO, duration);
        }
        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        String locationText = null;
        if (locationInfo != null) {
            if (!TextUtils.isEmpty(locationInfo.postalCode)) {
                locationText = locationInfo.postalCode;
            }
            if (!TextUtils.isEmpty(locationInfo.country)) {
                locationText += ", " + locationInfo.country;
            }
            if (!TextUtils.isEmpty(locationInfo.area)) {
                locationText += ", " + locationInfo.area;
            }
        }
        if (!TextUtils.isEmpty(mSource))
            params.put(PROPERTY_SOURCE, mSource);
        if (!TextUtils.isEmpty(mSourceDetails))
            params.put(PROPERTY_SOURCE_DETAILS, mSourceDetails);
        if (mTVShowCardData != null && mTVShowCardData.generalInfo != null)
            params.put(PROPERTY_TVSHOW_NAME, mTVShowCardData.generalInfo.title);
        params.put(PROPERTY_LOCATION, locationText == null || TextUtils.isEmpty(locationText) ? "NA" : locationText);
        String network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
        params.put(PROPERTY_MOBILE_NETWORK, network == null || TextUtils.isEmpty(network) ? "NA" : network);
        params.put(PROPERTY_CONTENT_TYPE, mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null ? "NA" : mData.generalInfo.type);
        if (isLocalPlayback) {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.VIDEO_TYPE_DOWNLOAD);
        } else {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.STREAMING);
        }
        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                params.put(Analytics.PROPERTY_PROGRAM_NAME, mData.generalInfo.title); //3
                params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
            } else {
                params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
            }
            contentPartnerName = mData == null || mData.contentProvider == null ? contentPartnerName : mData.contentProvider;
            params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        } else {
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
            } else if (!TextUtils.isEmpty(mCarouselTitle)
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                if (mData.content != null
                        && mData.content.genre != null
                        && mData.content.genre.size() > 0) {
                    params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
                }
            }
        }
        //Adding PreferredLanguages to clevertapEvent;
        String preferredLanguage = getPreferredLanguagesString();
        params.put(PROPERTY_PREFERRED_LANGUAGE, preferredLanguage);
        event(HIGH, EVENT_VIDEO_STOPPED, params);
    }

    public static void eventVideoPlayed(CardData mTVShowCardData, CardData mData,
                                        long ptimeInSec, long percentCompleted,
                                        int startPoint, boolean isLocalPlayback,
                                        String mSource, String mSourceDetails,
                                        String tab, int sourceCarouselPosition) {
        //Log.d(TAG, "eventVideoPlayed- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title);

        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }

        if (ptimeInSec * 60 < MAX_PLAYED_TIME_MIN) {
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
        String contentPartnerName = mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName;
        params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        params.put(PROPERTY_PER_COMPLETED, percentCompleted); //3
        params.put(PROPERTY_DURATION_VIEWED, ptimeInSec); //3
        params.put(PROPERTY_START_POINT, startPoint); //3
        params.put(PROPERTY_SOURCE, mSource);
        params.put(PROPERTY_SOURCE_DETAILS, mSourceDetails);
        params.put(PROPERTY_TAB, tab != null ? tab : "");
        params.put(PROPERTY_CAROUSEL_POSITION, sourceCarouselPosition >= 0 ? sourceCarouselPosition + "" : "");
        String contentLanguage = "NA";
        if (mData != null && !TextUtils.isEmpty(mData.trackingID)) {
            params.put(Analytics.PROPERTY_TRACKING_ID, mData.trackingID);
        }
        if (mData.content != null
                && mData.content.language != null) {
            for (String language : mData.content.language) {
                if (!TextUtils.isEmpty(language)) {
                    contentLanguage = language;
                    break;
                }
            }
        }

        if (mTVShowCardData != null && mTVShowCardData.generalInfo != null)
            params.put(PROPERTY_TVSHOW_NAME, mTVShowCardData.generalInfo.title);

        params.put(Analytics.PROPERTY_CONTENT_LANGUAGE, contentLanguage); //4

        String contentGenre = "NA";
        if (mData.content != null
                && mData.content.genre != null
                && mData.content.genre.size() > 0) {
            contentGenre = mData.content.genre.get(0).name;
            int duration = Util.calculateDurationInSeconds(mData.content.duration);
            params.put(PROPERTY_LENGTH_OF_VIDEO, duration);
        }
        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        String locationText = null;
        if (locationInfo != null) {
            if (!TextUtils.isEmpty(locationInfo.postalCode)) {
                locationText = locationInfo.postalCode;
            }
            if (!TextUtils.isEmpty(locationInfo.country)) {
                locationText += ", " + locationInfo.country;
            }
            if (!TextUtils.isEmpty(locationInfo.area)) {
                locationText += ", " + locationInfo.area;
            }
        }
        if (isLocalPlayback) {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.VIDEO_TYPE_DOWNLOAD);
        } else {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.STREAMING);
        }
        params.put(PROPERTY_LOCATION, locationText == null || TextUtils.isEmpty(locationText) ? "NA" : locationText);
        String network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
        params.put(PROPERTY_MOBILE_NETWORK, network == null || TextUtils.isEmpty(network) ? "NA" : network);
        params.put(PROPERTY_CONTENT_TYPE, mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null ? "NA" : mData.generalInfo.type);
        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                params.put(Analytics.PROPERTY_PROGRAM_NAME, mData.generalInfo.title); //3
                params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
            } else {
                params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
            }
            contentPartnerName = mData == null || mData.contentProvider == null ? contentPartnerName : mData.contentProvider;
            params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        } else {
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
            } else if (!TextUtils.isEmpty(mCarouselTitle)
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                if (mData.content != null
                        && mData.content.genre != null
                        && mData.content.genre.size() > 0) {
                    params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
                }
            }
        }
        //Adding PreferredLanguages to clevertapEvent;
        String preferredLanguage = getPreferredLanguagesString();
        params.put(PROPERTY_PREFERRED_LANGUAGE, preferredLanguage);
        event(HIGH, EVENT_VIDEO_PLAYED, params);
        ComScoreAnalytics.getInstance().setEventContentPlayed(mData,
                sourceCarouselPosition, sourceCarouselPosition, tab, mSource, mSourceDetails);
        MyplexAnalytics.getInstance().event(new Event(EVENT_VIDEO_PLAYED, params, AnalyticsConstants.EventPriority.HIGH));
    }

    public static void eventVideoPlayed(String contentModel, CardData mTVShowCardData, CardData mData,
                                        long ptimeInSec, long percentCompleted, int startPoint, boolean isLocalPlayback,
                                        String mSource, String mSourceDetails, String tab, int sourceCarouselPosition, int sourceContentPosition) {
        //Log.d(TAG, "eventVideoPlayed- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title);

        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }

        if (ptimeInSec * 60 < MAX_PLAYED_TIME_MIN) {
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
        String contentPartnerName = mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName;
        params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        params.put(PROPERTY_PER_COMPLETED, percentCompleted); //3
        params.put(PROPERTY_DURATION_VIEWED, ptimeInSec); //3
        params.put(PROPERTY_START_POINT, startPoint); //3
        params.put(PROPERTY_SOURCE, mSource);
        params.put(PROPERTY_SOURCE_DETAILS, mSourceDetails);
        params.put(PROPERTY_TAB, tab != null ? tab : "");
        params.put(PROPERTY_CAROUSEL_POSITION, sourceCarouselPosition >= 0 ? sourceCarouselPosition + "" : "");
        params.put(PROPERTY_CONTENT_POSITION, sourceContentPosition >= 0 ? sourceContentPosition + 1 + "" : "");
        String contentLanguage = "NA";
        if (mTVShowCardData != null && !TextUtils.isEmpty(mTVShowCardData.trackingID)) {
            params.put(Analytics.PROPERTY_TRACKING_ID, mTVShowCardData.trackingID);
        } else if (mData != null && !TextUtils.isEmpty(mData.trackingID)) {
            params.put(Analytics.PROPERTY_TRACKING_ID, mData.trackingID);
        }
        if (mData.content != null
                && mData.content.language != null) {
            for (String language : mData.content.language) {
                if (!TextUtils.isEmpty(language)) {
                    contentLanguage = language;
                    break;
                }
            }
        }
        if (!TextUtils.isEmpty(contentModel))
            params.put(PROPERTY_CONTENT_MODEL, contentModel);

        if (mTVShowCardData != null && mTVShowCardData.generalInfo != null)
            params.put(PROPERTY_TVSHOW_NAME, mTVShowCardData.generalInfo.title);

        params.put(Analytics.PROPERTY_CONTENT_LANGUAGE, contentLanguage); //4

        String contentGenre = "NA";
        if (mData.content != null
                && mData.content.genre != null
                && mData.content.genre.size() > 0) {
            contentGenre = mData.content.genre.get(0).name;
            int duration = Util.calculateDurationInSeconds(mData.content.duration);
            params.put(PROPERTY_LENGTH_OF_VIDEO, duration);
        }
        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        String locationText = null;
        if (locationInfo != null) {
            if (!TextUtils.isEmpty(locationInfo.postalCode)) {
                locationText = locationInfo.postalCode;
            }
            if (!TextUtils.isEmpty(locationInfo.country)) {
                locationText += ", " + locationInfo.country;
            }
            if (!TextUtils.isEmpty(locationInfo.area)) {
                locationText += ", " + locationInfo.area;
            }
        }
        if (isLocalPlayback) {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.VIDEO_TYPE_DOWNLOAD);
        } else if (mSourceDetails != null && APIConstants.SOURCE_DETAIL_AUTOPLAY.equalsIgnoreCase(mSourceDetails)) {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.AUTOPLAY_CONSUMPTION_TYPE);
        } else {
            params.put(PROPERTY_CONSUMPTION_TYPE, APIConstants.STREAMING);
        }
        params.put(PROPERTY_LOCATION, locationText == null || TextUtils.isEmpty(locationText) ? "NA" : locationText);
        String network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
        params.put(PROPERTY_MOBILE_NETWORK, network == null || TextUtils.isEmpty(network) ? "NA" : network);
        params.put(PROPERTY_CONTENT_TYPE, mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null ? "NA" : mData.generalInfo.type);
        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                params.put(Analytics.PROPERTY_PROGRAM_NAME, mData.generalInfo.title); //3
                params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
            } else {
                params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
            }
            contentPartnerName = mData == null || mData.contentProvider == null ? contentPartnerName : mData.contentProvider;
            params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        } else {
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
            } else if (!TextUtils.isEmpty(mCarouselTitle)
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                if (mData.content != null
                        && mData.content.genre != null
                        && mData.content.genre.size() > 0) {
                    params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
                }
            }
        }
        //Adding PreferredLanguages to clevertapEvent;
        String preferredLanguage = getPreferredLanguagesString();
        params.put(PROPERTY_PREFERRED_LANGUAGE, preferredLanguage);
        event(HIGH, EVENT_VIDEO_PLAYED, params);
        MyplexAnalytics.getInstance().event(new Event(EVENT_VIDEO_PLAYED, params, AnalyticsConstants.EventPriority.HIGH));
        ComScoreAnalytics.getInstance().setEventContentPlayed(mData,
                sourceCarouselPosition, sourceCarouselPosition, tab, mSource, mSourceDetails);
    }


    public static void eventVideoDetailsViewed(CardData mTVShowCardData, CardData mData,
                                               String mSource, String mSourceDetails, boolean isLoggedIn, String tab, int position, int contentPosition) {


        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }

        //Log.d(TAG, "gaPlayedVideoTimeCalculation- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title + " action: " + action);
        Map<String, Object> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
        String contentPartnerName = mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName;
        if (mTVShowCardData != null
                && mTVShowCardData.generalInfo != null) {
            params.put(PROPERTY_TVSHOW_NAME, mTVShowCardData.generalInfo.title); //3
        }
        params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        params.put(PROPERTY_SOURCE, mSource);
        params.put(PROPERTY_SOURCE_DETAILS, mSourceDetails == null ? "NA" : mSourceDetails);
        params.put(PROPERTY_REGISTERED_USER, isLoggedIn ? Analytics.YES : Analytics.NO);
        params.put(PROPERTY_TAB, tab != null ? tab : "");
        params.put(PROPERTY_CAROUSEL_POSITION, position >= 0 ? position + "" : "");
        if (mData != null && !TextUtils.isEmpty(mData.trackingID)) {
            params.put(Analytics.PROPERTY_TRACKING_ID, mData.trackingID);
        }
        String contentLanguage = "NA";
        if (mData.content != null
                && mData.content.language != null) {
            for (String language : mData.content.language) {
                if (!TextUtils.isEmpty(language)) {
                    contentLanguage = language;
                    break;
                }
            }
        }

        params.put(Analytics.PROPERTY_CONTENT_LANGUAGE, contentLanguage); //4

        String contentGenre = "NA";
        if (mData.content != null
                && mData.content.genre != null
                && mData.content.genre.size() > 0) {
            contentGenre = mData.content.genre.get(0).name;
            int duration = Util.calculateDurationInSeconds(mData.content.duration);
            params.put(PROPERTY_LENGTH_OF_VIDEO, duration);
        }
        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        String locationText = null;
        if (locationInfo != null) {
            if (!TextUtils.isEmpty(locationInfo.postalCode)) {
                locationText = locationInfo.postalCode;
            }
            if (!TextUtils.isEmpty(locationInfo.country)) {
                locationText += ", " + locationInfo.country;
            }
            if (!TextUtils.isEmpty(locationInfo.area)) {
                locationText += ", " + locationInfo.area;
            }
        }
        params.put(PROPERTY_LOCATION, locationText == null || TextUtils.isEmpty(locationText) ? "NA" : locationText);
        String network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
        params.put(PROPERTY_MOBILE_NETWORK, network == null || TextUtils.isEmpty(network) ? "NA" : network);
        params.put(PROPERTY_CONTENT_TYPE, mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null ? "NA" : mData.generalInfo.type);
        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                params.put(Analytics.PROPERTY_PROGRAM_NAME, mData.generalInfo.title); //3
                params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
            } else {
                params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
            }
            contentPartnerName = mData == null || mData.contentProvider == null ? contentPartnerName : mData.contentProvider;
            params.put(PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
        } else {
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
            } else if (!TextUtils.isEmpty(mCarouselTitle)
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                if (mData.content != null
                        && mData.content.genre != null
                        && mData.content.genre.size() > 0) {
                    params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
                }
            }

            try {
                CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
                if (DownloadUtil.isFileExist(mData._id + ".mp4", ApplicationController.getAppContext()) && downloadlist != null) {
                    String bytesDownloaded = "NA";

                    if (downloadlist != null
                            && downloadlist.mDownloadedList != null
                            && downloadlist.mDownloadedList.containsKey(mData._id)) {
                        CardDownloadData cardDownloadData = downloadlist.mDownloadedList.get(mData._id);
                        bytesDownloaded = "" + cardDownloadData.mDownloadedBytes;
                    }
                    params.put(Analytics.PROPERTY_CONTENT_SIZE, bytesDownloaded);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        event(Analytics.EventPriority.MEDIUM, EVENT_VIDEO_DETAILS_VIEWED, params);
        if (BuildConfig.FLAVOR.contains("idea")) {
            Log.d("idea appsflyer", AppsFlyerTracker.CONTENT_VIEWED);
            AppsFlyerTracker.aftrackEvent(AppsFlyerTracker.CONTENT_VIEWED, params, AnalyticsConstants.EventPriority.HIGH);
        }
        CleverTap.eventPageViewed(CleverTap.PAGE_DETAILS);
        ComScoreAnalytics.getInstance().setEventContentDetailsViewed(mData, contentPosition, position, tab, mSource, mSourceDetails);
        MyplexAnalytics.getInstance().event(new Event(EVENT_VIDEO_DETAILS_VIEWED, params, AnalyticsConstants.EventPriority.HIGH));
    }

    public static void eventCategoryViewed(String categoryName) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_CATEGORY, categoryName); //2
        event(Analytics.EventPriority.MEDIUM, EVENT_CATEGORY_VIEWED, params);
    }

    public static void eventSearched(String queryName, String category, String clicked) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(category))
            params.put(PROPERTY_CONTENT_TYPE, category); //2

        if (!TextUtils.isEmpty(queryName))
            params.put(PROPERTY_KEYWORD, queryName); //2

        if (!TextUtils.isEmpty(clicked))
            params.put(PROPERTY_CONTENT_CLICKED, clicked); //2
        event(Analytics.EventPriority.MEDIUM, EVENT_SEARCHED, params);
    }

    public static void eventSearched(String contentType, String queryName, String filter, String clicked) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(contentType))
            params.put(PROPERTY_CONTENT_TYPE, contentType); //2

        if (!TextUtils.isEmpty(filter))
            params.put(PROPERTY_FILTER, filter);

        if (!TextUtils.isEmpty(queryName))
            params.put(PROPERTY_KEYWORD, queryName); //2

        if (!TextUtils.isEmpty(clicked))
            params.put(PROPERTY_CONTENT_CLICKED, clicked); //2
        event(Analytics.EventPriority.MEDIUM, EVENT_SEARCHED, params);
    }

    public static void eventRegistrationCompleted(String email, String mobilenumber, String autoLoginValue) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_PHONE, mobilenumber); //2
        params.put(PROPERTY_EMAIL, email); //2
        if (!TextUtils.isEmpty(autoLoginValue))
            params.put(PROPERTY_AUTO_LOGIN, autoLoginValue); //2
        event(HIGH, EVENT_REGISTRATION_COMPLETED, params);
        MyplexAnalytics.getInstance().event(new Event(EVENT_REGISTRATION_COMPLETED, params, AnalyticsConstants.EventPriority.HIGH));
    }

    public static void eventPageViewed(String pageName) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_PAGE_NAME, pageName); //2
        event(HIGH, EVENT_PAGE_VIEWED, params);
    }

    public static void eventClicked(String pageName, String action) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_PAGE_NAME, pageName); //2
        params.put(PROPERTY_ACTION, action); //2
        event(Analytics.EventPriority.LOW, EVENT_CLICKED, params);
    }

    public static void eventFilterApplied(String genre, String language) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_GENRE, genre); //2
        params.put(PROPERTY_LANGUAGE, language); //2
        event(Analytics.EventPriority.LOW, EVENT_FILTER_APPLIED, params);
    }

    public static void eventPlanSelected(String plan, String paymentMode, String amount, String duration) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_PLAN, plan); //2
        params.put(PROPERTY_PAYMENT_MODE_SELECTED, paymentMode); //2
        params.put(PROPERTY_AMOUNT, amount); //2
        params.put(PROPERTY_DURATION, duration); //2
        event(Analytics.EventPriority.LOW, EVENT_PLAN_SELECTED, params);
    }


    public static void eventConsentPageViewed(String plan, String paymentMode, String amount, String duration, boolean isSMSFlow) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_PLAN, plan); //2
        params.put(PROPERTY_PAYMENT_MODE_SELECTED, paymentMode); //2
        params.put(PROPERTY_AMOUNT, amount); //2
        params.put(PROPERTY_DURATION, duration); //2
        params.put(PROPERTY_CG_PAGE_TYPE, isSMSFlow ? "sms consent" : "cg page"); //2
        event(Analytics.EventPriority.LOW, EVENT_CONSENT_PAGE_VIEWED, params);
    }

    public static void init(Application application) {
        try {

            // if (ApplicationController.IS_DEBUG_BUILD)
               /* CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG);
            cleverTapAPI = ApplicationController.getCleverTap(application);
            cleverTapAPI.enableDeviceNetworkInfoReporting(true);
            Location location = cleverTapAPI.getLocation();
            cleverTapAPI.setLocation(location);*/

            Map<String, Object> params = new HashMap<>();
            String deviceType = "Mobile";
            if (SDKUtils.isTablet(application)) {
                deviceType = "Tablet";
            }
            params.put(PROPERTY_DEVICE_TYPE, application.getResources().getString(R.string.osname));
            params.put(PROPERTY_DEVICE_OS, application.getResources().getString(R.string.osname));
            params.put(PROPERTY_DEVICE_MODEL, android.os.Build.MODEL);
            params.put(USER_PROPERTY_APP_LANGUGAE, PrefUtils.getInstance().getAppLanguageToSendServerInStringFormat());
            if (!TextUtils.isEmpty(PrefUtils.getInstance().getServiceName()) && params != null) {
                params.put(PROPERTY_SERVICE_NAME, PrefUtils.getInstance().getServiceName());
            } else if (params != null) {
                params.put(PROPERTY_SERVICE_NAME, PrefUtils.getInstance().getDefaultServiceName());
            }
            setProperties(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Log.d(TAG, "init clevertap_account_id- " + application.getString(R.string.clevertap_account_id) + " clevertap_account_token- " + application.getString(R.string.clevertap_account_token));
    }


    public static void updateUserProfileWithPreferredLanguages(Context mContext) {
        Map<String, Object> params = new HashMap<>();
        String deviceType = "Mobile";
        if (SDKUtils.isTablet(mContext)) {
            deviceType = "Tablet";
        }
        params.put(PROPERTY_DEVICE_TYPE, mContext.getResources().getString(R.string.osname));
        params.put(PROPERTY_DEVICE_OS, mContext.getResources().getString(R.string.osname));
        if (mContext.getResources().getBoolean(R.bool.isTablet)) {
            params.put(PROPERTY_DEVICE_OS, "Tablet");
        }
        params.put(PROPERTY_DEVICE_MODEL, android.os.Build.MODEL);
        params.put(PROPERTY_PREFERRED_LANGUAGE, getPreferredLanguagesString());
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getServiceName()) && params != null)
            params.put(PROPERTY_SERVICE_NAME, PrefUtils.getInstance().getServiceName());
        setProperties(params);
    }

    public static void eventNotificationOpened(String notificationTitle, String nid) {
        Map<String, Object> params = new HashMap<>();
        params.put(Analytics.NOTIFICATION_TITLE, notificationTitle);
        if (!TextUtils.isEmpty(nid)) {
            params.put(Analytics.NOTIFICATION_NID, nid);
        }
    }


    public static void eventNotificationPlayBackSuccess(String notificationTitle, String nid, CardData mData, String profileSelect) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(nid)) {
            params.put(Analytics.NOTIFICATION_NID, nid);
        }
        if (!TextUtils.isEmpty(notificationTitle)) {
            params.put(Analytics.NOTIFICATION_TITLE, notificationTitle);
        }


        if (mData != null && mData.generalInfo != null) {

            params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title);
            params.put(Analytics.PROPERTY_CONTENT_ID, mData._id);
            params.put(Analytics.CONTENT_TYPE, mData.generalInfo.type == null ? APIConstants.NOT_AVAILABLE : mData.generalInfo.type);
            params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.publishingHouse == null ? APIConstants.NOT_AVAILABLE : mData.publishingHouse.publishingHouseName); //3
            if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider); //3
                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                    params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                    params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
                }
            }
            params.put(Analytics.PROPERTY_STREAM_PROFILE, profileSelect);
            params.put(Analytics.PROPERTY_NETWORK, SDKUtils.getInternetConnectivity(ApplicationController.getAppContext()));
        }
        event(HIGH, EVENT_NOTIFICATION_PLAYED, params);
    }


    public static void eventNotificationPlayBackFailed(String notificationTitle, String nid, CardData mData, String profileSelect) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(nid)) {
            params.put(Analytics.NOTIFICATION_NID, nid);
        }
        if (!TextUtils.isEmpty(notificationTitle)) {
            params.put(Analytics.NOTIFICATION_TITLE, notificationTitle);
        }
        if (mData != null && mData.generalInfo != null) {
            params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title);
            params.put(Analytics.PROPERTY_CONTENT_ID, mData._id);
            params.put(Analytics.CONTENT_TYPE, mData.generalInfo.type == null ? "NA" : mData.generalInfo.type);
            params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName); //3
            if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider); //3
                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                    params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                    params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
                }
            }
            params.put(Analytics.PROPERTY_STREAM_PROFILE, profileSelect);
            params.put(Analytics.PROPERTY_NETWORK, SDKUtils.getInternetConnectivity(ApplicationController.getAppContext()));
        }
        event(HIGH, EVENT_NOTIFICATION_UNABLE_TO_PLAY, params);
    }

    public static void eventDownload(CardDownloadData downloadData, String status) {
        if (downloadData == null) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_STATUS, status);
        params.put(PROPERTY_CONTENT_NAME, downloadData.title);
        params.put(PROPERTY_GENRE, downloadData.genres);
        String category = "video";
        if (downloadData.ItemType != null
                && downloadData.ItemType.equalsIgnoreCase(APIConstants.TYPE_MOVIE)) {
            category = "movie";
        } else if (downloadData.ItemType != null
                && downloadData.ItemType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)) {
            category = "tv show";
        }
        params.put(PROPERTY_CATEGORY, category);
        params.put(PROPERTY_CONTENT_TYPE, downloadData.ItemType);
        params.put(PROPERTY_LANGUAGE, downloadData.time_languages);
        params.put(PROPERTY_CONTENT_PARTNER_NAME, downloadData.partnerName);
        params.put(PROPERTY_CONTENT_ID, downloadData._id);
        params.put(PROPERTY_PER_COMPLETED, downloadData.mPercentage);
        String formattedSize = String.format("%.0f", downloadData.mDownloadTotalSize < 0 ? 0 : downloadData.mDownloadTotalSize) + " MB";
        params.put(PROPERTY_DOWNLOAD_FILE_SIZE, formattedSize);
        event(HIGH, EVENT_DOWNLOAD, params);
    }

    public static void eventDownloadError(String type, String errorMessage, String reason) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_TYPE, type);
        params.put(PROPERTY_ERROR_MSG, errorMessage);
        params.put(PROPERTY_REASON, reason);
        event(HIGH, EVENT_DOWNLOAD, params);
    }

    public static void eventRegistrationInitiated(String email, String mobileno, String status, String reason) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(mobileno)) {
            params.put(PROPERTY_PHONE, mobileno); //2
        }
        if (!TextUtils.isEmpty(email)) {
            params.put(PROPERTY_EMAIL, email); //2
        }
        if (!TextUtils.isEmpty(status)) {
            params.put(PROPERTY_STATUS, status); //2
        }
        if (!TextUtils.isEmpty(reason)) {
            params.put(REASON_FAILURE, reason); //2
        }
        event(HIGH, EVENT_REGISTRATION_INITIATED, params);
        MyplexAnalytics.getInstance().event(new Event(EVENT_REGISTRATION_INITIATED, params, AnalyticsConstants.EventPriority.HIGH));
    }

    public static void eventRegistrationPageViewed(String source, String sourceDetails) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(source))
            params.put(PROPERTY_SOURCE, source); //2

        if (!TextUtils.isEmpty(sourceDetails))
            params.put(PROPERTY_SOURCE_DETAILS, sourceDetails); //2
        event(HIGH, EVENT_REGISTRATION_PAGE_VIEWED, params);
    }


    public static void eventOtpEntered(String otp, String mode) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(otp))
            params.put(PARAM_OTP, otp); //2

        if (!TextUtils.isEmpty(mode))
            params.put(PARAM_OTP_DETECTION, mode); //2
        event(HIGH, EVENT_OTP_ENTERED, params);
    }


    public static void eventOtpStatus(String otp, String status, String reason) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(otp))
            params.put(PARAM_OTP, otp); //2

        if (!TextUtils.isEmpty(status))
            params.put(PROPERTY_STATUS, status); //2

        if (!TextUtils.isEmpty(reason))
            params.put(REASON_FAILURE, reason); //2

        event(HIGH, EVENT_OTP_STATUS, params);
    }


    public static void eventRegistrationFailed(String email, String phone, String reason) {
        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(email))
            params.put(PARAM_EMAIL_ID, email); //2

        if (!TextUtils.isEmpty(phone))
            params.put(PROPERTY_PHONE, phone); //2

        if (!TextUtils.isEmpty(reason))
            params.put(REASON_FAILURE, reason); //2

        event(HIGH, EVENT_REGISTRATION_FAILED, params);
        MyplexAnalytics.getInstance().event(new Event(EVENT_REGISTRATION_FAILED, params, AnalyticsConstants.EventPriority.HIGH));
    }


    public static void eventPrivacyPolicyClicked(String action) {
        Map<String, Object> params = new HashMap<>();

        if (!TextUtils.isEmpty(action))
            params.put(PROPERTY_CLICKED, action); //2

        event(HIGH, EVENT_PRIVACY_POLICY_UPDATE, params);
    }

    public static void eventParentalControlStatus(String status) {
        Map<String, Object> params = new HashMap<>();

        if (!TextUtils.isEmpty(status))
            params.put(PROPERTY_STATUS, status); //2

        event(Analytics.EventPriority.LOW, EVENT_PARENTAL_CONTROL_STATUS, params);
    }


    public static void eventParentalControlHint(String hint, String pin) {
        Map<String, Object> params = new HashMap<>();

        if (!TextUtils.isEmpty(hint))
            params.put(PROPERTY_HINT, hint);
        if (!TextUtils.isEmpty(pin))
            params.put(PROPERTY_PIN, pin);//2

        event(Analytics.EventPriority.LOW, EVENT_PARENTAL_CONTROL_HINT, params);
    }

    public static void eventPromoVideoShown(String promoId, String iteration) {
        Map<String, Object> params = new HashMap<>();

        if (!TextUtils.isEmpty(promoId))
            params.put(PROPERTY_PROMO_ID, promoId);
        if (!TextUtils.isEmpty(iteration))
            params.put(PROPERTY_ITERATION, iteration);//2

        event(HIGH, EVENT_PROMO_VIDEO_SHOWN, params);
    }

    public static void eventPromoVideoSkipped(String promoId, String iteration) {
        Map<String, Object> params = new HashMap<>();

        if (!TextUtils.isEmpty(promoId))
            params.put(PROPERTY_PROMO_ID, promoId);
        if (!TextUtils.isEmpty(iteration))
            params.put(PROPERTY_ITERATION, iteration);//2

        event(HIGH, EVENT_PROMO_VIDEO_SKIPPED, params);
    }

    public static void eventPromoVideoCompleted(String promoId, String iteration) {
        Map<String, Object> params = new HashMap<>();

        if (!TextUtils.isEmpty(promoId))
            params.put(PROPERTY_PROMO_ID, promoId);
        if (!TextUtils.isEmpty(iteration))
            params.put(PROPERTY_ITERATION, iteration);//2

        event(HIGH, EVENT_PROMO_VIDEO_COMPLETED, params);
    }

    public static void eventPromoVideoAddedToWatchList(String promoId, String contentId, String source) {
        Map<String, Object> params = new HashMap<>();

        if (!TextUtils.isEmpty(promoId))
            params.put(PROPERTY_PROMO_ID, promoId);
        if (!TextUtils.isEmpty(contentId))
            params.put(PROPERTY_CONTENT_ID, contentId);//2

        if (!TextUtils.isEmpty(contentId))
            params.put(PROPERTY_SOURCE, source);//2

        event(HIGH, EVENT_PROMO_VIDEO_ADDED_TO_WATCH_LIST, params);
    }


    public static void eventPreferedLanguage() {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_PREFERRED_LANGUAGE, getPreferredLanguagesString());
        event(HIGH, EVENT_PREFERRED_LANGUAGE, params);
    }

    public static void eventLogOut() {
        Map<String, Object> params = new HashMap<>();
        event(HIGH, EVENT_LOG_OUT, params);
    }

    public static void eventRatingPopUpShown(String rating, String ratingDetails) {

        Map<String, Object> params = new HashMap<>();
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getLastContentIDPlayed()))
            params.put(PROPERTY_CONTENT_ID_IN_APP_RATING, PrefUtils.getInstance().getLastContentIDPlayed());//2
        params.put(PROPERTY_COUNT_OF_POPUP_IN_APP_RATING, PrefUtils.getInstance().getWhichElementToBeCompared());
        if (!TextUtils.isEmpty(rating))
            params.put(PROPERTY_RATING_IN_APP_RATING, rating);
        if (!TextUtils.isEmpty(ratingDetails))
            params.put(PROPERTY_RATING_DETAIL_IN_APP_RATING, ratingDetails);
        params.put(PROPERTY_INCREMENTAL_DURATION_IN_APP_RATING, PrefUtils.getInstance().getTotalMOU());
        params.put(PROPERTY_DURATION_PLAYED_BEFORE_POP_UP_IN_APP_RATING, PrefUtils.getInstance().getLastPlayedContentMOU());
        params.put(PROPERTY_APP_OPENS_IN_APP_RATING, PrefUtils.getInstance().getAppLaunchCount());
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getLastContentPlayed()))
            params.put(PROPERTY_CONTENT_NAME_IN_APP_RATING, PrefUtils.getInstance().getLastContentPlayed());//2
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getLastContentSourcePlayed()))
            params.put(PROPERTY_SOURCE_IN_APP_RATING, PrefUtils.getInstance().getLastContentSourcePlayed()); //2

        if (!TextUtils.isEmpty(PrefUtils.getInstance().getLastContentSourceDetailsPlayed()))
            params.put(PROPERTY_SOURCE_DETAILS_IN_APP_RATING, PrefUtils.getInstance().getLastContentSourceDetailsPlayed());

        event(HIGH, SOURCE_IN_APP_RATING_SHOWN, params);
    }

    public static void eventAppInAppClick(String partnerName, String contentName,
                                          String genre, String language, String contentType, String tab, String source, String sourceDetails) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARTNER_NAME, partnerName);
        params.put(PROPERTY_CONTENT_NAME, contentName);
        params.put(PROPERTY_GENRE, genre);
        params.put(PROPERTY_LANGUAGE, language);
        params.put(PROPERTY_CONTENT_TYPE, contentType);
        params.put(PROPERTY_TAB, tab);
        params.put(PROPERTY_SOURCE, source);
        params.put(PROPERTY_SOURCE_DETAILS, sourceDetails);
        event(HIGH, EVENT_APP_IN_APP_CLICK, params);
    }


    private static String getPreferredLanguagesString() {
        List<PreferredLanguageItem> items = PrefUtils.getInstance().getPreferredLanguageItems();
        String preferredLanguages = "";
        if (PrefUtils.getInstance().getPreferredLanguageItems() != null) {
            for (int i = 0; i < items.size(); i++) {
                preferredLanguages = (i != items.size() - 1) ? preferredLanguages + items.get(i).getTerm() + "," : preferredLanguages + items.get(i).getTerm();
            }
        }
        return preferredLanguages;
    }

    public static void eventPreviewVideoShown(String action, int durationViewed, String content_Id, String content_name, String videoCompleted,
                                              CardData mCardData, String mTabName, String source, String mSourceDetails) {
        Map<String, Object> params = new HashMap<>();
        String mVideoCompleted = "Yes";
        params.put(PROPERTY_ACTION, action); //2
        if (durationViewed == 0) {
            mVideoCompleted = "No";
            params.put(PROPERTY_DURATION_VIEWED, "NA");
        } else {
            mVideoCompleted = "Yes";
            params.put(PROPERTY_DURATION_VIEWED, durationViewed);
        }
        params.put(PROPERTY_CONTENT_ID, content_Id); //2
        params.put(PROPERTY_CONTENT_NAME, content_name); //2
        params.put(PROPERTY_VIDEO_COMPLETED, videoCompleted);
        event(HIGH, EVENT_VIDEO_BANNER_SHOWED, params);
        //ComScoreAnalytics.getInstance().setEventPreviewVideoPlayed(mCardData,mTabName,durationViewed,mVideoCompleted,source,mSourceDetails);
        ComScoreAnalytics.getInstance().setEventPromoVideoShowed(content_Id, action, durationViewed, "NA");
    }

    public static void eventPreviewVideoAction(String action, CardData mCardData) {
        Map<String, Object> params = new HashMap<>();
        params.put(PROPERTY_ACTION, action);
        event(HIGH, EVENT_VIDEO_BANNER_SHOWED, params);
        ComScoreAnalytics.getInstance().setEventPreviewVideoOrAddedToWatchList(action, mCardData);
    }
}
