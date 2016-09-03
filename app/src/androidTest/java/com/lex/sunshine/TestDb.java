package com.lex.sunshine;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.lex.sunshine.db.WeatherContract;
import com.lex.sunshine.db.WeatherDbHelper;

import java.util.HashSet;

/**
 * Created by Alex on 14/08/2016.
 */
public class TestDb extends AndroidTestCase {

    private final String LOG_TAG = TestDb.class.getSimpleName();


    /*
     * Public Methods
     */
    public void setUp(){
        this.deleteDatabase();
    }

    public void testCreateDb(){
        //Build a HashSet of all of table names we wish to look for
        //Note that there will be another table in the DB that stores the Android metadata
        //(db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        SQLiteDatabase sqLiteDatabase = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, sqLiteDatabase.isOpen());

        //Have we created the tables we want?
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database was not been created correctly", cursor.moveToFirst());

        //Verify that the tables have been created
        do {
            tableNameHashSet.remove(cursor.getString(0));
        } while(cursor.moveToNext());

        // If it fails, it means that your database doesn't contain both the location entry and
        // weather entry tables
        assertTrue("Error: Your database was created without both the location entry " +
                "and weather entry tables", tableNameHashSet.isEmpty());

        //Now, do our tables contain the correct columns?
        cursor = sqLiteDatabase.rawQuery("PRAGMA table_info("
                + WeatherContract.LocationEntry.TABLE_NAME + ")", null);

        assertTrue("Error: This means that we were unable to query the database for " +
                "table information", cursor.moveToFirst());

        //Build a HashSet of all of column names that we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTINGS);

        int columnNameIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(cursor.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required entry columns",
                locationColumnHashSet.isEmpty());

        sqLiteDatabase.close();
    }
/*
    public void testInsertLocation(){
        SQLiteDatabase sqLiteDatabase = new WeatherDbHelper(mContext).getWritableDatabase();
        assertTrue("Error: Database was not open", sqLiteDatabase.isOpen());

        long rowId = TestUtilities.insertNorthPoleLocationValues(mContext);
        assertTrue("Error: The row was not inserted properly in the Location Table", rowId != -1);

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + WeatherContract.LocationEntry.TABLE_NAME, null);
        TestUtilities.validateCursor("TestDb", cursor, TestUtilities.createNorthPoleLocationValues());

        cursor.close();
        sqLiteDatabase.close();
    }

    public void testInsertWeather(){
        SQLiteDatabase sqLiteDatabase = new WeatherDbHelper(mContext).getWritableDatabase();
        assertTrue("Error: Database was not open", sqLiteDatabase.isOpen());

        long locationId = TestUtilities.insertBelfastLocationValues(mContext);

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + WeatherContract.LocationEntry.TABLE_NAME, null);
        TestUtilities.validateCursor("TesteDb", cursor, TestUtilities.createBelfastLocationValues());

        cursor.close();

        long weatherId = TestUtilities.insertWeatherValues(mContext, locationId);

        cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + WeatherContract.WeatherEntry.TABLE_NAME, null);
        TestUtilities.validateCursor("TesteDb", cursor, TestUtilities.createWeatherValues(locationId));

        cursor.close();
        sqLiteDatabase.close();
    }
*/
    /*
     * Private Methods
     */
    private void deleteDatabase(){
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

}
