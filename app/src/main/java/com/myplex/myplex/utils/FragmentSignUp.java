package com.myplex.myplex.utils;


import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static com.myplex.api.myplexAPISDK.getApplicationContext;
import static com.myplex.myplex.ApplicationController.getAppContext;
import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;
import static com.myplex.myplex.utils.Util.getJsonFromAssets;

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
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.api.request.user.RequestSMCNumbers;
import com.myplex.api.request.user.SignUpEncryptedShreyas;
import com.myplex.api.request.user.SignUpOTPRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.Countries;
import com.myplex.model.MsisdnData;
import com.myplex.model.OfferResponseData;
import com.myplex.model.SMCLIstResponse;
import com.myplex.model.SmcDatum;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.OfferFragment;
import com.myplex.myplex.ui.fragment.PackagesFragment;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.facebook.CallbackManager;
//import com.facebook.login.widget.LoginButton;


/**
 * Created by Srikanth on 10/21/2014.
 */
public class FragmentSignUp extends BaseFragment implements View.OnFocusChangeListener, View.OnTouchListener {

    public static final String PARAM_MSISDN = "msisdn";
    public static final String PARAM_IS_EXISTING_USER = "is_existing_user_to_disable_mobile_number";
    public static final String PARAM_LOGIN_DURING_BROWSE = "during_browse";
    public static final String PARAM_UPDATE_EMAIL = "update_email";
    private View rootView;
    private EditText mMobileNoEditText;
    private String enteredOTP, newOTP;
    private TextView bottomText;
    private EditText passwordField, mobileNumber, subscriberName, cnfPwdField, otpEditText, newMobileNumberSignUp;
    private TextView mobileValid, subscriberValid, smartCardValid, otpValid, mOtpText, resendOTPButton, signUpText, newMobileNumberValid;
    private RelativeLayout smartCardFeild, smartCardFieldLayout;
    private String mMobileNo;
    private TextInputLayout otpInputLayout;
    private TextView mSmartCardText, mMobileNumberText, mSubscriberNameText, mSuggestionText, mNewMobileNumberText;
    /* private EditText passwordField,nameField,emailField,cnfPwdField;
     private String mEmailID,pwd,cnfPwd,name;*/
    private String mSubscribername, mSmartCardNumber, cnfPwd, name, mobileNumberText, newMobileNumberText;
    private AutoCompleteTextView mDropDownEmailIDs;
    private Button mRegisterButton, otpValidationButton;
    private Context mContext;
    private RecyclerView smartCardNumberSpinner;
    private SmartCardRecyclerViewAdapter smartCardRecyclerViewAdapter;
    private ImageView smcSpinerImg;
    private GoogleApiClient mGoogleSignInClient;
    private static final int GMAIL_SIGN_IN = 111;
    public static final String TAG = "FragmentSignIn";
    private Button social_fb_login;
    private String mPage = "mobile";
    private boolean isOTP = false;
    List<SmcDatum> items = new ArrayList<>();
    private AppCompatTextView smartCardNumber;
    private CountDownTimer countDownTimer;
    String strValue = "";
    long Mmin, Ssec;

    private View.OnClickListener mSignInContinueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mobileNumber.getText().toString().isEmpty()) {
                mobileValid.setText("Mobile number field should not be empty");
                mobileValid.setVisibility(View.VISIBLE);
                return;
            }
            if(mobileNumber.getText().toString().length() != 10){
                mobileValid.setText(R.string.smart_card_alert);
                mobileValid.setVisibility(View.VISIBLE);
                return;
            }
            FirebaseAnalytics.getInstance().userSignUpStarted();
            showAndProceedStep1SignInRequest();
        }
    };


    private View.OnClickListener mStep3ClickListenerChangeNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Attach and use this to Redirect and change his number similar to start step 1.
            if (mBaseActivity != null) {
                mBaseActivity.removeFragment(FragmentSignUp.this);
                mBaseActivity.pushFragment(FragmentSignUp.newInstance(getArguments()));
            }
        }
    };


    private SMCClick smartCardnumberClickListener = new SMCClick() {
        @Override
        public void onClick(int position) {
            smartCardNumber.setText(items.get(position).getSmc());
            subscriberName.setText(items.get(position).getName());
            smartCardNumberSpinner.setVisibility(View.GONE);
            smcSpinerImg.setTag("Close");
            smcSpinerImg.setImageResource(R.drawable.ic_down);
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
    private ImageView backNavigation;
    //    private FrameLayout mFrameLayout;
    Spinner country, gender, age;
    String countrySelected, genderSelected, ageSelected;
    ArrayList<Countries> countryList;
    ArrayList<String> genderLists;
    ArrayList<String> ageLists;

    private Spinner country_code_spinner;
    // private TextView country_code_text;
//    private TextView smartCardNumber;
//private LoginButton loginButton;
    private FragmentSignIn fragmentSignIn;
    private String newSMCRequest = "false";
    //private CallbackManager callbackManager;


    public static FragmentSignUp newInstance(Bundle args) {
        FragmentSignUp fragmentOTPVerification = new FragmentSignUp();
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
        // callbackManager = CallbackManager.Factory.create();
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
//        mFrameLayout = new FrameLayout(mContext);
        rootView = inflater.inflate(R.layout.signup_paas, container, false);
        readBundleValues(getArguments());
        source = null;
        Bundle bundle = getArguments();
        if (getArguments() != null && getArguments().containsKey(Analytics.PROPERTY_SOURCE)) {
            source = getArguments().getString(Analytics.PROPERTY_SOURCE);
        }
        if (bundle.containsKey("full_name") && !bundle.getString("full_name").isEmpty()) {
            name = bundle.getString("full_name");
        }
        if (bundle.containsKey("smart_card_number") && !bundle.getString("smart_card_number").isEmpty()) {
            mSmartCardNumber = bundle.getString("smart_card_number");
        }
        if (bundle.containsKey("mobile_number") && !bundle.getString("mobile_number").isEmpty()) {
            mobileNumberText = bundle.getString("mobile_number");
        }
        if (bundle.containsKey("otp") && !bundle.getString("otp").isEmpty()) {
            enteredOTP = bundle.getString("otp");
        }
        if (bundle.containsKey("new_mobile") && bundle.getString("new_mobile") != null && !bundle.getString("new_mobile").isEmpty()) {
            newMobileNumberText = bundle.getString("new_mobile");
        }
        if (bundle.containsKey("new_otp") && bundle.getString("new_otp") != null && !bundle.getString("new_otp").isEmpty()) {
            newOTP = bundle.getString("new_otp");
        }
        if (bundle.containsKey("newSMCRequest") && bundle.getString("newSMCRequest") != null && !bundle.getString("newSMCRequest").isEmpty()) {
            newSMCRequest = bundle.getString("newSMCRequest");
        }
        sourceDetails = null;
        if (getArguments() != null && getArguments().containsKey(Analytics.PROPERTY_SOURCE_DETAILS)) {
            sourceDetails = getArguments().getString(Analytics.PROPERTY_SOURCE_DETAILS);
        }
        CleverTap.eventRegistrationPageViewed(source, sourceDetails);
        initComponent();

        String serverClientId = mContext.getResources().getString(R.string.server_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(serverClientId)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        fragmentSignIn = new FragmentSignIn();
        return rootView;
    }

    private void initComponent() {
        mMobileNoEditText = (EditText) rootView.findViewById(R.id.mobileSignUp);
       /* nameField = (EditText) rootView.findViewById(R.id.nameSignUp);
        emailField = (EditText) rootView.findViewById(R.id.emailIDSignUp);
*/
        mobileNumber = (EditText) rootView.findViewById(R.id.mobileNumberSignUp); // nameFiled
        newMobileNumberSignUp = (EditText) rootView.findViewById(R.id.new_mobileNumberSignUp); // nameFiled
        subscriberName = (EditText) rootView.findViewById(R.id.subscriberName);      // emailField
        cnfPwdField = (EditText) rootView.findViewById(R.id.cnfpwdSignUp);
        passwordField = (EditText) rootView.findViewById(R.id.pwdSignUp);
        country = (Spinner) rootView.findViewById(R.id.countrySignUp);
        gender = (Spinner) rootView.findViewById(R.id.genderSignUp);
        age = (Spinner) rootView.findViewById(R.id.ageSignUp);
        mRegisterButton = (Button) rootView.findViewById(R.id.registerSignUp);
        smcSpinerImg = rootView.findViewById(R.id.smc_spinner_img);
        bottomText = rootView.findViewById(R.id.bottomText);
        smcSpinerImg.setTag("Close");
        smartCardNumberSpinner = rootView.findViewById(R.id.smc_cards_list);
        smartCardNumberSpinner.setVisibility(View.GONE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        smartCardNumberSpinner.setLayoutManager(layoutManager);
        smartCardRecyclerViewAdapter = new SmartCardRecyclerViewAdapter(items, smartCardnumberClickListener);
        smartCardNumberSpinner.setAdapter(smartCardRecyclerViewAdapter);

        //smartCardNumberSpinner=rootView.findViewById(R.id.smc_spinner);
        /*smartCardNumberSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smcSpinerImg.setImageResource(R.drawable.ic_next_arrow_icon);
                Toast.makeText(mContext, "spinner is clicked", Toast.LENGTH_SHORT).show();
                Context wrapper = new ContextThemeWrapper(getContext(), R.style.PopupMenuTheme);
                PopupMenu popup = new PopupMenu(wrapper, view);
                popup.getMenuInflater().inflate(R.menu.genres_menu, popup.getMenu());
                for (int i = 0; i < items.size(); i++) {
                    popup.getMenu().add(0, i, i, items.get(i));
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int menuItemID = menuItem.getItemId();
                        smartCardNumber.setText(items.get(menuItemID));
                        smcSpinerImg.setImageResource(R.drawable.ic_down);
                        return false;
                    }
                });
            }
        });*/

        TextView signInClick = (TextView) rootView.findViewById(R.id.signInText);
        mobileValid = (TextView) rootView.findViewById(R.id.mobileNumber_valid);
        subscriberValid = (TextView) rootView.findViewById(R.id.subscriber_valid);
        smartCardValid = (TextView) rootView.findViewById(R.id.smart_card);
        backNavigation = (ImageView) rootView.findViewById(R.id.back_navigation);
        mMobileNumberText = (TextView) rootView.findViewById(R.id.mobile_number_text);
        mSmartCardText = (TextView) rootView.findViewById(R.id.smart_card_number);
        mSubscriberNameText = (TextView) rootView.findViewById(R.id.subscriber_name);
        mSuggestionText = (TextView) rootView.findViewById(R.id.suggestion_text);
        mNewMobileNumberText = (TextView) rootView.findViewById(R.id.new_mobile_number_text);
        otpValid = (TextView) rootView.findViewById(R.id.otp_validation_alert);
        mOtpText = (TextView) rootView.findViewById(R.id.otp_text);
        signUpText = (TextView) rootView.findViewById(R.id.signUptitle);
        otpEditText = (EditText) rootView.findViewById(R.id.otp_edittext);
        setEditTextMaxLength(Integer.parseInt(PrefUtils.getInstance().getOTPLength()));
        String termsAndPolicytext = getResources().getString(R.string.by_signing_in_you_agree_to_client_s_t_c_and_privacy_policy);
        Spannable span = new SpannableString(termsAndPolicytext);
        ClickableSpan clickableSpanTerms = new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent tnc = new Intent(mContext, LiveScoreWebView.class);
                CleverTap.eventPageViewed(CleverTap.PAGE_TERMS_AND_CONDITIONS);
                AppsFlyerTracker.eventBrowseHelp();
                tnc.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.terms));
                tnc.putExtra("url", APIConstants.getFAQURL() + APIConstants.TNC_URL);
                if(!TextUtils.isEmpty(PrefUtils.getInstance().getTncUrl())){
                    tnc.putExtra("url", PrefUtils.getInstance().getTncUrl());
                }else{
                    tnc.putExtra("url", APIConstants.getFAQURL() + APIConstants.TNC_URL);
                }
                mContext.startActivity(tnc);
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        ClickableSpan clickableSpanPrivacy = new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent ppc = new Intent(mContext, LiveScoreWebView.class);
                CleverTap.eventPageViewed(CleverTap.PAGE_PRIVACY_POLICY);
                AppsFlyerTracker.eventBrowseHelp();
                ppc.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.privacy_policy));
                if(!TextUtils.isEmpty(PrefUtils.getInstance().getPrivacy_policy_url())){
                    ppc.putExtra("url", PrefUtils.getInstance().getPrivacy_policy_url());
                }else{
                    ppc.putExtra("url", APIConstants.getFAQURL() + APIConstants.PRIVACY_POLICY_URL);
                }
                mContext.startActivity(ppc);
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        if (termsAndPolicytext.contains(getResources().getString(R.string.terms))) {
            span.setSpan(clickableSpanTerms, span.toString().indexOf(getResources().getString(R.string.terms)), termsAndPolicytext.indexOf(getResources().getString(R.string.terms)) + String.valueOf(getResources().getString(R.string.terms)).length(), 0);
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.theme_app_color)), span.toString().indexOf(getResources().getString(R.string.terms)), termsAndPolicytext.indexOf("Terms") + String.valueOf(getResources().getString(R.string.terms)).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
           // span.setSpan(clickableSpanTerms, span.toString().indexOf(getResources().getString(R.string.terms)), termsAndPolicytext.indexOf("Terms") + String.valueOf(getResources().getString(R.string.terms)).length(), 0);
        }

        if (termsAndPolicytext.contains(getResources().getString(R.string.privacypolicy))) {
            span.setSpan(clickableSpanPrivacy, span.toString().indexOf(getResources().getString(R.string.privacypolicy)), termsAndPolicytext.indexOf(getResources().getString(R.string.privacypolicy)) + String.valueOf(getResources().getString(R.string.privacypolicy)).length(), 0);
            span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.theme_app_color)), span.toString().indexOf(getResources().getString(R.string.privacypolicy)), termsAndPolicytext.indexOf("Privacy") + String.valueOf(getResources().getString(R.string.privacypolicy)).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        bottomText.setText(span);
        bottomText.setMovementMethod(LinkMovementMethod.getInstance());
        otpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                otpValid.setVisibility(View.GONE);
                if (otpEditText.isFocused()) {
                    otpValid.setVisibility(View.GONE);
                }
                if (!otpEditText.getText().toString().isEmpty()) {
                    if(otpEditText.getText().toString().length()== Integer.parseInt(PrefUtils.getInstance().getOTPLength())){
                        otpValidationButton.setBackgroundResource(R.drawable.rounded_corner_button_white);

                    }else{
                        otpValidationButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                    }
                } else {
                    otpValidationButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        otpValidationButton = (Button) rootView.findViewById(R.id.proceed_btn);
        resendOTPButton = rootView.findViewById(R.id.resend_otp_button);
        newMobileNumberValid = rootView.findViewById(R.id.new_mobileNumber_valid);
        otpInputLayout = rootView.findViewById(R.id.otp_input_layout);
        rl_space_root = rootView.findViewById(R.id.rl_space_root);
        updateHorizontalSpacing();


        mobileNumber.requestFocus();

        otpValidationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBaseActivity != null) {
                    if(mPage.equalsIgnoreCase("new_otp"))
                        newOTP = otpEditText.getText().toString();
                    else
                        enteredOTP = otpEditText.getText().toString();
                    if(otpEditText.getText().toString().isEmpty()){
                        otpValid.setText("Please Enter OTP");
                        otpValid.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (!isValidOTP(otpEditText.getText().toString())) {
                        otpValid.setText(R.string.Invalid_OTP);
                        otpValid.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (!isValidOTP(otpEditText.getText().toString())) {
                        otpValid.setVisibility(View.VISIBLE);
                        return;
                    }
                    if(mPage.equalsIgnoreCase("otp"))
                        signupOTPRequest(mobileNumber.getText().toString(), otpEditText.getText().toString());
                    else
                        smcSignupOTPRequest(mobileNumber.getText().toString(), otpEditText.getText().toString());
                  /*  Bundle args = new Bundle();
                    args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE,mIsLoginDuringBrowse);
                    args.putBoolean("updateMobileNumber",true);
                    mBaseActivity.pushFragment(FragmentCreatePassword.newInstance(args));*/
                }
            }
        });


        backNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackClicked();
            }
        });
        country_code_spinner = rootView.findViewById(R.id.country_code_spinner);
        /* country_code_text = rootView.findViewById(R.id.country_code_text);*/
        smartCardFieldLayout = rootView.findViewById(R.id.smc_feild_layout);
        smartCardFieldLayout.setOnClickListener(v -> {
            if (smcSpinerImg.getTag().equals("Close")) {
                smcSpinerImg.setImageResource(R.drawable.ic_next_arrow_icon);
                smcSpinerImg.getLayoutParams().height = 25;
                smcSpinerImg.getLayoutParams().width = 25;
                smcSpinerImg.setTag("Open");
                smartCardNumberSpinner.setVisibility(View.VISIBLE);
            } else {
                smcSpinerImg.getLayoutParams().height = 30;
                smcSpinerImg.getLayoutParams().width = 30;
                smcSpinerImg.setImageResource(R.drawable.ic_down);
                smcSpinerImg.setTag("Close");
                smartCardNumberSpinner.setVisibility(View.GONE);
            }
        });
        smartCardFeild = rootView.findViewById(R.id.smc_feild);
        smartCardNumber = rootView.findViewById(R.id.smartCardNumber);  //smartCardNumber
        // smartCardNumber.setText(items.get(0));
        subscriberName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                subscriberValid.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /*smartCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
             smartCardValid.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/
        mobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mobileValid.setVisibility(View.GONE);
                if (mobileNumber.isFocused()) {
                    mobileValid.setVisibility(View.GONE);
                }
                if (!mobileNumber.getText().toString().isEmpty()) {
                    if(mobileNumber.length()== 10){
                        mRegisterButton.setBackgroundResource(R.drawable.rounded_corner_button_white);

                    }else{
                        mRegisterButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                    }
                } else {
                    mRegisterButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // ImageButton signInGoogle=rootView.findViewById(R.id.sign_in_button1);
      /*  signInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button1:
                        Googlesign();
                        break;
                }
            }
        });
        */
        social_fb_login = rootView.findViewById(R.id.facebook_icon_sign_up_social);
       /* loginButton = (LoginButton)rootView.findViewById(R.id.login_button);
//        loginButton.setReadPermissions(Arrays.asList("basic_info", "email", "user_likes", "user_status"));
        loginButton.setReadPermissions(Arrays.asList("email","public_profile"));*/
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


        String[] genderRange = PrefUtils.getInstance().getUserGenderRange().split(",");
        List<String> genderLists = new ArrayList<String>(Arrays.asList(genderRange));
        genderLists.add(0, "Select Gender");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, genderLists);
        //genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(genderAdapter);

        String jsonFileString = getJsonFromAssets(getApplicationContext(), "country.json");
        Log.i("data", jsonFileString);
        Gson gson = new Gson();
        Type listUserType = new TypeToken<ArrayList<Countries>>() {
        }.getType();
        ArrayList<Countries> countriesList = gson.fromJson(jsonFileString, listUserType);
        countriesList.add(0, new Countries("Select Country", "0", "0"));
        String[] countries = new String[countriesList.size()];
        String[] countriesForCode = new String[countriesList.size() - 1];
        for (int i = 0; i < countriesList.size(); i++) {
            if (i > 0) {
                countriesForCode[i - 1] = countriesList.get(i).name;
            }
            countries[i] = countriesList.get(i).name;
        }
        //  ArrayAdapter countryAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, countries);
        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // country.setAdapter(countryAdapter);
//        final List<Countries> countriesList1 = countriesList;
//        countriesList1.remove(0);
        Typeface amazonEmberBold = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
        mRegisterButton.setOnClickListener(mSignInContinueClickListener);
        mRegisterButton.setTypeface(amazonEmberBold);
        otpValidationButton.setTypeface(amazonEmberBold);
        resendOTPButton.setTypeface(amazonEmberBold);
        //For country Code
        // ArrayAdapter countryAdapterForCode = new ArrayAdapter<String>(mContext, R.layout.spinner_item, countriesForCode);
        //   country_code_spinner.setAdapter(countryAdapterForCode);
    /*    country_code_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                country_code_spinner.performClick();
            }
        });*/
/*
        country_code_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                country_code_text.setText(countriesList1.get(position).dial_code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
*/
        setData();
        resendOTPButton.setEnabled(false);
        NumberFormat f = new DecimalFormat("00");
        countDownTimer = new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Used for formatting digit to be in 2 digits only
                //     NumberFormat f = new DecimalFormat("00");

                try {
                    Mmin = (millisUntilFinished / 1000) / 60;
                    Ssec = (millisUntilFinished / 1000) % 60;

                    strValue = f.format(Mmin) + ":" + f.format(Ssec);

                    Log.d(TAG, "onTick strValue: Mmin " + Mmin + " sec :" + Ssec);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                resendOTPButton.setText("Resend OTP in " + strValue + " sec");
                resendOTPButton.setEnabled(false);
            }
            // When the task is over it will print 00:00:00 there
            public void onFinish() {
                resendOTPButton.setText("Resend OTP");
                resendOTPButton.setEnabled(true);
            }
        };
        resendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   AlertDialogUtil.showToastNotification("Request Sent");
               // requestResetPassword(mobileNumber);
                signupOTPRequest( mobileNumber.getText().toString(),"");
            }
        });
    }

    public void setData() {
        if (mobileNumberText != null)
            mobileNumber.setText(mobileNumberText);
        if (name != null)
            subscriberName.setText(name);
        if (mSmartCardNumber != null) {
            smartCardNumber.setText(mSmartCardNumber);
            if(newSMCRequest != null && !newSMCRequest.isEmpty() && newSMCRequest.equalsIgnoreCase("true")) {
                showChangeMobileNumber();
            } else {
                fetchSMCNumbers();
                showDetailsFields();
            }
        }
    }

    private void fbSignin() {
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
    BottomSheetDialog bottomSheetDialog;

    private void showBottomSheetDialog(String text) {
        bottomSheetDialog = new BottomSheetDialog(mContext, R.style.NoBackgroundDialogTheme);
        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.register_bottom_sheet_dialog, null);
       /* RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(Gravity.CENTER);
      //  params.setMargins(120, 0, 120, 40);
        view.setLayoutParams(params);*/
        bottomSheetDialog.setContentView(view);
        ImageView closeIcon=view.findViewById(R.id.back_navigation);
        AppCompatButton continueSMCRegister=view.findViewById(R.id.second_time_smc_register_button);
        TextView secondText = view.findViewById(R.id.second_time_register_suggestion);
        TextView skipToLogin=view.findViewById(R.id.skip_to_login);
        secondText.setText(text);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
        skipToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //The implementation to go to login screen goes here
                bottomSheetDialog.dismiss();
                Bundle args = new Bundle();
                mBaseActivity.pushFragment(FragmentSignIn.newInstance(args));
            }
        });
        continueSMCRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //The implementation to go to OTP screen after clicking on continue goes here
                bottomSheetDialog.dismiss();
                newSMCRequest = "true";
                signupOTPRequest( mobileNumber.getText().toString(),"");
            }
        });
        Log.d(TAG, "showBottomSheetDialog: "+ (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE));
        if(DeviceUtils.isTablet(mContext)){
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                    BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setPeekHeight(0); // Remove this line to hide a dark background if you manually hide the dialog.
                }
            });
        }
        bottomSheetDialog.show();
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

        if (v == null) return;
       /* if (v.getId() == R.id.mobileSignUp) {
            validateAndUpdateUIForMobileNo();
            return;
        }
        if (v.getId() == R.id.emailIDSignUp) {
            validateAndUpdateUIForEmailId();
            return;
        }*/

        if (v.getId() == R.id.mobileNumberSignUp) {
            validateAndUpdateUIForMobileNo();
            return;
        }
        if (v.getId() == R.id.subscriberName) {
            validateAndUpdateUIForSubscriberName();
            return;
        }
        if (v.getId() == R.id.smartCardNumber) {
            validateAndUpdateUIForSmartCardNumber();
            return;
        }
    }

    private boolean isValidSubscriberName(String userName) {
        if (userName == null || TextUtils.isEmpty(userName)) {
            return false;
        }

        if (userName.length() > 0) {
           /* if(userName.matches("^[a-zA-Z\\s]{0,22}$")){
            return true;
            }*/
          /*  int lengthFromDot = 0;
            if (userName.length() >= 0 && userName.substring(userName.indexOf(".")) != null) {
                lengthFromDot = userName.substring(userName.indexOf(".")).length();
                LoggerD.debugDownload("lengthFromDot- " + lengthFromDot + " emailId.substring(emailId.indexOf(\".\"))- " + userName.substring(userName.indexOf(".")));
            }
            if (userName.contains("@") && userName.contains(".") && !userName.contains(" ") && lengthFromDot > 2) {

            }*/
        }
        return true;
    }

    private boolean isValidSmartCardNumber() {
        mSmartCardNumber = smartCardNumber.getText().toString();

        boolean isValidSmartCardNumber = false;

        // mImageViewMobileNoTickMark.setImageResource(0);
        if (mSmartCardNumber == null || mSmartCardNumber.isEmpty()) {
            return false;
        }
        try {
            long num = Long.parseLong(mSmartCardNumber);
            LoggerD.debugOTP(num + " is a number");
            if (mSmartCardNumber.length() > 0) {
              /*  mSubscriberNameText.setVisibility(View.GONE);
                mMobileNumberText.setVisibility(View.GONE);
                mSmartCardText.setVisibility(View.GONE);
                mRegisterText.setVisibility(View.GONE);
                mSuggestionText.setVisibility(View.GONE);
                mobileNumber.setVisibility(View.GONE);
                smartCardNumber.setVisibility(View.GONE);
                smartCardFieldLayout.setVisibility(View.GONE);
                subscriberName.setVisibility(View.GONE);
                mOtpSuggestionText.setVisibility(View.VISIBLE);
                mOtpText.setVisibility(View.VISIBLE);
                otpEditText.setVisibility(View.VISIBLE);
                otpValidationButton.setVisibility(View.VISIBLE);
                registerButton.setVisibility(View.VISIBLE);
                mRegisterButton.setVisibility(View.GONE);
                signUpText.setVisibility(View.VISIBLE);*/
                //otpInputLayout.setVisibility(View.VISIBLE);
                // showMobNoWrongTickmark = true;
                isValidSmartCardNumber = true;
            }
        } catch (NumberFormatException e) {
            LoggerD.debugOTP(mSmartCardNumber + "is not a number");
        }
        return isValidSmartCardNumber;
    }

    private void validateAndUpdateUIForMobileNo() {
        /*mMobileNo = mMobileNoEditText.getText().toString();*/
        mMobileNo = mobileNumber.getText().toString();
        mSubscriberNameText.setVisibility(View.VISIBLE);
        mMobileNumberText.setVisibility(View.VISIBLE);
        mSmartCardText.setVisibility(View.VISIBLE);
        mobileNumber.setVisibility(View.VISIBLE);
        smartCardFieldLayout.setVisibility(View.VISIBLE);
        smartCardNumber.setVisibility(View.VISIBLE);
        subscriberName.setVisibility(View.VISIBLE);
        mobileValid.setVisibility(View.VISIBLE);
        subscriberValid.setVisibility(View.VISIBLE);
        smartCardValid.setVisibility(View.VISIBLE);

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
        } else {
            Toast.makeText(mContext, "Invalid Mobile number", Toast.LENGTH_LONG).show();
        }

    }
    /*private void validateAndUpdateUIForEmailId() {
        mEmailID = emailField.getText().toString();
        boolean isEmailValid = false;
        mEmailID = mEmailID.trim();
        // mImageViewEmailIdTickMark.setImageResource(0);

        if (mEmailID != null && mEmailID.length() == 0) {
           *//* showEmailIdTickMark = false;
            mImageViewEmailIdTickMark.setVisibility(View.GONE);
            mImageViewEmailIdTickMark.setImageResource(0);*//*
            // return;
        }

       *//* if (showEmailIdTickMark && emailSupported) {
            mImageViewEmailIdTickMark.setVisibility(View.VISIBLE);
            mImageViewEmailIdTickMark.setImageResource(R.drawable.otp_cross_icon);
        }*//*
        if (isValidEmailID(mEmailID) ) {
            *//*showEmailIdTickMark = true;
            mImageViewEmailIdTickMark.setVisibility(View.VISIBLE);
            mImageViewEmailIdTickMark.setImageResource(R.drawable.otp_correct_icon);*//*
        }else{
            Toast.makeText(mContext,"Invalid Email ID",Toast.LENGTH_LONG).show();
        }

    }*/


    private void validateAndUpdateUIForSubscriberName() {
        mSubscribername = subscriberName.getText().toString();
        boolean isEmailValid = false;
        mSubscribername = mSubscribername.trim();
        // mImageViewEmailIdTickMark.setImageResource(0);

        if (mSubscribername != null && mSubscribername.length() == 0) {
            subscriberValid.setVisibility(View.VISIBLE);
        }


        if (isValidSubscriberName(mSubscribername)) {

        }

    }

    private void validateAndUpdateUIForSmartCardNumber() {
        mSmartCardNumber = smartCardNumber.getText().toString();
        boolean isEmailValid = false;
        mSmartCardNumber = mSmartCardNumber.trim();
        // mImageViewEmailIdTickMark.setImageResource(0);

        if (mSmartCardNumber != null && mSmartCardNumber.length() == 0) {

        }

        if (isValidSubscriberName(mSubscribername)) {
        } else {
            Toast.makeText(mContext, "Invalid Email ID", Toast.LENGTH_LONG).show();
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

    private void fetchSMCNumbers() {
        RequestSMCNumbers.Params params = new RequestSMCNumbers.Params(mobileNumber.getText().toString());

        final RequestSMCNumbers requestSMCNumbers = new RequestSMCNumbers(params, new APICallback<SMCLIstResponse>() {
            @Override
            public void onResponse(APIResponse<SMCLIstResponse> response) {
                if (response != null && response.body() != null && response.body().getSmcs() != null && response.body().getSmcs().size() > 0) {
                    items.clear();
                 /*   for (String smd :
                            response.body().getSmcs()) {
                        items.add(new SMCObject());
                    }*/
                    items = response.body().getSmcData();
                    smartCardNumber.setText(items.get(0).getSmc());
                    subscriberName.setText(items.get(0).getName());
                    //smartCardRecyclerViewAdapter.notifyDataSetChanged();
                    smartCardRecyclerViewAdapter = new SmartCardRecyclerViewAdapter(items, smartCardnumberClickListener);
                    smartCardNumberSpinner.setAdapter(smartCardRecyclerViewAdapter);
                    smcSpinerImg.setVisibility(View.VISIBLE);
                } else {
                    smcSpinerImg.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugOTP("Failed: " + t);
                dismissProgressBar();
                smcSpinerImg.setVisibility(View.GONE);
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    Analytics.mixpanelEventUnableToFetchOffers(mContext.getString(R.string.network_error), APIConstants.NOT_AVAILABLE);
                    showDeviceAuthenticationFailed(mContext.getString(R.string.network_error));
                    return;
                }
                Analytics.mixpanelEventUnableToFetchOffers(t == null || t.getMessage() == null ? APIConstants.NOT_AVAILABLE : t.getMessage(), APIConstants.NOT_AVAILABLE);
                showDeviceAuthenticationFailed(PrefUtils.getInstance().getPrefMessageFailedToFetchOffers());
            }
        });

        APIService.getInstance().execute(requestSMCNumbers);
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


    private void signupOTPRequest(String mMobileNo, String otp) {

        showProgressBar();
      /*  CleverTap.eventRegistrationInitiated(mEmailID.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);

        LoggerD.debugOTP("emailId- " + mEmailID);*/
        CleverTap.eventRegistrationInitiated(mSubscribername.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);

        LoggerD.debugOTP("emailId- " + mSubscribername);

        SignUpOTPRequest.Params msisdnParams = new SignUpOTPRequest.Params(mMobileNo, otp, newSMCRequest);

        SignUpOTPRequest login = new SignUpOTPRequest(msisdnParams,
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
                            /*      CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, "Invalid API Response");*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, "Invalid API Response");
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

                        if (response.body().code == 200
                                || response.body().code == 201) {
                            if (response.body().status.equalsIgnoreCase("SUCCESS_NEED_CONFIRMATION")) {
                                showOTPFields("otp");
                            }
                            else {
                             /*   Bundle args = new Bundle();
                                args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                                args.putBoolean("isRegister", true);
                                args.putString("mobile_number", mobileNumber.getText().toString());
                                args.putString("otp", otpEditText.getText().toString());
                                args.putString("full_name", subscriberName.getText().toString());
                                args.putString("smart_card_number", smartCardNumber.getText().toString());
                                mBaseActivity.pushFragment(FragmentCreatePassword.newInstance(args));*/
                                fetchSMCNumbers();
                                showDetailsFields();
                            }
                            return;
                        }
                        if(response.body().code == 205) {
                            if (response.body().status.equalsIgnoreCase("SUCCESS_NEED_NEW_MOBILE")) {
                                if(response.body().message != null) {
                                    showBottomSheetDialog(response.body().message);
                                    return;
                                }
                            }
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
                            /*CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);
                            mixpanelOTPLoginFailed(response.body().status, String.valueOf(response.body().code));
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, response.body().message);
                            }
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            // login failed
                            /*  CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);

                            FirebaseAnalytics.getInstance().userSignUpCompleted();
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        }

                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 217) {
                            isOtpLogin = true;
                            FirebaseAnalytics.getInstance().userSignUpCompleted();
                            return;
                        }


                        if (!TextUtils.isEmpty(response.body().message)) {
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, response.body().message);
                            }
                            /* CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);
//                            AlertDialogUtil.showToastNotification(response.body().message);
                                AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                                    @Override
                                    public void onDialogClick(String buttonText) {

                                    }
                                });

                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        dismissProgressBar();
                        LoggerD.debugOTP("Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, mContext.getString(R.string.network_error));
                            }
                           /* CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailedWithOutExit(null);
                    }
                });
        APIService.getInstance().execute(login);
    }

    private void smcSignupOTPRequest(String mMobileNo, String otp) {

        showProgressBar();
      /*  CleverTap.eventRegistrationInitiated(mEmailID.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);

        LoggerD.debugOTP("emailId- " + mEmailID);*/
        CleverTap.eventRegistrationInitiated(mSubscribername.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);

        LoggerD.debugOTP("emailId- " + mSubscribername);
        String temp = "false";
        if(!otp.isEmpty())
            temp = "true";
        SignUpOTPRequest.Params msisdnParams = new SignUpOTPRequest.Params(mMobileNo, otp, temp, newMobileNumberSignUp.getText().toString());

        SignUpOTPRequest login = new SignUpOTPRequest(msisdnParams,
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
                            /*      CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, "Invalid API Response");*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, "Invalid API Response");
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

                        if (response.body().code == 200
                                || response.body().code == 201) {
                            if (response.body().status.equalsIgnoreCase("SUCCESS_NEED_CONFIRMATION")) {
                                showOTPFields("new_otp");
                            }
                            else {
                             /*   Bundle args = new Bundle();
                                args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                                args.putBoolean("isRegister", true);
                                args.putString("mobile_number", mobileNumber.getText().toString());
                                args.putString("otp", otpEditText.getText().toString());
                                args.putString("full_name", subscriberName.getText().toString());
                                args.putString("smart_card_number", smartCardNumber.getText().toString());
                                mBaseActivity.pushFragment(FragmentCreatePassword.newInstance(args));*/
                               /* fetchSMCNumbers();
                                showDetailsFields();*/
                                Bundle args = new Bundle();
                                args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
                                args.putBoolean("isRegister", true);
                                args.putString("mobile_number", mobileNumber.getText().toString());
                                args.putString("new_mobile", newMobileNumberSignUp.getText().toString());
                                args.putString("otp", enteredOTP);
                                args.putString("full_name", subscriberName.getText().toString());
                                args.putString("smart_card_number", smartCardNumber.getText().toString());
                                args.putString("new_otp", newOTP);
                                args.putString("newSMCRequest", newSMCRequest);
                                mBaseActivity.pushFragment(FragmentCreatePassword.newInstance(args));
                            }
                            return;
                        }
                        if(response.body().code == 205) {
                            if (response.body().status.equalsIgnoreCase("SUCCESS_NEED_NEW_MOBILE")) {
                                if(response.body().message != null) {
                                    showBottomSheetDialog(response.body().message);
                                    return;
                                }
                            }
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
                            /*CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);
                            mixpanelOTPLoginFailed(response.body().status, String.valueOf(response.body().code));
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, response.body().message);
                            }
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            // login failed
                            /*  CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);

                            FirebaseAnalytics.getInstance().userSignUpCompleted();
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        }

                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 217) {
                            isOtpLogin = true;
                            FirebaseAnalytics.getInstance().userSignUpCompleted();
                            return;
                        }


                        if (!TextUtils.isEmpty(response.body().message)) {
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, response.body().message);
                            }
                            /* CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);
//                            AlertDialogUtil.showToastNotification(response.body().message);
                            AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                                @Override
                                public void onDialogClick(String buttonText) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        dismissProgressBar();
                        LoggerD.debugOTP("Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, mContext.getString(R.string.network_error));
                            }
                           /* CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailedWithOutExit(null);
                    }
                });
        APIService.getInstance().execute(login);
    }

    private void makeUserSignUpRequest() {

        showProgressBar();
      /*  CleverTap.eventRegistrationInitiated(mEmailID.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);

        LoggerD.debugOTP("emailId- " + mEmailID);*/
        CleverTap.eventRegistrationInitiated(mSubscribername.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);

        LoggerD.debugOTP("emailId- " + mSubscribername);
        SignUpEncryptedShreyas.Params msisdnParams = new SignUpEncryptedShreyas.Params(mMobileNo, mSubscribername, name, mSmartCardNumber, cnfPwd, countrySelected, genderSelected, ageSelected);

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
                            /*      CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, "Invalid API Response");*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, "Invalid API Response");
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
                            /*CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);
                            mixpanelOTPLoginFailed(response.body().status, String.valueOf(response.body().code));
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, response.body().message);
                            }
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            // login failed
                            /*  CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);

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
                            if (!TextUtils.isEmpty(response.body().serviceName)) {
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }
                            myplexAPI.clearCache(APIConstants.BASE_URL);
                            PropertiesHandler.clearCategoryScreenFilter();
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.SUCCESS, null);
                            }
                            CleverTap.eventRegistrationCompleted(response.body().email, response.body().mobile, Analytics.NO);
                            if (!TextUtils.isEmpty(response.body().mobile)) {
                                PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
                            } else if (!TextUtils.isEmpty(response.body().email)) {
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
                                if (response.body().code == 201) {
                                    AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                                } else if (response.body().code == 200) {
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
                            if (response.body().code == 201) {
                                AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                            } else if (response.body().code == 200) {
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
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, response.body().message);
                            }
                            /* CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, response.body().message);
                            AlertDialogUtil.showToastNotification(response.body().message);
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        dismissProgressBar();
                        LoggerD.debugOTP("Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, mContext.getString(R.string.network_error));
                            }
                           /* CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));*/
                            CleverTap.eventRegistrationFailed(mSubscribername, mMobileNo, mContext.getString(R.string.network_error));
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
       /* mEmailID = emailField.getText().toString();
        name = nameField.getText().toString();
        pwd = passwordField.getText().toString();*/
        mSubscribername = subscriberName.getText().toString();
        name = mobileNumber.getText().toString();
        mSmartCardNumber = smartCardNumber.getText().toString();
        cnfPwd = cnfPwdField.getText().toString();
        //mEmailID = emailField.getText().toString();
        mMobileNo = mobileNumber.getText().toString() + mMobileNo;
        mMobileNo = mMobileNo.replace("+", "");

        boolean isValidPhoneNumber = false;

        if (TextUtils.isEmpty(mobileNumber.getText().toString())) {
            //  AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            mobileValid.setVisibility(View.VISIBLE);
            return;
        }
        try {
            long num = Long.parseLong(mMobileNo);
            LoggerD.debugOTP(num + " is a number");
            if (mMobileNo.length() == 10 && mMobileNo.substring(0,1).matches("[6-9]")) {
                    mMobileNumberText.setVisibility(View.VISIBLE);
                    mobileNumber.setVisibility(View.VISIBLE);


                isValidPhoneNumber = true;
            } else {
                mobileValid.setVisibility(View.VISIBLE);
                return;
            }
        } catch (NumberFormatException e) {
            LoggerD.debugOTP(mMobileNo + "is not a number");
        }
      /*  if(TextUtils.isEmpty(country_code_text.getText().toString())){
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_country_code));
            return;
        }
        mMobileNo = country_code_text.getText().toString() + mMobileNo;
        mMobileNo = mMobileNo.replace("+","");
        mEmailID = mEmailID.trim();*/
      /*  if (smartCardFieldLayout.getVisibility() == View.GONE) {
          *//*  fetchSMCNumbers();
            showDetailsFields();*//*
            showOTPFields();
            return;
        }*/
        if(mPage.equalsIgnoreCase("change_mobile")) {
            if (TextUtils.isEmpty(newMobileNumberSignUp.getText().toString())) {
                //  AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
                newMobileNumberValid.setVisibility(View.VISIBLE);
                mMobileNumberText.setVisibility(View.GONE);
                mobileNumber.setVisibility(View.GONE);
                return;
            }
            long num = Long.parseLong(newMobileNumberSignUp.getText().toString());
            LoggerD.debugOTP(num + " is a number");
            if (newMobileNumberSignUp.getText().toString().length() == 10 && newMobileNumberSignUp.getText().toString().substring(0, 1).matches("[6-9]")) {
                mMobileNumberText.setVisibility(View.GONE);
                mobileNumber.setVisibility(View.GONE);
                newMobileNumberSignUp.setVisibility(View.VISIBLE);
                mNewMobileNumberText.setVisibility(View.VISIBLE);
                newMobileNumberValid.setVisibility(View.GONE);
            } else {
                mMobileNumberText.setVisibility(View.GONE);
                mobileNumber.setVisibility(View.GONE);
                newMobileNumberValid.setVisibility(View.VISIBLE);
                return;
            }
        }
        if(smartCardFieldLayout.getVisibility() == View.GONE) {
            if(!mPage.equalsIgnoreCase("change_mobile")) {
                newSMCRequest = "false";
                signupOTPRequest(mobileNumber.getText().toString(), "");
            } else {
                smcSignupOTPRequest(mobileNumber.getText().toString(), "");
            }
            return;
        }
        mSubscribername = mSubscribername.trim();
        if (!isValidPhoneNumber) {
            mMobileNoEditText.requestFocus();
            mobileValid.setVisibility(View.VISIBLE);
            smartCardValid.setVisibility(View.GONE);
            subscriberValid.setVisibility(View.GONE);
            // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            // CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_MOBILE_NO);
            return;
        }
     /*   if (!isValidEmailID(mEmailID) ) {
            mMobileNoEditText.clearFocus();
            emailField.requestFocus();
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
            //CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }*/
        if (!isValidSubscriberName(mSubscribername)) {
            mMobileNoEditText.clearFocus();
            subscriberName.requestFocus();
            mobileValid.setVisibility(View.GONE);
            subscriberValid.setVisibility(View.VISIBLE);
            smartCardValid.setVisibility(View.GONE);
            //CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }
        if (!isValidSmartCardNumber()) {
            mMobileNoEditText.clearFocus();
            subscriberName.clearFocus();
            mobileNumber.clearFocus();
//            smartCardNumber.requestFocus();
            mobileValid.setVisibility(View.GONE);
            subscriberValid.setVisibility(View.GONE);
            smartCardValid.setVisibility(View.VISIBLE);
            return;
        }


        /*if(!isValidName()){
            mMobileNoEditText.clearFocus();
            subscriberName.clearFocus();
            mobileNumber.requestFocus();
            AlertDialogUtil.showToastNotification("Please enter valid name");
            return;
        }*/
       /* if(!isValidPassword()){
            mMobileNoEditText.clearFocus();
            emailField.clearFocus();
            nameField.requestFocus();
            return;
        }*/

        /*if (country.getSelectedItemPosition() == 0) {
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

        }*/
        //Make login request and Start Otp reader and listen for otp
/*        PrefUtils.getInstance().setPrefTempMsisdn(mMobileNo);
        PrefUtils.getInstance().setPrefTempEMAILID(mEmailID);*/
        //hideSoftInputKeyBoard(mDropDownEmailIDs);
        //hideSoftInputKeyBoard(mOTPEditText);
        if(!newSMCRequest.equalsIgnoreCase("true")) {
            Bundle args = new Bundle();
            args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
            args.putBoolean("isRegister", true);
            args.putString("mobile_number", mobileNumber.getText().toString());
            args.putString("otp", enteredOTP);
            args.putString("full_name", subscriberName.getText().toString());
            args.putString("smart_card_number", smartCardNumber.getText().toString());
            mRegisterButton.setVisibility(View.GONE);
            mBaseActivity.pushFragment(FragmentCreatePassword.newInstance(args));
        }  else {
            showChangeMobileNumber();
           // smcSignupOTPRequest(mobileNumber.getText().toString(), "");
        }
        //signupOTPRequest(mobileNumber.getText().toString(), "");

    }

    /*private boolean isValidName() {
        if(TextUtils.isEmpty(name.trim())){
            return false;
        }
        return true;
    }*/
/*

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

    public void showDetailsFields() {
        mPage = "details";
        mSubscriberNameText.setVisibility(View.VISIBLE);
        mSmartCardText.setVisibility(View.VISIBLE);
        smartCardFieldLayout.setVisibility(View.VISIBLE);
        smartCardNumber.setVisibility(View.VISIBLE);
        mobileNumber.setVisibility(View.VISIBLE);
        mMobileNumberText.setVisibility(View.VISIBLE);
        mMobileNumberText.setText(getResources().getText(R.string.register_mobile_number));
        mSuggestionText.setText(getResources().getText(R.string.enter_otp_text));
        subscriberName.setVisibility(View.VISIBLE);
        mobileNumber.setEnabled(false);
        mRegisterButton.setVisibility(View.VISIBLE);
        otpEditText.setVisibility(View.GONE);
        otpValid.setVisibility(View.GONE);
        otpValidationButton.setVisibility(View.GONE);
        resendOTPButton.setVisibility(View.GONE);
        mOtpText.setVisibility(View.GONE);
        bottomText.setVisibility(View.GONE);
        if(countDownTimer!=null) {
            countDownTimer.cancel();
        }
    }

    public void showChangeMobileNumber(){
        mNewMobileNumberText.setVisibility(View.VISIBLE);
        mMobileNumberText.setVisibility(View.GONE);
        bottomText.setVisibility(View.GONE);
        newMobileNumberSignUp.setVisibility(View.VISIBLE);
        mobileNumber.setVisibility(View.GONE);
        mPage = "change_mobile";
        mSubscriberNameText.setVisibility(View.GONE);
        mSmartCardText.setVisibility(View.GONE);
        smartCardFieldLayout.setVisibility(View.GONE);
        smartCardNumber.setVisibility(View.GONE);
        mSuggestionText.setText(getResources().getText(R.string.otp_stp2_heading4));
        subscriberName.setVisibility(View.GONE);
        subscriberValid.setVisibility(View.GONE);
        smartCardValid.setVisibility(View.GONE);
        mSuggestionText.setVisibility(View.VISIBLE);
        mobileNumber.setEnabled(true);
        mobileNumber.requestFocus();
        // smartCardNumber.setText(items.get(0));

    }

    public void hideDetailsFields() {
        mPage = "";
        mSubscriberNameText.setVisibility(View.GONE);
        mSmartCardText.setVisibility(View.GONE);
        smartCardFieldLayout.setVisibility(View.GONE);
        smartCardNumber.setVisibility(View.GONE);
        mMobileNumberText.setText(getResources().getText(R.string.otp_stp1_heading3));
        mSuggestionText.setText(getResources().getText(R.string.register));
        subscriberName.setVisibility(View.GONE);
        subscriberValid.setVisibility(View.GONE);
        smartCardValid.setVisibility(View.GONE);
        mSuggestionText.setVisibility(View.VISIBLE);
        mobileNumber.setEnabled(true);
        bottomText.setVisibility(View.VISIBLE);
        mobileNumber.requestFocus();
        // smartCardNumber.setText(items.get(0));
        subscriberName.setText("");
    }

    public void showOTPFields(String mpage) {
        mPage = mpage;
        mSubscriberNameText.setVisibility(View.GONE);
        mSmartCardText.setVisibility(View.GONE);
        smartCardFieldLayout.setVisibility(View.GONE);
        smartCardNumber.setVisibility(View.GONE);
        subscriberName.setVisibility(View.GONE);
        subscriberValid.setVisibility(View.GONE);
        smartCardValid.setVisibility(View.GONE);
        mRegisterButton.setVisibility(View.GONE);
        mobileNumber.setVisibility(View.GONE);
        mobileValid.setVisibility(View.GONE);
        mMobileNumberText.setVisibility(View.GONE);
        bottomText.setVisibility(View.GONE);
        resendOTPButton.setVisibility(View.VISIBLE);
        countDownTimer.start();
        otpEditText.setVisibility(View.VISIBLE);
        otpValidationButton.setVisibility(View.VISIBLE);
        mOtpText.setVisibility(View.VISIBLE);
        otpEditText.setText("");
        mSuggestionText.setText(getResources().getText(R.string.Otp_received_suggestion));
        mNewMobileNumberText.setVisibility(View.GONE);
        newMobileNumberSignUp.setVisibility(View.GONE);
        newMobileNumberValid.setVisibility(View.GONE);
    }

    public void hideOTPFields() {
        mPage = "details";
      //  mSubscriberNameText.setVisibility(View.VISIBLE);
       // mSmartCardText.setVisibility(View.VISIBLE);
       // smartCardFieldLayout.setVisibility(View.VISIBLE);
       // smartCardNumber.setVisibility(View.VISIBLE);
        mSuggestionText.setText(getResources().getText(R.string.register));
       // subscriberName.setVisibility(View.VISIBLE);
        mobileNumber.setVisibility(View.VISIBLE);
        mMobileNumberText.setVisibility(View.VISIBLE);
        resendOTPButton.setVisibility(View.GONE);
        resendOTPButton.setEnabled(false);
        otpEditText.setVisibility(View.GONE);
        otpValid.setVisibility(View.GONE);
        otpValidationButton.setVisibility(View.GONE);
        mRegisterButton.setVisibility(View.VISIBLE);
        mOtpText.setVisibility(View.GONE);
        bottomText.setVisibility(View.VISIBLE);
        if(countDownTimer!=null) {
            countDownTimer.cancel();
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
        getActivity().onBackPressed();
        return false;
    }

    public boolean backHandle() {
        if (mPage.equalsIgnoreCase("details")) {
            hideDetailsFields();
            return false;
        } else if (mPage.equalsIgnoreCase("otp")) {
            hideOTPFields();
            return false;
        }  else if (mPage.equalsIgnoreCase("change_mobile")) {
            hideChangMobileNumberFields();
            showDetailsFields();
            return false;
        } else if (mPage.equalsIgnoreCase("new_otp")) {
            hideOTPFields();
            showChangeMobileNumber();
            return false;
        } else
            return true;
    }

    public void  hideChangMobileNumberFields(){
        newMobileNumberSignUp.setVisibility(View.GONE);
        newMobileNumberValid.setVisibility(View.GONE);
        mNewMobileNumberText.setVisibility(View.GONE);
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

    private void showSignInScreenFragment() {
        if (mBaseActivity != null) {
            mBaseActivity.removeFragment(this);
            Bundle args = new Bundle();
            args.putBoolean(FragmentSignIn.PARAM_LOGIN_DURING_BROWSE, mIsLoginDuringBrowse);
            mBaseActivity.pushFragment(FragmentSignIn.newInstance(args));
        }
    }

    private boolean isValidOTP(String enteredOTP) {
        if (enteredOTP == null || TextUtils.isEmpty(enteredOTP)) {
            return false;
        }
        if(!TextUtils.isEmpty(PrefUtils.getInstance().getOTPLength())) {
            final String requiredLength = PrefUtils.getInstance().getOTPLength();
            int length = Integer.parseInt(requiredLength);
            if (enteredOTP.length() > length && enteredOTP.length() < length) {
                otpValid.setVisibility(View.VISIBLE);
                return false;
            }
            if (enteredOTP.length() == length) {
                return true;
            }
        }
        return false;
    }


    private class SmartCardRecyclerViewAdapter extends RecyclerView.Adapter<SmartCardRecyclerViewAdapter.MyViewHolder> {
        List<SmcDatum> myListData = new ArrayList<>();
        SMCClick onClickListener;

        public SmartCardRecyclerViewAdapter(List<SmcDatum> list, SMCClick onClickListener) {
            myListData = list;
            this.onClickListener = onClickListener;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.smc_number_dropdown_item, parent, false);
            MyViewHolder viewHolder = new MyViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.textView.setText(myListData.get(position).getSmc());
            holder.textView.setTag(position);
            holder.textView.setOnClickListener(v -> {
                onClickListener.onClick((int)holder.textView.getTag());
            });
        }

        @Override
        public int getItemCount() {
            return myListData.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.textView = (TextView) itemView.findViewById(R.id.smc_card_number);
            }
        }
    }
    public void setEditTextMaxLength(int length) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        otpEditText.setFilters(filterArray);
    }

    interface SMCClick {
        public void onClick(int position);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateHorizontalSpacing();
    }

    int portraitWidth;
    RelativeLayout rl_space_root;
    private void updateHorizontalSpacing() {
        if(DeviceUtils.isTabletOrientationEnabled(mContext)){
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if(DeviceUtils.getScreenOrientation(mContext) != SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                if(portraitWidth <= 0) {
                    portraitWidth = rl_space_root.getLayoutParams().width;
                }
                rl_space_root.getLayoutParams().width =portraitWidth;
//                params.width = portraitWidth;
           }else {
                rl_space_root.getLayoutParams().width = (int)(0.45 * getResources().getDisplayMetrics().widthPixels);
//                params.width = (int)(0.45 * getResources().getDisplayMetrics().widthPixels);
            }
//            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
//            ll_space_root.setLayoutParams(params);
        }
    }

}
