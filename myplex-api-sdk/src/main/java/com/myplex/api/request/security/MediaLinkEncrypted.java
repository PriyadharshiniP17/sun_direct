package com.myplex.api.request.security;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CardResponseData;
import com.myplex.model.CardVideoResponseContainer;
import com.myplex.model.LocationInfo;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Apparao on 10/12/18.
 */

public class MediaLinkEncrypted extends APIRequest {


    private static final String TAG = MediaLinkEncrypted.class.getSimpleName();

    private Params params;

    public static class Params {
        String network;
        String mcc;
        String mnc;
        String fields;
        String contentId;
        JSONObject jsonObject;
        String nId;
        String profile;
        String startDate;
        String endDate;
        LocationInfo locationInfo;
        String consumptionType;
        boolean isPartnerAppInstalled;
        String partnerAppVersion;
        String ad_id;

        public Params(String contentId,String fields ,String network,String mcc ,String mnc){
            this.contentId = contentId;
            this.fields = fields;
            this.network = network;
            this.mcc =mcc;
            this.mnc =mnc;
        }

        public Params(String contentId, String profile, String nId, LocationInfo locationInfo){
            this.contentId = contentId;
            this.profile = profile;
            this.nId = nId;
            this.locationInfo = locationInfo;
            //Log.d(TAG,"Params: contentId=" + contentId + " profile= " + profile + " nId= " + nId);
        }


        public Params(String contentId, String profile, String nId, LocationInfo locationInfo,boolean isPartnerAppInstalled){
            this.contentId = contentId;
            this.profile = profile;
            this.nId = nId;
            this.locationInfo = locationInfo;
            this.isPartnerAppInstalled = isPartnerAppInstalled;
            //Log.d(TAG,"Params: contentId=" + contentId + " profile= " + profile + " nId= " + nId);
        }

        public Params(String contentId, String profile, String nId, LocationInfo locationInfo,boolean isPartnerAppInstalled,String partnerAppVersion){
            this.contentId = contentId;
            this.profile = profile;
            this.nId = nId;
            this.locationInfo = locationInfo;
            this.isPartnerAppInstalled = isPartnerAppInstalled;
            this.partnerAppVersion = partnerAppVersion;
            //Log.d(TAG,"Params: contentId=" + contentId + " profile= " + profile + " nId= " + nId);
        }

        public Params(String contentId, String profile, String nId, LocationInfo locationInfo,String consumptionType){
            this.contentId = contentId;
            this.profile = profile;
            this.nId = nId;
            this.locationInfo = locationInfo;
            this.consumptionType = consumptionType;
            //Log.d(TAG,"Params: contentId=" + contentId + " profile= " + profile + " nId= " + nId);
        }

        public Params(String contentId, String profile, String nId, String startDate, String endDate, LocationInfo locationInfo){
            this.contentId = contentId;
            this.profile = profile;
            this.nId = nId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.locationInfo = locationInfo;
            //Log.d(TAG, "Params: contentId=" + contentId + " profile= " + profile + " nId= " + nId + " startDate= " + startDate + " endDate= " + endDate);
        }

        public void setAd_id(String ad_id){
            this.ad_id = ad_id;
        }

    }

    public MediaLinkEncrypted(Params params, APICallback mListener) {
        super(mListener);
        this.params = params;
    }
    @Override
    protected void execute(myplexAPI myplexAPI) {
        String clientKey = PrefUtils.getInstance().getPrefClientkey();
        //Log.d(TAG,"clientKey=" + clientKey);
        String fields = "videos,videoInfo,subtitles";
        String IMEINUMBER = PrefUtils.getInstance().getPrefDeviceid();
        final String num[] = IMEINUMBER.split(APIConstants.SLASH_EXP);
        final String key = APIConstants.splitPart1();
        final String key2 = APIConstants.splitPart4();
        String payload="";
        String encryptionKey = key2 + num[4].substring(4) + key;

        String country = null,postalCode = null,area = null;
        if (params != null && params.locationInfo != null) {
            if (!TextUtils.isEmpty(params.locationInfo.postalCode)) {
                postalCode = params.locationInfo.postalCode;
            }
            if (!TextUtils.isEmpty(params.locationInfo.country)) {
                country = params.locationInfo.country;
            }
            if (!TextUtils.isEmpty(params.locationInfo.area)) {
                area = params.locationInfo.area;
            }
        }

        String mccAndMNCValues = SDKUtils.getMCCAndMNCValues(myplexAPISDK.getApplicationContext());
        String mccValue = "";
        String mncValue = "";

        if (mccAndMNCValues != null && mccAndMNCValues.length() > 0) {
            String[] mccArray = mccAndMNCValues.split(",");
            mccValue = mccArray[0];
            mncValue = mccArray[1];
            params.mcc=mccValue;
            params.mnc=mncValue;
        }

        params.jsonObject = new JSONObject();
        try {
            params.jsonObject.put("fields",(params.fields==null)?fields:params.fields);
            params.jsonObject.put("network",params.network);
            params.jsonObject.put("mcc",params.mcc);
            params.jsonObject.put("mnc",params.mnc);
            params.jsonObject.put("postalCode", postalCode);
            params.jsonObject.put("area", area);
            params.jsonObject.put("country", country);
            params.jsonObject.put("nid", params.nId);
            params.jsonObject.put("startDate", params.startDate);
            params.jsonObject.put("endDate", params.endDate);
            params.jsonObject.put("nid", params.nId);
            if(params.isPartnerAppInstalled){
                params.jsonObject.put("partnerAppInstalled",String.valueOf(params.isPartnerAppInstalled));
            }
            if (!TextUtils.isEmpty(params.partnerAppVersion)) {
                params.jsonObject.put("partnerAppVersion", params.partnerAppVersion);
            }
            if (!TextUtils.isEmpty(params.ad_id)) {
                params.jsonObject.put("ad_id", params.ad_id);
            }
            params.jsonObject.put("consumptionType", params.consumptionType);
            params.jsonObject.put("Cache-Control", APIConstants.HTTP_NO_CACHE);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            SDKLogger.debug("EncryptionKEY-"+key2+num[4].substring(4)+key);
            SDKLogger.debug("payload..."+params.jsonObject.toString());
            payload= APIEncryption.encryptBase64(params.jsonObject.toString(), encryptionKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO: changed here
        /*Call<CardVideoResponseContainer> mediaLinkCall = myplexAPI.getInstance().myplexAPIService
                .mediaLink(clientKey,params.contentId);*/
        Call<CardVideoResponseContainer> mediaLinkCall;
     /*   if(params.startDate != null && params.endDate != null) {
            mediaLinkCall = myplexAPI.getInstance().myplexAPIService.mediaLink(clientKey,params.contentId, payload,params.startDate, params.endDate,1);
        } else*/
        List<String> subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
        String packLanguage = "";
        if(subscribed_languages != null && subscribed_languages.size() > 0 && subscribed_languages.get(0) != null)
            packLanguage = subscribed_languages.get(0);
        String appLanguage = PrefUtils.getInstance().getAppLanguageToSendServer();
            mediaLinkCall = myplexAPI.getInstance().myplexAPIService.mediaLink(clientKey,packLanguage, appLanguage,params.contentId,payload,1);
        mediaLinkCall.enqueue(new Callback<CardVideoResponseContainer>() {
            @Override
            public void onResponse(Call<CardVideoResponseContainer> call, Response<CardVideoResponseContainer> response) {
                APIResponse apiResponse = new APIResponse(response.body(), null);
                apiResponse.setSuccess(response.isSuccessful());
                CardVideoResponseContainer cardResponseData = response.body();
                Gson gson = new Gson();
                if (cardResponseData != null && cardResponseData.response != null) {
                    try {
                        String decryptedResponse = APIEncryption.decryptBase64(cardResponseData.response, key2 + num[4].substring(4) + key);
                        SDKLogger.debug("decryptedResponse- " + decryptedResponse);
                        CardResponseData cardResponseData1 = gson.fromJson(decryptedResponse, CardResponseData.class);
                        apiResponse = new APIResponse(cardResponseData1, null);
                        apiResponse.setMessage(cardResponseData1.message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("myTag2",/*gson.toJson(response)*/response.toString());
                }
                MediaLinkEncrypted.this.onResponse(apiResponse);
            }

            @Override
            public void onFailure(Call<CardVideoResponseContainer> call, Throwable t) {
                //Log.d(TAG, "Error :" + t.getMessage());
                t.printStackTrace();
                if (t instanceof UnknownHostException
                        || t instanceof ConnectException) {
                    MediaLinkEncrypted.this.onFailure(t, ERR_NO_NETWORK);
                    return;
                }
                MediaLinkEncrypted.this.onFailure(t, ERR_UN_KNOWN);
            }
        });
    }
}
