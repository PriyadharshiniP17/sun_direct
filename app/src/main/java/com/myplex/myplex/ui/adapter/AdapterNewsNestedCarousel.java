package com.myplex.myplex.ui.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.card.MaterialCardView;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


public class AdapterNewsNestedCarousel extends RecyclerView.Adapter<AdapterNewsNestedCarousel.CarouselDataViewHolder> {

    private static final String TAG = AdapterNewsNestedCarousel.class.getSimpleName();

    private final Context mContext;
    private List<CardData> mListMovies;
//    private boolean mIsDummyData = false;
    private int mParentPosition;
    private String mPageName;
    private int mPageSize;
    private ProgressDialog mProgressDialog;
    private GenericListViewCompoment parentViewHolder;
    private RecyclerView recyclerViewReference;
    private String mBgColor;
    //adapter view click listener

    public AdapterNewsNestedCarousel(Context context, List<CardData> itemList) {
        mContext = context;
        mListMovies = itemList;
//        ScopedBus.getInstance().register(this);
    }
    public AdapterNewsNestedCarousel(Context context, List<CardData> itemList, RecyclerView recyclerView) {
        mContext = context;
        mListMovies = itemList;
        recyclerViewReference = recyclerView;
    }

    public void setBgColor(String mBgColor){
        this.mBgColor=mBgColor;
    }

/*
    public boolean isContainingDummies(){
        return mIsDummyData;
    }
*/

    private boolean isContinueWatchingSection;
    private OnItemRemovedListener mOnItemRemovedListener;

    private View.OnClickListener mOnItemRemoveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            removeItem(view);
        }
    };

    private void removeItem(View view) {
        LoggerD.debugLogAdapter("removeItem view data mParentPosition- " + mParentPosition + " getTag- " + view.getTag());
        try {
            if (view != null
                    && view.getTag() != null
                    && view.getTag() instanceof Integer) {
                int pos = (int) view.getTag();
                Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos)._id,mListMovies.get(pos).getType());
                mListMovies.remove(pos);
                if (recyclerViewReference != null) {
                    if (!recyclerViewReference.isComputingLayout()) {
                        notifyDataSetChanged();
                    } else {
                        recyclerViewReference.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
            if (mOnItemRemovedListener != null) {
                mOnItemRemovedListener.onItemRemoved(mParentPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void setData(final List<CardData> listMovies) {
        if (listMovies == null || recyclerViewReference == null) {
            return;
        }
        mListMovies = listMovies;
        recyclerViewReference.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!recyclerViewReference.isComputingLayout()) {
                        notifyDataSetChanged();
                    } else {
                        setData(listMovies);
                    }
                } catch (Exception e) {
                  //  Crashlytics.logException(e);
                }
            }
        });
    }

    public void addData(List<CardData> listMovies) {
        if (listMovies == null) {
            return;
        }

        //Log.d(TAG, "addData");
        if (mListMovies == null) {
            mListMovies = listMovies;
            if (recyclerViewReference != null) {
                if (!recyclerViewReference.isComputingLayout()) {
                    notifyDataSetChanged();
                } else {
                    recyclerViewReference.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }
            }
            return;
        }
        mListMovies.addAll(listMovies);
        if (recyclerViewReference != null) {
            if (!recyclerViewReference.isComputingLayout()) {
                notifyDataSetChanged();
            } else {
                recyclerViewReference.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }

    }

    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        CarouselDataViewHolder customItemViewHolder = null;
        view = LayoutInflater.from(mContext).inflate(R.layout.listitem_carousel_news, parent, false);
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int devicewidth = (displaymetrics.widthPixels) / 2;
        int margins =(int)Util.convertDpToPixel(19,mContext);
        devicewidth=devicewidth-margins;

       // holder.imageViewThumbNailLayout.getLayoutParams().width = devicewidth;
        holder.mImageViewMovies.getLayoutParams().width = devicewidth;
        holder.mLayoutContainer.getLayoutParams().width = devicewidth;

        holder.mainLayout.getLayoutParams().width=devicewidth;

        //Log.d(TAG, "onBindViewHolder" + position);
        bindGenreViewHolder(holder, mListMovies.get(position));
    }


    private void bindGenreViewHolder(final CarouselDataViewHolder holder, final CardData carouselData) {

//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {

        if (carouselData != null) {

            holder.mTextViewVODInfo1.setVisibility(View.GONE);
            holder.mTextViewVODInfo2.setVisibility(View.GONE);
            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);

            holder.mThumbnailDelete.setVisibility(View.GONE);
            holder.mContinueWatchingProgress.setVisibility(View.GONE);
            if (carouselData.generalInfo != null) {
                if(APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)
                        && carouselData.publishingHouse != null
                        && APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(carouselData.publishingHouse.publishingHouseName)){
                    holder.mImageViewProvideLogo.setVisibility(View.GONE);
//                    Picasso.with(mContext).load(APIConstants.getErosNowMusicLogoUrl()).into(holder.mImageViewProvideLogo);
                    if (!TextUtils.isEmpty(carouselData.globalServiceName)) {
                        holder.mTextViewVODInfo1.setVisibility(View.VISIBLE);
                        holder.mTextViewVODInfo1.setText(carouselData.globalServiceName);
                    }
                } else {
                    holder.mImageViewProvideLogo.setVisibility(View.GONE);
                }

                if ((APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                        || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type))) {
                    holder.mTextViewMovieTitle.setText(carouselData.generalInfo.briefDescription);
                } else {
                    holder.mTextViewMovieTitle.setText(carouselData.generalInfo.title);
                }

                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                        && !TextUtils.isEmpty(carouselData.globalServiceName)) {
                    holder.mTextViewMovieTitle.setText(carouselData.globalServiceName);
                }

                if(mBgColor!=null&&!TextUtils.isEmpty(mBgColor)){
                    holder.mTextViewMovieTitle.setTextColor(mContext.getResources().getColor(R.color.medium_small_content_light_theme_text_color));
                    holder.mTextViewVODInfo1.setTextColor(mContext.getResources().getColor(R.color.medium_small_content_light_theme_sub_title_text_color));
                    holder.mTextViewVODInfo2.setTextColor(mContext.getResources().getColor(R.color.medium_small_content_light_theme_sub_title_text_color));
                }
            }
            if (isContinueWatchingSection) {
                holder.mThumbnailDelete.setVisibility(View.VISIBLE);
                holder.mThumbnailDelete.setTag(mListMovies.indexOf(carouselData));
                holder.mThumbnailDelete.setOnClickListener(mOnItemRemoveClickListener);
                holder.mContinueWatchingProgress.setVisibility(View.GONE);
                if (carouselData != null && carouselData.elapsedTime > 0) {
                    holder.mImageViewContinueWatching.setVisibility(View.VISIBLE);
                    holder.mImageViewContinueWatching.setImageResource(R.drawable.thumbnail_play_icon);
                    LoggerD.debugLog("updatePlayerStatus: " +
                            "carouselData.elapsedTime: " + carouselData.elapsedTime +
                            "id- " + carouselData.generalInfo.title);
                    try {
                        int position = carouselData.elapsedTime;
                        int duration = Util.calculateDurationInSeconds(carouselData.content.duration);
                        int percent = 0;
                        if (duration > 0) {
                            // use long to avoid overflow
                            percent = (int) (100L * position / duration);
                        }
                        LoggerD.debugLog("updatePlayerStatus duration percent- " + percent);
//                        S2 | E1 | Pal Pal Dil Ke Paas
                        int watchedMinutes = position / 60;
                        int remainingduration = ((duration / 60) - watchedMinutes);
                        LoggerD.debugLog("remainingDuration in minutes- " + remainingduration + " mins to go");
                        holder.mContinueWatchingProgress.setVisibility(View.VISIBLE);
                        holder.mContinueWatchingProgress.setProgress(percent);
                        if (carouselData.isMovie()) {
                            if (remainingduration > 0)
                                holder.mTextViewMovieTitle.setText(remainingduration + " mins to go");
                            else
                                holder.mTextViewMovieTitle.setText(carouselData.getTitle());
                        } else {
                            String title = carouselData.getTitle();
                            holder.mTextViewMovieTitle.setText(title);
                            if (carouselData.isTVEpisode()) {
                                try {
                                    String[] splitText = title.split(" " + Pattern.quote("|") + " ");
                                    for (String token :
                                            splitText) {
                                        LoggerD.debugDownload("\t token- " + token);
                                    }
                                    if (splitText.length >= 2) {
                                        LoggerD.debugDownload("splitText- " + splitText + "\n1. " + splitText[0] + " \n2. " + splitText[1]);
                                        holder.mTextViewVODInfo1.setText(splitText[0] + " | " + splitText[1]);
                                        holder.mTextViewVODInfo1.setVisibility(View.VISIBLE);
                                    }
                                    if (!TextUtils.isEmpty(carouselData.globalServiceName)) {
                                        holder.mTextViewMovieTitle.setText(carouselData.globalServiceName);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LoggerD.debugDownload("\t exception message- " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            String imageLink = getImageLink(carouselData);
            /*if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                holder.mImageViewMovies.setImageResource(R.drawable
                        .epg_thumbnail_default);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.epg_thumbnail_default);
            }*/

            int placeholderid = R.drawable.black;
            int width = (ApplicationController.getApplicationConfig().screenWidth/2);
            int height;
            if (DeviceUtils.isTablet(mContext)) {
                 height =(int)Util.convertDpToPixel(100,mContext);
            }else {
                 height =(int)Util.convertDpToPixel(72,mContext);
            }
             height = (int) (width / 2.6);
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
            }

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

            if (carouselData != null&&carouselData.generalInfo!=null) {
                showViewRatingOnThumbnail(holder,carouselData);
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
            /*if (carouselData != null
                    && carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_VOD.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_TRAILER.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(carouselData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type))) {
                holder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
            }*/
        }

        holder.setClickListener(mOnItemClickListenerWithData);
        if (mOnItemClickListenerWithData == null) {
            holder.setClickListener(mOnItemClickListenerDefault);
        }

        holder.mTextViewMovieTitle.setVisibility(View.GONE);
        if (showTitle) {
            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
        }

//            }
//        });
        /*String partnerImageLink=carouselData.getPartnerImageLink(mContext);
        SDKLogger.debug("partnerImage "+partnerImageLink);*/
        /*if (!TextUtils.isEmpty(partnerImageLink)){
            holder.mImageViewPartner.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(partnerImageLink).resize(holder.mImageViewPartner.getLayoutParams().width,holder.mImageViewPartner.getLayoutParams().height).placeholder(R.drawable.epg_thumbnail_default).centerInside().into(holder.mImageViewPartner);
        }else{
            holder.mImageViewPartner.setVisibility(View.GONE);
        }*/
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        if (mListMovies == null) return 0;

        return mListMovies.size();
    }

    private static final int TYPE_ITEM = 1;

    @Override
    public int getItemViewType(int position) {

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

    public void setParentPosition(int position) {
        this.mParentPosition = position;
    }

    public void setParentViewHolder(GenericListViewCompoment holder) {
        this.parentViewHolder = holder;
    }
    private boolean showTitle = true;
    public void showTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }


    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    public class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //        private final ImageView mImageViewRupeeIcon;
        private ItemClickListenerWithData clickListener;
        final ImageView mImageViewPlayIcon;
        final ImageView mImageViewProvideLogo;
        final ImageView mImageViewMovies;
        final TextView mTextViewMovieTitle;
        final TextView mTextViewVODInfo1;
        final TextView mTextViewVODInfo2;
        final ImageView mImageViewContinueWatching;

        public ImageView mThumbnailDelete;
        public ProgressBar mContinueWatchingProgress;
        public ImageView mImageViewPartner;

        private ImageView thumbnailRatingIcon;
        private ImageView thumbnailViewsIcon;
        private TextView ratingCountText;
        private TextView viewsCountText;
        private RelativeLayout viewRatingParentLayout;
        private RelativeLayout imageViewThumbNailLayout;
        private MaterialCardView mLayoutContainer;

        private RelativeLayout mainLayout;

        public CarouselDataViewHolder(View view) {
            super(view);
//            UiUtil.showFeedback(view, true, R.color.list_item_bkg);
            mTextViewMovieTitle = (TextView) view.findViewById(R.id.textview_title_show);
            mImageViewMovies = (ImageView) view.findViewById(R.id.imageview_thumbnail_voditem);
            mImageViewPlayIcon = (ImageView)view.findViewById(R.id.thumbnail_movie_play);
            mTextViewVODInfo1 = (TextView) view.findViewById(R.id.vod_info1);
            mTextViewVODInfo2 = (TextView) view.findViewById(R.id.vod_info2);
            mImageViewProvideLogo = (ImageView)view.findViewById(R.id.thumbnail_provider_app);
//            mImageViewRupeeIcon = (ImageView)view.findViewById(R.id.thumbnail_rupee_icon);
            mThumbnailDelete = (ImageView)view.findViewById(R.id.thumbnail_movie_delete_icon);
            mContinueWatchingProgress = (ProgressBar) view.findViewById(R.id.continue_watching_progress);
            mImageViewContinueWatching = (ImageView) view.findViewById(R.id.thumbnail_movie_play_continue_watching);
            mImageViewPartner = (ImageView) view.findViewById(R.id.iv_partener_logo_right);
            ratingCountText = view.findViewById(R.id.rating_count_text);
            thumbnailRatingIcon = view.findViewById(R.id.rating_icon);
            viewsCountText = view.findViewById(R.id.views_count_text);
            thumbnailViewsIcon = view.findViewById(R.id.views_icon);
            viewRatingParentLayout = view.findViewById(R.id.view_rating_parent);
            mLayoutContainer=view.findViewById(R.id.layout_container);
            mainLayout=view.findViewById(R.id.list_item_carousel_news_main_layout);
            //imageViewThumbNailLayout=view.findViewById(R.id.imageview_thumbnail_layout);
//
            view.setOnClickListener(this);
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListMovies == null || mListMovies.isEmpty()) return false;
                    CardData data = null;
                    if (getAdapterPosition() != -1) {
                        data = mListMovies.get(getAdapterPosition());
                    }
                    if (data == null) {
                        return false;
                    }
                    String title = data.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        return false;
                    }
                    AlertDialogUtil.showToastNotification(title);
                    return false;
                }
            });
            if(mImageViewMovies!=null){
                mImageViewMovies.setOnClickListener(this);
            }
            if(mTextViewMovieTitle!=null){
                mTextViewMovieTitle.setOnClickListener(this);
            }
            if(mImageViewPlayIcon!=null){
                mImageViewPlayIcon.setOnClickListener(this);
            }
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

    public void setCarouselInfoData(String name, int pageSize) {
        if(!TextUtils.isEmpty(mPageName)){
            return;
        }
        this.mPageName = name;
        this.mPageSize = pageSize;
//        new MenuDataModel().fetchCarouseldataInAsynTask(mContext, mPageName, 1, mPageSize, true,this);
    }

    private void addCarouselData(final List<CardData> carouselList) {
        LoggerD.debugLog(TAG + " addCarouselData: mPageName- " + mPageName);
        if (carouselList == null) {
            return;
        }
        try {

            mListMovies = carouselList;
//            mIsDummyData = false;
            if (recyclerViewReference != null) {
                if (!recyclerViewReference.isComputingLayout()) {
                    notifyDataSetChanged();
                } else {
                    recyclerViewReference.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }
            }
//            CarouselInfoData carouselInfoData = mListCarouselInfo.get(position);
//            carouselInfoData.listCarouselData = carouselList;
//            notifyItemChanged(position);
        } catch (IllegalStateException e) {
            //Occurs while we try to modify data of recycler view while it is scrolling
            /*mRecyclerViewCarouselInfo.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position);
                }
            });*/
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final ItemClickListenerWithData mOnItemClickListenerDefault = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

            Analytics.gaEventBrowsedCategoryContentType(carouselData);

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

            if(position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA){
                return;
            }

            String publishingHouse = carouselData == null
                    || carouselData.publishingHouse == null
                    || TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName) ? null : carouselData.publishingHouse.publishingHouseName;

            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                HungamaPartnerHandler.launchDetailsPage(carouselData, mContext, null, null);
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
            showDetailsFragment(carouselData);

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

    private void showRelatedVODListFragment(CardData cardData) {
        Bundle args = new Bundle();
        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));
    }

    private void showDetailsFragment(CardData carouselData) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        String _id = carouselData._id;
        if (carouselData.generalInfo != null
                && APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                && carouselData.globalServiceId != null) {
            _id = carouselData.globalServiceId;
        }

        args.putString(CardDetails.PARAM_CARD_ID, _id);

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
                && !isContinueWatchingSection
                && !carouselData.isTVSeries()
                && !carouselData.isVODChannel()
                && !carouselData.isVODYoutubeChannel()
                && !carouselData.isVODCategory()
                && !carouselData.isTVSeason()) {
            CacheManager.setCardDataList(mListMovies);
        }


        if (carouselData.generalInfo != null
                && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type))) {
            args.putBoolean(CardDetails.PARAM_RESET_EPG_DATE_POSITION_IN_DETAIL, true);
        }

        String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_SIMILAR_CONTENT);

        ((BaseActivity) mContext).showDetailsFragment(args,carouselData);
    }


    public final String getImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null ||
                carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_NEWS_NESTED_IMAGE};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }

    public void showViewRatingOnThumbnail(AdapterNewsNestedCarousel.CarouselDataViewHolder holder, CardData cardData){
        if(cardData.stats != null && cardData.generalInfo.displayStatistics&&!cardData.isYoutube()){
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
        }
    }
}