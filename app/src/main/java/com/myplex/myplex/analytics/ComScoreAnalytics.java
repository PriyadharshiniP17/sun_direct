package com.myplex.myplex.analytics;

import android.text.TextUtils;

//import com.hifx.lens.Lens;
import com.myplex.model.CardData;
import com.myplex.util.PrefUtils;

import java.util.HashMap;

public class ComScoreAnalytics {

    private static ComScoreAnalytics _self = null;
    public static final String NS_CATEGORY = "ns_category";
    public static final String EVENT_CONTENT_DETAILS_VIEWED = "content details viewed";
    public static final String EVENT_CONTENT_LOADED = "content loaded";
    public static final String EVENT_VIDEO_PAUSED = "Video Paused";
    public static final String EVENT_VIDEO_SEEKED = "Video Seeked";
    public static final String EVENT_TAB_VIEWED = "tab viewed";
    public static final String EVENT_SEARCHED = "searched";
    public static final String EVENT_PROMO_VIDEO_SHOWED = "promo video showed";
    public static final String EVENT_LOGOUT = "logout";
    public static final String EVENT_SIGN_UP = "sign up";
    public static final String EVENT_LOGIN = "login";
    public static final String EVENT_DOWNLOAD_INITIATED = "download initiated";
    public static final String EVENT_DOWNLOAD_COMPLETED = "download completed";
    public static final String EVENT_FAVOURITE = "favourite";
    public static final String EVENT_SHARE = "share";
    public static final String EVENT_PAYMENT_STATUS = "payment status";
    public static final String EVENT_AD_PLAYED = "ad played";
    public static final String EVENT_AD_STARTED = "Ad start";
    public static final String EVENT_AD_CLICKED = "Ad clicked";
    public static final String EVENT_AD_ENDED = "Ad ended";
    public static final String EVENT_AD_SKIPPED = "Ad skipped";
    public static final String EVENT_POP_UP_SHOWED = "pop-up showed";
    public static final String EVENT_POP_UP_RESPONSE = "pop-up response";
    public static final String EVENT_ADD_TO_WATCH_LIST = "add to watchlist";
    public static final String EVENT_CAST = "cast";
    public static final String EVENT_UN_SUBSCRIPTION = "unsubscription";
    public static final String EVENT_DEEP_LINK = "deeplink";
    public static final String EVENT_SUBSCRIBE = "subscribe";
    public static final String EVENT_VIDEO_BANNER_SHOWED = "video banner showed";
    public static final String EVENT_EXIT_POP_UP = "Exit Popup";
    public static final String EVENT_CONTENT_PLAYED = "content played";
    public static final String EVENT_CONTENT_STARTED = "content started";


    public static final String PARAM_CONTENT_ID = "content_id";
    public static final String PARAM_CONTENT_NAME = "content_name";
    public static final String PARAM_SERIES_NAME = "series_name";
    public static final String PARAM_CONTENT_MODEL = "content_model";
    public static final String PARAM_CONTENT_TYPE = "content_type";
    public static final String PARAM_CONTENT_LANGUAGE = "content_language";
    public static final String PARAM_CONTENT_GENRE = "content_genre";
    public static final String PARAM_ADDED = "added";
    public static final String PARAM_REMOVED = "removed";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_SOURCE = "source";
    public static final String PARAM_CONTENT_CLICKED = "content_clicked";
    public static final String PARAM_SOURCE_NAME = "source_detail";
    public static final String PARAM_GLOBAL_ID = "global_id";
    public static final String CAROUSEL = "Carousel";
    public static final String BANNER = "Banner";
    public static final String NOTIFICATION = "Notification";
    public static final String PARAM_TAB_NAME = "tab";
    public static final String PARAM_KEYWORD = "keyword";
    public static final String PARAM_QUALITY = "quality";
    public static final String UNDER_SCORE = "_";
    public static final String PARAM_CONTENT_POSITION = "content_position";
    public static final String PARAM_CAROUSEL_POSITION = "carousel_position";
    public static final String PARAM_USER_ID = "user_id";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_START_POSITION = "start_point";
    public static final String PARAM_CONTENT_DURATION = "content_duration";
    public static final String PARAM_PAUSED_AT = "paused_at";
    public static final String PARAM_PERCENTAGE = "% completed";
    public static final String PARAM_DURATION_VIEWED = "duration_viewed";
    public static final String PARAM_VIDEO_COMPLETED = "video_completed";
    public static final String PARAM_LINK = "link";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_PREFERRED_LANGUAGE = "Preferred_Language";


    public static final String VALUE_SOURCE_CAROUSEL = "carousel";
    public static final String VALUE_SOURCE_BANNER = "banner";
    public static final String VALUE_SOURCE_SQUARE_BANNER = "square_banner";
    public static final String VALUE_SOURCE_POTRIRAT_BANNER = "potriat_banner";
    public static final String VALUE_SOURCE_SEARCH = "search";
    public static final String VALUE_SOURCE_DETAILS_SIMILAR_CONTENT = "similar_content";
    public static final String VALUE_SOURCE_DETAILS_EPG = "epg";
    public static final String VALUE_SOURCE_DETAILS_PROMO_BANNER = "promo_banner";
    public static final String VALUE_SOURCE_DETAILS = "details";
    public static final String VALUE_SOURCE_DOWNLOADED_VIDEOS = "downloaded_videos";
    public static final String VALUE_SOURCE_WATCHLIST_VIDEOS = "watchlist";
    public static final String VALUE_SOURCE_LIVE = "live tv";
    public static final String VALUE_SOURCE_NOTIFICATION = "notification";
    public static final String VALUE_SOURCE_FILTER = "filter";

    public static final String REASON_FAILURE = "reason_for_failure";
    public static final String SIGNUP_OPTION = "signup_option";
    public static final String PARAM_MOBILE = "mobile_number";
    public static final String PARAM_EMAIL = "email_id";
    public static final String PARAM_GENDER = "gender";
    public static final String PARAM_AGE = "age";
    public static final String PARAM_COUNTRY = "registered_country";
    public static final String PARAM_STATE = "registered_state";

    public static final String PARAM_POPUP_ID = "popup_id";
    public static final String PARAM_POPUP_NAME = "popup_name";
    public static final String PARAM_POPUP_MESSAGE = "popup_message";
    public static final String PARAM_POPUP_RESPONSE = "popup_response";
    public static final String PARAM_POPUP_VALUE = "popup_value";
    public static final String PARAM_AD_TYPE = "ad_type";
    public static final String PARAM_SKIPPABLE_AD = "skippable_ad";
    public static final String PARAM_AD_SKIP_STATUS = "skip_status";
    public static final String PARAM_AD_PLAYED_DURATION = "duration_played";
    public static final String PARAM_TOTAL_AD_DURATION = "total_ad_duration";
    public static final String PARAM_AD_STATUS = "ad_status";
    public static final String PARAM_AD_ID = "ad_id";

    public static final String PARAM_PROMO_ID = "promo_id";
    public static final String PARAM_PROMO_DURATION_VIWED = "duration_viewed";
    public static final String PARAM_PROMO_ITERATION = "iteration";
    public static final String PARAM_CAST = "cast";
    public static final String PARAM_DURATION = "duration";
    public static final String PARAM_PLAY_BUTTON = "play_button";


    public static ComScoreAnalytics getInstance() {
        if (_self == null) {
            _self = new ComScoreAnalytics();
        }
        return _self;
    }

    public void setEventPaymentStatus() {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(NS_CATEGORY, EVENT_PAYMENT_STATUS);
        notifyEvent(labels, EVENT_PAYMENT_STATUS);
    }

    public void setEventCast(CardData mCardData, int contentPosition, int carouselPosition, String mMenuGroup, String source
            , String sourceName, String cast) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CAST, cast);
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_PREFERRED_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(NS_CATEGORY, EVENT_CAST);
        notifyEvent(labels, EVENT_CAST);
    }

    public void setEventPopUpShowed(String popupId, String popupName, String popupMessage) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_POPUP_ID, popupId);
        labels.put(PARAM_POPUP_NAME, popupName);
        labels.put(PARAM_POPUP_MESSAGE, popupMessage);
        labels.put(NS_CATEGORY, EVENT_POP_UP_SHOWED);
        notifyEvent(labels, EVENT_POP_UP_SHOWED);
    }

    public void setEventPopUpResponse(String popupId, String popupName, String popupResponse, String popupValue) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_POPUP_ID, popupId);
        labels.put(PARAM_POPUP_NAME, popupName);
        labels.put(PARAM_POPUP_RESPONSE, popupResponse);
        labels.put(PARAM_POPUP_VALUE, popupValue);
        labels.put(NS_CATEGORY, EVENT_POP_UP_RESPONSE);
        notifyEvent(labels, EVENT_POP_UP_RESPONSE);
    }

    public void setEventExitPopUp(CardData mCardData, String action, String mMenuGroup) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_ACTION, action);
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(NS_CATEGORY, EVENT_EXIT_POP_UP);
        notifyEvent(labels, EVENT_EXIT_POP_UP);
    }

    public void setEventDownloadCompleted(CardData mCardData, String quality, boolean isSuccessOrFailed) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_QUALITY, checkAndReturnValue(quality));
        labels.put(PARAM_STATUS, isSuccessOrFailed ? "Success" : "Failed");
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(NS_CATEGORY, EVENT_DOWNLOAD_COMPLETED);
        notifyEvent(labels, EVENT_DOWNLOAD_COMPLETED);
    }

    public void setEventDownloadInitiated(CardData mCardData, String quality, String reason) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_QUALITY, checkAndReturnValue(quality));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(REASON_FAILURE, reason);
        labels.put(NS_CATEGORY, EVENT_DOWNLOAD_INITIATED);
        notifyEvent(labels, EVENT_DOWNLOAD_INITIATED);
    }

    public void setEventLogout() {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(NS_CATEGORY, EVENT_LOGOUT);
        notifyEvent(labels, EVENT_LOGOUT);
    }

    public void setEventSignUp(String signUp, String mobile, String email, String gender, String status, String reason) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(SIGNUP_OPTION, signUp);
        labels.put(PARAM_MOBILE, mobile);
        labels.put(PARAM_EMAIL, email);
        labels.put(PARAM_GENDER, gender);
        /*labels.put(PARAM_AGE, EVENT_SIGN_UP);
        labels.put(PARAM_COUNTRY, EVENT_SIGN_UP);
        labels.put(PARAM_STATE, EVENT_SIGN_UP);*/
        labels.put(PARAM_STATUS, status);
        labels.put(REASON_FAILURE, reason);
        labels.put(NS_CATEGORY, EVENT_SIGN_UP);
        notifyEvent(labels, EVENT_SIGN_UP);
    }


    public void setEventLogin(String mobile, String eMail, String reason, String status) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_MOBILE, mobile);
        labels.put(PARAM_EMAIL, eMail);
        labels.put(PARAM_STATUS, status);
        labels.put(REASON_FAILURE, reason);
        labels.put(NS_CATEGORY, EVENT_LOGIN);
        notifyEvent(labels, EVENT_LOGIN);
    }

    public void setEventContentStarted(CardData mCardData, int contentPosition, int carouselPosition, String mMenuGroup, String source
            , String sourceName, long savedContentPosition) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(PARAM_START_POSITION, checkAndReturnLongValue(savedContentPosition));
        labels.put(PARAM_CONTENT_DURATION, checkAndReturnNullValueForLong(mCardData));
        labels.put(NS_CATEGORY, EVENT_CONTENT_STARTED);
        notifyEvent(labels, EVENT_CONTENT_STARTED);
    }

    public void setEventVideoBannerShowed(CardData mCardData, int carouselPosition, String mMenuGroup, String source, String sourceName) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_ACTION, "Play");
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_DURATION, checkAndReturnNullValueForLong(mCardData));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_PREFERRED_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(NS_CATEGORY, EVENT_VIDEO_BANNER_SHOWED);
        notifyEvent(labels, EVENT_VIDEO_BANNER_SHOWED);
    }

    private String checkAndReturnNullValueForLong(CardData mCardData) {
        if (mCardData != null && mCardData.content != null && mCardData.content.duration != null) {
            return String.valueOf(mCardData.content.duration);
        }
        return "";
    }

    public void setEventContentPlayed(CardData mCardData, int contentPosition,
                                      int carouselPosition, String mMenuGroup, String source
            , String sourceName) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(NS_CATEGORY, EVENT_CONTENT_PLAYED);
        notifyEvent(labels, EVENT_CONTENT_PLAYED);
    }

    public void setEventContentDetailsViewed(CardData mCardData, int contentPosition, int carouselPosition, String mMenuGroup, String source
            , String sourceName) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(NS_CATEGORY, EVENT_CONTENT_DETAILS_VIEWED);
        notifyEvent(labels, EVENT_CONTENT_DETAILS_VIEWED);
    }

    public void setEventContentLoaded(CardData mCardData, int contentPosition, int carouselPosition, String mMenuGroup, String source
            , String sourceName, String playButton) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_CONTENT_DURATION, checkAndReturnNullValueForLong(mCardData));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_PLAY_BUTTON, playButton);
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(NS_CATEGORY, EVENT_CONTENT_LOADED);
        notifyEvent(labels, EVENT_CONTENT_LOADED);
    }

    public void setEventVideoPaused(CardData mCardData, int contentPosition, int carouselPosition, String mMenuGroup, String source
            , String sourceName, String playButton, long pausedAt) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_CONTENT_DURATION, checkAndReturnNullValueForLong(mCardData));
        //labels.put(PARAM_DURATION_VIEWED, String.valueOf(durationViwed));
        labels.put(PARAM_PAUSED_AT, String.valueOf(pausedAt));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_PLAY_BUTTON, playButton);
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(NS_CATEGORY, EVENT_VIDEO_PAUSED);
        notifyEvent(labels, EVENT_VIDEO_PAUSED);
    }


    public void setEventVideoSeeked(CardData mCardData, int contentPosition, int carouselPosition, String mMenuGroup, String source
            , String sourceName, String playButton) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_CONTENT_DURATION, checkAndReturnNullValueForLong(mCardData));
        //labels.put(PARAM_DURATION_VIEWED, String.valueOf(durationViwed));
        //labels.put(PARAM_PAUSED_AT, String.valueOf(pausedAt));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_PLAY_BUTTON, playButton);
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(NS_CATEGORY, EVENT_VIDEO_SEEKED);
        notifyEvent(labels, EVENT_VIDEO_SEEKED);
    }

    public void setInlineSearch(CardData mCardData, String tabName, String keyWord, boolean isContentClicked) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_CLICKED, isContentClicked ? "Yes" : "No");
        labels.put(PARAM_KEYWORD, checkAndReturnValue(keyWord));
        labels.put(PARAM_SOURCE, checkAndReturnValue(tabName));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(NS_CATEGORY, EVENT_SEARCHED);
        notifyEvent(labels, EVENT_SEARCHED);
    }

    public void setTabClickedEvent(String tabName) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(tabName));
        labels.put(NS_CATEGORY, EVENT_TAB_VIEWED);
        notifyEvent(labels, EVENT_TAB_VIEWED);
    }


    public void setEventShare(CardData mCardData) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(NS_CATEGORY, EVENT_SHARE);
        notifyEvent(labels, EVENT_SHARE);
    }

    public void setEventFavourite(CardData mCardData, boolean status) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_STATUS, status ? "Yes" : "No");
        labels.put(NS_CATEGORY, EVENT_FAVOURITE);
        notifyEvent(labels, EVENT_FAVOURITE);
    }

    public void setEventAddToWatchList(CardData mCardData, boolean isAddedOrRemoved, String source) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_ACTION, isAddedOrRemoved ? PARAM_ADDED : PARAM_REMOVED);
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_SOURCE, source);
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(NS_CATEGORY, EVENT_ADD_TO_WATCH_LIST);
        notifyEvent(labels, EVENT_ADD_TO_WATCH_LIST);
    }

    public void setEventPreviewVideoPlayed(CardData mCardData, String tabName, int durationViewed, String mVideoCompleted,
                                           String source, String sourceDetails) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_ACTION, "Play");
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(tabName));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceDetails));
        labels.put(PARAM_VIDEO_COMPLETED, checkAndReturnValue(mVideoCompleted));
        labels.put(PARAM_DURATION_VIEWED, checkAndReturnLongValue(durationViewed));
        labels.put(NS_CATEGORY, EVENT_ADD_TO_WATCH_LIST);
        notifyEvent(labels, EVENT_ADD_TO_WATCH_LIST);
    }

    public void setEventPromoVideoShowed(String promoId, String action, int durationViewed, String iteration) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_PROMO_ID, promoId);
        labels.put(PARAM_ACTION, action);
        labels.put(PARAM_PROMO_DURATION_VIWED, String.valueOf(durationViewed));
        labels.put(PARAM_PROMO_ITERATION, iteration);
        labels.put(NS_CATEGORY, EVENT_PROMO_VIDEO_SHOWED);
        notifyEvent(labels, EVENT_PROMO_VIDEO_SHOWED);
    }

    public void setEventPreviewVideoOrAddedToWatchList(String action, CardData mCardData) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_ACTION, checkAndReturnValue(action));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(NS_CATEGORY, EVENT_ADD_TO_WATCH_LIST);
        notifyEvent(labels, EVENT_ADD_TO_WATCH_LIST);
    }

    public void setEventDeepLink(String url, String actionType) {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_LINK, checkAndReturnValue(url));
        labels.put(PARAM_TYPE, checkAndReturnValue(actionType));
        labels.put(NS_CATEGORY, EVENT_DEEP_LINK);
        notifyEvent(labels, EVENT_DEEP_LINK);
    }

    public void setEventAdPlayed(CardData mCardData, String mMenuGroup, String source, String sourceName, int contentPosition, int carouselPosition,
                                 String adtype, boolean skippableAd, String skipStatus, String durationPlayed, String totalAdDur, String adId) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(PARAM_AD_TYPE, adtype);
        labels.put(PARAM_SKIPPABLE_AD, skippableAd ? "Yes" : "No");
        labels.put(PARAM_AD_SKIP_STATUS, skipStatus);
        labels.put(PARAM_AD_PLAYED_DURATION, durationPlayed);
        labels.put(PARAM_TOTAL_AD_DURATION, totalAdDur);
        labels.put(PARAM_AD_ID, adId);
        labels.put(NS_CATEGORY, EVENT_AD_PLAYED);
        notifyEvent(labels, EVENT_AD_PLAYED);
    }

    public void setEventAdStarted(CardData mCardData, String mMenuGroup, String source, String sourceName, int contentPosition, int carouselPosition,
                                  String adtype, boolean skippableAd, String skipStatus, String durationPlayed, String totalAdDur, String adStatus, String adId) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(PARAM_AD_TYPE, adtype);
        labels.put(PARAM_SKIPPABLE_AD, skippableAd ? "Yes" : "No");
        labels.put(PARAM_AD_SKIP_STATUS, skipStatus);
        labels.put(PARAM_AD_PLAYED_DURATION, durationPlayed);
        labels.put(PARAM_TOTAL_AD_DURATION, totalAdDur);
        labels.put(PARAM_AD_STATUS, adStatus);
        labels.put(PARAM_AD_ID, adId);
        labels.put(NS_CATEGORY, EVENT_AD_STARTED);
        notifyEvent(labels, EVENT_AD_STARTED);
    }

    public void setEventAdClicked(CardData mCardData, String mMenuGroup, String source, String sourceName, int contentPosition, int carouselPosition,
                                  String adtype, boolean skippableAd, String skipStatus, String durationPlayed, String totalAdDur, String adStatus, String adId) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(PARAM_AD_TYPE, adtype);
        labels.put(PARAM_SKIPPABLE_AD, skippableAd ? "Yes" : "No");
        labels.put(PARAM_AD_SKIP_STATUS, skipStatus);
        labels.put(PARAM_AD_PLAYED_DURATION, durationPlayed);
        labels.put(PARAM_TOTAL_AD_DURATION, totalAdDur);
        labels.put(PARAM_AD_STATUS, adStatus);
        labels.put(PARAM_AD_ID, adId);
        labels.put(NS_CATEGORY, EVENT_AD_CLICKED);
        notifyEvent(labels, EVENT_AD_CLICKED);
    }

    public void setEventAdEnded(CardData mCardData, String mMenuGroup, String source, String sourceName, int contentPosition, int carouselPosition,
                                String adtype, boolean skippableAd, String skipStatus, String durationPlayed, String totalAdDur, String adStatus, String adId) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(PARAM_AD_TYPE, adtype);
        labels.put(PARAM_SKIPPABLE_AD, skippableAd ? "Yes" : "No");
        labels.put(PARAM_AD_SKIP_STATUS, skipStatus);
        labels.put(PARAM_AD_PLAYED_DURATION, durationPlayed);
        labels.put(PARAM_TOTAL_AD_DURATION, totalAdDur);
        labels.put(PARAM_AD_STATUS, adStatus);
        labels.put(PARAM_AD_ID, adId);
        labels.put(NS_CATEGORY, EVENT_AD_ENDED);
        notifyEvent(labels, EVENT_AD_ENDED);
    }

    public void setEventAdSkipped(CardData mCardData, String mMenuGroup, String source, String sourceName, int contentPosition, int carouselPosition,
                                  String adtype, boolean skippableAd, String skipStatus, String durationPlayed, String totalAdDur, String adStatus, String adId) {
        if (isNullObject(mCardData)) {
            return;
        }
        HashMap<String, String> labels = new HashMap<>();
        labels.put(PARAM_CONTENT_TYPE, checkAndReturnValue(mCardData.generalInfo.type));
        labels.put(PARAM_CONTENT_ID, checkAndReturnValue(mCardData._id));
        labels.put(PARAM_CONTENT_NAME, checkAndReturnValue(mCardData.generalInfo.title));
        labels.put(PARAM_SERIES_NAME, checkAndReturnValue(mCardData.getTitle()));
        labels.put(PARAM_CONTENT_LANGUAGE, getLanguage(mCardData));
        labels.put(PARAM_CONTENT_GENRE, checkAndReturnValue(mCardData.generalInfo.category));
        labels.put(PARAM_GLOBAL_ID, checkAndReturnValue(mCardData.globalServiceId));
        labels.put(PARAM_SOURCE, checkAndReturnValue(source));
        labels.put(PARAM_SOURCE_NAME, checkAndReturnValue(sourceName));
        labels.put(PARAM_CONTENT_POSITION, checkAndReturnIntValue(contentPosition));
        labels.put(PARAM_CAROUSEL_POSITION, checkAndReturnIntValue(carouselPosition));
        labels.put(PARAM_TAB_NAME, checkAndReturnValue(mMenuGroup));
        labels.put(PARAM_CONTENT_MODEL, isContentPaidOrFree(mCardData.generalInfo.isSellable));
        labels.put(PARAM_USER_ID, checkAndReturnIntValue(PrefUtils.getInstance().getPrefUserId()));
        labels.put(PARAM_AD_TYPE, adtype);
        labels.put(PARAM_SKIPPABLE_AD, skippableAd ? "Yes" : "No");
        labels.put(PARAM_AD_SKIP_STATUS, skipStatus);
        labels.put(PARAM_AD_PLAYED_DURATION, durationPlayed);
        labels.put(PARAM_TOTAL_AD_DURATION, totalAdDur);
        labels.put(PARAM_AD_STATUS, adStatus);
        labels.put(PARAM_AD_ID, adId);
        labels.put(NS_CATEGORY, EVENT_AD_SKIPPED);
        notifyEvent(labels, EVENT_AD_SKIPPED);
    }

    private void notifyEvent(HashMap<String, String> labels, String eventName) {

    }

    private String isContentPaidOrFree(Boolean isSellable) {
        return isSellable ? "Paid" : "Free";
    }

    private String replaceSpacesWithUnderScores(String actualString) {
        return actualString.replace(" ", UNDER_SCORE);
    }

    private String getLanguage(CardData mData) {
        if (mData != null && mData.content != null && mData.content.language != null && mData.content.language.size() > 0) {
            return replaceSpacesWithUnderScores(mData.content.language.get(0));
        }
        return "";
    }

    private String checkAndReturnValue(String value) {
        if (value == null || TextUtils.isEmpty(value)) {
            return "NA";
        }
        return value;
    }

    private Boolean isNullObject(CardData mCardData) {
        return mCardData == null || mCardData.generalInfo == null;
    }

    private String checkAndReturnIntValue(int intValue) {
        return String.valueOf(intValue);
    }

    private String checkAndReturnLongValue(long intValue) {
        return String.valueOf(intValue);
    }
}
