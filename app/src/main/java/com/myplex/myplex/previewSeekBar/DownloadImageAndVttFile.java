package com.myplex.myplex.previewSeekBar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.model.CardData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.util.PrefUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import junit.framework.Assert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class DownloadImageAndVttFile {
    String vttPath;
    String[] spriteImagesUrlArray;
    private HashMap countFilesToDownload ;
    public static final String VTT_FILE_INDEX = "vtt";
    CardData cardData;
    private WeakReference<Context> contextWeakReference;
    private WebVTTParser webVTTParser;
    private boolean isParsingCompleted =false,isAllDownloadComplete = false,isThumbnailDownloadStarted =false;

    public enum STATE_OF_DOWNLOAD{
        STARTED,VTT_COMPLETED,PARSING_COMPLETED,IMAGE_DOWNLOAD_STARTED,IMAGE_DOWNLOAD_COMPLETED,IN_PROGRESS,COMPLETED,ERROR
    }
    DownloadImageVttFileListener downloadImageVttFileListener;

    public DownloadImageAndVttFile(WeakReference<Context> context,CardData cardData
                                    ,DownloadImageVttFileListener downloadImageVttFileListener) {
        contextWeakReference = context;
        this.cardData = cardData;
        this.downloadImageVttFileListener = downloadImageVttFileListener;
        vttPath = cardData.thumbnailSeekPreview;
        init();
    }

    private void init(){
        if(TextUtils.isEmpty(vttPath)){
            return;
        }
        countFilesToDownload = new HashMap<String,Boolean>();
        downloadImageVttFileListener.OnDownloadProgress(STATE_OF_DOWNLOAD.STARTED);
        if (vttPath != null && vttPath.contains("/320/")){
            vttPath = vttPath.replace("/320/","/150/");
        }
        initDownloadPreviewImageVttFile(vttPath, cardData);

    }

    private  void imageDownload(WeakReference<Context> ctx, String url, String localPath){
        Log.e("PreviewNew","Image download function called"+localPath);
        Context context = (Context) ctx.get();

        Picasso.get()
                .load(url)
                .into(getTarget(localPath));
    }
    private  Target getTarget(final  String localPath){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                /*new Thread(new Runnable() {

                    @Override
                    public void run() {*/
                        isThumbnailDownloadStarted = true;
                        Log.e("PreviewNew","Image download started background");
                        File file = new File(  ApplicationController.getAppContext().getFilesDir()+ "/" + localPath);
                        Log.e("PreviewImageDownload","ImageFilename -- "+ file.getAbsolutePath());
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                            ostream.flush();
                            ostream.close();
                            Log.e("PreviewNew","Image download completed");
                            checkAllInputFilesDownloaded(localPath);
                            Log.e("PreviewNew","Image download completed for "+ localPath);


                        } catch (IOException e) {
                            Log.e("PreviewNew", e.getLocalizedMessage());
                            reportError();
                            downloadImageVttFileListener.OnDownloadFailed();
                        }catch (Exception e){
                            Log.e("PreviewNew", e.getLocalizedMessage());
                            reportError();
                            downloadImageVttFileListener.OnDownloadFailed();
                        }
                   /* }
                }).start();
*/
            }

            @Override
            public void onBitmapFailed(Exception e,Drawable errorDrawable) {
                Log.e("PreviewNew","Failed");
                reportError();
                downloadImageVttFileListener.OnDownloadFailed();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        return target;
    }

    public void initDownloadPreviewImageVttFile(final String downloadUrl, final CardData cardData) {
        Assert.assertNotNull(downloadUrl);
        Assert.assertNotNull(cardData);
        executeDownloadPreviewImageVttFile(new DownloadPreviewImageVttFile(contextWeakReference, downloadUrl, cardData));
    }

    private void executeDownloadPreviewImageVttFile(DownloadPreviewImageVttFile task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    public class DownloadPreviewImageVttFile extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<Context> playerReference;
        private final String downloadPreviewImageVttUrl;
        private final CardData contentDownloadData;

        public DownloadPreviewImageVttFile(WeakReference<Context> dowloadManagerUtilityWeakReference, String downloadPreviewImageVttUrl, CardData contentDownloadData) {
            this.playerReference = dowloadManagerUtilityWeakReference;
            this.downloadPreviewImageVttUrl = downloadPreviewImageVttUrl;
            this.contentDownloadData = contentDownloadData;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            File data = null;
            try {
                Log.e("PreviewNew","vtt download started");
                URL u = new URL(downloadPreviewImageVttUrl);
                /*URLConnection conn = u.openConnection();*/
                /*int contentLength = conn.getContentLength();*/
                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(u.openStream(), 8192);

                /* DataInputStream stream = new DataInputStream(u.openStream());*/

               /* byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();*/
               Context mContext = (Context)contextWeakReference.get();
                if(mContext == null){
                    return null ;
                }
                String path = mContext.getFilesDir() +  DownloadUtil.downloadVideosStoragePath + contentDownloadData._id + File.separator;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                path += "preview.vtt";
                data = new File(path);
                Log.e("PreviewNew","Preview path- " + data.getAbsolutePath() + " cardData- " + data);
                // Output stream to write file
                OutputStream output = new FileOutputStream(data);
                /*DataOutputStream fos = new DataOutputStream(new FileOutputStream(data));
                fos.write(buffer);
                fos.flush();
                fos.close();*/
                byte data1[] = new byte[1024];
                while ((input.read(data1)) != -1) {

                    // writing data to file
                    output.write(data1);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return true;

                /*Toast.makeText(mContext,"vtt file download completed",Toast.LENGTH_SHORT);*/


            }catch(FileNotFoundException e) {
                Log.e("Preview",e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("Preview","VTT File download failed IO exception");
                e.printStackTrace();
            }  catch (Exception e){
                Log.e("Preview","VTT File download failed  exception");
                e.printStackTrace();
            }
            return  false ;
        }

        @Override
        protected void onPostExecute(Boolean path) {
            super.onPostExecute(path);
            if(path) {
                downloadImageVttFileListener.OnDownloadProgress(STATE_OF_DOWNLOAD.VTT_COMPLETED);
                vttFileDownloadSuccessful(contentDownloadData);
            }else{
                reportError();
                downloadImageVttFileListener.OnDownloadFailed();
            }
        }
    }

    void reportError(){
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable(){
            @Override
            public void run() {
                downloadImageVttFileListener.OnDownloadProgress(STATE_OF_DOWNLOAD.ERROR);
            }
        });
    }



    protected synchronized void checkAllInputFilesDownloaded(String fileName) {
        if(countFilesToDownload.containsKey(fileName)){
            countFilesToDownload.put(fileName,true);
        }
        isAllDownloadComplete = isAllFilesDownloaded();
        if(isAllDownloadComplete) {
            Context mContext = contextWeakReference.get();
            if(mContext == null){
                return;
            }
            Handler mainHandler = new Handler(mContext.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.e("PreviewNew","isAllDownloadComplete"+isAllDownloadComplete);
                    downloadImageVttFileListener.OnParseComplete(webVTTParser.webVTTModule, isAllDownloadComplete);
                }
            };
            mainHandler.post(myRunnable);


        }
        /*isAllFilesDownloaded();*/

    }

    public boolean isAllFilesDownloaded(){
        if(countFilesToDownload != null){
            Iterator it = countFilesToDownload.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                Log.e("PreviewNew",pair.getKey()+" "+pair.getValue());
                if((boolean)pair.getValue()==false){
                    return false;
                }

            }
            if(isParsingCompleted) {
                downloadImageVttFileListener.ParsingDownloadComplete(webVTTParser.webVTTModule);
                downloadImageVttFileListener.OnDownloadSuccess(true);
             }else{
                downloadImageVttFileListener.OnDownloadSuccess(false);
            }
            return true;
        }
        return false;
    }

    private void vttFileDownloadSuccessful(final CardData contentDownloadEvent) {
        if(contextWeakReference == null){
            return ;
        }
        Log.e("PreviewNew","vtt download completed");
        Context mContext = (Context)contextWeakReference.get();
        String path = mContext.getFilesDir() +  DownloadUtil.downloadVideosStoragePath + contentDownloadEvent._id + File.separator + "preview.vtt";;
        File file = new File(path);
        webVTTParser = new WebVTTParser(file);
        webVTTParser.setOnCompleteListener(webVttParserInterface);
        webVTTParser.init();

    }

    private String getSpriteImagePath(String thumbnailsUrl){
        try {
            if (cardData != null && !TextUtils.isEmpty(cardData.thumbnailSeekPreview)) {
                URL url = new URL(cardData.thumbnailSeekPreview);
                String token = new File(url.getPath()).getName();
                String spriteImage = cardData.thumbnailSeekPreview;
                String targetString = spriteImage.replace(token,thumbnailsUrl);
                return targetString;
            }
        }catch (Exception e){

        }
        return null;
    }

    WebVttParserInterface webVttParserInterface = new WebVttParserInterface() {
        @Override
        public void webVttParserComplete(HashSet<String> thumbnailsUrl) {
            Log.e("PreviewNew","Parsing completed");
            Log.e("PreviewNew","thumbnailsUrl"+thumbnailsUrl);
            if( thumbnailsUrl.isEmpty() ){
                return;
            }

            isParsingCompleted = true;
            downloadImageVttFileListener.OnDownloadProgress(STATE_OF_DOWNLOAD.IMAGE_DOWNLOAD_STARTED);
            for(String url : thumbnailsUrl) {
                countFilesToDownload.put(url, false);
            }
            for(String url : thumbnailsUrl) {
                url = getSpriteImagePath(url);
                if (url != null && url.contains("/320/")){
                    url = url.replace("/320/","/150/");
                }
                Log.e("PreviewNew", "thumbnailsUrl" + url);
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                String lastWord = url.substring(url.lastIndexOf("/") + 1);

                if (PrefUtils.getInstance().getIsShowThumbnailPreview()){
                    imageDownload(contextWeakReference, url, lastWord);
                    final String mUrl = url;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!isThumbnailDownloadStarted)
                                imageDownload(contextWeakReference, mUrl, lastWord);
                        }
                    },3000);
                }

            }
        }

        @Override
        public void webVttParserFailed() {
            downloadImageVttFileListener.OnParseFailed();
            reportError();
        }
    };

    public interface DownloadImageVttFileListener {
        public void OnDownloadFailed();

        public void OnDownloadSuccess(boolean showPreviewBar);

        public void OnDownloadProgress(STATE_OF_DOWNLOAD update);

        public void OnParseComplete(WebVTTModule webVTTModule,boolean showPreviewBar);

        public void OnParseFailed();

        public  void ParsingDownloadComplete(WebVTTModule webVTTModule);

    }


}
