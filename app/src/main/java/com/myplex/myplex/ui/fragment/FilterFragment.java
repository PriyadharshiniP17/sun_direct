package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.FilterRequest;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.FilterItem;
import com.myplex.model.GenreFilterData;
import com.myplex.model.GenresData;
import com.myplex.model.Languages;
import com.myplex.model.Terms;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.CustomPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.myplex.myplex.ui.activities.MainActivity.SECTION_MOVIES;

/**
 * Created by vijay das on 07-04-2018.
 */

public class FilterFragment extends BaseFragment {


    private static final String PARAM_CONTENT_TYPE = "TYPE";
    private CarouselInfoData mCarouselInfoData;
    private List<FilterItem> mFilterLanguages;
    private List<FilterItem> mFilterGeners;
    private Button btFilter;
    private TextView tvReset;
    TabLayout tabLayout;
    ViewPager viewPager;
    CustomPagerAdapter adapter;
    int mSectionType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.popup_window_filters, null);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        btFilter = (Button) view.findViewById(R.id.btApply);
        tvReset = (TextView) view.findViewById(R.id.tvReset);
//        Analytics.createScreenGA(Analytics.SCREEN_FILTER);
        FirebaseAnalytics.getInstance().createScreenFA((Activity)mContext,Analytics.SCREEN_FILTER);
        readBundleValues();
        fetchFilterData();

        return view;


    }
    public FilterFragment newInstance(CarouselInfoData carouselInfoData, int mSectionType) {

        LoggerD.debugDownload("newInstance");
        FilterFragment mFilterFragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putInt(FilterFragment.PARAM_CONTENT_TYPE,mSectionType);
        CacheManager.setCarouselInfoData(carouselInfoData);

        mFilterFragment.setArguments(args);

        return mFilterFragment;
    }

    private void readBundleValues() {
        Bundle args = getArguments();

        this.mCarouselInfoData = CacheManager.getCarouselInfoData();
        this.mSectionType = args.getInt(PARAM_CONTENT_TYPE);
        args.clear();
    }

    private void fetchFilterData() {
        String contentType = APIConstants.TYPE_LIVE;
        CarouselInfoData carouselInfoData = mCarouselInfoData;
        if (carouselInfoData != null && !TextUtils.isEmpty(carouselInfoData.shortDesc)) {
            contentType = carouselInfoData.shortDesc;
        }
        if (carouselInfoData != null && carouselInfoData.cachedFilterResponse != null) {
            parseFilterResponseData(carouselInfoData.cachedFilterResponse);
            return;
        }
        FilterRequest.Params requestParams = new FilterRequest.Params(contentType);
        final FilterRequest request = new FilterRequest(requestParams, new APICallback<GenreFilterData>() {
            @Override
            public void onResponse(APIResponse<GenreFilterData> response) {
                if (null == response.body() || response.body().results == null) {
                    closeFilterMenuPopup();
                    return;
                }

                try {
                    CarouselInfoData carouselInfoData = mCarouselInfoData;
                    carouselInfoData.cachedFilterResponse = response.body();
                    parseFilterResponseData(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                closeFilterMenuPopup();
            }
        });

        APIService.getInstance().execute(request);
    }

    private void closeFilterMenuPopup() {
        if (getActivity() == null || getActivity().isFinishing()) return;
        ((MainActivity)getActivity()).removeFragment(this);
        if (adapter != null && !adapter.isFiltersAvailable()) {
            getActivity().onBackPressed();
        }
    }



    private void parseFilterResponseData(GenreFilterData body) {
        List<FilterItem> groupGenres = new ArrayList<>();
        List<FilterItem> groupLanguages = new ArrayList<>();

        boolean isFilterAvailable = checkIsFilterSelected(body, groupLanguages, groupGenres);
//
//        ((MainActivity) getActivity()).setFilterIcon(R.drawable.actionbar_filter_icon_default);
//        if (isFilterAvailable)

        //TODO: change here
        if (groupLanguages == null || groupGenres == null) {
            Log.e("lang or gener", "null");
            return;
        }
        if(isAdded())
            setData(groupLanguages,groupGenres);
    }



    public void setData(final List<FilterItem> mFilterLanguages, final List<FilterItem> mFilterGeners) {
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        this.mFilterLanguages = mFilterLanguages;
        this.mFilterGeners = mFilterGeners;
        //TODO: hide loading textview
        adapter = new CustomPagerAdapter(mContext, mFilterLanguages, mFilterGeners, new CustomPagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(HashMap<Integer, ArrayList<String>> filterMap) {
//                closeFilterMenuPopup();
                if (getActivity() != null && getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).updateFilterData(filterMap);
            }
        });
        //TODO Added extra
        adapter.setFilterSectionType(mSectionType);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        adapter.notifyDataSetChanged();
        btFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null && adapter instanceof CustomPagerAdapter) {
                    closeFilterMenuPopup();
                    adapter.filterOnClickApply();
                    Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_FILTERED_BY_CATEGORY);
                }
            }
        });
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null && adapter instanceof CustomPagerAdapter) {
                    adapter.reset();
                    mCarouselInfoData.filteredData = null;
                }
            }
        });

    }



    public void updateFilterData(HashMap<Integer, ArrayList<String>> filterValuesMap) {
        ArrayList<String> languageList = new ArrayList<>();
        ArrayList<String> genreFilterList = new ArrayList<>();
        int genreKey = 0;
        int languageKey = 1;
        if (mSectionType == SECTION_MOVIES) {
            genreKey = 1;
            languageKey = 0;
        }
        if (filterValuesMap != null && filterValuesMap.containsKey(genreKey)) {
            genreFilterList = filterValuesMap.get(genreKey);
        }
        if (filterValuesMap != null && filterValuesMap.containsKey(languageKey)) {
            languageList = filterValuesMap.get(languageKey);
        }
        if (filterValuesMap == null) filterValuesMap = new HashMap<>();

        CarouselInfoData carouselInfoData = mCarouselInfoData;
        carouselInfoData.filteredData = filterValuesMap;
            /*if(genreFilterList.size()== 0 && languageList.size() == 0){
                closeFilterMenuPopup();
                return;
            }*/
        if (genreFilterList.size() > 0 && genreFilterList.get(0).equals("All")) {
            genreFilterList = new ArrayList<>();
        }
        if (languageList.size() > 0 && languageList.get(0).equals("All")) {
            languageList = new ArrayList<>();
        }

        String genreValues = joinList(genreFilterList, ",");
        String langValues = joinList(languageList, ",");

            try {
                ((MainActivity)getActivity()).setFilterIcon(R.drawable.actionbar_filter_icon_default);
            }catch (Exception e){
                e.printStackTrace();
            }

        if (TextUtils.isEmpty(langValues)
                && TextUtils.isEmpty(genreValues)
                && TextUtils.isEmpty(EPG.genreFilterValues)
                && TextUtils.isEmpty(EPG.langFilterValues)) {
            return;
        }

        EPG.genreFilterValues = genreValues;
        EPG.langFilterValues = langValues;

        String gaFilterNames = null;
        if (genreValues != null
                && !genreValues.equals("")) {
            gaFilterNames = genreValues;
        }

        if (langValues != null
                && !langValues.equals("")) {
            if (gaFilterNames != null) {
                gaFilterNames = gaFilterNames + "," + langValues;
            } else {
                gaFilterNames = langValues;
            }

            Analytics.mixpanelEventAppliedFilter(langValues, genreValues);
            CleverTap.eventFilterApplied(genreValues, langValues);
            Analytics.mixpanelSetPeopleProperty(Analytics.MIXPANEL_PEOPLE_SETTINGS_LANGUAGE_USED, true);
        }

        if (gaFilterNames != null) {
            Analytics.gaBrowseFilter(gaFilterNames, 1l);
        }
        ApplicationController.isDateChanged = true;
        ApplicationController.pageVisiblePos = 0;
        ApplicationController.pageItemPos = 0;
        EPG.globalPageIndex = 1;

        if (SECTION_MOVIES == mSectionType) {
            try {
                ((MainActivity) getActivity()).showViewAllFragmentWithFilter(langValues, genreValues);
                ((MainActivity) getActivity()).homePagerAdapterDynamicMenu.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        closeFilterMenuPopup();
        //mToolbar.showOverflowMenu();

    }

    private String joinList(ArrayList list, String literal) {
        return list.toString().replaceAll(",", literal).replaceAll("[\\[.\\].\\s+]", "");
    }

    private boolean checkIsFilterSelected(GenreFilterData body, List<FilterItem> groupLanguages, List<FilterItem> groupGenres) {
        if (body == null || body.results == null) {
            LoggerD.debugLog("GenreFilterData == null");
            closeFilterMenuPopup();
            return false;
        }

        Languages languages = body.results.languages;
        List<Terms> languagesList = null;
        if (languages != null) {
            languagesList = languages.terms;
        }
        GenresData genresData = body.results.genres;
        List<Terms> genresDataList = null;
        if (genresData != null) {
            genresDataList = genresData.terms;
        }
        if (languagesList == null || genresDataList == null) {
            System.out.println("filter null");
            closeFilterMenuPopup();
            return false;
        }
        if (languagesList.size() == 0 && genresDataList.size() == 0) {
            System.out.println("filter empty");
            closeFilterMenuPopup();
            return false;
        }
        CarouselInfoData carouselInfoData = mCarouselInfoData;
        if (carouselInfoData.filteredData == null) {
            carouselInfoData.filteredData = new HashMap<>();
        }
        HashMap<Integer, ArrayList<String>> alreadyFilteredMap = carouselInfoData.filteredData;
        int genreKey = 0;
        int languageKey = 1;
        if (mSectionType == SECTION_MOVIES) {
            genreKey = 1;
            languageKey = 0;
        }
        boolean isFilterAvailable = false;
        if (languagesList != null && alreadyFilteredMap != null) {
            for (int i = 0; i < languagesList.size(); i++) {
                FilterItem filterItem = new FilterItem();
                filterItem.setTitle(languagesList.get(i).term);
                if (alreadyFilteredMap.size() > 0) {
                    if (alreadyFilteredMap.containsKey(languageKey)) {
                        ArrayList<String> genreFilterItems = alreadyFilteredMap.get(languageKey);
                        if (genreFilterItems != null && genreFilterItems.size() > 0) {
                            for (int k = 0; k < genreFilterItems.size(); k++) {
                                if (genreFilterItems.get(k).equals(languagesList.get(i).term)) {
                                    isFilterAvailable = true;
                                    filterItem.setIsChecked(true);
                                    break;
                                } else {
                                    filterItem.setIsChecked(false);
                                }
                            }
                        } else {
                            filterItem.setIsChecked(false);
                        }
                    }
                } else {
                    filterItem.setIsChecked(false);
                }
                groupLanguages.add(filterItem);
            }

        }
      /*  int langCheckCnt =0;
        for(int i =0;i<groupLanguages.size();i++){
            if(groupLanguages.get(i).isChecked()){
                langCheckCnt++;
            }
        }
        FilterItem langFilterItem = new FilterItem();
       *//* if(langCheckCnt == groupLanguages.size()){
            langFilterItem.setIsChecked(true);
            langFilterItem.setTitle("All");
            groupLanguages.add(0, langFilterItem);
        }else {
            langFilterItem.setIsChecked(false);
            langFilterItem.setTitle("All");
            groupLanguages.add(0, langFilterItem);
        }*/

        if (languagesList != null && alreadyFilteredMap != null) {
            for (int i = 0; i < genresDataList.size(); i++) {
                FilterItem filterItem = new FilterItem();
                filterItem.setTitle(genresDataList.get(i).humanReadable);

                if (alreadyFilteredMap.size() > 0) {
                    if (alreadyFilteredMap.containsKey(genreKey)) {
                        ArrayList<String> genreFilterItems = alreadyFilteredMap.get(genreKey);
                        if (genreFilterItems != null && genreFilterItems.size() > 0) {
                            for (int k = 0; k < genreFilterItems.size(); k++) {
                                if (genreFilterItems.get(k).equals(genresDataList.get(i).humanReadable)) {
                                    isFilterAvailable = true;
                                    filterItem.setIsChecked(true);
                                    break;
                                } else {
                                    filterItem.setIsChecked(false);
                                }
                            }
                        } else {
                            filterItem.setIsChecked(false);
                        }
                    }
                } else {
                    filterItem.setIsChecked(false);
                }
                groupGenres.add(filterItem);
            }
        }
        return isFilterAvailable;
    }

    @Override
    public boolean onBackClicked() {
        return super.onBackClicked();
    }
}
