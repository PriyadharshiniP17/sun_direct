package com.myplex.api;


import static java.util.Collections.emptySet;

import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chuckerteam.chucker.api.ChuckerCollector;
import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.myplex.api.request.DynamicHostInterceptor;
import com.myplex.api.request.content.PlayerEventsRequest;
import com.myplex.model.AvailableLoginsPropertiesData;
import com.myplex.model.BaseResponseData;
import com.myplex.model.Bundle;
import com.myplex.model.CardDataCommentsItem;
import com.myplex.model.CardResponseData;
import com.myplex.model.CardVideoResponseContainer;
import com.myplex.model.CarouselInfoResponseData;
import com.myplex.model.ChannelsCatchupEPGResponseData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.model.ConnectionResponseData;
import com.myplex.model.CountriesResponse;
import com.myplex.model.DeleteNotificationResponse;
import com.myplex.model.DeviceRegData;
import com.myplex.model.FavouriteSectionsListResponse;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.GenreFilterData;
import com.myplex.model.NotificationList;
import com.myplex.model.SectionsListResponse;
import com.myplex.model.ImageUploadResponse;
import com.myplex.model.LanguageListResponse;
import com.myplex.model.LanguageResponse;
import com.myplex.model.MySubscribedPacksResponseData;
import com.myplex.model.OTTAppData;
import com.myplex.model.OfferResponseData;
import com.myplex.model.PreferredLanguageData;
import com.myplex.model.PropertiesData;
import com.myplex.model.SMCLIstResponse;
import com.myplex.model.SignupResponseData;
import com.myplex.model.SocialLoginData;
import com.myplex.model.UserProfileResponseData;
import com.myplex.model.UserSigninResponse;
import com.myplex.model.ValuesResponse;
import com.myplex.model.VernacularResponse;
import com.myplex.model.VernacularResponseNew;
import com.myplex.model.VstbLoginSessionResponse;
import com.myplex.sdk.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by Apalya on 9/14/2015.
 */
public class myplexAPI {

    public static final String TAG = myplexAPI.class.getSimpleName();
    private static myplexAPI _self = null;
    private static final long SIZE_OF_CACHE = 10 * 1024 * 1024; // 10 MB
    private static final String CACHE_NAME = "cache";
    public Retrofit retrofit;
    public myplexAPIInterface myplexAPIService;
    /* public static String DEVICE_REG_SALT3 = "1ioA";
     */
    public static String DEVICE_REG_SALT3 = APIConstants.DEVICE_REG_SALT3;
    public static String DEVICE_REG_SALT1 = APIConstants.DEVICE_REG_SALT1;


    public static final boolean ENABLE_SUBSCRIPTIONS_ON_CG_SMS_FLOW = true;
    private static Cache cache = null;

    private myplexAPI() {
        init();
    }


    public static myplexAPI getInstance() {
        if (_self == null) {
            _self = new myplexAPI();
        }
        return _self;
    }

    private void init() {
        File httpCacheDirectory = new File(myplexAPISDK.getApplicationContext().getExternalCacheDir(), CACHE_NAME);
        try {
            cache = new Cache(httpCacheDirectory, SIZE_OF_CACHE);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Could not create Cache!", e);
        }
        CertificatePinner certPinner = new CertificatePinner.Builder()
                .add(APIConstants.BASE_URL,
                        "sha256/t3D2HTnLR5Pp+M+tyD6p/0HrunnNEyf+1PT4RTEB8LE=")
                .build();
        OkHttpClient.Builder client = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (BuildConfig.DEBUG) {
                client = new OkHttpClient.Builder()
                        .connectTimeout(APIConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .readTimeout(APIConstants.DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .writeTimeout(APIConstants.DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        //  .certificatePinner(certPinner)
                        .cache(cache)
                        .addInterceptor(new DynamicHostInterceptor())
                        .connectionPool(new ConnectionPool(5, 4 * 1000, TimeUnit.MINUTES));
            } else {
                client = new OkHttpClient.Builder()
                        .connectTimeout(APIConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .readTimeout(APIConstants.DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .writeTimeout(APIConstants.DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .certificatePinner(certPinner)
                        .cache(cache)
                        .addInterceptor(new DynamicHostInterceptor())
                        .connectionPool(new ConnectionPool(5, 4 * 1000, TimeUnit.MINUTES))
                ;
            }
        } else {
            if (BuildConfig.DEBUG) {
                client = new OkHttpClient.Builder()
                        .connectTimeout(APIConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .readTimeout(APIConstants.DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .writeTimeout(APIConstants.DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .cache(cache)
                       // .certificatePinner(certPinner)
                        .addInterceptor(new DynamicHostInterceptor())
                        .connectionPool(new ConnectionPool(5, 4 * 1000, TimeUnit.MINUTES))
                ;
            } else {
                client = new OkHttpClient.Builder()
                        .connectTimeout(APIConstants.DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .readTimeout(APIConstants.DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .writeTimeout(APIConstants.DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                        .cache(cache)
                        .certificatePinner(certPinner)
                        .addInterceptor(new DynamicHostInterceptor())
                        .connectionPool(new ConnectionPool(5, 4 * 1000, TimeUnit.MINUTES))
                ;
            }
        }


        if (BuildConfig.DEBUG) {
            client.addInterceptor(
                    new ChuckerInterceptor.Builder(myplexAPISDK.getApplicationContext())
                            .collector(new ChuckerCollector(myplexAPISDK.getApplicationContext()))
                            .maxContentLength(250000L)
                            .redactHeaders(emptySet())
                            .alwaysReadResponseBody(false)
                            .build());
        }
        //Log.d(TAG, "base url: " + APIConstants.SCHEME + APIConstants.BASE_URL);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        //TODO need to check a new Logging Lib
        //TODO not priority right now
        client.interceptors().add(logging);
        retrofit = new Retrofit.Builder()
                .baseUrl(APIConstants.SCHEME + APIConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build())
                .build();

        myplexAPIService = retrofit.create(myplexAPIInterface.class);
    }

    public static void clearCache(String urlString) {
        try {
            Iterator<String> it = cache.urls();

            while (it.hasNext()) {
                String next = it.next();

                if (next.contains(urlString)) {
                    it.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        _self = null;
    }

    public interface myplexAPIInterface {

        // https://api.myplex.com/user/v2/registerDevice
        @FormUrlEncoded
        @POST("/user/v2/registerDevice")
        Call<DeviceRegData> registerDevice(@Header("X-MSISDN") String HTTP_X_MSISDN,
                                           @Field("serialNo") String serialNo,
                                           @Field("os") String os,
                                           @Field("osVersion") String osVersion,
                                           @Field("make") String make,
                                           @Field("model") String model,
                                           @Field("resolution") String resolution,
                                           @Field("profile") String profile,
                                           @Field("clientSecret") String clientSecret);

        @FormUrlEncoded
        @POST("/user/v2/registerDevice")
        Call<DeviceRegData> registerDevice(@Field("serialNo") String serialNo,
                                           @Field("os") String os,
                                           @Field("osVersion") String osVersion,
                                           @Field("make") String make,
                                           @Field("model") String model,
                                           @Field("resolution") String resolution,
                                           @Field("profile") String profile,
                                           @Field("clientSecret") String clientSecret);


        //        http://169.38.74.50/custom/vfplay/v10/registerDevice
        @FormUrlEncoded
        @POST("/custom/vfplay/v10/registerDevice")
        Call<DeviceRegData> registerDeviceEncryptedPayLoad(@Field("payload") String payload);

        @FormUrlEncoded
        @POST("custom/vfplay/v10/user/sigin")
        Call<BaseResponseData> msisdnLoginEncrypted(@Header("clientKey") String clientKey,
                                                    @Field("payload") String payload);

        // https://api.myplex.com/user/v2/signUp https://api.myplex.com/user/v2/signUp
        @FormUrlEncoded
        @POST("/user/v2/signUp")
        Call<BaseResponseData> signUp(@Header("clientKey") String clientKey,
                                      @Field("email") String email,
                                      @Field("password") String password,
                                      @Field("password2") String password2,
                                      @Field("profile") String profile);

        @FormUrlEncoded
        @POST("/ott/v1/connection_details/")
        Call<ConnectionResponseData> connectionDetails(@Header("clientKey") String clientKey,
                                                       @Field("name") String name,
                                                       @Field("mobile") String mobile,
                                                       @Field("email") String email,
                                                       @Field("pincode") String pincode,
                                                       @Field("connection") String connection);

        // https://api.myplex.com/user/v2/signIn
        @FormUrlEncoded
        @POST("/user/v2/signIn")
        Call<BaseResponseData> logIn(@Header("clientKey") String clientKey,
                                     @Field("userid") String userid,
                                     @Field("password") String password,
                                     @Field("profile") String profile);

        @FormUrlEncoded
        @POST("/user/v2/social/login")
        Call<BaseResponseData> SSOLogInRequest(@Header("clientKey") String clientKey,
                                               @Field("idToken") String idToken,
                                               @Field("authToken") String accessToken,
                                               @Field("tokenExpiry") String tokenExpiry);

        @FormUrlEncoded
        @POST("/user/v2/social/login")
        Call<BaseResponseData> SSOLogInRequest(@Header("clientKey") String clientKey,
                                               @Field("accessToken") String accessToken);

        @FormUrlEncoded
        @POST("/user/v2/forgotPassword")
        Call<BaseResponseData> forgotPassword(@Header("clientKey") String clientKey,
                                              @Field("mobile") String email);

        @FormUrlEncoded
        @POST("/user/v2/profile/mobileNumberUpdate/")
        Call<BaseResponseData> changeMobileNumber(@Header("clientKey") String clientKey,
                                              @Field("mobile") String mobile);

        @FormUrlEncoded
        @POST("/user/v2/profile/mobileNumberUpdate/")
        Call<BaseResponseData> changeMobileNumber(@Header("clientKey") String clientKey,
                                                  @Field("mobile") String mobile, @Field("otp") String otp);

        @FormUrlEncoded
        @POST("/user/v2/profile/mobileNumberUpdate/")
        Call<BaseResponseData> changeNewMobileNumber(@Header("clientKey") String clientKey,
                                                  @Field("mobile") String mobile, @Field("newMobile") String newMobileNumber);

        @FormUrlEncoded
        @POST("/user/v2/profile/mobileNumberUpdate/")
        Call<BaseResponseData> changeNewMobileNumber(@Header("clientKey") String clientKey,
                                                     @Field("mobile") String mobile, @Field("newMobile") String newMobileNumber,@Field("newOtp") String otp );

        @FormUrlEncoded
        @POST("/user/v2/forgotPassword")
        Call<BaseResponseData> mobileForgotPasswordOtpRequest(@Header("clientKey") String clientKey,
                                                              @Field("mobile") String mobile,
                                                              @Field("otp") String otp,
                                                              @Field("otpValidation") Boolean otpValidation);

        @FormUrlEncoded
        @POST("/user/v2/forgotPassword")
        Call<BaseResponseData> mobileForgotPasswordChangeRequest(@Header("clientKey") String clientKey,
                                                     @Field("mobile") String mobile,
                                                     @Field("otp") String otp,
                                                     @Field("otpValidation") Boolean otpValidation,
                                                     @Field("password") String password);

        @FormUrlEncoded
        @POST("/user/v2/changePassword")
        Call<BaseResponseData> updatePassword(@Header("clientKey") String clientKey,
                                              @Field("currentPassword") String currentPassword,
                                              @Field("newPassword") String newPassword);


        // orderBy=releasedate
        @GET("/content/v5/contentList")
        Call<CardResponseData> contentList(@Header("clientKey") String clientKey,
                                           @Query("type") String contentType,
                                           @Query("level") String level,
                                           @Query("fields") String fields,
                                           @Query("startIndex") int startIndex,
                                           @Query("count") int count,
                                           @Query("language") String language,
                                           @Query("genre") String genre,
                                           @Query("orderBy") String orderBy,
                                           @Query("publishingHouseId") String publishingHouseId,
                                           @Query("tags") String tags);

        // http://api-beta.myplex.in/epg/v2/epgList
        @GET("/epg/v2/epgList")
        Call<CardResponseData> epgListWithDynamicParams(@Header("clientKey") String clientKey,
                                                        @QueryMap Map<String, String> options);


        // http://api-beta.myplex.in/epg/v2/epgList
        @GET("/epg/v2/epgList")
        Call<CardResponseData> epgList(@Header("clientKey") String clientKey,
                                       @Query("startDate") String startDate,
                                       @Query("level") String level,
                                       @Query("imageProfile") String imageProfile,
                                       @Query("count") int count,
                                       @Query("startIndex") int startIndex,
                                       @Query("orderBy") String orderBy,
                                       @Query("sections") String genre,
                                       @Query("language") String language,
                                       @Query("publishingHouseId") String publishingHouseId);

        // http://api-beta.myplex.in/epg/v2/epgList
        @GET("/epg/v2/epgList")
        Call<CardResponseData> epgCircleList(@Header("clientKey") String clientKey,
                                             @Query("startDate") String startDate,
                                             @Query("level") String level,
                                             @Query("imageProfile") String imageProfile,
                                             @Query("count") int count,
                                             @Query("startIndex") int startIndex,
                                             @Query("orderBy") String orderBy,
                                             @Query("sections") String genre,
                                             @Query("language") String language,
                                             @Query("mcc") String mcc,
                                             @Query("mnc") String mnc,
                                             @Query("publishingHouseId") String publishingHouseId);


        // http://api-beta.myplex.in/epg/v2/epgList
        @GET("/epg/v2/epgList")
        Call<CardResponseData> epgCircleListWithPastEPG(@Header("clientKey") String clientKey,
                                                        @Query("startDate") String startDate,
                                                        @Query("level") String level,
                                                        @Query("imageProfile") String imageProfile,
                                                        @Query("count") int count,
                                                        @Query("startIndex") int startIndex,
                                                        @Query("orderBy") String orderBy,
                                                        @Query("sections") String genre,
                                                        @Query("language") String language,
                                                        @Query("mcc") String mcc,
                                                        @Query("mnc") String mnc,
                                                        @Query("dvr") String dvr,
                                                        @Query("publishingHouseId") String publishingHouseId);

        // http://api-beta.myplex.in/epg/v2/epgList
        @GET("/epg/v2/epgList")
        Call<CardResponseData> epgListWithPastEpg(@Header("clientKey") String clientKey,
                                                  @Query("startDate") String startDate,
                                                  @Query("level") String level,
                                                  @Query("imageProfile") String imageProfile,
                                                  @Query("count") int count,
                                                  @Query("startIndex") int startIndex,
                                                  @Query("orderBy") String orderBy,
                                                  @Query("sections") String genre,
                                                  @Query("language") String language,
                                                  @Query("dvr") String dvr,
                                                  @Query("publishingHouseId") String publishingHouseId);


        // http://api-beta.myplex.in/epg/v2/channelEPG/<contentId>
        @GET("/epg/v2/channelEPG/{contentId}")
        Call<CardResponseData> channelEPG(@Header("clientKey") String clientKey,
                                          @Path("contentId") String contentId,
                                          @Query("date") String date,
                                          @Query("level") String level,
                                          @Query("imageProfile") String imageProfile,
                                          @Query("count") int count,
                                          @Query("startIndex") int startIndex);

        @GET("/epg/v2/channelsEPG/{contentId}")
        Call<ChannelsEPGResponseData> channelListEPG(@Header("clientKey") String clientKey,
                                                     @Path("contentId") String contentId,
                                                     @Query("date") String date,
                                                     @Query("count") int count,
                                                     @Query("startIndex") int startIndex,
                                                     @Query("channelEpg") boolean channelEpg,
                                                     @Query("currentProgram") boolean currentProgram);

        @GET("/epg/v2/channelsEPG/{contentId}")
        Call<ChannelsEPGResponseData> channelListEPG(@Header("clientKey") String clientKey,
                                                     @Path("contentId") String contentId,
                                                     @Query("channelEpg") boolean channelEpg,
                                                     @Query("currentProgram") boolean currentProgram);

        @GET("/epg/v2/epgCatchup/{contentId}")
        Call<ChannelsCatchupEPGResponseData> epgCatchup(@Header("clientKey") String clientKey,
                                                        @Path("contentId") String contentId,
                                                        @Query("period") String date);


        // https://api.myplex.com/content/v3/contentDetail/1952/?clientKey=11b84c7672b9fd420b81d5f7725fb171b308db1734cb552d0ae2993487f9ab7c&fields=videos,videoInfo
        //        for past epg channels &startDate=2016-08-26T06:00:00.000Z&endDate=2016-08-26T07:00:00.000Z
        @GET("/content/v3/contentDetail/{contentId}")
        Call<CardResponseData> mediaLink(@Path("contentId") String contentId,
                                         @Header("clientKey") String clientKey,
                                         @Query("fields") String fields,
                                         @Query("network") String network,
                                         @Query("nid") String nid,
                                         @Query("startDate") String startDate,
                                         @Query("endDate") String endDate,
                                         @Query("postalCode") String postalCode,
                                         @Query("country") String country,
                                         @Query("area") String area,
                                         @Query("mcc") String mcc,
                                         @Query("mnc") String mnc,
                                         @Query("consumptionType") String consumptionType,
                                         @Header("Cache-Control") String cacheControl);


        /*https://api.myplex.com/content/v2/contentDetail/1952/
        ?clientKey=500705600bfdde0528c9ff83cee566ad7c3a5625775dfd428fa6a93201a3cea1
        &count=10
        &fields=user/currentdata,images,generalInfo,contents,comments,reviews/user,_id,relatedMedia,packages,relatedCast,dynamicMeta,_lastModifiedAt,_expiresAt,matchInfo,globalServiceId
        &imageType=coverposter
        &imageProfile=mdpi
        &fields=user/currentdata,packages*/
        @GET("/content/v3/contentDetail/{contentId}")
        Call<CardResponseData> contentDetails(@Path("contentId") String contentId,
                                              @Header("clientKey") String clientKey,
                                              @Query("fields") String fields,
                                              @Query("imageProfile") String imageProfile,
                                              @Query("imageType") String imageType,
                                              @Query("count") int count,
                                              @Query("mcc") String mcc,
                                              @Query("mnc") String mnc,
                                              @Header("Cache-Control") String cacheControl);

        @GET("/content/v3/contentDetail/{contentId}")
        Call<CardResponseData> contentDetails(@Path("contentId") String contentId,
                                              @Header("clientKey") String clientKey,
                                              @Query("fields") String fields,
                                              @Query("imageProfile") String imageProfile,
                                              @Query("imageType") String imageType,
                                              @Query("count") int count,
                                              @Query("mcc") String mcc,
                                              @Query("mnc") String mnc);

        @GET("content/v2/package/{packageId}/")
        Call<Bundle> bundleRequest(@Path("packageId") String packageId,
                                   @Header("clientKey") String clientKey);

        @GET("user/v2/web-view/")
        Call<BaseResponseData> getUserConsentUrl(@Query("type") String type,
                                                 @Header("clientKey") @Nullable String clientKey);


        @FormUrlEncoded
        @POST("/user/v2/billing/subscribe/")
        Call<BaseResponseData> subscriptionRequest(@Header("clientKey") String clientKey,
                                                   @Query("contentId") String contentIdPath,
                                                   @Query("paymentChannel") String paymentChannelPath,
                                                   @Query("packageId") String packageIdPath,
                                                   @Field("contentId") String contentId,
                                                   @Field("paymentChannel") String paymentChannel,
                                                   @Field("packageId") String packageId,
                                                   @Field("mobile") String mobile,
                                                   @Field("operator") String operator);

        @GET("/user/v2/package/{packageId}/")
        Call<BaseResponseData> msisdnRequest(@Path("packageId") String packageId,
                                             @Header("clientKey") String clientKey);

        @FormUrlEncoded
        @POST("/user/v2/generateKey")
        Call<DeviceRegData> generateKeyRequest(@Header("clientKey") String clientKey,
                                               @Field("deviceId") String packageId);


        //        http://api-beta.myplex.in/user/v2/events/mou/update/
        @GET("/user/v2/events/mou/update/")
        Call<BaseResponseData> mouUpdateRequest(@Header("clientKey") String clientKey,
                                                @Query("contentId") String contentId,
                                                @Query("elapsedTime") long elapsedTime,
                                                @Query("timeStamp") long timeStamp,
                                                @Query("network") String network,
                                                @Query("consumtionType") String consumtionType,
                                                @Query("nid") String nid,
                                                @Query("bytes") long bytes,
                                                @Query("averageBitrate") float averageBitrate,
                                                @Query("connectionSpeed") long bandWidth,
                                                @Query("weightedAverageBitrate") float weightedAverageBitrate,
                                                @Query("weightedConnectionSpeed") float weightedConnectionSpeed,
                                                @Query("videoStartupTime") long playbackStartUpTime,
                                                @Query("trackingId") String trackingId,
                                                @Query("bufferingRatio") int mBufferCount,
                                                @Query("sourceCarouselPosition") String sourceCarouselPosition,
                                                @Query("source") String mSource,
                                                @Query("sourceDetails") String mSourceDetails,
                                                @Query("sourceTab") String mSourceTab,
                                                @Query("platform") String platform,
                                                @Query("mediaSessionToken") String mediaSessionToken,
                                                @Query("os") String os);

        // https://api.myplex.com/user/v2/signIn
//        http://api-beta.myplex.in/user/v2/mobile/signIn
        @FormUrlEncoded
        @POST("/user/v2/mobile/signIn")
        Call<BaseResponseData> msisdnLogInRequest(@Header("X-MSISDN") String X_MSISDN,
                                                  @Header("clientKey") String clientKey,
                                                  @Field("mobile") String userid,
                                                  @Field("profile") String profile);

        @FormUrlEncoded
        @POST("/user/v3/mobile/signIn")
        Call<BaseResponseData> msisdnLogInRequestV3(@Header("clientKey") String clientKey,
                                                    @Field("mobile") String userid,
                                                    @Field("mode") String mode);

        @FormUrlEncoded
        @POST("/user/v3/mobile/signIn")
        Call<BaseResponseData> msisdnLogInRequestV3WithOtp(@Header("clientKey") String clientKey,
                                                           @Field("mobile") String userid,
                                                           @Field("mode") String mode,
                                                           @Field("otp") String otp);

        @FormUrlEncoded
        @POST("/user/v2/mobile/signIn")
        Call<BaseResponseData> maisonRetrievalLoginRequest(@Header("X-MOBILE-MSISDN") String X_MSISDN,
                                                           @Header("X-MOBILE-IMSI") String imsi,
                                                           @Header("clientKey") String clientKey,
                                                           @Field("profile") String profile);


        // https://api.myplex.com/user/v2/signIn
//        http://api-beta.myplex.in/user/v2/mobile/signIn
        @FormUrlEncoded
        @POST("/user/v2/mobile/signIn")
        Call<BaseResponseData> msisdnLogInRequest(@Header("clientKey") String clientKey,
                                                  @Field("mobile") String userid,
                                                  @Field("profile") String profile);
        //        https://api.myplex.com/content/v2/content/201/comments/?clientKey=256991c7239a3d50c6f09edce9e4e29985ad004c68c9dd72792cffb905079e03&count=20&startIndex=1
//        @GET("/user/v2/content/{contentId}/{fields}/")

        @GET("/content/v2/content/{contentId}/{fields}/")
        Call<ValuesResponse<CardDataCommentsItem>> commentsRequest(@Path("contentId") String contentId,
                                                                   @Path("fields") String fields,
                                                                   @Header("clientKey") String clientKey,
                                                                   @Query("count") int count,
                                                                   @Query("startIndex") int startIndex);

        @FormUrlEncoded
        @POST("/user/v2/content/{contentId}/{fields}/")
        Call<BaseResponseData> sendCommentMessage(@Path("contentId") String contentId,
                                                  @Header("clientKey") String clientKey,
                                                  @Path("fields") String fields,
                                                  @Field("comment") String comment,
                                                  @Field("clientKey") String clientKeyField);

        @FormUrlEncoded
        @POST("/user/v2/content/{contentId}/{fields}/")
        Call<BaseResponseData> sendCommentMessage(@Path("contentId") String contentId,
                                                  @Header("clientKey") String clientKey,
                                                  @Path("fields") String fields,
                                                  @Field("review") String review,
                                                  @Field("rating") int rating,
                                                  @Field("clientKey") String clientKeyField);

        @GET
        Call<CardResponseData> carouselRequest(@Url String url,
                                               @Header("contentLanguage") String contentLanguage,
                                               @Header("clientKey") String clientKey,
                                               @Header("packageLanguage") String packLanguage);

        // https://api.myplex.com/user/v2/signIn
        @GET("/ott/v1/appDetails/")
        Call<OTTAppData> ottAppRequest(@Header("clientKey") String clientKey,
                                       @Query("type") String contentType,
                                       @Query("version") int version);

        // https://api.myplex.com/user/v2/signIn
        @GET("/ott/v1/appDetails/")
        Call<OTTAppData> ottAppRequest(@Header("clientKey") String clientKey,
                                       @Query("type") String contentType,
                                       @Query("version") int version,
                                       @Header("Cache-Control") String nocache);

        //        https://api.myplex.com/content/v2/inlineSearch/?clientKey=42a42d9abb48f603acd14aea1461fa1e5f835002b9684393d54af1ef998d2982&query=A&level=dynamic&count=10
//        http://api-beta.myplex.in/content/v2/inlineSearch/?query=a&type=live&level=dynamic&count=10
        @GET("/content/v7/search/")
        Call<CardResponseData> inlineSearchRequest(@Header("clientKey") String clientKey,
                                                   @Query("query") String query,
                                                   @Query("type") String type,
                                                   @Query("level") String level,
                                                   @Query("fields") String fields,
                                                   @Query("count") int count,
                                                   @Query("searchFields") String searchFields,
                                                   @Query("startIndex") int startIndex,
                                                   @Query("publishingHouseId") String publishingHouseId);


        @GET("/content/v2/allFacates")
        Call<GenreFilterData> filterValuesRequest(@Header("clientKey") String clientKey,
                                                  @Query("type") String type);

        @GET("/user/v2/offers")
        Call<OfferResponseData> offeredPacksRequest(@Header("clientKey") String clientKey,
                                                    @Header("Cache-Control") String nocache

        );

        @GET("/user/v3/offers")
        Call<OfferResponseData> offeredPacksRequestV3(@Header("clientKey") String clientKey,
                                                      @Header("Cache-Control") String nocache

        );

        @GET("/user/v6/premium")
        Call<OfferResponseData> offeredPacksPremium(@Header("clientKey") String clientKey,
                                                    @Header("Cache-Control") String nocache

        );

        @GET("/user/v6/premium")
        Call<OfferResponseData> offeredPacksPremiumSubscribe(@Header("clientKey") String clientKey,
                                                             @Header("Cache-Control") String nocache,
                                                             @Query("packid") String packId
        );

        @GET("/user/v6/subscription/offers/")
        Call<OfferResponseData> offeredPacksRequestForDynamicAction(@Header("clientKey") String clientKey,
                                                                    @Header("Cache-Control") String nocache,
                                                                    @Query("source") String source,
                                                                    @Query("version") String paramApiVersion,
                                                                    @Query("contentId") String contentId);

        @GET("/user/v6/subscription/offers/")
        Call<OfferResponseData> offeredPacksRequestForDynamicActionWithMode(@Header("clientKey") String clientKey,
                                                                            @Header("Cache-Control") String nocache,
                                                                            @Query("source") String source,
                                                                            @Query("version") String paramApiVersion,
                                                                            @Query("contentId") String contentId,
                                                                            @Query("mode") String mode);


        //        http://169.38.117.74/epg/v2/programDetail/815_1453546800000?level=static&pretty=1
        @GET("/epg/v2/programDetail/{contentId}")
        Call<CardResponseData> programDetail(@Header("clientKey") String clientKey,
                                             @Path("contentId") String contentId,
                                             @Query("level") String level);

        @GET("user/v2/notification/registerDevice")
        Call<BaseResponseData> gcmIdRequest(@Header("clientKey") String clientKey,
                                            @Query("gcmId") String gcmId,
                                            @Query("appVersion") String appVersion,
                                            @Query("mcc") String mcc,
                                            @Query("mnc") String mnc

        );

        /*@GET("/content/v2/properties/all/")
        Call<PropertiesData> propertiesRequest(@Header("clientKey") String clientKey,
                                               @Query("appVersion") String appVersion,
                                               @Query("network") String network,
                                               @Query("mcc") String mcc,
                                               @Query("mnc") String mnc,
                                               @Header("Cache-Control") String cacheControl);*/

        @GET("/content/v2/properties/{clientSecret}")
        Call<PropertiesData> propertiesRequest(@Header("clientKey") String clientKey,
                                               @Path("clientSecret") String clientSecret,
                                               @Query("appVersion") String appVersion,
                                               @Query("network") String network,
                                               @Query("mcc") String mcc,
                                               @Query("mnc") String mnc
        );

        // http://169.38.74.50/content/v2/vods/GID_4001?pretty=1&level=devicemax
        @GET("/content/v3/vods/{contentId}")
        Call<CardResponseData> requestRelatedVODList(@Header("clientKey") String clientKey,
                                                     @Path("contentId") String contentId,
                                                     @Query("fields") String fields,
                                                     @Query("startIndex") int startIndex,
                                                     @Query("count") int count,
                                                     @Query("level") String level,
                                                     @Query("operator") String operator);

        @GET("/content/v3/vods/{contentId}")
        Call<CardResponseData> requestRelatedVODListWithOrderMode(@Header("clientKey") String clientKey,
                                                                  @Path("contentId") String contentId,
                                                                  @Query("fields") String fields,
                                                                  @Query("startIndex") int startIndex,
                                                                  @Query("count") int count,
                                                                  @Query("level") String level,
                                                                  @Query("operator") String operator,
                                                                  @Query("orderMode") String orderMode,
                                                                  @Query("orderBy") String orderBy);


        @GET("/content/v2/carousel/_info")
        Call<CarouselInfoResponseData> carouselInfoRequest(@Header("clientKey") String clientKey,
                                                           @Query("version") int version,
                                                           @Header("Cache-Control") String cacheControl,
                                                           @Header("contentLanguage") String contentLanguage,
                                                           @Header("packageLanguage") String packLanguage,
                                                           @Query("appLanguage") String appLanguage);

        @GET("/content/v2/carousel/{title}")
        Call<CardResponseData> carouselRequest(@Header("clientKey") String clientKey,
                                               @Path("title") String title,
                                               @Query("fields") String fields,
                                               @Query("count") int count,
                                               @Query("startIndex") int startIndex,
//                                               @Query("level") String level,
                                               @Query("mcc") String mcc,
                                               @Query("mnc") String mnc,
                                               @Header("Cache-Control") String cacheControl,
                                               @Query("serverPublishedTime") String modifiedOn,
                                               @Header("contentLanguage") String contentLanguage);


        @GET("/content/v2/carousel/{title}")
        Call<CardResponseData> carouselRequest(@Header("clientKey") String clientKey,
                                               @Path("title") String title,
                                               @Query("fields") String fields,
                                               @Query("count") int count,
                                               @Query("startIndex") int startIndex,
//                                               @Query("level") String level,
                                               @Query("mcc") String mcc,
                                               @Query("mnc") String mnc,
                                               @Query("serverPublishedTime") String modifiedOn);

        @GET("/content/v2/carousel/_info")
        Call<CarouselInfoResponseData> carouselInfoRequest(@Header("clientKey") String clientKey,
                                                           @Query("version") int version,
                                                           @Query("group") String group,
                                                           @Header("Cache-Control") String nocache,
                                                           @Header("contentLanguage") String contentLanguage,
                                                           @Header("packageLanguage") String packLanguage,
                                                           @Query("appLanguage") String appLanguage);

        @GET("/user/v2/profile")
        Call<UserProfileResponseData> userProfileRequest(@Header("clientKey") String clientKey, @Query("requestPackages") boolean requestPackages);

        @FormUrlEncoded
        @POST("/user/v2/partner/signIn")
        Call<BaseResponseData> partnerSignupRequest(@Header("clientKey") String clientKey,
                                                    @Field("partnerName") String partnerName);

        @GET("/user/v2/myPackages")
        Call<MySubscribedPacksResponseData> mySubscribedPacksRequest(@Header("clientKey") String clientKey,
                                                                     @Header("Cache-Control") String nocache);

        @FormUrlEncoded
        @POST("/user/v2/billing/unsubscribe")
        Call<BaseResponseData> unSubscribeRequest(@Header("clientKey") String clientKey,
                                                  @Field("packageId") String packageId,
                                                  @Field("operator") String operator);

        //        http://169.38.74.50/custom/vfplay/v1/hooqSessionLogin/1586/
// ?clientKey=3c8dee8d4ebef828efbd721379ae7aad4b46adc8b243c95c65f94f92401b449e&pretty=1&device_id=haskovhnsdjk.xgh&fields=videos
        @GET("/custom/vfplay/v1/hooqSessionLogin/{contentId}/")
        Call<VstbLoginSessionResponse> hooqSessionLoginRequest(@Header("clientKey") String clientKey,
                                                               @Path("contentId") String contentId,
                                                               @Query("device_id") String deviceId,
                                                               @Query("fields") String fields,
                                                               @Header("Cache-Control") String nocache,
                                                               @Query("postalCode") String postalCode,
                                                               @Query("country") String country,
                                                               @Query("area") String area,
                                                               @Query("mcc") String mcc,
                                                               @Query("mnc") String mnc);

        @FormUrlEncoded
        @POST("/custom/vfplay/v1/email/mobile/signIn/")
        Call<BaseResponseData> signInWithMsisdnAndEmailIDRequest(/*@Header("X-MSISDN") String X_MSISDN,*/
                @Field("mobile") String mobile,
                @Header("clientKey") String clientKey,
                @Field("email") String email,
                @Field("otp") String otp,
                @Header("Cache-Control") String cacheControl);


        @FormUrlEncoded
        @POST("/custom/vfplay/v10/email/mobile/signIn/")
        Call<BaseResponseData> otpSignInEncryptedPayLoad(/*@Header("X-MSISDN") String X_MSISDN,*/
                @Field("mobile") String mobile,
                @Header("clientKey") String clientKey,
                @Field("email") String email,
                @Field("otp") String otp,
                @Header("Cache-Control") String cacheControl);

        //        user/v2/events/player/{contentID}/updateStatus?action={Pause}&clientKey={clientKey}
        @FormUrlEncoded
        @POST("user/v2/events/player/{contentID}/updateStatus")
        Call<BaseResponseData> eventsPlayerStatusUpdateRequest(@Header("clientKey") String clientKey,
                                                               @Path("contentID") String contentID,
                                                               @Field("action") String action,
                                                               @Field("elapsedTime") int elapsedTime,
                                                               @Field("streamName") String streamName,
                                                               @Field("mediaSessionToken") String mediaSessionToken,
                                                               @Header("Cache-Control") String cacheControl);

        @GET("content/v2/similar/{contentId}/")
        Call<CardResponseData> similarContentRequest(@Header("clientKey") String clientKey,
                                                     @Path("contentId") String contentId,
                                                     @Query("level") String level,
                                                     @Query("count") int count,
                                                     @Query("fields") String fields);

        @FormUrlEncoded
        @POST("/custom/vfplay/v1/profile")
        Call<BaseResponseData> profileUpdateWithEmailRequest(@Header("clientKey") String clientKey,
                                                             @Field("email") String email,
                                                             @Header("Cache-Control") String cacheControl);

        @FormUrlEncoded
        @POST("/user/v2/smcNumbers")
        Call<SMCLIstResponse> getSMC(@Header("clientKey") String clientKey,
                                     @Field("mobile") String mobile);


        //        http://169.38.74.50/custom/vfplay/v1/hooqSessionLogin/1586/
// ?clientKey=3c8dee8d4ebef828efbd721379ae7aad4b46adc8b243c95c65f94f92401b449e&pretty=1&device_id=haskovhnsdjk.xgh&fields=videos
        //content/v2/similar/{contentId}/?clientKey=&level=&count

        @GET("content/v2/sectionsList/live")
        Call<SectionsListResponse> getSectionsList(@Header("clientKey") String clientKey);

        @GET("/user/v2/notifications")
        Call<NotificationList> getServiceNotifications(@Header("clientKey") String clientKey,@Query("startIndex") int startIndex, @Query("count") int count);

        @POST("/user/v2/notifications/{notificationId}/archive/")
        Call<DeleteNotificationResponse> getDeleteNotifications(@Header("clientKey") String clientKey,
                                                                @Path("notificationId") int notificationId);

        @POST("/user/v2/notifications/{notificationId}/viewed/")
        Call<DeleteNotificationResponse> geViewedNotifications(@Header("clientKey") String clientKey,
                                                                @Path("notificationId") int notificationId);

        @POST("/user/v2/notifications/archiveAll/")
        Call<DeleteNotificationResponse> getDeleteAllNotifications(@Header("clientKey") String clientKey);


        @GET("/user/v2/contentList/favourites/sections")
        Call<FavouriteSectionsListResponse> getFavouritesSectionsList(@Header("clientKey") String clientKey);

        @GET("user/v2/languages/")
        Call<LanguageListResponse> getLanguages(@Header("clientKey") String clientKey);

        // 	user/v2/content/{contentId}/favourite?clientKey=
//        http://169.38.74.50/user/v2/content/7564/favorite/?
        @FormUrlEncoded
        @POST("user/v2/content/{contentId}/favorite")
        Call<FavouriteResponse> favouriteRequest(@Header("clientKey") String clientKey,
                                                 @Path("contentId") String contentId,
                                                 @Field("contentType") String contentType,
                                                 @Field("clientKey") String clientKey1,
                                                 @Header("Cache-Control") String cacheControl);

        @FormUrlEncoded
        @POST("user/v2/content/{contentId}/watchlist")
        Call<FavouriteResponse> watchListRequest(@Header("clientKey") String clientKey,
                                                 @Path("contentId") String contentId,
                                                 @Field("contentType") String contentType,
                                                 @Field("clientKey") String clientKey1,
                                                 @Header("Cache-Control") String cacheControl);


        //        http://169.38.74.50/custom/vfplay/v1/analytics/events/?clientKey=30ba69deea04b53065dafa0006aa621ae56891c8ea0b97417f8427c6a2811604
        @FormUrlEncoded
        @POST("custom/vfplay/v1/analytics/events/")
        Call<BaseResponseData> analyticsEventsRequest(@Header("clientKey") String clientKey,
                                                      @Field("category") String category,
                                                      @Field("action") String action,
                                                      @Field("label") String label,
                                                      @Field("value") String value,
                                                      @Header("Cache-Control") String cacheControl);


        //        http://169.38.74.50/custom/vfplay/v1/analytics/events/?clientKey=30ba69deea04b53065dafa0006aa621ae56891c8ea0b97417f8427c6a2811604
        @FormUrlEncoded
        @POST("custom/vfplay/v1/playerLog")
        Call<BaseResponseData> playerLogRequest(@Header("clientKey") String clientKey,
                                                @Field("_id") String _id,
                                                @Field("title") String title,
                                                @Field("partnerId") String partnerId,
                                                @Field("partnerName") String partnerName,
                                                @Field("mediaUrl") String mediaUrl,
                                                @Field("action") String action,
                                                @Field("secDiff") long secDiff,
                                                @Field("platform") String platform,
                                                @Field("bitrate") float bitrate,
                                                @Field("resolution") String resolution,
                                                @Field("contentType") String contentType,
                                                @Header("Cache-Control") String cacheControl);


        //        http://169.38.74.50/custom/vfplay/v1/analytics/events/?clientKey=30ba69deea04b53065dafa0006aa621ae56891c8ea0b97417f8427c6a2811604
        @Headers("Content-Type:  application/json")
        @POST("custom/vfplay/v1/playerLog")
        Call<BaseResponseData> playerLogRequestWithJson(@Header("clientKey") String clientKey,
                                                        @Body List<PlayerEventsRequest.Params> listdata,
                                                        @Header("Cache-Control") String cacheControl);

        @GET("/user/v2/contentList/favorites")
        Call<CardResponseData> fetchFavouritesList(@Query("contentType") String contentType,
                                                   @Query("fields") String fields,
                                                   @Query("startIndex") int startIndex,
                                                   @Query("count") int count,
                                                   @Header("Cache-Control") String cacheControl,
                                                   @Header("clientKey") String clientKey,
                                                   @Query("level") String level);

        @GET("/user/v2/contentList/favorites")
        Call<CardResponseData> fetchFavouritesList(@Query("contentType") String contentType,
                                                   @Query("fields") String fields,
                                                   @Query("startIndex") int startIndex,
                                                   @Query("count") int count,
                                                   @Query("section") String genre,
                                                   @Header("Cache-Control") String cacheControl,
                                                   @Header("clientKey") String clientKey,
                                                   @Query("level") String level,
                                                   @Query("channelEpg") String channelEPG);

        // 	user/v2/content/{contentId}/favourite?clientKey=
//        http://169.38.74.50/user/v2/content/7564/favorite/?
        @GET("user/v2/content/{contentId}/favorite")
        Call<FavouriteResponse> favouriteCheckRequest(@Header("clientKey") String clientKey,
                                                      @Path("contentId") String contentId,
                                                      @Query("contentType") String contentType,
                                                      @Query("clientKey") String clientKey1,
                                                      @Header("Cache-Control") String cacheControl);


        @GET("/user/v2/contentList/watchlist")
        Call<CardResponseData> fetchWatchListList(@Query("contentType") String contentType,
                                                  @Query("fields") String fields,
                                                  @Query("startIndex") int startIndex,
                                                  @Query("count") int count,
                                                  @Header("Cache-Control") String cacheControl,
                                                  @Header("clientKey") String clientKey,
                                                  @Query("level") String level);

        // 	user/v2/content/{contentId}/favourite?clientKey=
//        http://169.38.74.50/user/v2/content/7564/favorite/?
        @GET("user/v2/content/{contentId}/watchlist")
        Call<FavouriteResponse> watchListCheckRequest(@Header("clientKey") String clientKey,
                                                      @Path("contentId") String contentId,
                                                      @Query("contentType") String contentType,
                                                      @Query("clientKey") String clientKey1,
                                                      @Header("Cache-Control") String cacheControl);

        //    http://169.38.74.50/content/v2/properties/loginsAvailable/ApalyaiOS
        @GET("/content/v2/properties/loginsAvailable/{clientSecrete}")
        Call<AvailableLoginsPropertiesData> loginsAvailable(@Path("clientSecrete") String clientSecrete,
                                                            @Header("Cache-Control") String cacheControl);


        @FormUrlEncoded
        @POST("custom/vfplay/v20/user/sigin")
        Call<BaseResponseData> msisdnLoginEncryptedV2(@Header("clientKey") String clientKey,
                                                      @Field("payload") String payload);

        @GET("/content/v3/media/{contentId}")
        Call<CardVideoResponseContainer> mediaLink(@Header("clientKey") String clientKey,
                                                   @Header("packageLanguage") String packLanguage,
                                                   @Header("contentLanguage") String contentLanguage,
                                                   @Path("contentId") String contentId,
                                                   @Query("payload") String payload,
                                                   @Query("version") int version);

        @GET("/content/v3/media/{contentId}")
        Call<CardVideoResponseContainer> mediaLink(@Header("clientKey") String clientKey,
                                                   @Path("contentId") String contentId,
                                                   @Query("payload") String payload,
                                                   @Header("contentLanguage") String contentLanguage,
                                                   @Query("startDate") String startDate,
                                                   @Query("endDate") String endDate,
                                                   @Query("version") int version);

        @POST("user/v2/unregisterDevice")
        Call<BaseResponseData> unregisterDevice(@Header("clientKey") String clientKey);

        @GET("custom/vfplay/v1/get_preferred_languages/")
        Call<PreferredLanguageData> preferredLanguagesRequest(@Header("clientKey") String clientKey);

        @POST("/user/v2/signOut")
        Call<BaseResponseData> signOut(@Header("clientKey") String clientKey);

        @GET
        Call<LanguageResponse> languageRequest(@Url String url,
                                               @Header("clientKey") String clientKey);

        @GET
        Call<VernacularResponse> vernacularRequest(@Url String url,
                                                   @Header("clientKey") String clientKey);

        @GET
        Call<VernacularResponseNew> vernacularRequestNew(@Url String url,
                                                         @Header("clientKey") String clientKey);

        @GET
        Call<CountriesResponse> countriesListRequest(@Url String url,
                                                     @Header("clientKey") String clientKey);

        @GET
        Call<CountriesResponse> statesListRequest(@Url String url,
                                                  @Header("clientKey") String clientKey);

        @FormUrlEncoded
        @POST("/user/v3/registerDevice")
        Call<DeviceRegData> registerDeviceEncryptedPayLoadShreyas(@Field("payload") String payload);

        @FormUrlEncoded
        @POST("/user/v2/signIn")
        Call<UserSigninResponse> msisdnLoginEncryptedShreyas(@Header("clientKey") String clientKey,
                                                           @Field("payload") String payload);

        @FormUrlEncoded
        @POST("/user/v2/signUp")
        Call<BaseResponseData> signUpEncrypted(@Header("clientKey") String clientKey,
                                               @Field("payload") String payload);
        @FormUrlEncoded
        @POST("/user/v2/smcSignup/")
        Call<BaseResponseData> smcSignup(@Header("clientKey") String clientKey,
                                               @Field("payload") String payload);

        @FormUrlEncoded
        @POST("/user/v2/userSignUp")
        Call<SignupResponseData> userSignUp(@Header("clientKey") String clientKey,
                                            @Field("payload") String payload);

        @FormUrlEncoded
        @POST("/user/v7/mobile/signIn")
        Call<UserSigninResponse> mobileSignInEncrypted(@Header("clientKey") String clientKey,
                                                     @Field("payload") String payload);


        @GET("/content/v2/contentList")
        Call<CardResponseData> profileActorContentList(@Header("clientKey") String clientKey,
                                                       @Query("type") String contentType,
                                                       @Query("language") String language,
                                                       @Query("orderMode") String orderMore,
                                                       @Query("person") String person,
                                                       @Query("fields") String fields,
                                                       @Query("startIndex") int startIndex,
                                                       @Query("publishingHouseId") String publishingHouseId,
                                                       @Query("orderBy") String orderBy,
                                                       @Query("GlobalServiceId") String globalServiceId,
                                                       @Query("tags") String tags,
                                                       @Query("genre") String genre,
                                                       @Query("query") String query,
                                                       @Query("siblingOrder") String siblingOrder,
                                                       @Query("contentRights") String contentRights,
                                                       @Query("displayLanguage") String displayLanguage,
                                                       @Query("count") int count);

        @GET("/content/v2/contentList")
        Call<CardResponseData> genreContentList(@Header("clientKey") String clientKey,
                                                @Query("type") String contentType,
                                                @Query("orderMode") String orderMore,
                                                @Query("fields") String fields,
                                                @Query("startIndex") int startIndex,
                                                @Query("orderBy") String orderBy,
                                                @Query("count") int count);

        @GET("/content/v2/contentList")
        Call<CardResponseData> profileActorContentList(@Header("clientKey") String clientKey,
                                                       @Query("type") String contentType,
                                                       @Query("language") String language,
                                                       @Query("orderMode") String orderMore,
                                                       @Query("person") String person,
                                                       @Query("fields") String fields,
                                                       @Query("startIndex") int startIndex,
                                                       @Query("orderBy") String orderBy,
                                                       @Query("GlobalServiceId") String globalServiceId,
                                                       @Query("tags") String tags,
                                                       @Query("genre") String genre,
                                                       @Query("query") String query,
                                                       @Query("siblingOrder") String siblingOrder,
                                                       @Query("contentRights") String contentRights,
                                                       @Query("displayLanguage") String displayLanguage,
                                                       @Query("count") int count);


        @FormUrlEncoded
        @POST("user/v2/social/login/Google/")
        Call<SocialLoginData> googleRequest(@Header("clientKey") String clientKey,
                                            @Field("authToken") String authToken,
                                            @Field("idToken") String idToken,
                                            @Field("googleId") String googleId,
                                            @Field("tokenExpiry") String expiry);

        @FormUrlEncoded
        @POST("/user/v2/social/login/FB")
        Call<SocialLoginData> fbRequest(@Field("clientKey") String clientKey,
                                        @Field("authToken") String authToken,
                                        @Field("tokenExpiry") String expiry);

        @POST("user/v2/profilePicture/{loggedInUserId}")
        Call<ImageUploadResponse> postImage(@Header("clientKey") String clientKey,
                                            @Body RequestBody image,
                                            @Path("loggedInUserId") String loggedInUserId);

        @FormUrlEncoded
        @POST("/user/v2/profile")
        Call<UserProfileResponseData> userProfileUpdateRequest(@Header("clientKey") String clientKey,
                                                               @Field("first") String name,
                                                               @Field("mobile_no") String mobile_no,
                                                               @Field("email") String email,
                                                               @Field("gender") String gender,
                                                               @Field("location") String location,
                                                               @Field("age") String age,
                                                               @Field("last") String last,
                                                               @Field("dob") String dob,
                                                               @Field("city") String city,
                                                               @Field("state") String state,
                                                               @Field("pincode") String pincode,
                                                               @Field("address") String address,
                                                               @Field("otp") String otp,
                                                        @Field("language") String language);

        @FormUrlEncoded
        @POST("user/v2/content/{contentId}/like")
        Call<FavouriteResponse> contentLikedRequest(@Header("clientKey") String clientKey,
                                                    @Path("contentId") String contentId,
                                                    @Field("contentType") String contentType,
                                                    @Field("clientKey") String clientKey1,
                                                    @Field("like") String isLike,
                                                    @Header("Cache-Control") String cacheControl);

        @GET("user/v2/content/{contentId}/like")
        Call<FavouriteResponse> likedContentCheckRequest(@Header("clientKey") String clientKey,
                                                         @Path("contentId") String contentId,
                                                         @Query("contentType") String contentType,
                                                         @Query("clientKey") String clientKey1,
                                                         @Header("Cache-Control") String cacheControl);

    }


}
