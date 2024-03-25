package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.FavouriteSectionsListRequest;
import com.myplex.model.CardData;
import com.myplex.model.FavouriteSectionsListResponse;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.adapter.FavouriteTabsNestedAdapter;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import viewpagerindicator.CustomViewPager;

public class FavouriteViewPagerFragment extends Fragment {
    //  RecyclerView genreList;
    private FragmentActivity mContext;
    public List<CardData> cardsList = new ArrayList<>();
    private FrameLayout tabAll;
    private TabLayout tabLayout;
    private ProgressBar progressBar, smallProgressBar;
    private TextView noDateText;
    private TextView mGridViewLoadingText;
    private boolean mIsLoadingMoreAvailable = true;
    private boolean mIsLoadingMorePages = false;
    private int mStartIndex = 1;
    private ImageView left_arrow, right_arrow;
    private LinearLayout noFavoritesFoundLayout;
    String tabName;
    Animation animation;
    private String selectedGenre = "";
    CustomViewPager viewpagerFavourite;
    FavouriteTabsNestedAdapter favouriteTabsNestedAdapter;
    private String selected_language  ;
    private List<String> subscribed_languages;
    private boolean isFromTab;
    private boolean isFromPage;
    private boolean isFirstTimeLoading = false;

    RelativeLayout rl_root_fav;
    ImageView iv_no_favourites;
    public FavouriteViewPagerFragment() {

    }
    public FavouriteViewPagerFragment(String tabName) {
        tabName = tabName;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getActivity();
        animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
        View view = inflater.inflate(R.layout.favourite_viewpager_fragment, container, false);
        viewpagerFavourite = view.findViewById(R.id.viewpager_favourite);
        mGridViewLoadingText = (TextView) view.findViewById(R.id.grid_footer_text_loading);
        noDateText = view.findViewById(R.id.no_data_text);
        tabAll=view.findViewById(R.id.tabLL);
        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        progressBar = view.findViewById(R.id.progressBar);
        smallProgressBar = view.findViewById(R.id.smallProgressBar);
        left_arrow = (ImageView) view.findViewById(R.id.left_arrow);
        right_arrow = (ImageView) view.findViewById(R.id.right_arrow);
        noFavoritesFoundLayout=view.findViewById(R.id.no_favourites_found_layout);
        rl_root_fav=view.findViewById(R.id.rl_root_fav);
        iv_no_favourites=view.findViewById(R.id.iv_no_favourites);
        left_arrow.setVisibility(View.GONE);

//        genresListAPICall();
        tabLayout.setHorizontalScrollBarEnabled(true);
       // viewpagerFavourite.setUserInputEnabled(true);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                String tabCode = tab.getTag().toString();
                //              int position = Integer.parseInt(tabCode);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!isFromPage) {
                            isFromTab = true;
                            if(!isFirstTimeLoading)
                                viewpagerFavourite.setCurrentItem(tab.getPosition(), false);
                        }
                        isFirstTimeLoading = false;
                        isFromPage = false;
                    }
                }, 500);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    showHideArrows();
                }
            });
        }
        left_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout != null) {
                    tabLayout.smoothScrollTo(tabLayout.getScrollX() - tabLayout.getWidth(), 0);
                    showHideArrows();
                }
            }
        });
        right_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout != null) {
                    tabLayout.smoothScrollTo(tabLayout.getScrollX() + tabLayout.getWidth(), 0);
                    showHideArrows();
                }
            }
        });
     /*   viewpagerFavourite.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
             *//*   if(!isFromTab) {
                    tabLayout.selectTab(tabLayout.getTabAt(position));
                }
                isFromTab = false;
                isFromPage = true;*//*
                // tabLayout.selectTab(tabLayout.getTabAt(position));

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                // tabLayout.getTabAt(position).select();
                if(!isFromTab && tabLayout!=null) {
                    isFromPage = true;
                    tabLayout.getTabAt(position).select();

                }
                isFromTab = false;

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
             *//*   tabLayout.getTabAt(state).select();
                isFromTab = true;*//*
            }
        });*/
        viewpagerFavourite.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(!isFromTab && tabLayout!=null) {
                    isFromPage = true;
                    tabLayout.getTabAt(position).select();

                }
                isFromTab = false;
                tabLayout.setScrollPosition(position,0f,true);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
     /*   viewpagerFavourite.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //  tabLayout.getTabAt(position).select();
                //  viewpager.setCurrentItem(position);
            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();
                viewpagerFavourite.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(APIConstants.IS_REFRESH_LIVETV){
            return;
        }
        genresListAPICall();
//        fetchMovieData(selectedGenre);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // fetchMovieData();
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void showHideArrows() {
        if (tabLayout.getScrollX() <= 30) {
            left_arrow.setVisibility(View.GONE);
        } else {
            left_arrow.setVisibility(View.VISIBLE);
        }
        Log.v("x-axis", "getScrollX " + tabLayout.getScrollX());
        Log.v("x-axis", "getX " + tabLayout.getX());
        Log.v("x-axis", "tabgetWidth  " + tabLayout.getChildAt(0).getMinimumWidth()*3);
        Log.v("x-axis", "screenWidth " + (int)mContext.getResources().getDimension(R.dimen._60sdp)* tabLayout.getTabCount());
        if (tabLayout.getScrollX() + (tabLayout.getWidth())  >ApplicationController.getApplicationConfig().screenWidth) {
            right_arrow.setVisibility(View.GONE);
        } else {
            right_arrow.setVisibility(View.VISIBLE);
        }

    }

    public void scrollToTop(){
        if(favouriteTabsNestedAdapter != null && favouriteTabsNestedAdapter.hashMap != null){
            Fragment screenFragment =  favouriteTabsNestedAdapter.hashMap.get(viewpagerFavourite.getCurrentItem());
            if(screenFragment != null && screenFragment instanceof FavouriteNewFragment)
                ((FavouriteNewFragment)screenFragment).startToScroll();
        }
    }

    public void genresListAPICall() {
        Fragment fragment = this;
        FavouriteSectionsListRequest mRequestFavourites = new FavouriteSectionsListRequest(new APICallback<FavouriteSectionsListResponse>() {
            @Override
            public void onResponse(APIResponse<FavouriteSectionsListResponse> response) {
                if (response == null
                        || response.body() == null) {
                    return;
                }

                //Log.d("Favourite", "FavouriteRequest: onResponse: message - " + response.body().message);

                if(response.body().getResults()!=null  && response.body().getResults().isEmpty()){
                    tabAll.setVisibility(View.GONE);
                    noFavoritesFoundLayout.setVisibility(View.VISIBLE);
                    updateOrientation();
                    // viewpagerFavourite.setUserInputEnabled(false);
                    viewpagerFavourite.setPagingEnabled(false);
                    tabLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
                if(response.body().getResults() != null && !response.body().getResults().isEmpty() && response.body().getResults().size()>0 && tabLayout!=null) {
                    List<String> terms = response.body().getResults();
                    List<String> reorderTerms = new ArrayList<>();

//                    setTabMode(terms);

                    tabLayout.removeAllTabs();
                    tabAll.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    //  viewpagerFavourite.setUserInputEnabled(true);
                    viewpagerFavourite.setPagingEnabled(true);
                    noFavoritesFoundLayout.setVisibility(View.GONE);
                    tabLayout.addTab(tabLayout.newTab().setTag(0).setText(robotoStringFont("All")));
                   /*else {
                            LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
                            if(locationInfo!=null && locationInfo.languageCode != null && locationInfo.languageName != null){

                            }
                        }*/
                    int selectedPosition = 0;
                    if(PrefUtils.getInstance().getAppLanguageFirstTime()!=null && PrefUtils.getInstance().getAppLanguageFirstTime().equalsIgnoreCase("true")){
                        subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
                        String converted_pack_language = "";
                        if (subscribed_languages != null && subscribed_languages.size() > 0) {
                            for (int i = 0; i < subscribed_languages.size(); i++) {
                                converted_pack_language = subscribed_languages.get(i).substring(0, 1).toUpperCase() + subscribed_languages.get(i).substring(1);
                                if (converted_pack_language != null && !reorderTerms.contains(converted_pack_language) && terms.contains(converted_pack_language)) {
                                    reorderTerms.add(converted_pack_language);
                                    tabLayout.addTab(tabLayout.newTab().setTag(1).setText(robotoStringFont(converted_pack_language)));
                                    selectedPosition = 1;
                                }
                            }
                        }
                        selected_language = PrefUtils.getInstance().getAppLanguageToSendServer();
                        String convertedGenre = "";
                        if (selected_language != null && !selected_language.isEmpty()) {
                            String newGenre = selected_language.toLowerCase(Locale.ROOT);
                            convertedGenre = newGenre.substring(0, 1).toUpperCase() + newGenre.substring(1);
                            if (convertedGenre != null && !reorderTerms.contains(convertedGenre) && terms.contains(convertedGenre)) {
                                reorderTerms.add(convertedGenre);
                                tabLayout.addTab(tabLayout.newTab().setTag(2).setText(robotoStringFont(convertedGenre)));
                                selectedPosition = 1;
                            }
                        }
                    }
                    else{
                        selected_language = PrefUtils.getInstance().getAppLanguageToSendServer();
                        String convertedGenre = "";
                        if (selected_language != null && !selected_language.isEmpty()) {
                            String newGenre = selected_language.toLowerCase(Locale.ROOT);
                            convertedGenre = newGenre.substring(0, 1).toUpperCase() + newGenre.substring(1);
                            if (convertedGenre != null && !reorderTerms.contains(convertedGenre) && terms.contains(convertedGenre)) {
                                reorderTerms.add(convertedGenre);
                                tabLayout.addTab(tabLayout.newTab().setTag(2).setText(robotoStringFont(convertedGenre)));
                                selectedPosition = 1;
                            }
                        }
                        subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
                        String converted_pack_language = "";
                        if (subscribed_languages != null && subscribed_languages.size() > 0) {
                            for (int i = 0; i < subscribed_languages.size(); i++) {
                                converted_pack_language = subscribed_languages.get(i).substring(0, 1).toUpperCase() + subscribed_languages.get(i).substring(1);
                                if (converted_pack_language != null && !reorderTerms.contains(converted_pack_language) && terms.contains(converted_pack_language)) {
                                    reorderTerms.add(converted_pack_language);
                                    tabLayout.addTab(tabLayout.newTab().setTag(1).setText(robotoStringFont(converted_pack_language)));
                                    selectedPosition = 1;
                                }
                            }
                        }
                    }
                    for (int i = 0; i < terms.size(); i++) {
                        if (terms.get(i) != null && !reorderTerms.contains(terms.get(i))) { // (!terms.get(i).equalsIgnoreCase(convertedGenre)  || !subscribed_languages.contains(terms.get(i)))
                            tabLayout.addTab(tabLayout.newTab().setTag(i).setText(robotoStringFont(terms.get(i))));
                            Log.d("GenreResponse", "terms" + terms);
                        }
                        else{
//                                tabLayout.getTabAt(1).select();

                        }


                        View root = tabLayout.getChildAt(0);
                        //TODO : enable below code for tab dividers and change color code accordingly
                        if (root instanceof LinearLayout) {
                            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                            GradientDrawable drawable = new GradientDrawable();
                            drawable.setColor(getResources().getColor(R.color.epg_date_tab_divider_color));
                            drawable.setSize(3, 1);
                            ((LinearLayout) root).setDividerPadding(20);
                            ((LinearLayout) root).setDividerDrawable(drawable);
                        }
                    }
                    if (tabLayout.getTabCount() < 7) {
                        right_arrow.setVisibility(View.GONE);
                        left_arrow.setVisibility(View.GONE);
                    } else {
                        if(tabLayout.getScrollX()==0){
                            left_arrow.setVisibility(View.GONE);
                        }else {
                            left_arrow.setVisibility(View.VISIBLE);
                        }
                        right_arrow.setVisibility(View.VISIBLE);
                    }
                      /*  ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(
                                mContext.getSupportFragmentManager());
                        viewpager.setAdapter(viewPagerAdapter);*/
                    favouriteTabsNestedAdapter = new FavouriteTabsNestedAdapter(getChildFragmentManager(), tabLayout, true);
                    viewpagerFavourite.setAdapter(favouriteTabsNestedAdapter);
                    selectPage(selectedPosition);
               /* new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(tabLayout!=null) {
                            tabLayout.getTabAt(1).select();
                            viewpagerFavourite.setCurrentItem(1);
                        }
                    }
                },0);*/

                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                Log.d("Favourite", "FavouriteRequest: onResponse: t- " + t);
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                    return;
                }
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
            }
        });
        APIService.getInstance().execute(mRequestFavourites);
    }

    void selectPage(int pageIndex){
        tabLayout.setScrollPosition(pageIndex,0f,true);
        isFirstTimeLoading = true;
        viewpagerFavourite.setCurrentItem(pageIndex);
    }

    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_bold.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    private void setTabMode(List<String> titleTabs){
        for (String module : titleTabs) {
            tabLayout.addTab(tabLayout.newTab().setText(module));
        }

        tabLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                // don't forget to add Tab first before measuring..
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int widthS = displayMetrics.widthPixels;
                tabLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int widthT = tabLayout.getMeasuredWidth();

                if (widthS > widthT) {
                    tabLayout.setTabMode(TabLayout.MODE_FIXED);
                    tabLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT));
                }else
                    tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateOrientation();
    }

    private void updateOrientation(){
        try {

            if(DeviceUtils.isTablet(mContext) && noFavoritesFoundLayout.getVisibility() == View.VISIBLE){
                if(DeviceUtils.getScreenOrientation(mContext) == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE){

                    ViewGroup.LayoutParams layoutParams1 = rl_root_fav.getLayoutParams();
                    ((ViewGroup.MarginLayoutParams) layoutParams1).bottomMargin= (int) mContext.getResources().getDimension(R.dimen._16sdp);
                    rl_root_fav.setLayoutParams(layoutParams1);

                    ViewGroup.LayoutParams layoutParams = noFavoritesFoundLayout.getLayoutParams();
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin= (int) mContext.getResources().getDimension(R.dimen._6sdp);
                    noFavoritesFoundLayout.setLayoutParams(layoutParams);

                    iv_no_favourites.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen._96sdp);
                    iv_no_favourites.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen._96sdp);

                }else {

                    ViewGroup.LayoutParams layoutParams1 = rl_root_fav.getLayoutParams();
                    ((ViewGroup.MarginLayoutParams) layoutParams1).bottomMargin= (int) mContext.getResources().getDimension(R.dimen._46sdp);
                    rl_root_fav.setLayoutParams(layoutParams1);

                    ViewGroup.LayoutParams layoutParams = noFavoritesFoundLayout.getLayoutParams();
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin= (int) mContext.getResources().getDimension(R.dimen._46sdp);
                    noFavoritesFoundLayout.setLayoutParams(layoutParams);

                    iv_no_favourites.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen._150sdp);
                    iv_no_favourites.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen._150sdp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


