package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherDbHelper;
import com.example.android.sunshine.databinding.ActivityCurrentWeatherBinding;
import com.example.android.sunshine.databinding.ListItemForecastTodayBinding;

import java.util.Date;

public class CurrentWeatherActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private TextView mTextView;

    private String weather;

    private WeatherDbHelper mDbHelper;


    private Uri mUri;

    private static ActivityCurrentWeatherBinding currentWeatherBinding;

    private static String weatherSummary="";

    private final int DETAIL_LOADER_ID = 11;


    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLOUMN_DESCRIPTION,
            WeatherEntry.COLOUMN_TEMP

    };

    // public static final String[] WEATHER_DETAIL_PROJECTION = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);
        Log.v("CurrentActivity", "This is the current activity");


        currentWeatherBinding = DataBindingUtil.setContentView(this, R.layout.activity_current_weather);


        Intent intent = getIntent();
        mUri = intent.getData();
        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, CurrentWeatherActivity.this);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void shareText(String weather) {

        String mimeType = "text/plain";
        String title = weatherSummary;

        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType(mimeType)
                .setText(weatherSummary).getIntent();
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_current_weather_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.share_icon:
                shareText(weather);
                return true;

            case R.id.settings_current_weather:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        return new CursorLoader(this, mUri, WEATHER_DETAIL_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            return;
        }

        long date = data.getLong(data.getColumnIndex(WeatherEntry.COLUMN_DATE));
        Date actualDate = new Date(date * 1000);
        String date_to_display = ForecastAdapter.getDate(date);


        String pressure = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_PRESSURE));
        String humidity = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
        String tempMax = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
        String tempMin = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
        String windSpeed = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED));
        String windDirection = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DEGREES));
        String description = data.getString(data.getColumnIndex(WeatherEntry.COLOUMN_DESCRIPTION));
        String temperature = data.getString(data.getColumnIndex(WeatherEntry.COLOUMN_TEMP));
        String time = ForecastAdapter.getTime(date);
//
//
//        mWindDegreesView.setText(windDirection);
//        mWindSpeedView.setText(windSpeed);
//        mDateView.setText(actualDate.toString()+","+relativeDate);
//        mHighTemperatureView.setText(tempMax);
//        mLowTemperatureView.setText(tempMin);
//        mHumidityView.setText(humidity);
//        mPressureView.setText(pressure);


        currentWeatherBinding.humidityValue.setText(humidity);
        currentWeatherBinding.pressureValue.setText(pressure);
        currentWeatherBinding.tempMaxValue.setText(tempMax);
        currentWeatherBinding.tempMinValue.setText(tempMin);
        currentWeatherBinding.windDirectionValue.setText(windDirection);
        currentWeatherBinding.windSpeedValue.setText(windSpeed);




        currentWeatherBinding.include.dayToday.setText(date_to_display);
        currentWeatherBinding.include.description.setText(description);
        currentWeatherBinding.include.temperature.setText(temperature);
        currentWeatherBinding.include.time.setText(time);


        setWeatherImage(description);


        weatherSummary="Date- "+date_to_display+"\nTemp- "+temperature+"\nDescription- "+description+"\nTime- "+time;


    }


    private static void setWeatherImage(String description) {
        if (description.equals("light rain"))
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.ic_light_rain);
        else if (description.equals("few clouds")) {
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.ic_light_clouds);
        } else if (description.equals("scattered clouds")) {
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.ic_cloudy);
        } else if (description.equals("broken clouds")) {
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.ic_cloudy);
        } else if (description.equals("overcast clouds")) {
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.ic_storm);
        } else if (description.equals("moderate rain")) {
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.ic_light_rain);
        } else if (description.equals("clear sky")) {
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.ic_clear);
        }else if(description.equals("heavy intensity rain")){
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.ic_rain);
        }
        else {
            currentWeatherBinding.include.imageView.setImageResource(R.drawable.map_marker);
        }
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
