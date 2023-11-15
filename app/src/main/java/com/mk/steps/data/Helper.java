package com.mk.steps.data;

import android.text.Editable;

import com.mk.steps.data.entity.Training;
import com.mk.steps.data.service.RetrofitService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Helper {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static String getStringFromDouble(double value) {
        return String.valueOf(upToOneDecimalPlace(value));
    }

    public static double getDoubleFromInput(Editable inputValue) {
        double doubleValue = Double.parseDouble(inputValue.toString());
        return upToOneDecimalPlace(doubleValue);
    }

    public static double upToOneDecimalPlace(double value) {
        long longValue = Math.round(value * 10);
        return ((double) longValue) / 10;
    }

    public static int getDuration(Date startDateTime) {
        return (int) getDateDiff(startDateTime, new Date(System.currentTimeMillis()), TimeUnit.MINUTES);
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public static String getStringDate(Date date) {
        return dateFormat.format(date);
    }

    public static String getStringDateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    public static Date getDateFromString(String date) throws ParseException {
        return dateFormat.parse(date);
    }

    public static String getTrainingHistory(List<Training> trainings) {
        String deletedTracksInfo = "";
        for(Training training : trainings) {
            StringBuilder sb = new StringBuilder();
            if(deletedTracksInfo.isEmpty()) {
                deletedTracksInfo = training.toString();
            } else {
                deletedTracksInfo = sb.append(deletedTracksInfo).append(getNewLine()).append(getNewLine()).append(training.toString()).toString();
            }
        }
        return deletedTracksInfo;
    }

    private static String getNewLine() {
        return System.getProperty("line.separator");
    }

    public static String getStringTemperature(double temperature) {
        return upToOneDecimalPlace(temperature) + " C";
    }

    public static String getStringDuration(int duration) {
        return duration + " мин.";
    }

    public static String getStringDistance(double distance) {
        return upToOneDecimalPlace(distance) + " км";
    }

    public static String getStringAccuracy(float accuracy) {
        return Math.round(accuracy) + " м";
    }

    public static float getSpeedInKmHour(float speedInMeterSecond) {
        return (speedInMeterSecond * 3600) / 1000;
    }

    public static RetrofitService getRetrofitApiWithUrl(String url) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(RetrofitService.class);
    }

    public static RetrofitService getRetrofitApiWithUrlAndAuth(String url) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator((route, response) -> {
                    Request request = response.request();
                    if (request.header("Authorization") != null)
                        // Логин и пароль неверны
                        return null;
                    return request.newBuilder()
                            .header("Authorization", Credentials.basic("admin", "123"))
                            .build();
                })
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(RetrofitService.class);
    }
}
