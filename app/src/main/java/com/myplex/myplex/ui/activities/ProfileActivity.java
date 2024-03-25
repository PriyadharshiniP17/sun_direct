package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.UserProfileRequest;
import com.myplex.model.UserProfileResponseData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.util.SDKUtils;

import java.util.List;


/**
 * Created by THIVIKRAMREDDY on 1/19/2016.
 */
public class ProfileActivity extends BaseActivity {
    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mHeaderImageView;
    private ImageView mChannelImageView;
    private RelativeLayout mRootLayout;
    private ImageView mProfileImageView;
    private final int code= 100;
    static String selectedImagePath;
    public static final int edit_profile_code=1001;
    public static final int success=1002;
    public static final String IS_LOG_OUT_REQUEST="logout_request";
    private int enabledisablePlayerLogsClickCount = 0;

    private TextView mUserNameEt,mMobileEt,mEmailIdEt;

    private LinearLayout editDetailsLayout;
    private LinearLayout logoutLayout;
    private LinearLayout goPremiumLayout;

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
            //showOverFlowSettings(v);
        }
    };
    private View.OnClickListener mProfilePicBtnAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(intent, "Select File"),
                    code);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0,0);

        mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mChannelImageView = (ImageView)mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
//        mProfileImageView = (CircleImageView)findViewById(R.id.profile_img);
        mProfileImageView = (ImageView)findViewById(R.id.profile_img);
        mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enabledisablePlayerLogsClickCount++;
                if(enabledisablePlayerLogsClickCount % 6 == 0){

                    if (!ApplicationController.SHOW_PLAYER_LOGS) {
                        ApplicationController.SHOW_PLAYER_LOGS = true;
//                        PrefUtils.getInstance().setPrefIsExoplayerEnabled(true);
//                        PrefUtils.getInstance().setPrefIsExoplayerDvrEnabled(true);
                        if (ApplicationController.ENABLE_SAVE_LOGS_TO_FILE) {
                            SDKUtils.captureLogsToSDCard(ProfileActivity.this);
                        }
                        AlertDialogUtil.showToastNotification("Player logs and ad tags are enabled");
                    }else{
                        if (ApplicationController.ENABLE_SAVE_LOGS_TO_FILE) {
                            SDKUtils.deleteLogFile(ProfileActivity.this);
                        }
                        ApplicationController.SHOW_PLAYER_LOGS = false;
//                        PrefUtils.getInstance().setPrefIsExoplayerEnabled(false);
//                        PrefUtils.getInstance().setPrefIsExoplayerDvrEnabled(false);
                        AlertDialogUtil.showToastNotification("Player logs and ad tags are disabled");
                    }
                    PrefUtils.getInstance().setPlayerLogs(ApplicationController.SHOW_PLAYER_LOGS);
                }
            }
        });
        editDetailsLayout=findViewById(R.id.edit_details_layout);
        goPremiumLayout=findViewById(R.id.go_premium_layout);

        mUserNameEt=findViewById(R.id.nameEt);
        mMobileEt=findViewById(R.id.mobileEt);
        mEmailIdEt=findViewById(R.id.emailEt);
        LinearLayout mySubscriptionsLayout=findViewById(R.id.subscprtions_layout);
        logoutLayout=findViewById(R.id.logout_layout);

        mySubscriptionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,ActivityMyPacks.class));
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ip=new Intent();
                ip.putExtra(IS_LOG_OUT_REQUEST,true);
                setResult(edit_profile_code,ip);
                finish();
            }
        });

        goPremiumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ip=new Intent(ProfileActivity.this, SubscriptionWebActivity.class);
                ip.putExtra(SubscriptionWebActivity.IS_FROM_PREMIUM,true);
                startActivity(ip);
            }
        });

        List<String> subscribedCardDataPackages = ApplicationController.getCardDataPackages();
        if (subscribedCardDataPackages == null
                || subscribedCardDataPackages.isEmpty()) {
            goPremiumLayout.setVisibility(View.VISIBLE);
            mySubscriptionsLayout.setVisibility(View.GONE);
        }else {
            goPremiumLayout.setVisibility(View.GONE);
            mySubscriptionsLayout.setVisibility(View.VISIBLE);
        }
        mChannelImageView.setVisibility(View.GONE);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);

        initUI();
    }

    private void initUI() {
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mToolbarTitle.setText("Profile");
        mHeaderImageView.setOnClickListener(mCloseAction);
        //setProfilePic();

        editDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ProfileActivity.this,EditProfileActivity.class),edit_profile_code);
            }
        });
        getProfileDetails();
    }

    private void getProfileDetails() {
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
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
                    if ( responseData.result != null
                            && responseData.result.profile != null) {
                        setData(responseData);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Profile update onFailure");
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }

    private void setData(UserProfileResponseData responseData){
        if (responseData != null && responseData.result != null && responseData.result.profile != null) {

            if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                if (responseData.result.profile.locations.get(0) != null
                        && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                    PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
            }

            if (responseData.result.profile.state != null && !TextUtils.isEmpty(responseData.result.profile.state)) {
                PrefUtils.getInstance().setUserState(responseData.result.profile.state);
            }

            if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
                PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
            }

            if (responseData.result.profile.mobile_no != null && !TextUtils.isEmpty(responseData.result.profile.mobile_no)) {
                mMobileEt.setText(responseData.result.profile.mobile_no);
            }
            if (responseData.result.profile.emails != null && responseData.result.profile.emails.size() >0 && !TextUtils.isEmpty(responseData.result.profile.emails.get(0).email)) {
                mEmailIdEt.setText(responseData.result.profile.emails.get(0).email);
            }

            if (responseData.result.profile.first != null && !TextUtils.isEmpty(responseData.result.profile.first)) {
                mUserNameEt.setText(responseData.result.profile.first + " " + responseData.result.profile.last);
            } else {
                final String usrName = PrefUtils.getInstance().getPrefFullName();
                if (usrName != null && usrName.length() > 0) {
                    mUserNameEt.setText(usrName);
                }
            }
        }
    }

    @Override
    public void setOrientation(int value) {

    }

    @Override
    public int getOrientation() {
        return 0;
    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void showActionBar() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==edit_profile_code){
            if(resultCode==success){
                if (data!=null){
                    final String usrName  = PrefUtils.getInstance().getPrefFullName();
                    if(usrName!=null && usrName.length()>0){
                        mUserNameEt.setText(usrName);
                    }
                }
            }
        }else {
            try {
                // When an Image is picked
                Uri selectedImageUri = data.getData();
                String[] projection = { MediaStore.MediaColumns.DATA };
                CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null,
                        null);
                Cursor cursor =cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                selectedImagePath = cursor.getString(column_index);
                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
                mProfileImageView.setImageBitmap(bm);

            } catch (Exception e) {
            }
        }
    }

    private void setProfilePic(){
        String imagePath = PrefUtils.getInstance().getPrefProfilePic();
        if(imagePath == null || imagePath.isEmpty())
            return;
        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(imagePath, options);
        if(bm!=null)
        mProfileImageView.setImageBitmap(bm);
    }

    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, ProfileActivity.class);
        return i;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(edit_profile_code);
        finish();
    }
}
