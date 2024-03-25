package com.myplex.myplex.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
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

import com.google.android.exoplayer2.ParserException;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.FavouriteRequest;
import com.myplex.api.request.user.FavouriteSectionsListRequest;
import com.myplex.api.request.user.FetchWatchlistFavorites;
import com.myplex.model.CardData;
import com.myplex.model.CardResponseData;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.FavouriteSectionsListResponse;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.fragment.epg.EPGUtil;
import com.myplex.myplex.ui.views.GenericListViewCompoment;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FavouriteFragment extends Fragment {
    RecyclerView genreList;
    FrameLayout content_searchview;
    private FragmentActivity mContext;
    public List<CardData> cardsList = new ArrayList<>();
    private FrameLayout tabAll;
    private TabLayout tabLayout;
    private ProgressBar progressBar, smallProgressBar;
    private TextView noDateText;
    private TextView mGridViewLoadingText;
    private boolean mIsLoadingMoreAvailable = true;
    private boolean mIsLoadingMorePages = false;
    private int mStartIndex = 1;
    private ImageView left_arrow, right_arrow;
    private LinearLayout noFavoritesFoundLayout;
    String tabName;
    Animation animation;
    private String selectedGenre = "";
    private LinearLayoutManager mLinearLayoutManager;
    private MyRecyclerViewAdapter expandableListAdapter;

    public FavouriteFragment(String tabName) {
        tabName = tabName;

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
        View view = inflater.inflate(R.layout.genre_fragment, container, false);
        genreList = view.findViewById(R.id.genre_list);
        mGridViewLoadingText = (TextView) view.findViewById(R.id.grid_footer_text_loading);
        noDateText = view.findViewById(R.id.no_data_text);
        tabAll=view.findViewById(R.id.tabLL);
        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        progressBar = view.findViewById(R.id.progressBar);
        smallProgressBar = view.findViewById(R.id.smallProgressBar);
        left_arrow = (ImageView) view.findViewById(R.id.left_arrow);
        right_arrow = (ImageView) view.findViewById(R.id.right_arrow);
        noFavoritesFoundLayout=view.findViewById(R.id.no_favourites_found_layout);
        left_arrow.setVisibility(View.GONE);
//        genresListAPICall();
        tabLayout.setHorizontalScrollBarEnabled(true);
        genreList.addOnScrollListener(mScrollListener);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mStartIndex = 1;
                mIsLoadingMoreAvailable = true;
                mIsLoadingMorePages = false;
                setData(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    showHideArrows();
                }
            });
        }
        content_searchview = view.findViewById(R.id.content_searchview);
        mLinearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        genreList.setLayoutManager(mLinearLayoutManager);
        left_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout != null) {
                    tabLayout.smoothScrollTo(tabLayout.getScrollX() - tabLayout.getWidth(), 0);
                    showHideArrows();
                }
            }
        });
        right_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout != null) {
                    tabLayout.smoothScrollTo(tabLayout.getScrollX() + tabLayout.getWidth(), 0);
                    showHideArrows();
                }
            }
        });
        return view;
    }

    private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView view, int scrollState) {

        }

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
        mStartIndex = 1;
        mIsLoadingMoreAvailable = true;
        mIsLoadingMorePages = false;
        genresListAPICall();
//        fetchMovieData(selectedGenre);
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // fetchMovieData();
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void showHideArrows() {
        if (tabLayout.getScrollX() <= 30) {
            left_arrow.setVisibility(View.GONE);
        } else {
            left_arrow.setVisibility(View.VISIBLE);
        }
        Log.v("x-axis", "getScrollX " + tabLayout.getScrollX());
        Log.v("x-axis", "getX " + tabLayout.getX());
        Log.v("x-axis", "tabgetWidth  " + tabLayout.getChildAt(0).getMinimumWidth()*3);
        Log.v("x-axis", "screenWidth " + (int)mContext.getResources().getDimension(R.dimen._60sdp)* tabLayout.getTabCount());
        if (tabLayout.getScrollX()+tabLayout.getWidth()  >  (int)mContext.getResources().getDimension(R.dimen._60sdp)* tabLayout.getTabCount() ) {
            right_arrow.setVisibility(View.GONE);
        } else {
            right_arrow.setVisibility(View.VISIBLE);
        }

    }


    private void fetchMovieData(String genre) {
        if (!mIsLoadingMorePages) {
            progressBar.setVisibility(View.VISIBLE);
        }

        FetchWatchlistFavorites.Params params = new FetchWatchlistFavorites.Params("live", "generalInfo,images", mStartIndex, genre,20, APIConstants.FAVOURITES_FETCH_REQUEST);
        FetchWatchlistFavorites fetchWatchlistFavorites = new FetchWatchlistFavorites(params, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                if (isAdded()) {
                    // cardsList.clear();
//                    noDateText.setVisibility(View.GONE);
                    noFavoritesFoundLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    smallProgressBar.setVisibility(View.GONE);
                    mGridViewLoadingText.setVisibility(View.GONE);
                    if (response != null && response.body() != null && response.body().results != null) {
                        List<CardData> dataList = response.body().results;
                        if (!mIsLoadingMorePages && (dataList == null || dataList.isEmpty())) {
//                            noDateText.setVisibility(View.VISIBLE);
                            noFavoritesFoundLayout.setVisibility(View.VISIBLE);
                            genreList.setVisibility(View.GONE);
                            return;
                        }
                        if (dataList == null || dataList.isEmpty()) {
                            if(mStartIndex == 1)
//                                noDateText.setVisibility(View.VISIBLE);
                                noFavoritesFoundLayout.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (dataList.size() < APIConstants.PAGE_INDEX_COUNT) {
                            mIsLoadingMoreAvailable = false;
                        }
                        genreList.setVisibility(View.VISIBLE);
                        if (mIsLoadingMorePages) {
//                            noDateText.setVisibility(View.GONE);
                            noFavoritesFoundLayout.setVisibility(View.GONE);
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
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    smallProgressBar.setVisibility(View.GONE);
//                CustomLog.e("epgfragment","fetch epg failed");
                }

            }
        });
        APIService.getInstance().execute(fetchWatchlistFavorites);
    }

    private void updateData(List<CardData> results) {
        expandableListAdapter = new MyRecyclerViewAdapter(mContext, results);
        genreList.setAdapter(expandableListAdapter);
    }

    public void genresListAPICall() {
        FavouriteSectionsListRequest mRequestFavourites = new FavouriteSectionsListRequest(new APICallback<FavouriteSectionsListResponse>() {
            @Override
            public void onResponse(APIResponse<FavouriteSectionsListResponse> response) {
                if (response == null
                        || response.body() == null) {
                    return;
                }
                if(response.body().getResults()!=null && response.body().getResults().isEmpty()){
                    tabAll.setVisibility(View.GONE);
                    noFavoritesFoundLayout.setVisibility(View.VISIBLE);
                    genreList.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
                if(response.body().getResults() != null && !response.body().getResults().isEmpty()) {
                    tabAll.setVisibility(View.VISIBLE);
                    noFavoritesFoundLayout.setVisibility(View.GONE);
                    List<String> terms = response.body().getResults();
                    //Log.d("Favourite", "FavouriteRequest: onResponse: message - " + response.body().message);
                    tabLayout.removeAllTabs();
                    tabLayout.addTab(tabLayout.newTab().setText(robotoStringFont("All")));
                    for (int i = 0; i < terms.size(); i++) {
                        if (terms.get(i) != null && terms.get(i) != null) {
                            tabLayout.addTab(tabLayout.newTab().setText(robotoStringFont(terms.get(i))));
                        }
                        View root = tabLayout.getChildAt(0);
                        //TODO : enable below code for tab dividers and change color code accordingly
                        if (root instanceof LinearLayout) {
                            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                            GradientDrawable drawable = new GradientDrawable();
                            drawable.setColor(getResources().getColor(R.color.epg_date_tab_divider_color));
                            drawable.setSize(2, 1);
                            ((LinearLayout) root).setDividerPadding(10);
                            ((LinearLayout) root).setDividerDrawable(drawable);
                        }
                    }
                    if (tabLayout.getTabCount() < 5) {
                        right_arrow.setVisibility(View.GONE);
                        left_arrow.setVisibility(View.GONE);
                    } else {
                        left_arrow.setVisibility(View.VISIBLE);
                        right_arrow.setVisibility(View.VISIBLE);
                    }
                    fetchMovieData(selectedGenre);
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

    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_bold.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        public List<CardData> mData;
        private LayoutInflater mInflater;
        private GenericListViewCompoment.ItemClickListener mClickListener;

        // data is passed into the constructor
        MyRecyclerViewAdapter(Context context, List<CardData> data) {
            this.mInflater = LayoutInflater.from(context);
            if(data != null) {
                for (CardData card:
                        data ) {
                    card.isFavourite = true;
                }
            }
            this.mData = data;

        }

        // inflates the row layout from xml when needed
        @Override
        public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.genre_list_item, parent, false);
            return new MyRecyclerViewAdapter.ViewHolder(view);
        }

        CardData carouselData = null;

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            // String animal = mData.get(position);

//            Like animation
            final Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
            CardData cardData = mData.get(position);
            if (cardData.images == null) {
                holder.imageView.setImageResource(R.drawable
                        .black);
            } else {
                String imageLink = cardData.getImageLink(APIConstants.IMAGE_TYPE_THUMBNAIL);
                PicassoUtil.with(mContext).load(imageLink, holder.imageView, R.drawable.black);
            }

            if (cardData.images == null) {
                holder.programIV.setImageResource(R.drawable
                        .black);
            } else {
                String imageLink = Util.getImageLink(cardData);
                PicassoUtil.with(mContext).load(imageLink, holder.programIV, R.drawable.movie_thumbnail_placeholder);
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
            if (cardData.getTitle() != null)
                holder.program_name.setText(cardData.getTitle());
            if (cardData.getTitle() != null)
                holder.program_name_type.setText(cardData.getTitle() + " - " + cardData.getType().substring(0, 1).toUpperCase() + "" + cardData.getType().substring(1));
            if (cardData.globalServiceName != null)
                holder.mTitle.setText(cardData.globalServiceName);
            if(cardData.getDescription() !=null){
                holder.descriptionText.setText(cardData.getDescription());
            }
            if (cardData.isExpand) {
                holder.descriptionText.setVisibility(View.VISIBLE);
                holder.expand_layout.setVisibility(View.VISIBLE);
                holder.program_name.setVisibility(View.GONE);
                holder.iv_like.setVisibility(View.GONE);
                holder.iv_like2.setVisibility(View.VISIBLE);
                holder.view_side.setVisibility(View.GONE);
            } else {
                holder.descriptionText.setVisibility(View.GONE);
                holder.expand_layout.setVisibility(View.GONE);
                holder.program_name.setVisibility(View.VISIBLE);
                holder.iv_like.setVisibility(View.VISIBLE);
                holder.iv_like2.setVisibility(View.GONE);
                holder.view_side.setVisibility(View.VISIBLE);
            }
            if (cardData.isFavourite)
                holder.iv_like.setImageResource(R.drawable.ic_likeborder);
            else
                holder.iv_like.setImageResource(R.drawable.ic_unlikeborder);

            if (cardData.isFavourite)
                holder.iv_like2.setImageResource(R.drawable.ic_likeborder);
            else
                holder.iv_like2.setImageResource(R.drawable.ic_unlikeborder);

            holder.iv_like.setTag(cardData);
            holder.iv_like2.setTag(cardData);
            holder.iv_like_layout.setTag(cardData);
            holder.linear_layout.setTag(cardData);
            holder.play_icon.setTag(cardData);
            holder.programIV.setTag(cardData);
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void add(List<CardData> vodCardList) {
            if(vodCardList != null) {
                for (CardData card:
                        vodCardList ) {
                    card.isFavourite = true;
                }
            }
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
            RelativeLayout iv_like_layout;
            RelativeLayout linear_layout;
            View view_side;

            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageview_thumbnail);
                mTitle = itemView.findViewById(R.id.title);
                mTextCount = itemView.findViewById(R.id.text_count);
                program_name = itemView.findViewById(R.id.program_name);
                timeDuration=itemView.findViewById(R.id.duration_tv);
                programIV = itemView.findViewById(R.id.programIV);
                expand_layout = itemView.findViewById(R.id.expand_layout);
                play_icon = itemView.findViewById(R.id.play_icon);
                iv_like = itemView.findViewById(R.id.iv_like);
                iv_like2 = itemView.findViewById(R.id.iv_like2);
                linear_layout=itemView.findViewById(R.id.linear_layout);
                program_name_type = itemView.findViewById(R.id.program_name_type);
                layout_thumbnail = itemView.findViewById(R.id.layout_thumbnail);
                view_side = itemView.findViewById(R.id.view_side);
                descriptionText = itemView.findViewById(R.id.text_description);
                iv_like_layout=itemView.findViewById(R.id.relative_layout);
                iv_like_layout.setOnClickListener(this);
//                layout_thumbnail.setOnClickListener(this);
                play_icon.setOnClickListener(this);
                programIV.setOnClickListener(this);
                iv_like.setOnClickListener(this);
                iv_like2.setOnClickListener(this);
                linear_layout.setOnClickListener(this);
            }

            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.play_icon:
                        // content_searchview.setVisibility(View.VISIBLE);
                        //  showDetailsPage(getAdapterPosition(), "", mData);
                        CardData cardData = null;
                        if (view.getTag() instanceof CardData) {
                            cardData = (CardData) view.getTag();
                        }
                        showDetailsFragment(cardData, -1, cardData.getTitle(), -1);
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
                        //notifyDataSetChanged();
                        notifyItemChanged(getAbsoluteAdapterPosition());
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
                        showDetailsFragment(cardData1, -1, cardData1.getTitle(), -1);
                        break;
                }

            }
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
                            /*    if (mData.get(position).currentUserData != null) {
                                    mData.get(position).currentUserData.favorite = cardData.currentUserData != null ? !cardData.currentUserData.favorite : false;
                                    notifyDataSetChanged();
                                }*/
                                mData.get(position).isFavourite =!cardData.isFavourite;
                                notifyDataSetChanged();
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

}


