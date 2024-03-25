package com.myplex.myplex.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.SeasonData;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.events.MessageEvent;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterRelatedVODListForDownloads;
import com.myplex.myplex.ui.adapter.DatesAdapter;
import com.myplex.myplex.ui.views.PopUpWindow;
import com.myplex.myplex.utils.PicassoUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.myplex.myplex.ApplicationController.getApplicationConfig;

/**
 * Created by Apalya on 15-Dec-15.
 */
public class FragmentRelatedVODListForDownloads extends BaseFragment{

    private static final String TAG = FragmentRelatedVODListForDownloads.class.getSimpleName();
    public static final String PARAM_BG_SECTION_COLOR = "bg_section_color";
    public static final String PARAM_BG_SECTION_LOGO_URL = "logo_url";
    static final String PARAM_DOWNLOAD_DATA = "download_data";
    private ListView mListViewRelatedVOD;
    private TextView mToolbarTitle;
    private TextView mTextViewErroFetch;
    private ImageView mHeaderImageView;
    private ImageView channelImageView;
    private AdapterRelatedVODListForDownloads mAdapterRelatedVODList;
    private CardData mRelatedVODData;
    private View mFooterView;
    private ProgressBar mFooterPbBar;
    private View rootView;
    private LayoutInflater mInflater;
    private Toolbar mToolbar;
    private View mCustomToolBarLayout;

    private View.OnClickListener mOnClickCloseAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            getActivity().finish();
            //showOverFlowSettings(v);
            mBaseActivity.removeFragment(FragmentRelatedVODListForDownloads.this);
        }
    };
    private ProgressDialog mProgressDialog;
    private String mContentType;

    private String mToolbarIconUrl;
    private String mToolbarBGColor;
    private boolean mIsFromViewAll;
    private TextView mTextViewSeason;
    private RelativeLayout mLayoutTVSeasons;
    private List<CardData> mListSeasons;
    private PopUpWindow mPopUpWindowSeasons;
    private int mSelectedSeasonPosition;
    private List<String> mListSeasonNames;
    private ImageView mImageButtonAllSeasons;
    private CardDownloadData mCardDownloadData;
    private AdapterRelatedVODListForDownloads.FragmentCloseListenerListener mFragmentCloseListenerListener = new AdapterRelatedVODListForDownloads.FragmentCloseListenerListener() {
        @Override
        public void onCloseFragment() {
            if (mContext == null || ((MainActivity) mContext).isFinishing()) {
                return;
            }
            EventBus.getDefault().post(new MessageEvent());
//            ScopedBus.getInstance().post(new ContentDownloadEvent(null));
            ((MainActivity) mContext).removeFragment(FragmentRelatedVODListForDownloads.this);
        }

        @Override
        public void updateSeasonData() {
            if (mCardDownloadData == null
                    || mCardDownloadData.tvSeasonsList == null){
                return;
            }
            for (int i = 0; i < mCardDownloadData.tvSeasonsList.size(); i++) {
                if (mCardDownloadData.tvSeasonsList.get(i).tvEpisodesList != null
                        && !mCardDownloadData.tvSeasonsList.get(i).tvEpisodesList.isEmpty()) {
                    mSelectedSeasonPosition = i;
                    prepareSeasonNames();
                    updateSeasonsData();
                    return;
                }
            }
        }
    };

    public static FragmentRelatedVODListForDownloads newInstance(Bundle args) {
        FragmentRelatedVODListForDownloads fragmentRelatedVODList = new FragmentRelatedVODListForDownloads();
        fragmentRelatedVODList.setArguments(args);
        return fragmentRelatedVODList;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();
        mBaseActivity = (BaseActivity) getActivity();
        readBundleData();
        mInflater = LayoutInflater.from(mContext);
        rootView = mInflater.inflate(R.layout.fragment_related_vodlist, container, false);

        rootView.findViewById(R.id.divider_view).setVisibility(View.VISIBLE);
        mListViewRelatedVOD = (ListView) rootView.findViewById(R.id.listview_related_vods);
        mTextViewErroFetch = (TextView) rootView.findViewById(R.id.error_message);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0,0);

        //Set up Toolbar title
        mCustomToolBarLayout = mInflater.inflate(R.layout.custom_toolbar, null, false);
        mToolbarTitle = (TextView) mCustomToolBarLayout.findViewById(R.id.toolbar_header_title);
        mHeaderImageView = (ImageView) mCustomToolBarLayout.findViewById(R.id.toolbar_settings_button);
        channelImageView = (ImageView) mCustomToolBarLayout.findViewById(R.id.toolbar_tv_channel_Img);
        mToolbar.addView(mCustomToolBarLayout);

        mLayoutTVSeasons = (RelativeLayout) rootView.findViewById(R.id.layout_season_drop_down);
        mTextViewSeason = (TextView) rootView.findViewById(R.id.header_title_text);
        mImageButtonAllSeasons = (ImageView) rootView.findViewById(R.id.drop_down_button);
        initUI();
        if(mCardDownloadData != null
                && mCardDownloadData.tvSeasonsList != null
                && !mCardDownloadData.tvSeasonsList.isEmpty()){
            prepareSeasonsUI();
            prepareSeasonsPopup();
            prepareSeasonNames();
        }
        updateSeasonsData();
        return rootView;
    }

    private void readBundleData() {
        Bundle args = getArguments();

        //Log.d(TAG,"onCreateView()");
        if (args != null) {
            mCardDownloadData = null;
            if(args.containsKey(PARAM_DOWNLOAD_DATA)){
                mCardDownloadData = (CardDownloadData) args.getSerializable(PARAM_DOWNLOAD_DATA);
            }

            if (args.containsKey(FragmentRelatedVODListForDownloads.PARAM_BG_SECTION_LOGO_URL)) {
                mToolbarIconUrl = args.getString(FragmentRelatedVODListForDownloads.PARAM_BG_SECTION_LOGO_URL);
            }
            if (args.containsKey(FragmentRelatedVODListForDownloads.PARAM_BG_SECTION_COLOR)) {
                mToolbarBGColor = args.getString(FragmentRelatedVODListForDownloads.PARAM_BG_SECTION_COLOR);
            }

            if(TextUtils.isEmpty(mToolbarIconUrl) || TextUtils.isEmpty(mToolbarBGColor)){
                if (mRelatedVODData != null
                        && mRelatedVODData.publishingHouse != null
                        && mRelatedVODData.publishingHouse.publishingHouseName != null
                        && mRelatedVODData.publishingHouse.publishingHouseName.equalsIgnoreCase(APIConstants.TYPE_HOOQ)) {
                    mToolbarIconUrl = PrefUtils.getInstance().getPrefHooqLogoImageUrl();
                    mToolbarBGColor = PrefUtils.getInstance().getPrefHooqBgsectionColor();
                }
            }

        }

    }

    private DatesAdapter mPopupListAdapter;
    private ListView mPopUpListView;

    private void showSeasonsPopUpWindow(View view){
        mPopUpWindowSeasons.attachPopupToView(view, new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
    }

    private List<String> getDummySeasons() {
        int size = 1;
        List<String> dummyList = new ArrayList<>(size);
        for (int i = 0; i < size; i++){
            dummyList.add("Loading...");
        }
        return dummyList;
    }

    private void prepareEpisodes() {
        if(mCardDownloadData == null){
            return;
        }
        fillData(mCardDownloadData);
        //Update selected season text on drop down header
        updateDropDownTitle();
    }

    private void prepareSeasonsPopup() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_window, null);
        mPopUpWindowSeasons = new PopUpWindow(layout);
        // TODO Add dummy data for seasons
//        mPopupListAdapter = new DatesAdapter(mContext,new ArrayList<String>(0));
        mPopupListAdapter = new DatesAdapter(mContext,DatesAdapter.AdapterType.SEASONS, getDummySeasons());
        mPopUpListView = (ListView) layout.findViewById(R.id.popup_listView);
        mPopUpListView.setAdapter(mPopupListAdapter);
        mPopUpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPopUpWindowSeasons.dismissPopupWindow();
                mSelectedSeasonPosition = position;
                prepareEpisodes();
            }
        });
    }

    private void prepareSeasonsUI() {
        mLayoutTVSeasons.setVisibility(View.VISIBLE);
        mTextViewSeason.setVisibility(View.VISIBLE);
        mLayoutTVSeasons.setOnClickListener(mTodayEPGListener);
        mTextViewSeason.setOnClickListener(mTodayEPGListener);
        mImageButtonAllSeasons.setOnClickListener(mTodayEPGListener);
    }

    private void updateSeasonsData() {
        if ((mListSeasonNames != null
                && mListSeasonNames.isEmpty())
                || mPopUpListView == null) {
            prepareEpisodes();
            return;
        }
        mPopupListAdapter = new DatesAdapter(mContext, DatesAdapter.AdapterType.SEASONS, mListSeasonNames);
        mPopUpListView.setAdapter(mPopupListAdapter);
        prepareEpisodes();
    }

    private void prepareSeasonNames() {
        mListSeasonNames = new ArrayList<>();
        for (SeasonData seasonData : mCardDownloadData.tvSeasonsList) {
            mListSeasonNames.add(seasonData.seasonName);
        }
    }

    private final View.OnClickListener mTodayEPGListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPopUpWindowSeasons != null
                    && mPopUpWindowSeasons.isPopupVisible()) {
                mPopUpWindowSeasons.dismissPopupWindow();
            } else {
                showSeasonsPopUpWindow(v);
            }
        }
    };

    private String getImageLink(List<CardDataImagesItem> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_ICON};
        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : images) {
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && ApplicationConfig.XXHDPI.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }

    private void updateDropDownTitle() {
        if (mTextViewSeason != null
                && mListSeasonNames != null
                && !mListSeasonNames.isEmpty()) {
            mTextViewSeason.setText(mListSeasonNames.get(mSelectedSeasonPosition));
        }
    }


    private void initUI() {

        updateChannelImage();
        boolean showProgress = true;
        mAdapterRelatedVODList = new AdapterRelatedVODListForDownloads(mContext,showProgress, mCardDownloadData, mSelectedSeasonPosition, mFragmentCloseListenerListener);
        mFooterView = mInflater.inflate(R.layout.view_footer_layout, mListViewRelatedVOD, false);
        mFooterPbBar = (ProgressBar) mFooterView.findViewById(R.id.footer_progressbar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mFooterPbBar.setIndeterminate(false);
            mFooterPbBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext,R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
        }
        mFooterPbBar.setVisibility(View.GONE);
        mListViewRelatedVOD.addFooterView(mFooterView);
        mListViewRelatedVOD.setAdapter(mAdapterRelatedVODList);
        mHeaderImageView.setOnClickListener(mOnClickCloseAction);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void updateChannelImage() {
        if (!TextUtils.isEmpty(mToolbarIconUrl)) {
            PicassoUtil.with(mContext)
                    .load(mToolbarIconUrl,channelImageView);

            if (mToolbarIconUrl.contains(APIConstants.PARAM_SCALE_WRAP)) {
                channelImageView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
            } else {
                channelImageView.getLayoutParams().width = (int) mContext.getResources().getDimension(R.dimen.margin_gap_42);
                channelImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
            }
        } else {
            channelImageView.setImageResource(R.drawable.app_icon);
        }

        if (!TextUtils.isEmpty(mToolbarBGColor)) {
            try {
                mCustomToolBarLayout.setBackgroundColor(Color.parseColor(mToolbarBGColor));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        if (mCardDownloadData != null
                && mCardDownloadData.title != null) {
            mToolbarTitle.setText(mCardDownloadData.title);
        }

    }

    private List<CardData> getDummyVODList() {

        List dummyList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dummyList.add(new CardData());
        }
        return dummyList;
    }

    private void fillData(CardDownloadData vodListData) {
        mListViewRelatedVOD.setVisibility(View.VISIBLE);
        mTextViewErroFetch.setVisibility(View.GONE);
        boolean showProgress = true;
        mAdapterRelatedVODList = new AdapterRelatedVODListForDownloads(mContext,showProgress, mCardDownloadData, mSelectedSeasonPosition, mFragmentCloseListenerListener);
        mListViewRelatedVOD.setAdapter(mAdapterRelatedVODList);
        mAdapterRelatedVODList.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        SDKUtils.saveObject(ApplicationController.getDownloadData(), getApplicationConfig().downloadCardsPath);
    }

    private void showNoDataMessage() {
        if (mTextViewErroFetch != null) {
            if(mTextViewErroFetch != null){
                if(mRelatedVODData != null && mRelatedVODData.generalInfo != null && !APIConstants.TYPE_VODCHANNEL.equals(mRelatedVODData.generalInfo.type)){
                    mTextViewErroFetch.setText("There are no downloads available right now");
                }
                if (mListViewRelatedVOD != null)
                    mListViewRelatedVOD.setVisibility(View.GONE);
                mTextViewErroFetch.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onBackClicked() {
        return super.onBackClicked();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG, "onConfigurationChanged(): " + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Log.d(TAG, "dismiss pop up window" );
            if (mPopUpWindowSeasons != null) {
                mPopUpWindowSeasons.dismissPopupWindow();
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
