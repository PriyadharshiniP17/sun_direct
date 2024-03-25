package com.myplex.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.AvailableLoginsProperties;
import com.myplex.api.request.content.PropertiesRequest;
import com.myplex.api.request.content.VernacularRequest;
import com.myplex.model.AdFullScreenListResponse;
import com.myplex.model.AdPopUpNotificationListResponse;
import com.myplex.model.AvailableLoginsPropertiesData;
import com.myplex.model.BitrateCapForPlayer;
import com.myplex.model.CategoryScreenFilters;
import com.myplex.model.ContentRatingConfig;
import com.myplex.model.LoginProperties;
import com.myplex.model.MobileBitrateCapVod;
import com.myplex.model.PartnerDetailsAppInAppResponse;
import com.myplex.model.PartnerDetailsResponse;
import com.myplex.model.PlayerControlsBitrates;
import com.myplex.model.PreviewProperties;
import com.myplex.model.PromoAdData;
import com.myplex.model.PropertiesData;
import com.myplex.model.RatingScreen;
import com.myplex.model.SearchConfigResponse;
import com.myplex.model.SetupBoxListResponse;
import com.myplex.model.VernacularResponse;
import com.myplex.model.VersionData;
import com.myplex.sdk.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by phani on 2/3/2016.
 */
public class PropertiesHandler {
    /*LOW
     MEDIUM
    HIGH*/
    private static final String PROPERTY_MIXPANEL_EVENTS_THRESHOLD_LOW = "LOW";
    private static final String PROPERTY_MIXPANEL_EVENTS_THRESHOLD_MED = "MEDIUM";
    private static final String PROPERTY_MIXPANEL_EVENTS_THRESHOLD_HIGH = "HIGH";
    private static final String TAG = PropertiesHandler.class.getSimpleName();
    private static LoginProperties loginProperties;
    private static PromoAdData sPromoAdData;
    private static PreviewProperties sPreviewProperties;
    private FirebaseRemoteConfig mRemoteConfig;
    private String clientKey;
    private String networkType;
    private String mccAndMNCValues;
    private String appVersionName;
    private String clientSecret;
    public static HashMap<String, String> propertiesMap = new HashMap<>();
    public static HashMap<String, String> propertiesEPGListAPIParams = new HashMap<>();
    public static HashMap<String, String> propertiesAdSpotIdMap = new HashMap<>();
    public static HashMap<String, Integer> QUALITY_MAP = new HashMap<>();
    MobileBitrateCapVod mobileBitrateCapVod;
    public static ArrayMap<String, Integer> BITRATE_CAP = new ArrayMap<>();


    public static HashMap<String, String> getPropertiesNativeDisplayAdSpotIdMap() {
        if (propertiesNativeDisplayAdSpotIdMap == null || propertiesNativeDisplayAdSpotIdMap.size() <= 0) {
            propertiesNativeDisplayAdSpotIdMap = new HashMap<>();
            String[] allTabStrings = PrefUtils.getInstance().getPrefVmaxNativeDisplayAdId() != null ? PrefUtils.getInstance().getPrefVmaxNativeDisplayAdId().split(",") : new String[]{};
            for (String tabString :
                    allTabStrings) {
                String[] tabAdSpotIds = tabString.split(":");
                propertiesNativeDisplayAdSpotIdMap.put(tabAdSpotIds[0], tabAdSpotIds[1]);
            }
        }
        return propertiesNativeDisplayAdSpotIdMap;
    }

    public static HashMap<String, String> getPropertiesNativeVideoAdSpotIdMap() {
        if (propertiesNativeVideoAdSpotIdMap == null || propertiesNativeVideoAdSpotIdMap.size() <= 0) {
            propertiesNativeVideoAdSpotIdMap = new HashMap<>();
            String[] allTabStrings = PrefUtils.getInstance().getPrefVmaxNativeVideoAdId() != null ? PrefUtils.getInstance().getPrefVmaxNativeVideoAdId().split(",") : new String[]{};
            for (String tabString :
                    allTabStrings) {
                String[] tabAdSpotIds = tabString.split(":");
                propertiesNativeVideoAdSpotIdMap.put(tabAdSpotIds[0], tabAdSpotIds[1]);
            }
        }
        return propertiesNativeVideoAdSpotIdMap;
    }

    public static HashMap<String, String> propertiesNativeDisplayAdSpotIdMap = new HashMap<>();
    public static HashMap<String, String> propertiesNativeVideoAdSpotIdMap = new HashMap<>();
    private Context mContext;


 /*  Commenting out as this will be used in later releases

    private static final String PROFILE_WIFI = "profile_WIFI";
    private static final String PROFILE_4G = "profile_4G";
    private static final String PROFILE_3G = "profile_3G";
    private static final String PROFILE_2G = "profile_2G";
    private static final String VERSION = "version";
    private static final String LINK = "link";
    private static final String TYPE = "TYPE";
    private static final String MESSAGE = "message";

    private static final String MAX_DISPLAY_COUNT_TIME_SHIFT_HELP = "maxDisplayCountTimeshiftHelp";
    private static final String ENABLE_HUNGAMA_SDK = "enableHungamaSDK";
    private static final String MIXPANEL_EVENT_PRIORITY = "mixpanelEventPriority";
    private static final String CLEVERTAP_EVENT_PRIORITY = "cleverTapEventPriority";
    private static final String ENABLE_MY_PACKS_SCREEN = "enableMyPacksScreen";
    private static final String ENABLE_HUNGAMA_LOGO = "enableHungamaLogo";
    private static final String ENABLE_DITTO_CHANNEL_LOGO_ON_EPG = "enableDittoChannelLogoOnEpg";
    private static final String DITTO_CHANNEL_LOGO_IMAGEURL = "dittoChannelLogoImageUrl";
    private static final String ENABLE_HUNGAMA_RENT_TAG = "enableHungamaRentTag";
    private static final String ENABLE_PAST_EPG = "enablePastEpg";
    private static final String NO_OF_PAST_EPG_DAYS = "noOfPastEpgDays";
    private static final String MESSAGE_FAILED_TO_FETCH_OFFER_PACKS = "messageFailedToFetchOfferPacks";
    private static final String LIVE_HLS_PARAM_SKIP_LIVE_WINDOW = "liveHLSParamSkipLiveWindow";
    private static final String HOOQ_BG_SECTION_COLOR = "hooqBgsectionColor";
    private static final String HOOQ_LOGO_IMAGEURL = "hooqLogoImageUrl";
    private static final String ENABLED_AD_SONY_LIV = "enabledAdSONYLIV";
    private static final String ENABLED_AD_VAST3 = "enabledAdVAST3";
    private static final String AD_PROVIDER_TAG_VAST3 = "adProviderTagVAST3";
    private static final String AD_PROVIDER_TAG_OOYALA = "adProviderTagOOYALA";
    private static final String ENABLE_MUSIC_TAB = "enableMusicTab";
    private static final String ALLOW_WIFI_NETWORK_FOR_PAYMENT = "allowWiFiNetworkForPayment";
    private static final String OTP_DETECTION_TIME = "OTPDetectionTime";
    private static final String MANUAL_OTP_ALLOW = "manualOTPallow";
    private static final String ENABLE_SKIP_BUTTON_ON_OTP = "enableSkipButtonOnOTP";
    private static final String SHARE_MESSAGE = "shareMessage";
    private static final String EROSNOW_MUSIC_LOGO_IMAGE_URL = "erosnowMusicLogoImageUrl";
    private static final String SHARE_LINK = "shareLink";
    private static final String ENABLE_SONY_CHANNEL_LOGO = "enableSonyChannelLogo";
    private static final String SONY_CHANNEL_LOGO_IMAGE_URL = "sonyChannelLogoImageUrl";
    private static final String ENABLED_AD_SONY_LIV_V2 = "enabledAdSONYLIV_v2";
    private static final String SHOW_ALL_PACKAGES_OFFER_SCREEN = "showAllPackagesOffersScreen";
    private static final String EPG_LIST_API_PARAMS = "epgListAPIParams";
    private static final String ENABLE_HELP_SCREEN = "enableHelpScreen";
    private static final String ENABLE_MIXPANEL = "enableMixpanel";
    private static final String ENABLE_CLEAVERTAP = "enableCleverTap";
    private static final String CHROMECAST_RECIEVER_ID = "chromeCastRecieverId";
    private static final String ENABLE_ON_BOARDING_SCREEN = "enableOnBoardingScreen";
    private static final String YUPTV_CHANNEL_LOGO_IAMGEURL = "yuptvChannelLogoImageUrl";
    private static final String ENABLE_YUPP_TV_CHANNEL_LOGO_ON_EPG = "enableYupptvChannelLogoOnEpg";
    private static final String ENABLE_MIXPANEL_V2 = "enableMixpanelV2";
    private static final String ENABLE_ALTBALAJI_DOWNLOAD = "enableAltBalajiDownload";
    private static final String ENABLE_HOOQ_DOWNLOAD = "enableHooqDownload";
    private static final String ENABLE_EROS_NOW_DOWNLOAD = "enableErosnowDownload";
    private static final String SHARE_MESSAGE_MENU = "shareMessage_menu";
    private static final String SHARE_LINK_MENU = "shareLink_menu";
    private static final String VMAX_AD_REFRESH_RATE = "vmax_ad_refresh_rate";
    private static final String VMAX_NATIVE_AD_SPOT_ID = "vmaxNativeAdSpotId";
    private static final String NEGATIVE_BUTTON_TEXT = "negativeButtonText";
    private static final String POSTIVE_BUTTON_TEXT = "positiveButtonText";
    private static final String ALERT_TITLE = "alertTitle";
    private static final String ENABLED_EROS_NOW_PLAYER_LOGS = "enabledErosnowPlayerLogs";
    private static final String PLAYER_LOGS_ENABLED_TO = "playerLogsEnabledTo";
    private static final String ENABLE_HUNGAMA_DOWNLOAD = "enableHungamaDownload";
    private static final String ENABLE_HOOQ_CHROMECAST = "enableHooqChromecast";
    private static final String STANDARD_PACKAGE_IDS = "standardPackageIds";
    private static final String ENABLE_EROSNOW_DOWNLOADV1 = "enableErosnowDownloadV1";
    private static final String MSG_DOWNLOAD_DRM_LICENSE_FAILURE = "msgDownloadDRMLicenseFailure";
    private static final String MESSAGE_NOSP_AVAILABLE = "messageNOSPAvailable";
    private static final String DOWNLOAD_STATE_NOSP_AVAILABLE = "downldStateNOSPAvailable";
    private static final String DOWNLOAD_STATE_DRM_LICENSE_FAILED = "downldStateDRMLicenseFailed";
    private static final String DOWNLOAD_STATE_FAILED_DUE_TO_LOW_MEM = "downldStateFailedDueToLowMem";
    private static final String ENABLE_HUNGAMA_DOWNLOAD_V1 = "enableHungamaDownloadV1";
    private static final String ENABLE_HUNGAMA_DOWNLOAD_V3 = "enableHungamaDownloadV3";
    private static final String APPSFLYER_PLAYBACK_EVENT_SECONDS = "appsflyerPlaybackEventSeconds";
    private static final String BANNER_SCROLL_FREQUENCY = "bannerScrollFrequency";
    private static final String NEXT_EPISODE_POPUP_PERCENTAGE = "nextEpisodePopupPercentage";
    private static final String PLAYER_SEEK_TIME_SECONDS = "playerSeekTimeSeconds";
    private static final String VMAX_AD_BUTTON_COLOR = "vmax_ad_button_color";
    private static final String VMAX_LAYOUT_BG_COLOR = "vmax_layout_bg_color";
    private static final String VMAX_AD_HEADER_FONT_COLOR = "vmax_ad_header_font_color";
    private static final String EMAIL_REQUIRED_FOR_PARTNERS = "emailRequiredForPartners";
    private static final String AUTO_LOGIN_FAILED_MESSAGE = "autoLoginFailedMessage";
    private static final String SHOW_PRIVACY_CONSENT = "showPrivacyConsent";
    private static final String PRIVACY_CONSENT_MESSAGE = "privacyConsentMessage";
    private static final String ENABLE_VMAX_FOOTER_BANNER_AD = "enableVmaxFooterBannerAd";
    private static final String ENABLE_VMAX_INTERSTITIAL_ADV2 = "enableVmaxInterstitialAdV2";
    private static final String ENABLE_VMAX_INSTREAM_AD_V2 = "enableVmaxInstreamAdV2";
    private static final String VMAX_INTSTL_AD_APP_OPEN_ENABLED = "vmaxIntstlAdAppOpenEnabled";
    private static final String VMAX_INTSTLAD_APP_EXIT_ENABLED = "vmaxIntstlAdAppExitEnabled";
    private static final String VMAX_INTSTL_AD_APP_EXIT_AD_SPOT_ID = "vmaxIntstlAdAppExitAdSpotId";
    private static final String VMAX_INTSTL_AD_APP_OPEN_AD_SPOT_ID = "vmaxIntstlAdAppOpenAdSpotId";
    private static final String VMAX_INTSTL_AD_TAB_SWITCH_ENABLED = "vmaxIntstlAdTabSwitchEnabled";
    private static final String VMAX_INTSTL_AD_TAB_SWITCH_AD_SPOT_ID= "vmaxIntstlAdTabSwitchAdSpotId";
    private static final String VMAX_FOOTER_BANNER_AD_SPOT_ID = "vmaxFooterBannerAdSpotId";
    private static final String VMAX_INTERSTITIAL_AD_SPOT_ID = "vmaxInterstitialAdSpotId";
    private static final String VMAX_INSTREAM_AD_SPOT_ID = "vmaxInstreamAdSpotId";
    private static final String VMAX_PRE_ROLL_AD_ENABLED = "vmaxPreRollAdEnabled";
    private static final String VMAX_MID_ROLL_AD_ENABLED = "vmaxMidRollAdEnabled";
    private static final String VMAX_POST_ROLL_AD_ENABLED = "vmaxPostRollAdEnabled";
    private static final String VMAX_VIDEO_AD_MIN_DURATION = "vmaxVideoAdMinDuration";
    private static final String VMAX_FOOTER_BANNER_AD_REFRESH_RATE = "vmaxFooterBannerAdRefreshRate";
    private static final String VMAX_NATIVE_VIDEO_AD_REFRESH_RATE = "vmaxNativeVideoAdRefreshRate";
    private static final String ENABLE_VMAX_FOR_PARTNERS = "enableVmaxForPartners";
    private static final String VMAX_AD_HEADER_SECONDARY_FONT_COLOR = "vmaxAdHeaderSecondaryFontColor";
    private static final String PARENTAL_CONTROL_ENABLED = "parentalControlEnabled";
    private static final String VMAX_LAYOUT_BACKGROUND_BORDER_COLOR = "vmaxLayoutBackgroundBorderColor";
    private static final String ENABLE_BRANCH_IO_ANALYTICS = "enableBranchIOAnalytics";
    private static final String BRANC_IO_EVENT_PRIORITY = "branchIOEventPriority";
    private static final String BRANCH_IO_LAYBACK_EVENT_SECONDS = "branchIOPlaybackEventSeconds";
    private static final String EVENT_LOGGER_ENABLED = "eventLoggerEnabled";
    private static final String INTERSTITIAL_AD_FREQUENCY = "interstitialAdFrequency";
    private static final String PROMO_VIDEO_AD_FREQUENCY = "promoVideoAdFrequencies";
    private static final String PROMO_AD_HTML_URL = "promoAdHtmlURL";
    private static final String PROMO_AD_JSON_V22 = "promoAdJsonV22";
    private static final String PARTNER_DETAILS = "partnerDetails";
    private static final String PARTNER_DETAILSV22 = "partnerDetailsV22";
    private static final String CATEGORY_SCREEN_FILTERSV22 = "categoryScreenFiltersV22";
    private static final String PUBLISHER_GROUP_IDS_ANDROID = "publisherGroupIds_Android";
    private static final String RATING_SCREEN_JSON = "rating_screen_json";
    private static final String HOOQ_SDK_ENABLED = "hooq_sdk_enabled";*/


    public static PartnerDetailsResponse getPartnerDetailsResponse(Context mContext) {
        if (sPartnerDetailsResponse == null) {
            APIConstants.partnerDetailsDataPath = APIConstants.getPartnerDetailsDataPath(mContext);
            sPartnerDetailsResponse = (PartnerDetailsResponse) SDKUtils.loadObject(APIConstants.partnerDetailsDataPath);
        }
        return sPartnerDetailsResponse;
    }

    public static SetupBoxListResponse getSetupBoxList(Context mContext) {
        if (setupBoxListResponse == null) {
            APIConstants.setupBoxListPath = APIConstants.getSetupBoxListPath(mContext);
            setupBoxListResponse = (SetupBoxListResponse) SDKUtils.loadObject(APIConstants.setupBoxListPath);
        }
        return setupBoxListResponse;
    }

    public static PartnerDetailsAppInAppResponse getPartnerAppinAppList(Context mContext) {
        if ( sPartnerDetailsAppinAppResponse == null) {
            APIConstants.partnerDetailsAppinAppDataPath = APIConstants.getPartnerDetailsDataAppinAppPath(mContext);
            sPartnerDetailsAppinAppResponse = (PartnerDetailsAppInAppResponse) SDKUtils.loadObject(APIConstants.partnerDetailsAppinAppDataPath);
        }
        return sPartnerDetailsAppinAppResponse;
    }

    public static SearchConfigResponse getSearchConfigResponse(Context mContext) {
        if (searchConfigResponse == null) {
            APIConstants.searchConfigDataPath = APIConstants.getSearchConfigDataPath(mContext);
            try {
                searchConfigResponse = (SearchConfigResponse) SDKUtils.loadObject(APIConstants.searchConfigDataPath);
            } catch (ClassCastException ce) {
                ce.printStackTrace();
            }
        }
        return searchConfigResponse;
    }

    public static AdFullScreenListResponse getAdFullScreenConfig(Context mContext) {
        if (adFullScreenConfig == null) {
            APIConstants.adFullScreenConfigPath = APIConstants.getAdFullScreenConfigPath(mContext);
            try {
                adFullScreenConfig = (AdFullScreenListResponse) SDKUtils.loadObject(APIConstants.adFullScreenConfigPath);
            } catch (ClassCastException ce) {
                ce.printStackTrace();
            }
        }
        return adFullScreenConfig;
    }

    public static AdPopUpNotificationListResponse getAdPopupNotification(Context mContext) {
        if (adPopUpNotificationConfig == null) {
            APIConstants.adPopuNotificationConfigPath = APIConstants.getAdPopupNotification(mContext);
            try {
                adPopUpNotificationConfig = (AdPopUpNotificationListResponse) SDKUtils.loadObject(APIConstants.adPopuNotificationConfigPath);
            } catch (ClassCastException ce) {
                ce.printStackTrace();
            }
        }
        return adPopUpNotificationConfig;
    }

    private static SearchConfigResponse searchConfigResponse;
    private static AdFullScreenListResponse adFullScreenConfig;
    private static AdPopUpNotificationListResponse adPopUpNotificationConfig;
    private static PlayerControlsBitrates playerControlsBitrates;

    public static void clearCategoryScreenFilter() {
        categoryScreenFilters = null;
    }

    public static CategoryScreenFilters getCategoryScreenFilters(Context mContext) {
        if (categoryScreenFilters == null) {
            APIConstants.categoryScreenFiltersV2 = APIConstants.getcategoryScreenFilters(mContext);
            try {
                String serviceContentConfig = PrefUtils.getInstance().getServiceContentConfig();
                if (serviceContentConfig != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(serviceContentConfig);
                        JSONObject serviceNameObject;
                        if (!TextUtils.isEmpty(PrefUtils.getInstance().getServiceName())) {
                            serviceNameObject = jsonObj.getJSONObject(PrefUtils.getInstance().getServiceName());
                        } else {
                            serviceNameObject = jsonObj.getJSONObject(PrefUtils.getInstance().getDefaultServiceName());
                        }

                        JSONArray categoryScreenFiltersJsonObject = serviceNameObject.getJSONArray(APIConstants.CATEGORY_SCREEN_FILTER);
                        Gson gson = new Gson();
                        categoryScreenFilters = (CategoryScreenFilters) gson.fromJson(APIConstants.CATEGORY_SCREEN_FILTER_INITIAL + categoryScreenFiltersJsonObject.toString() + "}", CategoryScreenFilters.class);
                        return categoryScreenFilters;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                categoryScreenFilters = (CategoryScreenFilters) SDKUtils.loadObject(APIConstants.categoryScreenFiltersV2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return categoryScreenFilters;
    }


    private static PartnerDetailsResponse sPartnerDetailsResponse;
    private static PartnerDetailsAppInAppResponse sPartnerDetailsAppinAppResponse;
    private static SetupBoxListResponse setupBoxListResponse;
    private static CategoryScreenFilters categoryScreenFilters;

    public static RatingScreen getRatingScreenConfig(Context mContext) {
        if (ratingScreen == null) {
            APIConstants.ratingPopUpConfig = APIConstants.getRatingScreen(mContext);
            try {
                ratingScreen = (RatingScreen) SDKUtils.loadObject(APIConstants.ratingPopUpConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ratingScreen;
    }

    private static RatingScreen ratingScreen;

    public void init(Context mContext, String clientKey, String internetConnectivity, String mccAndMNCValues, String appVersionName, String clientSecret) {
        this.mContext = mContext;
        this.clientKey = clientKey;
        networkType = internetConnectivity;
        this.mccAndMNCValues = mccAndMNCValues;
        this.appVersionName = appVersionName;
        this.clientSecret = clientSecret;
        mRemoteConfig = myplexAPISDK.getmRemoteConfig();
        if (propertiesMap.isEmpty()) {
            sendPropertiesRequest();
        }


        /*if (!"success".equalsIgnoreCase(PrefUtils.getInstance().getPrefLoginStatus())) {
            fetchAvailableLoginTypes();
        }*/
    }

    public void updateHardCodedStringVernacular() {

        VernacularRequest vernacularRequest = new VernacularRequest(new APICallback<VernacularResponse>() {
            @Override
            public void onResponse(APIResponse<VernacularResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }
                if (response.body().language_strings != null && response.body().language_strings != null) {
                    // for(int i =0;i<response.body().languageStrings.size();i++){
                    if (response.body().language_strings.More != null) {
                        StringManager.getInstance().addString(APIConstants.MORE, response.body().language_strings.More.getHindi());
                    } else {
                        StringManager.getInstance().addString(APIConstants.MORE, "");
                    }
                    if (response.body().language_strings.Season != null) {
                        StringManager.getInstance().addString(APIConstants.SEASON, response.body().language_strings.Season.getHindi());
                    } else {
                        StringManager.getInstance().addString(APIConstants.SEASON, "");
                    }

                    if (response.body().language_strings.upcomingPrograms != null) {
                        StringManager.getInstance().addString(APIConstants.UPCOMING_PROGRAMS, response.body().language_strings.upcomingPrograms.getHindi());
                    } else {
                        StringManager.getInstance().addString(APIConstants.UPCOMING_PROGRAMS, "");
                    }

                    if (response.body().language_strings.peopleAlsoWatched != null) {
                        StringManager.getInstance().addString(APIConstants.PEOPLE_ALSO_WATCHED, response.body().language_strings.peopleAlsoWatched.getHindi());
                    } else {
                        StringManager.getInstance().addString(APIConstants.PEOPLE_ALSO_WATCHED, "");
                    }

                    if (response.body().language_strings.currentlyPlayingOnOtherChannels != null) {
                        StringManager.getInstance().addString(APIConstants.CURRENTLY_PLAY_OTHER_CHANNELS, response.body().language_strings.currentlyPlayingOnOtherChannels.getHindi());
                    } else {
                        StringManager.getInstance().addString(APIConstants.CURRENTLY_PLAY_OTHER_CHANNELS, "");
                    }
                    //}
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(vernacularRequest);
    }

//    private void updateProperties() {
//
//        PrefUtils.getInstance().setPrefEnableOnBoardingScreen(
//                "true".equalsIgnoreCase(mRemoteConfig.getString(ENABLE_ON_BOARDING_SCREEN)));
//        PrefUtils.getInstance().
//                setPrefIsHooq_sdk_enabled("true".equalsIgnoreCase(mRemoteConfig.getString(HOOQ_SDK_ENABLED)));
//        PrefUtils.getInstance().setPrefEnableSkipOnOTP(
//                "true".equalsIgnoreCase(mRemoteConfig.getString(ENABLE_SKIP_BUTTON_ON_OTP)));
//        PrefUtils.getInstance().setPrefStandardPackageIds(mRemoteConfig.getString(STANDARD_PACKAGE_IDS));

//        PrefUtils.getInstance().setPrefChromeCastRecieverId(mRemoteConfig.getString(CHROMECAST_RECIEVER_ID));
//        addEpgListParamsToPropertiesMap(mRemoteConfig.getString(EPG_LIST_API_PARAMS));
//        //App Version Releated data
//        VersionData latestVersionData = new VersionData();
//        latestVersionData.version = (int)mRemoteConfig.getDouble(VERSION);
//        latestVersionData.link = mRemoteConfig.getString(LINK);
//        latestVersionData.type = mRemoteConfig.getString(TYPE);
//        latestVersionData.message = mRemoteConfig.getString(MESSAGE);
//        latestVersionData.negativeButtonText = mRemoteConfig.getString(NEGATIVE_BUTTON_TEXT);
//        latestVersionData.positiveButtonText = mRemoteConfig.getString(POSTIVE_BUTTON_TEXT);
//        latestVersionData.alertTitle = mRemoteConfig.getString(ALERT_TITLE);
//        APIConstants.versionDataPath = APIConstants.getVersionDataPath(mContext);
//        SDKUtils.saveObject(latestVersionData, APIConstants.versionDataPath);
//
//    }

    private void sendPropertiesRequest() {
        String mccValue = "";
        String mncValue = "";
        if (!TextUtils.isEmpty(mccAndMNCValues)) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
            Log.e("MCC ", mccValue + " MNC " + mncValue);
        }
        PropertiesRequest.Params params = new PropertiesRequest.Params(clientKey, appVersionName, networkType, mccValue, mncValue, clientSecret);
        PropertiesRequest propertiesRequest = new PropertiesRequest(params, new APICallback<PropertiesData>() {
            @Override
            public void onResponse(APIResponse<PropertiesData> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }
                String profile_2G = null;
                String profile_3G = null;
                String profile_4G = null;
                String profile_5G = null;
                String profile_wifi = null;
                try {
                    if (response.body().properties != null) {

                        if (!TextUtils.isEmpty(response.body().properties.enableOnBoardingScreen)) {
                            PrefUtils.getInstance().setPrefEnableOnBoardingScreen("true".equalsIgnoreCase(response.body().properties.enableOnBoardingScreen));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.hooq_sdk_enabled)) {
                            PrefUtils.getInstance().setPrefIsHooq_sdk_enabled("true".equalsIgnoreCase(response.body().properties.hooq_sdk_enabled));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableSkipButtonOnOTP)) {
                            PrefUtils.getInstance().setPrefEnableSkipOnOTP("true".equalsIgnoreCase(response.body().properties.enableSkipButtonOnOTP));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.standardPackageIds)) {
                            PrefUtils.getInstance().setPrefStandardPackageIds(response.body().properties.standardPackageIds);
                        }

                        if (response.body().properties.contentRatingConfig != null) {
                            Gson gson = new Gson();
                            String contentRatingConfigStr = StringEscapeUtils.unescapeJava(response.body().properties.contentRatingConfig);
                            ContentRatingConfig contentRatingConfig = gson.fromJson(contentRatingConfigStr, ContentRatingConfig.class);
                            PrefUtils.getInstance().setMaturityTimer(contentRatingConfig.duration + "," + contentRatingConfig.portrait + "," + contentRatingConfig.text);
                        }


                        try {
                            if (!TextUtils.isEmpty(response.body().properties.searchConfig)) {
                                String searchConfig = StringEscapeUtils.unescapeJava(response.body().properties.searchConfig);
                                SDKLogger.debug(" searchConfig " + searchConfig);
                                Gson gson = new Gson();
                                searchConfigResponse = gson.fromJson(searchConfig, SearchConfigResponse.class);
                                if (searchConfigResponse != null) {
                                    APIConstants.searchConfigDataPath = APIConstants.getSearchConfigDataPath(mContext);
                                    PrefUtils.getInstance().setRecentSearchCountLimit(searchConfigResponse.recentSearchCountLimit);
                                    SDKUtils.saveObject(searchConfigResponse, APIConstants.searchConfigDataPath);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!TextUtils.isEmpty(response.body().properties.serviceContentConfig)) {
                            String serviceContentConfig = StringEscapeUtils.unescapeJava(response.body().properties.serviceContentConfig);
                            SDKLogger.debug(" searchConfig " + serviceContentConfig);
                            PrefUtils.getInstance().saveServiceContentConfig(serviceContentConfig);
                        }


                        if (!TextUtils.isEmpty(response.body().properties.supportpage_url)) {
                            PrefUtils.getInstance().setSupportPageURL(response.body().properties.supportpage_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.contact_us_page_url)) {
                            PrefUtils.getInstance().setContactUsPageURL(response.body().properties.contact_us_page_url);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.chromeCastRecieverId)) {
                            PrefUtils.getInstance().setPrefChromeCastRecieverId(response.body().properties.chromeCastRecieverId);
                        }
                        if (response.body()!=null && response.body().properties != null && !TextUtils.isEmpty(response.body().properties.forceAcceptLanguage)) {
                            PrefUtils.getInstance().setForceAcceptLanguage(response.body().properties.forceAcceptLanguage);
                        } else {
                            PrefUtils.getInstance().setForceAcceptLanguage("");
                        }
                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setLegacyUpgradePopup(response.body().properties.legacyUpgradePopup);
                        }

                        if (response.body().properties != null && response.body().properties.thumbnailPreviewPlaybackEnabled != null) {
                            PrefUtils.getInstance().setThumbnailPreviewPlaybackEnabled(response.body().properties.thumbnailPreviewPlaybackEnabled);
                        }
                        /*PrefUtils.getInstance().setForceAcceptLanguage("hindi");*/
                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setBoolean(APIConstants.SHOULD_ENABLE_REMOTE_CONFIG_API, response.body().properties.enableRemoteConfigAndroid);
                        }

                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setBoolean(APIConstants.SHOULD_ENABLE_MEDIA_DOMAIN_API, response.body().properties.enableMediaSubDomain);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.vernacular_strings_api)) {
                            PrefUtils.getInstance().setVernacularLanguageURL(response.body().properties.vernacular_strings_api);
                            updateHardCodedStringVernacular();
                        }
                        if (response.body().properties.search_error != null) {
                            PrefUtils.getInstance().setSearchErrorMessage(response.body().properties.search_error);

                        }
                        if (!TextUtils.isEmpty(response.body().properties.app_languages_api_v2)) {
                            PrefUtils.getInstance().setAppLanguageURL(response.body().properties.app_languages_api_v2);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.share_deep_link_url)) {
                            PrefUtils.getInstance().setAppDeepLinkUrl(response.body().properties.share_deep_link_url);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.share_url)) {
                            PrefUtils.getInstance().setAppSharePwaurl(response.body().properties.share_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.otpLength)) {
                            PrefUtils.getInstance().setOTPLength(response.body().properties.otpLength);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.exploreOfferPageLink)) {
                            PrefUtils.getInstance().setExploreOffers(response.body().properties.exploreOfferPageLink);
                        }
                        if (response.body().properties!=null) {
                            PrefUtils.getInstance().setIsEnableSubScribeToApps(response.body().properties.Enable_NewUser_SubscribeToApps);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.subscribeToAppsPageLink)) {
                            PrefUtils.getInstance().setSubscribeToApps(response.body().properties.subscribeToAppsPageLink);
                        }

                        if (response.body().properties.epgListAPIParams != null) {
                            addEpgListParamsToPropertiesMap(response.body().properties.epgListAPIParams);
                        }

                        // default profile icon
                        if (!TextUtils.isEmpty(response.body().properties.defaultProfileImage)) {
                            PrefUtils.getInstance().setDefaultProfileImage(response.body().properties.defaultProfileImage);
                        } else {
                            PrefUtils.getInstance().setDefaultProfileImage("");
                        }

                        if (response.body().properties.version != 0
                                && !TextUtils.isEmpty(response.body().properties.link)
                                && !TextUtils.isEmpty(response.body().properties.type)
                                && !TextUtils.isEmpty(response.body().properties.message)) {
                            VersionData latestVersionData = new VersionData();
                            latestVersionData.version = response.body().properties.version;
                            latestVersionData.link = response.body().properties.link;
                            latestVersionData.type = response.body().properties.type;
                            latestVersionData.message = response.body().properties.message;
                            if (!TextUtils.isEmpty(response.body().properties.negativeButtonText)) {
                                latestVersionData.negativeButtonText = response.body().properties.negativeButtonText;
                            }
                            if (!TextUtils.isEmpty(response.body().properties.positiveButtonText)) {
                                latestVersionData.positiveButtonText = response.body().properties.positiveButtonText;
                            }
                            if (!TextUtils.isEmpty(response.body().properties.alertTitle)) {
                                latestVersionData.alertTitle = response.body().properties.alertTitle;
                            }
                            APIConstants.versionDataPath = APIConstants.getVersionDataPath(mContext);
                            SDKUtils.saveObject(latestVersionData, APIConstants.versionDataPath);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.profile_2G)) {
                            profile_2G = response.body().properties.profile_2G;
                        }
                        propertiesMap.put("2G", profile_2G);
                        PrefUtils.getInstance().setProfile2G(profile_2G);

                        if (!TextUtils.isEmpty(response.body().properties.profile_3G)) {
                            profile_3G = response.body().properties.profile_3G;
                        }
                        propertiesMap.put("3G", profile_3G);
                        PrefUtils.getInstance().setProfile3G(profile_3G);

                        if (!TextUtils.isEmpty(response.body().properties.profile_4G)) {
                            profile_4G = response.body().properties.profile_4G;
                        }
                        propertiesMap.put("4G", profile_4G);
                        PrefUtils.getInstance().setProfile4G(profile_4G);
                        if (!TextUtils.isEmpty(response.body().properties.profile_5G)) {
                            profile_5G = response.body().properties.profile_5G;
                        }
                        propertiesMap.put("5G", profile_5G);
                        PrefUtils.getInstance().setProfile5G(profile_5G);

                        if (!TextUtils.isEmpty(response.body().properties.profile_WIFI)) {
                            profile_wifi = response.body().properties.profile_WIFI;
                        }

                        propertiesMap.put("wifi", profile_wifi);
                        PrefUtils.getInstance().setProfileWifi(profile_wifi);
                        if (!TextUtils.isEmpty(response.body().properties.enableHungamaSDK)) {
                            PrefUtils.getInstance().setPrefEnableHungamaSDK("true".equalsIgnoreCase(response.body().properties.enableHungamaSDK));
                        }

                        if (response.body().properties.maxDisplayCountTimeshiftHelp > 0) {
                            PrefUtils.getInstance().setPrefMaxDisplayCountTimeShift(response.body().properties.maxDisplayCountTimeshiftHelp);
                        }
                        try {
                            if (!TextUtils.isEmpty(response.body().properties.partnerDetailsV23Android)) {
                                String partnerDetailResponse = StringEscapeUtils.unescapeJava(response.body().properties.partnerDetailsV23Android);
                                SDKLogger.debug(" partnerDetailResponses " + partnerDetailResponse);

                                if (response.body().properties != null) {
                                    Gson gson = new Gson();
                                    sPartnerDetailsResponse = gson.fromJson(partnerDetailResponse, PartnerDetailsResponse.class);
                                    if (sPartnerDetailsResponse != null) {
                                        APIConstants.partnerDetailsDataPath = APIConstants.getPartnerDetailsDataPath(mContext);
                                        SDKUtils.saveObject(sPartnerDetailsResponse, APIConstants.partnerDetailsDataPath);
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (!TextUtils.isEmpty(response.body().properties.partnerDetailsV51)) {
                                String partnerDetailsV51 = StringEscapeUtils.unescapeJava(response.body().properties.partnerDetailsV51);
                                SDKLogger.debug(" partnerDetailsV51 " + partnerDetailsV51);

                                if (response.body().properties != null) {
                                    Gson gson = new Gson();
                                    sPartnerDetailsAppinAppResponse = gson.fromJson(partnerDetailsV51, PartnerDetailsAppInAppResponse.class);
                                    if (sPartnerDetailsAppinAppResponse != null) {
                                        APIConstants.partnerDetailsAppinAppDataPath = APIConstants.getPartnerDetailsDataAppinAppPath(mContext);
                                        SDKUtils.saveObject(sPartnerDetailsAppinAppResponse, APIConstants.partnerDetailsAppinAppDataPath);
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (!TextUtils.isEmpty(response.body().properties.setupBoxList)) {
                                String setupBoxResponse = StringEscapeUtils.unescapeJava(response.body().properties.setupBoxList);
                                SDKLogger.debug(" setuBoxList " + setupBoxResponse);

                                if (response.body().properties != null) {
                                    Gson gson = new Gson();
                                    setupBoxListResponse = gson.fromJson(setupBoxResponse, SetupBoxListResponse.class);
                                    if (setupBoxListResponse != null) {
                                        APIConstants.setupBoxListPath = APIConstants.getSetupBoxListPath(mContext);
                                        SDKUtils.saveObject(setupBoxListResponse, APIConstants.setupBoxListPath);
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (!TextUtils.isEmpty(response.body().properties.adFullScreenConfig)) {
                                String adFullScreenResponseString = StringEscapeUtils.unescapeJava(response.body().properties.adFullScreenConfig);
                                Log.d(" adFullScreenResponse ", adFullScreenResponseString);

                                if (response.body().properties != null) {
                                    Gson gson = new Gson();
                                    adFullScreenConfig = gson.fromJson(adFullScreenResponseString, AdFullScreenListResponse.class);
                                    if (adFullScreenConfig != null) {
                                        APIConstants.adFullScreenConfigPath = APIConstants.getAdFullScreenConfigPath(mContext);
                                        SDKUtils.saveObject(adFullScreenConfig, APIConstants.adFullScreenConfigPath);
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (!TextUtils.isEmpty(response.body().properties.adPopUpNotification)) {
                                String adPopUpNotification = StringEscapeUtils.unescapeJava(response.body().properties.adPopUpNotification);
                                Log.d(" adPopUpNotification ", adPopUpNotification);

                                if (response.body().properties != null) {
                                    Gson gson = new Gson();
                                    adPopUpNotificationConfig = gson.fromJson(adPopUpNotification, AdPopUpNotificationListResponse.class);
                                    if (adPopUpNotificationConfig != null) {
                                        APIConstants.adPopuNotificationConfigPath = APIConstants.getAdPopupNotification(mContext);
                                        SDKUtils.saveObject(adPopUpNotificationConfig, APIConstants.adPopuNotificationConfigPath);
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (!TextUtils.isEmpty(response.body().properties.adPopUpNotificationV2)) {
                                String adPopUpNotification = StringEscapeUtils.unescapeJava(response.body().properties.adPopUpNotificationV2);
                                Log.d(" adPopUpNotification ", adPopUpNotification);

                                if (response.body().properties != null) {
                                    Gson gson = new Gson();
                                    adPopUpNotificationConfig = gson.fromJson(adPopUpNotification, AdPopUpNotificationListResponse.class);
                                    if (adPopUpNotificationConfig != null) {
                                        APIConstants.adPopuNotificationConfigPathV2 = APIConstants.getAdPopupNotification(mContext);
                                        SDKUtils.saveObject(adPopUpNotificationConfig, APIConstants.adPopuNotificationConfigPathV2);
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (!TextUtils.isEmpty(response.body().properties.categoryScreenFiltersV23)) {
                                String categoryScreenFilterData = StringEscapeUtils.unescapeJava(response.body().properties.categoryScreenFiltersV23);
                                Gson gson = new Gson();
                                CategoryScreenFilters categoryScreenFilters = gson.fromJson(categoryScreenFilterData, CategoryScreenFilters.class);
                                if (categoryScreenFilters != null) {
                                    APIConstants.categoryScreenFiltersV2 = APIConstants.getcategoryScreenFilters(mContext);
                                    SDKUtils.saveObject(categoryScreenFilters, APIConstants.categoryScreenFiltersV2);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enablePrefLangScreen)) {
                            PrefUtils.getInstance().setEnablePrefLangScreen("true".equalsIgnoreCase(response.body().properties.enablePrefLangScreen));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.mixpanelEventPriority)) {
                            if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_LOW.equalsIgnoreCase(response.body().properties.mixpanelEventPriority)) {
                                PrefUtils.getInstance().setPrefMixpanelEventPriority(1);
                            } else if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_MED.equalsIgnoreCase(response.body().properties.mixpanelEventPriority)) {
                                PrefUtils.getInstance().setPrefMixpanelEventPriority(2);
                            } else if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_HIGH.equalsIgnoreCase(response.body().properties.mixpanelEventPriority)) {
                                PrefUtils.getInstance().setPrefMixpanelEventPriority(3);
                            }
                        }

                        if (!TextUtils.isEmpty(response.body().properties.cleverTapEventPriority)) {
                            if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_LOW.equalsIgnoreCase(response.body().properties.cleverTapEventPriority)) {
                                PrefUtils.getInstance().setPrefCleverTapEventPriority(1);
                            } else if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_MED.equalsIgnoreCase(response.body().properties.cleverTapEventPriority)) {
                                PrefUtils.getInstance().setPrefCleverTapEventPriority(2);
                            } else if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_HIGH.equalsIgnoreCase(response.body().properties.cleverTapEventPriority)) {
                                PrefUtils.getInstance().setPrefCleverTapEventPriority(3);
                            }
                        }
                        try {
                            if (!TextUtils.isEmpty(response.body().properties.rating_screen_json)) {
                                String ratingPopUpData = StringEscapeUtils.unescapeJava(response.body().properties.rating_screen_json);
                                if (response.body().properties != null) {
                                    Gson gson = new Gson();
                                    ratingScreen = gson.fromJson(ratingPopUpData, RatingScreen.class);
                                    if (ratingScreen != null) {
                                        APIConstants.ratingPopUpConfig = APIConstants.getRatingScreen(mContext);
                                        SDKUtils.saveObject(ratingScreen, APIConstants.ratingPopUpConfig);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //Log.d(TAG, "response.body().properties " + response.body().properties);
                        if (!TextUtils.isEmpty(response.body().properties.branchIOEventPriority)) {
                            if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_LOW.equalsIgnoreCase(response.body().properties.branchIOEventPriority)) {
                                PrefUtils.getInstance().setPrefBranchIOEventPriority(1);
                            } else if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_MED.equalsIgnoreCase(response.body().properties.branchIOEventPriority)) {
                                PrefUtils.getInstance().setPrefBranchIOEventPriority(2);
                            } else if (PROPERTY_MIXPANEL_EVENTS_THRESHOLD_HIGH.equalsIgnoreCase(response.body().properties.branchIOEventPriority)) {
                                PrefUtils.getInstance().setPrefBranchIOEventPriority(3);
                            }
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableMyPacksScreen)) {
                            PrefUtils.getInstance().setPrefEnableMyPackScreen("true".equalsIgnoreCase(response.body().properties.enableMyPacksScreen));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableHungamaLogo)) {
                            PrefUtils.getInstance().setPrefEnableHungamaLogo("true".equalsIgnoreCase(response.body().properties.enableHungamaLogo));
                        }
                        if (!TextUtils.isEmpty(response.body().properties.enableDittoChannelLogoOnEpg)) {
                            PrefUtils.getInstance().setPrefEnableDittoChannelLogoOnEpg("true".equalsIgnoreCase(response.body().properties.enableDittoChannelLogoOnEpg));
                        }
                        if (!TextUtils.isEmpty(response.body().properties.enableYupptvChannelLogoOnEpg)) {
                            PrefUtils.getInstance().setPrefEnableYuptvChannelLogoOnEpg("true".equalsIgnoreCase(response.body().properties.enableYupptvChannelLogoOnEpg));
                        }
                        if (!TextUtils.isEmpty(response.body().properties.enableHungamaRentTag)) {
                            PrefUtils.getInstance().setPrefEnableHungamaRentTag("true".equalsIgnoreCase(response.body().properties.enableHungamaRentTag));
                        }
                        if (!TextUtils.isEmpty(response.body().properties.dittoChannelLogoImageUrl)) {
                            PrefUtils.getInstance().setPrefDittoChannelLogoUrlOnEpg(response.body().properties.dittoChannelLogoImageUrl);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.enablePastEpg)) {
                            PrefUtils.getInstance().setPrefEnablePastEpg("true".equalsIgnoreCase(response.body().properties.enablePastEpg));
                        }
                        if (response.body().properties.noOfPastEpgDays > 0) {
                            PrefUtils.getInstance().setPrefNoOfPastEpgDays(response.body().properties.noOfPastEpgDays);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.messageFailedToFetchOfferPacks)) {
                            PrefUtils.getInstance().setPrefMessageFailedToFetchOffers(response.body().properties.messageFailedToFetchOfferPacks);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.liveHLSParamSkipLiveWindow)) {
                            PrefUtils.getInstance().setPrefDittoStreamParam(response.body().properties.liveHLSParamSkipLiveWindow);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.hooqBgsectionColor)) {
                            PrefUtils.getInstance().setPrefHooqBgsectionColor(response.body().properties.hooqBgsectionColor);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.hooqLogoImageUrl)) {
                            PrefUtils.getInstance().setPrefHooqLogoImageUrl(response.body().properties.hooqLogoImageUrl);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enabledAdSONYLIV)) {
                            PrefUtils.getInstance().setPrefEnableSonyLivAd("true".equalsIgnoreCase(response.body().properties.enabledAdSONYLIV));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enabledAdVAST3)) {
                            PrefUtils.getInstance().setPrefEnableVAST3Ad("true".equalsIgnoreCase(response.body().properties.enabledAdVAST3));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.adProviderTagOOYALA)) {
                            PrefUtils.getInstance().setPrefAdProviderTagOoyala(response.body().properties.adProviderTagOOYALA);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.adProviderTagVAST3)) {
                            PrefUtils.getInstance().setPrefAdProviderTagVAST3(response.body().properties.adProviderTagVAST3);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableMusicTab)) {
                            PrefUtils.getInstance().setPrefEnableMusicTab("true".equalsIgnoreCase(response.body().properties.enableMusicTab));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.allowWiFiNetworkForPayment)) {
                            PrefUtils.getInstance().setPrefAllowWiFiNetworkForPayment("true".equalsIgnoreCase(response.body().properties.allowWiFiNetworkForPayment));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.manualOTPallow)) {
                            PrefUtils.getInstance().setPrefEnableManualOTP("true".equalsIgnoreCase(response.body().properties.manualOTPallow));
                        }

                        if (response.body().properties.OTPDetectionTime > 0) {
                            PrefUtils.getInstance().setPrefOTPDetectionTimeOut(response.body().properties.OTPDetectionTime);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.shareMessage)) {
                            PrefUtils.getInstance().setPrefShareMessage(response.body().properties.shareMessage);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.shareLink)) {
                            PrefUtils.getInstance().setPrefShareUrl(response.body().properties.shareLink);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.erosnowMusicLogoImageUrl)) {
                            PrefUtils.getInstance().setPrefErosNowMusicLogoUrlOnEpg(response.body().properties.erosnowMusicLogoImageUrl);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableSonyChannelLogo)) {
                            PrefUtils.getInstance().setPrefEnableSonyChannelLogoOnEpg("true".equalsIgnoreCase(response.body().properties.enableSonyChannelLogo));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.sonyChannelLogoImageUrl)) {
                            PrefUtils.getInstance().setPrefSonyChannelLogoUrlOnEpg(response.body().properties.sonyChannelLogoImageUrl);
                        }


                        if (!TextUtils.isEmpty(response.body().properties.enabledAdSONYLIV_v2)) {
                            PrefUtils.getInstance().setPrefEnableSonylivAdV2("true".equalsIgnoreCase(response.body().properties.enabledAdSONYLIV_v2));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.showAllPackagesOffersScreen)) {
                            PrefUtils.getInstance().setPrefShowAllPackagesOfferScreen("true".equalsIgnoreCase(response.body().properties.showAllPackagesOffersScreen));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableHelpScreen)) {
                            PrefUtils.getInstance().setPrefEnableHelpScreen("true".equalsIgnoreCase(response.body().properties.enableHelpScreen));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableCleverTap)) {
                            PrefUtils.getInstance().setPrefEnableCleverTap("true".equalsIgnoreCase(response.body().properties.enableCleverTap));
                        }
                    /*if (response.body().properties != null) {
                        PrefUtils.getInstance().setPrefShowPrivacyConsent("true".equalsIgnoreCase(response.body().properties.showPrivacyConsent));
                    }*/
                        if (!TextUtils.isEmpty(response.body().properties.yuptvChannelLogoImageUrl)) {
                            PrefUtils.getInstance().setPrefYupTVChannelLogoImageUrl(response.body().properties.yuptvChannelLogoImageUrl);
                        }


                        if (!TextUtils.isEmpty(response.body().properties.enableMixpanelV2)) {
                            PrefUtils.getInstance().setPrefEnableMixpanel("true".equalsIgnoreCase(response.body().properties.enableMixpanelV2));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableHooqDownload)) {
                            PrefUtils.getInstance().setPrefEnableHooqDownload("true".equalsIgnoreCase(response.body().properties.enableHooqDownload));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableErosnowDownload)) {
                            PrefUtils.getInstance().setPrefEnableErosnowDownload("true".equalsIgnoreCase(response.body().properties.enableErosnowDownload));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableErosnowDownloadV1)) {
                            PrefUtils.getInstance().setPrefEnableErosnowDownloadV1("true".equalsIgnoreCase(response.body().properties.enableErosnowDownloadV1));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableAltBalajiDownload)) {
                            PrefUtils.getInstance().setPrefEnableAltBalajiDownload("true".equalsIgnoreCase(response.body().properties.enableAltBalajiDownload));
                        }

//                    Log.d("PropertiesHandler:" , "" + response.body().properties);

                        if (!TextUtils.isEmpty(response.body().properties.shareMessage_menu)) {
                            PrefUtils.getInstance().setPrefShareMessageForMenu(response.body().properties.shareMessage_menu);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.shareLink_menu)) {
                            PrefUtils.getInstance().setPrefShareUrlForMenu(response.body().properties.shareLink_menu);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.vmax_ad_refresh_rate)) {
                            PrefUtils.getInstance().setPrefVmaxRefreshRate(response.body().properties.vmax_ad_refresh_rate);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.vmaxNativeAdSpotId)) {
                            PrefUtils.getInstance().setPrefVmaxNativeAdId(response.body().properties.vmaxNativeAdSpotId);
                            String[] allTabStrings = response.body().properties.vmaxNativeAdSpotId.split(",");
                            for (String tabString :
                                    allTabStrings) {
                                String[] tabAdSpotIds = tabString.split(":");
                                propertiesAdSpotIdMap.put(tabAdSpotIds[0], tabAdSpotIds[1]);
                            }
                            SDKLogger.debug("all tabs adspotIds- " + propertiesAdSpotIdMap);
                        }


                        if (!TextUtils.isEmpty(response.body().properties.enabledErosnowPlayerLogs)) {
                            PrefUtils.getInstance().setPrefEnableErosNowPlayerLogs("true".equalsIgnoreCase(response.body().properties.enabledErosnowPlayerLogs));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.playerLogsEnabledTo)) {
                            PrefUtils.getInstance().setPrefPlayerLogsEnableTo(response.body().properties.playerLogsEnabledTo);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableHungamaDownloadV3)) {
                            PrefUtils.getInstance().setPrefEnableHungamaDownload("true".equalsIgnoreCase(response.body().properties.enableHungamaDownloadV3));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableHooqChromecast)) {
                            PrefUtils.getInstance().setPrefEnableHOOQChromeCast("true".equalsIgnoreCase(response.body().properties.enableHooqChromecast));
                        }
                        if (!TextUtils.isEmpty(response.body().properties.msgDownloadDRMLicenseFailure)) {
                            PrefUtils.getInstance().setPrefMessageDRMLicenseFailed(response.body().properties.msgDownloadDRMLicenseFailure);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.messageNOSPAvailable)) {
                            PrefUtils.getInstance().setPrefMessageNoSpaceWhileUnzip(response.body().properties.messageNOSPAvailable);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.downldStateDRMLicenseFailed)) {
                            PrefUtils.getInstance().setPrefDownloadStateDRMLicenseFailed(response.body().properties.downldStateDRMLicenseFailed);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.downldStateFailedDueToLowMem)) {
                            PrefUtils.getInstance().setPrefDownldStateFailedDueToLowMem(response.body().properties.downldStateFailedDueToLowMem);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.privacyConsentMessage)) {
                            PrefUtils.getInstance().setPrefPrivacyConsentMessage(response.body().properties.privacyConsentMessage);
                        }


                        if (!TextUtils.isEmpty(response.body().properties.downldStateNOSPAvailable)) {
                            PrefUtils.getInstance().setPrefDownloadStateNOSPAvailable(response.body().properties.downldStateNOSPAvailable);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.appsflyerPlaybackEventSeconds)) {
                            PrefUtils.getInstance().setPrefAppsflyerPlaybackEventSeconds(response.body().properties.appsflyerPlaybackEventSeconds);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.bannerScrollFrequency)) {
                            PrefUtils.getInstance().setBannerAutoScrollFrequency(response.body().properties.bannerScrollFrequency);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.playerSeekTimeSeconds)) {
                            PrefUtils.getInstance().setPrefPlayerSeekTimeSeconds(response.body().properties.playerSeekTimeSeconds);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.vmax_ad_button_color)) {
                            PrefUtils.getInstance().setVmaxAdButtonColor(response.body().properties.vmax_ad_button_color);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.vmax_layout_bg_color)) {
                            PrefUtils.getInstance().setVmaxLayoutBgColor(response.body().properties.vmax_layout_bg_color);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.vmaxLayoutBackgroundBorderColor)) {
                            PrefUtils.getInstance().setVmaxAdBackGroundBorderColor(response.body().properties.vmaxLayoutBackgroundBorderColor);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.vmax_ad_header_font_color)) {
                            PrefUtils.getInstance().setVmaxAdHeaderFontColor(response.body().properties.vmax_ad_header_font_color);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.vmaxAdHeaderSecondaryFontColor)) {
                            PrefUtils.getInstance().setVmaxAdHeaderSecondaryFontColor(response.body().properties.vmaxAdHeaderSecondaryFontColor);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.emailRequiredForPartners)) {
                            PrefUtils.getInstance().setPrefEmailRequiredForPartners(response.body().properties.emailRequiredForPartners);
                        }


                        if (!TextUtils.isEmpty(response.body().properties.autoLoginFailedMessage)) {
                            PrefUtils.getInstance().setPrefAutoLoginFailedMessage(response.body().properties.autoLoginFailedMessage);
                        }

                        if (response.body() != null
                                && response.body().properties != null
                                && response.body().properties.bufferConfigAndroid != null) {
                            if (!TextUtils.isEmpty(response.body().properties.bufferConfigAndroid)) {
                                String bufferConfigAndroid = StringEscapeUtils.unescapeJava(response.body().properties.bufferConfigAndroid);
                                PrefUtils.getInstance().setBufferConfigAndroid(bufferConfigAndroid);
                            }
                        }

                        if (!TextUtils.isEmpty(response.body().properties.nextEpisodePopupPercentage)) {
                            // PrefUtils.getInstance().setNextEpisodePopupPercentage(response.body().properties.nextEpisodePopupPercentage);
                            String[] episodePercentages = response.body().properties.nextEpisodePopupPercentage.split(",");
                            for (String episodePercentage : episodePercentages) {
                                String[] percentagePair = episodePercentage.split("_", 2);
                                PrefUtils.getInstance().setString(percentagePair[0], percentagePair[1]);
                            }
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableVmaxFooterBannerAd)) {
                            PrefUtils.getInstance().setPrefEnableVmaxFooterBannerAd("true".equalsIgnoreCase(response.body().properties.enableVmaxFooterBannerAd));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableVmaxInterstitialAdV2)) {
                            PrefUtils.getInstance().setPrefEnableVmaxInterStitialAd("true".equalsIgnoreCase(response.body().properties.enableVmaxInterstitialAdV2));
                        }

                        if (response.body().properties != null) {
                            if (!TextUtils.isEmpty(response.body().properties.enableVmaxInstreamAdV2))
                                PrefUtils.getInstance().setPrefEnableVmaxInStreamAd("true".equalsIgnoreCase(response.body().properties.enableVmaxInstreamAdV2));
                            if (!TextUtils.isEmpty(response.body().properties.vmaxIntstlAdAppOpenAdSpotId))
                                PrefUtils.getInstance().setPrefVmaxInterStitialOpenAdId(response.body().properties.vmaxIntstlAdAppOpenAdSpotId);
                            if (!TextUtils.isEmpty(response.body().properties.vmaxIntstlAdAppExitAdSpotId))
                                PrefUtils.getInstance().setPrefVmaxInterStitialExitAdId(response.body().properties.vmaxIntstlAdAppExitAdSpotId);
                            if (!TextUtils.isEmpty(response.body().properties.vmaxIntstlAdTabSwitchAdSpotId))
                                PrefUtils.getInstance().setPrefVmaxInterStitialTabSwitchAdId(response.body().properties.vmaxIntstlAdTabSwitchAdSpotId);
                            if (!TextUtils.isEmpty(response.body().properties.vmaxIntstlAdAppOpenEnabled))
                                PrefUtils.getInstance().setPrefEnableVmaxInterStitialAppOpenAd("true".equalsIgnoreCase(response.body().properties.vmaxIntstlAdAppOpenEnabled));
                            if (!TextUtils.isEmpty(response.body().properties.vmaxIntstlAdAppExitEnabled))
                                PrefUtils.getInstance().setPrefEnableVmaxInterStitialAppExitAd("true".equalsIgnoreCase(response.body().properties.vmaxIntstlAdAppExitEnabled));
                            if (!TextUtils.isEmpty(response.body().properties.vmaxIntstlAdTabSwitchEnabled))
                                PrefUtils.getInstance().setPrefEnableVmaxInterStitialTabswitchAd("true".equalsIgnoreCase(response.body().properties.vmaxIntstlAdTabSwitchEnabled));
                            if (!TextUtils.isEmpty(response.body().properties.publisherGroupIds_Android_v23))
                                PrefUtils.getInstance().setPrefpublisherGroupIds_Android(response.body().properties.publisherGroupIds_Android_v23);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.vmaxPreRollAdEnabled)) {
                            PrefUtils.getInstance().setPrefEnableVmaxPreRollAd("true".equalsIgnoreCase(response.body().properties.vmaxPreRollAdEnabled));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.vmaxPostRollAdEnabled)) {
                            PrefUtils.getInstance().setPrefEnableVmaxPostRollAd("true".equalsIgnoreCase(response.body().properties.vmaxPostRollAdEnabled));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.vmaxMidRollAdEnabled)) {
                            PrefUtils.getInstance().setPrefEnableVmaxMidRollAd("true".equalsIgnoreCase(response.body().properties.vmaxMidRollAdEnabled));
                        }

                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setPrefVmaxVideoAdMinDuration(response.body().properties.vmaxVideoAdMinDuration);
                        }

                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setPrefVmaxFooterBannerAdRefreshRate(response.body().properties.vmaxFooterBannerAdRefreshRate);
                        }
/*
                    if (!TextUtils.isEmpty(response.body().properties.vmaxNativeVideoAdSpotId)) {
                        PrefUtils.getInstance().setPrefVmaxNativeVideoAdId(response.body().properties.vmaxNativeVideoAdSpotId);
                    }*/

                        if (!TextUtils.isEmpty(response.body().properties.vmaxFooterBannerAdSpotId)) {
                            PrefUtils.getInstance().setPrefVmaxBannerAdId(response.body().properties.vmaxFooterBannerAdSpotId);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.vmaxInterstitialAdSpotId)) {
                            PrefUtils.getInstance().setPrefVmaxInterStitialAdId(response.body().properties.vmaxInterstitialAdSpotId);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.vmaxInstreamAdSpotId)) {
                            PrefUtils.getInstance().setPrefVmaxInStreamAdId(response.body().properties.vmaxInstreamAdSpotId);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableVmaxForPartners)) {
                            PrefUtils.getInstance().setPrefEnableVmaxForPartners(response.body().properties.enableVmaxForPartners);
                        }

                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setPrefVmaxNativeVideoAdRefreshRate(response.body().properties.vmaxNativeVideoAdRefreshRate);
                        }

                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setPrefVmaxNativeDisplayAdRefreshRate(response.body().properties.vmaxNativeDisplayAdRefreshRate);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.parentalControlEnabled)) {
                            PrefUtils.getInstance().setPrefEnableParentalControl("true".equalsIgnoreCase(response.body().properties.parentalControlEnabled));
                        }


                        if (!TextUtils.isEmpty(response.body().properties.vmaxNativeDisplayAdSpotId)) {
                            PrefUtils.getInstance().setPrefVmaxNativeDisplayAdId(response.body().properties.vmaxNativeDisplayAdSpotId);
                            String[] allTabStrings = response.body().properties.vmaxNativeDisplayAdSpotId.split(",");
                            for (String tabString :
                                    allTabStrings) {
                                String[] tabAdSpotIds = tabString.split(":");
                                propertiesNativeDisplayAdSpotIdMap.put(tabAdSpotIds[0], tabAdSpotIds[1]);
                            }
                            SDKLogger.debug("all tabs adspotIds- " + propertiesNativeDisplayAdSpotIdMap);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.vmaxNativeVideoAdSpotId)) {
                            PrefUtils.getInstance().setPrefVmaxNativeVideoAdId(response.body().properties.vmaxNativeVideoAdSpotId);
                            String[] allTabStrings = response.body().properties.vmaxNativeVideoAdSpotId.split(",");
                            for (String tabString :
                                    allTabStrings) {
                                String[] tabAdSpotIds = tabString.split(":");
                                propertiesNativeVideoAdSpotIdMap.put(tabAdSpotIds[0], tabAdSpotIds[1]);
                            }
                            SDKLogger.debug("all tabs adspotIds- " + propertiesNativeVideoAdSpotIdMap);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.enableBranchIOAnalytics)) {
                            PrefUtils.getInstance().setPrefEnableBranchIOAnalytics("true".equalsIgnoreCase(response.body().properties.enableBranchIOAnalytics));
                        }

                        if (!TextUtils.isEmpty(response.body().properties.branchIOPlaybackEventSeconds)) {
                            PrefUtils.getInstance().setPrefBranchIOPlaybackEventSeconds(response.body().properties.branchIOPlaybackEventSeconds);
                        }

                        if (!TextUtils.isEmpty(response.body().properties.searchResultsGroupAndroid)) {
                            PrefUtils.getInstance().setSearchResultsGroupName(response.body().properties.searchResultsGroupAndroid);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.portraitPlayerSuggestiosAndroid)) {
                            PrefUtils.getInstance().setPortraitPlayerSuggestios(response.body().properties.portraitPlayerSuggestiosAndroid);
                        }
                        if (!(TextUtils.isEmpty(response.body().properties.eventLoggerURL))) {
                            PrefUtils.getInstance().setEventLoggerUrl(response.body().properties.eventLoggerURL);
                        }

                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setPrefVmaxInterStitialAdFrequency(response.body().properties.interstitialAdFrequency);
                        }
                        try {
                            if (!(TextUtils.isEmpty(response.body().properties.promoAdJsonV23))) {
                                Gson gson = new Gson();
                                sPromoAdData = gson.fromJson(StringEscapeUtils.unescapeJava(response.body().properties.promoAdJsonV23), PromoAdData.class);
                                if (sPromoAdData != null) {
                                    APIConstants.promoAdDataPath = APIConstants.getPromoAdDataPath(mContext);
                                    SDKUtils.saveObject(sPromoAdData, APIConstants.promoAdDataPath);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!TextUtils.isEmpty(response.body().properties.PREVIEWS_DEVICE_HEIGHT)) {
                            PrefUtils.getInstance().setString(APIConstants.PREVIEWS_DEVICE_HEIGHT, response.body().properties.PREVIEWS_DEVICE_HEIGHT);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.fullScreenRestrictedContentTypes)) {
                            PrefUtils.getInstance().setFullScreenRestrictedContentTypes(response.body().properties.fullScreenRestrictedContentTypes);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.fullScreenRestrictedPGrpIds)) {
                            PrefUtils.getInstance().setFullScreenRestrictedContentTypesPublishingHouseIds(response.body().properties.fullScreenRestrictedPGrpIds);
                        }
                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setPrefVmaxInterStitialAdFrequency(response.body().properties.interstitialAdFrequency);
                        }

                        if (response.body().properties != null) {
                            Gson gson = new Gson();
                            sPreviewProperties = gson.fromJson(StringEscapeUtils.unescapeJava(response.body().properties.previewVideoConfigJsonv3), PreviewProperties.class);
                            if (sPreviewProperties != null) {
                                APIConstants.previewPropertiesPath = APIConstants.getPreviewPropertiesPath(mContext);
                                SDKUtils.saveObject(sPreviewProperties, APIConstants.previewPropertiesPath);
                            }
                        }

                        if (response.body().properties.androidBitrateCap != null && !response.body().properties.androidBitrateCap.isEmpty()) {
                            PrefUtils.getInstance().setString(APIConstants.ANDROID_BITRATE_CAP, response.body().properties.androidBitrateCap);
                        }

                        if (response.body().properties.mobileBitrateCapVod != null && !response.body().properties.mobileBitrateCapVod.isEmpty()) {
                            Gson gson1 = new Gson();
                            String bitrateConfig = StringEscapeUtils.unescapeJava(response.body().properties.mobileBitrateCapVod);
                            SDKLogger.debug(" mobileBitrateCapVod " + bitrateConfig);
                            BitrateCapForPlayer bitrateCapForPlayer = gson1.fromJson(response.body().properties.mobileBitrateCapVod, BitrateCapForPlayer.class);
                            setBitrateForDifferentVideoQuality(bitrateCapForPlayer, false);
                        }
                        if (response.body().properties.mobileBitrateCapLive != null && !response.body().properties.mobileBitrateCapLive.isEmpty()) {
                            Gson gson2 = new Gson();
                            String bitrateConfig = StringEscapeUtils.unescapeJava(response.body().properties.mobileBitrateCapLive);
                            SDKLogger.debug(" mobileBitrateCapLive " + bitrateConfig);
                            BitrateCapForPlayer bitrateCapForPlayer = gson2.fromJson(response.body().properties.mobileBitrateCapLive, BitrateCapForPlayer.class);
                            setBitrateForDifferentVideoQuality(bitrateCapForPlayer, true);
                        }

                        if (response.body().properties.sdAzureTracks != 0) {
                            if (BITRATE_CAP != null) {
                                BITRATE_CAP.put("vod_sd_max_track", response.body().properties.sdAzureTracks);
                            }
                        }
                        if (response.body().properties.sdLiveTracks != 0) {
                            if (BITRATE_CAP != null) {
                                BITRATE_CAP.put("live_sd_max_track", response.body().properties.sdLiveTracks);
                            }
                        }


                        if (!TextUtils.isEmpty(response.body().properties.aboutapp_url)) {
                            PrefUtils.getInstance().setAboutapp_url(response.body().properties.aboutapp_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.faq_url)) {
                            PrefUtils.getInstance().setFaq_url(response.body().properties.faq_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.help_url)) {
                            PrefUtils.getInstance().setHelpUrl(response.body().properties.help_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.privacy_policy_url)) {
                            PrefUtils.getInstance().setPrivacy_policy_url(response.body().properties.privacy_policy_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.support_url)) {
                            PrefUtils.getInstance().setSupportUrl(response.body().properties.support_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.tnc_url)) {
                            PrefUtils.getInstance().setTncUrl(response.body().properties.tnc_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.subscribedButtonTextDetailScreen)) {
                            PrefUtils.getInstance().setSubscribedString(response.body().properties.subscribedButtonTextDetailScreen);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.buyButtonTextDetailScreen)) {
                            PrefUtils.getInstance().setBuyString(response.body().properties.buyButtonTextDetailScreen);
                        }
                        if (response.body().properties != null && response.body().properties.signinFlow != null) {
                            PrefUtils.getInstance().setSignInFlowFlag(response.body().properties.signinFlow);
                        }
                        if (response.body().properties != null && response.body().properties.app_redirect_url != null) {
                            PrefUtils.getInstance().setAppUrlRedirectionUrl(response.body().properties.app_redirect_url);
                        }
                        if (response.body().properties != null && response.body().properties.googleAdBannerInPlayer != null) {
                            PrefUtils.getInstance().setPrefBannerBelowCoverPoster(response.body().properties.googleAdBannerInPlayer);
                        }
                        if (response.body().properties != null && response.body().properties.googleInterstitialAdUnitId != null) {
                            PrefUtils.getInstance().setInterstrialAdUnitId(response.body().properties.googleInterstitialAdUnitId);
                        }
                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setMaximumInterstrialAdClicksToShow(response.body().properties.googleInterstitialAdClicks);
                        }

                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setIsLightThemeEnabled(response.body().properties.isLightThemeEnabled);
                        }

                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setIsShowUpdateProfile(response.body().properties.show_update_profile);
                            PrefUtils.getInstance().setIsShowThumbnailPreview(response.body().properties.show_thumbnail_preview_seekbar);
                            PrefUtils.getInstance().setIsShowNotificationOption(response.body().properties.notificationEnable_Android);
                        }
//                        if (!TextUtils.isEmpty(response.body().properties.profileListV2Android)) {
//                            Gson profileApiJson = new Gson();
//                            Type profileApiListData = new TypeToken<ArrayList<ProfileAPIListAndroid>>() {
//                            }.getType();
//                            //String hell ="[{ \\\"displayName\\\": \\\"MOVIES\\\", \\\"viewAll\\\": true, \\\"type\\\": \\\"movie\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali\\\", \\\"publishingHouseId\\\": \\\"\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"releaseDate\\\" }, { \\\"displayName\\\": \\\"COMEDY CLIPS\\\", \\\"viewAll\\\": true, \\\"type\\\": \\\"vod\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali\\\", \\\"publishingHouseId\\\": \\\"46\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"1\\\" }, { \\\"displayName\\\": \\\"MUSIC VIDEOS\\\", \\\"viewAll\\\": true, \\\"type\\\": \\\"musicVideo\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali\\\", \\\"publishingHouseId\\\": \\\"\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"1\\\" }]";
//                             String hell1 = StringEscapeUtils.unescapeJava(response.body().properties.profileListV2Android);
//                            ArrayList<ProfileAPIListAndroid> profileApiList = profileApiJson.fromJson(hell1, profileApiListData);
//                            if (profileApiList != null && profileApiList.size() > 0) {
//                                APIConstants.profileAPIListAndroid = APIConstants.getProfileAPIListAndroid(mContext);
//                                SDKUtils.saveObject(profileApiList, APIConstants.profileAPIListAndroid);
//                            }
//                        }else{
//                            Gson profileApiJson = new Gson();
//                            Type profileApiListData = new TypeToken<ArrayList<ProfileAPIListAndroid>>() {
//                            }.getType();
////                            String hell ="[{ \\\"displayName\\\": \\\"MOVIES\\\", \\\"viewAll\\\": true, \\\"type\\\": \\\"movie\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali,hindi,english\\\", \\\"publishingHouseId\\\": \\\"\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"releaseDate\\\" }, { \\\"displayName\\\": \\\"TRAILERS\\\", \\\"viewAll\\\": true, \\\"type\\\": \\\"trailer\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali,hindi,english\\\", \\\"publishingHouseId\\\": \\\"\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"\\\" }, { \\\"displayName\\\": \\\"CLIPS\\\", \\\"viewAll\\\": true, \\\"type\\\": \\\"vod\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali,hindi,english\\\", \\\"publishingHouseId\\\": \\\"\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"\\\" }]";
//                            String hell ="[ { \\\"displayName\\\": \\\"MOVIES\\\", \\\"viewAll\\\": true, \\\"type\\\": \\\"movie\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali,hindi,english\\\", \\\"publishingHouseId\\\": \\\"\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"releaseDate\\\" }, {\\\"displayName\\\": \\\"TRAILERS\\\", \\\"viewAll\\\": false, \\\"type\\\": \\\"trailer\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali,hindi,english\\\", \\\"publishingHouseId\\\": \\\"\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"\\\" }, {\\\"displayName\\\": \\\"SONGS & INTERVIEWS\\\", \\\"viewAll\\\": false, \\\"type\\\": \\\"vod\\\", \\\"language\\\": \\\"tamil,telugu,malayalam,kannada,bengali,hindi,english\\\", \\\"publishingHouseId\\\": \\\"\\\", \\\"pageCount\\\": 10, \\\"layoutType\\\": \\\"horizontalListBigItem\\\", \\\"orderBy\\\": \\\"\\\" }]";
//                            String hell1 = StringEscapeUtils.unescapeJava(hell);
//                            ArrayList<ProfileAPIListAndroid> profileApiList = profileApiJson.fromJson(hell1, profileApiListData);
//                            if (profileApiList != null && profileApiList.size() > 0) {
//                                APIConstants.profileAPIListAndroid = APIConstants.getProfileAPIListAndroid(mContext);
//                                SDKUtils.saveObject(profileApiList, APIConstants.profileAPIListAndroid);
//                            }
//                        }
                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setAdMobEnabled(response.body().properties.isAdEnabled);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.adId)) {
                            PrefUtils.getInstance().setAdmobUnitId(response.body().properties.adId);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.refund_policy_url)) {
                            PrefUtils.getInstance().setPrefRefundPolicyUrl(response.body().properties.refund_policy_url);
                        }
                        if (!TextUtils.isEmpty(response.body().properties.playerControlsDefalut)) {
                            PrefUtils.getInstance().setplayerControlsDefalut(response.body().properties.playerControlsDefalut);
                        }
                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setSubtitlesEnabled(response.body().properties.areEnabledSubtitles_Android);
                        }
                        if (response.body().properties != null) {
                            PrefUtils.getInstance().setEditMobileNumberEnabled(response.body().properties.editMobileNumberEnable_Android);
                        }if (response.body().properties != null) {
                            PrefUtils.getInstance().setIsEnabledVideoAnalytics(response.body().properties.isEnabledVideoAnalytics_Android);
                        }
                        try {
                            if (response.body().properties.playerControlsBitrates != null) {
                                String bitrateConfig = StringEscapeUtils.unescapeJava(response.body().properties.playerControlsBitrates);
                                SDKLogger.debug(" bitrateConfig " + bitrateConfig);
                                PrefUtils.getInstance().setplayerControlsBitrates(bitrateConfig);
                                Gson gson = new Gson();
                                playerControlsBitrates = gson.fromJson(bitrateConfig, PlayerControlsBitrates.class);


                               /* if (playerControlsBitrates != null) {
                                    //   APIConstants.searchConfigDataPath = APIConstants.getSearchConfigDataPath(mContext);
                                    if (playerControlsBitrates.auto != null) {
                                        PrefUtils.getInstance().setplayerControlsBitratesAuto(playerControlsBitrates.auto);
                                    } else {
                                        PrefUtils.getInstance().setplayerControlsBitratesAuto("Auto");
                                    }
                                    if (playerControlsBitrates.low != null) {
                                        PrefUtils.getInstance().setplayerControlsBitratesLow(playerControlsBitrates.low);
                                    } else {
                                        PrefUtils.getInstance().setplayerControlsBitratesLow("Low");
                                    }
                                    if (playerControlsBitrates.medium != null) {
                                        PrefUtils.getInstance().setplayerControlsBitratesMedium(playerControlsBitrates.medium);
                                    } else {
                                        PrefUtils.getInstance().setplayerControlsBitratesMedium("Medium");
                                    }
                                    if (playerControlsBitrates.high != null) {
                                        PrefUtils.getInstance().setplayerControlsBitratesHigh(playerControlsBitrates.high);
                                    } else {
                                        PrefUtils.getInstance().setplayerControlsBitratesHigh("High");
                                    }
                                } else {
                                    PrefUtils.getInstance().setplayerControlsBitratesAuto("Auto");
                                    PrefUtils.getInstance().setplayerControlsBitratesLow("Low");
                                    PrefUtils.getInstance().setplayerControlsBitratesMedium("Medium");
                                    PrefUtils.getInstance().setplayerControlsBitratesHigh("High");
                                }*/
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            //       if (response.body().properties.mobileBitrateCapVod != null) {

                            if (response.body().properties.playerControlsBitratesV2 != null) {

                                Gson gson = new Gson();
                                mobileBitrateCapVod = gson.fromJson(response.body().properties.playerControlsBitratesV2, MobileBitrateCapVod.class);
                                QUALITY_MAP.put("vod_sd_auto_min", Integer.valueOf(mobileBitrateCapVod.sd_bitrate.auto.min));
                                QUALITY_MAP.put("vod_sd_auto_max", Integer.valueOf(mobileBitrateCapVod.sd_bitrate.auto.max));
                                QUALITY_MAP.put("vod_sd_low_min", Integer.valueOf(mobileBitrateCapVod.sd_bitrate.low.min));
                                QUALITY_MAP.put("vod_sd_low_max", Integer.valueOf(mobileBitrateCapVod.sd_bitrate.low.max));
                                QUALITY_MAP.put("vod_sd_medium_min", Integer.valueOf(mobileBitrateCapVod.sd_bitrate.medium.min));
                                QUALITY_MAP.put("vod_sd_medium_max", Integer.valueOf(mobileBitrateCapVod.sd_bitrate.medium.max));
                                QUALITY_MAP.put("vod_sd_hd_min", Integer.valueOf(mobileBitrateCapVod.sd_bitrate.hd.min));
                                QUALITY_MAP.put("vod_sd_hd_max", Integer.valueOf(mobileBitrateCapVod.sd_bitrate.hd.max));

                                QUALITY_MAP.put("vod_hd_auto_min", Integer.valueOf(mobileBitrateCapVod.hd_bitrate.auto.min));
                                QUALITY_MAP.put("vod_hd_auto_max", Integer.valueOf(mobileBitrateCapVod.hd_bitrate.auto.max));
                                QUALITY_MAP.put("vod_hd_low_min", Integer.valueOf(mobileBitrateCapVod.hd_bitrate.low.min));
                                QUALITY_MAP.put("vod_hd_low_max", Integer.valueOf(mobileBitrateCapVod.hd_bitrate.low.max));
                                QUALITY_MAP.put("vod_hd_medium_min", Integer.valueOf(mobileBitrateCapVod.hd_bitrate.medium.min));
                                QUALITY_MAP.put("vod_hd_medium_max", Integer.valueOf(mobileBitrateCapVod.hd_bitrate.medium.max));
                                QUALITY_MAP.put("vod_hd_hd_min", Integer.valueOf(mobileBitrateCapVod.hd_bitrate.hd.min));
                                QUALITY_MAP.put("vod_hd_hd_max", Integer.valueOf(mobileBitrateCapVod.hd_bitrate.hd.max));
                            }
                            PrefUtils.getInstance().setQUALITY_MAP(QUALITY_MAP);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    if (response.body().commonParams != null && response.body().commonParams.size() > 0) {
                        PrefUtils.getInstance().setCommonParamsData(response.body().commonParams);
                    }
                    if (!TextUtils.isEmpty(response.body().properties.app_action_redirect)) {
                        PrefUtils.getInstance().setAppAcionRedirect(response.body().properties.app_action_redirect);
                    }
                    if (!TextUtils.isEmpty(response.body().properties.playerWatermarkURL)) {
                        PrefUtils.getInstance().setPlayerWatermarkURL(response.body().properties.playerWatermarkURL);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                try {
                    SDKLogger.debug("Error: " + t.getMessage());
                    t.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        APIService.getInstance().execute(propertiesRequest);
    }

    public void setBitrateForDifferentVideoQuality(BitrateCapForPlayer bitrateCapForPlayer, boolean forLive) {
        if (bitrateCapForPlayer != null) {
            if (!forLive) {
                if (bitrateCapForPlayer.sd_bitrate != null) {
                    if (bitrateCapForPlayer.sd_bitrate.auto != null) {
                        BITRATE_CAP.put("vod_sd_auto_max", bitrateCapForPlayer.sd_bitrate.auto.max);
                        BITRATE_CAP.put("vod_sd_auto_min", bitrateCapForPlayer.sd_bitrate.auto.min);
                    }
                    if (bitrateCapForPlayer.sd_bitrate.hd != null) {
                        BITRATE_CAP.put("vod_sd_hd_max", bitrateCapForPlayer.sd_bitrate.hd.max);
                        BITRATE_CAP.put("vod_sd_hd_min", bitrateCapForPlayer.sd_bitrate.hd.min);
                    }
                }

                if (bitrateCapForPlayer.sd_bitrate != null) {
                    if (bitrateCapForPlayer.sd_bitrate.medium != null) {
                        BITRATE_CAP.put("vod_sd_medium_max", bitrateCapForPlayer.sd_bitrate.medium.max);
                        BITRATE_CAP.put("vod_sd_medium_min", bitrateCapForPlayer.sd_bitrate.medium.min);
                    }
                    if (bitrateCapForPlayer.sd_bitrate.low != null) {
                        BITRATE_CAP.put("vod_sd_low_max", bitrateCapForPlayer.sd_bitrate.low.max);
                        BITRATE_CAP.put("vod_sd_low_min", bitrateCapForPlayer.sd_bitrate.low.min);
                    }
                }
                if (bitrateCapForPlayer.hd_bitrate != null) {
                    if (bitrateCapForPlayer.hd_bitrate.auto != null) {
                        BITRATE_CAP.put("vod_hd_auto_max", bitrateCapForPlayer.hd_bitrate.auto.max);
                        BITRATE_CAP.put("vod_hd_auto_min", bitrateCapForPlayer.hd_bitrate.auto.min);
                    }
                    if (bitrateCapForPlayer.hd_bitrate.hd != null) {
                        BITRATE_CAP.put("vod_hd_hd_max", bitrateCapForPlayer.hd_bitrate.hd.max);
                        BITRATE_CAP.put("vod_hd_hd_min", bitrateCapForPlayer.hd_bitrate.hd.min);
                    }
                }
                if (bitrateCapForPlayer.hd_bitrate != null) {
                    if (bitrateCapForPlayer.hd_bitrate.medium != null) {
                        BITRATE_CAP.put("vod_hd_medium_max", bitrateCapForPlayer.hd_bitrate.medium.max);
                        BITRATE_CAP.put("vod_hd_medium_min", bitrateCapForPlayer.hd_bitrate.medium.min);
                    }
                    if (bitrateCapForPlayer.hd_bitrate.low != null) {
                        BITRATE_CAP.put("vod_hd_low_max", bitrateCapForPlayer.hd_bitrate.low.max);
                        BITRATE_CAP.put("vod_hd_low_min", bitrateCapForPlayer.hd_bitrate.low.min);
                    }
                }
            } else {
                if (bitrateCapForPlayer.sd_bitrate != null) {
                    if (bitrateCapForPlayer.sd_bitrate.auto != null) {
                        BITRATE_CAP.put("live_sd_auto_max", bitrateCapForPlayer.sd_bitrate.auto.max);
                        BITRATE_CAP.put("live_sd_auto_min", bitrateCapForPlayer.sd_bitrate.auto.min);
                    }
                    if (bitrateCapForPlayer.sd_bitrate.hd != null) {
                        BITRATE_CAP.put("live_sd_hd_max", bitrateCapForPlayer.sd_bitrate.hd.max);
                        BITRATE_CAP.put("live_sd_hd_min", bitrateCapForPlayer.sd_bitrate.hd.min);
                    }
                }
                if (bitrateCapForPlayer.sd_bitrate != null) {
                    if (bitrateCapForPlayer.sd_bitrate.medium != null) {
                        BITRATE_CAP.put("live_sd_medium_max", bitrateCapForPlayer.sd_bitrate.medium.max);
                        BITRATE_CAP.put("live_sd_medium_min", bitrateCapForPlayer.sd_bitrate.medium.min);
                    }
                    if (bitrateCapForPlayer.sd_bitrate.low != null) {
                        BITRATE_CAP.put("live_sd_low_max", bitrateCapForPlayer.sd_bitrate.low.max);
                        BITRATE_CAP.put("live_sd_low_min", bitrateCapForPlayer.sd_bitrate.low.min);
                    }
                }
                if (bitrateCapForPlayer.hd_bitrate != null) {
                    if (bitrateCapForPlayer.hd_bitrate.auto != null) {
                        BITRATE_CAP.put("live_hd_auto_max", bitrateCapForPlayer.hd_bitrate.auto.max);
                        BITRATE_CAP.put("live_hd_auto_min", bitrateCapForPlayer.hd_bitrate.auto.min);
                       /* PropertiesHandler.BITRATE_CAP.put("live_hd_auto_max", 4000);
                        PropertiesHandler.BITRATE_CAP.put("live_hd_auto_min", 800);*/
                    }
                    if (bitrateCapForPlayer.hd_bitrate.hd != null) {
                        BITRATE_CAP.put("live_hd_hd_max", bitrateCapForPlayer.hd_bitrate.hd.max);
                        BITRATE_CAP.put("live_hd_hd_min", bitrateCapForPlayer.hd_bitrate.hd.min);

                        /*PropertiesHandler.BITRATE_CAP.put("live_hd_hd_max", 4000);
                        PropertiesHandler.BITRATE_CAP.put("live_hd_hd_min", 2000);*/


                    }
                }
                if (bitrateCapForPlayer.hd_bitrate != null) {
                    if (bitrateCapForPlayer.hd_bitrate.medium != null) {
                        BITRATE_CAP.put("live_hd_medium_max", bitrateCapForPlayer.hd_bitrate.medium.max);
                        BITRATE_CAP.put("live_hd_medium_min", bitrateCapForPlayer.hd_bitrate.medium.min);
                      /*  PropertiesHandler.BITRATE_CAP.put("live_hd_medium_max", 1900);
                        PropertiesHandler.BITRATE_CAP.put("live_hd_medium_min", 1000);*/
                    }
                    if (bitrateCapForPlayer.hd_bitrate.low != null) {
                        BITRATE_CAP.put("live_hd_low_max", bitrateCapForPlayer.hd_bitrate.low.max);
                        BITRATE_CAP.put("live_hd_low_min", bitrateCapForPlayer.hd_bitrate.low.min);
                     /*   PropertiesHandler.BITRATE_CAP.put("live_hd_low_max", 1000);
                        PropertiesHandler.BITRATE_CAP.put("live_hd_low_min", 800);*/
                    }
                }
            }

            for (int i = 0; i < BITRATE_CAP.size(); i++) {
                String key = (String) BITRATE_CAP.keyAt(i);
                int value = (int) BITRATE_CAP.valueAt(i);
                Log.e("Event", "key - " + key + " value - " + value);
            }
        }

    }

    private void addEpgListParamsToPropertiesMap(String epgListAPIParams) {
        SDKLogger.debug("addEpgListParamsToPropertiesMap epgListAPIParams- " + epgListAPIParams);
        if (epgListAPIParams == null || epgListAPIParams.isEmpty()) {
            return;
        }
        try {
            String[] paramsList = epgListAPIParams.split("&");
            for (String param :
                    paramsList) {
                String[] keyNvalue = param.split("=");
                SDKLogger.debug("addEpgListParamsToPropertiesMap keyNvalue[0]- " + keyNvalue[0] + " keyNvalue[1]- " + keyNvalue[1]);
                if (keyNvalue[0] != null)
                    propertiesEPGListAPIParams.put(keyNvalue[0].replaceAll(" ", ""), keyNvalue[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static LoginProperties getAvailableLoginProperties() {
        return loginProperties;
    }

    public void setLoginProperties(LoginProperties loginProperties) {
        this.loginProperties = loginProperties;
    }


    private void fetchAvailableLoginTypes() {
        AvailableLoginsProperties.Params params = new AvailableLoginsProperties.Params(mContext.getString(R.string.clientSecrete));
        AvailableLoginsProperties availableLogins = new AvailableLoginsProperties(params, new APICallback<AvailableLoginsPropertiesData>() {
            @Override
            public void onResponse(APIResponse<AvailableLoginsPropertiesData> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                setLoginProperties(response.body().properties);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
            }
        });

        APIService.getInstance().execute(availableLogins);
    }

    public static PromoAdData getPromoAdData(Context context) {
        if (sPromoAdData == null) {
            APIConstants.promoAdDataPath = APIConstants.getPromoAdDataPath(context);
            sPromoAdData = (PromoAdData) SDKUtils.loadObject(APIConstants.promoAdDataPath);
        }
        return sPromoAdData;
    }

    public static PreviewProperties getPreviewProperties(Context context) {
        if (sPreviewProperties == null) {
            APIConstants.previewPropertiesPath = APIConstants.getPromoAdDataPath(context);
            sPromoAdData = (PromoAdData) SDKUtils.loadObject(APIConstants.previewPropertiesPath);
        }
        return sPreviewProperties;
    }
}
