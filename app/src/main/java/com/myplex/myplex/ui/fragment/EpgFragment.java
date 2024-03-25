package com.myplex.myplex.ui.fragment;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.events.ChannelsUpdatedEvent;
import com.myplex.myplex.events.EPGHelpScreenEvent;
import com.myplex.myplex.events.EventNotifyEpgAdapter;
import com.myplex.myplex.events.OpenChannelEPGEvent;
import com.myplex.myplex.events.OpenFilterEvent;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ChannelItem;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.activities.ProgramGuideChannelActivity;
import com.myplex.myplex.ui.adapter.EpgListAdapter;
import com.myplex.myplex.ui.views.ListScrollManager;
import com.myplex.myplex.ui.views.SyncScrollListView;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class EpgFragment extends BaseFragment implements AbsListView.OnScrollListener,AdapterView.OnItemClickListener,View.OnClickListener, SDKUtils.RegenerateKeyRequestCallback {
    private static final String TAG = EpgFragment.class.getSimpleName();
    private static final String PARAM_EPG_DATE_POSITION = "datePos";
    private static final String PARAM_EPG_TIME = "time";
    private static final String PARAM_EPG_TIME_LIST = "list";
    private static final String PARAM_EPG_PAGE_POS = "pos";
    private static final String PARAM_EPG_GLOBAL_PAGE_INDEX = "globalPageIndex";
    private SyncScrollListView mEpgListView;
    private EpgListAdapter mEpgListAdapter;
    private String time = null;
    private ProgressBar mProgressBar;
    private boolean flag_loading = false;
    private View mFooterView;
    private ProgressBar mFooterPbBar;
    private List<CardData> mDataList  = new ArrayList<>();;
    private int pageIndex = 1;
    private int pos;
    private ArrayList<String> list;
    private static final int PAGE_SIZE = 10;
    private int datePos;
    private Parcelable state;
    public static boolean isChannelOpen = false;
    private TextView noResTxt;
    private boolean isRetryAlreadyDone = false;
    private boolean isEpgFetchFailedEventFiled = false;
    private static boolean isKeyRegenerateRequestMade;

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(!menuVisible && mEpgListView!=null){
            ApplicationController.pageVisiblePos = mEpgListView.getFirstVisiblePosition();
            if(mEpgListView.getChildAt(0)!=null){
                ApplicationController.pageItemPos    = mEpgListView.getChildAt(0).getTop();
            }

        }else if(mEpgListView!=null){
            mEpgListView.setSelectionFromTop(ApplicationController.pageVisiblePos,ApplicationController.pageItemPos);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        ListScrollManager.getInstance().addScrollClient(mEpgListView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PARAM_EPG_DATE_POSITION, datePos);
        outState.putString(PARAM_EPG_TIME, time);
        outState.putStringArrayList(PARAM_EPG_TIME_LIST, list);
        outState.putInt(PARAM_EPG_PAGE_POS, pos);
        outState.putInt(PARAM_EPG_GLOBAL_PAGE_INDEX, EPG.globalPageIndex);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_video, container, false);
        mEpgListView = (SyncScrollListView) rootView.findViewById(R.id.epg_listView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext,R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        noResTxt     = (TextView)rootView.findViewById(R.id.no_res_txt);
        Bundle args = getArguments();
        if (savedInstanceState != null) {
            args = savedInstanceState;
        }
        time = args.getString("time");
        pos = args.getInt("pos");
        list = args.getStringArrayList("list");
        datePos = args.getInt("datePos");
        pageIndex = EPG.globalPageIndex;
        initUI();
        ListScrollManager.getInstance().addScrollClient(mEpgListView);
        //System.out.println("accer EPG pos "+pos);
        //System.out.println("accer EPG time "+time);
       // System.out.println("accer EPG date pos "+datePos);
        return rootView;
    }
    // Called in Android UI's main thread
    public void onEventMainThread(OpenChannelEPGEvent event) {
        int pos = event.getPosition();

       if(!isChannelOpen){
           isChannelOpen = true;
//           showChannelEpg(mDataList.get(pos));
           showDetailsFragment(mDataList.get(pos));
       }
    }


    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pageIndex = EPG.globalPageIndex;
        ListScrollManager.getInstance().addScrollClient(mEpgListView);
    }

    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pageIndex = EPG.globalPageIndex;
        if(state != null) {
            Log.d("", "trying to restore listview state..");

            mEpgListView.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save ListView state @ onPause
        //pageIndex = EPG.globalPageIndex;



        Log.d("", "saving listview state @ onPause");
        state = mEpgListView.onSaveInstanceState();
       // Analytics.gaBrowse(Analytics.TYPE_EPG,1l);

    }

    @Override
    public void onStop() {
        super.onStop();
       //pageIndex = EPG.globalPageIndex;


    }

    @Override
    public void onDetach() {
        super.onDetach();
        ListScrollManager.getInstance().removeScrollClient(mEpgListView);

    }

    private void initUI() {
        pageIndex = EPG.globalPageIndex;

        mProgressBar.setVisibility(View.VISIBLE);
      //  System.out.println("accer initUI time "+time);
        fetchEpgData(false);
        mEpgListView.setOnScrollListener(this);
        mFooterView = getActivity().getLayoutInflater().inflate(R.layout.view_footer_layout, mEpgListView, false);
        mFooterPbBar = (ProgressBar) mFooterView.findViewById(R.id.footer_progressbar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mFooterPbBar.setIndeterminate(false);
            mFooterPbBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext,com.myplex.sdk.R.color.red_highlight_color), PorterDuff.Mode.MULTIPLY);
        }
        mFooterPbBar.setVisibility(View.GONE);
        mEpgListView.addFooterView(mFooterView);
        mEpgListView.setOnItemClickListener(this);
        noResTxt.setOnClickListener(this);
    }

    private void fetchEpgData(final boolean isLoadMore) {

        final Date date = Util.getCurrentDate(datePos);
        boolean isDVROnly = (PrefUtils.getInstance().getPrefEnablePastEpg() && datePos - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0) ? true : false;

        EPG.getInstance(Util.getCurrentDate(datePos)).findPrograms(APIConstants.PAGE_INDEX_COUNT, Util.getServerDateFormat(time, date), time, date, pageIndex, isLoadMore, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly,"", new EPG.CacheManagerCallback() {

            @Override
            public void OnlineResults(List<CardData> dataList, int pageIndex) {
                if (mFooterPbBar != null) mFooterPbBar.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                if (dataList != null && dataList.size() < PAGE_SIZE) {
                    if (isLoadMore) flag_loading = true;
                } else if (dataList != null && dataList.size() >= PAGE_SIZE) {
                    if (isLoadMore) flag_loading = false;
                } else if (dataList == null) {
                  /*  if(pageIndex>=1){
                        pageIndex--;
                        EPG.globalPageIndex = pageIndex;
                    }*/
                    if (isLoadMore) flag_loading = true;
                }
                //System.out.println("phani size "+dataList.size());
                updateChannelList(dataList, pageIndex);

            }

            @Override
            public void OnlineError(Throwable error, int errorCode) {
                String reason = error.getMessage() == null ? "NA" : error.getMessage();

                if(pos == 1 && pageIndex == 1 && !isEpgFetchFailedEventFiled){
                    isEpgFetchFailedEventFiled = true;
                    Analytics.mixpanelEventFailedFetchEpgList(reason, Util.getServerDateFormat(time, Util.getCurrentDate(datePos)));
                }

                if (reason.equalsIgnoreCase("ERR_INVALID_SESSION_ID") && errorCode == 401 && !isKeyRegenerateRequestMade) {
                    isKeyRegenerateRequestMade = true;
                    SDKUtils.makeReGenerateKeyRequest(mContext, EpgFragment.this);
//                    cacheManagerCallback.OnlineError(new Throwable(""), errorCode);
                    return;
                }
                mProgressBar.setVisibility(View.GONE);
                mEpgListView.setVisibility(View.VISIBLE);
                if (isLoadMore) flag_loading = true;
                if (mFooterPbBar != null) mFooterPbBar.setVisibility(View.GONE);

                if (mDataList == null || mDataList.size() == 0) {
                    showErrorMessage(error);
                }

            }
        });


    }
    private void updateChannelList(List<CardData> dataList, int pageIndex) {

        if (mEpgListAdapter == null && (((pageIndex - 1) * PAGE_SIZE) > mDataList.size())) {
            int emptyCardEndPos = (pageIndex - 1) * PAGE_SIZE;
            for (int i = 0; i < emptyCardEndPos; i++) {
                mDataList.add(i, EPG.createEmptyCards());
            }
        }

        if (dataList != null && dataList.size() < PAGE_SIZE) {
            flag_loading = true;
        }else if(dataList == null && pageIndex ==1){
            EPG.globalPageIndex =1;
            pageIndex =1;
            flag_loading = true;
        }
        if (dataList != null && dataList.size() > 0) {
            if (mDataList.size() > 0) {
                mDataList.addAll(mDataList.size(), dataList);
            } else {
                mDataList.addAll(dataList);
            }
        }

        mEpgListView.setVisibility(View.VISIBLE);
        noResTxt.setVisibility(View.GONE);
       // if(dataList!=null && dataList.size()>0)
        if (mEpgListAdapter == null) {
            mEpgListAdapter = new EpgListAdapter(getActivity(), mDataList, Util.getServerDateFormat(time, Util.getCurrentDate(datePos)), time, datePos);
            mEpgListView.setAdapter(mEpgListAdapter);
        } else {
            mEpgListAdapter.addData(mDataList);
        }
        if (ApplicationController.pageVisiblePos != 0)
            mEpgListView.setSelectionFromTop(ApplicationController.pageVisiblePos, ApplicationController.pageItemPos);

        addChannelImagesUrls(mDataList);

        if(mDataList == null || mDataList.size() == 0){
            showErrorMessage(null);
            //mEpgListView.S
        }
    }

    private void showErrorMessage(Throwable error) {
        if (noResTxt == null) return;

        if(ApplicationController.SHOW_PLAYER_LOGS) {
            noResTxt.setText("Inside OnlineError");
        }
        Log.e(TAG, "showErrorMessage: error");
        if (error != null) {
            Log.e(TAG, "showErrorMessage: error" + error.getMessage());
            String errorMessage = error.getMessage();
            if(mContext != null && mContext.getResources().getBoolean(R.bool.crashlytics_enable)){

            }
            if (errorMessage != null /*&& errorMessage.contains(APIConstants.MESSAGE_ERROR_CONN_RESET) */&& !isRetryAlreadyDone) {
                //Retry for data connection
                Log.e(TAG, "showErrorMessage: retrying again for reconnection");
                fetchEpgData(false);
                isRetryAlreadyDone = true;
                if(ApplicationController.SHOW_PLAYER_LOGS) {
                    noResTxt.setText(errorMessage);
                }

//                Crashlytics.log("Retrying for EPG");
            }

//            }
        }

        if (mEpgListAdapter == null
                || mEpgListAdapter.getCount() <= 0) {
            noResTxt.setVisibility(View.VISIBLE);
        }
    }
    private HashMap<Integer,String> addChannelData(List<CardData> dataList) {
        HashMap<Integer,String>channelImages = new HashMap<>();
        for(int i =0;i< dataList.size();i++){
            CardData cardData = dataList.get(i);
            if (cardData.images != null && cardData.images.values != null && cardData.images.values.size() > 0) {
                for (CardDataImagesItem imageItem : cardData.images.values) {
                    if (imageItem.type != null && imageItem.type.equalsIgnoreCase("thumbnail") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)

                            && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("250x375")) {
                        if (imageItem.link != null) {
                            channelImages.put(i, imageItem.link);

                        } else {
                            channelImages.put(i,null);
                        }
                        break;
                    }
                }
            } else {
                channelImages.put(i, null);
            }
        }
        return channelImages;

    }

    private void addChannelImagesUrls(List<CardData> list) {
        // HashMap<Integer,String> channelList =   addChannelData(dataList);
        HashMap<Integer, ChannelItem> channelImgData = EPG.getInstance(Util.getCurrentDate(datePos)).fetchChannels(list);

        // HashMap<Integer, String> channelList = EPG.getInstance(Util.getCurrentDate(datePos)).fetchChannelsList(list.size());

        //HashMap<Integer, ChannelItem> images =null;
        // TvGuideFragment.showChannels(channelImgData);

        ScopedBus.getInstance().post(new ChannelsUpdatedEvent(channelImgData));
        ScopedBus.getInstance().post(new EPGHelpScreenEvent());
        //return;


       /* HashMap<Integer,ChannelItem> channelIAllImgData = EPG.getInstance(Util.getCurrentDate(datePos)).fetchAllChannels();
        images =  fetchChannels(list,channelIAllImgData);
        if(images!=null && images.size()>0){
            //System.out.println("phani images "+images.size() +"list size "+list.size());
            ScopedBus.getInstance().post(new ChannelsUpdatedEvent(images));

        }*/

    }

    public HashMap<Integer,ChannelItem> fetchChannels(List<CardData> cardList,HashMap<Integer, ChannelItem> channelImgData){
        HashMap<Integer,ChannelItem> channelItemHashMap = new HashMap<>();
        for(int i =0;i<cardList.size();i++){
               CardData cardData = cardList.get(i);
            if(cardData.content!=null){
                for(int j =0 ;j<channelImgData.size();j++){
                    ChannelItem channelItem = channelImgData.get(j);
                    String id = channelItem.getChannelId();
                   ;
                    if(cardList.get(i).globalServiceId.equals(id)){
                        channelItemHashMap.put(i, channelItem);
                        break;
                    }
                }
            }else {
                channelItemHashMap.put(i,null);
            }

        }

        return channelItemHashMap;
    }
    @Override
    public void onScrollStateChanged(final AbsListView view, int scrollState) { }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
            if (!flag_loading) {
                flag_loading = true;
                mFooterPbBar.setVisibility(View.VISIBLE);
                time = list.get(pos);
                EPG.globalPageIndex = ++pageIndex;
                fetchEpgData(true);
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_EPG_BROWSED);

            }
        }
    }

    private void showChannelEpg(CardData channelData){
        //isChannelClickble = false;
        if (channelData == null || channelData.globalServiceId == null) {
            isChannelOpen = false;
            return;
        }
        MainActivity mActivity = (MainActivity) getActivity();
        Bundle args = new Bundle();
        args.putSerializable(ProgramGuideChannelActivity.PARAM_CHANNEL_DATA,channelData);
        args.putInt(ProgramGuideChannelActivity.DATE_POS,datePos);
        FragmentChannelEpg fragmentChannelEpg = FragmentChannelEpg.newInstance(args);
        mActivity.pushFragment(fragmentChannelEpg);
        

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CardData cardData = (CardData) parent.getItemAtPosition(position);
//        showChannelEpg(cardData);
        showDetailsFragment(cardData);

    }

    @Override
    public void onClick(View v) {
        if( EPG.genreFilterValues.length() != 0 || EPG.langFilterValues.length() !=0){
            ScopedBus.getInstance().post(new OpenFilterEvent());
        }
    }


    @Override
    public void onSuccess() {
        fetchEpgData(false);
    }

    @Override
    public void onFailed(String msg) {
        if (TextUtils.isEmpty(msg)) {
            AlertDialogUtil.showToastNotification(msg);
            return;
        }
        showErrorMessage(null);
    }

    private void showDetailsFragment(CardData programData){
        //isChannelClickble = false;
        if (programData == null || programData.globalServiceId == null) {
            isChannelOpen = false;
            return;
        }
        MainActivity mActivity = (MainActivity) getActivity();
        Bundle args = new Bundle();
        CacheManager.setSelectedCardData(programData);

        args.putString(CardDetails.PARAM_CARD_ID, programData.globalServiceId);
        args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
        if (null != programData.startDate
                && null != programData.endDate) {
            Date startDate = Util.getDate(programData.startDate);
            Date endDate = Util.getDate(programData.endDate);
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
        args.putInt(CardDetails.PARAM_EPG_DATE_POSITION,datePos);
        String adProvider = null;
        boolean adEnabled = false;
        if (programData != null
                && programData.content != null){
            if (!TextUtils.isEmpty(programData.content.adProvider)) {
                adProvider = programData.content.adProvider;
            }
            adEnabled = programData.content.adEnabled;
        }
        args.putString(CardDetails.PARAM_AD_PROVIDER, adProvider);
        args.putBoolean(CardDetails.PARAM_AD_ENBLED, adEnabled);
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_LIVE);
        if (programData != null
                && programData.generalInfo != null
                && programData.generalInfo.title != null)
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, programData.generalInfo.title);

        mActivity.showDetailsFragment(args,programData);


    }

    public void onEventMainThread(EventNotifyEpgAdapter event) {
        if (mEpgListAdapter != null) {
            mEpgListAdapter.notifyDataSetChanged();
        }
    }
}
