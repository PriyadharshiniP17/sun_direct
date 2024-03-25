package com.myplex.myplex.ui.component;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.model.CardData;
import com.myplex.model.EpisodeDisplayData;
import com.myplex.model.ShowDisplayTabs;
import com.myplex.util.AlertDialogUtil;
import com.myplex.myplex.R;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.utils.WeakRunnable;
import com.myplex.myplex.utils.WeakRunnableTwo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EpisodesTabViewItem extends GenericListViewCompoment{

    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = EpisodesTabViewItem.class.getSimpleName();
    private final LinearLayout tabsLayout;
    private final TabLayout tabs;
    private final ViewPager viewPager2;
    private final CardData mTvShowCardData;
    private String mBgColor;

    private final CacheManager mCacheManager = new CacheManager();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private FragmentManager fragmentManager;

    public EpisodesTabViewItem(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data,
                               View view, CardDetailViewFactory.CardDetailViewFactoryListener listener,
                               FragmentManager childFragmentManager,CardData mTvShowCardData) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        this.fragmentManager=childFragmentManager;
        this.mTvShowCardData=mTvShowCardData;
        tabsLayout=view.findViewById(R.id.tabs_layout);
        tabs=view.findViewById(R.id.nested_carousels_tab_layout);
        viewPager2=view.findViewById(R.id.nested_carousels_viewpager);
    }

    public static EpisodesTabViewItem createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data,
                                                 ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener,
                                                 FragmentManager childFragmentManager,CardData mTvShowCardData) {
        View view = LayoutInflater.from(context).inflate(R.layout.episode_tabs_layout,
                parent, false);
        EpisodesTabViewItem briefDescriptionComponent = new EpisodesTabViewItem(context, data, view, listener,childFragmentManager,mTvShowCardData);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int newPosition) {
        mData=mValues.get(position).cardData;
        List<CardData> mSeasonsListData=mValues.get(newPosition).seasonsList;
        mBgColor=mValues.get(position).mBgColor;
        tabsLayout.setVisibility(View.VISIBLE);

        if (mBgColor!=null&& !TextUtils.isEmpty(mBgColor)){
            tabs.setBackgroundColor(Color.parseColor(mBgColor));
            tabs.setTabTextColors(ContextCompat.getColorStateList(mContext, R.color.black));
        }
        /*if (mSeasonsListData!=null&&!mSeasonsListData.isEmpty()){
         showTabs(mSeasonsListData);
        }*/
        fetchTVSeasons(mData._id);
    }

    private void fetchTVSeasons(String contentId) {
        new SeasonsAPICallTask(this, contentId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class SeasonsAPICallTask extends AsyncTask<Void, Void, Void> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.Fr
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
        private final WeakReference<EpisodesTabViewItem> fragmentEpisodesWeakReference;
        private final WeakReference<String> contentIdRef;

        SeasonsAPICallTask(EpisodesTabViewItem fragmentEpisodesWeakReference, String contentId) {
            this.fragmentEpisodesWeakReference = new WeakReference<EpisodesTabViewItem>(fragmentEpisodesWeakReference);
            this.contentIdRef = new WeakReference<>(contentId);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (fragmentEpisodesWeakReference.get() == null) return null;

            if (contentIdRef.get() == null) return null;
            fragmentEpisodesWeakReference.get().mCacheManager.getRelatedVODListTypeExclusion(contentIdRef.get(), 1, true, APIConstants.TYPE_TVSEASON,
                    15,
                    new CacheManager.CacheManagerCallback() {
                        @Override
                        public void OnCacheResults(List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            EpisodesTabViewItem fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<EpisodesTabViewItem,
                                    List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(EpisodesTabViewItem fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription.showTabs(dataList);
                                }
                            });
                        }

                        @Override
                        public void OnOnlineResults(List<CardData> dataList) {
                            if (fragmentEpisodesWeakReference.get() == null)
                                return;
                            EpisodesTabViewItem fragmentCardDetailsDescription = fragmentEpisodesWeakReference.get();
                            fragmentCardDetailsDescription.mHandler.post(new WeakRunnableTwo<EpisodesTabViewItem,
                                    List<CardData>>(fragmentCardDetailsDescription, dataList) {
                                @Override
                                protected void safeRun(EpisodesTabViewItem fragmentCardDetailsDescription1, List<CardData> var1) {
                                    fragmentCardDetailsDescription.showTabs(dataList);
                                }
                            });

                        }

                        @Override
                        public void OnOnlineError(Throwable error, int errorCode) {
                            if (fragmentEpisodesWeakReference.get() == null) return;
                            //Log.d(TAG, "fetchCarouselData: OnOnlineError: t- ");
                            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                fragmentEpisodesWeakReference.get().mHandler.post(new WeakRunnable<EpisodesTabViewItem>(fragmentEpisodesWeakReference.get()) {
                                    @Override
                                    protected void safeRun(EpisodesTabViewItem var1) {
                                        AlertDialogUtil.showToastNotification(var1.mContext.getString(R.string.network_error));
                                    }
                                });
                            }
//                        showNoDataMessage();
                        }
                    });

            return null;
        }
    }

    private void showTabs(List<CardData> mListCarouselInfo){
        List<EpisodeDisplayData> episodeDisplayData=prepareSeasonNames(mListCarouselInfo);
        if (mListCarouselInfo.get(0).generalInfo.showDisplayTabs != null
                &&mListCarouselInfo.get(0).generalInfo.showDisplayTabs.showDisplayType!=null
                &&mListCarouselInfo.get(0).generalInfo.showDisplayTabs.showDisplayType.equalsIgnoreCase("episodes")) { // only episodes
            EpisodeTabsAdapter nestedTabsAdapter=new EpisodeTabsAdapter(fragmentManager,mListener,mValues,mContext,
                    mListCarouselInfo,mTvShowCardData,episodeDisplayData,mBgColor);
            nestedTabsAdapter.setTabsData(mListCarouselInfo);
            viewPager2.setAdapter(nestedTabsAdapter);
        }else if (mListCarouselInfo.size()==1){ //only one season
            EpisodeTabsAdapter nestedTabsAdapter=new EpisodeTabsAdapter(fragmentManager,mListener,mValues,mContext,
                    mListCarouselInfo,mTvShowCardData,episodeDisplayData,mBgColor);
            nestedTabsAdapter.setTabsData(mListCarouselInfo);
            viewPager2.setAdapter(nestedTabsAdapter);
        }else  { // multiple seasones
            SeasonsEpisodeTabsAdapter nestedTabsAdapter=new SeasonsEpisodeTabsAdapter(fragmentManager,mListener,mValues,mContext,
                    mListCarouselInfo,mTvShowCardData,episodeDisplayData,mBgColor);
            nestedTabsAdapter.setTabsData(mListCarouselInfo);
            viewPager2.setAdapter(nestedTabsAdapter);
        }
        viewPager2.setOffscreenPageLimit(2);
        tabs.setupWithViewPager(viewPager2);

        //viewPager2.setUserInputEnabled(false);
        /*new TabLayoutMediator(tabs, viewPager2,
                (tab, position) -> tab.setText(prepareSeasonNames(mListCarouselInfo).get(position))
        ).attach();*/
    }

    private List<EpisodeDisplayData> prepareSeasonNames(List<CardData> dataList) {
        List<EpisodeDisplayData> seasons = new ArrayList<>();
        if (dataList.get(0).generalInfo.showDisplayTabs != null
                &&dataList.get(0).generalInfo.showDisplayTabs.showDisplayType!=null
                &&dataList.get(0).generalInfo.showDisplayTabs.showDisplayType.equalsIgnoreCase("episodes")) {
            ShowDisplayTabs showDisplayTabs = dataList.get(0).generalInfo.showDisplayTabs;
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
                    seasons.add(episodeDisplayData);
                }
            }
        } else {
            String seasonText = "Season ";
            for (CardData cardData : dataList) {
                EpisodeDisplayData episodeDisplayData = new EpisodeDisplayData();
                if (cardData != null) {
                    episodeDisplayData.episodeTabName = seasonText;
                    episodeDisplayData.showDisplayTabs=cardData.generalInfo.showDisplayTabs;
                    episodeDisplayData.seasonId=cardData._id;
                    episodeDisplayData.count = 10;
                    seasons.add(episodeDisplayData);
                }
            }
        }
        Collections.reverse(seasons);
        return seasons;
    }

}
