package com.myplex.myplex.ui.views;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.FavouriteCheckRequest;
import com.myplex.api.request.content.FavouriteRequest;
import com.myplex.api.request.content.SimilarContentRequest;
import com.myplex.api.request.user.Comments;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataComments;
import com.myplex.model.CardDataCommentsItem;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CardDataHolder;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardDataRelatedCastItem;
import com.myplex.model.CardDataRelatedMultimediaItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardResponseData;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.SeasonData;
import com.myplex.model.ValuesResponse;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;
import com.myplex.util.StringEscapeUtils;
import com.myplex.util.StringManager;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.events.RefreshPotraitUI;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.activities.ProgramGuideChannelActivity;
import com.myplex.myplex.ui.adapter.AdapterBigHorizontalCarousel;
import com.myplex.myplex.ui.adapter.AdapterLiveTvItem;
import com.myplex.myplex.ui.adapter.AdapterSmallHorizontalCarousel;
import com.myplex.myplex.ui.adapter.DatesAdapter;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.ui.fragment.FragmentChannelEpg;
import com.myplex.myplex.ui.fragment.FragmentEpisodes;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.views.gifview.GifAnimationDrawable;
import com.myplex.myplex.utils.DownloadUtil;
import com.myplex.myplex.utils.Entities;
import com.myplex.myplex.utils.FetchDownloadProgress;
import com.myplex.myplex.utils.LocaleFontUtil;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static android.view.View.GONE;


public class CardDetailViewFactory implements FetchDownloadProgress.DownloadProgressStatus {
    public static final int CARDDETAIL_BRIEF_DESCRIPTION = 0;
    public static final int CARDDETAIL_TITLE_SECTION_VIEW = 1;
    public static final int CARDDETAIL_PACKAGES_VIEW = 2;
    public static final int CARDDETAIL_COMMENTS_VIEW = 3;
    public static final int CARDDETAIL_REMINDER_BUTTON = 4;
    public static final int CARDDETAIL_SIMILAR_VIEW = 5;
    public static final int CARDDETAIL_EPG_LAYOUT = 6;
    public static final int CARDDETAIL_DROPDOWN_TITLE = 7;
    // title sections
    public static final int CARDDETAIL_EPISODES_TITLE_LAYOUT = 8;
    public static final int CARDDETAIL_EPISODES_LAYOUT = 9;
    private final HandlerThread mHandlerThread;

    private CardDetailViewFactoryListener mListener;
    private Context mContext;
    private LayoutInflater mInflater;
    private CardData mData;
    private View mParentView;

    private View.OnClickListener mPackSubscribeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof CardDataPackages) {
                CardDataPackages packageitem = (CardDataPackages) v.getTag();
                SubcriptionEngine mSubscriptionEngine = new SubcriptionEngine(mContext);
                for (int i = 0; i < packageitem.priceDetails.size(); i++) {
                    CardDataPackagePriceDetailsItem packPriceItem = packageitem.priceDetails.get(i);
                    if (packPriceItem.name.equalsIgnoreCase("sundirect")) {
                        mSubscriptionEngine.doSubscription(packageitem, i);
                        return;
                    }
                }
            }
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
    private RecyclerView mRecyclerViewCarouselInfo;
    private ImageView mFavouriteImgBtn;
    private int mEpgDatePosition;
    private FragmentChannelEpg mFragmentChannelEpg;
    private TextView mEPGTitleDate;
    private TextView mTextViewSeasons,mTextViewSeasonsLang;
    private View.OnClickListener mDateLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onShowPopup();
        }
    };
    private ImageView downloadbtnImage;

    private Handler mHandler = null;
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
    private ImageView sharebtnimage;
    private String _id;
    private View.OnClickListener mFavouriteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mFavouriteImgBtn.setEnabled(false);
            mFavouriteImgBtn.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mFavouriteImgBtn != null)
                        mFavouriteImgBtn.setEnabled(true);
                }
            }, 2000);

            Util.showFeedback(mFavouriteImgBtn);
//                favouritebtnimage.setImageResource(0);
//                AlertDialogUtil.showToastNotification("work in progress");
            String type = mData.generalInfo.type;
            _id = mData._id;
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
            FavouriteRequest.Params favouritesParams = new FavouriteRequest.Params(_id,type);

//                AlertDialogUtil.showToastNotification("Please wait while we update the data...");
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
                                return;
                            }

                            //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                            if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                                showFavaouriteButton(true);
//                                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
//                                    showFavaouriteButton(false);
//                                }
                                if (mData != null
                                        && mData.currentUserData != null) {
                                    mData.currentUserData.favorite = response.body().favorite;
                                }

                                if (response.body().favorite) {
                                    mFavouriteImgBtn.setImageResource(R.drawable.description_added_to_watchlist_icon);
                                    AlertDialogUtil.showToastNotification("Added to Watchlist");
                                    PrefUtils.getInstance().shouldChangeFavouriteState(_id,true,tabName);
                                    PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER,true);
                                    ScopedBus.getInstance().post(new RefreshPotraitUI());
                                    if(mData != null && mData._id != null && !TextUtils.isEmpty(mData._id)) {
                                        CleverTap.eventPromoVideoAddedToWatchList(APIConstants.NOT_AVAILABLE, mData._id, CleverTap.SOURCE_DETAILS_SCREEN);
                                    }
                                    SDKLogger.debug("Added to Watchlist");
                                } else {
                                    AlertDialogUtil.showToastNotification("Removed from Watchlist");
                                    mFavouriteImgBtn.setImageResource(R.drawable.description_add_to_watchlist_icon);
                                    PrefUtils.getInstance().shouldChangeFavouriteState(_id,false,tabName);
                                    PrefUtils.getInstance().setBoolean(APIConstants.UPDATE_PORTRAIT_BANNER,true);
                                    ScopedBus.getInstance().post(new RefreshPotraitUI());
                                    SDKLogger.debug("Removed from Watchlist");
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
    private TextView movieName;
    private TextView releaseDate;
    private TextView moviedescription;
    private TextView moviegenre;
    private TextView castheadingtext;
    private TextView movietrailertext;
    private View castlayout;
    private TextView carddetailbriefdescription_cast;
    private ImageView mLangConIcon;
    private View.OnClickListener descriptionexpandtextListener  =new View.OnClickListener() {

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
    private TextView descriptionexpandtext;

    private View.OnClickListener castexpandtextListener  =new View.OnClickListener() {

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
            descriptionexpandtext.setVisibility(GONE);
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

    private TextView castexpandtext;
    private Runnable castLineAlignTask = new Runnable() {
        @Override
        public void run() {
            if (castexpandtext == null) {
                return;
            }
            castexpandtext.setVisibility(GONE);
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

    private View programthumnailcontainer;
    private TextView relatedcastdescriptiontext;
    private View relatedcastdescriptionlayout;
    private ImageView liveChannelImageIcon;
    private TextView carddetailbriefdescription_duration;
    private View year_duration_seperator;
    private TextView carddetailbriefdescription_language;
    private View lang_genre_seperator;

    private ImageView downloadingGIFAnim;
    private TextView downloadbtnStatusPercentText;
    private LinearLayout downloadbtnLayout;
    private boolean isToShowDownloadButton;
    private FragmentEpisodes mFragmentEpiosodes;
    private CardData mTVShowData;
    private DatesAdapter mPopupListAdapter;
    private ImageView mEpisodeDropDownIcon;
    private View seasons_episode_title_layout;
    private View description_layout;
    private String tabName;

    public void setSelectedSeasonPosition(int mSelectedSeasonPosition) {
        this.selectedSeasonPosition = mSelectedSeasonPosition;
    }

    public int getSelectedSeasonPosition() {
        return selectedSeasonPosition;
    }

    private int selectedSeasonPosition;
    private List<String> mSeasonList;
    private FragmentEpisodes.OnSeasonsLoadedListener mSeasonFetchListener = new FragmentEpisodes.OnSeasonsLoadedListener() {
        @Override
        public void onSeasonsDataLoaded(List<String> seasonsList) {
            if (mListener != null) {
                mListener.onSeasonDataLoaded(seasonsList);
            }
            mEpisodeDropDownIcon.setVisibility(View.VISIBLE);
            mSeasonList = seasonsList;
            if (seasonsList != null
                    && !seasonsList.isEmpty()) {
                mTextViewSeasons.setText(seasonsList.get(selectedSeasonPosition));
                if(PrefUtils.getInstance().getVernacularLanguage()) {
                    if (mSeasonList.get(selectedSeasonPosition) != null && !mSeasonList.get(selectedSeasonPosition).isEmpty()) {
                        String[] arr = mSeasonList.get(selectedSeasonPosition).split(" ");
                        if (arr != null && arr.length > 1) {
                            mTextViewSeasonsLang.setVisibility(View.VISIBLE);
                            mTextViewSeasonsLang.setText(StringManager.getInstance().getString(APIConstants.SEASON) + " " + arr[1]);
                        }else{
                            mTextViewSeasonsLang.setVisibility(GONE);
                        }
                    }else{
                        mTextViewSeasonsLang.setVisibility(GONE);
                    }
                }else{
                    mTextViewSeasonsLang.setVisibility(GONE);
                }
            }
            if (mPopupListAdapter == null) {
                mPopupListAdapter = new DatesAdapter(mContext, DatesAdapter.AdapterType.SEASONS, mSeasonList);
            }
            seasons_episode_title_layout.setEnabled(true);
            mPopupListAdapter.setData(seasonsList);
            mPopupListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onEpiosodesLoaded(List<CardData> episodes, boolean isLoadMore) {
            if (mListener != null) {
                mListener.onEpiosodesLoaded(episodes, isLoadMore);
            }
        }

        @Override
        public void onSeasonsNotAvailable() {
            if (mListener != null) {
                mListener.onSeasonNotAvailable();
            }
            seasons_episode_title_layout.setEnabled(false);
            if (mTextViewSeasons != null) {
                mTextViewSeasons.setText(mContext.getString(R.string.vf_txt_episodes));
                mTextViewSeasonsLang.setVisibility(GONE);
            }
        }
    };


    private void fillPackagesSection(CardData packsData) {
        if (packsData.packages == null ||
                packsData.packages.size() == 0) {
            return;
        }

        if (mPackagesContentLayout != null) {
            mPackagesContentLayout.removeAllViews();
        }

        for (CardDataPackages currentPackage : packsData.packages) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.carddetailpackages_listitem, null, false);
            TextView packageSubscribeText = (TextView) convertView.findViewById(R.id
                    .carddetailpack_subscribe_text);
            RelativeLayout packageSubscribeTextLayout = (RelativeLayout) convertView.findViewById(R.id
                    .carddetailpack_subscribe_text_layout);
            TextView packageTitle = (TextView) convertView.findViewById(R.id.carddetailpack_name);
            TextView packageDescription = (TextView) convertView.findViewById(R.id
                    .carddetailpack_description);
            TextView packageOfferDescription = (TextView) convertView.findViewById(R.id
                    .carddetailpack_offer_description);
            packageSubscribeTextLayout.setVisibility(View.VISIBLE);
            if (currentPackage.displayName != null) {
                packageTitle.setText(currentPackage.displayName);
            }
            if (currentPackage.bbDescription != null) {
                packageDescription.setText(currentPackage.bbDescription);
            }
//            if (currentPackage.cpDescripton != null) {
//                packageOfferDescription.setVisibility(View.VISIBLE);
//                packageOfferDescription.setText(currentPackage.cpDescripton);
//            } else {
            packageOfferDescription.setVisibility(GONE);
//            }
//            if (!TextUtils.isEmpty(currentPackage.cpDescriptionV2)) {
//                packageOfferDescription.setVisibility(View.VISIBLE);
//                packageOfferDescription.setText(currentPackage.cpDescriptionV2);
//            }
        /*for (CardDataPackagePriceDetailsItem priceDetails : currentPackage.priceDetails) {
                mViewHolder.mPackageSubscribeText.setText("Rs " + priceDetails.price+"/-");
        }*/
            packageSubscribeText.setTag(currentPackage);
            if (!TextUtils.isEmpty(currentPackage.actionButtonText)) {
                packageSubscribeText.setText(StringEscapeUtils.unescapeJava(currentPackage.actionButtonText));
            }
            if (!TextUtils.isEmpty(currentPackage.actionButtonTextV2)) {
                packageSubscribeText.setText(StringEscapeUtils.unescapeJava(currentPackage.actionButtonTextV2));
            }
            packageSubscribeText.setOnClickListener(mPackSubscribeListener);
            mPackagesContentLayout.addView(convertView);
        }

    }

    public void setEpgDatePosition(int epgDatePosition) {
        mEpgDatePosition = epgDatePosition;
    }

    public boolean isLive(CardData data) {
        if (data != null && data.generalInfo != null
                && APIConstants.TYPE_LIVE.equalsIgnoreCase(data.generalInfo.type)) {
            return true;
        }
        return false;
    }


    public void setTVShowData(CardData mTVShowData) {
        this.mTVShowData = mTVShowData;
    }

    public void onScroll(int scrollY) {
        if (mFragmentEpiosodes != null) {
            mFragmentEpiosodes.onScroll(scrollY);
        }
    }

    public void addDownloadProgressListeners() {
        showDownloadButton();
    }

    public void updateDescriptionData(CardData mData) {
        this.mData = mData;
        LoggerD.debugLog("update data- " + mData);

        downloadData = DownloadUtil.getDownloadDataFromDownloads(mData);
        if (mData != null
                && mTVShowData == null) {
            showTitle();
            showDescription();
        }
        showReleaseDate();
        showLanguageAndGenre();
        showDownloadButton();
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public interface CardDetailViewFactoryListener {
        void onPlayTrailer();
        void onPlayTrailerFromCarousel(int position);
        void onBuy();

        void onSimilarMoviesDataLoaded(String status);
        void onSimilarMoviesDataLoaded(String status,int position);

        void onShowPopup();

        void onPopupItemSelected(String date);

        void onDownloadContent(FragmentCardDetailsDescription.DownloadStatusListener downloadStatusListener);

        void onSeasonDataLoaded(List<String> seasonsList);

        void onEpiosodesLoaded(List<CardData> episodes, boolean isLoadMore);

        void onSeasonNotAvailable();

        FragmentManager getSuperChildFragmentManager();

        int getEpisodesLayoutStartPosition();

        void notifyItemChanged(int adapterPosition);

        void onLatestEpisodeButtonClick();
    }

    public void setOnCardDetailExpandListener(CardDetailViewFactoryListener listener) {
        this.mListener = listener;
    }

    public CardDetailViewFactory(Context cxt) {
        this.mContext = cxt;
        mInflater = LayoutInflater.from(mContext);
        mHandler = new Handler(Looper.getMainLooper());
        mHandlerThread = new HandlerThread("AdapterUpdater");
        mHandlerThread.start();
        selectedSeasonPosition = 0;
    }

    public void setParent(View parent) {
        this.mParentView = parent;
    }

    public View CreateView(CardData data, int type) {
        mData = data;
        switch (type) {
            case CARDDETAIL_BRIEF_DESCRIPTION:
                return createBriefDescriptionView();
            case CARDDETAIL_PACKAGES_VIEW:
                return createPackageView();
            case CARDDETAIL_COMMENTS_VIEW:
                return createCommentsView();
            case CARDDETAIL_SIMILAR_VIEW:
                return createSimilarMoviesView();
            case CARDDETAIL_EPG_LAYOUT:
                return createEPGLayout();
            case CARDDETAIL_DROPDOWN_TITLE:
                return createEPGLayoutTitle();
            case CARDDETAIL_EPISODES_TITLE_LAYOUT:
                return createEpisodesLayoutTitle();
            case CARDDETAIL_EPISODES_LAYOUT:
                return createEpisodeLayout();
//            case CARDDETAIL_TITLE_SECTION_VIEW:
//                return createTitleSectionView(title, altTitle);
//                break;
//            case CARDDETAIL_REMINDER_BUTTON:
//                return createbuttonLayout();
            default:
                break;
        }
        return null;
    }

    private View createEPGLayoutTitle() {
        View v = mInflater.inflate(R.layout.layout_channel_title_date, null);
        v.findViewById(R.id.date_layout).setOnClickListener(mDateLayoutClickListener);
        mEPGTitleDate = (TextView) v.findViewById(R.id.header_drop_down_title);
        TextView name_other_language = v.findViewById(R.id.header_sub_title_textLang);
        if(PrefUtils.getInstance().getVernacularLanguage() && !TextUtils.isEmpty(StringManager.getInstance().getString(APIConstants.UPCOMING_PROGRAMS))){
            name_other_language.setText(StringManager.getInstance().getString(APIConstants.UPCOMING_PROGRAMS));
            name_other_language.setVisibility(View.VISIBLE);
        }else{
            name_other_language.setVisibility(GONE);
        }
        ArrayList<String> nxtDateList = Util.showNextDates();
        String date = nxtDateList.get(mEpgDatePosition);
        setDateToCalender(date);
        return v;
    }


    private View createEpisodesLayoutTitle() {
        seasons_episode_title_layout = mInflater.inflate(R.layout.carddetailsdescription_episodes_season_title, null);
        seasons_episode_title_layout.setOnClickListener(mDateLayoutClickListener);
        mTextViewSeasons = (TextView) seasons_episode_title_layout.findViewById(R.id.header_title_text);
        mTextViewSeasonsLang = (TextView) seasons_episode_title_layout.findViewById(R.id.header_sub_title_textLang);
        if(PrefUtils.getInstance().getVernacularLanguage()){
            mTextViewSeasonsLang.setVisibility(View.VISIBLE);
        }else{
            mTextViewSeasonsLang.setVisibility(GONE);
        }
        mEpisodeDropDownIcon = (ImageView) seasons_episode_title_layout.findViewById(R.id.drop_down_button);
        mEpisodeDropDownIcon.setVisibility(GONE);
        return seasons_episode_title_layout;
    }

    private void onPopupItemSelected() {
        if (mData.isProgram() || mData.isLive()) {
            if (mFragmentChannelEpg == null) {
                return;
            }
            ArrayList<String> nxtDateList = Util.showNextDates();
            String date = nxtDateList.get(mEpgDatePosition);
            setDateToCalender(date);
            if (mListener != null) {
                mListener.onPopupItemSelected(date);
            }
            mFragmentChannelEpg.updateEPG(mEpgDatePosition);
            return;
        }
        if (mFragmentEpiosodes != null) {
            if (mListener != null) {
                mListener.onPopupItemSelected(mSeasonList.get(selectedSeasonPosition));
            }
            mTextViewSeasons.setText(mSeasonList.get(selectedSeasonPosition));
            if(ApplicationController.IS_VERNACULAR_TO_BE_SHOWN) {
                if (mSeasonList.get(selectedSeasonPosition) != null && !mSeasonList.get(selectedSeasonPosition).isEmpty()) {
                    String[] arr = mSeasonList.get(selectedSeasonPosition).split(" ");
                    if (arr != null && arr.length > 1) {
                        mTextViewSeasonsLang.setVisibility(View.VISIBLE);
                        if(StringManager.getInstance().getString(APIConstants.SEASON)!= null) {
                            mTextViewSeasonsLang.setText(StringManager.getInstance().getString(APIConstants.SEASON) + " " + arr[1]);
                        }else{
                            mTextViewSeasonsLang.setVisibility(GONE);
                        }
                    }else{
                        mTextViewSeasonsLang.setVisibility(GONE);
                    }
                }else{
                    mTextViewSeasonsLang.setVisibility(GONE);
                }
            }else{
                mTextViewSeasonsLang.setVisibility(GONE);
            }
            mFragmentEpiosodes.updateSeasonPosition(selectedSeasonPosition);
        }
    }

    private View createEPGLayout() {
        View v = mInflater.inflate(R.layout.layout_fragment_container, null);
        if (mData == null) {
            return null;
        }
        MainActivity mActivity = (MainActivity) mContext;
        Bundle args = new Bundle();
        args.putSerializable(ProgramGuideChannelActivity.PARAM_CHANNEL_DATA, mData);
        args.putInt(ProgramGuideChannelActivity.DATE_POS, mEpgDatePosition);
        args.putBoolean(FragmentChannelEpg.PARAM_IS_TO_SHOW_ONLY_EPG, true);
        mFragmentChannelEpg = FragmentChannelEpg.newInstance(args);
        try {
            FragmentManager fragmentManager = mListener.getSuperChildFragmentManager();
//            removeFragment(mCurrentFragment);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, mFragmentChannelEpg);
            mFragmentChannelEpg.setBaseActivity(mActivity);
            mFragmentChannelEpg.setContext(mContext);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        } catch (Throwable e) {
            e.printStackTrace();
//            Crashlytics.logException(e);
        }
//        mFragmentChannelEpg.getView().setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return v;
    }

    private View createEpisodeLayout() {
        View v = mInflater.inflate(R.layout.layout_fragment_container, null);
        if (mData == null) {
            return null;
        }
        MainActivity mActivity = (MainActivity) mContext;
        Bundle args = new Bundle();

        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, mTVShowData);
        mFragmentEpiosodes = FragmentEpisodes.newInstance(args);
        mFragmentEpiosodes.setOnSeasonsLoaded(mSeasonFetchListener);
        try {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
//            removeFragment(mCurrentFragment);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, mFragmentEpiosodes);
            mFragmentEpiosodes.setBaseActivity(mActivity);
            mFragmentEpiosodes.setContext(mContext);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        } catch (Throwable e) {
            e.printStackTrace();
//            Crashlytics.logException(e);
        }
//        mFragmentChannelEpg.getView().setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return v;
    }

    private View createSimilarMoviesView() {
        if (mData == null) {
            return null;
        }

        View v = mInflater.inflate(R.layout.fragment_carouselinfo, null);
        mRecyclerViewCarouselInfo = (RecyclerView) v.findViewById(R.id.recyclerview);

        HorizontalItemDecorator mHorizontalDividerDecoration = new HorizontalItemDecorator((int) mContext
                .getResources().getDimension(R.dimen.margin_gap_2));
        mRecyclerViewCarouselInfo.addItemDecoration(mHorizontalDividerDecoration);
        mRecyclerViewCarouselInfo.setItemAnimator(null);
        if (isLiveOrProgram() || isMusicVideo()) {
            AdapterLiveTvItem mAdapterCarouselInfo = new AdapterLiveTvItem(mContext, getDummyCarouselData(),mRecyclerViewCarouselInfo);
            mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
        } else {
            AdapterBigHorizontalCarousel mAdapterCarouselInfo = new AdapterBigHorizontalCarousel(mContext, getDummyCarouselData());
            mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
        }
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewCarouselInfo.setLayoutManager(layoutManager);
        fetchSimilarContent();
        return v;
    }

    private boolean isMusicVideo() {
        if (mData != null
                && mData.generalInfo != null
                && APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(mData.generalInfo.type)) {
            return true;
        }
        return false;
    }

    private final List<CardData> mDummyCarouselData = new ArrayList<>();

    private List<CardData> getDummyCarouselData() {
        if (!mDummyCarouselData.isEmpty()) {
            return mDummyCarouselData;
        }
        for (int i = 0; i < 10; i++) {
            mDummyCarouselData.add(new CardData());
        }
        return mDummyCarouselData;
    }

    private void fetchSimilarContent() {

        String contentId = mData._id;
        if (isMusicVideo()) {
            fetchRelatedVideos();
            return;
        } else if (isLiveOrProgram()) {
            fetchEpgData();
            return;
        }
        SimilarContentRequest.Params contentListparams = new SimilarContentRequest.Params(contentId, APIConstants.LEVEL_DEVICE_MAX, 1, APIConstants.PAGE_INDEX_COUNT);

        SimilarContentRequest mRequestContentList = new SimilarContentRequest(contentListparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        if (response == null
                                || response.body() == null) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }

                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: message- " + response.body().message);
                        if (response.body().results == null || response.body().results.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }
                        List<CardData> dataList = new ArrayList<>(response.body().results);
                        dataList = removeDuplicates(dataList);
                        if (dataList == null
                                || dataList.isEmpty()
                                || dataList.size() < 3) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }
                        mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                        AdapterBigHorizontalCarousel mAdapterCarouselInfo = new AdapterBigHorizontalCarousel(mContext, dataList);
                        mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);

                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: t- " + t);
                        if (mListener != null) {
                            mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                        }
                    }
                });
        APIService.getInstance().execute(mRequestContentList);

    }

    private void fetchEpgData() {

        final Date date = Util.getCurrentDate(0);
        final String time = Util.getCurrentEpgTablePosition();
        final StringBuilder languages = new StringBuilder();
        if (mData != null
                && mData.content != null
                && mData.content.language != null
                && mData.content.language.size() > 0) {
            for (String language : mData.content.language) {
                languages.append(languages.length() == 0 ? language : "," + language);
            }
        }
        final StringBuilder genres = new StringBuilder();
        if (mData != null
                && mData.content != null
                && mData.content.genre != null
                && mData.content.genre.size() > 0) {
            for (CardDataGenre genre : mData.content.genre) {
                genres.append(genres.length() == 0 ? genre.name : "," + genre.name);
            }
        }
        final String oldLanguage = EPG.langFilterValues;
        final String oldGenre = EPG.genreFilterValues;
        EPG.langFilterValues = languages.toString();
        EPG.genreFilterValues = genres.toString();
        LoggerD.debugLog("TVGuide: fetchEpgData: time- " + time + "" +
                " oldGenre- " + oldGenre + "" +
                " languages- " + languages + "" +
                " oldLanguage- " + oldLanguage);

        boolean isDVROnly = (PrefUtils.getInstance().getPrefEnablePastEpg() && 0 - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0) ? true : false;

        EPG.getInstance(Util.getCurrentDate(0)).findPrograms(11, Util.getServerDateFormat(time, date), time, date, 1, true, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly, "",new EPG.CacheManagerCallback() {

            @Override
            public void OnlineResults(List<CardData> dataList, int pageIndex) {
                //System.out.println("phani size "+dataList.size());
                EPG.langFilterValues = oldLanguage;
                EPG.genreFilterValues = oldGenre;
                List list = removeSameProgram(dataList);
                if (list == null
                        || list.isEmpty()
                        || list.size() < 2) {
                    if (mListener != null) {
                        mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                    }
                    return;
                }

                mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                AdapterLiveTvItem mAdapterCarouselInfo = new AdapterLiveTvItem(mContext, list,mRecyclerViewCarouselInfo);
                mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
            }

            @Override
            public void OnlineError(Throwable error, int errorCode) {
                if (mListener != null) {
                    mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                }
            }
        }, true);


    }

    private List<CardData> removeSameProgram(List<CardData> dataList) {
        if (mData == null
                || dataList == null
                || dataList.isEmpty()) {
            return dataList;
        }
        String globalServiceId = mData.globalServiceId;
        if (isLive(mData)) {
            globalServiceId = mData._id;
        }
        if (globalServiceId == null) {
            return dataList;
        }
        List<CardData> cardDataList = new ArrayList<>();
        cardDataList.addAll(dataList);
        for (int i = 0; i < cardDataList.size(); i++) {
            if (cardDataList.get(i).globalServiceId != null && cardDataList.get(i).globalServiceId.equalsIgnoreCase(globalServiceId)) {
                cardDataList.remove(i);
                return cardDataList;
            }
        }

        return cardDataList;
    }

    private void fetchRelatedVideos() {
        if (mData == null
                || TextUtils.isEmpty(mData.globalServiceId)) {
            //Log.d(TAG, "fetchRelatedVideos: globalServiceId is NA");
            return;
        }
        new CacheManager().getRelatedVODListTypeExclusion(mData.globalServiceId, 1, true, APIConstants.TYPE_TVSEASON,
                APIConstants.PAGE_INDEX_COUNT,
                new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }
                        //Log.d(TAG, "fetchFilterDataWithLanguageAndGenre: onResponse: message - ");
                        List<CardData> dataList1 = new ArrayList<>(dataList);
                        dataList1 = removeDuplicates(dataList1);
                        if (dataList1 == null
                                || dataList1.isEmpty()
                                || dataList.size() < 3) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }

                        mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                        AdapterSmallHorizontalCarousel mAdapterCarouselInfo = new AdapterSmallHorizontalCarousel(mContext, dataList1,mRecyclerViewCarouselInfo);
                        mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);
                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        if (dataList == null || dataList.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }

                        List<CardData> dataList1 = new ArrayList<>(dataList);
                        dataList1 = removeDuplicates(dataList1);
                        if (dataList1 == null || dataList1.isEmpty()) {
                            if (mListener != null) {
                                mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                            }
                            return;
                        }

                        mListener.onSimilarMoviesDataLoaded(APIConstants.SUCCESS);
                        AdapterSmallHorizontalCarousel mAdapterCarouselInfo = new AdapterSmallHorizontalCarousel(mContext, dataList1,mRecyclerViewCarouselInfo);
                        mRecyclerViewCarouselInfo.setAdapter(mAdapterCarouselInfo);

                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        //Log.d(TAG, "fetchCarouselData: OnOnlineError: t- ");
                        if (mListener != null) {
                            mListener.onSimilarMoviesDataLoaded(APIConstants.FAILED);
                        }
                    }
                });
    }

    public ArrayList<CardData> removeDuplicates(List<CardData> list) {
        Set<CardData> set = new TreeSet<>(new Comparator<CardData>() {
            @Override
            public int compare(CardData o1, CardData o2) {
                if (o1._id.equalsIgnoreCase(o2._id)) {
                    return 0;
                }
                return 1;
            }
        });
        for (int i = 0; i < list.size(); i++) {
            CardData data = list.get(i);
            if (mData != null
                    && mData._id.equalsIgnoreCase(data._id)) {
                list.remove(i);
                break;
            }
        }

        set.addAll(list);
        return new ArrayList(set);
    }

    private LinearLayout mPackagesContentLayout;

    private View createPackageView() {
        if (mData == null) {
            return null;
        }

        View v = mInflater.inflate(R.layout.carddetailpackages_listview, null);
        mPackagesContentLayout = (LinearLayout) v.findViewById(R.id
                .carddetailpackages_contentlayout);
        fillPackagesSection(mData);
        return v;
    }

    private List<CardDataPackages> dummyPacks() {
        List<CardDataPackages> dummyPacks = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            dummyPacks.add(new CardDataPackages());
        }
        return dummyPacks;
    }

    public View createTitleSectionView(String title) {
        if (title == null) {
            return null;
        }

        View v = mInflater.inflate(R.layout.carddetaildescriptiontitlesectionview, null);
        TextView mTitleView = (TextView) v.findViewById(R.id.carddetail_title_text);
        TextView mTitleOtherLanguageView = (TextView) v.findViewById(R.id.title_other_language);
        int textSize = 16;
        /*if (mContext.getString(R.string.carddetaila_similar_programs_section_title).equalsIgnoreCase(title)) {
            textSize = 14;
        }*/

        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        mTitleView.setText(title);
        mTitleOtherLanguageView.setVisibility(GONE);
        return v;
    }

    protected static final String TAG = CardDetailViewFactory.class.getSimpleName();


    private void setVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
            if (visibility == View.VISIBLE)
                view.setClickable(true);
        }
    }

    private View createBriefDescriptionView() {

        if (mData == null) {
            return null;
        }

        if (mData.generalInfo == null) {
            return null;
        }

        View v = mInflater.inflate(R.layout.carddetailbreifdescription, null);

        movieName = (TextView) v.findViewById(R.id.carddetailbreifdescription_movename);
        programthumnailcontainer = v.findViewById(R.id.carddetailbreifdescription_program_thumbnail_container);
        releaseDate = (TextView) v.findViewById(R.id.carddetailbriefdescription_releasedate);
        description_layout = v.findViewById(R.id.description_layout);
        moviedescription = (TextView) v.findViewById(R.id.carddetailbriefdescription_description);
        moviegenre = (TextView) v.findViewById(R.id.carddetailbriefdescription_genre);
        castlayout = v.findViewById(R.id.cast_layout);
        carddetailbriefdescription_cast = (TextView) v.findViewById(R.id.carddetailbriefdescription_cast);
        castexpandtext = (TextView) v.findViewById(R.id.carddetailbriefdescription_cast_readmore);
        castheadingtext = (TextView) v.findViewById(R.id.carddetailbriefdescription_cast_heading);
        movietrailertext = (TextView) v.findViewById(R.id.txt_trailer);
        mLangConIcon = (ImageView) v.findViewById(R.id.carddetailbriefdescription_lang_conversion_button);
        liveChannelImageIcon = (ImageView) v.findViewById(R.id.live_channel_thumbnail_icon);
        downloadbtnLayout = (LinearLayout) v.findViewById(R.id.download_btn_layout);
        downloadbtnStatusPercentText = (TextView) v.findViewById(R.id.download_btn_status_percent_text);
        downloadbtnLayout.setVisibility(GONE);
        downloadingGIFAnim = (ImageView) v.findViewById(R.id.downloading_gif_anim);
        downloadingGIFAnim.setBackgroundColor(Color.TRANSPARENT); //for gif without background

        downloadbtnImage = (ImageView) v.findViewById(R.id.carddetailbriefdescription_download_img);
        mFavouriteImgBtn = (ImageView) v.findViewById(R.id.carddetailbriefdescription_favourite_img);
        sharebtnimage = (ImageView) v.findViewById(R.id.carddetailbriefdescription_share_img);
        descriptionexpandtext = (TextView) v.findViewById(R.id.description_expand);
        relatedcastdescriptionlayout = v.findViewById(R.id.related_description_layout);
        relatedcastdescriptiontext = (TextView) v.findViewById(R.id.related_description);

        carddetailbriefdescription_duration = (TextView) v.findViewById(R.id.carddetailbriefdescription_duration);
        year_duration_seperator = v.findViewById(R.id.year_duration_seperator);

        carddetailbriefdescription_language = (TextView) v.findViewById(R.id.carddetailbriefdescription_language);
        lang_genre_seperator = v.findViewById(R.id.lang_genre_seperator);


        relatedcastdescriptionlayout.setVisibility(GONE);
        castheadingtext.setVisibility(GONE);
        downloadbtnImage.setVisibility(GONE);
        movietrailertext.setVisibility(GONE);
        programthumnailcontainer.setVisibility(GONE);
        if (mData.isProgram()) {
            showChannelIcon();
        }
        showFavaouriteButton(true);
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null))
            showFavaouriteButton(false);
        showRelatedCastText();
        showShareUI();
        updateFavouriteButton();
        showDownloadButton();
        showTitle();
        showTrailer();
        showReleaseDate();
        showLanguageAndGenre();
        showCast();
        showDescription();

        mFavouriteImgBtn.setOnClickListener(mFavouriteClickListener);
        moviedescription.post(moviedescriptionLineAlignTask);
        descriptionexpandtext.setOnClickListener(descriptionexpandtextListener);

        carddetailbriefdescription_cast.post(castLineAlignTask);
        castexpandtext.setOnClickListener(castexpandtextListener);

        return v;
    }

    private void showChannelIcon() {
        programthumnailcontainer.setVisibility(View.VISIBLE);
        String channelIconUrl = mData.getChannelIconUrl();
        if (!TextUtils.isEmpty(channelIconUrl)) {
            if (APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(channelIconUrl) == 0) {
                liveChannelImageIcon.setImageResource(R.drawable
                        .movie_thumbnail_placeholder);
                liveChannelImageIcon.setVisibility(GONE);
            } else {
                liveChannelImageIcon.setVisibility(View.VISIBLE);
                channelIconUrl = channelIconUrl.replace("epgimages/", "epgimagesV3/");
                PicassoUtil.with(mContext).load(channelIconUrl, liveChannelImageIcon, R.drawable.live_tv_channel_placeholder);
            }
        }
    }

    private void showDescription() {
        description_layout.setVisibility(View.VISIBLE);
        if (mData != null && mData.generalInfo != null && mData.generalInfo.type != null
                && (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE) || mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_NEWS))) {
//            v.findViewById(R.id.trailer_download_btns_layout).setVisibility(View.GONE);
            mLangConIcon.setImageResource(R.drawable.translation_icon);
            if (LocaleFontUtil.setLocalFontIfAvailable(mContext, mData, moviedescription)) {
                LocaleFontUtil.setLocalFontIfAvailable(mContext, mData, movieName);
                moviedescription.setText(mData.generalInfo.altDescription.get(0).description);
                movieName.setText(mData.generalInfo.altTitle.get(0).title.toLowerCase());
                if (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)) {
                    mLangConIcon.setVisibility(View.VISIBLE);
                    moviedescription.setTag(LocaleFontUtil.NATIVE);
                    CardDataHolder mDataHolder = new CardDataHolder();
                    mDataHolder.mCardDescText = moviedescription;
                    mDataHolder.mTitle = movieName;
                    mLangConIcon.setTag(mDataHolder);
                    mLangConIcon.setOnClickListener(mLocalFontListener);
                } else {
                    mLangConIcon.setVisibility(GONE);
                }
            } else if (mData.generalInfo.briefDescription != null) {

                String description = mData.generalInfo.briefDescription;
                try {
                    description = description.replaceAll("\n", "\r\n");
                    description = Entities.HTML40.unescape(description);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                moviedescription.setText(description);
                movieName.setText(mData.generalInfo.title.toLowerCase());
                moviedescription.setTextColor(mContext.getResources().getColor(R.color.white));
                movieName.setTextColor(mContext.getResources().getColor(R.color.white));
                mLangConIcon.setVisibility(GONE);
            }
        } else if (mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
            if (mData.generalInfo.briefDescription != null) {
                moviedescription.setText("" + mData.generalInfo.description);
            }
        } else {
            if (TextUtils.isEmpty(mData.getDescription())) {
                if (TextUtils.isEmpty(mData.getBriefDescription())) {
                    moviedescription.setVisibility(GONE);
                } else {
                    moviedescription.setVisibility(View.VISIBLE);
                    moviedescription.setText(mData.getBriefDescription());
                }
            } else {
                moviedescription.setVisibility(View.VISIBLE);
                moviedescription.setText(mData.getDescription());
            }
        }
        if (mTVShowData != null) {
            if (TextUtils.isEmpty(mTVShowData.getDescription())) {
                if (TextUtils.isEmpty(mTVShowData.getBriefDescription())) {
                    moviedescription.setVisibility(GONE);
                } else {
                    moviedescription.setVisibility(View.VISIBLE);
                    moviedescription.setText(mTVShowData.getBriefDescription());
                }
            } else {
                moviedescription.setVisibility(View.VISIBLE);
                moviedescription.setText(mTVShowData.getDescription());
            }
        }

    }

    private void showCast() {
        CardData mData = this.mData;
        if (mTVShowData != null) {
            mData = mTVShowData;
        }
        if (mData == null || mData.relatedCast == null
                || APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(mData.generalInfo.type)) {
            castlayout.setVisibility(GONE);
            carddetailbriefdescription_cast.setVisibility(GONE);
        } else {
            StringBuilder castMembers = new StringBuilder();
            for (CardDataRelatedCastItem relatedCast : mData.relatedCast.values) {
                if (castMembers.length() != 0) {
                    castMembers.append(", ");
                }
                castMembers.append(relatedCast.name);
            }
            if (TextUtils.isEmpty(castMembers) || castMembers.length() == 0) {
                carddetailbriefdescription_cast.setVisibility(GONE);
                castheadingtext.setVisibility(GONE);
            } else {
                castlayout.setVisibility(View.VISIBLE);
                carddetailbriefdescription_cast.setVisibility(View.VISIBLE);
                castheadingtext.setVisibility(View.VISIBLE);
                carddetailbriefdescription_cast.setVisibility(View.VISIBLE);
                carddetailbriefdescription_cast.setText(castMembers);
            }

        }
    }

    private void showLanguageAndGenre() {
        lang_genre_seperator.setVisibility(GONE);
        carddetailbriefdescription_language.setVisibility(GONE);
        if (mData.generalInfo != null) {
            if (!APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)) {
                moviegenre.setVisibility(GONE);
            }
            if (null != mData.content) {
                StringBuilder genres = new StringBuilder();
                if (mData.content.genre != null && mData.content.genre.size() > 0) {
                    for (CardDataGenre genre : mData.content.genre) {
                        if (genres.length() > 0) {
                            genres.append("| ");
                        }
                        genres.append(genre.name);
                    }
                    moviegenre.setVisibility(GONE);
                    lang_genre_seperator.setVisibility(GONE);
                    if (genres.length() > 0) {
                        moviegenre.setVisibility(View.VISIBLE);
                        moviegenre.setText(genres.toString());
                    }
                }

                if (mData.content.language != null && mData.content.language.size() > 0) {
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
                }
            }
        }

        if (mData.isProgram()) {
            if ((TextUtils.isEmpty(mData.startDate) && TextUtils.isEmpty(mData.endDate))) {
                moviegenre.setVisibility(GONE);
            } else {
                lang_genre_seperator.setVisibility(GONE);
                carddetailbriefdescription_language.setVisibility(GONE);
                moviegenre.setVisibility(View.VISIBLE);
                String startTime = mData.getTimeHHMM_AM(mData.getStartDate());
                String endTime = mData.getTimeHHMM_AM(mData.getEndDate());
                moviegenre.setText(startTime + "-" + endTime);
            }
        }

    }

    public void showReleaseDate() {
        year_duration_seperator.setVisibility(GONE);
        carddetailbriefdescription_duration.setVisibility(GONE);
        if (mData.content != null && mData.content.releaseDate != null) {
            if (mData.content.releaseDate.isEmpty() || APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type)) {
                releaseDate.setVisibility(GONE);
            } else {
                if (mData.generalInfo != null && APIConstants.TYPE_VOD.equalsIgnoreCase(mData.generalInfo.type)) {
                    releaseDate.setText(mData.getDDMMYYYYUTC());
                } else {
                    releaseDate.setText(mData.getDDMMYYYY());
                }
            }
        } else {
            releaseDate.setVisibility(GONE);
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
                releaseDate.setVisibility(GONE);
            }
        }

        if (APIConstants.TYPE_MOVIE.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_TRAILER.equalsIgnoreCase(mData.generalInfo.type)) {
            if (mData.content != null && mData.content.releaseDate != null) {
                if (TextUtils.isEmpty(mData.getYYYY())) {
                    releaseDate.setVisibility(GONE);
                    year_duration_seperator.setVisibility(GONE);
                } else {
                    releaseDate.setVisibility(View.VISIBLE);
                    releaseDate.setText(mData.getYYYY());
                }

            }
            if (mData.content != null
                    && mData.content.duration != null
                    && !mData.content.duration.equalsIgnoreCase("0:0:00")) {
                int contentDurationMnts = Util.calculateDurationInSeconds(mData.content.duration) / 60;//duratin in mints

                if (contentDurationMnts <= 0) {
                    releaseDate.setVisibility(GONE);
                    year_duration_seperator.setVisibility(GONE);
                } else {
                    if (!TextUtils.isEmpty(mData.getYYYY()))
                        year_duration_seperator.setVisibility(View.VISIBLE);
                    carddetailbriefdescription_duration.setVisibility(View.VISIBLE);
                    carddetailbriefdescription_duration.setText(contentDurationMnts +" mins");
                }
            }
        }

        if (mData.isProgram()) {
            releaseDate.setVisibility(View.VISIBLE);
            releaseDate.setText(mData.getTitle());
        }
        if (mTVShowData != null) {
            if (!mData.isTVSeries()
                    && !mData.isTVSeason()
                    && !mData.isVODYoutubeChannel()
                    && !mData.isVODCategory()
                    && !mData.isVODChannel()) {
                releaseDate.setVisibility(View.VISIBLE);
                releaseDate.setText(mData.getTitle());
            } else {
                releaseDate.setVisibility(GONE);
            }
        }
    }

    public void showTitle() {
        if (!TextUtils.isEmpty(mData.getTitle())) {
            movieName.setText(mData.getTitle());
            if (mData.isProgram()) {
                movieName.setText(mData.getChannelName());
            }
            if (mTVShowData != null) {
                releaseDate.setVisibility(View.VISIBLE);
                releaseDate.setText(mTVShowData.getTitle());
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
                    movietrailertext.setVisibility(View.VISIBLE);
                    movietrailertext.setOnClickListener(mTrailerButtonClickListener);
                }
            }
        }
    }

    private void showDownloadButton() {
        DownloadUtil.getDownloadDataFromDownloads(mData, new DownloadUtil.OnDataRetrieverListener() {
            @Override
            public void onDataLoaded(CardDownloadData data) {
                LoggerD.debugLog("update data- " + mData + " data- " + data);
                if (data != null) {
                    if (data.mCompleted) {
                        if (data.mPercentage == 100) {
                            if (APIConstants.isErosNowContent(data)
                                    && data.zipStatus < CardDownloadData.STATUS_FILE_UNZIPPED
                                    && data.mCompleted) {
                                if (data.zipStatus == CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE
                                        || data.zipStatus == CardDownloadData.STATUS_FAILED_TO_AQUIRE_DRM_LICENSE) {
                                    showDownloadFailedUI();
                                    return;
                                }
                                ErosNowDownloadManager.getInstance(mContext).addListener(mUnzipProgressListener);
                                showDownloadingUI(99);
                                return;
                            }
                            showDownloadCompleted();
                            return;
                        }
                        showDownloadFailedUI();
                    } else {
                        showDownloadingUI(data.mPercentage);
                        if (APIConstants.isHungamaContent(mData)) {
                        } else {
                            FetchDownloadProgress.getInstance(mContext).removeProgressListener(CardDetailViewFactory.this);
                            FetchDownloadProgress.getInstance(mContext).addProgressListener(CardDetailViewFactory.this);
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
                        && mData != null
                        && mData.isMovie()
                        && PrefUtils.getInstance().gePrefEnableErosnowDownloadV1())
                        ||(mData != null && APIConstants.isErosNowContent(mData)
                        &&( mData.isTVEpisode() ||mData.isTVSeason()|| mData.isVOD()))
                        || (APIConstants.isHungamaContent(mData)
                        && PrefUtils.gePrefEnableHungamaDownload())) {
                    mData.generalInfo.isDownloadable = true;
                    if ((APIConstants.isHungamaContent(mData)
                            && !APIConstants.isMovie(mData))){
                        mData.generalInfo.isDownloadable = false;
                    }
                }
                if(mData != null && APIConstants.isErosNowContent(mData)
                        && !mData.isMovie()){
                    mData.generalInfo.isDownloadable = false;
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
        downloadbtnImage.setVisibility(GONE);
        GifAnimationDrawable gifAnim = null;
        try {
            if (downloadbtnImage.getTag() instanceof GifAnimationDrawable) {
                gifAnim = (GifAnimationDrawable) downloadbtnImage.getTag();
            } else {
                gifAnim = new GifAnimationDrawable(downloadingGIFAnim.getContext().getResources().openRawResource(R.raw.download_progress_anim));
                gifAnim.setOneShot(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        final GifAnimationDrawable finalGifAnim = gifAnim;
        if (!finalGifAnim.isRunning()) {
            finalGifAnim.setVisible(true, true);
            finalGifAnim.start();
        }
        downloadingGIFAnim.setImageDrawable(gifAnim);
        downloadingGIFAnim.setVisibility(View.VISIBLE);
        downloadbtnStatusPercentText.setVisibility(View.VISIBLE);
        if (progress < 0) {
            progress = 0;
        }
        LoggerD.debugDownload("progress- " + progress + " formatted progress- " +  progress);
        downloadbtnStatusPercentText.setVisibility(View.VISIBLE);
        downloadbtnStatusPercentText.setText(String.valueOf(progress) + "%");
    }

    private void showDownloadFailedUI() {
        downloadingGIFAnim.setVisibility(GONE);
        downloadbtnImage.setImageResource(R.drawable.description_download_broken);
        downloadbtnImage.setVisibility(View.VISIBLE);
        downloadbtnStatusPercentText.setText(mContext.getString(R.string.download_state_failed_text));
    }

    private void showDownloadCompleted() {
        downloadingGIFAnim.setVisibility(GONE);
        downloadbtnStatusPercentText.setVisibility(GONE);
        downloadbtnImage.setImageResource(R.drawable.description_download_complete_icon);
        downloadbtnImage.setVisibility(View.VISIBLE);
    }

    private void showShareUI() {
        sharebtnimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharebtnimage.setEnabled(false);
                sharebtnimage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (sharebtnimage != null) {
                            sharebtnimage.setEnabled(true);
                        }
                    }
                },2000);

                Util.showFeedback(sharebtnimage);
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

                String msg = mContext.getString(R.string.share_message);
                if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefShareMessage())) {
                    msg = PrefUtils.getInstance().getPrefShareMessage();
                }
                if (msg.contains(APIConstants.HASH_CONTENT_NAME)) {
                    msg = msg.replace(APIConstants.HASH_CONTENT_NAME, TextUtils.isEmpty(contentName) ? "" : contentName);
                }
                String url = APIConstants.getShareUrl(mContext);
                if (!TextUtils.isEmpty(url)) {
                    msg = msg + "\n" + Uri.parse(url);
                }
                Util.shareData(mContext, 1, path, msg);// send message.
                return;
            }
        });
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
            }
        } else {
            relatedcastdescriptionlayout.setVisibility(GONE);
        }
    }

    private void checkAndEnableDownload(boolean enable) {
        LoggerD.debugLog(mData + " is downloadable- " + enable);
        if (enable) {
            downloadingGIFAnim.setVisibility(GONE);
            downloadbtnStatusPercentText.setVisibility(GONE);
            downloadbtnImage.setVisibility(View.VISIBLE);
            downloadbtnImage.setImageResource(R.drawable.description_download_icon_default);
            downloadbtnImage.setEnabled(true);
            downloadbtnImage.setOnClickListener(new View.OnClickListener() {
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
            downloadingGIFAnim.setVisibility(View.VISIBLE);
            downloadbtnStatusPercentText.setVisibility(View.VISIBLE);
            downloadbtnImage.setVisibility(GONE);
            downloadbtnImage.setEnabled(false);
            downloadbtnStatusPercentText.setText("Download");
            downloadingGIFAnim.setImageResource(R.drawable.download_na_icon);
            downloadbtnStatusPercentText.setAlpha(0.5f);
            downloadingGIFAnim.setAlpha(128);
            if (mData.isProgram()
                    || mData.isLive()
                    || mData.isTVSeries()
                    || mData.isTVSeason()
                    || mData.isVODCategory()
                    || mData.isVODChannel()
                    || mData.isVODYoutubeChannel()) {
                downloadbtnImage.setVisibility(GONE);
                downloadingGIFAnim.setVisibility(GONE);
                downloadbtnStatusPercentText.setVisibility(GONE);
            }
        }

    }

    public void startDownload() {
        if (mListener != null) {
            AlertDialogUtil.showProgressAlertDialog(mContext, "", mContext.getString(R.string.vf_download_init_download_message), true, false, null);
            mHandler.removeCallbacks(mDismissProgressTask);
            mHandler.postDelayed(mDismissProgressTask, 25 * 1000);
            isToShowDownloadButton = true;
            mListener.onDownloadContent(new FragmentCardDetailsDescription.DownloadStatusListener() {
                @Override
                public void onSuccess() {
                    if (((Activity) mContext).isFinishing()) {
                        return;
                    }
                    if (downloadbtnImage == null ) {
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
                            if (APIConstants.isHungamaContent(mData)) {
                            } else {
                                ErosNowDownloadManager.getInstance(mContext).initUnzipManagerListener(null);
                                FetchDownloadProgress.getInstance(mContext).removeProgressListener(CardDetailViewFactory.this);
                                FetchDownloadProgress.getInstance(mContext).addProgressListener(CardDetailViewFactory.this);
                                FetchDownloadProgress.getInstance(mContext).startPolling();
                            }
                            DownloadUtil.getDownloadDataFromDownloads(mData, new DownloadUtil.OnDataRetrieverListener() {
                                @Override
                                public void onDataLoaded(CardDownloadData data) {
                                    CleverTap.eventDownload(data, CleverTap.PROPERTY_DOWNLOAD_STARTED);
                                }
                            });
                        }
                    });

                }

                @Override
                public void onDownloadStarted() {
                    checkAndEnableDownload(mData != null
                            && mData.generalInfo != null
                            && mData.generalInfo.isDownloadable
                            && ApplicationController.ENABLE_DOWNLOADS);
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

    private LinearLayout mCommentContentLayout;
    private Button mLoadMore;

    private View createCommentsView() {

        View v = mInflater.inflate(R.layout.carddetailbriefcomment, null);
        mCommentContentLayout = (LinearLayout) v.findViewById(R.id.carddetailcomment_contentlayout);
        addSpace(mCommentContentLayout, (int) mContext.getResources().getDimension(R.dimen.margin_gap_16));
        fillCommentSectionData(mData);

        final Button editBox = (Button) v.findViewById(R.id.carddetailcomment_edittext);
        mLoadMore = (Button) v.findViewById(R.id.button_loadmore);
        editBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final String label = (String) editBox.getText();
                if (label.equalsIgnoreCase(mContext.getResources().getString(R.string.carddetailcommentsection_editcomment))) {
                    CommentDialog dialog = new CommentDialog(mContext);
                    dialog.showDialog(mData, new CommentDialog.MessagePostCallback() {

                        @Override
                        public void sendMessage(boolean status) {
                            if (status) {
//                                Util.showToast(mContext, mContext.getResources().getString(R.string.comment_success), Util.TOAST_TYPE_INFO);
                                AlertDialogUtil.showToastNotification(mContext.getResources()
                                        .getString(R.string.comment_success));
                                mStartIndexComment = 1;
                                refreshSection();
                            } else {
                                //remove this
//                                Util.showToast(mContext,mContext.getResources().getString(R.string.comment_post_fail), Util.TOAST_TYPE_ERROR);
                                AlertDialogUtil.showToastNotification(mContext.getResources()
                                        .getString(R.string.comment_post_fail));
                            }
                        }

                    });
                }
            }
        });

        mCommentSectionProgressBar = (ProgressBar) v.findViewById(R.id.carddetailcomment_progressBar);

        refreshSection();

        mLoadMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mStartIndexComment++;
                refreshSection();
                mLoadMore.setText(mContext.getString(R.string.progress_message));
                mLoadMore.setClickable(false);

            }
        });

        return v;
    }

    private void addSpace(ViewGroup v, int space) {
        Space gap = new Space(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, space);
        gap.setLayoutParams(params);
        v.addView(gap);
    }

    private int mStartIndexComment = 1;

    private void fillCommentSectionData(CardData card) {

        if (mStartIndexComment == 1)
            mCommentContentLayout.removeAllViews();

        if (card.comments == null || card.comments.values == null || card.comments.values.size() == 0) {
            setVisibility(mLoadMore, GONE);
            return;
        }

        if (mLoadMore != null) {
            mLoadMore.setText(mContext.getString(R.string.carddetailsectionheader_loadmore));
        }

        setVisibility(mLoadMore, card.comments.values.size() >= APIConstants.COUNT_COMMENTS_INT ? View
                .VISIBLE : GONE);

        for (CardDataCommentsItem commentsItem : card.comments.values) {
            View child = mInflater.inflate(R.layout.carddetailcomment_data, null);
            TextView personName = (TextView) child.findViewById(R.id.carddetailcomment_personname);
            commentsItem.name = "";
            if (commentsItem.name != null) {
                personName.setText(commentsItem.name);
            }
            TextView commentTime = (TextView) child.findViewById(R.id.carddetailcomment_time);
            commentTime.setText(Util.getFullUTCDateInString(commentsItem.timestamp));
            TextView commentMessage = (TextView) child.findViewById(R.id.carddetailcomment_comment);
            commentMessage.setText(commentsItem.comment);

            /*if (!TextUtils.isEmpty(commentsItem.name)) {
                Character firstChar = commentsItem.name.trim().charAt(0);
                if (Character.isLetter(firstChar)) {
                    ImageView imageView = (ImageView) child.findViewById(R.id.imageView1);
                    imageView.setImageResource(mContext.getResources().getIdentifier("" + Character.toLowerCase(firstChar), "drawable", mContext.getPackageName()));
                }

            }*/
            mCommentContentLayout.addView(child);

        }
    }


    private void refreshSection() {
        rotateRefresh();
        fetchCommentsData();
    }

    private void fetchCommentsData() {
        String contentId = null;
        if (mData != null
                && mData.generalInfo != null
                && mData.generalInfo.type != null) {
            if (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)
                    && mData.globalServiceId != null) {
                contentId = mData.globalServiceId;
            } else if (mData.generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_LIVE)
                    && mData._id != null) {
                contentId = mData._id;
            } else if (mData._id != null) {
                contentId = mData._id;
            }
        }
        Comments.Params commentsParams = new Comments.Params(contentId, APIConstants
                .FIELD_COMMENTS, 20, mStartIndexComment);
        Comments commentsRequest = new Comments(commentsParams, new APICallback<ValuesResponse<CardDataCommentsItem>>() {
            @Override
            public void onResponse(APIResponse<ValuesResponse<CardDataCommentsItem>> response) {
                stopRefresh();
                if (response == null || response.body() == null || response.body().results == null
                        && response.body().code != 200) {
                    mLoadMore.setVisibility(GONE);
                    return;
                }
                CardData cardData = new CardData();
                cardData.comments = new CardDataComments();
                cardData.comments.values = response.body().results.values;
                fillCommentSectionData(cardData);

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                stopRefresh();
                mLoadMore.setVisibility(GONE);
                if (t != null) {
                    //Log.d(TAG, "onFailure " + t.getMessage());
                }
            }
        });

        APIService.getInstance().execute(commentsRequest);
    }


    private ProgressBar mCommentSectionProgressBar;

    private void rotateRefresh() {
        if (mCommentSectionProgressBar == null) {
            return;
        }
        mCommentSectionProgressBar.setVisibility(View.VISIBLE);

    }

    private void stopRefresh() {
        if (mCommentSectionProgressBar == null) {
            return;
        }
        mCommentSectionProgressBar.setVisibility(GONE);

    }


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

    public void updateFavouriteButton() {
        if (mData == null || mFavouriteImgBtn == null) {
            return;
        }
        showFavaouriteButton(true);
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null))
            showFavaouriteButton(false);

        /*if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
            showFavaouriteButton(false);
        }*/
        String type = mData.generalInfo.type;
        String _id = mData._id;
        if (mTVShowData != null
                && mTVShowData.isVODYoutubeChannel()
                && mTVShowData.isVODChannel()
                && mTVShowData.isVODCategory()
                && mTVShowData.isTVSeries()) {
            type = mTVShowData.generalInfo.type;
            _id = mData.globalServiceId;
        }
        if (mData.isProgram()) {
            _id = mData.globalServiceId;
            type = APIConstants.TYPE_LIVE;
        }
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null))
            return;
        FavouriteCheckRequest.Params contentDetailsParams = new FavouriteCheckRequest.Params(_id,type);
        executeContentDetailRequest(contentDetailsParams);
        if (mData.currentUserData != null && mData.currentUserData.favorite) {
            mFavouriteImgBtn.setImageResource(R.drawable.description_added_to_watchlist_icon);
        } else {
            mFavouriteImgBtn.setImageResource(R.drawable.description_add_to_watchlist_icon);
        }
    }

    private void executeContentDetailRequest(FavouriteCheckRequest.Params contentDetailsParams) {

        final FavouriteCheckRequest contentDetails = new FavouriteCheckRequest(contentDetailsParams,
                new APICallback<FavouriteResponse>() {
                    String mAPIErrorMessage = null;

                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if(null == mListener){
                            onFailure(new Throwable(APIConstants.ERROR_CALLBACK_LISTENERS_NOT_REGD), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        if(response == null || response.body() == null){
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            showFavaouriteButton(true);
//                                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)) {
//                                    showFavaouriteButton(false);
//                                }
                            if (mData != null
                                    && mData.currentUserData != null) {
                                mData.currentUserData.favorite = response.body().favorite;
                            }

                            if (response.body().favorite) {
                                mFavouriteImgBtn.setImageResource(R.drawable.description_added_to_watchlist_icon);

                            } else {
                                mFavouriteImgBtn.setImageResource(R.drawable.description_add_to_watchlist_icon);
                                
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        String errorMessage = null;
                        String reason = t != null && (t.getMessage() != null) ? t.getMessage() : errorMessage;
                        if (!TextUtils.isEmpty(mAPIErrorMessage) && !"OK".equalsIgnoreCase(mAPIErrorMessage)) {
                            reason = mAPIErrorMessage;
                        }
                    }
                });

        APIService.getInstance().execute(contentDetails);

    }
    private void showFavaouriteButton(boolean b) {
        if (mData.isYoutube() || (mData.isTVEpisode() && mTVShowData == null)) {
            b = false;
        }
        if (b && ApplicationController.ENABLE_FAVOURITE) {
            mFavouriteImgBtn.setVisibility(View.VISIBLE);
            return;
        }
        mFavouriteImgBtn.setVisibility(GONE);

    }

    private List<String> getDummySeasons() {
        int size = 1;
        List<String> dummyList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            dummyList.add("Loading...");
        }
        return dummyList;
    }

    public void showPopupMenu(View view) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_window, null);
        mTodayEPGPOPUPWindow = new PopUpWindow(layout);
        mTodayEPGPOPUPWindow.attachPopupToView(view, new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                String date = null;
                if (mData.isProgram() || mData.isLive()) {
                    ArrayList<String> nxtDateList = Util.showNextDates();
                    date = nxtDateList.get(mEpgDatePosition);
                    setDateToCalender(date);
                } else {
                    if (mSeasonList != null) {
                        date = mSeasonList.get(selectedSeasonPosition);
                    }
                }
                if (mListener != null) {
                    mListener.onPopupItemSelected(date);
                }
            }
        });
        ListView mPopUpListView = (ListView) layout.findViewById(R.id.popup_listView);
        if (mSeasonList == null)
            mSeasonList = getDummySeasons();
        mPopupListAdapter = new DatesAdapter(mContext, DatesAdapter.AdapterType.SEASONS, mSeasonList);
        if (mData.isProgram() || mData.isLive()) {
            mPopupListAdapter = new DatesAdapter(mContext, Util.showNextDates());
        }
        mPopupListAdapter.setSelectedPosition(mEpgDatePosition);
        mPopUpListView.setAdapter(mPopupListAdapter);
        mPopUpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTodayEPGPOPUPWindow.dismissPopupWindow();
                selectedSeasonPosition = position;
                mEpgDatePosition = position;
                mPopupListAdapter.setSelectedPosition(position);
                mPopupListAdapter.notifyDataSetChanged();
                onPopupItemSelected();
            }
        });

    }

    public PopUpWindow getTodayEPGPOPUPWindow() {
        return mTodayEPGPOPUPWindow;
    }

    private PopUpWindow mTodayEPGPOPUPWindow;

    public Spannable setDateToCalender(String date) {
        Spannable cs = new SpannableString(date);
        if (date.contains("Today")) {
            cs.setSpan(new SuperscriptSpan(), 10, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 10, 12, 0);
        } else {
            cs.setSpan(new SuperscriptSpan(), 7, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            cs.setSpan(new RelativeSizeSpan(0.7f), 7, 9, 0);
        }
        if (mEPGTitleDate != null) {
            mEPGTitleDate.setText(cs);
        }
        return cs;
    }

    private boolean isLiveOrProgram() {
        if (mData == null
                || mData.generalInfo == null) {
            return false;
        }
        return (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(mData.generalInfo.type)
                || APIConstants.TYPE_LIVE.equalsIgnoreCase(mData.generalInfo.type));
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
                        if (downloadData.mPercentage ==100) {
                            if (APIConstants.isErosNowContent(downloadData)
                                    && downloadData.zipStatus < CardDownloadData.STATUS_FILE_UNZIPPED) {
                                if (downloadData.zipStatus == CardDownloadData.STATUS_FILE_NOT_ENOGH_SPACE
                                        || downloadData.zipStatus == CardDownloadData.STATUS_FAILED_TO_AQUIRE_DRM_LICENSE) {
                                    showDownloadFailedUI();
                                    return;
                                }
                                showDownloadingUI(99);
                                ErosNowDownloadManager.getInstance(mContext).addListener(mUnzipProgressListener);
                                return;
                            }
                            showDownloadCompleted();
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

    private CardDownloadData downloadData;
    private class DownloadManagerLister  {

        DownloadManagerLister() {

        }


    }




}
