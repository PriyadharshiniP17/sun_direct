package com.myplex.myplex.ads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.media.PlayerListener;
import com.myplex.myplex.media.VideoViewPlayer;
import com.myplex.myplex.media.exoVideo.MyplexVideoViewPlayer;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.views.CardVideoPlayer;
import com.myplex.myplex.utils.MOUTracker;
import com.myplex.util.PrefUtils;


/**
 * A manager class responsible for communicating with Pulse SDK through implementation of PulseSessionListener.
 */
public class PulseManager extends CardVideoPlayer {
    private static final String TAG = PulseManager.class.getSimpleName();
//    private PulseSession pulseSession;
//    private PulseVideoAd currentPulseVideoAd;

    private Uri videoContentUri;
    private long currentContentProgress = 0;
    private boolean duringVideoContent = false;
    private boolean contentStarted = false;
    private Activity activity;
    public ClickThroughCallback clickThroughCallback;
    private float currentAdProgress = 0;

    public static Handler contentProgressHandler = new Handler();
    public static Handler playbackHandler = new Handler();
    private boolean duringAd = false;
    private boolean isAdPuasedWhenPlaying = false;


    /**
     * A runnable responsible for monitoring ad playback timeout.
     */
    public Runnable playbackRunnable = new Runnable() {
        @Override
        public void run() {
            // Timeout for ad playback is reached and it should be reported to Pulse SDK.
            //Log.i(TAG, "playbackRunnable: Time out for ad playback is reached");
            /*if (currentPulseVideoAd != null) {
                currentPulseVideoAd.adFailed(PulseAdError.REQUEST_TIMED_OUT);
            } else {
                throw new RuntimeException("currentPulseVideoAd is null");
            }*/
        }
    };

    /**
     * A runnable called periodically to keep track of the content/Ad playback's progress.
     */
    public Runnable onEveryTimeInterval = new Runnable() {
        @Override
        public void run() {
            //Time interval in milliseconds to check playback progress.
            int timeInterval = 200;
           /* if(pulseSession == null || mVideoViewPlayer == null){
                return;
            }*/
            contentProgressHandler.postDelayed(onEveryTimeInterval, timeInterval);
            
            if (duringVideoContent) {
                if (mVideoViewPlayer.getCurrentPosition() != 0) {
                    currentContentProgress = mVideoViewPlayer.getCurrentPosition();
                    /*//Log.d(TAG,"onEveryTimeInterval: currentContentProgress- " +
                            ""+currentContentProgress);*/
                  /*  if(pulseSession != null){
                        //Report content progress to Pulse SDK. This progress would be used to trigger ad break.
                        pulseSession.contentPositionChanged(currentContentProgress / 1000);
                    }*/

                }

            } else if (duringAd) {
                
                if (mVideoViewPlayer.getCurrentPosition() != 0) {
                    currentAdProgress = mVideoViewPlayer.getCurrentPosition();
                    /*//Log.d(TAG,"onEveryTimeInterval: currentAdProgress- " +
                            ""+currentAdProgress);*/
                    //Report ad video progress to Pulse SDK.
//                    if(currentPulseVideoAd != null)
//                        currentPulseVideoAd.adPositionChanged(currentAdProgress / 1000);

                }
            }
        }
    };


 /*   PulseSessionListener mPulseSessionListener = new PulseSessionListener() {
        /////////////////////PulseSessionListener methods////////////

        *//**
         * Pulse SDK calls this method when content should be played/resumed.
         */
       // @Override
        public void startContentPlayback() {
            playVideoContent();
        }

        /**
         * Pulse SDK calls this method to signal an AdBreak.
         */
       // @Override
        public void startAdBreak() {
            //Pause the content playback and remove the player listener.
            //Log.i(TAG, "startAdBreak: Ad break started.");
            contentProgressHandler.post(onEveryTimeInterval);
            isAdResumed = false;
            duringAd = false;
            duringVideoContent = false;
//            mVideoViewPlayer.setPositionWhenPaused(mVideoViewPlayer.getCurrentPosition());
            mVideoViewPlayer.hideMediaController();
            mVideoViewPlayer.setPlayerListener(mAdPlayerListener);
        }

        /**
         * Pulse SDK calls this method to signal the ad playback.
         *
         * @param pulseVideoAd The {@link PulseVideoAd} that should be displayed.
         * @param timeout      The timeout for displaying the ad.
         *//*
        @Override
        public void startAdPlayback(PulseVideoAd pulseVideoAd, float timeout) {
            //Log.i(TAG, "startAdPlayback: ");
            currentPulseVideoAd = pulseVideoAd;
            String adUri = selectAppropriateMediaFile(pulseVideoAd.getMediaFiles()).getURL().toString();
            playAdContent(timeout, adUri);
        }

        *//**
         * Pulse SDK calls method to signal session completion.
         *//*
        @Override
        public void sessionEnded() {
            //Log.i(TAG, "sessionEnded: Session ended");
            duringVideoContent = false;
            duringAd = false;
            currentContentProgress = 0;
            removeCallback(contentProgressHandler);
            if (activity != null) {
                activity.finish();
            }
            closePlayer();
            mVideoViewPlayer.closeSession();
        }

        *//**
         * Pulse SDK calls this method to inform an incorrect/out of order reported event.
         *
         * @param error The produced error due to incorrect event report.
         *//*
        @Override
        public void illegalOperationOccurred(com.ooyala.adtech.Error error) {
            // In debug mode a runtime exception would be thrown in order to find and
            // correct mistakes in the integration.
            if (BuildConfig.DEBUG) {
                throw new RuntimeException(error.getMessage());
            } else {
                // Don't know how to recover from this, stop the session and continue
                // with the content.
                pulseSession.stopSession();
                pulseSession = null;
                mVideoViewPlayer.closeSession();
                startContentPlayback();
            }
        }

    };*/

    protected PlayerListener mPlayerListener = new PlayerListener() {


        @Override
        public void onStateChanged(int state1 , int elapsedTime)
        {
            state = PlayerListener.STATE_PAUSED;
            currentDuration = elapsedTime;
            switch (state1) {
                case PlayerListener.STATE_PAUSED:
                    if(isLocalPlayback){
//				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
                    }
                    //Log.d(TAG, "paused" + elapsedTime);
                    break;
                case PlayerListener.STATE_PLAYING:
                    //Log.d(TAG, "playing" + elapsedTime);
                    break;
                case PlayerListener.STATE_STOP:
//			if(isLocalPlayback){
////				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
//			}
                    //Log.d(TAG, "stop");
                    break;
                case PlayerListener.STATE_RESUME:
                    //Log.d(TAG, "resumes");
                    break;
                case PlayerListener.STATE_STARTED:
                    mVideoViewPlayer.showMediaController();
                    //Log.i(TAG, "mPlayerListener: mVideoViewPlayer showMediaController");
                    //mIsContentComplete boolean is used to ensure that mIsContentComplete event is only reported once.
                    if (contentStarted) {
                        //Report start of content playback.
                        //pulseSession.contentStarted();
                        contentStarted = false;
                        //Log.i(TAG, "Content playback started.");
                    } else {
                        //Log.i(TAG, "Content playback resumed.");
                    }
                    duringVideoContent = true;
                    //Log.d(TAG, "started");
                    break;
                case PlayerListener.STATE_COMPLETED:
                    //Log.d(TAG,"completed");
//			if(isLocalPlayback){
////				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
//			}
                    break;
            }
//		MediaUtil.savePlayerState(mData._id, state, elapsedTime);


        }
        @Override
        public void onSeekComplete(MediaPlayer mp, boolean isSeeking) {
            // TODO Auto-generated method stub
            if(mPlayerStatusListener != null){
                mPlayerStatusListener.playerStatusUpdate("onSeekComplete");
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int perBuffer) {
//		if(mPlayerStatusListener != null){
//			mPlayerStatusListener.playerStatusUpdate("buffering :: "+perBuffer);
//		}

            if (mPerBuffer <= perBuffer) {
                mPerBuffer = perBuffer;
            }
            if (mBufferPercentage != null) {
                //mBufferPercentage.setVisibility(View.VISIBLE);
                mBufferPercentage.setText("Loading " + mPerBuffer + "%");
            }
            int currentseekposition = mVideoViewPlayer.getCurrentPosition();
            if (currentseekposition < 0) {
                currentseekposition = 510;
            }
            if (isMediaPlaying() && currentseekposition > 500) {
                if (contentStarted) {
                    //Report start of content playback.
                    //pulseSession.contentStarted();
                    contentStarted = false;
                    //Log.i(TAG, "Content playback started.");
                } else {
                    //Log.i(TAG, "Content playback resumed.");
                }
                duringVideoContent = true;
                mPerBuffer = 0;
                mVideoViewPlayer.deregisteronBufferingUpdate();
                mProgressBarLayout.setVisibility(View.GONE);
                mVideoViewPlayer.showMediaController();
                //Log.i(TAG, "mPlayerListener: mVideoViewPlayer showMediaController");
                mPlayerState = PLAYER_PLAY;
                if(mPlayerStatusListener != null){
                    mPlayerStatusListener.playerStatusUpdate("Buffering ended");
                }
//			/*if(!mContext.getResources().getBoolean(R.bool.isTablet)){
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
//			}*/

                if(ApplicationController.ENABLE_MOU_TRACKING) {
                    if (mMouTracker == null) {
                        mMouTracker = new MOUTracker(mVideoViewPlayer, mContext, mData, null);
                        mMouTracker.setVODContentType(mContentType);
                        mMouTracker.start();
                        mMouTracker.setNId(mNid);
                        mMouTracker.setNotificationTitle(mNotificationTitle);
                        mMouTracker.setPlayedProfile(profileSelect);
                    }
                }
            }else if(isMediaPlaying()
                    &&(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM))){
                mPerBuffer = 0;
                mVideoViewPlayer.deregisteronBufferingUpdate();
                mProgressBarLayout.setVisibility(View.GONE);
                mVideoViewPlayer.showMediaController();
                //Log.i(TAG, "mPlayerListener: mVideoViewPlayer showMediaController");
                mPlayerState = PLAYER_PLAY;
                if(mPlayerStatusListener != null){
                    mPlayerStatusListener.playerStatusUpdate("Buffering ended");
                }
//			/*if(!mContext.getResources().getBoolean(R.bool.isTablet)){
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
//			}*/

                if(ApplicationController.ENABLE_MOU_TRACKING) {
                    if (mMouTracker == null) {
                        mMouTracker = new MOUTracker(mVideoViewPlayer, mContext, mData, null);
                        mMouTracker.setVODContentType(mContentType);
                        mMouTracker.start();
                        mMouTracker.setNId(mNid);
                        mMouTracker.setNotificationTitle(mNotificationTitle);
                        mMouTracker.setPlayedProfile(profileSelect);
                    }
                }
            }
        }
        @Override
        public boolean onError(MediaPlayer mp, int arg1, int arg2, String errorMessage, String stackTrace) {
            String what = new String();
            String error = new String();
            if(mPlayerStatusListener != null){

                switch (arg1) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        what = "MEDIA_ERROR_UNKNOWN";
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        what = "MEDIA_ERROR_SERVER_DIED";
                        break;
                    default:
                        what = ""+arg1;
                        break;
                }
                switch (arg2) {
                    default:
                        error = ""+arg2;
                        break;
                }
                mPlayerStatusListener.playerStatusUpdate("Play Error :: what = " + what + " extra= " + error);
            }
            if(mMinimizeButton != null){
                mMinimizeButton.setVisibility(View.INVISIBLE);
            }
            closePlayer();

            if (mPlayerState == PLAYER_STOPPED && !getEPGPlayBackInitialized()) {
                retryPlayback();
            }
            duringVideoContent = false;
            String reason = errorMessage == null ? "Play Error :: what = " + what + " extra= " + error : errorMessage;
            Analytics.mixPanelUnableToPlayVideo(mData, reason, profileSelect);
            Analytics.gaNotificationPlayBackFailed(mNotificationTitle, mNid, mData, profileSelect);

            return true;
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
            if(mPlayerStatusListener != null){
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
                        break;

                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        what = "MEDIA_INFO_BUFFERING_END";
                        onBufferingEnd();
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
                        what = ""+arg1;
                        break;
                }

                String extra = new String();
                extra = ""+arg2;
//			mPlayerStatusListener.playerStatusUpdate("Play Info :: what = "+what+" extra= "+extra);
            }
            return false;
        }
        @Override
        public void onCompletion(MediaPlayer mp) {
            //Log.i(TAG, "content playback completed.");
            //pulseSession.contentFinished();
            if(mPlayerStatusListener != null){
                mPlayerStatusListener.playerStatusUpdate("Play complete :: ");
            }
            duringVideoContent = false;
//		locationClient.disconnect();
        }
        @Override
        public void onDrmError(){
            closePlayer();
        }

        @Override
        public void onRetry() {
            if (mBufferPercentage != null) {
                mBufferPercentage.setVisibility(View.VISIBLE);
                mBufferPercentage.setText("retrying");
            }

        }

        @Override
        public void onFullScreen(boolean value) {
            isFullScreen = value;
		/*if (mContext.getResources().getBoolean(R.bool.isTablet)) {
			if (value) {
				playInLandscape();
			} else {
				playInPortrait();
			}
		} else {*/
            if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                resumePreviousOrientaionTimer();
            }
            else {
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                resumePreviousOrientaionTimer();
            }
        }

        @Override
        public void onBuffering() {
            if(mProgressBarLayout!=null && mProgressBarLayout.getVisibility() == View.GONE && !isMinimized){

                if (mBufferPercentage != null) {
                    mBufferPercentage.setVisibility(View.GONE);
                }
                mProgressBarLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onBufferingUpdate(boolean isBuffering) {

        }

        @Override
        public boolean onError(Object errorInfo, String errorTrace) {
            return false;
        }

        @Override
        public void onSubtitleChanged(String subtitleTrack) {

        }

    };



    private PlayerListener mAdPlayerListener = new PlayerListener() {


        @Override
        public void onSeekComplete(MediaPlayer mp, boolean isSeeking) {
            if(mPlayerStatusListener != null){
                mPlayerStatusListener.playerStatusUpdate("onSeekComplete");
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int perBuffer) {
            if (mPerBuffer <= perBuffer) {
                mPerBuffer = perBuffer;
            }
            if (mBufferPercentage != null) {
                //mBufferPercentage.setVisibility(View.VISIBLE);
                mBufferPercentage.setText("Loading " + mPerBuffer + "%");
            }
            int currentseekposition = mVideoViewPlayer.getCurrentPosition();
            if (currentseekposition < 0) {
                currentseekposition = 510;
            }
            if (currentseekposition > 500
                    && isMediaPlaying()) {
                mPerBuffer = 0;
                mVideoViewPlayer.deregisteronBufferingUpdate();
                mProgressBarLayout.setVisibility(View.GONE);
                mPlayerState = PLAYER_PLAY;
                if (mPlayerStatusListener != null) {
                    mPlayerStatusListener.playerStatusUpdate("Buffering ended");
                }
                mVideoViewPlayer.hideMediaController();
                //If the ad is played, remove the timeout handler.
                playbackHandler.removeCallbacks(playbackRunnable);

                if (duringAd) {
                    return;
                }
                //Report ad playback to Pulse SDK.
                //Log.i(TAG, "Ad playback started.");
                if(!isAdResumed){
                    //currentPulseVideoAd.adStarted();
                }
                duringAd = true;

//			/*if(!mContext.getResources().getBoolean(R.bool.isTablet)){
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
//			}*/
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra, String errorMessage, String stackTrace) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                    //Log.i(TAG, "unknown media playback error");
                    //currentPulseVideoAd.adFailed(PulseAdError.NO_SUPPORTED_MEDIA_FILE);

                    break;
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    //Log.i(TAG, "server connection died");
                  //  currentPulseVideoAd.adFailed(PulseAdError.REQUEST_TIMED_OUT);
                    break;
                default:
                    //Log.i(TAG, "generic audio playback error");
                    //currentPulseVideoAd.adFailed(PulseAdError.COULD_NOT_PLAY);
                    break;
            }
            duringAd = false;
            playbackHandler.removeCallbacks(playbackRunnable);
            return true;
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
            if(mPlayerStatusListener != null){
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
                        break;

                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        what = "MEDIA_INFO_BUFFERING_END";
                        onBufferingEnd();
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
                        what = ""+arg1;
                        break;
                }

                String extra = new String();
                extra = ""+arg2;
//			mPlayerStatusListener.playerStatusUpdate("Play Info :: what = "+what+" extra= "+extra);
            }
            return true;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            duringAd = false;
            //currentPulseVideoAd.adFinished();
            currentAdProgress = 0;
            //Log.i(TAG, "Ad playback completed.");
            //Report Ad completion to Pulse SDK.
        }

        @Override
        public void onFullScreen(boolean value) {
            isFullScreen = value;
		/*if (mContext.getResources().getBoolean(R.bool.isTablet)) {
			if (value) {
				playInLandscape();
			} else {
				playInPortrait();
			}
		} else {*/
            if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                resumePreviousOrientaionTimer();
            }
            else {
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                resumePreviousOrientaionTimer();
            }
        }

        @Override
        public void onDrmError() {

        }

        @Override
        public void onStateChanged(int state1, int pos) {
            state = PlayerListener.STATE_PAUSED;
            currentDuration = pos;
            switch (state1) {
                case PlayerListener.STATE_PAUSED:
                    if (isLocalPlayback) {
//				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
                    }
                    //Log.d(TAG, "paused" + pos);
                    break;
                case PlayerListener.STATE_PLAYING:
                    //Log.d(TAG, "playing" + pos);
                    break;
                case PlayerListener.STATE_STOP:
//			if(isLocalPlayback){
////				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
//			}
                    //Log.d(TAG, "stop");
                    break;
                case PlayerListener.STATE_RESUME:
                    //Log.d(TAG, "resumes");
                    break;
                case PlayerListener.STATE_STARTED:

                    break;
                case PlayerListener.STATE_COMPLETED:
                    //Log.d(TAG, "completed");
//			if(isLocalPlayback){
////				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
//			}
            }
        }

        @Override
        public void onRetry() {

        }

        @Override
        public void onBuffering() {
            if(mProgressBarLayout!=null && mProgressBarLayout.getVisibility() == View.GONE && !isMinimized){

                if (mBufferPercentage != null) {
                    mBufferPercentage.setVisibility(View.GONE);
                }
                mProgressBarLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onBufferingUpdate(boolean isBuffering) {

        }

        @Override
        public boolean onError(Object errorInfo, String errorTrace) {
            return false;
        }

        @Override
        public void onSubtitleChanged(String subtitleTrack) {

        }

    };

    private boolean isAdResumed = false;
    private View.OnTouchListener mOnVideoViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View arg0, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (duringAd) {
                    //Added to prevent click on the ads that are nor loaded yet which would prevent "ad paused before the ad played" error.
                    mVideoViewPlayer.onPause();
                    isAdPuasedWhenPlaying = true;
                    duringAd = false;
                    //Report ad paused to Pulse SDK.
                   // currentPulseVideoAd.adPaused();
                    //Report ad clicked to Pulse SDK.
                    //currentPulseVideoAd.adClickThroughTriggered();
//                    mVideoView.setOnTouchListener(null);
                    if(clickThroughCallback != null){
                      //  clickThroughCallback.onClicked(currentPulseVideoAd);
                    }
                    playbackHandler.removeCallbacks(playbackRunnable);
                    //Log.i(TAG, "ClickThrough occurred.");
                    return false;
                }
            }
            if (!isMinimized && videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW) {
                mVideoViewPlayer.onTouchEvent(event);
            }

            if (!isFullScreen && mMinimizeButton != null) {
                if (mTrailerButton != null && mTrailerButton.getVisibility() == View.VISIBLE) {
                    return false;
                }
//					mMinimizeButton.setVisibility(mVideoViewPlayer.isMediaControllerShowing()?View.VISIBLE:View.INVISIBLE);
            }
            return false;
        }
    };

    private void initPulse() {

        //Set a listener to receive low-level log messages about errors, warnings and the like.
        /*Pulse.setLogListener(new LogListener() {
            @Override
            public void onLog(LogItem logItem) {
                Log.i("PulseManager1", logItem.toString());
            }
        });*/

        // Initialize the Pulse SDK with "setPulseHost(Host, Device Container, Persistent Id)"
        // Host:
        //     Your Pulse account host
        // Device Container:
        //     Device container in Ooyala Pulse is used for targeting and
        //     reporting purposes. This device container attribute is only used
        //     if you want to override the Pulse device detection algorithm on the
        //     Pulse ad server. This should only be set if normal device detection
        //     does not work and only after consulting Ooyala personnel. An incorrect
        //     device container value can result in no ads being served or incorrect
        //     ad delivery and reports.
        // Persistent Id:
        //     The persistent identifier is used to identify the end user and is the
        //     basis for frequency capping, uniqueness, DMP targeting information and
        //     more. Use Apple's advertising identifier (IDFA), or your own unique
        //     user identifier here.
        // Refer to:
        //     http://support.ooyala.com/developers/ad-documentation/oadtech/ad_serving/dg/integration_sdk_parameter.html
        //Pulse.setPulseHost("http://in-setindia.videoplaza.tv", null, null);

//        Pulse.setPulseHost("http://pulse-demo.videoplaza.tv", null, null);
    }


    public PulseManager(Context context, CardData data,String conId) {
        super(context, data, conId);
        mContext = context;
        mData = data;
        streamId = conId;
        //Log.i(TAG, "PulseManager: consutructor<-");
        initPulse();

        int sdkVersion = Build.VERSION.SDK_INT;
        boolean isExoEnabled = PrefUtils.getInstance().getPrefIsExoplayerEnabled();
        boolean isDVRContent = PrefUtils.getInstance().getPrefIsExoplayerDvrEnabled();
//        if (mData != null
//                && mData.generalInfo != null
//                && mData.generalInfo.type != null
//                &&(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
//                || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM))) {

        if( sdkVersion>=16 && (isDVRContent || isExoEnabled)){
            videoPlayerType = VIDEO_PLAYER_TYPE.EXOVIDEOVIEW;
        } else
        {
            videoPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;
        }
//        videoPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;
//        }else{
//        mVideoViewPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;
//    }

        mInflator = LayoutInflater.from(mContext);
    }
    public void startPulseSession(Uri videoContentUri) {

        // Create and start a pulse session
        //Log.i(TAG, "startPulseSession: videoContentUri- "+ videoContentUri);
//        pulseSession = Pulse.createSession(getContentMetadata(), getRequestSettings(true));
//        pulseSession.startSession(mPulseSessionListener);

        this.videoContentUri =videoContentUri;

        //Initiating the handler to track the progress of content/ad playback.
        contentProgressHandler = new Handler();
    }

    /////////////////////Playback helper////////////////////

    /**
     * Play/resume the selected video content.
     */
    public void playVideoContent() {
        //Setup video player for content playback.
        //Log.i(TAG, "startContentPlayback:starting content playback ");
        if(APIConstants.STREAMINGFORMATHLS.equals(mStreamingFormat)) {
            mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HLS);
        }else if(APIConstants.STREAMINGFORMATHTTP.equals(mStreamingFormat)){
            mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
        }else if(APIConstants.STREAMINGFORMATRTSP.equals(mStreamingFormat)){
            mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.RTSP);
        }
        //Log.i(TAG, "startContentPlayback: currentContentProgress- " + currentContentProgress);
        mVideoViewPlayer.setPositionWhenPaused((int) currentContentProgress);
        mVideoViewPlayer.setUri(videoContentUri, VideoViewPlayer.StreamType.VOD);
        mVideoViewPlayer.setPlayerListener(mPlayerListener);
        mVideoView.setOnTouchListener(mOnVideoViewTouchListener);
        isToShowMediaController = true;
        mVideoViewPlayer.setMinized(false);
        contentStarted = true;
    }

    /**
     * Try to play the provided ad.
     *
     * @param timeout The timeout for ad playback.
     * @param adUri   The uri of the ad video.
     */
    private final int AD_TIME_OUT = 25;
    public void playAdContent(float timeout, String adUri) {
        //Configure a handler to monitor playback timeout.
        //Log.d(TAG, "playAdContent play adUri- " + adUri);
        isAdResumed = false;
        playbackHandler.postDelayed(playbackRunnable, (long) (AD_TIME_OUT * 1000));
        mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
        mVideoViewPlayer.setPositionWhenPaused((int) currentAdProgress);
        //Log.d(TAG, "playAdContent currentAdProgress- " + currentAdProgress);
        mVideoViewPlayer.setUri(Uri.parse(adUri), VideoViewPlayer.StreamType.VOD);
        mVideoViewPlayer.setPlayerListener(mAdPlayerListener);
        mVideoViewPlayer.setMinized(true);
        isToShowMediaController = false;
    }


    /**
     * This method would be called when user return from a click through page.
     * If the ad video support seeking, it would be resumed otherwise the ad would be played from the beginning.
     */
    public void resumeAdPlayback() {
       /* if (currentPulseVideoAd != null) {
            //Report ad resume to Pulse SDK.
            mVideoViewPlayer.setPlayerListener(null);
            currentPulseVideoAd.adResumed();
            isAdResumed = true;
            isAdPuasedWhenPlaying = false;
            playbackHandler.postDelayed(playbackRunnable, (AD_TIME_OUT * 1000));
            //Log.d(TAG, "resumeAdPlayback: mVideoViewPlayer.getCurrentPosition()- " +
                    mVideoViewPlayer.getCurrentPosition() +
                    " currentAdProgress- " + currentAdProgress);
            mVideoViewPlayer.onResume();
            mVideoViewPlayer.setPlayerListener(mAdPlayerListener);
            contentProgressHandler.post(onEveryTimeInterval);
        }*/
    }


    ///////////////////Helper methods//////////////////////

    /**
     * An ad contains a list of media file with different dimensions and bit rates.
     * In this example this method selects the media file with the highest bit rate
     * but in a production application the best media file should be selected based
     * on resolution/bandwidth/format considerations.
     *
     * @param potentialMediaFiles A list of available mediaFiles.
     * @return the selected media file.
     */
   /* MediaFile selectAppropriateMediaFile(List<MediaFile> potentialMediaFiles) {
        MediaFile selected = null;
        int highestBitrate = 0;
        for (MediaFile file : potentialMediaFiles) {
            if (file.getBitRate() > highestBitrate) {
                highestBitrate = file.getBitRate();
                selected = file;
            }
        }
        return selected;
    }*/

    /**
     * Create an instance of RequestSetting from the selected videoItem.
     *
     * @return The created {@link RequestSettings}
     */
 /*   private RequestSettings getRequestSettings(boolean applyDefualt) {
        RequestSettings newRequestSettings = new RequestSettings();
        List<RequestSettings.InsertionPointType> filter = new ArrayList<>();
       *//* if (videoItem.getMidrollPositions() != null && videoItem.getMidrollPositions().length != 0) {
            ArrayList<Float> playbackPosition = new ArrayList<>();
            for (int i = 0; i < videoItem.getMidrollPositions().length; i++) {
                playbackPosition.add((float) videoItem.getMidrollPositions()[i]);
            }
            newRequestSettings.setLinearPlaybackPositions(playbackPosition);
        }*//*
        if(applyDefualt){
            ArrayList<Float> playbackPosition = new ArrayList<>();
//            playbackPosition.add((float) 1 * 7);
//            int totalVideoDuration = mVideoViewPlayer.getCachedDuration();
            *//*for(int i = 1 ;totalVideoDuration > 0; totalVideoDuration = totalVideoDuration/7,i++){
                //Log.d(TAG,"count- " + totalVideoDuration + " and i- "+ i);
                playbackPosition.add((float) i * 7);
            }*//*
            for(int i = 7 ;i > 0; i--){
                playbackPosition.add((float) i * 7 *60);
            }
            newRequestSettings.setLinearPlaybackPositions(playbackPosition);
        }
        return newRequestSettings;
    }*/

    /**
     * Create an instance of ContentMetadata from the selected videoItem.
     *
     * @return The created {@link ContentMetadata}.
     */
    /*private ContentMetadata getContentMetadata() {
        ContentMetadata contentMetadata = new ContentMetadata();
        contentMetadata.setCategory("74952123-5380-45e5-857c-32e84248798c");
//        contentMetadata.setTags(new ArrayList<>(Arrays.asList(mContext.getResources().getString(R.string.ooyala_ad_tag))));
//        contentMetadata.setTags(new ArrayList<>(Arrays.asList("standard-linears")));
        return contentMetadata;
    }*/

    /**
     * A helper method to stop a handler by removing its callback method.
     *
     * @param handler the handler that should be stopped.
     */
    public void removeCallback(Handler handler) {

        if (handler == playbackHandler) {
            playbackHandler.removeCallbacks(playbackRunnable);
        } else if (handler == contentProgressHandler) {
            contentProgressHandler.removeCallbacks(onEveryTimeInterval);
        }
    }

    ////////////////////click through related methods///////////
    public void returnFromClickThrough() {
        resumeAdPlayback();
    }
    public interface ClickThroughCallback {
       // void onClicked(PulseVideoAd ad);
    }

    /////////////////////Runnable methods//////////////////////

    protected void initializeVideoPlay(Uri uri) {
        //Log.d(TAG, "video url "+uri);
        MyplexVideoViewPlayer.StreamType streamType = MyplexVideoViewPlayer.StreamType.VOD;
        if (mVideoViewPlayer == null) {
            Log.d("VideoViewPlayer","new mVideoViewPlayer");
            if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
                mVideoViewPlayer = new VideoViewPlayer((com.myplex.myplex.media.VideoView)mVideoView, mContext, uri,streamType);
            // mVideoViewPlayer.openVideo();
            startPulseSession(uri);
        } else {
            Log.d("VideoViewPlayer", "old mVideoViewPlayer");
            startPulseSession(uri);
        }
        mVideoViewPlayer.setOnLicenseExpiryListener(onLicenseExpiryListener);
        mVideoViewPlayer.hideMediaController();
        mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);

        mVideoView.setOnTouchListener(mOnVideoViewTouchListener);
//		mVideoViewPlayer.goToTime(MediaUtil.ELLAPSE_TIME);
    }
    public void setOnClickThroughCallback(PulseManager.ClickThroughCallback callback) {
        clickThroughCallback = callback;
    }

    public boolean isAdPlaying(){
        return isAdPuasedWhenPlaying ;
    }

    public void setCurrentContentProgress(long currentContentProgress) {
        //Log.d(TAG, "setCurrentContentProgress: " + currentContentProgress);
        this.currentContentProgress = currentContentProgress;
    }

    public long getCurrentContentProgress(){
        //Log.d(TAG, "getCurrentContentProgress: " + currentContentProgress);
        return currentContentProgress;
    }
    private class PulseSessionListener {
    }

}
