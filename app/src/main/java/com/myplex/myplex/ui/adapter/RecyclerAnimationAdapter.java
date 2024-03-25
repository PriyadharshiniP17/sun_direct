package com.myplex.myplex.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.rd.PageIndicatorView;

import java.util.List;

public class RecyclerAnimationAdapter extends RecyclerView.Adapter<RecyclerAnimationAdapter.ViewHolder> {

    private LayoutInflater mInflater;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClicked(CardData cardData, int contentPosition);
    }

    private Context mContext;

    private List<CardData> mItems;

    private int size;

    private boolean isInfiniteLoop = false;

    private PageIndicatorView pageIndicatorView;

    public void setPagerIndicator(PageIndicatorView pageIndicatorView){
        this.pageIndicatorView=pageIndicatorView;
    }

    public RecyclerAnimationAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = mInflater.inflate(R.layout.view_item_pager_anim, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder mViewHolder, int position) {


        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //if you need three fix imageview in width
        int devicewidth = displaymetrics.widthPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        /*int marginwidth = devicewidth/4 + 50;
        params.setMargins(0, -marginwidth, 0, -marginwidth);
        mViewHolder.viewPagerAnimRootLayout.setLayoutParams(params);

        mViewHolder.imageView.getLayoutParams().height = devicewidth + (devicewidth / 2);*/

        final CardData sliderModel = getPosition(position) >= mItems.size() ? null : mItems.get(getPosition(position));

        if (pageIndicatorView!=null){
            pageIndicatorView.setSelection(getPosition(position));
        }

        mViewHolder.imageView.setTag(sliderModel);
        mViewHolder.imageView.setTag(R.string.position, getPosition(position));

        mViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    CardData data = null;
                    int contentPosition = -1;
                    if (view.getTag() instanceof CardData) {
                        data = (CardData) view.getTag();
                        contentPosition = (int) view.getTag(R.string.position);
                    }

                    mOnItemClickListener.onItemClicked(data, contentPosition);
                }
            }
        });

        mViewHolder.setClickListener(mOnItemClickListener);

        String imageLink = null;
        imageLink = getImageLink(sliderModel);
        if (TextUtils.isEmpty(imageLink)
                || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            mViewHolder.imageView.setImageResource(R.color.black);
        } else if (!TextUtils.isEmpty(imageLink)) {
            if (mContext.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE) {
                PicassoUtil.with(mContext).load(imageLink, mViewHolder.imageView, R.color.black);
            } else {
                PicassoUtil.with(mContext).load(imageLink, mViewHolder.imageView, R.color.black);
            }
        }
        try {

            if (sliderModel.generalInfo != null) {
                if (!TextUtils.isEmpty(sliderModel.generalInfo.accessLabelImage) && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE) {
                    mViewHolder.badgeImage.setVisibility(View.VISIBLE);
                    PicassoUtil.with(mContext).load(sliderModel.generalInfo.accessLabelImage, mViewHolder.badgeImage);
                } else {
                    mViewHolder.badgeImage.setVisibility(View.GONE);
                }
            }

            if (sliderModel != null && sliderModel.generalInfo != null && mViewHolder.badgeImage != null) {
                if (!TextUtils.isEmpty(sliderModel.generalInfo.accessLabelImage) && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE) {
                    mViewHolder.badgeImage.setVisibility(View.VISIBLE);
                    PicassoUtil.with(mContext).load(sliderModel.generalInfo.accessLabelImage, mViewHolder.badgeImage);
                } else {
                    mViewHolder.badgeImage.setVisibility(View.GONE);
                }
            }


        } catch (Throwable e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
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

    private String getImageLink(CardData movie) {
        if (movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{
                APIConstants.IMAGE_TYPE_PORTRAIT_BANNER,
                APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,
                APIConstants.IMAGE_TYPE_THUMBNAIL,
                APIConstants.IMAGE_TYPE_THUMBNAIL_BANNER
        };

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : movie.images.values) {
                if (DeviceUtils.isTablet(mContext)) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnItemClickListener mOnItemClickListener;
        public ImageView imageView;
        public ImageView badgeImage;
        public RelativeLayout viewPagerAnimRootLayout;

        public ViewHolder(View convertView) {
            super(convertView);
            imageView = (ImageView) convertView.findViewById(R.id.thumbnail_movie);
            badgeImage = (ImageView) convertView.findViewById(R.id.content_badge);
            viewPagerAnimRootLayout = convertView.findViewById(R.id.viewPagerAnimRootLayout);

            convertView.setOnClickListener(this);
        }

        public void setClickListener(OnItemClickListener itemClickListener) {
            this.mOnItemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            if (this.mOnItemClickListener != null && mItems != null && getAdapterPosition() != -1) {
                this.mOnItemClickListener.onItemClicked(mItems.get(getAdapterPosition()), getAdapterPosition());
            }
        }
    }

}