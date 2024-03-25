package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.model.RelatedCastList;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.ui.views.GridSpacingItemDecoration;

import java.util.List;

public class CastAndCrewPillarItem extends GenericListViewCompoment {

    RecyclerView mRecyclerView;
    List<RelatedCastList> relatedCastItems;
    Context mContext;

    public static CastAndCrewPillarItem createView(Context context, ViewGroup parent,
                                                  List<RelatedCastList> carouselInfoData) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_carouselinfo, parent, false);
        return new CastAndCrewPillarItem(context, view, carouselInfoData);
    }

    public CastAndCrewPillarItem(Context mContext,View view,List<RelatedCastList> carouselInfoData) {
        super(view);
        mRecyclerView=view.findViewById(R.id.recyclerview);
        this.relatedCastItems=carouselInfoData;
        this.mContext=mContext;
    }

    @Override
    public void bindItemViewHolder(int position) {
        GridLayoutManager linearLayoutManager = new GridLayoutManager(mContext,2);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        GridSpacingItemDecoration mHorizontalMoviesDivieder = new GridSpacingItemDecoration(2,(int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4),true);
        mRecyclerView.removeItemDecoration(mHorizontalMoviesDivieder);
        mRecyclerView.addItemDecoration(mHorizontalMoviesDivieder);
        mRecyclerView.setItemAnimator(null);
        AdapterPillarItem adapterPillarItem=new AdapterPillarItem(mContext,relatedCastItems.get(position).values,mRecyclerView);
        mRecyclerView.setAdapter(adapterPillarItem);

    }
}
