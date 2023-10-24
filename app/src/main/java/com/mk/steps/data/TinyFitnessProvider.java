package com.mk.steps.data;

import android.util.Log;

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

    private final String TINY_FITNESS_URL = "https://tiny-fitness.ru/api/";

    final String TAG = "myLogs";

    public void saveTraining(Training training) {
        Log.d(TAG, "saveTraining");

        RetrofitService api = Helper.getRetrofitApiWithUrl(TINY_FITNESS_URL);

        String date = Helper.getStringDateTime(training.getDateTime());
        Log.d(TAG, date);
        api.saveTraining(training.getInternalCode(), date, training.getDistance(), training.getDuration(), training.getType())
                .enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure");
            }
        });
    }
}
