package com.myplex.myplex.media;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.drm.DrmErrorEvent;
import android.drm.DrmInfoEvent;
import android.drm.DrmStore;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.myplex.model.CardData;
import com.myplex.model.CardDataSubtitles;
import com.myplex.model.DownloadMediadata;
import com.myplex.model.ErrorManagerData;
import com.myplex.model.PlayerStatusUpdate;
import com.myplex.util.WidevineDrm;
import com.myplex.myplex.media.exoVideo.MyplexVideoViewPlayer;
import com.myplex.myplex.media.exoVideo.PlayerEventListenerInterface;
import com.myplex.myplex.media.exoVideo.TrackData;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


public class VideoViewPlayer implements MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,
		MediaPlayer.OnCompletionListener, OnPreparedListener,
		OnSeekCompleteListener, OnBufferingUpdateListener,MyplexVideoViewPlayer {


	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;
	private static final int STATE_SUSPEND = 6;
	private static final int STATE_RESUME = 7;
	private static final int STATE_SUSPEND_UNSUPPORTED = 8;
	private static final int STATE_RETRYING = 100;
	private static final int INTERVAL_RETRY = 3*1000;

	private static final String TAG = "VideoViewPlayer";
	private static final String DVR_URI_FLAG = "dw=";
	private int mCurrentState = STATE_IDLE;

	private VideoView mVideoView;

	private Uri mUri;
	private WidevineDrm drmManager;
	private boolean isLocked;

	// State maintained for proper onPause/OnResume behaviour.

	private int mAutoStartCount = 0;
	private int mPositionWhenPaused = -1;
	private boolean mWasPlayingWhenPaused = false;
	private boolean mControlResumed = false;
	private boolean mStopHandler = false;
	private boolean mSessionClosed = false;
	private Context mContext = null;
	private ErrorManagerData errordata = null;
	private ProgressBar mProgressBar = null;
	private boolean iPlayerStarted=false;

    public int _overrideWidth = 240;
    public int _overrideHeight = 320;
    private String streamName;


	StreamType mStreamType = null;
	StreamProtocol mStreamProtocol = StreamProtocol.RTSP;

	private MediaController2 mediaController2 = null;

	private MediaPlayer mMediaPlayer;
	private PlayerListener mPlayerListener = null;

	private static final int START = 1;
	private static final int STOP = 2;
	private static final int END = 201;
	private static final int INTERVAL_BUFFERING_PER_UPDATE = 200;


//	private ImageView mCenterPlayButton;

	private Handler mBufferProgressHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			
			if(mStopHandler){
				return;
			}
			if(mSessionClosed){
				return;
			}
			switch (msg.what) {
			case START:
				if (mVideoView == null) {
					return;
				}
				int perBuffer = mVideoView.getBufferPercentage();
				onBufferingUpdate(null, perBuffer);
				msg = obtainMessage(START);
				if (msg != null) {
					sendMessageDelayed(msg, INTERVAL_BUFFERING_PER_UPDATE);
				}
								
				break;
			}
		}
	};
	private boolean isPlayStarted=false;
	private boolean isLive;
	private boolean isMinimized;
	private View mPlayerHeaderView;
	private PlayerGestureListener mGestureDetector;
	private boolean isToolbarShown;
	private boolean isDVR;
	private List<PlayerEventListenerInterface> playerEventsListeners = new ArrayList<>();
	private CardData mCardData;
	private View playerMediaControllsView;
	private View mPlayerControllsView;
	private String stackTrace;

	public VideoViewPlayer(VideoView rootView, Context context, Uri videoUri,
			StreamType streamType) {
		mMediaPlayer = null;
		mVideoView = rootView;
		mStopHandler = false;
		mUri = videoUri;
		errordata = null;
		mContext = context;
		this.mStreamType = streamType;

		initilizeMediaController();
		

	}
	public void SetErrorData(ErrorManagerData data){
		this.errordata = data;
	}
	public ErrorManagerData GetErrorData(){
		if(errordata != null && mVideoView != null){
			errordata.playposition = ""+mVideoView.getCurrentPosition(); 
		}
		return errordata;
	}
	public void setUri(Uri videoUri,StreamType type){
//		mPositionWhenPaused = -1;
		mMediaPlayer = null;
		errordata = null;
		mStopHandler = false;
		this.mUri = videoUri;
		this.mStreamType = type; 
		mVideoView.setVisibility(View.VISIBLE);
		initilizeMediaController();
		if(mUri.toString().toLowerCase().contains("file:") && mUri.toString().toLowerCase().contains(".mp4")){
			openVideo();
			return;
		}
		else if(mUri.toString().contains(".wvm")||mUri.toString().contains("file:"))
		{
				prepareDrmManager(mUri.toString());
				acquireRights(mUri.toString());
		}
		else
		{
			openVideo();
		}
		if (mGestureDetector != null){
			mGestureDetector.setLive(isLive && !isDVR);
		}


	}
	private void initilizeMediaController() {

		if(mVideoView == null){
			return;
		}

		if (!(mVideoView.getParent() instanceof RelativeLayout)) {
			return;
		}
		
		// As of now , only RelativeLayout as VideoView parent is supported.

		RelativeLayout parentVideoViewlayout = (RelativeLayout) mVideoView
				.getParent();

//		if (mStreamType == StreamType.VOD) {
			if(mediaController2 == null){
				mediaController2 = new MediaController2(mContext,(mStreamType == StreamType.VOD)?true:false);
				mediaController2.setCustomVideoView(this);
			}
			mediaController2.setContentEnabled((mStreamType == StreamType.VOD)?true:false);
			LayoutParams layout_params = new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);

			layout_params.addRule(RelativeLayout.ALIGN_BOTTOM,
					mVideoView.getId());

			if(parentVideoViewlayout.indexOfChild(mediaController2) == -1){
				parentVideoViewlayout.addView(mediaController2, layout_params);
			}

			mediaController2.setVisibility(View.GONE);
//		}else if(mStreamType == StreamType.LIVE){
//			if(mediaController2 != null && parentVideoViewlayout.indexOfChild(mediaController2) != -1){
//				parentVideoViewlayout.removeView(mediaController2);
//			}
//		}

//		if(mCenterPlayButton == null){
//			mCenterPlayButton = new ImageView(mContext);
//		
//			mCenterPlayButton.setOnClickListener(mCenterPlayClickListener);
//
//			mCenterPlayButton.setImageResource(R.drawable.play_center_button);
//		
//			RelativeLayout.LayoutParams layout_params2 = new RelativeLayout.LayoutParams(
//				RelativeLayout.LayoutParams.WRAP_CONTENT,
//				RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//			layout_params2.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//			parentVideoViewlayout.addView(mCenterPlayButton, layout_params2);
//		}
//		mCenterPlayButton.setVisibility(View.GONE);

		mGestureDetector = new PlayerGestureListener((Activity)mContext,this, mediaController2, mGestureListener);
		mediaController2.setPlayerHeaderView(mPlayerHeaderView);
		mediaController2.setPlayerControllsView(mPlayerControllsView);
		mediaController2.setMediaControllsView(playerMediaControllsView);
		mediaController2.setLive(isLive);
	}
	private PlayerGestureListener.GestureListener mGestureListener;

	private PlayerStatusUpdate mPlayerStatusListener;
	public void setPlayerStatusUpdateListener(PlayerStatusUpdate listener){
		mPlayerStatusListener = listener;	
		mPlayerStatusListener.playerStatusUpdate("Player Type :: Native ");
	
		if(drmManager!=null) {
			drmManager.setPlayerListener(mPlayerStatusListener);
		}
	}
	
	public void openVideo() { 
		Log.d("PlayerScreen", "VideoViewPlayer openVideo Start");
		// For streams that we expect to be slow to start up, show a
		// progress spinner until playback starts.
		mAutoStartCount = 0;
		if (mUri == null || mVideoView == null) {
			return;
		}

		String scheme = mUri.getScheme();
		if(scheme == null){
			return;
		}
		if(mUri != null && mUri.toString().toLowerCase().contains(DVR_URI_FLAG)){
			isDVR = true;
		}
		if (scheme.equalsIgnoreCase("rtsp")) {
			mStreamProtocol = StreamProtocol.RTSP;
		} else if (scheme.equalsIgnoreCase("http")) {
			mStreamProtocol = StreamProtocol.HTTP_PROGRESSIVEPLAY;
		}
		if(mVideoView != null){
			try{
				mVideoView.stopPlayback();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
//		mVideoView.setVisibility(View.GONE);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		//mVideoView.setVideoURI(mUri);
		mVideoView.setVideoPath(mUri.toString());
		
		
		mVideoView.setOnPreparedListener(this);

		mBufferProgressHandler.sendEmptyMessage(START);

//		showProgressBar(true);

		// make the video view handle keys for seeking and pausing
		mVideoView.requestFocus();

		mCurrentState = STATE_PREPARING;
		Log.d("PlayerScreen", "VideoViewPlayer openVideo end");
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
		//Analytics.endTimedEvent(Analytics.PlayerBuffering);

	}

	OnClickListener mCenterPlayClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (mVideoView == null) {
				return;
			}

//			mCenterPlayButton.setVisibility(View.GONE);

			showProgressBar(true);

			if (mStreamType == StreamType.LIVE) {
				mBufferProgressHandler.sendEmptyMessage(START);
			}
				Log.d("PlayerScreen","onBufferingUpdate form mCenterPlayClickListener");
			onBufferingUpdate(null, 0);

			mVideoView.setVisibility(View.VISIBLE);

			if (mStreamType == StreamType.VOD) {

				// mVideoView.setVideoURI(mUri);

				if (mPositionWhenPaused > 0) {
					mVideoView.seekTo(mPositionWhenPaused);
					mPositionWhenPaused = -1;
				}

				if (mWasPlayingWhenPaused) {
					// mMediaController.show(0);
				}
			}

			mCurrentState = STATE_PREPARING;
			mVideoView.start();

		}
	};

	private Dialog mSplashScreen = null;
	private boolean mSplashScreenDismissed = false;

	/**
	 * call this method on Activity onPause
	 */
	
	public void onPause() {
		Log.d("PlayerScreen", "VideoViewPlayer onPause Start");
		
		if(mVideoView == null){
			return;
		}
		mControlResumed = false;
		mCurrentState = STATE_PAUSED;
        mPositionWhenPaused = mVideoView.getCurrentPosition();
		mWasPlayingWhenPaused = mVideoView.isPlaying();
		// mVideoView.stopPlayback();

		if(mediaController2 != null){
			mediaController2.setVisibility(View.INVISIBLE);
		}
		
		if (mBufferProgressHandler != null) {
			mBufferProgressHandler.removeMessages(START);
		}

		if (!mWasPlayingWhenPaused) {
			// player is already in pause state , no need to do anything.

			switch (mStreamType) {

			case LIVE:

				// to stop the video view reload the video automatically.
				mVideoView.setVisibility(View.GONE);

				break;

			case VOD:

				mVideoView.setVisibility(View.GONE);

				break;
			}

			return;
		}

		switch (mStreamType) {

		case LIVE:

			mVideoView.setVisibility(View.GONE);
			mVideoView.stopPlayback();

			break;

		case VOD:

			mVideoView.setVisibility(View.GONE);
			mVideoView.pause();

			break;
		}
		Log.d("PlayerScreen", "VideoViewPlayer onPause end"+ "  "+mPositionWhenPaused);
	}

	/**
	 * call this method on Activity onResume
	 */

	public void onResume() {
		Log.d("PlayerScreen", "VideoViewPlayer onResume Start");
				
		if(mVideoView == null){
			return;
		}
		mControlResumed = true;

		if (mCurrentState == STATE_PREPARING) {		
			mVideoView.setVisibility(View.VISIBLE);
				Log.d("PlayerScreen","onBufferingUpdate form onResume");
			onBufferingUpdate(null, 0);
				Log.d("PlayerScreen", "VideoViewPlayer onResume STATE_PREPARING End");
			return;
		}
//		mCenterPlayButton.setVisibility(View.GONE);

		if(mCurrentState == STATE_PAUSED){
			showProgressBar(true);
		}
		if (mStreamType == StreamType.LIVE) {
			mBufferProgressHandler.sendEmptyMessage(START);
		}
			Log.d("PlayerScreen","onBufferingUpdate form onResume1");
		onBufferingUpdate(null, 0);

		mVideoView.setVisibility(View.VISIBLE);

		if (mStreamType == StreamType.VOD) {

			// mVideoView.setVideoURI(mUri);
            if (mPositionWhenPaused > 0) {
				mVideoView.seekTo(mPositionWhenPaused);
				mPositionWhenPaused = -1;
			}

			if (mWasPlayingWhenPaused) {
				// mMediaController.show(0);
			}
		}

		mCurrentState = STATE_PREPARING;
		mVideoView.start();
//		if(mSplashScreenDismissed){
//		}else{
//			mCenterPlayButton.setVisibility(View.VISIBLE);
//			mCurrentState = STATE_IDLE;
//		}
		mSplashScreenDismissed = false;
		Log.d("PlayerScreen", "VideoViewPlayer onResume End");
	}

	public boolean onError(MediaPlayer player, int arg1, int arg2) {
		Log.d("PlayerScreen", "VideoViewPlayer onError End");
		if(mCurrentState == STATE_RETRYING){
			Log.d("PlayerScreen", "VideoViewPlayer STATE_RETRYING");
			return true;
		}
		mAutoStartCount++;
		if(mStreamProtocol == StreamProtocol.HTTP_PROGRESSIVEPLAY && mAutoStartCount < 2){
			int value = 0;
			if(player != null){
				mCurrentState = STATE_RETRYING;
				mVideoView.postDelayed(new Runnable() {

					@Override
					public void run() {
						if(mVideoView != null && mUri != null){
							try {
								mCurrentState = STATE_PREPARING;
								mVideoView.setVideoPath(mUri.toString());								
							} catch (IllegalStateException e) {
								mAutoStartCount = 3;
								e.printStackTrace();
								StringWriter sw = new StringWriter();
								e.printStackTrace(new PrintWriter(sw));
								stackTrace = sw.toString();
							}
						}

					}
				}, INTERVAL_RETRY);
			}
			else{
				value = 1;
				mVideoView.seekTo(mPositionWhenPaused);
				mVideoView.start();
			}
			if(mPlayerStatusListener != null){
				mPlayerStatusListener.playerStatusUpdate("Retrying "+mAutoStartCount+" form position "+mPositionWhenPaused+" with error "+arg1+" status "+value);
			}
            Toast.makeText(mContext, "Retrying " + mAutoStartCount, Toast.LENGTH_LONG).show();

			if (mPlayerListener != null) {
				mPlayerListener.onRetry();
			}
			return true;
		}
		
		if (mPlayerListener != null) {
			boolean ret = mPlayerListener.onError(player, arg1, arg2, null,stackTrace);
			mPlayerListener = null;
			return ret;
		}
		return false;
	}

	public void onCompletion(MediaPlayer mp) {
		Log.d("PlayerScreen", "VideoViewPlayer onCompletion End");
		if (mPlayerListener != null) {
			try{
			mPlayerListener.onCompletion(mp);
			//mixPanelVideoTimeCalculationOnCompletion();
			mPlayerListener.onStateChanged(PlayerListener.STATE_COMPLETED, 	0);
			}catch(Exception e){
				e.printStackTrace();
				Log.e("PlayerScreen", "some error " + e.getMessage());
			}
		}
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d("PlayerScreen", "VideoViewPlayer onPrepared End");
		mMediaPlayer = mp;
			Log.d("PlayerScreen", "VideoViewPlayer onPrepared");
		if(mVideoView == null){
			return;
		}
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play onPrepared :: ");
		}
		if(mediaController2 != null){
			mediaController2.setMediaPlayer(mp);
		}
//		showProgressBar(false);
		if(!mControlResumed){
			// If activity is not visible.
//			if(mSplashScreen != null && mSplashScreen.isShowing()){
//			}else{
//				mp.stop();			
//				return;
//			}
		}
		// Don't start until ready to play. The arg of seekTo(arg) is the start
		// point in
		// milliseconds from the beginning. In this example we start playing 1/5
		// of
		// the way through the video if the player can do forward seeks on the
		// video.

		
		
		mCurrentState = STATE_PREPARED;
		Log.d("PlayerScreen", "VideoViewPlayer onPrepared");
		mp.setOnSeekCompleteListener(this);
		mp.setOnBufferingUpdateListener(this);
		mp.setOnInfoListener(this);
//		mp.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
//			
//			@Override
//			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//				onBufferingUpdate(mp, 100);
//			}
//		});

		
			Log.d("PlayerScreen","onBufferingUpdate form onPrepared");
		onBufferingUpdate(mp, mVideoView.getBufferPercentage());

		mVideoView.start();

		if (mediaController2 != null) {
			mediaController2.setMediaPlayer(mVideoView);
		}
		if (mVideoView.canSeekForward() && mPositionWhenPaused > 0) {
			mVideoView.seekTo(mPositionWhenPaused);
		}
	}
	public int getmPositionWhenPaused() {
		return mPositionWhenPaused;
	}
	
	public void deregisteronBufferingUpdate(){
		mStopHandler = true;
		if(mMediaPlayer != null){
			mMediaPlayer.setOnBufferingUpdateListener(null);
		}
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play total duration :: "+mVideoView.getDuration());
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			if(retriever != null) {
				try {
					retriever.setDataSource(mUri.toString());
				}
				catch (IllegalArgumentException ex) {
					Log.e(TAG, "deregisteronBufferingUpdate IllegalArgumentException");
				}
				catch (Exception e) {
					Log.e(TAG, "deregisteronBufferingUpdate Exception");
				}
			}

		}
	}
	public boolean isControlResumed() {
		return mControlResumed;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d("PlayerScreen", "VideoViewPlayer onSeekComplete");
		
			
		if (mPlayerListener != null) {
			mPlayerListener.onSeekComplete(mp, false);

		}
		boolean dismissdialog = false;
		if(mVideoView != null && mStreamProtocol == StreamProtocol.RTSP && mVideoView.isPlaying() && mVideoView.getCurrentPosition() > (mPositionWhenPaused + 500)){
			dismissdialog = true;
		}
		if(mStreamProtocol != StreamProtocol.RTSP){
			dismissdialog = true;
		}
		if(dismissdialog){
			showProgressBar(false);
		}

		if (mediaController2 != null) {
			mediaController2.setEnabled(true);
		}

	}

	public void setProgressBarView(ProgressBar progressBar) {
		this.mProgressBar = progressBar;
		showProgressBar(false);
	}
	public void setSplashScreenView(Dialog progressBar) {
		this.mSplashScreen = progressBar;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int perBuffer) {

//		Log.d("VideoViewPlayer", "onBufferingUpdate: "+perBuffer);

		if (mPlayerListener != null) {
			mPlayerListener.onBufferingUpdate(arg0, perBuffer);
//			return;
		}

		if(mProgressBar != null && mProgressBar.getVisibility() != View.VISIBLE){
			return;
		}
		switch (mStreamProtocol) {
		case RTSP:
			boolean dismissdialog = false;
			if(perBuffer > 99){
				dismissdialog = true;
			}
			if(mVideoView != null && mVideoView.isPlaying() && mVideoView.getCurrentPosition() > (mPositionWhenPaused + 500)){
				dismissdialog = true;
			}
			if(dismissdialog){
				showProgressBar(false);
				mCurrentState = STATE_PREPARED;
				
				
			}
//			if (perBuffer > 100) {
//				showProgressBar(false);
//				mCurrentState = STATE_PREPARED;
//			}
//			
//			if (perBuffer == 0) {
//				showProgressBar(true);
//			}

			break;

		default:
			break;
		}

	}

	@Override
	public View getView() {
		return null;
	}

	@Override
	public boolean isPlaybackInitialized() {
		return false;
	}

	@Override
	public boolean isPlayerPaused() {
		return false;
	}

	/**
	 * call this method on Activity onTouchEvent
	 */

	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:

			if (mCurrentState < STATE_PREPARED) {
				return true;
			}
			
			if (mediaController2 != null) {
				mediaController2.doShowHideControl();
			}
			return true;

		}
		return false;
	}

	public void setPlayerListener(PlayerListener mPlayerListener) {
		this.mPlayerListener = mPlayerListener;
		if(mediaController2 != null){
			mediaController2.setPlayerListener(mPlayerListener);
		}
	}
	public void closeSession(){
		try {
			if (mVideoView != null) {
				mVideoView.stopPlayback();
				mVideoView.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			Log.e("PlayerScreen", "VideoViewPlayer stopPlayback exception");
			e.printStackTrace();
		}
	}
	public boolean isMediaControllerShowing(){
		if(mediaController2 != null){
			return mediaController2.isShowing();
		}
		return false;
	}
	public void hideMediaController(){
		if(mediaController2 != null){
			mediaController2.hide();
		}
	}
	public void playerInFullScreen(boolean value){
		if(mediaController2 != null){
			mediaController2.playerInFullScreen(value);
		}
	}
	public void showMediaController(){
		if(mediaController2 != null){
			mediaController2.show();
		}
	}
	
	public void disableMediaController(){
		if(mediaController2 != null){
			mediaController2.setEnabled(false);
			mediaController2.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setSplashScreenDismissed(boolean value){
		mSplashScreenDismissed = value;
	}
	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		mPositionWhenPaused = mVideoView.getCurrentPosition();
		if (mPlayerListener != null) {
			boolean ret = mPlayerListener.onInfo(arg0, arg1, arg2);
		}
		return false;
	}
	public void acquireRights(String url){
		int rightStatus= drmManager.checkRightsStatus(url);
		if(rightStatus!=DrmStore.RightsStatus.RIGHTS_VALID)
		{
			int status=drmManager.acquireRights(url);
			if(status!=0)
			{
                Toast.makeText(mContext, "Acquire Rights Failed", Toast.LENGTH_LONG).show();
				//closeSession();
				if(mPlayerListener!=null)
				{
					mPlayerListener.onDrmError();
				}
			}
		}
		else
		{
			startPlayer(true);
		}
		
	}
	public interface OnLicenseExpiry {
		public void licenseExpired();
	}
	
	private OnLicenseExpiry onLicenseExpiryListener=null;
	
	public void setOnLicenseExpiryListener(
			OnLicenseExpiry onLicenseExpiryListener) {
		this.onLicenseExpiryListener = onLicenseExpiryListener;
	}
	
	private void prepareDrmManager(String url){
		
		drmManager = new WidevineDrm(mContext);
		
		
		drmManager.logBuffer.append("Asset Uri: " + url + "\n");
		drmManager.logBuffer.append("Drm Server: " + WidevineDrm.Settings.DRM_SERVER_URI + "\n");
		drmManager.logBuffer.append("Device Id: " + WidevineDrm.Settings.DEVICE_ID + "\n");
		drmManager.logBuffer.append("Portal Name: " + WidevineDrm.Settings.PORTAL_NAME + "\n");

		
        // Set log update listener
        WidevineDrm.WidevineDrmLogEventListener drmLogListener =
            new WidevineDrm.WidevineDrmLogEventListener() {

            public void logUpdated(int status,int value) {
                
            	updateLogs(status,value);
            }
        };
		
        drmManager.setLogListener(drmLogListener);
        
        drmManager.registerPortal(WidevineDrm.Settings.PORTAL_NAME);
        
		}
	private void startPlayer(final boolean status){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				if(status){
					openVideo();
				}
				else
				{
					if(mPlayerListener!=null)
					{
						mPlayerListener.onDrmError();
					}
				}
			}
		});
	}
	protected void updateLogs(int status,int value) {
		// TODO Auto-generated method stub
		if(!iPlayerStarted)
		{
			if(status==0 && value== DrmInfoEvent.TYPE_RIGHTS_INSTALLED)
			{
				iPlayerStarted=true;
				startPlayer(true);
			}
			if(status!=0 ){
				iPlayerStarted=true;
				String errMsg = "Error while playing";
								
				switch (value) {
				case DrmErrorEvent.TYPE_NO_INTERNET_CONNECTION:
					errMsg="No Internet Connection";
					break;
				case DrmErrorEvent.TYPE_NOT_SUPPORTED:
					errMsg="Device Not Supported";
					break;
				case DrmErrorEvent.TYPE_OUT_OF_MEMORY:
					errMsg="Out of Memory";
					break;
				case DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED:
					errMsg="Process DRM Info failed";
					break;
				case DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED:
					errMsg="Remove All Rights failed";
					break;
				case DrmErrorEvent.TYPE_RIGHTS_NOT_INSTALLED:
					errMsg="Rights not installed";
					break;
				case DrmErrorEvent.TYPE_RIGHTS_RENEWAL_NOT_ALLOWED:
					errMsg="Rights renewal not allowed";
					break;
			}
				//mixPanelUnableToPlayVideo(errMsg);
                Toast.makeText(mContext, errMsg+" ("+status+")", Toast.LENGTH_LONG).show();
				startPlayer(false);
				//drmManager.
			}
			
			if(status == 608 || status == 607){
				 iPlayerStarted=false;
				 if(onLicenseExpiryListener != null ){
					 onLicenseExpiryListener.licenseExpired();
				 }
			}
			if(drmManager!=null)
				drmManager.unRegisterLogListener();
		
		}
	}
	
	public void setPositionWhenPaused(int mPositionWhenPaused) {
		this.mPositionWhenPaused = mPositionWhenPaused;
	}
	
	public boolean wasPlayingWhenPaused() {
		return mWasPlayingWhenPaused;
	}
	
	public void setFullScreenTooggle(int visibility){
	    	if(mediaController2 != null){
	    		mediaController2.setFullScreenTooggle(visibility);
	    	}
	}
	
	
	
	@Override
	public boolean isPlaying() {
		return mVideoView.isPlaying();
	}
	@Override
	public int getCurrentPosition() {
		return mVideoView.getCurrentPosition();
	}
	@Override
	public void resizeVideo(int width, int height) {
		_overrideHeight = height;
        _overrideWidth = width;
        // not sure whether it is useful or not but safe to do so
        mVideoView.getHolder().setFixedSize(width, height);
        //getHolder().setSizeFromLayout();
        mVideoView.requestLayout();
        mVideoView.invalidate(); // very important, so that onMeasure will be triggered

	}
	@Override
	public void setParams(LayoutParams params) {
		mVideoView.setLayoutParams(params);
	}
	@Override
	public void setStreamName(String streamName) {
		 this.streamName= streamName;
	}
	@Override
	public int getCachedDuration() {
		return mVideoView.getCachedDuration();
	}
	@Override
	public void setMinized(boolean minimized) {
		this.isMinimized = minimized;
	}

	@Override
	public void setAdContainer(FrameLayout frameLayout) {

	}

	@Override
	public void setDebugTxtView(TextView textView) {

	}

	@Override
	public void addPlayerEvent(PlayerEventListenerInterface playerEventListenerInterface) {
		playerEventsListeners.add(playerEventListenerInterface);
	}


	@Override
    public void setStreamProtocol(StreamProtocol streamProtocol) {
//        mStreamProtocol = streamProtocol;
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
	public void play() {

	}

	@Override
	public void setFullScreen(boolean isFullScreen) {
		if (mediaController2 != null)
			mediaController2.setFullScreen(isFullScreen);
	}

	@Override
	public long getConsumedData() {
		return 0;
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

	}

	@Override
	public void isPreviewInitialWorkDone(boolean b) {

	}

	@Override
	public boolean isLive() {
		return isLive;
	}

	@Override
	public void seekTo(int mNewPosition) {
		mMediaPlayer.seekTo(mNewPosition);
	}

	@Override
	public MediaController2 getMediaControllerInstance() {
		if (mediaController2 == null){
			mediaController2 = new MediaController2(mContext,(mStreamType == StreamType.VOD)?true:false);
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

	@Override
	public int getWidth() {
		return mVideoView.getWidth();
	}

	@Override
	public int getHeight() {
		return mVideoView.getHeight();
	}

	@Override
	public void setGestureListener(PlayerGestureListener.GestureListener mGestureListener) {
		this.mGestureListener = mGestureListener;
	}

	@Override
	public void onChangeQuality(float minHDRate, float maxHDRate, int videoTrackRenderer) {

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
	public void isPlayingAd(boolean b) {

	}

	@Override
	public void setDownloadMediaData(DownloadMediadata downloadMediaData) {

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

	@Override
	public void setCardData(CardData mCardData) {
		this.mCardData = mCardData;
	}

	@Override
	public boolean isBitrateCappingSupported() {
		return false;
	}

	@Override
	public void setSelectedTrack(TrackData position) {

	}

    @Override
    public void setSelectedTrack(TrackData trackData, int selectedTrack) {

    }

    @Override
	public int getTrackCount(int trackType) {
		return 0;
	}

	@Override
	public List<TrackData> getBitrateCappingTracks() {
		return null;
	}

	@Override
	public int getPositionWhenPaused() {
		return mPositionWhenPaused;
	}


	@Override
	public int getRendererType(int pos){
		return C.TRACK_TYPE_UNKNOWN;
	}

	@Override
	public void setSubtitle(String name) {

	}

	@Override
	public CardDataSubtitles getSubtitles() {
		return null;
	}

	@Override
	public boolean isSubtitlesSupported() {
		return false;
	}

	@Override
	public void showSubtitlesView() {

	}

	@Override
	public void hideSubtitlesView() {

	}

	@Override
	public String getSubtitleName() {
		return null;
	}

	@Override
	public void onTapToZoom() {

	}

	@Override
	public void setmParentLayout(View mParentLayout) {

	}

	@Override
	public void orientationChange(int orientation) {

	}

	@Override
	public void setAudioTrack(String language) {

	}

	@Override
	public String getContentMinBitrate() {
		return null;
	}

	@Override
	public String getContentMaxBitrate() {
		return null;
	}

	@Override
	public String getContentResolution() {
		return null;
	}

	@Override
	public String getContentLanguage() {
		return null;
	}

	@Override
	public List<String> getAudioTracks() {
		return null;
	}

	@Override
	public void onDestroy() {

	}

	@Override
	public void setWaterMarkImageView(ImageView sunnxtContentPlayerWatermark) {

	}

	@Override
	public void setShowWaterMark(boolean showWaterMark) {

	}
}
