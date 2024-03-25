package com.myplex.myplex.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myplex.api.APIConstants;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.UrlGatewayActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.HomePagerAdapterDynamicMenu;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.FragmentWebView;
import com.myplex.myplex.utils.ChromeTabUtils;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;

import java.util.List;

/**
 * Created by ramaraju on 07/04/2019.
 */
public class PromoBannerItem extends GenericListViewCompoment {


    private Context mContext;
    private List<CarouselInfoData> mListCarouselInfo;
    private static final String TAG = BigHorizontalItem.class.getSimpleName();
    private CarouselInfoData parentCarouselData;
    private View view;
    private CarouselInfoData carouselInfoData;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private String mMenuGroup;
    private String mPageTitle;
    GenericListViewCompoment holder = this;

    private Typeface mBoldTypeFace;
    public PromoBannerItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo, String menuGroup, String pageTitle) {
        super(view);
        this.view = view;
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        this.mMenuGroup = menuGroup;
        this.mPageTitle = pageTitle;
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");
    }

    public static PromoBannerItem createView(Context context, ViewGroup parent,
                                                 List<CarouselInfoData> carouselInfoData, String menuGroup, String pageTitle) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_promo_banner_image, parent, false);
        return new PromoBannerItem(context, view, carouselInfoData, menuGroup, pageTitle);
    }
    @Override
    public void bindItemViewHolder(final int position) {

        this.position = position;
        CarouselInfoData carouselInfoData = null;
        if (mListCarouselInfo != null
                && position < mListCarouselInfo.size()) {
            carouselInfoData = mListCarouselInfo.get(position);
        }

        if (carouselInfoData!=null&&carouselInfoData.showTitle){
            if (carouselInfoData.title!=null){
                mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
                mTextViewGenreMovieTitle.setText(carouselInfoData.title);
            }else {
                mTextViewGenreMovieTitle.setVisibility(View.GONE);
            }

        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPromoBannerItemCicked(v, position);
            }
        });
        if (mImageView!=null){
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPromoBannerItemCicked(v,position);
                }
            });
        }
        /*if(DeviceUtils.isTablet(mContext)) {
            int widthRatio = 9;
            int heightRatio = 2;
            int Height = (((ApplicationController.getApplicationConfig().screenHeight * heightRatio) / widthRatio));
            holder.mImageView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenHeight;
            holder.mImageView.getLayoutParams().height = Height;
        }*/
        /*if (DeviceUtils.isTablet(mContext)) {
            DisplayMetrics dm = new DisplayMetrics();
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
                ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                int widthRatio = 9;
                int heightRatio = 2;
                if (carouselInfoData != null
                        && carouselInfoData.showAll != null) {
                    String[] ratio = carouselInfoData.showAll.split(":");
                    try {
                        if (ratio.length > 1) {
                            widthRatio = Integer.parseInt(ratio[0]);
                            heightRatio = Integer.parseInt(ratio[1]);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int Height = (((ApplicationController.getApplicationConfig().screenHeight * heightRatio) / widthRatio));
                holder.mImageView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenHeight;
                holder.mImageView.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + holder.mImageView.getLayoutParams().width);
            } else {
                ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
                ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
                int widthRatio = 9;
                int heightRatio = 2;
                if (carouselInfoData != null
                        && carouselInfoData.showAll != null) {
                    String[] ratio = carouselInfoData.showAll.split(":");
                    try {
                        if (ratio.length > 1) {
                            widthRatio = Integer.parseInt(ratio[0]);
                            heightRatio = Integer.parseInt(ratio[1]);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int Height = (((ApplicationController.getApplicationConfig().screenWidth * heightRatio) / widthRatio));
                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                holder.mImageView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenWidth;
                holder.mImageView.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
            }
        } else {
            DisplayMetrics dm = new DisplayMetrics();
            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
            LoggerD.debugLog("screenHeight- " + ApplicationController.getApplicationConfig().screenHeight + " " +
                    " screenWidth- " + ApplicationController.getApplicationConfig().screenWidth);
            int widthRatio = 9;
            int heightRatio = 3;
            if (carouselInfoData != null
                    && carouselInfoData.showAll != null) {
                String[] ratio = carouselInfoData.showAll.split(":");
                try {
                    if (ratio.length > 1) {
                        widthRatio = Integer.parseInt(ratio[0]);
                        heightRatio = Integer.parseInt(ratio[1]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int Height = (((ApplicationController.getApplicationConfig().screenWidth * heightRatio) / widthRatio));
            Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
            //holder.mImageView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenWidth;
            holder.mImageView.getLayoutParams().height = Height;
            Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
        }*/
        carouselInfoData = mListCarouselInfo.get(position);
        String imageLink = carouselInfoData.getPromoUrl(DeviceUtils.isTablet(mContext), UiUtil.getScreenDensity(mContext));
        if (TextUtils.isEmpty(imageLink)
                || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            holder.mImageView.setImageResource(R.drawable.black);
        } else {
            if (mContext.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE) {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageView, R.drawable.black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageView, R.drawable.black);
            }
        }
    }


    private void onPromoBannerItemCicked(View v, int adapterPosition) {
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) return;
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(adapterPosition);
        if (carouselInfoData != null && HomePagerAdapterDynamicMenu.ACTION_LAUNCH_WEBPAGE.equalsIgnoreCase(carouselInfoData.appAction)) {
//            int launchType = SubscriptionWebActivity.PARAM_LAUNCH_NONE;
//            ((MainActivity)mContext).startActivityForResult(SubscriptionWebActivity.createIntent(mContext, carouselInfoData.actionUrl, launchType), SUBSCRIPTION_REQUEST);
            String url = carouselInfoData.actionUrl;
            if (!Util.checkUserLoginStatus()) {
                if (mContext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mContext;
                    mainActivity.initLogin(Analytics.VALUE_SOURCE_CAROUSEL, carouselInfoData.title);
                }
                return;
            }
            if (TextUtils.isEmpty(url) ) return;
            if (url.contains("mode=external")) {
                SDKLogger.debug("url- " + url);
                Intent browserX = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserX);
                return;
            }
            if (url.contains("?")) {
                if (url.endsWith("?"))
                    url = url + "clientKey=" + PrefUtils.getInstance().getPrefClientkey();
                else
                    url = url + "&clientKey=" + PrefUtils.getInstance().getPrefClientkey();
            } else {
                url = url + "?clientKey=" + PrefUtils.getInstance().getPrefClientkey();
            }
            SDKLogger.debug("url- " + url);
          /*  Bundle bundle = new Bundle();
            bundle.putString(FragmentWebView.PARAM_URL, url);
            bundle.putBoolean(FragmentWebView.PARAM_SHOW_TOOLBAR, true);
            bundle.putString(FragmentWebView.PARAM_TOOLBAR_TITLE, carouselInfoData.title);
            BaseFragment fragment = (BaseFragment) Fragment.instantiate(mContext, FragmentWebView.class.getName(), bundle);
            ((MainActivity) mContext).pushFragment(fragment);*/
            ((MainActivity) mContext).startActivityForResult(SubscriptionWebActivity.createIntent( ((MainActivity) mContext), url, SubscriptionWebActivity.PARAM_LAUNCH_BANNER), 1);

        }else if (carouselInfoData != null && APIConstants.LAUNCH_WEB_CHROME.equalsIgnoreCase(carouselInfoData.appAction)) {

            FirebaseAnalytics.getInstance().promobanner(Analytics.VALUE_SOURCE_DETAILS_PROMO_BANNER);
            FirebaseAnalytics.getInstance().createScreenFA((Activity) mContext, Analytics.VALUE_SOURCE_DETAILS_PROMO_BANNER);

            String url = carouselInfoData.actionUrl;
            if (!Util.checkUserLoginStatus()) {
                if (mContext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mContext;
                    mainActivity.initLogin(Analytics.VALUE_SOURCE_CAROUSEL, carouselInfoData.title);
                }
                return;
            }
            if (TextUtils.isEmpty(url) ) return;

            ChromeTabUtils.openUrl(mContext,url);
        } else if (carouselInfoData != null && APIConstants.LAUNCH_SUBSCRIBE.equalsIgnoreCase(carouselInfoData.appAction)) {
            Bundle args = new Bundle();
            if (!TextUtils.isEmpty(carouselInfoData.appAction))
                args.putString(APIConstants.ACTION_TYPE, carouselInfoData.appAction);
            if (!TextUtils.isEmpty(carouselInfoData.actionUrl))
                args.putString(APIConstants.ACTION_URL, carouselInfoData.actionUrl);
            ((BaseActivity) mContext).showDetailsFragment(args, null);
        }else if(carouselInfoData != null && APIConstants.ACTION_TYPE_DEEPLINK.equalsIgnoreCase(carouselInfoData.appAction)){
            constructDeepLinkUrl(carouselInfoData.actionUrl);
        }
    }

    private void constructDeepLinkUrl(String actionUrl) {
        Intent intent = new Intent(mContext, UrlGatewayActivity.class);
        intent.setData(Uri.parse(actionUrl));
        Log.d("LOG_TAG", "Intent: " + actionUrl);
        mContext.startActivity(intent);
        return;
    }
}
