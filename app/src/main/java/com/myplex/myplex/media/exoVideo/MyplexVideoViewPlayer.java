package com.myplex.myplex.media.exoVideo;

import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.model.CardData;
import com.myplex.model.CardDataSubtitles;
import com.myplex.model.DownloadMediadata;
import com.myplex.model.PlayerStatusUpdate;
import com.myplex.myplex.media.MediaController2;
import com.myplex.myplex.media.PlayerGestureListener;
import com.myplex.myplex.media.PlayerListener;
import com.myplex.myplex.media.VideoViewPlayer;

import java.util.ArrayList;
import java.util.List;


public interface MyplexVideoViewPlayer {

    void setCardData(CardData mCardData);

    boolean isBitrateCappingSupported();

    void setSelectedTrack(TrackData trackData);

    void setSelectedTrack(TrackData trackData, int selectedTrack);

    int getTrackCount(int trackType);

    List<TrackData> getBitrateCappingTracks();

    View getMediaControllerView();

    void setLive(boolean b);

    void setADTag(String adTag);

    void isPreviewInitialWorkDone(boolean b);

    boolean isLive();

    void seekTo(int mNewPosition);

    MediaController2 getMediaControllerInstance();

    int getCurrentState();

    boolean isMinimized();

    int getWidth();

    int getHeight();

    void setGestureListener(PlayerGestureListener.GestureListener mGestureListener);

    void onChangeQuality(float minHDRate, float maxHDRate, int videoTrackRenderer);

    void setPlayerTitleHeaderView(View view);

    void isToolbarShown(boolean isToShowToolBar);

    void isPlayingAd(boolean b);

    void setDownloadMediaData(DownloadMediadata downloadMediaData);

    void setPlayerGestureControllsView(View mMediaControllsLayoutContainer);
    void setPlayerMediaControllsView(View mPlayerMediaControllsLayoutContainer);

    CardData getCardData();

    enum StreamType {
        LIVE, VOD
    }

    enum StreamProtocol {
        RTSP, HTTP_PROGRESSIVEPLAY, HLS,DASH
    }

    boolean onTouchEvent(MotionEvent event);

    void playerInFullScreen(boolean b);

    void hideMediaController();

    void setUri(Uri uri, VideoViewPlayer.StreamType type);

    void setPositionWhenPaused(int pos);

    void closeSession();

    void deregisteronBufferingUpdate();

    void showMediaController();

    boolean isPlaying();

    int getCurrentPosition();

    void setPlayerListener(PlayerListener mPlayerListener);

    void resizeVideo(int width, int height);

    void setParams(RelativeLayout.LayoutParams params);

    void setStreamName(String serName);

    void setPlayerStatusUpdateListener(PlayerStatusUpdate mPlayerStatusUpdate);

    void onPause();

    void onResume();

    boolean isMediaControllerShowing();

    void setOnLicenseExpiryListener(VideoViewPlayer.OnLicenseExpiry onLicenseExpiry);

    boolean wasPlayingWhenPaused();

    void setFullScreenTooggle(int visibilty);

    int getCachedDuration();

    void setMinized(boolean minimized);

    void setAdContainer(FrameLayout frameLayout);

    void setDebugTxtView(TextView textView);

    void addPlayerEvent(PlayerEventListenerInterface playerEventListenerInterface);

    void setStreamProtocol(StreamProtocol streamProtocol);

    void setStreamType(VideoViewPlayer.StreamType streamType);

    void allowMediaController(boolean enable);

    void setFullScreen(boolean isFullScreen);

    long getConsumedData();

    /*For Hooq player callbacks
        * ================*/
    void play();

    View getView();

    boolean isPlaybackInitialized();

    boolean isPlayerPaused();

    int getPositionWhenPaused();

/*======================*/


    // exo player 2.2.0
    int getRendererType(int pos);

    void setSubtitle(String name);

    CardDataSubtitles getSubtitles();

    boolean isSubtitlesSupported();

    void showSubtitlesView();

    void hideSubtitlesView();

    String getSubtitleName();

    void onTapToZoom();

    void setmParentLayout(View mParentLayout);

    void orientationChange(int orientation);

    void setAudioTrack(String language);

    String getContentMinBitrate();
    String getContentMaxBitrate();
    String getContentResolution();
    String getContentLanguage();

    default List<String> getAudioTracks(){
        return new ArrayList<>();
    }

    default void onDestroy(){

    }

    default void setWaterMarkImageView(ImageView sunnxtContentPlayerWatermark){

    }

    default void setShowWaterMark(boolean showWaterMark){

    }


}
