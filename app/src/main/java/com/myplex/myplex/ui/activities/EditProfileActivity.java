package com.myplex.myplex.ui.activities;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.UpdateProfileRequest;
import com.myplex.api.request.user.UserProfileRequest;
import com.myplex.model.CountriesData;
import com.myplex.model.ImageUploadResponse;
import com.myplex.model.UploadImage;
import com.myplex.model.UserProfileResponseData;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.FragmentChangeMobile;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditProfileActivity extends BaseActivity {

    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mHeaderImageView,editOption,editMobile;
    private ImageView editProfile;
    private  String imageUrl;
    private ImageView mChannelImageView;
    private RelativeLayout mRootLayout;
    private EditText nameEt,mobileEt,emailEt,genderEt,lastNameEt,cityEt;
    private ProgressBar progress;
    private TextView dobTv,smartCardEt;
    private BaseFragment mCurrentFragment;
    private Spinner ageSpinner,countrySpinner,stateSpinner,genderSpinner,citySpinner;
    private Button updateButton,changePassword;
    private Calendar mDobCalender;
    private List<CountriesData> countriesList=new ArrayList<>();
    private List<CountriesData> statesList=new ArrayList<>();
    private List<CountriesData> citiesList = new ArrayList<>();
    private List<String> gendersList=new ArrayList<>();
    private String state,country,city;
    private String mFirstName,mMobileNo,mSmartCardNumber,mEmailId;
    private TextView nameValid,mobileNumberValid,smartcardNumberValid,emailValid;
    public  static int PERMISSION_REQUEST_CODE  = 120;
    private String profileid = "";
    SharedPreferences sp;
    private String mobileNo, otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if (DeviceUtils.isTabletOrientationEnabled(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0,0);

        mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mChannelImageView = (ImageView)mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        progress =  findViewById(R.id.progress);

        nameEt = findViewById(R.id.nameEditProfile);
        lastNameEt = findViewById(R.id.lastnameEditProfile);
        mobileEt = findViewById(R.id.mobileEditProfile);
        emailEt = findViewById(R.id.emailEditProfile);
        genderEt = findViewById(R.id.genderEditProfile);
        ageSpinner = findViewById(R.id.ageSpinnerEditProfile);
        genderSpinner=findViewById(R.id.genderSpinner);

        citySpinner=findViewById(R.id.citySpinner);

        cityEt = findViewById(R.id.cityEdit);
        dobTv = findViewById(R.id.dobEt);
        countrySpinner = findViewById(R.id.countrySpinner);
        stateSpinner = findViewById(R.id.stateSpinner);
        updateButton = findViewById(R.id.updateProfile);
        changePassword = findViewById(R.id.changePassword);
        editProfile = findViewById(R.id.edit_profile_image);
        editOption = findViewById(R.id.add_icon);
        editMobile = findViewById(R.id.edit_mobile);

        smartCardEt = findViewById(R.id.smartcardnumber);
        smartCardEt.setKeyListener(null);
        nameValid = findViewById(R.id.name_valid);
        mobileNumberValid = findViewById(R.id.mobile_valid);
        smartcardNumberValid = findViewById(R.id.smartcard_valid);
        emailValid = findViewById(R.id.email_valid);
        ll_space_root = findViewById(R.id.ll_space_root);
        updateHorizontalSpacing();

        initUI();
        sp=getSharedPreferences("profilePicture",MODE_PRIVATE);

        if(!sp.getString("dp","").equals("")){
            byte[] decodedString = Base64.decode(sp.getString("dp", ""), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            editProfile.setImageBitmap(decodedByte);
        }
        nameEt.addTextChangedListener(textWatcher);
        mobileEt.addTextChangedListener(textWatcher);
        smartCardEt.addTextChangedListener(textWatcher);
        emailEt.addTextChangedListener(textWatcher);
        smartCardEt.addTextChangedListener(textWatcher);
        if(PrefUtils.getInstance().getEditMobileNumberEnabled()){
            editMobile.setVisibility(View.VISIBLE);
        }else{
            editMobile.setVisibility(View.GONE);
        }
        editMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mobileNo != null && !mobileNo.isEmpty()) {
                    Bundle args = new Bundle();
                    args.putString("mobile_number", mobileEt.getText().toString());
                    pushFragment(FragmentChangeMobile.newInstance(args));
                    return;
                }
            }
        });

        Intent intent = getIntent();
        String flickerImage = intent.getStringExtra("profile_url");
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(flickerImage)
                .fitCenter()
                .onlyRetrieveFromCache(false)
                .error(R.drawable.nav_drawer_profile_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .dontAnimate()
                .into(editProfile);
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

    TextWatcher textWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(nameEt.isFocused()){
                nameValid.setVisibility(View.GONE);
            }
            if(mobileEt.isFocused()){
                mobileNumberValid.setVisibility(View.GONE);
            }
            if(smartCardEt.isFocused()){
                smartcardNumberValid.setVisibility(View.GONE);
                updateButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
            }
            if(emailEt.isFocused()){
                emailValid.setVisibility(View.GONE);
            }
               if (!smartCardEt.getText().toString().isEmpty()) {
                if(smartCardEt.length() > 0 ){
                    updateButton.setBackgroundResource(R.drawable.rounded_corner_button_white);

                }else{
                    updateButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            } else {
                updateButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
            }

           /* if (!emailEt.getText().toString().isEmpty()) {
                if(emailEt.length() > 0 ){
                    updateButton.setBackgroundResource(R.drawable.rounded_corner_button_white);

                }else{
                    updateButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
                }
            }*/ /*else {
                updateButton.setBackgroundResource(R.drawable.rounded_corner_button_orange);
            }*/
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private final DatePickerDialog.OnDateSetListener date=new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mDobCalender.set(Calendar.YEAR,year);
            mDobCalender.set(Calendar.MONTH,month);
            mDobCalender.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateDate();
        }
    };

    private void updateDate(){
        dobTv.setText(Util.DateFormat.YEAR_MONTH_DAY_FORMATTER_NUM.format(mDobCalender.getTime()));
    }

    private View.OnClickListener mCloseAction = v -> {
        finish();
        //onBackPressed();
        //showOverFlowSettings(v);
    };

    private void initUI() {
        if (mDobCalender == null) {
            mDobCalender = Calendar.getInstance();
        }
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mToolbarTitle.setText(getResources().getString(R.string.account_settings_text));
//        mToolbarTitle.setTextSize((int)getResources().getDimension(R.dimen.textsize_5));
        mHeaderImageView.setOnClickListener(mCloseAction);

       /* ArrayAdapter<String> ageAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getAgesList());
        ageSpinner.setAdapter(ageAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getGendersList());
        genderSpinner.setAdapter(genderAdapter);*/
        updateButton.setOnClickListener(v -> {

            if (mobileEt.getText().toString().isEmpty()) {
                mobileNumberValid.setText("Mobile number field should not be empty");
                mobileNumberValid.setVisibility(View.VISIBLE);
                return;
            }
            if(mobileEt.getText().toString().length() != 10){
                mobileNumberValid.setText(R.string.smart_card_alert);
                mobileNumberValid.setVisibility(View.VISIBLE);
                return;
            }
           /* if (emailEt.getText().toString().isEmpty()) {
                emailValid.setText("Email field should not be empty");
                emailValid.setVisibility(View.VISIBLE);
                return;
            }*/
           /* if(emailValid.getText().toString().length() < 4 ){
                emailValid.setText(R.string.otp_msg_invalid_email_id);
                emailValid.setVisibility(View.VISIBLE);
                return;
            }*/
            //if(validateValues()){
           /* if (TextUtils.isEmpty(country) || country.equalsIgnoreCase("Select Country")){
                Toast.makeText(EditProfileActivity.this, "Please select Country", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(state) || state.equalsIgnoreCase("Select State")){
                Toast.makeText(EditProfileActivity.this, "Please select State", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(city) || city.equalsIgnoreCase("Select City")){
                Toast.makeText(EditProfileActivity.this, "Please select City", Toast.LENGTH_SHORT).show();
                return;
            }*/
//            selectImage();

            if(isValidateValues()) {
                if(mobileNo != null && !mobileNo.isEmpty() && !mobileNo.equals(mobileEt.getText().toString())) {
                    Bundle args = new Bundle();
                    args.putString("mobile_number", mobileEt.getText().toString());
                    pushFragment(FragmentChangeMobile.newInstance(args));
                    return;
                }
                    updateUserData(checkAndReturnEmptyValue(nameEt),
                            checkAndReturnEmptyValue(mobileEt),
                            checkAndReturnEmptyValues(smartCardEt),
                            checkAndReturnEmptyValue(emailEt));
                }
            //}
        });
        editOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                        String[] permissions;
                        permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
                        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                    } else {
                        selectImage();
                    }
                }

            }
        });
        changePassword.setOnClickListener(v -> {
            startActivity(new Intent(EditProfileActivity.this,ActivityChangePassword.class));
            finish();
        });
        dobTv.setOnClickListener(v -> {
            DatePickerDialog datePickerDialogs = new DatePickerDialog(this, date, mDobCalender
                    .get(Calendar.YEAR), mDobCalender.get(Calendar.MONTH),
                    mDobCalender.get(Calendar.DAY_OF_MONTH));
            datePickerDialogs.getDatePicker().setMaxDate(new Date().getTime());
            datePickerDialogs.show();
        });
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              /*  if (countriesList!=null&&countriesList.size()!=0){
                        country=countriesList.get(position).name;
                        countrySpinner.setSelection(getCountryIndex(country));
                        String code=getCountryCodeIndex(countriesList.get(position).name);
                        getStatesList(code);
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*if (statesList != null && statesList.size() != 0) {
                        state = statesList.get(position).name;
                    stateSpinner.setSelection(getStateIndex(state));
                    String code = getStateCodeIndex(statesList.get(position).name);
                        getCitiesList(code);
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*if (citiesList != null && citiesList.size() != 0) {
                        city = citiesList.get(position).name;
                        citySpinner.setSelection(getCityIndex(city));
                        //String code = getStateCodeIndex(statesList.get(position).name);
                        //getCitiesList(code);
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getProfileDetails();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       /* if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
//            selectImage();
        }*/
        switch (requestCode){
            case 120: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                } else if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Allow permission for data access from Settings", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Allow permission for data access from Settings", Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void pushFragment(BaseFragment fragment) {
        if (isFinishing() || fragment == null) {
            return;
        }
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            removeFragment(mCurrentFragment);
            transaction.add(R.id.container, fragment);
            mCurrentFragment = fragment;
            fragment.setBaseActivity(this);
            fragment.setContext(this);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        } catch (Throwable e) {
            e.printStackTrace();
        }

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

    public void sendImageToServer(Bitmap thumbnail,String isRemove) {
        progress.setVisibility(View.VISIBLE);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.WEBP, 100, bytes);
        File destination = new File(getExternalFilesDir(null),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
            File file = new File(destination.getPath());
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), destination);
            RequestBody photo = RequestBody.create(MediaType.parse("application/image"), destination);
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("imageFile", destination.getName(), reqFile)
                    .addFormDataPart("removeProfilePicture",isRemove)
                    .build();

          /*  String user = PrefUtils.getInstance().getString("PREF_USER_ID");
            if (profileid != null && !TextUtils.isEmpty(user)) {
                if (!profileid.equalsIgnoreCase(PrefUtils.getInstance().getString("PREF_USER_ID")))
                    user = profileid;
                else
                    user = PrefUtils.getInstance().getString("PREF_USER_ID");
            } else
                user = PrefUtils.getInstance().getString("PREF_USER_ID");*/

            UploadImage addNewProfile = new UploadImage(body,
                    profileid,
                    new APICallback<ImageUploadResponse>() {
                        @Override
                        public void onResponse(APIResponse<ImageUploadResponse> response) {
                            progress.setVisibility(View.GONE);
                           // dismissProgressBar();
                            if (response.body() != null && response.isSuccess()) {
                              //  LogUtils.debug("Result", "" + response.body().getCode());
                                if (response.body().getCode() == 200) {
                                    Toast.makeText(getApplicationContext(), response.body().message, Toast.LENGTH_SHORT).show();
                                    String url = response.body().getResults();
                                    imageUrl =url;
                                    if(TextUtils.isEmpty(imageUrl)) {
                                        url=PrefUtils.getInstance().getDefaultProfileImage();
                                    }


                                   /* if (TextUtils.isEmpty(url)) {
                                        editProfile.setImageResource(R.drawable.nav_drawer_profile_thumbnail);
                                        editProfile.setTag("");
                                    } else {
                                        if (Util.isValidContextForGlide(getApplicationContext()))
                                            Glide.with(getApplicationContext())
                                                    .load(url)
                                                    .placeholder(R.drawable.nav_drawer_profile_thumbnail)
                                                    .error(R.drawable.nav_drawer_profile_thumbnail)
                                                    .dontAnimate()
                                                    // .apply(RequestOptions.bitmapTransform(new BlurTransformation(50)))
                                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                                    .into(editProfile);
                                    }*/
                                   /* try {
                                        sp.edit().putString("dp", "").commit();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }*/
                                    String currentUser = PrefUtils.getInstance().getString("PREF_USER_ID");
//                                    PrefUtils.getInstance().setString("IMAGE_URL", url);
                                    if (!TextUtils.isEmpty(currentUser) && currentUser.equalsIgnoreCase(profileid)) {
                                        PrefUtils.getInstance().setString("PROFILE_IMAGE_URL", url);
                                    }
                                    if (TextUtils.isEmpty(url)) {
                                            editProfile.setImageResource(R.drawable.nav_drawer_profile_thumbnail);
                                            editProfile.setTag("");
                                            imageUrl = "";
                                        } else {
                                            editProfile.setImageBitmap(thumbnail);
                                           /* Glide.with(getApplicationContext())
                                                    .asBitmap()
                                                    .load(url)
                                                    .placeholder(R.drawable.nav_drawer_profile_thumbnail)
                                                    .error(R.drawable.nav_drawer_profile_thumbnail)
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .skipMemoryCache(true)
                                                    .dontAnimate()
                                                    .into(new SimpleTarget<Bitmap>() {

                                                        @Override
                                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                            editProfile.setImageBitmap(resource);
                                                        }
                                                    });*/
                                          /*  Glide.with(getApplicationContext())
                                                    .asBitmap()
                                                    .load(url)
                                                    .placeholder(R.drawable.nav_drawer_profile_thumbnail)
                                                    .error(R.drawable.nav_drawer_profile_thumbnail)
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .skipMemoryCache(true)
                                                    .dontAnimate()
                                                    .into(new SimpleTarget<Bitmap>() {

                                                        @Override
                                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                            editProfile.setImageBitmap(resource);
                                                        }
                                                    });*/
                                        }
                                        //ScopedBus.getInstance().post(new ProfilePicSetting(url));
                                       // ((ProfileActivity) (getActivity())).updateProfileImages();
                                //    }
                                } else
                                    Toast.makeText(getApplicationContext(), response.body().message, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                          //  dismissProgressBar();
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
            APIService.getInstance().execute(addNewProfile);
        } catch (FileNotFoundException e) {
          //  dismissProgressBar();
            progress.setVisibility(View.GONE);
            e.printStackTrace();
        } catch (IOException e) {
           // dismissProgressBar();
            progress.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }
    private void setData(UserProfileResponseData responseData){
        if (responseData != null && responseData.result != null && responseData.result.profile != null) {
            if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                if (responseData.result.profile.locations.get(0) != null
                        && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                    PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
            }

            if (responseData.result.profile.name != null && !TextUtils.isEmpty(responseData.result.profile.name)) {
                nameEt.setText(responseData.result.profile.name);
                nameEt.setSelection(responseData.result.profile.name.length());
            }
             profileid = "" + responseData.result.profile._id;

            if (responseData.result.profile.mobile_no != null && !TextUtils.isEmpty(responseData.result.profile.mobile_no)) {
                mobileEt.setText(responseData.result.profile.mobile_no);
                mobileNo = responseData.result.profile.mobile_no;
            }
            if (responseData.result.profile.email != null && !TextUtils.isEmpty(responseData.result.profile.email)) {
                emailEt.setText(responseData.result.profile.email);
            }
            if (responseData.result.profile.smc_no != null && !TextUtils.isEmpty(responseData.result.profile.smc_no)) {
                smartCardEt.setText(responseData.result.profile.smc_no);
            }

         /*   String url = PrefUtils.getInstance().getString("IMAGE_URL");
            if(TextUtils.isEmpty(url)) {
                url=PrefUtils.getInstance().getDefaultProfileImage();
            }*/
            if (responseData.result.profile.profile_image != null && !TextUtils.isEmpty(responseData.result.profile.profile_image)) {
                imageUrl = responseData.result.profile.profile_image;

                if (TextUtils.isEmpty(imageUrl)) {
                    editProfile.setImageResource(R.drawable.nav_drawer_profile_thumbnail);
                    editProfile.setTag("");
                } else {
               /* if (Util.isValidContextForGlide(getApplicationContext()))
                    Glide.with(getApplicationContext())
                            .load(url)
                            .placeholder(R.drawable.nav_drawer_profile_thumbnail)
                            .error(R.drawable.nav_drawer_profile_thumbnail)
                            .dontAnimate()
                            // .apply(RequestOptions.bitmapTransform(new BlurTransformation(50)))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into(editProfile);*/

                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(imageUrl)
                            .fitCenter()
                            .onlyRetrieveFromCache(false)
                            .error(R.drawable.nav_drawer_profile_thumbnail)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                            .dontAnimate()
                            .into(editProfile);
//                                                .placeholder(R.drawable.nav_drawer_profile_thumbnail)

                            /*.into(new SimpleTarget<Bitmap>() {

                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    editProfile.setImageBitmap(resource);
                                }
                            });*/

                  //  PicassoUtil.with(getApplicationContext()).load(url, editProfile, R.drawable.nav_drawer_profile_thumbnail);
                }
            }else{
                editProfile.setImageResource(R.drawable.nav_drawer_profile_thumbnail);
            }

        /*    if (responseData.result.profile.state != null && !TextUtils.isEmpty(responseData.result.profile.state)) {
                PrefUtils.getInstance().setUserState(responseData.result.profile.state);
            }

            if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
                PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
            }
            if (responseData.result.profile.last != null && !TextUtils.isEmpty(responseData.result.profile.last)) {
                lastNameEt.setText(responseData.result.profile.last);
                lastNameEt.setSelection(responseData.result.profile.last.length());
            }
            if (responseData.result.profile.gender != null && !TextUtils.isEmpty(responseData.result.profile.gender)) {
                if (responseData.result.profile.gender.equalsIgnoreCase("M")) {
                    genderSpinner.setSelection(1);
                } else if (responseData.result.profile.gender.equalsIgnoreCase("F")) {
                    genderSpinner.setSelection(2);
                } else {
                    genderSpinner.setSelection(0);
                }
            }
            if (responseData.result.profile.age != null && !TextUtils.isEmpty(responseData.result.profile.age)) {
                ageSpinner.setSelection(getIndex(responseData.result.profile.age));
            } else {
                ageSpinner.setSelection(0);
            }

            if (responseData.result.profile.dob != null && !TextUtils.isEmpty(responseData.result.profile.dob)) {
                dobTv.setText(responseData.result.profile.dob);
            }

            if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
                cityEt.setText(responseData.result.profile.city);
                city = responseData.result.profile.city;
            }

            if (responseData.result.profile.emails != null && responseData.result.profile.emails.size() >0 && !TextUtils.isEmpty(responseData.result.profile.emails.get(0).email)) {
                emailEt.setText(responseData.result.profile.emails.get(0).email);
            }
            if (responseData.result.profile.locations.size() != 0) {
                country = responseData.result.profile.locations.get(0);
            }
            state = responseData.result.profile.state;*/
           // getCountriesList();
        }
    }


    private String checkAndReturnEmptyValue(EditText mEditValue){
        if(mEditValue.getText().toString()==null||TextUtils.isEmpty(mEditValue.getText().toString())){
            return "";
        }
        return mEditValue.getText().toString();
    }
    private String checkAndReturnEmptyValues(TextView mEditValues){
        if(mEditValues.getText().toString()==null||TextUtils.isEmpty(mEditValues.getText().toString())){
            return "";
        }
        return mEditValues.getText().toString();
    }
/*
    private String getGender(Spinner genderSpinner) {
        if (genderSpinner.getSelectedItem().toString().equalsIgnoreCase("Male")){
            return "M";
        }else if (genderSpinner.getSelectedItem().toString().equals("Female")){
            return "F";
        }
        return null;
    }


    private void getCountriesList(){
        CountriesListRequest countriesListRequest=new CountriesListRequest(new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().countries != null && response.body().countries.size()!= 0){
                    countriesList.clear();
                    countriesList.add(new CountriesData("NA","NA","Select Country"));
                    countriesList.addAll(response.body().countries);
                    ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(EditProfileActivity.this, R.layout.spinner_item,
                            getCountriesListInString());
                    countrySpinner.setAdapter(countriesAdapter);
                    if (country != null && !TextUtils.isEmpty(country)) {
                        countrySpinner.setSelection(getCountryIndex(country));
                    }
                    getStatesList(getCountryCodeIndex(country));
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(countriesListRequest);
    }

    private void getStatesList(String code){
        StatesListRequest.Params params=new StatesListRequest.Params(code);
        StatesListRequest statesListRequest=new StatesListRequest(params,new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().states != null && response.body().states.size()!= 0){
                    statesList.clear();
                    statesList.add(new CountriesData("NA","NA","Select State"));
                    statesList.addAll(response.body().states);
                    ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(EditProfileActivity.this,
                            R.layout.spinner_item,
                            getStatesListInString());
                    stateSpinner.setAdapter(statesAdapter);
                    if (state!=null&&!TextUtils.isEmpty(state)){
                        stateSpinner.setSelection(getStateIndex(state));
                    }else {
                        stateSpinner.setSelection(0);
                        state=stateSpinner.getSelectedItem().toString();
                    }
                    getCitiesList(getStateCodeIndex(state));
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(statesListRequest);
    }


    private void getCitiesList(String code) {
        CityListRequest.Params params = new CityListRequest.Params(code);
        CityListRequest statesListRequest = new CityListRequest(params, new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().cities != null && response.body().cities.size() != 0) {
                    citiesList.clear();
                    citiesList.add(new CountriesData("NA","NA","Select City"));
                    citiesList.addAll(response.body().cities);
                    ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(EditProfileActivity.this, R.layout.spinner_item,
                            getCityListInString());
                    citySpinner.setAdapter(statesAdapter);
                    if (city != null && !TextUtils.isEmpty(city)) {
                        citySpinner.setSelection(getCityIndex(city));
                    } else {
                        citySpinner.setSelection(0);
                        city = citySpinner.getSelectedItem().toString();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(statesListRequest);
    }

    private String getStateCodeIndex(String country) {
        String code = null;
        for (int p = 0; p < statesList.size(); p++) {
            if (statesList.get(p).name.equalsIgnoreCase(country)) {
                code = statesList.get(p).code;
            }
        }
        return code;
    }


    private int getCityIndex(String cityName) {
        int index = 0;
        for (int p = 0; p < citiesList.size(); p++) {
            if (cityName.equalsIgnoreCase(citiesList.get(p).name)) {
                index = p;
            }
        }
        return index;
    }

    private List<String> getCityListInString() {
        List<String> statesListNew = new ArrayList<>();
        for (int p = 0; p < citiesList.size(); p++) {
            statesListNew.add(citiesList.get(p).name);
        }
        return statesListNew;
    }

    private int getIndex(String myString) {
        List<String> ageLists = new ArrayList<String>();
        ageLists=getAgesList();
        int index = 0;
        for (int i = 0; i < ageLists.size(); i++) {
            if (ageLists.get(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }


    private boolean validateValues(){
        if (TextUtils.isEmpty(nameEt.getText().toString())){
            AlertDialogUtil.showToastNotification("Please enter first name");
            return false;
        }else if (TextUtils.isEmpty(lastNameEt.getText().toString())){
            AlertDialogUtil.showToastNotification("Please enter last name");
            return false;
        }*//*else if(ageSpinner.getSelectedItem().toString().equals("Select Age")){
            AlertDialogUtil.showToastNotification("Please select age");
            return false;
        }*//*
        else if(genderSpinner.getSelectedItem().toString().equals("Select Gender")){
            AlertDialogUtil.showToastNotification("Please select gender");
            return false;
        }else if (country == null || TextUtils.isEmpty(country)) {
            AlertDialogUtil.showToastNotification("Please select Country");
            return false;
        } else if (state == null || TextUtils.isEmpty(state)||state.equalsIgnoreCase("Select State")) {
            AlertDialogUtil.showToastNotification("Please select State");
            return false;
        }else if (TextUtils.isEmpty(dobTv.getText().toString())){
            AlertDialogUtil.showToastNotification("Please select Date of birth");
            return false;
        } else if (city == null || TextUtils.isEmpty(city)) {
            AlertDialogUtil.showToastNotification("Please select City name");
            return false;
        }
        return true;
    }

    private String getCountryCodeIndex(String country){
        String code = null;
        for (int p=0;p<countriesList.size();p++){
            if(countriesList.get(p).name.equalsIgnoreCase(country)){
                code=countriesList.get(p).indexCode;
            }
        }
        return code;
    }

    private List<String> getAgesList(){
        String[] ageRange = PrefUtils.getInstance().getUserAgeRange().split(",");
        List<String> ageLists = new ArrayList<String>(Arrays.asList(ageRange));
        ageLists.add(0, "Select Age");
        return  ageLists;
    }

    private List<String> getGendersList(){
        List<String> ageLists = new ArrayList<>();
        ageLists.add(0, "Select Gender");
        ageLists.add(1,"Male");
        ageLists.add(2,"Female");
        return  ageLists;
    }

    private List<String> getCountriesListInString(){
        List<String> countriesListNew=new ArrayList<>();
        for (int p=0;p<countriesList.size();p++){
            countriesListNew.add(countriesList.get(p).name);
        }
        return countriesListNew;
    }

    private List<String> getStatesListInString(){
        List<String> statesListNew=new ArrayList<>();
        for (int p=0;p<statesList.size();p++){
            statesListNew.add(statesList.get(p).name);
        }
        //statesListNew.add(0,"Select State");
        return statesListNew;
    }

    private int getCountryIndex(String countryName){
        int index=0;
        for (int p=0;p<countriesList.size();p++){
            if (countryName.equalsIgnoreCase(countriesList.get(p).name)){
                index=p;
            }
        }
        return index;
    }

    private int getStateIndex(String stateName){
        int index=0;
        for (int p=0;p<statesList.size();p++){
            if (stateName.equalsIgnoreCase(statesList.get(p).name)){
                index=p;
            }
        }
        return index;
    }*/

    private void updateUserData(final String name, String mobile, String smc, String email){
        UpdateProfileRequest.Params params = new UpdateProfileRequest.Params(name, mobile, email, smc);
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
                        if (name!=null){
                            PrefUtils.getInstance().setPrefFullName(name.trim());
                            FirebaseAnalytics.getInstance().setNameProperty(PrefUtils.getInstance().getPrefFullName());
                        }
                        if (mobile != null) {
                            PrefUtils.getInstance().setPrefMobileNumber(mobile);
                        }
                        AlertDialogUtil.showToastNotification(response.message());
                        Intent ip=new Intent();
                        ip.putExtra(APIConstants.SUCCESS,APIConstants.SUCCESS);
                        ip.putExtra("profile_url", imageUrl);
                        setResult(ProfileActivity.success,ip);
                        finish();
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
    private void selectImage() {
        final CharSequence[] options ;
        if(imageUrl != null && !imageUrl.isEmpty())
            options =  new CharSequence[]{ "Take Photo", "Choose from Gallery","Remove photo","Cancel"};
        else
            options =  new CharSequence[]{ "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AppCompatEditProfileAlertDialogStyle));
        builder.setCustomTitle(getLayoutInflater().inflate(R.layout.editprofile_dailog,null));

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));



                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, 2);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
                else if(options[item].equals("Remove photo")){
                    dialog.dismiss();
                    removeImage();
                }
            }
        });
        builder.show();
    }



    private void removeImage() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.nav_drawer_profile_thumbnail);
        if(!TextUtils.isEmpty(PrefUtils.getInstance().getDefaultProfileImage())){
            try {
                bm= Util.getImageBitmapFromURL(getApplicationContext(),PrefUtils.getInstance().getDefaultProfileImage());
            } catch (Exception e) {
                e.printStackTrace();
                bm = BitmapFactory.decodeResource(getResources(), R.drawable.nav_drawer_profile_thumbnail);
            }
        }
        sendImageToServer(bm,"true");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                onCaptureImageResult(data);
             /*   File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);
                    editProfile.setImageBitmap(bitmap);
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            } else if (requestCode == 2) {
              /*  Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                editProfile.setImageBitmap(thumbnail);*/
                onSelectFromGalleryResult(data);
            }
            else if(requestCode==3){
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        if (data != null && data.getExtras() != null) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            sendImageToServer(thumbnail,"false");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            sp.edit().putString("dp", encodedImage).commit();
            //editProfile.setImageBitmap(thumbnail);
        }
    }
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                sendImageToServer(bm,"false");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                sp.edit().putString("dp", encodedImage).commit();
               // editProfile.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //ivImage.setImageBitmap(bm);
    }
    private boolean isValidateValues() {
        // read mobile number and email ID's and validate for API request.
        LoggerD.debugOTP("showAndProceedStep1SignInRequest");

        mFirstName = nameEt.getText().toString();
        mFirstName = mFirstName.trim();
        if(mFirstName.isEmpty()){
            nameValid.setVisibility(View.VISIBLE);
            nameValid.setText("Please enter your name");
        }
        if (!isValidFirstName(mFirstName)) {
            //  mMobileNoEditText.clearFocus();
            // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            nameValid.setVisibility(View.VISIBLE);
            mobileNumberValid.setVisibility(View.GONE);
            smartcardNumberValid.setVisibility(View.GONE);
//            nameEt.requestFocus();
            emailValid.setVisibility(View.GONE);
//            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
            //          CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return false;
        }
        mMobileNo = mobileEt.getText().toString();
        mMobileNo = mMobileNo.trim();
        if (!isValidMobile(mMobileNo)) {
            //  mMobileNoEditText.clearFocus();
            nameValid.setVisibility(View.GONE);
            mobileNumberValid.setVisibility(View.VISIBLE);
            smartcardNumberValid.setVisibility(View.GONE);
//            mobileEt.requestFocus();
            emailValid.setVisibility(View.GONE);
            return false;

        }
        mSmartCardNumber = smartCardEt.getText().toString();
        mSmartCardNumber = mSmartCardNumber.trim();

        if (!isValidSmartCard(mSmartCardNumber)) {
            //  mMobileNoEditText.clearFocus();
            nameValid.setVisibility(View.GONE);
            mobileNumberValid.setVisibility(View.GONE);
            smartcardNumberValid.setVisibility(View.VISIBLE);
//            smartCardEt.requestFocus();
            emailValid.setVisibility(View.GONE);
        // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            //          CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return false;
        }
        mEmailId = emailEt.getText().toString();
        mEmailId = mEmailId.trim();
       /* if(!isValidEmailID(mEmailId)){
                //  mMobileNoEditText.clearFocus();
                nameValid.setVisibility(View.GONE);
                mobileNumberValid.setVisibility(View.GONE);
                smartcardNumberValid.setVisibility(View.GONE);
                emailValid.setVisibility(View.VISIBLE);
//                emailEt.requestFocus();
                return false;
        }*/
//        mMobileNo =mobileEt .getText().toString();
//        mMobileNo = mMobileNo.trim();
       /* if (!isValidMobile(mMobileNo)) {
            //  mMobileNoEditText.clearFocus();
            // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            mobileNumberValid.setVisibility(View.VISIBLE);
            nameValid.setVisibility(View.GONE);
            smartcardNumberValid.setVisibility(View.GONE);
            emailValid.setVisibility(View.GONE);
//            AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_email_id));
            //          CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }*/
      /*  if (mMobileNo.length() < 10) {
            //  mMobileNoEditText.clearFocus();
            nameValid.setVisibility(View.GONE);
            mobileNumberValid.setVisibility(View.VISIBLE);
            smartcardNumberValid.setVisibility(View.GONE);
            emailValid.setVisibility(View.GONE);

            // AlertDialogUtil.showToastNotification(mContext.getString(R.string.otp_msg_invalid_mobile_no));
            //          CleverTap.eventRegistrationInitiated(mEmailID != null ? mEmailID.trim() : null, mMobileNo != null ? mMobileNo.toLowerCase().trim() : null, APIConstants.FAILED, Analytics.INCORRECT_EMAIL_ID);
            return;
        }

*/
        return true;
    }
    private boolean isValidFirstName(String firstName) {
        if (firstName == null || TextUtils.isEmpty(firstName)) {
            return false;
        }

        if (firstName.length() > 0) {
            if(firstName.matches("^[a-zA-Z ]+$")){
                mobileNumberValid.setVisibility(View.GONE);
                smartcardNumberValid.setVisibility(View.GONE);
                emailValid.setVisibility(View.GONE);
                nameValid.setVisibility(View.GONE);
//                mobileEt.requestFocus();
                return true;
            }else{
                nameValid.setText("Please enter a valid name");
                nameValid.setVisibility(View.VISIBLE);
            }
        }
            /*int lengthFromDot = 0;
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
    private boolean isValidMobile(String mobileNumber) {
        if (mobileNumber == null || TextUtils.isEmpty(mobileNumber)) {
            return false;
        }

        if (mobileNumber.length() > 0) {
            if (mobileNumber.length() == 10) {
                if (mobileNumber.substring(0, 1).matches("[6-9]")) {
                    mobileNumberValid.setVisibility(View.GONE);
                    smartcardNumberValid.setVisibility(View.GONE);
                    emailValid.setVisibility(View.GONE);
//                    smartCardEt.requestFocus();
                    return true;
                }else{
                    mobileNumberValid.setVisibility(View.VISIBLE);
                }
            }
        }
        return false;
    }
    private boolean isValidSmartCard(String smartCard) {
        if (smartCard == null || TextUtils.isEmpty(smartCard)) {
            return false;
        }

        if(smartCard.length() > 11 && smartCard.length()<11) {
            return false;
        }
        if(smartCard.length() == 11){
            if(PrefUtils.getInstance().getPrefSmartCardNumber() != null)
            if(smartCard.equals(PrefUtils.getInstance().getPrefSmartCardNumber())) {
                return true;
            }
        }
        return false;
    }
    private boolean isValidEmailID(String emailId) {
        if (emailId == null || TextUtils.isEmpty(emailId)) {
            return false;
        }
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (emailId.length() > 0) {
            if(emailId.matches(emailPattern)) {
               // emailEt.requestFocus();
                return true;
            }else {
                emailValid.setVisibility(View.VISIBLE);
                return false;
            }
        }

            return false;
        }
            /*int lengthFromDot = 0;
            if (emailId.indexOf(".") >= 0 && emailId.substring(emailId.indexOf(".")) != null) {
                lengthFromDot = emailId.substring(emailId.indexOf(".")).length();
                LoggerD.debugDownload("lengthFromDot- " + lengthFromDot + " emailId.substring(emailId.indexOf(\".\"))- " + emailId.substring(emailId.indexOf(".")));
            }
            if (emailId.contains("@") && emailId.contains(".") && !emailId.contains(" ") && lengthFromDot > 2) {
                return true;
            }
        }*/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateHorizontalSpacing();

    }
    int portraitWidth;
    LinearLayout ll_space_root;
    private void updateHorizontalSpacing() {
        if(DeviceUtils.isTablet(EditProfileActivity.this)){
            if(DeviceUtils.getScreenOrientation(EditProfileActivity.this) != SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                if(portraitWidth <= 0)
                    portraitWidth = ll_space_root.getLayoutParams().width;
                ll_space_root.getLayoutParams().width =portraitWidth;
            }else {
                ll_space_root.getLayoutParams().width = (int)(0.45 * getResources().getDisplayMetrics().widthPixels);
            }

        }
    }

}


