package com.myplex.myplex.ui.fragment;

import static android.content.Context.POWER_SERVICE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.view.View.VISIBLE;
import static com.myplex.api.APIConstants.IS_SesonUIVisible;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.github.pedrovgs.DraggableListener;
import com.github.pedrovgs.DraggablePanel;
import com.github.pedrovgs.DraggablePanel.OnVisibilityChanged;
import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.ContentDetails;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.model.CardData;
import com.myplex.model.CardDataCurrentUserData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardDataRelatedMultimedia;
import com.myplex.model.CardDataTags;
import com.myplex.model.CardResponseData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.model.MiniPlayerStatusUpdate;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ads.MiniPulseManager;
import com.myplex.myplex.ads.PulseManager;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.PlayerStreamingAnalytics;
import com.myplex.myplex.events.EventNotifyEpgAdapter;
import com.myplex.myplex.events.EventSoftUpdateData;
import com.myplex.myplex.events.EventUpdatePlayerState;
import com.myplex.myplex.events.MediaPageVisibilityEvent;
import com.myplex.myplex.media.PlayerListener;
import com.myplex.myplex.media.exoVideo.ExoPlayerView;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.previewSeekBar.DownloadImageAndVttFile;
import com.myplex.myplex.previewSeekBar.WebVTTModule;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.views.CatchupItem;
import com.myplex.myplex.ui.views.MiniCardVideoPlayer;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.yupptv.analytics.plugin.YuppAnalytics;
import com.yupptv.playerinterface.YuppExoAnalyticsInterface;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

//import com.ooyala.pulse.PulseVideoAd;
//import static com.myplex.myplex.ui.fragment.CardDetails.PARAM_RELATED_CARD_DATA;
//import static com.myplex.myplex.ui.fragment.CardDetails.PARAM_SEASON_NAME;
//import static com.myplex.myplex.ui.views.MiniCardVideoPlayer.PLAYER_INITIATED;

/**
 * Created by Srikanth on 15-Dec-15.
 */
public class FragmentCardDetailsPlayer extends BaseFragment implements CacheManager.CacheManagerCallback, MiniPlayerStatusUpdate, OnVisibilityChanged {
    private static final String TAG = FragmentCardDetailsPlayer.class.getSimpleName();
    public static final String PARAM_KEPP_DESCRIPTION_VIEWS_UPDATe_DATA = "carddata_soft_update";
    private String orientation = "";
    private View rootView;
    private CardData mCardData;
    private CardData bundleCardData;
    private CardData vttdata;
    public  boolean isSubscriptionError;
    private RelativeLayout videoLayout;
    public MiniCardVideoPlayer mPlayer;
    public int selectedPosition;
    private String mId;
    private String mPartnerId;
    private final CacheManager mCacheManager = new CacheManager();
    private boolean mAutoPlay = false;
    private String mContentType;
    protected int mEpgDatePosition = 0;
    private boolean isTimeShiftHelpScreenShown = false;
    public boolean isMiniMized = false;
    private String mIsHooqContent;
    private final AlertDialogUtil.NeutralDialogListener mNeutralDialogListener = new AlertDialogUtil.NeutralDialogListener() {
        @Override
        public void onDialogClick(String buttonText) {
            if (mCardData == null) {
                return;
            }

            if (null != buttonText &&
                    buttonText.equalsIgnoreCase(mContext.getString(R.string
                            .play_button_retry))) {
                Bundle args = new Bundle();
                args.putString(CardDetails.PARAM_CARD_ID, mCardData._id);
                fetchCardData();
            }
        }
    };
    private boolean duringAd = false;
    private int mPartners = CardDetails.Partners.APALYA;
    public boolean mIsSupportCatchup;
    private String mNid;
    private Toolbar mToolbar;
    private RelativeLayout playLayout;
    private TextView mToolbarTitle;
    private ImageView mImageViewClose;
    private ImageView mToolbarLogo;

    private boolean isAgeRatingVisible = false;
    private boolean isAgeHandler = false;
    private Handler timerHandler = new Handler();

    private boolean isToShowToolBar = false;
    private String mNotificationTitle;
    private long currentContentProgress;
    public FragmentCardDetailsDescription mFragmentCardDetailsDescription;
    private Bundle mArguments;
    public DraggablePanel mDraggablePanel;
    private final DraggableListener mDraggableListener = new DraggableListener() {
        @Override
        public void onMaximized() {
            if(mPlayer != null && mPlayer.isFullScreen()) {
                return;
            }
            enableDraggablePanel();
            // ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.app_bkg));
            isDragging = false;
            showToolbar();
            isPlayingDirectlyInMiniPlayer = false;
            SDKLogger.debug("DraggablePanelDragging");
            if (mPlayer != null) {
                if (mPlayer.exoPlayerInstance() != null){
                    ExoPlayerView exoPlayerView = mPlayer.exoPlayerInstance();
                    exoPlayerView.handleAspectRatioOnFullScreen(false);
                }
                mPlayer.setMinimized(false);
                mPlayer.allowMediaController(true);
                if (/*mPlayer.isMediaPlaying()*/ mPlayer.mPlayerState==1 || mPlayer.mPlayerState==4 || mPlayer.mPlayerState==3) {
                    ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.app_bkg));
                    ((MainActivity) mContext).disableNavigation();
                    showDVRHelpScreen();
                    mPlayer.resumePreviousOrientaionTimer();
                }
                if (mDraggablePanel == null) return;
                mDraggablePanel.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null && mPlayer.shouldShowSubtitleView())
                            mPlayer.showSubtitleView();
                    }
                });
            }
            maximizePlayerControls();

            String duration = PrefUtils.getInstance().getMaturityTimer();
            if (mPlayer != null && mPlayer.ageRatingLayout != null && mPlayer.ageRatingLayoutPortrait != null) {

                mPlayer.ageRatingLayout.setVisibility(View.GONE);
                mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(duration)) {
                    String[] maturyData = duration.split(",");
                    String timer = maturyData[0];
                    String isPortrait = maturyData[1];
                    if (timer != null && !timer.isEmpty() && Integer.parseInt(timer) != 0) {
                        if (mCardData != null && mCardData.content != null && !TextUtils.isEmpty(mCardData.content.categoryType)) {
                            if (!isAgeRatingVisible) {
                                if (isPortrait.equalsIgnoreCase("true")) {
                                    mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                    mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                } else {
                                    mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                    mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }
            }
        }



        private void maximizePlayerControls(){
            RelativeLayout.LayoutParams layoutParams ;
            layoutParams = new RelativeLayout.LayoutParams(ApplicationController.getApplicationConfig().screenWidth,   getHeight(ApplicationController.getApplicationConfig().screenWidth));
            if (DeviceUtils.isTablet(mContext)) {
                layoutParams = new RelativeLayout.LayoutParams(ApplicationController.getApplicationConfig().screenWidth,   ApplicationController.getApplicationConfig().screenHeight + softNavigationBarHeight);
            }
            // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            if (mPlayer.mVideoView!= null) {
                layoutParams.setMargins(0,0,0,0);
                mPlayer.mVideoView.setLayoutParams(layoutParams);
            }
            if (mPlayer.imageFrameOverlay!= null) {
                layoutParams.setMargins(0,0,0,0);
                mPlayer.imageFrameOverlay.setLayoutParams(layoutParams);
            }
            if (mPlayer.mProgressBarLayout!= null) {
                layoutParams.setMargins(0,0,0,0);
                mPlayer.mProgressBarLayout.setLayoutParams(layoutParams);
            }
            if (mPlayer.mAdContainer!= null) {
                layoutParams.setMargins(0,0,0,0);
                mPlayer.mAdContainer.setLayoutParams(layoutParams);
            }
            if(mPlayer != null) {
              /*  mPlayer.mBackIconImageViewLayout.setVisibility(View.GONE);
                mPlayer.mBackIconImageView.setVisibility(View.GONE);*/
                //  mPlayer.back_nav_icon_3.setVisibility(View.GONE);
                mPlayer.back_nav_icon_layout.setVisibility(VISIBLE);
            }
            mPlayer.metaDataLayout.setVisibility(View.GONE);
            if (videoLayout!= null) {
                videoLayout.getLayoutParams().height =  getHeight(ApplicationController.getApplicationConfig().screenWidth);
                videoLayout.getLayoutParams().width =  ApplicationController.getApplicationConfig().screenWidth;
            }
            if (DeviceUtils.isTablet(mContext)) {
                if (videoLayout!= null) {
                    //  videoLayout.getLayoutParams().height =  getHeight(ApplicationController.getApplicationConfig().screenWidth);
                    // videoLayout.getLayoutParams().width =  ApplicationController.getApplicationConfig().screenWidth;
                    videoLayout.getLayoutParams().width =  ApplicationController.getApplicationConfig().screenWidth;
                    videoLayout.getLayoutParams().height =   ApplicationController.getApplicationConfig().screenHeight + softNavigationBarHeight;
                }
            }
            mPlayer.metaDataLayout.setVisibility(View.GONE);
            //  mPlayer.blurLayout.setBackgroundColor(getResources().getColor(R.color.app_bkg));
         /*   RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ApplicationController.getApplicationConfig().screenWidth,  getHeight(ApplicationController.getApplicationConfig().screenWidth));
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            if (videoLayout!= null && videoLayout != null) {
                videoLayout.setLayoutParams(layoutParams1);
            }*/
        }


        @Override
        public void onMinimized() {
            SDKLogger.debug("DraggablePanelDragging");
            if(mPlayer.isMediaPlaying()) {
                if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                } else {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            //((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            isDragging = false;
            isMiniMized = true;

            ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
            if (mPlayer != null ) {
                hideToobar();
//                mPlayer.setMinimized(true);
                // enableDraggablePanel();

                mPlayer.resumePreviousOrientaionTimer();
                hideMediaController();
                mPlayer.allowMediaController(false);
                if (mPlayer.isMediaPlaying() || mPlayer.mPlayerState==1) {
                    mPlayer.setMinimized(true);
                    ((MainActivity) mContext).sendMiniPlayerEnabledBroadCast();
                    mPlayer.resumePreviousOrientaionTimer();
                }
                if (mDraggablePanel == null) return;
                mDraggablePanel.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayer.hideSubtitleView();
                    }
                });
                minimizePlayerControls();
            }
            if (mPlayer != null && mPlayer.ageRatingLayout != null && mPlayer.ageRatingLayoutPortrait != null) {
                mPlayer.ageRatingLayout.setVisibility(View.GONE);
                mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
            }
            if(mPlayer != null) {
              /*  mPlayer.mBackIconImageViewLayout.setVisibility(View.GONE);
                mPlayer.mBackIconImageView.setVisibility(View.GONE);*/
                //  mPlayer.back_nav_icon_3.setVisibility(View.GONE);
                mPlayer.back_nav_icon_layout.setVisibility(View.GONE);
            }
            if(mCardData != null && mCardData.globalServiceName != null)
                mPlayer.channelName.setText(mCardData.globalServiceName);
            else {
                if(mCardData != null){
                    if(mCardData.getParnterTitle(mContext) != null)
                        mPlayer.channelName.setText(mCardData.getParnterTitle(mContext));
                }
            }
            if(mCardData != null && mCardData.getTitle() != null)
                mPlayer.programName.setText(mCardData.getTitle());
        }

        @Override
        public void onClosedToLeft() {
            Util.debugLog("onClosedToLeft()");
            isDragging = false;
            maximizePlayer();
            ((MainActivity) mContext).enableNavigation();
//            smoothSlideToHalfScreen();
            closeMaturityData();
            if (mCardData != null && mPlayer != null){
                if (mPlayer.mIsAdPlaying){
                    PlayerStreamingAnalytics.getInstance().notifyEnded(mCardData, true,mPlayer.adEventEllapsedTime);
                }else {
                    PlayerStreamingAnalytics.getInstance().notifyEnded(mCardData, false,0);
                }
            }
        }

        @Override
        public void onClosedToRight() {
            isDragging = false;
            Util.debugLog("onClosedToRight()");
            maximizePlayer();
            ((MainActivity) mContext).enableNavigation();
//            smoothSlideToHalfScreen();

            closeMaturityData();
            if (mCardData != null && mPlayer != null){
                if (mPlayer.mIsAdPlaying){
                    PlayerStreamingAnalytics.getInstance().notifyEnded(mCardData, true,mPlayer.adEventEllapsedTime);
                }else {
                    PlayerStreamingAnalytics.getInstance().notifyEnded(mCardData, false,0);
                }
            }
        }
    };

    public void minimizePlayerControls(){
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)getResources().getDimension(R.dimen._95sdp), (int)getResources().getDimension(R.dimen._120sdp));
        // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        if (mPlayer.mVideoView!= null) {
//                layoutParams.setMargins(10,10,10,10);
            mPlayer.mVideoView.setLayoutParams(layoutParams);
        }
        if (mPlayer.imageFrameOverlay!= null) {
//                layoutParams.setMargins(10,10,10,10);
            mPlayer.imageFrameOverlay.setLayoutParams(layoutParams);
        }
        if (mPlayer.mProgressBarLayout!= null) {
//                layoutParams.setMargins(10,10,10,10);
            mPlayer.mProgressBarLayout.setLayoutParams(layoutParams);
        }
        if (mPlayer.mAdContainer!= null) {
//                layoutParams.setMargins(10,10,10,10);
            mPlayer.mAdContainer.setLayoutParams(layoutParams);
        }
        if(mPlayer.mVideoViewPlayer != null && mPlayer.mVideoViewPlayer.isPlaying()) {
            if (mPlayer.playPauseBtn != null)
                mPlayer.playPauseBtn.setImageResource(R.drawable.ic_dock_pause);
        } else  if(mPlayer.mVideoViewPlayer != null && mPlayer.mVideoViewPlayer.isPlayerPaused()) {
            if(mPlayer.playPauseBtn!= null)
                mPlayer.playPauseBtn.setImageResource(R.drawable.ic_dock_play);
        }  else {
            if(mPlayer.playPauseBtn!= null)
                mPlayer.playPauseBtn.setImageResource(R.drawable.ic_dock_pause);
        }
        mPlayer.metaDataLayout.setVisibility(View.VISIBLE);
        mPlayer.mParentLayout.setBackgroundColor(Color.TRANSPARENT);
        // mPlayer.blurLayout.setBackgroundColor(getResources().getColor(R.color.black_30));

              /*if(mPlayer.isAdPlaying()){
                enableDraggablePanel();
            }else {*/
        disableDraggablePanel();
//            }
         /*   if (videoLayout!= null && videoLayout != null) {
                videoLayout.getLayoutParams().height = 200;
            }*/
        // ((MainActivity) getActivity()).mDraggablePanel.draggableView.setTopViewWidth(((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth()*2);
        Log.d(TAG, "minimizePlayerControls: ApplicationController.getApplicationConfig().screenWidth " + ApplicationController.getApplicationConfig().screenWidth);
        if(!DeviceUtils.isTablet(mContext)) {





            RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ApplicationController.getApplicationConfig().screenWidth, (int) getResources().getDimension(R.dimen._130sdp));
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//            layoutParams1.setMargins(10,10,10,10);
            if (videoLayout != null) {
                videoLayout.setLayoutParams(layoutParams1);
            }
        }else{
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int heights = displayMetrics.heightPixels;
            int widths = displayMetrics.widthPixels;

            RelativeLayout.LayoutParams layoutParams1;
            layoutParams1 = new RelativeLayout.LayoutParams(ApplicationController.getApplicationConfig().screenWidth, (int) getResources().getDimension(R.dimen._35sdp));
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            /*if (widths <= 1800) {
                layoutParams1 = new RelativeLayout.LayoutParams(ApplicationController.getApplicationConfig().screenWidth, (int) getResources().getDimension(R.dimen._35sdp));
            }
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);*/
            if (videoLayout!= null) {
                videoLayout.setLayoutParams(layoutParams1);
            }
        }
        mPlayer.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDraggablePanel.draggableView.closeToLeft();
            }
        });
        mPlayer.metaDataLayout.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDraggablePanel.draggableView.maximize();
                    break;
            }
            return false;
        });

        mPlayer.mVideoView.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if(mDraggablePanel != null && mDraggablePanel.draggableView != null && mDraggablePanel.draggableView.
                            checkMinimized()) {
                        mDraggablePanel.draggableView.maximize();
                    }
                    break;
            }
            return false;
        });
    }
    private boolean isVisible;
    private boolean isFromBackground;
    private boolean isFromWaitRetry;
    private CardData mTVShowCardData;
    private String mSeasonName;
    private String mSource;
    private String mSourceDetails;
    private String mSourceTab;
    private String mBgColor;
    private int sourceCarouselPosition;
    private boolean mKeepDescriptionViewsAndUpdateData;
    private int softNavigationBarHeight;
    private String tabName;
    private String seasonId;

    private boolean isPreviewInitialWorkDone = false;

    private void smoothSlideToHalfScreen() {
        if (mDraggablePanel == null) {
            return;
        }
        mDraggablePanel.slideToMiddle();
    }

    private boolean isPlayingDirectlyInMiniPlayer = false;
    private boolean isDragging;
    private final ViewDragHelper.Callback mDraggableViewCallbackListener = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return false;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            boolean isDraggedMinimumRange = mDraggablePanel.isDraggedMinimumRange();
            if (isDraggedMinimumRange) {
                if (mPlayer != null && mPlayer.isMediaPlaying()) {
                    hideToobar();
                    mPlayer.allowMediaController(false);
                    hideMediaController();
                }
            } else {
                if (autoPlayMinimize && isPlayingDirectlyInMiniPlayer) {
                    hideToobar();
                    return;
                }
                showToolbar();
                if (mPlayer != null && mPlayer.isFullScreen()) {
                    hideToobar();
                }
                mPlayer.allowMediaController(true);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            SDKLogger.debug("DraggablePanelDragging- " + state);
            if (state == DraggablePanel.ON_DRAG_START) {
                isDragging = true;
                if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                } else {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
               // ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
        }
    };
    private View mInflateView;
    private boolean mAdEnabled;
    private String mAdProvider;
    private boolean autoPlayMinimize;
    public List<CardData> mListQueueCardData;

    public static FragmentCardDetailsPlayer newInstance(Bundle args) {
        FragmentCardDetailsPlayer fragmentDetails = new FragmentCardDetailsPlayer();
        fragmentDetails.setArguments(args);
        return fragmentDetails;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mArguments != null) {
            outState = mArguments;
        }
        super.onSaveInstanceState(outState);
        outState.putString(CardDetails.PARAM_CARD_ID, mId);
        outState.putString(CardDetails.PARAM_CARD_DATA_TYPE, mContentType);
        outState.putInt(CardDetails.PARAM_PARTNER_TYPE, mPartners);
        outState.putBoolean(CardDetails.PARAM_AUTO_PLAY, mAutoPlay);
        outState.putInt(CardDetails.PARAM_EPG_DATE_POSITION, mEpgDatePosition);
        outState.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, mCardData);
        if (mPlayer != null) {
            outState.putLong(CardDetails.CURRENT_CONTENT_PROGRESS, mPlayer.getmSavedContentPosition());
        }
        outState.putInt(CardDetails.PARAM_EPG_DATE_POSITION, mEpgDatePosition);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mBaseActivity = (BaseActivity) getActivity();


        rootView = inflater.inflate(R.layout.fragment_card_details_player, container, false);
//        mToolbar = mBaseActivity.getDraggablePanelToolbar();
        playLayout = (RelativeLayout) rootView.findViewById(R.id.play_layout);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        int statusBarHeight = Util.getStatusBarHeight(mContext);
        FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) playLayout.getLayoutParams();
        if(layoutParams1 !=null) {
            layoutParams1.topMargin = statusBarHeight;
            playLayout.setLayoutParams(layoutParams1);
        }
        // ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.app_bkg));
        playInPortrait();
        softNavigationBarHeight = UiUtil.getSoftButtonsBarSizePort((Activity) mContext);
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else /*if (mBaseActivity != null)*/ {
            mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mBaseActivity.hideActionBar();
        }
        videoLayout = (RelativeLayout) rootView
                .findViewById(R.id.carddetail_videolayout);
      /*  mTextViewProgressBar = (TextView) rootView.findViewById(R.id.card_loading_progress);
        mRLayoutTimeShiftHelp = (RelativeLayout) rootView
                .findViewById(R.id.layout_timeshift_help_screen);*/
       /* int statusBarHeight = Util.getStatusBarHeight(mContext);
        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) videoLayout.getLayoutParams();
        layoutParams1.topMargin = statusBarHeight;
        videoLayout.setLayoutParams(layoutParams1);
*/
        mInflateView = LayoutInflater.from(mContext).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mImageViewClose = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mToolbarLogo = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        RelativeLayout mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        Util.debugLog("savedInstanceState- " + savedInstanceState);
        if (savedInstanceState == null) {
            savedInstanceState = getArguments();
            Util.debugLog("arguments- " + savedInstanceState);
        }
        mArguments = savedInstanceState;
        initializeBundleValues(mArguments);
        getCOntentDetailMultimedia();
        /*initializePlayerView();
        fetchCardData();*/
        return rootView;
    }

    private void initializePlayerView() {
        if (!isAdded() || isDetached() || rootView == null) {
            Util.debugLog("Fragment is not added or detached");
            return;
        }
        if (mPlayer != null
                && (mPlayer.isMediaPlaying()
                || mPlayer.isPlayerInitialized())) {
            Util.debugLog("MiniCardVideoPlayer still playing closing");
            mPlayer.quitPlayer();
        }

        if (mPartners == CardDetails.Partners.SONY) {
            mPlayer = new MiniPulseManager(mContext, mCardData, mId);
            videoLayout.removeAllViews();
            videoLayout.addView(mPlayer.CreatePlayerView(videoLayout));
            Util.debugLog("MiniPulseManager init");
        } else {
            mPlayer = new MiniCardVideoPlayer(mContext, mCardData, mId);
            videoLayout.removeAllViews();
            if (mPartners == CardDetails.Partners.HOOQ) {
                mPlayer.setVideoPlayerType(MiniCardVideoPlayer.VIDEO_PLAYER_TYPE.HOOQ_PLAYERVIEW);
            } else if (mPartners == CardDetails.Partners.HUNGAMA) {
                mPlayer.setVideoPlayerType(MiniCardVideoPlayer.VIDEO_PLAYER_TYPE.HUNGAMA_PLAYER_VIEW);
            }
            videoLayout.addView(mPlayer.CreatePlayerView(videoLayout));
            Util.debugLog("MiniCardVideoPlayer init");
        }
        if (mFragmentCardDetailsDescription != null) {
            mFragmentCardDetailsDescription.setPlayer(mPlayer);
        }
        Util.debugLog("MiniCardVideoPlayer " + " CardDetails _id: " + mId);
        mPlayer.setNotificationTitle(mNotificationTitle);
        mPlayer.setVODContentType(mContentType);
        mPlayer.setPlayerStatusUpdateListener(this);
        mPlayer.setOnClickThroughCallback(new PulseManager.ClickThroughCallback() {
         /*   @Override
            public void onClicked(PulseVideoAd ad) {
                if (ad.getClickthroughURL() != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad
                            .getClickthroughURL().toString()));
                    startActivity(intent);
                } else {
                    mPlayer.returnFromClickThrough();
                }
            }*/
        });
        mPlayer.setNid(mNid);
        if (mPlayer != null) {
            mPlayer.setEpgDatePosition(mEpgDatePosition);
        }
//        mDraggablePanel.setTopFragmentResize(true);
//        mDraggablePanel.setTopViewWidth(ApplicationController.getApplicationConfig().screenWidth);
        mPlayer.setmSavedContentPosition(currentContentProgress);
        ((MainActivity) mBaseActivity).setDraggableListener(mDraggableListener);
        isPlayingDirectlyInMiniPlayer = false;
        // mPlayer.queueListCardData(mListQueueCardData);
        mPlayer.setRelatedCardData(mTVShowCardData);
        mPlayer.setSource(mSource);
        mPlayer.setSourceDetails(mSourceDetails);
        mPlayer.setSourceTab(mSourceTab);
        mPlayer.setSourceCarouselPosition(sourceCarouselPosition);
        mPlayer.setmFragmentCardDetailsDescription(mFragmentCardDetailsDescription);
        if (!mAutoPlay) {
            disableDraggablePanel();
        }

        if (DeviceUtils.isTablet(mContext)
                && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            startInLandscapeWithNav();
        } else {
            if (autoPlayMinimize) {
                isPlayingDirectlyInMiniPlayer = false;
                enableDraggablePanel();
                minimizePlayer();
                hideToobar();
            } else {
                disableDraggablePanel();
//            if(ApplicationController.ENABLE_LIVETV_AUTO_PLAY){
                maximizePlayer();
//            }
            }
        }
    }

    private void initializeBundleValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        mPartners = CardDetails.Partners.APALYA;
        if (savedInstanceState.containsKey(CardDetails
                .PARAM_PARTNER_TYPE)) {
            mPartners = savedInstanceState.getInt(CardDetails.PARAM_PARTNER_TYPE);
        }
        if (savedInstanceState.containsKey(CardDetails
                .PARAM_SUPPORT_CATCHUP)) {
            mIsSupportCatchup = savedInstanceState.getBoolean(CardDetails.PARAM_SUPPORT_CATCHUP);
        }
        mPartnerId = null;
        if (savedInstanceState.containsKey(CardDetails.PARAM_PARTNER_ID)) {
            mPartnerId = savedInstanceState.getString(CardDetails.PARAM_PARTNER_ID);
        }

//        mPartnerId = HooqVstbUtilityHandler.DOWNLAOD_ID_2;
        mAdEnabled = false;
        if (savedInstanceState.containsKey(CardDetails.PARAM_AD_ENBLED)) {
            mAdEnabled = savedInstanceState.getBoolean(CardDetails.PARAM_AD_ENBLED);
        }

        mAdProvider = null;
        if (savedInstanceState.containsKey(CardDetails.PARAM_AD_PROVIDER)) {
            mAdProvider = savedInstanceState.getString(CardDetails.PARAM_AD_PROVIDER);
        }

        mId = null;
        if (savedInstanceState.containsKey(CardDetails.PARAM_CARD_ID)) {
            mId = savedInstanceState.getString(CardDetails.PARAM_CARD_ID);
        }

        mNid = null;
        if (savedInstanceState.containsKey(APIConstants.NOTIFICATION_PARAM_NID)) {
            mNid = savedInstanceState.getString(APIConstants.NOTIFICATION_PARAM_NID);
        }

        mNotificationTitle = null;
        if (savedInstanceState.containsKey(APIConstants.NOTIFICATION_PARAM_TITLE)) {
            mNotificationTitle = savedInstanceState.getString(APIConstants.NOTIFICATION_PARAM_TITLE);
        }
        LoggerD.debugLog("mNotificationTitle From FragmentCardDetailsPlayer mNotificationTitle- " + mNotificationTitle);

        mAutoPlay = false;
        if (PrefUtils.getInstance().isAutoplay() && savedInstanceState.containsKey(CardDetails.PARAM_AUTO_PLAY)) {
            mAutoPlay = savedInstanceState.getBoolean(CardDetails.PARAM_AUTO_PLAY);
            LoggerD.debugDownload("mAutoPlay- " + mAutoPlay);
        }
        mContentType = null;
        if (savedInstanceState.containsKey(CardDetails
                .PARAM_CARD_DATA_TYPE)) {
            mContentType = savedInstanceState.getString(CardDetails.PARAM_CARD_DATA_TYPE);
        }

        mCardData = null;
        if (savedInstanceState.containsKey(CardDetails.PARAM_SELECTED_CARD_DATA)) {
            mCardData = (CardData) savedInstanceState.getSerializable(CardDetails
                    .PARAM_SELECTED_CARD_DATA);
            bundleCardData = mCardData;
            if(bundleCardData != null) {
                // onUpdatePlayerData(bundleCardData);
                mFragmentCardDetailsDescription.onDataLoaded(bundleCardData, bundleCardData.generalInfo.type, mKeepDescriptionViewsAndUpdateData);
            }
            mCacheManager.setSelectedCardData(mCardData);
        }

        mEpgDatePosition = 0;
        if (savedInstanceState.containsKey(CardDetails.PARAM_EPG_DATE_POSITION)) {
            mEpgDatePosition = savedInstanceState.getInt(CardDetails.PARAM_EPG_DATE_POSITION);
        }

        currentContentProgress = 0;
        if (savedInstanceState.containsKey(CardDetails.CURRENT_CONTENT_PROGRESS)) {
            currentContentProgress = savedInstanceState.getLong(CardDetails.CURRENT_CONTENT_PROGRESS);
            Util.debugLog("mSavedContentPosition: " + currentContentProgress);
        }

        if (savedInstanceState.containsKey(CardDetails.PARAM_RESET_EPG_DATE_POSITION_IN_DETAIL)) {
            Util.debugLog("PARAM_RESET_EPG_DATE_POSITION_IN_DETAIL: ");
            mEpgDatePosition = 0;
        }

        if (mFragmentCardDetailsDescription != null) {
            mFragmentCardDetailsDescription.setEpgDatePosition(mEpgDatePosition);
        }

        autoPlayMinimize = false;
        if (savedInstanceState.containsKey(CardDetails.PARAM_AUTO_PLAY_MINIMIZED)) {
            autoPlayMinimize = savedInstanceState.getBoolean(CardDetails.PARAM_AUTO_PLAY_MINIMIZED);
        }
        String mAffiliateValue = null;
        if (savedInstanceState.containsKey(APIConstants.AFFILIATE_VALUE)) {
            mAffiliateValue = savedInstanceState.getString(APIConstants.AFFILIATE_VALUE);
            mFragmentCardDetailsDescription.setAffiliateValue(mAffiliateValue);
        }

//        mListQueueCardData = CacheManager.getCardDataList();

        mTVShowCardData = null;
       /* if (savedInstanceState.containsKey(PARAM_RELATED_CARD_DATA)) {
            mTVShowCardData = (CardData) savedInstanceState.getSerializable(PARAM_RELATED_CARD_DATA);
        }*/

        mSeasonName = null;
       /* if (savedInstanceState.containsKey(PARAM_SEASON_NAME)) {
            mSeasonName = savedInstanceState.getString(PARAM_SEASON_NAME);
            LoggerD.debugDownload("seasonName " + mSeasonName);
        }*/

        mSource = null;
        if (savedInstanceState.containsKey(Analytics.PROPERTY_SOURCE)) {
            mSource = savedInstanceState.getString(Analytics.PROPERTY_SOURCE);
            LoggerD.debugDownload("mSource-  " + mSource);
        }

        seasonId = null;
        if (savedInstanceState.containsKey(CardDetails.PARAM_SEASON_GLOBAL_SERVICE_ID)) {
            seasonId = savedInstanceState.getString(CardDetails.PARAM_SEASON_GLOBAL_SERVICE_ID);
            LoggerD.debugDownload("seasonId-  " + seasonId);
        }

        tabName = null;
        if (savedInstanceState.containsKey(APIConstants.TAB_NAME)) {
            tabName = savedInstanceState.getString(APIConstants.TAB_NAME);
        }
        mSourceDetails = null;
        if (savedInstanceState.containsKey(Analytics.PROPERTY_SOURCE_DETAILS)) {
            mSourceDetails = savedInstanceState.getString(Analytics.PROPERTY_SOURCE_DETAILS);
            LoggerD.debugDownload("mSource-  " + mSourceDetails);
        }
        mSourceTab = null;
        if (savedInstanceState.containsKey(CleverTap.PROPERTY_TAB)) {
            mSourceTab = savedInstanceState.getString(CleverTap.PROPERTY_TAB);
            LoggerD.debugDownload("mSource-  " + mSourceTab);
        }
        sourceCarouselPosition = -1;
        if (savedInstanceState.containsKey(CleverTap.PROPERTY_CAROUSEL_POSITION)) {
            sourceCarouselPosition = savedInstanceState.getInt(CleverTap.PROPERTY_CAROUSEL_POSITION, -1);
            LoggerD.debugDownload("sourceCarouselPosition-  " + sourceCarouselPosition);
        }

        mKeepDescriptionViewsAndUpdateData = false;
        if (PrefUtils.getInstance().isAutoplay() && savedInstanceState.containsKey(PARAM_KEPP_DESCRIPTION_VIEWS_UPDATe_DATA)) {
            mKeepDescriptionViewsAndUpdateData = savedInstanceState.getBoolean(PARAM_KEPP_DESCRIPTION_VIEWS_UPDATe_DATA);
            LoggerD.debugDownload("mKeepDescriptionViewsAndUpdateData- " + mKeepDescriptionViewsAndUpdateData);
        }

        mBgColor = null;
        if (savedInstanceState.containsKey(APIConstants.BG_COLOR)) {
            mBgColor = savedInstanceState.getString(APIConstants.BG_COLOR);
        }

        if (mFragmentCardDetailsDescription != null) {
            mFragmentCardDetailsDescription.setTVShowCardData(mTVShowCardData);
            mFragmentCardDetailsDescription.setTVSeasonName(mSeasonName);
            mFragmentCardDetailsDescription.setSeasonId(seasonId);
            mFragmentCardDetailsDescription.setTabName(tabName);
            mFragmentCardDetailsDescription.setBgColor(mBgColor);
        }
    }

    private final View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseActivity != null) {
                mBaseActivity.onBackPressed();
            } else {
                onBackClicked();
            }
        }
    };

    private void initToolbar() {
        isToShowToolBar = false;
        hideToobar();
        if (!isToShowToolBar) {
            return;
        }

        int topViewHeight = (int) getHeight(ApplicationController.getApplicationConfig().screenWidth);
        /*if (mCardData!=null&&mCardData.isMovie()){
            topViewHeight=(int) (((ApplicationController.getApplicationConfig().screenWidth * 4) / 3));
        }*/

        if (DeviceUtils.isTablet(mContext)
                && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            topViewHeight = ApplicationController.getApplicationConfig().screenHeight;
        }

        String logoImageUrl = null;
        String toolbarTitle = null;

        if (!TextUtils.isEmpty(mCardData.globalServiceName)) {
            toolbarTitle = mCardData.globalServiceName;
        }

        if ((PrefUtils.getInstance().getPrefEnableDittoChannelLogoOnEpg()
                && mCardData != null
                && mCardData.contentProvider != null
                && mCardData.contentProvider.equalsIgnoreCase(APIConstants.TYPE_DITTO))) {
            isToShowToolBar = true;
            logoImageUrl = APIConstants.getDittoChannelLogoUrl();
        } else if ((PrefUtils.getInstance().getPrefEnableSonyChannelLogoOnEpg()
                && mCardData != null
                && mCardData.contentProvider != null
                && mCardData.contentProvider.equalsIgnoreCase(APIConstants.TYPE_SONYLIV))) {
            isToShowToolBar = true;
            logoImageUrl = APIConstants.getSonyChannelLogoUrl();
        } else if ((mCardData != null
                && mCardData.generalInfo != null
                && mCardData.generalInfo.type != null
                && APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(mCardData.generalInfo.type)
                && mCardData.publishingHouse != null
                && APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(mCardData.publishingHouse.publishingHouseName))) {
            isToShowToolBar = true;
            logoImageUrl = APIConstants.getErosNowMusicLogoUrl();
            if (!TextUtils.isEmpty(mCardData.generalInfo.title)) {
                toolbarTitle = mCardData.generalInfo.title;
            }
        } else if (PrefUtils.getInstance().getPrefEnableYuptvChannelLogoOnEpg()
                && mCardData != null
                && mCardData.contentProvider != null
                && mCardData.contentProvider.equalsIgnoreCase(APIConstants.TYPE_YUPP_TV)) {
            isToShowToolBar = true;
            logoImageUrl = APIConstants.getYupTVLogoUrl();
        }


        if (isToShowToolBar) {
            if (!autoPlayMinimize) {
                showToolbar();
            }
            if (mToolbarTitle != null) {
                mToolbarTitle.setVisibility(View.INVISIBLE);
            }
            if (!TextUtils.isEmpty(toolbarTitle)) {
                mToolbarTitle.setText(toolbarTitle);
                mToolbarTitle.setVisibility(View.VISIBLE);
            }
            mImageViewClose.setOnClickListener(mCloseAction);
            topViewHeight = (int) (topViewHeight + mContext.getResources().getDimension(R.dimen.action_bar_height));
//            mToolbarLogo.setImageResource(R.drawable.xxhdpi_ditto_logo_android);
            if (logoImageUrl.contains(APIConstants.PARAM_SCALE_WRAP)) {
                mToolbarLogo.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            } else {
                mToolbarLogo.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
            }
            PicassoUtil.with(mContext).load(logoImageUrl, mToolbarLogo);
        }

        if (mDraggablePanel != null
                && mPlayer != null
                && mPlayer.changeOrientationOnClose()) {
            mDraggablePanel.setTopViewHeight(topViewHeight);
            mDraggablePanel.ensureTopViewWidth();
            mDraggablePanel.setTopFragmentResize(true);
        }

    }

    private void fetchCardData() {
        if (mId == null) {
            return;
        }
        mCacheManager.getCardDetails(mId, true, FragmentCardDetailsPlayer.this);

    }

    @Override
    public void onCloseFragment() {
        LoggerD.debugLog("onCloseFragment");
        if (mFragmentCardDetailsDescription != null) {
            mFragmentCardDetailsDescription.onCloseFragment();
        }
        // ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // send a bus event to inform player is closed if any one is registered for the MediaPageVisibilityEvent
        mIsSupportCatchup=false;
        EventBus.getDefault().post(new MediaPageVisibilityEvent(false));
        if (mPlayer != null && (mPlayer.isMediaPlaying() || mPlayer.isPlayerInitialized())) {
            mPlayer.closePlayer();
        }
        ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
    }


    @Override
    public boolean onBackClicked() {
        try {
            if (IS_SesonUIVisible) {
                mPlayer.dismissedSeasonUI();
                return false;
            }
            // below code is to close the player when the player is loading state
          /*  if ( mPlayer.mPlayerState == PLAYER_INITIATED){
                mPlayer.closePlayer();
                closePlayerFragment();
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            }*/
            if(mPlayer != null && mPlayer.subscriptionErrorLayout != null) {
                mPlayer.subscriptionErrorLayout.setVisibility(View.GONE);
            }
            if (isSubscriptionError){
                isSubscriptionError = false;
                mPlayer.closePlayer();
                closePlayerFragment();
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            }
            if (mFragmentCardDetailsDescription != null && mFragmentCardDetailsDescription.onBackClicked()) {
                return true;
            }
            // landscape mode
            if (mPlayer.isFullScreen()) {
//                if (!mContext.getResources().getBoolean(R.bool.isTablet)) {
               /* if (DeviceUtils.isTablet(mContext) &&
                        mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (mPlayer.isMediaPlaying() || mPlayer.isPlayerInitialized()) {
                        mPlayer.stopMOUTracking();
                        mPlayer.onStateChanged(PlayerListener.STATE_PAUSED, mPlayer.getStopPosition());
                        mPlayer.closePlayer();
                        ((MainActivity) mBaseActivity).enableNavigation();
                    }
                    hideToobar();
                    closePlayerFragment();
                    if (mCardData != null
                            && mCardData.generalInfo != null
                            && mCardData.generalInfo.title != null) {
                        if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)) {
                            Analytics.gaBrowseProgramDetails(mCardData.generalInfo.title);
                        }
                        if (APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(mCardData.generalInfo.type)) {
                            String subgenre = null;
                            if (mCardData.content != null
                                    && mCardData.content.genre != null
                                    && mCardData.content.genre.size() > 0) {
                                subgenre = mCardData.content.genre.get(0).name;
                            }
                            Analytics.gaPlayedVideoTimeCalculationForYoutube(Analytics.ACTION_TYPES.play.name(), mCardData.generalInfo.title, mCardData._id, subgenre, (mCardData == null || mCardData.publishingHouse == null) ? APIConstants.NOT_AVAILABLE : mCardData.publishingHouse.publishingHouseName);
                        }
                    }
                    Analytics.setVODSuperCardData(null);
                    Analytics.setVideosCarouselName(null);
                    return true;
                } else*/ { // player is in landscape mode
                    if (mPlayer.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || mPlayer.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        //Considering the TabUtils condition as from landscape to doc player the doc player is hanging for some time in top in portrait
                      /*  if (DeviceUtils.isTablet(mContext)) {
                      *//*  ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        mPlayer.resumePreviousOrientaionTimer();*//*
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mPlayer != null) {
                                        mPlayer.setMinimized(true);
                                    }
                                    minimizePlayer();
                                    hideToobar();
                                }
                            }, 300);
                            return true;
                        } else {*/
                            /*  ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        mPlayer.resumePreviousOrientaionTimer();*/
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mPlayer != null) {
                                        mPlayer.setMinimized(true);
                                    }
                                    minimizePlayer();
                                    hideToobar();

                                }
                            }, 100);
                            return true;
                  /*  }*/
                    } else {
                        ((BaseActivity) mContext).setOrientation(ActivityInfo
                                .SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        mPlayer.resumePreviousOrientaionTimer();
                    }
                }
//                }
                mPlayer.setFullScreen(!mPlayer.isFullScreen());

                return true;
            }

            if (isTimeShiftHelpScreenShown) {
                isTimeShiftHelpScreenShown = !isTimeShiftHelpScreenShown;
                mPlayer.setMinimized(false);
                ((MainActivity) mBaseActivity).hideTimeShiftHelpScreen();
                return true;
            }
            if (((MainActivity) mContext).getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_USER || ((MainActivity) mContext).getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
                    Log.d(TAG, "onBackClicked: SCREEN_ORIENTATION_USER ");
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                } else {
                    Log.d(TAG, "onBackClicked: SCREEN_ORIENTATION_PORTRAIT ");
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
               /* if (!DeviceUtils.isTablet(mContext)) {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }*/

            }else
                Log.d(TAG, "onBackClicked: onBackClicked ");

            Util.debugLog("mContentType: " + mContentType);
            if (mPlayer != null && mPlayer.isMediaPlaying()  && !(PrefUtils.getInstance().getSubscriptionStatusString()!=null && PrefUtils.getInstance().getSubscriptionStatusString().equalsIgnoreCase(APIConstants.USER_NOT_SUBSCRIBED)) && mDraggablePanel.isMaximized()) {
//                onViewChanged(true);
                minimizePlayer();
//                closePlayerFragment();
                mPlayer.mThumbnailPlay.setVisibility(View.GONE);
                hideToobar();
                return true;
            }

//			if(isMinimized){
//				return false;
//			}

          /*  if (mPlayer.isMediaPlaying() || mPlayer.isPlayerInitialized()) {
                mPlayer.stopMOUTracking();
                mPlayer.onStateChanged(PlayerListener.STATE_PAUSED, mPlayer.getStopPosition());
                mPlayer.closePlayer();
//                return isMinimized ? false : true;
            }*/
            hideToobar();
            closePlayerFragment();
            if (mCardData != null
                    && mCardData.generalInfo != null
                    && mCardData.generalInfo.title != null) {
                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)) {
                    Analytics.gaBrowseProgramDetails(mCardData.generalInfo.title);
                }
                if (APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(mCardData.generalInfo.type)) {
                    String subgenre = null;
                    if (mCardData.content != null
                            && mCardData.content.genre != null
                            && mCardData.content.genre.size() > 0) {
                        subgenre = mCardData.content.genre.get(0).name;
                    }
                    Analytics.gaPlayedVideoTimeCalculationForYoutube(Analytics.ACTION_TYPES.play.name(), mCardData.generalInfo.title, mCardData._id, subgenre, (mCardData == null || mCardData.publishingHouse == null) ? APIConstants.NOT_AVAILABLE : mCardData.publishingHouse.publishingHouseName);
                }
            }
            if (mBaseActivity != null) {
                ((MainActivity) mBaseActivity).enableNavigation();
            }
            Analytics.setVODSuperCardData(null);
            Analytics.setVideosCarouselName(null);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }


    @Override
    public void playerStatusUpdate(String value) {
        Log.d(TAG, "playerStatusUpdate(): " + ""+value);
        if (mFragmentCardDetailsDescription != null) {
            mFragmentCardDetailsDescription.playerStatusUpdate(value);
        }
    }


    public void minimizePlayer() {
        isMiniMized = true;
        if (mDraggablePanel == null) return;
        mDraggablePanel.post(new Runnable() {
            @Override
            public void run() {
                mDraggablePanel.minimize();
            }
        });
    }

    private void maximizePlayer() {
        if (mDraggablePanel == null) {
            return;
        }
        mDraggablePanel.maximize();
    }

    public void enableDraggablePanel() {
        if (mDraggablePanel == null) {
            return;
        }
        mDraggablePanel.setDisableDraggableViewOnTouch(false);
        mDraggablePanel.setDraggableViewEnabled(true);
    }

    public void disableDraggablePanel() {
        if (mDraggablePanel == null) {
            return;
        }
        mDraggablePanel.setDisableDraggableViewOnTouch(true);
        mDraggablePanel.setDraggableViewEnabled(false);
    }

    @Override
    public void onViewChanged(final boolean isMinimized) {


        if (rootView == null) {
            return;
        }

       /* Util.debugLog("onViewChanged() mPlayer.getHeight() " + mPlayer.getHeight());
        View view = rootView.findViewById(R.id.draggable_view);

        if (isMinimized) {

            if (mPlayer != null)
                mPlayer.minimize();
            view.getLayoutParams().height = mPlayer.getHeight();
            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            view.requestLayout();

            mBaseActivity.showActionBar();
            return;
        }

        view.getLayoutParams().height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        if (mPlayer != null)
            mPlayer.maximize();
        mBaseActivity.hideActionBar();*/

    }

    private void hideSystemUI() {
      /*  ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
*/
    }

    private void showSystemUI() {
        // ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ((MainActivity) mBaseActivity).showSystemUI();
    }

    @SuppressLint("CommitTransaction")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        String duration = PrefUtils.getInstance().getMaturityTimer();
        if (mPlayer != null && mPlayer.ageRatingLayout != null && mPlayer.ageRatingLayoutPortrait != null) {
            mPlayer.ageRatingLayout.setVisibility(View.GONE);
            mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(duration)) {
                String[] maturyData = duration.split(",");
                String timer = maturyData[0];
                String isPortrait = maturyData[1];
                if (timer != null && !timer.isEmpty() && Integer.parseInt(timer) != 0) {
                    if (mCardData != null && mCardData.content != null && !TextUtils.isEmpty(mCardData.content.categoryType)) {
                        if (!isAgeRatingVisible) {
                            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                if (mPlayer != null && mPlayer.mIsAdDisplayed){
                                    mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                    mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                }else {
                                    mPlayer.ageRatingLayout.setVisibility(View.VISIBLE);
                                    mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                }
                            } else {
                                if (isPortrait.equalsIgnoreCase("true") && mPlayer.isMinimized()) {
                                    if (mPlayer != null && mPlayer.mIsAdDisplayed){
                                        mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                        mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                    }else {
                                        mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                        mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                    }
                                } else {
                                    mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                    mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                }

                            }
                        }
                    }
                }
            }
        }

        SDKLogger.debug("MiniCardVideoPlayer: onConfigurationChanged(): " + newConfig.orientation + System.currentTimeMillis());
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /*if (mBottomActionBar != null) {
                mBottomActionBar.setVisibility(View.INVISIBLE);
			}*/
            if (mPlayer != null && !mPlayer.isYouTubePlayerLaunched()) {
                // commented the below as it is giving the error when we play the content in landscape
              /*  if (mDraggablePanel.isMinimized()) {
                    maximizePlayer();
                }*/
                hideSystemUI();
                mPlayer.playInLandscape();
                playInLandScape();
            /*    if (mPlayer != null && mPlayer.isMediaPlaying())
                    showGestureTip();*/
            }
//            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            if (mPlayer != null && !mPlayer.isYouTubePlayerLaunched()) {

                showSystemUI();
                // ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(0);
                mPlayer.playInPortrait();
                playInPortrait();
                if (mDraggablePanel != null)
                    mDraggablePanel.setTopFragmentResize(true);
//                mDraggablePanel.requestLayout();
//                mFragmentCardDetailsDescription.getView().requestLayout();
//                TODO needs optimizied fix for this issue
//                if (mDraggablePanel != null)
//                    mDraggablePanel.forceLayoutUpdate();
                showToolbar();
                ((MainActivity) mBaseActivity).hideGestureTips();
            }
          //  ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        }
        super.onConfigurationChanged(newConfig);
    }

    private void playInPortrait() {
        enableDraggablePanel();
        int topViewHeight = (int)getHeight(ApplicationController.getApplicationConfig().screenWidth);
        /*if (mCardData!=null&&mCardData.isMovie()){
            topViewHeight=(int) (((ApplicationController.getApplicationConfig().screenWidth * 4) / 3));
        }*/
        if (rootView != null /*&& !DeviceUtils.isTablet(mContext)*/) {
            rootView.forceLayout();
            rootView.requestLayout();
            rootView.invalidate();
            ViewCompat.postInvalidateOnAnimation(rootView);
            if (rootView.getLayoutParams() != null) {
                SDKLogger.debug("rootView: height- " + rootView.getLayoutParams().height + " width- " + rootView.getLayoutParams().width);
                rootView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                rootView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            if (videoLayout!= null) {
                videoLayout.getLayoutParams().height =   getHeight(ApplicationController.getApplicationConfig().screenWidth);
                videoLayout.getLayoutParams().width =   ApplicationController.getApplicationConfig().screenWidth;
            }
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ApplicationController.getApplicationConfig().screenWidth,   getHeight(ApplicationController.getApplicationConfig().screenWidth));
            if(mPlayer != null) {
                if (mPlayer.imageFrameOverlay != null) {
                    layoutParams.setMargins(0, 0, 0, 0);
                    mPlayer.imageFrameOverlay.setLayoutParams(layoutParams);
                }
                if (mPlayer.mProgressBarLayout != null) {
                    layoutParams.setMargins(0, 0, 0, 0);
                    mPlayer.mProgressBarLayout.setLayoutParams(layoutParams);
                }
                if (mPlayer.mAdContainer != null) {
                    layoutParams.setMargins(0, 0, 0, 0);
                    mPlayer.mAdContainer.setLayoutParams(layoutParams);
                }
            }
            rootView.forceLayout();
            rootView.requestLayout();
            rootView.invalidate();
            ViewCompat.postInvalidateOnAnimation(rootView);
        }
        if (mDraggablePanel == null) {
            return;
        }
        if (mPlayer != null && mPlayer.isMediaPlaying()) {
            ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.app_bkg));
        }
        if(mDraggablePanel!=null) {
            mDraggablePanel.setTopViewHeight(topViewHeight);
            mDraggablePanel.setBottomFragmentHeight(ApplicationController.getApplicationConfig().screenHeight - topViewHeight);
        }
        if (isToShowToolBar) {
            topViewHeight = (int) (topViewHeight + mContext.getResources().getDimension(R.dimen.action_bar_height));
            if (mDraggablePanel != null) {
                mDraggablePanel.setTopViewHeight(topViewHeight);
                mDraggablePanel.ensureTopViewWidth();
            }
        }
        mDraggablePanel.setFullScreen(false);
        mDraggablePanel.setTopFragmentResize(true);
        if (mPlayer != null && mPlayer.isAdPlaying() && !mPlayer.isYouTubePlayerPlaying()) {
            enableDraggablePanel();
        }
      /*  if (mPlayer != null
                && APIConstants.isHooqContent(mCardData)
                && (mPlayer.getCurrentState() == PLAYER_INITIATED
                || mPlayer.getCurrentState() == PLAYER_STOPPED)) {
            disableDraggablePanel();
        }*/
        try {
            if(mFragmentCardDetailsDescription!=null){
                mFragmentCardDetailsDescription.notifySeriesListHighlighter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDraggablePanel.requestLayout();
    }

    private void playInLandScape() {
        disableDraggablePanel();
        if(mDraggablePanel!=null && mPlayer != null) {
            mDraggablePanel.setTopViewHeight(mPlayer.getHeight());
        }
        if (rootView != null /*&& !DeviceUtils.isTablet(mContext)*/) {
            if (DeviceUtils.isTablet(mContext)) {
                rootView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenWidth;
                rootView.getLayoutParams().height = ApplicationController.getApplicationConfig().screenHeight + softNavigationBarHeight;
                if (videoLayout!= null && videoLayout != null) {
                    videoLayout.getLayoutParams().width =  ApplicationController.getApplicationConfig().screenWidth;
                    videoLayout.getLayoutParams().height =   ApplicationController.getApplicationConfig().screenHeight + softNavigationBarHeight;
                }
            } else {
                rootView.getLayoutParams().width = ApplicationController.getApplicationConfig().screenHeight + softNavigationBarHeight;
                rootView.getLayoutParams().height = ApplicationController.getApplicationConfig().screenWidth;
                if (videoLayout!= null && videoLayout != null) {
                    videoLayout.getLayoutParams().height =  ApplicationController.getApplicationConfig().screenWidth;
                    videoLayout.getLayoutParams().width =   ApplicationController.getApplicationConfig().screenHeight + softNavigationBarHeight;
                }
            }
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(videoLayout.getLayoutParams().width,   videoLayout.getLayoutParams().height);
            if(mPlayer != null) {
                if (mPlayer.imageFrameOverlay != null) {
                    layoutParams.setMargins(0, 0, 0, 0);
                    mPlayer.imageFrameOverlay.setLayoutParams(layoutParams);
                }
                if (mPlayer.mProgressBarLayout != null) {
                    layoutParams.setMargins(0, 0, 0, 0);
                    mPlayer.mProgressBarLayout.setLayoutParams(layoutParams);
                }
                if (mPlayer.mAdContainer != null) {
                    layoutParams.setMargins(0, 0, 0, 0);
                    mPlayer.mAdContainer.setLayoutParams(layoutParams);
                }
            }
            SDKLogger.debug("rootView: height- " + rootView.getLayoutParams().height + " width- " + rootView.getLayoutParams().width);
            rootView.forceLayout();
            rootView.requestLayout();
            rootView.invalidate();
            ViewCompat.postInvalidateOnAnimation(rootView);
            rootView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rootView.forceLayout();
                    rootView.requestLayout();
                    rootView.invalidate();
                    ViewCompat.postInvalidateOnAnimation(rootView);
                }
            }, 3000);
        }
        mDraggablePanel.setBottomFragmentHeight(0);
        mDraggablePanel.setFullScreen(true);
        mDraggablePanel.setTopFragmentResize(true);
//        LoggerD.debugLog("playInLandscape() FCDP mWidth = " + mPlayer.getWidth() + " * mHeight = " + mPlayer.getHeight());
        disableDraggablePanel();
        int statusBarHeight = Util.getStatusBarHeight(mContext);
        FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) playLayout.getLayoutParams();
        layoutParams1.topMargin = statusBarHeight;
        playLayout.setLayoutParams(layoutParams1);
        hideToobar();
    }

    private void showToolbar() {
        if (mToolbar == null || !isToShowToolBar || mPlayer.isFullScreen() || mDraggablePanel.isMinimized()) {
            return;
        }
        mToolbar.setVisibility(View.VISIBLE);
    }

    private void hideToobar() {
        if (mToolbar == null) {
            return;
        }
        mToolbar.setVisibility(View.GONE);
    }

    @Override
    public void OnCacheResults(List<CardData> dataList) {
        if (null == dataList) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
            return;
        }
        for (CardData cardData : dataList) {

            if (!isResponseMatches(cardData)) {
                continue;
            }
            mCardData = cardData;
            if (relatedMultimedia != null) {
                mCardData.relatedMultimedia = relatedMultimedia;
            }
            if (cardDataCurrentUserData != null) {
                mCardData.currentUserData = cardDataCurrentUserData;
            }
            if (cardDataPackages != null) {
                mCardData.packages = cardDataPackages;
            }
            if (cardDataTags != null){
                mCardData.tags = cardDataTags;
            }
            if (mCardData != null
                    && (mCardData.isTVEpisode()
                    || mCardData.isTVSeries()
                    || mCardData.isVODYoutubeChannel()
                    || mCardData.isTVEpisode()
                    || mCardData.isTVSeason()
                    || mCardData.isVODChannel()
                    || mCardData.isVODCategory())) {
                mTVShowCardData = mCardData;
                mAutoPlay = false;
            }

            if (null != mPlayer) {
                mPlayer.updateCardPreviewImage(mCardData);
            }
            if (mFragmentCardDetailsDescription != null) {
                mFragmentCardDetailsDescription.setTVShowCardData(mTVShowCardData);
                mFragmentCardDetailsDescription.setTVSeasonName(mSeasonName);
                mFragmentCardDetailsDescription.onDataLoaded(cardData, mContentType, mKeepDescriptionViewsAndUpdateData);
            }
            LoggerD.debugDownload("mNid- " + mNid);
            LoggerD.debugDownload("is free content- " + Util.isFreeContent(mCardData));
            if (!Util.isFreeContent(mCardData) && TextUtils.isEmpty(mNid)) {
                LoggerD.debugDownload("Disabling AutoPlay- " + mAutoPlay);
                mAutoPlay = false;
            }
            mAutoPlay = true;
            //call description fragments fill data or updated its own card data
            if ((mAutoPlay || mCardData.isLive() || mCardData.isNewsContent()) && mPlayer != null/* && !isLiveOrProgram()*/) {
                mAutoPlay = false;
                if (mCardData.playFullScreen && !checkParentalControlEnabled(mCardData)) {
                    if (isFullScreenRequired(mCardData)) {
                        ((BaseActivity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }

                }
                mPlayer.playContent();
            } else {
                mAutoPlay = false;
                if (mCardData.playFullScreen && mPlayer != null) {
                    if (!checkParentalControlEnabled(mCardData))
                        if (isFullScreenRequired(mCardData)) {
                            ((BaseActivity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                    mPlayer.playContent();
                }


            }

            initToolbar();
            if (autoPlayMinimize) {
                mDraggablePanel.post(new Runnable() {
                    @Override
                    public void run() {
                        hideToobar();
                    }
                });
            }
            if (mPlayer != null) {
                mPlayer.isToolbarShown(isToShowToolBar);
            }
            break;
        }
//        epgChannelsListApiCall();

    }


    private boolean checkParentalControlEnabled(CardData mData) {
        switch (PrefUtils.getInstance().getPrefParentalControlOpt()) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return Util.isAdultContent(mData);
            default:
                return false;
        }
    }

    private boolean isResponseMatches(CardData cardData) {
        if (mId == null) {
            return false;
        }
        String contentId = cardData._id;
        if (cardData != null
                && cardData.generalInfo != null
                && cardData.generalInfo.type != null) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)) {
                contentId = cardData.globalServiceId;
            }
        }

        if (contentId != null && contentId.equalsIgnoreCase(mId)) {
            return true;
        }
        return false;
    }

    @Override
    public void OnOnlineResults(List<CardData> dataList) {
        if (null == dataList) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
            return;
        }
        for (CardData cardData : dataList) {
            if (!isResponseMatches(cardData)) {
                continue;
            }
            mCardData = cardData;
            if (relatedMultimedia != null) {
                mCardData.relatedMultimedia = relatedMultimedia;
            }
            if (cardDataCurrentUserData != null) {
                mCardData.currentUserData = cardDataCurrentUserData;
            }
            if (mCardData != null
                    && (mCardData.isTVEpisode()
                    || mCardData.isTVSeries()
                    || mCardData.isVODYoutubeChannel()
                    || mCardData.isTVSeason()
                    || mCardData.isVODChannel()
                    || mCardData.isVODCategory())) {
                mTVShowCardData = mCardData;
                mAutoPlay = false;
            }

            if (null != mPlayer) {
                mPlayer.updateCardPreviewImage(mCardData);
            }
            if (mFragmentCardDetailsDescription != null) {
                mFragmentCardDetailsDescription.setTVShowCardData(mTVShowCardData);
                mFragmentCardDetailsDescription.setTVSeasonName(mSeasonName);
                mFragmentCardDetailsDescription.onDataLoaded(cardData, mContentType, mKeepDescriptionViewsAndUpdateData);
            }

            if (!Util.isFreeContent(mCardData) && TextUtils.isEmpty(mNid)) {
                mAutoPlay = false;
            }
            mAutoPlay = true;
            //call description fragments fill data or updated its own card data
            if (mAutoPlay && mPlayer != null/* && !isLiveOrProgram()*/) {
                mAutoPlay = false;
                if (mCardData.playFullScreen) {
                    if (isFullScreenRequired(mCardData)) {
                        ((BaseActivity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }

                }
                mPlayer.playContent();
            } else {
                mAutoPlay = false;
                if (mCardData.playFullScreen && mPlayer != null) {
                    if (isFullScreenRequired(mCardData)) {
                        ((BaseActivity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    mPlayer.playContent();
                }


            }
            initToolbar();
            if (autoPlayMinimize) {
                mDraggablePanel.post(new Runnable() {
                    @Override
                    public void run() {
                        hideToobar();
                    }
                });
            }
            if (mPlayer != null) {
                mPlayer.isToolbarShown(isToShowToolBar);
            }
            break;
        }

    }

    @Override
    public void OnOnlineError(Throwable error, int errorCode) {
        if (errorCode == APIRequest.ERR_NO_NETWORK) {
            AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
                    "", mContext.getString(R.string.play_button_retry), mNeutralDialogListener);
            return;
        }
        AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
    }

    public void closePlayerFragment() {
        if (mBaseActivity == null) {
            return;
        }
        if(mDraggablePanel != null && mDraggablePanel.draggableView!=null)
            mDraggablePanel.draggableView.closeToLeft();
        ((MainActivity) mBaseActivity).closePlayerFragment();
    }

    private void hideMediaController() {
        if (mPlayer != null) {
            mPlayer.hideMediaController();
        }
    }

    //time analytics
    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null && mPlayer.mIsAdPlaying && mCardData != null){
            PlayerStreamingAnalytics.getInstance().notifyPaused(mCardData,true,mPlayer.adEventEllapsedTime);
        }
        Util.debugLog("CardDetails: onPause");
        if(mPlayer.mPlayerState != 4) {
            isFromBackground = true;
        }
        if (mPlayer != null && (mPlayer.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE |mPlayer.getScreenOrientation() == SCREEN_ORIENTATION_LANDSCAPE) && mPlayer.isMediaPlaying() && (mDraggablePanel!=null && !mDraggablePanel.isMinimized())) {
            orientation = "landscape";
        } else {
            orientation = "portrait";
        }
        if(mPlayer.mPlayerState == 99)
            isFromWaitRetry = true;
        isVisible = false;
        if (mPlayer != null) {
            duringAd = mPlayer.isAdPausedWhilePlaying();
//            mPlayer.removeHandlerCallbacksIfAny();
            Util.debugLog("CardDetails:onPause: is playing " + mPlayer.isMediaPlaying());
            if (mPlayer.isMediaPlaying()) {
                mPlayer.stopMOUTracking();
                mPlayer.onStateChanged(PlayerListener.STATE_PAUSED, mPlayer.getStopPosition());
                mPlayer.onPause();
                //   isFromBackground = true;
                if (isTimeShiftHelpScreenShown) {
                    isTimeShiftHelpScreenShown = !isTimeShiftHelpScreenShown;
                    mPlayer.setMinimized(false);
                    ((MainActivity) mBaseActivity).hideTimeShiftHelpScreen();
                }
            } else if (mPlayer.isPlayerInitialized() && !duringAd) {
                mPlayer.closePlayer();
            }
            if (duringAd) {
                mPlayer.onPause();
            }
            mPlayer.pauseYoutubePlayer();
           /* PowerManager powerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                APIConstants.IS_PLAYER_SCREEN_LOCKED = powerManager.isInteractive();
            } else {
                APIConstants.IS_PLAYER_SCREEN_LOCKED = powerManager.isScreenOn();
            }*/
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.onDestroy();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
            YuppExoAnalyticsInterface.getInstance(mContext).cleanup();
            YuppExoAnalyticsInterface.getInstance(mContext).releaseYuppExoAnalyticsInterface();
            YuppAnalytics.getInstance(mContext).releaseUSAnalytics();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.debugLog("CardDetails: onResume");

            if (((MainActivity) mContext) != null && ((MainActivity) mContext).isOpenDrawer() && mPlayer != null && mPlayer.isMinimized() && mPlayer.isMediaPlaying()) {
                if(mPlayer!=null && mPlayer.mPlayerState!=1 ) {
                ((MainActivity) mContext).closeDrawer();
                //  return;
            }
        }
        if(mPlayer!=null) {
            if (!mPlayer.isMinimized() && (mPlayer.isMediaPlaying() || mPlayer.isMediaPaused())) {
                ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.app_bkg));
            }
        }
        isVisible = true;
        PowerManager powerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);

        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            APIConstants.IS_PLAYER_SCREEN_LOCKED = powerManager.isInteractive();
        } else {
            APIConstants.IS_PLAYER_SCREEN_LOCKED = powerManager.isScreenOn();
        }*/
        if (mPlayer == null || (mDraggablePanel != null && mDraggablePanel.getVisibility() == View.GONE)) {
            return;
        }
      /*  if (duringAd) {
            mPlayer.returnFromClickThrough();
            mPlayer.onResume();
            return;
        }*/
        if(mDraggablePanel != null && mDraggablePanel.isMinimized()) {
                mDraggablePanel.minimize();
        }
        if(isFromWaitRetry){
          /*  if (mCardData != null && mCardData.generalInfo != null && mCardData.generalInfo.type!= null && mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    || mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
                ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_LANDSCAPE);
            }
            else
                ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_PORTRAIT);*/
            isFromWaitRetry = false;
            return;
        }
       /* if (APIConstants.IS_PLAYER_SCREEN_LOCKED) {
           if (!mPlayer.isMediaPlaying()) {
               mPlayer.onPause();
               return;
           }
        }*/
        if (!mPlayer.isMediaPlaying()) {
            //  mPlayer.playContent();
            Util.debugLog("CardDetails:onResume Media is not playing ");
            mPlayer.onResume();
            if(isFromBackground) {
          /*  if(mCardData!=null ) {
                if ( mCardData.generalInfo != null && mCardData.generalInfo.type != null && mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                        || mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
                    ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
            else
                ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_PORTRAIT);*/
                if (mPlayer != null && mPlayer.chromeCastPopup) {
                    orientation = "portrait";
                }
                if (orientation != null && orientation.equalsIgnoreCase("landscape")) {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo
                            .SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else {

                    if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
                        mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    } else {
                        mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    //((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                isFromBackground = false;
            }
            if(DeviceUtils.isTablet(mContext)){
                if(mPlayer != null && mPlayer.chromeCastPopup && isFromBackground) {
//                    mPlayer.chromeCastBack.setVisibility(VISIBLE);
                    mPlayer.mThumbnailPlay.setVisibility(VISIBLE);
                }else{
                    mPlayer.chromeCastBack.setVisibility(View.GONE);
                    disableDraggablePanel();
                }
            }else {
            if (mPlayer != null && mPlayer.chromeCastPopup == true) {
                mPlayer.chromeCastBack.setVisibility(VISIBLE);
                mPlayer.mBackIconImageView.setVisibility(View.GONE);
                mPlayer.mThumbnailPlay.setVisibility(VISIBLE);
            }else{
                disableDraggablePanel();
            }
            }
            if (mDraggablePanel != null && mDraggablePanel.isMinimized()) {
                //   ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                if (mPlayer != null) {
                    mPlayer.setMinimized(true);
                    mPlayer.resumePreviousOrientaionTimer();
                }
            }
        }
        mAutoPlay = true;
        if (mAutoPlay && !mPlayer.isYouTubePlayerLaunched()&& !mPlayer.isLaunchDeeplink) {
            if(DeviceUtils.isTablet(mContext)){
                if(mPlayer != null && mPlayer.chromeCastPopup) {
                    mPlayer.chromeCastBack.setVisibility(VISIBLE);
                    return;
                }else{
                    if(mPlayer != null && !mPlayer.chromeCastPopup) {
                        return;
                    }else {
                        mPlayer.playContent();
                    }
                }
            }
        }
    }

    public void updateData(Bundle args) {
        if (args == null) {
            Util.debugLog("args are null");
            return;
        }
        ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.app_bkg));
        EventBus.getDefault().post(new MediaPageVisibilityEvent(true));
        if (isPlayingSameContent(args)) {
            boolean autoPlayInMinimized = false;
            if (args.containsKey(CardDetails.PARAM_AUTO_PLAY_MINIMIZED)) {
                autoPlayInMinimized = args.getBoolean(CardDetails.PARAM_AUTO_PLAY_MINIMIZED);
            }
            if (!autoPlayInMinimized) {
                maximizePlayer();
            }
            LoggerD.debugLog("Media is already playing return");
            return;
        }
        this.mArguments = args;
        initializeBundleValues(args);
        getCOntentDetailMultimedia();
        /*initializePlayerView();
        fetchCardData();*/
    }

    private boolean isPlayingSameContent(Bundle args) {
        String _id = null;
        if (args.containsKey(CardDetails.PARAM_CARD_ID)) {
            _id = args.getString(CardDetails.PARAM_CARD_ID);
        }
        boolean autoPlayInMinimized = false;
        boolean isMediaPlaying = false;
        boolean isSameContentId = false;
        boolean isAutoPlay = false;
        boolean isCatchup = false;
        if (args.containsKey(CardDetails.PARAM_AUTO_PLAY_MINIMIZED)) {
            autoPlayInMinimized = args.getBoolean(CardDetails.PARAM_AUTO_PLAY_MINIMIZED);
        }
        if (args.containsKey("position")) {
            selectedPosition = args.getInt("position");
        }
        if (args.containsKey(CatchupItem.IS_CATCH_UP)) {
            isCatchup = args.getBoolean(CatchupItem.IS_CATCH_UP);
            //Fixed the catchup content not playing when clicked on the catchup content from catchup see all page
            if(isCatchup) {
                mIsSupportCatchup = true;
            }else{
                mIsSupportCatchup = false;
            }
        } else {
            mIsSupportCatchup = false;
        }
        if(args.containsKey("isLiveNow")) {
            if(args.getBoolean("isLiveNow")){
                return  false;
            }
        }
        if(isCatchup)
            return false;
        if (args.containsKey(CardDetails.PARAM_AUTO_PLAY)) {
            isAutoPlay = args.getBoolean(CardDetails.PARAM_AUTO_PLAY);
        }
        isMediaPlaying = mPlayer != null && mPlayer.isPlayerInitialized();
        isSameContentId = mId != null && mId.equalsIgnoreCase(_id);
        if (isMediaPlaying) {
            if (autoPlayInMinimized || (isSameContentId && isAutoPlay)) {
                return true;
            }
        }

        return false;
    }

    public void setPlayerDescriptionListener(FragmentCardDetailsDescription fragmentCardDetailsDescription) {
        this.mFragmentCardDetailsDescription = fragmentCardDetailsDescription;
    }

    public void setDraggablePanel(DraggablePanel mDraggablePanel) {
        this.mDraggablePanel = mDraggablePanel;
        if (mDraggablePanel != null) {
            mDraggablePanel.setDraggableViewCallbackListener(mDraggableViewCallbackListener);
        }
    }

    public void showMediaController() {
        if (mPlayer != null) {
            mPlayer.showMediaController();
        }
    }

    public void resumePreviousOrientaionTimer() {
        if (mPlayer != null) {
            mPlayer.resumePreviousOrientaionTimer();
        }
    }

    public void setShowingHelpScreen(boolean b) {
        if (mPlayer != null) {
            mPlayer.setShowingHelpScreen(b);
        }
    }

    @Override
    public void onClosePlayer() {
        closeMaturityData();

        maximizePlayer();
        disableDraggablePanel();
    }

    @Override
    public void onPlayerStarted(boolean isAd) {
        LoggerD.debugLog("onPlayerStarted isAd- " + isAd);
        if (mDraggablePanel.getVisibility() != View.VISIBLE) {
            if (mPlayer != null && mPlayer.isMediaPlaying()) {
                mPlayer.closePlayer();
                return;
            }
        }

      /*  if (isAd) {
//            if (mDraggablePanel.isMinimized()) {
            maximizePlayer();
//            disableDraggablePanel();
//            }
            return;
        }*/
        if (mPlayer != null && !mPlayer.isFullScreen()) {
            if (!mDraggablePanel.isMinimized()) {
                mPlayer.allowMediaController(true);
                enableDraggablePanel();
            }

            mDraggablePanel.setPlayerSeekbarView(mPlayer.getPlayerSeekBarView());

        }

        /*if (mPlayer != null && mPlayer.isFullScreen())
            showGestureTip();*/

        showDVRHelpScreen();


        String duration = PrefUtils.getInstance().getMaturityTimer();
        if (mPlayer != null && mPlayer.ageRatingLayout != null && mPlayer.ageRatingLayoutPortrait != null) {
            mPlayer.ageRatingLayout.setVisibility(View.GONE);
            mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);

            ////Log.d(TAG, ":::" + duration + "rating :: " + isAgeRatingVisible + "handler:::" + isAgeHandler + "category::::" + mCardData.content.categoryType);
            if (!TextUtils.isEmpty(duration)) {
                String[] maturyData = duration.split(",");
                String timer = maturyData[0];
                String isPortrait = maturyData[1];
                String ratedText = maturyData[2];

                if (timer != null && !timer.isEmpty() && Integer.parseInt(timer) != 0) {
                    if (mCardData != null && mCardData.content != null && !TextUtils.isEmpty(mCardData.content.categoryType)) {
                        if (!isAgeRatingVisible) {
                            try {
                                mPlayer.ageNumberTv.setText(mCardData.content.categoryType.toUpperCase());
                                mPlayer.ageNumberTvP.setText(mCardData.content.categoryType.toUpperCase());
                                String catName = mCardData.content.categoryName;
                                String output = catName.substring(0, 1).toUpperCase() + catName.substring(1);
                                mPlayer.ageDescriptionTv.setText(output);
                                mPlayer.ageDescriptionTvP.setText(output);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (mPlayer != null && mPlayer.isFullScreen()) {
                                mPlayer.ageRatingLayout.setVisibility(View.VISIBLE);
                                mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                            } else {
                                if (isPortrait.equalsIgnoreCase("true") && !mPlayer.isMinimized()) {
                                    mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                    mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                } else {
                                    mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                    mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                }
                            }

                            if (!isAgeHandler) {
                                isAgeHandler = true;
                                if (timerHandler != null) {
                                    timerHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            isAgeRatingVisible = true;
                                            if (mPlayer != null && mPlayer.ageRatingLayout != null && mPlayer.ageRatingLayoutPortrait != null) {
                                                mPlayer.ageRatingLayout.setVisibility(View.GONE);
                                                mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
                                            }
                                        }
                                    }, Integer.parseInt(timer) * 1000);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void showGestureTip() {
        LoggerD.debugLog("shownCountOfTimeShiftHelp: ");
        if (!PrefUtils.getInstance().getPrefIsGestureTipShown()) {
            //Log.v(TAG, "showing help screen");
            PrefUtils.getInstance().setPrefIsGestureTipShown(true);
            mPlayer.allowMediaController(true);
            mPlayer.setShowingHelpScreen(true);
            mPlayer.showMediaController();
            mPlayer.setMinimized(true);
            ((MainActivity) mBaseActivity).showGestureTip();
        }
    }

    private void showDVRHelpScreen() {

        if (!ApplicationController.ENABLE_HELP_SCREEN) {
            return;
        }
        int shownCountOfTimeShiftHelp = PrefUtils.getInstance().getPrefShownCountTimeShiftHelp();
        int maxCountOfTimeShiftHelp = PrefUtils.getInstance().getPrefMaxDisplayCountTimeShift();
        //Log.v(TAG, "shownCountOfTimeShiftHelp: " + shownCountOfTimeShiftHelp + " maxCountOfTimeShiftHelp: " + maxCountOfTimeShiftHelp + "isTimeShiftHelpScreenShown: " + isTimeShiftHelpScreenShown);
        if (shownCountOfTimeShiftHelp < maxCountOfTimeShiftHelp
                && !isTimeShiftHelpScreenShown
                && mPlayer != null && mPlayer.isPlayingDVR()
                && !mDraggablePanel.isMinimized()
                && (!isPlayingDirectlyInMiniPlayer)) {

            //Log.v(TAG, "showing help screen");
            PrefUtils.getInstance().setPrefShownCountTimeShiftHelp(++shownCountOfTimeShiftHelp);
            isTimeShiftHelpScreenShown = true;
            mPlayer.allowMediaController(true);
            mPlayer.setShowingHelpScreen(true);
            mPlayer.showMediaController();
            mPlayer.setMinimized(true);
            ((MainActivity) mBaseActivity).showTimeShiftHelpScreen();
        }
    }

    @Override
    public void onUpdatePlayerData(CardData mData) {
        if (mData == null
                || mData.generalInfo == null
                || mData.generalInfo.type == null) {
            return;
        }
        mCardData = mData;
        if(mPlayer != null) {
            mPlayer.mData = mData;
        }
        mId = mData._id;
        mKeepDescriptionViewsAndUpdateData = true;
        if(mFragmentCardDetailsDescription != null) {
            mFragmentCardDetailsDescription.onDataLoaded(mCardData, mCardData.generalInfo.type, mKeepDescriptionViewsAndUpdateData);
        }
        initToolbar();
    }

    public interface DraggableState {
        int minimized = 1;
        int maximized = 2;
        int enable = 3;
        int disable = 4;
        int none = 0;
    }

    @Override
    public int getMiniPlayerState() {
        if (mDraggablePanel == null) {
            return DraggableState.none;
        }
        if (mDraggablePanel.isMinimized()) {
            return DraggableState.minimized;
        }
        if (mDraggablePanel.isMaximized()) {
            return DraggableState.maximized;
        }
        return DraggableState.none;

    }

    @Override
    public void onPlayerBackPressed() {
        onBackClicked();
    }

    @Override
    public void onCompleted(boolean isAd) {
        LoggerD.debugLog("onCompleted: isAd- " + isAd);
        if (isAd) {
            if (mDraggablePanel != null) {
                mDraggablePanel.removeLayoutUpdates();
                return;
            }
        }
       /* maximizePlayer();
        disableDraggablePanel();*/
    }

    @Override
    public void onDraggablePanelVisibilityChanged(boolean isVisible) {
    }

    public void queueListCardData(List<CardData> listCardData) {
        this.mListQueueCardData = listCardData;
    }

    @Override
    public boolean isFragmentVisible() {
        return isVisible && mDraggablePanel != null && mDraggablePanel.getVisibility() == View.VISIBLE;
    }

    @Override
    public void changeMiniplayerState(int state) {
        if (mDraggablePanel == null) {
            return;
        }
        switch (state) {
            case DraggableState.maximized:
                mDraggablePanel.maximize();
                break;
            case DraggableState.minimized:
                mDraggablePanel.minimize();
                break;
            case DraggableState.disable:
                disableDraggablePanel();
                break;
            case DraggableState.enable:
                enableDraggablePanel();
                break;
        }
    }

    @Override
    public void startInLandscape() {
        if (mDraggablePanel.isMinimized()) {
            maximizePlayer();
        }
        playInLandScape();
        mPlayer.playInLandscape();
        hideSystemUI();
        ((MainActivity) mBaseActivity).disableNavigation();
    }

    public void startInLandscapeWithNav() {
//        if (mDraggablePanel.isMinimized()) {
        maximizePlayer();
//        }
        playInLandScape();
        mPlayer.playInLandscape();
        hideSystemUI();
    }

    @Override
    public boolean isDragging() {
        return isDragging;
    }


    public void onEventMainThread(EventNotifyEpgAdapter event) {
        if (mPlayer != null
                && !mPlayer.isMediaPlaying()
                && !mPlayer.isPlayerInitialized()) {
            mPlayer.updateCardPreviewImage(mCardData);
        }
    }

    private boolean isLiveOrProgram() {
        if (mCardData == null
                || mCardData.generalInfo == null) {
            return false;
        }
        return (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(mCardData.generalInfo.type));
    }

    public void onEventMainThread(EventSoftUpdateData event) {
        if (event == null) return;
        CardData cardData = event.cardData;
        CardData tvShowData = event.tvShowCardData;

    }

    public void onEventMainThread(EventUpdatePlayerState event) {
        if (event != null) {
            if (event.action == EventUpdatePlayerState.ACTION_PAUSE) {
                onPause();
            } else if (event.action == EventUpdatePlayerState.ACTION_PLAY) {
                onResume();
            }
        }
    }

    public boolean isFullScreenRequired(CardData cardData) {
        if (cardData != null
                && cardData.generalInfo != null
                && (APIConstants.TYPE_LIVE.equalsIgnoreCase(cardData.generalInfo.type) || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type))) {
            return true;
        }
        if (cardData != null && cardData.generalInfo != null && !TextUtils.isEmpty(cardData.generalInfo.type)) {
            if (!TextUtils.isEmpty(PrefUtils.getInstance().getFullScreenRestrictedContentTypes())) {
                String[] contentTypeList = PrefUtils.getInstance().getFullScreenRestrictedContentTypes().split(",");
                if (contentTypeList.length > 0) {
                    for (String contentType :
                            contentTypeList) {
                        if (cardData.generalInfo.type.equalsIgnoreCase(contentType)) {
                            return false;
                        }
                    }
                }
            }
        }
        if (cardData != null && cardData.publishingHouse != null) {
            if (!TextUtils.isEmpty(PrefUtils.getInstance().getFullScreenRestrictedContentTypesPublishingHouseIds())) {
                String[] contentPublishingHouseList = PrefUtils.getInstance().getFullScreenRestrictedContentTypesPublishingHouseIds().split(",");
                if (contentPublishingHouseList.length > 0) {
                    int[] contentPublishingHouseListInt = new int[contentPublishingHouseList.length];
                    for (int i = 0; i < contentPublishingHouseList.length; i++) {
                        contentPublishingHouseListInt[i] = Integer.parseInt(contentPublishingHouseList[i]);
                    }
                    for (int publishingHouseID :
                            contentPublishingHouseListInt) {
                        if (cardData.publishingHouse.publishingHouseId == publishingHouseID) {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void getCOntentDetailMultimedia() {
        // hiding the error layout and preview image before load the content
        if(mPlayer != null && mPlayer.mErrorLayout != null ){
            mPlayer.mErrorLayout.setVisibility(View.GONE);
        }
        if(mPlayer != null && mPlayer.mPreviewImage != null ){
            mPlayer.mPreviewImage.setVisibility(View.GONE);
        }
        if(mContentType==null || (mContentType.equals(APIConstants.TYPE_PROGRAM) || mContentType.equals(APIConstants.TYPE_LIVE))){
            if (mId == null || TextUtils.isEmpty(mId)) {
                initializePlayerView();
                fetchCardData();
                return;
            }
        }


        ContentDetails.Params params = new ContentDetails.Params(mId, "mdpi", "coverposter", 10, APIConstants.HTTP_NO_CACHE,
                mContext.getResources().getString(R.string.content_details_fields));
        ContentDetails contentDetails = new ContentDetails(params, mMediaLinkFetchListener);
        APIService.getInstance().execute(contentDetails);
    }

    public void getCOntentDetailThumnailPreview() {
        PrefUtils.getInstance().setShowPreviewStatus(false);
        new DownloadImageAndVttFile(new WeakReference<Context>(mContext), vttdata, new DownloadImageAndVttFile.DownloadImageVttFileListener() {
            @Override
            public void OnDownloadFailed() {
                isPreviewInitialWorkDone = false;
                if (mPlayer != null && mPlayer.getMediaControllerInstance() != null) {
                    PrefUtils.getInstance().setShowPreviewStatus(false);
                    mPlayer.getMediaControllerInstance().setShowPreviewSeekBar(false);
                }
            }

            @Override
            public void OnDownloadSuccess(boolean showPreviewBar) {
                Log.e("PreviewNew", "OnDownloadSuccess - showPreviewBar" + "showPreviewBar");

            }

            @Override
            public void OnDownloadProgress(DownloadImageAndVttFile.STATE_OF_DOWNLOAD update) {
                Log.e("PreviewNew", "OnDownloadProgress" + update);

            }

            @Override
            public void OnParseComplete(WebVTTModule webVTTModule, boolean showPreviewBar) {
                Log.e("PreviewNew", "OnParseComplete - showPreviewBar" + showPreviewBar);
                isPreviewInitialWorkDone = showPreviewBar;
                if (mPlayer != null && mPlayer.getMediaControllerInstance() != null) {
                    if (mPlayer.exoPlayerInstance() != null){
                        ExoPlayerView exoPlayerView = mPlayer.exoPlayerInstance();
                        exoPlayerView.setPreviewInitialWorkDone(isPreviewInitialWorkDone);
                    }
                    PrefUtils.getInstance().setShowPreviewStatus(showPreviewBar);
                    mPlayer.getMediaControllerInstance().setWebVTTModule(webVTTModule);
                    mPlayer.getMediaControllerInstance().setShowPreviewSeekBar(showPreviewBar);}
            }

            @Override
            public void OnParseFailed() {
                Log.e("PreviewNew", "OnParseFailed");
                isPreviewInitialWorkDone = false;
                if (mPlayer != null && mPlayer.getMediaControllerInstance() != null) {
                    PrefUtils.getInstance().setShowPreviewStatus(false);
                    mPlayer.getMediaControllerInstance().setShowPreviewSeekBar(false);
                }
            }




            @Override
            public void ParsingDownloadComplete(final WebVTTModule webVTTModule) {
                Handler uiHandler = new Handler(Looper.getMainLooper());
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("PreviewNew", "ParsingDownloadComplete");
                        isPreviewInitialWorkDone = true;
                        if (mPlayer!=null){
                            mPlayer.isPreviewInitialWorkDone(isPreviewInitialWorkDone);
                        }
                        /*if (mPlayer != null && mPlayer.getMediaControllerInstance() != null) {
                            mPlayer.getMediaControllerInstance().setWebVTTModule(webVTTModule);
                            mPlayer.getMediaControllerInstance().setShowPreviewSeekBar(true);
                        }*/
                    }
                });

            }
        });

    }

    CardDataRelatedMultimedia relatedMultimedia = null;
    CardDataCurrentUserData cardDataCurrentUserData = null;
    List<CardDataPackages> cardDataPackages = null;
    CardDataTags cardDataTags = null;
    private APICallback mMediaLinkFetchListener = new APICallback<CardResponseData>() {


        public int mAPIErrorCode;

        @Override
        public void onResponse(APIResponse<CardResponseData> response) {

            //Log.d(TAG, "onResponse ");
            if (null == response || response.body() == null) {
                onFailure(new Throwable("Empty response or response body"), APIRequest.ERR_UN_KNOWN);
                initializePlayerView();
                fetchCardData();
                return;
            }
            mAPIErrorCode = response.body().code;

            if (response.body().results == null
                    || response.body().results.size() == 0) {
                onFailure(new Throwable("Invalid or empty results"), APIRequest.ERR_UN_KNOWN);
                initializePlayerView();
                fetchCardData();
                return;
            }

            //Log.d(TAG, "success: message- " + response.body().message);


            for (CardData data : response.body().results) {
                if (data != null && data.relatedMultimedia != null) {
                    relatedMultimedia = data.relatedMultimedia;
                    mCacheManager.setSelectedCardData(mCardData);
                }
                if (data != null && data.currentUserData != null) {
                    cardDataCurrentUserData = data.currentUserData;
                    mCacheManager.setSelectedCardData(mCardData);
                }
                //Audio languages not displaying in meta data fix
                if (mCardData!=null && mCardData.content!=null && mCardData.content.audioLanguage!=null
                        && data != null && data.content != null && data.content.audioLanguage != null) {
                    mCardData.content.audioLanguage = data.content.audioLanguage;
                }

                if (data != null && data.packages != null) {
                    cardDataPackages = data.packages;
                }
                if (data != null && data.thumbnailSeekPreview != null) {
                    vttdata = data;
                    if (PrefUtils.getInstance().getIsShowThumbnailPreview()) {
                        getCOntentDetailThumnailPreview();
                    }
                }

                if (data != null && data.tags != null && data.tags.values != null && data.tags.values.size() > 0){
                    cardDataTags = data.tags;
                }

                if (response.body().ui != null
                        && response.body().ui.action != null
                        && response.body().ui.message != null) {
                    if (response.body().ui.action.equalsIgnoreCase(APIConstants.OFFER_ACTION_SHOW_TOAST)) {
                        AlertDialogUtil.showToastNotification(response.body().ui.message);
                    }
                }


            }
            initializePlayerView();
            fetchCardData();

        }


        @Override
        public void onFailure(Throwable t, int errorCode) {
            initializePlayerView();
            fetchCardData();

        }
    };

    private void closeMaturityData() {
        if (timerHandler != null) {
            timerHandler.removeCallbacksAndMessages(null);
            isAgeRatingVisible = false;
            isAgeHandler = false;
        }

        if (mPlayer != null && mPlayer.ageRatingLayout != null && mPlayer.ageRatingLayoutPortrait != null) {
            mPlayer.ageRatingLayout.setVisibility(View.GONE);
            mPlayer.ageRatingLayoutPortrait.setVisibility(View.GONE);
        }
    }
    private int getHeight(int mWidth) {
        /*if (mCardData!=null&&mCardData.isMovie()){
            return (mWidth * 4) / 3;
        }else {*/
        return (mWidth * 9) / 16;
        //}

    }

    public void epgChannelsListApiCall() {
        final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String selectedDateInString = format.format(date);
        String dateStamp = Util.getYYYYMMDD(selectedDateInString);
        ChannelListEPG.Params params = new ChannelListEPG.Params("", dateStamp);
        ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
            @Override
            public void onResponse(APIResponse<ChannelsEPGResponseData> response) {


            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(channelListEPG);
    }

}