package com.myplex.myplex.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.OfferResponseData;
import com.myplex.model.SeasonData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.download.DownloadManagerUtility;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsPlayer;
import com.myplex.myplex.ui.fragment.PackagesFragment;
import com.myplex.myplex.ui.views.gifview.GifAnimationDrawable;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.myplex.utils.WeakRunnable;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;
import static com.myplex.myplex.ui.fragment.CardDetails.PARAM_RELATED_CARD_DATA;
import static com.myplex.myplex.ui.fragment.PackagesFragment.PARAM_SUBSCRIPTION_TYPE_NONE;


/**
 * Created by Phani on 12/12/2015.
 */
public class AdapterEpisode extends RecyclerView.Adapter<AdapterEpisode.EpisodeViewHolder>  implements FetchDownloadProgress.DownloadProgressStatus {
    private static final String TAG = AdapterEpisode.class.getSimpleName();
    private static final int MAX_COUNT_FOR_PREF_IDS = 60;
    private static final int DESCRIPTION_COLLAPSED = 1;
    private static final int DESCRIPTION_EXPANDED= 0;
    private static final int ITEM_TYPE_LOADING = 2;
    private static final int ITEM_DATA = 1;
    private final HandlerThread mHandlerThread;
    private final Handler mPollingThreadHandler;
    private Context mContext;
    private List<CardData> mListEpisodes;
    private View.OnClickListener mPlayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof CardData) {
                CardData programData = (CardData) v.getTag();
                CacheManager.setSelectedCardData(programData);

                Bundle args = new Bundle();
                args.putString(CardDetails.PARAM_CARD_ID, programData.globalServiceId);
                args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                if (null != programData.startDate
                        && null != programData.endDate) {
                    Date startDate = Util.getDate(programData.startDate);
                    Date endDate = Util.getDate(programData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                    }
                }
                args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_DETAILS);
                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, Analytics.VALUE_SOURCE_DETAILS_EPG);

                ((BaseActivity)mContext).showDetailsFragment(args, programData);
//                ((BaseActivity)mContext).pushFragment(CardDetails.newInstance(args));
            }

        }
    };
    private CardData mTVShowCardData;


    private ErosNowDownloadManager.UnzipProcessListener mUnzipProgressListener = new ErosNowDownloadManager.UnzipProcessListener() {
        @Override
        public void onCompletion(CardDownloadData cardDownloadData) {
            LoggerD.debugDownload("unzip done for ");
            DownloadProgress(null, cardDownloadData);
        }

        @Override
        public void onFailure() {

        }
    };
    private String mCurrentSeasonName;
    private Handler mHandler;
    private String mSearchQuery;
    private CardData mRelatedVODData;
    private CarouselInfoData mCarouselInfoData;
    private RecyclerView mRecyclerView;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    public void addAll(List<CardData> cardDataList) {
        if (mListEpisodes == null || cardDataList == null) return;
        int currentSize = mListEpisodes.size();
        mListEpisodes.addAll(cardDataList);
        notifyItemRangeInserted(currentSize, cardDataList.size());
    }

    private int expandPosition = -1;
    private boolean isToExpand = true;
    public void add(CardData cardData) {
        if (mListEpisodes == null) return;
        int currentSize = mListEpisodes.size();
        mListEpisodes.add(cardData);
        notifyItemInserted(currentSize);
    }

    public void removeItem(int position) {
        if (mListEpisodes == null) return;
        mListEpisodes.remove(position);
        notifyItemRemoved(position);
    }

    public AdapterEpisode(Context context, List<CardData> episodesList) {
        mContext = context;
        mListEpisodes = episodesList;
        mHandlerThread = new HandlerThread("EpisodesAdapterUpdater");
        mHandlerThread.start();
        mPollingThreadHandler = new Handler(mHandlerThread.getLooper());
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Called when RecyclerView needs a new {@link } of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #(, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #(, int)
     */
    @Override
    public EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EpisodeViewHolder viewHolder;
        switch (viewType) {
            case ITEM_TYPE_LOADING:
                viewHolder = new LoadingViewHolder(LayoutInflater.from(mContext).inflate(R.layout.view_footer_layout, parent, false));
                LoggerD.debugLog("viewType:: LoadingViewHolder position::" + viewHolder.getAdapterPosition());
                break;
            default:
                viewHolder = new EpisodeViewHolder(LayoutInflater.from(mContext).inflate(R.layout.listitem_related_vods_tvshows, parent, false));
                LoggerD.debugLog("viewType:: EpisodeViewHolder position::" + viewHolder.getAdapterPosition());
                break;
        }
        return viewHolder;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link #} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link #()} which will
     * have the updated adapter position.
     * <p>
     * Override  instead if Adapter can
     * handle effcient partial bind.
     *
     * @param mViewHolder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final EpisodeViewHolder mViewHolder, int position) {
        LoggerD.debugLog("onBindViewHolder:: position::" + position);
        if (mViewHolder instanceof LoadingViewHolder) {
            return;
        }
        CardData cardData = mListEpisodes.get(position);
        if (cardData != null) {
            if (cardData.generalInfo != null) {
                if (cardData.isVODChannel()
                        || cardData.isTVSeason()
                        && ApplicationController.SHOW_PLAYER_LOGS
                        && cardData.generalInfo.title != null
                        && cardData.content != null
                        && cardData.content.serialNo != null
                        && !cardData.content.serialNo.isEmpty()) {
                    mViewHolder.mEpiosdeTitle.setText(cardData.content.serialNo + " " + cardData.generalInfo.title);
                } else if (cardData.generalInfo.title != null) {
                    mViewHolder.mEpiosdeTitle.setText(cardData.generalInfo.title);
                }
            }
            showContentDuration(cardData, mViewHolder);
            CardDataImagesItem imageItem = cardData.getImageItem();
            if (imageItem != null) {
                if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)) {
                    mViewHolder.mThumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    mViewHolder.mThumbnailView.invalidate();
                } else {
                    mViewHolder.mThumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mViewHolder.mThumbnailView.invalidate();
                }
                mViewHolder.mThumbnailView.setImageResource(R.drawable
                        .black);
                if (!TextUtils.isEmpty(imageItem.link)) {
                    /*Picasso.with(mContext).load(imageItem.link).error(R.drawable
                            .epg_thumbnail_default).placeholder(R.drawable
                            .epg_thumbnail_default).into(mViewHolder.mThumbnailView);*/
                    PicassoUtil.with(mContext).load(imageItem.link, mViewHolder.mThumbnailView, R.drawable.black);
                }
            }

            showCollapseAndExpand(mViewHolder, cardData, position);
        }
        CardDownloadData cardDownloadData = DownloadUtil.getDownloadDataFromDownloads(cardData);
        if (cardDownloadData != null) {
            if (cardDownloadData.mCompleted) {
                if (cardDownloadData.mPercentage == 100) {
                    if (APIConstants.isErosNowContent(cardDownloadData)
                            && cardDownloadData.zipStatus < CardDownloadData.STATUS_FILE_UNZIPPED
                            && cardDownloadData.mCompleted) {
                        if (cardDownloadData.zipStatus == CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE
                                || cardDownloadData.zipStatus == CardDownloadData.STATUS_FAILED_TO_AQUIRE_DRM_LICENSE) {
                            showDownloadFailedUI(mViewHolder, cardDownloadData);
                            return;
                        }
                        ErosNowDownloadManager.getInstance(mContext).addListener(mUnzipProgressListener);
                        showDownloadingUI(mViewHolder, cardDownloadData);
                        return;
                    }
                    showDownloadCompleted(mViewHolder, cardDownloadData);
                    return;
                }
                showDownloadFailedUI(mViewHolder, cardDownloadData);
            } else {
                showDownloadingUI(mViewHolder, cardDownloadData);
                if (cardData.isHungama()) {
                } else {
                    FetchDownloadProgress.getInstance(mContext).removeProgressListener(AdapterEpisode.this);
                    FetchDownloadProgress.getInstance(mContext).addProgressListener(AdapterEpisode.this);
                    FetchDownloadProgress.getInstance(mContext).startPolling();
                }
            }
            return;
        }
        if ((APIConstants.isHooqContent(cardData)
                && PrefUtils.getInstance().gePrefEnableHooqDownload())
                || (APIConstants.isAltBalajiContent(cardData)
                && PrefUtils.getInstance().gePrefEnableAltBalajiDownload())
                || (APIConstants.isErosNowContent(cardData)
                && PrefUtils.getInstance().gePrefEnableErosnowDownloadV1())
                || (APIConstants.isHungamaContent(cardData)
                && PrefUtils.gePrefEnableHungamaDownload())) {
            cardData.generalInfo.isDownloadable = true;
            if (APIConstants.isHungamaContent(cardData)
                    && !APIConstants.isMovie(cardData)) {
                cardData.generalInfo.isDownloadable = false;
            }
        }
        showDownloadButtonUI(mViewHolder, cardData);
    }

    private boolean checkParentalControlEnabled() {
        switch (PrefUtils.getInstance().getPrefParentalControlOpt()) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return Util.isAdultContent(mRelatedVODData);
            default:
                return false;
        }
    }
    private void showDownloadButtonUI(final EpisodeViewHolder mViewHolder, final CardData cardData) {
        mViewHolder.mDownloadBtn.setImageResource(R.drawable.carddetaildescription_download_icon_without_text);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(View.GONE);
        mViewHolder.mDownloadBtn.setEnabled(false);
        mViewHolder.mDownloadBtn.setVisibility(View.GONE);
        if (cardData != null
                && cardData.generalInfo != null
                && cardData.generalInfo.isDownloadable) {
            mViewHolder.mDownloadBtn.setEnabled(true);
            mViewHolder.mDownloadBtn.setVisibility(View.VISIBLE);
        }
        mViewHolder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(cardData,mViewHolder);
            }
        });
    }


    private void launchLoginActivity() {
        if (mContext == null) {
            return;
        }
        String sourceDetails = null;
        String source = null;
        ((Activity) mContext).startActivityForResult(LoginActivity.createIntent(mContext, true, false, PARAM_SUBSCRIPTION_TYPE_NONE, source, sourceDetails), MainActivity.INTENT_REQUEST_TYPE_LOGIN);
    }

    private boolean isToShowDownloadButton = false;
    private java.lang.Runnable mDismissProgressTask = new Runnable() {
        @Override
        public void run() {
            isToShowDownloadButton = false;
            AlertDialogUtil.dismissProgressAlertDialog();
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.vf_download_error_while_download));
        }
    };
    private void startDownload(final CardData cardData, final EpisodeViewHolder mViewHolder) {
        if (!Util.checkUserLoginStatus()) {
            launchLoginActivity();
            return;
        }
        DownloadManagerUtility mDownloadManagerUtility = new DownloadManagerUtility(mContext);
        final ContentDownloadEvent contentDownloadEvent = new ContentDownloadEvent(cardData, mCurrentSeasonName, mTVShowCardData);
        isToShowDownloadButton = true;
        mHandler.removeCallbacks(mDismissProgressTask);
        mHandler.postDelayed(mDismissProgressTask, 25 * 1000);
        AlertDialogUtil.showProgressAlertDialog(mContext, "", mContext.getString(R.string.vf_download_init_download_message), true, false, null);
        mDownloadManagerUtility.initializeDownload(contentDownloadEvent, new FragmentCardDetailsDescription.DownloadStatusListener() {
            @Override
            public void onSuccess() {
                if (((Activity) mContext).isFinishing() || mHandler == null) {
                    return;
                }
                mHandler.removeCallbacks(mDismissProgressTask);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListEpisodes == null || cardData == null) return;
                        if (cardData.isHungama()) {
                        } else {
                            FetchDownloadProgress.getInstance(mContext).removeProgressListener(AdapterEpisode.this);
                            FetchDownloadProgress.getInstance(mContext).addProgressListener(AdapterEpisode.this);
                            FetchDownloadProgress.getInstance(mContext).startPolling();
                        }
                        EventBus.getDefault().post(contentDownloadEvent);
                        AlertDialogUtil.dismissProgressAlertDialog();
//                        notifyItemChanged(mListEpisodes.indexOf(cardData));
//                        if (mRecyclerView != null) {
//                            mRecyclerView.findViewHolderForAdapterPosition(mListEpisodes.indexOf(cardData)).itemView.requestLayout();
//                        }
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onDownloadStarted() {
                if (((Activity) mContext).isFinishing() || mHandler == null) {
                    return;
                }
                mHandler.removeCallbacks(mDismissProgressTask);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListEpisodes == null || cardData == null) return;
                        AlertDialogUtil.dismissProgressAlertDialog();
//                        notifyItemChanged(mListEpisodes.indexOf(cardData));
//                        if (mRecyclerView != null) {
//                            mRecyclerView.findViewHolderForAdapterPosition(mListEpisodes.indexOf(cardData)).itemView.requestLayout();
//                        }
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onDownloadInitialized() {
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListEpisodes == null || cardData == null) return;
//                        notifyItemChanged(mListEpisodes.indexOf(cardData));
//                        if (mRecyclerView != null) {
//                            mRecyclerView.findViewHolderForAdapterPosition(mListEpisodes.indexOf(cardData)).itemView.requestLayout();
//                        }
                        notifyDataSetChanged();

                    }
                });
            }

            @Override
            public void onFailure(final String message) {
                if (((Activity) mContext).isFinishing() || mHandler == null) {
                    return;
                }
                mHandler.removeCallbacks(mDismissProgressTask);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        /*if (mPlayer != null) {
                            if (wasPlayerInitialized) {
                                wasPlayerInitialized = true;
                                mPlayer.playContent();
                                return;
                            }
                            mPlayer.onResume();
                        }*/
                        AlertDialogUtil.dismissProgressAlertDialog();
                        String finalMessage = message;
                        if (TextUtils.isEmpty(finalMessage)) {
                            finalMessage = mContext.getString(R.string.vf_download_error_while_download);
                        }

                        if (APIConstants.PLAY_ERR_USER_NOT_SUBSCRIBED.equalsIgnoreCase(message)) {
                            if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW) {
                                fetchOfferAvailability(cardData);
                                return;
                            }
//                    fetchPackageData(true);
                        }
                        AlertDialogUtil.showToastNotification(finalMessage);
                    }
                });

            }

            @Override
            public void onDownloadCancelled() {
                mHandler.removeCallbacks(mDismissProgressTask);
                AlertDialogUtil.dismissProgressAlertDialog();
            }

            @Override
            public boolean isToShowDownloadButton() {
                return isToShowDownloadButton;
            }
        });
    }

    public void showCollapseAndExpand(final EpisodeViewHolder mViewHolder, CardData cardData, int position) {
        if(mViewHolder == null || mViewHolder.mCollapseOrExpandBtn == null){
            return;
        }
        mViewHolder.mCollapseOrExpandBtn.setVisibility(View.GONE);
        String description = cardData.getBriefDescription();
        mViewHolder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
        mViewHolder.mEpiosdeDescription.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(description)) {
            mViewHolder.mCollapseOrExpandBtn.setVisibility(View.VISIBLE);
            mViewHolder.mEpiosdeDescription.setText(cardData.getBriefDescription());
        }
        mViewHolder.mCollapseOrExpandBtn.setTag(position);
        if (position == expandPosition) {
            if (isToExpand) {
                mViewHolder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_collapse_icon);
                mViewHolder.mEpiosdeDescription.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
                mViewHolder.mEpiosdeDescription.setVisibility(View.GONE);
            }
        }
        mViewHolder.mCollapseOrExpandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() instanceof Integer) {
                    int lastExpandPosition = expandPosition;
                    expandPosition = (int) v.getTag();
                    if (lastExpandPosition == expandPosition) {
                        isToExpand = !isToExpand;
                    } else {
                        isToExpand = true;
                    }
//                    notifyItemChanged(expandPosition);
//                    notifyItemChanged(lastExpandPosition);
//                    if (mListener != null) {
//                        mListener.onDataChange();
//                    }
                    if (isToExpand) {
                        mViewHolder.mCollapseOrExpandBtn.setTag(DESCRIPTION_EXPANDED);
                        mViewHolder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_collapse_icon);
                        mViewHolder.mEpiosdeDescription.setVisibility(View.VISIBLE);
                    } else {
                        mViewHolder.mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
                        mViewHolder.mCollapseOrExpandBtn.setTag(DESCRIPTION_COLLAPSED);
                        mViewHolder.mEpiosdeDescription.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void showContentDuration(CardData cardData, EpisodeViewHolder mViewHolder) {
        if (cardData.content != null) {

            if (cardData.isVODCategory()
                    || cardData.isVODYoutubeChannel()
                    || cardData.isTVSeason()
                    || cardData.isTVSeries()
                    || cardData.isTVEpisode()) {
                if (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(cardData.generalInfo.type)) {
                    mViewHolder.mEpiosodeDuration.setVisibility(View.GONE);
                    if (cardData.content != null
                            && cardData.content.duration != null) {
                        mViewHolder.mEpiosodeDuration.setVisibility(View.VISIBLE);
                        mViewHolder.mEpiosodeDuration.setText(cardData.getDurationWithFormat());
                    }
                } else {
                    if (!TextUtils.isEmpty(cardData.content.duration)) {
                        mViewHolder.mEpiosodeDuration.setText(cardData.getDurationWithFormat());
                        mViewHolder.mEpiosodeDuration.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(cardData.content.duration)) {
                    mViewHolder.mEpiosodeDuration.setText(Util.getDDMMYYYYUTC(cardData.content.releaseDate));
                    mViewHolder.mEpiosodeDuration.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void showDownloadCompleted(EpisodeViewHolder mViewHolder, CardDownloadData cardDownloadData) {
        mViewHolder.mDownloadBtn.setImageResource(R.drawable.episode_list_download_icon_complete);
        mViewHolder.mDownloadBtn.setVisibility(View.VISIBLE);
        mViewHolder.mDownloadBtn.setEnabled(false);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(View.GONE);
    }

    private void showDownloadingUI(final EpisodeViewHolder mViewHolder, CardDownloadData cardDownloadData) {
        mViewHolder.mDownloadBtn.setVisibility(View.GONE);
        mViewHolder.mDownloadPercentText.setVisibility(View.VISIBLE);
        if (cardDownloadData != null) {
            if (cardDownloadData.mPercentage < 0) {
                cardDownloadData.mPercentage = 0;
            }
            mViewHolder.mDownloadPercentText.setText(String.valueOf((int)cardDownloadData.mPercentage) + "%");
        }
        try {
            final GifAnimationDrawable gifAnim;
            if (mViewHolder.mDownloadBtn.getTag() instanceof GifAnimationDrawable) {
                android.util.Log.v("GifAnimationDrawable", "Animation drawable tag is null");
                gifAnim = (GifAnimationDrawable) mViewHolder.mDownloadBtn.getTag();
            } else {
                android.util.Log.v("GifAnimationDrawable", "Animation drawable taken from tag");
                gifAnim = new GifAnimationDrawable(mViewHolder.mDownloadBtn.getContext().getResources().openRawResource(R.raw.download_progress_anim));
                gifAnim.setOneShot(false);
            }
            if (mViewHolder.downloadingGIFAnim.getDrawable() == null) {
                mViewHolder.downloadingGIFAnim.setTag(gifAnim);
                mViewHolder.downloadingGIFAnim.setImageDrawable(gifAnim);
               /* mViewHolder.downloadingGIFAnim.post(new WeakRunnable<AdapterEpisode>(this){
                    @Override
                    protected void safeRun(AdapterEpisode adapterEpisode) {
                        android.util.Log.v("GifAnimationDrawable", "Post Animation Started");
                        if (!gifAnim.isRunning()) {
                            gifAnim.setVisible(true, true);
                            gifAnim.stop();
                            gifAnim.start();
                        }
                        mViewHolder.downloadingGIFAnim.invalidateDrawable(mViewHolder.downloadingGIFAnim.getDrawable());
                        mViewHolder.downloadingGIFAnim.postDelayed(this, 7000);
                    }
                });*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mViewHolder.downloadingGIFAnim.setVisibility(View.VISIBLE);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(View.VISIBLE);
    }

    private void showDownloadFailedUI(EpisodeViewHolder mViewHolder, CardDownloadData cardDownloadData) {
        mViewHolder.mDownloadBtn.setImageResource(R.drawable.description_download_broken);
        mViewHolder.mDownloadBtn.setVisibility(View.VISIBLE);
        mViewHolder.mDownloadBtn.setEnabled(false);
        mViewHolder.mDownloadInProgressLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if(mListEpisodes == null){
            return 0;
        }
        return mListEpisodes.size();
    }

    public void setTVShowData(CardData relatedVODData) {
        this.mTVShowCardData = relatedVODData;
    }


    private void notifyDataChangeOnSeperateThread(String contntId) {
        mPollingThreadHandler.postDelayed(new WeakRunnable<String>(contntId) {
            @Override
            public void safeRun(String contentId) {
                if (((Activity) mContext).isFinishing()) return;
                if (contentId != null
                        && mListEpisodes != null) {
                    for (int i = 0; i < mListEpisodes.size(); i++) {
                        CardData cardData = mListEpisodes.get(i);
                        if (cardData != null && cardData._id != null && contentId.equalsIgnoreCase(cardData._id)) {
                            mHandler.post(new WeakRunnable<Integer>(new Integer(1)) {
                                @Override
                                public void safeRun(Integer position) {
//                                    notifyItemChanged(position);
//                                    if (mRecyclerView != null) {
//                                        mRecyclerView.findViewHolderForAdapterPosition(position).itemView.requestLayout();
//                                    }
                                    notifyDataSetChanged();

                                }
                            });

                        }
                    }
                }
            }
        }, 5000);
    }

    private void notifyDataChangeOnSeperateThread(CardDownloadData data) {
        mPollingThreadHandler.postDelayed(new WeakRunnable<CardDownloadData>(data) {
            @Override
            public void safeRun(CardDownloadData data) {
                if (((Activity) mContext).isFinishing()) return;
                if (data == null) return;
                if (data.tvSeasonsList == null && data.tvEpisodesList == null) {
                    if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(data.partnerName) && (data.mPercentage != 100 && !data.mCompleted)) {
                        return;
                    }
                    if (data != null
                            && data._id != null
                            && mListEpisodes != null) {
                        for (int i = 0; i < mListEpisodes.size(); i++) {
                            CardData cardData = mListEpisodes.get(i);
                            if (cardData != null && cardData._id != null && data._id.equalsIgnoreCase(cardData._id)) {
                                mHandler.post(new WeakRunnable<Integer>(new Integer(i)) {
                                    @Override
                                    public void safeRun(Integer position) {
                                        LoggerD.debugLog("notifyItemChanged:: position:: " + position);
//                                        notifyItemChanged(position);
//                                        if (mRecyclerView != null) {
//                                            mRecyclerView.findViewHolderForAdapterPosition(position).itemView.requestLayout();
//                                        }
                                        notifyDataSetChanged();
                                    }
                                });
                                return;
                            }
                        }
                    }
                } else if (data.tvSeasonsList == null && data.tvEpisodesList != null) {
                    for (CardDownloadData episode : data.tvEpisodesList) {
                        if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(episode.partnerName) && (data.mPercentage != 100 && !data.mCompleted)) {
                            return;
                        }
                        if (episode != null
                                && episode._id != null
                                && mListEpisodes != null) {
                            for (int i = 0; i < mListEpisodes.size(); i++) {
                                CardData cardData = mListEpisodes.get(i);
                                if (cardData != null && cardData._id != null && episode._id.equalsIgnoreCase(cardData._id)) {
                                    mHandler.post(new WeakRunnable<Integer>(new Integer(i)) {
                                        @Override
                                        public void safeRun(Integer position) {
                                            LoggerD.debugLog("notifyItemChanged:: position:: " + position);
//                                            notifyItemChanged(position);
//                                            if (mRecyclerView != null) {
//                                                mRecyclerView.findViewHolderForAdapterPosition(position).itemView.requestLayout();
//                                            }
                                            notifyDataSetChanged();
                                        }
                                    });
                                    return;
                                }
                            }
                        }
                    }
                } else if (data.tvSeasonsList != null) {
                    for (SeasonData seasonData : data.tvSeasonsList) {
                        for (CardDownloadData episode : seasonData.tvEpisodesList) {
                            if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(episode.partnerName) && (data.mPercentage != 100 && !data.mCompleted)) {
                                return;
                            }
                            if (episode != null
                                    && episode._id != null
                                    && mListEpisodes != null) {
                                for (int i = 0; i < mListEpisodes.size(); i++) {
                                    CardData cardData = mListEpisodes.get(i);
                                    if (cardData != null && cardData._id != null && episode._id.equalsIgnoreCase(cardData._id)) {
                                        mHandler.post(new WeakRunnable<Integer>(new Integer(i)) {
                                            @Override
                                            public void safeRun(Integer position) {
                                                LoggerD.debugLog("notifyItemChanged:: position:: " + position);
//                                                notifyItemChanged(position);
//                                                if (mRecyclerView != null) {
//                                                    mRecyclerView.findViewHolderForAdapterPosition(position).itemView.requestLayout();
//                                                }
                                                notifyDataSetChanged();
                                            }
                                        });
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, 5000);
    }



    @Override
    public void DownloadProgress(CardData cardData, CardDownloadData downloadData) {
        notifyDataChangeOnSeperateThread(downloadData);
    }

    public void setCurrentSeasonName(String cardData) {
        this.mCurrentSeasonName = cardData;
    }

    public void addDownloadListener() {

    }

    public void setSearchQuery(String mSearchQuery) {
        this.mSearchQuery = mSearchQuery;
    }

    public void setCarouselInfoData(CarouselInfoData mCarouselInfoData) {
        this.mCarouselInfoData = mCarouselInfoData;
    }

    public void setRelatedVODData(CardData mRelatedVODData) {
        this.mRelatedVODData = mRelatedVODData;
    }

    public CardData getItem(int i) {
        if (mListEpisodes == null) return null;
        return mListEpisodes.get(i);
    }

    public List<CardData> getItems() {
        return mListEpisodes;
    }


    private class LoadingViewHolder extends EpisodeViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.footer_progressbar);
        }
    }

    public class EpisodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView downloadingGIFAnim;
        TextView mDownloadPercentText;
        LinearLayout mDownloadInProgressLinearLayout;
        TextView mEpiosdeDescription;
        public ImageView mCollapseOrExpandBtn;
        ImageView mDownloadBtn;
        ImageView mThumbnailView;
        TextView mEpiosodeDuration;
        TextView mEpiosdeTitle;
        ImageView mReminderImage;
        ProgressBar mProgressBar;

        public EpisodeViewHolder(View itemView) {
            super(itemView);
            mThumbnailView = (ImageView) itemView.findViewById(R.id.imageview_thumbnail);
            mEpiosodeDuration = (TextView) itemView.findViewById(R.id
                    .textview_duration);
            mEpiosdeTitle = (TextView) itemView.findViewById(R.id
                    .textview_title);
            mEpiosdeDescription = (TextView) itemView.findViewById(R.id
                    .textview_description);
            mReminderImage = (ImageView) itemView.findViewById(R.id
                    .imageview_play_alarm_download);
            mDownloadBtn = (ImageView) itemView.findViewById(R.id
                    .download_btn_image);
            mCollapseOrExpandBtn = (ImageView) itemView.findViewById(R.id
                    .expand_btn_image);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.customProgress);
            mDownloadInProgressLinearLayout = (LinearLayout) itemView.findViewById(R.id.download_btn_layout);
            mDownloadPercentText = (TextView) itemView.findViewById(R.id
                    .download_btn_status_percent_text);
            downloadingGIFAnim = (ImageView) itemView.findViewById(R.id.downloading_gif_anim);
            if (downloadingGIFAnim != null)
                downloadingGIFAnim.setBackgroundColor(Color.TRANSPARENT); //for gif without background
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListEpisodes == null || mListEpisodes.isEmpty()
                    || getAdapterPosition() >= mListEpisodes.size()){
                return;
            }

            try {
                showDetailsFragment(mListEpisodes.get(getAdapterPosition()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private void showDetailsFragment(CardData cardData) {
        //Log.d(TAG, "onItemClick");

        if (cardData == null) {
            return;
        }
        CacheManager.setSelectedCardData(cardData);
        Bundle args = new Bundle();
        args.putString(CardDetails
                .PARAM_CARD_ID, cardData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        if (mRelatedVODData != null
                && mRelatedVODData.generalInfo != null) {
            args.putString(CardDetails
                    .PARAM_CARD_DATA_TYPE, mRelatedVODData.generalInfo.type);
        }
        int partnerType = CardDetails.Partners.APALYA;
        partnerType = Util.getPartnerTypeContent(cardData);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, partnerType);
        String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        if (mCarouselInfoData != null)
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mCarouselInfoData.title);

        if (mSearchQuery != null) {
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mSearchQuery);
        }
        if (cardData.generalInfo != null
                && cardData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(cardData.generalInfo.type))) {
            //Log.d(TAG, "type: " + cardData.generalInfo.type + " title: " + cardData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, cardData);
        }
        args.putString(CardDetails.PARAM_CARD_ID, cardData._id);
        args.putString(CardDetails.PARAM_CARD_DATA_TYPE, cardData.generalInfo.type);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putBoolean(FragmentCardDetailsPlayer.PARAM_KEPP_DESCRIPTION_VIEWS_UPDATe_DATA, true);
        if (mCurrentSeasonName != null) {
            args.putString(CardDetails.PARAM_SEASON_NAME, mCurrentSeasonName);
        }


        if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_LIVE.equalsIgnoreCase(cardData.generalInfo.type)
                && !APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)
                && !cardData.isTVSeries()
                && !cardData.isVODChannel()
                && !cardData.isVODYoutubeChannel()
                && !cardData.isVODCategory()
                && !cardData.isTVSeason()) {
            CacheManager.setCardDataList(mListEpisodes);
        }
        args.putSerializable(PARAM_RELATED_CARD_DATA, mTVShowCardData);
        ((MainActivity)mContext).showDetailsFragment(args, cardData);
//            mBaseActivity.pushFragment(CardDetails.newInstance(args));

    }

    private void fetchOfferAvailability(CardData mCardData) {
//        showProgressBar();
        if (mCardData == null) {
            return;
        }
        OfferedPacksRequest.Params params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_5, APIConstants.PARAM_CONTENT_DETAIL, mCardData._id);
        final OfferedPacksRequest contentDetails = new OfferedPacksRequest(params, new APICallback<OfferResponseData>() {
            @Override
            public void onResponse(APIResponse<OfferResponseData> response) {
                if (response == null || response.body() == null) {
                    Analytics.mixpanelEventUnableToFetchOffers(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIConstants.NOT_AVAILABLE);
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
                    return;
                }
                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && response.body().code == 216) {
                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")) {
                    if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW
                            && response.body().ui != null
                            && response.body().ui.action != null) {
                        switch (response.body().ui.action) {
                            case APIConstants.OFFER_ACTION_SHOW_MESSAGE:
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                                        @Override
                                        public void onDialogClick(String buttonText) {
                                        }
                                    });
                                }
                                break;
                            case APIConstants.OFFER_ACTION_SHOW_TOAST:
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showToastNotification(response.body().message);
                                }
                                break;
                            case APIConstants.APP_LAUNCH_WEB:
                                if (!TextUtils.isEmpty(response.body().ui.redirect)) {
                                    ((Activity)mContext).startActivityForResult(SubscriptionWebActivity.createIntent(mContext, response.body().ui.redirect, SubscriptionWebActivity.PARAM_LAUNCH_NONE), SUBSCRIPTION_REQUEST);
                                }
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_OFFER:
                                launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER);
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION:
                                launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                                break;
                            default:
                                break;
                        }
                        return;
                    }

                    boolean isOfferAvailable = false;
                    for (CardDataPackages offerPack : response.body().results) {
                        if (offerPack.subscribed) {
                            return;
                        }
                        if (APIConstants.TYPE_OFFER.equalsIgnoreCase(offerPack.packageType)) {
                            isOfferAvailable = true;
                            break;
                        }
                    }
                    if (isOfferAvailable) {
                        launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER);
                        return;
                    }
                    launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugOTP("Failed: " + t);
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    Analytics.mixpanelEventUnableToFetchOffers(mContext.getString(R.string.network_error), APIConstants.NOT_AVAILABLE);
                    return;
                }
                Analytics.mixpanelEventUnableToFetchOffers(t == null || t.getMessage() == null ? APIConstants.NOT_AVAILABLE : t.getMessage(), APIConstants.NOT_AVAILABLE);
            }
        });

        APIService.getInstance().execute(contentDetails);
    }



    private void launchPackagesScreen(int subscriptionType) {
        if (mContext == null) {
            return;
        }
        String source = null;
        String sourceDetails = null;
        mContext.startActivity(LoginActivity.createIntent(mContext, true, true, subscriptionType, source, sourceDetails));
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = ITEM_DATA;
        if(mListEpisodes.get(position).isLoading()) viewType = ITEM_TYPE_LOADING;
        return viewType;
    }

    private class DownloadPollingPostTask implements Runnable {

        private final WeakReference<GifAnimationDrawable> gifAnimationDrawableWeakReference;
        private final WeakReference<ImageView> downloadingGIFAnimWeakReference;

        public DownloadPollingPostTask(WeakReference<GifAnimationDrawable> gifAnimationDrawableWeakReference, WeakReference<ImageView> downloadingGIFAnimWeakReference) {
            this.gifAnimationDrawableWeakReference = gifAnimationDrawableWeakReference;
            this.downloadingGIFAnimWeakReference = downloadingGIFAnimWeakReference;
        }

        @Override
        public void run() {
            GifAnimationDrawable gifAnim = null;
            ImageView downloadingGIFAnim = null;
            if (gifAnimationDrawableWeakReference == null || gifAnimationDrawableWeakReference.get() == null) {
                gifAnim = gifAnimationDrawableWeakReference.get();
            }
            if (downloadingGIFAnimWeakReference == null || downloadingGIFAnimWeakReference.get() == null) {
                downloadingGIFAnim = downloadingGIFAnimWeakReference.get();
            }
            if (gifAnim == null || downloadingGIFAnim == null) return;
            android.util.Log.v("GifAnimationDrawable", "Post Animation Started");
            if (!gifAnim.isRunning()) {
                gifAnim.setVisible(true, true);
                gifAnim.stop();
                gifAnim.start();
            }
            downloadingGIFAnim.invalidateDrawable(downloadingGIFAnim.getDrawable());
            downloadingGIFAnim.postDelayed(this, 7000);
        }
    }


}
