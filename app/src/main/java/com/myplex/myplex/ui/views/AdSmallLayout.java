package com.myplex.myplex.ui.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myplex.model.AdLayoutsData;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.Util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.myplex.api.myplexAPISDK.getApplicationContext;
import static com.myplex.myplex.utils.Util.getJsonFromAssets;

public class AdSmallLayout extends GenericListViewCompoment {


    private Context mContext;
    private static final String TAG = AdSmallLayout.class.getSimpleName();
    private View view;
    private GenericListViewCompoment holder = this;
    private RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData carouselInfoData;
    TemplateView template;
    private List<CarouselInfoData> mListCarouselInfoData;

    public AdSmallLayout(Context mContext, View view,
                          RecyclerView mRecyclerViewCarouselInfo, CarouselInfoData carouselInfoData
            , List<CarouselInfoData> mListCarouselInfoData) {
        super(view);
        this.view = view;
        this.mContext = mContext;
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        this.carouselInfoData = carouselInfoData;
        this.mListCarouselInfoData=mListCarouselInfoData;
        template =view.findViewById(R.id.medium_ad_template_view);
    }

    public static AdSmallLayout createView(Context context, ViewGroup parent,
                                            CarouselInfoData carouselInfoData,
                                            RecyclerView mRecyclerViewCarouselInfo
            ,List<CarouselInfoData> mListCarouselInfoData) {
        View view = LayoutInflater.from(context).inflate(R.layout.native_small_ad_layout,
                parent, false);
        return new AdSmallLayout(context, view, mRecyclerViewCarouselInfo, carouselInfoData,mListCarouselInfoData);
    }

    @Override
    public void bindItemViewHolder(int position) {
        if (!Util.isPremiumUser()) {
            String adsLayoutsJson = getJsonFromAssets(getApplicationContext(), "adLayouts.json");
            Log.i("data", adsLayoutsJson);
            Gson gson = new Gson();
            Type listUserType = new TypeToken<ArrayList<AdLayoutsData>>() {
            }.getType();
            ArrayList<AdLayoutsData> adLayoutsData = gson.fromJson(adsLayoutsJson, listUserType);
            AdLayoutsData adData = getAdData(adLayoutsData, mListCarouselInfoData.get(position).layoutType);

            if (adData != null) {
                AdLoader adLoader = new AdLoader.Builder(mContext, adData.adUnitId)
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                            @Override
                            public void onNativeAdLoaded(@NotNull NativeAd NativeAd) {
                        /*NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().withMainBackgroundColor
                                ().build();
                        template.setStyles(styles);*/
                                template.setNativeAd(NativeAd);
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                                Util.loadAdError(loadAdError, TAG);
                            }
                        })
                        .build();

                adLoader.loadAd(new AdManagerAdRequest.Builder().build());
            }
        }else {
            template.setVisibility(View.VISIBLE);
        }

        /*AdLoader adLoader = new AdLoader.Builder(mContext, "/21830968352/Test_Ad_Card_Bw_Rails")
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd unifiedNativeAd) {
                        if (unifiedNativeAd != null) {
                            if (nativeAd != null) {
                                nativeAd.destroy();
                            }
                            TemplateView template = itemView.findViewById(R.id.medium_ad_template_view);
                            template.setNativeAd(unifiedNativeAd);
                            //mAdLayout.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Util.loadAdError(adError,TAG);
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        adLoader.loadAd(new AdManagerAdRequest.Builder().build());*/
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
