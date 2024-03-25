package com.myplex.myplex.ui.fragment.epg;

import static com.myplex.myplex.ui.fragment.epg.EPGView.CURRENT_PROGRAM;
import static com.myplex.myplex.ui.fragment.epg.EPGView.FUTURE_PROGRAM;
import static com.myplex.myplex.ui.fragment.epg.EPGView.PAST_PROGRAM;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apalya.myplex.eventlogger.core.Constant;
import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.ParserException;
import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.Maps;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.model.CardData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.model.Result;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.DetailsPageDialogFragment;
import com.myplex.myplex.ui.views.ObservableScrollView;
import com.myplex.myplex.ui.views.ObservableScrollViewCallbacks;
import com.myplex.myplex.ui.views.ScrollState;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EPGFragment#} factory method to
 * create an instance of this fragment.
 */

public class EPGFragment extends BaseFragment implements ObservableScrollViewCallbacks, TabLayout.OnTabSelectedListener {
    private final static int INITIAL_OFFSET = 20; //50
    private final static int PAGINATION_OFFSET = 1;
    private EPGView epg;
    private TabLayout tabLayout;
    private ObservableScrollView scrollView;
    private ImageView left_arrow, right_arrow;
    private ProgressBar epgProgress;
    private int todayTabSelectedIndex = 0;
    private boolean mIsLoadingMoreAvailable = true;
    private int mStartIndex = 1;
    EPG epgresponse;
    private int selectedTabIndex = -1;
    int selectedOption = -1;
    String selectedRecordValue = "";
    private EPGDataImpl epgDataImplObject;
    private List<EPG.EPGTab> tabs = null;
    private HashMap<String, Boolean> tabDataTracker = new HashMap<String, Boolean>();
    private Timer timer;
    private RelativeLayout progressLayout, noEpgLayout, mSideArrowsLayout;
    private TextView channelName;
    private View view;
    // private FragmentHost mFragmentHost;
    private FragmentActivity mActivity;
    //private Preferences mPreferenceUtils;
    private TextView nodata;
    private boolean tabSelected = false;
    private Bundle mBundle;
    private String targetPage, title, navFromPath, filters, eventName;
    private boolean initialRequest = true;
    private Dialog eventInfoDialog;
    private int pageNo;
    HashMap<String, EPGScroll> epgScrollHashMap = new HashMap<String, EPGScroll>();
    private boolean loadMoreInprogress = false;
    private String totalEpgStartTime, totalEpgEndTime;
    private int pagePosition = 0;// starting pageposition from 1 as we are fetching initially 6 channels and 3 channels on pagination.So Page no's should be 0,2,3,4.... (Multiples of 3 )
    private List<EPG.EPGTab> epgTabs;
    private boolean isVisible = false;
    // private TextView dummyTextView;
    // private View dummyChannelView;
    private RadioGroup mDvrRadioGroup;
    // private BottomDataModel mBottomDataModel = null;
    private String templateCode = null;
    private int mPortraitWidthInPixels = 0;
    Spinner dateSpinner;
    TextView categoriesTV, selectedDateTV;
    String selectedGenres = "";
    private boolean isCardClicked = true;
    public boolean isFirstTime = true;
    String programStartDate=null;
    RecyclerView categoryRecyclerView;
    private List<CardData> mDataList = new ArrayList<>();
    int position = 0;
    //CategoryRecyclerViewAdapter categoryRecyclerViewAdapter;
    String filterString = "partnerCode:sonyliv,yupptv;payType:F";
    private Handler ClickHandler = new Handler();
    private final SimpleDateFormat mFullDateTimeFormat = new SimpleDateFormat("EE, MMM dd, hh:mm a", Locale.getDefault());
    private Runnable isCardClickedRunnable = new Runnable() {
        @Override
        public void run() {
            isCardClicked = true;
        }
    };
    public static EPGFragment instance = null;

    public static EPGFragment getInstance() {
        return instance;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBundle = getArguments();
        if (epg != null) {
            epg.setActivityContext(mActivity);
        }
        if (mPortraitWidthInPixels < 1)
            mPortraitWidthInPixels = getResources().getDisplayMetrics().widthPixels;
        if (epg != null)
            epg.setWidthInPixels(mPortraitWidthInPixels);
        //TODO::GOPI
        // FetchEPG(null, null);
        fetchEPGChannel();
    }

    private void initTimeIndicatorTimer() {
        TimerTask timerTask = new TimeBarIndicator();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 60 * 1000);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser /*&& isResumed()*/) {
            if (mActivity == null) {
                return;
            }

        } else {
            isVisible = false;
        }


    }

    String filterCode = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.epg_main_layout, container, false);
            setRetainInstance(true);
            selectedGenres = "";
            progressLayout = (RelativeLayout) view.findViewById(R.id.progresslayout);
            mSideArrowsLayout = (RelativeLayout) view.findViewById(R.id.sidearrows_layout);
            noEpgLayout = (RelativeLayout) view.findViewById(R.id.noepglayout);
            channelName = (TextView) view.findViewById(R.id.channelName);
//            nodata =  (TextView) view.findViewById(R.id.noepg);
            dateSpinner = (Spinner) view.findViewById(R.id.dateSpinner);
            categoryRecyclerView = (RecyclerView) view.findViewById(R.id.categories_recycler_view);
            categoriesTV = (TextView) view.findViewById(R.id.categoriesTV);
            selectedDateTV = (TextView) view.findViewById(R.id.selectedDateTV);
            epg = (EPGView) view.findViewById(R.id.epg);
            scrollView = (ObservableScrollView) view.findViewById(R.id.scroll);
            left_arrow = (ImageView) view.findViewById(R.id.left_arrow);
            right_arrow = (ImageView) view.findViewById(R.id.right_arrow);
            epgProgress = (ProgressBar) view.findViewById(R.id.epgProgress);
            ObjectAnimator anim = ObjectAnimator.ofInt(scrollView, "scrollY", scrollView.getBottom());
            anim.setDuration(2000);
            anim.start();

            scrollView.getLayoutParams().height = ApplicationController.getApplicationConfig().screenHeight;
            categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            categoryRecyclerView.setHasFixedSize(true);
            left_arrow.setVisibility(View.GONE);
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
            channelName.setText(robotoStringFont(getResources().getString(R.string.channels)));
            noEpgLayout.setVisibility(View.GONE);
            tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
            tabLayout.setHorizontalScrollBarEnabled(true);
            tabLayout.addOnTabSelectedListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tabLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        showHideArrows();
                    }
                });
            }
            scrollView.setScrollViewCallbacks(this);
            if (mPortraitWidthInPixels < 1)
                mPortraitWidthInPixels = getResources().getDisplayMetrics().widthPixels;
            filterCode = "genreCode";
            if (epg != null) {
                epg.clearEPGImageCache();
                epg.setWidthInPixels(mPortraitWidthInPixels);
            }
            epg.setEPGObject(epg);
            epg.setEPGClickListener(new EPGClickListener() {
                @Override
                public void onChannelClicked(int channelPosition, EPG.EPGChannel epgChannel, int programPosition, String channel_id, EPG.EPGProgram epgEvent) {
                    //highLight the selected box
                    if (isCardClicked) {
//                        showProgress(true);
                       /* highLightBox(epgEvent, channelPosition, true);
                        isCardClicked = false;
                        ClickHandler.postDelayed(isCardClickedRunnable, 1000);
                        // RestAdapter.enableCache(false);
                        clickEvent(channelPosition, programPosition, channel_id, epgEvent);*/
                    }
                }

                int programPosition, startPosition, count;
                @Override
                public void onEventClicked(final int channelPosition,  int programPosition1, final String channel_id, final EPG.EPGProgram epgEvent) {
                    // Toast.makeText(mActivity, "11 clicked", Toast.LENGTH_SHORT).show();
                    if (isCardClicked) {
//                        showProgress(true);
                        //highLight the selected box
                        programPosition = programPosition1;
                        startPosition = 0;
                        count = 0;
                        highLightBox(epgEvent, channelPosition, false);
                        isCardClicked = false;
                        ClickHandler.postDelayed(isCardClickedRunnable, 1000);

                     //   if(epgEvent.isCatchup().equalsIgnoreCase("false")) {
                        
                        //Showing the toast message when the catchup content is not available which means,
                        // when exceeds 24 hrs time the catchup data wont be available to play, so showing the toast message
                        Calendar calendar = Calendar.getInstance();
                        long currentTime = calendar.getTimeInMillis();
                        long programStartTime=0;
                        long totalIntervalTime=0;
                        try {
                            if (epgresponse != null && epgresponse.getData() != null && epgresponse.getData().get(channelPosition) != null
                                    && epgresponse.getData().get(channelPosition).getCardPrograms() != null
                                    && epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition) != null
                                    && epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition).getTitle() != null) {
                                programStartDate = epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition).startDate;
                            }
                            programStartTime = Util.parseXsDateTime(programStartDate);
                            totalIntervalTime=currentTime-programStartTime;//totalIntervalTime in milliseconds

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                            int programType1 = PAST_PROGRAM;
                            boolean isfutureProgram = isFutureProgram(getEventStartTime(epgEvent));
                            boolean iscurrentProgram = isfutureProgram ? false : isCurrent(getEventStartTime(epgEvent),getEventEndTime(epgEvent));
                            if(isfutureProgram)
                                programType1 = FUTURE_PROGRAM;
                            else if(iscurrentProgram)
                                programType1 = CURRENT_PROGRAM;
                            if(programType1 == PAST_PROGRAM)
                            if(epgEvent.isCatchup().equalsIgnoreCase("false")) {
                                return;
                            } else {
                                if(totalIntervalTime>86400000){//when convert 24hrs to milliseconds we will get 86400000
                                    Toast.makeText(mContext,"Catch up is not available beyond 24 hrs",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                showDetailsFragment(epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition), -1, epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition).getTitle(), -1, true);
                                return;
                            }
                            if(programType1 == CURRENT_PROGRAM) {
                              //  showDetailsPage(0, "", epgresponse.getData().get(channelPosition).getCardPrograms().subList(programPosition, epgresponse.getData().get(channelPosition).getCardPrograms().size()), epgresponse.getData().get(channelPosition).getPrograms().subList(programPosition, epgresponse.getData().get(channelPosition).getCardPrograms().size()));
                                showDetailsFragment(epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition), -1, epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition).getTitle(), -1, false);
                            }
                            else {
                               // if(mDataList.size() != 0)
                                if(epgresponse != null && epgresponse.getData() != null && epgresponse.getData().size() > 0 &&  epgresponse.getData().get(channelPosition).getCardPrograms() != null &&
                                        epgresponse.getData().get(channelPosition).getCardPrograms().size() >0 && epgresponse.getData().get(channelPosition).getPrograms() != null &&
                                        epgresponse.getData().get(channelPosition).getPrograms().size()>0) {
                                    if(epgEvent.isCatchup().equalsIgnoreCase("false")) {
                                        for(int i=0 ; i <  epgresponse.getData().get(channelPosition).getPrograms().size(); i++) {
                                            EPG.EPGProgram event = epgresponse.getData().get(channelPosition).getPrograms().get(i);
                                            long eventStartTime = getEventStartTime(event);
                                            long eventEndTime = getEventEndTime(event);
                                            int programType = PAST_PROGRAM;
                                            boolean isfutureProgram1 = isFutureProgram(eventStartTime);
                                            boolean iscurrentProgram1 = isfutureProgram1 ? false : isCurrent(eventStartTime,eventEndTime);
                                            if(isfutureProgram1)
                                                programType = FUTURE_PROGRAM;
                                            else if(iscurrentProgram1)
                                                programType = CURRENT_PROGRAM;
                                            if(programType == CURRENT_PROGRAM) {
                                                startPosition = i;
                                                count = 0;
                                            }
                                            count++;
                                            if(epgEvent.getDisplay().getTitle().equalsIgnoreCase(event.getDisplay().getTitle())) {
                                                programPosition = count - 1;
                                            }
                                        }
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                showDetailsPage(programPosition, "", epgresponse.getData().get(channelPosition).getCardPrograms().subList(startPosition, epgresponse.getData().get(channelPosition).getCardPrograms().size()), epgresponse.getData().get(channelPosition).getPrograms().subList(startPosition, epgresponse.getData().get(channelPosition).getPrograms().size()));
                                            }
                                        },500);

                                    } else
                                        showDetailsPage(programPosition, "", epgresponse.getData().get(channelPosition).getCardPrograms(), epgresponse.getData().get(channelPosition).getPrograms());
                                }
                            }
                       // }
                      /*  else {
                           // showDetailsPage(programPosition, "", epgresponse.getData().get(channelPosition).getCardPrograms(), epgresponse.getData().get(channelPosition).getPrograms());
                            showDetailsFragment(epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition), -1, epgresponse.getData().get(channelPosition).getCardPrograms().get(programPosition).getTitle(), -1, true);
                        }*/
                        // Toast.makeText(mActivity, epgEvent.getDisplay().getTitle() + " clicked", Toast.LENGTH_SHORT).show();
                        //  showEventInformationDialog(channelPosition,programPosition,epgEvent);
                        // showDialogWithOptions();
            /*    ActionBottomSheetDialogFragment bottomSheetDialogFragment = ActionBottomSheetDialogFragment.newInstance();
                bottomSheetDialogFragment.show(mActivity.getSupportFragmentManager(),"bottom_epg_layout");*/
                        //clickEvent(channelPosition, programPosition, channel_id, epgEvent);
                        // NavigationUtils.performItemClickNavigation(mActivity,epgEvent.getTarget().getPath());
                    }
                }

                @Override
                public void onChannelClicked(int channelPosition, EPG.EPGChannel epgChannel) {

                }


                @Override
                public void onResetButtonClicked() {
                    //   selectedTabIndex = todayTabSelectedIndex;
                    if (tabDataTracker != null && (!tabDataTracker.containsKey("" + todayTabSelectedIndex) || !tabDataTracker.get("" + todayTabSelectedIndex))) {

                        //if (tabDataTracker != null && todayTabSelectedIndex > 0 && todayTabSelectedIndex < tabs.size() && (!tabDataTracker.containsKey("" + (todayTabSelectedIndex - 1)) || !tabDataTracker.get("" + (todayTabSelectedIndex - 1))))
                        //      FetchEPG("" + tabs.get(todayTabSelectedIndex - 1).getStartTime(), "" + tabs.get(todayTabSelectedIndex - 1).getEndTime(), todayTabSelectedIndex - 1);


                        //  fetchepg = true;
                        FetchEPG("" + tabs.get(todayTabSelectedIndex).getStartTime(), "" + tabs.get(todayTabSelectedIndex).getEndTime(), todayTabSelectedIndex);
                    }

                    tabSelected = true;
                    epg.recalculateAndRedraw(true);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    tabSelected = false;
                                }
                            }, 500);

                }

                @Override
                public void updateScroll(int value) {
                    //CustomLog.e("update scroll", "  value = " + value);
                    //final int tabPosition = value + todayTabSelectedIndex;

         /*       if(tabSelected) {
                    if (tabLayout != null) {
                        tabLayout.setScrollPosition(tabPosition, 0.0F, true);
                        selectedTabIndex = tabPosition;
                    }
                    return;
                }*/

//Fetch previous days data
              /*  if (tabDataTracker != null && tabPosition > 0 && tabPosition < tabs.size() && (!tabDataTracker.containsKey("" + (tabPosition - 1)) || !tabDataTracker.get("" + (tabPosition - 1)))) {
                    FetchEPG("" + tabs.get(tabPosition - 1).getStartTime(), "" + tabs.get(tabPosition - 1).getEndTime(), tabPosition - 1);
                }*/
                    //CustomLog.e("EPGFrndment","updateScroll "+value);
       /*             if (initialRequest || (getActivity() != null && ((MainActivity)getActivity()).isEPGRefreshed)) {
                        if (!isBackgroundRequestCompleted && tabLayout != null)
                            tabLayout.setScrollPosition(selectedTabIndex, 0.0F, true);
                        ((MainActivity)getActivity()).isEPGRefreshed = false;
                        return;
                    }
                    CustomLog.e("currentTimeMillis", ""+System.currentTimeMillis());
                    CustomLog.e("currentTimeMillis", ""+tabs.get(todayTabSelectedIndex).getEndTime());
                    if (System.currentTimeMillis() >= tabs.get(todayTabSelectedIndex).getEndTime()) {
                        updateDaysTab();
                    }else {
                        if (tabDataTracker != null && !(tabPosition < 0) && tabPosition < tabs.size() && (!tabDataTracker.containsKey("" + tabPosition) || !tabDataTracker.get("" + tabPosition))) {
                            FetchEPG("" + tabs.get(tabPosition).getStartTime(), "" + tabs.get(tabPosition).getEndTime(), tabPosition);
                            requestUserEPGData("" + tabs.get(tabPosition).getStartTime(), "" + tabs.get(tabPosition).getEndTime());
                        }
                        if (tabLayout != null) {
                            if (selectedTabIndex > -1 && selectedTabIndex < tabLayout.getTabCount())
                                selectText(tabLayout.getTabAt(selectedTabIndex), R.color.epg_date_tab_Text_color);
                            tabLayout.setScrollPosition(tabPosition, 0.0F, true);
                            selectedTabIndex = tabPosition;
                            selectText(tabLayout.getTabAt(tabPosition), R.color.epg_date_tab_selectedText_color);
                        }
                    }*/

                }

                @Override
                public void loadMore() {
                    Log.e("loadMore method", "  loadMore method " + applyFilters());


                    EPGScroll scrollObject = null;
                    if (epgScrollHashMap != null && epgScrollHashMap.containsKey("" + selectedTabIndex)) {
                        scrollObject = epgScrollHashMap.get("" + selectedTabIndex);
                    } else if (epgScrollHashMap == null) {
                        epgScrollHashMap = new HashMap<String, EPGScroll>();
                    }
                    Log.e("loadMore method", "  loadMore method " + scrollObject);
                    if (scrollObject == null) {
                        scrollObject = new EPGScroll();
                        scrollObject.pageValue = 45;
                        scrollObject.index = selectedTabIndex;
                        scrollObject.loadMore = true;
                        epgScrollHashMap.put("" + selectedTabIndex, scrollObject);
                    }
                    //to handle pagination for loadmore(24 hours data) need to send starttime and endtime as null

                    /*Commneted below hashmap flow, might need later*/
                /* Log.e("loadMore","++++++"+scrollObject.toString());
                if(scrollObject != null && scrollObject.loadMore) {
                    String filter = null;
                    if(filters != null && filters.length() > 3)
                        filter = filters;
                    if(tabs != null && selectedTabIndex > -1 && selectedTabIndex < tabs.size())
                        FetchEPG("" + tabs.get(selectedTabIndex).getStartTime(), "" + tabs.get(selectedTabIndex).getEndTime(), filter, scrollObject.pageValue, PAGINATION_OFFSET, selectedTabIndex);
                }*/
                    if (mIsLoadingMoreAvailable) {
                       // mStartIndex++;
                        FetchEPG(null, null, applyFilters(), pagePosition, INITIAL_OFFSET, selectedTabIndex);
                        //   FetchEPG(null, null);
                    }

                }

                @Override
                public void showToolTip() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //epg.getChildCountoFView();
                            //   epg.setDummyViews(dummyTextView,dummyChannelView);
                            //epg.handleGuidePopup(mActivity,isAdded(),true,EPGFragment.this);
                        }
                    }, 100);
                }
            });


            mBundle = getArguments();
           /* if (mBundle != null && mBundle.containsKey(NavigationConstants.MENU_ITEM_CODE)) {
                targetPage = mBundle.getString(NavigationConstants.MENU_ITEM_CODE);
            }*/
            //FetchEPG(null, null);
            //fetchTemplate(APIUtils.getInstance(mActivity));
        } catch (InflateException e) {
            e.printStackTrace();
            /* map is already there, just return view as it is */
        }


        return view;
    }

    //to check whether event is finished event
    public boolean isEventFinished(EPG.EPGProgram event) {

        long startTime, endTime;
        startTime = epg.getEventStartTime(event);
        endTime = epg.getEventEndTime(event);
        long currentTimeMillis = System.currentTimeMillis();
        if (event != null && event.getTarget().getPageAttributes().getIsLive() != null && event.getTarget().getPageAttributes().getIsLive().equalsIgnoreCase("true")) {
            if (startTime < currentTimeMillis && endTime < currentTimeMillis)
                return true;
        }
        return false;
    }

    public long getEventStartTime(EPG.EPGProgram event) {
        if (event != null && event.getDisplay().getMarkers() != null) {

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            int size = markers.size();
            for (int i = 0; i < size; i++) {
                String markertype = markers.get(i).getMarkerType();
                try {
                    if (markertype.equalsIgnoreCase("startTime"))
                        return (Long.parseLong(markers.get(i).getValue()) + DateHelper.getInstance().getTimezoneOffsetValue());
                } catch (NumberFormatException e) {

                }

            }

        }
        return -1l;
    }

    public boolean isCurrent(long eventstarttime,long eventendtime) {

        long now = getCurrentTimeInMillis();
        //CustomLog.e("EPG"," event start time :"+getEventStartTime(event));
        //CustomLog.e("EPG"," event end time :"+getEventEndTime(event));
        // CustomLog.e("EPG"," event now time :"+now);
        return now >= eventstarttime && now <= eventendtime;
    }

    public long getCurrentTimeInMillis() {
        return DateHelper.getInstance().getCurrentLocalTime();

        // return System.currentTimeMillis();
    }

    public boolean isFutureProgram(long eventstarttime){
        long now = getCurrentTimeInMillis();
        return now < eventstarttime;
    }

    public long getEventEndTime(EPG.EPGProgram event) {
        if (event != null && event.getDisplay().getMarkers() != null) {
            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            int size = markers.size();
            for (int i = 0; i < size; i++) {
                try {
                    if (markers.get(i).getMarkerType().equalsIgnoreCase("endTime"))
                        return (Long.parseLong(markers.get(i).getValue()) + DateHelper.getInstance().getTimezoneOffsetValue());
                } catch (NumberFormatException e) {

                }

            }
        }
        return -1l;
    }

    public void showHideArrows() {
        if (tabLayout.getScrollX() <= 30) {
            left_arrow.setVisibility(View.GONE);
        } else {
            left_arrow.setVisibility(View.VISIBLE);
        }
        Log.v("x-axis", "getScrollX " + tabLayout.getScrollX());
        Log.v("x-axis", "getX " + tabLayout.getX());
        Log.v("x-axis", "getWidth  " + tabLayout.getWidth());
        Log.v("x-axis", "screenWidth " + ApplicationController.getApplicationConfig().screenWidth);
        if (tabLayout.getScrollX() + tabLayout.getWidth() - (tabLayout.getWidth() / 2) > ApplicationController.getApplicationConfig().screenWidth || DeviceUtils.isTabletAndLandscape(mContext)) {
            right_arrow.setVisibility(View.GONE);
        } else {
            right_arrow.setVisibility(View.VISIBLE);
        }

    }

    private void showDetailsFragment(CardData carouselData, int position,String carousalTitle,int parentPosition, boolean isSupportCatchup) {

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
        args.putBoolean(CardDetails.PARAM_SUPPORT_CATCHUP, isSupportCatchup);
        args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
        ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
    }
    private void showDetailsPage(int selectedPosition, String categoryTitle, List<CardData> carouselInfoDataList, List<EPG.EPGProgram> epgProgramList) {
     /*   if (isAdded() && DetailsPageDialogFragment.getInstance() != null) {
            DetailsPageDialogFragment.getInstance().dismiss();
        }*/
       /* Fragment fragment = new DetailsPageDialogFragment(selectedPosition,categoryTitle,carouselInfoDataList);
        FragmentManager fm = ((AppCompatActivity)mContext).getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(fragment.getClass().getSimpleName());
        ft.replace(R.id.content_searchview, fragment);
        ft.commit();*/
        DialogFragment newFragment = DetailsPageDialogFragment.newInstance(mContext);
        if(((MainActivity) mContext).isMediaPlaying()){
            ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.onPause();
        }
        if (selectedTabIndex == 0)
            ApplicationController.DATE_POSITION = -1;
        else if (selectedTabIndex == -1 || selectedTabIndex == 1)
            ApplicationController.DATE_POSITION = 0;
        else
            ApplicationController.DATE_POSITION = selectedTabIndex - 1;
        final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String selectedDateInString = format.format(date);
        String dateStamp = Util.getYYYYMMDD(selectedDateInString);
        String dayMonthYear = Util.getDayMonthYear(selectedDateInString).toString();
        // Log.d("date","strings..."+dayMonthYear);
        ((DetailsPageDialogFragment) newFragment).setData(selectedPosition, categoryTitle, carouselInfoDataList, epgProgramList, dayMonthYear);
        newFragment.show(getFragmentManager(), "dialog");

     /*   WindowManager.LayoutParams lp = newFragment.getDialog().getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        DetailsPageDialogFragment.getInstance().getDialog().getWindow().setAttributes(lp);*/
        // newFragment.getDialog().getWindow().setAttributes(lp);
    }


    //fetch epg based on channel id
    private void fetchEPGChannel() {
        updateProgress(true);
        epgProgress.setVisibility(View.VISIBLE);
        final CacheManager mCacheManager = new CacheManager();
        // int todayPosition = PrefUtils.getInstance().getPrefEnablePastEpg() ? PrefUtils.getInstance().getPrefNoOfPastEpgDays() : ApplicationController.DATE_POSITION;
        Log.e("EPGFragment", "selectedTabIndex " + selectedTabIndex);
        if (selectedTabIndex == 0)
            ApplicationController.DATE_POSITION = -1;
        else if (selectedTabIndex == -1 || selectedTabIndex == 1)
            ApplicationController.DATE_POSITION = 0;
        else
            ApplicationController.DATE_POSITION = selectedTabIndex - 1;
        final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String selectedDateInString = format.format(date);
        String dateStamp = Util.getYYYYMMDD(selectedDateInString);
        updateProgress(true);
        fetchEPGChannels(new EPGChannelImplementation() {
            @Override
            public void onSuccess(List<CardData> cardDataList) {
                if(cardDataList != null && cardDataList.size() > 0) {
                    List<EPG.EPGData> allChannelProgramList = new ArrayList();
                    String mChannelIds = "";
                    for (int i = 0; i < cardDataList.size(); i++) {
                        String id = cardDataList.get(i).globalServiceId;
                        if (id != null) {
                            mChannelIds = mChannelIds + cardDataList.get(i).globalServiceId;
                            if (i != cardDataList.size() - 1)
                                mChannelIds = mChannelIds + ",";
                        }
                    }
                    Log.e("EPG Fragment", "fetchEPGChannelsdata " + mChannelIds);
                    ChannelListEPG.Params params = new ChannelListEPG.Params(mChannelIds, dateStamp);
                    ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
                        @Override
                        public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                            if (isAdded()) {
                                tabDataTracker = ((MainActivity) getActivity()).getEpgDataTracker();
                                epgresponse = new EPG();
                                // EPG epgresponse = (EPG) response;
                                epgresponse.setTiltle("TV Guide");
                                //  List<EPG.EPGTab> epgTabs = new ArrayList<>();
                                List<EPG.EPGTab> epgTabs = Util.showNextDatesEPG(true);
                                epgresponse.setTabs(epgTabs);
                                if (response.body()!=null && response.body().getResults() != null && response.body().getResults().size()>0) {
                                    for (int i = 0; i < response.body().getResults().size(); i++) {
                                        allChannelProgramList.add(getEPGPrograms(response.body().getResults().get(i).getPrograms()));
                                    }
                                    epgresponse.setData(allChannelProgramList);
                                    if (epgresponse.getTabs() != null && epgresponse.getTabs().size() > 0) {
                                        tabs = epgresponse.getTabs();
                                        // if (startTime == null) {
                                        updateTabs(epgresponse.getTabs());
                                        //}
                                        updateEPGScrollData(todayTabSelectedIndex, true);
                                        initTimeIndicatorTimer();
                                        prepareEPGData(epgresponse.getData(), false,true);
                                        if (epgDataImplObject != null && epgDataImplObject.getChannelCount() > 0) {
                                            epgDataImplObject.setEPGPrograms(0, epgresponse.getData().get(0).getPrograms());
                                            epg.redraw();
                                        }
                                        Log.e("epg fragment", "fetch epgdata success3");
                                        Log.e("epg fragment", "fetch epgdata success4 " + epgDataImplObject);
                                        updateProgress(false);
                                        epgProgress.setVisibility(View.GONE);
                                    } else {
                                        updateProgress(false);
                                        noEpgLayout.setVisibility(View.VISIBLE);
                                        epgProgress.setVisibility(View.GONE);
                                    }

                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            if (isAdded()) {
                                Log.e("EPG Fragment", "fetchEPGChannelsdata7");
                                loadMoreInprogress = false;
                                updateProgress(false);
                                if (getActivity() == null || getActivity().isFinishing())
                                    return;
                                epg.setVisibility(View.GONE);
                                epgProgress.setVisibility(View.GONE);
                            }
                        }
                    });
                    APIService.getInstance().execute(channelListEPG);
                } else {
                    Log.e("EPG Fragment", "fetchEPGChannelsdata7");
                    loadMoreInprogress = false;
                    updateProgress(false);
                    if (getActivity() == null || getActivity().isFinishing())
                        return;
                    epg.setVisibility(View.GONE);
                    epgProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure() {
                if (isAdded()) {
                    Log.e("EPG Fragment", "fetchEPGChannelsdata7");
                    loadMoreInprogress = false;
                    updateProgress(false);
                    if (getActivity() == null || getActivity().isFinishing())
                        return;
                    epg.setVisibility(View.GONE);
                    epgProgress.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // this.mFragmentHost = (FragmentHost) activity;
        mActivity = getActivity();
        //  mPreferenceUtils = Preferences.instance(mActivity);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //  this.mFragmentHost = (FragmentHost) context;
        mActivity = getActivity();
        //  mPreferenceUtils = Preferences.instance(mActivity);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFirstTime = true;
        tabLayout.smoothScrollTo(0,0);
        //  Log.e("isCurrent","onResume");
        if (epg != null) {
            //       Log.e("isCurrent","onResume");
            epg.viewInvalidate(false);
            epg.redraw();

            epg.viewInvalidate(false);
        }
        if(scrollView!=null) {
            scrollView.getLayoutParams().height = ApplicationController.getApplicationConfig().screenHeight;
        }

        // if(getUserVisibleHint()){
        //handleGuidePopup();
        //}
    }

    private void updateTabs(List<EPG.EPGTab> epgTabs) {
        this.epgTabs = epgTabs;
        String epgStartTime = "", epgEndTime = "";

        todayTabSelectedIndex = 0;
        for (int i = 0; i < epgTabs.size(); i++) {
            if (i == 0)
                totalEpgStartTime = "" + epgTabs.get(i).getStartTime();

            if (i == epgTabs.size() - 1)
                totalEpgEndTime = "" + epgTabs.get(i).getEndTime();

            //  tabDataTracker.put(""+i,true);
            if(i == 0)
                tabLayout.addTab(tabLayout.newTab().setText(robotoStringFont(epgTabs.get(i).getTitle())));
            else
                tabLayout.addTab(tabLayout.newTab().setText(robotoStringFont(epgTabs.get(i).getTitle())));
            if (epgTabs.get(i).getIsSelected() && epgTabs.get(i).getTitle().equalsIgnoreCase(getString(R.string.today))) {
                todayTabSelectedIndex = i;
            }
            View root = tabLayout.getChildAt(0);
            //TODO : enable below code for tab dividers and change color code accordingly
            if (root instanceof LinearLayout) {
                ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(getResources().getColor(R.color.epg_date_tab_divider_color));
                drawable.setSize(3, 1);
                ((LinearLayout) root).setDividerPadding(20);
                ((LinearLayout) root).setDividerDrawable(drawable);
            }
        }

        //   tabDataTracker.put(""+todayTabSelectedIndex,true);
        //updateDataTracker("" + todayTabSelectedIndex, true);
        Log.e("todayTabSelectedIndex", todayTabSelectedIndex + "");
        if (epg != null && epgTabs != null)
            epg.updateDurationsInMillis(todayTabSelectedIndex, (epgTabs.size() - 1) - todayTabSelectedIndex);

        if (tabLayout != null && tabLayout.getTabCount() > 0) {
            selectedTabIndex = todayTabSelectedIndex;
            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            initialRequest = false;
                            if(tabLayout.getTabAt(todayTabSelectedIndex) != null)
                                tabLayout.getTabAt(todayTabSelectedIndex).select();

                            //handleGuidePopup();

                        }
                    }, 50);
        }

    }

    @Override
    public void onDestroy() {
        if (epg != null) {
            epg.clearEPGImageCache();
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (epg != null)
            epg = null;
        if (epgDataImplObject != null)
            epgDataImplObject = null;

        cancelTimeBarTimer();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "amazon_ember_cd_bold.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private EPG.EPGData getEPGPrograms(List<CardData> cardData) {

        EPG.EPGData epgData = new EPG.EPGData();
        // channel
        EPG.EPGChannel epgChannel = new EPG.EPGChannel();
        epgChannel.setmNetworkInfo(null);
        epgChannel.setTarget(null);
        if (cardData != null && cardData.size()> 0 && cardData.get(0) != null) {
            // meta data
            EPG.EPGMetadata metadata = new EPG.EPGMetadata();
            metadata.setId(cardData.get(0).globalServiceId);
            metadata.setMonochromeImage(cardData.get(0).getImageLink(APIConstants.IMAGE_TYPE_THUMBNAIL));
            if (cardData.get(0) != null && cardData.get(0).content != null && cardData.get(0).content.channelNumber != null && !cardData.get(0).content.channelNumber.isEmpty())
                metadata.setChannelNumber(cardData.get(0).content.channelNumber);
            epgChannel.setMetadata(metadata);
            // PosterDisplayChannel
            EPG.PosterDisplayChannel posterDisplayChannel = new EPG.PosterDisplayChannel();
            posterDisplayChannel.setMarkers(null);
            posterDisplayChannel.setTitle(cardData.get(0).getTitle());
            posterDisplayChannel.setImageUrl(cardData.get(0).getImageLink(APIConstants.IMAGE_TYPE_THUMBNAIL));
            epgChannel.setDisplay(posterDisplayChannel);
            epgData.setChannel(epgChannel);
            List<EPG.EPGProgram> epgPrograms = new ArrayList<>();
            for (int j = 0; j < cardData.size(); j++) {
                EPG.EPGProgram epgProgram = new EPG.EPGProgram();
                // network info
                epgProgram.setmNetworkInfo(null);
                // meta data
                EPG.EPGMetadata metadata1 = new EPG.EPGMetadata();
                metadata1.setId(cardData.get(j).globalServiceId);
                epgProgram.setMetadata(metadata1);
                // template code
                epgProgram.setTemplate("");
                if(cardData.get(j).content.isSupportCatchup != null) {
                    if(cardData.get(j).content.isSupportCatchup.isEmpty() || cardData.get(j).content.isSupportCatchup.equalsIgnoreCase("false"))
                        epgProgram.setCatchup("false");
                    else
                        epgProgram.setCatchup("true");
                } else
                    epgProgram.setCatchup("false");


                // poster display
                EPG.PosterDisplay posterDisplay = new EPG.PosterDisplay();
                List<EPG.PosterDisplay.Marker> markerList = new ArrayList<>();
                try {
                    EPG.PosterDisplay.Marker startMarker = new EPG.PosterDisplay.Marker();
                    startMarker.setMarkerType("startTime");
                    long startTime = Util.parseXsDateTime(cardData.get(j).startDate);
                  //  Log.e("startTime", " "+startTime);
                    startMarker.setValue(String.valueOf(startTime));
                    EPG.PosterDisplay.Marker endMarker = new EPG.PosterDisplay.Marker();
                    endMarker.setMarkerType("endTime");
                    long endTime = Util.parseXsDateTime(cardData.get(j).endDate);
                    endMarker.setValue(String.valueOf(endTime));
                    markerList.add(startMarker);
                    markerList.add(endMarker);
                } catch (ParserException e) {
                    e.printStackTrace();
                }
                posterDisplay.setMarkers(markerList);
                posterDisplay.setTitle(cardData.get(j).getTitle());
                posterDisplay.setImageUrl(cardData.get(j).getImageLink(APIConstants.IMAGE_TYPE_THUMBNAIL));

                epgProgram.setDisplay(posterDisplay);

                epgPrograms.add(epgProgram);
            }
            epgData.setPrograms(epgPrograms);
            epgData.setCardPrograms(cardData);
        }
        return epgData;
    }

    private void FetchEPG(final String startTime, String endTime) {

        if (epg == null && view == null)
            return;
        if (epg == null && view != null)
            epg = view.findViewById(R.id.epg);

        // ((MainActivity) mActivity).doEPGCacheRefresh = true;
        updateProgress(true);

//        nodata.setVisibility(View.GONE);
        noEpgLayout.setVisibility(View.GONE);
        epg.setVisibility(View.GONE);
        categoryRecyclerView.setVisibility(View.GONE);
        loadMoreInprogress = true;
        Log.e("filter", "++++++++++ " + applyFilters());
        if (selectedTabIndex == 0)
            ApplicationController.DATE_POSITION = -1;
        else if (selectedTabIndex == -1 || selectedTabIndex == 1)
            ApplicationController.DATE_POSITION = 0;
        else
            ApplicationController.DATE_POSITION = selectedTabIndex - 1;

        List<String> list = Util.generateEpgTable(ApplicationController.ENABLE_BROWSE_PAST_EPG);
        final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        boolean isDVROnly = (PrefUtils.getInstance().getPrefEnablePastEpg() && ApplicationController.DATE_POSITION - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0) ? true : false;
        String time = Util.convertDate(date.getTime());
        com.myplex.myplex.model.EPG.getInstance(Util.getCurrentDate(ApplicationController.DATE_POSITION)).findPrograms(APIConstants.PAGE_INDEX_COUNT_EPG, Util.getServerDateFormat(time, date), time, date, mStartIndex, true, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly, "", new com.myplex.myplex.model.EPG.CacheManagerCallback() {
            @Override
            public void OnlineResults(List<CardData> dataList, int pageIndex) {
//                mDataList.clear();
                mDataList = dataList;
                if (isAdded()) {
                    Log.e("epg fragment", "fetch epgdata success");
                    if (epg == null && view == null)
                        return;
                    if (epg == null)
                        epg = view.findViewById(R.id.epg);

                    epg.setVisibility(View.VISIBLE);

                    loadMoreInprogress = false;
                    if (mActivity == null || mActivity.isFinishing()) {
                        updateProgress(false);
                        return;
                    }
                    if (dataList == null)
                        mIsLoadingMoreAvailable = false;
                    if (dataList != null && dataList.size() < APIConstants.PAGE_INDEX_COUNT_EPG) {
                        mIsLoadingMoreAvailable = false;
                    }
                    if (mDataList != null && mDataList.size()>0) {
                        Log.e("epg fragment", "fetch epgdata success1");
                        fetchEPGChannel();
                    }

                }

            }

            @Override
            public void OnlineError(Throwable error, int errorCode) {
                String reason = error.getMessage() == null ? "NA" : error.getMessage();
                if (isAdded()) {
//                CustomLog.e("epgfragment","fetch epg failed");

                    loadMoreInprogress = false;
                    if (mActivity == null || mActivity.isFinishing())
                        return;
                    epg.setVisibility(View.GONE);
//                categoryRecyclerView.setVisibility(View.GONE);
//                showErrorView(true);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    updateProgress(false);
                                    //showBaseErrorLayout(true, "", error.getMessage(), errorCallback);
                                }
                            }
                            , 1000);
                }

            }
        });

    }

    /*isNextPageExists made as false when loadmore is not available
     * added the check before calling fetch data */
    boolean isNextPageExists;

    //todo to call while Pagination
    private void FetchEPG(String startTime, String endTime, String filters, int pageNo, int offset, final int tabPosition) {

        Log.e("Pagination", "came to loadmore with " + isNextPageExists);
        Log.e("Pagination", "came to loadmore in progress " + loadMoreInprogress);
        if(loadMoreInprogress)
            return;

        mStartIndex ++;
        final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String selectedDateInString = format.format(date);
        String dateStamp = Util.getYYYYMMDD(selectedDateInString);


        fetchEPGChannels(new EPGChannelImplementation() {
            @Override
            public void onSuccess(List<CardData> cardDataList) {
                if (cardDataList == null || cardDataList.size() == 0) {
                    updateProgress(false);
                    loadMoreInprogress = true;
                    //  tabSelected = false;
                    if (getActivity() == null || getActivity().isFinishing())
                        return;
                    if (!getUserVisibleHint()) {
                        updateProgress(false);
                        return;
                    }
                } else {
                    String mChannelIds = "";
                    for (int i = 0; i < cardDataList.size(); i++) {
                        String id = cardDataList.get(i).globalServiceId;
                        if (id != null) {
                            mChannelIds = mChannelIds + cardDataList.get(i).globalServiceId;
                            if (i != cardDataList.size() - 1)
                                mChannelIds = mChannelIds + ",";
                        }
                    }
                    ChannelListEPG.Params params = new ChannelListEPG.Params(mChannelIds, dateStamp);
                    ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
                        @Override
                        public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                            if (isAdded()) {
                                Log.e("epgfrag", "response received");
                                updateProgress(false);
                                loadMoreInprogress = false;
                                //  tabSelected = false;
                                if (getActivity() == null || getActivity().isFinishing())
                                    return;
                                if (!getUserVisibleHint()) {
                                    updateProgress(false);
                                    return;
                                }
                                List<EPG.EPGData> allChannelProgramList = new ArrayList<>();
                                if(response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size()>0){
                                    for(int i=0 ; i < response.body().getResults().size(); i++) {
                                        allChannelProgramList.add(getEPGPrograms(response.body().getResults().get(i).getPrograms()));
                                    }

                                }
                                // epgresponse.setData(allChannelProgramList);
                                epgresponse.getData().addAll(allChannelProgramList);
                                if (epgresponse != null) {
                                    if (allChannelProgramList.size() > 0) {
                                        updateEPGData(allChannelProgramList);
                                        updateEPGScrollData(tabPosition, true);
                                    } else {
                                        updateEPGScrollData(tabPosition, false);
                                        loadMoreInprogress = true;
                                    }
                                } else {
                                    updateEPGScrollData(tabPosition, false);
                                    loadMoreInprogress = true;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            // tabSelected = false;
                            loadMoreInprogress = true;
                            updateProgress(false);
                        }
                    });
                    APIService.getInstance().execute(channelListEPG);
                }
            }

            @Override
            public void onFailure() {
                // tabSelected = false;
                loadMoreInprogress = true;
                updateProgress(false);
            }
        });
    }

    private void updateEPGScrollData(int tabPosition, boolean loadMore) {

        Log.e("updateEPGScrollData", "+++++++" + loadMore);
        if (!loadMore)
            epg.resetScrollOffsetValue(0);
        isNextPageExists = loadMore;

        /*Commented below logic might need later*/
        /*if(epgTabs != null){
            if(epgScrollHashMap == null)
                epgScrollHashMap = new HashMap<String, EPGScroll>();

                for (int i = 0; i < epgTabs.size(); i++) {
                    EPGScroll scrollObject = new EPGScroll();
                    scrollObject.index = i;
                    scrollObject.loadMore = loadMore;
                    scrollObject.pageValue = pagePosition;
                    epgScrollHashMap.put("" + i, scrollObject);



                CustomLog.e("scrollObject", "++++++" + epgScrollHashMap.toString());
            }
        }*/

        /*Already commented*/
/*        if(epgScrollHashMap != null ){
            if(epgScrollHashMap.containsKey(""+tabPosition)){
                EPGScroll scrollObject = epgScrollHashMap.get(""+tabPosition);
                scrollObject.index = tabPosition;
                scrollObject.loadMore = loadMore;
                scrollObject.pageValue = scrollObject.pageValue+1;
                epgScrollHashMap.put(""+tabPosition,scrollObject);
            }else{
                EPGScroll scrollObject = new EPGScroll();
                scrollObject.index = tabPosition;
                scrollObject.loadMore = loadMore;
                scrollObject.pageValue = 1;
                epgScrollHashMap.put(""+tabPosition,scrollObject);

            }
        }*/

    }

    /*Spinner onItemselected api hit calls below method
     * */
    private void FetchEPG(String startTime, String endTime, final int tabPosition) {
       // loadMoreInprogress = true;
        final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String selectedDateInString = format.format(date);
        String dateStamp = Util.getYYYYMMDD(selectedDateInString);
        fetchEPGChannels(new EPGChannelImplementation() {
            @Override
            public void onSuccess(List<CardData> cardDataList) {
                if (cardDataList == null || cardDataList.size() == 0) {
                    loadMoreInprogress = false;
                    updateProgress(false);
                    //  tabSelected = false;
                    if (getActivity() == null || getActivity().isFinishing())
                        return;
                    if (!getUserVisibleHint()) {
                        updateProgress(false);
                        return;
                    }
                } else {
                    String mChannelIds = "";
                    for (int i = 0; i < cardDataList.size(); i++) {
                        String id = cardDataList.get(i).globalServiceId;
                        if (id != null) {
                            mChannelIds = mChannelIds + cardDataList.get(i).globalServiceId;
                            if (i != cardDataList.size() - 1)
                                mChannelIds = mChannelIds + ",";
                        }
                    }
                    ChannelListEPG.Params params = new ChannelListEPG.Params(mChannelIds, dateStamp);
                    ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
                        @Override
                        public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                            if (isAdded()) {
                                loadMoreInprogress = false;
                                updateProgress(false);
                                //  tabSelected = false;
                                if (getActivity() == null || getActivity().isFinishing())
                                    return;
                                if (!getUserVisibleHint()) {
                                    updateProgress(false);
                                    return;
                                }
                                List<EPG.EPGData> allChannelProgramList = new ArrayList<>();
                                if(response.body()!=null && response.body().getResults()!=null && response.body().getResults().size()>0){
                                    List<Result> results = response.body().getResults();
                                        for (int i = 0; i < results.size(); i++) {
                                            allChannelProgramList.add(getEPGPrograms(response.body().getResults().get(i).getPrograms()));
                                        }

                                }
                                epgresponse.setData(allChannelProgramList);
                                if (allChannelProgramList != null) {
                                    if (allChannelProgramList.size() > 0) {
                                        //tabDataTracker.put("" + tabPosition, true);
                                      /*  updateDataTracker("" + tabPosition, true);
                                        updateEPGData(allChannelProgramList);
                                        updateEPGScrollData(tabPosition, true);
                                        epg.recalculateAndRedraw(true);*/
                                        updateEPGScrollData(todayTabSelectedIndex, true);
                                        initTimeIndicatorTimer();
                                        prepareEPGData(epgresponse.getData(), false,false);
                                        if (epgDataImplObject != null && epgDataImplObject.getChannelCount() > 0) {
                                            epgDataImplObject.setEPGPrograms(0, epgresponse.getData().get(0).getPrograms());
                                            epg.redraw();
                                        }
                                    }

                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            // tabSelected = false;
                            loadMoreInprogress = false;
                            updateProgress(false);
                        }
                    });
                    APIService.getInstance().execute(channelListEPG);
                }
            }

            @Override
            public void onFailure() {
                // tabSelected = false;
                loadMoreInprogress = false;
                updateProgress(false);
            }
        });
    }

    private void updateDataTracker(String position, boolean value) {
        tabDataTracker.put(position, value);
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setEpgDataTracker(tabDataTracker);
        }
    }
    void updateNewData(List<EPG.EPGData> epgData) {
        Log.e("new updateData", "updateNewData ");
//        nodata.setVisibility(View.GONE);
        noEpgLayout.setVisibility(View.GONE);
        HashMap<EPG.EPGChannel, List<EPG.EPGProgram>> result = Maps.newLinkedHashMap();
        int size = epgData.size();
        for (int i = 0; i < size; i++) {
            result.put(epgData.get(i).getChannel(), epgData.get(i).getPrograms());
        }

        if (result.size() > 0) {
            if (epgDataImplObject != null)
                epgDataImplObject.updateData(result);
            /*  epg.recalculateAndRedraw(false);*/
        } else {
//                nodata.setVisibility(View.VISIBLE);
            //    hideBaseErrorLayout();
            noEpgLayout.setVisibility(View.VISIBLE);
        }
        EPGFragment.this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (epg != null) {
                    epg.redraw();
                    epg.viewInvalidate(false);
                }
                //  //CustomLog.e("epgfrag","update epgdata 7");
            }
        });

    }

    private void updateProgress(boolean show) {
      //  progressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        if(progressLayout != null) {
            progressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }

    }

    private void prepareEPGData(List<EPG.EPGData> epgData, boolean reset, boolean recalculateandredraw) {
//        nodata.setVisibility(View.GONE);
        noEpgLayout.setVisibility(View.GONE);
        HashMap<EPG.EPGChannel, List<EPG.EPGProgram>> result = Maps.newLinkedHashMap();
        int size = epgData.size();
        for (int i = 0; i < size; i++) {
            /*    if(epgData.get(i).getPrograms().size()>0)*/
            result.put(epgData.get(i).getChannel(), epgData.get(i).getPrograms());
        }
        if (size > 0) {
            if (epgData.get(0).getPrograms() != null && epgData.get(0).getPrograms().size() > 0) {
                templateCode = epgData.get(0).getPrograms().get(0).getTemplate();
            }
            if (templateCode == null) {
                if (epgData.size() > 1 && epgData.get(1).getPrograms() != null && epgData.get(1).getPrograms().size() > 0) {
                    templateCode = epgData.get(1).getPrograms().get(1).getTemplate();
                }
            }
        }


        // new EPGDataImpl(result);

        if (result.size() > 0) {
            epgDataImplObject = new EPGDataImpl(result);
            epg.setEPGData(epgDataImplObject);
            if(recalculateandredraw) {
                Log.e("EPGFragment", "calling recalculate and redraw from prepare epg");
                epg.recalculateAndRedraw(false);
            }else
                epg.recalculateAndRedrawNewDay(false,selectedTabIndex-todayTabSelectedIndex);

            noEpgLayout.setVisibility(View.GONE);
        } else {
//            nodata.setVisibility(View.VISIBLE);
            // hideBaseErrorLayout();
            noEpgLayout.setVisibility(View.VISIBLE);
        }
        if (reset) {
            epg.scrollToTopPosition();
        }
    }

    private void prepareEPGDataOfNewDay(List<EPG.EPGData> epgData, long startTime) {
//        nodata.setVisibility(View.GONE);
        noEpgLayout.setVisibility(View.GONE);
        HashMap<EPG.EPGChannel, List<EPG.EPGProgram>> result = Maps.newLinkedHashMap();
        int size = epgData.size();
        for (int i = 0; i < size; i++) {
            /*    if(epgData.get(i).getPrograms().size()>0)*/
            result.put(epgData.get(i).getChannel(), epgData.get(i).getPrograms());
        }
        if (size > 0) {
            if (epgData.get(0).getPrograms() != null && epgData.get(0).getPrograms().size() > 0) {
                templateCode = epgData.get(0).getPrograms().get(0).getTemplate();
            }
            if (templateCode == null) {
                if (epgData.size() > 1 && epgData.get(1).getPrograms() != null && epgData.get(1).getPrograms().size() > 0) {
                    templateCode = epgData.get(1).getPrograms().get(1).getTemplate();
                }
            }
        }


        // new EPGDataImpl(result);

        if (result.size() > 0) {
            epgDataImplObject = new EPGDataImpl(result);
            epg.setEPGData(epgDataImplObject);
            epg.recalculateAndRedraw(false);
            //epg.recalculateAndRedrawNewDay(false,selectedTabIndex-todayTabSelectedIndex);
        } else {
//            nodata.setVisibility(View.VISIBLE);
            //hideBaseErrorLayout();
            noEpgLayout.setVisibility(View.VISIBLE);
        }
        epg.resetScrollOffsetValue(0);


    }

    private void updateEPGData(final List<EPG.EPGData> epgData) {
        Log.e("epgfrag", "update epgdata : ");
        if (epgDataImplObject != null && epgDataImplObject.getEpgData() != null && epgData != null) {
            Log.e("epgfrag", "update epgDataImplObject : " + epgDataImplObject.getEpgData().size());
            Log.e("epgfrag", "update epgdata : " + epgData.size());
//            nodata.setVisibility(View.GONE);
            noEpgLayout.setVisibility(View.GONE);
            epg.setVisibility(View.VISIBLE);
            boolean isUpdateRequired = false;
            // Checking if there is any change in the channels count.(applying filters will result different channel count for different tabs
            for (int i = 0; i < epgData.size(); i++) {
                boolean exist = false;
                for (int j = 0; j < epgDataImplObject.getEpgData().size(); j++) {
                    Object key = epgDataImplObject.getEpgData().keySet().toArray()[j];
                    if (key instanceof EPG.EPGChannel) {
                        if (((EPG.EPGChannel) key).getDisplay().getTitle().equalsIgnoreCase(epgData.get(i).getChannel().getDisplay().getTitle())) {
                            exist = true;
                            break;
                        }
                    }
                }
                Log.e("epgfrag " + epgData.get(i).getChannel().getDisplay().getTitle(), "update epgdata programs : " + epgData.get(i).getPrograms().size());
                Log.e("epgfrag", "epgDataImplObject: " + epgDataImplObject);
                exist = false;
                if (!exist) {
                    epgDataImplObject.getEpgData().put(epgData.get(i).getChannel(), epgData.get(i).getPrograms());
                    isUpdateRequired = true;
                }
                Log.e("isUpdateRequired ", "++++++++++++" + epgDataImplObject.getEpgData().size());
            }
            Log.e("isUpdateRequired ", "++++++++++++" + isUpdateRequired);
            if (isUpdateRequired) {
                Map<EPG.EPGChannel, List<EPG.EPGProgram>> result = Maps.newLinkedHashMap();
                result.putAll(epgDataImplObject.getEpgData());
                epgDataImplObject.updateData(result);

            }
            /* //todo Priint valuyes remove*/
            Log.e("epgDataImplObject", "++++++++++++" + epgDataImplObject.getEpgData().size());

            Log.e("epgfrag", "update epgdata 4 ");
            Log.e("epgfrag", "update epgdata 4 " + epgDataImplObject.getEpgData().size());
//                    epg.setEPGData(epgDataImplObject.getEpgData());
            //CustomLog.e("epgfrag","update epgdata 5");
            //epg.recalculateAndRedraw(false);
            EPGFragment.this.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("epgfrag", "calling redraw ");
                    if (epg != null) {
                        epg.redraw();
                        epg.viewInvalidate(false);
                    }
                    //  //CustomLog.e("epgfrag","update epgdata 7");
                }
            });


        }

    }

    private void refreshTimeBar() {
        if (epg != null && EPGFragment.this.isVisible) {
            epg.redraw();
            epg.viewInvalidate(false);
        }
    }

    private void cancelTimeBarTimer() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }


    private void selectText(TabLayout.Tab tab, int colorCode) {
       /* View view = tab.getCustomView();
        TextView selectedText = (TextView) view.findViewById(R.id.customFontTabLayout);
        if (mActivity != null && selectedText != null)
            selectedText.setTextColor(ContextCompat.getColor(Objects.requireNonNull(mActivity), colorCode));*/
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {


        selectedTabIndex = tab.getPosition();

        if (selectedTabIndex == 0)
            ApplicationController.DATE_POSITION = -1;
        else if (selectedTabIndex == -1 || selectedTabIndex == 1)
            ApplicationController.DATE_POSITION = 0;
        else
            ApplicationController.DATE_POSITION = selectedTabIndex - 1;

        if ((selectedTabIndex == 0 && todayTabSelectedIndex == 0) || initialRequest) // stopping to fetch data for default tab position 0
            return;
        mStartIndex = 1;
        boolean fetchepg = false;
        loadMoreInprogress = false;
        // Fetching the selected tab and previous tab also as the data for some timezones is not 12 to 12
   /*     if (tabDataTracker != null && tab.getPosition() > 0 && tab.getPosition() < tabs.size() && (!tabDataTracker.containsKey("" + (tab.getPosition() - 1)) || !tabDataTracker.get("" + (tab.getPosition() - 1)))) {
            fetchepg = true;
            FetchEPG("" + tabs.get(tab.getPosition() - 1).getStartTime(), "" + tabs.get(tab.getPosition() - 1).getEndTime(), tab.getPosition() - 1);
        }*/

        if (tabDataTracker != null && (!tabDataTracker.containsKey("" + tab.getPosition()) || !tabDataTracker.get("" + tab.getPosition()))) {
            fetchepg = true;
            FetchEPG("" + tabs.get(tab.getPosition()).getStartTime(), "" + tabs.get(tab.getPosition()).getEndTime(), tab.getPosition());
        }


        tabSelected = true;

        if (this.epg != null /*&& tab.getPosition() != selectedTabIndex*/)
            this.epg.scrollTimeBarToPosition(tab.getPosition() - todayTabSelectedIndex, true, true);

        //  if(!fetchepg) // using this flag "tabSelected"  to stop the Fetch requests from updatescroll when tab selected
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        tabSelected = false;
                    }
                }, 500);

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        //selectText(tab, R.color.white_30);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if (this.epg != null && tab.getPosition() != selectedTabIndex)
            this.epg.scrollTimeBarToPosition(tab.getPosition() - todayTabSelectedIndex, false, true);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

        int x = 0;

    }

    @Override
    public void onScrollReachedBottom() {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    String applyFilters() {
        return "";
    }

    private void resetValues() {
        if (tabDataTracker != null) {
            tabDataTracker.clear();
            if (mActivity != null && mActivity instanceof MainActivity) {
                //  ((MainActivity)mActivity).setEpgDataTracker(tabDataTracker);
            }
        }

        if (mActivity != null && mActivity instanceof MainActivity) {
            //  ((MainActivity)mActivity).setEpgData(null);
        }

//    pagePosition=1;

        if (epg != null)
            epg.resetScrollOffsetValue(2);
        if (epgScrollHashMap != null) {
            epgScrollHashMap.clear();
            epgScrollHashMap = null;
        }

        if (epgDataImplObject != null) {
            epgDataImplObject.clearValues();
            epg.redraw();
            epg.viewInvalidate(false);
        }
    }

    class TimeBarIndicator extends TimerTask {
        @Override
        public void run() {
            if (EPGFragment.this.mActivity != null) {
                EPGFragment.this.mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //  //CustomLog.e("EPG","timeline head refresh timebar");

                        if (!DateUtils.getMinites(DateHelper.getInstance().getCurrentLocalTime()).equalsIgnoreCase("00")) {
                            if (!DateUtils.getMinites(DateHelper.getInstance().getCurrentLocalTime()).equalsIgnoreCase("30"))
                                DateHelper.getInstance().setElapseTime(60 * 1000);
                            refreshTimeBar();
                        }
                    }
                });
            }


        }
    }

    class EPGScroll {
        public int index;

        @Override
        public String toString() {
            return "EPGScroll{" +
                    "index=" + index +
                    ", loadMore=" + loadMore +
                    ", pageValue=" + pageValue +
                    '}';
        }

        public boolean loadMore;
        public int pageValue;
    }

    public void clickEvent(final int channelPosition, final int programPosition, final String channel_id, final EPG.EPGProgram epgEvent) {
        /*if (!NetworkUtils.isConnected(mActivity)) {
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.please_check_your_connection), Toast.LENGTH_SHORT).show();
            return;
        }*/

        // trackTvGuideClickEvent(channelPosition, epgEvent);

        boolean isLive = false;
        //to check whether the event is live or not
               /* if(epgEvent.getTarget()!=null && epgEvent.getTarget().getPageAttributes()!=null && epgEvent.getTarget().getPageAttributes().getClevertapContentType() !=null
                        && epgEvent.getTarget().getPageAttributes().getClevertapContentType().equalsIgnoreCase("live")){
                        if(isEventLiveBasedOnTimeBar(epgEvent)) {
                            isLive = true;
                        }
                }*/
        if (null != epgEvent.getTarget().getPageAttributes()) {
            if (null != epgEvent.getTarget().getPageAttributes().getIsLive()
                    && epgEvent.getTarget().getPageAttributes().getIsLive().equalsIgnoreCase("true"))
                isLive = true;
        }

        //info not available events
        if (epgEvent.getDisplay().getTitle().startsWith("No Info Available")) {
            Toast.makeText(mActivity, epgEvent.getDisplay().getTitle(), Toast.LENGTH_SHORT).show();
            // eventAlreadyClicked = false;
        }
        //live events
        else if (isLive && !isEventFinished(epgEvent)) {
            final String template_code = "tvguide-popup-sample";
            String path = "";
            if (epgEvent.getTarget() != null && epgEvent.getTarget().getPath() != null)
                path = epgEvent.getTarget().getPath();

            final EPG.PosterDisplay display = epgEvent.getDisplay();
            List<EPG.PosterDisplay.Marker> markersList;
            boolean isPlayback = true;
            if (display != null && (markersList = display.getMarkers()) != null
                    && display.getMarkers().size() > 0) {
                final int size = markersList.size();

                for (EPG.PosterDisplay.Marker marker : display.getMarkers()) {
                    String markerType = marker.getMarkerType();
                    if ((markerType.equalsIgnoreCase("special") && marker.getValue()
                            .equalsIgnoreCase("SignIn"))
                            || (markerType.equalsIgnoreCase("special")
                            && marker.getValue().equalsIgnoreCase("Subscribe"))) {
                        //showOverlay(channelPosition, channel_id, epgEvent);
                        isPlayback = false;
                        break;
                    }
                }
            }
            final String targetPath = epgEvent.getTarget().getPath();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (epg != null) {
            epg.viewInvalidate(false);
            epg.redraw();
        }
    }



    public void highLightBox(EPG.EPGProgram epgEvent, int channelPosition, boolean isChannel) {
        if (epg != null) {
            epg.viewInvalidate(false);
            epg.redraw();

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (epg != null)
                    epg.setDrawingStatus(true, epgEvent, channelPosition, isChannel);
            }
        }, 50);

    }
    public void fetchEPGChannels(EPGChannelImplementation epgChannelImplementation) {
        final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        boolean isDVROnly = (PrefUtils.getInstance().getPrefEnablePastEpg() && ApplicationController.DATE_POSITION - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0) ? true : false;
        String time = Util.convertDate(date.getTime());
        com.myplex.myplex.model.EPG.getInstance(Util.getCurrentDate(ApplicationController.DATE_POSITION)).findPrograms(APIConstants.PAGE_INDEX_COUNT, Util.getServerDateFormat(time, date), time, date, mStartIndex, true, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly, "", new com.myplex.myplex.model.EPG.CacheManagerCallback() {
            @Override
            public void OnlineResults(List<CardData> dataList, int pageIndex) {
                epgChannelImplementation.onSuccess(dataList);
            }

            @Override
            public void OnlineError(Throwable error, int errorCode) {
                epgChannelImplementation.onFailure();
            }
        });
    }
    interface EPGChannelImplementation {
        public void onSuccess(List<CardData> cardDataList);

        public void onFailure();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            if(DeviceUtils.isTablet(mContext)){
                showHideArrows();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}