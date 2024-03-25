package com.myplex.myplex.ui.views;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.myplex.api.APIConstants.ERROR_CONTENT_TYPE;
import static com.myplex.api.APIConstants.IS_PAUSE_MINICARD;
import static com.myplex.api.APIConstants.IS_SeasonUIForBack;
import static com.myplex.api.APIConstants.IS_SesonUIVisible;
import static com.myplex.api.APIConstants.MATCH_NOT_STARTED;
import static com.myplex.api.APIConstants.PARAM_CHROME_CAST_CONTENT_TYPE;
import static com.myplex.api.APIConstants.PARAM_CHROME_CAST_DRM_LICENSE_URL;
import static com.myplex.api.APIConstants.PARAM_CHROME_CAST_LICENSE_URL;
import static com.myplex.api.APIConstants.PARAM_CHROME_CAST_PARTNER_TYPE;
import static com.myplex.api.APIConstants.PLAY_ERR_COUNTRY_NOT_ALLOWED;
import static com.myplex.api.APIConstants.PLAY_ERR_MANDATORY_UPDATE;
import static com.myplex.api.APIConstants.PLAY_ERR_NON_LOGGED_USER;
import static com.myplex.api.APIConstants.PWA_URL;
import static com.myplex.api.APIConstants.VIDEOQUALTYHIGH;
import static com.myplex.api.APIConstants.VIDEOQUALTYLOW;
import static com.myplex.api.APIConstants.isHooqContent;
import static com.myplex.api.myplexAPISDK.getApplicationContext;
import static com.myplex.myplex.ui.fragment.epg.EPGView.CURRENT_PROGRAM;
import static com.myplex.myplex.ui.fragment.epg.EPGView.FUTURE_PROGRAM;
import static com.myplex.myplex.ui.fragment.epg.EPGView.PAST_PROGRAM;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.mediarouter.app.MediaRouteActionProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apalya.myplex.eventlogger.MyplexEvent;
import com.apalya.myplex.eventlogger.core.Constant;
import com.apalya.myplex.eventlogger.model.Error;
import com.apalya.myplex.eventlogger.model.Media;
import com.github.pedrovgs.LoggerD;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.images.WebImage;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;
import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.api.request.security.MediaLinkEncrypted;
import com.myplex.api.request.user.UserProfileRequest;
import com.myplex.model.AdFullScreenListResponse;
import com.myplex.model.AlarmData;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardDataPurchaseItem;
import com.myplex.model.CardDataRelatedMultimediaItem;
import com.myplex.model.CardDataVideos;
import com.myplex.model.CardDataVideosItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.model.DownloadMediadata;
import com.myplex.model.LocationInfo;
import com.myplex.model.MenuDataModel;
import com.myplex.model.MiniPlayerStatusUpdate;
import com.myplex.model.PartnerDetailsAppInAppResponse;
import com.myplex.model.PartnerDetailsResponse;
import com.myplex.model.PublishingHouse;
import com.myplex.model.SeasonData;
import com.myplex.model.SkipIntro;
import com.myplex.model.UserProfileResponseData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ads.PulseManager;
import com.myplex.myplex.analytics.APIAnalytics;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.analytics.PlayerStreamingAnalytics;
import com.myplex.myplex.analytics.partners.ErosNowPlayerEvents;
import com.myplex.myplex.media.MediaController2;
import com.myplex.myplex.media.PlayerGestureListener;
import com.myplex.myplex.media.PlayerListener;
import com.myplex.myplex.media.VideoView;
import com.myplex.myplex.media.VideoViewPlayer;
import com.myplex.myplex.media.exoVideo.ExoPlayerView;
import com.myplex.myplex.media.exoVideo.MyPlexAdEventCallback;
import com.myplex.myplex.media.exoVideo.MyplexVideoViewPlayer;
import com.myplex.myplex.media.exoVideo.PlayerEventListenerInterface;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.PlayerUpdateStateListener;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.ExpandedControlsActivity;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterEpisodePlayer;
import com.myplex.myplex.ui.adapter.HomePagerAdapterDynamicMenu;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsPlayer;
import com.myplex.myplex.ui.fragment.epg.DateHelper;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.MOUTracker;
import com.myplex.myplex.utils.ParentalControlDialog;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.ReminderListener;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;
import com.myplex.util.WidevineDrm;
import com.squareup.picasso.Picasso;
import com.yupptv.analytics.plugin.YuppAnalytics;
import com.yupptv.analytics.plugin.utils.AdPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.RenderScriptBlur;


//import static com.myplex.myplex.ui.fragment.PackagesFragment.PARAM_SUBSCRIPTION_TYPE_NONE;

public class MiniCardVideoPlayer implements AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener, AdEvent.AdEventListener, CacheManager.CacheManagerCallback,
        SDKUtils.RegenerateKeyRequestCallback, PlayerGestureListener.GestureListener, MyPlexAdEventCallback {
    public static final String MINIPLAYER = "mini player";
    private static final int PARAM_SUBSCRIPTION_TYPE_NONE = 0;
    private final String REQUEST_ID = "request_id";
    private final String PAYLOAD = "payload";
    public static final String PLAYER_STATUS_STARTED = "player_started";
    private static final long CENTER_BOX_VISIBILITY_DELAY = 0;
    private static final int MIDROLL_AD_CUE_POINT_IN_MINTS = /*use m if(s==60)*/1 * /*s*/60 * /*ms*/1000;
    protected Context mContext;
    protected LayoutInflater mInflator;
    private Bundle mArgumentsOfPlayer;
    private String currentData;
    private String requestID = null;
    private CardData mCardData;
    public boolean isCatchup;
    public boolean isFromCatchup;
    ListView mseasonList;
    SubtitleView subtitleView;
    private ImageView mWaterMarkImage;
    public View mParentLayout;
    public ImageView mPreviewImage;
    private View mPreviewImageOverlay;
    private ImageView mReminderImage;
    public ImageView mThumbnailPlay;
    protected TextView mBufferPercentage;
    public RelativeLayout mProgressBarLayout;
    public RelativeLayout mErrorLayout;
    private RelativeLayout mVideoViewParent;
    public SurfaceView mVideoView;
    public FrameLayout mHooqVideoViewLayout, imageFrameOverlay;
    protected RelativeLayout mHungamaVideoViewLayout;
    public CardData mData;
    protected int mPerBuffer;
    private LinearLayout mbutton_SeasonUI;
    public RelativeLayout subscriptionErrorLayout;
    private TextView videoTitleError;
    private RelativeLayout mSeasonUI;
    private int mWidth;
    private int mHeight;
    private RelativeLayout previousChannelLLSub, nextChannelLLSub;
    TextView previousChannelNameSub, previousChannelProgramSub, nextChannelNameSub, nextChannelProgramSub, subTitleError, titleError;
    private boolean isInPlayBackInitiatedMode = false;
    private TextView mDebugTextView;
    public String mAPIResponseVideosMessage;
    String mApiResponseVideosStatus;
    private final CacheManager mCacheManager = new CacheManager();
    private AdapterEpisodePlayer mAdapterEpisode;
    private RecyclerView mRecyclerViewPlayer;
    private List<CardData> mListSeasons;
    private CardData mRelatedVODData;
    private String contentId;
    private int mStartIndex = 1;
    private int mSelectedSeasonPosition;
    private CarouselInfoData mCarouselInfoData;
    private TextView mTextViewErroFetch;
    private List<String> mListSeasonNames;
    private boolean isLoadingMoreRequest;
    private boolean mIsLoadingMoreAvailable;
    private int skipIntroStartPosition;
    private int skipIntroEndPosition;
    private int skipIntroJumpTo;
    private String skipIntroSkipDisplay;
    private int skipEndStartPosition;
    private int skipEndEndPosition;
    private String skipEndSkipDisplay;
    private RelativeLayout skipIntroBtn, skipEndCreditBtn;
    public ImageView playPauseBtn, closeBtn;
    public LinearLayout playLayout;
    public TextView programName, channelName,videoTitle,videoDescription;;
    private MenuItem mMediaRouteMenuItem;
    private float mContentAspectRatio;
    private View mRewindButtonContainer, mFfwdButtonContainer;
    private TextView mFfwdText, mRewindText;
    private ImageView mFfwdButton, mRewButton;
    private RelativeLayout.LayoutParams layoutParamsOriginal;
    /*Ad releated fields*/
    // Container with references to video player and ad UI ViewGroup.
    private AdDisplayContainer mAdDisplayContainer;
    private TextView fingerPrintText;
    private RelativeLayout fingerPrintLayout;
    public LinearLayout ageRatingLayout, ageRatingLayoutPortrait;
    public RelativeLayout metaDataLayout;
    public eightbitlab.com.blurview.BlurView blurLayout;
    public TextView ageNumberTv, ageDescriptionTv, ageNumberTvP, ageDescriptionTvP;
    public int adEventEllapsedTime = 0;
    public RelativeLayout previousContentLL, nextContentLL;
    public TextView previousChannelName, previousProgramName, nextChannelName, nextProgramName;
    public ImageView next_content_image, previous_content_image;
    // The AdsLoader instance exposes the requestAds method.
    private AdsLoader mAdsLoader;
    //  public   boolean isStaticAdEnabled;
    public CardData previousCard, nextCard;
    private SessionManagerListener<CastSession> mCastConsumer;
    private Handler mHandlerLoop = new Handler();
    long frequencyMillSecond = 5000, displayMillSecond = 5000;
    private OnClickListener mSubscribePackageListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.playerStatusUpdate(APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED);
            }
        }
    };
    private OnClickListener mclickSeasonUIlist = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mbutton_SeasonUI.setVisibility(INVISIBLE);
        }
    };
    private OnClickListener mclickSeasonUI = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mSeasonUI != null) {
                mSeasonUI.setVisibility(INVISIBLE);
            }
            IS_SesonUIVisible = false;
            if (mSeason_icon != null) {
                // mSeason_icon.setVisibility(View.VISIBLE);
            }
            // resumeContentAfterAdPlayback();
            // mVideoViewPlayer.setPositionWhenPaused(mSavedContentPosition);

            if (IS_PAUSE_MINICARD == true) {
                mVideoViewPlayer.onPause();
                IS_PAUSE_MINICARD = false;
            } else {
                mVideoViewPlayer.onResume();
            }
        }
    };

    public Toolbar mChromeCastToolbar;
    protected boolean isToPlayOnLocalDevice;
    private YouTubePlayerSupportFragmentX youtubeFragment;
    private YouTubePlayer youtubePlayer;
    private YouTubePlayer.PlaybackEventListener mYoutubePlayerListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {
            /*if(mPlayerStatusListener != null){
                mPlayerStatusListener.onPlayerStarted(false);
            }*/
            resumePreviousOrientaionTimer();
            startMOUTracker(true);
            mBackIconImageViewLayout.setVisibility(GONE);

        }

        @Override
        public void onPaused() {
        }

        @Override
        public void onStopped() {
//            closePlayer();
        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };
    private boolean isChromeCastClickEventDone;
    protected boolean changeOrientationOnClose = true;

    private final Object chromeCastDialogLock = new Object();
    private DownloadMediadata downloadMediaData;
    private List<PlayerEventListenerInterface> playerEventsListeners = new ArrayList<>();
    //    private HooqChromecastEvent event;
    private JSONObject extendedAttr;
    protected boolean isDownloadedContentPlayback;
    private boolean isLoginRequestThroughPlayback;
    private boolean isShowingAlertDialog=false;
    private String mSource;
    private String mSourceDetails;
    private String mSourceTab;
    private int sourceCarouselPosition = -1;
    public ImageView mBackIconImageView;
    public RelativeLayout mTrackingStrip;
    private TextView goLiveText;
    public View mBackIconImageViewLayout;
    public ImageView back_nav_icon_error,chromeCastBack;
    public LinearLayout back_nav_icon_layout;
    public ImageView back_nav_icon_3;
    private FrameLayout mNextEpisodePopupWindow;
    public Handler contentProgressHandler = new Handler();
    private boolean didUserHide = false;
    private ImageView mNextEpisodeImage;
    private TextView mNextEpisodeTimer;
    private ImageView mNextEpisodePlayIcon;
    private int mNextEpisodePosition;
    private int popUpPercentage = 95;
    public View mGestureControllsLayoutContainer;
    private View mediacontrolls, mediacontrollsLive;
    private Button mbutton_Season, mbutton_Season_2;
    private boolean isNextEpisodePopupEnabled = false;

    private boolean isTimerScheduled;
    public RelativeLayout mPlayerContainer;
    private LinearLayout mSeason_icon;
    private FrameLayout vmaxAdContainer;
    private SparseArray<Integer> vmaxAdCuePoints = new SparseArray();
    private boolean isToShowVmaxPostRollAd = true;
    private MyplexEvent myplexEvent;
    private String stackTrace;
    private int bufferCount = 0;
    public FrameLayout mAdContainer;

    private static final boolean DEBUG = false;

    RelativeLayout playerexitscreen;

    public void setmFragmentCardDetailsDescription(FragmentCardDetailsDescription mFragmentCardDetailsDescription) {
        this.mFragmentCardDetailsDescription = mFragmentCardDetailsDescription;
    }

    private FragmentCardDetailsDescription mFragmentCardDetailsDescription;

    public void addPlayerEventListener(PlayerEventListenerInterface playerEventListenerInterface) {
        if (!playerEventsListeners.contains(playerEventListenerInterface)) {
            playerEventsListeners.add(playerEventListenerInterface);
        }
    }

    public void setRelatedCardData(CardData mRelatedCardData) {
        this.mRelatedCardData = mRelatedCardData;
    }

    private CardData mRelatedCardData;

    protected void startMOUTracker(boolean isYoutubePlayback) {
        if (!ApplicationController.ENABLE_MOU_TRACKING) {
            return;
        }
        if (mMouTracker == null) {
            mMouTracker = new MOUTracker(mVideoViewPlayer, mContext, mData, mRelatedCardData);
            if (mSourceTab != null)
                mMouTracker.setSourceTab(mSourceTab);
            mMouTracker.setSourceCarouselPosition(sourceCarouselPosition);
            mMouTracker.setSource(mSource);
            mMouTracker.setSourceDetails(mSourceDetails);
            mMouTracker.setIsTrailerPlayed(mIsTrailerPlaying);
            mMouTracker.setTrailerContentId(mTrailerContentId);
            mMouTracker.setVODContentType(mContentType);
            mMouTracker.start();
            mMouTracker.setNId(mNid);
            mMouTracker.setNotificationTitle(mNotificationTitle);
            mMouTracker.setPlayedProfile(profileSelect);
            mMouTracker.setLastWatchedPosition(mSavedContentPosition);
            mMouTracker.setLocalPlayBack(downloadMediaData != null);
            mMouTracker.setSource(mSource);
            mMouTracker.setSourceDetails(mSourceDetails);
            if (isYoutubePlayback) {
                mMouTracker.setPlayer(MiniCardVideoPlayer.this);
            }
        }
    }

    protected DialogCastOrPlayLocally mChromeCastDialog;
    private boolean isRetryPlayBak;

    // AdsManager exposes methods to control ad playback and listen to ad events.
    private AdsManager mAdsManager;

    // Factory class for creating SDK objects.
    private ImaSdkFactory mSdkFactory;

    // Default VAST ad tag; more complex apps might select ad tag based on content video criteria.
    private String mDefaultAdTagUrl;

    public boolean isLaunchDeeplink;
    public boolean chromeCastPopup;

    // The SDK will render ad playback UI elements into this ViewGroup.
    public ViewGroup mAdPlayerContainer;

    // Used to track if the current video is an ad (as opposed to a content video).
    public boolean mIsAdDisplayed;
    public boolean mIsAdPlaying = false;

    private final List<VideoAdPlayer.VideoAdPlayerCallback> mAdCallbacks =
            new ArrayList<VideoAdPlayer.VideoAdPlayerCallback>(1);

    // Check if the content video is complete
    protected boolean mIsContentComplete;

    // The saved position in the content to resume to after ad playback.
    protected int mSavedContentPosition;

    protected int mSavedAdContentPosition;

    private String mAdContentUrl;
    private boolean mIsContentStarted;
    protected boolean isMediaPaused;
    public List<CardData> mListQueueCardData;
    protected SessionManager mCastManager;
    protected MediaInfo mMediaInfo;
    private GestureDetector mGestureDetector;

    private Handler mHandler;
    private Handler mHandlerdeday = new Handler();
    private boolean isToolbarShown;

    private int mediaBitrate = -1;
    private static final int TIMEOUT_UNSET = -1;
    private static final int BITRATE_UNSET = -1;

    public void isPreviewInitialWorkDone(boolean isPreviewIntitalWorkDone) {
        if (mVideoViewPlayer != null && isPlayerInitialized()) {
            mVideoViewPlayer.isPreviewInitialWorkDone(isPreviewIntitalWorkDone);
        }
    }

    public boolean isPlayerInitialized() {
        return mPlayerState == PLAYER_INITIATED || isInPlayBackInitiatedMode || (mVideoViewPlayer != null && mVideoViewPlayer.isPlaybackInitialized());
    }

    public int getCurrentState() {
        return mPlayerState;
    }

    public int mPlayerState;
    public static int PLAYER_PLAY = 1;
    public static int PLAYER_STOPPED = 3;
    private static final int WAIT_FORRETRY = 99;
    public static final int PLAYER_INITIATED = 4;
    private PlayerFullScreen mPlayerFullScreen;
    boolean isESTPackPurchased = false;
    private String drmLicenseType = "st";
    protected String mNotificationTitle;

    public boolean isYouTubePlayerLaunched() {
        return isYouTubePlayerLaunched;
    }

    private boolean isYouTubePlayerLaunched = false;

    protected boolean isLocalPlayback = false;
    private static final String TAG = MiniCardVideoPlayer.class.getSimpleName();
    protected int currentDuration = 0;
    private String download_link, adaptive_link;
    private String camAngleSelect;
    private String langSelect;
    protected String profileSelect;
    protected String mStreamingFormat;
    protected boolean isFullScreen;
    private String partnerPackageName;


    public boolean isMinimized() {
        return isMinimized;
    }

    public void setMinimized(boolean minimized) {
        isMinimized = minimized;
        if (isMinimized) {
            hideAllControlls();

        }
    }

    protected boolean isMinimized = false;

    private static final int INTERVAL_RETRY = 10 * 1000;

    private int mAutoRetry = 0;
    private static final int MAX_AUTO_RETRY = 1;
    private static final int BEHIND_LIVE_WINDOW_MAX_AUTO_RETRY = 3;
    public MyplexVideoViewPlayer mVideoViewPlayer;
    protected String streamId;
    private LinearLayout debugTxtLayout;
    private CardDownloadData mDownloadData = null;

    protected boolean isPlayBackStartedAlready = false;
    protected String mContentType;

    private boolean adsAvailableWithContent = false;
    private boolean isVmaxAdEnabled = true;
    private boolean enableVMAXMidrollAds = true;
    private boolean vmaxCuePointsPrepared = false;
    private int behindLiveWindowAutoRetryCount = 0;
    private Menu mMenu;
    protected PlayerListener mPlayerListener = new PlayerListener() {

        @Override
        public void onStateChanged(int state, int elapsedTime) {
//            MiniCardVideoPlayer.this.state = PlayerListener.STATE_PAUSED;
            currentDuration = elapsedTime;
            switch (state) {
                case PlayerListener.STATE_PAUSED:
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("State:Paused");
                    }
                    if (isLocalPlayback) {
//				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
                    }
                    if (mVideoViewPlayer != null) {
                        mVideoViewPlayer.showMediaController();
                    }
                    //Log.d(TAG, "paused" + elapsedTime);
                    mThumbnailPlay.setVisibility(GONE);

                    setUpHandlerAndRunnable_update();

                    ComScoreAnalytics.getInstance().setEventVideoPaused(mData, mSavedContentPosition, sourceCarouselPosition, mSourceTab, mSource
                            , mSourceDetails, "pause", elapsedTime);
                    PlayerStreamingAnalytics.getInstance().notifyPaused(mData, false, 0);
                    break;
                case PlayerListener.STATE_PLAYING:
                    //   ((ExoPlayerView)mVideoViewPlayer).handleAspectRatioOnFullScreen(true);
                    //Log.d(TAG, "playing" + elapsedTime);
//                    mThumbnailPlay.setVisibility(View.GONE);
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("State:Started");
                    }
                    break;
                case PlayerListener.STATE_STOP:
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("State:Stoped");
                    }
//			if(isLocalPlayback){
//				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
//			}
                    //Log.d(TAG, "stop");
                    break;
                case PlayerListener.STATE_RESUME:
                    //Log.d(TAG, "resumes");
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("State:Resumed");
                    }
                    ComScoreAnalytics.getInstance().setEventContentLoaded(mData, mSavedContentPosition, sourceCarouselPosition, mSourceTab, mSource
                            , mSourceDetails, "resume");
                    PlayerStreamingAnalytics.getInstance().notifyPlaying(mData, false, 0);
                    break;
                case PlayerListener.STATE_PREPARING:
                    //showAdDialog();
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("State:Preparing");
                    }
                    if (mData != null && mData.isTVEpisode()) {
                        contentProgressHandler.post(onEveryTimeInterval);
                    }
                    break;
                case PlayerListener.STATE_PREPARED:
                    mBackIconImageView.setImageResource(R.drawable.back_icon_player);
                    //  ((ExoPlayerView)mVideoViewPlayer).handleAspectRatioOnFullScreen(true);
                    if (mVideoViewPlayer != null) {
                        mVideoViewPlayer.setFullScreen(isFullScreen);
                    }
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("State:Prepared");
                    }
                    if (mData != null && mData.isTVEpisode()) {
                        contentProgressHandler.post(onEveryTimeInterval);
                    }
                    break;
                case PlayerListener.STATE_STARTED:
                    // ((ExoPlayerView)mVideoViewPlayer).handleAspectRatioOnFullScreen(true);
                    mPlayerState = PLAYER_PLAY;
                    mPreviewImage.setVisibility(INVISIBLE);
                    mPreviewImageOverlay.setVisibility(INVISIBLE);
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.onPlayerStarted(mIsAdDisplayed);
                        mPlayerStatusListener.playerStatusUpdate("Buffering ended");
                    }

                    if (mData != null && !mData.isLive() && !mData.isProgram()) {
                        contentProgressHandler.post(onEveryTimeInterval);
                    }
                        mBackIconImageViewLayout.setVisibility(GONE);
                    back_nav_icon_layout.setVisibility(VISIBLE);
                    if (isFullScreen && !mIsAdPlaying) {
                        mChromeCastToolbar.setVisibility(VISIBLE);
                        mBackIconImageView.setImageResource(R.drawable.back_icon_player);
                    } else {
                        mChromeCastToolbar.setVisibility(GONE);
                        mBackIconImageView.setImageResource(R.drawable.icon_back_arrow_new);
                    }
                    if (!mIsAdDisplayed) {
                        LoggerD.debugLog("showMediaController");
                        allowMediaController(true);
                        showMediaController();
                        if (isVmaxAdEnabled && !vmaxCuePointsPrepared) {
                            vmaxCuePointsPrepared = true;
                            prepareMidRollCuePoints();
                        }
                    }
                    LoggerD.debugLog("changeOrientationOnClose- " + changeOrientationOnClose);
                    if (!isHelpScreenShowing && !isMinimized() && changeOrientationOnClose && !isPlayBackStartedAlready) {
                        resumePreviousOrientaionTimer();
                        if (isQueueAvailable()) {
                            changeOrientationOnClose = false;
                        }
                    }
                    if (!isPlayBackStartedAlready) {
                        clevertapVideoStarted();
                        isPlayBackStartedAlready = true;
                    }
                    startMOUTracker(false);
                    //Log.d(TAG, "started");

                    setUpHandlerAndRunnable_update();

                    if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                        setFullScreen(true);
                    }

                    ComScoreAnalytics.getInstance().setEventContentLoaded(mData, mSavedContentPosition, sourceCarouselPosition, mSourceTab, mSource
                            , mSourceDetails, "started");
                    PlayerStreamingAnalytics.getInstance().notifyPlaying(mData, false, 0);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (mVideoViewPlayer != null && mVideoViewPlayer instanceof ExoPlayerView) {
                                String contentMaxBitrate = mVideoViewPlayer.getContentMaxBitrate();
                                String contentMinBitrate = mVideoViewPlayer.getContentMinBitrate();
                                String contentResolution = mVideoViewPlayer.getContentResolution();
                                mPlayerStatusListener.playerStatusUpdate("Min Bitrate:: "+contentMinBitrate);
                                mPlayerStatusListener.playerStatusUpdate("Max Bitrate:: "+contentMaxBitrate);
                                mPlayerStatusListener.playerStatusUpdate("Content Resolution:: "+contentResolution);
                            }

                        }
                    }, 2000);


                    break;
                case PlayerListener.STATE_COMPLETED:
                    //Log.d(TAG, "completed");
                    PlayerStreamingAnalytics.getInstance().notifyEnded(mData, false, 0);
                    break;
//                    return;*/
//			if(isLocalPlayback){
//				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
//			}

            }
         /*   if (mData == null
                    || mData.generalInfo == null
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)) {
                return;
            }*/
            updatePlayerStatus(state, elapsedTime);

        }

        @Override
        public void onSeekComplete(MediaPlayer mp, boolean isSeeking) {
            // TODO Auto-generated method stub
            if (mVideoViewPlayer != null) {
                adEventEllapsedTime = mVideoViewPlayer.getCurrentPosition() / 1000;
            }
            //Log.d(TAG, "onSeekComplete");
            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.playerStatusUpdate("onSeekComplete");
            }

            PlayerStreamingAnalytics.getInstance().notifySeekStarted(mData, getSourceTab());
            if (mData != null
                    && mData.generalInfo != null
                    && isPlayingDVR
                    && !TextUtils.isEmpty(mData.globalServiceName)
                    && (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type) ||
                    APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type))) {
                //Log.d(TAG, "onSeekComplete:  gaPlayedTimeShiftChannel- " + mData.globalServiceName);
                Analytics.gaPlayedTimeShiftChannel(mData.globalServiceName.toLowerCase());
                ComScoreAnalytics.getInstance().setEventVideoSeeked(mData, mSavedContentPosition, sourceCarouselPosition, mSourceTab, mSource
                        , mSourceDetails, "seeked");
                PlayerStreamingAnalytics.getInstance().notifySeekStarted(mData, getSourceTab());
                PlayerStreamingAnalytics.getInstance().seekStartFromPosition(mData, mVideoViewPlayer.getCurrentPosition(), getSourceTab());
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int perBuffer) {

            if (mPerBuffer <= perBuffer) {
                mPerBuffer = perBuffer;
            }
            mPerBuffer = mPerBuffer / 100000;

            if (mPerBuffer > 100)
                mPerBuffer = 99;

            if (mBufferPercentage != null) {
                mBufferPercentage.setText("Loading " + mPerBuffer + "%");
            }
            int currentseekposition = mVideoViewPlayer.getCurrentPosition();
            if (currentseekposition < 0) {
                currentseekposition = 510;
            }
//            LoggerD.debugHooqVstbLog("onBufferingUpdate: mVideoViewPlayer.isPlaying- " + mVideoViewPlayer.isPlaying() + " currentseekposition- " + currentseekposition);
            if (mVideoViewPlayer.isPlaying() && currentseekposition > 500) {
                if (mPlayerListener != null)
                    mPlayerListener.onStateChanged(PlayerListener.STATE_STARTED, 0);
                mPerBuffer = 0;
                mVideoViewPlayer.deregisteronBufferingUpdate();
                mProgressBarLayout.setVisibility(INVISIBLE);
            } else if (mVideoViewPlayer.isPlaying()
                    && (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM))) {
                if (mPlayerListener != null)
                    mPlayerListener.onStateChanged(PlayerListener.STATE_STARTED, 0);
                mPerBuffer = 0;
                mVideoViewPlayer.deregisteronBufferingUpdate();
                mProgressBarLayout.setVisibility(INVISIBLE);
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int whatCode, int arg2, String errorMessage, String stackTrace) {
            SDKLogger.debug("stackTrace- " + stackTrace + " errorMessage- " + errorMessage);
            stackTrace = stackTrace == null ? "NA" : stackTrace;
            String what = new String();
            String error = new String();
            //sravani
            if (whatCode == ExoPlayerView.MEDIA_ERROR_DRM_BEHIND_LIVE_WINDOW_EXCEPTION) {
                // Toast.makeText(getActivity(),"Behind Live Window Exception",Toast.LENGTH_LONG).show();
                ((ExoPlayerView) mVideoViewPlayer).updateCurrentPosition();
                // ((ExoPlayerView) mVideoViewPlayer).();
                playContent();
//                resumeContentAfterAdPlayback();
                return true;
            }
            quitPlayer();
            HashMap properties = new HashMap<String, String>();
            String contentPartnerName = mData == null || mData.publishingHouse == null || mData.publishingHouse.publishingHouseName == null ? "NA" :
                    mData.publishingHouse.publishingHouseName;
            if (contentPartnerName.equalsIgnoreCase(APIConstants.NOT_AVAILABLE)) {
                contentPartnerName = mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider;
            }
            properties.put(Constant.CONTENT_PARTNER_NAME, contentPartnerName);
            properties.put(Constant.CONTENT_ID, mData == null || mData._id == null ? "NA" : mData._id);
            if (mData != null && mData.generalInfo != null && mData.generalInfo.type != null) {
                properties.put(ERROR_CONTENT_TYPE, mData.generalInfo.type);
            }

            if (mPlayerStatusListener != null) {
                switch (whatCode) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        what = "MEDIA_ERROR_UNKNOWN";
                        break;
                    case ExoPlayerView.MEDIA_ERROR_DRM_SESSION_EXCEPTION:
                        what = "DRM_SESSION_EXCEPTION";
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        what = "MEDIA_ERROR_SERVER_DIED";
                        break;
                    default:
                        what = "" + whatCode;
                        break;
                }
                switch (arg2) {
                    default:
                        error = "" + arg2;
                        break;
                }
                mPlayerStatusListener.playerStatusUpdate("Play Error :: what = " + what + " extra= " + error);
            }

            String reason = errorMessage == null ? "Play Error :: what = " + what + " extra= " + error : errorMessage;
            Analytics.mixPanelUnableToPlayVideo(mData, reason, profileSelect);
            Analytics.gaNotificationPlayBackFailed(mNotificationTitle, mNid, mData, profileSelect);

            if (mPlayerStatusListener != null
                    && mPlayerStatusListener.isFragmentVisible()
                    && whatCode == ExoPlayerView.MEDIA_ERROR_DRM_SESSION_EXCEPTION
                    && !ConnectivityUtil.isConnected(mContext)) {
                mPlayerStatusListener.playerStatusUpdate(errorMessage);
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.media_player_error_no_network_drm_session));
               /*
                hashMap.put((mData == null || mData.publishingHouse == null || mData.publishingHouse.publishingHouseName == null ? "NA" : mData.publishingHouse.publishingHouseName),
                        mData == null ||mData._id == null ?"NA" : mData._id);*/
                logErrorParams(Constant.ERROR_CATEGORY.PLAYBACK,
                        (mApiResponseVideosStatus != null && mAPIResponseVideosMessage != null) ? (mAPIResponseVideosMessage + " | " + mApiResponseVideosStatus) : Constant.PLAYBACK_ERROR_FETCHING,
                        mStreamingFormat, (mData == null || mData.content == null || mData.content.drmType == null ? "NA" : mData.content.drmType), (url == null ? "NA" : url),
                        (mData == null || mData.content == null || mData.content.drmEnabled ? "NA" : String.valueOf(mData.content.drmEnabled)),
                        errorMessage, null, Constant.PLAYBACK_CATEGORY, stackTrace, properties);
                return true;
            }
            if (mPlayerStatusListener != null
                    && mPlayerStatusListener.isFragmentVisible()
                    && arg2 == CardDetails.Partners.HOOQ) {
                mPlayerStatusListener.playerStatusUpdate(errorMessage);
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.canot_fetch_url) + " error: " + whatCode);
                constructStackTrace(errorMessage);
                logErrorParams(Constant.ERROR_CATEGORY.PLAYBACK,
                        (mApiResponseVideosStatus != null && mAPIResponseVideosMessage != null) ? (mAPIResponseVideosMessage + " | " + mApiResponseVideosStatus) : Constant.PLAYBACK_ERROR_FETCHING,
                        mStreamingFormat, (mData == null || mData.content == null || mData.content.drmType == null ? "NA" : mData.content.drmType), (url == null ? "NA" : url),
                        (mData == null || mData.content == null || mData.content.drmEnabled ? "NA" : String.valueOf(mData.content.drmEnabled)),
                        errorMessage, null, Constant.PLAYBACK_CATEGORY, stackTrace, properties);
                return true;
            }

            if (mPlayerStatusListener != null
                    && mPlayerStatusListener.isFragmentVisible()
                    && arg2 == CardDetails.Partners.HUNGAMA) {
                mPlayerStatusListener.playerStatusUpdate(errorMessage);
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.canot_fetch_url) + " .");
                logErrorParams(Constant.ERROR_CATEGORY.PLAYBACK,
                        (mApiResponseVideosStatus != null && mAPIResponseVideosMessage != null) ? (mAPIResponseVideosMessage + " | " + mApiResponseVideosStatus) : Constant.PLAYBACK_ERROR_FETCHING,
                        mStreamingFormat, (mData == null || mData.content == null || mData.content.drmType == null ? "NA" : mData.content.drmType), (url == null ? "NA" : url),
                        (mData == null || mData.content == null || mData.content.drmEnabled ? "NA" : String.valueOf(mData.content.drmEnabled)),
                        errorMessage, null, Constant.PLAYBACK_CATEGORY, stackTrace, properties);
                return true;
            }
            isParentalControlPinConfirmed = true;
            if (mPlayerState == PLAYER_STOPPED) {
                if (whatCode == ExoPlayerView.MEDIA_ERROR_DRM_BEHIND_LIVE_WINDOW_EXCEPTION && behindLiveWindowAutoRetryCount < BEHIND_LIVE_WINDOW_MAX_AUTO_RETRY) {
                    behindLiveWindowAutoRetryCount++;
                    playContent();
                } else {
                    retryPlayback();
                }
            }
            logErrorParams(Constant.ERROR_CATEGORY.PLAYBACK,
                    (mApiResponseVideosStatus != null && mAPIResponseVideosMessage != null) ? (mAPIResponseVideosMessage + " | " + mApiResponseVideosStatus) : Constant.PLAYBACK_ERROR_FETCHING,
                    mStreamingFormat, (mData == null || mData.content == null || mData.content.drmType == null ? "NA" : mData.content.drmType), (url == null ? "NA" : url),
                    (mData == null || mData.content == null || mData.content.drmEnabled ? "NA" : String.valueOf(mData.content.drmEnabled)),
                    errorMessage, null,
                    Constant.PLAYBACK_CATEGORY, stackTrace, properties);
            return true;
        }

        @Override
        public boolean onError(Object errorObject, String stackTrace) {
            stackTrace = stackTrace == null ? "NA" : stackTrace;
            HashMap properties = new HashMap<String, String>();
            String contentPartnerName = mData == null || mData.publishingHouse == null || mData.publishingHouse.publishingHouseName == null ? "NA" :
                    mData.publishingHouse.publishingHouseName;
            if (contentPartnerName.equalsIgnoreCase(APIConstants.NOT_AVAILABLE)) {
                contentPartnerName = mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider;
            }
            properties.put(Constant.CONTENT_PARTNER_NAME, contentPartnerName);
            properties.put(Constant.CONTENT_ID, mData == null || mData._id == null ? "NA" : mData._id);
            if (mData != null && mData.generalInfo != null && mData.generalInfo.type != null) {
                properties.put(ERROR_CONTENT_TYPE, mData.generalInfo.type);
            }
            String what = new String();
            String error = new String();
            quitPlayer();

            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.playerStatusUpdate("Play Error :: what = " + what + " extra= " + error);
            }

            Analytics.mixPanelUnableToPlayVideo(mData, error, profileSelect);
            Analytics.gaNotificationPlayBackFailed(mNotificationTitle, mNid, mData, profileSelect);

            mPlayerStatusListener.playerStatusUpdate(error);
            if (mPlayerStatusListener != null
                    && mPlayerStatusListener.isFragmentVisible()) {
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.canot_fetch_url));
            }
            isParentalControlPinConfirmed = true;
            if (mPlayerState == PLAYER_STOPPED) {
                retryPlayback();
            }

            logErrorParams(Constant.ERROR_CATEGORY.PLAYBACK,
                    (mApiResponseVideosStatus != null && mAPIResponseVideosMessage != null) ? (mAPIResponseVideosMessage + " | " + mApiResponseVideosStatus) : Constant.PLAYBACK_ERROR_FETCHING,
                    mStreamingFormat, (mData == null || mData.content == null || mData.content.drmType == null ? "NA" : mData.content.drmType), (url == null ? "NA" : url),
                    (mData == null || mData.content == null || mData.content.drmEnabled ? "NA" : String.valueOf(mData.content.drmEnabled)),
                    "NA",
                    "NA",
                    Constant.PLAYBACK_CATEGORY, stackTrace, properties);
            return true;
        }


        @Override
        public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
            if (mPlayerStatusListener != null) {
                String what = new String();
                switch (arg1) {
                    case MediaPlayer.MEDIA_INFO_UNKNOWN:
                        what = "MEDIA_INFO_UNKNOWN";
                        break;
                    case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        what = "MEDIA_INFO_VIDEO_TRACK_LAGGING";
                        break;
//			case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
//				break;

                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        what = "MEDIA_INFO_BUFFERING_START";
                        onBuffering();
                        PlayerStreamingAnalytics.getInstance().notifyBufferStarted(mData, getSourceTab());
                        break;

                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        what = "MEDIA_INFO_BUFFERING_END";
                        onBufferingEnd();
                        PlayerStreamingAnalytics.getInstance().notifyBufferStoped(mData, getSourceTab());
                        break;

                    case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        what = "MEDIA_INFO_BAD_INTERLEAVING";
                        break;

                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        what = "MEDIA_INFO_NOT_SEEKABLE";
                        break;

                    case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        what = "MEDIA_INFO_METADATA_UPDATE";
                        break;
//			case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
//				break;
//			case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
//				break;
                    default:
                        what = "" + arg1;
                        break;
                }

                String extra = new String();
                extra = "" + arg2;
//			mPlayerStatusListener.playerStatusUpdate("Play Info :: what = "+what+" extra= "+extra);
            }
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            LoggerD.debugLog("onCompletion");
            if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null)
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup = false;
          //  isFromCatchup = false;
            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.onCompleted(false);
            }
            updatePlayerStatus(PlayerListener.STATE_COMPLETED,getCurrentPosition());
            mSavedContentPosition = 0;
            mIsContentComplete = true;
            mIsContentStarted = false;

            if (isVmaxAdEnabled && isToShowVmaxPostRollAd && PrefUtils.getInstance().getPrefEnableVmaxPostRollAd()) {
                loadInstreamVideo();
                return;
            }
            if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null && isFromCatchup){
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup = true;
                mThumbnailPlay.setOnClickListener(mPlayerClickListener);
            }
            if (mAdsLoader != null) {
                mAdsLoader.contentComplete();
                if (!adsAvailableWithContent) {
                    playNextItem();
                }
            } else {
                // stoping the player to play next item
                if (isMinimized) {
                    playPauseBtn.setImageResource(R.drawable.ic_dock_play);
                } else {
                    mThumbnailPlay.setVisibility(VISIBLE);
                    mThumbnailPlay.setImageResource(R.drawable.player_replay_btn);
                }
                getMediaControllerInstance().mPauseButton.setVisibility(GONE);
                //   showMediaController();
                // playNextItem();
//                quitPlayer();
            }
//		locationClient.disconnect();
            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                YuppAnalytics.getInstance(mContext).handlePlayEnd();
            }
        }

        @Override
        public void onDrmError() {
            quitPlayer();
        }

        @Override
        public void onRetry() {
            if (mBufferPercentage != null) {
                mBufferPercentage.setVisibility(VISIBLE);
                mBufferPercentage.setText("retrying");
            }

        }

        @Override
        public void onFullScreen(boolean value) {
            setFullScreen(value);
            //Log.d(TAG, "onFullScreen(): " + value);
            if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                /*if (DeviceUtils.isTablet(mContext)) {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                } else {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }*/
                ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_PORTRAIT);
                resumePreviousOrientaionTimer();

            } else {
                /*if (DeviceUtils.isTablet(mContext)) {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                } else {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }*/
                ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                resumePreviousOrientaionTimer();
            }

        }

        @Override
        public void onBuffering() {
            bufferCount = bufferCount + 1;
            LoggerD.debugLog("onBuffering" + mVideoViewPlayer.isPlayerPaused());
            /* if (mProgressBarLayout != null && *//*mVideoViewPlayer.isPlayerPaused() &&*//* !isMinimized) {
                LoggerD.debugLog("hideBufferProgress");
                hideBufferProgress();
                return;
            }*/

            if (mProgressBarLayout != null && (mProgressBarLayout.getVisibility() == GONE || mProgressBarLayout.getVisibility() == INVISIBLE) && !isMinimized) {
                if (mBufferPercentage != null) {
                    mBufferPercentage.setVisibility(GONE);
                }
                mProgressBarLayout.setVisibility(VISIBLE);
            }

        }

        @Override
        public void onBufferingUpdate(boolean isBuffering) {
            if (isBuffering) {
                showBufferProgress();
            } else {
                hideBufferProgress();
            }
        }

        @Override
        public void onSubtitleChanged(String subtitleTrack) {
            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.playerStatusUpdate("Subtitle Selected:: " + subtitleTrack);
            }
        }

    };

    private boolean isBehindLiveWindowException(String stackTrace) {
        return stackTrace != null && stackTrace.contains("BehindLiveWindowException");
    }

    // TODO: 25/06/22
    public void playNextItem_player(Bundle args, CardData cardData) {
        mbutton_Season.setVisibility(GONE);
        mSeason_icon.setVisibility(INVISIBLE);
        mSeasonUI.setVisibility(INVISIBLE);
        IS_SesonUIVisible = false;
        IS_PAUSE_MINICARD = false;
        mbutton_SeasonUI.setVisibility(INVISIBLE);
        mRecyclerViewPlayer.computeHorizontalScrollOffset();
        mArgumentsOfPlayer = args;
        mCardData = cardData;
        changeOrientationOnClose = false;
        closePlayer();
        try {
            mData = mCardData;
        } catch (Exception e) {
            e.printStackTrace();
            quitPlayer();
            return;
        }
        updateCardPreviewImage(mData);
        mPlayerStatusListener.onUpdatePlayerData(mData);
        if (mPlayerStatusListener != null && isHooqContent(mData)) {
            mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.disable);
            if (!isFullScreen()) {
                mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.maximized);
            }
        }
        if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
            mSeason_icon.setVisibility(INVISIBLE);
            mSeasonUI.setVisibility(INVISIBLE);
            IS_SesonUIVisible = false;
            mbutton_SeasonUI.setVisibility(INVISIBLE);

        }
        playContent();
        //isPlayBackStartedAlready = true;
        IS_PAUSE_MINICARD = false;
    }


    public void playPreviousNextEpisode(CardData cardData, boolean isCatchup) {
        mCardData = cardData;
        this.isCatchup = isCatchup;
        changeOrientationOnClose = false;
        closePlayer();
        if(!isFromCatchup) {
            previousContentLL.setVisibility(GONE);
            nextContentLL.setVisibility(GONE);
        }
        MediaController2.rvAudioTracks.setVisibility(GONE);
        isFromCatchup = false;
        try {
            mData = mCardData;
        } catch (Exception e) {
            e.printStackTrace();
            quitPlayer();
            return;

        }
        updateCardPreviewImage(mData);
        mPlayerStatusListener.onUpdatePlayerData(mData);
        if (mPlayerStatusListener != null && isHooqContent(mData)) {
            mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.disable);
            if (!isFullScreen()) {
                mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.maximized);
            }
        }
        if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
            mSeason_icon.setVisibility(INVISIBLE);
            mSeasonUI.setVisibility(INVISIBLE);
            IS_SesonUIVisible = false;
            mbutton_SeasonUI.setVisibility(INVISIBLE);

        }
        playContent();
        //isPlayBackStartedAlready = true;
        IS_PAUSE_MINICARD = false;
    }

    public void showSubscriptionError(){
        if(subscriptionErrorLayout != null) {
            subscriptionErrorLayout.setVisibility(VISIBLE);
            try {
                if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                    playerexitscreen.setVisibility(VISIBLE);
                }else {
                    playerexitscreen.setVisibility(GONE);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
    protected void playNextItem() {
        if (!isQueueAvailable()) {
            LoggerD.debugLog("playNextItem no queue items quitPlayer");
            quitPlayer();
            return;
        }
        changeOrientationOnClose = false;
        closePlayer();
        try {
            CardData nextEpisodeData = getNextEpisodeData();
            LoggerD.debugLog("playNextItem nextEpisodeData- " + nextEpisodeData);
            if (nextEpisodeData == null) {
                quitPlayer();
                return;
            }
            mData = nextEpisodeData;
//            }
        } catch (Exception e) {
            e.printStackTrace();
            quitPlayer();
            return;
        }

/*        if (Util.isHooqContent(mData)
                && mData != null
                && mData.generalInfo != null
                && mData.generalInfo.partnerId != null) {
            setmHooqPlayerBundleArgs(HooqVstbUtilityHandler.getInstance().getPlayerArgs(mData.generalInfo.partnerId,
                    APIConstants.HOOQ_PLAYER_ID_EXOPLAYER,
                    APIConstants.HOOQ_DRMAGENT_ID_EXOPLAYER,
                    APIConstants.HOOQ_DRM_DESCRIPTION_WIDEWINE_VER_SIX,
                    MediaFormat.MPEGDASH));
        }*/
        updateCardPreviewImage(mData);
        mPlayerStatusListener.onUpdatePlayerData(mData);
        LoggerD.debugLog("HooqLoopFullScreen isFullScreen- " + isFullScreen());
        if (mPlayerStatusListener != null && isHooqContent(mData)) {
            mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.disable);
            if (!isFullScreen()) {
                /*(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)*/
                mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.maximized);
                LoggerD.debugLog("HooqLoopFullScreen close and maximize player");
            }
        }
        SDKLogger.debug("playNextItem mListQueueCardData- " + mListQueueCardData);
        playContent();
        isPlayBackStartedAlready = true;
    }

    private CardData getNextEpisodeData() {
        //            if (isEpisodeData()) {
        boolean isEnable = true;
        if (!isEnable) {
            return null;
        }
        if (!isQueueAvailable()) {
            return null;
        }
        CardData nextItemData = null;
        if (isEpisodeData())
            nextItemData = getNextItemByOrder();

        if (mData != null
                && nextItemData != null
                && mData.equals(nextItemData)) {
            LoggerD.debugLog("playNextItem is same as the old one");
            return null;
        }

        if (nextItemData == null) {
            int currentItemPosition = mListQueueCardData.indexOf(mData);
            int nextItemPosition = currentItemPosition + 1;
            if (currentItemPosition >= mListQueueCardData.size() - 1) {
                LoggerD.debugLog("playNextItem All the items have been played, starting the videos from first video again");
                nextItemPosition = 0;
            }

            if (isEpisodeData()) {
                nextItemPosition = currentItemPosition - 1;
                if (currentItemPosition == 0) {
                    changeOrientationOnClose = true;
                    return null;
                }
            }
            nextItemData = mListQueueCardData.get(nextItemPosition);
        }
        if (mData != null
                && mData.publishingHouse != null
                && nextItemData != null
                && nextItemData.publishingHouse != null
                && !mData.publishingHouse.publishingHouseName.equalsIgnoreCase(nextItemData.publishingHouse.publishingHouseName)) {
            changeOrientationOnClose = true;
            return null;
        }
        return nextItemData;
    }

    private boolean getAutoPlaybackOrder() {
        PartnerDetailsResponse partnerDetailsResponse = PropertiesHandler.getPartnerDetailsResponse(mContext);
        String partnerName = (mData.publishingHouse != null && !TextUtils.isEmpty(mData.publishingHouse.publishingHouseName)) ? mData.publishingHouse.publishingHouseName : mData.contentProvider;
        if (!TextUtils.isEmpty(partnerName) && partnerDetailsResponse != null && mData != null) {
            for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
                if (partnerDetailsResponse != null
                        && partnerDetailsResponse.partnerDetails != null
                        && partnerDetailsResponse.partnerDetails.get(i) != null
                        && !TextUtils.isEmpty(partnerDetailsResponse.partnerDetails.get(i).name)
                        && partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
                    if (partnerDetailsResponse.partnerDetails.get(i).episodePlaybackOrder != null && partnerDetailsResponse.partnerDetails.get(i).episodePlaybackOrder.equalsIgnoreCase("desc")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private CardData getNextItemByOrder() {
        CardData nextItemData = null;
        boolean autoPlaybackDescending = getAutoPlaybackOrder();

        for (int i = 0; i < mListQueueCardData.size(); i++) {
            Integer currentItemSerialNo = Integer.parseInt(mData.content.serialNo);
            Integer tempItemSerialNo = Integer.parseInt(mListQueueCardData.get(i).content.serialNo);
            if (currentItemSerialNo.equals(tempItemSerialNo)) {
                if (autoPlaybackDescending) {
                    if (i == 0) {
                        return null;
                    } else {
                        return mListQueueCardData.get(i - 1);
                    }
                } else {
                    if (i == (mListQueueCardData.size() - 1)) {
                        return null;
                    } else {
                        return mListQueueCardData.get(i + 1);
                    }

                }
            }
            // //Log.d(TAG, "getNextItemBySerialNo: nextItemSerialNo- " + nextItemSerialNo + " itemSerialNo- " + itemSerialNo);

        }

        return nextItemData;
    }

    private CardData getNextItemBySerialNo() {
        CardData nextItemData = null;
        if (mData == null
                || mData.content == null
                || TextUtils.isEmpty(mData.content.serialNo)) {
            return nextItemData;
        }
        try {
            boolean isSerialNoFound = false;
            Integer currentItemSerialNo = Integer.parseInt(mData.content.serialNo);
            Integer nextItemSerialNo = currentItemSerialNo;
            for (CardData itemData : mListQueueCardData) {
                if (itemData.content != null
                        || TextUtils.isEmpty(itemData.content.serialNo)) {
                    Integer itemSerialNo = Integer.parseInt(itemData.content.serialNo);
                    if ((itemSerialNo.intValue() > currentItemSerialNo.intValue()
                            && itemSerialNo.intValue() < nextItemSerialNo.intValue())
                            || (nextItemSerialNo.intValue() == currentItemSerialNo.intValue()
                            && itemSerialNo.intValue() > currentItemSerialNo.intValue())) {
                        nextItemSerialNo = itemSerialNo;
                        nextItemData = itemData;
                        if (nextItemSerialNo.intValue() == currentItemSerialNo.intValue() + 1) {
                            break;
                        }
                        //Log.d(TAG, "getNextItemBySerialNo: nextItemSerialNo- " + nextItemSerialNo + " itemSerialNo- " + itemSerialNo);
                    }
                    isSerialNoFound = true;
                }
            }
            if (nextItemSerialNo == currentItemSerialNo && isSerialNoFound) {
                return mData;
            }
            //Log.d(TAG, "getNextItemBySerialNo: nextItemSerialNo- " + nextItemSerialNo + " currentSerialNo- " + currentItemSerialNo);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextItemData;
    }

    protected boolean isQueueAvailable() {
        return mListQueueCardData != null && !mListQueueCardData.isEmpty() && mListQueueCardData.size() != 1
                && mListQueueCardData.contains(mData) && !(mListQueueCardData.indexOf(mData) == (mListQueueCardData.size() - 1));
    }

    private boolean isEpisodeData() {
        return (mRelatedCardData != null
                && mRelatedCardData.generalInfo != null
                && (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mRelatedCardData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(mRelatedCardData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(mRelatedCardData.generalInfo.type)));
    }

    public int getCurrentPosition() {
        if (mVideoViewPlayer != null) {
            return mVideoViewPlayer.getCurrentPosition() / 1000;
        }
        return 0;
    }

    private Runnable mPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            Long updatePeriod = Long.valueOf((PrefUtils.getInstance().getUpdatesStatusIntervalInSec()) * 1000);
            //Log.d(TAG, "API progressautomation1 complect addplay --" + PrefUtils.getInstance().getUpdatesStatusIntervalInSec());
            if (mContext != null && !(ConnectivityUtil.isConnected(mContext))) {
                Toast.makeText(mContext, R.string.error_network_not_available, Toast.LENGTH_SHORT).show();
                //Log.d(TAG, "no network available");
            }

            if (isMediaPlaying()) {
                if (!isAdPlaying()) {
                    if (mVideoViewPlayer != null && mVideoViewPlayer.isPlaying() && !mIsContentComplete) {
                        //Log.d(TAG, "API progressautomation1 complect1 " + mVideoViewPlayer.getCurrentState() + "  " + mVideoViewPlayer.isPlaying());
                        updatePlayerStatus(PlayerListener.STATE_PAUSED, getCurrentPosition());
                    }
                    if (!mVideoViewPlayer.isPlaying()) {
                        //Log.d(TAG, "API progressautomation1 " + !mVideoViewPlayer.isPlaying() + "  " + isMediaPlaying());
                    }
                } else {
                    //Log.d(TAG, "API progressautomation1 complectplay " + isMediaPlaying());
                    mHandlerdeday.removeCallbacks(mPlayerRunnable);

                }
                resumePreviousOrientaionTimer();
            } else {
                //Log.d(TAG, "API progressautomation1 complectplay media not playing " + isMediaPlaying());
                mHandlerdeday.removeCallbacks(mPlayerRunnable);
            }
            if (mIsContentComplete) {
                updatePlayerStatus(PlayerListener.STATE_COMPLETED,getCurrentPosition());
                mHandlerdeday.removeCallbacks(mPlayerRunnable);
                //Log.d(TAG, "API progressau                                                                                                                                                    tomation1 complect " + mIsContentComplete);
            }


            mHandlerdeday.postDelayed(this, updatePeriod);
        }
    };

    private void setUpHandlerAndRunnable_update() {
        //Log.d(TAG, "API" + PrefUtils.getInstance().getUpdatesStatusIntervalInSec() + " " + PrefUtils.getInstance().getSeekBarEnable());
        if (PrefUtils.getInstance().getUpdatesStatusIntervalInSec() > 0) {
            if (isAdPlaying()) {
                mHandlerdeday.removeCallbacks(mPlayerRunnable);
                //Log.d(TAG, "API progressautomation1 complect addplay");
            }
            if (isMediaPlaying() && !mVideoViewPlayer.isPlaying()) {
                //Log.d(TAG, "API progressautomation1 pause " + !mVideoViewPlayer.isPlaying() + "  " + isMediaPlaying());
                mHandlerdeday.removeCallbacks(mPlayerRunnable);
            } else {

                mHandlerdeday.removeCallbacks(mPlayerRunnable);
                mPlayerRunnable.run();
            }
        }
    }

    public void updatePlayerStatus(int state, int elapsedTime) {
        if (mVideoViewPlayer == null) {
            return;
        }
        LoggerD.debugLog("updatePlayerStatus elapsedTime- " + elapsedTime + " mVideoViewPlayer.getCachedDuration()/1000- " + mVideoViewPlayer.getCachedDuration() / 1000);

        if (PlayerListener.STATE_PAUSED != state
                || elapsedTime <= 0
                || mData == null) {
            return;
        }
        int position = mVideoViewPlayer.getCurrentPosition();
        int duration = mVideoViewPlayer.getCachedDuration();
        long percent = 0;
        if (duration > 0) {
            // use long to avoid overflow
            percent = 100L * position / duration;
        }

        String action = Analytics.ACTION_TYPES.pause.name();

        LoggerD.debugLog("updatePlayerStatus duration percent- " + percent);
        LoggerD.debugLog("updatePlayerStatus duration- " + duration + " elpaseTime- " + position);
        if (percent >= 95) {
            LoggerD.debugLog("updatePlayerStatus video completed update the position with 0 position");
            elapsedTime = 0;
            action = Analytics.ACTION_TYPES.complete.name();
        }
        if (mVideoViewPlayer != null && elapsedTime >= duration / 1000) {
            elapsedTime = 0;
        }
        if (isDownloadedContentPlayback) {
            updateSavedPositionInLocally(elapsedTime);
            return;
        }
    String streamName="";
        if(mData!= null && mData.generalInfo!=null && mData.generalInfo.type!= null) {
            if (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
                streamName = APIConstants.TYPE_LIVE;
            } else {
                streamName = mData.getType();
            }
        }
        if(streamName!=null){
            if(!streamName.equals(APIConstants.TYPE_LIVE) && mData._id != null && !mData._id.equals("")) {
                Util.updatePlayerStatus(elapsedTime, action, mData._id,streamName, new PlayerUpdateStateListener() {
                    @Override
                    public void onSuccess(String status) {
                        if (APIConstants.PLAY_ERR_NON_LOGGED_USER.equalsIgnoreCase(status)) {
                            if(!isShowingAlertDialog) {
                                ((MainActivity) mContext).reloadData();
                                if (((MainActivity) mContext).isMediaPlaying()) {
                                    ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.onPause();
                                }
                                isLoginRequestThroughPlayback = true;
                                PrefUtils.getInstance().setPrefLoginStatus("");
                                String message = "Already Logged-in for Another Device. Please login here again to get access.";
           /* if(((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mAPIResponseVideosMessage != null)
                message = ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mAPIResponseVideosMessage;*/
                                AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", mContext.getResources()
                                                .getString(R.string.feedbackokbutton),
                                        new AlertDialogUtil.NeutralDialogListener() {
                                            @Override
                                            public void onDialogClick(String buttonText) {
                                                PrefUtils.getInstance().setPrefLoginStatus("");
                                                AlertDialogUtil.dismissDialog();
                                                launchLoginActivity();
                                            }
                                        });
                            }
                            isShowingAlertDialog=false;
                        }
                    }
                });
            }else if(mData.globalServiceId != null) {
                Util.updatePlayerStatus(elapsedTime, action, mData.globalServiceId,streamName, new PlayerUpdateStateListener() {
                    @Override
                    public void onSuccess(String status) {
                        if (APIConstants.PLAY_ERR_NON_LOGGED_USER.equalsIgnoreCase(status)) {
                            //Fixed the popup displaying multiple times
                            if (!isShowingAlertDialog) {
                                ((MainActivity) mContext).reloadData();
                                if (((MainActivity) mContext).isMediaPlaying()) {
                                    onPause();
                                }
                                isLoginRequestThroughPlayback = true;
                                PrefUtils.getInstance().setPrefLoginStatus("");
                                String message = "Already Logged-in for Another Device. Please login here again to get access.";
           /* if(((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mAPIResponseVideosMessage != null)
                message = ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mAPIResponseVideosMessage;*/
                                AlertDialogUtil.showNeutralAlertDialog(mContext, message, "", mContext.getResources()
                                                .getString(R.string.feedbackokbutton),
                                        new AlertDialogUtil.NeutralDialogListener() {
                                            @Override
                                            public void onDialogClick(String buttonText) {
                                                AlertDialogUtil.dismissDialog();
                                                launchLoginActivity();

                                            }
                                        });
                                //

                            }
                            isShowingAlertDialog=false;
                        }
                    }
                });
            }

        }


    }

    private void updateSavedPositionInLocally(int duration) {
        CardDownloadData cardDownloadData = DownloadUtil.getDownloadDataFromDownloads(mData);
        LoggerD.debugDownload("elapsedTime- " + cardDownloadData);
        if (cardDownloadData == null) {
            return;
        }
        cardDownloadData.elapsedTime = duration;
    }

    @Override
    public void onSuccess() {
        playContent();
    }

    @Override
    public void onFailed(String msg) {
        if (isFullScreen()) {
            changeOrientationOnClose = true;
        }
        if (mMediaLinkFetchListener == null) {
            return;
        }
        if (TextUtils.isEmpty(msg)) {
            mMediaLinkFetchListener.onFailure(new Throwable("Failed to re-gen key: " + msg), APIRequest.ERR_UN_KNOWN);
            return;
        }
        mMediaLinkFetchListener.onFailure(new Throwable(msg), APIRequest.ERR_UN_KNOWN);
    }

    protected PulseManager.ClickThroughCallback clickThroughCallback;

    public void setOnClickThroughCallback(PulseManager.ClickThroughCallback callback) {
        clickThroughCallback = callback;
    }

    private String url = "NA";


    private APICallback mMediaLinkFetchListener = new APICallback<CardResponseData>() {


        public int mAPIErrorCode;

        @Override
        public void onResponse(APIResponse<CardResponseData> response) {
            //if(!isStaticAdEnabled)
            // if(FragmentCardDetailsPlayer.DraggableState.maximized == 4) {
            AdFullScreenListResponse adFullScreenConfigResponse = PropertiesHandler.getAdFullScreenConfig(mContext);
            if (!isRetryPlayBak && adFullScreenConfigResponse != null && adFullScreenConfigResponse.getAdFullScreenConfig().get(0) != null && adFullScreenConfigResponse.getAdFullScreenConfig().get(0).getActive().equalsIgnoreCase("true")) {
                if (adFullScreenConfigResponse.getAdFullScreenConfig().get(0) != null && adFullScreenConfigResponse.getAdFullScreenConfig().get(0).getPosition().equalsIgnoreCase("pre-roll")) {
                    if (adFullScreenConfigResponse.getAdFullScreenConfig().get(0) != null && adFullScreenConfigResponse.getAdFullScreenConfig().get(0).getType().equalsIgnoreCase("image"))
                        showAdDialog(this, response, adFullScreenConfigResponse);
                }
                return;
            }
            // }
            setPlayerData(this, response);
        }


        @Override
        public void onFailure(Throwable t, int errorCode) {
            quitPlayer();
            HashMap properties = new HashMap<String, String>();
            String contentPartnerName = mData == null || mData.publishingHouse == null || mData.publishingHouse.publishingHouseName == null ? "NA" :
                    mData.publishingHouse.publishingHouseName;
            if (contentPartnerName.equalsIgnoreCase(APIConstants.NOT_AVAILABLE)) {
                contentPartnerName = mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider;
            }
            properties.put(Constant.CONTENT_PARTNER_NAME, contentPartnerName);
            if (!TextUtils.isEmpty(requestID)) properties.put(REQUEST_ID, requestID);
            properties.put(PAYLOAD, currentData == null ? "NA" : currentData);
            properties.put(Constant.CONTENT_ID, mData == null || mData._id == null ? "NA" : mData._id);
            if (mData != null && mData.generalInfo != null && mData.generalInfo.type != null) {
                properties.put(ERROR_CONTENT_TYPE, mData.generalInfo.type);
            }
            //Log.d(TAG, "onFailure " + t);
            String errorMessage = mData != null && mData.generalInfo != null
                    && (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type))
                    ? mContext.getString(R.string.canot_fetch_url) : mContext.getString(R.string.canot_fetch_url_videos);

            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                errorMessage = mContext.getString(R.string.network_error);
            }
            String reason = (t != null && t.getMessage() != null)
                    ? t.getMessage()
                    : errorMessage;
            if (!TextUtils.isEmpty(mAPIResponseVideosMessage) && !"OK".equalsIgnoreCase(mAPIResponseVideosMessage)) {
                errorMessage = mAPIResponseVideosMessage;
                reason = mAPIResponseVideosMessage;
            } else {
                errorMessage = reason;
            }
            if (mPlayerStatusListener != null
                    && mPlayerStatusListener.isFragmentVisible()) {
                // if (!APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED.equalsIgnoreCase(errorMessage) || !APIConstants.ERR_PACKAGES_NOT_DEFINED .equalsIgnoreCase(errorMessage))
                //   AlertDialogUtil.showToastNotification(errorMessage);
            }

            String _id = mData._id;
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                _id = mData.globalServiceId;
            }
            SDKLogger.debug("reason:: " + reason + " " +
                    "errorMessage:: " + errorMessage + " " +
                    "errorDetailedInfo:: " + errorDetailedInfo);
            if (errorDetailedInfo != null && errorDetailedInfo.equalsIgnoreCase("Fragment is not visible")) {
                errorDetailedInfo = "";
                return;
            }
            Analytics.mixPanelUnableToFetchVideoLink(mData == null || mData.generalInfo == null || mData.generalInfo.title == null ? APIConstants.NOT_AVAILABLE : mData.generalInfo.title, contentPartnerName, mData == null ? APIConstants.NOT_AVAILABLE : _id, mNid, reason, mNotificationTitle);
            constructStackTrace(errorMessage);
            String message = (mApiResponseVideosStatus != null && mAPIResponseVideosMessage != null)
                    ? (mAPIResponseVideosMessage + " | " + mApiResponseVideosStatus) : Constant.PLAYBACK_ERROR_FETCHING;
            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.playerStatusUpdate(errorMessage);
                mPlayerStatusListener.playerStatusUpdate("errorDetailedInfo::" + errorDetailedInfo);
            }
            logErrorParams(Constant.ERROR_CATEGORY.API,
                    message,
                    null,
                    null,
                    null,
                    null,
                    (errorDetailedInfo != null)
                            ? (errorMessage + " | " + errorDetailedInfo) : errorMessage,
                    String.valueOf(mAPIErrorCode) == null ? "NA" : String.valueOf(mAPIErrorCode),
                    Constant.API_CATEGORY,
                    stackTrace,
                    properties);
            errorDetailedInfo = "";
        }
    };

    /*
    * "skipIntro": [
              {
              "skipType" : "scene",
              "startPosition" : "20",
              "endPosition" : "160",
              "jumpTo" : "200",
              "skipDisplay" : "SKIP INTRO",
              "description" : "SKIP INTRO",
              "image" : "<link>",
              "action" : "skipIntro"
              }
              ]
    */
    private void setPlayerData(APICallback callback, APIResponse<CardResponseData> response) {
        //Log.d(TAG, "onResponse ");
        int mAPIErrorCode;
        if (null == response || response.body() == null) {
            callback.onFailure(new Throwable("Empty response or response body"), APIRequest.ERR_UN_KNOWN);
            return;
        }

        Log.d("TYPE", "MinicardVideo Player" + response);

        setRequestIDAndPayLoad(response);
        mApiResponseVideosStatus = response.body().status;
        mAPIErrorCode = response.body().code;
        mAPIResponseVideosMessage = response.body().message;
        if (response.body().status.equalsIgnoreCase("ERR_INVALID_SESSION_ID") && response.body().code == 401) {
            SDKUtils.makeReGenerateKeyRequest(mContext, MiniCardVideoPlayer.this);
//                    cacheManagerCallback.OnlineError(new Throwable(""), errorCode);
            return;
        }
        if (response.body().results == null
                || response.body().results.size() == 0) {
            callback.onFailure(new Throwable("Invalid or empty results"), APIRequest.ERR_UN_KNOWN);
            return;
        }
        if(response!=null && response.body()!=null && response.body().results!=null && response.body().results.get(0)!=null
                && response.body().results.get(0).content!=null && response.body().results.get(0).content.audioLanguage!=null
                && response.body().results.get(0).content.audioLanguage.size()>0 && mData!=null && mData.content!=null &&
                mData.content.audioLanguage!=null){
           mData.content.audioLanguage= response.body().results.get(0).content.audioLanguage;
        }
        String _id = mData._id;
        if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            _id = mData.globalServiceId;
        }
        if (mIsTrailerPlaying) {
            _id = mTrailerContentId;
        }
        //Log.d(TAG, "success: message- " + response.body().message);
        String contentPartnerName = mData == null || mData.publishingHouse == null || mData.publishingHouse.publishingHouseName == null ? APIConstants.NOT_AVAILABLE : mData.publishingHouse.publishingHouseName;
        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            contentPartnerName = mData == null || mData.contentProvider == null ? APIConstants.NOT_AVAILABLE : mData.contentProvider; //3
        }
        Gson gson = new Gson();
        String mediaResponse = gson.toJson(response);
        Log.d("MyTag", "MinicardVideoPlayer" + mediaResponse);

        for (CardData data : response.body().results) {
            Log.e("VIDEO_STATUS ::: ", data.videos.status);
            Log.e("VIDEO_MESSAGE ::: ", data.videos.message);

            if (PrefUtils.getInstance().getIsToShowForm()) {
                if (mPlayerStatusListener != null) {
                    mPlayerStatusListener.playerStatusUpdate(PLAY_ERR_MANDATORY_UPDATE);
                }
                return;
            }

            if (data.videos != null && data.videos.status != null && !data.videos.status.equalsIgnoreCase("SUCCESS")) {
                if (data.videos.message != null && (APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED.equalsIgnoreCase(data.videos.status) ||  data.videos.status.equalsIgnoreCase("ERR_USER_NOT_SUBSCRIBED"))) {
                    // AlertDialogUtil.showToastNotification(data.videos.message);
                    mAPIResponseVideosMessage = data.videos.message;
                    mApiResponseVideosStatus = data.videos.status;
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate(APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED);
                    }
                    Analytics.mixPanelUnableToFetchVideoLink(mData == null || mData.generalInfo == null || mData.generalInfo.title == null ? APIConstants.NOT_AVAILABLE : mData.generalInfo.title, contentPartnerName, mData == null ? APIConstants.NOT_AVAILABLE : _id, mNid, "status: " + data.videos.status + " message: " + data.videos.message, mNotificationTitle);

                    if (mHandler == null) {
                        mHandler = new Handler(Looper.getMainLooper());
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            quitPlayer();
                        }
                    }, 5000);
                    return;
                } else if (PLAY_ERR_NON_LOGGED_USER.equalsIgnoreCase(data.videos.status)) {
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate(PLAY_ERR_NON_LOGGED_USER);
                    }
                    return;
                }
                    else if (MATCH_NOT_STARTED.equalsIgnoreCase(data.videos.status)) {
                        if (mPlayerStatusListener != null) {
                            mPlayerStatusListener.playerStatusUpdate(MATCH_NOT_STARTED);
                        }
                        if(data.videos.ui!=null &&data.videos.ui.message!=null && !TextUtils.isEmpty(data.videos.ui.message) ){
                            AlertDialogUtil.showNeutralAlertDialog(mContext, data.videos.ui.message, "", mContext.getResources()
                                            .getString(R.string.feedbackokbutton),
                                    new AlertDialogUtil.NeutralDialogListener() {
                                        @Override
                                        public void onDialogClick(String buttonText) {
                                            AlertDialogUtil.dismissDialog();
                                            ((MainActivity) mContext).closePlayerFragment();
                                        }
                                    });

                        }
                        return;
                } else if (PLAY_ERR_COUNTRY_NOT_ALLOWED.equalsIgnoreCase(data.videos.status)) {
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate(PLAY_ERR_COUNTRY_NOT_ALLOWED);
                    }
                    AlertDialogUtil.showToastNotification(data.videos.message);
                    callback.onFailure(new Throwable(data.videos.message), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                //  mAPIResponseVideosMessage = data.videos.message;
                // mApiResponseVideosStatus = data.videos.status;
                AlertDialogUtil.showToastNotification(data.videos.message);
                callback.onFailure(new Throwable(mAPIResponseVideosMessage), APIRequest.ERR_UN_KNOWN);
                return;
            }

            CardDataVideos videos = data.videos;
            mData.videoInfo = data.videoInfo;
            mData.videos = data.videos;
            /*data.thumbnailSeekPreview = "https://apalyademo.s3.amazonaws.com/thumbnailSeekPreview/263422/263422.vtt";*/
            mData.thumbnailSeekPreview = data.thumbnailSeekPreview;
            mAPIResponseVideosMessage = data.videos.message;
            mApiResponseVideosStatus = data.videos.status;
            mData.subtitles = data.subtitles;
            mData.skipConfig = data.skipConfig;
            mData.content.isSupportCatchup = data.content.isSupportCatchup;
            mData.content.adEnabled = data.content.adEnabled;
            mData.content.adConfig = data.content.adConfig;
            if (data != null && data.fingerPrint != null) {
                if (data.fingerPrint.getActive() != null && data.fingerPrint.getActive().equalsIgnoreCase("true")) {
                    if (data.fingerPrint.getText() != null) {
                        fingerPrintText.setText(data.fingerPrint.getText());
                        fingerPrintText.setTextColor(Color.parseColor(data.fingerPrint.getTextColor()));
                        fingerPrintText.setBackgroundColor(Color.parseColor(data.fingerPrint.getTextBackground()));
                        if(data.fingerPrint.getFrequencyInSeconds() != null)
                            frequencyMillSecond = data.fingerPrint.getFrequencyInSeconds() * 1000;
                        if(data.fingerPrint.getDisplayDurationInSeconds() != null)
                            displayMillSecond = data.fingerPrint.getDisplayDurationInSeconds() * 1000;
                        if(data.fingerPrint.getTextSize() != null) {
                            fingerPrintText.setTextSize(Float.parseFloat(data.fingerPrint.getTextSize()));
                        }
                       // startRepeatingTask();
                        handleFingerPrint();
                        //  ((ExoPlayerView) mVideoViewPlayer).initilizeMediaController();
                    }
                }
            }
            if (data.skipConfig != null) {
                getSkipIntroInfo();
                getSkipEndCreditInfo();
            }
              /*  if (data != null && data.content != null && data.content.videoQuality != null) {
                    PrefUtils.getInstance().setContentVideoQuality(data.content.videoQuality);
                }*/

            if (data != null
                    && data.videos != null
                    && data.videos.adConfig != null
                    && data.videos.adConfig.vast != null) {
                if (!TextUtils.isEmpty(data.videos.adConfig.vast.android)) {
                    try {
                        mDefaultAdTagUrl = URLDecoder.decode(data.videos.adConfig.vast.android, "UTF-8");
                        mDefaultAdTagUrl = data.videos.adConfig.vast.android;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        mDefaultAdTagUrl = "";

                    }
                } else if (!TextUtils.isEmpty(data.videos.adConfig.vast.all)) {
                    try {
                        mDefaultAdTagUrl = URLDecoder.decode(data.videos.adConfig.vast.all, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        mDefaultAdTagUrl = "";

                    }
                } else {
                    mDefaultAdTagUrl = "";
                }
            }
            if (response.body().ui != null
                    && response.body().ui.action != null
                    && response.body().ui.message != null) {
                if (response.body().ui.action.equalsIgnoreCase(APIConstants.OFFER_ACTION_SHOW_TOAST)) {
                    AlertDialogUtil.showToastNotification(response.body().ui.message);
                }
            }

            if (data.videos != null
                    && data.videos.status != null
                    && data.videos.status.equalsIgnoreCase("SUCCESS")
                    && data.videos.actionUrl != null && data.videos.appAction != null) {
                fireAppInAppEvent();
                if (data.videos.appAction.equalsIgnoreCase(HomePagerAdapterDynamicMenu.ACTION_DEEPLINK)) {
                    launchPartnerAppWithActionUrl(data.videos.actionUrl);
                } else {
                    launchPartnerWebViewWithActionUrl(data.videos.actionUrl);
                }
                return;
            }
            if (!checkUrlAndInitializePlayBack(videos)) {
                callback.onFailure(new Throwable("Unable to choose video link"), APIRequest.ERR_UN_KNOWN);
            }
            if (data != null && data.playerConfig != null) {
                PrefUtils.getInstance().setSeekBarEnable(data.playerConfig.seekEnabled);
                if (data.playerConfig.updateStatusIntervalInSec > 0) {
                    PrefUtils.getInstance().setUpdatesStatusIntervalInSec(data.playerConfig.updateStatusIntervalInSec);
                } else {
                    PrefUtils.getInstance().setUpdatesStatusIntervalInSec(4);
                }
            } else {
                PrefUtils.getInstance().setSeekBarEnable(true);
                PrefUtils.getInstance().setUpdatesStatusIntervalInSec(4);
            }
            if (data.mediaSessionToken != null) {
                PrefUtils.getInstance().setMediaSessionToken(data.mediaSessionToken);
            }

            if (getMediaControllerInstance() != null && getMediaControllerInstance().catchUpLinear != null) {
                if (mData != null && mData.content != null && mData.content.isSupportCatchup != null && !mData.content.isSupportCatchup.isEmpty()) {
                    ((ExoPlayerView) mVideoViewPlayer).initilizeMediaController();
                } else
                    getMediaControllerInstance().catchUpLinear.setVisibility(GONE);
            }
                if( data.videos !=null && data.videos.values!=null && data.videos.values.size()==0
                        && data.videos.message!=null && !TextUtils.isEmpty(data.videos.message)){
//                   callback.onFailure(new Throwable(mAPIResponseVideosMessage), APIRequest.ERR_UN_KNOWN);
                    AlertDialogUtil.showNeutralAlertDialog(mContext, data.videos.message, "", mContext.getResources()
                                    .getString(R.string.feedbackokbutton),
                            new AlertDialogUtil.NeutralDialogListener() {
                                @Override
                                public void onDialogClick(String buttonText) {
                                    AlertDialogUtil.dismissDialog();
                                    ((MainActivity) mContext).closePlayerFragment();
                                }
                            });
                    return;
                }

        }
    }

    private void fireAppInAppEvent() {
        StringBuilder genres = new StringBuilder();
        StringBuilder time_language = new StringBuilder();
        if (null != mData.content) {
            if (mData.content.genre != null && mData.content.genre.size() > 0) {
                for (CardDataGenre genre : mData.content.genre) {
                    genres.append(genre.name.toUpperCase() + " | ");
                }
                List<String> languages = mData.content.language;
                for (int i = 0; i < languages.size(); i++) {
                    String language = languages.get(i);
                    time_language.append(language.toUpperCase() + " | ");
                }
            }
        }
        if (mData != null
                && mData.publishingHouse != null
                && mData.publishingHouse.publishingHouseName != null
                && mData.generalInfo != null && mData.generalInfo.type != null) {
            CleverTap.eventAppInAppClick(mData.publishingHouse.publishingHouseName,
                    mData.generalInfo.title,
                    genres.toString(), time_language.toString(), mData.generalInfo.type, getSourceTab(), mSource, mSourceDetails);
        }
    }

    private void getSkipIntroInfo() {
/*        skipIntroStartPosition = 2000;
        skipIntroEndPosition = 16000;
        skipIntroJumpTo = 20000;
        skipIntroSkipDisplay = "Skip Intro";*/
        List<SkipIntro> skipIntros = mData.skipConfig.getSkipIntro();
        if (skipIntros != null && skipIntros.size() > 0 && skipIntros.get(0) != null) {
            skipIntroStartPosition = skipIntros.get(0).getSkipStartPosition() * 1000;
            skipIntroEndPosition = skipIntros.get(0).getSkipEndPosition() * 1000;
            skipIntroJumpTo = skipIntros.get(0).getJumpTo() * 1000;
            skipIntroSkipDisplay = skipIntros.get(0).getSkipDisplay();
        }

    }

    /*
    * "skipEndCredit":
              [
              {
              "skipType" : "scene",
              "startPosition" : "3000",
              "endPosition" : "0",
              "skipDisplay" : "SKIP END CREDIT",
              "description" : "SKIP end credit",
              "image" : "<link>",
              "action" : "autoPlay"
              }
            ]
    */
    private void getSkipEndCreditInfo() {
/*        skipEndStartPosition= 29000;
         skipEndEndPosition =59000;
        skipEndSkipDisplay= "Skip To End";*/
    }

    private void launchPartnerAppWithActionUrl(String actionUrl) {

        try {
            quitPlayer();
            launchPlayStoreToDownloadTheApp(actionUrl);
        } catch (Exception e) {
            quitPlayer();
            e.printStackTrace();
            launchPlayStoreToDownloadTheApp(actionUrl);
        }
    }

    private void launchPlayStoreToDownloadTheApp(String actionUrl) {
        try {
            isLaunchDeeplink = true;
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void launchPartnerWebViewWithActionUrl(String actionUrl) {
       /* if (mData != null
                && mData.publishingHouse != null
                && mData.publishingHouse.publishingHouseName != null) {
            Intent webViewIntent = FullscreenWebViewActivity.
                    createIntent(mContext, null,
                            false, true, actionUrl,
                            mData.publishingHouse.publishingHouseName);
            mContext.startActivity(webViewIntent);
        }*/
        isLaunchDeeplink = true;
        try {    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)));}  catch (Exception exception) {    exception.printStackTrace();}
    }

    String errorDetailedInfo = null;

    private boolean checkUrlAndInitializePlayBack(CardDataVideos videos) {
        LoggerD.debugHooqVstbLog("ChromeCast: checkUrlAndInitializePlayBack");
        if (mPlayerStatusListener != null && !mPlayerStatusListener.isFragmentVisible()) {
            LoggerD.debugHooqVstbLog("ChromeCast: checkUrlAndInitializePlayBack fragment is not visible");
            errorDetailedInfo = "Fragment is not visible";
            return false;
        }
        LoggerD.debugHooqVstbLog("ChromeCast: checkUrlAndInitializePlayBack fragment is visible playing content");
        if (videos != null
                && ("SUCCESS".equalsIgnoreCase(videos.status))
                && (videos.values != null)
                && (videos.values.size() > 0)) {
            String videoType = mData.generalInfo.type;
            //Log.d(TAG, "Video type " + videoType);
            if (mData.videoInfo != null && mData.videoInfo.cdnTypes != null) {
                if (mData.videoInfo.cdnTypes.contains(APIConstants.TYPE_YOUTUBE)) {
                    String youtubeId = getYoutubeLink(videos);
                    if (!TextUtils.isEmpty(youtubeId)) {
                        launchYoutubePlayer(youtubeId);
                        Analytics.gaNotificationPlayBackSuccess(mNotificationTitle, mNid, mData, profileSelect);
                        return true;
                    }
                }
            }
            /*if(videos != null && videos.values != null && videos.values.size()>0){
                CardDataVideosItem cardDataVideosItem =videos.values.get(0);
                if(cardDataVideosItem != null && !TextUtils.isEmpty(cardDataVideosItem.format) ){
                    if(cardDataVideosItem.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)){
                        chooseLiveStreamType(videos.values, false);
                        return true;
                    }else if(cardDataVideosItem.format.equalsIgnoreCase(APIConstants.STREAMDASH)){
                        url = chooseVodStreamType(videos.values);
                        if (TextUtils.isEmpty(url)) {
                            errorDetailedInfo = "Video URL is empty, failed to chooseVodStreamType";
                             return false;
                        }
                        initPlayBack(url);
                        return true;
                    }else{
                        return false;
                    }
                }
            }*/
            if (APIConstants.TYPE_LIVE.equalsIgnoreCase(videoType)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(videoType) || mIsTrailerPlaying) {
                chooseLiveStreamType(videos.values, false);
                //Log.d(TAG,"ChooseVideoStream live url:"+videos.values);
                return true;
            } else if (APIConstants.TYPE_VOD.equalsIgnoreCase(videoType)
                    || APIConstants.TYPE_MOVIE.equalsIgnoreCase(videoType)
                    || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(videoType)) {

                url = chooseVodStreamType(videos.values);
                //Log.d(TAG,"ChooseVideoStream live url:"+url);
                System.out.println("SELECTED PROFILE:: " + profileSelect);
                if (TextUtils.isEmpty(url)) {
                    errorDetailedInfo = "Video URL is empty, failed to chooseVodStreamType";
                    return false;
                }

                initPlayBack(url);
                return true;

            } else {
                chooseLiveStreamType(videos.values, false);
                return true;
            }
        }
        return false;
    }


    private MOUTracker mMouTracker;

    private boolean isPlayingDVR = false;
    private boolean isHelpScreenShowing = false;

    public void setFullScreenListener(PlayerFullScreen mListener) {
        this.mPlayerFullScreen = mListener;
    }

    public void setPlayerStatusUpdateListener(MiniPlayerStatusUpdate listener) {
        mPlayerStatusListener = listener;
    }

    protected MiniPlayerStatusUpdate mPlayerStatusListener;

    public void onStateChanged(int statePaused, int stopPosition) {
        if (mPlayerListener != null)
            mPlayerListener.onStateChanged(statePaused, stopPosition);
    }

    public void returnFromClickThrough() {

    }

    public void removeHandlerCallbacksIfAny() {
        contentProgressHandler.removeCallbacks(onEveryTimeInterval);
    }

    public void setNotificationTitle(String notificationTitle) {
        this.mNotificationTitle = notificationTitle;
    }

    public void setVODContentType(String cotentType) {
        this.mContentType = cotentType;
    }

    public void setStreamId(String mId) {
        streamId = mId;
    }


    /**
     * Request video ads using the default VAST ad tag. Typically, you would change your ad tag
     * URL based on the current content being played.
     */
    private void requestAds() {
        // replaceAdvertisingID();
        //requestAds(mDefaultAdTagUrl);
    }

    private AdvertisingIdClient.Info idInfo = null;
    private String advertId = null;


    public void getAdvertisingID() {
        /* if (Util.isNetworkAvailable(mContext)) {*/
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    advertId = idInfo.getId();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                return advertId;
            }

            @Override
            protected void onPostExecute(String advertId) {
                //TODO: when advertID is empty
                if (advertId != null && !advertId.isEmpty()) {
                    fetchUrl(null, advertId);
                } else {
                    fetchUrl(null, null);
                }
                return;
            }

        };
        task.execute();

        // }
    }


    private String getCustomADTagURL(String adTagUrl) {
        LoggerD.debugIMAads("adTagUrl- " + adTagUrl);


        String vid = mData._id;
        String url = "";
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getAppSharePwaUrl())) {
            url = /*PrefUtils.getInstance().getAppSharePwaUrl() +*/ mData.generalInfo.type + "/detail/" + mData._id + "/" + mData.generalInfo.title;
        } else {
            url = PWA_URL + mData.generalInfo.type + "/detail/" + mData._id + "/" + mData.generalInfo.title;
        }
        url = url.replaceAll("[|]", "%7C").replaceAll(" ", "%20");

        String title = "";
        if (!TextUtils.isEmpty(mData.getTitle())) {
            title = mData.getTitle().replaceAll("[|]", "%7C").replaceAll(" ", "%20");
        }

        int contentDurationMnts = Util.calculateDurationInSeconds(mData.content.duration) / 60;
        String contentDuration = "";
        if (contentDurationMnts > 0) {
            contentDuration = Util.getDurationString(contentDurationMnts);
        }

        String customAdData = "&vid=" + vid + "&url=" + url + "&cust_params=age%3D" + Util.getAgeString() +
                "%26gender%3D" + Util.getGenderString() +
                "%26content_type%3D" + mData.getType() +
                "%26content_id%3D" + mData._id +
                "%26duration%3D" + contentDuration +
                "%26content_language%3D" + getLanguage() +
                "%26user_type%3D" + Util.getUserType() +
                "%26content_name%3D" + title +
                "%26tags%3D" + "content tags" +
                "%26content_page%3D" + getSourceTab() +
                "%26consent_targeting%3D" + "Yes" +
                "%26source%3D" + "direct" +
                "%26video_watch_count%3D" + PrefUtils.getInstance().getAdVideoCount();

        adTagUrl = adTagUrl + customAdData;
        adTagUrl = adTagUrl.replaceAll(" ", "%20");

        //Log.d("AdTagUrl :::", adTagUrl+"");
        if (ApplicationController.SHOW_PLAYER_LOGS) {
            AlertDialogUtil.showAdAlertDialog(mContext, adTagUrl, "AD Logs", "Okay");
        }

        return adTagUrl;
    }

    private String getLanguage() {
        if (mData != null && mData.content != null && mData.content.language != null
                && !mData.content.language.isEmpty() && mData.content.language.get(0) != null) {
            return mData.content.language.get(0);
        }
        return "NA";
    }

    private void recycleAdDisplayContainer() {
        mAdDisplayContainer.setPlayer(null);
        mAdDisplayContainer.setAdContainer(null);
        mAdDisplayContainer = null;
    }
// Called when the content is completed.
//    private OnContentCompleteListener mOnContentCompleteListener;

    // VideoAdPlayer interface implementation for the SDK to send ad play/pause type events.

// [END init_region]
// [START content_progress_provider_region]

    // ContentProgressProvider interface implementation for the SDK to check content progress.
    private ContentProgressProvider mContentProgressProvider = new ContentProgressProvider() {
        @Override
        public VideoProgressUpdate getContentProgress() {
            /*LoggerD.debugIMAads("getContentProgress getCachedDuration- " + mVideoViewPlayer.getCachedDuration() +
                    " getCurrentPosition- " + mVideoViewPlayer.getCurrentPosition()
            );*/
            if (mIsAdDisplayed || mVideoViewPlayer.getCachedDuration() <= 0) {
                return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
            }

            return new VideoProgressUpdate(mVideoViewPlayer.getCurrentPosition(),
                    mVideoViewPlayer.getCachedDuration());

        }
    };

    /**
     * Pause the currently playing content video in preparation for an ad to play, and disables
     * the media controller.
     */
    public void pauseContentForAdPlayback() {
        savePosition();
        mVideoViewPlayer.closeSession();
    }

    private void savePosition() {
        if (mVideoViewPlayer == null) {
            return;
        }
        if (mIsAdDisplayed) {
            mSavedAdContentPosition = mVideoViewPlayer.getCurrentPosition();
            return;
        }
        if (mIsContentStarted) {
            mSavedContentPosition = mVideoViewPlayer.getCurrentPosition();
        }
    }

/////////////////////Playback helper////////////////////

    /**
     * Play/resume the selected video content.
     */
    public void playVideoContent(String AdURL) {
//        mData.content.isChromeCastEnabled = true;
        if (mCastManager != null && isConnected()
                && isCastSupportedContent()
                && !isToPlayOnLocalDevice
                && !isDownloadedContentPlayback && mContentVideoUri != null) {
            playContentOnCastTV(mContentVideoUri);
//            playVideoContent();
            return;
        }
        if (isConnected() && !isToPlayOnLocalDevice){
            closePlayer();
            return;
        }
        mIsAdDisplayed = false;
        //Setup video player for content playback.
        LoggerD.debugLog("playVideoContent");
        LoggerD.debugIMAads("playVideoContent:starting content playback ");
        showPlayerLoading();
        if (APIConstants.STREAMINGFORMATHLS.equals(mStreamingFormat)) {
            mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HLS);
        } else if (APIConstants.STREAMINGFORMATHTTP.equals(mStreamingFormat)) {
            mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
        } else if (APIConstants.STREAMINGFORMATRTSP.equals(mStreamingFormat)) {
            mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.RTSP);
        }

        mIsContentStarted = true;
        LoggerD.debugIMAads("playVideoContent: mSavedContentPosition- " + mSavedContentPosition);
        if (mIsTrailerPlaying) {
            mVideoViewPlayer.setPositionWhenPaused(0);
        } else {
            mVideoViewPlayer.setPositionWhenPaused((int) mSavedContentPosition);
        }
        if (mVideoViewPlayer != null) {
            if (isFromCatchup)
                mVideoViewPlayer.setLive(false);
            else
                mVideoViewPlayer.setLive(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                        || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type));
            mVideoViewPlayer.setCardData(mData);
        }

        if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)) {
            String url = mContentVideoUri.toString() /*+ "&ads.paln="+ MainActivity.nonceString*/;
            mContentVideoUri = Uri.parse(url);
        }

//        nextPositionToShowAdInMilliSeconds = mSavedContentPosition + MIDROLL_AD_CUE_POINT_IN_MINTS;
        mVideoViewPlayer.setDownloadMediaData(downloadMediaData);
        mVideoViewPlayer.setPlayerListener(mPlayerListener);

        mVideoViewPlayer.isPlayingAd(false);
        mVideoViewPlayer.setADTag(AdURL);
        mVideoViewPlayer.setAdContainer(mAdContainer);
        mVideoViewPlayer.setUri(mContentVideoUri, VideoViewPlayer.StreamType.VOD);
        MOUTracker.captureThePlaybackTriggerTime();
        bufferCount = 0;
        mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
        if (mVideoView != null) {
            mVideoView.setOnTouchListener(mOnVideoViewTouchListener);
        }
        mVideoViewParent.setVisibility(VISIBLE);
        mVideoViewParent.setOnClickListener(null);
        mVideoViewParent.setOnTouchListener(mOnVideoViewTouchListener);
        mVideoViewPlayer.setOnLicenseExpiryListener(onLicenseExpiryListener);

        allowMediaController(true);
        mVideoViewPlayer.setMinized(false);
        if (mVideoViewPlayer != null) {
            for (PlayerEventListenerInterface listenerInterface : playerEventsListeners) {
                mVideoViewPlayer.addPlayerEvent(listenerInterface);
            }
        }
    }

    private void handleAdEvent(AdEvent adEvent) {
        // Log.d("ImaAdsLoader",adEvent.getType()+"");
        switch (adEvent.getType()) {
            case TAPPED:
                if (mVideoViewPlayer.isMediaControllerShowing()) {
                    mVideoViewPlayer.hideMediaController();
                } else {
                    mVideoViewPlayer.showMediaController();
                }

                break;
            case LOADED:
                mIsAdPlaying = true;
                mIsAdDisplayed = true;
                ((ExoPlayerView) mVideoViewPlayer).handleAspectRatioOnFullScreen(true);
                mPreviewImage.setVisibility(INVISIBLE);
                mPreviewImageOverlay.setVisibility(INVISIBLE);
                mBackIconImageViewLayout.setVisibility(GONE);
                mBufferPercentage.setVisibility(GONE);
                mProgressBarLayout.setVisibility(GONE);
                mBackIconImageViewLayout.setVisibility(VISIBLE);
                // getView(R.id.grdient_container).setVisibility(View.VISIBLE);
                PlayerStreamingAnalytics.getInstance().notifyPlaying(mData, true, adEventEllapsedTime);
                ComScoreAnalytics.getInstance().setEventAdStarted(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                        adEvent.getType().name(), adEvent.getAd().isSkippable(), "No", "", String.valueOf(adEvent.getAd().getDuration()), "started", adEvent.getAd().getAdId());
                allowPlayPauseMediaControllers();
                if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                    YuppAnalytics.getInstance(mContext).handleAdStart(AdPosition.PREROLL);
                }

                break;
            case CONTENT_RESUME_REQUESTED:
                mIsAdPlaying = false;
                ((ExoPlayerView) mVideoViewPlayer).handleAspectRatioOnFullScreen(true);
                mIsAdDisplayed = false;
                if(mProgressBarLayout!=null && mProgressBarLayout.getVisibility()==VISIBLE){
                    mProgressBarLayout.setVisibility(GONE);
                }
                showMediaController();
                break;
            case CONTENT_PAUSE_REQUESTED:
                mIsAdPlaying = true;
                ((ExoPlayerView) mVideoViewPlayer).handleAspectRatioOnFullScreen(true);
                mPreviewImage.setVisibility(INVISIBLE);
                mPreviewImageOverlay.setVisibility(INVISIBLE);
                mBackIconImageViewLayout.setVisibility(GONE);
                mBufferPercentage.setVisibility(GONE);
                mProgressBarLayout.setVisibility(GONE);
                mBackIconImageViewLayout.setVisibility(VISIBLE);
                mReminderImage.setVisibility(INVISIBLE);
                // getView(R.id.grdient_container).setVisibility(View.VISIBLE);
                hideMediaController();
                mIsAdDisplayed = true;
                break;
            case COMPLETED:
                mIsAdPlaying = false;
                mIsAdDisplayed = false;
                PlayerStreamingAnalytics.getInstance().notifyEnded(mData, true, adEventEllapsedTime);
                ComScoreAnalytics.getInstance().setEventAdEnded(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                        adEvent.getType().name(), adEvent.getAd().isSkippable(), "No", "", String.valueOf(adEvent.getAd().getDuration()), "ended", adEvent.getAd().getAdId());
                if(mProgressBarLayout!=null && mProgressBarLayout.getVisibility()==VISIBLE){
                    mProgressBarLayout.setVisibility(GONE);
                }
                if(!((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup && !isCatchup)
                    previousNextChannels();
                else
                    setNextPreviousData();
                allowPlayPauseMediaControllers();
              resumeContentAfterAdPlayback();
                if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                    YuppAnalytics.getInstance(mContext).handleAdEnd(AdPosition.PREROLL.toString());
                }
                break;
            case SKIPPED:
                mIsAdPlaying = false;
                mIsAdDisplayed = false;
                if(!((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup && !isCatchup)
                    previousNextChannels();
                else
                    setNextPreviousData();
                allowPlayPauseMediaControllers();
                PlayerStreamingAnalytics.getInstance().notifyEnded(mData, true, adEventEllapsedTime);
                ComScoreAnalytics.getInstance().setEventAdSkipped(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                        adEvent.getType().name(), adEvent.getAd().isSkippable(), "yes", "", String.valueOf(adEvent.getAd().getDuration()), "skipped", adEvent.getAd().getAdId());
               resumeContentAfterAdPlayback();
                if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                    YuppAnalytics.getInstance(mContext).handleAdSkipped(AdPosition.PREROLL.toString());
                }

                break;
            case PAUSED:
                mIsAdDisplayed = false;
                PlayerStreamingAnalytics.getInstance().notifyPaused(mData, true, adEventEllapsedTime);
                break;
            case CLICKED:
                ComScoreAnalytics.getInstance().setEventAdClicked(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                        adEvent.getType().name(), adEvent.getAd().isSkippable(), "No", "", String.valueOf(adEvent.getAd().getDuration()), "clicked", adEvent.getAd().getAdId());
                break;
            case ALL_ADS_COMPLETED:
                mIsAdDisplayed = false;
                mIsAdPlaying = false;
                if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                    YuppAnalytics.getInstance(mContext).handleAdSkipped(AdPosition.PREROLL.toString());
                }
                break;
            case LOG:
                Map<String, String> adData = adEvent.getAdData();
                String message = "AdEvent: " + adData;
                Log.i("ImaAdsLoader", message);
                break;
            default:

                break;
        }
    }

    //setting exo player instance
    public ExoPlayerView exoPlayerInstance() {
        return (ExoPlayerView) mVideoViewPlayer;
    }

    public MediaController2 getMediaControllerInstance() {
        return mVideoViewPlayer.getMediaControllerInstance();
    }

    /**
     * Play/resume the selected video content.
     */
    public void playAdContent() {
        showPlayerLoading();
        //Configure a handler to monitor playback timeout.
        //Log.d(TAG, "playAdContent play adUri- " + mAdContentUrl);
//        playbackHandler.postDelayed(playbackRunnable, (long) (AD_TIME_OUT * 1000));
        mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
        mVideoViewPlayer.setPositionWhenPaused((int) mSavedAdContentPosition);
        mVideoViewPlayer.setPlayerListener(mPlayerListener);
        mVideoViewPlayer.isPlayingAd(true);
        mVideoViewPlayer.setUri(Uri.parse(mAdContentUrl), VideoViewPlayer.StreamType.VOD);
        mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
        mVideoView.setOnTouchListener(mOnVideoViewTouchListener);
        allowMediaController(false);
        mVideoViewPlayer.setMinized(true);
        mVideoViewPlayer.hideMediaController();
    }

    /**
     * Resume the content video from its previous playback progress position after
     * an ad finishes playing. Re-enables the media controller.
     */
    public void resumeContentAfterAdPlayback() {
        if (mContentVideoUri == null || "".equals(mContentVideoUri)) {
            LoggerD.debugIMAads("No content URL specified.");
            return;
        }

        if (!mIsContentComplete) {
            playVideoContent(null);
        } else {
//            mVideoPlayer.stopPlayback();
            quitPlayer();
        }
    }

    private void restorePosition() {
        mVideoViewPlayer.setPositionWhenPaused(mSavedContentPosition);
    }

    /**
     * An event raised when ads are successfully loaded from the ad server via an AdsLoader.
     */
    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
        // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
        // events for ad playback and errors.
        mAdsManager = adsManagerLoadedEvent.getAdsManager();
        LoggerD.debugIMAads("onAdsManagerLoaded:");

        // Attach event and error event listeners.
        mAdsManager.addAdErrorListener(this);
        mAdsManager.addAdEventListener(this);
        mAdsManager.init();
    }


    /**
     * Responds to AdEvents.
     */
    @Override
    public void onAdEvent(AdEvent adEvent) {
        LoggerD.debugIMAads("onAdEvent: Event- " + adEvent.getType());
        // These are the suggested event types to handle. For full list of all ad event types,
        // see the documentation for AdEvent.AdEventType.
       /* APIAnalytics.postAnalyticsEventsRequest(APIAnalytics.CATEGORY_DEKKHO_TV_ADS,
                APIAnalytics.DEKKHO_TV_AD_EVENT,
                "EVENT",
                String.valueOf(adEvent.getType()));*/

        switch (adEvent.getType()) {
            case LOADED:
                // AdEventType.LOADED will be fired when ads are ready to be played.
                // AdsManager.start() begins ad playback. This method is ignored for VMAP or ad
                // rules playlists, as the SDK will automatically start executing the playlist.
                mAdsManager.start();
                adsAvailableWithContent = true;
                ComScoreAnalytics.getInstance().setEventAdStarted(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                        adEvent.getType().name(), adEvent.getAd().isSkippable(), "No", String.valueOf(mAdsManager.getAdProgress().getCurrentTime()), String.valueOf(adEvent.getAd().getDuration()), "started", adEvent.getAd().getAdId());
                break;
            case CONTENT_PAUSE_REQUESTED:
                // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before a video ad is
                // played.
                pauseContentForAdPlayback();
                break;
            case CONTENT_RESUME_REQUESTED:
                // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is completed and you
                // should start playing your content.
                adsAvailableWithContent = false;
                resumeContentAfterAdPlayback();
                break;
            case ALL_ADS_COMPLETED:
                if (mAdsManager != null) {
                    mAdsManager.destroy();
                    mAdsManager = null;
                }
                break;
            case SKIPPED:
                if (mAdsManager != null) {
                    APIAnalytics.postAnalyticsEventsRequest(APIAnalytics.CATEGORY_DEKKHO_TV_ADS,
                            APIAnalytics.DEKKHO_TV_AD_SKIPPED,
                            adEvent.getAd().getTitle(),
                            String.valueOf(mAdsManager.getAdProgress().getCurrentTime()));
                    ComScoreAnalytics.getInstance().setEventAdSkipped(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                            adEvent.getType().name(), adEvent.getAd().isSkippable(), "yes", String.valueOf(mAdsManager.getAdProgress().getCurrentTime()), String.valueOf(adEvent.getAd().getDuration()), "skipped", adEvent.getAd().getAdId());
                }
                break;
            case CLICKED:
                if (mAdsManager != null) {
                    APIAnalytics.postAnalyticsEventsRequest(APIAnalytics.CATEGORY_DEKKHO_TV_ADS,
                            APIAnalytics.DEKKHO_TV_AD_CLICKED,
                            adEvent.getAd().getTitle(),
                            String.valueOf(mAdsManager.getAdProgress().getCurrentTime()));

                    ComScoreAnalytics.getInstance().setEventAdClicked(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                            adEvent.getType().name(), adEvent.getAd().isSkippable(), "No", String.valueOf(mAdsManager.getAdProgress().getCurrentTime()), String.valueOf(adEvent.getAd().getDuration()), "clicked", adEvent.getAd().getAdId());
                }
                break;
            case COMPLETED:
                if (mAdsManager != null) {
                    ComScoreAnalytics.getInstance().setEventAdEnded(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                            adEvent.getType().name(), adEvent.getAd().isSkippable(), "No", String.valueOf(mAdsManager.getAdProgress().getCurrentTime()), String.valueOf(adEvent.getAd().getDuration()), "ended", adEvent.getAd().getAdId());

                    ComScoreAnalytics.getInstance().setEventAdPlayed(mData, getSourceTab(), getSource(), getSourceDetails(), Integer.valueOf((int) getmSavedContentPosition()), getSourceCarouselPosition(),
                            adEvent.getType().name(), adEvent.getAd().isSkippable(), "No", String.valueOf(mAdsManager.getAdProgress().getCurrentTime()), String.valueOf(adEvent.getAd().getDuration()), adEvent.getAd().getAdId());
                }
                break;
            case LOG:
                Map<String, String> adData = adEvent.getAdData();
                String message = "AdEvent: " + adData;
                Log.i("ImaAdsLoader", message);
                break;
            default:
                break;
        }
    }

    /**
     * An event raised when there is an error loading or playing ads.
     */
    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        LoggerD.debugIMAads("onAdError adErrorEvent: " + adErrorEvent.getError().getMessage());
        if (mAdsManager != null && mAdsManager.getCurrentAd() != null && mAdsManager.getCurrentAd().getTitle() != null) {
            APIAnalytics.postAnalyticsEventsRequest(APIAnalytics.CATEGORY_DEKKHO_TV_ADS,
                    APIAnalytics.DEKKHO_TV_AD_EVENT,
                    mAdsManager.getCurrentAd().getTitle(),
                    String.valueOf(mAdsManager.getAdProgress().getCurrentTime()));
        }
        resumeContentAfterAdPlayback();
    }

    public boolean isMediaPaused() {
        return isMediaPaused;
    }

    public void queueListCardData(List<CardData> listQueueCardData) {
        LoggerD.debugLog("playNextItem listQueueCardData- " + listQueueCardData + " isTVShow- "
                + (mData != null
                && (mData.isTVSeries()
                || mData.isVODChannel()
                || mData.isVODYoutubeChannel()
                || mData.isTVEpisode()
                || mData.isVODCategory()
                || mData.isTVSeason()))
                + " isInPlayBackInitiatedMode- " + isInPlayBackInitiatedMode);
        if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null) {
            if(listQueueCardData != null) {
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.queueListCardData(listQueueCardData);
            }
        }
        this.mListQueueCardData = listQueueCardData;
        if (mData != null
                && (mData.isTVSeries()
                || mData.isVODChannel()
                || mData.isVODYoutubeChannel()
                || mData.isVODCategory()
                || mData.isTVSeason())) {
            if (isInPlayBackInitiatedMode)
                playContent();
        }
    }

    public void isToolbarShown(boolean isToShowToolBar) {
        this.isToolbarShown = isToShowToolBar;
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.isToolbarShown(isToShowToolBar);
        }
    }

    public int getYoutubePlayerPosition() {
        return youtubePlayer == null ? 0 : youtubePlayer.getCurrentTimeMillis();
    }

    public int getYoutubePlayerDuration() {
        return youtubePlayer == null ? 0 : youtubePlayer.getDurationMillis();
    }

    public void pauseYoutubePlayer() {
        if (youtubePlayer != null && isYouTubePlayerPlaying()) {
            youtubePlayer.pause();
        }
    }

    public boolean changeOrientationOnClose() {
        return changeOrientationOnClose;
    }

    public boolean isLoginRequestThroughPlayback() {
        return isLoginRequestThroughPlayback;
    }

    public void setSource(String mSource) {
        this.mSource = mSource;
    }

    public void setSourceDetails(String mSourceDetails) {
        this.mSourceDetails = mSourceDetails;
    }

    public String getSource() {
        return mSource;
    }

    public String getSourceDetails() {
        return mSourceDetails;
    }

    public void setSourceTab(String sourceTab) {
        this.mSourceTab = sourceTab;
    }

    public void setSourceCarouselPosition(int sourceCarouselPosition) {
        this.sourceCarouselPosition = sourceCarouselPosition;
    }

    public String getSourceTab() {
        return mSourceTab;
    }

    public int getSourceCarouselPosition() {
        return sourceCarouselPosition;
    }


    public void showSubtitleView() {
        if (mVideoViewPlayer == null) return;
        mVideoViewPlayer.showSubtitlesView();
    }

    public void hideSubtitleView() {
        if (mVideoViewPlayer == null) return;
        mVideoViewPlayer.hideSubtitlesView();
    }

    public boolean shouldShowSubtitleView() {
        return mVideoViewPlayer != null && mContext != null && mVideoViewPlayer.getSubtitleName() != null && !mContext.getString(R.string.subtitle_opt_none).equalsIgnoreCase(mVideoViewPlayer.getSubtitleName());
    }

    @Override
    public void setAdEvent(AdEvent adEvent) {
        handleAdEvent(adEvent);
    }


    public interface PlayerFullScreen {
        void playerInFullScreen(boolean value);
    }

    public enum VIDEO_PLAYER_TYPE {
        VIDEOVIEW,
        EXOVIDEOVIEW,
        HOOQ_PLAYERVIEW,
        HUNGAMA_PLAYER_VIEW
    }

    public static VIDEO_PLAYER_TYPE videoPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;

    public static VIDEO_PLAYER_TYPE getVideoPlayerType() {
        return videoPlayerType;
    }

    public static void setVideoPlayerType(VIDEO_PLAYER_TYPE videoPlayerType) {
        MiniCardVideoPlayer.videoPlayerType = videoPlayerType;
    }

    public MiniCardVideoPlayer(Context context, CardData data, String conId) {
        this.mContext = context;
        this.mData = data;
        streamId = conId;
        if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mListQueueCardData != null) {
            mListQueueCardData = ((MainActivity) mContext).mFragmentCardDetailsPlayer.mListQueueCardData;
        }
        mTrailerContentId = null;
        mIsTrailerPlaying = false;

        mInflator = LayoutInflater.from(mContext);
        Util.prepareDisplayinfo((Activity) mContext);

        int sdkVersion = Build.VERSION.SDK_INT;
        boolean isExoEnabled = PrefUtils.getInstance().getPrefIsExoplayerEnabled();
        boolean isDVRContent = PrefUtils.getInstance().getPrefIsExoplayerDvrEnabled();
//        if (mData != null
//                && mData.generalInfo != null
//                && mData.generalInfo.type != null
//                &&(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
//                || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM))) {

        if (sdkVersion >= 16 && (isDVRContent || isExoEnabled)) {
            videoPlayerType = VIDEO_PLAYER_TYPE.EXOVIDEOVIEW;
        } else {
            videoPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;
        }

//        }else{
//        videoPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;
//    }
        //initOrientaion(); //commented by sravani
        if (!Util.checkUserLoginStatus()
                || !isVideoHasMinDurationToShowAd()
                || TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxInStreamAdId())
                || !PrefUtils.getInstance().getPrefEnableVmaxInStreamAd()) {
            isVmaxAdEnabled = false;
        }
    }

    private void initOrientaion() {
        mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor
                        (Settings.System.ACCELEROMETER_ROTATION),
                true, new ContentObserver(new Handler()) {

                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        resumePreviousOrientaionTimer();
                    }
                });
    }


    public View CreatePlayerView(View parentLayout) {
        myplexEvent = MyplexEvent.getInstance(mContext);
        mParentLayout = parentLayout;
        final View v = mInflator.inflate(R.layout.cardmediasubitemvideo_mini_player, null);
        subtitleView = v.findViewById(R.id.subtitles_view);
        mVideoViewParent = (RelativeLayout) v;
        vmaxAdContainer = (FrameLayout) v.findViewById(R.id.vmax_ad_container);
        mBackIconImageViewLayout = v.findViewById(R.id.back_nav_icon_layout_2);
        back_nav_icon_error = v.findViewById(R.id.back_nav_icon_error);
        mWidth = ApplicationController.getApplicationConfig().screenWidth;

        ageRatingLayout = v.findViewById(R.id.age_rating_layout);
        fingerPrintText = v.findViewById(R.id.fingerPrintText);
        fingerPrintLayout = v.findViewById(R.id.fingerPrintLayout);
        ageRatingLayoutPortrait = v.findViewById(R.id.age_rating_layout_portrait);
        ageNumberTv = v.findViewById(R.id.age_number_tv);
        ageNumberTvP = v.findViewById(R.id.age_number_tv_p);
        ageDescriptionTv = v.findViewById(R.id.age_description_tv);
        ageDescriptionTvP = v.findViewById(R.id.age_description_tv_p);
        metaDataLayout = v.findViewById(R.id.meta_data_layout);
        blurLayout = v.findViewById(R.id.docBlurLayout);
        videoTitle = v.findViewById(R.id.video_title);
        videoDescription= v.findViewById(R.id.video_desc);

        mAdContainer = v.findViewById(R.id.adContainer);

        /*if(mCardData.isMovie()){
            mHeight = (mWidth * 4) / 3;
        }else {*/
        mHeight = getHeight(mWidth);
        //}

        previousChannelLLSub = v.findViewById(R.id.previous_content_ll_sub);
        nextChannelLLSub = v.findViewById(R.id.next_content_ll_sub);
        previousChannelNameSub = v.findViewById(R.id.previous_channel_name_sub);
        previousChannelProgramSub = v.findViewById(R.id.previous_program_name_sub);
        nextChannelNameSub = v.findViewById(R.id.next_channel_name_sub);
        nextChannelProgramSub = v.findViewById(R.id.next_program_name_sub);
        titleError = v.findViewById(R.id.title_error);
        titleError.setText(mContext.getResources().getString(R.string.subscription_required));
        subTitleError = v.findViewById(R.id.sub_title_error);
        String text = "<p><b><font color='black'>Please visit <br> Sun Direct Website</font><br>-------------</br><br>Call</br><br><font color='#FF6600'>+91 7601012345</font></br><br>with your Registered Mobile No</br></b></p>";
        subTitleError.setText(Html.fromHtml(text));

        try {
            playerexitscreen = v.findViewById(R.id.playerexitscreen);
           /* playerexitscreen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mPlayerStatusListener != null){
                        mPlayerStatusListener.onPlayerBackPressed();
                    }
                }
            });*/
            if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                playerexitscreen.setVisibility(VISIBLE);
            }else {
                playerexitscreen.setVisibility(GONE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mDebugTextView = (TextView) v.findViewById(R.id.debug_textView);
        debugTxtLayout = (LinearLayout) v.findViewById(R.id.debug_textView_layout);

        mPreviewImage = (ImageView) v
                .findViewById(R.id.cardmediasubitemvideo_imagepreview);
        mWaterMarkImage = (ImageView) v
                .findViewById(R.id.watermarklogo);
        mPreviewImageOverlay = v.findViewById(R.id.cardmediasubitemvideo_imagepreview_overlay);
        imageFrameOverlay = v.findViewById(R.id.image_overlay);
        mChromeCastToolbar = (Toolbar) v
                .findViewById(R.id.chromecast_toolbar);
        mAdPlayerContainer = (RelativeLayout) v.findViewById(R.id.video_player_with_ad_playback);
        mPlayerContainer = (RelativeLayout) v.findViewById(R.id.video_player_container);
        chromeCastBack= (ImageView) v.findViewById(R.id.chrome_cast_back);
        if(chromeCastBack!=null) {
            chromeCastBack.setVisibility(GONE);
        }

        if (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW) {
            mVideoView = (SurfaceView) v.findViewById(R.id.cardmediasubitemvideo_videopreview);
        } else if (videoPlayerType == VIDEO_PLAYER_TYPE.HOOQ_PLAYERVIEW) {
            mHooqVideoViewLayout = (FrameLayout) v.findViewById(R.id.cardmediasubitemvideo_hooq_videoview);
        } else if (videoPlayerType == VIDEO_PLAYER_TYPE.HUNGAMA_PLAYER_VIEW) {
            mHungamaVideoViewLayout = (RelativeLayout) v.findViewById(R.id.cardmediasubitemvideo_hungama_videoview);
            mHungamaVideoViewLayout.setOnTouchListener(mOnVideoViewTouchListener);
        } else {
            mVideoView = (SurfaceView) v.findViewById(R.id.cardmediasubitemvideo_exovideopreview);
            mVideoViewPlayer = (ExoPlayerView) mVideoView;
            mVideoViewPlayer.setStreamName(streamId);
            mVideoViewPlayer.setDebugTxtView(mDebugTextView);
        }
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.setmParentLayout(mParentLayout);
        }

        if (mVideoViewPlayer != null) {
            ((ExoPlayerView) mVideoViewPlayer).setMyPlexAdEventCallback(this);
        }

        if (videoPlayerType == VIDEO_PLAYER_TYPE.HOOQ_PLAYERVIEW) {
            mPlayerContainer.removeView(mHungamaVideoViewLayout);
            mPlayerContainer.removeView(mVideoView);
        } else if (videoPlayerType == VIDEO_PLAYER_TYPE.HUNGAMA_PLAYER_VIEW) {
            mPlayerContainer.removeView(mVideoView);
            mPlayerContainer.removeView(mHooqVideoViewLayout);
        } else {
            if (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW) {
                mPlayerContainer.removeView(v.findViewById(R.id.cardmediasubitemvideo_exovideopreview));
            } else {
                mPlayerContainer.removeView(v.findViewById(R.id.cardmediasubitemvideo_videopreview));
            }
            mPlayerContainer.removeView(mHooqVideoViewLayout);
            mPlayerContainer.removeView(mHungamaVideoViewLayout);
        }
        mAdPlayerContainer.removeView(vmaxAdContainer);
        mVideoViewParent.setOnClickListener(mPlayerClickListener);
        mReminderImage = (ImageView) v
                .findViewById(R.id.cardmediasubitemvideo_imagereminder);
        mThumbnailPlay = (ImageView) v
                .findViewById(R.id.cardmediasubitemvideo_play_icon);
        mReminderImage.setImageResource(R.drawable.oncard_set_reminder_icon);
        mNextEpisodePopupWindow = (FrameLayout) v.findViewById(R.id.next_episode_window);
        mNextEpisodeImage = (ImageView) v.findViewById(R.id.next_episode_image);
        mNextEpisodeTimer = (TextView) v.findViewById(R.id.next_episode_timer);
        mNextEpisodePlayIcon = (ImageView) v.findViewById(R.id.next_episode_play_icon);
        mGestureControllsLayoutContainer = v.findViewById(R.id.gesture_controlls_container);
        mediacontrolls = v.findViewById(R.id.mediacontrolls);
        mediacontrollsLive = v.findViewById(R.id.mediacontrollsLive);
        mRecyclerViewPlayer = v.findViewById(R.id.recyclerView);
        mSeason_icon = v.findViewById(R.id.season_icon);
        mTextViewErroFetch = (TextView) v.findViewById(R.id.error_message);
        skipIntroBtn = v.findViewById(R.id.btnskipIntro);
        playPauseBtn = v.findViewById(R.id.play_pause_btn);
        playLayout = v.findViewById(R.id.play_layout);
        closeBtn = v.findViewById(R.id.close_btn);
        channelName = v.findViewById(R.id.channelName);
        programName = v.findViewById(R.id.programName);
        previousContentLL = (RelativeLayout) v.findViewById(R.id.previous_content_ll);
        nextContentLL = (RelativeLayout) v.findViewById(R.id.next_content_ll);
        previousChannelName = (TextView) v.findViewById(R.id.previous_channel_name);
        previous_content_image = (ImageView) v.findViewById(R.id.previous_content_image);
        next_content_image = (ImageView) v.findViewById(R.id.next_content_image);
        previousProgramName = (TextView) v.findViewById(R.id.previous_program_name);
        nextChannelName = (TextView) v.findViewById(R.id.next_channel_name);
        nextProgramName = (TextView) v.findViewById(R.id.next_program_name);
        mRewindButtonContainer = (View) v.findViewById(R.id.rewind_container);
        mRewButton = (ImageView) v.findViewById(R.id.media_player_rewind_icon);
        mRewindText = (TextView) v.findViewById(R.id.media_player_rewind_text);
        mFfwdButtonContainer = (View) v.findViewById(R.id.fforward_container);
        mFfwdButton = (ImageView) v.findViewById(R.id.media_player_fowrard_icon);
        mFfwdText = (TextView) v.findViewById(R.id.media_player_ffword_text);

        float radius = 15f;

        View decorView = ((BaseActivity) mContext).getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();

        blurLayout.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur((BaseActivity)mContext))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false);

        mHandler = new Handler();
        skipIntroBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoViewPlayer != null && mVideoViewPlayer instanceof ExoPlayerView) {
                    mVideoViewPlayer.seekTo(skipIntroJumpTo);
                }
            }
        });
        nextContentLL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //mBackIconImageView.setVisibility(GONE);
                loadNextContent();
            }
        });
        back_nav_icon_error.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerStatusListener != null) {
                    mPlayerStatusListener.onPlayerBackPressed();
                }
            }
        });
        nextChannelLLSub.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setFullScreen(true);
                subscriptionErrorLayout.setVisibility(GONE);
                loadNextContent();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playContent();
                    }
                },5000);
            }
        });
        previousContentLL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // mBackIconImageView.setVisibility(GONE);
                loadPreviousContent();
            }
        });
        previousChannelLLSub.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionErrorLayout.setVisibility(GONE);
                setFullScreen(true);
                loadPreviousContent();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playContent();
                    }
                },5000);
            }
        });
        if (playLayout != null) {
            playLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getMediaControllerInstance() != null) {
                        getMediaControllerInstance().doPauseResume();
                        hideMediaController();
                        if (mVideoViewPlayer != null && mVideoViewPlayer.isPlaying()) {
                            if (playPauseBtn != null)
                                playPauseBtn.setImageResource(R.drawable.ic_dock_pause);
                        } else {
                            if (playPauseBtn != null)
                                playPauseBtn.setImageResource(R.drawable.ic_dock_play);
                        }
                    }
                }
            });
        }
        skipEndCreditBtn = v.findViewById(R.id.btnskipEnd);
        skipEndCreditBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoViewPlayer != null && mVideoViewPlayer instanceof ExoPlayerView) {
                    int duration = mVideoViewPlayer.getCachedDuration();
                    duration = duration - 5000;
                    mVideoViewPlayer.seekTo(duration);
                }
            }
        });
       /* if (mListQueueCardData != null && mRelatedVODData != null &&
                mRelatedVODData.generalInfo != null && mRelatedVODData.generalInfo.type != null && (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mRelatedVODData.generalInfo.type))) {
            mSeason_icon.setVisibility(View.VISIBLE);
        }*/
        if (mSeason_icon != null) {
            mSeason_icon.setOnClickListener(new OnClickListener() {
                                                public void onClick(View v) {

                                                    setSessionData();
                                                }
                                            }
            );

        }
        mbutton_Season = v.findViewById(R.id.button_season);
        mbutton_Season_2 = v.findViewById(R.id.button_season_2);
        mseasonList = v.findViewById(R.id.season_list);
        if (mbutton_Season != null) {
            mbutton_Season.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    if (mListSeasons == null) {
                    } else {
                        mbutton_SeasonUI.setVisibility(VISIBLE);
                        mbutton_SeasonUI.bringToFront();

                        mSeasonUI.bringChildToFront(mbutton_SeasonUI);

                        //                   mGestureControllsLayoutContainer.setVisibility(GONE);
                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.season_list, R.id.textView, mListSeasonNames);


                        mseasonList.setAdapter(arrayAdapter);

                        mseasonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                if (mAdapterEpisode != null) {
                                    mAdapterEpisode.removeAllEpisodesOnChangingSeason();
                                }
                                mSelectedSeasonPosition = position;
                                mIsLoadingMoreAvailable = true;
                                mStartIndex = 1;
                                fetchTVEpisodes();
                                if (mAdapterEpisode != null
                                        && mListSeasonNames != null
                                        && !mListSeasonNames.isEmpty())
                                    mAdapterEpisode.setCurrentSeasonName(mListSeasonNames.get(mSelectedSeasonPosition));
                                mbutton_SeasonUI.setVisibility(INVISIBLE);
                                //mSeason_icon.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });
        }
        if (mData != null
                && mData.startDate == null) {
            mReminderImage.setVisibility(INVISIBLE);
        } else if (null != mData && null != mData.startDate
                && null != mData.endDate) {
            Date startDate = Util.getDate(mData.startDate);
            Date endDate = Util.getDate(mData.endDate);
            Date currentDate = new Date();
            if ((currentDate.after(startDate)
                    && currentDate.before(endDate))
                    || currentDate.after(endDate)) {
//                mReminderImage.setImageResource(R.drawable.thumbnail_play_icon);
            }
        }
        mReminderImage.setTag(mData);
        ReminderListener reminderListener = new ReminderListener(mContext, null, mEpgDatePosition);
//        mReminderImage.setOnClickListener(reminderListener);

        mBufferPercentage = (TextView) v
                .findViewById(R.id.carddetaildesc_movename);
        ProgressBar mProgressBar = (ProgressBar) v.findViewById(R.id.cardmediasubitemvideo_progressbar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        Random rnd = new Random();
        int Low = 100;
        int High = 196;
        mBackIconImageView = (ImageView) getView(R.id.back_nav_icon);
        goLiveText = (TextView) getView(R.id.golive);
        back_nav_icon_3 = (ImageView) getView(R.id.back_nav_icon_3);
        back_nav_icon_layout = (LinearLayout) getView(R.id.back_nav_icon_layout);
        if (back_nav_icon_3 != null) {
            back_nav_icon_3.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_PORTRAIT);

                    mSeasonUI.setVisibility(INVISIBLE);
                    IS_SesonUIVisible = false;
                    mbutton_SeasonUI.setVisibility(INVISIBLE);
                    //mSeason_icon.setVisibility(View.VISIBLE);
                    back_nav_icon_3.setVisibility(INVISIBLE);
                    if (IS_PAUSE_MINICARD == true) {
                        mVideoViewPlayer.onPause();
                        IS_PAUSE_MINICARD = false;
                    } else {
                        mVideoViewPlayer.onResume();
                    }

                }
            });
        }
        int color = Color.argb(255, rnd.nextInt(High - Low) + Low,
                rnd.nextInt(High - Low) + Low, rnd.nextInt(High - Low) + Low);
        mPreviewImage.setBackgroundColor(color);
        mProgressBarLayout = (RelativeLayout) v
                .findViewById(R.id.cardmediasubitemvideo_progressbarLayout);
        this.layoutParamsOriginal = (RelativeLayout.LayoutParams) mProgressBarLayout.getLayoutParams();
        mErrorLayout = (RelativeLayout) v
                .findViewById(R.id.cardmediasubitemvideo_error);
        mSeasonUI = (RelativeLayout) v.findViewById(R.id.seasonUI);

        mbutton_SeasonUI = v.findViewById(R.id.button_SeasonUI);
        subscriptionErrorLayout = v.findViewById(R.id.subscription_error_layout);
        videoTitleError = v.findViewById(R.id.video_title_error);
        if (mbutton_SeasonUI != null) {
            mbutton_SeasonUI.setOnClickListener(mclickSeasonUIlist);
        }
        if (mSeasonUI != null) {
            mSeasonUI.setOnClickListener(mclickSeasonUI);
        }
        if (null != mData
                && mData.images != null) {
            for (CardDataImagesItem imageItem : mData.images.values) {
                if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null
                        && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI) && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
                    if (imageItem.link == null
                            || imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
                        mPreviewImage.setImageResource(0);
                    } else if (imageItem.link != null) {
                        PicassoUtil.with(mContext).load(imageItem.link, mPreviewImage, R.drawable.black);
                    }
                    break;
                }
            }
        }
        if (null != mData
                && null != mData._id
                && mData._id.equalsIgnoreCase("0")) {
            mVideoViewParent.setOnClickListener(null);
            mVideoView.setVisibility(INVISIBLE);
            mProgressBarLayout.setVisibility(INVISIBLE);
            mPreviewImage.setScaleType(ScaleType.CENTER);
            mPreviewImage.setBackgroundColor(Color.BLACK);
        } else {
            Util.showFeedback(mVideoViewParent);
        }
        try {
            mCastManager = CastContext.getSharedInstance(mContext).getSessionManager();
        } catch (Exception e) {
            e.printStackTrace();
            ((MainActivity) mContext).checkPlayServices();
        }
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.setGestureListener(this);
            /*if (mData != null && (mData.isLive() || mData.isProgram()))
                mVideoViewPlayer.setPlayerMediaControllsView(mediacontrollsLive);
            else*/
            mVideoViewPlayer.setPlayerMediaControllsView(mediacontrolls);

            mVideoViewPlayer.setPlayerTitleHeaderView(getView(R.id.layout_title));
            mVideoViewPlayer.setPlayerGestureControllsView(mGestureControllsLayoutContainer);
        }
        getView(R.id.back_nav_icon).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
                    MediaController2.rvAudioTracks.setVisibility(GONE);
                    if (isFromCatchup && isFullScreen) {
                        isFromCatchup = false;
                        if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null)
                            ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup = false;
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("contentTitle", "");
                        editor.commit();
                        showPlayerLoading();
                        if(mData != null && mData.globalServiceId != null)
                            getEPGData(mData.globalServiceId);
                        // previousNextChannels();
                        // playContent();
                        return;
                    }
                    if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
                        ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    } else {
                        ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    //((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_PORTRAIT);
                    if (mPlayerStatusListener != null) {
                        Util.showFeedback(view);
                        setMinimized(true);
                        mPlayerStatusListener.onPlayerBackPressed();
                        mThumbnailPlay.setVisibility(GONE);

                    }
                }else {
                    ((MainActivity) mContext).closePlayerFragment();
                }
            }
        });

        /*getView(R.id.golive).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFromCatchup && isFullScreen) {
                    isFromCatchup = false;
                    if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null)
                        ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup = false;
                    showPlayerLoading();
                    previousNextChannels();
                    playContent();
                    return;
                }
            }
        });
*/
        mBackIconImageViewLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 13/05/22
                // ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_PORTRAIT);
                if (mPlayerStatusListener != null) {
                    Util.showFeedback(v);
                    mPlayerStatusListener.onPlayerBackPressed();
                }
            }
        });
        mBackIconImageViewLayout.setVisibility(VISIBLE);
        if (getMediaControllerInstance() != null)
            getMediaControllerInstance().setMiniCardVideoPlayer(MiniCardVideoPlayer.this);
        try {
            ((ExoPlayerView) mVideoView).setOnAspectRatioChangeListener(new ExoPlayerView.OnAspectRatioChangeListener() {
                @Override
                public void contentAspectRatio(float aspectRatio) {

                    if (isFullScreen())
                        setAspectRatio(aspectRatio);
                }

                @Override
                public void frameAspectRatio(float aspectRatio) {
                    setframeAspectRatio();
                }


            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        return v;
    }
    public void getEPGData(String contentId){
        ChannelListEPG.Params params = new ChannelListEPG.Params(contentId, "", false, false);
        ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
            @Override
            public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                if (response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size() > 0) {
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        List<CardData> cardDataList = response.body().getResults().get(i).getPrograms();
                        if(cardDataList.size() >0){
                            playPreviousNextEpisode(cardDataList.get(0), false);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(channelListEPG);
    }
    public  void loadPreviousContent(){
        getMediaControllerInstance().audioPosition = 0;
        getMediaControllerInstance().subtitlePosition = 1;

        if (isFromCatchup) {
            ((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition = ((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition - 1;
            previousContentLL.setVisibility(GONE);
            playPreviousNextEpisode(previousCard, true);
            //  setNextPreviousData();
        }
        else {
            if (previousCard != null) {
                playPreviousNextEpisode(previousCard, false);
            }
        }
    }
    public void loadNextContent(){
        getMediaControllerInstance().audioPosition = 0;
        getMediaControllerInstance().subtitlePosition = 1;

        if (isFromCatchup) {
            ((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition = ((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition + 1;
            nextContentLL.setVisibility(GONE);
            playPreviousNextEpisode(nextCard, true);
            //  setNextPreviousData();
        } else {
            if (nextCard != null) {
                playPreviousNextEpisode(nextCard, false);
            }
        }
    }
    private void setAspectRatio(float contentAspectRatio) {
        mContentAspectRatio = contentAspectRatio;
        //commented the below line as it changing the video aspect ration when we change from portrait to landscape
        resetContentRatio();
        /*mParentLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                resetContentRatio();
            }
        });*/
    }

    public void setNextPreviousData(){
        if(getMediaControllerInstance() != null && getMediaControllerInstance().cardDataList != null) {
            if (((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition != 0 && getMediaControllerInstance().cardDataList.size()>((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition) {
                previousCard = getMediaControllerInstance().cardDataList.get(((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition - 1);
                if (previousCard != null) {
                    if (isFullScreen && !mIsAdPlaying && isCatchup)
                        previousContentLL.setVisibility(VISIBLE);
                    else
                        previousContentLL.setVisibility(INVISIBLE);
                    if (previousCard.globalServiceName != null) {
                        previousChannelName.setVisibility(VISIBLE);
                        previousChannelName.setText(previousCard.globalServiceName);
                        previous_content_image.setVisibility(VISIBLE);
                    }
                    if (previousCard.getTitle() != null) {
                        previousProgramName.setVisibility(VISIBLE);
                        previousProgramName.setText(previousCard.getTitle());
                    }

                }
            } else {
                previousContentLL.setVisibility(INVISIBLE);
            }
            if (getMediaControllerInstance().cardDataList.size()>((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition && ((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition != getMediaControllerInstance().cardDataList.size() - 1) {
                nextCard = getMediaControllerInstance().cardDataList.get(((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition + 1);
                if (isFullScreen && !mIsAdPlaying && isCatchup)
                    nextContentLL.setVisibility(VISIBLE);
                else
                    nextContentLL.setVisibility(INVISIBLE);
                if (nextCard.globalServiceName != null) {
                    nextChannelName.setVisibility(VISIBLE);
                    nextChannelName.setText(nextCard.globalServiceName);
                    next_content_image.setVisibility(VISIBLE);
                }
                if (nextCard.getTitle() != null) {
                    nextProgramName.setVisibility(VISIBLE);
                    nextProgramName.setText(nextCard.getTitle());
                }
            } else {
                nextContentLL.setVisibility(INVISIBLE);
            }
        }
    }

    public void allowPlayPauseMediaControllers(){
        if(isFullScreen && !mIsAdPlaying ) {
            getTextView(R.id.video_title).setVisibility(VISIBLE);
            getTextView(R.id.video_desc).setVisibility(VISIBLE);
        }
        else{
            getTextView(R.id.video_title).setVisibility(GONE);
            getTextView(R.id.video_desc).setVisibility(GONE);
            nextContentLL.setVisibility(GONE);
            previousContentLL.setVisibility(GONE);
        }
    }
    public void setSessionData(){
        mSeasonUI.setVisibility(VISIBLE);
        IS_SesonUIVisible = true;
        if (mVideoViewPlayer.isPlayerPaused()) {
            IS_PAUSE_MINICARD = true;
        }
      /*  if (mVideoViewPlayer.isPlaying()) {
            onPause();
        }*/
        //  back_nav_icon_3.setVisibility(View.VISIBLE);
        mSeason_icon.setVisibility(INVISIBLE);
        hideMediaController();
        hideAllControlls();
        isMediaControllerEnabled = false;
        mRelatedVODData = mRelatedCardData;
        if (mRelatedVODData != null) {
            if (mRelatedVODData.globalServiceId != null) {
                contentId = mRelatedVODData.globalServiceId;
            } else {
                contentId = mRelatedVODData._id;
            }
        }
        mStartIndex = 1;
        initUI(mListQueueCardData);//1 st this
        if (mRelatedVODData != null
                && mRelatedVODData.generalInfo != null
                && mRelatedVODData.generalInfo.type != null) {
            if(APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mRelatedVODData.generalInfo.type)){
                fetchTVEpisodes();
            }else if((APIConstants.TV_SERIES.equalsIgnoreCase(mRelatedVODData.generalInfo.type))) {
                fetchTVSeasons();
            }
        }

        if (mListSeasonNames == null) {
            mbutton_Season.setVisibility(GONE);
            mbutton_Season_2.setVisibility(GONE);

        } else {
            if (mListSeasonNames.size() > 5) {
                mbutton_Season.setVisibility(GONE);
                mbutton_Season_2.setVisibility(GONE);
            } else {
                mbutton_Season.setVisibility(GONE);
                mbutton_Season_2.setVisibility(GONE);

            }
        }

    }
    public void playNextEpisode(){
//        if(mAdapterEpisode.setEpisodeData())

    }

    private void resetContentRatio() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mParentLayout != null) {
                    float mDisplayAspectRatio = (float) mParentLayout.getWidth() / mParentLayout.getHeight();
                    if (mContentAspectRatio == 0 || mDisplayAspectRatio == 0) {
                        return;
                    }
                    if (mContentAspectRatio <= mDisplayAspectRatio) {
                        float currentWidth = mParentLayout.getHeight() * mContentAspectRatio;
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) currentWidth, mParentLayout.getHeight());
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        if (mVideoView != null && mVideoView.getHolder() != null) {
                            mVideoView.setLayoutParams(layoutParams);
                            mVideoView.getHolder().setFixedSize((int) currentWidth, mParentLayout.getHeight());
                        }
                    } else {
                        float currentHeight = mParentLayout.getWidth() * (1 / mContentAspectRatio);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mParentLayout.getWidth(), (int) currentHeight);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        if (mVideoView != null && mVideoView.getHolder() != null) {
                            mVideoView.setLayoutParams(layoutParams);
                            mVideoView.getHolder().setFixedSize(mParentLayout.getWidth(), (int) currentHeight);
                        }
                    }
                }
            }
        });

    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    public void handleFingerPrint() {
        if(mPlayerState == PLAYER_PLAY) {
            if (isMinimized) {
                fingerPrintLayout.setVisibility(GONE);
            } else {
                if (fingerPrintLayout.getVisibility() == VISIBLE)
                    fingerPrintLayout.setVisibility(GONE);
                else
                    fingerPrintLayout.setVisibility(VISIBLE);
            }
            RelativeLayout.LayoutParams trackingStripParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int left = 0, right, top = 0, bottom;
            if (!isFullScreen) {
                int mWidth;
                int mHeight;
                if (isMinimized) {
                    mWidth = (int) mContext.getResources().getDimension(R.dimen._95sdp);
                    mHeight = (int)mContext.getResources().getDimension(R.dimen._120sdp);
                } else {
                    mWidth = ApplicationController.getApplicationConfig().screenWidth;
                    mHeight = getHeight(mWidth);
                }
                left = new Random().nextInt((mWidth - 10) + 1) + 10;
                top = new Random().nextInt((mHeight - 10) + 1) + 10;
                if (left + fingerPrintLayout.getWidth() > mWidth) {
                    left = left - fingerPrintLayout.getWidth();
                }
                if (top + fingerPrintLayout.getHeight() > mHeight) {
                    top = top - fingerPrintLayout.getHeight();
                }
            } else {
                int mWidth = (ApplicationController.getApplicationConfig().screenWidth * 16) / 9;
                int mHeight = ApplicationController.getApplicationConfig().screenWidth;
                left = new Random().nextInt((mWidth - 10) + 1) + 10;
                top = new Random().nextInt((mHeight - 10) + 1) + 10;
                if (left + fingerPrintLayout.getWidth() > mWidth) {
                    left = left - fingerPrintLayout.getWidth();
                }
                if (top + fingerPrintLayout.getHeight() > mHeight) {
                    top = top - fingerPrintLayout.getHeight();
                }
            }
            trackingStripParams.setMargins(left, top, 0, 0);
            fingerPrintLayout.setLayoutParams(trackingStripParams);
        }
        mHandlerLoop.postDelayed( mStatusChecker, displayMillSecond);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            fingerPrintLayout.setVisibility(GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handleFingerPrint();
                }
            }, frequencyMillSecond);
        }
    } ;
   /* Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                //this function can change value of mInterval.
                if(mPlayerState == PLAYER_PLAY) {
                    if (isMinimized) {
                        fingerPrintLayout.setVisibility(GONE);
                    } else {
                        if (fingerPrintLayout.getVisibility() == VISIBLE)
                            fingerPrintLayout.setVisibility(GONE);
                        else
                            fingerPrintLayout.setVisibility(VISIBLE);
                    }
                    RelativeLayout.LayoutParams trackingStripParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int left = 0, right, top = 0, bottom;

                    *//*if (mData != null && mData.fingerPrint != null && mData.fingerPrint.getFrequencyInSeconds() != null)
                        delayMillSecond = mData.fingerPrint.getFrequencyInSeconds() * 1000;*//*
                    if (!isFullScreen) {
                        int mWidth;
                        int mHeight;
                        if (isMinimized) {
                            mWidth = (int) mContext.getResources().getDimension(R.dimen._95sdp);
                            mHeight = (int)mContext.getResources().getDimension(R.dimen._120sdp);
                        } else {
                            mWidth = ApplicationController.getApplicationConfig().screenWidth;
                            mHeight = getHeight(mWidth);
                        }
                        left = new Random().nextInt((mWidth - 10) + 1) + 10;
                        top = new Random().nextInt((mHeight - 10) + 1) + 10;
                        if (left + fingerPrintLayout.getWidth() > mWidth) {
                            left = left - fingerPrintLayout.getWidth();
                        }
                        if (top + fingerPrintLayout.getHeight() > mHeight) {
                            top = top - fingerPrintLayout.getHeight();
                        }
                    } else {
                        int mWidth = (ApplicationController.getApplicationConfig().screenWidth * 16) / 9;
                        int mHeight = ApplicationController.getApplicationConfig().screenWidth;
                        left = new Random().nextInt((mWidth - 10) + 1) + 10;
                        top = new Random().nextInt((mHeight - 10) + 1) + 10;
                        if (left + fingerPrintLayout.getWidth() > mWidth) {
                            left = left - fingerPrintLayout.getWidth();
                        }
                        if (top + fingerPrintLayout.getHeight() > mHeight) {
                            top = top - fingerPrintLayout.getHeight();
                        }
                    }
                    trackingStripParams.setMargins(left, top, 0, 0);
                    fingerPrintLayout.setLayoutParams(trackingStripParams);
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandlerLoop.postDelayed(mStatusChecker, frequencyMillSecond);
            }
        }
    };*/

    void stopRepeatingTask() {
        if (mHandlerLoop != null)
            mHandlerLoop.removeCallbacks(mStatusChecker);
    }

    public void setframeAspectRatio() {
        RelativeLayout.LayoutParams layoutParams;

        if (isFullScreen) {
            int navBarHeight = UiUtil.getNavBarHeight(mContext);

            if (DeviceUtils.isTablet(mContext)) {
                DisplayMetrics dm = new DisplayMetrics();
                ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
                ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
                ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;

                mWidth = ApplicationController.getApplicationConfig().screenWidth;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && UiUtil.hasSoftKeys(mContext)) {
                    mHeight = ApplicationController.getApplicationConfig().screenHeight + navBarHeight;
                } else {
                    mHeight = ApplicationController.getApplicationConfig().screenHeight;
                }
            } else {
                mWidth = ApplicationController.getApplicationConfig().screenHeight;
                mHeight = ApplicationController.getApplicationConfig().screenWidth;
            }

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else {
            DisplayMetrics dm = new DisplayMetrics();
            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
            //} else {
            mWidth = ApplicationController.getApplicationConfig().screenWidth;
            mHeight = getHeight(mWidth);

            layoutParams = new RelativeLayout.LayoutParams((int) mWidth, mHeight);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        }

/*
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) mWidth, mHeight);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
*/
        if (mVideoView != null && mVideoView.getHolder() != null) {
            mVideoView.setLayoutParams(layoutParams);
            // mVideoView.getHolder().setFixedSize((int) mWidth, mHeight);
        }
    }

    private int getHeight(int mWidth) {
        /*if (mCardData!=null&&mCardData.isMovie()){
            return (mWidth * 4) / 3;
        }else {*/
        // increased the player height
        return ((mWidth * 9) / 16);
        //}

    }

    public void setFullScreenEdgeToEdge(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
            activity.getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    public void resetFullScreenEdgeToEdge(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public void dismissedSeasonUI() {
        if (IS_SesonUIVisible) {
            mSeasonUI.setVisibility(INVISIBLE);
            IS_SesonUIVisible = false;
            mbutton_SeasonUI.setVisibility(INVISIBLE);
            mSeason_icon.setVisibility(INVISIBLE);
            mVideoViewPlayer.onResume();
            IS_SeasonUIForBack = true;
        }
    }

    private void initUI(final List<CardData> mListQueueCardData) {
        mRecyclerViewPlayer.setItemAnimator(null);
        mAdapterEpisode = new AdapterEpisodePlayer(mContext, new ArrayList<>(), MiniCardVideoPlayer.this);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewPlayer.setLayoutManager(layoutManager);
        mRecyclerViewPlayer.setAdapter(mAdapterEpisode);
        mRecyclerViewPlayer.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dx > 1) {
                    if (mRecyclerViewPlayer == null) return;

                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerViewPlayer.getLayoutManager();
                    int totalItemCount = linearLayoutManager.getItemCount();

                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (mIsLoadingMoreAvailable && !isLoadingMoreRequest && (totalItemCount - 1 <= lastVisibleItem/* + visibleThreshold*/)) {
                        mStartIndex++;
                        isLoadingMoreRequest = true;

                        if (APIConstants.TV_SERIES.equalsIgnoreCase(mRelatedVODData.generalInfo.type) || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mRelatedVODData.generalInfo.type)) {
                            fetchTVEpisodes();
                            return;
                        }
                        fetchRelatedVODData(false);
                    }

                }

            }

        });

    }

    private void fetchRelatedVODData(boolean isCachRequest) {
//        if (true/*getArguments() != null && !getArguments().containsKey(FragmentCarouselViewAll.PARAM_FROM_VIEW_ALL)*/) {
            mCacheManager.getRelatedVODList(contentId, mStartIndex, isCachRequest,10,
                    MiniCardVideoPlayer.this);
//            return;
//        }
//        fetchCarouselData();
    }

    private void fetchTVSeasons() {
        if (contentId == null) {
            return;
        }
        mCacheManager.getRelatedVODListTypeExclusion(contentId, 1, true, APIConstants.TYPE_TVSEASON,
                10,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            showNoDataMessage();
                            return;
                        }
                        mListSeasons = dataList;
                        updateSeasons(dataList);
                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            showNoDataMessage();
                            return;
                        }
                        mListSeasons = dataList;
                        updateSeasons(dataList);
                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        showNoDataMessage();
                    }
                });
    }

    private void updateSeasons(List<CardData> dataList) {
        if (dataList == null
                || dataList.isEmpty()) {
            return;
        }

        contentId = dataList.get(dataList.size()-1)._id;
        //  dataList = sortDataList(dataList);
       /* mListSeasonNames = prepareSeasonNames(dataList);
        if (mListSeasonNames == null || mListSeasonNames.isEmpty()) {
            SDKLogger.debug("Unable to load season names possible cause might be serialNo from server is null in prepareSeasonName() -> CardData.get(index).content.serialNo");
            return;
        }*/
        fetchTVEpisodes();
    }


    private void fetchTVEpisodes() {
        if (mRelatedVODData == null) {
            return;
        }
//        CardData seasonData = mListSeasons.get(mSelectedSeasonPosition);
//        updateDropDownTitle();
        //  showProgressBar();
        mCacheManager.getRelatedVODList(contentId, mStartIndex, false,10,
                MiniCardVideoPlayer.this);
    }

    private void updateDropDownTitle() {
        if (mbutton_Season != null
                && !mListSeasonNames.isEmpty()) {
            mbutton_Season.setText(mListSeasonNames.get(mSelectedSeasonPosition) + "  ");


        } else {
            mbutton_SeasonUI.setVisibility(INVISIBLE);
            mSeasonUI.setVisibility(INVISIBLE);
            IS_SesonUIVisible = false;

        }
    }

    private void fillData(List<CardData> vodListData) {

        if (vodListData != null
                && vodListData.isEmpty()) {
            showNoDataMessage();
            return;
        }
        if(mAdapterEpisode == null){
            mAdapterEpisode = new AdapterEpisodePlayer(mContext, vodListData, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            mRecyclerViewPlayer.setLayoutManager(layoutManager);
            mRecyclerViewPlayer.setAdapter(mAdapterEpisode);
        }else {
            mAdapterEpisode.addAll(vodListData,false);
        }

        //    mAdapterEpisode.setListener(new AdapterEpisodePlayer.AdapterDataChange() {
        //      @Override
        //    public void onDataChange() {
        //      //postResizeFragmentHeight();
        //}
        //   });

        if (mListSeasonNames != null
                && !mListSeasonNames.isEmpty()) {
            mAdapterEpisode.setCurrentSeasonName(mListSeasonNames.get(mSelectedSeasonPosition));
        }

        mAdapterEpisode.setCarouselInfoData(mCarouselInfoData);
        mAdapterEpisode.setRelatedVODData(mRelatedVODData);
        mAdapterEpisode.setTVShowData(mRelatedVODData);
    }

    private void fetchCarouselData() {
        if (mCarouselInfoData == null) {
            return;
        }
        final int pageSize = mCarouselInfoData.pageSize > 0 ? mCarouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT;
        new MenuDataModel().fetchCarouseldata(mContext, mCarouselInfoData.name, mStartIndex, pageSize, true, mCarouselInfoData.modified_on, new MenuDataModel.CarouselContentListCallback() {
            @Override
            public void onCacheResults(List<CardData> dataList) {
                if ((dataList == null || dataList.isEmpty())) {
                    showNoDataMessage();
                    return;
                }
                fillData(dataList);
            }

            @Override
            public void onOnlineResults(List<CardData> dataList) {
                if ((dataList == null || dataList.isEmpty())) {
                    showNoDataMessage();
                    return;
                }
                fillData(dataList);
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    showNoDataMessage();
                    return;
                }
                showNoDataMessage();
            }
        });
    }

    private void showNoDataMessage() {
        if (mTextViewErroFetch != null) {
            if (mTextViewErroFetch != null) {
                if (mRelatedVODData != null && mRelatedVODData.generalInfo != null && !APIConstants.TYPE_VODCHANNEL.equals(mRelatedVODData.generalInfo.type)) {
                    mTextViewErroFetch.setText(mContext.getString(R.string.error_fetch_videos));
                }
                if (mRecyclerViewPlayer != null)
                    mRecyclerViewPlayer.setVisibility(View.GONE);
                mTextViewErroFetch.setVisibility(VISIBLE);
            }
        }
    }

    private OnClickListener mPlayerClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
           /* ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                setFullScreen(true);
                playInLandscape();
                playContent();
            }*/
            // commented the above code  to support the potrait mode
            if(mIsContentComplete && !PrefUtils.getInstance().getSeekBarEnable()) {
                if (((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null ) {
                    ((MainActivity) mContext).mFragmentCardDetailsPlayer.getCOntentDetailMultimedia();
                    return;
                }
            }
            if(isCastPopupShowing){
                return;
            }else{
                mThumbnailPlay.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playContent();
                    }
                });
            }
//            playContent();
        }
    };


    private void fetchUserId() {
        // AlertDialogUtil.showProgressAlertDialog(mContext);
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            int mAPIErrorCode;

            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                // AlertDialogUtil.dismissProgressAlertDialog();
                if (response == null || response.body() == null) {
                    onFailure(new Throwable("Invalid or null response"), 0);
                    //Log.d(TAG, "fetchUserId null ");
                    return;
                }
                mApiResponseVideosStatus = response.body().status;
                mAPIResponseVideosMessage = response.body().message;
                if (response.body().code == 402) {
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate(PLAY_ERR_NON_LOGGED_USER);
                    }
                    return;
                }
                if (response.body().code == 200) {
                    mAPIErrorCode = response.body().code;
                    UserProfileResponseData responseData = response.body();
                    if (responseData != null
                            && responseData.result != null
                            && responseData.result.profile != null) {
                        Analytics.mixpanelIdentify();
                        PrefUtils.getInstance().setPrefUserId(responseData.result.profile._id);
                        if (!TextUtils.isEmpty(responseData.result.profile.serviceName)) {
                            PrefUtils.getInstance().setServiceName(responseData.result.profile.serviceName);
                        }
                        if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                            if (responseData.result.profile.locations.get(0) != null
                                    && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                                PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
                        }

                        if (responseData.result.profile.state != null && !TextUtils.isEmpty(responseData.result.profile.state)) {
                            PrefUtils.getInstance().setUserState(responseData.result.profile.state);
                        }

                        if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
                            PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
                        }

                        PrefUtils.getInstance().setServiceName(responseData.result.profile.serviceName);
                        playContent();
//                        PlayUtils.initialization((Activity) mContext);
                        //Log.d(TAG, "fetchUserId profile._id " + responseData.result.profile._id);
                        return;
                    }
                }
                onFailure(new Throwable(mAPIResponseVideosMessage), 0);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                // AlertDialogUtil.dismissProgressAlertDialog();
                String errorMessage = mContext.getString(R.string.canot_fetch_url);
                if (!ConnectivityUtil.isConnected(mContext)) {
                    errorMessage = mContext.getString(R.string.network_error);
                }
                HashMap properties = new HashMap<String, String>();
                String contentPartnerName = mData == null || mData.publishingHouse == null || mData.publishingHouse.publishingHouseName == null ? "NA" :
                        mData.publishingHouse.publishingHouseName;
                if (contentPartnerName.equalsIgnoreCase(APIConstants.NOT_AVAILABLE)) {
                    contentPartnerName = mData == null || mData.contentProvider == null ? "NA" : mData.contentProvider;
                }
                properties.put(Constant.CONTENT_PARTNER_NAME, contentPartnerName);
                properties.put(Constant.CONTENT_ID, mData == null || mData._id == null ? "NA" : mData._id);
                if (mData != null && mData.generalInfo != null && mData.generalInfo.type != null) {
                    properties.put(ERROR_CONTENT_TYPE, mData.generalInfo.type);
                }
                if (mPlayerStatusListener != null
                        && mPlayerStatusListener.isFragmentVisible()) {
                    AlertDialogUtil.showToastNotification(errorMessage);
                }
                if (t != null && t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                if (!TextUtils.isEmpty(mAPIResponseVideosMessage) && !"OK".equalsIgnoreCase(mAPIResponseVideosMessage)) {
                    errorMessage = mAPIResponseVideosMessage;
                }
                constructStackTrace(errorMessage);
                logErrorParams(Constant.ERROR_CATEGORY.API,
                        (mApiResponseVideosStatus != null && mAPIResponseVideosMessage != null) ? (mAPIResponseVideosMessage + " | " + mApiResponseVideosStatus) : Constant.PLAYBACK_ERROR_FETCHING,
                        mStreamingFormat, (mData == null || mData.content == null || mData.content.drmType == null ? "NA" : mData.content.drmType), (url == null ? "NA" : url),
                        (mData == null || mData.content == null || mData.content.drmEnabled ? "NA" : String.valueOf(mData.content.drmEnabled)),
                        errorMessage,
                        String.valueOf(mAPIErrorCode) == null ? "NA" : String.valueOf(mAPIErrorCode),
                        Constant.API_CATEGORY, stackTrace, properties);

            }
        });
        APIService.getInstance().execute(userProfileRequest);

    }


    public void playContent() {
        isLoginRequestThroughPlayback = false;
        try {
            isNextEpisodePopupEnabled = false;
            String publishingHouseId = PrefUtils.getInstance().getString(String.valueOf(mData.getPublishingHouseId()));
            if (!TextUtils.isEmpty(publishingHouseId)) {
                isNextEpisodePopupEnabled = true;
                popUpPercentage = Integer.parseInt(publishingHouseId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mPlayerStatusListener != null) {
            mPlayerStatusListener.playerStatusUpdate("User id:: " + PrefUtils.getInstance().getPrefUserId());
            if (mData != null && mData._id != null) {
                mPlayerStatusListener.playerStatusUpdate("Content id:: " + mData._id);
            }


            LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
            String postalCode = locationInfo == null || locationInfo.postalCode == null ? "NA" : locationInfo.postalCode;
            String country = locationInfo == null || locationInfo.country == null ? "NA" : locationInfo.country;
            String area = locationInfo == null || locationInfo.area == null ? "NA" : locationInfo.area;
            mPlayerStatusListener.playerStatusUpdate("Postal Code:: " + postalCode);
            mPlayerStatusListener.playerStatusUpdate("Country Code:: " + country);
            mPlayerStatusListener.playerStatusUpdate("Area:: " + area);
            mPlayerStatusListener.playerStatusUpdate("Quality:: " +PrefUtils.getInstance().getPrefPlayBackQuality());
        }

        if(mData!=null && mData.generalInfo!=null && mData.generalInfo._id!=null) {
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("contentTitle",  mData.generalInfo._id).apply();
        }
        if (canBePlayed()) {
            if (ApplicationController.SHOW_PLAYER_LOGS) {
                debugTxtLayout.setVisibility(VISIBLE);
            }
            if (isVmaxAdEnabled && enableVMAXMidrollAds && !isTimerScheduled) {
                isTimerScheduled = true;
                timer.schedule(timerTask, 0, 1000);
            }
            if (mData != null
                    && (mData.isTVSeries()
                    || mData.isVODChannel()
                    || mData.isVODYoutubeChannel()
                    || mData.isVODCategory()
                    || mData.isTVSeason())) {
                if (mListQueueCardData != null
                        && !mListQueueCardData.isEmpty()) {
                    boolean playFullScreen = false;
                    if (mData.playFullScreen) {
                        playFullScreen = true;
                    }
                    mData = getEpisodeData();
                    if (mData == null) {
                        return;
                    }
                    mData.playFullScreen = playFullScreen;
                    SDKLogger.debug("mData is null- " + mData);
                    //  it is raising the issue of updating the meta data
                 /*   if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.onUpdatePlayerData(mData);
                    }*/
                    playContent();
                    return;
                }
                isInPlayBackInitiatedMode = true;
                showPlayerLoading();
                return;
            }
            String publishingHouse = mData == null
                    || mData.publishingHouse == null
                    || TextUtils.isEmpty(mData.publishingHouse.publishingHouseName) ? null : mData.publishingHouse.publishingHouseName;
            if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA)) {
                int userid = PrefUtils.getInstance().getPrefUserId();
                String serviceName = PrefUtils.getInstance().getServiceName();
                if (TextUtils.isEmpty(serviceName)) {
                    fetchUserId();
                    return;
                }
                Analytics.mixpanelIdentify();
                String partnerSignUpStatus = PrefUtils.getInstance().getPrefPartenerSignUpStatus();
                if (!APIConstants.SUCCESS.equalsIgnoreCase(partnerSignUpStatus)) {
                    //AlertDialogUtil.showProgressAlertDialog(mContext);
                    HungamaPartnerHandler.getInstance(mContext).doSignUP(APIConstants.TYPE_HUNGAMA, new HungamaPartnerHandler.SignUpListener() {
                        @Override
                        public void onSignUpSuccess(int code, String message) {
                            //AlertDialogUtil.dismissProgressAlertDialog();
                            PrefUtils.getInstance().setPrefPartenerSignUpStatus(APIConstants.SUCCESS);
                            playContent();
                        }

                        @Override
                        public void onSignUpFailure() {
                            //AlertDialogUtil.dismissProgressAlertDialog();
                            String errorMessage = mContext.getString(R.string.canot_fetch_url);
                            if (!ConnectivityUtil.isConnected(mContext)) {
                                errorMessage = mContext.getString(R.string.network_error);
                            }
                            if (mPlayerStatusListener != null
                                    && mPlayerStatusListener.isFragmentVisible()) {
                                AlertDialogUtil.showToastNotification(errorMessage);
                            }
                        }
                    });
                    return;
                }
            }

            isPlayBackStartedAlready = false;
            isToPlayOnLocalDevice = false;
            mIsContentComplete = false;
            isInPlayBackInitiatedMode = true;
            mVideoViewParent.setOnClickListener(null);
            initCastConsumer();
            if (DeviceUtils.isTablet(mContext)) {
                resumePreviousOrientaionTimer();
            }
          /*  if(!isStaticAdEnabled)
                showAdDialog();
            else*/
            //Handled the player loading when the Rent a button is shwoing
            if(PrefUtils.getInstance().getSubscriptionStatusString()!=null ) {
                if (PrefUtils.getInstance().getSubscriptionStatusString().equalsIgnoreCase(APIConstants.USER_NOT_SUBSCRIBED)
                        && mData !=null && mData.generalInfo!=null&& mData.generalInfo.contentRights!=null
                        && mData.generalInfo.contentRights.size()>0 &&mData.generalInfo.contentRights.get(0)!=null
                && mData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD) ) {
                    mThumbnailPlay.setVisibility(GONE);
                } else {
                    getAdvertisingID();
                }
            }

        }
    }

    private static String getMimeType(String fileUrl) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private void showAdDialog(APICallback callback, APIResponse<CardResponseData> response, AdFullScreenListResponse adFullScreenListResponse) {
        // isStaticAdEnabled = true;
        Dialog dialog = new Dialog(mContext);
        //  dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_alert_dailog);

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.BLACK));
        //  dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        ;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        ;

        window.setAttributes(lp);
        lp.gravity = Gravity.FILL;

        ImageView imageView = (ImageView) dialog.findViewById(R.id.image_view);
        ProgressBar progress = dialog.findViewById(R.id.progress);
        ImageView closeIcon = (ImageView) dialog.findViewById(R.id.close_icon_alert);
        TextView skip_text = (TextView) dialog.findViewById(R.id.skip_text);


        Handler myHandler = new Handler();
        Runnable myRunnable = () -> {
            if (!((Activity) mContext).isFinishing())
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    setPlayerData(callback, response);
                }
        };

        if (adFullScreenListResponse != null && adFullScreenListResponse.getAdFullScreenConfig() != null && adFullScreenListResponse.getAdFullScreenConfig().size() > 0) {
            if (adFullScreenListResponse.getAdFullScreenConfig().get(0).getImageUrl() != null) {
                imageView.setVisibility(VISIBLE);
                Picasso.get().load(adFullScreenListResponse.getAdFullScreenConfig().get(0).getImageUrl())
                        .into(imageView);
                long delayMillSecond = 5000;
                if (adFullScreenListResponse.getAdFullScreenConfig().get(0).getDurationInSeconds() != null)
                    delayMillSecond = adFullScreenListResponse.getAdFullScreenConfig().get(0).getDurationInSeconds() * 1000;

                myHandler.postDelayed(myRunnable, delayMillSecond);
                if (!((Activity) mContext).isFinishing())
                    dialog.show();
            }
            if (adFullScreenListResponse.getAdFullScreenConfig().get(0).getSkippable() != null && adFullScreenListResponse.getAdFullScreenConfig().get(0).getSkippable().equalsIgnoreCase("true"))
                skip_text.setVisibility(VISIBLE);
            else
                skip_text.setVisibility(GONE);

            if (adFullScreenListResponse.getAdFullScreenConfig().get(0).getSkipText() != null)
                skip_text.setText(adFullScreenListResponse.getAdFullScreenConfig().get(0).getSkipText());

      /*  imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adFullScreenListResponse.getAdFullScreenConfig().get(0).getActionUrl() != null)
                    ((BaseActivity) mContext).startActivityForResult(SubscriptionWebActivity.createIntent( ((BaseActivity) mContext), adFullScreenListResponse.getAdFullScreenConfig().get(0).getActionUrl(), SubscriptionWebActivity.PARAM_LAUNCH_NONE), 100);
            }
        });*/
            skip_text.setOnClickListener(v -> {
                mHandler.removeCallbacks(myRunnable);
                //isStaticAdEnabled = false;
                setPlayerData(callback, response);
                dialog.dismiss();

            });

        }

    }


    private CardData getEpisodeData() {
        for (CardData episode :
                mListQueueCardData) {
            if (episode != null
                    && !(episode.isTVSeries()
                    || episode.isVODChannel()
                    || episode.isVODYoutubeChannel()
                    || episode.isVODCategory()
                    || episode.isTVSeason())) {
                return episode;
            } else {
                LoggerD.debugLog("getEpisodeData episode- " + episode);
            }
        }
/*        try {
            // Crashlytics.logException(new Throwable("Episode is not found in queue data"));
        } catch (Throwable t) {
            t.printStackTrace();
        }*/
        return null;
    }

    private void initializeHooqPlayback() {

        //                TODO Optimize the condition for hooq content.
        if (!isToPlayOnLocalDevice && mCastManager != null && isConnected() && PrefUtils.getInstance().gePrefEnableHOOQChromeCast()) {

            return;
        }
        if (mPlayerStatusListener != null
                && mPlayerStatusListener.isFragmentVisible()
                || mHooqVideoViewLayout == null) {
            closePlayer();
            AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.canot_fetch_url));
            return;
        }

        if (isVmaxAdEnabled && PrefUtils.getInstance().getPrefEnableVmaxPreRollAd()) {
            showPlayerLoading();
            /*if (vmaxAdView == null)
                loadInstreamVideo();
            else
                vmaxAdView.cacheAd();*/
            return;
        }
        playHooqPlayback();
    }

    private boolean isVideoHasMinDurationToShowAd() {
        int minVideoDurationToShowAds = PrefUtils.getInstance().getPrefVmaxVideoAdMinDuration();
        int contentDuration = 0;
        try {
            if (mData != null)
                contentDuration = mData.getDurationInMints();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentDuration >= minVideoDurationToShowAds;
    }

    private void playHooqPlayback() {
        if (mPlayerStatusListener != null
                && mPlayerStatusListener.isFragmentVisible()
                && mHooqVideoViewLayout == null) {
            closePlayer();
            AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.canot_fetch_url));
            return;
        }
        if (!isMediaPlaying()) {
            showPlayerLoading();
        }

        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.setGestureListener(this);
            mVideoViewPlayer.setPositionWhenPaused(mSavedContentPosition);
          /*  if ((mData.isLive() || mData.isProgram()))
                mVideoViewPlayer.setPlayerMediaControllsView(mediacontrollsLive);
            else*/
            mVideoViewPlayer.setPlayerMediaControllsView(mediacontrolls);

            mVideoViewPlayer.setPlayerTitleHeaderView(getView(R.id.layout_title));
            mVideoViewPlayer.setPlayerGestureControllsView(mGestureControllsLayoutContainer);
        }
        mVideoViewPlayer.setDebugTxtView(mDebugTextView);
        if (mHooqVideoViewLayout.indexOfChild(mVideoViewPlayer.getView()) == -1)
            mHooqVideoViewLayout.addView(mVideoViewPlayer.getView());
        mHooqVideoViewLayout.setOnTouchListener(mOnVideoViewTouchListener);
        mVideoViewParent.setOnTouchListener(mOnVideoViewTouchListener);
        mVideoViewPlayer.setPlayerListener(mPlayerListener);
        mHooqVideoViewLayout.setVisibility(VISIBLE);
       /* mHooqVideoViewLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (!isMinimized) {
                    if (mVideoViewPlayer != null)
                        mVideoViewPlayer.onTouchEvent(event);
                }

                return false;
            }
        });*/
        if (!isLocalPlayback && mData != null)
            onLastPausedTimeFetched(mData.elapsedTime);
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.setPositionWhenPaused(mSavedContentPosition);
            mVideoViewPlayer.setLive(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type));
            mVideoViewPlayer.setCardData(mData);
        }
        mVideoViewPlayer.play();
        if (DeviceUtils.isTablet(mContext)
                && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPlayerStatusListener.startInLandscape();
        }
        if (mVideoViewPlayer != null) {
            for (PlayerEventListenerInterface listenerInterface : playerEventsListeners) {
                mVideoViewPlayer.addPlayerEvent(listenerInterface);
            }
        }
    }

    public void initializeHungamaPlayback() {
        if (mPlayerStatusListener != null
                && mPlayerStatusListener.isFragmentVisible()
                && mHungamaVideoViewLayout == null) {
            AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.canot_fetch_url));
            return;
        }
        if (isVmaxAdEnabled && PrefUtils.getInstance().getPrefEnableVmaxPreRollAd()) {
            showPlayerLoading();
            /*if (vmaxAdView == null)
                loadInstreamVideo();
            else
                vmaxAdView.cacheAd();*/
            return;
        }
        playHungamaContent();
    }

    private void playHungamaContent() {
        SDKLogger.debug("playHungamaContent");
        if (mPlayerStatusListener != null
                && mPlayerStatusListener.isFragmentVisible()
                && mHungamaVideoViewLayout == null) {
            AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.canot_fetch_url));
            return;
        }
        showPlayerLoading();

//        mVideoViewPlayer = new HungamaPlayView((Activity) mContext,
//                "18261833", VideoPlayingType.MOVIE);
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.setGestureListener(this);
            mVideoViewPlayer.setPlayerTitleHeaderView(getView(R.id.layout_title));
            mVideoViewPlayer.setPlayerGestureControllsView(mGestureControllsLayoutContainer);
            /*if ((mData.isLive() || mData.isProgram()))
                mVideoViewPlayer.setPlayerMediaControllsView(mediacontrollsLive);
            else*/
            mVideoViewPlayer.setPlayerMediaControllsView(mediacontrolls);
        }
        mVideoViewPlayer.setDebugTxtView(mDebugTextView);
        if (mHungamaVideoViewLayout != null) {
            SDKLogger.debug("adding video view to hungama player view container");
            if (mHungamaVideoViewLayout.indexOfChild(mVideoViewPlayer.getView()) == -1)
                mHungamaVideoViewLayout.addView(mVideoViewPlayer.getView());
            mHungamaVideoViewLayout.setOnTouchListener(mOnVideoViewTouchListener);
            mHungamaVideoViewLayout.setVisibility(VISIBLE);
        }
        mVideoViewParent.setOnTouchListener(mOnVideoViewTouchListener);
        mVideoViewPlayer.setPlayerListener(mPlayerListener);
        mThumbnailPlay.setVisibility(View.GONE);
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.setLive(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type));
            mVideoViewPlayer.setCardData(mData);
        }
        mVideoViewPlayer.play();

        if (DeviceUtils.isTablet(mContext)
                && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPlayerStatusListener.startInLandscape();
        }
        if (mVideoViewPlayer != null) {
            for (PlayerEventListenerInterface listenerInterface : playerEventsListeners) {
                mVideoViewPlayer.addPlayerEvent(listenerInterface);
            }
        }

    }

    public boolean mAlreadyPlayInitialized = false;


    public void fetchUrl(String id, String advertId) {
        isYouTubePlayerLaunched = false;
        showPlayerLoading();
        if (mVideoView != null)
            mVideoView.setSecure(true);

        mPlayerState = PLAYER_INITIATED;
        //MediaLink.Params mediaLinkparams = null;
        if (mData != null) {
            contentId = mData._id;
        }
       /* ContentDetails.Params params = new ContentDetails.Params(contentId,"mdpi","coverposter",10,APIConstants.HTTP_NO_CACHE,"user/currentdata,images,generalInfo,videos");
        ContentDetails contentDetails = new ContentDetails(params,mMediaLinkFetchListener);
        APIService.getInstance().execute(contentDetails);*/
        MediaLinkEncrypted.Params mediaLinkparams = null;
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        if (null != mData
                && null != mData.generalInfo) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
                mediaLinkparams = new MediaLinkEncrypted.Params(mData.globalServiceId, SDKUtils.getInternetConnectivity(mContext), mNid, locationInfo);
                if (mEpgDatePosition - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0
                        && PrefUtils.getInstance().getPrefEnablePastEpg()
                        && mData.startDate != null
                        && mData.endDate != null) {
                    mediaLinkparams = new MediaLinkEncrypted.Params(mData.globalServiceId, SDKUtils.getInternetConnectivity(mContext), mNid, mData.startDate, mData.endDate, locationInfo);
                }
                if (isCatchup && mData.startDate != null
                        && mData.endDate != null) {
                    isCatchup = false;
                    isFromCatchup = true;
//                    goLiveText.setVisibility(View.VISIBLE);
                    mediaLinkparams = new MediaLinkEncrypted.Params(mData.globalServiceId, SDKUtils.getInternetConnectivity(mContext), mNid, mData.startDate, mData.endDate, locationInfo);
                } else if (((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup) {
                    isFromCatchup = true;
//                    goLiveText.setVisibility(View.VISIBLE);
                    mediaLinkparams = new MediaLinkEncrypted.Params(mData.globalServiceId, SDKUtils.getInternetConnectivity(mContext), mNid, mData.startDate, mData.endDate, locationInfo);
                }
            }/* else if (APIConstants.TYPE_NEWS.equalsIgnoreCase(mData.generalInfo.type)) {
                CardDataVideosItem videosItem = getCardDataVideosItem(mData);
                if (videosItem != null
                        && videosItem.link != null
                        && null != videosItem.type) {
                    if (videosItem.type.equalsIgnoreCase(APIConstants.TYPE_NEWS)) {
                        initPlayBack(videosItem.link);
                    } else if (videosItem.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)) {
                        launchYoutubePlayer(videosItem.link);
                    }
                    return;
                } else {
                    if (mPlayerStatusListener != null
                            && mPlayerStatusListener.isFragmentVisible()) {
                        AlertDialogUtil.showToastNotification(mContext.getString(R.string.canot_fetch_url));
                    }
                    quitPlayer();
                    return;
                }
            }*/ else {
                String contentId = null;
                if (mData != null) {
                    contentId = mData._id;
                }
                if (mIsTrailerPlaying) {
                    contentId = mTrailerContentId;
                }
                if (isContentAPartnerContent(mData)) {
                    if (checkPartnerAppIsInstalled()) {
                        String appVersion = getAppVersion();
                        if (!TextUtils.isEmpty(appVersion))
                            mediaLinkparams = new MediaLinkEncrypted.Params(contentId, SDKUtils.getInternetConnectivity(mContext), mNid, locationInfo, true, appVersion);
                        else
                            mediaLinkparams = new MediaLinkEncrypted.Params(contentId, SDKUtils.getInternetConnectivity(mContext), mNid, locationInfo, true);
                    } else {
                        //mediaLinkparams = new MediaLinkEncrypted.Params(contentId, SDKUtils.getInternetConnectivity(mContext), mNid, locationInfo);
                        String fields = "videos,videoInfo,subtitles,thumbnailSeekPreview,skipConfig";
                        mediaLinkparams = new MediaLinkEncrypted.Params(contentId, fields, SDKUtils.getInternetConnectivity(mContext), "0", "0");
                    }
                } else {
                    mediaLinkparams = new MediaLinkEncrypted.Params(contentId, SDKUtils.getInternetConnectivity(mContext), mNid, locationInfo);
                }
            }


            if (mediaLinkparams != null) {
                mediaLinkparams.setAd_id(advertId);
            }
            mMedialLink = new MediaLinkEncrypted(mediaLinkparams, mMediaLinkFetchListener);
            APIService.getInstance().execute(mMedialLink);
        }

    }

    private boolean checkPartnerAppIsInstalled() {

        try {
            mContext.getPackageManager().getApplicationInfo(partnerPackageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private String getAppVersion() {

        try {
            String appVersion = PrefUtils.getInstance().getAppVersion(mContext, partnerPackageName);
            return appVersion;
        } catch (Exception e) {
            return null;
        }

    }

    private boolean isContentAPartnerContent(CardData cardData) {
        if (cardData != null
                && cardData.publishingHouse != null
                && cardData.publishingHouse.publishingHouseName != null) {
            return checkWhetherPartnerPresentInProperties(cardData.publishingHouse);
        } else {
            return false;
        }

    }

    private boolean checkWhetherPartnerPresentInProperties(PublishingHouse publishingHouse) {

        if (publishingHouse != null
                && publishingHouse.publishingHouseName != null
                && !publishingHouse.publishingHouseName.isEmpty()) {

            if (mContext != null) {
                PartnerDetailsAppInAppResponse partnerDetailsResponse = PropertiesHandler.getPartnerAppinAppList(mContext);
                String partnerName = publishingHouse.publishingHouseName;
                if (!TextUtils.isEmpty(partnerName) && partnerDetailsResponse != null) {
                    for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
                        if (partnerDetailsResponse != null
                                && partnerDetailsResponse.partnerDetails != null
                                && partnerDetailsResponse.partnerDetails.get(i) != null
                                && !TextUtils.isEmpty(partnerDetailsResponse.partnerDetails.get(i).name)
                                && partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
                            if (partnerDetailsResponse.partnerDetails.get(i).packageName != null)
                                partnerPackageName = partnerDetailsResponse.partnerDetails.get(i).packageName;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void showPlayerLoading() {
        if (mHooqVideoViewLayout != null)
            mHooqVideoViewLayout.setVisibility(View.GONE);

        if (mHungamaVideoViewLayout != null)
            mHungamaVideoViewLayout.setVisibility(View.GONE);
        getView(R.id.grdient_container).setVisibility(GONE);
        mReminderImage.setVisibility(INVISIBLE);
        mThumbnailPlay.setVisibility(INVISIBLE);
        mPerBuffer = 0;
        mBufferPercentage.setText("Loading");
        //mPreviewImage.setVisibility(View.INVISIBLE);
        if (mErrorLayout != null) {
            mErrorLayout.setVisibility(INVISIBLE);
        }
        mVideoViewParent.setVisibility(VISIBLE);
        mVideoViewParent.setOnClickListener(null);
        mProgressBarLayout.setVisibility(VISIBLE);
        if (mVideoView != null) {
            if (PrefUtils.getInstance().getShowWaterMark()) {
                mWaterMarkImage.setVisibility(INVISIBLE);
                String partnerImageLink = PrefUtils.getInstance().getPlayerWatermarkURL();

                Picasso.get().load(partnerImageLink)
                        .resize(mWaterMarkImage.getLayoutParams().width, mWaterMarkImage.getLayoutParams().height)
                        .placeholder(R.drawable.watermark_default_logo_bcn)
                        .centerInside()
                        .into(mWaterMarkImage);
            } else {
                mWaterMarkImage.setVisibility(GONE);
            }
        }
        /*if (mVideoView != null) {
            mVideoView.setVisibility(View.VISIBLE);
        }
        if (mVideoViewParent != null)
            mVideoViewParent.setVisibility(View.VISIBLE);*/
    }

    private CardDataVideosItem getCardDataVideosItem(CardData mData) {
        if (mData.videos == null
                || mData.videos.values == null
                || mData.videos.values.isEmpty()) {
            return null;
        }
        for (CardDataVideosItem videosItem : mData.videos.values) {
            return videosItem;
        }
        return null;
    }


    public void updateCardPreviewImage(CardData data) {
        if (data == null)
            return;
        mData = data;
        isDownloadedContentPlayback = false;
        if (isMediaPlaying())
            return;
        String imageLink = getImageLink();
        if (imageLink == null
                || imageLink.compareTo("Images/NoImage.jpg") == 0) {
            mPreviewImage.setImageResource(0);
        } else if (imageLink != null) {
            PicassoUtil.with(mContext).load(imageLink, mPreviewImage, R.drawable.black);

        }

        mReminderImage.setVisibility(GONE);
        mReminderImage.setImageResource(R.drawable.oncard_set_reminder_icon);

        mReminderImage.setTag(mData);
        ReminderListener reminderListener = new ReminderListener(mContext, null, mEpgDatePosition);
//        mReminderImage.setOnClickListener(reminderListener);

        mSavedContentPosition = 0;
        boolean isCurrentProgram = false;
        if (mData != null
                && mData.isProgram()
                && mData.startDate == null) {
//            mReminderImage.setImageResource(R.drawable.thumbnail_play_icon);
            mReminderImage.setOnClickListener(mPlayerClickListener);
            mReminderImage.setVisibility(INVISIBLE);
        } else if (mData != null
                && mData.isProgram()
                && mData.startDate != null
                && mData.endDate != null) {
            Date startDate = Util.getDate(mData.startDate);
            Date endDate = Util.getDate(mData.endDate);
            Date currentDate = new Date();
            if ((currentDate.after(startDate)
                    && currentDate.before(endDate))
                    || currentDate.after(endDate)) {
                isCurrentProgram = true;
                mReminderImage.setOnClickListener(mPlayerClickListener);
                mReminderImage.setVisibility(INVISIBLE);
//                mReminderImage.setImageResource(R.drawable.thumbnail_play_icon);
            }
        }

        if (mData != null
                && (mData.startDate == null
                || mData.endDate == null)) {
            mReminderImage.setVisibility(INVISIBLE);
        }
        AlarmData alarmData = Util.getReminderProgmaNameIfExistAtThisTime(mData);

        if (!isCurrentProgram && alarmData != null
                && alarmData.title != null
                && alarmData.title.equalsIgnoreCase(mData.generalInfo.title)) {
            mReminderImage.setVisibility(GONE);
            mReminderImage.setImageResource(R.drawable.oncard_set_reminder_icon_active);
        }
        if (mReminderImage.getVisibility() == VISIBLE) {
            debugTxtLayout.setVisibility(INVISIBLE);
        }
        if (mData != null && mData.generalInfo != null) {
            if (!mData.isProgram()) {
                mReminderImage.setVisibility(INVISIBLE);
                mThumbnailPlay.setVisibility(VISIBLE);
                mThumbnailPlay.setOnClickListener(mPlayerClickListener);
            }
            if (mVideoViewPlayer != null) {
                if (isFromCatchup)
                    mVideoViewPlayer.setLive(false);
                else
                    mVideoViewPlayer.setLive(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                            || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type));
                mVideoViewPlayer.setCardData(mData);
            }
            String channelName = "", programType = "", programName = "";
            if (mData != null && mData.globalServiceName != null)
                channelName = mData.globalServiceName;
            if (mData != null && mData.getType() != null)
                programType = mData.getType();
            if (mData != null && mData.getTitle() != null)
                programName = mData.getTitle() + " - " + programType.substring(0, 1).toUpperCase() + "" + programType.substring(1) + " | " + channelName;

            /*   if (mData != null && mData.generalInfo.type != null && mData.generalInfo.type.equals(APIConstants.TYPE_MOVIE)) {
             *//*  RelativeLayout.LayoutParams expandLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                expandLayout.setMargins(150,80,150,0);
                expandLayout.addRule(Gravity.CENTER);*//*
                //  getTextView(R.id.video_desc).setLayoutParams(expandLayout);
                getTextView(R.id.video_title).setText(programName);
                getTextView(R.id.video_desc).setText(mData.getDescription());
            }*/
            if (!mIsAdPlaying) {
                if (mData.content != null && mData.content.genre != null && mData.content.genre.size() > 0 && mData.content.genre.get(0) != null) {
                    if (mData.content.subGenres != null && mData.content.subGenres.size() > 0 && mData.content.subGenres.get(0) != null)
                        getTextView(R.id.video_title).setText(mData.getTitle() + "  " + "|" + "  " + mData.content.genre.get(0).name + " - " + mData.content.subGenres.get(0));
                    else
                        getTextView(R.id.video_title).setText(mData.getTitle() + "  " + "|" + "  " + mData.content.genre.get(0).name);
                } else
                    getTextView(R.id.video_title).setText(mData.getTitle());
                // getTextView(R.id.video_title).setText(programName);
                videoTitleError.setText(getTextView(R.id.video_title).getText().toString());
                getTextView(R.id.video_desc).setText(mData.getDescription());
            }
        }
        profileSelect = null;
        if (isPlayerInitialized()) {
            mReminderImage.setVisibility(INVISIBLE);
        }
        mHandler = new Handler(Looper.getMainLooper());
        initGoogleChromeCast();
        attachPlayerEvents();
        mBackIconImageViewLayout.setVisibility(VISIBLE);
        isVmaxAdEnabled = isContentPartnerHasVmaxAdsEnabled();
        SDKLogger.debug("isVmaxAdEnabled- " + isVmaxAdEnabled);
        enableVMAXMidrollAds = mData != null && !mData.isLive();
        SDKLogger.debug("enableVMAXMidrollAds- " + enableVMAXMidrollAds);
        if(!((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup && !isCatchup)
            previousNextChannels();
        else
            setNextPreviousData();

    }
    public boolean isFutureProgram(long eventstarttime){
        long now = getCurrentTimeInMillis();
        return now < eventstarttime;
    }
    public long getCurrentTimeInMillis() {
        return DateHelper.getInstance().getCurrentLocalTime();

        // return System.currentTimeMillis();
    }
    public boolean isCurrent(long eventstarttime,long eventendtime) {

        long now = getCurrentTimeInMillis();
        //CustomLog.e("EPG"," event start time :"+getEventStartTime(event));
        //CustomLog.e("EPG"," event end time :"+getEventEndTime(event));
        // CustomLog.e("EPG"," event now time :"+now);
        return now >= eventstarttime && now <= eventendtime;
    }
    private void previousNextChannels() {
        String channels = "";
        if (mData != null && mData.content != null && mData.content.previousChannel != null) {
            if (!mData.content.previousChannel.isEmpty()) {
                channels = mData.content.previousChannel;
                previousChannelName.setVisibility(VISIBLE);
                previousProgramName.setVisibility(VISIBLE);
                previous_content_image.setVisibility(VISIBLE);
            } else if (mData.content.previousChannel.isEmpty()) {
                previousChannelName.setVisibility(GONE);
                previousProgramName.setVisibility(GONE);
                previous_content_image.setVisibility(GONE);
            }
        }
        if (mData != null && mData.content != null && mData.content.nextChannel != null) {
            if (!mData.content.nextChannel.isEmpty()) {
                if (channels.isEmpty())
                    channels = "" + mData.content.nextChannel;
                else
                    channels = channels + "," + mData.content.nextChannel;
            }
        }
        if (!channels.isEmpty()) {
            final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String selectedDateInString = format.format(date);
            String dateStamp = Util.getYYYYMMDD(selectedDateInString);
            ChannelListEPG.Params params = new ChannelListEPG.Params(channels, dateStamp, true, false);
            ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
                @Override
                public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                    if (response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size() > 0) {
                        for (int i = 0; i < response.body().getResults().size(); i++) {
                            List<CardData> cardDataList = response.body().getResults().get(i).getPrograms();
                            if (mData != null && mData.content != null && mData.content.previousChannel != null) {
                                if (!mData.content.previousChannel.isEmpty() && response.body().getResults().get(i).getContentid().equalsIgnoreCase(mData.content.previousChannel)) {
                                    //  if(getMediaControllerInstance() != null) {
                                    if (isFullScreen && !mIsAdPlaying) {
                                        previousContentLL.setVisibility(VISIBLE);
                                    }
                                    try {
                                        if (cardDataList.size() > 0) {
                                            for (int j = 0; j < cardDataList.size(); j++) {
                                                CardData cardData = cardDataList.get(j);
                                                long startDate = Util.parseXsDateTime(cardData.startDate);
                                                long endDate = Util.parseXsDateTime(cardData.endDate);
                                                int programType1 = PAST_PROGRAM;
                                                boolean isfutureProgram = isFutureProgram(startDate);
                                                boolean iscurrentProgram = isfutureProgram ? false : isCurrent(startDate, endDate);
                                                if (isfutureProgram)
                                                    programType1 = FUTURE_PROGRAM;
                                                else if (iscurrentProgram)
                                                    programType1 = CURRENT_PROGRAM;
                                                if (programType1 == CURRENT_PROGRAM) {
                                                    previousCard = response.body().getResults().get(i).getPrograms().get(j);
                                                    previous_content_image.setVisibility(VISIBLE);
                                                    previousChannelName.setText(response.body().getResults().get(i).getPrograms().get(j).globalServiceName);
                                                    previousProgramName.setText(response.body().getResults().get(i).getPrograms().get(j).getTitle());
                                                    break;
                                                }
                                                //  }
                                            }
                                        }
                                    } catch (Exception e) {

                                    }
                                }
                            }
                            if (mData != null && mData.content != null && mData.content.nextChannel != null) {
                                if (!mData.content.nextChannel.isEmpty() && response.body().getResults().get(i).getContentid().equalsIgnoreCase(mData.content.nextChannel)) {
                                    // if(getMediaControllerInstance() != null) {
                                    if (isFullScreen && !mIsAdPlaying) {
                                        nextContentLL.setVisibility(VISIBLE);

                                    }
                                    try {
                                        if (cardDataList.size() > 0) {
                                            for (int j = 0; j < cardDataList.size(); j++) {
                                                CardData cardData = cardDataList.get(j);
                                                long startDate = Util.parseXsDateTime(cardData.startDate);
                                                long endDate = Util.parseXsDateTime(cardData.endDate);
                                                int programType1 = PAST_PROGRAM;
                                                boolean isfutureProgram = isFutureProgram(startDate);
                                                boolean iscurrentProgram = isfutureProgram ? false : isCurrent(startDate, endDate);
                                                if (isfutureProgram)
                                                    programType1 = FUTURE_PROGRAM;
                                                else if (iscurrentProgram)
                                                    programType1 = CURRENT_PROGRAM;
                                                if (programType1 == CURRENT_PROGRAM) {
                                                    next_content_image.setVisibility(VISIBLE);
                                                    nextCard = response.body().getResults().get(i).getPrograms().get(j);
                                                    nextChannelName.setText(response.body().getResults().get(i).getPrograms().get(j).globalServiceName);
                                                    nextProgramName.setText(response.body().getResults().get(i).getPrograms().get(j).getTitle());
                                                    break;
                                                }
                                                //  }
                                            }
                                        }
                                    } catch (Exception e) {

                                    }
                                }
                            }
                        }
                    }
                    if(previousContentLL.getVisibility() == VISIBLE) {
                        previousChannelLLSub.setVisibility(VISIBLE);
                        previousChannelNameSub.setText(previousChannelName.getText().toString());
                        previousChannelProgramSub.setText(previousProgramName.getText().toString());
                    }
                    if(nextContentLL.getVisibility() == VISIBLE) {
                        nextChannelLLSub.setVisibility(VISIBLE);
                        nextChannelNameSub.setText(nextChannelName.getText().toString());
                        nextChannelProgramSub.setText(nextProgramName.getText().toString());
                    }
                }

                @Override
                public void onFailure(Throwable t, int errorCode) {
                    previousContentLL.setVisibility(GONE);
                    nextContentLL.setVisibility(View.GONE);
                    nextChannelLLSub.setVisibility(GONE);
                    previousChannelLLSub.setVisibility(GONE);
                }
            });
            APIService.getInstance().execute(channelListEPG);
        }
    }


    /* private void getCatcUpData(){
         if(mData != null && mData._id != null) {
             final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
             SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
             String selectedDateInString = format.format(date);
             String dateStamp = Util.getYYYYMMDD(selectedDateInString);
             ChannelListEPG.Params params = new ChannelListEPG.Params(mData._id, dateStamp, true);
             ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
                 @Override
                 public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                     if(response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size() >0) {
                         for (int i = 0; i < response.body().getResults().size(); i++) {
                             List<CardData> cardDataList = response.body().getResults().get(i).getPrograms();
                             if(cardDataList != null && cardDataList.size() > 0) {
                                 getMediaControllerInstance().catchUpLinear.setVisibility(View.VISIBLE);
                              *//*   getMediaControllerInstance().catchup_list.setVisibility(View.VISIBLE);
                                getMediaControllerInstance().catchup_list.setHasFixedSize(true);
                                // recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                                getMediaControllerInstance().catchup_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, true));
                                AdapterMedHorizontalCarousel adapterMedHorizontalCarousel = new AdapterMedHorizontalCarousel(mContext,cardDataList,getMediaControllerInstance().catchup_list);
                               // adapterMedHorizontalCarousel.setData(cardDataList);
                                getMediaControllerInstance().catchup_list.setAdapter(adapterMedHorizontalCarousel);
                                if (mVideoViewPlayer != null && mVideoViewPlayer instanceof ExoPlayerView) {
                                    ((ExoPlayerView) mVideoViewPlayer).;
                                }*//*
                            } else
                                getMediaControllerInstance().catchUpLinear.setVisibility(GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t, int errorCode) {
                    getMediaControllerInstance().catchUpLinear.setVisibility(GONE);
                }
            });
            APIService.getInstance().execute(channelListEPG);
        }
    }
*/
    private boolean isContentPartnerHasVmaxAdsEnabled() {
        String vmaxAdEnabledPartnerIds = PrefUtils.getInstance().getPrefEnableVmaxForPartners();
        if (mData == null || vmaxAdEnabledPartnerIds == null || !PrefUtils.getInstance().getPrefEnableVmaxInStreamAd()) {
            SDKLogger.debug("vmaxAdEnabledPartnerIds- " + vmaxAdEnabledPartnerIds + " mData- " + mData + " getPrefEnableVmaxInStreamAd- " + PrefUtils.getInstance().getPrefEnableVmaxInStreamAd());
            return false;
        }
        SDKLogger.debug("vmaxAdEnabledPartnerIds- " + vmaxAdEnabledPartnerIds + " publishingHouseId- " + mData.getPublishingHouseId());
        return vmaxAdEnabledPartnerIds.contains(mData.getPublishingHouseId() + "");
    }

    private List<String> prepareSeasonNames(List<CardData> dataList) {
        List<String> seasons = new ArrayList<>();
        String seasonText = "Season ";
        for (Iterator<CardData> iterator = dataList.iterator(); iterator.hasNext(); ) {
            CardData seasonData = iterator.next();
            if (seasonData.content != null
                    && seasonData.content.serialNo != null) {
                seasons.add(seasonText + seasonData.content.serialNo);
            }
        }
        return seasons;
    }

    private void attachPlayerEvents() {
        boolean enablePlayerLogs = false;
        String playerLogsTo = PrefUtils.getInstance().getPrefPlayerLogsEnableTo();
        if ((APIConstants.isErosNowContent(mData)
                && APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(playerLogsTo))
                || "all".equalsIgnoreCase(playerLogsTo)) {
            enablePlayerLogs = true;
        }
        if (enablePlayerLogs)
            addPlayerEventListener(new ErosNowPlayerEvents());

    }

    private void updateSubscribeButton() {
        if (isFreeContent()) {
            mThumbnailPlay.setVisibility(GONE);
            mThumbnailPlay.setOnClickListener(mPlayerClickListener);
            return;
        }
        mThumbnailPlay.setVisibility(GONE);
        mThumbnailPlay.setImageResource(R.drawable.thumbnail_pay_icon);
        mThumbnailPlay.setOnClickListener(mSubscribePackageListener);
        mVideoViewParent.setOnClickListener(mSubscribePackageListener);
    }

    private boolean isFreeContent() {
        List<String> subscribedCardDataPackages = ApplicationController.getCardDataPackages();
        if (subscribedCardDataPackages == null
                || subscribedCardDataPackages.isEmpty()) {
            return false;
        }

        if (mData == null
                || mData.packages == null
                || mData.packages.isEmpty()) {
            return true;
        }

        for (CardDataPackages contentPackages : mData.packages) {
            if (subscribedCardDataPackages.contains(contentPackages.packageId)) {
                return true;
            }
        }
        return false;
    }


    public String convertDateFormat(String from, String to, String date) {
        SimpleDateFormat sdfFrom = new SimpleDateFormat(from);
        SimpleDateFormat sdfTo = new SimpleDateFormat(to);
        Date mDate = null;
        String convertedDate = "";

        try {
            mDate = sdfFrom.parse(date);
            convertedDate = sdfTo.format(mDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    protected String getImageLink() {

        String imageLink = null;
        if (mData == null
                || mData.images == null
                || mData.images.values == null
                || mData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER, APIConstants.IMAGE_TYPE_BANNER};
        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : mData.images.values) {
//                LoggerD.debugHooqVstbLog("imageItem: imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)) {
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)
                            || (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile))
                            || (APIConstants.IMAGE_TYPE_BANNER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile))) {
                        if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)) {
//                            mPreviewImage.setScaleType(null);
                            mPreviewImage.setBackgroundColor(mContext.getResources().getColor(R.color.black));
                            mPreviewImage.setScaleType(ScaleType.FIT_CENTER);
                            mPreviewImage.requestLayout();
                            mPreviewImage.invalidate();
                            return imageItem.link;
//                        mPreviewImage.setScaleType(Scale);
//                        android:scaleType="centerCrop"
                        }
                        mPreviewImage.setAdjustViewBounds(true);
                        mPreviewImage.setScaleType(ScaleType.CENTER_CROP);
                        mPreviewImage.invalidate();
//                    LoggerD.debugHooqVstbLog("imageType- " + imageType + " imageItem: imageType- " + imageItem.type);
                        return imageItem.link;
                    }
                }
            }
        }
        return imageLink;
    }


    //MediaLink mMedialLink;
    MediaLinkEncrypted mMedialLink;

    public void closePlayer() {
        LoggerD.debugLog("closePlayer");
        mIsTrailerPlaying = false;
        mTrailerContentId = null;
        mPlayerState = PLAYER_STOPPED;
        isInPlayBackInitiatedMode = false;
        mAlreadyPlayInitialized = false;
        isMediaPaused = false;
        isFromCatchup = false;
        stopMOUTracking();
        if(!APIConstants.IS_FROM_RECOMMENDATIONS) {
                mListQueueCardData = null;
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.mListQueueCardData = null;
        }
        APIConstants.IS_FROM_RECOMMENDATIONS=false;
        hideAllControlls();
        onDestroy();
        detachYoutubePlayerFragment();
        hideInstream();
      /*  mListQueueCardData = null;
        ((MainActivity) mContext).mFragmentCardDetailsPlayer.mListQueueCardData = null;*/
        if (mData != null
                && mData.generalInfo != null
               /* && !(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type))*/) {
                updatePlayerStatus(PlayerListener.STATE_PAUSED, getCurrentPosition());

        }

        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.hideMediaController();
            mVideoViewPlayer.closeSession();
        }
        mPerBuffer = 0;
        hidePlayerLoading();
        if (changeOrientationOnClose)
            resumePreviousOrientaionTimer();
        mVideoViewParent.setOnClickListener(mPlayerClickListener);
        mVideoViewParent.setEnabled(true);
        LoggerD.debugHooqVstbLog("closing player");
        if (mCastManager != null) {
            mCastManager.removeSessionManagerListener(mCastConsumer, CastSession.class);
        }
        if (mVideoView != null) {
            mVideoView.setSecure(false);
        }
        if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
            YuppAnalytics.getInstance(mContext).handlePlayEndedByUser();
        }
    }

    public void hidePlayerLoading() {
        if (mContext == null) {
            return;
        }
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBackIconImageViewLayout.setVisibility(VISIBLE);
                mReminderImage.setVisibility(INVISIBLE);
                getView(R.id.grdient_container).setVisibility(GONE);
                mPreviewImage.setVisibility(VISIBLE);
                SDKLogger.debug("show play icon");
                mPreviewImageOverlay.setVisibility(VISIBLE);
                mThumbnailPlay.setVisibility(VISIBLE);
                mThumbnailPlay.setOnClickListener(mPlayerClickListener);
                if (mHooqVideoViewLayout != null) {
//                    mHooqVideoViewLayout.setVisibility(GONE);
                    /*if (mVideoViewPlayer != null
                            && mVideoViewPlayer.getView() != null
                            && mHooqVideoViewLayout.indexOfChild(mVideoViewPlayer.getView()) != -1) {
                        mHooqVideoViewLayout.removeAllViews();
                    }*/
                    mHooqVideoViewLayout.setOnTouchListener(null);
                }
                mProgressBarLayout.setVisibility(INVISIBLE);
                if (mVideoView != null) {
                    mVideoView.setVisibility(INVISIBLE);
                }
                mVideoViewParent.setEnabled(true);
                if (mData == null) {
                    return;
                }
                if (mData != null && (mData.isTVSeries()
                        || mData.isVODChannel()
                        || mData.isVODYoutubeChannel()
                        || mData.isVODCategory()
                        || mData.isTVSeason())) {
                    SDKLogger.debug("hide play icon");
                    hidePlayIcon();
                }
            }
        });
    }

    private void hidePlayIcon() {
        mThumbnailPlay.setVisibility(GONE);
        mReminderImage.setVisibility(GONE);
    }

    private boolean isAdResumed = false;
    private View.OnTouchListener mOnVideoViewTouchListener = new View.OnTouchListener() {
        private boolean isDoubleTap = false;

        public GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isFullScreen && !APIConstants.IS_PLAYER_SCREEN_LOCKED) {

                    if (mVideoViewPlayer != null) {
                        mVideoViewPlayer.onTapToZoom();
                        mVideoViewPlayer.hideMediaController();

                    }
                }
                return super.onDoubleTap(e);
            }


        });

        @Override
        public boolean onTouch(View arg0, MotionEvent motionEvent) {

            /*if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (duringAd) {
                    //Added to prevent click on the ads that are nor loaded yet which would prevent "ad paused before the ad played" error.
                    mVideoViewPlayer.onPause();
                    isAdPuasedWhenPlaying = true;
                    duringAd = false;
                    //Report ad paused to Pulse SDK.
                    currentPulseVideoAd.adPaused();
                    //Report ad clicked to Pulse SDK.
                    currentPulseVideoAd.adClickThroughTriggered();
//                    mVideoView.setOnTouchListener(null);
                    if(clickThroughCallback != null){
                        clickThroughCallback.onClicked(currentPulseVideoAd);
                    }
                    playbackHandler.removeCallbacks(playbackRunnable);
                    //Log.i(TAG, "ClickThrough occurred.");
                    return false;
                }
            }*/

            if (isMinimized) {
                return true;
            }
            getMediaControllerInstance().rvAudioTracks.setVisibility(GONE);
            Log.e("ON TOUCH", "TOUCHED");
            gestureDetector.onTouchEvent(motionEvent);
            if (!isMinimized && (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)) {
                Log.e("ON TOUCH", "TOUCHED INSIDE CONDITION");
                mVideoViewPlayer.onTouchEvent(motionEvent);
            }
            if (APIConstants.IS_PLAYER_SCREEN_LOCKED && isFullScreen) {
                //commenting because when we unlock and lock the mobile these contollers are not getting visible
                getTextView(R.id.video_title).setVisibility(INVISIBLE);
                getTextView(R.id.video_desc).setVisibility(INVISIBLE);
                goLiveText.setVisibility(GONE);
                if (mChromeCastToolbar != null) {
                    mChromeCastToolbar.setVisibility(INVISIBLE);
                }
                if (mBackIconImageView != null)
                    mBackIconImageView.setVisibility(INVISIBLE);
              /*  if (mBackIconImageViewLayout != null) {
                    mBackIconImageViewLayout.setVisibility(INVISIBLE);
                }*/
                if (mGestureControllsLayoutContainer != null)
                    mGestureControllsLayoutContainer.setVisibility(INVISIBLE);
            } else {
                getTextView(R.id.video_title).setVisibility(GONE);
                getTextView(R.id.video_desc).setVisibility(GONE);
                chromeCastBack.setVisibility(GONE);
                if (mChromeCastToolbar != null) {
                    mChromeCastToolbar.setVisibility(GONE);
                }
                mBackIconImageView.setVisibility(VISIBLE);
                if (!isFullScreen || mIsAdPlaying) {
                    getTextView(R.id.video_title).setVisibility(GONE);
                    getTextView(R.id.video_desc).setVisibility(GONE);
                    mChromeCastToolbar.setVisibility(GONE);
                } else {
                    getTextView(R.id.video_title).setVisibility(VISIBLE);
                    getTextView(R.id.video_desc).setVisibility(VISIBLE);
                    mChromeCastToolbar.setVisibility(VISIBLE);
                    if (isFromCatchup && !mIsAdPlaying){
                        goLiveText.setVisibility(VISIBLE);
                    }
                }

                /*if(mBackIconImageViewLayout!=null) {
                    mBackIconImageViewLayout.setVisibility(View.VISIBLE);
                }
*/
            }

            if (!isFullScreen) {
                APIConstants.IS_PLAYER_SCREEN_LOCKED = false;
            }
            return false;
        }
    };


    public void unLockControles() {
        getTextView(R.id.video_title).setVisibility(VISIBLE);
        getTextView(R.id.video_desc).setVisibility(VISIBLE);
        if (mChromeCastToolbar != null) {
            mChromeCastToolbar.setVisibility(VISIBLE);
        }
        if (mBackIconImageView != null)
            mBackIconImageView.setVisibility(VISIBLE);
     /*   if (mBackIconImageViewLayout != null) {
            mBackIconImageViewLayout.setVisibility(VISIBLE);
        }*/
        if (mGestureControllsLayoutContainer != null)
            mGestureControllsLayoutContainer.setVisibility(VISIBLE);
    }

    protected void initializeVideoPlay(Uri uri) {
        //Log.d(TAG, "video url " + uri);
        VideoViewPlayer.StreamType streamType = VideoViewPlayer.StreamType.VOD;
        if (mVideoViewPlayer == null) {
            Log.d("VideoViewPlayer", "new mVideoViewPlayer");
            if (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW) {
                mVideoViewPlayer = new VideoViewPlayer((VideoView) mVideoView, mContext, uri, streamType);
            }
            if (mVideoViewPlayer == null) {
                return;
            }
            // mVideoViewPlayer.openVideo();
            if (APIConstants.STREAMINGFORMATHLS.equals(mStreamingFormat)) {
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HLS);
            } else if (APIConstants.STREAMINGFORMATHTTP.equals(mStreamingFormat)) {
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
            } else if (APIConstants.STREAMINGFORMATRTSP.equals(mStreamingFormat)) {
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.RTSP);
            }

//            mVideoViewPlayer.setPlayerListener(mPlayerListener);
//            mVideoViewPlayer.setUri(uri, streamType);
        } else {
            Log.d("VideoViewPlayer", "old mVideoViewPlayer");
            if (APIConstants.STREAMINGFORMATHLS.equals(mStreamingFormat)) {
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HLS);
            } else if (APIConstants.STREAMINGFORMATHTTP.equals(mStreamingFormat)) {
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
            } else if (APIConstants.STREAMINGFORMATRTSP.equals(mStreamingFormat)) {
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.RTSP);
            }
//            mVideoViewPlayer.setPlayerListener(mPlayerListener);
//            mVideoViewPlayer.setUri(uri, streamType);
        }
        /*mVideoViewPlayer.setPlayerListener(mPlayerListener);
        mVideoViewPlayer.setOnLicenseExpiryListener(onLicenseExpiryListener);
        mVideoViewPlayer.hideMediaController();
        mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
*/
/*
        if (mData != null
                && mData.generalInfo != null
                && mData.generalInfo.type != null
                && mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VOD)) {
            mVideoViewPlayer.setPositionWhenPaused(0);
        }
*/

/*
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (!isMinimized && videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW) {
                    if (mVideoViewPlayer != null)
                        mVideoViewPlayer.onTouchEvent(event);
                }

                return false;
            }
        });
*/

        if (mVideoViewPlayer == null) {
            Log.d("VideoViewPlayer", "new mVideoViewPlayer");
            if (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
                mVideoViewPlayer = new VideoViewPlayer((VideoView) mVideoView, mContext, uri, streamType);
            // mVideoViewPlayer.openVideo();
        }

        Log.d("VideoViewPlayer", "old mVideoViewPlayer");
        startPlayBackWithAdOrVideo();
//        mVideoViewPlayer.setPlayerListener(mPlayerListener);
//        mVideoViewPlayer.hideMediaController();
//        mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
//        mVideoView.setOnTouchListener(mOnVideoViewTouchListener);
        if ((mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)/*|| mData.playFullScreen*/) {
            if (mListQueueCardData != null && mData != null &&
                    mData.generalInfo != null && mData.generalInfo.type != null && (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type))) {
                // mSeason_icon.setVisibility(View.VISIBLE);
            }
            mPlayerStatusListener.startInLandscape();
        }

    }

    private void startPlayBackWithAdOrVideo() {
        String adProvider = null;
        boolean adEnabled = false;
        if (mData != null
                && mData.content != null) {
            if (!TextUtils.isEmpty(mData.content.adProvider)) {
                adProvider = mData.content.adProvider;
            }
            adEnabled = mData.content.adEnabled;
        }

        if (mDefaultAdTagUrl == null) {
            if (mData != null && mData.content != null && mData.content.adConfig != null && mData.content.adConfig.vast != null
                    && mData.content.adConfig.vast.android != null) {
                mDefaultAdTagUrl = mData.content.adConfig.vast.android;
            }
        }

        if (isVmaxAdEnabled
                && PrefUtils.getInstance().getPrefEnableVmaxPreRollAd()
                && !isToPlayOnLocalDevice
                && !isRetryPlayBak) {
            showPlayerLoading();
            loadInstreamVideo();
            return;
        }

        if (!adEnabled
                || mDefaultAdTagUrl == null
                || TextUtils.isEmpty(mDefaultAdTagUrl)
                || isToPlayOnLocalDevice
                || isRetryPlayBak) {
            playVideoContent(null);
            isRetryPlayBak = false;
        } else {
            if (Util.isPremiumUser()) {
                playVideoContent(null);
            } else {
                playVideoContent(/*getCustomADTagURL*/(mDefaultAdTagUrl));
            }
        }

    }

    private void playVideoFile(CardDownloadData mDownloadData) {

        isLocalPlayback = true;
        drmLicenseType = "lp";
        String url = "file://" + mDownloadData.mDownloadPath;


        if (mData.content != null && mData.content.drmEnabled) {

            String licenseData = "clientkey:" + PrefUtils.getInstance().getPrefClientkey() + ",contentid:" + mData._id + "," +
                    "type:" + drmLicenseType + "," +
                    "" + getDrmProfileString() + "," + APIConstants.getDRMDeviceParams();

            byte[] data;
            try {
                data = licenseData.getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                WidevineDrm.Settings.USER_DATA = base64;
                WidevineDrm.Settings.DEVICE_ID = PrefUtils.getInstance().getPrefDeviceid();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Uri uri;
        uri = Uri.parse(url);
        if (mDownloadData.mCompleted) {
            DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            uri = manager.getUriForDownloadedFile(mDownloadData.mDownloadId);
        }
        if (mPlayerStatusListener != null) {
            mPlayerStatusListener.playerStatusUpdate("Playing :: " + url);
        }
        VideoViewPlayer.StreamType streamType = VideoViewPlayer.StreamType.VOD;
        if (mVideoViewPlayer == null) {
            Log.d("VideoViewPlayer", "new mVideoViewPlayer");
            if (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
                mVideoViewPlayer = new VideoViewPlayer((VideoView) mVideoView, mContext, uri, streamType);
            //mVideoViewPlayer.openVideo();
            mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
            mVideoViewPlayer.setPlayerListener(mPlayerListener);
            mVideoViewPlayer.setUri(uri, streamType);
        } else {
            mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
            Log.d("VideoViewPlayer", "old mVideoViewPlayer");
            mVideoViewPlayer.setPlayerListener(mPlayerListener);
            mVideoViewPlayer.setUri(uri, streamType);
        }
        mVideoViewPlayer.isPlayingAd(false);
        mVideoViewPlayer.setOnLicenseExpiryListener(onLicenseExpiryListener);
        mVideoViewPlayer.hideMediaController();
        mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (!isMinimized && videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW) {
                    mVideoViewPlayer.onTouchEvent(event);
                }
                return false;
            }
        });

        int ellapseTime = PrefUtils.getInstance().getInt(mData._id, 0);
        mVideoViewPlayer.setPositionWhenPaused(ellapseTime * 1000);
    }

    public void stopMOUTracking() {
        if (mMouTracker != null) {
            mMouTracker.setBufferCount(bufferCount);
            mMouTracker.stoppedAt();
            mMouTracker.setSourceTab(mSourceTab);
            mMouTracker = null;
        }
    }

    public void resumePreviousOrientaionTimer() {
        LoggerD.debugLog("HooqLoopFullScreen resumePreviousOrientaionTimer");
     /*   if (mTimer != null) {
            LoggerD.debugLog("HooqLoopFullScreen cancel oreintation");
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                resumePreviousOrientaion();
            }
        }, 3000);*/
        resumePreviousOrientaion();
    }

    private Timer mTimer;

    public void resumePreviousOrientaion() {
        LoggerD.debugLog("MiniCardVideoPlayer resumePreviousOrientaion");
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
               /* if (isMinimized) {
                    return;
                }*/
               /* if (isHelpScreenShowing || mPlayerStatusListener.isDragging()) {
                    return;
                }
                if (mPlayerState == PLAYER_PLAY
                        || isYouTubePlayerPlaying()
                        || (isHooqContent(mData) && isPlayerInitialized())
                        || mIsAdDisplayed
                        || (DeviceUtils.isTablet(mContext)
                        && isPlayerInitialized())) {
                    if (Util.isScreenAutoRotationOnDevice(mContext)) {
                        LoggerD.debugLog("MiniCardVideoPlayer resumePreviousOrientaion sensor");
                        if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                            ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        else {
                            // only auto rotate when the player is in portrait mode
                            if (isMinimized())
                                ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_PORTRAIT);
                            else
                                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        }
                    } else {
                        ((BaseActivity) mContext).setOrientation(getScreenOrientation());
                    }
                } else {
                    if (DeviceUtils.isTablet(mContext)) {
                        ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    } else {
                        LoggerD.debugLog("MiniCardVideoPlayer resumePreviousOrientaion portrait");
                        if (mData != null && mData.playFullScreen)
                            ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_PORTRAIT);
                    }
                }*/
                if ((isMediaPlaying() || APIConstants.IS_SHOWING_SUBSCRIPTION_POPUP) && !isMinimized()) {
                    APIConstants.IS_SHOWING_SUBSCRIPTION_POPUP=false;
                    if ((mPlayerState == PLAYER_PLAY || mPlayerState == PLAYER_INITIATED || mPlayerState== PLAYER_STOPPED) && isFullScreen) {
                        if (android.provider.Settings.System.getInt(mContext.getContentResolver(),
                                Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
                            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        } else {
//                            if(DeviceUtils.isTabletOrientationEnabled(mContext))
                                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
//                            else
//                                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                    } else {
                        if (android.provider.Settings.System.getInt(mContext.getContentResolver(),
                                Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
                            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        } else {
                            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        }
                    }
                }
            }
        });
    }

    public boolean isYouTubePlayerPlaying() {
        if (youtubePlayer == null) {
            return false;
        }
        try {
            return youtubePlayer.getCurrentTimeMillis() > 0;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    //	}
    public int getScreenOrientation() {
        Log.e("MINICARDVIDEO PLAYER::", "ORIENTATION METHOD");

        int orientation;

        DisplayMetrics dm = new DisplayMetrics();
        Display getOrient = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        getOrient.getMetrics(dm);

        if (dm.widthPixels < dm.heightPixels) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        } else {
            orientation = SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
        return orientation;

    }

    public void playInLandscape() {
        if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null
                && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mDraggablePanel != null)
            ((MainActivity) mContext).mFragmentCardDetailsPlayer.mDraggablePanel.setClickToMaximizeEnabled(false);
        setFullScreenEdgeToEdge((Activity) mContext);
        if(!mIsAdPlaying) {
            mChromeCastToolbar.setVisibility(VISIBLE);
            getTextView(R.id.video_title).setVisibility(VISIBLE);
            getTextView(R.id.video_desc).setVisibility(VISIBLE);
        }

      /*  RelativeLayout.LayoutParams trackingStripParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trackingStripParams.setMargins(0,270,0,0);
       // trackingStripParams.addRule(Gravity.CENTER);
        fingerPrintLayout.setLayoutParams(trackingStripParams);*/

        if (mData != null && mData.content.isSupportCatchup.equalsIgnoreCase("false") && (mData.isLive() || mData.isProgram())) {
            getImageView(R.id.media_player_fowrard_icon).setVisibility(GONE);
            getImageView(R.id.media_player_rewind_icon).setVisibility(GONE);
        } else {
            getImageView(R.id.media_player_fowrard_icon).setVisibility(VISIBLE);
            getImageView(R.id.media_player_rewind_icon).setVisibility(VISIBLE);
        }
        if (mPlayerStatusListener != null) {
            mPlayerStatusListener.playerStatusUpdate("Play in lanscape :: ");
        }
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.orientationChange(Configuration.ORIENTATION_LANDSCAPE);
        }

        int statusBarHeight = UiUtil.getStatusBarHeight(mContext);
        int navBarHeight = UiUtil.getNavBarHeight(mContext);

        if (DeviceUtils.isTablet(mContext)) {
            DisplayMetrics dm = new DisplayMetrics();
            ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;

            mWidth = ApplicationController.getApplicationConfig().screenWidth;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && UiUtil.hasSoftKeys(mContext)) {
//                findViewById(R.id.container).setPadding(0, 0, 0, navBarHeight);
                mHeight = ApplicationController.getApplicationConfig().screenHeight + navBarHeight;
            } else {
                mHeight = ApplicationController.getApplicationConfig().screenHeight;
            }
        } else {
            mWidth = (ApplicationController.getApplicationConfig().screenWidth * 16) / 9;
            mHeight = ApplicationController.getApplicationConfig().screenWidth;
        }
        LoggerD.debugLog("MiniCardVideoPlayer mWidth- " + mWidth + " mHeight- " + mHeight);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        params.height = mHeight + statusBarHeight;
        //showAdDialog();
        Util.debugLog("playInLandscape() mWidth = " + mWidth + " * mHeight = " + mHeight
                + " params.height- " + params.height);
      /*  if (mListQueueCardData != null && mSeason_icon != null && mData != null &&
                mData.generalInfo != null && mData.generalInfo.type != null && (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type))) {
            mSeason_icon.setVisibility(View.VISIBLE);
        }*/
        mVideoViewParent.setEnabled(false);

        if (mVideoView != null) {
            mVideoView.setLayoutParams(params);
        }
        if (mVideoViewPlayer != null)
            mVideoViewPlayer.setParams(params);
        /*if (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
            ((VideoViewExtn) mVideoView).resizeVideo(mWidth, mHeight);
		else {
			mVideoViewPlayer.resizeVideo(mWidth, mHeight);
		}*/
        ((BaseActivity) mContext).hideActionBar();
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.playerInFullScreen(true);
        }
        setFullScreen(true);
        if (mPlayerFullScreen != null) {
            mPlayerFullScreen.playerInFullScreen(true);
        }
        mParentLayout.setBackgroundColor(Color.BLACK);
        hideMediaController();
        // Make the status bar and navigation bar visible again.
        // Whenever the status bar and navigation bar appear, we want the playback controls to
        // appear as well.
        resumePreviousOrientaion();

        if (mData!=null && mData.isLive()) {
            mFfwdButton.setVisibility(INVISIBLE);
            mRewButton.setVisibility(INVISIBLE);
        } else {
            mFfwdButton.setVisibility(VISIBLE);
            mRewButton.setVisibility(VISIBLE);
        }
    }


    public void playInPortrait() {

        if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null
                && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mDraggablePanel != null)
            ((MainActivity) mContext).mFragmentCardDetailsPlayer.mDraggablePanel.setClickToMaximizeEnabled(true);
        resetFullScreenEdgeToEdge((Activity) mContext);
        getTextView(R.id.video_title).setVisibility(GONE);
        getTextView(R.id.video_desc).setVisibility(GONE);
        mChromeCastToolbar.setVisibility(GONE);

        getImageView(R.id.media_player_fowrard_icon).setVisibility(GONE);
        getImageView(R.id.media_player_rewind_icon).setVisibility(GONE);
        if (mPlayerStatusListener != null) {
            mPlayerStatusListener.playerStatusUpdate("Play in portrait :: ");
        }
        setFullScreen(false);
        if (mCardData != null && mCardData.isMovie()) {
            if (isMediaPlaying()) {
                onPause();
                closePlayer();
                updateCardPreviewImage(mCardData);
            }
        }
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.orientationChange(Configuration.ORIENTATION_PORTRAIT);

            mVideoViewPlayer.playerInFullScreen(false);
        }

        //if (DeviceUtils.isTablet(mContext)) {
        DisplayMetrics dm = new DisplayMetrics();
        ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
        ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
        //} else {
        mWidth = ApplicationController.getApplicationConfig().screenWidth;
        mHeight = getHeight(mWidth);

        //}
        LoggerD.debugLog("MiniCardVideoPlayer" +
                " mWidth- " + mWidth + " mHeight- " + mHeight + " " +
                " screenHeight- " + ApplicationController.getApplicationConfig().screenHeight + " " +
                " screenWidth- " + ApplicationController.getApplicationConfig().screenWidth);

        hideMediaController();
        hideAllControlls();
        mVideoViewParent.requestLayout();
        if (isHooqContent(mData) && (mPlayerState == PLAYER_INITIATED || mPlayerState == PLAYER_STOPPED) && !changeOrientationOnClose) {
            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.disable);
            }
        }
        resumePreviousOrientaionTimer();
        if (mSeason_icon != null)
            mSeason_icon.setVisibility(INVISIBLE);
        if (mbutton_SeasonUI != null) {
            mbutton_SeasonUI.setVisibility(INVISIBLE);
        }
        if (mSeasonUI != null) {
            mSeasonUI.setVisibility(INVISIBLE);
            IS_SesonUIVisible = false;
        }
        if (APIConstants.IS_PLAYER_SCREEN_LOCKED) {
            APIConstants.IS_PLAYER_SCREEN_LOCKED = false;
            allowMediaController(true);
            mVideoViewPlayer.showMediaController();
        }
        mParentLayout.setBackgroundColor(Color.BLACK);

        if (mData !=null &&(mData.isLive() || mData.isProgram())) {
            mFfwdButton.setVisibility(INVISIBLE);
            mRewButton.setVisibility(INVISIBLE);
        } else {
            mFfwdButton.setVisibility(VISIBLE);
            mRewButton.setVisibility(VISIBLE);
        }
        if(subscriptionErrorLayout.getVisibility()==VISIBLE) {
            subscriptionErrorLayout.setVisibility(GONE);
            playContent();
        }

    }

    public void minimize() {
        Util.debugLog("minimize()");
        isMinimized = true;
        mVideoViewPlayer.setMinized(true);

        DisplayMetrics dm = new DisplayMetrics();
        ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
        ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;

        mWidth = (int) (ApplicationController.getApplicationConfig().screenWidth * 0.45);
        mHeight = getHeight(mWidth);

        if (mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)) {
            mHeight = (mWidth * 3) / 4;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                mWidth, mHeight);
        mPreviewImage.setLayoutParams(params);
        mVideoViewParent.findViewById(R.id.cardmedia_mini).setVisibility(VISIBLE);
        mVideoView.setLayoutParams(params);
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.hideMediaController();
        }
    }


    public void maximize() {
        Util.debugLog("maximize()");
        isMinimized = false;
        mVideoViewPlayer.setMinized(false);
        mWidth = ApplicationController.getApplicationConfig().screenWidth;
        mHeight = getHeight(mWidth);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                mWidth, mHeight);
        mPreviewImage.setLayoutParams(params);
        mVideoView.setLayoutParams(params);
        if (!didUserHide)
            mNextEpisodePopupWindow.setVisibility(VISIBLE);

    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getStopPosition() {
        if (mVideoViewPlayer == null) {
            return 0;
        }
        return (mVideoViewPlayer.getCurrentPosition() / 1000);
    }

    public boolean isMediaPlaying() {
//        if (videoPlayerType == VIDEO_PLAYER_TYPE.HOOQ_PLAYERVIEW && mVideoViewPlayer != null) {
        /*if (videoPlayerType == VIDEO_PLAYER_TYPE.HOOQ_PLAYERVIEW &&
                mVideoViewPlayer != null) {
            LoggerD.debugHooqVstbLog("isMediaPlaying(): mVideoViewPlayer.isPlaying- " + mVideoViewPlayer.isPlaying());
            return mVideoViewPlayer.isPlaying() || mVideoViewPlayer.isPlaybackInitialized();
        }

        if (videoPlayerType == VIDEO_PLAYER_TYPE.HUNGAMA_PLAYER_VIEW &&
                mVideoViewPlayer != null) {
            LoggerD.debugHooqVstbLog("isMediaPlaying(): mVideoViewPlayer.isPlaying- " + mVideoViewPlayer.isPlaying());
            return mVideoViewPlayer.isPlaying() || mVideoViewPlayer.isPlaybackInitialized();
        }*/

        if (/*mVideoView == null || */mVideoViewPlayer == null) {
            return false;
        }
        LoggerD.debugLog("isMediaPlaying(): mVideoViewPlayer.getCurrentPosition- " + mVideoViewPlayer.getCurrentPosition());
        return mVideoViewPlayer.getCurrentPosition() > 0 || mVideoViewPlayer.isPlaying();

    }

    protected boolean isParentalControlPinConfirmed = false;

    /**
     * @return can able to play
     */
    public boolean canBePlayed() {
        /*if (!checkUserLoginStatus()) {
            isLoginRequestThroughPlayback = true;
            launchLoginActivity();
            return false;
        }*/

        if (checkLocalPlayback()) {
            isDownloadedContentPlayback = true;
            mChromeCastToolbar.setVisibility(GONE);
            return false;
        }

        if (!Util.isNetworkAvailable(mContext)) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string
                    .network_error));
            return false;
        }
        String emailRequiredForPartners = PrefUtils.getInstance().getPrefEmailRequiredForPartners();
        if (mData != null
                && mData.publishingHouse != null
                && TextUtils.isEmpty(PrefUtils.getInstance().getPrefEmailID())
                && emailRequiredForPartners != null
                && mData.publishingHouse.publishingHouseName != null
                && emailRequiredForPartners.toLowerCase().contains(mData.publishingHouse.publishingHouseName.toLowerCase())
                && ApplicationController.ENABLE_OTP_LOGIN) {
//                TODO launch login fragment
            String title = "";
            if (mData != null
                    && mData.generalInfo != null
                    && !TextUtils.isEmpty(mData.generalInfo.title)) {
                title = mData.generalInfo.title;
            }
            AlertDialogUtil.showAlertDialog(mContext,
                    mContext.getString(R.string.msg_login_with_email) + " " + title,
                    "",
                    mContext.getString(R.string.dialog_cancel),
                    mContext.getString(R.string.dialog_ok),
                    new AlertDialogUtil.DialogListener() {
                        @Override
                        public void onDialog1Click() {

                        }

                        @Override
                        public void onDialog2Click() {
                            if (mContext != null) {
                                launchLoginActivity();
                            }
                        }
                    });
            return false;
        }
        /*if (!PrefUtils.getInstance().getprefIsAgeAbove18Plus()
                && !PrefUtils.getInstance().getPrefEnableParentalControl()
                && Util.isAdultContent(mData)) {
            AlertDialogUtil.showAlertDialog(mContext, mContext.getString(R.string.txt_adult_warning), "", false,
                    getApplicationContext().getString(R.string.go_back),
                    getApplicationContext().getString(R.string.confirm)
                    , new AlertDialogUtil.DialogListener() {

                        @Override
                        public void onDialog1Click() {

                        }

                        @Override
                        public void onDialog2Click() {
                            PrefUtils.getInstance().setPrefIsAgeAbove18Plus(true);
                            playContent();
                        }
                    });
            return false;
        }*/
        if (mData != null
                && mData.generalInfo != null
                && mData.generalInfo.type != null) {
            if (mData.generalInfo.type.equalsIgnoreCase
                    (APIConstants.TYPE_PROGRAM)) {
                if (null != mData && null != mData.startDate
                        && null != mData.endDate) {
                    Date startDate = Util.getDate(mData.startDate);
                    Date endDate = Util.getDate(mData.endDate);
                    Date currentDate = new Date();
                    if (!(currentDate.after(startDate)
                            && currentDate.before(endDate))
                            && !currentDate.after(endDate)) {
                        if (mReminderImage != null) {
                            mReminderImage.performClick();
                            return false;
                        }
                    }
                }
            }
        }
        // It is a wifi
        if (checkParentalControlEnabled() && !isParentalControlPinConfirmed) {
            showPINConfirmationDialog();
            return false;
        }
        return true;
    }

    private boolean checkParentalControlEnabled() {
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

    private void showPINConfirmationDialog() {
        ParentalControlDialog parentalControlDialog = new ParentalControlDialog(mContext, new ParentalControlDialog.ParentalControlOptionUpdateListener() {
            @Override
            public void onUpdateOption(boolean success) {
                SDKLogger.debug("is pin entered correct" + success);
                if (!success) {
                    return;
                }
                isParentalControlPinConfirmed = true;
                playContent();
            }
        });
        try {
            parentalControlDialog.showConfirmPINDialog();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private boolean checkLocalPlayback() {

        if (mData == null) {
            return false;
        }
        downloadMediaData = null;
        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
        CardDownloadData contentDownloadData = null;
        String contentKey = mData._id;
        if (mData.publishingHouse != null
                && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(mData.publishingHouse.publishingHouseName)
                || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(mData.publishingHouse.publishingHouseName))) {
            contentKey = mData.generalInfo.partnerId;
        }
        String downloadedKey = null;
        for (String key : downloadlist.mDownloadedList.keySet()) {
            CardDownloadData availableDownloadData = downloadlist.mDownloadedList.get(key);
            if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList == null) {
                downloadedKey = availableDownloadData._id;
                if (contentKey.equalsIgnoreCase(downloadedKey)) {
                    contentDownloadData = availableDownloadData;
                    break;
                }
            } else if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList != null) {
                for (CardDownloadData episode : availableDownloadData.tvEpisodesList) {
                    downloadedKey = episode._id;
                    if (contentKey.equalsIgnoreCase(downloadedKey)) {
                        contentDownloadData = episode;
                        break;
                    }
                }
            } else if (availableDownloadData.tvSeasonsList != null) {
                for (SeasonData seasonData : availableDownloadData.tvSeasonsList) {
                    for (CardDownloadData episode : seasonData.tvEpisodesList) {
                        downloadedKey = episode._id;
                        if (contentKey.equalsIgnoreCase(downloadedKey)) {
                            contentDownloadData = episode;
                            break;
                        }
                    }
                }
            }
        }
        if (contentDownloadData == null) {
            return false;
        }

        if (contentDownloadData != null
                && !contentDownloadData.mCompleted
                && contentDownloadData.mPercentage != 100) {
            return false;
        }
        if (APIConstants.isErosNowContent(contentDownloadData) && contentDownloadData != null && contentDownloadData.zipStatus < CardDownloadData.STATUS_FILE_UNZIPPED) {
            return false;
        }

// It is a wifi
        /*if (!PrefUtils.getInstance().getprefIsAgeAbove18Plus()
                && !PrefUtils.getInstance().getPrefEnableParentalControl()
                && Util.isAdultContent(mData)) {
            AlertDialogUtil.showAlertDialog(mContext, mContext.getString(R.string.txt_adult_warning), "", false,
                    getApplicationContext().getString(R.string.go_back),
                    getApplicationContext().getString(R.string.confirm)
                    , new AlertDialogUtil.DialogListener() {

                        @Override
                        public void onDialog1Click() {

                        }

                        @Override
                        public void onDialog2Click() {
                            PrefUtils.getInstance().setPrefIsAgeAbove18Plus(true);
                            playContent();
                        }
                    });
            return true;
        }*/

        // It is a wifi
        if (checkParentalControlEnabled() && !isParentalControlPinConfirmed) {
            showPINConfirmationDialog();
            return true;
        }

        if (mData != null && mData.localFilePath != null || contentDownloadData != null) {
            isDownloadedContentPlayback = true;
            onLastPausedTimeFetched(contentDownloadData.elapsedTime);
            if (contentDownloadData != null) {
                mData.localFilePath = contentDownloadData.mDownloadPath;
            }

            /*String url= mContext.getFilesDir().getAbsolutePath() + *//*File.separator +*//*
                    DownloadUtil.downloadVideosStoragePath + contentDownloadData.title
                    +APIConstants.UNDERSCORE+contentDownloadData._id;*/
            String url = mContext.getFilesDir().getAbsolutePath() + mData.localFilePath;
            if (new File(url).exists()) {
                LoggerD.debugHooqVstbLog("PlayContent download file exists url- " + url);
            } else {
                LoggerD.debugHooqVstbLog("PlayContent download file does not exists url- " + url);
            }
            if (contentDownloadData != null) {
                downloadMediaData = new DownloadMediadata(url, contentDownloadData.videoTrackId, contentDownloadData.audioTrackId, contentDownloadData.videoFileName, contentDownloadData.audioFileName, contentDownloadData.variantType);
            } else {
                downloadMediaData = new DownloadMediadata(url, 0, 0, null, null, null);
            }
            mData.offlinePlayerType = contentDownloadData.contentType;
            mData.offline_link = url;
            initPlayBack(downloadMediaData.url);
            return true;
        }
        return false;
    }

    private void launchLoginActivity() {
        if (mContext == null) {
            return;
        }
        isShowingAlertDialog=true;
        ((Activity) mContext).startActivityForResult(LoginActivity.createIntent(mContext, true, false, PARAM_SUBSCRIPTION_TYPE_NONE, mSource, mSourceDetails), MainActivity.INTENT_REQUEST_TYPE_LOGIN);
    }

    private String getLink(Map<String, String> pMap, String firstPref, String
            secondPref, String... profile) {
        for (String string : profile) {
            if (pMap.get(string + firstPref) != null) {
                if ((profileSelect == null || profileSelect.length() == 0) || mIsTrailerPlaying)
                    profileSelect = string;
                mStreamingFormat = firstPref;
                return pMap.get(profileSelect + firstPref);
            } else if (pMap.get(string + secondPref) != null) {
                if (profileSelect == null || profileSelect.length() == 0 || mIsTrailerPlaying)
                    profileSelect = string;
                mStreamingFormat = secondPref;
                return pMap.get(profileSelect + secondPref);
            }

        }
        return "";

    }

    private String getLink(Map<String, String> pMap, String firstPref, String
            secondPref, String thirdPref, String... profile) {
        for (String string : profile) {
            if (pMap.get(string + firstPref) != null) {
                if ((profileSelect == null || profileSelect.length() == 0 || isTrailerPlayed))
                    profileSelect = string;
                mStreamingFormat = firstPref;
                return pMap.get(profileSelect + firstPref);
            } else if (pMap.get(string + secondPref) != null) {
                if (profileSelect == null || profileSelect.length() == 0 || isTrailerPlayed)
                    profileSelect = string;
                mStreamingFormat = secondPref;
                return pMap.get(profileSelect + secondPref);
            } else if (pMap.get(string + thirdPref) != null) {
                if (profileSelect == null || profileSelect.length() == 0 || isTrailerPlayed)
                    profileSelect = string;
                mStreamingFormat = thirdPref;
                return pMap.get(profileSelect + thirdPref);
            }
        }
        return "";
    }

    private String TYPE_2G = "2G";


    private String chooseVodStreamType(List<CardDataVideosItem> items) {

        String TYPE_WIFI = "WIFI";
        String TYPE_5G = "5G";
        String TYPE_4G = "4G";
        String TYPE_3G = "3G";

        HashMap<String, String> profileMap = new HashMap<>();
        String mConnectivity = SDKUtils.getInternetConnectivity(mContext);
        int ellapsedTime = 0;
        for (CardDataVideosItem item : items) {
            if (!APIConstants.VIDEO_TYPE_DOWNLOAD.equalsIgnoreCase(item.type)) {
                ellapsedTime = item.elapsedTime;
                adEventEllapsedTime = item.elapsedTime;
            }
            if (item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVEDVR)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVE)
                    && item.format.equalsIgnoreCase(APIConstants.STREAMDASH)
                    && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(VIDEOQUALTYHIGH) && item.format.equalsIgnoreCase(APIConstants.STREAMDASH) && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(VIDEOQUALTYLOW) && item.format.equalsIgnoreCase(APIConstants.STREAMDASH) && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(TYPE_WIFI) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))) {
                profileMap.put(item.profile + item.format, item.link);

            }else if ((item.profile.equalsIgnoreCase(TYPE_5G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))) {
                profileMap.put(item.profile + item.format, item.link);
            }  else if ((item.profile.equalsIgnoreCase(TYPE_4G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(TYPE_3G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(TYPE_2G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(TYPE_WIFI) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(TYPE_5G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)) {
                profileMap.put(item.profile + item.format, item.link);
            }else if ((item.profile.equalsIgnoreCase(TYPE_4G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(TYPE_3G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(TYPE_2G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(VIDEOQUALTYHIGH) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP) && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(VIDEOQUALTYLOW) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP) && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(VIDEOQUALTYHIGH) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP) && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(VIDEOQUALTYLOW) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP) && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            }

        }

        onLastPausedTimeFetched(ellapsedTime);
        if (mConnectivity.equalsIgnoreCase(TYPE_WIFI)) {
            return (getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHTTP,
                    new String[]{APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW, APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE, TYPE_WIFI, TYPE_5G,TYPE_4G, TYPE_3G, TYPE_2G}));
        } else if (mConnectivity.equalsIgnoreCase(TYPE_5G)) {
            return (getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHTTP,
                    new String[]{APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW, APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE, TYPE_5G,TYPE_4G, TYPE_3G, TYPE_2G}));
        }else if (mConnectivity.equalsIgnoreCase(TYPE_4G)) {
            return (getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHTTP,
                    new String[]{APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW, APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE, TYPE_4G, TYPE_3G, TYPE_2G}));
        } else if (mConnectivity.equalsIgnoreCase(TYPE_3G)) {
            return (getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHTTP,
                    new String[]{APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW, APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE, TYPE_3G, TYPE_2G}));
        } else if (mConnectivity.equalsIgnoreCase(TYPE_2G)) {
            return (getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHTTP,
                    new String[]{APIConstants.VIDEOQUALTYLOW, APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE, TYPE_2G}));
        } else {
            return (getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHTTP,
                    new String[]{APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW, APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE, TYPE_3G, TYPE_2G}));
        }

    }

    private void chooseLiveStreamType(List<CardDataVideosItem> items, boolean isTrailer) {
        HashMap<String, String> profileMap = new HashMap<String, String>();
        int ellapsedTime = 0;

        for (CardDataVideosItem item : items) {
            if (!APIConstants.VIDEO_TYPE_DOWNLOAD.equalsIgnoreCase(item.type)) {
                ellapsedTime = item.elapsedTime;
            }
            if (item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVE) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVE)
                    && item.format.equalsIgnoreCase(APIConstants.STREAMDASH)
                    && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(VIDEOQUALTYHIGH) && item.format.equalsIgnoreCase(APIConstants.STREAMDASH) && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(VIDEOQUALTYLOW) && item.format.equalsIgnoreCase(APIConstants.STREAMDASH) && item.type.equalsIgnoreCase(APIConstants.STREAMING)) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATRTSP))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATRTSP))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYLOW)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if ((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYLOW)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATRTSP))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVEDVR) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))) {
                profileMap.put(item.profile + item.format, item.link);
            } else if (item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYMEDIUM) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))) {
                profileMap.put(item.profile + item.format, item.link);
            }
        }

        onLastPausedTimeFetched(ellapsedTime);

        String mConnectivity = SDKUtils.getInternetConnectivity(mContext);

        if (mConnectivity.equalsIgnoreCase("wifi")) {
            initPlayBack(getLink(profileMap, APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMINGFORMATRTSP,
                    new String[]{APIConstants.STREAMADAPTIVEDVR, APIConstants
                            .STREAMADAPTIVE, PrefUtils.getInstance()
                            .getProfileWifi()
                            ,PrefUtils.getInstance().getProfile5G()
                            , PrefUtils.getInstance().getProfile4G()
                            , PrefUtils.getInstance().getProfile3G()
                            , PrefUtils.getInstance().getProfile2G()
                            , APIConstants.VIDEOQUALTYVERYHIGH, APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYMEDIUM, APIConstants.VIDEOQUALTYLOW,}));

        } else if (mConnectivity.equalsIgnoreCase("3G")) {
            initPlayBack(getLink(profileMap, APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMINGFORMATRTSP,
                    new String[]{APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE,
                            PrefUtils.getInstance().getProfile3G()
                            , PrefUtils.getInstance().getProfile2G(),
                            APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYMEDIUM, APIConstants.VIDEOQUALTYLOW}));
        } else if (mConnectivity.equalsIgnoreCase("2G")) {
            initPlayBack(getLink(profileMap, APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHLS, APIConstants.STREAMINGFORMATRTSP,
                    new String[]{APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE, PrefUtils.getInstance().getProfile2G(), APIConstants.VIDEOQUALTYLOW}));
        } else if (mConnectivity.equalsIgnoreCase("4G")) {
            initPlayBack(getLink(profileMap, APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMINGFORMATRTSP,
                    new String[]{APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE,
                            PrefUtils.getInstance().getProfile4G(),
                            PrefUtils.getInstance().getProfile3G(),
                            PrefUtils.getInstance().getProfile2G(),
                            APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYMEDIUM, APIConstants.VIDEOQUALTYLOW}));

//            Toast.makeText(mContext,mContext.getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        } else if (mConnectivity.equalsIgnoreCase("5G")) {
            initPlayBack(getLink(profileMap, APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMINGFORMATRTSP,
                    new String[]{APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE,
                            PrefUtils.getInstance().getProfile5G(),
                            PrefUtils.getInstance().getProfile4G(),
                            PrefUtils.getInstance().getProfile3G(),
                            PrefUtils.getInstance().getProfile2G(),
                            APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYMEDIUM, APIConstants.VIDEOQUALTYLOW}));

//            Toast.makeText(mContext,mContext.getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        } else {
            initPlayBack(getLink(profileMap, APIConstants.STREAMDASH,
                    APIConstants.STREAMINGFORMATHLS,
                    APIConstants.STREAMINGFORMATRTSP,
                    new String[]{APIConstants.STREAMADAPTIVEDVR, APIConstants
                            .STREAMADAPTIVE, PrefUtils.getInstance()
                            .getProfileWifi()
                            , PrefUtils.getInstance().getProfile5G()
                            , PrefUtils.getInstance().getProfile4G()
                            , PrefUtils.getInstance().getProfile3G()
                            , PrefUtils.getInstance().getProfile2G()
                            , APIConstants.VIDEOQUALTYVERYHIGH, APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYMEDIUM, APIConstants.VIDEOQUALTYLOW,}));

        }


    }

    private void setBitrateForTrailer(String url) {
        if (url == null) return;
        String bitrateTrailer = null;
        if (url.contains("vhigh")) profileSelect = "high";
        if (url.contains("low")) profileSelect = "low";
        if (url.contains("medium")) profileSelect = "medium";
    }

    protected Uri mContentVideoUri;

    public void initPlayBack(String url) {
//        url = "https://s3-ap-southeast-1.amazonaws.com/apalyademo/501/sd/501_sd.mpd";
//        url = "https://preprod-cdn.cloud.altbalaji.com/content/2016-06/472-57604d3c12582/manifest.mpd";
        //Log.d(TAG, "MyUrl Got the link for playback = " + url);
        long durationInSeconds = 0;
        if (mData.content != null && mData.content.duration != null) {
            durationInSeconds = Util.calculateDurationInSeconds(mData.content.duration);
        }

        if (url == null) {
            quitPlayer();
            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.playerStatusUpdate("No url to play.");

            }
            Analytics.gaNotificationPlayBackFailed(mNotificationTitle, mNid, mData, profileSelect);
            Analytics.mixPanelUnableToPlayVideo(mData, "No url to play.", profileSelect);
            AlertDialogUtil.showToastNotification("No url to play.");
            return;
        }
        isPlayingDVR = false;
        if (url != null && url.toLowerCase().contains(ExoPlayerView.DVR_URI_FLAG)) {
            isPlayingDVR = true;
        }
        mContentVideoUri = Uri.parse(url);
        if (mVideoViewPlayer != null && mVideoViewPlayer instanceof ExoPlayerView) {
            mVideoViewPlayer.setStreamName(mData.globalServiceId);
        }
        if (mVideoViewPlayer != null && mVideoViewPlayer instanceof ExoPlayerView) {
            ((ExoPlayerView) mVideoViewPlayer).setSubtitleView(subtitleView);
        }
        //FirebaseAnalytics.getInstance().FAContentDetailViewed(mData,durationInSeconds,mSource,mSourceDetails);
        //setBitrateForTrailer(url); //url not mContentVideoUri
        initializeVideoPlay(mContentVideoUri);
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setFullScreen(boolean isFullScreen) {
        if (mVideoViewPlayer != null) {
            mVideoViewPlayer.setFullScreen(isFullScreen);
        }
        this.isFullScreen = isFullScreen;
        if (!isMediaControllerEnabled || isFullScreen || !isMediaPlaying()) {
            mBackIconImageView.setImageResource(R.drawable.back_icon_player);
            /*if(isFromCatchup && isFullScreen)
                goLiveText.setVisibility(View.VISIBLE);
            else
                goLiveText.setVisibility(GONE);*/
        } else if (isMediaControllerEnabled) {
            mBackIconImageView.setImageResource(R.drawable.back_icon_player);
//            goLiveText.setVisibility(GONE);
        }
        if (isFullScreen) {
            if (isFromCatchup && !mIsAdPlaying) {
                goLiveText.setVisibility(VISIBLE);
                next_content_image.setVisibility(GONE);
                previous_content_image.setVisibility(GONE);
                previousChannelName.setVisibility(GONE);
                previousProgramName.setVisibility(GONE);
                nextProgramName.setVisibility(GONE);
                nextChannelName.setVisibility(GONE);
                previousContentLL.setVisibility(GONE);
                nextContentLL.setVisibility(GONE);
            } else {
                goLiveText.setVisibility(GONE);
                getMediaControllerInstance().rvAudioTracks.setVisibility(GONE);
                if(!mIsAdPlaying) {
                    previousContentLL.setVisibility(VISIBLE);
                    nextContentLL.setVisibility(VISIBLE);
                    nextProgramName.setVisibility(VISIBLE);
                    nextChannelName.setVisibility(VISIBLE);
                }
            }
            mBackIconImageView.setVisibility(VISIBLE);
        } else {
            nextContentLL.setVisibility(GONE);
            previousContentLL.setVisibility(GONE);
            mBackIconImageView.setVisibility(VISIBLE);
            goLiveText.setVisibility(GONE);
        }

    }

    private void clevertapVideoStarted() {
        if (mData == null
                || mData.generalInfo == null
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || mSavedContentPosition <= 0) {
            CleverTap.eventVideoStarted(mRelatedCardData, mData, downloadMediaData != null,
                    mSource, mSourceDetails, mSourceTab, sourceCarouselPosition, mSavedContentPosition);
        }
    }

    public void onLastPausedTimeFetched(int ellapseTime) {
        LoggerD.debugLog("onLastPausedTimeFetched: elapsedTime- " + ellapseTime);

        if (ellapseTime > 0) {
            mSavedContentPosition = ellapseTime * 1000;
//            nextPositionToShowAdInMilliSeconds = mSavedContentPosition + MIDROLL_AD_CUE_POINT_IN_MINTS;
        }
        LoggerD.debugLog("mSavedContentPosition- " + mSavedContentPosition + " nextPositionToShowAdInMilliSeconds-" + nextPositionToShowAdInMilliSeconds);
    }

    protected VideoViewPlayer.OnLicenseExpiry onLicenseExpiryListener = new VideoViewPlayer.OnLicenseExpiry() {

        @Override
        public void licenseExpired() {

            if (mContext == null || !(mContext instanceof Activity)) {
                return;
            }

            ((Activity) mContext).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    AlertDialogUtil.showToastNotification("License Expired.");
//					PackagePopUp popup = new PackagePopUp(mContext,(View)mParentLayout.getParent());
//					myplexapplication.getCardExplorerData().cardDataToSubscribe =  mData;
//					popup.showPackDialog(mData, ((Activity)mContext).getActionBar().getCustomView());

                }
            });
        }
    };


    private OnClickListener mResumeClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mErrorLayout == null || mVideoViewPlayer == null) {
                return;
            }
            mErrorLayout.setVisibility(INVISIBLE);
            mVideoViewPlayer.onResume();

        }
    };

    protected void retryPlayback() {

        if (mData == null || mData.generalInfo == null || mData.generalInfo.type == null) {
            return;
        }

        if (isMinimized) return;


        mProgressBarLayout.setVisibility(INVISIBLE);
        mThumbnailPlay.setVisibility(VISIBLE);
        mPreviewImage.setVisibility(INVISIBLE);
        mPreviewImageOverlay.setVisibility(INVISIBLE);
        mPlayerState = WAIT_FORRETRY;
        mErrorLayout.setVisibility(VISIBLE);
//         cardmediasubitem_retryButton
        Button retryButton = (Button) mErrorLayout.findViewById(R.id.cardmediasubitem_retryButton);
        retryButton.setAllCaps(false);
        retryButton.setOnClickListener(mPlayerClickListener);
        retryButton.setText(mContext.getString(R.string.play_button_retry));

        // for vod and movies
        isRetryPlayBak = true;

        if (!(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type))) {

            TextView textView = (TextView) mErrorLayout.findViewById(R.id.cardmediasubitem_retrytext);
            textView.setVisibility(VISIBLE);
            textView.setText(mContext.getString(R.string.play_msg_err));
            return;
        }

        // for live, sports live and sports vod content

        mAutoRetry++;
        boolean profileSelected = false;
        if (!TextUtils.isEmpty(profileSelect) && mData.videoInfo != null
                && mData.videoInfo.profiles != null) {
            String lowerQuality = null;
            // re-try with lower bitrate
            List<String> profiles = mData.videoInfo.profiles;

            for (String profile : profiles) {

                if (profile.equalsIgnoreCase(profileSelect)) {
                    //Log.d(TAG, "profile's: profileSelect- " + profileSelect);
                    profileSelected = true;
                    profileSelect = lowerQuality;
                    //Log.d(TAG, "profile's: update profile- " + profile + " profileSelect- " + profileSelect + " lowerQuality- " + lowerQuality);
                    break;
                }
                lowerQuality = profile;
                //Log.d(TAG, "profile's: profileSelect- " + profileSelect + " lowerQuality- " + lowerQuality);
            }

        }
        if (!profileSelected) {
            pickRetryPlayBackProfile();
        }

        camAngleSelect = langSelect = "";

        TextView textView = (TextView) mErrorLayout.findViewById(R.id.cardmediasubitem_retrytext);

        if (mAutoRetry > MAX_AUTO_RETRY) {

            textView.setVisibility(VISIBLE);
            textView.setText(mContext.getString(R.string.play_msg_err));
            return;
        }

        textView.setVisibility(VISIBLE);
        textView.setText(mContext.getString(R.string.play_msg_err_retrying));

        mVideoViewParent.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mPlayerState != WAIT_FORRETRY) {
                    return;
                }
                mErrorLayout.setVisibility(INVISIBLE);
                AlertDialogUtil.showToastNotification(mContext
                        .getString(R.string.play_retry_lower_bitrate));

                //Handled the player loading when the Rent a button is showing
                if(PrefUtils.getInstance().getSubscriptionStatusString()!=null ) {
                    if (PrefUtils.getInstance().getSubscriptionStatusString().equalsIgnoreCase(APIConstants.USER_NOT_SUBSCRIBED)
                            && mData !=null && mData.generalInfo!=null&& mData.generalInfo.contentRights!=null
                            && mData.generalInfo.contentRights.size()>0 &&mData.generalInfo.contentRights.get(0)!=null
                            && mData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD) ) {
                        mThumbnailPlay.setVisibility(GONE);
                    } else {
                        getAdvertisingID();
                    }
                }
            }
        }, INTERVAL_RETRY);

    }

    public void onPause() {
        if (mVideoViewPlayer != null) {
            isMediaPaused = true;
            mVideoViewPlayer.onPause();
        }
       /* if (mCastManager == null) {
            mCastManager = VideoCastManager.getInstance();
        }
        if (mCastManager != null) {
            mCastManager.removeVideoCastConsumer(mCastConsumer);
        }*/
        removeHandlerCallbacksIfAny();
        if (mIsAdDisplayed) {
          /*  if (vmaxAdView != null) {
                vmaxAdView.pauseInstreamAd();
            }*/
        }
    }

    public void onDestroy() {
        if (youtubePlayer != null) {
            youtubePlayer.release();
        }
        if (mHandlerdeday != null && mPlayerRunnable != null) {
            mHandlerdeday.removeCallbacks(mPlayerRunnable);
        }
        stopRepeatingTask();
    }

    public void onResume() {
        /*if (mCastManager == null) {
            mCastManager = VideoCastManager.getInstance();
        }
        if (mCastManager != null) {
            mCastManager.removeVideoCastConsumer(mCastConsumer);
        }
        mCastManager.addVideoCastConsumer(mCastConsumer);*/

        if (mIsAdDisplayed) {
            playVideoContent(null);
            mIsAdPlaying=false;
            mIsAdDisplayed=false;
           /* if (vmaxAdView != null) {
                vmaxAdView.resumeInstreamAd();
                return;
            }*/
        }
        if (mVideoViewPlayer != null && mVideoViewPlayer.wasPlayingWhenPaused()) {
            LoggerD.debugHooqVstbLog("onResume: wasPlayingWhenPaused- " + mVideoViewPlayer.wasPlayingWhenPaused());
            isMediaPaused = false;
            //showPlayButton();
            mThumbnailPlay.setVisibility(GONE);
            mReminderImage.setVisibility(GONE);
            mVideoViewParent.setOnClickListener(null);
            mVideoViewPlayer.onResume();
            if (mData != null && !mData.isLive() && !mData.isProgram()) {
                contentProgressHandler.postDelayed(onEveryTimeInterval, 1000);
            }
        }
        if (isYouTubePlayerLaunched()) {
            quitPlayer();
        }


    }

    private String getContentType() {
        if (mData != null && mData.currentUserData != null && mData.currentUserData.purchase != null) {
            for (CardDataPurchaseItem data : mData.currentUserData.purchase) {
                if (data.contentType != null) {
                    return data.contentType;
                }
            }
        }

        return null;
    }

    private String getDrmProfileString() {

        if (mData != null && mData.currentUserData != null && mData.currentUserData.purchase != null) {
            for (CardDataPurchaseItem data : mData.currentUserData.purchase) {
                if (data.contentType != null && data.contentType.equalsIgnoreCase(APIConstants.VIDEOQUALTYHD)) {
                    return "profile:1";
                }
            }
        }

        return "profile:0";

    }

    public void onBufferingEnd() {
        if (mProgressBarLayout != null && mProgressBarLayout.getVisibility() == VISIBLE && !isMinimized) {

            if (mBufferPercentage != null) {
                mBufferPercentage.setVisibility(INVISIBLE);
            }
            mProgressBarLayout.setVisibility(INVISIBLE);
        }
    }

    public long getTotalplayedTimeInMinutes() {
        if (mMouTracker == null) {
            return 0;
        }
        return mMouTracker.getTotalPlayedTimeInMinutes();
    }

    public long getTotalplayedTimeInSeconds() {
        if (mMouTracker == null) {
            return 0;
        }
        return mMouTracker.getTotalPlayedTimeInSeconds();
    }

    private int mEpgDatePosition = 0;

    public void setEpgDatePosition(int epgDatePosition) {
        mEpgDatePosition = epgDatePosition;
    }

    public boolean isAdPausedWhilePlaying() {
        return mIsAdDisplayed;
    }

    public long getmSavedContentPosition() {
        if (mVideoViewPlayer == null) return 0;
        return mVideoViewPlayer.getCurrentPosition();
    }

    public void setmSavedContentPosition(long mSavedContentPosition) {
    }

    protected String mNid = null;

    public void setNid(String nid) {
        mNid = nid;
    }

    public boolean isPlayingDVR() {
        return isPlayingDVR;
    }

    public void showMediaController() {
        if (mVideoViewPlayer != null
                && (!isMinimized && !isHelpScreenShowing)
                && isMediaPlaying()
                && !isAdPlaying()) {
            LoggerD.debugLog("showMediaController");
            mVideoViewPlayer.showMediaController();
        }
    }

    public void setShowingHelpScreen(boolean isTimeShiftHelpScreenShowing) {
        this.isHelpScreenShowing = isTimeShiftHelpScreenShowing;
    }

    private void launchYoutubePlayer(String link) {
        /*isYouTubePlayerLaunched = true;
        mPreviewImage.setVisibility(View.VISIBLE);
        mVideoViewParent.setEnabled(true);
        Util.launchYouyubePlayer((Activity) mContext, link);*/
        mProgressBarLayout.setVisibility(INVISIBLE);
        attachAndLoadYoutubeVideoView(link);
        LoggerD.debugLog("launchYoutubePlayer link- " + link);
    }

    private String getYoutubeLink(CardDataVideos videos) {
        if (videos == null) {
            return null;
        }
        for (CardDataVideosItem videosItem : videos.values) {
            if (APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(videosItem.format)) {
                return videosItem.link;
            }
        }
        return null;
    }


    private void pickRetryPlayBackProfile() {
        if (mData != null
                && mData.videos != null
                && mData.videos.values != null) {
            for (CardDataVideosItem videosItem : mData.videos.values) {
                if (videosItem != null
                        && videosItem.profile != null
                        && profileSelect != null
                        && !videosItem.profile.equalsIgnoreCase(profileSelect)
                        && !TextUtils.isEmpty(videosItem.format)
                        && !TextUtils.isEmpty(videosItem.link)
                        && !APIConstants.VIDEO_TYPE_DOWNLOAD.equalsIgnoreCase(videosItem.type)) {
                    profileSelect = videosItem.profile;
                    break;
                }
            }
        }
        //Log.d(TAG, "profileSelect- " + profileSelect);
    }

    public void hideMediaController() {
        if (mVideoViewPlayer != null)
            mVideoViewPlayer.hideMediaController();
    }

    public boolean isMediaControllerEnabled() {
        return isMediaControllerEnabled;
    }

    private boolean isMediaControllerEnabled = true;

    public void allowMediaController(boolean enable) {
        if (enable && !isFullScreen && !isMinimized && mBackIconImageView!=null) {
            mBackIconImageView.setImageResource(R.drawable.icon_back_arrow_new);
        } else {
            mBackIconImageView.setImageResource(R.drawable.back_icon_player);
        }

        if (mVideoViewPlayer != null && !isMinimized) {
            isMediaControllerEnabled = enable;
            mVideoViewPlayer.allowMediaController(enable);
        }
    }

    public boolean isAdPlaying() {
        return mIsAdDisplayed;
    }

    private void showBufferProgress() {
        if (mBufferPercentage != null) {
            mBufferPercentage.setVisibility(VISIBLE);
        }
        mProgressBarLayout.setVisibility(VISIBLE);
    }

    private void hideBufferProgress() {
        if (mBufferPercentage != null) {
            mBufferPercentage.setVisibility(INVISIBLE);
        }
        mProgressBarLayout.setVisibility(INVISIBLE);
    }

    public View getPlayerSeekBarView() {
        if (mediacontrolls != null) {
            return mediacontrolls.findViewById(R.id.mediacontroller_progress);
        } else {
            return null;
        }
    }

    private String mTrailerContentId;
    private boolean mIsTrailerPlaying = false;
    private boolean isTrailerPlayed = false;

    public void playTrailerContent() {
        if (mData == null
                || mData.relatedMultimedia == null
                || mData.relatedMultimedia.values.isEmpty()) {
            return;
        }
        if (mIsTrailerPlaying) {
//            AlertDialogUtil.showToastNotification(mContext.getString(R.string.text_trailer_is_already_playing));
            return;
        }
        for (CardDataRelatedMultimediaItem relatedMultimediaItem : mData.relatedMultimedia.values) {
            if (relatedMultimediaItem.generalInfo != null
                    && APIConstants.TYPE_TRAILER.equalsIgnoreCase(relatedMultimediaItem.generalInfo.type)) {
                if (isMediaPlaying()) {
                    mSavedContentPosition = mVideoViewPlayer.getCurrentPosition();
                    closePlayer();
                }
                mTrailerContentId = relatedMultimediaItem._id;
                mIsTrailerPlaying = true;
                isTrailerPlayed = true;
                //profileSelect=null;
                playContent();
            }
        }
    }

    public void playTrailerContent(int position) {
        if (mData == null
                || mData.relatedMultimedia == null
                || mData.relatedMultimedia.values.isEmpty()) {
            return;
        }
        if (mIsTrailerPlaying) {
//            AlertDialogUtil.showToastNotification(mContext.getString(R.string.text_trailer_is_already_playing));
            return;
        }
        if (position != -1) {
            CardDataRelatedMultimediaItem relatedMultimediaItem = mData.relatedMultimedia.values.get(position);
            if (relatedMultimediaItem.generalInfo != null
                    && APIConstants.TYPE_TRAILER.equalsIgnoreCase(relatedMultimediaItem.generalInfo.type)) {
                if (isMediaPlaying()) {
                    mSavedContentPosition = mVideoViewPlayer.getCurrentPosition();
                    closePlayer();
                }
                mTrailerContentId = relatedMultimediaItem._id;
                mIsTrailerPlaying = true;
                isTrailerPlayed = true;
                playContent();
            }
        } else {
            AlertDialogUtil.showToastNotification("Unable to play selected content");
        }
        /*for (CardDataRelatedMultimediaItem relatedMultimediaItem : mData.relatedMultimedia.values) {

        }*/
    }

    protected JSONObject jsonObj = null;

    public boolean isCastPopupShowing;

    protected synchronized void playContentOnCastTV(Uri uri) {
        // uri = Uri.parse("https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd");
        if (mCastManager != null && !isConnected() || isDownloadedContentPlayback) {
            LoggerD.debugLog("sundirectCastManager: castmanager is not connected or it is a downloaded content");
            isCastPopupShowing = false;
            return;
        }
        if (mChromeCastDialog != null && mChromeCastDialog.isShowing()) {
            isCastPopupShowing = false;
            return;
        }
        if (isMediaPlaying()) {
            closePlayer();
        }
        final Uri uri1 = uri;
        try {
            jsonObj = new JSONObject();
            jsonObj.put(APIConstants.PARAM_CHROME_CAST_DESCRIPTION, "subtitle");
            if (mData != null) {
                //jsonObj.put(PARAM_CHROME_CAST_DRM_LICENSE_URL, APIConstants.getDRMLicenseUrl(mData._id, APIConstants.STREAMING));
                if (mData.videos != null && mData.videos.values != null && mData.videos.values.size() > 0 && mData.videos.values.get(0) != null && mData.videos.values.get(0).licenseUrl != null) {
                    jsonObj.put(PARAM_CHROME_CAST_DRM_LICENSE_URL, mData.videos.values.get(0).licenseUrl);
                    jsonObj.put(PARAM_CHROME_CAST_LICENSE_URL, mData.videos.values.get(0).licenseUrl);
                    Log.e("ChromeCast", "drmLicenseUrl" + mData.videos.values.get(0).licenseUrl);
                } else if (mData.videos != null && mData.videos.values != null && mData.videos.values.size() > 0 && mData.videos.values.get(0) != null && mData.videos.values.get(0).license_url != null) {
                    jsonObj.put(PARAM_CHROME_CAST_DRM_LICENSE_URL, mData.videos.values.get(0).license_url);
                    Log.e("ChromeCast", "drmLicenseUrl" + mData.videos.values.get(0).license_url);
                } else {
                    jsonObj.put(PARAM_CHROME_CAST_DRM_LICENSE_URL, APIConstants.getDRMLicenseUrl(mData._id, APIConstants.STREAMING));
                    Log.e("ChromeCast", "drmLicenseUrl" + APIConstants.getDRMLicenseUrl(mData._id, APIConstants.STREAMING));
                }
            }
            // jsonObj.put(PARAM_CHROME_CAST_DRM_LICENSE_URL, "https://proxy.uat.widevine.com/proxy?video_id=GTS_SW_SECURE_CRYPTO&provider=widevine_test");
            String partnerType = APIConstants.VALUE_CHROME_CAST_APALYA;
            if (mData != null
                    && mData.publishingHouse != null
                    && !TextUtils.isEmpty(mData.publishingHouse.publishingHouseName)) {
                partnerType = mData.publishingHouse.publishingHouseName;
            }
            jsonObj.put(PARAM_CHROME_CAST_PARTNER_TYPE, partnerType.toLowerCase());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to add description to the json object", e);
        }
        Log.e("ChromeCast", "title" + mData.generalInfo.title);
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mData.generalInfo.description);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mData.generalInfo.title);
        String imageLink = getImageLink();
        if(imageLink !=null) {
            movieMetadata.addImage(new WebImage(Uri.parse(imageLink)));
        }
        String url2 = uri1.toString();
//        url2 = "http://85mum-content.hungama.com/1218/4/FF-2016-00000908/stream.mpd";
//        url2 = "http://85-content.hungama.com/1218/4/FF-2016-00000300/stream.mpd";

        List<MediaTrack> mediaTracks = new ArrayList<>();
        if (mData != null && mData.subtitles != null && mData.subtitles.values != null && mData.subtitles.values.size() > 0) {
            for (int i = 0; i < mData.subtitles.values.size(); i++) {
                MediaTrack subtitles = new MediaTrack.Builder(1, MediaTrack.TYPE_TEXT)
                        .setName(mData.subtitles.values.get(i).language)
                        .setSubtype(MediaTrack.SUBTYPE_CAPTIONS)
                        .setContentId(mData.subtitles.values.get(i).link_sub)
                        // language is required for subtitle type but optional otherwise
                        .setLanguage("en‐US")
                        .build();
                Log.e("link_sub", "" + mData.subtitles.values.get(i).link_sub + ".vtt");
                mediaTracks.add(subtitles);
            }
        }

        if (mediaTracks.size() > 0) {
            mMediaInfo = new MediaInfo.Builder(url2)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(APIConstants.PARAM_CHROME_CAST_CONTENT_TYPE)
                    .setMetadata(movieMetadata)
                    .setMediaTracks(mediaTracks)
                    .setStreamDuration(1000)
                    .setCustomData(jsonObj)
                    .build();
        } else {
            mMediaInfo = new MediaInfo.Builder(url2)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(APIConstants.PARAM_CHROME_CAST_CONTENT_TYPE)
                    .setMetadata(movieMetadata)
                    .setStreamDuration(1000)
                    .setCustomData(jsonObj)
                    .build();
        }
        Log.e(TAG, "onApplicationLaunched() is reached");
        if (mChromeCastDialog == null)
            mChromeCastDialog = new DialogCastOrPlayLocally(mContext);
        mChromeCastDialog.showDialog(new DialogCastOrPlayLocally.OnDailogClickListener() {
            @Override
            public void onChoosePlayOnTV() {
                if(DeviceUtils.isTablet(mContext))
                    chromeCastPopup = true;
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isCastPopupShowing = false;
                playVideo(mMediaInfo, jsonObj);
                Analytics.gaPlayedOnCastTV(Analytics.ACTION_TYPES.play.name(), mData);
                ComScoreAnalytics.getInstance().setEventCast(mData, sourceCarouselPosition, sourceCarouselPosition,
                        mSourceTab, mSource, mSourceDetails, "chromecast");
            }

            @Override
            public void dismissProgressBar() {
                hidePlayerLoading();
            }

            @Override
            public void onChoosePlayLocally() {
                isCastPopupShowing = false;
                Log.e(TAG, "video url " + uri1);
                isToPlayOnLocalDevice = true;
                MediaRouter mMediaRouter = (MediaRouter) mContext.getSystemService(Context.MEDIA_ROUTER_SERVICE);
                mMediaRouter.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouter.getDefaultRoute());
                if (mVideoViewPlayer != null
                        && mVideoViewPlayer.wasPlayingWhenPaused()) {
                    onResume();
                    return;
                }
                startPlayBackWithAdOrVideo();
            }
        });
    }


    protected void playVideo(MediaInfo mediaInfo, JSONObject jsonObj) {
        try {
            CastSession castSession = CastContext.getSharedInstance(mContext)
                    .getSessionManager()
                    .getCurrentCastSession();
            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
                @Override
                public void onStatusUpdated() {
                    Log.e("CHROMECAST", "onStatusUpdated");

                }

                @Override
                public void onMetadataUpdated() {
                    Log.e("CHROMECAST", "onMetadataUpdated");

                }

                @Override
                public void onQueueStatusUpdated() {
                    Log.e("CHROMECAST", "onQueueStatusUpdated");
                }

                @Override
                public void onPreloadStatusUpdated() {
                    Log.e("CHROMECAST", "onPreloadStatusUpdated");
                }

                @Override
                public void onSendingRemoteMediaRequest() {
                    Log.e("CHROMECAST", "onSendingRemoteMediaRequest");
                }

                @Override
                public void onAdBreakStatusUpdated() {
                    Log.e("CHROMECAST", "onAdBreakStatusUpdated");

                }
            });

            remoteMediaClient.load(mediaInfo);
            mProgressBarLayout.setVisibility(GONE);
            mPreviewImage.setVisibility(VISIBLE);
            mPreviewImageOverlay.setVisibility(VISIBLE);
            Log.d("ChromeCast", "playVideo mediaInfo.getContentId- " + mediaInfo.getContentId() + "" +
                    " mSavedContentPosition- " + mSavedContentPosition);
            if (isMediaPlaying()
                    && mVideoViewPlayer != null
                    && mVideoViewPlayer.getPositionWhenPaused() > 500) {
                mSavedContentPosition = mVideoViewPlayer.getPositionWhenPaused();
            }
            Intent intent = new Intent(mContext, ExpandedControlsActivity.class);
            intent.putExtra(APIConstants.IS_AD_BEING_PLAYED, false);
            if (mData.subtitles != null && mData.subtitles.values != null && mData.subtitles.values.size() > 0
                    && mData.subtitles.values.get(0) != null && mData.subtitles.values.get(0).link_sub != null) {
                intent.putExtra(APIConstants.IS_SUBTITLES_AVAILABLE, true);
            } else {
                intent.putExtra(APIConstants.IS_SUBTITLES_AVAILABLE, false);
            }
            if (Util.checkActivityPresent(mContext)) {
                mContext.startActivity(intent);
            }
            //mCastManager.startVideoCastControllerActivity(mContext, mediaInfo, mSavedContentPosition, true);
        } catch (Exception e) {
            e.printStackTrace();
            ((MainActivity) mContext).checkPlayServices();
        }

    }


    public void quitPlayer() {
        if (mPlayerStatusListener != null) {
            mPlayerStatusListener.onClosePlayer();
        }
        isParentalControlPinConfirmed = false;
        removeHandlerCallbacksIfAny();
        //  setFullScreen(false);
        closePlayer();
    }

    private void hideAllControlls() {
        if (mHandler == null || mHideRunnable == null) {
            return;
        }
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.postDelayed(mHideRunnable, CENTER_BOX_VISIBILITY_DELAY);
    }

    private void setViewVisibility(int id, boolean visibile) {
        int visibility;
        if (visibile) {
            visibility = VISIBLE;
        } else {
            visibility = INVISIBLE;
        }
        getView(id).setVisibility(visibility);
    }

    private View getView(int id) {
        return mVideoViewParent.findViewById(id);
    }

    private ImageView getImageView(int id) {
        return (ImageView) mVideoViewParent.findViewById(id);
    }

    private TextView getTextView(int id) {
        return (TextView) mVideoViewParent.findViewById(id);
    }


    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            setViewVisibility(R.id.app_video_center_box, false);
            setViewVisibility(R.id.app_video_volume_box, false);
            setViewVisibility(R.id.app_video_brightness_box, false);
            setViewVisibility(R.id.app_video_fastForward_box, false);
            mGestureControllsLayoutContainer.setVisibility(GONE);
            Log.d(TAG, "run: mGestureControllsLayoutContainer");
            mediacontrolls.setVisibility(GONE);
            mNextEpisodePopupWindow.setVisibility(GONE);

        }
    };

    public void onVolumeSlide(int i) {
        if (!APIConstants.IS_PLAYER_SCREEN_LOCKED) {
            String s = String.valueOf(i);
            if (i == 0) {
                s = "off";
            }
            setViewVisibility(R.id.app_video_fastForward_box, false);
            setViewVisibility(R.id.app_video_brightness_box, false);
            setViewVisibility(R.id.app_video_volume_box, true);
            getImageView(R.id.app_video_volume_icon).setImageResource(i <= 0 ? R.drawable.volume_muted
                    : i <= 5 ? R.drawable.volume_low
                    : i <= 10 ? R.drawable.volume_medium
                    : R.drawable.volume_high);
            getTextView(R.id.app_video_volume).setText(s);
            setViewVisibility(R.id.app_video_volume, true);
            setViewVisibility(R.id.app_video_center_box, true);
        }
    }

    public void onBrightnessSlide(String s) {
        if (!APIConstants.IS_PLAYER_SCREEN_LOCKED) {
            getTextView(R.id.app_video_brightness).setText(s);
            setViewVisibility(R.id.app_video_volume_box, false);
            setViewVisibility(R.id.app_video_fastForward_box, false);
            setViewVisibility(R.id.app_video_brightness, true);
            setViewVisibility(R.id.app_video_brightness_box, true);
            setViewVisibility(R.id.app_video_center_box, true);
        }
//            $.id(R.id.app_video_brightness).text(((int) (lpa.screenBrightness * 100)) + "%");
//            $.id(R.id.app_video_brightness_box).visible();
    }

    public void onProgressSlide(int showDelta, long newPosition, long duration) {
        if (!APIConstants.IS_PLAYER_SCREEN_LOCKED) {
            if (showDelta != 0) {
                setViewVisibility(R.id.app_video_volume_box, false);
                setViewVisibility(R.id.app_video_brightness_box, false);
                setViewVisibility(R.id.app_video_fastForward_box, true);
                setViewVisibility(R.id.app_video_center_box, true);
                String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
                getTextView(R.id.app_video_fastForward).setText(text + "s");
                getTextView(R.id.app_video_fastForward_target).setText(Util.generateTime(newPosition));
//            getTextView(com.myplex.sundirect.R.id.app_video_fastForward_all).setText(Util.generateTime(duration));
            }
        }
    }

    public void onEndGesture() {
        hideAllControlls();
    }

    private void hideSystemUI() {

        /*new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {*/
//                if(mPlayer.isFullScreen()){
        ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//                }
           /* }
        });*/
    }


    private void constructStackTrace(String errorMessage) {
        Throwable st = new Throwable(errorMessage);
        st.printStackTrace();
        StringWriter sw = new StringWriter();
        st.printStackTrace(new PrintWriter(sw));
        stackTrace = sw.toString();
        SDKLogger.debug("stackTrace- " + stackTrace);
        stackTrace = stackTrace == null ? "NA" : stackTrace;
    }

    private void initGoogleChromeCast() {
        // initialize chrome cast
        Log.d("ChromeCast", "initGoogleChromeCast");
        if (mContext == null || ((Activity) mContext).isFinishing()) {
            Log.d("ChromeCast", "initGoogleChromeCast invalid state");
            return;
        }
        try {
            mCastManager = CastContext.getSharedInstance(mContext).getSessionManager();
        } catch (Exception e) {
            e.printStackTrace();
            ((MainActivity) mContext).checkPlayServices();
            return;
        }
        if (mCastConsumer == null) {
            initCastConsumer();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndEnableChromeCast();
            }
        }, 5000);
    }


    private void initCastConsumer() {
        if (mCastManager == null) {
            try {
                mCastManager = CastContext.getSharedInstance(mContext).getSessionManager();
            } catch (Exception e) {
                e.printStackTrace();
                ((MainActivity) mContext).checkPlayServices();
                return;
            }
        }
        mCastManager.removeSessionManagerListener(mCastConsumer, CastSession.class);
        mCastConsumer = new SessionManagerListener<CastSession>() {
            @Override
            public void onSessionStarting(CastSession castSession) {
                Log.e("ChromeCast", "onSessionStarting");
            }

            @Override
            public void onSessionStarted(CastSession castSession, String s) {
                Log.e("ChromeCast", "onSessionStarted");
                /*if (isMediaPlaying()) {
                    onPause();
                }*/
                if (mPlayerStatusListener != null && !mPlayerStatusListener.isFragmentVisible()) {
                    LoggerD.debugLog("fragment is not visible exit player");
                    closePlayer();
                    return;
                }
                isToPlayOnLocalDevice = false;
//                checkUrlAndInitializePlayBack(mData.videos);
//                playVideo();
                if (mHandler == null) {
                    mHandler = new Handler(Looper.getMainLooper());
                }
                if (!isChromeCastClickEventDone) {
                    isChromeCastClickEventDone = true;
                    CleverTap.eventClicked(CleverTap.PAGE_DETAILS, CleverTap.ACTION_CHROMECAST);
                }

//                 (chromeCastDialogLock) {
                if (mContentVideoUri != null && !isCastPopupShowing
                        && !isDownloadedContentPlayback) {
                    isCastPopupShowing = true;
                    playContentOnCastTV(mContentVideoUri);
                }
//                }

            }

            @Override
            public void onSessionStartFailed(CastSession castSession, int i) {
                Log.e("ChromeCast", "onSessionStartFailed");
            }

            @Override
            public void onSessionEnding(CastSession castSession) {
                Log.e("ChromeCast", "onSessionEnding");

            }

            @Override
            public void onSessionEnded(CastSession castSession, int i) {
                Log.e("ChromeCast", "onSessionEnded");
                isChromeCastClickEventDone = false;

            }

            @Override
            public void onSessionResuming(CastSession castSession, String s) {
                Log.e("ChromeCast", "onSessionResuming");

            }

            @Override
            public void onSessionResumed(CastSession castSession, boolean b) {
                Log.e("ChromeCast", "onSessionResumed");


            }

            @Override
            public void onSessionResumeFailed(CastSession castSession, int i) {
                Log.e("ChromeCast", "onSessionResumeFailed");

            }

            @Override
            public void onSessionSuspended(CastSession castSession, int i) {
                Log.e("ChromeCast", "onSessionSuspended");

            }
        };
        mCastManager.addSessionManagerListener(mCastConsumer,
                CastSession.class);


    }

    protected void checkAndEnableChromeCast() {
        if (isDownloadedContentPlayback) {
            mChromeCastToolbar.setVisibility(View.GONE);
            if (mChromeCastToolbar.getMenu() != null
                    && mChromeCastToolbar.getMenu().findItem(R.id.media_route_menu_item) != null)
                mChromeCastToolbar.getMenu().findItem(R.id.media_route_menu_item).setVisible(true);
            return;
        }
        mChromeCastToolbar.setVisibility(View.GONE);
        try {
            mCastManager = CastContext.getSharedInstance(mContext).getSessionManager();
        } catch (Exception e) {
            e.printStackTrace();
            ((MainActivity) mContext).checkPlayServices();
            return;
        }
        if (videoPlayerType == VIDEO_PLAYER_TYPE.HUNGAMA_PLAYER_VIEW) {
            mChromeCastToolbar.setVisibility(INVISIBLE);
            mChromeCastToolbar.inflateMenu(R.menu.cast_menu);
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                    mChromeCastToolbar.getMenu(),
                    R.id.media_route_menu_item);
        } else if (videoPlayerType == VIDEO_PLAYER_TYPE.HOOQ_PLAYERVIEW && PrefUtils.getInstance().gePrefEnableHOOQChromeCast()) {
            mChromeCastToolbar.setVisibility(INVISIBLE);
            mChromeCastToolbar.inflateMenu(R.menu.cast_menu);
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                    mChromeCastToolbar.getMenu(),
                    R.id.media_route_menu_item);
        } else {
            if (ApplicationController.ENABLE_CHROME_CAST && isCastSupportedContent()) {
                mChromeCastToolbar.setVisibility(GONE);
                mChromeCastToolbar.inflateMenu(R.menu.cast_menu);
                CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                        mChromeCastToolbar.getMenu(),
                        R.id.media_route_menu_item);
            }
        }
        mMenu = mChromeCastToolbar.getMenu();
        mMediaRouteMenuItem = mMenu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mMediaRouteMenuItem);
        if (mediaRouteActionProvider != null)
            mediaRouteActionProvider.setAlwaysVisible(true);
    }

    public boolean isConnected() {
        try {
            CastSession castSession = CastContext.getSharedInstance(mContext)
                    .getSessionManager()
                    .getCurrentCastSession();

            return (castSession != null && castSession.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
            ((MainActivity) mContext).checkPlayServices();
            return false;

        }
    }

    protected synchronized void playContentOnCastTV(final String contentUrl, String licenseUri) {
        if (mCastManager != null && !isConnected() || isDownloadedContentPlayback) {
            LoggerD.debugLog("sundirectCastManager: castmanager is not connected or it is a downloaded content");
            isCastPopupShowing = false;
            return;
        }
        LoggerD.debugLog("contentUri- " + contentUrl + " licenseUri- " + licenseUri);

        if (mChromeCastDialog != null && mChromeCastDialog.isShowing()) {
            isCastPopupShowing = false;
            return;
        }
        /*if (TextUtils.isEmpty(licenseUri)) {
            // Sending side
            byte[] data = new byte[0];
            try {
                data = licenseUri.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
            licenseUri = "http://169.38.74.50/licenseproxy/v2/proxy?url="+ base64;
        }*/
        if (isMediaPlaying()) {
            onPause();
        }
        try {
            jsonObj = new JSONObject();
            jsonObj.put(APIConstants.PARAM_CHROME_CAST_DESCRIPTION, "subtitle");
            if (mData != null) {
                jsonObj.put(APIConstants.PARAM_CHROME_CAST_DRM_LICENSE_URL, licenseUri);
            }
            String partnerType = APIConstants.VALUE_CHROME_CAST_APALYA;
            if (mData != null
                    && mData.publishingHouse != null
                    && !TextUtils.isEmpty(mData.publishingHouse.publishingHouseName)) {
                partnerType = mData.publishingHouse.publishingHouseName;
            }
            jsonObj.put(APIConstants.PARAM_CHROME_CAST_PARTNER_TYPE, partnerType.toLowerCase());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to add description to the json object", e);
        }

        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mData.generalInfo.description);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mData.generalInfo.title);
        String imageLink = getImageLink();
        movieMetadata.addImage(new WebImage(Uri.parse(imageLink)));
        String url2 = contentUrl.toString();
//        url2 = "http://85mum-content.hungama.com/1218/4/FF-2016-00000908/stream.mpd";
//        url2 = "http://85-content.hungama.com/1218/4/FF-2016-00000300/stream.mpd";
        List<MediaTrack> mediaTracks = new ArrayList<>();
        if (mData != null && mData.subtitles != null && mData.subtitles.values != null && mData.subtitles.values.size() > 0) {
            for (int i = 0; i < mData.subtitles.values.size(); i++) {
                MediaTrack subtitles = new MediaTrack.Builder(1, MediaTrack.TYPE_TEXT)
                        .setName(mData.subtitles.values.get(i).language)
                        .setSubtype(MediaTrack.SUBTYPE_CAPTIONS)
                        .setContentId(mData.subtitles.values.get(i).link_sub)
                        // language is required for subtitle type but optional otherwise
                        .setLanguage("en‐US")
                        .build();
                mediaTracks.add(subtitles);
            }
        }
        if (mediaTracks.size() > 0) {
            mMediaInfo = new MediaInfo.Builder(url2)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(PARAM_CHROME_CAST_CONTENT_TYPE)
                    .setMetadata(movieMetadata)
                    .setStreamDuration(1000)
                    .setMediaTracks(mediaTracks)
                    .setCustomData(jsonObj)
                    .build();
        } else {
            mMediaInfo = new MediaInfo.Builder(url2)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(PARAM_CHROME_CAST_CONTENT_TYPE)
                    .setMetadata(movieMetadata)
                    .setStreamDuration(1000)
                    .setCustomData(jsonObj)
                    .build();
        }
        Log.e(TAG, "onApplicationLaunched() is reached");
        /*if (true) {
            isCastPopupShowing = false;
            playVideo(mMediaInfo, jsonObj);
            Analytics.gaPlayedOnCastTV(Analytics.ACTION_TYPES.play.name(), mData);
            return;
        }*/
        if (mChromeCastDialog == null)
            mChromeCastDialog = new DialogCastOrPlayLocally(mContext);
        mChromeCastDialog.showDialog(new DialogCastOrPlayLocally.OnDailogClickListener() {
            @Override
            public void onChoosePlayOnTV() {
                isCastPopupShowing = false;

                playVideo(mMediaInfo, jsonObj);
                Analytics.gaPlayedOnCastTV(Analytics.ACTION_TYPES.play.name(), mData);
            }

            @Override
            public void dismissProgressBar() {
                hidePlayerLoading();
            }

            @Override
            public void onChoosePlayLocally() {
                isCastPopupShowing = false;
                Log.e(TAG, "video url " + contentUrl);
                isToPlayOnLocalDevice = true;
                if (mVideoViewPlayer != null
                        && mVideoViewPlayer.wasPlayingWhenPaused()) {
                    onResume();
                    return;
                }
                initializeHooqPlayback();
            }
        });
    }

    protected boolean isCastSupportedContent() {
      /*  if (mData == null || mData.content == null || !mData.content.isChromeCastEnabled) {
            Log.e("ChromeCast", "Content enable false");
            return false;
        }*/
        Log.e("ChromeCast", "Content enable true");
        return true;
    }


    /**
     * Initialize the YouTubeSupportFrament attached as top fragment to the DraggablePanel widget and
     * reproduce the YouTube video represented with a YouTube url.
     */
    private void initializeYoutubeFragment(final String youtubeId) {

        String developerKey = mContext.getString(R.string.config_google_developerkey);

        if (TextUtils.isEmpty(developerKey)) {
            return;
        }

        youtubeFragment = new YouTubePlayerSupportFragmentX();
        youtubeFragment.initialize(developerKey, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                YouTubePlayer player, boolean wasRestored) {
                if (!wasRestored) {
                    youtubePlayer = player;
                    youtubePlayer.loadVideo(youtubeId);
                    youtubePlayer.setShowFullscreenButton(true);
                    if (Util.isScreenAutoRotationOnDevice(mContext)) {
                        youtubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                                | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                                | YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE
                                | YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                    } else {
                        youtubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                                | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                                | YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                    }
                    youtubePlayer.setPlaybackEventListener(mYoutubePlayerListener);
                    youtubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                        @Override
                        public void onFullscreen(boolean b) {
                            if (!b) {
                                if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
//                                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                    resumePreviousOrientaionTimer();
                                }
                            } else {
                                if (youtubeFragment.getView() != null) {
                                    youtubeFragment.getView().invalidate();
                                    youtubeFragment.getView().postInvalidate();
                                }
//                                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                                resumePreviousOrientaionTimer();
                            }
                        }
                    });
                    youtubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

                }
                LoggerD.debugLog("initializeYoutubeFragment: onInitializationSuccess");
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                YouTubeInitializationResult error) {
                LoggerD.debugLog("initializeYoutubeFragment: onInitializationFailure");
            }
        });
    }

    private void attachAndLoadYoutubeVideoView(String youtubeId) {
        if (mContext == null) {
            return;
        }
//        FrameLayout frameLayout = new FrameLayout(mContext);
//        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        initializeYoutubeFragment(youtubeId);

        try {
            AppCompatActivity activity = (AppCompatActivity) mContext;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (activity != null) {
                activity.findViewById(R.id.cardmediasubitemvideo_youtube_video_view).setVisibility(VISIBLE);
            }
            transaction.replace(R.id.cardmediasubitemvideo_youtube_video_view, youtubeFragment);
            transaction.commitAllowingStateLoss();
            if (DeviceUtils.isTablet(mContext)
                    && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mPlayerStatusListener.startInLandscape();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void detachYoutubePlayerFragment() {
        if (youtubeFragment == null || youtubePlayer == null) {
            return;
        }
        try {
            AppCompatActivity activity = (AppCompatActivity) mContext;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            activity.findViewById(R.id.cardmediasubitemvideo_youtube_video_view).setVisibility(GONE);
            transaction.remove(youtubeFragment);
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * A runnable called periodically to keep track of the content's progress.
     */
    private Runnable onEveryTimeInterval = new Runnable() {
        @Override
        public void run() {
            //Time interval in milliseconds to check playback progress.
            int timeInterval = 1000;
            contentProgressHandler.postDelayed(onEveryTimeInterval, timeInterval);
            if (mVideoViewPlayer != null && mVideoViewPlayer.isPlaying()) {
                if (mVideoViewPlayer.getCurrentPosition() != 0) {
                    float currentPosition = mVideoViewPlayer.getCurrentPosition();
                    float totalTime = mVideoViewPlayer.getCachedDuration();
                    float percentage = currentPosition / totalTime;
                    if ((int) (percentage * 100) > popUpPercentage) {
                        if (!mIsAdDisplayed)
                            showNextEpisodeWindow();
                        contentProgressHandler.postDelayed(forWindowTimeUpdate, timeInterval);
                    }
                    if (!mIsAdDisplayed) {
                        if ((int) currentPosition > 0 && (int) currentPosition > skipIntroStartPosition
                                && (int) currentPosition < skipIntroEndPosition) {
                            skipIntroBtn.setVisibility(VISIBLE);
                            skipEndCreditBtn.setVisibility(GONE);
                        } else if ((int) currentPosition > 0 && (int) currentPosition > skipIntroEndPosition
                                && (int) currentPosition < skipEndStartPosition) {
                            skipIntroBtn.setVisibility(View.GONE);
                            skipEndCreditBtn.setVisibility(GONE);
                        } else if ((int) currentPosition > 0 && (int) currentPosition > skipEndEndPosition) {
                            skipIntroBtn.setVisibility(View.GONE);
                            skipEndCreditBtn.setVisibility(GONE);
                        } else if ((int) currentPosition > 0 && (int) currentPosition > skipEndStartPosition
                                && (int) currentPosition < skipEndEndPosition) {
                            skipIntroBtn.setVisibility(GONE);
                            skipEndCreditBtn.setVisibility(VISIBLE);
                        }
                    } else {
                        skipIntroBtn.setVisibility(View.GONE);
                        skipEndCreditBtn.setVisibility(GONE);
                    }
                }
            }
        }
    };
    private Runnable forWindowTimeUpdate = new Runnable() {
        @Override
        public void run() {
            if (mVideoViewPlayer == null) return;
            int timeInterval = 1000;
            if (mVideoViewPlayer.isPlaying()) {
                float currentPosition = mVideoViewPlayer.getCurrentPosition();
                float totalTime = mVideoViewPlayer.getCachedDuration();
                int remainingTimeInSec = (int) (totalTime - currentPosition) / 1000;
                String remainingTimeText = "Next episode in " + remainingTimeInSec + " seconds";
                if (remainingTimeInSec > 60) {
                    int seconds = remainingTimeInSec % 60;
                    int minutes = (remainingTimeInSec / 60) % 60;
                    int hours = remainingTimeInSec / 3600;
                    String duration = hours > 0 ? String.format("%02d hours %02d mints %02d seconds", hours, minutes, seconds) : String.format("%02d mints %02d seconds", minutes, seconds);
                    remainingTimeText = "Next episode in " + duration;
                    float percentage = currentPosition / totalTime;
                    if ((int) (percentage * 100) > popUpPercentage) {
                        contentProgressHandler.postDelayed(forWindowTimeUpdate, timeInterval);
                    } else {
                        mNextEpisodePopupWindow.setVisibility(GONE);
                        contentProgressHandler.postDelayed(onEveryTimeInterval, timeInterval);
                    }
                }
                mNextEpisodeTimer.setText(remainingTimeText);
            }
        }
    };

    private void showNextEpisodeWindow() {
        if (!isFullScreen || !isNextEpisodePopupEnabled) return;
        final CardData cardData = getNextEpisodeData();
        if (cardData == null) {
            return;
        }
        PicassoUtil.with(mContext).load(Util.getImageLink(cardData), mNextEpisodeImage);
        mNextEpisodePlayIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                /*mData = cardData;
                updateCardPreviewImage(mData);
                LoggerD.debugLog("HooqLoopFullScreen isFullScreen- " + isFullScreen());
                if (mPlayerStatusListener != null && isHooqContent(mData)) {
                    mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.disable);
                    if (!isFullScreen()) {
                        *//*(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)*//*
                        mPlayerStatusListener.changeMiniplayerState(FragmentCardDetailsPlayer.DraggableState.maximized);
                        LoggerD.debugLog("HooqLoopFullScreen close and maximize player");
                    }
                }*/
                playNextItem();
//                isPlayBackStartedAlready = true;
                mNextEpisodePopupWindow.setVisibility(GONE);
//                removeHandlerCallbacksIfAny();
            }
        });
        mNextEpisodePopupWindow.setVisibility(VISIBLE);
        mSeasonUI.setVisibility(INVISIBLE);
        IS_SesonUIVisible = false;
        mbutton_SeasonUI.setVisibility(INVISIBLE);
    }

    public void loadInstreamVideo() {
        SDKLogger.debug("loading vmax Instream Video Ad");
        /*if (vmaxAdView != null) {
            vmaxAdView.onDestroy();
        }*/
        if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxInStreamAdId())) {
            SDKLogger.debug("getPrefVmaxInStreamAdId is empty starting content playback");
            startContentPlayback();
            return;
        }

    }

    private void startContentPlayback() {
        SDKLogger.debug("starting playback");
        if (mVideoViewPlayer != null
                && mVideoViewPlayer.wasPlayingWhenPaused()) {
            if (mIsContentComplete) {
                SDKLogger.debug("content playback is completed");
                if (mAdsLoader != null) {
                    mAdsLoader.contentComplete();
                    if (!adsAvailableWithContent) {
                        SDKLogger.debug("ads are not available with content start next content playback");
                        playNextItem();
                    }
                } else {
                    SDKLogger.debug("ads loader is not initialized start next content playback");
                    playNextItem();
                }
            } else {
                SDKLogger.debug("resuming content playabck");
                if (mVideoViewPlayer != null) {
                    mVideoViewPlayer.isPlayingAd(false);
                    allowMediaController(true);
                    mVideoViewPlayer.setMinized(false);
                }
                onResume();
                showMediaController();
            }
        } else {
            if (videoPlayerType == VIDEO_PLAYER_TYPE.HOOQ_PLAYERVIEW) {
                playHooqPlayback();
            } else if (videoPlayerType == VIDEO_PLAYER_TYPE.HUNGAMA_PLAYER_VIEW) {
                playHungamaContent();
            } else {
                playVideoContent(mDefaultAdTagUrl);
            }
        }
    }

    private void detachVideoView() {
        if (mAdPlayerContainer != null) {
            if (mAdPlayerContainer.indexOfChild(mPlayerContainer) != -1) {
                mAdPlayerContainer.removeView(mPlayerContainer);
            }
        }
    }

    private void attachVideoView() {
        if (mAdPlayerContainer != null) {
            if (mAdPlayerContainer.indexOfChild(mPlayerContainer) == -1) {
                mAdPlayerContainer.addView(mPlayerContainer);
            }
        }
    }


    protected AudioManager.OnAudioFocusChangeListener afChangeListenerForVmaxAds = new AudioManager
            .OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            //Log.d(TAG, "Received audio focus call back");

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
              /*  if (vmaxAdView != null) {
                    vmaxAdView.pauseInstreamAd();
                }*/
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
               /* if (vmaxAdView != null) {
                    vmaxAdView.resumeInstreamAd();
                }*/
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                MediaController2.getAudioManger(mContext).abandonAudioFocus(afChangeListenerForVmaxAds);
            }
        }
    };

    public void hideInstream() {
        try {
            SDKLogger.debug("hide instream: ");
            if (mIsAdDisplayed) {
                resumePreviousOrientaionTimer();
            }
            mIsAdDisplayed = false;
//            resumePreviousOrientaionTimer();
 /*  if (vmaxAdView != null)
            vmaxAdView.pauseInstreamAd();*/

            if (mAdPlayerContainer != null) {
                if (mAdPlayerContainer.indexOfChild(vmaxAdContainer) != -1)
                    mAdPlayerContainer.removeView(vmaxAdContainer);
            }
            vmaxAdContainer.removeAllViews();
            vmaxAdContainer.setVisibility(View.GONE);
            MediaController2.getAudioManger(mContext).abandonAudioFocus(afChangeListenerForVmaxAds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int nextPositionToShowAdInMilliSeconds = 0;
    private int nextAdPosition = 0;
    /**
     * A runnable called periodically to keep track of the content's progress.
     */
    private Runnable contentProgressTask = new Runnable() {
        @Override
        public void run() {
//            Player position based mid roll ads
            if (mVideoViewPlayer != null && mVideoViewPlayer.isPlaying()) {
                if (mVideoViewPlayer.getCurrentPosition() != 0) {
                    if (vmaxAdCuePoints == null || vmaxAdCuePoints.size() <= nextAdPosition) return;
                    nextPositionToShowAdInMilliSeconds = vmaxAdCuePoints.get(nextAdPosition);
                    SDKLogger.debug("player current postion- " + mVideoViewPlayer.getCurrentPosition() + "" +
                            " duration- " + mVideoViewPlayer.getCachedDuration() + "" +
                            " current position in seconds- " + (mVideoViewPlayer.getCurrentPosition() / 1000) + "" +
                            " nextPositionToShowAdInMilliSeconds- " + nextPositionToShowAdInMilliSeconds / 1000);
                    long currentPosition = mVideoViewPlayer.getCurrentPosition()/* / 1000 / 60*/; //mints
                    if (currentPosition / 1000 == (nextPositionToShowAdInMilliSeconds / 1000)) {
                        vmaxAdCuePoints.remove(nextAdPosition);
                        nextAdPosition++;
                        if (isVmaxAdEnabled) {
                            loadInstreamVideo();
                        }

                    }
                }
            }
        }
    };

    private Timer timer = new Timer();

    /**
     * Set Timer for 30 sec after content start to show the cached Ad
     */

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (((Activity) mContext).isFinishing()) return;
            ((Activity) mContext).runOnUiThread(contentProgressTask);

        }
    };


    private void prepareMidRollCuePoints() {
        if (mVideoViewPlayer == null || !PrefUtils.getInstance().getPrefEnableVmaxMidRollAd())
            return;
        int duration = mVideoViewPlayer.getCachedDuration();
        int i = 0;
        boolean isSingleMidRollCuePoint = true;
        isToShowVmaxPostRollAd = true;
        int vmaxAdMinVideoDuration = PrefUtils.getInstance().getPrefVmaxVideoAdMinDuration();
        if (duration < vmaxAdMinVideoDuration) {
            isToShowVmaxPostRollAd = false;
            return;
        }
        if (isSingleMidRollCuePoint) {
            vmaxAdCuePoints.put(i, duration / 2);
            return;
        }
        while (duration - nextPositionToShowAdInMilliSeconds > 0) {
            if (nextPositionToShowAdInMilliSeconds == 0) {
                nextPositionToShowAdInMilliSeconds = mVideoViewPlayer.getCurrentPosition();
            }
            nextPositionToShowAdInMilliSeconds += MIDROLL_AD_CUE_POINT_IN_MINTS;
            vmaxAdCuePoints.put(i, nextPositionToShowAdInMilliSeconds);
            i++;
            SDKLogger.debug("index- " + i + " duration- " + duration + " nextPositionToShowAdInMilliSeconds- " + nextPositionToShowAdInMilliSeconds);
        }
        SDKLogger.debug("vamxAdCuePoints- " + vmaxAdCuePoints);
        for (int k = 0; k < vmaxAdCuePoints.size(); k++) {
            SDKLogger.debug("vmaxAdCuePoints.get(position) at position- " + k + " - " + vmaxAdCuePoints.get(k));
        }
    }

    private void logErrorParams(Constant.ERROR_CATEGORY error_category,
                                String message,
                                String format, String drmType, String url, String drmEnabled,
                                String errorCodeMessage, String errorCode, String sourceDetails, String stackTrace,
                                HashMap<String, String> properties) {
        format = (format == null ? "NA" : format);
        drmType = (drmType == null ? "NA" : drmType);
        url = (url == null ? "NA" : url);
        drmEnabled = (drmEnabled == null ? "NA" : drmEnabled);
        stackTrace = stackTrace == null ? "NA" : stackTrace;
        message = (message == null ? "NA" : message);
        SDKLogger.debug("stackTrace- " + stackTrace);
        myplexEvent.identify(String.valueOf(PrefUtils.getInstance().getPrefUserId()));
        myplexEvent.logError(error_category,
                message,
                new Media(format, drmType, url, drmEnabled),
                new Error(errorCodeMessage, errorCode, sourceDetails, stackTrace), properties);

    }


    @Override
    public void OnCacheResults(List<CardData> dataList) {
        if (dataList == null) {
            if (!isLoadingMoreRequest) {
                showNoDataMessage();
            }
            return;
        }
        mIsLoadingMoreAvailable = true;
        if (mCacheManager.isLastPage()) {
            mIsLoadingMoreAvailable = false;
        }

        if (isLoadingMoreRequest) {
            isLoadingMoreRequest = false;

            if (mAdapterEpisode != null && dataList.size() > 0) {
                mAdapterEpisode.addAll(dataList, isLoadingMoreRequest);
            }
            return;
        }
        fillData(dataList);
    }


    @Override
    public void OnOnlineResults(List<CardData> dataList) {
        if (dataList == null) {
            if (!isLoadingMoreRequest) {
                showNoDataMessage();
            }
            return;
        }
        mIsLoadingMoreAvailable = true;
        if (isLoadingMoreRequest) {
            isLoadingMoreRequest = false;


            if (mAdapterEpisode != null && dataList.size() > 0) {
                mAdapterEpisode.addAll(dataList, isLoadingMoreRequest);
            }
            return;
        }
//        AdapterEpisodePlayer.EpisodeViewHolder viewHolder = (AdapterEpisodePlayer.EpisodeViewHolder) mRecyclerViewPlayer.findViewHolderForLayoutPosition(0);
//            viewHolder.mCollapseOrExpandBtn.requestLayout();
//            mRecyclerView.postInvalidate();

        fillData(dataList);
    }

    @Override
    public void OnOnlineError(Throwable error, int errorCode) {
    }

    private final View.OnClickListener mTodayEPGListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };


    public void setRequestIDAndPayLoad(APIResponse<CardResponseData> response) {
        try {
            requestID = response.body().request_id;
            currentData = new Gson().toJson(response.body());
        } catch (Exception e) {
            Log.e(TAG, "setRequestIDAndPayLoad: ", e);
        }
    }
}

