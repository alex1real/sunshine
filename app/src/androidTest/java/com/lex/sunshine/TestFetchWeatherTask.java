package com.lex.sunshine;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.lex.sunshine.db.WeatherContract;

/**
 * Created by Alex on 10/09/2016.
 */
public class TestFetchWeatherTask extends AndroidTestCase {

    static final String ADD_LOCATION_SETTING = "Sunnydale, CA";
    static final String ADD_LOCATION_CITY = "Sunnydale";
    static double ADD_LOCATION_LAT = 34.425833;
    static double ADD_LOCATION_LON = -119.714167;

    @TargetApi(11)
    public void testAddLocaiton(){
        //start from a clean state
        getContext().getContentResolver().delete(
                WeatherContract.LocationEntry.CONTENT_URI,
                WeatherContract.LocationEntry.TABLE_NAME + "."
                        + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS + " = ?",
                new String[]{ADD_LOCATION_SETTING}
        );

        FetchWeatherTask fwt = new FetchWeatherTask(getContext(), null);
        long locationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                ADD_LOCATION_LAT, ADD_LOCATION_LON);

        // Does addLocaiton return a valid record ID?
        assertFalse("Error: addLocation() returned an invalid ID on insert", locationId == -1);

        // Test all this twice
        for(int i = 0; i < 2; i++){

            // Does the ID points to our locaiton?
            Cursor locationCursor = getContext().getContentResolver().query(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    new String[]{
                            WeatherContract.LocationEntry._ID,
                            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS,
                            WeatherContract.LocationEntry.COLUMN_CITY_NAME,
                            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                            WeatherContract.LocationEntry.COLUMN_COORD_LONG
                    },
                    WeatherContract.LocationEntry.TABLE_NAME + "."
                    + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS + " = ?",
                    new String[]{ADD_LOCATION_SETTING},
                    null
            );

            // These match the indices of the projection
            if(locationCursor.moveToFirst()){
                assertEquals("Error: the required value of locationId does not match the returned " +
                        "value from addLocation", locationCursor.getLong(0), locationId);
                assertEquals("Error: the queried value of location setting is incorrect",
                        locationCursor.getString(1), ADD_LOCATION_SETTING);
                assertEquals("Error: the queried value of location city is incorrect",
                        locationCursor.getString(2), ADD_LOCATION_CITY);
                assertEquals("Error: the queried value of latitude is incorrect",
                        locationCursor.getDouble(3), ADD_LOCATION_LAT);
                assertEquals("Error: the queried value of longiture is incorrect",
                        locationCursor.getDouble(4), ADD_LOCATION_LON);
            }
            else{
                fail("Error: the id you used to query returned an empty cursor");
            }

            // There should be no more records
            assertFalse("Error: there should be only one record returned form a location query",
                    locationCursor.moveToNext());

            // Add the location again
            long newLocationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                    ADD_LOCATION_LAT, ADD_LOCATION_LON);

            assertEquals("Error: inserting a location again should return the same ID",
                    locationId, newLocationId);

        }

        //Reset our state back to normal
        getContext().getContentResolver().delete(
                WeatherContract.LocationEntry.CONTENT_URI,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS + " = ?",
                new String[]{ADD_LOCATION_SETTING}
        );

        // Clean up the test so that other tests can use the content provider
        getContext().getContentResolver()
                .acquireContentProviderClient(WeatherContract.LocationEntry.CONTENT_URI)
                .getLocalContentProvider().shutdown();

    }
}
