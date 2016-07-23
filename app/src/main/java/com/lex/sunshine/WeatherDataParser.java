package com.lex.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alex on 07/07/2016.
 */
public class WeatherDataParser {

    private final String LOG_TAG = WeatherDataParser.class.getSimpleName();
    private final String units;

    /***************
     * Constructor *
     **************/
    /*
     * Unit: (metric/Imperial)
     */
    public WeatherDataParser(String units){
        this.units = units;
    }

    /******************
     * Public Methods *
     *****************/
    /**********************************************************************************************
     * Take the String representing the complete forecast in JSON Format and pull out the data we *
     * need to construct the Strings needed for the wireframes.                                   *
     *********************************************************************************************/
    public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays){

        String[] forecastList = null;

        try {
            //Get JSON Root node
            JSONObject jsonObject = new JSONObject(forecastJsonStr);
            //Navigate to the forecast list node
            JSONArray jsonForecastList = jsonObject.getJSONArray("list");

            forecastList = new String[numDays];

            long dateUnixFormat;
            double min;
            double max;
            String weatherDesc;

            //Iterate through forecasts
            for(int i = 0; i < numDays; i ++){

                JSONObject forecast = jsonForecastList.getJSONObject(i);
                dateUnixFormat = forecast.getLong("dt");

                JSONObject temperture = forecast.getJSONObject("temp");
                min = temperture.getDouble("min");
                max = temperture.getDouble("max");

                JSONArray weatherList = forecast.getJSONArray("weather");
                //Get the first weather in the List.
                //In the sample data, always there was only one weather in weather list.
                JSONObject weather = weatherList.getJSONObject(0);
                //String weatherDesc = weather.getString("description"); -- Same information of "main" tag, but more detailed.
                weatherDesc = weather.getString("main");

                forecastList[i] = formatForecastString(dateUnixFormat, min, max, weatherDesc);
            }

        }
        catch(JSONException e){

        }

        return forecastList;
    }

    /*******************
     * Private Methods *
     ******************/
    /****************************************************************************
     * Format the forecast String to this pattern: "Mon, Jun 1 - Clear - 18/13" *
     ***************************************************************************/
    private String formatForecastString(long dateUnixFormat,
                                               double minTemperature,
                                               double maxTemperature,
                                               String weatherDesc){

        String forecast = null;

        String formattedDate = getReadableDateString(dateUnixFormat);

        forecast = String.format("%s - %s - %.0f/%.0f", formattedDate,
                                                        weatherDesc,
                                                        maxTemperature,
                                                        minTemperature);

        return forecast;
    }

    /* The date/time conversion code is going to be moved outside the AsyncTask later,
     * so for convenience we're breaking it out into its own method now. */
    private String getReadableDateString(long time){
        Date date = new Date(time * 1000);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd");

        return sdf.format(date);
    }

}
