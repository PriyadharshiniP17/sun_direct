package com.myplex.myplex.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/*import com.facebook.AccessToken;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;*/
import com.github.pedrovgs.LoggerD;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;
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

import java.util.HashMap;
import java.util.Map;

import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;


/**
 * Created by Srikanth on 10/21/2014.
 */
public class FragmentSignInWithEmail extends BaseFragment {

    public static final String PARAM_MSISDN = "msisdn";
    public static final String PARAM_IS_EXISTING_USER = "is_existing_user_to_disable_mobile_number";
    public static final String PARAM_LOGIN_DURING_BROWSE = "during_browse";
    public static final String PARAM_UPDATE_EMAIL = "update_email";
    private View rootView;
    public static final String TAG = FragmentSignInWithEmail.class.getName();


    private EditText mEmailEditText;
    private EditText mPwdEditText;
    private TextView mFrgtPassword;
    private Button signUpButton;
    private Button emailButton;
    private Context mContext;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int GMAIL_SIGN_IN = 111;
    private String userEmail,userId;
    private int numberOfLoginAttempts = 0;
    private Button social_fb_login;
    private String enteringEmailId;
    private String enteringPassword;


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
                mBaseActivity.removeFragment(FragmentSignInWithEmail.this);
                mBaseActivity.pushFragment(FragmentSignInWithEmail.newInstance(getArguments()));
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

    private boolean mIsExistingUser;
    private boolean isOtpLogin;
    private boolean mIsLoginDuringBrowse;
    private Typeface msundirectRegularFontTypeFace;
    private boolean isSubscriptionFailed = false;
    private String source;
    private String sourceDetails;
//    private LoginButton loginButton;
    TextView countryCodeTv;


    public static FragmentSignInWithEmail newInstance(Bundle args) {
        FragmentSignInWithEmail fragmentOTPVerification = new FragmentSignInWithEmail();
        fragmentOTPVerification.setArguments(args);
        return fragmentOTPVerification;
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        mFrameLayout.removeAllViews();
//        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        rootView = inflater.inflate(R.layout.fragment_otp, null);
//        initComponent();
//        mFrameLayout.addView(rootView);
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();

        mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rootView = inflater.inflate(R.layout.sign_in_with_email_shreyas, container, false);
        readBundleValues(getArguments());
        source = null;
        if (getArguments() != null && getArguments().containsKey(Analytics.PROPERTY_SOURCE)) {
            source = getArguments().getString(Analytics.PROPERTY_SOURCE);
        }

        sourceDetails = null;
        if (getArguments() != null && getArguments().containsKey(Analytics.PROPERTY_SOURCE_DETAILS)) {
            sourceDetails = getArguments().getString(Analytics.PROPERTY_SOURCE_DETAILS);
        }
        CleverTap.eventRegistrationPageViewed(source, sourceDetails);
//        Update the email when email is not available during login

        initComponent();


        String serverClientId = mContext.getResources().getString(R.string.server_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext,googleSignInOptions);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void handleGmailSignInResult(Task<GoogleSignInAccount> completedTask,Activity activity) {
        mActivity=(AppCompatActivity) activity;
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            fetchUserGmailData(account);
            // Signed in successfully, show authenticated UI.

        }
        catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.d(TAG, "signInResult:failed code=" + e.getStatusCode());
            fetchUserGmailData(null);
        }
    }
   /* public void  getFacebookProfile(LoginResult loginResult,Activity mactivity){
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
            //Log.d(TAG,"email :- "+email);
            //Log.d(TAG,"displayName :-"+displayName);
            //Log.d(TAG,"tokenId :-"+tokenId);
            //Log.d(TAG,"serverAuthCode :-"+serverAuthCode);
            //Log.d(TAG,"id :-"+id);



            Map<String,String> map = new HashMap<>();
            if (email != null) {
                map.put("email",email);
                //Log.d(TAG,"UserId"+email);
            } else {
                map.put("email","");
            }
            if (id != null) {
                map.put("googleId",id);
            } else {
                map.put("googleId","");
            }
            if (displayName != null) {
                map.put("userName",displayName);
            } else {
                map.put("userName","");
            }

            if(tokenId != null) {
                map.put("authToken",serverAuthCode);
            } else {
                map.put("authToken","");
            }

            if(tokenId != null) {
                map.put("idToken",tokenId);
            } else {
                map.put("idToken","");
            }
            socialSignRequest(map);
        }
    }

    private void SocialRequestForFb(final Map<String, String> eMailParams) {
        FBLogin fbLogin = new FBLogin(new APICallback<SocialLoginData>() {
            @Override
            public void onResponse(APIResponse<SocialLoginData> response) {
                Log.d("TAG","response"+response);
                if (response != null && response.body() != null) {
                    if (response.body().code != 200 && response.body().code != 201) {
                        userEmail = response.body().email;
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
                    if (response.body().code == 200 || response.body().code == 201 ) {
                        myplexAPI.clearCache(APIConstants.BASE_URL);
                        PropertiesHandler.clearCategoryScreenFilter();
                        if(!TextUtils.isEmpty(mEmailID)){
                            PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                        }
                        if(!TextUtils.isEmpty(mEmailID)){
                            PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                        }

                        else if(!TextUtils.isEmpty(response.body().email)){
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
                        if(response.body().code==201){
                            AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                        }else if(response.body().code == 200){
                            AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                        }
                        AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, eMailParams);
                        mixpanelOTPLoginSuccess();
//                        mActivity=(AppCompatActivity)getActivity();
                        mIsLoginDuringBrowse=true;
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
                                        properties.put("Email",userEmail);
                                        properties.put("User Id",userId);
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
                                properties.put("Email",userEmail);
                                properties.put("User Id",userId);

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
                    properties.put("Email",userEmail);
                    properties.put("User Id",userId);

//                    Analytics.createEventLoggerAPI(RESPONSE_NULL ,0+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                dismissProgressBar();
                //Log.d(TAG, "Failure");
                //Log.d(TAG, "Failure" + errorCode);

                HashMap<String, String> properties = new HashMap<>();
                properties.put("Email",userEmail);
                properties.put("User Id",userId);

//                if(t!= null && t.getMessage() != null) {
//                    Analytics.createEventLoggerAPI(t.getMessage(),errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
//                }else{
//                    Analytics.createEventLoggerAPI(NULL_VALUE,errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
//                }
            }
        },eMailParams);
        APIService.getInstance().execute(fbLogin);

    }

    private void socialSignRequest(final Map<String, String> eMailParams) {
        userEmail = eMailParams.get("email");
        userId = eMailParams.get("googleId");
        //Log.d(TAG,"mActivity:314"+mActivity);

        GoogleLogin googleLogin = new GoogleLogin(new APICallback<SocialLoginData>() {
            @Override
            public void onResponse(APIResponse<SocialLoginData> response) {
                if (response != null && response.body() != null) {
                    if (response.body().code != 200 && response.body().code != 201) {
                        userEmail = response.body().email;
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
                    if (response.body().code == 200 || response.body().code == 201 ) {
                        myplexAPI.clearCache(APIConstants.BASE_URL);
                        PropertiesHandler.clearCategoryScreenFilter();
                        if(!TextUtils.isEmpty(mEmailID)){
                            PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                        }
                        if(!TextUtils.isEmpty(mEmailID)){
                            PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                        }

                        else if(!TextUtils.isEmpty(response.body().email)){
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
                        if(response.body().code==201){
                            AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                        }else if(response.body().code == 200){
                            AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                        }
                        AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, eMailParams);
                        mixpanelOTPLoginSuccess();
//                        mActivity=(AppCompatActivity)getActivity();
                        mIsLoginDuringBrowse=true;
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
                                        properties.put("Email",userEmail);
                                        properties.put("User Id",userId);
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
                                properties.put("Email",userEmail);
                                properties.put("User Id",userId);

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
                    properties.put("Email",userEmail);
                    properties.put("User Id",userId);

//                    Analytics.createEventLoggerAPI(RESPONSE_NULL ,0+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                dismissProgressBar();


                HashMap<String, String> properties = new HashMap<>();
                properties.put("Email",userEmail);
                properties.put("User Id",userId);

//                if(t!= null && t.getMessage() != null) {
//                    Analytics.createEventLoggerAPI(t.getMessage(),errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
//                }else{
//                    Analytics.createEventLoggerAPI(NULL_VALUE,errorCode+"",APIConstants.ERR_SOCIAL_SIGNIN,TAG,properties);
//                }
            }
        },eMailParams);
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
        mEmailEditText = (EditText) rootView.findViewById(R.id.emailIDSignIn);
        mPwdEditText = (EditText) rootView.findViewById(R.id.pwdSignIn);

        mFrgtPassword=rootView.findViewById(R.id.textForgotPassword);

        signUpButton = (Button) rootView.findViewById(R.id.signinwithphoneNumber);
        emailButton = (Button) rootView.findViewById(R.id.emailIdButtonSignIN);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        ImageButton signInButton=rootView.findViewById(R.id.sign_in_button1);
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

        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enteringEmailId=s.toString();
                checkAndUpdateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPwdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enteringPassword=s.toString();
                checkAndUpdateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        social_fb_login=rootView.findViewById(R.id.facebook_icon_sign_up_social);
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

    }

    private void checkAndUpdateButton(){
        if (isValidEmailID(enteringEmailId)&&enteringPassword!=null&&enteringPassword.length()>=6){
            emailButton.setBackground(getResources().getDrawable(R.drawable.edged_button_red));
            emailButton.setTextColor(getResources().getColor(R.color.submit_button_text_color));
        }else {
            emailButton.setBackground(getResources().getDrawable(R.drawable.edged_button_grey));
            emailButton.setTextColor(getResources().getColor(R.color.submit_button_text_color));
        }
    }

    private void showResetPasswordFragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(FragmentSignInWithEmail.PARAM_LOGIN_DURING_BROWSE,mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentResetPassword.newInstance(args));
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        if (getActivity() != null)
            getActivity().startActivityForResult(signInIntent, GMAIL_SIGN_IN);
    }

    private  void  fbSignin(){
     //   loginButton.performClick();
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
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mEmailEditText != null) {
            hideSoftInputKeyBoard(mEmailEditText);
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
        }if(getActivity()!=null) {
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
        Activity activity = mActivity;
        if (activity == null || activity.isFinishing()) return;
        if (mIsLoginDuringBrowse) {
            activity.setResult(MainActivity.INTENT_RESPONSE_TYPE_SUCCESS);
            if (isSubscriptionFailed) {
                activity.setResult(MainActivity.INTENT_RESPONSE_TYPE_SUCCESS_SUBSCRIPTION_FAILED);
            }
            activity.finish();
            return;
        }
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

        MSISDNLoginEncryptedShreyas.Params msisdnParams = new MSISDNLoginEncryptedShreyas.Params(mEmailID,mPassword);

        MSISDNLoginEncryptedShreyas login = new MSISDNLoginEncryptedShreyas(msisdnParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
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
                        LoggerD.debugOTP("success: msisdn login status : " + response.body().status + " code : " + response.body().code
                                + " message : " + response.body().message);
                        Map<String, String> params = new HashMap<>();
                        //params.put(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_sundirect);

                        if (!TextUtils.isEmpty(response.body().userid)) {
                            params.put(Analytics.USER_ID, response.body().userid);
                        }

                        if (!APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            // login failed
                            params.put(Analytics.REASON_FAILURE, response.body().message);
                            params.put(Analytics.SIGN_UP_OPTION, Analytics.ACCOUNT_CLIENT);
                            params.put(Analytics.ERROR_CODE, String.valueOf(response.body().code));
                            Analytics.trackEvent(Analytics.EventPriority.MEDIUM, Analytics.EVENT_SIGN_UP_FAILED, params);
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                            showDeviceAuthenticationFailedWithOutExit(response.body().message);
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
                            mixpanelOTPLoginFailed(response.body().status, String.valueOf(response.body().code));
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            // login failed
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);

                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        }

                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 217) {
                            isOtpLogin = true;
                            mixpanelOTPLoginInitiated();
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            return;
                        }

                        if (response.body().status.equalsIgnoreCase("SUCCESS")
                                && (response.body().code == 200
                                || response.body().code == 201)) {
                            if(!TextUtils.isEmpty(response.body().serviceName)){
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            myplexAPI.clearCache(APIConstants.BASE_URL);
                            PropertiesHandler.clearCategoryScreenFilter();
                            if(!TextUtils.isEmpty(mEmailID)){
                                PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                            }
                            if(!TextUtils.isEmpty(mEmailID)){
                                PrefUtils.getInstance().setPrefMsisdnNo(mEmailID);
                            }
                            CleverTap.eventRegistrationCompleted(response.body().email, response.body().mobile, Analytics.NO);
                            if(!TextUtils.isEmpty(response.body().mobile)){
                                PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
                            } else if(!TextUtils.isEmpty(response.body().email)){
                                PrefUtils.getInstance().setPrefMsisdnNo(response.body().email);
                            }
                            if (!TextUtils.isEmpty(response.body().email)) {
                                PrefUtils.getInstance().setPrefEmailID(response.body().email);
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
                                    PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
                                    if (response.body().email != null)
                                        PrefUtils.getInstance().setPrefEmailID(response.body().email);
                                    SDKUtils.saveObject(msisdnData, APIConstants.msisdnPath);
                                }
                                LoggerD.debugOTP("Info: msisdn login: " + "success and launching offer");
                                try {
                                    LoggerD.debugOTP("Info: msisdn login: " + "success" + " userid= " + response.body().userid);
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
                                    AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                                }else if(response.body().code == 200){
                                    AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                                }
                                AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_CLIENT);
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.MIXPANEL_PEOPLE_JOINING_DATE, Util.getCurrentDate());
                                params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                                Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                                mixpanelOTPLoginSuccess();
                                fetchOfferAvailability();
                                // mBaseActivity.pushFragment(new OfferedPacksFragment());
                                return;
                            }
                            try {
                                LoggerD.debugOTP("Info: msisdn login: " + "success" + " userid= " + response.body().userid);
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
                                AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                            }else if(response.body().code == 200){
                                AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                            }
                            AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                            Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                            mixpanelOTPLoginSuccess();
                            launchMainActivity();
                        }

                        if (!TextUtils.isEmpty(response.body().message)) {

                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
                            AlertDialogUtil.showToastNotification(response.body().message);
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
                            if(!TextUtils.isEmpty(response.body().serviceName)){
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
        mPassword = mPwdEditText.getText().toString();
        mPassword = mPassword.trim();
        if(TextUtils.isEmpty(mPassword)){
            AlertDialogUtil.showToastNotification("Please enter valid password");
            return;
        }
        if (!isValidEmailID(mEmailID)) {
            //  mMobileNoEditText.clearFocus();

            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
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
            int lengthFromDot = 0;
            if (emailId.indexOf(".") >= 0 && emailId.substring(emailId.indexOf(".")) != null) {
                lengthFromDot = emailId.substring(emailId.indexOf(".")).length();
                LoggerD.debugDownload("lengthFromDot- " + lengthFromDot + " emailId.substring(emailId.indexOf(\".\"))- " + emailId.substring(emailId.indexOf(".")));
            }
            if (emailId.contains("@") && emailId.contains(".") && !emailId.contains(" ") && lengthFromDot > 2) {
                return true;
            }
        }
        return false;
    }



    public void showProgressBar() {

        if (mContext == null) {
            return;
        }
        if(!Util.checkActivityPresent(mContext)){
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
                    isSMSFlow = data.getBooleanExtra("isSMS",false);
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
            args.putBoolean(FragmentSignInWithEmail.PARAM_LOGIN_DURING_BROWSE,mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentMobileSignUpSignIn.newInstance(args));
        }
    }

    private void showOTPragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(FragmentOtpVerificationMobileSignUpSignIn.PARAM_LOGIN_DURING_BROWSE,mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentOtpVerificationMobileSignUpSignIn.newInstance(args));
        }
    }
}
