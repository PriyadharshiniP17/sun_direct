package com.myplex.myplex.ui.activities;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.multidex.BuildConfig;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.animationviewpager.SliderLayout;
import com.myplex.myplex.animationviewpager.ViewPagerEx;
import com.myplex.myplex.gcm.MyGcmListenerService;
import com.myplex.myplex.gcm.RegistrationJobScheduler;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.adapter.OTTAppsImageSliderAdapter;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.FragmentPreferredLanguages;
import com.myplex.myplex.ui.fragment.FragmentRegisterCreatePassword;
import com.myplex.myplex.ui.fragment.FragmentResetPassword;
import com.myplex.myplex.ui.fragment.OfferFragment;
import com.myplex.myplex.ui.fragment.PackagesFragment;
import com.myplex.myplex.ui.fragment.VmaxInterstitialAdFragment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.FragmentCreatePassword;
import com.myplex.myplex.utils.FragmentGetNewConnection;
import com.myplex.myplex.utils.FragmentNewUser;
import com.myplex.myplex.utils.FragmentOTPVerification;
import com.myplex.myplex.utils.FragmentSetTopBoxes;
import com.myplex.myplex.utils.FragmentSignIn;
import com.myplex.myplex.utils.FragmentSignUp;
import com.myplex.myplex.utils.LongThread;
import com.myplex.myplex.utils.MainActivityLauncherUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.MsisdnRetrivalEngine;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;

import org.json.JSONException;
import org.json.JSONObject;

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

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import jp.wasabeef.blurry.Blurry;

/*import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;*/

public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST_CODE = 201;
    private static final int REQUEST_PERMISSION_SETTING = 401;
    private static final int GMAIL_SIGN_IN = 111;
    private static final int SHOW_HOME_FROM_ON_BOARDING = 3 * 1000;
    private static final String FLAG_AUTO_LOGIN = "auto";
    private static final String FLAG_MSISDN = "msisdn";
    private BaseFragment mCurrentFragment;
    private boolean isVisible;
    private boolean interstitialShown = false;
    private boolean isForgotPassword = false;
    private static boolean isFromSplash = true;
    private boolean isOnBoardingTaskCompleted;
    private static boolean showPackagesDuringBrowse;
    private FragmentSignIn fragmentSignIn;
    private static boolean mIsLoginDuringBrowse;
    private GenerateKeyRequest generateKeyRequest;
    private DeviceRegistrationEncryptedShreyas deviceRegistration;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private static final int SPLASH_TIME = 1 * 1000;
    //    private CallbackManager callbackManager;
    private MSISDNLogin login;
    private static String mSource;
    private static String mSourceDetails;
    private int progressCount;
    private boolean isTimeOutReached;
    private static final String PATH_WATCH = "watch";
    private static final String PATH_PAGE = "page";
    private static final String PATH_LIVE = "live";
    private static final String PATH_DETAIL = "detail";
    private WebView webView, errorWebView;
    private ImageView mBlurredBackground;
    private ProgressBar mProgressBar;
    private Context mContext;
    private DeviceRegData mDeviceRegData;
    private static final int ONBOARDING_IMAGE_DOWNLODING_TIME_OUT = 30 * 1000;
    private List<OTTAppsImageSliderAdapter.SliderModel> mSliderItems = new ArrayList<>();
    private SliderLayout mSliderLayout;
    private com.yqritc.scalablevideoview.ScalableVideoView videoView;
    private boolean playCompleted = false;
    private boolean servicesCompleted = false;
    private boolean servicesError = false;

    private RelativeLayout mLayoutRetry;
//    boolean connected = false;

    @Keep
    public static int dexSignatureStatusBitField = 0;
    @Keep
    public static int dateStatusBitField = 0;
    @Keep
    public static int contextStatusBitField = 0;
    @Keep
    public static int manifestStatusBitField = 0;
    @Keep
    public static int certificateStatusBitField = 0;

    private enum SchemeType {
        http, https
    }

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


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
      /*  String installerInfo;
        PackageManager packageManager = getPackageManager();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                installerInfo = packageManager.getInstallSourceInfo("com.sundirect.android.staging").getInstallingPackageName();
            else installerInfo = packageManager.getInstallerPackageName("com.sundirect.android.staging");

        } catch (Exception e) {
            installerInfo = null;
            e.printStackTrace();
        }
       *//* if(installerInfo == null){
            return;
        }*/
        mContext = getApplicationContext();
        webView = findViewById(R.id.webView);
        mSliderLayout = findViewById(R.id.slider);
        mBlurredBackground = findViewById(R.id.image_blur_bg);
        errorWebView = findViewById(R.id.errorWebView);
        videoView = findViewById(R.id.video_view);
        mProgressBar = findViewById(R.id.card_loading_progres_bar);

        mLayoutRetry = (RelativeLayout) findViewById(R.id.retry_layout);
        mLayoutRetry.setVisibility(GONE);
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SDKLogger.debug("onCreate called");
        fragmentSignIn = new FragmentSignIn();
        //  callbackManager= CallbackManager.Factory.create();
        final Context context = this;
        int videoId = getResources().getIdentifier("sundirect_final_splash_video", "raw", mContext.getPackageName());
        if (videoId != 0) {
            String videoPath = "android.resource://" + mContext.getPackageName() + "/" + videoId;
//                    videoView.setVideoURI(Uri.parse(videoPath));
            try {
                videoView.setRawData(videoId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isFromSplash = getIntent().getBooleanExtra("isFromSplash", true);
        // commented the location permission
        //checkPermissions(this, savedInstanceState, false);
        initUI(savedInstanceState);
      /*  LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        String accessToken = loginResult.getAccessToken()
                                .getToken();
                        //Log.d(TAG,"accessToken"+ accessToken);
                        fragmentSignIn.getFacebookProfile(loginResult, (Activity) context);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Toast.makeText(getApplicationContext(),"login Cancel",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
//                        Toast.makeText(getApplicationContext(),"login SERROR",Toast.LENGTH_LONG).show();
                        //Log.d(TAG,"ERROR CODE"+exception);
                    }
                });*/
    }

    public static void logData() {
        // Print status values in hex
        System.out.println("Nagravision certificateStatusBitField : 0x"
                + Integer.toHexString(certificateStatusBitField));
        System.out.println("Nagravision dateStatusBitField : 0x"
                + Integer.toHexString(dateStatusBitField));
        System.out.println("Nagravision contextStatusBitField : 0x"
                + Integer.toHexString(contextStatusBitField));
        System.out.println("Nagravision manifestStatusBitField : 0x"
                + Integer.toHexString(manifestStatusBitField));
        System.out.println("Nagravision dexSignatureStatusBitField : 0x"
                + Integer.toHexString(dexSignatureStatusBitField));
    }


    private void showRetryOption(boolean b) {
        if (b) {
            mLayoutRetry.setVisibility(View.VISIBLE);
            return;
        }
        mLayoutRetry.setVisibility(GONE);
    }

    public void onClicks(View v) {
        if(isNetworkConnected()) {
            showRetryOption(false);
            mProgressBar.setVisibility(View.VISIBLE);
            afterVideoCompleted();
        }else{
            Toast.makeText(mContext, mContext.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            showRetryOption(true);
        }
    }


    private void initUI(final Bundle savedInstanceState) {
        logData();
        if(MainActivity.isOpen && Util.checkUserLoginStatus() && getIntent() != null && getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL) != null) {
            launchMainActivityFlow();
           return;
        }
        if (isFromSplash) {
            /*int videoId = getResources().getIdentifier("splashvideo", "raw", mContext.getPackageName());
            if (videoId != 0) {
                String videoPath = "android.resource://" + mContext.getPackageName() + "/" + videoId;
//                    videoView.setVideoURI(Uri.parse(videoPath));
                try {
                    videoView.setRawData(videoId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
            try {
                if(videoView !=null) {
                    videoView.prepare(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            videoView.start();
                        }
                    });
                }
            } catch (IOException ioe) {
                //ignore
                Toast.makeText(mContext, "Restart your application", Toast.LENGTH_SHORT).show();
            }
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // initUI(savedInstanceState);
                    playCompleted = true;
                    if (servicesCompleted){
                        launchMainActivityFlow();
                    } else if(servicesError){
                        showErrorPopUp();
                    } else{
                        if(isNetworkConnected()) {
                            showRetryOption(false);
                            mProgressBar.setVisibility(View.VISIBLE);
                            afterVideoCompleted();
                        }else{
                            Toast.makeText(mContext, mContext.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                            showRetryOption(true);
                        }
                    }
                   /* mProgressBar.setVisibility(View.VISIBLE);
                    afterVideoCompleted();*/
                }
            });
        }else {
            afterVideoCompleted();
        }
    }
    private void afterVideoCompleted(){
        if (!PrefUtils.getInstance().isPreferredLanguageFragmentShown() && PrefUtils.getInstance().isPrefLangScreenEnabled()) {
            mCurrentFragment = FragmentPreferredLanguages.newInstance();
            ((FragmentPreferredLanguages) mCurrentFragment).setOnPreferredLanguagesActionPerformedListener(new FragmentPreferredLanguages.OnPreferredLanguagesActionPerformedListener() {
                @Override
                public void onSkipClicked() {
                    initUI(savedInstanceState);
                }

                @Override
                public void onDoneClicked() {
                    initUI(savedInstanceState);
                }
            });
            pushFragment(mCurrentFragment);
            return;
        }
        if (!interstitialShown)
            initBranch();
        Bundle args = new Bundle();
        mIsLoginDuringBrowse = getIntent().getBooleanExtra(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, false);
        if (!Util.checkUserLoginStatus())
            mIsLoginDuringBrowse = true;
        showPackagesDuringBrowse = getIntent().getBooleanExtra(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE, false);
        mSource = getIntent().getStringExtra(Analytics.PROPERTY_SOURCE);
        mSourceDetails = getIntent().getStringExtra(Analytics.PROPERTY_SOURCE_DETAILS);
        args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, getIntent().getBooleanExtra(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, false));
        args.putBoolean(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE, getIntent().getBooleanExtra(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE, false));
        args.putString(Analytics.PROPERTY_SOURCE, getIntent().getStringExtra(Analytics.PROPERTY_SOURCE));
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, getIntent().getStringExtra(Analytics.PROPERTY_SOURCE_DETAILS));
        if (getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME) != null) {
            args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME));
            args.putString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT));
            args.putString(APIConstants.NOTIFICATION_PARAM_LAYOUT, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_LAYOUT));
            args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
        }
        if(getIntent() != null && getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL) != null) {
            args.putString(APIConstants.NOTIFICATION_LAUNCH_URL, getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL));
        }
        if (getIntent().getBooleanExtra(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE, false)) {
            int subscriptionType = getIntent().getIntExtra(PackagesFragment.PARAM_SUBSCRIPTION_TYPE, 0);
            switch (subscriptionType) {
                case PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER:
                    mCurrentFragment = OfferFragment.newInstance(args);
                    break;
                default:
                    mCurrentFragment = PackagesFragment.newInstance(args);
                    break;
            }
//            args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, getIntent().getBooleanExtra(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, false));
//            args.putBoolean(PackagesFragment.PARAM_SHOW_PACKAGES_OFFER_SCREEN, getIntent().getBooleanExtra(PackagesFragment.PARAM_SHOW_PACKAGES_OFFER_SCREEN, false));
//            if (getIntent().getBooleanExtra(PackagesFragment.PARAM_SHOW_PACKAGES_OFFER_SCREEN, false)) {
//                mCurrentFragment = PackagesFragment.newInstance(args);
            pushFragment(mCurrentFragment);
        } /*else if (Util.checkUserLoginStatus() && PrefUtils.getInstance().getPrefEnableVmaxInterStitialAppOpenAd() && !interstitialShown) {
            interstitialShown = true;
            mCurrentFragment = VmaxInterstitialAdFragment.newInstance(null);
            pushFragment(mCurrentFragment);
        }*/ else if (savedInstanceState == null) {
           /* mCurrentFragment = LoginFragment.newInstance(args);
            pushFragment(mCurrentFragment);*/
            String shouldEnableDirectAppLaunch = PrefUtils.getInstance().getSignInFlowFlag();
            if (shouldEnableDirectAppLaunch != null) {
                if (shouldEnableDirectAppLaunch.equalsIgnoreCase(APIConstants.SIGN_IN_FLOW_ON_PLAY)) {
                    ApplicationController.ENABLE_DIRECT_APP_LAUNCH = true;
                } else if (shouldEnableDirectAppLaunch.equalsIgnoreCase(APIConstants.SIGN_IN_FLOW_ON_APP_OPEN)) {
                    ApplicationController.ENABLE_DIRECT_APP_LAUNCH = false;
                }
            }
            proceedDeviceRegistration();
        }
    }

    private void proceedDeviceRegistration() {
        //TODO need to go through hooq sdk initialization also handle the case like after offer consumption only need to start processing the notification data.
        if (PrefUtils.getInstance().getPrefOfferPackSubscriptionStatus()
                || PrefUtils.getInstance().getPrefIsSkipPackages()
                || ApplicationController.ENABLE_DIRECT_APP_LAUNCH) {
            if (Util.checkUserLoginStatus() && Util.onHandleExternalIntent(LoginActivity.this)) {
                finish();
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
            APIConstants.msisdnPath = getFilesDir() + "/" + "msisdn.bin";
        }
//        Analytics.createScreenGA(Analytics.SCREEN_SPLASH);
        FirebaseAnalytics.getInstance().createScreenFA(LoginActivity.this, Analytics.SCREEN_SPLASH);
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
                        if (getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
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
                        /*if (!isAdded()) {
                            return;
                        }*/
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

    private void showPackagesScreen() {
        Bundle args = new Bundle();
        args.putBoolean(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE, showPackagesDuringBrowse);
        pushFragment(PackagesFragment.newInstance(args));
    }

    private void showOTPLoginScreenFragment(String msisdn, boolean isExistingUser, boolean isLoginDuringBrowse) {
        LoggerD.debugLog("showOTPLoginScreenFragment: msisdn- " + msisdn);
        removeFragment(mCurrentFragment);
        Bundle args = new Bundle();
        args.putString(FragmentOTPVerification.PARAM_MSISDN, msisdn);
        args.putBoolean(FragmentOTPVerification.PARAM_IS_EXISTING_USER, isExistingUser);
        args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
        args.putString(Analytics.PROPERTY_SOURCE, mSource);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSourceDetails);
        args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);

        pushFragment(FragmentOTPVerification.newInstance(args));

    }

    private void showSignInScreenFragment(String msisdn, boolean isExistingUser, boolean isLoginDuringBrowse) {
        LoggerD.debugLog("showSignUpScreenFragment: msisdn- " + msisdn);
        //  if (mCurrentFragment != null) {
        //     removeFragment(mCurrentFragment);
        if(getIntent() != null && getIntent().hasExtra("isFromPendingSMC") && getIntent().getBooleanExtra("isFromPendingSMC", false)) {
            showSignUpScreenFragment();
            return;
        }
        Bundle args = new Bundle();
        args.putString(FragmentOTPVerification.PARAM_MSISDN, msisdn);
        args.putBoolean(FragmentOTPVerification.PARAM_IS_EXISTING_USER, isExistingUser);
        args.putBoolean(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
        args.putString(Analytics.PROPERTY_SOURCE, mSource);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSourceDetails);
        args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
        args.putBoolean("isFromSplash", isFromSplash);
        if (getIntent().hasExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID)) {
            final String _id = getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);
            args.putString(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, _id);
        }
        if (getIntent().hasExtra(APIConstants.NOTIFICATION_LAUNCH_URL)) {
            final String launchURL = getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL);
            args.putString(APIConstants.NOTIFICATION_LAUNCH_URL, launchURL);
        }
        pushFragment(FragmentSignIn.newInstance(args));
        //   }
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

    private void fetchRequiredData() {
        if (ConnectivityUtil.isConnected(getApplicationContext())) {
            sendGcmRequest();
            String clientSecret = getResources().getString(R.string.clientSecret);
            if (!BuildConfig.FLAVOR.contains("idea")) {
                clientSecret = "all";
            }
            APIService.getInstance().updateProperties(getApplicationContext(), PrefUtils.getInstance().getPrefClientkey(), SDKUtils
                    .getInternetConnectivity(getApplicationContext()), SDKUtils.getMCCAndMNCValues(getApplicationContext()), Util
                    .getAppVersionName(getApplicationContext()), clientSecret);

        }
    }

    private void sendGcmRequest() {
        if (getApplicationContext() == null) {
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
            if (getApplicationContext() != null) {
                RegistrationJobScheduler.enqueueWork(getApplicationContext(), new Intent());
            }
        }
    }

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(LoginActivity.this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerDevice() {
//Device registration call
        DeviceRegistrationEncryptedShreyas.Params deviceRegparams = null;
        if (getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
            //Log.d(TAG, "Dev reg with hardcoded msisdn header + msisdn- " + getString(R.string.current_msisdn));
            deviceRegparams = new DeviceRegistrationEncryptedShreyas.Params("SunDirectAndroid", getString(R.string.current_msisdn));
        } else {
            //Log.d(TAG, "Dev reg with out hardcoded msisdn");
            deviceRegparams = new DeviceRegistrationEncryptedShreyas.Params("SunDirectAndroid");
        }
        deviceRegistration = new DeviceRegistrationEncryptedShreyas(LoginActivity.this, deviceRegparams,
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
                        params.put(Analytics.DEVICE_DESC, getString(R.string.osname));
                        if (!devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                            showDeviceAuthenticationFailed("Code: " + response.body().code +
                                    " Msg: " + response.body().message);
                            params.put(Analytics.REASON_FAILURE, devRegResponse.message);
                            params.put(Analytics.ERROR_CODE, String.valueOf(devRegResponse.code));
                            Analytics.mixpanelDeviceRegistrationFailed(params);
                            return;
                        }
                        if (devRegResponse.status.equalsIgnoreCase("SUCCESS") && devRegResponse.code == 205) {
                            PrefUtils.getInstance().setPrefLoginStatus("success");
                            launchMainActivity();
                        } else if (devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
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

    private void showOnBoardingScreen() {
        LoggerD.debugLog("showOnBoardingScreen:");
        isOnBoardingTaskCompleted = false;
        isTimeOutReached = false;
        //  fetchOttAppsDataAndShowBanners();
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

    private void registerDeviceEncryption() {
//Device registration call
        DeviceRegistrationEncryptedShreyas.Params deviceRegparams = null;
        String clientSecret = getResources().getString(R.string.clientSecret);
        Log.d("clientSecret", "clientSecret - " + clientSecret);
        if (getResources().getBoolean(R.bool.is_hardcoded_msisdn)) {
            //Log.d(TAG, "Dev reg with hardcoded msisdn header + msisdn- " + getString(R.string.current_msisdn));

            deviceRegparams = new DeviceRegistrationEncryptedShreyas.Params(clientSecret, getString(R.string.current_msisdn));
        } else {
            //Log.d(TAG, "Dev reg with out hardcoded msisdn");
            deviceRegparams = new DeviceRegistrationEncryptedShreyas.Params(clientSecret);
        }
        DeviceRegistrationEncryptedShreyas deviceRegistrationEncryptedPayLoad = new DeviceRegistrationEncryptedShreyas(LoginActivity.this, deviceRegparams,
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
                           /* if (!isAdded()) {
                                return;
                            }*/
                            if (ApplicationController.ENABLE_DIRECT_APP_LAUNCH) {
                                if (mDeviceRegData != null && mDeviceRegData.appLoginConfig != null && mDeviceRegData.appLoginConfig.msisdn != null) {
                                    //Log.d(TAG, "mDeviceRegData.appLoginConfig.msisdn- " + mDeviceRegData.appLoginConfig.msisdn);
                                    if (mDeviceRegData.appLoginConfig.msisdn != null) {
                                        PrefUtils.getInstance().setPrefMsisdnNo(mDeviceRegData.appLoginConfig.msisdn);
                                        PrefUtils.getInstance().setPrefTempMsisdn(mDeviceRegData.appLoginConfig.msisdn);
                                    }
                                }
                                fetchRequiredData();
                                if (devRegResponse.status.equalsIgnoreCase("SUCCESS") && devRegResponse.code == 205) {
                                    PrefUtils.getInstance().setPrefLoginStatus("success");
                                    if (!Util.checkUserLoginStatus()){
                                        String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
                                        showSignInScreenFragment(msisdn, false, mIsLoginDuringBrowse);
                                        return;
                                    }else {
                                        launchMainActivity();
                                        return;
                                    }
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
                                    String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
                                    showSignInScreenFragment(msisdn, false, mIsLoginDuringBrowse);
                                    return;
                                }
                                if (!Util.checkUserLoginStatus()){
                                    String msisdn = PrefUtils.getInstance().getPrefMsisdnNo();
                                    showSignInScreenFragment(msisdn, false, mIsLoginDuringBrowse);
                                    return;
                                }else {
                                    launchMainActivity();
                                    return;
                                }
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
                            showDeviceAuthenticationFailed(getString(R.string.network_error));
                            return;
                        }
                        if (t instanceof SSLPeerUnverifiedException) {
                            String message = getResources().getString(R.string.dev_auth_failed_message);
                            showDeviceAuthenticationFailed(message + "..");
                            return;
                        }
                        showDeviceAuthenticationFailed(null);
                    }
                });

        APIService.getInstance().execute(deviceRegistrationEncryptedPayLoad);
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
                    launchofferUserScreen();
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

    private void makeMSISDNLogin(final String msisdn, String imsi, final boolean isFromMsisdnReterival) {
        /*//Log.d(TAG, "makeMSISDNLogin: msisdn: " + msisdn + "" +
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
                            if (ConnectivityUtil.isConnectedWifi(getApplicationContext())) {
                                launchofferUserScreen();
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
                            if (!TextUtils.isEmpty(response.body().serviceName)) {
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
                                        APIConstants.msisdnPath = getFilesDir() + "/" + "msisdn.bin";
                                    }
                                    SDKUtils.saveObject(msisdnData, APIConstants.msisdnPath);
                                }
                                //Log.d(TAG, "Info: msisdn login: " + "success and launching offer");
                                try {
                                    //Log.d(TAG, "Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                                    PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                                    Util.setUserIdInMyPlexEvents(getApplicationContext());
                                    if (!TextUtils.isEmpty(response.body().serviceName)) {
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
                                if (response.body().code == 201) {
                                    PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(false);
                                    AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                                } else if (response.body().code == 200) {
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
                                Util.setUserIdInMyPlexEvents(getApplicationContext());
                                if (!TextUtils.isEmpty(response.body().serviceName)) {
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
                            if (response.body().code == 201) {
                                PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(false);
                                AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                            } else if (response.body().code == 200) {
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
                            showDeviceAuthenticationFailed(getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailed(null);
                    }
                });
        //   APIService.getInstance().execute(login);
    }

    private void launchofferUserScreen() {
        Bundle args = new Bundle();

        OfferFragment offerFragment = OfferFragment.newInstance(args);
        pushFragment(offerFragment);
    }

    private void showDeviceAuthenticationFailed(String message) {
        if (message == null) {
            message = getResources().getString(R.string.dev_auth_failed_message);
        }
        AlertDialogUtil.showNeutralAlertDialog(getApplicationContext(), message, "", getResources()
                        .getString(R.string.feedbackokbutton),
                new AlertDialogUtil.NeutralDialogListener() {
                    @Override
                    public void onDialogClick(String buttonText) {
                        finish();
                    }
                });
    }

    private void launchMainActivity() {
        // getUserConsent();
        makeUserProfileRequest();
        //makeUserProfileRequest();
    }

    private void launchMainActivity(String message) {
        //   Activity activity = mActivity;
        //  if (activity == null || activity.isFinishing()) return;
        Bundle bundle;
        //  if (getArguments() != null && getArguments().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME)) {
        bundle = new Bundle();
        bundle.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME));
        bundle.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE));
        bundle.putString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT));
        bundle.putString(APIConstants.NOTIFICATION_PARAM_LAYOUT, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_LAYOUT));
        if(getIntent() != null && getIntent().hasExtra(APIConstants.NOTIFICATION_LAUNCH_URL) && getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL)!= null)
            bundle.putString(APIConstants.NOTIFICATION_LAUNCH_URL, getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL));
        MainActivityLauncherUtil.setNotificationViewAllData(bundle);
        //  }
        MainActivityLauncherUtil.initStartUpCalls(LoginActivity.this, message);
    }

    AlertDialog dialog;

    private void showErrorPopUp() {
        if(!playCompleted) {
            servicesError = true;
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setMessage(getResources().getString(R.string.error_popup));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                finish();
            }
        });
        dialog = builder.create();

        if (Util.checkActivityPresent(LoginActivity.this)) {
            dialog.show();
            dialog.setCancelable(false);
        }
    }

    private void getUserConsent() {
        if (playCompleted)
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
                if (Util.isNetworkAvailable(getApplicationContext())) {
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

    private void showNoNetworkPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setMessage(getResources().getString(R.string.network_error));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                finish();
            }
        });
        dialog = builder.create();

        if (Util.checkActivityPresent(LoginActivity.this)) {
            dialog.show();
            dialog.setCancelable(false);
        }
    }

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
                if (playCompleted)
                    mProgressBar.setVisibility(View.VISIBLE);
                //Log.d(TAG, "WebView onPageStarted...");
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Log.d(TAG, "WebView Processing webview url click...");

                if (!Util.isNetworkAvailable(getApplicationContext())) {
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
                                    finish();
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
                    launchofferUserScreen();
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
                                    AlertDialogUtil.showNeutralAlertDialog(getApplicationContext(), response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
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
                                    startActivityForResult(SubscriptionWebActivity.createIntent(mContext, response.body().ui.redirect, SubscriptionWebActivity.PARAM_LAUNCH_HOME), SubscriptionWebActivity.SUBSCRIPTION_REQUEST);
                                    finish();
                                }
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_OFFER:

                                pushFragment(OfferFragment.newInstance(null));

                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION:
                                pushFragment(PackagesFragment.newInstance(null));
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
                    if (isOfferAvailable) {
                        pushFragment(OfferFragment.newInstance(null));
                        return;
                    }

                    if (PrefUtils.getInstance().getPrefIsSkipPackages()) {
                        launchMainActivity();
                        return;
                    }

                    pushFragment(PackagesFragment.newInstance(null));

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

        //  APIService.getInstance().execute(contentDetails);
    }


    private void makeUserProfileRequest() {
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

                        if (responseData.result.profile.name != null && !TextUtils.isEmpty(responseData.result.profile.name)) {
                            PrefUtils.getInstance().setPrefFullName(responseData.result.profile.name );
                        }
                        if (responseData.result.profile.mobile_no != null && !TextUtils.isEmpty(responseData.result.profile.mobile_no)) {
                            PrefUtils.getInstance().setPrefMobileNumber(responseData.result.profile.mobile_no);
                        }
                        if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                            if (responseData.result.profile.locations.get(0) != null
                                    && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                                PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
                        }

                        if (responseData.result.profile.state != null && !TextUtils.isEmpty(responseData.result.profile.state)) {
                            PrefUtils.getInstance().setUserState(responseData.result.profile.state);
                        }

                        if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
                            PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
                        }

                        if (responseData.result.profile.showForm) {
                            /*Intent ip=new Intent(mContext, MandatoryProfileActivity.class);
                            startActivityForResult(ip,PROFILE_UPDATE_REQUEST);*/
                            PrefUtils.getInstance().setIsToShowForm(true);
                            launchMainActivityAfterProfileCheck();
                        } else {
                            launchMainActivityAfterProfileCheck();
                        }
                    } else {
                        launchMainActivityAfterProfileCheck();
                    }
                } else {
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

    private void launchMainActivityAfterProfileCheck() {
        //  Activity activity = mActivity;
        //    if (activity == null || activity.isFinishing()) return;
        servicesCompleted = true;
        if (playCompleted) {
            Bundle bundle;
            // if (getArguments() != null && getArguments().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME)) {
            bundle = new Bundle();

            bundle.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME));
            bundle.putString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT));
            bundle.putString(APIConstants.NOTIFICATION_PARAM_LAYOUT, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_LAYOUT));
            if(getIntent() != null && getIntent().hasExtra(APIConstants.NOTIFICATION_LAUNCH_URL) && getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL) != null)
                bundle.putString(APIConstants.NOTIFICATION_LAUNCH_URL, getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL));
            MainActivityLauncherUtil.setNotificationViewAllData(bundle);
            //      }
            MainActivityLauncherUtil.initStartUpCalls(LoginActivity.this);
        }
    }

    public void launchMainActivityFlow() {
        Bundle bundle;
        // if (getArguments() != null && getArguments().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME)) {
        bundle = new Bundle();

        bundle.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME));
        bundle.putString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT));
        bundle.putString(APIConstants.NOTIFICATION_PARAM_LAYOUT, getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_LAYOUT));
        if(getIntent() != null && getIntent().hasExtra(APIConstants.NOTIFICATION_LAUNCH_URL) &&getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL) != null)
            bundle.putString(APIConstants.NOTIFICATION_LAUNCH_URL, getIntent().getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL));
        MainActivityLauncherUtil.setNotificationViewAllData(bundle);
        //      }
        MainActivityLauncherUtil.initStartUpCalls(LoginActivity.this);
    }

    public void deepLinkingAppsFlyer() {
        /** Set Up Conversion Listener to get attribution data **/




        /* This API enables AppsFlyer to detect installations, sessions, and updates. */

    }

    private void constructDeepLinkUrl(String contentId) {
        Log.d("LOG_TAG", "contentId: " + contentId);
        Intent intent = new Intent(this, UrlGatewayActivity.class);
        intent.setData(Uri.parse("https://www.sundirectplay.in/tv/detail/" + contentId + "/"));
        Log.d("LOG_TAG", "Intent: " + "https://www.sundirectplay.in/tv/detail/" + contentId + "/");
        startActivity(intent);
        return;
    }

    private boolean shouldShowInterstitial() {
        return true;
    }

    private void initBranch() {
        try {

            //CleverTapAPI cleverTapAPI = ApplicationController.getCleverTap(getApplication());
            if (!PrefUtils.getInstance().getPrefEnableBranchIOAnalytics()) {
                SDKLogger.debug("Oops, branch is disabled from properties");
                return;
            }
            Branch branch = Branch.getInstance();
           /* String clevertapAttributionId = cleverTapAPI.getCleverTapAttributionIdentifier();
            if (ApplicationController.getAppsFlyerLibInstance() != null) {
                ApplicationController.getAppsFlyerLibInstance().setCustomerUserId(clevertapAttributionId);
            }*/
            /*branch.setRequestMetadata("$clevertap_attribution_id",
                    clevertapAttributionId);
            SDKLogger.debug("clevertapAttributionId- " + clevertapAttributionId);*/
            // Branch init
            Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
                @Override
                public void onInitFinished(JSONObject referringParams, BranchError error) {
                    if (error == null) {
                        SDKLogger.debug("BranchIO " + referringParams.toString());
                    } else {
                        SDKLogger.debug("BranchIO " + error.getMessage());
                    }
                    try {
                        if (referringParams.has(APIConstants.DEEPLINK_URL) && referringParams.get(APIConstants.DEEPLINK_URL) != null) {
                            String deepLinkUrl = referringParams.get("deepLinkUrl").toString();
                            SDKLogger.debug("BranchIO DeepLinkUrl" + deepLinkUrl);
                            // Need to launch from here directly
                            handleExternalUrl(Uri.parse(deepLinkUrl));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, getIntent().getData(), this);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void setOrientation(int value) {
        setRequestedOrientation(value);
    }

    @Override
    public int getOrientation() {
        return 0;
    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void showActionBar() {

    }

    @Override
    public void pushFragment(BaseFragment fragment) {
        if (isFinishing() || fragment == null) {
            return;
        }
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            removeFragment(mCurrentFragment);
            transaction.add(R.id.container, fragment);
            mCurrentFragment = fragment;
            fragment.setBaseActivity(this);
            fragment.setContext(this);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
            if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            } else {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        } catch (Throwable e) {
            e.printStackTrace();

        }

    }

    @Override
    public void removeFragment(BaseFragment fragment) {
        if (isFinishing() || fragment == null) {
            return;
        }
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (mCurrentFragment != null) {
                transaction.remove(mCurrentFragment);
            }
            transaction.commitAllowingStateLoss();

        } catch (Throwable e) {
            e.printStackTrace();

        }
    }

    private int exitBackPressCounts = 1;
    private final int EXIT_ON_BACK_COUNT = 1;
    private final int RESET_BACKPRESS_TIMER = 5 * 1000;
    private boolean mShowExitToast = true;

    private boolean closeApplication() {
        if (mShowExitToast) {
            exitBackPressCounts++;
            //Log.d(TAG, "back press count " + exitBackPressCounts);
            if (exitBackPressCounts > EXIT_ON_BACK_COUNT) {
                //Log.d(TAG, "back press count reached to max " + exitBackPressCounts);
                AlertDialogUtil.showToastNotification("Press back again to close the application.");
                mShowExitToast = false;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        mShowExitToast = true;
                        exitBackPressCounts = 0;
                        //Log.d(TAG, "timer reset back press count " + exitBackPressCounts);
                    }
                }, RESET_BACKPRESS_TIMER);
            }
            return false;
        } else {
            //Log.d(TAG, "exiting App");
            exitApp();
            return true;
        }
    }

    private void exitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.runFinalizersOnExit(true);
        System.exit(0);
        finish();
    }

    @Override
    public void onBackPressed() {
        try {
            if (getIntent() != null
                    && (getIntent().hasExtra(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE)
                    || getIntent().hasExtra(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE))) {
                finish();
                return;
            }
         /*   if(isForgotPassword) {
                isForgotPassword = false;
                finish();
                return;
            }*/
            if (mCurrentFragment != null && mCurrentFragment instanceof VmaxInterstitialAdFragment) {
                removeFragment(mCurrentFragment);
                getSupportFragmentManager().executePendingTransactions();
                initUI(null);
                return;
            }
            if (mCurrentFragment != null && mCurrentFragment instanceof FragmentResetPassword) {
                if(((FragmentResetPassword) mCurrentFragment).backHandle()) {
                    removeFragment(mCurrentFragment);
                    Bundle args = new Bundle();
                    args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                    pushFragment(FragmentSignIn.newInstance(args));
                }
                return;
            }
            if (mCurrentFragment != null && mCurrentFragment instanceof com.myplex.myplex.ui.fragment.FragmentOTPVerification) {

                return;
            }
            if (mCurrentFragment != null && mCurrentFragment instanceof FragmentSignUp) {
                if(((FragmentSignUp) mCurrentFragment).backHandle()) {
                    removeFragment(mCurrentFragment);
                    Bundle args = new Bundle();
                    args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                    pushFragment(FragmentSignIn.newInstance(args));
                }
                return;

            }
            if (mCurrentFragment != null && mCurrentFragment instanceof FragmentGetNewConnection) {
                if(((FragmentGetNewConnection) mCurrentFragment).backHandle()) {
                    removeFragment(mCurrentFragment);
                    Bundle args = new Bundle();
                    args.putString("isFrom", "new_user");
                    args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                    pushFragment(FragmentSignIn.newInstance(args));
                }
                return;

            }
            if (mCurrentFragment != null && mCurrentFragment instanceof FragmentCreatePassword) {
                removeFragment(mCurrentFragment);
               /* Bundle args = new Bundle();
                args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                pushFragment(FragmentSignIn.newInstance(args));*/
                if(((FragmentCreatePassword) mCurrentFragment != null)) {
                    FragmentCreatePassword fragmentCreatePassword = (FragmentCreatePassword) mCurrentFragment;
                    Bundle args = new Bundle();
                    if(fragmentCreatePassword.isRegister) {
                        if(fragmentCreatePassword.backHandle()) {
                            args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                            args.putString("mobile_number", fragmentCreatePassword.mobilenumber);
                            args.putString("full_name", fragmentCreatePassword.name);
                            args.putString("otp", fragmentCreatePassword.otp);
                            args.putString("smart_card_number", fragmentCreatePassword.smcNumber);
                            args.putString("new_mobile", fragmentCreatePassword.newMobile);
                            args.putString("new_otp", fragmentCreatePassword.newOtp);
                            args.putString("newSMCRequest", fragmentCreatePassword.newSMCRequest);
                            pushFragment(FragmentSignUp.newInstance(args));
                        }
                    } else {
                        args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                        args.putString("mobile_number", fragmentCreatePassword.mobilenumber);
                        pushFragment(FragmentResetPassword.newInstance(args));
                    }
                }
                return;
            }
            if (mCurrentFragment != null && mCurrentFragment instanceof FragmentNewUser){
                removeFragment(mCurrentFragment);
                super.onBackPressed();
                return;
            }
         /*   if (mCurrentFragment != null && mCurrentFragment instanceof FragmentGetNewConnection){
                removeFragment(mCurrentFragment);
                super.onBackPressed();
                return;
            }*/
            if (mCurrentFragment != null && mCurrentFragment instanceof FragmentSetTopBoxes){
                FragmentSetTopBoxes  setTopBoxes= (FragmentSetTopBoxes) mCurrentFragment;
                removeFragment(mCurrentFragment);
                Bundle args = new Bundle();
                args.putString("name", setTopBoxes.name);
                args.putString("mobile", setTopBoxes.mobilenumber);
                args.putString("pincode", setTopBoxes.pincode);
                args.putString("emailID", setTopBoxes.email);
                args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                pushFragment(FragmentGetNewConnection.newInstance(args));
               // super.onBackPressed();
                return;
            }
            if (mCurrentFragment != null && mCurrentFragment instanceof FragmentRegisterCreatePassword){
                FragmentRegisterCreatePassword  registerCreatePassword= (FragmentRegisterCreatePassword) mCurrentFragment;
                removeFragment(mCurrentFragment);
                Bundle args = new Bundle();
                args.putString("name", registerCreatePassword.name);
                args.putString("mobile", registerCreatePassword.mobilenumber);
                args.putString("pincode", registerCreatePassword.pincode);
                args.putString("emailID", registerCreatePassword.email);
                args.putString("isFrom", registerCreatePassword.isFrom);
                args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                pushFragment(FragmentGetNewConnection.newInstance(args));
                // super.onBackPressed();
                return;
            }

            if (mCurrentFragment != null
                    && mCurrentFragment.onBackClicked()) {
                return;
            }
            if (closeApplication()) {
                exitApp();
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Intent createIntent(Context mContext,
                                      boolean isLoginDuringBrowse,
                                      boolean showPackagesScreen,
                                      int subscriptionType,
                                      String source,
                                      String sourceDetails) {
        Intent loginIntent = new Intent(mContext, LoginActivity.class);
        mIsLoginDuringBrowse = isLoginDuringBrowse;
        if (!Util.checkUserLoginStatus())
            mIsLoginDuringBrowse = true;
        showPackagesDuringBrowse = showPackagesScreen;
        mSource = source;
        mSourceDetails = sourceDetails;
        isFromSplash = false;
        loginIntent.putExtra(FragmentOTPVerification.PARAM_LOGIN_DURING_BROWSE, isLoginDuringBrowse);
        loginIntent.putExtra(PackagesFragment.PARAM_SHOW_PACKAGES_DURING_BROWSE, showPackagesScreen);
        loginIntent.putExtra(PackagesFragment.PARAM_SUBSCRIPTION_TYPE, subscriptionType);
        loginIntent.putExtra(Analytics.PROPERTY_SOURCE, source);
        loginIntent.putExtra(Analytics.PROPERTY_SOURCE_DETAILS, sourceDetails);
        loginIntent.putExtra("isFromSplash", false);
        //isFromSplash = false;
        return loginIntent;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
//        HooqVstbUtilityHandler.getInstance().onRequestPermissionsResult(this, requestCode,permissions,grantResults);
        SDKLogger.debug("onRequestPermission called");
      /*  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: show Dialog
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    SDKLogger.debug("Don't ask again selected by user");
                    showDialog(true);

                } else
                    showDialog(false);
            }
            SDKLogger.debug("Mandatory permissions Not Accepted");
        } else*/ {
            SDKLogger.debug("Mandatory permissions Accepted");
            initUI(savedInstanceState);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showDialog(final boolean shouldShowRationale) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        if (!shouldShowRationale) {
            alertDialogBuilder.setMessage(getString(R.string.permission_warning_text));
            alertDialogBuilder.setTitle(getString(R.string.permission_text));
        } else {
            alertDialogBuilder.setMessage(getString(R.string.permission_warning_settings));
            alertDialogBuilder.setTitle(getString(R.string.permission_text));
        }
        alertDialogBuilder.setPositiveButton(shouldShowRationale ? getString(R.string.accept_from_settings) : getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (shouldShowRationale) {
                    //TODO: Redirect to settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                } else
                    checkPermissions(LoginActivity.this, savedInstanceState, true);

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LoggerD.debugLog("LoginActivity: onActivityResult: resultCode- " + resultCode);
        //Log.d(TAG, "mActivity :this" + this);
        if (requestCode == GMAIL_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (fragmentSignIn != null)
                fragmentSignIn.handleGmailSignInResult(task, this);
        } else {
           /* if (FacebookSdk.isFacebookRequestCode(requestCode)) {
                if (callbackManager != null)
                    callbackManager.onActivityResult(requestCode, resultCode, data);
            }*/
        }
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            initUI(savedInstanceState);
        }
        if (mCurrentFragment != null) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try {
//            CleverTapAPI.getInstance(this).event.pushNotificationEvent(intent.getExtras());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        LoggerD.debugLog("CleverTap: LoginActivity: onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
    }

    @Override
    public void onPause() {
        try {
            super.onPause();
            isVisible = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            isVisible = true;
            if (!ConnectivityUtil.isConnected(getApplicationContext())) {
                Toast.makeText(mContext, mContext.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isActivityVisible() {
        return isVisible;
    }


    //To handle DeepLinks from Branch IO

    private boolean handleExternalUrl(Uri uri) {

        boolean intentHandled = false;
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        if (uri == null || uri.getScheme() == null) {
            startMainActivity(intent);
            return intentHandled;
        }
        SchemeType scheme = SchemeType.valueOf(uri.getScheme());
        List<String> list = new ArrayList<>();
        if (uri.getPathSegments() != null) {
            list = uri.getPathSegments();
        }


        switch (scheme) {
            case http:
            case https:
            default:
                if (list != null) {
                    int indexOfPathQuery = 0;
                    String firstPathSegment = null;
                    try {
                        if (list.contains(PATH_PAGE)) {
                            indexOfPathQuery = list.indexOf(PATH_PAGE);
                        } else if (list.contains(PATH_WATCH)) {
                            indexOfPathQuery = list.indexOf(PATH_WATCH);
                        } else if (list.contains(PATH_DETAIL)) {
                            indexOfPathQuery = list.indexOf(PATH_DETAIL);
                        } else if (list.contains(PATH_LIVE)) {
                            indexOfPathQuery = list.indexOf(PATH_LIVE);
                        }
                        firstPathSegment = list.get(indexOfPathQuery);
                        String secondPathSegment = list.get(indexOfPathQuery + 1);
                        intentHandled = true;
                        switch (firstPathSegment) {
                            case PATH_WATCH:
                            case PATH_DETAIL:
                            case PATH_LIVE:
                                intent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, secondPathSegment);
                                break;
                            case PATH_PAGE:
                                intent.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE, secondPathSegment);
                                break;
                        }
                        intent.putExtra(APIConstants.MESSAGE_TYPE, APIConstants.NOTIFICATION_PARAM_MESSAGE_TYPE_INAPP);

                    } catch (Exception e) {
                        //Log.d(TAG, "Invalid content id:" + firstPathSegment);
                        //Log.d(TAG, e.getMessage());
                    }
                }
                break;
        }
        startMainActivity(intent);
        return intentHandled;
    }

    private void startMainActivity(Intent intent) {
        if (intent == null) {
            intent = new Intent(this, LoginActivity.class);
        }
        finish();
        startActivity(intent);
    }

    Bundle savedInstanceState;

    private void checkPermissions(Context context, Bundle savedInstanceState, boolean isMandatoryPermissionsOnly) {
        this.savedInstanceState = savedInstanceState;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          /*  if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions;
                permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            } else {
                initUI(savedInstanceState);
            }*/
        } else
            initUI(savedInstanceState);
    }

    private OnCompleteListener mOnCompleteListener = task -> {
        FirebaseRemoteConfig mConfig = ApplicationController.getmFirebaseRemoteConfig();
        mConfig.activate();
    };


    private OnFailureListener mOnFailureListener = e -> e.printStackTrace();

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

    private boolean isNetworkConnected(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if(connectivityManager!=null){
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!=null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
                return true;
            return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;

        }
            return false;
    }
    private void showSignUpScreenFragment() {
        Bundle args = new Bundle();
        args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
        pushFragment(FragmentSignUp.newInstance(args));
    }
}