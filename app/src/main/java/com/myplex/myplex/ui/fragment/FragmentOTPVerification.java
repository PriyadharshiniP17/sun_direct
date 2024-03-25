package com.myplex.myplex.ui.fragment;

import static com.myplex.myplex.ApplicationController.getAppContext;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.myplex.api.request.user.ForgotPasswordOTPRequest;
import com.myplex.api.request.user.RequestResetPassword;
import com.myplex.api.request.user.UpdateProfileRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.model.UserProfileResponseData;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.FragmentCreatePassword;
import com.myplex.myplex.utils.OTPCallback;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class FragmentOTPVerification extends BaseFragment {

    private View rootView;
    private EditText /*mSmartCardNumber*/mMobileNumber, mOTPNumber,newPwdET, confirmNewPwdET;
    private Button mSubmitButton, mProceed;
    private String mobileNumber;
    private ImageView backNavigation;
    private String enteredOTP;
    /*private String emailID;*/
    private TextInputLayout otpInputLayout;
    /* TextView smartCardAlert,mOTPAlert,mResendOtpBtn,mOTPReceivedSug,mSmartCardSug;*/
    private TextView smartCardAlert, mOTPAlert, mResendOtpBtn, mOTPReceivedSug, mSmartCardSug, mConfirmPasswordText, mNewPassowrdText;
    private TextView  newPasswordValid, confirmPasswordValid;
    private TextView mobileText, otpText;
    private ProgressDialog mProgressDialog;
    private static OTPCallback otpCallback;
    private String name, mobile, smartCard, email;
    private CountDownTimer countDownTimer;

    public static final String TAG = "FragmentOTPVerfication";
    String strValue = "";
    long Mmin, Ssec;
    public static FragmentOTPVerification newInstance(Bundle args, OTPCallback otp) {
        FragmentOTPVerification fragmentResetPassword = new FragmentOTPVerification();
        fragmentResetPassword.setArguments(args);
        otpCallback = otp;
        return fragmentResetPassword;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();

        mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        mFrameLayout = new FrameLayout(mContext);
        rootView = inflater.inflate(R.layout.fragment_otp_verification, container, false);
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
        otpText = rootView.findViewById(R.id.otp_text);
        confirmPasswordValid = (TextView) rootView.findViewById(R.id.confirm_valid);
        newPasswordValid = (TextView) rootView.findViewById(R.id.new_password_valid);
        newPwdET = rootView.findViewById(R.id.newPWDEditText_feild);
        confirmNewPwdET = rootView.findViewById(R.id.confirmNewPwdEditText);
        mConfirmPasswordText = rootView.findViewById(R.id.new_password_text);
        mNewPassowrdText = rootView.findViewById(R.id.confirm_passowrd_text);
        Bundle bundle = getArguments();
        if(bundle.containsKey("name") && !bundle.getString("name").isEmpty()) {
            name = bundle.getString("name");
        }
        if(bundle.containsKey("mobile") && !bundle.getString("mobile").isEmpty()) {
            mobile = bundle.getString("mobile");
        }
        if(bundle.containsKey("smartCard") && !bundle.getString("smartCard").isEmpty()) {
            smartCard = bundle.getString("smartCard");
        }
        if(bundle.containsKey("email") && !bundle.getString("email").isEmpty()) {
            email = bundle.getString("email");
        }
        /*  mSmartCardNumber=rootView.findViewById(R.id.smart_card_edittext);
         */
        mMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                smartCardAlert.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
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
                mobileNumber = mMobileNumber.getText().toString();
                if (!isValidMobileNumber(mobileNumber)) {
//                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
                    smartCardAlert.setVisibility(View.VISIBLE);
                    return;
                }
                //requestResetPassword(mobileNumber);
            }
        });
        mOTPNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mOTPAlert.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        backNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackClicked();
            }
        });
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
                    //requestForgotOtpValidation(mobileNumber, enteredOTP, true);
                    //showPasswordFields();
                    updateUserData(name, mobile, smartCard, email);

                }
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
              //  signupOTPRequest(mobilenumber, updateMobileNumber.getText().toString() , otp,"", name, smcNumber, confirmNewPwdET.getText().toString());
            }
        });
        return rootView;
    }


    private void updateUserData(final String name, String mobile, String smc, String email){
        UpdateProfileRequest.Params params = new UpdateProfileRequest.Params(name, mobile, email, smc, enteredOTP);
        UpdateProfileRequest updateProfileRequest=new UpdateProfileRequest(params,new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();
                    if(responseData.status!=null&&responseData.status.equals(APIConstants.SUCCESS)){

                        if(otpCallback != null) {
                            otpCallback.onSuccess(enteredOTP);
                            onBackClicked();
                        }
                    }else {
                        AlertDialogUtil.showToastNotification(response.message());
                    }
                }else {
                    if (response.message()!=null&&!TextUtils.isEmpty(response.message())){
                        AlertDialogUtil.showToastNotification(response.message());
                    }else {
                        AlertDialogUtil.showToastNotification(getResources().getString(R.string.default_profile_update_message));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(updateProfileRequest);
    }
    public void showPasswordFields(){
        mOTPAlert.setVisibility(View.GONE);
        otpInputLayout.setVisibility(View.GONE);
        otpText.setVisibility(View.GONE);
        newPasswordValid.setVisibility(View.VISIBLE);
        newPwdET.setVisibility(View.VISIBLE);
        mNewPassowrdText.setVisibility(View.VISIBLE);
        confirmPasswordValid.setVisibility(View.VISIBLE);
        confirmNewPwdET.setVisibility(View.VISIBLE);
        mConfirmPasswordText.setVisibility(View.VISIBLE);
    }
    public  void hidePasswordFields(){
        mOTPAlert.setVisibility(View.VISIBLE);
        otpInputLayout.setVisibility(View.VISIBLE);
        otpText.setVisibility(View.VISIBLE);
        newPasswordValid.setVisibility(View.GONE);
        newPwdET.setVisibility(View.GONE);
        mNewPassowrdText.setVisibility(View.GONE);
        confirmPasswordValid.setVisibility(View.GONE);
        confirmNewPwdET.setVisibility(View.GONE);
        mConfirmPasswordText.setVisibility(View.GONE);
    }

    void hideSoftInputKeyBoard(View view) {
        if (view != null && mContext != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void requestResetPassword(String email) {
        showProgressBar();
        LoggerD.debugOTP("emailId- " + email);
        RequestResetPassword.Params requestparams = new RequestResetPassword.Params(email);
        RequestResetPassword resetPassword = new RequestResetPassword(requestparams, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response == null || response.body() == null) {
                    AlertDialogUtil.showToastNotification(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL);
                    return;
                }
                dismissProgressBar();
                if (response.body().code == 200) {
                    if (response.body().status != null && response.body().status.equals(APIConstants.SUCCESS)) {

                       // AlertDialogUtil.showToastNotification(response.message());
                        mSubmitButton.setVisibility(View.GONE);
                        mSmartCardSug.setVisibility(View.GONE);
                        mResendOtpBtn.setVisibility(View.VISIBLE);
                        otpInputLayout.setVisibility(View.VISIBLE);
                        mOTPReceivedSug.setVisibility(View.VISIBLE);
                        countDownTimer.start();
                        mProceed.setVisibility(View.VISIBLE);
                        mobileText.setVisibility(View.GONE);
                        otpText.setVisibility(View.VISIBLE);
                        mOTPNumber.setVisibility(View.VISIBLE);
                        mMobileNumber.setVisibility(View.GONE);
                    /*    if (mBaseActivity != null) {
                            mBaseActivity.pushFragment(FragmentSignIn.newInstance(getArguments()));
                        }*/
                    }
                } else {
                    if (response.message() != null && !TextUtils.isEmpty(response.message())) {
                        smartCardAlert.setVisibility(View.VISIBLE);
                      //  AlertDialogUtil.showToastNotification(response.message());
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
                        args.putString("otp", otp);
                        mBaseActivity.pushFragment(FragmentCreatePassword.newInstance(args));
                    }
                } else {
                    if (response.message() != null && !TextUtils.isEmpty(response.message())) {
                        AlertDialogUtil.showToastNotification(response.message());
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
            mResendOtpBtn.setVisibility(View.GONE);
            countDownTimer.cancel();
            otpInputLayout.setVisibility(View.GONE);
            mOTPReceivedSug.setVisibility(View.GONE);
            mProceed.setVisibility(View.GONE);
            mOTPNumber.setVisibility(View.GONE);
            mMobileNumber.setVisibility(View.VISIBLE);
            mobileText.setVisibility(View.VISIBLE);
            otpText.setVisibility(View.GONE);
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
                return true;
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

}
