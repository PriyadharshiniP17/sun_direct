package com.myplex.myplex.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.github.pedrovgs.LoggerD;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.epg.EPGList;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardResponseData;
import com.myplex.model.CategoryScreenFilters;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.utils.Util;

import junit.framework.Assert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by samir on 12/16/2015.
 */
public class EPG {

    private SparseArray<TreeSet<Event>> epgTable;
    private Hashtable<String, Integer> channelPositions;
    private HashMap<Integer,String>channelImages;
    private HashMap<Integer,ChannelItem>channelList;
    private static final int PAGE_SIZE = 10;
    private EPGList epgList;
    public static final String TAG = EPG.class.getSimpleName();
    private static EPG mInstance = null;
    private Date dateOfEPG;
    public static int globalPageIndex =1;
    public static String genreFilterValues ="";
    public static String langFilterValues="";


    public static EPG getInstance(Date currentDate) {
        if (mInstance == null || ApplicationController.isDateChanged) {
            ApplicationController.isDateChanged = false;
            mInstance = new EPG(currentDate);
        }
        return mInstance;
    }

    public interface CacheManagerCallback {

        void OnlineResults(List<CardData> dataList,int pageIndex);

        void OnlineError(Throwable error, int errorCode);
    }

    /**
     * Here we comparing based On starTime of event
     */
    private class EventComparator implements Comparator<Event> {

        @Override
        public int compare(Event event1, Event event2) {
            return event1.startTime > event2.startTime ? 1 : -1;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }


    public EPG(Date dateOfEPG) {
        this.dateOfEPG = dateOfEPG;
        init();
    }

    /**
     * Initializing the collections objects;
     */
    private void init() {
        channelPositions = new Hashtable<>();
        epgTable = new SparseArray<>();
        channelImages = new HashMap<>();
        channelList = new HashMap<>();
    }

    /**
     * Fill the EPG Table with programs based on Channels
     * @param programs             server list of programs
     * @param displayPageIndex            pagination value
     * @param time                 Epg fetched time
     * @param date                 Requested Date.
     * @param isForceUpdate        forceRefresh the user while EmptyCard request
     * @param cacheManagerCallback callback to the user
     * @param isToSendDataImmediately
     */
    private void addPrograms(List<CardData> programs, int displayPageIndex, String time, Date date, boolean isForceUpdate, CacheManagerCallback cacheManagerCallback, boolean isToSendDataImmediately) {
        //Log.v(TAG, "addPrograms " + programs.size());
        Assert.assertNotNull(programs);
        Assert.assertNotNull(date);
        // add this programs to epgData
        for (int i = 0; i < programs.size(); i++) {

            Date startDate = Util.getUTCDate(programs.get(i).startDate);
            Date endDate = Util.getUTCDate(programs.get(i).endDate);

            CardData cardData = programs.get(i);
            cardData.pageIndex = displayPageIndex;
            String channelId = programs.get(i).globalServiceId;

            if (channelId == null) continue;

            TreeSet<Event> orderedPrograms;

            if (channelPositions.containsKey(channelId)) {
                orderedPrograms = epgTable.get(channelPositions.get(channelId));
            } else {
                orderedPrograms = new TreeSet<>(new EventComparator());
                int size = epgTable.size();
                epgTable.put(size, orderedPrograms);
                channelPositions.put(channelId, size);
               // addChannelData(cardData,size);
                addChannelImageData(cardData,size);

            }

            Event event = new Event(startDate.getTime(), endDate.getTime(), cardData);
            orderedPrograms.add(event);
        }

        if(isToSendDataImmediately){
            cacheManagerCallback.OnlineResults(programs, displayPageIndex);
            return;
        }

        if (epgTable.size() > 0 && cacheManagerCallback != null) {
            List<CardData> programsList = fetchEpgChannelPrograms(Util.getRequiredDate(time, date), displayPageIndex);
            cacheManagerCallback.OnlineResults(programsList, displayPageIndex);
        }
    }

    private void addChannelData(CardData cardData, int size) {
        if (cardData.images != null && cardData.images.values != null && cardData.images.values.size() > 0) {
            for (CardDataImagesItem imageItem : cardData.images.values) {
                if (imageItem.type != null && imageItem.type.equalsIgnoreCase("thumbnail") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)
                        && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("250x375")) {
                    if (imageItem.link != null) {
                        channelImages.put(size, imageItem.link);
                    } else {
                        channelImages.put(size,null);
                    }
                    break;
                }
            }
        } else {
            channelImages.put(size, null);
        }
    }
    private void addChannelImageData(CardData cardData, int size) {
        ChannelItem channelItem = new ChannelItem();
        if(cardData.images == null || cardData.images.values == null || cardData.images.values.size() ==0){
            channelItem.setChannelId(cardData.globalServiceId);
            channelItem.setChannelImgUrl(null);
            channelList.put(size, channelItem);
            return;
        }
            for (CardDataImagesItem imageItem : cardData.images.values) {
                if (imageItem.type != null && imageItem.type.equalsIgnoreCase("thumbnail") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)

                        && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("250x375")) {
                    if (imageItem.link != null && imageItem.link.trim().length()>0) {
                        channelItem.setChannelId(cardData.globalServiceId);
                        channelItem.setChannelImgUrl(imageItem.link.replace("epgimages/", "epgimagesV2/"));
                        channelList.put(size, channelItem);
                    } else {
                        channelItem.setChannelId(cardData.globalServiceId);
                        channelItem.setChannelImgUrl(null);
                        channelList.put(size, channelItem);
                    }
                    break;
                }else {
                    channelItem.setChannelId(cardData.globalServiceId);
                    channelItem.setChannelImgUrl(null);
                    channelList.put(size, channelItem);
                }
            }
    }
    /**
     * Find the requested programs based time and pageIndex
     * Initially check in epgTable if it exists returns ProgramsList to callback
     * If not Exists Request to server fetch those programs
     * @param dateStamp            server Requested timeStamp
     * @param epgTime              User requested epgTime
     * @param date                 User Selected Date
     * @param displayPageIndex            Pagination Index
     * @param isForceUpdate        forceRefresh the user while EmptyCard request.
     * @param mccAndMNCValues
     * @param isDVROnly
     * @param cacheManagerCallback callback
     */
    public void findPrograms(int count, String dateStamp, String epgTime, Date date, int displayPageIndex, boolean isForceUpdate, String mccAndMNCValues, boolean isDVROnly, String genreValues,CacheManagerCallback cacheManagerCallback) {
        // search for programs in epgData if not send request to server
        if (epgTable == null || epgTable.size() == 0 || isForceUpdate) { // send the online request
            //Log.v(TAG, "No data fetching onlineResults");
            onlineRequest(null,count, dateStamp, displayPageIndex, epgTime, date, isForceUpdate, mccAndMNCValues, isDVROnly,genreValues, cacheManagerCallback, true);
            return;
        }
            //Need to check data available or not
     /*   if (checkChannelEpgPrograms(Util.getRequiredDate(epgTime, date), displayPageIndex)) {
            //Log.v(TAG, "fetching from cacheResults");
            List<CardData> cacheCardsList = fetchEpgChannelPrograms(Util.getRequiredDate(epgTime, date), displayPageIndex);
            cacheManagerCallback.OnlineResults(cacheCardsList, displayPageIndex);
            return;
        }*/
        //Log.v(TAG, "No data fetching onlineResults");
        onlineRequest(null,count, dateStamp, displayPageIndex, epgTime, date, isForceUpdate, mccAndMNCValues, isDVROnly,genreValues, cacheManagerCallback, true);


    }

    /**
     * Find the requested programs based time and pageIndex
     * Initially check in epgTable if it exists returns ProgramsList to callback
     * If not Exists Request to server fetch those programs
     * @param dateStamp            server Requested timeStamp
     * @param epgTime              User requested epgTime
     * @param date                 User Selected Date
     * @param displayPageIndex            Pagination Index
     * @param isForceUpdate        forceRefresh the user while EmptyCard request.
     * @param mccAndMNCValues
     * @param isDVROnly
     * @param cacheManagerCallback callback
     * @param isToRedirectDireclty
     *
     */
    public void findPrograms(int count, String dateStamp, String epgTime, Date date, int displayPageIndex, boolean isForceUpdate, String mccAndMNCValues, boolean isDVROnly, String genreValues, CacheManagerCallback cacheManagerCallback, boolean isToRedirectDireclty) {
        // search for programs in epgData if not send request to server
        if (epgTable == null || epgTable.size() == 0 || isForceUpdate) { // send the online request
            //Log.v(TAG, "No data fetching onlineResults");
            onlineRequest(null,count, dateStamp, displayPageIndex, epgTime, date, isForceUpdate, mccAndMNCValues, isDVROnly, genreValues,cacheManagerCallback, isToRedirectDireclty);
            return;
        }
        //Need to check data available or not
       /* if (checkChannelEpgPrograms(Util.getRequiredDate(epgTime, date), displayPageIndex)) {
            //Log.v(TAG, "fetching from cacheResults");
            List<CardData> cacheCardsList = fetchEpgChannelPrograms(Util.getRequiredDate(epgTime, date), displayPageIndex);
            cacheManagerCallback.OnlineResults(cacheCardsList, displayPageIndex);
            return;
        }*/
        //Log.v(TAG, "No data fetching onlineResults");
        onlineRequest(null,count, dateStamp, displayPageIndex, epgTime, date, isForceUpdate, mccAndMNCValues, isDVROnly,genreValues, cacheManagerCallback, isToRedirectDireclty);


    }

    /**
     * Check the channel programs exists or not
     * @param date         User Selected Date
     * @param displayPageIndex    Pagination Index.
     * @return
     */
      private boolean checkChannelEpgPrograms(Date date, int displayPageIndex) {

          int startIndex = (displayPageIndex - 1) * PAGE_SIZE;

          long currentIndexTime = date.getTime();
          int endIndex;
          endIndex = (startIndex + PAGE_SIZE) > epgTable.size() ? epgTable.size() : (startIndex + PAGE_SIZE);
          for (int index = startIndex; index < endIndex; index++) {

              TreeSet<Event> events = epgTable.get(index);
              List<Event> list = new ArrayList<>(events);

              for (int j = 0; j < list.size(); j++) {
                  Event event = list.get(j);

                  if (event.startTime <= currentIndexTime && event.endTime >= currentIndexTime) {
                      return true;
                  } else {
                      break;
                  }
              }
          }
          return false;
      }
    /**
     * Fetch the programs based on Date and Pagination index value
     *
     * @param date      User`s Requested Date
     * @param displayPageIndex Pagination index value
     * @return It returns the  List of cardData.
     */

    public List<CardData> fetchEpgChannelPrograms(Date date, int displayPageIndex) {
        //Log.v(TAG, "fetchEpgChannelPrograms");
        List<CardData> cardList = new ArrayList<>();

        int startIndex = (displayPageIndex - 1) * PAGE_SIZE;

        long currentIndexTime = date.getTime();
        boolean isProgramFound ;
        int endIndex;
        endIndex = (startIndex + PAGE_SIZE) > epgTable.size() ? epgTable.size() : (startIndex + PAGE_SIZE);
        for (int index = startIndex; index < endIndex; index++) {
            TreeSet<Event> events = epgTable.get(index);
            List<Event> list = new ArrayList<>(events);
            isProgramFound = false;
            for (int j = 0; j < list.size(); j++) {
                Event event = list.get(j);
                if ((event.startTime <= currentIndexTime && event.endTime >= currentIndexTime)) {
                    isProgramFound = true;
                    cardList.add(event.cardData);
                    break;
                }
            }
            if(!isProgramFound){
                cardList.add(createEmptyCards());
            }
        }
       //Log.v(TAG, "results size " + cardList.size());
        return cardList;


    }

    /**
     * Send the server Request to fetch the epg Programs List
     * @param count
     * @param dateStamp            Input to Server DateStamp
     * @param displayPageIndex            Pagination index value
     * @param time                 User Requested EPG Time.
     * @param date                 User Requested Date.
     * @param isForceUpdate        forceRefresh the user while EmptyCard request
     * @param mccAndMNCValues      MNC and MCC Values
     * @param isDVROnly
     * @param cacheManagerCallback callback to the user
     * @param isToSendDataImmediately
     */
    private void onlineRequest(Context mContext,
                               int count, String dateStamp,
                               final int displayPageIndex,
                               final String time, final Date date,
                               final boolean isForceUpdate, String mccAndMNCValues,
                               boolean isDVROnly, String genreValues, final CacheManagerCallback cacheManagerCallback,
                               final boolean isToSendDataImmediately) {

        String OrderBy = null;
        String publishingHouseId = null;
        CategoryScreenFilters categoryScreenFilters = PropertiesHandler.getCategoryScreenFilters(mContext);
        if(categoryScreenFilters != null && categoryScreenFilters.categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters.size() > 0){
            for(int i=0;i<categoryScreenFilters.categoryScreenFilters.size();i++){
                if(categoryScreenFilters.categoryScreenFilters.get(i) != null
                        && !TextUtils.isEmpty(categoryScreenFilters.categoryScreenFilters.get(i).contentType)
                        && categoryScreenFilters.categoryScreenFilters.get(i).contentType.equalsIgnoreCase(APIConstants.TYPE_LIVE)){
                    OrderBy = categoryScreenFilters.categoryScreenFilters.get(i).orderBy;
                    publishingHouseId = categoryScreenFilters.categoryScreenFilters.get(i).publishingHouseId;
                }
            }
        }

        String mccValue = "";
        String mncValue = "";
        if (mccAndMNCValues != null && mccAndMNCValues.length() > 0) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }
        String orderBy = "";
        if(OrderBy != null){
            orderBy = OrderBy;
        } else if (ApplicationController.iS_CIRCLE_BASED_REQ) {
            orderBy = "siblingOrder,location";
        } else {
            orderBy = "siblingOrder";
        }

      /*  //Log.v(TAG, "onlineRequest: " +
                " dateStamp- " + dateStamp + "" +
                " time= " + date + "" +
                " displayPageIndex = " + displayPageIndex + "" +
                " genre " + genreFilterValues + "" +
                " lang " + langFilterValues + "" +
                " mcc " + mccValue + "" +
                " count " + count + "" +
                " mnc " + mncValue);*/

        EPGList.Params epgListParams = new EPGList.Params(dateStamp, "devicemax", "mdpi",
                count, displayPageIndex, orderBy, genreValues, EPG.langFilterValues,
                mccValue, mncValue, ApplicationController.iS_CIRCLE_BASED_REQ, isDVROnly, isToSendDataImmediately,publishingHouseId);
        epgList = new EPGList(epgListParams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if (null == response.body()) {
                            cacheManagerCallback.OnlineResults(null, displayPageIndex);
                            return;
                        }
/*
                        if (response.body().status.equalsIgnoreCase("ERR_INVALID_SESSION_ID") && response.body().code == 401) {
                            cacheManagerCallback.OnlineError(new Throwable(""), errorCode);
                        }
*/
                        if (!APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            cacheManagerCallback.OnlineError(new Throwable(response.body().status), response.body().code);
                            return;
                        }

                        if (response.body().results == null || response.body().results.size() == 0) {
                            cacheManagerCallback.OnlineResults(null, displayPageIndex);
                            return;
                        }

                        if (cacheManagerCallback != null) {
                            addPrograms(response.body().results, displayPageIndex, time, date, isForceUpdate,cacheManagerCallback,isToSendDataImmediately);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        cacheManagerCallback.OnlineError(t, errorCode);
                    }
                });

        APIService.getInstance().execute(epgList);

    }

    public class Event {
        long startTime;
        long endTime;
        CardData cardData; // program info

        public Event(long startTime, long endTime, CardData data) {
            this.startTime = startTime;
            this.endTime = endTime;
            cardData = data;
        }
        @Override
        public boolean equals(Object o) {
            Event event = (Event)o;
            if(cardData  == null || event == null || event.cardData == null ) return false;
            return cardData._id == event.cardData._id;
        }

    }

    public class Channel {
        String channelId;
        String imageUrl;
        TreeSet<Event> orderedPrograms;
        Event currentProgram;

    }
    public static CardData createEmptyCards(){
        CardData cardData = new CardData();
        return cardData;
    }

    public HashMap<Integer,String> fetchChannelsList(int size) {

        HashMap<Integer, String> channelList = new HashMap<>();
        for (int i = 0; i < size; i++) {
            channelList.put(i, channelImages.get(i));
        }
        if(channelList == null || channelList.size() ==0)
            return null;

        return channelList;

    }

    public HashMap<Integer,ChannelItem> fetchChannels(List<CardData> cardList){
        HashMap<Integer,ChannelItem> channelItemHashMap = new HashMap<>();
        for(int j =0 ;j<cardList.size();j++){
            ChannelItem channelItem = channelList.get(j);
            channelItemHashMap.put(j,channelItem);
        }
        return channelItemHashMap;
    }
    public HashMap<Integer,ChannelItem> fetchAllChannels(){
        if(channelList!=null && channelList.size()>0){
            return channelList;
        }
        return null;
    }


    /**
     * Send the server Request to fetch the epg Programs List
     * @param count
     * @param dateStamp            Input to Server DateStamp
     * @param displayPageIndex            Pagination index value
     * @param time                 User Requested EPG Time.
     * @param date                 User Requested Date.
     * @param isForceUpdate        forceRefresh the user while EmptyCard request
     * @param mccAndMNCValues      MNC and MCC Values
     * @param isDVROnly
     * @param cacheManagerCallback callback to the user
     */
    private void onlineRequest(Context mContext, int count, String dateStamp, final int displayPageIndex,
                               final String time, final Date date,
                               final boolean isForceUpdate, String mccAndMNCValues,
                               boolean isDVROnly, final CacheManagerCallback cacheManagerCallback) {

        String OrderBy = null;
        String publishingHouseId = null;
        CategoryScreenFilters categoryScreenFilters = PropertiesHandler.getCategoryScreenFilters(mContext);
        if(categoryScreenFilters != null && categoryScreenFilters.categoryScreenFilters != null
                && categoryScreenFilters.categoryScreenFilters.size() > 0 ){
            for(int i=0;i<categoryScreenFilters.categoryScreenFilters.size();i++){
                if( categoryScreenFilters.categoryScreenFilters.get(i) != null
                        && !TextUtils.isEmpty(categoryScreenFilters.categoryScreenFilters.get(i).contentType)
                        && categoryScreenFilters.categoryScreenFilters.get(i).contentType.equalsIgnoreCase(APIConstants.TYPE_LIVE)){
                    OrderBy = categoryScreenFilters.categoryScreenFilters.get(i).orderBy;
                    publishingHouseId = categoryScreenFilters.categoryScreenFilters.get(i).publishingHouseId;
                }
            }
        }

        String mccValue = "";
        String mncValue = "";
        if (mccAndMNCValues != null && mccAndMNCValues.length() > 0) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
        }
        String orderBy = "";
        if(OrderBy != null){
            orderBy = OrderBy;
        } else if (ApplicationController.iS_CIRCLE_BASED_REQ) {
            orderBy = "siblingOrder,location";
        } else {
            orderBy = "siblingOrder";
        }

       /* //Log.v(TAG, "onlineRequest: " +
                " dateStamp- " + dateStamp + "" +
                " time= " + date + "" +
                " displayPageIndex = " + displayPageIndex + "" +
                " genre " + genreFilterValues + "" +
                " lang " + langFilterValues + "" +
                " mcc " + mccValue + "" +
                " count " + count + "" +
                " mnc " + mncValue);*/

        EPGList.Params epgListParams = new EPGList.Params(dateStamp,
                "epgstatic",
                "mdpi",
                count, displayPageIndex,
                orderBy, EPG.genreFilterValues,
                EPG.langFilterValues, mccValue, mncValue,
                ApplicationController.iS_CIRCLE_BASED_REQ,
                isDVROnly, false,publishingHouseId);
        epgList = new EPGList(epgListParams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if (null == response.body()) {
                            cacheManagerCallback.OnlineResults(null, displayPageIndex);
                            return;
                        }
/*
                        if (response.body().status.equalsIgnoreCase("ERR_INVALID_SESSION_ID") && response.body().code == 401) {
                            cacheManagerCallback.OnlineError(new Throwable(""), errorCode);
                        }
*/
                        if (!APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            cacheManagerCallback.OnlineError(new Throwable(response.body().status), response.body().code);
                            return;
                        }

                        if (response.body().results == null || response.body().results.size() == 0) {
                            cacheManagerCallback.OnlineResults(null, displayPageIndex);
                            return;
                        }

                        if (cacheManagerCallback != null) {
                            cacheManagerCallback.OnlineResults(response.body().results, displayPageIndex);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        cacheManagerCallback.OnlineError(t, errorCode);
                    }
                });

        APIService.getInstance().execute(epgList);

    }

    /**
     * Find the requested programs based time and pageIndex
     * Initially check in epgTable if it exists returns ProgramsList to callback
     * If not Exists Request to server fetch those programs
     * @param count            server Requested count
     * @param pageIndex              User requested pageIndex
     * @param cacheManagerCallback callback
     *
     */
    public void findPrograms(Context mContext, int count, int pageIndex, CacheManagerCallback cacheManagerCallback) {
        List<String> list = generateEpgTable(ApplicationController.ENABLE_BROWSE_PAST_EPG);

        String time = list.get(0);
        final Date date = Util.getCurrentDate(0);
        boolean isDVROnly = (PrefUtils.getInstance().getPrefEnablePastEpg() && 0 - PrefUtils.getInstance().getPrefNoOfPastEpgDays() < 0) ? true : false;
        // search for programs in epgData if not send request to server
        //Log.v(TAG, "No data fetching onlineResults");
//        pageSize, Util.getServerDateFormat(time, date), time, date, mStartIndex, true, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly,
        onlineRequest(mContext,count, Util.getServerDateFormat(time, date), pageIndex, time, date, true, SDKUtils.getMCCAndMNCValues(mContext), isDVROnly, cacheManagerCallback);
    }


    public ArrayList<String> generateEpgTable(boolean backEpg) {
        ArrayList<String> epgCounterList = new ArrayList<>();

        SimpleDateFormat adParser = new SimpleDateFormat("HH:mm");

        SimpleDateFormat minParser = new SimpleDateFormat("mm");
        SimpleDateFormat hrsParser = new SimpleDateFormat("HH");

        Calendar cal = Calendar.getInstance();
        String min = minParser.format(cal.getTime());
        String hrs = hrsParser.format(cal.getTime());

        StringBuilder epgTimeTable = new StringBuilder();
        int diff = Integer.parseInt(min) % ApplicationController.TIME_SHIFT;
        int showTime = Integer.parseInt(min) - diff;
        String tm = String.valueOf(hrs) + ":" + String.valueOf(showTime);
        try {
            if (!backEpg /*&& dateTxt.getText().equals("Schedule")*/) {
                adParser.parse(tm);
                Calendar call = Calendar.getInstance();
                call.setTime(adParser.getCalendar().getTime());
                String toTime = adParser.format(call.getTime());

                Calendar calendarAdd = Calendar.getInstance();
                epgCounterList.add(toTime);
                while (!toTime.equals("23:45")) {
                    calendarAdd.setTime(adParser.getCalendar().getTime());
                    calendarAdd.add(Calendar.MINUTE, ApplicationController.TIME_SHIFT);
                    toTime = adParser.format(calendarAdd.getTime());
                    epgTimeTable.append(epgTimeTable.length() == 0 ? "" : ", " + epgTimeTable);
                    epgCounterList.add(toTime);
                }
                LoggerD.debugLog("TVGuide: generateEpgTable: epgTimeTable- " + epgTimeTable.toString());
                return epgCounterList;
            }

            Calendar calendar = Calendar.getInstance();
            String parseTime = "00:00";
            adParser.parse(parseTime);
            String time = "";
            epgCounterList.add(parseTime);
            while (!time.equals("23:45")) {
                calendar.setTime(adParser.getCalendar().getTime());
                calendar.add(Calendar.MINUTE, ApplicationController.TIME_SHIFT);
                time = adParser.format(calendar.getTime());
                epgTimeTable.append(epgTimeTable.length() == 0 ? "" : ", " + epgTimeTable);
                epgCounterList.add(time);
            }
            LoggerD.debugLog("TVGuide: generateEpgTable: epgTimeTable- " + epgTimeTable.toString());

            return epgCounterList;


        } catch (ParseException e) {
            e.printStackTrace();
            return epgCounterList;
        }
    }

}
