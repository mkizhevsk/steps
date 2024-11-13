package com.mk.steps.data.provider;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.mk.steps.ui.MainActivity;
import com.mk.steps.data.util.Helper;
import com.mk.steps.data.entity.Training;
import com.mk.steps.data.service.RetrofitService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TinyFitnessProvider {

    private static TinyFitnessProvider ourInstance = new TinyFitnessProvider();
    public static TinyFitnessProvider getInstance() {
        return ourInstance;
    }

    private final String HTTPS_TINY_FITNESS_URL = "https://tiny-fitness.ru/api/";
    private final String HTTP_TINY_FITNESS_URL = "http://tiny-fitness.ru/api/";

    final String TAG = "myLogs";

    public void saveTraining(Training training) {
        Log.d(TAG, "saveTraining " + Build.VERSION.SDK_INT);

        RetrofitService api;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            api = Helper.getRetrofitApiWithUrlAndAuth(HTTPS_TINY_FITNESS_URL);
        } else {
            api = Helper.getRetrofitApiWithUrlAndAuth(HTTP_TINY_FITNESS_URL);
        }

        String date = Helper.getStringDateTime(training.getDateTime());
        Log.d(TAG, date);
        api.saveTraining(training.getInternalCode(), date, training.getDistance(), training.getDuration(), training.getType())
                .enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse " + response.isSuccessful());
                MainActivity.tinyFitnessHandler.sendMessage(getTinyFitnessMessage(response.isSuccessful()));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure");
                MainActivity.tinyFitnessHandler.sendMessage(getTinyFitnessMessage(false));
            }
        });
    }

    private Message getTinyFitnessMessage(boolean result) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("result", result);

        Message message = new Message();
        message.setData(bundle);

        return message;
    }
}
