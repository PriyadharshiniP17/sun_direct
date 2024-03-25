package com.myplex.myplex.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPurchaseItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.DownloadContentData;
import com.myplex.model.SeasonData;
import com.myplex.player_config.DownloadManagerMaintainer;
import com.myplex.player_sdk.download.DownloadConfig;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.util.WidevineDrm;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.download.DownloadDataItem;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.services.FetchDownloadManagerIntentService;
import com.myplex.myplex.ui.activities.MainActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


import static com.myplex.api.APIConstants.TYPE_HUNGAMA;
import static com.myplex.myplex.ApplicationController.getApplicationConfig;

public class DownloadUtil {

    public static final String TAG = "DownloadUtil";

    private static int mDownloadStatus = 0;
    public static String downloadVideosStoragePath = "sundirect/videos/";
    private static final String DOWNLOAD_CONTENT_URI = "content://downloads/my_downloads";

    public static String getContentStoragePath(String title, Context mContext) {
        if (TextUtils.isEmpty(title)) return null;
        File data = new File(mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + title);
        if (!data.exists()) {
            data.mkdirs();
        }
        return data.getAbsolutePath();
    }

    public static File getMetaFile(String contentId,Context mContext) {
        String path = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + contentId + File.separator;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        path += "metaK";
        File data = new File(path);
        return data;
    }

    public static void deleteRecursive(final File fileOrDirectory, final ErosNowDownloadManager.OnDeletionLisnter onDeletionLisnter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (fileOrDirectory == null) {
                    LoggerD.debugDownload("deleteRecursive fileOrDirectory- " + fileOrDirectory);
                    return;
                }
                if (fileOrDirectory.isDirectory()) {
                    for (File child : fileOrDirectory.listFiles()) {
                        LoggerD.debugDownload("deleteRecursive child- " + child);
                        deleteRecursive(child);
                    }
                }
                LoggerD.debugDownload("deleteRecursive fileOrDirectory is not a directory- " + fileOrDirectory);
                fileOrDirectory.delete();

                if (onDeletionLisnter != null) {
                    onDeletionLisnter.onDelete();
                }
            }
        }).start();
    }

    public enum DownloadStatus {
        ALREADY_FILE_EXISTS, NOT_AVAILABLE_IN_DOWNLOADS, ERROR
    }




    public void actionDownloadComplete(Context context, Intent intent) {


        long download_id = intent.getLongExtra(
                DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        LoggerD.debugLog("actionDownloadComplete: downlaod_id- " + download_id);
        final CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
        if (downloadlist == null) {
            return;
        }
        final HashMap<String, CardDownloadData> mDownloadedList = downloadlist.mDownloadedList;

        if (mDownloadedList == null || mDownloadedList.isEmpty()) {
            return;
        }

        CardDownloadData availableDownloadData = null;
        for (String key : mDownloadedList.keySet()) {

            availableDownloadData = mDownloadedList.get(key);

            if (availableDownloadData == null) {
                continue;
            }
            if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList == null) {
                if (availableDownloadData.mVideoDownloadId == download_id
                        && availableDownloadData.zipStatus > CardDownloadData.STATUS_FILE_UNZIPPING) {
                    showNotificationDataAndUpdate(context, availableDownloadData,
                            availableDownloadData.mVideoDownloadId, downloadlist);
                    return;
                }
            } else if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList != null) {
                for (CardDownloadData episode : availableDownloadData.tvEpisodesList) {
                    if (episode.mVideoDownloadId == download_id
                            && availableDownloadData.zipStatus > CardDownloadData.STATUS_FILE_UNZIPPING) {
                        showNotificationDataAndUpdate(context, episode,episode.mVideoDownloadId, downloadlist);
                        return;
                    }
                }
            } else if (availableDownloadData.tvSeasonsList != null) {
                for (SeasonData seasonData : availableDownloadData.tvSeasonsList) {
                    for (CardDownloadData episode : seasonData.tvEpisodesList) {
                        if (episode.mVideoDownloadId == download_id
                                && episode.zipStatus > CardDownloadData.STATUS_FILE_UNZIPPING) {
                            showNotificationDataAndUpdate(context, episode,episode.mVideoDownloadId, downloadlist);
                            return;
                        }
                    }
                }
            }

        }

    }

    private void showNotificationDataAndUpdate(Context context, final CardDownloadData finalCardDownloadData,
                                               long download_id, final CardDownloadedDataList downloadList) {
        final FetchDownloadProgress mDownloadProgressManager = FetchDownloadProgress.getInstance(context);
        if (finalCardDownloadData.mVideoDownloadId == download_id) {

//            //Log.d(TAG, "download complete for content id :" + contentId);
            mDownloadStatus = mDownloadProgressManager.getDownloadStatusFromSDK(finalCardDownloadData._id);

            Log.e("Download","downlaod_id- :" + download_id +
//                    " contentId- " + contentId +
                            " getDownloadFailedReason- " + mDownloadProgressManager.getDownloadFailedReason(mDownloadStatus) +
                            " getDownloadStatus- " + mDownloadProgressManager.getDownloadStatusFromSDK(finalCardDownloadData._id)
            );

            LoggerD.debugLog("download complete status :" + mDownloadProgressManager.getDownloadFailedReason(mDownloadStatus));

            if (DownloadManagerMaintainer.STATE_COMPLETED == mDownloadStatus) {
                //notifyDownloadComplete(contentId);
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_DOWNLOADED_VIDEOS);
                finalCardDownloadData.mPercentage = 100;
                finalCardDownloadData.mCompleted = true;
                SDKUtils.saveObject(downloadList, getApplicationConfig().downloadCardsPath);
                showNotification(context, finalCardDownloadData._id, finalCardDownloadData);
            } else {
                AlertDialogUtil.showToastNotification(String.valueOf(mDownloadStatus));
            }
        }
    }

    private void acquireRights(CardDownloadData cardDownloadData, Context context, String contentId, CardData cardData) {

        if (cardData != null && cardData.generalInfo != null && cardData.generalInfo.type != null
                && cardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VOD)) {
            return;
        }
        if (cardData != null && cardData.currentUserData != null &&
                cardData.currentUserData.purchase != null && !cardData.currentUserData.purchase.isEmpty()) {

            CardDataPurchaseItem purchaseitem = cardData.currentUserData.purchase.get(0);

            if (purchaseitem.type != null && purchaseitem.type.equalsIgnoreCase("Rental")) {
                Log.e(TAG, "skiping acquireRights for DTR");
                return;
            }
        }

        String url = "file://" + cardDownloadData.mDownloadPath;
        WidevineDrm drmManager = new WidevineDrm(context);
        drmManager.registerPortal(WidevineDrm.Settings.PORTAL_NAME);
        int rightStatus = drmManager.checkRightsStatus(url);
        if (rightStatus != DrmStore.RightsStatus.RIGHTS_VALID) {
            int status = drmManager.acquireRights(url);
            /*if(status == DrmStore.RightsStatus.RIGHTS_VALID)
            {
				Util.showToast(context, "Rights Installed", Util.TOAST_TYPE_INFO);
			}else {
				Util.showToast(context, "accquired rights failed", Util.TOAST_TYPE_INFO);
			}*/
        }

    }

    public static void showNotification(final Context context, final String _id, final CardDownloadData cardDownloadData) {
        CleverTap.eventDownload(cardDownloadData, CleverTap.PROPERTY_DOWNLOAD_DOWNlOADED);
        CacheManager cacheManager = new CacheManager();
        cacheManager.getCardDetails(_id, false, new CacheManager.CacheManagerCallback() {
            @Override
            public void OnCacheResults(List<CardData> dataList) {
                //Log.d(TAG, "CardData on online results ");
                CardData cardDatas = null;
                if (dataList == null ||
                        dataList.isEmpty()) {
                    //Log.d(TAG, "OnOnlineResults dataList  - " + dataList);
                    return;
                }
                for (CardData data : dataList) {
                    if (data._id.equalsIgnoreCase(_id)) {
                        cardDatas = data;
                    }
                }
                if (cardDatas == null) {
                    return;
                }
                String title = cardDatas.generalInfo.title;
                //Log.d(TAG, "title =" + title);
                Intent notificationIntent = new Intent(context, MainActivity.class);

                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //Log.d(TAG, "DownloadUtil: OnOnlineResults - title " + title + " _id " + _id);
                notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_TITLE, title);
                if (DownloadManagerMaintainer.STATE_COMPLETED == mDownloadStatus
                        || (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardDownloadData.partnerName)
                        || APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardDownloadData.partnerName)
                        || APIConstants.isHungamaContent(cardDownloadData)
                        && cardDownloadData.mPercentage == 100
                        && cardDownloadData.mCompleted)) {
//                    acquireRights(cardDownloadData, context, _id, cardDatas);
                        String key = cardDatas.generalInfo._id + APIConstants.UNDERSCORE + APIConstants.downLoadStartTime;
                        long startTime = PrefUtils.getInstance().getLong(key);
                        long timetakenForDownload = System.currentTimeMillis() - startTime;
                        long timeInMinutes = TimeUnit.MILLISECONDS.toMinutes(timetakenForDownload);
                        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, _id);
                        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_ACTION,
                                APIConstants.NOTIFICATION_PARAM_AUTOPLAY);
                        String publishingHouseName = APIConstants.NOT_AVAILABLE;
                        if (cardDatas != null && cardDatas.publishingHouse != null) {
                            publishingHouseName = cardDatas.publishingHouse.publishingHouseName;
                        }
                    if (cardDatas.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VOD)) {
                        ComScoreAnalytics.getInstance().setEventDownloadCompleted(cardDatas,"NA",true);
                        Analytics.mixPanelDownloadsVideo(cardDatas.generalInfo.title, cardDatas._id, cardDownloadData.mDownloadTotalSize + "", timeInMinutes + "", publishingHouseName);
                    }
                    Util.showNotification(context, title, context.getString(R.string.notification_download_complete) + " " + title, notificationIntent);
                    return;
                }
                ComScoreAnalytics.getInstance().setEventDownloadCompleted(cardDatas,"NA",false);
                Util.showNotification(context, title, context.getString(R.string.notification_download_failed) + " " + title, notificationIntent);
            }

            @Override
            public void OnOnlineResults(List<CardData> dataList) {
                //Log.d(TAG, "CardData on online results ");
                CardData cardDatas = null;
                if (dataList == null ||
                        dataList.isEmpty()) {
                    //Log.d(TAG, "OnOnlineResults dataList  - " + dataList);
                    return;
                }
                for (CardData data : dataList) {
                    if (data._id.equalsIgnoreCase(_id)) {
                        cardDatas = data;
                    }
                }
                if (cardDatas == null) {
                    return;
                }
                String title = cardDatas.generalInfo.title;
                //Log.d(TAG, "title =" + title);
                Intent notificationIntent = new Intent(context, MainActivity.class);

                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //Log.d(TAG, "DownloadUtil: OnOnlineResults - title " + title + " _id " + _id);
                notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_TITLE, title);
                if (DownloadManager.STATUS_SUCCESSFUL == mDownloadStatus
                        || (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardDownloadData.partnerName)
                        || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardDownloadData.partnerName)
                        && cardDownloadData.mPercentage == 100
                        && cardDownloadData.mCompleted)) {
//                    acquireRights(cardDownloadData, context, _id, cardDatas);
                        String key = cardDatas.generalInfo._id + APIConstants.UNDERSCORE + APIConstants.downLoadStartTime;
                        long startTime = PrefUtils.getInstance().getLong(key);
                        long timetakenForDownload = System.currentTimeMillis() - startTime;
                        long timeInMinutes = TimeUnit.MILLISECONDS.toMinutes(timetakenForDownload);
                        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, _id);
                        notificationIntent.putExtra(APIConstants.NOTIFICATION_PARAM_ACTION, APIConstants.NOTIFICATION_PARAM_AUTOPLAY);
                        String publishingHouseName = APIConstants.NOT_AVAILABLE;
                        if (cardDatas != null && cardDatas.publishingHouse != null) {
                            publishingHouseName = cardDatas.publishingHouse.publishingHouseName;
                        }
                    if (cardDatas.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VOD)) {
                        Analytics.mixPanelDownloadsVideo(cardDatas.generalInfo.title, cardDatas._id, cardDownloadData.mDownloadTotalSize + "", timeInMinutes + "", publishingHouseName);
                    }
                    Util.showNotification(context, title, context.getString(R.string.notification_download_complete) + " " + title, notificationIntent);
                    return;
                }

                Util.showNotification(context, title, context.getString(R.string.notification_download_failed) + " " + title, notificationIntent);
            }

            @Override
            public void OnOnlineError(Throwable error, int errorCode) {

            }

        });
    }


    public static DownloadStatus startDownload(String aUrl, CardData aMovieData, Context mContext) {

        long downloadStartTime = System.currentTimeMillis();
        String key = aMovieData.generalInfo._id + APIConstants.UNDERSCORE + APIConstants.downLoadStartTime;
        PrefUtils.getInstance().setLong(key, downloadStartTime);
        long lastDownloadId = -1L;
        String aMovieName = aMovieData.generalInfo.title.toLowerCase();
        String aFileName = aMovieData._id;
        //Analytics.mixPanelDownloadsMovie(aMovieName,aFileName);
        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
        if (downloadlist != null
                && downloadlist.mDownloadedList != null
                && downloadlist.mDownloadedList.containsKey(aMovieData._id)) {
            CardDownloadData downloadedData = downloadlist.mDownloadedList.get(aMovieData._id);
            if (downloadedData != null
                    && (downloadedData.mDownloadTotalSize == -1
                    || (downloadedData.mCompleted && downloadedData.mPercentage == 0))) {
                downloadlist.mDownloadedList.remove(aMovieData._id);
                SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
            } else {
                return DownloadStatus.ALREADY_FILE_EXISTS;
            }
        }
        File fileDir = new File(mContext.getExternalFilesDir(null) + downloadVideosStoragePath);
        if (!fileDir.isDirectory()) {
            fileDir.mkdirs();
        }
        if (DownloadUtil.isFileExist(aFileName + ".mp4",mContext)) {
            DownloadUtil.removeFile(aFileName + ".mp4",mContext);
        }
        if (Util.isDownloadManagerAvailable(mContext)) {
            try {
                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(aUrl);
                try {
                    lastDownloadId =
                            manager.enqueue(new DownloadManager.Request(uri)
                                    .setAllowedOverRoaming(true)
                                    .setTitle(mContext.getString(R.string.app_name))
                                    .setDescription(aMovieName)
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                    .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath, aFileName + ".mp4"));
                    //Log.d(TAG, "downloading started for the url " + aUrl);
                } catch (Exception e) {
                    lastDownloadId = 0;
                    //Log.d(TAG, "download failed");
                    ComScoreAnalytics.getInstance().setEventDownloadInitiated(aMovieData,"NA", mContext.getResources().getString(R.string.download_error_while_download));
                    AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.vf_download_error_while_download));
                    e.printStackTrace();
                }
                if (lastDownloadId > 0) {
                    ComScoreAnalytics.getInstance().setEventDownloadInitiated(aMovieData,"NA", "NA");
                    AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_started));
                } else {
                    AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_error_network_failure));
                    ComScoreAnalytics.getInstance().setEventDownloadInitiated(aMovieData,"NA", mContext.getResources().getString(R.string.download_error_network_failure));
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_error_invalid_link));
                ComScoreAnalytics.getInstance().setEventDownloadInitiated(aMovieData,"NA", mContext.getResources().getString(R.string.download_error_invalid_link));
            }
        }
        if (downloadlist == null) {
            downloadlist = new CardDownloadedDataList();
            downloadlist.mDownloadedList = new HashMap<String, CardDownloadData>();
        }
        CardDownloadData downloadData = new CardDownloadData();
        downloadData.mDownloadId = lastDownloadId;
        downloadData.mDownloadPath = downloadVideosStoragePath + aFileName + ".mp4";
        //downloadData.mDownloadPath=mContext.getExternalFilesDir(null).getPath() +"/"+aMovieName+".wvm";
        downloadlist.mDownloadedList.put(aMovieData._id, downloadData);

        downloadData.mDownloadId = lastDownloadId;
        downloadData._id = aMovieData._id;
        downloadData.fileName = aFileName;
        downloadData.title = aMovieData.generalInfo.title;
        downloadData.ItemType = aMovieData.generalInfo.type;
        downloadData.downloadType = CardDownloadData.TYPE_DOWNLOAD_NON_DRM;
        downloadData.isStoredInternally = true;
        /*if(aMovieData.generalInfo.type.equalsIgnoreCase("movie")) {
			downloadData.ImageUrl = Util.getThumbImageLink(aMovieData);
			downloadData.coverPosterImageUrl = Util.getImageLink(aMovieData);
		}else{*/
        downloadData.ImageUrl = Util.getImageLink(aMovieData);
        downloadData.coverPosterImageUrl = Util.getImageLink(aMovieData);
//		}
        if (null != aMovieData.content) {
            StringBuilder genres = new StringBuilder();
            StringBuilder time_language = new StringBuilder();
            if (aMovieData.content.genre != null && aMovieData.content.genre.size() > 0) {
                for (CardDataGenre genre : aMovieData.content.genre) {
                    genres.append(genre.name.toUpperCase() + " | ");
                    break;
                }

                String releasedate = aMovieData.content.releaseDate;
                String upTo4Characters = "";
                if (releasedate != null)
                    upTo4Characters = releasedate.substring(0, Math.min(releasedate.length(), 4));
                List<String> languages = aMovieData.content.language;
                for (String language : languages) {
                    time_language.append(language.toUpperCase() + " | ");
                    break;
                }
                time_language.append(" " + upTo4Characters);
                downloadData.genres = genres.toString();
                if (!TextUtils.isEmpty(time_language)) {
                    downloadData.time_languages = time_language.toString();
                }
            }
        }
        //downloadData.mDownloadPath=mContext.getExternalFilesDir(null).getPath() +"/"+aMovieName+".wvm";

        ApplicationController.setDownloadData(downloadlist);

        SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);

        return DownloadStatus.NOT_AVAILABLE_IN_DOWNLOADS;
    }


    public static DownloadStatus startDownload(String aUrl,
                                               CardData cardData,
                                               Context mContext,
                                               String fileName,
                                               @NonNull String mVideoUrl,
                                               @NonNull String mAudioUrl,
                                               @NonNull String videoFileName,
                                               @NonNull String audioFileName,
                                               String seasonName,
                                               CardData tvShowData) {

        createNoMediaFile(mContext);

//        long downloadStartTime = System.currentTimeMillis();
//        String key = cardData.generalInfo._id + APIConstants.UNDERSCORE + APIConstants.downLoadStartTime;
//        PrefUtils.getInstance().setLong(key, downloadStartTime);

        long lastDownloadId = -1L;
        long videoDownloadId = -1L;
        long audioDownloadId = -1L;
        LoggerD.debugDownload("OnParseSuccess aUrl- " + aUrl
                + " fileName- " + fileName
                + " mVideoUrl- " + mVideoUrl
                + " mAudioUrl- " + mAudioUrl
                + " videoFileName- " + videoFileName
                + " audioFileName- " + audioFileName
        );
        String aMovieName = cardData._id.toLowerCase();

        String aFileName;

        if (fileName == null) {
            aFileName = cardData._id + ".mp4";
        } else {
            aFileName = fileName;
        }

        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();

        File fileDir = new File(mContext.getExternalFilesDir(null) + downloadVideosStoragePath + "/" + aMovieName);
        LoggerD.debugDownload("fileDir:fileDir- " + fileDir.getAbsolutePath());

        if (!fileDir.isDirectory()) {
            fileDir.mkdirs();
        }
        if (DownloadUtil.isFileExist(aFileName,mContext)) {
            DownloadUtil.removeFile(aFileName,mContext);
        }
        if (Util.isDownloadManagerAvailable(mContext)) {
            try {
                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(aUrl);
                try {
                    if (PrefUtils.getInstance().isDownloadOnlyOnWifi()) {
                        if (SDKUtils.getInternetConnectivity(mContext).equalsIgnoreCase("wifi")) {
                            lastDownloadId = manager.enqueue(new DownloadManager.Request(uri)
                                    .setAllowedOverRoaming(true)
                                    .setTitle(fileName)
									.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                                    .setDescription(fileName)
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                    .setVisibleInDownloadsUi(false)
                                    .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath + aMovieName, aFileName));
                            //Log.d(TAG, "downloading started for the url " + aUrl);
                        } else {
                            AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.txt_download_settings));
                        }
                    } else {
                        lastDownloadId = manager.enqueue(new DownloadManager.Request(uri)
                                .setAllowedOverRoaming(true)
                                .setTitle(fileName)
                                .setDescription(fileName)
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                .setVisibleInDownloadsUi(false)
                                .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath + aMovieName, aFileName));
                        //Log.d(TAG, "downloading started for the url " + aUrl);
                    }
                } catch (Exception e) {
                    lastDownloadId = 0;
                    //Log.d(TAG, "download failed");
                    AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.vf_download_error_while_download));
                    e.printStackTrace();
                    LoggerD.debugDownload("Exception:e- " + e.getMessage());
                }

                if (lastDownloadId > 0) {

                    final DownloadManager videoDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
//					Uri videoUri = Uri.parse("https://preprod-cdn.cloud.altbalaji.com/content/2016-06/472-57604d3c12582/SHOOTOUT_AT_WADAL_a1f8f0a3_video_track_0.mp4");
                    if (mVideoUrl != null) {
                        Uri videoUri = Uri.parse(mVideoUrl);
                        try {

                            if (PrefUtils.getInstance().isDownloadOnlyOnWifi()) {
                                if (SDKUtils.getInternetConnectivity(mContext).equalsIgnoreCase("wifi")) {
                                    videoDownloadId = videoDownloadManager.enqueue(new DownloadManager.Request(videoUri)
                                            .setAllowedOverRoaming(true)
                                            .setTitle(videoFileName)
										.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                                            .setDescription(videoFileName)
                                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                            .setTitle(cardData.generalInfo.title)
                                            .setVisibleInDownloadsUi(false)
                                            .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath + aMovieName, videoFileName));
                                    //Log.d(TAG, "downloading started for the url " + aUrl);
                                    LoggerD.debugDownload("downloadVideosStoragePath- " + downloadVideosStoragePath + " aMovieName- " + videoFileName);

                                } else {
                                    AlertDialogUtil.showToastNotification("Sorry Can't Download as you are not On Wifi, and your network preference was Wifi");
                                }

                            } else {
                            /*lastDownloadId=
									videoDownloadManager.enqueue(new DownloadManager.Request(videoUri)
											.setAllowedOverRoaming(true)
											.setTitle(mContext.getString(R.string.app_name))
											.setDescription(aMovieName)
											.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
											.setDestinationInExternalPublicDir(downloadVideosStoragePath, aFileName + ".mp4"));*/
                                videoDownloadId = videoDownloadManager.enqueue(new DownloadManager.Request(videoUri)
                                        .setAllowedOverRoaming(true)
                                        .setTitle(videoFileName)
                                        .setDescription(videoFileName)
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                        .setVisibleInDownloadsUi(false)
                                        .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath + aMovieName, videoFileName));
                                //Log.d(TAG, "downloading started for the url " + aUrl);
                                LoggerD.debugDownload("downloadVideosStoragePath- " + downloadVideosStoragePath + " aMovieName- " + videoFileName);
                            }
                        } catch (Exception e) {
                            lastDownloadId = 0;
                            //Log.d(TAG, "download failed");
                            AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.vf_download_error_while_download));
                            e.printStackTrace();
                            LoggerD.debugDownload("Exception e- " + e.getMessage());
                        }
                    }
                    if (videoDownloadId > 0) {
                        final DownloadManager audioDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                        if (mAudioUrl != null) {
                            Uri audioUri = Uri.parse(mAudioUrl);
                            try {
                                if (PrefUtils.getInstance().isDownloadOnlyOnWifi()) {
                                    if (SDKUtils.getInternetConnectivity(mContext).equalsIgnoreCase("wifi")) {
                                        audioDownloadId = audioDownloadManager.enqueue(new DownloadManager.Request(audioUri)
                                                .setAllowedOverRoaming(true)
                                                .setTitle(audioFileName)
											.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                                                .setDescription(audioFileName)
                                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                                .setTitle(cardData.generalInfo.title)
                                                .setVisibleInDownloadsUi(false)
                                                .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath + aMovieName, audioFileName));
                                        //Log.d(TAG, "downloading started for the url " + aUrl);
                                    } else {
                                        AlertDialogUtil.showToastNotification("Sorry Can't Download as you are not On Wifi, and your network preference was Wifi");
                                    }
                                } else {
                                    audioDownloadId = audioDownloadManager.enqueue(new DownloadManager.Request(audioUri)
                                            .setAllowedOverRoaming(true)
                                            .setTitle(audioFileName)
                                            .setDescription(audioFileName)
                                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                            .setTitle(cardData.generalInfo.title)
                                            .setVisibleInDownloadsUi(false)
                                            .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath + aMovieName, audioFileName));
                                    //Log.d(TAG, "downloading started for the url " + aUrl);
                                }
                            } catch (Exception e) {
                                lastDownloadId = 0;
                                //Log.d(TAG, "download failed");
                                AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.vf_download_error_while_download));
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_error_network_failure));
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_error_invalid_link));
            }
        }
        if (aFileName.contains(".mpd") && audioDownloadId > 0) {
            AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_started));
        } else if (!TextUtils.isEmpty(aFileName) && lastDownloadId > 0) {
            AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_started));
        }
        if (downloadlist == null) {
            downloadlist = new CardDownloadedDataList();
            downloadlist.mDownloadedList = new LinkedHashMap<>();
        }
        CardDownloadData downloadData = getDownloadData(seasonName, tvShowData, cardData, null,lastDownloadId, videoDownloadId, audioDownloadId, aMovieName, aFileName, videoFileName, audioFileName, null, mContext);
        if (downloadData != null) {
            downloadData.isStoredInternally = true;
            downloadlist.mDownloadedList.put(downloadData.downloadKey, downloadData);
            ApplicationController.setDownloadData(downloadlist);

            LoggerD.debugDownload("mDownloadPath- " + downloadData.mDownloadPath
                    + " audioFilePath- " + downloadData.audioFilePath
                    + " lastDownloadId- " + lastDownloadId
                    + " audioDownloadId- " + audioDownloadId
                    + " videoDownloadId- " + videoDownloadId
            );
        }

        SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
        FetchDownloadProgress.getInstance(mContext).startPolling();
        return DownloadStatus.NOT_AVAILABLE_IN_DOWNLOADS;
    }

    public static CardDownloadData getDownloadData(String seasonName,
                                                   CardData tvShowData,
                                                   CardData cardData,
                                                   String remoteUrl,
                                                   long lastDownloadId,
                                                   long videoDownloadId,
                                                   long audioDownloadId,
                                                   String aMovieName,
                                                   String aFileName,
                                                   String videoFileName,
                                                   String audioFileName,
                                                   String varientType, Context context) {
        if(cardData == null){
            LoggerD.debugDownload("cardData is null make sure it must not be null");
            return null;
        }
        String downloadKey = cardData._id;
        if(tvShowData != null){
            LoggerD.debugDownload("the content belongs to tv show");
            downloadKey = tvShowData._id;
        }
        if(APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(cardData.generalInfo.type)){
            downloadKey = cardData._id;
            if(APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)){
                downloadKey = cardData.generalInfo.partnerId;
            }
        }
        LoggerD.debugDownload("final download key is " + downloadKey);
        CardDownloadedDataList downloadList = ApplicationController.getDownloadData();
        CardDownloadData downloadData = null;
        if(downloadList != null
                && downloadList.mDownloadedList != null
                && downloadList.mDownloadedList.containsKey(downloadKey)){
            downloadData = downloadList.mDownloadedList.get(downloadKey);
        }

        LoggerD.debugDownload("available downloadData- " + downloadData);
        if (downloadData == null) {
            LoggerD.debugDownload("well there is no downloadData available");
            downloadData = new CardDownloadData();
        }
        LoggerD.debugDownload("available remoteUrl- " + remoteUrl);
        if (tvShowData == null) {
            prepareDownloadData(downloadData, cardData, lastDownloadId, videoDownloadId, audioDownloadId, aMovieName, aFileName,videoFileName, audioFileName, varientType, context);
            downloadData.hooqCacheId = remoteUrl;
            LoggerD.debugDownload("movie or video data is prepared downloaddata " + downloadData);
            return downloadData;
        }
        prepareDownloadData(downloadData, tvShowData, 0, 0, 0, aMovieName, aFileName, videoFileName, audioFileName, varientType, context);
        LoggerD.debugDownload("tv show data is preparing");
        if (TextUtils.isEmpty(seasonName)) {
            if(downloadData.tvEpisodesList == null){
                LoggerD.debugDownload("tv show with out a season data, and empty episodes list" +  downloadData);
                downloadData.tvEpisodesList = new ArrayList<>();
            }
            CardDownloadData episodeData = new CardDownloadData();
            prepareDownloadData(episodeData, cardData, lastDownloadId, videoDownloadId, audioDownloadId, aMovieName, aFileName,videoFileName, audioFileName, varientType, context);
            episodeData.hooqCacheId = remoteUrl;
            downloadData.tvEpisodesList.add(episodeData);
            LoggerD.debugDownload("tv show with out a season data, prepared episodeData, downloaddata " + episodeData + downloadData);
            return downloadData;
        }
        if (downloadData.tvSeasonsList == null) {
            LoggerD.debugDownload("tv show with a season data available, prepared downloadData- " + downloadData);
            downloadData.tvSeasonsList = new ArrayList<>();
        }
        SeasonData seasonData = getSeasonByName(seasonName,downloadData.tvSeasonsList);
        if (downloadData.tvSeasonsList.isEmpty()
                || seasonData == null) {
            seasonData = new SeasonData();
            seasonData.seasonName = seasonName;
            seasonData.tvEpisodesList = new ArrayList<>();
            CardDownloadData episodeData = new CardDownloadData();
            prepareDownloadData(episodeData, cardData, lastDownloadId, videoDownloadId, audioDownloadId, aMovieName, aFileName,videoFileName, audioFileName, varientType, context);
            episodeData.hooqCacheId = remoteUrl;
            seasonData.tvEpisodesList.add(episodeData);
            downloadData.tvSeasonsList.add(seasonData);
            LoggerD.debugDownload("tv show with out a season data, and current season are empty prepared season name, episodeData, downloaddata " + seasonName + episodeData + downloadData);
            return downloadData;
        }
        if (seasonData != null
                && seasonData.tvEpisodesList == null) {
            LoggerD.debugDownload("tv show with a season data, and current season are empty prepared season name, tvEpisodesList, downloaddata " + seasonName + seasonData.tvEpisodesList + downloadData);
            seasonData.tvEpisodesList = new ArrayList<>();
            seasonData.seasonName = seasonName;
            CardDownloadData episodeData = new CardDownloadData();
            prepareDownloadData(episodeData, cardData, lastDownloadId, videoDownloadId, audioDownloadId, aMovieName, aFileName,videoFileName, audioFileName, varientType, context);
            episodeData.hooqCacheId = remoteUrl;
            seasonData.tvEpisodesList.add(episodeData);
            downloadData.tvSeasonsList.add(seasonData);
        }
        CardDownloadData episodeData = new CardDownloadData();
        prepareDownloadData(episodeData, cardData, lastDownloadId, videoDownloadId, audioDownloadId, aMovieName, aFileName,videoFileName, audioFileName, varientType, context);
        episodeData.hooqCacheId = remoteUrl;
        seasonData.tvEpisodesList.add(episodeData);
        LoggerD.debugDownload("tv show with a season data, and current season are not empty prepared season name, episodeData, downloaddata " + seasonName + episodeData + downloadData);
        //downloadData.mDownloadPath=mContext.getExternalFilesDir(null).getPath() +"/"+aMovieName+".wvm";

        return downloadData;
    }

    public static void prepareDownloadData(final CardDownloadData downloadData,
                                           CardData cardData,
                                           long lastDownloadId,
                                           long videoDownloadId,
                                           long audioDownloadId,
                                           String aMovieName,
                                           String aFileName,
                                           String videoFileName,
                                           String audioFileName, String varientType, Context context) {
        if (cardData == null) {
            return;
        }
        downloadData._id = cardData._id;
        downloadData.title = cardData.generalInfo.title;
        downloadData.ItemType = cardData.generalInfo.type;
        downloadData.downloadType = CardDownloadData.TYPE_DOWNLOAD_DRM;
        downloadData.videoFileName = videoFileName;
        downloadData.audioFileName = audioFileName;
        if (!TextUtils.isEmpty(varientType))
            downloadData.variantType = varientType;

        downloadData.ImageUrl = Util.getImageLink(cardData);
//        new DownloadBitmapTask(context, downloadData);
        downloadData.coverPosterImageUrl = Util.getImageLink(cardData);
        String downloadKey = cardData._id;
        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(cardData.generalInfo.type)) {
            if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)) {
                downloadKey = cardData.generalInfo.partnerId;
            }
        }
        downloadData.mDownloadId = lastDownloadId;
        downloadData.mVideoDownloadId = videoDownloadId;
        if (videoDownloadId <= 0) {
            downloadData.mVideoDownloadId = lastDownloadId;
        }
        downloadData.mAudioDownloadId = audioDownloadId;
        downloadData.fileName = aFileName;
//        if (videoDownloadId > 0) {
//            downloadKey = String.valueOf(videoDownloadId);
//        } else if (lastDownloadId > 0) {
//            downloadKey = String.valueOf(lastDownloadId);
//        }
        downloadData.content = cardData.content;
        downloadData.mDownloadPath = downloadVideosStoragePath + aMovieName + "/" + aFileName;
        downloadData.audioFilePath = downloadVideosStoragePath + aMovieName + "/" + audioFileName;
        downloadData.downloadKey = downloadKey;
        if (cardData != null
                && cardData.publishingHouse != null
                && cardData.publishingHouse.publishingHouseName != null) {
            downloadData.partnerName = cardData.publishingHouse.publishingHouseName;
        }
        if (null != cardData.content) {
            StringBuilder genres = new StringBuilder();
            StringBuilder time_language = new StringBuilder();
            if (cardData.content.genre != null && cardData.content.genre.size() > 0) {
                /*for (CardDataGenre genre : cardData.content.genre) {
                    genres.append(genre.name.toUpperCase() + " | ");
                    break;
                }*/

                String releasedate = cardData.content.releaseDate;
                String upTo4Characters = "";
                if (releasedate != null)
                    upTo4Characters = releasedate.substring(0, Math.min(releasedate.length(), 4));
                /*List<String> language = cardData.content.language;
                for (String a : language) {
                    time_language.append(a.toUpperCase() + " | ");
                    break;
                }*/
                if (cardData.content != null
                        && cardData.content.language != null
                        && !cardData.content.language.isEmpty()
                        && cardData.content.language.get(0) != null) {
                    time_language.append(cardData.content.language.get(0));
                }
                if (cardData.content != null
                        && cardData.content.genre != null
                        && !cardData.content.genre.isEmpty()
                        && cardData.content.genre.get(0) != null
                        && cardData.content.genre.get(0).name != null) {
                    genres.append(cardData.content.genre.get(0).name);
                }
//                time_language.append("| " + upTo4Characters);
                downloadData.genres = genres.toString();
                downloadData.time_languages = time_language.toString();
            }
            downloadData.duration = cardData.content.duration;
            downloadData.releaseDate = cardData.content.releaseDate;
        }
        downloadData.briefDescription = cardData.generalInfo.briefDescription;
        downloadData.description = cardData.generalInfo.description;
        downloadData.zipStatus = CardDownloadData.STATUS_FILE_UNZIPPED;
        downloadData.isStoredInternally = true;
        if (APIConstants.isErosNowContent(cardData)) {
            downloadData.zipStatus = CardDownloadData.STATUS_FILE_ZIPPED;
        }

    }

    private static SeasonData getSeasonByName(String seasonName, List<SeasonData> seasons) {
        for (SeasonData seasonData : seasons) {
            if(seasonData.seasonName.equalsIgnoreCase(seasonName)) return seasonData;
        }
        return null;
    }


    public static boolean isFileExist(String fileName, Context mContext) {
        try {
            File sdDir = mContext.getExternalFilesDir(null);
            File file = new File(sdDir + DownloadUtil.downloadVideosStoragePath, fileName);
            if (file != null) {
                return file.getAbsoluteFile().exists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void removeFile(String fileName, Context mContext) {
        // TODO Auto-generated method stub
        try {
            File file = new File(mContext.getExternalFilesDir(null) + DownloadUtil.downloadVideosStoragePath,
                    fileName);
            if (file != null) {
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static DownloadStatus startDownload2(Context mContext, String aUrl, String
            fileName, String location) {
        if (aUrl == null) {
            return null;
        }
        long lastDownloadId = -1;
        if (Util.isDownloadManagerAvailable(mContext)) {
            try {
                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);


                //Uri uri=Uri.parse("http://46.137.243.190/wvm/armag_prod2.wvm");
                Uri uri = Uri.parse(aUrl);


                try {

                    lastDownloadId =
                            manager.enqueue(new DownloadManager.Request(uri)
                                    .setAllowedOverRoaming(true)
                                    .setTitle(fileName)
                                    .setDescription(fileName)
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                    .setDestinationInExternalFilesDir(mContext,location, fileName));
                    //Log.d(TAG, "downloading started for the url " + aUrl);
                } catch (Exception e) {
                    //Log.d(TAG, "download failed");
                    AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.vf_download_error_while_download));
                    e.printStackTrace();
//                    Crashlytics.logException(e);
                }

                if (lastDownloadId > 0) {
                    AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_started));
                } else {
                    AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_error_network_failure));
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                AlertDialogUtil.showToastNotification(mContext.getResources().getString(R.string.download_error_invalid_link));
            }
        } else {
            //Download Manager is not available
        }


        return DownloadStatus.NOT_AVAILABLE_IN_DOWNLOADS;
    }


    public static boolean createNoMediaFile(Context mContext) {
        File directory = new File(mContext.getExternalFilesDir(null) + downloadVideosStoragePath + "/");
        if (directory != null) {
            if (!directory.exists()) {
                directory.mkdirs();
            }
        } else {
            return false;
        }
        File file = new File(mContext.getExternalFilesDir(null) + downloadVideosStoragePath + "/");
        if (file != null) {
            try {
                if (file.exists()) {
                    return false;
                } else {
                    return file.createNewFile();
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static final void getDownloadDataFromDownloads(final CardData cardData, final OnDataRetrieverListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(listener != null){
                    CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
                    String contentKey = cardData._id;
                    if (cardData.publishingHouse != null
                            && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                            || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName))) {
                        contentKey = cardData.generalInfo.partnerId;
                    }
                    String downloadedKey = null;
                    for (String key : downloadlist.mDownloadedList.keySet()) {
                        CardDownloadData downloadedData = downloadlist.mDownloadedList.get(key);
                        if (downloadedData.tvSeasonsList == null && downloadedData.tvEpisodesList == null) {
                            downloadedKey = downloadedData._id;
                            if (cardData.publishingHouse != null
                                    && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                                    || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName))) {
                                downloadedKey = downloadedData.downloadKey;
                            }
                            if (contentKey.equalsIgnoreCase(downloadedKey)) {
                                sendData(downloadedData);
                                return;
                            }
                        } else if (downloadedData.tvSeasonsList == null && downloadedData.tvEpisodesList != null) {
                            for (CardDownloadData episode : downloadedData.tvEpisodesList) {
                                downloadedKey = episode._id;
                                if (cardData.publishingHouse != null
                                        && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                                        || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName))) {
                                    downloadedKey = episode.downloadKey;
                                }
                                if(contentKey.equalsIgnoreCase(downloadedKey)){
                                    sendData(episode);
                                    return;
                                }
                            }
                        } else if (downloadedData.tvSeasonsList != null) {
                            for (SeasonData seasonData : downloadedData.tvSeasonsList) {
                                for (CardDownloadData episode : seasonData.tvEpisodesList) {
                                    downloadedKey = episode._id;
                                    if (cardData.publishingHouse != null
                                            && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                                            || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName))) {
                                        downloadedKey = episode.downloadKey;
                                    }
                                    if (contentKey.equalsIgnoreCase(downloadedKey)) {
                                        sendData(episode);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    sendData(null);
                }
            }

            private void sendData(final CardDownloadData downloadedData) {
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDataLoaded(downloadedData);
                        }
                    });
                }
            }
        }).start();
    }


    public static final void getDownloadDataFromDownloads(final CardDownloadData cardDownloadData, final OnDataRetrieverListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(listener != null){
                    CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
                    String contentKey = cardDownloadData._id;
                    if (cardDownloadData != null
                            && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardDownloadData.partnerName)
                                || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardDownloadData.partnerName))) {
                        contentKey = cardDownloadData.downloadKey;
                    }
                    String downloadedKey = null;
                    for (String key : downloadlist.mDownloadedList.keySet()) {
                        CardDownloadData downloadedData = downloadlist.mDownloadedList.get(key);
                        if (downloadedData.tvSeasonsList == null && downloadedData.tvEpisodesList == null) {
                            downloadedKey = downloadedData._id;
                            if ((APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardDownloadData.partnerName)
                                    || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardDownloadData.partnerName))) {
                                downloadedKey = downloadedData.downloadKey;
                            }
                            if (contentKey.equalsIgnoreCase(downloadedKey)) {
                                sendData(downloadedData);
                                return;
                            }
                        } else if (downloadedData.tvSeasonsList == null && downloadedData.tvEpisodesList != null) {
                            for (CardDownloadData episode : downloadedData.tvEpisodesList) {
                                downloadedKey = episode._id;
                                if ((APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardDownloadData.partnerName)
                                        || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardDownloadData.partnerName))) {
                                    downloadedKey = episode.downloadKey;
                                }
                                if(contentKey.equalsIgnoreCase(downloadedKey)){
                                    sendData(episode);
                                    return;
                                }
                            }
                        } else if (downloadedData.tvSeasonsList != null) {
                            for (SeasonData seasonData : downloadedData.tvSeasonsList) {
                                for (CardDownloadData episode : seasonData.tvEpisodesList) {
                                    downloadedKey = episode._id;
                                    if ((APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardDownloadData.partnerName)
                                            || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardDownloadData.partnerName))) {
                                        downloadedKey = episode.downloadKey;
                                    }
                                    if (contentKey.equalsIgnoreCase(downloadedKey)) {
                                        sendData(episode);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    sendData(null);
                }
            }

            private void sendData(final CardDownloadData downloadedData) {
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDataLoaded(downloadedData);
                        }
                    });
                }
            }
        }).start();
    }

    public static final CardDownloadData getDownloadDataFromDownloads(final CardData cardData) {
        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
        String contentKey = cardData._id;
        if (cardData.publishingHouse != null
                && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName))) {
            contentKey = cardData.generalInfo.partnerId;
        }
        if (contentKey == null) {
            return null;
        }
        String downloadedKey = null;
        for (String key : downloadlist.mDownloadedList.keySet()) {
            CardDownloadData availableDownloadData = downloadlist.mDownloadedList.get(key);
            if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList == null) {
                downloadedKey = availableDownloadData._id;
                if (cardData.publishingHouse != null
                        && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                        || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName))) {
                    downloadedKey = availableDownloadData.downloadKey;
                }
                if (contentKey.equalsIgnoreCase(downloadedKey)) {
                    return availableDownloadData;
                }
            } else if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList != null) {
                for (CardDownloadData episode : availableDownloadData.tvEpisodesList) {
                    downloadedKey = episode._id;
                    if (cardData.publishingHouse != null
                            && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                            || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName))) {
                        downloadedKey = episode.downloadKey;
                    }
                    if(contentKey.equalsIgnoreCase(downloadedKey)){
                        return episode;
                    }
                }
            } else if (availableDownloadData.tvSeasonsList != null) {
                for (SeasonData seasonData : availableDownloadData.tvSeasonsList) {
                    for (CardDownloadData episode : seasonData.tvEpisodesList) {
                        downloadedKey = episode._id;
                        if (cardData.publishingHouse != null
                                && (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                                || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName))) {
                            downloadedKey = episode.downloadKey;
                        }
                        if (contentKey.equalsIgnoreCase(downloadedKey)) {
                            return episode;
                        }
                    }
                }
            }
        }
        return null;
    }

    public interface OnDataRetrieverListener {
        void onDataLoaded(CardDownloadData data);
    }

    public static float getBitsInMbs(float bits){
        return  (bits / 8f / 1024f / 1024f);//GB/hr
    }

    public static DownloadStatus startDownload(final DownloadDataItem downloadDataItem,Context mContext) {

        if (downloadDataItem == null) {
            return DownloadStatus.NOT_AVAILABLE_IN_DOWNLOADS;
        }
        createNoMediaFile(mContext);

//        long downloadStartTime = System.currentTimeMillis();
//        String key = cardData.generalInfo._id + APIConstants.UNDERSCORE + APIConstants.downLoadStartTime;
//        PrefUtils.getInstance().setLong(key, downloadStartTime);

        long lastDownloadId = -1L;
        long videoDownloadId = -1L;
        long audioDownloadId = -1L;
        /*LoggerD.debugDownload("OnParseSuccess aUrl- " + aUrl
                + " fileName- " + fileName
                + " mVideoUrl- " + mVideoUrl
                + " mAudioUrl- " + mAudioUrl
                + " videoFileName- " + videoFileName
                + " audioFileName- " + audioFileName
        );*/
        String aMovieName = downloadDataItem.cardData._id.toLowerCase();

        String aFileName;

        if (downloadDataItem.fileName == null) {
            aFileName = downloadDataItem.cardData._id + ".mp4";
        } else {
            aFileName = downloadDataItem.fileName;
        }

        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();

        File fileDir = new File(mContext.getExternalFilesDir(null) + downloadVideosStoragePath + "/" + aMovieName);
        LoggerD.debugDownload("fileDir:fileDir- " + fileDir.getAbsolutePath());

        if (!fileDir.isDirectory()) {
            fileDir.mkdirs();
        }
        if (DownloadUtil.isFileExist(aFileName,mContext)) {
            DownloadUtil.removeFile(aFileName,mContext);
        }
        if (Util.isDownloadManagerAvailable(downloadDataItem.mContext)) {
            try {
                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) downloadDataItem.mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(downloadDataItem.aUrl);
                try {
                    if (PrefUtils.getInstance().isDownloadOnlyOnWifi()) {
                        if (SDKUtils.getInternetConnectivity(downloadDataItem.mContext).equalsIgnoreCase("wifi")) {
                            lastDownloadId = manager.enqueue(new DownloadManager.Request(uri)
                                    .setAllowedOverRoaming(true)
                                    .setTitle(downloadDataItem.fileName)
                                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                                    .setDescription(downloadDataItem.fileName)
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                    .setVisibleInDownloadsUi(false)
                                    .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath + aMovieName, aFileName));
                            //Log.d(TAG, "downloading started for the url " + downloadDataItem.aUrl);
                        } else {
                            AlertDialogUtil.showToastNotification(downloadDataItem.mContext.getResources().getString(R.string.txt_download_settings));
                        }
                    } else {
                        lastDownloadId = manager.enqueue(new DownloadManager.Request(uri)
                                .setAllowedOverRoaming(true)
                                .setTitle(downloadDataItem.fileName)
                                .setDescription(downloadDataItem.fileName)
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                .setVisibleInDownloadsUi(false)
                                .setDestinationInExternalFilesDir(mContext,downloadVideosStoragePath + aMovieName, aFileName));
                        LoggerD.debugDownload("downloading started for the url " + downloadDataItem.aUrl);
                    }
                } catch (Exception e) {
                    lastDownloadId = 0;
                    //Log.d(TAG, "download failed");
                    AlertDialogUtil.showToastNotification(downloadDataItem.mContext.getResources().getString(R.string.vf_download_error_while_download));
                    e.printStackTrace();
                    LoggerD.debugDownload("Exception:e- " + e.getMessage());
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                AlertDialogUtil.showToastNotification(downloadDataItem.mContext.getResources().getString(R.string.download_error_invalid_link));
            }
        }

        if (lastDownloadId > 0) {
            AlertDialogUtil.showToastNotification(downloadDataItem.mContext.getResources().getString(R.string.download_started));
        }

        if (downloadlist == null) {
            downloadlist = new CardDownloadedDataList();
            downloadlist.mDownloadedList = new LinkedHashMap<>();
        }
        CardDownloadData downloadData = getDownloadData(downloadDataItem.seasonName,
                downloadDataItem.tvShowData,
                downloadDataItem.cardData,
                null,
                lastDownloadId,
                videoDownloadId,
                audioDownloadId,
                aMovieName,
                aFileName,
                downloadDataItem.videoFileName,
                downloadDataItem.audioFileName,
                downloadDataItem.varientType, downloadDataItem.mContext);
        if (downloadData != null) {
            downloadData.isStoredInternally = true;
            downloadlist.mDownloadedList.put(downloadData.downloadKey, downloadData);
            ApplicationController.setDownloadData(downloadlist);
        }
        LoggerD.debugDownload("mDownloadPath- " + downloadData.mDownloadPath
                + " audioFilePath- " + downloadData.audioFilePath
                + " lastDownloadId- " + lastDownloadId
                + " audioDownloadId- " + audioDownloadId
                + " videoDownloadId- " + videoDownloadId);
        SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
        FetchDownloadProgress.getInstance(downloadDataItem.mContext).startPolling();
        return DownloadStatus.NOT_AVAILABLE_IN_DOWNLOADS;
    }


    public static void removeFilePath(String filePath) {
        // TODO Auto-generated method stub
        try {
            File file = new File(filePath);
            if (file != null) {
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteRecursive(final File fileOrDirectory) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (fileOrDirectory == null) {
                    LoggerD.debugDownload("deleteRecursive fileOrDirectory- " + fileOrDirectory);
                    return;
                }
                if (fileOrDirectory.isDirectory()) {
                    for (File child : fileOrDirectory.listFiles()) {
                        LoggerD.debugDownload("deleteRecursive child- " + child);
                        deleteRecursive(child);
                    }
                }
                LoggerD.debugDownload("deleteRecursive fileOrDirectory is not a directory- " + fileOrDirectory);
                fileOrDirectory.delete();
            }
        }).start();
    }

    public static CardDownloadData prepareHungamaDownloadData(Context mContext, CardData cardData, String downloadPath){

        if (cardData == null) {
            return null;
        }
        CardDownloadData downloadData = new CardDownloadData();
        downloadData._id = cardData._id;
        downloadData.title = cardData.generalInfo.title;
        downloadData.ItemType = cardData.generalInfo.type;
        downloadData.downloadType = CardDownloadData.TYPE_DOWNLOAD_DRM;

        downloadData.ImageUrl = Util.getImageLink(cardData);
        downloadData.coverPosterImageUrl = Util.getImageLink(cardData);
//        new DownloadBitmapTask(mContext, downloadData);
        String downloadKey = cardData._id;
        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(cardData.generalInfo.type)) {
            if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)
                    || TYPE_HUNGAMA.equalsIgnoreCase(cardData.publishingHouse.publishingHouseName)) {
                downloadKey = cardData.generalInfo.partnerId;
            }
        }

        downloadData.mDownloadPath = downloadPath;
        downloadData.downloadKey = downloadKey;
        if (cardData != null
                && cardData.publishingHouse != null
                && cardData.publishingHouse.publishingHouseName != null) {
            downloadData.partnerName = cardData.publishingHouse.publishingHouseName;
        }
        downloadData.content = cardData.content;
        if (null != cardData.content) {
            StringBuilder genres = new StringBuilder();
            StringBuilder time_language = new StringBuilder();
            if (cardData.content.genre != null && cardData.content.genre.size() > 0) {
                /*for (CardDataGenre genre : cardData.content.genre) {
                    genres.append(genre.name.toUpperCase() + " | ");
                    break;
                }*/

                String releasedate = cardData.content.releaseDate;
                String upTo4Characters = "";
                if (releasedate != null)
                    upTo4Characters = releasedate.substring(0, Math.min(releasedate.length(), 4));
                /*List<String> language = cardData.content.language;
                for (String a : language) {
                    time_language.append(a.toUpperCase() + " | ");
                    break;
                }*/
                if (cardData.content != null
                        && cardData.content.language != null
                        && !cardData.content.language.isEmpty()
                        && cardData.content.language.get(0) != null) {
                    time_language.append(cardData.content.language.get(0));
                }
                if (cardData.content != null
                        && cardData.content.genre != null
                        && !cardData.content.genre.isEmpty()
                        && cardData.content.genre.get(0) != null
                        && cardData.content.genre.get(0).name != null) {
                    genres.append(cardData.content.genre.get(0).name);
                }
//                time_language.append("| " + upTo4Characters);
                downloadData.genres = genres.toString();
                downloadData.time_languages = time_language.toString();
            }
            downloadData.duration = cardData.content.duration;
            downloadData.releaseDate = cardData.content.releaseDate;
        }
        downloadData.briefDescription = cardData.generalInfo.briefDescription;
        downloadData.description = cardData.generalInfo.description;
        downloadData.zipStatus = CardDownloadData.STATUS_FILE_UNZIPPING;
        if (APIConstants.isErosNowContent(cardData)) {
            downloadData.zipStatus = CardDownloadData.STATUS_FILE_ZIPPED;
        }
        return downloadData;
    }

    private static class DownloadBitmapTask {
        DownloadBitmapTask(final Context context, final CardDownloadData downloadData) {
            PicassoUtil.with(context).download(downloadData.coverPosterImageUrl, new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    LoggerD.debugDownload("onBitmapLoaded "
                            + "\nbitmap- " + bitmap
                            + "\ndownloadData- " + downloadData);
                    if (bitmap == null || downloadData == null) {
                        return;
                    }
                }

                @Override
                public void onBitmapFailed(Exception e,Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
            PicassoUtil.with(context).download(downloadData.ImageUrl, new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    LoggerD.debugDownload("onBitmapLoaded "
                            + "\nbitmap- " + bitmap
                            + "\ndownloadData- " + downloadData);
                    if (bitmap == null || downloadData == null) {
                        return;
                    }
                }

                @Override
                public void onBitmapFailed(Exception e,Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
    }

    public static boolean isFileExists(String path) {
        try {
            File f = new File(path);
            return f.exists();
        } catch (Exception ex) {
            if (ex != null) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static DownloadStatus startDownload(String aUrl,
                                               CardData aMovieData,
                                               Context mContext,
                                               String fileName,
                                               @NonNull String mVideoUrl,
                                               @NonNull String mAudioUrl,
                                               @NonNull String videoFileName,
                                               @NonNull String audioFileName,
                                               String subtitleLink,
                                               String size,
                                               String drmToken,
                                               String drmLicense,
                                               String format) {

        createNoMediaFile(mContext);

        long downloadStartTime = System.currentTimeMillis();
        String key = aMovieData.generalInfo._id + APIConstants.UNDERSCORE + APIConstants.downLoadStartTime;
        PrefUtils.getInstance().setLong(key, downloadStartTime);
        if (mContext != null && Util.isNetworkAvailable(mContext)) {
            if (aMovieData.generalInfo != null) {
                //Util.updatePlayerEvents(aMovieData.generalInfo._id, String.valueOf(0), "Download", aMovieData.generalInfo.type, 0);
            }
        } else {
            String updatePlayerString = "contentId:" + aMovieData.generalInfo._id + "," + "elapsedTime:" + 0 + "," +
                    "action:" + "Download" + "," + "contentType:" + aMovieData.generalInfo.type + "," + "mou:" + 0;
            //PrefUtils.getInstance().storePlayerEvent(updatePlayerString);
        }
        String subtitleFileName = APIConstants.SUBTITLE_FILE_NAME;
        long lastDownloadId = -1L;
        long videoDownloadId = -1L;
        long audioDownloadId = -1L;
        long subtitleDownloadId = -1L;

        String audioFilePath;
        String aMovieName = aMovieData.generalInfo.title.toLowerCase() + APIConstants.UNDERSCORE + aMovieData._id;

        String aFileName;

        if (fileName == null) {
            aFileName = aMovieData._id;
        } else {
            aFileName = fileName;
        }

        //Analytics.mixPanelDownloadsMovie(aMovieName,aFileName);
        CardDownloadedDataList downloadlist = null;
        try {
            downloadlist = (CardDownloadedDataList) SDKUtils.loadObject(ApplicationController.getApplicationConfig().downloadCardsPath);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (downloadlist != null
                && downloadlist.mDownloadedList != null
                && downloadlist.mDownloadedList.containsKey(aMovieData._id)) {
            CardDownloadData downloadedData = downloadlist.mDownloadedList.get(aMovieData._id);
            if (downloadedData != null
                    && (downloadedData.mDownloadTotalSize == -1
                    || (downloadedData.mCompleted && downloadedData.mPercentage == 0))) {
                downloadlist.mDownloadedList.remove(aMovieData._id);
                SDKUtils.saveObject(downloadlist, ApplicationController.getApplicationConfig().downloadCardsPath);
            } else {
                return DownloadStatus.ALREADY_FILE_EXISTS;
            }
        }
//        File fileDir = new File(Environment.getExternalStorageDirectory()+downloadVideosStoragePath+"/"+aMovieName);
        File fileDir = new File(mContext.getExternalFilesDir(null) + downloadVideosStoragePath + "/" + aMovieName);
        //Log.d(TAG, "scoped permission called");
        if (!fileDir.isDirectory()) {
            fileDir.mkdirs();
        }
        if (DownloadUtil.isFileExist(aFileName,mContext)) {
            DownloadUtil.removeFile(aFileName,mContext);
        }

        if(ApplicationController.shouldUseExoDownloadManager){
            //Using same Service as fetch to keep track of downloads
            startDownloadUsingFetch(aUrl,aMovieData,mContext,fileName,mVideoUrl,
                    mAudioUrl, videoFileName,   audioFileName,  subtitleLink, size,drmToken,drmLicense,format);
            return DownloadStatus.NOT_AVAILABLE_IN_DOWNLOADS;
        }
        return DownloadStatus.NOT_AVAILABLE_IN_DOWNLOADS;
    }


    public static void startDownloadUsingFetch(String aUrl, final CardData aMovieData, final Context mContext,
                                               String fileName, @NonNull String mVideoUrl,
                                               @NonNull String mAudioUrl, @NonNull String videoFileName,
                                               @NonNull String audioFileName, String subtitleLink,
                                               final String size, String drmToken, String drmLicenseUrl,String format){
        Intent downloadServiceIntent = new Intent(mContext, FetchDownloadManagerIntentService.class);
        DownloadContentData downloadContentData = new DownloadContentData();
        downloadContentData.aUrl = aUrl;
        downloadContentData.aMovieData = aMovieData;
        downloadContentData.fileName = fileName;
        downloadContentData.mVideoUrl = mVideoUrl;
        downloadContentData.mAudioUrl = mAudioUrl;
        downloadContentData.videoFileName = videoFileName;
        downloadContentData.audioFileName = audioFileName;
        downloadContentData.subtitleLink = subtitleLink;
        downloadContentData.size = size;
        downloadContentData.format=format;

        if(ApplicationController.shouldUseExoDownloadManager){
            downloadContentData.drmLicenseUrl = drmLicenseUrl;
            downloadContentData.drmToken = drmToken;
            downloadContentData.shouldDownloadOnWifi = PrefUtils.getInstance().isDownloadOnlyOnWifi();
            downloadContentData.userAgent = "SunDirect";
        }
        downloadServiceIntent.putExtra(APIConstants.DOWNLOAD_CONTENT_DATA,downloadContentData);
        mContext.startService(downloadServiceIntent);

    }

    public static int getDownloadStatusFromSDK(String contentId){
        return DownloadManagerMaintainer.getInstance().getDownloadStatus(contentId);
    }


    public static void saveDownloadData(Context mContext, CardData aMovieData,
                                        String aFileName, long lastDownloadId,
                                        long videoDownloadId, long audioDownloadId,
                                        long subtitleDownloadId, String aMovieName,
                                        String audioFileName,String size,
                                        CardDownloadedDataList downloadlist,
                                        boolean isExoDownloadManagerUsed,
                                        DownloadConfig.DOWNLOAD_CONTENT_TYPE contentType) {

        //Try fetching existing list
        if(downloadlist == null){
            try {
                downloadlist = (CardDownloadedDataList) SDKUtils.loadObject(ApplicationController.getApplicationConfig().downloadCardsPath);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        //If first case fails then create new one
        if (downloadlist == null) {
            downloadlist = new CardDownloadedDataList();
            downloadlist.mDownloadedList = new LinkedHashMap<String, CardDownloadData>();
        }else if(downloadlist.mDownloadedList.containsKey(aMovieData._id)){
            return;
        }
        if (TextUtils.isEmpty(aMovieName)) {
            aMovieName = aMovieData.generalInfo.title.toLowerCase() + APIConstants.UNDERSCORE + aMovieData._id;
        }
        CardDownloadData downloadData = new CardDownloadData();
        downloadData.mDownloadId = lastDownloadId;
        downloadData._id = aMovieData._id;
        downloadData.fileName = aFileName;
        downloadData.title = aMovieData.generalInfo.title;
        downloadData.mVideoDownloadId = videoDownloadId;
        downloadData.mAudioDownloadId = audioDownloadId;
        if (contentType== DownloadConfig.DOWNLOAD_CONTENT_TYPE.CONTENT_TYPE_DASH){
            downloadData.contentType=APIConstants.STREAMDASH;
        }else {
            downloadData.contentType=APIConstants.STREAMINGFORMATHLS;
        }

        if (subtitleDownloadId > 0) {
            downloadData.mSubtitleDownloadId = subtitleDownloadId;
        }
        downloadData.ItemType = aMovieData.generalInfo.type;
        /*if (aMovieData.content != null && aMovieData.content.contentRating != null)
            downloadData.starRating = Float.parseFloat(aMovieData.content.contentRating);
*/
        if (aMovieData.publishingHouse != null) {
            downloadData.publishingHouseID = aMovieData.publishingHouse.publishingHouseId;
        }
        downloadData.isFree = aMovieData != null && aMovieData.generalInfo != null && aMovieData.generalInfo.contentRights != null
                && aMovieData.generalInfo.contentRights.size() > 0 && aMovieData.generalInfo.contentRights.get(0) != null &&
                aMovieData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.TYPE_FREE);
        if (aMovieData.generalInfo != null) {
            downloadData.showWatermark = aMovieData.generalInfo.getShowWatermark();
        }
        if (aMovieData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_MOVIE)) {
            downloadData.ImageUrl = getImageLink(aMovieData, aMovieData.generalInfo.type);
            downloadData.coverPosterImageUrl = Util.getImageLink(aMovieData);
            PicassoUtil.with(mContext).fetch(downloadData.ImageUrl);
            PicassoUtil.with(mContext).fetch(downloadData.coverPosterImageUrl);
        }
        if (aMovieData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VODCHANNEL)) {
            downloadData.ImageUrl = getImageLink(aMovieData, aMovieData.generalInfo.type);
            PicassoUtil.with(mContext).fetch(downloadData.ImageUrl);
        } else {
            downloadData.ImageUrl = getImageLink(aMovieData, APIConstants.COVERPOSTER);
            downloadData.coverPosterImageUrl = getImageLink(aMovieData, aMovieData.generalInfo.type);
            PicassoUtil.with(mContext).fetch(downloadData.ImageUrl);
            PicassoUtil.with(mContext).fetch(downloadData.coverPosterImageUrl);
        }
        downloadData.mDownloadPath = downloadVideosStoragePath + aMovieName;
        downloadData.audioFilePath = downloadVideosStoragePath + aMovieName + "/" + audioFileName;
        if (null != aMovieData.content) {
            StringBuilder genres = new StringBuilder();
            StringBuilder genresForMixpanel = new StringBuilder();
            StringBuilder time_language = new StringBuilder();
            if (aMovieData.content.genre != null && aMovieData.content.genre.size() > 0) {
                for (CardDataGenre genre : aMovieData.content.genre) {

                    genres.append(genre.name.toUpperCase() + " | ");
                    if (genresForMixpanel.length() == 0) {
                        genresForMixpanel.append(genre.name);
                    } else {
                        genresForMixpanel.append(genre.name + ",");
                    }
                }

                String releasedate = aMovieData.content.releaseDate;
                String upTo4Characters = "";
                if (releasedate != null)
                    upTo4Characters = releasedate.substring(0, Math.min(releasedate.length(), 4));
                List<String> language = aMovieData.content.language;
                for (String a : language) {
                    time_language.append(a.toUpperCase() + " | ");
                }
                time_language.append(" " + upTo4Characters);
                downloadData.genres = genres.toString();
                downloadData.time_languages = time_language.toString();
                StringBuilder languageContent = new StringBuilder();
                if (aMovieData.content != null && aMovieData.content.language != null) {

                    for (String a : aMovieData.content.language) {
                        if (languageContent.length() == 0) {
                            languageContent.append(a);
                        } else {
                            languageContent.append("," + a);
                        }
                    }

                } else {
                    languageContent.append(Analytics.NULL_VALUE);
                }
                downloadData.language = languageContent.toString();


            }
            if (aMovieData.publishingHouse != null && aMovieData.publishingHouse.publishingHouseName != null) {
                downloadData.contentPartner = aMovieData.publishingHouse.publishingHouseName;
            } else {
                downloadData.contentPartner = Analytics.NULL_VALUE;

            }
            if (aMovieData.globalServiceId != null && !aMovieData.globalServiceId.isEmpty())
                downloadData.globalServiceID = aMovieData.globalServiceId;
            else
                downloadData.globalServiceID = Analytics.NULL_VALUE;

            if (!TextUtils.isEmpty(aMovieData.content.categoryType)) {
                downloadData.categoryType = aMovieData.content.categoryType;
            } else {
                downloadData.categoryType = "";
            }
            if (!TextUtils.isEmpty(aMovieData.content.categoryName)) {
                downloadData.categoryName = aMovieData.content.categoryName;
            } else {
                downloadData.categoryName = "";
            }
        }

        if(aMovieData!=null&&aMovieData.generalInfo!=null) {
            if(!TextUtils.isEmpty(aMovieData.generalInfo.briefDescription)) {
                downloadData.briefDescription=aMovieData.generalInfo.briefDescription;
            }else {
                downloadData.briefDescription=Analytics.NULL_VALUE;
            }

            if(!TextUtils.isEmpty(aMovieData.generalInfo.description)) {
                downloadData.description=aMovieData.generalInfo.description;
            }else {
                downloadData.description=Analytics.NULL_VALUE;
            }
        }

        //downloadData.mDownloadPath=mContext.getExternalFilesDir(null).getPath() +"/"+aMovieName+".wvm";
        downloadlist.mDownloadedList.put(aMovieData._id, downloadData);
        ApplicationController.sDownloadList = downloadlist;
        SDKUtils.saveObject(downloadlist, ApplicationController.getApplicationConfig().downloadCardsPath);
        Analytics.downloadInitiatedEvent(aMovieData, size, Analytics.NULL_VALUE);
       /* AlertDialogUtil.showToastNotification(
                mContext.getResources().getString(R.string.download_started_info) + " \"" +
                        aMovieData.generalInfo.title + ".\" " +
                        mContext.getResources().getString
                                (R.string.notification_download_complete_info));*/
    }

    public static final String getImageLink(CardData carouselData, String type) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes;
        if (type.equalsIgnoreCase(APIConstants.TYPE_VODCHANNEL)) {
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_PREVIEW, APIConstants.COVERPOSTER};
        } else if (type.equalsIgnoreCase(APIConstants.TYPE_MUSIC_VIDEO)
                || type.equalsIgnoreCase(APIConstants.TYPE_VOD)
                || type.equalsIgnoreCase(APIConstants.TYPE_MOVIE)) {
            imageTypes = new String[]{
                    APIConstants.COVERPOSTER,
                    APIConstants.IMAGE_TYPE_PORTRAIT_BANNER,
                    APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,};
        } else {
            imageTypes = new String[]{APIConstants.COVERPOSTER};
        }

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.HDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }



}
