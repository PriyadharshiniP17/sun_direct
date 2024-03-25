package com.myplex.myplex.ui.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.myplex.api.APIConstants;
import com.myplex.model.AdLayoutsData;
import com.myplex.model.CarouselInfoData;

import com.myplex.myplex.ApplicationController;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.Util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AdNativeVideoLayout extends GenericListViewCompoment {

    private Context mContext;
    private static final String TAG = AdNativeVideoLayout.class.getSimpleName();
    private GenericListViewCompoment holder = this;
    private CarouselInfoData carouselInfoData;
    List<CarouselInfoData> mListCarouselInfoData;
    TemplateView template;
    private String mTabName;
    private BroadcastReceiver localBroadCastReceiver;

    public AdNativeVideoLayout(Context mContext, View view, CarouselInfoData carouselInfoData,
                               List<CarouselInfoData> mListCarouselInfoData,String mTabName) {
        super(view);
        this.mContext = mContext;
        this.mListCarouselInfoData=mListCarouselInfoData;
        this.carouselInfoData = carouselInfoData;
        this.mTabName=mTabName;
        template =view.findViewById(R.id.medium_ad_template_view);
    }

    public static AdNativeVideoLayout createView(Context context, ViewGroup parent,
                                                 CarouselInfoData carouselInfoData,
                                                 List<CarouselInfoData> mListCarouselInfoData,String mTabName) {
        View view = LayoutInflater.from(context).inflate(R.layout.native_video_ad_layout, parent, false);
        return new AdNativeVideoLayout(context, view, carouselInfoData,mListCarouselInfoData,mTabName);
    }

    private void registerBroadcastReceiver(int position) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(APIConstants.PAGE_CHANGE_BROADCAST);

        localBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if(intent.getAction().equalsIgnoreCase(APIConstants.PAGE_CHANGE_BROADCAST)||
                            intent.getAction().equalsIgnoreCase(APIConstants.MINI_PLAYER_DISABLED_BROADCAST)) {
                        if (intent.hasExtra(APIConstants.TAB_NAME)) {
                            String currentPage = intent.getStringExtra(APIConstants.TAB_NAME);
                            if (currentPage!=null&&currentPage.equalsIgnoreCase(mTabName)){
                                if (holder.nativeAdTemplateView!=null){
                                    holder.nativeAdTemplateView.destroyNativeAd();
                                }
                                if (holder.adView!=null){
                                    holder.adView.removeAllViews();
                                }
                                loadAd(position);
                            }
                        }
                    }
                }
            }
        };
        ApplicationController.getLocalBroadcastManager().registerReceiver(localBroadCastReceiver,filter);

    }


    @Override
    public void bindItemViewHolder(int position) {
        registerBroadcastReceiver(position);
        loadAd(position);
    }

    public void loadAd(int position){
        if(!Util.isPremiumUser()&&mListCarouselInfoData!=null&&!mListCarouselInfoData.isEmpty()
                && mListCarouselInfoData.get(position) != null && !TextUtils.isEmpty(mListCarouselInfoData.get(position).actionUrl) ) {

            String adUnitId;
            String adPosition="NA";
            if(mListCarouselInfoData.get(position).actionUrl.contains(";")){
                String[] adIdAndPositionArray=mListCarouselInfoData.get(position).actionUrl.split(";");
                adUnitId=adIdAndPositionArray[0];
                adPosition=adIdAndPositionArray[1];
            }else {
                adUnitId=mListCarouselInfoData.get(position).actionUrl;
            }

            if (adUnitId != null) {
                AdLoader adLoader = new AdLoader.Builder(mContext, adUnitId)
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                            @Override
                            public void onNativeAdLoaded(@NotNull NativeAd NativeAd) {
                                template.setVisibility(View.VISIBLE);
                                template.setNativeAd(NativeAd);
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                                template.setVisibility(View.GONE);
                                Util.loadAdError(loadAdError, TAG);
                            }
                        })
                        .build();

                adLoader.loadAd(new AdManagerAdRequest.Builder()
                        .addCustomTargeting("ad_position",adPosition)
                        .addCustomTargeting("content_type","NA")
                        .addCustomTargeting("content_id","NA")
                        .addCustomTargeting("content_language","NA")
                        .addCustomTargeting("user_type",Util.getUserType())
                        .addCustomTargeting("gender", Util.getGenderString())
                        .addCustomTargeting("age",Util.getAgeString())
                        .addCustomTargeting("content_name","NA")
                        .addCustomTargeting("content_page",mTabName)
                        .addCustomTargeting("video_watch_count",PrefUtils.getInstance().getAdVideoCount()+"")
                        /*.addCustomTargeting("tags","content tags")
                        .addCustomTargeting("duration","NA")
                        .addCustomTargeting("consent_targeting","Yes")
                        .addCustomTargeting("source","")
                        .addCustomTargeting("video_watch_count","1")*/
                        .build());

                PrefUtils.getInstance().setAdVideoCount(PrefUtils.getInstance().getAdVideoCount()+1);

                String AdData="Ad tags native:::"+"ad_position : "+ adPosition+ "  user_type : "+ Util.getUserType()+"  gender : "+Util.getGenderString()+
                        "  age : "+ Util.getAgeString()+"  content_page : "+ mTabName+
                        "  video_watch_count : "+ PrefUtils.getInstance().getAdVideoCount();

                if (ApplicationController.SHOW_PLAYER_LOGS) {
                    AlertDialogUtil.showAdAlertDialog(mContext, AdData, "AD Logs", "Okay");
                }
            }
        }else {
            template.setVisibility(View.GONE);
        }
    }

    private AdLayoutsData getAdData(ArrayList<AdLayoutsData> adLayoutsData,String layoutType) {
        for (int p=0;p<adLayoutsData.size();p++){
            if (layoutType.equalsIgnoreCase(adLayoutsData.get(p).layoutType)){
                return adLayoutsData.get(p);
            }
        }
        return null;
    }
}
