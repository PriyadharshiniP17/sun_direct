package com.myplex.myplex.ui.adapter;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataContent;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CardDataImages;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.LanguageTitleData;
import com.myplex.model.PublishingHouse;
import com.myplex.player_config.DownloadManagerMaintainer;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.download.Decompress;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.events.DownloadDeleteEvent;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentDownloaded;
import com.myplex.myplex.ui.views.gifview.GifAnimationDrawable;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.myplex.myplex.ApplicationController.getApplicationConfig;
import static com.myplex.myplex.ApplicationController.getDownloadData;

/**
 * Created by apalya on 3/4/2017.
 */

public class AdapterForDownloads extends BaseAdapter implements FetchDownloadProgress.DownloadProgressStatus,
        ErosNowDownloadManager.UnzipProcessListener {


    private final boolean showProgress;
    private final int layoutType;

    @Override
    public void onCompletion(CardDownloadData cardDownloadData) {
        refreshData();
        LoggerD.debugDownload("UnzipProcessListener:onCompletion cardDownloadData- " + cardDownloadData);
    }

    @Override
    public void onFailure() {
        refreshData();
        LoggerD.debugDownload("UnzipProcessListener:onCompletion cardDownloadData- " + cardDownloadData);
    }

    private Context mContext;
    private List<CardDownloadData> mDownloadedMovies;
//    private WaveHelper mWaveHelper;
    private FetchDownloadProgress mDownloadProgressManager;
    private Handler mHandler;
    private final DownloadManagerLister downloadManagerListener;
    CardDownloadData cardDownloadData;

    public AdapterForDownloads(Context context, boolean showProgress, int layoutType) {
        mContext = context;
        mDownloadedMovies = new ArrayList<>();
        mHandler = new Handler(Looper.getMainLooper());
        mDownloadProgressManager = FetchDownloadProgress.getInstance(mContext);
        mDownloadProgressManager.startPolling();
//        HungamaPartnerHandler.getInstance(mContext).addProgressListener(this);
        downloadManagerListener = new DownloadManagerLister();

        this.layoutType = layoutType;
        this.showProgress = showProgress;
    }

    @Override
    public int getCount() {
        if (mDownloadedMovies != null && mDownloadedMovies.size() >= 1)
            return mDownloadedMovies.size();
        else
            return 0;
    }

    @Override
    public CardDownloadData getItem(int position) {
        return mDownloadedMovies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void add(final List<CardDownloadData> vodCardList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDownloadedMovies.clear();
                mDownloadedMovies.addAll(vodCardList);
                notifyDataSetChanged();
            }
        });
    }

    public void reset(List<CardDownloadData> vodCardList) {
        mDownloadedMovies = vodCardList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder mViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.downloads_for_tvshows, null, false);

            mViewHolder = new ViewHolder();
            injectViews(mViewHolder, convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        cardDownloadData = mDownloadedMovies.get(position);
        if (null == cardDownloadData) {
            return convertView;
        }
        String imageLink = cardDownloadData.ImageUrl;
        if (TextUtils.isEmpty(imageLink)
                || imageLink.compareTo("Images/NoImage.jpg") == 0) {
            mViewHolder.mThumbnailMovie.setImageResource(R.drawable
                    .movie_thumbnail_placeholder);
        } else {
            PicassoUtil.with(mContext).load(imageLink, mViewHolder.mThumbnailMovie, R.drawable.movie_thumbnail_placeholder);
        }

        mViewHolder.mTextViewTitle.setVisibility(VISIBLE);
        mViewHolder.mTextViewTitle.setText(cardDownloadData.title);
        if (cardDownloadData.genres != null && cardDownloadData.time_languages != null) {
            mViewHolder.mGenres.setText(cardDownloadData.genres + " | "+cardDownloadData.time_languages);
        }
        String formattedSize = String.format("%.0f",cardDownloadData.mDownloadedBytes) +" MB";
        mViewHolder.videoSize.setVisibility(VISIBLE);

        if (cardDownloadData != null
                && cardDownloadData.duration != null
                && cardDownloadData.duration.equalsIgnoreCase("0:0:00")) {
            int contentDurationMnts = Util.calculateDurationInSeconds(cardDownloadData.duration) / 60;//duratin in mints
            if (contentDurationMnts <= 0) {
                mViewHolder.videoSize.setVisibility(View.GONE);
            } else {
                mViewHolder.videoSize.setText(cardDownloadData.duration);
                mViewHolder.videoSize.setVisibility(View.VISIBLE);
            }
        }else{
            mViewHolder.videoSize.setVisibility(VISIBLE);
        }

        /*if (cardDownloadData.mDownloadedBytes < 0 || APIConstants.isHungamaContent(cardDownloadData)) {
            mViewHolder.videoSize.setVisibility(GONE);
        }*/
        mViewHolder.mPlayButton.setTag(position);
        /*mViewHolder.mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(view.getTag() instanceof Integer)) {
                    return;
                }
                int pos = (int)view.getTag();
                showDetailsFragment(pos);
            }
        });*/
        mViewHolder.deleteButton.setTag(position);
        mViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(view.getTag() instanceof Integer)) {
                    return;
                }
                int pos = (int)view.getTag();
                showDetailsPopup(pos);
                //deleteDownloads(pos);
            }
        });
        mViewHolder.cancle_text.setTag(position);
        mViewHolder.cancle_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(view.getTag() instanceof Integer)) {
                    return;
                }
                int pos = (int)view.getTag();
                deleteDownloads(pos);
            }
        });


        mViewHolder.videoType.setVisibility(GONE);

        if (cardDownloadData != null) {
            if (cardDownloadData.mCompleted) {
                int status=getDownloadStatusFromSDK(cardDownloadData._id);
                    if (status== DownloadManagerMaintainer.STATE_FAILED) {
                        showDownloadFailedUI(mViewHolder, cardDownloadData);
                        return convertView;
                    } else if (status== DownloadManagerMaintainer.STATE_DOWNLOADING) {
                        showDownloadingUI(mViewHolder, cardDownloadData);
                        return convertView;
                    } else if (status == DownloadManagerMaintainer.STATE_COMPLETED) {
                        showDownloadCompleted(mViewHolder, cardDownloadData, position);
                        return convertView;
                    }
                showDownloadFailedUI(mViewHolder, cardDownloadData);
            } else {
                showDownloadingUI(mViewHolder, cardDownloadData);
            }
            return convertView;
        }
        return convertView;
    }

    public int getDownloadStatusFromSDK(String contentId){
        return DownloadManagerMaintainer.getInstance().getDownloadStatus(contentId);
    }

    private void showDetailsPopup(int pos) {

        LinearLayout ll_play_now,ll_DownloadPage,ll_Delect_Download;
        TextView txt_dialog_alert;

        Dialog dialog=new Dialog(mContext);
        dialog.setContentView(R.layout.custom_dialog_download_popup);

        ll_play_now=dialog.findViewById(R.id.ll_play_now);
        ll_DownloadPage=dialog.findViewById(R.id.ll_DownloadPage);
        ll_Delect_Download=dialog.findViewById(R.id.ll_delect_download);
        txt_dialog_alert=dialog.findViewById(R.id.txt_dia);

        
        ll_play_now.setOnClickListener(v ->{
            dialog.dismiss();
            showDetailsFragment(pos);
        });
        ll_DownloadPage.setOnClickListener(v -> {
        });
        ll_Delect_Download.setOnClickListener(v -> {
            dialog.dismiss();
            deleteDownloads(pos);
        });

        ll_DownloadPage.setVisibility(GONE);

        if (getDownloadStatusFromSDK(mDownloadedMovies.get(pos)._id) ==DownloadManagerMaintainer.STATE_COMPLETED) {
            ll_play_now.setVisibility(VISIBLE);
            txt_dialog_alert.setText(mContext.getResources().getString(R.string.download_text));
        } else {
            ll_play_now.setVisibility(GONE);
            txt_dialog_alert.setText(mContext.getResources().getString(R.string.no_download_text));
        }

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        dialog.show();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void showDownloadCompleted(ViewHolder mViewHolder, CardDownloadData cardDownloadData, int position) {
        /*if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardDownloadData.ItemType)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(cardDownloadData.ItemType)
                || APIConstants.TYPE_VOD.equalsIgnoreCase(cardDownloadData.ItemType)) {
            mViewHolder.mViewContainer.setTag(position);
            mViewHolder.mViewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(view.getTag() instanceof Integer)) {
                        return;
                    }
                    int pos = (int) view.getTag();
                    showDetailsFragment(pos);
                }
            });
        }*/
        mViewHolder.mStatusText.setText("Downloaded Successfully");
        mViewHolder.mStatusText.setVisibility(VISIBLE);
        mViewHolder.mPlayButton.setVisibility(VISIBLE);
        mViewHolder.downloadbtnImage.setVisibility(View.VISIBLE);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(GONE);
        mViewHolder.downloadbtnImage.setImageResource(R.drawable.episode_list_download_icon_complete);
        mViewHolder.deleteButton.setVisibility(VISIBLE);
        ComScoreAnalytics.getInstance().setEventDownloadCompleted(generateCardData(cardDownloadData),"NA",true);
    }

    private void showDownloadingUI(final ViewHolder mViewHolder, CardDownloadData cardDownloadData) {
        if (layoutType == FragmentDownloaded.TYPE_TV_SHOWS) {
            mViewHolder.mPlayButton.setVisibility(GONE);
            mViewHolder.mDownloadInProgressLinearLayout.setVisibility(GONE);
            mViewHolder.downloadbtnImage.setVisibility(View.GONE);
            return;
        }
        mViewHolder.mViewContainer.setOnClickListener(null);
        mViewHolder.mPlayButton.setVisibility(GONE);
        mViewHolder.downloadbtnImage.setVisibility(View.GONE);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(VISIBLE);
        mViewHolder.mDownloadPercentText.setVisibility(View.VISIBLE);
        try {
            final GifAnimationDrawable gifAnim;
            if (mViewHolder.downloadingGIFAnim.getTag() instanceof GifAnimationDrawable) {
                Log.v("GifAnimationDrawable", "Animation drawable tag is null");
                gifAnim = (GifAnimationDrawable) mViewHolder.downloadingGIFAnim.getTag();
            } else {
                Log.v("GifAnimationDrawable", "Animation drawable taken from tag");
                gifAnim = new GifAnimationDrawable(mViewHolder.downloadingGIFAnim.getContext().getResources().openRawResource(R.raw.download_progress_anim));
                gifAnim.setOneShot(false);
            }
//            if (mViewHolder.downloadingGIFAnim.getDrawable() == null) {
                mViewHolder.downloadingGIFAnim.setTag(gifAnim);
                mViewHolder.downloadingGIFAnim.setImageDrawable(gifAnim);
               /* mViewHolder.downloadingGIFAnim.post(new WeakRunnable<AdapterForDownloads>(this) {
                    @Override
                    protected void safeRun(AdapterForDownloads adapterEpisode) {
                        Log.v("GifAnimationDrawable", "Post Animation Started");
                        if (!gifAnim.isRunning()) {
                            gifAnim.setVisible(true, true);
                            gifAnim.stop();
                            gifAnim.start();
                        }
                        mViewHolder.downloadingGIFAnim.invalidateDrawable(mViewHolder.downloadingGIFAnim.getDrawable());
                        mViewHolder.downloadingGIFAnim.postDelayed(this, 7000);
                    }
                });*/
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String status = mContext.getResources().getString(R.string.downloading);

        if (cardDownloadData != null) {
            if (cardDownloadData.mPercentage < 0) {
                cardDownloadData.mPercentage = 0;
            }
            String progress = String.valueOf((int) cardDownloadData.mPercentage) + "%";
            if (cardDownloadData.mPercentage == 100
                    && cardDownloadData.zipStatus < CardDownloadData.STATUS_FILE_UNZIPPED) {
                progress = "99%";
                status = mContext.getResources().getString(R.string.download_status_extracting);
                if (cardDownloadData.zipStatus == CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE) {
                    String prefStateNOSP = PrefUtils.getInstance().getPrefDownloadStateNOSPAvailable();
                    status = TextUtils.isEmpty(prefStateNOSP) ? mContext.getResources().getString(R.string.download_status_not_enough_space) : prefStateNOSP;
                } else if (cardDownloadData.zipStatus == CardDownloadData.STATUS_FAILED_TO_AQUIRE_DRM_LICENSE) {
                    String prefStateDRMLicenseFaile = PrefUtils.getInstance().getPrefDownloadStateDRMLicenseFailed();
                    status = TextUtils.isEmpty(prefStateDRMLicenseFaile) ? mContext.getResources().getString(R.string.download_status_failed_acquiring_drm_license) : prefStateDRMLicenseFaile;
                }
            }
            LoggerD.debugDownload("progress- " + cardDownloadData.mPercentage);
            mViewHolder.mDownloadPercentText.setText(progress);
        }
        mViewHolder.mPlayButton.setVisibility(GONE);
        if (!ConnectivityUtil.isConnected(mContext)) {
            mViewHolder.downloadingGIFAnim.setImageResource(R.drawable.carddetaildescription_download_icon_without_text);
            status = mContext.getResources().getString(R.string.download_state_paused_text);
        }
        mViewHolder.mStatusText.setText(status);
        mViewHolder.mStatusText.setVisibility(View.VISIBLE);
        mViewHolder.downloadingGIFAnim.setVisibility(View.VISIBLE);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(View.VISIBLE);
    }

    private void showDownloadFailedUI(ViewHolder mViewHolder, CardDownloadData cardDownloadData) {
        mViewHolder.mStatusText.setVisibility(VISIBLE);
        mViewHolder.mPlayButton.setVisibility(GONE);
        mViewHolder.downloadbtnImage.setVisibility(View.VISIBLE);
        mViewHolder.downloadbtnImage.setImageResource(R.drawable.description_download_broken);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(GONE);
        if (layoutType == FragmentDownloaded.TYPE_MOVIES) {
            mViewHolder.mViewContainer.setOnClickListener(null);
        }
        if (cardDownloadData.mPercentage != 100) {
            String error = mContext.getResources().getString(R.string.download_state_failed_generic_text);
            try {
                if (mDownloadProgressManager != null
                        && cardDownloadData != null
                        && cardDownloadData.mDownloadId != -1) {
                    switch (mDownloadProgressManager.getDownloadStatus(cardDownloadData.mDownloadId)) {
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            error = mContext.getResources().getString(R.string.download_state_rare_download);
                            break;
                    }
                    if (ApplicationController.SHOW_PLAYER_LOGS) {
                        error = error + " status: " + mDownloadProgressManager.getDownloadStatus(cardDownloadData.mDownloadId);
                    }
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            LoggerD.debugDownload("Download information for " + cardDownloadData.mDownloadId + mDownloadProgressManager.getDownloadStatus(cardDownloadData.mDownloadId));

            mViewHolder.mStatusText.setText(error);
        } else if (APIConstants.isErosNowContent(cardDownloadData)
                && cardDownloadData.zipStatus < CardDownloadData.STATUS_FILE_UNZIPPED) {
            mViewHolder.mDownloadPercentText.setText("99%");
            mViewHolder.mStatusText.setText(R.string.download_status_extracting);
            if (cardDownloadData.zipStatus == CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE) {
                String prefStateNOSP = PrefUtils.getInstance().getPrefDownloadStateNOSPAvailable();
                mViewHolder.mStatusText.setText(TextUtils.isEmpty(prefStateNOSP) ? mContext.getResources().getString(R.string.download_status_not_enough_space) : prefStateNOSP);
            } else if (cardDownloadData.zipStatus == CardDownloadData.STATUS_FAILED_TO_AQUIRE_DRM_LICENSE) {
                String prefStateDRMLicenseFaile = PrefUtils.getInstance().getPrefDownloadStateDRMLicenseFailed();
                mViewHolder.mStatusText.setText(TextUtils.isEmpty(prefStateDRMLicenseFaile) ? mContext.getResources().getString(R.string.download_status_failed_acquiring_drm_license) : prefStateDRMLicenseFaile);
            }
        }

    }

    private void showDetailsFragment(int position) {
        if (mDownloadedMovies == null
                || mDownloadedMovies.isEmpty()
                || position >= mDownloadedMovies.size()) {
            return;
        }
        final CardDownloadData cardDownloadData = mDownloadedMovies.get(position);
        CardData cardData = generateCardData(cardDownloadData);
        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        args.putString(CardDetails
                .PARAM_CARD_ID, cardData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putString(CardDetails.PARAM_PARTNER_ID, cardData.generalInfo.partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_DOWNLOADED_VIDEOS);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, cardData.generalInfo.title);

        ((MainActivity) mContext).showDetailsFragment(args, cardData);
    }

    public CardData generateCardData(CardDownloadData cardDownloadData) {
        CardData cardData = new CardData();

        // LanguageTitleData for CardData
        LanguageTitleData titleData = new LanguageTitleData();
        List<LanguageTitleData> languageTitleDatas = new ArrayList<>();
        languageTitleDatas.add(titleData);
        titleData.language = cardDownloadData.time_languages;
        titleData.title = cardDownloadData.title;

        //CardDataGeneralInfo for CardData
        CardDataGeneralInfo generalInfo = new CardDataGeneralInfo();
        generalInfo._id = cardDownloadData._id;
        generalInfo.type = cardDownloadData.ItemType;
        generalInfo.title = cardDownloadData.title;
        generalInfo.altTitle = languageTitleDatas;
        generalInfo.partnerId = cardDownloadData.downloadKey;
        generalInfo.description = cardDownloadData.description;
        generalInfo.briefDescription = cardDownloadData.briefDescription;
        cardData.publishingHouse = new PublishingHouse();
        cardData.publishingHouse.publishingHouseName = cardDownloadData.partnerName;

        // For Images
        CardDataImages images = new CardDataImages();
        List<CardDataImagesItem> imagesItems = new ArrayList<>();
        CardDataImagesItem cardDataImagesItem = new CardDataImagesItem();
        cardDataImagesItem.link = cardDownloadData.coverPosterImageUrl;
        cardDataImagesItem.profile = "mdpi";
        cardDataImagesItem.resolution = "640x360";
        cardDataImagesItem.type = APIConstants.COVERPOSTER;
        imagesItems.add(cardDataImagesItem);
        images.values = imagesItems;
        //For Player Link
        cardData.localFilePath = cardDownloadData.mDownloadPath;
        if (APIConstants.isHooqContent(cardData)) {
            cardData.localFilePath = cardDownloadData.hooqCacheId;
        }
        cardData.isDownloadDataOnExternalStorage = !cardDownloadData.isStoredInternally;
        cardData._id = cardDownloadData._id;
        cardData.generalInfo = generalInfo;
        /*cardData.content = new CardDataContent();
        if (null != cardData.content) {
            cardData.content.genre = new ArrayList<>();
            CardDataGenre genre = new CardDataGenre();
            genre.name = cardDownloadData.genres;
            cardData.content.genre.add(genre);

            cardData.content.language = new ArrayList<>();
            cardData.content.language.add(cardDownloadData.time_languages);

            cardData.content.duration = cardDownloadData.duration;
            cardData.content.releaseDate = cardDownloadData.releaseDate;
        }*/
        cardData.images = images;
        CardDataContent cardDataContent = new CardDataContent();
        cardDataContent.categoryName = cardDownloadData.categoryName;
        cardDataContent.categoryType = cardDownloadData.categoryType;
        cardData.content = cardDataContent;
        return cardData;
    }

    private void deleteDownloads(final int position) {
        if (mDownloadedMovies == null || mDownloadedMovies.isEmpty()) {
            return;
        }
        final CardDownloadData cardDownloadData = mDownloadedMovies.get(position);
        String itemType = null;
        if (cardDownloadData == null
                || cardDownloadData.ItemType == null) {
            return;
        }
        switch (cardDownloadData.ItemType) {
            case APIConstants.TYPE_MOVIE:
                itemType = "Movie";
                break;
            default:
                itemType = "Video";
                break;
        }
        new MaterialAlertDialogBuilder(mContext)
//                .setTitle(R.string.vf_download_state_deleted_text)
                .setMessage(mContext.getString(R.string.vf_txt_delete_download ) + " " + itemType + " ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        String status = CleverTap.PROPERTY_DOWNLOAD_DELETED;
                        if (cardDownloadData != null
                                && cardDownloadData.mPercentage < 100) {
                            status = CleverTap.PROPERTY_DOWNLOAD_CANCELED;
                        }
                        try {
                            CleverTap.eventDownload(cardDownloadData, status);


                            if (APIConstants.isHungamaContent(cardDownloadData)) {
                                LoggerD.debugDownload("deleteDownload downloadKey- " + cardDownloadData.downloadKey);
                            }
                            Decompress.getInstance(mContext).clearTasks();
                            DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                            String Keyspath = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + cardDownloadData._id + File.separator + "metaK";
                            DownloadUtil.removeFile(Keyspath,mContext);
                            String downloadFilelocation = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + cardDownloadData._id;
                            DownloadUtil.deleteRecursive(new File(downloadFilelocation));
                            manager.remove(cardDownloadData.mDownloadId);
                            manager.remove(cardDownloadData.mVideoDownloadId);
                            manager.remove(cardDownloadData.mAudioDownloadId);
                        } catch (Exception e) {
                            e.printStackTrace();
                            LoggerD.debugDownload("Excep in DownloadScreen" + e.toString());
                        }
                        if (position < mDownloadedMovies.size()) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.vf_txt_download_deleted));
                            mDownloadedMovies.remove(position);
                            removeAndSaveDownloadedData(cardDownloadData._id);
                            notifyDataSetChanged();
                        }
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
    }


    public class ViewHolder {
        ImageView mThumbnailMovie;
        TextView mGenres;
        TextView mTextViewTitle;
        CardDownloadData mVodCardData;
        TextView mDownloadPercentText;
        ProgressBar progressBar;
        TextView cancle_text;
        ImageView deleteButton;
        ImageView mPlayButton;
        TextView mStatusText;
        TextView videoLength;
        TextView videoType;
        TextView videoSize;

        public View mViewContainer;
        public LinearLayout mDownloadInProgressLinearLayout;
        public ImageView downloadingGIFAnim;
        public ImageView downloadbtnImage;
    }

    private void injectViews(ViewHolder mViewHolder, View convertView) {
        mViewHolder.mThumbnailMovie = (ImageView) convertView.findViewById(R.id.imageview_thumbnail);
        mViewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.textview_title);
        mViewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        mViewHolder.mDownloadPercentText = (TextView) convertView.findViewById(R.id.download_btn_status_percent_text);
        mViewHolder.mGenres = (TextView) convertView.findViewById(R.id.genres);
        mViewHolder.cancle_text = (TextView) convertView.findViewById(R.id.cancel_text);
        mViewHolder.deleteButton = (ImageView) convertView.findViewById(R.id.deleteIcon);
        mViewHolder.mStatusText = (TextView) convertView.findViewById(R.id.status_text);
        mViewHolder.videoLength = (TextView) convertView.findViewById(R.id.text_video_length);
        mViewHolder.videoType = (TextView) convertView.findViewById(R.id.text_video_type);
        mViewHolder.videoSize = (TextView) convertView.findViewById(R.id.text_video_size);
        mViewHolder.mViewContainer =  convertView.findViewById(R.id.layout_thumbnail);
        mViewHolder.mDownloadInProgressLinearLayout = (LinearLayout) convertView.findViewById(R.id.download_btn_layout);
        mViewHolder.downloadingGIFAnim = (ImageView) convertView.findViewById(R.id.downloading_gif_anim);
        if (mViewHolder.downloadingGIFAnim != null)
            mViewHolder.downloadingGIFAnim.setBackgroundColor(Color.TRANSPARENT); //for gif without background
        mViewHolder.mPlayButton = (ImageView) convertView.findViewById(R.id.download_pause_play);
        mViewHolder.downloadbtnImage = (ImageView) convertView.findViewById(R.id.carddetailbriefdescription_download_img);

    }

    private class DownloadManagerLister {

        DownloadManagerLister() {

        }


    }

    @Override
    public void DownloadProgress(CardData cardData, final CardDownloadData downloadData) {
        // TODO Auto-generated method downloadData
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mDownloadedMovies.size(); i++) {
                    CardDownloadData item = mDownloadedMovies.get(i);
                    if (downloadData._id.equalsIgnoreCase(item._id)) {
                        mDownloadedMovies.remove(i);
                        mDownloadedMovies.add(i, downloadData);
                        CardDownloadedDataList downloadList = ApplicationController.getDownloadData();
                        downloadList.mDownloadedList.put(downloadData._id, downloadData);
                        break;
                    }
                }
                notifyDataSetChanged();
            }
        });

    }

    private void removeAndSaveDownloadedData(String cardId) {
        LoggerD.debugDownload("-------******-----------");
        CardDownloadedDataList downloadlist = getDownloadData();
        if (downloadlist == null
                || downloadlist.mDownloadedList == null
                || downloadlist.mDownloadedList.isEmpty()) {
            LoggerD.debugDownload("empty downloads list");
            return;
        }
        for (String key : downloadlist.mDownloadedList.keySet()) {
            CardDownloadData downloadData = downloadlist.mDownloadedList.get(key);
            String downloadKey = downloadData._id;
            if(APIConstants.TYPE_HOOQ.equalsIgnoreCase(downloadData.partnerName)
                    || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(downloadData.partnerName)){
                downloadKey = downloadData.downloadKey;
            }
            if (downloadKey.equalsIgnoreCase(cardId)) {
                LoggerD.debugDownload("found data for key- " + key);
                downloadlist.mDownloadedList.remove(key);
                break;
            }
        }
        LoggerD.debugDownload("update download data in files");
        ApplicationController.setDownloadData(downloadlist);
        SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
        EventBus.getDefault().post(new DownloadDeleteEvent());
        LoggerD.debugDownload("-------******-----------");
    }



    private void refreshData() {
//        mHandler.remAoveCallbacks(notifyTask);
        mHandler.postDelayed(notifyTask, 1000);
    }

    Runnable notifyTask = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    private void updateDownloadData(String downloadKey, float progress) {
        if (downloadKey == null) {
            LoggerD.debugDownload("downloadKey is null");
            return;
        }
        CardDownloadedDataList downloadlist = getDownloadData();
        if (downloadlist == null
                || downloadlist.mDownloadedList == null
                || downloadlist.mDownloadedList.isEmpty()) {
            LoggerD.debugDownload("empty downloads");
            return;
        }
        for (CardDownloadData cardDownloadData : mDownloadedMovies) {
            if (downloadKey.equalsIgnoreCase(cardDownloadData.downloadKey)) {
                if (cardDownloadData.mPercentage == 100 && cardDownloadData.mCompleted) {
                    LoggerD.debugDownload("download completed downloadKey- " + downloadKey + " progress- " + progress);
                    break;
                }
//                long mb = (1024L * 1024L);
//                cardDownloadData.mDownloadedBytes = mediaCacheItem.getLocalFileSize() / mb;
//                cardDownloadData.mDownloadTotalSize = mediaCacheItem.getLocalFileSize() / mb;
                cardDownloadData.mPercentage = (int) progress;
                if (progress == 100) {
                    cardDownloadData.mCompleted = true;
                    cardDownloadData.mPercentage = 100;
                    LoggerD.debugDownload("showNotification downloadedData- " + cardDownloadData);
                    ApplicationController.setDownloadData(downloadlist);
                    SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
                    DownloadUtil.showNotification(mContext, cardDownloadData._id, cardDownloadData);
                }
                LoggerD.debugDownload("put downloadKey- " + downloadKey + " progress- " + progress);
                downloadlist.mDownloadedList.put(downloadKey, cardDownloadData);
                break;
            }
        }
        refreshData();
    }
}
