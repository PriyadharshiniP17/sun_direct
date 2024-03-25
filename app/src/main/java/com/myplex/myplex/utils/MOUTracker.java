package com.myplex.myplex.utils;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.myplex.analytics.AnalyticsConstants;
import com.myplex.analytics.Event;
import com.myplex.analytics.MyplexAnalytics;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.MOUUpdateRequest;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.media.exoVideo.MyplexVideoViewPlayer;
import com.myplex.myplex.ui.views.MiniCardVideoPlayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import static com.myplex.myplex.ApplicationController.getApplicationConfig;
//import static com.myplex.myplex.ApplicationController.getMOUNotFiredData;

/**
 * Created by Apalya on 9/18/2014.
 */
public class MOUTracker {

    private final static String TAG = "MOUTracker";
    private static String STREAM_TYPE = "stream";

    public static boolean prepareContentToPlay = false;
    public static boolean startedContentToPlay = false;
    public static boolean firstFrameRendered = false;
    public static int bufferTimeOnFirstFrameRender;

    private boolean isPlaying = false;
    private boolean isStarted = false;

    private boolean playerClosed = false;

    private long currentTime = 0;
    private long pausedAt = 0;
    private long playedTime = 0;
    private long totalPlayedTime = 0;
    private final MyplexVideoViewPlayer mPlayer;

    private static final int POLL_INTERVAL = 3 * 1000;

    private Thread monitoringThread = null;
    private final Context mContext;
    private static CardData mCardData;
    private static float weightedAverageBitrate = 1;
    private static float averageBitrate = 1;
    private static long prevTimeWhereAverageBitRateCapturedMS,prevTimeWhereAverageConnectionSpeedCapturedMS;
    private static long prevTimeWhereAverageBitRateCapturedSec = 1,prevTimeWhereAverageConnectionSpeedCapturedSec =1;

    private MOUUpdateRequest mouUpdateRequest;
    private String mContentType;
    private boolean isPlayBackStartedAlready;
    private String mNotificationTitle;
    private String mNid;
    private String mProfileSelect;
    private String mTrailerContentId;
    private MiniCardVideoPlayer mPlayerWithYoutubePlayer;
    private int mSavedContentPosition;
    private String mSource;
    private String mSourceDetails;
    private CardData mTVShowCardData;
    private String mSourceTab;
    private static DefaultBandwidthMeter sBandwidthMeter;
    private static long effectiveBitrate;
    private static float weightedAverageConnectionSpeed,averageConnectionSpeed ;
    private static long playbackTriggerTime;
    private static long playbackStartUpTime;
    private int mBufferCount = 0;

    public static int bufferTimeinsec;
    public static int playCount;
    public static int pauseCount;

    public static int forwardCount;
    public static int forwardTime;

    public static int backwardCount;
    public static int backwardTime;
    private String previewContentType = null;

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

    public void setBufferCount(int bufferCount){
        this.mBufferCount = bufferCount;
    }

    public static void setBandWidthMeter(DefaultBandwidthMeter bandWidthMeter) {
        sBandwidthMeter = bandWidthMeter;
    }

    public int getSourceCarouselPosition() {
        return sourceCarouselPosition;
    }

    public void setSourceCarouselPosition(int sourceCarouselPosition) {
        this.sourceCarouselPosition = sourceCarouselPosition;
    }

    private int sourceCarouselPosition = -1;

    public int getSourceContentPosition() {
        return sourceContentPosition;
    }

    public void setSourceContentPosition(int sourceContentPosition) {
        this.sourceContentPosition = sourceContentPosition;
    }

    private int sourceContentPosition = -1;



    public void setLocalPlayBack(boolean localPlayBack) {
        isLocalPlayBack = localPlayBack;
    }

    private boolean isLocalPlayBack;

    public void setIsTrailerPlayed(boolean mIsTrailerPlayed) {
        this.mIsTrailerPlayed = mIsTrailerPlayed;
    }

    public void setTrailerContentId(String mTrailerContentId) {
        this.mTrailerContentId = mTrailerContentId;
    }

    private boolean mIsTrailerPlayed;

    public MOUTracker(MyplexVideoViewPlayer mPlayer, Context context, CardData mCardData, CardData mTVShowCardData) {
        this.mPlayer = mPlayer;
        this.mContext = context;
        MOUTracker.mCardData = mCardData;
        this.mTVShowCardData = mTVShowCardData;
    }

    public void start() {
        if (monitoringThread == null) {
            monitoringThread = new Thread(monitoringRunnaable);
            startVideoTime();
            monitoringThread.start();
        }
    }


    private Runnable monitoringRunnaable = new Runnable() {
        @Override
        public void run() {
            boolean currentPlayingState;

            try {
                while (!playerClosed) {
                    //Log.d(TAG, "MOU monitoring thread is running");
                    currentPlayingState = isPlaying();

                    if (!isStarted && currentPlayingState) {
                        // player started
                        startVideoTime();
                        isStarted = true;
                        isPlaying = true;
                        //Log.d(TAG, "player started");
                    }

                    if (isPlaying && !currentPlayingState) {
                        // player stopped
                        pausedAt();
                        Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.pause.name(), mCardData,
                                getTotalPlayedTimeInMinutes(), getTotalPlayedTimeInSeconds());
                        isPlaying = false;
                        //Log.d(TAG, "player stopped getTotalPlayedTimeInMinutes: " + getTotalPlayedTimeInMinutes() + " isPlaying: " + isPlaying + " currentPlayingState: " + currentPlayingState);
                    }

                    if (!isPlaying && currentPlayingState) {
                        // player resumed
                        resumedAt();
                        isPlaying = true;
                        //Log.d(TAG, "player resumed");
                    }

                    Thread.currentThread().sleep(POLL_INTERVAL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private long getPlayedPercentage() {
        if (mPlayerWithYoutubePlayer != null) {
            return getPercentageFromYoutubePlayer();
        }
        if (mPlayer == null) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getCachedDuration();
        long percent = 0;
        if (duration > 0) {
            // use long to avoid overflow
            percent = 100L * position / duration;
        }
        return percent;
    }

    private long getPercentageFromYoutubePlayer() {
        if (mPlayerWithYoutubePlayer == null) {
            return 0;
        }
        int position = mPlayerWithYoutubePlayer.getYoutubePlayerPosition();
        int duration = mPlayerWithYoutubePlayer.getYoutubePlayerDuration();
        long percent = 0;
        if (duration > 0) {
            // use long to avoid overflow
            percent = 100L * position / duration;
        }
        return percent;
    }

    private boolean isPlaying() {
        return (mPlayer != null && mPlayer.isPlaying()) || (mPlayerWithYoutubePlayer != null && mPlayerWithYoutubePlayer.isYouTubePlayerPlaying());
    }


    //// from begin 2 code flows can happen 1) pause  2) stop
    public void startVideoTime() {
        totalPlayedTime = 0;
        playedTime = 0;
        pausedAt = 0;
        currentTime = System.currentTimeMillis(); //10.00
        isPlaying = true;
        //Log.d(TAG, "checktime startVideoTime  " + new Date(currentTime));
    }

    // from pause 2 code flows can happen 1) resume  2) stop
    public void pausedAt() {
        pausedAt = System.currentTimeMillis(); //10.10 //10.35 //stop at 10.45
        playedTime = TimeUnit.MILLISECONDS.toSeconds(pausedAt) - TimeUnit.MILLISECONDS.toSeconds(currentTime);
        isPlaying = false;
        //Log.d(TAG, "checktime pausedAt  " + new Date(pausedAt));
        //Log.d(TAG, "checktime currentTime  " + currentTime + "  paused time " + pausedAt);
        //Log.d(TAG, "checktime pausedAt  playedTime in seconds " + playedTime);
        totalPlayedTime();
    }

    // from resumed 2 code flows can happen 1) pause  2) stop
    public void resumedAt() {
        currentTime = System.currentTimeMillis(); //10.20
        isPlaying = true;
        //Log.d(TAG, "checktime resumedAt  currentTime " + new Date(currentTime));
    }

    public void stoppedAt() {
        playerClosed = true;
        long ptimeInMinutes = getTotalPlayedTimeInMinutes();
        long ptimeInSec = getTotalPlayedTimeInSeconds() % 60;
        //Log.d(TAG, "before paused/totalPlayedTime calc stoppedAt ptimeInMinutes: " + ptimeInMinutes + " ptimeInSec: " + ptimeInSec);
        if (isPlaying) {
            pausedAt();
            getTotalPlayedTime();
        } else {
            //left intentionally to be filled
            getTotalPlayedTime();
        }
        ptimeInMinutes = getTotalPlayedTimeInMinutes();
        ptimeInSec = getTotalPlayedTimeInSeconds();
        long durationInSeconds = Util.calculateDurationInSeconds(String.valueOf(mPlayer.getCachedDuration()));
        //Log.d(TAG, "after paused/totalPlayedTime calc stoppedAt ptimeInMinutes: " + ptimeInMinutes + " ptimeInSec: " + ptimeInSec);
        if (mCardData != null
                && mCardData.generalInfo != null
                && mCardData.generalInfo.type != null) {
            if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mCardData.generalInfo.type)) {
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_MOVIE_STREAMED_FOR_MIN, ptimeInSec);
                FirebaseAnalytics.getInstance().eventContentPlayed(mCardData,durationInSeconds,ptimeInSec,mSource,mSourceDetails);
            } else if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mCardData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)) {
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_TV_STREAMED_FOR_MIN, ptimeInMinutes);
                FirebaseAnalytics.getInstance().eventContentPlayed(mCardData,durationInSeconds,ptimeInSec,mSource,mSourceDetails);
            }
        }
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_TIME_PLAYED_IN_SEC, ptimeInSec);
        if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mContentType)
                || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(mContentType)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(mContentType)) {
            Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.play.name(), mContentType, mCardData, ptimeInMinutes, ptimeInSec);
            FirebaseAnalytics.getInstance().eventContentPlayed(mCardData,durationInSeconds,ptimeInSec,mSource,mSourceDetails);

        } else {
            Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.play.name(), mCardData, ptimeInMinutes, ptimeInSec);
        }
        String subtitle = mPlayer.getSubtitleName();
        subtitle = TextUtils.isEmpty(subtitle) ? mContext.getString(R.string.subtitle_opt_none) : subtitle;
        CleverTap.eventVideoStopped(mTVShowCardData, mCardData, getTotalPlayedTimeInSeconds(), getPlayedPercentage(), isLocalPlayBack, mSource, mSourceDetails, subtitle,mSourceTab,sourceCarouselPosition);
        if (mSavedContentPosition > 0) {
            CleverTap.eventVideoPlayed(mTVShowCardData, mCardData, getTotalPlayedTimeInSeconds(), getPlayedPercentage(), mSavedContentPosition, isLocalPlayBack, mSource, mSourceDetails,mSourceTab,sourceCarouselPosition);
        }
        int secs = 30;
        try {
            String prefSecs = PrefUtils.getInstance().getPrefAppsflyerPlaybackEventSeconds();
            if (!TextUtils.isEmpty(prefSecs)) {
                secs = Integer.parseInt(prefSecs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getTotalPlayedTimeInSeconds() >= secs && !PrefUtils.getInstance().getAppsFlyerPlayedEventFor30SecFired()) {
            PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(true);
            AppsFlyerTracker.eventPlayedVideoFor30Sec(new HashMap<String, Object>());
        }

        try {
            String prefSecs = PrefUtils.getInstance().getPrefBranchIOPlaybackEventSeconds();
            if (!TextUtils.isEmpty(prefSecs)) {
                secs = Integer.parseInt(prefSecs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getTotalPlayedTimeInSeconds() >= secs && !PrefUtils.getInstance().getBranchIOPlayedEventFor30SecFired()) {
            PrefUtils.getInstance().setBranchIOPlayedEventFor30SecFired(true);
            MyplexAnalytics.getInstance().event(new Event("played video for " + secs + " sec", new HashMap<String, Object>(), AnalyticsConstants.EventPriority.HIGH));
        }
        if (!isPlayBackStartedAlready) {
            isPlayBackStartedAlready = true;
            Analytics.gaNotificationPlayBackSuccess(mNotificationTitle, mNid, mCardData, mProfileSelect);
        }
            /*String record = PrefUtils.getInstance().getString(mContext.getString(R.string
                    .last_played_util_mou));
            record = getRecordInLine(record, ptimeInMinutes, ptimeInSec);
            SharedPrefUtils.writeToSharedPref(mContext, mContext.getString(R.string.last_played_util_mou), record);*/
        makeMouUpdateRequest();
        storeDataForAnalyticsEventForLastPlayedContent();
    }

    private void storeDataForAnalyticsEventForLastPlayedContent(){
        if (mCardData != null && mCardData.generalInfo != null && mCardData.generalInfo.title != null)
            PrefUtils.getInstance().setLastContentPlayed(mCardData.generalInfo.title);
        if (mCardData != null && mCardData._id != null)
            PrefUtils.getInstance().setLastContentIDPlayed( mCardData._id);
        if(mSource != null && !mSource.isEmpty()){
            PrefUtils.getInstance().setLastContentSourcePlayed( mSource);
        }else{
            PrefUtils.getInstance().setLastContentSourcePlayed( APIConstants.NOT_AVAILABLE);
        }
        if(mSourceDetails != null && !mSourceDetails.isEmpty()){
            PrefUtils.getInstance().setLastContentSourceDetailsPlayed(mSourceDetails);
        }else{
            PrefUtils.getInstance().setLastContentSourceDetailsPlayed(APIConstants.NOT_AVAILABLE);
        }

        PrefUtils.getInstance().setLastPlayedContentMOU(getTotalPlayedTimeInMinutes());
        long totalMOU = PrefUtils.getInstance().getTotalMOU();
        totalMOU = totalMOU + getTotalPlayedTimeInMinutes();
        PrefUtils.getInstance().setTotalMOU(totalMOU);
    }
    MOUUpdateRequestStorage mouUpdateRequestStorage;

    private void makeMouUpdateRequest() {
        String contentId = null;
        String trackingId = null;
        if (mCardData != null
                && mCardData.generalInfo != null
                && mCardData.generalInfo.type != null) {
            if (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                    && mCardData.globalServiceId != null) {
                contentId = mCardData.globalServiceId;
            } else if (mCardData._id != null) {
                contentId = mCardData._id;
            }
        } else if (mCardData._id != null) {
            contentId = mCardData._id;
        }
        if(mCardData != null && !TextUtils.isEmpty(mCardData.trackingID)){
            trackingId = mCardData.trackingID;
        }
        if (mIsTrailerPlayed) {
            contentId = mTrailerContentId;
        }

        if(mPlayer != null)
        //LoggerD.debugLog("makeMouUpdateRequest mPlayer.getConsumedData()- " + mPlayer.getConsumedData());
        //For Offline
        mouUpdateRequestStorage = new MOUUpdateRequestStorage(
                getTotalPlayedTimeInSeconds(),
                currentTime, SDKUtils.getInternetConnectivity(mContext),
                STREAM_TYPE, mNid, mPlayer != null ? mPlayer.getConsumedData() : 0,0,contentId,
                trackingId, averageBitrate,effectiveBitrate,weightedAverageBitrate,weightedAverageConnectionSpeed,playbackStartUpTime,mBufferCount
                ,sourceCarouselPosition,mSource,mSourceDetails,mSourceTab);

        //For Immediate Request
        MOUUpdateRequest.Params mouParams = new MOUUpdateRequest.Params(contentId,
                getTotalPlayedTimeInSeconds(), currentTime, SDKUtils.getInternetConnectivity(mContext),
                STREAM_TYPE, mNid, mPlayer != null ? mPlayer.getConsumedData() : 0,trackingId, averageBitrate,weightedAverageBitrate,weightedAverageConnectionSpeed,effectiveBitrate,playbackStartUpTime,mBufferCount
                    ,sourceCarouselPosition,mSource,mSourceDetails,mSourceTab);
        mouUpdateRequest = new MOUUpdateRequest(mouParams, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response.body() != null
                        && response.body().message != null) {
                    Log.e("TRACKER1",
                            "successfull upadted in server with response"
                                    + response.body().message);
                    if (response.body().code == 201  ){
                        Log.e("TRACKER1",
                                "successfull upadted in server with code"
                                        + response.body().code);
                        }else{
                        Log.e("TRACKER1",
                                "successfull upadted in server with code"
                                        + response.body().code);
                        //storeOfflineMOU();

                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                Log.e("TRACKER1", "Failed to update in server");
                //storeOfflineMOU();
            }
        });
        APIService.getInstance().execute(mouUpdateRequest);
        initializeVariablesForAverageBitrate();
    }

  /*  public void storeOfflineMOU(){
        MOUUpdateRequestStorageList downloadedDataList = getMOUNotFiredData();
        if (downloadedDataList != null && downloadedDataList.mDownloadedList != null ) {
            Log.e("TRACKER1", "downloadedDataList.mDownloadedList.size()"+downloadedDataList.mDownloadedList.size());
                downloadedDataList.mDownloadedList.add(mouUpdateRequestStorage);
                ApplicationController.setOfflineMOUData(downloadedDataList);
                SDKUtils.saveObject(downloadedDataList, getApplicationConfig().offlineMOUPath);
                Log.e("TRACKER1",downloadedDataList.toString());

        }
    }*/

    public static  void makeMouUpdateRequestForOffline(final MOUUpdateRequestStorage mouUpdateRequestStorage) {
        if (mouUpdateRequestStorage!=null){
            MOUUpdateRequest.Params mouParams = new MOUUpdateRequest.Params(mouUpdateRequestStorage.getContentId(),
                    mouUpdateRequestStorage.getElapsedTime(), mouUpdateRequestStorage.getTimeStamp(),
                    mouUpdateRequestStorage.getInternetConnectivity(), mouUpdateRequestStorage.getConsumptionType(),
                    mouUpdateRequestStorage.getNid(), mouUpdateRequestStorage.getBytes(),mouUpdateRequestStorage.trackingId,
                    mouUpdateRequestStorage.getAverageBitrate(), mouUpdateRequestStorage.getWeightedAverageBitrate(),
                    mouUpdateRequestStorage.getWeightedConnectionSpeed(),mouUpdateRequestStorage.getBandWidthOfDevice(),
                    mouUpdateRequestStorage.getPlaybackStartUpTime(),mouUpdateRequestStorage.getBufferCount(),
                    mouUpdateRequestStorage.getSourceCarouselPosition(),mouUpdateRequestStorage.getmSource(),
                    mouUpdateRequestStorage.getmSourceDetails(),mouUpdateRequestStorage.getmSourceTab());
            MOUUpdateRequest mouUpdateRequest = new MOUUpdateRequest(mouParams, new APICallback<BaseResponseData>() {
                @Override
                public void onResponse(APIResponse<BaseResponseData> response) {
                    if (response.body() != null
                            && response.body().message != null) {
                        Log.e("TRACKER1",
                                "successfull upadted in server with response"
                                        + response.body().message);
                        deleteUpdatedMOUs(mouUpdateRequestStorage);

                    }
                }

                @Override
                public void onFailure(Throwable t, int errorCode) {
                    Log.e("TRACKER1", "Failed to update in server offline MOUs");
                }
            });
            APIService.getInstance().execute(mouUpdateRequest);
        }
    }

    public static void deleteUpdatedMOUs(MOUUpdateRequestStorage mouUpdateRequestStorage){
        try {

            Log.e("TRACKER1","getApplicationConfig().offlineMOUPath- " + ApplicationController.getApplicationConfig().offlineMOUPath);
            if (!DownloadUtil.isFileExists(ApplicationController.getApplicationConfig().offlineMOUPath)) {
                Log.e("TRACKER1","getApplicationConfig().offlineMOUPath- "  + ApplicationController.getApplicationConfig().offlineMOUPath + " file does not exists");
            }  /*{
                MOUUpdateRequestStorageList sMOUNotFiredList = ApplicationController.getMOUNotFiredData();
                if(sMOUNotFiredList.mDownloadedList.contains(mouUpdateRequestStorage)){
                    sMOUNotFiredList.mDownloadedList.remove(mouUpdateRequestStorage);
                 //   ApplicationController.setOfflineMOUData(sMOUNotFiredList);
                    SDKUtils.saveObject( sMOUNotFiredList, ApplicationController.getApplicationConfig().offlineMOUPath);
                    Log.e("TRACKER1"," After Deletion -- "+sMOUNotFiredList.toString());
                    if (sMOUNotFiredList != null && sMOUNotFiredList.mDownloadedList != null && sMOUNotFiredList.mDownloadedList.size()>0 ){
                        makeMouUpdateRequestForOffline(sMOUNotFiredList.mDownloadedList.get(0));
                    }else{
                        Log.e("TRACKER1"," sMOUNotFiredList empty ");
                    }
                };
            }*/
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Log.e("TRACKER1"," Exception in Deletion in offline MOUs ");
            LoggerD.debugDownload("Exception- " + e.getMessage());
        }

    }

    //invoked only when paused or stopped
    private void totalPlayedTime() {
        //playedTime = pausedAt - currentTime; //.20
        totalPlayedTime = totalPlayedTime + playedTime;
        playedTime = 0;
        isPlaying = false;
        //Log.d(TAG, "checktime totalPlayedTime() totalPlayedTime in seconds " + totalPlayedTime);
        //Log.d(TAG, "checktime totalPlayedTime() totalPlayedTime in minutes " + TimeUnit.SECONDS.toMinutes(totalPlayedTime));

    }

    //returns in minutes
    public long getTotalPlayedTime() {
        //Log.d(TAG, "checktime Player closed total time is in minutes" + TimeUnit.SECONDS.toMinutes(totalPlayedTime));
        return totalPlayedTime + 1;
    }

    public long getTotalPlayedTimeInMinutes() {
        //Log.d(TAG, "checktime Player closed total time is in minutes" + TimeUnit.SECONDS.toMinutes(getTotalPlayedTime()));
        return TimeUnit.SECONDS.toMinutes(getTotalPlayedTime());
    }

    public long getTotalPlayedTimeInSeconds() {
        //Log.d(TAG, "checktime Player closed total time is in minutes " + TimeUnit.SECONDS.toMinutes(getTotalPlayedTime()) + " totalSeconds: " + getTotalPlayedTime());
        return getTotalPlayedTime();
    }

    public static String getRecordInLine(String record, long minutes, long seconds) {
        if (record == null) record = "";
        if (mCardData == null) return record;
        StringTokenizer recordTokens = new StringTokenizer(record, ",");
        List<String> recordsList = new ArrayList<>();
        while (recordTokens.hasMoreTokens()) {
            recordsList.add(recordTokens.nextToken());
        }

        for (int i = 0; recordsList.size() > 4; i++) {
            recordsList.remove(i);
        }
        record = "";
        for (String eachRecord : recordsList) {
            if (!record.equalsIgnoreCase("")) {
                record = record + "," + eachRecord;
            } else {
                record = eachRecord;
            }

        }
        String title = mCardData.generalInfo.title;
        if (title.length() > 9) {
            title = title.substring(0, 9);
        }
        if (!record.equalsIgnoreCase("")) {
            record = record + ",\n" + mCardData._id + "- " + title + ": " + minutes +
                    "m: " + seconds + "s";
        } else {
            record = mCardData._id + "- " + title + ": " + minutes +
                    "m: " + seconds + "s";
        }
        Log.d("Analytics", "mou : " + record);
        return record;
    }

    public void setVODContentType(String contentType) {
        this.mContentType = contentType;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.mNotificationTitle = notificationTitle;
    }

    public void setNId(String nid) {
        this.mNid = nid;
    }

    public void setPlayedProfile(String profile) {
        this.mProfileSelect = profile;
    }

    public void setPlayer(MiniCardVideoPlayer miniCardVideoPlayer) {
        this.mPlayerWithYoutubePlayer = miniCardVideoPlayer;
    }

    public void setLastWatchedPosition(int mSavedContentPosition) {
        this.mSavedContentPosition = mSavedContentPosition;
    }

    public void setSourceTab(String mSourceTab) {
        this.mSourceTab = mSourceTab;
    }

    public String getSourceTab(){
        return this.mSourceTab;
    }

    /*
        These methods are used for sending additional properties to MOU API
     */

    //Will save the current time when the player was given url


    public static void initializeVariablesForAverageBitrate(){
        prevTimeWhereAverageBitRateCapturedSec = 1;
        SDKLogger.debug("Bit rate reset");
        weightedAverageBitrate = 1;
        averageBitrate =1;
        prevTimeWhereAverageBitRateCapturedMS = 1;
        prevTimeWhereAverageConnectionSpeedCapturedMS =1;
        prevTimeWhereAverageConnectionSpeedCapturedSec =1;

    }

    public static void computeAndSaveAverageBitrate(float bitrate){
        SDKLogger.debug("Bit Rate "+bitrate);
        long time  = System.currentTimeMillis();
        if (prevTimeWhereAverageBitRateCapturedMS > 1) {
            float timeElapsed = TimeUnit.MILLISECONDS.toSeconds(time)
                    - TimeUnit.MILLISECONDS.toSeconds(prevTimeWhereAverageBitRateCapturedMS);
            weightedAverageBitrate = ((weightedAverageBitrate *prevTimeWhereAverageBitRateCapturedSec) + (bitrate*timeElapsed))/ (prevTimeWhereAverageBitRateCapturedSec+timeElapsed);
            averageBitrate =( averageBitrate + bitrate)/2;
            SDKLogger.debug("Bit Rate updated"+ weightedAverageBitrate);
            SDKLogger.debug("averageBitrate updated"+ averageBitrate);
            prevTimeWhereAverageBitRateCapturedMS = time;
            prevTimeWhereAverageBitRateCapturedSec = prevTimeWhereAverageBitRateCapturedSec +(long) timeElapsed;
        }else{
            weightedAverageBitrate = bitrate;
            averageBitrate = bitrate;
            SDKLogger.debug("Bit Rate out side"+ weightedAverageBitrate);
            SDKLogger.debug("averageBitrate updated"+ averageBitrate);
            prevTimeWhereAverageBitRateCapturedMS = time;
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                saveBandWidthEstimateIfAvailable();
            }
        }, 5000);
       ;
    }
    //Using exoPlayer's Bandwidth meter to get an estimate
    private static void saveBandWidthEstimateIfAvailable() {
    try {

        if (sBandwidthMeter != null) {
            long bitrateEstimate = sBandwidthMeter.getBitrateEstimate();
            /*effectiveBitrate = bitrateEstimate == BandwidthMeter.NO_ESTIMATE
                    ? 0 : (long) (bitrateEstimate * 0.75);
            SDKLogger.debug("effectiveBitrate" + effectiveBitrate);*/
            long time = System.currentTimeMillis();
            if (prevTimeWhereAverageConnectionSpeedCapturedMS > 1) {
                float timeElapsed = TimeUnit.MILLISECONDS.toSeconds(time)
                        - TimeUnit.MILLISECONDS.toSeconds(prevTimeWhereAverageConnectionSpeedCapturedMS);
                weightedAverageConnectionSpeed = ((weightedAverageConnectionSpeed * prevTimeWhereAverageConnectionSpeedCapturedSec) + (bitrateEstimate * timeElapsed)) / (prevTimeWhereAverageConnectionSpeedCapturedSec + timeElapsed);
                averageConnectionSpeed = (averageConnectionSpeed + bitrateEstimate) / 2;
                SDKLogger.debug("weightedAverageConnectionSpeed updated" + weightedAverageConnectionSpeed);
                SDKLogger.debug("averageConnectionSpeed updated" + averageConnectionSpeed);
                prevTimeWhereAverageConnectionSpeedCapturedMS = time;
                prevTimeWhereAverageConnectionSpeedCapturedSec = prevTimeWhereAverageConnectionSpeedCapturedMS + (long) timeElapsed;
            } else {
                weightedAverageConnectionSpeed = bitrateEstimate;
                averageConnectionSpeed = bitrateEstimate;
                SDKLogger.debug("weightedAverageConnectionSpeed out side" + weightedAverageConnectionSpeed);
                SDKLogger.debug("averageConnectionSpeed updated" + averageConnectionSpeed);
                prevTimeWhereAverageConnectionSpeedCapturedMS = time;

            }
        }
    }catch (Exception e){
        e.printStackTrace();
    }
    }

    public static void captureThePlaybackTriggerTime(){
        playbackTriggerTime = System.currentTimeMillis();
    }
    public static void onFirstFrameRenderedByPlayer(){
        playbackStartUpTime  = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toSeconds(playbackTriggerTime);
    }

    public void stoppedAutoPlayAt(boolean isPreviewContent) {
        if(!isPreviewContent) {
            previewContentType = null;
        }else {
            previewContentType = APIConstants.CONTENT_TYPE_PREVIEW;
        }

        playerClosed = true;
        long ptimeInMinutes = getTotalPlayedTimeInMinutes();
        long ptimeInSec = getTotalPlayedTimeInSeconds() % 60;
        //Log.d(TAG, "before paused/totalPlayedTime calc stoppedAt ptimeInMinutes: " + ptimeInMinutes + " ptimeInSec: " + ptimeInSec);

        pausedAt();
        getTotalPlayedTime();

        ptimeInMinutes = getTotalPlayedTimeInMinutes();
        ptimeInSec = getTotalPlayedTimeInSeconds();
        //Log.d(TAG, "after paused/totalPlayedTime calc stoppedAt ptimeInMinutes: " + ptimeInMinutes + " ptimeInSec: " + ptimeInSec);
        if (mCardData != null
                && mCardData.generalInfo != null
                && mCardData.generalInfo.type != null) {
            if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mCardData.generalInfo.type)) {
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_MOVIE_STREAMED_FOR_MIN, ptimeInSec);
            } else if (APIConstants.TYPE_LIVE.equalsIgnoreCase(mCardData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)) {
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_TV_STREAMED_FOR_MIN, ptimeInMinutes);
            }
        }
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_TIME_PLAYED_IN_SEC, ptimeInSec);
        if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mContentType)
                || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(mContentType)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(mContentType)) {
            Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.play.name(), mContentType, mCardData, ptimeInMinutes, ptimeInSec);
        } else {
            Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.play.name(), mCardData, ptimeInMinutes, ptimeInSec);
        }
        String contentModel = CleverTap.PREVIEW_BANNER;
        String subtitle = mPlayer != null ? mPlayer.getSubtitleName() : "";
        subtitle = TextUtils.isEmpty(subtitle) ? mContext.getString(R.string.subtitle_opt_none) : subtitle;
        CleverTap.eventVideoStopped(contentModel,mTVShowCardData, mCardData, getTotalPlayedTimeInSeconds(), getPlayedPercentage(), isLocalPlayBack,
                mSource, mSourceDetails, subtitle,mSourceTab,sourceCarouselPosition,sourceContentPosition,mBufferCount,
                bufferTimeinsec,pauseCount,playCount,forwardCount,backwardCount,forwardTime,backwardTime,previewContentType);

        clearOnVideoStopped();

        if (mSavedContentPosition > 0) {
            CleverTap.eventVideoPlayed(contentModel,mTVShowCardData, mCardData, getTotalPlayedTimeInSeconds(),
                    getPlayedPercentage(), mSavedContentPosition, isLocalPlayBack, mSource, mSourceDetails,
                    mSourceTab,sourceCarouselPosition,sourceContentPosition);
        }
        if(!isPreviewContent) {
            int secs = 30;
            try {
                String prefSecs = PrefUtils.getInstance().getPrefAppsflyerPlaybackEventSeconds();
                if (!TextUtils.isEmpty(prefSecs)) {
                    secs = Integer.parseInt(prefSecs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (getTotalPlayedTimeInSeconds() >= secs && !PrefUtils.getInstance().getAppsFlyerPlayedEventFor30SecFired()) {
                PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(true);
                AppsFlyerTracker.eventPlayedVideoFor30Sec(new HashMap<String, Object>());
            }

            try {
                String prefSecs = PrefUtils.getInstance().getPrefBranchIOPlaybackEventSeconds();
                if (!TextUtils.isEmpty(prefSecs)) {
                    secs = Integer.parseInt(prefSecs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (getTotalPlayedTimeInSeconds() >= secs && !PrefUtils.getInstance().getBranchIOPlayedEventFor30SecFired()) {
                PrefUtils.getInstance().setBranchIOPlayedEventFor30SecFired(true);
                MyplexAnalytics.getInstance().event(new Event("played video for " + secs + " sec", new HashMap<String, Object>(), AnalyticsConstants.EventPriority.HIGH));
            }
        }

        if (!isPlayBackStartedAlready) {
            isPlayBackStartedAlready = true;
            Analytics.gaNotificationPlayBackSuccess(mNotificationTitle, mNid, mCardData, mProfileSelect);
        }
            /*String record = PrefUtils.getInstance().getString(mContext.getString(R.string
                    .last_played_util_mou));
            record = getRecordInLine(record, ptimeInMinutes, ptimeInSec);
            SharedPrefUtils.writeToSharedPref(mContext, mContext.getString(R.string.last_played_util_mou), record);*/
        makeMouUpdateRequest();
        storeDataForAnalyticsEventForLastPlayedContent();
    }

    public void stoppedAutoPlayAt(boolean isPreviewContent, CardData presentCardData) {
        if(!isPreviewContent) {
            previewContentType = null;
        }else {
            previewContentType = APIConstants.CONTENT_TYPE_PREVIEW;
        }

        playerClosed = true;
        long ptimeInMinutes = getTotalPlayedTimeInMinutes();
        long ptimeInSec = getTotalPlayedTimeInSeconds() % 60;
        //Log.d(TAG, "before paused/totalPlayedTime calc stoppedAt ptimeInMinutes: " + ptimeInMinutes + " ptimeInSec: " + ptimeInSec);

        pausedAt();
        getTotalPlayedTime();

        ptimeInMinutes = getTotalPlayedTimeInMinutes();
        ptimeInSec = getTotalPlayedTimeInSeconds();
        //Log.d(TAG, "after paused/totalPlayedTime calc stoppedAt ptimeInMinutes: " + ptimeInMinutes + " ptimeInSec: " + ptimeInSec);
        if (presentCardData != null
                && presentCardData.generalInfo != null
                && presentCardData.generalInfo.type != null) {
            if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(presentCardData.generalInfo.type)) {
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_MOVIE_STREAMED_FOR_MIN, ptimeInSec);
            } else if (APIConstants.TYPE_LIVE.equalsIgnoreCase(presentCardData.generalInfo.type)
                    || APIConstants.TYPE_PROGRAM.equalsIgnoreCase(presentCardData.generalInfo.type)) {
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_TV_STREAMED_FOR_MIN, ptimeInMinutes);
            }
        }
        Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_TIME_PLAYED_IN_SEC, ptimeInSec);
        if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mContentType)
                || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(mContentType)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(mContentType)) {
            Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.play.name(), mContentType, presentCardData, ptimeInMinutes, ptimeInSec);
        } else {
            Analytics.gaPlayedVideoTimeCalculation(Analytics.ACTION_TYPES.play.name(), presentCardData, ptimeInMinutes, ptimeInSec);
        }
        String contentModel = CleverTap.PREVIEW_BANNER;
        String subtitle = mPlayer != null ? mPlayer.getSubtitleName() : "";
        subtitle = TextUtils.isEmpty(subtitle) ? mContext.getString(R.string.subtitle_opt_none) : subtitle;
        CleverTap.eventVideoStopped(contentModel,mTVShowCardData, presentCardData, getTotalPlayedTimeInSeconds(), getPlayedPercentage(), isLocalPlayBack, mSource, mSourceDetails, subtitle,mSourceTab,sourceCarouselPosition,sourceContentPosition,mBufferCount,
                bufferTimeinsec,pauseCount,playCount,forwardCount,backwardCount,forwardTime,backwardTime,previewContentType);

        clearOnVideoStopped();

        CleverTap.eventVideoPlayed(contentModel,mTVShowCardData, presentCardData, getTotalPlayedTimeInSeconds(),
                getPlayedPercentage(), 1, isLocalPlayBack, mSource, mSourceDetails,mSourceTab,sourceCarouselPosition,sourceContentPosition);

        if(!isPreviewContent) {
            int secs = 30;
            try {
                String prefSecs = PrefUtils.getInstance().getPrefAppsflyerPlaybackEventSeconds();
                if (!TextUtils.isEmpty(prefSecs)) {
                    secs = Integer.parseInt(prefSecs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (getTotalPlayedTimeInSeconds() >= secs && !PrefUtils.getInstance().getAppsFlyerPlayedEventFor30SecFired()) {
                PrefUtils.getInstance().setAppsFlyerPlayedEventFor30SecFired(true);
                AppsFlyerTracker.eventPlayedVideoFor30Sec(new HashMap<String, Object>());
            }

            try {
                String prefSecs = PrefUtils.getInstance().getPrefBranchIOPlaybackEventSeconds();
                if (!TextUtils.isEmpty(prefSecs)) {
                    secs = Integer.parseInt(prefSecs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (getTotalPlayedTimeInSeconds() >= secs && !PrefUtils.getInstance().getBranchIOPlayedEventFor30SecFired()) {
                PrefUtils.getInstance().setBranchIOPlayedEventFor30SecFired(true);
                MyplexAnalytics.getInstance().event(new Event("played video for " + secs + " sec", new HashMap<String, Object>(), AnalyticsConstants.EventPriority.HIGH));
            }
        }

        if (!isPlayBackStartedAlready) {
            isPlayBackStartedAlready = true;
            Analytics.gaNotificationPlayBackSuccess(mNotificationTitle, mNid, presentCardData, mProfileSelect);
        }
            /*String record = PrefUtils.getInstance().getString(mContext.getString(R.string
                    .last_played_util_mou));
            record = getRecordInLine(record, ptimeInMinutes, ptimeInSec);
            SharedPrefUtils.writeToSharedPref(mContext, mContext.getString(R.string.last_played_util_mou), record);*/
        makeMouUpdateRequest();
        storeDataForAnalyticsEventForLastPlayedContent();
    }

    public void clearOnVideoStopped() {
        playCount = 0;
        pauseCount = 0;
        bufferTimeinsec = 0;
        forwardCount = 0;
        backwardCount = 0;
        forwardTime = 0;
        backwardTime = 0;
    }

}
