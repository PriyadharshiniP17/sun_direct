package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.InlineSearch;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataHolder;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardResponseData;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.activities.ProgramGuideChannelActivity;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.List;


public class SearchSuggestions extends BaseFragment {

    private static final String TAG = SearchSuggestions.class.getSimpleName();
    public static final String PARAM_SEARCH_QUERY = "search_query";
    public static final String PARAM_SEARCH_ALLOW_BROWSE_MORE = "search_browse_more";
    public static final String PARAM_SEARCH_CONTENT_TYPE = "search_content_type";
    public static final String PARAM_TAB_NAME="tab_name";
    private static final int PARAM_PAGE_COUNT = 10;
    private SearchListAdapter mSearchListAdapter;
    private ListView mSearchSuggestionList;
    private  TextView no_res_txt;
    private int mStartIndex = 1;
    private ProgressBar mFooterPbBar;
    private boolean mIsLoadingMorePages = false;
    private boolean mIsLoadingMoreAvailable = true;
    private View mFooterView;
    private String mQuery;
    private String mContentType;
    private String mTabName;
    private boolean isBrowsing;
    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (totalItemCount != 0
                    && firstVisibleItem + visibleItemCount == totalItemCount + 1
                    && isBrowsing) {

                if (!mIsLoadingMorePages) {
                    if (!mIsLoadingMoreAvailable) {
                        return;
                    }
                    mIsLoadingMorePages = true;
                    if (mFooterPbBar != null) {
                        mFooterPbBar.setVisibility(View.VISIBLE);
                    }
                    mStartIndex++;
                    doInlineSearch();
                }
            }
        }
    };
    private boolean mAllowBrowseMore;

    private void launchPlayStore(String androidAppUrl) {
        Uri appStoreLink = Uri.parse(androidAppUrl);
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, appStoreLink));
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {

//            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            CardData searchData = mSearchListAdapter.getItem(position);
            Analytics.gaInlineSearch(searchData.generalInfo.title.toLowerCase(), mSearchListAdapter.getCount());
            CleverTap.eventSearched(mQuery, mContentType, Analytics.YES);
            ComScoreAnalytics.getInstance().setInlineSearch(searchData,mTabName,mQuery,true);
            ((MainActivity) mContext).onBackPressed();
            if (getActivity() != null
                    && !getActivity().isFinishing()
                    && searchData != null
                    && searchData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(searchData.generalInfo.type)
                    && !TextUtils.isEmpty(searchData.generalInfo.deepLink)) {
//                carouselData.generalInfo.deepLink = "http://www.sonyliv.com/details/live/5230736998001/Santhiya-Sehaj-Path-:-Giani-Jagtar-Singh-:-Epi-112";
                getActivity().startActivity(LiveScoreWebView.createIntent(getActivity(), searchData.generalInfo.deepLink,APIConstants.TYPE_SPORTS,searchData.generalInfo.title));
            } else if (searchData != null
                    && searchData.generalInfo != null) {
                    showCardDetailsFragment(searchData);
            }

        }

    };

    private void showCardDetailsFragment(CardData searchData) {
        CacheManager.setSelectedCardData(searchData);
        Bundle args = new Bundle();
        args.putString(CardDetails
                .PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
        args.putString(CardDetails
                .PARAM_CARD_ID, searchData._id);
        args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
        args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(searchData));
        if (searchData.generalInfo != null
                && searchData.generalInfo.type != null
                && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(searchData.generalInfo.type)
                || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(searchData.generalInfo.type)
                || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(searchData.generalInfo.type)
                || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(searchData.generalInfo.type)
                || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(searchData.generalInfo.type))) {
            //Log.d(TAG, "type: " + searchData.generalInfo.type + " title: " + searchData.generalInfo.title);
            args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, searchData);
        }
        args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_SEARCH);
        if (mQuery != null)
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mQuery);
        mBaseActivity.showDetailsFragment(args, searchData);
    }

    private boolean isSonyLiveContent(CardData data) {
        if(data == null
                || data.publishingHouse == null){
            return false;
        }

        if(APIConstants.TYPE_SONYLIV.equals(data.publishingHouse.publishingHouseName)){
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SearchSuggestions.PARAM_SEARCH_QUERY, mQuery);
        outState.putBoolean(SearchSuggestions.PARAM_SEARCH_ALLOW_BROWSE_MORE, mAllowBrowseMore);
        outState.putString(SearchSuggestions.PARAM_TAB_NAME,mTabName);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mContext == null)
            return null;
        mBaseActivity = (BaseActivity) getActivity();
        View rootView = inflater.inflate(R.layout.searchsuggestions, container, false);
//        Analytics.createScreenGA(Analytics.SCREEN_SEARCH);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_SEARCH);
        mSearchSuggestionList = (ListView) rootView.findViewById(R.id.suggestionlist);
        no_res_txt = (TextView)rootView.findViewById(R.id.no_search_res_txt);
        mSearchListAdapter = new SearchListAdapter(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<CardData>());
        mFooterView = getActivity().getLayoutInflater().inflate(R.layout.view_footer_layout,
                mSearchSuggestionList, false);
        mFooterPbBar = (ProgressBar) mFooterView.findViewById(R.id.footer_progressbar);
        final int version = Build.VERSION.SDK_INT;
        if (version < 21) {
            mFooterPbBar.setIndeterminate(false);
            mFooterPbBar.getIndeterminateDrawable().setColorFilter(SDKUtils.getColor(mContext,com.myplex.sdk.R.color.red_highlight_color), PorterDuff.Mode.MULTIPLY);
        }
        mFooterPbBar.setVisibility(View.GONE);
        mSearchSuggestionList.addFooterView(mFooterView);
       // mSearchSuggestionList.setAdapter(mSearchListAdapter);
        mSearchSuggestionList.setOnScrollListener(mScrollListener);
        mSearchSuggestionList.setOnItemClickListener(mItemClickListener);
        Bundle args = savedInstanceState;
        if(args == null){
            args = getArguments();
        }
        readBundleValues(args);
        setQuery(mQuery,mContentType,mAllowBrowseMore,mTabName);
        return rootView;
    }

    private boolean isAnalyticsEventFiled = false;

    public void setQuery(String searchString, String type, boolean allowBrowseBore,String tabName) {
        if(mContext == null || !isAdded()){
            return;
        }
        LoggerD.debugLog("search query: mQuery, mAllowBrowseMore, mContentType- " + mQuery+", "+mAllowBrowseMore+", "+mContentType);
        mIsLoadingMorePages = false;
        mIsLoadingMoreAvailable = true;
        mQuery = searchString;
        mContentType = type;
        mTabName=tabName;
        isBrowsing = allowBrowseBore;
        mStartIndex = 1;
        if (inlineSearchRequest != null) {
            inlineSearchRequest.cancel();
            inlineSearchRequest = null;
        }
        FirebaseAnalytics.getInstance().search(searchString);
        doInlineSearch();
        if (!isAnalyticsEventFiled) {
            isAnalyticsEventFiled = true;
            Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_SEARCHED_FOR_CONTENT);
            AppsFlyerTracker.eventSearchQuery(searchString);
        }
    }


    private void readBundleValues(Bundle args) {

        if (args == null) {
            return;
        }
        if (args.containsKey(PARAM_SEARCH_QUERY)) {
            mQuery = args.getString(PARAM_SEARCH_QUERY);
            mAllowBrowseMore = args.getBoolean(PARAM_SEARCH_ALLOW_BROWSE_MORE);
            mContentType = args.getString(PARAM_SEARCH_CONTENT_TYPE);
            mTabName=args.getString(PARAM_TAB_NAME);
            LoggerD.debugLog("search query: mQuery, mAllowBrowseMore, mContentType- " + mQuery+", "+mAllowBrowseMore+", "+mContentType);
        }

    }

    public static SearchSuggestions newInsance(Bundle args) {
        SearchSuggestions searchSuggestions = new SearchSuggestions();
        searchSuggestions.setArguments(args);
        return searchSuggestions;
    }

    private void doInlineSearch() {

        String query = mQuery.trim();
        if(TextUtils.isEmpty(query)
            ||TextUtils.isEmpty(mContentType)){
            return;
        }

        InlineSearch.Params inlineSearchParams = new InlineSearch.Params(query,mContentType,
                null,PARAM_PAGE_COUNT,mStartIndex);
        inlineSearchRequest = new InlineSearch(inlineSearchParams, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {

             /*   if (getActivity() != null && getActivity() instanceof MainActivity && TextUtils.isEmpty(((MainActivity) getActivity()).getQuery())) {
                    clear();
                    return;
                }*/
                if(response == null
                        || response.body() == null
                        || response.body().results == null) {
                    no_res_txt.setVisibility(View.VISIBLE);
                    mSearchSuggestionList.setVisibility(View.GONE);
                    return;
                }
                Gson gson=new Gson();
                Log.d("SearchResult","SearchSuggesstion"  + gson.toJson(response.body().results));

                final CardResponseData minResultSet = response.body();
                if (mFooterPbBar != null) {
                    mFooterPbBar.setVisibility(View.GONE);
                }
                if(minResultSet.results.size() == 0 && !mIsLoadingMorePages){
                    no_res_txt.setVisibility(View.VISIBLE);
                    mSearchSuggestionList.setVisibility(View.GONE);
                    return;
                }else {
                    mSearchSuggestionList.setVisibility(View.VISIBLE);
                    no_res_txt.setVisibility(View.GONE);
                }
                if (minResultSet.results.size() < PARAM_PAGE_COUNT) {
                    mIsLoadingMoreAvailable = false;
                }
                if (mIsLoadingMorePages) {
                    mIsLoadingMorePages = false;
                    if (mSearchListAdapter != null) {
                        mSearchSuggestionList.setOnScrollListener(mScrollListener);
                       mSearchListAdapter.addData(minResultSet.results);
                    }
                    return;
                }

                mSearchListAdapter = new SearchListAdapter(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, minResultSet.results);
                mSearchSuggestionList.setAdapter(mSearchListAdapter);
                mSearchSuggestionList.setOnScrollListener(mScrollListener);

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                if (getActivity() != null && getActivity() instanceof MainActivity && TextUtils.isEmpty(((MainActivity) getActivity()).getQuery())) {
                    clear();
                    return;
                }
                mIsLoadingMorePages = false;
                if(t != null){
                    //Log.d(TAG, "" + t.getMessage());
                }
            }
        });
        APIService.getInstance().execute(inlineSearchRequest);
    }

    private InlineSearch inlineSearchRequest;

    private void showEpgFragment(CardData channelData){

        if (channelData == null || channelData._id == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putSerializable(ProgramGuideChannelActivity.PARAM_CHANNEL_DATA,channelData);
        FragmentChannelEpg fragmentChannelEpg = FragmentChannelEpg.newInstance(args);
        ((MainActivity) mContext).pushFragment(fragmentChannelEpg);
    }

    @Override
    public boolean onBackClicked() {
        return super.onBackClicked();
    }

    private void showRelatedVODListFragment(CardData cardData) {
        Bundle args = new Bundle();
        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
        args.putString(FragmentRelatedVODList.PARAM_SEARCH_QUERY, mQuery);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));
    }

    public void clear() {
        if (mSearchSuggestionList != null) {
            mSearchSuggestionList.setOnScrollListener(null);
        }
        if (mSearchListAdapter != null) {
            mSearchListAdapter.clearResults();
            mSearchListAdapter.notifyDataSetChanged();
        }
    }
}

class SearchListAdapter extends ArrayAdapter<CardData> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<CardData> mDataList;

    public SearchListAdapter(Context context, int resource,
                             int textViewResourceId, List<CardData> objects) {
        super(context, resource, textViewResourceId, objects);
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mDataList = objects;
    }

    public void addData(List<CardData> datalist) {
        if (datalist == null) {
            return;
        }
        if(mDataList == null){
            mDataList = datalist;
            return;
        }
        mDataList.addAll(datalist);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null)
            v = mInflater.inflate(R.layout.searchresults, null);
        CardData data = mDataList.get(position);
        v.setId(position);
        CardDataHolder dataHolder = (CardDataHolder) v.getTag();
        if (dataHolder == null) {
            dataHolder = new CardDataHolder();
            dataHolder.mTitle = (TextView) v.findViewById(R.id.title);

            dataHolder.mPreview = (ImageView) v.findViewById(R.id.thumbnailimage);
        }
        dataHolder.mDataObject = data;

        if (data.generalInfo != null && data.generalInfo.title != null) {
            dataHolder.mTitle.setText(data.generalInfo.title);
        }

        dataHolder.mPreview.setImageResource(R.drawable
                .black);
        String imageLink = getImageLink(data,dataHolder.mPreview);
        PicassoUtil.with(mContext).load(imageLink,dataHolder.mPreview,R.drawable
                .black);

        return v;
    }
    private String getImageLink(CardData mData, ImageView imageView) {

        String imageLink = null;
        if(mData == null){
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,};
        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : mData.images.values) {
//                LoggerD.debugHooqVstbLog("imageItem: imageType- " + imageItem.type);
                if(imageType.equalsIgnoreCase(imageItem.type)){
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)
                            || (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile))) {
                        if(APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)){
//                            mPreviewImage.setScaleType(null);
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            imageView.requestLayout();
                            imageView.invalidate();
                            return imageItem.link;
//                        mPreviewImage.setScaleType(Scale);
//                        android:scaleType="centerCrop"
                        }
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.invalidate();

//                    LoggerD.debugHooqVstbLog("imageType- " + imageType + " imageItem: imageType- " + imageItem.type);
                        return imageItem.link;
                    }
                }
            }
        }
        return imageLink;
    }


    public void clearResults() {
        if (mDataList == null) return;
        mDataList.clear();
    }
}
