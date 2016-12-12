package com.lex.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Alex on 28/11/2016.
 */

public class Utility {

    public static String formatTemperature(double temperature, boolean isMetric){
        double temp;

        if(!isMetric){
            temp = 9 * temperature / 5 + 32;
        }
        else{
            temp = temperature;
        }

        return String.format("%.0fº", temp);
    }

    public static String formatDate(long dateInMillis){
        Date date = new Date(dateInMillis);

        return DateFormat.getDateInstance().format(date);
    }

    public static String getFriendlyDayString(Context context, long dateInMillis){
        String formattedDate;

        SimpleDateFormat sdfThisWeek = new SimpleDateFormat("EEEE");
        SimpleDateFormat sdfOtherWeeks = new SimpleDateFormat("EEE MMM d");

        Calendar currentDate = new GregorianCalendar();
        int currentDateDayOfYear = currentDate.get(Calendar.DAY_OF_YEAR);

        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(dateInMillis);
        int dateDayOfYear = date.get(Calendar.DAY_OF_YEAR);

        if(dateDayOfYear == currentDateDayOfYear){
            formattedDate = context.getString(R.string.today);
        }
        else if(dateDayOfYear == currentDateDayOfYear + 1){
            formattedDate = context.getString(R.string.tomorrow);
        }
        else if(date.get(Calendar.WEEK_OF_YEAR) == currentDate.get(Calendar.WEEK_OF_YEAR)){
            formattedDate = sdfThisWeek.format(date.getTime());
        }
        else{
            formattedDate = sdfOtherWeeks.format(date.getTime());
        }

        return formattedDate;
    }

    public static String getPreferredLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_default_location));
    }

    public static boolean isMetric(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(context.getString(R.string.pref_temperature_unit_key),
                context.getString(R.string.pref_temperature_unit_metric))
                .equals(context.getString(R.string.pref_temperature_unit_metric));
    }

}
