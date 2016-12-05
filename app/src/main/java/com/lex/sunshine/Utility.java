package com.lex.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Alex on 28/11/2016.
 */

public class Utility {
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

    public static String formatTemperature(double temperature, boolean isMetric){
        double temp;

        if(!isMetric){
            temp = 9 * temperature / 5 + 32;
        }
        else{
            temp = temperature;
        }

        return String.format("%.0f", temp);
    }

    public static String formatDate(long dateInMillis){
        Date date = new Date(dateInMillis);

        return DateFormat.getDateInstance().format(date);
    }
}
