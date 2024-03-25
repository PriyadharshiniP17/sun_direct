package com.myplex.myplex.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
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
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;

import java.util.Date;
import java.util.List;


public class AdapterBannerNewHorizontalCarousel extends RecyclerView.Adapter<AdapterBannerNewHorizontalCarousel.CarouselDataViewHolder>{

    private static final String TAG = AdapterBannerNewHorizontalCarousel.class.getSimpleName();

    private final Context mContext;
    private List<CardData> mListMovies;
    private String mPageName;
    private int mPageSize;
    private ProgressDialog mProgressDialog;
    private boolean isContinueWatchingSection;
    private OnItemRemovedListener mOnItemRemovedListener;
    private RecyclerView recyclerViewReference;


    private View.OnClickListener mOnItemRemoveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            removeItem(view);
        }
    };
    private boolean showTitle;

    private void removeItem(View view) {
        LoggerD.debugLogAdapter("removeItem view data mParentPosition- " + mParentPosition + " getTag- " + view.getTag());
        try {
            if (view != null
                    && view.getTag() != null
                    && view.getTag() instanceof Integer) {
                int pos = (int) view.getTag();
                Util.updatePlayerStatus(0, Analytics.ACTION_TYPES.delete.name(), mListMovies.get(pos)._id,mListMovies.get(pos).getType());
                mListMovies.remove(pos);
                notifyDataSetChanged();
                if (mOnItemRemovedListener != null) {
                    mOnItemRemovedListener.onItemRemoved(mParentPosition);
                }
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

    public AdapterBannerNewHorizontalCarousel(Context context, List<CardData> itemList, RecyclerView recyclerView) {
        mContext = context;
        mListMovies = itemList;
        recyclerViewReference = recyclerView;

    }


    public AdapterBannerNewHorizontalCarousel(Context context, List<CardData> itemList, boolean isDummyData) {
        mContext = context;
        mListMovies = itemList;
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
        view = LayoutInflater.from(mContext).inflate(R.layout.listitem_banner_new_recycler, parent, false);
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        //Log.d(TAG,"onBindViewHolder" + position);
        bindGenreViewHolder(holder, mListMovies.get(position));
    }


    private void bindGenreViewHolder(final CarouselDataViewHolder holder, final CardData carouselData) {
                if (carouselData != null) {

                    int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                    int displayHeight = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight();
                    holder.thumbnailMovieLayout.getLayoutParams().width = displayWidth -  (int)mContext.getResources().getDimension(R.dimen._45sdp);
                    holder.thumbnailMovieLayout.getLayoutParams().height = displayHeight / 4 + (int)mContext.getResources().getDimension(R.dimen._30sdp);
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
                    if (isContinueWatchingSection) {
                        holder.mThumbnailDelete.setVisibility(View.VISIBLE);
                        holder.mThumbnailDelete.setTag(mListMovies.indexOf(carouselData));
                        holder.mThumbnailDelete.setOnClickListener(mOnItemRemoveClickListener);
                        holder.mContinueWatchingProgress.setVisibility(View.GONE);
                        if(carouselData != null && carouselData.elapsedTime > 0){
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
                                holder.mImageViewMovies.setImageResource(R.drawable
                                        .movie_thumbnail_placeholder);
                            } else {
                                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.movie_thumbnail_placeholder);
                            }

                }
                try {

                    if (carouselData != null && carouselData.publishingHouse != null) {

                        if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(carouselData.publishingHouse.publishingHouseName)) {
                        } else if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(carouselData.publishingHouse.publishingHouseName)) {

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
                                holder.mImageViewRentBand.setVisibility(View.VISIBLE);
                            } else {
                                holder.mImageViewRentBand.setVisibility(View.GONE);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

        if (carouselData != null&&carouselData.generalInfo!=null) {
            showViewRatingOnThumbnail(holder,carouselData);
        }

        if(carouselData == null){
            holder.mImageViewPlayIcon.setVisibility(View.GONE);
        } else {
            holder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
            if(!Util.isFreeContent(carouselData)){
                holder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_pay_icon);
            } else {
                holder.mImageViewPlayIcon.setImageResource(R.drawable.banner_play_icon);
            }
        }
        holder.rentLayout.setVisibility(View.GONE);
      /*  if(carouselData != null && carouselData.generalInfo !=null && carouselData.generalInfo.contentRights != null && carouselData.generalInfo.contentRights.size()>0 && carouselData.generalInfo.contentRights.get(0)!= null) {
            if (carouselData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                holder.rentLayout.setVisibility(View.VISIBLE);
            }
        }*/
        holder.setClickListener(mOnItemClickListenerWithData);
        if (mOnItemClickListenerWithData == null) {
            holder.setClickListener(mOnItemClickListenerDefault);
        }
        holder.mTextViewMovieTitle.setText(carouselData.getTitle());
        holder.mTextViewMovieTitle.setVisibility(View.GONE);
        if (showTitle) {
            holder.mTextViewMovieTitle.setVisibility(View.VISIBLE);
        }

        String partnerImageLink = carouselData.getPartnerImageLink(mContext);
        SDKLogger.debug("partnerImage " + partnerImageLink);
        if (!TextUtils.isEmpty(partnerImageLink)) {
            holder.mImageViewPartnerLogo.setVisibility(View.VISIBLE);
            //Picasso.with(mContext).load(partnerImageLink).placeholder(R.drawable.epg_thumbnail_default).resize(holder.mImageViewPartnerLogo.getLayoutParams().width, holder.mImageViewPartnerLogo.getLayoutParams().height).centerInside().into(holder.mImageViewPartnerLogo);
            PicassoUtil.with(mContext).loadCenterInside(partnerImageLink,holder.mImageViewPartnerLogo,R.drawable.black,holder.mImageViewPartnerLogo.getLayoutParams().width,
                    holder.mImageViewPartnerLogo.getLayoutParams().height);
        }else{
            holder.mImageViewPartnerLogo.setVisibility(View.GONE);
        }
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        if(mListMovies == null) return 0;

        return mListMovies.size();
    }

    private static final int TYPE_ITEM = 1;
    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
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
    class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListenerWithData clickListener;

        final ImageView mImageViewMovies;
        final ImageView mImageViewPlayIcon;
        final ImageView mImageViewInstallIcon;
        final ImageView mImageViewProvideLogo;
        final TextView mTextViewMovieTitle;
        final ImageView mImageViewRentBand;
        public ImageView mThumbnailDelete;
        public ProgressBar mContinueWatchingProgress;
        final ImageView mImageViewPartnerLogo;
        private ImageView thumbnailRatingIcon;
        private ImageView thumbnailViewsIcon;
        private TextView ratingCountText;
        private TextView viewsCountText;
        private RelativeLayout viewRatingParentLayout;
        private RelativeLayout thumbnailMovieLayout;
        private LinearLayout rentLayout;

        public CarouselDataViewHolder(View view) {
            super(view);
            mTextViewMovieTitle = (TextView)view.findViewById(R.id.textview_movies_title);
            mImageViewMovies = (ImageView)view.findViewById(R.id.thumbnail_movie);
            mImageViewPlayIcon = (ImageView)view.findViewById(R.id.thumbnail_movie_play);
            mImageViewInstallIcon = (ImageView)view.findViewById(R.id.thumbnail_provider_app_install);
            mImageViewProvideLogo = (ImageView)view.findViewById(R.id.thumbnail_provider_app);
            mImageViewRentBand = (ImageView)view.findViewById(R.id.thumbnail_rent_band);
            mImageViewPartnerLogo = (ImageView)view.findViewById(R.id.iv_partener_logo_right);
            mThumbnailDelete = (ImageView)view.findViewById(R.id.thumbnail_movie_delete_icon);
            mContinueWatchingProgress = (ProgressBar) view.findViewById(R.id.continue_watching_progress);
//            mImageViewRupeeIcon = (ImageView)view.findViewById(R.id.thumbnail_rupee_icon);
            ratingCountText = view.findViewById(R.id.rating_count_text);
            thumbnailRatingIcon = view.findViewById(R.id.rating_icon);
            viewsCountText = view.findViewById(R.id.views_count_text);
            thumbnailViewsIcon = view.findViewById(R.id.views_icon);
            viewRatingParentLayout = view.findViewById(R.id.view_rating_parent);
            thumbnailMovieLayout = view.findViewById(R.id.thumbnail_movie_layout);
            rentLayout = view.findViewById(R.id.iv_rent);
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListMovies == null || mListMovies.isEmpty()) return false;
                    CardData data = mListMovies.get(getAdapterPosition());
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
            view.setOnClickListener(this);

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
            if (this.clickListener != null && mListMovies != null) {
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
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER, APIConstants.IMAGE_TYPE_COVERPOSTER};

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

    public final ItemClickListenerWithData mOnItemClickListenerDefault = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            //movie item clicked we can have movie data here

            if (carouselData == null || carouselData._id == null) return;

            Analytics.gaEventBrowsedCategoryContentType(carouselData);

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
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_SIMILAR_CONTENT);
        if (carouselData.generalInfo != null
                && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type))) {
            args.putBoolean(CardDetails.PARAM_RESET_EPG_DATE_POSITION_IN_DETAIL, true);
        }
        ((BaseActivity) mContext).showDetailsFragment(args,carouselData);
    }

    public void setCarouselInfoData(String name, int pageSize) {
        if(!TextUtils.isEmpty(mPageName)){
            return;
        }
        this.mPageName = name;
        this.mPageSize = pageSize;
    }


    private void addCarouselData(final List<CardData> carouselList) {
        LoggerD.debugLog(TAG + " addCarouselData: mPageName- " + mPageName);
        try {
            if (carouselList == null) {

                return;
            } else {
                mListMovies = carouselList;
            }
            notifyDataSetChanged();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showViewRatingOnThumbnail(AdapterBannerNewHorizontalCarousel.CarouselDataViewHolder holder, CardData cardData){
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