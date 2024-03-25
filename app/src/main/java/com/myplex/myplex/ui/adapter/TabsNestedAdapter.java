package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.fragment.CategoryFragment;
import com.myplex.myplex.ui.fragment.FragmentCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentViewpagerCarouselInfo;
import com.myplex.myplex.utils.TypefaceSpan;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Ramraju on 2/20/2018.
 */

public class TabsNestedAdapter extends FragmentStateAdapter {

    List<CarouselInfoData> mListCarouselInfoData;
    Context mContext;
    public HashMap<Integer, Fragment> hashMap = new HashMap<>();

    public TabsNestedAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void setTabsData(List<CarouselInfoData> mListCarouselInfoData,Context mContext){
        this.mListCarouselInfoData=mListCarouselInfoData;
        this.mContext=mContext;
    }

   /* public TabsNestedAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle bundle=new Bundle();
        Fragment fragmentCarouselInfo=null;
        bundle.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, mListCarouselInfoData.get(position).name);
        fragmentCarouselInfo = Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), bundle);
        return fragmentCarouselInfo;
    }

    @Override
    public int getCount() {
        return mListCarouselInfoData.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return robotoStringFont(mListCarouselInfoData.get(position).title);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Yet another bug in FragmentStatePagerAdapter that destroyItem is called on fragment that hasnt been added. Need to catch
        try {
            super.destroyItem(container, position, object);
        } catch (IllegalStateException ex) {
            LoggerD.debugDownload("Exception destroyItem- " + ex.getMessage());
            ex.printStackTrace();
        }catch(Exception e){
            LoggerD.debugDownload("Exception destroyItem- " + e.getMessage());
            e.printStackTrace();
        }
    }*/

    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_regular.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle=new Bundle();
        Fragment fragmentCarouselInfo=null;
        bundle.putString(FragmentViewpagerCarouselInfo.PARAM_APP_FRAG_TYPE, mListCarouselInfoData.get(position).name);
        CacheManager.setCarouselInfoData(mListCarouselInfoData.get(position));
        fragmentCarouselInfo = Fragment.instantiate(mContext, FragmentViewpagerCarouselInfo.class.getName(), bundle);
        hashMap.put(position, fragmentCarouselInfo);
        //Fragment mFragment = new CategoryFragment(mListCarouselInfoData.get(position));
        return fragmentCarouselInfo;
    }

    @Override
    public int getItemCount() {
        return mListCarouselInfoData.size();
    }
}
