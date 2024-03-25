package com.myplex.myplex.ui.fragment;

import static android.R.layout.simple_list_item_1;
import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.pedrovgs.LoggerD;
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
import com.myplex.model.CarouselInfoData;
import com.myplex.model.MenuDataModel;
import com.myplex.model.SearchConfigResponse;
import com.myplex.model.SearchFilterResponse;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.concurrentExecution.Callback;
import com.myplex.myplex.concurrentExecution.noThreads.MakeAndTrackMultipleCalls;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.LiveScoreWebView;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterCarouselInfo;
import com.myplex.myplex.ui.adapter.AdapterSearchFilter;
import com.myplex.myplex.ui.adapter.AdapterSearchSuggestions;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.CustomSwipeToRefresh;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SearchSuggestionsWithFilter extends BaseFragment {

    private static final String TAG = SearchSuggestionsWithFilter.class.getSimpleName();
    public static final String PARAM_SEARCH_QUERY = "search_query";
    public static final String PARAM_SEARCH_ALLOW_BROWSE_MORE = "search_browse_more";
    public static final String PARAM_SEARCH_CONTENT_TYPE = "search_content_type";
    public static final String PARAM_TAB_NAME="tab_name";
    private static final int PARAM_PAGE_COUNT = 10;
    private SearchListAdapterFilter mSearchListAdapter;
    private ListView mSearchSuggestionList;
    private TextView searchResultsText;
    private RelativeLayout headerLayout;
    private ImageView backIcon, closeIcon, voiceSearch;
    private ImageView profileIv;
    private TextView userName;
    private EditText searchTextBox;
    protected static final int SEARCH_SPEECH = 1357;
    // private TextView no_res_txt;
    private TextView errorText;


    private int mStartIndex = 1;
    private boolean mIsLoadingMorePages = false;
    private boolean mIsLoadingMoreAvailable = true;
    private String mQuery;
    private String mContentType;
    private String mTabName;
    private boolean isBrowsing;
    public RecyclerView rvFilterItems;
    public RecyclerView mTrendingSearchRecyclerView;
    public RecyclerView searchResultCarousel;
    private CustomSwipeToRefresh mSwipeToRefreshSearch;
    public RecyclerView mSuggestionsRecyclerView;
    RecyclerView.ItemDecoration verticalItemDecoration;
    private ProgressBar mSearchProgressBar;
    private List<CarouselInfoData> mTrendingList;
    AdapterSearchSuggestions adapterSearchSuggestions;
    List<CardData> textList;
    AdapterCarouselInfo carouselInfoData;
    private RelativeLayout mLayoutRetry;
    private ImageView mImageViewRetry;
    private TextView mTextViewErrorRetryAgain;

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loadSearchCarouselData();
            loadCarouselInfo();
            showRetryOption(false);
        }
    };

    private AdapterSearchFilter adapterSearchFilter;
    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0 &&
                    isBrowsing) {
                if (!mIsLoadingMorePages) {
                    if (!mIsLoadingMoreAvailable) {
                        return;
                    }
                    mIsLoadingMorePages = true;
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

    public void createString(String search) {
        String strNow = PrefUtils.getInstance().getString("PREF_SEARCH_STRING");
        Log.d("String", "" + strNow);
        if (strNow == null) {
            String searchString1 = search + ",";
            PrefUtils.getInstance().setString("PREF_SEARCH_STRING", searchString1);
            return;
        }
        String str = PrefUtils.getInstance().getString("PREF_SEARCH_STRING");
        String[] splitString = (PrefUtils.getInstance().getString("PREF_SEARCH_STRING").split(","));
        for (String string : splitString) {
            if (string.equals(search)) {
                return;
            }
        }
        int numberOfRecentSearchToShow;
        try {
            numberOfRecentSearchToShow = /*Integer.parseInt(*/PrefUtils.getInstance().getRecentSearchCountLimit();
        } catch (Exception e) {
            e.printStackTrace();
            numberOfRecentSearchToShow = 5;
        }
        if (getNumberOfCommas(PrefUtils.getInstance().getString("PREF_SEARCH_STRING")) < numberOfRecentSearchToShow) {
            str = str + search + ",";
            PrefUtils.getInstance().setString("PREF_SEARCH_STRING", str);
        } else {
            str = str + search + ",";
            str = deleteFirst(str);
            PrefUtils.getInstance().setString("PREF_SEARCH_STRING", str);
        }
    }

    private String deleteFirst(String str) {
        ArrayList<String> elephantList = new ArrayList<>(Arrays.asList(str.split(",")));
        elephantList.remove(0);
        str = android.text.TextUtils.join(",", elephantList);
        str = str + ",";
        return str;
    }

    public int getNumberOfCommas(String commaString) {
        int numberOfComma = 0;
        for (int i = 0; i < commaString.length(); i++) {
            if (commaString.charAt(i) == ',') numberOfComma++;
        }
        return numberOfComma;
    }
    private List<CarouselInfoData> mListCarouselInfo;

    private void loadSearchCarouselData() {
        showProgressBar();
        new MenuDataModel().fetchMenuList("searchResults", 1, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
                hideProgressBar();
                //updateData(dataList);
                mListCarouselInfo = dataList;
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                hideProgressBar();
              //  updateData(dataList);
                mListCarouselInfo = dataList;
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                hideProgressBar();
                if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
                    showRetryOption(true);
                }
                //Log.d(TAG, "onOnlineError: error- " + error.getMessage() + " errorCode- " + errorCode);
//                TODO Check if previous response data is available.
            }
        });

    }
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            hideSoftInputKeyBoard(searchTextBox);
            searchTextBox.setText("");
         /*   ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);*/
            CardData searchData = mSearchListAdapter.getItem(position);
            Analytics.gaInlineSearch(searchData.generalInfo.title.toLowerCase(), mSearchListAdapter.getCount());
            ComScoreAnalytics.getInstance().setInlineSearch(searchData,mTabName,mQuery,true);
            if (itemSelected != null)
                CleverTap.eventSearched(mContentType, mQuery, itemSelected.displayName, Analytics.YES);
            else
                CleverTap.eventSearched(mQuery, mContentType, Analytics.YES);
            ((MainActivity) mContext).onBackPressed();

            if (getActivity() != null
                    && !getActivity().isFinishing()
                    && searchData != null
                    && searchData.generalInfo != null
                    && APIConstants.TYPE_SPORTS.equalsIgnoreCase(searchData.generalInfo.type)
                    && !TextUtils.isEmpty(searchData.generalInfo.deepLink)) {
//                carouselData.generalInfo.deepLink = "http://www.sonyliv.com/details/live/5230736998001/Santhiya-Sehaj-Path-:-Giani-Jagtar-Singh-:-Epi-112";
                getActivity().startActivity(LiveScoreWebView.createIntent(getActivity(), searchData.generalInfo.deepLink, APIConstants.TYPE_SPORTS, searchData.generalInfo.title));
            } else if (searchData != null
                    && searchData.generalInfo != null) {
                createString(searchData.generalInfo.title);
                showCardDetailsFragment(searchData);
            }

        }

    };

    @Override
    public void onResume() {
        super.onResume();

        updateProfileImage();

        if(PrefUtils.getInstance().getPrefFullName() != null)
            userName.setText("Hello, "+PrefUtils.getInstance().getPrefFullName());
        else
            userName.setText("Hello, Sun Direct User");
    }

    public void updateProfileImage() {

        String url = PrefUtils.getInstance().getString("PROFILE_IMAGE_URL");
        if(TextUtils.isEmpty(url)) {
            url=PrefUtils.getInstance().getDefaultProfileImage();
            profileIv.setImageResource(R.drawable.nav_drawer_profile_thumbnail);
        }
        Glide.with(mContext.getApplicationContext())
                .asBitmap()
                .load(url)
                .placeholder(R.drawable.nav_drawer_profile_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .error(R.drawable.nav_drawer_profile_thumbnail)
                .dontAnimate()
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        profileIv.setImageBitmap(resource);
                    }
                });
        ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);
        ((MainActivity)requireActivity()).updateBottomBar(true, 1);
    }

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
       // searchData.globalServiceId = searchData._id;
        args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, searchData);
      /*  if (searchData != null
                && searchData.generalInfo != null
                && (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(searchData.generalInfo.type) || APIConstants.TYPE_LIVE.equalsIgnoreCase(searchData.generalInfo.type))) {
            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }*/
        if (mQuery != null)
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, mQuery);
        mBaseActivity.showDetailsFragment(args, searchData);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SearchSuggestionsWithFilter.PARAM_SEARCH_QUERY, mQuery);
        outState.putBoolean(SearchSuggestionsWithFilter.PARAM_SEARCH_ALLOW_BROWSE_MORE, mAllowBrowseMore);
        outState.putString(SearchSuggestionsWithFilter.PARAM_TAB_NAME,mTabName);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if(((MainActivity)requireActivity()).blurlayout_toolbar!=null) {
            ((MainActivity) requireActivity()).blurlayout_toolbar.setVisibility(GONE);
        }
        ((MainActivity)requireActivity()).updateBottomBar(true, 0);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mContext == null)
            return null;
        mBaseActivity = (BaseActivity) getActivity();
        ((MainActivity)requireActivity()).updateBottomBar(true, 0);
        ((MainActivity)requireActivity()).blurlayout_toolbar.setVisibility(GONE);
        View rootView = inflater.inflate(R.layout.searchsuggestions_filter, container, false);
//        Analytics.createScreenGA(Analytics.SCREEN_SEARCH);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_SEARCH);
        searchResultCarousel = (RecyclerView) rootView.findViewById(R.id.searchResultCarousel);
        mSwipeToRefreshSearch =  rootView.findViewById(R.id.swipe_to_refresh_search);
        mSuggestionsRecyclerView=rootView.findViewById(R.id.suggestions_recyclerview);
        headerLayout = (RelativeLayout)rootView.findViewById(R.id.header_layout);
        searchTextBox = (EditText) rootView.findViewById(R.id.searchTextBox);
        closeIcon = (ImageView) rootView.findViewById(R.id.closeIcon);
        backIcon = (ImageView) rootView.findViewById(R.id.backIcon);
        profileIv=rootView.findViewById(R.id.profile_iv);
        userName=rootView.findViewById(R.id.user_name);
        mTrendingSearchRecyclerView = (RecyclerView) rootView.findViewById(R.id.trendingSearchRecycler);
        mTrendingSearchRecyclerView.setNestedScrollingEnabled(false);
        mSuggestionsRecyclerView.setNestedScrollingEnabled(false);
        verticalItemDecoration = new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.carousel_gap));
        mSearchSuggestionList = (ListView) rootView.findViewById(R.id.suggestionlist);
        searchResultsText=rootView.findViewById(R.id.search_results_text);
        errorText = (TextView) rootView.findViewById(R.id.errorText);
        mSearchProgressBar = rootView.findViewById(R.id.searchProgress);
        mLayoutRetry = rootView.findViewById(R.id.retry_layout);
        mImageViewRetry = rootView.findViewById(R.id.imageview_error_retry);
        mTextViewErrorRetryAgain = rootView.findViewById(R.id.textview_error_retry);
        TextView hint_text = rootView.findViewById(R.id.hint_text);

        Typeface mBoldTypeFace = Typeface.createFromAsset(mContext.getAssets(), "font/amazon_ember_cd_bold.ttf");

        hint_text.setTypeface(mBoldTypeFace);
        searchTextBox.setTypeface(mBoldTypeFace);

        adapterSearchSuggestions=new AdapterSearchSuggestions();
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(mContext,RecyclerView.VERTICAL,false);
        mSuggestionsRecyclerView.setLayoutManager(linearLayoutManager);
        mSuggestionsRecyclerView.removeItemDecoration(verticalItemDecoration);
        mSuggestionsRecyclerView.addItemDecoration(verticalItemDecoration);
        mSuggestionsRecyclerView.setItemAnimator(null);
        mSuggestionsRecyclerView.setAdapter(adapterSearchSuggestions);

        carouselInfoData = new AdapterCarouselInfo(mContext, new ArrayList<>(), true);
//                dataList.get(0).layoutType=APIConstants.LAYOUT_TYPE_TEXT_FLOW_LAYOUT;
        LinearLayoutManager linearLayoutManager1 =new LinearLayoutManager(mContext,RecyclerView.VERTICAL,false);
        mTrendingSearchRecyclerView.setLayoutManager(linearLayoutManager1);
        mTrendingSearchRecyclerView.removeItemDecoration(verticalItemDecoration);
        mTrendingSearchRecyclerView.addItemDecoration(verticalItemDecoration);
        mTrendingSearchRecyclerView.setItemAnimator(null);
        mTrendingSearchRecyclerView.setAdapter(carouselInfoData);

        mLayoutRetry.setVisibility(View.GONE);
        mImageViewRetry.setOnClickListener(mRetryClickListener);
        mTextViewErrorRetryAgain.setOnClickListener(mRetryClickListener);

        errorText.setText(PrefUtils.getInstance().getSearchErrorMessage());
        errorText.setVisibility(GONE);
        mTrendingSearchRecyclerView.setVisibility(View.VISIBLE);
        //  trendingTextView.setVisibility(View.VISIBLE);
        mSearchListAdapter = new SearchListAdapterFilter(mContext, simple_list_item_1, android.R.id.text1, new ArrayList<CardData>());
        searchTextBox.addTextChangedListener(mTextWatcher);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInputKeyBoard(searchTextBox);
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setHomeTab();
                }
            }
        });
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearText();
            }
        });
        mSwipeToRefreshSearch.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                  searchTextBox.getText().clear();
                  loadCarouselInfo();
            }
        });

        mSuggestionsRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                    }
                    case MotionEvent.ACTION_UP: {
                        //do something with up swipe
                    }
                }

                return false;
            }
        });
        mTrendingSearchRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                    }
                    case MotionEvent.ACTION_UP: {
                        //do something with up swipe
                    }
                }

                return false;
            }
        });
        searchResultCarousel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                    }
                    case MotionEvent.ACTION_UP: {
                        //do something with up swipe
                    }
                }

                return false;
            }
        });





        voiceSearch = rootView.findViewById(R.id.voice_search);
        voiceSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftInputKeyBoard(view);
//                if (trendingFrameLayout != null && trendingFrameLayout.getVisibility() == View.VISIBLE) {
//                    trendingFrameLayout.setVisibility(View.GONE);
//                }
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice searching...");
                getActivity().startActivityForResult(intent, SEARCH_SPEECH);
            }
        });
        PrefUtils.getInstance().setAppLanguageToShow("");
        final ArrayList<String> list = new ArrayList<>();

        ArrayList<String> list1 = null;
        final int version = Build.VERSION.SDK_INT;

        // mSearchSuggestionList.setAdapter(mSearchListAdapter);
        mSearchSuggestionList.setOnScrollListener(mScrollListener);
        mSearchSuggestionList.setOnItemClickListener(mItemClickListener);
        Bundle args = savedInstanceState;
        if (args == null) {
            args = getArguments();
        }
        searchResultCarousel.setNestedScrollingEnabled(false);

//        rvFilterItems.setNestedScrollingEnabled(false);
        readBundleValues(args);
        //setQuery(mQuery, mContentType, mAllowBrowseMore,mTabName);
//        setFilterItems();
        loadSearchCarouselData();
        loadCarouselInfo();
        return rootView;
    }

    public void clearText(){
        if (searchTextBox != null && !TextUtils.isEmpty(searchTextBox.getText())) {
            closeIcon.setVisibility(View.GONE);
            searchTextBox.getText().clear();
            searchResultsText.setVisibility(GONE);
            mSearchSuggestionList.setVisibility(View.GONE);
            errorText.setVisibility(View.GONE);
            headerLayout.setVisibility(GONE);
            hideSoftInputKeyBoard(searchTextBox);
            searchResultCarousel.setVisibility(GONE);
            updateData(mTrendingList);

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //LogUtils.debug(TAG, "onActivityResult requestCode :" + requestCode + "resultcode :" + resultCode + "code :" + data);
        //check the requestCode as a case
        switch (requestCode) {
            case SEARCH_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    final ArrayList<String> text = data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    //Util.showAlertDialog(text.get(0));
                    String type = null;

                    //LogUtils.debug(TAG, "showSearchFragment: onClick type- " + text.get(0).toString());
                    //Addanalytics just record textchanges
                    if (text.get(0).length() != 0) {
                        searchTextBox.setText(text.get(0));
                        setQuery( text.get(0), mContentType, mAllowBrowseMore,mTabName);
                        //rootView.setAlpha(1);
                        /*if (!isFilterDataAvaiable) {
                            searchFilterLayout.setVisibility(View.GONE);
                            //hideSnackBar();
                        } else
                            searchFilterLayout.setVisibility(View.VISIBLE);*/
                    } else {
                        setQuery( text.get(0), mContentType, mAllowBrowseMore,mTabName);
                        //rootView.setAlpha(0.9f);
                        //searchFilterLayout.setVisibility(View.GONE);
                        //hideSnackBar();
                    }
                }
                break;
            }
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (closeIcon != null) {
                if (s.length() > 2) {
                    closeIcon.setVisibility(View.VISIBLE);
                 //   searchResultsText.setVisibility(View.VISIBLE);
                    mSearchSuggestionList.setVisibility(View.VISIBLE);
                    voiceSearch.setVisibility(GONE);
                } else {
                    errorText.setVisibility(GONE);
                    closeIcon.setVisibility(View.GONE);
                    voiceSearch.setVisibility(View.VISIBLE);
                    mTrendingSearchRecyclerView.setVisibility(View.VISIBLE);
                    searchResultsText.setVisibility(GONE);
                    mSearchSuggestionList.setVisibility(GONE);
                    searchResultCarousel.setVisibility(GONE);
                    updateData(mTrendingList);
                }
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
            // LogUtils.debug(TAG, "search suggetion afterTextChanged");
            if (TextUtils.getTrimmedLength(s) > 2) {
                setQuery(s.toString(), mContentType, mAllowBrowseMore, mTabName);

            }
            else
            {
                hideProgressBar();
                mQuery = "";
                if (inlineSearchRequest != null) {
                    inlineSearchRequest.cancel();
                    inlineSearchRequest = null;
                }
                searchResultsText.setVisibility(GONE);
                mSearchSuggestionList.setVisibility(GONE);
                searchResultCarousel.setVisibility(GONE);
            //    updateData(mTrendingList);
            }
        }

    };

    public void setListViewHeightBasedOnChildren(ListView list) {
        ListAdapter listAdapter = list.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, list);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = totalHeight + (list.getDividerHeight() * (listAdapter.getCount() - 1));
        list.setLayoutParams(params);
    }

    public SearchFilterResponse itemSelected;
    public SearchFilterResponse allSelected;
    List<SearchFilterResponse> filterItems;
    private void setFilterItems() {
        SearchConfigResponse searchConfigResponse = PropertiesHandler.getSearchConfigResponse(mContext);
        if (searchConfigResponse == null || searchConfigResponse.filter == null || searchConfigResponse.filter.isEmpty()) {
            //TODO: hid filter
            if (mContentType == null)
                mContentType = "";
            return;
        }
       filterItems = searchConfigResponse.filter;
        //TODO: to select first item by default
        filterItems.get(0).isChecked = true;
        //TODO: this block is to select the first item by defalut
        {
            itemSelected = filterItems.get(0);
            allSelected = filterItems.get(0);
            previousContentType = mContentType;
            mContentType = filterItems.get(0).key;
            for (int i = 1; i < filterItems.size(); i++) {
                filterItems.get(i).isChecked = false;
            }
        }
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false);
        rvFilterItems.setLayoutManager(layoutManager);
        rvFilterItems.addItemDecoration(new HorizontalItemDecorator(8));
        adapterSearchFilter = new AdapterSearchFilter(mContext,filterItems);
        adapterSearchFilter.setOnItemClickListener(new AdapterSearchFilter.OnItemClickListener() {
            @Override
            public void onClick(SearchFilterResponse item) {
                if (item != null) {
                    itemSelected = item;
                    previousContentType = mContentType;
                    mContentType = item.key;
                    if (previousContentType != null && !previousContentType.equalsIgnoreCase(mContentType)) {
                        mStartIndex = 1;
                        doInlineSearch();
                    }
                }
            }
        });
        rvFilterItems.setAdapter(adapterSearchFilter);
    }

    private void loadCarouselInfo() {
        showProgressBar();
        new MenuDataModel().fetchMenuList(mContext.getResources().getString(R.string.trending_search), 1, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
                hideProgressBar();
                mTrendingList = dataList;
                updateData(dataList);
                mSwipeToRefreshSearch.setRefreshing(false);
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {
                hideProgressBar();
                mTrendingList = dataList;
                updateData(dataList);
                mSwipeToRefreshSearch.setRefreshing(false);
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                hideProgressBar();
                mSwipeToRefreshSearch.setRefreshing(false);
                //Log.d(TAG, "onOnlineError: error- " + error.getMessage() + " errorCode- " + errorCode);
//                TODO Check if previous response data is available.
            }
        });

    }
    void hideSoftInputKeyBoard(View view) {
        if (view != null && mContext != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateData(List<CarouselInfoData> dataList){
        if (dataList != null) {
            Log.d("Trending",dataList.toString());
            if (errorText.getVisibility() == View.VISIBLE) {
                if (dataList.size() > 1) {
                    dataList.remove(1);
                    carouselInfoData.setCarouselInfoData(dataList);
                }else{
                    if(dataList.size() > 0){
                        carouselInfoData.setCarouselInfoData(dataList);
                    }
                }
            }else if(errorText.getVisibility()== View.GONE && dataList.size()==1){
                dataList.clear();
                loadCarouselInfo();
                carouselInfoData.setCarouselInfoData(dataList);
            }else{
                carouselInfoData.setCarouselInfoData(dataList);
            }
        }
    }

    public void trendingData(CarouselInfoData carouselInfoData) {
        //   AlertDialogUtil.showProgressAlertDialog(mContext);
        new MenuDataModel().fetchCarouseldata(mContext, carouselInfoData.name, 1, carouselInfoData.pageSize > 0 ? carouselInfoData.pageSize : APIConstants.PAGE_INDEX_COUNT, true, carouselInfoData.modified_on, new MenuDataModel.CarouselContentListCallback() {
            @Override
            public void onCacheResults(List<CardData> dataList) {
                bindDataToRecyclerView(dataList);
            }

            @Override
            public void onOnlineResults(List<CardData> dataList) {

                bindDataToRecyclerView(dataList);
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {
                LoggerD.debugLog(error.toString() + errorCode);
            }
        });

    }

    private void bindDataToRecyclerView(List<CardData> cardData) {
        //TODO code to bind
        Log.d("Trending Response:::", cardData.toString());
        if (cardData != null && cardData.size() > 0) {


        }

    }

    String previousContentType;
    private boolean isAnalyticsEventFiled = false;

    //@RequiresApi(api = Build.VERSION_CODES.O)
    public void setQuery(String searchString, String type, boolean allowBrowseBore,String tabName) {


        if (mContext == null || !isAdded()) {
            return;
        }
        LoggerD.debugLog("search query: mQuery, mAllowBrowseMore, mContentType- " + mQuery + ", " + mAllowBrowseMore + ", " + mContentType);
        mQuery = searchString;
        mTabName=tabName;


//        PrefUtils.getInstance().setLastSearchQuery(mQuery);
        Log.d("Stored Query:::", mQuery);


        if (itemSelected == null)
            mContentType = type;
        else
            mContentType = itemSelected.key;
        isBrowsing = allowBrowseBore;
        mStartIndex = 1;
        if (inlineSearchRequest != null) {
            inlineSearchRequest.cancel();
            inlineSearchRequest = null;
        }
      //   doInlineSearch();
        tryParallelExecution();
        if (!isAnalyticsEventFiled) {
            isAnalyticsEventFiled = true;
            Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_SEARCHED_FOR_CONTENT);
            AppsFlyerTracker.eventSearchQuery(searchString);
        }
        FirebaseAnalytics.getInstance().search(mQuery);

    }

    private void tryParallelExecution() {
        if(mQuery.isEmpty()) return;
        final MakeAndTrackMultipleCalls multipleCalls = new MakeAndTrackMultipleCalls(mListCarouselInfo,
                filterItems,
                mQuery.toLowerCase(),
                "",
                "",
                "",1300);
        multipleCalls.setOnCompleteListener(new Callback() {
            @Override
            public void onComplete(List<CarouselInfoData> mListCarouselInfoData) {
              //  updateCarouselInfo(mListCarouselInfoData);
                //Log.d(TAG, "searchResponse" + mListCarouselInfoData.toString());
                if (mListCarouselInfoData != null) {
                    Log.d("Trending",mListCarouselInfoData.toString());
                    if(mListCarouselInfoData.size() > 0){
                        // carouselInfoData = new AdapterPartnerHorizontalCarousel(mContext, mListCarouselInfoData.get(0).listCarouselData, searchResultCarousel);
                       // dataList.get(0).layoutType=APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM;
                        //AdapterCarouselInfo carouselInfoData = new AdapterCarouselInfo(mContext, mListCarouselInfoData, true);
                    /*    LinearLayoutManager linearLayoutManager=new LinearLayoutManager(mContext,RecyclerView.HORIZONTAL,false);
                        mTrendingSearchRecyclerView.setLayoutManager(linearLayoutManager);
                        mTrendingSearchRecyclerView.removeItemDecoration(verticalItemDecoration);
                        mTrendingSearchRecyclerView.addItemDecoration(verticalItemDecoration);
                        mTrendingSearchRecyclerView.addItemDecoration(new HorizontalItemDecorator(8));
                        mTrendingSearchRecyclerView.setItemAnimator(null);*/
                        if(!searchTextBox.getText().toString().isEmpty()) {
                           // mTrendingSearchRecyclerView.setAdapter(carouselInfoData);
                            carouselInfoData.setCarouselInfoData(mListCarouselInfoData);
                            //Fix for the issue, the search results are not showing when we enter complete name
                            mTrendingSearchRecyclerView.setAdapter(carouselInfoData);
                        } else{
                          //  updateData(mTrendingList);
                        }
                        mTrendingSearchRecyclerView.setVisibility(View.VISIBLE);
                        errorText.setVisibility(GONE);
                      //  mTrendingSearchRecyclerView.setVisibility(GONE);
                  /*      mSearchListAdapter = new SearchListAdapterFilter(mContext, android.R.layout.simple_list_item_2, android.R.id.text1, mListCarouselInfoData.get(0).listCarouselData);
                        mSearchSuggestionList.setAdapter(mSearchListAdapter);
                        mSearchSuggestionList.setOnScrollListener(mScrollListener);
                        setListViewHeightBasedOnChildren(mSearchSuggestionList);
                        mTrendingSearchRecyclerView.setVisibility(GONE);*/
                    } else {
                        errorText.setVisibility(View.VISIBLE);
                        updateData(mTrendingList);
                    }
                } else{
                    errorText.setVisibility(View.VISIBLE);
                    updateData(mTrendingList);
                }
            }

            @Override
            public void onFailed() {
             /*   if (searchStateListener != null) {
                    searchStateListener.onNillDataLoaded();
                }*/
                if(mQuery.isEmpty()) return;
                errorText.setVisibility(View.VISIBLE);
                updateData(mTrendingList);
                mTrendingSearchRecyclerView.setVisibility(View.VISIBLE);
                //Log.d(TAG, "searchResponse fail");
            }
        });
        multipleCalls.trackApiCalls();
    }


    private void readBundleValues(Bundle args) {

        if (args == null) {
            return;
        }
        if (args.containsKey(PARAM_SEARCH_QUERY)) {
            mQuery = args.getString(PARAM_SEARCH_QUERY);
            mAllowBrowseMore = args.getBoolean(PARAM_SEARCH_ALLOW_BROWSE_MORE);
            mTabName=args.getString(PARAM_TAB_NAME);
            mContentType = args.getString(PARAM_SEARCH_CONTENT_TYPE);
            LoggerD.debugLog("search query: mQuery, mAllowBrowseMore, mContentType- " + mQuery + ", " + mAllowBrowseMore + ", " + mContentType);
        }

    }

    public static SearchSuggestionsWithFilter newInsance(Bundle args) {
        SearchSuggestionsWithFilter searchSuggestions = new SearchSuggestionsWithFilter();
        searchSuggestions.setArguments(args);
        return searchSuggestions;
    }

    private void doInlineSearch() {
        String query = mQuery.trim();
        if (TextUtils.isEmpty(query)
                || mContentType == null || (PropertiesHandler.getSearchConfigResponse(mContext) != null && query.length() < PropertiesHandler.getSearchConfigResponse(mContext).characterLimit)) {

            return;
        }
        InlineSearch.Params inlineSearchParams = null;
        if (itemSelected != null && itemSelected.searchFields != null) {
            inlineSearchParams = new InlineSearch.Params(query, mContentType,
                    null, PARAM_PAGE_COUNT, mStartIndex, itemSelected.searchFields);

            Log.d("InLineSearch:::", inlineSearchParams.toString());

        } else {
            /*if (itemSelected != null && itemSelected.displayName != null && itemSelected.displayName.equalsIgnoreCase("Actors")) {
                inlineSearchParams = new InlineSearch.Params(query, mContentType,
                        null, PARAM_PAGE_COUNT, mStartIndex, "personsFullNames");
            } else if (itemSelected != null && itemSelected.displayName != null && itemSelected.displayName.equalsIgnoreCase("All")) {
                inlineSearchParams = new InlineSearch.Params(query, mContentType,
                        null, PARAM_PAGE_COUNT, mStartIndex, "personsFullNames,title");
            } else {*/
            inlineSearchParams = new InlineSearch.Params(query, mContentType,
                    null, PARAM_PAGE_COUNT, mStartIndex);
            //}
        }
        showProgressBar();
        inlineSearchRequest = new InlineSearch(inlineSearchParams, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                //Log.d(TAG, "searchResponse" + response);
                hideProgressBar();
            /*    if (getActivity() != null && getActivity() instanceof MainActivity && TextUtils.isEmpty(((MainActivity) getActivity()).getQuery())) {
                    clear();
                    return;
                }*/
                if (response == null
                        || response.body() == null
                        || response.body().results == null) {

                    errorText.setVisibility(View.VISIBLE);
                    updateData(mTrendingList);
                    //  trendingTextView.setVisibility(View.VISIBLE);
                    mTrendingSearchRecyclerView.setVisibility(View.VISIBLE);
                    //     no_res_txt.setVisibility(View.VISIBLE);
                    searchResultsText.setVisibility(GONE);
                    mSearchSuggestionList.setVisibility(View.GONE);
                    return;
                } else {
                    mTrendingSearchRecyclerView.setVisibility(GONE);
                }
                final CardResponseData minResultSet = response.body();
                if (minResultSet.results.size() == 0 && !mIsLoadingMorePages && mStartIndex == 1) {
                    errorText.setVisibility(View.VISIBLE);
                    updateData(mTrendingList);
                    searchResultsText.setVisibility(GONE);
                    mSearchSuggestionList.setVisibility(View.GONE);
                    if(allSelected != null && mContentType.equalsIgnoreCase(allSelected.key)){
                        headerLayout.setVisibility(GONE);
                        errorText.setText(PrefUtils.getInstance().getSearchErrorMessage());
                        mTrendingSearchRecyclerView.setVisibility(View.VISIBLE);
                    }else{
                        headerLayout.setVisibility(View.VISIBLE);
                        errorText.setText(R.string.no_content);
                        mTrendingSearchRecyclerView.setVisibility(GONE);
                    }
                    //no_res_txt.setVisibility(View.VISIBLE);

//                   trendingTextView.setVisibility(View.VISIBLE);


                    return;
                } else {
                    searchResultsText.setVisibility(View.VISIBLE);
                    mSearchSuggestionList.setVisibility(View.VISIBLE);
                    errorText.setVisibility(GONE);
                    mTrendingSearchRecyclerView.setVisibility(GONE);
                }
                headerLayout.setVisibility(View.VISIBLE);
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

                mSearchListAdapter = new SearchListAdapterFilter(mContext, android.R.layout.simple_list_item_2, android.R.id.text1, minResultSet.results);
                mSearchSuggestionList.setAdapter(mSearchListAdapter);
                mSearchSuggestionList.setOnScrollListener(mScrollListener);
                setListViewHeightBasedOnChildren(mSearchSuggestionList);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                hideProgressBar();
                if (getActivity() != null && getActivity() instanceof MainActivity && TextUtils.isEmpty(((MainActivity) getActivity()).getQuery())) {
                    clear();
                    return;
                }
                mIsLoadingMorePages = false;
                if (t != null) {
                    //Log.d(TAG, "" + t.getMessage());
                }
            }
        });
        APIService.getInstance().execute(inlineSearchRequest);
    }

    private InlineSearch inlineSearchRequest;

    @Override
    public boolean onBackClicked() {
        SDKLogger.debug("onBackClicked called");
        return super.onBackClicked();
    }

    public void clear() {
        hideProgressBar();
        if (mSearchSuggestionList != null) {
            mSearchSuggestionList.setOnScrollListener(null);
            headerLayout.setVisibility(View.GONE);
            errorText.setVisibility(GONE);
            mTrendingSearchRecyclerView.setVisibility(View.VISIBLE);
        }
        if (mSearchListAdapter != null) {
            mSearchListAdapter.clearResults();
            mSearchListAdapter.notifyDataSetChanged();
            mSearchSuggestionList.setOnScrollListener(null);
            searchResultsText.setVisibility(GONE);
            mSearchSuggestionList.setVisibility(GONE);
            errorText.setVisibility(GONE);
            headerLayout.setVisibility(GONE);
            mTrendingSearchRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void showRetryOption(boolean b) {
        if (b) {
            mLayoutRetry.setVisibility(View.VISIBLE);
            return;
        }
        mLayoutRetry.setVisibility(View.GONE);
    }

    private void showProgressBar(){
        if(mSearchProgressBar != null){
            mSearchProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private  void hideProgressBar(){
        if(mSearchProgressBar != null){
            mSearchProgressBar.setVisibility(GONE);
        }
    }
}


class SearchListAdapterFilter extends ArrayAdapter<CardData> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<CardData> mDataList;

    public SearchListAdapterFilter(Context context, int resource,
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
        if (mDataList == null) {
            mDataList = datalist;
            Log.d("MDataList", mDataList.toString());
            return;
        }
        mDataList.addAll(datalist);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null)
            v = mInflater.inflate(R.layout.searchresults_filter, null);
        CardData data = mDataList.get(position);
        v.setId(position);
        CardDataHolder dataHolder = (CardDataHolder) v.getTag();
        if (dataHolder == null) {
            dataHolder = new CardDataHolder();
            dataHolder.mTitle = (TextView) v.findViewById(R.id.title);
            dataHolder.mSubTitle=v.findViewById(R.id.sub_title_tv);

            dataHolder.mPreview = (ImageView) v.findViewById(R.id.thumbnailimage);
        }
        dataHolder.mDataObject = data;

        if (data.generalInfo != null && data.generalInfo.title != null) {
            dataHolder.mTitle.setText(data.generalInfo.title);
        }


        if (data!=null){
            if (data.isTVEpisode()){
                dataHolder.mSubTitle.setVisibility(View.VISIBLE);
                dataHolder.mSubTitle.setText("Episode "+data.content.serialNo);
            }
            else {
                dataHolder.mSubTitle.setVisibility(View.VISIBLE);
                if (data.content != null && data.content.genre.size() > 0) {
                    dataHolder.mSubTitle.setText(data.getGenre());
                }
            }
        }

        dataHolder.mPreview.setImageResource(R.drawable
                .black);
        String imageLink = getImageLink(data, dataHolder.mPreview);
        PicassoUtil.with(mContext).load(imageLink,dataHolder.mPreview,R.drawable.black);

        return v;
    }

    private String getImageLink(CardData mData, ImageView imageView) {

        String imageLink = null;
        if (mData == null) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,};
        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : mData.images.values) {
//                LoggerD.debugHooqVstbLog("imageItem: imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)) {
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)
                            || (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile))) {
                        if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)) {
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