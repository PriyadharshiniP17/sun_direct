package com.myplex.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.myplex.model.RemoteConfigDomainPlayerEvents;
import com.myplex.util.PrefUtils;

import java.util.List;

public class RemoteConfigUtils {

    private static String metaDomain;
    private static String playerEventsMetaDomain;
    private static String metaDomainScheme;
    private static String playerEventsMetaDomainScheme;
    private static String mediaDomain;
    private static String mediaDomainScheme;



    public static String[] getHostForAPI(String apiCall){

        //Check for MediaLink first
        if(isMediaLinkCall(apiCall)){
            if (PrefUtils.getInstance().getBoolean(APIConstants.SHOULD_ENABLE_MEDIA_DOMAIN_API, true)) {
                mediaDomain = APIConstants.MEDIA_DOMAIN ;
                mediaDomainScheme = APIConstants.MEDIA_SCHEME;
                return new String[] {mediaDomain,mediaDomainScheme};
            } else {
                //Null is handled in the parent class
                return null;
            }
        }
        if(haveToUsePlayerAPI(apiCall)){
            return new String[] {playerEventsMetaDomain,playerEventsMetaDomainScheme};
        }
        else if(haveToUseMetaAPI(apiCall)){
            return new String[] {metaDomain,metaDomainScheme};
        }
        return null;
    }


    private static boolean isSearchFilterCall(String apiCall) {
        return apiCall.toLowerCase().contains("/content/v7/search/");
    }

    private static boolean isMediaLinkCall(String apiCall) {
        return apiCall.toLowerCase().contains("/media");
    }


    private static boolean haveToUseMetaAPI(String apiCall) {

        //Adding a try-catch block so that is there is any exception in the Json it will use the Default one
        RemoteConfigDomainPlayerEvents events =
                null;
        try {
            events = new Gson()
                    .fromJson(myplexAPISDK.getmRemoteConfig().getString(APIConstants.DOMAIN_META)
                            , RemoteConfigDomainPlayerEvents.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return false;
        }

        try {
            if (events != null
                    && events.ignore != null
                    && events.ignore.size() > 0) {
                for(String ignoreAPI : events.ignore){
                    if(apiCall.toLowerCase().contains(ignoreAPI.toLowerCase())){
                        metaDomain = null;
                        metaDomainScheme = null;
                        return true;
                    }
                }
            }
            if (events != null
                    && events.apis != null
                    && events.apis.size() > 0) {
                for(String api : events.apis){
                    if(apiCall.toLowerCase().contains(api.toLowerCase())){
                        metaDomain = events.host;
                        metaDomainScheme = events.scheme;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean haveToUsePlayerAPI(String apiCall) {

        //Adding a try-catch block so that is there is any exception in the Json it will use the Default one
        RemoteConfigDomainPlayerEvents events =
                null;
        try {
            events = new Gson()
                    .fromJson(myplexAPISDK.getmRemoteConfig().getString(APIConstants.DOMAIN_PLAYER_EVENTS)
                            , RemoteConfigDomainPlayerEvents.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return false;
        }


        try {
            if (events != null
                    && events.ignore != null
                    && events.ignore.size() > 0) {
                for(String ignoreAPI : events.ignore){
                    if(apiCall.toLowerCase().contains(ignoreAPI.toLowerCase())){
                        playerEventsMetaDomain = null;
                        playerEventsMetaDomainScheme = null;
                        return true;
                    }
                }
            }
            if (events != null
                    && events.apis != null
                    && events.apis.size() > 0) {
                for(String api : events.apis){
                    if(apiCall.toLowerCase().contains(api.toLowerCase())){
                        playerEventsMetaDomain = events.host;
                        playerEventsMetaDomainScheme = events.scheme;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
