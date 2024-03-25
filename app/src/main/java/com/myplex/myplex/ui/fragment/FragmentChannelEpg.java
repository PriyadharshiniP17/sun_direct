package com.myplex.myplex.ui.fragment;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
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
import com.myplex.myplex.events.EventNotifyEpgAdapter;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.ProgramGuideChannelActivity;
import com.myplex.myplex.ui.adapter.AdapterChannelEpgForRecyclerView;
import com.myplex.myplex.ui.adapter.DatesAdapter;
import com.myplex.myplex.ui.views.PopUpWindow;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Srikanth on 15-Dec-15.
 */
public class FragmentChannelEpg extends BaseFragment implements CacheManager.CacheManagerCallback {
    private static final String TAG = FragmentChannelEpg.class.getSimpleName();
    private static final String PARAM_CHANNEL_DATA = "selectedChannelData";
    private static final String DATE_POS = "date_pos";
    private static final String PARAM_DATE = "selectedDate";
    private static final String PARAM_FROM = "isFromNotification";
    public static final String PARAM_IS_TO_SHOW_ONLY_EPG = "isToShowOnlyEPG";

    private final AlertDialogUtil.NeutralDialogListener mNeutralDialogListener = new AlertDialogUtil.NeutralDialogListener() {
        @Override
        public void onDialogClick(String buttonText) {
            if (null != buttonText &&
                    buttonText.equalsIgnoreCase(mContext.getString(R.string.play_button_retry))) {
                fetchProgramData(mArguments);
            }
        }
    };

    private final View.OnClickListener mTodayEPGListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mTodayEPGPOPUPWindow != null
                    && mTodayEPGPOPUPWindow.isPopupVisible()) {
                mTodayEPGPOPUPWindow.dismissPopupWindow();
            } else {
                showPopupMenu(v);
            }
        }
    };

    private final View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseActivity != null && !mBaseActivity.isFinishing()) {
                mBaseActivity.removeFragment(FragmentChannelEpg.this);
            }
            //showOverFlowSettings(v);
        }
    };

    private RelativeLayout mTodayEPGLayout;
    private boolean isInDetailsPage = false;
    private List<CardData> adapterData;

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
        ListView mPopUpListView = (ListView) layout.findViewById(R.id.popup_listView);
        final DatesAdapter mPopupListAdapter = new DatesAdapter(mContext, Util.showNextDates());
        mPopupListAdapter.setSelectedPosition(datePos);
        mPopUpListView.setAdapter(mPopupListAdapter);
        mPopUpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTodayEPGPOPUPWindow.dismissPopupWindow();
                datePos = position;
                mPopupListAdapter.setSelectedPosition(datePos);
                mPopupListAdapter.notifyDataSetChanged();
                Date selectedDate = Util.getCurrentDate(position);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                if (null != selectedDate) {
                    String selectedDateInString = format.format(selectedDate);
                    String dateStamp = Util.getYYYYMMDD(selectedDateInString);
                    if (mArguments.containsKey(PARAM_FROM)) {
                        fetchProgramData(dateStamp);
                        return;
                    }
                    Bundle args = new Bundle();
                    args.putSerializable(FragmentChannelEpg.PARAM_CHANNEL_DATA, mChannelData);
                    args.putString(FragmentChannelEpg.PARAM_DATE, dateStamp);
                    fetchProgramData(args);
                }
            }
        });
    }

    private PopUpWindow mTodayEPGPOPUPWindow;
    private Bundle mArguments;

    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mImageViewClose;

    private boolean mIsToShowToolBar = false;
    private LayoutInflater mInflater;

    private RecyclerView mRecyclerView;
    private TextView mTodayEPGTitle;
    private TextView mErrorTextView;
    private ImageView mImageViewChannel;
    private AdapterChannelEpgForRecyclerView mAdapterChannelEpgForRecyclerView;
    private RelativeLayout mRootLayout;
    private CardData mChannelData;
    private Context mContext;
    private int datePos;
    private final CacheManager mCacheManager = new CacheManager();
    private RelativeLayout helpScreenLayout;

    public static FragmentChannelEpg newInstance(Bundle args) {
        FragmentChannelEpg fragmentChannelEpg = new FragmentChannelEpg();
        fragmentChannelEpg.setArguments(args);
        return fragmentChannelEpg;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView: isDetached:- " + isDetached());
        if (mContext == null) {
            mContext = getActivity();
            mBaseActivity = (BaseActivity) getActivity();
        }
        mArguments = getArguments();
        if (mArguments != null && mArguments.containsKey(PARAM_IS_TO_SHOW_ONLY_EPG)) {
            isInDetailsPage = mArguments.containsKey(PARAM_IS_TO_SHOW_ONLY_EPG);
        }
        boolean isFullScreenNeedToPlayed = false;
        if(mArguments != null && mArguments.containsKey(ProgramGuideChannelActivity.PARAM_CHANNEL_DATA)){
            CardData cardData = (CardData) mArguments.getSerializable(ProgramGuideChannelActivity.PARAM_CHANNEL_DATA);
            if(cardData != null && cardData.playFullScreen) {
                isFullScreenNeedToPlayed = true;
            }
        }
        mInflater = LayoutInflater.from(mContext);
        View rootView = inflater.inflate(R.layout.fragment_epg_channel, container, false);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
//        if (mBaseActivity != null) {
//            mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            mBaseActivity.hideActionBar();
//        }
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.tv_guide_channel_listView);
        mTodayEPGLayout = (RelativeLayout) rootView.findViewById(R.id.tv_guide_layout_today_epg);
        mTodayEPGTitle = (TextView) rootView.findViewById(R.id.tv_guide_today_epg_title);
        mErrorTextView = (TextView) rootView.findViewById(R.id.error_message);
        ImageView mTodayEPGButton = (ImageView) rootView.findViewById(R.id.drop_down_button);
        helpScreenLayout = (RelativeLayout) rootView.findViewById(R.id.prog_help_screen_layout);
        mTodayEPGButton.setOnClickListener(mTodayEPGListener);
        mTodayEPGTitle.setOnClickListener(mTodayEPGListener);
        mTodayEPGLayout.setOnClickListener(mTodayEPGListener);

//        Analytics.createScreenGA(Analytics.SCREEN_CHANNEL_EPG);
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_EPG_BROWSED);
        //mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else if (mBaseActivity != null && !isFullScreenNeedToPlayed) {
            mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if(!isFullScreenNeedToPlayed) {
            Util.prepareDisplayinfo(getActivity());
        }
        initToolbar();

        //mInflateView.findViewById(R.id.toolbar_filter_button).setVisibility(View.GONE);
        if (mArguments.containsKey(PARAM_CHANNEL_DATA))
            mChannelData = (CardData) mArguments.getSerializable(PARAM_CHANNEL_DATA);

        int todayPosition = PrefUtils.getInstance().getPrefEnablePastEpg() ? PrefUtils.getInstance().getPrefNoOfPastEpgDays() : ApplicationController.DATE_POSITION;
        datePos = mArguments.getInt(DATE_POS, todayPosition);

        initUI();
        if (mArguments.containsKey(PARAM_FROM)) {
            Date selectedDate = Util.getCurrentDate(datePos);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String selectedDateInString = format.format(selectedDate);
            String dateStamp = Util.getYYYYMMDD(selectedDateInString);
            fetchProgramData(dateStamp);
        } else {
            fetchProgramData(mArguments);
        }
        return rootView;

    }

    private void initToolbar() {
        mInflateView = mInflater.inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mImageViewClose = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        mImageViewChannel = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mIsToShowToolBar = true;
        mToolbar.setVisibility(View.VISIBLE);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mImageViewClose.setOnClickListener(mCloseAction);
    }

    private void initUI() {
/*        mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
        AdapterSmallHorizontalCarousel mAdapterCarouselInfo = new AdapterSmallHorizontalCarousel(mContext, dataList);
        mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);*/

        VerticalSpaceItemDecoration verticalDividerDecoration = new VerticalSpaceItemDecoration((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_8));
        mRecyclerView.addItemDecoration(verticalDividerDecoration);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        mAdapterChannelEpgForRecyclerView = new AdapterChannelEpgForRecyclerView(mContext, getDummyChannelList(), datePos);
        mRecyclerView.setAdapter(mAdapterChannelEpgForRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
//        mRecyclerView.setOnItemClickListener(mProgramClickListener);
        updateChannelImage(mChannelData);
        if (isInDetailsPage) {
            mToolbar.setVisibility(View.GONE);
            mTodayEPGLayout.setVisibility(View.GONE);
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
        if (mArguments.containsKey(APIConstants.NOTIFICATION_PARAM_CONTENT_ID))
            cId = mArguments.getString(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);
        mTodayEPGTitle.setAllCaps(false);
        setDateToCalender(date);
        mCacheManager.getEPGChannelData(cId, dateStamp, false, FragmentChannelEpg.this);
    }

    private void fetchProgramData(Bundle args) {
        if (null != args) {
            EpgFragment.isChannelOpen = false;
            mChannelData = (CardData) args.getSerializable(PARAM_CHANNEL_DATA);

            String dateStamp;
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
            if (args.containsKey(PARAM_DATE)) {
                dateStamp = args.getString(PARAM_DATE);
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
            mCacheManager.getEPGChannelData(cId, dateStamp, false, FragmentChannelEpg.this);
        }

    }

    private void setDateToCalender(String date) {
        Spannable cs = new SpannableString(date);
        if (date.contains("Today")) {
            cs.setSpan(new SuperscriptSpan(), 10, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 10, 12, 0);
        } else {
            cs.setSpan(new SuperscriptSpan(), 7, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 7, 9, 0);
        }
        mTodayEPGTitle.setText(cs);
    }

    private void showToolbar() {
        if (mToolbar == null || !mIsToShowToolBar) {
            return;
        }
        mToolbar.setVisibility(View.VISIBLE);
    }

    private void hideToolbar() {
        if (mToolbar == null) {
            return;
        }
        mToolbar.setVisibility(View.GONE);
    }

    //time analytics
    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause");
        if (mChannelData != null
                && mChannelData.globalServiceName != null) {
            Analytics.gaBrowseChannelEpg(mChannelData.globalServiceName);
            String _id = mChannelData._id;
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mChannelData.generalInfo.type)) {
                _id = mChannelData.globalServiceId; //2
            }
            Analytics.mixpanelBrowseChannelEpg(_id, mChannelData.globalServiceName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (DeviceUtils.isTablet(mContext)
//                && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//        } else {
//            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        //Log.d(TAG, "onResume");
        mAdapterChannelEpgForRecyclerView.notifyDataSetChanged();
    }

    @Override
    public void OnCacheResults(List<CardData> dataList) {
        if (null == dataList) {
            showErrorMessage();
//            AlertDialogUtil.showToastNotification(mContext.getString(R.string.programguide_data_fetch_error));
            return;
        }
        fillData(dataList);
    }

    @Override
    public void OnOnlineResults(List<CardData> dataList) {
        if (null == dataList) {
//            AlertDialogUtil.showToastNotification(mContext.getString(R.string.programguide_data_fetch_error));
            showErrorMessage();
            return;
        }
        fillData(dataList);
    }

    @Override
    public void OnOnlineError(Throwable error, int errorCode) {
        if (errorCode == APIRequest.ERR_NO_NETWORK) {
            AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
                    "", mContext.getString(R.string.play_button_retry), mNeutralDialogListener);
            return;
        }
//        AlertDialogUtil.showToastNotification(mContext.getString(R.string.programguide_data_fetch_error));
    }


    private Handler mHandlerUIUpdate = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.d(TAG, "mHandlerUIUpdate handleMessage: updating UI");
            if (msg != null && msg.obj != null) {
                if (msg.obj instanceof List) {
                    skipCompletedProgramsAndApply((List<CardData>) msg.obj);
                    return;
                }
            }
            if (mAdapterChannelEpgForRecyclerView != null)
                mAdapterChannelEpgForRecyclerView.notifyDataSetChanged();
        }
    };

    private void fillData(List<CardData> channelData) {
        skipCompletedProgramsAndApply(channelData);
    }

    private void skipCompletedProgramsAndApply(final List<CardData> channelData) {

        if (datePos - PrefUtils.getInstance().getPrefNoOfPastEpgDays() >= 0
                || !PrefUtils.getInstance().getPrefEnablePastEpg()) {
            for (Iterator<CardData> it = channelData.iterator(); it.hasNext(); ) {
                CardData cardData = it.next();
                if (null != cardData.endDate
                        && null != cardData.startDate) {
                    Date endDate = Util.getDate(cardData.endDate);
                    Date currentDate = new Date();
                    Date startDate = Util.getDate(cardData.startDate);
                    if (currentDate.after(endDate)) {
                        if (!(currentDate.after(startDate)
                                && currentDate.before(endDate))) {
                            it.remove();
                        }
                    }
                }
            }
        }
        if (channelData == null ||
                channelData.isEmpty()) {
            showErrorMessage();
            return;
        }
        CardData firstProgram = channelData.get(0);
        if (datePos - PrefUtils.getInstance().getPrefNoOfPastEpgDays() >= 0 || !PrefUtils.getInstance().getPrefEnablePastEpg()) {
            if (firstProgram != null && firstProgram.getEndDate() != null) {
                final long programEndDurationInMs = firstProgram.getEndDate().getTime() - new Date().getTime();
                //Log.d(TAG, "handler will update the ui after programEndDurationInMs: " + programEndDurationInMs + "from now");
                if (programEndDurationInMs > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (channelData.size() > 1) {
                                Message message = new Message();
                                message.obj = channelData;
                                mHandlerUIUpdate.sendMessageDelayed(message, programEndDurationInMs);
                            }
                        }
                    }).start();
                }
            }
        }
        setAdapterData(channelData);

    }

    private void showErrorMessage() {
        mErrorTextView.setVisibility(View.VISIBLE);
//        mErrorTextView.setText(mContext.getString(R.string.data_fetch_error));
        mRecyclerView.setVisibility(View.GONE);
        if (!isInDetailsPage) {
            return;
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (resizeFragmentHeight()) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else {
                                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        }
                    }
                });
            }
        });

    }

    private boolean resizeFragmentHeight() {

        if (FragmentChannelEpg.this.getView() == null
                || FragmentChannelEpg.this.getView().getLayoutParams() == null) {
            return false;
        }
        if (mRecyclerView == null
                || mAdapterChannelEpgForRecyclerView == null
                || mRecyclerView.getLayoutManager() == null) {
            LoggerD.debugLog("FragmentChannelEpg: invalid recyclerview and adapter");
            FragmentChannelEpg.this.getView().getLayoutParams().height = 0;
            FragmentChannelEpg.this.getView().getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            FragmentChannelEpg.this.getView().requestLayout();
            return false;
        }

        View v = mRecyclerView.findViewHolderForAdapterPosition(mAdapterChannelEpgForRecyclerView.getItemCount() - 1) == null ? null : mRecyclerView.findViewHolderForAdapterPosition(mAdapterChannelEpgForRecyclerView.getItemCount() - 1).itemView;
        if (v == null) {
            if (FragmentChannelEpg.this.getView() != null
                    && FragmentChannelEpg.this.getView().getLayoutParams() != null) {
                FragmentChannelEpg.this.getView().getLayoutParams().height = 0;
                FragmentChannelEpg.this.getView().getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                FragmentChannelEpg.this.getView().requestLayout();
            }
            LoggerD.debugLog("FragmentChannelEpg: invalid v");
            return false;
        }

        int noOfItems = mAdapterChannelEpgForRecyclerView.getItemCount();
//        v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int totalHeight = (mRecyclerView.getLayoutManager().getDecoratedMeasuredHeight(v)) * noOfItems;
        if (v == null
                && FragmentChannelEpg.this.getView() != null
                && FragmentChannelEpg.this.getView().getLayoutParams() != null) {
            FragmentChannelEpg.this.getView().getLayoutParams().height = 0;
            FragmentChannelEpg.this.getView().getLayoutParams().height = totalHeight;
            FragmentChannelEpg.this.getView().requestLayout();
            return true;
        }
        return false;
    }

    private boolean isHelpScreenShown() {
        if (!ApplicationController.ENABLE_HELP_SCREEN || isInDetailsPage) {
            return false;
        }
        boolean ranBefore = PrefUtils.getInstance().getProgramHelpScreenPref();
        if (!ranBefore) {
            PrefUtils.getInstance().setProgramHelpScreenPref(true);
            helpScreenLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    helpScreenLayout.setVisibility(View.VISIBLE);
                }
            }, 2000);
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

    private void updateChannelImage(CardData cardData) {
        String toolbarTitle = null;
        String imageUrl = null;
        boolean imageLoaded = false;
        if (PrefUtils.getInstance().getPrefEnableDittoChannelLogoOnEpg()
                && cardData != null
                && APIConstants.TYPE_DITTO.equalsIgnoreCase(cardData.contentProvider)) {
            if (!TextUtils.isEmpty(cardData.globalServiceName)) {
                toolbarTitle = cardData.globalServiceName;
            }
            imageUrl = APIConstants.getDittoChannelLogoUrl();
        } else if ((PrefUtils.getInstance().getPrefEnableSonyChannelLogoOnEpg()
                && cardData != null
                && APIConstants.TYPE_SONYLIV.equalsIgnoreCase(cardData.contentProvider))) {
            if (!TextUtils.isEmpty(cardData.globalServiceName)) {
                toolbarTitle = cardData.globalServiceName;
            }
            imageUrl = APIConstants.getSonyChannelLogoUrl();
        } else {
            imageUrl = getImageLinkToolbar(cardData);
        }
        if (!TextUtils.isEmpty(toolbarTitle)) {
            mToolbarTitle.setText(toolbarTitle);
        }
        if (!TextUtils.isEmpty(imageUrl)) {
            imageLoaded = true;
            PicassoUtil.with(mContext).load(imageUrl,mImageViewChannel);
            if (imageUrl.contains(APIConstants.PARAM_SCALE_WRAP)) {
                mImageViewChannel.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            } else {
                mImageViewChannel.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
            }
        }
        if (!imageLoaded) {
            mImageViewChannel.setImageResource(R.drawable.black);
        }
    }

    private String getImageLinkToolbar(CardData cardData) {
        if (cardData == null
                || cardData.images == null
                || cardData.images.values == null
                || cardData.images.values.isEmpty()) {
            return null;
        }
        for (CardDataImagesItem imageItem : cardData.images.values) {
            if (imageItem.type != null && imageItem.type.equalsIgnoreCase("thumbnail") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)
                    && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("250x375")) {
                if (imageItem.link != null && imageItem.link.trim().length() > 0) {
                    return imageItem.link;
                }
            }

        }
        return null;
    }

    private List<CardData> getDummyChannelList() {
        List<CardData> dummyList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            dummyList.add(new CardData());
        }
        return dummyList;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG, "onConfigurationChanged(): " + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Log.d(TAG, "dismiss pop up window");
            if (mTodayEPGPOPUPWindow != null) {
                mTodayEPGPOPUPWindow.dismissPopupWindow();
            }
        }
        super.onConfigurationChanged(newConfig);
    }


    public void updateEPG(int mEpgDatePosition) {
        this.datePos = mEpgDatePosition;
        Date selectedDate = Util.getCurrentDate(datePos);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        if (null != selectedDate) {
            String selectedDateInString = format.format(selectedDate);
            String dateStamp = Util.getYYYYMMDD(selectedDateInString);
            if (mArguments.containsKey(PARAM_FROM)) {
                fetchProgramData(dateStamp);
                return;
            }
            Bundle args = new Bundle();
            args.putSerializable(FragmentChannelEpg.PARAM_CHANNEL_DATA, mChannelData);
            args.putString(FragmentChannelEpg.PARAM_DATE, dateStamp);
            fetchProgramData(args);
        }
    }


    public void onEventMainThread(EventNotifyEpgAdapter event) {
        if (mAdapterChannelEpgForRecyclerView != null) {
            mAdapterChannelEpgForRecyclerView.notifyDataSetChanged();
        }
    }

    public void setAdapterData(List<CardData> channelData) {
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.GONE);
        mAdapterChannelEpgForRecyclerView = new AdapterChannelEpgForRecyclerView(mContext, channelData, datePos);
        mRecyclerView.setAdapter(mAdapterChannelEpgForRecyclerView);
        updateChannelImage(channelData.get(0));
        if (isHelpScreenShown()) {
            helpScreenLayout.setVisibility(View.INVISIBLE);
        }
        if (!isInDetailsPage) {
            return;
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (resizeFragmentHeight())
                            mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        });
    }
}
