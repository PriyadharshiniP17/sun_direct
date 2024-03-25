package com.myplex.myplex.ui.fragment.epg;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BaseTarget;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Created by Kristoffer.
 */
public class EPGUtil {
    private static final String TAG = "EPGUtil";
    private static final DateTimeFormatter dtfShortTime = DateTimeFormat.forPattern("hh:mm a");
    //private static Picasso picasso = null;

    public static String getShortTime(long timeMillis) {
        return dtfShortTime.print(timeMillis);
    }

    public static String getWeekdayName(long dateMillis) {
        LocalDate date = new LocalDate(dateMillis);
        return date.dayOfWeek().getAsText();
    }

    public static int getDayInteger(long dateMillis) {
        LocalDate date = new LocalDate(dateMillis);
        return date.dayOfYear().get();
    }

    public static void loadImageInto(Context context, String url, int width, int height, BaseTarget target) {
        if(null != target)
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .override(width, height)
                    .into(target);

       /* Glide.with(context).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(target);*/
    }
}
