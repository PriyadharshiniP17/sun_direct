package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.myplex.model.OTTApp;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.utils.PicassoUtil;

import java.util.List;

import autoscroll.RecyclingPagerAdapter;

/**
 * Created by samir on 1/14/2016.
 */
public class OTTAppsImageSliderAdapter extends RecyclingPagerAdapter {


    public int getRealCount() {
        return mItems.size();
    }

    public static class SliderModel {
        public String imageUrl;
        public String siblingOrder;
        public OTTApp ottApp;
        public String contentId;
        public String partnerContentId;
        public Bitmap bitmap;
    }

    public interface OnItemClickListener {
        void onItemClicked(SliderModel sliderModel);
    }

    private Context mContext;

    private List<SliderModel> mItems;

    private OnItemClickListener mOnItemClickListener;

    private int size;

    private boolean isInfiniteLoop = false;

    public OTTAppsImageSliderAdapter(Context context, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.mOnItemClickListener = onItemClickListener;

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
    public OTTAppsImageSliderAdapter setInfiniteLoop(boolean isInfiniteLoop) {
        this.isInfiniteLoop = isInfiniteLoop;
        return this;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getCount() {
        if (mItems == null) {
            return 0;
        }
        return isInfiniteLoop ? Integer.MAX_VALUE : mItems.size();
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

    public void setItems(List<SliderModel> mItems) {
        this.mItems = mItems;
        size = mItems.size();
    }

   /* @Override
    public int getCount() {
        return mItems == null ? 0 : mItems.size();
    }
*/

    @Override
    public View getView(int position, View convertView, ViewGroup collection) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.view_ottapp_slider, collection, false);
        ImageView imageView = (ImageView) layout.findViewById(R.id.slider_image);
        ImageView imageViewPlayIcon = (ImageView) layout.findViewById(R.id.banner_play_icon);
        imageViewPlayIcon.setVisibility(View.GONE);
        //TextView textView = (TextView) layout.findViewById(R.id.slider_title);
        final SliderModel sliderModel = getPosition(position) >= mItems.size() ? null : mItems.get(getPosition(position));


        if (sliderModel != null && TextUtils.isEmpty(sliderModel.imageUrl)) {
            if (mContext.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE) {
                PicassoUtil.with(mContext).load(sliderModel.imageUrl, imageView, R.drawable.black);
            } else {
                PicassoUtil.with(mContext).load(sliderModel.imageUrl, imageView, R.drawable.black);
            }
        }

       /* if(sliderModel.ottApp != null && sliderModel.ottApp.offerDescription != null){
            textView.setText(sliderModel.ottApp.offerDescription);
        }
*/
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(sliderModel);
                }
            }
        });
        return layout;
    }

}
