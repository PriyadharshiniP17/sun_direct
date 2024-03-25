package com.myplex.myplex.ui.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPackages;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.utils.FixedAspectRatioRelativeLayout;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AdapterLongBigHorizontalCarousel extends RecyclerView.Adapter<AdapterLongBigHorizontalCarousel.CarouselDataViewHolder>{

    private static final String TAG = AdapterLongBigHorizontalCarousel.class.getSimpleName();
//    private boolean mIsDummyData = false;

    private final Context mContext;
    private List<CardData> mListMovies,mListMoviesRef;
    private String mPageName;
    private int mPageSize;
    private ImageView mImageViewFree;
    private ProgressDialog mProgressDialog;
    private boolean isContinueWatchingSection;
    private OnItemRemovedListener mOnItemRemovedListener;
    private RecyclerView recyclerViewReference;
    private String layoutType;
    List<CardDataPackages> userPackages;

    private View.OnClickListener mOnItemRemoveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            removeItem(view);
        }
    };
    private boolean showTitle;

    public void setLayoutType(String layoutType){
        this.layoutType = layoutType;
    }

    private void removeItem(View view) {
        try {
            if (view != null
                    && view.getTag() != null
                    && view.getTag() instanceof Integer) {
                int pos = (int) view.getTag();
                Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos)._id,mListMovies.get(pos).getType());
                mListMovies.remove(pos);
               /* notifyDataSetChanged();
                if (mOnItemRemovedListener != null) {
                    mOnItemRemovedListener.onItemRemoved(mParentPosition);
                }*/
                if (mListMoviesRef != null && mListMoviesRef.size()>pos) {
                    mListMoviesRef.remove(pos);
                }
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos,mListMovies.size());
            }
            if (mOnItemRemovedListener != null && (mListMovies == null || (mListMovies != null && mListMovies.size()==0))) {
                mOnItemRemovedListener.onItemRemoved(mParentPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParentPosition(int mParentPosition) {
        this.mParentPosition = mParentPosition;
    }

    private int mParentPosition;

    //adapter view click listener

    public AdapterLongBigHorizontalCarousel(Context context, List<CardData> itemList, RecyclerView recyclerView, String mPageName) {
        mContext = context;
        mListMovies = itemList;
        mListMoviesRef = itemList;
        this.mPageName=mPageName;
        recyclerViewReference = recyclerView;
        userPackages = PrefUtils.getInstance().getPackages();
    }

    public AdapterLongBigHorizontalCarousel(Context context, List<CardData> itemList) {
        mContext = context;
        mListMovies = itemList;
    }
    private boolean isSimilarContentCarousel = false;
    public void isSimilarContents(boolean isSimilarContentCarousel){
        this.isSimilarContentCarousel = isSimilarContentCarousel;
    }

    public void setData(final List<CardData> listMovies) {
        if (listMovies == null || recyclerViewReference == null) {
            return;
        }
        mListMovies = listMovies;
        mListMovies = new ArrayList<>(listMovies);;
        mListMoviesRef = listMovies;
        recyclerViewReference.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!recyclerViewReference.isComputingLayout()) {
                        if(recyclerViewReference != null && recyclerViewReference.getRecycledViewPool() != null) {
                            recyclerViewReference.getRecycledViewPool().clear();
                        }
                        notifyDataSetChanged();
                    } else {
                        setData(listMovies);
                    }
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
    }

    public void addData(List<CardData> listMovies) {
        if(listMovies == null){
            return;
        }

        //Log.d(TAG, "addData");
        if(mListMovies == null){
            mListMovies = listMovies;
            notifyDataSetChanged();
            return;
        }
        mListMovies.addAll(listMovies);
        notifyDataSetChanged();

    }

    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        CarouselDataViewHolder customItemViewHolder;

        if (viewType==TYPE_ITEM){
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem_big_movie_recycler, parent, false);
        }else {
            view = LayoutInflater.from(mContext).inflate(R.layout.native_thumbnail_layout, parent, false);
        }
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //if you need three fix imageview in width
        /*int devicewidth = (displaymetrics.widthPixels) / 3;

        holder.thumbnailMovieLayout.getLayoutParams().width = devicewidth;
        holder.mImageViewMovies.getLayoutParams().width = devicewidth;
        holder.layoutContainer.getLayoutParams().width = devicewidth;
*/
        if (mListMovies.get(position).isAdType()){
            if(!Util.isPremiumUser()){
                bindAdView(holder,mListMovies.get(position));
            } else {
                if (holder.adMainLayout!=null){
                    holder.adMainLayout.setVisibility(GONE);
                }
            }
        } else {
            bindGenreViewHolder(holder, mListMovies.get(position));
        }

    }

    private void bindAdView(CarouselDataViewHolder holder, CardData cardData) {
        if (cardData.isAdType()&&!Util.isPremiumUser()&&cardData.content!=null&&cardData.content.actionURL!=null){
            if (cardData.content.actionURL.contains(";")) {
                String[] adUnitIdAndSize = cardData.content.actionURL.split(";");
                String adUnitId=adUnitIdAndSize[0];
                String adUnitSizes=adUnitIdAndSize[1];
                String adUnitPosition=adUnitIdAndSize[2];
                if (adUnitId!=null&&adUnitSizes!=null&&adUnitPosition!=null&&adUnitSizes.contains(",")){
                    String[] adSizesInWidthAndHeight=adUnitSizes.split(",");
                    String adWidth=adSizesInWidthAndHeight[0];
                    String adHeight=adSizesInWidthAndHeight[1];
                    AdLoader adLoader = new AdLoader.Builder(mContext, adUnitId)
                            /*.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                                @Override
                                public void onNativeAdLoaded(@NotNull NativeAd NativeAd) {
                                    if (holder.adTemplate != null){
                                        holder.adTemplate.setNativeAd(NativeAd);
                                        holder.adFrameLayout.setVisibility(GONE);
                                        holder.adTemplate.setVisibility(VISIBLE);
                                    }
                                }
                            })*/.forAdManagerAdView(new OnAdManagerAdViewLoadedListener() {
                                @Override
                                public void onAdManagerAdViewLoaded(@NotNull AdManagerAdView adView) {
                                    // Show the banner ad.
                                    holder.adFrameLayout.setVisibility(VISIBLE);
                                    holder.adTemplate.setVisibility(GONE);
                                    holder.adFrameLayout.addView(adView);
                                }
                            }, new AdSize(Integer.parseInt(adWidth),Integer.parseInt(adHeight)))
                            .withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                                    Util.loadAdError(loadAdError, TAG);
                                }
                            })
                            .build();

                    adLoader.loadAd(new AdManagerAdRequest.Builder()
                            .addCustomTargeting("ad_position", adUnitPosition)
                            .addCustomTargeting("content_type", "NA")
                            .addCustomTargeting("content_id", "NA")
                            .addCustomTargeting("content_language", "NA")
                            .addCustomTargeting("user_type", Util.getUserType())
                            .addCustomTargeting("gender", Util.getGenderString())
                            .addCustomTargeting("age", Util.getAgeString())
                            .addCustomTargeting("content_name", "NA")
                            .addCustomTargeting("tags", "NA")
                            .addCustomTargeting("content_page", mPageName)
                            .addCustomTargeting("duration", "NA")
                            /*.addCustomTargeting("consent_targeting","Yes")*/
                            /*.addCustomTargeting("source","")*/
                            .addCustomTargeting("video_watch_count", PrefUtils.getInstance().getAdVideoCount()+"")
                            .build());

                    PrefUtils.getInstance().setAdVideoCount(PrefUtils.getInstance().getAdVideoCount()+1);

                    String AdData="Ad tags Big carousel:::"+"ad_position : "+ adUnitPosition+ "  user_type : "+ Util.getUserType()+"  gender : "+Util.getGenderString()+
                            "  age : "+ Util.getAgeString()+"  content_page : "+ mPageName+
                            "  video_watch_count : "+ PrefUtils.getInstance().getAdVideoCount();

                    if (ApplicationController.SHOW_PLAYER_LOGS) {
                        AlertDialogUtil.showAdAlertDialog(mContext, AdData, "AD Logs", "Okay");
                    }

                }else {
                    if (holder.adMainLayout!=null){
                        holder.adMainLayout.setVisibility(GONE);
                    }
                }
            }else {
                if (holder.adMainLayout!=null){
                    holder.adMainLayout.setVisibility(GONE);
                }
            }
        }else {
            if (holder.adMainLayout!=null){
                holder.adMainLayout.setVisibility(GONE);
            }
        }
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mContext, adWidth);
    }


    private void bindGenreViewHolder(final CarouselDataViewHolder holder, final CardData carouselData) {

        if (carouselData != null) {

/*
            if (carouselData.isAdType()){
                holder.mAdLayout.setVisibility(View.VISIBLE);
                holder.thumbnailMovieLayout.setVisibility(View.GONE);
            }else {
                holder.mAdLayout.setVisibility(View.GONE);
                holder.thumbnailMovieLayout.setVisibility(View.VISIBLE);
            }
*/

            if (carouselData.generalInfo != null
                    && carouselData.generalInfo.title != null) {
                holder.mTextViewMovieTitle.setText(carouselData.generalInfo.title);
                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                        && !TextUtils.isEmpty(carouselData.globalServiceName)) {
                    holder.mTextViewMovieTitle.setText(carouselData.globalServiceName);
                }
            }
            holder.mThumbnailDelete.setVisibility(View.GONE);
            holder.mContinueWatchingProgress.setVisibility(View.GONE);
            holder.rentLayout.setVisibility(View.GONE);
           /* if(carouselData != null && carouselData.generalInfo !=null && carouselData.generalInfo.contentRights != null && carouselData.generalInfo.contentRights.size()>0 && carouselData.generalInfo.contentRights.get(0)!= null) {
                if (carouselData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                    holder.rentLayout.setVisibility(View.VISIBLE);
                }
            }*/
            if (isContinueWatchingSection) {
                holder.mThumbnailDelete.setVisibility(View.VISIBLE);
                holder.mThumbnailDelete.setTag(mListMovies.indexOf(carouselData));
                holder.mThumbnailDelete.setOnClickListener(mOnItemRemoveClickListener);
                holder.mContinueWatchingProgress.setVisibility(View.GONE);
                if(carouselData != null && carouselData.elapsedTime > 0){
                    try {
                        int position = carouselData.elapsedTime;
                        int duration = Util.calculateDurationInSeconds(carouselData.content.duration);
                        int percent = 0;
                        if (duration > 0) {
                            // use long to avoid overflow
                            percent = (int) (100L * position / duration);
                        }
                        holder.mContinueWatchingProgress.setVisibility(View.VISIBLE);
                        holder.mContinueWatchingProgress.setProgress(percent);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            String imageLink = getImageLink(carouselData);
            if (TextUtils.isEmpty(imageLink)
                    || imageLink.compareTo("Images/NoImage.jpg") == 0) {
                holder.mImageViewMovies.setImageResource(R.drawable.black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.black);
            }
           /* if(carouselData != null && carouselData.generalInfo !=null && carouselData.generalInfo.contentRights != null && carouselData.generalInfo.contentRights.size()>0 && carouselData.generalInfo.contentRights.get(0)!= null) {
                if (carouselData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                    holder.rentLayout.setVisibility(View.VISIBLE);
                    if (carouselData.packages != null && userPackages != null) {
                        for (CardDataPackages pack :
                                carouselData.packages) {
                            for (CardDataPackages userPack : userPackages) {
                                if (pack != null && pack.packageId != null && userPack != null && userPack.packageId != null) {
                                    if (pack.packageId.equalsIgnoreCase(userPack.packageId)) {
                                        holder.rentLayout.setVisibility(View.GONE);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }*/
            /*int placeholderid = R.drawable.movie_thumbnail_placeholder;
            int width = (ApplicationController.getApplicationConfig().screenWidth/3) - (int)Util.convertDpToPixel(20,mContext);
            int height = width * 9 / 16;
            height = width * 3/ 2;
            holder.mImageViewMovies.getLayoutParams().height = height;
            holder.mImageViewMovies.requestLayout();
            if (imageLink == null
                    || imageLink.compareTo("Images/NoImage.jpg") == 0) {
                holder.mImageViewMovies.getLayoutParams().height=height;
                holder.mImageViewMovies.requestLayout();
                //Picasso.with(mContext).load(placeholderid).error(placeholderid).placeholder(placeholderid).resize(width, height).into(mViewHolder.mThumbnailMovie);
                PicassoUtil.with(mContext).load(placeholderid, holder.mImageViewMovies,placeholderid,width,height);
            } else if (imageLink != null) {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies,placeholderid,width,height);
                //Picasso.with(mContext).load(imageLink).error(placeholderid).placeholder(placeholderid).resize(width, height).into(mViewHolder.mThumbnailMovie);
            }*/
        }
        try {

            if (carouselData != null && carouselData.publishingHouse != null) {
                if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(carouselData.publishingHouse.publishingHouseName)) {
                    if(PrefUtils.getInstance().getPrefEnableHungamaLogo()) {
                        // display hungama logo on movie poster
                        holder.mImageViewProvideLogo.setImageResource(R.drawable.hungama_logo);
                        holder.mImageViewProvideLogo.setVisibility(View.VISIBLE);
                    }
                    boolean isRentContent = false;
                    if (carouselData.generalInfo != null
                            && carouselData.generalInfo.contentRights != null
                            && !carouselData.generalInfo.contentRights.contains(APIConstants.CONTENT_RIGHTS_SVOD)) {
                        if (carouselData.generalInfo.contentRights.contains(APIConstants.CONTENT_RIGHTS_TVODPREMIUM)
                                || carouselData.generalInfo.contentRights.contains(APIConstants.CONTENT_RIGHTS_TVOD)) {
                            isRentContent = true;
                        }
                    }
                    if (isRentContent && PrefUtils.getInstance().getPrefEnableHungamaRentTag()) {
                        holder.mImageViewRentBand.setVisibility(View.GONE);
                    } else {
                        holder.mImageViewRentBand.setVisibility(View.GONE);
                    }
/*                            if (!PrefUtils.getInstance().getPrefEnableHungamaSDK()) {
                                Intent hungamaLaunchIntent = ApplicationController.getHungamaLaunchIntent();
                                if (hungamaLaunchIntent != null) {
                                    //app is installed launching the app
                                    holder.mImageViewInstallIcon.setVisibility(View.GONE);
                                } else {
                                    holder.mImageViewInstallIcon.setVisibility(View.VISIBLE);
                                }
                            }*/

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(carouselData == null){
            holder.mImageViewPlayIcon.setVisibility(View.GONE);
        } else {
            // holder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
            if(!Util.isFreeContent(carouselData)){
                holder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_pay_icon);
            } else {
                holder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_play_icon);
            }
        }

/*        if (carouselData != null
                && carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TRAILER.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type))) {
            holder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
        }*/
//        holder.voteAvrg.setText(String.valueOf(movie.getVoteAverage()));
//        holder.date.setText(movie.getReleaseDate());
        holder.setClickListener(mOnItemClickListenerWithData);
        if (mOnItemClickListenerWithData == null) {
            holder.setClickListener(mOnItemClickListenerDefault);
        }
        holder.mTextViewMovieTitle.setText(carouselData.getTitle());
        holder.mTextViewMovieTitle.setVisibility(View.GONE);
        if (showTitle) {
            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
        }



//            }
//        });
        String partnerImageLink = carouselData.getPartnerImageLink(mContext);
        SDKLogger.debug("partnerImage " + partnerImageLink);
        if (!TextUtils.isEmpty(partnerImageLink)) {
            holder.mImageViewPartner.setVisibility(View.VISIBLE);
            //Picasso.with(mContext).load(partnerImageLink).resize(holder.mImageViewPartner.getLayoutParams().width, holder.mImageViewPartner.getLayoutParams().height).placeholder(R.drawable.epg_thumbnail_default).centerInside().into(holder.mImageViewPartner);
            PicassoUtil.with(mContext).loadCenterInside(partnerImageLink,holder.mImageViewPartner,R.drawable.black,
                    holder.mImageViewPartner.getLayoutParams().width, holder.mImageViewPartner.getLayoutParams().height);
        }else{
            holder.mImageViewPartner.setVisibility(View.GONE);
        }
        if (carouselData != null ) {
            showViewRatingOnThumbnail(holder,carouselData);
        }

        if(carouselData.generalInfo != null && holder.userBadgeImage != null){
            if(!TextUtils.isEmpty(carouselData.generalInfo.accessLabelImage) && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE){
                holder.userBadgeImage.setVisibility(View.VISIBLE);
                PicassoUtil.with(mContext).load(carouselData.generalInfo.accessLabelImage,holder.userBadgeImage);
            }else {
                holder.userBadgeImage.setVisibility(View.GONE);
            }
        }

        boolean isRentTVODContent = false;
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.contentRights != null && carouselData.generalInfo.contentRights.size()>0){
            List<String> contentRights = carouselData.generalInfo.contentRights;
            for(int i=0; i < contentRights.size(); i++){
                if (APIConstants.CONTENT_RIGHTS_TVOD.equalsIgnoreCase(contentRights.get(i))) {
                    isRentTVODContent = true;
                }
            }
        }



        /*if (holder.rentTVODBadge != null) {
            if (holder.rentTVODBadge != null && isRentTVODContent && APIConstants.IS_ENABLE_TVOD_RENT_BADGE) {
                holder.rentTVODBadge.setVisibility(View.VISIBLE);
            } else {
                holder.rentTVODBadge.setVisibility(View.GONE);
            }
        }*/
    }

    public void showViewRatingOnThumbnail(CarouselDataViewHolder holder, CardData cardData){

        /*if(cardData.content != null && PrefUtils.getInstance().isShowCarouselRatingsEnabled()){
            if (TextUtils.isEmpty(cardData.content.contentRating)) {
                holder.thumbnailRatingIcon.setVisibility(View.GONE);
                holder.ratingCountText.setVisibility(View.GONE);
            } else {
                holder.viewRatingParentLayout.setVisibility(View.VISIBLE);
                holder.thumbnailRatingIcon.setVisibility(View.VISIBLE);
                holder.ratingCountText.setVisibility(View.VISIBLE);
                holder.ratingCountText.setText(cardData.content.contentRating);
            }
        }else {
            holder.thumbnailRatingIcon.setVisibility(View.GONE);
            holder.ratingCountText.setVisibility(View.GONE);
        }
        if(cardData.stats != null && PrefUtils.getInstance().isShowCarouselViewsEnabled()){
            if (TextUtils.isEmpty(cardData.stats.getViewCount())) {
                holder.thumbnailViewsIcon.setVisibility(View.GONE);
                holder.viewsCountText.setVisibility(View.GONE);
            } else {
                holder.viewRatingParentLayout.setVisibility(View.VISIBLE);
                holder.thumbnailViewsIcon.setVisibility(View.VISIBLE);
                holder.viewsCountText.setVisibility(View.VISIBLE);
                holder.viewsCountText.setText(cardData.stats.getViewCount());
            }
        }else {
            holder.thumbnailViewsIcon.setVisibility(View.GONE);
            holder.viewsCountText.setVisibility(View.GONE);
        }*/
        if(isSimilarContentCarousel)
            holder.viewRatingParentLayout.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if(mListMovies == null) return 0;

        return mListMovies.size();
    }

    private static final int TYPE_ITEM = 1;
    public static final int TYPE_AD=2;
    @Override
    public int getItemViewType(int position) {

        if (mListMovies.get(position).isAdType()){
            return TYPE_AD;
        }
        return TYPE_ITEM;

        /*if (mItemList.get(position) instanceof Movie) {
            type = TYPE_MOVIE;
        } else if (mItemList.get(position) instanceof RelatedMoviesItem) {
            type = TYPE_RELATED_ITEMS;
        }*/

//        return type;
    }

    public void isContinueWatchingSection(boolean b) {
        this.isContinueWatchingSection = b;
    }

    public void setRemoveItemListener(OnItemRemovedListener mOnItemRemovedListener) {
        this.mOnItemRemovedListener = mOnItemRemovedListener;
    }

    public void showTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    public class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListenerWithData clickListener;

        final ImageView mImageViewMovies;
        final ImageView mImageViewPlayIcon;
        final ImageView mImageViewInstallIcon;
        final ImageView mImageViewProvideLogo;
        final TextView mTextViewMovieTitle;
        final ImageView mImageViewRentBand;
        public ImageView mThumbnailDelete;
        public ProgressBar mContinueWatchingProgress;
        final ImageView mImageViewPartner;
        private ImageView thumbnailRatingIcon;
        private ImageView thumbnailViewsIcon;
        private TextView ratingCountText;
        private TextView viewsCountText;
        private RelativeLayout viewRatingParentLayout;
        private ImageView userBadgeImage;
        private ImageView rentTVODBadge;
        //        final ImageView mImageViewRupeeIcon;
        final RelativeLayout thumbnailMovieLayout;
        final RelativeLayout layoutContainer;
        /*final RelativeLayout mAdLayout;
        */
        final TemplateView adTemplate;
        final FrameLayout adFrameLayout;
        final LinearLayout adMainLayout;
        final FixedAspectRatioRelativeLayout fixedAspectRatioRelativeLayout;
        private LinearLayout rentLayout;

        public CarouselDataViewHolder(View view) {
            super(view);
//            UiUtil.showFeedback(view,true, R.color.list_item_bkg);
            mTextViewMovieTitle = (TextView)view.findViewById(R.id.textview_movies_title);
            mImageViewMovies = (ImageView)view.findViewById(R.id.thumbnail_movie);
            thumbnailMovieLayout = (RelativeLayout)view.findViewById(R.id.thumbnail_movie_layout);
            layoutContainer = (RelativeLayout)view.findViewById(R.id.layout_container);
            mImageViewPlayIcon = (ImageView)view.findViewById(R.id.thumbnail_movie_play);
            mImageViewInstallIcon = (ImageView)view.findViewById(R.id.thumbnail_provider_app_install);
            mImageViewProvideLogo = (ImageView)view.findViewById(R.id.thumbnail_provider_app);
            mImageViewRentBand = (ImageView)view.findViewById(R.id.thumbnail_rent_band);
            mThumbnailDelete = (ImageView)view.findViewById(R.id.thumbnail_movie_delete_icon);
            mContinueWatchingProgress = (ProgressBar) view.findViewById(R.id.continue_watching_progress);
            mImageViewPartner= (ImageView)  view.findViewById(R.id.iv_partener_logo_right);
//            mImageViewRupeeIcon = (ImageView)view.findViewById(R.id.thumbnail_rupee_icon);
            ratingCountText = view.findViewById(R.id.rating_count_text);
            thumbnailRatingIcon = view.findViewById(R.id.rating_icon);
            viewsCountText = view.findViewById(R.id.views_count_text);
            thumbnailViewsIcon = view.findViewById(R.id.views_icon);
            viewRatingParentLayout = view.findViewById(R.id.view_rating_parent);
            userBadgeImage = view.findViewById(R.id.content_badge);
            mImageViewFree= (ImageView) view.findViewById(R.id.iv_free_logo_left);
            adFrameLayout=view.findViewById(R.id.ad_layout);
            adMainLayout=view.findViewById(R.id.ad_main_layout);
            /*mAdLayout=view.findViewById(R.id.new_ad_layout);
            */
            //rentTVODBadge = (ImageView)view.findViewById(R.id.content_rent_badge);
            adTemplate=view.findViewById(R.id.thumbnail_ad_layout);
            rentLayout = view.findViewById(R.id.iv_rent);
            fixedAspectRatioRelativeLayout=view.findViewById(R.id.fixed_layout);
            fixedAspectRatioRelativeLayout.getLayoutParams().width = itemView.getResources().getDimensionPixelSize(R.dimen.long_big_horizontal_width);
            mImageViewMovies.getLayoutParams().width = itemView.getResources().getDimensionPixelSize(R.dimen.long_big_horizontal_width);
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try {
                        if (mListMovies == null || mListMovies.isEmpty()) return false;
                        if (getAdapterPosition()>=mListMovies.size()){
                            return false;
                        }
                        CardData data = mListMovies.get(getAdapterPosition());
                        if (data == null) {
                            return false;
                        }
                        String title = data.getTitle();
                        if (TextUtils.isEmpty(title)) {
                            return false;
                        }
                        AlertDialogUtil.showToastNotification(title);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
            view.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListenerWithData itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (this.clickListener != null && mListMovies != null && getAdapterPosition() != -1) {
                this.clickListener.onClick(v, getAdapterPosition(), mParentPosition, mListMovies.get(getAdapterPosition()));
            }
        }

    }

    // Overriding this method to get access to recyclerView on which current MovieAdapter has been attached.
    // In the future we will use that reference for scrolling to newly added relatedMovies item
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }

    private ItemClickListenerWithData mOnItemClickListenerWithData;
    public void setOnItemClickListenerWithMovieData(ItemClickListenerWithData onItemClickListenerWithData) {
        this.mOnItemClickListenerWithData = onItemClickListenerWithData;
    }

    public static final String getImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{
                APIConstants.IMAGE_TYPE_PORTRAIT_BANNER,
                APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,
                APIConstants.IMAGE_TYPE_THUMBNAIL,
                APIConstants.IMAGE_TYPE_THUMBNAIL_BANNER,
                APIConstants.IMAGE_TYPE_COVERPOSTER
        };

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

    public final ItemClickListenerWithData mOnItemClickListenerDefault = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

            Analytics.gaEventBrowsedCategoryContentType(carouselData);
/*

            if (carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
                //Log.d(TAG,"type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
                showRelatedVODListFragment(carouselData);
                return;
            }
*/

            if(position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA){
                return;
            }

            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;

            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, null, null,position);
                return;
            }

            if (carouselData != null
                    && carouselData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !TextUtils.isEmpty(carouselData.generalInfo.deepLink)) {
//                carouselData.generalInfo.deepLink = "http://www.sonyliv.com/details/live/5230736998001/Santhiya-Sehaj-Path-:-Giani-Jagtar-Singh-:-Epi-112";
                mContext.startActivity(LiveScoreWebView.createIntent(mContext, carouselData.generalInfo.deepLink,APIConstants.TYPE_SPORTS,carouselData.generalInfo.title));
                return;
            }

            showDetailsFragment(carouselData,position);

        }

    };

    private boolean isSonyLiveContent(CardData data) {
        if(data == null
                || data.publishingHouse == null){
            return false;
        }

        if(APIConstants.TYPE_SONYLIV.equals(data.publishingHouse.publishingHouseName)){
            return true;
        }
        return false;
    }


    private void showDetailsFragment(CardData carouselData,int contentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);

        if(carouselData != null
                && carouselData.generalInfo != null){
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)){
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
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, isSonyLiveContent(carouselData) ? CardDetails.Partners.SONY : CardDetails.Partners.APALYA);

        if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                && !APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type)
                && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                && !carouselData.isTVSeries()
                && !carouselData.isVODChannel()
                && !carouselData.isVODYoutubeChannel()
                && !carouselData.isVODCategory()
                && !carouselData.isTVSeason()) {
            CacheManager.setCardDataList(mListMovies);
        }
        String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        if (mSource != null && !mSource.isEmpty()) {
            args.putString(Analytics.PROPERTY_SOURCE, mSource);
        }else{
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        }
        if(mSourceDetails != null &&! mSourceDetails.isEmpty()){
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSourceDetails);
        }else{
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_SIMILAR_CONTENT);
        }

        if (carouselData.generalInfo != null
                && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type))) {
            args.putBoolean(CardDetails.PARAM_RESET_EPG_DATE_POSITION_IN_DETAIL, true);
        }
        args.putInt(CleverTap.PROPERTY_CONTENT_POSITION, contentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args,carouselData);
    }

    public void setCarouselInfoData(String name, int pageSize) {
        if(!TextUtils.isEmpty(mPageName)){
            return;
        }
        this.mPageName = name;
        this.mPageSize = pageSize;
//        new MenuDataModel().fetchCarouseldataInAsynTask(mContext, mPageName, 1, mPageSize, true,this);
    }




/*


    public void onEventMainThread(EventNetworkConnectionChange event) {
        LoggerD.debugLog(TAG + " ConnectivityReceiver: connectivity- " + event.isConnected() + " mPageName- " + mPageName + " mPageSize- " + mPageSize);
        if(event.isConnected() && isContainingDummies() || mListMovies == null || mListMovies.isEmpty()){
            new MenuDataModel().fetchCarouseldataInAsynTask(mContext, mPageName, 1, mPageSize, true, new MenuDataModel.CarouselContentListCallback() {

                @Override
                public void onCacheResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnCacheResults: name- " + mPageName);
                    if (dataList != null && !dataList.isEmpty()) {
                        addCarouselData(dataList);
                    }
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    //Log.d(TAG, "OnOnlineResults: name- " + mPageName);

                    if (dataList != null && !dataList.isEmpty()) {
                        addCarouselData(dataList);
                    }
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {

                }
            });
        }
    }
*/

    public  String mSourceDetails,mSource ;
    public void setSourceDetailsForAnalytics(String sourceDetailsForAnalytics,String source){
        mSourceDetails = sourceDetailsForAnalytics;
        mSource = source;
    }
}