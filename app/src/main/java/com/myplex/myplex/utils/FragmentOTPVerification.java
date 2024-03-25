package com.myplex.myplex.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
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
import com.myplex.api.request.content.AvailableLoginsProperties;
import com.myplex.api.request.user.MSISDNLoginV3;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.api.request.user.ProfileUpdateWithEmailIDRequest;
import com.myplex.model.AvailableLoginsPropertiesData;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.LoginProperties;
import com.myplex.model.MsisdnData;
import com.myplex.model.OfferResponseData;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;
import com.myplex.util.StringEscapeUtils;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;


/**
 * Created by Srikanth on 10/21/2014.
 */
public class FragmentOTPVerification extends BaseFragment implements View.OnFocusChangeListener, View.OnTouchListener, OtpReader.OTPListener {

    public static final String PARAM_MSISDN = "msisdn";
    public static final String PARAM_IS_EXISTING_USER = "is_existing_user_to_disable_mobile_number";
    public static final String PARAM_LOGIN_DURING_BROWSE = "during_browse";
    public static final String PARAM_UPDATE_EMAIL = "update_email";
    private View rootView;
    private TextView mTextViewHeading1;
    private TextView mTextViewHeading2;
    private TextView mTextViewHeading3;
    private TextView mTextViewChangeNumber;
    private EditText mMobileNoEditText;
    private EditText mOTPEditText;
    private AutoCompleteTextView mDropDownEmailIDs;
    private Button mButton1;
    private Button mButton2;
    private Context mContext;
    private OtpReader mOtpReader;

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
                mBaseActivity.removeFragment(FragmentOTPVerification.this);
                mBaseActivity.pushFragment(FragmentOTPVerification.newInstance(getArguments()));
            }
        }
    };

    private View.OnClickListener mStep3ClickListenerOTPManuallySubmit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOTPEditText != null) {
                hideSoftInputKeyBoard(mOTPEditText);
            }
            showAndProceedStep4SendManualOTP();
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
    private String mOtp;

    private ProgressDialog mProgressDialog;
    private boolean isOtpRequestManualEnter;
    private TextView mTextViewNote;
    private TextView mTextViewSkip;
    private boolean mIsExistingUser;
    private boolean isOtpLogin;
    private boolean mIsLoginDuringBrowse;
    private Typeface msundirectRegularFontTypeFace;
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


    public static FragmentOTPVerification newInstance(Bundle args) {
        FragmentOTPVerification fragmentOTPVerification = new FragmentOTPVerification();
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
        rootView = inflater.inflate(R.layout.fragment_otp, container, false);
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
        if (Util.checkUserLoginStatus()
                && TextUtils.isEmpty(PrefUtils.getInstance().getPrefEmailID())
                && (!PrefUtils.getInstance().getPrefIsOTPSkipped() || mIsLoginDuringBrowse)) {
            emailSupported = true;
            initComponent();
            return rootView;
        }
//        Login properties are available now show the login screen UI
        LoginProperties logniProperties = PropertiesHandler.getAvailableLoginProperties();
        if (logniProperties != null
                && logniProperties.loginSupported != null
                && !logniProperties.loginSupported.isEmpty()) {
            emailSupported = logniProperties.loginSupported.contains("email");
            mobileNoSupported = logniProperties.loginSupported.contains("mobile");
            initComponent();
            return rootView;
        }
        initComponent();
        fetchAvailableLoginTypes();
//        mFrameLayout.addView(rootView);
        return rootView;
    }

    private void fetchAvailableLoginTypes() {
        AlertDialogUtil.showProgressAlertDialog(mContext);
        AvailableLoginsProperties.Params params = new AvailableLoginsProperties.Params(getString(R.string.clientSecrete));
        AvailableLoginsProperties availableLogins = new AvailableLoginsProperties(params, new APICallback<AvailableLoginsPropertiesData>() {
            @Override
            public void onResponse(APIResponse<AvailableLoginsPropertiesData> response) {
                AlertDialogUtil.dismissProgressAlertDialog();
                if (response == null || response.body() == null) {
                    initComponent();
                    return;
                }
                if (response.body().properties != null
                        && response.body().properties.loginSupported != null
                        && !response.body().properties.loginSupported.isEmpty()) {
                    emailSupported = response.body().properties.loginSupported.contains("email");
                    mobileNoSupported = response.body().properties.loginSupported.contains("mobile");
                }
                initComponent();
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                AlertDialogUtil.dismissProgressAlertDialog();
                initComponent();
            }
        });

        APIService.getInstance().execute(availableLogins);
    }

    private void initComponent() {
        mMobileNoEditText = (EditText) rootView.findViewById(R.id.otp_mobile_no);
        mOTPEditText = (EditText) rootView.findViewById(R.id.otp_text_enter_otp);
        mTextViewHeading1 = (TextView) rootView.findViewById(R.id.otp_heading1);
        mTextViewHeading2 = (TextView) rootView.findViewById(R.id.otp_heading2);
        mTextViewHeading3 = (TextView) rootView.findViewById(R.id.otp_heading3);
        mTextViewNote = (TextView) rootView.findViewById(R.id.otp_note_text);
        mTextViewSkip = (TextView) rootView.findViewById(R.id.otp_skip_text);
        mTextViewTnC = (TextView) rootView.findViewById(R.id.txt_tnc);
        mImageViewMobileNoTickMark = (ImageView) rootView.findViewById(R.id.otp_mobile_no_tick_mark);
        mImageViewEmailIdTickMark = (ImageView) rootView.findViewById(R.id.otp_email_id_tick_mark);
        String tncString = StringEscapeUtils.unescapeJava(mContext.getString(R.string.txt_tnc_on_otp));
        Spannable wordtoSpan = new SpannableString(tncString);
        ClickableSpan span1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                // do some thing
                Intent i = new Intent(mContext, LiveScoreWebView.class);
                i.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.tnc));
                if (!TextUtils.isEmpty(PrefUtils.getInstance().getTncUrl())) {
                    i.putExtra("url", PrefUtils.getInstance().getTncUrl());
                } else {
                    i.putExtra("url", APIConstants.getFAQURL());
                }

                mContext.startActivity(i);
            }

            @Override
            public void updateDrawState(final TextPaint textPaint) {
                textPaint.setColor(Color.BLUE);
                textPaint.setUnderlineText(true);
            }
        };

        mTextViewTnC.setMovementMethod(LinkMovementMethod.getInstance());
        wordtoSpan.setSpan(span1, tncString.indexOf("Terms"), tncString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), tncString.indexOf("Terms"), tncString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTextViewTnC.setText(wordtoSpan);

        mTextViewChangeNumber = (TextView) rootView.findViewById(R.id.otp_change_number);
        mButton1 = (Button) rootView.findViewById(R.id.otp_btn_1);
        mButton2 = (Button) rootView.findViewById(R.id.otp_btn_2);
        mDropDownEmailIDs = (AutoCompleteTextView) rootView.findViewById(R.id.otp_drop_down_email_ids);
        mDropDownEmailIDs.setVisibility(View.GONE);
        if (emailSupported) {
            mDropDownEmailIDs.setVisibility(View.VISIBLE);
        }
        mMobileNoEditText.setOnFocusChangeListener(this);
        mDropDownEmailIDs.setOnFocusChangeListener(this);
        mDropDownEmailIDs.setOnTouchListener(this);
        mMobileNoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                handleFocusChange(mMobileNoEditText);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mDropDownEmailIDs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handleFocusChange(mDropDownEmailIDs);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mDropDownEmailIDs.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    mButton1.performClick();
                    return true;
                }
                return false;
            }
        });

        mMobileNoEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    mButton1.performClick();
                    return true;
                }
                return false;
            }
        });

        msundirectRegularFontTypeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/amazon_ember_cd_regular.ttf");
        mButton1.setOnClickListener(mSignInContinueClickListener);
        if (isOtpLogin) {
            mixpanelOTPLoginInitiated();
            startOtpReader();
            showAndProceedStep2WaitForAutoDetect();
        } else {
            initUI();
        }
    }

    private void initUI() {

        List<String> emailIds = Util.getEmailAccounts(mContext);
        LoggerD.debugOTP("emailIDs- " + emailIds + " mobile number- " + mMobileNo);
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefTempMsisdn())) {
            mMobileNo = PrefUtils.getInstance().getPrefTempMsisdn();
        }

        if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefTempEMAILID())) {
            mEmailID = PrefUtils.getInstance().getPrefTempEMAILID();
        }

        if (!TextUtils.isEmpty(mEmailID)) {
            mDropDownEmailIDs.setText(mEmailID.toLowerCase());
        }

        if (!TextUtils.isEmpty(mMobileNo)) {
            trimMobileNumberTo10Digits();
            mMobileNoEditText.setText(mMobileNo);
            if (TextUtils.isEmpty(mEmailID)) {
                mDropDownEmailIDs.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded() || !emailSupported) {
                            return;
                        }
                        mDropDownEmailIDs.requestFocus();
                        mDropDownEmailIDs.showDropDown();
                    }
                });
            }

        }
        if (!emailIds.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.simple_dropdown_item_1line, emailIds);
            mDropDownEmailIDs.setAdapter(adapter);
            mDropDownEmailIDs.setThreshold(0);
        }
        mTextViewNote.setVisibility(View.VISIBLE);
        mTextViewTnC.setVisibility(View.VISIBLE);
        if (mIsExistingUser) {
            showAlreadyExistingUserScreen();
//            Analytics.createScreenGA(Analytics.SCREEN_PROFILE_UPDATE_SCREEN);
            FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.SCREEN_PROFILE_UPDATE_SCREEN);
        } else {
//            Analytics.createScreenGA(Analytics.SCREEN_LOGIN_SCREEN);
            FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.SCREEN_LOGIN_SCREEN);
        }
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
        if (mDropDownEmailIDs == null || !isAdded() || getActivity().isFinishing()) {
            return;
        }
        mDropDownEmailIDs.post(new Runnable() {
            @Override
            public void run() {
                if (!hasFocus) {
                    handleFocusChange(v);
                    return;
                }
                if (v.getId() == R.id.otp_drop_down_email_ids)
                    mDropDownEmailIDs.showDropDown();
            }
        });
    }

    private void handleFocusChange(View v) {

        if (v == null || isOtpLogin) return;

        if (v.getId() == R.id.otp_mobile_no) {
            validateAndUpdateUIForMobileNo();
            return;
        }
        if (v.getId() == R.id.otp_drop_down_email_ids) {
            validateAndUpdateUIForEmailId();
            return;
        }
    }

    private void validateAndUpdateUIForMobileNo() {
        mMobileNo = mMobileNoEditText.getText().toString();

        boolean isValidPhoneNumber = false;
        try {
            long num = Long.parseLong(mMobileNo);
            LoggerD.debugOTP(num + " is a number");
            if (mMobileNo.length() == 10) {
                showMobNoWrongTickmark = true;
                isValidPhoneNumber = true;
            }
        } catch (NumberFormatException e) {
            LoggerD.debugOTP(mMobileNo + "is not a number");
        }
        mImageViewMobileNoTickMark.setImageResource(0);
        if (mMobileNo == null || mMobileNo.isEmpty()) {
            showMobNoWrongTickmark = false;
            mImageViewMobileNoTickMark.setVisibility(View.GONE);
            mImageViewMobileNoTickMark.setImageResource(0);
            return;
        }
        if (showMobNoWrongTickmark) {
            mImageViewMobileNoTickMark.setVisibility(View.VISIBLE);
            mImageViewMobileNoTickMark.setImageResource(R.drawable.otp_cross_icon);
        }

        if (isValidPhoneNumber) {
            mImageViewMobileNoTickMark.setVisibility(View.VISIBLE);
            mImageViewMobileNoTickMark.setImageResource(R.drawable.otp_correct_icon);
        }

    }

    private void validateAndUpdateUIForEmailId() {
        mEmailID = mDropDownEmailIDs.getText().toString();

        mEmailID = mEmailID.trim();
        mImageViewEmailIdTickMark.setImageResource(0);

        if (mEmailID != null && mEmailID.length() == 0) {
            showEmailIdTickMark = false;
            mImageViewEmailIdTickMark.setVisibility(View.GONE);
            mImageViewEmailIdTickMark.setImageResource(0);
            return;
        }

        if (showEmailIdTickMark && emailSupported) {
            mImageViewEmailIdTickMark.setVisibility(View.VISIBLE);
            mImageViewEmailIdTickMark.setImageResource(R.drawable.otp_cross_icon);
        }
        if (isValidEmailID(mEmailID) && emailSupported) {
            showEmailIdTickMark = true;
            mImageViewEmailIdTickMark.setVisibility(View.VISIBLE);
            mImageViewEmailIdTickMark.setImageResource(R.drawable.otp_correct_icon);
        }

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mDropDownEmailIDs == null) {
            return false;
        }
        mDropDownEmailIDs.post(new Runnable() {
            @Override
            public void run() {
                if (mDropDownEmailIDs == null || !mDropDownEmailIDs.hasFocus()) {
                    return;
                }
                mDropDownEmailIDs.showDropDown();
            }
        });
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
        if (!isOtpRequestManualEnter) {
            mOtp = null;
        }
        if (TextUtils.isEmpty(mOtp)) {
            CleverTap.eventRegistrationInitiated(mEmailID.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);
        }
        LoggerD.debugOTP("emailId- " + mEmailID);
        MSISDNLoginV3.Params msisdnParams = new MSISDNLoginV3.Params(mMobileNo, "", mOtp);

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
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, "Invalid API Response");
                            showDeviceAuthenticationFailedWithOutExit(null);
                            return;
                        }
                        LoggerD.debugOTP("success: msisdn login status : " + response.body().status + " code : " + response.body().code
                                + " message : " + response.body().message);
                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.ACCOUNT_TYPE, "");

                        if (!TextUtils.isEmpty(response.body().userid)) {
                            params.put(Analytics.USER_ID, response.body().userid);
                        }

                        if (!APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            // login failed
                            params.put(Analytics.REASON_FAILURE, response.body().message);
                            params.put(Analytics.SIGN_UP_OPTION, "");
                            params.put(Analytics.ERROR_CODE, String.valueOf(response.body().code));
                            Analytics.trackEvent(Analytics.EventPriority.MEDIUM, Analytics.EVENT_SIGN_UP_FAILED, params);
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                            showDeviceAuthenticationFailedWithOutExit(response.body().message);
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
                            mixpanelOTPLoginFailed(response.body().status, String.valueOf(response.body().code));
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, response.body().message);
                            }
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            // login failed
                            CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);

                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        }

                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 217) {
                            isOtpLogin = true;
                            mixpanelOTPLoginInitiated();
                            startOtpReader();
                            showAndProceedStep2WaitForAutoDetect();
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
                                AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.ACCOUNT_TYPE, "");
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
                            AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                            Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                            mixpanelOTPLoginSuccess();
                            launchMainActivity();
                        }

                        if (!TextUtils.isEmpty(response.body().message)) {
                            if (!TextUtils.isEmpty(mOtp)) {
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, response.body().message);
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
                                CleverTap.eventOtpStatus(mOtp, APIConstants.FAILED, mContext.getString(R.string.network_error));
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
        mTextViewHeading1.setVisibility(View.VISIBLE);
        mTextViewHeading2.setVisibility(View.VISIBLE);
        mTextViewHeading3.setVisibility(View.GONE);
        mTextViewHeading2.setText(mContext.getString(R.string.otp_enter_email));
        if (PrefUtils.getInstance().getPrefEnableSkipOnOTP()) {
            mTextViewSkip.setVisibility(View.VISIBLE);
            mTextViewSkip.setTypeface(msundirectRegularFontTypeFace, Typeface.BOLD_ITALIC);
            mTextViewSkip.setOnClickListener(mSkipClickListener);
        }
    }

    private void showAndProceedStep1SignInRequest() {
        // read mobile number and email ID's and validate for API request.
        LoggerD.debugOTP("showAndProceedStep1SignInRequest");
        if (mIsExistingUser) {
            showAlreadyExistingUserScreen();
        } else {
            mTextViewHeading1.setVisibility(View.VISIBLE);
            mTextViewHeading2.setVisibility(View.VISIBLE);
            mTextViewHeading3.setVisibility(View.VISIBLE);
            mTextViewChangeNumber.setVisibility(View.GONE);
        }


        mButton1.setVisibility(View.VISIBLE);

        mMobileNo = mMobileNoEditText.getText().toString();
        mEmailID = mDropDownEmailIDs.getText().toString();

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

        mEmailID = mEmailID.trim();
        if (!isValidPhoneNumber) {
            mDropDownEmailIDs.clearFocus();
            mMobileNoEditText.requestFocus();
            mImageViewMobileNoTickMark.setVisibility(View.VISIBLE);
            mImageViewMobileNoTickMark.setImageResource(R.drawable.otp_cross_icon);
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_MOBILE_NO);
            return;
        }
        if (!isValidEmailID(mEmailID) && emailSupported) {
            mMobileNoEditText.clearFocus();
            mDropDownEmailIDs.requestFocus();
            mImageViewEmailIdTickMark.setVisibility(View.VISIBLE);
            mImageViewEmailIdTickMark.setImageResource(R.drawable.otp_cross_icon);
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
            CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }
        //Make login request and Start Otp reader and listen for otp
        FirebaseAnalytics.getInstance().setEmailProperty(mEmailID);
        FirebaseAnalytics.getInstance().setMobileNumberProperty(mMobileNo);
        PrefUtils.getInstance().setPrefTempMsisdn(mMobileNo);
        PrefUtils.getInstance().setPrefTempEMAILID(mEmailID);
        hideSoftInputKeyBoard(mDropDownEmailIDs);
        hideSoftInputKeyBoard(mOTPEditText);
        mDropDownEmailIDs.clearFocus();
        isOtpRequestManualEnter = false;
        if (mIsExistingUser) {
            updateProfileWithEmailID();
            return;
        }
        makeSMSRetrieverAPI();
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
            /*com.google.firebase.analytics.FirebaseAnalytics.getInstance(mContext).setUserProperty(FirebaseAnalytics.PROPERTY_EMAIl,emailId);*/
        }
        return false;
    }

    private void showAndProceedStep2WaitForAutoDetect() {
        // Look up for OTP message If detected automatically send it
        LoggerD.debugOTP("showAndProceedStep2WaitForAutoDetect");
//        Analytics.createScreenGA(Analytics.SCREEN_OTP_SCREEN);
        FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.SCREEN_OTP_SCREEN);
        mImageViewMobileNoTickMark.setVisibility(View.GONE);
        mImageViewEmailIdTickMark.setVisibility(View.GONE);
        rootView.findViewById(R.id.otp_drop_down_email_ids_otp).setVisibility(View.GONE);

        mTextViewHeading1.setVisibility(View.VISIBLE);
        mTextViewHeading2.setVisibility(View.VISIBLE);
        mTextViewHeading3.setVisibility(View.VISIBLE);
        mTextViewNote.setVisibility(View.GONE);
        mTextViewSkip.setVisibility(View.GONE);
        mTextViewTnC.setVisibility(View.GONE);

        mTextViewHeading1.setText(mContext.getString(R.string.otp_stp2_heading1));
        mTextViewHeading2.setText(mContext.getString(R.string.otp_stp2_heading2));
        mTextViewHeading3.setText(mContext.getString(R.string.otp_stp2_heading3));

        mTextViewSkip.setVisibility(View.GONE);
        mButton1.setVisibility(View.VISIBLE);
        mButton2.setVisibility(View.VISIBLE);

        mButton1.setText(mContext.getString(R.string.otp_resend));
        if (mContext != null) {
            mButton2.setText(mContext.getString(R.string.otp_waiting));
        } else {
            mButton2.setText(mContext.getString(R.string.otp_go));
        }

        Drawable background = mButton1.getBackground();
        int color = mContext.getResources().getColor(R.color.dim_gray);
        if (background instanceof GradientDrawable) {
            LoggerD.debugOTP("GradientDrawable background type");
            ((GradientDrawable) background.mutate()).setColor(color);
        }
        mDropDownEmailIDs.setVisibility(View.GONE);
        mDropDownEmailIDs.clearFocus();
        mMobileNoEditText.setVisibility(View.GONE);
        mMobileNoEditText.clearFocus();
        mOTPEditText.setVisibility(View.VISIBLE);
        if (mContext != null) {
            mOTPEditText.setEnabled(false);
            mButton2.setEnabled(false);
        } else {
            mOTPEditText.setEnabled(true);
            mButton2.setEnabled(true);
        }
        mButton1.setEnabled(false);

        mMobileNoEditText.setHint(mContext.getString(R.string.otp_otp_hint));
        mButton1.setOnClickListener(null);
    }

    private void showAndProceedStep3AllowEnterOtpManual() {
        LoggerD.debugOTP("showAndProceedStep3AllowEnterOtpManual");
        LoggerD.debugOTP("getPrefEnableManualOTP- " + PrefUtils.getInstance().getPrefEnableManualOTP());
        if (!PrefUtils.getInstance().getPrefEnableManualOTP()) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_otp_not_recieved));
            // Redirect the user to re-enter the data
            mStep3ClickListenerChangeNumber.onClick(null);
            return;
        }
        rootView.findViewById(R.id.otp_drop_down_email_ids_otp).setVisibility(View.VISIBLE);
        // Listen to OTP message If detected automatically send it
        mTextViewHeading1.setVisibility(View.VISIBLE);
        mTextViewHeading2.setVisibility(View.GONE);
        mTextViewHeading3.setVisibility(View.VISIBLE);
        mTextViewNote.setVisibility(View.GONE);
        mTextViewSkip.setVisibility(View.GONE);
        mTextViewTnC.setVisibility(View.GONE);

        mTextViewHeading1.setText(mContext.getString(R.string.otp_stp3_heading1));
        mTextViewHeading3.setText(mContext.getString(R.string.otp_stp3_heading3));

        mButton1.setVisibility(View.VISIBLE);
        mButton2.setVisibility(View.VISIBLE);

        mButton1.setText(mContext.getString(R.string.otp_resend));
        mButton2.setText(mContext.getString(R.string.otp_go));

        Drawable background = mButton1.getBackground();
        int color = mContext.getResources().getColor(R.color.epg_list_item_color);
        if (background instanceof GradientDrawable) {
            LoggerD.debugOTP("GradientDrawable background type");
            ((GradientDrawable) background.mutate()).setColor(color);
        }

        mDropDownEmailIDs.setVisibility(View.GONE);
        mMobileNoEditText.setVisibility(View.GONE);
        mOTPEditText.setVisibility(View.VISIBLE);
        mOTPEditText.setEnabled(true);
        mMobileNoEditText.setHint(mContext.getString(R.string.otp_otp_hint));
        mButton1.setEnabled(true);
        mButton2.setEnabled(true);

//        mTextViewChangeNumber.setText(Html.fromHtml(mContext.getString(R.string.otp_change_numer)));
        mTextViewChangeNumber.setVisibility(View.VISIBLE);
        mTextViewChangeNumber.setTypeface(msundirectRegularFontTypeFace, Typeface.BOLD_ITALIC);
        mTextViewChangeNumber.setOnClickListener(mStep3ClickListenerChangeNumber);
        mButton1.setOnClickListener(mStep1ClickListenerResendOTP);
        mButton2.setOnClickListener(mStep3ClickListenerOTPManuallySubmit);
    }

    private void showAndProceedStep4SendManualOTP() {
        // read mobile number and email ID's and validate for API request.
        LoggerD.debugOTP("showAndProceedStep3AllowEnterOtpManual");

        isOtpRequestManualEnter = true;

        mOtp = mOTPEditText.getText().toString();
        CleverTap.eventOtpEntered(mOtp, "manual");
        if (mOtp.length() <= 0) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_otp));
            return;
        }

        mTextViewHeading1.setVisibility(View.VISIBLE);
        mTextViewHeading2.setVisibility(View.GONE);
        mTextViewHeading3.setVisibility(View.VISIBLE);
        mTextViewNote.setVisibility(View.GONE);
        mTextViewSkip.setVisibility(View.GONE);
        mTextViewTnC.setVisibility(View.GONE);

/*

        mTextViewHeading1.setText(mContext.getString(R.string.otp_stp2_heading1));
        mTextViewHeading2.setText(mContext.getString(R.string.otp_stp2_heading2));
        mTextViewHeading3.setText(mContext.getString(R.string.otp_stp2_heading3));
*/

        mButton1.setVisibility(View.VISIBLE);
        mButton2.setVisibility(View.VISIBLE);


//        mOtp = mOTPEditText.getText().toString();

       /* mButton1.setEnabled(false);
        mButton1.postDelayed(new Runnable() {
            @Override
            public void run() {
                mButton1.setEnabled(true);
            }
        },2000);*/
        Drawable background = mButton1.getBackground();
        int color = mContext.getResources().getColor(R.color.epg_list_item_color);
        if (background instanceof GradientDrawable) {
            LoggerD.debugOTP("GradientDrawable background type");
            ((GradientDrawable) background.mutate()).setColor(color);
        }
        //Make login request and Start Otp reader and listen for otp
        PrefUtils.getInstance().setPrefTempMsisdn(mMobileNo);
        PrefUtils.getInstance().setPrefTempEMAILID(mEmailID);
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
        showAndProceedStep3AllowEnterOtpManual();
        stopOtpReader();
        CleverTap.eventOtpStatus(null, APIConstants.FAILED, Analytics.EVENT_VALUE_OTP_TIME_OUT);
    }

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
}
