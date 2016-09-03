package com.lex.sunshine;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.lex.sunshine.db.WeatherContract;
import com.lex.sunshine.db.WeatherDbHelper;

/**
 * Created by Alex on 28/08/2016.
 */
public class TestProvider extends AndroidTestCase {

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
