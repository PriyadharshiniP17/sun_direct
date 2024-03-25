package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APIConstants;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.FragmentAppCarouselInfo;
import com.myplex.myplex.ui.fragment.HomeCarouselInfo;
import com.myplex.myplex.ui.fragment.LiveTVFragment;
import com.myplex.myplex.ui.fragment.SearchSuggestions;
import com.myplex.myplex.ui.fragment.SearchSuggestionsWithFilter;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.recyclerViewScrollListener;
import com.myplex.myplex.ui.fragment.FragmentCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentMoviesCarousel2;
import com.myplex.myplex.ui.fragment.FragmentWebView;
import com.myplex.myplex.ui.fragment.MyDownloadsFragment;
import com.myplex.myplex.ui.fragment.TvGuideFragment;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.myplex.utils.Util;

import java.util.List;

import viewpagerindicator.IconPagerAdapter;

import static com.myplex.api.APIConstants.APP_ACTION_LIVE_PROGRAM_LIST;
import static com.myplex.api.APIConstants.MENU_HOME;
import static com.myplex.api.APIConstants.MENU_LIVE_TV;
import static com.myplex.api.APIConstants.MENU_SEARCH;
import static com.myplex.api.APIConstants.MENU_VOD;


/**
 * Created by Apalya on 12/3/2015.
 */
public class HomePagerAdapterDynamicMenu extends FragmentStatePagerAdapter implements IconPagerAdapter {
    private static final String ACTION_MENU = "menu";
    public static final String ACTION_LAUNCH_WEBPAGE = "launchWebPage";
    private static final String ACTION_EPG = "epg";
    private static final String ACTION_MOVIE = "movie";
    public static final String ACTION_DEEPLINK = "deepLink";
    public static final String ACTION_DOWNLOADS = "downloads";
    private List<CarouselInfoData> mListCarouselInfoData;
    private Context mContext;
    private String genreFilterValues;
    private String langFilterValues;
    private StateListDrawable StateFulImage;
    public Fragment fragment;
    public Fragment homeFragment;
    public Fragment appFragment;
    public Fragment mSearchSuggestionFrag;

    public void setViewScrollListener(recyclerViewScrollListener viewScrollListener) {
        mViewScrollListener = viewScrollListener;
    }

    private recyclerViewScrollListener mViewScrollListener;

    public HomePagerAdapterDynamicMenu(FragmentManager supportFragmentManager, Context mainActivity, String genre, String languages, List<CarouselInfoData> carouselInfoDataList) {
        super(supportFragmentManager);
        mContext = mainActivity;
        genreFilterValues = genre;
        langFilterValues = languages;
        mListCarouselInfoData = carouselInfoDataList;

    }

    private StateListDrawable createStateFulImages(int index) {
        // Create the stateListDrawable progrmatically.
        StateFulImage = new StateListDrawable();

        StateFulImage.addState(new int[]{-android.R.attr.state_selected},
                new BitmapDrawable(mContext.getResources(), Util.getBitmap(mContext, null, mListCarouselInfoData.get(index), false)));
        StateFulImage.addState(new int[]{android.R.attr.state_selected},
                new BitmapDrawable(mContext.getResources(), Util.getBitmap(mContext, null, mListCarouselInfoData.get(index), true)));

        return StateFulImage;
    }


    @Override
    public Fragment getItem(int position) {

        Bundle bundle = new Bundle();
        CarouselInfoData carouselInfoData = mListCarouselInfoData.get(position);
        //Adding a nullCheck for appAction.
        if(carouselInfoData != null) {
            switch (carouselInfoData.name) {
                case MENU_HOME: //Home
                    if (carouselInfoData == null) {
                        homeFragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                        return homeFragment;
                    }
                    if (carouselInfoData != null && carouselInfoData.appAction == null) {
                        CacheManager.setCarouselInfoData(carouselInfoData);
                        bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, carouselInfoData.name);
                        bundle.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, carouselInfoData.title);
                        homeFragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                        return homeFragment;
                    }
                case MENU_LIVE_TV: //Live TV
                    if (carouselInfoData == null) {
                        fragment = Fragment.instantiate(mContext, LiveTVFragment.class.getName(), bundle);
                        return fragment;
                    }
                    if (carouselInfoData != null && carouselInfoData.appAction == null) {
                        CacheManager.setCarouselInfoData(carouselInfoData);
                        bundle.putString(LiveTVFragment.PARAM_APP_FRAG_TYPE, carouselInfoData.name);
                        bundle.putString(LiveTVFragment.PARAM_APP_PAGE_TITLE, carouselInfoData.title);
                        fragment = Fragment.instantiate(mContext, LiveTVFragment.class.getName(), bundle);
                        return fragment;
                    }
                case MENU_VOD:
                    if (carouselInfoData == null) {
                        appFragment = Fragment.instantiate(mContext, FragmentAppCarouselInfo.class.getName(), bundle);
                        return appFragment;
                    }
                    if (carouselInfoData != null && carouselInfoData.appAction == null) {
                        CacheManager.setCarouselInfoData(carouselInfoData);
                        bundle.putString(FragmentAppCarouselInfo.PARAM_APP_FRAG_TYPE, carouselInfoData.name);
                        bundle.putString(FragmentAppCarouselInfo.PARAM_APP_PAGE_TITLE, carouselInfoData.title);
                        appFragment = Fragment.instantiate(mContext, FragmentAppCarouselInfo.class.getName(), bundle);
                        return appFragment;
                    }
                case MENU_SEARCH: //Search
                    Bundle args = new Bundle();
                    args.putString(SearchSuggestionsWithFilter.PARAM_SEARCH_QUERY, "");
                    args.putBoolean(SearchSuggestionsWithFilter.PARAM_SEARCH_ALLOW_BROWSE_MORE, true);
                    Log.d("shortDesc", carouselInfoData.shortDesc);
                    Log.d("title", carouselInfoData.title);
                    args.putString(SearchSuggestionsWithFilter.PARAM_SEARCH_CONTENT_TYPE, "Live");
                    args.putString(SearchSuggestionsWithFilter.PARAM_TAB_NAME, carouselInfoData.title);
                    mSearchSuggestionFrag = SearchSuggestionsWithFilter.newInsance(args);
                    return mSearchSuggestionFrag;
                default:
                    if (carouselInfoData == null) {
                        fragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                        return fragment;
                    }
                    if (carouselInfoData != null && carouselInfoData.appAction == null) {
                        CacheManager.setCarouselInfoData(carouselInfoData);
                        bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, carouselInfoData.name);
                        bundle.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, carouselInfoData.title);
                        fragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                        return fragment;
                    }
            }
        }

        switch (carouselInfoData.appAction) {
            case ACTION_EPG:
                fragment = Fragment.instantiate(mContext, TvGuideFragment.class.getName(), bundle);
                break;
            case ACTION_LAUNCH_WEBPAGE:

              break;
            case APP_ACTION_LIVE_PROGRAM_LIST:
            case ACTION_MENU:
                CacheManager.setCarouselInfoData(carouselInfoData);
                bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, carouselInfoData.name);
                bundle.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, carouselInfoData.title);
                bundle.putSerializable(FragmentCarouselInfo.PARAM_CAROUSAL, carouselInfoData);
                if (carouselInfoData.title != null && carouselInfoData.title.equalsIgnoreCase(ACTION_DOWNLOADS)) {
                    bundle.putBoolean(MyDownloadsFragment.PARAM_SHOW_TOOLBAR, false);
                    fragment = Fragment.instantiate(mContext, MyDownloadsFragment.class.getName(), bundle);
                } else {
                    fragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                }
                break;
            case ACTION_MOVIE:
                CacheManager.setCarouselInfoData(carouselInfoData);
                fragment = Fragment.instantiate(mContext, FragmentMoviesCarousel2.class.getName()
                        , bundle);
                break;
            case ACTION_DOWNLOADS:
                fragment = Fragment.instantiate(mContext, MyDownloadsFragment.class.getName(), bundle);
                break;
            default:
                fragment = Fragment.instantiate(mContext, TvGuideFragment.class.getName(), bundle);
        }
        try {
            ((FragmentCarouselInfo) fragment).setHomePagerAdapterDynamicMenu(this);
            if (mViewScrollListener != null) {
                ((FragmentCarouselInfo) fragment).setViewScrollListener(mViewScrollListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragment;
    }
/*
    private List<String> checkAndUpdatePageOrderFromMixpanelPageOrder() {
        String mixpanelPageOrder = PrefUtils.getInstance().getPrefMixpanelMenuOrder();
        if(TextUtils.isEmpty(mixpanelPageOrder)){
            return defaultPageNames;
        }
        String COMMA_EXPR = ",";
        if (mixpanelPageOrder.contains(COMMA_EXPR)) {
            defaultPageNames.clear();
            defaultPageNames.addAll(Arrays.asList(mixpanelPageOrder.split(COMMA_EXPR)));
        }
        return defaultPageNames;
    }*/

    public int getPositionInCarousalWhenClickedBottomTab(String name) {
        int i = 0;
        if (mListCarouselInfoData != null && mListCarouselInfoData.size() > 0) {
            for (; i < mListCarouselInfoData.size(); i++) {

                if (mListCarouselInfoData.get(i).title.equalsIgnoreCase(name)) {
                    return i;
                }

            }
        }

        return 0;

    }
    @Override
    public int getIconResId(int index) {
        return 0;
    }

    @Override
    public StateListDrawable getDynamicIcons(int index) {

        return createStateFulImages(index);

    }

    @Override
    public int getCount() {
        return mListCarouselInfoData != null ? mListCarouselInfoData.size() : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Spannable st = robotoStringFont(mListCarouselInfoData.get(position).title);
        if (PrefUtils.getInstance().getVernacularLanguage() && (mListCarouselInfoData.get(position).altTitle != null && !mListCarouselInfoData.get(position).altTitle.isEmpty())) {
            st = robotoStringFont(mListCarouselInfoData.get(position).altTitle);
        }
        return st;
    }

    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "Roboto-Medium.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    public int getItemPosition(Object object) {
        return POSITION_UNCHANGED;
    }

    public List<CarouselInfoData> getPageList() {
        return mListCarouselInfoData;
    }


  /*  public Bitmap getDefaultBitmap(boolean isSelected, String carousalName){
        Bitmap bitmap;
        if (carousalName.equals(APIConstants.MENU_TYPE_GROUP_ANDROID_HOME_SUN)) {
            if (!isSelected) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.home);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.home_selected);
            }
        }else if(carousalName.equals(APIConstants.MENU_TYPE_GROUP_ANDROID_MOVIES)){
            if (!isSelected) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.movies);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.movies_selected);
            }

        }else if (carousalName.equals(APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW)){
            if (!isSelected) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.tv_shows);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.tv_shows_selected);
            }

        }else if (carousalName.equals(APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS)){
            if (!isSelected) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_selected);
            }

        }else if (carousalName.equals(APIConstants.MENU_TYPE_GROUP_ANDROID_COMEDY_CLIPS)){
            if (!isSelected) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.deselected_comedy);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.selected_comedy);
            }

        }else {
            if (!isSelected) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.tv_shows);
            } else {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.tv_shows_selected);
            }

        }
        return  bitmap;
    }*/
}
