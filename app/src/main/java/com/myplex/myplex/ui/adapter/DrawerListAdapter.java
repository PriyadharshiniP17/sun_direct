package com.myplex.myplex.ui.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.ViewGroup;

import com.myplex.api.APIConstants;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ProfileAPIListAndroid;
import com.myplex.myplex.ui.views.NavigationDrawerItem;
import com.myplex.myplex.ui.views.NavigationDrawerSeperatorItem;
import com.myplex.myplex.ui.views.UiCompoment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Vijay Dhas Gilbert on 4/19/2017.
 */

public class DrawerListAdapter extends RecyclerView.Adapter<UiCompoment> {

    private OnItemClickListener mOnItemClickListener;
    private static final byte ITEM_TYPE_NAV_DRAWER_ITEM = 1;
    private static final byte ITEM_TYPE_NAV_DRAWER_SEPERATOR = 2;

    private List<CarouselInfoData> navItemsList;
    private Context mContext;

    public static int selectedItem = 0;


    public DrawerListAdapter(List<CarouselInfoData> navItemsList, Context mContext) {
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
            case ITEM_TYPE_NAV_DRAWER_ITEM:
                viewHolder = NavigationDrawerItem.createView(mContext, parent, navItemsList);
                viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
                break;
            case ITEM_TYPE_NAV_DRAWER_SEPERATOR:
                viewHolder= NavigationDrawerSeperatorItem.createView(mContext,parent,navItemsList);
                //viewHolder.setUiComponentListenerInterface(uiCompomentListenerInterface);
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
        if (navItemsList!=null&&navItemsList.get(position).layoutType!=null&& !TextUtils.isEmpty(navItemsList.get(position).layoutType)){
            if (navItemsList.get(position).layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_NAVIGATION_SEPERATOR)){
                return ITEM_TYPE_NAV_DRAWER_SEPERATOR;
            }else {
                return ITEM_TYPE_NAV_DRAWER_ITEM;
            }
        }
        return ITEM_TYPE_NAV_DRAWER_SEPERATOR;
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

    public void addOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

}