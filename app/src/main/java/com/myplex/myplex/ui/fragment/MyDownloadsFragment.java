package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.myplex.R;
import com.myplex.myplex.events.DownloadDataLoaded;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.MyDownloadsTabPagerAdapter;

/**
 * Created by Phani on 12/12/2015.
 */
public class MyDownloadsFragment extends BaseFragment {

    public static final String PARAM_SHOW_TOOLBAR = "show_toolbar";
    public Boolean isToShowToolBar=false;

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((MainActivity) mBaseActivity).enableNavigation();
            ((MainActivity) mBaseActivity).updateNavigationBarAndToolbar();
            mBaseActivity.removeFragment(MyDownloadsFragment.this);
        }
    };
    private MyDownloadsTabPagerAdapter myDownloadsTabPagerAdapter;
    private ViewPager mViewPager;

    public void setToolBar(boolean isToShowToolBar){
        this.isToShowToolBar=isToShowToolBar;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();

        Bundle args = getArguments();
        ((MainActivity) mBaseActivity).disableNavigation();

        View rootView = inflater.inflate(R.layout.activity_my_downloads, container, false);
        if (isToShowToolBar) {
            showToolBar(isToShowToolBar,rootView);
        }else {
            showToolBar(false,rootView);
        }
        myDownloadsTabPagerAdapter = new MyDownloadsTabPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager_my_downloads);
        mViewPager.setAdapter(myDownloadsTabPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                LoggerD.debugDownload("onPageSelected: position- " + position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(2);
        applyFontedTab(getActivity(),mViewPager,tabLayout);
        tabLayout.setVisibility(View.GONE);
        return rootView;
    }

    private void showToolBar(boolean aBoolean,View rootView) {
        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);

        View mInflateView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_toolbar, null, false);
        TextView mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        TextView mToolbarTitleLang = mInflateView.findViewById(R.id.toolbar_header_title_lang);
        if (getArguments() != null && getArguments().containsKey(APIConstants.LanguageTitle)
                && !TextUtils.isEmpty(getArguments().getString(APIConstants.LanguageTitle, ""))) {
            mToolbarTitleLang.setVisibility(View.VISIBLE);
            mToolbarTitleLang.setText(getArguments().getString(APIConstants.LanguageTitle, ""));
        }
        ImageView mCloseIcon = (ImageView)mInflateView.findViewById(R.id.toolbar_settings_button);
        RelativeLayout mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mToolbarTitle.setText(R.string.navigation_my_downloads);
        mCloseIcon.setOnClickListener(mCloseAction);
        ImageView mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);
        if (aBoolean){
            mToolbar.setVisibility(View.VISIBLE);
        }else {
            mToolbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
       // SDKUtils.saveObject(ApplicationController.getDownloadData(), getApplicationConfig().downloadCardsPath);
    }

    SparseArray<Boolean> loadedDataStatus = new SparseArray();

    public void onEventMainThread(DownloadDataLoaded event) {
        try {
            loadedDataStatus.put(event.type, event.isEmpty);
            if (myDownloadsTabPagerAdapter != null
                    && myDownloadsTabPagerAdapter.getCount() == loadedDataStatus.size()) {
                for (int i = 0; i < loadedDataStatus.size(); i++) {
                    boolean isEmpty = loadedDataStatus.valueAt(i);
                    if (!isEmpty) {
                        mViewPager.setCurrentItem(i);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onBackClicked() {
        ((MainActivity) mBaseActivity).enableNavigation();
        ((MainActivity) mBaseActivity).updateNavigationBarAndToolbar();
        return super.onBackClicked();
    }

    private  void applyFontedTab(Activity activity, ViewPager viewPager, TabLayout tabLayout) {
        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
            TextView tv = (TextView) activity.getLayoutInflater().inflate(R.layout.center_text_view, null);
            if (i == viewPager.getCurrentItem()) tv.setSelected(true);
            tv.setText(viewPager.getAdapter().getPageTitle(i));
            tabLayout.getTabAt(i).setCustomView(tv);
        }
    }
}
