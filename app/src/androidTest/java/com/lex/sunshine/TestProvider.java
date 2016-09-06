package com.lex.sunshine;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.lex.sunshine.db.WeatherContract;
import com.lex.sunshine.db.WeatherDbHelper;

/**
 * Created by Alex on 28/08/2016.
 */
public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getSimpleName();
/*
    public void testBasicWeatherQuery(){
        // Insert the test record directly into the database
        WeatherDbHelper weatherDbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase sqLiteDatabase = weatherDbHelper.getWritableDatabase();

        ContentValues locationValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = TestUtilities.insertNorthPoleLocationValues(mContext);

        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);

        long weatherRowId = sqLiteDatabase.insert(WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                weatherValues);
        assertTrue("Unable to Insert WeatherEntry into the database", weatherRowId != -1);

        sqLiteDatabase.close();

        // Test the basic content provider Query
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        // Make sure we get the currect cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", weatherCursor, weatherValues);
    }
*/
    public void testGetType(){
        String testLocation = "London, UK";
        long testDate = 1419120000L;// December 21st, 2014

        // content://com.lex.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.CONTENT_URI);

        // vnd.android.cursor.dir/com.lex.sunshine.app/weather
        assertEquals("Error: The WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                WeatherContract.WeatherEntry.CONTENT_TYPE, type);


        // content://com.lex.sunshine.app/weather/London, UK
        type = mContext.getContentResolver()
                .getType(WeatherContract.WeatherEntry.buildWeatherLocation(testLocation));

        // vnd.android.cursor.dir/com.lex.sunshine.app/weather
        assertEquals("Error: The WeatherEntry CONTENT_URI with location should return return " +
                "WeatherEntry.CONTENT_TYPE",
                WeatherContract.WeatherEntry.CONTENT_TYPE, type);


        // content://com.lex.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver()
                .getType(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(testLocation,
                        testDate));

        // vnd.android.cursor.item/com.lex.sunshine.app/weather/1419120000
        assertEquals("Error: The WeatherEntry CONTENT_URI with location and date should return" +
                "WeatherEntry.CONTENT_ITEM_TYPE",
                WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE, type);


        // content://com.lex.sunshine.app/location
        type = mContext.getContentResolver().getType(WeatherContract.LocationEntry.CONTENT_URI);

        //vnd.android.cursor.dir/com.lex.sunshine.app/location
        assertEquals("Error: The LocationEntry CONTENT_URI should return LocationEntry.CONTENT_TYPE",
                WeatherContract.LocationEntry.CONTENT_TYPE, type);
    }

    public void testInsertReadProvider(){
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        //Register a content observer for our insert. This time directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(WeatherContract.LocationEntry.CONTENT_URI, true, tco);
        Uri locationUri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, testValues);

        //Did your content observer get called?
        //If it fails, insert location isn't calling getContext().getResolver().notifyChange(uri, null)
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long locationRowId = ContentUris.parseId(locationUri);

        //Verify if we got a row back
        assertTrue(locationRowId != -1);

        //Data is inserted IN THEORY. Now pull some out to stare at it and verify it made the round
        // trip

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                null, // leave "columns" null just returns all the columns
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validationg Location Entry.",
                cursor,
                testValues);

        // Now that we have a location, lets add some weather.
        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);
        // The TestContentObserver is a one-shot class.
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(WeatherContract.WeatherEntry.CONTENT_URI,
                true,
                tco);

        Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI,weatherValues);
        assertTrue(weatherInsertUri != null);

        // Did the Content Observer get called?
        // It it fails, the insert weather in ContentProvider isn't calling
        // getContext().getContentResolver.notifyChange(uri, null)
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A Cursor is the primary interface to the query results
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.CONTENT_URI, // Table to Query
                null, //leaving "columns" null just returns all the columns
                null, // columns fot the where clause
                null, // Values for the where clause
                null  // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert",
                weatherCursor,
                weatherValues);

        // Add the location values in with the weather data so that we can make sure that the join
        // worked and we actually get all the values back.
        weatherValues.putAll(testValues);

        // Get the joined Weather and Location Data.
        weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocation(TestUtilities.TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns
                null, // columns for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating joined Weather and " +
                "Location Data.",
                weatherCursor,
                weatherValues);

        // Get the joined Weather and Location with a start date
        Log.v(LOG_TAG, "WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate() Uri = " +
                WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        TestUtilities.TEST_LOCATION,
                        TestUtilities.TEST_DATE
                ).toString());
        weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        TestUtilities.TEST_LOCATION,
                        TestUtilities.TEST_DATE
                ),
                null, // leaving "columns" null just returns all the columns
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating joined Weather and " +
                "Location with start date.",
                weatherCursor,
                weatherValues);

        Log.v(LOG_TAG, "WeatherContract.WeatherEntry.buildWeatherLocationWithDate() Uri = "
                + WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                TestUtilities.TEST_LOCATION,
                TestUtilities.TEST_DATE).toString());
        // Get the joined Weather data for a specific date
        weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        TestUtilities.TEST_LOCATION,
                        TestUtilities.TEST_DATE
                ),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider. Error validating joined Weather and Location data for a specific date.",
                weatherCursor,
                weatherValues);
    }

    // This test checks to make sure that the content provider is registered correctly.
    public void testProviderRegistry(){
        PackageManager pm = mContext.getPackageManager();

        // The component name is defined based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                WeatherProvider.class.getName());

        try{
            // Fetch the provider info using the component name from the PackageManager
            // This throw an exception if the Provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract
            assertEquals("Error: WeatherProvider registered with authority: "
                    + providerInfo.authority + " instead of authority: "
                    + WeatherContract.CONTENT_AUTHORITY,
                    providerInfo.authority,
                    WeatherContract.CONTENT_AUTHORITY);

        }
        catch(PackageManager.NameNotFoundException e){
            // Probably the provider is not registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }
}
