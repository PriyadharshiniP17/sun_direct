package com.myplex.myplex.utils;


import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static com.myplex.myplex.ApplicationController.getAppContext;
import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;
import static com.myplex.myplex.subscribe.SubscriptionWebActivity.isFromSignIn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.user.DeviceUnRegRequest;
import com.myplex.api.request.user.MSISDNLoginEncryptedShreyas;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.api.request.user.ProfileUpdateWithEmailIDRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.MsisdnData;
import com.myplex.model.OfferResponseData;
import com.myplex.model.SocialLoginData;
import com.myplex.model.UserSigninResponse;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.FragmentResetPassword;
import com.myplex.myplex.ui.fragment.OfferFragment;
import com.myplex.myplex.ui.fragment.PackagesFragment;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;

import java.util.HashMap;
import java.util.Map;

/*import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;*/


/**
 * Created by Srikanth on 10/21/2014.
 */
public class FragmentSignIn extends BaseFragment {

    public static final String PARAM_MSISDN = "msisdn";
    public static final String PARAM_IS_EXISTING_USER = "is_existing_user_to_disable_mobile_number";
    public static final String PARAM_LOGIN_DURING_BROWSE = "during_browse";
    public static final String PARAM_UPDATE_EMAIL = "update_email";
    private View rootView;
    public static final String TAG = "FragmentSignIn";


    private EditText mMobileNoEditText;
    private TextInputEditText mEmailEditText;
    private TextInputEditText mPwdEditText;
    private TextView mFrgtPassword;
    private Button signUpButton;
    private Button emailButton;
    private Button phnButton;
    private Context mContext;
    private OtpReader mOtpReader;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int GMAIL_SIGN_IN = 111;
    private String otp, phnNum, userEmail, userId;
    private int numberOfLoginAttempts = 0;
    private GoogleApiClient apiClient;
    private Button social_fb_login;
    private FragmentSignIn fragmentSignIn;
    private TextView mobileValid, passwordValid, newUser, signinTv, newUserTv;
    private View newuserLine, signinLine;
    private RelativeLayout sign_in_ll, new_user_ll;
    private LinearLayout signin_ll, new_user_layout;

//    private CallbackManager callbackManager;


    private View.OnClickListener mSignInContinueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    private View.OnClickListener mStep1ClickListenerResendOTP = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            showAndProceedStep2WaitForAutoDetect();
//            Attach and use this for Resend otp action
           /* if (mOTPEditText != null) {
                hideSoftInputKeyBoard(mOTPEditText);
            }
            mOTPEditText.setText("");
            mOTPEditText.clearFocus();*/
        }
    };

    private View.OnClickListener mStep3ClickListenerChangeNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Attach and use this to Redirect and change his number similar to start step 1.
            if (mBaseActivity != null) {
                mBaseActivity.removeFragment(FragmentSignIn.this);
                mBaseActivity.pushFragment(FragmentSignIn.newInstance(getArguments()));
            }
        }
    };

    private View.OnClickListener mStep3ClickListenerOTPManuallySubmit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*if (mOTPEditText != null) {
                hideSoftInputKeyBoard(mOTPEditText);
            }*/
            //showAndProceedStep4SendManualOTP();
        }
    };

    private View.OnClickListener mSkipClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PrefUtils.getInstance().setPrefIsOTPSkipped(true);
            launchMainActivity();
        }
    };

    private String mMobileNo;
    private String mEmailID;
    private String mPassword;

    private ProgressDialog mProgressDialog;
    private boolean isOtpRequestManualEnter;
    private TextView mTextViewNote;
    private TextView mTextViewSkip;
    private boolean mIsExistingUser;
    private boolean isOtpLogin;
    private boolean mIsLoginDuringBrowse;
    private boolean isFromSplash;
    private Typeface msundirectRegularFontTypeFace;
    private TextView mTextViewTnC;
    private ImageView mImageViewEmailIdTickMark;
    private ImageView mImageViewMobileNoTickMark;
    private boolean showMobNoWrongTickmark = false;
    private boolean showEmailIdTickMark = false;
    private boolean isSubscriptionFailed = false;
    private String source;
    private String sourceDetails;
    private String notificationId, launchURL;
    private boolean emailSupported = false;
    private boolean mobileNoSupported = true;
    private TabLayout tabLayout;
    Typeface amazonEmberRegular, amazonEmberBold;
    private AppCompatButton getNewConnection, subscribeToApps,exploreOffers;
//    private LoginButton loginButton;


//    private FrameLayout mFrameLayout;


    public static FragmentSignIn newInstance(Bundle args) {
        FragmentSignIn fragmentOTPVerification = new FragmentSignIn();
        fragmentOTPVerification.setArguments(args);
        return fragmentOTPVerification;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateHorizontalSpacing();
/*        mFrameLayout.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_otp, null);
        initComponent();
        mFrameLayout.addView(rootView);*/
    }
    int portraitWidth;
    LinearLayout ll_space_root;
    private void updateHorizontalSpacing() {
        if(DeviceUtils.isTabletOrientationEnabled(mContext)){
            if(DeviceUtils.getScreenOrientation(mContext) != SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                if(portraitWidth <= 0)
                    portraitWidth = ll_space_root.getLayoutParams().width;
                ll_space_root.getLayoutParams().width =portraitWidth;
            }else {
                ll_space_root.getLayoutParams().width = (int)(0.45 * getResources().getDisplayMetrics().widthPixels);
            }

        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        amazonEmberRegular = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_regular.ttf");
        amazonEmberBold = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
//        mFrameLayout = new FrameLayout(mContext);
        rootView = inflater.inflate(R.layout.signin_paas, container, false);
        readBundleValues(getArguments());
        source = null;
        if (getArguments() != null && getArguments().containsKey(Analytics.PROPERTY_SOURCE)) {
            source = getArguments().getString(Analytics.PROPERTY_SOURCE);
        }


        sourceDetails = null;
        if (getArguments() != null && getArguments().containsKey(Analytics.PROPERTY_SOURCE_DETAILS)) {
            sourceDetails = getArguments().getString(Analytics.PROPERTY_SOURCE_DETAILS);
        }
        if (getArguments() != null && getArguments().containsKey(APIConstants.NOTIFICATION_PARAM_CONTENT_ID)) {
            notificationId = getArguments().getString(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);
        }
        if (getArguments() != null && getArguments().containsKey(APIConstants.NOTIFICATION_LAUNCH_URL)) {
            launchURL = getArguments().getString(APIConstants.NOTIFICATION_LAUNCH_URL);
        }
        CleverTap.eventRegistrationPageViewed(source, sourceDetails);
//        Update the email when email is not available during login

        initComponent();

//        mFrameLayout.addView(rootView);

        String serverClientId = mContext.getResources().getString(R.string.server_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, googleSignInOptions);

      /*  twitter_login = (TwitterLoginButton)rootView.findViewById(R.id.twitter_sign_up_social);
        if(twitter_login!=null) {
            twitter_login.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    // Do something with result, which provides a TwitterSession for making API calls
                    TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                    TwitterAuthToken authToken = session.getAuthToken();
                    String token = authToken.token;
                    String secret = authToken.secret;
                    //Log.d(TAG, "authToken" + authToken);
                    //Log.d(TAG, "token" + token);
                    //Log.d(TAG, "secret" + secret);

//                loginMethod(session);
                }

                @Override
                public void failure(TwitterException exception) {
                    // Do something on failure
                    Toast.makeText(mContext, "Login fail", Toast.LENGTH_LONG).show();
                }
            });
        }*/
        if (getArguments() != null && getArguments().containsKey("isFrom")) {
            String isFrom = getArguments().getString("isFrom");
            if(!isFrom.isEmpty()) {
                setNewUserTab();
            }
        }
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    public void handleGmailSignInResult(Task<GoogleSignInAccount> completedTask, Activity activity) {
        mActivity = (AppCompatActivity) activity;
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            fetchUserGmailData(account);
            // Signed in successfully, show authenticated UI.

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.d(TAG, "signInResult:failed code=" + e.getStatusCode());
            fetchUserGmailData(null);
        }
    }
  /*  public void  getFacebookProfile(LoginResult loginResult,Activity mactivity){
        mActivity=(AppCompatActivity)mactivity;
//        Log.d("TAG","getFacebookProfile"+accessToken);
        String accesstoken=loginResult.getAccessToken().getToken();
        String tokenExpiry=(String.valueOf( loginResult.getAccessToken().getExpires()));
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

//        loginButton.setReadPermissions(Arrays.asList("email","public_profile", "user_birthday"));
        if(accessToken!=null) {
            //Log.d(TAG, "getFacebookProfile" + accessToken.getToken());
            //Log.d(TAG, "getFacebookProfile" + accessToken.getPermissions());
        }

        //Log.d(TAG,"tokenExpiry"+tokenExpiry);
        //Log.d(TAG,"accesstoken"+accesstoken);
        Map<String,String> map = new HashMap<>();
        if(accessToken!=null)
            map.put("authToken",accesstoken);
        if(tokenExpiry!=null)
            map.put("tokenExpiry",tokenExpiry);
        showProgressBar();
        SocialRequestForFb(map);
    }*/


    private void fetchUserGmailData(GoogleSignInAccount account) {
        if (account != null) {
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            String id = account.getId();
            String tokenId = account.getIdToken();
            String serverAuthCode = account.getServerAuthCode();
            //Log.d(TAG, "email :- " + email);
            //Log.d(TAG, "displayName :-" + displayName);
            //Log.d(TAG, "tokenId :-" + tokenId);
            //Log.d(TAG, "serverAuthCode :-" + serverAuthCode);
            //Log.d(TAG, "id :-" + id);


            Map<String, String> map = new HashMap<>();
            if (email != null) {
                map.put("email", email);
                //Log.d(TAG, "UserId" + email);
            } else {
                map.put("email", "");
            }
            if (id != null) {
                map.put("googleId", id);
            } else {
                map.put("googleId", "");
            }
            if (displayName != null) {
                map.put("userName", displayName);
            } else {
                map.put("userName", "");
            }

            if (tokenId != null) {
                map.put("authToken", serverAuthCode);
            } else {
                map.put("authToken", "");
            }

            if (tokenId != null) {
                map.put("idToken", tokenId);
            } else {
                map.put("idToken", "");
            }
            socialSignRequest(map);
        }
    }

    private void SocialRequestForFb(final Map<String, String> eMailParams) {
        FBLogin fbLogin = new FBLogin(new APICallback<SocialLoginData>() {
            @Override
            public void onResponse(APIResponse<SocialLoginData> response) {
                Log.d("TAG", "response" + response);
                if (response != null && response.body() != null) {
                    if (response.body().code != 200 && response.body().code != 201) {
                        userEmail = response.body().email;
                        phnNum = null;
                        userId = response.body().userid;
                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.REASON_FAILURE, response.body().message);
                        params.put(Analytics.SIGN_UP_OPTION, Analytics.ACCOUNT_CLIENT);
                        params.put(Analytics.ERROR_CODE, String.valueOf(response.body().code));
                        Analytics.trackEvent(Analytics.EventPriority.MEDIUM, Analytics.EVENT_SIGN_UP_FAILED, params);
                        LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        showDeviceAuthenticationFailedWithOutExit(response.body().message);
                        CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
                        mixpanelOTPLoginFailed(response.body().message, String.valueOf(response.body().code));

                        return;
                    }
                    if (response.body().code == 200 || response.body().code == 201) {
                        myplexAPI.clearCache(APIConstants.BASE_URL);
                        PropertiesHandler.clearCategoryScreenFilter();
                        if (!TextUtils.isEmpty(mEmailID)) {
                            PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                        }
                        if (!TextUtils.isEmpty(mEmailID)) {
                            PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                        } else if (!TextUtils.isEmpty(response.body().email)) {
                            PrefUtils.getInstance().setPrefMsisdnNo(response.body().email);
                        }
                        if (!TextUtils.isEmpty(response.body().email)) {
                            PrefUtils.getInstance().setPrefEmailID(response.body().email);
                        }
                        PrefUtils.getInstance().setPrefLoginStatus("success");
//                        Toast.makeText(mContext, "Login Success", Toast.LENGTH_SHORT).show();

                        try {
                            LoggerD.debugOTP("Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                            PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                            Util.setUserIdInMyPlexEvents(mContext);
                           /* if(!TextUtils.isEmpty(response.body().serviceName)) {
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }*/
                            Analytics.mixpanelIdentify();
                            if (!TextUtils.isEmpty(response.body().email)) {
                                Analytics.setMixPanelEmail(response.body().email);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                        if (response.body().code == 201) {
                            AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                        } else if (response.body().code == 200) {
                            AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                        }
                        AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, eMailParams);
                        mixpanelOTPLoginSuccess();
//                        mActivity=(AppCompatActivity)getActivity();
                        mIsLoginDuringBrowse = true;
                        fetchOfferAvailability();
                    } else if (response.body().code == 401) {
                        dismissProgressBar();
                        numberOfLoginAttempts++;
                        if (response.body().message != null) {
                            Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                        }
                    } else if (response.body().code == 423) {
                        dismissProgressBar();
                        numberOfLoginAttempts++;
                        if (response.body().message != null)
                            Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                    } else if (response.body().code == 500) {
                        dismissProgressBar();
                        numberOfLoginAttempts++;
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                    } else if (response.body().code == 400) {
                        dismissProgressBar();
                        numberOfLoginAttempts++;
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_SHORT).show();
                    } else if (response.body().code == 403) {
//                        Util.deleteDownloadedMovies(mContext, null, false);
                        numberOfLoginAttempts++;
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_SHORT).show();
                        DeviceUnRegRequest deviceUnregister = new DeviceUnRegRequest(new APICallback<BaseResponseData>() {
                            @Override
                            public void onResponse(APIResponse<BaseResponseData> response) {

                                if (response != null && response.body() != null) {
                                    if (response.body().code == 200) {
                                        SocialSignInUserReq(eMailParams);
                                    } else {
                                        dismissProgressBar();
                                        HashMap<String, String> properties = new HashMap<>();
                                        properties.put("Email", userEmail);
                                        properties.put("User Id", userId);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable t, int errorCode) {
                                dismissProgressBar();
                                Map<String, Object> param = new HashMap<>();
//                                param.put(EVENT_ERROR_SOURCE_PARAM, Analytics.SOCIAL_SIGNIN);
//                                param.put(EVENT_ERROR_CODE_PARAM, errorCode + "");
//                                if (t != null)
//                                    param.put(EVENT_REASON_FOR_FAILURE_PARAM, convertToLowerCase(t.toString()));
//                                param.put(PROPERTY_CONTENT_ID, NULL_VALUE);
//                                param.put(PROPERTY_CONTENT_NAME, NULL_VALUE);
//                                param.put(PROPERTY_SERIES_NAME,NULL_VALUE);
//                                Analytics.trackEvent(Analytics.EventPriority.HIGH, EVENT_ERROR_ENCOUNTERED, param);

                                HashMap<String, String> properties = new HashMap<>();
                                properties.put("Email", userEmail);
                                properties.put("User Id", userId);

                              /*  if(t!= null && t.getMessage() != null) {
                                    Analytics.createEventLoggerAPI(t.getMessage(),errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
                                }else{
                                    Analytics.createEventLoggerAPI(NULL_VALUE,errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
                                }*/
                            }
                        });
                        APIService.getInstance().execute(deviceUnregister);
                    } else {
                        dismissProgressBar();
                        Toast.makeText(mContext, "Unable to Login", Toast.LENGTH_LONG).show();
                    }
                } else {
                    HashMap<String, String> properties = new HashMap<>();
                    properties.put("Email", userEmail);
                    properties.put("User Id", userId);

//                    Analytics.createEventLoggerAPI(RESPONSE_NULL ,0+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                dismissProgressBar();
                //Log.d(TAG, "Failure");
                //Log.d(TAG, "Failure" + errorCode);

                HashMap<String, String> properties = new HashMap<>();
                properties.put("Email", userEmail);
                properties.put("User Id", userId);

//                if(t!= null && t.getMessage() != null) {
//                    Analytics.createEventLoggerAPI(t.getMessage(),errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
//                }else{
//                    Analytics.createEventLoggerAPI(NULL_VALUE,errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
//                }
            }
        }, eMailParams);
        APIService.getInstance().execute(fbLogin);

    }

    private void socialSignRequest(final Map<String, String> eMailParams) {
        userEmail = eMailParams.get("email");
        userId = eMailParams.get("googleId");
        //Log.d(TAG, "mActivity:314" + mActivity);

        GoogleLogin googleLogin = new GoogleLogin(new APICallback<SocialLoginData>() {
            @Override
            public void onResponse(APIResponse<SocialLoginData> response) {
                if (response != null && response.body() != null) {
                    if (response.body().code != 200 && response.body().code != 201) {
                        userEmail = response.body().email;
                        phnNum = null;
                        userId = response.body().userid;
                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.REASON_FAILURE, response.body().message);
                        params.put(Analytics.SIGN_UP_OPTION, Analytics.ACCOUNT_CLIENT);
                        params.put(Analytics.ERROR_CODE, String.valueOf(response.body().code));
                        Analytics.trackEvent(Analytics.EventPriority.MEDIUM, Analytics.EVENT_SIGN_UP_FAILED, params);
                        LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        showDeviceAuthenticationFailedWithOutExit(response.body().message);
                        CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
                        mixpanelOTPLoginFailed(response.body().message, String.valueOf(response.body().code));

                        return;
                    }
                    if (response.body().code == 200 || response.body().code == 201) {
                        myplexAPI.clearCache(APIConstants.BASE_URL);
                        PropertiesHandler.clearCategoryScreenFilter();
                        if (!TextUtils.isEmpty(mEmailID)) {
                            PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                        }
                        if (!TextUtils.isEmpty(mEmailID)) {
                            PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                        } else if (!TextUtils.isEmpty(response.body().email)) {
                            PrefUtils.getInstance().setPrefMsisdnNo(response.body().email);
                        }
                        if (!TextUtils.isEmpty(response.body().email)) {
                            PrefUtils.getInstance().setPrefEmailID(response.body().email);
                        }
                        PrefUtils.getInstance().setPrefLoginStatus("success");

                        try {
                            LoggerD.debugOTP("Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                            PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                            Util.setUserIdInMyPlexEvents(mContext);
                           /* if(!TextUtils.isEmpty(response.body().serviceName)) {
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }*/
                            Analytics.mixpanelIdentify();
                            if (!TextUtils.isEmpty(response.body().email)) {
                                Analytics.setMixPanelEmail(response.body().email);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                        if (response.body().code == 201) {
                            AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                        } else if (response.body().code == 200) {
                            AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                        }
                        AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, eMailParams);
                        mixpanelOTPLoginSuccess();
//                        mActivity=(AppCompatActivity)getActivity();
                        mIsLoginDuringBrowse = true;
                        fetchOfferAvailability();
                    } else if (response.body().code == 401) {
                        dismissProgressBar();
                        numberOfLoginAttempts++;
                        if (response.body().message != null) {
                            Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                        }
                    } else if (response.body().code == 423) {
                        dismissProgressBar();
                        numberOfLoginAttempts++;
                        if (response.body().message != null)
                            Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                    } else if (response.body().code == 500) {
                        dismissProgressBar();
                        numberOfLoginAttempts++;
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                    } else if (response.body().code == 400) {
                        dismissProgressBar();
                        numberOfLoginAttempts++;
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_SHORT).show();
                    } else if (response.body().code == 403) {
//                        Util.deleteDownloadedMovies(mContext, null, false);
                        numberOfLoginAttempts++;
                        DeviceUnRegRequest deviceUnregister = new DeviceUnRegRequest(new APICallback<BaseResponseData>() {
                            @Override
                            public void onResponse(APIResponse<BaseResponseData> response) {

                                if (response != null && response.body() != null) {
                                    if (response.body().code == 200) {
                                        SocialSignInUserReq(eMailParams);
                                    } else {
                                        dismissProgressBar();
                                        HashMap<String, String> properties = new HashMap<>();
                                        properties.put("Email", userEmail);
                                        properties.put("User Id", userId);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable t, int errorCode) {
                                dismissProgressBar();

                                Map<String, Object> param = new HashMap<>();
//                                param.put(EVENT_ERROR_SOURCE_PARAM, Analytics.SOCIAL_SIGNIN);
//                                param.put(EVENT_ERROR_CODE_PARAM, errorCode + "");
//                                if (t != null)
//                                    param.put(EVENT_REASON_FOR_FAILURE_PARAM, convertToLowerCase(t.toString()));
//                                param.put(PROPERTY_CONTENT_ID, NULL_VALUE);
//                                param.put(PROPERTY_CONTENT_NAME, NULL_VALUE);
//                                param.put(PROPERTY_SERIES_NAME,NULL_VALUE);
//                                Analytics.trackEvent(Analytics.EventPriority.HIGH, EVENT_ERROR_ENCOUNTERED, param);

                                HashMap<String, String> properties = new HashMap<>();
                                properties.put("Email", userEmail);
                                properties.put("User Id", userId);

                              /*  if(t!= null && t.getMessage() != null) {
                                    Analytics.createEventLoggerAPI(t.getMessage(),errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
                                }else{
                                    Analytics.createEventLoggerAPI(NULL_VALUE,errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
                                }*/
                            }
                        });
                        APIService.getInstance().execute(deviceUnregister);
                    } else {
                        dismissProgressBar();
                        Toast.makeText(mContext, "Unable to Login", Toast.LENGTH_LONG).show();
                    }
                } else {
                    HashMap<String, String> properties = new HashMap<>();
                    properties.put("Email", userEmail);
                    properties.put("User Id", userId);

//                    Analytics.createEventLoggerAPI(RESPONSE_NULL ,0+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                dismissProgressBar();


                HashMap<String, String> properties = new HashMap<>();
                properties.put("Email", userEmail);
                properties.put("User Id", userId);

//                if(t!= null && t.getMessage() != null) {
//                    Analytics.createEventLoggerAPI(t.getMessage(),errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
//                }else{
//                    Analytics.createEventLoggerAPI(NULL_VALUE,errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
//                }
            }
        }, eMailParams);
        APIService.getInstance().execute(googleLogin);
    }

    private void SocialSignInUserReq(final Map<String, String> bodyParams) {
        if (!ConnectivityUtil.isConnected(mContext)) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_network_not_available), Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressBar();
//        Crashlytics.setUserEmail(bodyParams.get("email"));
//        String userIdSha1 = Util.sha1Hash(bodyParams.get("email"));
//        Crashlytics.setUserName(userIdSha1);
//        Crashlytics.setUserIdentifier(userIdSha1);
        socialSignRequest(bodyParams);
    }

    private void initComponent() {
        mMobileNoEditText = (EditText) rootView.findViewById(R.id.phnSignIn);
        mEmailEditText = (TextInputEditText) rootView.findViewById(R.id.mobile_number);
        mPwdEditText = (TextInputEditText) rootView.findViewById(R.id.pwdSignIn);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);
        mFrgtPassword = rootView.findViewById(R.id.textForgotPassword);

        signUpButton = (Button) rootView.findViewById(R.id.signUpButtonSignIN);
        emailButton = (Button) rootView.findViewById(R.id.emailIdButtonSignIN);
        mobileValid = (TextView) rootView.findViewById(R.id.mobile_valid);
        passwordValid = (TextView) rootView.findViewById(R.id.password_valid);
        newUser = rootView.findViewById(R.id.new_user);
        sign_in_ll = rootView.findViewById(R.id.sign_in_ll);
        new_user_ll = rootView.findViewById(R.id.new_user_ll);

        signin_ll = rootView.findViewById(R.id.signin_ll);
        new_user_layout = rootView.findViewById(R.id.new_user_layout);
        signinLine = rootView.findViewById(R.id.signin_line);
        newuserLine = rootView.findViewById(R.id.newuser_line);
        signinTv = rootView.findViewById(R.id.signin_tv);
        newUserTv = rootView.findViewById(R.id.new_user_tv);
        ll_space_root = rootView.findViewById(R.id.ll_space_root);
        updateHorizontalSpacing();

        if(!DeviceUtils.isTablet(mContext)) {
            newUserTv.setTextSize(12);
            signinTv.setTextSize(14);
        }else{
            signinTv.setTextSize(18);
            newUserTv.setTextSize(22);
        }
            newuserLine.setVisibility(View.GONE);
            newUserTv.setTypeface(amazonEmberRegular);

        signinLine.setVisibility(View.VISIBLE);
        signinTv.setTypeface(amazonEmberBold);
        mPwdEditText.setTransformationMethod(new FragmentSignIn.AsteriskPasswordTransformationMethods());

        signin_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSigninTab();
            }
        });
        new_user_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewUserTab();
               /* if(!DeviceUtils.isTablet(mContext)) {
                    signinTv.setTextSize(12);
                    newUserTv.setTextSize(14);
                }else{
                    signinTv.setTextSize(18);
                    newUserTv.setTextSize(22);
                }
                signinLine.setVisibility(View.GONE);
                signinTv.setTypeface(amazonEmberRegular);
                hideSoftInputKeyBoard(v);
                newuserLine.setVisibility(View.VISIBLE);
                newUserTv.setTypeface(amazonEmberBold);
                sign_in_ll.setVisibility(View.GONE);
                new_user_ll.setVisibility(View.VISIBLE);*/
            }
        });
        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewUserFragment();

            }
        });
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addTab(tabLayout.newTab().setText(robotoStrinBoldgFont("SIGN IN")));
        tabLayout.addTab(tabLayout.newTab().setText(robotoStringFont("NEW USER")));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tabLayout.getSelectedTabPosition();
                tab.setText(robotoStrinBoldgFont(tab.getText().toString()));
                if (position == 0) {
                    sign_in_ll.setVisibility(View.VISIBLE);
                    new_user_ll.setVisibility(View.GONE);
                } else {
                    tabLayout.setPadding(0, 0, 0, 0);
                    sign_in_ll.setVisibility(View.GONE);
                    new_user_ll.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tabLayout.setPadding(0, 0, 0, 0);
                tab.setText(robotoStringFont(tab.getText().toString()));

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        emailButton.setTypeface(amazonEmberBold);

        mEmailEditText.requestFocus();
        phnButton = (Button) rootView.findViewById(R.id.phnButtonSignIN);

        mEmailEditText.addTextChangedListener(textWatcher);
        mPwdEditText.addTextChangedListener(textWatcher);


        /*mPwdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordValid.setVisibility(View.GONE);
                if (mPwdEditText.getText().toString().isEmpty()) {
                    emailButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                } else {
                    emailButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
*/
       /* mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mobileValid.setVisibility(View.GONE);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mobileValid.setVisibility(View.GONE);
                if (mEmailEditText.getText().toString().isEmpty()) {
                    emailButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                } else {
                    emailButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mobileValid.setVisibility(View.GONE);
            }
        });
*/
        phnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOTPragment();
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEmailEditText.getText().toString().isEmpty()){
                    mobileValid.setText(R.string.otp_msg_invalid_mobile_nos);
                    mobileValid.setVisibility(View.VISIBLE);
                    return;
                }
                if (!isValidEmailID(mEmailEditText.getText().toString())) {
                    mobileValid.setText(R.string.otp_msg_invalid_mobile_no);
                    mobileValid.setVisibility(View.VISIBLE);
                    return;
                }
                if(mPwdEditText.getText().toString().isEmpty()){
                    passwordValid.setText(R.string.passwords);
                    passwordValid.setVisibility(View.VISIBLE);
                    return;
                }
                if (!isValidPassword(mPwdEditText.getText().toString())) {
                    passwordValid.setText(R.string.password);
                    passwordValid.setVisibility(View.VISIBLE);
                    return;
                }
                emailSignInRequest();
            }
        });
        msundirectRegularFontTypeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/amazon_ember_cd_regular.ttf");
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignUpScreenFragment();
            }
        });

        mFrgtPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetPasswordFragment();
            }
        });

        ImageButton signInButton = rootView.findViewById(R.id.sign_in_button1);
        signInButton.setBackgroundResource(R.drawable.google);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button1:
                        signIn();
                        break;
                }
            }
        });

        social_fb_login = rootView.findViewById(R.id.facebook_icon_sign_up_social);
       /* loginButton = (LoginButton)rootView.findViewById(R.id.login_button);
//        loginButton.setReadPermissions(Arrays.asList("basic_info", "email", "user_likes", "user_status"));
        loginButton.setReadPermissions(Arrays.asList("email","public_profile"));
        loginButton.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);*/
//        loginButton.setLoginBehavior(LoginBehavior.NATIVE_ONLY);
//        loginButton.setLoginBehavior(LoginBehavior.WEB_ONLY);
        // If you are using in a fragment, call loginButton.setFragment(this);
        social_fb_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbSignin();

            }
        });
        signUpButton.setTypeface(amazonEmberBold);
        getNewConnection = rootView.findViewById(R.id.get_new_connection);
        subscribeToApps = rootView.findViewById(R.id.subscribe_to_apps);
        exploreOffers = rootView.findViewById(R.id.explore_offerings);
        exploreOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tenantId =APIConstants.TENANT_ID;
                String clientKey = PrefUtils.getInstance().getPrefClientkey();
                String url = PrefUtils.getInstance().getExploreOffers();
                if (url.contains("tenantId") && url.contains("clientKey")) {
                    url = url.replace("{tenantId}",tenantId);
                    url = url.replace("{clientKey}", clientKey);
                }
                if (url != null && !url.isEmpty()) {
                    startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), url, SubscriptionWebActivity.PARAM_LAUNCH_NONE, false), SUBSCRIPTION_REQUEST);

                }
            }
        });
        getNewConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewConnectionFragment("new_user_connection");

            }
        });
        subscribeToApps.setVisibility(View.GONE);
        //Showing the subscribe to apps button based on the property enabled or not
        if(PrefUtils.getInstance().getIsEnableSubScribeToApps()!=null && !TextUtils.isEmpty(PrefUtils.getInstance().getIsEnableSubScribeToApps()) && PrefUtils.getInstance().getIsEnableSubScribeToApps().equalsIgnoreCase("true")) {
            subscribeToApps.setVisibility(View.VISIBLE);
            subscribeToApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                getNewConnectionFragment("subscribe_apps");
                    String tenantId = APIConstants.TENANT_ID;
                    String clientKey = PrefUtils.getInstance().getPrefClientkey();
                    String url = PrefUtils.getInstance().getSubscribeToApps();
                    if (url.contains("tenantId") && url.contains("clientKey")) {
                        url = url.replace("{tenantId}", tenantId);
                        url = url.replace("{clientKey}", clientKey);
                    }
                    if (url != null && !url.isEmpty()) {
                        startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), url, SubscriptionWebActivity.PARAM_LAUNCH_NONE, false), SUBSCRIPTION_REQUEST);

                    }
                }

            });
        }

    }
    private void getNewConnectionFragment(String from) {
            if (mBaseActivity != null) {
                mBaseActivity.removeFragment(this);
                Bundle args = new Bundle();
                args.putString("isFrom", from);
                args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                mBaseActivity.pushFragment(FragmentGetNewConnection.newInstance(args));
            }
    }

    public void  setSigninTab(){
        if(!DeviceUtils.isTablet(mContext)) {
            newUserTv.setTextSize(12);
            signinTv.setTextSize(14);
        }else{
            newUserTv.setTextSize(18);
            signinTv.setTextSize(22);
        }
        newuserLine.setVisibility(View.GONE);
        newUserTv.setTypeface(amazonEmberRegular);
        signinLine.setVisibility(View.VISIBLE);
        signinTv.setTypeface(amazonEmberBold);
        sign_in_ll.setVisibility(View.VISIBLE);
        new_user_ll.setVisibility(View.GONE);
    }
    public void setNewUserTab(){
        if(!DeviceUtils.isTablet(mContext)) {
            signinTv.setTextSize(12);
            newUserTv.setTextSize(14);
        }else{
            newUserTv.setTextSize(18);
            signinTv.setTextSize(22);
        }
        signinLine.setVisibility(View.GONE);
        signinTv.setTypeface(amazonEmberRegular);
        newuserLine.setVisibility(View.VISIBLE);
        newUserTv.setTypeface(amazonEmberBold);
        sign_in_ll.setVisibility(View.GONE);
        new_user_ll.setVisibility(View.VISIBLE);
    }
    public Spannable robotoStrinBoldgFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_bold.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_regular.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }


    private void showNewUserFragment() {
        if (mBaseActivity != null) {
            Bundle args = new Bundle();

            args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentNewUser.newInstance(args));
        }
    }

    private void showResetPasswordFragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentResetPassword.newInstance(args));
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        if (getActivity() != null)
            getActivity().startActivityForResult(signInIntent, GMAIL_SIGN_IN);
    }

    private void fbSignin() {
        //loginButton.performClick();
    }

    private void readBundleValues(Bundle arguments) {
        if (arguments == null) {
            return;
        }
        if (arguments.containsKey(PARAM_MSISDN)) {
            mMobileNo = arguments.getString(PARAM_MSISDN);
            LoggerD.debugOTP("readBundleValues: mMobileNo- " + mMobileNo);
        }
        if (arguments.containsKey(PARAM_IS_EXISTING_USER)) {
            mIsExistingUser = arguments.getBoolean(PARAM_IS_EXISTING_USER);
            LoggerD.debugOTP("readBundleValues: mIsExistingUser- " + mIsExistingUser);
        }
        if (arguments.containsKey(PARAM_LOGIN_DURING_BROWSE)) {
            mIsLoginDuringBrowse = arguments.getBoolean(PARAM_LOGIN_DURING_BROWSE);
            LoggerD.debugOTP("readBundleValues: mIsExistingUser- " + mIsExistingUser);
        }
        if (arguments.containsKey("isFromSplash")) {
            isFromSplash = arguments.getBoolean("isFromSplash");
            LoggerD.debugOTP("readBundleValues: mIsExistingUser- " + mIsExistingUser);
        }
    }

    TextWatcher textWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (mEmailEditText.isFocused()) {
                mobileValid.setVisibility(View.GONE);
            }
            if (mPwdEditText.isFocused()) {
                passwordValid.setVisibility(View.GONE);
            }
            if (!mEmailEditText.getText().toString().isEmpty() && !mPwdEditText.getText().toString().isEmpty()) {
                if(mEmailEditText.getText().toString().length()==10 && mPwdEditText.getText().toString().length()>=6){
                    emailButton.setBackgroundResource(R.drawable.rounded_corner_button_white);

                }else{
                    emailButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            } /*else if (mEmailEditText.getText().toString().isEmpty() && !mPwdEditText.getText().toString().isEmpty()) {
                emailButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
            } else if (!mEmailEditText.getText().toString().isEmpty() && mPwdEditText.getText().toString().isEmpty()) {
                emailButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
            }*/ else {
                emailButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    @Override
    public void onPause() {
        super.onPause();
        if (mEmailEditText != null) {
            hideSoftInputKeyBoard(mEmailEditText);
        }
        if (mMobileNoEditText != null) {
            hideSoftInputKeyBoard(mMobileNoEditText);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
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


    private void showDeviceAuthenticationFailedWithOutExit(String message) {
        if (message == null) {
            message = mContext.getResources().getString(R.string.dev_auth_failed_message);
        }
        if (getActivity() != null) {
            AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", getActivity().getResources()
                            .getString(R.string.feedbackokbutton),
                    new AlertDialogUtil.NeutralDialogListener() {
                        @Override
                        public void onDialogClick(String buttonText) {
                        }
                    });
        }
    }


    private void launchNonsundirectUserScreen() {
        Bundle args = new Bundle();

        OfferFragment
                offerFragment = OfferFragment.newInstance(args);
        if (mBaseActivity != null && isAdded()) {
            mBaseActivity.removeFragment(this);
        }
        mBaseActivity.pushFragment(offerFragment);
    }

    private void launchMainActivity() {
        if(!SubscriptionWebActivity.isFromSignIn) return;
        Activity activity = mActivity;
        if (activity == null || activity.isFinishing()) return;
        /*if (mIsLoginDuringBrowse && !isFromSplash) {
            activity.setResult(MainActivity.INTENT_RESPONSE_TYPE_SUCCESS);
            if (isSubscriptionFailed) {
                activity.setResult(MainActivity.INTENT_RESPONSE_TYPE_SUCCESS_SUBSCRIPTION_FAILED);
            }
            activity.finish();
            return;
        }*/
        Bundle bundle = new Bundle();
        bundle.putString(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, notificationId);
        if(launchURL != null)
            bundle.putString(APIConstants.NOTIFICATION_LAUNCH_URL, launchURL);
        MainActivityLauncherUtil.setNotificationViewAllData(bundle);
        MainActivityLauncherUtil.initStartUpCalls(activity);
    }

    private void fetchOfferAvailability() {
        showProgressBar();

        OfferedPacksRequest.Params params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_5, APIConstants.PARAM_APPLAUNCH, null);

        final OfferedPacksRequest contentDetails = new OfferedPacksRequest(params, new APICallback<OfferResponseData>() {
            @Override
            public void onResponse(APIResponse<OfferResponseData> response) {

                dismissProgressBar();
                if (response == null || response.body() == null) {
                    showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
                    Analytics.mixpanelEventUnableToFetchOffers(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIConstants.NOT_AVAILABLE);
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    showDeviceAuthenticationFailed(response.body().message);
                    Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
                    return;
                }
                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && response.body().code == 216) {
                    launchNonsundirectUserScreen();
                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")) {
                    if (response.body().code == 219
                            || response.body().code == 220
                            || (!myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW)) {
                        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                        launchMainActivity();
                        return;
                    }
//                    response.body().ui.action = APIConstants.APP_LAUNCH_WEB;
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
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showToastNotification(response.body().message);
                                }
                                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                                launchMainActivity();
                                break;
                            case APIConstants.APP_LAUNCH_HOME:
                                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                                launchMainActivity();
                                break;
                            case APIConstants.APP_LAUNCH_WEB:
//                                response.body().ui.redirect = "http://169.38.74.50/custom/vfplay/v1/webSubscriptions/?clientKey=" + PrefUtils.getInstance().getPrefClientkey() + "&offerId=No+Offer";
                                if (!TextUtils.isEmpty(response.body().ui.redirect)) {
                                    int launchType = SubscriptionWebActivity.PARAM_LAUNCH_HOME;
                                    if (mIsLoginDuringBrowse) {
                                        launchType = SubscriptionWebActivity.PARAM_LAUNCH_NONE;
                                    }
                                    startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), response.body().ui.redirect, launchType), SUBSCRIPTION_REQUEST);
                                    if (!mIsLoginDuringBrowse) {
                                        getActivity().finish();
                                    }
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
                    if (response.body().code == 219
                            || response.body().code == 220
                            || response.body().results == null
                            || response.body().results.isEmpty()
                            || !myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW) {
                        PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                        launchMainActivity();
                        return;
                    }
                    boolean isOfferAvailable = false;
                    for (CardDataPackages offerPack : response.body().results) {
                        if (offerPack.subscribed) {
                            PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                            launchMainActivity();
                            return;
                        }
                        if (APIConstants.TYPE_OFFER.equalsIgnoreCase(offerPack.packageType)) {
//                            mBaseActivity.pushFragment(OfferFragment.newInstance(null));
                            isOfferAvailable = true;
                            break;
                        }
                    }
                    if (isOfferAvailable) {
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
                LoggerD.debugOTP("Failed: " + t);
                dismissProgressBar();
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


    private void makeUserLoginRequest() {

        showProgressBar();

        LoggerD.debugOTP("emailId- " + mEmailID);
        // As per first cut mobile and otp requirement  hard coding mail id and password
        MSISDNLoginEncryptedShreyas.Params msisdnParams = new MSISDNLoginEncryptedShreyas.Params(mEmailID, mPassword);

        MSISDNLoginEncryptedShreyas login = new MSISDNLoginEncryptedShreyas(msisdnParams,
                new APICallback<UserSigninResponse>() {
                    @Override
                    public void onResponse(APIResponse<UserSigninResponse> response) {
                        LoggerD.debugOTP("response-" + response);
                        if (!isAdded()) {
                            return;
                        }
                        dismissProgressBar();
                        if (response == null
                                || response.body() == null) {
                            // login failed
                            LoggerD.debugOTP("success: msisdn login: " + "failed");
                            showDeviceAuthenticationFailedWithOutExit(null);
                            return;
                        }
                        LoggerD.debugOTP("success: msisdn login status : " + response.body().getStatus() + " code : " + response.body().getCode()
                                + " message : " + response.body().getMessage());
                        Map<String, String> params = new HashMap<>();
                        //params.put(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_sundirect);

                        if (!TextUtils.isEmpty(response.body().getUserid())) {
                            params.put(Analytics.USER_ID, response.body().getUserid());
                        }

                        if (!APIConstants.SUCCESS.equalsIgnoreCase(response.body().getStatus())) {
                            // login failed
                            params.put(Analytics.REASON_FAILURE, response.body().getMessage());
                            params.put(Analytics.SIGN_UP_OPTION, Analytics.ACCOUNT_CLIENT);
                            params.put(Analytics.ERROR_CODE, String.valueOf(response.body().getCode()));
                            Analytics.trackEvent(Analytics.EventPriority.MEDIUM, Analytics.EVENT_SIGN_UP_FAILED, params);
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().getMessage());
                            showDeviceAuthenticationFailedWithOutExit(response.body().getMessage());
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().getMessage());
                            mixpanelOTPLoginFailed(response.body().getStatus(), String.valueOf(response.body().getCode()));
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().getStatus())
                                && response.body().getCode() == 216) {
                            // login failed
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().getMessage());

                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().getMessage());
                        }

                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().getStatus())
                                && response.body().getCode() == 217) {
                            isOtpLogin = true;
                            mixpanelOTPLoginInitiated();
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            return;
                        }

                        if (response.body().getStatus().equalsIgnoreCase("SUCCESS")
                                && (response.body().getCode() == 200
                                || response.body().getCode() == 201)) {
                        /*    if (!TextUtils.isEmpty(response.body().serviceName)) {
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }*/
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            myplexAPI.clearCache(APIConstants.BASE_URL);
                            PropertiesHandler.clearCategoryScreenFilter();
                            if (!TextUtils.isEmpty(mEmailID)) {
                                PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                            }
                            if (!TextUtils.isEmpty(mEmailID)) {
                                PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                            }
                           // CleverTap.eventRegistrationCompleted(response.body().email, response.body().mobile, Analytics.NO);
                            if (!TextUtils.isEmpty(response.body().getMobile_no())) {
                                PrefUtils.getInstance().setPrefMsisdnNo(response.body().getMobile_no());
                            } /*else if (!TextUtils.isEmpty(response.body().email)) {
                                PrefUtils.getInstance().setPrefMsisdnNo(response.body().email);
                            }
                            if (!TextUtils.isEmpty(response.body().email)) {
                                PrefUtils.getInstance().setPrefEmailID(response.body().email);
                            }*/
                            PrefUtils.getInstance().setPrefLoginStatus("success");

                            if (ApplicationController.FLAG_ENABLE_OFFER_SUBSCRIPTION) {

                                if (response.body().getMobile_no() != null && !response.body().getMobile_no().isEmpty()) {
                                    PrefUtils.getInstance().setPrefMsisdnNo(response.body().getMobile_no());
                                 /*   if (response.body().email != null)
                                        PrefUtils.getInstance().setPrefEmailID(response.body().email);*/
                                    MsisdnData msisdnData = new MsisdnData();
                                    msisdnData.operator = SubcriptionEngine.OPERATORS_NAME;
                                    msisdnData.msisdn = response.body().getMobile_no();

                                    if (APIConstants.msisdnPath == null) {
                                        APIConstants.msisdnPath = mContext.getFilesDir() + "/" + "msisdn.bin";
                                    }
                                    PrefUtils.getInstance().setPrefMsisdnNo(response.body().getMobile_no());
                                  /*  if (response.body().email != null)
                                        PrefUtils.getInstance().setPrefEmailID(response.body().email);*/
                                    SDKUtils.saveObject(msisdnData, APIConstants.msisdnPath);
                                }
                                LoggerD.debugOTP("Info: msisdn login: " + "success and launching offer");
                                try {
                                  /*  LoggerD.debugOTP("Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                                    PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));*/
                                    Util.setUserIdInMyPlexEvents(mContext);
                                   /* if (!TextUtils.isEmpty(response.body().serviceName)) {
                                        PrefUtils.getInstance().setServiceName(response.body().serviceName);
                                    }*/
                                    Analytics.mixpanelIdentify();
                                   /* if (!TextUtils.isEmpty(response.body().email)) {
                                        Analytics.setMixPanelEmail(response.body().email);
                                    }*/
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (response.body().getCode() == 201) {
                                    AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                                } else if (response.body().getCode() == 200) {
                                    AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                                }
                                AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_CLIENT);
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.MIXPANEL_PEOPLE_JOINING_DATE, Util.getCurrentDate());
                                params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                                Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                                mixpanelOTPLoginSuccess();
                                // TODO: 27/09/22
                               /* if(response.body().getUi() != null && response.body().getUi().getAction().equalsIgnoreCase("htmlOfferPage")) {
                                    if (!TextUtils.isEmpty(response.body().getUi().getRedirect()) && isAdded() && getActivity() != null) {
                                        if(notificationId != null)
                                             startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), response.body().getUi().getRedirect(), SubscriptionWebActivity.PARAM_LAUNCH_NONE, notificationId, true), SUBSCRIPTION_REQUEST);
                                        else
                                            startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), response.body().getUi().getRedirect(), SubscriptionWebActivity.PARAM_LAUNCH_NONE, true), SUBSCRIPTION_REQUEST);
                                        getActivity().finish();
                                        return;
                                    }
                                }
                                fetchOfferAvailability();
                                // mBaseActivity.pushFragment(new OfferedPacksFragment());
                                return;*/
                            }
                            try {
                                LoggerD.debugOTP("Info: msisdn login: " + "success" + " userid= " + response.body().getUserid());
                                PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().getUserid()));
                                Util.setUserIdInMyPlexEvents(mContext);
                           /*     if (!TextUtils.isEmpty(response.body().serviceName)) {
                                    PrefUtils.getInstance().setServiceName(response.body().serviceName);
                                }
                                Analytics.mixpanelIdentify();
                                if (!TextUtils.isEmpty(response.body().email)) {
                                    Analytics.setMixPanelEmail(response.body().email);
                                }*/
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                            if (response.body().getCode() == 201) {
                                AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                            } else if (response.body().getCode() == 200) {
                                AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                            }
                            AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                            Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                            mixpanelOTPLoginSuccess();
                            isFromSignIn = true;
                            launchMainActivity();
                        }

                        if (!TextUtils.isEmpty(response.body().getMessage())) {

                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().getMessage());
                           // AlertDialogUtil.showToastNotification(response.body().getMessage());
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        dismissProgressBar();
                        LoggerD.debugOTP("Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {

                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailedWithOutExit(null);
                    }
                });
        APIService.getInstance().execute(login);
    }

    private void updateProfileWithEmailID() {
        String emailId = mEmailID.trim().toLowerCase();
        showProgressBar();
        Analytics.mixpanelProfileEmailInitiated(emailId);
        ProfileUpdateWithEmailIDRequest.Params profileUpdateParams = new ProfileUpdateWithEmailIDRequest.Params(emailId);

        ProfileUpdateWithEmailIDRequest login = new ProfileUpdateWithEmailIDRequest(profileUpdateParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
                        if (!isAdded()) {
                            return;
                        }
                        dismissProgressBar();
                        if (response == null
                                || response.body() == null) {
                            // login failed
                            LoggerD.debugOTP("updateProfileWithEmailID: msisdn login: " + "failed");
                            showDeviceAuthenticationFailedWithOutExit(null);
                            return;
                        }
                        LoggerD.debugOTP("updateProfileWithEmailID: msisdn login status : " + response.body().status + "code :" + response.body().code
                                + "message :" + response.body().message);

                        if (!APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            // login failed
                            LoggerD.debugOTP("updateProfileWithEmailID: msisdn login: " + "failed" + response.body().message);
                            showDeviceAuthenticationFailedWithOutExit(response.body().message);
                            return;
                        }

                        if (response.body().status.equalsIgnoreCase("SUCCESS")
                                && (response.body().code == 200
                                || response.body().code == 201)) {
                            myplexAPI.clearCache(APIConstants.BASE_URL);
                            PropertiesHandler.clearCategoryScreenFilter();
                            if (!TextUtils.isEmpty(response.body().serviceName)) {
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }
                            LoggerD.debugOTP("updateProfileWithEmailID: response.body().email: " + response.body().email);
                            if (!TextUtils.isEmpty(response.body().email)) {
                                Analytics.setMixPanelEmail(response.body().email);
                                PrefUtils.getInstance().setPrefEmailID(response.body().email);
                                Analytics.mixpanelProfileEmailSuccess();
                            }
                            getActivity().setResult(MainActivity.INTENT_RESPONSE_TYPE_EMAIL_UPDATE_SUCCESS);
                            getActivity().finish();
                        }

                        if (!TextUtils.isEmpty(response.body().message)) {
                            AlertDialogUtil.showToastNotification(response.body().message);
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        dismissProgressBar();
                        LoggerD.debugOTP("updateProfileWithEmailID: onFailed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailedWithOutExit(null);
                    }
                });
        APIService.getInstance().execute(login);
    }

    private void mixpanelOTPLoginInitiated() {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.DEVICE_ID, PrefUtils.getInstance().getPrefDeviceid());
        Analytics.trackEvent(Analytics.EventPriority.LOW, Analytics.EVENT_OTP_LOGIN_INITIATED, params);
    }

    private void mixpanelOTPLoginSuccess() {
        if (!isOtpLogin) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.DEVICE_ID, PrefUtils.getInstance().getPrefDeviceid());
        int userid = PrefUtils.getInstance().getPrefUserId();
        params.put(Analytics.USER_ID, userid == 0 ? "NA" : userid + "");
        String otpDetection = "auto";
        if (isOtpRequestManualEnter) {
            otpDetection = "manual";
        }
        params.put(Analytics.PARAM_OTP_DETECTION, otpDetection);
        Analytics.mixpanelOTPLoginSuccess(params);

    }

    private void mixpanelOTPLoginFailed(String reason, String errorCode) {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.DEVICE_ID, PrefUtils.getInstance().getPrefDeviceid());
        params.put(Analytics.REASON_FAILURE, reason);
        params.put(Analytics.ERROR_CODE, errorCode);
        Analytics.mixpanelOTPLoginFailed(params);
    }


    private void emailSignInRequest() {
        // read mobile number and email ID's and validate for API request.
        LoggerD.debugOTP("showAndProceedStep1SignInRequest");

        mEmailID = mEmailEditText.getText().toString();
        mEmailID = mEmailID.trim();

        if (!isValidEmailID(mEmailID)) {
            //  mMobileNoEditText.clearFocus();
            // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            mobileValid.setVisibility(View.VISIBLE);
            passwordValid.setVisibility(View.GONE);
//            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
            //          CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }
        if (mEmailID.length() < 10) {
            //  mMobileNoEditText.clearFocus();
            mobileValid.setVisibility(View.VISIBLE);
            passwordValid.setVisibility(View.GONE);

            // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            //          CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }
        mPassword = mPwdEditText.getText().toString();
        mPassword = mPassword.trim();
        if (!isValidPassword(mPassword)) {
            //  mMobileNoEditText.clearFocus();
            // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            mobileValid.setVisibility(View.GONE);
            passwordValid.setVisibility(View.VISIBLE);
//            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
            //          CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }
        if (mPassword.isEmpty()) {
            //  mMobileNoEditText.clearFocus();
            mobileValid.setVisibility(View.GONE);
            passwordValid.setVisibility(View.VISIBLE);

            // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            //          CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }
        FirebaseAnalytics.getInstance().setEmailProperty(mEmailID);
        FirebaseAnalytics.getInstance().userSignInStarted();
        makeUserLoginRequest();

    }

    private boolean isValidEmailID(String emailId) {
        if (emailId == null || TextUtils.isEmpty(emailId)) {
            return false;
        }

        if (emailId.length() > 0) {
            if (emailId.length() < 10 && emailId.length() > 10) {
                return false;
            }
            if (emailId.length() == 10) {
                if(emailId.substring(0, 1).matches("[6-9]")) {
                    mobileValid.setVisibility(View.GONE);
                    mPwdEditText.requestFocus();
                    return true;
                }else{
                    mobileValid.setVisibility(View.VISIBLE);
                }
            }
        }
            /*int lengthFromDot = 0;
            if (emailId.indexOf(".") >= 0 && emailId.substring(emailId.indexOf(".")) != null) {
                lengthFromDot = emailId.substring(emailId.indexOf(".")).length();
                LoggerD.debugDownload("lengthFromDot- " + lengthFromDot + " emailId.substring(emailId.indexOf(\".\"))- " + emailId.substring(emailId.indexOf(".")));
            }
            if (emailId.contains("@") && emailId.contains(".") && !emailId.contains(" ") && lengthFromDot > 2) {
                return true;
            }
        }*/
        return false;
    }


    private boolean isValidPassword(String password) {
        if (password == null || TextUtils.isEmpty(password)) {
            return false;
        }
        String newPasswordPattern = "^(?=.*[a-z])(?=."
                + "*[A-Z])(?=.*\\d)"
                + "(?=.*[-+_!@#$%^&*., ?]).+$";
        if (password.length() <= 6 && password.length() >= 32) {
            passwordValid.setVisibility(View.VISIBLE);
            return false;
        } if (password.length() >= 6 && password.length() <= 32) {
            if (password.matches(newPasswordPattern)) {
                mobileValid.setVisibility(View.GONE);
            }
                // showMobNoWrongTickmark = true;
                return true;
            }
        return false;
        }

/*
        if (password.length() > 0) {

           */
/* if (password.length() < 6 && password.length() > 6) {
                return false;
            }
            if (password.length() == 6) {
                return true;
            }*//*

            return true;
        }
*/

    public void showProgressBar() {

        if (mContext == null) {
            return;
        }
        if (!Util.checkActivityPresent(mContext)) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressDialog.show(mContext, "", "", true, false, null);
        mProgressDialog.setContentView(R.layout.layout_progress_dialog);
        ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(com.myplex.sdk.R.id.imageView1);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
    }

    public void dismissProgressBar() {
        try {
            if (getActivity() != null
                    && getActivity().isFinishing()
                    || !isAdded()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public boolean onBackClicked() {
        getActivity().finish();
        return super.onBackClicked();
    }


    void hideSoftInputKeyBoard(View view) {
        if (view != null && mContext != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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

        /*if (requestCode == GMAIL_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (mActivity != null)
                handleGmailSignInResult(task,mActivity);
        }*/

        if (requestCode == SubscriptionWebActivity.SUBSCRIPTION_REQUEST) {
            launchMainActivity();
            isSubscriptionFailed = true;
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
                    if (!mIsLoginDuringBrowse) {
                        Util.launchActivity(getActivity(), MainActivity.createIntent(getActivity(), page));
                        return;
                    }
                    isSubscriptionFailed = false;
                    launchMainActivity();
                } else if (mContext.getString(R.string.skip_text).equalsIgnoreCase(page)) {
                    launchMainActivity();
                } else if (resultCode == APIConstants.SUBSCRIPTIONERROR) {
//                    getActivity().finish();
                    String message = mContext.getString(R.string.canot_connect_server);
                    if (!ConnectivityUtil.isConnected(mContext)) {
                        message = mContext.getString(R.string.network_error);
                    }
                    showDeviceAuthenticationFailed(message);
                    getActivity().finish();
                } else {
                    launchMainActivity();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressBar();
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissProgressBar();
    }

    private void makeSMSRetrieverAPI() {

        SmsRetrieverClient client = SmsRetriever.getClient(mContext /* context */);
        Task<Void> task = client.startSmsRetriever();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Successfully started retriever, expect broadcast intent
                // ...
                LoggerD.debugOTP("SMSRetriever-     " + "Sucess");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to start retriever, inspect Exception for more details
                // ...
                LoggerD.debugOTP("SMSRetriever-     " + "Failed");
            }
        });
    }

    private void showSignUpScreenFragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentSignUp.newInstance(args));
        }
    }

    private void showOTPragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(FragmentOTPVerification_shreyas.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentOTPVerification_shreyas.newInstance(args));
        }
    }
    public class AsteriskPasswordTransformationMethods extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new FragmentSignIn.AsteriskPasswordTransformationMethods.PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;
            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }
            public char charAt(int index) {
                return '*'; // This is the important part
            }
            public int length() {
                return mSource.length(); // Return default
            }
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    };

}
