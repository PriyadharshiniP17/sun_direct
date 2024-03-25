package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.viewpager.widget.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.events.ChangeMenuVisibility;
import com.myplex.myplex.events.ChannelsUpdatedEvent;
import com.myplex.myplex.events.EPGHelpScreenEvent;
import com.myplex.myplex.events.OpenChannelEPGEvent;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.model.ChannelItem;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.ChannelEpgListAdapter;
import com.myplex.myplex.ui.adapter.DatesAdapter;
import com.myplex.myplex.ui.adapter.EpgCounterPagerAdapter;
import com.myplex.myplex.ui.views.ListScrollManager;
import com.myplex.myplex.ui.views.PopUpWindow;
import com.myplex.myplex.ui.views.SyncScrollListView;
import com.myplex.myplex.utils.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pageslidingstrip.PagerSlidingTabStrip;
import viewpagerindicator.CustomViewPager;


/**
 * Created by phani on 12/3/2015.
 */
public class TvGuideFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = TvGuideFragment.class.getSimpleName();
    private static final String PARAM_SELECTED_EPG_CALENDER_DATE_POSITION = "epg_calender_date_position";
    private CustomViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private LinearLayout mDateLayout;
    private EpgCounterPagerAdapter mAdapter;
    private TextView dateTxt;
    private ListView mPopUpListView;
    private DatesAdapter mPopupListAdapter;
    private static SyncScrollListView mEpgChannelListView;
    public static ChannelEpgListAdapter mChannelEpgListAdapter;
    public static Map<Integer, ChannelItem> channelsList;
    private Handler mNextSlideHandler = new Handler();
    private ArrayList<String> list;
    private boolean isHelpScreenEventIsTriggered = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PARAM_SELECTED_EPG_CALENDER_DATE_POSITION, ApplicationController.DATE_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tvguide, container, false);
//        Analytics.createScreenGA(Analytics.SCREEN_TVGUIDE);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_TVGUIDE);
        mContext = getActivity();
        mViewPager = (CustomViewPager) rootView.findViewById(R.id.pager);
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) rootView.findViewById(R.id.indicator);
        Typeface font = Typeface.createFromAsset(mContext.getAssets(),
                "font/amazon_ember_cd_regular.ttf");
        mPagerSlidingTabStrip.setTypeface(font, 0);
        mDateLayout = (LinearLayout) rootView.findViewById(R.id.date_layout);
        dateTxt = (TextView) rootView.findViewById(R.id.date_txt);
        mEpgChannelListView = (SyncScrollListView) rootView.findViewById(R.id.epg_channel_list);
        ApplicationController.DATE_POSITION = PrefUtils.getInstance().getPrefEnablePastEpg() ? PrefUtils.getInstance().getPrefNoOfPastEpgDays() : 0;
        if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_SELECTED_EPG_CALENDER_DATE_POSITION)) {
            ApplicationController.DATE_POSITION = savedInstanceState.getInt(PARAM_SELECTED_EPG_CALENDER_DATE_POSITION);
        }
        initUi();
        return rootView;
    }


    private Runnable mNextSlideTimeTask = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(0);
        }
    };
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String currentEpgTime = Util.getCurrentEpgTablePosition();
            if (TextUtils.isEmpty(dateTxt.getText()) || TextUtils.isEmpty(currentEpgTime)) {
                return;
            }
            if (dateTxt.getText().equals("Schedule") && !currentEpgTime.equalsIgnoreCase(list.get(0))) {
                initUi();
            }
        }
    };


    private void initUi() {
        if (!isAdded()) {
            //Log.d(TAG, "initUi: isAdded- " + isAdded() + " returning from initUI");
            return;
        }

        mNextSlideHandler.postDelayed(mNextSlideTimeTask, 1000 * 60);
        //Log.d(TAG, "initUi:  mEpgChannelListView.setVisibility(View.GONE)- ");
        ArrayList<String> nxtDateList = Util.showNextDates();
        String curDate = nxtDateList.get(ApplicationController.DATE_POSITION);
        setDateToCalendar(curDate);
//        boolean isPastEpg = todayPosition -
        list = generateEpgTable(ApplicationController.ENABLE_BROWSE_PAST_EPG);
        mDateLayout.setOnClickListener(this);

        if (list == null || list.size() == 0)
            return;
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        //int dialerPos = getCurrentEpgTablePosition(list);
        mAdapter = new EpgCounterPagerAdapter(getChildFragmentManager(), list,
                parser.format(date), mContext, ApplicationController.DATE_POSITION);
        mViewPager.setAdapter(mAdapter);
        mPagerSlidingTabStrip.setViewPager(mViewPager);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources()
                .getDisplayMetrics());
        mViewPager.setPageMargin(pageMargin);
        mPagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ApplicationController.CURRENT_SELECTED_PAGE_POS = position;
                //Log.d(TAG, "mViewPager: onPageSelected() position:" + position);
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_EPG_BROWSED);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        if (channelsList == null) {
            channelsList = generateDummyChannels();
        }
        mChannelEpgListAdapter = new ChannelEpgListAdapter(mContext, channelsList);
        mEpgChannelListView.setAdapter(mChannelEpgListAdapter);

        mEpgChannelListView.setOnItemClickListener(this);
       /* mEpgChannelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScopedBus.getInstance().post(new OpenChannelEPGEvent(position));
            }
        });*/
    }

    private void setDateToCalendar(String curDate) {
        if (curDate.contains("Today")) {
            dateTxt.setText("Schedule");
        } else {
            String[] splited = curDate.split("\\s+");
            String day = splited[0] + " " + splited[1];
            Spannable cs = new SpannableString(day);
            cs.setSpan(new SuperscriptSpan(), 7, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 7, 9, 0);
            dateTxt.setText(cs);
        }
    }

    public void onEventMainThread(EPGHelpScreenEvent event) {
        isHelpScreenEventIsTriggered = true;
        if (getUserVisibleHint() && isHelpScreenEventIsTriggered) {
            isHelpScreenEventIsTriggered = false;
            ((MainActivity) mContext).showHelpScreenShown();
        }
    }

    @Override
    public void onClick(View v) {

        if (datePOPUPWindow != null
                && datePOPUPWindow.isPopupVisible()) {
            datePOPUPWindow.dismissPopupWindow();
        } else {
            showEPGFilterPopupMenu(v);
        }
    }

    PopUpWindow datePOPUPWindow;

    private void showEPGFilterPopupMenu(View view) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_window, null);
        datePOPUPWindow = new PopUpWindow(layout);
        datePOPUPWindow.attachPopupToView(view, new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        mPopUpListView = (ListView) layout.findViewById(R.id.popup_listView);
        mPopupListAdapter = new DatesAdapter(mContext, Util.showNextDates());
        mPopupListAdapter.setSelectedPosition(ApplicationController.DATE_POSITION);
        mPopUpListView.setAdapter(mPopupListAdapter);
        mPopUpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                datePOPUPWindow.dismissPopupWindow();
                Util.getCurrentDate(position);
                ApplicationController.isDateChanged = true;
                ApplicationController.DATE_POSITION = position;
                //  mTitlePageIndicator.setSelected(true);
                EPG.globalPageIndex = 1;
                ApplicationController.pageVisiblePos = 0;
                ApplicationController.pageItemPos = 0;
                mAdapter.updateCurrentDate(position);
                mViewPager.setVisibility(View.VISIBLE);
                mPopupListAdapter.setSelectedPosition(ApplicationController.DATE_POSITION);
                mPopupListAdapter.notifyDataSetChanged();

                ArrayList<String> nxtDateList = Util.showNextDates();
                String date = nxtDateList.get(position);
                if (date.contains("Today")) {
                    dateTxt.setText("Today");

                } else {
                    String[] splited = date.split("\\s+");
                    dateTxt.setText(splited[0] + " " + splited[1]);
                }
                initUi();
            }
        });
    }

    public ArrayList<String> generateEpgTable(boolean backEpg) {
        ArrayList<String> epgCounterList = new ArrayList<>();

        SimpleDateFormat adParser = new SimpleDateFormat("HH:mm");

        SimpleDateFormat minParser = new SimpleDateFormat("mm");
        SimpleDateFormat hrsParser = new SimpleDateFormat("HH");

        Calendar cal = Calendar.getInstance();
        String min = minParser.format(cal.getTime());
        String hrs = hrsParser.format(cal.getTime());

        StringBuilder epgTimeTable = new StringBuilder();
        int diff = Integer.parseInt(min) % ApplicationController.TIME_SHIFT;
        int showTime = Integer.parseInt(min) - diff;
        String tm = String.valueOf(hrs) + ":" + String.valueOf(showTime);
        try {
            if (!backEpg && dateTxt.getText().equals("Schedule")) {
                adParser.parse(tm);
                Calendar call = Calendar.getInstance();
                call.setTime(adParser.getCalendar().getTime());
                String toTime = adParser.format(call.getTime());

                Calendar calendarAdd = Calendar.getInstance();
                epgCounterList.add(toTime);
                while (!toTime.equals("23:45")) {
                    calendarAdd.setTime(adParser.getCalendar().getTime());
                    calendarAdd.add(Calendar.MINUTE, ApplicationController.TIME_SHIFT);
                    toTime = adParser.format(calendarAdd.getTime());
                    epgTimeTable.append(epgTimeTable.length() == 0 ? "" : ", " + epgTimeTable);
                    epgCounterList.add(toTime);
                }
                LoggerD.debugLog("TVGuide: generateEpgTable: epgTimeTable- " + epgTimeTable.toString());
                return epgCounterList;
            }

            Calendar calendar = Calendar.getInstance();
            String parseTime = "00:00";
            adParser.parse(parseTime);
            String time = "";
            epgCounterList.add(parseTime);
            while (!time.equals("23:45")) {
                calendar.setTime(adParser.getCalendar().getTime());
                calendar.add(Calendar.MINUTE, ApplicationController.TIME_SHIFT);
                time = adParser.format(calendar.getTime());
                epgTimeTable.append(epgTimeTable.length() == 0 ? "" : ", " + epgTimeTable);
                epgCounterList.add(time);
            }
            LoggerD.debugLog("TVGuide: generateEpgTable: epgTimeTable- " + epgTimeTable.toString());

            return epgCounterList;


        } catch (ParseException e) {
            e.printStackTrace();
            return epgCounterList;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListScrollManager.getInstance().addScrollClient(mEpgChannelListView);
    }

    @Override
    public void onResume() {
        super.onResume();
        ListScrollManager.getInstance().addScrollClient(mEpgChannelListView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //Log.d(TAG, "onDetach()");
        ListScrollManager.getInstance().removeScrollClient(mEpgChannelListView);
    }


    private Map<Integer, ChannelItem> generateDummyChannels() {
        Map<Integer, ChannelItem> dummyChannelsList = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            //ChannelItem channelItem = new ChannelItem();
            dummyChannelsList.put(i, null);
        }
        return dummyChannelsList;
    }


    // Called in Android UI's main thread
    public void onEventMainThread(ChannelsUpdatedEvent event) {

        if (event.getData() == null || event.getData().size() == 0) {
           /* //Log.d(TAG, "onEventMainThread:  event.getData()- " + event.getData() + "making " +
                    "mEpgChannelListView gone");*/
            mEpgChannelListView.setVisibility(View.GONE);
            return;
        }
        channelsList = event.getData();
        mChannelEpgListAdapter.updateChannelImages(event.getData());
     /*   //Log.d(TAG, "onEventMainThread:  event.getData()- " + event.getData() + "making " +
                "mEpgChannelListView visible");*/
        mEpgChannelListView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScopedBus.getInstance().post(new OpenChannelEPGEvent(position));

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            AppsFlyerTracker.eventBrowseTab(APIConstants.TYPE_LIVE);
            Analytics.gaBrowse(APIConstants.TYPE_LIVE, 1L);
            // the onResume function is a good place to call the functions to display surveys or
            // in app notifications. It is safe to call both these methods right after each other,
            // since they do nothing if a notification or survey is already showing.
        }
        ScopedBus.getInstance().post(new ChangeMenuVisibility(true, MainActivity.SECTION_LIVE));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isHelpScreenEventIsTriggered) {
            isHelpScreenEventIsTriggered = false;
            ((MainActivity) mContext).showHelpScreenShown();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG, "onConfigurationChanged(): " + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Log.d(TAG, "dismiss pop up window");
            if (datePOPUPWindow != null) {
                datePOPUPWindow.dismissPopupWindow();
            }
        }

        if (mChannelEpgListAdapter != null && mAdapter != null) {
            mChannelEpgListAdapter.notifyDataSetChanged();
            mAdapter.notifyDataSetChanged();
        }
        super.onConfigurationChanged(newConfig);
    }


}
