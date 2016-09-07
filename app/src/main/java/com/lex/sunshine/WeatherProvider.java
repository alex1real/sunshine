package com.lex.sunshine;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        final SQLiteDatabase sqLiteDatabase = weatherDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int numAffectedRows = 0;
        long rowId;

        switch(match){
            case WEATHER:
                sqLiteDatabase.beginTransaction();

                try{
                    for(ContentValues contentValues : values){
                        normalizeDate(contentValues);
                        rowId = sqLiteDatabase.insert(
                                WeatherContract.LocationEntry.TABLE_NAME,
                                null,
                                contentValues);

                        if(rowId != -1){
                            ++numAffectedRows;
                        }
                    }

                    sqLiteDatabase.setTransactionSuccessful();

                }
                finally {
                    sqLiteDatabase.endTransaction();
                }

                break;

            default:
                throw new UnsupportedOperationException();
        }

        return numAffectedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        final SQLiteDatabase sqLiteDatabase = weatherDbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int numAffectedRows;

        switch (match){
            case LOCATION:
                numAffectedRows = sqLiteDatabase.delete(WeatherContract.LocationEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            case WEATHER:
                numAffectedRows = sqLiteDatabase.delete(WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(numAffectedRows > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        sqLiteDatabase.close();

        return numAffectedRows;
    }

    @Override
    public boolean onCreate(){
        weatherDbHelper = new WeatherDbHelper(getContext());

        return true;
    }

    @Override
    public String getType(Uri uri){
        //Use the Uri Matcher to determine what kind of URI this is.
        final int match = uriMatcher.match(uri);

        switch (match){
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues){
        final SQLiteDatabase sqLiteDatabase = weatherDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri = null;

        long _id;

        switch (match){
            case WEATHER:
                normalizeDate(contentValues);
                _id = sqLiteDatabase.insert(WeatherContract.WeatherEntry.TABLE_NAME,
                        null,
                        contentValues);

                if(_id > 0)
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);

                break;

            case LOCATION:
                _id = sqLiteDatabase.insert(WeatherContract.LocationEntry.TABLE_NAME,
                        null,
                        contentValues);
                if(_id > 0)
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);

                break;

            default:
                throw new UnsupportedOperationException("Unkwnon uri: " + uri);
        }

        if(_id <= 0)
            throw new android.database.SQLException("Failed to insert row into " + uri);

        getContext().getContentResolver().notifyChange(uri, null);
        sqLiteDatabase.close();

        return returnUri;
    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder){
        // Here is the switch statement that, given a URI, will determine what kind of request is,
        // and query the database accordingly.
        Cursor retCursor;

        switch(uriMatcher.match(uri)){
            //weather
            case WEATHER:
                retCursor = weatherDbHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case WEATHER_WITH_LOCATION:
                retCursor = getWeatherByLocationSettings(uri, projection, sortOrder);
                break;

            case WEATHER_WITH_LOCATION_AND_DATE:
                retCursor = getWeatherByLocationSettingsAndDate(uri, projection, sortOrder);
                break;

            case LOCATION:
                retCursor = weatherDbHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new UnsupportedOperationException("Uknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
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

    @Override
    public int update(Uri uri,
                      ContentValues contentValues,
                      String selection,
                      String[] selectionArgs){

        final SQLiteDatabase sqLiteDatabase = weatherDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);

        int numAffectedRows;

        switch (match){
            case WEATHER:
                normalizeDate(contentValues);
                numAffectedRows = sqLiteDatabase.update(WeatherContract.WeatherEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;

            case LOCATION:
                numAffectedRows = sqLiteDatabase.update(WeatherContract.LocationEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;

            default:
                numAffectedRows = 0;
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(numAffectedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        sqLiteDatabase.close();

        return numAffectedRows;
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
                WeatherContract.PATH_WEATHER,
                WEATHER);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*",
                WEATHER_WITH_LOCATION);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*/#",
                WEATHER_WITH_LOCATION_AND_DATE);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_LOCATION,
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
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

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
