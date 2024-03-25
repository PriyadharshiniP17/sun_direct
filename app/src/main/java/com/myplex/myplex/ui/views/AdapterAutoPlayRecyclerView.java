package com.myplex.myplex.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.FavouriteRequest;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.FavouriteResponse;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.PortraitViewPagerImageSliderAdapter;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.util.List;

public class AdapterAutoPlayRecyclerView extends RecyclerView.Adapter<AdapterAutoPlayRecyclerView.CarouselDataViewHolder> {

    public static final int PAGE_COUNT_FACTOR = 100000;
    /*private VmaxAdView mVmaxAdView;*/
    protected static final String TAG = PortraitViewPagerImageSliderAdapter.class.getSimpleName();
    private Context mContext;
    private List<CardData> mItems;

    public String getTabName() {
        return tabName;
    }
    private String tabName;
    private AdapterAutoPlayRecyclerView.OnItemClickListener mOnItemClickListener;
    private int size;
    private boolean isInfiniteLoop = false;
    private String layoutType;
    private boolean isTitleVisible = true;
    private int carouselPosition;

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setCarouselPosition(int position) {
        this.carouselPosition = position;
    }

    void setTitleVisibility(boolean showTitle) {
        isTitleVisible = showTitle;
    }

    public interface OnItemClickListener {
        void onItemClicked(CardData cardData);
    }

    AdapterAutoPlayRecyclerView(Context context) {
        this.mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        SDKLogger.debug("constructor");
    }
    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        CarouselDataViewHolder customItemViewHolder;
        view = LayoutInflater.from(mContext).inflate(R.layout.view_portrait_ottapp_slider_player, parent, false);
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }


    public void setItems(List<CardData> mItems,String layoutType) {
        this.mItems = mItems;
        size = mItems.size();
        this.layoutType = layoutType;
    }

    @Override
    public void onBindViewHolder(final CarouselDataViewHolder mViewHolder,int pos) {
        Log.e("Target BindViewHolder","called");
        final int position = pos % mItems.size();
        final CardData sliderModel = mItems.get(position);
        mViewHolder.imageView.setTag(sliderModel);
        mViewHolder.videoSurfaceView.setTag(sliderModel);
        String imageLink = null;
        imageLink = getImageLink(sliderModel);
        if (TextUtils.isEmpty(imageLink)
                || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            mViewHolder.imageView.setImageResource(R.drawable
                    .movie_thumbnail_placeholder);
            Log.e("Image link : ","Null");
        } else if (imageLink != null) {
            if (mContext.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE) {
                PicassoUtil.with(mContext).load(imageLink, mViewHolder.imageView, R.drawable.movie_thumbnail_placeholder);
                Log.e("Image link : ",imageLink);
            } else {
                PicassoUtil.with(mContext).load(imageLink, mViewHolder.imageView, R.drawable.movie_thumbnail_placeholder);
            }
        }
        if(isTitleVisible){
            if(sliderModel != null && sliderModel.generalInfo != null && !TextUtils.isEmpty(sliderModel.generalInfo.title)){
                mViewHolder.titleText.setText(sliderModel.generalInfo.title);
            }else {
                mViewHolder.titleText.setVisibility(View.GONE);
            }
            if (sliderModel.content != null && sliderModel.content.genre.size() > 0) {
                mViewHolder.subTitleText.setText(sliderModel.getGenre());
            }else {
                mViewHolder.subTitleText.setVisibility(View.GONE);
            }
            mViewHolder.textViewTitle.setVisibility(View.GONE);
            mViewHolder.textViewTitle.setText((sliderModel != null && sliderModel.generalInfo != null && !TextUtils.isEmpty(sliderModel.generalInfo.title)) ? sliderModel.generalInfo.title : "");
        }else{
            mViewHolder.subTitleText.setVisibility(View.GONE);
            mViewHolder.titleText.setVisibility(View.GONE);
            mViewHolder.textViewTitle.setVisibility(View.GONE);
        }
        if (mViewHolder.iv_watchlist_icon != null) {
            if(PrefUtils.getInstance().getWatchlistItemsFromServer() && !PrefUtils.getInstance().getBoolean(APIConstants.UPDATE_PORTRAIT_BANNER,false)) {
                if (sliderModel != null && sliderModel.currentUserData != null && sliderModel.currentUserData.favorite) {
                    mViewHolder.iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon_added);
                } else {
                    mViewHolder.iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon);
                }
            }else{
                if(sliderModel != null && sliderModel.isFavourite){
                    mViewHolder.iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon_added);
                }else{
                    mViewHolder.iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon);
                }
            }

        }


        if (mViewHolder.rlWatchList != null) {
            mViewHolder.rlWatchList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewHolder.rlWatchList.setEnabled(false);
                    mViewHolder.rlWatchList.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mViewHolder.rlWatchList != null)
                                mViewHolder.rlWatchList.setEnabled(true);
                        }
                    }, 2000);

                    //Util.showFeedback(rlWatchList);
                    String type = sliderModel.generalInfo.type;
                    String _id = sliderModel._id;
                    if (sliderModel != null
                            && (sliderModel.isVODYoutubeChannel()
                            || sliderModel.isTVSeason()
                            || sliderModel.isVODChannel()
                            || sliderModel.isVODCategory()
                            || sliderModel.isTVSeries())) {
                        type = sliderModel.generalInfo.type;
                        _id = sliderModel._id;
                    }
                    if (sliderModel.isProgram()) {
                        _id = sliderModel.globalServiceId;
                        type = APIConstants.TYPE_LIVE;
                    }
                    if (!Util.checkUserLoginStatus()) {
                        ((MainActivity) mContext).initLogin(Analytics.VALUE_SOURCE_CAROUSEL, (mContext != null && ((MainActivity) mContext).carouselInfoData != null) ? ((MainActivity) mContext).carouselInfoData.title : sliderModel.getTitle());
                        ((MainActivity) mContext).isFavoriteRequestFromPortraitBanner(true,_id,type);
                        return;
                    }
                    FavouriteRequest.Params favouritesParams = new FavouriteRequest.Params(_id,type);

                    FavouriteRequest mRequestFavourites = new FavouriteRequest(favouritesParams,
                            new APICallback<FavouriteResponse>() {
                                @Override
                                public void onResponse(APIResponse<FavouriteResponse> response) {
                                    if (response == null
                                            || response.body() == null) {
                                        return;
                                    }
                                    if (response.body().code == 402) {
                                        PrefUtils.getInstance().setPrefLoginStatus("");
                                        return;
                                    }

                                    //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                                    if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                                        //showFavaouriteButton(true);
                                        if (sliderModel != null
                                                && sliderModel.currentUserData != null) {
                                            sliderModel.currentUserData.favorite = response.body().favorite;
                                            sliderModel.isFavourite = true;
                                            PrefUtils.getInstance().shouldChangeFavouriteState(sliderModel._id,true,tabName);
                                        }

                                        if (response.body().favorite) {
                                            mViewHolder.iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon_added);
                                            AlertDialogUtil.showToastNotification("Added to Watchlist");
                                            fireCleverTapEvent(CleverTap.PROPERTY_ADD_TO_WATCHLIST,sliderModel);
                                            if(sliderModel != null && sliderModel._id != null && !TextUtils.isEmpty(sliderModel._id)) {
                                                CleverTap.eventPromoVideoAddedToWatchList(APIConstants.NOT_AVAILABLE, sliderModel._id, CleverTap.SOURCE_BANNER);
                                            }

                                        } else {
                                            AlertDialogUtil.showToastNotification("Removed from Watchlist");
                                            mViewHolder.iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon);
                                            sliderModel.isFavourite = false;
                                            PrefUtils.getInstance().shouldChangeFavouriteState(sliderModel._id,false,tabName);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t, int errorCode) {
                                    //Log.d(TAG, "FavouriteRequest: onResponse: t- " + t);
                                    if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                        AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                                        return;
                                    }
                                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                                }
                            });
                    APIService.getInstance().execute(mRequestFavourites);
                }
            });
        }

        String partnerImage = sliderModel.getPartnerImageLink(mContext);
        if(!TextUtils.isEmpty(partnerImage)){
            PicassoUtil.with(mContext).load(partnerImage, mViewHolder.imageViewParnter, R.drawable.movie_thumbnail_placeholder);
        }else{
            Log.e("Image link : ","Transparent");
            mViewHolder.imageViewParnter.setImageResource(R.drawable.transparent);
        }
        if (mViewHolder.imageViewPlayIcon != null) {
            mViewHolder.imageViewPlayIcon.setVisibility(View.GONE);
        }

        if(sliderModel.generalInfo != null && mViewHolder.userBadgeImage != null){
            if(!TextUtils.isEmpty(sliderModel.generalInfo.accessLabelImage) && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE){
                mViewHolder.userBadgeImage.setVisibility(View.VISIBLE);
                PicassoUtil.with(mContext).load(sliderModel.generalInfo.accessLabelImage,mViewHolder.userBadgeImage);
            }else {
                mViewHolder.userBadgeImage.setVisibility(View.GONE);
            }
        }
        /*if (sliderModel != null
                && sliderModel.generalInfo != null
                && sliderModel.generalInfo.type != null
                && (APIConstants.TYPE_MOVIE.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_TRAILER.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(sliderModel.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(sliderModel.generalInfo.type))) {
            imageViewPlayIcon.setVisibility(View.VISIBLE);
        }*/
       /* if(sliderModel.ottApp != null && sliderModel.ottApp.offerDescription != null){
            textView.setText(sliderModel.ottApp.offerDescription);
        }
*/
        mViewHolder.imageView.setVisibility(View.VISIBLE);
       /* mViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                if (mOnItemClickListener != null) {
                    CardData data = null;
                    if (view.getTag() instanceof CardData) {
                        data = (CardData) view.getTag();
                    }

                    mOnItemClickListener.onItemClicked(data);
                }
            }
        });*/
        mViewHolder.videoSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    CardData data = null;
                    if (view.getTag() instanceof CardData) {
                        data = (CardData) view.getTag();
                    }

                    mOnItemClickListener.onItemClicked(data);
                }
            }
        });
        SDKLogger.debug("sliderModel- " + sliderModel);
        mViewHolder.mContainer.setVisibility(View.GONE);
        try {
            if (sliderModel != null
                    && sliderModel.isAdType()) {
                if (mViewHolder.tvPlay != null)
                    mViewHolder.tvPlay.setVisibility(View.GONE);
                if (mViewHolder.rlWatchList != null)
                    mViewHolder.rlWatchList.setVisibility(View.GONE);
                if (mViewHolder.rlPlayLayout != null)
                    mViewHolder.rlPlayLayout.setVisibility(View.GONE);
                // if (mVmaxAdView == null) {
                if (sliderModel != null
                        && sliderModel.generalInfo != null
                        && sliderModel.generalInfo._id != null) {
                    //TODO: generalInfo._id contains two : seperated adspotIds 1st one is for 3:4 aspect ratio and 2nd is for 16:9 aspect ratio

                }
                // }
                mViewHolder.mContainer.setVisibility(View.VISIBLE);
                  /*if (mVmaxAdView != null && mViewHolder.mContainer.indexOfChild(mVmaxAdView) == -1) {
                    SDKLogger.debug("view is not added adding at position- " + position + " sliderModel- " + sliderModel);
                    if (mVmaxAdView.getParent() != null)
                        ((ViewGroup) mVmaxAdView.getParent()).removeAllViews();
                    mViewHolder.mContainer.addView(mVmaxAdView);
                    if (mVmaxAdView.getAdState() == VmaxAdView.AdState.STATE_AD_READY) {
                        mVmaxAdView.setVisibility(View.VISIBLE);
                        mViewHolder.imageView.setVisibility(View.GONE);
                        Log.e("Image link : ","Cause of Vmax");
                    }
                }*/
            }else{
                if (mViewHolder.tvPlay != null) {
                    mViewHolder.tvPlay.setVisibility(View.VISIBLE);
                    mViewHolder.tvPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnItemClickListener != null) {
                                CardData sliderModel = mItems.get(position);
                                if(sliderModel != null ) {
                                    if (mContext!=null){
                                        sliderModel.playFullScreen = mContext.getResources().getBoolean(R.bool.play_banner_in_landscape);
                                    }else {
                                        sliderModel.playFullScreen = false;
                                    }
                                    mOnItemClickListener.onItemClicked(sliderModel);
                                    fireCleverTapEvent(CleverTap.PROPERTY_PLAY,sliderModel);
                                }
                            }
                        }
                    });
                }
                if (mViewHolder.rlWatchList != null)
                    mViewHolder.rlWatchList.setVisibility(View.GONE);
                if (mViewHolder.rlPlayLayout != null)
                    mViewHolder.rlPlayLayout.setVisibility(View.GONE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public int getRealCount(){
        return mItems.size();
    };

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    public class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imageView;
        final TextView textViewTitle;
        final RelativeLayout mContainer;
        final ImageView imageViewPlayIcon;
        final TextView tvPlay;
        final ImageView iv_watchlist_icon;
        final ImageView imageViewParnter;
        final RelativeLayout rlWatchList;
        final RelativeLayout rlPlayLayout;
        final SurfaceView videoSurfaceView;
        final FrameLayout playerFrameLayout;
        final TextView titleText;
        final TextView subTitleText;
        final ImageView userBadgeImage;
        public CarouselDataViewHolder(View itemView) {
            super(itemView);
            imageView =  itemView.findViewById(R.id.slider_image);
            mContainer =  itemView.findViewById(R.id.ad_container);
            imageViewPlayIcon =  itemView.findViewById(R.id.banner_play_icon);
            tvPlay =  itemView.findViewById(R.id.tv_play);
            textViewTitle = itemView.findViewById(R.id.tv_title);
            imageViewParnter  = (ImageView) itemView.findViewById(R.id.iv_partner);
            rlWatchList = (RelativeLayout) itemView.findViewById(R.id.rl_add_to_watchlist);
            rlPlayLayout = (RelativeLayout) itemView.findViewById(R.id.play_layout);
            iv_watchlist_icon   = (ImageView) itemView.findViewById(R.id.iv_watchlist_icon);
            videoSurfaceView = itemView.findViewById(R.id.inlinePlayerSurfaceView);
            playerFrameLayout = itemView.findViewById(R.id.playerFrameLayout);
            titleText=itemView.findViewById(R.id.title_tv);
            subTitleText=itemView.findViewById(R.id.sub_title_tv);
            userBadgeImage = itemView.findViewById(R.id.content_badge);
        }

        @Override
        public void onClick(View v) {

        }
    }


    private String getImageLink(CardData movie) {
        if (movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes;
        if(layoutType != null && layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_PORTRAIT_BANNER)) {
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL};
        }else if(layoutType != null && layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_SQUARE_BANNER)){
            imageTypes = new String[]{ APIConstants.IMAGE_TYPE_SQUARE_BANNER};
        }else if(layoutType != null && layoutType.equalsIgnoreCase(APIConstants.IMAGE_TYPE_THUMBNAIL)){
            imageTypes = new String[]{ APIConstants.IMAGE_TYPE_THUMBNAIL};
        }else{
            imageTypes = new String[]{ APIConstants.IMAGE_TYPE_THUMBNAIL};
        }

//        String[] imageTypes = new String[]{ APIConstants.IMAGE_TYPE_PORTRAIT_BANNER,
//                APIConstants.IMAGE_TYPE_THUMBNAIL, APIConstants.IMAGE_TYPE_SQUARE_BANNER};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : movie.images.values) {
                if (DeviceUtils.isTablet(mContext)) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }

                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }
        }
        return null;
    }



    private void setUpVmaxView(RelativeLayout layout) {
        String background = PrefUtils.getInstance().getVmaxLayoutBgColor();
        if (background == null) {
            background = "#000000";
        }
        layout.setBackgroundColor(Color.parseColor(background));
    }

    private void fireCleverTapEvent(String action,CardData cardData) {
        if(cardData != null && cardData.generalInfo != null){
            CleverTap.eventPreviewVideoAction(action,cardData);
        }
    }
}
