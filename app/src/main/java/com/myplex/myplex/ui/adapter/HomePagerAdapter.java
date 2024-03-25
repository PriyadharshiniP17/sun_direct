package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;

import com.myplex.api.APIConstants;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.ui.fragment.FragmentCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentMoviesCarousel2;
import com.myplex.myplex.ui.fragment.TvGuideFragment;
import com.myplex.myplex.utils.TypefaceSpan;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Apalya on 12/3/2015.
 */
public class HomePagerAdapter extends FragmentStatePagerAdapter {
    private static final String PAGE_MUSIC = "Music";
    private static final String PAGE_VIDEOS = "Videos";
    private static final String PAGE_TVSHOWS = "TV Shows";
    private static final String PAGE_MOVIES = "Movies";
    private static final String PAGE_LIVETV = "Live TV";

    public static String getPageYoutube() {
        return PAGE_YOUTUBE;
    }

    private static final String PAGE_YOUTUBE = "YouTube";

    public static String getPageMusicVideos() {
        return PAGE_MUSIC_VIDEOS;
    }

    public static String getPageMovieTrailer() {
        return PAGE_MOVIE_TRAILER;
    }

    private static final String PAGE_MUSIC_VIDEOS = "Music Videos";
    private static final String PAGE_KIDS = "Kids";
    private static final String PAGE_MOVIE_TRAILER = "Movie Trailer";
    //            0 = "TV Shows"
//            1 = " Live TV"
//            2 = " Movies"
//            3 = " Music"
//            4 = " Videos"
    private Context mContext;
    private List<String> defaultPageNames = new ArrayList<>();
    private String genreFilterValues;
    private String langFilterValues;

    /*public static enum HomePageTabs{
        LIVETV, MOVIES, MUSIC, TV_SHOWS,VIDEOS;
    }*/

    public HomePagerAdapter(FragmentManager supportFragmentManager, Context mainActivity, String genre, String languages) {
        super(supportFragmentManager);
        mContext = mainActivity;
        genreFilterValues = genre;
        langFilterValues  = languages;
        preparePageOrder();

    }

    private void preparePageOrder() {
        defaultPageNames.add(getPageLivetv());
        defaultPageNames.add(getPageMovies());
        defaultPageNames.add(getPageTvshows());
//        defaultPageNames.add(getPageMusicVideos());
        defaultPageNames.add(getPageMusic());
        defaultPageNames.add(getPageKids());
        defaultPageNames.add(getPageVideos());

        if(!PrefUtils.getInstance().getPrefEnableMusicTab()){
            defaultPageNames.remove(getPageMusic());
        }
    }

    public static final String getPageMusic() {
        return PAGE_MUSIC;
    }

    public static final String getPageVideos() {
        return PAGE_VIDEOS;
    }

    public static final String getPageTvshows() {
        return PAGE_TVSHOWS;
    }

    public static final String getPageMovies() {
        return PAGE_MOVIES;
    }

    public static final String getPageLivetv() {
        return PAGE_LIVETV;
    }

    public final static String getPageKids() {
        return PAGE_KIDS;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        switch (defaultPageNames.get(position)) {
            case PAGE_MOVIES:
//                bundle.putInt(AppsListFragment.PARAM_APP_FRAG_TYPE, AppsListFragment.PARAM_APP_FRAG_MOVIE);
                fragment = Fragment.instantiate(mContext, FragmentMoviesCarousel2.class.getName()
                        , bundle);
                break;
            case PAGE_TVSHOWS:
                bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW);
                fragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                break;
            case PAGE_VIDEOS:
                bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS);
                fragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                break;
            case PAGE_MUSIC:

                bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS);
                fragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                break;
            case PAGE_LIVETV:
                fragment = Fragment.instantiate(mContext, TvGuideFragment.class.getName(), bundle);
                break;
            case PAGE_KIDS:
                bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS);
                fragment = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
                break;
            default:
                fragment = Fragment.instantiate(mContext, TvGuideFragment.class.getName(), bundle);
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


    @Override
    public int getCount() {
        return defaultPageNames.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Spannable st = robotoStringFont(defaultPageNames.get(position));
        return st;
    }

    public Spannable robotoStringFont(String name){
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext,"Roboto-Medium.ttf"), 0, spanString.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString ;
    }
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public List<String> getPageList(){
        return defaultPageNames;
    }
}
