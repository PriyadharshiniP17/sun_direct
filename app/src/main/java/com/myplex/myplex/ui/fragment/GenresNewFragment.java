package com.myplex.myplex.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

//import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ParserException;
import com.google.android.material.imageview.ShapeableImageView;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.FavouriteRequest;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.model.CardData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.MenuDataModel;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.epg.EPGUtil;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GenresNewFragment extends Fragment {
    RecyclerView genreList;
    FrameLayout content_searchview;
    SwipeRefreshLayout mSwipeToRefreshGeners;
    private FragmentActivity mContext;
    public List<CardData> cardsList = new ArrayList<>();

    private ProgressBar progressBar, smallProgressBar;
    private TextView noDateText;
    private TextView mGridViewLoadingText;
    private boolean mIsLoadingMoreAvailable = true;
    private boolean mIsLoadingMorePages = false;
    private int mStartIndex = 1;

    String tabName;
    Animation animation;
    private String selectedGenre = "";
    private LinearLayoutManager mLinearLayoutManager;
    private MyRecyclerViewAdapter expandableListAdapter;
    private String selected_language  ;

    CircleImageView fab_top_scroll;

    public GenresNewFragment() {

    }
    public GenresNewFragment(String tabName) {
        this.tabName = tabName;

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
        animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
        View view = inflater.inflate(R.layout.genre_new_fragment, container, false);
        genreList = view.findViewById(R.id.genre_list);
        mSwipeToRefreshGeners = view.findViewById(R.id.swipe_to_refresh_geners);
        mGridViewLoadingText = (TextView) view.findViewById(R.id.grid_footer_text_loading);
        noDateText = view.findViewById(R.id.no_data_text);

        progressBar = view.findViewById(R.id.progressBar);
        smallProgressBar = view.findViewById(R.id.smallProgressBar);
        fab_top_scroll=view.findViewById(R.id.fab_top_scrollf);

//        genresListAPICall();

        genreList.addOnScrollListener(mScrollListener);
        mSwipeToRefreshGeners.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mStartIndex = 1;
                mIsLoadingMoreAvailable = true;
                mIsLoadingMorePages = false;
                fetchMovieData(selectedGenre);
            }
        });
        content_searchview = view.findViewById(R.id.content_searchview);
        mLinearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        genreList.setLayoutManager(mLinearLayoutManager);


        fab_top_scroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // genreList.scrollToPosition(0);
                genreList.getLayoutManager().smoothScrollToPosition(genreList,new RecyclerView.State(), 0);
                showTopNavigateArrow(false);
                ((MainActivity)requireActivity()).updateBottomBar(true, 0);
                ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);

            }
        });

        return view;
    }

    private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView view, int scrollState) {
            LinearLayoutManager myLayoutManager = (LinearLayoutManager) view.getLayoutManager();
            int startScrollPosition = myLayoutManager.findFirstVisibleItemPosition();
            int endScrollPosition = myLayoutManager.findLastVisibleItemPosition();
            showTopNavigateArrow((endScrollPosition >= 15));
            try {
                if (endScrollPosition > mLastFirstVisibleItem) {
                    if (DeviceUtils.isTablet(mContext)){
                        if (((MainActivity)requireActivity()).IsScrolled1st){
                            ((MainActivity)requireActivity()).updateBottomBar(false, 2);
                        }else if (!((MainActivity)requireActivity()).IsScrolled1st){
                            ((MainActivity)requireActivity()).updateBottomBar(false, 0);
                        }
                    }else{
                        ((MainActivity)requireActivity()).updateBottomBar(false, 0);
                    }
                    Log.i("a", "scrolling down...");
                } else if (endScrollPosition < mLastFirstVisibleItem) {
                    ((MainActivity)requireActivity()).updateBottomBar(true, 0);
                    ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);
                    Log.i("a", "scrolling up...");
                }else
                    ((MainActivity)requireActivity()).updateBottomBar(true, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mLastFirstVisibleItem = endScrollPosition;
        }
        int mLastFirstVisibleItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int visibleItemCount = recyclerView.getChildCount();
            int totalItemCount = mLinearLayoutManager.getItemCount();
            int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                if (!mIsLoadingMorePages) {
                    if (!mIsLoadingMoreAvailable) {
                        return;
                    }
                    mIsLoadingMorePages = true;
                    mStartIndex++;
                    mGridViewLoadingText.setVisibility(View.VISIBLE);
                    smallProgressBar.setVisibility(View.VISIBLE);
                    fetchMovieData(selectedGenre);
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //When we come back from the subscription error then the list is scrolling to top ,not
        // staying in the last position where user stopped the scrolling
        if(APIConstants.IS_REFRESH_LIVETV1){
            progressBar.setVisibility(GONE);
            smallProgressBar.setVisibility(View.GONE);
            mStartIndex = 1;
            mIsLoadingMoreAvailable = true;
            mIsLoadingMorePages = false;
            if(tabName != null) setData(tabName);
           /* mStartIndex = 1;
            mIsLoadingMoreAvailable = true;
            mIsLoadingMorePages = false;
            fetchMovieData(tabName);*/
//            APIConstants.IS_REFRESH_LIVETV=false;
            return;
        }


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // fetchMovieData();
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void startToScroll() {
        if (genreList != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    genreList.scrollToPosition(0);
                    fab_top_scroll.setVisibility(GONE);
                }
            }, 200);

        }
    }

    private void showTopNavigateArrow(boolean isShow){
        if(isShow){
            fab_top_scroll.setVisibility(VISIBLE);
        }else {
            fab_top_scroll.setVisibility(GONE);
        }
    }

    private void fetchMovieData(String genre) {
        if (!mIsLoadingMorePages) {
            progressBar.setVisibility(View.VISIBLE);
        }
        myplexAPI.clearCache(APIConstants.BASE_URL);
        myplexAPISDK.ENABLE_FORCE_CACHE = true;
        MenuDataModel.clearCache();
        ApplicationController.DATE_POSITION = 0;
        //  List<String> list = Util.generateEpgTable(ApplicationController.ENABLE_BROWSE_PAST_EPG);
        // final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        Date date = new Date();
        boolean isDVROnly = (PrefUtils.getInstance().getPrefEnablePastEpg() && ApplicationController.DATE_POSITION - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0) ? true : false;
        //Log.e("time.......", list.get(0));
        Log.e("time date.......", "" + date.getTime());
        Log.e("time hours.......", "" + Util.convertDate(date.getTime()));
        // Log.e("time getServerDateFormat.......", ""+Util.getServerDateFormat(list.get(0), date));

        com.myplex.myplex.model.EPG.getInstance(date).findPrograms(10,
                Util.getServerDateFormat(Util.convertDate(date.getTime()), date),
                Util.convertDate(date.getTime()), date, mStartIndex,
                true, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly,
                genre,
                new com.myplex.myplex.model.EPG.CacheManagerCallback() {
                    @Override
                    public void OnlineResults(List<CardData> dataList, int pageIndex) {
                        if (isAdded()) {
                            // cardsList.clear();
                            mSwipeToRefreshGeners.setRefreshing(false);
                            noDateText.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            smallProgressBar.setVisibility(View.GONE);
                            mGridViewLoadingText.setVisibility(View.GONE);
                            if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
                                noDateText.setVisibility(View.VISIBLE);
                                genreList.setVisibility(View.GONE);
                                return;
                            }
                            if ((dataList == null || dataList.isEmpty())) {
                                if(mStartIndex == 1)
                                    noDateText.setVisibility(View.VISIBLE);
                                return;
                            }
                            if (dataList.size() < APIConstants.PAGE_INDEX_COUNT) {
                                mIsLoadingMoreAvailable = false;
                            }
                            genreList.setVisibility(View.VISIBLE);
                            if (mIsLoadingMorePages) {
                                noDateText.setVisibility(View.GONE);
                                mIsLoadingMorePages = false;
                                if (expandableListAdapter != null) {
                                    expandableListAdapter.add(dataList);
                                    expandableListAdapter.notifyDataSetChanged();
                                }
                            } else {
                                updateData(dataList);
                            }
                        }
                    }


                    @Override
                    public void OnlineError(Throwable error, int errorCode) {
                        if (isAdded()) {
                            mSwipeToRefreshGeners.setRefreshing(false);
                            progressBar.setVisibility(View.GONE);
                            smallProgressBar.setVisibility(View.GONE);
                            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            }
//                CustomLog.e("epgfragment","fetch epg failed");
                        }

                    }
                });
    }

    private void updateData(List<CardData> results) {
        expandableListAdapter = new MyRecyclerViewAdapter(mContext, results);
        genreList.setAdapter(expandableListAdapter);
        ((MainActivity)requireActivity()).updateBottomBar(true, 0);
    }



    public void setData(String genresName) {
        if (genresName.equalsIgnoreCase("All")) {
            selectedGenre = "";
            fetchMovieData("");
        } else {
            selectedGenre = genresName;
            fetchMovieData(genresName);
        }
    }

    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<GenresNewFragment.MyRecyclerViewAdapter.ViewHolder> {

        public List<CardData> mData;
        private LayoutInflater mInflater;
        Typeface regular,bold;
        private GenericListViewCompoment.ItemClickListener mClickListener;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<CardData> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public GenresNewFragment.MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.genre_list_item, parent, false);
            return new GenresNewFragment.MyRecyclerViewAdapter.ViewHolder(view);
        }

        CardData carouselData = null;

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(GenresNewFragment.MyRecyclerViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            // String animal = mData.get(position);

//            Like animation
            final Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
            CardData cardData = mData.get(position);
            regular=Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_regular.ttf");
            bold=Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");
            if (cardData.images == null) {
                holder.imageView.setImageResource(R.drawable.black);
            } else {
                String imageLink = cardData.getImageLink(APIConstants.IMAGE_TYPE_ICON);
                Glide.with(holder.imageView.getContext()).load(imageLink).placeholder(R.drawable.black).into(holder.imageView);
//                PicassoUtil.with(mContext).load(imageLink, holder.imageView, R.drawable.black);
            }

            if (cardData.images == null) {
                holder.programIV.setImageResource(R.drawable.black);
            } else {
                String imageLink = Util.getImageLink(cardData);
                PicassoUtil.with(mContext).load(imageLink, holder.programIV, R.drawable.black);
            }

            if (cardData != null && cardData.content != null && cardData.content.channelNumber != null && !cardData.content.channelNumber.isEmpty())
                holder.mTextCount.setText(cardData.content.channelNumber);

            long startDate = 0;
            try {
                startDate = Util.parseXsDateTime(cardData.startDate);
                EPGUtil.getShortTime(startDate);
                String startTime=EPGUtil.getShortTime(startDate);
                long endDate=Util.parseXsDateTime(cardData.endDate);
                EPGUtil.getShortTime(endDate);
                String endTime=EPGUtil.getShortTime(endDate);
                holder.timeDuration.setText(startTime+"  "+"-"+"  "+endTime);

            } catch (ParserException e) {
                e.printStackTrace();
            }
            if (cardData.getTitle() != null) {
                holder.program_name.setText((cardData.getTitle()));
                holder.program_name.setTypeface(regular);
            }
            if (cardData.getTitle() != null)
                holder.program_name_type.setText(cardData.getTitle() + " - " + cardData.getType().substring(0, 1).toUpperCase() + "" + cardData.getType().substring(1));
            if (cardData.globalServiceName != null) {
                holder.mTitle.setText(cardData.globalServiceName);
                holder.mTitle.setTypeface(bold);
            }
            if(cardData.getDescription() != null){
                holder.descriptionText.setText(cardData.getDescription());
            }
            if (cardData.isExpand) {
                holder.expand_layout.setVisibility(View.VISIBLE);
                holder.descriptionText.setVisibility(View.VISIBLE);
                holder.program_name.setVisibility(View.GONE);
                holder.iv_like.setVisibility(View.INVISIBLE);
                holder.iv_like2.setVisibility(View.VISIBLE);
                holder.view_side.setVisibility(View.GONE);
//                holder.lav_like.setVisibility(GONE);

                //   holder.mTitle.setPadding(100,0,0,20);
            } else {
                holder.expand_layout.setVisibility(View.GONE);
                holder.program_name.setVisibility(View.VISIBLE);
                holder.iv_like.setVisibility(View.VISIBLE);
                holder.iv_like2.setVisibility(View.GONE);
                holder.descriptionText.setVisibility(View.GONE);
                holder.view_side.setVisibility(View.VISIBLE);
            }

            if (cardData.currentUserData != null && cardData.currentUserData.favorite) {
                holder.iv_like.setColorFilter(null);
                holder.iv_like.setImageResource(R.drawable.ic_likeborder);
                holder.iv_like.getDrawable().setTintList(null);
//                holder.lav_like.setVisibility(VISIBLE);

            }
            else {
                holder.iv_like.setImageResource(R.drawable.ic_unlikeborder);
                holder.iv_like.setColorFilter(SDKUtils.getColor(mContext,R.color.light_grey), PorterDuff.Mode.MULTIPLY);
//                holder.lav_like.setVisibility(GONE);
            }
            if (cardData.currentUserData != null && cardData.currentUserData.favorite)
                holder.iv_like2.setImageResource(R.drawable.ic_likeborder);
            else
                holder.iv_like2.setImageResource(R.drawable.ic_unlikeborder);

            holder.iv_like.setTag(cardData);
            holder.iv_like_layout.setTag(cardData);
            holder.linear_layout.setTag(cardData);
            holder.iv_like2.setTag(cardData);
            holder.play_icon.setTag(cardData);
            holder.programIV.setTag(cardData);
            if(mData != null && mData.size()-1 == position) {
                // LoggerD.debugLog("position..." + position);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, (int)mContext.getResources().getDimension(R.dimen.margin_gap_84));
                holder.mainLayout.setLayoutParams(params);
            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 0);
                holder.mainLayout.setLayoutParams(params);
            }
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void add(List<CardData> vodCardList) {
            if (mData != null) {
                mData.addAll(vodCardList);
                notifyDataSetChanged();
            }
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView imageView, play_icon, iv_like, iv_like2;
            ShapeableImageView programIV;
            TextView mTitle, program_name, program_name_type, mTextCount,timeDuration,descriptionText;
            RelativeLayout expand_layout;
            LinearLayout layout_thumbnail;
            LinearLayout mainLayout;
            RelativeLayout linear_layout;
            RelativeLayout iv_like_layout;
            View view_side;
//            LottieAnimationView lav_like;

            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageview_thumbnail);
                mTitle = itemView.findViewById(R.id.title);
                mTextCount = itemView.findViewById(R.id.text_count);
                program_name = itemView.findViewById(R.id.program_name);
                programIV = itemView.findViewById(R.id.programIV);
                timeDuration=itemView.findViewById(R.id.duration_tv);
                expand_layout = itemView.findViewById(R.id.expand_layout);
                play_icon = itemView.findViewById(R.id.play_icon);
                iv_like = itemView.findViewById(R.id.iv_like);
                iv_like2 = itemView.findViewById(R.id.iv_like2);
                program_name_type = itemView.findViewById(R.id.program_name_type);
                layout_thumbnail = itemView.findViewById(R.id.layout_thumbnail);
                mainLayout = itemView.findViewById(R.id.main_layout);
                linear_layout=itemView.findViewById(R.id.linear_layout);
                iv_like_layout=itemView.findViewById(R.id.relative_layout);
                iv_like_layout.setOnClickListener(this);
                view_side = itemView.findViewById(R.id.view_side);
                descriptionText = itemView.findViewById(R.id.text_description);
//                lav_like = itemView.findViewById(R.id.lav_like);
//                layout_thumbnail.setOnClickListener(this);
                linear_layout.setOnClickListener(this);
                play_icon.setOnClickListener(this);
                programIV.setOnClickListener(this);
                iv_like.setOnClickListener(this);
                iv_like2.setOnClickListener(this);
            }

            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.play_icon:
                        // content_searchview.setVisibility(View.VISIBLE);
                        CardData cardData = null;
                        if (view.getTag() instanceof CardData) {
                            cardData = (CardData) view.getTag();
                        }
                        if(cardData!=null ) {
                            /*if (cardData.globalServiceId != null)
                                getEPGData(cardData.globalServiceId);
                            else if (cardData._id != null) {*/
                                if (cardData.isLive()) {
                                    getEPGData(cardData._id);
                                } else {
                                    showDetailsFragment(cardData);
                                }
                          /*  }*/
                        }
                        //  showDetailsPage(getAdapterPosition(), "", mData);
                        break;
                    /*case R.id.programIV:
                      //  showDetailsPage(getAdapterPosition(), "", mData);
                        CardData cardData1 = null;
                        if (view.getTag() instanceof CardData) {
                            cardData1 = (CardData) view.getTag();
                        }
                        showDetailsFragment(cardData1, -1, cardData1.getTitle(), -1);
                        break;*/
                    case R.id.layout_thumbnail:
                        for (int i = 0; i < mData.size(); i++) {
                            if (i == getAbsoluteAdapterPosition()) {
                                if (mData.get(i).isExpand)
                                    mData.get(i).isExpand = false;
                                else
                                    mData.get(i).isExpand = true;
                            } else
                                mData.get(i).isExpand = false;
                        }
                        notifyDataSetChanged();
                        // notifyItemChanged(getAbsoluteAdapterPosition());
                        break;

                    case R.id.expand_layout:
                        iv_like.setVisibility(View.GONE);
                        program_name.setVisibility(View.GONE);
                        break;
                    case R.id.iv_like:
                        iv_like2.startAnimation(animation);
                        if (view.getTag() instanceof CardData) {
                            carouselData = (CardData) view.getTag();
                        }
//                        lav_like.setVisibility(VISIBLE);
//                        lav_like.playAnimation();
                        favouriteAPICall(getAdapterPosition(), carouselData);
                        break;
                    case R.id.iv_like2:
                        iv_like2.startAnimation(animation);
                        if (view.getTag() instanceof CardData) {
                            carouselData = (CardData) view.getTag();
                        }
                        favouriteAPICall(getAdapterPosition(), carouselData);
                        break;
                    case R.id.relative_layout:
                        iv_like2.startAnimation(animation);
                        if (view.getTag() instanceof CardData) {
                            carouselData = (CardData) view.getTag();
                        }
                        favouriteAPICall(getAdapterPosition(), carouselData);
                        break;
                    case R.id.linear_layout:
                        CardData cardData1 = null;
                        if (view.getTag() instanceof CardData) {
                            cardData1 = (CardData) view.getTag();
                        }
                        if(cardData1!=null ) {
                            if (cardData1.globalServiceId != null)
                                getEPGData(cardData1.globalServiceId);
                            else if (cardData1._id != null) {
                                if (cardData1.isLive()) {
                                    getEPGData(cardData1._id);
                                } else {
                                    showDetailsFragment(cardData1);
                                }
                            }
                        }
                        break;
                }

            }
        }

        public void getEPGData(String contentId){
        /*final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String selectedDateInString = format.format(date);
        String dateStamp = Util.getYYYYMMDD(selectedDateInString);*/
            ChannelListEPG.Params params = new ChannelListEPG.Params(contentId, "", false, false);
            ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
                @Override
                public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                    if (response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size() > 0) {
                        for (int i = 0; i < response.body().getResults().size(); i++) {
                            List<CardData> cardDataList = response.body().getResults().get(i).getPrograms();
                            if(cardDataList.size() >0){
                                showDetailsFragment(cardDataList.get(0));
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t, int errorCode) {
                    Log.d("Favourite", "onFailure: ");
                }
            });
            APIService.getInstance().execute(channelListEPG);
        }
        private void showDetailsFragment(CardData carouselData) {

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
                //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
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

     /*   if (!APIConstants.LAYOUT_TYPE_CONTINUE_WATCHING.equalsIgnoreCase(carouselInfoData.layoutType)) {
            if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_LIVE.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)
                    && !carouselData.isTVSeries()
                    && !carouselData.isVODChannel()
                    && !carouselData.isVODYoutubeChannel()
                    && !carouselData.isVODCategory()
                    && !carouselData.isTVSeason()) {
                args.putSerializable(CardDetails.PARAM_QUEUE_LIST_CARD_DATA, (Serializable) carouselInfoData.listCarouselData);
            }
        }*/

            String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
            args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
            args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
            //    args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
            //  args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
            args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
            ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
        }
        public void favouriteAPICall(int position, CardData cardData) {
            FavouriteRequest.Params favouritesParams = new FavouriteRequest.Params(carouselData.globalServiceId, "live");
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
                                if (response.message() != null)
                                    Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Log.d("Favourite", "FavouriteRequest: onResponse: message - " + response.body().message);
                            if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                                //showFavaouriteButton(true);
                                if (position>=0 && mData.get(position).currentUserData != null) {
                                    mData.get(position).currentUserData.favorite = cardData.currentUserData != null ? !cardData.currentUserData.favorite : false;
                                    notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            Log.d("Favourite", "FavouriteRequest: onResponse: t- " + t);
                            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                                return;
                            }
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                        }
                    });
            APIService.getInstance().execute(mRequestFavourites);

           /* ContentLikedRequest.Params likedContentParams = new ContentLikedRequest.Params(carouselData.globalServiceId, carouselData.getType(), cardData.currentUserData!= null ?cardData.currentUserData.favorite : false);

            ContentLikedRequest mRequestLikedContent = new ContentLikedRequest(likedContentParams, (APICallback) (new APICallback() {
                public void onResponse(@Nullable APIResponse response) {
                    if (isAdded()) {
                        if (response != null && response.body() != null) {
                            Object var10001 = response.body();
                            Intrinsics.checkNotNull(var10001);
                            if (StringsKt.equals("SUCCESS", ((FavouriteResponse) var10001).status, true)) {
                                var10001 = response.body();
                                Intrinsics.checkNotNull(var10001);
                                if (StringsKt.equals("movie is liked", ((FavouriteResponse) var10001).message, true)) {


                                }
                                if (mData.get(position).currentUserData != null)
                                    mData.get(position).currentUserData.favorite = cardData.currentUserData != null ? !cardData.currentUserData.favorite : false;
                                notifyDataSetChanged();
                            }

                        }
                    }
                }

                public void onFailure(@Nullable Throwable t, int errorCode) {
                    Log.d(myplexAPI.TAG, "ContentLikedRequest: onFailure: t- " + t);
                    if (isAdded()) {
                        if (errorCode == -300) {
                            AlertDialogUtil.showToastNotification((CharSequence) mContext.getResources().getString(R.string.network_error));
                        }
                    }
                }
            }));
            APIService.getInstance().execute((APIRequest) mRequestLikedContent);*/
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
            //  Log.d("DetailsPAgeDialogFragment", "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
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

        //  ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
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

    private void showDetailsPage(int selectedPosition, String categoryTitle, List<CardData> carouselInfoDataList) {
        if (DetailsPageDialogFragment.getInstance() != null) {
            DetailsPageDialogFragment.getInstance().dismiss();
        }
       /* Fragment fragment = new DetailsPageDialogFragment(selectedPosition,categoryTitle,carouselInfoDataList);
        FragmentManager fm = ((AppCompatActivity)mContext).getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(fragment.getClass().getSimpleName());
        ft.replace(R.id.content_searchview, fragment);
        ft.commit();*/
        DialogFragment newFragment = DetailsPageDialogFragment.newInstance(mContext);
        ((DetailsPageDialogFragment) newFragment).setData(selectedPosition, categoryTitle, carouselInfoDataList);
        newFragment.show(getFragmentManager(), "dialog");

     /*   WindowManager.LayoutParams lp = newFragment.getDialog().getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        DetailsPageDialogFragment.getInstance().getDialog().getWindow().setAttributes(lp);*/
        // newFragment.getDialog().getWindow().setAttributes(lp);
    }

    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        if(spanString.length()>0) {
            spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_bold.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return spanString;
    }






}


