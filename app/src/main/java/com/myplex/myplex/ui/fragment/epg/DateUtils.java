package com.myplex.myplex.ui.fragment.epg;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Divya on 5/18/2017.
 */

public class DateUtils {

    public static String formatMillis(long millis) {
        String result = "";
        int hr = (int) millis / 3600000;
        millis %= 3600000;
        int min = (int) millis / 60000;
        millis %= 60000;
        int sec = (int) millis / 1000;
        if (hr > 0) {
            result += hr + ":";
        }
        if (min >= 0) {
            if (min > 9) {
                result += min + ":";
            } else {
                result += "0" + min + ":";
            }
        }
        if (sec > 9) {
            result += sec;
        } else {
            result += "0" + sec;
        }
        return result;
    }

    public static String getDate(long dateinmillisec) {
        Date date = new Date(dateinmillisec);
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
        /*SimpleDateFormat df2 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");*/
        String dateText = df2.format(date);
        System.out.println(dateText);
        return dateText;
    }

    public static String getDateYear(long dateinmillisec) {
        Date date = new Date(dateinmillisec);
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy");
        /*SimpleDateFormat df2 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");*/
        String dateText = df2.format(date);
        System.out.println(dateText);
        return dateText;
    }

    public static final String millisecondsTo12HourFormat(long millisecond) {

        Date date = new Date(millisecond);
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(date);

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");

       /* format.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));*/
        String formatted = format.format(date);
        return formatted;
    }

    public static final String millisecondsTo24HourFormat(long millisecond) {

        Date date = new Date(millisecond);
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(date);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

       /* format.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));*/
        String formatted = format.format(date);
        return formatted;
    }
    public static final String getMinites(long millisecond) {

        Date date = new Date(millisecond);
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(date);

        SimpleDateFormat format = new SimpleDateFormat("mm");

        /* format.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));*/
        String formatted = format.format(date);
        return formatted;
    }

    public static String getDate(String dateInMilliseconds,String dateFormat) {
        if(dateInMilliseconds == null || dateInMilliseconds.trim().length()<1 ||
                dateInMilliseconds.equalsIgnoreCase("null"))
            return "";

        SimpleDateFormat input = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat output = new SimpleDateFormat("dd MMM"); // dd MMM yyyy - dd MMM
       try {
            return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
//            return output.format(input.parse(DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString()));                 // parse input
            //   tripDate.setText(output.format(oneWayTripDate));    // format output
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isPastProgram(long end) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        final long now = cal.getTimeInMillis();
        return end < now;
    }
}
