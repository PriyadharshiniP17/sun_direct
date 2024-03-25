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
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
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
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterBigHorizontalCarousel;
import com.myplex.myplex.utils.MOUTracker;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.myplex.model.ApplicationConfig.HDPI;
import static com.myplex.model.ApplicationConfig.MDPI;
import static com.myplex.model.ApplicationConfig.XHDPI;
import static com.myplex.model.ApplicationConfig.XXHDPI;
import static com.myplex.player_config.PlayerConfig.PlayerType.PROGRESSIVE_PLAYER;

public class ListPlayerRecyclerView extends RecyclerView {

    public ListPlayerRecyclerView(@NonNull Context context) {
        super(context);
        this.mContext = context;
        initialize(mContext);
    }

    public ListPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initialize(mContext);
    }

    public ListPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initialize(mContext);

    }

    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private MyplexPlayer player;
    private int playPosition;
    private ImageView mCoverImage;
    private SurfaceView videoSurfaceView;
    private AdapterBigHorizontalCarousel.CarouselDataViewHolder mPlayerItemViewHolder;
    private Context mContext;
    private int POST_AUTO_PLAY_WAIT_TIME = 2500;
    private int PRE_AUTO_PLAY_WAIT_TIME = 2000;
    private int AUTO_PLAY_VIDEO_TIME_OUT = 5000;
    private boolean didPlayerPlay = false;
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
    private int MAX_POSITIONS_TO_SCROLL = Integer.MAX_VALUE;
    private int playerDuration = 0;
    private int prevPlayPos;
    private final int PLAYER_VOLUME_MUTE = 0;
    private int carouselPosition;
    private MOUTracker mMouTracker;
    private int bufferCount = 0;
    private boolean isContentPlayed = false;


    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    private ProgressBar mProgressBar;
    private String contentUrl;

    private MyplexPlayerInterface playerInterface;

    public void setListCardData(List<CardData> mCardDataList) {
        this.mCardDataList = mCardDataList;
    }

    Handler playbackTestHandler = new Handler();
    Handler playBackTimeOutHandler = new Handler();

    Runnable playBackTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if(didPlayerPlay){
                return;
            }
            if(isAttachedToWindow()){
                if (playerInterface != null && !playerInterface.getPlayWhenReady()) {
                    player.pausePlayer();
                }
                cycleToNextContent();
            }
        }
    };


    Runnable playbackRunnable = () -> {
        if(ApplicationController.isMiniPlayerEnabled){
            return;
        }
        if(isActivityPaused){
            return;
        }
        if(isAttachedToWindow()) {
            if (!shouldStartPlayback()) {
                cycleToNextContent();
                return;
            }
            if (isUrlPresent()) {
                preparePlayerWithUrl();
            } else {
                cycleToNextContent();
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
                        pausePlayer();
                        tryPlayingContent();
                        isActivityPaused = false;
                    }else if(intent.getAction().equalsIgnoreCase(APIConstants.MINI_PLAYER_ENABLED_BROADCAST)){
                        pausePlayer();
                       // ApplicationController.isMiniPlayerEnabled = true;
                        cycleToNextContent();
                    }else if(intent.getAction().equalsIgnoreCase(APIConstants.MINI_PLAYER_DISABLED_BROADCAST)){
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

    private void setProgress() {
        if(prevPlayPos != playPosition){
            pausePlayer();
        }
        if(mProgressBar != null && player != null){
            mProgressBar.setProgress((int)player.getCurrentPosition());
            playerDuration = (int)(player.getCurrentPosition()/1000);
        }
    }
    private void initialize(Context mContext) {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoSurfaceDefaultHeight = point.x / 4;
        screenDefaultHeight = point.y;

        addOnScrollListener(mOnScrollListener);
        addOnChildAttachStateChangeListener(mChildAttachStateChangeListener);
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
        if(isContentPlayed) {
            Log.e("MOUTRACKER_AUTOPLAY_VOD","STOP MOU Tracker");
            stopMOUTracking();
            isContentPlayed = false;
        }
        releasePlayer();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initialize(mContext);
        tryPlayingContent();

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
    }


    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                LoggerD.debugLog("Inline "+" Trying to play content");
                hideUIElements();
                pausePlayer();
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
        String thumbnailPreviewPlaybackEnabled = PrefUtils.getInstance().getThumbnailPreviewPlaybackEnabled();

        Log.e("PREVIEW_ENABLE","thumbnailPreviewPlaybackEnabled : "+thumbnailPreviewPlaybackEnabled);
        Log.e("PREVIEW_ENABLE","isLoggedIn : "+ isLoggedIn);

        if (thumbnailPreviewPlaybackEnabled != null && thumbnailPreviewPlaybackEnabled.equalsIgnoreCase("registered")) {
            if(!isLoggedIn)
                return false;
        } else if(thumbnailPreviewPlaybackEnabled != null && thumbnailPreviewPlaybackEnabled.equalsIgnoreCase("off")) {
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
        //Preview Data Null, no point in checking more
        return currentPreviewData != null;
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
                if (sdk_version > 18) {
                    String sdk_versionString = String.valueOf(sdk_version);
                    List<String> list = Arrays.asList(mPreviewProperties.previewVideoConfig.PREVIEW_DISABLE_ANDROID_OS.split(","));
                    if (list != null && list.size() > 0) {
                        if (list.contains(sdk_versionString)) {
                            return false;
                        }
                    }
                    return true;
                }            }
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
        int partitionSize = 3;
        try {
            if (!TextUtils.isEmpty(PrefUtils.getInstance().getMotionVideoPartition())) {
                partitionSize = Integer.parseInt(PrefUtils.getInstance().getMotionVideoPartition());
            }


            List<List<CardData>> partitions = new LinkedList<>();
            if (mCardDataList == null || mCardDataList.size() <= 0) return null;

            for (int i = 0; i < mCardDataList.size(); i += partitionSize) {
                partitions.add(mCardDataList.subList(i,
                        Math.min(i + partitionSize, mCardDataList.size())));
            }


            for (int l = 0; l < partitions.size(); l++) {

                CardData randomCardData = getRandomCardDataFromList(partitions.get(l));
                if (randomCardData != null) {

                    int startPos = getStartPosition();
                    int endPos = getEndPosition();
                    for (int j = startPos; j < endPos; j++) {
                        if (mCardDataList != null && mCardDataList.size() > 0) {
                            CardData visibleCardData = mCardDataList.get(j);
                            if (visibleCardData != null) {

                                if (visibleCardData._id != null && visibleCardData._id.equalsIgnoreCase(randomCardData._id)) {
                                    presentCardData = randomCardData;
                                    mPreviewData = presentCardData.previews;

                                    if (mPreviewData != null && mPreviewData.values != null && mPreviewData.values.size() > 0) {
                                        for (int i = 0; i < mPreviewData.values.size(); i++) {
                                            if ((mPreviewData.values.get(i).layoutType.equalsIgnoreCase("portraitBanner")
                                                    && mPreviewData.values.get(i).previewType.equalsIgnoreCase("PromoPlay")) || (mPreviewData.values.get(i).layoutType.equalsIgnoreCase("regularLandscape")
                                                    && mPreviewData.values.get(i).previewType.equalsIgnoreCase("PromoPlay"))) {
                                                currentPreviewData = mPreviewData.values.get(i);
                                                playPosition = j;
                                                return currentPreviewData;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private CardData getRandomCardDataFromList(List<CardData> listCardData) {
        if (listCardData != null && listCardData.size() != 0) {
            int sze = listCardData.size();

            Random r = new Random();
            int randomNumber = r.nextInt(sze);
            return listCardData.get(randomNumber);
        } else {
            return null;
        }
    }

    private void preparePlayerWithUrl() {
        bufferCount = 0;
        MOUTracker.captureThePlaybackTriggerTime();

        initialize(mContext);
        try {
            mPlayerItemViewHolder  = (AdapterBigHorizontalCarousel.CarouselDataViewHolder) findViewHolderForAdapterPosition(playPosition);

            if (mCoverImage != null) {
                Log.d("Inline","Cover Image Visible");
                mCoverImage.setVisibility(VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            pausePlayer();
            return;
        }

        if(mPlayerItemViewHolder == null) return;
        mCoverImage = mPlayerItemViewHolder.itemView.findViewById(R.id.thumbnail_movie);
        videoSurfaceView = mPlayerItemViewHolder.itemView.findViewById(R.id.sv_Player);

        if(videoSurfaceView == null) {
            pausePlayer();
            return;
        }

        int height = (((ApplicationController.getApplicationConfig().screenWidth * 4) / 3));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) videoSurfaceView.getLayoutParams();
        layoutParams.height = height;
        videoSurfaceView.setLayoutParams(layoutParams);
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
                if (presentCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                        || presentCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
                    playerConfigBuilder.setShouldUseLiveSeekWindow(true);
                }

                playerInterface = new PlayerConfig().getPlayerInstance(PROGRESSIVE_PLAYER);

                player = new MyplexPlayer();
                player.setInterface(playerInterface);
                player.preparePlayer(playerConfigBuilder.build(), MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.PAUSE);

                AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                if (audio != null /*&& ApplicationController.shouldMutePreviewContent*/) {
                    playerInterface.setPlayerVolume(0);

                    //AUTO Play mute is off So no volume will play
                    if (mPreviewProperties.previewVideoConfig.AUTOPLAY_MUTE.equalsIgnoreCase("OFF")) {
                        playerInterface.setPlayerVolume(0);
                    }
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
//        performMute();
    }

    private void performMute(){
        if(player != null){
            ApplicationController.shouldMutePreviewContent = true;
            //player.setVolume(PLAYER_VOLUME_MUTE);
        }
    }

    public void pausePlayer() {
        hideUIElements();
        if(isContentPlayed) {
            Log.e("MOUTRACKER_AUTOPLAY_VOD","STOP MOU Tracker");
            stopMOUTracking();
            isContentPlayed = false;
        }

        try {
            if(player != null){
                Log.e("MOTIONVIDEO_DEBUG","player.release()");
                player.releasePlayer(MainActivity.ANALYTICS_STATE == MainActivity.AnalyticsPlayerState.STOP);
                //player.clearVideoSurface();
                player = null;
                if(videoSurfaceView != null){
                    videoSurfaceView.setVisibility(GONE);
                }
                if (didPlayerPlay) {
                    // fireCleverTapEvent(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            didPlayerPlay = false;
            removeProgressUpdateHandlerMessages();
        }

    }
    /*private void fireCleverTapEvent(boolean stateEnded) {
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

            int duration = com.android.myplex.utils.Util.calculateDurationInSeconds(presentCardData.content.duration);

            String source = CleverTap.SOURCE_BANNER;
            String sourceDetails =  (mContext != null && ((MainActivity) mContext).carouselInfoData != null) ? ((MainActivity) mContext).carouselInfoData.title : presentCardData.getTitle();

            if (stateEnded) {
                CleverTap.eventPreviewVideoShown("NA",playerDuration,presentCardData._id,presentCardData.generalInfo.title,"yes",contentPosition,carouselPosition,contentGenre,contentType,language.toString(),contentPartnerName,currentPage,duration,source,sourceDetails);
            }else{
                CleverTap.eventPreviewVideoShown("NA",playerDuration,presentCardData._id,presentCardData.generalInfo.title,"no",contentPosition,carouselPosition,contentGenre,contentType,language.toString(),contentPartnerName,currentPage,duration,source,sourceDetails);
            }
        }
    }
*/
    public void setCarouselPosition(int position) {
        this.carouselPosition = position;
    }

    private void hideUIElements() {
        if (mCoverImage != null) {
            mCoverImage.setVisibility(VISIBLE);
        }
        if(mProgressBar != null){
            mProgressBar.setVisibility(INVISIBLE);
        }
    }

    public void releasePlayer() {
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

    private int getStartPosition(){
        if(getLayoutManager() != null) {
            return ((LinearLayoutManager) getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        } else {
            return 0;
        }
    }

    private int getEndPosition(){
        if(getLayoutManager() != null) {
            return ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
        } else {
            return 0;
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
                        pausePlayer();

                    bufferCount++;
                    break;
                case ExoPlayer.STATE_ENDED:
                    //   Log.e("Inline", "onPlayerStateChanged: ENDED ");
                    if (didPlayerPlay) {
                        //fireCleverTapEvent(true);
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
                    if(player == null){
                        return;
                    }
                    if(prevPlayPos == playPosition){
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
                    if(mProgressBar != null){
                        mProgressBar.setVisibility(VISIBLE);
                        mProgressBar.setMax((int)player.getDuration());
                    }
                    playBackTimeOutHandler.removeCallbacks(playBackTimeOutRunnable);
                    if(!isContentPlayed) {
                        Log.e("MOUTRACKER_AUTOPLAY_VOD", "START MOU Tracker");
                        startMOUTracker();
                    }
                    isContentPlayed = true;

                    didPlayerPlay = true;
                    break;
                default:
                    cycleToNextContent();
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
            pausePlayer();
            //tryPlayingContent();
            Log.e("MOTIONVIDEO_DEBUG","error " + error);
            if(mCoverImage != null){
                Log.d("Inline","Cover Image Visible");
                mCoverImage.setVisibility(VISIBLE);
            }
          //  ApplicationController.didPlayerEncounterError = true;
        }

        @Override
        public void onPositionDiscontinuity(int i) {

        }
    };

    public void stopMOUTracking() {
        if (mMouTracker != null) {
            mMouTracker.setBufferCount(bufferCount);
            mMouTracker.stoppedAutoPlayAt(true, presentCardData);
            mMouTracker.setSourceTab(currentPage);
            mMouTracker = null;
        }
    }

    protected void startMOUTracker() {
        if (!ApplicationController.ENABLE_MOU_TRACKING) {
            return;
        }
        if (mMouTracker == null && presentCardData != null) {
            mMouTracker = new MOUTracker(null, mContext, presentCardData, presentCardData);
            if (currentPage != null)
                mMouTracker.setSourceTab(currentPage);
            mMouTracker.setSourceCarouselPosition(carouselPosition);
            mMouTracker.setSourceContentPosition(0);
            mMouTracker.setSource(APIConstants.CAROUSEL_AUTOPLAY);
            mMouTracker.setSourceDetails(APIConstants.SOURCE_DETAIL_AUTOPLAY);
            mMouTracker.setTrailerContentId(presentCardData._id);
            if (presentCardData.generalInfo != null)
                mMouTracker.setVODContentType(presentCardData.generalInfo.type);
            mMouTracker.start();
        }
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