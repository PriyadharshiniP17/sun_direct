package com.myplex.myplex.concurrentExecution.noThreads;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.InlineSearch;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.SearchFilterResponse;
import com.myplex.myplex.concurrentExecution.Callback;
import com.myplex.util.PrefUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MakeAndTrackMultipleCalls {

    private List<CarouselInfoData> mListCarouselInfo;
    private List<SearchFilterResponse> searchConfigList;
    private String searchQuery;
    private String searchFirstFilter;
    private String searchSecondFilter;
    private String searchThirdFilter;
    private List<Integer> posTracking = new ArrayList<>();
    private List<InlineSearch> searchRequestList = new ArrayList<>();
    private Handler mHandler;
    private Callback mCallback;
    private final int DEFAULT_QUERY_TIME = 300;
    private int TOTAL_TIME_OUT = 5000;
    private int INCREMENTAL_TIMEOUT = 0;
    private List<CarouselInfoData> mFinalList = new ArrayList<>();
    private HashMap<Integer, CarouselInfoData> mDataHolder = new HashMap<>();
    public MakeAndTrackMultipleCalls(List<CarouselInfoData> mListCarouselInfo,
                                     List<SearchFilterResponse> searchConfigList,
                                     String searchQuery,
                                     String searchFirstFilter,
                                     String searchSecondFilter,
                                     String searchThirdFilter, int SERVER_TIME_OUT){
        this.mListCarouselInfo = mListCarouselInfo;
        this.searchConfigList = searchConfigList;
        this.searchQuery = searchQuery;
        this.searchFirstFilter = searchFirstFilter;
        this.searchSecondFilter = searchSecondFilter;
        this.searchThirdFilter = searchThirdFilter;
        mHandler = new Handler(Looper.getMainLooper());
        this.TOTAL_TIME_OUT = SERVER_TIME_OUT;
    }

    public void setOnCompleteListener(Callback mCallback){
        this.mCallback = mCallback;
    }

    private Runnable apiCallsTracker = new Runnable() {
        @Override
        public void run() {
            if(posTracking.size() == mListCarouselInfo.size()){
                if (mDataHolder != null && mDataHolder.size() > 0) {
                    for (int i =0;i < mListCarouselInfo.size();i++){
                        if (mDataHolder.get(i) != null
                                && mDataHolder.get(i).listCarouselData != null
                                && mDataHolder.get(i).listCarouselData.size() > 0) {
                            mFinalList.add(mDataHolder.get(i));
                        }
                    }
                    if (mCallback != null) {
                        if (mFinalList.size() > 0) {
                            mCallback.onComplete(mFinalList);
                        }else{
                            mCallback.onFailed();
                        }
                    }
                }else{
                    if(mCallback != null){
                        mCallback.onFailed();
                    }
                }
            }else{
                if(mHandler != null){
                    mHandler.postDelayed(apiCallsTracker,DEFAULT_QUERY_TIME);
                }
                if(INCREMENTAL_TIMEOUT >= TOTAL_TIME_OUT){
                    cancelAllCalls();
                }else{
                    INCREMENTAL_TIMEOUT += DEFAULT_QUERY_TIME;
                }
            }
        }
    };

    private void cancelAllCalls() {
      if(searchRequestList != null && searchRequestList.size() > 0){
          for(int i = 0; i< searchRequestList.size(); i++){
              searchRequestList.get(i).cancel();
          }
          mCallback.onFailed();
      }
    }

    public void trackApiCalls(){
        if(mListCarouselInfo == null){
            return;
        }
        for(int i =0; i< mListCarouselInfo.size(); i++){
            final int pos  = i;
            try {
                CarouselInfoData carouselInfoData = mListCarouselInfo.get(pos);
             /*   final String searchKey = fetchSearchKey(searchConfigList,carouselInfoData.title);
                final String searchFields = fetchSearchFields(searchConfigList,carouselInfoData.title);
                final String publishingHouseId = fetchPublishingHouseId(searchConfigList,carouselInfoData.title);*/
                InlineSearch.Params inlineSearchParams = new InlineSearch.Params(searchQuery, carouselInfoData.name,
                        null,
                      10,
                        1);

                InlineSearch inlineSearchRequest = new InlineSearch(inlineSearchParams, new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                      //  Log.e("Search only ",searchKey+" "+searchFields);
                        if (response == null || response.body() == null) {
                            return;
                        }
                        if (response.body().results != null && response.body().results.size() > 0){
                            mListCarouselInfo.get(pos).listCarouselData = response.body().results;
                            mDataHolder.put(pos, mListCarouselInfo.get(pos));
                        }
                        posTracking.add(pos);
                        System.out.println("TASK "+pos+" Complete");
                    }
                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                       // Log.e("Search only ",searchKey+" "+searchFields);
                        if (t != null) {
                            LoggerD.debugLog("Search"+ "" + t.getMessage());
                        }
                        posTracking.add(pos);
                        System.out.println("TASK "+pos+" Complete");
                    }
                });
                APIService.getInstance().execute(inlineSearchRequest);
                searchRequestList.add(inlineSearchRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(mHandler != null){
            mHandler.postDelayed(apiCallsTracker,DEFAULT_QUERY_TIME);
        }

    }


    private String fetchSearchKey(List<SearchFilterResponse> searchSuggetionsFilters, String name){
        if (searchSuggetionsFilters != null && searchSuggetionsFilters.size() > 0) {
            for (int i = 0; i < searchSuggetionsFilters.size(); i++) {
                if (searchSuggetionsFilters.get(i).displayName.equalsIgnoreCase(name))
                    return searchSuggetionsFilters.get(i).key;
            }
        }
        return "";
    }

    private String fetchSearchFields(List<SearchFilterResponse> searchSuggetionsFilters,String name){
        if (searchSuggetionsFilters != null && searchSuggetionsFilters.size() > 0) {
            for (int i = 0; i < searchSuggetionsFilters.size(); i++) {
                if (searchSuggetionsFilters.get(i).displayName.equalsIgnoreCase(name))
                    return searchSuggetionsFilters.get(i).searchFields;
            }
        }
        return "";
    }

    private String fetchPublishingHouseId(List<SearchFilterResponse> searchSuggetionsFilters,String name){
        if (searchSuggetionsFilters != null && searchSuggetionsFilters.size() > 0) {
            for (int i = 0; i < searchSuggetionsFilters.size(); i++) {
                if (searchSuggetionsFilters.get(i).displayName.equalsIgnoreCase(name))
                    return searchSuggetionsFilters.get(i).publishingHouseId;
            }
        }
        return "";
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mHandler != null && apiCallsTracker != null) {
            mHandler.removeCallbacks(apiCallsTracker);
        }
    }
}
