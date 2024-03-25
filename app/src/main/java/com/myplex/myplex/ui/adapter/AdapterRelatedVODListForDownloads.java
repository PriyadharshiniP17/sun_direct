package com.myplex.myplex.ui.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APIConstants;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CardDataImages;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.LanguageTitleData;
import com.myplex.model.PublishingHouse;
import com.myplex.model.SeasonData;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.download.Decompress;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.views.gifview.GifAnimationDrawable;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.myplex.myplex.ApplicationController.getApplicationConfig;
import static com.myplex.myplex.ApplicationController.getDownloadData;

/**
 * Created by apalya on 3/4/2017.
 */

public class AdapterRelatedVODListForDownloads extends BaseAdapter implements FetchDownloadProgress.DownloadProgressStatus {

    private static final String TAG = AdapterMovieList.class.getSimpleName();
    private final DownloadManagerLister downloadManagerListener;
    private int seasonPosition;
    private Context mContext;
    private List<CardDownloadData> mDownloadedMovies;
//    private WaveHelper mWaveHelper;
    private FetchDownloadProgress mDownloadProgressManager;
    private Handler mHandler;
    CardDownloadData parentCardDownloadData;
    CardDownloadData cardDownloadData;
    private boolean showProgress;
    private FragmentCloseListenerListener mFragmentCloseListener;
    private final ErosNowDownloadManager.UnzipProcessListener mUnzipProcessListener = new ErosNowDownloadManager.UnzipProcessListener() {
        @Override
        public void onCompletion(CardDownloadData cardDownloadData) {
            notifyDataSetChanged();
        }

        @Override
        public void onFailure() {

        }
    };
    public void updateSeasonData(int seasonPosition){
        this.seasonPosition = seasonPosition;
        if(this.parentCardDownloadData == null
                || this.parentCardDownloadData.tvSeasonsList == null
                || this.parentCardDownloadData.tvSeasonsList.isEmpty()){
            if(this.parentCardDownloadData == null
                    || this.parentCardDownloadData.tvEpisodesList == null
                    || this.parentCardDownloadData.tvEpisodesList.isEmpty()){
                return;
            }
            mDownloadedMovies = this.parentCardDownloadData.tvEpisodesList;
            return;
        }
        mDownloadedMovies = this.parentCardDownloadData.tvSeasonsList.get(seasonPosition).tvEpisodesList;
    }
    public AdapterRelatedVODListForDownloads(Context context, boolean showProgress, CardDownloadData cardDownloadData, int seasonPosition, FragmentCloseListenerListener fragmentCloseListenerListener) {
        mContext = context;
        mDownloadedMovies = new ArrayList<>();
        mHandler = new Handler(Looper.getMainLooper());
        mDownloadProgressManager = FetchDownloadProgress.getInstance(mContext);
        mDownloadProgressManager.addProgressListener(this);
        mDownloadProgressManager.startPolling();
        this.showProgress = showProgress;
        this.parentCardDownloadData = cardDownloadData;
        this.seasonPosition = seasonPosition;
        this.mFragmentCloseListener = fragmentCloseListenerListener;
        downloadManagerListener = new DownloadManagerLister();
        ErosNowDownloadManager.getInstance(mContext).initUnzipManagerListener(mUnzipProcessListener);

        updateSeasonData(seasonPosition);
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

    public void add(List<CardDownloadData> vodCardList) {
        mDownloadedMovies.addAll(vodCardList);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.downloads_for_tvshows, parent, false);
            mViewHolder = new ViewHolder();
            injectViews(mViewHolder, convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        if (mDownloadedMovies == null) {
            return convertView;
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
        mViewHolder.mTextViewTitle.setText(cardDownloadData.title);
//        mViewHolder.mGenres.setText(cardDownloadData.genres);
//        mViewHolder.mLanguages.setText(cardDownloadData.time_languages);
        if (!TextUtils.isEmpty(cardDownloadData.duration)) {
            mViewHolder.mGenres.setText(cardDownloadData.duration);
            mViewHolder.mGenres.setVisibility(VISIBLE);
        }
        String totalSize = Double.toString(Math.floor(cardDownloadData.mDownloadedBytes)) +" MB";
        mViewHolder.videoSize.setText(totalSize);
        if (!showProgress) {
            mViewHolder.mPlayButton.setVisibility(INVISIBLE);
            mViewHolder.deleteButton.setVisibility(INVISIBLE);
            mViewHolder.cancle_text.setVisibility(INVISIBLE);
            mViewHolder.mDownloadPercentText.setVisibility(INVISIBLE);
            mViewHolder.mStatusText.setVisibility(INVISIBLE);
            return convertView;
        }
        mViewHolder.videoLength.setVisibility(GONE);
        mViewHolder.videoSize.setVisibility(GONE);
        mViewHolder.videoType.setVisibility(GONE);
        mViewHolder.mPlayButton.setTag(position);
        mViewHolder.mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(view.getTag() instanceof Integer)) {
                    return;
                }
                int pos = (int)view.getTag();
                showDetailsFragment(pos);
            }
        });
        mViewHolder.deleteButton.setTag(position);
        mViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(view.getTag() instanceof Integer)) {
                    return;
                }
                int pos = (int)view.getTag();
                deleteDownloads(pos);
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


        if (cardDownloadData != null) {
            if (cardDownloadData.mCompleted) {
                if (cardDownloadData.mPercentage == 100) {
                    if (APIConstants.isErosNowContent(cardDownloadData)
                            && cardDownloadData.zipStatus < CardDownloadData.STATUS_FILE_UNZIPPED
                            && cardDownloadData.mCompleted) {
                        if (cardDownloadData.zipStatus == CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE
                                || cardDownloadData.zipStatus == CardDownloadData.STATUS_FAILED_TO_AQUIRE_DRM_LICENSE) {
                            showDownloadFailedUI(mViewHolder, cardDownloadData);
                            return convertView;
                        }
                        showDownloadingUI(mViewHolder, cardDownloadData);
                        return convertView;
                    }
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

    private void injectViews(ViewHolder mViewHolder, View convertView) {
        mViewHolder.mThumbnailMovie = (ImageView) convertView.findViewById(R.id.imageview_thumbnail);
        mViewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.textview_title);
        mViewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        mViewHolder.mGenres = (TextView) convertView.findViewById(R.id.genres);
        mViewHolder.cancle_text = (TextView) convertView.findViewById(R.id.cancel_text);
        mViewHolder.deleteButton = (ImageView) convertView.findViewById(R.id.deleteIcon);
        mViewHolder.mStatusText = (TextView) convertView.findViewById(R.id.status_text);
        mViewHolder.videoLength = (TextView) convertView.findViewById(R.id.text_video_length);
        mViewHolder.videoType = (TextView) convertView.findViewById(R.id.text_video_type);
        mViewHolder.videoSize = (TextView) convertView.findViewById(R.id.text_video_size);
        mViewHolder.mViewContainer = convertView.findViewById(R.id.layout_thumbnail);
        mViewHolder.mPlayButton = (ImageView) convertView.findViewById(R.id.download_pause_play);
        mViewHolder.mDownloadInProgressLinearLayout = (LinearLayout) convertView.findViewById(R.id.download_btn_layout);
        mViewHolder.mDownloadPercentText = (TextView) convertView.findViewById(R.id
                .download_btn_status_percent_text);
        mViewHolder.downloadingGIFAnim = (ImageView) convertView.findViewById(R.id.downloading_gif_anim);
        if (mViewHolder.downloadingGIFAnim != null)
            mViewHolder.downloadingGIFAnim.setBackgroundColor(Color.TRANSPARENT); //for gif without background
        mViewHolder.downloadbtnImage = (ImageView) convertView.findViewById(R.id.carddetailbriefdescription_download_img);

    }


    private void showDetailsFragment(int position) {
        final CardDownloadData cardDownloadData = mDownloadedMovies.get(position);
        CardData cardData = generateCardData(cardDownloadData);
        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        args.putString(CardDetails.PARAM_CARD_ID, cardData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putString(CardDetails.PARAM_PARTNER_ID, cardData.generalInfo.partnerId);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_DOWNLOADED_VIDEOS);
        args.putString(Analytics.PROPERTY_SOURCE_DETAILS, cardData.generalInfo.title);

        ((MainActivity) mContext).showDetailsFragment(args, cardData);
    }

    private CardData generateCardData(CardDownloadData cardDownloadData) {
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
        generalInfo.description = cardDownloadData.description;
        generalInfo.briefDescription = cardDownloadData.briefDescription;
        generalInfo.partnerId = cardDownloadData.downloadKey;
        cardData.publishingHouse = new PublishingHouse();
        cardData.publishingHouse.publishingHouseName = cardDownloadData.partnerName;
        cardData.isDownloadDataOnExternalStorage = !cardDownloadData.isStoredInternally;
/*
        cardData.content = new CardDataContent();
        if (null != cardData.content) {
            cardData.content.genre = new ArrayList<>();
            CardDataGenre genre = new CardDataGenre();
            genre.name = cardDownloadData.genres;
            cardData.content.genre.add(genre);

            cardData.content.language = new ArrayList<>();
            cardData.content.language.add(cardDownloadData.time_languages);
            cardData.content.releaseDate = cardDownloadData.releaseDate;

            cardData.content.duration = cardDownloadData.duration;
        }
*/
        cardData.content = cardDownloadData.content;

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
        if(APIConstants.isHooqContent(cardData)){
            cardData.localFilePath = cardDownloadData.hooqCacheId;
        }
        cardData._id = cardDownloadData._id;
        cardData.generalInfo = generalInfo;

        cardData.images = images;

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
        new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle)
//                .setTitle(R.string.vf_download_state_deleted_text)
                .setMessage(mContext.getString(R.string.vf_txt_delete_download) + " " +itemType + " ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        String status = CleverTap.PROPERTY_DOWNLOAD_DELETED;
                        if (cardDownloadData != null
                                && cardDownloadData.mPercentage < 100) {
                            status = CleverTap.PROPERTY_DOWNLOAD_CANCELED;
                        }
                        CleverTap.eventDownload(cardDownloadData, status);

                        if (APIConstants.isHungamaContent(cardDownloadData)) {
                            LoggerD.debugDownload("deleteDownload downloadKey- " + cardDownloadData.downloadKey);
                        }
                        DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                        try {
                            Decompress.getInstance(mContext).clearTasks();
                            String downloadFilelocation = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + cardDownloadData._id;
                            DownloadUtil.deleteRecursive(new File(downloadFilelocation));
                            String Keyspath = mContext.getExternalFilesDirs(null) + File.separator + DownloadUtil.downloadVideosStoragePath + cardDownloadData._id + File.separator + "metaK";
                            DownloadUtil.removeFile(Keyspath,mContext);
                            manager.remove(cardDownloadData.mDownloadId);
                            manager.remove(cardDownloadData.mVideoDownloadId);
                            manager.remove(cardDownloadData.mAudioDownloadId);
                        } catch (Exception e) {
                            e.printStackTrace();
                            LoggerD.debugDownload("Excep in DownloadScreen" + e.toString());
                        }
                        if (position < mDownloadedMovies.size()) {
                            Toast.makeText(mContext, mContext.getString(R.string.vf_txt_download_deleted), Toast.LENGTH_SHORT).show();
                            mDownloadedMovies.remove(position);
                            updateCardDownloadedData();
                            notifyDataSetChanged();
                        }
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        })
                .show();


    }

    private void updateCardDownloadedData() {
        CardDownloadedDataList downloadlist = getDownloadData();
        if (downloadlist == null || parentCardDownloadData == null) {
            return;
        }
        String downloadKey = parentCardDownloadData._id;
        if (mDownloadedMovies.isEmpty()) {
            if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(parentCardDownloadData.partnerName)) {
                downloadKey = parentCardDownloadData.downloadKey;
            }

            if (!checkSeasonsAreAvailable()) {
                if (downloadlist != null
                        && !downloadlist.mDownloadedList.isEmpty()) {
                    downloadlist.mDownloadedList.remove(downloadKey);
                }
                ApplicationController.setDownloadData(downloadlist);
                SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
                if (mFragmentCloseListener != null) {
                    mFragmentCloseListener.onCloseFragment();
                }
                return;
            }
        }

        if (this.parentCardDownloadData.tvSeasonsList == null
                || this.parentCardDownloadData.tvSeasonsList.isEmpty()) {
            if (this.parentCardDownloadData.tvEpisodesList != null
                    && !this.parentCardDownloadData.tvEpisodesList.isEmpty()) {
                parentCardDownloadData.tvEpisodesList = mDownloadedMovies;
                ApplicationController.setDownloadData(downloadlist);
                SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
                return;
            }
        }

        this.parentCardDownloadData.tvSeasonsList.get(seasonPosition).tvEpisodesList = mDownloadedMovies;
        if (seasonPosition < this.parentCardDownloadData.tvSeasonsList.size()
                && mDownloadedMovies.isEmpty()) {
            this.parentCardDownloadData.tvSeasonsList.remove(seasonPosition);
            if (mFragmentCloseListener != null) {
                mFragmentCloseListener.updateSeasonData();
            }
        }
        downloadlist.mDownloadedList.put(downloadKey, parentCardDownloadData);
        ApplicationController.setDownloadData(downloadlist);
        SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
        return;
    }

    private boolean checkSeasonsAreAvailable() {
        if (this.parentCardDownloadData.tvSeasonsList == null
                || this.parentCardDownloadData.tvSeasonsList.isEmpty()) {
            return false;
        }
        for (SeasonData seasonData: this.parentCardDownloadData.tvSeasonsList) {
            if(!seasonData.tvEpisodesList.isEmpty()) return true;
        }
        return false;
    }



    public class ViewHolder {
        ImageView mThumbnailMovie;
        TextView mGenres;
        TextView mTextViewTitle;
        CardDownloadData mVodCardData;
        ProgressBar progressBar;
        TextView cancle_text;
        ImageView deleteButton;
        ImageView mPlayButton;
        TextView mStatusText;
        TextView videoLength;
        TextView videoType;
        TextView videoSize;
        public View mViewContainer;
        public TextView mDownloadPercentText;
        public LinearLayout mDownloadInProgressLinearLayout;
        public ImageView downloadingGIFAnim;
        public ImageView downloadbtnImage;
    }


    private class DownloadManagerLister  {

        DownloadManagerLister() {

        }

           }

    @Override
    public void DownloadProgress(CardData cardData, CardDownloadData downloadData) {
        // TODO Auto-generated method stub
        if (parentCardDownloadData == null) return;
        CardDownloadedDataList downloadList = ApplicationController.getDownloadData();
        parentCardDownloadData = downloadList.mDownloadedList.get(parentCardDownloadData.downloadKey);
        updateSeasonData(seasonPosition);
        refreshData();
    }


    public interface FragmentCloseListenerListener {
        void onCloseFragment();
        void updateSeasonData();
    }


    private void updateDownloadData(String downloadKey, float progress) {
        if (downloadKey == null) {
            return;
        }
        CardDownloadedDataList downloadlist = getDownloadData();
        String downloadedItemId = downloadKey;
        if (downloadlist == null
                || downloadlist.mDownloadedList == null
                || downloadlist.mDownloadedList.isEmpty()) {
            return;
        }
        for (CardDownloadData cardDownloadData : mDownloadedMovies) {
            if (downloadedItemId.equalsIgnoreCase(cardDownloadData.downloadKey)) {
                if (cardDownloadData.mPercentage == 100 && cardDownloadData.mCompleted) {
                    break;
                }
                cardDownloadData.mPercentage = (int) progress;
                long mb = (1024L * 1024L);
//                cardDownloadData.mDownloadedBytes = mediaCacheItem.getLocalFileSize() / mb;
//                cardDownloadData.mDownloadTotalSize = mediaCacheItem.getLocalFileSize() / mb;
                if (progress == 100) {
                    cardDownloadData.mCompleted = true;
                    LoggerD.debugDownload("showNotification downloadedData- " + cardDownloadData);
                    parentCardDownloadData.tvSeasonsList.get(seasonPosition).tvEpisodesList = mDownloadedMovies;
                    downloadlist.mDownloadedList.put(parentCardDownloadData.downloadKey, parentCardDownloadData);
                    ApplicationController.setDownloadData(downloadlist);
                    SDKUtils.saveObject(downloadlist, getApplicationConfig().downloadCardsPath);
                    refreshData();
                    DownloadUtil.showNotification(mContext, cardDownloadData._id, cardDownloadData);
                }
                break;
            }
        }
        if (parentCardDownloadData == null) {
            return;
        }
        if (parentCardDownloadData.tvSeasonsList == null
                || parentCardDownloadData.tvSeasonsList.isEmpty()) {
            if (parentCardDownloadData.tvEpisodesList == null
                    || parentCardDownloadData.tvEpisodesList.isEmpty()) {
                return;
            }
            parentCardDownloadData.tvEpisodesList = mDownloadedMovies;
            return;
        }
        if (parentCardDownloadData.tvSeasonsList.get(seasonPosition) != null
                && (parentCardDownloadData.tvSeasonsList.get(seasonPosition).tvEpisodesList == null
                || parentCardDownloadData.tvSeasonsList.get(seasonPosition).tvEpisodesList.isEmpty())) {
            return;
        }
        parentCardDownloadData.tvSeasonsList.get(seasonPosition).tvEpisodesList = mDownloadedMovies;
        downloadlist.mDownloadedList.put(parentCardDownloadData.downloadKey, parentCardDownloadData);
        refreshData();
    }


    private void showDownloadCompleted(ViewHolder mViewHolder, CardDownloadData cardDownloadData, int position) {
        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardDownloadData.ItemType)
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
        }
        mViewHolder.mStatusText.setVisibility(GONE);
        mViewHolder.mPlayButton.setVisibility(VISIBLE);
        mViewHolder.downloadbtnImage.setVisibility(View.VISIBLE);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(GONE);
        mViewHolder.downloadbtnImage.setImageResource(R.drawable.episode_list_download_icon_complete);
        mViewHolder.deleteButton.setVisibility(VISIBLE);
    }

    private void showDownloadingUI(final ViewHolder mViewHolder, CardDownloadData cardDownloadData) {
        mViewHolder.downloadbtnImage.setVisibility(View.GONE);
        mViewHolder.mPlayButton.setVisibility(GONE);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(VISIBLE);
        mViewHolder.mDownloadPercentText.setVisibility(View.VISIBLE);
        mViewHolder.mViewContainer.setOnClickListener(null);
        if (cardDownloadData != null) {
            if (cardDownloadData.mPercentage < 0) {
                cardDownloadData.mPercentage = 0;
            }
            LoggerD.debugDownload("progress- " + cardDownloadData.mPercentage);
            mViewHolder.mDownloadPercentText.setText(String.valueOf((int) cardDownloadData.mPercentage) + "%");
        }
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
            mViewHolder.downloadingGIFAnim.setTag(gifAnim);
            mViewHolder.downloadingGIFAnim.setImageDrawable(gifAnim);
            /*mViewHolder.downloadingGIFAnim.post(new WeakRunnable<AdapterRelatedVODListForDownloads>(this) {
                @Override
                protected void safeRun(AdapterRelatedVODListForDownloads adapterEpisode) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        String status = mContext.getResources().getString(R.string.downloading);

        if (!ConnectivityUtil.isConnected(mContext)) {
            mViewHolder.downloadingGIFAnim.setImageResource(R.drawable.carddetaildescription_download_icon_without_text);
            status = mContext.getResources().getString(R.string.download_state_paused_text);
        }
        mViewHolder.mStatusText.setText(status);
        mViewHolder.downloadingGIFAnim.setVisibility(View.VISIBLE);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(View.VISIBLE);
    }

    private void showDownloadFailedUI(ViewHolder mViewHolder, CardDownloadData cardDownloadData) {
        mViewHolder.mStatusText.setVisibility(VISIBLE);
        mViewHolder.mPlayButton.setVisibility(GONE);
        mViewHolder.downloadbtnImage.setVisibility(View.VISIBLE);
        mViewHolder.downloadbtnImage.setImageResource(R.drawable.description_download_broken);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(GONE);
        mViewHolder.mViewContainer.setOnClickListener(null);
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


    private void refreshData() {
//        mHandler.removeCallbacks(notifyTask);
        mHandler.postDelayed(notifyTask, 1000);
    }

    Runnable notifyTask = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };
}
