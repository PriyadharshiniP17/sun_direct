package com.myplex.myplex.download;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.security.MediaLinkEncrypted;
import com.myplex.model.CardData;
import com.myplex.model.CardDataVideos;
import com.myplex.model.CardDataVideosItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.CardResponseData;
import com.myplex.model.LocationInfo;
import com.myplex.model.SeasonData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.download.drm.utils.AdaptionSet;
import com.myplex.myplex.download.drm.utils.MPD;
import com.myplex.myplex.download.drm.utils.MPDParser;
import com.myplex.myplex.download.drm.utils.RepresentationData;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.Util;

import junit.framework.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;


/**
 * Created by Srikanth on 20-Jul-17.
 */

public class DownloadManagerUtility {

    private final Context mContext;

    AlertDialog dialog;
    AlertDialog.Builder builder;


    public SimpleExoPlayer player;
    byte[] keySetId;
    private FragmentCardDetailsDescription.DownloadStatusListener mDownloadStatusListener;

    AlertDialog.Builder builderforDownloads;

    private LinearLayout hdOption, bestOption, goodOption, dataSaverOption;
    private Button hdButton, bestButton, goodButton, dataSaverButton;

    private ImageView cancelDownloadIv;

    String drmToken = "";

    String DownloadUrl;
    private String qualitySelected;
    String StreamUrl;


    @SuppressLint("StringFormatInvalid")
    private void showDownloadQualitySelection(final MPD mpd, final ContentDownloadEvent eventData) {
        if (mContext == null
                || ((Activity) mContext).isFinishing()
                || !(mDownloadStatusListener != null
                && mDownloadStatusListener.isToShowDownloadButton())) {
            return;
        }
        builder = new AlertDialog.Builder(mContext);
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
        for (AdaptionSet adaptionSet : mpd.adaptionSetList) {
            if (adaptionSet.type == AdaptionSet.TYPE_VIDEO) {
                for (RepresentationData representationData : adaptionSet.listRepresentations) {
                    float floatBitrate = (representationData.bandwidth / 8f / 1024f / 1024f / 1024f) * 60f * 60f;//GB/hr
                    LoggerD.debugDownload("prepare download tracks: bitrate(GB/hr)- " + floatBitrate + " original bitrate/sec- " + representationData.bandwidth);
                    float mbs = representationData.bandwidth / 1000000f;
                    representationData.dataPerHourInGbph = floatBitrate;
                    float kbs = mbs * 1024;
                    if (kbs < dataSaverBandwidth) {
                        dataSaverOption.setVisibility(View.VISIBLE);
                        dataSaverButton.setVisibility(View.VISIBLE);
                        dataSaverButton.setTag(representationData);
                        dataSaverOptionText.setText(String.format(mContext.getString(R.string.download_option_text_data_saver), String.format("%.2f", floatBitrate)));
                    } else if (kbs < goodBandwidth) {
                        goodOption.setVisibility(View.VISIBLE);
                        goodButton.setVisibility(View.VISIBLE);
                        goodButton.setTag(representationData);
                        goodOptionText.setText(String.format(mContext.getString(R.string.download_option_text_good), String.format("%.2f", floatBitrate)));
                    } else if (kbs < bestBandwidth) {
                        bestOption.setVisibility(View.VISIBLE);
                        bestButton.setVisibility(View.VISIBLE);
                        bestButton.setTag(representationData);
                        bestOptionText.setText(String.format(mContext.getString(R.string.download_option_text_best), String.format("%.2f", floatBitrate)));
                    } else if (kbs >= bestBandwidth && floatBitrate <= 4) {
                        hdOption.setVisibility(View.VISIBLE);
                        hdButton.setVisibility(View.VISIBLE);
                        hdButton.setTag(representationData);
                        hdOptionText.setText(String.format(mContext.getString(R.string.download_option_text_hd), String.format("%.2f", floatBitrate)));
                    }
                }
            }
        }
        hdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepresentationData representationData = (RepresentationData) view.getTag();
                chooseDownloadAudioLink(representationData, mpd, eventData);
                dialog.dismiss();
            }
        });
        bestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepresentationData representationData = (RepresentationData) view.getTag();
                chooseDownloadAudioLink(representationData, mpd, eventData);
                dialog.dismiss();
            }
        });
        goodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepresentationData representationData = (RepresentationData) view.getTag();
                chooseDownloadAudioLink(representationData, mpd, eventData);
                dialog.dismiss();
            }
        });
        dataSaverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepresentationData representationData = (RepresentationData) view.getTag();
                chooseDownloadAudioLink(representationData, mpd, eventData);
                dialog.dismiss();
            }
        });
        dialog = builder.create();
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

    }

    private void chooseDownloadAudioLink(final RepresentationData videoRepresentationData, final MPD mpd, final ContentDownloadEvent event) {
        if (mDownloadStatusListener != null) {
            mDownloadStatusListener.onDownloadStarted();
        }
        RepresentationData audioRepresentationData = null;
        for (AdaptionSet adaptionSet : mpd.adaptionSetList) {
            if (adaptionSet.type == AdaptionSet.TYPE_AUDIO) {
                for (RepresentationData data : adaptionSet.listRepresentations) {
                    audioRepresentationData = data;
                    break;
                }
            }
        }
        LoggerD.debugDownload("Audio_Url" + audioRepresentationData.getUrl() + "Video_Url" + videoRepresentationData.getUrl());
        final String mpdName = mpd.getMpdName();
        LoggerD.debugDownload("mpdName " + mpdName + " getStreamURL- " + mpd.getStreamURL());
        String downloadType = APIConstants.VIDEOQUALTYSD;
//            Since these are small videos assume max 30
        float contentDurationHrs = Util.calculateDurationInSeconds(event.cardData.content.duration) / 60f;//duratin in mints
//            contentDurationHrs = Integer.parseInt(event.cardData.content.duration);
        //and make the Gbph to mbpm
        double requiredSpaceFactor = ((videoRepresentationData.dataPerHourInGbph / 1000) / 60) * contentDurationHrs;
        String formattedSize = String.format("%.2f", requiredSpaceFactor) + " MB";
        String errorMessage = mContext.getString(R.string.play_download_insufficent_memory_vod, String.valueOf(requiredSpaceFactor));
        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(event.cardData.generalInfo.type)) {
            contentDurationHrs = Util.calculateDurationInSeconds(event.cardData.content.duration) / 60f / 60f;// duration in hrs
            requiredSpaceFactor = (videoRepresentationData.dataPerHourInGbph) * contentDurationHrs;
            formattedSize = String.format("%.2f", requiredSpaceFactor) + " GB";
            errorMessage = mContext.getString(R.string.play_download_insufficent_memory_hd, formattedSize);
            downloadType = APIConstants.VIDEOQUALTYHD;
        }
        if (!Util.hasSpaceAvailabeToDownload(downloadType, requiredSpaceFactor, mContext)) {
            AlertDialogUtil.showToastNotification(errorMessage);
            return;
        }
        if (mDownloadStatusListener != null) {
            mDownloadStatusListener.onDownloadInitialized();
        }
        final RepresentationData finalAudioRepresentationData = audioRepresentationData;

        new Handler(mContext.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                downloadFile(mpd.getStreamURL(),
                        mpd.getVideoURL(),
                        mpd.getAudioURL(),
                        mpd.getVideoFileName(),
                        mpd.getAudioFileName(),
                        mpdName,
                        event,
                        videoRepresentationData.id,
                        finalAudioRepresentationData.id);
                if (mDownloadStatusListener != null) mDownloadStatusListener.onSuccess();
            }
        });

    }

    private CardDataVideosItem getDownloadLink(CardDataVideos videos) {
        if (videos == null
                || videos.values == null
                || videos.values.isEmpty()) {
            return null;
        }
        for (CardDataVideosItem videoItem : videos.values) {
            if (APIConstants.VIDEO_TYPE_DOWNLOAD.equalsIgnoreCase(videoItem.type)) {
                return videoItem;
            }
        }
        return null;
    }


    public DownloadManagerUtility(Context context) {
        this.mContext = context;
    }

    /*
        This Event can be fired from any class or Fragment.
        This will handel all the Downloading ogin required for Both MPD and Direct Download
        Only parameters will change for both the downloads.
     */
    public void initializeDownload(ContentDownloadEvent event,
                                   FragmentCardDetailsDescription.DownloadStatusListener downloadStatusListener) {
        if (event == null || event.cardData == null) {
            notifyError(null);
            return;
        }
        this.mDownloadStatusListener = downloadStatusListener;
        if (!ConnectivityUtil.isConnected(mContext)) {
            notifyError(mContext.getString(R.string.network_error));
            return;
        }
        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
        String contentId = event.cardData._id;
        for (String key : downloadlist.mDownloadedList.keySet()) {
            CardDownloadData availableDownloadData = downloadlist.mDownloadedList.get(key);
            if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList == null) {
                if (contentId.equalsIgnoreCase(availableDownloadData._id)) {
                    notifyError(mContext.getString(R.string.download_video_already_downloaded));
                    return;
                }
            } else if (availableDownloadData.tvSeasonsList == null && availableDownloadData.tvEpisodesList != null) {
                contentId = event.cardData._id;
                for (CardDownloadData episode : availableDownloadData.tvEpisodesList) {
                    if (episode != null
                            && contentId.equalsIgnoreCase(episode._id)) {
                        notifyError(mContext.getString(R.string.download_video_already_downloaded));
                        return;
                    }
                }
            } else if (availableDownloadData.tvSeasonsList != null) {
                for (SeasonData seasonData : availableDownloadData.tvSeasonsList) {
                    contentId = event.cardData._id;
                    for (CardDownloadData episode : seasonData.tvEpisodesList) {
                        if (episode != null
                                && contentId.equalsIgnoreCase(episode._id)) {
                            notifyError(mContext.getString(R.string.download_video_already_downloaded));
                            return;
                        }
                    }
                }
            }
        }

        if (PrefUtils.getInstance().isDownloadOnlyOnWifi()
                && !ConnectivityUtil.isConnectedWifi(mContext)) {
            notifyError(mContext.getString(R.string.vf_download_error_wifi_conn_pref));
            return;
        }

        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        MediaLinkEncrypted.Params mediaLinkparams = new MediaLinkEncrypted.Params(event.cardData._id, SDKUtils.getInternetConnectivity(mContext), null, locationInfo, APIConstants.STREAMDOWNLOAD);
        MediaLinkAPIResponseCallBack mMediaLinkFetchListener = new MediaLinkAPIResponseCallBack(new WeakReference<>(this), event);
        MediaLinkEncrypted mMedialLink = new MediaLinkEncrypted(mediaLinkparams, mMediaLinkFetchListener);
        APIService.getInstance().execute(mMedialLink);
    }

    private void notifyError(String msg) {
        if (mDownloadStatusListener != null)
            mDownloadStatusListener.onFailure(msg);
    }

    public void initDownloadKeys(final String downloadUrl, String licenseUrl, final ContentDownloadEvent downloadContentData) {
        Assert.assertNotNull(downloadUrl);
        Assert.assertNotNull(downloadContentData);
        executeDownloadKeys(new DownloadKeys(new WeakReference<>(this), downloadUrl, licenseUrl, downloadContentData));
    }

    private void executeDownloadKeys(DownloadKeys task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }

    private void parseMpdForDownload(final String mDownloadLink, final ContentDownloadEvent contentDownloadEvent) {

        final MPDParser mpdParser = new MPDParser();
        if (APIConstants.isAltBalajiContent(contentDownloadEvent.cardData)) {
            mpdParser.setPartnerType(CardDetails.Partners.ALTBALAJI);
        } else if (APIConstants.isErosNowContent(contentDownloadEvent.cardData)) {
            mpdParser.setPartnerType(CardDetails.Partners.EROSNOW);
        }
        MPDParserCallBack mMPDParserCallback = new MPDParserCallBack(new WeakReference<>(this), contentDownloadEvent, mDownloadLink);
        mpdParser.addParserListerner(mMPDParserCallback);
        mpdParser.execute(mDownloadLink);

    }


    private void downloadFile(String mpdurl, String downloadVideoUrl, String downloadAudioUrl, String videoFileName, String audioFileName, String mpdName, ContentDownloadEvent cardData, int videoTrackId, int audioTrackId) {

        if (downloadAudioUrl != null && downloadVideoUrl != null) {
            if (dialog != null) {
                dialog.dismiss();
            }
            //DownloadUtil.startDownload(mpdurl, cardData.cardData, mContext, mpdName, downloadVideoUrl, downloadAudioUrl, videoFileName, audioFileName, cardData.seasonName, cardData.tvShowCardData);
            if (mDownloadStatusListener != null) {
                mDownloadStatusListener.onSuccess();
            }
        }


    }


    class MPDParserCallBack implements MPDParser.MDPParserListerner {
        private final ContentDownloadEvent contentDownloadEvent;
        private final String mDownloadLink;
        private final WeakReference<DownloadManagerUtility> mRefDowloadManagerUtility;

        MPDParserCallBack(WeakReference<DownloadManagerUtility> mRefDowloadManagerUtility, ContentDownloadEvent cardData, String downloadLink) {
            this.mRefDowloadManagerUtility = mRefDowloadManagerUtility;
            this.contentDownloadEvent = cardData;
            this.mDownloadLink = downloadLink;
        }

        @Override
        public void OnParseFailed() {
//                showAlertDialog(getResources().getString(R.string.parse_error));
            notifyError(mContext.getString(R.string.vf_download_error_while_download));
        }

        @Override
        public void OnParseSuccess(MPD mpd) {
            if (mRefDowloadManagerUtility != null) {
                mRefDowloadManagerUtility.get().showDownloadQualitySelection(mpd, contentDownloadEvent);
            }
        }

        @Override
        public void OnParseProgress(MPDParser.Progress update) {

        }
    }


    private class DownloadKeys extends AsyncTask<Void, Void, Void> {

        private final WeakReference<DownloadManagerUtility> mDownloadManagerUtilityWeakReference;
        private final String downloadUrl, downloadLicenseUrl;
        private final ContentDownloadEvent contentDownloadData;

        public DownloadKeys(WeakReference<DownloadManagerUtility> dowloadManagerUtilityWeakReference, String downloadUrl, String downloadLicenseUrl, ContentDownloadEvent contentDownloadData) {
            this.mDownloadManagerUtilityWeakReference = dowloadManagerUtilityWeakReference;
            this.downloadUrl = downloadUrl;
            this.contentDownloadData = contentDownloadData;
            this.downloadLicenseUrl = downloadLicenseUrl;
        }

        @Override
        protected Void doInBackground(Void... params) {


            try {
                String path = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + contentDownloadData.cardData._id + File.separator;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                path += "metaK";
                File data = new File(path);
              /*  if (ContextCompat.checkSelfPermission(mContext,
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
                LoggerD.debugDownload("DownloadKeys keySetId path- " + data.getAbsolutePath() + " cardData- " + data);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(data));
                objectOutputStream.writeObject(keySetId);
                objectOutputStream.close();
                if (mDownloadManagerUtilityWeakReference != null)
                    mDownloadManagerUtilityWeakReference.get().parseMpdForDownload(downloadUrl, contentDownloadData);
            } catch (Exception e) {
                notifyError(mContext.getString(R.string.vf_download_error_while_download));
                e.printStackTrace();
            }
            return null;
        }
    }


    class MediaLinkAPIResponseCallBack implements APICallback<CardResponseData> {

        private final WeakReference<DownloadManagerUtility> mRefMediaLinkAPIResponseCallBack;
        private final ContentDownloadEvent eventData;

        MediaLinkAPIResponseCallBack(WeakReference<DownloadManagerUtility> mRef, ContentDownloadEvent event) {
            this.mRefMediaLinkAPIResponseCallBack = mRef;
            this.eventData = event;
        }


        String mAPIErrorMessage;

        @Override
        public void onResponse(APIResponse<CardResponseData> response) {
//            //Log.d(TAG, "onResponse ");
            if (null == response) {
                onFailure(new Throwable(mContext.getString(R.string.canot_connect_server)), APIRequest.ERR_UN_KNOWN);
                return;
            }

            if (null == response.body()) {
                onFailure(new Throwable(mContext.getString(R.string.canot_connect_server)), APIRequest.ERR_UN_KNOWN);
                return;
            }

            mAPIErrorMessage = response.body().message;
            if (response.body().results == null
                    || response.body().results.size() == 0) {
                onFailure(new Throwable(mContext.getString(R.string.canot_connect_server)), APIRequest.ERR_UN_KNOWN);
                mAPIErrorMessage = APIConstants.ERROR_EPMTY_RESULTS;
                return;
            }
            final CardData data = response.body().results.get(0);
            if (data.videos != null && data.videos.status != null && !data.videos.status.equalsIgnoreCase("SUCCESS")) {
                if (data.videos.message != null && APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED.equalsIgnoreCase(data.videos.status)) {
                    // AlertDialogUtil.showToastNotification(cardData.videos.message);
                    notifyError(APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED);
                    return;
                } else if (data.videos.message != null && "ERR_PACKAGES_NOT_DEFINED".equalsIgnoreCase(data.videos.status)) {
                    notifyError(data.videos.message);
                    return;
                }
                onFailure(new Throwable(mContext.getString(R.string.canot_connect_server)), APIRequest.ERR_UN_KNOWN);
                return;
            }
            eventData.cardData.videos = data.videos;
            eventData.cardData.subtitles=data.subtitles;

            builderforDownloads = new AlertDialog.Builder(mContext);
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.download_pop_up, null);
            /*TextView info = dialogView.findViewById(R.id.info);
            if (PrefUtils.getInstance().getDrmClientDialogMessage() != null) {
                info.setText(PrefUtils.getInstance().getDrmClientDialogMessage());
            }*/
            hdOption = dialogView.findViewById(R.id.hd_option);
            bestOption = dialogView.findViewById(R.id.best_option);
            goodOption = dialogView.findViewById(R.id.good_option);
            dataSaverOption = dialogView.findViewById(R.id.data_saver_option);


            hdButton = dialogView.findViewById(R.id.hd_button);
            bestButton = dialogView.findViewById(R.id.best_button);

            goodButton = dialogView.findViewById(R.id.good_button);

            dataSaverButton = dialogView.findViewById(R.id.data_saver_button);
            cancelDownloadIv=dialogView.findViewById(R.id.cancel_download);

            cancelDownloadIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    mDownloadStatusListener.onDownloadCancelled();
                }
            });

            builderforDownloads.setView(dialogView);

            hdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    qualitySelected = "hd";
                    collectSelectedBandWidthAndStartDownload(eventData.cardData,"hd");
                    /*if (downloadUrls.get("High") != null) {
                        DownloadUrl = downloadUrls.get("High");
                    } else {
                        DownloadUrl = oldUrls.get("High");
                    }

                    startDownload(DownloadUrl, eventData.cardData, drmToken,getDrmLicenseUrl(downloadMovie,DownloadUrl)
                            ,getType(downloadMovie,DownloadUrl));*/
                    dialog.dismiss();
                }
            });
            bestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    qualitySelected = "best";
                    collectSelectedBandWidthAndStartDownload(eventData.cardData,"best");
                    /*if (downloadUrls.get("High") != null) {
                        DownloadUrl = downloadUrls.get("High");
                    } else if (oldUrls.get(APIConstants.STREAMADAPTIVEDVR) != null) {
                        DownloadUrl = oldUrls.get(APIConstants.STREAMADAPTIVEDVR);
                    } else {
                        DownloadUrl = oldUrls.get("Low");
                    }
                    startDownload(DownloadUrl, eventData.cardData, drmToken,getDrmLicenseUrl(downloadMovie,DownloadUrl)
                    ,getType(downloadMovie,DownloadUrl));*/
                    dialog.dismiss();
                }
            });
            goodButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    qualitySelected = "good";
                    collectSelectedBandWidthAndStartDownload(eventData.cardData,"good");
                    /*DownloadUrl = downloadUrls.get("High");
                    startDownload(DownloadUrl, eventData.cardData, drmToken,getDrmLicenseUrl(downloadMovie,DownloadUrl)
                            ,getType(downloadMovie,DownloadUrl));*/
                    dialog.dismiss();
                }
            });
            dataSaverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    qualitySelected = "datasaver";
                    collectSelectedBandWidthAndStartDownload(eventData.cardData,"datasaver");
                    /*DownloadUrl = downloadUrls.get("High");
                    qualitySelected = "datasaver";
                    startDownload(DownloadUrl, eventData.cardData, drmToken,getDrmLicenseUrl(downloadMovie,DownloadUrl)
                            ,getType(downloadMovie,DownloadUrl));*/
                    dialog.dismiss();
                }
            });


            if (dialog != null && dialog.isShowing()) {
                return;
            }
            dialog = builderforDownloads.create();
            dialog.setCancelable(false);

            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(lp);
            //Fatal Exception: android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@1e13c1b is not valid;
            // is your activity running?

            if (Util.checkActivityPresent(mContext)) {
                    dialog.show();
            }

        }

        @Override
        public void onFailure(Throwable t, int errorCode) {
            if (mDownloadStatusListener != null) {
                String message = null;
                if (t != null)
                    message = t.getMessage();
                notifyError(message);
            }
        }
    }

    private void collectSelectedBandWidthAndStartDownload(CardData downloadMovie,String qualitySelected){

        final HashMap<String, String> downloadUrls = new HashMap<>();
        final HashMap<String, String> oldUrls = new HashMap<>();
        String downloadFormat = APIConstants.STREAMDASH;
        String downloadType = APIConstants.OFFLINE_DOWNLOAD;

        for (int i = 0; i < downloadMovie.videos.values.size(); i++) {
            if (downloadMovie.videos.values.get(i).format.equalsIgnoreCase(downloadFormat)
                    && downloadMovie.videos.values.get(i).type.equalsIgnoreCase(downloadType)) {
                downloadUrls.put(downloadMovie.videos.values.get(i).profile, downloadMovie.videos.values.get(i).link);
                if (downloadMovie.videos.values.get(i).drmToken != null) {
                    drmToken = downloadMovie.videos.values.get(i).drmToken;
                }
            }
            if ((downloadMovie.videos.values.get(i).format.equalsIgnoreCase("dash")||
                    downloadMovie.videos.values.get(i).format.equalsIgnoreCase("hls"))
                    && ((downloadMovie.videos.values.get(i).type.equalsIgnoreCase("streaming")||
                    downloadMovie.videos.values.get(i).type.equalsIgnoreCase("download")))) {
                oldUrls.put(downloadMovie.videos.values.get(i).profile, downloadMovie.videos.values.get(i).link);
            }
        }

        if (downloadUrls.get(qualitySelected)!=null){
            DownloadUrl = downloadUrls.get(qualitySelected);
        }else if (oldUrls.get(qualitySelected)!=null){
            DownloadUrl=oldUrls.get(qualitySelected);
        }else if (oldUrls.get("High")!=null){
            DownloadUrl=oldUrls.get("High");
        } else if (oldUrls.get(APIConstants.STREAMADAPTIVEDVR) != null) {
            DownloadUrl = oldUrls.get(APIConstants.STREAMADAPTIVEDVR);
        }

        startDownload(DownloadUrl, downloadMovie, drmToken,getDrmLicenseUrl(downloadMovie,DownloadUrl)
                ,getType(downloadMovie,DownloadUrl));
        ComScoreAnalytics.getInstance().setEventDownloadInitiated(downloadMovie,qualitySelected, "NA");
    }

    private String getType(CardData mData,String selectedUrl){
        String type="";
        if(selectedUrl!=null&&!TextUtils.isEmpty(selectedUrl)){
            for (int i = 0; i < mData.videos.values.size(); i++) {
                if (selectedUrl.equalsIgnoreCase(mData.videos.values.get(i).link)){
                    type=mData.videos.values.get(i).format;
                }
            }
        }
        return type;
    }

    private String getDrmLicenseUrl(CardData mData,String selectedUrl){
         String drmLicenseUrl="";
         if(selectedUrl!=null&&!TextUtils.isEmpty(selectedUrl)){
             for (int i = 0; i < mData.videos.values.size(); i++) {
                 if (selectedUrl.equalsIgnoreCase(mData.videos.values.get(i).link)){
                     drmLicenseUrl=mData.videos.values.get(i).licenseUrl;
                 }
             }
         }
        return drmLicenseUrl;
    }

    private void startDownload(final String downloadUrl, final CardData mData,
                               String drmToken,String drmLicenseUrl,String format) {
        if (mDownloadStatusListener != null) {
            mDownloadStatusListener.onDownloadStarted();
        }
        StreamUrl = downloadUrl;
        //Util.incrementPlaybackCounter();
        try {
            if (StreamUrl == null) {
                AlertDialogUtil.showToastNotification("Sorry Can't Download");
            }
            if (drmLicenseUrl==null||TextUtils.isEmpty(drmLicenseUrl)){
                drmLicenseUrl = APIConstants.SCHEME + APIConstants.BASE_URL + APIConstants.LICENSE_URL + "?content_id=" + mData._id + "&licenseType=download&timestamp=" + System.currentTimeMillis() + "&clientKey=" + PrefUtils.getInstance().getPrefClientkey();
                SDKLogger.debug("Download License"+drmLicenseUrl);
            }
            startExoDownload(downloadUrl, mData, drmToken, drmLicenseUrl,format);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Unable to download", Toast.LENGTH_SHORT).show();
        }

    }

    private void startExoDownload(String downloadUrl, CardData cardData, String drmToken,String drmLicenseUrl,String format) {
        if (mDownloadStatusListener != null) {
            mDownloadStatusListener.onDownloadInitialized();
        }
        String subtitleUrl = "";
        if (cardData != null && cardData.subtitles != null && cardData.subtitles.values != null &&
                cardData.subtitles.values.size() > 0 && cardData.subtitles.values.get(0) != null && cardData.subtitles.values.get(0).link_sub != null) {
            subtitleUrl = cardData.subtitles.values.get(0).link_sub;
        }
        DownloadUtil.startDownload(downloadUrl,cardData,mContext,
                null,null,null,
                null,null,subtitleUrl,qualitySelected,drmToken,drmLicenseUrl,format);
    }
}
