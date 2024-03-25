package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CardDataImagesItem;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.util.Date;
import java.util.List;

/**
 * Created by Phani on 12/12/2015.
 */
public class AdapterMovieList extends BaseAdapter {
    private static final String TAG = AdapterMovieList.class.getSimpleName();
    private Context mContext;
    private List<CardData> mListMovies;
    private String mBgColor;
    private View.OnClickListener mPlayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof CardData) {
                CardData movieData = (CardData) v.getTag();
                showDetailsFragment(movieData);
            }

        }
    };
    private boolean showTitle = true;


    public AdapterMovieList(Context context, List<CardData> moviesList) {
        mContext = context;
        mListMovies = moviesList;
    }

    @Override
    public int getCount() {
        if (mListMovies != null && mListMovies.size() >= 1)
            return mListMovies.size();
        else
            return 0;
    }

    @Override
    public CardData getItem(int position) {
        return mListMovies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void add(List<CardData> vodCardList) {
        if(mListMovies != null){
            mListMovies.addAll(vodCardList);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_movie_list, null, false);
            mViewHolder = new ViewHolder();
            mViewHolder.mThumbnailMovie = (ImageView) convertView.findViewById(R.id.imageview_thumbnail);
            mViewHolder.mImageViewPlayIcon = (ImageView) convertView.findViewById(R.id.thumbnail_movie_play);
            mViewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id
                    .textview_title);
            mViewHolder.mTextViewGenres = (TextView) convertView.findViewById(R.id
                    .textview_genre);
            mViewHolder.mTextViewDescription = (TextView) convertView.findViewById(R.id
                    .textview_description);
//            mViewHolder.mImageViewRupeeIcon = (ImageView) convertView.findViewById(R.id.thumbnail_rupee_icon);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        CardData cardData = mListMovies.get(position);
        if (null == cardData) {
            return convertView;
        }
        String imageLink = getImageLink(cardData);
        if (imageLink == null
                || imageLink.compareTo("Images/NoImage.jpg") == 0) {
            mViewHolder.mThumbnailMovie.setImageResource(R.drawable
                    .movie_thumbnail_placeholder);
        } else if (imageLink != null) {
/*            Picasso.with(mContext).load(imageLink).error(R.drawable
                    .movie_thumbnail_placeholder).placeholder(R.drawable
                    .movie_thumbnail_placeholder).into(mViewHolder.mThumbnailMovie);*/
            PicassoUtil.with(mContext).load(imageLink,mViewHolder.mThumbnailMovie,R.drawable.movie_thumbnail_placeholder);
        }



        if(cardData == null){
            mViewHolder.mImageViewPlayIcon.setVisibility(View.GONE);
        } else {
            mViewHolder.mImageViewPlayIcon.setVisibility(View.VISIBLE);
            if(!Util.isFreeContent(cardData)){
                mViewHolder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_pay_icon);
            } else {
                mViewHolder.mImageViewPlayIcon.setImageResource(R.drawable.thumbnail_play_icon);
            }
        }


        if(null != cardData.generalInfo){
            if (null != cardData.generalInfo.title) {
                mViewHolder.mTextViewTitle.setVisibility(View.VISIBLE);
                mViewHolder.mTextViewTitle.setText(cardData.generalInfo.title);
            }
            if(cardData.generalInfo.description != null){
                mViewHolder.mTextViewDescription.setText(cardData.generalInfo.description);
            }
        }
        if (null != cardData.content){
            StringBuilder genres = new StringBuilder();
            for(CardDataGenre genre: cardData.content.genre){
                if(genres.length() != 0){
                    genres.append("| ");
                }
                genres.append(genre.name);
            }
            if (cardData.content.language != null && cardData.content.language.size() > 0) {
                for (String language : cardData.content.language) {
                    if (genres.length() != 0) {
                        genres.append("| ");
                    }
                    genres.append(language);
                }
            }
            LoggerD.debugHooqVstbLog("genres & languages- " + genres);
            mViewHolder.mTextViewGenres.setText(genres);
            mViewHolder.mTextViewGenres.setVisibility(View.VISIBLE);
        }
        if (!showTitle) {
            mViewHolder.mTextViewTitle.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void showTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public void setBgColor(String mBgColor){
        this.mBgColor=mBgColor;
    }


    public class ViewHolder {
        ImageView mThumbnailMovie, mImageViewPlayIcon;
        TextView mTextViewTitle;
        TextView mTextViewGenres;
        TextView mTextViewDescription;
//        public ImageView mImageViewRupeeIcon;
    }

    public static int getTotalDuration(Date startDate, Date endDate, boolean isTotalDuration) {
        Date date = new Date();
        long diff;
        if (isTotalDuration) {
            diff = endDate.getTime() - startDate.getTime();
        } else {
            diff = date.getTime() - startDate.getTime();
        }
        double diffInHours = diff / ((double) 1000 * 60 * 60);

        int min = (int) Math.round(diffInHours * 60);

        return min;
    }


    public String getImageLink(CardData movie) {
        if(movie == null || movie.images == null || movie.images.values == null || movie.images.values.isEmpty()){
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,APIConstants.IMAGE_TYPE_COVERPOSTER};
        for(String imageType : imageTypes){
            for (CardDataImagesItem imageItem : movie.images.values) {
                //Log.d(TAG, "getImageLink: imageType: " + imageItem.type + " imageLink- " + imageItem.link + " imageprofile: " + imageItem.profile);
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }

    private void showDetailsFragment(CardData movieData) {
        CacheManager.setSelectedCardData(movieData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, movieData._id);
//        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(movieData));
        String partnerId = movieData == null || movieData.generalInfo == null || movieData.generalInfo.partnerId == null ? null : movieData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);

        ((BaseActivity) mContext).showDetailsFragment(args, movieData);
    }

}
