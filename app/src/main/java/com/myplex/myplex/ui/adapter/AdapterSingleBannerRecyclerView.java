package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;

import java.util.ArrayList;
import java.util.List;

public class AdapterSingleBannerRecyclerView extends RecyclerView.Adapter<AdapterSingleBannerRecyclerView.CarouselDataViewHolder> {

    protected static final String TAG = AdapterSingleBannerRecyclerView.class.getSimpleName();
    private Context mContext;
    private List<CardData> mItems = new ArrayList<>();
    private int carouselPosition;
    private CarouselInfoData carouselInfoData;

    public String getTabName() {
        return tabName;
    }
    private String tabName;
    private OnItemClickListener mOnItemClickListener;
    private String layoutType;
    private boolean isTitleVisible = true;
    private String mBgColor;


    public void setTabName(String tabName) {
        this.tabName = tabName;
    }
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setTitleVisibility(boolean showTitle) {
        isTitleVisible = showTitle;
    }

    public void setCarouselPosition(int position) {
        this.carouselPosition = position;
    }

    public interface OnItemClickListener {
        void onItemClicked(CardData cardData, int pos);
    }

    public AdapterSingleBannerRecyclerView(Context context) {
        this.mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        SDKLogger.debug("constructor");
    }
    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        CarouselDataViewHolder customItemViewHolder;
        view = LayoutInflater.from(mContext).inflate(R.layout.listitem_singlebannerplayeritem, parent, false);
        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }


    public void setItems(List<CardData> mItems, String layoutType, CarouselInfoData carouselInfoData,String mBgColor) {
        this.mItems.clear();
        this.mItems.addAll(mItems);
        this.layoutType = layoutType;
        this.carouselInfoData = carouselInfoData;
        this.mBgColor=mBgColor;
    }

    @Override
    public void onBindViewHolder(final CarouselDataViewHolder mViewHolder, final int pos) {
        Log.e("Target BindViewHolder","called");
        final int position = pos % mItems.size();
        final CardData sliderModel = mItems.get(position);

        int Height;
        if (DeviceUtils.isTablet(mContext)) {
            DisplayMetrics dm = new DisplayMetrics();
            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
            Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth + " dm.density- " + dm.density + " real width- " + (ApplicationController.getApplicationConfig().screenWidth / dm.density));
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                float ratio= ((float)9)/16;
                int newWidth = (int)(ApplicationController.getApplicationConfig().screenWidth - (0.25 * ApplicationController.getApplicationConfig().screenWidth));
                Height = (int) (newWidth * ratio);
                mViewHolder.imageView.getLayoutParams().height = Height;
                mViewHolder.videoSurfaceView.getLayoutParams().width = newWidth;
                mViewHolder.videoSurfaceView.getLayoutParams().height = Height;
            } else {
                Log.d("ACI HEIGHT", ApplicationController.getApplicationConfig().screenHeight + " ACI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
                Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
                mViewHolder.imageView.getLayoutParams().height = Height;
            }
        } else {
            Height = (int) (((ApplicationController.getApplicationConfig().screenWidth * 9) / 16));
            mViewHolder.imageView.getLayoutParams().height = Height;
            Log.d("ACI HEIGHT", Height + " AMCI WIDTH " + ApplicationController.getApplicationConfig().screenWidth);
        }

        mViewHolder.videoSurfaceView.setTag(sliderModel);
        mViewHolder.videoSurfaceView.setTag(R.string.position,pos);

        mViewHolder.imageView.setTag(sliderModel);
        mViewHolder.imageView.setTag(R.string.position,pos);

        mViewHolder.singleBannerTitle.setText(carouselInfoData.title == null ? "" : carouselInfoData.title);
        if(carouselInfoData.altTitle != null && !carouselInfoData.altTitle.isEmpty()){
            mViewHolder.singleBannerAltTitle.setVisibility(View.VISIBLE);
            mViewHolder.singleBannerAltTitle.setText(carouselInfoData.altTitle );
        }

        if (mBgColor!=null&&!TextUtils.isEmpty(mBgColor)){
            mViewHolder.singleBannerAltTitle.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
            mViewHolder.singleBannerTitle.setTextColor(mContext.getResources().getColor(R.color.light_theme_carousel_heading_text_color));
        }

        if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN ){
            mViewHolder.singleBannerTitle.setVisibility(View.GONE);
            if(carouselInfoData.altTitle == null || carouselInfoData.altTitle.isEmpty()){
                mViewHolder.singleBannerAltTitle.setVisibility(View.GONE);

                mViewHolder.singleBannerTitle.setVisibility(View.VISIBLE);
            }else{
                mViewHolder.singleBannerAltTitle.setVisibility(View.VISIBLE);
            }
        }else{
            mViewHolder.singleBannerTitle.setVisibility(View.VISIBLE);

            if(carouselInfoData.altTitle != null && !carouselInfoData.altTitle.isEmpty()){
                mViewHolder.singleBannerAltTitle.setVisibility(View.VISIBLE);
            }else{
                mViewHolder.singleBannerAltTitle.setVisibility(View.GONE);
            }
        }

        if (isTitleVisible){
            mViewHolder.singleBannerTitle.setVisibility(View.VISIBLE);
        }else {
            mViewHolder.singleBannerTitle.setVisibility(View.GONE);
        }

        String imageLink = null;
        imageLink = sliderModel.getImageLink(APIConstants.IMAGE_TYPE_BANNER);
        /*if(mItems!=null && mItems.size()>0) {
            imageLink = getImageLink(mItems.get(0));*/
            if (TextUtils.isEmpty(imageLink)
                    || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
                mViewHolder.imageView.setImageResource(R.drawable
                        .black);
            } else {
                if (mContext.getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_LANDSCAPE) {
                    PicassoUtil.with(mContext).
                            loadCenterInside(imageLink, mViewHolder.imageView,
                                    R.drawable.black,
                                    ApplicationController.getApplicationConfig().
                                            screenWidth, Height);
                } else {
                PicassoUtil.with(mContext).loadCenterCrop(imageLink, mViewHolder.imageView,
                        R.drawable.black,
                        ApplicationController.getApplicationConfig().screenWidth, Height);
//                    PicassoUtil.with(mContext).load(imageLink, mViewHolder.imageView);
                }
            }


        mViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    CardData data = null;
                    int contentPos = -1;
                    if (view.getTag() instanceof CardData) {
                        data = (CardData) view.getTag();
                        contentPos = (int) view.getTag(R.string.position);
                    }

                    mOnItemClickListener.onItemClicked(data,contentPos);
                }
            }
        });
        mViewHolder.videoSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    CardData data = null;
                    int contentPos = -1;
                    if (view.getTag() instanceof CardData) {
                        data = (CardData) view.getTag();
                        contentPos = (int) view.getTag(R.string.position);
                    }

                    mOnItemClickListener.onItemClicked(data,contentPos);
                }
            }
        });
        SDKLogger.debug("sliderModel- " + sliderModel);
        String partnerImageURL = carouselInfoData.listCarouselData.get(position).getPartnerImageLink(mContext);
        if(!TextUtils.isEmpty(partnerImageURL)){
            PicassoUtil.with(mContext).load(partnerImageURL, mViewHolder.partnerLogoImageView,
                    R.drawable.black);
        }else{
            mViewHolder.partnerLogoImageView.setImageResource(R.drawable.transparent);
        }
        if(carouselInfoData!=null && carouselInfoData.listCarouselData!=null && carouselInfoData.listCarouselData.get(position).generalInfo != null) {
            if (!TextUtils.isEmpty(carouselInfoData.listCarouselData.get(position).generalInfo.accessLabelImage) && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE) {
                mViewHolder.userBadgeImage.setVisibility(View.VISIBLE);
                PicassoUtil.with(mContext).load(carouselInfoData.listCarouselData.get(position).generalInfo.accessLabelImage,
                        mViewHolder.userBadgeImage);
            } else {
                mViewHolder.userBadgeImage.setVisibility(View.GONE);
            }
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
        final ImageView mMuteButton;
        final SurfaceView videoSurfaceView;
        final FrameLayout playerFrameLayout;
        final TextView singleBannerTitle;
        final TextView singleBannerAltTitle;
        final ImageView userBadgeImage;
        final ImageView partnerLogoImageView;

        public CarouselDataViewHolder(View itemView) {
            super(itemView);
            imageView =  itemView.findViewById(R.id.imageview_thumbnail_voditem);
            videoSurfaceView = itemView.findViewById(R.id.playerSurfaceView);
            playerFrameLayout = itemView.findViewById(R.id.playerFrameLayout);
            mMuteButton = itemView.findViewById(R.id.mutebuttoniv);
            singleBannerTitle = itemView.findViewById(R.id.single_banner_title);
            singleBannerAltTitle = itemView.findViewById(R.id.single_banner_alt_title);
            userBadgeImage = itemView.findViewById(R.id.content_badge);
            partnerLogoImageView = itemView.findViewById(R.id.iv_partener_logo_right);
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
        if (layoutType != null && layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_PORTRAIT_BANNER)) {
            imageTypes = new String[]{ APIConstants.GIF_IMAGE_TYPE_PORTRAIT_BANNER, APIConstants.IMAGE_TYPE_PORTRAIT_BANNER};
        }else if(layoutType != null && layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_SQUARE_BANNER)){
            imageTypes = new String[]{ APIConstants.GIF_IMAGE_TYPE_SQUARE_BANNER, APIConstants.IMAGE_TYPE_SQUARE_BANNER};
        } else{
            imageTypes = new String[]{ APIConstants.GIF_IMAGE_TYPE_PORTRAIT_BANNER, APIConstants.GIF_IMAGE_TYPE_SQUARE_BANNER,
                    APIConstants.IMAGE_TYPE_PORTRAIT_BANNER, APIConstants.IMAGE_TYPE_SQUARE_BANNER};
        }

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
}
