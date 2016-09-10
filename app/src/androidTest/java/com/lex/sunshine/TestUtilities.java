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

    public static final String TEST_LOCATION = "North Pole,us";
    public static final long TEST_DATE = 1419033600L; //December 20th, 2014

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

    public static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int index = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found" + error, index == -1);

            String expectedValue = entry.getValue().toString();
            String cursorValue = valueCursor.getString(index);
            assertEquals("Value '" + entry.getValue().toString()
                            + "' didn't match the expected value '" + expectedValue + "'." + error,
                    expectedValue, cursorValue);
        }

    }

    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues){
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }


    /*
     * Private Methods
     */
    /*
     The functions provided inside of TestProvider use this utility class to test the ContentObserver
     callbacks using the Polling Check class the we grabbed from the Android CTS tests.

     Note  that this only tests that onChange function is called; it doesn't test that the correct
     Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread ht;
        boolean contentChangedFlag;

        private TestContentObserver(HandlerThread ht){
            super(new Handler(ht.getLooper()));

            this.ht = ht;
        }

        static TestContentObserver getTestContentObserver(){
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        //On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange){
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri){
            contentChangedFlag = true;
        }

        public void waitForNotificationOrFail(){
            // Note: The Polling Check class is taken from the Android CTS (Compability Test Suit).
            // It's usefull to look at Android CTS source for ideas on how to test your Android
            // applications. The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000){
                @Override
                protected boolean check(){
                    return contentChangedFlag;
                }
            }.run();
            ht.quit();
        }
    }

    static TestContentObserver getTestContentObserver(){
        return TestContentObserver.getTestContentObserver();
    }
}
