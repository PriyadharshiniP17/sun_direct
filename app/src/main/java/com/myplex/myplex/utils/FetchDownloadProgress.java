package com.myplex.myplex.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.SeasonData;
import com.myplex.player_config.DownloadManagerMaintainer;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.myplex.myplex.ApplicationController.getApplicationConfig;

public class FetchDownloadProgress {
    public static final String TAG = "FetchDownloadProgress";
    private static final long POLL_DELAY = 5 * 1000;
    private static final Object lock = new Object();
    private static final long POLL_DELAY_EACH_ITEM = 2000;
    private Handler mPollingHandlerMultiple;
    private HandlerThread mHandlerThread;
    private Context mContext;
    private CardData mCardData;
    private List<CardData> mCardDatalist;
    private DownloadProgressStatus mListener;
    private DownloadManager mDownloadManager;
    private CardDownloadData mDownloadData;
    private boolean mStopPolling = false;
    private final List<DownloadProgressStatus> listeners = new ArrayList<>();

    public void addProgressListener(DownloadProgressStatus listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
        LoggerD.debugDownload("DebugDownloadAdapterPerformance: added the lister for " + listener + " count- " + listeners.size());
    }

    public void removeProgressListener(DownloadProgressStatus listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
        LoggerD.debugDownload("DebugDownloadAdapterPerformance: added the lister for " + listener + " count- " + listeners.size());
    }

    private static volatile FetchDownloadProgress fetchDownloadProgress = null;

    public static FetchDownloadProgress getInstance(Context context) {
        if (fetchDownloadProgress == null) {
            synchronized (lock) {
                if (fetchDownloadProgress == null) {
                    fetchDownloadProgress = new FetchDownloadProgress(context);
                }
            }
        }
        return fetchDownloadProgress;
    }

    private FetchDownloadProgress(Context cxt) {
        mContext = cxt;
        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        mStopPolling = false;
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mPollingHandlerMultiple = new MultiHandlerThread(mHandlerThread.getLooper());
        startPolling();
    }


    public interface DownloadProgressStatus {
        void DownloadProgress(CardData cardData, CardDownloadData downloadData);
    }

    public void setDownloadProgressListener(DownloadProgressStatus listener) {
        this.mListener = listener;
    }

    public void stopPolling() {
        mStopPolling = true;
    }

    public void startPolling(CardData data) {
        CardDownloadedDataList downloadData = ApplicationController.getDownloadData();

        if (downloadData == null) {
            return;
        }
        //Log.d(TAG, "Download startPolling ");
        mStopPolling = false;
        mCardData = data;
        //Log.d(TAG, "size of list" + downloadData.mDownloadedList.size());
        mDownloadData = downloadData.mDownloadedList.get(mCardData._id);
        mPollingHandler.sendEmptyMessage(CONTINUE_POLLING);
    }

    public void startPolling() {
        CardDownloadedDataList downloadData = ApplicationController.getDownloadData();
        if (downloadData == null) {
            return;
        }
        if (!mStopPolling) {
            mStopPolling = false;
        }
        mPollingHandlerMultiple.sendEmptyMessage(CONTINUE_POLLING);

    }

    public void startPolling(List<CardData> data) {
        CardDownloadedDataList downloadData = ApplicationController.getDownloadData();
        if (downloadData == null) {
            return;
        }
        //Log.d(TAG, "Download startPolling ");
        mStopPolling = false;
        mCardDatalist = data;
        mPollingHandlerMultiple.sendEmptyMessage(CONTINUE_POLLING);
    }

    private static final int CONTINUE_POLLING = -1;
    private static final int POLLING_ABORT = 0;

    private final Handler mPollingHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (mStopPolling) {
                return;
            }
            switch (msg.what) {
                case CONTINUE_POLLING: {
                    boolean continuePolling = false;
                    try {
                        if (mDownloadData == null || mDownloadData.mDownloadId == -1) {
                            return;
                        }
                        //Log.d(TAG, "handleMessage2");
                        DownloadManager.Query q = new DownloadManager.Query();
                        //Log.d(TAG, "Download information for " + mDownloadData.mDownloadId);
                        q.setFilterById(mDownloadData.mDownloadId);
                        Cursor cursor = mDownloadManager.query(q);
                        if (cursor == null) {
                            return;
                        }
                        cursor.moveToFirst();
                        long bytes_downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        long bytes_total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            mDownloadData.mCompleted = true;
                            mDownloadData.mPercentage = 100;
//						for Analytics //Util.java startDownload() method the Analytics.downloadStartTime is initialized
                            String key = mCardData.generalInfo._id + APIConstants.UNDERSCORE + APIConstants.downLoadStartTime;
                            long startTime = PrefUtils.getInstance().getLong(key);
                            long timetakenForDownload = System.currentTimeMillis() - startTime;
                            long timeInMinutes = TimeUnit.MILLISECONDS.toMinutes(timetakenForDownload);
                            long mb = 1024L * 1024L;
                            long bytesinMB = 0;
                            if (bytes_total != 0) {
                                bytesinMB = (bytes_total / mb);
                            }
                            if (mCardData != null
                                    && mCardData._id != null
                                    && mCardData.generalInfo != null
                                    && mCardData.generalInfo.type != null
                                    && mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_VOD)) {
                                mDownloadData.mDownloadedBytes = (double) bytes_downloaded / mb;
                                mDownloadData.mDownloadTotalSize = bytesinMB;
//							Util.saveExternalDownloadInfo(mCardData, mDownloadData);
                            }
                            //Analytics.downloadStartTime = 0;//setting the starting time to zero

                            PrefUtils.getInstance().setLong(APIConstants.downLoadStartTime, 0);
                        } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                            mDownloadData.mCompleted = true;
                            mDownloadData.mPercentage = 0;
                            String reason1 = getDownloadFailedReason(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)));
                            LoggerD.debugLog("reason1- " + reason1);
                            if (DownloadManager.ERROR_INSUFFICIENT_SPACE == cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)))
                                mDownloadData.zipStatus = CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE;
//						Util.saveExternalDownloadInfo(mCardData, mDownloadData);
                        } else {
                            final int dl_progress = (int) ((bytes_downloaded * 100) / bytes_total);
                            mDownloadData.mCompleted = false;
                            mDownloadData.mPercentage = dl_progress;
                            long mb = 1024L * 1024L;
                            mDownloadData.mDownloadedBytes = (double) bytes_downloaded / mb;
                            mDownloadData.mDownloadTotalSize = (double) bytes_total / mb;
                            continuePolling = true;
                        }
                        //Log.d(TAG, "Download information for " + mDownloadData.mDownloadId + " isCompleted = " + mDownloadData.mCompleted + " percentage = " + mDownloadData.mPercentage);
                        cursor.close();
                        if (mListener != null && !mStopPolling) {
                            mListener.DownloadProgress(mCardData, mDownloadData);
                        }
                    } catch (Exception e) {
                        //Log.d(TAG, "handleMessage4");
                        mDownloadData.mCompleted = true;
                        mDownloadData.mPercentage = 0;
                        if (mListener != null && !mStopPolling) {
                            mListener.DownloadProgress(mCardData, mDownloadData);
                        }
                        // TODO: handle exception
                    }
                    if (continuePolling) {
                        sendEmptyMessageDelayed(CONTINUE_POLLING, POLL_DELAY);
                    }
                    break;
                }
                case POLLING_ABORT: {
                    break;
                }
            }
        }
    };


    private class MultiHandlerThread extends Handler{

        private Object dlock = new Object();

        private MultiHandlerThread(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            if (mStopPolling) {
                return;
            }
            switch (msg.what) {
                case CONTINUE_POLLING: {
                    CardDownloadedDataList downloadData = ApplicationController.getDownloadData();
                    boolean continuePolling = false;
                    try {
                        //Log.d(TAG, "size of list" + downloadData.mDownloadedList.size());
                        for (String key : downloadData.mDownloadedList.keySet()) {
                            try {
                                boolean isPending = false;
                                CardDownloadData availableDownloadData = downloadData.mDownloadedList.get(key);
                                String downloadKey = null;
                                if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList == null) {
                                        /*downloadKey = String.valueOf(availableDownloadData.mVideoDownloadId);*/
                                        isPending = informDownloadProgress(availableDownloadData);
                                } else if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList != null) {
                                    for (CardDownloadData episode : availableDownloadData.tvEpisodesList) {
                                            isPending = informDownloadProgress(episode);
                                    }
                                } else if (availableDownloadData.tvSeasonsList != null) {
                                    for (SeasonData seasonData : availableDownloadData.tvSeasonsList) {
                                        for (CardDownloadData episode : seasonData.tvEpisodesList) {
                                                isPending = informDownloadProgress(episode);
                                        }
                                    }
                                }
                                if (isPending && !continuePolling) {
                                    continuePolling = true;
                                    try {
                                        synchronized (dlock) {
                                            LoggerD.debugDownload("wait for 2 secs");
                                            Thread.sleep(POLL_DELAY_EACH_ITEM);
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        //Log.d(TAG, "handleMessage4");
                        continuePolling = true;
                        // TODO: handle exception
                    }
                    if (continuePolling) {
                        sendEmptyMessageDelayed(CONTINUE_POLLING, POLL_DELAY);
                    }
                    break;
                }
                case POLLING_ABORT: {
                    break;
                }
            }
        }
    };

    private boolean informDownloadProgress(CardDownloadData downloadData){
        try {
            boolean continuePolling = false;
            int dl_progress= (int) DownloadManagerMaintainer.getInstance().getDownloadPercentage(downloadData._id);
            downloadData.mPercentage = dl_progress;
            int status=getDownloadStatusFromSDK(downloadData._id);
            if(status ==DownloadManagerMaintainer.STATE_COMPLETED){
                if (downloadData != null) {
                    if (!downloadData.mCompleted) {
                        downloadData.mCompleted = true;
                        downloadData.mPercentage = 100;
                        SDKUtils.saveObject(ApplicationController.getDownloadData(), getApplicationConfig().downloadCardsPath);
                    }
                }
            }else if(status==DownloadManagerMaintainer.STATE_FAILED){
                downloadData.mCompleted = true;
                downloadData.mPercentage = 0;
                /*String reason1 = getDownloadFailedReason(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)));
                LoggerD.debugDownload("reason1- " + reason1);
                if (DownloadManager.ERROR_INSUFFICIENT_SPACE == cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)))
                    downloadData.zipStatus = CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE;*/
            }
            else {
                if (downloadData.mPercentage == 100) {
                    LoggerD.debugDownload(downloadData + " \n percentage is 100 before success full status");
                }
                continuePolling = true;
            }

            LoggerD.debugDownload("downloadData Download information for title- " + downloadData.title
                    + " \n isCompleted = " + downloadData.mCompleted
                    + " \n percentage = " + downloadData.mPercentage);
            notifyUpdate(downloadData);
            return continuePolling;

        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }

    private boolean informDownloadProgress(CardDownloadData downloadData, String downloadKey) {
        Cursor cursor = null;
        try {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(Long.parseLong(downloadKey));
            cursor = mDownloadManager.query(q);
            if (cursor == null) {
                return true;
            }
            boolean continuePolling = false;
            cursor.moveToFirst();
            long bytes_downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long bytes_total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            final int dl_progress = (int) ((bytes_downloaded * 100) / bytes_total);
            downloadData.mPercentage = dl_progress;
            long mb = 1024L * 1024L;
            downloadData.mDownloadedBytes = (double) bytes_downloaded / mb;
            downloadData.mDownloadTotalSize = (double) bytes_total / mb;
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                if (downloadData != null) {
                    mb = 1024L * 1024L;
                    downloadData.mDownloadedBytes = (double) bytes_downloaded / mb;
                    downloadData.mDownloadTotalSize = (double) bytes_total / mb;
                    if (!downloadData.mCompleted) {
                        downloadData.mCompleted = true;
                        downloadData.mPercentage = 100;
                        SDKUtils.saveObject(ApplicationController.getDownloadData(), getApplicationConfig().downloadCardsPath);
                    }
                }
            } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                downloadData.mCompleted = true;
                downloadData.mPercentage = 0;
                String reason1 = getDownloadFailedReason(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)));
                LoggerD.debugDownload("reason1- " + reason1);
                if (DownloadManager.ERROR_INSUFFICIENT_SPACE == cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)))
                    downloadData.zipStatus = CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE;
            } else {
                if (downloadData.mPercentage == 100) {
                    LoggerD.debugDownload(downloadData + " \n percentage is 100 before success full status");
                }
                continuePolling = true;
            }
            LoggerD.debugDownload("downloadData Download information for title- " + downloadData.title
                    + " \n isCompleted = " + downloadData.mCompleted
                    + " \n downloadData.mDownloadedBytes = " + downloadData.mDownloadedBytes
                    + " \n downloadData.mDownloadTotalSize = " + downloadData.mDownloadTotalSize
                    + " \n percentage = " + downloadData.mPercentage);
            cursor.close();
            notifyUpdate(downloadData);
            return continuePolling;
        } catch (Exception e) {
            cursor.close();
            e.printStackTrace();
            return true;
        }
    }

    private void notifyUpdate(final CardDownloadData downloadData) {
        for (DownloadProgressStatus listener : listeners) {
            if (listener != null) {
                listener.DownloadProgress(null, downloadData);
            }
        }
    }

    public String getDownloadFailedReason(int reason) {

        String failedReason = "";

        switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME:
                failedReason = "ERROR_CANNOT_RESUME";
                break;
            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                failedReason = "ERROR_DEVICE_NOT_FOUND";
                break;
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                failedReason = "ERROR_FILE_ALREADY_EXISTS";
                break;
            case DownloadManager.ERROR_FILE_ERROR:
                failedReason = "ERROR_FILE_ERROR";
                break;
            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                failedReason = "ERROR_HTTP_DATA_ERROR";
                break;
            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                failedReason = "ERROR_INSUFFICIENT_SPACE";
                break;
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                failedReason = "ERROR_TOO_MANY_REDIRECTS";
                break;
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                break;
            case DownloadManager.ERROR_UNKNOWN:
                failedReason = "ERROR_UNKNOWN";
                break;
        }

        return failedReason;

    }

    public int getDownloadStatusFromSDK(String contentId){
        return DownloadManagerMaintainer.getInstance().getDownloadStatus(contentId);
    }

    public int getDownloadStatus(long download_id) {

        Cursor cursor = null;
        try {
            //Log.d(TAG, "handleMessage2");
            DownloadManager.Query q = new DownloadManager.Query();
            //Log.d(TAG, "Download information for " + download_id);
            q.setFilterById(download_id);
            cursor = mDownloadManager.query(q);
            if (cursor == null) {
                return 0;
            }

            if (cursor.moveToFirst()) {

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    return DownloadManager.STATUS_SUCCESSFUL;

                } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {

                    int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int reason = cursor.getInt(columnReason);
                    return reason;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    public long getSpaceRequired(long download_id) {
        Cursor cursor = null;
        try {
            //Log.d(TAG, "handleMessage2");
            DownloadManager.Query q = new DownloadManager.Query();
            //Log.d(TAG, "Download information for " + download_id);
            q.setFilterById(download_id);
            cursor = mDownloadManager.query(q);
            if (cursor == null) {
                return 0;
            }

            if (cursor.moveToFirst()) {

                long bytes_downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                long bytes_total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    if (cursor != null)
                        cursor.close();
                    return 0;
                } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                    if (cursor != null)
                        cursor.close();
                    return 0;
                }

                long bytesToBeDownload = bytes_total - bytes_downloaded;
                if (cursor != null)
                    cursor.close();
                return bytesToBeDownload;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (cursor != null)
                cursor.close();
            return 0;
        }
        if (cursor != null)
            cursor.close();
        return 0;
    }


    public int getDownloadPercentage(long download_id) {

        int dl_percentage = -1;

        try {
            DownloadManager.Query q = new DownloadManager.Query();
            //Log.d(TAG, "Download information for " + download_id);
            q.setFilterById(download_id);
            Cursor cursor = mDownloadManager.query(q);
            if (cursor == null) {
                return dl_percentage;
            }
            cursor.moveToFirst();
            long bytes_downloaded = cursor
                    .getLong(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

            long bytes_total = cursor.getLong(cursor
                    .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

            if (cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                dl_percentage = 100;
            } else if (cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                dl_percentage = 0;
            }

            dl_percentage = (int) ((bytes_downloaded * 100) / bytes_total);
            cursor.close();
            return dl_percentage;

        } catch (Throwable e) {
            //Log.d(TAG, "handleMessage4" + e);

        }

        return dl_percentage;
    }

}
