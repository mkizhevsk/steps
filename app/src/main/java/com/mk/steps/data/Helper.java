package com.mk.steps.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.mk.steps.data.entity.Training;
import com.mk.steps.data.service.RetrofitService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Helper {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static boolean checkPermissions(Context context, Activity mainActivity) {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(context, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mainActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    public static String getStringFromDouble(double value) {
        return String.valueOf(upToOneDecimalPlace(value));
    }

    public static double upToOneDecimalPlace(double value) {
        long intValue = Math.round(value * 10);
        return ((double) intValue) / 10;
    }

    public static int getDuration(Date startDateTime) {
        return (int) getDateDiff(startDateTime, new Date(System.currentTimeMillis()), TimeUnit.MINUTES);
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public static String getStringDate(Date date) {

        return sdf.format(date);
    }

    public static Date getDateFromString(String date) throws ParseException {
        return sdf.parse(date);
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
        return accuracy + " м";
    }

    public static RetrofitService getRetrofitApiWithUrl(String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(RetrofitService.class);
    }
}
