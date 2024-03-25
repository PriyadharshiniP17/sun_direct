package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;

import java.util.List;

public class AdapterLanguages extends RecyclerView.Adapter<AdapterLanguages.CarouselDataViewHolder>  {


    private Context mContext;
    private List<CarouselInfoData> mListMovies;

    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_language_child, parent, false);
        return new CarouselDataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

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

//        final ImageView mImageViewRupeeIcon;


        public CarouselDataViewHolder(View view) {
            super(view);
//            UiUtil.showFeedback(view,true, R.color.list_item_bkg);
            mTextViewMovieTitle = (TextView)view.findViewById(R.id.textview_movies_title);
            mImageViewMovies = (ImageView)view.findViewById(R.id.thumbnail_movie);
            mImageViewPlayIcon = (ImageView)view.findViewById(R.id.thumbnail_movie_play);
            mImageViewInstallIcon = (ImageView)view.findViewById(R.id.thumbnail_provider_app_install);
            mImageViewProvideLogo = (ImageView)view.findViewById(R.id.thumbnail_provider_app);
            mImageViewRentBand = (ImageView)view.findViewById(R.id.thumbnail_rent_band);
            mThumbnailDelete = (ImageView)view.findViewById(R.id.thumbnail_movie_delete_icon);
            mContinueWatchingProgress = (ProgressBar) view.findViewById(R.id.continue_watching_progress);
//            mImageViewRupeeIcon = (ImageView)view.findViewById(R.id.thumbnail_rupee_icon);
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListMovies == null || mListMovies.isEmpty()) return false;
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
            if (this.clickListener != null && mListMovies != null) {
                showRelatedVODListFragment(mListMovies.get(getAdapterPosition()));
            }
        }

    }


    private void showRelatedVODListFragment(CarouselInfoData carouselInfoData) {
        Bundle args = new Bundle();

        CacheManager.setCarouselInfoData(carouselInfoData);
        showCarouselViewAllFragment(args);
    }


    private void showCarouselViewAllFragment(Bundle args) {
        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }
    public static final String getImageLink(CarouselInfoData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.size() > 0 ) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER, APIConstants.IMAGE_TYPE_COVERPOSTER};

        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : carouselData.images) {
//                LoggerD.debugHooqV stbLog("imageItem: imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
//                    LoggerD.debugHooqVstbLog("imageType- " + imageType + " imageItem: imageType- " + imageItem.type);
                    return imageItem.link;
                }
            }
        }

        return null;
    }

}
