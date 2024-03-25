package com.myplex.util;

import static com.myplex.api.APIConstants.ARE_SUBTITLES_ENABLED;
import static com.myplex.api.APIConstants.IS_EDIT_MOBILE_NUMBER_ENABLED;
import static com.myplex.api.APIConstants.IS_ENABLED_VIDEO_ANALYTICS;
import static com.myplex.api.APIConstants.IS_PORTING_TO_SECURE_RANDOM_DONE;
import static com.myplex.api.APIConstants.SHOULD_ENABLE_GOOGLE_ADS;
import static com.myplex.util.VersionUpdateUtil.DAY_MILLIS;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myplex.api.APIConstants;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CardData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CommonParams;
import com.myplex.model.PartnerDetailItem;
import com.myplex.model.PartnerDetailsResponse;
import com.myplex.model.PreferredLanguageItem;
import com.myplex.sdk.R;

import org.json.JSONObject;
import org.junit.Assert;

import java.lang.reflect.Type;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utilities and constants related to app preferences.
 */
public class PrefUtils {

    private static final String PREF_ENABLE_SONYLIV_AD = "pref_enable_sonylive_ad";
    private static final String PREF_ENABLE_VAST3_AD = "pref_enable_vast3_ad";
    private static final String PREF_ADPROVIDER_TAG_OOYALA = "pref_ad_provider_tag_ooyala";
    private static final String PREF_ADPROVIDER_TAG_VAST3 = "pref_ad_provider_tag_vast3";
    private static final String PREF_ENABLE_MUSIC_TAG = "pref_enable_music_tab";
    private static final String PREF_ALLOW_WIFI_FOR_PAYMENT = "pref_allow_wifi_for_payment";
    private static final String PREF_ENABLE_MANUAL_OTP = "pref_enable_manual_otp";
    private static final String PREF_OTP_DETECTION_TIME_OUT = "pref_otp_detection_time_out";
    private static final String PREF_SHARE_MESSAGE = "pref_share_msg";
    private static final String PREF_EROSNOW_MUSIC_LOGO_URL = "pref_erosnow_music_logo_url";
    private static final String PREF_SHARE_URL = "share_url";
    private static final String PREF_CHROMECAST_RECIEVER_ID = "pref_chrome_cast_reciever_id";
    private static final String PREF_IS_CLEVERTAP_GCM_TOKEN_UPDATED = "pref_gcm_token_updated";
    private static final String PREF_YUPP_TV_LOGO_URL = "pref_yuptv_logo_url";
    private static final String PREF_ENABLE_YUPP_TV_CHANNEL_LOGO_ON_EPG = "pref_enable_yup_tv_logo";
    private static final String PREF_ENABLE_CLEVERTAP_API = "pref_enable_clevertap_api";
    private static final String PREF_DOWNLOAD_ONLY_ON_WIFI = "pref_download_only_on_wifi";
    private static final String PREF_AUTOPLAY = "pref_autoplay";
    private static final String PREF_DARKMODE = "pref_darkmode";
    private static final String PREF_NOTIFICATION = "pref_notification";
    private static final String PREF_HOOQ_SESSION_EXPIRY_TIME = "pref_hooq_session_expiry_time";
    private static final String PREF_HOOQ_SESSION_TOKEN = "pref_hooq_session_token";
    private static final String PREF_HOOQ_HMAC = "pref_hooq_hmac";
    private static final String PREF_SHARE_MESSAGE_FOR_MENU = "pref_share_message_for_menu";
    private static final String PREF_SHARE_URL_FOR_MENU = "pref_share_url_for_menu";
    private static final String PREF_PLAYER_LOGS_ENABLE_TO = "pref_player_logs_enable_to";
    private static final String PREF_ENABLE_EROS_NOW_PLAYER_LOGS = "pref_enable_eros_now_player_logs";
    private static final String PREF_SHOW_PLAYBACK_OPT_HINT = "pref_show_playback_opt_hint";
    private static final String PREF_ENABLE_HOOQ_CHROMECAST = "pref_enable_hooq_chromecast";
    private static final String PREF_ENABLE_APPSFLYER_PLAYED_EVENT_FOR_30_SEC = "appsflyer_played_video_event_for_30_sec";
    private static final String PREF_STANDARD_PACKAGE_IDS = "pref_standard_package_ids";
    private static final String PREF_MESSAGE_DRM_LICENSE_FAILED = "pref_message_drm_license_failure";
    private static final String PREF_MESSAGE_NO_SPACE_AVAILABLE_TO_UNZIP = "pref_message_no_space_to_unzip";
    private static final String PREF_DOWNLOAD_STATE_NO_SPACE_AVAILABLE_TO_UNZIP = "pref_download_state_no_space_available_to_extract";
    private static final String PREF_DOWNLOAD_STATE_FAILED_TO_DOWNLOAD_DUE_TO_LOW_MEM = "pref_download_state_failed_due_to_low_memory";
    private static final String PREF_DOWNLOAD_STATE_DRM_LICENSE_FAILED = "pref_download_state_drm_license_failed";
    private static final String PREF_APPSFLYER_PLAYBACK_EVENT_SECONDS = "pref_appsflyerPlaybackEventSeconds";
    private static final String BANNER_AUTO_SCROLL_FREQUENCY = "bannerScrollFrequency";
    private static final String NEXT_EPISODE_POPUP_PERCENTAGE = "nextEpisodePopupPercentage";
    private static final String PREF_IS_GESTURE_TIPS_SHOWN = "is_gesture_tips_shown";
    private static final String PREF_FFORWRD_SECONDS = "pref_fforward_seconds";
    private static final String PREF_EMAIL_REQUIERED_FOR_PARTNERS = "email_required_for_partners";
    private static final String PREF_AUTO_LOGIN_FAILED_MSG = "auto_login_failed";
    private static final String PREF_LAST_VMAX_INTERSTITIAL_AD_SHOWN_DAY = "last_vmax_interstial_ad_shown_date";
    private static final String PREF_VMAX_BANNER_ADSPOT_ID = "pref_vmax_banner_adspot_id";
    private static final String PREF_VMAX_INTERSTITIAL_AD_ID = "pref_vmax_interstitial_ad_id";
    private static final String PREF_VMAX_INTERSTITIAL_APP_TAB_SWITCH_AD_ID="vmaxIntstlAdTabSwitchAdSpotId";
    private static final String PREF_VMAX_INTERSTITIAL_APP_OPEN_AD_ID="vmaxIntstlAdAppOpenAdSpotId";
    private static final String PREF_VMAX_INTERSTITIAL_APP_EXIT_AD_ID="vmaxIntstlAdAppExitAdSpotId";
    private static final String PREF_VMAX_INSTREAM_AD_ID = "pref_vmax_instream_ad_id";
    private static final String PREF_VMAX_NATIVE_VIDEO_AD_ID = "pref_vmax_native_video_ad_id";
    private static final String PREF_VMAX_NATIVE_DISPLAY_AD_ID = "pref_vmax_native_display_ad_id";
    private static final String PREF_ENABLE_VMAX_INSTREAM_AD = "pref_enable_vmax_instream_ad";
    private static final String PREF_ENABLE_VMAX_INTERSTITIAL_AD = "pref_enable_interstitial_ad";
    private static final String PREF_ENABLE_VMAX_INTERSTITIAL_TABSWITCH_AD="vmaxIntstlAdTabSwitchEnabled";
    private static final String PREF_ENABLE_VMAX_INTERSTITIAL_APP_OPEN_AD="vmaxIntstlAdAppOpenEnabled";
    private static final String PREF_ENABLE_VMAX_INTERSTITIAL_APP_EXIT_AD="vmaxIntstlAdAppExitEnabled";
    private static final String PREF_ENABLE_VMAX_FOOTER_BANNER_AD = "pref_enable_footer_banner_ad";
    private static final String PREF_ENABLE_VMAX_PREROLL_AD = "pref_enable_vmax_preroll_ad";
    private static final String PREF_ENABLE_VMAX_POSTROLL_AD = "pref_enable_vmax_postroll_ad";
    private static final String PREF_ENABLE_VMAX_MIDROLL_AD = "pref_enable_vmax_midroll_ad";
    private static final String PREF_VMAX_VIDEO_AD_MIN_DURATION = "pref_vmax_video_ad_min_duration";
    private static final String PREF_VMAX_FOOTER_BANNER_AD_REFRESH_RATE = "pref_vmax_footer_banner_ad_refresh_rate";
    private static final String PREF_ENABLE_VMAX_FOR_PARTNERS = "pref_enable_vmax_for_partners";
    private static final String PREF_VMAX_NATIVE_VIDEO_AD_REFRESH_RATE = "pref_native_video_ad_refresh_rate";
    private static final String PREF_VMAX_NATIVE_DISPLAY_AD_REFRESH_RATE = "pref_native_display_ad_refresh_rate";
    private static final String PREF_PRIVACY_CONSENT_MESSAGE = "pref_privacy_consent_message";
    private static final String VMAX_AD_HEADER_SECONDARY_FONT_COLOR = "pref_vmax_header_secondary_font_color";
    private static final String PREF_PARENTAL_CONTROLL_OPT = "pref_parental_controll_opt";
    private static final String PREF_PARENTAL_CONTROLL_PIN = "pref_parental_control_pin";
    private static final String PREF_ENABLE_PARENTAL_CONTROL = "pref_enable_parental_control";
    private static final String VMAX_LAYOUT_BG_BORDER_COLOR = "pref_vmax_layout_bg_border_color";
    private static final String PREF_ENABLE_BRANCH_IO_ANALYTICS = "pref_enable_branch_io_analytics";
    private static final String PREF_BRANCH_IO_EVENT_PRIORITY = "pref_branch_io_event_priority";
    private static final String PREF_ENABLE_BRANCH_IO_PLAYED_EVENT_FOR_30_SEC = "pref_branch_io_played_event_for_30_sec";
    private static final String PREF_BRANCH_IO_PLAYBACK_EVENT_SECONDS = "pref_branch_io_playback_event_sec";
    private static final String PREF_VMAX_INTERSTITIAL_AD_FREQUENCY = "pref_vmax_interstitial_ad_frequency";
    private static final String PREF_PROMO_AD_JSON = "pref_promo_ad_json";
    private static final String PREF_APP_LAUNCH_COUNT_UP_UNTILL_20 = "pref_app_launch_count_up_untill_20";
    private static final String PREF_PROMO_AD_ID = "pref_promo_ad_id";
    private static final String PARTNER_DETAILS="partnerDetailResponses";
    private static final String PREF_PUBLISHERGROUP_IDS_ANDROID = "publisherGroupIds_Android";
    private static final String PrefSubtitleName = "subtitle";
    private static final String PREF_IS_PREFERRED_LANGUAGE_SELECTION_FRAGMENTSHOWN = "is_preferred_language_fragment_shown";
    private static final String PREF_PREFERRED_LANGUAGES_SELECTED = "preferred_languages_selected";
    private static final String PREF_WATCHLIST_ITEMS = "watchlist_items";
    private static final String PREF_COUNT_RECENT_SEARCH = "count_recent_search";
    private static final String PREF_SERVICE_NAME = "service_name";
    private static final String PREF_DEFAULT_SERVICE_NAME ="default_service_name";
    private static final String PREF_SERVICE_CONTENT_CONFIG = "service_content_config";
    private static final String PREF_ABOUT_APP_URL = "about_app_url";
    private static final String PREF_HELP_URL = "help_url";
    private static final String PREF_PRIVACY_POLICY_URL = "privacy_policy_url";
    private static final String PREF_SUPPORT_URL = "support_url";
    private static final String PREF_REFUND_POLICY_URL = "refund_policy_url";
    private static final String PREF_TNC_URL = "tnc_url";
    private static final String PREF_SUBSCRIBED = "subscribed";
    private static final String SUBSCRIPTION_STATUS = "";
    private static final String PREF_BUY = "buy";
    private static final String PREF_VIDEO_PLAYED_FIRST_TIME = "video played first time";
    private static final String PREF_FAQ_URL = "faq_url";
    private final  String PREF_APP_LAUNCH_COUNT = "appLaunch";
    private static final  String PREF_ACCESS_TOKEN = "access_token";
    private static final String PREF_OTP_EXPIRY_TIME_OUT = "pref_otp_expiry_time_out";
    private final String PREF_HUNGAMA_NETWORK_TYPE = "pref_hungama_network_type";

    private final String EU_USER_ENABLED = "EU_USER_ENABLED";
    private final String USER_CONSENT = "user_consent";
    private final String USER_CONSENT_TOKEN = "user_consent_token";

    public final String IS_LIGHT_THEME_ENABLED="IS_LIGHT_THEME_ENABLED";
    public final String IS_SHOW_UPDATE_PROFILE="IS_SHOW_UPDATE_PROFILE";
    public final String IS_SHOW_THUMBNAIL_PREVIEW_SEEKBAR="IS_SHOW_THUMBNAIL_PREVIEW_SEEKBAR";
    public final String IS_SHOW_NOTIFICATION_OPTION="IS_SHOW_NOTIFICATION_OPTION";


    public final String SHOW_PREVIEW_STATUS="SHOW_PREVIEW_STATUS";
    public final String ENABLE_SUBSCRIBE_TO_APPS="ENABLE_SUBSCRIBE_TO_APPS";

    //    llowWiFiNetworkForPayment
    private final String FILE_NAME = "SpotflockPreferences";
    private final String PREF_DEVICEID = "pref_device_id";
    private final String PREF_CLIENTKEY = "pref_client_key";
    private final String PREF_CLIENTKEY_EXPIRY = "pref_client_key_expiry";
    private final String PREF_LOGIN_STATUS = "pref_msisdn_login_status";
    private final String PREF_MSISDN_NO = "pref_msisdn_no";
    private final String PREF_FULL_NAME = "pref_full_name";
    private final String PREF_LANGUAGE_SELECTED="pref_language_selected";
    private final String PREF_HOME_POPUP = "pref_home_popup";
    private final String PREF_HOME_POPUP_ID = "pref_home_popup_id";
    private final String PREF_MOBILE_NO = "pref_mobile_no";
    private final String PREF_USER_GENDER= "pref_user_gender";
    private final String PREF_USER_DOB="pref_user_dob";
    private final String PREF_NETWORK_TYPE = "pref_network_type";
    private final String PREF_IS_OFFER_PACK_SUBSCRIBED = "pref_is_offer_pack_subscribed";
    private final String PREF_IS_EXOPLAYER_ENABLED = "pref_exo_enable";
    private final String PREF_IS_EXOPLAYER_DVR_ENABLED = "pref_exo_enable_dvr";
    private final String PROFILE_NAME = "profile_name";
    private final String PROFILE_PIC = "profile_pic";
    private final String PREF_ALREADY_SET_REMINDER_TIME = "pref_already_set_reminder_time";
    private final String PREF_RATING = "pref_rating";
    private final String PREF_PROFILE_2G = "pref_2g";
    private final String PREF_PROFILE_3G = "pref_3g";
    private final String PREF_PROFILE_4G = "pref_4g";
    private final String PREF_PROFILE_5G = "pref_5g";
    private final String PREF_PROFILE_WIFI = "pref_wifi";
    private final String PREF_SEED_VALUE = "seed_value";
    private final String PREF_LAST_VERSION_UPDATED_DATE = "pref_last_version_updated_date";
    private final String PREF_PLAYER_LOGS = "pref_player_logs";
    private final String PREF_SUNDIRECT_MUSIC_URL = "pref_sundirect_music_url";
    private final String PREF_SUNDIRECT_SUPPORT_URL = "pref_sundirect_support_url";
    private final String PREF_SHREYAS_CONTACT_US_URL = "pref_shreyas_contact_us_url";
    private final String PREF_SEARCH_ERROR_MESSAGE= "search_error_message";
    private final String PREF_EPG_HELP_SCREEN = "pref_epg_help_screen";
    private final String PREF_PROGRAM_HELP_SCREEN = "pref_prog_helpscreen";
    private final String PREF_LAST_NOTIFICATION_DATA = "pref_last_notification_data";
    private final String PREF_SHOWN_COUNT_TIME_SHIFT_HELP = "pref_shown_count_of_time_shift_help";
    private final String PREF_MAX_DISPLAY_COUNT_TIMESHIFT_HLEP = "pref_max_display_count_timeshift_help";
    private final String PREF_USER_ID = "pref_user_id";
    private final String PREF_PARTNER_SIGNUP_STATUS = "pref_partner_signup_status";
    private final String PREF_ENABLE_HUNGAMA_SDK = "pref_enable_hungama_sdk";
    private final String PREF_TRACKING_DISTINCT_ID = "pref_tracking_distinct_id";
    private final String PREF_MIXPANEL_EVENT_PRIORITY = "pref_mixpanel_event_priority";
    private final String PREF_CLEVERTAP_EVENT_PRIORITY = "pref_clevertap_event_priority";
    private final String PREF_ENABLE_MYPACKS_SCREEN = "pref_enable_mypacks_screen";
    private final String PREF_ENABLE_HUNGAMA_LOGO = "pref_enable_mypacks_screen";
    private final String PREF_ENABLE_DITTO_CHANNEL_LOGO_ON_EPG = "pref_enable_ditto_channel_logo_on_epg";
    private final String PREF_DITTO_CHANNEL_LOGO_IMAGE_URL_ON_EPG = "pref_ditto_channel_logo_image_url_on_epg";
    private final String PREF_HUNGAMA_RENT_TAG = "pref_hungama_rent_tag";
    private final String PREF_ENABLE_PAST_EPG = "pref_enable_past_epg";
    private final String PREF_NO_OF_PAST_EPG_DAYS = "pref_no_of_past_days";
    private final String PREF_MESSAGE_FAILED_TO_FETCH_OFFER_PACKS = "pref_message_failed_to_fetch_offer_packs";
    private final String PREF_DITTO_STREAM_PARAM = "pref_ditto_stream_param";
    private final String PREF_ENABLE_HOOQ_BRANDING = "pref_enable_hooq_branding";
    private final String PREF_HOOQ_LOGO_URL = "pref_enable_hooq_logo_image_url";
    private final String PREF_HOOQ_BG_COLOR = "pref_enable_hooq_bg_color";
    private final String PREF_LOCATION = "pref_location";
    private final String PREF_MIXPANEL_DEFUALT_PAGE_INDEX = "pref_mixpanel_default_page_index";
    private final String PREF_EMAIL_ID = "pref_email_id";
    private final String PREF_SMART_CARD_NUMBER = "pref_smart_card_number";
    private final String PREF_TEMP_EMAIL_ID = "pref_temp_email_id";
    private final String PREF_TEMP_MSISDN = "pref_temp_msisdn";
    private static final String PREF_ENABLE_SKIP_ON_OTP = "pref_enable_existing_user_email_validation";
    private static final String PREF_IS_SKIPPED_OTP_LOGIN = "pref_is_skipped_otp_login";
    private final String PREF_ENABLE_SONY_CHANNEL_LOGO_ON_EPG = "pref_enable_sony_channel_logo_on_epg";
    private final String PREF_SONY_LOGO_IMAGE_URL_ON_EPG = "pref_sony_channel_logo_image_url_on_epg";
    private static final String PREF_ENABLE_SONYLIV_AD_V2 = "pref_enable_sonylive_ad_v2";
    private static final String PREF_IS_SKIPPED_PACKAGES = "pref_skipped_packages";
    private static final String PREF_SHOW_ALL_PACKAGES_OFFER_SCREEN = "pref_show_all_packages_offer_screen";
    private static final String PREF_ENABLE_HELP_SCREEN = "pref_enable_help_screen";
    private static final String PREF_ENABLE_MIXPANEL_API = "pref_enable_mixpanel";
    private final String PREF_PLAYBACK_QUALITY_SAVED_POS = "pref_playback_quality_saved_pos";
    private final String PREF_ENABLE_ONBOARDING_SCREEN = "pref_enable_on_boarding_screen";
    private final String PREF_SHOW_PRIVACY_CONSENT = "pref_show_privacy_consent";
    private final String PREF_APP_REDIRECT_URL="app_redirect_url";
    private static final String PREF_MOTIONVIDEO_PARTITION = "pref_motionvideo_partition";
    private static final String PREF_BANNER_BELOW_COVER_POSTER="pref_banner_below_cover_poster";
    private static final String PREF_INTERSTITIAL_AD="pref_interstitial_ad";

    private SharedPreferences sharedPreferences;
    private static PrefUtils _self = null;
    private String PREF_IS_AGE_ABOVE_18_PLUS = "pref_is_age_above_18_plus";
    private final String PREF_ENABLE_ALT_BAlAJI_DOWNlOAD = "pref_alt_balaji_download";
    private final String PREF_ENABLE_HOOQ_DOWNlOAD = "pref_hooq_download";
    private final String PREF_ENABLE_EROSNOW_DOWNlOAD = "pref_erosnow_download";
    private final String PREF_VMAX_REFRESH_RATE = "vmax_ad_refresh_rate";
    private final String PREF_VMAX_AD_ID = "vmax_native_content_id";
    private static final String PREF_ENABLE_HUNGAMA_DOWNlOAD = "pref_enable_hungama_download_v1";
    private final String PREF_ENABLE_EROSNOW_DOWNlOAD_V1 = "pref_erosnow_download";
    private final String PREF_SIGN_IN_FLOW = "pref_sign_in_flow";

    //VMAX-UI VARIABLES
    private static final String VMAX_AD_BUTTON_COLOR = "vmax_ad_button_color";
    private static final String VMAX_LAYOUT_BG_COLOR = "vmax_layout_bg_color";
    private static final String VMAX_AD_HEADER_FONT_COLOR = "vmax_ad_header_font_color";
    private static final String EVENT_LOGGER_ENABLED = "pref_event_logger_enabled";
    private static final String EVENT_LOGGER_URL ="pref_event_logger_url";
    private static final String SEARCH_RESULT_GROUP_NAME ="pref_search_result";
    private static final String PORTRAIT_PLAYER_SUGGESTIONS ="pref_portraitPlayerSuggestiosAndroid";

    private final String PREF_SECRET_KEY = "pref_secret_key";
    private static final String PrefIsHooq_sdk_enabled="hooq_sdk_enabled";
    private static final String DID_USER_RATE_APP = "did_user_rate_app";
    private final String RATING_FREQUENCY = "ratingFrequency";
    private final String RATING_MESSAGE = "ratingMessage";
    private final String SHOW_RATING = "showRating";
    private final String WHICH_ELEMENT_TO_BE_COMPARE = "whichElementToCompare";
    private final String BASE_MOU = "baseMOU";
    private final String USER_NEED_TO_GIVE_RATING = "userNeedToGiveRating";
    private final  String LAST_CONTENT_PLAYED = "lastContentPlayed";
    private final  String LAST_CONTENT_ID_PLAYED = "lastContentIdPlayed";
    private final  String LAST_CONTENT_SOURCE_PLAYED = "lastContentPlayedSource";
    private final  String LAST_CONTENT_SOURCE_DETAILS_PLAYED = "lastContentPlayedSourceDetails";
    private final  String TOTAL_MOU = "totalMOU";
    private final  String LAST_CONTENT_PLAYED_MOU = "lastContentPlayedMOU";
    private String LOCAL_USER_AGE_RANGE = "18-25 Years,26-30 Years,31-35 Years,36-40 Years,41-50 Years,51-60 Years,61-70 Years,71-80 Years,81-90 Years";
    private final String USER_AGE_RANGE = "userAgeRange";
    private String LOCAL_USER_GENDER_RANGE = "Female,Male";
    private final String USER_GENDER_RANGE = "userGenderRange";
    private static final String PREVIEW_VIDEO_PLAY_COUNT = "preview_video_play_count";
    private static final String COMMONPARAMSRESPONSE="commonparamsResponses";
    private static final String BITRATES="bitrates";
    private final String AUTO = "auto";
    private final String LOW = "low";
    private final String MEDIUM = "medium";
    private final String HIGH = "high";
    private final String PLAYERCONTROL = "PLAYERCONTROL";
    private final String SETQUALITYMAP = "setqualitymap";
    private final String PREF_CONTENT_VIDEO_QUALITY = "pref_content_video_quality";

    private static final String BUFFER_CONFIG_ANDROID = "buffer_config_android";
    private static final String PREF_ENABLE_AUTOPLAY_NONLOGGEDIN_USER = "pref_enable_autoplay_nonloggedin_user";

    private static final String PREF_IS_TO_SHOW_FORM = "pref_is_to_show_form";

    private static final String PACKAGE_QUALITY = "packageQuality";

    private static final String INTERSTRIAL_AD_CLICKS="interstrail_Ad_clicks";

    private static final String MAX_INTERSTRITAIL_AD_CLICKS="max_ad_clicks";

    public int selectedQuality = 0;
    private static final String MATURITY_TIMER = "maturity_timer";
    private final String AD_WATCH_VIDEO_COUNT = "ad_watch_video_count";
    private final String USER_COUNTRY = "user_country";
    private final String USER_SUBSCRIBED_LANGUAGE = "user_subscribed_languages";
    private final String PENDING_SMC_NUMBER = "pendingSMCNumbers";
    private final String USER_PACKAGES = "user_packages";
    private final String USER_STATE = "user_state";
    private final String USER_CITY = "user_city";

    public static final String APP_SHARE_DEEP_LINK_URL = "app_share_deep_link_url";
    public static final String APP_SHARE_PWA_URL = "app_share_url";
    public static final String OTP_lenght = "otp_length";
    public static final String IS_explore_offers = "is_explore_offers";
    public static final String IS_SUBSCRIBE_TO_APPS = "is_subscribe_to_apps";


    // default profile icon
    public String defaultProfileImage;
    public static final String PREF_DEF_PROFILE="pref_def_profile";


    private static final String PREF_ADMOB_UNIT_ID = "admob_unit_id";
    private static final String PREF_PLAYERWATERMARTKURL = "playerWatermarkURL";

    public static final String PREF_APPS_AS_HOME = "PREF_APPS_AS_HOME";

    private final String[] keysToLeaveAlone = new String[] {
            PREF_CLIENTKEY,
            PREF_CLIENTKEY_EXPIRY,
            PREF_SEED_VALUE,
            PREF_SECRET_KEY,
            PREF_DEVICEID,PREF_NETWORK_TYPE,
            IS_PORTING_TO_SECURE_RANDOM_DONE};

    private PrefUtils() {
        sharedPreferences = myplexAPISDK.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getString(String key) {
        Assert.assertNotNull(key);
        return sharedPreferences.getString(key, null);
    }

    public void setString(final String key, final String value) {
        Assert.assertNotNull(key);
        Assert.assertNotNull(value);
        sharedPreferences.edit().putString(key, value).apply();

    }

    public long getLong(String key) {
        Assert.assertNotNull(key);
        return sharedPreferences.getLong(key, 0);
    }

    public long getLong(String key, long value) {
        Assert.assertNotNull(key);
        return sharedPreferences.getLong(key, value);
    }

    public void setLong(final String key, final long value) {
        Assert.assertNotNull(key);
        Assert.assertNotNull(value);
        sharedPreferences.edit().putLong(key, value).commit();

    }

    public void setInt(final String key, final int value) {
        Assert.assertNotNull(key);
        Assert.assertNotNull(value);
        sharedPreferences.edit().putInt(key, value).apply();

    }

    public int getInt(final String key, final int defValue) {
        Assert.assertNotNull(key);
        return sharedPreferences.getInt(key, defValue);

    }

    public void setBoolean(final String key, final boolean value) {
        Assert.assertNotNull(key);
        Assert.assertNotNull(value);
        sharedPreferences.edit().putBoolean(key, value).apply();

    }

    public boolean getBoolean(final String key, boolean defaultValue) {
        Assert.assertNotNull(key);
        return sharedPreferences.getBoolean(key, defaultValue);

    }

    public String getAppVersion(Context mContext, String packageName) {
        PackageInfo pInfo = null;
        try {
            pInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pInfo != null)
            return pInfo.versionCode + "";
        else
            return "";
    }

    public static PrefUtils getInstance() {
        if (_self == null) {
            _self = new PrefUtils();
        }
        return _self;
    }

    public String getPrefLoginStatus() {
        return _self.getString(PREF_LOGIN_STATUS);
    }

    public void setPrefLoginStatus(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_LOGIN_STATUS, status);
    }

    public void setPrefProfilePicPath(String status) {
        Assert.assertNotNull(status);
        _self.setString(PROFILE_PIC, status);
    }

    public String getPrefProfilePic() {
        return _self.getString(PROFILE_PIC);
    }


    public void setPrefFullName(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_FULL_NAME, status);
    }
    public void setPrefLanguageSelected(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_LANGUAGE_SELECTED, status);
    }

    public void setPopup(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_HOME_POPUP, status);
    }
    public void setPopupAdId(String id) {
        Assert.assertNotNull(id);
        _self.setString(PREF_HOME_POPUP_ID, id);
    }

    public void setPrefMobileNumber(String mobileNumber) {
        Assert.assertNotNull(mobileNumber);
        _self.setString(PREF_MOBILE_NO, mobileNumber);
    }

    public String getPrefMobileNumber() {
        return _self.getString(PREF_MOBILE_NO);
    }

    public String getPrefFullName() {
        return _self.getString(PREF_FULL_NAME);
    }
    public String getPrefSelectedLangauage() {
        return _self.getString(PREF_LANGUAGE_SELECTED);
    }
    public boolean isPopup() {
        return _self.getBoolean(PREF_HOME_POPUP, false);
    }
    public String getPopupId() {
        return _self.getString(PREF_HOME_POPUP_ID);
    }

    public void setUserGender(String gender){
        Assert.assertNotNull(gender);
        _self.setString(PREF_USER_GENDER, gender);
    }

    public String getPrefUserGender() {
        return _self.getString(PREF_USER_GENDER);
    }

    public void setUserDOB(String gender){
        Assert.assertNotNull(gender);
        _self.setString(PREF_USER_DOB, gender);
    }

    public String getPrefUserDOB() {
        return _self.getString(PREF_USER_DOB);
    }

    public void setPrefPlayBackQuality(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_NETWORK_TYPE, status);
    }

    public String getPrefPlayBackQuality() {
        return _self.getString(PREF_NETWORK_TYPE);

    }

    public void setPrefOfferPackSubscriptionStatus(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_IS_OFFER_PACK_SUBSCRIBED, status);
    }

    public boolean getPrefOfferPackSubscriptionStatus() {
        return _self.getBoolean(PREF_IS_OFFER_PACK_SUBSCRIBED, false);
    }

    private String setEncryptedValue(String status, String seedValue) {
        try {
            String encryptValue = DeviceEncryption.encrypt(seedValue, status);
            if (!TextUtils.isEmpty(encryptValue)) {
                return encryptValue;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setPrefClientkey(String status) {
        Assert.assertNotNull(status);
        String seedValue = generateRandomString();
        if (TextUtils.isEmpty(seedValue)) {
            seedValue = generateRandomString();
        }
        String encryptValue = setEncryptedValue(status, seedValue);
        if (!TextUtils.isEmpty(encryptValue))
            _self.setString(PREF_CLIENTKEY, encryptValue);
        if (Build.VERSION.SDK_INT >= 24 && !PrefUtils.getInstance().getBoolean(IS_PORTING_TO_SECURE_RANDOM_DONE, false)) {
            PrefUtils.getInstance().setBoolean(IS_PORTING_TO_SECURE_RANDOM_DONE, true);
        }
    }

    public String getPrefClientkey() {
        String encryptValue = _self.getString(PREF_CLIENTKEY);
        String decryptedValue = getDecryptedValue(encryptValue, getSeedValue());
        if (Build.VERSION.SDK_INT >= 24 && decryptedValue != null &&
                !PrefUtils.getInstance()
                        .getBoolean(IS_PORTING_TO_SECURE_RANDOM_DONE, false)) {
            setPrefClientkey(decryptedValue);
            PrefUtils.getInstance().setBoolean(IS_PORTING_TO_SECURE_RANDOM_DONE, true);
        }
        return decryptedValue;
    }

    private String getDecryptedValue(String encryptValue, String seedValue) {
        String decryptValue = null;
        try {
            if (!TextUtils.isEmpty(encryptValue) && !TextUtils.isEmpty(seedValue)) { // both encrypt and seed value
                decryptValue = DeviceEncryption.decrypt(seedValue, encryptValue);
                return decryptValue;
            }

            if (!TextUtils.isEmpty(encryptValue) && TextUtils.isEmpty(seedValue)) { // seed value null case
                seedValue = generateRandomString();
                String encryptedValue = DeviceEncryption.encrypt(seedValue, encryptValue);
                _self.setString(PREF_CLIENTKEY, encryptedValue);
                decryptValue = DeviceEncryption.decrypt(seedValue, encryptedValue);
                return decryptValue;
            }

        } catch (Exception e) {
            if (e instanceof NoSuchProviderException) {
                Log.e("P_EXCEPTION", "satisfied");
            }
            e.printStackTrace();
        }

        return decryptValue;
    }

    public void setSeedValue(String seedValue) {
        Assert.assertNotNull(seedValue);
        _self.setString(PREF_SEED_VALUE, seedValue);
    }

    public String getSeedValue() {
        return _self.getString(PREF_SEED_VALUE);
    }

    public void setPrefClientkeyExpiry(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_CLIENTKEY_EXPIRY, status);
    }

    public String getPrefClientkeyExpiry() {
        return _self.getString(PREF_CLIENTKEY_EXPIRY);

    }

    public void setPrefDeviceid(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_DEVICEID, status);
    }

    public String getPrefDeviceid() {
        return _self.getString(PREF_DEVICEID);

    }

    public void setPrefMsisdnNo(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_MSISDN_NO, status);
    }

    public String getPrefMsisdnNo() {
        return _self.getString(PREF_MSISDN_NO);

    }
    public String getDefaultProfileImage() {
        return _self.getString(PREF_DEF_PROFILE);
    }

    public void setDefaultProfileImage(String defaultProfileImage) {
        this.defaultProfileImage = defaultProfileImage;
        _self.setString(PREF_DEF_PROFILE,defaultProfileImage);
    }

    public boolean getPrefIsExoplayerDvrEnabled() {
        return _self.getBoolean(PREF_IS_EXOPLAYER_DVR_ENABLED, true);
    }

    public boolean getPrefIsExoplayerEnabled() {
        return _self.getBoolean(PREF_IS_EXOPLAYER_ENABLED, true);
    }

    public String getProfileName() {
        return _self.getString(PROFILE_NAME);
    }

    public void setPrefIsExoplayerDvrEnabled(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_IS_EXOPLAYER_DVR_ENABLED, status);
    }

    public void setPrefIsExoplayerEnabled(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_IS_EXOPLAYER_ENABLED, status);
    }

    public void setProfileName(String status) {
        Assert.assertNotNull(status);
        _self.setString(PROFILE_NAME, status);
    }

    public void setPrefAlreadySetReminderTimes(String status) {
        _self.setString(PREF_ALREADY_SET_REMINDER_TIME, status);
    }

    public String getPrefAlreadySetReminderTimes() {
        if(_self.getString(PREF_ALREADY_SET_REMINDER_TIME)==null || _self.getString(PREF_ALREADY_SET_REMINDER_TIME).equals(""))
            return null;
        return _self.getString(PREF_ALREADY_SET_REMINDER_TIME);
    }

    public void setRating(int value) {
        _self.setInt(PREF_RATING, value);
    }

    public int getRating() {
        return _self.getInt(PREF_RATING, 0);
    }

    public void setProfile2G(String value) {
        _self.setProfileString(PREF_PROFILE_2G, value);
    }

    public String getProfile2G() {
        return _self.getString(PREF_PROFILE_2G);
    }

    public void setProfile3G(String value) {
        _self.setProfileString(PREF_PROFILE_3G, value);
    }

    public String getProfile3G() {
        return _self.getString(PREF_PROFILE_3G);
    }

    public void setProfile5G(String value) {
        _self.setProfileString(PREF_PROFILE_5G, value);
    }

    public String getProfile5G() {
        return _self.getString(PREF_PROFILE_5G);
    }  public void setProfile4G(String value) {
        _self.setProfileString(PREF_PROFILE_4G, value);
    }

    public String getProfile4G() {
        return _self.getString(PREF_PROFILE_4G);
    }

    public void setProfileWifi(String value) {
        _self.setProfileString(PREF_PROFILE_WIFI, value);
    }

    public String getProfileWifi() {
        return _self.getString(PREF_PROFILE_WIFI);
    }

    public void setProfileString(final String key, final String value) {

        sharedPreferences.edit().putString(key, value).apply();

    }

    private String generateRandomString() {
        String rndTxt = myplexAPISDK.getApplicationContext().getResources().getString(R.string.random_string_txt, new Date().toString());
        String hexTxt = DeviceEncryption.toHex(rndTxt);
        setSeedValue(hexTxt);
        return hexTxt;

    }

    public boolean getPlayerLogs() {
        return _self.getBoolean(PREF_PLAYER_LOGS, false);
    }

    public void setPlayerLogs(final boolean value) {
        _self.setBoolean(PREF_PLAYER_LOGS, value);
    }

    public void setLastVersionUpdatedDate(long value) {
        _self.setLong(PREF_LAST_VERSION_UPDATED_DATE, value);
    }

    public long getLastVersionUpdatedDate() {
        return _self.getLong(PREF_LAST_VERSION_UPDATED_DATE);
    }


    public void setsundirectMusicURL(String value) {
        _self.setProfileString(PREF_SUNDIRECT_MUSIC_URL, value);
    }

    public String getsundirectMusicURL() {
        return _self.getString(PREF_SUNDIRECT_MUSIC_URL);
    }

    public void setEpgHelpScreenPref(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_EPG_HELP_SCREEN, status);
    }

    public boolean getEpgHelpScreenPref() {
        return _self.getBoolean(PREF_EPG_HELP_SCREEN, false);
    }

    public void setProgramHelpScreenPref(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_PROGRAM_HELP_SCREEN, status);
    }

    public boolean getProgramHelpScreenPref() {
        return _self.getBoolean(PREF_PROGRAM_HELP_SCREEN, false);
    }

    public void setPrefLastNotificationData(String value) {
        Assert.assertNotNull(value);
        _self.setProfileString(PREF_LAST_NOTIFICATION_DATA, value);
    }

    public String getPrefLastNotificationData() {
        return _self.getString(PREF_LAST_NOTIFICATION_DATA);
    }

    public void setPrefShownCountTimeShiftHelp(int value) {
        Assert.assertNotNull(value);
        _self.setInt(PREF_SHOWN_COUNT_TIME_SHIFT_HELP, value);
    }

    public int getPrefShownCountTimeShiftHelp() {
        return _self.getInt(PREF_SHOWN_COUNT_TIME_SHIFT_HELP, 0);
    }


    public void setPrefMaxDisplayCountTimeShift(int value) {
        Assert.assertNotNull(value);
        _self.setInt(PREF_MAX_DISPLAY_COUNT_TIMESHIFT_HLEP, value);
    }

    public int getPrefMaxDisplayCountTimeShift() {
        return _self.getInt(PREF_MAX_DISPLAY_COUNT_TIMESHIFT_HLEP, 1);
    }

    public int getPrefUserId() {
        return _self.getInt(PREF_USER_ID, 0);
    }

    public void setPrefUserId(int userId) {
        Assert.assertNotNull(userId);
        _self.setInt(PREF_USER_ID, userId);
    }

    public String getPrefPartenerSignUpStatus() {
        return _self.getString(PREF_PARTNER_SIGNUP_STATUS);
    }

    public void setPrefPartenerSignUpStatus(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_PARTNER_SIGNUP_STATUS, status);
    }

    public void setPrefEnableHungamaSDK(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_HUNGAMA_SDK, status);
    }

    public boolean getPrefEnableHungamaSDK() {
        return _self.getBoolean(PREF_ENABLE_HUNGAMA_SDK, true);
    }

    public String getPrefTrackingDistinctId() {
        return _self.getString(PREF_TRACKING_DISTINCT_ID);
    }

    public void setPrefTrackingDistinctId(String trackingDistinctId) {
        Assert.assertNotNull(trackingDistinctId);
        _self.setString(PREF_TRACKING_DISTINCT_ID, trackingDistinctId);
    }

    public void setPrefMixpanelEventPriority(int value) {
        _self.setInt(PREF_MIXPANEL_EVENT_PRIORITY, value);
    }

    public int getPrefMixpanelEventPriority() {
        return _self.getInt(PREF_MIXPANEL_EVENT_PRIORITY, 2);
    }

    public void setPrefEnableMyPackScreen(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_MYPACKS_SCREEN, status);
    }

    public boolean getPrefEnableMyPackScreen() {
        return _self.getBoolean(PREF_ENABLE_MYPACKS_SCREEN, false);
    }

    public boolean getPrefEnableHungamaLogo() {
        return _self.getBoolean(PREF_ENABLE_HUNGAMA_LOGO, false);
    }

    public void setPrefEnableHungamaLogo(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_HUNGAMA_LOGO, status);
    }

    public boolean getPrefEnableDittoChannelLogoOnEpg() {
        return _self.getBoolean(PREF_ENABLE_DITTO_CHANNEL_LOGO_ON_EPG, false);
    }

    public void setPrefEnableDittoChannelLogoOnEpg(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_DITTO_CHANNEL_LOGO_ON_EPG, status);
    }

    public String getPrefDittoChannelLogoUrlOnEpg() {
        return _self.getString(PREF_DITTO_CHANNEL_LOGO_IMAGE_URL_ON_EPG);
    }

    public void setPrefDittoChannelLogoUrlOnEpg(String value) {
        Assert.assertNotNull(value);
        _self.setString(PREF_DITTO_CHANNEL_LOGO_IMAGE_URL_ON_EPG, value);
    }

    public void setPrefEnableHungamaRentTag(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_HUNGAMA_RENT_TAG, status);
    }

    public boolean getPrefEnableHungamaRentTag() {
        return _self.getBoolean(PREF_HUNGAMA_RENT_TAG, false);
    }

    public void setPrefEnablePastEpg(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_PAST_EPG, status);
    }

    public boolean getPrefEnablePastEpg() {
        return _self.getBoolean(PREF_ENABLE_PAST_EPG, false);
    }

    public void setPrefNoOfPastEpgDays(int value) {
        Assert.assertNotNull(value);
        _self.setInt(PREF_NO_OF_PAST_EPG_DAYS, value);
    }

    public int getPrefNoOfPastEpgDays() {
        return _self.getInt(PREF_NO_OF_PAST_EPG_DAYS, 5);
    }

    public String getPrefMessageFailedToFetchOffers() {
        return _self.getString(PREF_MESSAGE_FAILED_TO_FETCH_OFFER_PACKS);
    }

    public void setPrefMessageFailedToFetchOffers(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_MESSAGE_FAILED_TO_FETCH_OFFER_PACKS, message);
    }

    public String getPrefDittoStreamParam() {
        return _self.getString(PREF_DITTO_STREAM_PARAM);
    }

    public void setPrefDittoStreamParam(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_DITTO_STREAM_PARAM, message);
    }


    public void setPrefEnableHooqBranding(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_HOOQ_BRANDING, status);
    }

    public boolean getPrefEnableHooqBranding() {
        return _self.getBoolean(PREF_ENABLE_HOOQ_BRANDING, true);
    }

    public void setPrefHooqBgsectionColor(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_HOOQ_BG_COLOR, message);
    }

    public String getPrefHooqBgsectionColor() {
        return _self.getString(PREF_HOOQ_BG_COLOR);
    }

    public void setPrefHooqLogoImageUrl(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_HOOQ_LOGO_URL, message);
    }

    public String getPrefHooqLogoImageUrl() {
        return _self.getString(PREF_HOOQ_LOGO_URL);
    }

    public void setPrefLocation(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_LOCATION, message);
    }

    public String getPrefLocation() {
        return _self.getString(PREF_LOCATION);
    }

    public void setPrefMixpanelDefaultPageIndex(int message) {
        Assert.assertNotNull(message);
        _self.setInt(PREF_MIXPANEL_DEFUALT_PAGE_INDEX, message);
    }

    public int getPrefMixpanelDefaultPageIndex() {
        return _self.getInt(PREF_MIXPANEL_DEFUALT_PAGE_INDEX, 0);
    }


    public void setPrefEnableSonyLivAd(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_SONYLIV_AD, status);
    }

    public boolean getPrefEnableSonyLivAd() {
        return _self.getBoolean(PREF_ENABLE_SONYLIV_AD, false);
    }


    public void setPrefEnableVAST3Ad(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_VAST3_AD, status);
    }

    public boolean getPrefEnableVAST3Ad() {
        return _self.getBoolean(PREF_ENABLE_VAST3_AD, false);
    }

    public void setPrefAdProviderTagOoyala(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_ADPROVIDER_TAG_OOYALA, message);
    }

    public String getPrefAdProviderTagOoyala() {
        return _self.getString(PREF_ADPROVIDER_TAG_OOYALA);
    }


    public void setPrefAdProviderTagVAST3(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_ADPROVIDER_TAG_VAST3, message);
    }

    public String getPrefAdProviderTagVAST3() {
        return _self.getString(PREF_ADPROVIDER_TAG_VAST3);
    }

    public void setPrefEnableMusicTab(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_MUSIC_TAG, status);
    }

    public boolean getPrefEnableMusicTab() {
        return _self.getBoolean(PREF_ENABLE_MUSIC_TAG, true);
    }


    public void setPrefAllowWiFiNetworkForPayment(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ALLOW_WIFI_FOR_PAYMENT, status);
    }

    public boolean getPrefAllowWiFiNetworkForPayment() {
        return _self.getBoolean(PREF_ALLOW_WIFI_FOR_PAYMENT, false);
    }

    public void setPrefEmailID(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_EMAIL_ID, status);
    }

    public String getPrefEmailID() {
        return _self.getString(PREF_EMAIL_ID);

    }

    public void setPrefSmartCardNumber(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_SMART_CARD_NUMBER, status);
    }


    public String getPrefSmartCardNumber() {
        return _self.getString(PREF_SMART_CARD_NUMBER);

    }

    public String getPrefTempMsisdn() {
        return _self.getString(PREF_TEMP_MSISDN);
    }

    public void setPrefTempMsisdn(String msg) {
        Assert.assertNotNull(msg);
        _self.setString(PREF_TEMP_MSISDN, msg);
    }

    public String getPrefTempEMAILID() {
        return _self.getString(PREF_TEMP_EMAIL_ID);
    }

    public void setPrefTempEMAILID(String msg) {
        Assert.assertNotNull(msg);
        _self.setString(PREF_TEMP_EMAIL_ID, msg);
    }


    public void setPrefEnableManualOTP(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_MANUAL_OTP, status);
    }

    public boolean getPrefEnableManualOTP() {
        return _self.getBoolean(PREF_ENABLE_MANUAL_OTP, true);
    }

    public void setPrefOTPDetectionTimeOut(int value) {
        Assert.assertNotNull(value);
        _self.setInt(PREF_OTP_DETECTION_TIME_OUT, value);
    }

    public int getPrefOTPDetectionTimeOut() {
        return _self.getInt(PREF_OTP_DETECTION_TIME_OUT, 10);
    }

    public void setPrefEnableSkipOnOTP(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_SKIP_ON_OTP, status);
    }

    public boolean getPrefEnableSkipOnOTP() {
        return _self.getBoolean(PREF_ENABLE_SKIP_ON_OTP, false);
    }
    public void setPrefShowPrivacyConsent(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_SHOW_PRIVACY_CONSENT, status);
    }

    public boolean getPrefShowPrivacyConsent() {
        return _self.getBoolean(PREF_SHOW_PRIVACY_CONSENT, true);
    }
    public void setPrefIsOTPSkipped(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_IS_SKIPPED_OTP_LOGIN, status);
    }

    public boolean getPrefIsOTPSkipped() {
        return _self.getBoolean(PREF_IS_SKIPPED_OTP_LOGIN, false);
    }

    public String getPrefShareMessage() {
        return _self.getString(PREF_SHARE_MESSAGE);
    }

    public void setPrefShareMessage(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_SHARE_MESSAGE, message);
    }


    public String getPrefErosNowMusicLogoUrlOnEpg() {
        return _self.getString(PREF_EROSNOW_MUSIC_LOGO_URL);
    }

    public void setPrefErosNowMusicLogoUrlOnEpg(String value) {
        Assert.assertNotNull(value);
        _self.setString(PREF_EROSNOW_MUSIC_LOGO_URL, value);
    }


    public String getPrefShareUrl() {
        return _self.getString(PREF_SHARE_URL);
    }

    public void setPrefShareUrl(String url) {
        Assert.assertNotNull(url);
        _self.setString(PREF_SHARE_URL, url);
    }

    public boolean getPrefEnableSonyChannelLogoOnEpg() {
        return _self.getBoolean(PREF_ENABLE_SONY_CHANNEL_LOGO_ON_EPG, false);
    }

    public void setPrefEnableSonyChannelLogoOnEpg(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_SONY_CHANNEL_LOGO_ON_EPG, status);
    }

    public String getPrefSonyChannelLogoUrlOnEpg() {
        return _self.getString(PREF_SONY_LOGO_IMAGE_URL_ON_EPG);
    }

    public void setPrefSonyChannelLogoUrlOnEpg(String value) {
        Assert.assertNotNull(value);
        _self.setString(PREF_SONY_LOGO_IMAGE_URL_ON_EPG, value);
    }

    public boolean getPrefEnableSonylivAdV2() {
        return _self.getBoolean(PREF_ENABLE_SONYLIV_AD_V2, false);
    }

    public void setPrefEnableSonylivAdV2(boolean value) {
        Assert.assertNotNull(value);
        _self.setBoolean(PREF_ENABLE_SONYLIV_AD_V2, value);
    }

    public void setPrefIsSkipPackages(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_IS_SKIPPED_PACKAGES, status);
    }

    public boolean getPrefIsSkipPackages() {
        return _self.getBoolean(PREF_IS_SKIPPED_PACKAGES, false);
    }

    public void setPrefShowAllPackagesOfferScreen(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_SHOW_ALL_PACKAGES_OFFER_SCREEN, status);
    }

    public boolean getPrefShowAllPackagesOfferScreen() {
        return _self.getBoolean(PREF_SHOW_ALL_PACKAGES_OFFER_SCREEN, false);
    }

    public void setPrefEnableHelpScreen(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_HELP_SCREEN, status);
    }

    public boolean getPrefEnableHelpScreen() {
        return _self.getBoolean(PREF_ENABLE_HELP_SCREEN, false);
    }

    public boolean getPrefEnableMixpanel() {
        return _self.getBoolean(PREF_ENABLE_MIXPANEL_API, false);
    }

    public void setPrefEnableMixpanel(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_MIXPANEL_API, status);
    }

    public String getPrefChromeCastRecieverId() {
        return _self.getString(PREF_CHROMECAST_RECIEVER_ID);
    }

    public void setPrefChromeCastRecieverId(String value) {
        Assert.assertNotNull(value);
        _self.setString(PREF_CHROMECAST_RECIEVER_ID, value);
    }

    public void setPrefPlayBackQualityPos(int mSelectedPosition) {
        Assert.assertNotNull(mSelectedPosition);
        _self.setInt(PREF_PLAYBACK_QUALITY_SAVED_POS, mSelectedPosition);
    }

    public int getPrefPlayBackQualityPos() {
        return _self.getInt(PREF_PLAYBACK_QUALITY_SAVED_POS, 0);
    }

    public void setPrefEnableOnBoardingScreen(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_ONBOARDING_SCREEN, status);
    }

    public boolean getPrefEnableOnBoardingScreen() {
        return _self.getBoolean(PREF_ENABLE_ONBOARDING_SCREEN, false);
    }

    public void setIsDownloadOnlyWifi(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_DOWNLOAD_ONLY_ON_WIFI, status);
    }

    public boolean isDownloadOnlyOnWifi() {
        return _self.getBoolean(PREF_DOWNLOAD_ONLY_ON_WIFI, false);
    }

    public void setAutoPlay(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_AUTOPLAY, status);
    }

    public boolean isAutoplay() {
        return _self.getBoolean(PREF_AUTOPLAY, false);
    }
    public void setWhiteMode(boolean mode) {
        Assert.assertNotNull(mode);
        _self.setBoolean(PREF_DARKMODE, mode);
    }

    public boolean getWhiteMode() {
        return _self.getBoolean(PREF_DARKMODE, false);
    }

    public void setEnableNotifications(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_NOTIFICATION, status);
    }

    public boolean isNotificationEnabled() {
        return _self.getBoolean(PREF_NOTIFICATION, true);
    }

    public void setPrefIsCleverTapGCMTokenUpdated(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_IS_CLEVERTAP_GCM_TOKEN_UPDATED, status);
    }

    public boolean getPrefIsCleverTapGCMTokenUpdated() {
        return _self.getBoolean(PREF_IS_CLEVERTAP_GCM_TOKEN_UPDATED, false);
    }

    public void setPrefIsAgeAbove18Plus(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_IS_AGE_ABOVE_18_PLUS, status);
    }

    public boolean getprefIsAgeAbove18Plus() {
        return _self.getBoolean(PREF_IS_AGE_ABOVE_18_PLUS, false);
    }


    public String getPrefYupTVChannelLogoImageUrl() {
        return _self.getString(PREF_YUPP_TV_LOGO_URL);
    }

    public void setPrefYupTVChannelLogoImageUrl(String value) {
        Assert.assertNotNull(value);
        _self.setString(PREF_YUPP_TV_LOGO_URL, value);
    }


    public boolean getPrefEnableYuptvChannelLogoOnEpg() {
        return _self.getBoolean(PREF_ENABLE_YUPP_TV_CHANNEL_LOGO_ON_EPG, false);
    }

    public void setPrefEnableYuptvChannelLogoOnEpg(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_YUPP_TV_CHANNEL_LOGO_ON_EPG, status);
    }


    public void setPrefCleverTapEventPriority(int value) {
        _self.setInt(PREF_CLEVERTAP_EVENT_PRIORITY, value);
    }

    public int getPrefCleverTapEventPriority() {
        return _self.getInt(PREF_CLEVERTAP_EVENT_PRIORITY, 2);
    }

    public boolean getPrefEnableCleverTap() {
        return _self.getBoolean(PREF_ENABLE_CLEVERTAP_API, true);
    }

    public void setPrefEnableCleverTap(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(PREF_ENABLE_CLEVERTAP_API, status);
    }

    public void setHooqSessionExpiryTime(String sessionExpiryTime) {
        Assert.assertNotNull(sessionExpiryTime);
        _self.setString(PREF_HOOQ_SESSION_EXPIRY_TIME, sessionExpiryTime);
    }

    public String gePrefHooqSessionExpiryTime() {
        return _self.getString(PREF_HOOQ_SESSION_EXPIRY_TIME);
    }


    public void setHooqSessionToken(String sessionExpiryTime) {
        Assert.assertNotNull(sessionExpiryTime);
        _self.setString(PREF_HOOQ_SESSION_TOKEN, sessionExpiryTime);
    }

    public String gePrefHooqSessionToken() {
        return _self.getString(PREF_HOOQ_SESSION_TOKEN);
    }


    public void setHooqHmac(String sessionExpiryTime) {
        Assert.assertNotNull(sessionExpiryTime);
        _self.setString(PREF_HOOQ_HMAC, sessionExpiryTime);
    }

    public String gePrefHooqHmac() {
        return _self.getString(PREF_HOOQ_HMAC);
    }

    public void setPrefEnableAltBalajiDownload(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_ALT_BAlAJI_DOWNlOAD, b);
    }

    public boolean gePrefEnableAltBalajiDownload() {
        return _self.getBoolean(PREF_ENABLE_ALT_BAlAJI_DOWNlOAD, true);
    }

    public void setPrefEnableHooqDownload(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_HOOQ_DOWNlOAD, b);
    }

    public boolean gePrefEnableHooqDownload() {
        return _self.getBoolean(PREF_ENABLE_HOOQ_DOWNlOAD, true);
    }

    public void setPrefEnableErosnowDownload(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_EROSNOW_DOWNlOAD, b);
    }

    public boolean gePrefEnableErosnowDownload() {
        return _self.getBoolean(PREF_ENABLE_EROSNOW_DOWNlOAD, true);
    }



    public void setPrefEnableErosnowDownloadV1(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_EROSNOW_DOWNlOAD_V1, b);
    }

    public boolean gePrefEnableErosnowDownloadV1() {
        return _self.getBoolean(PREF_ENABLE_EROSNOW_DOWNlOAD_V1, true);
    }


    public String getPrefShareMessageForMenu() {
        return _self.getString(PREF_SHARE_MESSAGE_FOR_MENU);
    }

    public void setPrefShareMessageForMenu(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_SHARE_MESSAGE_FOR_MENU, message);
    }


    public String getPrefShareUrlForMenu() {
        return _self.getString(PREF_SHARE_URL_FOR_MENU);
    }

    public void setPrefShareUrlForMenu(String url) {
        Assert.assertNotNull(url);
        _self.setString(PREF_SHARE_URL_FOR_MENU, url);
    }

    public String getPrefVmaxRefreshRate() {
        return _self.getString(PREF_VMAX_REFRESH_RATE);
    }

    public void setPrefVmaxRefreshRate(String rate) {
        _self.setString(PREF_VMAX_REFRESH_RATE, rate);
    }

    public String getPrefVmaxNativeAdId() {
        return _self.getString(PREF_VMAX_AD_ID);
    }

    public String getPrefVmaxNativeDisplayAdId() {
        return _self.getString(PREF_VMAX_NATIVE_DISPLAY_AD_ID);
    }


    public void setPrefVmaxNativeAdId(String id) {
        _self.setString(PREF_VMAX_AD_ID, id);
    }



    public boolean getPrefEnableErosNowPlayerLogs() {
        return _self.getBoolean(PREF_ENABLE_EROS_NOW_PLAYER_LOGS, false);
    }

    public void setPrefEnableErosNowPlayerLogs(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_EROS_NOW_PLAYER_LOGS, b);
    }


    public String getPrefPlayerLogsEnableTo() {
        return _self.getString(PREF_PLAYER_LOGS_ENABLE_TO);
    }

    public void setPrefPlayerLogsEnableTo(String url) {
        Assert.assertNotNull(url);
        _self.setString(PREF_PLAYER_LOGS_ENABLE_TO, url);
    }

    public void setPrefEnableHungamaDownload(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_HUNGAMA_DOWNlOAD, b);
    }

    public static boolean gePrefEnableHungamaDownload() {
        return _self.getBoolean(PREF_ENABLE_HUNGAMA_DOWNlOAD, true);
    }

    public boolean getPrefShowPlaybackOptHint() {
        return _self.getBoolean(PREF_SHOW_PLAYBACK_OPT_HINT, true);
    }

    public void setPrefShowPlaybackOptHint(boolean show) {
        Assert.assertNotNull(show);
        _self.setBoolean(PREF_SHOW_PLAYBACK_OPT_HINT, show);
    }

    public void setPrefEnableHOOQChromeCast(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_HOOQ_CHROMECAST, b);
    }

    public static boolean gePrefEnableHOOQChromeCast() {
        return _self.getBoolean(PREF_ENABLE_HOOQ_CHROMECAST, true);
    }

    public void setAppsFlyerPlayedEventFor30SecFired(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_APPSFLYER_PLAYED_EVENT_FOR_30_SEC, b);
    }

    public boolean getAppsFlyerPlayedEventFor30SecFired() {
        return _self.getBoolean(PREF_ENABLE_APPSFLYER_PLAYED_EVENT_FOR_30_SEC, false);
    }

    public void setPrefStandardPackageIds(String prefStandardPackageIds) {
        Assert.assertNotNull(prefStandardPackageIds);
        _self.setString(PREF_STANDARD_PACKAGE_IDS, prefStandardPackageIds);
    }

    public String getPrefStandardPackageIds() {
        return _self.getString(PREF_STANDARD_PACKAGE_IDS);
    }

    public String getPrefMessageDRMLicenseFailed() {
        return _self.getString(PREF_MESSAGE_DRM_LICENSE_FAILED);
    }

    public void setPrefMessageDRMLicenseFailed(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_MESSAGE_DRM_LICENSE_FAILED, message);
    }


    public String getPrefMessageNoSpaceWhileUnzip() {
        return _self.getString(PREF_MESSAGE_NO_SPACE_AVAILABLE_TO_UNZIP);
    }

    public void setPrefMessageNoSpaceWhileUnzip(String message) {
        Assert.assertNotNull(message);
        _self.setString(PREF_MESSAGE_NO_SPACE_AVAILABLE_TO_UNZIP, message);
    }

    public String getPrefDownloadStateDRMLicenseFailed() {
        return _self.getString(PREF_DOWNLOAD_STATE_DRM_LICENSE_FAILED);
    }

    public String getPrefDownloadStateFailedDueToLowMem() {
        return _self.getString(PREF_DOWNLOAD_STATE_FAILED_TO_DOWNLOAD_DUE_TO_LOW_MEM);
    }

    public String getPrefDownloadStateNOSPAvailable() {
        return _self.getString(PREF_DOWNLOAD_STATE_NO_SPACE_AVAILABLE_TO_UNZIP);
    }

    public void setPrefDownloadStateDRMLicenseFailed(String downldStateDRMLicenseFailed) {
        Assert.assertNotNull(downldStateDRMLicenseFailed);
        _self.setString(PREF_DOWNLOAD_STATE_DRM_LICENSE_FAILED, downldStateDRMLicenseFailed);
    }

    public void setPrefDownldStateFailedDueToLowMem(String downldStateFailedDueToLowMem) {
        Assert.assertNotNull(downldStateFailedDueToLowMem);
        _self.setString(PREF_DOWNLOAD_STATE_FAILED_TO_DOWNLOAD_DUE_TO_LOW_MEM, downldStateFailedDueToLowMem);
    }

    public void setPrefDownloadStateNOSPAvailable(String downldStateNOSPAvailable) {
        Assert.assertNotNull(downldStateNOSPAvailable);
        _self.setString(PREF_DOWNLOAD_STATE_NO_SPACE_AVAILABLE_TO_UNZIP, downldStateNOSPAvailable);
    }

    public void setPrefAppsflyerPlaybackEventSeconds(String appsflyerPlaybackEventSeconds) {
        Assert.assertNotNull(appsflyerPlaybackEventSeconds);
        _self.setString(PREF_APPSFLYER_PLAYBACK_EVENT_SECONDS, appsflyerPlaybackEventSeconds);
    }


    public String getPrefAppsflyerPlaybackEventSeconds() {
        return _self.getString(PREF_APPSFLYER_PLAYBACK_EVENT_SECONDS);
    }

    public void setBannerAutoScrollFrequency(String bannerFrequency){
        Assert.assertNotNull(bannerFrequency);
        _self.setString(BANNER_AUTO_SCROLL_FREQUENCY,bannerFrequency);
    }

    public void setNextEpisodePopupPercentage(String popupPercentage) {
        Assert.assertNotNull(popupPercentage);
        _self.setString(NEXT_EPISODE_POPUP_PERCENTAGE, popupPercentage);
    }

    public String getNextEpisodePopupPercentage() {
        return _self.getString(NEXT_EPISODE_POPUP_PERCENTAGE);
    }
    public String getBannerAutoScrollFrequency(){
        return _self.getString(BANNER_AUTO_SCROLL_FREQUENCY);
    }

    public boolean getPrefIsGestureTipShown() {
        return _self.getBoolean(PREF_IS_GESTURE_TIPS_SHOWN, false);
    }

    public void setPrefIsGestureTipShown(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_IS_GESTURE_TIPS_SHOWN, b);
    }

    public void setPrefPlayerSeekTimeSeconds(String popupPercentage) {
        Assert.assertNotNull(popupPercentage);
        _self.setString(PREF_FFORWRD_SECONDS, popupPercentage);
    }

    public String getPrefPlayerSeekTimeSeconds() {
        return _self.getString(PREF_FFORWRD_SECONDS);
    }

    //VMAX-AD Variables and setters and getters
    public void setVmaxAdButtonColor(String color){
        Assert.assertNotNull(color);
        _self.setString(VMAX_AD_BUTTON_COLOR,color);
    }
    public String getVmaxAdButtonColor(){
       return _self.getString(VMAX_AD_BUTTON_COLOR);
    }
    public void setVmaxLayoutBgColor(String color){
        Assert.assertNotNull(color);
        _self.setString(VMAX_LAYOUT_BG_COLOR,color);
    }
    public String getVmaxLayoutBgColor(){
       return _self.getString(VMAX_LAYOUT_BG_COLOR);
    }
    public void setVmaxAdHeaderFontColor(String color){
        Assert.assertNotNull(color);
        _self.setString(VMAX_AD_HEADER_FONT_COLOR,color);
    }
    public String getVmaxAdHeaderFontColor(){
       return _self.getString(VMAX_AD_HEADER_FONT_COLOR);
    }

    public void setVmaxAdHeaderSecondaryFontColor(String color) {
        Assert.assertNotNull(color);
        _self.setString(VMAX_AD_HEADER_SECONDARY_FONT_COLOR, color);
    }

    public String getVmaxAdHeaderSecondaryFontColor() {
        return _self.getString(VMAX_AD_HEADER_SECONDARY_FONT_COLOR);
    }

    public void setPrefEmailRequiredForPartners(String emailRequiredForPartners) {
        Assert.assertNotNull(emailRequiredForPartners);
        _self.setString(PREF_EMAIL_REQUIERED_FOR_PARTNERS, emailRequiredForPartners);
    }

    public String getPrefEmailRequiredForPartners() {
        return _self.getString(PREF_EMAIL_REQUIERED_FOR_PARTNERS);
    }

    public void setPrefAutoLoginFailedMessage(String autoLoginFailedMessage) {
        Assert.assertNotNull(autoLoginFailedMessage);
        _self.setString(PREF_AUTO_LOGIN_FAILED_MSG, autoLoginFailedMessage);
    }

    public String getPrefAutoLoginFailedMessage() {
        String message = _self.getString(PREF_AUTO_LOGIN_FAILED_MSG);
        return TextUtils.isEmpty(message) ? myplexAPISDK.getApplicationContext().getString(R.string.manual_login_msg) : message;
    }

    public long getLastVMXAdShownDate() {
        return _self.getLong(PREF_LAST_VMAX_INTERSTITIAL_AD_SHOWN_DAY);
    }

    public void setLastVMXAdShownDate(long value) {
        _self.setLong(PREF_LAST_VMAX_INTERSTITIAL_AD_SHOWN_DAY, value);
    }


    public void setPrefVmaxBannerAdId(String value) {
        _self.setString(PREF_VMAX_BANNER_ADSPOT_ID, value);
    }


    public String getPrefVmaxBannerAdId() {
        return _self.getString(PREF_VMAX_BANNER_ADSPOT_ID);
    }


    public void setPrefVmaxInterStitialAdId(String value) {
        _self.setString(PREF_VMAX_INTERSTITIAL_AD_ID, value);
    }

    public void setPrefVmaxInterStitialOpenAdId(String value) {
        _self.setString(PREF_VMAX_INTERSTITIAL_APP_OPEN_AD_ID, value);
    }

    public void setPrefVmaxInterStitialTabSwitchAdId(String value) {
        _self.setString(PREF_VMAX_INTERSTITIAL_APP_TAB_SWITCH_AD_ID, value);
    }

    public void setPrefVmaxInterStitialExitAdId(String value) {
        _self.setString(PREF_VMAX_INTERSTITIAL_APP_EXIT_AD_ID, value);
    }


    public String getPrefVmaxInterStitialAdId() {
        return _self.getString(PREF_VMAX_INTERSTITIAL_AD_ID);
    }

    public String getPrefVmaxInterStitialOpenAdId() {
        return _self.getString(PREF_VMAX_INTERSTITIAL_APP_OPEN_AD_ID);
    }

    public String getPrefVmaxInterStitialExitAdId() {
        return _self.getString(PREF_VMAX_INTERSTITIAL_APP_EXIT_AD_ID);
    }

    public String getPrefVmaxInterStitialTabSwitchAdId() {
        return _self.getString(PREF_VMAX_INTERSTITIAL_APP_TAB_SWITCH_AD_ID);
    }


    public void setPrefVmaxInStreamAdId(String value) {
        _self.setString(PREF_VMAX_INSTREAM_AD_ID, value);
    }


    public String getPrefVmaxInStreamAdId() {
        return _self.getString(PREF_VMAX_INSTREAM_AD_ID);
    }


    public void setPrefVmaxNativeVideoAdId(String value) {
        _self.setString(PREF_VMAX_NATIVE_VIDEO_AD_ID, value);
    }

    public void setPrefVmaxNativeDisplayAdId(String value) {
        _self.setString(PREF_VMAX_NATIVE_DISPLAY_AD_ID, value);
    }


    public String getPrefVmaxNativeVideoAdId() {
        return _self.getString(PREF_VMAX_NATIVE_VIDEO_AD_ID);
    }

    public void setPrefEnableVmaxInStreamAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_INSTREAM_AD, b);
    }

    public boolean getPrefEnableVmaxInStreamAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_INSTREAM_AD, false);
    }


    public void setPrefEnableVmaxInterStitialAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_INTERSTITIAL_AD, b);
    }

    public void setPrefEnableVmaxInterStitialTabswitchAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_INTERSTITIAL_TABSWITCH_AD, b);
    }

    public void setPrefpublisherGroupIds_Android(String s) {
        Assert.assertNotNull(s);
        _self.setString(PREF_PUBLISHERGROUP_IDS_ANDROID, s);
    }

    public void setPrefEnableVmaxInterStitialAppOpenAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_INTERSTITIAL_APP_OPEN_AD, b);
    }

    public void setPrefEnableVmaxInterStitialAppExitAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_INTERSTITIAL_APP_EXIT_AD, b);
    }

    public boolean getPrefEnableVmaxInterStitialAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_INTERSTITIAL_AD, false);
    }

    public boolean getPrefEnableVmaxInterStitialTabswitchAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_INTERSTITIAL_TABSWITCH_AD, false);
    }

    public String getPrefpublisherGroupIds_Android() {
        String serviceContentConfig = getServiceContentConfig();
        if (serviceContentConfig != null) {
            try {
                JSONObject jsonObj = new JSONObject(serviceContentConfig);
                JSONObject serviceNameObject;
                if(!TextUtils.isEmpty(getServiceName())) {
                    serviceNameObject = jsonObj.getJSONObject(getServiceName());
                }else{
                    serviceNameObject = jsonObj.getJSONObject(getDefaultServiceName());
                }
                String publisherId = serviceNameObject.getString(APIConstants.SERVICE_NAME_PUBLISHER);
                return  publisherId;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*serviceContentConfig.*/
        return _self.getString(PREF_PUBLISHERGROUP_IDS_ANDROID);

    }

    public boolean getPrefEnableVmaxInterStitialAppOpenAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_INTERSTITIAL_APP_OPEN_AD, false);
    }

    public boolean getPrefEnableVmaxInterStitialAppExitAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_INTERSTITIAL_APP_EXIT_AD, false);
    }

    public void setPrefEnableVmaxFooterBannerAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_FOOTER_BANNER_AD, b);
    }

    public boolean getPrefEnableVmaxFooterBannerAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_FOOTER_BANNER_AD, false);
    }

    public void setPrefEnableVmaxPreRollAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_PREROLL_AD, b);
    }

    public boolean getPrefEnableVmaxPreRollAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_PREROLL_AD, false);
    }

    public void setPrefEnableVmaxPostRollAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_POSTROLL_AD, b);
    }

    public boolean getPrefEnableVmaxPostRollAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_POSTROLL_AD, false);
    }


    public void setPrefEnableVmaxMidRollAd(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_VMAX_MIDROLL_AD, b);
    }

    public boolean getPrefEnableVmaxMidRollAd() {
        return _self.getBoolean(PREF_ENABLE_VMAX_MIDROLL_AD, false);
    }

    public void setPrefVmaxVideoAdMinDuration(int vmaxVideoAdMinDuration) {
        Assert.assertNotNull(vmaxVideoAdMinDuration);
        _self.setInt(PREF_VMAX_VIDEO_AD_MIN_DURATION, vmaxVideoAdMinDuration);
    }

    public int getPrefVmaxVideoAdMinDuration() {
        return _self.getInt(PREF_VMAX_VIDEO_AD_MIN_DURATION, 10);
    }

    public void setPrefVmaxFooterBannerAdRefreshRate(int vmaxVideoAdMinDuration) {
        Assert.assertNotNull(vmaxVideoAdMinDuration);
        _self.setInt(PREF_VMAX_FOOTER_BANNER_AD_REFRESH_RATE, vmaxVideoAdMinDuration);
    }

    public int getPrefVmaxFooterBannerAdRefreshRate() {
        return _self.getInt(PREF_VMAX_FOOTER_BANNER_AD_REFRESH_RATE, 30);
    }

    public void setPrefEnableVmaxForPartners(String value) {
        _self.setString(PREF_ENABLE_VMAX_FOR_PARTNERS, value);
    }

    public String getPrefEnableVmaxForPartners() {
        return _self.getString(PREF_ENABLE_VMAX_FOR_PARTNERS);
    }

    public void setPrefVmaxNativeVideoAdRefreshRate(int vmaxNativeVideoAdRefreshRate) {
        Assert.assertNotNull(vmaxNativeVideoAdRefreshRate);
        _self.setInt(PREF_VMAX_NATIVE_VIDEO_AD_REFRESH_RATE, vmaxNativeVideoAdRefreshRate);
    }

    public int getPrefVmaxNativeVideoAdRefreshRate() {
        return _self.getInt(PREF_VMAX_NATIVE_VIDEO_AD_REFRESH_RATE, 15);
    }

    public void setPrefVmaxNativeDisplayAdRefreshRate(int vmaxNativeVideoAdRefreshRate) {
        Assert.assertNotNull(vmaxNativeVideoAdRefreshRate);
        _self.setInt(PREF_VMAX_NATIVE_DISPLAY_AD_REFRESH_RATE, vmaxNativeVideoAdRefreshRate);
    }

    public int getPrefVmaxNativeDisplayAdRefreshRate() {
        return _self.getInt(PREF_VMAX_NATIVE_DISPLAY_AD_REFRESH_RATE, 15);
    }

    public String getPrefPrivacyConsentMessage() {
        return _self.getString(PREF_PRIVACY_CONSENT_MESSAGE);
    }
    public void setPrefPrivacyConsentMessage(String privacyConsentMessage) {
        Assert.assertNotNull(privacyConsentMessage);
        _self.setString(PREF_PRIVACY_CONSENT_MESSAGE, privacyConsentMessage);
    }
    public void setPrefParentalControlOpt(int value) {
        _self.setInt(PREF_PARENTAL_CONTROLL_OPT, value);
    }

    public int getPrefParentalControlOpt() {
        return _self.getInt(PREF_PARENTAL_CONTROLL_OPT, 0);
    }

    public void setPrefParentalControlPIN(int value) {
        _self.setInt(PREF_PARENTAL_CONTROLL_PIN, value);
    }

    public int getPrefParentalControlPIN() {
        return _self.getInt(PREF_PARENTAL_CONTROLL_PIN, -1);
    }

    public void setPrefEnableParentalControl(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_PARENTAL_CONTROL, b);
    }

    public boolean getPrefEnableParentalControl() {
        return _self.getBoolean(PREF_ENABLE_PARENTAL_CONTROL, false);
    }

    public void setVmaxAdBackGroundBorderColor(String color){
        Assert.assertNotNull(color);
        _self.setString(VMAX_LAYOUT_BG_BORDER_COLOR,color);
    }
    public String getVmaxAdBgBorderColor(){
        return _self.getString(VMAX_LAYOUT_BG_BORDER_COLOR);
    }

    public void setPrefEnableBranchIOAnalytics(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_BRANCH_IO_ANALYTICS, b);
    }

    public boolean getPrefEnableBranchIOAnalytics() {
        return _self.getBoolean(PREF_ENABLE_BRANCH_IO_ANALYTICS, false);
    }

    public void setPrefBranchIOEventPriority(int value) {
        _self.setInt(PREF_BRANCH_IO_EVENT_PRIORITY, value);
    }

    public int getPrefBranchIOEEventPriority() {
        return _self.getInt(PREF_BRANCH_IO_EVENT_PRIORITY, 2);
    }


    public void setBranchIOPlayedEventFor30SecFired(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_BRANCH_IO_PLAYED_EVENT_FOR_30_SEC, b);
    }

    public boolean getBranchIOPlayedEventFor30SecFired() {
        return _self.getBoolean(PREF_ENABLE_BRANCH_IO_PLAYED_EVENT_FOR_30_SEC, false);
    }


    public void setPrefBranchIOPlaybackEventSeconds(String appsflyerPlaybackEventSeconds) {
        Assert.assertNotNull(appsflyerPlaybackEventSeconds);
        _self.setString(PREF_BRANCH_IO_PLAYBACK_EVENT_SECONDS, appsflyerPlaybackEventSeconds);
    }


    public String getPrefBranchIOPlaybackEventSeconds() {
        return _self.getString(PREF_BRANCH_IO_PLAYBACK_EVENT_SECONDS);
    }

    public void setEventLoggerUrl(String url) {
        Assert.assertNotNull(url);
        _self.setString(EVENT_LOGGER_URL, url);
    }

    public String getEventLoggerUrl() {
        return _self.getString(EVENT_LOGGER_URL);
    }

    public void setPortraitPlayerSuggestios(String name) {
        Assert.assertNotNull(name);
        _self.setString(PORTRAIT_PLAYER_SUGGESTIONS, name);
    }

    public String getPortraitPlayerSuggestios() {
        return _self.getString(PORTRAIT_PLAYER_SUGGESTIONS);
    }

    public void setSearchResultsGroupName(String name) {
        Assert.assertNotNull(name);
        _self.setString(SEARCH_RESULT_GROUP_NAME, name);
    }

    public String getSearchResultsGroupName() {
        return _self.getString(SEARCH_RESULT_GROUP_NAME);
    }

    public boolean getPrefEventLoggerEnabled() {
        return _self.getBoolean(EVENT_LOGGER_ENABLED, true);
    }

    public void setPrefEventLoggerEnabled(boolean status) {
        Assert.assertNotNull(status);
        _self.setBoolean(EVENT_LOGGER_ENABLED, status);
    }

    public void setPrefVmaxInterStitialAdFrequency(long interstitialAdFrequency) {
        Assert.assertNotNull(interstitialAdFrequency);
        _self.setLong(PREF_VMAX_INTERSTITIAL_AD_FREQUENCY, interstitialAdFrequency);
    }

    public long getPrefVmaxInterStitialAdFrequency() {
        return _self.getLong(PREF_VMAX_INTERSTITIAL_AD_FREQUENCY, DAY_MILLIS);
    }

    public void setPrefSecretKey(SecretKey key){
        Assert.assertNotNull(key);
        byte[] secretKey = key.getEncoded();
        SDKLogger.debug("ENCRYPTION SET MET:::"+String.valueOf(secretKey.length));
        String secKey = Base64.encodeToString(secretKey,Base64.DEFAULT);
        SDKLogger.debug("ENCRYPTION SET MET:::" + secKey);
        _self.setString(PREF_SECRET_KEY, secKey);
    }
    public SecretKey getPrefSecretKey(){
        String encodedKey =  _self.getString(PREF_SECRET_KEY);
        if(encodedKey != null) {
            byte[] byteKey = Base64.decode(encodedKey,Base64.DEFAULT);
            SecretKeySpec key = new SecretKeySpec
                    (byteKey, "AES");
            return key;
        }
        return null;
    }

    public void setAppLaunchCountUpUntill20(int value) {
        Assert.assertNotNull(value);
        _self.setInt(PREF_APP_LAUNCH_COUNT_UP_UNTILL_20, value);
    }

    public int getAppLaunchCountUpUntill20() {
        return _self.getInt(PREF_APP_LAUNCH_COUNT_UP_UNTILL_20, 0);
    }

    public void setPromoAdId(String value) {
        Assert.assertNotNull(value);
        _self.setString(PREF_PROMO_AD_ID, value);
    }

    public String getPromoAdId() {
        return _self.getString(PREF_PROMO_AD_ID);
    }

    public boolean setPartnerDetails(PartnerDetailItem partnerDetails) {
        if(partnerDetails!=null){
            return sharedPreferences.edit().putString(PARTNER_DETAILS,new Gson().toJson(partnerDetails)).commit();
        }
        return false;
    }

    public List<PartnerDetailsResponse> getPartnerDetails(){
        try {
            Type type = new TypeToken<List<PartnerDetailsResponse>>() {
            }.getType();
            Gson gson = new Gson();
            String json=sharedPreferences.getString(PARTNER_DETAILS, "");
            if(!TextUtils.isEmpty(json))
                return gson.fromJson(json, type);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String getPart1() {
        return "w3Ypsr5r";
        /*return "gJN2sr5r";*/
    }

    public void setDidUserRateTheApp (boolean didUserRateTheApp){
        _self.setBoolean(DID_USER_RATE_APP,didUserRateTheApp);
    }
    public boolean didUserRateTheApp(){
        return _self.getBoolean(DID_USER_RATE_APP,false);
    }
    public String getRatingMessage() {
        return _self.getString(RATING_MESSAGE);
    }

    public void setRatingMessage(String ratingMessage) {
        _self.setString(RATING_MESSAGE,ratingMessage);
    }

    public boolean isShowRating() {
        return _self.getBoolean(SHOW_RATING,false);
    }

    public void setWhichElementToBeCompared(int whichElementToBeCompared) {
        _self.setInt(WHICH_ELEMENT_TO_BE_COMPARE,whichElementToBeCompared);
    }
    public int getWhichElementToBeCompared() {
        return _self.getInt(WHICH_ELEMENT_TO_BE_COMPARE,0);
    }


    public void setBaseMOUForAppRating(long mou) {
        _self.setLong(BASE_MOU,mou);
    }
    public long getBaseMOUForAppRating() {
        return _self.getLong(BASE_MOU,0L);
    }

    public boolean isUserNeedToGiveRating(){
        return _self.getBoolean(USER_NEED_TO_GIVE_RATING,true);
    }

    public  void setUserNeedToGiveRating(boolean userRatingGiven){
        _self.setBoolean(USER_NEED_TO_GIVE_RATING,userRatingGiven);
    }

    public void setLastContentPlayed(String lastContentPlayed){
        _self.setString(LAST_CONTENT_PLAYED,lastContentPlayed);
    }

    public String getLastContentPlayed(){
        return _self.getString(LAST_CONTENT_PLAYED);
    }
    public void setLastContentIDPlayed(String lastContentPlayed){
        _self.setString(LAST_CONTENT_ID_PLAYED,lastContentPlayed);
    }

    public String getLastContentIDPlayed(){
        return _self.getString(LAST_CONTENT_ID_PLAYED);
    }
    public void setLastContentSourcePlayed(String lastContentPlayed){
        _self.setString(LAST_CONTENT_SOURCE_PLAYED,lastContentPlayed);
    }

    public String getLastContentSourcePlayed(){
        return _self.getString(LAST_CONTENT_SOURCE_PLAYED);
    }
    public void setLastContentSourceDetailsPlayed(String lastContentPlayed){
        _self.setString(LAST_CONTENT_SOURCE_DETAILS_PLAYED,lastContentPlayed);
    }

    public String getLastContentSourceDetailsPlayed(){
        return _self.getString(LAST_CONTENT_SOURCE_DETAILS_PLAYED);
    }

    public void setTotalMOU(long totalMOU){
        _self.setLong(TOTAL_MOU,totalMOU);
    }

    public long getTotalMOU(){
        return _self.getLong(TOTAL_MOU);
    }

    public void setLastPlayedContentMOU(long lastContentPlayedMOU){
        _self.setLong(LAST_CONTENT_PLAYED_MOU,lastContentPlayedMOU);
    }

    public long getLastPlayedContentMOU(){
        return _self.getLong(LAST_CONTENT_PLAYED_MOU);
    }
    public void setAppLaunchCount(int value) {
        Assert.assertNotNull(value);
        _self.setInt(PREF_APP_LAUNCH_COUNT, value);
    }

    public int getAppLaunchCount() {
        return _self.getInt(PREF_APP_LAUNCH_COUNT, 0);
    }

    public void setPrefIsHooq_sdk_enabled(boolean hooq_sdk_enabled) {
        _self.setBoolean(PrefIsHooq_sdk_enabled, hooq_sdk_enabled);
    }
    public boolean getPrefIsHooq_sdk_enabled() {
        return _self.getBoolean(PrefIsHooq_sdk_enabled, false);
    }

    public void setSubtitle(String name) {
        _self.setString(PrefSubtitleName,name);
    }

    public String getSubtitle(){
        return _self.getString(PrefSubtitleName);
    }

    public boolean isPreferredLanguageFragmentShown() {
        return _self.getBoolean(PREF_IS_PREFERRED_LANGUAGE_SELECTION_FRAGMENTSHOWN, false);
    }

    public void setPreferredLanguageSelectionFragmentshown(boolean isShown) {
        _self.setBoolean(PREF_IS_PREFERRED_LANGUAGE_SELECTION_FRAGMENTSHOWN, true);
        SharedPreferences sp;
    }

    public void setPreferredLanguages(List<PreferredLanguageItem> items) {
        _self.setList(PREF_PREFERRED_LANGUAGES_SELECTED, items, new TypeToken<List<PreferredLanguageItem>>() {
        }.getType());
    }

    private void setList(String prefPreferredLanguagesSelected, List items, Type type) {
        _self.setString(prefPreferredLanguagesSelected, new Gson().toJson(items, type));
    }

    private List getList(String prefPreferredLanguagesSelected, Type type) {
        return (!TextUtils.isEmpty(_self.getString(prefPreferredLanguagesSelected))) ? (List) new Gson().fromJson(_self.getString(prefPreferredLanguagesSelected), type) : null;
    }

    public List<PreferredLanguageItem> getPreferredLanguageItems() {
        return _self.getList(PREF_PREFERRED_LANGUAGES_SELECTED, new TypeToken<List<PreferredLanguageItem>>() {
        }.getType());
    }

    public void clearAllForLogout() {
        boolean isPreferredLanguagesFragmentShown = isPreferredLanguageFragmentShown();
        String contactUs=getContactUsPageURL();
        String faqUrl=getFaq_url();
        String supportPageURL=getSupportPageURL();
        String refundPolicy=getPrefRefundPolicyUrl();
        String aboutUsUrl=getAboutapp_url();
        String termsAndConditionsUrl=getTncUrl();
        String privacyPolicyUrl=getPrivacy_policy_url();
        List<PreferredLanguageItem> preferredLanguageItems = getPreferredLanguageItems();
        _self.removeAll();
        if (preferredLanguageItems != null){
            setPreferredLanguages(preferredLanguageItems);
        }
        setPreferredLanguageSelectionFragmentshown(isPreferredLanguagesFragmentShown);
        if (contactUs!=null){
            setContactUsPageURL(contactUs);
        }
        if(faqUrl!=null){
            setFaq_url(faqUrl);
        }
        if (privacyPolicyUrl!=null){
            setPrivacy_policy_url(privacyPolicyUrl);
        }
        if (aboutUsUrl!=null){
            setAboutapp_url(aboutUsUrl);
        }
        if (refundPolicy!=null){
            setPrefRefundPolicyUrl(refundPolicy);
        }
        if (supportPageURL!=null){
            setSupportPageURL(supportPageURL);
        }
        if (termsAndConditionsUrl!=null){
            setTncUrl(termsAndConditionsUrl);
        }
    }

    private void removeAll() {
        Map<String,?> prefs = null;
        if (sharedPreferences != null) {
            prefs = sharedPreferences.getAll();
        }else{
            sharedPreferences = myplexAPISDK.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        }
        if (prefs != null) {
            for(Map.Entry<String,?> prefToReset : prefs.entrySet()){
                if(!isAKeyToNotEdit(prefToReset.getKey()))
                    sharedPreferences.edit().remove(prefToReset.getKey()).apply();
            }
        }
    }

    private boolean isAKeyToNotEdit(String key) {

        for (String aKeysToLeaveAlone : keysToLeaveAlone) {
            if (aKeysToLeaveAlone.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    public void setEnablePrefLangScreen(boolean enabled) {
        _self.setBoolean(APIConstants.ENABLE_PREF_LANG_SCREEN, enabled);
    }

    public boolean isPrefLangScreenEnabled() {
        return _self.getBoolean(APIConstants.ENABLE_PREF_LANG_SCREEN, false);
    }

    public void setWatchlistItems(List<CardData> items,String tabName) {
        String KEY = PREF_WATCHLIST_ITEMS+tabName;
        _self.setList(KEY, items, new TypeToken<List<CardData>>() {
        }.getType());
    }

    public List<CardData> getWatchListItems(String tabName) {
        return _self.getList(PREF_WATCHLIST_ITEMS+tabName, new TypeToken<List<CardData>>() {
        }.getType());
    }


    public void shouldChangeFavouriteState(final String cardDataID,final boolean isFavourite,final String ...tabName){

        Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (String name: tabName) {
                    List<CardData> watchListItems = getWatchListItems(name);
                    if(watchListItems == null || watchListItems.size() <= 0){
                      continue;
                    }
                    for (int i = 0;i<watchListItems.size();i++) {
                        CardData cardData = watchListItems.get(i);
                        if (!TextUtils.isEmpty(cardData._id) && cardData._id.equalsIgnoreCase(cardDataID)) {
                            cardData.isFavourite = isFavourite;
                            watchListItems.set(i,cardData);
                            Log.e("MOVIE STATUS","Movie name : "+cardData.generalInfo.title+" fav Status :"+ cardData.isFavourite);
                            setWatchlistItems(watchListItems,name);
                            return;
                        }
                    }
                }
            }
        };
        handler.post(run);
//        Thread thread = new Thread(){
//            @Override
//            public void run() {
//                super.run();
//
//            }
//        };
//        thread.start();

    }



//    public void addToWatchList(CardData cardData) {
//        if(cardData==null||TextUtils.isEmpty(cardData._id))return;
//        List<CardData> watchListItems = getWatchListItems();
//        if (watchListItems == null || watchListItems.size() <= 0) {
//            watchListItems = new ArrayList<CardData>();
//            cardData.isFavourite = true;
//            watchListItems.add(cardData);
//            setWatchlistItems(watchListItems);
//            return;
//        }
//        boolean found=false;
//        int index=0;
//        if (watchListItems != null && watchListItems.size() > 0) {
//            for (CardData item : watchListItems) {
//                if (!TextUtils.isEmpty(item._id) && item._id.equalsIgnoreCase(cardData._id)) {
//                    found = true;
//                    cardData.isFavourite = true;
//                    watchListItems.set(index,cardData);
//                }
//                index++;
//            }
//            if(!found)
//                watchListItems.add(cardData);
//        }
//        setWatchlistItems(watchListItems);
//    }
//
//    public void removeFromWatchList(CardData cardData) {
//        if(cardData==null||TextUtils.isEmpty(cardData._id))return;
//        List<CardData> watchListItems = getWatchListItems();
//        if (watchListItems == null || watchListItems.size() <= 0) {
//
//            return;
//        }
//        int index=0;
//        if (watchListItems != null && watchListItems.size() > 0) {
//            try {
//            CardData item;
//            for (int i=0; i < watchListItems.size();i++) {
//                item = watchListItems.get(i);
//                if(item == null)
//                    continue;
//                if (!TextUtils.isEmpty(item._id) && item._id.equalsIgnoreCase(cardData._id)) {
//                    watchListItems.get(i).isFavourite = false;
//                }
//                index++;
//            }
//
//            }catch (Exception e){
//
//            }
//
//        }
//        setWatchlistItems(watchListItems);
//    }

    public void setWatchlistItemsFromServer(boolean b) {
         _self.setBoolean(APIConstants.IS_RESPONSE_FROM_SERVER, b);
    }
    public boolean getWatchlistItemsFromServer() {
        return _self.getBoolean(APIConstants.IS_RESPONSE_FROM_SERVER, true);
    }
    public void setAppLanguageToShow(String s) {
        _self.setString(APIConstants.APP_LANGAUGE,s);
    }
    public String getAppLanguageToShow() {
        return _self.getString(APIConstants.APP_LANGAUGE);
    }

    public void saveAppLanguage(int i) {
        _self.setInt(APIConstants.APP_LANGAUGE_SELECTED,i);
    }

    public int getAppLanguage() {
        return _self.getInt(APIConstants.APP_LANGAUGE_SELECTED,-1);
    }

   /* public String getAppLanguageToSendServer() {
        String  language =APIConstants.DEFAULT_SELECTED_APP_LANGUAGE;
        if(PrefUtils.getInstance().getAppLanguageToShow() != null && !PrefUtils.getInstance().getAppLanguageToShow().isEmpty() ){
            String[] splitlanguage =  PrefUtils.getInstance().getAppLanguageToShow().split(",");
            if(splitlanguage != null && splitlanguage.length>0){
                language = splitlanguage[getAppLanguage()];
            }

        }
        //String  language = PrefUtils.getInstance().getAppLanguageToShow().split(",")[getAppLanguage()];
        return language;
    }*/

    public void setAppLanguageToSendServer(String s) {
        _self.setString(APIConstants.APP_LANGAUGE_STRING,s);
    }
    public void setAppLanguageFirstTime(String s) {
        _self.setString(APIConstants.APP_FIRST_LANGAUGE_STRING,s);
    }

    public String getAppLanguageToSendServer() {
        return  _self.getString(APIConstants.APP_LANGAUGE_STRING);
    }
    public String getAppLanguageFirstTime() {
        return  _self.getString(APIConstants.APP_FIRST_LANGAUGE_STRING);
    }

    public String getAppLanguageToSendServerInStringFormat() {
        String  languageSelected =APIConstants.DEFAULT_SELECTED_APP_LANGUAGE;
        String langTemp = _self.getString(APIConstants.APP_LANGAUGE_STRING);
        if( langTemp != null && !langTemp.isEmpty()){
            languageSelected = langTemp;
        }
        return  languageSelected;
    }

    public  String getAppLanguageURL(){
        String appURl =  _self.getString(APIConstants.APP_LANGAUGE_URL);
        return appURl;
    }

    public  void setAppLanguageURL(String appURL){
        _self.setString(APIConstants.APP_LANGAUGE_URL,appURL);

    }

    public  String getVernacularLanguageURL(){
        String appURl =  _self.getString(APIConstants.VERNACULAR_LANGAUGE_URL);
        return appURl;
    }

    public  void setVernacularLanguageURL(String vernacularURL){
        _self.setString(APIConstants.VERNACULAR_LANGAUGE_URL,vernacularURL);

    }

    public void setVernacularLanguage(boolean vernacular) {
        _self.setBoolean(APIConstants.VERNACULAR_LANGUAGE,vernacular);
    }

    public boolean getVernacularLanguage() {
        return _self.getBoolean(APIConstants.VERNACULAR_LANGUAGE,false);
    }

    public void setReferralSDKPresent(boolean isReferral) {
        _self.setBoolean(APIConstants.IS_REFERRAL_SDK_PRESENT,isReferral);
    }

    public boolean isReferralSDKPresent() {
        return _self.getBoolean(APIConstants.IS_REFERRAL_SDK_PRESENT,false);
    }

    public String getFullScreenRestrictedContentTypes() {
        return _self.getString(APIConstants.FULL_SCREEN_RESTRICTION_CONTENT_TYPE);
    }
    public void setFullScreenRestrictedContentTypes(String restrictedContentType) {
         _self.setString(APIConstants.FULL_SCREEN_RESTRICTION_CONTENT_TYPE,restrictedContentType);
    }

    public String getFullScreenRestrictedContentTypesPublishingHouseIds() {
        String serviceContentConfig = getServiceContentConfig();
        if (serviceContentConfig != null) {
            try {
                JSONObject jsonObj = new JSONObject(serviceContentConfig);
                JSONObject serviceNameObject ;
                if(!TextUtils.isEmpty(getServiceName())) {
                    serviceNameObject = jsonObj.getJSONObject(getServiceName());
                }else{
                    serviceNameObject = jsonObj.getJSONObject(getDefaultServiceName());
                }
                String publisherId = serviceNameObject.getString(APIConstants.FULL_SCREEN_RESTRICTION_KEY);
                return  publisherId;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return _self.getString(APIConstants.FULL_SCREEN_RESTRICTION_CONTENT_TYPE_PUBLISHINGHOUSE_ID);
    }
    public void setFullScreenRestrictedContentTypesPublishingHouseIds(String restrictedContentTypeID) {
        _self.setString(APIConstants.FULL_SCREEN_RESTRICTION_CONTENT_TYPE_PUBLISHINGHOUSE_ID,restrictedContentTypeID);
    }

    public void setForceAcceptLanguage(String forceAcceptLangauge) {
        _self.setString(APIConstants.FORCE_ACCEPT_LANGUAGE,forceAcceptLangauge);
    }


    public String getForceAcceptLanguage() {
       return _self.getString(APIConstants.FORCE_ACCEPT_LANGUAGE);
    }
    public void setMediaSessionToken(String mediaSessionToken) {
        _self.setString(APIConstants.MEDIASESSIONTOKEN,mediaSessionToken);
    }
    public String getMediaSessionToken() {
       return _self.getString(APIConstants.MEDIASESSIONTOKEN);
    }
    public void setSeekBarEnable(Boolean seekBarEnable) {
        _self.setBoolean(APIConstants.SEEKENABLED,seekBarEnable);

    }
    public boolean getSeekBarEnable() {
        return _self.getBoolean(APIConstants.SEEKENABLED,true);
    }
    public void setUpdatesStatusIntervalInSec(int UPDATESSTATUSINTERVALINSEC) {
        Assert.assertNotNull(UPDATESSTATUSINTERVALINSEC);
        _self.setInt(APIConstants.UPDATESSTATUSINTERVALINSEC, UPDATESSTATUSINTERVALINSEC);
    }

    public int getUpdatesStatusIntervalInSec() {
        return _self.getInt(APIConstants.UPDATESSTATUSINTERVALINSEC, -1);
    }

    public List<CommonParams> getCommonParamsData() {
        return _self.getList(COMMONPARAMSRESPONSE, new TypeToken<List<CommonParams>>() {
        }.getType());
    }

    public void setCommonParamsData(List<CommonParams> commonParamsList) {
        _self.setList(COMMONPARAMSRESPONSE, commonParamsList, new TypeToken<List<CommonParams>>() {
        }.getType());

    }
    public String getContentVideoQuality() {
        return _self.getString(PREF_CONTENT_VIDEO_QUALITY);
    }

    public void setContentVideoQuality(String contentVideoQuality) {
        Assert.assertNotNull(contentVideoQuality);
        _self.setString(PREF_CONTENT_VIDEO_QUALITY, contentVideoQuality);
    }

    public void setQUALITY_MAP(HashMap<String,Integer> quality_map ) {
        Gson gson = new Gson();
        String json = gson.toJson(quality_map);
        PrefUtils.getInstance().setString(SETQUALITYMAP,json);

    }
    public HashMap<String,Integer> getQUALITY_MAP() {
        Gson gson = new Gson();
        String json = PrefUtils.getInstance().getString(SETQUALITYMAP);
        if(json == null){
            return null;
        }
        java.lang.reflect.Type type = new TypeToken<HashMap<String,Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void setplayerControlsBitratesAuto(String playerControlAuto) {
        _self.setString(AUTO, playerControlAuto);
    }
    public void setplayerControlsBitrates(String playerControls) {
        _self.setString(BITRATES, playerControls);
    }

    public String getplayerControlsBitratesAuto() {
        return _self.getString(AUTO);
    }

    public String getplayerControlsBitrates() {
        return _self.getString(BITRATES);
    }


    public void setplayerControlsBitratesLow(String playerControlLow) {
        _self.setString(LOW, playerControlLow);
    }

    public String getplayerControlsBitratesLow() {
        return _self.getString(LOW);
    }

    public void setplayerControlsBitratesMedium(String playerControlMedium) {
        _self.setString(MEDIUM, playerControlMedium);
    }

    public String getplayerControlsBitratesMedium() {
        return _self.getString(MEDIUM);
    }

    public void setplayerControlsBitratesHigh(String playerControlHigh) {
        _self.setString(HIGH, playerControlHigh);
    }

    public String getplayerControlsBitratesHigh() {
        return _self.getString(HIGH);
    }

    public void setplayerControlsDefalut(String playerControlDefalut) {
        _self.setString(PLAYERCONTROL, playerControlDefalut);
    }

    public String getsetplayerControlsDefalut() {
        return _self.getString(PLAYERCONTROL);
    }

    public void setAppAcionRedirect(String app_action_redirect) {
        _self.setString(APIConstants.APPActionRedirect,app_action_redirect);
    }
    public String getAppAcionRedirect() {
        return _self.getString(APIConstants.APPActionRedirect);
    }

    public void setPlayerWatermarkURL(String PlayerWatermarkURL) {
        _self.setString(PREF_PLAYERWATERMARTKURL, PlayerWatermarkURL);
    }

    public String getPlayerWatermarkURL() {
        return _self.getString(PREF_PLAYERWATERMARTKURL);

    }
    public void setShowWaterMark(Boolean showWaterMarkEnable) {
        _self.setBoolean(APIConstants.SHOWWATERMARKENABLED,showWaterMarkEnable);
    }
    public boolean getShowWaterMark() {
        return _self.getBoolean(APIConstants.SHOWWATERMARKENABLED,true);
    }
    public String getLastSearchQuery() {

         return _self.getString(APIConstants.LAST_SEARCH_QUERY);

    }
    public void setLastSearchQuery(String lastSearchQuery) {

         _self.setString(APIConstants.LAST_SEARCH_QUERY,lastSearchQuery);


    }

    public int getRecentSearchCountLimit() {
        int count =  _self.getInt(PREF_COUNT_RECENT_SEARCH, 0);
        if(count>0){
            return count;
        }
        return 5;
    }
    public void setRecentSearchCountLimit(int count) {
        _self.setInt(PREF_COUNT_RECENT_SEARCH, count);

    }

    public void setSupportPageURL(String supportpage_url) {
        _self.setString(PREF_SUNDIRECT_SUPPORT_URL, supportpage_url);
    }


    public String getContactUsPageURL() {
        return _self.getString(PREF_SHREYAS_CONTACT_US_URL);
    }

    public void setContactUsPageURL(String contactUsPageURL) {
        _self.setString(PREF_SHREYAS_CONTACT_US_URL, contactUsPageURL);
    }


    public String getSupportPageURL() {
        return _self.getString(PREF_SUNDIRECT_SUPPORT_URL);
    }

    public String getPrefRefundPolicyUrl() {
        return _self.getString(PREF_REFUND_POLICY_URL);
    }

    public void setPrefRefundPolicyUrl(String refundPolicyUrl) {
        _self.setString(PREF_REFUND_POLICY_URL, refundPolicyUrl);
    }

    public void setSearchErrorMessage(String search_error) {
        _self.setString(PREF_SEARCH_ERROR_MESSAGE, search_error);
    }
    public String getSearchErrorMessage() {
        String error = _self.getString(PREF_SEARCH_ERROR_MESSAGE);
        if(TextUtils.isEmpty(error)){
            return "Couldn't find what you were looking for?\n\nHow about some suggestions from the trending searches?";
        }else{
            return error;
        }
    }

    public void savePreviewHashMap(ConcurrentHashMap<String,Integer> previewVideoCountMap) {
        Gson gson = new Gson();
        String json = gson.toJson(previewVideoCountMap);
        PrefUtils.getInstance().setString(PREVIEW_VIDEO_PLAY_COUNT,json);
    }


    public ConcurrentHashMap<String,Integer> getPreviewPlayData() {
        Gson gson = new Gson();
        String json = PrefUtils.getInstance().getString(PREVIEW_VIDEO_PLAY_COUNT);
        if(json == null){
            return null;
        }
        java.lang.reflect.Type type = new TypeToken<ConcurrentHashMap<String,Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public String getSignInFlowFlag(){
       return  _self.getString(PREF_SIGN_IN_FLOW);
    }

    public void setSignInFlowFlag(String signinFlow) {
        if (signinFlow != null) {
            _self.setString(PREF_SIGN_IN_FLOW, signinFlow);
        }
    }

    public void setServiceName(String serviceName) {
        _self.setString(PREF_SERVICE_NAME, serviceName);

    }

    public String getServiceName() {
        return _self.getString(PREF_SERVICE_NAME);

    }
    public void setDefaultServiceName(String serviceName) {
        _self.setString(PREF_DEFAULT_SERVICE_NAME, serviceName);

    }

    public String getDefaultServiceName() {
        return _self.getString(PREF_DEFAULT_SERVICE_NAME);

    }

    public void saveServiceContentConfig(String serviceContentConfig) {
        _self.setString(PREF_SERVICE_CONTENT_CONFIG, serviceContentConfig);

    }

    public String getServiceContentConfig() {
        return _self.getString(PREF_SERVICE_CONTENT_CONFIG);

    }

    public void setTncUrl(String serviceName) {
        _self.setString(PREF_TNC_URL, serviceName);

    }

    public String getTncUrl() {
        return _self.getString(PREF_TNC_URL);

    }

    public void setSubscribedString(String serviceName) {
        _self.setString(PREF_SUBSCRIBED, serviceName);

    }

    public String getSubscribedString() {
        String subsribed = _self.getString(PREF_SUBSCRIBED);
        if(TextUtils.isEmpty(subsribed)){
            subsribed = "Subscribed";
        }
        return subsribed;

    }
    public void setSubscriptionStatusString(String serviceName) {
        _self.setString(SUBSCRIPTION_STATUS, serviceName);

    }
    public String getSubscriptionStatusString() {
        return _self.getString(SUBSCRIPTION_STATUS);

    }

    public void setBuyString(String serviceName) {
        _self.setString(PREF_BUY, serviceName);

    }

    public String getBuyString() {
        String buy = _self.getString(PREF_BUY);
        if(TextUtils.isEmpty(buy)){
            buy = "Buy";
        }
        return buy;

    }
    public void setSupportUrl(String serviceName) {
        _self.setString(PREF_SUPPORT_URL, serviceName);

    }

    public String getSupportUrl() {
        return _self.getString(PREF_SUPPORT_URL);

    }
    public void setPrivacy_policy_url(String serviceName) {
        _self.setString(PREF_PRIVACY_POLICY_URL, serviceName);

    }

    public String getPrivacy_policy_url() {
        return _self.getString(PREF_PRIVACY_POLICY_URL);

    }
    public void setHelpUrl(String serviceName) {
        _self.setString(PREF_HELP_URL, serviceName);

    }

    public String getHelpUrl() {
        return _self.getString(PREF_HELP_URL);

    }
    public void setAboutapp_url(String serviceName) {
        _self.setString(PREF_ABOUT_APP_URL, serviceName);

    }

    public String getAboutapp_url() {
        return _self.getString(PREF_ABOUT_APP_URL);

    }

    public boolean isVideoPlayedForFirstTime() {
        return _self.getBoolean(PREF_VIDEO_PLAYED_FIRST_TIME,false);
    }
    public void setVideoPlayedForFirstTime(boolean isPlayed) {
         _self.setBoolean(PREF_VIDEO_PLAYED_FIRST_TIME,isPlayed);
    }

    public void setFaq_url(String faq_url) {
        _self.setString(PREF_FAQ_URL, faq_url);
    }
    public String getFaq_url() {
        return _self.getString(PREF_FAQ_URL);
    }

    public String getUserAgeRange() {
        if (_self.getString(USER_AGE_RANGE) == null || _self.getString(USER_AGE_RANGE).isEmpty()) {
            return LOCAL_USER_AGE_RANGE;
        } else {
            return _self.getString(USER_AGE_RANGE);
        }
    }

    public void setUserAgeRange(String userAgeRange) {
        _self.setString(USER_AGE_RANGE, userAgeRange);
    }

    public String getUserGenderRange() {
        if (_self.getString(USER_GENDER_RANGE) == null || _self.getString(USER_GENDER_RANGE).isEmpty()) {
            return LOCAL_USER_GENDER_RANGE;
        } else {
            return _self.getString(USER_GENDER_RANGE);
        }
    }

    public void setUserGenderRange(String userAgeRange) {
        _self.setString(USER_GENDER_RANGE, userAgeRange);
    }

    public void setAccessToken(String access_token) {
        _self.setString(PREF_ACCESS_TOKEN,access_token);
    }

    public String getAccessToken(String access_token) {
      return  _self.getString(PREF_ACCESS_TOKEN);
    }

    public void setLegacyUpgradePopup(boolean legacyUpgradePopup) {
        _self.setBoolean(APIConstants.LEGACY_UPGRADE_POPUP,legacyUpgradePopup);
    }

    public boolean getLegacyUpgradePopup() {
        return _self.getBoolean(APIConstants.LEGACY_UPGRADE_POPUP,false);
    }

    public void setAppUrlRedirectionUrl(String appUrlRedirectionUrl) {
        _self.setString(PREF_APP_REDIRECT_URL,appUrlRedirectionUrl);
    }

    public String getAppUrlRedirectionUrl() {
        return  _self.getString(PREF_APP_REDIRECT_URL);
    }

    private int ArtistProfileMoviesListCount = 50;

    public int getArtistProfileMoviesListCount() {
        return ArtistProfileMoviesListCount;
    }

    public void setArtistProfileMoviesListCount(int artistProfileMoviesListCount) {
        ArtistProfileMoviesListCount = artistProfileMoviesListCount;
    }


    public int getArtistProfileComedyClipsListCount() {
        return ArtistProfileComedyClipsListCount;
    }

    public void setArtistProfileComedyClipsListCount(int artistProfileComedyClipsListCount) {
        ArtistProfileComedyClipsListCount = artistProfileComedyClipsListCount;
    }

    private int ArtistProfileComedyClipsListCount = 50;

    public void setAdmobUnitId(String adId) {
        _self.setString(PREF_ADMOB_UNIT_ID,adId);
    }

    public String getAdmobUnitId() {
        return  _self.getString(PREF_ADMOB_UNIT_ID);
    }

    public void setAdMobEnabled(boolean status) {
        _self.setBoolean(SHOULD_ENABLE_GOOGLE_ADS,status);
    }

    public boolean isAdEnabled() {
        return _self.getBoolean(SHOULD_ENABLE_GOOGLE_ADS,false);
    }

    public void setSubtitlesEnabled(boolean value) {
        _self.setBoolean(ARE_SUBTITLES_ENABLED,value);
    }
    public boolean getSubtitlesEnabled(){
        return _self.getBoolean(ARE_SUBTITLES_ENABLED,false);
    }
    public void setEditMobileNumberEnabled(boolean value) {
        _self.setBoolean(IS_EDIT_MOBILE_NUMBER_ENABLED,value);
    }
    public boolean getEditMobileNumberEnabled(){
        return _self.getBoolean(IS_EDIT_MOBILE_NUMBER_ENABLED,false);
    }

    public void setIsEnabledVideoAnalytics(boolean value) {
        _self.setBoolean(IS_ENABLED_VIDEO_ANALYTICS,value);
    }
    public boolean getIsEnabledVideoAnalytics(){
        return _self.getBoolean(IS_ENABLED_VIDEO_ANALYTICS,false);
    }
    public void setPrefOTPExpiryTimeOut(long value) {
        Assert.assertNotNull(value);
        _self.setLong(PREF_OTP_EXPIRY_TIME_OUT, value);
    }

    public long getPrefOTPExpiryTimeOut() {
        return _self.getLong(PREF_OTP_EXPIRY_TIME_OUT, 100000);
    }

    public String getBufferConfigAndroid() {
        String bufferConfigAndroid = _self.getString(BUFFER_CONFIG_ANDROID);
        if (TextUtils.isEmpty(bufferConfigAndroid)) {
            return APIConstants.BUFFER_CONFIG_ANDROID;
        }
        return bufferConfigAndroid;
    }

    public void setBufferConfigAndroid(String bufferConfigAndroid) {
        if (!TextUtils.isEmpty(bufferConfigAndroid)) {
            _self.setString(BUFFER_CONFIG_ANDROID, bufferConfigAndroid);
        }
    }

    public  String getBannerPreviewPlaybackEnabled(){
        return _self.getString(APIConstants.BANNER_PREVIEW_PLAYBACK_ENABLED);
    }

    public  void setBannerPreviewPlaybackEnabled(String bannerPreviewPlaybackEnabled){
        _self.setString(APIConstants.BANNER_PREVIEW_PLAYBACK_ENABLED,bannerPreviewPlaybackEnabled);
    }

    public  String getThumbnailPreviewPlaybackEnabled(){
        return _self.getString(APIConstants.THUMBNAIL_PREVIEW_PLAYBACK_ENABLED);
    }

    public  void setThumbnailPreviewPlaybackEnabled(String thumbnailPreviewPlaybackEnabled){
        _self.setString(APIConstants.THUMBNAIL_PREVIEW_PLAYBACK_ENABLED,thumbnailPreviewPlaybackEnabled);
    }

    public void setMotionVideoPartition(String motionVideoPartition) {
        _self.setString(PREF_MOTIONVIDEO_PARTITION,motionVideoPartition);
    }
    public String getMotionVideoPartition() {
        return _self.getString(PREF_MOTIONVIDEO_PARTITION);
    }

    public  String getBlockAutoplayContentPartners(){
        return _self.getString(APIConstants.BLOCK_AUTOPLAY_CP);
    }

    public  void setBlockAutoplayContentPartners(String blockAutoplayCP){
        _self.setString(APIConstants.BLOCK_AUTOPLAY_CP,blockAutoplayCP);
    }

    public void setPrefHungamaPlayBackQuality(String status) {
        Assert.assertNotNull(status);
        _self.setString(PREF_HUNGAMA_NETWORK_TYPE, status);
    }

    public String getPrefHungamaPlayBackQuality() {
        return _self.getString(PREF_HUNGAMA_NETWORK_TYPE);
    }

    public void setPrefEnableAutoplayNonLoggedinUser(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_ENABLE_AUTOPLAY_NONLOGGEDIN_USER, b);
    }

    public boolean getPrefEnableAutoplayNonLoggedinUser() {
        return _self.getBoolean(PREF_ENABLE_AUTOPLAY_NONLOGGEDIN_USER, false);
    }

    public void setIsToShowForm(boolean b) {
        Assert.assertNotNull(b);
        _self.setBoolean(PREF_IS_TO_SHOW_FORM, b);
    }

    public boolean getIsToShowForm() {
        return _self.getBoolean(PREF_IS_TO_SHOW_FORM, false);
    }

    public void setPackageQuality(String quality) {
        _self.setString(PACKAGE_QUALITY,quality);
    }

    public String getPackageQuality(){
        String quality = _self.getString(PACKAGE_QUALITY);
        String defaultPackageQuality = "0-4500";
        if(!TextUtils.isEmpty(quality))
            return quality;
        else
            return defaultPackageQuality;
    }

    public int getMaximumInterstrialAdClicksToShow(){
        return _self.getInt(MAX_INTERSTRITAIL_AD_CLICKS, 10);
    }

    public void setMaximumInterstrialAdClicksToShow(int value) {
        _self.setInt(MAX_INTERSTRITAIL_AD_CLICKS, value);
    }

    public void setInterstrialAdClicks(int value) {
        _self.setInt(INTERSTRIAL_AD_CLICKS, value);
    }

    public int getInterstrialAdClicks() {
        return _self.getInt(INTERSTRIAL_AD_CLICKS, 0);
    }


    public void setInterstrialAdUnitId(String value) {
        _self.setString(PREF_INTERSTITIAL_AD, value);
    }

    public String getInterstrialAdUnitId() {
        return _self.getString(PREF_INTERSTITIAL_AD);
    }

    public void setPrefBannerBelowCoverPoster(String value) {
        _self.setString(PREF_BANNER_BELOW_COVER_POSTER, value);
    }

    public String getPrefBannerBelowCoverPoster() {
        return _self.getString(PREF_BANNER_BELOW_COVER_POSTER);
    }


    public String getMaturityTimer() {
        return _self.getString(MATURITY_TIMER);
    }

    public void setMaturityTimer(String maturityTimer) {
        _self.setString(MATURITY_TIMER, maturityTimer);
    }

    public void isEUUserEnabled(boolean userConsentStatus) {
        _self.setBoolean(EU_USER_ENABLED, userConsentStatus);
    }

    public boolean getIsEUUserEnabled() {
        return _self.getBoolean(EU_USER_ENABLED, false);
    }

    public void didShowEUPopUpEnabled(boolean userConsentStatus) {
        _self.setBoolean(USER_CONSENT, userConsentStatus);
    }

    public boolean getdidShowEUPopUpEnabled() {
        return _self.getBoolean(USER_CONSENT, false);
    }

    public void setUserConsentToken(String token) {
        _self.setString(USER_CONSENT_TOKEN, token);
    }

    public String getUserConsentToken() {
        return _self.getString(USER_CONSENT_TOKEN);
    }

    public void setIsLightThemeEnabled(boolean userConsentStatus) {
        _self.setBoolean(IS_LIGHT_THEME_ENABLED, userConsentStatus);
    }

    public boolean getIsLightThemeEnabled() {
        return _self.getBoolean(IS_LIGHT_THEME_ENABLED, false);
    }

    public void setAdVideoCount(int value) {
        _self.setInt(AD_WATCH_VIDEO_COUNT, value);
    }

    public int getAdVideoCount() {
        return _self.getInt(AD_WATCH_VIDEO_COUNT, 1);
    }

    public void setUSerCountry(String value) {
        _self.setString(USER_COUNTRY, value);
    }

    public String getUserCountry() {
        return _self.getString(USER_COUNTRY);
    }

    public void setSubscribedLanguage(List<String>values) {
        String KEY=USER_SUBSCRIBED_LANGUAGE;
        _self.setList(KEY, values,new TypeToken<List<String>>() {
        }.getType());
    }
    public List<String> getSubscribedLanguage() {
        return _self.getList(USER_SUBSCRIBED_LANGUAGE, new TypeToken<List<String>>() {
        }.getType());
    }
    public void setPackages(List<CardDataPackages>values) {
        String KEY=USER_PACKAGES;
        _self.setList(KEY, values,new TypeToken<List<CardDataPackages>>() {
        }.getType());
    }
    public List<CardDataPackages> getPackages() {
        return _self.getList(USER_PACKAGES, new TypeToken<List<CardDataPackages>>() {
        }.getType());
    }
    public void setUserState(String value) {
        _self.setString(USER_STATE, value);
    }

    public String getUserState() {
        return _self.getString(USER_STATE);
    }

    public void setUserCity(String value) {
        _self.setString(USER_CITY, value);
    }

    public String getUSerCity() {
        return _self.getString(USER_CITY);
    }

    public void setIsShowUpdateProfile(boolean isShow) {
        _self.setBoolean(IS_SHOW_UPDATE_PROFILE, isShow);
    }

    public boolean getIsShowUpdateProfile() {
        return _self.getBoolean(IS_SHOW_UPDATE_PROFILE,true);
    }
    public void setAppDeepLinkUrl(String value) {
        _self.setString(APP_SHARE_DEEP_LINK_URL, value);
    }

    public String getAppDeepLinkUrl() {
        return _self.getString(APP_SHARE_DEEP_LINK_URL);
    }

    public void setAppSharePwaurl(String value) {
        _self.setString(APP_SHARE_PWA_URL, value);
    }

    public String getAppSharePwaUrl() {
        return _self.getString(APP_SHARE_PWA_URL);
    }

    public void setOTPLength(String value) {
        _self.setString(OTP_lenght, value);
    }
    public String getOTPLength() {
        return _self.getString(OTP_lenght);
    }

    public String getExploreOffers() {
        return _self.getString(IS_explore_offers);
    }

    public void setExploreOffers(String value) {
        _self.setString(IS_explore_offers, value);
    }
    public String getSubscribeToApps() {
        return _self.getString(IS_SUBSCRIBE_TO_APPS);
    }

    public void setSubscribeToApps(String value) {
        _self.setString(IS_SUBSCRIBE_TO_APPS, value);
    }
    public void setShowPreviewStatus(boolean isShow) {
        _self.setBoolean(SHOW_PREVIEW_STATUS, isShow);
    }

    public boolean getShowPreviewStatus() {
        return _self.getBoolean(SHOW_PREVIEW_STATUS,false);
    }

    public void setIsEnableSubScribeToApps(String enable_NewUser_SubscribeToApps) {
        _self.setString(ENABLE_SUBSCRIBE_TO_APPS, enable_NewUser_SubscribeToApps);
    }
    public String getIsEnableSubScribeToApps() {
        return _self.getString(ENABLE_SUBSCRIBE_TO_APPS);
    }

    public void setIsShowThumbnailPreview(boolean isShow) {
        _self.setBoolean(IS_SHOW_THUMBNAIL_PREVIEW_SEEKBAR, isShow);
    }

    public boolean getIsShowThumbnailPreview() {
        return _self.getBoolean(IS_SHOW_THUMBNAIL_PREVIEW_SEEKBAR,true);
    }
    public void setPendingSMC(List<String>values) {
        String KEY=PENDING_SMC_NUMBER;
        _self.setList(KEY, values,new TypeToken<List<String>>() {
        }.getType());
    }
    public List<String> getPendingSMC() {
        return _self.getList(PENDING_SMC_NUMBER, new TypeToken<List<String>>() {
        }.getType());
    }

    public void setIsShowNotificationOption(boolean isShow) {
        _self.setBoolean(IS_SHOW_NOTIFICATION_OPTION, isShow);
    }
    public boolean getIsShowNotificationOption() {
        return _self.getBoolean(IS_SHOW_NOTIFICATION_OPTION, false);
    }

}
