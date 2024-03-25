package com.myplex.myplex.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.adapter.AdapterChannelEpg;
import com.myplex.myplex.ui.adapter.DatesAdapter;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.EpgFragment;
import com.myplex.myplex.ui.views.PopUpWindow;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Phani on 12/12/2015.
 */
public class ProgramGuideChannelActivity extends BaseActivity implements CacheManager.CacheManagerCallback {
    public static final String PARAM_CHANNEL_DATA = "selectedChannelData";
    public static final String DATE_POS = "date_pos";
    public static final String PARAM_DATE = "selectedDate";
    public static final String PARAM_FROM = "isFromNotification";
    private static final String TAG = ProgramGuideChannelActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private View mInflateView;
    private ListView mListView;
    private TextView mToolbarTitle;
    private TextView mTodayEPGTitle;
    private TextView mErrorTextView;
    private ImageView mTodayEPGButton;
    private ImageView mImageViewClose;
    private ImageView channelImageView;
    private AdapterChannelEpg mAdapterChannelEpg;
    private RelativeLayout mRootLayout;
    private RelativeLayout mTodayEPGLayout;
    private CardData mChannelData;
    private Context mContext;
    private int datePos;
    private final CacheManager mCacheManager = new CacheManager();
    private RelativeLayout helpScreenLayout;

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
            //showOverFlowSettings(v);
        }
    };

    private void showOverFlowSettings(View v) {
        Context wrapper = new ContextThemeWrapper(mContext, R.style.PopupMenu);
       /* PopupMenu popupMenu = new PopupMenu(wrapper, v) {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {

                switch (item.getItemId()) {
                    default:
                        return super.onMenuItemSelected(menu, item);
                }
            }
        };
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.main_settings, popupMenu.getMenu());

        Menu menu = popupMenu.getMenu();
            menu.removeItem(R.id.action_settings);
            menu.removeItem(R.id.action_filter);
            menu.removeItem(R.id.action_search);
//        popupMenu.inflate(R.menu.main_settings);
        popupMenu.show();*/
    }

    private AlertDialogUtil.NeutralDialogListener mNeutralDialogListener = new AlertDialogUtil.NeutralDialogListener() {
        @Override
        public void onDialogClick(String buttonText) {
            if(null != buttonText &&
                    buttonText.equalsIgnoreCase(mContext.getString(R.string.play_button_retry))){
                fetchProgramData(getIntent());
            }
        }
    };

    private AdapterView.OnItemClickListener mProgramClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // launch card details
            CardData programData = (CardData) mAdapterChannelEpg.getItem(position);
            //Log.d(TAG,"onItemClick");

            if(null == programData){
                return;
            }
            if(null == programData.globalServiceId){
                return;
            }
            //Log.d(TAG,"onItemClick");
            CacheManager.setSelectedCardData(programData);
            Intent contentDetailsActivity = new Intent(mContext, CardDetailsActivity.class);
            contentDetailsActivity.putExtra(CardDetails
                    .PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
            contentDetailsActivity.putExtra(CardDetails
                    .PARAM_CARD_ID, programData.globalServiceId);
            contentDetailsActivity.putExtra(CardDetails
                    .PARAM_EPG_DATE_POSITION, datePos);
            if(null != programData.startDate
                    && null != programData.endDate){
                Date startDate = Util.getDate(programData.startDate);
                Date endDate = Util.getDate(programData.endDate);
                Date currentDate = new Date();
                if ((currentDate.after(startDate)
                        && currentDate.before(endDate))
                        || currentDate.after(endDate)) {
                    contentDetailsActivity.putExtra(CardDetails
                            .PARAM_AUTO_PLAY, true);
                }
            }

            mContext.startActivity(contentDetailsActivity);
        }
    };
    private View.OnClickListener mTodayEPGListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mTodayEPGPOPUPWindow != null
                && mTodayEPGPOPUPWindow.isPopupVisible()){
                mTodayEPGPOPUPWindow.dismissPopupWindow();
            }else {
                showPopupMenu(v);
            }
        }
    };

    PopUpWindow mTodayEPGPOPUPWindow;
    private ListView mPopUpListView;
    private DatesAdapter mPopupListAdapter;

    private void showPopupMenu(View view) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_window, null);
        mTodayEPGPOPUPWindow = new PopUpWindow(layout);
        mTodayEPGPOPUPWindow.attachPopupToView(view, new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
        mPopUpListView = (ListView) layout.findViewById(R.id.popup_listView);
        mPopupListAdapter = new DatesAdapter(mContext, Util.showNextDates());
        mPopUpListView.setAdapter(mPopupListAdapter);
        mPopUpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTodayEPGPOPUPWindow.dismissPopupWindow();
                datePos = position;
                Date selectedDate = Util.getCurrentDate(position);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                if (null != selectedDate) {
                    String selectedDateInString = format.format(selectedDate);
                    String dateStamp = Util.getYYYYMMDD(selectedDateInString);
                    if(getIntent().hasExtra(PARAM_FROM)){
                        fetchProgramData(dateStamp);
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra(ProgramGuideChannelActivity.PARAM_CHANNEL_DATA, mChannelData);
                    intent.putExtra(ProgramGuideChannelActivity.PARAM_DATE,dateStamp);
                    fetchProgramData(intent);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_guide_channel);
//        Analytics.createScreenGA(Analytics.SCREEN_CHANNEL_EPG);
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_EPG_BROWSED);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;
        Util.prepareDisplayinfo(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0,0);
        mListView = (ListView) findViewById(R.id.tv_guide_channel_listView);
        mTodayEPGLayout = (RelativeLayout) findViewById(R.id.tv_guide_layout_today_epg);
        mTodayEPGTitle = (TextView) findViewById(R.id.tv_guide_today_epg_title);
        mErrorTextView = (TextView) findViewById(R.id.error_message);
        mTodayEPGButton = (ImageView) findViewById(R.id.drop_down_button);
        helpScreenLayout = (RelativeLayout)findViewById(R.id.prog_help_screen_layout);
        mTodayEPGButton.setOnClickListener(mTodayEPGListener);
        mTodayEPGTitle.setOnClickListener(mTodayEPGListener);

        mInflateView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mImageViewClose = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        channelImageView = (ImageView)mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        //mInflateView.findViewById(R.id.toolbar_filter_button).setVisibility(View.GONE);
        if(getIntent().hasExtra(PARAM_CHANNEL_DATA))
        mChannelData = (CardData) getIntent().getSerializableExtra(PARAM_CHANNEL_DATA);
        int todayPosition = PrefUtils.getInstance().getPrefEnablePastEpg() ? PrefUtils.getInstance().getPrefNoOfPastEpgDays() : ApplicationController.DATE_POSITION;
        datePos = getIntent().getIntExtra(DATE_POS, todayPosition);
        initUI();
        if (getIntent().hasExtra(PARAM_FROM)) {
            Date selectedDate = Util.getCurrentDate(datePos);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String selectedDateInString = format.format(selectedDate);
            String dateStamp = Util.getYYYYMMDD(selectedDateInString);
            fetchProgramData(dateStamp);
        } else {
            fetchProgramData(getIntent());
        }
    }

    private void fetchProgramData(String dateStamp) {
       // String dateStamp = null;
        String cId = "";
        if (dateStamp == null) {
            Date currentDate = new Date();
            dateStamp = Util.getYYYYMMDD(currentDate);
        }
        ArrayList<String> nxtDateList = Util.showNextDates();
        String date = nxtDateList.get(datePos);
        if(getIntent().hasExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID))
           cId = getIntent().getStringExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);
        mTodayEPGTitle.setAllCaps(false);
        setDateToCalender(date);
        mCacheManager.getEPGChannelData(cId, dateStamp, false,ProgramGuideChannelActivity.this);
    }

    private void fetchProgramData(Intent intent) {
        if (null != intent) {
            EpgFragment.isChannelOpen = false;
            mChannelData = (CardData) intent.getSerializableExtra(PARAM_CHANNEL_DATA);

            String dateStamp = null;
            String cId = "";
            if (mChannelData != null
                    && mChannelData.generalInfo != null
                    && mChannelData.generalInfo.type != null) {
                if (mChannelData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                        && mChannelData.globalServiceId != null) {
                    cId = mChannelData.globalServiceId;
                } else if (mChannelData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                        && mChannelData._id != null) {
                    cId = mChannelData._id;
                }
            }
            if (intent.hasExtra(PARAM_DATE)) {
                dateStamp = intent.getStringExtra(PARAM_DATE);
            } else if (mChannelData != null
                    && mChannelData.startDate != null) {
                dateStamp = Util.getYYYYMMDD(mChannelData.startDate);
            } else {
                Date currentDate = new Date();
                dateStamp = Util.getYYYYMMDD(currentDate);
            }

            ArrayList<String> nxtDateList = Util.showNextDates();
            String date = nxtDateList.get(datePos);
//                    String[] splited = date.split("\\s+");
//                    mTodayEPGTitle.setText(splited[0]+" "+splited[1]);
            mTodayEPGTitle.setAllCaps(false);
            setDateToCalender(date);
           // mTodayEPGTitle.setText("" + date);
            //mTodayEPGTitle.setText("" + date);
            mCacheManager.getEPGChannelData(cId, dateStamp, false,ProgramGuideChannelActivity.this);
        }

    }

    private void setDateToCalender(String date) {
        Spannable cs = new SpannableString(date);
        if(date.contains("Today") ){
            cs.setSpan(new SuperscriptSpan(), 10, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 10, 12, 0);
        }else {
            cs.setSpan(new SuperscriptSpan(), 7, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 7, 9, 0);
        }
        mTodayEPGTitle.setText(cs);
    }

    private void initUI() {

        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
      //  mToolbarTitle.setText("TV Guide");
//        mImageViewClose.setImageResource(R.drawable.egp_close_icon);
        mAdapterChannelEpg = new AdapterChannelEpg(this, getDummyChannelList(),datePos);
        mListView.setAdapter(mAdapterChannelEpg);
        mListView.setOnItemClickListener(mProgramClickListener);
//        mImageViewClose.setOnClickListener(this);
        mImageViewClose.setOnClickListener(mCloseAction);

        updateChannelImage(mChannelData);
    }

    private void updateChannelImage(CardData cardData) {

        if(PrefUtils.getInstance().getPrefEnableDittoChannelLogoOnEpg()
                && cardData != null
                && cardData.contentProvider != null
                && cardData.contentProvider.equalsIgnoreCase(APIConstants.TYPE_DITTO)){
            if (!TextUtils.isEmpty(cardData.globalServiceName)) {
                mToolbarTitle.setText(cardData.globalServiceName);
            }

            //Picasso.with(this).load(APIConstants.getDittoChannelLogoUrl()).error(R.drawable.epg_thumbnail_default).placeholder(R.drawable.epg_thumbnail_default).into(channelImageView);
            PicassoUtil.with(this).load(APIConstants.getDittoChannelLogoUrl(),channelImageView,R.drawable.black);
            return;
        }
        if(cardData == null || cardData.generalInfo == null || !cardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM) ){
            //Picasso.with(this).load(R.drawable.epg_thumbnail_default).error(R.drawable.epg_thumbnail_default).placeholder(R.drawable.epg_thumbnail_default).into(channelImageView);
            PicassoUtil.with(this).load(R.drawable.epg_thumbnail_default,channelImageView,R.drawable.black);
            return;
        }
        if(cardData.images == null || cardData.images.values == null || cardData.images.values.size() ==0){
          // Picasso.with(this).load(R.drawable.epg_thumbnail_default).error(R.drawable.epg_thumbnail_default).placeholder(R.drawable.epg_thumbnail_default).into(channelImageView);
            PicassoUtil.with(this).load(R.drawable.black,channelImageView,R.drawable.black);
            return;
        }

        boolean imageLoaded = false;
        for (CardDataImagesItem imageItem : cardData.images.values) {
            if (imageItem.type != null && imageItem.type.equalsIgnoreCase("thumbnail") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)
                    && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("250x375")) {
                if (imageItem.link != null && imageItem.link.trim().length()>0) {
                    imageLoaded = true;
                    //Picasso.with(this).load(imageItem.link).error(R.drawable.epg_thumbnail_default).placeholder(R.drawable.epg_thumbnail_default).into(channelImageView);
                    PicassoUtil.with(this).load(imageItem.link,channelImageView,R.drawable.black);
                    break;
                }else {
                   // Picasso.with(this).load(R.drawable.epg_thumbnail_default).error(R.drawable.epg_thumbnail_default).placeholder(R.drawable.epg_thumbnail_default).into(channelImageView);
                    PicassoUtil.with(this).load(R.drawable.epg_thumbnail_default,channelImageView,R.drawable.epg_thumbnail_default);
                }
            }

        }

        if(!imageLoaded) {
            //Picasso.with(this).load(R.drawable.epg_thumbnail_default).error(R.drawable.epg_thumbnail_default).placeholder(R.drawable.epg_thumbnail_default).into(channelImageView);
            PicassoUtil.with(this).load(R.drawable.black,channelImageView,R.drawable.black);
        }

    }

    private List<CardData> getDummyChannelList() {

        List dummyList = new ArrayList<>();
        for (int i = 0 ; i < 10 ; i++){
            dummyList.add(new CardData());
        }
        return dummyList;
    }

    private void fillData(List<CardData> channelData) {
        skipCompletedProgramsAndApply(channelData);
    }

    private void skipCompletedProgramsAndApply(List<CardData> channelData) {

        if(datePos - PrefUtils.getInstance().getPrefNoOfPastEpgDays() >= 0
                || !PrefUtils.getInstance().getPrefEnablePastEpg()){
            for (Iterator<CardData> it = channelData.iterator(); it.hasNext(); ) {
                CardData cardData = it.next();
                if(null != cardData.endDate
                        && null != cardData.startDate){
                    Date endDate = Util.getDate(cardData.endDate);
                    Date currentDate = new Date();
                    Date startDate = Util.getDate(cardData.startDate);
                    if(currentDate.after(endDate)){
                        if(!(currentDate.after(startDate)
                                && currentDate.before(endDate))){
                            it.remove();
                        }
                    }
                }
            }
        }

        if(channelData == null ||
                channelData.isEmpty()){
            mErrorTextView.setVisibility(View.VISIBLE);
            mErrorTextView.setText(mContext.getString(R.string.data_fetch_error));
            mListView.setVisibility(View.GONE);
            return;
        }
        mListView.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.GONE);
        mAdapterChannelEpg = new AdapterChannelEpg(this, channelData,datePos);
        mListView.setAdapter(mAdapterChannelEpg);
        mAdapterChannelEpg.notifyDataSetChanged();
        mListView.setOnItemClickListener(mProgramClickListener);
        updateChannelImage(channelData.get(0));
        if (isHelpScreenShown()) {
            helpScreenLayout.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    public void setOrientation(int REQUEST_ORIENTATION) {
        setRequestedOrientation(REQUEST_ORIENTATION);
    }

    @Override
    public int getOrientation() {
        return getRequestedOrientation();
    }

    @Override
    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
    }

    @Override
    public void showActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.show();
        }
    }

    @Override
    public void OnCacheResults(List<CardData> dataList) {
        if(null == dataList){
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.programguide_data_fetch_error));
            return;
        }
        fillData(dataList);
    }

    @Override
    public void OnOnlineResults(List<CardData> dataList) {
        if(null == dataList){
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.programguide_data_fetch_error));
            return;
        }
        fillData(dataList);
    }

    @Override
    public void OnOnlineError(Throwable error, int errorCode) {
        if(errorCode == APIRequest.ERR_NO_NETWORK){
            AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
                    "", mContext.getString(R.string.play_button_retry), mNeutralDialogListener);
            return;
        }
        AlertDialogUtil.showToastNotification(mContext.getString(R.string.programguide_data_fetch_error));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mChannelData != null
                && mChannelData.globalServiceName != null){
            Analytics.gaBrowseChannelEpg(mChannelData.globalServiceName);
            String _id = mChannelData._id;
            if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mChannelData.generalInfo.type)){
                    _id = mChannelData.globalServiceId; //2
            }
            Analytics.mixpanelBrowseChannelEpg(_id,mChannelData.globalServiceName);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapterChannelEpg.notifyDataSetChanged();
    }
    private boolean isHelpScreenShown() {

        boolean ranBefore = PrefUtils.getInstance().getProgramHelpScreenPref();
        if (!ranBefore) {
            PrefUtils.getInstance().setProgramHelpScreenPref(true);
              helpScreenLayout.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      helpScreenLayout.setVisibility(View.VISIBLE);

                  }
              },2000);
            helpScreenLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    helpScreenLayout.setVisibility(View.INVISIBLE);
                    return true;
                }

            });

        }
        return ranBefore;

    }
}
