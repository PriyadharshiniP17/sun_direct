package com.myplex.myplex.download;

import android.content.Context;
import android.text.TextUtils;

import com.github.pedrovgs.LoggerD;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.myplex.myplex.ApplicationController.getDownloadData;

public class Decompress implements Executor {
    private static final int BUFFER_SIZE = 32 * 1024;
    private static final Integer STATUS_FAILED = 200;
    private static final Integer STATUS_SUCCESS = 400;
    private static Decompress mDecompress;
    private final Context mContext;
//    private static final int BUFFER_SIZE = 8192;

    private byte[] buffer;
    private float per = 0;
    private float totalFiles;
    private List<OnExtractCompletionListener> mListeners = new ArrayList<>();

    final Queue<Runnable> tasks = new ArrayDeque();

    public static final Executor DUAL_THREAD_EXECUTOR = Executors.newFixedThreadPool(3);

    Runnable active;
    private CopyOnWriteArrayList<String> taskList = new CopyOnWriteArrayList<>();

    public synchronized void execute(final Runnable r) {
        tasks.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (active == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            DUAL_THREAD_EXECUTOR.execute(active);
        }
    }

    private Decompress(Context mContext){
        buffer = new byte[BUFFER_SIZE];
        this.mContext = mContext;
    }

    public void unzip(final DecompressTaskData taskData, final OnExtractCompletionListener lister) {
        if (taskData == null || taskData.cardDownloadData == null) {
            return;
        }
        if (checkTaskIfAlreadyInProgress(taskData.cardDownloadData)) {
            return;
        }
        addToTaskList(taskData.cardDownloadData);
        addListener(lister);
        execute(new Runnable() {
            @Override
            public void run() {
                doInBackground(taskData);
            }
        });
    }

    private boolean checkTaskIfAlreadyInProgress(CardDownloadData cardDownloadData) {
        if (cardDownloadData == null || cardDownloadData.downloadKey == null)
            return false;
        return taskList.contains(cardDownloadData.downloadKey);
    }

    public static synchronized Decompress getInstance(Context mContext) {
        if (mDecompress == null) {
            mDecompress = new Decompress(mContext);
        }
        return mDecompress;
    }

    public void clearTasks() {
        taskList.clear();
    }


    public interface OnExtractCompletionListener {
        void onComplete(CardDownloadData cardDownloadData);
        void onProgress(CardDownloadData cardDownloadData, float progress);
        void onFailed(CardDownloadData cardDownloadData);
    }

    protected void onProgressUpdate(CardDownloadData cardDownloadData) {
//Since it's an inner class, Bar should be able to be called directly
        LoggerD.debugDownload("Decompress totalFiles- " + totalFiles + " completed- " + per);
        LoggerD.debugDownload("Decompress percentage- " + String.valueOf((per / totalFiles) * 100));
        notifyPorgress(cardDownloadData, (per / totalFiles) * 100);
    }

    protected void doInBackground(DecompressTaskData taskData) {
        if(taskData == null){
            notifyFailed(null);
        }
        per = 0;
        ZipFile zip = null;
        FileInputStream fin = null;
        ZipInputStream zin = null;
        try {
            String zipFilePath = taskData.zipFilePath;
            double requiredSpace = getRequiredSpace(zipFilePath);
            int requiredSpaceInMb = (int) (requiredSpace/1024/1024);
            LoggerD.debugDownload("required space to download- " + requiredSpaceInMb);
            if (!isSpaceAvailable(requiredSpace)) {
                taskData.cardDownloadData.zipStatus = CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE;
                notifyFailed(taskData.cardDownloadData);
                return;
            }
            String destLocationPath = taskData.destinationFilePath;
            final CardDownloadData downloadData = taskData.cardDownloadData;
            _dirChecker(destLocationPath+"");

            zip = new ZipFile(zipFilePath);
            totalFiles = zip.size();

            fin = new FileInputStream(zipFilePath);
            zin = new ZipInputStream(fin);
            File outputDir = new File(destLocationPath);
            File tmp = null;
            ZipEntry ze = null;
            CardDownloadedDataList downloadedDataList = getDownloadData();
            downloadData.zipStatus = CardDownloadData.STATUS_FILE_UNZIPPING;
            LoggerD.debugDownload("Updating carddownload data as zip status unzipping for - " + downloadData.title);
            for (; ((ze = zin.getNextEntry()) != null); ) {
                String entryName = ze.getName();
                LoggerD.debugDownload("Decompress more " + entryName);
/*
                File file = new File(destLocationPath, entryName);
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    LoggerD.debugDownload("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                    return STATUS_FAILED;
                }
*/
                if (ze.isDirectory())
                    continue;
                if (ze.isDirectory()) {
                    _dirChecker(destLocationPath+entryName);
                    LoggerD.debugDownload("Decompress created the directory " + entryName);
                } else {
                    tmp = File.createTempFile("decomp", ".tmp", outputDir);
                    BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(tmp));
                    copyStream(zin, fout, taskData);
                    zin.closeEntry();
                    fout.close();
                    String fileName = entryName.substring(entryName.lastIndexOf(File.separator));
                    tmp.renameTo(new File(destLocationPath + fileName.toLowerCase()));
                    per++;
                    onProgressUpdate(taskData.cardDownloadData);
                    LoggerD.debugDownload("Unzipping entryName- " + entryName + " fileName- " + fileName);
                }
            }
            Util.closeQuietly(zin);
            Util.closeQuietly(fin);
            Util.closeQuietly(zip);
            notifyCompletion(taskData.cardDownloadData);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Util.closeQuietly(zin);
            Util.closeQuietly(fin);
            Util.closeQuietly(zip);
            LoggerD.debugDownload("Decompress unzip" + e);
            if (e != null && e instanceof FileNotFoundException) {
                try {
                    if(handleFileName(taskData)) return;
                } catch (Throwable ex) {
                    //Crashlytics.logException(ex);
                    ex.printStackTrace();
                    notifyFailed(taskData.cardDownloadData);
                }
            }
        }
        notifyFailed(taskData.cardDownloadData);
    }

    private boolean handleFileName(DecompressTaskData taskData) {
            File file = new File(taskData.zipFilePath);
            if (file != null && !file.exists()) {
                final String destinationFilePath = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + taskData.cardDownloadData._id + File.separator;
                File directory = new File(destinationFilePath);
                if (directory == null || !directory.exists()) {
                    return false;
                }
                for (File f : directory.listFiles()) {
                    if (f != null && f.isFile()) {
                        String name = f.getName();
                        if (!TextUtils.isEmpty(name)) {
                            if (name.startsWith(taskData.cardDownloadData._id)
                                    && name.endsWith(".zip")) {
                                File from = new File(destinationFilePath, name);
                                File to = new File(destinationFilePath, taskData.cardDownloadData.fileName);
                                from.renameTo(to);
                                clearTasks();
                                unzip(taskData, null);
                                return true;
                            }
                        }
                    }
                    // Do your stuff
                }
            }
            return false;
    }

    private boolean isSpaceAvailable(double requiredSpace) {
        try {
            double availableSpaceOnDiskMB = Util.getAvailebleFreeSpaceInMBOnDisk(ApplicationController.getAppContext());
            LoggerD.debugDownload("required space in mb's " + (requiredSpace / 1024 / 1024 ) + " MB " + "" +
                    " AvailableFreeSpaceInMBOnDisk- " + (availableSpaceOnDiskMB / 1024 / 1024 ) + " MB");
            if (requiredSpace < availableSpaceOnDiskMB) {
                LoggerD.debugDownload("space is available");
                return true;
            }
        } catch (Exception e) {
            LoggerD.debugDownload("File not found : " + e.getMessage() + e);
            return false;
        }
        return false;
    }

    private double getRequiredSpace(String zipFilePath) {
        try {
            File file = new File(zipFilePath);
            double lengthIn = file.length();
            LoggerD.debugLog("File Path : " + file.getPath() + ", File size : " + (lengthIn / 1024 / 1024) + " MB");
            return lengthIn;
        } catch (Exception e) {
            LoggerD.debugLog("File not found : " + e.getMessage() + e);
            return 0;
        }
    }
    private void addToTaskList(CardDownloadData downloadData) {
        if (downloadData == null) return;
        taskList.add(downloadData.downloadKey);
    }


    private void _dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            LoggerD.debugDownload("Decompress created the directory " + f.getAbsolutePath() + " make directories " + f.mkdirs());
        }
    }

    /**
     * Copy from one stream to another.  Throws IOException in the event of error
     * (for example, SD card is full)
     *  @param is Input stream.
     * @param os Output stream.
     * @param taskData
     */
    public void copyStream(InputStream is, OutputStream os, DecompressTaskData taskData) throws IOException {
        try {
            for (; ; ) {
                int count = is.read(buffer, 0, BUFFER_SIZE);
                if (count == -1) {
                    break;
                }
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
//            String message  = e.getMessage();
//            if (!TextUtils.isEmpty(message) && message.contains("ENOSPC")) {
            double requiredSpace = getRequiredSpace(taskData.zipFilePath);
            int requiredSpaceInMb = (int) (requiredSpace / 1024 / 1024);
            String requiredSpaceStr = mContext.getResources().getString(R.string.play_download_insufficent_memory_while_unzip, requiredSpaceInMb);
            String prefNOSPString = PrefUtils.getInstance().getPrefMessageNoSpaceWhileUnzip();
            if (!TextUtils.isEmpty(prefNOSPString)) {
                requiredSpaceStr = prefNOSPString.replace("%1", requiredSpaceInMb + "");
            }
            taskData.cardDownloadData.zipStatus = CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE;
            notifyFailed(taskData.cardDownloadData);
            AlertDialogUtil.showToastNotification(requiredSpaceStr);
            throw e;
//            }
        }
    }

    public void addListener(OnExtractCompletionListener lister) {
        if (lister == null || mListeners.contains(lister)) {
            return;
        }
        mListeners.add(lister);
    }


    public void notifyPorgress(CardDownloadData cardDownloadData, float progress) {
        if (mListeners == null) {
            return;
        }
        for (OnExtractCompletionListener listener :
                mListeners) {
            listener.onProgress(cardDownloadData, progress);
        }
    }

    public void notifyCompletion(CardDownloadData cardDownloadData) {
        if (mListeners == null) {
            return;
        }
        removeTask(cardDownloadData);
        for (OnExtractCompletionListener listener :
                mListeners) {
            listener.onComplete(cardDownloadData);
        }
    }

    private void removeTask(CardDownloadData cardDownloadData) {
        if (cardDownloadData == null) return;
        taskList.remove(cardDownloadData.downloadKey);
    }

    public void notifyFailed(CardDownloadData cardDownloadData) {
        if (mListeners == null) {
            return;
        }
        for (OnExtractCompletionListener listener :
                mListeners) {
            listener.onFailed(cardDownloadData);
        }
    }

    public static class DecompressTaskData {
        public String zipFilePath;
        public String destinationFilePath;
        public CardDownloadData cardDownloadData;

        public DecompressTaskData() {
        }
    }


}