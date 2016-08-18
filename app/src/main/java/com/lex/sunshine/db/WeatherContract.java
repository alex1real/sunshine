package com.lex.sunshine.db;

import android.provider.BaseColumns;

/**
 * Defines tables and columns for the weather database
 * Created by Alex on 13/08/2016.
 */
public class WeatherContract {

    //Inner class that defines table contents of the location table
    public static final class LocationEntry implements BaseColumns{

        public static final String TABLE_NAME = "location";

        //The location settings string that will be sent to openWeatherMap
        public static final String COLUMN_LOCATION_SETTINGS = "location_settings";

        //A human readable location String.
        public static final String COLUMN_CITY_NAME = "city_name";

        //In order to uniquely pin point the location map intent,
        //we store longitude and latitude
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

    }

    // Inner class that defines table contents of the weather table
    public static final class WeatherEntry implements BaseColumns {

        public static final String TABLE_NAME = "weather";

        //Column with the fk into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";

        //Weather Id is returned by API, to identify the icon to be used.
        public static final String COLUMN_WEATHER_ID = "wheater_id";

        //Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";

        //Short description and long description of the weather, as privided by API.
        // e. g. "clear" vs "sky is clear"
        public static final String COLUMN_SHORT_DESC = "short_desc";

        //Min and Max tempertures for the day (stored as floats)
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        //Humidity is stored as a float representing percentage
        public static final String COLUMN_HUMIDITY = "humidity";

        //Pressure is stored as a float
        public static final String COLUMN_PRESSURE = "pressure";

        //Wind is stored as a float representing wind speed in mph
        public static final String COLUMN_WIND_SPEED = "wind";

        //Degrees are meteorological degrees (e.g, 0 is north, 180 is south). Stored as floats.
        public static final String COLUMN_DEGREES = "degrees";

    }

}
