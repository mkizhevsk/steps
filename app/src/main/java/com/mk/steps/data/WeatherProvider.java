package com.mk.steps.data;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.mk.steps.MainActivity;
import com.mk.steps.data.dto.weather.Weather;
import com.mk.steps.data.service.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherProvider {

    private static WeatherProvider ourInstance = new WeatherProvider();

    public static WeatherProvider getInstance() {
        return ourInstance;
    }

    private String openWeatherAppId = "6e71959cff1c0c71a6049226d45c69a1";
    private String openWeatherUnits = "metric";

    final String TAG = "myLogs";

    public WeatherProvider() {
    }

    public void getTemperature() {
        Log.d(TAG, "temperature start");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService api = retrofit.create(RetrofitService.class);

        api.loadPojoCityWeather(openWeatherAppId, openWeatherUnits, "izhevsk").enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                Weather weather = response.body();
                double temperature = weather.getMain().getTemp();

                MainActivity.weatherHandler.sendMessage(getWeatherHandlerMessage(temperature));

                Log.d(TAG, " temperature " + weather.getVisibility() + " " + temperature);
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {

            }
        });
    }

    private Message getWeatherHandlerMessage(double temperature) {
        Bundle bundle = new Bundle();
        bundle.putDouble("temperature", temperature);

        Message message = new Message();
        message.setData(bundle);

        return message;
    }
}
