package com.myplex.myplex.debug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.ContentDetails;
import com.myplex.api.request.content.FilterRequest;
import com.myplex.api.request.content.RequestContentList;
import com.myplex.api.request.epg.EPGList;
import com.myplex.api.request.security.MediaLinkEncrypted;
import com.myplex.api.request.user.DeviceRegistration;
import com.myplex.api.request.user.Login;
import com.myplex.api.request.user.SignUp;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGenre;
import com.myplex.model.CardResponseData;
import com.myplex.model.DeviceRegData;
import com.myplex.model.GenreFilterData;
import com.myplex.model.LocationInfo;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.CardDetailsActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Created by Srikanth on 12/10/2015.
 */
public class DebugActivity extends BaseActivity {
    private Context mContext;
    private TextView mStatus;
    DeviceRegistration deviceRegistration;
    Login login;
    SignUp signUP;
    RequestContentList contentList;
    EPGList epgList;
    MediaLinkEncrypted medialLink;
    ContentDetails contentDetails;
    private String TAG = DebugActivity.class.getSimpleName();
    private TextView dummyTxt ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        dummyTxt = (TextView)findViewById(R.id.dateTxt);

        SpannableStringBuilder cs = new SpannableStringBuilder("Sun 24th Jan");
        Spannable cs1 = new SpannableString("Sun 24th Jan");
        cs1.setSpan(new SuperscriptSpan(), 6, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        dummyTxt.setText(cs1);
        mContext = this;
        mStatus = (TextView) findViewById(R.id.status);
        int position = 25;
        int s = (position/10)+1;
        //914430347360


       /* int pageIndex =0;
        if (position < 9) {
            pageIndex = 1;
        }else if(position>9 && position<=19){
            pageIndex = 2;
        }else {
            pageIndex = position/10;
        }*/

       // UiUtil.getServerDateFormat("10:15");
      //  initRequestCalls();
    }
    private void prepareFilterListData() {

        FilterRequest.Params requestParams = new FilterRequest.Params("allFacates");
        final FilterRequest request = new FilterRequest(requestParams, new APICallback<GenreFilterData>() {
            @Override
            public void onResponse(APIResponse<GenreFilterData> response) {
                if (null == response.body()) {
                    return;
                }


            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });

        APIService.getInstance().execute(request);
    }
    public void onClick(View v) {
        mStatus.setText("requesting...");
        String domain = getString(R.string.config_domain_name);
        switch (v.getId()) {

            case R.id.launch_hooq_sdk_player:
//                startActivity(new Intent(this, LaunchActivity.class));
                return;
                // APIService.newInstance().execute(deviceRegistration);
                //  EPG.newInstance(Util.getCurrentDate(0)).fetchEpgChannelPrograms(Util.getRequiredDate("15:00", new Date()), 1, genre, lang, genre, date, lang, cacheManagerCallback);

//                break;
            case R.id.device_registration:
               // APIService.newInstance().execute(deviceRegistration);
              //  EPG.newInstance(Util.getCurrentDate(0)).fetchEpgChannelPrograms(Util.getRequiredDate("15:00", new Date()), 1, genre, lang, genre, date, lang, cacheManagerCallback);


                break;

            case R.id.sign_up:
               // APIService.newInstance().execute(signUP);
              //  EPG.newInstance(Util.getCurrentDate(0)).fetchEpgChannelPrograms(Util.getRequiredDate("18:15",new Date()), 1, genre, lang, genre, date, lang, cacheManagerCallback);

                break;

            case R.id.login:
                //EPG.newInstance(Util.getCurrentDate(0)).fetchEpgChannelPrograms(Util.getRequiredDate("18:00",new Date()),1, genre, lang, genre, date, lang, cacheManagerCallback);
             //   APIService.newInstance().execute(login);
                break;

            case R.id.content_list:
                prepareFilterListData();
               // makeEpgCall("10:00", 1);
               // APIService.newInstance().execute(contentList);
                break;

            case R.id.channel_epg:
                makeEpgCall("18:15",1);

                // APIService.newInstance().execute(channelEPG);
                break;

            case R.id.epg_list:
                makeEpgCall("15:00", 1);
              // APIService.newInstance().execute(epgList);
                break;
            case R.id.medi_links:
                makeEpgCall("15:00", 2);
               // APIService.newInstance().execute(medialLink);
                break;

            case R.id.card_details:
                APIService.getInstance().execute(contentDetails);
                break;

            default:
                break;
        }
    }
    private void makeEpgCall(String time,int pageIndex){
        ArrayList<String > genreList = new ArrayList<>();
        ArrayList<String > langList = new ArrayList<>();
       genreList.add("news");

        EPG.getInstance(Util.getCurrentDate(0)).findPrograms(APIConstants.PAGE_INDEX_COUNT,Util.getServerDateFormat(time, new Date()), time, new Date(), pageIndex, false, getMCCAndMNCValues(), false, "",new EPG.CacheManagerCallback() {
            @Override
            public void OnlineResults(List<CardData> dataList, int pageIndex) {
                getFilteredResults(dataList);

            }

            @Override
            public void OnlineError(Throwable error, int errorCode) {

            }
        });
        //APIService.newInstance().equals()


    }


    private void getFilteredResults(List<CardData> dataList) {
        ArrayList<String> genreData = new ArrayList<>();
       // genreData.add("news");
        genreData.add("phani");
        ArrayList<String>langData = new ArrayList<>();
       // langData.add("hindi");
       // langData.add("kannada");
        langData.add("nani");
       List<CardData> genreList = new ArrayList<>();
        List<CardData>langList = new ArrayList<>();
        for(int i =0;i<dataList.size();i++){
            CardData cardData = dataList.get(i);
            if (cardData.content != null) {
                if (cardData.content.genre != null && cardData.content.genre.size() > 0) {
                    List<CardDataGenre> cardDataGenres = cardData.content.genre;
                    String genre = cardDataGenres.get(0).name;
                    for(int k =0 ;k<genreData.size();k++){
                           if(genre.equals(genreData.get(k))){
                               genreList.add(cardData);
                               break;
                           }
                    }
                }
                if (cardData.content.language != null && cardData.content.language.size() > 0) {
                    String lang = cardData.content.language.get(0);
                    for(int k =0 ;k<langData.size();k++){
                        if(lang.equals(langData.get(k))){
                            langList.add(cardData);
                            break;
                        }
                    }
                }
            }
        }

        for(int i =0;i<genreList.size();i++){
              for(int k =0;k<langList.size();k++){
                  if(genreList.get(i).globalServiceId.equals(langList.get(k).globalServiceId)){
                      genreList.remove(i);
                      break;
                  }
              }
        }
        for(int i=0;i<langList.size();i++){
            genreList.add(langList.get(i));
        }

        Collections.sort(genreList, new StudentDateComparator());



    }
    class StudentDateComparator implements Comparator<CardData> {
        public int compare(CardData s1, CardData s2) {
            return  Float.valueOf(s2.content.siblingOrder).compareTo(Float.valueOf(s1.content.siblingOrder));
        }
    }

    private void initRequestCalls() {

        //Device registration call
        DeviceRegistration.Params deviceRegparams = new DeviceRegistration.Params(mContext.getResources().getString(R.string.clientSecret));

        deviceRegistration = new DeviceRegistration(DebugActivity.this, deviceRegparams,
                new APICallback<DeviceRegData>() {
                    @Override
                    public void onResponse(APIResponse<DeviceRegData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if(null == response) {
                            mStatus.setText("Status: Sorry some error occurred");
                        return;
                        }
                        if(null == response.body()){
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                            mStatus.setText("status: clientKey: "
                                    + response.body().clientKey
                                    + ", expiresAt: " + response.body().expiresAt
                                    + ", deviceId: " + response.body().deviceId
                                    + ", message: " + response.body().message);
                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        mStatus.setText("dev reg status" + t);
                    }
                });


        //Content list call
        RequestContentList.Params contentListparams = new RequestContentList.Params(APIConstants.TYPE_LIVE,1,20,"english","");

        contentList = new RequestContentList(contentListparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if(null == response) {
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        if(null == response.body()){
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                            mStatus.setText("message: " + response.body().message + " size " + response.body().results.size());

                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        mStatus.setText("contentList status" + t);
                    }
                });

        SignUp.Params signUpParams = new SignUp.Params("testemail@apalya.com","Apalya01",
                "Apalya01");

        signUP = new SignUp(signUpParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if(null == response) {
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        if(null == response.body()){
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                            mStatus.setText("message: " + response.message() + " status "
                                    + response.body().status);
                    }


                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        mStatus.setText("contentList status" + t);
                    }
                });

        Login.Params loginParams = new Login.Params("testemail@apalya.com","Apalya01");

        login = new Login(loginParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if(null == response) {
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        if(null == response.body()){
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                            mStatus.setText("message: " + response.message() + " status "
                                    + response.body().status);

                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        mStatus.setText("contentList status" + t);
                    }
                });

        //EPG list call
        EPGList.Params epgListparams = new EPGList.Params("2015-12-14T10:00:00Z","epgstatic","mdpi",10,1,"siblingOrder","","","","", ApplicationController.iS_CIRCLE_BASED_REQ, PrefUtils.getInstance().getPrefEnablePastEpg(),false);

        epgList = new EPGList(epgListparams,
                new APICallback<CardResponseData>() {
                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if(null == response) {
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        if(null == response.body()){
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        mStatus.setText("message: " + response.message() + " status "
                                + response.body().status);
                        List<CardData>list =  response.body().results;

                        String title = list.get(0).generalInfo.title;



                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        mStatus.setText("contentList status" + t);
                    }
                });


        LocationInfo locationInfo = myplexAPISDK.getLocationInfo();
        MediaLinkEncrypted.Params mediaLinkparams = new MediaLinkEncrypted.Params("201", SDKUtils
                .getInternetConnectivity(this),null, locationInfo);

        medialLink = new MediaLinkEncrypted(mediaLinkparams,
                new APICallback<CardResponseData>() {

                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if(null == response) {
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        if(null == response.body()){
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        if(response.body().code == 402){
                            PrefUtils.getInstance().setPrefLoginStatus("");
                            return;
                        }
                            mStatus.setText("message: " + response.body().message + " size " + response.body().results.size());
                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        mStatus.setText("contentList status" + t);
                    }
                });
        final String _id = "201";
        ContentDetails.Params contentDetailsParams = new ContentDetails.Params(_id,"mdpi",
                "coverposter",10);

        contentDetails = new ContentDetails(contentDetailsParams,
                new APICallback<CardResponseData>() {

                    @Override
                    public void onResponse(APIResponse<CardResponseData> response) {
                        //Log.d(TAG, "success: " + response.body());
                        if(null == response) {
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        if(null == response.body()){
                            mStatus.setText("Status: Sorry some error occurred");
                            return;
                        }
                        mStatus.setText("message: " + response.body().message + " size " + response.body().results.size());
                        if(null != mContext && !((Activity)mContext).isFinishing()){
                            Intent contentDetailsActivity = new Intent(mContext,CardDetailsActivity.class);
                            if(null != response.body().results
                                    && response.body().results.size() > 0){
                                for (CardData cardData : response.body().results) {
                                    if (cardData._id.equalsIgnoreCase(_id)) {
                                        contentDetailsActivity.putExtra(CardDetails
                                                .PARAM_CARD_ID, cardData._id);
                                        mContext.startActivity(contentDetailsActivity);
                                    }
                                }
                            }else{
                                mStatus.setText("Status: "+response.body().message);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t,int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        mStatus.setText("contentList status" + t);
                    }
                });

    }

    @Override
    public void setOrientation(int value) {

    }

    @Override
    public int getOrientation() {
        return 0;
    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void showActionBar() {

    }

    private  String getMCCAndMNCValues(){
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        String codes ="";

        if (!networkOperator.isEmpty()) {
            int mcc = Integer.parseInt(networkOperator.substring(0, 3));
            int mnc = Integer.parseInt(networkOperator.substring(3));
            codes = mcc+","+mnc;
            String bcc ="";
            String[] a = bcc.split(",");
            System.out.println("a "+a);

        }

        return codes;
    }

}
