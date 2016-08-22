package com.lex.sunshine;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.lex.sunshine.db.WeatherContract;

/**
 * Created by Alex on 20/08/2016.
 */
public class TestWeatherContract extends AndroidTestCase {

    // Intentionally includes a slash to make Uri is getting quoted correctly
    private static final String TEST_WEATHER_LOCATION = "/North Pole";
    private static final long TEST_WEATHER_DATE = 1419033600L;

    public void testBuildWeatherLocation(){
        Uri locationUri = WeatherContract.WeatherEntry.buildWeatherLocation(TEST_WEATHER_LOCATION);

        assertNotNull("Error: Null Uri returned. You must fill-in buildWeatherLocation in " +
                "WeatherContract", locationUri);
        assertEquals("Error: Weather location not properly appended to the end of the Uri",
                TEST_WEATHER_LOCATION, locationUri.getLastPathSegment());
        assertEquals("Error: Weather location Uri doesn't match our expected result",
                locationUri.toString(), "content://com.lex.sunshine.app/weather/%2FNorth%20Pole");
    }

}
