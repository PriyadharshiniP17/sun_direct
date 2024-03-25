package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.model.RelatedCastList;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;

import java.util.List;


public class CastAndCrewRoleNameItem extends GenericListViewCompoment {

    RecyclerView mRecyclerView;
    List<RelatedCastList> relatedCastItems;
    Context mContext;

    public static CastAndCrewRoleNameItem createView(Context context, ViewGroup parent,
                                                        List<RelatedCastList> carouselInfoData) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_carouselinfo, parent, false);
        return new CastAndCrewRoleNameItem(context, view, carouselInfoData);
    }

    public CastAndCrewRoleNameItem(Context mContext,View view,List<RelatedCastList> carouselInfoData) {
        super(view);
        mRecyclerView=view.findViewById(R.id.recyclerview);
        this.relatedCastItems=carouselInfoData;
        this.mContext=mContext;
    }

    @Override
    public void bindItemViewHolder(int position) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        VerticalSpaceItemDecoration mHorizontalMoviesDivieder = new VerticalSpaceItemDecoration((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_2));
        mRecyclerView.removeItemDecoration(mHorizontalMoviesDivieder);
        mRecyclerView.addItemDecoration(mHorizontalMoviesDivieder);
        mRecyclerView.setItemAnimator(null);
        AdapterCastNameItem adapterCastNameItem=new AdapterCastNameItem(mContext,relatedCastItems.get(position).values,mRecyclerView);
        mRecyclerView.setAdapter(adapterCastNameItem);
    }
}
