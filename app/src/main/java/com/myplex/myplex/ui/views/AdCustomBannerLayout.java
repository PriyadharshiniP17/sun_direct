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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.formats.AdManagerAdViewOptions;
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.myplex.api.APIConstants;
import com.myplex.model.AdLayoutsData;
import com.myplex.model.AdSizes;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.ApplicationController;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.Util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AdCustomBannerLayout extends GenericListViewCompoment {

    private Context mContext;
    private static final String TAG = AdCustomBannerLayout.class.getSimpleName();
    private View vmaxAdViewBanner;
    private GenericListViewCompoment holder = this;
    private CarouselInfoData carouselInfoData;
    private List<CarouselInfoData> mListCarouselInfoData;
    private String mTabName;
    private BroadcastReceiver localBroadCastReceiver;

    public AdCustomBannerLayout(Context mContext, View view,
                                CarouselInfoData carouselInfoData,
                                List<CarouselInfoData> mListCarouselInfoData, String mTabName) {
        super(view);
        this.vmaxAdViewBanner = view;
        this.mContext = mContext;
        this.mTabName = mTabName;
        this.carouselInfoData = carouselInfoData;
        this.mListCarouselInfoData = mListCarouselInfoData;
    }

    public static AdCustomBannerLayout createView(Context context, ViewGroup parent,
                                                  List<CarouselInfoData> mListCarouselInfo,
                                                  CarouselInfoData carouselInfoData, String mTabName) {
        View view = LayoutInflater.from(context).inflate(R.layout.native_custom_banner_ad_layout,
                parent, false);
        return new AdCustomBannerLayout(context, view, carouselInfoData, mListCarouselInfo, mTabName);
    }

    private void registerBroadcastReceiver(int position) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(APIConstants.PAGE_CHANGE_BROADCAST);
        filter.addAction(APIConstants.MINI_PLAYER_DISABLED_BROADCAST);

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

    private void loadAd(int position) {
        if (!Util.isPremiumUser() && mListCarouselInfoData != null && !mListCarouselInfoData.isEmpty()
                && mListCarouselInfoData.get(position).actionUrl != null) {
            List<AdSizes> adSizes = getAdSizes(mListCarouselInfoData.get(position).showAll);
            String adUnitId;
            String adPosition = "NA";
            if (mListCarouselInfoData.get(position).actionUrl.contains(";")) {
                String[] adIdAndPositionArray = mListCarouselInfoData.get(position).actionUrl.split(";");
                adUnitId = adIdAndPositionArray[0];
                adPosition = adIdAndPositionArray[1];
            } else {
                adUnitId = mListCarouselInfoData.get(position).actionUrl;
            }
            if (adUnitId != null) {
                AdLoader adLoader = new AdLoader.Builder(mContext, adUnitId)
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                            @Override
                            public void onNativeAdLoaded(@NotNull NativeAd nativeAd) {
                                // Show the ad.
                                holder.nativeAdTemplateView.setVisibility(VISIBLE);
                                holder.adView.setVisibility(GONE);
                                holder.nativeAdTemplateView.setNativeAd(nativeAd);
                            }
                        })
                        .forAdManagerAdView(new OnAdManagerAdViewLoadedListener() {
                                                @Override
                                                public void onAdManagerAdViewLoaded(@NotNull AdManagerAdView adView) {
                                                    // Show the banner ad.
                                                    holder.adView.setVisibility(VISIBLE);
                                                    holder.nativeAdTemplateView.setVisibility(GONE);
                                                    holder.adView.addView(adView);
                                                }
                                            }, getGoogleAdSize(adSizes, 0)
                                , getGoogleAdSize(adSizes, 1)
                                , getGoogleAdSize(adSizes, 2),
                                getGoogleAdSize(adSizes, 3),
                                getGoogleAdSize(adSizes, 4))
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(@NotNull LoadAdError error) {
                                Util.loadAdError(error,TAG);
                                holder.adView.setVisibility(GONE);
                                holder.nativeAdTemplateView.setVisibility(GONE);
                                // Handle the failure by logging, altering the UI, and so on.
                            }
                        })
                        .withAdManagerAdViewOptions(new AdManagerAdViewOptions.Builder()
                                // Methods in the AdManagerAdViewOptions.Builder class can be
                                // used here to specify individual options settings.
                                .build())
                        .build();
                adLoader.loadAd(new AdManagerAdRequest.Builder()
                        .addCustomTargeting("ad_position", adPosition)
                        .addCustomTargeting("content_type", "NA")
                        .addCustomTargeting("content_id", "NA")
                        .addCustomTargeting("content_language", "NA")
                        .addCustomTargeting("user_type", Util.getUserType())
                        .addCustomTargeting("gender", Util.getGenderString())
                        .addCustomTargeting("age", Util.getAgeString())
                        .addCustomTargeting("content_name", "NA")
                        .addCustomTargeting("content_page", mTabName)
                        .addCustomTargeting("video_watch_count", PrefUtils.getInstance().getAdVideoCount()+"")
                        /*.addCustomTargeting("tags", "content tags")
                        .addCustomTargeting("duration", "NA")
                        .addCustomTargeting("consent_targeting", "Yes")
                        .addCustomTargeting("source", "")
                        */
                        .build());

                PrefUtils.getInstance().setAdVideoCount(PrefUtils.getInstance().getAdVideoCount()+1);

                String AdData="Ad tags Banner:::"+"ad_position : "+ adPosition+ "  user_type : "+ Util.getUserType()+"  gender : "+Util.getGenderString()+
                        "  age : "+ Util.getAgeString()+"  content_page : "+ mTabName+
                        "  video_watch_count : "+ PrefUtils.getInstance().getAdVideoCount();

                if (ApplicationController.SHOW_PLAYER_LOGS) {
                    AlertDialogUtil.showAdAlertDialog(mContext, AdData, "AD Logs", "Okay");
                }
            }
        } else {
            holder.adView.setVisibility(GONE);
            holder.nativeAdTemplateView.setVisibility(GONE);
        }
    }

    private AdSize getGoogleAdSize(List<AdSizes> adSizesList, int position) {
        if (adSizesList!=null&&!adSizesList.isEmpty()&&adSizesList.size() > position) {
            return new AdSize(Integer.parseInt(adSizesList.get(position).adWidth), Integer.parseInt(adSizesList.get(position).adHeight));
        }
        return AdSize.BANNER;
    }

    private List<AdSizes> getAdSizes(String adSizes) {
        List<AdSizes> adSizesList = new ArrayList<>();
        if (adSizes!=null&&!TextUtils.isEmpty(adSizes)&&adSizes.contains(";")){
            String[] adSizesArray = adSizes.split(";");
            for (String s : adSizesArray) {
                String[] eachAdSize = s.split(",");
                AdSizes adSizeObject = new AdSizes();
                adSizeObject.adWidth = eachAdSize[0];
                adSizeObject.adHeight = eachAdSize[1];
                adSizesList.add(adSizeObject);
            }
        }
        return adSizesList;
    }

    private AdLayoutsData getAdData(ArrayList<AdLayoutsData> adLayoutsData, String layoutType) {
        for (int p = 0; p < adLayoutsData.size(); p++) {
            if (layoutType.equalsIgnoreCase(adLayoutsData.get(p).layoutType)) {
                return adLayoutsData.get(p);
            }
        }
        return null;
    }
}
