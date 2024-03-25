package com.myplex.myplex.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.RequestResetPassword;
import com.myplex.model.BaseResponseData;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.FragmentRegisterCreatePassword;
import com.myplex.util.AlertDialogUtil;

public class FragmentGetNewConnection extends BaseFragment {

    private Context mContext;
    private View rootView;
    private BaseFragment mCurrentFragment;
    private EditText mobileNumber, pinCode, name, emailID;
    private String isFrom ;
    private String nameString, mobileNUmberString, pinCodeString, emaiIDString;
    private AppCompatButton continueButton;
    private ImageView back_navigation;
    private TextView nameValidationAlert, mobileValidationAlert, emailValidationAlert, pinCodeValidationAlert;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        rootView = inflater.inflate(R.layout.fragment_get_new_connection, container, false);
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        back_navigation = rootView.findViewById(R.id.back_navigation);
        back_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // getActivity().onBackPressed();
                onBackClicked();
            }

        });
        nameValidationAlert = rootView.findViewById(R.id.name_valid_alert);
        mobileValidationAlert = rootView.findViewById(R.id.mobile_valid_alert);
        emailValidationAlert = rootView.findViewById(R.id.email_valid_alert);
        pinCodeValidationAlert = rootView.findViewById(R.id.pincode_valid_alert);

        name = rootView.findViewById(R.id.name_edittext);
        emailID = rootView.findViewById(R.id.email_id_edittext);
        mobileNumber = rootView.findViewById(R.id.mobile_no_edittext);
        pinCode = rootView.findViewById(R.id.pin_code_edittext);
        continueButton = rootView.findViewById(R.id.continue_button);
        Bundle bundle = getArguments();
        if (bundle.containsKey("isFrom") && !bundle.getString("isFrom").isEmpty()) {
            isFrom = bundle.getString("isFrom");
        }
        if (bundle.containsKey("name") && !bundle.getString("name").isEmpty()) {
            nameString = bundle.getString("name");
        }
        if (bundle.containsKey("pincode") && !bundle.getString("pincode").isEmpty()) {
            pinCodeString = bundle.getString("pincode");
        }
        if (bundle.containsKey("mobile") && !bundle.getString("mobile").isEmpty()) {
            mobileNUmberString = bundle.getString("mobile");

        } if (bundle.containsKey("emailID") && !bundle.getString("emailID").isEmpty()) {
            emaiIDString = bundle.getString("emailID");
        }
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobileNUmberString=mobileNumber.getText().toString();
                pinCodeString=pinCode.getText().toString();
                emaiIDString=emailID.getText().toString();
                nameString=name.getText().toString();
                if(name.getText().toString().isEmpty()){
                    nameValidationAlert.setText("Name field should not be empty");
                    nameValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if (!isValidUserName(name.getText().toString())) {
                    nameValidationAlert.setText("Please enter valid name");
                    nameValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if(mobileNumber.getText().toString().isEmpty()){
                    mobileValidationAlert.setText("Mobile number field should not be empty");
                    mobileValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if (!isValidMobileNumber(mobileNumber.getText().toString())) {
                    mobileValidationAlert.setText(R.string.smart_card_alert);
                    mobileValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if(emailID.getText().toString().isEmpty()){
                    emailValidationAlert.setText("Email field should not be empty");
                    emailValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if (!isValidEmailID(emailID.getText().toString())) {
                    emailValidationAlert.setText("Please enter valid email");
                    emailValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if(pinCode.getText().toString().isEmpty()){
                    pinCodeValidationAlert.setText("Pincode field should not be empty");
                    pinCodeValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if (!isValidPinCode(pinCode.getText().toString())) {
                    pinCodeValidationAlert.setText("Please enter valid pincode");
                    pinCodeValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }

                if(!isValidUserName(nameString)){
                    nameValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if(!isValidMobileNumber(mobileNUmberString)) {
                    mobileValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }
                if(!isValidEmailID(emaiIDString)){
                    emailValidationAlert.setVisibility(View.VISIBLE);
                    return;
                }

                if(!isValidPinCode(pinCodeString)){
                    pinCodeValidationAlert.setVisibility(View.VISIBLE);
                        return;
                    }
                if(isFrom!=null && isFrom.equalsIgnoreCase("subscribe_apps")) {
//                    showRegisterCreatePassword();
                    requestResetPassword(mobileNUmberString);
                }else{
                    showSetTopBoxFragment();
                }

                }


        });
        name.addTextChangedListener(textWatcher);
        mobileNumber.addTextChangedListener(textWatcher);
        emailID.addTextChangedListener(textWatcher);
        pinCode.addTextChangedListener(textWatcher);
        setData();

        return rootView;
    }

    private void requestResetPassword(String mobileNumber) {
//        showProgressBar();
        LoggerD.debugOTP("emailId- " + mobileNumber);
        RequestResetPassword.Params requestparams = new RequestResetPassword.Params(mobileNumber);
        RequestResetPassword resetPassword = new RequestResetPassword(requestparams, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response == null || response.body() == null) {
                    com.myplex.util.AlertDialogUtil.showToastNotification(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL);
                    return;
                }
//                dismissProgressBar();
                if (response.body().code == 200) {
                    if (response.body().status != null && response.body().status.equals(APIConstants.SUCCESS)) {

                        // AlertDialogUtil.showToastNotification(response.message());
                       /* mSubmitButton.setVisibility(View.GONE);
                        mSmartCardSug.setVisibility(View.GONE);
                        mResendOtpBtn.setVisibility(View.VISIBLE);
                        otpInputLayout.setVisibility(View.VISIBLE);
                        mOTPReceivedSug.setVisibility(View.VISIBLE);
                        mProceed.setVisibility(View.VISIBLE);
                        mobileText.setVisibility(View.GONE);
                        otpText.setVisibility(View.VISIBLE);
                        mOTPNumber.setVisibility(View.VISIBLE);
                        mMobileNumber.setVisibility(View.GONE);*/

                        /*if (mBaseActivity != null) {
                            mBaseActivity.pushFragment(FragmentSignIn.newInstance(getArguments()));
                        }*/
                        showRegisterCreatePassword();
                    }
                    else if(response.body().status != null && response.body().status.equals("SUCCESS_NEED_CONFIRMATION")){
                        showRegisterCreatePassword();
                    }
                } else {
                    if (response.message() != null && !TextUtils.isEmpty(response.message())) {
                        mobileValidationAlert.setVisibility(View.VISIBLE);
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

    public boolean backHandle(){
      /*  if(mPage.equalsIgnoreCase("details")) {
            hideDetailsFields();
            return false;
        }
        else if(mPage.equalsIgnoreCase("otp")) {
            hideOTPFields();
            return false;
        } else*/
            return true;
    }

    @Override
    public boolean onBackClicked() {
        getActivity().onBackPressed();
        return false;
    }
    public void setData() {
        if (mobileNUmberString != null)
            mobileNumber.setText(mobileNUmberString);
        if (nameString != null)
            name.setText(nameString);
        if (pinCodeString != null)
            pinCode.setText(pinCodeString);
        if(emaiIDString!=null)
            emailID.setText(emaiIDString);
        }


    private boolean isValidEmailID(String emailAddress) {

            if (emailAddress == null || TextUtils.isEmpty(emailAddress)) {
                emailValidationAlert.setVisibility(View.VISIBLE);
                nameValidationAlert.setVisibility(View.GONE);
                pinCodeValidationAlert.setVisibility(View.GONE);
                mobileValidationAlert.setVisibility(View.GONE);
                return false;
            }
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2,6}+";
            if (emailAddress.length() > 0) {
                if (emailAddress.matches(emailPattern)) {
                    pinCode.requestFocus();
                    return true;
                } else {
                    emailValidationAlert.setVisibility(View.VISIBLE);
                    nameValidationAlert.setVisibility(View.GONE);
                    pinCodeValidationAlert.setVisibility(View.GONE);
                    mobileValidationAlert.setVisibility(View.GONE);
                    return false;
                }
            }


        return false;
    }
    private boolean isValidUserName(String userName){
            if (userName == null || TextUtils.isEmpty(userName)) {
                nameValidationAlert.setVisibility(View.VISIBLE);
                pinCodeValidationAlert.setVisibility(View.GONE);
                mobileValidationAlert.setVisibility(View.GONE);
                emailValidationAlert.setVisibility(View.GONE);
                return false;
            }
            if (userName.length() > 2) {
                if (userName.matches("^[a-zA-Z\\s]{0,22}$")) {
                    mobileNumber.requestFocus();
                    return true;
                }
            }
        return false;
    }

    private void showSetTopBoxFragment() {
        if (mBaseActivity != null) {
            Bundle args = new Bundle();
            args.putString("name", name.getText().toString());
            args.putString("mobile", mobileNumber.getText().toString());
            args.putString("pincode", pinCode.getText().toString());
            args.putString("emailID", emailID.getText().toString());
            mBaseActivity.pushFragment(FragmentSetTopBoxes.newInstance(args));
        }
    }

    private void showRegisterCreatePassword() {
        if (mBaseActivity != null) {
            Bundle args = new Bundle();
            args.putString("name", name.getText().toString());
            args.putString("mobile", mobileNumber.getText().toString());
            args.putString("pincode", pinCode.getText().toString());
            args.putString("emailID", emailID.getText().toString());
            args.putString("isFrom", isFrom);
            mBaseActivity.pushFragment(FragmentRegisterCreatePassword.newInstance(args));
        }
    }


    private boolean isValidPinCode(String pinCodeText) {
            if (pinCodeText == null || TextUtils.isEmpty(pinCodeText)) {
                pinCodeValidationAlert.setVisibility(View.VISIBLE);
                mobileValidationAlert.setVisibility(View.GONE);
                nameValidationAlert.setVisibility(View.GONE);
                emailValidationAlert.setVisibility(View.GONE);
                return false;
            }
            if (pinCodeText.length() > 0)
                if (pinCodeText.length() < 6 && pinCodeText.length() > 6) {
                    pinCodeValidationAlert.setVisibility(View.VISIBLE);
                    mobileValidationAlert.setVisibility(View.GONE);
                    nameValidationAlert.setVisibility(View.GONE);
                    emailValidationAlert.setVisibility(View.GONE);
                    return false;
                }
            if (pinCodeText.length() == 6) {
                return true;

            }

        return  false;
    }

    private boolean isValidMobileNumber(String number) {

            if (number == null || TextUtils.isEmpty(number)) {
                mobileValidationAlert.setVisibility(View.VISIBLE);
                pinCodeValidationAlert.setVisibility(View.GONE);
                nameValidationAlert.setVisibility(View.GONE);
                emailValidationAlert.setVisibility(View.GONE);
                return false;

            }
            if (number.length() < 10 && number.length() > 10) {
                mobileValidationAlert.setVisibility(View.VISIBLE);
                pinCodeValidationAlert.setVisibility(View.GONE);
                nameValidationAlert.setVisibility(View.GONE);
                emailValidationAlert.setVisibility(View.GONE);
                return false;
            }
            if (number.length() == 10) {
                if(number.substring(0, 1).matches("[6-9]")) {
                    emailID.requestFocus();
                    return true;
                }else{
                    mobileValidationAlert.setVisibility(View.VISIBLE);
                }
            }

        return false;
    }
    public static FragmentGetNewConnection newInstance(Bundle args) {
        FragmentGetNewConnection fragmentGetNewConnection = new FragmentGetNewConnection();
        fragmentGetNewConnection.setArguments(args);
        return fragmentGetNewConnection;
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (mobileNumber.isFocused()) {
                mobileValidationAlert.setVisibility(View.GONE);
            }
            if (!mobileNumber.getText().toString().isEmpty() && !name.getText().toString().isEmpty() && !emailID.getText().toString().isEmpty() &&!pinCode.getText().toString().isEmpty()) {
                if (mobileNumber.getText().toString().length() == 10 && name.getText().toString().length() >= 2 && emailID.getText().toString().length() >= 2 && pinCode.getText().toString().length() >= 6) {
                    continueButton.setBackgroundResource(R.drawable.rounded_corner_button_white);

                } else {
                    continueButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            }
            if (pinCode.isFocused()) {
                pinCodeValidationAlert.setVisibility(View.GONE);
            }
           /* if (!pinCode.getText().toString().isEmpty()) {
                if(pinCode.length()== 6){
                    continueButton.setBackgroundResource(R.drawable.rounded_corner_button_white);

                }else{
                    continueButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            } else {
                continueButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
            }*/
            if (name.isFocused()) {
                nameValidationAlert.setVisibility(View.GONE);
            }
            if (emailID.isFocused()) {
                emailValidationAlert.setVisibility(View.GONE);
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

}
