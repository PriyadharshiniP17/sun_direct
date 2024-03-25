package com.myplex.myplex.ui.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;

import java.util.List;

import static com.myplex.api.APIConstants.IMAGE_TYPE_SQUARE_BANNER;

/**
 * Created by ramaraju on 4/22/2017.
 */

public class AdapterSquareItemBig extends RecyclerView.Adapter<AdapterSquareItemBig.CarouselDataViewHolder> {

    private static final String TAG = AdapterSquareItemBig.class.getSimpleName();
    private boolean mIsDummyData = false;
    private final Context mContext;
    private List<CardData> mListMovies;
    private boolean showViewAllText;
    public void setParentPosition(int mParentPosition) {
        this.mParentPosition = mParentPosition;
    }
    private int mParentPosition;
    private boolean isfromSearch;

    //adapter view click listener
    public AdapterSquareItemBig(Context context, List<CardData> itemList,boolean showViewAllText) {
        mContext = context;
        mListMovies = itemList;
        this.showViewAllText = showViewAllText;
    }

    public AdapterSquareItemBig(Context context, List<CardData> itemList, boolean isDummyData, boolean showViewAllText, boolean isFrommSearch) {
        mContext = context;
        mListMovies = itemList;
        mIsDummyData = isDummyData;
        this.showViewAllText = showViewAllText;
        this.isfromSearch = isFrommSearch;

    }

    public boolean isContainingDummies(){
        return mIsDummyData;
    }

    public void setData(List<CardData> listMovies) {
        if (listMovies == null) {
            return;
        }
        mIsDummyData = false;
        mListMovies = listMovies;
        notifyDataSetChanged();
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
        CarouselDataViewHolder customItemViewHolder;
        View view = LayoutInflater.from(mContext).inflate(R.layout.square_bigitem, parent, false);
        ImageView thumbnailMovie = view.findViewById(R.id.thumbnail_movie);
        if (DeviceUtils.isTablet(mContext)) {
            Log.d("RESIZE_TAG", "RESIZED");
            int deviceWidth = ApplicationController.getApplicationConfig().screenWidth;
            int paddingRight = (int) UiUtil.convertDpToPixel(12f, mContext);
            int width = deviceWidth / 4 - paddingRight;
            int height =   width ;
            ViewGroup.LayoutParams layoutParams = thumbnailMovie.getLayoutParams();
            layoutParams.height = height;
            layoutParams.width = width;
            thumbnailMovie.requestLayout();
        }
        customItemViewHolder = new CarouselDataViewHolder(view);
        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        //Log.d(TAG,"onBindViewHolder" + position);
            bindGenreViewHolder(holder, mListMovies.get(position));
    }

    private void bindGenreViewHolder(CarouselDataViewHolder holder, final CardData carouselData) {
        if (carouselData != null) {
            if (carouselData.generalInfo != null
                    && carouselData.generalInfo.title != null) {
                String languageFromServer = null;
                if (carouselData.content != null && carouselData.content.language != null && carouselData.content.language.size()>0 )
                    languageFromServer = carouselData.content.language.get(0);
                String text = carouselData.generalInfo.title;
                holder.mTextViewMovieTitle.setText(text);
            }
            String imageLink;
            if (isfromSearch)
                imageLink = gethdmiImageLink(carouselData);
            else
                imageLink = getImageLink(carouselData);
            if (imageLink == null
                    || imageLink.compareTo("Images/NoImage.jpg") == 0) {
                holder.mImageViewMovies.setImageResource(R.drawable
                        .movie_thumbnail_placeholder);
            } else {
                PicassoUtil.with(mContext).load(imageLink, holder.mImageViewMovies, R.drawable.movie_thumbnail_placeholder);
            }
        }
        holder.setClickListener(mOnItemClickListenerWithData);
    }

    private String gethdmiImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_SQUARE};
        if (carouselData.generalInfo != null && (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type) || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type))) {
            imageTypes = new String[]{APIConstants.COVERPOSTER, APIConstants.COVERPOSTER};
        }

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if (DeviceUtils.isTablet(mContext)) {
                    if (imageType.equalsIgnoreCase(imageItem.type) && ApplicationConfig.HDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }else if(imageType.equalsIgnoreCase(imageItem.type) && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type) && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }
        }
        return null;
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

    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    class CarouselDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemClickListenerWithData clickListener;
        private final ImageView mImageViewMovies;
        private final TextView mTextViewMovieTitle;

        public CarouselDataViewHolder(View view) {
            super(view);
            mTextViewMovieTitle = view.findViewById(R.id.movie_title_big_item);
            mImageViewMovies = view.findViewById(R.id.thumbnail_movie);
            if (mImageViewMovies!=null){
                mImageViewMovies.setOnClickListener(this);
            }
            if (mTextViewMovieTitle!=null){
                mTextViewMovieTitle.setOnClickListener(this);
            }
            view.setOnClickListener(this);
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
        recyclerView.getRecycledViewPool().setMaxRecycledViews(TYPE_ITEM, 100);
        recyclerView.setItemViewCacheSize(100);
    }

    private ItemClickListenerWithData mOnItemClickListenerWithData;
    public void setOnItemClickListenerWithMovieData(ItemClickListenerWithData onItemClickListenerWithData) {
        this.mOnItemClickListenerWithData = onItemClickListenerWithData;
    }

    private String getImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_SQUARE,IMAGE_TYPE_SQUARE_BANNER};
        if(carouselData.generalInfo != null && (APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type) || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type))){
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_SQUARE,APIConstants.IMAGE_TYPE_SQUARE};
        }

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if(DeviceUtils.isTablet(mContext)){
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            &&  ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
                else{
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }
        }
        return null;
    }
}
