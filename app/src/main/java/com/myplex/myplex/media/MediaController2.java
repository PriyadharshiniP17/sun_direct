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

package com.myplex.myplex.media;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static com.myplex.api.APIConstants.VIDEO_QUALITY_AUTO;
import static com.myplex.api.APIConstants.VIDEO_QUALITY_HD;
import static com.myplex.api.APIConstants.VIDEO_QUALITY_LOW;
import static com.myplex.api.APIConstants.VIDEO_QUALITY_MEDIUM;
import static com.myplex.api.myplexAPISDK.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.api.request.epg.EpgCatchUpList;
import com.myplex.model.CardData;
import com.myplex.model.CardDataPlayerOptionItem;
import com.myplex.model.CardDataSubtitleItem;
import com.myplex.model.CardDataSubtitles;
import com.myplex.model.ChannelsCatchupEPGResponseData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.media.exoVideo.CustomTrackSelecter;
import com.myplex.myplex.media.exoVideo.ExoPlayerView;
import com.myplex.myplex.media.exoVideo.MyplexVideoViewPlayer;
import com.myplex.myplex.media.exoVideo.TrackData;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.previewSeekBar.PicassoTransformation;
import com.myplex.myplex.previewSeekBar.PreviewSeekBar;
import com.myplex.myplex.previewSeekBar.WebVTTData;
import com.myplex.myplex.previewSeekBar.WebVTTModule;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterCatchupCarousel;
import com.myplex.myplex.ui.adapter.AdapterPlayerOption;
import com.myplex.myplex.ui.adapter.AudioTrackAdapter;
import com.myplex.myplex.ui.fragment.epg.DateHelper;
import com.myplex.myplex.ui.fragment.epg.EPGUtil;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;
import com.myplex.myplex.ui.views.MiniCardVideoPlayer;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.LangUtil;
import com.myplex.myplex.utils.MOUTracker;
import com.myplex.myplex.utils.SubtitlesDialog;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.squareup.picasso.Picasso;
import com.yupptv.analytics.plugin.YuppAnalytics;
import com.yupptv.playerinterface.YuppExoAnalyticsInterface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MediaController2 extends LinearLayout {

    private static final String TAG = MediaController2.class.getSimpleName();

    public void setShowPreviewSeekBar(boolean showPreviewSeekBar) {
        this.showPreviewSeekBar = showPreviewSeekBar;
        if (mProgress != null) {
            mProgress.setShowPreview(showPreviewSeekBar);
        }
    }

    private boolean showPreviewSeekBar = false;
    public MediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private View mRoot;
    public PreviewSeekBar mProgress;
    public View divider_line;
    private TextView mEndTime, mCurrentTime;
    private boolean mShowing;
    private boolean mDragging;
    private static final int sDefaultTimeout = 5000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean mUseFastForward;
    private boolean mFromXml;
    private boolean mListenersSet;
    private OnClickListener mNextListener, mPrevListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    public RelativeLayout mPauseButton;
    public ImageView mPauseButtonImage;
    private ImageView mFfwdButton;
    private ImageView mRewButton;
    private ImageView mNextButton;
    private ImageView mPrevButton;
    private RelativeLayout mMuteButton;
    private ImageView go_live_button;
    private LinearLayout playerQualityLayout;
    private ImageView mMuteButtonImage;
    private RelativeLayout mFullScreenTooggle, mPlayerExitScreen, durationLL;
    private ImageView mFullScreenTooggleImage;
    private MediaPlayer mMediaPlayer = null;
    private boolean mMuteEnabled = false;
    private VideoViewPlayer mCustomVideoView = null;
    private boolean mContentEnabled = false;
    private PlayerListener mPlayerListener;
    private static AudioManager mAudioManager;
    private View mPlayerHeaderView;
    private boolean isToolbarShown;
    private View mPlayerControllsView;
    private View mPlayerMediaControllsView;
    private ImageView mPauseButtonImageCenter;
    private View mFfwdButtonContainer;
    private RelativeLayout.LayoutParams layoutParamsOriginal;
    private TextView mFfwdText;
    private View mRewindButtonContainer;
    private TextView mRewindText;
    private SubtitlesDialog subtitlesDialog;
    private boolean isDVR;
    private boolean isNonDVRLive;
    private boolean mIsLocked;
    /*  Preview Seekbar variables*/

    private ImageView imageViewSeekBar;
    private FrameLayout previewFrameLayout;
    public static RecyclerView rvAudioTracks;
    private FrameLayout mLockScreen;
    private ImageView mLockScreenImage;
    public boolean isPausedWhiilePlaying;
    public LinearLayout videoQualityLinear, audioLanguageLinear, catchUpLinear, bottomSelectionParentLL, episodeLinear;
    //    mSubtitleSettingsButtton,
    public MiniCardVideoPlayer miniCardVideoPlayer;
    public String videoQuality = "";
    SettingsContentObserver settingsContentObserver;


    private int jumpToSecs;

    private LinearLayout ll_player_option_bg;

    boolean isAudioAvailable, isSubtitleAvailable;

    LinearLayout ll_next_episode;


    public void setLive(boolean isLIve) {
        isNonDVRLive = isLIve && !isDVR;
        this.isLive = isLIve;
        SDKLogger.debug("VFPLAY: isLive- " + isLive + " isDVR- " + isDVR + " isNonDVRLive- " + isNonDVRLive);
        initializePlaybackControlls();
//        epgChannelsListApiCall();
        /*if (mSubtitleSettingsButtton != null) {
            mSubtitleSettingsButtton.setVisibility(VISIBLE);
            SDKLogger.debug("VFPLAY: isLive- " + isLive + " isDVR- " + isDVR);
            if (isLive || isDVR) {
                mSubtitleSettingsButtton.setVisibility(GONE);
            }
        }*/
    }

    private boolean isLive = false;

    public void setCustomTrackSelecter(CustomTrackSelecter mCustomTrackSelecter) {
        this.mCustomTrackSelecter = mCustomTrackSelecter;
    }

    private CustomTrackSelecter mCustomTrackSelecter = null;

    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    private boolean isFullScreen;

    public boolean isAllowed() {
        return isAllowed;
    }

    public void setAllowMediaController(boolean enable) {
        this.isAllowed = enable;
    }

    private boolean isAllowed;
    public WebVTTModule webVTTModule;

    private boolean didUserPause = false;

    public boolean didUserPause() {
        return didUserPause;
    }

    public void setDidUserPause(boolean didUserPause) {
        this.didUserPause = didUserPause;
    }

    public MediaController2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;
        setTag("MediaController2 LinearLayout");
        setAnchorView(null);

    }


    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setMediaPlayer(MediaPlayer mp) {
        mMediaPlayer = mp;
    }

    public void setCustomVideoView(VideoViewPlayer v) {
        mCustomVideoView = v;
    }

    public void setPlayerListener(PlayerListener mPlayerListener) {
        this.mPlayerListener = mPlayerListener;
    }

    public void setContentEnabled(boolean contentEnabled) {
        mContentEnabled = contentEnabled;
        setAnchorView(null);
    }

    public MediaController2(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = false;
        mContentEnabled = useFastForward;

        setAnchorView(null);
        mAudioManager = getAudioManger(mContext);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        int result = mAudioManager.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start playback.
            Log.d("MediaController", "Request for AudioManager request focus is successful");
        } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            Log.d(TAG, "Request for AudioManager request focus is failed");
        }
        //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,5,0);
// Make the status bar and navigation bar visible again.
        // Whenever the status bar and navigation bar appear, we want the playback controls to
        // appear as well.
        ((Activity) mContext).getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        // By doing a logical AND, we check if the fullscreen option is triggered (i.e. the
                        // status bar is hidden). If the result of the logical AND is 0, that means that the
                        // fullscreen flag is NOT triggered. This means that the status bar is showing. If
                        // this is the case, then we show the playback controls as well (by calling show()).
                        if (((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)) {
                            if (!isFullScreen) {
                                return;
                            }
                            /*((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);*/
                            hide();
//                            LoggerD.debugExoVideoViewResizable("setOnSystemUiVisibilityChangeListener status bar is visible SYSTEM_UI_FLAG_FULLSCREEN is deactivated");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //   hideSystemUI();
                                }
                            }, sDefaultTimeout);
                        }
//                        LoggerD.debugExoVideoViewResizable("setOnSystemUiVisibilityChangeListener " + String.valueOf((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) + " isFullScreen- " + isFullScreen()
//                                + " i- " + i);
                    }
                }

        );
    }

    public static AudioManager getAudioManger(Context mContext) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        }
        return mAudioManager;
    }

    public MediaController2(Context context) {
        super(context);
        mContext = context;
        mUseFastForward = false;
        initFloatingWindow();
        setAnchorView(null);
    }

    private void initFloatingWindow() {

//        mWindowManager = (WindowManager)mContext.getSystemService("window");
//        mWindow = PolicyManager.makeNewWindow(mContext);
//        mWindow.setWindowManager(mWindowManager, null, null);
//        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
//        mDecor = mWindow.getDecorView();
//        mDecor.setOnTouchListener(mTouchListener);
//        mWindow.setContentView(this);
//        mWindow.setBackgroundDrawableResource(android.R.color.transparent);
//
//        // While the media controller is up, the volume control keys should
//        // affect the media stream type
//        mWindow.setVolumeControlStream(AudioManager.STREAM_MUSIC);


//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
//        requestFocus();
    }


    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                }
            }
            return false;
        }
    };

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mPlayer = player;
                if (mPlayer instanceof ExoPlayerView) {
                    mProgress.setPreviewLoader((ExoPlayerView) mPlayer);
                    if (mList == null)
                        mList = new ArrayList<String>();
                    if (mPlayer instanceof ExoPlayerView) {
                        //   mList.clear();
                        mList = ((ExoPlayerView) mPlayer).getLanguages();
                        //To know the audio of the current playing content
                        if(((ExoPlayerView) mPlayer).getContentLanguage()!=null) {
                            presentContentLanguage = ((ExoPlayerView) mPlayer).getContentLanguage();
                        }

                    }
                    LoggerD.debugLog("audio tracks" + ((ExoPlayerView) mPlayer).getLanguages());
                    if (mList != null && mList.size() > 0) {
                        isAudioAvailable = true;
                        if(miniCardVideoPlayer != null && miniCardVideoPlayer.mData != null && miniCardVideoPlayer.mData.content != null && miniCardVideoPlayer.mData.content.language != null && miniCardVideoPlayer.mData.content.language.size() > 0 ) {
                            if(miniCardVideoPlayer.mData.content.language.get(0) != null) {
                                for(int i = 0; i < mList.size(); i++) {
                                    if (mList.size() > audioPosition && isAudioTrackChanged) {
                                        if (LangUtil.getSubtitleTrackName(mList.get(i)).equalsIgnoreCase(LangUtil.getSubtitleTrackName(mList.get(audioPosition)))) {
                                            audioPosition = i;
                                            isAudioTrackChanged=false;
                                            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                                                YuppAnalytics.getInstance(mContext).sendAudioTrackNames((LangUtil.getSubtitleTrackName(mList.get(audioPosition))), true);
                                            }
                                            return;
                                        }
                                    }else if(presentContentLanguage!=null && LangUtil.getSubtitleTrackName(mList.get(i)).equalsIgnoreCase(LangUtil.getSubtitleTrackName(presentContentLanguage))){
                                        audioPosition = i;
                                        if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                                            YuppAnalytics.getInstance(mContext).sendAudioTrackNames((LangUtil.getSubtitleTrackName(mList.get(audioPosition))), true);
                                        }
                                    }
                                   /* else {
                                        audioPosition = 0;
                                    }*/
                                }
                            }
                        } else
                            audioPosition = 0;

                    } else {
                        isAudioAvailable = false;
                    }
                    updateAudioSubtitleVisibility();

                }
                LoggerD.debugHooqVstbLog("setMediaPlayer>updatePlayPause");
                updatePlayPause();
                if (miniCardVideoPlayer != null && miniCardVideoPlayer.mData != null && miniCardVideoPlayer.mData.content != null &&
                        miniCardVideoPlayer.mData.content.isSupportCatchup != null &&
                        !miniCardVideoPlayer.mData.content.isSupportCatchup.isEmpty() && miniCardVideoPlayer.mData.content.isSupportCatchup.equalsIgnoreCase("true")) {
                    if(miniCardVideoPlayer!=null && !miniCardVideoPlayer.mIsAdPlaying) {
                        catchUpLinear.setVisibility(View.VISIBLE);
                        getCatcUpData();
                    }else{
                        catchUpLinear.setVisibility(View.GONE);
                    }
                } else {
                    catchUpLinear.setVisibility(View.GONE);
                }
                if (miniCardVideoPlayer != null && miniCardVideoPlayer.mListQueueCardData != null && miniCardVideoPlayer.mData != null &&
                        miniCardVideoPlayer.mData.generalInfo != null && miniCardVideoPlayer.mData.generalInfo.type != null && (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(miniCardVideoPlayer.mData.generalInfo.type))) {
                    episodeLinear.setVisibility(View.VISIBLE);
//            ll_next_episode.setVisibility(View.VISIBLE);

                }
                //getCatcUpData();
            }
        });
    }



    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view) {

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        removeAllViews();
        View v = makeControllerView();

        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mContentEnabled) {
            mRoot = inflate.inflate(R.layout.media_controller, null);
        } else {
            mRoot = inflate.inflate(R.layout.media_controller_live, null);
        }
        initControllerView(mRoot);
        return mRoot;
    }

    public void playerInFullScreen(boolean value) {
        if (mCustomTrackSelecter != null) {
            mCustomTrackSelecter.dismiss();
        }
        dismissSubtitles();
        updateLayoutParams(value);
        if (mFullScreenTooggleImage == null) {
            return;
        }
        if (value) {
            playerQualityLayout.setVisibility(GONE);
            //  videoQualityLinear.setVisibility(VISIBLE);
            // audioLanguageLinear.setVisibility(VISIBLE);
            // catchUpLinear.setVisibility(VISIBLE);
            //mFullScreenTooggleImage.setVisibility(VISIBLE);

            // as per 5.0 ui commented the below line
            mEndTime.setVisibility(GONE);
            if(miniCardVideoPlayer!=null && !miniCardVideoPlayer.mIsAdPlaying) {
                mCurrentTime.setVisibility(VISIBLE);
                bottomSelectionParentLL.setVisibility(VISIBLE);
                if(bottomSheetSubtitleAudioDialog != null)
                    bottomSheetSubtitleAudioDialog.dismiss();
           /* RelativeLayout.LayoutParams miniMize = new RelativeLayout.LayoutParams(70, 50);
            miniMize.addRule(RelativeLayout.ALIGN_RIGHT,bottomSelectionParentLL.getId());
            miniMize.addRule(RelativeLayout.BELOW, mProgress.getId());
            miniMize.setMargins(0, 20, 40, 0);
            mFullScreenTooggle.setLayoutParams(miniMize);*/
                //mFullScreenTooggleImage.setImageResource(R.drawable.fullscreen_minimize_icon);

                mLockScreenImage.setImageResource(R.drawable.icon_unlock_new);
                mLockScreen.setVisibility(VISIBLE);
                mLockScreenImage.setVisibility(VISIBLE);
                mProgress.setBottom(10);
                mPlayerExitScreen.setVisibility(VISIBLE);
            }
            if(bottomSheetSubtitleAudioDialog != null && bottomSheetDialog!=null && bottomSheetDialog.isShowing()) {
                bottomSheetSubtitleAudioDialog.show();
            }
            mFullScreenTooggle.setVisibility(GONE);
            // mRewindButtonContainer.setVisibility(VISIBLE);
            //mFfwdButtonContainer.setVisibility(VISIBLE);
            // mFullScreenTooggleImage.setVisibility(GONE);
            mMuteButton.setVisibility(GONE);

            if (mMuteEnabled)  {
                mMuteButtonImage.setImageResource(R.drawable.icon_unmute_new);
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                //mMediaPlayer.setVolume(1,1);
                mMuteEnabled = false;
            }

        } else {
            mLockScreen.setVisibility(GONE);
            mLockScreenImage.setVisibility(GONE);
            // mFullScreenTooggleImage.setImageResource(R.drawable.fullscreen_exit_btn);
            playerQualityLayout.setVisibility(VISIBLE);
            bottomSelectionParentLL.setVisibility(GONE);
            mFullScreenTooggle.setVisibility(VISIBLE);
           /* if(miniCardVideoPlayer!=null && !miniCardVideoPlayer.mIsAdPlaying) {
                mFullScreenTooggle.setVisibility(GONE);
            }*/
            rvAudioTracks.setVisibility(GONE);
            mPlayerExitScreen.setVisibility(GONE);
            if (bottomSheetDialog != null)
                bottomSheetDialog.dismiss();
            if (bottomSheetSubtitleAudioDialog != null)
                bottomSheetSubtitleAudioDialog.dismiss();
            //  audioLanguageLinear.setVisibility(GONE);
            //  videoQualityLinear.setVisibility(GONE);
            // catchUpLinear.setVisibility(GONE);
            // mFullScreenTooggleImage.setVisibility(VISIBLE);
            mMuteButton.setVisibility(VISIBLE);
            if(miniCardVideoPlayer!=null && miniCardVideoPlayer.isFromCatchup && mFfwdButtonContainer!=null && mRewindButtonContainer!=null ){
              mFfwdButtonContainer.setVisibility(VISIBLE);
              mRewindButtonContainer.setVisibility(VISIBLE);
            }

        }
        if (isLive && miniCardVideoPlayer!=null && miniCardVideoPlayer.mData!=null && miniCardVideoPlayer.mData.generalInfo!=null && miniCardVideoPlayer.mData.generalInfo.isDvr) {
            mRoot.findViewById(R.id.mediacontroller_progress).setVisibility(VISIBLE);
        }else if(isLive){
            mRoot.findViewById(R.id.mediacontroller_progress).setVisibility(INVISIBLE);
        }
        else {
            mRoot.findViewById(R.id.mediacontroller_progress).setVisibility(VISIBLE);
        }

        if (isFullScreen && miniCardVideoPlayer!=null && !miniCardVideoPlayer.mIsAdPlaying) {
            divider_line.setVisibility(VISIBLE);
            ll_player_option_bg.setBackground(getResources().getDrawable(R.drawable.bg_player_option));
        } else {
            ll_player_option_bg.setBackground(null);
            divider_line.setVisibility(GONE);
        }
        expandLayoutParams(value);

        int yourDP = (int) getResources().getDimension(R.dimen._10sdp);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,yourDP,r.getDisplayMetrics());
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mProgress.getLayoutParams();

        if(value){
            params.setMargins((int) px,0,(int) px,0); // params.setMargins(0,0,0,0); at first
        }else {
            params.setMargins(0,0,0,0); // params.setMargins(0,0,0,0); at first
        }

        mProgress.setLayoutParams(params);

    }

    private void updateLayoutParams(boolean value) {
        if (mRoot == null || mRoot.getLayoutParams() == null) {
            return;
        }
        if (value) {
            RelativeLayout.LayoutParams relparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) mContext.getResources().getDimension(R.dimen.margin_gap_56));
            LinearLayout.LayoutParams linparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, /*(int) mContext.getResources().getDimension(R.dimen.margin_gap_56)*/ViewGroup.LayoutParams.WRAP_CONTENT);
            /*relparams.addRule(RelativeLayout.ALIGN_PARENT_TOP);*/
            /*linparams.bottomMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_2);
            relparams.topMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_2);*/
            if (mPlayerHeaderView != null) {
                // mPlayerHeaderView.setLayoutParams(relparams);
            }
            if (mRoot.findViewById(R.id.controlls) != null) {
                mRoot.findViewById(R.id.controlls).setLayoutParams(linparams);
            }
            /*if (mPlayerControllsView != null) {
                RelativeLayout.LayoutParams controllsparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) mContext.getResources().getDimension(R.dimen.margin_gap_56));
                controllsparams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                mPlayerControllsView.setLayoutParams(controllsparams);
            }*/
        } else {
            RelativeLayout.LayoutParams relparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) mContext.getResources().getDimension(R.dimen.margin_gap_56));
            LinearLayout.LayoutParams linparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, /*(int) mContext.getResources().getDimension(R.dimen.margin_gap_56)*/ViewGroup.LayoutParams.WRAP_CONTENT);
            /*relparams.addRule(RelativeLayout.ALIGN_PARENT_TOP);*/
            /*linparams.bottomMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_4);
            relparams.topMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_4);*/
            if (mPlayerHeaderView != null) {
                //   mPlayerHeaderView.setLayoutParams(relparams);
            }
            if (mRoot.findViewById(R.id.controlls) != null) {
                mRoot.findViewById(R.id.controlls).setLayoutParams(linparams);
            }
            /*if (mPlayerControllsView != null) {
                RelativeLayout.LayoutParams controllsparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) mContext.getResources().getDimension(R.dimen.margin_gap_56));
                controllsparams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                mPlayerControllsView.setLayoutParams(controllsparams);
            }*/
        }
    }

    private boolean mPlayerFullScreen = false;

    private void initControllerView(View v) {
        mPlayerFullScreen = false;
        if (mContentEnabled) {
            mPauseButton = (RelativeLayout) v.findViewById(R.id.playpauseRL);
            mPauseButtonImage = (ImageView) v.findViewById(R.id.playpauseimage);

            showFeedback(mPauseButton);
        }
        if (mPauseButton != null) {
            mPauseButton.setVisibility(View.GONE);
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        mFullScreenTooggle = (RelativeLayout) v.findViewById(R.id.playerfullscreen);
        mPlayerExitScreen = (RelativeLayout) v.findViewById(R.id.playerexitscreen);
        durationLL = (RelativeLayout) v.findViewById(R.id.durationLL);
        mCurrentTime = (TextView) v.findViewById(R.id.playerexpiredtime);
        mProgress = (PreviewSeekBar) v.findViewById(R.id.mediacontroller_progress);
        divider_line = (View) v.findViewById(R.id.divider_line);
        ll_player_option_bg = v.findViewById(R.id.ll_player_option_bg);
        imageViewSeekBar = mRoot.findViewById(R.id.imageView);
        previewFrameLayout = mRoot.findViewById(R.id.previewFrameLayout);
        rvAudioTracks = mRoot.findViewById(R.id.rvAudioTracks);
        //mFullScreenTooggle.setVisibility(VISIBLE);
        showFeedback(mFullScreenTooggle);
        mFullScreenTooggleImage = (ImageView) v.findViewById(R.id.playerfullscreenimage);
        mFullScreenTooggleImage.setVisibility(VISIBLE);
        //mFullScreenTooggleImage.setVisibility(VISIBLE);
        // expandLayoutParams();
        mLockScreen = (FrameLayout) v.findViewById(R.id.rrlockplayer);
        mLockScreenImage = (ImageView) v.findViewById(R.id.lock_player);
        playerQualityLayout = v.findViewById(R.id.playerquality_layout);
        audioLanguageLinear = (LinearLayout) v.findViewById(R.id.audiolanguagelinear);
        videoQualityLinear = (LinearLayout) v.findViewById(R.id.videoqualitylinear);
//        mSubtitleSettingsButtton = (LinearLayout) v.findViewById(R.id.header_settings_subtitles);
        catchUpLinear = (LinearLayout) v.findViewById(R.id.catchuplinear);
        episodeLinear = (LinearLayout) v.findViewById(R.id.episodeLinear);
        ll_next_episode = v.findViewById(R.id.ll_next_episode);
        bottomSelectionParentLL = (LinearLayout) v.findViewById(R.id.bottomSelectionParentLL);

      /*  mPauseButton.setAlpha(0);
        mPauseButtonImageCenter.setAlpha(0);
        mPauseButtonImage.setAlpha(0);*/

        if (mFullScreenTooggle != null) {
            mFullScreenTooggle.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mPlayerListener != null) {
                        mPlayerFullScreen = !mPlayerFullScreen;
                        mPlayerListener.onFullScreen(mPlayerFullScreen);
                        playerInFullScreen(mPlayerFullScreen);
                    }
                }
            });
        }
        if (mPlayerExitScreen != null) {
            mPlayerExitScreen.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mPlayerListener != null) {
                        mPlayerFullScreen = !mPlayerFullScreen;
                        mPlayerListener.onFullScreen(mPlayerFullScreen);
                        playerInFullScreen(mPlayerFullScreen);
                    }
                }
            });
        }
        catchUpLinear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });
        episodeLinear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                miniCardVideoPlayer.setSessionData();
            }
        });
       /* ll_next_episode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                miniCardVideoPlayer.playNextEpisode();
            }
        });*/
        mMuteButton = (RelativeLayout) v.findViewById(R.id.playervolume);
        go_live_button =  v.findViewById(R.id.go_live_button);
        showFeedback(mMuteButton);
        mMuteButtonImage = (ImageView) v.findViewById(R.id.playervolumeimage);
//        mMuteButtonImage.setVisibility(GONE);

        v.findViewById(R.id.lock_player).setOnClickListener(view -> {
            if (mIsLocked) {
                mLockScreenImage.setImageResource(R.drawable.icon_unlock_new);
                mIsLocked = false;
                APIConstants.IS_PLAYER_SCREEN_LOCKED = false;
                if(miniCardVideoPlayer != null)
                    miniCardVideoPlayer.unLockControles();
                show();
            } else {
                mLockScreenImage.setImageResource(R.drawable.icon_lock_new);
                hide();
                mIsLocked = true;
                APIConstants.IS_PLAYER_SCREEN_LOCKED = true;
            }
        });

        v.findViewById(R.id.videoqualitylinear).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rvAudioTracks.getVisibility() == VISIBLE) {
                    rvAudioTracks.setVisibility(GONE);
                } else {
                    rvAudioTracks.setVisibility(VISIBLE);
                    if (mCustomTrackSelecter != null) {
                        mCustomTrackSelecter.showQualitySelectionGrid(rvAudioTracks, videoQuality, new QualitySelection() {
                            @Override
                            public void getSelectedItem(String name) {
                                videoQuality = name;
                            }
                        });
                    }
                }

            }
        });
        audioLanguageLinear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) "+ (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE));
//                if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    showAudioSubtitleBottomSheet();
/*                if (rvAudioTracks.getVisibility() == VISIBLE) {
                    rvAudioTracks.setVisibility(GONE);
                } else {
                    rvAudioTracks.setVisibility(VISIBLE);
                    showAudioTrackDialog();
                }*/
            }
        });

     /*   v.findViewById(R.id.playerquality).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCustomTrackSelecter != null) {
                    mCustomTrackSelecter.showQualitySelectionGrid();
                }
            }
        });*/
        if (mMuteButton != null) {
            mMuteButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
//					AudioManager audioManager = myplexapplication.getAudioManger();
                        //	int current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//					    //If you want to player is mute ,then set_volume variable is zero.Otherwise you may supply some value.
//						int set_volume=0;
//						if(current_volume == 0){
//							 set_volume = 1;
//						}
//						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,set_volume, 0);

                        if (!mMuteEnabled) {
//							Toast toast = Toast.makeText(mContext, "Muted", Toast.LENGTH_LONG);
//							toast.show();
                            Toast.makeText(mContext,
                                    "Muted",
                                    Toast.LENGTH_LONG).show();
                            mMuteButtonImage.setImageResource(R.drawable.icon_mute_new);
                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                            //mMediaPlayer.setVolume(0,0);
                            mMuteEnabled = true;
                        } else {
//							Toast toast = Toast.makeText(mContext, "Unmuted", Toast.LENGTH_LONG);
//							toast.show();
                            Toast.makeText(mContext,
                                    "UnMuted",
                                    Toast.LENGTH_LONG).show();
                            mMuteButtonImage.setImageResource(R.drawable.icon_unmute_new);
                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                            //mMediaPlayer.setVolume(1,1);
                            mMuteEnabled = false;
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
//
//        mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
//        if (mFfwdButton != null) {
//            mFfwdButton.setOnClickListener(mFfwdListener);
//            if (!mFromXml) {
//                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
//            }
//        }
//
//        mRewButton = (ImageButton) v.findViewById(R.id.rew);
//        if (mRewButton != null) {
//            mRewButton.setOnClickListener(mRewListener);
//            if (!mFromXml) {
//                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
//            }
//        }
//
//        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
//        mNextButton = (ImageButton) v.findViewById(R.id.next);
//        if (mNextButton != null && !mFromXml && !mListenersSet) {
//            mNextButton.setVisibility(View.GONE);
//        }
//        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
//        if (mPrevButton != null && !mFromXml && !mListenersSet) {
//            mPrevButton.setVisibility(View.GONE);
//        }


        if (!PrefUtils.getInstance().getSeekBarEnable()) {
            mProgress.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(!PrefUtils.getInstance().getSeekBarEnable()) {
                        return true;
                    }else{
                       return false;
                    }
                }
            });
        }
        if (mProgress != null) {
            /*SeekBar seeker = (SeekBar) mProgress;*/
            mProgress.setOnSeekBarChangeListener(mSeekListener);
//            onSeekBarChangeListener=mSeekListener;
            mProgress.setMax(1000);
            /*if(mProgress.getThumb() != null
                    && mProgress.getThumb().mutate() != null){
                mProgress.getThumb().mutate().setAlpha(0);
            }*/
        }

        if (mContentEnabled) {
            mEndTime = (TextView) v.findViewById(R.id.playertotaltime);
            //  mCurrentTime = (TextView) v.findViewById(R.id.playerexpiredtime);
        }
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        if (getScreenOrientation() == SCREEN_ORIENTATION_PORTRAIT) {
            // mFullScreenTooggle.setVisibility(VISIBLE);
            if(miniCardVideoPlayer!=null && !miniCardVideoPlayer.mIsAdPlaying) {
                mCurrentTime.setVisibility(VISIBLE);
            }
            playerInFullScreen(false);
        } else
            playerInFullScreen(true);

        installPrevNextListeners();
        updateLayoutParams(isFullScreen);
    }

    private void expandLayoutParams(boolean isFullScreen) {
        if (isFullScreen) {
            RelativeLayout.LayoutParams expandLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (divider_line.getVisibility() == INVISIBLE) {
                expandLayout.addRule(RelativeLayout.BELOW, divider_line.getId());
            } else {
                expandLayout.addRule(RelativeLayout.BELOW, mProgress.getId());
            }
            expandLayout.setMargins(125, 20, 20, 0);
            durationLL.setLayoutParams(expandLayout);
            RelativeLayout.LayoutParams divider = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    4);
            divider.addRule(RelativeLayout.BELOW, rvAudioTracks.getId());
            divider_line.setLayoutParams(divider);
        } else {

            RelativeLayout.LayoutParams durationParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            durationParams.setMargins(20, 0, 20, 20);
            //   divider.addRule(RelativeLayout.BELOW, mProgress.getId());
            durationLL.setLayoutParams(durationParams);
            RelativeLayout.LayoutParams expandLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    4);
            expandLayout.addRule(RelativeLayout.BELOW, durationLL.getId());
            divider_line.setLayoutParams(expandLayout);
        }
    }

    private void currentTimeLayout(boolean value) {
        RelativeLayout.LayoutParams timeLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 50);
        if (value) {
            timeLayout.addRule(RelativeLayout.ALIGN_LEFT);
            timeLayout.setMargins(50, 0, 0, 0);
            mCurrentTime.setLayoutParams(timeLayout);
        } else {
            timeLayout.addRule(RelativeLayout.BELOW, mProgress.getId());
            timeLayout.setMargins(50, 0, 0, 0);
            mCurrentTime.setLayoutParams(timeLayout);
        }
    }

    public List<CardData> cardDataList;

    public void getCatcUpData() {
        if (mPlayer instanceof ExoPlayerView) {
            String mId = null;
            if (((ExoPlayerView) mPlayer).mCardData != null
                    && ((ExoPlayerView) mPlayer).mCardData.generalInfo != null
                    && ((ExoPlayerView) mPlayer).mCardData.generalInfo.type != null
                    && ((ExoPlayerView) mPlayer).mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                    && ((ExoPlayerView) mPlayer).mCardData.globalServiceId != null) {
                mId = ((ExoPlayerView) mPlayer).mCardData.globalServiceId;
            } else if (((ExoPlayerView) mPlayer).mCardData != null
                    && ((ExoPlayerView) mPlayer).mCardData._id != null) {
                mId = ((ExoPlayerView) mPlayer).mCardData._id;
            }
            if (mId != null) {
                final Date date = Util.getCurrentDate(ApplicationController.DATE_POSITION);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String selectedDateInString = format.format(date);
                String dateStamp = Util.getYYYYMMDD(selectedDateInString);
                EpgCatchUpList.Params params = new EpgCatchUpList.Params(mId, dateStamp, true);
                EpgCatchUpList channelListEPG = new EpgCatchUpList(params, new APICallback<ChannelsCatchupEPGResponseData>() {
                    @Override
                    public void onResponse(APIResponse<ChannelsCatchupEPGResponseData> response) {
                        if (response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size() > 0) {
                        /*    for (int i = 0; i < response.body().getResults().size(); i++) {
                                 cardDataList = response.body().getResults().get(i).getPrograms();
                                if (cardDataList != null && cardDataList.size() > 0) {
                                    catchUpLinear.setVisibility(View.VISIBLE);
                                    rvAudioTracks.setVisibility(View.VISIBLE);
                                    rvAudioTracks.setHasFixedSize(true);
                                    // recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                                    rvAudioTracks.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, true));
                                  //  AdapterMedHorizontalCarousel adapterMedHorizontalCarousel = new AdapterMedHorizontalCarousel(mContext, cardDataList, rvAudioTracks);
                                    // adapterMedHorizontalCarousel.setData(cardDaplayPreviousNextEpisodetaList);
                                    AdapterEpisodePlayer mAdapterEpisode = new AdapterEpisodePlayer(mContext, cardDataList, null);
                                    rvAudioTracks.setAdapter(mAdapterEpisode);


                                } else
                                    catchUpLinear.setVisibility(GONE);
                            }*/
                            cardDataList = response.body().getResults();
                            if (((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup || miniCardVideoPlayer.isCatchup) {
                                miniCardVideoPlayer.setNextPreviousData();
                            }
                            // catchUpLinear.setVisibility(VISIBLE);
                        } /*else
                            catchUpLinear.setVisibility(GONE);*/
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        // catchUpLinear.setVisibility(GONE);
                    }
                });
                APIService.getInstance().execute(channelListEPG);
            }
        }
    }

    BottomSheetDialog bottomSheetDialog;

    private void showBottomSheetDialog() {
      /*  if (mPlayer.isPlaying())
            doManualPlayPause();*/
        bottomSheetDialog = new BottomSheetDialog(mContext, R.style.NoBackgroundDialogTheme);
        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(Gravity.CENTER);
        params.setMargins(120, 0, 120, 40);
        view.setLayoutParams(params);
        bottomSheetDialog.setContentView(view);
        RecyclerView rv_catchup_list = view.findViewById(R.id.rv_catchup_list);
        ImageView leftArrow = view.findViewById(R.id.left_arrow);
        ImageView rightArrow = view.findViewById(R.id.right_arrow);
        ImageView close_btn = view.findViewById(R.id.close_btn);
        rv_catchup_list.setHasFixedSize(true);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, mContext.getResources().getDisplayMetrics());
        HorizontalItemDecorator mHorizontalMoviesDivieder = new HorizontalItemDecorator(margin);
        // recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        leftArrow.setVisibility(GONE);
        rv_catchup_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rv_catchup_list.getLayoutManager();  //new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
                //Log.d(TAG, "onScrollStateChanged: " + pastVisibleItems + " , " + visibleItemCount);
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    rightArrow.setVisibility(GONE);
                    leftArrow.setVisibility(VISIBLE);
                } else if (pastVisibleItems == 0) {
                    rightArrow.setVisibility(VISIBLE);
                    leftArrow.setVisibility(GONE);
                } else {
                    rightArrow.setVisibility(VISIBLE);
                    leftArrow.setVisibility(VISIBLE);
                }

                super.onScrollStateChanged(recyclerView, newState);
            }

        });

        rv_catchup_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        rv_catchup_list.addItemDecoration(mHorizontalMoviesDivieder);
        if (cardDataList != null && cardDataList.size() > 0) {
            if (mPlayer instanceof ExoPlayerView) {
                AdapterCatchupCarousel adapterMedHorizontalCarousel = new AdapterCatchupCarousel(mContext, cardDataList, null);
                // adapterMedHorizontalCarousel.setData(cardDataList);
                rv_catchup_list.setAdapter(adapterMedHorizontalCarousel);
                adapterMedHorizontalCarousel.notifyDataSetChanged();
                adapterMedHorizontalCarousel.setOnItemClickListenerWithMovieData(new ItemClickListenerWithData() {
                    @Override
                    public void onClick(View view, int position, int parentPosition, CardData movieData) {
                        if (miniCardVideoPlayer != null) {
                            bottomSheetDialog.dismiss();
                            if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null) {
                                ((MainActivity) mContext).mFragmentCardDetailsPlayer.selectedPosition = position;
                            }
                            if(movieData != null) {
                                String title = movieData.startDate;
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("contentTitle", title);
                                editor.apply();
                                editor.commit();
                            }
                            miniCardVideoPlayer.playPreviousNextEpisode(movieData, true);
                        }
                    }
                });
            }
        }

        close_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                bottomSheetBehavior.setPeekHeight(view.getHeight());
            }
        });
        bottomSheetDialog.show();
    }

    public void fullScreen() {
        if (mPlayerListener != null) {
            mPlayerListener.onFullScreen(true);
            playerInFullScreen(true);
        }
    }

    List<String> mList;
    public int audioPosition;
    public int subtitlePosition;
    public boolean isAudioTrackChanged=false;
    public String presentContentLanguage=null;

    public void showAudioTrackDialog() {
        if (mList != null) {
            AudioTrackAdapter adapter = new AudioTrackAdapter(mList, new AudioTrackListner() {
                @Override
                public void getSelectedItem(int audio) {
                    if (mPlayer instanceof ExoPlayerView) {
                        ((ExoPlayerView) mPlayer).setAudioTrack(mList.get(audio));
                        audioPosition = audio;
                        rvAudioTracks.setVisibility(GONE);
                        isAudioTrackChanged=true;
                    }
                }
            }, audioPosition, mContext);
            rvAudioTracks.setHasFixedSize(true);
            // recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            rvAudioTracks.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, true));
            rvAudioTracks.setAdapter(adapter);
        }

    }

    public void onClickQualitySelection() {
        if (mCustomTrackSelecter != null) {
            if (mPlayer != null && !(mPlayer instanceof MyplexVideoViewPlayer)) {
                List<TrackData> mTrackDataList = ((MyplexVideoViewPlayer) mPlayer).getBitrateCappingTracks();

                int count = 0;
                for (int i = 0; i < mTrackDataList.size(); i++) {
                    TrackData track = mTrackDataList.get(i);

                    String packageQuality = PrefUtils.getInstance().getPackageQuality();
                    if (!TextUtils.isEmpty(packageQuality)) {
                        String[] minMaxValue = packageQuality.split("-");
                        float startBitrate = Float.parseFloat(minMaxValue[0]);
                        float endBitrate = Float.parseFloat(minMaxValue[1]);

                        if (track.bitrate >= startBitrate && track.bitrate <= endBitrate) {
                            count++;
                        }
                    }
                }

                if (count == 1) {
                    return;
                }
            }

            // mCustomTrackSelecter.showQualitySelectionGrid();
        }
    }


    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        LoggerD.debugLog("showMediaController");
        show(sDefaultTimeout);
    }

    public void setMiniCardVideoPlayer(MiniCardVideoPlayer miniCardVideoPlayer) {
        this.miniCardVideoPlayer = miniCardVideoPlayer;
    }

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

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mPlayer == null) {
                return;
            }

            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        Log.v("MediaController", "show: isAllowed- " + isAllowed() + " timeout- " + timeout);
        if (mPlayer == null || !isAllowed()) {
            return;
        }
       if(mContext != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer != null &&
               ((MainActivity) mContext).mFragmentCardDetailsPlayer.mDraggablePanel != null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mDraggablePanel.isMinimized()) {
           return;
       }
        if (miniCardVideoPlayer != null && miniCardVideoPlayer.isMinimized()) {
            return;
        }
        if (APIConstants.IS_PLAYER_SCREEN_LOCKED) {
            mRoot.findViewById(R.id.playerexpiredtime).setVisibility(INVISIBLE);
            mRoot.findViewById(R.id.playerfullscreen).setVisibility(INVISIBLE);
            // mRoot.findViewById(R.id.playerquality).setVisibility(VISIBLE);
            mRoot.findViewById(R.id.playertotaltime).setVisibility(INVISIBLE);
            miniCardVideoPlayer.mBackIconImageView.setVisibility(INVISIBLE);
            miniCardVideoPlayer.nextContentLL.setVisibility(INVISIBLE);
            miniCardVideoPlayer.previousContentLL.setVisibility(INVISIBLE);
            miniCardVideoPlayer.mChromeCastToolbar.setVisibility(INVISIBLE);
            miniCardVideoPlayer.videoDescription.setVisibility(INVISIBLE);
            miniCardVideoPlayer.videoTitle.setVisibility(INVISIBLE);
            mRoot.findViewById(R.id.mediacontroller_progress).setVisibility(INVISIBLE);
            mRoot.findViewById(R.id.divider_line).setVisibility(INVISIBLE);
            mRoot.findViewById(R.id.rrlockplayer).setVisibility(VISIBLE);
            mFfwdButtonContainer.setVisibility(INVISIBLE);
            mRewindButtonContainer.setVisibility(INVISIBLE);
            mPauseButtonImageCenter.setVisibility(INVISIBLE);
            bottomSelectionParentLL.setVisibility(INVISIBLE);
            mPlayerExitScreen.setVisibility(INVISIBLE);
            if(miniCardVideoPlayer != null && isFullScreen && miniCardVideoPlayer.mGestureControllsLayoutContainer != null)
                miniCardVideoPlayer.mGestureControllsLayoutContainer.setVisibility(INVISIBLE);
            //  mRoot.findViewById(R.id.video_title).setVisibility(INVISIBLE);

            //     mtitle.setVisibility(INVISIBLE);
/*
            mheader_title.setVisibility(INVISIBLE);
*/

        } else {
            if (isFullScreen && miniCardVideoPlayer!=null && !miniCardVideoPlayer.mIsAdPlaying) {
                mRoot.findViewById(R.id.rrlockplayer).setVisibility(VISIBLE);
                bottomSelectionParentLL.setVisibility(VISIBLE);
                mPlayerExitScreen.setVisibility(VISIBLE);
                miniCardVideoPlayer.mBackIconImageView.setVisibility(VISIBLE);
                if(!((MainActivity) mContext).mFragmentCardDetailsPlayer.mIsSupportCatchup){
                    miniCardVideoPlayer.nextContentLL.setVisibility(VISIBLE);
                    miniCardVideoPlayer.previousContentLL.setVisibility(VISIBLE);
                }else {
                    miniCardVideoPlayer.nextContentLL.setVisibility(GONE);
                    miniCardVideoPlayer.previousContentLL.setVisibility(GONE);
                }
                miniCardVideoPlayer.mChromeCastToolbar.setVisibility(VISIBLE);
                miniCardVideoPlayer.videoTitle.setVisibility(VISIBLE);
                miniCardVideoPlayer.videoDescription.setVisibility(VISIBLE);
            }
            if (isLive && miniCardVideoPlayer!=null && miniCardVideoPlayer.mData!=null && miniCardVideoPlayer.mData.generalInfo!=null && miniCardVideoPlayer.mData.generalInfo.isDvr) {
                mRoot.findViewById(R.id.mediacontroller_progress).setVisibility(VISIBLE);
            }else if(isLive){
                mRoot.findViewById(R.id.mediacontroller_progress).setVisibility(INVISIBLE);
            }
            else {
                mRoot.findViewById(R.id.mediacontroller_progress).setVisibility(VISIBLE);
            }

            if (isFullScreen) {
                ll_player_option_bg.setBackground(getResources().getDrawable(R.drawable.bg_player_option));
                divider_line.setVisibility(VISIBLE);
            } else{
                ll_player_option_bg.setBackground(null);
                divider_line.setVisibility(GONE);
            }
            mPauseButtonImageCenter.setVisibility(VISIBLE);
            // mRoot.findViewById(R.id.playerquality).setVisibility(VISIBLE); //disabled as per client requirement
            mRoot.findViewById(R.id.playerexpiredtime).setVisibility(VISIBLE);
            mRoot.findViewById(R.id.playertotaltime).setVisibility(GONE);
            // mRoot.findViewById(R.id.playerfullscreen).setVisibility(VISIBLE);
            //commented not required this fast forward for sun direct
            // mFfwdButtonContainer.setVisibility(VISIBLE);
            //commented not required this rewind  for sun direct
            if (isFullScreen && miniCardVideoPlayer != null && (isLive )) {
                mRewindButtonContainer.setVisibility(INVISIBLE);
                mFfwdButtonContainer.setVisibility(INVISIBLE);
                mFfwdButton.setVisibility(INVISIBLE);
                mRewButton.setVisibility(INVISIBLE);
            } else {
                mRewindButtonContainer.setVisibility(VISIBLE);
                mFfwdButtonContainer.setVisibility(VISIBLE);
                mFfwdButton.setVisibility(VISIBLE);
                mRewButton.setVisibility(VISIBLE);
            }

            if(isLive){
                mRewindButtonContainer.setVisibility(INVISIBLE);
                mFfwdButtonContainer.setVisibility(INVISIBLE);
            }else {
                mRewindButtonContainer.setVisibility(VISIBLE);
                mFfwdButtonContainer.setVisibility(VISIBLE);
            }

            if(miniCardVideoPlayer != null && isFullScreen && miniCardVideoPlayer.mGestureControllsLayoutContainer != null)
                miniCardVideoPlayer.mGestureControllsLayoutContainer.setVisibility(VISIBLE);
        }

        LoggerD.debugLog("showMediaController");

        LoggerD.debugHooqVstbLog("show>updatePlayPause + timeout- " + timeout);
        updatePlayPause();
        if (!mShowing) {
//            setProgress();
            hideSystemUI();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            if (mPlayerMediaControllsView != null)
                UiUtil.applyShowAlphaAnimation(mPlayerMediaControllsView);
//            UiUtil.applyShowAlphaAnimation(this);
//            setVisibility(View.VISIBLE);
            if ((!isToolbarShown || isFullScreen())
                    && mPlayerHeaderView != null) {
//                mPlayerHeaderView.setVisibility(VISIBLE);
                UiUtil.applyShowAlphaAnimation(mPlayerHeaderView);
            }
            if (mPlayerControllsView != null) {
//                mPlayerHeaderView.setVisibility(VISIBLE);
                UiUtil.applyShowAlphaAnimation(mPlayerControllsView);
            }
            invalidate();
            requestLayout();
            mShowing = true;
        }

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }

        showPlayerControllersWhilePlayingAd();

    }

    private void showPlayerControllersWhilePlayingAd() {
        if(miniCardVideoPlayer!=null && miniCardVideoPlayer.mIsAdPlaying){
            mPauseButtonImageCenter.setVisibility(GONE);
          /*  if(!DeviceUtils.isTablet(mContext)){
                RelativeLayout.LayoutParams playPauseParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                playPauseParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mPauseButtonImageCenter.setLayoutParams(playPauseParams);
            }*/

           /* mFullScreenTooggle.setVisibility(GONE);
            mPlayerExitScreen.setVisibility(GONE);*/
            mLockScreenImage.setVisibility(GONE);
            audioLanguageLinear.setVisibility(GONE);
            ll_player_option_bg.setVisibility(GONE);
            mCurrentTime.setVisibility(GONE);
            if (!isFullScreen) {
                mFullScreenTooggle.setVisibility(VISIBLE);
            }else{
                mPlayerExitScreen.setVisibility(VISIBLE);
            }
            mFfwdButtonContainer.setVisibility(GONE);
            mRewindButtonContainer.setVisibility(GONE);
            mProgress.setVisibility(GONE);
            mMuteButtonImage.setVisibility(GONE);
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
        }else {
            if (!mIsLocked) {
                mPauseButtonImageCenter.setVisibility(VISIBLE);
                RelativeLayout.LayoutParams playPauseParams = new RelativeLayout.LayoutParams(layoutParamsOriginal);
//                playPauseParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                playPauseParams.setMargins(0,10,120,0);
                mPauseButtonImageCenter.setLayoutParams(playPauseParams);
                if (!isFullScreen) {
                    mFullScreenTooggle.setVisibility(VISIBLE);
                }
                mCurrentTime.setVisibility(VISIBLE);
                if (isFullScreen) {
                    mPlayerExitScreen.setVisibility(VISIBLE);
                    mLockScreenImage.setVisibility(VISIBLE);
                }
                mMuteButtonImage.setVisibility(VISIBLE);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
                if(miniCardVideoPlayer!=null && miniCardVideoPlayer.isFromCatchup){
                    mFfwdButtonContainer.setVisibility(VISIBLE);
                    mRewindButtonContainer.setVisibility(VISIBLE);
                }
                ll_player_option_bg.setVisibility(VISIBLE);
                updateAudioSubtitleVisibility();
            }
        }
    }

    private void hideSystemUI() {
        if (isFullScreen) {
            ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
           /* ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
            );*/
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        Log.v("MediaController", "hide()");
        if (mShowing) {
            try {
                hideSystemUI();
                mHandler.removeMessages(SHOW_PROGRESS);
//                setVisibility(View.INVISIBLE);
//                setVisibility(INVISIBLE);
                if (mPlayerMediaControllsView != null)
                    UiUtil.applyHideAlphaAnimation(mPlayerMediaControllsView);
                /*UiUtil.applyHideAlphaAnimation(this);*/
                if (mPlayerHeaderView != null) {
//                    mPlayerHeaderView.setVisibility(INVISIBLE);
                    UiUtil.applyHideAlphaAnimation(mPlayerHeaderView);
                }

                if (mPlayerControllsView != null) {
//                mPlayerHeaderView.setVisibility(VISIBLE);
                    UiUtil.applyHideAlphaAnimation(mPlayerControllsView);
                }

            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer != null && mPlayer.isPlaying()
                        /*  && !isNonDVRLive*/) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        if (isNonDVRLive && !(miniCardVideoPlayer!=null && miniCardVideoPlayer.mData!=null && miniCardVideoPlayer.mData.generalInfo!=null && miniCardVideoPlayer.mData.generalInfo.isDvr)) {
            showLiveSeekBarUI();
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        Log.d("DEBUG", "setProgress position " + position);
        Log.d("DEBUG", "setProgress duration " + duration);
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
           /* if (isNonDVRLive) {
                mProgress.setProgress(1000);
            }*/
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        String totalTime = mEndTime.getText().toString();
        if (mCurrentTime != null) {
            RelativeLayout.LayoutParams progressLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (getScreenOrientation() == SCREEN_ORIENTATION_PORTRAIT) {
                if (!APIConstants.IS_PLAYER_SCREEN_LOCKED) {
                    if (miniCardVideoPlayer != null && !miniCardVideoPlayer.mIsAdPlaying) {
                        mCurrentTime.setVisibility(VISIBLE);
                        /* if(isNonDVRLive){
                            Log.e("mCurrentTime","ONE"+stringForTime(position));
                            if(miniCardVideoPlayer!=null && miniCardVideoPlayer.mData.startDate!=null && miniCardVideoPlayer.mData.endDate!=null) {
                                long startDate = 0;
                                try {
                                    startDate = Util.parseXsDateTime(miniCardVideoPlayer.mData.startDate);
                                    EPGUtil.getShortTime(startDate);
                                    long endDate = Util.parseXsDateTime(miniCardVideoPlayer.mData.endDate);

                                    EPGUtil.getShortTime(endDate);
                                    String totalDuration= String.valueOf(endDate-startDate);

                                    Calendar calendar = Calendar.getInstance();
                                    long time=calendar.getTimeInMillis();
                                    String presentRunningTime = String.valueOf(time-startDate);

                                    mCurrentTime.setText(stringForTime(Integer.parseInt(presentRunningTime))+"/"+stringForTime(Integer.parseInt(totalDuration)));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }else {*/
                        mCurrentTime.setText(stringForTime(position) + "/" + totalTime);
                    }
                }
                progressLayout.setMargins(0, 50, 0, 0);
                // mProgress.setLayoutParams(progressLayout);
                // currentTimeLayout(true);
            } else {
                // currentTimeLayout(false);
                progressLayout.setMargins(0, 0, 0, 0);
                progressLayout.addRule(RelativeLayout.ABOVE, bottomSelectionParentLL.getId());
                // mProgress.setLayoutParams(progressLayout);
                if(!APIConstants.IS_PLAYER_SCREEN_LOCKED) {
                    if (miniCardVideoPlayer != null && !miniCardVideoPlayer.mIsAdPlaying) {
                        mCurrentTime.setVisibility(VISIBLE);
                        mCurrentTime.setText(stringForTime(position) + "/" + totalTime);
                    }
                }

            }
            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                YuppAnalytics.getInstance(mContext).setPlayerPosition(position);
                YuppAnalytics.getInstance(mContext).setTotalDuration(duration);
            }
        }

        return position;
    }

    private void showLiveSeekBarUI() {
        if (mEndTime != null) {
            mEndTime.setText("Live");
            mEndTime.setVisibility(GONE);
        }

        if (!APIConstants.IS_PLAYER_SCREEN_LOCKED) {
            if (mCurrentTime != null && miniCardVideoPlayer != null && !miniCardVideoPlayer.mIsAdPlaying) {
                mCurrentTime.setVisibility(VISIBLE);
                if (miniCardVideoPlayer != null && !miniCardVideoPlayer.mIsAdPlaying) {
                    mCurrentTime.setVisibility(VISIBLE);
                    if (miniCardVideoPlayer != null && miniCardVideoPlayer.mData.startDate != null && miniCardVideoPlayer.mData.endDate != null) {
                        long startDate = 0;
                        try {
                            startDate = Util.parseXsDateTime(miniCardVideoPlayer.mData.startDate);
                            EPGUtil.getShortTime(startDate);
                            Log.e("presentTime","startDate"+stringForTime((int)startDate));
                            long endDate = Util.parseXsDateTime(miniCardVideoPlayer.mData.endDate);
                            Log.e("presentTime","endDate"+stringForTime((int)endDate));
                            EPGUtil.getShortTime(endDate);
                            String totalDuration = String.valueOf(endDate - startDate);
                            Log.e("presentTime","totalDuration"+stringForTime(Integer.parseInt(totalDuration)));
                            Calendar calendar = Calendar.getInstance();
                            long time = calendar.getTimeInMillis();
                            Log.e("presentTime","time"+time);
                            String presentRunningTime = String.valueOf(time - startDate);

                            Log.e("presentTime","PresentReunningTime"+stringForTime(Integer.parseInt(presentRunningTime)));

                            if(((time-startDate))>=(endDate - startDate)){
                                presentRunningTime=String.valueOf(0);
                                totalDuration=String.valueOf(1000);
                                mCurrentTime.setText(stringForTime(Integer.parseInt(presentRunningTime)) + "/" + stringForTime(Integer.parseInt(totalDuration)));
                                if(miniCardVideoPlayer.mData!=null &&miniCardVideoPlayer.mData.globalServiceId!=null && !miniCardVideoPlayer.isFromCatchup) {
                                    getEPGData(miniCardVideoPlayer.mData.globalServiceId);
                                    if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                                        YuppExoAnalyticsInterface.getInstance(mContext).cleanup();
                                        YuppAnalytics.getInstance(mContext).releaseUSAnalytics();
                                    }
                                }

                            }
                            mCurrentTime.setText(stringForTime(Integer.parseInt(presentRunningTime)) + "/" + stringForTime(Integer.parseInt(totalDuration)));
                            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                                YuppAnalytics.getInstance(mContext).setPlayerPosition((time - startDate));
                                YuppAnalytics.getInstance(mContext).setTotalDuration((endDate - startDate));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (mProgress != null) {
            mProgress.setProgress(1000);
            mProgress.setEnabled(false);
        }
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
//                            playPreviousNextEpisode(cardDataList.get(0), false);
                            if (((MainActivity) mContext).mFragmentCardDetailsPlayer != null) {
//                                ((MainActivity) mContext).mFragmentCardDetailsPlayer.mFragmentCardDetailsDescription.onDataLoaded(cardDataList.get(0),cardDataList.get(0).generalInfo.type , true);
                                ((MainActivity) mContext).mFragmentCardDetailsPlayer.onUpdatePlayerData(cardDataList.get(0));
                            }
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

    public GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (isFullScreen && !mIsLocked) {
                if (((MyplexVideoViewPlayer) mPlayer) != null) {
                    ((MyplexVideoViewPlayer) mPlayer).onTapToZoom();
                }
            }
            return super.onDoubleTap(e);
        }

    });

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mShowing) {
                hide();
                return true;
            } else {
                show(sDefaultTimeout);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        LoggerD.debugHooqVstbLog("dispatchKeyEvent>updatePlayPause + " + keyCode);
        if (mPlayer == null) {
            return false;
        }
        if (event.getRepeatCount() == 0 && (
                keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                        keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                        keyCode == KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            show(sDefaultTimeout);
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePlayPause();
            }
            updatePlayerState(PlayerListener.STATE_STOP, 0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            hide();

            return true;
        } else {
            show(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean isManualPlayPause;
    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doManualPlayPause();
        }
    };

    public void doManualPlayPause() {
        doPauseResume();
        show(sDefaultTimeout);
        isManualPlayPause = true;
        if (mPlayer.isPlaying()) {
            if(miniCardVideoPlayer.mProgressBarLayout.getVisibility() == VISIBLE)
                miniCardVideoPlayer.mProgressBarLayout.setVisibility(GONE);
            if (!isFullScreen) {
                if (mPauseButtonImageCenter != null) {
                    mPauseButtonImageCenter.setImageResource(R.drawable.icon_pause_portrait);
//                    mPauseButtonImageCenter.getDrawable().setAlpha(255);

//                    mPauseButtonImageCenter.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.LIGHTEN);
                }
                mPauseButtonImage.setImageResource(R.drawable.icon_pause_portrait);
//                mPauseButtonImage.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.LIGHTEN);
            } else {
                if (mPauseButtonImageCenter != null) {
                    mPauseButtonImageCenter.setImageResource(R.drawable.icon_pause_portrait);
                }
                mPauseButtonImage.setImageResource(R.drawable.icon_pause_portrait);
            }
            if (miniCardVideoPlayer != null && miniCardVideoPlayer.playPauseBtn != null) {
                miniCardVideoPlayer.playPauseBtn.setImageResource(R.drawable.ic_dock_pause);
            }
        } else {
            if (!isFullScreen) {
                if (mPauseButtonImageCenter != null) {
                    mPauseButtonImageCenter.setImageResource(R.drawable.icon_play_portrait);
                }
                mPauseButtonImage.setImageResource(R.drawable.icon_play_portrait);
            } else {
                if (mPauseButtonImageCenter != null) {
                    mPauseButtonImageCenter.setImageResource(R.drawable.icon_play_portrait);
                }
                mPauseButtonImage.setImageResource(R.drawable.icon_play_portrait);
            }
            if (miniCardVideoPlayer != null && miniCardVideoPlayer.playPauseBtn != null) {
                miniCardVideoPlayer.playPauseBtn.setImageResource(R.drawable.ic_dock_play);
            }
        }
    }

    public void updatePlayPause() {
        LoggerD.debugHooqVstbLog("updatePlayPause");
        if (mRoot == null
                || mPauseButton == null
                || mPauseButtonImage == null
                || mPlayer == null
                || !isEnabled())
            return;
        LoggerD.debugHooqVstbLog("updatePlayPause: " + mPlayer.isPlaying());
        setEnabled(true);
        if (mPlayer.isPlaying()) {
            if(miniCardVideoPlayer.mProgressBarLayout.getVisibility() == VISIBLE)
                miniCardVideoPlayer.mProgressBarLayout.setVisibility(GONE);
            if (!isFullScreen) {
                if (mPauseButtonImageCenter != null) {
                    mPauseButtonImageCenter.setImageResource(R.drawable.icon_pause_portrait);
//                    mPauseButtonImageCenter.getDrawable().setAlpha(255);

//                    mPauseButtonImageCenter.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.LIGHTEN);
                }
                mPauseButtonImage.setImageResource(R.drawable.icon_pause_portrait);
//                mPauseButtonImage.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.LIGHTEN);
            } else {
                if (mPauseButtonImageCenter != null) {
                    mPauseButtonImageCenter.setImageResource(R.drawable.icon_pause_portrait);
                }
                mPauseButtonImage.setImageResource(R.drawable.icon_pause_portrait);
            }
            if (miniCardVideoPlayer != null && miniCardVideoPlayer.playPauseBtn != null) {
                miniCardVideoPlayer.playPauseBtn.setImageResource(R.drawable.ic_dock_pause);
            }
        } else {
            if (!isFullScreen) {
                if (mPauseButtonImageCenter != null) {
                    //   mPauseButtonImageCenter.setImageResource(R.drawable.icon_play_portrait);
                }
                // mPauseButtonImage.setImageResource(R.drawable.icon_play_portrait);
            } else {
                if (mPauseButtonImageCenter != null) {
                    //   mPauseButtonImageCenter.setImageResource(R.drawable.icon_play_portrait);
                }
                // mPauseButtonImage.setImageResource(R.drawable.icon_play_portrait);
            }
            if (miniCardVideoPlayer != null && miniCardVideoPlayer.playPauseBtn != null) {
                miniCardVideoPlayer.playPauseBtn.setImageResource(R.drawable.ic_dock_play);
            }
        }
    }

    public void doShowHideControl() {
        if (mShowing) {
            LoggerD.debugHooqVstbLog("doShowHideControl>hide:");
            hide();
        } else {
            LoggerD.debugHooqVstbLog("doShowHideControl>show:");
            show();
        }
    }

    private long totalTime = 0;
    private long startTime = 0;

    public void doPauseResume() {
        if (mPlayer == null) {
            return;
        }
        isManualPlayPause = false;
        if (mPlayer.isPlaying()) {
            MOUTracker.pauseCount += 1;
            mPlayer.pause();
            updatePlayerState(PlayerListener.STATE_PAUSED, mPlayer.getCurrentPosition());
            startTime = DateHelper.getInstance().getCurrentLocalTime();
        } else {
            MOUTracker.playCount += 1;
            if (DateHelper.getInstance().getCurrentLocalTime() - startTime > 10000 && isLive) {
                mPlayer.seekTo(mPlayer.getDuration() - 5000);
                setProgress();
            /*    mPauseButtonImageCenter.setVisibility(View.INVISIBLE);
                show(sDefaultTimeout);*/
                mPlayer.start();
                updatePlayerState(PlayerListener.STATE_PLAYING, mPlayer.getCurrentPosition());
            } else {
                mPlayer.start();
                updatePlayerState(PlayerListener.STATE_PLAYING, mPlayer.getCurrentPosition());
            }
        }
        LoggerD.debugHooqVstbLog("doPauseResume>updatePlayPause");
        updatePlayPause();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        long startposition;
        long newposition;
        int startSeekTime;
        int endSeekTime;

        public void onStartTrackingTouch(SeekBar bar) {
            show(sDefaultTimeout);
            //  mFfwdButtonContainer.setVisibility(INVISIBLE);
            //   mRewindButtonContainer.setVisibility(INVISIBLE);
            mPauseButtonImageCenter.setVisibility(INVISIBLE);

            mDragging = true;
            LoggerD.debugLog("mDragging- " + mDragging);

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
            int endProgress = bar.getProgress();
            long endDuration = mPlayer.getDuration();
            startposition=(endDuration * endProgress) / 1000L;
            if (mPlayerListener != null) {
                mPlayerListener.onSeekComplete(mMediaPlayer, true);
            }
            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                YuppAnalytics.getInstance(mContext).handleSeekStart(mPlayer.getCurrentPosition());
            }
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
//            if (!fromuser) {
//                 We're not interested in programmatically generated changes to
//                 the progress bar's position.
//                return true;
//            }
            if (mPlayer == null) {
                return;
            }
            long duration = mPlayer.getDuration();
            String endTime = mEndTime.getText().toString();
            if (isNonDVRLive) {
                //(Build.VERSION.SDK_INT==26){
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!(miniCardVideoPlayer!=null && miniCardVideoPlayer.mData!=null && miniCardVideoPlayer.mData.generalInfo!=null && miniCardVideoPlayer.mData.generalInfo.isDvr)) {
                            showLiveSeekBarUI();
                        }
                    }
                }, 200);

                /*}else {
                    showLiveSeekBarUI();
                }*/
                long newPosition = (long) ((duration * progress * 1.0) / 1000);
                String time = Util.generateTime(newPosition);
                if (mCurrentTime != null && newPosition > 0)
                    mCurrentTime.setText(stringForTime((int) newPosition) + "/" + endTime);

                if(newPosition>(duration-15000)){
                    Log.e("TimeSift","OnProgressChanged"+"Forward");
                    go_live_button.setVisibility(GONE);
                }else{
                    Log.e("TimeSift","OnProgressChanged"+"Backward");
                    go_live_button.setVisibility(VISIBLE);
                }

            } else {

                if (fromuser && showPreviewSeekBar) {
                    //mFfwdButtonContainer.setVisibility(INVISIBLE);
                    //mRewindButtonContainer.setVisibility(INVISIBLE);
                    mPauseButtonImageCenter.setVisibility(INVISIBLE);
                    previewFrameLayout.setVisibility(VISIBLE);
                } else {
                    //commented not required this fast forward for sun direct
                    //  mFfwdButtonContainer.setVisibility(VISIBLE);
                    //commented not required this rewind  for sun direct
                    //  mRewindButtonContainer.setVisibility(VISIBLE);
                    mPauseButtonImageCenter.setVisibility(VISIBLE);
                    previewFrameLayout.setVisibility(INVISIBLE);
                }
                long newPosition = (long) ((duration * progress * 1.0) / 1000);
                String time = Util.generateTime(newPosition);
                if (mCurrentTime != null && newPosition > 0)
                    mCurrentTime.setText(stringForTime((int) newPosition) + "/" + endTime);
            }
/*            // cause the progress bar to be updated even if mShowing
            // was already true.  This happens, for example, if we're
            // paused with the progress bar showing the user hits play.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);

            Message msg = mHandler.obtainMessage(FADE_OUT);
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, sDefaultTimeout);*/
            if (mDragging) {
                show(sDefaultTimeout);
            }

        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            if (mPlayer == null) {
                return;
            }
//            setProgress();
//            updatePlayPause();
            if (isNonDVRLive && !(miniCardVideoPlayer!=null && miniCardVideoPlayer.mData!=null && miniCardVideoPlayer.mData.generalInfo!=null && miniCardVideoPlayer.mData.generalInfo.isDvr)) {
                showLiveSeekBarUI();
            } else {
            //            setProgress();
//            updatePausePlay();
                show(sDefaultTimeout);

//             Ensure that progress is properly updated in the future,
//             the call to show() does not guarantee this because it is a
//             no-op if we are already showing.
                mHandler.sendEmptyMessage(SHOW_PROGRESS);

                int progress = bar.getProgress();

                int endProgress = bar.getProgress();
                long endDuration = mPlayer.getDuration();
                newposition = (endDuration * endProgress) / 1000L;
                if (newposition >= endDuration) {
                    newposition = endDuration - 5000;
                }
                mPlayer.seekTo((int) newposition);

                endSeekTime = Util.generateTimeInSec(newposition);

                if (startSeekTime < endSeekTime) {
                    MOUTracker.backwardTime += Math.abs(startSeekTime - endSeekTime);
                } else if (startSeekTime > endSeekTime) {
                    MOUTracker.forwardTime += Math.abs(startSeekTime - endSeekTime);
                }

                if ((int) newposition > startposition) {
                    MOUTracker.forwardCount += 1;
                } else if ((int) newposition < startposition) {
                    MOUTracker.backwardCount += 1;
                }

                setEnabled(false);
                LoggerD.debugLog("disable controlls");
                if (mCurrentTime != null)
                    mCurrentTime.setText(stringForTime((int) newposition) + "/" + totalTime);

                if (mPlayerListener != null) {
                    mPlayerListener.onSeekComplete(mMediaPlayer, false);
                }
                previewFrameLayout.setVisibility(INVISIBLE);
                //commented not required this fast forward  for sun direct
                // mFfwdButtonContainer.setVisibility(VISIBLE);
                //commented not required this rewind  for sun direct
                // mRewindButtonContainer.setVisibility(VISIBLE);
                mPauseButtonImageCenter.setVisibility(VISIBLE);
            }
            if(PrefUtils.getInstance().getIsEnabledVideoAnalytics()) {
                YuppAnalytics.getInstance(mContext).handleSeekEnd(mPlayer.getCurrentPosition(), startposition, true);
            }
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        LoggerD.debugLog("enable controls- " + enabled);
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }

        if (mPauseButtonImageCenter != null) {
            mPauseButtonImageCenter.setEnabled(enabled);
        }

        if (mProgress != null) {
            mProgress.setEnabled(true);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private OnClickListener mRewListener = new OnClickListener() {
        public void onClick(View v) {
            LoggerD.debugLog("rewind to -" + playerSeekTimeSeconds);
            if (!PrefUtils.getInstance().getSeekBarEnable()) {
                return;
            }
            if (mPlayer == null) {
                LoggerD.debugLog("player is null");
                return;
            }
            int pos = mPlayer.getCurrentPosition();
            pos -= playerSeekTimeSeconds * 1000; // milliseconds
            if (pos < 0) {
                return;
            }
            mPlayer.seekTo(pos);
            setProgress();
            mPauseButtonImageCenter.setVisibility(View.VISIBLE);
            show(sDefaultTimeout);
        }
    };

    private int playerSeekTimeSeconds = 10;
    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View v) {
            if (!PrefUtils.getInstance().getSeekBarEnable()) {
                return;
            }
            LoggerD.debugLog("fowrad to +" + playerSeekTimeSeconds);
            if (mPlayer == null) {
                LoggerD.debugLog("player is null");
                return;
            }
            int pos = mPlayer.getCurrentPosition();
            int duration = mPlayer.getDuration();
            if (pos >= duration) {
                return;
            }
            pos += playerSeekTimeSeconds * 1000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();
            mPauseButtonImageCenter.setVisibility(View.VISIBLE);
            show(sDefaultTimeout);
        }
    };

    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        mListenersSet = true;

        if (mRoot != null) {
            installPrevNextListeners();

            if (mNextButton != null && !mFromXml) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public long setProgress(long position, long duration) {
        LoggerD.debugLog("mDragging- " + mDragging);
        if (mPlayer == null || mDragging) {
            return 0;
        }
//        int position = mPlayer.getCurrentPosition();
//        int duration = mPlayer.getDuration();
        if (isNonDVRLive && !(miniCardVideoPlayer!=null && miniCardVideoPlayer.mData!=null && miniCardVideoPlayer.mData.generalInfo!=null && miniCardVideoPlayer.mData.generalInfo.isDvr)) {
            showLiveSeekBarUI();
            return 0;
        }
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime((int) duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime((int) position) + "/" + totalTime);

        mHandler.removeMessages(SHOW_PROGRESS);
        return position;
    }

    public void setPlayerHeaderView(View playerHeaderView) {
        this.mPlayerHeaderView = playerHeaderView;
        /*if (mSubtitleSettingsButtton != null) {
            mSubtitleSettingsButtton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSubtitles();
                }
            });
            //enableSubtitles(false);
        }*/
    }

    private void showSubtitles() {
        if (mPlayer == null || !(mPlayer instanceof MyplexVideoViewPlayer)) return;
        showSubtitlePopup();
    }

    private void showSubtitlePopup() {
        LoggerD.debugLog("showSubtitlePopup");
        if (subtitlesDialog == null)
            subtitlesDialog = new SubtitlesDialog(mContext, mPlayer, new SubtitlesDialog.SubTitleSelectionListener() {
                @Override
                public void onSubtitleChanged(String subtitleName) {
                    LoggerD.debugLog("subtitleName- " + subtitleName);
                    if (mPlayer != null) {
                        ((MyplexVideoViewPlayer) mPlayer).setSubtitle(subtitleName);
                    }
                    if (mPlayerListener != null)
                        mPlayerListener.onSubtitleChanged(subtitleName);
                }
            });
        subtitlesDialog.show();
    }


    private void dismissSubtitles() {
        if (subtitlesDialog == null) {
            return;
        }
        subtitlesDialog.dismiss();
    }

    public String getSubTitleName() {
        if (subtitlesDialog == null) return null;
        return subtitlesDialog.getSubtitleName();
    }

    private void initializePlaybackControlls() {
        settingsContentObserver = new SettingsContentObserver(mContext, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.
                CONTENT_URI, true, settingsContentObserver);
        if (mPlayerControllsView == null) return;

        try {
            if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefPlayerSeekTimeSeconds()))
                playerSeekTimeSeconds = Integer.parseInt(PrefUtils.getInstance().getPrefPlayerSeekTimeSeconds());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mFfwdButtonContainer = (View) mPlayerControllsView.findViewById(R.id.fforward_container);
        mFfwdButton = (ImageView) mPlayerControllsView.findViewById(R.id.media_player_fowrard_icon);
        mFfwdText = (TextView) mPlayerControllsView.findViewById(R.id.media_player_ffword_text);
        mFfwdText.setText("+" + String.valueOf(playerSeekTimeSeconds));

        mFfwdText.setVisibility(GONE);

        mFfwdButton.setOnClickListener(mFfwdListener);
        mFfwdButtonContainer.setOnClickListener(mFfwdListener);

        mRewindButtonContainer = (View) mPlayerControllsView.findViewById(R.id.rewind_container);
        mRewButton = (ImageView) mPlayerControllsView.findViewById(R.id.media_player_rewind_icon);
        mRewindText = (TextView) mPlayerControllsView.findViewById(R.id.media_player_rewind_text);
        mRewindText.setVisibility(GONE);
        mRewindText.setText("-" + String.valueOf(playerSeekTimeSeconds));
        mRewindButtonContainer.setOnClickListener(mRewListener);
        mRewButton.setOnClickListener(mRewListener);
        go_live_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPlayer != null && (mPlayer.getDuration() > 0) /*&& go_live_button.getVisibility() != VISIBLE*/) {
                    mPlayer.seekTo(mPlayer.getDuration() - 5000);
                }
            }
        });
        Log.d(TAG, "initializePlaybackControlls: isNonDVRLive => "+ isNonDVRLive + " isFullScreen => "+ isFullScreen);

        mPauseButtonImageCenter = (ImageView) mPlayerControllsView.findViewById(R.id.media_player_play_pause_icon);
        this.layoutParamsOriginal = (RelativeLayout.LayoutParams) mPauseButtonImageCenter.getLayoutParams();
        mPauseButtonImageCenter.setOnClickListener(mPauseListener);


        if(isLive){
            mFfwdButton.setVisibility(INVISIBLE);
            mRewButton.setVisibility(INVISIBLE);
        }else {
            mFfwdButton.setVisibility(VISIBLE);
            mRewButton.setVisibility(VISIBLE);
        }


    }

    public void isToolbarShown(boolean isToShowToolbar) {
        this.isToolbarShown = isToShowToolbar;
    }

    public void enableTrackSelector(boolean b) {
        if (!b) {
//                mRoot.findViewById(R.id.playerquality).setVisibility(GONE);
            mRoot.findViewById(R.id.playerquality).setAlpha(0.4f);
            mRoot.findViewById(R.id.playerquality).setEnabled(false);
        } else {
//                mRoot.findViewById(R.id.playerquality).setVisibility(VISIBLE);
            mRoot.findViewById(R.id.playerquality).setAlpha(1f);
            mRoot.findViewById(R.id.playerquality).setEnabled(true);
        }
    }

    public void enableSubtitles(boolean b) {
        SDKLogger.debug("VFPLAY: isLive- " + isLive + " isDVR- " + isDVR);
        if (isLive || isDVR) {
            return;
        }

        if (!b) {
            isSubtitleAvailable = false;
//                mRoot.findViewById(R.id.playerquality).setVisibility(GONE);
//            mSubtitleSettingsButtton.setAlpha(0.4f);
//            mSubtitleSettingsButtton.setEnabled(false);
//            mSubtitleSettingsButtton.setVisibility(GONE);
        } else {
//                mRoot.findViewById(R.id.playerquality).setVisibility(VISIBLE);
//            mSubtitleSettingsButtton.setAlpha(1f);
//            mSubtitleSettingsButtton.setEnabled(true);
//            mSubtitleSettingsButtton.setVisibility(VISIBLE);
            isSubtitleAvailable = true;
        }
        updateAudioSubtitleVisibility();
    }

    public void hideTrackSelectorButton() {
        mRoot.findViewById(R.id.playerquality).setVisibility(VISIBLE);
//            mRoot.findViewById(R.id.playerquality).setEnabled(false);
    }

    public void destroyMediaPlayer() {
        mPlayer = null;
    }

    public void setPlayerControllsView(View mPlayerControllsView) {
        this.mPlayerControllsView = mPlayerControllsView;
        initializePlaybackControlls();
    }

    public void setMediaControllsView(View mPlayerMediaControllsView) {
        this.mPlayerMediaControllsView = mPlayerMediaControllsView;
        mRoot = this.mPlayerMediaControllsView;
        initControllerView(this.mPlayerMediaControllsView);
    }

    public void setDVR(boolean DVR) {
        isDVR = DVR;
    }

    public void setPreviewSeekBar(long currentPosition) {
        if (webVTTModule != null && webVTTModule.isReady()) {
            webVTTModule.updateToStartInTime(currentPosition);
            WebVTTData webVTTData = webVTTModule.getWebVTTData();
            if (webVTTData == null) {
                return;
            }
            int x = 0, y = 0, width = 0, height = 0;
            String previewImageCoordinateCrudeData = webVTTData.getText();
            String cacheKey = webVTTData.getCacheKey();
            /*previewImageCoordinateCrudeData ="84005.mp_sprite.jpg#xywh=0,56,100,56";*/
            String[] getFileName;
            try {
                getFileName = previewImageCoordinateCrudeData.split("#");
                if (!(getFileName != null && getFileName.length > 1 && !TextUtils.isEmpty(getFileName[0]))) {
                    return;
                }
                String[] equalToSplit = previewImageCoordinateCrudeData.split("=");
                if (equalToSplit != null && equalToSplit.length > 1) {
                    String[] xyCoordinates = equalToSplit[1].split(",");
                    if (xyCoordinates != null && xyCoordinates.length > 1) {
                        x = (int) Integer.parseInt(xyCoordinates[0]);
                        y = (int) Integer.parseInt(xyCoordinates[1]);
                        width = (int) Integer.parseInt(xyCoordinates[2]);
                        height = (int) Integer.parseInt(xyCoordinates[3]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            int finalX = x;
            int finalY = y;
            int finalWidth = width;
            int finalHeight = height;
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File(ApplicationController.getAppContext().getFilesDir() + "/" + getFileName[0]);
                        // PicassoUtil.with(mContext).load(file, imageViewSeekBar,new PicassoTransformation(finalX, finalY, finalWidth, finalHeight,cacheKey));
                        Picasso.get().load(file)
                                .error(new ColorDrawable(Color.parseColor("#4d000000")))
                                .placeholder(new ColorDrawable(Color.parseColor("#4d000000")))
                                .transform(new PicassoTransformation(finalX, finalY, finalWidth, finalHeight, cacheKey))
                                .into(imageViewSeekBar);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } else {
            previewFrameLayout.setVisibility(INVISIBLE);
        }

    }

    public void setWebVTTModule(WebVTTModule webVttData) {
        this.webVTTModule = webVttData;
        int i = 0;


        while (i < webVTTModule.subtitles.size()) {
            System.out.println(webVTTModule.subtitles.get(i));
            WebVTTData webVTTData = webVTTModule.subtitles.get(i);

            String previewImageCoordinateCrudeData = webVTTData.getText();
            String[] getFileName;
            int x = 1, y = 1, width = 1, height = 1;
            String cacheKey = null;
            try {
                getFileName = previewImageCoordinateCrudeData.split("#");
                if (!(getFileName != null && getFileName.length > 1 && !TextUtils.isEmpty(getFileName[0]))) {
                    return;
                }
                cacheKey = webVTTData.getCacheKey();
                String[] equalToSplit = previewImageCoordinateCrudeData.split("=");
                if (equalToSplit != null && equalToSplit.length > 1) {
                    String[] xyCoordinates = equalToSplit[1].split(",");
                    if (xyCoordinates != null && xyCoordinates.length > 1) {
                        x = (int) Integer.parseInt(xyCoordinates[0]);
                        y = (int) Integer.parseInt(xyCoordinates[1]);
                        width = (int) Integer.parseInt(xyCoordinates[2]);
                        height = (int) Integer.parseInt(xyCoordinates[3]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            int finalX = x;
            int finalY = y;
            int finalWidth = width;
            int finalHeight = height;
            String finalCacheKey = cacheKey;
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File(ApplicationController.getAppContext().getFilesDir() + "/" + getFileName[0]);
                        // PicassoUtil.with(mContext).load(file, imageViewSeekBar,new PicassoTransformation(finalX, finalY, finalWidth, finalHeight, finalCacheKey));
                        Picasso.get().load(file)
                                .error(new ColorDrawable(Color.parseColor("#4d000000")))
                                .placeholder(new ColorDrawable(Color.parseColor("#4d000000")))
                                .transform(new PicassoTransformation(finalX, finalY, finalWidth, finalHeight, finalCacheKey))
                                .into(imageViewSeekBar);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            i++;
        }

    }

    public void setSkipIntroTimetoJump(int timetoJump) {
        jumpToSecs = timetoJump;
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();
    }

    /**
     * @param state This method is the callback method for the player state.
     */
    public void updatePlayerState(int state, int position) {
        if (mPlayerListener != null && mPlayer != null) {
            int pos = mPlayer.getCurrentPosition() / 1000;
            mPlayerListener.onStateChanged(state, pos);
        }

    }

    public void resumePlay(int ellapseTime) {
        long pos = 1000L * ellapseTime;
//         mProgress.setProgress( (int) pos);
        mPlayer.seekTo((int) pos);
    }

    public void setFullScreenTooggle(int visibility) {
        if (mFullScreenTooggle != null) {
            //mFullScreenTooggle.setVisibility(visibility);
        }
    }

    public void showFeedback(View v) {
        if (v == null) {
            return;
        }
        v.setOnTouchListener((v1, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // v1.setBackgroundColor(mContext.getResources().getColor(R.color.red_highlight_color));
                    break;
                default:
                    v1.setBackgroundColor(Color.TRANSPARENT);
                    break;
            }
            return false;
        });
    }


    private final AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager
            .OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
           /* //Log.d(TAG, "Received audio focus call back focusChange- "
                    + (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ? "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT"
                    : (focusChange == AudioManager.AUDIOFOCUS_GAIN) ? "AudioManager.AUDIOFOCUS_GAIN"
                    : (focusChange == AudioManager.AUDIOFOCUS_LOSS) ? "AudioManager.AUDIOFOCUS_LOSS" : focusChange + " UnKnown"));*/
            if (null == mPlayer) return;
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
                if (!mPlayer.isPlaying() && !isManualPlayPause) {
                    mPlayer.start();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                if (!mPlayer.isPlaying()) {
                    mAudioManager.abandonAudioFocus(afChangeListener);
                }
            }
        }
    };

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        LoggerD.debugHooqVstbLog("changedView- " + changedView.getTag() + " id " + changedView.getId() + " visibility- " + visibility);
    }

    public int getDuration() {
        if (mPlayer == null) {
            return -1;
        }
        return mPlayer.getDuration();
    }

    public void togglePlaybackOptimization(boolean show) {
        if (show) {
            mRoot.findViewById(R.id.live_hint_container).setVisibility(VISIBLE);
            return;
        }
        mRoot.findViewById(R.id.live_hint_container).setVisibility(GONE);
    }

    int selectedPosition = PrefUtils.getInstance().selectedQuality;

    public void setUserChoiceQuality() {

        try {
            String userSelectedQuality;
            if (isPausedWhiilePlaying) {
                switch (selectedPosition) {
                    case 1:
                        userSelectedQuality = "low";
                        break;
                    case 2:
                        userSelectedQuality = "medium";
                        break;
                    case 3:
                        userSelectedQuality = "high";
                        break;
                    case 0:
                        userSelectedQuality = "auto";
                        break;
                    default:
                        userSelectedQuality = "auto";
                        break;
                }

            } else {
                userSelectedQuality = PrefUtils.getInstance().getPrefPlayBackQuality();
            }
            if (userSelectedQuality!=null && userSelectedQuality.equalsIgnoreCase("high")) {
                userSelectedQuality = VIDEO_QUALITY_HD;
            }
            switch (userSelectedQuality) {
                case VIDEO_QUALITY_LOW:
                    selectedPosition = 1;
                    PrefUtils.getInstance().selectedQuality = 1;
                    break;
                case VIDEO_QUALITY_MEDIUM:
                    selectedPosition = 2;
                    PrefUtils.getInstance().selectedQuality = 2;
                    break;
                case VIDEO_QUALITY_HD:
                    selectedPosition = 3;
                    PrefUtils.getInstance().selectedQuality = 3;
                    break;
                case VIDEO_QUALITY_AUTO:
                default:
                    selectedPosition = 0;
                    PrefUtils.getInstance().selectedQuality = 0;
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setText(String quality) {
        TextView qualityView = mRoot.findViewById(R.id.playerquality);
        qualityView.setText(quality);
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

    public interface AudioTrackListner {
        public void getSelectedItem(int audio);
    }

    public interface QualitySelection {
        public void getSelectedItem(String name);
    }
    public class SettingsContentObserver extends ContentObserver {
        float previousVolume = 0;
        Context context;
        SettingsContentObserver(Context c, Handler handler) {
            super(handler);
            context = c;
            AudioManager am = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
            previousVolume = ((double) am.getStreamVolume(AudioManager.STREAM_MUSIC)) / 10 > 1.0 ? 1 : (float) (((double) am.getStreamVolume(AudioManager.STREAM_MUSIC)) / 10);
        }
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            try {
                AudioManager audio = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                float currentVolume = ((double) audio.getStreamVolume(AudioManager.STREAM_MUSIC)) / 10 > 1.0 ? 1 : (float) (((double) audio.getStreamVolume(AudioManager.STREAM_MUSIC)) / 10);

                if (currentVolume > 0) {
                    mMuteButtonImage.setImageResource(R.drawable.icon_unmute_new);
                    mMuteEnabled = false;
                } else if (currentVolume <= 0) {
                    mMuteButtonImage.setImageResource(R.drawable.icon_mute_new);
                    mMuteEnabled = true;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void updateAudioSubtitleVisibility() {
        if(isAudioAvailable || isSubtitleAvailable){
            audioLanguageLinear.setVisibility(VISIBLE);
        }else {
            audioLanguageLinear.setVisibility(VISIBLE);
        }
    }


    BottomSheetDialog bottomSheetSubtitleAudioDialog;

    //isSubtitleAvailable - is subtitle option available or not
    //isAudioAvailable- is multiple audio option available or not
    private void showAudioSubtitleBottomSheet(){
        /*if (mPlayer.isPlaying())
            doManualPlayPause();*/
      /*  if(bottomSheetSubtitleAudioDialog != null){
            bottomSheetSubtitleAudioDialog.show();
        }else{*/
            bottomSheetSubtitleAudioDialog = new BottomSheetDialog(mContext, R.style.NoBackgroundDialogThemePlayerOption);
            View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.bottom_sheet_player_options, null);

            bottomSheetSubtitleAudioDialog.setContentView(view);
            RecyclerView rv_subtitles = view.findViewById(R.id.rv_subtitles);
            TextView tv_no_subtitle = view.findViewById(R.id.tv_no_subtitle);
            RecyclerView rv_audio = view.findViewById(R.id.rv_audio);
            TextView tv_no_audio = view.findViewById(R.id.tv_no_audio);
            ImageView iv_close = view.findViewById(R.id.iv_close);
//        rv_subtitles.setHasFixedSize(true);
//        rv_audio.setHasFixedSize(true);

            VerticalSpaceItemDecoration mHorizontalMoviesDivieder = new VerticalSpaceItemDecoration((int) mContext
                    .getResources().getDimension(R.dimen.margin_gap_2));

            rv_subtitles.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            rv_audio.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            rv_audio.removeItemDecoration(mHorizontalMoviesDivieder);
            rv_subtitles.removeItemDecoration(mHorizontalMoviesDivieder);
            rv_audio.addItemDecoration(mHorizontalMoviesDivieder);
            rv_subtitles.addItemDecoration(mHorizontalMoviesDivieder);
            rv_audio.setItemAnimator(null);
            rv_subtitles.setItemAnimator(null);
            if (isSubtitleAvailable) {
                rv_subtitles.setVisibility(VISIBLE);
                tv_no_subtitle.setVisibility(GONE);

                if(cardDataPlayerOptionSubtitle.size() == 0){
                    MyplexVideoViewPlayer myplexVideoViewPlayer = (MyplexVideoViewPlayer) mPlayer;

                    CardDataSubtitles cardDataSubtitles = myplexVideoViewPlayer.getSubtitles();
                    List<CardDataSubtitleItem> cardDataSubtitleItem = cardDataSubtitles.values;

                    for(CardDataSubtitleItem item : cardDataSubtitleItem){
                        CardDataPlayerOptionItem cardDataPlayerOptionItem = new CardDataPlayerOptionItem();
                        cardDataPlayerOptionItem.setName(item.language);
                        cardDataPlayerOptionItem.setType(AdapterPlayerOption.SUBTITLE);
                        cardDataPlayerOptionSubtitle.add(cardDataPlayerOptionItem);
                    }

                    CardDataPlayerOptionItem cardDataPlayerOptionItem = new CardDataPlayerOptionItem();
                    cardDataPlayerOptionItem.setName("Off");
                    cardDataPlayerOptionItem.setType(AdapterPlayerOption.SUBTITLE);
                    subtitlePosition = 1;

                    cardDataPlayerOptionSubtitle.add(0, cardDataPlayerOptionItem);

                }

                adapterPlayerOptionSubtitle = new AdapterPlayerOption(mContext, cardDataPlayerOptionSubtitle, new AdapterPlayerOption.PlayerOptionListener() {
                    @Override
                    public void onClickItem(int position) {
                        if (cardDataPlayerOptionSubtitle != null && cardDataPlayerOptionSubtitle.size() > 0 && position>=0) {
                            String name = (position == 0) ? getResources().getString(R.string.subtitle_opt_none) : cardDataPlayerOptionSubtitle.get(position).getName();

                            if (mPlayer != null) {
                                ((MyplexVideoViewPlayer) mPlayer).setSubtitle(name);
                            }
                            if (mPlayerListener != null)
                                mPlayerListener.onSubtitleChanged(name);
                                subtitlePosition = position;
                                adapterPlayerOptionSubtitle.notifyDataSetChanged();
                        }
                    }
                }, subtitlePosition);
                rv_subtitles.setAdapter(adapterPlayerOptionSubtitle);


            }else {
                rv_subtitles.setVisibility(GONE);
                tv_no_subtitle.setVisibility(VISIBLE);
            }

            if (isAudioAvailable) {
                rv_audio.setVisibility(VISIBLE);
                tv_no_audio.setVisibility(GONE);
                cardDataPlayerOptionAudio.clear();

                    for(String item : mList){
                        CardDataPlayerOptionItem cardDataPlayerOptionItem = new CardDataPlayerOptionItem();
                        cardDataPlayerOptionItem.setName(item);
                        cardDataPlayerOptionItem.setType(AdapterPlayerOption.AUDIO);
                        cardDataPlayerOptionAudio.add(cardDataPlayerOptionItem);
                    }




                adapterPlayerOptionAudio = new AdapterPlayerOption(mContext, cardDataPlayerOptionAudio, new AdapterPlayerOption.PlayerOptionListener() {
                    @Override
                    public void onClickItem(int position) {
                        try {
                            if (mPlayer instanceof ExoPlayerView) {
                                if (mList != null && mList.size() > 1)
                                    ((ExoPlayerView) mPlayer).setAudioTrack(mList.get(position));
                                audioPosition = position;
                                adapterPlayerOptionAudio.notifyDataSetChanged();
                                isAudioTrackChanged=true;
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }, audioPosition);
                rv_audio.setAdapter(adapterPlayerOptionAudio);


            } else {
                rv_audio.setVisibility(GONE);
                tv_no_audio.setVisibility(VISIBLE);
            }

            iv_close.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetSubtitleAudioDialog.dismiss();
                }
            });

            bottomSheetSubtitleAudioDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                    setupFullHeight(bottomSheetDialog);
                }
            });
            bottomSheetSubtitleAudioDialog.show();
        }



 /*   }*/
    List<CardDataPlayerOptionItem> cardDataPlayerOptionAudio = new ArrayList<>();
    List<CardDataPlayerOptionItem> cardDataPlayerOptionSubtitle = new ArrayList<>();
    AdapterPlayerOption adapterPlayerOptionAudio;
    AdapterPlayerOption adapterPlayerOptionSubtitle;

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        if (behavior.isDraggable()) {

        }
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING)

                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}

