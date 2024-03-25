package com.myplex.api.request;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APIConstants;
import com.myplex.api.RemoteConfigUtils;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.CommonParams;
import com.myplex.model.PreferredLanguageItem;
import com.myplex.util.PrefUtils;

import java.io.IOException;
import java.util.List;

import io.github.inflationx.calligraphy3.BuildConfig;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class DynamicHostInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request httpRequest = chain.request();
        // add try catch
        try {

            Request requestToSendToServer;
            if (PrefUtils.getInstance().getBoolean(APIConstants.SHOULD_ENABLE_REMOTE_CONFIG_API, false)) {
                requestToSendToServer = changeTheHost(httpRequest);
            } else if (PrefUtils.getInstance().getBoolean(APIConstants.SHOULD_ENABLE_MEDIA_DOMAIN_API, false)) {
                requestToSendToServer = changeMediaLinkHost(httpRequest);
            }
            else {
                if(null!=PrefUtils.getInstance().getCommonParamsData()) {
                    requestToSendToServer = changeTheQueryParam(httpRequest);
                }else {
                    requestToSendToServer = getTheDefaultRequest(httpRequest);
                }
            }
            if(isLoginService(httpRequest)){
                requestToSendToServer = getHttpLoginRequest(httpRequest);
            }
            return chain.proceed(requestToSendToServer);

        }catch (Exception e){
            e.printStackTrace();
        }
        /*
        CODE to add a queryParam Dynamically for every Request.
        This can also be moved to RemoteConfigUtils to make it request Specific.
        Request request = chain.request();
        HttpUrl url = request.url().newBuilder().addQueryParameter("key","value").build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
        */
        return chain.proceed(httpRequest);
    }

    private Request getHttpLoginRequest(Request httpRequest) {
        Request request = null;
        try {
            String apiCall = httpRequest.url().uri().toString();
            request = getTheDefaultRequest(httpRequest);
            String protocol = APIConstants.LOGIN_SCHEME;
            String hostUrl =  APIConstants.BASE_URL;
            if (hostUrl != null && protocol != null) {

                HttpUrl newUrl = httpRequest.url().newBuilder()
                        .scheme(protocol)
                        .host(hostUrl)
                        .build();
                request = request.newBuilder()
                        .url(newUrl)
                        .build();
                return request;
            }
        }catch (Exception e){
            request = getTheDefaultRequest(httpRequest);
            e.printStackTrace();
        }
        return request;
    }

    private boolean isLoginService(Request httpRequest) {
        String apiCall = httpRequest.url().uri().toString();
        if(apiCall.toLowerCase().contains("/user/sigin")){
            return true;
        }
        return false;
    }


    private Request changeMediaLinkHost(Request httpRequest) {
        String apiCall = httpRequest.url().uri().toString();
        if(apiCall.toLowerCase().contains("/media")){
            return changeTheHost(httpRequest);
        }else{
            return getTheDefaultRequest(httpRequest);
        }
    }


    private Request changeTheHost(Request httpRequest) {
        Request request = null;
        try{
        String apiCall = httpRequest.url().uri().toString();
        String[] hostAndScheme = RemoteConfigUtils.getHostForAPI(apiCall);
        String host = null;
        String scheme = null;
        if (hostAndScheme != null && hostAndScheme.length > 1) {
            host = hostAndScheme[0];
            scheme = hostAndScheme[1];
        }
        request = getTheDefaultRequest(httpRequest);
        if (host != null && scheme != null) {

                HttpUrl newUrl = httpRequest.url().newBuilder()
                        .scheme(scheme)
                        .host(host)
                        .build();
                request = request.newBuilder()
                        .url(newUrl)
                        .build();
                return request;
            }
        }catch (Exception e){
            request = getTheDefaultRequest(httpRequest);
            e.printStackTrace();
        }
        return request;
    }
    private Request changeTheQueryParam(Request httpRequest) {
        Request request = null;
        try{
        request = getTheDefaultRequest(httpRequest);
        if(PrefUtils.getInstance().getCommonParamsData()!=null&&PrefUtils.getInstance().getCommonParamsData().size()>0){
            List<CommonParams> commonParas = PrefUtils.getInstance().getCommonParamsData();
            if(commonParas!=null){
                for (CommonParams commonParameters : commonParas) {
                        if (commonParameters.type.equalsIgnoreCase("parameter")) {
                            HttpUrl newUrl = httpRequest.url().newBuilder()
                                    .addQueryParameter(commonParameters.key, commonParameters.value)
                                    .build();
                            httpRequest = httpRequest.newBuilder()
                                    .url(newUrl)
                                    .build();
                        }
                }
            }}

        }catch (Exception e){
            request = getTheDefaultRequest(httpRequest);
            e.printStackTrace();
        }
        return request;
    }


    //The Default Request that we send to server with remoteConfig disabled
    private Request getTheDefaultRequest(Request httpRequest) {
        Request.Builder requestBuilder;
        ///staging
            ///   requestBuilder = httpRequest.newBuilder().header("x-myplex-tenant-id", "b378dd52-c91d-46df-b1ad-8b42b22317c6");
        ////production
        ///requestBuilder = httpRequest.newBuilder().header("x-myplex-tenant-id", "788e9713-8edc-4891-a395-158371be7a8d");
        ////shreyas production
        ///requestBuilder = httpRequest.newBuilder().header("x-myplex-tenant-id", "1186b7f4-1391-4b59-bfd0-97e609de359f");
        requestBuilder = httpRequest.newBuilder().header("x-myplex-tenant-id", APIConstants.TENANT_ID);
        requestBuilder.addHeader("X-myplex-platform", "Android");
        if(PrefUtils.getInstance().getCommonParamsData()!=null){
            List<CommonParams> commonParas = PrefUtils.getInstance().getCommonParamsData();
            if(commonParas!=null){
                for (CommonParams commonParameters : commonParas) {
                    if (commonParameters.type.equalsIgnoreCase("header")) {
                        requestBuilder.addHeader(commonParameters.key, commonParameters.value);
                    }
                }}}


        if(!TextUtils.isEmpty(PrefUtils.getInstance().getForceAcceptLanguage())){
            requestBuilder.addHeader("Accept-Language", PrefUtils.getInstance().getForceAcceptLanguage());
        }else {
            requestBuilder.addHeader("Accept-Language", PrefUtils.getInstance().getAppLanguageToSendServerInStringFormat());
        }
        if(!TextUtils.isEmpty(PrefUtils.getInstance().getServiceName())){
            requestBuilder.addHeader(APIConstants.SERVICE_NAME, PrefUtils.getInstance().getServiceName());
        }else{
            requestBuilder.addHeader(APIConstants.SERVICE_NAME, PrefUtils.getInstance().getDefaultServiceName());
        }// add try catch
        try {
            List<PreferredLanguageItem> items = PrefUtils.getInstance().getPreferredLanguageItems();
            String preferredLanguages = "";
            if (PrefUtils.getInstance().getPreferredLanguageItems() != null) {
                for (int i = 0; i < items.size(); i++) {
                    preferredLanguages = (i != items.size() - 1) ? preferredLanguages + items.get(i).getTerm() + "," : preferredLanguages + items.get(i).getTerm();
                }
            }
            if (!TextUtils.isEmpty(preferredLanguages))
                requestBuilder.addHeader("contentLanguage", preferredLanguages);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PackageManager packageManager = myplexAPISDK.getApplicationContext().getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(
                    myplexAPISDK.getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        requestBuilder.addHeader("X-myplex-AppVersion", String.valueOf(packageInfo.versionCode));
        Log.e("URL : " , httpRequest.url() + "");
        return requestBuilder.build();
    }


}
