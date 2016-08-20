package com.lex.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.lex.sunshine.db.WeatherContract;
import com.lex.sunshine.db.WeatherDbHelper;

import junit.framework.Test;

import java.util.Map;
import java.util.Set;

/**
 * Created by Alex on 18/08/2016.
 */
public class TestUtilities extends AndroidTestCase {

    private static final String TEST_LOCATION = "North Pole,us";
    private static final long TEST_DATE = 1419033600L; //December 20th, 2014

    /*
     * Public Methods
     */
    public static ContentValues createBelfastLocationValues(){
        ContentValues belfastLocationValues = new ContentValues();

        belfastLocationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS, "Belfast,gb");
        belfastLocationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "Belfast");
        belfastLocationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 54.5833);
        belfastLocationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, -5.9333);

        return belfastLocationValues;
    }

    public static ContentValues createNorthPoleLocationValues(){
        ContentValues northPoleContentValues = new ContentValues();

        northPoleContentValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS, TEST_LOCATION);
        northPoleContentValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "North Pole");
        northPoleContentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 64.7488);
        northPoleContentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, -147.353);

        return northPoleContentValues;
    }

    /*
     * This is to create some default weather values for database tests.
     */
    public static ContentValues createWeatherValues(long locationRowId){
        ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, TEST_DATE);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    public static long insertBelfastLocationValues(Context context){
        WeatherDbHelper weatherDbHelper = new WeatherDbHelper(context);
        SQLiteDatabase sqLiteDatabase = weatherDbHelper.getWritableDatabase();

        ContentValues belfastValues = TestUtilities.createBelfastLocationValues();

        long locationRowId = sqLiteDatabase.insert(WeatherContract.LocationEntry.TABLE_NAME,
                                                    null,
                                                    belfastValues);

        assertTrue("Error: Failure to insert Belfast Location Values", locationRowId != -1);

        return locationRowId;
    }

    public static long insertNorthPoleLocationValues(Context context){
        // Insert our test records into the database
        WeatherDbHelper weatherDbHelper = new WeatherDbHelper(context);
        SQLiteDatabase sqLiteDatabase = weatherDbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        long locationRowId = sqLiteDatabase.insert(WeatherContract.LocationEntry.TABLE_NAME,
                                                    null,
                                                    testValues);

        // Verify if we got a row back
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }

    public static long insertWeatherValues(Context context, long locationId){
        WeatherDbHelper weatherDbHelper = new WeatherDbHelper(context);
        SQLiteDatabase sqLiteDatabase = weatherDbHelper.getWritableDatabase();

        ContentValues weatherValues = TestUtilities.createWeatherValues(locationId);

        long weatherId = sqLiteDatabase.insert(WeatherContract.WeatherEntry.TABLE_NAME,
                                                null,
                                                weatherValues);

        assertTrue("Error: Failure to insert Weather Values", weatherId != -1);

        return weatherId;
    }

    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues){
        assertTrue("Emtpy cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }


    /*
     * Private Methods
     */
    private static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int index = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found" + error, index == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString()
                            + "' didn't match the expected value '" + expectedValue + "'." + error,
                    expectedValue, valueCursor.getString(index));
        }

    }

}
