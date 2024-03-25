package com.myplex.myplex.ui.component;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.myplex.model.CardData;
import com.myplex.model.EpisodeDisplayData;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.utils.TypefaceSpan;

import java.util.List;

public class SeasonsEpisodeTabsAdapter extends FragmentStatePagerAdapter {

    List<CardData> mListCarouselInfoData;
    Context mContext;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private CardData mTVShowCardData;
    private List<DetailsViewContent.DetailsViewDataItem> mValues;
    private List<EpisodeDisplayData> tabNames;
    private String mBgColor;
    public void setTabsData(List<CardData> mListCarouselInfoData){
        this.mListCarouselInfoData=mListCarouselInfoData;
    }

    public SeasonsEpisodeTabsAdapter(FragmentManager fragmentManager,
                                     CardDetailViewFactory.CardDetailViewFactoryListener mListener,
                                     List<DetailsViewContent.DetailsViewDataItem> data, Context mContext,
                                     List<CardData> mListCarouselInfo,
                                     CardData mTVShowCardData, List<EpisodeDisplayData> tabNames,
                                     String mBgColor){
        super(fragmentManager);
        this.mListener=mListener;
        this.mValues=data;
        this.mContext=mContext;
        this.mTVShowCardData=mTVShowCardData;
        this.tabNames=tabNames;
        this.mBgColor=mBgColor;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        EpisodeTabFragment episodeTabFragment=new EpisodeTabFragment().newInstance(mListener,position,mValues,
                mListCarouselInfoData,mTVShowCardData,tabNames.get(position),mBgColor);
        CacheManager.setCardDataList(mListCarouselInfoData);
        return episodeTabFragment;
    }

    @Override
    public int getCount() {
        return tabNames.size();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        Spannable st;
      //  st = robotoStringFont(tabNames.get(position).episodeTabName);
        String pos= String.valueOf(position+1);
        st = robotoStringFont(tabNames.get(position).episodeTabName+" "+pos);
        return st;
    }



    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "Roboto-Medium.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }
}

