package com.myplex.myplex.ui.component;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.tabs.TabLayout;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.EpisodeDisplayData;
import com.myplex.model.ShowDisplayTabs;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.EpisodeItemAdapter;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsPlayer;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.utils.Util;
import com.myplex.myplex.utils.WeakRunnableTwo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.myplex.myplex.ui.fragment.CardDetails.PARAM_RELATED_CARD_DATA;

public class SeasonTabFragment extends BaseFragment {

    List<CardData> mListCarouselInfoData;
    Context mContext;
    RecyclerView mRecyclerViewCarousel;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final CacheManager mCacheManager = new CacheManager();
    private int mTVEpisodesListStartIndex = 1;
    private boolean isLoadMoreRequestInProgress;
    private boolean mIsLoadingMoreAvailable;
    private ArrayList<CardData> mListEpisodes;
    private CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    EpisodeItemAdapter episodeItemAdapter;
    private static final String TAG = EpisodeTabsAdapter.class.getSimpleName();
    private String mCurrentSeasonName;
    private CardData mTVShowCardData;
    private boolean isToExpand;
    private int expandPosition;
    private String mSearchQuery;
    private String sourceDetails,source;
    private List<DetailsViewContent.DetailsViewDataItem> mValues;
    private int position;
    int startIndex = 1;
    EpisodeDisplayData seasonsData;
    String mBgColor;
    FragmentManager fragmentManager;

    private  LinearLayout tabsLayout;
    private  TabLayout tabs;
    private  ViewPager viewPager2;

    public SeasonTabFragment newInstance(CardDetailViewFactory.CardDetailViewFactoryListener mListener, int position,
                                         List<DetailsViewContent.DetailsViewDataItem> mValues, List<CardData> mListCarouselInfoData,
                                         CardData mTVShowCardData, EpisodeDisplayData count, String mBgColor,FragmentManager fragmentManager){
        SeasonTabFragment episodeTabFragment=new SeasonTabFragment();
        /*Bundle args = new Bundle();
        args.putString("ID",mListCarouselInfoData.get(position)._id);*/
        episodeTabFragment.setAllData(mListener,position,mValues,mListCarouselInfoData,mTVShowCardData,count,mBgColor,fragmentManager);
        //episodeTabFragment.setArguments(args);
        return episodeTabFragment;
    }

    private void setAllData(CardDetailViewFactory.CardDetailViewFactoryListener mListener, int position,
                            List<DetailsViewContent.DetailsViewDataItem> mValues,
                            List<CardData> mListCarouselInfoData, CardData mTVShowCardData, EpisodeDisplayData count,
                            String mBgColor, FragmentManager fragmentManager){
        this.mListener=mListener;
        this.mValues=mValues;
        this.seasonsData =count;
        this.position=position;
        this.mTVShowCardData=mTVShowCardData;
        this.mListCarouselInfoData=mListCarouselInfoData;
        this.fragmentManager=fragmentManager;
        this.mBgColor=mBgColor;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_carouselinfo_episode_tabs, container, false);

        tabsLayout=rootView.findViewById(R.id.tabs_layout);
        tabs=rootView.findViewById(R.id.nested_carousels_tab_layout);
        viewPager2=rootView.findViewById(R.id.nested_carousels_viewpager);
        tabsLayout.setVisibility(View.VISIBLE);

        /*LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,
                false);
        linearLayoutManager.setItemPrefetchEnabled(false);
        mRecyclerViewCarousel = rootView.findViewById(R.id.recyclerview);
        mRecyclerViewCarousel.setLayoutManager(linearLayoutManager);
        VerticalSpaceItemDecoration mHorizontalMoviesDivieder = new VerticalSpaceItemDecoration((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_4));
        mRecyclerViewCarousel.removeItemDecoration(mHorizontalMoviesDivieder);
        mRecyclerViewCarousel.addItemDecoration(mHorizontalMoviesDivieder);
        mRecyclerViewCarousel.setItemAnimator(null);
        mRecyclerViewCarousel.setFocusableInTouchMode(false);
        episodeItemAdapter=new EpisodeItemAdapter(mListener,mContext,Util.getDummyCardData(),mBgColor);
        mRecyclerViewCarousel.setTag(episodeItemAdapter);
        mRecyclerViewCarousel.setAdapter(episodeItemAdapter);
        episodeItemAdapter.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
        episodeItemAdapter.setParentPosition(position);
        isLoadMoreRequestInProgress = false;
        mIsLoadingMoreAvailable = true;
        mRecyclerViewCarousel.addOnItemTouchListener(mScrollTouchListener);
        mRecyclerViewCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoadMoreRequestInProgress
                        && mIsLoadingMoreAvailable) {
                    isLoadMoreRequestInProgress = true;
                    fetchTVEpisodes(position);
                    return;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });*/
        //getArgs();

       // fetchTVEpisodes(position);
        showTabs(mListCarouselInfoData);
        return rootView;

    }

    private void showTabs(List<CardData> mListCarouselInfo){
        List<EpisodeDisplayData> episodeDisplayData=prepareSeasonNames(mListCarouselInfo);
        /*if (mListCarouselInfo.get(0).generalInfo.showDisplayTabs != null
                &&mListCarouselInfo.get(0).generalInfo.showDisplayTabs.showDisplayType!=null
                &&mListCarouselInfo.get(0).generalInfo.showDisplayTabs.showDisplayType.equalsIgnoreCase("episodes")) {
        */
        EpisodeTabsAdapter nestedTabsAdapter=new EpisodeTabsAdapter(getChildFragmentManager(),mListener,mValues,mContext,
                    mListCarouselInfo,mTVShowCardData,episodeDisplayData,mBgColor);
            nestedTabsAdapter.setTabsData(mListCarouselInfo);
            viewPager2.setAdapter(nestedTabsAdapter);
            viewPager2.setOffscreenPageLimit(2);
            tabs.setupWithViewPager(viewPager2);
        /*}else {
            SeasonsTabsAdapter nestedTabsAdapter=new SeasonsTabsAdapter(fragmentManager,mListener,mValues,mContext,
                    mListCarouselInfo,mTVShowCardData,episodeDisplayData,mBgColor);
            nestedTabsAdapter.setTabsData(mListCarouselInfo);
            viewPager2.setAdapter(nestedTabsAdapter);
            viewPager2.setOffscreenPageLimit(2);
            tabs.setupWithViewPager(viewPager2);
        }*/

        //viewPager2.setUserInputEnabled(false);
        /*new TabLayoutMediator(tabs, viewPager2,
                (tab, position) -> tab.setText(prepareSeasonNames(mListCarouselInfo).get(position))
        ).attach();*/
    }

    private List<EpisodeDisplayData> prepareSeasonNames(List<CardData> dataList) {
        List<EpisodeDisplayData> seasons = new ArrayList<>();
        /*if (dataList.get(0).generalInfo.showDisplayTabs != null
                &&dataList.get(0).generalInfo.showDisplayTabs.showDisplayType!=null
                &&dataList.get(0).generalInfo.showDisplayTabs.showDisplayType.equalsIgnoreCase("episodes")) {
        */    ShowDisplayTabs showDisplayTabs = seasonsData.showDisplayTabs;
            if (showDisplayTabs.showDisplayfrequency != null) {
                int totalTabsSize = (int) Math.ceil((Double.parseDouble(showDisplayTabs.showDisplayEnd) -
                        Double.parseDouble(showDisplayTabs.showDisplayStart))/Double.parseDouble(showDisplayTabs.showDisplayfrequency));
                int frequency = Integer.parseInt(showDisplayTabs.showDisplayfrequency);
                int showDisplayEnd = Integer.parseInt(showDisplayTabs.showDisplayEnd);
                int showDisplayStart=Integer.parseInt(showDisplayTabs.showDisplayStart);
                int firstItem=0;
                int secondItem=0;
                for (int p = 0; p < totalTabsSize; p++) {
                    EpisodeDisplayData episodeDisplayData=new EpisodeDisplayData();
                    if (p==0){
                        firstItem = showDisplayStart;
                    }else {
                        firstItem = secondItem;
                    }
                    secondItem = firstItem + frequency;
                    episodeDisplayData.startIndex=p+1;
                    episodeDisplayData.count=(secondItem-firstItem);
                    if (p == totalTabsSize - 1) {
                        episodeDisplayData.episodeTabName = "Latest Episodes";
                    } else {
                        int secondItemToDisplay = secondItem - 1;
                        episodeDisplayData.episodeTabName
                                = showDisplayTabs.showDisplayText + " " + firstItem + "-" + secondItemToDisplay;
                    }
                    episodeDisplayData.seasonId=seasonsData.seasonId;
                    seasons.add(episodeDisplayData);
                }
            }
            Collections.reverse(seasons);
       /* } else {
            String seasonText = "Season ";
            for (CardData cardData : dataList) {
                EpisodeDisplayData episodeDisplayData = new EpisodeDisplayData();
                if (cardData != null) {
                    episodeDisplayData.episodeTabName = cardData.generalInfo.title;
                    episodeDisplayData.showDisplayTabs=cardData.generalInfo.showDisplayTabs;
                    episodeDisplayData.count = 10;
                    seasons.add(episodeDisplayData);
                }
            }
        }*/
        return seasons;
    }

    RecyclerView.OnItemTouchListener mScrollTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            int action = e.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    rv.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    private void fetchTVEpisodes(int position) {
        int count=10;
        LoggerD.debugLog("FragmentEpisodes:: fetching episodes");
        if (mListCarouselInfoData == null || mListCarouselInfoData.isEmpty()) return;
        CardData seasonData;
        if (mListCarouselInfoData.get(0).generalInfo.showDisplayTabs != null &&
                mListCarouselInfoData.get(0).generalInfo.showDisplayTabs.showDisplayType != null &&
                mListCarouselInfoData.get(0).generalInfo.showDisplayTabs.showDisplayType.equalsIgnoreCase("episodes")) {
            seasonData = mListCarouselInfoData.get(0);
            startIndex= seasonsData.startIndex;
            count= seasonsData.count;
        } else {
            seasonData = mListCarouselInfoData.get(position);
            startIndex=1;
        }
        if (seasonData == null) return;
        //if (!isLoadMoreRequestInProgress)
            //addFooterProgressLoadingCard();
            new EpisodesAPICallTask(this, seasonData._id,startIndex,count)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }



    private static class EpisodesAPICallTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<SeasonTabFragment> fragmentEpisodesWeakReference;
        private final WeakReference<String> contentIdReference;
        private int startIndex;
        private int count;

        EpisodesAPICallTask(SeasonTabFragment fragmentEpisodesWeakReference, String contentId, int startIndex, int count) {
            this.fragmentEpisodesWeakReference = new WeakReference<SeasonTabFragment>(fragmentEpisodesWeakReference);
            this.contentIdReference = new WeakReference<>(contentId);
            this.count=count;
            this.startIndex= startIndex;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (fragmentEpisodesWeakReference.get() == null)
                return null;
            SeasonTabFragment fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
            if (contentIdReference.get() == null) {
                return null;
            }
            //Update selected season text on drop down header
//            fragmentCardDetailsDescription.updateDropDownTitle();
//            fragmentCardDetailsDescription.showProgressBar();

            if (fragmentCardDetailsDescription.isLoadMoreRequestInProgress)
                startIndex = fragmentCardDetailsDescription.mTVEpisodesListStartIndex + 1;
            fragmentCardDetailsDescription.mCacheManager.getRelatedVODList(contentIdReference.get(), startIndex,
                    true,count,
                    new CacheManager.CacheManagerCallback() {
                        @Override
                        public void OnCacheResults(final List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            SeasonTabFragment fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<SeasonTabFragment, List<CardData>>
                                    (fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(SeasonTabFragment fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateEpisodesData(var1,false);
                                }
                            });
                        }

                        @Override
                        public void OnOnlineResults(List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            SeasonTabFragment fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<SeasonTabFragment,
                                    List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(SeasonTabFragment fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateEpisodesData(var1,true);
                                }
                            });
                        }

                        @Override
                        public void OnOnlineError(Throwable error, int errorCode) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            SeasonTabFragment fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<SeasonTabFragment, List<CardData>>(fragmentCardDetailsDescription, null) {
                                @Override
                                protected void safeRun(SeasonTabFragment fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription1.updateEpisodesData(var1,false);
                                }
                            });
                        }
                    });
            return null;
        }

    }

    private void updateEpisodesData(List<CardData> cardDataList, boolean isToReverseList){
        if (cardDataList == null) {
            if (isLoadMoreRequestInProgress) {
                mIsLoadingMoreAvailable = false;
                return;
            }
            //showErrorMessageView();
            return;
        }
        mIsLoadingMoreAvailable = true;
        if (isLoadMoreRequestInProgress) {
            isLoadMoreRequestInProgress= false;
            mTVEpisodesListStartIndex++;
            if (mCacheManager.isLastPage()) {
                mIsLoadingMoreAvailable = false;
            }

            if (mListEpisodes != null) {
                mListEpisodes.addAll(cardDataList);
                episodeItemAdapter.setListData(cardDataList);
                return;
            }
        }
        /*if (isToReverseList){
            Collections.reverse(cardDataList);
        }*/
        mListEpisodes=new ArrayList<>();
        mListEpisodes.addAll(cardDataList);
        episodeItemAdapter=new EpisodeItemAdapter(mListener,mContext,cardDataList,mBgColor);
        mRecyclerViewCarousel.setTag(episodeItemAdapter);
        mRecyclerViewCarousel.setAdapter(episodeItemAdapter);
        mRecyclerViewCarousel.setNestedScrollingEnabled(false);
        episodeItemAdapter.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
        episodeItemAdapter.setParentPosition(position);
        isLoadMoreRequestInProgress=false;
        if(position==0){
            mListener.onEpiosodesLoaded(cardDataList, false);
        }
        //mListEpisodes = new ArrayList<>(cardDataList);
    }

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, final CardData carouselData) {
            try {
                showDetailsFragment(carouselData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void showDetailsFragment(CardData cardData) {
        //Log.d(TAG, "onItemClick");

        if (cardData == null) {
            return;
        }
        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        args.putString(CardDetails
                .PARAM_CARD_ID, cardData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        int partnerType = CardDetails.Partners.APALYA;
        partnerType = Util.getPartnerTypeContent(cardData);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, partnerType);
        String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        if (!TextUtils.isEmpty(source))
            args.putString(Analytics.PROPERTY_SOURCE, source);
        if (!TextUtils.isEmpty(sourceDetails))
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, sourceDetails);

        if (mSearchQuery != null) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSearchQuery);
        }
        if (cardData.generalInfo != null
                && cardData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
            //Log.d(TAG, "type: " + cardData.generalInfo.type + " title: " + cardData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, cardData);
        }
        args.putString(CardDetails.PARAM_CARD_ID, cardData._id);
        args.putString(CardDetails.PARAM_CARD_DATA_TYPE, cardData.generalInfo.type);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putBoolean(FragmentCardDetailsPlayer.PARAM_KEPP_DESCRIPTION_VIEWS_UPDATe_DATA, true);
        if (mCurrentSeasonName != null) {
            args.putString(CardDetails.PARAM_SEASON_NAME, mCurrentSeasonName);
        }

        if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_LIVE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)
                && !cardData.isTVSeries()
                && !cardData.isVODChannel()
                && !cardData.isVODYoutubeChannel()
                && !cardData.isVODCategory()
                && !cardData.isTVSeason()) {
            /*int episodesLayoutStartPosition = 0;
            if (mListener != null) {
                episodesLayoutStartPosition = mListener.getEpisodesLayoutStartPosition();
            }
            mValues.subList(episodesLayoutStartPosition, mValues.size() - 1);
            List<CardData> listEpisodes = new ArrayList<>();
            for (int i = 0; i < mValues.size(); i++) {
                if (i >= episodesLayoutStartPosition) {
                    listEpisodes.add(mValues.get(i).cardData);
                }
            }*/
            CacheManager.setCardDataList(mListEpisodes);
        }
        args.putSerializable(PARAM_RELATED_CARD_DATA, mTVShowCardData);
        ((MainActivity)mContext).showDetailsFragment(args, cardData);
//            mBaseActivity.pushFragment(CardDetails.newInstance(args));

    }

}
