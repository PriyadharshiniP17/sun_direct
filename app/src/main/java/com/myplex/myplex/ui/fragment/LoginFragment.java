package com.myplex.myplex.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.BuildConfig;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.github.pedrovgs.LoggerD;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.OTTAppRequest;
import com.myplex.api.request.user.DeviceRegistrationEncryptedShreyas;
import com.myplex.api.request.user.GenerateKeyRequest;
import com.myplex.api.request.user.MSISDNLogin;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.api.request.user.UserConsentUrl;
import com.myplex.api.request.user.UserProfileRequest;
import com.myplex.model.AppLoginConfigData;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardDataOttImagesItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.DeviceRegData;
import com.myplex.model.MsisdnData;
import com.myplex.model.OTTApp;
import com.myplex.model.OTTAppData;
import com.myplex.model.OfferResponseData;
import com.myplex.model.UserProfileResponseData;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.MsisdnRetrivalEngine;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.animationviewpager.SliderLayout;
import com.myplex.myplex.animationviewpager.ViewPagerEx;
import com.myplex.myplex.gcm.MyGcmListenerService;
import com.myplex.myplex.gcm.RegistrationJobScheduler;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.OTTAppsImageSliderAdapter;
import com.myplex.myplex.ui.views.PrivacyPolicyDialogWebView;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.FragmentOTPVerification;
import com.myplex.myplex.utils.FragmentSignIn;
import com.myplex.myplex.utils.FragmentSignUp;
import com.myplex.myplex.utils.LongThread;
import com.myplex.myplex.utils.MainActivityLauncherUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLPeerUnverifiedException;

import jp.wasabeef.blurry.Blurry;

import static com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription.IS_PROFILE_UPDATE_SUCCESS;
import static com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription.PROFILE_UPDATE_REQUEST;

public class LoginFragment extends BaseFragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private static final String FLAG_AUTO_LOGIN = "auto";
    private static final String FLAG_MSISDN = "msisdn";
    private static final int SPLASH_TIME = 1 * 1000;
    private static final int SHOW_HOME_FROM_ON_BOARDING = 3 * 1000;
    private static final int ONBOARDING_IMAGE_DOWNLODING_TIME_OUT = 30 * 1000;
    private Context mContext;

    private MSISDNLogin login;
    private DeviceRegistrationEncryptedShreyas deviceRegistration;
    private GenerateKeyRequest generateKeyRequest;
    private ProgressBar mProgressBar;
    private boolean mIsLoginDuringBrowse;
    private boolean isFromSplash = false;
    private boolean isVideoStarted = false;
    private boolean showPackagesDuringBrowse;
    private ImageView centerIcon;
    private com.yqritc.scalablevideoview.ScalableVideoView videoView;
    //    private AutoScrollViewPager mViewPager;
//    private RelativeLayout mViewPagerContainer;
//    private CircleIndicator mIndicator;
//    private TextView mDescriptionTxt;
    private DeviceRegData mDeviceRegData;
    private List<OTTAppsImageSliderAdapter.SliderModel> mSliderItems = new ArrayList<>();
    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            progressCount++;
            LoggerD.debugLog("showOnBoardingScreen: handleMessage: progressCount- " + progressCount + " obj- " + message.obj);
            mSliderItems.get(message.what).bitmap = (Bitmap) message.obj;
            LoggerD.debugLog("showOnBoardingScreen: handleMessage: isTimeOutReached- " + isTimeOutReached);
            if (isTimeOutReached && mThreadPoolExecutor != null) {
                mThreadPoolExecutor.shutdownNow();
                LoggerD.debugLog("showOnBoardingScreen: time out is reached shutting down all images downloading task");
            }
            if (progressCount == mSliderItems.size() && !isTimeOutReached) {
                showBanners(mSliderItems);
            }
            return false;
        }
    };
    private SliderLayout mSliderLayout;
    private ImageView mBlurredBackground;
    private int progressCount;
    private View rootView;
    private boolean isOnBoardingTaskCompleted;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private boolean isTimeOutReached;
    private String mSource;
    private String mSourceDetails;
    private WebView webView;
    private WebView errorWebView;

    private void showBanners(final List<OTTAppsImageSliderAdapter.SliderModel> mSliderItems) {
        isOnBoardingTaskCompleted = true;
        mSliderLayout.addOnPageChangeListener(new ViewPagerEx.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mSliderItems == null) {
                    return;
                }
                if (position == mSliderItems.size() - 1) {
                    if (mSliderLayout != null) {
                        mSliderLayout.setPagingEnabled(false);
                        mSliderLayout.removeOnPageChangeListener(this);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doLogin();
                        }
                    }, SPLASH_TIME);
                }
                if (DeviceUtils.isTablet(mContext)) {
                    Blurry.with(mContext).from(mSliderItems.get(position).bitmap).into(mBlurredBackground);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mSliderLayout.setData(mSliderItems);
        mSliderLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        mSliderLayout.setDuration(SHOW_HOME_FROM_ON_BOARDING);
        if (mSliderItems != null && !mSliderItems.isEmpty() && DeviceUtils.isTablet(mContext)) {
            Blurry.with(mContext).from(mSliderItems.get(0).bitmap).into(mBlurredBackground);
        }
    }


    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        mSliderLayout =  rootView.findViewById(R.id.slider);
        mBlurredBackground =  rootView.findViewById(R.id.image_blur_bg);
        mProgressBar =  rootView.findViewById(R.id.card_loading_progres_bar);
        centerIcon =  rootView.findViewById(R.id.splash_center_icon);
        videoView = rootView.findViewById(R.id.video_view);
        webView = rootView.findViewById(R.id.webView);
        errorWebView = rootView.findViewById(R.id.errorWebView);
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            ((LoginActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            ((LoginActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String shouldEnableDirectAppLaunch = PrefUtils.getInstance().getSignInFlowFlag();
        if(shouldEnableDirectAppLaunch != null){
            if(shouldEnableDirectAppLaunch.equalsIgnoreCase(APIConstants.SIGN_IN_FLOW_ON_PLAY)){
                ApplicationController.ENABLE_DIRECT_APP_LAUNCH = true;
            }else if(shouldEnableDirectAppLaunch.equalsIgnoreCase(APIConstants.SIGN_IN_FLOW_ON_APP_OPEN)){
                ApplicationController.ENABLE_DIRECT_APP_LAUNCH = false;
            }
        }
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(UiUtil.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        readBundle();
        return rootView;
    }

    private void readBundle() {
        Bundle args = getArguments();
        if (args == null || args.isEmpty()) {
            return;
        }
        mIsLoginDuringBrowse = false;
        if (args.containsKey(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE)) {
            mIsLoginDuringBrowse = getArguments().getBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE);
        }
        if (args.containsKey("isFromSplash")) {
            isFromSplash = getArguments().getBoolean("isFromSplash");
        } else
            isFromSplash = true;

        if (!Util.checkUserLoginStatus())
            mIsLoginDuringBrowse = true;

        showPackagesDuringBrowse = false;
        if (args.containsKey(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE)) {
            showPackagesDuringBrowse = getArguments().getBoolean(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE);
        }
        mSource = null;
        if (args.containsKey(Analytics.PROPERTY_SOURCE)) {
            mSource = getArguments().getString(Analytics.PROPERTY_SOURCE);
        }

        mSourceDetails = null;
        if (args.containsKey(Analytics.PROPERTY_SOURCE_DETAILS)) {
            mSourceDetails = getArguments().getString(Analytics.PROPERTY_SOURCE_DETAILS);
        }
        if(isFromSplash) {
            int videoId = getResources().getIdentifier("splashvideo", "raw", mContext.getPackageName());
            if (videoId != 0) {
                String videoPath = "android.resource://" + mContext.getPackageName() + "/" + videoId;
//                    videoView.setVideoURI(Uri.parse(videoPath));
                try {
                    videoView.setRawData(videoId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                videoView.prepare(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        videoView.start();
                    }
                });
            } catch (IOException ioe) {
                //ignore
            }
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // initUI(savedInstanceState);
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        if (ApplicationController.ENABLE_USER_PRIVACY_POLICY_ALERT
                && PrefUtils.getInstance().getPrefShowPrivacyConsent()
                && ConnectivityUtil.isConnected(mContext)) {
            showPrivacyPolicyUpdate(getActivity());
            return;
        }
        //getUserConsent();
        proceedDeviceRegistration();
    }

    private void proceedDeviceRegistration() {
        //TODO need to go through hooq sdk initialization also handle the case like after offer consumption only need to start processing the notification data.
        if (PrefUtils.getInstance().getPrefOfferPackSubscriptionStatus()
                || PrefUtils.getInstance().getPrefIsSkipPackages()
                || ApplicationController.ENABLE_DIRECT_APP_LAUNCH) {
            if (Util.onHandleExternalIntent(getActivity())) {
                getActivity().finish();
                return;
            }
        }
        updateRemoteConfig();
        checkDeviceRegDetails();
    }

    private void updateRemoteConfig() {
        ApplicationController.getmFirebaseRemoteConfig().fetch(0)
                .addOnCompleteListener(mOnCompleteListener)
                .addOnFailureListener(mOnFailureListener);
    }

    private void checkDeviceRegDetails() {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        final String clientKeyExp = PrefUtils.getInstance().getPrefClientkeyExpiry();
        LoggerD.debugLog("existing clientKey- " + clientKey);
        if (clientKey == null) {
            if (!ApplicationController.ENABLE_OTP_LOGIN) {
                registerDevice();
                return;
            }
            registerDeviceEncryption();
            return;
        }
        fetchRequiredData();
        if (APIConstants.msisdnPath == null) {
            APIConstants.msisdnPath = mContext.getFilesDir() + "/" + "msisdn.bin";
        }
//        Analytics.createScreenGA(Analytics.SCREEN_SPLASH);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_SPLASH);
        if (Util.isTokenValid(clientKeyExp)) {
            if (ApplicationController.ENABLE_DIRECT_APP_LAUNCH && ApplicationController.ENABLE_OTP_LOGIN) {
                if (mIsLoginDuringBrowse) {
                    if (!Util.checkUserLoginStatus()) {
                        String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
                        //Log.d(TAG, "Pref msisdn " + msisdn);
                        showSignInScreenFragment(msisdn, false, mIsLoginDuringBrowse);
                        return;
                        /*if (ApplicationController.ENABLE_OTP_LOGIN) {
                            showOTPLoginScreenFragment(msisdn, false, mIsLoginDuringBrowse);
                            return;
                        }
                        if (mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
                            makeMSISDNLogin(msisdn, "", false);
                        } else {
                            makeMSISDNLogin(null, "", false);
                        }*/
                    } else {
                        String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
                        //Log.d(TAG, "Pref msisdn " + msisdn);
                        if (ApplicationController.ENABLE_OTP_LOGIN) {
                            boolean isExistingUser = false;
                            if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefEmailID()) && (!PrefUtils.getInstance().getPrefIsOTPSkipped() || mIsLoginDuringBrowse)) {
                                isExistingUser = true;
                            }
                            showOTPLoginScreenFragment(msisdn, isExistingUser, mIsLoginDuringBrowse);
                            return;
                        }
                        if (mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
                            makeMSISDNLogin(msisdn, "", false);
                        } else {
                            makeMSISDNLogin(null, "", false);
                        }
                    }
                }
                LoggerD.debugLog("direct launch and otp enabled launching home now");
                /*if (!Util.checkUserLoginStatus() && getResources().getBoolean(R.bool.is_login_check_request_enabled) && ConnectivityUtil.isConnectedMobile(mContext)) {
                    makeSignInCheck();
                    return;
                }*/
                launchMainActivity();
                return;
            }
            if (Util.checkUserLoginStatus()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoggerD.debugOTP("executing after delay");
                        if (!isAdded()) {
                            return;
                        }
                        if (ApplicationController.ENABLE_OTP_LOGIN) {
                            if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefEmailID()) && (!PrefUtils.getInstance().getPrefIsOTPSkipped() || mIsLoginDuringBrowse)) {
                                String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
                                showOTPLoginScreenFragment(msisdn, true, mIsLoginDuringBrowse);
                                return;
                            }
                        }

                        if (showPackagesDuringBrowse) {
                            showPackagesScreen();
                            return;
                        }
                        if (ApplicationController.FLAG_ENABLE_OFFER_SUBSCRIPTION) {
                            if (!PrefUtils.getInstance().getPrefOfferPackSubscriptionStatus()
                                    && !PrefUtils.getInstance().getPrefIsSkipPackages()) {
                                // mBaseActivity.pushFragment(new OfferedPacksFragment());
                                fetchOfferAvailability(false);
                                return;
                            }
                        }
                        launchMainActivity();
                    }


                }, SPLASH_TIME);

            } else {
                if (ApplicationController.ENABLE_ON_BOARDING && PrefUtils.getInstance().getPrefEnableOnBoardingScreen()) {
                    showOnBoardingScreen();
                    return;
                }
                String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
                //Log.d(TAG, "Pref msisdn " + msisdn);
                if (ApplicationController.ENABLE_OTP_LOGIN) {
                    showOTPLoginScreenFragment(msisdn, false, mIsLoginDuringBrowse);
                    return;
                }
                if (mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
                    makeMSISDNLogin(msisdn, "", false);
                } else {
                    makeMSISDNLogin(null, "", false);
                }
                return;
            }

          /*  new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getActivity().finish();
                    launchActivity();
                }
            }, SPLASH_TIME);
            return;*/
        } else {
            //clientKey is expired , Generate new Key
            makeReGenerateKeyRequest();
        }

    }

    private void makeSignInCheck() {
        LoggerD.debugLog("checking for user sign in details");
        MSISDNLogin.Params msisdnParams = new MSISDNLogin.Params(null, null);

        MSISDNLogin login = new MSISDNLogin(msisdnParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
                        LoggerD.debugLog("response- " + response);
                        if (!isAdded()) {
                            LoggerD.debugLog("fragment is removed");
                            return;
                        }
                        if (response == null
                                || response.body() == null) {
                            // login failed
                            LoggerD.debugOTP("invalid response");
                            launchMainActivity();
                            return;
                        }

                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.ACCOUNT_TYPE, "");
                        if (!TextUtils.isEmpty(response.body().userid)) {
                            params.put(Analytics.USER_ID, response.body().userid);
                        }
                        if (response.body().status != null
                                && response.body().status.equalsIgnoreCase("SUCCESS")
                                && (response.body().code == 200
                                || response.body().code == 201)) {
                            myplexAPI.clearCache(APIConstants.BASE_URL);
                            PropertiesHandler.clearCategoryScreenFilter();
                            if(!TextUtils.isEmpty(response.body().serviceName)){
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }
                            CleverTap.eventRegistrationCompleted(response.body().email, response.body().mobile, Analytics.YES);
                            PrefUtils.getInstance().setPrefLoginStatus("success");

                            if (ApplicationController.FLAG_ENABLE_OFFER_SUBSCRIPTION) {

                                if (response.body().mobile != null && !response.body().mobile.isEmpty()) {
                                    PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
                                    if (response.body().email != null)
                                        PrefUtils.getInstance().setPrefEmailID(response.body().email);
                                    MsisdnData msisdnData = new MsisdnData();
                                    msisdnData.operator = SubcriptionEngine.OPERATORS_NAME;
                                    msisdnData.msisdn = response.body().mobile;

                                    if (APIConstants.msisdnPath == null) {
                                        APIConstants.msisdnPath = mContext.getFilesDir() + "/" + "msisdn.bin";
                                    }
                                    SDKUtils.saveObject(msisdnData, APIConstants.msisdnPath);
                                }
                                //Log.d(TAG, "Info: msisdn login: " + "success and launching offer");
                                try {
                                    //Log.d(TAG, "Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                                    PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                                    Util.setUserIdInMyPlexEvents(mContext);
                                    if(!TextUtils.isEmpty(response.body().serviceName)) {
                                        PrefUtils.getInstance().setServiceName(response.body().serviceName);
                                    }
                                    Analytics.mixpanelIdentify();
                                    if (!TextUtils.isEmpty(response.body().email)) {
                                        Analytics.setMixPanelEmail(response.body().email);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if(response.body().code==201){
                                    PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(false);
                                    AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                                }else if(response.body().code == 200){
                                    PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(true);
                                    AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                                }
                                AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.ACCOUNT_TYPE, "");
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.MIXPANEL_PEOPLE_JOINING_DATE, Util.getCurrentDate());
                                params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                                Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                                fetchOfferAvailability(true);
                                // mBaseActivity.pushFragment(new OfferedPacksFragment());
                                return;
                            }
                            try {
                                //Log.d(TAG, "Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                                PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                                Util.setUserIdInMyPlexEvents(mContext);
                                if(!TextUtils.isEmpty(response.body().serviceName)) {
                                    PrefUtils.getInstance().setServiceName(response.body().serviceName);
                                }
                                Analytics.mixpanelIdentify();
                                if (!TextUtils.isEmpty(response.body().email)) {
                                    Analytics.setMixPanelEmail(response.body().email);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                            if(response.body().code==201){
                                PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(false);
                                AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                            }else if(response.body().code == 200){
                                PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(true);
                                AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                            }
                            AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                            Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                        }
                        launchMainActivity();
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        LoggerD.debugOTP("invalid response");
                        launchMainActivity();
                    }
                });
        APIService.getInstance().execute(login);

    }

    private void showPackagesScreen() {
        if (mBaseActivity == null) return;
        Bundle args = new Bundle();
        args.putBoolean(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE, showPackagesDuringBrowse);
        mBaseActivity.pushFragment(PackagesFragment.newInstance(args));
    }

    private void fetchOfferAvailability(final boolean isToLaunchHomeOnError) {
        SDKLogger.debug("isToLaunchHomeOnError- " + isToLaunchHomeOnError);
        OfferedPacksRequest.Params params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_5, APIConstants.PARAM_APPLAUNCH, null);
        final OfferedPacksRequest contentDetails = new OfferedPacksRequest(params, new APICallback<OfferResponseData>() {
            @Override
            public void onResponse(APIResponse<OfferResponseData> response) {
                //   hideProgressBar();
                SDKLogger.debug("response- " + response);
                if (response == null || response.body() == null) {
                    SDKLogger.debug("invalid offer response");
                    showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
                    Analytics.mixpanelEventUnableToFetchOffers(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIConstants.NOT_AVAILABLE);
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    SDKLogger.debug("offer response is not successfull");
                    showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
                    Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
                    return;
                }
                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && response.body().code == 216) {
                    SDKLogger.debug("offer response is 216");

                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")) {
                    if (response.body().code == 219
                            || response.body().code == 220
                            || (response.body().results != null
                            && response.body().results.size() <= 0
                            && !myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW)) {
                        SDKLogger.debug("offer response is already subscribed user");
                        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                        launchMainActivity();
                        return;
                    }
                    if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW
                            && response.body().ui != null
                            && response.body().ui.action != null) {
                        switch (response.body().ui.action) {
                            case APIConstants.OFFER_ACTION_SHOW_MESSAGE:
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                                        @Override
                                        public void onDialogClick(String buttonText) {
                                            PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                                            launchMainActivity();
                                        }
                                    });
                                }
                                break;
                            case APIConstants.OFFER_ACTION_SHOW_TOAST:
                                if (!isToLaunchHomeOnError) {
                                    if (!TextUtils.isEmpty(response.body().message)) {
                                        AlertDialogUtil.showToastNotification(response.body().message);
                                    }
                                    launchMainActivity();
                                } else {
                                    launchMainActivity(response.body().message);
                                }
                                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                                break;
                            case APIConstants.APP_LAUNCH_HOME:
                                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                                launchMainActivity();
                                break;
                            case APIConstants.APP_LAUNCH_WEB:
                                if (!TextUtils.isEmpty(response.body().ui.redirect)) {
                                    startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), response.body().ui.redirect, SubscriptionWebActivity.PARAM_LAUNCH_HOME), SubscriptionWebActivity.SUBSCRIPTION_REQUEST);
                                    getActivity().finish();
                                }
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_OFFER:
                                if (mBaseActivity != null) {
                                    mBaseActivity.pushFragment(OfferFragment.newInstance(null));
                                }
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION:
                                if (mBaseActivity != null) {
                                    mBaseActivity.pushFragment(PackagesFragment.newInstance(null));
                                }
                                break;
                            default:
                                showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
                                break;
                        }
                        return;
                    }

                    boolean isOfferAvailable = false;
                    for (CardDataPackages packageItem : response.body().results) {
                        if (packageItem.subscribed) {
                            PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                            launchMainActivity();
                            return;
                        }
                        if (APIConstants.TYPE_OFFER.equalsIgnoreCase(packageItem.packageType)) {
//                            mBaseActivity.pushFragment(OfferFragment.newInstance(null));
                            isOfferAvailable = true;
                            continue;
                        }
                    }
                    if (isOfferAvailable && mBaseActivity != null) {
                        mBaseActivity.pushFragment(OfferFragment.newInstance(null));
                        return;
                    }

                    if (PrefUtils.getInstance().getPrefIsSkipPackages()) {
                        launchMainActivity();
                        return;
                    }

                    if (mBaseActivity != null) {
                        mBaseActivity.pushFragment(PackagesFragment.newInstance(null));
                    }


                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "Failed: " + t);
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    Analytics.mixpanelEventUnableToFetchOffers(mContext.getString(R.string.network_error), APIConstants.NOT_AVAILABLE);
                    showDeviceAuthenticationFailed(mContext.getString(R.string.network_error));
                    return;
                }
                Analytics.mixpanelEventUnableToFetchOffers(t == null || t.getMessage() == null ? APIConstants.NOT_AVAILABLE : t.getMessage(), APIConstants.NOT_AVAILABLE);
                showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
            }
        });

        APIService.getInstance().execute(contentDetails);
    }


    private void makeReGenerateKeyRequest() {
        generateKeyRequest = new GenerateKeyRequest(new APICallback<DeviceRegData>() {
            @Override
            public void onResponse(APIResponse<DeviceRegData> response) {

                if (response == null || response.body() == null) {
                    showDeviceAuthenticationFailed(null);
                }
                //Log.d(TAG, "success: clientKey reg: " + response.body());

                DeviceRegData devRegResponse = response.body();

                if (!devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                    showDeviceAuthenticationFailed("Code: " + response.body().code +
                            " Msg: "
                            + response.body().message);
                    return;
                }

                if (devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                    //Device registration is succeess
                    //initialize msisdnLogin automatically if autologin is enabled
                    //degug if hardcoded msisdn send header msisdn
                    /*if (ApplicationController.ENABLE_AUTO_LOGIN_SCREEN) {
//                        makeLoginRequest(devRegResponse.appLoginConfig);
                        showOTPLoginScreenFragment(devRegResponse.appLoginConfig.msisdn);
                        return;
                    }*/
                    String login_status = PrefUtils.getInstance().getPrefLoginStatus();
                    String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
                    if (ApplicationController.ENABLE_DIRECT_APP_LAUNCH) {
                        fetchRequiredData();
                        launchMainActivity();
                        return;
                    }
                    if (ApplicationController.ENABLE_OTP_LOGIN) {
                        if (login_status != null && login_status.equalsIgnoreCase("success")) {
                            if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefEmailID()) && (!PrefUtils.getInstance().getPrefIsOTPSkipped() || mIsLoginDuringBrowse)) {
                                showOTPLoginScreenFragment(msisdn, true, mIsLoginDuringBrowse);
                                return;
                            }
                            launchMainActivity();
                            return;
                        }
                        showOTPLoginScreenFragment(msisdn, false, mIsLoginDuringBrowse);
                        return;
                    }
                    launchMainActivity();
                    return;
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "Failed: " + t);
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    showDeviceAuthenticationFailed(mContext.getString(R.string.network_error));
                    return;
                }
                showDeviceAuthenticationFailed(null);
            }
        });
        APIService.getInstance().execute(generateKeyRequest);
    }

    private void showOTPLoginScreenFragment(String msisdn, boolean isExistingUser, boolean isLoginDuringBrowse) {
        LoggerD.debugLog("showOTPLoginScreenFragment: msisdn- " + msisdn);
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putString(FragmentOTPVerification.PARAM_MSISDN, msisdn);
            args.putBoolean(FragmentOTPVerification.PARAM_IS_EXISTING_USER, isExistingUser);
            args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
            args.putString(Analytics.PROPERTY_SOURCE, mSource);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSourceDetails);
            args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);

            mBaseActivity.pushFragment(FragmentOTPVerification.newInstance(args));
        }
    }

    private void showSignUpScreenFragment(String msisdn, boolean isExistingUser, boolean isLoginDuringBrowse) {
        LoggerD.debugLog("showSignUpScreenFragment: msisdn- " + msisdn);
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putString(FragmentOTPVerification.PARAM_MSISDN, msisdn);
            args.putBoolean(FragmentOTPVerification.PARAM_IS_EXISTING_USER, isExistingUser);
            args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
            args.putString(Analytics.PROPERTY_SOURCE, mSource);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSourceDetails);
            args.putBoolean(FragmentSignUp.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentSignUp.newInstance(args));
        }
    }

    private void showDeviceAuthenticationFailed(String message) {
        if (message == null) {
            message = mContext.getResources().getString(R.string.dev_auth_failed_message);
        }
        AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", mContext.getResources()
                        .getString(R.string.feedbackokbutton),
                new AlertDialogUtil.NeutralDialogListener() {
                    @Override
                    public void onDialogClick(String buttonText) {
                        getActivity().finish();
                    }
                });
    }

    private void showSignInScreenFragment(String msisdn, boolean isExistingUser, boolean isLoginDuringBrowse) {
        LoggerD.debugLog("showSignUpScreenFragment: msisdn- " + msisdn);
        if (mBaseActivity != null) {
//            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putString(FragmentOTPVerification.PARAM_MSISDN, msisdn);
            args.putBoolean(FragmentOTPVerification.PARAM_IS_EXISTING_USER, isExistingUser);
            args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
            args.putString(Analytics.PROPERTY_SOURCE, mSource);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSourceDetails);
            args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
            args.putBoolean("isFromSplash", isFromSplash);
            mBaseActivity.pushFragment(FragmentSignIn.newInstance(args));
        }
    }

    private void registerDevice() {
//Device registration call
        DeviceRegistrationEncryptedShreyas.Params deviceRegparams = null;
        if (mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
            //Log.d(TAG, "Dev reg with hardcoded msisdn header + msisdn- " + mContext.getString(R.string.current_msisdn));
            deviceRegparams = new DeviceRegistrationEncryptedShreyas.Params("sundirectAndroid", mContext.getString(R.string.current_msisdn));
        } else {
            //Log.d(TAG, "Dev reg with out hardcoded msisdn");
            deviceRegparams = new DeviceRegistrationEncryptedShreyas.Params("sundirectAndroid");
        }
        deviceRegistration = new DeviceRegistrationEncryptedShreyas(getActivity(), deviceRegparams,
                new APICallback<DeviceRegData>() {
                    @Override
                    public void onResponse(APIResponse<DeviceRegData> response) {

                        if (null == response || null == response.body()) {
                            showDeviceAuthenticationFailed(null);
                            return;
                        }
                        //Log.d(TAG, "success: device reg: " + response.body());

                        DeviceRegData devRegResponse = response.body();
                        //Log.d(TAG, "success: device reg: " + response.body().message);

                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.DEVICE_ID, devRegResponse.deviceId);
                        params.put(Analytics.DEVICE_DESC, mContext.getString(R.string.osname));
                        if (!devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                            showDeviceAuthenticationFailed("Code: " + response.body().code +
                                    " Msg: " + response.body().message);
                            params.put(Analytics.REASON_FAILURE, devRegResponse.message);
                            params.put(Analytics.ERROR_CODE, String.valueOf(devRegResponse.code));
                            Analytics.mixpanelDeviceRegistrationFailed(params);
                            return;
                        }
                        if (devRegResponse.status.equalsIgnoreCase("SUCCESS")&&devRegResponse.code==205) {
                            PrefUtils.getInstance().setPrefLoginStatus("success");
                            launchMainActivity();
                        }else if (devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                            mDeviceRegData = devRegResponse;
                            AppsFlyerTracker.eventDeviceRegistrationSuccess(new HashMap<String, Object>());
                            Analytics.mixpanelDeviceRegistrationSuccess(params);
                            if (ApplicationController.ENABLE_DIRECT_APP_LAUNCH && ApplicationController.ENABLE_OTP_LOGIN) {
                                if (mDeviceRegData.appLoginConfig.msisdn != null) {
                                    PrefUtils.getInstance().setPrefTempMsisdn(mDeviceRegData.appLoginConfig.msisdn);
                                    PrefUtils.getInstance().setPrefMsisdnNo(mDeviceRegData.appLoginConfig.msisdn);
                                }
                                fetchRequiredData();
                                launchMainActivity();
                                return;
                            }
                            if (ApplicationController.ENABLE_ON_BOARDING && PrefUtils.getInstance().getPrefEnableOnBoardingScreen()) {
                                showOnBoardingScreen();
                                return;
                            }
                            doLogin();
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            showDeviceAuthenticationFailed(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailed(null);
                    }
                });

        APIService.getInstance().execute(deviceRegistration);
    }

    private void registerDeviceEncryption() {
//Device registration call
        DeviceRegistrationEncryptedShreyas.Params deviceRegparams = null;
        String clientSecret = mContext.getResources().getString(R.string.clientSecret);
        Log.d("clientSecret", "clientSecret - "+clientSecret);
        if (mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
            //Log.d(TAG, "Dev reg with hardcoded msisdn header + msisdn- " + mContext.getString(R.string.current_msisdn));

            deviceRegparams = new DeviceRegistrationEncryptedShreyas.Params(clientSecret, mContext.getString(R.string.current_msisdn));
        } else {
            //Log.d(TAG, "Dev reg with out hardcoded msisdn");
            deviceRegparams = new DeviceRegistrationEncryptedShreyas.Params(clientSecret);
        }
        DeviceRegistrationEncryptedShreyas deviceRegistrationEncryptedPayLoad = new DeviceRegistrationEncryptedShreyas(getActivity(), deviceRegparams,
                new APICallback<DeviceRegData>() {
                    @Override
                    public void onResponse(APIResponse<DeviceRegData> response) {

                        if (null == response || null == response.body()) {
                            showDeviceAuthenticationFailed(null);
                            return;
                        }
                        //Log.d(TAG, "success: device reg: " + response.body());
                        DeviceRegData devRegResponse = response.body();
                        if (devRegResponse == null
                                || devRegResponse.status == null
                                || devRegResponse.message == null) {
                            showDeviceAuthenticationFailed(null);
                            return;
                        }
                        //Log.d(TAG, "success: device reg: " + devRegResponse.message);

                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.DEVICE_ID, devRegResponse.deviceId);
                        params.put(Analytics.DEVICE_DESC, mContext.getString(R.string.osname));
                        if (!devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                            showDeviceAuthenticationFailed("Code: " + response.body().code +
                                    " Msg: " + response.body().message);
                            params.put(Analytics.REASON_FAILURE, devRegResponse.message);
                            params.put(Analytics.ERROR_CODE, String.valueOf(devRegResponse.code));
                            Analytics.mixpanelDeviceRegistrationFailed(params);
                            return;
                        }

                        if (devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                            mDeviceRegData = devRegResponse;
                            AppsFlyerTracker.eventDeviceRegistrationSuccess(new HashMap<String, Object>());
                            Analytics.mixpanelDeviceRegistrationSuccess(params);
                            if(!isAdded()){
                                return;
                            }
                            if (ApplicationController.ENABLE_DIRECT_APP_LAUNCH) {
                                if(mDeviceRegData != null && mDeviceRegData.appLoginConfig != null && mDeviceRegData.appLoginConfig.msisdn != null) {
                                    //Log.d(TAG, "mDeviceRegData.appLoginConfig.msisdn- " + mDeviceRegData.appLoginConfig.msisdn);
                                    if (mDeviceRegData.appLoginConfig.msisdn != null) {
                                        PrefUtils.getInstance().setPrefMsisdnNo(mDeviceRegData.appLoginConfig.msisdn);
                                        PrefUtils.getInstance().setPrefTempMsisdn(mDeviceRegData.appLoginConfig.msisdn);
                                    }
                                }
                                fetchRequiredData();
                                if (devRegResponse.status.equalsIgnoreCase("SUCCESS")&&devRegResponse.code==205) {
                                    PrefUtils.getInstance().setPrefLoginStatus("success");
                                    launchMainActivity();
                                    return;
                                }
                                /*if (!Util.checkUserLoginStatus() && getResources().getBoolean(R.bool.is_login_check_request_enabled)  && ConnectivityUtil.isConnectedMobile(mContext)) {
                                    makeSignInCheck();
                                    return;
                                } else*/
                                if (Util.checkUserLoginStatus()) {
                                    /**
                                     * Since user is already logged in, It is needed to clear the login session due to device registration is done in a active login session.
                                     * */
                                    LoggerD.debugLog("Need to clear the login session, device registration happend after login.");
                                    PrefUtils.getInstance().setPrefLoginStatus("");
                                }
                                launchMainActivity();
                                return;
                            }
                            if (ApplicationController.ENABLE_ON_BOARDING && PrefUtils.getInstance().getPrefEnableOnBoardingScreen()) {
                                showOnBoardingScreen();
                                return;
                            }
                            doLogin();
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            showDeviceAuthenticationFailed(mContext.getString(R.string.network_error));
                            return;
                        }
                        if(t instanceof SSLPeerUnverifiedException){
                            String message = mContext.getResources().getString(R.string.dev_auth_failed_message);
                            showDeviceAuthenticationFailed(message + "..");
                            return;
                        }
                        showDeviceAuthenticationFailed(null);
                    }
                });

        APIService.getInstance().execute(deviceRegistrationEncryptedPayLoad);
    }

    public void setOttAppsList(List<OTTApp> ottAppList) {
//        mAppsList = ottAppList;
        /*try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if (ottAppList == null) {
            doLogin();
            return;
        }
        for (OTTApp app : ottAppList) {
            if (app.images != null && app.images.values != null) {
                for (CardDataOttImagesItem image : app.images.values) {
                    if (image.type != null && image.type.equalsIgnoreCase("thumbnail")) {
                        app.imageUrl = image.link;
                    } else if (image.type != null && image.type.equalsIgnoreCase("coverposter")) {
                        OTTAppsImageSliderAdapter.SliderModel model = new OTTAppsImageSliderAdapter.SliderModel();
                        model.imageUrl = image.link;
                        model.ottApp = app;
                        model.siblingOrder = image.siblingOrder;
                        model.contentId = image.contentId;
                        model.partnerContentId = image.partnerContentId;
                        mSliderItems.add(model);
                    }
                }
            }
        }

        if (mSliderItems.isEmpty()) {
            doLogin();
            return;
        }
        bannerImagesOrder(mSliderItems);
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        mThreadPoolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES * mSliderItems.size(),
                NUMBER_OF_CORES * mSliderItems.size(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );

        progressCount = 0;
        for (int i = 0; i < mSliderItems.size(); i++) {
//            final int finalI = i;
            mThreadPoolExecutor.execute(new LongThread(i, mSliderItems.get(i).imageUrl, new Handler(mHandlerCallback)));
        }
        final Timer timer = new Timer();
        TimerTask blockingTimer = new TimerTask() {
            @Override
            public void run() {
                LoggerD.debugLog("showOnBoardingScreen: time out reached skip onboarding");
                cancel();
                if (timer != null)
                    timer.cancel();
                if (isOnBoardingTaskCompleted) {
                    return;
                }
                isTimeOutReached = true;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (mThreadPoolExecutor != null) {
                            mThreadPoolExecutor.shutdownNow();
                            LoggerD.debugLog("showOnBoardingScreen: time out is reached shutting down all images downloading task");
                        }
                        doLogin();
                    }
                });
            }
        };
        timer.schedule(blockingTimer, ONBOARDING_IMAGE_DOWNLODING_TIME_OUT);
    }

    private void bannerImagesOrder(List<OTTAppsImageSliderAdapter.SliderModel> sliderItems) {
        Collections.sort(sliderItems, new Comparator<OTTAppsImageSliderAdapter.SliderModel>() {

            @Override
            public int compare(OTTAppsImageSliderAdapter.SliderModel lhs, OTTAppsImageSliderAdapter.SliderModel rhs) {
                if (lhs == null
                        || rhs == null) {
                    return -1;
                }
                if (lhs.siblingOrder == null
                        || rhs.siblingOrder == null) {
                    return -1;
                }
                int lhsSiblingOrder = Integer.parseInt(lhs.siblingOrder);
                int rhsSiblingOrder = Integer.parseInt(rhs.siblingOrder);
                return rhsSiblingOrder > lhsSiblingOrder ? 1 : -1;
            }
        });
    }

    private void fetchOttAppsDataAndShowBanners() {

        String contentType = APIConstants.TYPE_ONBOARDING_ANDROID;
        if (DeviceUtils.isTablet(mContext)) {
            contentType = APIConstants.TYPE_ONBOARDING;
        }
        OTTAppRequest.Params ottAppreqParams = new OTTAppRequest.Params(contentType, true);
        OTTAppRequest ottAppRequest = new OTTAppRequest(ottAppreqParams, new APICallback<OTTAppData>() {
            @Override
            public void onResponse(APIResponse<OTTAppData> response) {
                if (response == null || response.body() == null || response.body().results == null) {
                    LoggerD.debugLog("showOnBoardingScreen: fetchOttAppsDataAndShowBanners: response or its body or its results is null ");
                    setOttAppsList(null);
                    return;
                }

                List<OTTApp> mAppsList = response.body().results;
                if (mAppsList == null) {
                    LoggerD.debugLog("showOnBoardingScreen: fetchOttAppsDataAndShowBanners: response results are null ");
                    setOttAppsList(null);
                    return;
                }


                setOttAppsList(mAppsList);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugLog("showOnBoardingScreen: fetchOttAppsDataAndShowBanners: error response");
                setOttAppsList(null);
            }
        });
        APIService.getInstance().execute(ottAppRequest);
    }

    private void showOnBoardingScreen() {
        LoggerD.debugLog("showOnBoardingScreen:");
        isOnBoardingTaskCompleted = false;
        isTimeOutReached = false;
        fetchOttAppsDataAndShowBanners();
    }

    private void doLogin() {
        //Device registration is succeess
        //initialize msisdnLogin automatically if autologin is enabled
        //debug if hardcoded msisdn send header msisdn
        String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
        fetchRequiredData();
        LoggerD.debugLog("doLogin");

        if (mDeviceRegData != null && mDeviceRegData.appLoginConfig != null) {
            if (ApplicationController.ENABLE_AUTO_LOGIN_SCREEN) {
                if (ApplicationController.ENABLE_OTP_LOGIN) {
                    if (mDeviceRegData.appLoginConfig.msisdn != null) {
                        PrefUtils.getInstance().setPrefTempMsisdn(mDeviceRegData.appLoginConfig.msisdn);
                    }
                    showOTPLoginScreenFragment(mDeviceRegData.appLoginConfig.msisdn, false, mIsLoginDuringBrowse);
                    return;
                }
                //Log.d(TAG, "success: device reg: " + mDeviceRegData.appLoginConfig.msisdn);
                makeLoginRequest(mDeviceRegData.appLoginConfig);
                return;
            }
            launchMainActivity();
        } else {
            if (ApplicationController.ENABLE_OTP_LOGIN) {
                showOTPLoginScreenFragment(msisdn, false, mIsLoginDuringBrowse);
                return;
            }
            if (mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
                if (msisdn == null) {
                    msisdn = mContext.getString(R.string.current_msisdn);
                }
                makeMSISDNLogin(msisdn, "", false);
            } else {
                makeMSISDNLogin(null, "", false);
            }
        }
    }

    private void makeLoginRequest(AppLoginConfigData appLoginConfig) {
        if (appLoginConfig != null
                && appLoginConfig.type != null
                && appLoginConfig.medium != null
                && appLoginConfig.type.equalsIgnoreCase(FLAG_AUTO_LOGIN)
                && appLoginConfig.medium.equalsIgnoreCase(FLAG_MSISDN)) {
            if (appLoginConfig.msisdn != null) {
                PrefUtils.getInstance().setPrefMsisdnNo(appLoginConfig.msisdn);
                //Log.d(TAG, "makeLoginRequest: device reg resp login with hardcoded msisdn: " + appLoginConfig.msisdn);
                MsisdnData msisdnData = new MsisdnData();
                msisdnData.operator = SubcriptionEngine.OPERATORS_NAME;
                msisdnData.msisdn = appLoginConfig.msisdn;

                if (APIConstants.msisdnPath == null) {
                    APIConstants.msisdnPath = mContext.getFilesDir() + "/" + "msisdn.bin";
                }
                SDKUtils.saveObject(msisdnData, APIConstants.msisdnPath);
                if (ConnectivityUtil.isConnectedWifi(mContext)
                        && !mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {

                    return;
                }
                //Log.d(TAG, "making msisdn login with msisdn " + appLoginConfig.msisdn);
                if (mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
                    makeMSISDNLogin(appLoginConfig.msisdn, "", false);
                } else {
                    makeMSISDNLogin(null, "", false);
                }
                return;
            } else {
                //msisdn is null but msisdn and type and auto login type
                //return;
                //Log.d(TAG, "making msisdn login with OUT msisdn ");
                makeMSISDNLogin(null, "", false);
                return;
            }
        } else if (mContext.getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
            //Log.d(TAG, "making msisdn login with HARDCODED msisdn ");
            makeMSISDNLogin(mContext.getResources().getString(R.string.current_msisdn), "", false);
            return;
        }
    }

    private void showUseOnlysundirectNetworkMessage() {
        AlertDialogUtil.showAlertDialog(mContext, mContext.getString(R.string.use_only_sundirect_network_message) + " " +
                        mContext.getResources().getString(R.string.app_name), "",
                mContext.getString(R.string.cancel),
                mContext.getString(R.string.alert_dataconnection_viewsetttings), new AlertDialogUtil.DialogListener() {
                    @Override
                    public void onDialog1Click() {

                    }

                    @Override
                    public void onDialog2Click() {
                                                                    /*Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                                                    startActivity(intent);*/
                        //launching the mobile data
                        // settings
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                        startActivity(intent);
                    }
                });
    }

    private void launchMainActivity() {
        getUserConsent();
        //makeUserProfileRequest();
    }


    private void launchMainActivity(String message) {
        Activity activity = mActivity;
        if (activity == null || activity.isFinishing()) return;
        Bundle bundle;
        if (getArguments() != null && getArguments().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME)) {
            bundle = new Bundle();
            bundle.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME));
            bundle.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE, getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
            bundle.putString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, getArguments().getString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT));
            bundle.putString(APIConstants.NOTIFICATION_PARAM_LAYOUT, getArguments().getString(APIConstants.NOTIFICATION_PARAM_LAYOUT));
            MainActivityLauncherUtil.setNotificationViewAllData(bundle);
        }
        MainActivityLauncherUtil.initStartUpCalls(activity, message);
    }

    private void makeUserProfileRequest(){
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    launchMainActivityAfterProfileCheck();
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    launchMainActivityAfterProfileCheck();
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();
                    if (responseData.result != null
                            && responseData.result.profile != null) {
                        if (responseData.result.profile.first != null && !TextUtils.isEmpty(responseData.result.profile.first)) {
                            if (responseData.result.profile.last != null && !TextUtils.isEmpty(responseData.result.profile.last)) {
                                PrefUtils.getInstance().setPrefFullName(responseData.result.profile.first + " " + responseData.result.profile.last);
                            } else {
                                PrefUtils.getInstance().setPrefFullName(responseData.result.profile.first);
                            }
                        }
                        if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                            if (responseData.result.profile.locations.get(0) != null
                                    && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                                PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
                        }

                        if (responseData.result.profile.state !=null&&!TextUtils.isEmpty(responseData.result.profile.state)){
                            PrefUtils.getInstance().setUserState(responseData.result.profile.state);
                        }

                        if (responseData.result.profile.city !=null&&!TextUtils.isEmpty(responseData.result.profile.city)){
                            PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
                        }

                        if (responseData.result.profile.showForm){
                            /*Intent ip=new Intent(mContext, MandatoryProfileActivity.class);
                            startActivityForResult(ip,PROFILE_UPDATE_REQUEST);*/
                            PrefUtils.getInstance().setIsToShowForm(true);
                            launchMainActivityAfterProfileCheck();
                        }else {
                            launchMainActivityAfterProfileCheck();
                        }
                    }else {
                        launchMainActivityAfterProfileCheck();
                    }
                }else {
                    launchMainActivityAfterProfileCheck();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Profile update onFailure");
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }



    private void launchMainActivityAfterProfileCheck(){
        Activity activity = mActivity;
        if (activity == null || activity.isFinishing()) return;
        Bundle bundle;
        if (getArguments() != null && getArguments().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME)) {
            bundle = new Bundle();
            bundle.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, getArguments().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME));
            bundle.putString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, getArguments().getString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT));
            bundle.putString(APIConstants.NOTIFICATION_PARAM_LAYOUT, getArguments().getString(APIConstants.NOTIFICATION_PARAM_LAYOUT));
            MainActivityLauncherUtil.setNotificationViewAllData(bundle);
        }
        MainActivityLauncherUtil.initStartUpCalls(activity);
    }

    private void makeMSISDNLogin(final String msisdn, String imsi, final boolean isFromMsisdnReterival) {
       /* //Log.d(TAG, "makeMSISDNLogin: msisdn: " + msisdn + "" +
                " imsi- " + imsi + "" +
                " isFromMsisdnReterival- " + isFromMsisdnReterival);*/

        MSISDNLogin.Params msisdnParams = new MSISDNLogin.Params(msisdn, imsi);

        login = new MSISDNLogin(msisdnParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
                        if (response == null
                                || response.body() == null) {
                            // login failed
                            //Log.d(TAG, "success: msisdn login: " + "failed");
                            showDeviceAuthenticationFailed(null);
                            return;
                        }
                      /*  //Log.d(TAG, "success: msisdn login status : " + response.body().status + "code :" + response.body().code
                                + "message :" + response.body().message);*/
                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_CLIENT);
                        if (!TextUtils.isEmpty(response.body().userid)) {
                            params.put(Analytics.USER_ID, response.body().userid);
                        }
                        if (!APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            // login failed
                            params.put(Analytics.REASON_FAILURE, response.body().message);
                            params.put(Analytics.SIGN_UP_OPTION, Analytics.ACCOUNT_CLIENT);
                            params.put(Analytics.ERROR_CODE, String.valueOf(response.body().code));
                            Analytics.trackEvent(Analytics.EventPriority.MEDIUM, Analytics.EVENT_SIGN_UP_FAILED, params);
                            //Log.d(TAG, "success: msisdn login: " + "failed" + response.body().message);
                            showDeviceAuthenticationFailed(response.body().message);
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            // login failed
                            if (isFromMsisdnReterival) {

                                return;
                            }
                            if (ConnectivityUtil.isConnectedWifi(mContext)) {
                                return;
                            }
                            final MsisdnRetrivalEngine msisdnRetrivalEngine = new MsisdnRetrivalEngine(mContext);
                            // Skip if mobile data is disabled
                            msisdnRetrivalEngine.setUseOnlyMobileData(false);

                            msisdnRetrivalEngine.getMsisdnData(new MsisdnRetrivalEngine.MsisdnRetrivalEngineListener() {

                                @Override
                                public void onMsisdnData(MsisdnData data) {
                                    msisdnRetrivalEngine.deRegisterCallBacks();
                                    if (data == null
                                            || data.msisdn == null) {

                                        return;
                                    }
                                    makeMSISDNLogin(data.msisdn, data.imsi, true);

                                    Log.e(TAG, "onMsisdnData msisdn " + data.msisdn + " operator " + data.operator);

                                }
                            });


                            //Log.d(TAG, "success: msisdn login: " + "failed" + response.body().message);

                        }

                        if (response.body().status.equalsIgnoreCase("SUCCESS")
                                && (response.body().code == 200
                                || response.body().code == 201)) {
                            myplexAPI.clearCache(APIConstants.BASE_URL);
                            PropertiesHandler.clearCategoryScreenFilter();
                            if(!TextUtils.isEmpty(response.body().serviceName)){
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }
                            PrefUtils.getInstance().setPrefLoginStatus("success");

                            if (ApplicationController.FLAG_ENABLE_OFFER_SUBSCRIPTION) {

                                if (response.body().mobile != null && !response.body().mobile.isEmpty()) {
                                    PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
                                    if (response.body().email != null)
                                        PrefUtils.getInstance().setPrefEmailID(response.body().email);
                                    MsisdnData msisdnData = new MsisdnData();
                                    msisdnData.operator = SubcriptionEngine.OPERATORS_NAME;
                                    msisdnData.msisdn = response.body().mobile;

                                    if (APIConstants.msisdnPath == null) {
                                        APIConstants.msisdnPath = mContext.getFilesDir() + "/" + "msisdn.bin";
                                    }
                                    SDKUtils.saveObject(msisdnData, APIConstants.msisdnPath);
                                }
                                //Log.d(TAG, "Info: msisdn login: " + "success and launching offer");
                                try {
                                    //Log.d(TAG, "Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                                    PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                                    Util.setUserIdInMyPlexEvents(mContext);
                                    if(!TextUtils.isEmpty(response.body().serviceName)) {
                                        PrefUtils.getInstance().setServiceName(response.body().serviceName);
                                    }
                                    Analytics.mixpanelIdentify();
                                    if (!TextUtils.isEmpty(response.body().email)) {
                                        Analytics.setMixPanelEmail(response.body().email);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Analytics.mixpanelSetOncePeopleProperty(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_CLIENT);
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.MIXPANEL_PEOPLE_JOINING_DATE, Util.getCurrentDate());
                                params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                                Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                                fetchOfferAvailability(false);
                                if(response.body().code==201){
                                    PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(false);
                                    AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                                }else if(response.body().code == 200){
                                    PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(true);
                                    AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                                }
                                AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                                // mBaseActivity.pushFragment(new OfferedPacksFragment());
                                return;
                            }
                            try {
                                //Log.d(TAG, "Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                                PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                                Util.setUserIdInMyPlexEvents(mContext);
                                if(!TextUtils.isEmpty(response.body().serviceName)) {
                                    PrefUtils.getInstance().setServiceName(response.body().serviceName);
                                }
                                Analytics.mixpanelIdentify();
                                if (!TextUtils.isEmpty(response.body().email)) {
                                    Analytics.setMixPanelEmail(response.body().email);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                            Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                            if(response.body().code==201){
                                PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(false);
                                AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                            }else if(response.body().code == 200){
                                PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(true);
                                AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                            }
                            AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                            launchMainActivity();
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            showDeviceAuthenticationFailed(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailed(null);
                    }
                });
        APIService.getInstance().execute(login);
    }



    @Override
    public boolean onBackClicked() {
        return super.onBackClicked();
    }

    private void fetchRequiredData() {
        if (ConnectivityUtil.isConnected(mContext)) {
            sendGcmRequest();
            String clientSecret = mContext.getResources().getString(R.string.clientSecret);
            if(!BuildConfig.FLAVOR.contains("idea")){
                clientSecret ="all";
            }
            APIService.getInstance().updateProperties(mContext, PrefUtils.getInstance().getPrefClientkey(), SDKUtils
                    .getInternetConnectivity(mContext), SDKUtils.getMCCAndMNCValues(mContext), Util
                    .getAppVersionName(mContext),clientSecret);

        }
    }

    private void sendGcmRequest() {
        if (mContext == null) {
            return;
        }
        boolean isGcmIdSent = PrefUtils.getInstance().getBoolean(MyGcmListenerService.SENT_TOKEN_TO_SERVER, false);
        if (!checkPlayServices()) {
            return;
        }
        if (!isGcmIdSent || !PrefUtils.getInstance().getPrefIsCleverTapGCMTokenUpdated()) {
            // Start IntentService to register this application with GCM.
           /* Intent intent = new Intent(mContext, RegistrationIntentService.class);
            mContext.startService(intent);*/
            if(mContext!= null) {
                RegistrationJobScheduler.enqueueWork(mContext, new Intent());
            }
        }
    }

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog((Activity) mContext, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                ((Activity) mContext).finish();
            }
            return false;
        }
        return true;
    }

    public static BaseFragment newInstance(Bundle args) {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setArguments(args);
        return loginFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SubscriptionWebActivity.SUBSCRIPTION_REQUEST) {

            String packageName = APIConstants.NOT_AVAILABLE;
            double price = -1;
            boolean isSMSFlow = false;
            String gaEventAction = packageName + Analytics.HEIFEN_WITH_SPACE_ENCLOSED + price;
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras.containsKey("packageName")) {
                    packageName = data.getStringExtra("packageName");
                }
                if (extras.containsKey("contentprice")) {
                    price = data.getDoubleExtra("contentprice", -1);
                }
                if (extras.containsKey("isSMS")) {
                    isSMSFlow = data.getBooleanExtra("isSMS", false);
                }
                gaEventAction = packageName + Analytics.HEIFEN_WITH_SPACE_ENCLOSED + (price < 0 ? APIConstants.NOT_AVAILABLE : price + "");
                if (extras.containsKey("cgPageLoaded")) {
                    if (data.getBooleanExtra("cgPageLoaded", false)) {
                        Analytics.gaCategorySubscription(gaEventAction, Analytics.EVENT_LABEL_CG_PAGE);
                        String duration = null;
                        if (extras.containsKey("duration")) {
                            duration = data.getStringExtra("duration");
                        }
                        String paymentModeSelected = null;
                        if (extras.containsKey("paymentMode")) {
                            paymentModeSelected = data.getStringExtra("paymentMode");
                        }
                        CleverTap.eventConsentPageViewed(gaEventAction, paymentModeSelected == null ? "NA" : paymentModeSelected, price + "", duration, isSMSFlow);
                    }
                }
            }
            LoggerD.debugLog("PackagesFragment: onActivityResult: resultCode- " + resultCode);
            if (resultCode == APIConstants.SUBSCRIPTIONINPROGRESS
                    || resultCode == APIConstants.SUBSCRIPTIONSUCCESS) {
                Analytics.gaCategorySubscription(gaEventAction, Analytics.EVENT_LABEL_PAYMENT_SUCCESS);
            } else if (resultCode == APIConstants.SUBSCRIPTIONCANCELLED) {
//                Analytics.gaCategorySubscription(gaEventAction, Analytics.EVENT_LABEL_PAYMENT_CANCEL);
            } else {
//                Analytics.gaCategorySubscription(gaEventAction, Analytics.EVENT_LABEL_PAYMENT_FAILED);
            }


            if (data != null) {
                String page = data.getStringExtra(APIConstants.NOTIFICATION_PARAM_PAGE);
                if (resultCode == APIConstants.SUBSCRIPTIONSUCCESS
                        || resultCode == APIConstants.SUBSCRIPTIONINPROGRESS) {
                    if (!TextUtils.isEmpty(page)
                            && APIConstants.APP_LAUNCH_NATIVE_OFFER.equalsIgnoreCase(page)) {
                        if (mBaseActivity != null) {
                            mBaseActivity.pushFragment(OfferFragment.newInstance(null));
                        }
                        return;
                    } else if (!TextUtils.isEmpty(page)
                            && APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION.equalsIgnoreCase(page)) {
                        if (mBaseActivity != null) {
                            mBaseActivity.pushFragment(PackagesFragment.newInstance(null));
                        }
                        return;
                    }
                    if (!PrefUtils.getInstance().getPrefOfferPackSubscriptionStatus()) {
                        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                    }
                    Util.launchActivity(getActivity(), MainActivity.createIntent(getActivity(), page));
                } else if (mContext.getString(R.string.skip_text).equalsIgnoreCase(page)) {
                    launchMainActivity();
                } else if (resultCode == APIConstants.SUBSCRIPTIONERROR) {
//                    getActivity().finish();
                    String message = mContext.getString(R.string.canot_connect_server);
                    if (!ConnectivityUtil.isConnected(mContext)) {
                        message = mContext.getString(R.string.network_error);
                    }
                    showDeviceAuthenticationFailed(message);
                } else {
                    getActivity().finish();
                }
            }
        }

        if (requestCode==PROFILE_UPDATE_REQUEST){
            if (data != null && data.getExtras() != null) {
                Bundle extras = data.getExtras();
                if (extras.containsKey(IS_PROFILE_UPDATE_SUCCESS)){
                    boolean isProfileUpdateSuccess=data.getBooleanExtra(extras.getString(IS_PROFILE_UPDATE_SUCCESS),true);
                    if (isProfileUpdateSuccess){
                        launchMainActivityAfterProfileCheck();
                    }else {
                        getActivity().finish();
                    }
                }else {
                    getActivity().finish();
                }
            }
        }
    }

    private void getUserConsent() {
        mProgressBar.setVisibility(View.VISIBLE);
        UserConsentUrl.Params params = new UserConsentUrl.Params("privacyConsent");
        final UserConsentUrl consentUrlCall = new UserConsentUrl(params, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response == null || response.body() == null) {
                    // Analytics.createEventLoggerAPI(RESPONSE_NULL, 0 + "", APIConstants.ERR_APP_LAUNCH_ISSUE, TAG, null);
                    // showErrorWebView();
                    showErrorPopUp();
                    return;
                }
                if (response.body().status.equalsIgnoreCase("SUCCESS") || response.body().code == 200) {
                    PrefUtils.getInstance().isEUUserEnabled(response.body().isGDPREnabled);
                    if (response.body().show_privacy_popup != null
                            && response.body().show_privacy_popup.equalsIgnoreCase("true")) {
                        PrefUtils.getInstance().didShowEUPopUpEnabled(true);
                        if (response.body().web_url != null) {
                            PrefUtils.getInstance().didShowEUPopUpEnabled(true);
                            openWebViewWithConsentUrl(response.body().web_url);
                        } else {
                            makeUserProfileRequest();
                        }
                    } else {
                        makeUserProfileRequest();
                    }
                } else {
                    //Analytics.createEventLoggerAPI(response.body().message, response.body().code + "", APIConstants.ERR_APP_LAUNCH_ISSUE, TAG, null);
                    //showErrorWebView();
                    showErrorPopUp();
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                if (Util.isNetworkAvailable(mContext)) {
                    if (t != null && t.getMessage() != null) {
                        // Analytics.createEventLoggerAPI(t.getMessage(), errorCode + "", APIConstants.ERR_APP_LAUNCH_ISSUE, TAG, null);
                    } else {
                        // Analytics.createEventLoggerAPI(NULL_VALUE, errorCode + "", APIConstants.ERR_APP_LAUNCH_ISSUE, TAG, null);
                    }
                    //showErrorWebView();
                    showErrorPopUp();
                } else
                    makeUserProfileRequest();
            }
        });
        APIService.getInstance().execute(consentUrlCall);
    }

    /*@SuppressLint("SetJavaScriptEnabled")
    private void showErrorWebView() {

        if (Util.isNetworkAvailable(mContext)) {
            mProgressBar.setVisibility(View.GONE);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(APIConstants.ERROR_WEB_URL);
            webView.setWebViewClient(errorWebViewClient);
        } else {
            showNoNetworkPopUp();
        }
    }*/

    AlertDialog dialog;

    private void showNoNetworkPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setMessage(getResources().getString(R.string.network_error));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                getActivity().finish();
            }
        });
        dialog = builder.create();

        if (Util.checkActivityPresent(mContext)) {
            dialog.show();
            dialog.setCancelable(false);
        }
    }

    private void showErrorPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setMessage(getResources().getString(R.string.error_popup));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                getActivity().finish();
            }
        });
        dialog = builder.create();

        if (Util.checkActivityPresent(mContext)) {
            dialog.show();
            dialog.setCancelable(false);
        }
    }

    /*private WebViewClient errorWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (!Util.isNetworkAvailable(mContext)) {
                showNoNetworkPopUp();
                return true;
            }
            //Log.d(TAG, "Processing webview url click...");
            view.loadUrl(APIConstants.ERROR_WEB_URL);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //Log.d(TAG, "Finished loading URL: " + url);
            mProgressBar.setVisibility(View.GONE);
            Uri uri = Uri.parse(url);
            if (!uri.isHierarchical()) {
                return;
            }
            try {
                Set<String> queryParams = uri.getQueryParameterNames();
                if (queryParams.size() > 0) {
                    String action = uri.getQueryParameter("action");
                    String close = uri.getQueryParameter("close");
                    if (action != null && action.equalsIgnoreCase("retry")) {
                        Intent intent = new Intent(mContext, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    if (close != null && close.equalsIgnoreCase("yes")) {
                        webView.setVisibility(View.GONE);
                        getActivity().finish();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                proceedDeviceRegistration();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }
    };*/

    @SuppressLint("SetJavaScriptEnabled")
    private void openWebViewWithConsentUrl(String web_url) {
//        if (PrefUtils.getInstance().getdidShowEUPopUpEnabled()) {
//            return;
//        }
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setVisibility(View.VISIBLE);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("WebView", consoleMessage.message());
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
                //Log.d(TAG, "WebView onPageStarted...");
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Log.d(TAG, "WebView Processing webview url click...");

                if (!Util.isNetworkAvailable(mContext)) {
                    showNoNetworkPopUp();
                    return true;
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //Log.d(TAG, "WebView Finished loading URL: " + url);
                mProgressBar.setVisibility(View.GONE);
                Uri uri = Uri.parse(url);
                String path1 = uri.getEncodedPath();
                if (errorWebView != null && errorWebView.getVisibility() == View.VISIBLE) {
                    errorWebView.setVisibility(View.GONE);
                }
                //Log.d(TAG, "path1: " + path1);
                if (!uri.isHierarchical()) {
                    return;
                }

                try {
                    Set<String> queryParams = uri.getQueryParameterNames();
                    if (queryParams.size() > 0) {
                        if (queryParams.contains("status")) {
                            String status = uri.getQueryParameter("status");
                            String token = uri.getQueryParameter("token");
                            if (status != null) {
                                if (status.equalsIgnoreCase("accept")) {
                                    PrefUtils.getInstance().didShowEUPopUpEnabled(true);
                                    PrefUtils.getInstance().setUserConsentToken(token);

                                    if (queryParams.contains("close")) {
                                        String close = uri.getQueryParameter("close");
                                        if (!TextUtils.isEmpty(close) && close.equalsIgnoreCase("yes")) {
                                            webView.setVisibility(View.GONE);
//                                            dynamicAppPermissions();
                                            makeUserProfileRequest();
                                        }
                                    }


                                } else if (status.equalsIgnoreCase("deny")) {
                                    getActivity().finish();
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    makeUserProfileRequest();
                }

            }

        });
//        webView.loadUrl("http://192.168.26.94:8080/permission/index.html");
        webView.loadUrl(web_url);

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int action = keyEvent.getAction();
                //Log.d(TAG, "WebView setOnKeyListener action: " + action);
                return false;
            }
        });
    }


    private void showPrivacyPolicyUpdate(final Activity mContext) {
        if (mContext == null || mContext.isFinishing()) {
            return;
        }
        PrivacyPolicyDialogWebView privacyPolicyDialogWebView = new PrivacyPolicyDialogWebView(getActivity(), new PrivacyPolicyDialogWebView.DialogListener() {
            @Override
            public void onAccept() {
                //dialog.dismiss();
                try {
                    PrefUtils.getInstance().setPrefShowPrivacyConsent(false);
                    proceedDeviceRegistration();
                    CleverTap.eventPrivacyPolicyClicked(CleverTap.VALUE_ACCEPT);
                } catch (Throwable t) {
                    t.printStackTrace();
                    //Crashlytics.logException(t);
                }
            }

            @Override
            public void onDecline() {
                try {
                    if (mContext == null || mContext.isFinishing()) return;
                    CleverTap.eventPrivacyPolicyClicked(CleverTap.VALUE_DECLINE);
                    mContext.finish();
                } catch (Throwable t) {
                    t.printStackTrace();
                    // Crashlytics.logException(t);
                }
            }
        });
        privacyPolicyDialogWebView.showDialog();
    }

    private OnCompleteListener mOnCompleteListener = task -> {
        FirebaseRemoteConfig mConfig = ApplicationController.getmFirebaseRemoteConfig();
        mConfig.activate();
    };



    private OnFailureListener mOnFailureListener = e -> e.printStackTrace();

}
