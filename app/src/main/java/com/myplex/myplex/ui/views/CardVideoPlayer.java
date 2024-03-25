package com.myplex.myplex.ui.views;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.security.MediaLinkEncrypted;
import com.myplex.model.AlarmData;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPurchaseItem;
import com.myplex.model.CardDataVideos;
import com.myplex.model.CardDataVideosItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.CardResponseData;
import com.myplex.model.LocationInfo;
import com.myplex.model.PlayerStatusUpdate;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.BuildConfig;
import com.myplex.myplex.R;
import com.myplex.myplex.ads.PulseManager;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.media.PlayerListener;
import com.myplex.myplex.media.VideoView;
import com.myplex.myplex.media.VideoViewExtn;
import com.myplex.myplex.media.VideoViewPlayer;
import com.myplex.myplex.media.exoVideo.ExoPlayerView;
import com.myplex.myplex.media.exoVideo.MyplexVideoViewPlayer;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.MOUTracker;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.ReminderListener;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.util.WidevineDrm;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class CardVideoPlayer {
    private static final int MAX_COUNT_FOR_PREF_IDS = 60;
    protected Context mContext;
	protected LayoutInflater mInflator;
	private View mParentLayout;
	private ImageView mPreviewImage;
    private ImageView mReminderImage;
    private ImageView mThumbnailPlay;
	protected TextView mTrailerButton;
    private TextView recordedProgName;
    protected TextView mMinimizeButton;
	protected TextView mBufferPercentage;
	protected RelativeLayout mProgressBarLayout;
	private RelativeLayout mErrorLayout;
	private RelativeLayout mVideoViewParent;
	protected SurfaceView mVideoView;
	private LinearLayout mScoreCardLayout;
    protected CardData mData;
	protected int mPerBuffer;
	private int mWidth;
	private int mHeight;
	protected int mPlayerState;
	protected static int PLAYER_PLAY = 1;
	protected static int PLAYER_STOPPED = 3;
	private static final int WAIT_FORRETRY = 99;
	private PlayerFullScreen mPlayerFullScreen;
	boolean isESTPackPurchased=false;
	private String drmLicenseType="st";
	protected String mNotificationTitle;

	public boolean isYouTubePlayerLaunched() {
		return isYouTubePlayerLaunched;
	}

	private boolean isYouTubePlayerLaunched = false;

	private boolean mTrailerAvailable = false;
	protected boolean isLocalPlayback = false;
	private static final String TAG = "CardVideoPlayer";
//	private Location location;
//	private LocationClient locationClient;
protected int state=0;
	protected int currentDuration = 0;
	private String  download_link,adaptive_link;
	private String camAngleSelect;
    private String langSelect;
    protected String profileSelect;
    protected String mStreamingFormat ;
	protected boolean isFullScreen;
    private boolean isTriler;
	protected boolean isMinimized = false;

	private static final int INTERVAL_RETRY = 10*1000;
	
	private int mAutoRetry = 0;
	private static final int MAX_AUTO_RETRY = 1;
    protected MyplexVideoViewPlayer mVideoViewPlayer;
    protected String streamId;
 	private LinearLayout debugTxtLayout;
    private CardDownloadData mDownloadData = null;

    private APICallback mMediaLinkFetchListener = new APICallback<CardResponseData>() {

		String mAPIErrorMessage;
                @Override
                public void onResponse(APIResponse<CardResponseData> response) {
                    //Log.d(TAG,"onResponse ");
                    if(null == response) {
                        onFailure(new Throwable(mContext.getString(R.string.canot_fetch_url)), APIRequest.ERR_UN_KNOWN);
                        return;
                    }

                    if(null == response.body()){
                        onFailure(new Throwable(mContext.getString(R.string.canot_fetch_url)), APIRequest.ERR_UN_KNOWN);
                        return;
                    }

					mAPIErrorMessage = response.body().message;
					if(response.body().results == null
                            || response.body().results.size() == 0){
						onFailure(new Throwable(mContext.getString(R.string.canot_fetch_url)), APIRequest.ERR_UN_KNOWN);
						mAPIErrorMessage = APIConstants.ERROR_EPMTY_RESULTS;
                        return;
                    }

					String _id = mData._id;
					if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
						_id = mData.globalServiceId;
					}
                    //Log.d(TAG, "success: message- " + response.body().message);
					String contentPartnerName = mData == null || mData.publishingHouse == null || mData.publishingHouse.publishingHouseName == null ? APIConstants.NOT_AVAILABLE : mData.publishingHouse.publishingHouseName;
					if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
							|| APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
						contentPartnerName = mData == null || mData.contentProvider == null ? APIConstants.NOT_AVAILABLE : mData.contentProvider; //3
					}
                    for(CardData data : response.body().results){
                        if(data.videos == null || data.videos.values == null || data.videos.values.size() == 0)
                        {
                            if (data.videos != null && data.videos.status != null && !data.videos.status.equalsIgnoreCase("SUCCESS")) {

                                if (data.videos.message != null && "ERR_USER_NOT_SUBSCRIBED".equalsIgnoreCase(data.videos.status)) {
                                   // AlertDialogUtil.showToastNotification(data.videos.message);
                                    closePlayer();
                                    if (mPlayerStatusListener != null){
                                        mPlayerStatusListener.playerStatusUpdate("ERR_USER_NOT_SUBSCRIBED");
                                    }
									Analytics.mixPanelUnableToFetchVideoLink(mData == null || mData.generalInfo == null || mData.generalInfo.title == null ? APIConstants.NOT_AVAILABLE : mData.generalInfo.title, contentPartnerName, mData == null ? APIConstants.NOT_AVAILABLE : _id, mNid, "status: " + data.videos.status + " message: " + data.videos.message, mNotificationTitle);
									return;
                                }else if(data.videos.message != null && "ERR_PACKAGES_NOT_DEFINED".equalsIgnoreCase(data.videos.status)){
									AlertDialogUtil.showToastNotification(data.videos.message);
									closePlayer();
									if (mPlayerStatusListener != null){
										mPlayerStatusListener.playerStatusUpdate("ERR_PACKAGES_NOT_DEFINED");
									}
									Analytics.mixPanelUnableToFetchVideoLink(mData == null || mData.generalInfo == null || mData.generalInfo.title == null ? APIConstants.NOT_AVAILABLE : mData.generalInfo.title, contentPartnerName, mData == null ? APIConstants.NOT_AVAILABLE : _id, mNid, "status: " + data.videos.status + " message: " + data.videos.message, mNotificationTitle);
									return;
								}
                            }
							mAPIErrorMessage = "status: " + data.videos.status + " message: " + data.videos.message;
                            onFailure(new Throwable(mContext.getString(R.string.canot_fetch_url)), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        CardDataVideos videos = data.videos;
                        if(("SUCCESS".equalsIgnoreCase(videos.status))
                                || (videos.values!=null)
                                || (videos.values.size()>0)){
							String videoType = mData.generalInfo.type;
							//Log.d(TAG, "Video type " + videoType);
							mData.videoInfo = data.videoInfo;
							mData.videos = data.videos;
							if (mData.videoInfo != null && mData.videoInfo.cdnTypes != null) {
								if (mData.videoInfo.cdnTypes.contains(APIConstants.TYPE_YOUTUBE)) {
									String youtubeId = getYoutubeLink(videos);
									if (!TextUtils.isEmpty(youtubeId)) {
										launchYoutubePlayer(youtubeId);
										Analytics.gaNotificationPlayBackSuccess(mNotificationTitle, mNid, mData, profileSelect);
										return;
									}
								}
							}
							chooseLiveStreamType(videos.values,false);
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t,int errorCode) {
                    closePlayer();
                    //Log.d(TAG, "onFailure " + t);
                    String errorMessage = mData != null && mData.generalInfo != null
                            && (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
                            || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type))
                            ? mContext.getString(R.string.canot_fetch_url) : mContext.getString(R.string.canot_fetch_url_videos);

                    if(errorCode == APIRequest.ERR_NO_NETWORK){
                            errorMessage = mContext.getString(R.string.network_error);
                    }

                    AlertDialogUtil.showToastNotification(errorMessage);
					String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
					if (!TextUtils.isEmpty(mAPIErrorMessage)) {
						reason = mAPIErrorMessage;
					}
					String _id = mData._id;
					if(APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)){
						_id = mData.globalServiceId;
					}
					String contentPartnerName = mData == null || mData.publishingHouse == null || mData.publishingHouse.publishingHouseName == null ? APIConstants.NOT_AVAILABLE : mData.publishingHouse.publishingHouseName;
					if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
							|| APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
						contentPartnerName = mData == null || mData.contentProvider == null ? APIConstants.NOT_AVAILABLE : mData.contentProvider; //3
					}

					Analytics.mixPanelUnableToFetchVideoLink(mData == null || mData.generalInfo == null || mData.generalInfo.title == null ? APIConstants.NOT_AVAILABLE : mData.generalInfo.title, contentPartnerName, mData == null ? APIConstants.NOT_AVAILABLE : _id, mNid, reason, mNotificationTitle);

                }
            };

	protected boolean isToShowMediaController = true;

    protected MOUTracker mMouTracker;

	private boolean isPlayingDVR = false;
	private boolean isTimeShiftHelpScreenShowing = false;

	public void setFullScreenListener(PlayerFullScreen mListener){
		this.mPlayerFullScreen = mListener;
	}
	public void setPlayerStatusUpdateListener(PlayerStatusUpdate listener){
		mPlayerStatusListener = listener;
	}
	protected PlayerStatusUpdate mPlayerStatusListener;

    public void onStateChanged(int statePaused, int stopPosition) {
        if(mPlayerListener != null)
            mPlayerListener.onStateChanged(statePaused,stopPosition);
    }

    public void returnFromClickThrough() {

    }

    public void removeCallback(Handler contentProgressHandler) {

    }

	public void setNotificationTitle(String notificationTitle) {
		this.mNotificationTitle = notificationTitle;
	}

	public void setVODContentType(String cotentType) {
		this.mContentType = cotentType;
	}

	public interface PlayerFullScreen{
		void playerInFullScreen(boolean value);
	}
	 public enum VIDEO_PLAYER_TYPE {
	        VIDEOVIEW,
	        EXOVIDEOVIEW
	 }

	    public static VIDEO_PLAYER_TYPE videoPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;

	public CardVideoPlayer(Context context, CardData data,String conId) {
		this.mContext = context;
		this.mData = data;
        streamId = conId;

        int sdkVersion = Build.VERSION.SDK_INT;
        boolean isExoEnabled = PrefUtils.getInstance().getPrefIsExoplayerEnabled();
        boolean isDVRContent = PrefUtils.getInstance().getPrefIsExoplayerDvrEnabled();
//        if (mData != null
//                && mData.generalInfo != null
//                && mData.generalInfo.type != null
//                &&(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
//                || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM))) {

            if( sdkVersion >= 16 && (isDVRContent || isExoEnabled)){
                videoPlayerType = VIDEO_PLAYER_TYPE.EXOVIDEOVIEW;
            } else
            {
                videoPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;
            }

//        }else{
//        videoPlayerType = VIDEO_PLAYER_TYPE.VIDEOVIEW;
//    }

		mInflator = LayoutInflater.from(mContext);
        Util.prepareDisplayinfo((Activity)mContext);
	}

	public View CreatePlayerView(View parentLayout) {
		 
		mParentLayout = parentLayout;
		final View v = mInflator.inflate(R.layout.cardmediasubitemvideo, null);
		mVideoViewParent = (RelativeLayout) v;

		mWidth = ApplicationController.getApplicationConfig().screenWidth;
		mHeight = (mWidth * 9) / 16;
        TextView debugTextView = (TextView)v.findViewById(R.id.debug_textView);
        debugTxtLayout = (LinearLayout)v.findViewById(R.id.debug_textView_layout);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				mWidth, mHeight);
		mPreviewImage = (ImageView) v
				.findViewById(R.id.cardmediasubitemvideo_imagepreview);
		mPreviewImage.setLayoutParams(params);
        if (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW) {
            mVideoView = (SurfaceView) v
                    .findViewById(R.id.cardmediasubitemvideo_videopreview);
            ((VideoViewExtn) mVideoView).resizeVideo(mWidth, mHeight);
        }else{
            mVideoView = (SurfaceView) v.findViewById(R.id.cardmediasubitemvideo_exovideopreview);
            mVideoViewPlayer = (ExoPlayerView) mVideoView;
            mVideoViewPlayer.resizeVideo(mWidth, mHeight);
           // mVideoViewPlayer.setStreamName(id);
            mVideoViewPlayer.setStreamName(streamId);
            mVideoViewPlayer.setDebugTxtView(debugTextView);
          

        }
		//mVideoViewPlayer.setStreamName(mData.generalInfo.title);
		mVideoViewParent.setLayoutParams(params);		
		mVideoView.setLayoutParams(params);
//		mPlayButton.setTypeface(FontUtil.ss_symbolicons_line);

        mTrailerButton = (TextView)v.findViewById(R.id.cardmediasubitemtrailer_play);
//        mTrailerButton.setTypeface(FontUtil.ss_symbolicons_line);
        mVideoViewParent.setOnClickListener(mPlayerClickListener);

		/*if(mData.generalInfo!=null && mData.generalInfo.type != null ){
			if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCHANNEL)
					|| mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCATEGORY)
					|| mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODYOUTUBECHANNEL)){
				mTrailerButton.setVisibility(View.GONE);
			}
		}*/
		recordedProgName = (TextView)v.findViewById(R.id.recordedProgName);
		
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
		
		mMinimizeButton = (TextView)v.findViewById(R.id.cardmedia_minimize);
//		mMinimizeButton.setTypeface(FontUtil.ss_symbolicons_line);

        mReminderImage = (ImageView) v
                .findViewById(R.id.cardmediasubitemvideo_imagereminder);
        mThumbnailPlay = (ImageView) v
                .findViewById(R.id.cardmediasubitemvideo_play_icon);
        mReminderImage.setImageResource(R.drawable.oncard_set_reminder_icon);
        if(mData != null
                && mData.startDate == null){
            mReminderImage.setVisibility(View.GONE);
        }else if(null != mData && null != mData.startDate
                && null != mData.endDate){
            Date startDate = Util.getDate(mData.startDate);
            Date endDate = Util.getDate(mData.endDate);
            Date currentDate = new Date();
            if ((currentDate.after(startDate)
                    && currentDate.before(endDate))
					|| currentDate.after(endDate)) {
                mReminderImage.setImageResource(R.drawable.oncard_play_icon);
            }
        }
        mReminderImage.setTag(mData);
        ReminderListener reminderListener = new ReminderListener(mContext,null,mEpgDatePosition);
        mReminderImage.setOnClickListener(reminderListener);
//        initSportsStatusLayout(v);
		int[] location = new int[2];
		mTrailerButton.getLocationOnScreen(location);
		if(location.length >0)
		{
			//Log.i(TAG, "Toast pos, X:"+mTrailerButton.getBottom()+5+" Y:"+location[1]+10);
			//Util.showToastAt(mContext, "Play Trailer", Util.TOAST_TYPE_INFO, Gravity.TOP|Gravity.LEFT,mTrailerButton.getBottom()+5, location[1]+10);
		}

		mTrailerButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View view) {
						
//						if(mPlayerStatusListener != null){
//							isMinimized =!isMinimized;
//							mPlayerStatusListener.onViewChanged(isMinimized);							
//							return;
//						}

					}
				});
		mMinimizeButton.setVisibility(View.INVISIBLE);
		mMinimizeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mPlayerStatusListener != null){
					mPlayerStatusListener.onViewChanged(true);
				}
				
			}
		});

		mBufferPercentage = (TextView) v
				.findViewById(R.id.carddetaildesc_movename);

		Random rnd = new Random();
		int Low = 100;
		int High = 196;

		int color = Color.argb(255, rnd.nextInt(High - Low) + Low,
				rnd.nextInt(High - Low) + Low, rnd.nextInt(High - Low) + Low);
		mPreviewImage.setBackgroundColor(color);
		mProgressBarLayout = (RelativeLayout) v
				.findViewById(R.id.cardmediasubitemvideo_progressbarLayout);

		mErrorLayout = (RelativeLayout) v
				.findViewById(R.id.cardmediasubitemvideo_error);
		if (null != mData
                    && mData.images != null) {
			for (CardDataImagesItem imageItem : mData.images.values) {
				if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null
						&& imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI) && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
					if (imageItem.link == null
							|| imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
						mPreviewImage.setImageResource(0);
					} else if (imageItem.link != null) {
                        PicassoUtil.with(mContext).load(imageItem.link,mPreviewImage,R.drawable.tv_guide_thumbnail_default);
					}
					break;
				}
			}
		}
		if(null != mData
            && null != mData._id
                && mData._id.equalsIgnoreCase("0"))
		{
			mVideoViewParent.setOnClickListener(null);
			mTrailerButton.setVisibility(View.GONE);
			mVideoView.setVisibility(View.INVISIBLE);
			mProgressBarLayout.setVisibility(View.INVISIBLE);
			mPreviewImage.setScaleType(ScaleType.CENTER);
			mPreviewImage.setBackgroundColor(Color.BLACK);
		}else{
			  UiUtil.showFeedback(mVideoViewParent);
		}
		
		TextView movieTitle = (TextView) v.findViewById(R.id.cardmedia_mini_title);
        if(null != mData
                && null != mData.generalInfo
            && null != mData.generalInfo.title){
            movieTitle.setText(mData.generalInfo.title.toLowerCase());
        }
//		movieTitle.setTypeface(FontUtil.Roboto_Regular);
		
		TextView deleteIcon = (TextView) v.findViewById(R.id.card_title_deleteText);
//		deleteIcon.setTypeface(FontUtil.ss_symbolicons_line);
		deleteIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPlayerStatusListener != null) {
                    mPlayerStatusListener.onCloseFragment();
                }

            }
        });

		View cardmedia_expand_button=  v.findViewById(R.id.cardmedia_expand_button);
		cardmedia_expand_button.setOnClickListener(mPlayerClickListener);
		v.findViewById(R.id.cardmedia_expand_text).setOnClickListener(mPlayerClickListener);
        UiUtil.showFeedback(cardmedia_expand_button);
		return v;
	}


	 	
	public void fetchUrl(String id){			
        isYouTubePlayerLaunched = false;
		recordedProgName.setVisibility(View.GONE);
        mReminderImage.setVisibility(View.GONE);
        mThumbnailPlay.setVisibility(View.GONE);
		mTrailerButton.setVisibility(View.INVISIBLE);
		mProgressBarLayout.setVisibility(View.VISIBLE);
//		mVideoView.setVisibility(View.VISIBLE);			
		mPreviewImage.setVisibility(View.INVISIBLE);			

		if(mErrorLayout != null){
			mErrorLayout.setVisibility(View.GONE);
		}
		
		if(mScoreCardLayout != null){
			mScoreCardLayout.setVisibility(View.INVISIBLE);
		}
		
		/*if(sportsStatusRefresh !=null){
			sportsStatusRefresh.stop();
		}
		*/
		if(checkForLocalPlayback()){
			return;
		}
		//TODO: migrating to MediaLinkEncrypted
        //MediaLink.Params mediaLinkparams = null;
		MediaLinkEncrypted.Params mediaLinkparams=null;
		LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
		if (null != mData
                && null != mData.generalInfo) {
			if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
				String fields = null;
				mediaLinkparams = new MediaLinkEncrypted.Params(mData.globalServiceId, SDKUtils.getInternetConnectivity(mContext), mNid, locationInfo);
				if (mEpgDatePosition - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0
						&& PrefUtils.getInstance().getPrefEnablePastEpg()
						&& mData.startDate != null
						&& mData.endDate != null) {
					mediaLinkparams = new MediaLinkEncrypted.Params(mData.globalServiceId, SDKUtils.getInternetConnectivity(mContext), mNid, mData.startDate , mData.endDate, locationInfo);
				}
			} else if (APIConstants.TYPE_NEWS.equalsIgnoreCase(mData.generalInfo.type)) {
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
                }else{
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.canot_fetch_url));
                    closePlayer();
                    return;
                }
            } else {
                mediaLinkparams = new MediaLinkEncrypted.Params(mData._id, SDKUtils.getInternetConnectivity(mContext), mNid, locationInfo);
            }
            mMedialLink = new MediaLinkEncrypted(mediaLinkparams, mMediaLinkFetchListener);
            APIService.getInstance().execute(mMedialLink);
        }

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

    //MediaLink mMedialLink;
	MediaLinkEncrypted mMedialLink;
        
	private boolean checkForLocalPlayback() {
		
		if(isTriler)
			return false;
		CardDownloadedDataList downloadData = ApplicationController.getDownloadData();
		if (downloadData == null) {
			return false;
		}

		mDownloadData = downloadData.mDownloadedList
				.get(mData._id);

		if (mDownloadData == null) {
			return false;
		}

		boolean isFileExist = false;
		if(mData != null
				&& mData.generalInfo != null
				&& mData.generalInfo.type != null
				&& mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VOD)){
			isFileExist = DownloadUtil.isFileExist(mData._id + ".mp4",mContext);
		}


		if (!isFileExist) {
			if (mPlayerStatusListener != null) {
				mPlayerStatusListener
						.playerStatusUpdate("Download Completed and file doesn't exists, starting player.....");
			}
			Util.removeDownload(mDownloadData.mDownloadId, mContext);
			return false;
		}

		if (mDownloadData.mCompleted && mDownloadData.mPercentage == 0) {
			if (mPlayerStatusListener != null) {
				mPlayerStatusListener
						.playerStatusUpdate("Download failed and removing request and deleting the file");
			}
			closePlayer();
			Util.removeDownload(mDownloadData.mDownloadId, mContext);
			AlertDialogUtil.showToastNotification("Download has failed, Please check if sufficent memory is available.");
			return false;
		}

		if (mPlayerStatusListener != null) {
			mPlayerStatusListener
					.playerStatusUpdate("file exists, starting player.....per download :"
							+ mDownloadData.mPercentage);
		}

		playVideoFile(mDownloadData);
		return true;

	}
	private OnClickListener mPlayerClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//This event is handled in CardDEtails
			/*if(mData.generalInfo!=null){
				if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCHANNEL)
						|| mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCATEGORY)
						|| mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODYOUTUBECHANNEL)){

				return;
				}
			}*/
//			setEPGPlayInitialized(false);
			/*((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
				setFullScreen(true);
				playInLandscape();*/
				playContent();
//			}
			
		}
	};
	
	
	private OnClickListener mShareClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			if(mData == null || mData.generalInfo == null || mData.generalInfo.title == null){
				return;
			}
			/*String username = SharedPrefUtils.getFromSharedPreference(mContext,
					mContext.getString(R.string.devusername));
			String mSharelink = SharedPrefUtils.getFromSharedPreference(
					mContext, mContext.getString(R.string.pref_sharelink));
			String path= Util.takeScreenShot((Activity)mContext);
			if (username == null || mSharelink == null) {
				Util.shareData(mContext, 1, path, "movies");// send message.
				return;
			}*//*
//			Util.shareReferralLinkData(mContext);
			Map<String, String> map = new HashMap<String, String>();
			String param1 = "imagePath";
			map.put(param1, path);
			ReferralRequestUtil.launch(mContext,map);*/

		}
	};
	
	public void playContent(){
        if(ApplicationController.SHOW_PLAYER_LOGS){
            debugTxtLayout.setVisibility(View.VISIBLE);
        }
		if(isMinimized){
			isMinimized =!isMinimized;
			mPlayerStatusListener.onViewChanged(isMinimized);	
			return;
		}
		if(canBePlayed(true)){
//			mAlreadyPlayInitialized = true;
			isTriler = false;
			//FetchUrl();
			fetchUrl(null);
            mVideoViewParent.setOnClickListener(null);
			/*Analytics.startVideoTime();
			Analytics.gaPlayedMovieEvent(mData, 0);*/
			/*if (mData != null
					&& mData.generalInfo != null
					&& mData.generalInfo.type != null
					&&(mData.generalInfo.type
							.equalsIgnoreCase(APIConstants.TYPE_LIVE))) {
				if (mData._id != null && !TextUtils.isEmpty(mData._id)
						&& mData.generalInfo != null
						&& mData.generalInfo.title != null
						&& !TextUtils.isEmpty(mData.generalInfo.title)) {
					Util.setAutoReminder(mContext, new Date(), mData.generalInfo.title, mContext
                            .getResources().getString(
                            R.string.reminder_notification_message_for_livetv), mData._id);
				}
			}*/
		}
		
	}
	private boolean lastWatchedStatus = false;
	public boolean mAlreadyPlayInitialized = false;

	public void closePlayer() {
		mPlayerState = PLAYER_STOPPED;
        mAlreadyPlayInitialized = false;
        stopMOUTracking();
		if (mVideoViewPlayer != null) {
			mVideoViewPlayer.closeSession();
		}
		mPerBuffer = 0;
		if (mVideoViewPlayer != null) {
			mVideoViewPlayer.hideMediaController();
		}
		/*if(mData != null && mData.generalInfo != null
				&& (mData.generalInfo.type.equalsIgnoreCase(APIConstants.CARD_TYPE_VOD)
						|| mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCATEGORY)
						|| mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCHANNEL))){
			mVideoViewParent.setOnClickListener(mPlayerClickListener);
			mPlayButton.setVisibility(View.INVISIBLE);
			mTrailerButton.setVisibility(View.GONE);
			mShareButton.setVisibility(View.INVISIBLE);
			mProgressBarLayout.setVisibility(View.GONE);
			mPreviewImage.setVisibility(View.INVISIBLE);
			mVideoViewParent.setEnabled(true);
			if(mScoreCardLayout != null){
				mScoreCardLayout.setVisibility(View.INVISIBLE);
			}
			if(mScoreCardLayout != null){
				mScoreCardLayout.setVisibility(View.INVISIBLE);
			}
//			if(!mContext.getResources().getBoolean(R.bool.isTablet)){
				((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//			}
			return;
		}*/
		mVideoViewParent.setOnClickListener(mPlayerClickListener);
//		mPlayButton.setVisibility(View.VISIBLE);
//		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
//		mShareButton.setVisibility(View.VISIBLE);
		mProgressBarLayout.setVisibility(View.GONE);
		mPreviewImage.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
		mVideoViewParent.setEnabled(true);
        if(APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)){
            mReminderImage.setVisibility(View.GONE);
            mThumbnailPlay.setVisibility(View.GONE);
            mThumbnailPlay.setOnClickListener(mPlayerClickListener);
        }else if(APIConstants.TYPE_VOD.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mData.generalInfo.type)
				|| APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type)
				|| APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(mData.generalInfo.type)){
            mReminderImage.setVisibility(View.VISIBLE);
            mReminderImage.setImageResource(R.drawable.oncard_play_icon);
            mReminderImage.setOnClickListener(mPlayerClickListener);
        }

        //Making stream music volume enable after the player closed
		if(mScoreCardLayout != null){
			mScoreCardLayout.setVisibility(View.VISIBLE);
		}
		if(mScoreCardLayout != null){
			mScoreCardLayout.setVisibility(View.VISIBLE);
		}
//		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		}
				
	}

	public void FetchUrl() {

		isLocalPlayback = false;

//
//		if(mData._id == null || !AllowedContentIdList.isAllowed(mData._id) ){
//
//			Util.showToast(mContext, "Your country is not allowed for this content.",Util.TOAST_TYPE_ERROR);
//			return;
//		}

//		location = locationClient.getLastLocation();
//		if(location!=null)
//			Log.d("amlan",location.getLatitude()+":"+location.getLongitude());

		mTrailerButton.setVisibility(View.INVISIBLE);
		mProgressBarLayout.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPreviewImage.setVisibility(View.INVISIBLE);
		if(mScoreCardLayout != null){
			mScoreCardLayout.setVisibility(View.INVISIBLE);
		}
		/*if(sportsStatusRefresh !=null){
			sportsStatusRefresh.stop();
		}
		MediaUtil.setUrlEventListener(new MediaUtilEventListener() {

            @Override
            public void urlReceived(boolean aStatus, String url, String message, String statusCode) {
                if (!aStatus) {
                    closePlayer();

                    String msg = "Failed in fetching the url.";

                    if (!TextUtils.isEmpty(message)) {
                        msg = message;
                    }

//					Util.showToast(mContext, msg,Util.TOAST_TYPE_ERROR);

                    if (statusCode != null && statusCode.equalsIgnoreCase("ERR_USER_NOT_SUBSCRIBED")) {

                        PackagePopUp popup = new PackagePopUp(mContext, (View) mParentLayout.getParent());
                        myplexapplication.getCardExplorerData().cardDataToSubscribe = mData;
                        popup.showPackDialog(mData, ((Activity) mContext).getActionBar().getCustomView());

                    }

                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("Failed in fetching the url.");
                        mixPanelUnableToPlayVideo(Analytics.FAILED_TO_FETCH_URL);
                    }
//					Toast.makeText(mContext, "Failed in fetching the url.",
//							Toast.LENGTH_SHORT).show();
                    return;
                }
                if (url == null) {
                    closePlayer();
                    if (mPlayerStatusListener != null) {
                        mPlayerStatusListener.playerStatusUpdate("No url to play.");
                        mixPanelUnableToPlayVideo(Analytics.NO_URL_TO_PLAY);
                    }
                    Util.showToast(mContext, "No url to play.", Util.TOAST_TYPE_ERROR);
//					Toast.makeText(mContext, "No url to play.",
//							Toast.LENGTH_SHORT).show();
                    return;
                }
				*//*if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Url Received :: "+url);
				}*//*
                if (isESTPackPurchased || url.contains("_est_")) {
                    url = url.replace("widevine:", "http:");
//					url = "http://122.248.233.48/wvm/100_ff_4_medium.wvm";
//					url = "https://demostb.s3.amazonaws.com/myplex2.apk";
                    closePlayer();
                    if (Util.getSpaceAvailable() >= 1) {
                        if (Util.isWifiEnabled(mContext)) {
                            Util.startDownload(url, mData, mContext);
                        } else {
                            Util.showToast(mContext, "Downloading is supported only on Wifi, please turn on wifi and try again.", Util.TOAST_TYPE_INFO);
                        }
                    } else {
                        Util.showToast(mContext, "Download failed due to insufficent memory, please free space up to 1GB to start download", Util.TOAST_TYPE_INFO);
                    }
                    return;
                } else {
                    drmLicenseType = "st";
                }

                if (mData.content != null && mData.content.drmEnabled) {
                    String licenseData = "clientkey:" + myplexapplication.getDevDetailsInstance().getClientKey() + ",contentid:" + mData._id + ",type:" + drmLicenseType + ",profile:0";

                    byte[] data;
                    try {
                        data = licenseData.getBytes("UTF-8");
                        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                        Settings.USER_DATA = base64;
                        Settings.DEVICE_ID = myplexapplication.getDevDetailsInstance().getClientDeviceId();
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (!lastWatchedStatus) {
                    myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);
                    lastWatchedStatus = true;
                }
                Util.showToastAt(mContext.getString(R.string.adultwarning), mData, mContext);
                Uri uri;
//				uri = Uri.parse("rtsp://46.249.213.87:554/playlists/bollywood-action_qcif.hpl.3gp");
//				uri = Uri.parse("http://59.162.166.211:8080/player/3G_H264_320x240_600kbps.3gp");
//				uri = Uri.parse("http://122.248.233.48/wvm/100_ff_5.wvm");
                uri = Uri.parse(url);
                // Toast.makeText(getContext(), "URL:"+url,
                // Toast.LENGTH_SHORT).show();
                if (mPlayerStatusListener != null) {
                    mPlayerStatusListener.playerStatusUpdate("Playing :: " + url);
                }


                // if movie then fetch elapse time
                // default init player

                initializeVideoPlay(uri);

            }

            @Override
            public void lastPausedTimeFetched(int ellapseTime) {
                if (ellapseTime > 60) {
                    if (mVideoViewPlayer == null) {
                        if (videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
                            mVideoViewPlayer = new VideoViewPlayer((VideoView) mVideoView, mContext, null, StreamType.VOD);
                    }
//					mVideoViewPlayer.setPositionWhenPaused(ellapseTime*1000);
                }
            }


        });


//		*//*boolean*//* lastWatchedStatus=false;
		for(CardData data:myplexapplication.getUserProfileInstance().lastVisitedCardData)
		{
			if(data._id.equalsIgnoreCase(mData._id))
			{
				lastWatchedStatus=true;

			}
		}
		*//*if(!lastWatchedStatus)
			myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);*//*

		String expiryTime=null;
		boolean allowPlaying=true;
		if(mData.currentUserData!=null)
        {
			for(CardDataPurchaseItem data:mData.currentUserData.purchase)
            {
                if(data.type.equalsIgnoreCase("download") || data.type.equalsIgnoreCase("est")){
                    isESTPackPurchased=true;

                }
                expiryTime=data.validity;
            }
        }
		//Following check is not needed for now since DRM
*//*		if(expiryTime!=null)
		{
			if(!Util.isTokenValid(expiryTime))
			{
				closePlayer();
				Util.showToast(mContext, "Your Subscription has been expired.",Util.TOAST_TYPE_ERROR);
				return;
			}
			else
			{
				allowPlaying=true;
			}
		}
		else
		{
			allowPlaying=true;
		}*//*

		if(allowPlaying)
		{
	        String qualityType = new String();
	        String streamingType = new String();

	        streamingType = APIConstants.STREAMNORMAL;

	        if(mData.content !=null && mData.content.drmEnabled)
	        {
	        	qualityType = APIConstants.VIDEOQUALTYSD;
	        	streamingType = APIConstants.STREAMADAPTIVE;
	        }
	        else
	        	qualityType = APIConstants.VIDEOQUALTYLOW;


	        if(Util.isWifiEnabled(mContext))
	        {
	        	if(mData.content !=null && mData.content.drmEnabled){
	        		qualityType= APIConstants.VIDEOQUALTYSD;
	        		streamingType = APIConstants.STREAMADAPTIVE;
	        	}
	        	else
	        		qualityType= APIConstants.VIDEOQUALTYHIGH;

	        	if(mContext.getResources().getBoolean(R.bool.isTablet) && mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(APIConstants.CONTENT_SPORTS_LIVE)){
	        		// for tablet use very high quality link.
	        		qualityType= APIConstants.VIDEOQUALTYVERYHIGH;
	        	}
	        }

	        if(myplexapplication.mDownloadList != null){
	        	if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Download Details are available...");
				}
				CardDownloadData mDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(mData._id);
				if(mDownloadData!=null){

					// for local playback
					if(mDownloadData.mCompleted && Util.isFileExist(mData._id+".wvm"))
					{
						// download complete
						if(mDownloadData.mPercentage==0)
						{
							if(mPlayerStatusListener != null){
								mPlayerStatusListener.playerStatusUpdate("Download failed and removing request and deleting the file");
							}
							closePlayer();
							Util.removeDownload(mDownloadData.mDownloadId, mContext);
							Util.showToast(mContext, "Download has failed, Please check if sufficent memory is available.",Util.TOAST_TYPE_ERROR);

						}
						else{
							if(mPlayerStatusListener != null){
								mPlayerStatusListener.playerStatusUpdate("Download inprogess and file exists, starting player.....");
							}
							playVideoFile(mDownloadData);
						}
					}
					else
					{
						// download inprogress
						if(Util.isFileExist(mData._id+".wvm"))
						{
							if(mPlayerStatusListener != null){
								mPlayerStatusListener.playerStatusUpdate("Download Completed and file exists, starting player.....");
							}
							playVideoFile(mDownloadData);
						}
						else{
							if(mPlayerStatusListener != null){
								mPlayerStatusListener.playerStatusUpdate("Download Completed and file doesn't exists, starting player.....");
							}
							Util.removeDownload(mDownloadData.mDownloadId, mContext);
							MediaUtil.setContext(mContext);
							MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased,APIConstants.STREAMINGFORMATHLS);

						}
					}
				}
				else{

					// streaming
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("Download Details for this content not available, so requesting url...");
					}
					MediaUtil.setContext(mContext);
					MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased,APIConstants.STREAMINGFORMATHLS);
					*//*if(mData.generalInfo != null && mData.generalInfo.type != null ){
						if(!mData.generalInfo.type.equalsIgnoreCase("live")){
							// Its a live video Dont fetch resumed status
							MediaUtil.getPlayerState(mData._id);
						}
					}*//*
				}
			}
	        else
	        {
	        	// streaming Movie
	        	if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Download Details not available, so requesting url...");
				}
	        	MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased,APIConstants.STREAMINGFORMATHLS);
	        	*//*if(mData.generalInfo != null && mData.generalInfo.type != null ){
					if(!mData.generalInfo.type.equalsIgnoreCase("live")){
						// Its a live video Dont fetch resumed status
						MediaUtil.getPlayerState(mData._id);
					}
				}*//*
	        }
	      }*/
	}
	protected void initializeVideoPlay(Uri uri ) {
		//Log.d(TAG, "video url "+uri);
        VideoViewPlayer.StreamType streamType = VideoViewPlayer.StreamType.VOD;
		if (mVideoViewPlayer == null) {
			Log.d("VideoViewPlayer","new mVideoViewPlayer");
			if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
			    mVideoViewPlayer = new VideoViewPlayer((VideoView)mVideoView, mContext, uri,streamType);
			// mVideoViewPlayer.openVideo();
            if(APIConstants.STREAMINGFORMATHLS.equals(mStreamingFormat)) {
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HLS);
            }else if(APIConstants.STREAMINGFORMATHTTP.equals(mStreamingFormat)){
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
            }else if(APIConstants.STREAMINGFORMATRTSP.equals(mStreamingFormat)){
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.RTSP);
            }

			mVideoViewPlayer.setPlayerListener(mPlayerListener);
			mVideoViewPlayer.setUri(uri, streamType);
		} else {
			Log.d("VideoViewPlayer", "old mVideoViewPlayer");
            if(APIConstants.STREAMINGFORMATHLS.equals(mStreamingFormat)) {
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HLS);
            }else if(APIConstants.STREAMINGFORMATHTTP.equals(mStreamingFormat)){
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
            }else if(APIConstants.STREAMINGFORMATRTSP.equals(mStreamingFormat)){
                mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.RTSP);
            }
			mVideoViewPlayer.setPlayerListener(mPlayerListener);
			mVideoViewPlayer.setUri(uri, streamType);
		}
		mVideoViewPlayer.setOnLicenseExpiryListener(onLicenseExpiryListener);
		mVideoViewPlayer.hideMediaController();
		mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);

        if(mData != null
				&& mData.generalInfo!= null
				&& mData.generalInfo.type != null
				&& mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VOD)){
			mVideoViewPlayer.setPositionWhenPaused(0);
		}

		mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
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
        });
//		mVideoViewPlayer.goToTime(MediaUtil.ELLAPSE_TIME);
	}
private void playVideoFile(CardDownloadData mDownloadData){

	isLocalPlayback  = true;
	drmLicenseType="lp";
	String url="file://"+mDownloadData.mDownloadPath;
	

	if(mData.content !=null && mData.content.drmEnabled)
	{
		
		String licenseData="clientkey:"+PrefUtils.getInstance().getPrefClientkey()+",contentid:"+mData._id+"," +
                "type:"+drmLicenseType+"," +
                ""+getDrmProfileString() +","+ APIConstants.getDRMDeviceParams();
		
		byte[] data;
		try {
			data = licenseData.getBytes("UTF-8");
			String base64 = Base64.encodeToString(data, Base64.DEFAULT);
			WidevineDrm.Settings.USER_DATA=base64;
			WidevineDrm.Settings.DEVICE_ID = PrefUtils.getInstance().getPrefDeviceid();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	Uri uri ;
	uri = Uri.parse(url);
	if(mDownloadData.mCompleted)
	{
		DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		uri=manager.getUriForDownloadedFile(mDownloadData.mDownloadId);
	}
	if(mPlayerStatusListener != null){
		mPlayerStatusListener.playerStatusUpdate("Playing :: "+url);
	}
    VideoViewPlayer.StreamType streamType = VideoViewPlayer.StreamType.VOD;
	if (mVideoViewPlayer == null) {
		Log.d("VideoViewPlayer","new mVideoViewPlayer");
		if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
		     mVideoViewPlayer = new VideoViewPlayer((VideoView)mVideoView,mContext, uri, streamType);
		//mVideoViewPlayer.openVideo();
        mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
		mVideoViewPlayer.setPlayerListener(mPlayerListener);
		mVideoViewPlayer.setUri(uri, streamType);
	} else {
        mVideoViewPlayer.setStreamProtocol(MyplexVideoViewPlayer.StreamProtocol.HTTP_PROGRESSIVEPLAY);
		Log.d("VideoViewPlayer","old mVideoViewPlayer");
		mVideoViewPlayer.setPlayerListener(mPlayerListener);
		mVideoViewPlayer.setUri(uri, streamType);
	}
	mVideoViewPlayer.setOnLicenseExpiryListener(onLicenseExpiryListener);
	mVideoViewPlayer.hideMediaController();
	mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
	mVideoView.setOnTouchListener(new View.OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			if(!isMinimized && videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW){
				mVideoViewPlayer.onTouchEvent(event);
			}
			return false;
		}
	});

	int ellapseTime = PrefUtils.getInstance().getInt(mData._id,0);
	mVideoViewPlayer.setPositionWhenPaused(ellapseTime * 1000);
}
/*	private void FetchTrailerUrl(String contentId)
	{
		
//		if(mData._id == null || !AllowedContentIdList.isAllowed(mData._id) ){
//			
//			Util.showToast(mContext, "Your country is not allowed for this content.",Util.TOAST_TYPE_ERROR);
//			return;
//		}
		for(CardData data:myplexapplication.getUserProfileInstance().lastVisitedCardData)
		{
			if(data._id.equalsIgnoreCase(mData._id))
			{
				lastWatchedStatus=true;
			}
		}
		
		mPlayButton.setVisibility(View.INVISIBLE);
		mTrailerButton.setVisibility(View.INVISIBLE);
		mShareButton.setVisibility(View.INVISIBLE);
		mProgressBarLayout.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPreviewImage.setVisibility(View.INVISIBLE);
		
        String qualityType = new String();
        String streamingType = new String();
        
        streamingType = APIConstants.STREAMNORMAL;
    	qualityType = APIConstants.VIDEOQUALTYHIGH;
        
        if(Util.isWifiEnabled(mContext))
        {
    		qualityType= APIConstants.VIDEOQUALTYHIGH;
        }
        MediaUtil.setUrlEventListener(new MediaUtilEventListener() {

			@Override
			public void urlReceived(boolean aStatus, String url, String message, String statusCode ) {
				if (!aStatus) {
					closePlayer();

					String msg ="Failed in fetching the url.";

					if(!TextUtils.isEmpty(message)){
						msg = message;
					}

					Util.showToast(mContext, msg,Util.TOAST_TYPE_ERROR);

					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("Failed in fetching the url.");
						mixPanelUnableToPlayVideo(Analytics.FAILED_TO_FETCH_URL);
					}
					return;
				}
				if (url == null) {
					closePlayer();
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("No url to play.");
					}
					mixPanelUnableToPlayVideo(Analytics.NO_URL_TO_PLAY);
					Util.showToast(mContext, "No url to play.",Util.TOAST_TYPE_ERROR);
					return;
				}
				if(!lastWatchedStatus){
					myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);
					lastWatchedStatus = true;
				}
				Uri uri ;
//				uri = Uri.parse("rtsp://46.249.213.87:554/playlists/bollywood-action_qcif.hpl.3gp");
//				uri = Uri.parse("http://59.162.166.211:8080/player/3G_H264_320x240_600kbps.3gp");
//				uri = Uri.parse("http://122.248.233.48/wvm/100_ff_5.wvm");
				uri = Uri.parse(url);
				// Toast.makeText(getContext(), "URL:"+url,
				// Toast.LENGTH_SHORT).show();
				if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Playing :: "+url);
				}
                VideoViewPlayer.StreamType streamType = StreamType.VOD;
				if (mVideoViewPlayer == null) {
					if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
					mVideoViewPlayer = new VideoViewPlayer((VideoView)mVideoView,mContext, uri, streamType);
					//mVideoViewPlayer.openVideo();
					mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
					mVideoViewPlayer.setUri(uri, streamType);
				} else {
					mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
					mVideoViewPlayer.setUri(uri, streamType);
				}
				mVideoViewPlayer.hideMediaController();
				mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
				mVideoView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View arg0, MotionEvent event) {
						if(!isMinimized && videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW){
							mVideoViewPlayer.onTouchEvent(event);
						}
						return false;
					}
				});
			}

			@Override
			public void lastPausedTimeFetched(int ellapseTime) {}
		});
        MediaUtil.setContext(mContext);
        MediaUtil.getVideoUrl(contentId, qualityType, streamingType, isESTPackPurchased, APIConstants.STREAMINGFORMATHTTP);
	}
	*/
/*
	private void mixPanelUnableToPlayVideo(String error) {
    	
       	int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  cardData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		String contentName = cardData.generalInfo.title;
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.PROPERTY_CONTENT_NAME, contentName);
		params.put(Analytics.PROPERTY_CONTENT_ID, cardData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY, Analytics.movieOrLivetv(cardData.generalInfo.type));
		params.put(Analytics.REASON_FAILURE, error);
		String event = Analytics.EVENT_UNABLE_TO_PLAY + Analytics.EMPTY_SPACE + contentName;
		Analytics.trackEvent(Analytics.EventPriority.HIGH, event, params);
		//Analytics.createEventGA(easyTracker, Analytics.EVENT_PLAY,Analytics.CONTENT_PLAY_ERROR,contentName );
    }
	
	public View CreateTabletPlayerView(View parentLayout) {

		mWidth = myplexapplication.getApplicationConfig().screenWidth;
		mWidth = (myplexapplication.getApplicationConfig().screenWidth/3)*2;
		int marginleft = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
		mWidth -= marginleft*2;
		mHeight = (mWidth * 9)/16;

		mParentLayout = parentLayout;
		LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(mWidth,mHeight);
		layoutparams.setMargins(marginleft, marginleft, marginleft, marginleft);
		mParentLayout.setLayoutParams(layoutparams);
		View v = mInflator.inflate(R.layout.cardmediasubitemvideo, null);
		mVideoViewParent = (RelativeLayout) v;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);
		mPreviewImage = (FadeInNetworkImageView) v.findViewById(R.id.cardmediasubitemvideo_imagepreview);
		mPreviewImage.setLayoutParams(params);
		if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW){
			mVideoView = (SurfaceView) v.findViewById(R.id.cardmediasubitemvideo_videopreview);		
			((VideoViewExtn) mVideoView).resizeVideo(mWidth, mHeight);
		}else{
			mVideoView = (SurfaceView) v.findViewById(R.id.cardmediasubitemvideo_exovideopreview);		
			mVideoViewPlayer = (ExoVideoView) mVideoView;
            mVideoViewPlayer.resizeVideo(mWidth, mHeight);
		}
	
		mVideoView.setLayoutParams(params);
		mPlayButton = (TextView) v.findViewById(R.id.cardmediasubitemvideo_play);
		mPlayButton.setTypeface(UiUtil.ss_symbolicons_line);
		if(mData.generalInfo!=null && mData.generalInfo.type != null ){
			if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCHANNEL)
					|| mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCATEGORY)
					|| mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODYOUTUBECHANNEL)){
				mPlayButton.setVisibility(View.GONE);				
			}
		}
		mTrailerButton = (TextView)v.findViewById(R.id.cardmediasubitemtrailer_play);
		mTrailerButton.setTypeface(UiUtil.ss_symbolicons_line);
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
		if(mData.generalInfo!=null && mData.generalInfo.type != null ){
			if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCHANNEL)){
				mTrailerButton.setVisibility(View.GONE);				
			}
		}
		mShareButton = (TextView)v.findViewById(R.id.cardmediasubitem_share);
		mShareButton.setTypeface(UiUtil.ss_symbolicons_line);
		mShareButton.setOnClickListener(mShareClickListener);
		
		recordedProgName = (TextView)v.findViewById(R.id.recordedProgName);
		
		initSportsStatusLayout(v);
		mTrailerButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mTrailerAvailablemData !=null && mData.relatedMultimedia !=null &&
						mData.relatedMultimedia.values !=null
						 && mData.relatedMultimedia.values.size() >0)
				{
					for (CardDataRelatedMultimediaItem mmItem : mData.relatedMultimedia.values) {
						{
							if(mmItem.content !=null && mmItem.content.categoryName !=null && mmItem.content.categoryName.equalsIgnoreCase("trailer") && mmItem.generalInfo !=null && mmItem.generalInfo._id !=null)
							{
								Analytics.isTrailer = true;
								Analytics.startVideoTime();
																
								FetchTrailerUrl(mmItem.generalInfo._id);
								mVideoViewParent.setOnClickListener(null);
								break;
							}
						}
					}
				}
				
			}
		});
		
		mBufferPercentage = (TextView) v.findViewById(R.id.carddetaildesc_movename);

		Random rnd = new Random();
		int Low = 100;
		int High = 196;

		int color = Color.argb(255, rnd.nextInt(High - Low) + Low,
				rnd.nextInt(High - Low) + Low, rnd.nextInt(High - Low) + Low);
		mPreviewImage.setBackgroundColor(color);
		mProgressBarLayout = (RelativeLayout) v
				.findViewById(R.id.cardmediasubitemvideo_progressbarLayout);

		mErrorLayout = (RelativeLayout) v
				.findViewById(R.id.cardmediasubitemvideo_error);
		
		if (mData.images != null) {
			for (CardDataImagesItem imageItem : mData.images.values) {
				if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null
						&& imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)
						&& imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
					if (imageItem.link == null
							|| imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
						mPreviewImage.setImageResource(0);
					} else if (imageItem.link != null) {
						mPreviewImage.setImageUrl(imageItem.link,
								MyVolley.getImageLoader());
					}
					break;
				}
			}
		}
		mVideoViewParent.setOnClickListener(mPlayerClickListener);
		if(mData._id.equalsIgnoreCase("0"))
		{
			mVideoViewParent.setOnClickListener(null);
			mPlayButton.setVisibility(View.GONE);
			mTrailerButton.setVisibility(View.GONE);
			mShareButton.setVisibility(View.GONE);
			mVideoView.setVisibility(View.INVISIBLE);
			mProgressBarLayout.setVisibility(View.INVISIBLE);
			mPreviewImage.setScaleType(ScaleType.CENTER);
			mPreviewImage.setBackgroundColor(Color.BLACK);
		}
		// mPlay.setOnClickListener(mPlayListener);
		return v;
	}
*/

    public void stopMOUTracking(){
        if(mMouTracker != null) {
            mMouTracker.stoppedAt();
            mMouTracker = null;
        }
    }

	protected boolean isPlayBackStartedAlready = false;
	protected String mContentType;
	protected PlayerListener mPlayerListener = new PlayerListener() {


        @Override
        public void onStateChanged(int state , int elapsedTime)
        {
            CardVideoPlayer.this.state = PlayerListener.STATE_PAUSED;
            currentDuration = elapsedTime;
            switch (state) {
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
			//Log.d(TAG, "onSeekComplete");
            if(mPlayerStatusListener != null){
                mPlayerStatusListener.playerStatusUpdate("onSeekComplete");
            }

            if(mData != null
                    && mData.generalInfo != null
					&& isPlayingDVR
                    && !TextUtils.isEmpty(mData.globalServiceName)
                    && (APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type) ||
                        APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type))){
				//Log.d(TAG, "onSeekComplete:  gaPlayedTimeShiftChannel- " + mData.globalServiceName);
                Analytics.gaPlayedTimeShiftChannel(mData.globalServiceName.toLowerCase());
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
            if (mVideoViewPlayer.isPlaying() && currentseekposition > 500) {
                mPerBuffer = 0;
                mVideoViewPlayer.deregisteronBufferingUpdate();
                mProgressBarLayout.setVisibility(View.GONE);
                mVideoViewPlayer.showMediaController();
                mPlayerState = PLAYER_PLAY;
                if(mPlayerStatusListener != null){
                    mPlayerStatusListener.playerStatusUpdate("Buffering ended");
					mPlayerStatusListener.playerStatusUpdate("Show Helpscreen");
                }
//			/*if(!mContext.getResources().getBoolean(R.bool.isTablet)){
				if(!isTimeShiftHelpScreenShowing){
					((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				}
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
            }else if(mVideoViewPlayer.isPlaying()
                    &&(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM))){
                mPerBuffer = 0;
				mVideoViewPlayer.deregisteronBufferingUpdate();
                mProgressBarLayout.setVisibility(View.GONE);
                mVideoViewPlayer.showMediaController();
                mPlayerState = PLAYER_PLAY;
                if(mPlayerStatusListener != null){
                    mPlayerStatusListener.playerStatusUpdate("Buffering ended");
					mPlayerStatusListener.playerStatusUpdate("Show Helpscreen");
                }
//			/*if(!mContext.getResources().getBoolean(R.bool.isTablet)){
				if(!isTimeShiftHelpScreenShowing){
					((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				}
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
        public boolean onError(MediaPlayer mp, int arg1, int arg2, String errorMessage ,String stackTrace) {
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
            if(mPlayerStatusListener != null){
                mPlayerStatusListener.playerStatusUpdate("Play complete :: ");
            }
            closePlayer();

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
		public boolean onError(Object errorInfo,String errorTrace) {
			return false;
		}

		@Override
		public void onSubtitleChanged(String subtitleTrack) {

		}

	};



	private PulseManager.ClickThroughCallback clickThroughCallback;
    public void setOnClickThroughCallback(PulseManager.ClickThroughCallback callback) {
        clickThroughCallback = callback;
    }
    private boolean isEPGPlayBackInitialized = false;
    public void setEPGPlayInitialized(boolean value){
        isEPGPlayBackInitialized = value;
    }
    public boolean getEPGPlayBackInitialized(){
        return isEPGPlayBackInitialized;
    }
	public void resumePreviousOrientaionTimer(){
		if(mTimer != null ){
			mTimer.cancel();
		}
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                resumePreviousOrientaion();

            }
        }, 5000);
    }
	private Timer mTimer;
	private void  resumePreviousOrientaion(){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				if(isMinimized){
					return;
				}
				if(mPlayerState == PLAYER_PLAY){
					((BaseActivity)mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				}else{
					((BaseActivity)mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		});
	}

//	}
	public int getScreenOrientation(){
		Display getOrient = ((Activity) mContext).getWindowManager().getDefaultDisplay();
		if(getOrient.getWidth() < getOrient.getHeight()){
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; 
		}
		return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		
	}
	public void playInLandscape() {
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play in lanscape :: ");
		}
		if(mVideoViewPlayer != null){
			mVideoViewPlayer.playerInFullScreen(true);
		}
		int statusBarHeight = UiUtil.getStatusBarHeight(mContext);

//		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
		int derviedWidth = ApplicationController.getApplicationConfig().screenHeight;
		int derviedHeight = ApplicationController.getApplicationConfig().screenWidth - statusBarHeight;
		// only for live

		int modifiedWidth = derviedWidth;
		if (mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase("live"))
			modifiedWidth = (derviedHeight * 4) / 3;
//		}
		/*if(mContext.getResources().getBoolean(R.bool.isTablet)){
			derviedHeight = myplexapplication.getApplicationConfig().screenHeight - statusBarHeight;
			LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(derviedWidth,derviedHeight);
			mParentLayout.setLayoutParams(layoutparams);
		}*/
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(derviedWidth,derviedHeight);
		RelativeLayout.LayoutParams parent = new RelativeLayout.LayoutParams(derviedWidth,derviedHeight);

		//Log.d(TAG, "width = " + derviedWidth + " * height = " + derviedHeight);
		// only for live
		if(mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase("live"))
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mVideoViewParent.setLayoutParams(parent);
        mVideoViewParent.setEnabled(false);

		mVideoView.setLayoutParams(params);
		 if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW){
			   ((VideoViewExtn) mVideoView).resizeVideo(modifiedWidth, derviedHeight);
		 }else{
			 mVideoViewPlayer.resizeVideo(modifiedWidth, derviedHeight);
		 }
		((BaseActivity) mContext).hideActionBar();
		if(mPlayerFullScreen != null){
			mPlayerFullScreen.playerInFullScreen(true);
		}
		// mParentLayout.setLayoutParams(mParentLayoutParams);
		mParentLayout.setBackgroundColor(Color.BLACK);
		setFullScreen(true);
		if(mMinimizeButton != null){
			mMinimizeButton.setVisibility(View.INVISIBLE);
		}
	}
	public void playInPortrait() {
		if(mPlayerStatusListener != null){
            mPlayerStatusListener.playerStatusUpdate("Play in portrait :: ");
		}
		if(mVideoViewPlayer != null) {
            mVideoViewPlayer.playerInFullScreen(false);
		}
		/*if(mContext.getResources().getBoolean(R.bool.isTablet)){
			mWidth = ApplicationController.getApplicationConfig().screenWidth;
			mWidth = (ApplicationController.getApplicationConfig().screenWidth/3)*2;
			int marginleft = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
			mWidth -= marginleft*2;
			mHeight = (mWidth * 9)/16;

			LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(mWidth,mHeight);
			layoutparams.setMargins(marginleft, marginleft, marginleft, marginleft);
			mParentLayout.setLayoutParams(layoutparams);
			((MainBaseOptions) mContext).showActionBar();
		}*/

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);

		//Log.d(TAG, "width = " + mWidth + " * height = " + mHeight);
		mVideoViewParent.setLayoutParams(params);
		mVideoView.setLayoutParams(params);
		if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW){
			  ((VideoViewExtn) mVideoView).resizeVideo(mWidth, mHeight);

		}else{
			mVideoViewPlayer.resizeVideo(mWidth, mHeight);
		}

		if(mPlayerFullScreen != null){
			mPlayerFullScreen.playerInFullScreen(false);
		}
		// mParentLayout.setLayoutParams(params);
		setFullScreen(false);
		if(mVideoViewPlayer!=null && isToShowMediaController){
            mVideoViewPlayer.showMediaController();
        }
	}
	
	public void minimize(){
		isMinimized = true;
		mVideoViewPlayer.setMinized(true);
		mWidth = (int)(ApplicationController.getApplicationConfig().screenWidth*0.45);
		mHeight = (mWidth * 9) / 16;
		
		if(mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)){
			mHeight = (mWidth * 3) / 4;
		}
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				mWidth, mHeight);		
		mPreviewImage.setLayoutParams(params);
		mVideoViewParent.findViewById(R.id.cardmedia_mini).setVisibility(View.VISIBLE);
		mVideoView.setLayoutParams(params);
		if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW){
			  ((VideoViewExtn) mVideoView).resizeVideo(mWidth, mHeight);
		}else{
			mVideoViewPlayer.resizeVideo(mWidth, mHeight);
		}
		
		if(mVideoViewPlayer != null){
			mVideoViewPlayer.hideMediaController();
		}
		if(mMinimizeButton != null){
			mMinimizeButton.setVisibility(View.INVISIBLE);
		}
	}
	
	
	public void maximize(){
		
		isMinimized = false;
		mVideoViewPlayer.setMinized(false);
		mWidth = ApplicationController.getApplicationConfig().screenWidth;
		mHeight = (mWidth * 9) / 16;
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				mWidth, mHeight);		
		mPreviewImage.setLayoutParams(params);
		mVideoViewParent.findViewById(R.id.cardmedia_mini).setVisibility(View.GONE);
		mVideoView.setLayoutParams(params);
        if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW){
			  ((VideoViewExtn) mVideoView).resizeVideo(mWidth, mHeight);
		}else{
			mVideoViewPlayer.resizeVideo(mWidth, mHeight);
		}
		
	}
	
	public int getHeight() {
		return mHeight;
	}

	
	
	public int getStopPosition(){
		return (mVideoViewPlayer.getCurrentPosition()/1000);
	}
	public boolean isMediaPlaying(){		
		if(mVideoView == null || mVideoViewPlayer ==null){
			return false;
		}
		if(mVideoViewPlayer.getCurrentPosition() == 0)
			return false;
		else
			return true;

	}
	/**
	 * @param isMovie a movie Only to show the alert message
	 * @return can able to play 
	 */
	public boolean canBePlayed(boolean isMovie) {
		// Before playing any video we have to check whether user has logged In
		// or not.
		/*String email = myplexapplication.getUserProfileInstance()
				.getUserEmail();
		if (email.equalsIgnoreCase("NA") || email.equalsIgnoreCase("")) {
					 if (MediaUtil.checkAndSetPlayedMaxCount(mContext)) {

						 AlertDialogUtil.showAlert(mContext, mContext.getResources()
                                         .getString(R.string.must_logged_in), mContext
                                         .getResources().getString(R.string.continiue_as_guest),
                                 mContext.getResources().getString(R.string.login_to_play),
                                 CardVideoPlayer.this);

						 return false;
					}
		}
		*/
		CardDownloadedDataList downloadData = ApplicationController.getDownloadData();

		if (downloadData != null && mData != null) {
			
			CardDownloadData mDownloadData = downloadData.mDownloadedList
					.get(mData._id);
			if (mDownloadData != null) {
				return true;
			}
		}
        if(mData != null
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
		if(!Util.isNetworkAvailable(mContext)){
			AlertDialogUtil.showToastNotification(mContext.getString(R.string
                    .network_error));
			return false;
		}
        if(mData != null
                && mData.generalInfo != null
                && mData.generalInfo.type != null) {
            if (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE))
                return true;
        }
		/*if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_TV_SEASON) || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_TV_SERIES))
			return false;
		
		*//*if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)){
			Util.launchYouyubePlayer((Activity) mContext, mData._id);
			Analytics.mixPanelPlayedBreakingNews(mData);
			Analytics.gaPlayedBreakingNews(mData);
			return false;
		}
		*//*
		if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.CARD_TYPE_VOD)){
			return true;
		}
        if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_NEWS)){
            return true;
        }*/

		/*String networkInfo = UiUtil.getInternetConnectivity(mContext);
		if (networkInfo.equalsIgnoreCase("2G")) {
			UiUtil.showToast(
					mContext.getResources().getString(
							R.string.error_message_2g_videoplay), mContext);
			return false;
		} else if (networkInfo.equalsIgnoreCase("3G")) {
			if (isMovie)
				Util.showToast(
						mContext.getResources().getString(
								R.string.alert_message_3g_movie), mContext);
			else
				Util.showToast(
						mContext.getResources().getString(
								R.string.alert_message_3g_trailer), mContext);
			return true;
		}*/

		// It is a wifi
		return true;
	}
/*
	private OnClickListener mScoreCardClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if(!Util.isNetworkAvailable(mContext)){
				Util.showToast(mContext, "No Network connection",Util.TOAST_TYPE_ERROR);
				return;
			}			
			
			if(mData.matchInfo == null || TextUtils.isEmpty(mData.matchInfo.matchMobileUrl )){
				Util.showToast(mContext, "Not Available",Util.TOAST_TYPE_ERROR);
				return;
			}
			Intent i = new Intent(mContext,LiveScoreWebView.class);
			Bundle b = new Bundle();
			b.putString("url", mData.matchInfo.matchMobileUrl );
			b.putBoolean("isProgressDialogCancelable", true);
			i.putExtras(b);	
			((Activity) mContext).startActivityForResult(i, APIConstants.SUBSCRIPTIONREQUEST);
			
		}
	};
	*/
/*	private void initSportsStatusLayout(final View view){
		if(mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(APIConstants.CONTENT_SPORTS_LIVE)){
			mTrailerButton.setVisibility(View.GONE);
			mShareButton.setVisibility(View.GONE);
			mScoreCardLayout = (LinearLayout)view.findViewById(R.id.cardmedia_scorecard_layout);
			mScoreCardLayout.setVisibility(View.VISIBLE);
			mScoreCardLayout.setOnClickListener(mScoreCardClickListener);
			mTrailerAvailable=false;
			
			
			OnResponseListener onResponseListener = new OnResponseListener() {

				@Override
				public void response(boolean status, MatchStatus matchStatus) {

					if (!status) {
						return;
					}
					
					if(!TextUtils.equals(MatchStatus.STATUS_LIVE, matchStatus.status)){
						stopSportsStatusRefresh();
					}
					
					TextView textView1 = (TextView) view
							.findViewById(R.id.cardmedia_scorecard_textLine1);
					TextView textView2 = (TextView) view
							.findViewById(R.id.cardmedia_scorecard_textLine2);
					TextView textView3 = (TextView) view
							.findViewById(R.id.cardmedia_scorecard_textLine3);
					
					ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mScoreCardLayout,
							"alpha", 0f, 1f);
					fadeAnim2.setDuration(800);
					fadeAnim2.start();
					
					if(matchStatus.matchType == MATCH_TYPE.FIFA){
						
						Team team1= matchStatus.teams.get(0);
						Team team2= matchStatus.teams.get(1);
						
						
						if(team1.validate() && team2.validate()){

							String text = "("+team1.score+") "+ team1.sname + " " + "vs"							
									+" "+team2.sname + " ("+team2.score+")";
							textView1.setText(text);
						}
						
						if(!TextUtils.isEmpty(matchStatus.statusDescription)){
							textView2.setVisibility(View.VISIBLE);
							textView2.setText(matchStatus.statusDescription);	
							textView2.setSelected(true);
						}
						
						return;
					}
					
					if(!TextUtils.equals(MatchStatus.STATUS_LIVE, matchStatus.status) || 
							matchStatus.teams == null || 
							matchStatus.teams.isEmpty()){

						if(!TextUtils.isEmpty(matchStatus.matchTitle)){
							textView1.setText(matchStatus.matchTitle);
						}
						if(!TextUtils.isEmpty(matchStatus.statusDescription)){
							textView2.setText(matchStatus.statusDescription);
							textView2.setSelected(true);
						}
						return;
					}
					
					if(matchStatus.teams.size() ==1){
						
						Team team= matchStatus.teams.get(0);
						
						if(team.validate()){
							textView1.setText(team.sname +" " + team.score);						
						}
						if(!TextUtils.isEmpty(matchStatus.statusDescription)){
							textView2.setText(matchStatus.statusDescription);
						}
						
						return;
					}
					
					if(matchStatus.teams.size() == 2 ){
						
						Team team1= matchStatus.teams.get(0);
						Team team2= matchStatus.teams.get(1);
						if(team1.validate()){
							textView1.setText(team1.sname +" " + team1.score);
						}
						if(team2.validate()){
							textView2.setText(team2.sname +" " + team2.score);						
						}
						if(!TextUtils.isEmpty(matchStatus.statusDescription)){
							textView3.setVisibility(View.VISIBLE);
							textView3.setText(matchStatus.statusDescription);	
							textView3.setSelected(true);
						}
					}
					

				}

			
			};
			
			sportsStatusRefresh = new SportsStatusRefresh(mData._id, onResponseListener);
			sportsStatusRefresh.start();
		}
	}
	*/
	/*
	public void stopSportsStatusRefresh(){
		*//*if(sportsStatusRefresh != null){
			sportsStatusRefresh.stop();
		}*//*
	}*/
	
//	@Override
	/*public void onUrlFetched(List<CardDataVideosItem> items ,VideoInfo videoInfo)
	{
		String videoType = mData.generalInfo.type;
		//Log.d(TAG,"Video type "+ videoType);
		mData.videoInfo = videoInfo;
		
//		initPlayBack("https://myplexv2betadrmstreaming.s3.amazonaws.com/813/813_sd_est_1391082325821.wvm");
		*//*if(videoType.equalsIgnoreCase(APIConstants.TYPE_MOVIE)){
			chooseStreamOrDownload(items);
		}else*//* if(videoType.equalsIgnoreCase(APIConstants.TYPE_LIVE)){
			chooseLiveStreamType(items,false);			
		}*//*else if(videoType.equalsIgnoreCase(APIConstants.TYPE_TV_EPISODE)){
			chooseStreamOrDownload(items);
		}else if(videoType.equalsIgnoreCase(APIConstants.CONTENT_SPORTS_LIVE)){
			chooseLiveStreamType(items,false);	
		}else if(videoType.equalsIgnoreCase(APIConstants.TYPE_NEWS)){
			chooseLiveStreamType(items,true);
		}*//*else{
            chooseLiveStreamType(items,true);
        }

	}*/
	/*
	@Override
	public void onUrlFetched(List<CardDataVideosItem> items) 
	{
		String videoType = mData.generalInfo.type;		
		//Log.d(TAG,"Video type "+ videoType);
		
		initPlayBack("https://myplexv2betadrmstreaming.s3.amazonaws.com/813/813_sd_est_1391082325821.wvm");
		if(videoType.equalsIgnoreCase(APIConstants.TYPE_MOVIE)){
			chooseStreamOrDownload(items);
		}else if(videoType.equalsIgnoreCase(APIConstants.VIDEO_TYPE_LIVE)){
			chooseLiveStreamType(items,false);			
		}
		
	}*/
//	@Override
	/*public void onTrailerUrlFetched(List<CardDataVideosItem> videos, VideoInfo videoInfo) {
		chooseLiveStreamType(videos,true);
		mData.videoInfo = videoInfo;
	}*/
	private String getLink(Map<String, String> pMap, String firstPref, String secondPref, String thirdPref, String ...profile)
	{
		for(String string : profile){
			if(pMap.get(string+firstPref)!=null){
				if((profileSelect == null || profileSelect.length()==0)) {
					profileSelect = string;
				}
                mStreamingFormat = firstPref;
				return pMap.get(profileSelect+firstPref);
			} else if(pMap.get(string+secondPref)!=null){
				if(profileSelect == null || profileSelect.length()==0) {
					profileSelect = string;
				}
                mStreamingFormat = secondPref;
                return pMap.get(profileSelect+secondPref);
			} else if(pMap.get(string + thirdPref)!=null){
				if(profileSelect == null || profileSelect.length()==0)
					profileSelect=string;
				mStreamingFormat = thirdPref;
				return pMap.get(profileSelect+thirdPref);
			}
			
		}
		return "";
		
	}

	private String TYPE_2G = "2G";
	private String TYPE_WIFI = "WIFI";
	private String TYPE_4G = "4G";
	private String TYPE_5G = "5G";
	private String TYPE_3G = "3G";
	private String chooseVodStreamType (List<CardDataVideosItem> items){


		HashMap<String, String> profileMap = new HashMap<>();
		
		for(CardDataVideosItem item : items){
			if(item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVEDVR)) {
				profileMap.put(item.profile + item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_WIFI) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_5G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(TYPE_4G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(TYPE_3G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(TYPE_2G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			}
			
			else if((item.profile.equalsIgnoreCase(TYPE_WIFI) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
				profileMap.put(item.profile+item.format, item.link);			
			}else if((item.profile.equalsIgnoreCase(TYPE_5G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_4G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_3G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(TYPE_2G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
				profileMap.put(item.profile+item.format, item.link);
			}
			
		}
		
		String mConnectivity  = SDKUtils.getInternetConnectivity(mContext);

		if(mConnectivity.equalsIgnoreCase(TYPE_WIFI)){
			return (getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
					APIConstants.STREAMINGFORMATHTTP,APIConstants.STREAMINGFORMATHTTP,
					new String[]{APIConstants.STREAMADAPTIVEDVR,TYPE_WIFI,TYPE_5G,TYPE_4G,TYPE_3G, TYPE_2G}));
		}else if(mConnectivity.equalsIgnoreCase(TYPE_5G)){
            return(getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
					APIConstants.STREAMINGFORMATHTTP,APIConstants.STREAMINGFORMATHTTP,
					new String[]{APIConstants.STREAMADAPTIVEDVR,TYPE_5G,TYPE_4G,TYPE_3G, TYPE_2G}));
        }else if(mConnectivity.equalsIgnoreCase(TYPE_4G)){
            return(getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
					APIConstants.STREAMINGFORMATHTTP,APIConstants.STREAMINGFORMATHTTP,
					new String[]{APIConstants.STREAMADAPTIVEDVR,TYPE_4G,TYPE_3G, TYPE_2G}));
        }else if(mConnectivity.equalsIgnoreCase(TYPE_3G)){
            return(getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
					APIConstants.STREAMINGFORMATHTTP,APIConstants.STREAMINGFORMATHTTP,
					new String[]{APIConstants.STREAMADAPTIVEDVR,TYPE_3G, TYPE_2G}));
        }else if(mConnectivity.equalsIgnoreCase(TYPE_2G)){
			return(getLink(profileMap, APIConstants.STREAMINGFORMATHLS,APIConstants.STREAMINGFORMATHTTP,APIConstants.STREAMINGFORMATHTTP,
					new String[]{APIConstants.STREAMADAPTIVEDVR,TYPE_2G}));
		}else {
            return(getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
					APIConstants.STREAMINGFORMATHTTP,APIConstants.STREAMINGFORMATHTTP,
					new String[]{APIConstants.STREAMADAPTIVEDVR,TYPE_3G, TYPE_2G}));
        }

	}
	private void chooseLiveStreamType(List<CardDataVideosItem> items,boolean isTrailer)
	{
        HashMap<String, String> profileMap = new HashMap<String, String>();

        for(CardDataVideosItem item : items){
            if(item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVE) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
                profileMap.put(item.profile + item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
                profileMap.put(item.profile + item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
                profileMap.put(item.profile + item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATRTSP))){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATRTSP))){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYLOW)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
                profileMap.put(item.profile+item.format, item.link);
            }else if((item.profile.equalsIgnoreCase(APIConstants.VIDEOQUALTYLOW)) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATRTSP))){
                profileMap.put(item.profile+item.format, item.link);
            }else if(item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVEDVR) && (item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
                profileMap.put(item.profile+item.format, item.link);
            }if(item.profile.equalsIgnoreCase(APIConstants.STREAMADAPTIVEDVR)) {
				profileMap.put(item.profile + item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_WIFI) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_5G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_4G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_3G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_2G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			} else if((item.profile.equalsIgnoreCase(TYPE_WIFI) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_5G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_4G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_3G)) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS)){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(TYPE_2G) && item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHLS))){
				profileMap.put(item.profile+item.format, item.link);
			}
        }


        String mConnectivity  = SDKUtils.getInternetConnectivity(mContext);

        if(isTrailer){
            if(mConnectivity.equalsIgnoreCase("wifi")){
                initPlayBack(getLink(profileMap, APIConstants.STREAMINGFORMATHTTP,
                        APIConstants.STREAMINGFORMATRTSP, APIConstants.STREAMINGFORMATHTTP,
                        new String[]{APIConstants.VIDEOQUALTYHIGH,APIConstants.VIDEOQUALTYLOW}));

            }else if(mConnectivity.equalsIgnoreCase("3G")){
                initPlayBack(getLink(profileMap, APIConstants.STREAMINGFORMATHTTP,
                        APIConstants.STREAMINGFORMATRTSP, APIConstants.STREAMINGFORMATHTTP,
                        new String[]{APIConstants.VIDEOQUALTYHIGH,APIConstants.VIDEOQUALTYLOW}));
            }else if(mConnectivity.equalsIgnoreCase("2G")){
                initPlayBack(getLink(profileMap, APIConstants.STREAMINGFORMATHTTP,APIConstants.STREAMINGFORMATRTSP, APIConstants.STREAMINGFORMATHTTP,
                        new String[]{APIConstants.VIDEOQUALTYLOW}));
            }else{
//                Toast.makeText(mContext, mContext.getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
            }
            return;
        }

        if(mConnectivity.equalsIgnoreCase("wifi")){
                initPlayBack(getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
                        APIConstants.STREAMINGFORMATRTSP, APIConstants.STREAMINGFORMATHTTP,
                        new String[]{APIConstants.STREAMADAPTIVEDVR,APIConstants
                                .STREAMADAPTIVE, PrefUtils.getInstance()
								.getProfileWifi(), APIConstants.VIDEOQUALTYVERYHIGH, APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW, TYPE_WIFI,TYPE_5G, TYPE_4G, TYPE_3G, TYPE_2G}));

        }else if(mConnectivity.equalsIgnoreCase("3G")){
                initPlayBack(getLink(profileMap, APIConstants.STREAMINGFORMATHLS,
                        APIConstants.STREAMINGFORMATRTSP, APIConstants.STREAMINGFORMATHTTP,
                        new String[]{APIConstants.STREAMADAPTIVEDVR,APIConstants.STREAMADAPTIVE,
                                PrefUtils.getInstance()
                                .getProfile3G(), APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW, TYPE_3G, TYPE_2G}));
        }else if(mConnectivity.equalsIgnoreCase("2G")){
                initPlayBack(getLink(profileMap, APIConstants.STREAMINGFORMATHLS,APIConstants.STREAMINGFORMATRTSP, APIConstants.STREAMINGFORMATHTTP,
                    new String[]{PrefUtils.getInstance().getProfile2G(),APIConstants.VIDEOQUALTYLOW, TYPE_2G}));
        }else if(mConnectivity.equalsIgnoreCase("4G")){
            initPlayBack(getLink(profileMap, APIConstants.STREAMINGFORMATHLS, APIConstants.STREAMINGFORMATHTTP,
                    APIConstants.STREAMINGFORMATRTSP,
                    new String[]{APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE,
                            PrefUtils.getInstance()
                            .getProfile4G(), APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW, TYPE_4G,TYPE_3G, TYPE_2G}));

//            Toast.makeText(mContext,mContext.getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        }else if(mConnectivity.equalsIgnoreCase("5G")){
            initPlayBack(getLink(profileMap, APIConstants.STREAMINGFORMATHLS, APIConstants.STREAMINGFORMATHTTP,
                    APIConstants.STREAMINGFORMATRTSP,
                    new String[]{APIConstants.STREAMADAPTIVEDVR, APIConstants.STREAMADAPTIVE,
                            PrefUtils.getInstance()
                            .getProfile5G(), APIConstants.VIDEOQUALTYHIGH, APIConstants.VIDEOQUALTYLOW,TYPE_5G, TYPE_4G,TYPE_3G, TYPE_2G}));
}


    }
	/*private void chooseStreamOrDownload(List<CardDataVideosItem> items) {
		CardDataVideosItem adaptive = new CardDataVideosItem();
		CardDataVideosItem download = new CardDataVideosItem();	
		for(CardDataVideosItem item : items){
			if(item.type!=null && item.type.equalsIgnoreCase("adaptive")){
				if((item.link!=null && item.link.length()>0)&&(item.format!=null 
										&& item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP)))
					adaptive = item;
			}else if(item.type.equalsIgnoreCase("download")){
				if((item.link!=null && item.link.length()>0)&&(item.format!=null &&
										item.format.equalsIgnoreCase(APIConstants.STREAMINGFORMATHTTP)))		
					download = item;	
			}
		}
		if(download!=null)
			 download_link = download.link;		
		if(adaptive!=null){			
			adaptive_link = adaptive.link;
		}
		final int ellapseTime = adaptive.elapsedTime;
		if(SharedPrefUtils.getBoolFromSharedPreference(mContext, mContext.getString(R.string.is_dont_ask_again))) {
            if(SharedPrefUtils.getBoolFromSharedPreference(mContext, mContext.getString(R.string.isDownload))){
				if(download_link!=null)					
					initPlayBack(download_link);
				else if(adaptive_link!=null){                                        			
					initPlayBack(adaptive_link);			
					onLastPausedTimeFetched(adaptive.elapsedTime);			
				}
			}else{
				 Util.showToast(mContext, mContext.getString(R.string.switch_to_download_in_setting_msg), Util.TOAST_TYPE_INFO);
				 if(adaptive_link!=null)			
					 initPlayBack(adaptive_link);
				else if(download_link!=null)			
					initPlayBack(download_link);
			}
		}else if(download_link!=null && adaptive_link!=null){
				DownloadStreamDialog dialog = new DownloadStreamDialog(mContext,mData.generalInfo.title+" rental options", getContentType());
				dialog.setListener(new DownloadListener() {
					@Override
					public void onOptionSelected(boolean isDownload) {
						if(isDownload){
							initPlayBack(download_link);
						}else{
							initPlayBack(adaptive_link);
							onLastPausedTimeFetched(ellapseTime);
						}					
					}

                    @Override
                    public void onCancelSelected() {
                        closePlayer();
                    }
                });
				dialog.showDialog();	
				return;
		}else if(adaptive_link!=null){
			initPlayBack(adaptive_link);
			onLastPausedTimeFetched(adaptive.elapsedTime);	
			return;
		}else if(download_link!=null){
			initPlayBack(download_link);
			return;
		}
	}*/
/*	@Override
	public void onUrlFetchFailed(String message)
	{
		closePlayer();
		if(message != null && message.equalsIgnoreCase("ERR_USER_NOT_SUBSCRIBED")){
			if(mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_TV_EPISODE)){
				if(mPlayerStatusListener!=null)
					mPlayerStatusListener.playerStatusUpdate("ERR_USER_NOT_SUBSCRIBED");
				return;
			}
			*//*PackagePopUp popup = new PackagePopUp(mContext,(View)mParentLayout.getParent());
			myplexapplication.getCardExplorerData().cardDataToSubscribe =  mData;
			popup.showPackDialog(mData, ((Activity) mContext).getActionBar().getCustomView());*//*

        }*//*else if(message != null && message.equalsIgnoreCase("User has not logged in")){
            AlertDialogUtil.showAlert(mContext, mContext.getResources()
                            .getString(R.string.must_logged_in), mContext
                            .getResources().getString(R.string.continiue_as_guest),
                    mContext.getResources().getString(R.string.login_to_play),
                    CardVideoPlayer.this);
        }*//*else{
            UiUtil.showToast(mContext, message, UiUtil.TOAST_TYPE_INFO);
        }

//        UiUtil.showToast(mContext, message, UiUtil.TOAST_TYPE_INFO);

	}*/

	private void setBitrateForTrailer(String url) {
		if(url == null) return;
		String bitrateTrailer = null;
/*		if(Analytics.isTrailer) {
			if(url.contains("high")) bitrateTrailer = "high";
			if(url.contains("low")) bitrateTrailer = "low";
			if(url.contains("veryhigh")) bitrateTrailer = "veryhigh";
			if(url.contains("medium")) bitrateTrailer = "medium";
		}*/
//		else {
			if(url.contains("vhigh")) bitrateTrailer = "vhigh";
			if(url.contains("low")) bitrateTrailer = "low";
			if(url.contains("medium")) bitrateTrailer = "medium";
//		}
		if(bitrateTrailer != null) {
			if(mData != null && mData.generalInfo != null) {
				String cardId = mData.generalInfo._id;
//				String key = Analytics.TRAILER_BITRATE+_id;
//				SharedPrefUtils.writeToSharedPref(myplexapplication.getAppContext(), key, bitrateTrailer);
			}			
		}
	}
	public void initPlayBack(String url){
		//Log.d(TAG,"Got the link for playback = "+url);
		if (url == null) {
			closePlayer();
			if(mPlayerStatusListener != null){
				mPlayerStatusListener.playerStatusUpdate("No url to play.");
			}
			Analytics.gaNotificationPlayBackFailed(mNotificationTitle, mNid, mData, profileSelect);
			Analytics.mixPanelUnableToPlayVideo(mData,"No url to play.", profileSelect);
			AlertDialogUtil.showToastNotification("No url to play.");
			return;
		}
		isPlayingDVR = false;
        if(url!=null && url.toLowerCase().contains(ExoPlayerView.DVR_URI_FLAG)){
            isPlayingDVR = true;
        }
		
		/*if(isESTPackPurchased || url.contains("_est_"))
		{
			url=url.replace("widevine:", "http:");			
			closePlayer();
			if(Util.hasSpaceAvailabeToDownload(getContentType(), mContext))
			{
				if(Util.isWifiEnabled(mContext) || Util.is3GEnabled(mContext))
				{
					*//**//*if(ApplicationSettings.ENABLE_SHOW_PLAYER_LOGS_SETTINGS){
                        if(mData._id != null && mData._id.equalsIgnoreCase("413")){
                                url="http://192.168.60.36/myplex/413_sd_est_1388644246475.wvm";
                        }else if(mData._id != null && mData._id.equalsIgnoreCase("415")){
                                url="http://192.168.60.36/myplex/415_sd_est_1388645176954.wvm";
                        }else if(mData._id != null && mData._id.equalsIgnoreCase("446")){
                                url="http://192.168.60.36/myplex/446_sd_est_1386786732268.wvm";
                        }
                }*//**//*
					Util.startDownload(url, mData, mContext);
				}
				else
				{
					Util.showToast(mContext, "Downloading is supported on wifi and 3G.", Util.TOAST_TYPE_INFO);
				}
			}
			else
			{
				String msg = mContext.getString(R.string.play_download_insufficent_memory);
				String contentType = getContentType();
				if(!TextUtils.isEmpty(contentType) && contentType.equalsIgnoreCase(APIConstants.VIDEOQUALTYHD)){
					msg = mContext.getString(R.string.play_download_insufficent_memory_hd);
				}
				Util.showToast(mContext, msg, Util.TOAST_TYPE_INFO);
			}
			return;
		}
		else{
			drmLicenseType="st";
		}*/

		/*if(mData.content !=null && mData.content.drmEnabled)
		{
			String licenseData="clientkey:"+myplexapplication.getDevDetailsInstance().getClientKey()+",contentid:"+mData._id+",type:"+drmLicenseType+",profile:0,"+APIConstants.getDRMDeviceParams();

			byte[] data;
			try {
				data = licenseData.getBytes("UTF-8");
				*//**//*String base64 = Base64.encodeToString(data, Base64.DEFAULT);
				Settings.USER_DATA=base64;
				Settings.DEVICE_ID=myplexapplication.getDevDetailsInstance().getClientDeviceId();*//**//*
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	
		}
		if(!lastWatchedStatus){
			myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);
			lastWatchedStatus = true;
		}*/
//		Util.showToastAt(mContext.getString(R.string.adultwarning), mData, mContext);
		Uri uri ;
		uri = Uri.parse(url);
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("id : " + mData._id);
			mPlayerStatusListener.playerStatusUpdate("Playing :: "+url);
		}
        if(mVideoViewPlayer != null  && mVideoViewPlayer instanceof ExoPlayerView){
            mVideoViewPlayer.setStreamName(mData.globalServiceId);
        }
		setBitrateForTrailer(url); //url not uri
        initializeVideoPlay(uri);
    }
	public boolean isFullScreen() {
		   return isFullScreen;			
		}
	public void setFullScreen(boolean isFullScreen) {			
		this.isFullScreen = isFullScreen;			
		}        			
	public void onLastPausedTimeFetched(int ellapseTime) {			
		if(ellapseTime > 60){			
			if(mVideoViewPlayer ==null){
				if(videoPlayerType == VIDEO_PLAYER_TYPE.VIDEOVIEW)
					mVideoViewPlayer = new  VideoViewPlayer((VideoView)mVideoView, mContext,null , VideoViewPlayer.StreamType.VOD);
				}			
			mVideoViewPlayer.setPositionWhenPaused(ellapseTime * 1000);
			}			
	}
	protected VideoViewPlayer.OnLicenseExpiry onLicenseExpiryListener = new VideoViewPlayer.OnLicenseExpiry() {
		
		@Override
		public void licenseExpired() {
			
			if(mContext == null || ! (mContext instanceof Activity)){
				return;
			}
			
			((Activity)mContext).runOnUiThread(new Runnable() {

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
	
	public void updateCardPreviewImage(CardData data){
		if(data == null || data.images==null || data.images.values==null)
			return;
		mData = data;
		/*if(mData.relatedMultimedia==null
				|| mData.relatedMultimedia.values==null 
					|| mData.relatedMultimedia.values.size()==0){
			mTrailerAvailable = false;
		}else{
			for (CardDataRelatedMultimediaItem mmItem : mData.relatedMultimedia.values) {
				{
					if(mmItem.content !=null && mmItem.content.categoryName !=null 
							&& mmItem.content.categoryName.equalsIgnoreCase("trailer") 
								&& mmItem.generalInfo !=null && mmItem.generalInfo._id !=null)
					{
						mTrailerAvailable = true;
						break;
					}
				}
			}
		}*/
//        Analytics.gaPlayedVideo(mData,mMouTracker.getTotalPlayedTimeInMinutes());
		if(isMediaPlaying())
			return;
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
		if (mData.images != null 
				&& mData.images.values != null
				&& !mData.images.values.isEmpty()
				&& mData.images.values.size() > 0) {
            for (CardDataImagesItem imageItem : mData.images.values) {
                if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null
                        && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI) && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
                    if (imageItem.link == null
                            || imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
                        mPreviewImage.setImageResource(0);
                    } else if (imageItem.link != null) {
                        PicassoUtil.with(mContext).load(imageItem.link,mPreviewImage,R.drawable.tv_guide_thumbnail_default);
                    }
                    break;
                }
            }
		}
        if(mData != null
                && mData.startDate == null){
            mReminderImage.setVisibility(View.GONE);
        }else if(null != mData && null != mData.startDate
                && null != mData.endDate){
            Date startDate = Util.getDate(mData.startDate);
            Date endDate = Util.getDate(mData.endDate);
            Date currentDate = new Date();
            if ((currentDate.after(startDate)
                    && currentDate.before(endDate))
					|| currentDate.after(endDate)) {
                mReminderImage.setVisibility(View.VISIBLE);
                mReminderImage.setImageResource(R.drawable.oncard_play_icon);
            }
        }

        mReminderImage.setTag(mData);
        ReminderListener reminderListener = new ReminderListener(mContext,null,mEpgDatePosition);
        mReminderImage.setOnClickListener(reminderListener);

        if(mData != null
                && (mData.startDate == null
                    || mData.endDate == null)){
            mReminderImage.setVisibility(View.GONE);
        }
        AlarmData alarmData = Util.getReminderProgmaNameIfExistAtThisTime(mData);
        if(alarmData != null
                && alarmData.title != null
                && alarmData.title.equalsIgnoreCase(mData.generalInfo.title)){
			mReminderImage.setVisibility(View.VISIBLE);
            mReminderImage.setImageResource(R.drawable.oncard_set_reminder_icon_active);
        }
        if(mReminderImage.getVisibility() == View.VISIBLE){
            debugTxtLayout.setVisibility(View.GONE);
        }
        if (mData != null && mData.generalInfo != null) {
            if (APIConstants.TYPE_NEWS.equalsIgnoreCase(mData.generalInfo.type)) {
                if (!mData.generalInfo.videoAvailable) {
                    mTrailerButton.setVisibility(View.GONE);
                    mVideoViewParent.setOnClickListener(null);
                }
            }else if(APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                    || APIConstants.TYPE_VOD.equalsIgnoreCase(mData.generalInfo.type)
					|| APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type)
					|| APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(mData.generalInfo.type)){
                mReminderImage.setVisibility(View.GONE);
                mThumbnailPlay.setVisibility(View.GONE);
                mThumbnailPlay.setOnClickListener(mPlayerClickListener);
            }
        }

    }
	
	/*public void createRecordPlayView(String url,String programmName){
		final String urlString = url;
		mTrailerButton.setVisibility(View.VISIBLE);
		mTrailerButton.setText(mContext.getString(R.string.record_play));
		if(mMinimizeButton!=null)
		mMinimizeButton.setVisibility(View.INVISIBLE);
  
		mTrailerButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {	
				mVideoViewParent.setOnClickListener(null);
				mProgressBarLayout.setVisibility(View.VISIBLE);	
				mPreviewImage.setVisibility(View.INVISIBLE);
				mTrailerButton.setVisibility(View.INVISIBLE);
				mPlayButton.setVisibility(View.INVISIBLE);
				mShareButton.setVisibility(View.INVISIBLE);
//				mPlayButton.setOnClickListener(null);
//				mVideoViewParent.setEnabled(false);
				recordedProgName.setVisibility(View.INVISIBLE);
				initPlayBack(urlString);
				
			}
		});
		recordedProgName.setVisibility(View.VISIBLE);
		recordedProgName.setText(programmName);
		recordedProgName.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mTrailerButton.performClick();
			}
		});
	}
	public void removeRecordPay(){
		closePlayer();
		mTrailerButton.setVisibility(View.GONE);
		recordedProgName.setVisibility(View.GONE);
		mPlayButton.setVisibility(View.VISIBLE);
        mShareButton.setVisibility(View.VISIBLE);
	}
	public void removeProgrammeName(){
		if(mPreviewImage!=null)
			mPreviewImage.setVisibility(View.INVISIBLE);	
		if(recordedProgName!=null)
			recordedProgName.setVisibility(View.GONE);
		if(mTrailerButton!=null)
			mTrailerButton.setVisibility(View.GONE);
		if(mPlayButton!=null)
			mPlayButton.setVisibility(View.GONE);
		mVideoViewParent.setOnClickListener(null);
		if(mShareButton!=null){
			mShareButton.setVisibility(View.GONE);
			
		}
	}*/
	public boolean getTrailer(){
		return isTriler;
	}
	
	private OnClickListener mResumeClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mErrorLayout == null || mVideoViewPlayer == null){
				return;
			}
			mErrorLayout.setVisibility(View.GONE);
			mVideoViewPlayer.onResume();

		}
	};
	
	private void showPlayButton(){
		
		mErrorLayout.setVisibility(View.VISIBLE);
		
		TextView textView = (TextView) mErrorLayout.findViewById(R.id.cardmediasubitem_retrytext);			
		textView.setVisibility(View.GONE);
		
		Button retryButton = (Button) mErrorLayout.findViewById(R.id.cardmediasubitem_retryButton);
		retryButton.setOnClickListener(mResumeClickListener);
		retryButton.setText("resume");
		if(BuildConfig.FLAVOR.contains("idea")){
			retryButton.setBackgroundColor(mContext.getResources().getColor(R.color.progressbar_color));
		}
    }
	
	protected void retryPlayback(){
		
		if(mData == null || mData.generalInfo == null || mData.generalInfo.type == null){
			return;
		}
		
		if(isTriler){
			// No retry screen for trailer
			return;
		}
		
		if(isMinimized) return;
		
		recordedProgName.setVisibility(View.GONE);

		mTrailerButton.setVisibility(View.INVISIBLE);			
		mProgressBarLayout.setVisibility(View.VISIBLE);	
		mPreviewImage.setVisibility(View.INVISIBLE);
		mPlayerState = WAIT_FORRETRY;		
		mErrorLayout.setVisibility(View.VISIBLE);		
		// cardmediasubitem_retryButton
		Button retryButton = (Button) mErrorLayout.findViewById(R.id.cardmediasubitem_retryButton);
        retryButton.setAllCaps(false);
		retryButton.setOnClickListener(mPlayerClickListener);	
		retryButton.setText(mContext.getString(R.string.play_button_retry));
		if(BuildConfig.FLAVOR.contains("idea")){
			retryButton.setBackgroundColor(mContext.getResources().getColor(R.color.progressbar_color));
		}
		// for vod and movies

		if (!(APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)
				|| APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)
				|| APIConstants.TYPE_VOD.equalsIgnoreCase(mData.generalInfo.type)
				|| APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type)
				|| APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type))) {
			
			TextView textView = (TextView) mErrorLayout.findViewById(R.id.cardmediasubitem_retrytext);			
			textView.setVisibility(View.VISIBLE);
			textView.setText(mContext.getString(R.string.play_msg_err));
			/*if(isLocalPlayback){
				int duration = mVideoViewPlayer.getCachedDuration();				
				if(mDownloadData != null && mDownloadData.mDownloadId != -1)
				{
					FetchDownloadProgress fetchDownloadProgress = new FetchDownloadProgress(mContext);
					int dl_percentage = fetchDownloadProgress.getDownloadPercentage(mDownloadData.mDownloadId);
					if(dl_percentage > 1 && duration > 1){
						int durationDownloaded = (duration*dl_percentage)/100;
						String play_duration_available = Util.stringForTime(durationDownloaded);
						if(play_duration_available != null){
							textView.setText(mContext.getString(R.string.play_msg_download_inprogress)
									+ " "+play_duration_available + " only.");
							return;
						}
					}else if (dl_percentage  == 0 || duration == -1){
						if(mData != null
								&& mData.generalInfo != null
								&& mData.generalInfo.type != null
								&& mData.generalInfo.type.equalsIgnoreCase(APIConstants.CARD_TYPE_VOD)){
							textView.setText(mContext.getString(R.string.play_msg_download_not_enough_for_vod));
							return;
						}
						textView.setText(mContext.getString(R.string.play_msg_download_not_enough));
						return;
					}
				}
				textView.setText(mContext.getString(R.string.play_msg_err_local_file));
				
			}*/
			return;
		}
		
		// for live, sports live and sports vod content
		
		mAutoRetry ++;
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

		camAngleSelect  = langSelect = "";

		TextView textView = (TextView) mErrorLayout.findViewById(R.id.cardmediasubitem_retrytext);	
		
		if(mAutoRetry > MAX_AUTO_RETRY){
			
			textView.setVisibility(View.VISIBLE);
			textView.setText(mContext.getString(R.string.play_msg_err));
			return;
		}
		
		textView.setVisibility(View.VISIBLE);
		textView.setText(mContext.getString(R.string.play_msg_err_retrying));		
		
		mVideoViewParent.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(mPlayerState != WAIT_FORRETRY){
					return;
				}
				mErrorLayout.setVisibility(View.GONE);
                AlertDialogUtil.showToastNotification(mContext
                        .getString(R.string.play_retry_lower_bitrate));
				fetchUrl(null);

			}
		}, INTERVAL_RETRY);

	}

	public void onPause(){
    	if(mVideoViewPlayer != null){
    		mVideoViewPlayer.onPause();
    	}
    	
    }


    
    public void onResume(){
    	if(mVideoViewPlayer != null && mVideoViewPlayer.wasPlayingWhenPaused()){
    		//showPlayButton();
            mVideoViewPlayer.onResume();
    	}
		if(isYouTubePlayerLaunched()){
			closePlayer();
		}
    }
    
    private String  getContentType(){
    	if(mData != null && mData.currentUserData != null && mData.currentUserData.purchase != null)
        {    
			for(CardDataPurchaseItem data:mData.currentUserData.purchase)
            {
                if(data.contentType != null ){
                	return data.contentType;
                }               
            }
        }
    	
    	return null;
    }
    private String getDrmProfileString(){
    	
    	if(mData != null && mData.currentUserData != null && mData.currentUserData.purchase != null)
        {    
			for(CardDataPurchaseItem data:mData.currentUserData.purchase)
            {
                if(data.contentType != null && data.contentType.equalsIgnoreCase(APIConstants.VIDEOQUALTYHD) ){
                	return "profile:1";
                }               
            }
        }
    	
    	return "profile:0";
    	
    }
    public void onBufferingEnd(){
    	 if(mProgressBarLayout!=null && mProgressBarLayout.getVisibility() == View.VISIBLE && !isMinimized){		
         	
        		if (mBufferPercentage != null) {
        			mBufferPercentage.setVisibility(View.GONE);
        		}
         	   mProgressBarLayout.setVisibility(View.GONE);
            }
    }

    public long getTotalplayedTimeInMinutes(){
        if(mMouTracker == null){
            return 0;
        }
        return mMouTracker.getTotalPlayedTimeInMinutes();
    }

	public long getTotalplayedTimeInSeconds(){
		if(mMouTracker == null){
			return 0;
		}
		return mMouTracker.getTotalPlayedTimeInSeconds();
	}

    private int mEpgDatePosition = 0;

    public void setEpgDatePosition(int epgDatePosition) {
        mEpgDatePosition = epgDatePosition;
    }

    public boolean isAdPlaying(){
        return false;
    }

    public long getCurrentContentProgress(){
        if(mVideoViewPlayer == null) return 0;
        return mVideoViewPlayer.getCurrentPosition();
    }

    public void setCurrentContentProgress(long currentContentProgress) {
    }

    protected String mNid = null;
    public void setNid(String nid) {
        mNid = nid;
    }

	public boolean isPlayingDVR() {
		return isPlayingDVR;
	}

	public void showMediaController(){
		if(mVideoViewPlayer != null)
			mVideoViewPlayer.showMediaController();
	}

	public void setShowingHelpScreen(boolean isTimeShiftHelpScreenShowing) {
		this.isTimeShiftHelpScreenShowing = isTimeShiftHelpScreenShowing;
	}

	private void launchYoutubePlayer(String link) {
		isYouTubePlayerLaunched = true;
		mProgressBarLayout.setVisibility(View.INVISIBLE);
		mPreviewImage.setVisibility(View.VISIBLE);
		mVideoViewParent.setEnabled(true);
		mVideoViewParent.setOnClickListener(mPlayerClickListener);
		Util.launchYouyubePlayer((Activity) mContext, link);
	}

	private String getYoutubeLink(CardDataVideos videos) {
		if(videos == null){
			return null;
		}
		for(CardDataVideosItem videosItem : videos.values){
			if(APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(videosItem.format)){
				return videosItem.link;
			}
		}
		return null;
	}


	private void pickRetryPlayBackProfile() {
		if(mData != null
				&& mData.videos != null
				&& mData.videos.values != null){

			for (CardDataVideosItem videosItem : mData.videos.values) {
				if (!APIConstants.VIDEOQUALTYSD.equalsIgnoreCase(videosItem.profile)
						&& !videosItem.profile.equalsIgnoreCase(profileSelect)
						&& !TextUtils.isEmpty(videosItem.format)
						&& !TextUtils.isEmpty(videosItem.link)) {
					profileSelect = videosItem.profile;
					break;
				}
			}
		}
		//Log.d(TAG, "profileSelect- " + profileSelect);
	}
	/*public void onConfigurationChanged(Configuration newConfig){
		if(newConfig.orientation==SCREEN_ORIENTATION_LANDSCAPE){
			playInLandscape();
			playContent();
		}else{
			((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_LANDSCAPE);
			playContent();
		}

	}*/

}
