package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;

import java.util.List;

import autoscroll.RecyclingPagerAdapter;

/**
 * Created by Apparao 4/26/2019.
 */
public class PortraitViewPagerImageSliderAdapter extends RecyclingPagerAdapter {

    public static final int PAGE_COUNT_FACTOR = 100000;
    private LayoutInflater mInflater;
    protected static final String TAG = PortraitViewPagerImageSliderAdapter.class.getSimpleName();
    private boolean isTitleVisible = true;

    public void setTitleVisibility(boolean showTitle) {
        isTitleVisible = showTitle;
    }

    public interface OnItemClickListener {
        void onItemClicked(CardData cardData);
    }

    private Context mContext;

    private List<CardData> mItems;

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    private String tabName;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private OnItemClickListener mOnItemClickListener;

    private int size;

    private boolean isInfiniteLoop = false;

    public PortraitViewPagerImageSliderAdapter(Context context) {

        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        SDKLogger.debug("constructor");
    }

    /**
     * @return the isInfiniteLoop
     */
    public boolean isInfiniteLoop() {
        return isInfiniteLoop;
    }

    /**
     * @param isInfiniteLoop the isInfiniteLoop to set
     */
    public PortraitViewPagerImageSliderAdapter setInfiniteLoop(boolean isInfiniteLoop) {
        this.isInfiniteLoop = isInfiniteLoop;
        return this;
    }

    @Override
    public int getItemPosition(Object object) {
        if (PrefUtils.getInstance().getBoolean(APIConstants.UPDATE_PORTRAIT_BANNER,false)) {
            PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER,false);
            return POSITION_NONE;
        } else {
            return super.getItemPosition(object);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getCount() {
        return isInfiniteLoop ? mItems.size() * PAGE_COUNT_FACTOR : mItems.size();
    }

    public int getRealCount() {
        return mItems.size();
    }

    /**
     * get really position
     *
     * @param position
     * @return
     */
    private int getPosition(int position) {
        return isInfiniteLoop ? position % size : position;
    }

    public void setItems(List<CardData> mItems) {
        this.mItems = mItems;
        size = mItems.size();
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup collection) {
        final ViewHolder mViewHolder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.view_portrait_ottapp_slider, collection, false);
            mViewHolder = new ViewHolder();
            injectViews(mViewHolder, convertView, position);
            convertView.setTag(mViewHolder);
        }else
            mViewHolder = (ViewHolder) convertView.getTag();
        final CardData sliderModel = getPosition(position) >= mItems.size() ? null : mItems.get(getPosition(position));
        mViewHolder.setCardData(sliderModel);
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
        if (mViewHolder.iv_watchlist_icon != null) {
            if(sliderModel != null && sliderModel.isFavourite){
                mViewHolder.iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon_added);
            }else{
                mViewHolder.iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon);
            }
        }
        return convertView;
    }

    private void injectViews(ViewHolder mViewHolder, View convertView, int position) {
        mViewHolder.imageView = (ImageView) convertView.findViewById(R.id.slider_image);
        mViewHolder.mContainer = (RelativeLayout) convertView.findViewById(R.id.ad_container);
        mViewHolder.tvPlay = convertView.findViewById(R.id.tv_play);;
        mViewHolder.imageViewPlayIcon = convertView.findViewById(R.id.banner_play_icon);
        mViewHolder.textViewTitle = convertView.findViewById(R.id.tv_title);
        mViewHolder.imageViewParnter = (ImageView) convertView.findViewById(R.id.iv_partner);
        mViewHolder.rlWatchList = (RelativeLayout) convertView.findViewById(R.id.rl_add_to_watchlist);
        mViewHolder.rlPlayLayout = (RelativeLayout) convertView.findViewById(R.id.play_layout);
         mViewHolder.iv_watchlist_icon = (ImageView) convertView.findViewById(R.id.iv_watchlist_icon);
         mViewHolder.titleText=convertView.findViewById(R.id.title_tv);
        mViewHolder.userBadgeImage = convertView.findViewById(R.id.content_badge);
         mViewHolder.subTitleText=convertView.findViewById(R.id.sub_title_tv);
    }

    private String getImageLink(CardData movie) {
        if (movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{ APIConstants.IMAGE_TYPE_PORTRAIT_BANNER,APIConstants.IMAGE_TYPE_THUMBNAIL};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : movie.images.values) {
                if (DeviceUtils.isTablet(mContext)) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }

                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }
        }
        return null;
    }


    private View getVmaxAdView(final ViewHolder container, String adSpotId) {
       //        adSpotId = "ae1f49cd";
        RelativeLayout layout = null;
        if (adSpotId != null) {


            if (DeviceUtils.isTablet(mContext))
                layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.listitem_vmax_image_ads_banners, null);
            else {
                layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.listitem_vmax_image_ads_portrait_banners, null);
                DisplayMetrics dm = new DisplayMetrics();
                if (mContext instanceof MainActivity)
                    ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                int height = (int) ((ApplicationController.getApplicationConfig().screenWidth / dm.density) / 0.75);
                height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, mContext.getResources().getDisplayMetrics());
                if (layout != null && layout.getLayoutParams() != null) {
                    layout.getLayoutParams().height = height;
                }
            }

            GradientDrawable gradientDrawable = (GradientDrawable) layout.findViewById(R.id.vmax_sponsored).getBackground();
            gradientDrawable.setColor(UiUtil.getColor(mContext, R.color.color_e9c53b));
            setUpVmaxView(layout);

//            vmaxAdView.setTestDevices(VmaxAdView.TEST_via_ADVID, "efee1d0d-27e6-4095-8aaa-5b603d87d145");
//     vmaxAdView.setTestDevices(VmaxAdView.TEST_via_ADVID,"ae1ee71d-8d1d-4e8f-bf30-d7fb321918c2");
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


    class ViewHolder {

        private CardData mCardData;
        public ImageView imageView;
        public TextView textViewTitle,tvPlay;
        public RelativeLayout mContainer;
        public ImageView imageViewPlayIcon;
        public ImageView userBadgeImage;
        ImageView imageViewParnter;
        RelativeLayout rlWatchList;
        RelativeLayout rlPlayLayout;
        ImageView iv_watchlist_icon;
        TextView titleText;
        TextView subTitleText;
        public void setCardData(CardData mCardData){
            this.mCardData = mCardData;
            bindData();
        }

        private void bindData(){
            imageView.setTag(mCardData);
            String imageLink = null;
            imageLink = getImageLink(mCardData);
            if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                imageView.setImageResource(R.drawable
                        .movie_thumbnail_placeholder);
            } else if (imageLink != null) {
                if (mContext.getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_LANDSCAPE) {
                    PicassoUtil.with(mContext).load(imageLink, imageView, R.drawable.movie_thumbnail_placeholder);
                } else {
                    PicassoUtil.with(mContext).load(imageLink, imageView, R.drawable.movie_thumbnail_placeholder);
                }
            }
            if(isTitleVisible){
                if(mCardData != null && mCardData.generalInfo != null && !TextUtils.isEmpty(mCardData.generalInfo.title)){
                    titleText.setText(mCardData.generalInfo.title);
                }else {
                    titleText.setVisibility(View.GONE);
                }

                if (mCardData.content != null && mCardData.content.genre.size() > 0) {
                    subTitleText.setText(mCardData.getGenre());
                }else {
                    subTitleText.setVisibility(View.GONE);
                }

                textViewTitle.setVisibility(View.GONE);
                textViewTitle.setText((mCardData != null && mCardData.generalInfo != null && !TextUtils.isEmpty(mCardData.generalInfo.title)) ? mCardData.generalInfo.title : "");
            }else{
                subTitleText.setVisibility(View.GONE);
                titleText.setVisibility(View.GONE);
                textViewTitle.setVisibility(View.GONE);
            }

            if(mCardData!=null && mCardData.generalInfo != null && userBadgeImage != null){
                if(!TextUtils.isEmpty(mCardData.generalInfo.accessLabelImage) && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE){
                    userBadgeImage.setVisibility(View.VISIBLE);
                    PicassoUtil.with(mContext).load(mCardData.generalInfo.accessLabelImage,userBadgeImage);
                }else {
                    userBadgeImage.setVisibility(View.GONE);
                }
            }


            String partnerImage = mCardData.getPartnerImageLink(mContext);
            if(!TextUtils.isEmpty(partnerImage)){
                PicassoUtil.with(mContext).load(partnerImage, imageViewParnter, R.drawable.movie_thumbnail_placeholder);
            }else{
                imageViewParnter.setImageResource(R.drawable.transparent);
            }
            if (imageViewPlayIcon != null) {
                imageViewPlayIcon.setVisibility(View.GONE);
            }
            imageView.setVisibility(View.VISIBLE);
            /*imageView.setOnClickListener(new View.OnClickListener() {
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
            mContainer.setVisibility(View.GONE);
            if (rlWatchList != null) {
                rlWatchList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        rlWatchList.setEnabled(false);
                        rlWatchList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (rlWatchList != null)
                                    rlWatchList.setEnabled(true);
                            }
                        }, 2000);

                        //Util.showFeedback(rlWatchList);
                        String type = mCardData.generalInfo.type;
                        String _id = mCardData._id;
                        if (mCardData != null
                                && (mCardData.isVODYoutubeChannel()
                                || mCardData.isTVSeason()
                                || mCardData.isVODChannel()
                                || mCardData.isVODCategory()
                                || mCardData.isTVSeries())) {
                            type = mCardData.generalInfo.type;
                            _id = mCardData._id;
                        }
                        if (mCardData.isProgram()) {
                            _id = mCardData.globalServiceId;
                            type = APIConstants.TYPE_LIVE;
                        }
                        if (!Util.checkUserLoginStatus()) {
                            ((MainActivity) mContext).initLogin(Analytics.VALUE_SOURCE_CAROUSEL, (mContext != null && ((MainActivity) mContext).carouselInfoData != null) ? ((MainActivity) mContext).carouselInfoData.title : mCardData.getTitle());
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
                                            if (mCardData != null
                                                    && mCardData.currentUserData != null) {
                                                mCardData.currentUserData.favorite = response.body().favorite;
                                                mCardData.isFavourite = true;
                                                PrefUtils.getInstance().shouldChangeFavouriteState(mCardData._id,true,tabName);
                                            }

                                            if (response.body().favorite) {
                                                iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon_added);
                                                if(mCardData != null && mCardData._id != null && !TextUtils.isEmpty(mCardData._id)) {
                                                    CleverTap.eventPromoVideoAddedToWatchList(APIConstants.NOT_AVAILABLE, mCardData._id, CleverTap.SOURCE_BANNER);
                                                }
                                                AlertDialogUtil.showToastNotification("Added to Watchlist");
                                            } else {
                                                AlertDialogUtil.showToastNotification("Removed from Watchlist");
                                                iv_watchlist_icon.setImageResource(R.drawable.watchlist_icon);
                                                mCardData.isFavourite = false;
                                                PrefUtils.getInstance().shouldChangeFavouriteState(mCardData._id,false,tabName);
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

            try {
                if (mCardData != null
                        && mCardData.isAdType()) {
                    if (tvPlay != null)
                        tvPlay.setVisibility(View.GONE);
                    if (rlWatchList != null)
                        rlWatchList.setVisibility(View.GONE);
                    if (rlPlayLayout != null)
                        rlPlayLayout.setVisibility(View.GONE);

                    // if (mVmaxAdView == null) {
                    if (mCardData != null
                            && mCardData.generalInfo != null
                            && mCardData.generalInfo._id != null) {
                        //TODO: generalInfo._id contains two : seperated adspotIds 1st one is for 3:4 aspect ratio and 2nd is for 16:9 aspect ratio
                                          }
                    // }
                    mContainer.setVisibility(View.VISIBLE);

                }else{
                    if (tvPlay != null) {
                        tvPlay.setVisibility(View.VISIBLE);
                        tvPlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mOnItemClickListener != null) {
                                   // CardData sliderModel = getPosition(position) >= mItems.size() ? null : mItems.get(getPosition(position));
                                    if(mCardData != null ) {
                                        if (mContext!=null){
                                            mCardData.playFullScreen = mContext.getResources().getBoolean(R.bool.play_banner_in_landscape);
                                        }else {
                                            mCardData.playFullScreen = false;
                                        }
                                        mOnItemClickListener.onItemClicked(mCardData);
                                    }
                                }
                            }
                        });
                    }
                    if (rlWatchList != null)
                        rlWatchList.setVisibility(View.GONE);
                    if (rlPlayLayout != null)
                        rlPlayLayout.setVisibility(View.GONE);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }





}
