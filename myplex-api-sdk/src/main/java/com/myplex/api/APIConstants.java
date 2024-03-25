package com.myplex.api;

import android.content.Context;
import android.text.TextUtils;

import com.myplex.api.request.security.APIEncryption;
import com.myplex.model.CardData;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.PreferredLanguageItem;
import com.myplex.util.PrefUtils;

import java.util.List;

/**
 * Created by Apalya on 21-Dec-15.
 */
public class APIConstants {

    //Menus

    public static final String MENU_HOME = "smartPhHome5x";
    public static final String MENU_LIVE_TV = "smartPhLive5x";
    public static final String MENU_SEARCH = "smartPhSearch5x";
    public static final String MENU_VOD = "smartPhMovies5x";
    //    VFCARE
    public static final String SENDER_VFCARE = "VFCARE";
    public static final String PARAM_APP_FRAG_TYPE = "fragment_type";
    public static final int PARAM_APP_FRAG_MOVIE = 0;
    public static final int PARAM_APP_FRAG_MUSIC = 1;
    public static final String PLAYSTORE_DOWNLOAD_PATH = "market://details?id=";
    public static final String PARAM_SCALE_WRAP = "scale=wrap";
    public static final String APPSFLYER_CONTENT_ID = "content_id";
    public static final String APPSFLYER_IS_FIRST_LAUNCH = "is_first_launch";
    public static final String SERVICE_NAME = "serviceName";
    public static final String SERVICE_NAME_PUBLISHER = "publisherGroupIds";
    public static final String CATEGORY_SCREEN_FILTER_INITIAL = "{\"categoryScreenFilters\":";
    public static final String CATEGORY_SCREEN_FILTER = "categoryScreenFilters";
    public static final String FULL_SCREEN_RESTRICTION_KEY = "fullScreenRestrictedPGrpIds";
    public static final String PLATFORM_MOBILE = "mobile";
    public static final String OS_ANDROID = "android";
    public static final String IMAGE_TYPE_SQUARE = "squareimage";
    public static final String FA_NOTIFICATION_PARAM_IMAGE_URL = "image_url";
    public static String searchConfigDataPath;
    public static String adFullScreenConfigPath;
    public static String adPopuNotificationConfigPath;
    public static String adPopuNotificationConfigPathV2;
    public static String searchConfigCarouselDataPath;
    public static final String LAST_SEARCH_QUERY = "last_query";
    public static final int PAGE_INDEX_COUNT = 10;
    public static final int PAGE_INDEX_COUNT_EPG = 10;
    public static final String PAYMENT_CHANNEL_INAPP = "INAPP";
    public static final String IMAGE_NOT_AVAILABALE = "Images/NoImage.jpg";
    public static final String VIDEO_TYPE_DOWNLOAD = "download";
    public static final String SIDENAV_MENU_NAME_LOGOUT = "Logout";
    public static final String TYPE_HOOQ = "hooq";
    public static final String TYPE_HUNGAMA = "hungama";
    public static final String TYPE_ZEE5 = "Zee5AIP";
    public static final String HTTP_NO_CACHE = "no-cache";
    public static final String NOTIFICATION_PARAM_NID = "nid";
    public static final String SUCCESS = "SUCCESS";

    public static final String NOT_AVAILABLE = "NA";
    public static final String LANGUAGES = "Languages";
    public static final String GENRE = "Genres";
    public static final String TV_SERIES = "tvSeries";
    public static final String PARTNER_NAME = "partner_name";
    public static final String PREFERRED_LANGUAGES = "preferredLanguages";
    public static final String PRIVACY_POLICY = "Privacy Policy";
    public static final String ABOUT_US = "About Us";
    public static final String FAQS = "FAQs";
    public static final String REFER_FRIEND = "Refer a Friend";
    public static final String RATE_US = "Rate US";
    public static final String CONTACT_US = "contactUs";
    public static final String CONTACT_US_TITLE_FOR_MORE="Contact us";
    public static final String TERMS_AND_CONDITIONS = "Terms and Conditions";
    public static final String PREMIUM = "premium";
    public static final String LanguageTitle = "altTitle";
    public static final String ALL = "all";
    public static final String LOGIN = "login";
    public static final String LAYOUT_TYPE_PORTRAIT_BANNER = "portraitBanner";
    public static final String LAYOUT_TYPE_SQUARE_BANNER = "squareBanner";
    public static final String IS_RESPONSE_FROM_SERVER = "response_server";
    public static final String APP_LANGAUGE = "app language";
    public static final String APP_LANGAUGE_SELECTED = "app_language_selected";
    public static final String DEFAULT_SELECTED_APP_LANGUAGE = "english";
    public static final String APP_LANGAUGE_URL = "app_language_url";
    public static final String VERNACULAR_LANGAUGE_URL = "vernacular_language_url";
    public static final String FULL_SCREEN_RESTRICTION_CONTENT_TYPE = "full_screen_restricted_content_types";
    public static final String FULL_SCREEN_RESTRICTION_CONTENT_TYPE_PUBLISHINGHOUSE_ID = "full_screen_restricted_publishinghouse_id";
    public static final String VERNACULAR_LANGUAGE = "vernacular_language";
    public static final String IS_REFERRAL_SDK_PRESENT = "referral_sdk_present";
    public static final String APP_LANGAUGE_STRING = "app language_string";
    public static final String APP_FIRST_LANGAUGE_STRING = "app_first_language_string";
    public static final String APP_ACTION_APP_LANGUAGE = "appLanguage";
    public static final String MORE = "more";
    public static final String SEASON = "season";
    public static final String PEOPLE_ALSO_WATCHED = "peopleAlsoWatch";
    public static final String UPCOMING_PROGRAMS = "upcomingPrograms";
    public static final String CURRENTLY_PLAY_OTHER_CHANNELS = "currentlyPlayingOtherChannel";
    public static final String TAB_NAME = "tab_name";
    public static final String BG_COLOR = "bg_Color";
    public static final String IS_AD_BEING_PLAYED = "is_ad_playing";
    public static final String IS_SUBTITLES_AVAILABLE = "is_subtitle_available";
    public static final String TRENDING_SEARCH = "search";
    public static final String SUBTITLE_FILE_NAME = "subtitle.vtt";
    public static final String LAUNCH_WEB_CHROME = "launchWebPageInChrome";

    public static final String CARD_ID = "id";
    public static final String DOWNLOAD_CONTENT_DATA = "content_data";

    static final int DEFAULT_READ_TIMEOUT_MILLIS = 30 * 1000; // 20s
    static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 30 * 1000; // 20s
    static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 25 * 1000; // 15s
    public static final String MESSAGE_TYPE = "message_type";
    public static final String NOTIFICATION_PARAM_MESSAGE_TYPE_INAPP = "IN-APP";
    public static final String AD_TYPE_OOYALA = "OOYALA";
    public static final String AD_TYPE_VAST3 = "VAST3";
    public static final CharSequence OTP_SENDER_ID = "VM-VFPLAY";
    public static final CharSequence OTP_SENDER_ID_2 = "59840200";
    public static final CharSequence SHREYAS_SENDER_ID_1 = "SENOTP";
    public static final CharSequence SHREYAS_SENDER_ID_2 = "VM-SENOTP";
    public static final CharSequence SHREYAS_SENDER_ID_3 = "AD_SENOTP";
    public static final CharSequence SHREYAS_SENDER_ID_4 = "JX-SENOTP";
    public static final CharSequence SHREYAS_SENDER_ID_5 = "SHRYET";
    public static final String ACTION_SMS_RECIEVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String TYPE_EROS_NOW = "erosnow";

    public static final String TYPE_BCN_PACKAGE = "com.apexott";
    public static final String TYPE_IDEA_PACKAGE = "com.ideacellular.digitalvideo";
    public static final String LEVEL_DYNAMIC = "dynamic";

    public static final String LEVEL_DEVICE_MAX = "devicemax";
    public static final String FAILED = "FAILED";
    public static final String HASH_CONTENT_NAME = "#CONTENT_NAME";
    public static final String HASH_DESCRIPTION = "#DESCRIPTION";
    public static final String HTTP_MARKET_URL = "https://play.google.com/store/apps/details?id=";
    public static final int PARAM_CAROUSEL_API_VERSION = 24;
    public static final String TYPE_OFFER = "offer";
    //  public static final String PARAM_CAROUSEL_API_FIELDS = "generalInfo,stats,images,relatedCast,publishingHouse,contents,relatedMedia,globalServiceId,previews,packages";
    public static final String PARAM_CAROUSEL_API_FIELDS = "generalInfo,images,relatedCast,publishingHouse,contents,relatedMedia,globalServiceId,previews,user,packages";
    public static final String PARAM_CAROUSEL_API_FIELDS_FOR_FAVORITES_INFO = "generalInfo,images,relatedCast,publishingHouse,contents,previews,relatedMedia,globalServiceId,user,currentdata";
    public static final String PARAM_SEARCH_API_FIELDS = "generalInfo,images,relatedCast,publishingHouse,contents,relatedMedia,reviews/user,globalServiceId";
    public static final String PARAM_RELATED_VOD_API_FIELDS = "generalInfo,images,relatedCast,publishingHouse,contents,relatedMedia,reviews/user,globalServiceId";
    public static final String PLAY_ERR_USER_NOT_SUBSCRIBED = "errorMessage::No packages defined for this non free content";
    public static final String ERR_PACKAGES_NOT_DEFINED = "errorMessage::Content is not free, user has not subscribed";
    public static final String PLAY_ERR_NON_LOGGED_USER = "ERR_NON_LOGGED_USER";
    public static final String PLAY_ERR_MANDATORY_UPDATE = "ERR_MANDATORY_UPDATE";
    public static final String PLAY_ERR_COUNTRY_NOT_ALLOWED = "ERR_COUNTRY_NOT_ALLOWED";
    public static final String MATCH_NOT_STARTED = "Match Not Started";
    public static final String AUTOPLAY_CONSUMPTION_TYPE = "autoplay";

    public static final String ALLPACKAGES = "allpackages";
    public static final String TYPE_APALYA_VIDEOS = "ApalyaVideos";
    public static final String LAUNCH_TAB_HASH = "launchTab#";
    public static final String PREFIX_HASH = "#";
    public static final String TYPE_ONBOARDING = "onboarding";
    public static final String TYPE_ONBOARDING_ANDROID = "onboarding_android";
    public static final String PARAM_CLEVERTAP_NOTIFICATION_WZRK_ = "wzrk_";
    public static final String PARAM_CLEVERTAP_NOTIFICATION_WZRK_PIVOT = "wzrk_pivot";
    public static final String PARAM_CLEVERTAP_NOTIFICATION_WZRK_DEFAULT = "wzrk_default";
    public static final String PARAM_CLEVERTAP_NOTIFICATION_NT = "nt";
    public static final String PARAM_CLEVERTAP_NOTIFICATION_NM = "nm";
    public static final String STREAMING = "streaming";
    public static final String PARAM_CONSUMPTION_STREAM = "stream";
    public static final String TYPE_YUPP_TV = "yupptv";
    public static final String TYPE_ALT_BALAJI = "altbalaji";
    public static final String LAUNCH_WEB_PAGE = "launchWebPage";
    public static final String LAUNCH_SUBSCRIBE = "Subscribe";
    public static final String USER_NOT_SUBSCRIBED = "not_subscribed";
    public static final String IS_YOU_TUBE_CONTENT = "is_youtube_content";
    public static final String IS_SELLABLE_FALSE = "is_sellable_false";
    public static final String USER_NOT_LOGGED_IN = "not_logged_in";
    public static final String USER_ALREADY_SUBSCRIBED = "already_Subscribed";

    //    Chrome Cast Constants
    public static final String PARAM_CHROME_CAST_DESCRIPTION = "description";
    public static final String PARAM_CHROME_CAST_DRM_LICENSE_URL = "drm_url";
    public static final String PARAM_CHROME_CAST_LICENSE_URL = "licenseUrl";
    public static final String PARAM_CHROME_CAST_PARTNER_TYPE = "providerType";

    public static final String VALUE_CHROME_CAST_APALYA = "apalya";
    public static final String PARAM_CHROME_CAST_CONTENT_TYPE = "videos/mpd";
    public static final String LAYOUT_TYPE_SIDE_NAV_MENU = "sideNavMenu";
    public static final String APP_ACTION_MYDOWNLOADS = "myDownloads";
    public static final String APP_ACTION_MYWATCHLIST = "playlist";
    public static final String APP_ACTION_MYFAVOURITES = "favorites";
    public static final String APP_ACTION_WATCH_HISTORY = "watchhistory";
    public static final String APP_ACTION_APP_SETTINGS = "AppSettings";
    public static final String APP_ACTION_ABOUT = "About";
    public static final String APP_ACTION_APP_MORE = "More";
    public static final String APP_ACTION_HELP = "Help";

    public static final String TYPE_ADBANNER_IMAGE = "adBannerImage";
    public static final String SUBTITLE_WEBVTT_TEXT = "webvtt";
    private static final String SCHEME_HTTPS = "https://";
    public static final String NOTIFICATION_PARAM_ADD_TO_WATCHLIST = "addToWatchList";
    public static final String NOTIFICATION_PARAM_ADD_TO_WATCHLIST_CONTENT_TYPE = "addToWatchListContentType";
    public static final String LAYOUT_TYPE_NESTED_CAROUSEL = "nestedCarousel";
    public static final String NOTIFICATION_PARAM_CAROUSEL_NAME = "c_name";
    public static final String NOTIFICATION_PARAM_PAGE_COUNT = "c_name_count";
    public static final String NOTIFICATION_PARAM_LAYOUT = "c_layout";
    public static final String NOTIFICATION_VIEW_ALL_DATA = "notificationViewAllData";
    public static final String NOTIFICATION_PARAM_CAROUSEL_TITLE = "c_title";
    public static final String NOTIFICATION_LAUNCH_URL = "external url";
    public static final String IS_PORTING_TO_SECURE_RANDOM_DONE = "is_porting_secure_random_completed";
    public static final String SLASH_EXP = "-";
    private static final String META_SUB_DOMAIN = "meta_subdomainn";
    private static final String META_SUB_DOMAIN_SCHEME = "domain_scheme";
    public static final String ACTION_TYPE_DEEPLINK = "deeplink";

    public static final String ACTION_TYPE = "actionType";
    public static final String ACTION_URL = "actionUrl";

    public static final String LAYOUT_TYPE_BIG_BANNER_VERTICAL_LAYOUT = "weeklyTrendingBigItem";
    public static final String LAYOUT_TYPE_WEEKLY_TRENDING_BIG_LAYOUT = "weeklyTrendingBigItem";
    public static final String LAYOUT_TYPE_WEEKLY_TRENDING_MEDIUM_LAYOUT = "weeklyTrendingMediumItem";
    public static final String LAYOUT_TYPE_BIG_BANNER_HORIZONTAL_LAYOUT = "weeklyTrendingMediumItem";
    public static final String LAYOUT_TYPE_TEXT_FLOW_LAYOUT = "TextFlowLayout";
    public static final String LAYOUT_TYPE_SEARCH_LANGUAGES = "LanguageLayout";
    public static final String LAYOUT_TYPE_BIG_BANNER_VERTICAL_LAYOUT_DOUBLE_TITLE = "bigbannerVerticalLayoutDoubleTitle";

    public static final String LAYOUT_TYPE_PILLAR_LAYOUT = "castandCrewPillarLayout";
    public static final String LAYOUT_TYPE_ROLE_NAME_LAYOUT = "roleNameLayout";

    public static String LICENSE_URL = "/licenseproxy/v3/modularLicense/";
    public static final String ANDROID_BITRATE_CAP = "androidBitrateCap";
    public static final String BUFFER_CONFIG_ANDROID = "{\"live\":{\"max\":30,\"min\":15},\"vod\":{\"max\":30,\"min\":15}}";

    public static final String DOWNLOAD_BITRATE_CONFIG = "download_bitrate_config";

    public static final String TYPE_FREE = "avod";

    public static String SCHEME = "https://";
    public static String BASE_URL = "";
    public static String TENANT_ID = "32fdcbc2-8e4f-4d8e-a391-b59f4d9b070c";
    public static String DEVICE_REG_SALT1 = "gJN2";
    public static String DEVICE_REG_SALT2 = "yszNKKxY";
    public static String DEVICE_REG_SALT3 = "Nz0C";
    public static String FAQ_URL = "/debug/faq";
    public static String TNC_URL = "?page=tnc";
    public static String PRIVACY_POLICY_URL = "?page=privacy";
    public static String HELP_URL = "/debug/help";
    public static String SUPPORT_URL = "/debug/support";
    public static String TYPE_TRAILER = "trailer";

    public static String msisdnPath;
    public static String locationPath;
    public static String menuListPath;
    public static String versionDataPath;

    public static final String VIDEOQUALTYLOW = "Low";
    public static final String VIDEOQUALTYMEDIUM = "Medium";
    public static final String VIDEOQUALTYHIGH = "High";
    public static final String VIDEOQUALTYVERYHIGH = "VeryHigh";
    public static final String VIDEOQUALTYSD = "sd";
    public static final String VIDEOQUALTYHD = "hd";
    public static final String VODVIDEOQUALTYSD = "VODsd";
    public static final String MYPLEX = "myplex";


    public static final String STREAMINGFORMATHTTP = "http";
    public static final String STREAMINGFORMATRTSP = "rtsp";
    public static final String STREAMINGFORMATHLS = "hls";

    public static final String STREAMADAPTIVE = "adaptive";
    public static final String STREAMNORMAL = "streaming";
    public static final String STREAMDOWNLOAD = "download";

    public static final String STREAMADAPTIVEDVR = "adaptivedvr";
    public static final String STREAMDASH = "dash";

    public static final String OFFLINE_DOWNLOAD = "offline_download";

    public static final String TYPE_LIVE = "live";
    public static final String TYPE_YOUTUBE = "youtube";
    public static final String TYPE_VODCHANNEL = "vodchannel";
    public static final String TYPE_VODCATEGORY = "vodcategory";
    public static final String TYPE_VODYOUTUBECHANNEL = "vodyoutubechannel";
    public static final String TYPE_VOD = "vod";
    public static final String TYPE_NEWS = "news";
    public static final String TYPE_MOVIE = "movie";
    public static final String TYPE_PROGRAM = "program";
    public static final String TYPE_SONYLIV = "SONYLIV";
    public static final String TYPE_MUSIC = "music";
    public static final String TYPE_PAGE_TV_SHOWS = "tv shows";
    public static final String TYPE_PAGE_VIDEOS = "videos";
    public static final String TYPE_PAGE_MUSIC_VIDEOS = "music videos";
    public static final String TYPE_TVSEASON = "tvseason";
    public static final String TYPE_TVEPISODE = "tvepisode";
    public static final String TYPE_DITTO = "ditto";
    public static final String TYPE_TVSERIES = "tvseries";
    public static final String TYPE_MUSIC_VIDEO = "musicvideo";
    public static final String TYPE_SPORTS = "sports";
    public static boolean IS_PAUSE_MINICARD = false;
    public static boolean IS_SesonUIVisible = false;
    public static boolean IS_SeasonUIForBack = false;
    public static boolean IS_Subcriped = false;
    public static boolean IS_LOCK = false;
    public static boolean IS_REFRESH_LIVETV = false;
    public static boolean IS_REFRESH_LIVETV1 = true;
    public static boolean IS_SHOWING_SUBSCRIPTION_POPUP = false;
    public static boolean IS_BACK_FROM_FRAGMENT_VOD_LIST = true;
    public static boolean IS_FROM_RECOMMENDATIONS = false;

    //    musicvideo
    public static final String TYPE_CAROUSEL_TVSHOW = "tvshow";
    public static final String SLASH = "/";
    public static final String QUESTION_MARK = "?";
    public static final String AMPERSAND = "&";
    public static final String EQUAL = "=";
    public static final String UNDERSCORE = "_";

    //Notification params
    public static final String NOTIFICATION_PARAM_CONTENT_TYPE = "content_type";
    public static final String NOTIFICATION_PARAM_CONTENT_ID = "_id";
    public static final String NOTIFICATION_PARAM_TITLE = "title";
    public static final String NOTIFICATION_PARAM_ACTION = "action";
    public static final String NOTIFICATION_PARAM_AUTOPLAY = "autoplay";
    public static final String NOTIFICATION_PARAM_CHANNEL = "channelId";
    public static final String NOTIFICATION_PARAM_MESSAGE = "message";
    public static final String NOTIFICATION_PARAM_VURL = "vurl";
    public static final String NOTIFICATION_PARAM_YUID = "yuid";
    public static final String NOTIFICATION_PARAM_VIDEO_URL = "videoUrl";
    public static final String NOTIFICATION_PARAM_AID = "_aid";
    public static final String NOTIFICATION_PARAM_NOTIFICATION_ID = "notificationId";
    public static final String NOTIFICATION_PARAM_PAGE = "page";
    public static final String NOTIFICATION_PARAM_PARTNER_ID = "partnerId";
    public static final String NOTIFICATION_PARAM_PARTNER_NAME = "partnerName";
    public static final String NOTIFICATION_PARAM_IMAGE_URL = "imageUrl";
    public static final String NOTIFICATION_PARAM_URL = "url";
    public static final String NOTIFICATION_PARAM_DOWNLOAD = "Downloads";
    public static final String NOTIFICATION_PARAM_TIME = "time";
    public static final String AFFILIATE_VALUE = "affiliate_value";


    public static final String SUBSCRIBE_TAG = "subscribe";
    public static final String USER_CONTEXT = "user/v2";
    public static final String BILLING_EVENT = "billing";
    public static final String FIELD_COMMENTS = "comments";
    public static final String COMMENT = "comment";
    public static final String RATING = "rating";
    public static final String PARTNER = "partner";

    public static final String AIRTEL_MSISDN_RETRIEVER_URL = "http://10.2.216.230:8080/SamsungBillingHub/MsisdnRetriever";
    public static final String IDEA_MSISDN_RETRIEVER_URL = "http://115.112.238.41/SamsungBillingHub/MsisdnRetriever";

    // public static final String LENS_SDK_END_POINT_URL = "https://scribe-news.mmonline.io/t";
    public static final String LENS_SDK_END_POINT_URL = "https://lens-stag-max-scribe.hifx.in/t";
    //public static final String LENS_SDK_SHEME_NAME = "mmnews/v10";
    public static final String LENS_SDK_SHEME_NAME = "max/v1";


    public static final String DEVICE_OEM = "deviceOem";
    public static final String DEVICE_MODEL = "deviceModel";
    private static final String GET_REFERAL = "getReferal";

    public static final String VIDEO_QUALITY_LOW = "low";
    public static final String VIDEO_QUALITY_MEDIUM = "medium";
    public static final String VIDEO_QUALITY_HD = "hd";
    public static final String VIDEO_QUALITY_AUTO = "auto";

    public static final int DOWNLOAD_REFRESH_DELAY = 5 * 1000; // 5s

    // Subscritption status
    public static final int SUBSCRIPTIONERROR = 1;
    public static final int SUBSCRIPTIONSUCCESS = 2;
    public static final int SUBSCRIPTIONREQUEST = 3;
    public static final int LANGUAGESREQUESTCODE = 4;
    public static final int SUBSCRIPTIONINPROGRESS = 5;
    public static final int SUBSCRIPTIONCANCELLED = 6;
    public static final int ONSUBSCRIPTIONDONE = 9;
    public static final int SUBSCRIPTIONSMSCONFIRMATION = 7;
    public static final int SUBSCRIPTIONSMSFAILED = 8;

    public static final int FAVOURITES_FETCH_REQUEST = 1;
    public static final int FAVOURITES_CHANNEL_FETCH_REQUEST = 3;
    public static final int WATCHLIST_FETCH_REQUEST = 2;
    public static final int NOTIFICATION_REQUEST = 10;

    public static final int COUNT_COMMENTS_INT = 20;
    public static double priceTobecharged = 0.0;
    public static final String COVERPOSTER = "coverposter";
    public static String downLoadStartTime = "download_start_time";
    public static final String IMAGE_TYPE_PORTRAIT_COVERPOSTER = "portraitcoverposter";
    public static final String IMAGE_TYPE_PORTRAIT_BANNER = "portraitbanner";
    public static final String IMAGE_TYPE_SQUARE_BANNER = "squarebanner";
    public static final String IMAGE_TYPE_COVERPOSTER = "coverposter";
    public static final String IMAGE_TYPE_NEWS_NESTED_IMAGE = "newsNestedLayout";
    public static final String IMAGE_TYPE_BANNER = "banner";
    public static final String IMAGE_TYPE_NO_IMAGE = "Images/NoImage.jpg";
    public static final String IMAGE_TYPE_ICON = "icon";
    public static final String IMAGE_TYPE_THUMBNAIL = "thumbnail";
    public static final String IMAGE_TYPE_THUMBNAIL_BANNER = "ThumbnailBanner";
    public static final String IMAGE_TYPE_PREVIEW = "preview";

    public static final String GIF_IMAGE_TYPE_PORTRAIT_COVERPOSTER = "GIFportraitcoverposter";
    public static final String GIF_IMAGE_TYPE_PORTRAIT_BANNER = "GIFportraitBanner";
    public static final String GIF_IMAGE_TYPE_SQUARE_BANNER = "GIFsquareBanner";
    public static final String GIF_IMAGE_TYPE_COVERPOSTER = "GIFcoverposter";
    public static final String GIF_IMAGE_TYPE_BANNER = "GIFbanner";


    public static String PARAM_CONTENT_DETAILS_ALL_FIELDS = "user/currentdata,images,generalInfo,stats" +
            "contents,comments,reviews/user,_id,relatedMedia,packages,relatedCast," +
            "dynamicMeta,_lastModifiedAt,_expiresAt,matchInfo,globalServiceId,publishingHouse";

    public static String PARAM_CONTENT_DETAILS_CURRENT_USERDATA_FIELDS = "user/currentdata,packages,publishingHouse,generalInfo,reviews/user";

    public static String MESSAGE_ERROR_CONN_RESET = "recvfrom failed: ECONNRESET";

    public static String PARAM_CONTENT_DETAILS_GENERALINFO_FIELDS = "generalInfo";


    public static String profileAPIListAndroid;

    public static String getProfileAPIListAndroid(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "profileAPIListAndroid.bin";
    }

    public static final String LAYOUT_TYPE_MOVIES_SEARCH = "Movies";
    public static final String LAYOUT_TYPE_TV_SHOWS_SEARCH = "TV Shows";
    public static final String LAYOUT_TYPE_LIVE_TV_SEARCH = "Live TV";
    public static final String LAYOUT_TYPE_VIDEOS_SEARCH = "Music";
    public static final String LAYOUT_TYPE_PERSON_SEARCH = "Actors";
    public static final String TYPE_MUSIC_VIDEOS = "Music Videos";
    public static final String TYPE_COMEDY_CLIPS = "Comedy Clips";
    public static final String LAYOUT_TYPE_WEEKLY_TRENDING_SMALL_ITEM = "weeklyTrendingSmallItem";
    public static final String NAME_OF_ARTIST = "name";
    public static final String COMING_FROM_ARTIST_PROFILE = "comingFromArtistProfile";
    public static final String TYPE_OF_CONTENT = "typeOfContent";
    public static final String TYPE_ACTOR_ROFILE_VIEW_ALL_DATA = "actorProfileViewAllData";
    public static final String PUBLISHING_ID = "housingPublishId";

    // menu types
    //public static final String MENU_TYPE_GROUP_ANDROID_NAV_MENU = "androidNavMenu";
    public static final String MENU_TYPE_GROUP_ANDROID_NAV_MENU = "navMenuSmartPh";
    public static final String MENU_TYPE_GROUP_ANDROID_TVSHOW = "androidTvShows";
    public static final String MENU_TYPE_GROUP_ANDROID_VIDEOS = "androidVideos";
    public static final String MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS = "MusicVideos";
    public static final String MENU_TYPE_GROUP_ANDROID_KIDS = "Kids";
    public static final String MENU_TYPE_GROUP_ANDROID_KIDS_MENU = "KidsMenu";

    //layout types
    public static final String LAYOUT_TYPE_MENU = "menu";
    public static final String LAYOUT_TYPE_NAV_MENU = "navMenuSmartPh5x";
    public static final String LAYOUT_TYPE_EPG = "epg";
    public static final String LAYOUT_TYPE_BANNER = "banner";
    public static final String LAYOUT_TYPE_WEBPAGE = "webPage";
    public static final String LAYOUT_TYPE_VMAX_IMAGE_ADS = "vMaxAdImageLayout";
    public static final String LAYOUT_TYPE_VMAX_VIDEO_ADS = "vMaxAdVideoLayout";
    public static final String LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM = "horizontalListBigItem";
    public static final String LAYOUT_TYPE_HORIZONTAL_LIST_MEDIUM_ITEM = "horizontalListMediumItem";
    public static final String LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM = "horizontalListSmallItem";
    public static final String LAYOUT_TYPE_HORIZONTAL_LIST_GENERIC_ITEM = "horizontalListGenericItem";
    public static final String LAYOUT_TYPE_HORIZONTAL_LIST_LIVE_PROGRAM_ITEM = "liveProgramItem";
    public static final String LAYOUT_TYPE_SQUARE_LIST_SMALL_TIEM = "squareListSmallItem";
    public static final String LAYOUT_TYPE_SINGLE_BANNER_ITEM = "singleBannerItem";
    public static final String LAYOUT_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM = "PromobannerV2";
    public static final String LAYOUT_TYPE_HORIZONTAL_LIST_LARGE_ITEM = "horizontalListLargeItem";
    public static final String APP_ACTION_LIVE_PROGRAM_LIST = "liveProgramList";
    public static final String TYPE_SHOW_ALL_VIEW_ALL = "View All";
    public static final String LAYOUT_TYPE_BROWSE_GRID = "browseGrid";
    public static final String LAYOUT_TYPE_BROWSE_LIST = "browseList";
    public static final String LAYOUT_TYPE_BIG_BROWSE_LIST = "browseListBig";
    public static final String LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID = "browseSmallSquareGrid";
    public static final String LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER = "browseListBigWithoutFilter";
    public static final String LAYOUT_TYPE_HUNGAMA_SDK_VIEW_ALL = "launchHungamaViewAllSDK";
    public static final String LAYOUT_TYPE_HUNGAMA_SDK_VIEW_ALL_HOME = "launchHungamaSDKHome";
    public static final String LAYOUT_TYPE_CONTINUE_WATCHING = "continueWatching";
    public static final String LAYOUT_TYPE_CONTINUE_WATCHING_EPG = "continueWatchingEPG";
    public static final String LAYOUT_TYPE_PROMO_BANNER = "promoBanner";
    public static final String LAYOUT_TYPE_EMPTY_FOOTER = "emptyFooter";
    public static final String LAYOUT_TYPE_ARTIST_BANNER_DESCRIPTION = "artist_banner_description";
    public static final String LAYOUT_TYPE_EXCLUSIVE_HORIZONTA_LIST_BIG_ITEM = "exclusiveHorizontalListBigItem";
    public static final String LAYOUT_TYPE_BROWSE_CATEGORY_SCREEN = "browseCategoryScreen";
    public static final String LAYOUT_TYPE_RESENT_SEARCH = "recentSearch";
    public static final String LAYOUT_TYPE_PREVIEW_CAROUSAL = "previewCarousel";
    public static final String LAYOUT_TYPE_AUTOPLAY_SINGLE_BANNER = "autoPlaySingleBanner";
    public static final String LAYOUT_TYPE_ROUNDED_ARTIST_CAROUSEL = "roundedArtistCarousel";
    public static final String LAYOUT_TYPE_3D_CAROUSEL = "horizontal3DListBigItem";
    public static final String LAYOUT_TYPE_BIG_HORIZONTAL_3D_CAROUSEL = "weeklyTrending3DListBigItem";
    public static final String LAYOUT_TYPE_NAVIGATION_ITEM = "navigationItem";
    public static final String LAYOUT_TYPE_NAVIGATION_SEPERATOR = "sideNavMenuSeparator";
    public static final String LAYOUT_TYPE_NEWS_NESTED_CAORUSEL = "newsNestedLayout";
    public static final String LAYOUT_TYPE_CATEGORY = "category";
    public static final String LAYOUT_TYPE_PARTNERS = "Partner";
    public static final String LAYOUT_TYPE_LIVE_SMALL_ITEM = "Livechannelssmall";
    public static final String LAYOUT_TYPE_EXTRA_SMALL_COVER_P = "ExtraSmallCoverposter";
    public static final String LAYOUT_TYPE_LONG_PORTRAIT_MEDIUM = "LongPortraitMedium";
    public static final String LAYOUT_TYPE_NEXT_PROGRAM = "NextProgram";
    public static final String LAYOUT_TYPE_CATCHUP = "catchup";
    public static final String LAYOUT_TYPE_RECOMMEDED_PROGRAM = "recommendedProgramsReco";
    public static final String LAYOUT_TYPE_WEEKLY_TRENDING_SEASON_ITEM = "Weeklytrendingseasonsitem";

    public static final String CONTENT_RIGHTS_TVODPREMIUM = "tvod-premium";
    public static final String CONTENT_RIGHTS_TVOD = "tvod";
    public static final String CONTENT_RIGHTS_SVOD = "svod";

    public static final boolean IS_ENABLE_USER_SEGMENT_BADGE = true;

    public static final String ERROR_CALLBACK_LISTENERS_NOT_REGD = "Callback listener's are not registered";
    public static final String ERROR_RESPONSE_OR_RESPONSE_BODY_NULL = "server response or response body is null";
    public static final String ERROR_EPMTY_RESULTS = "empty results";

    //hooq constants
    public static final String HOOQ_PLAYER_ID_EXOPLAYER = "EXOPLAYER";
    public static final String HOOQ_DRMAGENT_ID_EXOPLAYER = "EXOPLAYER";
    public static final String HOOQ_DRM_DESCRIPTION_WIDEWINE_VER_SIX = "WIDEVINE (6):2.0";
    public static String downloadImagesStoragePath = "/.myplex/images/";

    //offers api constants
    public static final String APP_LAUNCH_HOME = "launchHomePage";
    public static final String APP_LAUNCH_WEB = "htmlOfferPage";
    public static final String APP_LAUNCH_NATIVE_SUBSCRIPTION = "nativeSubPage";
    public static final String APP_LAUNCH_NATIVE_OFFER = "nativeOfferPage";
    public static final String OFFER_ACTION_PAGE_NAVIGATION = "pageNavigation";
    public static final String OFFER_ACTION_MOBILE_DATA_ALERT = "mobileDataAlert";
    public static final String OFFER_ACTION_SHOW_MESSAGE = "showMessage";
    public static final String OFFER_ACTION_SHOW_TOAST = "showToast";
    public static final String OFFER_ACTION_PROMOTINAL = "promotional";
    public static String PARAM_APPLAUNCH = "appLaunch";
    public static String PARAM_CONTENT_DETAIL = "contentDetail";
    public static String EVENT_FAILED_TO_FETCH_MEDIA_URL = "Failed to fetch media url";
    public static String EVENT_PLAYBACK_FAILED = "Playback failed";
    public static String DEEPLINK_URL = "deepLinkUrl";
    public static String promoAdDataPath;
    public static String previewPropertiesPath;
    public static String partnerDetailsDataPath;
    public static String partnerDetailsAppinAppDataPath;
    public static String setupBoxListPath;
    public static String categoryScreenFiltersV2;
    public static String GENRES = "genres";
    public static String ratingPopUpConfig;
    public static final String RATED = "rated";
    public static final String IGNORED = "ignored";

    public static final String SOURCE_HOME = "Source";
    public static final String VALUE_SOURCE_IN_APP = "In App";

    //For remoteConfig Settings and paramKeys
    public static final String SHOULD_ENABLE_REMOTE_CONFIG_API = "enableRemoteConfigAndroid";
    public static final String SHOULD_ENABLE_MEDIA_DOMAIN_API = "enabledSubDomainAPI";
    public static final String ENABLE_PREF_LANG_SCREEN = "enablePrefLangScreen";
    public static final String DOMAIN_PLAYER_EVENTS = "domain_playerevents";
    public static final String DOMAIN_META = "domain_meta";
    public static final String DOMAIN_MEDIA = "domain_media";

    public static final String UPDATE_PORTRAIT_BANNER = "update portrait banner";

    public static final String SIGN_IN_FLOW_ON_PLAY = "onPlay";
    public static final String SIGN_IN_FLOW_ON_APP_OPEN = "onAppOpen";


    public static String MEDIA_DOMAIN = "media.sundirect.in";
    public static String MEDIA_SCHEME = "http";
    public static String FORCE_ACCEPT_LANGUAGE = "forceAcceptLanguage";
    public static final String ERROR_CONTENT_TYPE = "content_type";
    public static final String PREVIEWS_DEVICE_HEIGHT = "previews_device_height";
    public static String LOGIN_SCHEME = "http";
    //BroadCast Constants
    public static String PAUSE_BROADCAST = "pause_broadcast";
    public static String RESUME_BROADCAST = "resume_broadcast";
    public static String PAGE_CHANGE_BROADCAST = "page_change_broadcast";
    public static String MINI_PLAYER_ENABLED_BROADCAST = "mini_player_enabled_broadcast";
    public static String MINI_PLAYER_DISABLED_BROADCAST = "mini_player_disabled_broadcast";
    public static String IS_CONTINUE_WATCHING_SHORT_CUT_ADDED = "is_continue_watching_short_cut_added";
    public static String SHARE_ARTIST_DEEP_LINK_URL = "https://www.sundirect.com/";
    public static String PWA_URL = "https://www.sundirect.com";
    public static String LEGACY_UPGRADE_POPUP = "legacy_upgrade_popup";


    //Google Admob constants
    public static final String LAYOUT_TYPE_MEDIUM_AD = "AdMediumLayout";
    public static final String LAYOUT_TYPE_SMALL_AD = "AdSmallLayout";
    public static final String LAYOUT_TYPE_NATIVE_IMAGE_AD = "AdNativeImage";
    public static final String LAYOUT_TYPE_NATIVE_VIDEO_AD = "AdNativeVideo";
    public static final String LAYOUT_TYPE_CUSTOM_AD_BANNER = "customAdBanner";
    public static final String LAYOUT_TYPE_BANNER_STRIP_BELOW_COVER_POSTER = "BannerStripBelowCoverPoster";
    public static final String LAYOUT_TYPE_AD_BANNER_BTW_CAROUSELS_SMALL = "AdBannerbtwrailsSmall";
    public static final String LAYOUT_TYPE_AD_BANNER_BTW_CAROUSELS_MEDIUM = "AdBannerbtwrailsMedium";
    public static final String LAYOUT_TYPE_AD_BANNER_BTW_CAROUSELS_LARGE = "AdBannerbtwrailsLarge";

    public static final String SHOULD_ENABLE_GOOGLE_ADS = "isAdEnabled";

    public static boolean IS_PLAYER_SCREEN_LOCKED = false;
    public static boolean IS_LOCKED = false;
    public static final String ARE_SUBTITLES_ENABLED = "are_subtitles_enabled";
    public static final String IS_EDIT_MOBILE_NUMBER_ENABLED = "edit_mobile_number_enabled";
    public static final String IS_ENABLED_VIDEO_ANALYTICS = "enable_video_analytics";

    //Apexott
    public static String SHARE_ARTIST_DEEP_LINK_URL_BCN = "https://www.devpwapaas.myplex.com/";
    public static String SEEKENABLED = "Seekenble";
    public static String UPDATESSTATUSINTERVALINSEC = "updateStatusIntervalInSec";
    public static String MEDIASESSIONTOKEN = "mediaSessionToken";
    public static String APPActionRedirect = "app_action_redirect";
    public static String SHOWWATERMARKENABLED = "showwatermarkenable";

    public static final String BANNER_PREVIEW_PLAYBACK_ENABLED = "bannerPreviewPlaybackEnabled";
    public static final String THUMBNAIL_PREVIEW_PLAYBACK_ENABLED = "thumbnailPreviewPlaybackEnabled";

    public static final String CAROUSEL_AUTOPLAY = "Carousel";
    public static final String SOURCE_DETAIL_AUTOPLAY = "AutoPlay Banner";

    public static final String BLOCK_AUTOPLAY_CP = "blockAutoplayContentPartners";

    public static final String CONTENT_TYPE_PREVIEW = "preview";

    //RemoteConfig
    public static final String TEST_PARAM = "test_param";

    public static String getRatingScreen(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "ratingScreen.bin";
    }

    /**
     * It forms the subscription request url to subscribe the user
     *
     * @param paymentChannel type of the payment channel
     * @param packageId      it represent packageId
     * @param contentId      it represent contentId of channelId/ProgramId
     * @return subscription request url
     */
    public static String getSusbcriptionRequest(String paymentChannel, String packageId, String contentId) {
        return SCHEME + BASE_URL + SLASH +
                USER_CONTEXT + SLASH + BILLING_EVENT + SLASH + SUBSCRIBE_TAG + SLASH +
                QUESTION_MARK + "clientKey=" + PrefUtils.getInstance().getPrefClientkey()
                + AMPERSAND + "paymentChannel=" + paymentChannel + AMPERSAND + "packageId=" +
                packageId + AMPERSAND + "contentId=" + contentId;
    }

    /**
     * It Forms the deviceParams like Device Model,Manufacturer details
     *
     * @return device related parameters
     */
    public static String getDRMDeviceParams() {
        String params = DEVICE_MODEL + EQUAL + android.os.Build.MODEL + AMPERSAND + DEVICE_OEM + EQUAL + android.os.Build.MANUFACTURER;
        return params.replace(" ", "%20");
    }

    /**
     * It forms the url to download OTTApp
     *
     * @param appName it represents the name of the OTTApp
     * @return OTTApp url
     */
    public static String getOTTAppDownloadUrl(String appName) {
        return SCHEME + BASE_URL + SLASH +
                "ott/v1/confirmDownload" +
                QUESTION_MARK + "clientKey=" + PrefUtils.getInstance().getPrefClientkey() + AMPERSAND + "appName=" + appName;
    }

    /**
     * It forms the FAQ Url to load FAQ Content in WebView
     *
     * @return FAQ Url
     */
    public static String getFAQURL() {
        return APIConstants.SCHEME + APIConstants.BASE_URL + APIConstants.FAQ_URL;
    }

    /**
     * It forms the helpUrl to load Help related content in webView
     *
     * @return helpUrl
     */
    public static String getHelpURL() {
        return APIConstants.SCHEME + APIConstants.BASE_URL + APIConstants.HELP_URL;
    }

    public static String getSupportURL() {
        return APIConstants.SCHEME + APIConstants.BASE_URL + APIConstants.SUPPORT_URL;
    }

    public static String getPartnerSusbcriptionUrl(String partnerName,
                                                   String partnerContentId,
                                                   String packType,
                                                   String packPrice,
                                                   String contentName,
                                                   String imageUrl) {
        ///user/v2/billing/partner/subscribe/
        return SCHEME + BASE_URL + SLASH +
                USER_CONTEXT + SLASH + BILLING_EVENT + SLASH + PARTNER + SLASH + SUBSCRIBE_TAG + SLASH +
                QUESTION_MARK + "clientKey=" + PrefUtils.getInstance().getPrefClientkey()
                + AMPERSAND + "partnerName=" + partnerName + AMPERSAND + "partnerContentId=" +
                partnerContentId + AMPERSAND + "packType=" + packType + AMPERSAND + "packPrice=" + packPrice + AMPERSAND + "contentTitle=" +
                contentName + AMPERSAND + "imageUrl=" + imageUrl;
    }

    public static String getDittoChannelLogoUrl() {
        return PrefUtils.getInstance().getPrefDittoChannelLogoUrlOnEpg();
    }

    public static String getErosNowMusicLogoUrl() {

        if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefErosNowMusicLogoUrlOnEpg())) {
            return "http://stagingimage.s3.amazonaws.com/xhdpi_erosnow_logo.png?scale=wrap";
        }
        return PrefUtils.getInstance().getPrefErosNowMusicLogoUrlOnEpg();
    }

    public static String getYupTVLogoUrl() {

        if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefYupTVChannelLogoImageUrl())) {
            return "http://myplexv2stagingimages.s3.amazonaws.com/xhdpi_Yupp_TV_logo.png?scale=wrap";
        }
        return PrefUtils.getInstance().getPrefYupTVChannelLogoImageUrl();
    }

    public static String getShareUrl(Context mContext) {

        return PrefUtils.getInstance().getPrefShareUrl();
    }

    public static String getSonyChannelLogoUrl() {
        if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefSonyChannelLogoUrlOnEpg())) {
            return "https://stagingimage.s3.amazonaws.com/xhdpi_sony_liv_icon.png";
        }
        return PrefUtils.getInstance().getPrefSonyChannelLogoUrlOnEpg();
    }

    public static String getHooqUserId() {
        String hooqUserId = "";
        if (PrefUtils.getInstance().getPrefUserId() > 0) {
            hooqUserId = "VFPlay-" + PrefUtils.getInstance().getPrefUserId();
        }
        return hooqUserId;
    }

    public static String getDRMLicenseUrl(String id, String type) {
        return APIConstants.SCHEME_HTTPS + APIConstants.BASE_URL + "/licenseproxy/v2/modularLicense?content_id=" + id
                + "" + "&licenseType=" + type
                + "" + "&clientKey=" + PrefUtils.getInstance().getPrefClientkey()
                + "" + "&timestamp=" + System.currentTimeMillis();
    }


    public static String getDRMLicenseUrl(String id, String type, String variantType) {
        return APIConstants.SCHEME_HTTPS + APIConstants.BASE_URL + "/licenseproxy/v2/modularLicense?content_id=" + id
                + "" + "&licenseType=" + type
                + "" + "&variantType=" + variantType
                + "" + "&clientKey=" + PrefUtils.getInstance().getPrefClientkey()
                + "" + "&timestamp=" + System.currentTimeMillis();
    }

    public static boolean isHooqContent(CardData data) {
        return data != null
                && data.publishingHouse != null
                && data.publishingHouse.publishingHouseName != null
                && TYPE_HOOQ.equalsIgnoreCase(data.publishingHouse.publishingHouseName);
    }

    public static boolean isAltBalajiContent(CardData data) {
        return data != null
                && data.publishingHouse != null
                && data.publishingHouse.publishingHouseName != null
                && TYPE_ALT_BALAJI.equalsIgnoreCase(data.publishingHouse.publishingHouseName);
    }


    public static boolean isHungamaContent(CardData data) {
        return data != null
                && data.publishingHouse != null
                && data.publishingHouse.publishingHouseName != null
                && TYPE_HUNGAMA.equalsIgnoreCase(data.publishingHouse.publishingHouseName);
    }

    public static boolean isZee5Content(CardData data) {
        return data != null
                && data.publishingHouse != null
                && data.publishingHouse.publishingHouseName != null
                && TYPE_ZEE5.equalsIgnoreCase(data.publishingHouse.publishingHouseName);
    }

    public static boolean isMovie(CardData data) {
        return data != null
                && data.generalInfo != null
                && data.generalInfo.type != null
                && TYPE_MOVIE.equalsIgnoreCase(data.generalInfo.type);
    }

    public static boolean isVideo(CardData data) {
        return data != null
                && data.generalInfo != null
                && data.generalInfo.type != null
                && TYPE_VOD.equalsIgnoreCase(data.generalInfo.type);
    }

    public static boolean isEpisode(CardData data) {
        return data != null
                && data.generalInfo != null
                && data.generalInfo.type != null
                && TYPE_TVEPISODE.equalsIgnoreCase(data.generalInfo.type);
    }

    public static String getShareUrlForMenu(Context mContext) {
        return PrefUtils.getInstance().getPrefShareUrlForMenu();
    }

    public static boolean isErosNowContent(CardData data) {
        return data != null
                && data.publishingHouse != null
                && data.publishingHouse.publishingHouseName != null
                && TYPE_EROS_NOW.equalsIgnoreCase(data.publishingHouse.publishingHouseName);
    }

    public static boolean isErosNowContent(CardDownloadData downloadData) {
        return downloadData != null
                && downloadData.partnerName != null
                && TYPE_EROS_NOW.equalsIgnoreCase(downloadData.partnerName);
    }

    public static boolean isHungamaContent(CardDownloadData cardDownloadData) {
        return cardDownloadData != null
                && cardDownloadData.partnerName != null
                && TYPE_HUNGAMA.equalsIgnoreCase(cardDownloadData.partnerName);
    }

    public static String getPromoAdDataPath(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "promoAdData.bin";
    }

    public static String getPreviewPropertiesPath(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "previewProperties.bin";
    }

    public static String getVersionDataPath(Context mContext) {
        return mContext.getFilesDir() + "/" + "versionData.bin";
    }

    public static String getPartnerDetailsDataPath(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "partnerDetailsV23.bin";
    }

    public static String getPartnerDetailsDataAppinAppPath(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "partnerDetailsV51.bin";
    }

    public static String getSetupBoxListPath(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "setupBoxList.bin";
    }

    public static String getSearchConfigDataPath(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "searchConfigData.bin";
    }

    public static String getAdFullScreenConfigPath(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "adFullScreenConfig.bin";
    }

    public static String getAdPopupNotification(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "adPopUpNotification.bin";
    }


    public static String getSearchConfigAndroidDataPath(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "searchConfigAndroid.bin";
    }

    public static String getcategoryScreenFilters(Context mContext) {
        if (mContext == null) return null;
        return mContext.getFilesDir() + "/" + "categoryScreenFiltersV22.bin";
    }

    public static String splitPart1() {
        String subPart = PrefUtils.getInstance().getPart1();
        return subPart.substring(0, 4);
    }

    public static String splitPart4() {
        String subPart = APIEncryption.PART4;
        return subPart.substring(4, 8);
    }

    public static String getCarouselAPIUrl(Context context, String title, String fields, int count, int startIndex, String mcc, String mnc, String serverModifiedTime, String globalServiceId) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
//        //Log.d(TAG,"clientKey=" + clientKey);
        List<PreferredLanguageItem> items = PrefUtils.getInstance().getPreferredLanguageItems();
        String preferredLanguages = "";
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                preferredLanguages = (i != items.size() - 1) ? preferredLanguages + items.get(i).getTerm() + "," : preferredLanguages + items.get(i).getTerm();
            }
        }
        String url = null;
        if (myplexAPISDK.getmRemoteConfig() != null) {
            url = myplexAPISDK.getmRemoteConfig().getString(APIConstants.META_SUB_DOMAIN);
        } else {
            url = APIConstants.SCHEME + APIConstants.BASE_URL;
        }
        String appLanguage = PrefUtils.getInstance().getAppLanguageToSendServerInStringFormat();
        List<String> subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
        String packLanguage = "";
        if(subscribed_languages != null && subscribed_languages.size() > 0 && subscribed_languages.get(0) != null)
            packLanguage = subscribed_languages.get(0);
        if (url.isEmpty()) {
            if (preferredLanguages != null && !preferredLanguages.isEmpty()) {
                if (globalServiceId != null)
                    return APIConstants.SCHEME + APIConstants.BASE_URL + "/content/v2/carousel/" + title +
                            "?fields=" + fields + "&count=" + count + "&startIndex=" + startIndex + "&contentLanguage=" + preferredLanguages + "&mnc=" + mnc + "&mcc=" + mcc + "&serverPublishedTime=" + serverModifiedTime + "&appLanguage=" + appLanguage +"&packlanguage="+packLanguage+ "&contentId=" + globalServiceId;
                else
                    return APIConstants.SCHEME + APIConstants.BASE_URL + "/content/v2/carousel/" + title +
                            "?fields=" + fields + "&count=" + count + "&startIndex=" + startIndex + "&contentLanguage=" + preferredLanguages + "&mnc=" + mnc + "&mcc=" + mcc + "&serverPublishedTime=" + serverModifiedTime + "&appLanguage=" + appLanguage+"&packlanguage="+packLanguage;
            } else {
                if (globalServiceId != null)
                    return APIConstants.SCHEME + APIConstants.BASE_URL + "/content/v2/carousel/" + title +
                            "?fields=" + fields + "&count=" + count + "&startIndex=" + startIndex + "&mnc=" + mnc + "&mcc=" + mcc + "&serverPublishedTime=" + serverModifiedTime + "&appLanguage=" + appLanguage +"&packlanguage="+packLanguage+ "&contentId=" + globalServiceId;
                else
                    return APIConstants.SCHEME + APIConstants.BASE_URL + "/content/v2/carousel/" + title +
                            "?fields=" + fields + "&count=" + count + "&startIndex=" + startIndex + "&mnc=" + mnc + "&mcc=" + mcc + "&serverPublishedTime=" + serverModifiedTime + "&appLanguage=" + appLanguage+"&packlanguage="+packLanguage;
            }

        } else {
            if (preferredLanguages == null || preferredLanguages.isEmpty()) {
                if (globalServiceId != null)
                    return myplexAPISDK.getmRemoteConfig().getString(META_SUB_DOMAIN) + "/content/v2/carousel/" + title +
                            "?fields=" + fields + "&count=" + count + "&startIndex=" + startIndex + "&mcc=" + mcc + "&serverPublishedTime=" + serverModifiedTime + "&appLanguage=" + appLanguage +"&packlanguage="+packLanguage+  "&contentId=" + globalServiceId;
                else
                    return myplexAPISDK.getmRemoteConfig().getString(META_SUB_DOMAIN) + "/content/v2/carousel/" + title +
                            "?fields=" + fields + "&count=" + count + "&startIndex=" + startIndex + "&mcc=" + mcc + "&serverPublishedTime=" + serverModifiedTime + "&appLanguage=" + appLanguage+"&packlanguage="+packLanguage;
            } else {
                if (globalServiceId != null)
                    return myplexAPISDK.getmRemoteConfig().getString(META_SUB_DOMAIN) + "/content/v2/carousel/" + title +
                            "?fields=" + fields + "&count=" + count + "&startIndex=" + startIndex + "&contentLanguage=" + preferredLanguages + "&mcc=" + mcc + "&serverPublishedTime=" + serverModifiedTime + "&appLanguage=" + appLanguage +"&packlanguage="+packLanguage+  "&contentId=" + globalServiceId;
                else
                    return myplexAPISDK.getmRemoteConfig().getString(META_SUB_DOMAIN) + "/content/v2/carousel/" + title +
                            "?fields=" + fields + "&count=" + count + "&startIndex=" + startIndex + "&contentLanguage=" + preferredLanguages + "&mcc=" + mcc + "&serverPublishedTime=" + serverModifiedTime + "&appLanguage=" + appLanguage+"&packlanguage="+packLanguage;
            }
        }

    }

    public static boolean isPortraitBannerLayout(CarouselInfoData carouselInfoData) {
        return carouselInfoData != null
                && carouselInfoData.layoutType != null
                && (carouselInfoData.layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_PORTRAIT_BANNER)
                || carouselInfoData.layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_SQUARE_BANNER));
    }


}
