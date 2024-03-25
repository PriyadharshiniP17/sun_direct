package com.myplex.myplex.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;

import java.util.List;

public class NavigationDrawerSeperatorItem extends GenericListViewCompoment {

    Context mContext;
    List<CarouselInfoData> mCarouselInfoData;

    public NavigationDrawerSeperatorItem(Context context, View view, List<CarouselInfoData> carouselInfoData) {
        super(view);
        this.mContext=context;
        this.mCarouselInfoData=carouselInfoData;
    }

    public static NavigationDrawerSeperatorItem createView(Context context, ViewGroup parent,
                                                  List<CarouselInfoData> carouselInfoData) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_nav_seperator_view, parent, false);
        View line = view.findViewById(R.id.line);
        line.setVisibility(View.GONE);
        return new NavigationDrawerSeperatorItem(context, view, carouselInfoData);
    }

    @Override
    public void bindItemViewHolder(int position) {
        final CarouselInfoData navDrawerItem = mCarouselInfoData.get(position);

    }
}
