package com.myplex.myplex.download;

import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CardDataVideos;
import com.myplex.model.CardDataVideosItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.PublishingHouse;
import com.myplex.model.SeasonData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.download.drm.utils.AdaptionSet;
import com.myplex.myplex.download.drm.utils.MPD;
import com.myplex.myplex.download.drm.utils.MPDParser;
import com.myplex.myplex.download.drm.utils.RepresentationData;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.recievers.DownloadManagerIntentService;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.Util;

import junit.framework.Assert;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.myplex.myplex.ApplicationController.getApplicationConfig;
import static com.myplex.myplex.ApplicationController.getDownloadData;

/**
 * Created by Srikanth on 08-Sep-17.
 */

public class ErosNowDownloadManager {
    //    public static final String EROSNOW_DOWNLOAD_CONTENT_ID = "45424";
    public static final String EROSNOW_DOWNLOAD_CONTENT_ID = "45375";
//    public static final String EROSNOW_DOWNLOAD_CONTENT_ID = "101302";
    public static final int ERROR_CODE_RIGHTS_REQUEST_FAILURE = 500;

    private static Context mContext = null;
    List<UnzipProcessListener> unzipListners = new ArrayList<>();
    private static ErosNowDownloadManager erosNowDownloadManager = null;
    private FragmentCardDetailsDescription.DownloadStatusListener mDownloadStatusListener;
    private FetchDownloadProgress.DownloadProgressStatus mDownloadProgressListener = new FetchDownloadProgress.DownloadProgressStatus() {
        @Override
        public void DownloadProgress(CardData cardData, CardDownloadData downloadData) {
            if (downloadData != null
                    && APIConstants.isErosNowContent(downloadData)
                    && downloadData.mCompleted
                    && (downloadData.zipStatus == CardDownloadData.STATUS_FILE_ZIPPED)) {
                LoggerD.debugDownload("@@ Initialize unzip for downloaded file- " + downloadData.title);
                unzipFile(downloadData, null);
            }
        }
    };
    private Decompress.OnExtractCompletionListener mUnzipCompletionListener = new Decompress.OnExtractCompletionListener() {
        @Override
        public void onComplete(CardDownloadData cardDownloadData) {
            LoggerD.debugDownload("Successfully unzipped the file, cardDownloadData- " + cardDownloadData);
            if (cardDownloadData == null) {
                return;
            }
            String destinationFilePath = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + cardDownloadData._id + File.separator;
            initDownloadKeys(destinationFilePath + "manifest.mpd", cardDownloadData);
        }

        @Override
        public void onProgress(CardDownloadData cardDownloadData, float progress) {
            LoggerD.debugDownload("onProgress the file, cardDownloadData- " + cardDownloadData);
            if (cardDownloadData == null) {
                return;
            }
            LoggerD.debugDownload("onProgress progress- " + progress + " aggr per- " + (cardDownloadData.mPercentage + progress / 50));

        }

        @Override
        public void onFailed(CardDownloadData cardDownloadData) {
//                        initDownloadKeys(destinationFilePath + "manifest.mpd", cardDownloadData);
            LoggerD.debugDownload("Failed to unzip the file");
            CardData cardData = new CardData();
            cardData._id = cardDownloadData._id;

            CardDownloadData data = DownloadUtil.getDownloadDataFromDownloads(cardData);
            if (data == null) {
                return;
            }
            CardDownloadedDataList downloadedDataList = getDownloadData();
            downloadedDataList.mDownloadedList.put(data._id, cardDownloadData);
            //SDKUtils.saveObject(downloadedDataList, getApplicationConfig().downloadCardsPath);
            informUnzipCompletion(cardDownloadData);
        }
    };


    private static boolean isContextExpired(Context mContext, Context newContext) {
        return (mContext != null
                && mContext instanceof Application
                && newContext instanceof Activity)
                || (mContext != null
                && mContext instanceof Activity
                && ((Activity) mContext).isFinishing()
                && newContext != null);
    }

    private ErosNowDownloadManager(Context context) {
        mContext = context;
    }

    public static synchronized ErosNowDownloadManager getInstance(Context context) {
        try {
            if (erosNowDownloadManager == null) {
                erosNowDownloadManager = new ErosNowDownloadManager(context);
            }

            if (isContextExpired(mContext, context)) {
                erosNowDownloadManager = new ErosNowDownloadManager(context);
                LoggerD.debugDownload("over riding the instanse with Acitivity context");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return erosNowDownloadManager;
    }

    private Context getContext() {
        return mContext;
    }

    public synchronized void unzipFile(final CardDownloadData cardDownloadData, final UnzipProcessListener listener) {
        LoggerD.debugDownload("initializing unzip for taskData- " + cardDownloadData.title);
        addListener(listener);
        final String destinationFilePath = mContext.getExternalFilesDir(null)  + DownloadUtil.downloadVideosStoragePath + cardDownloadData._id + File.separator;
        Decompress mDecompress = Decompress.getInstance(mContext);
        Decompress.DecompressTaskData taskData = new Decompress.DecompressTaskData();
        taskData.cardDownloadData = cardDownloadData;
        taskData.zipFilePath = destinationFilePath + cardDownloadData.fileName;
        taskData.destinationFilePath = destinationFilePath;
        mDecompress.unzip(taskData, mUnzipCompletionListener);
    }


    public void initDownloadKeys(final String downloadUrl, final CardDownloadData cardDownloadData) {
        Assert.assertNotNull(downloadUrl);
//        Assert.assertNotNull(cardDownloadData);
        executeDownloadKeys(new DownloadKeys(new WeakReference<>(this), downloadUrl, cardDownloadData));
    }

    private void executeDownloadKeys(DownloadKeys task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    private void parseMpdForDownload(final CardDownloadData cardDownloadData) {
        LoggerD.debugDownload("parseMpdForDownload cardDownloadData- " + cardDownloadData.title);
        final MPDParser mpdParser = new MPDParser();
        mpdParser.setPartnerType(CardDetails.Partners.ALTBALAJI);
        if (APIConstants.TYPE_EROS_NOW.equalsIgnoreCase(cardDownloadData.partnerName)) {
            mpdParser.setPartnerType(CardDetails.Partners.EROSNOW);
        }
        MPDParserCallBack mMPDParserCallback = new MPDParserCallBack(cardDownloadData);
        mpdParser.addParserListerner(mMPDParserCallback);
        String contentId = cardDownloadData._id;
        if (TextUtils.isEmpty(contentId)) {
            LoggerD.debugDownload("invalid content id");
            return;
        }
        String zipFilePath = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + contentId + File.separator;
        mpdParser.execute(zipFilePath + "manifest.mpd");
    }

    public void startDownload(ContentDownloadEvent event, FragmentCardDetailsDescription.DownloadStatusListener downloadStatusListener) {
        this.mDownloadStatusListener = downloadStatusListener;
        if (event != null) {
            showDownloadQualitySelection(event);
        }
    }

    private void downloadFile(CardDataVideosItem downloadLink, ContentDownloadEvent event) {
//        String downloadLink1 = "http://movies-dash-dl-a.erosnow.com/d/09/1000009/dl-low.zip";
        if (downloadLink == null || TextUtils.isEmpty(downloadLink.link)) {
            return;
        }
        final DownloadDataItem downloadDataItem = new DownloadDataItem();
        downloadDataItem.cardData = event.cardData;
        downloadDataItem.tvShowData = event.tvShowCardData;
        downloadDataItem.seasonName = event.seasonName;
        downloadDataItem.aUrl = downloadLink.link.replace("https:", "http:");
        downloadDataItem.fileName = event.cardData._id + ".zip";
        downloadDataItem.varientType = downloadLink.profile;
        downloadDataItem.mContext = mContext;
//        saveDownloadDataItem(cardData);
        LoggerD.debugDownload("cardData- " + downloadDataItem);
        if (mDownloadStatusListener != null) {
            mDownloadStatusListener.onDownloadInitialized();
        }
        new DeleteThread(downloadDataItem.cardData, new OnDeletionLisnter() {
            @Override
            public void onDelete() {
                new Handler(mContext.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //DownloadUtil.startDownload(downloadDataItem,mContext);
                        if (mDownloadStatusListener != null) mDownloadStatusListener.onSuccess();
                    }
                });
            }
        }).start();
    }

    public void destroy() {
        erosNowDownloadManager = null;
        LoggerD.debugDownload("destroy this");
    }


    class MPDParserCallBack implements MPDParser.MDPParserListerner {

        private CardDownloadData cardDownloadData;

        MPDParserCallBack(CardDownloadData cardDownloadData) {
            this.cardDownloadData = cardDownloadData;
        }

        @Override
        public void OnParseFailed() {
//                showAlertDialog(getResources().getString(R.string.parse_error));
//            Toast.makeText(mContext, "onFailed to parse", Toast.LENGTH_SHORT).show();
            LoggerD.debugDownload("failed to parse mpd file");
        }

        @Override
        public void OnParseSuccess(MPD mpd) {
            String videoUrl = null;
            String audioUrl = null;
            for (AdaptionSet adaptionSet : mpd.adaptionSetList) {
                switch (adaptionSet.type) {
                    case AdaptionSet.TYPE_VIDEO:
                        for (RepresentationData representationData : adaptionSet.listRepresentations) {
                            float floatBitrate = (representationData.bandwidth / 8f / 1024f / 1024f / 1024f) * 60f * 60f;//GB/hr
                            LoggerD.debugDownload("prepare download tracks: bandwidth- " + floatBitrate);
//                            float mbs = representationData.bandwidth / 1000000f;
//                            float kbs = mbs * 1024;
                            videoUrl = representationData.getFileName();
                        }
                        break;
                    case AdaptionSet.TYPE_AUDIO:
                        for (RepresentationData representationData : adaptionSet.listRepresentations) {
                            float floatBitrate = (representationData.bandwidth / 8f / 1024f / 1024f / 1024f) * 60f * 60f;//GB/hr
                            LoggerD.debugDownload("prepare download tracks: bandwidth- " + floatBitrate);
//                            float mbs = representationData.bandwidth / 1000000f;
//                            float kbs = mbs * 1024;
                            audioUrl = representationData.getFileName();
                        }
                        break;
                }
            }
            CardData cardData = new CardData();
            cardData._id = cardDownloadData._id;

            //CardDataGeneralInfo for CardData
            CardDataGeneralInfo generalInfo = new CardDataGeneralInfo();
            generalInfo._id = cardDownloadData._id;
            cardData.generalInfo = generalInfo;
            cardData.publishingHouse = new PublishingHouse();
            cardData.publishingHouse.publishingHouseName = APIConstants.TYPE_EROS_NOW;

            final String destinationFilePath = mContext.getExternalFilesDir(null)
                    + File.separator + DownloadUtil.downloadVideosStoragePath
                    + cardDownloadData._id + File.separator
                    + cardDownloadData.fileName;

            final String finalAudioUrl = audioUrl;
            final String finalVideoUrl = videoUrl;
            cardDownloadData.audioFileName = finalAudioUrl;
            cardDownloadData.audioFilePath = finalAudioUrl;
            cardDownloadData.videoFileName = finalVideoUrl;
            String mpdFilePath = DownloadUtil.downloadVideosStoragePath + cardDownloadData._id + File.separator + "manifest.mpd";
            cardDownloadData.fileName = mpdFilePath;
            cardDownloadData.mDownloadPath = mpdFilePath;
            cardDownloadData.mCompleted = true;
            cardDownloadData.mPercentage = 100;
            cardDownloadData.zipStatus = CardDownloadData.STATUS_FILE_UNZIPPED;
            CardDownloadData data = DownloadUtil.getDownloadDataFromDownloads(cardData);
            if (data == null) {
                return;
            }
            CardDownloadedDataList downloadedDataList = getDownloadData();
            final HashMap<String, CardDownloadData> mDownloadedList = downloadedDataList.mDownloadedList;
            CardDownloadData availableDownloadData = null;
            try {
                if(cardDownloadData.ItemType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)){
                    for(String key: mDownloadedList.keySet()){
                        availableDownloadData = mDownloadedList.get(key);
                        if(availableDownloadData.ItemType.equalsIgnoreCase(APIConstants.TV_SERIES)){
                            if (availableDownloadData.tvSeasonsList != null) {
                                for (SeasonData seasonData : availableDownloadData.tvSeasonsList) {
                                    for (CardDownloadData episode : seasonData.tvEpisodesList) {
                                        if(episode.mVideoDownloadId == cardDownloadData.mVideoDownloadId){
                                            episode.zipStatus = cardDownloadData.zipStatus;
                                        }
                                    }
                                }
                            }else{
                                downloadedDataList.mDownloadedList.put(data._id, cardDownloadData);
                            }
                        }
                    }
                }else{
                    downloadedDataList.mDownloadedList.put(data._id, cardDownloadData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            DownloadUtil.removeFilePath(destinationFilePath);
            SDKUtils.saveObject(downloadedDataList, getApplicationConfig().downloadCardsPath);
            informUnzipCompletion(cardDownloadData);
            Intent intent = new Intent();
            intent.setClass(mContext, DownloadManagerIntentService.class);
            intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, cardDownloadData.mVideoDownloadId);
            //DownloadManagerIntentService.enqueueWork(mContext,intent);
            LoggerD.debugDownload("saving parsed mpd download data- " + cardDownloadData);
        }


        @Override
        public void OnParseProgress(MPDParser.Progress update) {
            LoggerD.debugDownload("update" + update);
        }
    }

    private void informUnzipCompletion(CardDownloadData cardDownloadData) {
        LoggerD.debugDownload("informUnzipCompletion cardDownloadData- " + cardDownloadData + " unzipListners- " + unzipListners);
        if (unzipListners == null) return;
        Iterator<UnzipProcessListener> iter = unzipListners.iterator();
        while(iter.hasNext()){
            UnzipProcessListener listener = iter.next();
            LoggerD.debugDownload("Inform unzipCompletion for cardDownloadData- " + cardDownloadData + " listener- " + listener);
            listener.onCompletion(cardDownloadData);
        }
    }


    private void informUnzipFailure() {
        for (UnzipProcessListener listener : unzipListners) {
            listener.onFailure();
        }
    }


    public interface UnzipProcessListener {
        void onCompletion(CardDownloadData cardDownloadData);

        void onFailure();
    }

    public void checkAndCompleteUnzip() {
//        parseMpdForDownload(ApplicationController.getDownloadData().mDownloadedList.get());
        for (String key : getDownloadData().mDownloadedList.keySet()) {
            CardDownloadData cardDownloadData = getDownloadData().mDownloadedList.get(key);
            if (cardDownloadData != null
                    && APIConstants.isErosNowContent(cardDownloadData)
                    && cardDownloadData.mCompleted
                    && cardDownloadData.zipStatus <= CardDownloadData.STATUS_FILE_UNZIPPING) {
                LoggerD.debugDownload("@@ Initialize unzip for downloaded file- " + cardDownloadData.title);
                unzipFile(cardDownloadData, null);
            }
        }
    }

    public void initUnzipManagerListener(UnzipProcessListener listener) {
        LoggerD.debugDownload("init unzip listeners");
        addListener(listener);
        if (mContext == null) {
            LoggerD.debugDownload("failed to unzip downloaded file, invalid context");
            return;
        }
        FetchDownloadProgress.getInstance(mContext).removeProgressListener(mDownloadProgressListener);
        FetchDownloadProgress.getInstance(mContext).addProgressListener(mDownloadProgressListener);
    }

    private class DownloadKeys extends AsyncTask<Void, Void, Void> {

        private final WeakReference<ErosNowDownloadManager> mDownloadManagerUtilityWeakReference;
        private final String downloadUrl;
        private CardDownloadData cardDownloadData;

        public DownloadKeys(WeakReference<ErosNowDownloadManager> dowloadManagerUtilityWeakReference, String downloadUrl, CardDownloadData cardDownloadData) {
            this.mDownloadManagerUtilityWeakReference = dowloadManagerUtilityWeakReference;
            this.downloadUrl = downloadUrl;
            this.cardDownloadData = cardDownloadData;
        }

        @Override
        protected Void doInBackground(Void... params) {

//            String drmLicenseUrl = APIConstants.getDRMLicenseUrl(contentDownloadData.cardData._id, APIConstants.VIDEO_TYPE_DOWNLOAD);
            String contentId = null;
            if (cardDownloadData == null) {
                return null;
            }
            contentId = cardDownloadData._id;
            //String drmLicenseUrl = APIConstants.getDRMLicenseUrl(contentId, APIConstants.VIDEO_TYPE_DOWNLOAD,cardDownloadData.variantType);
            try {
                DefaultHttpDataSourceFactory httpDataSourceFactory;

                /*httpDataSourceFactory = new DefaultHttpDataSourceFactory("sundirect Play");
                OfflineLicenseHelper<FrameworkMediaCrypto> offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(drmLicenseUrl, httpDataSourceFactory);
                byte[] keySetId = offlineLicenseHelper.download(httpDataSourceFactory.createDataSource(), downloadUrl);
                if (keySetId != null) {
                    for (int i = 0; i < keySetId.length; i++) {
                        LoggerD.debugDownload("DEBUGDownloadKeys keySetId- " + keySetId[i]);
                    }
                }*/
//                String path = Environment.getExternalStorageDirectory() + File.separator + DownloadUtil.downloadVideosStoragePath + contentDownloadData.cardData._id + File.separator;
                File data = DownloadUtil.getMetaFile(contentId,mContext);
                /*if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        LoggerD.debugDownload("WRITE_EXTERNAL_STORAGE contains");

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        LoggerD.debugDownload("doesn't have WRITE_EXTERNAL_STORAGE");
                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions((Activity) mContext,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                1);

                        // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }*/
                /*LoggerD.debugDownload("DownloadKeys keySetId path- " + data.getAbsolutePath() + " cardData- " + data);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(data));
                objectOutputStream.writeObject(keySetId);
                objectOutputStream.close();*/
                if (mDownloadManagerUtilityWeakReference != null)
                    mDownloadManagerUtilityWeakReference.get().parseMpdForDownload(cardDownloadData);
            } /*catch (UnsupportedDrmException e) {
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.vf_download_error_while_download));
                e.printStackTrace();
            } catch (InterruptedException e) {
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.vf_download_error_while_download));
                e.printStackTrace();
            } catch (IOException e) {
                AlertDialogUtil.showToastNotification(mContext.getString(R.string.vf_download_error_while_download));
                e.printStackTrace();
            } catch (DrmSession.DrmSessionException e) {
                cardDownloadData.zipStatus = CardDownloadData.STATUS_FAILED_TO_AQUIRE_DRM_LICENSE;
                CardDownloadedDataList downloadedDataList = getDownloadData();
                downloadedDataList.mDownloadedList.put(cardDownloadData._id, cardDownloadData);
                SDKUtils.saveObject(downloadedDataList, getApplicationConfig().downloadCardsPath);

                String requiredSpaceStr = mContext.getResources().getString(R.string.vf_download_error_license_retrieval_failure);
                String prefNOSPString = PrefUtils.getInstance().getPrefMessageDRMLicenseFailed();
                if (!TextUtils.isEmpty(prefNOSPString)) {
                    requiredSpaceStr = prefNOSPString;
                }
                AlertDialogUtil.showToastNotification(requiredSpaceStr);
                e.printStackTrace();
*/
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }


    private void showDownloadQualitySelection(final ContentDownloadEvent eventData) {
        if (mContext == null || ((Activity)mContext).isFinishing()
                || !(mDownloadStatusListener != null
                && mDownloadStatusListener.isToShowDownloadButton())) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.download_pop_up, null);

        Button dataSaverButton;
        LinearLayout hdOption;
        LinearLayout dataSaverOption;
        LinearLayout bestOption;
        Button bestButton;
        LinearLayout goodOption;
        Button hdButton;
        Button goodButton;
        hdOption = (LinearLayout) dialogView.findViewById(R.id.hd_option);
        bestOption = (LinearLayout) dialogView.findViewById(R.id.best_option);
        goodOption = (LinearLayout) dialogView.findViewById(R.id.good_option);
        dataSaverOption = (LinearLayout) dialogView.findViewById(R.id.data_saver_option);

        hdButton = (Button) dialogView.findViewById(R.id.hd_button);
        bestButton = (Button) dialogView.findViewById(R.id.best_button);

        goodButton = (Button) dialogView.findViewById(R.id.good_button);

        dataSaverButton = (Button) dialogView.findViewById(R.id.data_saver_button);

        TextView bestOptionText = (TextView) dialogView.findViewById(R.id.best_option_text);
        TextView hdOptionText = (TextView) dialogView.findViewById(R.id.hd_option_text);
        TextView goodOptionText = (TextView) dialogView.findViewById(R.id.good_option_text);
        TextView dataSaverOptionText = (TextView) dialogView.findViewById(R.id.data_saver_text);

        float dataSaverBandwidth = 90f;
        float goodBandwidth = 180f;
        float bestBandwidth = 360f;
        /*if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(eventData.cardData.generalInfo.type)) {
            dataSaverBandwidth = 0.5f;
            goodBandwidth = 1f;
            bestBandwidth = 2f;
        } else if (APIConstants.TYPE_VOD.equalsIgnoreCase(eventData.cardData.generalInfo.type)) {
            dataSaverBandwidth = 20f;
            goodBandwidth = 40f;
            bestBandwidth = 60f;
        }*/
        if (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(eventData.cardData.generalInfo.type)
                && APIConstants.isAltBalajiContent(eventData.cardData)) {
            dataSaverBandwidth = 210f;
            goodBandwidth = 360f;
            bestBandwidth = 480f;
        }
        builder.setView(dialogView);
        CardDataVideos videos = eventData.cardData.videos;
        if(videos == null
                || videos.values == null
                || videos.values.isEmpty()){
//            TODO show download not available message
            return;
        }
        for (CardDataVideosItem videoItem : videos.values) {
            if (APIConstants.VIDEO_TYPE_DOWNLOAD.equalsIgnoreCase(videoItem.type)) {
//                for (RepresentationData representationData : adaptionSet.listRepresentations) {
                float bitrate = Float.parseFloat(videoItem.bitrate);
                float floatBitrate = (bitrate / 8f / 1024f / 1024f / 1024f) * 60f * 60f;//GB/hr
                LoggerD.debugDownload("prepare download tracks: bitrate(GB/hr)- " + floatBitrate  + " original bitrate/sec- " + videoItem.bitrate);
                float mbs = bitrate / 1000000f;
                float kbs = mbs * 1024;
                if (APIConstants.VIDEOQUALTYLOW.equalsIgnoreCase(videoItem.profile)) {
                    dataSaverOption.setVisibility(View.VISIBLE);
                    dataSaverButton.setVisibility(View.VISIBLE);
                    dataSaverButton.setTag(videoItem);
                    dataSaverOptionText.setText(String.format(mContext.getString(R.string.download_option_text), String.format("%.2f", floatBitrate)));
                } else if (APIConstants.VIDEOQUALTYMEDIUM.equalsIgnoreCase(videoItem.profile)) {
                    goodOption.setVisibility(View.VISIBLE);
                    goodButton.setVisibility(View.VISIBLE);
                    goodButton.setTag(videoItem);
                    goodOptionText.setText(String.format(mContext.getString(R.string.download_option_text), String.format("%.2f", floatBitrate)));
                } else if (APIConstants.VIDEOQUALTYHIGH.equalsIgnoreCase(videoItem.profile)) {
                    bestOption.setVisibility(View.VISIBLE);
                    bestButton.setVisibility(View.VISIBLE);
                    bestButton.setTag(videoItem);
                    bestOptionText.setText(String.format(mContext.getString(R.string.download_option_text), String.format("%.2f", floatBitrate)));
                } else if (kbs >= bestBandwidth && floatBitrate <= 4) {
                    hdOption.setVisibility(View.VISIBLE);
                    hdButton.setVisibility(View.VISIBLE);
                    hdButton.setTag(videoItem);
                    hdOptionText.setText(String.format(mContext.getString(R.string.download_option_text), String.format("%.2f", floatBitrate)));
                }
            }
//            }
        }

        final AlertDialog dialog = builder.create();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (mDownloadStatusListener != null) {
                    mDownloadStatusListener.onDownloadCancelled();
                }
            }
        });

        hdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardDataVideosItem downloadItem = (CardDataVideosItem) view.getTag();
                chooseDownloadAudioLink(downloadItem, eventData);
                dialog.dismiss();

            }
        });
        bestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardDataVideosItem downloadItem = (CardDataVideosItem) view.getTag();
                chooseDownloadAudioLink(downloadItem, eventData);
                dialog.dismiss();

            }
        });
        goodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardDataVideosItem downloadItem = (CardDataVideosItem) view.getTag();
                chooseDownloadAudioLink(downloadItem, eventData);
                dialog.dismiss();

            }
        });
        dataSaverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardDataVideosItem downloadItem = (CardDataVideosItem) view.getTag();
                chooseDownloadAudioLink(downloadItem, eventData);
                dialog.dismiss();
            }
        });
    }

    private void chooseDownloadAudioLink(CardDataVideosItem videosItem, ContentDownloadEvent eventData) {
        LoggerD.debugDownload("videosItem- " + videosItem);
        String downloadType = APIConstants.VIDEOQUALTYSD;
        if (mDownloadStatusListener != null) {
            mDownloadStatusListener.onDownloadStarted();
        }
//            Since these are small videos assume max 30
        //and make the Gbph to mbpm
        float floatBitrate = (Integer.parseInt(videosItem.bitrate) / 8f / 1024f / 1024f / 1024f) * 60f * 60f;//GB/hr
//            Since these are small videos assume max 30
        float contentDuration = Util.calculateDurationInSeconds(eventData.cardData.content.duration) / 60;// duration in mints
//            contentDuration = Integer.parseInt(event.cardData.content.duration);
        //and make the Gbph to mbpm
        double requiredSpaceFactor = (floatBitrate / 60f) * 1024f * contentDuration;//MB/mnt
        String formattedSize = String.format("%.2f", requiredSpaceFactor) + " MB";
        String errorMessage = mContext.getString(R.string.play_download_insufficent_memory_vod, formattedSize);
        if(APIConstants.TYPE_MOVIE.equalsIgnoreCase(eventData.cardData.generalInfo.type)){
            contentDuration = Util.calculateDurationInSeconds(eventData.cardData.content.duration) / 60f / 60f; //duration in hrs

            requiredSpaceFactor = floatBitrate * contentDuration; //GB/hrs
            formattedSize = String.format("%.2f",requiredSpaceFactor) +" GB";
            LoggerD.debugDownload("formattedSize- " + formattedSize);
            errorMessage = mContext.getString(R.string.play_download_insufficent_memory_hd, formattedSize);
            downloadType = APIConstants.VIDEOQUALTYHD;
        }
        if (!Util.hasSpaceAvailabeToDownload(downloadType, requiredSpaceFactor, mContext)) {
            AlertDialogUtil.showToastNotification(errorMessage);
            return;
        }
        downloadFile(videosItem, eventData);
    }

    private String getDownloadLink(CardDataVideos videos) {
        if(videos == null
                || videos.values == null
                || videos.values.isEmpty()){
            return null;
        }
        for(CardDataVideosItem videoItem: videos.values){
            if(APIConstants.VIDEO_TYPE_DOWNLOAD.equalsIgnoreCase(videoItem.type)){
                return videoItem.link;
            }
        }
        return null;
    }

    public void addListener(UnzipProcessListener unzipProcessListener){
        if (unzipProcessListener == null) {
            return;
        }
        if (!unzipListners.contains(unzipProcessListener))
            unzipListners.add(unzipProcessListener);
    }


    public synchronized void removeListener(UnzipProcessListener unzipProcessListener){
        if (unzipProcessListener == null) {
            return;
        }
        unzipListners.remove(unzipProcessListener);
    }


    public interface OnDeletionLisnter {
        void onDelete();
    }

    public static class DeleteThread extends Thread {
        private OnDeletionLisnter onDeletionLisnter;
        private final CardData cardData;

        public DeleteThread(CardData cardData, OnDeletionLisnter onDeletionLisnter) {
            this.cardData = cardData;
            this.onDeletionLisnter = onDeletionLisnter;
        }

        @Override
        public void run() {
            super.run();
            final String downloadFilelocation = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + cardData._id;
            LoggerD.debugDownload("deletion started for - " + downloadFilelocation);
            try {
                DownloadUtil.deleteRecursive(new File(downloadFilelocation), new OnDeletionLisnter() {
                    @Override
                    public void onDelete() {
                        LoggerD.debugDownload("deletion compeleted for - " + downloadFilelocation);
                        if (onDeletionLisnter != null) onDeletionLisnter.onDelete();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                LoggerD.debugDownload("failed deletion for - " + downloadFilelocation);
            }
        }
    }
}
