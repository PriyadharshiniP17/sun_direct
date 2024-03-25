package com.myplex.myplex.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//
//import com.facebook.CallbackManager;
//import com.facebook.login.LoginBehavior;
//import com.facebook.login.widget.LoginButton;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.myplex.model.Countries;
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
import com.myplex.myplex.ui.fragment.OfferFragment;
import com.myplex.myplex.ui.fragment.PackagesFragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.myplex.api.myplexAPISDK.getApplicationContext;
import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;
import static com.myplex.myplex.utils.Util.getJsonFromAssets;
import static de.greenrobot.event.EventBus.TAG;


/**
 * Created by Srikanth on 10/21/2014.
 */
public class FragmentOTPVerification_shreyas extends BaseFragment implements  OtpReader.OTPListener{

    public static final String PARAM_MSISDN = "msisdn";
    public static final String PARAM_IS_EXISTING_USER = "is_existing_user_to_disable_mobile_number";
    public static final String PARAM_LOGIN_DURING_BROWSE = "during_browse";
    public static final String PARAM_UPDATE_EMAIL = "update_email";
    private View rootView;


    private EditText mMobileNoEditText;
    private EditText mOTPEditText;

    private Context mContext;
    private OtpReader mOtpReader;

    private Spinner country_code_spinner;
    private TextView country_code_text;
    private String otp, phnNum,userEmail,userId;
    private int numberOfLoginAttempts = 0;
    private FragmentSignIn fragmentSignIn;
//    private CallbackManager callbackManager;
//    private LoginButton loginButton;

    private View.OnClickListener mSignInContinueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAndProceedStep1SignInRequest();
        }
    };
    private View.OnClickListener mStep1ClickListenerResendOTP = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            showAndProceedStep2WaitForAutoDetect();
//            Attach and use this for Resend otp action
            if (mOTPEditText != null) {
                hideSoftInputKeyBoard(mOTPEditText);
            }
            mOTPEditText.setText("");
            mOTPEditText.clearFocus();
            showAndProceedStep1SignInRequest();
        }
    };

    private View.OnClickListener mStep3ClickListenerChangeNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Attach and use this to Redirect and change his number similar to start step 1.
            if (mBaseActivity != null) {
                mBaseActivity.removeFragment(FragmentOTPVerification_shreyas.this);
                mBaseActivity.pushFragment(FragmentOTPVerification_shreyas.newInstance(getArguments()));
            }
        }
    };



    private String mMobileNo;
    private String mOtp;

    private ProgressDialog mProgressDialog;
    private boolean isOtpRequestManualEnter;

    private boolean isOtpLogin;
    private boolean mIsLoginDuringBrowse;

    private boolean showMobNoWrongTickmark = false;
    private boolean showEmailIdTickMark = false;
    private boolean isSubscriptionFailed = false;
    private String source;
    private String sourceDetails;
    private boolean emailSupported = false;
    private boolean mobileNoSupported = true;
//    private FrameLayout mFrameLayout;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int GMAIL_SIGN_IN = 111;

    public static FragmentOTPVerification_shreyas newInstance(Bundle args) {
        FragmentOTPVerification_shreyas fragmentOTPVerification = new FragmentOTPVerification_shreyas();
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
//        mFrameLayout = new FrameLayout(mContext);
        rootView = inflater.inflate(R.layout.fragment_otp_shreyas, container, false);
        Button emailLogin = rootView.findViewById(R.id.emailIdButtonSignIN);
        emailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showemailLoginragment();
            }
        });
        Button signUpBtn = rootView.findViewById(R.id.signUpButtonSignIN);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignUpScreenFragment();
            }
        });
        Button signInPhnBtn = rootView.findViewById(R.id.phnButtonSignIN);
        signInPhnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignUpScreenFragment();
            }
        });
//        loginButton = (LoginButton)rootView.findViewById(R.id.login_button);
//        loginButton.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        Button signInButtonFb=rootView.findViewById(R.id.sign_in_button_fb);
        signInButtonFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInFB();
            }
        });

        Button signInButton= rootView.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        readBundleValues(getArguments());
        source = null;
        if (getArguments() != null && getArguments().containsKey(Analytics.PROPERTY_SOURCE)) {
            source = getArguments().getString(Analytics.PROPERTY_SOURCE);
        }

        sourceDetails = null;
        if (getArguments() != null && getArguments().containsKey(Analytics.PROPERTY_SOURCE_DETAILS)) {
            sourceDetails = getArguments().getString(Analytics.PROPERTY_SOURCE_DETAILS);
        }

        initComponent();


        String serverClientId = mContext.getResources().getString(R.string.server_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext,googleSignInOptions);
//        mFrameLayout.addView(rootView);



        return rootView;
    }


    private void initComponent() {
        mMobileNoEditText = (EditText) rootView.findViewById(R.id.phnSignIn);
        mOTPEditText = (EditText) rootView.findViewById(R.id.pwdSignIn);


        Button mMobileLoginBtn = rootView.findViewById(R.id.phnButtonSignIN);
        mMobileLoginBtn.setOnClickListener(mSignInContinueClickListener);

        country_code_spinner = rootView.findViewById(R.id.country_code_spinner);
        country_code_text = rootView.findViewById(R.id.country_code_text);

        String jsonFileString = getJsonFromAssets(getApplicationContext(), "country.json");
        Log.i("data", jsonFileString);
        Gson gson = new Gson();
        Type listUserType = new TypeToken<ArrayList<Countries>>() { }.getType();
        final ArrayList<Countries> countriesList = gson.fromJson(jsonFileString, listUserType);

        final String[] countries = new String[countriesList.size() ];

        for (int i = 0; i < countriesList.size(); i++) {
            countries[i ] = countriesList.get(i).name;
        }
        ArrayAdapter countryAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, countries);
        country_code_spinner.setAdapter(countryAdapter);
        country_code_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                country_code_spinner.performClick();
            }
        });
        country_code_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                country_code_text.setText(countriesList.get(position).dial_code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initUI() {


       /* if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefTempMsisdn())) {
            mMobileNo = PrefUtils.getInstance().getPrefTempMsisdn();
        }*/



    }

    private void readBundleValues(Bundle arguments) {
        if (arguments == null) {
            return;
        }
        if (arguments.containsKey(PARAM_MSISDN)) {
            mMobileNo = arguments.getString(PARAM_MSISDN);
            LoggerD.debugOTP("readBundleValues: mMobileNo- " + mMobileNo);
        }

        if (arguments.containsKey(PARAM_LOGIN_DURING_BROWSE)) {
            mIsLoginDuringBrowse = arguments.getBoolean(PARAM_LOGIN_DURING_BROWSE);
        }
    }




    @Override
    public void onPause() {
        super.onPause();

        if (mMobileNoEditText != null) {
            hideSoftInputKeyBoard(mMobileNoEditText);
        }
        if (mOTPEditText != null) {
            hideSoftInputKeyBoard(mOTPEditText);
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
        AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", mContext.getResources()
                        .getString(R.string.feedbackokbutton),
                new AlertDialogUtil.NeutralDialogListener() {
                    @Override
                    public void onDialogClick(String buttonText) {
                    }
                });
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

        if (TextUtils.isEmpty(mOtp)) {
            CleverTap.eventRegistrationInitiated("", mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);
        }
        MSISDNLoginEncryptedShreyas.Params msisdnParams = new MSISDNLoginEncryptedShreyas.Params(mMobileNo,mOtp,"");

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
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                            showDeviceAuthenticationFailedWithOutExit(response.body().message);
                            CleverTap.eventRegistrationFailed("", mMobileNo, response.body().message);
                            mixpanelOTPLoginFailed(response.body().status, String.valueOf(response.body().code));
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED,response.body().message);
                            }
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            // login failed
                            CleverTap.eventRegistrationFailed("", mMobileNo, response.body().message);

                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        }

                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 217) {
                            isOtpLogin = true;
                            mixpanelOTPLoginInitiated();
                            startOtpReader();
                            showAndProceedStep2WaitForAutoDetect();
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            return;
                        }

                        if (response.body().status.equalsIgnoreCase("SUCCESS")
                                && (response.body().code == 200
                                || response.body().code == 201)) {
                            if(!TextUtils.isEmpty(response.body().serviceName)){
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }
                            myplexAPI.clearCache(APIConstants.BASE_URL);
                            PropertiesHandler.clearCategoryScreenFilter();
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.SUCCESS,null);
                            }
                            CleverTap.eventRegistrationCompleted(response.body().email, response.body().mobile, Analytics.NO);
                            PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
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
                                FirebaseAnalytics.getInstance().userSignedInCompleted();
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
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED,response.body().message);
                            }
                            CleverTap.eventRegistrationFailed("", mMobileNo, response.body().message);
                            AlertDialogUtil.showToastNotification(response.body().message);
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        dismissProgressBar();
                        LoggerD.debugOTP("Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED,mContext.getString(R.string.network_error));
                            }
                            CleverTap.eventRegistrationFailed("", mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailedWithOutExit(null);
                    }
                });
        APIService.getInstance().execute(login);
    }

    private void updateProfileWithEmailID() {
        showProgressBar();
        Analytics.mixpanelProfileEmailInitiated("");
        ProfileUpdateWithEmailIDRequest.Params profileUpdateParams = new ProfileUpdateWithEmailIDRequest.Params("");

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
        params.put(Analytics.PARAM_OTP, mOtp);
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
        params.put(Analytics.PARAM_OTP, mOtp);
        params.put(Analytics.REASON_FAILURE, reason);
        params.put(Analytics.ERROR_CODE, errorCode);
        Analytics.mixpanelOTPLoginFailed(params);
    }

    private void showAlreadyExistingUserScreen() {
        mMobileNoEditText.setEnabled(false);

    }

    private void showAndProceedStep1SignInRequest() {
        // read mobile number and email ID's and validate for API request.
        LoggerD.debugOTP("showAndProceedStep1SignInRequest");


        mMobileNo = mMobileNoEditText.getText().toString();
        mOtp =mOTPEditText.getText().toString();
        boolean isValidPhoneNumber = false;
        try {
            long num = Long.parseLong(mMobileNo);
            LoggerD.debugOTP(num + " is a number");
            if (mMobileNo.length() == 10 && mMobileNo.matches("[0-9]+")) {
                isValidPhoneNumber = true;
            }
        } catch (NumberFormatException e) {
            LoggerD.debugOTP(mMobileNo + "is not a number");
        }
        if(TextUtils.isEmpty(country_code_text.getText().toString())){
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_country_code));
            return;
        }
        if (!isValidPhoneNumber) {
            mMobileNoEditText.requestFocus();
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            return;
        } if (mOTPEditText.getVisibility()==View.VISIBLE&&
              TextUtils.isEmpty(mOTPEditText.getText().toString())){
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.empty_otp_text));
            return;
        }
        if (mOTPEditText.getVisibility()==View.VISIBLE && mOTPEditText.getText().toString().length()!=6){
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_length_error));
            return;
        }
        //Make login request and Start Otp reader and listen for otp
        mMobileNo = country_code_text.getText().toString() + mMobileNo;
        mMobileNo = mMobileNo.replace("+","");
        PrefUtils.getInstance().setPrefTempMsisdn(mMobileNo);
        FirebaseAnalytics.getInstance().setMobileNumberProperty(mMobileNo);
        hideSoftInputKeyBoard(mMobileNoEditText);
        hideSoftInputKeyBoard(mOTPEditText);
        isOtpRequestManualEnter = false;
        FirebaseAnalytics.getInstance().userSignInStarted();
        makeSMSRetrieverAPI();
        makeUserLoginRequest();

    }



    private void showAndProceedStep2WaitForAutoDetect() {
        // Look up for OTP message If detected automatically send it
        LoggerD.debugOTP("showAndProceedStep2WaitForAutoDetect");
//        Analytics.createScreenGA(Analytics.SCREEN_OTP_SCREEN);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_OTP_SCREEN);
        // mMobileNoEditText.setVisibility(View.GONE);
        mMobileNoEditText.clearFocus();
        mOTPEditText.setVisibility(View.VISIBLE);
        mOTPEditText.requestFocus();

        mOTPEditText.setHint(mContext.getString(R.string.otp_otp_hint));

    }



    private void showAndProceedStep4SendManualOTP() {
        // read mobile number and email ID's and validate for API request.
        LoggerD.debugOTP("showAndProceedStep3AllowEnterOtpManual");

        isOtpRequestManualEnter = true;

        mOtp = mOTPEditText.getText().toString();
        CleverTap.eventOtpEntered(mOtp,"manual");
        if (mOtp.length() <= 0) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_otp));
            return;
        }


/*

        mTextViewHeading1.setText(mContext.getString(R.string.otp_stp2_heading1));
        mTextViewHeading2.setText(mContext.getString(R.string.otp_stp2_heading2));
        mTextViewHeading3.setText(mContext.getString(R.string.otp_stp2_heading3));
*/



//        mOtp = mOTPEditText.getText().toString();

       /* mButton1.setEnabled(false);
        mButton1.postDelayed(new Runnable() {
            @Override
            public void run() {
                mButton1.setEnabled(true);
            }
        },2000);*/

        //Make login request and Start Otp reader and listen for otp
        PrefUtils.getInstance().setPrefTempMsisdn(mMobileNo);
        makeUserLoginRequest();

    }

    @Override
    public void otpReceived(String messageText) {
        mOtp = messageText;
        isOtpRequestManualEnter = true;
        LoggerD.debugOTP("messageText- " + messageText);
        mOTPEditText.setText(messageText);
        CleverTap.eventOtpEntered(mOtp, "auto");
        makeUserLoginRequest();
        stopOtpReader();
    }

    private void startOtpReader() {
        LoggerD.debugOTP("startOTPReader");
        if (mContext == null) {
            LoggerD.debugOTP("mCotext == null");
            return;
        }
        mOtpReader = OtpReader.getInstance(mContext);
        mOtpReader.start(ApplicationController.getAppContext(), PrefUtils.getInstance().getPrefOTPDetectionTimeOut());
        mOtpReader.setOtpListener(this);
    }

    private void stopOtpReader() {
        LoggerD.debugOTP("startOTPReader");
        if (mOtpReader == null) {
            LoggerD.debugOTP("mCotext == null");
            return;
        }
        mOtpReader.stop();
        mOtpReader = null;
    }


    @Override
    public void otpTimeOut() {

        stopOtpReader();
        CleverTap.eventOtpStatus(null, APIConstants.FAILED, Analytics.EVENT_VALUE_OTP_TIME_OUT);
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
        stopOtpReader();
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

    private void showemailLoginragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            mBaseActivity.pushFragment(FragmentSignIn.newInstance(args));
        }
    }

    private void showSignUpScreenFragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(FragmentOTPVerification_shreyas.PARAM_LOGIN_DURING_BROWSE,mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentSignUp.newInstance(args));
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        if (getActivity() != null)
            getActivity().startActivityForResult(signInIntent, GMAIL_SIGN_IN);
    }

    private void signInFB(){
        //loginButton.performClick();

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
                        phnNum = null;
                        userId = response.body().userid;
                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.REASON_FAILURE, response.body().message);
                        params.put(Analytics.SIGN_UP_OPTION, Analytics.ACCOUNT_CLIENT);
                        params.put(Analytics.ERROR_CODE, String.valueOf(response.body().code));
                        Analytics.trackEvent(Analytics.EventPriority.MEDIUM, Analytics.EVENT_SIGN_UP_FAILED, params);
                        LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        showDeviceAuthenticationFailedWithOutExit(response.body().message);
                        CleverTap.eventRegistrationFailed(userEmail, mMobileNo, response.body().message);
                        mixpanelOTPLoginFailed(response.body().message, String.valueOf(response.body().code));

                        return;
                    }
                    if (response.body().code == 200 || response.body().code == 201 ) {
                        myplexAPI.clearCache(APIConstants.BASE_URL);
                        PropertiesHandler.clearCategoryScreenFilter();
                        if(!TextUtils.isEmpty(userEmail)){
                            PrefUtils.getInstance().setPrefMsisdnNo(userEmail);
                        }
                        if(!TextUtils.isEmpty(userEmail)){
                            PrefUtils.getInstance().setPrefMsisdnNo(userEmail);
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
}
