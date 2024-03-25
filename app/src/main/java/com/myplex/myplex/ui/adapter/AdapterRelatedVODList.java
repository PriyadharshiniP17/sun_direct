package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.security.MediaLinkEncrypted;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataVideosItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.CardResponseData;
import com.myplex.model.LocationInfo;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.download.DownloadManagerUtility;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.ui.views.SubscriptionPacksDialog;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phani on 12/12/2015.
 */
public class AdapterRelatedVODList extends BaseAdapter {

    private static final String TAG = AdapterRelatedVODList.class.getSimpleName();
    private final Context mContext;
    private final List<CardData> mRelatedVids;
    private final LayoutInflater mLayoutInflater;
    private String mContentType = null;
    private final List<VODCardDataHolder> mVodCardDataHolders = new ArrayList<>();
    private CardData parentCardData;


    public AdapterRelatedVODList(Context context, List<CardData> baseCardDataList, String contentType) {
        mContext = context;
        this.mRelatedVids = baseCardDataList;
        mLayoutInflater = LayoutInflater.from(mContext);
        mContentType = contentType;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int getCount() {
        if (mRelatedVids != null && mRelatedVids.size() >= 1)
            return mRelatedVids.size();
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public CardData getItem(int position) {
        if (mRelatedVids == null) return null;
        return mRelatedVids.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(CardData vodCard) {
        if (mRelatedVids != null) {
            mRelatedVids.add(vodCard);
            notifyDataSetChanged();
        }
    }

    public void addAll(List<CardData> listVodCard) {
        if (mRelatedVids != null
                && listVodCard != null) {
            mRelatedVids.addAll(listVodCard);
            notifyDataSetChanged();
        }
    }

    public List<CardData> getAllItems() {
        return mRelatedVids;
    }

    public void setParentCardData(CardData parent) {
        this.parentCardData = parent;
    }

    public class VODCardDataHolder {
        public CardData mVodCardData;
        public TextView mTextViewTitle;
        public TextView mTextViewVideoDuration;
        public TextView mTextViewGenre;
        public TextView mTextViewStartDate;
        public ImageView mImageViewDownloadButton;
        public ImageView mImageViewThumbnail;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        //Log.d(TAG, "registerDataSetObserver - observer");
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        VODCardDataHolder mViewHolder;
        if (convertView == null) {
            if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(mContentType)
                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(mContentType)) {
                convertView = mLayoutInflater.inflate(R.layout.listitem_related_vods_videos, parent, false);
            } else {
                convertView = mLayoutInflater.inflate(R.layout.listitem_related_vods_tvshows, parent, false);
            }
            mViewHolder = new VODCardDataHolder();
            mViewHolder.mImageViewThumbnail = (ImageView) convertView.findViewById(R.id.imageview_thumbnail);
            mViewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.textview_title);
            mViewHolder.mTextViewVideoDuration = (TextView) convertView.findViewById(R.id.textview_duration);
            mViewHolder.mImageViewDownloadButton = (ImageView) convertView.findViewById(R.id.imageview_play_alarm_download);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (VODCardDataHolder) convertView.getTag();
        }


        mViewHolder.mImageViewDownloadButton.setVisibility(View.GONE);
        mViewHolder.mVodCardData = mRelatedVids.get(position);

        try {
            if (mViewHolder.mVodCardData != null) {

                if (mViewHolder.mVodCardData.generalInfo != null) {
                    if ((APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mContentType)
                            || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(mContentType))
                            && ApplicationController.SHOW_PLAYER_LOGS
                            && mViewHolder.mVodCardData.generalInfo.title != null
                            && mViewHolder.mVodCardData.content != null
                            && mViewHolder.mVodCardData.content.serialNo != null
                            && !mViewHolder.mVodCardData.content.serialNo.isEmpty()) {
                        mViewHolder.mTextViewTitle.setText(mViewHolder.mVodCardData.content.serialNo + " " + mViewHolder.mVodCardData.generalInfo.title);
                    } else if (mViewHolder.mVodCardData.generalInfo.title != null) {
                        mViewHolder.mTextViewTitle.setText(mViewHolder.mVodCardData.generalInfo.title);
                    }

                    if (mViewHolder.mVodCardData != null
                            && (APIConstants.TYPE_VOD.equalsIgnoreCase(mViewHolder.mVodCardData.generalInfo.type)
                            || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mViewHolder.mVodCardData.generalInfo.type)
                            || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(mContentType)
                            || APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(mViewHolder.mVodCardData.generalInfo.type))) {
                        mViewHolder.mImageViewDownloadButton.setVisibility(View.VISIBLE);
                        mViewHolder.mImageViewDownloadButton.setImageResource(R.drawable.thumbnail_play_icon);
                        /*if (mViewHolder.mVodCardData.generalInfo.isDownloadable) {//6411
                            mViewHolder.mImageViewDownloadButton.setTag(mViewHolder);
                            mViewHolder.mImageViewDownloadButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.download_default));
                            updateDownloadStatus(mViewHolder.mVodCardData, null, mViewHolder);
                            mViewHolder.mImageViewDownloadButton.setOnClickListener(mDownloadListener);
                        } else {
                            mViewHolder.mImageViewDownloadButton.setImageResource(R.drawable.oncard_play_icon);
                        }*/
                    }
                }
                if (mViewHolder.mVodCardData.content != null) {

                    if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(mContentType)
                            || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(mContentType)
                            || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(mContentType)
                            || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(mContentType)) {
                        if (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mViewHolder.mVodCardData.generalInfo.type)) {
                            mViewHolder.mTextViewVideoDuration.setVisibility(View.GONE);
                            if (mViewHolder.mVodCardData.content != null
                                    && mViewHolder.mVodCardData.content.duration != null) {
                                mViewHolder.mTextViewVideoDuration.setVisibility(View.VISIBLE);
                                mViewHolder.mTextViewVideoDuration.setText(Util.getDurationWithFormat(mViewHolder.mVodCardData.content.duration));
                            }
                        } else {
                            if (!TextUtils.isEmpty(mViewHolder.mVodCardData.content.duration)) {
                                mViewHolder.mTextViewVideoDuration.setText(Util.getDuration(mViewHolder.mVodCardData.content.duration));
                                mViewHolder.mTextViewVideoDuration.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        if (!TextUtils.isEmpty(mViewHolder.mVodCardData.content.duration)) {
                            mViewHolder.mTextViewVideoDuration.setText(Util.getDDMMYYYYUTC(mViewHolder.mVodCardData.content.releaseDate));
                            mViewHolder.mTextViewVideoDuration.setVisibility(View.VISIBLE);
                        }
                    }
                }
                String imageUrl = getImageLink(mViewHolder.mVodCardData, mViewHolder.mImageViewThumbnail);
                mViewHolder.mImageViewThumbnail.setImageResource(R.drawable
                        .black);
                if (!TextUtils.isEmpty(imageUrl)) {
/*                    Picasso.with(mContext).load(imageUrl).error(R.drawable
                            .epg_thumbnail_default).placeholder(R.drawable
                            .epg_thumbnail_default).into(mViewHolder.mImageViewThumbnail);*/
                    PicassoUtil.with(mContext).load(imageUrl,mViewHolder.mImageViewThumbnail,R.drawable
                            .black);
                }
                if (mViewHolder.mVodCardData != null
                        && mViewHolder.mVodCardData.generalInfo != null)
                    LoggerD.debugHooqVstbLog("contentName- " + mViewHolder.mVodCardData.generalInfo.title + " imageUrl- " + imageUrl);
            }

        } catch (Exception e) {
            //Log.d(TAG, "" + e.getMessage());
            e.printStackTrace();
        }
        return convertView;
    }

    private String getImageLink(CardData cardData, ImageView imageViewThumbnail) {
        if (cardData == null
                || cardData.images == null
                || cardData.images.values== null) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,};
        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : cardData.images.values) {
//                LoggerD.debugHooqVstbLog("imageItem: imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)) {
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)
                            || (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile))) {
                        if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)) {
                            imageViewThumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            imageViewThumbnail.invalidate();
                            return imageItem.link;
                        }
                        imageViewThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageViewThumbnail.invalidate();
                        return imageItem.link;
                    }
                }
            }
        }
        if (cardData.images != null
                && cardData.images.values != null && cardData.images.values.size() > 0) {
            for (CardDataImagesItem imageItem : cardData.images.values) {
                if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)
                        && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
                    if (imageItem.link == null
                            || imageItem.link.compareTo(APIConstants.IMAGE_TYPE_NO_IMAGE) == 0) {
                        return null;
                    } else if (imageItem.link != null) {
                        return imageItem.link;
                    }
                    break;
                }
            }
        }
        return null;
    }

    private void getDownloadbleLink(final VODCardDataHolder vodCardDataHolder) {
        // TODO Auto-generated method stub
        //Log.d(TAG, "getDownloadbleLink fetching download link");
        if (vodCardDataHolder == null
                || vodCardDataHolder.mVodCardData == null
                || vodCardDataHolder.mVodCardData._id == null) {
            showDownloadError(null);
            return;
        }

        MediaLinkEncrypted.Params mediaLinkparams = null;

        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        mediaLinkparams = new MediaLinkEncrypted.Params(vodCardDataHolder.mVodCardData._id, SDKUtils.getInternetConnectivity(mContext), null, locationInfo);
        MediaLinkEncrypted mMedialLink = new MediaLinkEncrypted(mediaLinkparams, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                //Log.d(TAG, "onResponse response - " + response);
                if (response == null || response.body() == null) {
                    showDownloadError(null);
                    return;
                }

                if (response.body().results == null
                        || response.body().results.size() == 0) {
                    showDownloadError(null);
                    return;
                }
                if(response.body().code == 402){
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }

                //Log.d(TAG, "onResponse message- " + response.body().message);
                for (CardData data : response.body().results) {
                    if (data.videos != null && data.videos.values != null
                            && data.videos.values.size() > 0) {
                        if (!"SUCCESS".equalsIgnoreCase(data.videos.status)) {

                            if (data.videos.message != null && "ERR_USER_NOT_SUBSCRIBED".equalsIgnoreCase(data.videos.status)) {
                                // AlertDialogUtil.showToastNotification(data.videos.message);
                                fetchPackageData(vodCardDataHolder.mVodCardData, false);
                                return;
                            } else if (data.videos.message != null && "ERR_PACKAGES_NOT_DEFINED".equalsIgnoreCase(data.videos.status)) {
                                showDownloadError(data.videos.message);
                                return;
                            }
                        }
                        for (CardDataVideosItem videoItems : data.videos.values) {
                            if (APIConstants.VIDEO_TYPE_DOWNLOAD.equalsIgnoreCase(videoItems.type)) {
                                DownloadUtil.DownloadStatus download_status = DownloadUtil.startDownload(videoItems.link, vodCardDataHolder.mVodCardData, mContext,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null); //TODO: Commneted for now
                                if (download_status == DownloadUtil.DownloadStatus.NOT_AVAILABLE_IN_DOWNLOADS) {
                                    mVodCardDataHolders.add(vodCardDataHolder);
                                    mDownloadProgressManager = FetchDownloadProgress.getInstance(mContext);
                                    mDownloadProgressManager.addProgressListener(mDownloadProgressStatus);
                                    mDownloadProgressManager.startPolling();
                                    Analytics.gaDownloadVideos(vodCardDataHolder.mVodCardData.generalInfo.title);
                                } else if (download_status == DownloadUtil.DownloadStatus.ALREADY_FILE_EXISTS) {
                                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.download_video_already_downloaded));
                                }
//                                return videoItems.link;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                //Log.d(TAG, "onResponse message- " + t);
                showDownloadError(null);
            }
        });
        APIService.getInstance().execute(mMedialLink);
        notifyDataSetChanged();

    }

    String _id = null;

    private void fetchPackageData(CardData mVodCardData, boolean isCacheRequest) {

        if (mVodCardData != null
                && mVodCardData.generalInfo != null
                && APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mVodCardData.generalInfo.type)
                && mVodCardData.globalServiceId != null) {
            _id = mVodCardData.globalServiceId;
        } else if (mVodCardData._id != null) {
            _id = mVodCardData._id;
        }
        new CacheManager().getCardDetails(_id, isCacheRequest, new CacheManager.CacheManagerCallback() {
            @Override
            public void OnCacheResults(List<CardData> dataList) {
                if (null == dataList) {
//                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
                    return;
                }
                for (CardData cardData : dataList) {
                    if (null != cardData
                            && null != cardData._id) {
                        if (cardData._id.equalsIgnoreCase(_id)) {
                            SDKUtils.getCardExplorerData().cardDataToSubscribe = cardData;
                            showPackagesPopup(cardData);
                        }
                    }
                }
            }

            @Override
            public void OnOnlineResults(List<CardData> dataList) {
                if (null == dataList) {
//                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
                    return;
                }
                for (CardData cardData : dataList) {
                    if (null != cardData
                            && null != cardData._id) {
                        if (cardData._id.equalsIgnoreCase(_id)) {
                            SDKUtils.getCardExplorerData().cardDataToSubscribe = cardData;
                            showPackagesPopup(cardData);
                        }
                    }
                }
            }

            @Override
            public void OnOnlineError(Throwable error, int errorCode) {
//                AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
            }
        });
    }

    private void showPackagesPopup(CardData cardData) {
        if (cardData != null
                && cardData.packages != null
                && cardData.packages.size() > 0) {
            SubscriptionPacksDialog subscriptionPacksDialog = new SubscriptionPacksDialog(mContext);
            subscriptionPacksDialog.showDialog(cardData);
        }
    }

    private final View.OnClickListener mDownloadListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            // TODO Auto-generated method stub
            v.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    v.setEnabled(true);
                }
            }, 3 * 1000);
            v.setEnabled(false);
            if (v.getTag() instanceof VODCardDataHolder) {
                String tabName = ((MainActivity) mContext).getSelectedPageName();
                CleverTap.eventClicked(tabName == null ? "NA" : tabName, CleverTap.ACTION_DOWNLOAD);
                VODCardDataHolder mVODCardDataHolder = (VODCardDataHolder) v.getTag();
                DownloadManagerUtility downloadManagerUtility = new DownloadManagerUtility(mContext);
                downloadManagerUtility.initializeDownload(new ContentDownloadEvent(mVODCardDataHolder.mVodCardData,null,parentCardData), new FragmentCardDetailsDescription.DownloadStatusListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onDownloadStarted() {

                    }

                    @Override
                    public void onDownloadInitialized() {

                    }

                    @Override
                    public void onFailure(String message) {
                    }

                    @Override
                    public void onDownloadCancelled() {

                    }

                    @Override
                    public boolean isToShowDownloadButton() {
                        return false;
                    }
                });
                /*if (Util.hasSpaceAvailabeToDownload(APIConstants.VODVIDEOQUALTYSD, mContext)) {
                    getDownloadbleLink(mVODCardDataHolder);
                } else {
                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.play_download_insufficent_memory_vod));
                }*/
            }
        }
    };
    private FetchDownloadProgress mDownloadProgressManager;
    private Handler mHandler;
    private final FetchDownloadProgress.DownloadProgressStatus mDownloadProgressStatus = new FetchDownloadProgress.DownloadProgressStatus() {

        @Override
        public void DownloadProgress(final CardData cardData,
                                     final CardDownloadData downloadData) {
            // TODO Auto-generated method stub
            mHandler.postDelayed(new Runnable() {
                /**
                 * When an object implementing interface <code>Runnable</code> is used
                 * to create a thread, starting the thread causes the object's
                 * <code>run</code> method to be called in that separately executing
                 * thread.
                 * <p>
                 * The general contract of the method <code>run</code> is that it may
                 * take any action whatsoever.
                 *
                 * @see Thread#run()
                 */
                @Override
                public void run() {
                    updateDownloadStatus(cardData, downloadData, null);

                }
            },1000);
        }

    };

    private void updateDownloadStatus(CardData cardData,
                                      CardDownloadData downloadData, VODCardDataHolder mDataHolder) {
        // TODO Auto-generated method stub
        CardDownloadData localDownloadData;
        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();
        if (downloadlist == null) {
            return;
        }
        localDownloadData = downloadlist.mDownloadedList.get(cardData._id);
        if (localDownloadData == null) {
            return;
        }
        if (downloadData != null) {
            localDownloadData = downloadData;
        }
        for (VODCardDataHolder dataHolder : mVodCardDataHolders) {
            if (dataHolder.mVodCardData != null
                    && dataHolder.mVodCardData._id != null
                    && dataHolder.mVodCardData._id.equalsIgnoreCase(cardData._id)
                    && downloadData != null) {
                mDataHolder = dataHolder;
            }
        }
        if (mDataHolder == null) return;
        if (!localDownloadData.mCompleted) {
            if (localDownloadData.mPercentage == 0) {
                mDataHolder.mImageViewDownloadButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.download_progress_1));
            } else {
                int downloadPercent = localDownloadData.mPercentage / 10;
                downloadPercent = Math.abs(downloadPercent);
                if (downloadPercent >= 1 && downloadPercent <= 8) {
                    mDataHolder.mImageViewDownloadButton.setImageResource(mContext.getResources().getIdentifier("download_progress_" + downloadPercent, "drawable", mContext.getPackageName()));
                }
            }
        } else {
            if (localDownloadData.mPercentage == 0) {
                mDataHolder.mImageViewDownloadButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.download_default));
            } else {
                mDataHolder.mImageViewDownloadButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.download_complete));
                notifyDataSetChanged();
            }

        }


    }

    public void showDownloadError(String message) {
        if (message == null) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.download_not_available));
            return;
        }
        AlertDialogUtil.showToastNotification(message);
    }
}
