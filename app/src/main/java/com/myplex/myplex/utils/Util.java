package com.myplex.myplex.utils;

import static com.myplex.api.myplexAPISDK.getApplicationContext;
import static com.myplex.myplex.ApplicationController.ENABLE_RUPEE_SYMBOL;
import static com.myplex.myplex.ApplicationController.getAppContext;
import static com.myplex.myplex.utils.Util.DateFormat.yyyy_MM_dd;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.apalya.myplex.eventlogger.MyplexEvent;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.OfflineLicenseHelper;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.user.EventsPlayerStatusUpdateRequest;
import com.myplex.model.AlarmData;
import com.myplex.model.AlarmsSetData;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPackagePriceDetailsItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.CarouselInfoData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.model.PlayerUpdateStateListener;
import com.myplex.myplex.recievers.ReminderReceiver;
import com.myplex.myplex.ui.activities.LoginActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.epg.EPG;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Apalya on 10/27/2015.
 */
public class Util {
    private static final String TAG = Util.class.getSimpleName();
    private static final int MAX_COUNT_FOR_PREF_ALARMS = 45;
    private static final String MINIPLAYERTAG = "MiniPlayer";
    private static ArrayList<String> tabsThatHavePortraitBanner = new ArrayList<>();
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    // bandwidth in kbps
    private static int POOR_BANDWIDTH = 150;
    private static int AVERAGE_BANDWIDTH = 550;
    private static int GOOD_BANDWIDTH = 2000;
    private static long startTime;
    private static long endTime;
    private static long fileSize;

    public static boolean isPremiumUser(){
        List<String> subscribedCardDataPackages = ApplicationController.getCardDataPackages();
        if (subscribedCardDataPackages == null
                || subscribedCardDataPackages.isEmpty()) {
            return false;
        }
        return true;
    }

    public static int getNumColumns(Context mContext) {
        if (DeviceUtils.isTablet(mContext)) {
            if( mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return 4;
            }
            return 3;
        } else {
            return 2;
        }
    }
    static Bitmap image;
    public static Bitmap getImageBitmapFromURL(Context context, String imageUrl){
        Bitmap imageBitmap = null;
        try {
            imageBitmap = new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    try {

                        Glide.with(context)
                                .asBitmap()
                                .load(imageUrl)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        image = resource;
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });
                    } catch (Exception e) {
                        image = BitmapFactory.decodeResource(context.getResources(), R.drawable.nav_drawer_profile_thumbnail);
                        e.printStackTrace();
                    }
                    return image;
                }
            }.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            imageBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.nav_drawer_profile_thumbnail);
        }
        return imageBitmap;
    }
    public static int getNumColumns(Context mContext,CarouselInfoData mCarouselInfoData) {
        if (DeviceUtils.isTablet(mContext)) {
            if( mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (isBigLayoutType(mCarouselInfoData)){
                    return 4;
                }
                return 3;
            }
            if (isBigLayoutType(mCarouselInfoData)){
                return 3;
            }
            return 2;
        } else {
            if (isBigLayoutType(mCarouselInfoData)){
                return 3;
            }
            return 2;
        }
    }

    public static boolean isBigLayoutType(CarouselInfoData mCarouselInfoData) {
        return (mCarouselInfoData != null
                && (mCarouselInfoData.isViewAllBigItemLayout()
                || mCarouselInfoData.isViewAllBigItemLayoutWithoutFilter()));
    }

    public static String doEncrypt(String realText) {
        if (realText == null) {
            return realText;
        }
        try {
            realText = Base64Util.encodeToString(realText.getBytes(), Base64.DEFAULT);
        } catch (Exception e) {

        }
        return realText;
    }

    public static String getDDMMYYYY(String dateInStringUTC) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(dateInStringUTC);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date).toString();
        }

        return "";

    }


    public static String getDDMMYYYYUTC(String dateInStringUTC) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(dateInStringUTC);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
//            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date).toString();
        }

        return "";

    }
    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    public static String getYYYY(String dateInStringUTC) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(dateInStringUTC);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date).toString();
        }

        return "";

    }

    public static String getYYYYMMDD(Object date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getDefault());
        Date parsedDate = null;
        try {
            String dateInYYYMMDD = "";
            if (date instanceof Date) {
                dateInYYYMMDD = sdf.format(date);
            } else if (date instanceof String) {
                parsedDate = format.parse(date.toString());
                if (parsedDate != null) {
                    dateInYYYMMDD = sdf.format(parsedDate).toString();
                }
            } else {
                return "";
            }
            return dateInYYYMMDD;

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void prepareDisplayinfo(Activity activity) {
        try {
          //  APIConstants.BASE_URL = activity.getString(R.string.config_domain_name);
            APIConstants.TENANT_ID = activity.getString(R.string.tenant_id);
            APIConstants.msisdnPath = activity.getFilesDir() + "/" + "msisdn.bin";
            APIConstants.locationPath = activity.getFilesDir() + "/" + "location.bin";
            APIConstants.menuListPath = activity.getFilesDir() + "/" + "menuList.bin";
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            ApplicationController.getApplicationConfig().screenHeight = dm.heightPixels;
            ApplicationController.getApplicationConfig().screenWidth = dm.widthPixels;
            ApplicationController.getApplicationConfig().type = UiUtil.findDpi(dm.densityDpi);
            SDKUtils.downloadStoragePath = "/sdcard/Android/data/" + activity.getPackageName() +
                    "/files/";
            ApplicationController.getApplicationConfig().downloadCardsPath = activity.getFilesDir() + "/" + "downloadlist.bin";
            //Replace internalPath with appDirectory to store in memory card.
            //Remember to add WRITE_EXTERNAL_STORAGE permission in Manifest file
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static CardDownloadData saveExternalDownloadInfo(CardData aMovieData, CardDownloadData mDownloadData) {

        if (aMovieData == null) {
            return null;
        }
        CardDownloadedDataList downloadlist = null;
        try {
            downloadlist = (CardDownloadedDataList) SDKUtils.loadObject(ApplicationController.getApplicationConfig().downloadCardsPath);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (downloadlist == null) {
            downloadlist = new CardDownloadedDataList();
            downloadlist.mDownloadedList = new LinkedHashMap<>();
        }

        CardDownloadData downloadData = mDownloadData;
        // downloadData.mDownloadPath=DownloadUtil.downloadVideosStoragePath+aMovieData._id+".mp4";
        downloadlist.mDownloadedList.put(aMovieData._id, downloadData);
        ApplicationController.sDownloadList = downloadlist;
        SDKUtils.saveObject(downloadlist, ApplicationController.getApplicationConfig().downloadCardsPath);

        return downloadData;
    }


    public static boolean isTokenValid(String clientKeyExp) {

        //Util.showToast(clientKeyExp);

        boolean flag = true;
        if (flag) return flag;

        List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));

        Date convertedDate = new Date();
        for (SimpleDateFormat pattern : knownPatterns) {
            try {
                convertedDate = pattern.parse(clientKeyExp);
                break;
            } catch (ParseException pe) {
                pe.printStackTrace();
            } catch (Exception e) {
            }
        }
        Date currentDate = new Date();
        if (convertedDate.compareTo(currentDate) > 0) {
            //Util.showToast("Valid");
            return true;
        } else {
            //Util.showToast("Invalid");
            return false;
        }

    }

    public static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable())
            return true;
        return false;
    }

    public static String getTimeHHMM(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    public static String getTimeHHMM_AM(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    public static Date getDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getDurationWithFormat(String duration) {
        if (duration == null) return null;

        duration = duration.replace(":0:", ":00:");
        if (duration.endsWith(":0")) {
            duration = duration.replace(duration.substring(duration.length() - 1, duration.length()), ":00");
        }

        String[] splitValues = duration.split(":");
        if (splitValues[0].length() == 1) {
            duration = "0" + duration;
        }
        if (splitValues.length > 2) {
            if (splitValues[0].equalsIgnoreCase("0") || splitValues[0].equalsIgnoreCase("00")) {
                duration = splitValues[1] + ":" + splitValues[2] + " mins";
                if (splitValues[1].length() == 1) {
                    duration = "0" + duration;
                }
            } else {
                duration = duration + " hrs";
            }
        } else if (splitValues.length == 2) {
            duration = duration + " mins";
            if (splitValues[0].length() == 1) {
                duration = "0" + duration;
            }
        }
        return duration;
    }

    public static int getStatusBarHeight(final Context context) {
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return resources.getDimensionPixelSize(resourceId);
        else
            return (int) Math.ceil((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25) * resources.getDisplayMetrics().density);
    }

    public static String getDuration(String duration) {
        if (duration == null) return null;

        duration = duration.replace(":0:", ":00:");
        if (duration.endsWith(":0")) {
            duration = duration.replace(duration.substring(duration.length() - 1, duration.length()), ":00");
        }
        String[] splitValues = duration.split(":");
        if (splitValues[0].length() == 1) {
            duration = "0" + duration;
        }
        if (splitValues.length > 2) {
            if (splitValues[0].equalsIgnoreCase("0") || splitValues[0].equalsIgnoreCase("00")) {
                duration = splitValues[1] + ":" + splitValues[2];
                if (splitValues[1].length() == 1) {
                    duration = "0" + duration;
                }
            }
        }
        return duration;
    }

    public static String getFullUTCDateInString(String dateInString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date).toString();
        }

        return "";
    }

    public static Spannable getDayMonthYear(String dateInString) {
        Date date = null;
        SimpleDateFormat formatForDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf;
        Spannable cs = null;
        try {
            date = formatForDate.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new SpannableString("");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.get(Calendar.DAY_OF_MONTH);
        String format = "dd'" + getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH))
                + "'" + " " + "MMM " + "yyyy";


        sdf = new SimpleDateFormat(format);

        dateInString = sdf.format(c.getTime());
    /*    for (int i = 0; i < 6; i++) {
            c.setTime(date);
            c.add(Calendar.DATE, i);  // number of days to add
            c.get(Calendar.DAY_OF_MONTH);
            String format = "dd'" + getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH))
                    + "'" + " " + "MMM " + "yyyy";


            sdf = new SimpleDateFormat(format);

            dateInString = sdf.format(c.getTime());

        }*/
        cs = new SpannableString(dateInString);
        int indexOfAphostrophe = 2;
        cs.setSpan(new SuperscriptSpan(), indexOfAphostrophe, indexOfAphostrophe + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        cs.setSpan(new RelativeSizeSpan(0.7f), indexOfAphostrophe, indexOfAphostrophe + 2, 0);
        return cs;

    }
    public static ArrayList<String> generateEpgTable(boolean backEpg) {
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
    public static ArrayList<String> showNextDates() {
        Date date = new Date();
        SimpleDateFormat sdf;
        ArrayList<String> datesList = new ArrayList<>();


        Calendar c = Calendar.getInstance();

        for (int dayCountOffSet = PrefUtils.getInstance().getPrefEnablePastEpg() ? -PrefUtils.getInstance().getPrefNoOfPastEpgDays() : 0; dayCountOffSet < 7; dayCountOffSet++) {
            c.setTime(date);
            c.add(Calendar.DATE, dayCountOffSet);  // number of days to add
            c.get(Calendar.DAY_OF_MONTH);
            String st = "EEE" + ",";
            String format = st + " dd" + "'" + getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH)) + "'" + " MMM";


            sdf = new SimpleDateFormat(format);

            String dt = sdf.format(c.getTime());

            if (c.getTime().equals(date)) {
                String[] splited = dt.split(",");
                String todayDateText = "Today," + " " + splited[1];
                dt = todayDateText;
            }
            datesList.add(dt);
        }
         /*   String today = datesList.get(0);
            String[] splited = today.split(",");
            String st = "Today," + " " + splited[1];
            datesList.set(0, st);*/

        return datesList;

    }

    public static ArrayList<EPG.EPGTab> showNextDatesEPG(boolean past) {
        Date date = new Date();
        SimpleDateFormat sdf;
        ArrayList<EPG.EPGTab> datesList = new ArrayList<>();


        Calendar c = Calendar.getInstance();

        for (int dayCountOffSet = PrefUtils.getInstance().getPrefEnablePastEpg() ? -PrefUtils.getInstance().getPrefNoOfPastEpgDays() : past ? -1 : 0; dayCountOffSet < 7; dayCountOffSet++) {
            EPG.EPGTab epgTab = new EPG.EPGTab();
            c.setTime(date);
            c.add(Calendar.DATE, dayCountOffSet);  // number of days to add
            c.get(Calendar.DAY_OF_MONTH);
            String st = "EEE" + ",";
            String format = st + " dd" + "'" + getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH)) + "'" + " MMM";


            sdf = new SimpleDateFormat(format);

            String dt = sdf.format(c.getTime());

          /*  if (c.getTime().equals(date)) {
                String[] splited = dt.split(",");
                String todayDateText = "Today," + " " + splited[1];
                dt = todayDateText;
                epgTab.setTitle("Today");
            } else {
                epgTab.setTitle(dt.split(",")[1]);
            }*/
            if(isToday(c.getTime().getTime())) {
                epgTab.setTitle("Today");
            } else if(isYesterday(c.getTime().getTime())) {
                epgTab.setTitle("Yesterday");
            } else if(isTomorrow(c.getTime().getTime())) {
                epgTab.setTitle("Tomorrow");
            } else {
                epgTab.setTitle(dt.split(",")[1]);
            }
           // epgTab.setTitle(dt.split(",")[0]);
            epgTab.setSubtitle(dt.split(",")[1]);
            epgTab.setStartTime(c.getTime().getTime());
            epgTab.setEndTime(date.getTime());
            epgTab.setIsSelected(true);
             epgTab.setFormatDate(dt);
             epgTab.setDate(c.getTime());

             Calendar calendar = Calendar.getInstance();
            String parseTime = "00:00";
            SimpleDateFormat adParser = new SimpleDateFormat("HH:mm");
            try {
                adParser.parse(parseTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String time = "";
            calendar.setTime(adParser.getCalendar().getTime());
            calendar.add(Calendar.MINUTE, ApplicationController.TIME_SHIFT);
            time = adParser.format(calendar.getTime());
            epgTab.setTime(time);
            datesList.add(epgTab);
        }
         /*   String today = datesList.get(0);
            String[] splited = today.split(",");
            String st = "Today," + " " + splited[1];
            datesList.set(0, st);*/

        return datesList;

    }

    public static Boolean isToday(Long whenInMillis) {
        return DateUtils.isToday(whenInMillis);
    }

    public static Boolean isTomorrow(Long whenInMillis) {
        return DateUtils.isToday(whenInMillis - DateUtils.DAY_IN_MILLIS);
    }

    public static Boolean isYesterday(Long whenInMillis) {
        return DateUtils.isToday(whenInMillis + DateUtils.DAY_IN_MILLIS);
    }

    public static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static String convertDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        return hours+":"+minutes;
    }

    public static Date getCurrentDate(int pos) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (PrefUtils.getInstance().getPrefEnablePastEpg()) {
            pos = pos - PrefUtils.getInstance().getPrefNoOfPastEpgDays();
        }
        calendar.add(Calendar.DATE, pos);
        return calendar.getTime();
    }

    public static void setReminder(final Context mContext,
                                   final String notificationTitle,
                                   final String _id,
                                   final Date programmeTime, final String notificationMessage, String contentType, String startDate) {

        final Date now = new Date();

        if (programmeTime == null) {
            return;
        }
        final Calendar prg = Calendar.getInstance();
        prg.setTimeZone(TimeZone.getDefault());
        prg.setTime(programmeTime);


        if (!now.before(programmeTime)) {
            return;
        }


        String text = null;
        text = notificationMessage;

        if (text == null) {
            text = "The programm is scheduled at "
                    + getTimeHHMM(programmeTime);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, prg.get(Calendar.SECOND));
        calendar.set(Calendar.MINUTE, prg.get(Calendar.MINUTE));
        calendar.set(Calendar.HOUR_OF_DAY,
                prg.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.DAY_OF_MONTH,
                prg.get(Calendar.DAY_OF_MONTH));

        Intent alarmintent = new Intent(mContext,
                ReminderReceiver.class);
        if(contentType != null){
            alarmintent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_TYPE, contentType);
        }
        alarmintent.putExtra(APIConstants.NOTIFICATION_PARAM_TITLE, notificationTitle);
        alarmintent.putExtra("note", text);
        alarmintent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, _id);
        long when = prg.getTimeInMillis() - (5*60*1000);
        alarmintent.putExtra(APIConstants.NOTIFICATION_PARAM_TIME, startDate);
        alarmintent.setData(Uri.parse("alarm://" + when));

        PendingIntent sender = PendingIntent.getBroadcast(
                mContext, (int) when, alarmintent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | Intent.FILL_IN_DATA  | PendingIntent.FLAG_MUTABLE);

        AlarmManager am = (AlarmManager) mContext
                .getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, when, sender);
    }

    public static Date getUTCDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date getRequiredDate(String time, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat parse = new SimpleDateFormat("yyyy:MM:dd");
        String st = parse.format(date);
        String[] sp = st.split(":");
        int year = Integer.parseInt(sp[0]);
        int month = Integer.parseInt(sp[1]);
        int day = Integer.parseInt(sp[2]);
        if(time != null) {
            String[] timeSpilt = time.split(":");
            calendar.set(year, month - 1, day, Integer.parseInt(timeSpilt[0]), Integer.parseInt(timeSpilt[1]), 00);
        }
        return calendar.getTime();
    }

    public static String getServerDateFormat(String time, Date dt) {
        SimpleDateFormat parse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = getRequiredDate(time, dt);
        return parse.format(date);


    }

    public static int getTotalDuration(String startTime, String endTime, boolean isTotalDuration) {
        Date startDate = getUTCDate(startTime);
        Date date = new Date();
        Date endDate = getUTCDate(endTime);
        long diff;
        if (isTotalDuration) {
            diff = endDate.getTime() - startDate.getTime();
        } else {
            diff = date.getTime() - startDate.getTime();
        }
        double diffInHours = diff / ((double) 1000 * 60 * 60);

        int min = (int) Math.round(diffInHours * 60);

        return min;
    }

    public static String getAppVersionName(Context mContext) {
        final PackageManager pm = mContext.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(mContext.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        try {
            return pm.getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean onHandleExternalIntent(Activity activity) {

        if (activity.getIntent() == null)
            return false;

        Bundle bundle = activity.getIntent().getExtras();

        if (bundle == null)
            return false;

        boolean intentHandled = false;
        Intent intent;
        intent = new Intent(activity, MainActivity.class);
        for (String key : bundle.keySet()) {
            if (bundle.containsKey(key) && bundle.get(key) != null) {
                if (key.equalsIgnoreCase(APIConstants.NOTIFICATION_PARAM_TITLE)
                        || APIConstants.NOTIFICATION_PARAM_MESSAGE.equals(key)
                        || APIConstants.MESSAGE_TYPE.equals(key)
                ||APIConstants.NOTIFICATION_PARAM_ACTION.equals(key)) {
                    intentHandled = true;
                }
                if (bundle.get(key) instanceof Boolean) {
                    //Log.d(TAG, "key " + key + ", value: " + bundle.getBoolean(key));
                    intent.putExtra(key, bundle.getBoolean(key));
                } else if (bundle.get(key) instanceof Integer) {
                    //Log.d(TAG, "key " + key + ", value: " + bundle.getInt(key));
                    intent.putExtra(key, bundle.getInt(key));
                } else if (bundle.get(key) instanceof String && bundle.getString(key) != null) {
                    //Log.d(TAG, "key " + key + ", value: " + bundle.getString(key));
                    intent.putExtra(key, bundle.getString(key).trim());
                }
                //Adding this line cause the parameter action is not being picked up in the usual for loop
                if(bundle.get(APIConstants.NOTIFICATION_PARAM_ACTION) != null){
                    //Log.d(TAG, "key " + key + ", value: " + bundle.getString(key));
                    intent.putExtra(APIConstants.NOTIFICATION_PARAM_ACTION,
                            bundle.get(APIConstants.NOTIFICATION_PARAM_ACTION).toString().trim());
                }
                if(key != null
                        && key.startsWith(APIConstants.PARAM_CLEVERTAP_NOTIFICATION_WZRK_)){
                    //Log.d(TAG, "CleverTap wzrk_ params remove key " + key + ", value: " + bundle.getString(key));
                    bundle.remove(key);
                    intent.removeExtra(key);
                }
            }
        }
        if (!intentHandled) {
            return false;
        }
        launchActivity(activity, intent);
        return true;
    }

    public static void launchActivity(Activity activity, Intent intent) {
        //Log.d(TAG, "Util -> launch activity");
        activity.startActivity(intent);
    }


    public static AlarmData getReminderProgmaNameIfExistAtThisTime(CardData programData) {
        if (programData == null
                || programData.startDate == null) {
            return null;
        }
        String allProgramStartTimes = PrefUtils.getInstance()
                .getPrefAlreadySetReminderTimes();

        Gson gson = new Gson();
        AlarmsSetData alarmsSetData = gson.fromJson(allProgramStartTimes, AlarmsSetData.class);
        if (alarmsSetData == null || alarmsSetData.results == null) {
            return null;
        }
        try {
            for (AlarmData alarm : alarmsSetData.results) {
                if (alarm.startDate.equalsIgnoreCase(programData.startDate)
                        && alarm._id != null
                        && alarm._id.equalsIgnoreCase(programData.globalServiceId)) {
                    return alarm;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d(TAG, "" + e.getMessage());
        }

        return null;
    }

    public static void cancelReminder(final Context mContext,
                                      final String notificationTitle,
                                      final String _id,
                                      final Date programmeTime, final String notificationMessage, String contentType) {

        final Date now = new Date();

        if (programmeTime == null) {
            return;
        }
        final Calendar prg = Calendar.getInstance();
        prg.setTimeZone(TimeZone.getDefault());
        prg.setTime(programmeTime);

        if (!now.before(programmeTime)) {
            return;
        }


        String text;
        text = notificationMessage;
        text = "The programme is scheduled at "
                + getTimeHHMM(programmeTime);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, prg.get(Calendar.SECOND));
        calendar.set(Calendar.MINUTE, prg.get(Calendar.MINUTE));
        calendar.set(Calendar.HOUR_OF_DAY,
                prg.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.DAY_OF_MONTH,
                prg.get(Calendar.DAY_OF_MONTH));

        Intent alarmintent = new Intent(mContext,
                ReminderReceiver.class);
        if (contentType != null) {
            alarmintent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_TYPE, contentType);
        }
        alarmintent.putExtra(APIConstants.NOTIFICATION_PARAM_TITLE, notificationTitle);
        alarmintent.putExtra("note", text);
        alarmintent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, _id);

      //  long when = prg.getTimeInMillis() ;
        long when = prg.getTimeInMillis() - (5 * 60 * 1000);
        alarmintent.setData(Uri.parse("alarm://" + when));

        PendingIntent sender = PendingIntent.getBroadcast(
                mContext, (int) when, alarmintent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | Intent.FILL_IN_DATA | PendingIntent.FLAG_MUTABLE);

        AlarmManager am = (AlarmManager) mContext
                .getSystemService(Context.ALARM_SERVICE);

        //  am.set(AlarmManager.RTC_WAKEUP, when, sender);
/*
        if (Build.VERSION.SDK_INT < 23) {
            if (Build.VERSION.SDK_INT >= 19) {
                //  if(System.currentTimeMillis()<when)
                am.setExact(AlarmManager.RTC_WAKEUP, when, sender);
            } else {
                // if(System.currentTimeMillis()<when)
                am.set(AlarmManager.RTC_WAKEUP, when, sender);
            }
        } else {
            // if(System.currentTimeMillis()<when)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, sender);
        }*/
        am.cancel(sender);
    }
    public static void updateAalarmTimes(CardData programData, boolean isToRemoveExipired) {
        //Retrieve all Reminder programs data
        String allProgramStartTimes = PrefUtils.getInstance()
                .getPrefAlreadySetReminderTimes();
        try {
            AlarmsSetData alarmsSet = new AlarmsSetData();

            Gson gson = new Gson();
            if (allProgramStartTimes != null) {
                //If programs are available make Them in to Object
                alarmsSet = gson.fromJson(allProgramStartTimes, AlarmsSetData.class);
                //Go through all Objects to update if already a program exists same time
                //Or If the time is expired.
                for (Iterator<AlarmData> it = alarmsSet.results.iterator(); it.hasNext(); ) {
                    AlarmData alarm = it.next();
                    //if isToRemoveExipired is true Then removing alarm if it is expired
                    if(programData!=null) {
                        if (isToRemoveExipired) {
                            Date programstartDate = Util.getDate(alarm.startDate);
                            Date currentDate = new Date();
                            if (programstartDate.before(currentDate)) {
                                it.remove();
                            }
                            if (alarm.startDate != null && alarm.startDate.equalsIgnoreCase(programData.startDate)) {
                                it.remove();
                            }
                        } else {
                            if (programData == null
                                    || programData.startDate == null) {
                                break;
                            }
                            //Removing alarm if current Program data is not null and If there is a
                            // program already
                            // available at the same time
                            if (alarm.startDate.equalsIgnoreCase(programData.startDate)) {
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }
            if (alarmsSet != null
                    && alarmsSet.results != null
                    && alarmsSet.results.size() > MAX_COUNT_FOR_PREF_ALARMS) {
                alarmsSet.results.remove(0);
            }
            if (!isToRemoveExipired) {
                if (programData != null
                        && programData.startDate != null
                        && programData.generalInfo != null
                        && programData.generalInfo.title != null) {
                    AlarmData newAlarm = new AlarmData();
                    newAlarm.startDate = programData.startDate;
                    newAlarm.title = programData.generalInfo.title;
                    newAlarm._id = programData.globalServiceId;
                    alarmsSet.results.add(newAlarm);
                }
            }
            allProgramStartTimes = gson.toJson(alarmsSet);
            PrefUtils.getInstance().setPrefAlreadySetReminderTimes(allProgramStartTimes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getAvailebleFreeSpaceInMBOnDisk(Context mContext) {
        long mb = 1024L * 1024L ;
        double freeSpace = 0;

        File file = new File(mContext.getExternalFilesDir(null).getPath());
        if (file != null) {
            freeSpace = file.getFreeSpace();
        }

        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();

        if (downloadlist != null) {
            FetchDownloadProgress fetchDownloadProgress = FetchDownloadProgress.getInstance(mContext);
            for (CardDownloadData cardDownloadData : downloadlist.mDownloadedList.values()) {
                if (cardDownloadData.mDownloadId > 1) {
                    freeSpace = freeSpace - fetchDownloadProgress.getSpaceRequired(cardDownloadData.mDownloadId);
                }
            }
        }

        double freeSpaceMb = freeSpace / mb;
        return freeSpace;
    }


    public static boolean hasSpaceAvailabeToDownload(String contentType, double mbs, Context mContext) {
        long requiredSpaceFactor = 1024L * 1024L * 1024L;
        if(APIConstants.VIDEOQUALTYSD.equalsIgnoreCase(contentType)){
            requiredSpaceFactor = 1024L * 1024L;
        }
        double freeSpace = 0;

        File file = mContext.getExternalFilesDir(null);
        if (file != null) {
            freeSpace = file.getFreeSpace();
        }

        CardDownloadedDataList downloadlist = ApplicationController.getDownloadData();

        if (downloadlist != null) {
            FetchDownloadProgress fetchDownloadProgress = FetchDownloadProgress.getInstance(mContext);
            for (CardDownloadData cardDownloadData : downloadlist.mDownloadedList.values()) {
                if (cardDownloadData.mDownloadId > 1) {
                    freeSpace = freeSpace - fetchDownloadProgress.getSpaceRequired(cardDownloadData.mDownloadId);
                }
            }
        }

        double availablefreeSpaceFactor = freeSpace / requiredSpaceFactor;

            return availablefreeSpaceFactor > mbs;
    }

    /**
     * @param context used to check the device version and DownloadManager information
     * @return true if the download manager is available
     */
    public static boolean isDownloadManagerAvailable(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return false;
            }
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
//            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
//                    PackageManager.MATCH_DEFAULT_ONLY);
//            return list.size() > 0;
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            return manager != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void showNotification(Context context, String title, String message, Intent notificationIntent) {

        if (notificationIntent == null) {
            notificationIntent = new Intent(context, LoginActivity.class);
        }

        long when = Calendar.getInstance().getTimeInMillis();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        //Update reminder alarms list
        Util.updateAalarmTimes(null, true);
        /*create unique this intent from  other intent using setData */
        notificationIntent.setData(Uri.parse("content://" + when));
        /*create new task for each notification with pending intent so we set Intent.FLAG_ACTIVITY_NEW_TASK */
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

// define sound URI, the sound to be played when there's a notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "sundirect Play",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("sundirect Play Notifications");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"default");

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable
                .sundirect_final_app_icon);
        builder.setContentIntent(contentIntent)
                .setLargeIcon(largeIcon)
                .setTicker(context.getResources().getString(R.string.app_name))
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setWhen(when)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setContentText(message)
                .setColor(context.getResources()
                        .getColor(R.color.white));
        if(Build.VERSION.SDK_INT >=33){
            builder.setSmallIcon(R.drawable.sundirect_logo_notification);
        }else {
            builder.setSmallIcon(R.drawable.sundirect_final_app_icon);
        }
        Notification notification;

        if (Build.VERSION.SDK_INT < 16) {
            notification = builder.getNotification();
            notification.defaults |= Notification.DEFAULT_ALL;
        } else {
            notification = builder.build();
            notification.priority = Notification.PRIORITY_MAX;
        }
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                int smallIconViewId = context.getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());

                if (smallIconViewId != 0) {
                    if (notification.contentView != null)
                        notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                    if (notification.headsUpContentView != null)
                        notification.headsUpContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                    if (notification.bigContentView != null)
                        notification.bigContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify((int) when, notification);
    }

    public static void removeDownload(long id, Context mContext) {
        if (isDownloadManagerAvailable(mContext)) {
            try {
                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                manager.remove(id);
                Uri path = manager.getUriForDownloadedFile(id);
                if (path != null) {
                    File file = new File(path.toString());
                    if (file != null) {
                        file.delete();
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean canResolveIntent(Intent intent, Context context) {
        List<ResolveInfo> resolveInfo = context.getPackageManager()
                .queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }

    private static final int REQ_RESOLVE_SERVICE_MISSING = 2;

    public static void launchYouyubePlayer(Activity activity, String video_id) {

        String developerKey = activity.getString(R.string.config_google_developerkey);

        if (TextUtils.isEmpty(developerKey)) {
            return;
        }

        Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                activity, developerKey,
                video_id, 0, true, false);

        if (intent != null) {
            if (canResolveIntent(intent, activity)) {
                activity.startActivityForResult(intent, 101);
            } else {
                // Could not resolve the intent - must need to install or update
                // the YouTube API service.
                YouTubeInitializationResult.SERVICE_MISSING.getErrorDialog(
                        activity, REQ_RESOLVE_SERVICE_MISSING).show();
            }
        }
    }


    public static final String getImageLink(CardData carouselData) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER};
        String profile = ApplicationConfig.MDPI;
        if (carouselData != null
                && carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
                && APIConstants.TYPE_MOVIE.equalsIgnoreCase(carouselData.generalInfo.type)) {
            profile = ApplicationConfig.MDPI;
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER, APIConstants.IMAGE_TYPE_COVERPOSTER,
            APIConstants.IMAGE_TYPE_THUMBNAIL};
        }

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                LoggerD.debugDownload("getImageLink for download item type- " + imageItem.type + " profile- " + imageItem.profile + " link- " + imageItem.link);
                if (imageType.equalsIgnoreCase(imageItem.type)
                        && profile.equalsIgnoreCase(imageItem.profile)) {
                    return imageItem.link;
                }
            }
        }

        return null;
    }
    private static final Pattern XS_DATE_TIME_PATTERN = Pattern.compile(
            "(\\d\\d\\d\\d)\\-(\\d\\d)\\-(\\d\\d)[Tt]"
                    + "(\\d\\d):(\\d\\d):(\\d\\d)(\\.(\\d+))?"
                    + "([Zz]|((\\+|\\-)(\\d\\d):?(\\d\\d)))?");
    public static long parseXsDateTime(String value) throws ParserException {
        if(value == null)
            return 0;
        Matcher matcher = XS_DATE_TIME_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new ParserException("Invalid date/time format: " + value);
        }

        int timezoneShift;
        if (matcher.group(9) == null) {
            // No time zone specified.
            timezoneShift = 0;
        } else if (matcher.group(9).equalsIgnoreCase("Z")) {
            timezoneShift = 0;
        } else {
            timezoneShift = ((Integer.parseInt(matcher.group(12)) * 60
                    + Integer.parseInt(matcher.group(13))));
            if (matcher.group(11).equals("-")) {
                timezoneShift *= -1;
            }
        }

     //   Calendar dateTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        Calendar dateTime= Calendar.getInstance();
        dateTime.clear();
        // Note: The month value is 0-based, hence the -1 on group(2)
        dateTime.set(Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)) - 1,
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4)),
                Integer.parseInt(matcher.group(5)),
                Integer.parseInt(matcher.group(6)));
        if (!TextUtils.isEmpty(matcher.group(8))) {
            final BigDecimal bd = new BigDecimal("0." + matcher.group(8));
            // we care only for milliseconds, so movePointRight(3)
            dateTime.set(Calendar.MILLISECOND, bd.movePointRight(3).intValue());
        }

        long time = dateTime.getTimeInMillis();
        if (timezoneShift != 0) {
            time -= timezoneShift * 60000;
        }

        return time;
    }
   public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        return date;
    }

    public static void debugLog(String msg) {
        Log.d(MINIPLAYERTAG, msg);
    }

    public static View.OnTouchListener consumeTouch() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        };
    }

    public static List<String> getEmailAccounts(Context context) {
        List<String> emails = new ArrayList<String>();

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                emails.add(possibleEmail);
                Log.d("samir", possibleEmail);
            }
        }

        return emails;
    }

    public static String takeScreenShot(final Activity activity) {
        // TODO Auto-generated method stub
        final View view = activity.getWindow().getDecorView();
        final String aFileName = "myplex_screen" + ".jpeg";
        // TODO Auto-generated method stub
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay()
                .getHeight();
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
                - statusBarHeight);
        savemyplexPic(b, aFileName);
        view.destroyDrawingCache();

        return getAppContext().getExternalFilesDir(null)
                + APIConstants.downloadImagesStoragePath + aFileName;
    }

    public static boolean savemyplexPic(Bitmap b, String aFileName) {
        File picDir = new File(getAppContext().getExternalFilesDir(null)
                + APIConstants.downloadImagesStoragePath);
        if (!picDir.exists()) {
            picDir.mkdir();
        }
        File picFile = new File(picDir + "/" + aFileName);
        if (picFile.exists()) {
            picFile.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(picFile);
            if (null != fos) {
                boolean saved = b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                return saved;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Uri getLocalBitmapUri(Bitmap bmp,Context mcontext) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(mcontext.getFilesDir()+"/shared_images/"+"share_image.png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            //TODO: Handle this to avoid exception
            bmpUri = FileProvider.getUriForFile(mcontext,
                    getFileProviderAuthority(),
                    file);
            //bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public static void shareData(final Context mContext, int aType, String aPath, String msg) {

        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        //sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        LoggerD.debugLog("share message- " + msg);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        if (aPath == null) {
            aType = 3;
        }
        if (aType == 1) {
            if (aPath.startsWith("http")) {
                PicassoUtil.with(mContext).load(aPath, new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Uri picUri = getLocalBitmapUri(bitmap,mContext);
                        sendIntent.setData(getLocalBitmapUri(bitmap,mContext));
                        sendIntent.setType("image/*");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, picUri);
                        mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.send_to)));
                    }

                    @Override
                    public void onBitmapFailed(Exception e,Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
                return;
            }

            //final Uri picUri = Uri.fromFile(new File(aPath));
            final Uri picUri = FileProvider.getUriForFile(mContext,
                    getFileProviderAuthority(),
                    new File(aPath));
            sendIntent.setData(picUri);
            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, picUri);
        } else if (aType == 2) {
            //Uri picUri = Uri.fromFile(new File("/storage/sdcard0/DCIM/Camera/VID_20131002_163415.3gp"));
            //Uri picUri = Uri.fromFile(new File(aPath));
            final Uri picUri = FileProvider.getUriForFile(mContext,
                    getFileProviderAuthority(),
                    new File(aPath));
            sendIntent.setData(picUri);
            sendIntent.setType("video/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, picUri);
        } else {
            sendIntent.setType("text/plain");
        }
//        Analytics.mixPanelSharedMyplexExperience();
        mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.send_to)));
    }

    public static void showFeedback(View v) {
        if (v == null) {
            return;
        }
        v.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(v.getContext().getResources().getColor(R.color.red_highlight_color));
                        break;
                    default:
                        v.setBackgroundColor(Color.TRANSPARENT);
                        break;
                }
                return false;
            }
        });
    }

    public static RecyclerView.OnScrollListener getSrollListenerForPicasso(final Context mContext) {
        return null;
//        new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
////                newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING
////                newState == RecyclerView.SCROLL_STATE_IDLE
//                /*if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
//                    PicassoUtil.with(mContext).pause();
//                } else {
//                    PicassoUtil.with(mContext).resume();
//                }*/
//
//            }
//        };
    }

    public static int generateTimeInSec(long time) {
        int totalSeconds = (int) (time / 1000);
        return totalSeconds;
    }

    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public static int getPartnerTypeContent(CardData data) {
        if (data == null
                || data.publishingHouse == null) {
            return CardDetails.Partners.APALYA;
        }

        if (APIConstants.TYPE_SONYLIV.equals(data.publishingHouse.publishingHouseName)) {
            return CardDetails.Partners.SONY;
        }
        if (APIConstants.TYPE_HOOQ.equalsIgnoreCase(data.publishingHouse.publishingHouseName)) {
            return CardDetails.Partners.HOOQ;
        }

        if (APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(data.publishingHouse.publishingHouseName)) {
            return CardDetails.Partners.HUNGAMA;
        }
        return CardDetails.Partners.APALYA;
    }


    public static boolean isFreeContent(CardData mData) {
        if (mData != null && mData.generalInfo != null) {
            if (!mData.generalInfo.isSellable) {
                return true;
            }
        }
        if (!ENABLE_RUPEE_SYMBOL || !ConnectivityUtil.isConnected(ApplicationController.getAppContext())
                || (PrefUtils.getInstance().getPrefLoginStatus() == null
                    || !"success".equalsIgnoreCase(PrefUtils.getInstance().getPrefLoginStatus()))
                || !myplexAPISDK.PARAM_TO_SEND_ALL_PACKAGES_FIELD) {
            return true;
        }

        if (mData == null
                || mData.packages == null
                || mData.packages.isEmpty()) {
            return true;
        }

        if (isAnyPackHasZeroPricePoint(mData)) {
            return true;
        }


        List<String> subscribedCardDataPackages = ApplicationController.getCardDataPackages();
        if (subscribedCardDataPackages == null
                || subscribedCardDataPackages.isEmpty()) {
            return false;
        }

        for (CardDataPackages contentPackages : mData.packages) {
            if (subscribedCardDataPackages.contains(contentPackages.packageId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnyPackHasZeroPricePoint(CardData mData) {
        if (mData == null
                || mData.packages == null
                || mData.packages.isEmpty()) {
            LoggerD.debugDownload("data is null or no packages available for content- " + mData != null ? String.valueOf(mData.packages) : "null data");
            return true;
        }
//        float price = 10000.99f;
        for (CardDataPackages packageitem : mData.packages) {
            if (packageitem.priceDetails != null) {
                for (CardDataPackagePriceDetailsItem priceDetailItem : packageitem.priceDetails) {
                    if (!priceDetailItem.paymentChannel.equalsIgnoreCase(APIConstants
                            .PAYMENT_CHANNEL_INAPP) && priceDetailItem.price <= 0.0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean areAllZeroPricePacks(CardData mData) {
        if (mData == null
                || mData.packages == null
                || mData.packages.isEmpty()) {
            return true;
        }
//        float price = 10000.99f;
        for (CardDataPackages packageitem : mData.packages) {
            if (packageitem.priceDetails != null) {
                for (CardDataPackagePriceDetailsItem priceDetailItem : packageitem.priceDetails) {
                    if (!priceDetailItem.paymentChannel.equalsIgnoreCase(APIConstants
                            .PAYMENT_CHANNEL_INAPP) && priceDetailItem.price > 0.0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static String getCurrentEpgTablePosition() {
        SimpleDateFormat adParser = new SimpleDateFormat("HH:mm");

        SimpleDateFormat minParser = new SimpleDateFormat("mm");
        SimpleDateFormat hrsParser = new SimpleDateFormat("HH");

        Calendar cal = Calendar.getInstance();
        String minutes = minParser.format(cal.getTime());
        String hours = hrsParser.format(cal.getTime());

        int timeDiff = Integer.parseInt(minutes) % ApplicationController.TIME_SHIFT;
        int showingTime = Integer.parseInt(minutes) - timeDiff;
        String currentEpgTime = String.valueOf(hours) + ":" + String.valueOf(showingTime);
        LoggerD.debugLog("TVGuide: getCurrentEpgTablePosition: currentEpgTime- " + currentEpgTime);

        return currentEpgTime;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static boolean isScreenAutoRotationOnDevice(Context mContext) {
        if (!ApplicationController.ENABE_DEVICE_AUTO_ROTATE_SETTING) {
            return true;
        }
        try {
            return mContext == null ? false : android.provider.Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
        } catch (Exception e) {
            return true;
        }

    }

    public static void updatePlayerStatus(int elapsedTime, String action, String id,String streamName, PlayerUpdateStateListener... playerUpdateStateListener) {
        LoggerD.debugLog("updatePlayerStatus: " +
                "elapsedTime: " + elapsedTime +
                "action- " + action +
                "id- " + id);
        EventsPlayerStatusUpdateRequest.Params params = new EventsPlayerStatusUpdateRequest.Params(elapsedTime, action, id,streamName);
        EventsPlayerStatusUpdateRequest eventsPlayerStatusUpdateRequest = new EventsPlayerStatusUpdateRequest(params, new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (null == response || response.body() == null) {
                    onFailure(new Throwable(ApplicationController.getAppContext().getString(R.string.canot_fetch_url)), APIRequest.ERR_UN_KNOWN);
                    return;
                }
                if( playerUpdateStateListener!=null && playerUpdateStateListener.length>0 && playerUpdateStateListener[0]!=null && response!=null && response.body()!=null && response.body().status!=null) {
                    playerUpdateStateListener[0].onSuccess(response.body().status);
                }
                LoggerD.debugLog("updatePlayerStatus: player status update " +
                        "message- " + response.body().message +
                        "status- " + response.body().status);
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugLog("updatePlayerStatus: player status update " +
                        "t- " + t.getMessage() +
                        "errorCode- " + errorCode);
            }
        });
        APIService.getInstance().execute(eventsPlayerStatusUpdateRequest);
    }

    public static int calculateDurationInSeconds(String duration) {

        int durationInSeconds = 0;
        try {
            String[] hhmmss = duration.split(":");
            int hhtosecFactor = 60 * 60;
            int mmtosecFactor = 60;
            if (hhmmss.length > 2) {
                durationInSeconds = Integer.parseInt(hhmmss[0]) * hhtosecFactor + Integer.parseInt(hhmmss[1]) * mmtosecFactor + Integer.parseInt(hhmmss[2]);
            } else if (hhmmss.length > 1) {
                durationInSeconds = Integer.parseInt(hhmmss[0]) * mmtosecFactor + Integer.parseInt(hhmmss[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return durationInSeconds;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    public static boolean isAdultContent(CardData data) {
        if (data == null
                || data.content == null
                || data.content.certifiedRatings == null
                || data.content.certifiedRatings.values == null
                || data.content.certifiedRatings.values.isEmpty()
                || data.content.certifiedRatings.values.get(0).rating == null) {
            return false;
        }
        return data.content.certifiedRatings.values.get(0).rating
                .equalsIgnoreCase("A")
                || data.content.certifiedRatings.values.get(0).rating
                .equalsIgnoreCase("R");
    }


    /**
     * Closes a {@link Closeable}, suppressing any {@link IOException} that may occur. Both {@link
     * java.io.OutputStream} and {@link InputStream} are {@code Closeable}.
     *
     * @param closeable The {@link Closeable} to close.
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // Ignore.
        }
    }

    public static void showToastAt(String msg, int x, int y) {
        try {
            Toast toastMessage = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
            //Set listed gravity here.
            toastMessage.setGravity(Gravity.TOP, x, y);
            toastMessage.setDuration(Toast.LENGTH_LONG);
            toastMessage.show();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }
    public static byte[] getOfflineKeys(String  id){

        byte[] keySetId = null;
        File data = DownloadUtil.getMetaFile(id,ApplicationController.getAppContext());
        try {
            if (data.exists()) {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(data));
                keySetId = (byte[]) objectInputStream.readObject();
                objectInputStream.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return keySetId;
    }

    public static boolean isOffliceLicenseValid(String id, CardDownloadData cardDownloadData){
        OfflineLicenseHelper offlineLicenseHelper;
        Pair<Long,Long> pair;
        byte[] keySetId,newKeysetId;

        keySetId = Util.getOfflineKeys(id);
        String drmLicenseUrl = APIConstants.getDRMLicenseUrl(id, APIConstants.VIDEO_TYPE_DOWNLOAD, cardDownloadData.variantType);

        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory("sundirect Play");
        try {
            offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(drmLicenseUrl,httpDataSourceFactory);
            pair = offlineLicenseHelper.getLicenseDurationRemainingSec(keySetId);
            if(pair.first >0 && pair.second > 0){
                return true;
            }else{
                return false;
            }
        } catch (com.google.android.exoplayer2.drm.UnsupportedDrmException e) {
            e.printStackTrace();
        } catch (DrmSession.DrmSessionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Long getOfflineLicenseValidty(String id){
        OfflineLicenseHelper offlineLicenseHelper;
        Pair<Long,Long> pair;
        byte[] keySetId,newKeysetId;

        keySetId = Util.getOfflineKeys(id);
        String drmLicenseUrl = APIConstants.getDRMLicenseUrl(id, APIConstants.VIDEO_TYPE_DOWNLOAD);
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory("sundirect Play");
        try {
            offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(drmLicenseUrl,httpDataSourceFactory);
            pair = offlineLicenseHelper.getLicenseDurationRemainingSec(keySetId);
            if (pair.first > 0 && pair.second > 0) {
                return pair.first;
            } else {
                return pair.first;
            }
        } catch (com.google.android.exoplayer2.drm.UnsupportedDrmException e) {
            e.printStackTrace();
        } catch (DrmSession.DrmSessionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getListFromCommaString(String commaString){
        List<String> ids = new ArrayList<>();
        if(TextUtils.isEmpty(commaString)){
            return ids;
        }
        ids = Arrays.asList(commaString.split(","));
        return ids;
    }

    public static boolean isToSendAllPackagesField(){
        if (!checkUserLoginStatus()) {
            return false;
        }
        String standardPackageIds = PrefUtils.getInstance().getPrefStandardPackageIds();
        List<String> subscribedPackageIds = ApplicationController.getCardDataPackages();
        List<String> standardPackageIdsList = getListFromCommaString(standardPackageIds);
        if (subscribedPackageIds == null
                || standardPackageIdsList == null
                || subscribedPackageIds.isEmpty()
                || standardPackageIdsList.isEmpty()) {
            return true;
        }
        for (String id :
                subscribedPackageIds) {
            if (standardPackageIdsList.contains(id)) {
                return false;
            }
        }
        return true;
    }

    public static void setUserIdInMyPlexEvents(Context mContext){
        if (PrefUtils.getInstance().getPrefEventLoggerEnabled()) {
            MyplexEvent myplexEvent = MyplexEvent.getInstance(mContext);
            String userid = String.valueOf(PrefUtils.getInstance().getPrefUserId());
            myplexEvent.identify(userid);
            myplexEvent.setHostURL(PrefUtils.getInstance().getEventLoggerUrl());
        }
    }


    public static boolean checkUserLoginStatus() {
        String login_status = PrefUtils.getInstance().getPrefLoginStatus();
        return login_status != null && login_status.equalsIgnoreCase("success");
    }

    private static int imageCount;
    private static int size;

    public static void setImageCount(int imageCount) {
        Util.imageCount = imageCount;
        Util.size = imageCount;
    }
    private static Target getTarget(final Context mContext,final String name){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                File file = new File(mContext.getFilesDir()+ "/" + name+".png");
                LoggerD.debugLog("Bitmap Saving to..."+file.toString());
                FileOutputStream ostream = null;
                try {
                    file.createNewFile();
                    ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, ostream);
                    imageCount++;
                    LoggerD.debugLog(TAG+"IMAGE COUNT "+String.valueOf(imageCount));
                } catch (IOException e) {
                    LoggerD.debugLog("IOException" + e.getLocalizedMessage());
                } finally {
                    Util.closeQuietly(ostream);
                }

            }

            @Override
            public void onBitmapFailed(Exception e,Drawable errorDrawable) {
                imageCount++;
                LoggerD.debugLog(TAG+"Bitmap failed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                LoggerD.debugLog(TAG+"Bitmap Preparing to download");
                size++;
                LoggerD.debugLog("preparing size:"+String.valueOf(size));
            }
        };
        return target;
    }
    public static boolean allImagesLoaded(int tabSize){
        if (imageCount == size|| imageCount == tabSize * 2) {
            return true;
        } else {
            return false;
        }
    }


    public static void saveMenuIcons(Context mContext, String imageLink, String name, List<CardDataImagesItem> mListCarouselInfoData){
        // Split the image at .png
        /*if (imageLink != null) {
            LoggerD.debugDownload("imageLink- " + imageLink);
            imageLink = imageLink.replace("hamburger", "v2_hamburger");
            LoggerD.debugDownload("imageLink- " + imageLink);
        }*/
            String imageUnselected[] = imageLink.split("(?=.png)",2);
            if(imageUnselected.length>1) {
                String unselected = imageUnselected[0] + imageUnselected[1];
                String selected = imageUnselected[0] + "_highlight" + imageUnselected[1];

                Log.e("Menu Icons", "Menu Image Selected : " + selected);
                Log.e("Menu Icons", "Menu Image UnSelected : " + unselected);
                if (!Util.isNetworkAvailable(mContext)) {
                    File image = new File(mContext.getFilesDir() + "/" + name + "_highlight.png");
                    File image2 = new File(mContext.getFilesDir() + "/" + name + ".png");
                    if (image != null && image2 != null) {
                        imageCount = imageCount + 2;
                    }
                    return;
                }
                Log.e("UNSELECT IMAGE", unselected);
                Log.e("SELECT IMAGE", selected);
                //Save Unselected Image
                PicassoUtil.with(mContext)
                        .load(unselected, getTarget(mContext, name));
                //Save Selected Image
                PicassoUtil.with(mContext)
                        .load(selected, getTarget(mContext, name + "_highlight"));
            }

    }

    public static float convertDpToPixel(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static String toCamelCase(String str) {

        if (str == null) {
            return null;
        }

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    public static boolean checkActivityPresent(Context context){
        if(context == null){
            return false;
        }
        Activity activity = (Activity) context;
        boolean isDestroying = false;
        if(Build.VERSION.SDK_INT >= 17){
            isDestroying = activity.isDestroyed();
        }
        if(activity.isFinishing() || isDestroying){
            return false;
        }
        return true;
    }

    public static boolean checkActivityPresent(Activity context){
        if(context == null){
            return false;
        }
       // Activity activity = (Activity) context;
        boolean isDestroying = false;
        if(Build.VERSION.SDK_INT >= 17){
            isDestroying = context.isDestroyed();
        }
        if(context.isFinishing() || isDestroying){
            return false;
        }
        return true;
    }

    public static String getFileProviderAuthority(){
        return getApplicationContext().getString(R.string.file_provider_authority);
    }

    public static Bitmap getBitmap(@NonNull Context mContext, @Nullable String name,boolean selectedState) {
        File image;
        if(name != null){
            if(!selectedState){
                image = new File(mContext.getFilesDir()+"/"+name+".png");
            }else{
                image = new File(mContext.getFilesDir()+"/"+name+"_highlight.png");
            }
        }else{
            if (name != null) {
                image = new File(mContext.getFilesDir()+"/"+name+".png");
            }else{
                return null;
            }
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        if (bitmap == null){
            return   null;
        }
        if(DeviceUtils.isTablet(mContext)){
            if(ApplicationController.getApplicationConfig().screenHeight >= 2000
                    && ApplicationController.getApplicationConfig().screenWidth >= 1200){
                return Bitmap.createScaledBitmap(bitmap,60,60,true);

            }else{
                return Bitmap.createScaledBitmap(bitmap,30,30,true);
            }
        }
        else{
            if(ApplicationController.getApplicationConfig().screenHeight >= 1770
                    && ApplicationController.getApplicationConfig().screenWidth >= 1080){
                return Bitmap.createScaledBitmap(bitmap,60,60,true);

            }if(ApplicationController.getApplicationConfig().screenHeight >= 1100
                    && ApplicationController.getApplicationConfig().screenWidth >= 720){
                return Bitmap.createScaledBitmap(bitmap,40,40,true);
            }
            else{
                return Bitmap.createBitmap(bitmap);
            }
        }
    }



    //New Central Mentod to access the stored bitmaps
    public static Bitmap getBitmap(@NonNull Context mContext, @Nullable String name, @Nullable CarouselInfoData carouselInfoData,boolean selectedState) {
        File image;
        if(carouselInfoData != null){
            if(!selectedState){
                image = new File(mContext.getFilesDir()+"/"+carouselInfoData.name+".png");
            }else{
                image = new File(mContext.getFilesDir()+"/"+carouselInfoData.name+"_highlight.png");
            }
        }else{
            if (name != null) {
                image = new File(mContext.getFilesDir()+"/"+name+".png");
            }else{
                return null;
            }
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        if (bitmap == null){
            return   null;
        }
       /* if(DeviceUtils.isTablet(mContext)){
            if(ApplicationController.getApplicationConfig().screenHeight >= 2000
                    && ApplicationController.getApplicationConfig().screenWidth >= 1200){
                return Bitmap.createScaledBitmap(bitmap,60,60,true);

            }else{
                return Bitmap.createScaledBitmap(bitmap,30,30,true);
            }
        }
        else*/{
            if(ApplicationController.getApplicationConfig().screenHeight >= 1770
                    && ApplicationController.getApplicationConfig().screenWidth >= 1080){
                return Bitmap.createScaledBitmap(bitmap,60,60,true);

            }if(ApplicationController.getApplicationConfig().screenHeight >= 1100
                    && ApplicationController.getApplicationConfig().screenWidth >= 720){
                return Bitmap.createScaledBitmap(bitmap,40,40,true);
            }
            else{
                return Bitmap.createBitmap(bitmap);
            }
        }
    }

    private static final List<CardData> mDummyCardData = new ArrayList<>();
    private static final List<CarouselInfoData> mDummyCarouselInfoData = new ArrayList<>();

    public static List<CardData> getDummyCardData() {
        if (!mDummyCardData.isEmpty()) {
            return mDummyCardData;
        }
        for (int i = 0; i < 10; i++) {
            mDummyCardData.add(new CardData());
        }
        return mDummyCardData;
    }

    public static List<CarouselInfoData> getDummyCarouselInfoData() {
        if (!mDummyCarouselInfoData.isEmpty()) {
            return mDummyCarouselInfoData;
        }
        for (int i = 0; i < 10; i++) {
            mDummyCarouselInfoData.add(new CarouselInfoData());
        }
        return mDummyCarouselInfoData;
    }

    public static void addTabsThatHavePortraitBanner(String tabNameToAdd){
        if(tabsThatHavePortraitBanner != null){
            if(!tabsThatHavePortraitBanner.contains(tabNameToAdd)){
                tabsThatHavePortraitBanner.add(tabNameToAdd);
            }
        }
    }

    public static boolean doesCurrentTabHasPortraitBanner(String tabNameToCheck){
        if(tabNameToCheck != null){
            if (tabsThatHavePortraitBanner != null && tabsThatHavePortraitBanner.size() > 0) {
                 return tabsThatHavePortraitBanner.contains(tabNameToCheck);
            }else{
                return false;
            }
        }
        return false;
    }

    public static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }

    public static final String getSquareImageLink(CardData carouselData,boolean isTab) {
        if (carouselData == null || carouselData.images == null || carouselData.images.values == null || carouselData.images.values.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_SQUARE};
        if(carouselData != null
                && carouselData.generalInfo != null
                && carouselData.generalInfo.type != null
        ){
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_SQUARE_BANNER};
        }

        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : carouselData.images.values) {
                if(isTab){
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && (ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)||ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile))) {
                        return imageItem.link;
                    }
                }
                else{
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && (ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)||ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile))) {
                        return imageItem.link;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isValidEmailID(String emailId) {
        if (emailId == null || TextUtils.isEmpty(emailId)) {
            return false;
        }

        if (emailId.length() > 0) {
            int lengthFromDot = 0;
            if (emailId.indexOf(".") >= 0 && emailId.substring(emailId.indexOf(".")) != null) {
                lengthFromDot = emailId.substring(emailId.indexOf(".")).length();
                LoggerD.debugDownload("lengthFromDot- " + lengthFromDot + " emailId.substring(emailId.indexOf(\".\"))- " + emailId.substring(emailId.indexOf(".")));
            }
            if (emailId.contains("@") && emailId.contains(".") && !emailId.contains(" ") && lengthFromDot > 2) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public static String getUserType() {
        if (!Util.checkUserLoginStatus()){
            return "GUEST";
        }else if (Util.isPremiumUser()){
            return "PAID";
        }else {
            return "FREE";
        }
    }

    public static String getAgeFromDOB() {
        if (PrefUtils.getInstance().getPrefUserDOB()!=null&&!TextUtils.isEmpty(PrefUtils.getInstance().getPrefUserDOB())){
            Date date = null;
            SimpleDateFormat sdf = new SimpleDateFormat(yyyy_MM_dd);
            try {
                date = sdf.parse(PrefUtils.getInstance().getPrefUserDOB());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(date == null)
                return "0";

            Calendar dob = Calendar.getInstance();
            Calendar today = Calendar.getInstance();

            dob.setTime(date);

            int year = dob.get(Calendar.YEAR);
            int month = dob.get(Calendar.MONTH);
            int day = dob.get(Calendar.DAY_OF_MONTH);

            dob.set(year, month+1, day);

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                age--;
            }



            return String.valueOf(age);
        }
        return "0";
    }

    public static String getAgeString() {
        if (!TextUtils.isEmpty(Util.getAgeFromDOB())) {
            if (Integer.valueOf(Util.getAgeFromDOB()) >= 13 && Integer.valueOf(Util.getAgeFromDOB()) <= 17) {
                return "13to17yrs";
            } else if (Integer.valueOf(Util.getAgeFromDOB()) >= 18 && Integer.valueOf(Util.getAgeFromDOB()) <= 24) {
                return "18to24yrs";
            } else if (Integer.valueOf(Util.getAgeFromDOB()) >= 25 && Integer.valueOf(Util.getAgeFromDOB()) <= 34) {
                return "25to34yrs";
            } else if (Integer.valueOf(Util.getAgeFromDOB()) >= 35 && Integer.valueOf(Util.getAgeFromDOB()) <= 44) {
                return "35to44yrs";
            } else if (Integer.valueOf(Util.getAgeFromDOB()) >= 45 && Integer.valueOf(Util.getAgeFromDOB()) <= 54) {
                return "45to54yrs";
            } else if (Integer.valueOf(Util.getAgeFromDOB()) >= 55 && Integer.valueOf(Util.getAgeFromDOB()) <= 64) {
                return "55to64yrs";
            } else if (Integer.valueOf(Util.getAgeFromDOB()) >= 65) {
                return "65+yrs";
            }
        }
        return "NA";
    }

    public static String getGenderString() {
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefUserGender()) && PrefUtils.getInstance().getPrefUserGender().equalsIgnoreCase("m")) {
            return "male";
        } else if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefUserGender()) && PrefUtils.getInstance().getPrefUserGender().equalsIgnoreCase("f")) {
            return "female";
        }
        return "NA";
    }

    public static String getDurationString(Integer contentDurationMnts) {
        if (contentDurationMnts >= 1 && contentDurationMnts <= 5) {
            return "1-5mins";
        } else if (contentDurationMnts >= 6 && contentDurationMnts <= 11) {
            return "5-12mins";
        } else if (contentDurationMnts >= 12 && contentDurationMnts <= 29) {
            return "12-30mins";
        } else if (contentDurationMnts >= 30 && contentDurationMnts <= 89) {
            return "30-90mins";
        } else if (contentDurationMnts >= 90) {
            return "90mins+";
        }
        return "0mins";
    }


    public static class DateFormat {
        public static String dd_MM = "dd-MMM";
        public static String dd_MM_yyyy = "dd - MMM - yyyy";
        public static String d_m_y = "dd-MM-yyyy";
        public static String yyyy_MM_dd="yyyy-MM-dd";
        public static final SimpleDateFormat DAY_MONTH_FORMATTER = new SimpleDateFormat(dd_MM, Locale.getDefault());
        public static final SimpleDateFormat DAY_MONTH_YEAR_FORMATTER= new SimpleDateFormat(dd_MM_yyyy, Locale.getDefault());
        public static final SimpleDateFormat DAY_MONTH_YEAR_FORMATTER_NUM= new SimpleDateFormat(d_m_y, Locale.getDefault());
        public static final SimpleDateFormat YEAR_MONTH_DAY_FORMATTER_NUM = new SimpleDateFormat(yyyy_MM_dd, Locale.getDefault());
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static String getSplitGlobalServiceId(String globalServiceId) {
        if (!TextUtils.isEmpty(globalServiceId)) {
            String[] gidArray = globalServiceId.split(",");
            if (gidArray.length > 0) {
                return gidArray[0];
            }
        }
        return globalServiceId;
    }

    public static void loadAdError(LoadAdError error,String layoutType){
        // Gets the domain from which the error came.
        String errorDomain = error.getDomain();
        // Gets the error code. See
        // https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest#constant-summary
        // for a list of possible codes.
        int errorCode = error.getCode();
        // Gets an error message.
        String errorMessage = error.getMessage();
        // Gets additional response information about the request. See
        // https://developers.google.com/admob/android/response-info for more
        // information.
        ResponseInfo responseInfo = error.getResponseInfo();
        // Gets the cause of the error, if available.
        AdError cause = error.getCause();
        // All of this information is available via the error's toString() method.
        Log.d("Ads:: "+layoutType, error.toString());
    }
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                   ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
