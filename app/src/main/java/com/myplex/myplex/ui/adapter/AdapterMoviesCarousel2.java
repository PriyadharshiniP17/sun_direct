package com.myplex.myplex.ui.adapter;

import static com.myplex.myplex.ui.activities.MainActivity.INTENT_REQUEST_TYPE_LOGIN;
import static com.myplex.myplex.ui.activities.MainActivity.INTENT_RESPONSE_TYPE_SUCCESS;
import static com.myplex.myplex.ui.adapter.AdapterCarouselInfo.ITEM_TYPE_EMPTY_FOOTER;
import static com.myplex.myplex.ui.adapter.AdapterCarouselInfo.ITEM_TYPE_PROMO_BANNER;
import static com.myplex.myplex.ui.adapter.AdapterCarouselInfo.ITEM_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataOttImagesItem;
import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.OTTApp;
import com.myplex.model.RequestState;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentWebView;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;
import com.myplex.myplex.ui.views.SubscriptionPacksDialog;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import autoscroll.AutoScrollViewPager;
import viewpagerindicator.CircleIndicator;


public class AdapterMoviesCarousel2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = AdapterMoviesCarousel2.class.getSimpleName();
    private static final int TYPE_HEADER_PAGER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER_GRID = 2;
    private static final int RECYCLERVIEW_OTHER_CHILD_ITEM_COUNT = 1;
    private static final int TYPE_FROM_MOVIE = -1;
    private static final byte ITEM_TYPE_VMAX_IMAGE_ADVERTISE = 35;
    private static final byte ITEM_TYPE_VMAX_VIDEO_ADVERTISE = 36;
    private Context mContext;
    private List<CarouselInfoData> mListCarouselInfo;
    private RecyclerView mRecyclerViewMoviesCarousel;
    private boolean mRelatedItemsLoading;
    private final List<OTTAppsImageSliderAdapter.SliderModel> mSliderItems = new ArrayList<>();
    private List<OTTApp> mAppsList;

    private RecyclerView.ItemDecoration mHorizontalDividerDecoration;
    private final RecyclerView.ItemDecoration mHorizontalMoviesDivieder;
    private final View.OnClickListener mViewAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof CarouselInfoData) {
                CleverTap.eventPageViewed(CleverTap.PAGE_VIEW_ALL);
                final CarouselInfoData carouselInfoData = (CarouselInfoData) v.getTag();
                if (carouselInfoData == null || carouselInfoData.name == null || carouselInfoData.listCarouselData == null) {
                    return;
                }
                showCarouselViewAllFragment(carouselInfoData);
            }
        }
    };

    private View.OnClickListener mRetryListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                int position = (int) view.getTag();
                LoggerD.debugLogAdapter("parentview position- " + position + " getItemCount- " + getItemCount());
                if (position > getItemCount() - 1 || mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
                    return;
                }
                CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
                startAsyncTaskInParallel(new CarouselRequestTask(AdapterMoviesCarousel2.this, carouselInfoData.name, carouselInfoData.pageSize, position + 1,carouselInfoData.modified_on));
            } catch (ArrayIndexOutOfBoundsException aie) {
                aie.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private OnItemRemovedListener mOnItemRemovedListener = new OnItemRemovedListener() {
        @Override
        public void onItemRemoved(int mParentPosition) {
            if (mListCarouselInfo == null) {
                LoggerD.debugLog("removeItem: invalid operation of removal carousel info is empty");
                return;
            }
            CarouselInfoData carouselInfoData = mListCarouselInfo.get(mParentPosition);
            if (carouselInfoData == null) {
                LoggerD.debugLog("removeItem: invalid operation of removal " + carouselInfoData == null ? " no carousel title" : carouselInfoData.title);
                return;
            }
            safelyNotifyItemRemoved(carouselInfoData);
        }
    };
    private Handler mHandler;
    private OTTAppsImageSliderAdapter.SliderModel mSliderModel;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private AdapterCarouselInfo.CallbackListener callbackListener;
    private CarouselInfoData mCarouselInfoData;
    private RelativeLayout layout;

    public void setCarouselInfoDataSection(CarouselInfoData mCarouselInfoData) {
        this.mCarouselInfoData = mCarouselInfoData;
    }

    public void setCallBackListener(AdapterCarouselInfo.CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }


    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData movieData) {
            //movie item clicked we can have movie data here

            if (movieData == null) return;
//                launchPlayStore("market://details?id=com.hungama.movies");
            if (movieData._id == null) {
                return;
            }

            CarouselInfoData carouselInfoData = parentPosition >= 0 ? mListCarouselInfo.get(parentPosition) : null;

            if (carouselInfoData != null && carouselInfoData.title != null) {
                gaBrowse(movieData, carouselInfoData.title);
            }


            String publishingHouse = movieData == null
                    || movieData.publishingHouse == null
                    || TextUtils.isEmpty(movieData.publishingHouse.publishingHouseName) ? null : movieData.publishingHouse.publishingHouseName;

            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(movieData, mContext, carouselInfoData, null);
                return;
            }
            if (PrefUtils.getInstance().getPrefIsHooq_sdk_enabled()) {
                try {

                    LoggerD.debugHooqVstbLog("ItemClick: publishingHouseName: " +
                            movieData != null &&
                            movieData.publishingHouse != null &&
                            movieData.publishingHouse.publishingHouseName != null ? movieData.publishingHouse.publishingHouseName : "NA");
                    if (movieData != null && movieData.publishingHouse != null && movieData.publishingHouse.publishingHouseName != null) {
                            /*&& mAppsList != null && !mAppsList.isEmpty()) {
                        for (OTTApp ottApp : mAppsList) {
                            //check app name is starts with publishing house name
                            if (ottApp.appName.toLowerCase().contains(movieData.publishingHouse.publishingHouseName.toLowerCase())
                                    || ottApp.appName.equalsIgnoreCase(movieData.publishingHouse.publishingHouseName.toLowerCase())
                                    || ottApp.appName.toLowerCase().startsWith(movieData.publishingHouse.publishingHouseName.toLowerCase())) {
                                if(APIConstants.TYPE_HOOQ.equalsIgnoreCase(movieData.publishingHouse.publishingHouseName)){
                                        showDetailsFragment(movieData);
                                        return;
                                }
                            }
                        }*/
                        if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(movieData.publishingHouse.publishingHouseName)) {
                            if (carouselInfoData == null || TextUtils.isEmpty(carouselInfoData.bgSectionColor))
                                showDetailsFragment(movieData, position);
                            else
                                showDetailsFragment(movieData, carouselInfoData.bgSectionColor);
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            LoggerD.debugLog("pacakge name- " + mContext.getPackageName());
            if (checkSubscriptionAndLaunchDeepLink(movieData, position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA ? EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA : TYPE_FROM_MOVIE)) {
                return;
            }
            if (carouselInfoData == null)
                showDetailsFragment(movieData, position);
            else
                showDetailsFragment(movieData, carouselInfoData.bgSectionColor);
        }

    };

    private void showDetailsFragment(CardData movieData, String bgSectionColor) {
        CacheManager.setSelectedCardData(movieData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, movieData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(movieData));
        String partnerId = movieData == null || movieData.generalInfo == null || movieData.generalInfo.partnerId == null ? null : movieData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        ((BaseActivity) mContext).showDetailsFragment(args, movieData);
    }

    private void showDetailsFragment(CardData movieData, int position) {
        CacheManager.setSelectedCardData(movieData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, movieData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(movieData));
        String partnerId = movieData == null || movieData.generalInfo == null || movieData.generalInfo.partnerId == null ? null : movieData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, "banners");
        if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            if (searchMovieData != null) {
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, searchMovieData.getSearchString());
            }
        }
        ((BaseActivity) mContext).showDetailsFragment(args, movieData);
    }


    private void gaBrowse(CardData movieData, String carouselSectionName) {

        if (movieData.generalInfo == null || movieData.generalInfo.title == null || carouselSectionName == null) {
            return;
        }

        Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_MOVIES, carouselSectionName, movieData.generalInfo.title);
    }

    private ProgressDialog mProgressDialog;

    private boolean checkSubscriptionAndLaunchDeepLink(CardData movieData, int type) {
        if (SDKUtils.getCardExplorerData().cardDataToSubscribe != null && movieData != null &&
                SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null && movieData._id != null
                && SDKUtils.getCardExplorerData().cardDataToSubscribe._id.equalsIgnoreCase(movieData._id)) {
            movieData = SDKUtils.getCardExplorerData().cardDataToSubscribe;
        }

        if (movieData == null) {
            return false;
        }

        if (movieData.currentUserData != null && movieData.currentUserData.purchase != null && movieData.currentUserData.purchase.size() != 0) {
            //paid content purchased launch deeplink if it is ottapp data
            return launchDeepLinkOrInstallPartnerApp(movieData);
        }

//      if (movieData._id != null && ApplicationController.FLAG_ENABLE_TRYNBUY_SUBSCRIPTION) {
        if (movieData._id != null && (movieData.packages != null && !movieData.packages.isEmpty() || type == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) && ApplicationController.FLAG_ENABLE_TRYNBUY_SUBSCRIPTION) {
            //check purchases paid content
            //if purchase request not happened use cache
            showProgressBar();
            fetchSubscriptionPackages(movieData);
            return true;
        }

        //not paid content launch deeplink if it is ottapp data
        return launchDeepLinkOrInstallPartnerApp(movieData);
    }

    private boolean launchDeepLinkOrInstallPartnerApp(CardData movieData) {
        try {
            if (movieData != null && movieData.publishingHouse != null && movieData.publishingHouse.publishingHouseName != null
                    && mAppsList != null && !mAppsList.isEmpty()) {
                for (OTTApp ottApp : mAppsList) {
                    //check app name is starts with publishing house name
                    if (ottApp.appName.toLowerCase().contains(movieData.publishingHouse.publishingHouseName.toLowerCase())
                            || ottApp.appName.equalsIgnoreCase(movieData.publishingHouse.publishingHouseName.toLowerCase())
                            || ottApp.appName.toLowerCase().startsWith(movieData.publishingHouse.publishingHouseName.toLowerCase())) {
                        if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(movieData.publishingHouse.publishingHouseName)) {
                            AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_MOVIE, APIConstants.TYPE_HOOQ, false);
                        } else if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(movieData.publishingHouse.publishingHouseName)) {
                            AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_MOVIE, APIConstants.TYPE_HUNGAMA, false);
                        }
                        if (!TextUtils.isEmpty(ottApp.androidPackageName)) {
                            handleOTTAppClicked(ottApp, movieData);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showCarouselViewAllFragment(CarouselInfoData carouselInfoData) {
        Bundle args = new Bundle();

        CacheManager.setCarouselInfoData(carouselInfoData);

        args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, APIConstants.TYPE_MOVIE);
        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }

    public AdapterMoviesCarousel2(Context context, List<CarouselInfoData> listCarouselInfo) {
        mContext = context;
        mListCarouselInfo = listCarouselInfo;
        mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        mHorizontalMoviesDivieder = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_5));
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        GenericViewHolder viewHolder = null;
        //Log.d(TAG, "onCreateViewHolder");
        if (viewType == TYPE_HEADER_PAGER) {
            //inflate your layout and pass it to view holder
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_movies_view_pager, parent, false);
            viewHolder = new MovieViewPagerViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem_carousel_linear_recycler_carousel_movies, parent, false);
            viewHolder = new MovieCarouselViewHolder(view);
        } else if (viewType == TYPE_FOOTER_GRID) {
            //inflate your layout and pass it to view holder
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_grid_view_apps, parent, false);
            viewHolder = new MovieAppsGridViewHolder(view);
        }  else if(viewType == ITEM_TYPE_PROMO_BANNER) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_promo_banner_image, parent, false);
            viewHolder = new PromoBannerViewHolder(view);
        }else if(viewType == ITEM_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM) {
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem_singlebanneritem, parent, false);
            viewHolder = new PromoBannerV2ViewHolder(view);
        }
        else if (viewType == ITEM_TYPE_EMPTY_FOOTER) {
            view = new LinearLayout(mContext);
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mContext.getResources().getDisplayMetrics());
            if (Util.checkUserLoginStatus())
                height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, mContext.getResources().getDisplayMetrics());
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            viewHolder = new EmptyFooterViewItem(view);
        }
        return viewHolder;
    }


    private void setUpVmaxView(RelativeLayout layout){
        String buttonColor = PrefUtils.getInstance().getVmaxAdButtonColor();
        String background = PrefUtils.getInstance().getVmaxLayoutBgColor();
        String textColor = PrefUtils.getInstance().getVmaxAdHeaderFontColor();
        String secondaryTextColor = PrefUtils.getInstance().getVmaxAdHeaderSecondaryFontColor();
        if (buttonColor == null) {
            buttonColor = "#399c1e";
        }
        if (background == null) {
            background = "#000000";
        }
        if (textColor == null) {
            textColor = "#FFFFFF";
        }
        if (secondaryTextColor == null) {
            secondaryTextColor = "#999999";
        }

     /*   Button installNow = (Button)layout.findViewById(R.id.vmax_custom_cta);
        GradientDrawable drawable = (GradientDrawable) installNow.getBackground();
        drawable.setStroke(2, Color.parseColor(buttonColor));
        drawable.setColor(Color.parseColor(background));

      //  ((TextView)layout.findViewById(R.id.vmax_custom_title)).setTextColor(Color.parseColor(secondaryTextColor));
//        ((TextView)layout.findViewById(R.id.vmax_custom_desc)).setTextColor(Color.parseColor(textColor));
        layout.setBackgroundColor(Color.parseColor(background));

        installNow.setBackgroundDrawable(drawable);
        installNow.setTextColor(Color.parseColor(buttonColor));*/
        layout.findViewById(R.id.title_info_container).setBackgroundColor(Color.parseColor(background));
        layout.setBackgroundColor(Color.parseColor(background));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder" + position);
        if (holder instanceof MovieViewPagerViewHolder) {
            bindViewPagerViewHolder((MovieViewPagerViewHolder) holder);
        } else if (holder instanceof MovieAppsGridViewHolder) {
            bindGridViewHolder((MovieAppsGridViewHolder) holder);
        } else if (holder instanceof MovieCarouselViewHolder) {
            bindGenreViewHolder((MovieCarouselViewHolder) holder, position);
        }/* else if (holder instanceof VmxItemViewHolder) { //TODO:: Remove commented if block to visible ads
            bindVmxItemViewHolder((VmxItemViewHolder) holder, position);
        } */else if(holder instanceof PromoBannerViewHolder){
            bindPromoBannerItem((PromoBannerViewHolder) holder,position);
        } else if(holder instanceof AdapterCarouselInfo.EmptyFooterViewItem){
            bindEmptyFooterItem((AdapterCarouselInfo.EmptyFooterViewItem) holder,position);
        }
    }

    private void bindEmptyFooterItem(AdapterCarouselInfo.EmptyFooterViewItem holder, int position) {

    }

    private void bindVmxItemViewHolder(final VmxItemViewHolder holder, final int position) { //TODO:: Remove commented if block to visible ads
        ImageView vmax_custom_otherimg;
        RelativeLayout vmax_custom_media_view;
        //vmax_custom_otherimg = (ImageView) holder.vmaxAdViewBanner.findViewById(R.id.vmax_custom_otherimg);
        vmax_custom_media_view = (RelativeLayout) holder.vmaxAdViewBanner.findViewById(R.id.vmax_custom_media_view);
       // SDKLogger.debug("layouts- vmax_custom_media_view " + vmax_custom_media_view + " vmax_custom_otherimg- " + vmax_custom_otherimg);
        DisplayMetrics dm = new DisplayMetrics();
        ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        SDKLogger.debug("ACI HEIGHT" + ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth + " dm.density- " + dm.density + " real width- " + (ApplicationController.getApplicationConfig().screenWidth / dm.density));
        int height  = (int) ((ApplicationController.getApplicationConfig().screenWidth / dm.density) / 2.);
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, mContext.getResources().getDisplayMetrics());
        SDKLogger.debug("desired ad height- " + height);

        if (vmax_custom_media_view != null
                && vmax_custom_media_view.getLayoutParams() != null) {
            vmax_custom_media_view.getLayoutParams().height = height;
        }

    }

    private void bindGridViewHolder(MovieAppsGridViewHolder holder) {
        if (mAppsList == null || mAppsList.size() == 0) {
            mAppsList = getDummyOttAppList();
        }
        AdapterAppsGrid adapterGridView = new AdapterAppsGrid(mContext, mAppsList);
        holder.mGridViewApps.setAdapter(adapterGridView);
        setListViewHeightBasedOnChildren(holder.mGridViewApps);
        holder.mGridViewApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                AlertDialogUtil.showToastNotification("position- " + position);
                handleOTTAppClicked(mAppsList.get(position));
            }
        });

    }


    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    private static void setListViewHeightBasedOnChildren(GridView gridView) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(gridView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, gridView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        try {
            Field field = gridView.getClass().getDeclaredField("mVerticalSpacing");
            field.setAccessible(true);
            int vSpacing = field.getInt(gridView);
            params.height = (totalHeight + (vSpacing * (listAdapter.getCount() - 1))) / 2;
            gridView.setLayoutParams(params);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private List<OTTApp> getDummyOttAppList() {

        List<OTTApp> dummyList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            dummyList.add(new OTTApp());
        }
        return dummyList;
    }

    private void bindViewPagerViewHolder(final MovieViewPagerViewHolder holder) {
        if (DeviceUtils.isTablet(mContext)) {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                DisplayMetrics dm = new DisplayMetrics();
                ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
                ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;

                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                int Height = (int) mContext.getResources().getDimension(R.dimen.margin_gap_275);
                holder.mViewPager.getLayoutParams().height = Height;
                holder.mPreviewLayout.getLayoutParams().height = Height;
                holder.leftGradient.getLayoutParams().height = Height;
                holder.rightGradient.getLayoutParams().height = Height;
                holder.gradientContainer.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + holder.mViewPager.getLayoutParams().width);
            } else {
                DisplayMetrics dm = new DisplayMetrics();
                ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
                ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;

                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                int Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
                holder.mViewPager.getLayoutParams().height = Height;
                holder.mPreviewLayout.getLayoutParams().height = Height;
                holder.leftGradient.getLayoutParams().height = Height;
                holder.rightGradient.getLayoutParams().height = Height;
                holder.gradientContainer.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
            }
        } else {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            DisplayMetrics dm = new DisplayMetrics();
//            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
//            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
//            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;

//                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
//                int Height = (int) mContext.getResources().getDimension(R.dimen.margin_gap_275);
//                holder.mViewPager.getLayoutParams().height = Height;
//                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + holder.mViewPager.getLayoutParams().width);
            } else {
//            DisplayMetrics dm = new DisplayMetrics();
//            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
//            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
//            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;

                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                int Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
                holder.mViewPager.getLayoutParams().height = Height;
                holder.leftGradient.getLayoutParams().height = Height;
                holder.rightGradient.getLayoutParams().height = Height;
                holder.gradientContainer.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
            }
        }

        if (!mSliderItems.isEmpty()) {
            OTTAppsImageSliderAdapter ottAppsImageSliderAdapter = new OTTAppsImageSliderAdapter(mContext, new OTTAppsImageSliderAdapter.OnItemClickListener() {
                @Override
                public void onItemClicked(final OTTAppsImageSliderAdapter.SliderModel sliderModel) {
//                    checkSubscriptionAndLaunchDeepLink(movieData, TYPE_FROM_MOVIE);
                    handleOnClickBanners(sliderModel);
                }
            });
            bannerImagesOrder(mSliderItems);
            ottAppsImageSliderAdapter.setItems(mSliderItems);
            ottAppsImageSliderAdapter.setInfiniteLoop(true);
            holder.mViewPager.setAdapter(ottAppsImageSliderAdapter);
            holder.mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    double timer;
                    if(PrefUtils.getInstance().getBannerAutoScrollFrequency() != null)
                        timer = Integer.parseInt(PrefUtils.getInstance().getBannerAutoScrollFrequency())*1000;
                    else
                        timer = 2000;
                    holder.mViewPager.setInterval(((int) timer));
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            holder.mOfferDecriptionLayout.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(mSliderItems.get(0).ottApp.offerDescription.trim())) {
                holder.mDescriptionTxt.setText(mSliderItems.get(0).ottApp.offerDescription);
                holder.mOfferDecriptionLayout.setVisibility(View.VISIBLE);
            }
            holder.mViewPager.setVisibility(View.VISIBLE);
            holder.mViewPagerContainer.setVisibility(View.VISIBLE);
            holder.mPreviewLayout.setVisibility(View.GONE);
//            holder.mIndicator.setViewPager(holder.mViewPager);
            holder.mIndicator.setPageCount(ottAppsImageSliderAdapter.getRealCount());
            holder.mIndicator.setViewPager(holder.mViewPager);
            double timer;
            if(PrefUtils.getInstance().getBannerAutoScrollFrequency() != null)
                timer = Integer.parseInt(PrefUtils.getInstance().getBannerAutoScrollFrequency())*1.5*1000;
            else
                timer = 3000;
            holder.mViewPager.setInterval((int)timer);
            if (callbackListener != null
                    && callbackListener.isPageVisible()) {
                if (holder.mViewPager != null) {
                    holder.mViewPager.startAutoScroll();
                }
            }

            if (DeviceUtils.isTablet(mContext)) {
                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    final int pagerPadding = (int) (getPagerPadding() + mContext.getResources().getDimension(R.dimen.margin_gap_16));
                    holder.mViewPager.setClipToPadding(false);
                    holder.mViewPager.setPadding(pagerPadding, 0, pagerPadding, 0);
                    holder.mViewPager.setPageMargin(10);
                    if (ottAppsImageSliderAdapter.getCount() > 1) {
                        holder.mViewPager.setCurrentItem(ottAppsImageSliderAdapter.getCount() / 2);
                        holder.mViewPager.setOffscreenPageLimit(3);
                    }
                    holder.mViewPager.invalidate();
                } else {
                    holder.mViewPager.setCurrentItem(ottAppsImageSliderAdapter.getCount() / 2);
                    holder.mViewPager.setPadding(0, 0, 0, 0);
                    holder.mViewPager.setPageMargin(10);
                    holder.mViewPager.invalidate();
                }
            } else {
                if (ottAppsImageSliderAdapter.getCount() > 1) {
                    holder.mViewPager.setCurrentItem(ottAppsImageSliderAdapter.getCount() / 2);
                    holder.mViewPager.setOffscreenPageLimit(3);
                }
            }
        } else {
            holder.mPreviewLayout.setVisibility(View.VISIBLE);
            holder.mViewPager.setVisibility(View.GONE);
            holder.mViewPagerContainer.setVisibility(View.GONE);
            holder.mOfferDecriptionLayout.setVisibility(View.GONE);
        }
        holder.mViewPager.setAutoScrollDurationFactor(10);
        holder.mLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mViewPager.setCurrentItem(holder.mViewPager.getCurrentItem()-1);
            }
        });
        holder.mRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mViewPager.setCurrentItem(holder.mViewPager.getCurrentItem()+1);
            }
        });
    }

    private void handleOnClickBanners(OTTAppsImageSliderAdapter.SliderModel sliderModel) {
        OTTApp ottApp;
        if (sliderModel == null) {
            return;
        }
        mSliderModel = sliderModel;
        ottApp = sliderModel.ottApp;
        final String partnerContentId = (sliderModel != null) ? sliderModel.partnerContentId : null;

        if (ottApp != null && (ottApp.appName.toLowerCase().startsWith(APIConstants.TYPE_HOOQ)
                || TextUtils.isEmpty(ottApp.androidPackageName)
              )) {
            if (PrefUtils.getInstance().getPrefIsHooq_sdk_enabled()
                    || TextUtils.isEmpty(ottApp.androidPackageName)
                  ) {
                CardData movieData = new CardData();
                movieData._id = sliderModel.contentId;
//                            showDetailsFragment(movieData,true,partnerContentId);
                fetchCardDataAndLaunchDetailsPage(sliderModel.contentId);
//                            fetchCardDataAndLaunchDetailsPage("31975");
                return;
            }

            if (!ApplicationController.FLAG_ENABLE_TRYNBUY_SUBSCRIPTION) {
                handleOTTAppClicked(ottApp);
                return;
            }

        }
        if (!TextUtils.isEmpty(partnerContentId)) {
            if (ottApp != null && ottApp.appName.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                String partnerSignUpStatus = PrefUtils.getInstance().getPrefPartenerSignUpStatus();
                CardData cardData = new CardData();
                cardData.generalInfo = new CardDataGeneralInfo();
                cardData.generalInfo.partnerId = partnerContentId;
                cardData._id = sliderModel.contentId;
                cardData.generalInfo._id = sliderModel.contentId;
//                                HungamaPartnerHandler.launchDetailsPage(cardData, mContext);
//                                showDetailsFragment(cardData);
                fetchCardDataAndLaunchDetailsPage(sliderModel.contentId);
                return;
//                                        CardData cardData = new CardData();
//                                        cardData.generalInfo = new CardDataGeneralInfo();
//                                        cardData.generalInfo.partnerId = partnerContentId;
//                                        cardData._id = sliderModel.contentId;
//                                        cardData.generalInfo._id = sliderModel.contentId;
//                                        HungamaPartnerHandler.launchDetailsPage(cardData, mContext);
            }
        }

        CardData movieData = new CardData();
        movieData._id = sliderModel.contentId;
        fetchSubscriptionPackages(movieData);
    }

    private int getPagerPadding() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenDensity = metrics.densityDpi;
        int margin = metrics.widthPixels / 4;
//        switch (screenDensity) {
//            case DisplayMetrics.DENSITY_LOW:
//                Log.d("DISPLAY PROFILE", "LOW");
//                margin = 30;
//                break;
//            case DisplayMetrics.DENSITY_MEDIUM:
//                Log.d("DISPLAY PROFILE", "MEDIUM");
//                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    margin = 275;
//                } else if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    margin = 15;
//                }
//                break;
//            case DisplayMetrics.DENSITY_TV:
//                Log.d("DISPLAY PROFILE", "TV");
//                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    margin = 345;
//                } else if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    margin = 0;
//                }
//                break;
//            case DisplayMetrics.DENSITY_HIGH:
//                Log.d("DISPLAY PROFILE", "HIGH");
//                margin = 125;
//                break;
//            case DisplayMetrics.DENSITY_XHIGH:
//                Log.d("DISPLAY PROFILE", "XHIGH");
//                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    margin = 675;
//                } else if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    margin = 45;
//                }
//                break;
//            case DisplayMetrics.DENSITY_XXHIGH:
//                Log.d("DISPLAY PROFILE", "XXHIGH");
//                margin = 200;
//                break;
//            case DisplayMetrics.DENSITY_XXXHIGH:
//                Log.d("DISPLAY PROFILE", "XXXHIGH");
//                margin = 250;
//                break;
//        }
        return margin;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_TYPE_LOGIN && resultCode == INTENT_RESPONSE_TYPE_SUCCESS) {
            handleOnClickBanners(mSliderModel);
        }
    }

    public void setSearchMoviedata(EventSearchMovieDataOnOTTApp searchMovieDataOnOTTApp) {
        this.searchMovieData = searchMovieDataOnOTTApp;
    }

    public static class CardDetailsCacheManagerCallBack implements CacheManager.CacheManagerCallback {

        private final WeakReference<AdapterMoviesCarousel2> refAdapterMoviesCarousel;

        public CardDetailsCacheManagerCallBack(WeakReference<AdapterMoviesCarousel2> refAdapterMoviesCarousel) {
            this.refAdapterMoviesCarousel = refAdapterMoviesCarousel;
        }

        @Override
        public void OnCacheResults(List<CardData> dataList) {
            if (refAdapterMoviesCarousel == null || dataList == null
                    || dataList.isEmpty()) {
                return;
            }
            AdapterMoviesCarousel2 adapterMoviesCarousel2 = refAdapterMoviesCarousel.get();
            adapterMoviesCarousel2.showDetailsFragment(dataList.get(0), -1);
            adapterMoviesCarousel2.dismissProgressBar();

        }

        @Override
        public void OnOnlineResults(List<CardData> dataList) {
            if (refAdapterMoviesCarousel == null || dataList == null
                    || dataList.isEmpty()) {
                return;
            }
            AdapterMoviesCarousel2 adapterMoviesCarousel2 = refAdapterMoviesCarousel.get();
            if (adapterMoviesCarousel2 == null) {
                return;
            }
            adapterMoviesCarousel2.showDetailsFragment(dataList.get(0), -1);
            adapterMoviesCarousel2.dismissProgressBar();

        }

        @Override
        public void OnOnlineError(Throwable error, int errorCode) {
            //Log.d(TAG, "OnOnlineError: " + error);
            if (refAdapterMoviesCarousel == null
                    || refAdapterMoviesCarousel.get() == null
                    || refAdapterMoviesCarousel.get().mContext == null) {
                return;
            }
            AdapterMoviesCarousel2 adapterMoviesCarousel2 = refAdapterMoviesCarousel.get();
            adapterMoviesCarousel2.dismissProgressBar();
            if (errorCode == APIRequest.ERR_NO_NETWORK) {
//                            AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
//                                    "", mContext.getString(R.string.play_button_retry), null);
                AlertDialogUtil.showToastNotification(adapterMoviesCarousel2.mContext.getString(R.string.network_error));
                return;
            }
            AlertDialogUtil.showToastNotification(adapterMoviesCarousel2.mContext.getString(R.string.canot_connect_server));
        }
    }

    private void fetchCardDataAndLaunchDetailsPage(final String _id) {
        showProgressBar();
        new CacheManager().getCardDetails(_id, true, new CardDetailsCacheManagerCallBack(new WeakReference<>(this)));
    }


    private String getImageLink(List<CardDataImagesItem> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_ICON};
        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : images) {

                if (DeviceUtils.isTablet(mContext)) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && UiUtil.getScreenDensity(mContext).equalsIgnoreCase(imageItem.profile)) {
                        Log.d("SCREEN DENSITY ", UiUtil.getScreenDensity(mContext)
                                + " IMAGE ITEM PROFILE " + imageItem.profile
                                + " IMAGE LINK " + imageItem.link);
                        return imageItem.link;
                    }
//                    if (imageType.equalsIgnoreCase(imageItem.type)
//                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
//                        return imageItem.link;
//                    }
                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XXHDPI.equalsIgnoreCase(imageItem.profile)) {
                        Log.d("IMAGE ITEM PROFILE ", imageItem.profile
                                + " IMAGE LINK " + imageItem.link);
                        return imageItem.link;
                    }
                }
            }
        }

        return null;
    }

    private void bindGenreViewHolder(final MovieCarouselViewHolder holder, final int position) {

//        //Log.d(TAG, "bindGenreViewHolder");

        if (mListCarouselInfo == null) {
            return;
        }
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(position - 1);
        if (carouselInfoData != null && carouselInfoData.title != null) {
            holder.mTextViewGenreMovieTitle.setText(carouselInfoData.title);
            holder.mLayoutViewAll.setTag(carouselInfoData);
            holder.mLayoutViewAll.setOnClickListener(mViewAllClickListener);
            holder.mLayoutViewAll.setVisibility(View.GONE);
            if (carouselInfoData.enableShowAll) {
                if (!TextUtils.isEmpty(carouselInfoData.showAll)) {
                    holder.mTextViewViewAll.setText(carouselInfoData.showAll);
                }
                holder.mLayoutViewAll.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(carouselInfoData.bgColor)) {
                try {
                    holder.mLayoutCarouselTitle.setBackgroundColor(Color.parseColor(carouselInfoData.bgColor));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            holder.mChannelImageView.setVisibility(View.GONE);
            String imageLink = getImageLink(carouselInfoData.images);
            if (!TextUtils.isEmpty(imageLink)) {
                if (imageLink == null || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                    holder.mChannelImageView.setVisibility(View.GONE);
                } else if (imageLink != null) {
                    holder.mChannelImageView.setVisibility(View.VISIBLE);
                   // Picasso.with(mContext).load(imageLink).into(holder.mChannelImageView);
                    PicassoUtil.with(mContext).load(imageLink, holder.mChannelImageView);
                    if (imageLink.contains(APIConstants.PARAM_SCALE_WRAP)) {
                        holder.mChannelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    } else {
                        if (DeviceUtils.isTablet(mContext)) {
                            holder.mChannelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                        } else {
                            holder.mChannelImageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
                        }
                    }
                }
//                if (DeviceUtils.isTablet(mContext)) {
//                    holder.mChannelImageView.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.margin_gap_36);
//                }
            }
        }

        AdapterBigHorizontalCarousel mAdapterBigHorizontalCarousel = null;
        if (carouselInfoData.listCarouselData == null) {
            LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " listCarouselData is null");
            carouselInfoData.listCarouselData = getDummyCarouselData();
        }
        if (holder.mRecyclerViewCarousel.getTag() instanceof AdapterBigHorizontalCarousel) {
            LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " getTag");
            mAdapterBigHorizontalCarousel = (AdapterBigHorizontalCarousel) holder.mRecyclerViewCarousel.getTag();
            mAdapterBigHorizontalCarousel.setData(carouselInfoData.listCarouselData);
        } else {
            LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " create adapter");
            mAdapterBigHorizontalCarousel = new AdapterBigHorizontalCarousel(mContext, carouselInfoData.listCarouselData);
        }
        LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " requestState- " + carouselInfoData.requestState);
        mAdapterBigHorizontalCarousel.showTitle(false);
        holder.mRecyclerViewCarousel.setItemAnimator(null);
        holder.mRecyclerViewCarousel.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        holder.mRecyclerViewCarousel.removeItemDecoration(mHorizontalMoviesDivieder);
        holder.mRecyclerViewCarousel.addItemDecoration(mHorizontalMoviesDivieder);
        holder.mRecyclerViewCarousel.setFocusableInTouchMode(false);
        holder.mRecyclerViewCarousel.setTag(mAdapterBigHorizontalCarousel);
        holder.mRecyclerViewCarousel.setItemAnimator(null);
        holder.mRecyclerViewCarousel.setHasFixedSize(true);
        mAdapterBigHorizontalCarousel.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
        mAdapterBigHorizontalCarousel.setParentPosition(position - 1);
        mAdapterBigHorizontalCarousel.setCarouselInfoData(carouselInfoData.name, carouselInfoData.pageSize);
        mAdapterBigHorizontalCarousel.isContinueWatchingSection(carouselInfoData.layoutType != null
                && APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType));
        mAdapterBigHorizontalCarousel.setRemoveItemListener(mOnItemRemovedListener);
        holder.mRecyclerViewCarousel.setAdapter(mAdapterBigHorizontalCarousel);
        if (TextUtils.isEmpty(carouselInfoData.name)) {
            return;
        }
        switch (carouselInfoData.requestState) {
            case NOT_LOADED:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
                startAsyncTaskInParallel(new CarouselRequestTask(AdapterMoviesCarousel2.this, carouselInfoData.name, carouselInfoData.pageSize > 0 ? carouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT, position,carouselInfoData.modified_on));
                break;
            case IN_PROGRESS:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                holder.mTextViewErrorRetry.setVisibility(View.VISIBLE);
                holder.mRecyclerViewCarousel.setVisibility(View.INVISIBLE);
                holder.mTextViewErrorRetry.setTag(position - 1);
                holder.mTextViewErrorRetry.setOnClickListener(mRetryListener);
                break;
            default:
                holder.mTextViewErrorRetry.setVisibility(View.GONE);
                holder.mRecyclerViewCarousel.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void startAsyncTaskInParallel(CarouselRequestTask task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    private final List<CardData> mDummyCarouselData = new ArrayList<>();

    private List<CardData> getDummyCarouselData() {
        if (!mDummyCarouselData.isEmpty()) {
            return mDummyCarouselData;
        }
        for (int i = 0; i < 10; i++) {
            mDummyCarouselData.add(new CardData());
        }
        return mDummyCarouselData;
    }

    @Override
    public int getItemCount() {
        if (mListCarouselInfo.isEmpty()) {
            return 0;
        }
        return mListCarouselInfo.size() + RECYCLERVIEW_OTHER_CHILD_ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == TYPE_HEADER_PAGER)
            return TYPE_HEADER_PAGER;
        else if (position == TYPE_ITEM || position <= mListCarouselInfo.size()) {
            if (APIConstants.LAYOUT_TYPE_VMAX_IMAGE_ADS.equalsIgnoreCase(mListCarouselInfo.get(position - 1).layoutType)
                    && PrefUtils.getInstance().getPrefVmaxNativeAdId() != null)
                return ITEM_TYPE_VMAX_IMAGE_ADVERTISE;
            else if (APIConstants.LAYOUT_TYPE_VMAX_VIDEO_ADS.equalsIgnoreCase(mListCarouselInfo.get(position - 1).layoutType)
                    && PrefUtils.getInstance().getPrefVmaxNativeAdId() != null)
                return ITEM_TYPE_VMAX_VIDEO_ADVERTISE;
            else if (APIConstants.LAYOUT_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM.equalsIgnoreCase(mListCarouselInfo.get(position -1).layoutType))
                return  ITEM_TYPE_SINGLE_RECTANGULAR_BANNER_ITEM;
            else if (APIConstants.LAYOUT_TYPE_PROMO_BANNER.equalsIgnoreCase(mListCarouselInfo.get(position -1).layoutType))
                return ITEM_TYPE_PROMO_BANNER;
            else if (APIConstants.LAYOUT_TYPE_EMPTY_FOOTER.equalsIgnoreCase(mListCarouselInfo.get(position-1).layoutType))
                return ITEM_TYPE_EMPTY_FOOTER;
            else
                return TYPE_ITEM;
        }

        return TYPE_FOOTER_GRID;
    }

    public void setOttAppsList(List<OTTApp> ottAppList) {
        mAppsList = ottAppList;
        for (OTTApp app : mAppsList) {
            if (app.images != null && app.images.values != null) {
                for (CardDataOttImagesItem image : app.images.values) {
                    if (image.type != null && image.type.equalsIgnoreCase("thumbnail")) {
                        app.imageUrl = image.link;
                    } else if (image.type != null && image.type.equalsIgnoreCase("coverposter")) {
                        OTTAppsImageSliderAdapter.SliderModel model = new OTTAppsImageSliderAdapter.SliderModel();
                        model.imageUrl = image.link;
                        model.ottApp = app;
                        model.siblingOrder = image.siblingOrder;
                        model.contentId = image.contentId;
                        model.partnerContentId = image.partnerContentId;
                        mSliderItems.add(model);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setCarouselInfoData(List<CarouselInfoData> carouselList) {
        if (carouselList == null) {
            return;
        }
        mListCarouselInfo = carouselList;
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxBannerAdId())
                && PrefUtils.getInstance().getPrefEnableVmaxFooterBannerAd()) {
            SDKLogger.debug("Adding empty view");
            addEmptyViewAtFooter();
        }
        notifyDataSetChanged();
    }

    static class GenericViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListener clickListener;

        GenericViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (clickListener == null) return;
            this.clickListener.onClick(v, getAdapterPosition());
        }
    }

    class MovieViewPagerViewHolder extends GenericViewHolder {

        private final AutoScrollViewPager mViewPager;
        private final TextView mDescriptionTxt;
        private final LinearLayout mOfferDecriptionLayout;
        private final LinearLayout mPreviewLayout;
        //        private final TabLayout mTabIndicatorDots;
        final CircleIndicator mIndicator;
        private final RelativeLayout mViewPagerContainer;
        private ImageView mRightArrow;
        private ImageView mLeftArrow;
        private View leftGradient,rightGradient;
        private RelativeLayout gradientContainer;

        MovieViewPagerViewHolder(View itemView) {
            super(itemView);
//            mIndicator = (CirclePageIndicator)itemView.findViewById(R.id.indicator);
            mViewPager = (AutoScrollViewPager) itemView.findViewById(R.id.pager_ottapps);
//            mTabIndicatorDots = (TabLayout) itemView.findViewById(R.id.tab_dots);
            mDescriptionTxt = (TextView) itemView.findViewById(R.id.slider_title);
            mOfferDecriptionLayout = (LinearLayout) itemView.findViewById(R.id.offer_description_layout);
            mPreviewLayout = (LinearLayout) itemView.findViewById(R.id.previewLayout);
            mViewPagerContainer = (RelativeLayout) itemView.findViewById(R.id.pager_ottapps_layout);
//            mIndicator = (CircleIndicator) itemView.findViewById(R.id.view_pager_indicator);
            mIndicator = (CircleIndicator) itemView.findViewById(R.id.view_pager_indicator);
            mRightArrow = (ImageView)itemView.findViewById(R.id.viewpager_right_arrow);
            mLeftArrow = (ImageView)itemView.findViewById(R.id.viewpager_left_arrow);
            leftGradient = itemView.findViewById(R.id.left_gradient_view);
            rightGradient = itemView.findViewById(R.id.right_gradient_view);
            gradientContainer = (RelativeLayout) itemView.findViewById(R.id.grdient_container);
//            int Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
//            mViewPager.setMinimumHeight(Height);
//            mViewPagerContainer.setMinimumHeight(Height);
        }
    }

    class MovieAppsGridViewHolder extends GenericViewHolder {

        private final GridView mGridViewApps;

        MovieAppsGridViewHolder(View itemView) {
            super(itemView);
            mGridViewApps = (GridView) itemView.findViewById(R.id.gridview_apps);
        }
    }

    /**
     * ViewHolder of the VMX Banner item
     */
    private class VmxItemViewHolder extends GenericViewHolder {
        private View vmaxAdViewBanner;

        VmxItemViewHolder(View view) {
            super(view);

        }
    }

    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    class MovieCarouselViewHolder extends GenericViewHolder {

        private final RecyclerView mRecyclerViewCarousel;
        private final TextView mTextViewGenreMovieTitle;
        private final TextView mTextViewViewAll;
        private final TextView mTextViewErrorRetry;
        private final LinearLayout mLayoutViewAll;
        private final ImageView mChannelImageView;
        final RelativeLayout mLayoutCarouselTitle;


        MovieCarouselViewHolder(View view) {
            super(view);
            mTextViewGenreMovieTitle = (TextView) view.findViewById(R.id.textview_genre_title);
            mTextViewViewAll = (TextView) view.findViewById(R.id.textview_view_all);
            mRecyclerViewCarousel = (RecyclerView) view.findViewById(R.id.recycler_view_movie);
            mLayoutViewAll = (LinearLayout) view.findViewById(R.id.layout_view_all);
            mChannelImageView = (ImageView) view.findViewById(R.id.toolbar_tv_channel_Img);
            mLayoutCarouselTitle = (RelativeLayout) view.findViewById(R.id.layout_carousel_title);
            mTextViewErrorRetry = (TextView) view.findViewById(R.id.textview_error_retry);
        }
    }

    // Overriding this method to get access to recyclerView on which current MovieAdapter has been attached.
    // In the future we will use that reference for scrolling to newly added relatedMovies item
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerViewMoviesCarousel = recyclerView;
//        mRecyclerViewMoviesCarousel.addOnScrollListener(Util.getSrollListenerForPicasso(mContext));
        recyclerView.getRecycledViewPool().setMaxRecycledViews(TYPE_ITEM, 15);
        recyclerView.setItemViewCacheSize(15);
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
    }

    private static class CarouselRequestTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        private final String mPageName;
        private final boolean mIsToOnlyUpdateCache;
        private int mPageCount;
        private int mPosition;
        private String modified_on;
        private WeakReference<AdapterMoviesCarousel2> weakReference;
        CarouselRequestTask(AdapterMoviesCarousel2 adapterMoviesCarousel2, String pageName, int pageCount, int position,String modified_on) {
            weakReference = new WeakReference<>(adapterMoviesCarousel2);
            mPageName = pageName;
            mIsToOnlyUpdateCache = false;
            mPosition = position;
            mPageCount = pageCount;
            this.modified_on = modified_on;
        }

        CarouselRequestTask(AdapterMoviesCarousel2 adapterMoviesCarousel2, String pageName, boolean mIsToOnlyUpdateCache,String modified_on) {
            weakReference = new WeakReference<>(adapterMoviesCarousel2);
            mPageName = pageName;
            this.mIsToOnlyUpdateCache = mIsToOnlyUpdateCache;
            this.modified_on = modified_on;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            boolean isCacheRequest = true;
            if (mIsToOnlyUpdateCache) {
                isCacheRequest = false;
            }
            if (weakReference == null || weakReference.get() == null) {
                return null;
            }
            new MenuDataModel().fetchCarouseldata(weakReference.get().mContext, mPageName, 1, mPageCount, isCacheRequest, modified_on,new MenuDataModel.CarouselContentListCallback() {
                @Override
                public void onCacheResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnCacheResults: name- " + mPageName + " mPageCount: " + mPageCount);
                    if (mIsToOnlyUpdateCache) {
                        return;
                    }
                    if (weakReference == null || weakReference.get() == null) {
                        return;
                    }
                    if (dataList != null && !dataList.isEmpty()) {
                        weakReference.get().addCarouselData(dataList, mPosition);
                    }
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnCacheResults: name- " + mPageName + " mPageCount: " + mPageCount);
                    if (mIsToOnlyUpdateCache) {
                        return;
                    }
                    if (weakReference == null || weakReference.get() == null) {
                        return;
                    }
                    if (dataList != null && !dataList.isEmpty()) {
                        weakReference.get().addCarouselData(dataList, mPosition);
                    }

                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {
                    if (weakReference == null || weakReference.get() == null) {
                        return;
                    }
                    weakReference.get().addCarouselData(null, mPosition);
                }
            });
            return null;
        }

        protected void onPreExecute() {
            // Perform setup - runs on user interface thread
        }

        protected void onPostExecute(Void result) {
            // Update user interface
        }
    }


    private void addCarouselData(List<CardData> carouselList, final int position) {
        try {
            CarouselInfoData carouselInfoData = mListCarouselInfo.get(position - 1);
            if (carouselList == null) {
                LoggerD.debugLogAdapter("carousel title- " + carouselInfoData.title + " requestState- " + carouselInfoData.requestState);
                carouselInfoData.requestState = RequestState.ERROR;
            } else {
                carouselInfoData.requestState = RequestState.SUCCESS;
                carouselInfoData.listCarouselData = carouselList;
            }
            safelyNotifyItemChanged(position);
        } catch (IllegalStateException e) {
            //Occurs while we try to modify data of recycler view white it is scrolling
            e.printStackTrace();
            mRecyclerViewMoviesCarousel.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void safelyNotifyItemChanged(final int position) {
        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mRecyclerViewMoviesCarousel.isComputingLayout()) {
                        notifyItemChanged(position);
                    } else {
                        safelyNotifyItemChanged(position);
                    }
                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void bannerImagesOrder(List<OTTAppsImageSliderAdapter.SliderModel> sliderItems) {
        Collections.sort(sliderItems, new Comparator<OTTAppsImageSliderAdapter.SliderModel>() {

            @Override
            public int compare(OTTAppsImageSliderAdapter.SliderModel lhs, OTTAppsImageSliderAdapter.SliderModel rhs) {
                if (lhs == null
                        || rhs == null) {
                    return -1;
                }
                if (lhs.siblingOrder == null
                        || rhs.siblingOrder == null) {
                    return -1;
                }
                int lhsSiblingOrder = Integer.parseInt(lhs.siblingOrder);
                int rhsSiblingOrder = Integer.parseInt(rhs.siblingOrder);
                return rhsSiblingOrder > lhsSiblingOrder ? 1 : -1;
            }
        });
    }

    private void launchPlayStore(String androidAppUrl) {
        Uri appStoreLink = Uri.parse(androidAppUrl);
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, appStoreLink));
    }

    private void handleOTTAppClicked(final OTTApp appData) {

        PackageManager pm = mContext.getPackageManager();
        try {
            final Intent launchIntent = pm.getLaunchIntentForPackage(appData.androidPackageName);
            if (launchIntent != null) {
                //app is installed launching the app
                mContext.startActivity(launchIntent);
                return;
            }
            String confirmMessage = mContext.getString(R.string
                    .start);
            String alertMessage = appData.confirmationMessage;
            if (appData.installationHelp != null && !appData.installationHelp.isEmpty()) {
                alertMessage = appData.installationHelp;
            }
            if (alertMessage == null) {
                alertMessage = "Watch free movies on " + appData.title;
            }
            AlertDialogUtil.showAlertDialog(mContext, alertMessage, "",
                    mContext.getString(R.string.cancel), confirmMessage, new AlertDialogUtil.DialogListener() {
                        @Override
                        public void onDialog1Click() {

                        }

                        @Override
                        public void onDialog2Click() {

                            String url = appData.androidAppUrl;

                            if (appData.installType != null && appData.installType.equalsIgnoreCase("ottConfirm")) {
                                url = APIConstants.getOTTAppDownloadUrl(appData.appName);
                            }
                            if (url != null) {
                                //app is not installed launching the playstore
                                if (ConnectivityUtil.isConnected(mContext)) {
                                    launchPlayStore(url);
                                } else {
                                    Toast.makeText(mContext, mContext.getResources().getString(R
                                            .string.network_error), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleOTTAppClicked(final OTTApp appData, CardData movieData) {

        PackageManager pm = mContext.getPackageManager();
        try {
            final Intent launchIntent = pm.getLaunchIntentForPackage(appData.androidPackageName);
            if (launchIntent != null) {
                //app is installed launching the app
                if (movieData != null && movieData.generalInfo != null && !TextUtils.isEmpty(movieData.generalInfo.deepLink)) {
                    launchIntent.setData(Uri.parse(movieData.generalInfo.deepLink));
                }
                mContext.startActivity(launchIntent);
                return;
            }
            String confirmMessage = mContext.getString(R.string
                    .start);
            String alertMessage = appData.confirmationMessage;
            if (appData.installationHelp != null && !appData.installationHelp.isEmpty()) {
                alertMessage = appData.installationHelp;
            }
            if (alertMessage == null) {
                alertMessage = "Watch free movies on " + appData.title;
            }
            AlertDialogUtil.showAlertDialog(mContext, alertMessage, "",
                    mContext.getString(R.string.cancel), confirmMessage, new AlertDialogUtil.DialogListener() {
                        @Override
                        public void onDialog1Click() {

                        }

                        @Override
                        public void onDialog2Click() {

                            String url = appData.androidAppUrl;

                            if (appData.installType != null && appData.installType.equalsIgnoreCase("ottConfirm")) {
                                url = APIConstants.getOTTAppDownloadUrl(appData.appName);
                            }
                            if (url != null) {
                                //app is not installed launching the playstore
                                if (ConnectivityUtil.isConnected(mContext)) {
                                    launchPlayStore(url);
                                } else {
                                    Toast.makeText(mContext, mContext.getResources().getString(R
                                            .string.network_error), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchSubscriptionPackages(final CardData carouselData) {
        CacheManager mCacheManager = new CacheManager();
        if (carouselData == null) {
            return;
        }

        boolean isCacheRequest = false;
        if (SDKUtils.getCardExplorerData().cardDataToSubscribe == null) {
            isCacheRequest = true;
        }
        boolean isCurrentUserDataRequest = true;
        showProgressBar();
        mCacheManager.getSubscriptionPackages(carouselData._id, isCacheRequest, isCurrentUserDataRequest, new CacheManager.CacheManagerCallback() {
            @Override
            public void OnCacheResults(List<CardData> dataList) {
                dismissProgressBar();
                if (dataList == null || dataList.isEmpty()) {
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.canot_connect_server));
                    return;
                }
                carouselData.currentUserData = dataList.get(0).currentUserData;
                if (dataList.get(0).packages != null) {
                    carouselData.packages = dataList.get(0).packages;
                }
                if (dataList.get(0).publishingHouse != null) {
                    carouselData.publishingHouse = dataList.get(0).publishingHouse;
                }
                if (dataList.get(0).generalInfo != null) {
                    carouselData.generalInfo = dataList.get(0).generalInfo;
                }

                if (ApplicationController.FLAG_ENABLE_TRYNBUY_SUBSCRIPTION) {
                    launchDeepLinkOrShowPackages(carouselData);
                    return;
                }

                String publishingHouse = carouselData == null
                        || carouselData.publishingHouse == null
                        || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;

                if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                    HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, null, "banners");
                    return;
                }
            }

            @Override
            public void OnOnlineResults(List<CardData> dataList) {
                dismissProgressBar();
                if (dataList == null || dataList.isEmpty()) {
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.canot_connect_server));
                    return;
                }
                carouselData.currentUserData = dataList.get(0).currentUserData;
                if (dataList.get(0).packages != null) {
                    carouselData.packages = dataList.get(0).packages;
                }
                if (dataList.get(0).publishingHouse != null) {
                    carouselData.publishingHouse = dataList.get(0).publishingHouse;
                }
                if (dataList.get(0).generalInfo != null) {
                    carouselData.generalInfo = dataList.get(0).generalInfo;
                }

                if (ApplicationController.FLAG_ENABLE_TRYNBUY_SUBSCRIPTION) {
                    launchDeepLinkOrShowPackages(carouselData);
                    return;
                }

                String publishingHouse = carouselData == null
                        || carouselData.publishingHouse == null
                        || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;

                if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                    HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, null, "banners");
                    return;
                }

            }

            @Override
            public void OnOnlineError(Throwable error, int errorCode) {
                //Log.d(TAG, "Failed: " + error);
                dismissProgressBar();
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
//                            AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
//                                    "", mContext.getString(R.string.play_button_retry), null);
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                    return;
                }
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.canot_connect_server));
            }
        });

    }

    private void launchDeepLinkOrShowPackages(CardData movieData) {
        if (movieData.currentUserData != null && movieData.currentUserData.purchase != null && movieData.currentUserData.purchase.size() != 0 || !ApplicationController.FLAG_ENABLE_TRYNBUY_SUBSCRIPTION) {
            //packs already purchased.
            launchDeepLinkOrInstallPartnerApp(movieData);
            if (ApplicationController.FLAG_ENABLE_TRYNBUY_SUBSCRIPTION)
                updateCarouselMoviesData();
        } else {
            //not purchased display packs
            showPackages(movieData);
        }
    }

    private void updateCarouselMoviesData() {
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
        for (CarouselInfoData carousel : mListCarouselInfo) {
            startAsyncTaskInParallel(new CarouselRequestTask(AdapterMoviesCarousel2.this, carousel.name, true,carousel.modified_on));
        }
    }

    private void showPackages(CardData movieData) {
        if (movieData == null || movieData.packages == null) {
            return;
        }
        SDKUtils.getCardExplorerData().cardDataToSubscribe = movieData;
        if (movieData.packages.size() > 1) {
            if (movieData != null
                    && movieData.packages != null
                    && movieData.packages.size() > 0) {
                SubscriptionPacksDialog subscriptionPacksDialog = new SubscriptionPacksDialog(mContext);
                subscriptionPacksDialog.showDialog(movieData);
                return;
            }
        }
        for (CardDataPackages packageItem : movieData.packages) {
            if (packageItem.priceDetails != null) {
                float price = 10000.99f;
                for (int i = 0; i < packageItem.priceDetails.size(); i++) {
                    CardDataPackagePriceDetailsItem priceDetailItem = packageItem.priceDetails.get(i);
                    if (!priceDetailItem.paymentChannel.equalsIgnoreCase(APIConstants.PAYMENT_CHANNEL_INAPP)
                            && priceDetailItem.price < price
                            && priceDetailItem.name.equalsIgnoreCase("sundirect")) {
                        price = priceDetailItem.price;
                        if (price > 0.0) {
                            SubcriptionEngine mSubscriptionEngine = new SubcriptionEngine(mContext);
                            mSubscriptionEngine.doSubscription(packageItem, i);
                            return;
                        }
                    }
                }
            }
        }
    }


    public void showProgressBar() {

        if (mContext == null) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressDialog.show(mContext, "", "Loading...", true, true, null);
        mProgressDialog.setContentView(R.layout.layout_progress_dialog);
        ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.imageView1);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(UiUtil.getColor(mContext,R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
    }

    public void dismissProgressBar() {
        try {
            if (mContext == null || ((Activity) mContext).isFinishing()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onFullScreen(boolean b) {

        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
        if (b) {
            if (mRecyclerViewMoviesCarousel.findViewHolderForAdapterPosition(0) instanceof MovieViewPagerViewHolder) {
                MovieViewPagerViewHolder viewPagerViewHolder = (MovieViewPagerViewHolder) mRecyclerViewMoviesCarousel.findViewHolderForAdapterPosition(0);
                viewPagerViewHolder.mViewPager.stopAutoScroll();
            }
        } else {
            if (mRecyclerViewMoviesCarousel.findViewHolderForAdapterPosition(0) instanceof MovieViewPagerViewHolder) {
                MovieViewPagerViewHolder viewPagerViewHolder = (MovieViewPagerViewHolder) mRecyclerViewMoviesCarousel.findViewHolderForAdapterPosition(0);
                viewPagerViewHolder.mViewPager.startAutoScroll();
            }
        }
    }


    private void safelyNotifyItemRemoved(final CarouselInfoData carouselInfoData) {
        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LoggerD.debugDownload("DeletionCarouselInfo: removal " + carouselInfoData.title);
                    if (carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()) {
                        int mParentPosition = mListCarouselInfo.indexOf(carouselInfoData);
                        mListCarouselInfo.remove(carouselInfoData);
                        if (!mRecyclerViewMoviesCarousel.isComputingLayout()) {
                            mRecyclerViewMoviesCarousel.getRecycledViewPool().clear();
                            notifyItemRemoved(mParentPosition);
                            notifyItemRangeChanged(mParentPosition, mListCarouselInfo.size());
                        } else {
                            safelyNotifyItemRemoved(carouselInfoData);
                        }
                    }

                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void bindPromoBannerItem(PromoBannerViewHolder holder, int position) {
        CarouselInfoData carouselInfoData = null;
        if (mListCarouselInfo != null
                && position < mListCarouselInfo.size()) {
            carouselInfoData = mListCarouselInfo.get(position);
        }
        if (DeviceUtils.isTablet(mContext)) {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                DisplayMetrics dm = new DisplayMetrics();
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
                int Height = (int) (((ApplicationController.getApplicationConfig().screenHeight * heightRatio) / widthRatio));
                holder.mImageView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenHeight;
                holder.mImageView.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + holder.mImageView.getLayoutParams().width);
            } else {
                DisplayMetrics dm = new DisplayMetrics();
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
                int Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * heightRatio) / widthRatio));
                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                holder.mImageView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenWidth;
                holder.mImageView.getLayoutParams().height = Height;
                Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
            }
        } else {
//            DisplayMetrics dm = new DisplayMetrics();
//            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
//            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
//            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
            int widthRatio = 9;
            int heightRatio = 4;
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
            int Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * heightRatio) / widthRatio));
            Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
            holder.mImageView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenWidth;
            holder.mImageView.getLayoutParams().height = Height;
            Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
        }
        carouselInfoData = mListCarouselInfo.get(position-1);
        String imageLink = carouselInfoData.getPromoUrl(DeviceUtils.isTablet(mContext), UiUtil.getScreenDensity(mContext));
        if (TextUtils.isEmpty(imageLink)
                || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            holder.mImageView.setImageResource(R.drawable.black);
        } else if (imageLink != null) {
            if (mContext.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE) {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageView, R.drawable.black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageView, R.drawable.black);
            }
        }
    }


    /**
     * ViewHolder of the medium horizontal item
     */
    class PromoBannerViewHolder extends GenericViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        public PromoBannerViewHolder(View view) {
            super(view);
            mImageView = (ImageView)itemView.findViewById(R.id.promo_image);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            super.onClick(v);
            onPromoBannerItemCicked(v, getAdapterPosition());
        }
    }
    class PromoBannerV2ViewHolder extends GenericViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        public PromoBannerV2ViewHolder(View view) {
            super(view);
            mImageView = (ImageView)itemView.findViewById(R.id.promo_image);
            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            super.onClick(v);
//            onPromoBannerItemCicked(v, getAdapterPosition());
        }
    }

    private void onPromoBannerItemCicked(View v, int adapterPosition) {
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) return;
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(adapterPosition-1);
        if (carouselInfoData != null && HomePagerAdapterDynamicMenu.ACTION_LAUNCH_WEBPAGE.equalsIgnoreCase(carouselInfoData.appAction)) {
//            int launchType = SubscriptionWebActivity.PARAM_LAUNCH_NONE;
//            ((MainActivity)mContext).startActivityForResult(SubscriptionWebActivity.createIntent(mContext, carouselInfoData.actionUrl, launchType), SUBSCRIPTION_REQUEST);
            String url = carouselInfoData.actionUrl;
            if (!Util.checkUserLoginStatus()) {
                if (mContext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mContext;
                    mainActivity.initLogin(Analytics.VALUE_SOURCE_CAROUSEL, Analytics.VALUE_SOURCE_DETAILS_PROMO_BANNER);
                }
                return;
            }
            if (url == null) return;
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

            Bundle bundle = new Bundle();
            bundle.putString(FragmentWebView.PARAM_URL, url);
            bundle.putBoolean(FragmentWebView.PARAM_SHOW_TOOLBAR, true);
            bundle.putString(FragmentWebView.PARAM_TOOLBAR_TITLE, carouselInfoData.title);
            BaseFragment fragment = (BaseFragment) Fragment.instantiate(mContext, FragmentWebView.class.getName(), bundle);
            ((MainActivity) mContext).pushFragment(fragment);
        }
    }

    private void addEmptyViewAtFooter() {
        CarouselInfoData dummyFooterView = new CarouselInfoData();
        dummyFooterView.layoutType = APIConstants.LAYOUT_TYPE_EMPTY_FOOTER;
        if (mListCarouselInfo == null)
            mListCarouselInfo = new ArrayList<>();
        mListCarouselInfo.add(dummyFooterView);
    }

    /**
     *ViewHolder for LiveTV Program Item
     */
    class EmptyFooterViewItem extends GenericViewHolder {

        public EmptyFooterViewItem(View view) {
            super(view);
        }
    }
}
