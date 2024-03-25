package com.myplex.myplex.ui.fragment.epg;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by gopi on 14/3/18.
 */

public class DateHelper {
    private static DateHelper dateHelperInstance = new DateHelper();
    public  Calendar calInstance;
    public  DateTime dateTimeInstance;
    public long timezoneOffsetValue = 0l;
    public long elapsedtime = 0l;


    public static DateHelper getInstance(){
        return dateHelperInstance;
    }

    public  void setCurrentLocalTime(String date){
        try
        {
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            //localSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
            Calendar localCalendar = Calendar.getInstance();
            Date localDate = localSimpleDateFormat.parse(date);
            //Date localDate = localSimpleDateFormat.parse("TUE, 13 Mar 2018 19:44:12 GMT+17:30");
            Log.e("EPG"," current calendar time date"+ DateTime.now());
            localCalendar.setTime(localDate);
            this.calInstance = localCalendar;
            elapsedtime = 0l;
            Log.e("EPG"," current calendar time in millis "+ localCalendar.getTimeInMillis());
            Log.e("EPG"," current time in millis "+ System.currentTimeMillis());

        }
        catch (Exception localException)
        {
            localException.printStackTrace();
        }
    }

/*    public  void setCurrentLocalTime(String date){
        try
        {
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            CustomLog.e("EPG"," timezone display name"+ localSimpleDateFormat.getTimeZone().getDisplayName());
           localSimpleDateFormat.setTimeZone(TimeZone.getDefault());


            Calendar localCalendar = Calendar.getInstance();
            Date localDate = localSimpleDateFormat.parse(date);
            DateTime dt = new DateTime(localDate);
            // DateTimeZone dtZone = DateTimeZone.forID("America/New_York");

            DateTimeZone dtZone =   DateTimeZone.forTimeZone(TimeZone.getDefault());
            DateTimeZone.setDefault(dtZone);


            //Date localDate = localSimpleDateFormat.parse("TUE, 13 Mar 2018 19:44:12 GMT+17:30");
            CustomLog.e("EPG"," current calendar time date"+ DateTime.now());
            localCalendar.setTime(localDate);
            CustomLog.e("EPG"," current calendar time date"+ localDate);
            if(this.calInstance != null)
                this.calInstance = null;
            this.calInstance = localCalendar;
            CustomLog.e("EPG"," current calendar time in millis "+ localCalendar.getTimeInMillis());
            CustomLog.e("EPG"," current time in millis "+ System.currentTimeMillis());

        }
        catch (Exception localException)
        {
            localException.printStackTrace();
        }
    }*/




    public long getTimezoneOffsetValue(){
        return  timezoneOffsetValue;
    }
    public  long getCurrentLocalTime(){
        if(this.calInstance != null)
            return this.calInstance.getTimeInMillis() +elapsedtime /*- 18000000*/;
      //  Log.e("EPG"," calinstance null system current time  "+ System.currentTimeMillis());
        return System.currentTimeMillis();

    }

    public void setElapseTime(long timefromstartinmillis){
        elapsedtime = elapsedtime + timefromstartinmillis;
      //  CustomLog.e("EPG","timeline head elapsed time "+elapsedtime);
    }

    public long getDayTimeInMillis()
    {
        DateTime localDateTime = new DateTime(getCurrentLocalTime());
        return new Duration(localDateTime, localDateTime.plusDays(1).withTimeAtStartOfDay()).getMillis();
    }

    public long getElapsedTime()
    {
        Calendar localCalendar = Calendar.getInstance();
        long l = getCurrentLocalTime();
        localCalendar.setTimeInMillis(l);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);
        return l - localCalendar.getTimeInMillis();
    }

    public String getTimeZoneDisplayName(){
     Date date = new Date(calInstance != null ?this.calInstance.getTimeInMillis():System.currentTimeMillis());
     return   TimeZone.getDefault().getDisplayName() +" ("+ TimeZone.getDefault().getDisplayName(TimeZone.getDefault().inDaylightTime(date),TimeZone.SHORT)+")";
    // return   TimeZone.getDefault().getDisplayName();
    }
/*   public  void setCurrentLocalTime(String date){
        try
        {
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            Date localDate = localSimpleDateFormat.parse(date);

            DateTime dt = new DateTime(localDate);
            //DateTimeZone dtZone = DateTimeZone.forID("Brazil/East");
            DateTimeZone dtZone = DateTimeZone.forID("Etc/GMT");
            DateTime dtus = dt.withZone(dtZone);

            Date dateBrazil = dtus.toLocalDateTime().toDate();


            Calendar localCalendar = Calendar.getInstance();
            localCalendar.setTime(dateBrazil);
            this.calInstance = localCalendar;
            this.dateTimeInstance= dt;

            CustomLog.e("EPG"," calendar instance time in millis "+ calInstance.getTimeInMillis());
            CustomLog.e("EPG"," current calendar time in millis "+ localCalendar.getTimeInMillis());
            CustomLog.e("EPG"," current time in millis "+ System.currentTimeMillis());
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
        }
    }*/
}
