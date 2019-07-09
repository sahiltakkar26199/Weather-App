package com.example.android.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;

public class WeatherDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";

    private static final int DATABASE_VERSION = 1;

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //  COMPLETED (15) Override onCreate and create the weather table from within it

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE "+ WeatherEntry.TABLE_NAME+"("
                +WeatherEntry.COLUMN_WEATHER_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                WeatherEntry.COLUMN_DATE       + " INTEGER, "                 +

                WeatherEntry.COLUMN_MIN_TEMP   + " REAL, "                    +
                WeatherEntry.COLUMN_MAX_TEMP   + " REAL, "                    +

                WeatherEntry.COLUMN_HUMIDITY   + " REAL, "                    +
                WeatherEntry.COLUMN_PRESSURE   + " REAL, "                    +
                WeatherEntry.COLOUMN_TEMP      + " TEXT, "                    +
                WeatherEntry.COLOUMN_DESCRIPTION + " TEXT, "                  +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL, "                    +
                WeatherEntry.COLUMN_DEGREES    + " REAL" + ");";


            sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

//  COMPLETED (16) Override onUpgrade, but don't do anything within it yet
/**
 * This database is only a cache for online data, so its upgrade policy is simply to discard
 * the data and call through to onCreate to recreate the table. Note that this only fires if
 * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
 * depend on the version number for your application found in your app/build.gradle file. If
 * you want to update the schema without wiping data, commenting out the current body of this
 * method should be your top priority before modifying this method.
 *
 * @param sqLiteDatabase Database that is being upgraded
 * @param oldVersion     The old database version
 * @param newVersion     The new database version
*/
 @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

}