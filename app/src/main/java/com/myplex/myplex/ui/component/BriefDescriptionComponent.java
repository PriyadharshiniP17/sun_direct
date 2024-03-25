package com.myplex.myplex.ui.component;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.myplex.api.APIConstants.IS_Subcriped;
import static com.myplex.api.APIConstants.PWA_URL;
import static com.myplex.myplex.ApplicationController.getApplicationConfig;
import static com.myplex.myplex.ApplicationController.getDownloadData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.pedrovgs.LoggerD;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.exoplayer2.ParserException;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.formats.AdManagerAdViewOptions;
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.ContentLikedRequest;
import com.myplex.api.request.content.FavouriteCheckRequest;
import com.myplex.api.request.content.FavouriteRequest;
import com.myplex.api.request.content.FetchLikeCheckRequest;
import com.myplex.api.request.content.WatchListCheckRequest;
import com.myplex.api.request.content.WatchListRequest;
import com.myplex.model.AdLayoutsData;
import com.myplex.model.AdSizes;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CardDataHolder;
import com.myplex.model.CardDataImages;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataRelatedCastItem;
import com.myplex.model.CardDataRelatedMultimediaItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.LanguageTitleData;
import com.myplex.model.PublishingHouse;
import com.myplex.model.RelatedCastList;
import com.myplex.model.SeasonData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.download.Decompress;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.events.ContentDownloadEvent;
import com.myplex.myplex.events.DownloadDeleteEvent;
import com.myplex.myplex.events.RefreshPotraitUI;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.fragment.epg.EPGUtil;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.Entities;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.LocaleFontUtil;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.player_config.DownloadManagerMaintainer;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

public class BriefDescriptionComponent extends GenericListViewCompoment implements
        FetchDownloadProgress.DownloadProgressStatus, DialogInterface.OnClickListener {

    private final ImageView sharebtnimage;
    private final TextView descriptionexpandtext;
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private CardData mData;
    private CardData mDataForLikes;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;
    private static final String TAG = BriefDescriptionComponent.class.getSimpleName();
    private View programthumnailcontainer;
    private TextView relatedcastdescriptiontext;
    private View relatedcastdescriptionlayout;
    private ImageView liveChannelImageIcon;
    private TextView carddetailbriefdescription_duration;
    private View year_duration_seperator;
    private TextView carddetailbriefdescription_language;
    private TextView lang_genre_seperator;
    private RelativeLayout main_brief_description_layout;
    private LinearLayout latest_episode_button_layout;
    private TextView episode_number_text;

    private ImageView downloadingGIFAnim;
    private TextView downloadbtnStatusPercentText;
    private LinearLayout downloadbtnLayout;
    private RelativeLayout trailerLayout;
    private boolean isToShowDownloadButton;
    private CardData mTVShowData;
    private View description_layout;
    private String tabName;
    private String mBgColor;
    private TextView movieName;
    private TextView releaseDate;
    private TextView content_timings;
    private TextView moviedescription;
    private TextView moviegenre;
    private RatingBar rating;
    private TextView castheadingtext;
    private TextView movietrailertext;
    private View castlayout;
    private View castNewLayout;
    private TextView castValue, director_value,language;
    private TextView carddetailbriefdescription_cast;
    private ImageView mLangConIcon;
    private ImageView downloadbtnImage;
    private TextView castexpandtext;
    private ImageView mFavouriteImgBtn;
    private Handler mHandler = null;
    private TextView buyBtn;
    private ImageView sideImageViewIcon;
    private RelativeLayout thumbnail_movie_layout;
    private TextView watchListText;
    private TextView detailLikeText;
    private TextView favouriteText;
    private TextView shareText;
    private TextView authorNameTv;
    private TextView downloadText;
    private TextView detailDownloadText;
    private ImageView detailLikeImage;
    private ImageView watchListImage;
    private ImageView favouriteImage;
    private LinearLayout watchListLayout;
    private LinearLayout shareLayout;
    private LinearLayout likeLayout;
    private LinearLayout downloadLayoutL;
    private LinearLayout favouriteLayout;
    private LinearLayout newsauthor;
    private FrameLayout bannerCustomAdContainer;
    private LinearLayout tvShowTitleContainer;
    private TextView tvShowTitleText;
    private LinearLayout packExpiryLL;
    private TextView packExpityText, packLimit;
    TemplateView template;

    private static boolean isContentLiked;
    private static String contentId;
    private static String contentType;

    private ViewGroup contentRatingLayout;
    private TextView ageNumberTv, ageDescriptionTv;
    private Integer contentDuration = 0;


    private TextView viewsCountTv;
    private LinearLayout viewsCountLayout;
    private UnifiedNativeAd nativeAd;
    private FrameLayout mAdLayout;


    LinearLayout ll_cast, ll_director,ll_audioLanguage;

    LinearLayout ll_buy, ll_expiry;
    ImageView iv_expiry_icon;
    TextView tv_expiry_time_left;

    private Typeface mBoldTypeFace;
    Typeface mRegularTypeFace;

    private java.lang.Runnable mDismissProgressTask = new Runnable() {
        @Override
        public void run() {
            isToShowDownloadButton = false;
            checkAndEnableDownload(mData != null
                    && mData.generalInfo != null
                    && mData.generalInfo.isDownloadable
                    && ApplicationController.ENABLE_DOWNLOADS);
            AlertDialogUtil.dismissProgressAlertDialog();
            AlertDialogUtil.showToastNotification(mContext.getString(R.string.vf_download_error_while_download));
        }
    };
    private View.OnClickListener mTrailerButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onPlayTrailer();
            }
        }
    };
    private View.OnClickListener mBuyButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onBuy();
            }
        }
    };

    private View.OnClickListener mLocalFontListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
//            Util.showFeedback(v);
            Integer isNative;
            CardDataHolder mCardDataHolder;
            TextView descriptionView;
            TextView titleView;
            if (v.getTag() instanceof CardDataHolder) {
                mCardDataHolder = (CardDataHolder) v.getTag();
                descriptionView = mCardDataHolder.mCardDescText;
                titleView = mCardDataHolder.mTitle;
                isNative = (Integer) descriptionView.getTag();
            } else {
                return;
            }
            switch (isNative) {
                case LocaleFontUtil.NATIVE:
                    if (mData.generalInfo.description != null) {
                        descriptionView
                                .setText(mData.generalInfo.description);
                        titleView.setText(mData.generalInfo.title);
                    }
                    descriptionView.setTag(LocaleFontUtil.NONNATIVE);
                    v.setTag(mCardDataHolder);
                    break;
                case LocaleFontUtil.NONNATIVE:
                    v.setTag(LocaleFontUtil.NATIVE);
                    if (!LocaleFontUtil.setLocalFontIfAvailable(mContext, mData,
                            descriptionView)) {
                        descriptionView.setText(mData.generalInfo.description);
                        titleView.setText(mData.generalInfo.title);
                        descriptionView.setTag(LocaleFontUtil.NATIVE);
                        v.setTag(mCardDataHolder);
                    } else {
                        descriptionView.setText(mData.generalInfo.altDescription
                                .get(0).description);
                        titleView.setText(mData.generalInfo.altTitle
                                .get(0).title);
                        descriptionView.setTag(LocaleFontUtil.NATIVE);
                        v.setTag(mCardDataHolder);
                    }
                    break;
                default:
                    break;
            }
        }

    };


    private ErosNowDownloadManager.UnzipProcessListener mUnzipProgressListener = new ErosNowDownloadManager.UnzipProcessListener() {
        @Override
        public void onCompletion(CardDownloadData cardDownloadData) {
            LoggerD.debugDownload("unzip done for ");
            DownloadProgress(mData, cardDownloadData);
        }

        @Override
        public void onFailure() {

        }
    };

    private View.OnClickListener mFavouritesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            favouriteLayout.setEnabled(false);
            favouriteLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (favouriteLayout != null)
                        favouriteLayout.setEnabled(true);
                }
            }, 2000);

            //Util.showFeedback(watchListLayout);
//                favouritebtnimage.setImageResource(0);
//                AlertDialogUtil.showToastNotification("work in progress");
            String type = mData.generalInfo.type;
            String _id = mData._id;
            if (mTVShowData != null
                    && (mTVShowData.isVODYoutubeChannel()
                    || mTVShowData.isTVSeason()
                    || mTVShowData.isVODChannel()
                    || mTVShowData.isVODCategory()
                    || mTVShowData.isTVSeries())) {
                type = mTVShowData.generalInfo.type;
                _id = mTVShowData._id;
            }
            if (mData.isProgram()) {
                _id = mData.globalServiceId;
                type = APIConstants.TYPE_LIVE;
            }
            FavouriteRequest.Params favouritesParams = new FavouriteRequest.Params(_id, type);

//                AlertDialogUtil.showToastNotification("Please wait while we update the data...");
            final String final_id = _id;
            FavouriteRequest mRequestFavourites = new FavouriteRequest(favouritesParams,
                    new APICallback<FavouriteResponse>() {
                        @Override
                        public void onResponse(APIResponse<FavouriteResponse> response) {
                            if (response == null
                                    || response.body() == null) {
                                return;
                            }
                            if (response.body().code == 402) {
                                PrefUtils.getInstance().setPrefLoginStatus("");
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.login_required));
                                return;
                            }

                            //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                            if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                                showWatchListAndFavouritesButton(true);
//                                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
//                                    showFavaouriteButton(false);
//                                }
                                if (mData != null
                                        && mData.currentUserData != null) {
                                    mData.currentUserData.favorite = response.body().favorite;
                                }
                                if (response.body().favorite) {
                                    updateFavouriteButtonBgAndText(true);
                                    AlertDialogUtil.showToastNotification("Added to Favourites");
                                    PrefUtils.getInstance().shouldChangeFavouriteState(final_id, true, tabName);
                                    PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER, true);
                                    ScopedBus.getInstance().post(new RefreshPotraitUI());
                                    if (mData != null && mData._id != null && !TextUtils.isEmpty(mData._id)) {
                                        CleverTap.eventPromoVideoAddedToWatchList(APIConstants.NOT_AVAILABLE, mData._id, CleverTap.SOURCE_DETAILS_SCREEN);
                                    }
                                    SDKLogger.debug("Added to favourites");
                                    FirebaseAnalytics.getInstance().addOrRemoveFromWatchList(true, mData);
                                    ComScoreAnalytics.getInstance().setEventAddToWatchList(mData, true, CleverTap.SOURCE_DETAILS_SCREEN);
                                    ComScoreAnalytics.getInstance().setEventFavourite(mData, true);
                                } else {
                                    AlertDialogUtil.showToastNotification("Removed from Favourites");
                                    updateFavouriteButtonBgAndText(false);
                                    PrefUtils.getInstance().shouldChangeFavouriteState(final_id, false, tabName);
                                    PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER, true);
                                    ScopedBus.getInstance().post(new RefreshPotraitUI());
                                    SDKLogger.debug("Removed from favourites");
                                    FirebaseAnalytics.getInstance().addOrRemoveFromWatchList(false, mData);
                                    ComScoreAnalytics.getInstance().setEventAddToWatchList(mData, false, CleverTap.SOURCE_DETAILS_SCREEN);
                                    ComScoreAnalytics.getInstance().setEventFavourite(mData, false);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            //Log.d(TAG, "FavouriteRequest: onResponse: t- " + t);
                            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                                return;
                            }
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                        }
                    });
            APIService.getInstance().execute(mRequestFavourites);
        }
    };


    private View.OnClickListener mWatchListClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            watchListLayout.setEnabled(false);
            watchListLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (watchListLayout != null)
                        watchListLayout.setEnabled(true);
                }
            }, 2000);

            //Util.showFeedback(watchListLayout);
//                favouritebtnimage.setImageResource(0);
//                AlertDialogUtil.showToastNotification("work in progress");
            String type = mData.generalInfo.type;
            String _id = mData._id;
            if (mTVShowData != null
                    && (mTVShowData.isVODYoutubeChannel()
                    || mTVShowData.isTVSeason()
                    || mTVShowData.isVODChannel()
                    || mTVShowData.isVODCategory()
                    || mTVShowData.isTVSeries())) {
                type = mTVShowData.generalInfo.type;
                _id = mTVShowData._id;
            }
            if (mData.isProgram()) {
                _id = mData.globalServiceId;
                type = APIConstants.TYPE_LIVE;
            }
            WatchListRequest.Params favouritesParams = new WatchListRequest.Params(_id, type);

//                AlertDialogUtil.showToastNotification("Please wait while we update the data...");
            final String final_id = _id;
            WatchListRequest mRequestFavourites = new WatchListRequest(favouritesParams,
                    new APICallback<FavouriteResponse>() {
                        @Override
                        public void onResponse(APIResponse<FavouriteResponse> response) {
                            if (response == null
                                    || response.body() == null) {
                                return;
                            }
                            if (response.body().code == 402) {
                                PrefUtils.getInstance().setPrefLoginStatus("");
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.login_required));
                                return;
                            }

                            //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                            if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                                showWatchListAndFavouritesButton(true);
//                                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
//                                    showFavaouriteButton(false);
//                                }
                                if (mData != null
                                        && mData.currentUserData != null) {
                                    mData.currentUserData.watchlist = response.body().watchlist;
                                }
                                if (response.body().watchlist) {
                                    updateWatchListButtonBgAndIcon(true);
                                    AlertDialogUtil.showToastNotification("Added to Watchlist");
                                    PrefUtils.getInstance().shouldChangeFavouriteState(final_id, true, tabName);
                                    PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER, true);
                                    ScopedBus.getInstance().post(new RefreshPotraitUI());
                                    if (mData != null && mData._id != null && !TextUtils.isEmpty(mData._id)) {
                                        CleverTap.eventPromoVideoAddedToWatchList(APIConstants.NOT_AVAILABLE, mData._id, CleverTap.SOURCE_DETAILS_SCREEN);
                                    }
                                    SDKLogger.debug("Added to Watchlist");
                                    FirebaseAnalytics.getInstance().addOrRemoveFromWatchList(true, mData);
                                    ComScoreAnalytics.getInstance().setEventAddToWatchList(mData, true, CleverTap.SOURCE_DETAILS_SCREEN);
                                } else {
                                    AlertDialogUtil.showToastNotification("Removed from Watchlist");
                                    updateWatchListButtonBgAndIcon(false);
                                    PrefUtils.getInstance().shouldChangeFavouriteState(final_id, false, tabName);
                                    PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER, true);
                                    ScopedBus.getInstance().post(new RefreshPotraitUI());
                                    SDKLogger.debug("Removed from Watchlist");
                                    FirebaseAnalytics.getInstance().addOrRemoveFromWatchList(false, mData);
                                    ComScoreAnalytics.getInstance().setEventAddToWatchList(mData, false, CleverTap.SOURCE_DETAILS_SCREEN);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            //Log.d(TAG, "FavouriteRequest: onResponse: t- " + t);
                            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                                return;
                            }
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                        }
                    });
            APIService.getInstance().execute(mRequestFavourites);
        }
    };
    private View.OnClickListener descriptionexpandtextListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            boolean isEllipsized = false;
            Layout l = moviedescription.getLayout();
            if (l != null) {
                int lines = l.getLineCount();
                if (lines > 0) {
                    if (l.getEllipsisCount(lines - 1) > 0) {
                        isEllipsized = true;
                    }
                }
            }

            if (isEllipsized) {
                try {
                    moviedescription.setEllipsize(null);
                    moviedescription.setMaxLines(Integer.MAX_VALUE);
                    descriptionexpandtext.setText(mContext.getString(R.string.msg_show_less_description));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            moviedescription.setMaxLines(3);
            moviedescription.setEllipsize(TextUtils.TruncateAt.END);
            descriptionexpandtext.setText(mContext.getString(R.string.msg_read_more_description));

        }
    };

    private View.OnClickListener castexpandtextListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            boolean isEllipsized = false;
            Layout l = carddetailbriefdescription_cast.getLayout();
            if (l != null) {
                int lines = l.getLineCount();
                if (lines > 0) {
                    if (l.getEllipsisCount(lines - 1) > 0) {
                        isEllipsized = true;
                    }
                }
            }

            if (isEllipsized) {
                try {
                    carddetailbriefdescription_cast.setEllipsize(null);
                    carddetailbriefdescription_cast.setMaxLines(Integer.MAX_VALUE);
                    castexpandtext.setText(mContext.getString(R.string.msg_show_less_description));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            carddetailbriefdescription_cast.setMaxLines(2);
            carddetailbriefdescription_cast.setEllipsize(TextUtils.TruncateAt.END);
            castexpandtext.setText(mContext.getString(R.string.msg_read_more_description));

        }
    };

    private Runnable moviedescriptionLineAlignTask = new Runnable() {
        @Override
        public void run() {
            if (descriptionexpandtext == null) {
                return;
            }
            descriptionexpandtext.setVisibility(View.GONE);
            Layout l = moviedescription.getLayout();
            if (l != null) {
                int lines = l.getLineCount();
                if (lines > 0) {
                    if (l.getEllipsisCount(lines - 1) > 0) {
                        descriptionexpandtext.setVisibility(View.VISIBLE);
                    }
                }
            }

        }
    };

    private Runnable castLineAlignTask = new Runnable() {
        @Override
        public void run() {
            if (castexpandtext == null) {
                return;
            }
            castexpandtext.setVisibility(View.GONE);
            Layout l = carddetailbriefdescription_cast.getLayout();
            if (l != null) {
                int lines = l.getLineCount();
                if (lines > 0) {
                    if (l.getEllipsisCount(lines - 1) > 0) {
                        castexpandtext.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };


    public BriefDescriptionComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        mHandler = new Handler(Looper.getMainLooper());
        movieName = view.findViewById(R.id.carddetailbreifdescription_movename);
        content_timings = view.findViewById(R.id.content_duration);

        //movieName.setTypeface(ResourcesCompat.getFont(mContext,R.font.indulekha));
        programthumnailcontainer = view.findViewById(R.id.carddetailbreifdescription_program_thumbnail_container);
        releaseDate = view.findViewById(R.id.carddetailbriefdescription_releasedate);
        description_layout = view.findViewById(R.id.description_layout);
        moviedescription = view.findViewById(R.id.carddetailbriefdescription_description);
        //moviedescription.setTypeface(ResourcesCompat.getFont(mContext,R.font.panchari));
        moviegenre = view.findViewById(R.id.carddetailbriefdescription_genre);
        rating = view.findViewById(R.id.watchNowRating);
        thumbnail_movie_layout = view.findViewById(R.id.thumbnail_movie_layout);
        sideImageViewIcon = view.findViewById(R.id.imageviewsideicon);
        castlayout = view.findViewById(R.id.cast_layout);
        castNewLayout = view.findViewById(R.id.new_cast_layout);
        castValue = view.findViewById(R.id.cast_value);
        language = view.findViewById(R.id.language_value);
        director_value = view.findViewById(R.id.director_value);
        ll_cast = view.findViewById(R.id.ll_cast);
        ll_audioLanguage = view.findViewById(R.id.multi_audio_language);
        ll_director = view.findViewById(R.id.ll_director);

        carddetailbriefdescription_cast = view.findViewById(R.id.carddetailbriefdescription_cast);
        castexpandtext = view.findViewById(R.id.carddetailbriefdescription_cast_readmore);
        castheadingtext = view.findViewById(R.id.carddetailbriefdescription_cast_heading);
        movietrailertext = view.findViewById(R.id.txt_trailer);
        buyBtn = view.findViewById(R.id.buy_btn);
        ll_buy= view.findViewById(R.id.ll_buy);
        ll_expiry= view.findViewById(R.id.ll_expiry);
        iv_expiry_icon= view.findViewById(R.id.iv_expiry_icon);
        tv_expiry_time_left= view.findViewById(R.id.tv_expiry_time_left);


        mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");
        mRegularTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_regular.ttf");
        buyBtn.setTypeface(mRegularTypeFace);
        tv_expiry_time_left.setTypeface(mRegularTypeFace);

        mLangConIcon = view.findViewById(R.id.carddetailbriefdescription_lang_conversion_button);
        liveChannelImageIcon = view.findViewById(R.id.live_channel_thumbnail_icon);
        downloadbtnLayout = view.findViewById(R.id.download_btn_layout);
        //  downloadbtnLayout.setVisibility(View.GONE);
        downloadbtnStatusPercentText = view.findViewById(R.id.download_btn_status_percent_text);

        downloadingGIFAnim = view.findViewById(R.id.downloading_gif_anim);
        downloadingGIFAnim.setBackgroundColor(Color.TRANSPARENT); //for gif without background
        //trailerLayout = view.findViewById(R.id.trailer_download_btns_layout);
        downloadbtnImage = view.findViewById(R.id.carddetailbriefdescription_download_image);

        sharebtnimage = view.findViewById(R.id.carddetailbriefdescription_share_img);
        descriptionexpandtext = view.findViewById(R.id.description_expand);
        relatedcastdescriptionlayout = view.findViewById(R.id.related_description_layout);
        relatedcastdescriptiontext = view.findViewById(R.id.related_description);

        carddetailbriefdescription_duration = view.findViewById(R.id.carddetailbriefdescription_duration);
        year_duration_seperator = view.findViewById(R.id.year_duration_seperator);

        carddetailbriefdescription_language = view.findViewById(R.id.carddetailbriefdescription_language);
        lang_genre_seperator = view.findViewById(R.id.lang_genre_seperator);

        shareText = view.findViewById(R.id.shareTv);
        /*downloadText=view.findViewById(R.id.downloadTv);*/
        detailDownloadText = view.findViewById(R.id.detail_download_text);

        shareLayout = view.findViewById(R.id.shareLayout);
        watchListLayout = view.findViewById(R.id.watchlistLayout);
        watchListImage = view.findViewById(R.id.watchListIv);
        watchListText = view.findViewById(R.id.watchListTv);
        favouriteText = view.findViewById(R.id.favourite_tv);
        detailLikeText = view.findViewById(R.id.liketv);
        likeLayout = view.findViewById(R.id.likeLayout);
        favouriteLayout = view.findViewById(R.id.favourite_layout);
        detailLikeImage = view.findViewById(R.id.likeIv);
        favouriteImage = view.findViewById(R.id.favourite_iv);
        viewsCountLayout = view.findViewById(R.id.viewsLayout);
        viewsCountTv = view.findViewById(R.id.viewsCount);
        //mAdLayout = view.findViewById(R.id.adLayout);

        template = view.findViewById(R.id.ad_banner_template_view);
        newsauthor = view.findViewById(R.id.newsAuthor);
        authorNameTv = view.findViewById(R.id.authorname);
        downloadLayoutL = view.findViewById(R.id.download_layout);
        main_brief_description_layout = view.findViewById(R.id.main_container_brief_des_layout);
        bannerCustomAdContainer = view.findViewById(R.id.ad_image_view);

        tvShowTitleContainer = view.findViewById(R.id.tv_show_data_container);
        tvShowTitleText = view.findViewById(R.id.tv_show_title);
        packExpiryLL = view.findViewById(R.id.pack_expiry_ll);
        packExpityText = view.findViewById(R.id.pack_expiry_text);
        packLimit = view.findViewById(R.id.pack_limit);

        latest_episode_button_layout = view.findViewById(R.id.watch_latest_episode_button_layout);
        episode_number_text = view.findViewById(R.id.episode_number_text);

        authorNameTv = view.findViewById(R.id.authorname);
        downloadLayoutL = view.findViewById(R.id.download_layout);
        main_brief_description_layout = view.findViewById(R.id.main_container_brief_des_layout);
        bannerCustomAdContainer = view.findViewById(R.id.ad_image_view);

        contentRatingLayout = view.findViewById(R.id.age_rating_layout);
        ageNumberTv = view.findViewById(R.id.age_number_tv);
        ageDescriptionTv = view.findViewById(R.id.age_description_tv);


    }

    public static BriefDescriptionComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.carddetailbreifdescription,
                parent, false);
        BriefDescriptionComponent briefDescriptionComponent = new BriefDescriptionComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    public void changeBuyButtonText(String buttonText) {
        if (buyBtn != null && !TextUtils.isEmpty(buttonText)) {
            buyBtn.setText("" + buttonText);
        }
    }

    @Override
    public void bindItemViewHolder(int position) {
        DetailsViewContent.DetailsViewDataItem item = mValues.get(position);
        this.tabName = item.tabName;
        this.mData = item.cardData;
        this.mBgColor = item.mBgColor;
        this.mTVShowData = item.tvshowCardData;
        LoggerD.debugLog("carddata::" + mData);
        relatedcastdescriptionlayout.setVisibility(View.GONE);
        castheadingtext.setVisibility(View.GONE);
        movietrailertext.setVisibility(View.GONE);

        if (mData != null && mData.isTVSeries()) {
            if (!mValues.isEmpty() && mValues.size() > 1 && mValues.get(1).cardData != null) {
                mData = mValues.get(1).cardData;
            }
        }

        updateBgColorsForAllIconsBackgroundsAndTextColors();

        if (mData.isYoutube()) {
            buyBtn.setVisibility(View.GONE);
        }

        programthumnailcontainer.setVisibility(View.GONE);
        if (mData == null || mData.generalInfo == null) return;
        if (mData.isProgram()) {
            showChannelIcon();
        }
        showWatchListAndFavouritesButton(true);
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null))
            showWatchListAndFavouritesButton(false);
        showRelatedCastText();
        showShareUI();
        updateWatchListButton();
        updateFavouritesButton();
        showDownloadButton();
        showTitle();
        if (mData.isMovie()) {
            showSideImage();
        } else {
            thumbnail_movie_layout.setVisibility(View.GONE);
        }
        showWaterMark();
        showTrailer();
        showExpiryButton();
        if(mData != null && mData.generalInfo !=null && mData.generalInfo.contentRights != null && mData.generalInfo.contentRights.size()>0 && mData.generalInfo.contentRights.get(0)!= null) {
            if (mData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                showBuyButton(position);
            }
        }else {
            ll_buy.setVisibility(GONE);
        }


        showReleaseDate();
        //showLanguageAndGenre();
        showCast();
        showAudioLanguagesList();
        showDescription();
        showLatestEpisodeButton();
        //fetchLikeStatusAndShowUI(item.cardData);
        showNewsAuthorLayout();

        watchListLayout.setOnClickListener(mWatchListClickListener);
        likeLayout.setOnClickListener(mLikeClickListener);
        favouriteLayout.setOnClickListener(mFavouritesClickListener);
        moviedescription.post(moviedescriptionLineAlignTask);
        descriptionexpandtext.setOnClickListener(descriptionexpandtextListener);

        carddetailbriefdescription_cast.post(castLineAlignTask);
        castexpandtext.setOnClickListener(castexpandtextListener);
        showMaturityData();

        if (!PrefUtils.getInstance().isAdEnabled() || TextUtils.isEmpty(PrefUtils.getInstance().getAdmobUnitId())) {
            SDKLogger.debug("Ad is disabled or ad Id is null");
            return;
        }

        showGoogleNativeAds();

    }

    private void showMaturityData() {
        try {
            if (mData != null && mData.content != null && mData.content.categoryType != null) {
                String categoryNameStr = mData.content.categoryType;
                if (!TextUtils.isEmpty(categoryNameStr)) {
                    contentRatingLayout.setVisibility(View.VISIBLE);
                    ageNumberTv.setText(categoryNameStr.toUpperCase());
                    String catName = mData.content.categoryName;
                    String output = catName.substring(0, 1).toUpperCase() + catName.substring(1);
                    ageDescriptionTv.setText(output);
                } else {
                    contentRatingLayout.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showNewsAuthorLayout() {
        if (mData.isNewsContent()) {
            if (mData != null
                    && mData.relatedCast != null
                    && mData.relatedCast.values != null
                    && !mData.relatedCast.values.isEmpty()) {
                if (mData.relatedCast.values.get(0) != null
                        && mData.relatedCast.values.get(0).name != null
                        && !TextUtils.isEmpty(mData.relatedCast.values.get(0).name)) {
                    newsauthor.setVisibility(View.VISIBLE);
                    authorNameTv.setText(mData.relatedCast.values.get(0).name);
                } else {
                    newsauthor.setVisibility(GONE);
                    //authorNameTv.setText("Unnamed Author");
                }
            } else {
                newsauthor.setVisibility(GONE);
                //authorNameTv.setText("Unnamed Author");
            }
        } else {
            newsauthor.setVisibility(View.GONE);
        }
    }

    private void showLatestEpisodeButton() {
        if (mData == null || latest_episode_button_layout == null || mData.generalInfo == null) {
            return;
        }
        if (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TV_SERIES)) {
            if (mValues != null && mValues.get(position).isToShowWatchLatestEpisodeButton) {
                // hide the latest episode
                latest_episode_button_layout.setVisibility(GONE);
                if (mValues.get(position).latestEpisodeText != null) {
                    episode_number_text.setText(mValues.get(position).latestEpisodeText);
                }
                latest_episode_button_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onLatestEpisodeButtonClick();
                    }
                });
            } else {
                latest_episode_button_layout.setVisibility(View.GONE);
            }
        } else {
            latest_episode_button_layout.setVisibility(View.GONE);
        }
    }

    public void updateFavouritesButton() {
        if (!Util.checkUserLoginStatus() || mData == null || favouriteLayout == null || mData.generalInfo == null) {
            return;
        }
        showWatchListAndFavouritesButton(true);
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null))
            showWatchListAndFavouritesButton(false);

        /*if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            showFavaouriteButton(false);
        }*/
        String type = mData.generalInfo.type;
        String _id = mData._id;
        if (mTVShowData != null
                && (mTVShowData.isVODYoutubeChannel()
                || mTVShowData.isTVSeason()
                || mTVShowData.isVODChannel()
                || mTVShowData.isVODCategory()
                || mTVShowData.isTVSeries())) {
            type = mTVShowData.generalInfo.type;
            _id = mTVShowData._id;
        }
        if (mData.isProgram()) {
            _id = mData.globalServiceId;
            type = APIConstants.TYPE_LIVE;
        }
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null))
            return;
        FavouriteCheckRequest.Params contentDetailsParams = new FavouriteCheckRequest.Params(_id, type);
        executeFavouriteRequest(contentDetailsParams);
        updateFavouriteButtonBgAndText(mData.currentUserData != null && mData.currentUserData.favorite);
    }

    private void executeFavouriteRequest(FavouriteCheckRequest.Params contentDetailsParams) {

        final FavouriteCheckRequest contentDetails = new FavouriteCheckRequest(contentDetailsParams,
                new APICallback<FavouriteResponse>() {
                    String mAPIErrorMessage = null;

                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (null == mListener) {
                            onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        if (response == null || response.body() == null) {
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            showWatchListAndFavouritesButton(true);
//                                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
//                                    showFavaouriteButton(false);
//                                }
                            if (mData != null
                                    && mData.currentUserData != null) {
                                mData.currentUserData.favorite = response.body().favorite;
                            }
                            updateFavouriteButtonBgAndText(response.body().favorite);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        String errorMessage = null;
                        String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                        if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                            reason = mAPIErrorMessage;
                        }
                    }
                });

        APIService.getInstance().execute(contentDetails);

    }


    public void updateWatchListButton() {
        if (!Util.checkUserLoginStatus() || mData == null || watchListLayout == null || mData.generalInfo == null) {
            return;
        }
        showWatchListAndFavouritesButton(true);
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null))
            showWatchListAndFavouritesButton(false);

        /*if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            showFavaouriteButton(false);
        }*/
        String type = mData.generalInfo.type;
        String _id = mData._id;
        if (mTVShowData != null
                && (mTVShowData.isVODYoutubeChannel()
                || mTVShowData.isTVSeason()
                || mTVShowData.isVODChannel()
                || mTVShowData.isVODCategory()
                || mTVShowData.isTVSeries())) {
            type = mTVShowData.generalInfo.type;
            _id = mTVShowData._id;
        }
        if (mData.isProgram()) {
            _id = mData.globalServiceId;
            type = APIConstants.TYPE_LIVE;
        }
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null))
            return;
        WatchListCheckRequest.Params contentDetailsParams = new WatchListCheckRequest.Params(_id, type);
        executeContentDetailRequest(contentDetailsParams);
        updateWatchListButtonBgAndIcon(mData.currentUserData != null && mData.currentUserData.watchlist);
    }

    private void executeContentDetailRequest(WatchListCheckRequest.Params contentDetailsParams) {

        final WatchListCheckRequest contentDetails = new WatchListCheckRequest(contentDetailsParams,
                new APICallback<FavouriteResponse>() {
                    String mAPIErrorMessage = null;

                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (null == mListener) {
                            onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        if (response == null || response.body() == null) {
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            showWatchListAndFavouritesButton(true);
//                                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
//                                    showFavaouriteButton(false);
//                                }
                            if (mData != null
                                    && mData.currentUserData != null) {
                                mData.currentUserData.watchlist = response.body().watchlist;
                            }
                            updateWatchListButtonBgAndIcon(response.body().watchlist);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        String errorMessage = null;
                        String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                        if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                            reason = mAPIErrorMessage;
                        }
                    }
                });

        APIService.getInstance().execute(contentDetails);

    }

    private boolean isToShowLikeButton() {
        /*if(BuildConfig.FLAVOR.contains("bcn")){
            return (mData.isTVEpisode()&& mTVShowData==null);
        }*/
        return mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null);
    }

    private void showWatchListAndFavouritesButton(boolean b) {
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null)) {
            b = false;
        }
        if (b && ApplicationController.ENABLE_FAVOURITE) {
            watchListLayout.setVisibility(View.VISIBLE);
            favouriteLayout.setVisibility(View.VISIBLE);
            return;
        }
        watchListLayout.setVisibility(View.GONE);
        favouriteLayout.setVisibility(View.GONE);
    }

    private void showChannelIcon() {
        programthumnailcontainer.setVisibility(View.VISIBLE);
        String channelIconUrl = mData.getChannelIconUrl();
        if (!TextUtils.isEmpty(channelIconUrl)) {
            if (APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(channelIconUrl) == 0) {
                liveChannelImageIcon.setImageResource(R.drawable
                        .movie_thumbnail_placeholder);
                liveChannelImageIcon.setVisibility(View.GONE);
            } else {
                liveChannelImageIcon.setVisibility(GONE);
                channelIconUrl = channelIconUrl.replace("epgimages/", "epgimagesV3/");
                PicassoUtil.with(mContext).load(channelIconUrl, liveChannelImageIcon, R.drawable.live_tv_channel_placeholder);
            }
        }
    }

    private void showDescription() {
        description_layout.setVisibility(View.VISIBLE);
        if (mData != null && mData.generalInfo != null && mData.generalInfo.type != null
                && (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)
                || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_NEWS))) {
            if (mData.generalInfo.briefDescription != null) {
                String description = mData.generalInfo.briefDescription;
                // description = description.replace ("\\r\\n", "\n").replace ("\\n", "\n");
                try {
                    description = description.replace("\\n", "\r\n");
                    description = Entities.HTML40.unescape(description);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(description) && description.equalsIgnoreCase("NA")) {
                    moviedescription.setText("");
                    moviedescription.setVisibility(GONE);
                } else {
                    moviedescription.setVisibility(VISIBLE);
                    moviedescription.setText(description);
                }
                movieName.setText(mData.generalInfo.title.toLowerCase());
                mLangConIcon.setVisibility(View.GONE);
            }
        } else if (mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
            if (mData.generalInfo.briefDescription != null) {
                if (!TextUtils.isEmpty(mData.generalInfo.description) && mData.generalInfo.description.equalsIgnoreCase("NA")) {
                    moviedescription.setText("");
                    moviedescription.setVisibility(GONE);
                } else {
                    moviedescription.setVisibility(VISIBLE);
                    moviedescription.setText("" + mData.generalInfo.description);
                }
            }
        } else {
            if (TextUtils.isEmpty(mData.getDescription())) {
                if (TextUtils.isEmpty(mData.getBriefDescription())) {
                    moviedescription.setVisibility(View.GONE);
                } else {
                    moviedescription.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(mData.getBriefDescription()) && mData.getBriefDescription().equalsIgnoreCase("NA")) {
                        moviedescription.setText("");
                    } else {
                        moviedescription.setText(mData.getBriefDescription());
                    }
                }
            } else {
                if (!TextUtils.isEmpty(mData.getDescription()) && mData.getDescription().equalsIgnoreCase("NA")) {
                    moviedescription.setVisibility(View.GONE);
                    moviedescription.setText("");
                } else {
                    moviedescription.setVisibility(View.VISIBLE);
                    moviedescription.setText(mData.getDescription());
                }
            }
        }
    }

    public void fetchLikeStatusAndShowUI(final CardData dataaa) {
        /*if(BuildConfig.FLAVOR.contains("bcn")){*/
        if (isToShowLikeButton() || mData.generalInfo.displayStatistics) {
            likeLayout.setVisibility(View.VISIBLE);
            return;
        }
        //}
        else {
            if (isToShowLikeButton() || !mData.generalInfo.displayStatistics) {
                likeLayout.setVisibility(View.VISIBLE);
                return;
            }
        }
        updateLikeButtonBgAndIcon(isContentLiked);

        mDataForLikes = dataaa;
        contentType = mData.generalInfo.type;
        contentId = mData._id;
        if (mData.isProgram()) {
            contentId = mData.globalServiceId;
            contentType = APIConstants.TYPE_LIVE;
        }

        if (mDataForLikes.stats != null) {
            try {
                if (!TextUtils.isEmpty(mDataForLikes.stats.getLikeCount()) && Integer.parseInt(mDataForLikes.stats.getLikeCount()) > 0) {
                    if ("1".equals(mDataForLikes.stats.getLikeCount())) {
                        detailLikeText.setText(mDataForLikes.stats.getLikeCount() + " Like");
                    } else {
                        detailLikeText.setText(mDataForLikes.stats.getLikeCount() + " Likes");
                    }
                } else {
                    detailLikeText.setText("Like");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                if (!TextUtils.isEmpty(mDataForLikes.stats.getLikeCount())) {
                    if ("1".equals(mDataForLikes.stats.getLikeCount())) {
                        detailLikeText.setText(mDataForLikes.stats.getLikeCount() + " Like");
                    } else {
                        detailLikeText.setText(mDataForLikes.stats.getLikeCount() + " Likes");
                    }
                } else {
                    detailLikeText.setText("Like");
                }
            }
            try {
                if (!TextUtils.isEmpty(mDataForLikes.stats.getViewCount())) {
                    viewsCountLayout.setVisibility(View.VISIBLE);
                    viewsCountTv.setText(mData.stats.getViewCount());
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                if (mData.generalInfo.displayStatistics) {
                    if (!TextUtils.isEmpty(mDataForLikes.stats.getViewCount())) {
                        viewsCountLayout.setVisibility(View.VISIBLE);
                        viewsCountTv.setText(mData.stats.getViewCount());
                    }
                }
            }
        }

        FetchLikeCheckRequest.Params likeCheckRequestParams = new FetchLikeCheckRequest.Params(contentId, contentType);
        FetchLikeCheckRequest mRequestLikeStatus = new FetchLikeCheckRequest(likeCheckRequestParams,
                new APICallback<FavouriteResponse>() {
                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null || response.body() == null) {
                            updateLikeButtonBgAndIcon(false);
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "FetchContentLikeStatus: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            isContentLiked = response.body().like;
                            updateLikeButtonBgAndIcon(isContentLiked);
                        } else {
                            isContentLiked = false;
                            updateLikeButtonBgAndIcon(isContentLiked);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "FetchContentLikeStatus: onFailure: t- " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestLikeStatus);
    }

    private void showCast() {
        CardData mData = this.mData;
        if (mTVShowData != null) {
            mData = mTVShowData;
        }
        if (mData == null || mData.relatedCast == null
                || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_NEWS.equalsIgnoreCase(
                mData.generalInfo.type
        )) {
            castlayout.setVisibility(View.GONE);
            castNewLayout.setVisibility(View.GONE);
            carddetailbriefdescription_cast.setVisibility(View.GONE);
        } else {
            StringBuilder castMembers = new StringBuilder();
            for (CardDataRelatedCastItem relatedCast : mData.relatedCast.values) {
                if (castMembers.length() != 0) {
                    castMembers.append(", ");
                }
                castMembers.append(relatedCast.name);
            }
            if (TextUtils.isEmpty(castMembers) || castMembers.length() == 0) {
                carddetailbriefdescription_cast.setVisibility(View.GONE);
                castheadingtext.setVisibility(View.GONE);
            } else {
                if (mData.relatedCast.values.size() > 0) {
                    castlayout.setVisibility(View.VISIBLE);
                    castNewLayout.setVisibility(VISIBLE);
                    List<CardDataRelatedCastItem> roleNamesList = new ArrayList<>();
                    for (int p = 0; p < mData.relatedCast.values.size(); p++) {
                        if (mData.relatedCast.values.get(p).images.values != null && mData.relatedCast.values.get(p).images.values.size() != 0) {
                            //  pillarItemsList.add(mData.relatedCast.values.get(p));
                        } else {
                            roleNamesList.add(mData.relatedCast.values.get(p));
                        }
                    }
                    RelatedCastList roleNamesRelatedCastList = new RelatedCastList();
                    roleNamesRelatedCastList.values = roleNamesList;
                    roleNamesRelatedCastList.mLayoutType = APIConstants.LAYOUT_TYPE_ROLE_NAME_LAYOUT;
                    CardData finalMData = mData;
                    String actorName = "", directorName = "";
                    if (roleNamesList != null) {
                        for (int i = 0; i < roleNamesList.size(); i++) {
                            if (roleNamesList.get(i).types.get(0).equalsIgnoreCase("Actor")) {
                                if (actorName.isEmpty())
                                    actorName = roleNamesList.get(i).name;
                                else
                                    actorName = actorName + ", " + roleNamesList.get(i).name;
                            }
                            if (roleNamesList.get(i).types.get(0).equalsIgnoreCase("director")) {
                                if (directorName.isEmpty())
                                    directorName = roleNamesList.get(i).name;
                                else
                                    directorName = directorName + ", " + roleNamesList.get(i).name;
                            }
                        }
                        if(actorName!=null && !actorName.trim().equals("")){
                            ll_cast.setVisibility(VISIBLE);
                            castValue.setText(actorName);
                        }else {
                            ll_cast.setVisibility(GONE);
                        }

                        if(directorName!=null && !directorName.trim().equals("")){
                            ll_director.setVisibility(VISIBLE);
                            director_value.setText(directorName);
                        }else {
                            ll_director.setVisibility(GONE);
                        }

                    }
                    //commented as getting new layout on click of cast meta data in portrait player scree
                    /*castNewLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, CastAndCrewActivity.class);
                            intent.putExtra(CastAndCrewActivity.CARD_DATA_CAST_AND_CREW_VALUE, finalMData);
                            mContext.startActivity(intent);
                        }
                    });*/
/*
                carddetailbriefdescription_cast.setVisibility(View.VISIBLE);
                castheadingtext.setVisibility(View.VISIBLE);
                carddetailbriefdescription_cast.setVisibility(View.VISIBLE);
*/
                    carddetailbriefdescription_cast.setText(castMembers);
                }else {

                }
            }

        }
    }
   public void showAudioLanguagesList() {

       CardData mData = this.mData;
       String finalLanguage = "";
       if (mData != null && mData.content != null && mData.content.audioLanguage != null && mData.content.audioLanguage.size() >0) {
           for (int i = 0; i < mData.content.audioLanguage.size(); i++) {
               if (i == 0) {
                   finalLanguage = firstCharacterCaptial(mData.content.audioLanguage.get(i));
               } else {
                   finalLanguage = finalLanguage + ", " + firstCharacterCaptial(mData.content.audioLanguage.get(i));
               }
           }
           ll_audioLanguage.setVisibility(VISIBLE);
           language.setText(finalLanguage);
       }/*else if(mData != null && mData.content != null && mData.content.language!=null && mData.content.language.get(0)!=null && !TextUtils.isEmpty(mData.content.language.get(0))) {
           finalLanguage=firstCharacterCaptial(mData.content.language.get(0));
           language.setText(finalLanguage);
           ll_audioLanguage.setVisibility(VISIBLE);
       }*/
       else {
           ll_audioLanguage.setVisibility(GONE);
       }
   }
    private String firstCharacterCaptial(String languageName){
        StringBuilder sb = new StringBuilder(languageName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    private void showLanguageAndGenre() {
        lang_genre_seperator.setVisibility(View.GONE);
        carddetailbriefdescription_language.setVisibility(View.GONE);
        if (mData.generalInfo != null) {
            if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)) {
                moviegenre.setVisibility(View.GONE);
            }
            if (null != mData.content) {
                StringBuilder genres = new StringBuilder();
                if (mData.content.genre != null && mData.content.genre.size() > 0) {
                    for (CardDataGenre genre : mData.content.genre) {
                        if (genres.length() > 0) {
                            genres.append(" | ");
                        }
                        genres.append(genre.name);
                    }
                    moviegenre.setVisibility(View.GONE);
                    lang_genre_seperator.setVisibility(View.GONE);
                    if (genres.length() > 0) {
                        moviegenre.setVisibility(View.VISIBLE);
                        moviegenre.setText(genres.toString());
                    }
                }

                /*if (mData.content.language != null && mData.content.language.size() > 0) {
                    StringBuilder languageBuilder = new StringBuilder();
                    for (String language : mData.content.language) {
                        if (languageBuilder.length() > 0) {
                            languageBuilder.append("| ");
                        }
                        if (!TextUtils.isEmpty(language)) {
                            String lang = language.substring(0, 1).toUpperCase() + language.substring(1);
                            languageBuilder.append(lang);
                        }
                    }
                    if (genres.length() > 0 && languageBuilder.length() > 0) {
                        lang_genre_seperator.setVisibility(View.VISIBLE);
                    }
                    if (languageBuilder.length() != 0) {
                        carddetailbriefdescription_language.setVisibility(View.VISIBLE);
                        carddetailbriefdescription_language.setText(languageBuilder.toString());
                    }
                }*/
            }
        }

        if (mData.isProgram()) {
            if ((TextUtils.isEmpty(mData.startDate) && TextUtils.isEmpty(mData.endDate))) {
                moviegenre.setVisibility(View.GONE);
            } else {
                lang_genre_seperator.setVisibility(View.GONE);
                carddetailbriefdescription_language.setVisibility(View.GONE);
                moviegenre.setVisibility(View.VISIBLE);
                String startTime = mData.getTimeHHMM_AM(mData.getStartDate());
                String endTime = mData.getTimeHHMM_AM(mData.getEndDate());
                moviegenre.setText(startTime + "-" + endTime);
            }
        } else {
            moviegenre.setVisibility(View.GONE);
        }

    }

    public void showReleaseDate() {
        if (mData == null || mData.generalInfo == null) {
            return;
        }
        year_duration_seperator.setVisibility(View.GONE);
        carddetailbriefdescription_duration.setVisibility(View.GONE);
        if (mData.content != null && mData.content.releaseDate != null) {
            if (mData.content.releaseDate.isEmpty() || APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)) {
                releaseDate.setVisibility(View.GONE);
            } else {
                if (mData.generalInfo != null && APIConstants.TYPE_VOD.equalsIgnoreCase(mData.generalInfo.type)) {
                    releaseDate.setText(mData.getDDMMYYYYUTC());
                } else {
                    if (mData.isNewsContent()) {
                        year_duration_seperator.setVisibility(VISIBLE);
                    }
                    releaseDate.setText(mData.getDDMMYYYY());
                }
            }
        } else {
            releaseDate.setVisibility(View.GONE);
        }

        if (APIConstants.TYPE_VOD.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(mData.generalInfo.type)) {
            if (mData.content != null
                    && mData.content.duration != null) {
                releaseDate.setVisibility(View.VISIBLE);
                releaseDate.setText(mData.getDurationWithFormat());
            } else {
                releaseDate.setVisibility(View.GONE);
            }
        }

        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_TRAILER.equalsIgnoreCase(mData.generalInfo.type)) {
            if (mData.content != null && mData.content.releaseDate != null) {
                if (TextUtils.isEmpty(mData.getDDMMYYYYUTC())) {
                    releaseDate.setVisibility(View.GONE);
                    year_duration_seperator.setVisibility(View.GONE);
                } else {
                    releaseDate.setVisibility(View.VISIBLE);
                    releaseDate.setText(mData.getDDMMYYYYUTC());
                }

            }
            if (mData.content != null
                    && mData.content.duration != null
                    && !mData.content.duration.equalsIgnoreCase("0:0:00")) {
                int contentDurationMnts = Util.calculateDurationInSeconds(mData.content.duration) / 60;//duratin in mints

                if (contentDurationMnts <= 0) {
                    releaseDate.setVisibility(View.GONE);
                    year_duration_seperator.setVisibility(View.GONE);
                } else {
                    if (!TextUtils.isEmpty(mData.getYYYY()))
                        year_duration_seperator.setVisibility(View.VISIBLE);
                    carddetailbriefdescription_duration.setVisibility(View.VISIBLE);
                    carddetailbriefdescription_duration.setText(contentDurationMnts + " mins");
                    contentDuration = contentDurationMnts;
                }
            }
            /*if(BuildConfig.FLAVOR.contains("bcn")) {*/
            /*if (mData.content != null && mData.content.contentRating != null && !mData.content.contentRating.isEmpty()) {
                String contentrating = mData.content.contentRating;
                try {
                    rating.setRating(Float.parseFloat(contentrating));
                } catch (NumberFormatException e) {
                    rating.setRating((float) 4.5);
                }
            } else {
                rating.setRating((float) 4.5);
            }*/
            //}
        }

        if (mData.isProgram() || mData.isLive()) {
            releaseDate.setVisibility(View.VISIBLE);
            //  releaseDate.setText(mData.getTitle()+"  "+"|"+"  "+mData.generalInfo.type);
            if (mData.content != null && mData.content.genre != null && mData.content.genre.size() > 0 && mData.content.genre.get(0) != null) {
//                if (mData.content.subGenres != null && mData.content.subGenres.size() > 0 && mData.content.subGenres.get(0) != null)
//                    releaseDate.setText(mData.getTitle() + "  " + "|" + "  " + mData.content.genre.get(0).name + " - " + mData.content.subGenres.get(0));
//                else
                    releaseDate.setText(mData.getTitle() + "  " + "|" + "  " + mData.content.genre.get(0).name);
            } else
                releaseDate.setText(mData.getTitle());
            if (mData != null && mData.startDate != null) {
                try {
                    long startDate = Util.parseXsDateTime(mData.startDate);
                    EPGUtil.getShortTime(startDate);
                    String startTime = EPGUtil.getShortTime(startDate);

                    long endDate = Util.parseXsDateTime(mData.endDate);
                    EPGUtil.getShortTime(endDate);
                    String endTime = EPGUtil.getShortTime(endDate);
                    content_timings.setText(startTime + " - " + endTime);
                    Log.d("endTime", "EPG" + EPGUtil.getShortTime(endDate));

                } catch (ParserException e) {
                    e.printStackTrace();
                }
            } else {
                content_timings.setVisibility(GONE);
            }
        } else {
            releaseDate.setVisibility(View.VISIBLE);
            //  releaseDate.setText(mData.getTitle() + "  " + "|" + "  " + mData.generalInfo.type);
            if (mData.generalInfo!=null && mData.generalInfo.type!=null && !TextUtils.isEmpty(mData.generalInfo.type)
                    && mData.content != null && mData.content.genre != null && mData.content.genre.size() > 0
                    && mData.content.genre.get(0) != null && mData.getParnterTitle(mContext) != null) {
//                if (mData.content.subGenres != null && mData.content.subGenres.size() > 0 && mData.content.subGenres.get(0) != null)
//                    releaseDate.setText(mData.getTitle() + "  " + "|" + "  " + mData.content.genre.get(0).name + " - " + mData.content.subGenres.get(0));
//                else
                    releaseDate.setText(mData.getParnterTitle(mContext)+" "+"|"+" "+firstCharacterCaptial(mData.generalInfo.type) + "  " + "-" + "  " + mData.content.genre.get(0).name);
            } else
                releaseDate.setText(mData.getTitle());
            if (mData != null && mData.startDate != null && !(mData.isTVEpisode()|| mData.isTVSeason() ||mData.isTVSeries() || mData.isMovie())) {
                try {
                    long startDate = Util.parseXsDateTime(mData.startDate);
                    EPGUtil.getShortTime(startDate);
                    String startTime = EPGUtil.getShortTime(startDate);

                    long endDate = Util.parseXsDateTime(mData.endDate);
                    EPGUtil.getShortTime(endDate);
                    String endTime = EPGUtil.getShortTime(endDate);
                    content_timings.setText(startTime + " - " + endTime);
                    Log.d("endTime", "EPG" + EPGUtil.getShortTime(endDate));

                } catch (ParserException e) {
                    e.printStackTrace();
                }
            } else {
                content_timings.setVisibility(GONE);
            }
        }
        if (mTVShowData != null) {
            if (!mData.isTVSeries()
                    && !mData.isTVSeason()
                    && !mData.isVODYoutubeChannel()
                    && !mData.isVODCategory()
                    && !mData.isVODChannel()
                    && !mData.isTVEpisode()) {
                tvShowTitleContainer.setVisibility(View.VISIBLE);
                tvShowTitleText.setText(mData.getTitle());
            } else {
                tvShowTitleContainer.setVisibility(View.GONE);
            }
        }
    }

    public static final String getImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL,
                APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,
                APIConstants.IMAGE_TYPE_COVERPOSTER};

        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : carouselData.images.values) {
//                LoggerD.debugHooqVstbLog("imageItem:` imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)) {
//                    LoggerD.debugHooqVstbLog("imageType- " + imageType + " imageItem: imageType- " + imageItem.type);
                    return imageItem.link;
                }
            }
        }

        return null;
    }

    public void showSideImage() {

        String imageLink = getImageLink(mData);
        if (TextUtils.isEmpty(imageLink)
                || imageLink.compareTo("Images/NoImage.jpg") == 0) {
            sideImageViewIcon.setImageResource(R.drawable
                    .movie_thumbnail_placeholder);
        } else {
            PicassoUtil.with(mContext).load(imageLink, sideImageViewIcon, R.drawable.tv_guide_thumbnail_default);

        }
    }

    public void showWaterMark() {
        if (mData != null && mData.generalInfo != null) {
            if (mData.generalInfo.showWatermark) {
                PrefUtils.getInstance().setShowWaterMark(true);
            } else {
                PrefUtils.getInstance().setShowWaterMark(false);
            }
        } else
            PrefUtils.getInstance().setShowWaterMark(false);
    }

    public void showTitle() {
        if (!TextUtils.isEmpty(mData.getTitle())) {
            movieName.setText(mData.getTitle());
            //  if (mData.isProgram()) {
            if (mData.content != null && mData.content.channelNumber != null && mData.getChannelName() != null)
                movieName.setText(mData.content.channelNumber + ". " + mData.getChannelName());
            else
                movieName.setText(mData.getChannelName());
            // }
            if (mTVShowData != null) {
                movieName.setText(mTVShowData.getTitle());
                if (!mData.isTVSeries()
                        && !mData.isTVSeason()
                        && !mData.isVODYoutubeChannel()
                        && !mData.isVODCategory()
                        && !mData.isVODChannel()
                        && !mData.isTVEpisode()) {
                    tvShowTitleContainer.setVisibility(View.VISIBLE);
                    tvShowTitleText.setText(mData.getTitle());
                } else {
                    tvShowTitleContainer.setVisibility(View.GONE);
                }
            }
            if (!mData.isProgram()) {
                if (/*mData.getParnterTitle(mContext) != null*/ mData!=null && mData.generalInfo!=null && mData.generalInfo.title!=null)
                    movieName.setText(mData.generalInfo.title);
            }
        }
    }

    private void showTrailer() {
        boolean isEnable = true;
        if (!isEnable) return;
        if (mData != null
                && mData.relatedMultimedia != null
                && mData.relatedMultimedia.values != null
                && !mData.relatedMultimedia.values.isEmpty()) {
            for (CardDataRelatedMultimediaItem relatedMultimediaItem : mData.relatedMultimedia.values) {
                if (relatedMultimediaItem.generalInfo != null
                        && APIConstants.TYPE_TRAILER.equalsIgnoreCase(relatedMultimediaItem.generalInfo.type)) {
                    /*trailerLayout.setVisibility(View.VISIBLE);*/
                    movietrailertext.setVisibility(View.GONE);
                    movietrailertext.setOnClickListener(mTrailerButtonClickListener);
                }
            }
        }
    }

    private void showExpiryButton() {
        packExpiryLL.setVisibility(GONE);
        if (mData != null && mData.generalInfo != null && mData.generalInfo.contentRights != null && mData.generalInfo.contentRights.size() > 0 && mData.generalInfo.contentRights.get(0) != null) {
            if (mData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
               getExpiryText();
            }
        }
    }

    private String getExpiryText() {
      /*  List<String> subscribedCardDataPackages = ApplicationController.getCardDataPackages();
        if (subscribedCardDataPackages == null
                || subscribedCardDataPackages.isEmpty()) {
            return "";
        }*/

        if (mData == null || mData.currentUserData == null
                || mData.currentUserData.purchase == null
                || mData.currentUserData.purchase.isEmpty()) {
            return "";
        }

        if(mData.currentUserData.purchase.get(0) != null) {
            if (mData.currentUserData.purchase.get(0).validity != null) {
              //  return convertDateFormat("YYYY-MM-dd hh:mm:ss", "dd/MM/yyyy", mData.currentUserData.purchase.get(0).validity);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date readDate = null;
                try {
                    readDate = df.parse(mData.currentUserData.purchase.get(0).validity);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(readDate.getTime());
                    Date userDob = readDate;
                    Date today = new Date();
                    df.format(today);
                    long diff =  userDob.getTime() - today.getTime();
                    int numOfYear = (int) ((diff / (1000 * 60 * 60 * 24))/365);
                    int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
                    int hours = (int) (diff / (1000 * 60 * 60));
                    int minutes = (int) (diff / (1000 * 60));
                    int seconds = (int) (diff / (1000));
                    packExpiryLL.setVisibility(GONE);
//                    packExpityText.setText("Rental expires on: "+ cal.get(Calendar.DAY_OF_MONTH) +" "+cal.getDisplayName(Calendar.MONTH, Calendar.SHORT,  Locale.getDefault())+" "+ cal.get(Calendar.YEAR));
                    buyBtn.setText("Rental expires on: "+ cal.get(Calendar.DAY_OF_MONTH) +" "+cal.getDisplayName(Calendar.MONTH, Calendar.SHORT,  Locale.getDefault())+" "+ cal.get(Calendar.YEAR));

                    String days = "";
                    //commented to display the validity in the format of days+hours+minutes+seconds
                    /*if(numOfDays > 0) {
                        if(numOfDays == 1)
                            days = numOfDays + " day";
                        else
                            days = numOfDays + " days";
                    } else {*/
                    /*
                        if(hours > 0){
                            if(hours == 1)
                                days = hours + " hr";
                            else
                                days = hours + " hrs";
                        } else {
                            if(minutes > 0) {
                                if(minutes == 1)
                                    days = minutes + " min";
                                else
                                    days = minutes + " mins";
                            } else {
                                if(seconds > 0) {
                                    if(seconds == 1)
                                        days = seconds + " sec";
                                    else
                                        days = seconds + " secs";
                                }
                            }
                    }*/

                    if(hours>24){
                        int day = (int) TimeUnit.SECONDS.toDays(seconds);
                         hours = (int) (TimeUnit.SECONDS.toHours(seconds) - (day *24));
                        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
                        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
                        if(day==1){
                            days=day+"day "+hours+":"+minute+":"+second;
                        }else{
                            days=day+"days "+hours+":"+minute+":"+second;
                        }
                    }else{
                        days=hours+"hrs";
                    }
//                    packLimit.setText(days + " limit from the time of playback");
                    tv_expiry_time_left.setText(days + " Left");
                    tv_expiry_time_left.setTypeface(mBoldTypeFace);

                    tv_expiry_time_left.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.textsize_12));

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }
        return "";
    }

    public String convertDateFormat(String from, String to, String date) {
        SimpleDateFormat sdfFrom = new SimpleDateFormat(from);
        SimpleDateFormat sdfTo = new SimpleDateFormat(to);
        Date mDate = null;
        String convertedDate = "";

        try {
            mDate = sdfFrom.parse(date);
            convertedDate = sdfTo.format(mDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }


/*
    private void showBuyButton() {
        boolean isEnable = true;
        if (!isEnable) return;
        if (mData != null && mData.generalInfo != null && !mData.generalInfo.isSellable) {
            */
/*if(BuildConfig.FLAVOR.contains("bcn")){*//*

            if (!mData.generalInfo.isSellable) {
                if (mData.generalInfo.contentRights != null) {
                    if (mData.generalInfo.contentRights.contains("avod")) {
                        buyBtn.setVisibility(View.VISIBLE);
                        buyBtn.setText("Watch for Free ");
                    }
                }
            }//}
            return;
        }
        if (mData != null && mData.generalInfo != null && mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)) {
            return;
        }
        if (!Util.checkUserLoginStatus()) {
            return;
        }
        if (mData != null
                && mData.currentUserData != null
                && mData.currentUserData.purchase != null
                && mData.currentUserData.purchase.size() > 0) {

            buyBtn.setVisibility(View.VISIBLE);
            buyBtn.setEnabled(false);
            buyBtn.setText(PrefUtils.getInstance().getSubscribedString());
            buyBtn.setOnClickListener(null);

        } else if (mData != null
                && mData.currentUserData != null
                && mData.currentUserData.purchase != null
                && mData.currentUserData.purchase.size() == 0) {
            buyBtn.setVisibility(View.VISIBLE);
            buyBtn.setEnabled(true);
            */
/*if(BuildConfig.FLAVOR.contains("bcn")){*//*

            if (mData.packages != null && mData.packages.size() > 0) {
                if (mData.packages.get(0).priceDetails.size() > 0 && mData.packages.get(0).priceDetails != null) {
                    if (IS_Subcriped == true) {
                        buyBtn.setVisibility(View.VISIBLE);
                        buyBtn.setEnabled(false);
                        buyBtn.setOnClickListener(null);
                        IS_Subcriped = false;
                        return;
                    } else
                        buyBtn.setText("View At ₹ " + mData.packages.get(0).priceDetails.get(0).price);
                }
            }//}
            else {
                buyBtn.setText(PrefUtils.getInstance().getBuyString());
            }

            buyBtn.setOnClickListener(mBuyButtonClickListener);
        } else {
            buyBtn.setVisibility(View.GONE);
        }
    }
*/

    private void showBuyButton(int position) {
        DetailsViewContent.DetailsViewDataItem item = mValues.get(position);
       /* boolean isEnable = false;
        if (!isEnable) return;*/
        if (mData != null && mData.generalInfo != null && !mData.generalInfo.isSellable) {
            /*if(BuildConfig.FLAVOR.contains("bcn")){*/
            if (!mData.generalInfo.isSellable) {
                if (mData.generalInfo.contentRights != null) {
                    if (mData.generalInfo.contentRights.contains("avod")) {
                        buyBtn.setVisibility(View.VISIBLE);
                        buyBtn.setText("Watch for Free ");

                        ll_buy.setVisibility(VISIBLE);
                        ll_expiry.setVisibility(GONE);
                    }
                }
            }//}
            return;
        }
        if (item.isSubscribed.equalsIgnoreCase(APIConstants.IS_SELLABLE_FALSE)) {
            return;
        }
        if (item.isSubscribed.equalsIgnoreCase(APIConstants.IS_YOU_TUBE_CONTENT)) {
            return;
        }
        if (item.isSubscribed.equalsIgnoreCase(APIConstants.USER_NOT_LOGGED_IN)) {
            return;
        }
        if (item.isSubscribed.equalsIgnoreCase(APIConstants.USER_ALREADY_SUBSCRIBED)) {
//            buyBtn.setVisibility(GONE);
            buyBtn.setEnabled(false);
//            buyBtn.setText(PrefUtils.getInstance().getSubscribedString());
            buyBtn.setOnClickListener(null);

            ll_buy.setVisibility(VISIBLE);
            ll_buy.setEnabled(false);
            ll_expiry.setVisibility(VISIBLE);
            iv_expiry_icon.setVisibility(VISIBLE);
//            tv_expiry_time_left.setText("");


        } else if (item.isSubscribed.equalsIgnoreCase(APIConstants.USER_NOT_SUBSCRIBED)) {
            buyBtn.setVisibility(View.VISIBLE);
            buyBtn.setEnabled(true);
//            buyBtn.setText(PrefUtils.getInstance().getBuyString());
            /*if(BuildConfig.FLAVOR.contains("bcn")){*/
            if (mData.packages != null && mData.packages.size() > 0) {
                if (mData.packages.get(0).priceDetails.size() > 0 && mData.packages.get(0).priceDetails != null) {
                    if (IS_Subcriped) {
                        buyBtn.setVisibility(View.VISIBLE);
                        buyBtn.setEnabled(false);
                        buyBtn.setOnClickListener(null);

                        ll_buy.setVisibility(VISIBLE);
                        ll_buy.setEnabled(false);
                        ll_buy.setOnClickListener(null);
                        ll_expiry.setVisibility(VISIBLE);
                        iv_expiry_icon.setVisibility(VISIBLE);
//                        tv_expiry_time_left.setText("");

                        IS_Subcriped = false;
                        return;
                    } else {
//                        buyBtn.setText("Rent At ₹ " + mData.packages.get(0).priceDetails.get(0).price);
                        if(mData.packages!=null && mData.packages.size()>0 && mData.packages.get(0).priceDetails!=null && mData.packages.get(0).priceDetails.size()> 0){
                            if(mData.packages.get(0).priceDetails.get(0).price<1){
                              String  priceInPisa;
                              float price=mData.packages.get(0).priceDetails.get(0).price*100;
                                int inPiase=(int)price;
                                priceInPisa=String.valueOf(inPiase);
                                tv_expiry_time_left.setText(priceInPisa+" "+"P");
                            }else {
                                String price = "";
                                try {
                                    price = new DecimalFormat("#0.##").format(mData.packages.get(0).priceDetails.get(0).price)+mContext.getResources().getString(R.string.money_symbol);
                                } catch (Exception e){
                                    e.printStackTrace();
                                    price = mData.packages.get(0).priceDetails.get(0).price + mContext.getResources().getString(R.string.money_symbol);
                                }
                                tv_expiry_time_left.setText(price);
                            }
                            tv_expiry_time_left.setTypeface(mBoldTypeFace);

                            tv_expiry_time_left.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.textsize_20));

                        }

                    }
            }
                ll_buy.setVisibility(VISIBLE);
                iv_expiry_icon.setVisibility(GONE);
                ll_buy.setEnabled(true);
                buyBtn.setText(mContext.getResources().getString(R.string.now_available_to_rent));
                ll_buy.setOnClickListener(mBuyButtonClickListener);
            }//}
            else {
                ll_buy.setVisibility(GONE);
//                buyBtn.setText(PrefUtils.getInstance().getBuyString());
            }


        } else {
            buyBtn.setVisibility(View.GONE);
            ll_buy.setVisibility(View.GONE);
        }

    }

    private void showDownloadButton() {
        DownloadUtil.getDownloadDataFromDownloads(mData, new DownloadUtil.OnDataRetrieverListener() {
            @Override
            public void onDataLoaded(CardDownloadData data) {
                LoggerD.debugLog("update data- " + mData + " data- " + data);
                if (data != null) {
                    if (data.mCompleted) {
                        if (DownloadManagerMaintainer.getInstance().getDownloadStatus(data._id) ==
                                DownloadManagerMaintainer.STATE_FAILED) {
                            showDownloadFailedUI();
                            return;
                        } else if (DownloadManagerMaintainer.getInstance().getDownloadStatus(data._id) ==
                                DownloadManagerMaintainer.STATE_DOWNLOADING) {
                            showDownloadingUI(99);
                            return;
                        } else if (DownloadManagerMaintainer.getInstance().getDownloadStatus(data._id) ==
                                DownloadManagerMaintainer.STATE_COMPLETED) {
                            showDownloadCompleted(data);
                            return;
                        }
                    } else {
                        showDownloadingUI(data.mPercentage);
                        FetchDownloadProgress.getInstance(mContext).removeProgressListener(BriefDescriptionComponent.this);
                        FetchDownloadProgress.getInstance(mContext).addProgressListener(BriefDescriptionComponent.this);
                        FetchDownloadProgress.getInstance(mContext).startPolling();
                    }
                    return;
                }
                if (mData != null
                        && (mData.isTVSeries()
                        || mData.isTVSeason()
                        || mData.isVODCategory()
                        || mData.isVODChannel()
                        || mData.isVODYoutubeChannel())) {
                    mData.generalInfo.isDownloadable = false;
                }
//                mData.generalInfo.isDownloadable = true;
                checkAndEnableDownload(mData != null
                        && mData.generalInfo != null
                        && mData.generalInfo.isDownloadable
                        && ApplicationController.ENABLE_DOWNLOADS);

            }
        });
    }

    private void showDownloadingUI(int progress) {
        if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
            downloadbtnImage.setImageResource(R.drawable.download_default_light_theme);
            detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_light_theme_color));
        } else {
            downloadbtnImage.setImageResource(R.drawable.download_default);
            detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
        }
        detailDownloadText.setText("Downloading");
        detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.download_percent_text_colour));
        downloadbtnImage.setVisibility(View.VISIBLE);
    }

    private void showDownloadFailedUI() {
        downloadLayoutL.setVisibility(VISIBLE);
        downloadbtnImage.setImageResource(R.drawable.description_download_broken);
        detailDownloadText.setText("Download failed");
        detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.download_percent_text_colour));
        downloadbtnImage.setVisibility(View.VISIBLE);
    }


    private void showDownloadCompleted(CardDownloadData data) {
        downloadLayoutL.setVisibility(View.VISIBLE);
        if (DownloadManagerMaintainer.getInstance().getDownloadStatus(data._id) ==
                DownloadManagerMaintainer.STATE_COMPLETED) {
            detailDownloadText.setText("Downloaded");
            if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
                downloadbtnImage.setImageResource(R.drawable.download_icon_completed_light_theme);
                detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_light_theme_color));
            } else {
                downloadbtnImage.setImageResource(R.drawable.download_completed);
                detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            }
            downloadbtnImage.setVisibility(View.VISIBLE);
            downloadLayoutL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDownloadDetailsPopUP(data);
                }
            });
        }
    }

    private void showDownloadDetailsPopUP(CardDownloadData pos) {
        LinearLayout ll_play_now, ll_DownloadPage, ll_Delect_Download;

        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.custom_dialog_download_popup);

        ll_play_now = dialog.findViewById(R.id.ll_play_now);
        ll_DownloadPage = dialog.findViewById(R.id.ll_DownloadPage);
        ll_Delect_Download = dialog.findViewById(R.id.ll_delect_download);

        ll_play_now.setOnClickListener(v -> {
            dialog.dismiss();
            showDetailsFragment(pos);
        });
        ll_DownloadPage.setOnClickListener(v -> {
            dialog.dismiss();
        });
        ll_Delect_Download.setOnClickListener(v -> {
            dialog.dismiss();
            deleteDownloads(pos);
        });


        if (DownloadManagerMaintainer.getInstance().getDownloadStatus(pos._id)
                == DownloadManagerMaintainer.STATE_COMPLETED) {
            ll_play_now.setVisibility(VISIBLE);
        } else {
            ll_play_now.setVisibility(GONE);
        }

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        dialog.show();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void showDetailsFragment(CardDownloadData mDownloadedMovies) {
        if (mDownloadedMovies == null) {
            return;
        }
        CardData cardData = generateCardData(mDownloadedMovies);
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
        cardData.content = cardDownloadData.content;
        cardData.images = images;

        return cardData;
    }

    private void deleteDownloads(final CardDownloadData cardDownloadData) {
        if (cardDownloadData == null) {
            return;
        }
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
                .setMessage(mContext.getString(R.string.vf_txt_delete_download) + " " + itemType + " ?")
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
                            DownloadUtil.removeFile(Keyspath, mContext);
                            String downloadFilelocation = mContext.getExternalFilesDir(null) + File.separator + DownloadUtil.downloadVideosStoragePath + cardDownloadData._id;
                            DownloadUtil.deleteRecursive(new File(downloadFilelocation));
                            manager.remove(cardDownloadData.mDownloadId);
                            manager.remove(cardDownloadData.mVideoDownloadId);
                            manager.remove(cardDownloadData.mAudioDownloadId);
                        } catch (Exception e) {
                            e.printStackTrace();
                            LoggerD.debugDownload("Excep in DownloadScreen" + e.toString());
                        }
                        if (DownloadManagerMaintainer.getInstance().getDownloadStatus(cardDownloadData._id) ==
                                DownloadManagerMaintainer.STATE_COMPLETED) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.vf_txt_download_deleted));
                            removeAndSaveDownloadedData(cardDownloadData._id);
                            showDownloadButton();
                        }
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
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
            if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(downloadData.partnerName)
                    || APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(downloadData.partnerName)) {
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

    private void showShareUI() {
        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareLayout.setEnabled(false);
                constructShareUrl(mData);
                shareLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (shareLayout != null) {
                            shareLayout.setEnabled(true);
                        }
                    }
                }, 2000);
                ComScoreAnalytics.getInstance().setEventShare(mData);
                FirebaseAnalytics.getInstance().onShareClick(false, mData);
                return;
            }
        });
    }

    private void constructShareUrl(final CardData mData) {
        String dynamicLink = null;
        String pwa_url = "";
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getAppSharePwaUrl())) {
            pwa_url = PrefUtils.getInstance().getAppSharePwaUrl() + mData.generalInfo.type + "/detail/" + mData._id + "/" + mData.generalInfo.title;
        } else {
            pwa_url = PWA_URL + mData.generalInfo.type + "/detail/" + mData._id + "/" + mData.generalInfo.title;
        }

        Uri deepLink;
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getAppDeepLinkUrl())) {
            deepLink = Uri.parse(PrefUtils.getInstance().getAppDeepLinkUrl());
        } else {
            deepLink = Uri.parse(APIConstants.SHARE_ARTIST_DEEP_LINK_URL);
        }

        final Uri fallBackUri = Uri.parse(pwa_url);

        String packageName = mContext.getPackageName();

// Build the link with all required parameters
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(mContext.getString(R.string.appcode_for_deeplink))
                .path("/")
                .appendQueryParameter("link", deepLink + mData.generalInfo.type + "/detail/" + mData._id + "/" +
                        mData.generalInfo.title)
                .appendQueryParameter("apn", packageName);

        dynamicLink = builder.build().toString();
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(dynamicLink))
                .setAndroidParameters(new
                        DynamicLink.AndroidParameters.Builder(packageName)
                        .setFallbackUrl(fallBackUri)
                        .build())
                .buildShortDynamicLink()
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
// Short link created
//Util.showFeedback(shareLayout);
                            String path = null;
                            if (mData.images != null
                                    && mData.images.values != null
                                    && !mData.images.values.isEmpty()
                                    && mData.images.values.size() > 0) {
                                for (CardDataImagesItem imageItem : mData.images.values) {
                                    if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null
                                            && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI) && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
                                        if (imageItem.link != null
                                                || imageItem.link.compareTo("Images/NoImage.jpg") != 0) {
                                            path = imageItem.link;
                                        }
                                        break;
                                    }
                                }
                            }
                            if (TextUtils.isEmpty(path)) {
                                path = Util.takeScreenShot((Activity) mContext);
                            }
                            String contentName = null;
                            if (mData != null
                                    && mData.generalInfo != null
                                    && mData.generalInfo.title != null) {
                                contentName = mData.generalInfo.title;
                            }

                            String msg;
                            if (mData.isNewsContent()) {
                                msg = mContext.getString(R.string.share_news_message);
                            } else {
                                msg = mContext.getString(R.string.share_message);
                            }
                            if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefShareMessage())) {
                                msg = PrefUtils.getInstance().getPrefShareMessage();
                            }
                            if (msg.contains(APIConstants.HASH_CONTENT_NAME)) {
                                msg = msg.replace(APIConstants.HASH_CONTENT_NAME, TextUtils.isEmpty(contentName) ? "" : contentName);
                            }

                            if (mData.isNewsContent()) {
                                if (msg.contains(APIConstants.HASH_DESCRIPTION)) {
                                    msg = msg.replace(APIConstants.HASH_DESCRIPTION,
                                            TextUtils.isEmpty(mData.getBriefDescription()) ? "" : mData.getBriefDescription());
                                }
                            }

                            String url = task.getResult().getShortLink().toString();
/*if (mData.generalInfo.type != null && mData.generalInfo._id != null && mData.generalInfo.title != null) {
url = APIConstants.SHARE_ARTIST_DEEP_LINK_URL + mData.generalInfo.type + "/detail/" + mData.generalInfo._id + "/" + mData.generalInfo.title + "?sourceType=android";
} else {
url = APIConstants.getShareUrl(mContext);
}*/
// String shareUrl=PrefUtils.getInstance().getShortLink();
                            if (!TextUtils.isEmpty(url)) {
                                msg = msg + "\n" + Uri.parse(url.trim());
                            }
                            Util.shareData(mContext, 1, path, msg);// send message.

// PrefUtils.getInstance().setShortLink(task.getResult().getShortLink().toString());
                        } else {
                            LoggerD.debugOTP(String.valueOf(task.getException()));
                        }
                    }
                });
    }

    private View.OnClickListener mLikeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            likeLayout.setEnabled(false);
            likeLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (likeLayout != null)
                        likeLayout.setEnabled(true);
                }
            }, 1000);

            final boolean currentLikeStatus = !isContentLiked;
            ContentLikedRequest.Params likedContentParams = new ContentLikedRequest.Params(contentId, contentType, currentLikeStatus);

            ContentLikedRequest mRequestLikedContent = new ContentLikedRequest(likedContentParams,
                    new APICallback<FavouriteResponse>() {
                        @Override
                        public void onResponse(APIResponse<FavouriteResponse> response) {
                            if (response == null
                                    || response.body() == null) {
                                return;
                            }
                            if (response.body().code == 402) {
                                PrefUtils.getInstance().setPrefLoginStatus("");
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.login_required));
                                return;
                            }
                            //Log.d(TAG, "ContentLikedRequest: onResponse: message - " + response.body().message);
                            if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                                //Set Like Status
                                isContentLiked = currentLikeStatus;
                                int likeCount = 0;
                                try {
                                    if (mDataForLikes.stats != null && mDataForLikes.stats.getLikeCount() != null &&
                                            !TextUtils.isEmpty(mDataForLikes.stats.getLikeCount())) {
                                        likeCount = Integer.parseInt(mDataForLikes.stats.getLikeCount());
                                        likeCount = currentLikeStatus ? likeCount + 1 : likeCount - 1;
                                        mDataForLikes.stats.setLikeCount(likeCount + "");
                                    }
                                    if (likeCount == 1) {
                                        detailLikeText.setText(likeCount + " Like");
                                    } else if (likeCount > 1) {
                                        detailLikeText.setText(likeCount + " Likes");
                                    } else {
                                        detailLikeText.setText("Like");
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    if (mDataForLikes.stats != null) {
                                        if (!TextUtils.isEmpty(mDataForLikes.stats.getLikeCount())) {
                                            if ("1".equals(mDataForLikes.stats.getLikeCount())) {
                                                detailLikeText.setText(mDataForLikes.stats.getLikeCount() + " Like");
                                            } else {
                                                detailLikeText.setText(mDataForLikes.stats.getLikeCount() + " Likes");
                                            }
                                        } else {
                                            detailLikeText.setText("Like");
                                        }
                                    }
                                }

                                likeLayout.setVisibility(View.VISIBLE);
                                /*if (currentLikeStatus) {
                                    ComScoreAnalytics.getInstance().setEventFavourite(mData);
                                }*/
                                updateLikeButtonBgAndIcon(currentLikeStatus);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorCode) {
                            //Log.d(TAG, "ContentLikedRequest: onFailure: t- " + t);
                            if (errorCode == APIRequest.ERR_NO_NETWORK) {
                                AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                                return;
                            }
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                        }
                    });
            APIService.getInstance().execute(mRequestLikedContent);
        }
    };

    private void updateLikeButtonBgAndIcon(boolean isSelected) {
        if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
            if (isSelected) {
                detailLikeImage.setImageResource(R.drawable.like_selected_light_theme);
                detailLikeText.setTextColor(mContext.getResources().getColor(R.color.selected_color));
            } else {
                detailLikeImage.setImageResource(R.drawable.like_unselected_light_theme);
                detailLikeText.setTextColor(mContext.getResources().getColor(R.color.unselected_light_theme_color));
            }
        } else {
            if (isSelected) {
                detailLikeImage.setImageResource(R.drawable.selected_like_icon);
                detailLikeText.setTextColor(mContext.getResources().getColor(R.color.selected_color));
            } else {
                detailLikeImage.setImageResource(R.drawable.unselected_like_icon);
                detailLikeText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            }
        }
    }

    private void updateWatchListButtonBgAndIcon(boolean isSelected) {
        if (isSelected) {
            watchListText.setText(mContext.getResources().getString(R.string.selected_watch_list_text));
        } else {
            watchListText.setText(mContext.getResources().getString(R.string.unselected_watch_list_text));
        }
        if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
            if (isSelected) {
                watchListImage.setImageResource(R.drawable.remove_from_watch_list_light_theme);
                watchListText.setTextColor(mContext.getResources().getColor(R.color.selected_color));
            } else {
                watchListImage.setImageResource(R.drawable.add_to_watch_list_light_theme);
                watchListText.setTextColor(mContext.getResources().getColor(R.color.unselected_light_theme_color));
            }
        } else {
            if (isSelected) {
                watchListImage.setImageResource(R.drawable.selected_watchlist);
                watchListText.setTextColor(mContext.getResources().getColor(R.color.selected_color));
            } else {
                watchListImage.setImageResource(R.drawable.unselected_watchlist);
                watchListText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            }
        }
    }

    private void updateFavouriteButtonBgAndText(boolean isSelected) {
        if (isSelected) {
            favouriteText.setText(mContext.getResources().getString(R.string.selected_favourite_text));
        } else {
            favouriteText.setText(mContext.getResources().getString(R.string.unselected_favourite_text));
        }
        if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
            if (isSelected) {
                favouriteImage.setImageResource(R.drawable.like_selected_light_theme);
                favouriteText.setTextColor(mContext.getResources().getColor(R.color.selected_color));
            } else {
                favouriteImage.setImageResource(R.drawable.like_unselected_light_theme);
                favouriteText.setTextColor(mContext.getResources().getColor(R.color.unselected_light_theme_color));
            }
        } else {
            if (isSelected) {
                favouriteImage.setImageResource(R.drawable.selected_like_icon);
                favouriteText.setTextColor(mContext.getResources().getColor(R.color.selected_color));
            } else {
                favouriteImage.setImageResource(R.drawable.unselected_like_icon);
                favouriteText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            }
        }
    }


    private void updateBgColorsForAllIconsBackgroundsAndTextColors() {
        int appBkgColor = mContext.getResources().getColor(R.color.app_bkg);
        if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
            int lightThemeColor = Color.parseColor(mBgColor);
            main_brief_description_layout.setBackgroundColor(lightThemeColor);
            likeLayout.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons_light));
            shareLayout.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons_light));
            watchListLayout.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons_light));
            favouriteLayout.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons_light));
            downloadLayoutL.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons_light));
            movieName.setTextColor(mContext.getResources().getColor(R.color.bd_title_light_color));
            carddetailbriefdescription_duration.setTextColor(mContext.getResources().getColor(R.color.bd_duration_light_theme_color));
            carddetailbriefdescription_language.setTextColor(mContext.getResources().getColor(R.color.bd_language_light_theme_color));
            lang_genre_seperator.setTextColor(mContext.getResources().getColor(R.color.bd_language_light_theme_color));
            moviegenre.setTextColor(mContext.getResources().getColor(R.color.white));
            year_duration_seperator.setBackgroundColor(mContext.getResources().getColor(R.color.bd_language_light_theme_color));
            releaseDate.setTextColor(mContext.getResources().getColor(R.color.white));
            sharebtnimage.setImageResource(R.drawable.share_unselected_light_theme);
            moviedescription.setTextColor(mContext.getResources().getColor(R.color.bd_description_light_theme_color));
            detailLikeText.setTextColor(mContext.getResources().getColor(R.color.bd_title_light_color));
            watchListText.setTextColor(mContext.getResources().getColor(R.color.bd_title_light_color));
            favouriteText.setTextColor(mContext.getResources().getColor(R.color.bd_title_light_color));
            shareText.setTextColor(mContext.getResources().getColor(R.color.bd_title_light_color));
            detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.bd_title_light_color));
            newsauthor.setBackgroundColor(mContext.getResources().getColor(R.color.news_author_light_theme_background_color));
            authorNameTv.setTextColor(mContext.getResources().getColor(R.color.news_author_light_theme_text_color));
            downloadbtnImage.setImageResource(R.drawable.download_default_light_theme);
            if (!Util.checkUserLoginStatus()) {
                updateFavouriteButtonBgAndText(false);
                updateWatchListButtonBgAndIcon(false);
            }
            setSizeByDP(sharebtnimage, 36);
            setSizeByDP(downloadbtnImage, 36);
            setSizeByDP(favouriteImage, 36);
            setSizeByDP(watchListImage, 36);
        } else {
            int detailsLayoutBg = mContext.getResources().getColor(R.color.details_icons_background_color);
            main_brief_description_layout.setBackgroundColor(appBkgColor);
            likeLayout.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons));
            shareLayout.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons));
            watchListLayout.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons));
            favouriteLayout.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons));
            downloadLayoutL.setBackground(mContext.getResources().getDrawable(R.drawable.background_bd_details_icons));
            movieName.setTextColor(mContext.getResources().getColor(R.color.bd_title_dark_theme_color));
            carddetailbriefdescription_duration.setTextColor(mContext.getResources().getColor(R.color.bd_duration_dark_theme_color));
            carddetailbriefdescription_language.setTextColor(mContext.getResources().getColor(R.color.bd_language_dark_theme_color));
            lang_genre_seperator.setTextColor(mContext.getResources().getColor(R.color.bd_language_dark_theme_color));
            moviegenre.setTextColor(mContext.getResources().getColor(R.color.white));
            year_duration_seperator.setBackgroundColor(mContext.getResources().getColor(R.color.bd_release_date_dark_theme_color));
            releaseDate.setTextColor(mContext.getResources().getColor(R.color.white));
            sharebtnimage.setImageResource(R.drawable.description_share_icon);
            moviedescription.setTextColor(mContext.getResources().getColor(R.color.white_65));
            detailLikeText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            watchListText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            favouriteText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            shareText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            newsauthor.setBackgroundColor(mContext.getResources().getColor(R.color.news_author_background_color));
            authorNameTv.setTextColor(mContext.getResources().getColor(R.color.news_author_text_color));
            downloadbtnImage.setImageResource(R.drawable.download_default);
            if (!Util.checkUserLoginStatus()) {
                updateFavouriteButtonBgAndText(false);
                updateWatchListButtonBgAndIcon(false);
            }
            setSizeByDP(sharebtnimage, 18);
            setSizeByDP(downloadbtnImage, 18);
            setSizeByDP(favouriteImage, 18);
            setSizeByDP(watchListImage, 18);
        }
    }

    private void setSizeByDP(ImageView view, int dimensionInPixel) {
        int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInPixel, mContext.getResources().getDisplayMetrics());
        view.getLayoutParams().height = dimensionInDp;
        view.getLayoutParams().width = dimensionInDp;
        view.requestLayout();
    }


    private void showRelatedCastText() {
        if (mData.generalInfo != null
                && APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(mData.generalInfo.type)) {
            String musicdesctiption = prepareMusicDescription(mData);
            if (!TextUtils.isEmpty(musicdesctiption)) {
                relatedcastdescriptionlayout.setVisibility(View.VISIBLE);
                relatedcastdescriptiontext.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(mData.globalServiceName))
                    musicdesctiption = musicdesctiption + "\nAlbum : " + mData.globalServiceName;
                relatedcastdescriptiontext.setText(musicdesctiption);
                relatedcastdescriptiontext.setTextColor(mContext.getResources().getColor(R.color.white_100));
            } else if (!TextUtils.isEmpty(mData.globalServiceName)) {
                relatedcastdescriptiontext.setText("Album : " + mData.globalServiceName);
            }else {
                relatedcastdescriptionlayout.setVisibility(View.GONE);
            }
        } else {
            relatedcastdescriptionlayout.setVisibility(View.GONE);
        }
    }

    private void checkAndEnableDownload(boolean enable) {
        if (mData != null
                && mData.generalInfo != null
                && mData.generalInfo.contentRights != null
                && mData.generalInfo.contentRights.size() > 0) {

            if (mData.generalInfo.contentRights.contains("tvod")) {
                enable = false;
            }
        }
        //   enable = false;
        LoggerD.debugLog(mData + " is downloadable- " + enable);
        if (enable) {
            downloadLayoutL.setVisibility(View.VISIBLE);
            if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
                downloadbtnImage.setImageResource(R.drawable.download_default_light_theme);
                detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_light_theme_color));
            } else {
                downloadbtnImage.setImageResource(R.drawable.download_default);
                detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            }
            downloadLayoutL.setEnabled(true);
            downloadLayoutL.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    downloadbtnImage.setEnabled(false);
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
        } else {
            downloadLayoutL.setVisibility(GONE);
            downloadbtnImage.setEnabled(false);
            if (mBgColor != null && !TextUtils.isEmpty(mBgColor)) {
                downloadbtnImage.setImageResource(R.drawable.download_na_icon);
                detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_light_theme_color));
            } else {
                downloadbtnImage.setImageResource(R.drawable.download_na_icon);
                detailDownloadText.setTextColor(mContext.getResources().getColor(R.color.unselected_color));
            }
        }
    }


    public void startDownload() {
        if (mListener != null) {
            AlertDialogUtil.showProgressAlertDialog(mContext, "",
                    mContext.getString(R.string.vf_download_init_download_message),
                    true, false, null);
            mHandler.removeCallbacks(mDismissProgressTask);
            mHandler.postDelayed(mDismissProgressTask, 25 * 1000);
            isToShowDownloadButton = true;
            mListener.onDownloadContent(new FragmentCardDetailsDescription.DownloadStatusListener() {
                @Override
                public void onSuccess() {
                    if (((Activity) mContext).isFinishing()) {
                        return;
                    }
                    if (downloadbtnImage == null) {
                        return;
                    }
                    mHandler.removeCallbacks(mDismissProgressTask);
                    downloadbtnImage.setEnabled(false);
                    AlertDialogUtil.dismissProgressAlertDialog();
                    downloadbtnImage.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadbtnImage.setEnabled(true);
                            showDownloadingUI(0);
                            FetchDownloadProgress.getInstance(mContext).removeProgressListener(BriefDescriptionComponent.this);
                            FetchDownloadProgress.getInstance(mContext).addProgressListener(BriefDescriptionComponent.this);
                            FetchDownloadProgress.getInstance(mContext).startPolling();
                            DownloadUtil.getDownloadDataFromDownloads(mData, new DownloadUtil.OnDataRetrieverListener() {
                                @Override
                                public void onDataLoaded(CardDownloadData data) {
                                    CleverTap.eventDownload(data, CleverTap.PROPERTY_DOWNLOAD_STARTED);
                                }
                            });
                            EventBus.getDefault().post(new ContentDownloadEvent(mData));
                        }
                    });

                }

                @Override
                public void onDownloadStarted() {
                    checkAndEnableDownload(mData != null
                            && mData.generalInfo != null
                            && mData.generalInfo.isDownloadable
                            && ApplicationController.ENABLE_DOWNLOADS);
                    AlertDialogUtil.showNeutralAlertDialog(mContext,
                            mContext.getResources().getString(R.string.download_started_info) + " \"" +
                                    mData.generalInfo.title + ".\" " +
                                    mContext.getResources().getString
                                            (R.string.notification_download_complete_info),
                            "Download Started", mContext.getString(R.string.dialog_ok), buttonText -> {
                            });
                    mHandler.removeCallbacks(mDismissProgressTask);
                    AlertDialogUtil.dismissProgressAlertDialog();
                }

                @Override
                public void onDownloadInitialized() {
                    checkAndEnableDownload(mData != null
                            && mData.generalInfo != null
                            && mData.generalInfo.isDownloadable
                            && ApplicationController.ENABLE_DOWNLOADS);
                    showLoadingAndDisableDownload();
                    showDownloadingUI(0);
                }

                @Override
                public void onFailure(String message) {
                    mHandler.removeCallbacks(mDismissProgressTask);
                    AlertDialogUtil.dismissProgressAlertDialog();
                    if (!mContext.getString(R.string.download_video_already_downloaded).equalsIgnoreCase(message)) {
                        checkAndEnableDownload(mData != null
                                && mData.generalInfo != null
                                && mData.generalInfo.isDownloadable
                                && ApplicationController.ENABLE_DOWNLOADS);
                    }
                    if (TextUtils.isEmpty(message))
                        message = mContext.getString(R.string.vf_download_error_while_download);
                    AlertDialogUtil.showToastNotification(message);
                }

                @Override
                public void onDownloadCancelled() {
                    mHandler.removeCallbacks(mDismissProgressTask);
                    AlertDialogUtil.dismissProgressAlertDialog();
                    checkAndEnableDownload(mData != null
                            && mData.generalInfo != null
                            && mData.generalInfo.isDownloadable
                            && ApplicationController.ENABLE_DOWNLOADS);
                }

                @Override
                public boolean isToShowDownloadButton() {
                    return isToShowDownloadButton;
                }

            });
        }
    }

    private void showLoadingAndDisableDownload() {
        if (((Activity) mContext).isFinishing()) {
            return;
        }
        downloadbtnImage.post(new Runnable() {
            @Override
            public void run() {
                downloadbtnImage.setVisibility(View.VISIBLE);
            }
        });
    }

    private String prepareMusicDescription(CardData mData) {
        String description = null;
        Map<String, String> relatedCastTypes = new HashMap<>();
        if (mData.relatedCast != null
                && mData.relatedCast.values != null) {
            for (CardDataRelatedCastItem relatedCastItem : mData.relatedCast.values) {
                if (relatedCastItem.name != null
                        && relatedCastItem.types != null) {
                    for (String type : relatedCastItem.types) {
                        type = type.substring(0, 1).toUpperCase() + type.substring(1);
                        String castName = relatedCastTypes.get(type);
                        if (!TextUtils.isEmpty(castName)) {
                            castName = castName + ", " + relatedCastItem.name;
                        } else {
                            castName = relatedCastItem.name;
                        }
                        relatedCastTypes.put(type, castName);
                    }
                }
            }
        }
        if (!relatedCastTypes.isEmpty()) {
            for (String type : relatedCastTypes.keySet()) {
                if (TextUtils.isEmpty(description)) {
                    description = type + " : " + relatedCastTypes.get(type);
                } else {
                    description = description + "\n" + type + " : " + relatedCastTypes.get(type);
                }

            }
        }
        return description;
    }

    @Override
    public void DownloadProgress(CardData cardData, final CardDownloadData downloadDat) {
        if (mHandler == null) return;
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
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
                if (downloadbtnImage == null) {
                    return;
                }
                CardDownloadData downloadData = getItemData(downloadDat);
                if (downloadData != null) {
                    if (downloadData.mCompleted) {
                        if (DownloadManagerMaintainer.getInstance().getDownloadStatus(downloadData._id) ==
                                DownloadManagerMaintainer.STATE_FAILED) {
                            showDownloadFailedUI();
                            return;
                        } else if (DownloadManagerMaintainer.getInstance().getDownloadStatus(downloadData._id) ==
                                DownloadManagerMaintainer.STATE_DOWNLOADING) {
                            showDownloadingUI(99);
                            return;
                        } else if (DownloadManagerMaintainer.getInstance().getDownloadStatus(downloadData._id) ==
                                DownloadManagerMaintainer.STATE_COMPLETED) {
                            showDownloadCompleted(downloadDat);
                            return;
                        }
                        showDownloadFailedUI();
                        return;
                    }
                    showDownloadingUI(downloadData.mPercentage);
                }
            }
        }, 1000);

    }


    private CardDownloadData getItemData(CardDownloadData downloadData) {

        String downloadedKey = null;
        if (downloadData.tvSeasonsList == null && downloadData.tvEpisodesList == null) {
            downloadedKey = downloadData._id;
            if (mData.publishingHouse != null
                    && APIConstants.TYPE_HOOQ.equalsIgnoreCase(mData.publishingHouse.publishingHouseName)) {
                downloadedKey = downloadData.downloadKey;
            }
            if (mData._id.equalsIgnoreCase(downloadedKey)) {
                return downloadData;
            }
        } else if (downloadData.tvSeasonsList == null && downloadData.tvEpisodesList != null) {
            for (CardDownloadData episode : downloadData.tvEpisodesList) {
                downloadedKey = mData._id;
                if (mData.publishingHouse != null
                        && APIConstants.TYPE_HOOQ.equalsIgnoreCase(mData.publishingHouse.publishingHouseName)) {
                    downloadedKey = episode.downloadKey;
                }
                if (mData._id.equalsIgnoreCase(downloadedKey)) {
                    return episode;
                }
            }
        } else if (downloadData.tvSeasonsList != null) {
            for (SeasonData seasonData : downloadData.tvSeasonsList) {
                for (CardDownloadData episode : seasonData.tvEpisodesList) {
                    downloadedKey = episode._id;
                    if (mData.publishingHouse != null
                            && APIConstants.TYPE_HOOQ.equalsIgnoreCase(mData.publishingHouse.publishingHouseName)) {
                        downloadedKey = episode.downloadKey;
                    }
                    if (mData._id.equalsIgnoreCase(downloadedKey)) {
                        return episode;
                    }
                }
            }
        }

        return null;
    }

    public void showGoogleNativeAds() {
        if (!Util.isPremiumUser() && PrefUtils.getInstance().isAdEnabled() &&
                PrefUtils.getInstance().getPrefBannerBelowCoverPoster() != null) {
            if (PrefUtils.getInstance().getPrefBannerBelowCoverPoster().contains(";")) {
                String[] adUnitIdAndSizes = PrefUtils.getInstance().getPrefBannerBelowCoverPoster().split(";");
                String adUnitId = adUnitIdAndSizes[0];
                String adUnitSizes = adUnitIdAndSizes[1];
                String adUnitPosition = adUnitIdAndSizes[2];
                List<AdSizes> adSizes = getAdSizes(adUnitSizes);
                if (adUnitId != null && adUnitPosition != null) {
                    AdLoader adLoader = new AdLoader.Builder(mContext, adUnitId)
                            .forAdManagerAdView(new OnAdManagerAdViewLoadedListener() {
                                                    @Override
                                                    public void onAdManagerAdViewLoaded(@NotNull AdManagerAdView adView) {
                                                        // Show the banner ad.
                                                        bannerCustomAdContainer.setVisibility(VISIBLE);
                                                        template.setVisibility(GONE);
                                                        bannerCustomAdContainer.addView(adView);
                                                    }
                                                }, getGoogleAdSize(adSizes, 0)
                                    , getGoogleAdSize(adSizes, 1)
                                    , getGoogleAdSize(adSizes, 2))
                            .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                                @Override
                                public void onNativeAdLoaded(@NotNull NativeAd nativeAd) {
                                    // Show the ad.
                                    template.setVisibility(VISIBLE);
                                    bannerCustomAdContainer.setVisibility(GONE);
                                    template.setNativeAd(nativeAd);
                                }
                            })
                            .withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NotNull LoadAdError error) {
                                    Util.loadAdError(error, TAG);
                                    bannerCustomAdContainer.setVisibility(GONE);
                                    template.setVisibility(GONE);
                                    // Handle the failure by logging, altering the UI, and so on.
                                }
                            })
                            .withAdManagerAdViewOptions(new AdManagerAdViewOptions.Builder()
                                    // Methods in the AdManagerAdViewOptions.Builder class can be
                                    // used here to specify individual options settings.

                                    .build())
                            .build();
                    adLoader.loadAd(new AdManagerAdRequest.Builder()
                            .addCustomTargeting("ad_position", adUnitPosition)
                            .addCustomTargeting("content_type", mData.getType())
                            .addCustomTargeting("content_id", mData._id)
                            .addCustomTargeting("content_language", getLanguage())
                            .addCustomTargeting("user_type", Util.getUserType())
                            .addCustomTargeting("gender", Util.getGenderString())
                            .addCustomTargeting("age", Util.getAgeString())
                            .addCustomTargeting("content_name", mData.getTitle())
                            .addCustomTargeting("tags", "content tags")
                            .addCustomTargeting("content_page", tabName)
                            .addCustomTargeting("duration", Util.getDurationString(contentDuration))
                            .addCustomTargeting("consent_targeting", "Yes")
                            .addCustomTargeting("source", "direct")
                            .addCustomTargeting("video_watch_count", PrefUtils.getInstance().getAdVideoCount() + "")
                            .build());

                    PrefUtils.getInstance().setAdVideoCount(PrefUtils.getInstance().getAdVideoCount() + 1);

                    String AdData = "Ad tags::: Brief " + "ad_position : " + adUnitPosition + "  content_type : " + mData.getType() + "  content_id : " + mData._id +
                            "   content_language : " + getLanguage() + "  user_type : " + Util.getUserType() + "  gender : " + Util.getGenderString() +
                            "  age : " + Util.getAgeString() + "  content_name : " + mData.getTitle() + "  content_page : " + tabName + "  duration : " + Util.getDurationString(contentDuration) +
                            "  video_watch_count : " + PrefUtils.getInstance().getAdVideoCount();

                    if (ApplicationController.SHOW_PLAYER_LOGS) {
                        AlertDialogUtil.showAdAlertDialog(mContext, AdData, "AD Logs", "Okay");
                    }
                } else {
                    bannerCustomAdContainer.setVisibility(GONE);
                    template.setVisibility(GONE);
                }
            } else {
                bannerCustomAdContainer.setVisibility(GONE);
                template.setVisibility(GONE);
            }
        }
    }

    private String getLanguage() {
        if (mData != null && mData.content != null && mData.content.language != null
                && !mData.content.language.isEmpty() && mData.content.language.get(0) != null) {
            return mData.content.language.get(0);
        }
        return "NA";
    }

    private AdSize getGoogleAdSize(List<AdSizes> adSizesList, int position) {
        if (adSizesList.size() > position) {
            return new AdSize(Integer.parseInt(adSizesList.get(position).adWidth), Integer.parseInt(adSizesList.get(position).adHeight));
        }
        return AdSize.BANNER;
    }

    private List<AdSizes> getAdSizes(String adSizes) {
        List<AdSizes> adSizesList = new ArrayList<>();
        String[] adSizesArray = adSizes.split(";");
        for (String s : adSizesArray) {
            String[] eachAdSize = s.split(",");
            AdSizes adSizeObject = new AdSizes();
            adSizeObject.adWidth = eachAdSize[0];
            adSizeObject.adHeight = eachAdSize[1];
            adSizesList.add(adSizeObject);
        }
        return adSizesList;
    }

    private String checkAndReturnValue(CardData mCardData) {
        if (mCardData != null && mCardData.generalInfo != null && mCardData.generalInfo.type != null) {
            return mCardData.generalInfo.type;
        }
        return "NA";
    }

    private AdLayoutsData getAdData(ArrayList<AdLayoutsData> adLayoutsData, String layoutType) {
        for (int p = 0; p < adLayoutsData.size(); p++) {
            if (layoutType.equalsIgnoreCase(adLayoutsData.get(p).layoutType)) {
                return adLayoutsData.get(p);
            }
        }
        return null;
    }

    private AdSize getAdSize(Context mContext) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = ((MainActivity) mContext).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mContext, adWidth);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
}
