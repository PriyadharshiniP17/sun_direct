package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.fragment.AboutSectionsFragment;
import com.myplex.myplex.ui.fragment.AppLanguageFragment;
import com.myplex.myplex.ui.fragment.AppSettingsFragment;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.HelpSectionFragment;
import com.myplex.myplex.ui.fragment.PendingSMCFragment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.util.PrefUtils;


/**
 * Created by Phani on 12/12/2015.
 */
public class SettingsActivity extends BaseActivity {
    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle,mToolbarTitleLanguage;
    private ImageView mHeaderImageView;
    private ImageView mChannelImageView;
    private ImageView mCloseIcon;
    private RelativeLayout mRootLayout;
    private Context mContext;
    private BaseFragment mCurrentFragment;
    private boolean isAppSettingFragmentOnBackPress = false;

    public static final String SECTION_TITLE = "section_title";
    public static final String SECTION_TITLE_LANGUAGE = "section_title_language";
    public static final String SECTION_TYPE = "section_type";

    public static final int APPLICATION_SETTINGS = 0;
    public static final int APPLICATION_ABOUT = 1;
    public static final int APPLICATION_HELP = 2;
    public static final int APPLICATION_APP_LANGUAGE = 3;
    public static final int APPLICATION_MORE = 4;
    public static final int PENDING_SMC_NUMBERS = 5;

    private String title,titleLang;
    private int mSectionType;

    private final CacheManager mCacheManager = new CacheManager();

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
            //showOverFlowSettings(v);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mContext = this;
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (DeviceUtils.isTabletOrientationEnabled(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        readIntentData();
     /*   mToolbar.setTitle("Settings");
        mToolbar.setNavigationIcon(R.drawable.arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });*/
        mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mToolbarTitle.setTextColor(getResources().getColor(R.color.white));
        mToolbarTitleLanguage = (TextView) mInflateView.findViewById(R.id.toolbar_header_title_lang);
        mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        mCloseIcon = (ImageView)mInflateView.findViewById(R.id.toolbar_settings_button);
        mCloseIcon.setOnClickListener(mCloseAction);
        mCloseIcon.setVisibility(View.VISIBLE);
        initUI();
    }

    private void readIntentData() {
        Bundle data = getIntent().getExtras();
        if (data != null) {
            title = data.getString(SECTION_TITLE);
            titleLang = data.getString(SECTION_TITLE_LANGUAGE);
            mSectionType = data.getInt(SECTION_TYPE);
        }
    }

    private void initUI() {
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mToolbarTitle.setText("Settings");
//        mToolbarTitle.setTextSize((int)getResources().getDimension(R.dimen.textsize_6));
       // mToolbarTitle.setText(title);
        setToolBarVisibility(true);
        switch (mSectionType) {
            case APPLICATION_SETTINGS:
                setToolBarVisibility(true);
                mCurrentFragment = new AppSettingsFragment();
                break;
            case APPLICATION_ABOUT:
                setToolBarVisibility(true);
                mCurrentFragment = new AboutSectionsFragment();
                break;
            case APPLICATION_HELP:
                setToolBarVisibility(true);
                mCurrentFragment = new HelpSectionFragment();
                break;
            case APPLICATION_MORE:
                setToolBarVisibility(true);
                mCurrentFragment = new MoreSectionFragment();
                break;
            case APPLICATION_APP_LANGUAGE:
                setToolBarVisibility(true);
                mCurrentFragment = new AppLanguageFragment();
               // PrefUtils.getInstance().setAppLanguageToShow("English,Hindi");
                break;
            case PENDING_SMC_NUMBERS:
                setToolBarVisibility(true);
                mCurrentFragment = new PendingSMCFragment();
                break;
        }
        pushFragment(mCurrentFragment);
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

    public void  setToolBarVisibility(boolean isToolbarNeedToBeShown){
        if(isToolbarNeedToBeShown){
            if(PrefUtils.getInstance().getVernacularLanguage()){

                if(titleLang != null && !titleLang.isEmpty()){
                    mToolbarTitleLanguage.setText(titleLang);
                    mToolbarTitleLanguage.setVisibility(View.VISIBLE);
                }else{
                    mToolbarTitleLanguage.setVisibility(View.GONE);
                }
            }else{
                mToolbarTitleLanguage.setVisibility(View.GONE);
            }
        }else{
            mToolbarTitleLanguage.setVisibility(View.GONE);
        }
    }


    @Override
    public void pushFragment(BaseFragment fragment) {
        if (fragment == null) {
            return;
        }
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            removeFragment(mCurrentFragment);
            transaction.add(R.id.content_settingsview, fragment);
            mCurrentFragment = fragment;
            fragment.setBaseActivity(this);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
            updateToolbarTitle(mCurrentFragment);
        } catch (Throwable e) {
            e.printStackTrace();

        }

    }

    @Override
    public void removeFragment(BaseFragment fragment) {
        if (fragment == null) {
            return;
        }
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (mCurrentFragment != null) {
                transaction.remove(mCurrentFragment);
            }
            transaction.commitAllowingStateLoss();

        } catch (Throwable e) {
            e.printStackTrace();

        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        setToolBarVisibility(true);
        if(mCurrentFragment instanceof AppLanguageFragment){
            isAppSettingFragmentOnBackPress = true;
            mCurrentFragment = new AppSettingsFragment();
            pushFragment(mCurrentFragment);
        }if(mCurrentFragment instanceof PendingSMCFragment){
            isAppSettingFragmentOnBackPress = true;
            mCurrentFragment = new AppSettingsFragment();
            pushFragment(mCurrentFragment);
        }
        else {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            }else if(isAppSettingFragmentOnBackPress){
                isAppSettingFragmentOnBackPress = false;
            }else {
                finish();
            }

        }


       // finish();
    }

    private void updateToolbarTitle(BaseFragment fragment){
        if(fragment instanceof AppSettingsFragment){
            mToolbarTitle.setText(title);
        }else if(fragment instanceof AboutSectionsFragment){
            mToolbarTitle.setText(mContext.getResources().getString(R.string.about_us));
        }else  if(fragment instanceof MoreSectionFragment){
            mToolbarTitle.setText(R.string.others);
        }
        else if(fragment instanceof AppLanguageFragment){
            mToolbarTitle.setText(R.string.language_setting);
        }
        else if(fragment instanceof PendingSMCFragment){
            mToolbarTitle.setText(R.string.unregistered_smc);
        }
    }
}
