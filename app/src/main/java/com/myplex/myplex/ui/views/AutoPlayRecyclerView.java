package com.myplex.myplex.ui.views;

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
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.PreviewData;
import com.myplex.model.PreviewDataValues;
import com.myplex.model.PreviewProperties;
import com.myplex.player_config.PlayerConfig;
import com.myplex.player_sdk.ConstructPlayer;
import com.myplex.player_sdk.MyplexPlayer;
import com.myplex.player_sdk.MyplexPlayerInterface;
import com.myplex.player_sdk.PlayerCallbackInterface;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.utils.MOUTracker;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.rd.PageIndicatorView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.myplex.model.ApplicationConfig.HDPI;
import static com.myplex.model.ApplicationConfig.MDPI;
import static com.myplex.model.ApplicationConfig.XHDPI;
import static com.myplex.model.ApplicationConfig.XXHDPI;
import static com.myplex.player_config.PlayerConfig.PlayerType.PROGRESSIVE_PLAYER;

public class AutoPlayRecyclerView extends RecyclerView {


    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private MyplexPlayer player;
    private int playPosition;
    private ImageView mCoverImage;
    private ImageView mMuteButton;
    private View rowParent;
    private SurfaceView videoSurfaceView;
    private AdapterAutoPlayRecyclerView.CarouselDataViewHolder mPlayerItemViewHolder;
    private int recyclerViewChildrenSize;
    private Context mContext;
    private int POST_AUTO_PLAY_WAIT_TIME = 2500;
    private int PRE_AUTO_PLAY_WAIT_TIME = 2000;
    private int AUTO_PLAY_VIDEO_TIME_OUT = 5000;
    private boolean didPlayerPlay = false;
    private boolean didUserReleaseFinger = true;
    private static final int SHOW_PROGRESS = 2;
    private BroadcastReceiver localBroadCastReceiver;
    private String currentPage = ApplicationController.FIRST_TAB_NAME;
    private String mMenuGroup ;
    private PreviewData mPreviewData;
    private PreviewDataValues currentPreviewData;
    private List<CardData> mCardDataList = new ArrayList<>();
    private CardData presentCardData;
    private PreviewProperties mPreviewProperties;
    private boolean isActivityPaused = false;
    private boolean isMiniPlayerEnabled = false;
    private int MAX_POSITIONS_TO_SCROLL = Integer.MAX_VALUE;
    private ConcurrentHashMap<String,Integer> previewVideoCountMap;
    private int playerDuration = 0;
    private int prevPlayPos;
    private final int PLAYER_VOLUME_MUTE = 0;
    private final int PLAYER_VOLUME_UNMUTE = 1;
    private int carouselPosition;
    private boolean isContentPlayed = false;
    private MOUTracker mMouTracker;
    private int bufferCount = 0;
    private PageIndicatorView pageIndicatorView;
    private TextView mCountText;

    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    private ProgressBar mProgressBar;
    private String contentUrl;

    private MyplexPlayerInterface playerInterface;

    public void setListCardData(List<CardData> mCardDataList) {
        this.mCardDataList = mCardDataList;
    }
    //Handlers to control AutoScroll and Playback TimeOut
    Handler cyclingHandler = new Handler();
    Handler playbackTestHandler = new Handler();
    Handler playBackTimeOutHandler = new Handler();

    Runnable playBackTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if(didPlayerPlay){
                return;
            }
            if(isAttachedToWindow()){
                if (player != null && playerInterface != null && !playerInterface.getPlayWhenReady()) {
                    player.pausePlayer();
                }
                cycleToNextContent();
            }
        }
    };



    Runnable cyclingRunnable = new Runnable() {
        @Override
        public void run() {
            if(didPlayerPlay){
                return;
            }
            try {
                if (isAttachedToWindow()) {
                    if(playPosition+1 < MAX_POSITIONS_TO_SCROLL){
                        playPosition++;
                        // Log.e("Inline","Scroll to pos in thread");
                        LayoutManager layoutManager = getLayoutManager();
                        if( layoutManager!= null)
                            layoutManager.smoothScrollToPosition(AutoPlayRecyclerView.this,null,playPosition);
                    }
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    };

    Runnable playbackRunnable = new Runnable() {
        @Override
        public void run() {
            if(isActivityPaused){
                return;
            }
            if(ApplicationController.isMiniPlayerEnabled){
                //cycleToNextContent();
                return;
            }

            if (getAdapter() != null) {
                mMenuGroup = ((CyclicRecyclerViewAdapter)getAdapter()).getTabName();
            }

            if(currentPage != null && !currentPage.equalsIgnoreCase(mMenuGroup)){
                return;
            }
            if (isAttachedToWindow()) {
                if(!shouldStartPlayback()){
                    cycleToNextContent();
                    return;
                }
                if (isUrlPresent()) {
                    preparePlayerWithUrl();
                }else{
                    cycleToNextContent();
                }
            }
        }
    };

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
                    if(intent.getAction().equalsIgnoreCase(APIConstants.PAUSE_BROADCAST)){
                        pausePlayer();
                        isActivityPaused = true;
                    }else if(intent.getAction().equalsIgnoreCase(APIConstants.RESUME_BROADCAST)){
                        tryPlayingContent();
                        isActivityPaused = false;
                    }else if(intent.getAction().equalsIgnoreCase(APIConstants.PAGE_CHANGE_BROADCAST)){
                        if(intent.hasExtra(APIConstants.TAB_NAME)){
                            currentPage = intent.getStringExtra(APIConstants.TAB_NAME);
                            pausePlayer();
                            tryPlayingContent();
                        }
                    }else if(intent.getAction().equalsIgnoreCase(APIConstants.MINI_PLAYER_ENABLED_BROADCAST)){
                        pausePlayer();
                        isMiniPlayerEnabled = true;
                        //ApplicationController.isMiniPlayerEnabled = true;
                        cycleToNextContent();
                    }else if(intent.getAction().equalsIgnoreCase(APIConstants.MINI_PLAYER_DISABLED_BROADCAST)){
                        isMiniPlayerEnabled = false;
                        ApplicationController.isMiniPlayerEnabled = false;
                        cycleToNextContent();
                    }
                }
            }
        };
        ApplicationController.getLocalBroadcastManager().registerReceiver(localBroadCastReceiver,filter);

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case SHOW_PROGRESS:
                    setProgress();
                    msg = obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(msg, 50);
                    break;
            }
        }
    };

    private void removeProgressUpdateHandlerMessages(){
        if (mHandler != null) {
            mHandler.removeMessages(SHOW_PROGRESS);
        }
        if(mProgressBar != null){
            mProgressBar.setProgress(0);
        }
    }

    public void setPagerIndicator(PageIndicatorView pageIndicatorView){
        this.pageIndicatorView=pageIndicatorView;
    }

    public void setCountText(TextView textView){
        this.mCountText=textView;
    }

    private void setProgress() {
        if(prevPlayPos != playPosition){
            pausePlayer();
        }
        if(mProgressBar != null && player != null){
            mProgressBar.setProgress((int)player.getCurrentPosition());
            playerDuration = (int)(player.getCurrentPosition()/1000);
        }
    }

    public int getCurrentPlayerVolume() {
        return currentPlayerVolume;
    }

    private AudioManager getAudioManager(){
        return (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }
    private int currentPlayerVolume = 0;

    public AutoPlayRecyclerView(Context context) {
        super(context);
        mContext = context;
        initialize(context);
    }

    public AutoPlayRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initialize(context);
    }

    public AutoPlayRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initialize(context);
    }
    public void startAutoScroll() {
        cycleToNextContent();
    }

    public void setStopScrollWhenTouch(boolean b) {
        //TODO
    }

    public void setInterval(int timer) {
        //TODO
    }

    public void setAutoScrollDurationFactor(int i) {
        //TODO
    }



    private void initialize(Context mContext) {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoSurfaceDefaultHeight = point.x / 4;
        screenDefaultHeight = point.y;
        addOnScrollListener(mOnScrollListener);
        addOnChildAttachStateChangeListener(mChildAttachStateChangeListener);
        // Log.e("Inline","Adding Player Listener");
        registerBroadcastReceiver();
        mPreviewProperties = PropertiesHandler.getPreviewProperties(mContext);
        if (mPreviewProperties != null && mPreviewProperties.previewVideoConfig !=  null) {
            PRE_AUTO_PLAY_WAIT_TIME = mPreviewProperties.previewVideoConfig.AUTOPLAY_WAIT_TIME;
            POST_AUTO_PLAY_WAIT_TIME =  mPreviewProperties.previewVideoConfig.POST_AUTO_PLAY_WAIT_TIME;
            AUTO_PLAY_VIDEO_TIME_OUT =  mPreviewProperties.previewVideoConfig.AUTO_PLAY_VIDEO_TIME_OUT;
        }
    }



    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releasePlayer();
        if(isContentPlayed) {
            Log.e("MOUTRACKER_AUTOPLAY","STOP MOU Tracker");
            stopMOUTracking();
            isContentPlayed = false;
        }
        PrefUtils.getInstance().savePreviewHashMap(previewVideoCountMap);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initialize(mContext);
        tryPlayingContent();

    }

    public void setSizeOfChildren(int childrenSize){
        this.recyclerViewChildrenSize = childrenSize;
    }


    public void setCycle(boolean isCyclic) {

    }

    public void setPageMargin(int dimension) {

        //TODO Add code to setup page margin
    }

    public void setCurrentItem(int itemPosition) {
        //TODO add code to scroll to specific position
        // Log.e("Inline","Scroll to pos in current item");
        int middlePos = Integer.MAX_VALUE/recyclerViewChildrenSize/2;
        if (getLayoutManager() != null) {
            getLayoutManager().scrollToPosition(middlePos * recyclerViewChildrenSize);
        }
        playPosition = middlePos * recyclerViewChildrenSize;
    }



    public void cycleToNextContent(){
        if(mCoverImage != null && mCoverImage.getVisibility() != VISIBLE){
            Animation fadeOut = new AlphaAnimation(0, 1);
            fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
            fadeOut.setStartOffset(800);
            fadeOut.setDuration(350);
            mCoverImage.setAnimation(fadeOut);
            Log.d("Inline","Cover Image Visible");
            mCoverImage.setVisibility(VISIBLE);
        }
        if(cyclingHandler != null){
            cyclingHandler.removeCallbacks(cyclingRunnable);
            cyclingHandler.postDelayed(cyclingRunnable,POST_AUTO_PLAY_WAIT_TIME);
        }
    }



    private RecyclerView.OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            playPosition = getTargetPositionFromRecyclerView();
            if (pageIndicatorView!=null){
                int contentPosition=0;
                if(mCardDataList != null) {
                    contentPosition = playPosition % mCardDataList.size();
                }
                pageIndicatorView.setSelection(contentPosition);
            }
            if (mCountText!=null){
                int contentPosition=0;
                if(mCardDataList != null) {
                    contentPosition = playPosition % mCardDataList.size();
                    mCountText.setText(contentPosition+1+"/"+mCardDataList.size());
                }
            }
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                LoggerD.debugLog("Inline "+" Trying to play content");
                hideUIElements();
                tryPlayingContent();
            }else{
                playBackTimeOutHandler.removeCallbacks(playBackTimeOutRunnable);
                LoggerD.debugLog("Inline  "+" Trying to pause content");
                pausePlayer();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };



    public void tryPlayingContent(){
        if (playbackTestHandler != null) {
            playbackTestHandler.removeCallbacks(playbackRunnable);
            playbackTestHandler.postDelayed(playbackRunnable,PRE_AUTO_PLAY_WAIT_TIME);
        }
    }

    private boolean isUrlPresent() {
        if(mPreviewProperties != null
                && mPreviewProperties.previewVideoConfig != null){
            if (mPreviewProperties.previewVideoConfig.AUTOPLAY_MUTE.equalsIgnoreCase("ON")) {
                if(currentPreviewData != null && currentPreviewData.link != null){
                    contentUrl = currentPreviewData.link;
                    return true;
                }
            }else if(mPreviewProperties.previewVideoConfig.AUTOPLAY_MUTE.equalsIgnoreCase("OFF")){
                if(currentPreviewData != null && currentPreviewData.alternateLink != null){
                    contentUrl = currentPreviewData.alternateLink;
                    return true;
                }else if(currentPreviewData != null && currentPreviewData.link != null){
                    contentUrl = currentPreviewData.link;
                    return true;
                }
            }else {
                return false;
            }
        }
        return false;
    }

    private boolean shouldStartPlayback() {
        boolean isLoggedIn = Util.checkUserLoginStatus();
        String bannerPreviewPlaybackEnabled = PrefUtils.getInstance().getBannerPreviewPlaybackEnabled();

        Log.e("PREVIEW_ENABLE","bannerPreviewPlaybackEnabled : "+bannerPreviewPlaybackEnabled);
        Log.e("PREVIEW_ENABLE","isLoggedIn : "+ isLoggedIn);

        if (bannerPreviewPlaybackEnabled != null && bannerPreviewPlaybackEnabled.equalsIgnoreCase("registered")) {
            if(!isLoggedIn)
                return false;
        } else if(bannerPreviewPlaybackEnabled != null && bannerPreviewPlaybackEnabled.equalsIgnoreCase("off")) {
            return false;
        }


        if(!isPreviewPlaybackEnabledForClientOS()){
            return false;
        }
        if(ApplicationController.didPlayerEncounterError){
            return false;
        }
        if(!isValidConditionToPlay()){
            //Network Check
            return false;
        }
        if(!isPreviewPlaybackEnabled()){
            //Preview is not enabled, no playback
            return false;
        }
        if(!doesPreviewNetworkMatch()){
            //Network did not match no playback
            return false;
        }
        if(!isDeviceModelSupported()){
            //Device Support is disabled no playback
            return false;
        }
        if(!isDeviceResolutionSupported()){
            return false;
        }
        if(!isMinMemoryAvailable()){
            //Not enough Memory as per API no playback
            return false;
        }
        currentPreviewData = getCurrentPreviewData();
        if(currentPreviewData == null){
            //Preview Data Null, no point in checking more
            return false;
        }
        int maxPreview = 0;
        try {
            maxPreview = Integer.parseInt(currentPreviewData.maxPreviews);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if(maxPreview == 0){
            //Always play as MaxPreview is zero
            return true;
        }
        if(presentCardData == null || presentCardData._id == null){
            return false;
        }
        if(previewVideoCountMap == null){
            return true;
        }
        if(!previewVideoCountMap.containsKey(presentCardData._id)){
            return true;
        }
        if(previewVideoCountMap.containsKey(presentCardData._id)){
            return previewVideoCountMap.get(presentCardData._id) <= maxPreview;
        }
        return false;
    }

    private boolean isDeviceResolutionSupported() {
        String devResolution = UiUtil.getScreenDensity(mContext);
        if(mPreviewProperties != null && mPreviewProperties.previewVideoConfig != null
                && mPreviewProperties.previewVideoConfig.PREVIEWS_MIN_RESOLUTION_ANDROID != null){
            if(devResolution.equalsIgnoreCase(mPreviewProperties.previewVideoConfig.PREVIEWS_MIN_RESOLUTION_ANDROID)){
                return true;
            }
            int currentDensity = convertDensityStringToInt(devResolution);
            int serverDensityChoice = convertDensityStringToInt(mPreviewProperties.previewVideoConfig.PREVIEWS_MIN_RESOLUTION_ANDROID);
            return currentDensity >= serverDensityChoice;
        }
        return true;
    }

    private boolean isMinMemoryAvailable() {

        ActivityManager actManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }
        long totalMemory = memInfo.totalMem/1000000;
        int minMemory = Integer.parseInt(mPreviewProperties.previewVideoConfig.PREVIEWS_MIN_MEMORY);
        return (int)totalMemory > minMemory;
    }

    private boolean isDeviceModelSupported() {

        if(mPreviewProperties != null
                && mPreviewProperties.previewVideoConfig != null
                && mPreviewProperties.previewVideoConfig.PREVIEWS_BLACKLIST_DEVICES != null){
            if (mPreviewProperties.previewVideoConfig.PREVIEWS_BLACKLIST_DEVICES.size() > 0) {
                return !mPreviewProperties.previewVideoConfig.PREVIEWS_BLACKLIST_DEVICES.contains(Build.MODEL);
            }
        }
        return true;
    }

    private boolean doesPreviewNetworkMatch() {
        if(mPreviewProperties == null
                || mPreviewProperties.previewVideoConfig == null
                || mPreviewProperties.previewVideoConfig.AUTOPLAY_NETWORK == null){
            return false;
        }
        List<String> network = mPreviewProperties.previewVideoConfig.AUTOPLAY_NETWORK;
        String currentConn = SDKUtils.getInternetConnectivity(mContext);
        if (network.size() > 0) {
            for(int i =0; i<network.size();i++){
                if(network.get(i).equalsIgnoreCase("ANY")){
                    return true;
                }else{
                    if(network.get(i).equalsIgnoreCase(currentConn)){
                        return true;
                    }
                }
            }
        }else
            return false;

        return false;
    }

    private boolean isPreviewPlaybackEnabled() {
        return mPreviewProperties != null
                && mPreviewProperties.previewVideoConfig != null
                && mPreviewProperties.previewVideoConfig.PREVIEWS_ENABLE;
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
                    if (list != null && list.size() > 0) {
                        //Log.e("BlackListing", "sdk_version" + list.toString());
                        if (list.contains(sdk_versionString)) {
                            //Log.e("BlackListing","Found");
                            return false;
                        }
                    }
                    return true;
                }


            }
            //Log.e("BlackListing","Not Found");
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidConditionToPlay(){
        return Util.isNetworkAvailable(mContext);
    }

    private PreviewDataValues getCurrentPreviewData() {
        if (mCardDataList != null && mCardDataList.size() > 0) {
            presentCardData = mCardDataList.get(playPosition % mCardDataList.size());
            if (presentCardData != null) {
                mPreviewData = presentCardData.previews;
            }else{
                return null;
            }
        }
        if(mPreviewData != null && mPreviewData.values != null && mPreviewData.values.size() > 0){
            for (int i = 0; i< mPreviewData.values.size(); i++){
                if(mPreviewData.values.get(i).layoutType.equalsIgnoreCase("portraitBanner")
                        && mPreviewData.values.get(i).previewType.equalsIgnoreCase("PromoPlay")){
                    currentPreviewData = mPreviewData.values.get(i);
                    break;
                }else{
                    currentPreviewData = null;
                }
            }
        }else {
            currentPreviewData = null;
        }
        return currentPreviewData;
    }

    private void preparePlayerWithUrl() {
        bufferCount = 0;
        // get targetPosition in RecyclerView
        initialize(mContext);
        int targetPosition = getTargetPositionFromRecyclerView();
        // Log.e("Target POS",String.valueOf(targetPosition));
        if (targetPosition < 0 ) {
            return;
        }
        //  Log.e("Inline",String.valueOf(targetPosition));
        playPosition = targetPosition;
        //Using try-catch to trigger the logic only for SpecificItems
        try {
            mPlayerItemViewHolder  = (AdapterAutoPlayRecyclerView.CarouselDataViewHolder) findViewHolderForAdapterPosition(targetPosition);
            if (mCoverImage != null) {
                Log.d("Inline","Cover Image Visible");
                mCoverImage.setVisibility(VISIBLE);
            }
        } catch (Exception e) {
            pausePlayer();
            return;
        }
        if (mPlayerItemViewHolder == null) {
            //  Log.e("Inline",String.valueOf(targetPosition));
            return;
        }
        mCoverImage = mPlayerItemViewHolder.itemView.findViewById(R.id.slider_image);
        mMuteButton = mPlayerItemViewHolder.itemView.findViewById(R.id.muteButton);
        videoSurfaceView = mPlayerItemViewHolder.itemView.findViewById(R.id.inlinePlayerSurfaceView);
        rowParent = mPlayerItemViewHolder.itemView;
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.requestFocus();
        videoSurfaceView.setSecure(true);
        videoSurfaceView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    pausePlayer();
                }
            }
        });
        mMuteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playerInterface != null) {
                    if (playerInterface.getPlayerVolume() == 0) {
                        ApplicationController.shouldMutePreviewContent = false;
                        mMuteButton.setImageResource(R.drawable.volume_icon);
                        playerInterface.setPlayerVolume(PLAYER_VOLUME_UNMUTE);
                    } else {
                        ApplicationController.shouldMutePreviewContent = true;
                        mMuteButton.setImageResource(R.drawable.mute_icon);
                        playerInterface.setPlayerVolume(PLAYER_VOLUME_MUTE);
                    }
                }
            }
        });
        mMuteButton.setImageResource(R.drawable.mute_icon);

        String uriString = contentUrl;
        if (uriString != null) {

            MOUTracker.firstFrameRendered = false;
            MOUTracker.prepareContentToPlay = true;
            if (player == null) {
                String deviceModel = Build.MODEL;
                String deviceMake = Build.MANUFACTURER;

                Map<String, String> keyRequestProperties = new HashMap<>();
                keyRequestProperties.put("deviceMake", deviceMake);
                keyRequestProperties.put("deviceModel", deviceModel);
                ConstructPlayer.PlayerConfigBuilder playerConfigBuilder = new ConstructPlayer.PlayerConfigBuilder(mContext);
                playerConfigBuilder.setContentId(presentCardData._id);
                playerConfigBuilder.setPlayUrl(uriString)
                        .setKeyRequestProperties(keyRequestProperties)
                        .setPlayerSurfaceHolder(videoSurfaceView.getHolder());

                playerConfigBuilder.setPlayerCallbackInterface(mPlayerCallbackInterface);

                playerInterface = new PlayerConfig().getPlayerInstance(PROGRESSIVE_PLAYER);

                player = new MyplexPlayer();
                player.setInterface(playerInterface);
                player.preparePlayer(playerConfigBuilder.build(),MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.PAUSE);
                try {
                    AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                    if (audio != null && ApplicationController.shouldMutePreviewContent) {
                        currentPlayerVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                        playerInterface.setPlayerVolume(0);

                        //AUTO Play mute is off So no volume will play
                        if (mPreviewProperties.previewVideoConfig.AUTOPLAY_MUTE.equalsIgnoreCase("OFF")) {
                            playerInterface.setPlayerVolume(0);
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        if (playBackTimeOutHandler != null && playBackTimeOutRunnable != null) {
            playBackTimeOutHandler.removeCallbacks(playBackTimeOutRunnable);
            playBackTimeOutHandler.postDelayed(playBackTimeOutRunnable,AUTO_PLAY_VIDEO_TIME_OUT);
        }
        ((Activity)mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        prevPlayPos = playPosition;
    }

    void pausePlayer() {
        if(isContentPlayed) {
            Log.e("MOUTRACKER_AUTOPLAY","STOP MOU Tracker");
            stopMOUTracking();
            isContentPlayed = false;
        }
        hideUIElements();
        try {
            if(player != null){
                player.releasePlayer(MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.STOP);
                player = null;
                if(videoSurfaceView != null){
                    videoSurfaceView.setVisibility(GONE);
                }
                if (didPlayerPlay) {
                    fireCleverTapEvent(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            didPlayerPlay = false;
            removeProgressUpdateHandlerMessages();
        }

    }
    private void fireCleverTapEvent(boolean stateEnded) {
        int contentPosition = -1;
        if(mCardDataList != null) {
            contentPosition = playPosition % mCardDataList.size();
        }
        if(presentCardData != null){

            String contentGenre = "NA";
            if (presentCardData.content != null
                    && presentCardData.content.genre != null
                    && presentCardData.content.genre.size() > 0) {
                contentGenre = presentCardData.content.genre.get(0).name;
            }

            String contentType = "NA";
            if (presentCardData.generalInfo != null)
                contentType = presentCardData.generalInfo.type == null ? "NA" : presentCardData.generalInfo.type;

            StringBuilder language = new StringBuilder();
            if (null != presentCardData.content) {
                if (presentCardData.content.genre != null && presentCardData.content.genre.size() > 0) {
                    List<String> languages = presentCardData.content.language;
                    for (int i = 0; i < languages.size(); i++) {
                        String lang = languages.get(i);
                        language.append(lang.toUpperCase() + " | ");
                    }
                }
            }

            String contentPartnerName = (presentCardData.publishingHouse == null) ? APIConstants.NOT_AVAILABLE : presentCardData.publishingHouse.publishingHouseName;

            int duration = Util.calculateDurationInSeconds(presentCardData.content.duration);

            String source = CleverTap.SOURCE_BANNER;
            String sourceDetails =  (mContext != null && ((MainActivity) mContext).carouselInfoData != null) ? ((MainActivity) mContext).carouselInfoData.title : presentCardData.getTitle();

            if (stateEnded) {
                CleverTap.eventPreviewVideoShown("NA",playerDuration,presentCardData._id,presentCardData.generalInfo.title,"yes",
                        presentCardData,mMenuGroup, ComScoreAnalytics.BANNER,ComScoreAnalytics.VALUE_SOURCE_POTRIRAT_BANNER);
            }else{
                CleverTap.eventPreviewVideoShown("NA",playerDuration,presentCardData._id,presentCardData.generalInfo.title,"no",
                        presentCardData,mMenuGroup, ComScoreAnalytics.BANNER,ComScoreAnalytics.VALUE_SOURCE_POTRIRAT_BANNER);
            }
        }
    }

    public void setCarouselPosition(int position) {
        this.carouselPosition = position;
    }

    private void hideUIElements() {
        if (mCoverImage != null) {
            mCoverImage.setVisibility(VISIBLE);
        }
        if(mMuteButton != null){
            mMuteButton.setVisibility(GONE);
        }
        if(mProgressBar != null){
            mProgressBar.setVisibility(INVISIBLE);
        }
    }

    private void releasePlayer() {
        ((Activity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            if(player != null){
                Log.d("Inline","Player Released");
                player.releasePlayer(MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.STOP);
                player = null;
                if(videoSurfaceView != null){
                    videoSurfaceView.setVisibility(GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        removeProgressUpdateHandlerMessages();
        hideUIElements();
        if(localBroadCastReceiver != null){
            ApplicationController.getLocalBroadcastManager().unregisterReceiver(localBroadCastReceiver);
        }
    }

    private int getTargetPositionFromRecyclerView(){
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

        if (targetPosition < 0 || targetPosition == playPosition) {
            return playPosition;
        }
        return targetPosition;
    }


    private int getVisibleVideoSurfaceHeight(int playPosition) {
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


    private PlayerCallbackInterface mPlayerCallbackInterface = new PlayerCallbackInterface() {

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Format format;
            if (trackSelections != null && trackSelections.get(0) != null) {
                format = trackSelections.get(0).getSelectedFormat();
                MOUTracker.computeAndSaveAverageBitrate(format.bitrate / 1000.0f);
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
                    if(prevPlayPos != playPosition)
                        //pausePlayer();

                        bufferCount++;
                    break;
                case ExoPlayer.STATE_ENDED:
                    //   Log.e("Inline", "onPlayerStateChanged: ENDED ");
                    if (didPlayerPlay) {
                        fireCleverTapEvent(true);
                        didPlayerPlay = false;
                        cycleToNextContent();
                        ((Activity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                    hideUIElements();
                    removeProgressUpdateHandlerMessages();
                    break;
                case ExoPlayer.STATE_IDLE:
                    //   Log.e("Inline", "onPlayerStateChanged: IDLE ");
                    break;
                case ExoPlayer.STATE_READY:
                    // Log.e("Inline", "onPlayerStateChanged: Ready to play");
                    if(prevPlayPos == playPosition){
                        if(player != null)
                            player.resumePlayer();
                    }else{
                        pausePlayer();
                    }
                    if (mCoverImage != null && mCoverImage.getVisibility() == VISIBLE) {
                        Animation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                        fadeOut.setStartOffset(200);
                        fadeOut.setDuration(350);
                        mCoverImage.setAnimation(fadeOut);
                        Log.d("Inline","Cover Image GONE");
                        mCoverImage.setVisibility(GONE);
                        //  Log.e("Image link : ","STATE_READY Square");
                    }
                    if(mPreviewProperties != null && mPreviewProperties.previewVideoConfig != null){
                        if(mPreviewProperties.previewVideoConfig.AUTOPLAY_MUTE.equalsIgnoreCase("ON")){
                            mMuteButton.setVisibility(VISIBLE);
                            if(ApplicationController.shouldMutePreviewContent){
                                mMuteButton.setImageResource(R.drawable.mute_icon);
                            }else{
                                mMuteButton.setImageResource(R.drawable.volume_icon);
                            }
                        }else{
                            mMuteButton.setVisibility(GONE);
                        }
                    }
                    if(mProgressBar != null && player != null){
                        mProgressBar.setVisibility(VISIBLE);
                        mProgressBar.setMax((int)player.getDuration());
                    }


                    playBackTimeOutHandler.removeCallbacks(playBackTimeOutRunnable);
                    didPlayerPlay = true;
                    if (playerInterface != null && playerInterface.getPlayWhenReady()) {
                        updatePreviewPlayCount();
                    }

                    if(!isContentPlayed) {
                        Log.e("MOUTRACKER_AUTOPLAY", "START MOU Tracker");
//                        clevertapVideoStarted();
                        startMOUTracker();

                    }
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
            if(mCoverImage != null){
                Log.d("Inline","Cover Image Visible");
                mCoverImage.setVisibility(VISIBLE);
            }
            ApplicationController.didPlayerEncounterError = true;
        }

        @Override
        public void onPositionDiscontinuity(int i) {

        }
    };

    protected void startMOUTracker() {
        if (!ApplicationController.ENABLE_MOU_TRACKING) {
            return;
        }
        if (mMouTracker == null) {
            mMouTracker = new MOUTracker(null, mContext, presentCardData, presentCardData);
            if (currentPage != null)
                mMouTracker.setSourceTab(currentPage);
            mMouTracker.setSourceCarouselPosition(carouselPosition);

            int contentPosition = -1;
            if(mCardDataList != null) {
                contentPosition = playPosition % mCardDataList.size();
            }

            mMouTracker.setSourceContentPosition(contentPosition);
            mMouTracker.setSource(APIConstants.CAROUSEL_AUTOPLAY);
            mMouTracker.setSourceDetails(APIConstants.SOURCE_DETAIL_AUTOPLAY);
            if (presentCardData != null)
                mMouTracker.setTrailerContentId(presentCardData._id);
            if (presentCardData != null && presentCardData.generalInfo != null)
                mMouTracker.setVODContentType(presentCardData.generalInfo.type);
            mMouTracker.start();
        }
    }

    public void stopMOUTracking() {
        if (mMouTracker != null) {
            mMouTracker.setBufferCount(bufferCount);
            mMouTracker.stoppedAutoPlayAt(true, presentCardData);
            mMouTracker.setSourceTab(currentPage);
            mMouTracker = null;
        }
    }

    private void updatePreviewPlayCount() {
        previewVideoCountMap = PrefUtils.getInstance().getPreviewPlayData();
        if(previewVideoCountMap == null){
            previewVideoCountMap = new ConcurrentHashMap<>();
        }

        if(previewVideoCountMap.containsKey(presentCardData._id)){
            int count = previewVideoCountMap.get(presentCardData._id);
            previewVideoCountMap.put(presentCardData._id,count+1);
        }else{
            previewVideoCountMap.put(presentCardData._id,1);
        }
        PrefUtils.getInstance().savePreviewHashMap(previewVideoCountMap);

    }

    private OnChildAttachStateChangeListener mChildAttachStateChangeListener = new OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {

        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {
            if(mCoverImage != null && mCoverImage.getVisibility() == VISIBLE)
                pausePlayer();
        }
    };

    private int convertDensityStringToInt(String density){

        int densityInt = 0;
        switch (density){
            case MDPI:
                densityInt =  0;
                break;
            case HDPI:
                densityInt =  1;
                break;
            case XHDPI:
                densityInt =  2;
                break;
            case XXHDPI:
                densityInt =  3;
                break;
        }
        return densityInt;
    }


}
