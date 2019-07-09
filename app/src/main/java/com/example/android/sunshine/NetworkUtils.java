/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.data.WeatherDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {


    /*
     * NOTE: These values only effect responses from OpenWeatherMap, NOT from the fake weather
     * server. They are simply here to allow us to teach you how to build a URL if you were to use
     * a real API.If you want to connect your app to OpenWeatherMap's API, feel free to! However,
     * we are not going to show you how to do so in this course.
     */

    final static String QUERY_PARAM = "q";
    final static String ID_param = "appid";
    final static String LATITUDE_PARAM = "lat";
    final static String LONGITUDE_PARAM = "lon";

    private static WeatherDbHelper mDbHelper;

    private static Context context;

    /**
     * Builds the URL used to talk to the weather server using a location. This location is based
     * on the query capabilities of the weather provider that we are using.
     *
     * @return The URL to use to query the weather server.
     */
    public static URL buildUrl(String requestUrl) {

        Uri builtUri = Uri.parse(requestUrl).buildUpon()
                .appendQueryParameter(QUERY_PARAM, "delhi")
                .appendQueryParameter(ID_param, "fc8910fbab2a48ad9615dc1aa340356c").build();
//                .buildUpon()
//                .appendQueryParameter(QUERY_PARAM,"london")
//                .appendQueryParameter(FORMAT_PARAM,format)
//                .appendQueryParameter(UNITS_PARAM,units)
//                .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays)).build();
        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v("NavigationUtils", "Built Uri: " + url);

        return url;
    }

    /**
     * Builds the URL used to talk to the weather server using latitude and longitude of a
     * location.
     *
     * @param lat The latitude of the location
     * @param lon The longitude of the location
     * @return The Url to use to query the weather server.
     */
    public static URL buildUrl(String requestUrl, Double lat, Double lon) {
        Uri builtUri = Uri.parse(requestUrl).buildUpon()
                .appendQueryParameter(LATITUDE_PARAM, String.valueOf(lat))
                .appendQueryParameter(LONGITUDE_PARAM, String.valueOf(lon))
                .appendQueryParameter(ID_param, "fc8910fbab2a48ad9615dc1aa340356c").build();


        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v("NavigationUtils", "Built Uri: " + url);
        return url;

    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    private static String getResponseFromHttpUrl(URL url) throws IOException {

        String JsonResponse = "";
        if (url == null) {
            return JsonResponse;
        }
        HttpsURLConnection urlConnection = null;
        InputStream in = null;

        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            Log.v("NetworkUtils", "Nothing");
            in = urlConnection.getInputStream();
            JsonResponse = readInputStream(in);
        } catch (IOException e) {
            Log.e("MainActivity", "Problem retrieving the JSON results", e);
        } finally {
            urlConnection.disconnect();
        }
        return JsonResponse;
    }

//            Scanner scanner = new Scanner(in);
//            scanner.useDelimiter("\\A");
//
//            boolean hasInput = scanner.hasNext();
//            if (hasInput) {
//                return scanner.next();
//            } else {
//                return null;
//            }
//               finally {
//            if(urlConnection!=null)
//            urlConnection.disconnect();
//        }
//        return JsonResponse;


    private static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }


    public static void displayWeatherData(String jsonResponse) {


        try {

            String previousDate="";
            JSONObject reader = new JSONObject(jsonResponse);
            JSONArray arrayOfLists = reader.getJSONArray("list");
            Log.v("NetworkUtils", "Siz of list is " + arrayOfLists.length());
            ContentValues[] valuesArray = new ContentValues[arrayOfLists.length()];
            for (int i = 0; i < arrayOfLists.length(); i++) {
                JSONObject currentWeather = arrayOfLists.getJSONObject(i);
                JSONObject currentTemp = currentWeather.getJSONObject("main");
                double temperature = currentTemp.getDouble("temp");
                String temperatureInString = getTemperature(temperature);
                ;
                JSONArray weatherArray = currentWeather.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);
                String desciption = weather.getString("description");
                long timeStamp = currentWeather.getLong("dt");
//                String date_txt=currentWeather.getString("dt_txt");
//                String[] str=date_txt.split(" ");
//                Log.e("NetworkUtils","value of previous date is "+str[0]);
//                if(!previousDate.equals(str[0])) {
//                    Log.e("NetworkUtils", "anything is good");
//
//                    previousDate = str[0];
                    JSONObject mainTemp = currentWeather.getJSONObject("main");
                    String minTemp = mainTemp.getString("temp_min");
                    String maxTemp = mainTemp.getString("temp_max");
                    String humidity = mainTemp.getString("humidity");
                    String pressure = mainTemp.getString("pressure");
                    JSONObject Wind = currentWeather.getJSONObject("wind");
                    String windSpeed = Wind.getString("speed");
                    String directionInDegree = Wind.getString("deg");


                    ContentValues values = new ContentValues();
                    values.put(WeatherEntry.COLOUMN_DESCRIPTION, desciption);
                    values.put(WeatherEntry.COLOUMN_TEMP, temperatureInString);
                    values.put(WeatherEntry.COLUMN_DATE, timeStamp);
                    values.put(WeatherEntry.COLUMN_DEGREES, directionInDegree);
                    values.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                    values.put(WeatherEntry.COLUMN_MAX_TEMP, maxTemp);
                    values.put(WeatherEntry.COLUMN_MIN_TEMP, minTemp);
                    values.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                    values.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);

                    valuesArray[i] = values;
                }
        //    }


            if (valuesArray != null && valuesArray.length != 0) {

                context.getContentResolver().delete(WeatherEntry.CONTENT_URI, null, null);

                Log.v("NetworkUtils", "value of content uri is " + WeatherEntry.CONTENT_URI.toString());
                context.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, valuesArray);
            }
        } catch (JSONException e) {
            Log.e("NetworkUtils", "Problem parsing the JSON results", e);
        }


    }




    private static String getTemperature(double temperature) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        temperature = Double.valueOf(decimalFormat.format(temperature));

        if (MainActivity.temperatureType.equals("kelvin")) {
            temperature = temperature;
            return String.valueOf(temperature) + "K";
        } else if (MainActivity.temperatureType.equals("celcius")) {
            temperature = temperature - 273;
            temperature = Double.valueOf(decimalFormat.format(temperature));
            return String.valueOf(temperature) + "°C";
        }

        return String.valueOf(-1);
    }

    public static void fetchWeatherData(String requestUrl, double lat, double lon, Context context) {

        NetworkUtils.context = context;

        URL url = buildUrl(requestUrl, lat, lon);


        Log.v("NetworkUtils", "URL is " + url);

        String jsonResponse = null;
        try {
            jsonResponse = getResponseFromHttpUrl(url);
            Log.v("NetworkUtils", "value of JsonResponse " + jsonResponse);
        } catch (IOException e) {
            Log.e("MainActivity", "Problem making http request", e);
        }
        displayWeatherData(jsonResponse);

    }



}