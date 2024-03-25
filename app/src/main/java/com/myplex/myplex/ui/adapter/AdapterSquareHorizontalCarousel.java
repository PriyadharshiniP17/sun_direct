package com.myplex.myplex.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.PartnerDetailsResponse;
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
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;

import java.util.Date;
import java.util.List;


public class AdapterSquareHorizontalCarousel extends RecyclerView.Adapter<AdapterSquareHorizontalCarousel.CarouselDataViewHolder> {

    private static final String TAG = AdapterSquareHorizontalCarousel.class.getSimpleName();

    private final Context mContext;
    private List<CardData> mListMovies;
//    private boolean mIsDummyData = false;
    private int mParentPosition;
    private String mPageName;
    private int mPageSize;
    private ProgressDialog mProgressDialog;
    private GenericListViewCompoment parentViewHolder;
    private RecyclerView recyclerViewReference;
    //adapter view click listener

    public AdapterSquareHorizontalCarousel(Context context, List<CardData> itemList,RecyclerView recyclerView) {
        mContext = context;
        mListMovies = itemList;
        recyclerViewReference = recyclerView;
//        ScopedBus.getInstance().register(this);
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
                notifyDataSetChanged();
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
            if (!recyclerViewReference.isComputingLayout()) {
                notifyDataSetChanged();
            } else {
                setData(listMovies);
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
            notifyDataSetChanged();
            return;
        }
        mListMovies.addAll(listMovies);
        notifyDataSetChanged();

    }

    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        CarouselDataViewHolder customItemViewHolder = null;
        view = LayoutInflater.from(mContext).inflate(R.layout.listitem_small_square_horizontal_item, parent, false);
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder" + position);
        bindGenreViewHolder(holder, mListMovies.get(position));
    }


    private void bindGenreViewHolder(final CarouselDataViewHolder holder, final CardData carouselData) {

//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {

        if (carouselData != null) {

//            String imageLink = carouselData.getImageLink(APIConstants.IMAGE_TYPE_THUMBNAIL);
            String channelImageLink  = getChannelImageLink(carouselData);
            SDKLogger.debug("channelImageLink "+channelImageLink);
            /*if (imageLink == null
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                holder.mImageViewMovies.setImageResource(R.drawable
                        .placeholder_live_channel);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.placeholder_live_channel);
            }*/
        /*    if(!TextUtils.isEmpty(channelImageLink)  ){
                channelImageLink = channelImageLink.replace("epgimages/", "epgimagesV3/");
                PicassoUtil.with(mContext).load(channelImageLink, holder.mImageViewMovies, R.drawable.placeholder_live_channel);
            }

            String partnerImageLink = carouselData.getPartnerImageLink(mContext);
            SDKLogger.debug("partnerImage " + partnerImageLink);
            if (!TextUtils.isEmpty(partnerImageLink)) {
                holder.mImageViewProvideLogo.setVisibility(View.VISIBLE);
                //Picasso.with(mContext).load(partnerImageLink).placeholder(R.drawable.epg_thumbnail_default).resize(holder.mImageViewProvideLogo.getLayoutParams().width, holder.mImageViewProvideLogo.getLayoutParams().height).centerInside().into(holder.mImageViewProvideLogo);
                PicassoUtil.with(mContext).loadCenterInside(partnerImageLink,holder.mImageViewProvideLogo,R.drawable.epg_thumbnail_default,holder.mImageViewProvideLogo.getLayoutParams().width, holder.mImageViewProvideLogo.getLayoutParams().height);
            }else{
                holder.mImageViewProvideLogo.setVisibility(View.GONE);
            }*/



            if (carouselData.generalInfo != null && carouselData.generalInfo.title != null && !TextUtils.isEmpty(carouselData.generalInfo.title)) {
                holder.mTextViewMovieTitle.setText(carouselData.generalInfo.title);
            }
            String imageLink = carouselData.getImageLink(APIConstants.IMAGE_TYPE_COVERPOSTER);
            if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                holder.mImageViewMovies.setImageResource(R.drawable
                        .black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.black);
            }

        }

        holder.setClickListener(mOnItemClickListenerWithData);
        if (mOnItemClickListenerWithData == null) {
            holder.setClickListener(mOnItemClickListenerDefault);
        }

    }

    private String getPartnerImageLink(CardData carouselData) {
        if (mContext != null) {
            PartnerDetailsResponse partnerDetailsResponse = PropertiesHandler.getPartnerDetailsResponse(mContext);
            String partnerName=(carouselData.publishingHouse!=null&&!TextUtils.isEmpty(carouselData.publishingHouse.publishingHouseName))?carouselData.publishingHouse.publishingHouseName:carouselData.contentProvider;
            if (partnerName!=null&&partnerDetailsResponse != null && carouselData != null) {
                for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
                    if (partnerDetailsResponse != null
                            && partnerDetailsResponse.partnerDetails != null
                            && partnerDetailsResponse.partnerDetails.get(i) != null
                            && !TextUtils.isEmpty(partnerDetailsResponse.partnerDetails.get(i).name)
                            && partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
                        return partnerDetailsResponse.partnerDetails.get(i).imageURL;
                    }
                }
            }
        }
        return null;
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
    class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListenerWithData clickListener;
        final ImageView mImageViewProvideLogo;
        final ImageView mImageViewMovies;
        private TextView mTextViewMovieTitle;

        public CarouselDataViewHolder(View view) {
            super(view);
//            UiUtil.showFeedback(view, true, R.color.list_item_bkg);

            mImageViewMovies = (ImageView) view.findViewById(R.id.imageview_thumbnail_voditem);
            mImageViewProvideLogo = (ImageView)view.findViewById(R.id.iv_partener_logo_right);
            mTextViewMovieTitle = (TextView)view.findViewById(R.id.channel_title);

            view.setOnClickListener(this);
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
            notifyDataSetChanged();
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
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }

    public final String getChannelImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL,APIConstants.IMAGE_TYPE_THUMBNAIL};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }
}