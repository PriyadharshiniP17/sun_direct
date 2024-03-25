package com.myplex.myplex.ui.views;

import static com.myplex.api.APIConstants.TYPE_LIVE;
import static com.myplex.api.APIConstants.TYPE_PROGRAM;
import static com.myplex.model.ApplicationConfig.HDPI;
import static com.myplex.model.ApplicationConfig.MDPI;
import static com.myplex.model.ApplicationConfig.XHDPI;
import static com.myplex.model.ApplicationConfig.XXHDPI;
import static com.myplex.myplex.ApplicationController.elapsedTimeAutoplayContent;
import static com.myplex.myplex.utils.Util.isNumeric;
import static com.myplex.player_config.PlayerConfig.PlayerType.DASH_PLAYER;
import static com.myplex.player_config.PlayerConfig.PlayerType.HLS_PLAYER;
import static com.myplex.player_config.PlayerConfig.PlayerType.PROGRESSIVE_PLAYER;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.security.MediaLinkEncrypted;
import com.myplex.model.CardData;
import com.myplex.model.CardDataCurrentUserData;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardDataPurchaseItem;
import com.myplex.model.CardDataVideos;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.LocationInfo;
import com.myplex.model.PreviewProperties;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.model.BufferConfigAndroid;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterSingleBannerRecyclerView;
import com.myplex.myplex.ui.fragment.FragmentCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentLanguageCarouselInfo;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.LiveCardPlayerCallback;
import com.myplex.myplex.utils.MOUTracker;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.player_config.PlayerConfig;
import com.myplex.player_sdk.BufferConfig;
import com.myplex.player_sdk.ConstructPlayer;
import com.myplex.player_sdk.MyplexPlayer;
import com.myplex.player_sdk.MyplexPlayerInterface;
import com.myplex.player_sdk.PlayerCallbackInterface;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SingleBannerAutoplayRecyclerview extends RecyclerView implements LiveCardPlayerCallback {
    private Context mContext;
    private CardData data;
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private String drmLicenseUrl;
    private MyplexPlayer player;
    private Handler mainHandler;
    private ImageView mCoverImage;
    private ImageView userBadgeImage;
    private ImageView partnerLogoImageView;
    private ImageView mMuteButton;
    private SurfaceView videoSurfaceView;
    private int prevPlayPos;
    private int playPosition;
    private boolean didPlayerPlay = false;
    private Map<String, String> contentUrls = new HashMap<>();
    private int bufferCount = 0;
    private boolean isContentPlayed = false;
    private BroadcastReceiver localBroadCastReceiver;
    private boolean isActivityPaused = false;
    private String currentPage = ApplicationController.FIRST_TAB_NAME;
    private MOUTracker mMouTracker;
    private int carouselPosition;
    private CarouselInfoData carouselData;
    private final int PLAYER_VOLUME_MUTE = 0;
    private final int PLAYER_VOLUME_UNMUTE = 1;

    private int maxAutoBitrateToSupport = 1500000;
    private int minAutoBitrateToSupport = 0;
    private MyplexPlayerInterface playerInterface;
    public int bitrate;

    private Handler playbackTestHandler = new Handler();
    private String mMenuGroup;
    private PreviewProperties mPreviewProperties;
    private List<CardData> mCardDataList = new ArrayList<>();
    private Boolean isVideoUnMuteState = false;

    public void setListCardData(List<CardData> mCardDataList) {
        this.mCardDataList = mCardDataList;
        if (mCardDataList != null && mCardDataList.size() > 0)
            data = mCardDataList.get(0);
    }

    public void setCarouselPosition(int position) {
        this.carouselPosition = position;
    }

    public void setCarouselData(CarouselInfoData carouselData) {
        this.carouselData = carouselData;
        ApplicationController.isMiniPlayerEnabled = false;
        if (getAdapter() != null && isAttachedToWindow() && !isContentPlayed) {
            pausePlayer();
            if(((MainActivity)mContext)!=null && ((MainActivity)mContext).mFragmentCardDetailsPlayer!=null && ((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer!=null) {
                System.out.println("qwerty-init-" + ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized());
                if (!((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized()) {
                    tryPlayingContent();
                    getAdapter().notifyDataSetChanged();
                }
            }
        }
    }

    public SingleBannerAutoplayRecyclerview(Context context) {
        super(context);
        mContext = context;
        if (data != null)
            initialize(context, data);
    }

    public SingleBannerAutoplayRecyclerview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (data != null)
            initialize(context, data);
    }

    public SingleBannerAutoplayRecyclerview(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        if (data != null)
            initialize(context, data);
    }

    private void initialize(Context mContext, CardData cardDataaa) {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoSurfaceDefaultHeight = point.x / 4;
        screenDefaultHeight = point.y;

        if (elapsedTimeAutoplayContent != null && cardDataaa != null && cardDataaa._id != null && elapsedTimeAutoplayContent.get(cardDataaa._id)!=null) {
            try {
                long elapsedTime = elapsedTimeAutoplayContent.get(cardDataaa._id);
                playerInterface.forward(elapsedTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        addOnChildAttachStateChangeListener(mChildAttachStateChangeListener);
        registerBroadcastReceiver();
        ((MainActivity) mContext).setLiveCardPlayerCallback(this);

    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(APIConstants.PAUSE_BROADCAST);
        filter.addAction(APIConstants.RESUME_BROADCAST);
        filter.addAction(APIConstants.PAGE_CHANGE_BROADCAST);
        filter.addAction(APIConstants.MINI_PLAYER_ENABLED_BROADCAST);
        filter.addAction(APIConstants.MINI_PLAYER_DISABLED_BROADCAST);

        localBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if (intent.getAction().equalsIgnoreCase(APIConstants.PAUSE_BROADCAST)) {
                        pausePlayer();
                        isActivityPaused = true;
                    } else if (intent.getAction().equalsIgnoreCase(APIConstants.RESUME_BROADCAST)) {
                        pausePlayer();
                        tryPlayingContent();
                        Log.e("single_banner","RESUME_BROADCAST");
                        isActivityPaused = false;
                    } else if (intent.getAction().equalsIgnoreCase(APIConstants.PAGE_CHANGE_BROADCAST)) {
                        if (intent.hasExtra(APIConstants.TAB_NAME)) {
                            currentPage = ApplicationController.FIRST_TAB_NAME;
                            pausePlayer();
//                            tryPlayingContent();
                        }
                    } else if (intent.getAction().equalsIgnoreCase(APIConstants.MINI_PLAYER_ENABLED_BROADCAST)) {
                        pausePlayer();
                        ApplicationController.isMiniPlayerEnabled = true;
                        if(mMuteButton!=null) {
                            mMuteButton.setEnabled(false);
                        }
                    } else if (intent.getAction().equalsIgnoreCase(APIConstants.MINI_PLAYER_DISABLED_BROADCAST)) {
                        if (isAttachedToWindow() && !isContentPlayed) {
                            pausePlayer();
                            tryPlayingContent();
                        }
                        isContentPlayed=false;
                        ApplicationController.isMiniPlayerEnabled = false;
                        if(mMuteButton!=null) {
                            mMuteButton.setEnabled(true);
                        }
                    }
                }
            }
        };
        ApplicationController.getLocalBroadcastManager().registerReceiver(localBroadCastReceiver, filter);

    }

    private OnChildAttachStateChangeListener  mChildAttachStateChangeListener = new OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {

        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {
            pausePlayer();
        }
    };

    private PlayerCallbackInterface mPlayerCallbackInterface = new PlayerCallbackInterface() {

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        }

        @Override
        public void onVideoInputFormatChanged(Format format) {
        }

        @Override
        public void onRenderedFirstFrame(@Nullable Surface surface) {

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

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {

                case ExoPlayer.STATE_BUFFERING:
                    //  Log.e("Inline", "onPlayerStateChanged: Buffering ");
                    if (prevPlayPos != playPosition)
                        pausePlayer();

                    bufferCount++;
                    break;
                case ExoPlayer.STATE_ENDED:
                    //   Log.e("Inline", "onPlayerStateChanged: ENDED ");
                    if (didPlayerPlay) {
                        didPlayerPlay = false;
                        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                    hideUIElements();
                    break;
                case ExoPlayer.STATE_IDLE:
                    //   Log.e("Inline", "onPlayerStateChanged: IDLE ");
                    //Fix for the issue the live card player is not playing when we rotate the
                    // application from portrait to landscape
                        fetchUrl();
                    break;
                case ExoPlayer.STATE_READY:
                    if(((MainActivity)mContext)!=null && ((MainActivity)mContext).mFragmentCardDetailsPlayer!=null
                            && ((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer!=null
                            && !(((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer.mPlayerState==4 /*|| ((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized()*/ ||((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer.isMediaPlaying())) {
                        showUIElements();
                    }else{
                        hideUIElements();
                        isContentPlayed=false;
                    }
                    Log.e("Inline", "onPlayerStateChanged: Ready to play");
                    if (prevPlayPos == playPosition) {
                        if (player != null)
                            player.resumePlayer();
                    } else {
                        pausePlayer();
                    }

                    if (Boolean.parseBoolean(ApplicationController.shouldMuteAutoPlayContents.get(data._id))) {
                        mMuteButton.setImageResource(R.drawable.unmute_icon);
                    } else {
                        mMuteButton.setImageResource(R.drawable.mute_icon);
                        if(playerInterface!=null) {
                            playerInterface.setPlayerVolume(PLAYER_VOLUME_MUTE);
                        }
                    }
                    if (!isContentPlayed) {
                        Log.e("MOUTRACKER_AUTOPLAY ", "single_banner_START MOU Tracker");
                        clevertapVideoStarted();
                        startMOUTracker();

                    } else {
                        Log.e("MOUTRACKER_AUTOPLAY", "single_banner_START content play MOU Tracker");
                    }

                    didPlayerPlay = true;
                    isContentPlayed = true;
                    break;
                default:
                    break;
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
        public void onPlayerError(ExoPlaybackException error) {
            if (error != null && error.getCause() instanceof BehindLiveWindowException) {
                pausePlayer();
            } else {
                contentUrls.remove(data._id);
                pausePlayer();
                try {
                    if (error != null && error.getCause() != null) {
                        String message = error.getCause().getMessage();
                        int responseCode = ((HttpDataSource.InvalidResponseCodeException) error.getCause()).responseCode;
                        Log.e("Player Error", message);
                        if (responseCode == 403 || message.contains("403")) {
                            pausePlayer();
                            tryPlayingContent();
                        }
                    }
                } catch (Exception e) {
                    Log.e("Player Error ", e.getLocalizedMessage());
                }
            }
        }

        @Override
        public void onPositionDiscontinuity(int i) {
            //  Log.e("Inline", "onPlayerStateChanged: POSITION DISCONTINUED ");

        }
    };

    private void pausePlayer() {
        if (isContentPlayed) {
            Log.e("MOUTRACKER_AUTOPLAY", "single_banner_STOP MOU Tracker");
            stopMOUTracking();
            isContentPlayed = false;
        }

        releasePlayer();
    }

    private void releasePlayer() {
        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            if (player != null) {
                try {
                    long currentPosition = player.getCurrentPosition();
                    if (data != null)
                        elapsedTimeAutoplayContent.put(data._id, currentPosition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("Inline", "Player Released");
                player.releasePlayer(MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.STOP);
                player = null;
                if (videoSurfaceView != null) {
                    videoSurfaceView.setVisibility(GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            didPlayerPlay = false;
        }
        hideUIElements();
        if (localBroadCastReceiver != null) {
            ApplicationController.getLocalBroadcastManager().unregisterReceiver(localBroadCastReceiver);
        }
    }

    private void hideUIElements() {
        if (mCoverImage != null) {
            mCoverImage.setVisibility(VISIBLE);
        }
        if (userBadgeImage != null && APIConstants.IS_ENABLE_USER_SEGMENT_BADGE)
            userBadgeImage.setVisibility(VISIBLE);
        if(partnerLogoImageView != null)
            partnerLogoImageView.setVisibility(VISIBLE);
        if (videoSurfaceView != null) {
            videoSurfaceView.setVisibility(GONE);
        }
        if (mMuteButton != null) {
            mMuteButton.setVisibility(VISIBLE);
        }
    }

    private void showUIElements() {
        if (videoSurfaceView != null) {
            videoSurfaceView.setVisibility(VISIBLE);
        }
        if (mCoverImage != null) {
            mCoverImage.setVisibility(INVISIBLE);
        }
        if (userBadgeImage != null)
            userBadgeImage.setVisibility(INVISIBLE);
        if(partnerLogoImageView != null)
            partnerLogoImageView.setVisibility(INVISIBLE);
        if (mMuteButton != null) {
            mMuteButton.setVisibility(VISIBLE);
        }
    }

    protected void startMOUTracker() {
        if (!ApplicationController.ENABLE_MOU_TRACKING) {
            return;
        }
        if (mMouTracker == null) {
            mMouTracker = new MOUTracker(null, mContext, data, data);
            if (currentPage != null)
                mMouTracker.setSourceTab(currentPage);
            mMouTracker.setSourceCarouselPosition(carouselPosition);
            mMouTracker.setSourceContentPosition(0);
            mMouTracker.setSource(APIConstants.CAROUSEL_AUTOPLAY);
            mMouTracker.setSourceDetails(APIConstants.SOURCE_DETAIL_AUTOPLAY);
            if (data != null)
                mMouTracker.setTrailerContentId(data._id);
            if (data != null && data.generalInfo != null)
                mMouTracker.setVODContentType(data.generalInfo.type);
            mMouTracker.start();
        }
    }

    public void stopMOUTracking() {
        if (mMouTracker != null) {
            mMouTracker.setBufferCount(bufferCount);
            mMouTracker.stoppedAutoPlayAt(false, data);
            mMouTracker.setSourceTab(currentPage);
            mMouTracker = null;
        }
    }

    private void clevertapVideoStarted() {
        if (data != null && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(data.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(data.generalInfo.type))) {
            CleverTap.eventVideoStarted(data, data,false, APIConstants.CAROUSEL_AUTOPLAY,
                    APIConstants.SOURCE_DETAIL_AUTOPLAY, currentPage, carouselPosition, 0);
            ComScoreAnalytics.getInstance().setEventVideoBannerShowed(data, carouselPosition, mMenuGroup, APIConstants.CAROUSEL_AUTOPLAY,
                    APIConstants.SOURCE_DETAIL_AUTOPLAY);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initialize(mContext, data);
        if (getAdapter() != null && isAttachedToWindow() && !isContentPlayed) {
            pausePlayer();
          //  System.out.println("qwerty--"+((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized());
          /*  if (((MainActivity)mContext) != null && ((MainActivity)mContext).mFragmentCardDetailsPlayer != null && ((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer != null && !((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized()) {*/
                tryPlayingContent();
          /*  }*/
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(isContentPlayed) {
            Log.e("MOUTRACKER_AUTOPLAY","single_banner_STOP MOU Tracker");
            stopMOUTracking();
            isContentPlayed = false;
        }
        pausePlayer();

        didPlayerPlay = false;
    }

    public void tryPlayingContent() {
        int PRE_AUTO_PLAY_WAIT_TIME = 1000;
        if (carouselData != null && playbackTestHandler != null) {
            playbackTestHandler.removeCallbacks(playbackRunnable);

            if(isNumeric(carouselData.shortDesc)) {
                playbackTestHandler.postDelayed(playbackRunnable, (!TextUtils.isEmpty(carouselData.shortDesc)) ? Integer.parseInt(carouselData.shortDesc) : PRE_AUTO_PLAY_WAIT_TIME);
            } else {
                playbackTestHandler.postDelayed(playbackRunnable,PRE_AUTO_PLAY_WAIT_TIME);
            }
        }
    }

    Runnable playbackRunnable = new Runnable() {
        @Override
        public void run() {
//            if (isActivityPaused) {
//                return;
//            }
            Log.e("MOUTRACKER_AUTOPLAY", " single_banner :isMiniPlayerEnabled "+ApplicationController.isMiniPlayerEnabled);
           /* if (ApplicationController.isMiniPlayerEnabled) {
                return;
            }*/
           /* if (((MainActivity)mContext).mFragmentCardDetailsPlayer == null || ((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer == null || ((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized()){
                return;
            }*/
            if (getAdapter() != null) {
                mMenuGroup = ((AdapterSingleBannerRecyclerView) getAdapter()).getTabName();
            }
            //currentPage = TextUtils.isEmpty(ApplicationController.currentTab) ? ApplicationController.FIRST_TAB_NAME : ApplicationController.currentTab;
         /*   if (currentPage != null && !currentPage.equalsIgnoreCase(mMenuGroup)) {
                return;
            }*/
            if (isAttachedToWindow()) {
                if((MainActivity)mContext != null && ((MainActivity)mContext).homePagerAdapterDynamicMenu != null && ((MainActivity)mContext).homePagerAdapterDynamicMenu.homeFragment != null
                        && ((MainActivity)mContext).homePagerAdapterDynamicMenu.homeFragment instanceof FragmentCarouselInfo) {
                    if(((FragmentCarouselInfo) ((MainActivity)mContext).homePagerAdapterDynamicMenu.homeFragment).isPullRefresh) {
                        fetchUrl();
                        ((FragmentCarouselInfo) ((MainActivity)mContext).homePagerAdapterDynamicMenu.homeFragment).isPullRefresh = false;
                        return;
                    }
                }else if((MainActivity)mContext != null && ((MainActivity)mContext).homePagerAdapterDynamicMenu != null && ((MainActivity)mContext).homePagerAdapterDynamicMenu.homeFragment != null
                        && ((MainActivity)mContext).homePagerAdapterDynamicMenu.homeFragment instanceof FragmentLanguageCarouselInfo) {
                    if(((FragmentLanguageCarouselInfo) ((MainActivity)mContext).homePagerAdapterDynamicMenu.homeFragment).isPullRefresh) {
                        fetchUrl();
                        ((FragmentLanguageCarouselInfo) ((MainActivity)mContext).homePagerAdapterDynamicMenu.homeFragment).isPullRefresh = false;
                        return;
                    }
                }
                if (data != null && !TextUtils.isEmpty(contentUrls.get(data._id))){
                    preparePlayerWithUrl(data);
                } else{
                    fetchUrl();
                }
            }
        }
    };

    private void fetchUrl() {
        boolean isLoggedIn = Util.checkUserLoginStatus();

        if (!isLoggedIn)
            return;
        MediaLinkEncrypted.Params mediaLinkparams;
        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();

        boolean isContentPartnerDisable = false;
        try {
            String blockAutoplayContentPartners = PrefUtils.getInstance().getBlockAutoplayContentPartners();
            if(blockAutoplayContentPartners != null) {
                String[] blockAutoplayCPValues = blockAutoplayContentPartners.split(",");
                for (String value : blockAutoplayCPValues) {
                    if (null != mCardDataList && null != mCardDataList.get(0) && (mCardDataList.get(0).getPublishingHouseId() == Integer.parseInt(value))) {
                        isContentPartnerDisable = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // if (null != mCardDataList && null != mCardDataList.get(0) && (mCardDataList.get(0).isProgram() || mCardDataList.get(0).isLive() || mCardDataList.get(0).isVOD())) {
        if (!isContentPartnerDisable) {
            if (null != mCardDataList && null != mCardDataList.get(0)) {
                String contentId = mCardDataList.get(0)._id;
                mediaLinkparams = new MediaLinkEncrypted.Params(contentId, SDKUtils.getInternetConnectivity(mContext), "", locationInfo);

                MediaLinkEncrypted mMedialLink = new MediaLinkEncrypted(mediaLinkparams, mMediaLinkFetchListener);
                APIService.getInstance().execute(mMedialLink);
                System.out.println("URLS-"+mMedialLink);
                //  Toast.makeText(mContext, "hello--"+mMedialLink, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private APICallback mMediaLinkFetchListener = new APICallback<CardResponseData>() {

        @Override
        public void onResponse(APIResponse<CardResponseData> response) {
            if (response != null && response.body() != null && response.body().results != null) {
                for (int pos = 0; pos < response.body().results.size(); pos++) {

                    data = response.body().results.get(pos);
                    if(data != null) {
                        CardDataVideos videos = data.videos;
                        if (videos != null && videos.values != null && videos.values.size() > 0) {
                            String contentUrl = videos.values.get(0).link;
                            contentUrls.put(data._id, contentUrl);
                            preparePlayerWithUrl(data);
                        }
                    }
                }
            }
        }


        @Override
        public void onFailure(Throwable t, int errorCode) {
            Log.e("MEDIA LINK ENCRYPTED ", "FAIL : " + errorCode);
        }
    };


    private void preparePlayerWithUrl(final CardData cardDataa) {
        if (cardDataa == null) return;

        if(!checkIfPlayableTVODContent(cardDataa)) {
            return;
        }

        if (!shouldStartPlayback()) {
            return;
        }
        // get targetPosition in RecyclerView
        playPosition = 0;
        bufferCount = 0;
        //CleverTap.eventVideoDetailsViewed(contentModel,cardDataa, cardDataa, APIConstants.CAROUSEL_AUTOPLAY, APIConstants.SOURCE_DETAIL_AUTOPLAY, Util.checkUserLoginStatus(), currentPage, carouselPosition, 0);

        initialize(mContext, cardDataa);

        int targetPosition = getTargetPositionFromRecyclerView();
        // Log.e("Target POS",String.valueOf(targetPosition));
        if (targetPosition < 0) {
            return;
        }
        AdapterSingleBannerRecyclerView.CarouselDataViewHolder mPlayerItemViewHolder;
        try {
            mPlayerItemViewHolder = (AdapterSingleBannerRecyclerView.CarouselDataViewHolder) findViewHolderForAdapterPosition(targetPosition);
        } catch (Exception e) {
            pausePlayer();
            return;
        }
        if (mPlayerItemViewHolder == null) {
            //  Log.e("Inline",String.valueOf(targetPosition));
            return;
        }
        mCoverImage = mPlayerItemViewHolder.itemView.findViewById(R.id.imageview_thumbnail_voditem);
        mMuteButton = mPlayerItemViewHolder.itemView.findViewById(R.id.mutebuttoniv);
        videoSurfaceView = mPlayerItemViewHolder.itemView.findViewById(R.id.playerSurfaceView);
        partnerLogoImageView = mPlayerItemViewHolder.itemView.findViewById(R.id.iv_partener_logo_right);
        userBadgeImage = mPlayerItemViewHolder.itemView.findViewById(R.id.content_badge);
        MOUTracker.captureThePlaybackTriggerTime();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.requestFocus();
        videoSurfaceView.setSecure(true);
        videoSurfaceView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                pausePlayer();
            }
        });
        mMuteButton.setOnClickListener(view -> {
            if (playerInterface != null) {
                if (playerInterface.getPlayerVolume() == 0) {
                    ApplicationController.shouldMuteAutoPlayContents.put(cardDataa._id, "true");
                    mMuteButton.setImageResource(R.drawable.unmute_icon);
                    playerInterface.setPlayerVolume(PLAYER_VOLUME_UNMUTE);
                    isVideoUnMuteState = true;
                } else {
                    ApplicationController.shouldMuteAutoPlayContents.put(cardDataa._id, "false");
                    mMuteButton.setImageResource(R.drawable.mute_icon);
                    playerInterface.setPlayerVolume(PLAYER_VOLUME_MUTE);
                    isVideoUnMuteState = false;
                }
            }
        });

        if (Boolean.parseBoolean(ApplicationController.shouldMuteAutoPlayContents.get(cardDataa._id))) {
            mMuteButton.setImageResource(R.drawable.unmute_icon);
        }
        else {
            mMuteButton.setImageResource(R.drawable.mute_icon);
            if(playerInterface!=null) {
                playerInterface.setPlayerVolume(PLAYER_VOLUME_MUTE);
            }
        }

        if(isVideoUnMuteState)
            mMuteButton.performClick();
        String uriString = contentUrls.get(cardDataa._id);
        if (uriString != null) {
            int[] bitrates = getSDorHDBitrates(uriString);
            if (bitrates != null) {
                minAutoBitrateToSupport = bitrates[0] * 1000;
                maxAutoBitrateToSupport = bitrates[1] * 1000;
            }

            MOUTracker.firstFrameRendered = false;
            MOUTracker.prepareContentToPlay = true;
            if (player == null) {
                String deviceModel = Build.MODEL;
                String deviceMake = Build.MANUFACTURER;

                String drmLicenseUrl;

                if (cardDataa.videos != null && cardDataa.videos.values != null && cardDataa.videos.values.size() > 0 && cardDataa.videos.values.get(0) != null && cardDataa.videos.values.get(0).licenseUrl != null) {
                    drmLicenseUrl = cardDataa.videos.values.get(0).licenseUrl;
                } else {
                    drmLicenseUrl = APIConstants.SCHEME + APIConstants.BASE_URL + APIConstants.LICENSE_URL + "?content_id=" + cardDataa._id +
                            "&licenseType=streaming&timestamp=" + System.currentTimeMillis()
                            + "&clientKey=" + PrefUtils.getInstance().getPrefClientkey();
                }

                Map<String, String> keyRequestProperties = new HashMap<>();
                keyRequestProperties.put("deviceMake", deviceMake);
                keyRequestProperties.put("deviceModel", deviceModel);
                ConstructPlayer.PlayerConfigBuilder playerConfigBuilder = new ConstructPlayer.PlayerConfigBuilder(mContext);
                playerConfigBuilder.setContentId(cardDataa._id);
                playerConfigBuilder.setDrmUrl(drmLicenseUrl);

                playerConfigBuilder.setPlayUrl(uriString)
                        .setKeyRequestProperties(keyRequestProperties)
                        .setMinVideoBitrate(minAutoBitrateToSupport)
                        .setMaxVideoBitrate(maxAutoBitrateToSupport)
                        .setPlayerSurfaceHolder(videoSurfaceView.getHolder());
                BufferConfig bufferConfig = getBufferConfig();
                if (ApplicationController.shouldUseCustomLoadControl && bufferConfig != null) {
                    playerConfigBuilder.setBufferConfig(bufferConfig);
                    playerConfigBuilder.setShouldUseCustomLoadControl(true);
                }
                playerConfigBuilder.setPlayerCallbackInterface(mPlayerCallbackInterface);
                if (cardDataa.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                        || cardDataa.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
                    playerConfigBuilder.setShouldUseLiveSeekWindow(true);
                }

                if(cardDataa.videos != null && cardDataa.videos.values != null
                        && cardDataa.videos.values.size() > 0 && !TextUtils.isEmpty(cardDataa.videos.values.get(0).link)) {
                    String videoURL = cardDataa.videos.values.get(0).link;
                    if (videoURL.contains(".m3u8")) {
                        playerInterface = new PlayerConfig().getPlayerInstance(HLS_PLAYER);
                    } else if (videoURL.contains(".mpd")) {
                        playerInterface = new PlayerConfig().getPlayerInstance(DASH_PLAYER);
                    } else {
                        playerInterface = new PlayerConfig().getPlayerInstance(PROGRESSIVE_PLAYER);
                    }
                }else{
                    if (cardDataa.generalInfo.type.equalsIgnoreCase(TYPE_LIVE)
                            || (cardDataa.generalInfo.type.equalsIgnoreCase(TYPE_PROGRAM))) {
                        playerInterface = new PlayerConfig().getPlayerInstance(HLS_PLAYER);
                    } else {
                        playerInterface = new PlayerConfig().getPlayerInstance(DASH_PLAYER);
                    }
                }
                player = new MyplexPlayer();
                player.setInterface(playerInterface);
                player.preparePlayer(playerConfigBuilder.build(), MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.PAUSE);

                AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                if (audio != null && !Boolean.parseBoolean(ApplicationController.shouldMuteAutoPlayContents.get(cardDataa._id))) {
                    if(playerInterface!=null){
                    playerInterface.setPlayerVolume(0);
                    //AUTO Play mute is off So no volume will play
                    if(mPreviewProperties.previewVideoConfig.AUTOPLAY_MUTE.equalsIgnoreCase("OFF")) {
                        playerInterface.setPlayerVolume(0);
                    }
                    }
                }

            }
        }

        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prevPlayPos = playPosition;
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

    private int[] getBitratesAsInt(String[] bitratesString) {
        String[] bitRatestoConvert = bitratesString[1].split("-");
        int[] bitrates = new int[2];
        //LogUtils.error(TAG, bitRatestoConvert[0] + bitRatestoConvert[1]);
        bitrates[0] = Integer.parseInt(bitRatestoConvert[0]);
        bitrates[1] = Integer.parseInt(bitRatestoConvert[1]);
        return bitrates;
    }

    private boolean checkIfPlayableTVODContent(CardData cardDataa) {

        List<CardDataPackages> packages = cardDataa.packages;

        CardDataCurrentUserData currentUserData = cardDataa.currentUserData;
        if(cardDataa.generalInfo != null
                && cardDataa.generalInfo.contentRights != null
                && cardDataa.generalInfo.contentRights.size() > 0) {

            if(cardDataa.generalInfo.contentRights.contains("tvod")) {
                if(packages != null) {
                    for (final CardDataPackages cardDataPackage : packages) {
                        if (cardDataPackage != null
                                && !TextUtils.isEmpty(cardDataPackage.subscriptionType)
                                && cardDataPackage.subscriptionType.equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {

                            if (currentUserData != null && currentUserData.purchase != null && currentUserData.purchase.size() > 0) {
                                for (CardDataPurchaseItem cardDataPurchaseItem : currentUserData.purchase) {
                                    if (cardDataPackage.packageId.equalsIgnoreCase(cardDataPurchaseItem.packageId)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    private boolean shouldStartPlayback() {
        mPreviewProperties = PropertiesHandler.getPreviewProperties(mContext);
        if (!isPreviewPlaybackEnabledForClientOS()) {
            return false;
        }
        if (ApplicationController.didPlayerEncounterError) {
            return false;
        }
        if (!isValidConditionToPlay()) {
            //Network Check
            return false;
        }
        if (!isPreviewPlaybackEnabled()) {
            //Preview is not enabled, no playback
            return false;
        }
        if (!doesPreviewNetworkMatch()) {
            //Network did not match no playback
            return false;
        }
        if (!isDeviceModelSupported()) {
            //Device Support is disabled no playback
            return false;
        }
        if (!isDeviceResolutionSupported()) {
            return false;
        }
        if (!isMinMemoryAvailable()) {
            //Not enough Memory as per API no playback
            return false;
        }

        return true;
    }

    private boolean isPreviewPlaybackEnabledForClientOS() {
        try {
            if (mPreviewProperties != null
                    && mPreviewProperties.previewVideoConfig != null
                    && !TextUtils.isEmpty(mPreviewProperties.previewVideoConfig.PREVIEW_DISABLE_ANDROID_OS)) {
                int sdk_version = Build.VERSION.SDK_INT;
                //Log.e("BlackListing","sdk_version"+sdk_version);
                if (sdk_version > 18) {
                    String sdk_versionString = String.valueOf(sdk_version);
                    List<String> list = Arrays.asList(mPreviewProperties.previewVideoConfig.PREVIEW_DISABLE_ANDROID_OS.split(","));
                    if (list.size() > 0) {
                        //Log.e("BlackListing", "sdk_version" + list.toString());
                        //Log.e("BlackListing","Found");
                        return !list.contains(sdk_versionString);
                    }
                    return true;
                }


            }
            //Log.e("BlackListing","Not Found");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidConditionToPlay() {
        return Util.isNetworkAvailable(mContext);
    }

    private boolean isPreviewPlaybackEnabled() {
        return mPreviewProperties != null
                && mPreviewProperties.previewVideoConfig != null
                && mPreviewProperties.previewVideoConfig.PREVIEWS_ENABLE;
    }

    private boolean doesPreviewNetworkMatch() {
        if (mPreviewProperties == null
                || mPreviewProperties.previewVideoConfig == null
                || mPreviewProperties.previewVideoConfig.AUTOPLAY_NETWORK == null) {
            return false;
        }
        List<String> network = mPreviewProperties.previewVideoConfig.AUTOPLAY_NETWORK;
        String currentConn = SDKUtils.getInternetConnectivity(mContext);
        if (network.size() > 0) {
            for (int i = 0; i < network.size(); i++) {
                if (network.get(i).equalsIgnoreCase("ANY")) {
                    return true;
                } else {
                    if (network.get(i).equalsIgnoreCase(currentConn)) {
                        return true;
                    }
                }
            }
        } else
            return false;

        return false;
    }

    private boolean isDeviceModelSupported() {
        if (mPreviewProperties != null
                && mPreviewProperties.previewVideoConfig != null
                && mPreviewProperties.previewVideoConfig.PREVIEWS_BLACKLIST_DEVICES != null) {
            if (mPreviewProperties.previewVideoConfig.PREVIEWS_BLACKLIST_DEVICES.size() > 0) {
                return !mPreviewProperties.previewVideoConfig.PREVIEWS_BLACKLIST_DEVICES.contains(Build.MODEL);
            }
        }
        return true;
    }

    private boolean isDeviceResolutionSupported() {
        String devResolution = UiUtil.getScreenDensity(mContext);
        if (mPreviewProperties != null && mPreviewProperties.previewVideoConfig != null
                && mPreviewProperties.previewVideoConfig.PREVIEWS_MIN_RESOLUTION_ANDROID != null) {
            if (devResolution.equalsIgnoreCase(mPreviewProperties.previewVideoConfig.PREVIEWS_MIN_RESOLUTION_ANDROID)) {
                return true;
            }
            int currentDensity = convertDensityStringToInt(devResolution);
            int serverDensityChoice = convertDensityStringToInt(mPreviewProperties.previewVideoConfig.PREVIEWS_MIN_RESOLUTION_ANDROID);
            if(DeviceUtils.isTablet(mContext)) {
                return currentDensity <= serverDensityChoice;
            }else{
                return currentDensity >= serverDensityChoice;
            }
        }
        return true;
    }

    private int convertDensityStringToInt(String density) {
        int densityInt = 0;
        switch (density) {
            case MDPI:
                densityInt = 0;
                break;
            case HDPI:
                densityInt = 1;
                break;
            case XHDPI:
                densityInt = 2;
                break;
            case XXHDPI:
                densityInt = 3;
                break;
        }
        return densityInt;
    }

    private boolean isMinMemoryAvailable() {

        ActivityManager actManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }
        long totalMemory = memInfo.totalMem / 1000000;
        int minMemory = Integer.parseInt(mPreviewProperties.previewVideoConfig.PREVIEWS_MIN_MEMORY);
        return (int) totalMemory > minMemory;
    }

    private int getTargetPositionFromRecyclerView() {
        if (getLayoutManager() == null) return 0;
        int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

        if (endPosition - startPosition > 1) {
            endPosition = startPosition + 1;
        }
        if (startPosition < 0 || endPosition < 0) {
            return 0;
        }
        int targetPosition;
        if (startPosition != endPosition) {
            int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
            int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);
            targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
        } else {
            targetPosition = startPosition;
        }

        return targetPosition;
    }

    private int getVisibleVideoSurfaceHeight(int playPosition) {
        if (getLayoutManager() == null) return 0;
        int at = playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }
        int[] location01 = new int[2];
        child.getLocationInWindow(location01);
        if (location01[1] < 0) {
            return location01[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location01[1];
        }
    }

    @Override
    public void onDocPlayerClosed() {
        ApplicationController.isMiniPlayerEnabled = false;
        if(mMuteButton!=null) {
            mMuteButton.setEnabled(true);
        }
        if (isAttachedToWindow() && !isContentPlayed) {
            pausePlayer();
            tryPlayingContent();
            if(getAdapter() != null) {
                getAdapter().notifyDataSetChanged();
            }
        }

    }

    @Override
    public void onInternetConnected() {
//        initialize(mContext, data);
        if (getAdapter() != null && isAttachedToWindow() && !isContentPlayed) {
            pausePlayer();
            //  System.out.println("qwerty--"+((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized());
            if (((MainActivity)mContext) != null && ((MainActivity)mContext).mFragmentCardDetailsPlayer != null && ((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer != null && !((MainActivity)mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized()) {
                tryPlayingContent();
            }
        }

    }
}