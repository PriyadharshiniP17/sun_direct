package com.myplex.myplex.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.UpdatePasswordRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;

public class ActivityChangePassword extends AppCompatActivity {

    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mHeaderImageView;
    private ImageView mChannelImageView;
    private RelativeLayout mRootLayout;
    private EditText currentPwdET,newPwdET,confirmNewPwdET;
    private Button saveNewPwdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
      /*  if (DeviceUtils.isTablet(getApplicationContext())) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }*/

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0,0);

        mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mChannelImageView = (ImageView)mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);

//        currentPwdET=findViewById(R.id.currentPwdEditText);
        newPwdET=findViewById(R.id.newPWDEditText_feild);
        confirmNewPwdET=findViewById(R.id.confirmNewPwdEditText);
        saveNewPwdButton=(Button) findViewById(R.id.saveNewPassword);
        saveNewPwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidPasswords()){
                    updateUserPassword(currentPwdET.getText().toString(),newPwdET.getText().toString());
                }
            }
        });
        initUI();
    }

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
            //onBackPressed();
            //showOverFlowSettings(v);
        }
    };

    private void initUI() {
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mToolbarTitle.setText(getResources().getString(R.string.change_password));
        mHeaderImageView.setOnClickListener(mCloseAction);

    }


    private void updateUserPassword(final String currentPwd, String newPwd){
        UpdatePasswordRequest.Params params=new UpdatePasswordRequest.Params(currentPwd,newPwd);
        UpdatePasswordRequest updatePasswordRequest=new UpdatePasswordRequest(params,new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response == null || response.body() == null) {
                    AlertDialogUtil.showToastNotification(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL);
                    return;
                }
                if (response.body().code == 402) {
                    AlertDialogUtil.showToastNotification(response.message());
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    BaseResponseData responseData = response.body();
                    if(responseData.status!=null&&responseData.status.equals(APIConstants.SUCCESS)){
                        AlertDialogUtil.showToastNotification(response.message());
                        Intent ip=new Intent();
                        ip.putExtra(APIConstants.SUCCESS,APIConstants.SUCCESS);
                        setResult(ProfileActivity.success,ip);
                        finish();
                    }else {
                        AlertDialogUtil.showToastNotification(response.message());
                    }
                }else {
                    if (response.message()!=null&&!TextUtils.isEmpty(response.message())){
                        AlertDialogUtil.showToastNotification(response.message());
                    }else {
                        AlertDialogUtil.showToastNotification(getResources().getString(R.string.default_password_update_message));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                AlertDialogUtil.showToastNotification(t.getMessage());
                return;
            }
        });
        APIService.getInstance().execute(updatePasswordRequest);
    }

    private boolean isValidPasswords() {
        if(TextUtils.isEmpty(currentPwdET.getText().toString())){
            AlertDialogUtil.showToastNotification(getResources().getString(R.string.message_empty__current_pwd));
            return false;
        }
        if(TextUtils.isEmpty(newPwdET.getText().toString())){
            AlertDialogUtil.showToastNotification(getResources().getString(R.string.message_empty__new_pwd));
            return false;

        }
        if(TextUtils.isEmpty(confirmNewPwdET.getText().toString())){
            AlertDialogUtil.showToastNotification(getResources().getString(R.string.message_empty__confirm_pwd));
            return false;

        }

        if(!newPwdET.getText().toString().equals(confirmNewPwdET.getText().toString())){
            AlertDialogUtil.showToastNotification(getResources().getString(R.string.message_mismatch_pwd));
            return false;
        }
        return true;
    }


}