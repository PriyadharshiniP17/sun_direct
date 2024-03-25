package com.myplex.analytics;

import com.myplex.model.CardData;
import com.myplex.model.CardDownloadedDataList;

/**
 * Created by Apalya on 2/2/2018.
 */

public class AnalyticsConstants {
    public static final boolean ENABLE_MIXPANEL_API = true;
    public static final String EVENT_PLAYED_VIDEO_FOR_30_SEC = "played video for 30 sec";
    public static boolean IS_DEBUG_BUILD = true;
    public static final String PROPERTY_ANDROID_APP_VERSION = "android app version";
    public static final String PROPERTY_ANDROID_MODEL = "android model";
    public static final String PROPERTY_STATE = "state";
    public static final short MAX_PLAYED_TIME_MIN = 240;
    public static final String PARAM_EMAIL_ID = "email id";
    public static final String PARAM_OTP_MODE = "manual";
    public static final String PARAM_OTP = "otp";
    public static final String PARAM_OTP_DETECTION = "otp detection";
    public static final String PROPERTY_CARRIER_NAME = "carrier name";
    public  static final String TEST_EVENT ="Test APP Launch";

    public static final String VALUE_SOURCE_CAROUSEL = "carousel";
    public static final String VALUE_SOURCE_BANNER = "banner";
    public static final String VALUE_SOURCE_SEARCH = "search";
    public static final String VALUE_SOURCE_DETAILS_SIMILAR_CONTENT = "similar content";
    public static final String VALUE_SOURCE_DETAILS_EPG = "epg";
    public static final String VALUE_SOURCE_DETAILS = "details";
    public static final String VALUE_SOURCE_DOWNLOADED_VIDEOS = "downloaded videos";
    public static final String VALUE_SOURCE_LIVE = "live tv";
    public static final String VALUE_SOURCE_NOTIFICATION = "notification";
    public static final String VALUE_SOURCE_FILTER = "filter";


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
    public static String PAYMENT_METHOD = "payment method";
    public static final String PARTNER_NAME = "partner name";
    public static final String PURCHASE_TYPE = "purchase type";
    public static String PARTNER_CONTENT_ID = "partner content id";
    public static final String CONTENT_TYPE = "content type";
    public static final String NOTIFICATION_TITLE = "notification title";
    public static final String NOTIFICATION_NID = "nid";
    public static final String PROPERTY_CONTENT_GENRE = "content genre";
    public static final String PROPERTY_CONTENT_SUB_GENRE = "content sub genre";
    public static final String PROPERTY_CONTENT_SIZE = "content size (MB)";
    public static final String TIME_TAKEN_TO_DOWNLOAD = "time taken to download (in minutes)";
    public static final String PROPERTY_CONTENT_LANGUAGE = "content language";
    private static final String PROPERTY_SHOW_NAME = "show name";
    public static final String PROPERTY_PROGRAM_NAME = "program name";
    public static final String PROPERTY_NETWORK = "network";
    public static final String PROPERTY_STREAM_PROFILE = "stream profile";
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

    public static final String PROPERTY_MOBILE_NETWORK = "mobile network";
    public static final String PROPERTY_LENGTH_OF_VIDEO = "length of the video";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_CONTENT_PARTNER_NAME = "content partner name";
    public static final String PROPERTY_PER_COMPLETED = "% completed";
    public static final String PROPERTY_DURATION_VIEWED = "duration viewed";
    public static final String PROPERTY_START_POINT = "start point";
    public static final String PROPERTY_CONTENT_TYPE = "content type";
    private static final String PROPERTY_CATEGORY = "category";
    public static final String PROPERTY_CONSUMPTION_TYPE = "consumption type";
    private static final String PROPERTY_CG_PAGE_TYPE = "page type";

    //mixpanel property's values
    public static String ACCOUNT_CLIENT = "SUnDirect";
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

    private static final String EVENT_NOTIFICATION_PLAYED = "notification played";
    private static final String EVENT_NOTIFICATION_UNABLE_TO_PLAY = "rror notification unable to play";

    public static final String EVENT_VIDEO_STARTED = "video started";
    public static final String EVENT_VIDEO_STOPPED = "video stopped";
    public static final String EVENT_VIDEO_PLAYED = "video played";
    public static final String EVENT_VIDEO_DETAILS_VIEWED = "video details viewed";
    public static final String EVENT_CATEGORY_VIEWED = "category viewed";
    public static final String EVENT_SEARCHED = "searched";
    public static final String EVENT_REGISTRATION_COMPLETED = "registration completed";
    public static final String EVENT_REGISTRATION_INITIATED = "registration initiated";
    public static final String EVENT_OTP_ENTERED = "otp entered";
    public static final String EVENT_OTP_STATUS = "otp status";
    public static final String EVENT_REGISTRATION_FAILED = "registration failed";
    public static final String EVENT_REGISTRATION_PAGE_VIEWED = "registration page viewed";
    public static final String EVENT_VIDEO_PAGE_VIEWED = "video viewed details";




    private static final String EVENT_PAGE_VIEWED = "page viewed";
    private static final String EVENT_CLICKED = "clicked";
    private static final String EVENT_FILTER_APPLIED = "filter applied";
    private static final String EVENT_PLAN_SELECTED = "plan selected";
    private static final String EVENT_CONSENT_PAGE_VIEWED = "consent page viewed";

    public static final String PROPERTY_KEYWORD = "keyword";
    public static final String PROPERTY_EMAIL = "Email";
    public static final String PROPERTY_PHONE = "Phone";
    public static final String PROPERTY_IDENTITY = "Identity";
    public static final String PROPERTY_OTP_MODE = "otp mode";
    public static final String PROPERTY_OTP_STATUS ="status";
    public static final String PROPERTY_REASON ="reason";
    public static final String PROPERTY_SOURCE ="source";
    public static final String PROPERTY_SOURCE_DETAILS ="source details";






    private static final String PROPERTY_PAGE_NAME = "page name";
    private static final String PROPERTY_ACTION = "action";
    private static final String PROPERTY_GENRE = "genre";
    private static final String PROPERTY_LANGUAGE = "language";
    private static final String PROPERTY_PLAN = "plan";
    private static final String PROPERTY_PAYMENT_MODE_SELECTED = "payment mode selected";
    private static final String PROPERTY_AMOUNT = "amount";
    private static final String PROPERTY_DURATION = "duration";


    private static CardDownloadedDataList sDownloadList;

    public final class EventPriority{
        public static final int HIGH = 3;
        public static final int MEDIUM = 2;
        public static final int LOW = 1;
    }
    public final class Source{
        public static final String BANNER ="banner";
        public static final String CAROUSEL = "carousel";
        public static final String SEARCH = "search";

    }

    public static CardData mVODCardData;
    public static String mCarouselTitle = null;


    public static void setVODSuperCardData(CardData mVODData) {
        mVODCardData = mVODData;
    }

    public static void setVideosCarouselName(String title) {
        String mCarouselTitle = title;
    }

    public static String getVideosCarouselName() {
        return mCarouselTitle;
    }


}
