package com.myplex.myplex.utils;

import static com.myplex.api.myplexAPI.TAG;
import static com.myplex.myplex.ApplicationController.getAppContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.textfield.TextInputLayout;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.request.user.ForgotPasswordChangePasswordRequest;
import com.myplex.api.request.user.MSISDNLoginEncryptedShreyas;
import com.myplex.api.request.user.SignUpOTPRequest;
import com.myplex.api.request.user.UserProfileRequest;
import com.myplex.api.request.user.UserSignUpOTPRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.model.MsisdnData;
import com.myplex.model.SignupResponseData;
import com.myplex.model.UserProfileResponseData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class FragmentCreatePassword extends BaseFragment {
    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mHeaderImageView;
    private ImageView mChannelImageView;
    private RelativeLayout mRootLayout;
    private EditText currentPwdET, newPwdET, confirmNewPwdET, updateMobileNumber, otpEdittext;
    //  private TextView confirmPWDAlert;
    private TextInputLayout updateNumber, otpInputLayout;
    private Button saveNewPwdButton, getOtpButton;
    private TextView mResendOTPBtn;
    private ImageView backNavigation;
    private TextView suggestionText, updateMobileNumberValid, newPasswordValid, confirmPasswordValid, updateMobileText, otpText, otpValidationAlert;
    private LinearLayout registerInputFields, otpFields;
    public String mUpdateMobileNumber, updateNewPassword, updateConfirmPassword, otp;
    public Boolean isRegister;
    public String mobilenumber, name, smcNumber, newMobile = "", newOtp = "", newSMCRequest = "";
    private String mPage = "";
    private ProgressBar progress;
    public static final int SUBSCRIPTION_REQUEST = 100;
    private CountDownTimer countDownTimer;
    String strValue = "";
    long Mmin, Ssec;

    public static FragmentCreatePassword newInstance(Bundle args) {
        FragmentCreatePassword fragmentCreatePassword = new FragmentCreatePassword();
        fragmentCreatePassword.setArguments(args);
        return fragmentCreatePassword;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getActivity();
        View view = inflater.inflate(R.layout.activity_change_password, container, false);
       // mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        Bundle bundle = getArguments();
        isRegister = bundle.getBoolean("isRegister");
        if (bundle.containsKey("full_name") && bundle.getString("full_name") != null && !bundle.getString("full_name").isEmpty()) {
            name = bundle.getString("full_name");
        }
        if (bundle.containsKey("otp") && bundle.getString("otp") != null && !bundle.getString("otp").isEmpty()) {
            otp = bundle.getString("otp");
        }
        if (bundle.containsKey("smart_card_number") && bundle.getString("smart_card_number") != null && !bundle.getString("smart_card_number").isEmpty()) {
            smcNumber = bundle.getString("smart_card_number");
        }
        if (bundle.containsKey("mobile_number") && bundle.getString("mobile_number") != null && !bundle.getString("mobile_number").isEmpty()) {
            mobilenumber = bundle.getString("mobile_number");
        }
        if (bundle.containsKey("new_mobile") && bundle.getString("new_mobile") != null && !bundle.getString("new_mobile").isEmpty()) {
            newMobile = bundle.getString("new_mobile");
        }
        if (bundle.containsKey("new_otp") && bundle.getString("new_otp") != null && !bundle.getString("new_otp").isEmpty()) {
            newOtp = bundle.getString("new_otp");
        }
        if (bundle.containsKey("newSMCRequest") && bundle.getString("newSMCRequest") != null && !bundle.getString("newSMCRequest").isEmpty()) {
            newSMCRequest = bundle.getString("newSMCRequest");
        }
//        mToolbar.setContentInsetsAbsolute(0,0);

       /* mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mChannelImageView = (ImageView)mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);*/

//        currentPwdET=findViewById(R.id.currentPwdEditText);
        backNavigation = view.findViewById(R.id.back_navigation);
        backNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();*/
                if (getActivity() != null)
                    getActivity().onBackPressed();

            }
        });
        progress = view.findViewById(R.id.progress);
        newPwdET = view.findViewById(R.id.newPWDEditText_feild);
        confirmNewPwdET = view.findViewById(R.id.confirmNewPwdEditText);
        updateMobileNumberValid = (TextView) view.findViewById(R.id.update_phone_number_valid);
        confirmPasswordValid = (TextView) view.findViewById(R.id.confirm_valid);
        newPasswordValid = (TextView) view.findViewById(R.id.new_password_valid);
        updateMobileNumber = (EditText) view.findViewById(R.id.update_mobile_number);
        otpEdittext = (EditText) view.findViewById(R.id.otp_edittext);
        getOtpButton = view.findViewById(R.id.get_otp_button);
        otpValidationAlert = view.findViewById(R.id.otp_validation_alert);
        updateNumber = view.findViewById(R.id.update_number);
        otpInputLayout = view.findViewById(R.id.otp_input_layout);
        updateMobileText = (TextView) view.findViewById(R.id.update_number_text);
        otpText = (TextView) view.findViewById(R.id.otp_text);
        suggestionText = (TextView) view.findViewById(R.id.suggestion_text);
        registerInputFields = (LinearLayout) view.findViewById(R.id.input_fields);
        otpFields = (LinearLayout) view.findViewById(R.id.otp_fields);
        mResendOTPBtn = (TextView) view.findViewById(R.id.resend_otp_button);
        newPwdET.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        confirmNewPwdET.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        if (isRegister) {
            updateNumber.setVisibility(View.VISIBLE);
            updateMobileNumber.setVisibility(View.GONE);
            updateMobileText.setVisibility(View.GONE);
            if (mobilenumber != null && !mobilenumber.isEmpty())
                updateMobileNumber.setText(mobilenumber);
            //updateMobileNumberValid.setVisibility(View.VISIBLE);
        } else {
            updateNumber.setVisibility(View.GONE);
            updateMobileNumber.setVisibility(View.GONE);
            updateMobileText.setVisibility(View.GONE);
            updateMobileNumberValid.setVisibility(View.GONE);
        }
        updateMobileNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidUpdateMobileNumber()) {
                    updateMobileNumberValid.setVisibility(View.GONE);
                    return;
                } else {
                    updateMobileNumberValid.setVisibility(View.VISIBLE);
                    return;
                }


            }
        });
        newPwdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!newPwdET.getText().toString().isEmpty() && !confirmNewPwdET.getText().toString().isEmpty()) {
                    if(newPwdET.length() >= 6 && newPwdET.length() <= 32 && confirmNewPwdET.length() >= 6 && confirmNewPwdET.length() <= 32){
                        saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
                    }else{
                        saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                    }
                } else {
                    saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
                if (updateNewPassword != null) {
                    if (updateNewPassword.length() < 6) {
                        newPasswordValid.setText(R.string.otp_msg_new_password);
                        newPasswordValid.setVisibility(View.VISIBLE);
                    } else {
                        newPasswordValid.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                newPasswordValid.setVisibility(View.GONE);

            }
        });
        updateMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateMobileNumberValid.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmNewPwdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                confirmPasswordValid.setVisibility(View.GONE);
                if (confirmNewPwdET.isFocused()) {
                    confirmPasswordValid.setVisibility(View.GONE);
                }
                if (!confirmNewPwdET.getText().toString().isEmpty() && !newPwdET.getText().toString().isEmpty()) {
                    if(confirmNewPwdET.length() >= 6 && confirmNewPwdET.length() <= 32 && newPwdET.length() >= 6 && newPwdET.length() <= 32){
                        saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
                    }else{
                        saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                    }
                } else {
                    saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
        confirmNewPwdET.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                boolean handled = false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Handle pressing "Enter" key here
                    saveNewPwdButton.callOnClick();
                    handled = true;
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null && view != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return handled;
            }
        });
        newPwdET.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                boolean handled = false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Handle pressing "Enter" key here
                    confirmNewPwdET.requestFocus();
                    handled = true;
                }
                return handled;
            }
        });
        saveNewPwdButton = view.findViewById(R.id.saveNewPassword);
        Typeface amazonEmberRegular = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
        saveNewPwdButton.setTypeface(amazonEmberRegular);
        saveNewPwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNewPassword = newPwdET.getText().toString();
                updateConfirmPassword = confirmNewPwdET.getText().toString();
                if (updateNewPassword.isEmpty()) {
                    newPasswordValid.setText("Enter new password field should not be empty");
                    newPasswordValid.setVisibility(View.VISIBLE);
                    return;
                }
                if (!updateNewPassword.isEmpty() && updateConfirmPassword.isEmpty()) {
                    confirmPasswordValid.setText("Confirm new password field should not be empty");
                    confirmPasswordValid.setVisibility(View.VISIBLE);
                    return;
                }
                if (!updateConfirmPassword.equals(updateNewPassword)) {
                    confirmPasswordValid.setText(R.string.confirm_pswd_alert);
                    confirmPasswordValid.setVisibility(View.VISIBLE);
                    return;
                }
                if (isRegister && !isValidUpdateMobileNumber()) {
                    updateMobileNumberValid.setVisibility(View.VISIBLE);
                    return;
                } else if (!isValidUpdateNewPassword()) {
                    newPasswordValid.setVisibility(View.VISIBLE);
                    confirmPasswordValid.setVisibility(View.GONE);
                    return;
                } else if (!isValidUpdateConfirmPassword()) {
                    confirmPasswordValid.setVisibility(View.VISIBLE);
                    newPasswordValid.setVisibility(View.GONE);
                    return;
                } else if (!confirmNewPwdET.getText().toString().equals(newPwdET.getText().toString())) {
                    confirmPasswordValid.setVisibility(View.VISIBLE);
                    return;
                }
                if (isRegister) {
                  /*  if(!updateMobileNumber.getText().toString().equals(mobilenumber)) {
                        showOTPFields();
                        return;
                    }*/
                    signupOTPRequest(mobilenumber, newMobile, otp, newOtp, name, smcNumber, confirmNewPwdET.getText().toString());
                } else {
                    if (mobilenumber != null && otp != null)
                        updateUserPassword(mobilenumber, otp, newPwdET.getText().toString());
                }
                /*Bundle args = new Bundle();
//                mBaseActivity.pushFragment(FragmentSignIn.newInstance(args));
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();*/
            }
        });
        getOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOTP = otpEdittext.getText().toString();
                if (!isValidOTP(enteredOTP)) {
                    otpValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                signupOTPRequest(mobilenumber, updateMobileNumber.getText().toString(), otp, enteredOTP, name, smcNumber, confirmNewPwdET.getText().toString());
                // signupOTPRequest(updateMobileNumber.getText().toString(), otpEdittext.getText().toString());
            }
        });
        otpEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                otpValidationAlert.setVisibility(View.GONE);
                if (otpEdittext.isFocused()) {
                    otpValidationAlert.setVisibility(View.GONE);
                }
                if (!otpEdittext.getText().toString().isEmpty()) {
                    if(updateNewPassword.length() >= 6 && updateNewPassword.length() <= 32){
                        saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
                    }else{
                        saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                    }
                } else {
                    saveNewPwdButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
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
                mResendOTPBtn.setText("Resend OTP in " + strValue + " sec");
                mResendOTPBtn.setEnabled(false);

            }
            // When the task is over it will print 00:00:00 there
            public void onFinish() {
                mResendOTPBtn.setText("Resend OTP");
                mResendOTPBtn.setEnabled(true);
            }
        };
        mResendOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupOTPRequest(mobilenumber, updateMobileNumber.getText().toString(), otp, "", name, smcNumber, confirmNewPwdET.getText().toString());
            }
        });
//        initUI();
        return view;
    }

    private boolean isValidOTP(String enteredOTP) {
        if (enteredOTP == null || TextUtils.isEmpty(enteredOTP)) {
            return false;
        }
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getOTPLength())) {
            final String requiredLength = PrefUtils.getInstance().getOTPLength();
            int length = Integer.parseInt(requiredLength);
            if (enteredOTP.length() > length && enteredOTP.length() < length) {
                otpValidationAlert.setVisibility(View.VISIBLE);
                return false;
            }
            if (enteredOTP.length() == length) {
                return true;
            }
        }
        return false;
    }

    public boolean backHandle() {
        if (mPage.equalsIgnoreCase("otp")) {
            hideOTPFields();
            return false;
        } else
            return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public void showOTPFields() {
        mPage = "otp";
        registerInputFields.setVisibility(View.GONE);
        otpFields.setVisibility(View.VISIBLE);
        suggestionText.setText(getString(R.string.Otp_received_suggestion));
        mResendOTPBtn.setVisibility(View.VISIBLE);
        countDownTimer.start();
    }

    public void hideOTPFields() {
        mPage = "";
        registerInputFields.setVisibility(View.VISIBLE);
        otpFields.setVisibility(View.GONE);
        suggestionText.setText(getString(R.string.create_new_password));
        mResendOTPBtn.setVisibility(View.GONE);
        countDownTimer.cancel();
    }

    private void signupOTPRequest(String mMobileNo, String otp) {

        // showProgressBar();
      /*  CleverTap.eventRegistrationInitiated(mEmailID.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);

        LoggerD.debugOTP("emailId- " + mEmailID);*/
        // CleverTap.eventRegistrationInitiated(mSubscribername.trim(), mMobileNo.toLowerCase().trim(), APIConstants.SUCCESS, null);
        progress.setVisibility(View.VISIBLE);
        SignUpOTPRequest.Params msisdnParams = new SignUpOTPRequest.Params(mMobileNo, otp, "");

        SignUpOTPRequest login = new SignUpOTPRequest(msisdnParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
                        LoggerD.debugOTP("response-" + response);
                        if (!isAdded()) {
                            return;
                        }
                        //   dismissProgressBar();
                        progress.setVisibility(View.GONE);
                        if (response == null
                                || response.body() == null) {
                            // login failed
                            LoggerD.debugOTP("success: msisdn login: " + "failed");
                            /*      CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, "Invalid API Response");*/
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
                            if (otp.isEmpty())
                                showOTPFields();
                            else {
                                hideOTPFields();
                                mobilenumber = mMobileNo;
                            }
                            return;
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        LoggerD.debugOTP("Failed: " + t);
                        progress.setVisibility(View.GONE);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailedWithOutExit(null);
                    }
                });
        APIService.getInstance().execute(login);
    }

    private void signupOTPRequest(String mMobileNo, String mNewMobileNo, String otp, String new_otp, String name, String smc, String password) {

        //showProgressBar();
        progress.setVisibility(View.VISIBLE);
        UserSignUpOTPRequest.Params msisdnParams;
        if(newSMCRequest.isEmpty())
            msisdnParams = new UserSignUpOTPRequest.Params(mMobileNo, mNewMobileNo, otp, new_otp, name, smc, password);
        else
            msisdnParams = new UserSignUpOTPRequest.Params(mMobileNo, mNewMobileNo, otp, new_otp, name, smc, password,newSMCRequest);

        UserSignUpOTPRequest login = new UserSignUpOTPRequest(msisdnParams,
                new APICallback<SignupResponseData>() {
                    @Override
                    public void onResponse(APIResponse<SignupResponseData> response) {
                        LoggerD.debugOTP("response-" + response);
                        if (!isAdded()) {
                            return;
                        }
                        // dismissProgressBar();
                        progress.setVisibility(View.GONE);
                        if (response == null
                                || response.body() == null) {
                            // login failed
                            LoggerD.debugOTP("success: msisdn login: " + "failed");
                            /*      CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, "Invalid API Response");*/
                            return;
                        }
                        LoggerD.debugOTP("success: msisdn login status : " + response.body().status + " code : " + response.body().code
                                + " message : " + response.body().message);
                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_CLIENT);

                     /*   if (!TextUtils.isEmpty(response.body().userid)) {
                            params.put(Analytics.USER_ID, response.body().userid);
                        }*/

                        if (response.body() != null && response.body().code == 200
                                || response.body().code == 201) {
                            if (response.body().status.equalsIgnoreCase("SUCCESS_NEED_CONFIRMATION")) {
                                showOTPFields();
                            } else {
                              /*  if (response.body().getUi() != null && response.body().getUi().getAction().equalsIgnoreCase("htmlOfferPage")) {
                                    if (!TextUtils.isEmpty(response.body().getUi().getRedirect()) && isAdded() && getActivity() != null) {
                                        startActivityForResult(SubscriptionWebActivity.createIntent(getActivity(), response.body().getUi().getRedirect(), SubscriptionWebActivity.PARAM_LAUNCH_NONE), SUBSCRIPTION_REQUEST);
                                    }
                                } else*/ {
                                    if (response.body().getUi() != null && response.body().getUi().getAction().equalsIgnoreCase("showToast")) {
                                        if (response.body().message != null)
                                            AlertDialogUtil.showToastNotification(response.body().message);
                                    }
                                    fetchUserId();
                                }
                            }
                            // MainActivityLauncherUtil.initStartUpCalls(mActivity);

                            //makeUserLoginRequest();
                        } else {
                            AlertDialogUtil.showToastNotification(response.body().message);
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        LoggerD.debugOTP("response-" + errorCode);
                        // dismissProgressBar();
                        progress.setVisibility(View.GONE);
                    }
                });
        APIService.getInstance().execute(login);
    }


    private void makeUserLoginRequest() {
        LoggerD.debugOTP("emailId- " + mobilenumber);
        // As per first cut mobile and otp requirement  hard coding mail id and password
        MSISDNLoginEncryptedShreyas.Params msisdnParams = new MSISDNLoginEncryptedShreyas.Params(mobilenumber, updateConfirmPassword);

        MSISDNLoginEncryptedShreyas login = new MSISDNLoginEncryptedShreyas(msisdnParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
                        LoggerD.debugOTP("response-" + response);
                        if (!isAdded()) {
                            return;
                        }
                        // dismissProgressBar();
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
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            return;
                        }
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 216) {
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                        }

                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                                && response.body().code == 217) {
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            return;
                        }

                        if (response.body().status.equalsIgnoreCase("SUCCESS")
                                && (response.body().code == 200
                                || response.body().code == 201)) {
                            if (!TextUtils.isEmpty(response.body().serviceName)) {
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }
                            FirebaseAnalytics.getInstance().userSignedInCompleted();
                            myplexAPI.clearCache(APIConstants.BASE_URL);
                            PropertiesHandler.clearCategoryScreenFilter();
                            if (!TextUtils.isEmpty(mobilenumber)) {
                                PrefUtils.getInstance().setPrefMsisdnNo(mobilenumber);
                            }
                            if (!TextUtils.isEmpty(mobilenumber)) {
                                PrefUtils.getInstance().setPrefMsisdnNo(mobilenumber);
                            }
                            CleverTap.eventRegistrationCompleted(response.body().email, response.body().mobile, Analytics.NO);
                            if (!TextUtils.isEmpty(response.body().mobile)) {
                                PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
                            } else if (!TextUtils.isEmpty(response.body().email)) {
                                PrefUtils.getInstance().setPrefMsisdnNo(response.body().email);
                            }
                            PrefUtils.getInstance().setString("IMAGE_URL", "");
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
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_CLIENT);
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.MIXPANEL_PEOPLE_JOINING_DATE, Util.getCurrentDate());
                                params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                                Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                                mixpanelOTPLoginSuccess();
                                launchMainActivity();
                                // fetchOfferAvailability();
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
                            AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                            Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                            mixpanelOTPLoginSuccess();
                            launchMainActivity();
                        }

                        if (!TextUtils.isEmpty(response.body().message)) {

                            // CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
                            AlertDialogUtil.showToastNotification(response.body().message);
                        }

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //dismissProgressBar();
                        LoggerD.debugOTP("Failed: " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {

                            // CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, mContext.getString(R.string.network_error));
                            showDeviceAuthenticationFailedWithOutExit(mContext.getString(R.string.network_error));
                            return;
                        }
                        showDeviceAuthenticationFailedWithOutExit(null);
                    }
                });
        APIService.getInstance().execute(login);
    }

    private void launchMainActivity() {
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
        MainActivityLauncherUtil.initStartUpCalls(activity);
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

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //getActivity().finish();
            getActivity().onBackPressed();
            //onBackPressed();
            //showOverFlowSettings(v);
           /* FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.popBackStack();*/

        }
    };

    public void fetchUserId() {
        progress.setVisibility(View.VISIBLE);
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (!isAdded()) {
                    return;
                }
                // dismissProgressBar();
                progress.setVisibility(View.GONE);
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
                    FirebaseAnalytics.getInstance().userSignedInCompleted();
                    return;
                }
                if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                        && response.body().code == 216) {
                    FirebaseAnalytics.getInstance().userSignedInCompleted();
                    LoggerD.debugOTP("success: msisdn login: " + "failed" + response.body().message);
                }

                if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)
                        && response.body().code == 217) {
                    FirebaseAnalytics.getInstance().userSignedInCompleted();
                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && (response.body().code == 200
                        || response.body().code == 201)) {
                    if (!TextUtils.isEmpty(response.body().serviceName)) {
                        PrefUtils.getInstance().setServiceName(response.body().serviceName);
                    }
                    FirebaseAnalytics.getInstance().userSignedInCompleted();
                    myplexAPI.clearCache(APIConstants.BASE_URL);
                    PropertiesHandler.clearCategoryScreenFilter();
                    if (!TextUtils.isEmpty(mobilenumber)) {
                        PrefUtils.getInstance().setPrefMsisdnNo(mobilenumber);
                    }
                    if (!TextUtils.isEmpty(mobilenumber)) {
                        PrefUtils.getInstance().setPrefMsisdnNo(mobilenumber);
                    }
                    CleverTap.eventRegistrationCompleted(response.body().email, response.body().mobile, Analytics.NO);
                    if (!TextUtils.isEmpty(response.body().mobile)) {
                        PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
                    } else if (!TextUtils.isEmpty(response.body().email)) {
                        PrefUtils.getInstance().setPrefMsisdnNo(response.body().email);
                    }
                    PrefUtils.getInstance().setString("IMAGE_URL", "");
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
                        Analytics.mixpanelSetOncePeopleProperty(Analytics.ACCOUNT_TYPE, Analytics.ACCOUNT_CLIENT);
                        Analytics.mixpanelSetOncePeopleProperty(Analytics.MIXPANEL_PEOPLE_JOINING_DATE, Util.getCurrentDate());
                        params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                        Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                        mixpanelOTPLoginSuccess();
                        launchMainActivity();
                        // fetchOfferAvailability();
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
                    AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                    Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                    mixpanelOTPLoginSuccess();
                    launchMainActivity();
                }

                if (!TextUtils.isEmpty(response.body().message)) {

                    // CleverTap.eventRegistrationFailed(mEmailID, mMobileNo, response.body().message);
                    AlertDialogUtil.showToastNotification(response.body().message);
                }


            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }

    private void mixpanelOTPLoginSuccess() {
        Map<String, String> params = new HashMap<>();
        params.put(Analytics.DEVICE_ID, PrefUtils.getInstance().getPrefDeviceid());
        params.put(Analytics.PARAM_OTP, "");
        int userid = PrefUtils.getInstance().getPrefUserId();
        params.put(Analytics.USER_ID, userid == 0 ? "NA" : userid + "");
        String otpDetection = "auto";
       /* if (isOtpRequestManualEnter) {
            otpDetection = "manual";
        }*/
        params.put(Analytics.PARAM_OTP_DETECTION, otpDetection);
        Analytics.mixpanelOTPLoginSuccess(params);

    }

    private void initUI() {
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
       /* mToolbar.addView(mInflateView);
        mToolbarTitle.setText(getResources().getString(R.string.change_password));*/
        mHeaderImageView.setOnClickListener(mCloseAction);

    }


    private void updateUserPassword(String mobileNumber, String otp, String newPwd) {
        progress.setVisibility(View.VISIBLE);
        ForgotPasswordChangePasswordRequest.Params params = new ForgotPasswordChangePasswordRequest.Params(mobileNumber, otp, false, newPwd);
        ForgotPasswordChangePasswordRequest updatePasswordRequest = new ForgotPasswordChangePasswordRequest(params, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                progress.setVisibility(View.GONE);
                if (response == null || response.body() == null) {
                    com.myplex.util.AlertDialogUtil.showToastNotification(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL);
                    return;
                }
                if (response.body().code == 402) {
                    com.myplex.util.AlertDialogUtil.showToastNotification(response.message());
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    BaseResponseData responseData = response.body();
                    if (responseData.status != null && responseData.status.equals(APIConstants.SUCCESS)) {
                        com.myplex.util.AlertDialogUtil.showToastNotification(response.message());
                     /*Intent ip = new Intent();
                        ip.putExtra(APIConstants.SUCCESS, APIConstants.SUCCESS);
                        getActivity().setResult(ProfileActivity.success, ip);
                        getActivity().finish();*/
                        Bundle args = new Bundle();
                        mBaseActivity.pushFragment(FragmentSignIn.newInstance(args));
                    } else {
                        com.myplex.util.AlertDialogUtil.showToastNotification(response.message());
                    }
                } else {
                    if (response.message() != null && !TextUtils.isEmpty(response.message())) {
                        com.myplex.util.AlertDialogUtil.showToastNotification(response.message());
                    } else {
                        com.myplex.util.AlertDialogUtil.showToastNotification(getResources().getString(R.string.default_password_update_message));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                progress.setVisibility(View.GONE);
                com.myplex.util.AlertDialogUtil.showToastNotification(t.getMessage());
                return;
            }
        });
        APIService.getInstance().execute(updatePasswordRequest);
    }

    private boolean isValidUpdateMobileNumber() {
        mUpdateMobileNumber = updateMobileNumber.getText().toString();

        boolean isValidUpdateMobileNumber = false;

        // mImageViewMobileNoTickMark.setImageResource(0);
        if (mUpdateMobileNumber == null || mUpdateMobileNumber.isEmpty()) {
            updateMobileNumberValid.setVisibility(View.VISIBLE);
            return false;
        }

        try {
            long num = Long.parseLong(mUpdateMobileNumber);
            LoggerD.debugOTP(num + " is a number");
            if (mUpdateMobileNumber.length() > 0) {
                if (mUpdateMobileNumber.length() < 10 && mUpdateMobileNumber.length() > 10) {
                    return false;
                }
                if (mUpdateMobileNumber.length() == 10) {
                    newPasswordValid.setVisibility(View.GONE);
                    confirmPasswordValid.setVisibility(View.GONE);
                    newPwdET.requestFocus();
                    // showMobNoWrongTickmark = true;
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            LoggerD.debugOTP(mUpdateMobileNumber + "is not a number");
        }
        return isValidUpdateMobileNumber;
    }

    private boolean isValidUpdateNewPassword() {
        updateNewPassword = newPwdET.getText().toString();

        boolean isValidUpdateNewPassword = false;

        // mImageViewMobileNoTickMark.setImageResource(0);
        if (updateNewPassword == null || updateNewPassword.isEmpty()) {
            newPasswordValid.setVisibility(View.VISIBLE);
            confirmPasswordValid.setVisibility(View.GONE);
            return false;
        }

//            long num = Long.parseLong(updateNewPassword);
//            LoggerD.debugOTP(num + " is a number");
//            if (updateNewPassword.length() > 0) {
              /*  if (updateNewPassword.length() < 6 && updateNewPassword.length() > 6) {
                    return false;
                }*/
        String newPasswordPattern = "^(?=.*[a-z])(?=."
                + "*[A-Z])(?=.*\\d)"
                + "(?=.*[-+_!@#$%^&*., ?]).+$";
        if (updateNewPassword.length() <= 6 && updateNewPassword.length() >= 32) {
            newPasswordValid.setVisibility(View.VISIBLE);
            return false;
        }  if (updateNewPassword.length() >= 6 && updateNewPassword.length() <= 32) {
            if (updateNewPassword.matches(newPasswordPattern))
                updateMobileNumberValid.setVisibility(View.GONE);
            confirmPasswordValid.setVisibility(View.GONE);
            newPasswordValid.setVisibility(View.GONE);
            confirmNewPwdET.requestFocus();

            // showMobNoWrongTickmark = true;
            return true;

        }
//            }

       /* } catch (NumberFormatException e) {
            LoggerD.debugOTP(updateNewPassword + "is not a number");
        }*/
        return isValidUpdateNewPassword;
    }

    private boolean isValidUpdateConfirmPassword() {
        updateConfirmPassword = confirmNewPwdET.getText().toString();

        boolean isValidUpdateConfirmPassword = false;

        // mImageViewMobileNoTickMark.setImageResource(0);
        if (updateConfirmPassword == null || updateConfirmPassword.isEmpty()) {
            confirmPasswordValid.setVisibility(View.VISIBLE);
            newPasswordValid.setVisibility(View.GONE);
            return false;
        }

        /*try {
            long num = Long.parseLong(updateConfirmPassword);
            LoggerD.debugOTP(num + " is a number");*/
        String confirmPasswordPattern = "^(?=.*[a-z])(?=."
                + "*[A-Z])(?=.*\\d)"
                + "(?=.*[-+_!@#$%^&*., ?]).+$";
        if (updateConfirmPassword.length() <= 6 && updateConfirmPassword.length() >= 32) {
            confirmPasswordValid.setVisibility(View.VISIBLE);
            return false;
        } if (updateConfirmPassword.length() >= 6 && updateConfirmPassword.length() <= 32) {
            if (updateConfirmPassword.matches(confirmPasswordPattern))
                updateMobileNumberValid.setVisibility(View.GONE);
            confirmPasswordValid.setVisibility(View.GONE);
            newPasswordValid.setVisibility(View.GONE);

            // showMobNoWrongTickmark = true;
            return true;
        }
           /* if (updateConfirmPassword.length() == 6) {
                // showMobNoWrongTickmark = true;
                return true;
            }*/
       /* } catch (NumberFormatException e) {
            LoggerD.debugOTP(updateConfirmPassword + "is not a number");
        }*/
        return isValidUpdateConfirmPassword;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //   if (requestCode == SubscriptionWebActivity.SUBSCRIPTION_REQUEST) {
        fetchUserId();
        // }
    }

    /*private boolean isValidPasswords() {
     *//*if(TextUtils.isEmpty(currentPwdET.getText().toString())){
            com.myplex.util.AlertDialogUtil.showToastNotification(getResources().getString(R.string.message_empty__current_pwd));
            return false;
        }*//*
        if(TextUtils.isEmpty(newPwdET.getText().toString())){
            newPasswordValid.setVisibility(View.VISIBLE);
//            com.myplex.util.AlertDialogUtil.showToastNotification(getResources().getString(R.string.message_empty__new_pwd));
            return false;

        }
        if(TextUtils.isEmpty(confirmNewPwdET.getText().toString())){
            confirmPasswordValid.setVisibility(View.VISIBLE);
//            com.myplex.util.AlertDialogUtil.showToastNotification(getResources().getString(R.string.message_empty__confirm_pwd));
            return false;

        }


        if(!newPwdET.getText().toString().equals(confirmNewPwdET.getText().toString())){
             AlertDialogUtil.showToastNotification(getResources().getString(R.string.message_mismatch_pwd));
            return false;
        }
        return true;
    }*/
    @Override
    public boolean onBackClicked() {
        return super.onBackClicked();
    }

    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
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
    }

    ;
}
