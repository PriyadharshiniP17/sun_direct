package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.myplex.model.CardData;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;

import java.util.List;

public class BannerAdapter extends PagerAdapter {

    private Context mContext;
    private List<CarouselInfoData> mDataList ;

    public BannerAdapter(Context context, List<CarouselInfoData> dataList) {
        mContext = context;
        mDataList = dataList;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.favourite_fragment, collection, false);
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

}

