package com.myplex.myplex.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.CityListRequest;
import com.myplex.api.request.user.CountriesListRequest;
import com.myplex.api.request.user.StatesListRequest;
import com.myplex.api.request.user.UpdateProfileRequest;
import com.myplex.api.request.user.UserProfileRequest;
import com.myplex.model.CountriesData;
import com.myplex.model.CountriesResponse;
import com.myplex.model.UserProfileResponseData;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MandatoryProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mHeaderImageView;
    private ImageView mChannelImageView;
    private RelativeLayout mRootLayout;
    private TextView dobTv;
    private Spinner countrySpinner,stateSpinner,citySpinner;
    private Button updateButton;
    private Calendar mDobCalender;
    private List<CountriesData> countriesList = new ArrayList<>();
    private List<CountriesData> statesList = new ArrayList<>();
    private List<CountriesData> citiesList = new ArrayList<>();
    private String state, country,city;
    private EditText addressEt,pinCodeEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandatory_profile);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);

        mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);

        citySpinner=findViewById(R.id.citySpinner);
        dobTv = findViewById(R.id.dobEt);
        countrySpinner = findViewById(R.id.countrySpinner);
        stateSpinner = findViewById(R.id.stateSpinner);
        addressEt=findViewById(R.id.addressEt);
        pinCodeEt=findViewById(R.id.pinCodeEt);
        updateButton = findViewById(R.id.updateProfile);
        initUI();
    }

    private final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mDobCalender.set(Calendar.YEAR, year);
            mDobCalender.set(Calendar.MONTH, month);
            mDobCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate();
        }
    };

    private void updateDate() {
        dobTv.setText(Util.DateFormat.YEAR_MONTH_DAY_FORMATTER_NUM.format(mDobCalender.getTime()));
    }

    private View.OnClickListener mCloseAction = v -> {
        //finish();
        onBackPressed();
        //showOverFlowSettings(v);
    };

    private void initUI() {
        if (mDobCalender == null) {
            mDobCalender = Calendar.getInstance();
        }
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mToolbarTitle.setText("Mandatory Profile Update");
        mHeaderImageView.setOnClickListener(mCloseAction);
        updateButton.setOnClickListener(v -> {
            if (validateValues()) {
                updateUserData(citySpinner.getSelectedItem().toString(),
                        stateSpinner.getSelectedItem().toString(), dobTv.getText().toString(),addressEt.getText().toString(),
                        pinCodeEt.getText().toString(),"");
            }
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
                if (countriesList != null && countriesList.size() != 0) {
                    country = countriesList.get(position).name;
                    String code = getCountryCodeIndex(countriesList.get(position).name);
                    getStatesList(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (statesList != null && statesList.size() != 0) {
                    state = statesList.get(position).name;
                    String code = getStateCodeIndex(statesList.get(position).name);
                    getCitiesList(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                    if (responseData.result != null
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

    private void setData(UserProfileResponseData responseData) {
        if (responseData.result.profile.dob != null && !TextUtils.isEmpty(responseData.result.profile.dob)) {
            dobTv.setText(responseData.result.profile.dob);
        }
        if (responseData.result.profile.address != null && !TextUtils.isEmpty(responseData.result.profile.address)) {
            addressEt.setText(responseData.result.profile.address);
        }
        if (responseData.result.profile.pincode != null && !TextUtils.isEmpty(responseData.result.profile.pincode)) {
            pinCodeEt.setText(responseData.result.profile.pincode);
        }
        if (responseData.result.profile.locations.size() != 0) {
            country = responseData.result.profile.locations.get(0);
        }
        state = responseData.result.profile.state;
        city=responseData.result.profile.city;

        getCountriesList();
    }

    private void updateUserData(String city, String state, String dob,String address,String pinCode,String language) {
        UpdateProfileRequest.Params params = new UpdateProfileRequest.Params(countrySpinner.getSelectedItem().toString(), state, city, dob,
                address,pinCode,"");
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(params, new APICallback<UserProfileResponseData>() {
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
                    if (responseData.status != null && responseData.status.equals(APIConstants.SUCCESS)) {
                        FirebaseAnalytics.getInstance().setNameProperty(PrefUtils.getInstance().getPrefFullName());
                        AlertDialogUtil.showToastNotification(response.message());
                        Intent ip = new Intent();
                        ip.putExtra(FragmentCardDetailsDescription.IS_PROFILE_UPDATE_SUCCESS, true);
                        setResult(FragmentCardDetailsDescription.PROFILE_UPDATE_REQUEST, ip);
                        finish();
                    } else {
                        AlertDialogUtil.showToastNotification(response.message());
                    }
                } else {
                    if (response.message() != null && !TextUtils.isEmpty(response.message())) {
                        AlertDialogUtil.showToastNotification(response.message());
                    } else {
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


    private void getCountriesList() {
        CountriesListRequest countriesListRequest = new CountriesListRequest(new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().countries != null && response.body().countries.size() != 0) {
                    countriesList = response.body().countries;
                    ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(MandatoryProfileActivity.this, R.layout.spinner_item,
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

    private void getStatesList(String code) {
        StatesListRequest.Params params = new StatesListRequest.Params(code);
        StatesListRequest statesListRequest = new StatesListRequest(params, new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().states != null && response.body().states.size() != 0) {
                    statesList = response.body().states;
                    ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(MandatoryProfileActivity.this, R.layout.spinner_item,
                            getStatesListInString());
                    stateSpinner.setAdapter(statesAdapter);
                    if (state != null && !TextUtils.isEmpty(state)) {
                        stateSpinner.setSelection(getStateIndex(state));
                    } else {
                        stateSpinner.setSelection(0);
                        state = stateSpinner.getSelectedItem().toString();
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
                    citiesList = response.body().cities;
                    ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(MandatoryProfileActivity.this, R.layout.spinner_item,
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


    private boolean validateValues() {
        if (country == null || TextUtils.isEmpty(country)) {
            AlertDialogUtil.showToastNotification("Please select Country");
            return false;
        } else if (state == null || TextUtils.isEmpty(state)) {
            AlertDialogUtil.showToastNotification("Please select State");
            return false;
        } else if (city == null || TextUtils.isEmpty(city)) {
            AlertDialogUtil.showToastNotification("Please enter City name");
            return false;
        } else if (TextUtils.isEmpty(dobTv.getText().toString())) {
            AlertDialogUtil.showToastNotification("Please select Date of birth");
            return false;
        } else if (TextUtils.isEmpty(addressEt.getText().toString())) {
            AlertDialogUtil.showToastNotification("Please enter address");
            return false;
        } else if (TextUtils.isEmpty(pinCodeEt.getText().toString())) {
            AlertDialogUtil.showToastNotification("Please enter Pincode");
            return false;
        }
        return true;
    }

    private String getCountryCodeIndex(String country) {
        String code = null;
        for (int p = 0; p < countriesList.size(); p++) {
            if (countriesList.get(p).name.equalsIgnoreCase(country)) {
                code = countriesList.get(p).indexCode;
            }
        }
        return code;
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

    private List<String> getCountriesListInString() {
        List<String> countriesListNew = new ArrayList<>();
        for (int p = 0; p < countriesList.size(); p++) {
            countriesListNew.add(countriesList.get(p).name);
        }
        return countriesListNew;
    }

    private List<String> getStatesListInString() {
        List<String> statesListNew = new ArrayList<>();
        for (int p = 0; p < statesList.size(); p++) {
            statesListNew.add(statesList.get(p).name);
        }
        return statesListNew;
    }

    private List<String> getCityListInString() {
        List<String> statesListNew = new ArrayList<>();
        for (int p = 0; p < citiesList.size(); p++) {
            statesListNew.add(citiesList.get(p).name);
        }
        return statesListNew;
    }

    private int getCountryIndex(String countryName) {
        int index = 0;
        for (int p = 0; p < countriesList.size(); p++) {
            if (countryName.equalsIgnoreCase(countriesList.get(p).name)) {
                index = p;
            }
        }
        return index;
    }

    private int getStateIndex(String stateName) {
        int index = 0;
        for (int p = 0; p < statesList.size(); p++) {
            if (stateName.equalsIgnoreCase(statesList.get(p).name)) {
                index = p;
            }
        }
        return index;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}