package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.OnSingleClickListener;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.util.List;

/**
 * Created by apalya on 24/4/18.
 */

public class BannerAdapter3D extends RecyclerView.Adapter<BannerAdapter3D.CustomViewHolder> {
    private String TAG = BannerAdapter3D.class.getSimpleName();
    private Context mContext;
    private List<CardData> mItemList;
    private CarouselInfoData parentCarouselData;
    private final long MIN_CLICK_INTERVAL = 1500;
    private long mLastClickTime;

    public void setOnItemClickListener(ViewPagerImageSliderAdapter.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private ViewPagerImageSliderAdapter.OnItemClickListener mOnItemClickListener;

    public BannerAdapter3D(Context context, List<CardData> itemList, boolean isInfinite, CarouselInfoData parentCarouselData) {
        this.mContext = context;
        this.mItemList = itemList;
        this.parentCarouselData = parentCarouselData;
    }

    private String getImageLink2(CardData movie) {
        if (movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()) {
            return null;
        }
       // String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL};
        String[] imageTypes ;
        if (DeviceUtils.isTablet(mContext)) {
            //imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER};
           // imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_BANNER};
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER};
            //imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL};
        }else {
            //imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER};
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL};
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

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.banner_carousal_3d, null, false);
        CustomViewHolder customItemViewHolder = new CustomViewHolder(view);
        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int i) {
        int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
     //   int c = (int) (((double) 9 / 16) * displayWidth);
      //  int  height = ApplicationController.getApplicationConfig().screenHeight - ((int) mContext.getResources().getDimension(R.dimen._20sdp)+(int) mContext.getResources().getDimension(R.dimen.margin_gap_64)+(int) mContext.getResources().getDimension(R.dimen._50sdp)+(int) mContext.getResources().getDimension(R.dimen._80sdp));
      //  int height = ApplicationController.getApplicationConfig().screenHeight - (int)mContext.getResources().getDimension(R.dimen._205sdp);
      //  customViewHolder.itemView.setMinimumHeight(c);
      //  customViewHolder.imageView.setMaxWidth(displayWidth);
       // customViewHolder.itemView.setMaxWidth(displayWidth);
        int height;
        if(DeviceUtils.isTablet(mContext)) {
            customViewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            int widthRatio = 16;
            int heightRatio = 6;

            if(DeviceUtils.getScreenOrientation(mContext) == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                widthRatio = 16;
                heightRatio = 14;

            }

           height = (((ApplicationController.getApplicationConfig().screenHeight * heightRatio) / widthRatio));
        }
        else
             height = ApplicationController.getApplicationConfig().screenHeight - ((int) mContext.getResources().getDimension(R.dimen.margin_gap_64)+(int) mContext.getResources().getDimension(R.dimen._100sdp));
        customViewHolder.imageView.getLayoutParams().width = displayWidth ;
        customViewHolder.imageView.getLayoutParams().height = height;
        customViewHolder.bannerBg.getLayoutParams().width = displayWidth;
        customViewHolder.bannerBg.getLayoutParams().height = height;
        //NOT NULL SAFE!
        final CardData sliderModel = mItemList.get(i);

        //Setting movie name font
        customViewHolder.imageView.setTag(sliderModel);
        String imageLink2;
        if (sliderModel != null) {
            imageLink2 = getImageLink2(sliderModel);
           if (Util.isValidContextForGlide(mContext))
            Glide.with(mContext)
                    .load(imageLink2)
                    .placeholder(R.drawable.black)
                    //.transform(new RoundedCorners((int)customViewHolder.imageView.getContext().getResources().getDimension(R.dimen.margin_gap_30)))
                    .error(R.drawable.black)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(customViewHolder.imageView);
//            PicassoUtil.with(mContext).load(imageLink2, customViewHolder.imageView, R.drawable.movie_thumbnail_placeholder16x9);
        }
        customViewHolder.rentLayout.setVisibility(View.GONE);
       /* if(sliderModel != null && sliderModel.generalInfo !=null && sliderModel.generalInfo.contentRights != null && sliderModel.generalInfo.contentRights.size()>0 && sliderModel.generalInfo.contentRights.get(0)!= null) {
            if (sliderModel.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                customViewHolder.rentLayout.setVisibility(View.VISIBLE);
            }
        }*/
        String partnerImageLink = sliderModel.getPartnerImageLink(mContext);
        if(partnerImageLink !=null) {
       // Log.d("partnerImage.... " , partnerImageLink);
            if (!TextUtils.isEmpty(partnerImageLink)) {
                if (!partnerImageLink.isEmpty()) {
                    customViewHolder.mImageViewPartner.setVisibility(View.VISIBLE);
                    //Picasso.with(mContext).load(partnerImageLink).resize(holder.mImageViewPartner.getLayoutParams().width, holder.mImageViewPartner.getLayoutParams().height).placeholder(R.drawable.epg_thumbnail_default).centerInside().into(holder.mImageViewPartner);
                    PicassoUtil.with(mContext).load(partnerImageLink, customViewHolder.mImageViewPartner, R.drawable.black);
                } else {
                    customViewHolder.mImageViewPartner.setVisibility(View.GONE);
                }
            }else{
                customViewHolder.mImageViewPartner.setVisibility(View.GONE);
            }
        }else{
            customViewHolder.mImageViewPartner.setVisibility(View.GONE);
        }
        customViewHolder.imageView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //Log.d(TAG, "Single click listener called position :");

                if (mOnItemClickListener != null) {
                    CardData data = null;
                    long currentClickTime = SystemClock.uptimeMillis();
                    long elapsedTime = currentClickTime - mLastClickTime;
                    mLastClickTime = currentClickTime;

                    if (elapsedTime <= MIN_CLICK_INTERVAL)
                        return;
                    if (v.getTag() instanceof CardData) {
                        data = (CardData) v.getTag();
                    }
                    mOnItemClickListener.onItemClicked(data);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        ShapeableImageView imageView, mImageViewFreeBand;
        ImageView dolbyImage, subtitleLogo, bannerBg, mImageViewPartner;
        TextView title, tag_line;
//        MaterialCardView mPartnerImageLogo;
        private LinearLayout rentLayout;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image);
            bannerBg = itemView.findViewById(R.id.banner_bg);
            mImageViewPartner= (ImageView) itemView.findViewById(R.id.iv_partener_logo_right);
//            mPartnerImageLogo =(MaterialCardView)itemView.findViewById(R.id.partner_image_logo);
            rentLayout = itemView.findViewById(R.id.iv_rent);
        }
    }
}
