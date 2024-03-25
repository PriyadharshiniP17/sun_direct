package com.myplex.myplex.ui.fragment;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.model.CardData;
import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.PlayerStatusUpdate;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ads.PulseManager;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.media.PlayerListener;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.ui.views.CardDetailViewFactoryOld;
import com.myplex.myplex.ui.views.CardVideoPlayer;
import com.myplex.myplex.ui.views.SubscriptionPacksDialog;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.UiUtil;
//import com.ooyala.pulse.PulseVideoAd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Apalya on 15-Dec-15.
 */
public class CardDetails extends BaseFragment implements CacheManager.CacheManagerCallback,
        CardDetailViewFactoryOld.CardDetailViewFactoryListener, PlayerStatusUpdate {
    private static final String TAG = CardDetails.class.getSimpleName();
    public static final String CURRENT_CONTENT_PROGRESS = "current_content_progress";
    public static final String PARAM_CARD_DATA_TYPE = "card_data_type";
    public final static String PARAM_AUTO_PLAY = "auto_play";
    public final static String PARAM_EPG_DATE_POSITION = "epg_date_position";
    public final static String PARAM_PARTNER_TYPE = "partner_content_type";
    public static final String PARAM_SELECTED_CARD_DATA = "selected_card_data";
    public static final String PARAM_AUTO_PLAY_MINIMIZED = "auto_play_minimized";
    public static final String PARAM_QUEUE_LIST_CARD_DATA = "queue_list_card_data";
    public static final String PARAM_RESET_EPG_DATE_POSITION_IN_DETAIL = "param_reset_epg_date_position_in_detail";
    public static final String PARAM_SEASON_NAME = "season_name";
    public static final String PARAM_SEASON_GLOBAL_SERVICE_ID = "season_global_service_id";
    public static final String PARAM_PLAY_LANDSCAPE = "landscape_mode";
    public static final String PARAM_SUPPORT_CATCHUP = "true";

    private View rootView;
    private CardData mCardData;
    private LayoutInflater mInflater;
    protected LinearLayout mParentContentLayout;
    protected CardDetailViewFactoryOld mCardDetailViewFactory;

    private LinearLayout mDescriptionContentLayout;
    private LinearLayout mTitleSectionLayoutPacks;
    private LinearLayout mTitleSectionLayoutPlayerLogs;
    private LinearLayout mTitleSectionLayoutComments;
    private LinearLayout mPackagesLayout;
    private LinearLayout mPlayerLogsLayout;
    private LinearLayout mCommentsContentLayout;
    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
    private RelativeLayout videoLayout;
    private CardVideoPlayer mPlayer;
    private TextView mTextViewProgressBar;
    private String mId;
    private boolean isMinimized;
    private final CacheManager mCacheManager = new CacheManager();
    private boolean mAutoPlay = false;
    private String mContentType;
    private int mEpgDatePosition = 0;
    private RelativeLayout mRLayoutTimeShiftHelp;
    private boolean isTimeShiftHelpScreenShown = false;
    public final static String PARAM_CARD_ID = "selected_card_id";
    public final static String PARAM_PARTNER_ID = "partner_content_id";
    public final static String PARAM_RELATED_CARD_DATA = "related_card_data";
    public final static String PARAM_AD_ENBLED = "is_ad_enabled";
    public final static String PARAM_AD_PROVIDER = "ad_provider";
    private AlertDialogUtil.NeutralDialogListener mNeutralDialogListener = new AlertDialogUtil.NeutralDialogListener() {
        @Override
        public void onDialogClick(String buttonText) {
            if (mCardData == null) {
                return;
            }

            if (null != buttonText &&
                    buttonText.equalsIgnoreCase(mContext.getString(R.string
                            .play_button_retry))) {
                Bundle args = new Bundle();
                args.putString(PARAM_CARD_ID, mCardData._id);
                fetchCardData();
            }
        }
    };
    private boolean duringAd = false;
    private int mPartner = Partners.APALYA;
    private String mNid;
    private Handler mHandlerShowMediaController = new Handler();
    private static final int DEFAULT_MEDIACTROLLER_TIMEOUT = 10 * 1000;
    private Toolbar mToolbar;
    private View mInflateView;
    private TextView mToolbarTitle;
    private ImageView mImageViewClose;
    private ImageView mToolbarLogo;
    private RelativeLayout mRootLayout;

    private Runnable mRunnableShowMediaController = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null) {
                mPlayer.showMediaController();
            }
            mHandlerShowMediaController.postDelayed(mRunnableShowMediaController, DEFAULT_MEDIACTROLLER_TIMEOUT);
        }
    };
    private boolean mIsToShowToolBar = false;
    private String mNotificationTitle;
    private long currentContentProgress;

    public static CardDetails newInstance(Bundle args) {
        CardDetails fragmentDetails = new CardDetails();
        fragmentDetails.setArguments(args);
        return fragmentDetails;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PARAM_CARD_ID, mId);
        outState.putString(PARAM_CARD_DATA_TYPE, mContentType);
        outState.putInt(PARAM_PARTNER_TYPE, mPartner);
        outState.putBoolean(PARAM_AUTO_PLAY, mAutoPlay);
        outState.putInt(PARAM_EPG_DATE_POSITION, mEpgDatePosition);
        outState.putSerializable(PARAM_SELECTED_CARD_DATA, mCardData);
        if (mPlayer != null) {
            outState.putLong(CURRENT_CONTENT_PROGRESS, mPlayer.getCurrentContentProgress());
        }
        outState.putInt(PARAM_EPG_DATE_POSITION, mEpgDatePosition);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView: isDetached:- " + isDetached());
        if (mContext == null) {
            mContext = getActivity();
            mBaseActivity = (BaseActivity) getActivity();
        }
        mInflater = LayoutInflater.from(mContext);
        rootView = inflater.inflate(R.layout.fragment_card_details, container, false);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            if (mBaseActivity != null) {
                mBaseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mBaseActivity.hideActionBar();
            }
        }
        mScrollView = (ScrollView) rootView
                .findViewById(R.id.carddetail_scroll_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.carddetail_progressBar);
        videoLayout = (RelativeLayout) rootView
                .findViewById(R.id.carddetail_videolayout);
        mTextViewProgressBar = (TextView) rootView.findViewById(R.id.card_loading_progress);
        mParentContentLayout = (LinearLayout) rootView
                .findViewById(R.id.carddetail_detaillayout);
        mRLayoutTimeShiftHelp = (RelativeLayout) rootView
                .findViewById(R.id.layout_timeshift_help_screen);

        //Log.d(TAG, "savedInstanceState- " + savedInstanceState);
        if (savedInstanceState == null) {
            savedInstanceState = getArguments();
            //Log.d(TAG, "arguments- " + savedInstanceState);
        }
        initializeBundleValues(savedInstanceState);
        initializePlayerView();
        fetchCardData();
        fetchPackageData(false);
        initToolbar();
        return rootView;
    }

    private void initializePlayerView() {
        if (ApplicationController.FLAG_ENABLE_ADS && mPartner == Partners.SONY) {
            mPlayer = new com.myplex.myplex.ads.PulseManager(mContext, mCardData, mId);
        } else {
            mPlayer = new CardVideoPlayer(mContext, mCardData, mId);
        }
        mPlayer.setNotificationTitle(mNotificationTitle);
        videoLayout.addView(mPlayer.CreatePlayerView(videoLayout));

        //Log.d(TAG, "CardDetails _id: " + mId);

        mPlayer.setVODContentType(mContentType);
        mPlayer.setPlayerStatusUpdateListener(this);
        mPlayer.setOnClickThroughCallback(new PulseManager.ClickThroughCallback() {
           /* @Override
            public void onClicked(PulseVideoAd ad) {*/
            /*    if (ad.getClickthroughURL() != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad
                            .getClickthroughURL().toString()));
                    startActivity(intent);
                } else {
                    mPlayer.returnFromClickThrough();
                }*/
            //}
        });
        mPlayer.setNid(mNid);
        if (mPlayer != null) {
            mPlayer.setEpgDatePosition(mEpgDatePosition);
        }
        mPlayer.setCurrentContentProgress(currentContentProgress);
        mCardDetailViewFactory = new CardDetailViewFactoryOld(mContext);
        mCardDetailViewFactory.setParent(rootView);
        mCardDetailViewFactory.setOnCardDetailExpandListener(this);

    }

    private void initializeBundleValues(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(
                PARAM_PARTNER_TYPE)) {
            mPartner = savedInstanceState.getInt(PARAM_PARTNER_TYPE);

            if (savedInstanceState.containsKey(PARAM_CARD_ID)) {
                mId = savedInstanceState.getString(PARAM_CARD_ID);
            }
            //Log.d(TAG, "CardDetails _id: " + mId);

            if (savedInstanceState.containsKey(APIConstants.NOTIFICATION_PARAM_NID)) {
                mNid = savedInstanceState.getString(APIConstants.NOTIFICATION_PARAM_NID);
            }

            if (savedInstanceState.containsKey(APIConstants.NOTIFICATION_PARAM_TITLE)) {
                mNotificationTitle = savedInstanceState.getString(APIConstants.NOTIFICATION_PARAM_TITLE);
            }

            if (savedInstanceState.containsKey(PARAM_AUTO_PLAY)) {
                mAutoPlay = savedInstanceState.getBoolean(PARAM_AUTO_PLAY);
            }

            if (savedInstanceState.containsKey(
                    PARAM_CARD_DATA_TYPE)) {
                mContentType = savedInstanceState.getString(PARAM_CARD_DATA_TYPE);
            }

            mCardData = null;
            if (savedInstanceState.containsKey(PARAM_SELECTED_CARD_DATA)) {
                mCardData = (CardData) savedInstanceState.getSerializable(
                        PARAM_SELECTED_CARD_DATA);
                mCacheManager.setSelectedCardData(mCardData);
            }

            if (savedInstanceState.containsKey(PARAM_EPG_DATE_POSITION)) {
                mEpgDatePosition = savedInstanceState.getInt(PARAM_EPG_DATE_POSITION);
            }

            if (savedInstanceState.containsKey(CURRENT_CONTENT_PROGRESS)) {
                currentContentProgress = savedInstanceState.getLong(CURRENT_CONTENT_PROGRESS);
                //Log.d(TAG, "currentContentProgress: " + currentContentProgress);
            }

        }
    }

    private View.OnClickListener mCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getActivity() != null && !getActivity().isFinishing()) {
                getActivity().finish();
            }
            //showOverFlowSettings(v);
        }
    };

    private void initToolbar() {

        mInflateView = LayoutInflater.from(mContext).inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mInflateView.findViewById(R.id.toolbar_header_title);
        mImageViewClose = (ImageView) mInflateView.findViewById(R.id.toolbar_settings_button);
        mToolbarLogo = (ImageView) mInflateView.findViewById(R.id.toolbar_tv_channel_Img);
        mRootLayout = (RelativeLayout) mInflateView.findViewById(R.id.custom_toolbar_layout);
        if (PrefUtils.getInstance().getPrefEnableDittoChannelLogoOnEpg()
                && mCardData != null
                && mCardData.contentProvider != null
                && mCardData.contentProvider.equalsIgnoreCase(APIConstants.TYPE_DITTO)) {
            mIsToShowToolBar = true;
            mToolbar.setVisibility(View.VISIBLE);
            mToolbarTitle.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(mCardData.globalServiceName)) {
                mToolbarTitle.setText(mCardData.globalServiceName);
                mToolbarTitle.setVisibility(View.VISIBLE);
            }
//            mToolbarLogo.setImageResource(R.drawable.xxhdpi_ditto_logo_android);
            PicassoUtil.with(mContext).load(APIConstants.getDittoChannelLogoUrl(),mToolbarLogo,R.drawable.black);
        }

        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRootLayout.setLayoutParams(layoutParams);
        mToolbar.addView(mInflateView);
        mImageViewClose.setOnClickListener(mCloseAction);
    }

    private void fetchCardData() {
        showProgressBar();
        mCacheManager.getCardDetails(mId, true, CardDetails.this);
    }

    private boolean isDebug = true;
    private boolean saveButtonAdded = false;
    private List<String> playerLogs = new ArrayList<>();

    @Override
    public void playerStatusUpdate(String value) {
        if (value == null)
            return;

        if ("Show Helpscreen".contains(value)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    int shownCountOfTimeShiftHelp = PrefUtils.getInstance().getPrefShownCountTimeShiftHelp();
                    int maxCountOfTimeShiftHelp = PrefUtils.getInstance().getPrefMaxDisplayCountTimeShift();
                    //Log.v(TAG, "shownCountOfTimeShiftHelp: " + shownCountOfTimeShiftHelp + " maxCountOfTimeShiftHelp: " + maxCountOfTimeShiftHelp + "isTimeShiftHelpScreenShown: " + isTimeShiftHelpScreenShown);
                    if (shownCountOfTimeShiftHelp < maxCountOfTimeShiftHelp
                            && !isTimeShiftHelpScreenShown
                            && mPlayer != null && mPlayer.isPlayingDVR()) {
                        //Log.v(TAG, "showing help screen");
                        isTimeShiftHelpScreenShown = true;
                        mPlayer.showMediaController();
                        mPlayer.setShowingHelpScreen(true);
                        PrefUtils.getInstance().setPrefShownCountTimeShiftHelp(++shownCountOfTimeShiftHelp);
                        showTimeShiftHelpScreen();
                    }
                }
            });
        }
        if (value.equalsIgnoreCase("ERR_USER_NOT_SUBSCRIBED")) {
            isToShowPacks = true;
            fetchPackageData(false);
        }
        if (value != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
            Date resultdate = new Date(System.currentTimeMillis());
            value = sdf.format(resultdate) + "::" + value;
            playerLogs.add(value);
        }
        if (!isDebug) {
            return;
        }
        if (mPlayerLogsLayout != null) {
            if (!saveButtonAdded) {
                RelativeLayout subLayout = new RelativeLayout(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                subLayout.setLayoutParams(params);
                mPlayerLogsLayout.addView(subLayout);
                TextView text = (TextView) mInflater.inflate(R.layout.pricepopmodeheading, null);
                text.setPadding(8, 12, 12, 8);
                text.setText("Player Logs:");
                text.setTextSize(18);
                text.setTextAppearance(mContext, R.style.TextAppearance_FontMedium);

                text.setTextColor(mContext.getResources().getColor(R.color.white));
                subLayout.addView(text);


                ImageView saveTofileSystem = new ImageView(getContext());
                int imagesize = (int) getContext().getResources().getDimension(R.dimen
                        .margin_gap_36);
                RelativeLayout.LayoutParams buttonparams = new RelativeLayout.LayoutParams(imagesize, imagesize);
                buttonparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                saveTofileSystem.setLayoutParams(buttonparams);
                UiUtil.showFeedback(saveTofileSystem);
                saveTofileSystem.setImageResource(R.drawable.download);
                saveTofileSystem.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        String path = mContext.getExternalFilesDir(null) + File.separator + "playerlogs.txt";

                        try {
                            File file = new File(path);
                            file.createNewFile();
                            if (file.exists()) {
                                OutputStream fo = new FileOutputStream(file);
                                for (String str : playerLogs) {
                                    fo.write(str.getBytes());
                                }
                                fo.close();
                            }
                            AlertDialogUtil.showToastNotification("Logs saved at " + path);
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dev@apalya.myplex.tv", "qa@apalya.myplex.tv"});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Player Logs");
                            String manufacturer = Build.MANUFACTURER;
                            String model = Build.MODEL;
                            intent.putExtra(Intent.EXTRA_TEXT, "Please find the attached logs for " + manufacturer + " " + model);
                            Uri uri = Uri.parse("file://" + file);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            startActivity(Intent.createChooser(intent, "Send email..."));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                });
                subLayout.addView(saveTofileSystem);
                saveButtonAdded = true;
            }
            TextView text = (TextView) mInflater.inflate(R.layout.pricepopmodeheading, null);
            text.setPadding(8, 2, 8, 2);
            text.setText(value);
            text.setTextAppearance(mContext, R.style.TextAppearance_FontRegular);
            text.setTextColor(mContext.getResources().getColor(R.color.white));
            mPlayerLogsLayout.addView(text);
        }
        updatePlayerLogVisiblity();
    }

    private void showPackagesPopup(CardData packsData) {
        if (packsData != null
                && packsData.packages != null
                && packsData.packages.size() > 0) {
            SubscriptionPacksDialog subscriptionPacksDialog = new SubscriptionPacksDialog(mContext);
            subscriptionPacksDialog.showDialog(packsData);
        }
    }

    boolean isToShowPacks = false;

    private void fetchPackageData(boolean isCacheRequest) {
        mAutoPlay = false;
        if (mCardData != null
                && mCardData.generalInfo != null
                && mCardData.generalInfo.type != null
                && mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                && mCardData.globalServiceId != null) {
            mId = mCardData.globalServiceId;
        } else if (mCardData != null
                && mCardData._id != null) {
            mId = mCardData._id;
        }
        mCacheManager.getCardDetails(mId, isCacheRequest, new CacheManager.CacheManagerCallback() {
            @Override
            public void OnCacheResults(List<CardData> dataList) {
                if (null == dataList) {
//                    AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
                    return;
                }
                for (CardData cardData : dataList) {
                    if (null != cardData
                            && null != cardData._id) {
                        if (cardData._id.equalsIgnoreCase(mId)) {
                            SDKUtils.getCardExplorerData().cardDataToSubscribe = mCardData;
                            showPackages(cardData);
                            if (isToShowPacks) {
                                isToShowPacks = false;
                                showPackagesPopup(cardData);
                            }
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
                        if (cardData._id.equalsIgnoreCase(mId)) {
                            SDKUtils.getCardExplorerData().cardDataToSubscribe = mCardData;
                            showPackages(cardData);
                            if (isToShowPacks) {
                                isToShowPacks = false;
                                showPackagesPopup(cardData);
                            }
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


    @Override
    public void onCloseFragment() {

    }

    private void updatePlayerLogVisiblity() {
        if (mPlayerLogsLayout == null) {
            return;
        }

        if (ApplicationController.SHOW_PLAYER_LOGS) {
            if (playerLogs != null
                    && !playerLogs.isEmpty()) {
                mTitleSectionLayoutPlayerLogs.setVisibility(View.VISIBLE);
                mPlayerLogsLayout.setVisibility(View.VISIBLE);
            }
        } else {
            mTitleSectionLayoutPlayerLogs.setVisibility(View.GONE);
            mPlayerLogsLayout.setVisibility(View.GONE);
        }
    }

    private void fillData() {
        View v;

        if (mCardData != null
                && mCardData.generalInfo != null) {
            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)) {
//                Analytics.createScreenGA(Analytics.SCREEN_PROGRAM_DETAILS);
                FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_PROGRAM_DETAILS);
            } else if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mCardData.generalInfo.type)) {
//                Analytics.createScreenGA(Analytics.SCREEN_MOVIE_DETAILS);
                FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_MOVIE_DETAILS);
            } else if (APIConstants.TYPE_VOD.equalsIgnoreCase(mCardData.generalInfo.type)) {
                if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mContentType)) {
//                    Analytics.createScreenGA(Analytics.SCREEN_TV_SHOW_DETAILS);
                    FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_TV_SHOW_DETAILS);
                } else if (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(mContentType)
                        || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(mContentType)) {
//                    Analytics.createScreenGA(Analytics.SCREEN_VOD_DETAILS);
                    FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_VOD_DETAILS);
                }
            } else if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)) {
//                Analytics.createScreenGA(Analytics.SCREEN_PROGRAM_DETAILS);
                FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_PROGRAM_DETAILS);
            }
        }
        mTitleSectionLayoutPlayerLogs = new LinearLayout(mContext);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mTitleSectionLayoutPlayerLogs.setLayoutParams(descParams);
        v = mCardDetailViewFactory.createTitleSectionView(mContext.getString(R.string.player_logs),"");
        if (v != null) {
            mParentContentLayout.addView(mTitleSectionLayoutPlayerLogs);
            mTitleSectionLayoutPlayerLogs.addView(v);
        }
        mTitleSectionLayoutPlayerLogs.setVisibility(View.GONE);
        mPlayerLogsLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mPlayerLogsLayout.setLayoutParams(playParams);
        mPlayerLogsLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutTransition transition = new LayoutTransition();
        transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        mPlayerLogsLayout.setLayoutTransition(transition);
        mPlayerLogsLayout.setVisibility(View.GONE);
        mParentContentLayout.addView(mPlayerLogsLayout);

        mDescriptionContentLayout = new LinearLayout(mContext);
        mDescriptionContentLayout.setLayoutParams(descParams);


        mTitleSectionLayoutPacks = new LinearLayout(mContext);
        mTitleSectionLayoutPacks.setLayoutParams(descParams);
        mTitleSectionLayoutComments = new LinearLayout(mContext);
        mTitleSectionLayoutComments.setLayoutParams(descParams);


        mPackagesLayout = new LinearLayout(mContext);
        mPackagesLayout.setLayoutParams(descParams);

        mCommentsContentLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mCommentsContentLayout.setLayoutParams(commentParams);

        v = mCardDetailViewFactory.CreateView(mCardData,
                CardDetailViewFactory.CARDDETAIL_BRIEF_DESCRIPTION);
        if (v != null) {
            mParentContentLayout.addView(mDescriptionContentLayout);
            mDescriptionContentLayout.addView(v);
        }

        v = mCardDetailViewFactory.createTitleSectionView("Packs","");

        if (v != null) {
            mParentContentLayout.addView(mTitleSectionLayoutPacks);
            mTitleSectionLayoutPacks.addView(v);
        }
        mTitleSectionLayoutPacks.setVisibility(View.GONE);

        v = mCardDetailViewFactory.CreateView(mCardData,
                CardDetailViewFactory.CARDDETAIL_PACKAGES_VIEW);
        if (v != null) {
            mParentContentLayout.addView(mPackagesLayout);
            mPackagesLayout.addView(v);
        }
        mPackagesLayout.setVisibility(View.GONE);

       /* v = mCardDetailViewFactory.createTitleSectionView("Comments");

        if (v != null) {
            mParentContentLayout.addView(mTitleSectionLayoutComments);
            mTitleSectionLayoutComments.addView(v);
        }

        v = mCardDetailViewFactory.CreateView(mCardData,
                CardDetailViewFactory.CARDDETAIL_COMMENTS_VIEW);
        if (v != null) {
           // mParentContentLayout.addView(mCommentsContentLayout);
           // mCommentsContentLayout.addView(v);
        }
*/
    }

    private void showPackages(CardData packsData) {
        if (packsData == null
                && packsData.packages == null) {
            return;
        }
        float price = 10000.99f;
        if (packsData.packages == null || packsData.packages.size() == 0) {
            //comingsoon content
        } else {
            if (packsData.currentUserData != null && packsData.currentUserData.purchase != null && packsData.currentUserData.purchase.size() != 0) {
                //paid content
                if (mPackagesLayout != null) {
                    mTitleSectionLayoutPacks.setVisibility(View.GONE);
                    mPackagesLayout.setVisibility(View.GONE);
                }
            } else {
                for (CardDataPackages packageitem : packsData.packages) {
                    if (packageitem.priceDetails != null) {
                        for (CardDataPackagePriceDetailsItem priceDetailItem : packageitem.priceDetails) {
                            if (!priceDetailItem.paymentChannel.equalsIgnoreCase(APIConstants
                                    .PAYMENT_CHANNEL_INAPP) && priceDetailItem.price < price) {
                                price = priceDetailItem.price;
                                if (price > 0.0) {
                                    mPackagesLayout.setVisibility(View.VISIBLE);
                                    mTitleSectionLayoutPacks.setVisibility(View.VISIBLE);
                                    if (mPackagesLayout != null) {
                                        mPackagesLayout.removeAllViews();
                                    }
                                    View v = mCardDetailViewFactory.CreateView(packsData, CardDetailViewFactory.CARDDETAIL_PACKAGES_VIEW);

                                    if (v != null) {
                                        mPackagesLayout.addView(v);
                                    }
                                    mPackagesLayout.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                        }
                    } else {
//                        comingsoon content
                    }

                }
            }

        }
    }

    @Override
    public boolean onBackClicked() {
        try {
            if (mPlayer.isFullScreen()) {
//                if (!mContext.getResources().getBoolean(R.bool.isTablet)) {
                if (mPlayer.getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mPlayer.resumePreviousOrientaionTimer();
                } else {
                    ((BaseActivity) mContext).setOrientation(ActivityInfo
                            .SCREEN_ORIENTATION_LANDSCAPE);
                    mPlayer.resumePreviousOrientaionTimer();
                }
//                }
                mPlayer.setFullScreen(!mPlayer.isFullScreen());
                return true;
            }

            /*if ( mPlayer.isMediaPlaying() && !isMinimized) {
                onViewChanged(true);
                return true;
            }*/

//			if(isMinimized){
//				return false;
//			}

            //Log.d(TAG, "mContentType: " + mContentType);
            if (mPlayer.isMediaPlaying()) {
                mPlayer.stopMOUTracking();
                mPlayer.onStateChanged(PlayerListener.STATE_PAUSED, mPlayer.getStopPosition());
                mPlayer.closePlayer();
//                return isMinimized ? false : true;
            }
            if (mCardData != null
                    && mCardData.generalInfo != null
                    && mCardData.generalInfo.title != null) {
                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mCardData.generalInfo.type)) {
                    Analytics.gaBrowseProgramDetails(mCardData.generalInfo.title);
                }
                if (APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(mCardData.generalInfo.type)) {
                    String subgenre = null;
                    if (mCardData.content != null
                            && mCardData.content.genre != null
                            && mCardData.content.genre.size() > 0) {
                        subgenre = mCardData.content.genre.get(0).name;
                    }
                    Analytics.gaPlayedVideoTimeCalculationForYoutube(Analytics.ACTION_TYPES.play.name(), mCardData.generalInfo.title, mCardData._id, subgenre, (mCardData == null || mCardData.publishingHouse == null) ? APIConstants.NOT_AVAILABLE : mCardData.publishingHouse.publishingHouseName);
                }
            }

            Analytics.setVideosCarouselName(null);
            return false;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void onViewChanged(boolean isMinimized) {

        if (rootView == null) {
            return;
        }
        this.isMinimized = isMinimized;
        View view = rootView.findViewById(R.id.draggable_view);

        if (isMinimized) {

            mScrollView.setVisibility(View.GONE);
            if (mPlayer != null)
                mPlayer.minimize();
            view.getLayoutParams().height = mPlayer.getHeight();

            mBaseActivity.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            view.requestLayout();

            mBaseActivity.showActionBar();
            return;
        }

        view.getLayoutParams().height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        if (mPlayer != null)
            mPlayer.maximize();
        mScrollView.setVisibility(View.VISIBLE);
        mBaseActivity.hideActionBar();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /*if (mBottomActionBar != null) {
                mBottomActionBar.setVisibility(View.INVISIBLE);
			}*/
            if (mPlayer != null && !mPlayer.isYouTubePlayerLaunched()) {
                hideToobar();
                mPlayer.playInLandscape();
            }

        } else {
            /*if (mBottomActionBar != null) {
                mBottomActionBar.setVisibility(View.VISIBLE);
			}*/
            if (mPlayer != null && !mPlayer.isYouTubePlayerLaunched()) {
                showToolbar();
                mPlayer.playInPortrait();
            }

        }
        super.onConfigurationChanged(newConfig);
    }

    private void showToolbar() {
        if (mToolbar == null || !mIsToShowToolBar) {
            return;
        }
        mToolbar.setVisibility(View.VISIBLE);
    }

    private void hideToobar() {
        if (mToolbar == null) {
            return;
        }
        mToolbar.setVisibility(View.GONE);
    }

    @Override
    public void OnCacheResults(List<CardData> dataList) {
        hideProgressBar();
        if (null == dataList) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
            return;
        }
        for (CardData cardData : dataList) {
            if (null != cardData) {
                if (cardData.globalServiceId != null && cardData.globalServiceId
                        .equalsIgnoreCase(mId)) {
                    mCardData = cardData;
                    if (null != mPlayer) {
                        mPlayer.updateCardPreviewImage(mCardData);
                    }
                    fillData();
                } else if (cardData._id != null && cardData._id
                        .equalsIgnoreCase(mId)) {
                    mCardData = cardData;
                    if (null != mPlayer) {
                        mPlayer.updateCardPreviewImage(mCardData);

                    }
                    fillData();
                }
            }
            if (mAutoPlay) {
                mPlayer.playContent();
            }
        }

    }

    @Override
    public void OnOnlineResults(List<CardData> dataList) {
        hideProgressBar();
        if (null == dataList) {
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
            return;
        }
        for (CardData cardData : dataList) {

            if (null != cardData) {
                if (cardData.globalServiceId != null && cardData.globalServiceId
                        .equalsIgnoreCase(mId)) {
                    mCardData = cardData;
                    if (null != mPlayer) {
                        mPlayer.updateCardPreviewImage(mCardData);
                    }
                    fillData();
                } else if (cardData._id != null && cardData._id
                        .equalsIgnoreCase(mId)) {
                    mCardData = cardData;
                    if (null != mPlayer) {
                        mPlayer.updateCardPreviewImage(mCardData);
                    }
                    fillData();
                }
            }
        }
        if (mAutoPlay) {
            mPlayer.playContent();
        }

    }

    @Override
    public void OnOnlineError(Throwable error, int errorCode) {
        if (errorCode == APIRequest.ERR_NO_NETWORK) {
            AlertDialogUtil.showNeutralAlertDialog(mContext, mContext.getString(R.string.network_error),
                    "", mContext.getString(R.string.play_button_retry), mNeutralDialogListener);
            return;
        }
        AlertDialogUtil.showToastNotification(mContext.getString(R.string.data_fetch_error));
    }

    private void hideProgressBar() {
        if (null != mTextViewProgressBar) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mTextViewProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showProgressBar() {
        if (null != mTextViewProgressBar) {
            mProgressBar.setVisibility(View.VISIBLE);
            mTextViewProgressBar.setVisibility(View.VISIBLE);
        }
    }

    //time analytics
    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "CardDetails: onPause");
        if (mPlayer != null) {
            duringAd = mPlayer.isAdPlaying();
            mPlayer.removeCallback(PulseManager.contentProgressHandler);
            if (mPlayer.isMediaPlaying()) {
                mPlayer.stopMOUTracking();
                mPlayer.onStateChanged(PlayerListener.STATE_PAUSED, mPlayer.getStopPosition());
                mPlayer.onPause();
                hideTimeShiftHelpScreen();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "CardDetails: onResume");

        updatePlayerLogVisiblity();

        String mId = null;
        String subscribeDataId = null;

        if (mCardData != null
                && mCardData.generalInfo != null
                && mCardData.generalInfo.type != null) {
            if (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                    && mCardData.globalServiceId != null) {
                mId = mCardData.globalServiceId;
            } else if (mCardData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    && mCardData._id != null) {
                mId = mCardData._id;
            }
        }
        if (SDKUtils.getCardExplorerData().cardDataToSubscribe != null
                && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo != null
                && SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type != null) {
            if (SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId != null) {
                subscribeDataId = SDKUtils.getCardExplorerData().cardDataToSubscribe.globalServiceId;
            } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    && SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                subscribeDataId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
            } else if (SDKUtils.getCardExplorerData().cardDataToSubscribe._id != null) {
                subscribeDataId = SDKUtils.getCardExplorerData().cardDataToSubscribe._id;
            }
        }
        if (subscribeDataId != null && mId != null) {
            if (subscribeDataId.equalsIgnoreCase(mId)) {
                mCardData = SDKUtils.getCardExplorerData().cardDataToSubscribe;
                if (mCardDetailViewFactory != null) {
                    fetchPackageData(false);
                }
            }
        }

        if (mPlayer == null) {
            return;
        }

        if (duringAd) {
            mPlayer.returnFromClickThrough();
            return;
        }

        if (!mPlayer.isMediaPlaying()) {
            //  mPlayer.playContent();
            mPlayer.onResume();
        }

        if (mAutoPlay) {
            mPlayer.playContent();
        }
    }

    private void showTimeShiftHelpScreen() {
        if (mRLayoutTimeShiftHelp == null) {
            return;
        }
//        if (mContext != null) {
//            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        mRLayoutTimeShiftHelp.setVisibility(View.VISIBLE);
        mHandlerShowMediaController.postDelayed(mRunnableShowMediaController, DEFAULT_MEDIACTROLLER_TIMEOUT);
        mRLayoutTimeShiftHelp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideTimeShiftHelpScreen();
                return true;
            }
        });
    }

    private void hideTimeShiftHelpScreen() {
        if (mPlayer != null) {
            mPlayer.resumePreviousOrientaionTimer();
        }
        mPlayer.setShowingHelpScreen(false);
        mHandlerShowMediaController.removeCallbacks(mRunnableShowMediaController);
        mRLayoutTimeShiftHelp.setVisibility(View.GONE);
    }

    public void updateBundleData(Bundle arguments) {
        initializeBundleValues(arguments);
        initializePlayerView();
        fetchCardData();
        fetchPackageData(false);
        initToolbar();
    }

    @Override
    public void onPlayTrailer() {

    }

    @Override
    public void onSimilarMoviesDataLoaded(String status) {

    }

    @Override
    public void onShowPopup() {

    }

    @Override
    public void onPopupItemSelected(String date) {

    }

    @Override
    public void onDownloadContent(FragmentCardDetailsDescription.DownloadStatusListener downloadStatusListener) {

    }

    @Override
    public void onSeasonDataLoaded(List<String> seasonsList) {

    }

    @Override
    public void onEpiosodesLoaded(List<CardData> episodes, boolean isLoadMore) {

    }


    @Override
    public void onSeasonNotAvailable() {

    }

    @Override
    public FragmentManager getSuperChildFragmentManager() {
        return getChildFragmentManager();
    }


    public class Partners {
        public static final int APALYA = 1;
        public static final int HOOQ = 2;
        public static final int SONY = 3;
        public static final int DITTO = 3;
        public static final int HUNGAMA = 4;
        public static final int ALTBALAJI = 5;
        public static final int EROSNOW = 6;
    }
}
