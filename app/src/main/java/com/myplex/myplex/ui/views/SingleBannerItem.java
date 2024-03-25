package com.myplex.myplex.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.RequestState;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.activities.UrlGatewayActivity;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.adapter.HomePagerAdapterDynamicMenu;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.fragment.FragmentVODList;
import com.myplex.myplex.ui.fragment.SmallSquareItemsFragment;
import com.myplex.myplex.utils.ChromeTabUtils;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by ramaraju on 07/04/2019.
 */
public class SingleBannerItem extends GenericListViewCompoment {

    private List<CarouselInfoData> mListCarouselInfo;
    private Context mContext;
    private static final String TAG = SingleBannerItem.class.getSimpleName();
    private final RecyclerView.ItemDecoration mHorizontalDividerDecoration;
    private RecyclerView mRecyclerViewCarouselInfo;
    private CarouselInfoData parentCarouselData;
    private View view;
    private CarouselInfoData carouselInfoData;
    private final RecyclerView.ItemDecoration mHorizontalMoviesDivieder;
    private EventSearchMovieDataOnOTTApp searchMovieData;
    private String mMenuGroup;
    private String mPageTitle;
    GenericListViewCompoment holder = this;
    private Typeface mBoldTypeFace;
    public SingleBannerItem(Context mContext, View view, List<CarouselInfoData> mListCarouselInfo, RecyclerView mRecyclerViewCarouselInf,String mMenuGroup, String mPageTitleo) {
        super(view);
        this.view = view;
        this.mContext = mContext;
        this.mListCarouselInfo = mListCarouselInfo;
        mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        mHorizontalMoviesDivieder = new HorizontalItemDecorator(margin);
        this.mMenuGroup = mMenuGroup;
        this.mPageTitle = mPageTitle;
        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");
    }

    public static SingleBannerItem createView(Context context, ViewGroup parent,
                                               List<CarouselInfoData> carouselInfoData, RecyclerView mRecyclerViewCarouselInfo,String mMenuGroup, String mPageTitle) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_singlebanneritem, parent, false);
        return new SingleBannerItem(context, view, carouselInfoData, mRecyclerViewCarouselInfo,mMenuGroup,  mPageTitle);
    }



    @Override
    public void bindItemViewHolder(final int position) {

        this.position = position;
        int Height;
        CarouselInfoData carouselInfoData = null;
        if (mListCarouselInfo != null
                && position < mListCarouselInfo.size()) {
            carouselInfoData = mListCarouselInfo.get(position);
        }

        if (carouselInfoData!=null&&carouselInfoData.showTitle) {
            if (carouselInfoData.title != null) {
                mTextViewGenreMovieTitle.setTypeface(mBoldTypeFace);
                mTextViewGenreMovieTitle.setText(carouselInfoData.title);
            } else {
                mTextViewGenreMovieTitle.setVisibility(View.GONE);
            }
        }
        carouselInfoData = mListCarouselInfo.get(position);
        String imageLink = carouselInfoData.getPromoUrl(DeviceUtils.isTablet(mContext), UiUtil.getScreenDensity(mContext));
        if (TextUtils.isEmpty(imageLink)
                || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            holder.iv_movie.setImageResource(R.drawable.black);
        } else {
            if (mContext.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE) {
                PicassoUtil.with(mContext).load(imageLink, holder.iv_movie, R.drawable.black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.iv_movie, R.drawable.black);
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
        /*if (DeviceUtils.isTablet(mContext)) {
            DisplayMetrics dm = new DisplayMetrics();
            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
            Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth + " dm.density- " + dm.density + " real width- " + (ApplicationController.getApplicationConfig().screenWidth / dm.density));
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Height = (int) mContext.getResources().getDimension(R.dimen.margin_gap_275);
                holder.iv_movie.getLayoutParams().height = Height;
            } else {
                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                Height = (int) (ApplicationController.getApplicationConfig().screenWidth *0.56f);
                holder.iv_movie.getLayoutParams().height = Height;
            }
        } else {
            Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
            holder.iv_movie.getLayoutParams().height = Height;
            Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
        }
        if (mListCarouselInfo == null) {
            return;
        }*/
        /*final CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);

        if (carouselInfoData.listCarouselData == null || carouselInfoData.listCarouselData.isEmpty()) {
            //Log.d(TAG, "carouselInfoData.listCarouselData");
            if (!TextUtils.isEmpty(carouselInfoData.name)
                    && carouselInfoData.requestState == RequestState.NOT_LOADED) {
                carouselInfoData.requestState = RequestState.IN_PROGRESS;
                startAsyncTaskInParallel(new CarouselRequestTask(carouselInfoData));
            }
        } else {
            final CardData cardData = carouselInfoData.listCarouselData.get(0);
            String imageLink = null;
            imageLink = getImageLink(cardData);
            PicassoUtil.with(mContext).download(imageLink, new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    LoggerD.debugDownload( TAG
                            + "\nbitmap- " + bitmap
                            + "\ndownloadData- " + cardData);
                    if (bitmap == null || cardData == null) {
                        return;
                    }
                }

                @Override
                public void onBitmapFailed(Exception e,Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
            if (imageLink == null
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                holder.iv_movie.setImageResource(R.drawable
                        .banner_placeholder);
            } else if (imageLink != null) {
                if (mContext.getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_LANDSCAPE) {
                    PicassoUtil.with(mContext).
                            loadCenterInside(imageLink,holder.iv_movie,
                                    R.drawable.banner_placeholder,
                                    ApplicationController.getApplicationConfig().
                                    screenWidth,Height);
                } else {
                    PicassoUtil.with(mContext).loadCenterCrop(imageLink,holder.iv_movie,
                            R.drawable.banner_placeholder,
                            ApplicationController.getApplicationConfig().screenWidth,Height);

                }
            }
            holder.iv_movie.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cardData == null) {
                        return;
                    }

                    String publishingHouse = cardData == null
                            || cardData.publishingHouse == null
                            || TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName) ? null : cardData.publishingHouse.publishingHouseName;

                    if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                        HungamaPartnerHandler.launchDetailsPage(cardData, mContext, carouselInfoData, null);
                        return;
                    }
                    if (cardData != null
                            && cardData.generalInfo != null
                            && APIConstants.TYPE_SPORTS.equalsIgnoreCase(cardData.generalInfo.type)
                            && !TextUtils.isEmpty(cardData.generalInfo.deepLink)) {
                        mContext.startActivity(LiveScoreWebView.createIntent(mContext, cardData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, cardData.generalInfo.title));
                        return;
                    }
                    showDetailsFragment(cardData, -1,carouselInfoData.title,position);
                }
            });
            holder.mTextViewGenreMovieTitle.setText(carouselInfoData.title);
            String partnerImageLink = cardData.getPartnerImageLink(mContext);
            SDKLogger.debug("partnerImage " + partnerImageLink);
            if (!TextUtils.isEmpty(partnerImageLink)) {
                holder.mImageViewPartner.setVisibility(View.VISIBLE);
                PicassoUtil.with(mContext).
                        loadCenterInside(partnerImageLink,holder.
                                mImageViewPartner,
                                R.drawable.epg_thumbnail_default,
                                holder.mImageViewPartner.getLayoutParams().width,
                                holder.mImageViewPartner.getLayoutParams().height);
            }else{
                holder.mImageViewPartner.setVisibility(View.GONE);
            }

        }*/
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
        }
        else if(carouselInfoData != null && APIConstants.ACTION_TYPE_DEEPLINK.equalsIgnoreCase(carouselInfoData.appAction)){
            constructDeepLinkUrl(carouselInfoData.actionUrl);
        }
    }

    private void constructDeepLinkUrl(String actionUrl) {
        Intent intent = new Intent(mContext, UrlGatewayActivity.class);
        intent.setData(Uri.parse(actionUrl));
//        Log.d("LOG_TAG", "Intent: " + actionUrl);
        mContext.startActivity(intent);
        return;
    }

    private void startAsyncTaskInParallel(CarouselRequestTask task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private class CarouselRequestTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.Fr
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
        private final CarouselInfoData carouselInfoData;

        public CarouselRequestTask(CarouselInfoData carouselInfoData) {
            this.carouselInfoData = carouselInfoData;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            boolean isCacheRequest = true;
            new MenuDataModel().setPortraitBannerRequest(APIConstants.isPortraitBannerLayout(carouselInfoData)).fetchCarouseldata(mContext, carouselInfoData.name, 1, carouselInfoData.pageSize > 0 ? carouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT, isCacheRequest, carouselInfoData.modified_on,new MenuDataModel.CarouselContentListCallback() {
                @Override
                public void onCacheResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnCacheResults: name- " + carouselInfoData.name);
                    addCarouselData(dataList, carouselInfoData);
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnOnlineResults: name- " + carouselInfoData.name);
                    addCarouselData(dataList, carouselInfoData);
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {
                    addCarouselData(null, carouselInfoData);
                }
            });
            return null;
        }

    }

    private void addCarouselData(final List<CardData> carouselList, final CarouselInfoData carouselInfoData) {
        if (carouselList == null) {
            LoggerD.debugLogAdapter("carousel null results - " + carouselInfoData.title + " requestState- failed");
            carouselInfoData.requestState = RequestState.ERROR;
        } else {
            try {
                LoggerD.debugLogAdapter("carousel empty results - " + carouselInfoData.title + " requestState- success");
                carouselInfoData.requestState = RequestState.SUCCESS;
                carouselInfoData.listCarouselData = carouselList;
                notifyItemChanged();
            } catch (IllegalStateException e) {
                if (mRecyclerViewCarouselInfo != null) {
                    mRecyclerViewCarouselInfo.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged();
                        }
                    });
                }
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

            CarouselInfoData carouselInfoData = parentPosition >= 0 ? mListCarouselInfo.get(parentPosition) : null;

            if (carouselInfoData != null && carouselInfoData.title != null) {
                gaBrowse(carouselData, carouselInfoData.title);
                //Log.d(TAG, "carouselSectionName: " + carouselInfoData.title);
            }


            Analytics.gaEventBrowsedCategoryContentType(carouselData);

            if (carouselInfoData != null) {
                Analytics.setVideosCarouselName(carouselInfoData.title);
            }
            /*if (carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
                //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
                showRelatedVODListFragment(carouselData, parentPosition);
                return;
            }
*/
            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;


            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, carouselInfoData, null,parentPosition);
                return;
            }

            LoggerD.debugLog("pacakge name- " + mContext.getPackageName());

            if (carouselData != null
                    && carouselData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !TextUtils.isEmpty(carouselData.generalInfo.deepLink)) {
//                carouselData.generalInfo.deepLink = "http://www.sonyliv.com/details/live/5230736998001/Santhiya-Sehaj-Path-:-Giani-Jagtar-Singh-:-Epi-112";
                Analytics.createEventGA(Analytics.ACTION_TYPES.browse.name(), Analytics.EVENT_ACTION_BROWSE_SONY_SPORTS, carouselData.generalInfo.title, 1l);
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, carouselData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, carouselData.generalInfo.title));
                return;
            }

            if (carouselInfoData == null) {
                showDetailsFragment(carouselData, position,"",parentPosition);
            } else {
                showDetailsFragment(carouselData, carouselInfoData,parentPosition);
            }

        }

    };

    private void showDetailsFragment(CardData carouselData, CarouselInfoData carouselInfoData, int parentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
            //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, carouselData);
        }
        if (carouselData != null
                && carouselData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                args.putString(CardDetails.PARAM_CARD_ID, carouselData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != carouselData.startDate
                        && null != carouselData.endDate) {
                    Date startDate = Util.getDate(carouselData.startDate);
                    Date endDate = Util.getDate(carouselData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY_MINIMIZED, false);
                    }
                }
            }

        }

        if (!APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType)) {
            if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !carouselData.isTVSeries()
                    && !carouselData.isVODChannel()
                    && !carouselData.isVODYoutubeChannel()
                    && !carouselData.isVODCategory()
                    && !carouselData.isTVSeason()) {
                args.putSerializable(CardDetails.PARAM_QUEUE_LIST_CARD_DATA, (Serializable) carouselInfoData.listCarouselData);
            }
        }

        String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private void showDetailsFragment(CardData carouselData, int position,String carousalTitle,int parentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
            //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, carouselData);
        }
        if (carouselData != null
                && carouselData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                args.putString(CardDetails.PARAM_CARD_ID, carouselData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != carouselData.startDate
                        && null != carouselData.endDate) {
                    Date startDate = Util.getDate(carouselData.startDate);
                    Date endDate = Util.getDate(carouselData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY_MINIMIZED, false);
                    }
                }
            }
        }

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carousalTitle);
        if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            if (searchMovieData != null) {
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, searchMovieData.getSearchString());
            }
        }
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private final View.OnClickListener mViewAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {

            if (view.getTag() instanceof CarouselInfoData) {
                CleverTap.eventPageViewed(CleverTap.PAGE_VIEW_ALL);
                final CarouselInfoData carouselData = (CarouselInfoData) view.getTag();
                if (carouselData == null || carouselData.name == null) {
                    return;
                }
                int carouselPosition=-1;
                if (carouselData != null && mListCarouselInfo != null && mListCarouselInfo.size() > 0) {
                    for(int i=0;i<mListCarouselInfo.size();i++){
                        if(mListCarouselInfo.get(i)!=null&&!TextUtils.isEmpty(mListCarouselInfo.get(i).name)&&mListCarouselInfo.get(i).name.equalsIgnoreCase(carouselData.name))
                            carouselPosition = i;
                    }
                }
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, 500);
                view.setEnabled(false);
                if (APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW.equalsIgnoreCase(mMenuGroup)) {
                    Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_TVSHOWS, carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_TV_SHOWS, carouselData.title, true);
                } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
                    Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_VIDEOS, carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_VIDEOS, carouselData.title, true);
                } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
                    Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_MUSIC_VIDEOS, carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_MUSIC_VIDEOS, carouselData.title, true);
                } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup) || APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mMenuGroup)) {
                    Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_KIDS, carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(HomePagerAdapter.getPageKids(), carouselData.title, true);
                } else if (carouselData != null && mPageTitle != null) {
                    Analytics.browseViewAllEvent("browsed " + carouselData.title.toLowerCase(), carouselData);
                    AppsFlyerTracker.eventBrowseTabWithSectionViewAll(mPageTitle.toLowerCase(), carouselData.title, true);
                }
                if (carouselData != null)
                    LoggerD.debugLog("carouselData.showAllLayoutType- " + carouselData.showAllLayoutType);
                if (APIConstants.LAYOUT_TYPE_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                    showRelatedVODListFragment(carouselData,carouselPosition);
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)
                        || APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                    showCarouselViewAllFragment(carouselData,carouselPosition);
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(carouselData.showAllLayoutType)
                        && APIConstants.LAYOUT_TYPE_NESTED_CAROUSEL.equalsIgnoreCase(carouselData.layoutType)) {
                    Bundle args = new Bundle();
                    args.putSerializable(SmallSquareItemsFragment.PARAM_CAROUSEL_INFO_DATA, carouselData);
                    args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPosition);
                    ((MainActivity) mContext).pushFragment(SmallSquareItemsFragment.newInstance(args));
                    return;
                } else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                    showVODListFragment(carouselData,carouselPosition);
                    return;
                } else if (carouselData != null
                        && carouselData.showAllLayoutType.contains(APIConstants.LAUNCH_TAB_HASH)) {
                    try {
                        ((MainActivity) mContext).redirectToPage(carouselData.showAllLayoutType.split(APIConstants.PREFIX_HASH)[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                showVODListFragment(carouselData,carouselPosition);
            }
        }
    };

    private void showCarouselViewAllFragment(CarouselInfoData movieData, int carouselPosition) {
        Bundle args = new Bundle();

        CacheManager.setCarouselInfoData(movieData);

        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPosition);

        args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, mMenuGroup);
        if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup) || APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mMenuGroup)) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, APIConstants.TYPE_MOVIE);
        }
        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }

    private void showRelatedVODListFragment(CarouselInfoData parentCarouselInfoData, int carouselPosition) {
        //TODO show RelatedVodListFragment from main activity context

        Bundle args = new Bundle();
        //args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        if (carouselPosition >= 0) {
            if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty()) {
                CarouselInfoData  carouselInfoData= mListCarouselInfo.get(carouselPosition);
                CacheManager.setCarouselInfoData(carouselInfoData);

            }
        }
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));


    }

    private void showVODListFragment(CarouselInfoData carouselInfoData, int carouselPositoin) {
        //TODO show VODListFragment from MainActivity with bundle

        MenuDataModel.setSelectedMoviesCarouselList(carouselInfoData.name + "_" + 1, carouselInfoData.listCarouselData);
        Bundle args = new Bundle();
        CacheManager.setCarouselInfoData(carouselInfoData);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPositoin);
        args.putBoolean(ApplicationController.PARTNER_LOGO_BOTTOM_VISIBILTY, !APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM.equalsIgnoreCase(carouselInfoData.layoutType));
        if (!APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselInfoData.showAllLayoutType)) {
            args.putBoolean(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_CAROUSEL_GRID, true);
        }

        if (!TextUtils.isEmpty(mMenuGroup)) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, mMenuGroup);
        }
        ((MainActivity) mContext).pushFragment(FragmentVODList.newInstance(args));
    }

    public void setSearchMoviedata(EventSearchMovieDataOnOTTApp searchMovieDataOnOTTApp) {
        this.searchMovieData = searchMovieDataOnOTTApp;
    }


    private void gaBrowse(CardData movieData, String carouselSectionName) {

        if (movieData.generalInfo == null || movieData.generalInfo.title == null || carouselSectionName == null) {
            return;
        }

        if (APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_TVSHOWS, carouselSectionName, movieData.generalInfo.title);
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_VIDEOS, carouselSectionName, movieData.generalInfo.title);
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_MUSIC_VIDEOS, carouselSectionName, movieData.generalInfo.title);
        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup)||APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mMenuGroup)) {
            Analytics.gaBrowseCarouselSection(Analytics.EVENT_BROWSED_KIDS, carouselSectionName, movieData.generalInfo.title);
        } else if (mPageTitle != null) {
            Analytics.gaBrowseCarouselSection("browsed " + mPageTitle.toLowerCase(), carouselSectionName, movieData.generalInfo.title);
        }
    }

    public static final String getImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_BANNER, APIConstants.IMAGE_TYPE_COVERPOSTER,APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,APIConstants.IMAGE_TYPE_THUMBNAIL};

        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : carouselData.images.values) {
//                LoggerD.debugHooqVstbLog("imageItem:` imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
//                    LoggerD.debugHooqVstbLog("imageType- " + imageType + " imageItem: imageType- " + imageItem.type);
                    return imageItem.link;
                }
            }
        }

        return null;
    }
}
