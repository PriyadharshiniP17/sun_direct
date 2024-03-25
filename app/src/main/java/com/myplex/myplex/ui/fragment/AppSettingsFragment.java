package com.myplex.myplex.ui.fragment;

import static android.view.View.GONE;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.LanguageListRequest;
import com.myplex.model.LanguageListResponse;
import com.myplex.model.Term;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.model.SelectQuality;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.SettingsActivity;
import com.myplex.myplex.ui.views.AboutDialogWebView;
import com.myplex.myplex.utils.CustomGridView;
import com.myplex.myplex.utils.ParentalControlDialog;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Neosoft on 7/10/2017.
 */

public class AppSettingsFragment extends BaseFragment implements View.OnClickListener {

    private LinearLayout linearVideoPlaybackQuality;
    private TextView textVideoPlaybackQuality;
    private TextView selected_video_quality;
    private RadioGroup radioGroupQuality;
    private RadioButton radioButtonQualityAuto;
    private RadioButton radioButtonQualityHd;
    private RadioButton radioButtonQualitySd;
    private RadioButton radioButtonQualityLow;
    private CheckBox checkboxNotificationToggle;
    private CheckBox checkboxAutoPlayToggle;
    private CheckBox checkboxDownloadOnWifiToggle;
    private TextView textDownloadLocation;
    private LinearLayout linearParentalControl;
    private TextView textParentalControl;
    private RadioGroup radioGroupParentalControl;
    private RadioButton radioButtonParentalOff;
    private RadioButton radioButtonParentalAll;
    private RadioButton radioButtonParentalAdult;
    private Spinner mSpinnerQuality;
    private LinearLayout video_quality_ll;
    private RelativeLayout video_quality_popup;
    private ArrayList<SelectQuality> qualityList;
    private ArrayList<SelectQuality> selectedArray = new ArrayList<>();
    private CustomGridView video_quality_list;
    VideoQualityAdapter smartCardRecyclerViewAdapter;
    private ImageView more_arrow;
    private FrameLayout language_ll,unregistered_smc;
    TextView mTextAbout;
    private CheckBox live_channel_as_home_page;
    ArrayList<String> items = new ArrayList<>();
    public int selectedPosition;
    public String selectedQuality;
    public int positionSelected = -1;
    public boolean isChecked = false;
    TextView selected_language;
    private ProgressDialog mProgressDialog;
    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_app_settings_section, container, false);
        initUI(rootView, inflater);
        initUIAbout(rootView);
        return rootView;
    }

    private void initUI(View rootView, LayoutInflater inflater) {
        qualityList = new ArrayList<>();
        linearVideoPlaybackQuality = (LinearLayout) rootView.findViewById(R.id.linear_video_playback_quality);
        language_ll = (FrameLayout) rootView.findViewById(R.id.language_ll);
        unregistered_smc = (FrameLayout) rootView.findViewById(R.id.unregistered_smc);
        video_quality_ll = (LinearLayout) rootView.findViewById(R.id.video_quality_ll);
        video_quality_popup = (RelativeLayout) rootView.findViewById(R.id.video_quality_popup);
        textVideoPlaybackQuality = (TextView) rootView.findViewById(R.id.text_video_playback_quality);
        selected_video_quality = (TextView) rootView.findViewById(R.id.selected_video_quality);
        more_arrow = (ImageView) rootView.findViewById(R.id.more_arrow);
        video_quality_list = (CustomGridView) rootView.findViewById(R.id.video_quality_grid_list);
        video_quality_list.setExpanded(true);
        // selected_video_quality.setText(PrefUtils.getInstance().getContentVideoQuality() != null ? PrefUtils.getInstance().getContentVideoQuality():"Auto" );
        radioGroupQuality = (RadioGroup) rootView.findViewById(R.id.radio_group_quality);
        radioButtonQualityAuto = (RadioButton) rootView.findViewById(R.id.radio_button_quality_auto);
        radioButtonQualityAuto.setText("Auto");
        radioButtonQualityHd = (RadioButton) rootView.findViewById(R.id.radio_button_quality_hd);
        radioButtonQualityHd.setText("High");
        radioButtonQualitySd = (RadioButton) rootView.findViewById(R.id.radio_button_quality_sd);
        radioButtonQualitySd.setText("Medium");
        radioButtonQualityLow = (RadioButton) rootView.findViewById(R.id.radio_button_quality_low);
        radioButtonQualityLow.setText("Low");
        checkboxNotificationToggle = (CheckBox) rootView.findViewById(R.id.checkbox_notification_toggle);
        checkboxAutoPlayToggle = (CheckBox) rootView.findViewById(R.id.checkbox_auto_play_toggle);
        checkboxDownloadOnWifiToggle = (CheckBox) rootView.findViewById(R.id.checkbox_download_on_wifi_toggle);
        textDownloadLocation = (TextView) rootView.findViewById(R.id.text_download_location);
        linearVideoPlaybackQuality = (LinearLayout) rootView.findViewById(R.id.linear_video_playback_quality);
        FrameLayout fl_audio_quality = rootView.findViewById(R.id.fl_audio_quality);
        selected_language = rootView.findViewById(R.id.selected_language);
        fl_audio_quality.setOnClickListener(this);
        linearVideoPlaybackQuality.setOnClickListener(this);
        unregistered_smc.setOnClickListener(this);
        live_channel_as_home_page = (CheckBox) rootView.findViewById(R.id.checkbox_channel_as_homepage__toggle);
        if (PrefUtils.getInstance().getBoolean(PrefUtils.PREF_APPS_AS_HOME, false)) {
            live_channel_as_home_page.setChecked(true);
        } else {
            live_channel_as_home_page.setChecked(false);
        }
        live_channel_as_home_page.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    PrefUtils.getInstance().setBoolean(PrefUtils.PREF_APPS_AS_HOME, isChecked);
                else
                    PrefUtils.getInstance().setBoolean(PrefUtils.PREF_APPS_AS_HOME, isChecked);
            }
        });

        linearParentalControl = (LinearLayout) rootView.findViewById(R.id.linear_parental_control);
        mSpinnerQuality = (Spinner) rootView.findViewById(R.id.spinnerQuality);
        textParentalControl = (TextView) rootView.findViewById(R.id.text_parental_control);
        radioGroupParentalControl = (RadioGroup) rootView.findViewById(R.id.radio_group_parental_control);
        radioButtonParentalOff = (RadioButton) rootView.findViewById(R.id.radio_button_parental_off);
        radioButtonParentalAll = (RadioButton) rootView.findViewById(R.id.radio_button_parental_all);
        radioButtonParentalAdult = (RadioButton) rootView.findViewById(R.id.radio_button_parental_adult);
        linearParentalControl.setOnClickListener(this);
        fl_audio_quality.setOnClickListener(this);
        video_quality_ll.setOnClickListener(this);
        language_ll.setOnClickListener(this);
        linearParentalControl.setVisibility(View.GONE);
        if (ApplicationController.ENABLE_PARENTAL_CONTROL
                && PrefUtils.getInstance().getPrefEnableParentalControl()) {
            linearParentalControl.setVisibility(View.VISIBLE);
        }
        items.add(PrefUtils.getInstance().getplayerControlsBitratesAuto());
        items.add(PrefUtils.getInstance().getplayerControlsBitratesHigh());
        items.add(PrefUtils.getInstance().getplayerControlsBitratesMedium());
        items.add(PrefUtils.getInstance().getplayerControlsBitratesLow());
//        items.add("Auto"); //auto
//        items.add("Full HD"); // high
//        items.add("HD"); //medium
//        items.add("SD"); //low
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
//        video_quality_list.setLayoutManager(layoutManager);
      /*  qualityList.add(new selectQuality(PrefUtils.getInstance().getplayerControlsBitratesAuto(),PrefUtils.getInstance().getplayerControlsBitratesAuto(),false,false));
        qualityList.add(new selectQuality(PrefUtils.getInstance().getplayerControlsBitratesHigh(),PrefUtils.getInstance().getplayerControlsBitratesHigh(),false,false));
        qualityList.add(new selectQuality(PrefUtils.getInstance().getplayerControlsBitratesMedium(),PrefUtils.getInstance().getplayerControlsBitratesMedium(),false,false));*/
        String bitrateConfig = PrefUtils.getInstance().getplayerControlsBitrates();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String  quality = preferences.getString("value", "");
        if(preferences!=null && TextUtils.isEmpty(quality)){
            quality="Auto";
        }
        if(PrefUtils.getInstance().getPendingSMC() != null && PrefUtils.getInstance().getPendingSMC().size()>0)
            unregistered_smc.setVisibility(View.VISIBLE);
        else
            unregistered_smc.setVisibility(View.GONE);
        //If the video quality changed from Player screen want to reflect here need to use this selectedQuality in place of quality which is from shared preferences.
        String selectedQuality = PrefUtils.getInstance().getContentVideoQuality() != null ? PrefUtils.getInstance().getContentVideoQuality() : "Auto";
        
        selected_video_quality.setText(quality);
        if (bitrateConfig != null) {
            try {
                JSONObject jsonObject = new JSONObject(bitrateConfig);
                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                    String keyObj = it.next();
                    System.out.println("key : " + keyObj);
                    if (quality.equalsIgnoreCase(keyObj))
                        qualityList.add(new SelectQuality(keyObj, keyObj, true, false));
                    else
                        qualityList.add(new SelectQuality(keyObj, keyObj, false, false));
                    // System.out.println("value : " + valObj.toString());
                }
            } catch (Exception e) {

            }

        }
        smartCardRecyclerViewAdapter = new VideoQualityAdapter(inflater, qualityList);
        video_quality_list.setAdapter(smartCardRecyclerViewAdapter);
        smartCardRecyclerViewAdapter.notifyDataSetChanged();
        // we pass our item list and context to our Adapter.
        QualityAdapter adapter = new QualityAdapter(mContext, items);
        mSpinnerQuality.setAdapter(adapter);
        setPrefDataToUI();
        radioGroupQuality.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.radio_button_quality_auto:
                        textVideoPlaybackQuality.setText(getResources().getString(R.string.auto));
                        PrefUtils.getInstance().setPrefPlayBackQuality("auto");
                        toggleQualitySelectionVisibility();
                        break;
                    case R.id.radio_button_quality_hd:
                        textVideoPlaybackQuality.setText(getResources().getString(R.string.high_def));
                        PrefUtils.getInstance().setPrefPlayBackQuality("high");
                        toggleQualitySelectionVisibility();
                        break;
                    case R.id.radio_button_quality_sd:
                        textVideoPlaybackQuality.setText(getResources().getString(R.string.standard_def));
                        PrefUtils.getInstance().setPrefPlayBackQuality("medium");
                        toggleQualitySelectionVisibility();
                        break;
                    case R.id.radio_button_quality_low:
                        textVideoPlaybackQuality.setText(getResources().getString(R.string.low_def));
                        PrefUtils.getInstance().setPrefPlayBackQuality("low");
                        toggleQualitySelectionVisibility();
                        break;
                }
            }
        });

        checkboxAutoPlayToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.getInstance().setAutoPlay(b);
            }
        });
        FrameLayout fl_notification = rootView.findViewById(R.id.fl_notification);
        if(PrefUtils.getInstance().getIsShowNotificationOption())
        {
            fl_notification.setVisibility(View.VISIBLE);
            checkboxNotificationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    PrefUtils.getInstance().setEnableNotifications(b);
                }
            });
        }else {
            fl_notification.setVisibility(GONE);
        }

        checkboxDownloadOnWifiToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.getInstance().setIsDownloadOnlyWifi(b);
            }
        });
        radioGroupParentalControl.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final RadioGroup radioGroup, final @IdRes int i) {
                final int positionOfParentalRadioGroup = getPositionOfParentalRadioGroup(i);
                if (PrefUtils.getInstance().getPrefParentalControlOpt() == positionOfParentalRadioGroup) {
                    SDKLogger.debug("choosen the same option as previously i- " + i + " \ngetPrefParentalControlOpt- " + PrefUtils.getInstance().getPrefParentalControlOpt());
                    return;
                }
                ParentalControlDialog parentalControlDialog = new ParentalControlDialog(mContext, new ParentalControlDialog.ParentalControlOptionUpdateListener() {
                    @Override
                    public void onUpdateOption(boolean success) {
                        toggleParentalControlVisibility();
                        SDKLogger.debug("settings change " + success);
                        if (!success) {
                            updateParentalControlUI(PrefUtils.getInstance().getPrefParentalControlOpt());
                            return;
                        }
                        updateParentalControlUI(i);
                        String radioButtonName = getTextOfParentalRadioGroup(i);
                        CleverTap.eventParentalControlStatus(radioButtonName.toLowerCase());
                        if (positionOfParentalRadioGroup == 0)
                            PrefUtils.getInstance().setPrefParentalControlPIN(-1);
                    }
                });
                try {
                    parentalControlDialog.showSetPINDialog(positionOfParentalRadioGroup);
                } catch (Throwable e) {
                    updateParentalControlUI(PrefUtils.getInstance().getPrefParentalControlOpt());
                    e.printStackTrace();
                }
            }
        });

        getLanguages();
    }

    private void updateParentalControlUI(int i) {
        String message = null;
        switch (i) {
            case 0:
            case R.id.radio_button_parental_off:
                message = mContext.getString(R.string.parental_text_off);
                PrefUtils.getInstance().setPrefParentalControlOpt(0);
                SDKLogger.debug("parent control reset to Off");
                radioButtonParentalOff.setChecked(true);
                break;
            case 1:
            case R.id.radio_button_parental_all:
                message = mContext.getString(R.string.parental_text_all_content);
                SDKLogger.debug("parent control reset to All");
                PrefUtils.getInstance().setPrefParentalControlOpt(1);
                radioButtonParentalAll.setChecked(true);
                break;
            case 2:
            case R.id.radio_button_parental_adult:
                message = mContext.getString(R.string.parental_text_adult_content);
                PrefUtils.getInstance().setPrefParentalControlOpt(2);
                SDKLogger.debug("parent control set to Audult");
                radioButtonParentalAdult.setChecked(true);
                break;
        }
        textParentalControl.setText(message);
    }

    private int getPositionOfParentalRadioGroup(int i) {
        switch (i) {
            case R.id.radio_button_parental_off:
                return 0;
            case R.id.radio_button_parental_all:
                return 1;
            case R.id.radio_button_parental_adult:
                return 2;
            default:
                return 0;
        }
    }


    private String getTextOfParentalRadioGroup(int position) {
        switch (position) {
            case R.id.radio_button_parental_off:
                return "Off";
            case R.id.radio_button_parental_all:
                return "All Content";
            case R.id.radio_button_parental_adult:
                return "Adult Only";
            default:
                return "Off";
        }
    }

    /**
     *
     */
    private void setPrefDataToUI() {
        // TODO : SETUP UI FOR PLAYBACK QUALITY
        String playBackQuality = PrefUtils.getInstance().getPrefPlayBackQuality() != null
                ? PrefUtils.getInstance().getPrefPlayBackQuality() : "Auto";
        if (!Util.checkUserLoginStatus()) {
            playBackQuality = "auto";
        }
        /*if (playBackQuality.equalsIgnoreCase("Auto")) {
            radioButtonQualityAuto.setChecked(true);
            textVideoPlaybackQuality.setText(getResources().getString(R.string.auto));
        } else if (playBackQuality.equalsIgnoreCase("")) {
            radioButtonQualityAuto.setChecked(true);
            textVideoPlaybackQuality.setText(getResources().getString(R.string.auto));
        } else if (playBackQuality.equalsIgnoreCase(PrefUtils.getInstance().getplayerControlsBitratesHigh())) {
            textVideoPlaybackQuality.setText(PrefUtils.getInstance().getplayerControlsBitratesHigh());
            radioButtonQualityHd.setChecked(true);
        } else if (playBackQuality.equalsIgnoreCase(PrefUtils.getInstance().getplayerControlsBitratesMedium())) {
            textVideoPlaybackQuality.setText(PrefUtils.getInstance().getplayerControlsBitratesMedium());
            radioButtonQualitySd.setChecked(true);
        } else if (playBackQuality.equalsIgnoreCase(PrefUtils.getInstance().getplayerControlsBitratesLow())) {
            textVideoPlaybackQuality.setText(PrefUtils.getInstance().getplayerControlsBitratesLow());
            radioButtonQualityLow.setChecked(true);
        } else*/
        if (playBackQuality.equalsIgnoreCase("Auto")) {
            radioButtonQualityAuto.setChecked(true);
            textVideoPlaybackQuality.setText(getResources().getString(R.string.auto));
        } else if (playBackQuality.equalsIgnoreCase("high")) {
            textVideoPlaybackQuality.setText(getResources().getString(R.string.high_def));
            radioButtonQualityHd.setChecked(true);
        } else if (playBackQuality.equalsIgnoreCase("medium")) {
            textVideoPlaybackQuality.setText(getResources().getString(R.string.standard_def));
            radioButtonQualitySd.setChecked(true);
        } else if (playBackQuality.equalsIgnoreCase("low")) {
            textVideoPlaybackQuality.setText(getResources().getString(R.string.low_def));
            radioButtonQualityLow.setChecked(true);
        }
        updateParentalControlUI(PrefUtils.getInstance().getPrefParentalControlOpt());
        // TODO : SETUP UI FOR AUTOPLAY TOGGLE
        if (PrefUtils.getInstance().isAutoplay()) {
            checkboxAutoPlayToggle.setChecked(true);
        } else {
            checkboxAutoPlayToggle.setChecked(false);
        }

        // TODO : SETUP UI FOR NOTIFICATION TOGGLE
        if (PrefUtils.getInstance().isNotificationEnabled()) {
            checkboxNotificationToggle.setChecked(true);
        } else {
            checkboxNotificationToggle.setChecked(false);
        }

        // TODO : SETUP UI FOR DOWNLOAD ONLY ON WIFI TOGGLE
        if (PrefUtils.getInstance().isDownloadOnlyOnWifi()) {
            checkboxDownloadOnWifiToggle.setChecked(true);
        } else {
            checkboxDownloadOnWifiToggle.setChecked(false);
        }
    }

    private void initUIAbout(View rootView) {
        mTextAbout = (TextView) rootView.findViewById(R.id.text_about);
        mTextAbout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.linear_video_playback_quality:
                toggleQualitySelectionVisibility();
                break;
            case R.id.language_ll:
                BaseFragment mCurrentFragment = new AppLanguageFragment();
               // PrefUtils.getInstance().setAppLanguageToShow("English,Hindi");
                Bundle args = new Bundle();
                mBaseActivity.pushFragment(mCurrentFragment);
                break;
            case R.id.unregistered_smc:
                BaseFragment pendingSMCFragment = new PendingSMCFragment();
                // PrefUtils.getInstance().setAppLanguageToShow("English,Hindi");
                Bundle arg = new Bundle();
                mBaseActivity.pushFragment(pendingSMCFragment);
                break;
            case R.id.fl_audio_quality:
            case R.id.video_quality_ll:
             /*   Context wrapper = new ContextThemeWrapper(getContext(), R.style.PopupMenuTheme);
                PopupMenu popup = new PopupMenu(wrapper, view);
                popup.getMenuInflater().inflate(R.menu.genres_menu, popup.getMenu());
                    for (int i = 0; i < items.size(); i++) {
                            popup.getMenu().add(0, i , i, items.get(i));
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int menuItemID = item.getItemId();
                        selected_video_quality.setText(items.get(menuItemID));
                        PrefUtils.getInstance().setContentVideoQuality(items.get(menuItemID));
                        return false;
                    }
                });
                popup.show();*/
                if (more_arrow.getTag().equals("Close")) {
                    more_arrow.setTag("Open");
                    video_quality_popup.setVisibility(View.VISIBLE);
                    more_arrow.setImageResource(R.drawable.upside_arrow);
                } else {
                    more_arrow.setTag("Close");
                    video_quality_popup.setVisibility(View.GONE);
                    more_arrow.setImageResource(R.drawable.ic_down);
                }

                break;
            case R.id.linear_parental_control:
                if (!Util.checkUserLoginStatus()) {
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.parental_text_not_logged_in));
                    return;
                }
                toggleParentalControlVisibility();
                break;
            case R.id.text_about:
                ((SettingsActivity) getActivity()).setToolBarVisibility(false);
                showAboutFragment();
                break;
            case R.id.text_about_app:
                showAboutDialog();
                break;
            case R.id.text_tnc:
                Intent tnc = new Intent(mContext, LiveScoreWebView.class);
                CleverTap.eventPageViewed(CleverTap.PAGE_TERMS_AND_CONDITIONS);
                AppsFlyerTracker.eventBrowseHelp();
                if (!TextUtils.isEmpty(PrefUtils.getInstance().getTncUrl())) {
                    tnc.putExtra("url", PrefUtils.getInstance().getTncUrl());
                } else {
                    tnc.putExtra("url", APIConstants.getFAQURL() + APIConstants.TNC_URL);
                }
                tnc.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.tnc));
                mContext.startActivity(tnc);
                break;
            case R.id.text_privacy_policy:
                Intent ppc = new Intent(mContext, LiveScoreWebView.class);
                CleverTap.eventPageViewed(CleverTap.PAGE_PRIVACY_POLICY);
                AppsFlyerTracker.eventBrowseHelp();
                ppc.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.privacy_policy));
                if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrivacy_policy_url())) {
                    ppc.putExtra("url", PrefUtils.getInstance().getPrivacy_policy_url());
                } else {
                    ppc.putExtra("url", APIConstants.getFAQURL() + APIConstants.PRIVACY_POLICY_URL);
                }
                mContext.startActivity(ppc);
                break;
        }
    }

    private void showAboutFragment() {
        BaseFragment aboutFragment = new AboutSectionsFragment();
        ((SettingsActivity) getActivity()).pushFragment(aboutFragment);
    }

    private void showAboutDialog() {
        AboutDialogWebView aboutDialogWebView = new AboutDialogWebView(mContext);
        aboutDialogWebView.showDialog();
    }

    private void toggleQualitySelectionVisibility() {
        if (radioGroupQuality.getVisibility() == View.VISIBLE) {
            collapse(radioGroupQuality);
        } else {
            expand(radioGroupQuality);
        }
    }


    private void toggleParentalControlVisibility() {
        if (radioGroupParentalControl.getVisibility() == View.VISIBLE) {
            collapse(radioGroupParentalControl);
        } else {
            expand(radioGroupParentalControl);
        }
    }

    Interpolator easeInOutQuart = PathInterpolatorCompat.create(0.77f, 0f, 0.175f, 1f);

    public Animation expand(final View view) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = view.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0 so use 1 instead.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        if (view.getId() == radioGroupParentalControl.getId())
            textParentalControl.setVisibility(View.GONE);
        else
            textVideoPlaybackQuality.setVisibility(View.GONE);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                view.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);

                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setInterpolator(easeInOutQuart);
        animation.setDuration(computeDurationFromHeight(view));
        view.startAnimation(animation);

        return animation;
    }

    public Animation collapse(final View view) {
        final int initialHeight = view.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                    if (view.getId() == radioGroupParentalControl.getId())
                        textParentalControl.setVisibility(View.VISIBLE);
                    else
                        textVideoPlaybackQuality.setVisibility(View.VISIBLE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setInterpolator(easeInOutQuart);

        int durationMillis = computeDurationFromHeight(view);
        a.setDuration(durationMillis);

        view.startAnimation(a);

        return a;
    }

    private static int computeDurationFromHeight(View view) {
        // 1dp/ms * multiplier
        return (int) (view.getMeasuredHeight() / view.getContext().getResources().getDisplayMetrics().density);
    }

    public class QualityAdapter extends ArrayAdapter<String> {

        public QualityAdapter(Context context,
                              ArrayList<String> algorithmList) {
            super(context, 0, algorithmList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable
                View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable
                View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        private View initView(int position, View convertView,
                              ViewGroup parent) {
            // It is used to set our custom view.
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.quality_spinner_item, parent, false);
            }

            TextView textViewName = convertView.findViewById(R.id.quality);
            String currentItem = getItem(position);

            // It is used the name to the TextView when the
            // current item is not null.
            if (currentItem != null) {
                textViewName.setText(currentItem);
            }
            return convertView;
        }
    }


    private class VideoQualityAdapter extends BaseAdapter implements View.OnClickListener {
        private LayoutInflater inflter;
        ArrayList<SelectQuality> selectQuality = new ArrayList<>();
        private Context context;
        View.OnClickListener onClickListener;

        public VideoQualityAdapter(LayoutInflater inflater, ArrayList<SelectQuality> videoQualityList) {

            this.selectQuality = videoQualityList;
            inflter = inflater;
            if (videoQualityList != null) {
                for (int i = 0; i < videoQualityList.size(); i++) {
                    if (videoQualityList.get(i).isSelected()) {
                        selectedPosition = i;
                        selectedQuality = videoQualityList.get(i).getQuality();
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return selectQuality.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.video_quality_item_list, null);
            if (selectQuality == null || selectQuality.get(position) == null) {
                return null;
            }
            final CheckedTextView simpleCheckedTextView = view.findViewById(R.id.quality_name);
            simpleCheckedTextView.setText(selectQuality.get(position).getQualityToShow().substring(0, 1).toUpperCase() + selectQuality.get(position).getQualityToShow().substring(1).toLowerCase());
            simpleCheckedTextView.setChecked(selectQuality.get(position).isSelected());



            if(!selectQuality.get(position).isSelected()){
                simpleCheckedTextView.setBackground(mContext.getResources().getDrawable(R.drawable.language_rounded_corner));
                simpleCheckedTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            }else {
                simpleCheckedTextView.setBackground(mContext.getResources().getDrawable(R.drawable.language_rounded_corner_selected));
                simpleCheckedTextView.setTextColor(mContext.getResources().getColor(R.color.black));
            }


           /* if(!isChecked) {
                qualityList.get(0).setSelected(true);
                isChecked=true;
            }*/
            // perform on Click Event Listener on CheckedTextView
            simpleCheckedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    for (int i = 0; i < qualityList.size(); i++) {
                        selected_video_quality.setText(((TextView) v).getText().toString());
                        if (i == position) {
                            if (isChecked) {
                                qualityList.get(0).setSelected(false);
                            }
                            if (qualityList.get(i).isSelected()) {
                                qualityList.get(i).setSelected(false);
                            } else {
                                qualityList.get(i).setSelected(true);
                                PrefUtils.getInstance().setContentVideoQuality(qualityList.get(i).getQuality());
                                switch (PrefUtils.getInstance().getContentVideoQuality()) {
                                    case "Auto":
                                        textVideoPlaybackQuality.setText(getResources().getString(R.string.auto));
                                        PrefUtils.getInstance().setPrefPlayBackQuality("auto");
                                        break;
                                    case "High":
                                        textVideoPlaybackQuality.setText(getResources().getString(R.string.high_def));
                                        PrefUtils.getInstance().setPrefPlayBackQuality("high");
                                        break;
                                    case "Medium":
                                        textVideoPlaybackQuality.setText(getResources().getString(R.string.standard_def));
                                        PrefUtils.getInstance().setPrefPlayBackQuality("medium");
                                        break;
                                    case "Data Saver":
                                        textVideoPlaybackQuality.setText(getResources().getString(R.string.low_def));
                                        PrefUtils.getInstance().setPrefPlayBackQuality("low");
                                        break;
                                }
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("value",qualityList.get(i).getQuality());
                                editor.apply();
                                editor.commit();
                            }
                        } else
                            qualityList.get(i).setSelected(false);
                    }
                    notifyDataSetChanged();
                    selectedPosition = position;
                    selectedQuality = selectQuality.get(position).getQuality();
                }
            });
            return view;
        }

        @Override
        public void onClick(View view) {
            selected_video_quality.setText(((TextView) view).getText().toString());
            video_quality_popup.setVisibility(View.GONE);
            more_arrow.setImageResource(R.drawable.ic_down);
            more_arrow.setTag("Close");
            PrefUtils.getInstance().setContentVideoQuality(((TextView) view).getText().toString());
        }
    }


    /* private class VideoQualityAdapter extends RecyclerView.Adapter<VideoQualityAdapter.MyViewHolder>{
         List<String> myListData = new ArrayList<>();
         View.OnClickListener onClickListener;
         public VideoQualityAdapter(List<String> list, View.OnClickListener onClickListener){
             myListData = list;
             this.onClickListener = onClickListener;
         }

         @NonNull
         @Override
         public VideoQualityAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
             LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
             View listItem= layoutInflater.inflate(R.layout.video_quality_item_list, parent, false);
             VideoQualityAdapter.MyViewHolder viewHolder = new VideoQualityAdapter.MyViewHolder(listItem);
             return viewHolder;
         }

         @Override
         public void onBindViewHolder(@NonNull VideoQualityAdapter.MyViewHolder holder, int position) {
             holder.textView.setText(myListData.get(position));
             if(myListData.get(position).equalsIgnoreCase(selected_video_quality.getText().toString())) {
                 holder.textView.setTextColor(mContext.getResources().getColor(R.color.selected_video_quality));
             } else
                 holder.textView.setTextColor(mContext.getResources().getColor(R.color.white));
             holder.textView.setOnClickListener(v -> {
                 onClickListener.onClick(v);
             });
         }

         @Override
         public int getItemCount() {
             return myListData.size();
         }

         private class MyViewHolder extends RecyclerView.ViewHolder{
             public TextView textView;

             public MyViewHolder(@NonNull View itemView) {
                 super(itemView);
                 this.textView = (TextView) itemView.findViewById(R.id.quality_name);
             }
         }
     }*/
    private View.OnClickListener videoQualityClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selected_video_quality.setText(((TextView) v).getText().toString());
            video_quality_popup.setVisibility(View.GONE);
            more_arrow.setImageResource(R.drawable.ic_down);
            more_arrow.setTag("Close");
            PrefUtils.getInstance().setContentVideoQuality(((TextView) v).getText().toString());
            smartCardRecyclerViewAdapter.notifyDataSetChanged();
        }
    };

    private void getLanguages() {
        showProgressBar(true);
        LanguageListRequest languageRequest = new LanguageListRequest(new APICallback<LanguageListResponse>() {
            @Override
            public void onResponse(APIResponse<LanguageListResponse> response) {
                dismissProgressBar(true);
                if (response == null || null == response.body()) {
                    return;
                }
                if(response.body().getLanguages()!=null && response.body().getLanguages().size()>0){
                    List<Term> terms = response.body().getLanguages().get(0).getTerms();
                    if (terms != null && terms.size() > 0) {
                        // int selected = PrefUtils.getInstance().getAppLanguage();
                        String selectedLanguage;
                        if (PrefUtils.getInstance().getAppLanguageFirstTime() != null && PrefUtils.getInstance().getAppLanguageFirstTime().equalsIgnoreCase("true")) {
                            List<String> subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
                            if (subscribed_languages != null && subscribed_languages.size() > 0 && subscribed_languages.get(0) != null) {
                                selectedLanguage = subscribed_languages.get(0);
                            }
                            else{
                                selectedLanguage=PrefUtils.getInstance().getAppLanguageToSendServer();
                            }

                        } else {
                            selectedLanguage = PrefUtils.getInstance().getAppLanguageToSendServer();
                        }
                        if(selectedLanguage!=null && selectedLanguage.length()>1){
                            selectedLanguage = selectedLanguage.substring(0, 1).toUpperCase() + selectedLanguage.substring(1).toLowerCase();
                        }
                        selected_language.setText(selectedLanguage);
                    }
                }


            }


            @Override
            public void onFailure(Throwable t, int errorCode) {
                dismissProgressBar(true);
            }
        });
        APIService.getInstance().execute(languageRequest);
    }


    public void showProgressBar(boolean shouldUseProgressBar) {

        if (mContext == null) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE) {
            return;
        }
        if (shouldUseProgressBar) {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        } else {
            mProgressDialog = ProgressDialog.show(mContext, "", "Loading...", true, true, null);
            mProgressDialog.setContentView(R.layout.layout_progress_dialog);
            ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.imageView1);
            final int version = Build.VERSION.SDK_INT;
            if (version < 21) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.getIndeterminateDrawable().setColorFilter(UiUtil.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
            }
        }

    }
    public void dismissProgressBar(boolean shouldUseProgressBar) {
        try {
            if (!isAdded()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing() && !shouldUseProgressBar) {
                mProgressDialog.dismiss();
            }
            if (mProgressBar != null
                    && mProgressBar.getVisibility() == View.VISIBLE
                    && shouldUseProgressBar) {
                mProgressBar.setVisibility(GONE);
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
