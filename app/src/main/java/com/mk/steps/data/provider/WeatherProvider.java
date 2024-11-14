package com.mk.steps.data.provider;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mk.steps.ui.MainActivity;
import com.mk.steps.data.util.Helper;
import com.mk.steps.data.dto.weather.Weather;
import com.mk.steps.data.service.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherProvider {

    private static WeatherProvider ourInstance = new WeatherProvider();
    public static WeatherProvider getInstance() {
        return ourInstance;
    }

    private final String OPEN_WEATHER_API_URL = "https://api.openweathermap.org";
    private final String OPEN_WEATHER_APP_ID = "6e71959cff1c0c71a6049226d45c69a1";
    private final String OPEN_WEATHER_UNITS = "metric";
    private final String OPEN_WEATHER_CITY = "izhevsk";

    final String TAG = "myLogs";

    public WeatherProvider() {
    }

    public void getTemperature() {
        Log.d(TAG, "temperature start");

        RetrofitService api = Helper.getRetrofitApiWithUrl(OPEN_WEATHER_API_URL);

        api.loadPojoCityWeather(OPEN_WEATHER_APP_ID, OPEN_WEATHER_UNITS, OPEN_WEATHER_CITY)
                .enqueue(new Callback<Weather>() {

            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                Weather weather = response.body();
                double temperature = weather.getMain().getTemp();

                MainActivity.weatherHandler.sendMessage(getWeatherHandlerMessage(temperature));

                Log.d(TAG, " temperature " + weather.getVisibility() + " " + temperature);
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Log.e(TAG, t.toString());
            }
        });
    }

    public void checkNetworkAndFetchWeather(Context context) {
        Handler handler = new Handler();
        Runnable fetchWeatherRunnable = new Runnable() {
            @Override
            public void run() {
                if (isNetworkAvailable(context)) {
                    getTemperature();
                } else {
                    handler.postDelayed(this, 5000); // Retry every 5 seconds
                }
            }
        };
        handler.post(fetchWeatherRunnable); // Start the check immediately
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Message getWeatherHandlerMessage(double temperature) {
        Bundle bundle = new Bundle();
        bundle.putDouble("temperature", temperature);

        Message message = new Message();
        message.setData(bundle);

        return message;
    }
}
