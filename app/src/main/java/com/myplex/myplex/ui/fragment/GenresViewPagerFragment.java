package com.myplex.myplex.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.SectionsListRequest;
import com.myplex.model.CardData;
import com.myplex.model.SectionsListResponse;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.adapter.CategoryTabsNestedAdapter;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GenresViewPagerFragment extends Fragment {

    private FragmentActivity mContext;
    public List<CardData> cardsList = new ArrayList<>();
    private TabLayout tabLayout;
    private ImageView left_arrow, right_arrow;
    Animation animation;
    private String selectedGenre = "";
    private String selected_language  ;
    private List<String> subscribed_languages;
    private ViewPager viewpager;
    CategoryTabsNestedAdapter categoryFragment;
    private boolean isFromTab;
    private boolean isFromPage;

    public GenresViewPagerFragment(){

    }

    public GenresViewPagerFragment(String tabName) {
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
//        mContext = getActivity();
        mContext=requireActivity();
        animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
        View view = inflater.inflate(R.layout.genre_viewpager_fragment, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        viewpager = (ViewPager) view.findViewById(R.id.viewpager);
        left_arrow = (ImageView) view.findViewById(R.id.left_arrow);
        right_arrow = (ImageView) view.findViewById(R.id.right_arrow);
        left_arrow.setVisibility(View.GONE);
//        genresListAPICall();
        tabLayout.setHorizontalScrollBarEnabled(true);
        //  viewpager.setCurrentItem(0);
        //  viewpager.setOffscreenPageLimit(1);
        //  viewpager.setUserInputEnabled(true);
      /*  new TabLayoutMediator(tabLayout, viewpager,
                (tab, position) -> tab.setText(robotoStringFont(""))
        ).attach();*/
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(!isFromTab) {
                    isFromPage = true;
                    tabLayout.getTabAt(position).select();

                }
                isFromTab = false;
                //Fixed the highlighter is not getting selected on fast scrolling of sections
                tabLayout.setScrollPosition(position,0f,true);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
      /*  viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
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
                if(!isFromTab) {
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
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                  /*  String tabCode = tab.getTag().toString();
                    int position = Integer.parseInt(tabCode);
                    isFromTab = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            viewpager.setCurrentItem(tab.getPosition(), false);
                        }
                    }, 100);*/
                // isFromTab = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("GenresViewPagerFragment", "run: isFromPage "+ isFromPage + " tab.getPosition() " + tab.getPosition());
                        if(!isFromPage) {
                            isFromTab = true;
                            viewpager.setCurrentItem(tab.getPosition(), false);
                        }
                        isFromPage = false;
                    }
                }, 500);

                //  viewpager.setCurrentItem(tab.getPosition(), false);
                APIConstants.IS_REFRESH_LIVETV1=true;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
       /* viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
              //  tabLayout.getTabAt(position).select();
              //  viewpager.setCurrentItem(position);
            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();
                viewpager.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/
        if(DeviceUtils.isTablet(mContext)){
            left_arrow.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.icon_left_arrow));
            right_arrow.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.icon_right_arrow));
        }
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


        genresListAPICall();
        tabLayout.smoothScrollTo(0, 0);
        return view;
    }

    public void scrollToTop(){
        if(categoryFragment != null && categoryFragment.hashMap != null){
            Fragment screenFragment =  categoryFragment.hashMap.get(viewpager.getCurrentItem());
            if(screenFragment != null && screenFragment instanceof GenresNewFragment)
                ((GenresNewFragment)screenFragment).startToScroll();
        }
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
        if (tabLayout.getScrollX() - (tabLayout.getWidth() / 2) > ApplicationController.getApplicationConfig().screenWidth) {
            right_arrow.setVisibility(View.GONE);
        } else {
            right_arrow.setVisibility(View.VISIBLE);
        }

    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(@NonNull FragmentManager fm)
        {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            Fragment fragment = null;
            fragment = new GenresFragment("");
            return fragment;
        }

        @Override
        public int getCount()
        {
            return tabLayout.getTabCount();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return "";
        }
    }

    public void genresListAPICall() {
        Fragment fragment = this;
        SectionsListRequest mRequestFavourites = new SectionsListRequest(
                new APICallback<SectionsListResponse>() {
                    @Override
                    public void onResponse(APIResponse<SectionsListResponse> response) {
                        if (!isAdded())
                            return;
                        if (response == null
                                || response.body() == null) {
                            return;
                        }
                        List<String> terms = response.body().getResults();
                        List<String> reorderTerms = new ArrayList<>();
                        //Log.d("Favourite", "FavouriteRequest: onResponse: message - " + response.body().message);
                        tabLayout.removeAllTabs();
                        tabLayout.addTab(tabLayout.newTab().setTag(0).setText(robotoStringFont("All")));

                         /*else {
                            LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
                            if(locationInfo!=null && locationInfo.languageCode != null && locationInfo.languageName != null){

                            }
                        }*/

                        if(PrefUtils.getInstance().getAppLanguageFirstTime()!=null && PrefUtils.getInstance().getAppLanguageFirstTime().equalsIgnoreCase("true")) {
                            subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
                            String converted_pack_language = "";
                            if (subscribed_languages != null && subscribed_languages.size() > 0) {
                                for (int i = 0; i < subscribed_languages.size(); i++) {
                                    converted_pack_language = subscribed_languages.get(i).substring(0, 1).toUpperCase() + subscribed_languages.get(i).substring(1);
                                    if (converted_pack_language != null && !reorderTerms.contains(converted_pack_language)) {
                                        reorderTerms.add(converted_pack_language);
                                        tabLayout.addTab(tabLayout.newTab().setTag(1).setText(robotoStringFont(converted_pack_language)));
                                    }
                                }
                            }
                            selected_language = PrefUtils.getInstance().getAppLanguageToSendServer();
                            String convertedGenre = "";
                            if (selected_language != null && !selected_language.isEmpty()) {
                                String newGenre = selected_language.toLowerCase(Locale.ROOT);
                                convertedGenre = newGenre.substring(0, 1).toUpperCase() + newGenre.substring(1);
                                if (convertedGenre != null && !reorderTerms.contains(convertedGenre)) {
                                    reorderTerms.add(convertedGenre);
                                    tabLayout.addTab(tabLayout.newTab().setTag(2).setText(robotoStringFont(convertedGenre)));
                                }
                            }
                        }else{
                            selected_language = PrefUtils.getInstance().getAppLanguageToSendServer();
                            String convertedGenre = "";
                            if(selected_language != null && !selected_language.isEmpty()) {
                                String newGenre = selected_language.toLowerCase(Locale.ROOT);
                                convertedGenre = newGenre.substring(0, 1).toUpperCase() + newGenre.substring(1);
                                if ( convertedGenre != null && !reorderTerms.contains(convertedGenre)) {
                                    reorderTerms.add(convertedGenre);
                                    tabLayout.addTab(tabLayout.newTab().setTag(2).setText(robotoStringFont(convertedGenre)));
                                }
                            }
                            subscribed_languages=PrefUtils.getInstance().getSubscribedLanguage();
                            String converted_pack_language="";
                            if(subscribed_languages!=null && subscribed_languages.size()>0) {
                                for(int i=0;i<subscribed_languages.size();i++) {
                                    converted_pack_language = subscribed_languages.get(i).substring(0, 1).toUpperCase() +subscribed_languages.get(i).substring(1);
                                    if ( converted_pack_language != null && !reorderTerms.contains(converted_pack_language)) {
                                        reorderTerms.add(converted_pack_language);
                                        tabLayout.addTab(tabLayout.newTab().setTag(1).setText(robotoStringFont(converted_pack_language)));
                                    }
                                }
                            }
                        }
                        if(terms!=null)
                            for (int i = 0; i < terms.size(); i++) {
                                if ( terms.get(i) != null && !reorderTerms.contains(terms.get(i))) { // (!terms.get(i).equalsIgnoreCase(convertedGenre)  || !subscribed_languages.contains(terms.get(i)))
                                    tabLayout.addTab(tabLayout.newTab().setTag(i).setText(robotoStringFont(terms.get(i))));
                                    Log.d("GenreResponse","terms"+terms);
                                }
                            /*else{
                                tabLayout.getTabAt(1).select();
                            }*/

                                View root = tabLayout.getChildAt(0);
                                //TODO : enable below code for tab dividers and change color code accordingly
                                if (root instanceof LinearLayout) {
                                    ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                                    GradientDrawable drawable = new GradientDrawable();
                                    drawable.setColor(getResources().getColor(R.color.epg_date_tab_divider_color));
                                    drawable.setSize(3, 1);
                                    ((LinearLayout) root).setDividerPadding(10);
                                    ((LinearLayout) root).setDividerDrawable(drawable);
                                }
                            }
                        if (tabLayout.getTabCount() < 5) {
                            right_arrow.setVisibility(View.GONE);
                            left_arrow.setVisibility(View.GONE);
                        } else {
                            left_arrow.setVisibility(View.VISIBLE);
                            right_arrow.setVisibility(View.VISIBLE);
                        }
                      /*  ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(
                                mContext.getSupportFragmentManager());
                        viewpager.setAdapter(viewPagerAdapter);*/
                        categoryFragment = new CategoryTabsNestedAdapter(getChildFragmentManager(), tabLayout,false);
                        viewpager.setAdapter(categoryFragment);
                        selectPage(1);
                       /* new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(tabLayout!=null) {
                                    tabLayout.getTabAt(1).select();
                                }
                            }
                        },0);*/
                        //  tabLayout.setupWithViewPager(viewpager);

                        //   fetchMovieData(selectedGenre);
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

    /* public void setData(String genresName) {
         if (genresName.equalsIgnoreCase("All")) {
             selectedGenre = "";
             fetchMovieData("");
         } else {
             selectedGenre = genresName;
             fetchMovieData(genresName);
         }
     }*/
    void selectPage(int pageIndex){
//        tabLayout.setScrollPosition(pageIndex,0f,true);
        //Fixed the issue when we resume the application in Channels page from background, the first genre's data is getting under the second genre
        if(tabLayout!=null && tabLayout.getTabAt(pageIndex)!=null) {
            tabLayout.getTabAt(pageIndex).select();
        }
        viewpager.setCurrentItem(pageIndex);
    }


    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        if(spanString.length()>0) {
            spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_bold.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return spanString;
    }
    @Override
    public void onResume() {
        super.onResume();
        if(APIConstants.IS_REFRESH_LIVETV1){
            genresListAPICall();
            tabLayout.smoothScrollTo(0, 0);
            return;
        }

//        fetchMovieData(selectedGenre);

    }

    @Override
    public void onPause() {
        super.onPause();
        APIConstants.IS_REFRESH_LIVETV1=false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}


