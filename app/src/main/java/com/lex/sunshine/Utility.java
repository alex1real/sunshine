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

    public static final int COLOR_COLORFUL = 0;
    public static final int COLOR_BLACK_AND_WHITE = 1;

    public static String formatDate(long dateInMillis){
        Date date = new Date(dateInMillis);

        return DateFormat.getDateInstance().format(date);
    }

    public static String formatHumidity(Context context, double humidity){
        return String.format(context.getString(R.string.format_humidity), humidity);
    }

    public static String formatPressure(Context context, double pressure){
        return String.format(context.getString(R.string.format_pressure), pressure);
    }

    public static String formatTemperature(Context context, double temperature, boolean isMetric){
        double temp;

        if(!isMetric){
            temp = 9 * temperature / 5 + 32;
        }
        else{
            temp = temperature;
        }

        return String.format(context.getString(R.string.format_temperature), temp);
    }

    public static String formatWind(Context context, double speed, double degrees){
        String direction;

        degrees = degrees % 360;

        if(degrees >= 337.5 || degrees < 22.5){
            direction = "N";
        }
        else if(degrees >= 22.5 || degrees < 67.5){
            direction = "NE";
        }
        else if(degrees >= 67.5 || degrees < 112.5){
            direction = "E";
        }
        else if(degrees >= 112.5 || degrees < 157.5){
            direction = "SE";
        }
        else if(degrees >= 157.5 || degrees < 202.5){
            direction = "S";
        }
        else if(degrees >= 202.5 || degrees < 247.5){
            direction = "SW";
        }
        else if(degrees >= 247.5 || degrees < 292.5){
            direction = "W";
        }
        else{
            direction = "NW";
        }

        return String.format(context.getString(R.string.format_wind_kmh), speed, direction);
    }

    public static String getFriendlyDayString(Context context, long dateInMillis){
        String formattedDate;

        SimpleDateFormat sdfOtherWeeks = new SimpleDateFormat("EEE MMM d");

        Calendar currentDate = new GregorianCalendar();

        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(dateInMillis);

        if(date.get(Calendar.WEEK_OF_YEAR) == currentDate.get(Calendar.WEEK_OF_YEAR)){
            formattedDate = getWeekDay(dateInMillis);
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

    public static String getWeekDay(long dateInMillis){
        SimpleDateFormat sdfThisWeek = new SimpleDateFormat("EEEE");

        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(dateInMillis);
        int dayOfYear = date.get(Calendar.DAY_OF_YEAR);

        Calendar currentDate = new GregorianCalendar();
        int currentDayOfYear = currentDate.get(Calendar.DAY_OF_YEAR);

        String dayOfWeek;

        if(dayOfYear == currentDayOfYear){
            dayOfWeek = "Today";
        }
        else if(dayOfYear == currentDayOfYear + 1){
            dayOfWeek = "Tomorrow";
        }
        else{
            dayOfWeek = sdfThisWeek.format(date.getTime());
        }

        return dayOfWeek;
    }

    public static String getShortDate(long dateInMillis){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d");

        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(dateInMillis);

        return sdf.format(date.getTime());
    }

    public static boolean isMetric(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(context.getString(R.string.pref_temperature_unit_key),
                context.getString(R.string.pref_temperature_unit_metric))
                .equals(context.getString(R.string.pref_temperature_unit_metric));
    }

    public static int selectIcon(int weatherId, int color){
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if(weatherId == WeatherConditionMap.WEATHER_CLEAR_ID){
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.ic_clear;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.art_clear;
                default:
                    return R.drawable.ic_clear;
            }
        }
        else if(weatherId >= WeatherConditionMap.WEATHER_CLOUDS_MIN
                && weatherId <= WeatherConditionMap.WEATHER_CLOUDS_MAX){
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.ic_cloudy;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.art_clouds;
                default:
                    return R.drawable.ic_cloudy;
            }
        }
        else if(weatherId >= WeatherConditionMap.WEATHER_FOG_MIN
                && weatherId <= WeatherConditionMap.WEATHER_FOG_MAX){
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.ic_fog;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.art_fog;
                default:
                    return R.drawable.ic_fog;
            }
        }
        else if(weatherId == WeatherConditionMap.WEATHER_LIGHT_CLOUDS_ID){
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.ic_light_clouds;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.art_light_clouds;
                default:
                    return R.drawable.ic_light_clouds;
            }
        }
        else if(weatherId >= WeatherConditionMap.WEATHER_LIGHT_RAIN_MIN
                && weatherId <= WeatherConditionMap.WEATHER_LIGHT_RAIN_MAX){
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.ic_light_rain;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.art_light_rain;
                default:
                    return R.drawable.ic_light_rain;
            }
        }
        else if(weatherId >= WeatherConditionMap.WEATHER_RAIN_MIN
                && weatherId <= WeatherConditionMap.WEATHER_RAIN_MAX){
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.ic_rain;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.art_rain;
                default:
                    return R.drawable.ic_rain;
            }
        }
        else if(weatherId >= WeatherConditionMap.WEATHER_SNOW_MIN
                && weatherId <= WeatherConditionMap.WEATHER_SNOW_MAX){
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.art_snow;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.ic_snow;
                default:
                    return R.drawable.ic_snow;
            }
        }
        else if((weatherId >= WeatherConditionMap.WEATHER_STORM_MIN
                && weatherId <= WeatherConditionMap.WEATHER_STORM_MAX)
                || (weatherId >= WeatherConditionMap.WEATHER_STORM2_MIN
                && weatherId <= WeatherConditionMap.WEATHER_STORM2_MAX)){
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.ic_storm;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.art_storm;
                default:
                    return R.drawable.ic_storm;
            }
        }
        else{
            switch(color){
                case(Utility.COLOR_BLACK_AND_WHITE):
                    return R.drawable.ic_clear;
                case(Utility.COLOR_COLORFUL):
                    return R.drawable.art_clear;
                default:
                    return R.drawable.ic_clear;
            }
        }
    }

}
