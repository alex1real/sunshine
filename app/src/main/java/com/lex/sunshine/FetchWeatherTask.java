package com.lex.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Created by Alex on 04/07/2016.
 *                                    AsyncTask<Params, Progress, Result>*/
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private ArrayAdapter<String> forecastAdapter;
    private final Context context;

    private boolean debug = true;

    /*
     * Constructors
     */
    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter){
        this.context = context;
        this.forecastAdapter = forecastAdapter;
    }

    /*
     * Protected Methods
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
        Cursor cursor = context.getContentResolver().query(
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

            Uri locationUri = context.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            locationId = ContentUris.parseId(locationUri);
        }

        return locationId;

    }

    @Override
    protected String[] doInBackground(String... params){
        // If there's no location information, there's nothing to look up. Verify size of params.
        if(params.length == 0){
            return null;
        }

        String locationQuery = params[0];

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

            uriBuilder.appendQueryParameter(QUERY_PARAM, params[0]);
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
                return null;
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
                return null;
            }

            forecastJsonStr = buffer.toString();
        }
        catch(IOException e){
            Log.e(LOG_TAG,"Error ", e);

            return null;
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
            return  getWeatherDataFromJson(forecastJsonStr, locationQuery);
        }
        catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String[] result){
        if(result != null && forecastAdapter != null){
            forecastAdapter.clear();
            for(String dayForecastStr : result){
                forecastAdapter.addAll(dayForecastStr);
            }
            // New data is back form the server
        }
    }

    /*
     * Private Methods
     */
    /*
     * This code will allow the FetchWeatherTask to continue to return the strings that the UX
     * expects so that we can continue to test the application even once we being using the database
     */
    private String[] convertContentValuesToUXFormat(ContentValues[] contentValuesArray){
        // Return strings to keep UI functional for now
        String[] resultStrs = new String[contentValuesArray.length];

        String readableDate;
        String weatherDesc;
        String highAndLow;

        for(int i = 0; i < contentValuesArray.length; i++){
            ContentValues weatherValues = contentValuesArray[i];

            readableDate = getReadableDateString(weatherValues.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE));

            highAndLow = this.formatHighLows(
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP),
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));

            weatherDesc = weatherValues.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

            resultStrs[i] = readableDate + " - " + weatherDesc + " - " + highAndLow;
        }

        return resultStrs;
    }

    /*
     * Prepare weather Low/Highs for presentation
     */
    private String formatHighLows(double high, double low){
        /*
         Data is fetched in Celsius by default,
         If user prefers to see in Fahrenheit, convert the values here.
         We do that rather than fetching in Fahrenheit so that the user can change this option
         without us having to re-fetch the data once we start storing the values in a database.
         */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unitType = sharedPreferences.getString(
                context.getString(R.string.pref_temperature_unit_key),
                context.getString(R.string.pref_temperature_unit_metric));

        if(unitType.equals(context.getString(R.string.pref_temperature_unit_imperial))){
            high = (high * 1.8) + 32;
            low = (low *1.8) + 32;
        }
        else if(!unitType.equals(context.getString(R.string.pref_temperature_unit_metric))){
            Log.d(LOG_TAG, "Unit type not found: " + unitType);
        }

        // For presentation, assume the user doesn't care about tenth of a degree
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = "Max " + roundedHigh + " / " + "Min " + roundedLow;

        return highLowStr;
    }

    /*
     * The date/time convesion time code is going to be moved outside the async task later, so for
     * convenience we're breaking it out int its own method now.
     */
    private String getReadableDateString(long time){
        // Because the API returns an unix timestamp (measured in seconds), it must be converted to
        // milliseconds in order to be converted to a valid date.
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(time);

        SimpleDateFormat sdf = new SimpleDateFormat("E, MMM d");

        return sdf.format(gc.getTime()).toString();
    }

    /*
     * Take the string representing the complete forecast in Json format and pull out the data that
     * we need to construct the String needed for the wireframes.
     *
     * Constructor takes the JSON string and converts it into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr,
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
                int numAffectedRows = context.getContentResolver().bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        contentValuesArray);
            }

            // Sort Order: Ascending, by date
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            Uri weatherForecastLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                    locationSettings,
                    new GregorianCalendar().getTimeInMillis()
            );

            Cursor cursor = context.getContentResolver().query(weatherForecastLocationUri,
                    null, null, null, sortOrder);

            contentValuesArray = new ContentValues[cursor.getCount()];
            if(cursor.moveToFirst()){
                int i = 0;
                do{
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, cv);
                    contentValuesArray[i++] = cv;
                }while(cursor.moveToNext());
            }

            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + contentValuesArray.length + " inserted.");

            String[] resultsStr = this.convertContentValuesToUXFormat(contentValuesArray);

            return resultsStr;

        }
        catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }
}
