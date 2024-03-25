package com.myplex.myplex.ui.fragment;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.events.EventSearchMovieDataOnOTTApp;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.ui.views.NestedCarouselItem;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryFragment extends Fragment {
    RecyclerView recyclerView;
    private FragmentActivity mContext;
    private List<CardData> cardsList;
    CarouselInfoData mCarouselInfoData;


    public CategoryFragment(CarouselInfoData carouselInfoData){
        mCarouselInfoData = carouselInfoData;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getActivity();
        View view= inflater.inflate(R.layout.category_fragment, container, false);
        recyclerView=view.findViewById(R.id.category_list);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        //MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(mContext, cardsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        CarouselInfoData carouselData = mCarouselInfoData;
        startAsyncTaskInParallel(new CarouselRequestTask1(carouselData.name, carouselData.pageSize > 0 ? carouselData.pageSize : APIConstants.PAGE_INDEX_COUNT,  carouselData.modified_on));
        //recyclerView.setAdapter(adapter);
        return view;
    }



    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<CardData> mData;
        private LayoutInflater mInflater;
        //private OnItemClickListener mClickListener;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<CardData> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.list_dummy, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }

        public  String getImageLink(CardData carouselData) {
            if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
                return null;
            }
            String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER};
            String profile = ApplicationConfig.XXHDPI;
            if (carouselData != null
                    && carouselData.generalInfo != null
                    && carouselData.generalInfo.type != null
                    && APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)) {
                profile = ApplicationConfig.XHDPI;
                imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER, APIConstants.IMAGE_TYPE_COVERPOSTER,
                        APIConstants.IMAGE_TYPE_THUMBNAIL};
            }

            for (String imageType : imageTypes) {
                for (CardDataImagesItem imageItem : carouselData.images.values) {
                    LoggerD.debugDownload("getImageLink for download item type- " + imageItem.type + " profile- " + imageItem.profile + " link- " + imageItem.link);
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && profile.equalsIgnoreCase(imageItem.profile)) {
                        return imageItem.link;
                    }
                }
            }

            return null;
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
            // String animal = mData.get(position);
            CardData cardData = mData.get(position);
            if (cardData.images == null) {
                holder.imageView.setImageResource(R.drawable
                        .movie_thumbnail_placeholder);
            } else {
                String  imageLink  = getImageLink(cardData);
                PicassoUtil.with(mContext).load(imageLink, holder.imageView, R.drawable.movie_thumbnail_placeholder);
            }
            holder.imageView.setTag(cardData);
            if(cardData.getChannelName() != null)
                holder.mTitle.setText(cardData.getChannelName());

            if(cardData.content != null && cardData.content.channelNumber != null && !cardData.content.channelNumber.isEmpty())
                holder.mChannelNumber.setText(cardData.content.channelNumber);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    CarouselInfoData mCarouselInfoData = CacheManager.getCarouselInfoData();
                    CardData cardData = null;


                    if (view.getTag() instanceof CardData) {
                        cardData = (CardData) view.getTag();
                    }
                    if (cardData == null) {
                        return;
                    }
//                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    String publishingHouse = cardData == null
                            || cardData.publishingHouse == null
                            || TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName) ? null : cardData.publishingHouse.publishingHouseName;
                    if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                        HungamaPartnerHandler.launchDetailsPage(cardData, mContext, mCarouselInfoData, null);
                        return;
                    }
                    if (cardData != null
                            && cardData.generalInfo != null
                            && APIConstants.TYPE_SPORTS.equalsIgnoreCase(cardData.generalInfo.type)
                            && !TextUtils.isEmpty(cardData.generalInfo.deepLink)) {
                        mContext.startActivity(LiveScoreWebView.createIntent(mContext, cardData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, cardData.generalInfo.title));
                        return;
                    }
                    showDetailsFragment(cardData, -1, mCarouselInfoData.title,position);
                }
            });
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder  {
            public View cardView;
            ImageView imageView;
            TextView mTitle;
            TextView mChannelNumber;

            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.titleImage);
                mTitle = itemView.findViewById(R.id.title);
                mChannelNumber = itemView.findViewById(R.id.channel_number);
            }

        }
    }

    private void showDetailsFragment(CardData carouselData, int position,String carousalTitle,int parentPosition) {

        CacheManager.setSelectedCardData(carouselData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
            ////Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, carouselData);
        }
        if (carouselData != null
                && carouselData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                args.putString(CardDetails.PARAM_CARD_ID, carouselData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != carouselData.startDate
                        && null != carouselData.endDate) {
                    Date startDate = Util.getDate(carouselData.startDate);
                    Date endDate = Util.getDate(carouselData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY_MINIMIZED, false);
                    }
                }
            }
        }

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carousalTitle);
      /*  if (position == EventSearchMovieDataOnOTTApp.TYPE_FROM_SEARCH_DATA) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            if (searchMovieData != null) {
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, searchMovieData.getSearchString());
            }
        }*/
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }

    private void startAsyncTaskInParallel(CarouselRequestTask1 task) {

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CarouselRequestTask1 extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        private final String mPageName;
        //private int mPosition;
        private int mCount;
        private String modifiedOn;

        public CarouselRequestTask1(String pageName, int count, String modifiedOn) {
            mPageName = pageName;
           // mPosition = position;
            mCount = count;
            this.modifiedOn = modifiedOn;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            boolean isCacheRequest = true;
            String imageType;
            if (DeviceUtils.isTablet(mContext)) {
                imageType = "hdpi";
            } else {
                imageType = "hdpi";
            }

            new MenuDataModel().fetchCarouseldata(mContext, mPageName, 1, mCount > 0 ? mCount : APIConstants.PAGE_INDEX_COUNT, isCacheRequest, modifiedOn, new MenuDataModel.CarouselContentListCallback() {
                @Override
                public void onCacheResults(List<CardData> dataList) {
                    if(isAdded()) {
                        LoggerD.debugLogAdapter("OnCacheResults: name- " + mPageName);
                        if (dataList != null && !dataList.isEmpty()) {
                            // addCarouselData(dataList, mPosition);
                            // addCarouselData(dataList, mPosition);
                            ((BaseActivity) mContext).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    recyclerView.setAdapter(new MyRecyclerViewAdapter(mContext, dataList));
                                }
                            });
                        }
                    }
                }

                @Override
                public void onOnlineResults(List<CardData> dataList) {
                    if(isAdded()) {
                        LoggerD.debugLogAdapter("OnCacheResults: name- " + mPageName);

                        if (dataList != null && !dataList.isEmpty()) {
                            // addCarouselData(dataList, mPosition);
                            recyclerView.setAdapter(new MyRecyclerViewAdapter(mContext, dataList));
                        }
                    }
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {

                }
            });
            return null;
        }

        protected void onPreExecute() {
            // Perform setup - runs on user interface thread
        }

        protected void onPostExecute(Void result) {
            // Update user interface
        }
    }
}

