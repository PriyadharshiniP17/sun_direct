package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.FragmentCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentLanguageInfo;
import com.myplex.myplex.utils.PicassoUtil;

import java.util.List;

public class AdapterGenres extends RecyclerView.Adapter<AdapterGenres.CarouselDataViewHolder> {


    private Context mContext;
    private List<CarouselInfoData> mDataList;
    private List<CarouselInfoData> mListCarouselInfo;
    private boolean isGenreScreen;

    public AdapterGenres(Context context ,List<CarouselInfoData> dataList,boolean isGenreScreen){
         mContext = context;
         mDataList = dataList;
         this.isGenreScreen = isGenreScreen;
     }

    @Override
    public AdapterGenres.CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_genres_child, parent, false);
        return new CarouselDataViewHolder(itemView);    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {

        CarouselInfoData data = mDataList.get(position);
        String imageLink = getImageLink(data);
        if (TextUtils.isEmpty(imageLink)
                || imageLink.compareTo("Images/NoImage.jpg") == 0) {
            holder.mGenreImage.setImageResource(R.drawable
                    .movie_thumbnail_placeholder);
        } else {
            PicassoUtil.with(mContext).load(imageLink, holder.mGenreImage, R.drawable.movie_thumbnail_placeholder);
        }
        holder.setClickListener(mOnItemClickListenerWithData);


    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    private static final int TYPE_ITEM = 1;
    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    private ItemClickListenerWithData mOnItemClickListenerWithData;



    class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListenerWithData clickListener;

        private ImageView mGenreImage ;

        public CarouselDataViewHolder(View view) {
            super(view);
            mGenreImage =  view.findViewById(R.id.image);

            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mDataList == null || mDataList.isEmpty()) return false;
                   CarouselInfoData data = mDataList.get(getAdapterPosition());
                    if (data == null) {
                        return false;
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
            if (mDataList != null) {
                showRelatedVODListFragment(mDataList.get(getAdapterPosition()));
            }
        }

    }

    private void showRelatedVODListFragment(CarouselInfoData carouselInfoData) {
        Bundle args = new Bundle();

        CacheManager.setCarouselInfoData(carouselInfoData);

        showCarouselViewAllFragment(args, carouselInfoData);
    }

    private void showCarouselViewAllFragment(Bundle args,CarouselInfoData carouselInfoData) {

        CacheManager.setCarouselInfoData(carouselInfoData);

        if (mCarouselInfoData != null) {
            args.putString(FragmentCarouselInfo.PARAM_ANALYTICS_CAROUSAL_SOURCE, mCarouselInfoData.name);
            args.putString(FragmentCarouselInfo.PARAM_ANALYTICS_CAROUSAL_SOURCE_NAVIGANTION,"navigation menu");
        }
        if (isGenreScreen) {
            //args.putSerializable(FragmentCarouselInfo.PARAM_CAROUSEL_INFO_DATA, carouselInfoData);
            //((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
            CacheManager.setCarouselInfoData(carouselInfoData);
            args.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, carouselInfoData.title);
            args.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, null);
            args.putBoolean(FragmentCarouselInfo.PARAM_IS_GENRE_ONLY,true);
            args.putString(FragmentCarouselInfo.PARAM_GENRE,carouselInfoData.title);
        } else {

            CacheManager.setCarouselInfoData(carouselInfoData);
            args.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, carouselInfoData.title);
            args.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, carouselInfoData.title);
        }
        FragmentLanguageInfo fragment = FragmentLanguageInfo.newInstance(args);
        ((MainActivity) mContext).pushFragment(fragment);
    }

    public static String getImageLink(CarouselInfoData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.size() <=0) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_ICON, APIConstants.IMAGE_TYPE_ICON};

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }
    private CarouselInfoData mCarouselInfoData;
    public void setInfoForAnalyticsEvents(CarouselInfoData carouselInfoData){
        mCarouselInfoData = carouselInfoData;
    }


}
