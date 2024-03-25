package com.myplex.myplex.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.model.CarouselInfoData;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.MainActivity;

public class VmaxVideoAdViewItem extends GenericListViewCompoment{

    private Context mContext;
    private static final String TAG = BigHorizontalItem.class.getSimpleName();
    private View vmaxAdViewBanner;
    private GenericListViewCompoment holder = this;
    private RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData carouselInfoData;


    public VmaxVideoAdViewItem(Context mContext, View view, RecyclerView mRecyclerViewCarouselInfo,CarouselInfoData carouselInfoData) {
        super(view);
        this.vmaxAdViewBanner = view;
        this.mContext = mContext;
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        this.carouselInfoData = carouselInfoData;
    }


    public static VmaxVideoAdViewItem createView(Context context,CarouselInfoData carouselInfoData, RecyclerView mRecyclerViewCarouselInfo){
        return new VmaxVideoAdViewItem(context,getVmaxAdView(context,carouselInfoData),mRecyclerViewCarouselInfo,carouselInfoData);
    }

    public static View getVmaxAdView(Context mContext,CarouselInfoData carouselInfoData) {
        String adId = null;//PrefUtils.getInstance().getPrefVmaxNativeAdId();
        adId = PrefUtils.getInstance().getPrefVmaxNativeVideoAdId();
        if (carouselInfoData != null && !TextUtils.isEmpty(carouselInfoData.title)) {
            //adId = PropertiesHandler.propertiesAdSpotIdMap.get(mCarouselInfoData.title);
            adId = PropertiesHandler.getPropertiesNativeVideoAdSpotIdMap().get(carouselInfoData.title);
        }
        SDKLogger.debug("adspotIds- " + adId);
        if (adId == null) {
            adId = "";
        }
        if (adId != null) {
            try {

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }





    @Override
    public void bindItemViewHolder(final int position) {
        this.position = position;
        ImageView vmax_custom_otherimg;
        RelativeLayout vmax_custom_media_view;
        vmax_custom_media_view = (RelativeLayout) vmaxAdViewBanner.findViewById(R.id.vmax_custom_media_view);
        DisplayMetrics dm = new DisplayMetrics();
        ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = (int) ((ApplicationController.getApplicationConfig().screenWidth / dm.density) / 1.77);
        /*if (mListCarouselInfo != null && mListCarouselInfo.get(position) != null) {
            height = mListCarouselInfo.get(position).pageSize;
            SDKLogger.debug("received height in dp- " + height);
        }*/
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, mContext.getResources().getDisplayMetrics());
        SDKLogger.debug("calculated height- " + height);
        if (vmax_custom_media_view != null
                && vmax_custom_media_view.getLayoutParams() != null) {
            vmax_custom_media_view.getLayoutParams().height = height;
        }
    }

    private void removeItemFromParent(CarouselInfoData carouselInfoData) {
        if (carouselInfoData == null) {
            LoggerD.debugLog("removeItem: invalid operation of removal " + carouselInfoData == null ? " no carousel title" : carouselInfoData.title);
            return;
        }
        try {
            notifyItemRemoved(carouselInfoData);
        } catch (IllegalStateException e) {
            mRecyclerViewCarouselInfo.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged();
                }
            });
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
