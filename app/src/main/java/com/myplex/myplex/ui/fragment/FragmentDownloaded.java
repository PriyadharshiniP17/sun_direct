package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.model.CardData;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.events.DownloadDataLoaded;
import com.myplex.myplex.events.DownloadDeleteEvent;
import com.myplex.myplex.events.EventNetworkConnectionChange;
import com.myplex.myplex.events.MessageEvent;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterForDownloads;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;


/*** Created by on 7/20/2017.
 * */

public class FragmentDownloaded extends BaseFragment {

    AdapterForDownloads adapterForDownloads;

    public static final int TYPE_MOVIES = 0;
    public static final int TYPE_TV_SHOWS = 1;
    public static final int TYPE_VIDEOS = 2;

    int currentFragment = 0;

    private ListView mDownloadList;
    private ImageView mImageNoDownloads;
    private TextView mDownload_count;

    private void showRelatedVODFragmentForDownloads(CardDownloadData downloadData) {
        Bundle args = new Bundle();
        args.putSerializable(FragmentRelatedVODListForDownloads.PARAM_DOWNLOAD_DATA, downloadData);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODListForDownloads.newInstance(args));
    }

    public FragmentDownloaded newInstance(int type) {
        LoggerD.debugDownload("newInstance");
        FragmentDownloaded mFragmentDownloaded = new FragmentDownloaded();
        Bundle args = new Bundle();
        int currentType = 0;
        switch (type) {
            case 0:
                currentType = TYPE_MOVIES;
                break;
            case 1:
                currentType = TYPE_TV_SHOWS;
                break;
            case 2:
                currentType = TYPE_VIDEOS;
                break;
        }
        args.putInt("TYPE", currentType);
        mFragmentDownloaded.setArguments(args);
        return mFragmentDownloaded;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LoggerD.debugDownload("onCreateView");
        mContext = getActivity();

        getBundleValues();

        View rootView = inflater.inflate(R.layout.fragment_downloaded, container, false);

        mDownloadList = (ListView) rootView.findViewById(R.id.listview_movies);
        mImageNoDownloads = (ImageView) rootView.findViewById(R.id.coming_soon);
        mDownload_count = rootView.findViewById(R.id.download_count);
        mDownloadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapterForDownloads == null) return;
                CardDownloadData downloadData = adapterForDownloads.getItem(position);
                CardData cardData = adapterForDownloads.generateCardData(downloadData);
                CacheManager.setSelectedCardData(cardData);
                Bundle args = new Bundle();
                args.putString(CardDetails
                        .PARAM_CARD_ID, cardData._id);
                args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
                args.putString(CardDetails.PARAM_PARTNER_ID, cardData.generalInfo.partnerId);
                args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
                args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_DOWNLOADED_VIDEOS);
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, cardData.generalInfo.title);
                ((MainActivity) mContext).showDetailsFragment(args, cardData);
            }
        });
        loadData();
        FetchDownloadProgress.getInstance(mContext).addProgressListener(adapterForDownloads);
        ErosNowDownloadManager.getInstance(mContext).initUnzipManagerListener(adapterForDownloads);
        return rootView;
    }

    private void loadData() {
        //if user is not logged in donot show downloaded contents
        if (!Util.checkUserLoginStatus()){
            return;
        }

        List downloadedMovies = prepareData();
        if (downloadedMovies == null
                || downloadedMovies.isEmpty())
            mImageNoDownloads.setVisibility(View.VISIBLE);
        boolean showProgress = currentFragment != TYPE_TV_SHOWS;
        if (adapterForDownloads == null)
            adapterForDownloads = new AdapterForDownloads(mContext, showProgress, currentFragment);
        adapterForDownloads.add(downloadedMovies);
        mDownloadList.setAdapter(adapterForDownloads);

        mDownload_count.setVisibility(View.VISIBLE);
        if (downloadedMovies.size() >= 1) {
            mDownload_count.setText("Downloaded videos - " + downloadedMovies.size());
            mDownload_count.setVisibility(View.VISIBLE);
        } else {
            mDownload_count.setVisibility(View.GONE);
        }


        /*if (prepareData().size()!=0){
            String _id=prepareData().get(0)._id;
        Handler handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
               int percentage= (int) DownloadManagerMaintainer.getInstance().getDownloadPercentage(_id);
               System.out.println("Percentage downloaded::"+ percentage );
            }
        };
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        },1000,1000);
        }*/
    }


    private List<CardDownloadData> prepareData() {
        CardDownloadedDataList downloadedData = loadDownloadedData();
        Iterator it = null;
        List<CardDownloadData> downloadedMovies = new ArrayList<>();
        if (downloadedData != null) {
            it = downloadedData.mDownloadedList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry data = (Map.Entry) it.next();
                CardDownloadData cardDownloadData = downloadedData.mDownloadedList.get(data.getKey());
                downloadedMovies.add(cardDownloadData);
                mImageNoDownloads.setVisibility(View.GONE);
            }
        }
            /*switch (currentFragment) {
                case TYPE_MOVIES:

                    break;
                case TYPE_TV_SHOWS:
                    it = downloadedData.mDownloadedList.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry data = (Map.Entry) it.next();
                        CardDownloadData cardDownloadData = downloadedData.mDownloadedList.get(data.getKey());

                        if (downloadedData.mDownloadedList.size() != 0
                                && cardDownloadData.ItemType != null
                                && !cardDownloadData.ItemType.equalsIgnoreCase(APIConstants.TYPE_VOD)
                                && !cardDownloadData.ItemType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)
                                && !cardDownloadData.ItemType.equalsIgnoreCase(APIConstants.TYPE_MOVIE)) {
                            downloadedMovies.add(cardDownloadData);
                            mImageNoDownloads.setVisibility(View.GONE);
                        }
                    }
                    break;
                case TYPE_VIDEOS:
                    it = downloadedData.mDownloadedList.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry data = (Map.Entry) it.next();
                        CardDownloadData cardDownloadData = downloadedData.mDownloadedList.get(data.getKey());

                        if (cardDownloadData.ItemType != null
                                && (cardDownloadData.ItemType.equalsIgnoreCase(APIConstants.TYPE_VOD)
                                || cardDownloadData.ItemType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE))) {
                            downloadedMovies.add(cardDownloadData);
                            mImageNoDownloads.setVisibility(View.GONE);
                        }
                    }
                    break;
            }*/
        //  }
        EventBus.getDefault().post(new DownloadDataLoaded(currentFragment, downloadedMovies == null || downloadedMovies.isEmpty() ? true : false));
        return downloadedMovies;
    }

    @Override
    public void onResume() {
        super.onResume();
        LoggerD.debugDownload("onResume");
        if (adapterForDownloads != null) {
            adapterForDownloads.notifyDataSetChanged();
            if (adapterForDownloads.getCount() > 0)
                FetchDownloadProgress.getInstance(mContext).addProgressListener(adapterForDownloads);
            ErosNowDownloadManager.getInstance(mContext).initUnzipManagerListener(adapterForDownloads);
        }
    }

    private void getBundleValues() {
        Bundle args = getArguments();
        currentFragment = args.getInt("TYPE", 0);
        Log.e("CURRENT FRAGMENT", currentFragment + "");
    }

    private CardDownloadedDataList loadDownloadedData() {
        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
        return downloadlist;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LoggerD.debugDownload("DebugDownloadAdapterPerformance: onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LoggerD.debugDownload("DebugDownloadAdapterPerformance: onDetach");
    }

    @Override
    public void onPause() {
        super.onPause();
        FetchDownloadProgress.getInstance(mContext).removeProgressListener(adapterForDownloads);
        LoggerD.debugDownload("DebugDownloadAdapterPerformance: onPause");
        //SDKUtils.saveObject(ApplicationController.getDownloadData(), getApplicationConfig().downloadCardsPath);
    }

    public void onEventMainThread(MessageEvent event) {
        loadData();
        LoggerD.debugDownload("onEventMainThread ContentDownloadEvent");
    }

    public void onEventMainThread(DownloadDeleteEvent downloaddeleteEvent) {
        LoggerD.debugDownload("onEventMainThread DownloadDeleteEvent");
        if (adapterForDownloads == null) {
            return;
        }
        if (adapterForDownloads.getCount() <= 0) {
            mImageNoDownloads.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onBackClicked() {
        LoggerD.debugDownload("DebugDownloadAdapterPerformance: onPause");
        return super.onBackClicked();
    }

    public void onEventMainThread(EventNetworkConnectionChange event) {
        if (adapterForDownloads != null) {
            adapterForDownloads.notifyDataSetChanged();
        }
    }
}
