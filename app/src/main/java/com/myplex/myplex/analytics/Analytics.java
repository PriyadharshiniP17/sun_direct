package com.myplex.myplex.analytics;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
/*import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;*/
import com.google.firebase.analytics.FirebaseAnalytics;
import com.myplex.analytics.AnalyticsConstants;
import com.myplex.analytics.MyplexAnalytics;
import com.myplex.api.APIConstants;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CardData;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.LocationInfo;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.BuildConfig;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Srikanth on 13-Jan-16.
 */
public class Analytics {

    public static final short MAX_PLAYED_TIME_MIN = 240;
    public static final String PARAM_EMAIL_ID = "email id";
    public static final String PARAM_OTP = "otp";
    public static final String PARAM_OTP_DETECTION = "otp detection";
    public static final String PROPERTY_CARRIER_NAME = "carrier name";

    public static final String VALUE_SOURCE_CAROUSEL = "carousel";
    public static final String VALUE_SOURCE_BANNER = "banner";
    public static final String VALUE_SOURCE_SEARCH = "search";
    public static final String VALUE_SOURCE_DETAILS_SIMILAR_CONTENT = "similar content";
    public static final String VALUE_SOURCE_DETAILS_EPG = "epg";
    public static final String VALUE_SOURCE_DETAILS_PROMO_BANNER = "promo banner";
    public static final String VALUE_SOURCE_DETAILS = "details";
    public static final String VALUE_SOURCE_DOWNLOADED_VIDEOS = "downloaded videos";
    public static final String VALUE_SOURCE_WATCHLIST_VIDEOS = "watchlist";
    public static final String VALUE_SOURCE_LIVE = "live tv";
    public static final String VALUE_SOURCE_NOTIFICATION = "notification";
    public static final String VALUE_SOURCE_FILTER = "filter";
    public static final String ARTIST_PROFILE = "artistProfile";
    private static final boolean ENABLE_SESSION_EVENTS = false;
    public static final String INCORRECT_EMAIL_ID = "incorrect email id";
    public static final String INCORRECT_MOBILE_NO = "incorrect mobile no";
    public static final String EVENT_VALUE_OTP_TIME_OUT = "otp timeout";
    public static final String NO = "No";
    public static final String YES = "Yes";

    public static final String DOWNLOAD_MANAGER_UNAVAILABLE = "download manager unavailable";

    public static int THRESHOLD_EVENT_PRIORITY = EventPriority.LOW;
    private static final String TAG = Analytics.class.getSimpleName();
    static String mCarouselTitle = null;
    private Analytics() {

    }

    /*public static final Tracker easyTracker = ApplicationController.getTracker();*/

    // Content Types
    public static final String TYPE_MOVIES = "movies";
    public static final String TYPE_MUSIC = "music";
    public static final String TYPE_VIDEOS = "videos";
    public static final String TYPE_TVSHOWS = "tv shows";

    public static final String TYPE_FREE = "avod";
    public static final String FREE = "free";
    public static final String PAID = "paid";
    //screen names
    public static final String SCREEN_SPLASH = "splash";
    public static final String SCREEN_TVGUIDE = "tv guide";
    public static final String SCREEN_CHANNEL_EPG = "channel epg";
    public static final String SCREEN_FILTER = "filter";
    public static final String SCREEN_SEARCH = "search";
    public static final String SCREEN_PROGRAM_DETAILS = "program details";
    public static final String SCREEN_MUSIC = "music";
    public static final String SCREEN_TERMS_N_CONDITIONS = "terms and conditions";
    public static final String SCREEN_ABOUT = "about";
    public static final String SCREEN_HELP = "help";
    public static final String SCREEN_MOVIE_LIST = "movies list";
    public static final String SCREEN_TV_SHOW_DETAILS = "tv shows";
    public static final String SCREEN_VOD_DETAILS = "vod details";
    public static final String SCREEN_MOVIE_DETAILS = "movie details";
    public static final String SCREEN_OFFER = "offer";

    public static final String SCREEN_TVSHOWS_LIST = "tv shows list";
    public static final String SCREEN_VIDEOS_LIST = "videos list";
    public static final String SCREEN_OTP_SCREEN = "otp screen";
    public static final String SCREEN_LOGIN_SCREEN = "login screen";
    public static final String SCREEN_PROFILE_UPDATE_SCREEN = "profile update screen";
    public static final String SCREEN_MUSIC_VIDEOS = "music video";
    public static final String SCREEN_MUSIC_VIDEOS_LIST = "music video list";
    public static final String SCREEN_MUSIC_VIDEOS_DETAILS = "music video detail";
    public static final String SCREEN_KIDS = "kids";
    public static final String SCREEN_KIDS_LIST = "kids list";

    //CATEGORIES
    public static final String CATEGORY_BROWSE = "browse";
    private static final String CATEGORY_SEARCH = "search";
    public static final String CATEGORY_LIVETV = "live tv";
    public static final String CATEGORY_VIDEOS = "videos";
    private static final String CATEGORY_TVSHOWS = "tv shows";
    private static final String CATEGORY_BREAKING_NEWS = "breaking news";
    public static final String CATEGORY_MOVIE = "movie";
    public static final String CATEGORY_MUSIC = "music";
    private static final String CATEGORY_NOTIFICATION = "notification";
    private static final String CATEGORY_UX = "ux";
    public static final String CATEGORY_SDK = "sdk";
    private static final String CATEGORY_YOUTUBE = "youtube";
    public static final String CATEGORY_MUSIC_VIDEO = "music video";
    private static final String CATEGORY_SUBSCRIPTION = "subscription";


    //EVENTS
    public static final String EVENT_BROWSED_MOVIES = "browsed movies";
    public static final String EVENT_BROWSED_LIVE_TV = "browsed live tv";
    public static final String EVENT_BROWSED_CHANNEL_EPG = "browsed tv channel epg";
    public static final String EVENT_BROWSED_PROGRAM_DETAILS = "browsed program details";
    public static final String EVENT_BROWSED_MUSIC = "browsed music";
    public static final String EVENT_INLINE_SEARCH = "inline search";
    public static final String EVENT_BROWSED_TVSHOWS = "browsed tv shows";
    public static final String EVENT_BROWSED_VIDEOS = "browsed videos";
    public static final String EVENT_BROWSED_MOVIES_LIST = "browsed movies list";
    public static final String EVENT_TIMESHIFT_LIVE_TV = "timeshift live tv";
    public static final String EVENT_ACTION_HUNGAMA_CONTENT = "hungama content";
    public static final String EVENT_ACTION_HUNGAMA_HOME = "hungama home";
    public static final String EVENT_BROWSED_YOUTUBE = "browsed youtube";
    public static final String EVENT_BROWSED_MUSIC_VIDEOS = "browsed music videos";
    public static final String EVENT_BROWSED_KIDS = "browsed kids";

    public static final String EVENT_ACTION_FILTERED = "filtered";
    public static final String EVENT_ACTION_RECIEVED = "recieved";
    public static final String EVENT_ACTION_OPENS = "opens";
    public static final String EVENT_ACTION_RECIEVED_AUTO_REMINDER = "recieved auto reminder";
    public static final String EVENT_ACTION_OPENS_AUTO_REMINDER = "opens auto reminder";
    public static final String EVENT_ACTION_BROWSE_SONY_SPORTS = "browsed sony sports";

    public static final String NUMBER_OF_CARDS = "number of cards";
    public static final String GA_CURRENCY = "INR";
    public static String internetConectivityType = "";

    //mixpanel event names
    public static String EVENT_DEVICE_REGISTRATION_SUCCESS = "device registration success";
    public static String EVENT_DEVICE_REGISTRATION_FAILED = "device registration failed";
    public static String EVENT_SIGN_UP_SUCCESS = "sign up success";
    public static String EVENT_SIGN_UP_FAILED = "sign up failed";
    public static String EVENT_OFFER_ACTIVATION_SUCCESS = "offer activation success";
    public static String EVENT_OFFER_ACTIVATION_FAILED = "offer activation failed";

    public static String EVENT_PAYMENT_OPTION_SElECTED = "payment option selected";
    public static String EVENT_PAYMENT_SUCCESS = "payment success";
//    public static String EVENT_PAYMENT_FAILED = "payment failed";
    public static String EVENT_HUNGAMA_CONTENT = "hungama content";
    public static String EVENT_HUNGAMA_HOME_SCREEN = "hungama home screen";
    public static String EVENT_DEVICE_FAILED_TO_FETCH_OFFERS = "error unable to fetch offer packages";
    public static String EVENT_INITIATING_FEEDBACK = "initiating feedback";
    public static String EVENT_PROVIDED_FEEDBACK = "provided feedback";
    public static String EVENT_OTP_LOGIN_INITIATED = "otp login initiated";
    public static String EVENT_OTP_LOGIN_SUCCESS = "otp login success";
    public static String EVENT_OTP_LOGIN_FAILED = "otp login failed";
    public static String EVENT_EMAIL_INITIATED = "profile email initiated";
    public static String EVENT_EMAIL_SUCCESS = "profile email success";

    //mixpanel event properties
    public static String DEVICE_ID = "device id";
    public static String DEVICE_DESC = "device description";
    public static final String REASON_FAILURE = "reason for failure";
    public static String ERROR_CODE = "error code";
    public static String ACCOUNT_TYPE = "account type";
    public static String USER_ID = "user id";
    public static String JOINED_ON = "joined on";
    public static String SIGN_UP_OPTION = "sign up option";
    public static String PAYMENT_PRICE = "payment price";
    public static String PAYMENT_METHOD =  "payment method";
    public static final String PARTNER_NAME = "partner name";
    public static final String PURCHASE_TYPE = "purchase type";
    public static String PARTNER_CONTENT_ID = "partner content id";
    public static final String CONTENT_TYPE = "content type";
    public static final String NOTIFICATION_TITLE = "notification title";
    static final String NOTIFICATION_NID = "nid";
    public static final String PROPERTY_CONTENT_GENRE = "content genre";
    public static final String PROPERTY_CONTENT_SUB_GENRE = "content sub genre";
    public static final String PROPERTY_CONTENT_SIZE = "content size (MB)";
    public static final String TIME_TAKEN_TO_DOWNLOAD = "time taken to download (in minutes)";
    public static final String PROPERTY_CONTENT_LANGUAGE = "content language";
    private static final String PROPERTY_SHOW_NAME = "show name";
    public static final String PROPERTY_PROGRAM_NAME = "program name";
    public static final String PROPERTY_CHA_NAME = "channel name";
    public static final String PROPERTY_CONTENT_PARTNER_NAME = "content partner name";
    static final String PROPERTY_NETWORK = "network";
    static final String PROPERTY_STREAM_PROFILE = "stream profile";
    private static final String PROPERTY_TAB_NAME = "tab name";
    public static final String PROPERTY_CONTENT_ID = "content id";
    public static final String PROPERTY_CONTENT_NAME = "content name";
    public static final String PROPERTY_GENRE_FILTER = "genre filter";
    public static final String PROPERTY_LANGUAGE_FILTER = "language filter";
    public static final String PROPERTY_EPG_START_DATE = "epg start date";
    public static final String PROPERTY_DATA_CONNECTION = "data connection";
    public static final String PROPERTY_CITY = "city";
    public static final String PROPERTY_COUNTRY = "country";
    public static final String PROPERTY_GPS = "gps";
    public static final String PROPERTY_POSTAL_CODE = "postal code";
    public static final String PROPERTY_LATTITUDE = "Lat";
    public static final String PROPERTY_LONGITUDE = "Long";
    public static final String PROPERTY_SOURCE = "source";
    public static final String PROPERTY_SOURCE_DETAILS = "source details";
    public static final String PROPERTY_VIDEO_COMPLETED = "video completed";

    public static final String PROPERTY_QUALITY = "quality";
    //mixpanel property's values
    public static String ACCOUNT_CLIENT = "sundirect";
    //mixpanel people properties name
    public static final String MIXPANEL_PEOPLE_HUNGMA_SDK_USED = "hungama sdk used";
    public static final String MIXPANEL_PEOPLE_USER_ID = "user id";
    public static final String MIXPANEL_PEOPLE_FILTERED_BY_CATEGORY = "filtered by category";
    public static final String MIXPANEL_PEOPLE_MENU_USED = "menu used";
    public static final String MIXPANEL_PEOPLE_SEARCHED_FOR_CONTENT = "searched for content";
    public static final String MIXPANEL_PEOPLE_EPG_REMINDER_SET = "epg reminder set";
    public static final String MIXPANEL_PEOPLE_EPG_BROWSED = "epg browsed";
    public static final String MIXPANEL_PEOPLE_DOWNLOADED_VIDEOS = "downloaded videos";
    public static final String MIXPANEL_PEOPLE_VIDEO_GENRE_PLAYED = "video genre played";
    public static final String MIXPANEL_PEOPLE_VIDEO_SUBGENRE_PLAYED = "video sub-genre played";
    public static final String MIXPANEL_PEOPLE_NOTIFICATION_TAGS = "notification_tags";
    public static final String MIXPANEL_PEOPLE_TV_STREAMED_FOR_MIN = "tv streamed for (m)";
    public static final String MIXPANEL_PEOPLE_TIME_PLAYED_IN_SEC = "time played (in seconds)";
    public static final String MIXPANEL_PEOPLE_MOVIE_STREAMED_FOR_MIN = "movies streamed for (m)";
    public static final String MIXPANEL_PEOPLE_SETTINGS_LANGUAGE_USED = "settings language used";
    public static final String MIXPANEL_PEOPLE_JOINING_DATE = "joining date";
    private static final String MIXPANEL_PROPERTY_RATING = "rating";
    public static final String MIXPANEL_CHROMCAST_USED = "chromecast used";

    // mix panel event names
    public static final String EVENT_PLAYED_TV_CHANNEL = "played tv channel";
    public static final String EVENT_PLAYED_TV_SHOW = "played tv show";
    public static final String EVENT_PLAYED_VIDEOS = "played videos";
    public static final String EVENT_PLAYED_MOVIE = "played movie";
    public static final String EVENT_PLAYED_MUSIC_VIDEO = "played music video";
    private static final String EVENT_PLAYED_ON_CHROMECAST = "played on chromecast";
    public static final String EVENT_ACTION_OFFER = "offer";
    public static final String HEIFEN_WITH_SPACE_ENCLOSED = " - ";

    public static final String EVENT_PLAYED_DOWNLOADED_VIDEO = "played downloaded video";
    public static final String EVENT_DOWNLOADED_VIDEO = "downloaded video";
    public static final String EVENT_UNABLE_TO_PLAY = "error unable to play";
    public static final String EVENT_PLAYED_YOUTUBE_VIDEO = "played youtube video";
    public static final String EVENT_NOTIFICATION_OPEN = "notification open";
    public static final String EVENT_NOTIFICATION_PLAYED = "notification played";
    public static final String EVENT_NOTIFICATION_UNABLE_TO_PLAY = "error notification unable to play";
    public static final String EVENT_UNABLE_TO_FETCH_VIDEO_LINK = "error unable to fetch video link";
    public static final String EVENT_BROWSE_TAB = "browse ";
    public static final String EVENT_BROWSE_TV_CHANNEL_EPG = "browse tv channel epg";
    public static final String EVENT_APPLIED_FILTER = "applied filter";
    public static final String EVENT_FAILED_TO_LOAD_EPG_LIST = "error unable to fetch epg list";

    public static final String EVENT_LABEL_OFFER_SCREEN = "offer screen";
    public static final String EVENT_LABEL_OFFER_SKIPPED = "skipped offer";
    public static final String EVENT_LABEL_CG_PAGE = "cg page";
//    public static final String EVENT_LABEL_PAYMENT_FAILED = "payment failed";
    public static final String EVENT_LABEL_PAYMENT_SUCCESS = "payment success";
//    public static final String EVENT_LABEL_PAYMENT_CANCEL = "payment cancel";
    public static final String PROPERTY_TRACKING_ID = "module tracking id";
    public static final String NULL_VALUE = "na";
    public static final String DOWNLOAD_INITIATED_EVENT = "download initiated";
    public static final String PROPERTY_CONTENT_MODEL = "content model";
    public static final String PROPERTIES_CONTENT_LANGUAGE = "content language";
    public static final String PROPERTY_SERIES_NAME = "series name";

/*    otp login failed
    sign up failed
    error unable to play*/
    static CardData mVODCardData;
    public static void setVODSuperCardData(CardData mVODData) {
        mVODCardData = mVODData;
    }

    public static void setVideosCarouselName(String title) {
        mCarouselTitle = title;
    }


    public static String getVideosCarouselName() {
        return mCarouselTitle;
    }

    public static void mixpanelBrowseTabName(String tabName) {
        if(tabName == null){
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PROPERTY_TAB_NAME, tabName);
        String event = Analytics.EVENT_BROWSE_TAB + " " + tabName.toLowerCase();
        Analytics.trackEvent(Analytics.EventPriority.LOW, event, params);
    }

    public static void mixpanelBrowseChannelEpg(String _id, String globalServiceName) {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(globalServiceName) ? APIConstants.NOT_AVAILABLE : globalServiceName);
        params.put(Analytics.PROPERTY_CONTENT_ID, TextUtils.isEmpty(_id) ? APIConstants.NOT_AVAILABLE : _id);
        String event = Analytics.EVENT_BROWSE_TV_CHANNEL_EPG;
        Analytics.trackEvent(Analytics.EventPriority.LOW, event, params);
    }

    public static void mixpanelEventAppliedFilter(String langValues, String genreValues) {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PROPERTY_GENRE_FILTER, TextUtils.isEmpty(langValues) ? APIConstants.NOT_AVAILABLE : langValues);
        params.put(Analytics.PROPERTY_LANGUAGE_FILTER, TextUtils.isEmpty(genreValues) ? APIConstants.NOT_AVAILABLE : genreValues);
        String event = Analytics.EVENT_APPLIED_FILTER;
        Analytics.trackEvent(Analytics.EventPriority.LOW, event, params);
    }

    public static void mixpanelEventFailedFetchEpgList(String reason, String serverDateFormat) {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.REASON_FAILURE, TextUtils.isEmpty(reason) ? APIConstants.NOT_AVAILABLE : reason);
        params.put(Analytics.PROPERTY_EPG_START_DATE, TextUtils.isEmpty(serverDateFormat) ? APIConstants.NOT_AVAILABLE : serverDateFormat);
        String network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
        params.put(Analytics.PROPERTY_NETWORK, TextUtils.isEmpty(network) ? APIConstants.NOT_AVAILABLE : network);
        String event = Analytics.EVENT_FAILED_TO_LOAD_EPG_LIST;
        Analytics.trackEvent(Analytics.EventPriority.LOW, event, params);
    }

    public static void mixpanelEventUnableToFetchOffers(String error, String errorCode) {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.DEVICE_ID, PrefUtils.getInstance().getPrefDeviceid());
        params.put(Analytics.DEVICE_DESC, ApplicationController.getAppContext().getString(R.string.osname));
        params.put(Analytics.REASON_FAILURE, error);
        params.put(Analytics.ERROR_CODE, errorCode);
        params.put(Analytics.USER_ID, PrefUtils.getInstance().getPrefUserId() == 0 ? APIConstants.NOT_AVAILABLE : PrefUtils.getInstance().getPrefUserId() + "");
        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_DEVICE_FAILED_TO_FETCH_OFFERS, params);
    }

    public static void mixpanelInitiatingFeedback() {
        Map<String, String> params = new HashMap<>();
        Analytics.trackEvent(EventPriority.LOW, Analytics.EVENT_INITIATING_FEEDBACK, params);
    }


    public static void mixpanelProvidedFeedback(int rating) {
        Map<String, String> params = new HashMap<>();
        params.put(MIXPANEL_PROPERTY_RATING,String.valueOf(rating));
        Analytics.trackEvent(EventPriority.HIGH, Analytics.EVENT_PROVIDED_FEEDBACK, params);
        Map<String, Object> cparams = new HashMap<>();
        cparams.put(MIXPANEL_PROPERTY_RATING,String.valueOf(rating));
        CleverTap.setProperties(cparams);
    }

    public static void gaBrowseMusicVideo(String title) {
        if(title == null){
            return;
        }
        Analytics.createEventGA(Analytics.CATEGORY_MUSIC_VIDEO,
                ACTION_TYPES.browse.name(), title.toLowerCase(),
                1L);
    }

    public static void browseViewAllEvent(String tabName, CarouselInfoData carouselData) {
        String action = tabName + " " + (carouselData != null ? carouselData.title : "");
        action += " list";
        Analytics.createEventGA(Analytics.CATEGORY_BROWSE, action.toLowerCase(), Analytics.NUMBER_OF_CARDS, 1L);

    }

    public static void mixpanelDeviceRegistrationSuccess(Map<String, String> params) {
        trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_DEVICE_REGISTRATION_SUCCESS,params);
        Map<String, Object> cParams = new HashMap<>();
        cParams.putAll(params);
        CleverTap.event(Analytics.EventPriority.HIGH, Analytics.EVENT_DEVICE_REGISTRATION_SUCCESS,cParams);
    }

    public static void mixpanelDeviceRegistrationFailed(Map<String, String> params) {
        trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_DEVICE_REGISTRATION_FAILED,params);
        Map<String, Object> cParams = new HashMap<>();
        cParams.putAll(params);
        CleverTap.event(Analytics.EventPriority.HIGH, Analytics.EVENT_DEVICE_REGISTRATION_FAILED,cParams);
    }

    public static void mixpanelOfferActivationFailed(Map<String, String> params) {
        trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_OFFER_ACTIVATION_FAILED,params);
        Map<String, Object> cParams = new HashMap<>();
        cParams.putAll(params);
        CleverTap.event(Analytics.EventPriority.HIGH, Analytics.EVENT_OFFER_ACTIVATION_FAILED,cParams);
    }

    public static void mixpanelOfferActivationSuccess(Map<String, String> params) {
        trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_OFFER_ACTIVATION_SUCCESS,params);
        Map<String, Object> cParams = new HashMap<>();
        cParams.putAll(params);
        CleverTap.event(Analytics.EventPriority.HIGH, Analytics.EVENT_OFFER_ACTIVATION_SUCCESS,cParams);
    }

    public static void mixpanelOTPLoginSuccess(Map<String, String> params) {
        trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_OTP_LOGIN_SUCCESS,params);
        Map<String, Object> cParams = new HashMap<>();
        cParams.putAll(params);
    }

    public static void mixpanelOTPLoginFailed(Map<String, String> params) {
        trackEvent(Analytics.EventPriority.MEDIUM, Analytics.EVENT_OTP_LOGIN_FAILED,params);
        Map<String, Object> cParams = new HashMap<>();
        cParams.putAll(params);
    }

    public enum ACTION_TYPES {play, pause, stop, browse, download, search, played, error,delete,complete}

  /*  public static void createTransactionGA(String transactionid, String affiliation, Double revenue, Double tax, Double shippingCost) {
        easyTracker.send(new HitBuilders.TransactionBuilder()
                .setTransactionId(transactionid)  // (String) Transaction ID
                .setAffiliation(affiliation)      // (String) Affiliation
                .setRevenue(revenue)              // (Double) Revenue
                .setTax(tax)                     // (Double) Tax
                .setShipping(shippingCost)       // (String) Currency code
                .build());
    }

    public static void createItemGA(String transactionid, String productName, String productSKU, String productCategory,
                                    Double productPrice, Long quantity) {
        easyTracker.send(new HitBuilders.ItemBuilder()
                .setTransactionId(transactionid)     // (String) Transaction ID
                .setName(productName)                // (String) Product name
                .setSku(productSKU)                  // content id (String) Product SKU
                .setCategory(productCategory)        // (String) Product category
                .setPrice(productPrice)              // (Double) Product price
                .setQuantity(quantity)              // (Long) Product quantity
                .setCurrencyCode(Analytics.GA_CURRENCY) // (String) Currency code
                .build());

    }*/

    public static void createEventGA(String category, String action, String label, Long value) {
        if(ApplicationController.IS_DEBUG_BUILD){
            LoggerD.debugAnalytics("createEventGA category- " + category + " action- " + action + " label- " + label + " value- "+ value);
        }
        if (Analytics.CATEGORY_BROWSE.equalsIgnoreCase(category) || Analytics.CATEGORY_BROWSE.equalsIgnoreCase(action)
                || ACTION_TYPES.pause.name().equalsIgnoreCase(action)
                || ACTION_TYPES.stop.name().equalsIgnoreCase(action)) {
            return;
        }
       /* easyTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).setValue(value).build());
        Bundle params = new Bundle();
        params.putString("category", category);
        params.putString("action", action);
        params.putString("label", label);
        params.putLong("value", value);*/
       // FirebaseAnalytics.getInstance(ApplicationController.getAppContext()).logEvent(FirebaseAnalytics.Event.VIEW_ITEM,params);
    }

/*    public static void createUserTimingGA(String category, Long time, String name, String label) {
        easyTracker.send(new HitBuilders.TimingBuilder().setCategory(category).setValue(time).setVariable(name).setLabel(label).build());
    }

    public static void createSocialGA(String network, String action, String label) {
        easyTracker.send(new HitBuilders.SocialBuilder()
                .setNetwork(network) // Social network (required)
                .setAction(action)  // Social action (required)
                .setTarget(label)   // Social target
                .build());

    }*/

    /*public static void createExceptionGA(String exceptionDesc, boolean bool) {
        // False indicates a fatal exception
        easyTracker.send(new HitBuilders.ExceptionBuilder().setDescription(exceptionDesc).setFatal(bool).build());

    }

    public static void createScreenGA(String screenName) {
        easyTracker.setScreenName(screenName);
        easyTracker.send(new HitBuilders.AppViewBuilder().build());

        //For firebase Analytics
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "screen");
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, screenName);
       // FirebaseAnalytics.getInstance(ApplicationController.getAppContext()).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params);
        LoggerD.debugAnalytics("screenName: " + screenName);
    }*/

    public static void gaBrowse(String ctype, long swipeCount) {
        //Log.d(TAG,"gaBrowse: type: " + ctype);
        if (ctype.equalsIgnoreCase(APIConstants.TYPE_LIVE)) {
            Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_LIVE_TV, Analytics.NUMBER_OF_CARDS, swipeCount);
        } else if (ctype.equalsIgnoreCase(Analytics.TYPE_MOVIES)) {
            Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_MOVIES, Analytics.NUMBER_OF_CARDS, swipeCount);
        } else if (ctype.equalsIgnoreCase(Analytics.TYPE_MUSIC)) {
            Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_MUSIC, Analytics.NUMBER_OF_CARDS, swipeCount);
        } else if (ctype.equalsIgnoreCase(APIConstants.TYPE_VODCHANNEL)) {
            Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_TVSHOWS, Analytics.NUMBER_OF_CARDS, swipeCount);
        } else if (ctype.equalsIgnoreCase(APIConstants.TYPE_VODCATEGORY)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(ctype)) {
            Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_VIDEOS, Analytics.NUMBER_OF_CARDS, swipeCount);
        } else if (ctype.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)) {
            Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_YOUTUBE, Analytics.NUMBER_OF_CARDS, swipeCount);
        } else if (ctype.equalsIgnoreCase(APIConstants.TYPE_MUSIC_VIDEO)) {
            Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_MUSIC_VIDEOS, Analytics.NUMBER_OF_CARDS, swipeCount);
        }

    }

    public static void gaBrowseProgramDetails(String programName) {
        Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_PROGRAM_DETAILS, programName, 1L);
    }

    public static void gaBrowseChannelEpg(String channelName) {
        Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_BROWSED_CHANNEL_EPG, channelName, 1L);
    }

    public static void gaBrowseFilter(String filterName, long swipeCount) {
        Analytics.createEventGA(Analytics.CATEGORY_BROWSE, Analytics.EVENT_ACTION_FILTERED, filterName, swipeCount);
    }

    public static void gaInlineSearch(String keyword, long results) {
        Analytics.createEventGA(Analytics.CATEGORY_SEARCH, Analytics.EVENT_INLINE_SEARCH, keyword,
                results);
    }

    public static void gaEventBrowsedCategoryContentType(CardData carouselData) {
        if(carouselData == null || carouselData.generalInfo == null){
            return;
        }
        if(APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)){
            Analytics.gaBrowseTVShows(carouselData.generalInfo.title);
        } else if(APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)){
            Analytics.gaBrowseVideos(carouselData.generalInfo.title);
        } else if(APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)){
            Analytics.gaBrowseVideos(carouselData.generalInfo.title);
        } else if (APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)) {
            Analytics.gaBrowseYoutubeVideos(carouselData.generalInfo.title);
        } else if (APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)) {
            Analytics.gaBrowseMusicVideo(carouselData.generalInfo.title);
        } else if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)) {
            Analytics.createEventGA(Analytics.CATEGORY_MOVIE,
                    ACTION_TYPES.browse.name(), carouselData.generalInfo.title.toLowerCase(),
                    1L);
        }
    }

    public static void gaBrowseVideos(String title) {
        if(title == null){
            return;
        }
        Analytics.createEventGA(Analytics.CATEGORY_VIDEOS,
                ACTION_TYPES.browse.name(), title.toLowerCase(),
                1L);
    }

    public static void gaBrowseTVShows(String title) {
        if(title == null){
            return;
        }
        Analytics.createEventGA(Analytics.CATEGORY_TVSHOWS,
                ACTION_TYPES.browse.name(), title.toLowerCase(),
                1L);
    }

    public static void gaDownloadVideos(String title) {
        if(title == null){
            return;
        }
        Analytics.createEventGA(Analytics.CATEGORY_VIDEOS,
                ACTION_TYPES.download.name(), title.toLowerCase(),
                1L);
    }

    public static void gaBrowseYoutubeVideos(String title) {
        if(title == null){
            return;
        }
        Analytics.createEventGA(Analytics.CATEGORY_YOUTUBE,
                ACTION_TYPES.browse.name(), title.toLowerCase(),
                1L);
    }


    public static void gaPlayedVideoTimeCalculationForYoutube(String action, String title, String _id, String subgenre, String publishingHouseName) {
        //Log.d(TAG, "gaPlayedVideoTimeCalculationForYoutube- title: " + title + " action: " + action + " _id: " + _id + " subgenre: " + subgenre);
        if (title == null) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, _id); //2
        params.put(Analytics.PROPERTY_CONTENT_NAME, title); //3
        params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, publishingHouseName); //3
        params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, TextUtils.isEmpty(subgenre) ? APIConstants.NOT_AVAILABLE : subgenre);
        if (mVODCardData != null
                && mVODCardData.generalInfo != null
                && mVODCardData.generalInfo.title != null) {
            params.put(Analytics.PROPERTY_CONTENT_GENRE, mVODCardData.generalInfo.title);
        } else if (!TextUtils.isEmpty(mCarouselTitle)) {
            params.put(Analytics.PROPERTY_CONTENT_GENRE, mCarouselTitle);
        }
        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_PLAYED_YOUTUBE_VIDEO, params);

        mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_TIME_PLAYED_IN_SEC);
        Analytics.createEventGA(Analytics.CATEGORY_YOUTUBE, action, title.toLowerCase(), 1L);
        if (ACTION_TYPES.play.name().equalsIgnoreCase(action)) {
            AppsFlyerTracker.eventPlayedYoutube(new HashMap<String, Object>());
        }
    }

    public static void gaPlayedVideoTimeCalculation(String action, String type, CardData mData, long ptimeMinutes, long ptimeSec) {
        //Log.d(TAG, "gaPlayedVideoTimeCalculation- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title + " ptimeMinutes: " + ptimeMinutes + " ptimeSec: " + ptimeSec + " action: " + action);

        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.title == null) {
            return;
        }

        if (ptimeMinutes > MAX_PLAYED_TIME_MIN) {
            return;
        }

        try {
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
                JSONArray genreItems = new JSONArray();
                genreItems.put(mVODCardData.generalInfo.title
                        .toLowerCase());
                mixpanelUnionPeople(Analytics.MIXPANEL_PEOPLE_VIDEO_GENRE_PLAYED, genreItems);
                ArrayList<String> listTags = new ArrayList<>();
                listTags.add(mVODCardData.generalInfo.title
                        .toLowerCase());
                CleverTap.addProperties(Analytics.MIXPANEL_PEOPLE_VIDEO_GENRE_PLAYED, listTags);
            }
            if (mData.content != null
                    && mData.content.genre != null
                    && mData.content.genre.size() > 0) {
                JSONArray subgenreItems = new JSONArray();
                subgenreItems.put(mData.content.genre.get(0).name
                        .toLowerCase());
                mixpanelUnionPeople(Analytics.MIXPANEL_PEOPLE_VIDEO_SUBGENRE_PLAYED, subgenreItems);
                ArrayList<String> listTags = new ArrayList<>();
                listTags.add(mData.content.genre.get(0).name
                        .toLowerCase());
                CleverTap.addProperties(Analytics.MIXPANEL_PEOPLE_VIDEO_SUBGENRE_PLAYED, listTags);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.MIXPANEL_PEOPLE_TIME_PLAYED_IN_SEC, "" + ptimeSec); //3
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
        params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName); //3
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
        }
        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);

        String event = null;
        if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(type)) {
            params.put(Analytics.PROPERTY_SHOW_NAME, mVODCardData == null || mVODCardData.generalInfo == null
                    || TextUtils.isEmpty(mVODCardData.generalInfo.title) ? "NA" : mVODCardData.generalInfo.title);
            event = Analytics.EVENT_PLAYED_TV_SHOW;
            Analytics.createEventGA(Analytics.CATEGORY_TVSHOWS, action, mData.generalInfo.title.toLowerCase(), ptimeMinutes);
            if (ACTION_TYPES.play.name().equalsIgnoreCase(action)
                    || ACTION_TYPES.pause.name().equalsIgnoreCase(action)) {
                AppsFlyerTracker.eventPlayedTvshows(new HashMap<String, Object>());
            }
        } else if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(type)) {
            event = Analytics.EVENT_PLAYED_VIDEOS;
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
                params.put(Analytics.PROPERTY_CONTENT_GENRE, mVODCardData.generalInfo.title);
            } else if (!TextUtils.isEmpty(mCarouselTitle)
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(type)) {
                params.put(Analytics.PROPERTY_CONTENT_GENRE, mCarouselTitle);
                if (mData.content != null
                        && mData.content.genre != null
                        && mData.content.genre.size() > 0) {
                    params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
                }
            }
            if (DownloadUtil.isFileExist(mData._id + ".mp4",ApplicationController.getAppContext())) {

                String bytesDownloaded = "NA";
                CardDownloadedDataList downloadlist = null;
                try {
                    downloadlist = ApplicationController.getDownloadData();
                    if (DownloadUtil.isFileExist(mData._id + ".mp4",ApplicationController.getAppContext()) && downloadlist != null) {

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
                event = Analytics.EVENT_PLAYED_DOWNLOADED_VIDEO;
            }
            if (ACTION_TYPES.play.name().equalsIgnoreCase(action)
                    || ACTION_TYPES.pause.name().equalsIgnoreCase(action)) {
                AppsFlyerTracker.eventPlayedVideo(new HashMap<String, Object>());
            }
            Analytics.createEventGA(Analytics.CATEGORY_VIDEOS, action, mData.generalInfo.title.toLowerCase(), ptimeMinutes);
        } else if (APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(type)) {
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
                params.put(Analytics.PROPERTY_CONTENT_GENRE, mVODCardData.generalInfo.title);
            }
            if (mData.content != null
                    && mData.content.genre != null
                    && mData.content.genre.size() > 0) {
                params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
            }
            event = Analytics.EVENT_PLAYED_YOUTUBE_VIDEO;
            Analytics.createEventGA(Analytics.CATEGORY_BREAKING_NEWS, action, mData.generalInfo.title.toLowerCase(), ptimeMinutes);
        }
        if (ACTION_TYPES.play.name().equalsIgnoreCase(action)
                || ACTION_TYPES.pause.name().equalsIgnoreCase(action)) {
            Analytics.trackEvent(Analytics.EventPriority.HIGH, event, params);
        }
    }

    private static void mixpanelUnionPeople(String key, JSONArray items) {
       /* if (mMixPanelPeople == null
                || !(ApplicationController.ENABLE_MIXPANEL_API
                && PrefUtils.getInstance().getPrefEnableMixpanel())) {
            return;
        }
        mMixPanelPeople.union(key, items);*/
    }


    public static void gaPlayedOnCastTV(String action, CardData mData) {
        //Log.d(TAG, "gaPlayedOnCastTV- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title + " ptimeMinutes: " + " action: " + action);

        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3

        if(isProgram(mData)){
            params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
            params.put(Analytics.PROPERTY_CONTENT_NAME, mData.globalServiceName); //3
        }
        String contentPartnerName =  mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName;
        params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
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
        }

        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);

        mixpanelIncrementPeopleProperty(MIXPANEL_CHROMCAST_USED, 1L);
        if (ACTION_TYPES.play.name().equalsIgnoreCase(action)) {
            Analytics.trackEvent(Analytics.EventPriority.HIGH, EVENT_PLAYED_ON_CHROMECAST, params);
        }

    }

    private static boolean isProgram(CardData mData) {
        return mData == null || mData.generalInfo == null || mData.generalInfo.type == null || !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)? false : true;
    }

    public static void gaPlayedVideoTimeCalculation(String action, CardData mData, long ptimeMinutes, long ptimeSec) {
        //Log.d(TAG, "gaPlayedVideoTimeCalculation- type: " + mData.generalInfo.type + " title: " + mData.generalInfo.title + " ptimeMinutes: " + ptimeMinutes + " ptimeSec: " + ptimeSec + " action: " + action);

        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }

        if (ptimeMinutes > MAX_PLAYED_TIME_MIN) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_ID, mData._id); //2
        params.put(Analytics.MIXPANEL_PEOPLE_TIME_PLAYED_IN_SEC, "" + ptimeSec); //3
        params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
        String contentPartnerName =  mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName;
        params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
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
        }
        params.put(Analytics.PROPERTY_CONTENT_GENRE, contentGenre);

        String event = null;
        if (APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(mData.generalInfo.type)) {
            event = Analytics.EVENT_PLAYED_MUSIC_VIDEO;
            Analytics.createEventGA(Analytics.CATEGORY_MUSIC_VIDEO, action, mData.generalInfo.title.toLowerCase(), ptimeMinutes);
            if (ACTION_TYPES.play.name().equalsIgnoreCase(action)
                    || ACTION_TYPES.pause.name().equalsIgnoreCase(action)) {
                AppsFlyerTracker.eventPlayedMusicVideo(new HashMap<String, Object>());
            }
        } else if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
                params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                params.put(Analytics.PROPERTY_PROGRAM_NAME, mData.generalInfo.title); //3
                params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
            } else {
                params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title); //3
            }
            contentPartnerName = mData == null || mData.contentProvider == null ? contentPartnerName : mData.contentProvider;
            params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, contentPartnerName); //3
            event = Analytics.EVENT_PLAYED_TV_CHANNEL;
            Analytics.createEventGA(Analytics.CATEGORY_LIVETV, action, mData.generalInfo.title.toLowerCase(), ptimeMinutes);
            if (ACTION_TYPES.play.name().equalsIgnoreCase(action)
                    || ACTION_TYPES.pause.name().equalsIgnoreCase(action)) {
                AppsFlyerTracker.eventPlayedLiveTv(new HashMap<String, Object>());
            }
        } else if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)) {
            Analytics.createEventGA(Analytics.CATEGORY_MOVIE, action, mData.generalInfo.title.toLowerCase(), ptimeMinutes);
            event = Analytics.EVENT_PLAYED_MOVIE;
            if (ACTION_TYPES.play.name().equalsIgnoreCase(action)
                    || ACTION_TYPES.pause.name().equalsIgnoreCase(action)) {
                AppsFlyerTracker.eventPlayedMovie(new HashMap<String, Object>());
            }
        } else if (APIConstants.TYPE_MUSIC.equalsIgnoreCase(mData.generalInfo.type)) {
            Analytics.createEventGA(Analytics.CATEGORY_MUSIC, action, mData.generalInfo.title.toLowerCase(), ptimeMinutes);
        } else {
            event = Analytics.EVENT_PLAYED_VIDEOS;
            params.put(Analytics.CONTENT_TYPE, mData.generalInfo.type);
            if (mVODCardData != null && mVODCardData.generalInfo != null
                    && mVODCardData.generalInfo.title != null) {
                params.put(Analytics.PROPERTY_CONTENT_GENRE, mVODCardData.generalInfo.title);
            }  else if (!TextUtils.isEmpty(mCarouselTitle)
                    && !APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                params.put(Analytics.PROPERTY_CONTENT_GENRE, mCarouselTitle);
                if (mData.content != null
                        && mData.content.genre != null
                        && mData.content.genre.size() > 0) {
                    params.put(Analytics.PROPERTY_CONTENT_SUB_GENRE, mData.content.genre.get(0).name);
                }
            }
            try {
                CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
                if (DownloadUtil.isFileExist(mData._id + ".mp4",ApplicationController.getAppContext()) && downloadlist != null) {
                    String bytesDownloaded = "NA";

                    if (downloadlist != null
                            && downloadlist.mDownloadedList != null
                            && downloadlist.mDownloadedList.containsKey(mData._id)) {
                        CardDownloadData cardDownloadData = downloadlist.mDownloadedList.get(mData._id);
                        bytesDownloaded = "" + cardDownloadData.mDownloadedBytes;
                    }
                    event = Analytics.EVENT_PLAYED_DOWNLOADED_VIDEO;
                    params.put(Analytics.PROPERTY_CONTENT_SIZE, bytesDownloaded);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Analytics.createEventGA(Analytics.CATEGORY_VIDEOS, action, mData.generalInfo.title.toLowerCase(), ptimeMinutes);
        }
        if (ACTION_TYPES.play.name().equalsIgnoreCase(action)) {
            Analytics.trackEvent(Analytics.EventPriority.HIGH, event, params);
        }

    }

/*
    public static HitBuilders.EventBuilder setCustomDimensions(HitBuilders.EventBuilder appViewBuilder) {
        appViewBuilder.setCustomDimension(1, internetConectivityType);
        int sdkVersion = Build.VERSION.SDK_INT;
        boolean isExoEnabled = PrefUtils.getInstance().getPrefIsExoplayerEnabled();
        boolean isDVRContent = PrefUtils.getInstance().getPrefIsExoplayerDvrEnabled();
        if (sdkVersion >= 16 && (isDVRContent || isExoEnabled)) {
            appViewBuilder.setCustomDimension(2, "Custom");
        } else {
            appViewBuilder.setCustomDimension(2, "Native");
        }


        return appViewBuilder;
    }
*/

    public static void playerBitrateEvent(Context context, String category, String action, String label, int value) {
        if ("sessions".equalsIgnoreCase(category) || ENABLE_SESSION_EVENTS) {
            return;
        }
        internetConectivityType = SDKUtils.getInternetConnectivity(context);
      /*  easyTracker.send(setCustomDimensions(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value))
                .build());*/
    }

    public static void gaNotificationEvent(String eventAction, String eventLable) {
        createEventGA(CATEGORY_NOTIFICATION, eventAction, eventLable, 1L);
    }

    public static void gaBrowseCarouselSection(String tab, String section, String contentName) {
        if(section == null || contentName == null){
            return;
        }
        section = tab + " "  + section;
        //Log.d(TAG, section + " " + contentName);
        Analytics.createEventGA(Analytics.CATEGORY_BROWSE, section.toLowerCase(), contentName.toLowerCase(), 1L);
    }

    public static void gaPlayedTimeShiftChannel(String channelName) {
        if(TextUtils.isEmpty(channelName)){
            return;
        }
        Analytics.createEventGA(Analytics.CATEGORY_UX, EVENT_TIMESHIFT_LIVE_TV, channelName.toLowerCase(), 1L);
        AppsFlyerTracker.eventTimeShiftLiveTV();
    }

    private static JSONObject getJSON(Map<String, String> params){
        JSONObject data = new JSONObject();
        Set<String> keySet = params.keySet();

        for(String key:keySet){
//			mData.mEntries.add(object.get(key));

            try {
                if(params.get(key) != null){
                    data.put(key,params.get(key));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return data;
    }

    public static void trackEvent(int mixpanelEventPriority, String aEventName, Map<String, String> params) {

        if (ApplicationController.ENABLE_MIXPANEL_API
                && PrefUtils.getInstance().getPrefEnableMixpanel()
                && mixpanelEventPriority >= PrefUtils.getInstance().getPrefMixpanelEventPriority()) {
            /*mMixPanel.track(aEventName, getJSON(params));*/
            LoggerD.debugAnalytics("MixpanelAPI evevntName: " + aEventName);
            if (ApplicationController.IS_DEBUG_BUILD && params != null) {
                for (String key : params.keySet()) {
                    LoggerD.debugAnalytics("\nkey- " + key +
                            " value- " + params.get(key));
                }
            }
        }
    }

    public static void mixpanelIncrementPeopleProperty(String propertyName) {
       /* if (mMixPanelPeople == null
                || propertyName == null
                || !(ApplicationController.ENABLE_MIXPANEL_API
                && PrefUtils.getInstance().getPrefEnableMixpanel())) {
            return;
        }*/
        LoggerD.debugAnalytics("mixpanelSetPeopleProperty:" + " propertyName- " + propertyName);
        //mMixPanelPeople.increment(propertyName, 1d);
        /*try {
            Map<String, Object> params = new HashMap<>();
            Object existingValue = CleverTap.getProperty(propertyName);
            int time = 0;
            if(existingValue != null){
                time = (int) CleverTap.getProperty(propertyName);
            }
            params.put(propertyName, time + 1);
            CleverTap.createProfile(params);
        } catch (Exception e){
            e.printStackTrace();
        }
*/
    }

    public static void mixpanelSetPeopleProperty(String key, Object value) {
        if(key == null
                || value == null)
            return;
        if(!String.valueOf(value).equalsIgnoreCase("$email")){
            Map<String, Object> params = new HashMap<>();
            params.put(key, value);
            CleverTap.setProperties(params);
        }
        if (ApplicationController.IS_DEBUG_BUILD) {
            LoggerD.debugAnalytics("mixpanelSetPeopleProperty:" + " key- " + key +
                    " value- " + value);
        }
        /*if (mMixPanelPeople == null
                || !(ApplicationController.ENABLE_MIXPANEL_API
                && PrefUtils.getInstance().getPrefEnableMixpanel())) {
            return;
        }*/
      /*  mMixPanelPeople.set(key, value);*/

    }


    public static final class EventPriority{
        public static final int HIGH = 3;
        public static final int MEDIUM = 2;
        public static final int LOW = 1;
    }

    public static void mixPanelPeopleSetNotifcaionTags(String tagText) {

        if(null != tagText){
            try{
                JSONArray tags = new JSONArray();
                ArrayList<String> listTags = new ArrayList<>();
                for(String tag : tagText.split(",")){
                    tags.put(tag.toLowerCase());
                    listTags.add(tag.toLowerCase());
                }
                mixpanelUnionPeople(Analytics.MIXPANEL_PEOPLE_NOTIFICATION_TAGS, tags);
                CleverTap.addProperties(Analytics.MIXPANEL_PEOPLE_NOTIFICATION_TAGS, listTags);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public static void mixpanelIncrementPeopleProperty(String propertyName, long ptimeMinutes) {
        LoggerD.debugAnalytics("mixpanelIncrementPeopleProperty: propertyName: " + propertyName + " ptimeMinutes: " + ptimeMinutes);
        /*if (mMixPanelPeople == null || propertyName == null
                || !(ApplicationController.ENABLE_MIXPANEL_API
                && PrefUtils.getInstance().getPrefEnableMixpanel())) {
            return;
        }

        mMixPanelPeople.increment(propertyName, ptimeMinutes);*/
       /* try {
            Map<String, Object> params = new HashMap<>();
            Object existingValue = CleverTap.getProperty(propertyName);
            int time = 0;
            if (existingValue != null) {
                time = (int) CleverTap.getProperty(propertyName);
            }
            params.put(propertyName, ptimeMinutes + time);
            CleverTap.createProfile(params);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public static void mixpanelSetOncePeopleProperty(String key, Object value) {
        LoggerD.debugAnalytics("mixpanelSetOncePeopleProperty: key: " + key + " value: " + value);
        if (key == null || value == null) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        CleverTap.setProperties(params);
        /*if (mMixPanelPeople == null
                || key == null
                || value == null
                || !(ApplicationController.ENABLE_MIXPANEL_API
                && PrefUtils.getInstance().getPrefEnableMixpanel())) {
            return;
        }
        mMixPanelPeople.setOnce(key, value);*/

    }


    public static void gaNotificationPlayBackFailed(String mNotificationTitle, String nid, CardData mData, String profileSelect) {
        if (mNotificationTitle == null) {
            return;
        }
        mixpanelNotificationPlayBackFailed(mNotificationTitle, nid, mData, profileSelect);
        createEventGA(CATEGORY_NOTIFICATION, ACTION_TYPES.error.name(), mNotificationTitle, 1L);
        CleverTap.eventNotificationPlayBackFailed(mNotificationTitle, nid, mData, profileSelect);
    }

    public static void gaNotificationPlayBackSuccess(String mNotificationTitle, String nid, CardData mData, String profileSelect) {
        if (mNotificationTitle == null) {
            //Log.d(TAG,"ignore gaNotificationPlayBackSuccess event- mNotificationTitle- " + mNotificationTitle + " nid- " + nid);
            return;
        }
        mixpanelNotificationPlayBackSuccess(mNotificationTitle, nid, mData, profileSelect);
        createEventGA(CATEGORY_NOTIFICATION, ACTION_TYPES.played.name(), mNotificationTitle, 1L);
    }

    public static void mixPanelDownloadsVideo(String contentName, String contentId, String bytesDownloaded, String downloadTime, String publishingHouseName) {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PROPERTY_CONTENT_NAME, contentName);
        params.put(Analytics.PROPERTY_CONTENT_ID, contentId);
        params.put(Analytics.PROPERTY_CONTENT_SIZE, bytesDownloaded);
        params.put(Analytics.TIME_TAKEN_TO_DOWNLOAD, downloadTime);
        params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, publishingHouseName); //3
        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_DOWNLOADED_VIDEO, params);
    }

    public static void mixPanelUnableToPlayVideo(CardData mData, String error, String profileSelected) {
        try {
            CardData cardData = mData;
            if (cardData == null) return;
            if (cardData.generalInfo == null) return;
            String contentName = cardData.generalInfo.title;
            Map<String, String> params = new HashMap<>();
            params.put(Analytics.PROPERTY_CONTENT_NAME, contentName);
            params.put(Analytics.PROPERTY_CONTENT_ID, cardData._id);
            params.put(Analytics.CONTENT_TYPE, mData.generalInfo.type == null ? APIConstants.NOT_AVAILABLE : mData.generalInfo.type);
            params.put(Analytics.REASON_FAILURE, error);
            params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData.publishingHouse == null || mData.publishingHouse == null ? APIConstants.NOT_AVAILABLE : mData.publishingHouse.publishingHouseName); //3
            if(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
                params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider); //3
                if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
                    params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                    params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
                }
            }
            params.put(Analytics.PROPERTY_STREAM_PROFILE, profileSelected);
            String network = APIConstants.NOT_AVAILABLE;
            if(!TextUtils.isEmpty(SDKUtils.getInternetConnectivity(ApplicationController.getAppContext()))){
                network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
            }
            params.put(Analytics.PROPERTY_NETWORK, network);
            String event = Analytics.EVENT_UNABLE_TO_PLAY;
            Analytics.trackEvent(EventPriority.MEDIUM, event, params);
        } catch (Exception excp) {
            //Log.d(TAG, excp.toString());
        }

    }

    public static void mixpanelNotificationOpened(String notificationTitle, String nid) {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.NOTIFICATION_TITLE , notificationTitle);
        if(!TextUtils.isEmpty(nid)){
            params.put(Analytics.NOTIFICATION_NID , nid);
        }
        Analytics.trackEvent(Analytics.EventPriority.HIGH, EVENT_NOTIFICATION_OPEN, params);
        CleverTap.eventNotificationOpened(notificationTitle, nid);
    }

    private static void mixpanelNotificationPlayBackSuccess(String notificationTitle, String nid, CardData mData, String profileSelect) {
        Map<String, String> params = new HashMap<>();
        if(!TextUtils.isEmpty(nid)){
            params.put(Analytics.NOTIFICATION_NID , nid);
        }
        if(!TextUtils.isEmpty(notificationTitle)){
            params.put(Analytics.NOTIFICATION_TITLE , notificationTitle);
        }


        if(mData != null && mData.generalInfo != null){

            params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title);
            params.put(Analytics.PROPERTY_CONTENT_ID, mData._id);
            params.put(Analytics.CONTENT_TYPE, mData.generalInfo.type == null ? APIConstants.NOT_AVAILABLE : mData.generalInfo.type);
            params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.publishingHouse == null ? APIConstants.NOT_AVAILABLE : mData.publishingHouse.publishingHouseName); //3
            if(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
                params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider); //3
                if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
                    params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                    params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
                }
            }
            params.put(Analytics.PROPERTY_STREAM_PROFILE, profileSelect);
            params.put(Analytics.PROPERTY_NETWORK, SDKUtils.getInternetConnectivity(ApplicationController.getAppContext()));
        }
        Analytics.trackEvent(Analytics.EventPriority.HIGH, EVENT_NOTIFICATION_PLAYED, params);
        CleverTap.eventNotificationPlayBackSuccess(notificationTitle, nid, mData, profileSelect);
    }

    private static void mixpanelNotificationPlayBackFailed(String notificationTitle, String nid, CardData mData, String profileSelect) {
        Map<String, String> params = new HashMap<>();
        if(!TextUtils.isEmpty(nid)){
            params.put(Analytics.NOTIFICATION_NID , nid);
        }
        if(!TextUtils.isEmpty(notificationTitle)){
            params.put(Analytics.NOTIFICATION_TITLE , notificationTitle);
        }
        if(mData != null && mData.generalInfo != null){
            params.put(Analytics.PROPERTY_CONTENT_NAME, mData.generalInfo.title);
            params.put(Analytics.PROPERTY_CONTENT_ID, mData._id);
            params.put(Analytics.CONTENT_TYPE, mData.generalInfo.type == null ? "NA" : mData.generalInfo.type);
            params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.publishingHouse == null ? "NA" : mData.publishingHouse.publishingHouseName); //3
            if(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
                params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider); //3
                if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
                    params.put(Analytics.PROPERTY_CONTENT_ID, mData.globalServiceId); //2
                    params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(mData.globalServiceName) ? "NA" : mData.globalServiceName);
                }
            }
            params.put(Analytics.PROPERTY_STREAM_PROFILE, profileSelect);
            params.put(Analytics.PROPERTY_NETWORK, SDKUtils.getInternetConnectivity(ApplicationController.getAppContext()));
        }
        Analytics.trackEvent(Analytics.EventPriority.HIGH, EVENT_NOTIFICATION_UNABLE_TO_PLAY, params);
    }


    public static void setSuperProperties(Map<String, Object> params) {
        if(params == null){
            return;
        }
        JSONObject properties = new JSONObject();
        try {
            for (String key: params.keySet()){
                properties.put(key, params.get(key));
                LoggerD.debugAnalytics("setSuperProperties: key- " + key + " value- " + params.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } // default value
        /*mMixPanel.registerSuperProperties(properties);*/
    }

    public static void mixPanelUnableToFetchVideoLink(String contentName, String contentPartnerName, String _id, String nid, String reasonForFailure, String notificationTitle) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put(Analytics.PROPERTY_CONTENT_ID, _id);
            params.put(Analytics.PROPERTY_CONTENT_NAME, TextUtils.isEmpty(contentName) ? APIConstants.NOT_AVAILABLE : contentName);
            params.put(Analytics.PROPERTY_CONTENT_PARTNER_NAME, TextUtils.isEmpty(contentPartnerName) ? APIConstants.NOT_AVAILABLE : contentPartnerName);
            params.put(Analytics.NOTIFICATION_NID, nid == null ? APIConstants.NOT_AVAILABLE : nid);
            params.put(Analytics.REASON_FAILURE, reasonForFailure == null ? APIConstants.NOT_AVAILABLE : reasonForFailure);
            params.put(Analytics.NOTIFICATION_TITLE, notificationTitle == null ? APIConstants.NOT_AVAILABLE : notificationTitle);
            String network = SDKUtils.getInternetConnectivity(ApplicationController.getAppContext());
            params.put(Analytics.PROPERTY_NETWORK, TextUtils.isEmpty(network) ? APIConstants.NOT_AVAILABLE : network);
            String event = Analytics.EVENT_UNABLE_TO_FETCH_VIDEO_LINK;
            Analytics.trackEvent(Analytics.EventPriority.HIGH, event, params);
        } catch (Exception excp) {
            //Log.d(TAG, excp.toString());
        }
    }

    public static void setMixPanelEmail(String email){

        // alias device id to user's email id
//        mMixPanel.alias(email, mMixPanel.getDistinctId());
        mixpanelSetPeopleProperty("$email", email == null ? APIConstants.NOT_AVAILABLE : email);
        mixpanelSetPeopleProperty(PARAM_EMAIL_ID, email == null ? APIConstants.NOT_AVAILABLE : email);
        /*mMixPanel.flush();*/
    }


    public static void mixpanelProfileEmailInitiated(String email) {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PARAM_EMAIL_ID, email == null ? APIConstants.NOT_AVAILABLE : email);
        params.put(Analytics.USER_ID, PrefUtils.getInstance().getPrefUserId() == 0 ? APIConstants.NOT_AVAILABLE : PrefUtils.getInstance().getPrefUserId() + "");
        Analytics.trackEvent(EventPriority.MEDIUM, Analytics.EVENT_EMAIL_INITIATED, params);
    }


    public static void mixpanelProfileEmailSuccess() {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.PARAM_EMAIL_ID, PrefUtils.getInstance().getPrefEmailID() == null ? APIConstants.NOT_AVAILABLE : PrefUtils.getInstance().getPrefEmailID());
        params.put(Analytics.USER_ID, PrefUtils.getInstance().getPrefUserId() == 0 ? APIConstants.NOT_AVAILABLE : PrefUtils.getInstance().getPrefUserId() + "");
        Analytics.trackEvent(EventPriority.MEDIUM, Analytics.EVENT_EMAIL_SUCCESS, params);
    }


    public static void mixpanelEventHungamaContent(CardData cardData) {

        if(cardData == null){
            return;
        }
        Map<String, String> params = new HashMap<>();
        if(cardData != null
                && cardData.generalInfo != null){
            if(!TextUtils.isEmpty(cardData.generalInfo.type)) {
                params.put(Analytics.CONTENT_TYPE, cardData.generalInfo.type);
            }
            if(!TextUtils.isEmpty(cardData.generalInfo.title)) {
                Analytics.createEventGA(Analytics.CATEGORY_SDK,Analytics.EVENT_ACTION_HUNGAMA_CONTENT,cardData.generalInfo.title,1l);
                params.put(Analytics.PROPERTY_CONTENT_NAME, cardData.generalInfo.title);
            }
            params.put(Analytics.PARTNER_CONTENT_ID, cardData.generalInfo.partnerId);
            params.put(Analytics.PROPERTY_CONTENT_ID, cardData._id);
        }

        if(cardData.publishingHouse != null
                && cardData.publishingHouse.publishingHouseName != null
                && !TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName)){
            params.put(Analytics.PARTNER_NAME, cardData.publishingHouse.publishingHouseName);
        }

        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_HUNGAMA_CONTENT, params);
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_HUNGMA_SDK_USED);
        AppsFlyerTracker.aftrackEvent(Analytics.EVENT_ACTION_HUNGAMA_CONTENT, new HashMap<String, Object>(params), Analytics.EventPriority.HIGH);
    }

    public static void mixpanelEventHungamaContent(String partnerName, String partnerId, String contentId) {
        Map<String, String> params = new HashMap<>();

        if (!TextUtils.isEmpty(partnerName)) {
            params.put(Analytics.PARTNER_NAME, partnerName);
        }
        if (!TextUtils.isEmpty(contentId)) {
            params.put(Analytics.PROPERTY_CONTENT_ID, contentId);
        }
        if (!TextUtils.isEmpty(contentId)) {
            params.put(Analytics.PARTNER_CONTENT_ID, partnerId);
        }
        Analytics.trackEvent(Analytics.EventPriority.LOW, Analytics.EVENT_HUNGAMA_CONTENT, params);
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_HUNGMA_SDK_USED);
        AppsFlyerTracker.aftrackEvent(Analytics.EVENT_ACTION_HUNGAMA_CONTENT, new HashMap<String, Object>(params), Analytics.EventPriority.HIGH);
    }

    public static void mixpanelEventHungamaSDKHome() {
        Map<String, String> params = new HashMap<>();
        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_HUNGAMA_HOME_SCREEN, params);
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_HUNGMA_SDK_USED);
        Analytics.createEventGA(Analytics.CATEGORY_SDK,Analytics.EVENT_ACTION_HUNGAMA_HOME,"",1l);
        AppsFlyerTracker.aftrackEvent(Analytics.EVENT_ACTION_HUNGAMA_HOME,new HashMap<String, Object>(),Analytics.EventPriority.HIGH);
    }

    public static void mixpanelIdentify() {

        if(/*mMixPanel == null ||*/ ApplicationController.getAppContext() == null){
            return;
        }
        // We also identify the current user with a distinct ID, and
        // register ourselves for push notifications from Mixpanel.

        String trackingDistinctId = getTrackingDistinctId();

        int userid = PrefUtils.getInstance().getPrefUserId();

        if (userid > 0) {
            trackingDistinctId = String.valueOf(userid);
        }

        Map<String, Object> params = new HashMap<>();
        params.put(Analytics.USER_ID, userid == 0 ? "NA" : trackingDistinctId);
        params.put(Analytics.PROPERTY_DATA_CONNECTION, SDKUtils.getInternetConnectivity(ApplicationController.getAppContext()));
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        params.put(Analytics.PROPERTY_CITY, locationInfo == null || locationInfo.area == null ? "NA" : locationInfo.area);
        params.put(Analytics.PROPERTY_COUNTRY, locationInfo == null || locationInfo.country == null ? "NA" : locationInfo.country);
        params.put(Analytics.PROPERTY_GPS,locationInfo == null ? "NA" :  "latitude: " + locationInfo.latitude + " longitude: " + locationInfo.longitude);
//        params.put(Analytics.PROPERTY_LONGITUDE,locationInfo == null ? "NA" :  locationInfo.longitude);
//        params.put(Analytics.PROPERTY_LATTITUDE,locationInfo == null ? "NA" :  locationInfo.latitude);
        params.put(Analytics.PARAM_EMAIL_ID, PrefUtils.getInstance().getPrefEmailID() == null ? "NA" : PrefUtils.getInstance().getPrefEmailID());
        params.put(CleverTap.PROPERTY_STATE, locationInfo == null || locationInfo.state == null ? "NA" : locationInfo.state);
        // each of the below mentioned fields are optional
        // if set, these populate demographic information in the Dashboard


        if((ApplicationController.ENABLE_MIXPANEL_API
                && PrefUtils.getInstance().getPrefEnableMixpanel())){
            Analytics.setSuperProperties(params);
            mixpanelSetPeopleProperties();

            /*mMixPanel.identify(trackingDistinctId);*/ //this is the distinct_id value that
            // will be sent with events. If you choose not to set this,
            // the SDK will generate one for you

            /*mMixPanel.getPeople().identify(trackingDistinctId);*/ //this is the distinct_id
            // that will be used for people analytics. You must set this explicitly in order
            // to dispatch people data.
/*
        // People analytics must be identified separately from event analytics.
        // The data-sets are separate, and may have different unique keys (distinct_id).
        // We recommend using the same distinct_id value for a given user in both,
        // and identifying the user with that id as early as possible.
*/
            Analytics.mixpanelSetPeopleProperty(Analytics.MIXPANEL_PEOPLE_USER_ID, String.valueOf(userid));
        }
        String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
        params.remove(Analytics.PARAM_EMAIL_ID);
        params.remove(Analytics.USER_ID);
        params.put(CleverTap.PROPERTY_IDENTITY, userid == 0 ? "NA" : trackingDistinctId);
        params.put(CleverTap.PROPERTY_EMAIL,PrefUtils.getInstance().getPrefEmailID() == null ? "NA" : PrefUtils.getInstance().getPrefEmailID());
        params.put(CleverTap.PROPERTY_PHONE, TextUtils.isEmpty(msisdn) ? "NA" : "+91"+msisdn);
        int version = BuildConfig.VERSION_CODE;
        params.put(CleverTap.PROPERTY_ANDROID_APP_VERSION, version);
        params.put(CleverTap.PROPERTY_ANDROID_MODEL, Util.getDeviceName());
        CleverTap.setProperties(params);
        params.put(AnalyticsConstants.USER_ID, userid == 0 ? "NA" : trackingDistinctId);
        MyplexAnalytics.getInstance().createProfile(params);
    }

    private static void mixpanelSetPeopleProperties() {
        TelephonyManager manager = (TelephonyManager) ApplicationController.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        Analytics.mixpanelSetPeopleProperty(Analytics.PROPERTY_CARRIER_NAME, manager.getNetworkOperatorName());
        Analytics.mixpanelSetPeopleProperty(Analytics.PROPERTY_DATA_CONNECTION, SDKUtils.getInternetConnectivity(ApplicationController.getAppContext()));
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        Analytics.mixpanelSetPeopleProperty(Analytics.PROPERTY_CITY, locationInfo == null || locationInfo.area == null ? "NA" : locationInfo.area);
        Analytics.mixpanelSetPeopleProperty(Analytics.PROPERTY_COUNTRY, locationInfo == null || locationInfo.country == null ? "NA" : locationInfo.country);
        Analytics.mixpanelSetPeopleProperty(Analytics.PROPERTY_POSTAL_CODE, locationInfo == null || locationInfo.postalCode == null ? "NA" : locationInfo.postalCode);
        Analytics.mixpanelSetPeopleProperty(Analytics.PROPERTY_GPS, locationInfo == null ? "NA" : "latitude: " + locationInfo.latitude + " langitude- " + locationInfo.longitude);
    }

    private static String getTrackingDistinctId() {
        String ret = PrefUtils.getInstance().getPrefTrackingDistinctId();
        if (ret == null) {
            ret = generateDistinctId();
            PrefUtils.getInstance().setPrefTrackingDistinctId(ret);
        }
        return ret;
    }

    // These disinct ids are here for the purposes of illustration.
    // In practice, there are great advantages to using distinct ids that
    // are easily associated with user identity, either from server-side
    // sources, or user logins. A common best practice is to maintain a field
    // in your users table to store mixpanel distinct_id, so it is easily
    // accesible for use in attributing cross platform or server side events.
    private static String generateDistinctId() {
        final Random random = new Random();
        final byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public static void gaCategorySubscription(String eventAction, String eventLabel) {
        createEventGA(CATEGORY_SUBSCRIPTION, eventAction, eventLabel, 1L);
    }

    public static void downloadInitiatedEvent(CardData cardData, String size, String reasonforfailure) {
        HashMap<String, Object> params = new HashMap<>();
        if (cardData.generalInfo != null) {
            params.put(Analytics.PROPERTY_CONTENT_NAME, convertToLowerCase(cardData.generalInfo.title));
            params.put(Analytics.CONTENT_TYPE, convertToLowerCase(cardData.generalInfo.type));
        }
        params.put(Analytics.PROPERTY_CONTENT_ID, cardData._id);
        params.put(Analytics.REASON_FAILURE, reasonforfailure);
        params.put(Analytics.PROPERTY_QUALITY, size);
        if (cardData.generalInfo.contentRights != null && cardData.generalInfo.contentRights.size() > 0 &&
                cardData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.TYPE_FREE)) {
            params.put(Analytics.PROPERTY_CONTENT_MODEL, convertToLowerCase(Analytics.FREE));
        } else {
            params.put(Analytics.PROPERTY_CONTENT_MODEL, convertToLowerCase(Analytics.PAID));
        }
        params.put(Analytics.PROPERTIES_CONTENT_LANGUAGE, getLangaugeOfCardDate(cardData));
        if (cardData.globalServiceId != null) {
            params.put(Analytics.PROPERTY_SERIES_NAME, Util.getSplitGlobalServiceId(cardData.globalServiceId));
        } else {
            params.put(Analytics.PROPERTY_SERIES_NAME, Analytics.NULL_VALUE);
        }

        //Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.DOWNLOAD_INITIATED_EVENT, params);
    }

    public static String convertToLowerCase(String stringToConvert) {
        if (stringToConvert != null && !stringToConvert.isEmpty()) {
            return stringToConvert.toLowerCase();
        }
        return NULL_VALUE;
    }

    public static String getLangaugeOfCardDate(CardData mData) {
        if (mData.content != null && mData.content.language != null) {
            StringBuilder language = new StringBuilder();
            for (String a : mData.content.language) {
                if (language.length() == 0) {
                    language.append(a);
                } else {
                    language.append("," + a);
                }
            }
            if (language.length() == 0) {
                return Analytics.NULL_VALUE;
            } else {
                return convertToLowerCase(String.valueOf(language));
            }
        }
        return NULL_VALUE;
    }

}

