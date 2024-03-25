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
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.events.DownloadDataLoaded;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.MyWatchlistFavouritesTabAdapter;

import static com.myplex.myplex.ApplicationController.getApplicationConfig;

/**
 * Created by Ramaraju on 2/19/2018.
 */

public class MyWatchlistFavouritesFragment extends BaseFragment {

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((MainActivity) mBaseActivity).enableNavigation();
            ((MainActivity) mBaseActivity).updateNavigationBarAndToolbar();
            mBaseActivity.removeFragment(MyWatchlistFavouritesFragment.this);
        }
    };
    private MyWatchlistFavouritesTabAdapter myWatchlistTabAdapter;
    private ViewPager mViewPager;
    private String altitle,title;

    public void setTabName(String[]  tabName) {
        this.tabName = tabName;
    }

    public void setRequestType(int requestType){
        this.requestType=requestType;
    }

    private String[] tabName;

    private int requestType;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();

        ((MainActivity) mBaseActivity).disableNavigation();

        View rootView = inflater.inflate(R.layout.activity_my_downloads, container, false);
        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);

        View mInflateView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_toolbar, null, false);
        TextView mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        TextView mToolbarTitleOtherLang = (TextView) mInflateView.findViewById(R.id.toolbar_header_title_lang);
        ImageView mCloseIcon = (ImageView)mInflateView.findViewById(R.id.toolbar_settings_button);
        RelativeLayout mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        if(TextUtils.isEmpty(title)) {
            if (requestType== APIConstants.FAVOURITES_FETCH_REQUEST){
                mToolbarTitle.setText("Favourites");
            }else {
                mToolbarTitle.setText(R.string.navigation_my_watchlist);
            }

        }else{
            mToolbarTitle.setText(title);
        }
        if(PrefUtils.getInstance().getVernacularLanguage()){

            if ( !TextUtils.isEmpty(altitle)) {
                mToolbarTitleOtherLang.setText(altitle);
                mToolbarTitleOtherLang.setVisibility(View.VISIBLE);
            }else{
                mToolbarTitleOtherLang.setVisibility(View.GONE);
            }


        }else{
            mToolbarTitleOtherLang.setVisibility(View.GONE);
        }
        mCloseIcon.setOnClickListener(mCloseAction);
        ImageView mChannelImageView = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mChannelImageView.setVisibility(View.GONE);

        myWatchlistTabAdapter = new MyWatchlistFavouritesTabAdapter(getChildFragmentManager(),requestType);
        myWatchlistTabAdapter.setTabName(tabName);
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager_my_downloads);
        mViewPager.setAdapter(myWatchlistTabAdapter);

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
        tabLayout.setVisibility(View.VISIBLE);
        //Only for MyWatchList Screen
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(4);
        applyFontedTab(getActivity(),mViewPager,tabLayout);
        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
        SDKUtils.saveObject(ApplicationController.getDownloadData(), getApplicationConfig().downloadCardsPath);
    }

    SparseArray<Boolean> loadedDataStatus = new SparseArray();

    public void onEventMainThread(DownloadDataLoaded event) {
        loadedDataStatus.put(event.type, event.isEmpty);
        if (myWatchlistTabAdapter != null
                && myWatchlistTabAdapter.getCount() == loadedDataStatus.size()) {
            for (int i = 0; i < loadedDataStatus.size(); i++) {
                boolean isEmpty = loadedDataStatus.valueAt(i);
                if (!isEmpty) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onBackClicked() {
        ((MainActivity) mBaseActivity).enableNavigation();
        ((MainActivity) mBaseActivity).updateNavigationBarAndToolbar();
        return super.onBackClicked();
    }


    private void applyFontedTab(Activity activity, ViewPager viewPager, TabLayout tabLayout) {
        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
            TextView tv = (TextView) activity.getLayoutInflater().inflate(R.layout.center_text_view, null);
            if (i == viewPager.getCurrentItem()) tv.setSelected(true);
            tv.setText(viewPager.getAdapter().getPageTitle(i));
            tabLayout.getTabAt(i).setCustomView(tv);
        }
    }

    public void setAlttitle(String str){
        altitle = str;
    }
    public void settitle(String str){
        title = str;
    }
}
