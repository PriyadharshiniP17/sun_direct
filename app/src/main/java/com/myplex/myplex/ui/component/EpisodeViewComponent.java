package com.myplex.myplex.ui.component;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.OfferResponseData;
import com.myplex.model.SeasonData;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.download.DownloadManagerUtility;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsPlayer;
import com.myplex.myplex.ui.fragment.PackagesFragment;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.ui.views.gifview.GifAnimationDrawable;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.myplex.utils.WeakRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;
import static com.myplex.myplex.ui.fragment.CardDetails.PARAM_RELATED_CARD_DATA;
import static com.myplex.myplex.ui.fragment.PackagesFragment.PARAM_SUBSCRIPTION_TYPE_NONE;

public class EpisodeViewComponent extends GenericListViewCompoment implements FetchDownloadProgress.DownloadProgressStatus {
    private static final int DESCRIPTION_COLLAPSED = 1;
    private static final int DESCRIPTION_EXPANDED = 0;
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final Handler mHandler;
    private final Handler mPollingThreadHandler;
    private final HandlerThread mHandlerThread;
    private CardData mData;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = EpisodeViewComponent.class.getSimpleName();
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
    private String mCurrentSeasonName;
    private CardData mTVShowCardData;
    private boolean isToExpand;
    private int expandPosition;
    private String mSearchQuery;
    private String sourceDetails,source;

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


    public EpisodeViewComponent(Context mContext,
                                List<DetailsViewContent.DetailsViewDataItem> data, View view,
                                CardDetailViewFactory.CardDetailViewFactoryListener listener, String searchQuery,
                                String sourceDetails, CardData mTVShowData, String seasonName,String source) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        this.mSearchQuery = searchQuery;
        this.mCurrentSeasonName = seasonName;
        this.sourceDetails = sourceDetails;
        this.source = source;
        this.mTVShowCardData = mTVShowData;
        mHandler = new Handler(Looper.getMainLooper());
        mHandlerThread = new HandlerThread("EpisodesAdapterUpdater");
        mHandlerThread.start();
        mPollingThreadHandler = new Handler(mHandlerThread.getLooper());
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mValues == null || mValues.isEmpty()
                        || getAdapterPosition() >= mValues.size()) {
                    return;
                }
                try {
                    showDetailsFragment(mValues.get(getAdapterPosition()).cardData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
    }

    public static EpisodeViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener, String searchQuery, String sourceDetails,
                                                  CardData mTVShowData, String seasonName,String source) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem_related_vods_tvshows,
                parent, false);
        EpisodeViewComponent briefDescriptionComponent = new EpisodeViewComponent(context, data, view, listener, searchQuery, sourceDetails, mTVShowData, seasonName,source);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        this.mData = mValues.get(position).cardData;
        if (mData == null) {
            return;
        }
        if (mData != null) {
            if (mData.generalInfo != null) {
                if (mData.isVODChannel()
                        || mData.isTVSeason()
                        && ApplicationController.SHOW_PLAYER_LOGS
                        && mData.generalInfo.title != null
                        && mData.content != null
                        && mData.content.serialNo != null
                        && !mData.content.serialNo.isEmpty()) {
                    mEpiosdeTitle.setText(mData.content.serialNo + " " + mData.generalInfo.title);
                } else if (mData.generalInfo.title != null) {
                    mEpiosdeTitle.setText(mData.generalInfo.title);
                }
            }
            showContentDuration();
            CardDataImagesItem imageItem = mData.getImageItem();
            if (imageItem != null) {
                if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)) {
                    mThumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    mThumbnailView.invalidate();
                } else {
                    mThumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mThumbnailView.invalidate();
                }
                mThumbnailView.setImageResource(R.drawable
                        .black);
                if (!TextUtils.isEmpty(imageItem.link)) {
                    PicassoUtil.with(mContext).load(imageItem.link,mThumbnailView,R.drawable.black);
                }
            }

            //showCollapseAndExpand(mData, position);
        }
        /*CardDownloadData cardDownloadData = DownloadUtil.getDownloadDataFromDownloads(mData);
        SDKLogger.debug("cardDownloadData:: " + cardDownloadData + " mData:: " + mData);
        if (cardDownloadData != null) {
            if (cardDownloadData.mCompleted) {
                if (cardDownloadData.mPercentage == 100) {
                    if (APIConstants.isErosNowContent(cardDownloadData)
                            && cardDownloadData.zipStatus < CardDownloadData.STATUS_FILE_UNZIPPED
                            && cardDownloadData.mCompleted) {
                        if (cardDownloadData.zipStatus == CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE
                                || cardDownloadData.zipStatus == CardDownloadData.STATUS_FAILED_TO_AQUIRE_DRM_LICENSE) {
                            showDownloadFailedUI();
                            return;
                        }
                        ErosNowDownloadManager.getInstance(mContext).addListener(mUnzipProgressListener);
                        showDownloadingUI(cardDownloadData.mPercentage);
                        return;
                    }
                    showDownloadCompleted();
                    return;
                }
                showDownloadFailedUI();
            } else {
                showDownloadingUI(cardDownloadData.mPercentage);
                if (mData.isHungama()) {
                } else {
                    FetchDownloadProgress.getInstance(mContext).removeProgressListener(EpisodeViewComponent.this);
                    FetchDownloadProgress.getInstance(mContext).addProgressListener(EpisodeViewComponent.this);
                    FetchDownloadProgress.getInstance(mContext).startPolling();
                }
            }
            return;
        }
        if ((APIConstants.isHooqContent(mData)
                && PrefUtils.getInstance().gePrefEnableHooqDownload())
                || (APIConstants.isAltBalajiContent(mData)
                && PrefUtils.getInstance().gePrefEnableAltBalajiDownload())
                || (APIConstants.isErosNowContent(mData)
                && PrefUtils.getInstance().gePrefEnableErosnowDownloadV1())
                || (APIConstants.isHungamaContent(mData)
                && PrefUtils.gePrefEnableHungamaDownload())) {
            mData.generalInfo.isDownloadable = true;
            if (APIConstants.isHungamaContent(mData)
                    && !APIConstants.isMovie(mData)) {
                mData.generalInfo.isDownloadable = false;
            }
        }*/
        //showDownloadButtonUI();

    }

    private void showDownloadButtonUI() {
        boolean enabled = false;
        if(!enabled){
            return;
        }
        mDownloadBtn.setImageResource(R.drawable.carddetaildescription_download_icon_without_text);
        mDownloadInProgressLinearLayout.setVisibility(View.GONE);
        mDownloadBtn.setEnabled(false);
        mDownloadBtn.setVisibility(View.GONE);
        if (mData != null
                && mData.generalInfo != null
                && mData.generalInfo.isDownloadable) {
            mDownloadBtn.setEnabled(true);
            mDownloadBtn.setVisibility(View.VISIBLE);
        }
        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (Util.isAdultContent(mData)
                        && !PrefUtils.getInstance().getprefIsAgeAbove18Plus()) {
                    AlertDialogUtil.showAlertDialog(mContext, mContext.getString(R.string.txt_adult_warning), "", false,
                            myplexAPISDK.getApplicationContext().getString(R.string.go_back),
                            myplexAPISDK.getApplicationContext().getString(R.string.confirm)
                            , new AlertDialogUtil.DialogListener() {

                                @Override
                                public void onDialog1Click() {

                                }

                                @Override
                                public void onDialog2Click() {
                                    PrefUtils.getInstance().setPrefIsAgeAbove18Plus(true);
                                    startDownload();
                                }
                            });
                    return;
                }*/
                startDownload();
            }
        });
    }


    private void startDownload() {
        if (!Util.checkUserLoginStatus()) {
            launchLoginActivity();
            return;
        }
        // Season name retrieval from season data of title
        mCurrentSeasonName = mValues.get(mListener.getEpisodesLayoutStartPosition() - 1).title;
        DownloadManagerUtility mDownloadManagerUtility = new DownloadManagerUtility(mContext);
        final ContentDownloadEvent contentDownloadEvent = new ContentDownloadEvent(mData, mCurrentSeasonName, mTVShowCardData);
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
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mData == null) return;
                        if (mData.isHungama()) {
                        } else {
                            FetchDownloadProgress.getInstance(mContext).removeProgressListener(EpisodeViewComponent.this);
                            FetchDownloadProgress.getInstance(mContext).addProgressListener(EpisodeViewComponent.this);
                            FetchDownloadProgress.getInstance(mContext).startPolling();
                        }
                        EventBus.getDefault().post(contentDownloadEvent);
                        AlertDialogUtil.dismissProgressAlertDialog();
//                        notifyItemChanged(mListEpisodes.indexOf(mData));
                        showDownloadingUI(0);
                    }
                });
            }

            @Override
            public void onDownloadStarted() {
                if (((Activity) mContext).isFinishing() || mHandler == null) {
                    return;
                }
                mHandler.removeCallbacks(mDismissProgressTask);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mData == null) return;
                        AlertDialogUtil.dismissProgressAlertDialog();
                    }
                });
            }

            @Override
            public void onDownloadInitialized() {
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mData == null) return;
                        showLoadingAndDisableDownload();
                    }
                });
            }

            @Override
            public void onFailure(final String message) {
                if (((Activity) mContext).isFinishing() || mHandler == null) {
                    return;
                }
                mHandler.removeCallbacks(mDismissProgressTask);
                mHandler.post(new Runnable() {
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
                                fetchOfferAvailability(mData);
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


    private void showLoadingAndDisableDownload() {
        if (((Activity) mContext).isFinishing()) {
            return;
        }
        showDownloadingUI(0);
        mDownloadBtn.post(new Runnable() {
            @Override
            public void run() {
                mDownloadBtn.setVisibility(View.VISIBLE);
            }
        });
    }


    public void showCollapseAndExpand(CardData cardData, int position) {
        mCollapseOrExpandBtn.setVisibility(View.GONE);
        String description = cardData.getBriefDescription();
        mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
        mEpiosdeDescription.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(description)) {
            mCollapseOrExpandBtn.setVisibility(View.VISIBLE);
            mEpiosdeDescription.setText(cardData.getBriefDescription());
        }
        mCollapseOrExpandBtn.setTag(position);
        if (position == expandPosition) {
            if (isToExpand) {
                mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_collapse_icon);
                mEpiosdeDescription.setVisibility(View.VISIBLE);
            } else {
                mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
                mEpiosdeDescription.setVisibility(View.GONE);
            }
        }
        mCollapseOrExpandBtn.setOnClickListener(new View.OnClickListener() {
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
                        mCollapseOrExpandBtn.setTag(DESCRIPTION_EXPANDED);
                        mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_collapse_icon);
                        mEpiosdeDescription.setVisibility(View.VISIBLE);
                    } else {
                        mCollapseOrExpandBtn.setImageResource(R.drawable.episode_list_expand_icon);
                        mCollapseOrExpandBtn.setTag(DESCRIPTION_COLLAPSED);
                        mEpiosdeDescription.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void showContentDuration() {
        if (mData.content != null) {

            if (mData.isVODCategory()
                    || mData.isVODYoutubeChannel()
                    || mData.isTVSeason()
                    || mData.isTVSeries()
                    || mData.isTVEpisode()) {
                if (APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type)) {
                    mEpiosodeDuration.setVisibility(View.GONE);
                    if (mData.content != null
                            && mData.content.duration != null) {
                        mEpiosodeDuration.setVisibility(View.VISIBLE);
                        mEpiosodeDuration.setText(mData.getDurationWithFormat());
                    }
                } else {
                    if (!TextUtils.isEmpty(mData.content.duration)) {
                        mEpiosodeDuration.setText(mData.getDurationWithFormat());
                        mEpiosodeDuration.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(mData.content.duration)) {
                    mEpiosodeDuration.setText(Util.getDDMMYYYYUTC(mData.content.releaseDate));
                    mEpiosodeDuration.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void showDownloadCompleted() {
        mDownloadBtn.setImageResource(R.drawable.episode_list_download_icon_complete);
        mDownloadBtn.setVisibility(View.VISIBLE);
        mDownloadBtn.setEnabled(false);
        mDownloadInProgressLinearLayout.setVisibility(View.GONE);
    }

    private void showDownloadingUI(int progress) {
//        notify description change as well if required
        if (mListener != null) {
            mListener.notifyItemChanged(0);
        }
        mDownloadBtn.setVisibility(View.GONE);
        mDownloadPercentText.setVisibility(View.VISIBLE);
        if (progress < 0) {
            progress = 0;
        }
        mDownloadPercentText.setText(progress + "%");
        try {
            final GifAnimationDrawable gifAnim;
//            if (downloadingGIFAnim.getTag() != null && downloadingGIFAnim.getTag() instanceof GifAnimationDrawable) {
//                Log.v("GifAnimationDrawable", "Animation drawable tag is null");
//                gifAnim = (GifAnimationDrawable) mDownloadBtn.getTag();
//            } else {
            Log.v("GifAnimationDrawable", "Animation drawable taken from tag");
            gifAnim = new GifAnimationDrawable(mDownloadBtn.getContext().getResources().openRawResource(R.raw.download_progress_anim));
            gifAnim.setOneShot(false);
            downloadingGIFAnim.setTag(gifAnim);
//            }
            if (gifAnim != null && !gifAnim.isRunning()) {
                gifAnim.setVisible(true, true);
                gifAnim.start();
            }
            downloadingGIFAnim.setImageDrawable(gifAnim);
            downloadingGIFAnim.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        downloadingGIFAnim.setVisibility(View.VISIBLE);
        mDownloadInProgressLinearLayout.setVisibility(View.VISIBLE);
    }

    private void showDownloadFailedUI() {
        mDownloadBtn.setImageResource(R.drawable.description_download_broken);
        mDownloadBtn.setVisibility(View.VISIBLE);
        mDownloadBtn.setEnabled(false);
        mDownloadInProgressLinearLayout.setVisibility(View.GONE);
    }



    @Override
    public void DownloadProgress(CardData cardData, CardDownloadData downloadData) {
//        notifyDataChangeOnSeperateThread();
        SDKLogger.debug("cardData:: " + downloadData);
        if (mListener != null) {
            if (downloadData._id.equalsIgnoreCase(mData._id)) {
                int position = getAdapterPosition();
                SDKLogger.debug("updating the item position at:: " + position);
                mHandler.post(new WeakRunnable<Integer>(new Integer(position)) {
                    @Override
                    public void safeRun(Integer position) {
                        //java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.Integer.intValue()' on a null object reference
                        //at EpisodeViewComponent$10.safeRun(EpisodeViewComponent.java:636)
                        if(mListener != null && position != null) {
                            mListener.notifyItemChanged(position);
                        }
                    }
                });
            }
        }
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
                                    ((Activity) mContext).startActivityForResult(SubscriptionWebActivity.createIntent(mContext, response.body().ui.redirect, SubscriptionWebActivity.PARAM_LAUNCH_NONE), SUBSCRIPTION_REQUEST);
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

    private void notifyDataChangeOnSeperateThread() {
        mPollingThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (((Activity) mContext).isFinishing()) return;
                CardDownloadedDataList downloadedDataList = ApplicationController.getDownloadData();
                for (String key : downloadedDataList.mDownloadedList.keySet()) {
                    final CardDownloadData data = downloadedDataList.mDownloadedList.get(key);
                    if (data == null) continue;
                    if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(data.partnerName) && (data.mPercentage != 100 && !data.mCompleted)) {
                        return;
                    }
                    if (mData != null && mData._id != null && data._id != null && data._id.equalsIgnoreCase(mData._id)) {
                        mHandler.post(new WeakRunnable<CardDownloadData>(data) {
                            @Override
                            public void safeRun(CardDownloadData data) {
                                showDownloadingUI(data.mPercentage);
                            }
                        });
                    } else if (data.tvSeasonsList == null && data.tvEpisodesList != null) {
                        for (final CardDownloadData episode : data.tvEpisodesList) {
                            if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(episode.partnerName) && (data.mPercentage != 100 && !data.mCompleted)) {
                                return;
                            }
                            if (episode != null
                                    && episode._id != null) {
                                if (mData != null && mData._id != null && episode._id.equalsIgnoreCase(mData._id)) {
                                    mHandler.post(new WeakRunnable<CardDownloadData>(episode) {
                                        @Override
                                        public void safeRun(CardDownloadData episode1) {
                                            showDownloadingUI(episode1.mPercentage);
                                        }
                                    });
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
                                        && episode._id != null) {
                                    if (mData != null && mData._id != null && episode._id.equalsIgnoreCase(mData._id)) {
                                        if (mData != null && mData._id != null && episode._id.equalsIgnoreCase(mData._id)) {
                                            mHandler.post(new WeakRunnable<CardDownloadData>(data) {
                                                @Override
                                                public void safeRun(CardDownloadData downloadData) {
                                                    showDownloadingUI(downloadData.mPercentage);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }, 3000);
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
        int partnerType = CardDetails.Partners.APALYA;
        partnerType = Util.getPartnerTypeContent(cardData);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, partnerType);
        String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
        args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);

        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
        if (!TextUtils.isEmpty(source))
            args.putString(Analytics.PROPERTY_SOURCE, source);
        if (!TextUtils.isEmpty(sourceDetails))
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, sourceDetails);

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
            int episodesLayoutStartPosition = 0;
            if (mListener != null) {
                episodesLayoutStartPosition = mListener.getEpisodesLayoutStartPosition();
            }
            mValues.subList(episodesLayoutStartPosition, mValues.size() - 1);
            List<CardData> listEpisodes = new ArrayList<>();
            for (int i = 0; i < mValues.size(); i++) {
                if (i >= episodesLayoutStartPosition) {
                    listEpisodes.add(mValues.get(i).cardData);
                }
            }
            CacheManager.setCardDataList(listEpisodes);
        }
        args.putSerializable(PARAM_RELATED_CARD_DATA, mTVShowCardData);
        ((MainActivity)mContext).showDetailsFragment(args, cardData);
//            mBaseActivity.pushFragment(CardDetails.newInstance(args));

    }


}
