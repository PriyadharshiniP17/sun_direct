package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by phani on 2/2/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertiesRes implements Serializable{
    public String profile_WIFI;
    public String profile_5G;
    public String profile_4G;
    public String profile_3G;
    public String profile_2G;
    public int version;
    public String link;
    public String type;
    public String message;

    public int maxDisplayCountTimeshiftHelp;
    public String enableHungamaSDK;
    public String mixpanelEventPriority;
    public String cleverTapEventPriority;
    public String enableMyPacksScreen;
    public String enableHungamaLogo;
    public String enableDittoChannelLogoOnEpg;
    public String dittoChannelLogoImageUrl;
    public String enableHungamaRentTag;
    public String enablePastEpg;
    public String enablePrefLangScreen;
    public int noOfPastEpgDays;
    public String messageFailedToFetchOfferPacks;
    public String liveHLSParamSkipLiveWindow;
    public String hooqBgsectionColor;
    public String hooqLogoImageUrl;
    public String enabledAdSONYLIV;
    public String enabledAdVAST3;
    public String adProviderTagVAST3;
    public String adProviderTagOOYALA;
    public String enableMusicTab;
    public String allowWiFiNetworkForPayment;
    public int OTPDetectionTime;
    public String manualOTPallow;
    public String enableSkipButtonOnOTP;
    public String shareMessage;
    public String erosnowMusicLogoImageUrl;
    public String shareLink;
    public String enableSonyChannelLogo;
    public String sonyChannelLogoImageUrl;
    public String enabledAdSONYLIV_v2;
    public String showAllPackagesOffersScreen;
    public String epgListAPIParams;
    public String enableHelpScreen;
    public String enableMixpanel;
    public String enableCleverTap;
    public String chromeCastRecieverId;
    public String enableOnBoardingScreen;
    public String yuptvChannelLogoImageUrl;
    public String enableYupptvChannelLogoOnEpg;
    public String enableMixpanelV2;
    public String enableAltBalajiDownload;
    public String enableHooqDownload;
    public String enableErosnowDownload;
    public String shareMessage_menu;
    public String shareLink_menu;
    public String vmax_ad_refresh_rate;
    public String vmaxNativeAdSpotId;
    public String vmaxNativeDisplayAdSpotId;
    public String vmaxNativeVideoAdSpotId;
    public String negativeButtonText;
    public String positiveButtonText;
    public String alertTitle;
    public String enabledErosnowPlayerLogs;
    public String playerLogsEnabledTo;
    public String enableHungamaDownload;
    public String enableHooqChromecast;
    public String standardPackageIds;
    public String enableErosnowDownloadV1;
    public String msgDownloadDRMLicenseFailure;
    public String messageNOSPAvailable;
    public String downldStateNOSPAvailable;
    public String downldStateDRMLicenseFailed;
    public String downldStateFailedDueToLowMem;
    public String enableHungamaDownloadV1;
    public String enableHungamaDownloadV3;
    public String appsflyerPlaybackEventSeconds;
    public String bannerScrollFrequency;
    public String nextEpisodePopupPercentage;
    public String playerSeekTimeSeconds;
    public String vmax_ad_button_color;
    public String vmax_layout_bg_color;
    public String vmax_ad_header_font_color;
    public String emailRequiredForPartners;
    public String autoLoginFailedMessage;
    public String showPrivacyConsent;
    public String privacyConsentMessage;

    public String enableVmaxFooterBannerAd;
    public String enableVmaxInterstitialAdV2;
    public String enableVmaxInstreamAdV2;
    public String vmaxIntstlAdAppOpenEnabled;
    public String vmaxIntstlAdAppExitEnabled;
    public String vmaxIntstlAdAppExitAdSpotId;
    public String vmaxIntstlAdAppOpenAdSpotId;
    public String vmaxIntstlAdTabSwitchEnabled;
    public String vmaxIntstlAdTabSwitchAdSpotId;

    public String vmaxFooterBannerAdSpotId;
//    public String vmaxNativeVideoAdSpotId;
    public String vmaxInterstitialAdSpotId;
    public String vmaxInstreamAdSpotId;

    public String vmaxPreRollAdEnabled;
    public String vmaxMidRollAdEnabled;
    public String vmaxPostRollAdEnabled;

    public int vmaxVideoAdMinDuration = 10;
    public int vmaxFooterBannerAdRefreshRate = 30;
    public int vmaxNativeVideoAdRefreshRate = 30;
    public int vmaxNativeDisplayAdRefreshRate = 30;
    public String enableVmaxForPartners;
    public String vmaxAdHeaderSecondaryFontColor;
    public String parentalControlEnabled;
    public String vmaxLayoutBackgroundBorderColor;
    public String enableBranchIOAnalytics;
    public String branchIOEventPriority;
    public String branchIOPlaybackEventSeconds;
    public String eventLoggerEnabled;
    public String eventLoggerURL;
    public int interstitialAdFrequency;
    public String promoVideoAdFrequencies;
    public String promoAdHtmlURL;
    public String promoAdJsonV23;
    public String partnerDetails;
    public String partnerDetailsV23;
    public String partnerDetailsV23Android;
    public String partnerDetailsV51;
    public String setupBoxList;
    public String adFullScreenConfig;
    public String adPopUpNotification;
    public String adPopUpNotificationV2;
    public String searchResultsGroupAndroid;
    public String portraitPlayerSuggestiosAndroid;

    public String categoryScreenFiltersV23;
    public String publisherGroupIds_Android_v23;
    public String rating_screen_json;
    public String hooq_sdk_enabled;
    public String searchConfig;

    public boolean enableRemoteConfigAndroid;
    public boolean enableMediaSubDomain = false;
    public String vernacular_strings_api;
    public String app_languages_api;
    public String fullScreenRestrictedContentTypes;
    public String fullScreenRestrictedPGrpIds;
    public String forceAcceptLanguage;
    public String supportpage_url;
    public String contact_us_page_url;
    public String search_error;
    public String previewVideoConfigJsonv3;
    public String PREVIEWS_DEVICE_HEIGHT;
    public String signinFlow;
    public String serviceContentConfig;
    public String aboutapp_url;
    public String help_url;
    public String privacy_policy_url;
    public String support_url;
    public String tnc_url;
    public String faq_url;
    public String profileAPIListAndroid;
    public String profileListV2Android;
    public  String buyButtonTextDetailScreen;
    public  String subscribedButtonTextDetailScreen;
    public  boolean legacyUpgradePopup;
    public String app_redirect_url;

    public String adId;
    public boolean isAdEnabled;
    public String refund_policy_url;
    public String app_action_redirect;
    public String playerWatermarkURL;
    public String playerControlsBitrates;
    public String playerControlsBitratesV2;
    public String playerControlsDefalut;

    public String androidBitrateCap;
    public String mobileBitrateCapVod;
    public String mobileBitrateCapLive;
    public int sdLiveTracks;
    public int sdAzureTracks;

    public String bufferConfigAndroid;
    public String defaultProfileImage;

    public String thumbnailPreviewPlaybackEnabled= "registered";
    public String adLayoutsProperties;
    public String contentRatingConfig;
    public String googleAdBannerInPlayer;
    public String googleInterstitialAdUnitId;
    public int googleInterstitialAdClicks;
    public boolean isLightThemeEnabled;
    public String share_deep_link_url;
    public String share_url;
    public String app_languages_api_v2;
    public String otpLength;
    public String  exploreOfferPageLink;
    public String Enable_NewUser_SubscribeToApps ;
    public String  subscribeToAppsPageLink;
    public boolean show_update_profile = false;
    public boolean show_thumbnail_preview_seekbar = false;
    public boolean notificationEnable_Android = false;
    public boolean areEnabledSubtitles_Android = false;
    public boolean editMobileNumberEnable_Android = false;
    public boolean isEnabledVideoAnalytics_Android = false;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( " PropertiesRes {" );
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                //requires access to private field:
                result.append( field.get(this) );
            } catch ( IllegalAccessException ex ) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }
}
