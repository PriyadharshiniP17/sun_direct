package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.api.APIConstants;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ProfileAPIListAndroid;
import com.myplex.model.RelatedCastList;
import com.myplex.myplex.ui.views.UiCompoment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdapterCastAndCrew extends RecyclerView.Adapter<UiCompoment> {

    private DrawerListAdapter.OnItemClickListener mOnItemClickListener;
    private static final byte PILLAR_ITEM = 1;
    private static final byte ROLE_NAME_ITEM=3;

    private List<RelatedCastList> navItemsList;
    private Context mContext;


    public AdapterCastAndCrew(List<RelatedCastList> navItemsList, Context mContext) {
        this.navItemsList = navItemsList;
        this.mContext = mContext;
    }

    @NotNull
    @Override
    public UiCompoment onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
/*
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_navdrawer, parent, false);
        if (DeviceUtils.isTablet(mContext)
                && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            itemView.getLayoutParams().height = ApplicationController.getApplicationConfig().screenHeight / (navItemsList.size() + 3);
        }
*/

        UiCompoment viewHolder = null;
        switch (viewType) {
            case PILLAR_ITEM:
                viewHolder = CastAndCrewPillarItem.createView(mContext, parent, navItemsList);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ROLE_NAME_ITEM:
                viewHolder= CastAndCrewRoleNameItem.createView(mContext,parent,navItemsList);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UiCompoment holder, int position) {
        holder.bindItemViewHolder(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (navItemsList != null && navItemsList.get(position).mLayoutType != null && !TextUtils.isEmpty(navItemsList.get(position).mLayoutType)) {
            if (navItemsList.get(position).mLayoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_ROLE_NAME_LAYOUT)) {
                return ROLE_NAME_ITEM;
            } else if (navItemsList.get(position).mLayoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_PILLAR_LAYOUT)) {
                return PILLAR_ITEM;
            }
        }
        return ROLE_NAME_ITEM;
    }

    @Override
    public int getItemCount() {
        return navItemsList.size();
    }

    private UiCompoment.UiComponentListenerInterface uiCompomentListenerInterface = new UiCompoment.UiComponentListenerInterface() {
        @Override
        public void notifyDataChanged(int position) {
            notifyItemChanged(position);
        }

        @Override
        public void notifyItemNeedToBeRemoved(final CarouselInfoData carouselInfoData,final int position) {
            mOnItemClickListener.onOnItemClicked(carouselInfoData,position);
        }

        @Override
        public void notifyItemNeedToBeRemoved(ProfileAPIListAndroid carouselInfoData, int position) {

        }
    };

    public interface OnItemClickListener {
        void onOnItemClicked(CarouselInfoData navDrawerItem, int position);
    }

    public void addOnItemClickListener(DrawerListAdapter.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

}
