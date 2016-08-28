package com.lex.sunshine;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.lex.sunshine.db.WeatherContract;
import com.lex.sunshine.db.WeatherDbHelper;

/**
 * Created by Alex on 22/08/2016.
 */
public class WeatherProvider extends ContentProvider {

    // The Uri Matcher used by this Content Provider
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private WeatherDbHelper weatherDbHelper;

    protected static final int WEATHER = 100;
    protected static final int WEATHER_WITH_LOCATION = 101;
    protected static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    protected static final int LOCATION = 300;

    private static final SQLiteQueryBuilder weatherByLocationSettingQueryBuilder;

    // This "static" block works for a static class in the same way as a constructor, for a object
    // It's executed only once, when the class is loaded in the JVM and no more
    // (like in class initializations)
    static {
        weatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        // This is an inner join wich looks like
        // weather INNER JOIN location ON weather.location_id = location._id
        weatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN "
                + WeatherContract.LocationEntry.TABLE_NAME
                + " ON " + WeatherContract.WeatherEntry.TABLE_NAME + "."
                + WeatherContract.WeatherEntry.COLUMN_LOC_KEY
                + " = " + WeatherContract.LocationEntry.TABLE_NAME + "."
                + WeatherContract.WeatherEntry._ID
        );
    }

    /*
     * Constants: Search Specs
     */
    // location.location_settings = ?
    private static final String locationSettingsSelection =
            WeatherContract.LocationEntry.TABLE_NAME + "."
            + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS + " = ?";

    //location.location_settings = ? AND date = ?
    private static final String locationSettingsAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME + "."
                    + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS + " = ? AND "
                    + WeatherContract.WeatherEntry.TABLE_NAME + "."
                    + WeatherContract.WeatherEntry.COLUMN_DATE + " = ?";

    // location.location_settings = ? AND date >= ?
    private static final String locationSettingsWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME + "."
            + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS + " = ? AND "
            + WeatherContract.WeatherEntry.TABLE_NAME + "."
            + WeatherContract.WeatherEntry.COLUMN_DATE + " >= ?";

    /*
     * Public Methods
     */
    //Todo: Implement bulkInsert(Uri uri, ContentValues[] values)
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        return 0;
    }

    //ToDo: Implement delete(Uri uri, String selection, String[] selectionArgs)
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        return 0;
    }

    @Override
    public boolean onCreate(){
        weatherDbHelper = new WeatherDbHelper(getContext());

        return true;
    }

    //TODO: Implement getType(Uri uri)
    @Override
    public String getType(Uri uri){
        return null;
    }

    //TODO: Implement insert(Uri uri, ContentValues values)
    @Override
    public Uri insert(Uri uri, ContentValues contentValues){
        return null;
    }

    //TODO: Implement query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOder){
        return null;
    }

    // You don't need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown(){
        weatherDbHelper.close();
        super.shutdown();
    }

    //ToDo: Implement update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    @Override
    public int update(Uri uri,
                      ContentValues contentValues,
                      String selection,
                      String[] selectionArgs){
        return 0;
    }

    /*
     * Private Methods
     */
    /*
     * Students: Here is where you need to create the UriMatcher. This UriMatcher will match each
     * URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE and LOCATION
     * integer constants defined above. You can test this by uncommenting the testUriMatcher test
     * within TestUriMatcher.
     */
    protected static UriMatcher buildUriMatcher(){
        // 1) The code passed into the constructor represents the code to return for the root URI.
        // It's common to use NO_MATCH as the code for this case. Add the constructor below.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 2) Use the addURI function to match each of the types. Use the constants from
        // WeatherContract to help define  the types to the UriMatcher.
        // UriMatcher.addUri(authority, path, code)
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                //WeatherContract.PATH_WEATHER + "/#", //My Version
                WeatherContract.PATH_WEATHER, //Udacity Version - It doesn't match with WeatherContract.WeatherEntry.buildWeatherUri(long id)
                WEATHER);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*",
                WEATHER_WITH_LOCATION);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*/#",
                WEATHER_WITH_LOCATION_AND_DATE);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                //WeatherContract.PATH_LOCATION + "/#", //My Version
                WeatherContract.PATH_LOCATION, //Udacity Version - It doesn't match with WeatherContract.LocationEntry.buildLocationUri(long id)
                LOCATION);

        // 3) Return the new matcher!
        return uriMatcher;
    }

    private Cursor getWeatherByLocationSettings(Uri uri, String[] projection, String sortOrder){
        String locationSettings = WeatherContract.WeatherEntry.getLocationSettingsFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if(startDate == 0){
            selection = locationSettingsSelection;
            selectionArgs = new  String[]{locationSettings};
        }
        else{
            selection = locationSettingsWithStartDateSelection;
            selectionArgs = new String[]{locationSettings, Long.toString(startDate)};
        }

        return weatherByLocationSettingQueryBuilder.query(weatherDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getWeatherByLocationSettingsAndDate(Uri uri,
                                                       String[] projection,
                                                       String sortOrder){
        String locationSettings = WeatherContract.WeatherEntry.getLocationSettingsFromUri(uri);
        long date = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        return weatherByLocationSettingQueryBuilder.query(weatherDbHelper.getReadableDatabase(),
                projection,
                locationSettingsAndDaySelection,
                new String[]{locationSettings, Long.toString(date)},
                null,
                null,
                sortOrder);
    }

    private void normalizeDate(ContentValues contentValues){
        // Normalize the date value
        if(contentValues.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)){
            long dateValue = contentValues.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
            contentValues.put(WeatherContract.WeatherEntry.COLUMN_DATE,
                    WeatherContract.normalizeDate(dateValue));
        }
    }
}
