package com.myplex.myplex.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.Util;

import java.util.List;

public class NavigationDrawerItem extends GenericListViewCompoment {

    private List<CarouselInfoData> navItemsList;
    private Context mContext;
    private StateListDrawable StateFulImage;

    public static int selectedItem = 0;
    private Typeface mNormalTypeFace;
    public NavigationDrawerItem(Context context, View view, List<CarouselInfoData> carouselInfoData) {
        super(view);
        this.mContext=context;
        this.navItemsList=carouselInfoData;
        mNormalTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");
    }

    public static NavigationDrawerItem createView(Context context, ViewGroup parent,
                                                     List<CarouselInfoData> carouselInfoData) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_navdrawer, parent, false);
        return new NavigationDrawerItem(context, view, carouselInfoData);
    }

    @Override
    public void bindItemViewHolder(int position) {
        final CarouselInfoData navDrawerItem = navItemsList.get(position);
        mTitle.setText(navDrawerItem.title);
        if (ApplicationController.IS_VERNACULAR_TO_BE_SHOWN) {
            mTitleLang.setVisibility(View.VISIBLE);
            mTitleLang.setText(navDrawerItem.altTitle);
        } else {
            mTitleLang.setVisibility(View.GONE);
        }
        mTitle.setTypeface(mNormalTypeFace);


        String iconUrl = getImageLink(navDrawerItem.images);
        if (!TextUtils.isEmpty(iconUrl)) {
            /*Picasso.with(mContext)
                    .load(iconUrl)
                    .into(holder.mImage);*/
            mImage.setImageDrawable(getStatefulimage(position,navDrawerItem));
        } else {
            mImage.setImageResource(navDrawerItem.menuIcon);
        }

        /*if (position == selectedItem) {
            itemView.setSelected(true);
            mTitle.setTextColor(mContext.getResources().getColor(R.color.navigation_drawer_color_text_sec_iter));
            mTitleLang.setTextColor(mContext.getResources().getColor(R.color.navigation_drawer_color_text_sec_iter));
        } else {
            itemView.setSelected(false);
            mTitle.setTextColor(mContext.getResources().getColor(R.color.navigation_drawer_color_text_sec_iter));
            mTitleLang.setTextColor(mContext.getResources().getColor(R.color.navigation_drawer_color_text_sec_iter));
        }*/
        itemView.setOnClickListener(v -> {
            notifyItemClicked(navItemsList.get(position),position);
        });
    }

    private String getImageLink(List<CardDataImagesItem> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_ICON};
        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : images) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.XXHDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }

    private void notifyItemClicked(final CarouselInfoData carouselInfoData, final int mParentPosition) {

        if (carouselInfoData == null) {
            LoggerD.debugLog("removeItem: invalid operation of removal " + carouselInfoData == null ? " no carousel title" : carouselInfoData.title);
            return;
        }
        notifyItemRemoved(carouselInfoData);
    }



    private StateListDrawable getStatefulimage(int index, CarouselInfoData mListCarouselInfoData) {
        // Create the stateListDrawable progrmatically.
        StateFulImage = new StateListDrawable();

        StateFulImage.addState(new int[]{-android.R.attr.state_selected},
                new BitmapDrawable(mContext.getResources(), Util.getBitmap(mContext, null,mListCarouselInfoData,false)));
        StateFulImage.addState(new int[]{android.R.attr.state_selected},
                new BitmapDrawable(mContext.getResources(), Util.getBitmap(mContext,null,mListCarouselInfoData,true)));

        return StateFulImage;
    }
}
