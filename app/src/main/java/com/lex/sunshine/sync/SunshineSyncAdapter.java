package com.lex.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.lex.sunshine.BuildConfig;
import com.lex.sunshine.DetailActivity;
import com.lex.sunshine.MainActivity;
import com.lex.sunshine.R;
import com.lex.sunshine.Utility;
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

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

    /*************
     * Constants *
     ************/
    public final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 60 * 24;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/2;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    // These indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    /****************
     * Constructors *
     ***************/
    public SunshineSyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
    }

    /**************
     * Overriders *
     *************/
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        String locationQuery = Utility.getPreferredLocation(getContext());

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

        Log.d(LOG_TAG, "Sync complete");
    }

    /******************
     * Public Methods *
     *****************/
    /*
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime){
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            // We can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle())
                    .build();

            ContentResolver.requestSync(request);
        }
        else{
            ContentResolver.addPeriodicSync(account,
                    authority,
                    new Bundle(),
                    syncInterval);
        }
    }

    /*
     * Helper method to have sync adapter sync immediatly
     * @param context - The context used to access the account service
     */
    public static void syncImmediately(Context context){
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority),
                bundle);
    }

    /*
     * Helper method to get a fake account to be used with SyncAdapter, or make a new one if the
     * faze doesn't exist yet. If we make a new account, we call the onAccountCreated method so we
     * initialize things.
     *
     * @param context - The context used for access the account service.
     * @return - a fake account
     */
    public static Account getSyncAccount(Context context){
        // Get an instance of the Android Account Manager
        AccountManager accountManager =
                (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount =
                new Account(context.getString(R.string.app_name),
                        context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist.
        if(accountManager.getPassword(newAccount) == null){

            // Add the account and account type, no password or user data
            // If successful, return the Account object, otherwise report an error
            if(!accountManager.addAccountExplicitly(newAccount, "", null)){
                return null;
            }

            /*
             * If you don't set android:syncable="true" in your <provider> element in manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1) here.
             */

            onAccountCreated(newAccount, context);
        }

        return newAccount;
    }

    public static void initializeSyncAdapter(Context context){
        getSyncAccount(context);
    }

    public static void onAccountCreated(Account newAccount, Context context){
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled
         */
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority),
                true);

        /*
         * Finally, do a sync to get things started
         */
        syncImmediately(context);
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
        Cursor cursor = getContext().getContentResolver().query(
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

            Uri locationUri = getContext().getContentResolver().insert(
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

            int numAffectedRows;

            // Add to database
            if(contentValuesArray.length > 0){
                getContext().getContentResolver().bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        contentValuesArray);

                notifyWeather();
            }

            // Delete old data to avoid builind an endless history
            Calendar yesterday = new GregorianCalendar();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);

            getContext().getContentResolver().delete(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
                    new String[]{Long.toString(yesterday.getTimeInMillis())}
            );

        }
        catch(JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void notifyWeather(){
        Context context = getContext();

        // Checking the last update and notify it it the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayNotificationKey = context.getString(R.string.pref_enable_notification_key);
        boolean defaultNotificationSetting = Boolean.parseBoolean(
                context.getString(R.string.pref_enable_notification_defalut));
        boolean displayNotifications = prefs.getBoolean(displayNotificationKey,
                defaultNotificationSetting);

        if(displayNotifications) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);
            long currentTimeMillis = System.currentTimeMillis();

            if ((currentTimeMillis - lastSync) > DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.
                String location = Utility.getPreferredLocation(context);

                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location,
                        currentTimeMillis);

                // Query the content provider
                Cursor cursor = context.getContentResolver().query(weatherUri,
                        NOTIFY_WEATHER_PROJECTION,
                        null, null, null);

                if (cursor.moveToFirst()) {
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = Utility.selectIcon(weatherId, Utility.COLOR_COLORFUL);
                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast
                    String contentText = String.format(context.getString(R.string.format_notification),
                            desc,
                            Utility.formatTemperature(context, high),
                            Utility.formatTemperature(context, low));

                    // Build the notification here
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
                    notificationBuilder.setSmallIcon(iconId);
                    notificationBuilder.setContentTitle(title);
                    notificationBuilder.setContentText(contentText);

                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.setData(weatherUri);

                    // The stack builder object will contain an artificial back stack for the started
                    // Activity.
                    // This ensures that navigation backwards from the Activity leads out to
                    // application's home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    // Add the stack builder for the Intent (but not the Intent itself)
                    stackBuilder.addParentStack(DetailActivity.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(intent);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    notificationBuilder.setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);

                    // notificationId allows you to update the notification later on.
                    notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());

                    // Refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, currentTimeMillis);
                    editor.commit();
                }
            }
        }
    }

}
