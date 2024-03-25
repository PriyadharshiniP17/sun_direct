package com.myplex.myplex.utils;


import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//
//import com.facebook.CallbackManager;
//import com.facebook.login.widget.LoginButton;
import com.github.pedrovgs.LoggerD;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.user.MSISDNLoginV3;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.api.request.user.SignUpEncryptedShreyas;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.Countries;
import com.myplex.model.MsisdnData;
import com.myplex.model.OfferResponseData;
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


/**
 * Created by Srikanth on 10/21/2014.
 */
public class FragmentMobileSignUpSignIn extends BaseFragment implements View.OnFocusChangeListener, View.OnTouchListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String PARAM_MSISDN = "msisdn";
    public static final String PARAM_IS_EXISTING_USER = "is_existing_user_to_disable_mobile_number";
    public static final String PARAM_LOGIN_DURING_BROWSE = "during_browse";
    public static final String PARAM_UPDATE_EMAIL = "update_email";
    private View rootView;
    private EditText mMobileNoEditText;
    private EditText passwordField,nameField,emailField,cnfPwdField;
    private String mMobileNo;
    private String mEmailID,pwd,cnfPwd,name;
    private AutoCompleteTextView mDropDownEmailIDs;
    private Button mRegisterButton;
    private Context mContext;
    private GoogleApiClient mGoogleSignInClient;
    private static final int GMAIL_SIGN_IN = 111;
    private static final int PHONE_NUMBER_PICKING_REQUEST = 112;
    public static final String TAG = FragmentMobileSignUpSignIn.class.getName();
    private Button social_fb_login;

    private View.OnClickListener mSignInContinueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FirebaseAnalytics.getInstance().userSignUpStarted();
            showAndProceedStep1SignInRequest();
        }
    };


    private View.OnClickListener mStep3ClickListenerChangeNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Attach and use this to Redirect and change his number similar to start step 1.
            if (mBaseActivity != null) {
                mBaseActivity.removeFragment(FragmentMobileSignUpSignIn.this);
                mBaseActivity.pushFragment(FragmentMobileSignUpSignIn.newInstance(getArguments()));
            }
        }
    };



    private View.OnClickListener mSkipClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PrefUtils.getInstance().setPrefIsOTPSkipped(true);
            launchMainActivity();
        }
    };


    private String mOtp;

    private ProgressDialog mProgressDialog;
    private boolean isOtpRequestManualEnter;
    private boolean mIsExistingUser;
    private boolean isOtpLogin;
    private boolean mIsLoginDuringBrowse;
    private Typeface mRegularFontTypeFace;
    private TextView mTextViewTnC;
    private ImageView mImageViewEmailIdTickMark;
    private ImageView mImageViewMobileNoTickMark;
    private boolean showMobNoWrongTickmark = false;
    private boolean showEmailIdTickMark = false;
    private boolean isSubscriptionFailed = false;
    private String source;
    private String sourceDetails;
    private boolean emailSupported = false;
    private boolean mobileNoSupported = true;
    //    private FrameLayout mFrameLayout;
    Spinner country,gender,age;
    String countrySelected,genderSelected,ageSelected;
    ArrayList<Countries> countryList;
    ArrayList<String> genderLists;
    ArrayList<String> ageLists;

    private Spinner country_code_spinner;
    private TextView country_code_text;
//    private LoginButton loginButton;
//    private CallbackManager callbackManager;
    GoogleApiClient mCredentialsApiClient;


    public static FragmentMobileSignUpSignIn newInstance(Bundle args) {
        FragmentMobileSignUpSignIn fragmentOTPVerification = new FragmentMobileSignUpSignIn();
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
        //callbackManager = CallbackManager.Factory.create();
        mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        mFrameLayout = new FrameLayout(mContext);
        rootView = inflater.inflate(R.layout.sign_in_and_sign_up_mobile_no_shreyas, container, false);
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
        initComponentNew();

        String serverClientId = mContext.getResources().getString(R.string.server_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(serverClientId)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        mCredentialsApiClient= new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .enableAutoManage(mActivity, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();
        requestHint();

        return rootView;
    }


    private void initComponentNew() {
        mMobileNoEditText=rootView.findViewById(R.id.mobileSignUp);
        country_code_text=rootView.findViewById(R.id.country_code_text);
        Button redirectToEmailSignIn = rootView.findViewById(R.id.signInText);
        Button redirectToOtpScreen=rootView.findViewById(R.id.signupsignin);
        country_code_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

        ImageButton signInGoogle=rootView.findViewById(R.id.sign_in_button1);
        signInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button1:
                        Googlesign();
                        break;
                }
            }
        });
        social_fb_login=rootView.findViewById(R.id.facebook_icon_sign_up_social);
//        loginButton = (LoginButton)rootView.findViewById(R.id.login_button);
//        loginButton.setReadPermissions(Arrays.asList("basic_info", "email", "user_likes", "user_status"));
 //       loginButton.setReadPermissions(Arrays.asList("email","public_profile"));
        // If you are using in a fragment, call loginButton.setFragment(this);
        social_fb_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbSignin();

            }
        });

        redirectToEmailSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInScreenFragment();
            }
        });

        redirectToOtpScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectMobileAndSendToOtpFragment();
            }
        });

    }

    private void showBottomSheetDialog(){
        final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(mContext);
        bottomSheetDialog.setContentView(R.layout.country_code_layout);
        LinearLayout classlayout = (LinearLayout) bottomSheetDialog.findViewById(R.id.code_layout);
        String jsonFileString = getJsonFromAssets(getApplicationContext(), "country.json");
        Log.i("data", jsonFileString);
        Gson gson = new Gson();
        Type listUserType = new TypeToken<ArrayList<Countries>>() { }.getType();
        ArrayList<Countries> countriesList = gson.fromJson(jsonFileString, listUserType);
        final TextView[] codeTV=new TextView[countriesList.size()];
        for (int i = 0; i < countriesList.size(); i++) {
            codeTV[i]=new TextView(mContext);
            codeTV[i].setText(countriesList.get(i).name +" - "+countriesList.get(i).dial_code);
            mRegularFontTypeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");
            codeTV[i].setTypeface(mRegularFontTypeFace);
            codeTV[i].setPadding(16, 16, 16, 16);
            codeTV[i].setTextSize(12);
            codeTV[i].setTextColor(getResources().getColor(R.color.white));
            codeTV[i].setTag(countriesList.get(i));
            codeTV[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Countries countries= (Countries) v.getTag();
                    country_code_text.setText(countries.dial_code);
                    bottomSheetDialog.dismiss();
                }
            });
            if (classlayout != null) {
                classlayout.addView(codeTV[i]);
            }
        }
        bottomSheetDialog.show();
    }

    private void collectMobileAndSendToOtpFragment() {
        // read mobile number and email ID's and validate for API request.
        LoggerD.debugOTP("showAndProceedStep1SignInRequest");

        mMobileNo = mMobileNoEditText.getText().toString();
        boolean isValidPhoneNumber = false;
        try {
            long num = Long.parseLong(mMobileNo);
            LoggerD.debugOTP(num + " is a number");
            if (mMobileNo.length() >= 9 && mMobileNo.length()<=15 && mMobileNo.matches("[0-9]+")) {
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
        }
        //Make login request and Start Otp reader and listen for otp
        mMobileNo = country_code_text.getText().toString() + mMobileNo;
        mMobileNo = mMobileNo.replace("+","");
        PrefUtils.getInstance().setPrefTempMsisdn(mMobileNo);
        FirebaseAnalytics.getInstance().setMobileNumberProperty(mMobileNo);
        hideSoftInputKeyBoard(mMobileNoEditText);
        isOtpRequestManualEnter = false;
        FirebaseAnalytics.getInstance().userSignInStarted();
        getOtpRequest();
    }

    /*private void initComponent() {
        mMobileNoEditText = (EditText) rootView.findViewById(R.id.mobileSignUp);
        nameField = (EditText) rootView.findViewById(R.id.nameSignUp);
        emailField = (EditText) rootView.findViewById(R.id.emailIDSignUp);
        cnfPwdField = (EditText) rootView.findViewById(R.id.cnfpwdSignUp);
        passwordField = (EditText) rootView.findViewById(R.id.pwdSignUp);
        country = (Spinner)rootView.findViewById(R.id.countrySignUp);
        gender = (Spinner)rootView.findViewById(R.id.genderSignUp);
        age = (Spinner)rootView.findViewById(R.id.ageSignUp);
        mRegisterButton = (Button) rootView.findViewById(R.id.registerSignUp);
        TextView signInClick = (TextView)rootView.findViewById(R.id.signInText);

        country_code_spinner = rootView.findViewById(R.id.country_code_spinner);
        country_code_text = rootView.findViewById(R.id.country_code_text);
        ImageButton signInGoogle=rootView.findViewById(R.id.sign_in_button1);
        signInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button1:
                        Googlesign();
                        break;
                }
            }
        });
        social_fb_login=rootView.findViewById(R.id.facebook_icon_sign_up_social);
//        loginButton = (LoginButton)rootView.findViewById(R.id.login_button);
//        loginButton.setReadPermissions(Arrays.asList("basic_info", "email", "user_likes", "user_status"));
        //       loginButton.setReadPermissions(Arrays.asList("email","public_profile"));
        // If you are using in a fragment, call loginButton.setFragment(this);
        social_fb_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbSignin();

            }
        });

        signInClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInScreenFragment();
            }
        });


        countryList = new ArrayList<>();
        genderLists = new ArrayList<>();
        String[] ageRange = PrefUtils.getInstance().getUserAgeRange().split(",");
        List<String> ageLists = new ArrayList<String>(Arrays.asList(ageRange));
        ageLists.add(0, "Select Age");
        ArrayAdapter<String> ageAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, ageLists);
        //ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age.setAdapter(ageAdapter);


        String[]genderRange = PrefUtils.getInstance().getUserGenderRange().split(",");
        List<String> genderLists = new ArrayList<String>(Arrays.asList(genderRange));
        genderLists.add(0, "Select Gender");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(mContext,R.layout.spinner_item , genderLists);
        //genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(genderAdapter);

        String jsonFileString = getJsonFromAssets(getApplicationContext(), "country.json");
        Log.i("data", jsonFileString);
        Gson gson = new Gson();
        Type listUserType = new TypeToken<ArrayList<Countries>>() { }.getType();
        ArrayList<Countries> countriesList = gson.fromJson(jsonFileString, listUserType);
        countriesList.add(0, new Countries("Select Country","0","0"));
        String[] countries = new String[countriesList.size() ];
        String[] countriesForCode = new String[countriesList.size() -1];
        for (int i = 0; i < countriesList.size(); i++) {
            if(i > 0){
                countriesForCode[i-1] = countriesList.get(i).name;
            }
            countries[i] = countriesList.get(i).name;
        }
        ArrayAdapter countryAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, countries);
        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(countryAdapter);
        final List<Countries> countriesList1 = countriesList;
        countriesList1.remove(0);
        mRegisterButton.setOnClickListener(mSignInContinueClickListener);

        //For country Code
        ArrayAdapter countryAdapterForCode = new ArrayAdapter<String>(mContext, R.layout.spinner_item, countriesForCode);
        country_code_spinner.setAdapter(countryAdapterForCode);
        country_code_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                country_code_spinner.performClick();
            }
        });
        country_code_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                country_code_text.setText(countriesList1.get(position).dial_code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }*/

    private  void  fbSignin(){
        //loginButton.performClick();
    }

    private void Googlesign() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        if (getActivity() != null)
            getActivity().startActivityForResult(signInIntent, GMAIL_SIGN_IN);
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

    private void trimMobileNumberTo10Digits() {
        if (!TextUtils.isEmpty(mMobileNo)) {
            try {
                LoggerD.debugOTP("mMobileNo- " + mMobileNo);
                if (mMobileNo.length() > 10) {
                    mMobileNo = mMobileNo.substring(mMobileNo.length() % 10, mMobileNo.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onFocusChange(final View v, final boolean hasFocus) {

    }

    private void handleFocusChange(View v) {

        if (v == null ) return;

        if (v.getId() == R.id.mobileSignUp) {
            validateAndUpdateUIForMobileNo();
            return;
        }
      /*  if (v.getId() == R.id.emailIDSignUp) {
            validateAndUpdateUIForEmailId();
            return;
        }*/
    }

    private void validateAndUpdateUIForMobileNo() {
        mMobileNo = mMobileNoEditText.getText().toString();

        boolean isValidPhoneNumber = false;
        try {
            long num = Long.parseLong(mMobileNo);
            LoggerD.debugOTP(num + " is a number");
            if (mMobileNo.length() == 10) {
                // showMobNoWrongTickmark = true;
                isValidPhoneNumber = true;
            }
        } catch (NumberFormatException e) {
            LoggerD.debugOTP(mMobileNo + "is not a number");
        }
        // mImageViewMobileNoTickMark.setImageResource(0);
        if (mMobileNo == null || mMobileNo.isEmpty()) {
           /* showMobNoWrongTickmark = false;
            mImageViewMobileNoTickMark.setVisibility(View.GONE);
            mImageViewMobileNoTickMark.setImageResource(0);*/
            //return;
        }
       /* if (showMobNoWrongTickmark) {
            mImageViewMobileNoTickMark.setVisibility(View.VISIBLE);
            mImageViewMobileNoTickMark.setImageResource(R.drawable.otp_cross_icon);
        }*/

        if (isValidPhoneNumber) {
            /*mImageViewMobileNoTickMark.setVisibility(View.VISIBLE);
            mImageViewMobileNoTickMark.setImageResource(R.drawable.otp_correct_icon);*/
        }else{
            Toast.makeText(mContext,"Invalid Mobile number",Toast.LENGTH_LONG).show();
        }

    }

    private void validateAndUpdateUIForEmailId() {
        mEmailID = emailField.getText().toString();
        boolean isEmailValid = false;
        mEmailID = mEmailID.trim();
        // mImageViewEmailIdTickMark.setImageResource(0);

        if (mEmailID != null && mEmailID.length() == 0) {
           /* showEmailIdTickMark = false;
            mImageViewEmailIdTickMark.setVisibility(View.GONE);
            mImageViewEmailIdTickMark.setImageResource(0);*/
            // return;
        }

       /* if (showEmailIdTickMark && emailSupported) {
            mImageViewEmailIdTickMark.setVisibility(View.VISIBLE);
            mImageViewEmailIdTickMark.setImageResource(R.drawable.otp_cross_icon);
        }*/
        if (isValidEmailID(mEmailID) ) {
            /*showEmailIdTickMark = true;
            mImageViewEmailIdTickMark.setVisibility(View.VISIBLE);
            mImageViewEmailIdTickMark.setImageResource(R.drawable.otp_correct_icon);*/
        }else{
            Toast.makeText(mContext,"Invalid Email ID",Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDropDownEmailIDs != null) {
            hideSoftInputKeyBoard(mDropDownEmailIDs);
        }
        if (mMobileNoEditText != null) {
            hideSoftInputKeyBoard(mMobileNoEditText);
        }
        if (mCredentialsApiClient !=null){
            mCredentialsApiClient.stopAutoManage(mActivity);
            mCredentialsApiClient.disconnect();
        }

/*        if (mOTPEditText != null) {
            hideSoftInputKeyBoard(mOTPEditText);
        }*/
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


    private void makeUserSignUpRequest() {

        showProgressBar();
        CleverTap.eventRegistrationInitiated(mEmailID.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);

        LoggerD.debugOTP("emailId- " + mEmailID);
        SignUpEncryptedShreyas.Params msisdnParams = new SignUpEncryptedShreyas.Params(mMobileNo,mEmailID,name,pwd,cnfPwd,countrySelected,genderSelected,ageSelected);

        SignUpEncryptedShreyas login = new SignUpEncryptedShreyas(msisdnParams,
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
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, "Invalid API Response");
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
                            FirebaseAnalytics.getInstance().userSignUpCompleted();
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                            showDeviceAuthenticationFailedWithOutExit(response.body().message);
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
                            mixpanelOTPLoginFailed(response.body().status, String.valueOf(response.body().code));
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED,response.body().message);
                            }
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            // login failed
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);

                            FirebaseAnalytics.getInstance().userSignUpCompleted();
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        }

                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 217) {
                            isOtpLogin = true;
                            FirebaseAnalytics.getInstance().userSignUpCompleted();
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
                                    //PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
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
                                FirebaseAnalytics.getInstance().userSignUpCompleted();
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
                            FirebaseAnalytics.getInstance().userSignUpCompleted();
                            AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                            Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                            mixpanelOTPLoginSuccess();
                            launchMainActivity();
                        }

                        if (!TextUtils.isEmpty(response.body().message)) {
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED,response.body().message);
                            }
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
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
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailedWithOutExit(null);
                    }
                });
        APIService.getInstance().execute(login);
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
/*        mTextViewHeading1.setVisibility(View.VISIBLE);
        mTextViewHeading2.setVisibility(View.VISIBLE);
        mTextViewHeading3.setVisibility(View.GONE);
        mTextViewHeading2.setText(mContext.getString(R.string.otp_enter_email));*/
        if (PrefUtils.getInstance().getPrefEnableSkipOnOTP()) {
           /* mTextViewSkip.setVisibility(View.VISIBLE);
            mTextViewSkip.setTypeface(msundirectRegularFontTypeFace, Typeface.BOLD_ITALIC);
            mTextViewSkip.setOnClickListener(mSkipClickListener);*/
        }
    }

    private void showAndProceedStep1SignInRequest() {
        // read mobile number and email ID's and validate for API request.
        LoggerD.debugOTP("showAndProceedStep1SignInRequest");



        mRegisterButton.setVisibility(View.VISIBLE);

        mMobileNo = mMobileNoEditText.getText().toString();
        mEmailID = emailField.getText().toString();
        name = nameField.getText().toString();
        pwd = passwordField.getText().toString();
        cnfPwd = cnfPwdField.getText().toString();
        //mEmailID = emailField.getText().toString();

        boolean isValidPhoneNumber = false;
        try {
            long num = Long.parseLong(mMobileNo);
            LoggerD.debugOTP(num + " is a number");
            if (mMobileNo.length() >= 9 && mMobileNo.length()<=15&&mMobileNo.matches("[0-9]+")) {
                isValidPhoneNumber = true;
            }
        } catch (NumberFormatException e) {
            LoggerD.debugOTP(mMobileNo + "is not a number");
        }
        if(TextUtils.isEmpty(country_code_text.getText().toString())){
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_country_code));
            return;
        }
        mMobileNo = country_code_text.getText().toString() + mMobileNo;
        mMobileNo = mMobileNo.replace("+","");
        mEmailID = mEmailID.trim();
        if (!isValidPhoneNumber) {
            mMobileNoEditText.requestFocus();
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            // CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_MOBILE_NO);
            return;
        }
        if (!isValidEmailID(mEmailID) ) {
            mMobileNoEditText.clearFocus();
            emailField.requestFocus();
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
            //CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }

        if(!isValidName()){
            mMobileNoEditText.clearFocus();
            emailField.clearFocus();
            nameField.requestFocus();
            AlertDialogUtil.showToastNotification("Please enter valid name");
            return;
        }
        if(!isValidPassword()){
            mMobileNoEditText.clearFocus();
            emailField.clearFocus();
            nameField.requestFocus();
            return;
        }
        if (country.getSelectedItemPosition() == 0) {
            Toast.makeText(mContext, "Please select country", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (country.getSelectedItem() != null) {
                countrySelected = country
                        .getSelectedItem().toString();
            } else {
                Toast.makeText(mContext, "Please select Country", Toast.LENGTH_SHORT).show();
                return;

            }

        }
        if (gender.getSelectedItemPosition() == 0) {
            Toast.makeText(mContext, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (gender.getSelectedItem() != null) {
                genderSelected = gender
                        .getSelectedItem().toString();
                if(genderSelected.startsWith("M")){
                    genderSelected = "M";
                }else if(genderSelected.startsWith("F")){
                    genderSelected = "F";
                }else if(genderSelected.startsWith("T")){
                    genderSelected = "T";
                }
            } else {
                Toast.makeText(mContext, "Please select Gender", Toast.LENGTH_SHORT).show();
                return;

            }

        }
        if (age.getSelectedItemPosition() == 0) {
            Toast.makeText(mContext, "Please select age", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (age.getSelectedItem() != null) {
                ageSelected = age
                        .getSelectedItem().toString();
            } else {
                Toast.makeText(mContext, "Please select Age", Toast.LENGTH_SHORT).show();
                return;

            }

        }
        //Make login request and Start Otp reader and listen for otp
/*        PrefUtils.getInstance().setPrefTempMsisdn(mMobileNo);
        PrefUtils.getInstance().setPrefTempEMAILID(mEmailID);*/
        //hideSoftInputKeyBoard(mDropDownEmailIDs);
        //hideSoftInputKeyBoard(mOTPEditText);

        makeUserSignUpRequest();

    }

    private boolean isValidName() {
        if(TextUtils.isEmpty(name.trim())){
            return false;
        }
        return true;
    }


    private boolean isValidPassword() {
        if(TextUtils.isEmpty(pwd.trim())){
            AlertDialogUtil.showToastNotification("Please enter password");
            return false;
        }
        if(TextUtils.isEmpty(cnfPwd.trim())){
            AlertDialogUtil.showToastNotification("Please enter confirm password");
            return false;

        }

        if(!pwd.equals(cnfPwd)){
            AlertDialogUtil.showToastNotification("Passwords didn't match");
            return false;
        }
        return true;
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

    private void requestHint() {
       /*GoogleApiClient mCredentialsApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .enableAutoManage(mActivity, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();
        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(mCredentialsApiClient,hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    PHONE_NUMBER_PICKING_REQUEST, null, 0, 0, 0,null);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }*/



        HintRequest hintRequest = new HintRequest.Builder()
                .setHintPickerConfig(new CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .build())
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(mCredentialsApiClient,hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), PHONE_NUMBER_PICKING_REQUEST, null, 0, 0, 0,null);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Could not start hint picker Intent", e);
        }
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
        }else if (requestCode==PHONE_NUMBER_PICKING_REQUEST){
            if(data!=null){
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                if (credential != null) {
                    mMobileNo=credential.getId();
                    mMobileNo = mMobileNo.replace("+","");
                    getOtpRequest();
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

    private void showSignInScreenFragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(FragmentSignInWithEmail.PARAM_LOGIN_DURING_BROWSE,mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentSignInWithEmail.newInstance(args));
        }
    }

    private void showOTPragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putString(FragmentOtpVerificationMobileSignUpSignIn.PARAM_MSISDN,mMobileNo);
            args.putBoolean(FragmentOtpVerificationMobileSignUpSignIn.PARAM_LOGIN_DURING_BROWSE,mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentOtpVerificationMobileSignUpSignIn.newInstance(args));
        }
    }

    private void getOtpRequest() {

        showProgressBar();

        if (TextUtils.isEmpty(mOtp)) {
            CleverTap.eventRegistrationInitiated("", mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);
        }
        MSISDNLoginV3.Params msisdnParams = new MSISDNLoginV3.Params(mMobileNo,"",mOtp);

        MSISDNLoginV3 login = new MSISDNLoginV3(msisdnParams,
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
                            showOTPragment();
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            return;
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Log.d(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        //Log.d(TAG, "GoogleApiClient is suspended with cause code: " + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Log.d(TAG, "GoogleApiClient failed to connect: " + connectionResult);
    }
}
