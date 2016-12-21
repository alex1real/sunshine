package com.lex.sunshine.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.lex.sunshine.BuildConfig;
import com.lex.sunshine.db.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Alex on 21/12/2016.
 */

public class SunshineService extends IntentService {

    /*************
     * Constants *
     ************/
    public static final String LOCATION_QUERY_EXTRA = "lqe";
    public static final String LOG_TAG = SunshineService.class.getSimpleName();

    /****************
     * Constructors *
     ***************/
    public SunshineService(){
        super("Sunshine");
    }

    public SunshineService(String name) {
        super(name);
    }

    /**************
     * Overriders *
     *************/
    @Override
    protected void onHandleIntent(Intent intent) {
        String locationQuery = intent.getStringExtra(SunshineService.LOCATION_QUERY_EXTRA);

        // These two need to be declared outside the try/catch so that they can be closed in the
        // finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try{
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";

            Uri uri = Uri.parse(FORECAST_BASE_URL);
            Uri.Builder uriBuilder = uri.buildUpon();

            uriBuilder.appendQueryParameter(QUERY_PARAM, locationQuery);
            uriBuilder.appendQueryParameter(FORMAT_PARAM, format);
            uriBuilder.appendQueryParameter(UNITS_PARAM, units);
            uriBuilder.appendQueryParameter(DAYS_PARAM, Integer.toString(numDays));
            uriBuilder.appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY);

            uri = uriBuilder.build();

            URL url = new URL(uri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null){
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a lot easier if you print out completed buffer for
                // debugging.
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0){
                // Stream was empty. No point in parsing.
                return;
            }

            forecastJsonStr = buffer.toString();
            this.getWeatherDataFromJson(forecastJsonStr, locationQuery);
        }
        catch(IOException e){
            Log.e(LOG_TAG,"Error ", e);
        }
        catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        finally{
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }
                catch (final IOException e){
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try{
            getWeatherDataFromJson(forecastJsonStr, locationQuery);
        }
        catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /*******************
     * Private Methods *
     ******************/
    /*
     * Take the string representing the complete forecast in Json format and pull out the data that
     * we need to construct the String needed for the wireframes.
     *
     * Constructor takes the JSON string and converts it into an Object hierarchy for us.
     */
    /*
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSettings - The location string used to request data from the server.
     * @param cityName - A human-readable city name, e.g "Mountain View"
     * @param lat - the latitude of the city
     * @param lon - the longitude of the city
     * @return the row ID of the added location
     */
    protected long addLocation(String locationSettings, String cityName, double lat, double lon){
        long locationId;

        // Instructions: First, check if the location with city name exists in the db.
        Cursor cursor = this.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                null,
                WeatherContract.LocationEntry.TABLE_NAME + "." +
                        WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS + " = ?",
                new String[]{locationSettings},
                null
        );

        // If it exists, return the current ID.
        if(cursor.moveToFirst()){
            int columnId = cursor.getColumnIndex("_id");

            locationId = cursor.getLong(columnId);
        }
        // Otherwise, insert it using the content resolver and the base URI
        else{
            ContentValues locationValues = new ContentValues();
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS, locationSettings);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri locationUri = this.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            locationId = ContentUris.parseId(locationUri);
        }

        return locationId;

    }

    private void getWeatherDataFromJson(String forecastJsonStr,
                                        String locationSettings)
            throws JSONException{
        // Now we have a string representing the complete forecast in JSON Format.

        // These are the names of the JSON objects that need to be extracted

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information. Each day's forecast info is an element of the "list" array
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WIND_SPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        try{
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSettings, cityName, cityLatitude, cityLongitude);

            // Insert the new weather information into the database
            ContentValues[] contentValuesArray = new ContentValues[weatherArray.length()];

            // These are the values that will be collected
            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            GregorianCalendar gc = new GregorianCalendar();
            ContentValues weatherValues;

            for(int i = 0; i < weatherArray.length(); i++){
                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WIND_SPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                // Description is in a child array called "weather", which is 1 element lon.
                // That element also contains a weather code.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                // Temperatures are in a child object called "temp". Try not to name variables
                // "temp" when working with temperature. It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, gc.getTimeInMillis());
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                contentValuesArray[i] = weatherValues;

                // Iterating to the next day
                gc.add(Calendar.DAY_OF_WEEK, 1);
            }

            // Add to database
            if(contentValuesArray.length > 0){
                int numAffectedRows = this.getContentResolver().bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        contentValuesArray);
            }

        }
        catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /*****************
     * Inner Classes *
     ****************/
    public static class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, SunshineService.class);
            sendIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                    intent.getStringExtra(SunshineService.LOCATION_QUERY_EXTRA));
            context.startService(sendIntent);
        }
    }
}
