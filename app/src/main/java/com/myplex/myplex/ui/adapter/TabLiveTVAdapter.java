package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.myplex.myplex.ui.fragment.FavouriteFragment;
import com.myplex.myplex.ui.fragment.FavouriteNewFragment;
import com.myplex.myplex.ui.fragment.FavouriteViewPagerFragment;
import com.myplex.myplex.ui.fragment.GenresFragment;
import com.myplex.myplex.ui.fragment.GenresViewPagerFragment;
import com.myplex.myplex.ui.fragment.epg.EPGFragment;
import com.myplex.myplex.utils.TypefaceSpan;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Ramraju on 2/20/2018.
 */

public class TabLiveTVAdapter extends FragmentStateAdapter {

    List<String> mListCarouselInfoData;
    Context mContext;
    public Fragment mFragment;
    public HashMap<Integer, Fragment> hashMap = new HashMap<>();

    public TabLiveTVAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void setTabsData(List<String> mListCarouselInfoData,Context mContext){
        this.mListCarouselInfoData=mListCarouselInfoData;
        this.mContext=mContext;
    }


    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "font/amazon_ember_cd_regular.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
       /* Bundle bundle=new Bundle();
        Fragment fragmentCarouselInfo=null;
        bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, mListCarouselInfoData.get(position).name);
        CacheManager.setCarouselInfoData(mListCarouselInfoData.get(position));
        fragmentCarouselInfo = Fragment.instantiate(mContext, CategoryFragment.class.getName(), bundle);*/
        switch (position){
            case 0:
                mFragment = new GenresViewPagerFragment(mListCarouselInfoData.get(position));
                hashMap.put(position, mFragment);
                return mFragment;
            case 1 :
                Fragment mFragment1 = new FavouriteViewPagerFragment(mListCarouselInfoData.get(position));
              //  Fragment mFragment1 = Fragment.instantiate(mContext, TvGuideFragment.class.getName(), bundle1);
                hashMap.put(position, mFragment1);
                return mFragment1;
            default:
                Bundle bundle = new Bundle();
                Fragment epgFragment = Fragment.instantiate(mContext, EPGFragment.class.getName(), bundle);
                hashMap.put(position, epgFragment);
                return epgFragment;
        }

    }

    @Override
    public int getItemCount() {
        return mListCarouselInfoData.size();
    }
}
