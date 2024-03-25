package com.myplex.myplex.media.exoVideo;

import static android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;
import static com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_DEFAULT;
import static com.google.android.exoplayer2.upstream.HttpDataSource.HttpDataSourceException;
import static com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException;
import static com.myplex.api.APIConstants.IS_SesonUIVisible;
import static com.myplex.api.APIConstants.TYPE_PROGRAM;
import static com.myplex.myplex.ApplicationController.sShowLiveTVPlaybackOptHint;
import static com.myplex.player_config.PlayerConfig.PlayerType.DASH_PLAYER;
import static com.myplex.player_config.PlayerConfig.PlayerType.HLS_PLAYER;
import static com.myplex.player_config.PlayerConfig.PlayerType.PROGRESSIVE_PLAYER;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.github.pedrovgs.LoggerD;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.gson.Gson;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataSubtitleItem;
import com.myplex.model.CardDataSubtitles;
import com.myplex.model.CardDownloadData;
import com.myplex.model.DownloadMediadata;
import com.myplex.model.PartnerDetailsResponse;
import com.myplex.model.PlayerStatusUpdate;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.media.MediaController2;
import com.myplex.myplex.media.PlayerGestureListener;
import com.myplex.myplex.media.PlayerListener;
import com.myplex.myplex.media.VideoViewPlayer;
import com.myplex.myplex.model.BufferConfigAndroid;
import com.myplex.myplex.model.PlayerEvent;
import com.myplex.myplex.previewSeekBar.PreviewLoader;
import com.myplex.myplex.ui.TrackChangeInterface;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.DynamicShortCutUtil;
import com.myplex.myplex.utils.LangUtil;
import com.myplex.myplex.utils.MOUTracker;
import com.myplex.myplex.utils.Util;
import com.myplex.player_config.DownloadManagerMaintainer;
import com.myplex.player_config.PlayerConfig;
import com.myplex.player_sdk.BufferConfig;
import com.myplex.player_sdk.ConstructPlayer;
import com.myplex.player_sdk.MyplexPlayer;
import com.myplex.player_sdk.MyplexPlayerInterface;
import com.myplex.player_sdk.PlayerCallbackInterface;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.yupptv.analytics.plugin.YuppAnalytics;
import com.yupptv.analytics.plugin.utils.AdPosition;
import com.yupptv.playerinterface.YuppExoAnalyticsInterface;
import com.yupptvus.fragments.player.exoplayer.EventLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Displays a video file.  The ExoPlayerView class
 * can load videos from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that
 * it can be used in any layout manager, and provides various display options
 * such as scaling and tinting.
 */
public class ExoPlayerView extends SurfaceView implements MediaController.MediaPlayerControl, AudioCapabilitiesReceiver.Listener,
        MyplexVideoViewPlayer, PreviewLoader,TrackChangeInterface {
    public static final CharSequence DVR_URI_FLAG = "dw=";
    public static final int MEDIA_ERROR_DRM_SESSION_EXCEPTION = -100;
    public static final int MEDIA_ERROR_DRM_BEHIND_LIVE_WINDOW_EXCEPTION = -101;
    private String TAG = "ExoVideoView";

    //    Player View Fields
//    settable by the client
    private String mUri;
    private Map<String, String> mHeaders;
    private int mDuration;

    private Context mContext;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private static final int STATE_BUFFERING = 9;
    private static final int STATE_RETRYING = 100;

    private int bufferTime = 0;
    private long startTimeMillisec = 0;
    private long endTimeMillisec = 0;
    private long actualTimeMilliSec = 0;


    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder
            mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private MediaController mMediaController;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;

    // Exo Player Fields
    private boolean playerNeedsSource;
    private long playerPosition;
    public int _overrideWidth = 240;
    public int _overrideHeight = 320;
    private MediaController2 mediaController2;
    private boolean mStopHandler = false;
    private int mPositionWhenPaused = -1;
    private PlayerListener mPlayerListener = null;
    private PlayerStatusUpdate mPlayerStatusListener;
    private String streamName = null;
    private long sessionId = 0;
    private String action = null;
    private String adURL;
    private MyPlexAdEventCallback myPlexAdEventCallback;

    private int bandwidth = 0;
    private boolean isSessionClosed;
    private boolean mWasPlayingWhenPaused = false;
    private boolean mControlResumed = false;
    private boolean isMinimized = false;
    private static final int SEEK_INTERVAL = 2 * 60 * 1000;
    private static final int LIVE_SEEK_DURATION = 30 * 1000;
    private boolean isLive;
    private float minBitrate;
    private float maxBitrate;
    private String resolution;
    public String presentPlayingContentLanguage;
    private boolean isLocked;
    private PlayerGestureListener mGestureDetector;
    private boolean isFullScreen;
    private View mPlayerHeaderView;
    private boolean isToolbarShown;
    private boolean isPaused;
    private List<TrackData> mBitrateTrackList;
    private boolean isPlayingAd;
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    private View mParentLayout;
    private MyplexPlayer player;
    private FrameLayout adFrameLayout;
    public EventLogger videoAnalyticsEventLogger;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private DebugTextViewHelper debugViewHelper;
    private boolean shouldAutoPlay;
    public CardData mCardData;
    private boolean isDVR = false;
    private String drmLicenseUrl;
    private long bytesConsumed;
    private DownloadMediadata downloadMediaData;
    private long lastEventTime;
    private long currentEventTime;
    private View mPlayerControllsView;
    private View playerMediaControllsView;

    public int filesToDownload,filesAlreadyDownloaded =-1;
    private HashMap countFilesToDownload = new HashMap<String,Boolean>();

    public static final int ZOOM_IN = 111;
    public static final int ZOOM_OUT = 222;
    public static int videoScaleType = ZOOM_OUT;

    private int maxAutoBitrateToSupport = 1500000;
    private int minAutoBitrateToSupport = 0;
    private MyplexPlayerInterface playerInterface;
    private byte[] keySetId;
    private boolean isSubTitleAvailable = false;
    public long startTime, currentTime;
    private float mVideoAspectRatio;
    private boolean isUserVideoQualitySet = false;


    private boolean isPreviewInitialWorkDone = false;

    private boolean isPlayingInFullscreen = false;

    public void setPlayingInFullscreen(boolean playingInFullscreen) {
        isPlayingInFullscreen = playingInFullscreen;
    }

    //Used only to resume player when the Quality Change pop-up is shown
    //As Player is returning non-ready state in some cases
    private boolean wasQualityChangeShown = false;

    public void setGestureListener(PlayerGestureListener.GestureListener mGestureListener) {
        this.mGestureListener = mGestureListener;
    }

    @Override
    public void onChangeQuality(float minHDRate, float maxHDRate, int videoTrackRenderer) {
        changeQuality(minHDRate,maxHDRate,videoTrackRenderer);
    }

    @Override
    public void setPlayerTitleHeaderView(View view) {
        this.mPlayerHeaderView = view;
    }

    @Override
    public void isToolbarShown(boolean isToShowToolBar) {
        this.isToolbarShown = isToShowToolBar;
    }

    @Override
    public void isPlayingAd(boolean isAd) {
        this.isPlayingAd = isAd;
    }

    @Override
    public void setDownloadMediaData(DownloadMediadata downloadMediaData) {
        this.downloadMediaData = downloadMediaData;
    }

    @Override
    public void setPlayerGestureControllsView(View playerControllsView) {
        this.mPlayerControllsView = playerControllsView;
    }

    @Override
    public void setPlayerMediaControllsView(View playerMediaControllsView) {
        this.playerMediaControllsView = playerMediaControllsView;
    }

    @Override
    public CardData getCardData() {
        return this.mCardData;
    }
    private OnAspectRatioChangeListener onAspectRatioChangeListener;
    private TrackChangeInterface trackChangePlayer;
    private PlayerGestureListener.GestureListener mGestureListener;
    public void setOnAspectRatioChangeListener(OnAspectRatioChangeListener onAspectRatioChangeListener) {
        this.onAspectRatioChangeListener = onAspectRatioChangeListener;
    }

    public interface OnAspectRatioChangeListener{
        void contentAspectRatio(float aspectRatio);
        void frameAspectRatio(float aspectRatio);
    }

    public static void setDefaultCookieManager() {
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
    }

    @Override
    public void changeQuality(float minBitRate, float maxBitRate, int rendererIndex) {
        if(player != null){
            this.minBitrate = minBitRate;
            this.maxBitrate = maxBitRate;
            wasQualityChangeShown = true;
            System.out.println("MIN BITRATE:: "+minBitRate+" MAX BITRATE:: "+maxBitRate);
            Log.e("VIDEO_QUALITY_BITRATES","minBitRate : "  + minBitRate);
            Log.e("VIDEO_QUALITY_BITRATES","maxBitRate : "  + maxBitRate);
            player.changeQuality(minBitRate,maxBitRate,rendererIndex);
        }
    }

    @Override
    public boolean isPlayingAd() {
        return false;
    }

    StreamType mStreamType = null;
    StreamProtocol mStreamProtocol = StreamProtocol.RTSP;
    private int mAutoStartCount = 0;
    private ProgressBar mProgressBar = null;
    private static final int INTERVAL_RETRY = 3 * 1000;
    private boolean mSessionClosed = false;
    private static final int START = 1;
    private static final int INTERVAL_BUFFERING_PER_UPDATE = 200;
    private MediaController.MediaPlayerControl control;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;



    /**
     * Called when the audio capabilities change.
     *
     * @param audioCapabilities
     */
    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
//        boolean backgrounded = player.getBackgrounded();
       // boolean playWhenReady = player.getPlayWhenReady();
        boolean playWhenReady = false;
        if(playerInterface != null)
            playWhenReady = playerInterface.getPlayWhenReady();
        releasePlayer(false);
        preparePlayer(playWhenReady);
//        player.setBackgrounded(backgrounded);
    }

    public ExoPlayerView(Context context) {
        super(context);
        this.mContext = context;
        initVideoView();
    }

    public ExoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mStopHandler = false;
        initVideoView();
        initilizeMediaController();
        control = this;
        trackChangePlayer=this;
        setDefaultCookieManager();
    }


    private Handler mBufferProgressHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {

            if (mStopHandler) {
                return;
            }
            if (mSessionClosed) {
                return;
            }
            switch (msg.what) {
                case START:
                    int perBuffer = getBufferPercentage();
                    onBufferingUpdate(null, perBuffer);
                    msg = obtainMessage(START);
                    if (msg != null) {
                        sendMessageDelayed(msg, INTERVAL_BUFFERING_PER_UPDATE);
                    }

                    break;
            }
        }
    };

    public void onBufferingUpdate(MediaPlayer arg0, int perBuffer) {

        if (mPlayerListener != null) {
//            LoggerD.debugExoVideoViewResizable("onBufferingUpdate(arg0, bufferPercentage): >  mPlayerListener.onBufferingUpdate(mp, bufferPercentage)");
            mPlayerListener.onBufferingUpdate(arg0, perBuffer);
        }

        if (mProgressBar != null && mProgressBar.getVisibility() != View.VISIBLE) {
            return;
        }
        switch (mStreamProtocol) {
            case RTSP:
                boolean dismissdialog = false;
                if (perBuffer > 99) {
                    dismissdialog = true;
                }
                if (isPlaying() && getCurrentPosition() > (mPositionWhenPaused + 500)) {
                    dismissdialog = true;
                }
                if (dismissdialog) {
                    showProgressBar(false);
                    mCurrentState = STATE_PREPARED;
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void setAdContainer(FrameLayout frameLayout) {
        this.adFrameLayout = frameLayout;
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;

    }

    public void setVideoPath(String path) {
        setVideoURI(path);
        mUri = path;
//        Log.d("MyUrl",path);
    }

    public void setVideoURI(String uri) {
        setVideoURI(uri, null);
    }

    /**
     * @hide
     */
    public void setVideoURI(String uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
//        Log.d("MyUrl1",uri);
    }

    public void showProgressBar(boolean visibility) {

        if (mProgressBar == null) {
            return;
        }

        if (visibility) {
            mProgressBar.setVisibility(View.VISIBLE);

            return;
        }

        mProgressBar.setVisibility(View.GONE);

    }

    public void resizeVideo(int width, int height) {
        _overrideHeight = height;
        _overrideWidth = width;
        // not sure whether it is useful or not but safe to do so
//        getHolder().setFixedSize(width, height);
        getLayoutParams().height = height;
        getLayoutParams().width = width;
        getHolder().setSizeFromLayout();
        //getHolder().setSizeFromLayout();
        requestLayout();
        invalidate(); // very important, so that onMeasure will be triggered

    }

    @Override
    public void setParams(RelativeLayout.LayoutParams params) {
//        setLayoutParams(params);
        this.setLayoutParams(params);
        requestLayout();
        invalidate(); // very important, so that onMeasure will be triggered
    }


    @Override
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int surfaceWidth = 1;
        int surfaceHeight = 1;
        surfaceWidth = resolveAdjustedSize(widthMeasureSpec, widthMeasureSpec);
        surfaceHeight = resolveAdjustedSize(heightMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(surfaceWidth, surfaceHeight);
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                /* Parent says we can be as big as we want. Just don't be larger
                 * than max size imposed on ourselves.
                 */
                result = desiredSize;
                break;

            case MeasureSpec.AT_MOST:
                /* Parent says we can be as big as we want, up to specSize.
                 * Don't be larger than specSize, and don't be larger than
                 * the max size imposed on ourselves.
                 */
                result = Math.min(desiredSize, specSize);
                break;

            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }


    public void setMyPlexAdEventCallback(MyPlexAdEventCallback myPlexAdEventCallback) {
        this.myPlexAdEventCallback = myPlexAdEventCallback;
    }

    private CustomTrackSelecter mCustomTrackSelecter = null;

    public void processRemainingFunctionality() {

        initilizeMediaController();
        if (mediaController2 != null && mCardData != null) {
            mediaController2.setPlayerListener(mPlayerListener);
            /*mediaController2.setContentType(mCardData.generalInfo.type);
            mediaController2.setCardData(mCardData);*/
            mediaController2.setDidUserPause(false);
        }
        int mAutoStartCount = 0;
        if (mUri == null) {
            return;
        }
       /* try {
            stopPlayback();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        //openVideo();
        mBufferProgressHandler.sendEmptyMessage(START);

        requestFocus();
    }

    private int[] getSDorHDBitrates(String playableUrl) {
        String bitRateCap = PrefUtils.getInstance().getString(APIConstants.ANDROID_BITRATE_CAP);
        if (bitRateCap != null && !bitRateCap.isEmpty()) {
            /*
                Adding a try-catch block so that any change in the string structure does not cause a crash
                the player will fallback to the default settings
             */

            try {
                String[] mainBitrateString = bitRateCap.split("&");
                String[] sdBitratesString = mainBitrateString[0].split("=");
                String[] hdBitratesString = mainBitrateString[1].split("=");

                if (playableUrl.contains("_hd")) {
                    return getBitratesAsInt(hdBitratesString);
                } else {
                    return getBitratesAsInt(sdBitratesString);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private int[] getBitratesAsInt(String[] bitratesString) {
        String[] bitRatestoConvert = bitratesString[1].split("-");
        int[] bitrates = new int[2];
        //LogUtils.error(TAG, bitRatestoConvert[0] + bitRatestoConvert[1]);
        bitrates[0] = Integer.parseInt(bitRatestoConvert[0]);
        bitrates[1] = Integer.parseInt(bitRatestoConvert[1]);
        return bitrates;
    }


    private void preparePlayer(boolean playWhenReady) {

        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        processRemainingFunctionality();

        mCustomTrackSelecter = new CustomTrackSelecter(mContext, mediaController2, this);

        if(mCardData != null) {
            mCustomTrackSelecter.setCardData(mCardData);
            if (mCardData.generalInfo != null)
                mCustomTrackSelecter.setContentType(mCardData.generalInfo.type);
        }

//        int[] bitrates = getSDorHDBitrates(mUri);
        float[] bitrates = mCustomTrackSelecter.getUserChoiceQualityArray(mUri);
        if (bitrates != null) {
            minAutoBitrateToSupport = (int) (bitrates[0] * 1000);
            maxAutoBitrateToSupport = (int) (bitrates[1] * 1000);
            this.minBitrate = (float)minAutoBitrateToSupport;
            this.maxBitrate = (float)maxAutoBitrateToSupport;
        }
        if (mediaController2 != null) {
            mediaController2.setMediaPlayer(control);
        }

        MOUTracker.firstFrameRendered = false;
        MOUTracker.prepareContentToPlay = true;


        if (player == null) {
            String deviceModel = Build.MODEL;
            String deviceMake = Build.MANUFACTURER;
            String licenseType=APIConstants.STREAMING;


            if (downloadMediaData != null) {
                licenseType = APIConstants.VIDEO_TYPE_DOWNLOAD;
            }
            CardDownloadData data = DownloadUtil.getDownloadDataFromDownloads(mCardData);

            if (mCardData.videos != null && mCardData.videos.values != null && mCardData.videos.values.size() > 0 && mUri != null &&
                    !TextUtils.isEmpty(mUri)) {
                for (int p=0;p<mCardData.videos.values.size();p++){
                    if (mUri.equalsIgnoreCase(mCardData.videos.values.get(p).link)){
                       drmLicenseUrl=mCardData.videos.values.get(p).licenseUrl;
                       if(drmLicenseUrl == null)
                          drmLicenseUrl=mCardData.videos.values.get(p).license_url;
                        break;
                    }
                }
                if (drmLicenseUrl==null){
                    drmLicenseUrl = APIConstants.getDRMLicenseUrl(mCardData._id, licenseType, data != null ? data.variantType : null);
                }
            }
            else{
                drmLicenseUrl = APIConstants.getDRMLicenseUrl(mCardData._id, licenseType, data != null ? data.variantType : null);
            }


            String userAgent = "SunDirect";
            Map<String, String> keyRequestProperties = new HashMap<>();
            keyRequestProperties.put("deviceMake", deviceMake);
            keyRequestProperties.put("deviceModel", deviceModel);
            ConstructPlayer.PlayerConfigBuilder playerConfigBuilder = new ConstructPlayer.PlayerConfigBuilder(mContext);
            playerConfigBuilder.setContentId(mCardData._id);
          //  keyRequestProperties.put("nv-authorizations", "eyJraWQiOiI2OTY1MDciLCJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ2ZXIiOiIxLjAiLCJ0eXAiOiJDb250ZW50QXV0aFoiLCJleHAiOjE2NTk4MzY4MDAsImNvbnRlbnRSaWdodHMiOlt7ImNvbnRlbnRJZCI6IkFkaXRoeWFUViIsInVzYWdlUnVsZXNQcm9maWxlSWQiOiJUZXN0In1dfQ.0ZjZh85GwEyAJZvf8lT9QevyoEojecK8UR6qknI_K5s");

            playerConfigBuilder.setDrmUrl(drmLicenseUrl);
            Log.d("MyLicence",drmLicenseUrl);
            playerConfigBuilder.setUserAgent(userAgent);
            playerConfigBuilder.setPlayUrl(mUri)
                    .setKeyRequestProperties(keyRequestProperties)
                    .setMinVideoBitrate(minAutoBitrateToSupport)
                    .setMaxVideoBitrate(maxAutoBitrateToSupport)
                    .setPlayerSurfaceHolder(mSurfaceHolder);
            Log.d("MyUrl", "PREPARE PLAYER :"+ mUri);
            BufferConfig bufferConfig = getBufferConfig();
            if (ApplicationController.shouldUseCustomLoadControl && bufferConfig != null) {
                playerConfigBuilder.setBufferConfig(bufferConfig);
                playerConfigBuilder.setShouldUseCustomLoadControl(true);
            }
            if(ApplicationController.getAdvertiserID() != null && !TextUtils.isEmpty(adURL)){
                playerConfigBuilder.setShouldEnableDFP(true);
                playerConfigBuilder.setmAdsOverlayFrameLayout(adFrameLayout);
                playerConfigBuilder.setAdTagUri(Uri.parse(adURL));
            } else {
                playerConfigBuilder.setShouldEnableDFP(false);
            }

            if (!isPlayingOnline()) {
                keySetId = DownloadManagerMaintainer.getInstance().getOfflineDrmKeySetId(mCardData._id, mContext, "", false);
                if (keySetId == null) {
                    if (data != null) {
                        String path = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + data._id + File.separator;
                        path += "metaK";
                        keySetId = DownloadManagerMaintainer.getInstance().getOfflineDrmKeySetId(mCardData._id, mContext, path, true);
                    }
                }
                //playerConfigBuilder.setOfflineKeySetId(keySetId);
                playerConfigBuilder.setShouldUseOfflineKeySetId(true);
                //playerConfigBuilder.setIsOfflineContent(true);
            }
            playerConfigBuilder.setPlayerCallbackInterface(mPlayerCallbackInterface);
            if (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    || mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
                playerConfigBuilder.setShouldUseLiveSeekWindow(true);
            }
            if (PrefUtils.getInstance().getSubtitlesEnabled()) {
            String mimeType = MimeTypes.TEXT_VTT;

            if (mCardData.subtitles != null && mCardData.subtitles.values != null && mCardData.subtitles.values.size() > 0
                    && mCardData.subtitles.values.get(0) != null && mCardData.subtitles.values.get(0).link_sub != null) {
                isSubTitleAvailable = true;


                    CardDataSubtitleItem carddatasubtitleItem = mCardData.subtitles.values.get(0);
                    if (carddatasubtitleItem.link_sub.contains(".m3u8")) {
                        mimeType = MimeTypes.TEXT_VTT;
                        if (mCardData.isHooq()) {
                            carddatasubtitleItem.link_sub = carddatasubtitleItem.link_sub.replace(".m3u8", ".vtt");
                        }
                    } else if (carddatasubtitleItem.link_sub.contains(".ttml")
                            || carddatasubtitleItem.link_sub.contains(".dfxp")) {
                        mimeType = MimeTypes.APPLICATION_TTML;
                    } else if (carddatasubtitleItem.link_sub.contains(".srt")) {
                        mimeType = MimeTypes.APPLICATION_SUBRIP;
                    } else if (carddatasubtitleItem.link_sub.contains(".vtt")) {
                        mimeType = MimeTypes.TEXT_VTT;
                    }

                    playerConfigBuilder.setSubtitleMimeType(mimeType);
                    Log.d("MimeType", mimeType);
                    playerConfigBuilder.setSubtitleUrl(mCardData.subtitles.values.get(0).link_sub);
                    playerConfigBuilder.setSubTitleLanguage(mCardData.subtitles.values.get(0).language);
                    playerConfigBuilder.setSubTitleView(subtitleView);
                    subtitleView.setVisibility(VISIBLE);
                } else {
                    isSubTitleAvailable = false;
                }
            }
            mCustomTrackSelecter.setCDNTypeAzure(false);
            mCustomTrackSelecter.isToolbarShown(isToolbarShown);
            mCustomTrackSelecter.setTrackPlayer(trackChangePlayer);

            if(mCardData.videos != null && mCardData.videos.values != null
                    && mCardData.videos.values.size() > 0 &&mUri!=null&&!TextUtils.isEmpty(mUri) && isPlayingOnline()){
                String videoURL = null;
                for (int p=0;p<mCardData.videos.values.size();p++){
                    if (mUri.equalsIgnoreCase(mCardData.videos.values.get(p).link)){
                        videoURL=mCardData.videos.values.get(p).link;
                        break;
                    }else {
                        videoURL=mUri;
                    }
                }
                //Log.d(TAG,"MyUrl prepare player video Url :"+videoURL);
                if (videoURL!=null&&!TextUtils.isEmpty(videoURL)){
                    if (videoURL.contains(".m3u8")) {
                        playerInterface = new PlayerConfig().getPlayerInstance(HLS_PLAYER);
                    } else if (videoURL.contains(".mpd")) {
                        playerInterface = new PlayerConfig().getPlayerInstance(DASH_PLAYER);
                    }else{
                        playerInterface = new PlayerConfig().getPlayerInstance(PROGRESSIVE_PLAYER);
                    }
                }
            } else if (!isPlayingOnline()) {
                if (mCardData.offlinePlayerType!=null) {
                    if(mCardData.offlinePlayerType.equalsIgnoreCase(APIConstants.STREAMDASH)){
                        playerInterface = new PlayerConfig().getPlayerInstance(DASH_PLAYER);
                    }else {
                        playerInterface = new PlayerConfig().getPlayerInstance(HLS_PLAYER);
                    }
                } else {
                    if (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                            || mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)||
                            mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)) {
                        playerInterface = new PlayerConfig().getPlayerInstance(HLS_PLAYER);
                    }else {
                        playerInterface = new PlayerConfig().getPlayerInstance(DASH_PLAYER);
                    }
                }
            }
           /* if(mCardData._id.equals("141060"))
                playerInterface = new PlayerConfig().getPlayerInstance(DASH_PLAYER);*/
            player = new MyplexPlayer();
            player.setInterface(playerInterface);
            LoggerD.debugLog("ExoPlayerVideoView: duration- " + player.getDuration());
            if (mUri != null && mUri.toLowerCase().contains(DVR_URI_FLAG)) {
                isDVR = true;
            }
            if (isLive || isPlayingAd) {
                player.preparePlayer(playerConfigBuilder.build(), true);
//                playerInterface.forward(mPositionWhenPaused);
            } else {
                if (mCardData.subtitles != null && mCardData.subtitles.values != null && mCardData.subtitles.values.size() > 0) {
                    if (mediaController2 != null)
                        mediaController2.enableSubtitles(true);

                    player.preparePlayer(playerConfigBuilder.build(), true);
                    if (!mCardData.isLive() && playerInterface != null) {
                        playerInterface.forward(mPositionWhenPaused);
                    }
                } else {
                    player.preparePlayer(playerConfigBuilder.build(), true);
                    if (!mCardData.isLive() && playerInterface != null) {
                        playerInterface.forward(mPositionWhenPaused);
                    }
                }
                setSubtitle(PrefUtils.getInstance().getSubtitle());
            }
            ExoPlayer exoPlayer=playerInterface.getExoPlayer();

            if (mContext != null) {
                try {
                    if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {

                    YuppAnalytics.getInstance(mContext).initClient(mContext,
                            "1234", false);
                    initVideoAnalytics(exoPlayer);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }
    }

    private void initVideoAnalytics(ExoPlayer exoPlayer) {
        YuppExoAnalyticsInterface.getInstance(mContext).cleanup();

        YuppExoAnalyticsInterface.getInstance(mContext).intiExoAnalyticsInterface(YuppAnalytics.getInstance(mContext).getPlayStateMachine(),exoPlayer);

        YuppAnalytics.getInstance(mContext).initInternalAnalyticsMetaData(requiredMetadata(), "1234", false);


        videoAnalyticsEventLogger=new EventLogger(null);
        if(videoAnalyticsEventLogger!=null) {
               videoAnalyticsEventLogger.setmYuppExoAnalyticsInterface(YuppExoAnalyticsInterface.getInstance(mContext));
        }

        YuppAnalytics.getInstance(mContext).createSession();
        YuppExoAnalyticsInterface.getInstance(mContext).attachPlayer(exoPlayer);


    }


    private Map<String, String> requiredMetadata() {
        final HashMap<String, String> metaData = new HashMap<String, String>();
        metaData.clear();
        String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String contentType="";
        metaData.put("clientID","daf891f4-935b-4825-aa95-ad8befff7fec");
        metaData.put("deviceID","11");
        metaData.put("deviceType", getResources().getString(R.string.osname));
        metaData.put("deviceClient",getResources().getString(R.string.osname));
        metaData.put("country",PrefUtils.getInstance().getUserCountry());
        metaData.put("productName","test-sundirect");
        metaData.put("boxID",android_id);
        metaData.put("userID",String.valueOf(PrefUtils.getInstance().getPrefUserId()));
        if(mCardData!=null) {
            if(mCardData.content!=null && mCardData.content.channelNumber!=null) {
                metaData.put("channelID", mCardData.content.channelNumber);
            }
            if(mCardData.globalServiceName!=null) {
                metaData.put("channelName", mCardData.globalServiceName);
            }
            if(mCardData.getTitle()!=null) {
                metaData.put("programName", mCardData.getTitle());
            }
            if(mCardData.globalServiceId!=null) {
                metaData.put("programID",mCardData.globalServiceId);
            }else if(mCardData._id!=null){
                metaData.put("programID",mCardData._id);
            }
            if(mCardData.content.genre.size()>0) {
                metaData.put("genre", mCardData.content.genre.get(0).name);
            }
            if(mCardData.content.language.size()>0) {
                metaData.put("language", mCardData.content.language.get(0));
            }
            if(mCardData.getType()!=null) {
                if (mCardData.getType().equalsIgnoreCase(APIConstants.TYPE_LIVE) || mCardData.getType().equalsIgnoreCase(TYPE_PROGRAM)) {
                         if (((MainActivity) mContext) != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null
                                    && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer != null
                                    && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isFromCatchup) {
                                    contentType = "catchUp";
                              } else{
                             contentType="live";
                         }
                    } else if (mCardData.getType().equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)) {
                        contentType = "Episode";
                    } else if (mCardData.getType().equalsIgnoreCase(APIConstants.TYPE_MOVIE) || mCardData.getType().equalsIgnoreCase(APIConstants.TYPE_VOD)) {
                        if (mCardData.content != null && mCardData.generalInfo.contentRights != null && mCardData.generalInfo.contentRights.size() > 0 && mCardData.generalInfo.contentRights.get(0) != null && mCardData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                            contentType = "tvod";
                        } else {
                            contentType = mCardData.getType();
                        }
                    }
                    metaData.put("contentType", contentType);
            }
            if(mCardData.startDate!=null && mCardData.endDate!=null) {
                metaData.put("epgStartTime", mCardData.startDate);
                metaData.put("epgEndTime", mCardData.endDate);
            }
            if(player!=null) {
                metaData.put("vodStreamPosition", String.valueOf(player.getCurrentPosition()));
            }
            if(mCardData.getParnterTitle(mContext)!=null) {
                metaData.put("partnerName", mCardData.getParnterTitle(mContext));
            }
            int vendorID;
            if ( mCardData.generalInfo!=null && mCardData.getType()!=null &&  (mCardData.getType().equalsIgnoreCase(APIConstants.TYPE_LIVE) || mCardData.getType().equalsIgnoreCase(TYPE_PROGRAM))) {
                vendorID = -1;
            } else {
                vendorID = 1;
            }
            metaData.put("vendorID", String.valueOf(vendorID));
        }
        if(PrefUtils.getInstance().getSubscriptionStatusString()!=null && PrefUtils.getInstance().getSubscriptionStatusString().equalsIgnoreCase(APIConstants.USER_NOT_SUBSCRIBED)){
            metaData.put("isSubscribed","not-subscribed");
        }else{
            metaData.put("isSubscribed","subscribed");
        }
        if(mCardData!=null && mCardData.videos!=null && mCardData.videos.values!=null && mCardData.videos.values.size()>0) {
            metaData.put("streamUrl", mCardData.videos.values.get(0).link);
        }
        metaData.put("adType", AdPosition.PREROLL.toString());

        return metaData;
    }

    private PlayerCallbackInterface mPlayerCallbackInterface = new PlayerCallbackInterface() {

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            if (mediaController2 != null && !isUserVideoQualitySet) {
                isUserVideoQualitySet = true;
                mediaController2.setUserChoiceQuality();
            }
            if(trackSelections != null && trackSelections.get(1) != null && trackSelections.get(1).getSelectedFormat() != null && trackSelections.get(1).getSelectedFormat().language != null) {
                presentPlayingContentLanguage = trackSelections.get(1).getSelectedFormat().language;
            }
            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                if(presentPlayingContentLanguage!=null) {
                    YuppAnalytics.getInstance(mContext).sendAudioTrackNames(LangUtil.getSubtitleTrackName(presentPlayingContentLanguage), false);
                }
                if (trackSelections != null && trackSelections.get(0)!=null && trackSelections.get(0).getSelectedFormat() != null) {
                    try {
                        YuppAnalytics.getInstance(mContext).setBitrate(trackSelections.get(0).getSelectedFormat().bitrate);
                        YuppAnalytics.getInstance(mContext).setBitrateEvent(trackSelections.get(0).getSelectedFormat().bitrate);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }



        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        }

        @Override
        public void onVideoInputFormatChanged(Format format) {
        }

        @Override
        public void onRenderedFirstFrame(@Nullable Surface surface) {
        /*    if (mediaController2 != null && mediaController2.getmCardData() != null) {
                mediaController2.setAppActionFromCarousalData(appAction);
                mediaController2.setUpBottomSheet();
            }*/
           /* if (mCardData != null && mCardData.generalInfo != null) {
                int i = 0;
                if (player != null) {
                    i = (int) (long) player.getCurrentPosition();
                }
                if (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE) ||
                        mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
                    i = 0;
                }

                if (!isPlayerPlayEventFired && isPlayingOnline()) {
                    if (Util.isNetworkAvailable(mContext)) {
                        long pausedAt = System.currentTimeMillis(); //10.10 //10.35 //stop at 10.45
                        startTime = TimeUnit.MILLISECONDS.toSeconds(pausedAt) - TimeUnit.MILLISECONDS.toSeconds(currentTime);
                        Util.updatePlayerEvents(mCardData._id, Integer.toString(i / 1000), "Play", mCardData.generalInfo.type, 0);
                    } else {
                        String updatePlayerString = "contentId:" + mCardData._id + "," + "elapsedTime:" + Integer.toString(i / 1000) + "," +
                                "action:" + "Play" + "," + "contentType:" + mCardData.generalInfo.type + "," + "mou:" + 0;
                        PrefUtils.getInstance().storePlayerEvent(updatePlayerString);
                    }
                }
            }*/
        }

        @Override
        public void onSurfaceSizeChanged(int i, int i1) {

        }

        @Override
        public void onDroppedFrames(int i, long l) {

        }

        @Override
        public void onVideoDecoderInitialized(String s, long l, long l1) {

        }

        @Override
        public void onAdEvent(Object o) {
            if(myPlexAdEventCallback != null){
                myPlexAdEventCallback.setAdEvent((AdEvent) o);
            }
        }

        /**
         * Called when the player starts or stops loading the source.
         *
         * @param isLoading Whether the source is currently being loaded.
         */
        @Override
        public void onLoadingChanged(boolean isLoading) {
            showProgressBar(false);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            LoggerD.debugIMAads("onPlayerStateChanged prepare player completed for playbackState- " + playbackState);
            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics() ){
                YuppExoAnalyticsInterface.getInstance(mContext).onPlayWhenReadyChanged(playWhenReady, 0);
                YuppExoAnalyticsInterface.getInstance(mContext).onPlaybackStateChanged(playbackState);
            }
            switch (playbackState) {
//9
                case ExoPlayer.STATE_BUFFERING:
                    mCurrentState = STATE_BUFFERING;
                    mTargetState = STATE_BUFFERING;
                    notifyPlayerEvent(PlayerState.buffering);
                    if (mPlayerListener != null) {
                        mPlayerListener.onBuffering();
                    }
                    startTimeMillisec = System.currentTimeMillis();
                    break;
//4
                case ExoPlayer.STATE_ENDED:
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
//1
                case ExoPlayer.STATE_IDLE:
                    mCurrentState = STATE_IDLE;
                    mTargetState = STATE_IDLE;
                    break;
//3
                case ExoPlayer.STATE_READY:
                    if (player != null && IS_SesonUIVisible) {
                        player.pausePlayer();
                    }
                    if(mPlayerListener != null){
                        if (playerInterface != null) {
                            mPlayerListener.onStateChanged(PlayerListener.STATE_STARTED,
                                    (int) playerInterface.getCurrentPosition());
                        } else {
                            mPlayerListener.onStateChanged(PlayerListener.STATE_STARTED,
                                    0);
                        }
                    }
                    mCurrentState = STATE_PREPARED;
                    mTargetState = STATE_PREPARED;
                    /*if (mUri != null && mUri.toLowerCase().contains(ExoVideoView.DVR_URI_FLAG)) {
                        if (!isSeekDone) {
                            long seekDuration = player.getDuration();
                            if (seekDuration > (SEEK_INTERVAL)) {
                                player.seekTo(seekDuration - LIVE_SEEK_DURATION);
                                setLive(false);
                            }
                            isSeekDone = true;

                        }
                    }*/
                    mCanPause = mCanSeekBack = mCanSeekForward = true;
                    try {
                        onPrepared();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if(startTimeMillisec > 0) {
                        endTimeMillisec = System.currentTimeMillis();
                        actualTimeMilliSec = endTimeMillisec - startTimeMillisec;
                        startTimeMillisec = 0;
                        endTimeMillisec = 0;
                        if (actualTimeMilliSec > 0) {
                            bufferTime = (int) TimeUnit.MILLISECONDS.toSeconds(actualTimeMilliSec);
                            actualTimeMilliSec = 0;
                            MOUTracker.bufferTimeinsec += bufferTime;

                            if(!MOUTracker.firstFrameRendered) {
                                MOUTracker.bufferTimeOnFirstFrameRender = bufferTime;
                                MOUTracker.firstFrameRendered = true;
                            }
                        }
                    }
                    break;
//3
                default:
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    break;
            }

//4
            if (playWhenReady && playbackState == ExoPlayer.STATE_ENDED) { // Video completed
                if (mPlayerListener != null) {
                    mPlayerListener.onStateChanged(PlayerListener.STATE_COMPLETED, 0);
                    MediaPlayer mp = new MediaPlayer();
                    mPlayerListener.onCompletion(mp);
                }
                mWasPlayingWhenPaused = true;
                mCurrentState = STATE_PLAYBACK_COMPLETED;
                mTargetState = STATE_PLAYBACK_COMPLETED;
                if (mMediaController != null) {
                    mMediaController.hide();
                }
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onPlaybackSuppressionReasonChanged(int i) {

        }

        @Override
        public void onIsPlayingChanged(boolean b) {

        }

        @Override
        public void onRepeatModeChanged(int i) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean b) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
            SDKLogger.debug("stackTrace- "+stackTrace);
            String label = null;
            if (mCurrentState == STATE_RETRYING) {
                Log.d("PlayerScreen", "ExoVideoViewPlayer STATE_RETRYING");
                return;
            }

            String errorString = null;
            if (e != null
                    && e.getCause() != null
                    && e.getCause().getMessage() != null) {
                label = e.getCause().getMessage();
            }
            if (mPlayerStatusListener != null) {
                if (TextUtils.isEmpty(label)
                        && e.getCause() != null
                        && e.getCause().getClass() != null) {
                    label = e.getCause().getClass().getSimpleName();
                }
                mPlayerStatusListener.playerStatusUpdate("error cause: " + label);
            }
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause != null) {
                    cause.printStackTrace();
                }
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    /*if (decoderInitializationException.decoderName == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
//                            MediaCodecUtil.DecoderQueryException exception = (MediaCodecUtil.DecoderQueryException)e.getCause();
                            errorString = mContext.getString(R.string.error_querying_decoders);
                            label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + errorString;
                            mPlayerStatusListener.playerStatusUpdate(errorString);

                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString = mContext.getString(R.string.error_no_secure_decoder,
                                    decoderInitializationException.mimeType);
                            label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + errorString;
                            mPlayerStatusListener.playerStatusUpdate(errorString);

                        } else {
                            errorString = mContext.getString(R.string.error_no_decoder,
                                    decoderInitializationException.mimeType);
                            label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + errorString;
                            mPlayerStatusListener.playerStatusUpdate(errorString);
                        }
                    } else {
                        errorString = mContext.getString(R.string.error_instantiating_decoder,
                                decoderInitializationException.decoderName);
                        label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + errorString;
                        mPlayerStatusListener.playerStatusUpdate(errorString);
                    }*/

                    String stackTrace = "";
                    if(e != null)
                        stackTrace = Log.getStackTraceString(e);
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("Log Exception in player :"+stackTrace);
                    }
                } else if (cause instanceof InvalidResponseCodeException) {
                    InvalidResponseCodeException exception = (InvalidResponseCodeException) cause;
                    int code = exception.responseCode;
                    label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + "InvalidResponseCodeException" + "-" + code;
                    mPlayerStatusListener.playerStatusUpdate("InvalidResponseCodeException" + "-" + code);
                } else if (cause instanceof ParserException) {
                    label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + "ParserException" + "-" + "NA";
                    mPlayerStatusListener.playerStatusUpdate("ParserException" + "-" + "NA");

                } else if (cause instanceof HttpDataSourceException) {
                    label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + "HttpDataSourceException" + "-" + "NA";
                    mPlayerStatusListener.playerStatusUpdate("HttpDataSourceException" + "-" + "NA");

                } else if (cause instanceof UnsupportedDrmException) {
                    // Special case DRM failures.
                    UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) cause;

                    int stringId = unsupportedDrmException.reason == UnsupportedDrmException.REASON_INSTANTIATION_ERROR
                            ? R.string.error_drm_not_supported
                            : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                            ? R.string.error_drm_unsupported_scheme
                            : R.string.error_drm_unknown;
                    Toast.makeText(mContext, stringId, Toast.LENGTH_LONG).show();
                    label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + "UnsupportedDrmException" + "-" + "NA";
                    mPlayerStatusListener.playerStatusUpdate("UnsupportedDrmException" + "-" + "NA");
                } else if (cause instanceof FileNotFoundException) {
                    String errorMsg = "FileNotFoundException";
                    if (!TextUtils.isEmpty(e.getMessage()))
                        errorMsg = "Error Message: " + e.getMessage();

                    label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + errorMsg + "-" + "NA";
                    mPlayerStatusListener.playerStatusUpdate(errorMsg + "-" + "NA");
                } else {
                    String errorMsg = "FileNotFoundException";
                    if (!TextUtils.isEmpty(cause.getClass().getSimpleName()))
                        errorMsg = "Error Message: " + cause.getClass().getSimpleName();
                    label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + errorMsg + "-" + "NA";
                    mPlayerStatusListener.playerStatusUpdate(errorMsg + "-" + "NA");
                }
//                Analytics.playerBitrateEvent(mContext, "sessions", action, label + "", bandwidth);
            }


            // Analytics.getInstance().playerBitrateEvent("sessions",action,label+"",bandwidth);
            chunk++;
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            hideMediaController();
            updateCurrentPosition();
            playerNeedsSource = true;
            try {
                if (e.type != ExoPlaybackException.TYPE_SOURCE) {
                    if (e.getRendererException() != null) {
                        Writer writer = new StringWriter();
                        e.getCause().printStackTrace(new PrintWriter(writer));
                        stackTrace = "Uri:: " + mUri + "\n" + writer.toString();
                        mPlayerStatusListener.playerStatusUpdate(stackTrace);
                    }
                } else {
                    if (e.getCause() != null) {
                        Writer writer = new StringWriter();
                        e.getCause().printStackTrace(new PrintWriter(writer));
                        stackTrace = "Uri:: " + mUri + "\n" + writer.toString();
                        mPlayerStatusListener.playerStatusUpdate(stackTrace);
                    }
                }

            } catch (Exception ex) {
                e.printStackTrace();

            }
            onHandleError(e, label,stackTrace);
            //Sending video analayitics when we get any player error
            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                YuppExoAnalyticsInterface.getInstance(mContext).updateError(label);
            }
            if (!DeviceUtils.isTablet(mContext)) {
                LoggerD.debugLog("MiniCardVideoPlayer resumePreviousOrientaion portrait");
            }

            /*
             * Since pingIp is creating the ANR some times, Disabling printing the connection status on player log screen
             *
             * */
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) return;
                    final String networkAvailable = ConnectivityUtil.pingIp() + "";
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mContext == null) return;
                            if (mPlayerStatusListener != null) {
                                mPlayerStatusListener.playerStatusUpdate("isNetworkAvailable: " + networkAvailable);
                            }
                        }
                    });

                }
            }).start();*/
        }

        @Override
        public void onPositionDiscontinuity(int i) {

        }

        /**
         * Called when a position discontinuity occurs without a change to the timeline. A position
         * discontinuity occurs when the current window or period index changes (as a result of playback
         * transitioning from one period in the timeline to the next), or when the playback position
         * jumps within the period currently being played (as a result of a seek being performed, or
         * when the source introduces a discontinuity internally).
         * <p>
         * When a position discontinuity occurs as a result of a change to the timeline this method is
         * <em>not</em> called. {@link #//onTimelineChanged(Timeline, Object)} is called in this case.
         */
     /*   @Override
        public void onPositionDiscontinuity() {

        }
*/
        public void onPrepared() {
            //Log.d(TAG, "ExoVideoViewPlayer onPrepared End");
            // mMediaPlayer = mp;

            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.playerStatusUpdate("Play onPrepared :: ");
            }
            mCurrentState = STATE_PREPARED;

            if (mediaController2 != null) {
                mediaController2.setMediaPlayer(control);
            }
            if (mediaController2 != null) {
                mediaController2.setEnabled(true);
                mediaController2.setPlayerHeaderView(mPlayerHeaderView);
                mediaController2.setDVR(isDVR);
                mediaController2.setLive(isLive);
                //commented due to getting false value from partner details even though the content has subtitle values,links
//                mediaController2.enableSubtitles(isSubtitlesSupported() && isSubtitlesEnabledInProperties(getCardData()));
            }
            onBufferingUpdate(getBufferPercentage());
            if (mediaController2 != null && !updateTrackList && !isPlayingAd) {
                updateTrackList = true;
                mediaController2.setCustomTrackSelecter(mCustomTrackSelecter);
                /*mediaController2.enableTrackSelector(isBitrateCappingSupported());*/
                if(!isPlayingOnline()){
                    mediaController2.enableTrackSelector(false);
                }else {
                    mediaController2.enableTrackSelector(true);
                }
                notifyPlayerEvent(PlayerState.play);
                if (isLive && sShowLiveTVPlaybackOptHint) {
                    sShowLiveTVPlaybackOptHint = false;
                    mediaController2.togglePlaybackOptimization(true);
                    PrefUtils.getInstance().setPrefShowPlaybackOptHint(false);
                    mediaController2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mediaController2.togglePlaybackOptimization(false);
                        }
                    }, 5 * 1000);
                }
            }
//            start();
           /* if (!restoreQualityTrack) {
                if (mCustomTrackSelecter != null) {
                    mCustomTrackSelecter.setUserChoiceQuality(PrefUtils.getInstance().getContentVideoQuality() != null ? PrefUtils.getInstance().getContentVideoQuality() : "Auto", false);
                    restoreQualityTrack = true;
                    if (player != null)
                        player.resumePlayer();
                }
            }*/

            if (mSeekWhenPrepared > 0) {
                seekTo(mSeekWhenPrepared);
                if (player != null)
                    player.resumePlayer();
            }
        }

        private void onBufferingUpdate(int bufferPercentage) {
            if (mPlayerListener != null) {
                MediaPlayer mp = new MediaPlayer();
//                LoggerD.debugExoVideoViewResizable("onBufferingUpdate(bufferPercentage) > mPlayerListener:onBufferingUpdate(mp, bufferPercentage)");
                mPlayerListener.onBufferingUpdate(mp, bufferPercentage);
//			return;
            }

            if (mProgressBar != null && mProgressBar.getVisibility() != View.VISIBLE) {
                return;
            }
            switch (mStreamProtocol) {
                case RTSP:
                    boolean dismissdialog = false;
                    if (bufferPercentage > 99) {
                        dismissdialog = true;
                    }
                    if (isPlaying() && getCurrentPosition() > (mPositionWhenPaused + 500)) {
                        dismissdialog = true;
                    }
                    if (dismissdialog) {
                        showProgressBar(false);
                        mCurrentState = STATE_PREPARED;

                    }

                    break;

                default:
                    break;
            }
        }

    };

    private boolean isPlayingOnline() {
        if (mCardData == null || mCardData.offline_link == null) {
            return true;
        }

        return mCardData.offline_link.isEmpty();

    }



    private void clearResumePosition() {
        mPositionWhenPaused = (int) C.TIME_UNSET;
    }

    int chunk = 1;

    /*
    * release the media player in any state
    */
    private void releasePlayer(boolean cleartargetstate) {
        if (player != null) {
//            playerPosition = player.getCurrentPosition();
            updateCurrentPosition();
            // player.release();
            player.releasePlayer(MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.STOP);
            player = null;
            mCurrentState = STATE_IDLE;

            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            if (debugViewHelper != null) {
                debugViewHelper.stop();
                debugViewHelper = null;
            }
        }
        if(adFrameLayout != null){
            adFrameLayout.removeAllViews();
        }
    }


    public void updateCurrentPosition() {
        if (player == null) {
            return;
        }
       /* //Log.d(TAG, "releasePlayer before current position mPositionWhenPaused- " +
                mPositionWhenPaused);*/
        mPositionWhenPaused = (int) player.getCurrentPosition();
       /* //Log.d(TAG, "releasePlayer after current position mPositionWhenPaused- " +
                mPositionWhenPaused);*/
        if (mCardData != null && !TextUtils.isEmpty(mCardData._id)){
            if (ApplicationController.elapsedTimeAutoplayContent.containsKey(mCardData._id)){
                long elapsedTime = Long.valueOf(mPositionWhenPaused);
                ApplicationController.elapsedTimeAutoplayContent.put(mCardData._id, elapsedTime);
            }
        }
    }


    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        Log.d("MyUrl6","openVideo URI :" +mUri);
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        mDuration = -1;
        releasePlayer(false);
        preparePlayer(false);


    }

    private void attachMediaController() {
        if (player != null && mMediaController != null) {
            mMediaController = new MediaController(mContext);
            View anchorView = getParent() instanceof View ? (View) getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    boolean isSeekDone = false;

    private boolean updateTrackList;
    private boolean restoreQualityTrack;
    private String stackTrace;

    public void handleAspectRatioOnFullScreen(boolean fullscreen) {
        if(fullscreen) {
            scalingWithPlayerDimen();
        } else {
            scalingWithLayoutDimen();
        }
    }
    private void handleAspectRatioOnDoubleTap() {
        if(videoScaleType == ZOOM_OUT) {
            scalingWithPlayerDimen();
            videoScaleType = ZOOM_IN;
        } else {
            scalingWithLayoutDimen();
            videoScaleType = ZOOM_OUT;
        }
    }

    private void scalingWithLayoutDimen() {
        if (mParentLayout != null) {
            float mVideoAspectRatio = (float) mParentLayout.getWidth() / mParentLayout.getHeight();
            if (onAspectRatioChangeListener != null) {
                onAspectRatioChangeListener.frameAspectRatio(mVideoAspectRatio);
            }
        }
    }

    private void scalingWithPlayerDimen() {
        if (playerInterface != null && playerInterface.getVideoFormat() != null) {
            mVideoAspectRatio = (float) ((playerInterface.getVideoFormat().width * 16) / 9) / (playerInterface.getVideoFormat().width);
            if (onAspectRatioChangeListener != null) {
                onAspectRatioChangeListener.contentAspectRatio(mVideoAspectRatio);
            }
        }
    }

    //    TODO Construct and Notify All Listeners
    private void notifyPlayerEvent(PlayerState playerState) {


        Log.d("Player State",playerState.toString());
        if (mCardData != null) {
            if(playerState.toString().equalsIgnoreCase("play")){
                DynamicShortCutUtil.getInstance().addContinueWatchingShortCut(mContext,mCardData._id);
            }
            else if(playerState.toString().equalsIgnoreCase("finish")){
                DynamicShortCutUtil.getInstance().deleteContinueWatchingShortCut(mContext,mCardData._id);
            }
        }


        PlayerEvent playerEvent = new PlayerEvent();

        if (mCardData != null) {
            playerEvent._id = mCardData._id;

            if (mCardData.generalInfo != null) {
                playerEvent.partnerId = mCardData.generalInfo.partnerId;
                playerEvent.title = mCardData.generalInfo.title;
                playerEvent.contentType = mCardData.generalInfo.type;
            }

            if (mCardData.publishingHouse != null
                    && mCardData.publishingHouse.publishingHouseName != null)
                playerEvent.partnerName = mCardData.publishingHouse.publishingHouseName;
        }
        playerEvent.mediaUrl = mUri;

        if (player != null) {
            Format format = playerInterface.getVideoFormat();
            if (format != null) {
                if (getBitrateCappingTracks() != null
                        && !getBitrateCappingTracks().isEmpty()
                        && getBitrateCappingTracks().size() > mCustomTrackSelecter.getSelectedTrackIndex()) {
                    TrackData trackData = getBitrateCappingTracks().get(mCustomTrackSelecter.getSelectedTrackIndex());
                    if (trackData != null) {
                        playerEvent.bitrate = trackData.bitrate * 1000000f;

                        Log.e("stream bitrates","playerEvent.bitrate"+playerEvent.bitrate);

                    }
                }
                playerEvent.resolution = format.width + "x" + format.height;
                Log.e("stream bitrates","playerEvent.resolution"+playerEvent.resolution);
                this.resolution = playerEvent.resolution;
            }
            currentEventTime = System.currentTimeMillis();
            playerEvent.secDiff = (currentEventTime -lastEventTime) / 1000;
            if (lastEventTime == 0L) {
                playerEvent.secDiff = 0L;
            }
            lastEventTime = currentEventTime;
            if (playerState == PlayerState.seek) {
                playerEvent.secDiff = getCurrentPosition() / 1000;
            }
        }

        playerEvent.action = playerState.name();

        if (playerEventsListeners == null
                || playerEventsListeners.isEmpty()) {
            return;
        }
        for (PlayerEventListenerInterface playerEventListener: playerEventsListeners){
            playerEventListener.onPlayerEvent(playerEvent);
        }
    }


    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);

            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;

            if (player != null) {
                //player.setVideoSurface(holder.getSurface());
            }
            if (chunk > 1) {
//                String label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + "start";
//                Analytics.playerBitrateEvent(mContext, "sessions", action, label + "", bandwidth);
                chunk++;
            }
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more

            mSurfaceHolder = null;
            if (mMediaController != null) mMediaController.hide();
            releasePlayer(true);
            if (player != null) {
                player.releasePlayer(MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.STOP);
            }
            if (audioCapabilitiesReceiver != null)
                audioCapabilitiesReceiver.unregister();
            if (!isSessionClosed) {
//                String label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + "stop";
//                //  Analytics.getInstance().playerBitrateEvent("sessions",action,label+"",bandwidth);
//                Analytics.playerBitrateEvent(mContext, "sessions", action, label + "", bandwidth);
                chunk++;
            }
        }
    };


    @Override
    public View getView() {
        return null;
    }

    @Override
    public boolean isPlaybackInitialized() {
        return false;
    }

    public View getMediaControllerView() {
        return mediaController2;
    }

    @Override
    public void setLive(boolean isLive) {
        this.isLive = isLive;
    }

    @Override
    public void setADTag(String adTag) {
    this.adURL = adTag;
    }

    @Override
    public void isPreviewInitialWorkDone(boolean b) {
        isPreviewInitialWorkDone=b;
    }

    @Override
    public boolean isLive() {
        return isLive;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
       /* if (mGestureDetector != null && mGestureDetector.onTouchEvent(motionEvent))
            return true;

        return false;*/
        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                LoggerD.debugExoVideoViewResizable("HooqPlaybackView:onTouchEvent>ACTION_DOWN");
                if (mCurrentState < STATE_PREPARED) {
                    return true;
                }
                if (mediaController2 != null) {
                    if (!isMinimized)
                        mediaController2.doShowHideControl();
                }
                return true;

            case MotionEvent.ACTION_MOVE:

                break;
        }
        return false;
    }

    @Override
    public void playerInFullScreen(boolean b) {
        if (mediaController2 != null) {
            mediaController2.playerInFullScreen(b);
        }

    }

    @Override
    public void hideMediaController() {
        if (mediaController2 != null) {
            mediaController2.hide();
        }
    }

    @Override
    public void setUri(Uri uri, StreamType type) {
        mStopHandler = false;
        this.mUri = uri.toString();
        setVisibility(View.VISIBLE);
        //  UiUtils.animateVisible(this);
        this.mStreamType = type;
        initilizeMediaController();
        if (mediaController2 != null) {
            mediaController2.setPlayerListener(mPlayerListener);
        }
        mAutoStartCount = 0;
        if (mUri == null) {
            return;
        }
        try {
            stopPlayback();
        } catch (Exception e) {
            e.printStackTrace();

        }
        openVideo();
        mBufferProgressHandler.sendEmptyMessage(START);
        if (mediaController2 != null) {
            mediaController2.enableSubtitles(isSubtitlesSupported() && isSubtitlesEnabledInProperties(getCardData()));
        }
        requestFocus();
    }
    private boolean isSubtitlesEnabledInProperties(CardData cardData) {
        if (mContext != null) {
            PartnerDetailsResponse partnerDetailsResponse = PropertiesHandler.getPartnerDetailsResponse(mContext);
            if(partnerDetailsResponse == null){
                return  true;
            }
            String partnerName = (cardData.publishingHouse != null && !TextUtils.isEmpty(cardData.publishingHouse.publishingHouseName)) ? cardData.publishingHouse.publishingHouseName : cardData.contentProvider;
            if (!TextUtils.isEmpty(partnerName) && partnerDetailsResponse != null && cardData != null && partnerDetailsResponse.partnerDetails != null) {
                for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
                    if (partnerDetailsResponse != null
                            && partnerDetailsResponse.partnerDetails != null
                            && partnerDetailsResponse.partnerDetails.get(i) != null
                            && !TextUtils.isEmpty(partnerDetailsResponse.partnerDetails.get(i).name)
                            && partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
                        return partnerDetailsResponse.partnerDetails.get(i).subtitlesEnabled;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setPositionWhenPaused(int pos) {
        this.mPositionWhenPaused = pos;
//        playerPosition = pos;
    }


    @Override
    public void deregisteronBufferingUpdate() {
        mStopHandler = true;

        if (mPlayerStatusListener != null) {
            mPlayerStatusListener.playerStatusUpdate("Play total duration :: " + getDuration());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            if (retriever != null) {
                try {
                    retriever.setDataSource(mUri.toString());
                } catch (IllegalArgumentException ex) {
                    Log.e(TAG, "deregisteronBufferingUpdate IllegalArgumentException");
                } catch (Exception e) {
                    Log.e(TAG, "deregisteronBufferingUpdate Exception");
                }
            }

        }
    }

    @Override
    public void showMediaController() {
        LoggerD.debugLog("showMediaController");
        if (mediaController2 != null) {
            mediaController2.show();
        }
    }

    public void initilizeMediaController() {

        if (!(getParent() instanceof RelativeLayout)) {
            return;
        }
        // As of now , only RelativeLayout as VideoView parent is supported.

        RelativeLayout parentVideoViewlayout = (RelativeLayout) getParent();

        if (mediaController2 == null) {
            mediaController2 = new MediaController2(mContext, true);
        }
        mediaController2.setContentEnabled(true);
        RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        layout_params.addRule(RelativeLayout.ALIGN_BOTTOM,
                getId());

        if (parentVideoViewlayout.indexOfChild(mediaController2) == -1) {
            parentVideoViewlayout.addView(mediaController2, layout_params);
        }
        parentVideoViewlayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        mediaController2.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        mediaController2.setVisibility(View.GONE);
        mediaController2.setPlayerHeaderView(mPlayerHeaderView);
        mGestureDetector = new PlayerGestureListener((Activity) mContext, this, mediaController2, mGestureListener);
        mediaController2.setPlayerControllsView(mPlayerControllsView);
        mediaController2.setMediaControllsView(playerMediaControllsView);
        mediaController2.isToolbarShown(isToolbarShown);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {

        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    public boolean onHandleError(ExoPlaybackException e, String errorMessage, String stackTrace) {
        if (mCurrentState == STATE_RETRYING) {
            Log.d("PlayerScreen", "ExoVideoViewPlayer STATE_RETRYING");
            return true;
        }
        mAutoStartCount++;
        if (mStreamProtocol == StreamProtocol.HTTP_PROGRESSIVEPLAY && mAutoStartCount < 2) {
            int value = 0;
            if (player != null) {
                mCurrentState = STATE_RETRYING;
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (mUri != null) {
                            try {
                                mCurrentState = STATE_PREPARING;
                                setVideoPath(mUri.toString());
                            } catch (IllegalStateException e) {
                                mAutoStartCount = 3;
                                e.printStackTrace();
                            }
                        }
//                        Log.d("MyUrl8",mUri);

                    }
                }, INTERVAL_RETRY);
            } else {
                value = 1;
                seekTo(mPositionWhenPaused);
                start();
            }
            if (mPlayerStatusListener != null) {
                mPlayerStatusListener.playerStatusUpdate("Retrying " + mAutoStartCount + " form position " + mPositionWhenPaused + " status " + value);
            }
            // UiUtils.showToast("Retrying ", mContext);
            //  Toast.makeText(mContext, "Retrying",Toast.LENGTH_SHORT).show();
            if (mPlayerListener != null) {
                mPlayerListener.onRetry();
            }
            return true;
        }

        if (mPlayerListener != null) {
            MediaPlayer mp = new MediaPlayer();
            int what = 0;
            if (e != null
                    && e.getCause() instanceof DrmSession.DrmSessionException
                    || mCardData.isMovie()) {
                what = ExoPlayerView.MEDIA_ERROR_DRM_SESSION_EXCEPTION;
            } else if (isBehindLiveWindow(e)) {
                clearResumePosition();
                what = ExoPlayerView.MEDIA_ERROR_DRM_BEHIND_LIVE_WINDOW_EXCEPTION;
            }
            boolean ret = mPlayerListener.onError(mp, what, 0, errorMessage,stackTrace);
            mPlayerListener = null;
            return ret;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (playerInterface.getPlayWhenReady()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    && isPlaying()) {
                pause();
                mMediaController.show();
            } else {
                toggleMediaControlsVisiblity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    public void start() {
        if (player != null) {
            if (isInPlaybackState() /*&& playerInterface.getPlayWhenReady()*/) {
                player.resumePlayer();
                // nextPreviouslistener.userInteractedResume();
                mCurrentState = STATE_PLAYING;
                mTargetState = STATE_PLAYING;
            } else if (playerInterface != null && player != null && mediaController2 != null && mediaController2.didUserPause()) {
                playerInterface.pausePlayer();
                //  nextPreviouslistener.userInteractedPause();
                mTargetState = STATE_PAUSED;
            }
            //Only used to resume player when Quality is changed.
            //In some cases where hls stream's are changed as per Quality needed
            //player is not retuning the state ready immediately
            else if(wasQualityChangeShown){
                if (player != null
                        && playerInterface != null
                        && !playerInterface.getPlayWhenReady()) {
                    player.resumePlayer();
                }
            }
        }
        notifyPlayerEvent(PlayerState.resume);
        if (player != null
                && playerInterface != null
                &&playerInterface.getPlayWhenReady()) {
            String label = chunk + "-" + ((System.currentTimeMillis() / 1000) - sessionId) + "-" + "play";
            chunk++;
        }
        if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
            YuppExoAnalyticsInterface.getInstance(mContext).onPlayWhenReadyChanged(playerInterface.getPlayWhenReady(), 0);
            YuppExoAnalyticsInterface.getInstance(mContext).onPlaybackStateChanged(mCurrentState);
        }
    }

    public void stopPlayback() {
        LoggerD.debugLog("stop playback player- " + player);
        if (player != null) {
            if (getCurrentPosition() >= getDuration()) {
                // notifyPlayerEvent(PlayerState.finish);
            } else {
                // notifyPlayerEvent(PlayerState.stop);
            }
            player.releasePlayer(MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.STOP);
            player = null;
            isSeekDone = false;
            restoreQualityTrack = false;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    public void pause() {
        //   notifyPlayerEvent(PlayerState.pause);
        if (player != null && playerInterface != null && isInPlaybackState() && playerInterface.getPlayWhenReady()) {
            player.pausePlayer();
            mCurrentState = STATE_PAUSED;
        }
        isPaused = true;
        mTargetState = STATE_PAUSED;
        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
            YuppExoAnalyticsInterface.getInstance(mContext).onPlayWhenReadyChanged(playerInterface.getPlayWhenReady(), 0);
            YuppExoAnalyticsInterface.getInstance(mContext).onPlaybackStateChanged(mCurrentState);
        }

       /* if (playerInterface != null && !playerInterface.getPlayWhenReady()) {

//            String label = chunk +"-"+((System.currentTimeMillis()/1000)-sessionId)+ "-"+"pause";
        //    Analytics.getInstance().playerBitrateEvent("sessions",action,label+"", bandwidth);
            chunk++;
        }*/
    }

    // cache duration as mDuration for faster access
    public int getDuration() {
        if (isInPlaybackState() && mDuration <= 0) {
            mDuration = (int) player.getDuration();
        }
        if (mDuration > 0)
            return mDuration;

        return -1;
    }

    public int getCurrentPosition() {
        if (player != null) {
            return (int) player.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void setPlayerListener(PlayerListener mPlayerListener) {
        this.mPlayerListener = mPlayerListener;
    }

    @Override
    public void seekTo(int msec) {
        //Log.d(TAG, "seekTo: msec- " + msec);
        //Long myLong = new Long(msec);
        if (player == null) return;
        /*if (player.getDuration() <= msec
                && player.getDuration() != 2 * 1000) {
            msec = msec - 2 * 2 000;
        }*/
        // boolean dispatched = DEFAULT_SEEK_DISPATCHER.dispatchSeek(player, player.getCurrentWindowIndex(), myLong);
        //SDKLogger.debug("dispatched- " + dispatched);
        if (isInPlaybackState() || (mediaController2 != null && mediaController2.didUserPause())) {
            if (player != null && msec >= 0
                    && playerInterface != null
                    && !playerInterface.isPlayingAd()) {
                // playerInterface.onSeek((int)playerInterface.getCurrentPosition());
                playerInterface.getPlayWhenReady();
                playerInterface.forward(msec);
                mSeekWhenPrepared = 0;
            }
        } else {
            // The seek wasn't dispatched. If the progress bar was dragged by the user to perform the
            // seek then it'll now be in the wrong position. Trigger a progress update to snap it back.
//            updateProgress();
            mSeekWhenPrepared = msec;
        }
/*        if (isInPlaybackState()) {
            player.seekTo(myLong);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }*/
        //notifyPlayerEvent(PlayerState.seek);

    }

    @Override
    public MediaController2 getMediaControllerInstance() {
        if (mediaController2 == null){
            mediaController2 = new MediaController2(mContext, true);
        }
        return mediaController2;
    }

    @Override
    public int getCurrentState() {
        return mCurrentState;
    }

    @Override
    public boolean isMinimized() {
        return isMinimized;
    }

    public boolean isPlaying() {
        return isInPlaybackState() && (playerInterface != null && playerInterface.getPlayWhenReady());
    }

    public int getBufferPercentage() {
        if (playerInterface == null) return 0;
        return (int) playerInterface.getBufferedPosition();
    }

    private boolean isInPlaybackState() {
        if (playerInterface == null) return false;
        int state = playerInterface.getPlayerState();
        if (mediaController2 != null)
            return state == Player.STATE_READY && !mediaController2.didUserPause();
        else
            return false;

    }
    public boolean canPause() {
        return mCanPause;
    }

    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


    @Override
    public void closeSession() {
        try {
            LoggerD.debugLog("closeSession");
            stopPlayback();
            mWasPlayingWhenPaused = false;
            setVisibility(View.INVISIBLE);
            mBufferProgressHandler.removeMessages(START);
            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                YuppExoAnalyticsInterface.getInstance(mContext).cleanup();
                YuppExoAnalyticsInterface.getInstance(mContext).releaseYuppExoAnalyticsInterface();
                YuppAnalytics.getInstance(mContext).releaseUSAnalytics();
            }

            LoggerD.debugHooqVstbLog("closeSession: mBufferProgressHandler.removeMessages(START)");
        } catch (Exception e) {
            Log.e("PlayerScreen", "ExoVideoViewPlayer stopPlayback exception");
            e.printStackTrace();
        }
    }

    @Override
    public void setPlayerStatusUpdateListener(
            PlayerStatusUpdate mPlayerStatusUpdate) {
        mPlayerStatusListener = mPlayerStatusUpdate;
        /*if (eventLogger != null) {
            eventLogger.setPlayerStatusListener(mPlayerStatusListener);
        }*/
        mPlayerStatusListener.playerStatusUpdate("Player Type :: ExoVideo ");
    }

    private Dialog mSplashScreen = null;
    private boolean mSplashScreenDismissed = false;

    /**
     * call this method on Activity onPause
     */

    public void onPause() {
        Log.d("PlayerScreen", "ExoVideoViewPlayer onPause Start");

        mControlResumed = false;
        mCurrentState = STATE_PAUSED;
        updateCurrentPosition();
        mWasPlayingWhenPaused = mPositionWhenPaused > 0;
        //Log.d(TAG, "onPause: mPositionWhenPaused- " + mPositionWhenPaused);
/*
        mWasPlayingWhenPaused = isPlaying();*/
        // mVideoView.stopPlayback();

        if (mediaController2 != null) {
            mediaController2.setVisibility(View.INVISIBLE);
        }

        if (mBufferProgressHandler != null) {
            mBufferProgressHandler.removeMessages(START);
        }
        if (mStreamType == null) {
            return;
        }
        if (!mWasPlayingWhenPaused) {
            // player is already in pause state , no need to do anything.
            notifyPlayerEvent(PlayerState.pause);
            switch (mStreamType) {

                case LIVE:

                    // to stop the video view reload the video automatically.
                    setVisibility(View.GONE);

                    break;

                case VOD:

                    setVisibility(View.GONE);

                    break;
            }

            return;
        }

        switch (mStreamType) {

            case LIVE:

                notifyPlayerEvent(PlayerState.pause);
                setVisibility(View.GONE);
                stopPlayback();

                break;

            case VOD:

                //setVisibility(View.GONE);
                pause();

                break;
        }
        Log.d("PlayerScreen", "ExovideoViewPlayer onPause end" + "  " + mPositionWhenPaused);

    }

    /**
     * call this method on Activity onResume
     */

    public void onResume() {
        Log.d("PlayerScreen", "ExovideoViewPlayer onResume Start");

        mControlResumed = true;

        if (mCurrentState == STATE_PREPARING) {
            setVisibility(View.VISIBLE);
            Log.d("PlayerScreen", "onBufferingUpdate form onResume");
            onBufferingUpdate(null, 0);
            Log.d("PlayerScreen", "ExoVideoViewPlayer onResume STATE_PREPARING End");
            return;
        }
//		mCenterPlayButton.setVisibility(View.GONE);

        if (mCurrentState == STATE_PAUSED) {
            showProgressBar(true);
        }
        if (mStreamType == StreamType.LIVE) {
            mBufferProgressHandler.sendEmptyMessage(START);
        }
        Log.d("PlayerScreen", "onBufferingUpdate form onResume1");
        onBufferingUpdate(null, 0);

        setVisibility(View.VISIBLE);
        if (mWasPlayingWhenPaused)
            mSeekWhenPrepared = mPositionWhenPaused;
        if (mStreamType == StreamType.VOD) {

            // mVideoView.setVideoURI(mUri);
            //Log.d(TAG, "onResume: mPositionWhenPaused- " + mPositionWhenPaused);
            if (mPositionWhenPaused > 0) {
                seekTo(mPositionWhenPaused);
//				mPositionWhenPaused = -1;
            }
        }

        mCurrentState = STATE_PREPARING;
        mWasPlayingWhenPaused = false;
        start();

//		if(mSplashScreenDismissed){
//		}else{
//			mCenterPlayButton.setVisibility(View.VISIBLE);
//			mCurrentState = STATE_IDLE;
//		}
        mSplashScreenDismissed = false;
        Log.d("PlayerScreen", "ExoVideoViewPlayer onResume End");
    }

    @Override
    public boolean isMediaControllerShowing() {
        if (mediaController2 != null) {
            return mediaController2.isShowing();
        }
        return false;
    }

    private VideoViewPlayer.OnLicenseExpiry onLicenseExpiryListener = null;

    @Override
    public void setOnLicenseExpiryListener(VideoViewPlayer.OnLicenseExpiry onLicenseExpiry) {
        this.onLicenseExpiryListener = onLicenseExpiryListener;
    }

    @Override
    public boolean wasPlayingWhenPaused() {
        return mWasPlayingWhenPaused;
    }

    @Override
    public void setFullScreenTooggle(int visibility) {
        if (mediaController2 != null) {
            mediaController2.setFullScreenTooggle(visibility);
        }
    }

    @Override
    public int getCachedDuration() {
        return getDuration();
    }

    @Override
    public void setMinized(boolean minimized) {
        isMinimized = minimized;

    }

    @Override
    public void setDebugTxtView(TextView textView) {
        //debugTextView = textView;
    }

    private List<PlayerEventListenerInterface> playerEventsListeners = new ArrayList<>();

    @Override
    public void addPlayerEvent(PlayerEventListenerInterface playerEventListenerInterface) {
        playerEventsListeners.add(playerEventListenerInterface);
    }

    @Override
    public void setStreamProtocol(StreamProtocol streamProtocol) {
        mStreamProtocol = streamProtocol;
    }

    @Override
    public void setStreamType(StreamType streamType) {
        this.mStreamType = streamType;
    }

    @Override
    public void allowMediaController(boolean enable) {
        if (mediaController2 != null)
            mediaController2.setAllowMediaController(enable);
    }

    @Override
    public void setFullScreen(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
        if (mediaController2 != null)
            mediaController2.setFullScreen(isFullScreen);
    }

    @Override
    public long getConsumedData() {
        return bytesConsumed;
    }

    @Override
    public void play() {

    }

    @Override
    public boolean isPlayerPaused() {
        return isPaused;
    }

    @Override
    public int getPositionWhenPaused() {
        return mPositionWhenPaused;
    }

    @Override
    public int getRendererType(int pos) {
        return 0;
    }

    @Override
    public void setCardData(CardData mCardData) {
        this.mCardData = mCardData;
    }

    @Override
    public boolean isBitrateCappingSupported() {
        if (isPlayingAd) {
            return false;
        }
        return getTrackCount(C.TRACK_TYPE_VIDEO) > 2;
    }


    @Override
    public void setSelectedTrack(TrackData trackData) {
        /*if (trackSelector == null || trackData == null) {
            LoggerD.debugLog("sSelectedPosition- setSelectedTrack:trackData- is null " + trackData);
            return;
        }
        trackSelector.setRendererDisabled(0, false);
        if (trackData.name.equalsIgnoreCase("Auto")) {
            trackSelector.clearSelectionOverrides();
            return;
        }
        trackSelector.setSelectionOverride(trackData.trackRenderGroupPosition, trackData.trackGroups, new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, trackData.trackRenderGroupPosition, trackData.position));*/
    }

    @Override
    public void setSelectedTrack(TrackData trackData, int selectedTrack) {
/*        if (trackSelector == null || trackData == null) {
            LoggerD.debugLog("sSelectedPosition- setSelectedTrack:trackData- is null " + trackData);
            return;
        }
        trackSelector.setRendererDisabled(0, false);
        if (trackData.name.equalsIgnoreCase("Auto")) {
            trackSelector.clearSelectionOverrides();
            return;
        }
        trackSelector.setSelectionOverride(trackData.trackRenderGroupPosition, trackData.trackGroups, new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, trackData.trackRenderGroupPosition, trackData.position));*/
    }

    @Override
    public int getTrackCount(int trackType) {
        if ((mBitrateTrackList == null || mBitrateTrackList.isEmpty())) {
            mBitrateTrackList = getBitrateCappingTracks();
        }
        if (mBitrateTrackList == null) {
            return 0;
        }
        return mBitrateTrackList.size();
    }

    @Override
    public List<TrackData> getBitrateCappingTracks() {
        if (mCustomTrackSelecter == null) {
            return mBitrateTrackList;
        }
        if (mBitrateTrackList == null || mBitrateTrackList.isEmpty()) {
            mBitrateTrackList = mCustomTrackSelecter.prepareTrackList();
        }
        return mBitrateTrackList;
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e == null || e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

/*
    private MediaSource buildMediaSource(CardDataSubtitleItem carddatasubtitleItem) {
        MediaSource subTitleSource = null;
        LoggerD.debugLog("mimeType- " + carddatasubtitleItem);
        if (carddatasubtitleItem == null || carddatasubtitleItem.link_sub == null) return subTitleSource;
        if (carddatasubtitleItem.link_sub.contains(".m3u8")) {
            if (mCardData != null
                    && mCardData.isHooq()) {
                carddatasubtitleItem.link_sub = carddatasubtitleItem.link_sub.replace(".m3u8", ".vtt");
                Format englishSubsFormat = Format.createTextSampleFormat(null, MimeTypes.TEXT_VTT, null, Format.NO_VALUE, 0, carddatasubtitleItem.language, null);
                subTitleSource = new SingleSampleMediaSource(Uri.parse(carddatasubtitleItem.link_sub), mediaDataSourceFactory, englishSubsFormat, C.TIME_UNSET);
            } else {
                subTitleSource = buildMediaSource(Uri.parse(carddatasubtitleItem.link_sub), null);
            }
        } else if (carddatasubtitleItem.link_sub.contains(".ttml")
                || carddatasubtitleItem.link_sub.contains(".dfxp")) {
            Format englishSubsFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_TTML, null, Format.NO_VALUE, 0, carddatasubtitleItem.language, null);
            subTitleSource = new SingleSampleMediaSource(Uri.parse(carddatasubtitleItem.link_sub), mediaDataSourceFactory, englishSubsFormat, C.TIME_UNSET);
        } else if (carddatasubtitleItem.link_sub.contains(".srt")) {
            Format englishSubsFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE, C.SELECTION_FLAG_DEFAULT, carddatasubtitleItem.language, null);
            subTitleSource = new SingleSampleMediaSource(Uri.parse(carddatasubtitleItem.link_sub), mediaDataSourceFactory, englishSubsFormat, C.TIME_UNSET);
        }
        return subTitleSource;

    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        Log.v("Video Url", mUri);
        DefaultBandwidthMeter bandwidth_meter;
        if (sessionId == 0) {
            sessionId = System.currentTimeMillis() / 1000;
        }
        bandwidth_meter = bandwidthCalc();
        */
/*else {
            bandwidth_meter = new DefaultBandwidthMeter();
        }*//*

        MOUTracker.setBandWidthMeter(bandwidth_meter);
        //String url = "http://apalya-streaming.com:1935/livefeed/COLphaniORS_500.sdp/playlist.m3u8?cid=391&userid=71&osv=4.2.2&br=vhigh&model=C2104&os=Android&op=sundirectPLAY&stamp=3663461278&sign=0f432758545ae7f40c52d31a9d26752a";
        //   String url = "http://115.112.238.26:1935/livefeed/500.sdp/playlist.m3u8?msd=917382006415&m=C2104&v=15.0&bw=2&os=AND4&op=BSNL&mcc=404&mnc=07&sid=3646557954&osv=4.2.2&q=2&pt=1&sn=E24&cn=&pi=trial&pd=trial&ns=null&st=1&stamp=3646557960&sign=7a3e5188225d786ea3cdac0d8fa86a7d";


        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(null),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                if (mPlayerStatusListener != null) {
                    mPlayerStatusListener.playerStatusUpdate("drmLicenseUrl: " + drmLicenseUrl);
                }
                DashMediaSource dashMediaSource = new DashMediaSource(uri, buildDataSourceFactory(bandwidth_meter),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
                String videoUrl = null;
                String audioUrl = null;
                if (downloadMediaData != null) {
                    audioUrl = downloadMediaData.audioUrl;
                    videoUrl = downloadMediaData.videoUrl;
                }
                LoggerD.debugLog("Dash videoUrl- " + videoUrl + " audioUrl- " + audioUrl);
                if (dashMediaSource != null)
                    dashMediaSource.setDownloadedVideoUrl(audioUrl).setDownloadedAudioUrl(videoUrl);
                return dashMediaSource;
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, buildDataSourceFactory(bandwidth_meter), mainHandler, eventLogger, PrefUtils.getInstance().getPrefPlayBackQuality());
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, buildDataSourceFactory(null), new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


    */
/**
     * Returns a new DataSource factory.
     *
     * @param bandwidth_meter
     * @return A new DataSource factory.
     *//*

    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidth_meter) {
        return ((ApplicationController) ApplicationController.getAppContext())
                .buildDataSourceFactory(bandwidth_meter);
    }

    */
/**
     * Returns a new HttpDataSource factory.
     *
     * @return A new HttpDataSource factory.
     *//*

    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return ((ApplicationController) ApplicationController.getAppContext())
                .buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
                                                                           String licenseUrl, Map<String, String> keyRequestProperties) throws com.google.android.exoplayer2.drm.UnsupportedDrmException {
        if (com.google.android.exoplayer2.util.Util.SDK_INT < 18) {
            return null;
        }

        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false), keyRequestProperties);
//        return new OfflineDrmSessionManager<>(uuid,
//                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, null);
        //       return new StreamingDrmSessionManager<>(uuid,
        //  FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
        if (ConnectivityUtil.isConnected(mContext) && !(downloadlist != null ? downloadlist.mDownloadedList.containsKey(mCardData._id) : false)) {
            return new DefaultDrmSessionManager<>(uuid,
                    FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
        } else if (!ConnectivityUtil.isConnected(mContext) && (downloadlist != null ? downloadlist.mDownloadedList.containsKey(mCardData._id) : false)) {
            return new OfflineDrmSessionManager<>(uuid,
                    FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, new OfflineDrmSessionManager.EventListener() {
                @Override
                public void onDrmKeysLoaded() {

                }

                @Override
                public void onDrmSessionManagerError(Exception e) {

                }
            }, mCardData);
        } else {
            return new OfflineDrmSessionManager<>(uuid,
                    FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler,  new OfflineDrmSessionManager.EventListener() {
                @Override
                public void onDrmKeysLoaded() {

                }

                @Override
                public void onDrmSessionManagerError(Exception e) {

                }
            }, mCardData);
        }
    }
*/

    /*@Override
    public int getRendererType(int pos) {
        return player == null ? C.TRACK_TYPE_UNKNOWN : player.getRendererType(pos);
    }*/

    @Override
    public void setSubtitle(String name) {
        //TODO: subtitle needs to be set to the player
        if(!TextUtils.isEmpty(name))
            PrefUtils.getInstance().setSubtitle(name);
        this.subtitleName=name;
        if (TextUtils.isEmpty(name) || mContext.getString(R.string.subtitle_opt_none).equalsIgnoreCase(name)) {
            hideSubtitlesView();
            return;
        }else{
            showSubtitlesView();
        }
        /*if (mCardData == null || mCardData.subtitles == null || mCardData.subtitles.values == null)
            return;

        for(CardDataSubtitleItem subtitleItem:mCardData.subtitles.values) {
            if(subtitleItem.language.equalsIgnoreCase(name)) {
               *//* MergingMediaSource mergedSource;
                MediaSource mediaSource = buildMediaSource(Uri.parse(mUri), null);
                if (name != null) {
                    mergedSource = new MergingMediaSource(mediaSource, buildMediaSource(subtitleItem));
                    if(player != null) {
                        player.setTextOutput(new ComponentListener());
                        player.prepare(mergedSource, false, false);
                    }
                } else {
                    Toast.makeText(mContext, "there is no subtitle", Toast.LENGTH_SHORT).show();*//*
                }

        }*/
    }

    @Override
    public void setAudioTrack(String language) {
        if(playerInterface != null){
            if(!containsDigit(language))
                playerInterface.changeAudioTrack(language,false);
        }
    }

    @Override
    public String getContentMinBitrate() {
        return String.valueOf(minBitrate/1000);
    }

    @Override
    public String getContentMaxBitrate() {
        return String.valueOf(maxBitrate/1000);
    }

    @Override
    public String getContentResolution() {
        return resolution;
    }

    @Override
    public String getContentLanguage() {
        return presentPlayingContentLanguage;
    }

    public boolean containsDigit(String s) {
        boolean containsDigit = false;

        if (s != null && !s.isEmpty()) {
            for (char c : s.toCharArray()) {
                if (containsDigit = Character.isDigit(c)) {
                    break;
                }
            }
        }

        return containsDigit;
    }

    @Override
    public List<String> getAudioTracks() {
        if(playerInterface != null){
            return playerInterface.getAudioTracks();
        }
        return null;
    }

    public List<String> getLanguages(){
        if(playerInterface != null){
            return playerInterface.getAudioTracks();
        }
        return null;
    }
    @Override
    public void onDestroy() {
        if(playerInterface != null){
            playerInterface.releaseAdsLoader(true);
        }
    }

    @Override
    public void setWaterMarkImageView(ImageView sunnxtContentPlayerWatermark) {

    }

    @Override
    public void setShowWaterMark(boolean showWaterMark) {

    }


    @Override
    public CardDataSubtitles getSubtitles() {
        return mCardData != null ? mCardData.subtitles : null;
    }

    @Override
    public boolean isSubtitlesSupported() {
        return mCardData != null && mCardData.subtitles != null && mCardData.subtitles.values != null && mCardData.subtitles.values.size() > 0;
    }

    @Override
    public void showSubtitlesView() {
        if (this.subtitleView != null)
            this.subtitleView.setVisibility(VISIBLE);
    }

    @Override
    public void hideSubtitlesView() {
        if (this.subtitleView != null)
            this.subtitleView.setVisibility(GONE);
    }

    @Override
    public String getSubtitleName() {
        return PrefUtils.getInstance().getSubtitle();
    }


    String subtitleName;

    SubtitleView subtitleView;

    public void setSubtitleView(SubtitleView subtitleView) {
        this.subtitleView = subtitleView;
    }

    @Override
    public void onTapToZoom() {
        handleAspectRatioOnDoubleTap();
    }
    @Override
    public void setmParentLayout(View mParentLayout) {
        this.mParentLayout = mParentLayout;
    }
    @Override
    public void orientationChange(int orientation) {
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoScaleType = ZOOM_IN;
            changeVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT);
            handleAspectRatioOnFullScreen(true);
        } else {
            changeVideoScalingMode(VIDEO_SCALING_MODE_DEFAULT);
            handleAspectRatioOnFullScreen(true);
        }
    }
    private void changeVideoScalingMode(int scalingMode) {
        if (player != null) {
            playerInterface.setVideoScalingMode(scalingMode);
        }
    }

    public void setPreviewInitialWorkDone(boolean setPreviewInitailWorkDone){
        this.isPreviewInitialWorkDone=setPreviewInitailWorkDone;
    }

    @Override
    public void loadPreview(long currentPosition, long max) {
        if(!isPreviewInitialWorkDone) {
            return;
        }
        if(player != null) {
            if (mediaController2 != null) {
                mediaController2.setShowPreviewSeekBar(true);
               mediaController2.setPreviewSeekBar(currentPosition);
            }
        }
        /*if(isPlaying()){
            playerInterface.setPlayWhenReady(true);
        }else{
            if(player != null) {
                playerInterface.setPlayWhenReady(false);
            }
        }*/
    }

    private BufferConfig getBufferConfig() {
        BufferConfigAndroid bufferConfigAndroid;
        try {
            Gson gson = new Gson();
            bufferConfigAndroid =
                    gson.fromJson(PrefUtils.getInstance().getBufferConfigAndroid(),
                            BufferConfigAndroid.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BufferConfig.BufferConfigBuilder bufferConfigBuilder = new BufferConfig.BufferConfigBuilder();
        bufferConfigBuilder.setMinBufferVOD(bufferConfigAndroid.vod.min);
        bufferConfigBuilder.setMaxBufferVOD(bufferConfigAndroid.vod.max);
        bufferConfigBuilder.setMinBufferLive(bufferConfigAndroid.live.min);
        bufferConfigBuilder.setMaxBufferLive(bufferConfigAndroid.live.max);
        return bufferConfigBuilder.build();
    }

    public void setTrackPlayer(TrackChangeInterface control) {
        this.control = control;
    }
}


