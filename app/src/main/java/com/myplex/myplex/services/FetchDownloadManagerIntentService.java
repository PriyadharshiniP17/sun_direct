package com.myplex.myplex.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.myplex.api.APIConstants;
import com.myplex.downloads_manager.MyplexDownloadManger;
import com.myplex.model.CardData;
import com.myplex.model.DownloadContentData;
import com.myplex.player_config.DownloadManagerMaintainer;
import com.myplex.player_sdk.download.DownloadConfig;
import com.myplex.player_sdk.download.DownloadManagerCallbackListener;
import com.myplex.util.AlertDialogUtil;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.utils.DownloadUtil;
import java.util.HashMap;

public class FetchDownloadManagerIntentService extends Service {

    private ServiceHandler serviceHandler;
    Looper serviceLooper;
    private int startId;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FetchDownloadManagerIntentService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("FETCH DOWNLOAD","IN ON CREATE COMMAND");
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        if(ApplicationController.shouldUseExoDownloadManager){
            MyplexDownloadManger.getInstance().addDownloadManagerCallbackListener(callbackListener);
        }
        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(final Message msg) {
            // Normally we would do some work here, like download a file.
            Log.e("FETCH DOWNLOAD","IN MESSAGE HANDLER");

                /*if(MyplexDownloadManger.getInstance()!=null&&
                        MyplexDownloadManger.getInstance().hasActiveDownloads()){
                    Log.e("FETCH EXO-CHECK","HAS ACTIVE DOWNLOADS");
                    MyplexDownloadManger.getInstance().addDownloadManagerCallbackListener(callbackListener);
                }else{*/
                    Log.e("FETCH EXO-CHECK","IN KILLING");
                    stopSelf(msg.arg1);
                //}
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job

        }
    }
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.e("FETCH DOWNLOAD","IN ON START COMMAND");
        this.startId = startId;
        startDownload(intent);
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);
        return START_STICKY;
    }
    DownloadContentData contentData;

    private void startDownload(@Nullable final Intent intent) {
        if(intent == null){
            return;
        }
        final Context mContext = getApplicationContext();
        contentData = (DownloadContentData) intent.getSerializableExtra(APIConstants.DOWNLOAD_CONTENT_DATA);
        if(contentData == null)
            return;

        if(ApplicationController.shouldUseExoDownloadManager){
            startDownloadUsingExoDownloader(contentData,
                    contentData.aUrl,
                    contentData.aMovieData,
                    this,
                    contentData.drmLicenseUrl,
                    contentData.drmToken,
                    contentData.userAgent,
                    contentData.size,
                    contentData.shouldDownloadOnWifi,
                    contentData.subtitleLink);
            return;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {
        Log.e("FETCH DOWNLOAD","IN DESTROY");
        Log.d("FetchDownloadService","Destroyed");
        super.onDestroy();

    }

    private void startDownloadUsingExoDownloader(DownloadContentData contentData,
                                                 String aUrl,
                                                 CardData aMovieData, Context mContext,
                                                 String drmLicenseUrl,
                                                 String drmToken,
                                                 String userAgent,
                                                 String size,
                                                 boolean shouldDownloadOnWifi,
                                                 String subtitleLink) {

        //Adding subtitle to FetchDownLoad
        // as ExoPlayer does not download side-loaded subtitles
        DownloadConfig.QUALITY_TYPE qualityType = getQualityType(size)!=null?getQualityType(size): DownloadConfig.QUALITY_TYPE.BEST;

        boolean isDrmContent=false;
        DownloadConfig.DOWNLOAD_CONTENT_TYPE format;
        if (contentData.format.equalsIgnoreCase("dash")){
            format=DownloadConfig.DOWNLOAD_CONTENT_TYPE.CONTENT_TYPE_DASH;
            isDrmContent=true;
        }else {
            format=DownloadConfig.DOWNLOAD_CONTENT_TYPE.CONTENT_TYPE_HLS;
        }

        HashMap<String,String> keyProperties = new HashMap<>();
        if (!TextUtils.isEmpty(drmToken)) {
            keyProperties.put("Authorization",drmToken);
        }
        DownloadConfig.DownloadConfigBuilder builder =
                new DownloadConfig.DownloadConfigBuilder()
                        .set_id(Integer.parseInt(aMovieData._id))
                        .setContentType(format)
                        .setShouldShowDownloadNotification(false)
                        .setContext(mContext)
                        .setDownloadUrl(aUrl)
                        .setDRMContent(isDrmContent)
                        .setDrmLicenseUrl(drmLicenseUrl)
                        .setKeyRequestProperties(keyProperties)
                        .setShouldDownloadOnWifiOnly(shouldDownloadOnWifi)
                        .setDownloadManagerCallbackListener(new DownloadManagerCallbackListener() {
                            @Override
                            public void onDownloadAdded(String s) {
                                addDownloadToStorage(mContext,contentData,aMovieData,format);
                            }

                            @Override
                            public void onDownloadStopped(String s) {
                                /*if(DownloadManagerMaintainer.getInstance().getDownloadStatus(aMovieData._id)
                                        ==DownloadManagerMaintainer.STATE_STOPPED){
                                    DownloadManagerMaintainer.getInstance().restartDownload(aMovieData._id);
                                }*/
                            }

                            @Override
                            public void onDownloadRestarted(String s) {

                            }

                            @Override
                            public void onDownloadAdditionFailed(String s) {

                            }

                            @Override
                            public void onDownloadRestartFailed(String s) {

                            }

                            @Override
                            public void onDownloadCompleted(String s) {
                                stopSelf(startId);
                            }

                            @Override
                            public void onDownloadQueued(String s) {

                            }

                            @Override
                            public void onDownloadDeleted(String s) {

                            }

                            @Override
                            public void onDownloadTrackSelectionFailed(String s) {
                                AlertDialogUtil.showToastNotification("No Download tracks available for selected quality");
                            }

                            @Override
                            public void onDownloadStartFailed(String s) {
                                System.out.println("Download failed due to :: "+s);
                                AlertDialogUtil.showToastNotification("Download Failed due to::"+s);
                            }
                        })
                        .setQualityType(qualityType)
                        .setContentTitle(aMovieData.generalInfo.title)
                        .setUserAgent(userAgent);

        DownloadConfig config = builder.build();
        if (config != null)
            if (format == DownloadConfig.DOWNLOAD_CONTENT_TYPE.CONTENT_TYPE_DASH) {
                DownloadManagerMaintainer.getInstance().startDownload(config);
            } else {
                DownloadManagerMaintainer.getInstance().startNonDrmDownload(config);
            }
    }

    private void addDownloadToStorage(Context mContext, DownloadContentData downloadContentData,
                                      CardData aMovieData,DownloadConfig.DOWNLOAD_CONTENT_TYPE contentType) {
        DownloadUtil.saveDownloadData(mContext,aMovieData,downloadContentData.fileName,
                -1,-1,-1,-1,
                "",
                downloadContentData.audioFileName,
                downloadContentData.size,
                null,
                true,contentType);
    }


    private static DownloadConfig.QUALITY_TYPE getQualityType(String size) {
        DownloadConfig.QUALITY_TYPE qualityType;
        switch (size){
            case "hd":
                qualityType = DownloadConfig.QUALITY_TYPE.HD;
                break;
            case "datasaver":
                qualityType = DownloadConfig.QUALITY_TYPE.DATA_SAVER;
                break;
            case "good":
                qualityType = DownloadConfig.QUALITY_TYPE.GOOD;
                break;
            case "best":
                qualityType = DownloadConfig.QUALITY_TYPE.BEST;
                break;
            default:
                qualityType = DownloadConfig.QUALITY_TYPE.GOOD;
        }
        return qualityType;
    }

    private DownloadManagerCallbackListener callbackListener = new DownloadManagerCallbackListener() {
        @Override
        public void onDownloadAdded(String s) {

        }

        @Override
        public void onDownloadStopped(String s) {

        }

        @Override
        public void onDownloadRestarted(String s) {

        }

        @Override
        public void onDownloadAdditionFailed(String s) {

        }

        @Override
        public void onDownloadRestartFailed(String s) {

        }

        @Override
        public void onDownloadCompleted(String s) {
            stopSelf(startId);
        }

        @Override
        public void onDownloadQueued(String s) {

        }

        @Override
        public void onDownloadDeleted(String s) {

        }

        @Override
        public void onDownloadTrackSelectionFailed(String s) {
            AlertDialogUtil.showToastNotification("No Download tracks available for selected quality");
        }

        @Override
        public void onDownloadStartFailed(String s) {
            AlertDialogUtil.showToastNotification("Download Failed");
        }
    };


}
