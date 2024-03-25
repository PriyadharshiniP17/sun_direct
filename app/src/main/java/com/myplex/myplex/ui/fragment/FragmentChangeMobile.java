package com.myplex.myplex.ui.fragment;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static com.myplex.api.myplexAPI.TAG;
import static com.myplex.myplex.ApplicationController.getAppContext;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.textfield.TextInputLayout;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.RequestChangeMobile;
import com.myplex.api.request.user.ForgotPasswordOTPRequest;
import com.myplex.api.request.user.RequestResetPassword;
import com.myplex.model.BaseResponseData;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.EditProfileActivity;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.FragmentCreatePassword;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class FragmentChangeMobile extends BaseFragment {

    private View rootView;
    private EditText /*mSmartCardNumber*/mMobileNumber, mOTPNumber;
    private Button mSubmitButton, mProceed;
    private String mobileNumber;
    private ImageView backNavigation;
    private String enteredOTP;
    /*private String emailID;*/
    private TextInputLayout otpInputLayout;
    /* TextView smartCardAlert,mOTPAlert,mResendOtpBtn,mOTPReceivedSug,mSmartCardSug;*/
    private TextView smartCardAlert, mOTPAlert, mResendOtpBtn, mOTPReceivedSug, mSmartCardSug;
    private LinearLayout inputFeildsLayout;
    private TextView mobileText, otpText;
    private ProgressDialog mProgressDialog;
    private CountDownTimer countDownTimer;
    String strValue = "";
    long Mmin, Ssec;


    public static FragmentChangeMobile newInstance(Bundle args) {
        FragmentChangeMobile fragmentResetPassword = new FragmentChangeMobile();
        fragmentResetPassword.setArguments(args);
        return fragmentResetPassword;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();

        mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        mFrameLayout = new FrameLayout(mContext);
        rootView = inflater.inflate(R.layout.fragment_change_mobile, container, false);
        mOTPAlert = rootView.findViewById(R.id.otp_validation_alert);
        mOTPNumber = rootView.findViewById(R.id.otp_edittext);
        otpInputLayout = rootView.findViewById(R.id.otp_input_layout);
        backNavigation = rootView.findViewById(R.id.back_navigation);
        mProceed = rootView.findViewById(R.id.proceed_btn);
        mResendOtpBtn = rootView.findViewById(R.id.resend_otp_button);
        mSmartCardSug = rootView.findViewById(R.id.reset_suggestion_text);
        mOTPReceivedSug = rootView.findViewById(R.id.otp_received_suggestion);
        mMobileNumber = rootView.findViewById(R.id.mobile_number_editText);
        mobileText = rootView.findViewById(R.id.mobile_text);
        setEditTextMaxLength(Integer.parseInt(PrefUtils.getInstance().getOTPLength()));
        otpText = rootView.findViewById(R.id.otp_text);
        rl_space_root = rootView.findViewById(R.id.rl_space_root);
        updateHorizontalSpacing();
        /*  mSmartCardNumber=rootView.findViewById(R.id.smart_card_edittext);
         */
        mMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mMobileNumber.isFocused()) {
                    smartCardAlert.setVisibility(View.GONE);
                }
                if (!mMobileNumber.getText().toString().isEmpty()) {
                    if(mMobileNumber.getText().toString().length()==10){
                        mSubmitButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
                    }else{
                        mSubmitButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                    }
                } else {
                    mSubmitButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
        inputFeildsLayout=rootView.findViewById(R.id.input_feilds);
        smartCardAlert = rootView.findViewById(R.id.smartcard_validation_alert);
        mSubmitButton = rootView.findViewById(R.id.get_otp_button);
        Typeface amazonEmberRegular = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
        mSubmitButton.setTypeface(amazonEmberRegular);
        mProceed.setTypeface(amazonEmberRegular);
        mResendOtpBtn.setTypeface(amazonEmberRegular);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         /*       emailID=mSmartCardNumber.getText().toString();
                if (!isValidEmailID(emailID)){
//                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
                    smartCardAlert.setVisibility(View.VISIBLE);
                    return;
                }
                else {
                    mSubmitButton.setVisibility(View.GONE);
                    mSmartCardSug.setVisibility(View.GONE);
                    mResendOtpBtn.setVisibility(View.VISIBLE);
                    otpInputLayout.setVisibility(View.VISIBLE);
                    mOTPReceivedSug.setVisibility(View.VISIBLE);
                    mProceed.setVisibility(View.VISIBLE);
                    mOTPNumber.setVisibility(View.VISIBLE);
                    mProceed.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            enteredOTP=mOTPNumber.getText().toString();
                            if(!isValidOTP(enteredOTP)){
                                mOTPAlert.setVisibility(View.VISIBLE);
                            }
                            Bundle args = new Bundle();
                            mBaseActivity.pushFragment(FragmentCreatePassword.newInstance(args));
                        }
                    });
                }
                requestResetPassword(emailID);
            }
        });*/
                if(mMobileNumber.getText().toString().isEmpty()){
                    smartCardAlert.setText("Mobile number field should not be empty");
                    smartCardAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if (!isValidMobileNumber(mMobileNumber.getText().toString())) {
                    smartCardAlert.setText(R.string.smart_card_alert);
                    smartCardAlert.setVisibility(View.VISIBLE);
                    return;
                }
                mobileNumber = mMobileNumber.getText().toString();
                if (!isValidMobileNumber(mobileNumber)) {
//                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
                    smartCardAlert.setVisibility(View.VISIBLE);
                    return;
                } else {
                    mProceed.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            enteredOTP = mOTPNumber.getText().toString();
                            if(enteredOTP.isEmpty()){
                                mOTPAlert.setText("Please Enter OTP");
                                mOTPAlert.setVisibility(View.VISIBLE);
                                return;
                            }
                            if (!isValidOTP(enteredOTP)) {
                                mOTPAlert.setText(R.string.Invalid_OTP);
                                mOTPAlert.setVisibility(View.VISIBLE);
                                return;
                            } else {
                                requestForgotOtpValidation(mobileNumber, enteredOTP, true);
                            }
                        }
                    });
                }
                requestResetPassword(mobileNumber);
            }
        });
        mOTPNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mOTPAlert.setVisibility(View.GONE);
                if (mOTPNumber.isFocused()) {
                    mOTPAlert.setVisibility(View.GONE);
                }
                if (!mOTPNumber.getText().toString().isEmpty()) {
                    if(mOTPNumber.length()== Integer.parseInt(PrefUtils.getInstance().getOTPLength())){
                        mProceed.setBackgroundResource(R.drawable.rounded_corner_button_white);

                    }else{
                        mProceed.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                    }
                } else {
                    mProceed.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        backNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOTPAlert.setVisibility(View.GONE);
                onBackClicked();
            }
        });
        Bundle bundle = getArguments();
        if(bundle.containsKey("mobile_number") && !bundle.getString("mobile_number").isEmpty()) {
            String mobilenumber = bundle.getString("mobile_number");
            mMobileNumber.setText(mobilenumber);
        }
        mResendOtpBtn.setEnabled(false);
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
                mResendOtpBtn.setText("Resend OTP in " + strValue + " sec");
                mResendOtpBtn.setEnabled(false);

            }
            // When the task is over it will print 00:00:00 there
            public void onFinish() {
                mResendOtpBtn.setText("Resend OTP");
                mResendOtpBtn.setEnabled(true);
            }
        };
        mResendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtil.showToastNotification("Request Sent");
                requestResetPassword(mobileNumber);
            }
        });
        return rootView;
    }


    void hideSoftInputKeyBoard(View view) {
        if (view != null && mContext != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void requestResetPassword(String mobileNumber) {
        showProgressBar();
        LoggerD.debugOTP("mobileNumber- " + mobileNumber);
        RequestChangeMobile.Params requestparams = new RequestChangeMobile.Params(mobileNumber);
        RequestChangeMobile resetPassword = new RequestChangeMobile(requestparams, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response == null || response.body() == null) {
                    AlertDialogUtil.showToastNotification(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL);
                    return;
                }
                dismissProgressBar();
                if (response.body().code == 200) {
                    if (response.body().status != null && response.body().status.equals("SUCCESS_NEED_CONFIRMATION")) {

                       // AlertDialogUtil.showToastNotification(response.message());
                        mSubmitButton.setVisibility(View.GONE);
                        mSmartCardSug.setVisibility(View.GONE);
                        otpInputLayout.setVisibility(View.VISIBLE);
                        mOTPReceivedSug.setVisibility(View.VISIBLE);
                        mProceed.setVisibility(View.VISIBLE);
                        mobileText.setVisibility(View.VISIBLE);
                        otpText.setVisibility(View.VISIBLE);
                        mOTPNumber.setVisibility(View.VISIBLE);
                        mMobileNumber.setVisibility(View.VISIBLE);
                        mResendOtpBtn.setVisibility(View.VISIBLE);
                        mOTPNumber.setText("");
                        mOTPAlert.setVisibility(View.GONE);
                        countDownTimer.start();
                    /*    if (mBaseActivity != null) {
                            mBaseActivity.pushFragment(FragmentSignIn.newInstance(getArguments()));
                        }*/
                    }
                } else {
                    if (response.message() != null && !TextUtils.isEmpty(response.message())) {
//                        smartCardAlert.setVisibility(View.VISIBLE);
                      //  AlertDialogUtil.showToastNotification(response.message());
                        AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                            @Override
                            public void onDialogClick(String buttonText) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                AlertDialogUtil.showToastNotification(t.getMessage());
            }
        });

        APIService.getInstance().execute(resetPassword);
    }

    private void requestForgotOtpValidation(String email, String otp, Boolean otpValidation) {
        showProgressBar();
        LoggerD.debugOTP("emailId- " + email);
        ForgotPasswordOTPRequest.Params requestparams = new ForgotPasswordOTPRequest.Params(email, otp, otpValidation);
        ForgotPasswordOTPRequest resetPassword = new ForgotPasswordOTPRequest(requestparams, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response == null || response.body() == null) {
                    AlertDialogUtil.showToastNotification(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL);
                    return;
                }
                dismissProgressBar();
                if (response.body().code == 200) {
                    if (response.body().status != null && response.body().status.equals(APIConstants.SUCCESS)) {
                        Bundle args = new Bundle();
                        args.putString("mobile_number", mobileNumber);
//                        args.putString("otp", otp);
                        mBaseActivity.pushFragment(FragmentChangeNewMobile.newInstance(args));
                        countDownTimer.cancel();
                    }
                } else {
                    if (response.message() != null && !TextUtils.isEmpty(response.message())) {
//                        AlertDialogUtil.showToastNotification(response.message());

                        AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                            @Override
                            public void onDialogClick(String buttonText) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                AlertDialogUtil.showToastNotification(t.getMessage());
            }
        });

        APIService.getInstance().execute(resetPassword);
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
        //
        getActivity().onBackPressed();
        return false;
    }

    public boolean backHandle() {
        hideSoftInputKeyBoard(mMobileNumber);
        if (mMobileNumber.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            mSubmitButton.setVisibility(View.VISIBLE);
            mSmartCardSug.setVisibility(View.VISIBLE);
            otpInputLayout.setVisibility(View.GONE);
            mOTPReceivedSug.setVisibility(View.GONE);
            mProceed.setVisibility(View.GONE);
            mResendOtpBtn.setVisibility(View.GONE);
            mResendOtpBtn.setEnabled(false);
            mOTPNumber.setVisibility(View.GONE);
            mMobileNumber.setVisibility(View.VISIBLE);
            mobileText.setVisibility(View.VISIBLE);
            otpText.setVisibility(View.GONE);
            countDownTimer.cancel();
            return false;

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
        inputFeildsLayout.clearFocus();
        mActivity = null;
    }

    /* private boolean isValidEmailID(String emailId) {
         if (emailId == null || TextUtils.isEmpty(emailId)) {
             return false;
         }

         if (emailId.length() > 0) {
             if(emailId.length()<11 && emailId.length()>11){
                 return false;
             }
             if(emailId.length()==11){
                 return true;
             }
         }*/
    private boolean isValidMobileNumber(String mobileNumber) {
        if (mobileNumber == null || TextUtils.isEmpty(mobileNumber)) {
            return false;
        }

        if (mobileNumber.length() > 0) {
            if (mobileNumber.length() < 10 && mobileNumber.length() > 10) {
                return false;
            }
            if (mobileNumber.length() == 10) {
                if(mobileNumber.substring(0, 1).matches("[6-9]")) {
                    return true;
                }else{
                    smartCardAlert.setVisibility(View.VISIBLE);
                }
            }
        }
       /* if (emailId.length() > 0) {
            int lengthFromDot = 0;
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

    private boolean isValidOTP(String enteredOTP) {
        if (enteredOTP == null || TextUtils.isEmpty(enteredOTP)) {
            return false;
        }
        if(!TextUtils.isEmpty(PrefUtils.getInstance().getOTPLength())) {
            final String requiredLength = PrefUtils.getInstance().getOTPLength();
            int length = Integer.parseInt(requiredLength);
            if (enteredOTP.length() > length && enteredOTP.length() < length) {
                mOTPAlert.setVisibility(View.VISIBLE);
                return false;
            }
            if (enteredOTP.length() == length) {
                return true;
            }
        }
        return false;
    }
    public void setEditTextMaxLength(int length) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        mOTPNumber.setFilters(filterArray);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateHorizontalSpacing();

    }
    int portraitWidth;
    RelativeLayout rl_space_root;
    private void updateHorizontalSpacing() {
        if(DeviceUtils.isTablet(mContext)){
            if(DeviceUtils.getScreenOrientation(mContext) != SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                if(portraitWidth <= 0)
                    portraitWidth = rl_space_root.getLayoutParams().width;
                rl_space_root.getLayoutParams().width =portraitWidth;
            }else {
                rl_space_root.getLayoutParams().width = (int)(0.45 * getResources().getDisplayMetrics().widthPixels);
            }

        }
    }

}
