package com.myplex.myplex.analytics;

import android.text.TextUtils;

/*import com.comscore.streaming.AdvertisementMetadata;
import com.comscore.streaming.AdvertisementType;
import com.comscore.streaming.ContentMetadata;
import com.comscore.streaming.ContentType;
import com.comscore.streaming.StreamingAnalytics;*/
import com.myplex.model.CardData;
import com.myplex.model.CardDataContent;
import com.myplex.model.CardDataGenre;


public class PlayerStreamingAnalytics {

    private static PlayerStreamingAnalytics playerStreamingAnalytics = null;
//    private static StreamingAnalytics streamingAnalytics = null;

  //  private AdvertisementMetadata am = null;


    public static PlayerStreamingAnalytics getInstance() {
        if (playerStreamingAnalytics == null) {
            playerStreamingAnalytics = new PlayerStreamingAnalytics();
        }

        /*if (streamingAnalytics == null) {
            streamingAnalytics = new StreamingAnalytics();
            streamingAnalytics.setImplementationId("7947673");
            streamingAnalytics.setProjectId("");
            streamingAnalytics.setMediaPlayerName("");
            streamingAnalytics.setMediaPlayerVersion("");
            streamingAnalytics.createPlaybackSession();
        }*/

        return playerStreamingAnalytics;
    }

    public void notifyBufferStarted(CardData mCardData, String tabName) {
        if (isNullObject(mCardData)) {
            return;
        }
      /*  ContentMetadata cm = new ContentMetadata.Builder()
                .mediaType(ContentType.LONG_FORM_ON_DEMAND)
                .uniqueId(checkAndReturnValue(mCardData._id))
                .length(getDurationInMills(mCardData.content))
                .dictionaryClassificationC3("4")
                .dictionaryClassificationC4("*null")
                .dictionaryClassificationC6("*null")
                .stationTitle(PUBLISHER_NAME)
                .publisherName(PUBLISHER_NAME)
                .programTitle(checkAndReturnValue(mCardData.generalInfo.title))
                .genreName(getGenreName(mCardData))
                .classifyAsCompleteEpisode(true)
                .build();

        streamingAnalytics.setMetadata(cm);*/
       // streamingAnalytics.notifyBufferStart();
    }

    public void notifyBufferStoped(CardData mCardData, String tabName) {
        if (isNullObject(mCardData)) {
            return;
        }
       /* ContentMetadata cm = new ContentMetadata.Builder()
                .mediaType(ContentType.LONG_FORM_ON_DEMAND)
                .uniqueId(checkAndReturnValue(mCardData._id))
                .length(getDurationInMills(mCardData.content))
                .dictionaryClassificationC3("4")
                .dictionaryClassificationC4("*null")
                .dictionaryClassificationC6("*null")
                .stationTitle(PUBLISHER_NAME)
                .publisherName(PUBLISHER_NAME)
                .programTitle(checkAndReturnValue(mCardData.generalInfo.title))
                .genreName(getGenreName(mCardData))
                .classifyAsCompleteEpisode(true)
                .build();

        streamingAnalytics.setMetadata(cm);
*/       // streamingAnalytics.notifyBufferStop();
    }

    public void notifyPlaying(CardData mCardData, boolean isAd,int elapsedTime) {
        if (isNullObject(mCardData)) {
            return;
        }
       /* ContentMetadata cm = new ContentMetadata.Builder()
                .mediaType(ContentType.LONG_FORM_ON_DEMAND)
                .uniqueId(checkAndReturnValue(mCardData._id))
                .length(getDurationInMills(mCardData.content))
                .dictionaryClassificationC3("4")
                .dictionaryClassificationC4("*null")
                .dictionaryClassificationC6("*null")
                .stationTitle(PUBLISHER_NAME)
                .publisherName(PUBLISHER_NAME)
                .programTitle(checkAndReturnValue(mCardData.generalInfo.title))
                .genreName(getGenreName(mCardData))
                .classifyAsCompleteEpisode(true)
                .build();*/

        if (isAd) {
            /*am = new AdvertisementMetadata.Builder()
                    .mediaType(getAdtype(elapsedTime))
                    .relatedContentMetadata(cm)
                    .build();
            streamingAnalytics.setMetadata(am);
        } else {
            streamingAnalytics.setMetadata(cm);*/
        }
      //  streamingAnalytics.notifyPlay();
    }

    public void notifyPaused(CardData mCardData, boolean isAd,int elapsedTime) {
        if (isNullObject(mCardData)) {
            return;
        }
       /* ContentMetadata cm = new ContentMetadata.Builder()
                .mediaType(ContentType.LONG_FORM_ON_DEMAND)
                .uniqueId(checkAndReturnValue(mCardData._id))
                .length(getDurationInMills(mCardData.content))
                .dictionaryClassificationC3("4")
                .dictionaryClassificationC4("*null")
                .dictionaryClassificationC6("*null")
                .stationTitle(PUBLISHER_NAME)
                .publisherName(PUBLISHER_NAME)
                .programTitle(checkAndReturnValue(mCardData.generalInfo.title))
                .genreName(getGenreName(mCardData))
                .classifyAsCompleteEpisode(true)
                .build();*/

        if (isAd) {
           /* am = new AdvertisementMetadata.Builder()
                    .mediaType(getAdtype(elapsedTime))
                    .relatedContentMetadata(cm)
                    .build();
            streamingAnalytics.setMetadata(am);*/
        } else {
           // streamingAnalytics.setMetadata(cm);
        }
       // streamingAnalytics.notifyPause();
    }

    public void notifyEnded(CardData mCardData, boolean isAd,int elapsedTime) {
        if (isNullObject(mCardData)) {
            return;
        }
       /* ContentMetadata cm = new ContentMetadata.Builder()
                .mediaType(ContentType.LONG_FORM_ON_DEMAND)
                .uniqueId(checkAndReturnValue(mCardData._id))
                .length(getDurationInMills(mCardData.content))
                .dictionaryClassificationC3("4")
                .dictionaryClassificationC4("*null")
                .dictionaryClassificationC6("*null")
                .stationTitle(PUBLISHER_NAME)
                .publisherName(PUBLISHER_NAME)
                .programTitle(checkAndReturnValue(mCardData.generalInfo.title))
                .genreName(getGenreName(mCardData))
                .classifyAsCompleteEpisode(true)
                .build();
*/
        if (isAd) {
           /* am = new AdvertisementMetadata.Builder()
                    .mediaType(getAdtype(elapsedTime))
                    .relatedContentMetadata(cm)
                    .build();
            streamingAnalytics.setMetadata(am);*/
        } else {
            //streamingAnalytics.setMetadata(cm);
        }
       // streamingAnalytics.notifyEnd();
        //if (!isAd)
          //  streamingAnalytics.createPlaybackSession();
    }

    public void notifySeekStarted(CardData mCardData, String tabName) {
        if (isNullObject(mCardData)) {
            return;
        }
        /*ContentMetadata cm = new ContentMetadata.Builder()
                .mediaType(ContentType.LONG_FORM_ON_DEMAND)
                .uniqueId(checkAndReturnValue(mCardData._id))
                .length(getDurationInMills(mCardData.content))
                .dictionaryClassificationC3("4")
                .dictionaryClassificationC4("*null")
                .dictionaryClassificationC6("*null")
                .stationTitle(PUBLISHER_NAME)
                .publisherName(PUBLISHER_NAME)
                .programTitle(checkAndReturnValue(mCardData.generalInfo.title))
                .genreName(getGenreName(mCardData))
                .classifyAsCompleteEpisode(true)
                .build();

        streamingAnalytics.setMetadata(cm);*/
       // streamingAnalytics.notifySeekStart();
    }

    public void seekStartFromPosition(CardData mCardData, long pos, String tabName) {
        if (isNullObject(mCardData)) {
            return;
        }
        /*ContentMetadata cm = new ContentMetadata.Builder()
                .mediaType(ContentType.LONG_FORM_ON_DEMAND)
                .uniqueId(checkAndReturnValue(mCardData._id))
                .length(getDurationInMills(mCardData.content))
                .dictionaryClassificationC3("4")
                .dictionaryClassificationC4("*null")
                .dictionaryClassificationC6("*null")
                .stationTitle(PUBLISHER_NAME)
                .publisherName(PUBLISHER_NAME)
                .programTitle(checkAndReturnValue(mCardData.generalInfo.title))
                .genreName(getGenreName(mCardData))
                .classifyAsCompleteEpisode(true)
                .build();

        streamingAnalytics.setMetadata(cm);*/
       // streamingAnalytics.startFromPosition(pos);
    }

    private String checkAndReturnValue(String value) {
        if (value == null || TextUtils.isEmpty(value)) {
            return "NA";
        }
        return value;
    }

    private String getGenreName(CardData mData) {
        String genreName = "NA";
        if (mData != null && mData.content != null) {
            StringBuilder genres = new StringBuilder();
            if (mData.content.genre != null && mData.content.genre.size() > 0) {
                for (CardDataGenre genre : mData.content.genre) {
                    if (genres.length() > 0) {
                        genres.append(",");
                    }
                    genres.append(genre.name);
                }
                genreName = genres.toString();
            }
        }
        return genreName;
    }

  /*  private int getAdtype(int elapsedTime){
        int adType= AdvertisementType.ON_DEMAND_PRE_ROLL;
        if (elapsedTime >= 60){
            adType=AdvertisementType.ON_DEMAND_MID_ROLL;
        }
        return adType;
    }*/

    private Boolean isNullObject(CardData mCardData) {
        return mCardData == null || mCardData.generalInfo == null;
    }

    public int getDurationInMills(CardDataContent content) {
        int durationInSeconds = 0;
        if (content == null || content.duration == null) return durationInSeconds;
        try {
            String[] hhmmss = content.duration.split(":");
            int hhtosecFactor = 60 * 60;
            int mmtosecFactor = 60;
            if (hhmmss.length > 2) {
                durationInSeconds = Integer.parseInt(hhmmss[0]) * hhtosecFactor + Integer.parseInt(hhmmss[1]) * mmtosecFactor + Integer.parseInt(hhmmss[2]);
            } else if (hhmmss.length > 1) {
                durationInSeconds = Integer.parseInt(hhmmss[0]) * mmtosecFactor + Integer.parseInt(hhmmss[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return durationInSeconds * 1000;
    }
}
