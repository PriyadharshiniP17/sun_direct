package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
//import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.ui.fragment.CategoryFragment;
import com.myplex.myplex.ui.fragment.FavouriteNewFragment;
import com.myplex.myplex.ui.fragment.GenresFragment;
import com.myplex.myplex.ui.fragment.GenresNewFragment;
import com.myplex.myplex.utils.TypefaceSpan;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Ramraju on 2/20/2018.
 */

public class CategoryTabsNestedAdapter extends FragmentStatePagerAdapter {

    List<CarouselInfoData> mListCarouselInfoData;
    Context mContext;
    TabLayout tabLayout;
    boolean isFavourite;
    public HashMap<Integer, Fragment> hashMap = new HashMap<>();

    public CategoryTabsNestedAdapter(FragmentManager fragment, TabLayout tabLayout, boolean isFavourite) {
        super(fragment);
        this.tabLayout = tabLayout;
        this.isFavourite = isFavourite;
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
    public Fragment getItem(int position) {
        Fragment mFragment;
        //if(!isFavourite)
        mFragment = new GenresNewFragment( tabLayout.getTabAt(position).getText().toString());
        hashMap.put(position, mFragment);
      /*  else
            mFragment = new FavouriteNewFragment(tabLayout.getTabAt(position).getText().toString());*/
        return mFragment;
    }

    @Override
    public int getCount() {
        return tabLayout.getTabCount();
    }
}
