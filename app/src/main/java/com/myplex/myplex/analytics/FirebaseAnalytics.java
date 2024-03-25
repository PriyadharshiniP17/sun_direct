package com.myplex.myplex.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.myplex.model.CardData;
//import com.facebook.appevents.AppEventsConstants;
//import com.facebook.appevents.AppEventsLogger;

public class FirebaseAnalytics {

    private static final String PARAM_NAME = "name";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_MOBILE = "mobile";
    private static final String PARAM_PRICE = "price";
    private static final String PARAM_PAYMENT_MODE = "payment_mode";
    private static FirebaseAnalytics _self = null;
    private static com.google.firebase.analytics.FirebaseAnalytics mFirebaseTracker;
//    private static AppEventsLogger mFacebookLogger;
    private boolean mEnableFA = true;
    private boolean mEnableFacebookAnalytics=true;
    private Context mContext = null;


    //************************* MOU ************************
    public static String FA_CONTENT_PLAYED = "content_played";
    public static String FA_CONTENTDETAIL_VIEWED = "content_detail_viewed";

    //************************* PAYMENT ********************
    public static final String FA_PAYMENT_SUCCESS= "payment_success";
//    public static final String FA_PAYMENT_FAILED = "payment_failed";
    public static final String FA_ON_BUY_CLICKS="buy_clicks";
    public static final String FA_ON_BUY_CLICKED="buy_clicked";
//    public static final String FA_PAYMENT_CANCELLED="payment_cancelled";

    //************************ Event Parameter Names ****************
    public static String PARAM_CONTENT_NAME="content_name";
    public static String PARAM_CONTENT_TYPE="content_type";
    public static String PARAM_CONTENT_ID="content_id";
    public static String PARAM_CONTENT_DURATION="content_duration";
    public static String PARAM_DURATION_PLAYED="duration_played";
    public static String PARAM_CONTENT_LANGUAGE="content_language";
    public static String PARAM_SOURCE="source";
    public static String PARAM_SOURCE_DETAIL="source_detail";
    public static String PARAM_RELEASE_DATE="release_date";
    public static String PARAM_PAYMENT_STATUS="status";
    public static String PARAM_REASON="reason_of_failure";


    //************************* User properties ************************

    public static final String PROPERTY_EMAIl = "Email";
    public static final String PROPERTY_MOBILE = "Mobile";
    public static final String PROPERTY_NAME = "Name";
    public static final String PROPERTY_UTM_SOURCE = "utm_source";
    public static final String PROPERTY_UTM_MEDIUM = "utm_medium";
    public static final String PROPERTY_UTM_CAMPAIGN = "utm_campaign";
    public static final String PROPERTY_UTM_TERM = "utm_term";

    ///////////////////////SHARE_EVENTS//////////////

    public static final String FA_SHARE_FROM_CONTENT_DETAILS="shared_from_content_details";
    public static final String FA_SHARE_FROM_ARTIST_FRAGMENT="shared_from_artist_profile";

    ////////////////////FAVORITES_EVENTS////////////
    public static final String FA_ADDED_TO_WATCH_LIST="added_to_watch_list";
    public static final String FA_REMOVED_FROM_WATCH_LIST="removed_from_watch_list";
    public static final String FA_WATCH_LIST_EVENT="watch_list";


    ////////////AUTHENETACTION/////////////////////
    public static final String FA_USER_SIGN_UP_START="sign_up_started";
    public static final String FA_USER_SIGN_UP_COMPLETED="sign_up_completed";
    public static final String FA_USER_SIGN_IN_START="sign_in_started";
    public static final String FA_USER_SIGN_IN_COMPLETED="sign_in_completed";

    public static final String FA_SEARCH="search";
    public static final String FA_PROMO_BANNER="promo_banner";

    public static final String INFO="Info";
    public static final String UNDER_SCORE="_";

    public static FirebaseAnalytics getInstance() {
        if (_self == null) {
            _self = new FirebaseAnalytics();
        }
        return _self;
    }

    public void init(Context context, boolean enableFA,boolean facebookAnalytics) {
        this.mEnableFA = enableFA;
        this.mEnableFacebookAnalytics=facebookAnalytics;
        this.mContext = context;


        if (mEnableFA) {
            mFirebaseTracker = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
        }

//        if (mEnableFacebookAnalytics) {
//            mFacebookLogger = AppEventsLogger.newLogger(context);
//        }
    }


    private static boolean canSend() {
        return mFirebaseTracker != null;
    }
//
//    private static boolean canSendFacebookEvents(){
//       return mFacebookLogger != null;
//        return true;
//    }

    public void createScreenFA(Activity activity,String screenName) {
        if (!canSend()) return;
        mFirebaseTracker.setCurrentScreen(activity, screenName, null);
    }

    public void eventContentPlayed(CardData mCardData,Long duration,Long durationPlayed,String source,String sourceDetail) {
        if(isNullObject(mCardData)){
            return;
        }
        Bundle params = new Bundle();
        params.putString(PARAM_CONTENT_NAME,  checkAndReturnValue(mCardData.generalInfo.title));
        params.putString(PARAM_CONTENT_TYPE,  checkAndReturnValue(mCardData.generalInfo.type));
        params.putLong(PARAM_CONTENT_DURATION,  duration);
        params.putLong(PARAM_DURATION_PLAYED,  durationPlayed);
        params.putString(PARAM_CONTENT_LANGUAGE,  getLanguage(mCardData));
        params.putString(PARAM_SOURCE, checkAndReturnValue( source));
        params.putString(PARAM_SOURCE_DETAIL, checkAndReturnValue( sourceDetail));
        params.putString(PARAM_RELEASE_DATE, checkAndReturnValueReleaseData(mCardData));
        params.putString(INFO,checkAndReturnValue(mCardData.generalInfo.title)+
                UNDER_SCORE+checkAndReturnValue(mCardData.generalInfo.type)+UNDER_SCORE+duration+UNDER_SCORE
                +getLanguage(mCardData)+UNDER_SCORE+source+UNDER_SCORE+sourceDetail+UNDER_SCORE+checkAndReturnValueReleaseData(mCardData));
        checkAndSendEvents(FA_CONTENT_PLAYED,params);
    }
    public void FAContentDetailViewed(CardData mCardData, long duration,String source, String sourceDetail) {
        if(isNullObject(mCardData)){
            return;
        }
        Bundle params = new Bundle();
        params.putString(PARAM_CONTENT_NAME, checkAndReturnValue( mCardData.generalInfo.title));
        params.putString(PARAM_CONTENT_TYPE,  checkAndReturnValue(mCardData.generalInfo.type));
        params.putLong(PARAM_CONTENT_DURATION,  duration);
        params.putString(PARAM_CONTENT_LANGUAGE,  getLanguage(mCardData));
        params.putString(PARAM_SOURCE,  checkAndReturnValue(source));
        params.putString(PARAM_SOURCE_DETAIL, checkAndReturnValue( sourceDetail));
        params.putString(PARAM_RELEASE_DATE, checkAndReturnValueReleaseData(mCardData));
        params.putString(INFO,checkAndReturnValue(mCardData.generalInfo.title)
                +UNDER_SCORE+checkAndReturnValue(mCardData.generalInfo.type)+UNDER_SCORE+duration+UNDER_SCORE
                +getLanguage(mCardData)+UNDER_SCORE+source+UNDER_SCORE+sourceDetail+UNDER_SCORE+checkAndReturnValueReleaseData((mCardData)));
        checkAndSendEvents(FA_CONTENTDETAIL_VIEWED,params);
    }

    public void logPaymentsEvent(boolean isSuccess,String name, String email, String mobile, long price, String paymentMode,
                                 String status,String reasonForfailure,CardData mCardData) {
        if (isNullObject(mCardData)){
            return;
        }
        Bundle params = new Bundle();
        params.putString(PARAM_NAME,  checkAndReturnValue(name));
        params.putString(PARAM_EMAIL, checkAndReturnValue(email));
        params.putString(PARAM_MOBILE, checkAndReturnValue(mobile));
        params.putLong(PARAM_PRICE,  price);
        params.putString(PARAM_PAYMENT_MODE, checkAndReturnValue(paymentMode));
        if (!isSuccess){
            params.putString(PARAM_PAYMENT_STATUS,checkAndReturnValue(status));
            params.putString(PARAM_REASON,checkAndReturnValue(reasonForfailure));
            params.putString(INFO,name+UNDER_SCORE+checkAndReturnValue(email)
                    +UNDER_SCORE+checkAndReturnValue(mobile)+UNDER_SCORE+price
                    +UNDER_SCORE+checkAndReturnValue(paymentMode)+UNDER_SCORE+checkAndReturnValue(status)+UNDER_SCORE+checkAndReturnValue(reasonForfailure));
          /*  checkAndSendEvents(replaceSpacesWithUnderScores(checkAndReturnValue(mCardData.generalInfo.title))
                    +UNDER_SCORE+getLanguage(mCardData)+UNDER_SCORE+FA_PAYMENT_FAILED,params);
            checkAndSendEvents(FA_PAYMENT_FAILED, params);*/
        }else {
          // params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY,"USD");
            params.putString(INFO,checkAndReturnValue(name)
                    +UNDER_SCORE+email+UNDER_SCORE+checkAndReturnValue(mobile)+UNDER_SCORE+price+UNDER_SCORE+checkAndReturnValue(paymentMode));
            checkAndSendEvents(replaceSpacesWithUnderScores(checkAndReturnValue(mCardData.generalInfo.title))
                    +UNDER_SCORE+getLanguage(mCardData)+UNDER_SCORE+FA_PAYMENT_SUCCESS,params);
            checkAndSendEvents(FA_PAYMENT_SUCCESS, params);
          //  checkAndSendFbSpecialEvent(AppEventsConstants.EVENT_NAME_PURCHASED,1,params);
        }
    }

    public void onBuyClick(CardData mCardData){
        if (isNullObject(mCardData)){
            return;
        }
        Bundle params = new Bundle();
        params.putString(INFO,checkAndReturnValue(mCardData.generalInfo.type)+UNDER_SCORE+checkAndReturnValue(mCardData.generalInfo.title)
                +UNDER_SCORE+getLanguage(mCardData));
       // params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY,"USD");
        checkAndSendEvents(replaceSpacesWithUnderScores(checkAndReturnValue(mCardData.generalInfo.title))
                +UNDER_SCORE+getLanguage(mCardData)+UNDER_SCORE+FA_ON_BUY_CLICKED,params);
        checkAndSendEvents(FA_ON_BUY_CLICKS,params);
       // checkAndSendFbSpecialEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT,0.5,params);
    }

    public void onShareClick(boolean isFromArtistFragment,CardData mCardData){
        if (isNullObject(mCardData)){
            return;
        }
        Bundle params = new Bundle();
        params.putString(INFO,checkAndReturnValue(mCardData.generalInfo.type)+UNDER_SCORE+
                checkAndReturnValue(mCardData.generalInfo.title)+UNDER_SCORE+getLanguage(mCardData));
        if(isFromArtistFragment){
            checkAndSendEvents(FA_SHARE_FROM_ARTIST_FRAGMENT,params);
        }else {
            checkAndSendEvents(FA_SHARE_FROM_CONTENT_DETAILS,params);
        }
    }

    public void logPaymentCancelledEvent(boolean isSuccess, String name, String email, String mobile, long price, String paymentMode,
                                         String status, String reasonForfailure, CardData mCardData){
        Bundle params = new Bundle();
        params.putString(INFO,name+UNDER_SCORE+email+UNDER_SCORE+mobile+UNDER_SCORE+price+
                UNDER_SCORE+paymentMode+UNDER_SCORE+status+UNDER_SCORE+reasonForfailure);
//        checkAndSendEvents(FA_PAYMENT_CANCELLED,params);
    }

    public void addOrRemoveFromWatchList(boolean isAdded,CardData mCardData){
        if (isNullObject(mCardData)){
            return;
        }
        Bundle params = new Bundle();
        if (isAdded){
            params.putString(INFO,FA_ADDED_TO_WATCH_LIST+UNDER_SCORE+checkAndReturnValue(mCardData.generalInfo.type)+UNDER_SCORE
                    +checkAndReturnValue(mCardData.generalInfo.title)+UNDER_SCORE+getLanguage(mCardData));
        }else {
            params.putString(INFO,FA_REMOVED_FROM_WATCH_LIST+UNDER_SCORE+checkAndReturnValue(mCardData.generalInfo.type)
                    +UNDER_SCORE+checkAndReturnValue(mCardData.generalInfo.title));
        }
        checkAndSendEvents(FA_WATCH_LIST_EVENT,params);
    }

    public void userSignedInCompleted(){
        Bundle params = new Bundle();
        checkAndSendEvents(FA_USER_SIGN_IN_COMPLETED,params);
    }

    public void userSignInStarted(){
        Bundle params = new Bundle();
        checkAndSendEvents(FA_USER_SIGN_IN_START,params);
    }

    public void userSignUpStarted(){
        Bundle params = new Bundle();
        checkAndSendEvents(FA_USER_SIGN_UP_START,params);
    }

    public void userSignUpCompleted(){
        Bundle params = new Bundle();
        checkAndSendEvents(FA_USER_SIGN_UP_COMPLETED,params);
    }

    public void search(String mQuery){
        Bundle params = new Bundle();
        params.putString(INFO,mQuery);
        checkAndSendEvents(FA_SEARCH,params);
    }

    public void promobanner(String mQuery){
        Bundle params = new Bundle();
        params.putString(INFO,mQuery);
        checkAndSendEvents(FA_PROMO_BANNER,params);
    }

    public void setMobileNumberProperty(String mobileNumber){
        if (!canSend()) return;
        mFirebaseTracker.setUserProperty(PROPERTY_MOBILE,mobileNumber);
    }

    public void setEmailProperty(String email){
        if (!canSend()) return;
        mFirebaseTracker.setUserProperty(PROPERTY_EMAIl,email);
    }

    public void setNameProperty(String name){
        if (!canSend()) return;
        mFirebaseTracker.setUserProperty(PROPERTY_NAME,name);
    }

    private String replaceSpacesWithUnderScores(String actualString){
        return actualString.replace(" ",UNDER_SCORE);
    }

    private Boolean isNullObject(CardData mCardData){
        return mCardData == null || mCardData.generalInfo == null;
    }

    public String checkAndReturnValue(String value){
        if(value==null|| TextUtils.isEmpty(value)){
            return "NA";
        }
        return value;
    }

    private String checkAndReturnValueReleaseData(CardData value){
        if(value!=null&&value.content!=null&&value.content.releaseDate!=null){
            return value.content.releaseDate;
        }
        return "NA";
    }

    private String getLanguage(CardData mData){
        if (mData.content!=null&&mData.content.language != null && mData.content.language.size() > 0) {
                return  replaceSpacesWithUnderScores(mData.content.language.get(0));
        }
        return "";
    }

    public void checkAndSendFbSpecialEvent(String EVENT_NAME,double price,Bundle values){
//        if(canSendFacebookEvents()){
//            //mFacebookLogger.logEvent(EVENT_NAME,price,values);
//        }
    }

    private void checkAndSendEvents(String EVENT_NAME, Bundle values){
        if (canSend()){
            mFirebaseTracker.logEvent(EVENT_NAME,values);
        }
//        if (canSendFacebookEvents()){
//            //mFacebookLogger.logEvent(EVENT_NAME,values);
//        }
    }

}
