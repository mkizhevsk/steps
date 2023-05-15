package com.mk.steps;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.steps.data.Helper;
import com.mk.steps.data.TinyFitnessProvider;
import com.mk.steps.data.WeatherProvider;
import com.mk.steps.data.entity.Training;
import com.mk.steps.data.service.BaseService;
import com.mk.steps.data.service.LocationService;
import com.mk.steps.data.thread.DurationRunnable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    public static double distanceInMeters;

    public static Location currentLocation;
    public static List<Location> locationList;
    public static Training training;

    public static boolean start;
    private boolean finish;

    private TextView durationTextView;
    private TextView distanceTextView;
    private Button startFinishButton;
    private TextView temperatureTextView;
    private TextView accuracyTextView;

    private double temperature;

    private Date startDateTime;

    private final int MINIMUM_DURATION = 1;
    private final int MINIMUM_DISTANCE = 500;

    public static Handler showDataHandler;
    public static Handler durationHandler;
    public static Handler weatherHandler;

    private BaseService baseService;
    private LocationService locationService;

    final String TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        durationTextView = findViewById(R.id.durationTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        startFinishButton = findViewById(R.id.startFinishButton);
        accuracyTextView = findViewById(R.id.accuracyTextView);

        Log.d(TAG, "onCreate " + Build.VERSION.SDK_INT);

        distanceInMeters = 0;
        start = false;
        finish = false;

        if (Build.VERSION.SDK_INT >= 23) {
            if (Helper.checkPermissions(this, this)) {
                Log.d(TAG, "permission granted by default");
                startApp();
            } else {
                // todo quit
            }
        } else {
            startApp();
        }
    }

    private void startApp() {
        training = new Training(new Date(System.currentTimeMillis()), 0, 0, 1);
        locationList = new ArrayList<>();

        showDataHandler = getShowDataHandler();
        durationHandler = getDurationHandler();
        weatherHandler = getWeatherHandler();

        startBaseService();
        startLocationService();
        WeatherProvider.getInstance().getTemperature();
    }

    // handlers
    private Handler getShowDataHandler() {
        return new Handler(message -> {
            Log.d(TAG, "showDataHandler");
            showLocationData();

            return true;
        });
    }

    private Handler getDurationHandler() {
        return new Handler(message -> {
            if (startDateTime != null && start && !finish)
                training.setDuration(Helper.getDuration(startDateTime));

            showLocationData();

            //Log.d(TAG, "durationHandler " + training.getDuration());
            return true;
        });
    }

    private Handler getWeatherHandler() {
        return new Handler(message -> {
            Bundle bundle = message.getData();
            temperature = bundle.getDouble("temperature");

            temperatureTextView.setText(Helper.getStringTemperature(temperature));

            Log.d(TAG, "weatherHandler " + temperature);
            return true;
        });
    }

    private void showLocationData() {
        durationTextView.setText(Helper.getStringDuration(training.getDuration()));
        distanceTextView.setText(Helper.getStringDistance(training.getDistance()));
        if(currentLocation != null) accuracyTextView.setText(Helper.getStringAccuracy(currentLocation.getAccuracy()));
    }

    public void onClick(View view) {
        if(!start && !finish) {  //start training
            Log.d(TAG, "start button");
            startTraining();
        } else if(start && !finish) {  //finish training
            Log.d(TAG, "finish button");
            finishTraining();
        }
    }

    private void startTraining() {
        startFinishButton.setText("Finish");

        startDateTime = new Date(System.currentTimeMillis());
        training.setDate(startDateTime);

        Thread durationThread = new Thread(new DurationRunnable());
        durationThread.start();

        distanceInMeters = 0;
        start = true;
    }

    private void finishTraining() {
        training.setDuration(Helper.getDuration(startDateTime));

        showLocationData();
        finish = true;

        editDistance();
    }

    private void saveTraining() {
        Log.d(TAG, "saveTraining " + training.toString());
        if (training != null) {
            training.setId((int) baseService.insertTraining(training));

            TinyFitnessProvider.getInstance().saveTraining(training);

            Toast.makeText(this, "training was saved: " + Helper.upToOneDecimalPlace(training.getDistance()) + " | " + training.getDuration(), Toast.LENGTH_SHORT).show();
        }
    }

    public void editDistance() {
        LayoutInflater di = LayoutInflater.from(this);
        View pathView = di.inflate(R.layout.distance, null);
        AlertDialog.Builder newPathDialogBuilder = new AlertDialog.Builder(this);
        newPathDialogBuilder.setView(pathView);
        final EditText distanceInput = pathView.findViewById(R.id.input_distance);
        distanceInput.setText(Helper.getStringFromDouble(training.getDistance()));
        newPathDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        (dialog, id) -> {
                            Log.d(TAG, "from input: " + training.getDistance());

                            training.setDistance(Double.parseDouble(String.valueOf(distanceInput.getText())));
                            saveTraining();

                            distanceTextView.setText(String.valueOf(training.getDistance()));
                            startFinishButton.setText("...");
                        })
                .setNegativeButton("Отмена",
                        (dialog, id) -> dialog.cancel());
        AlertDialog createDialog = newPathDialogBuilder.create();
        createDialog.show();
    }

    // top right menu
    public  boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "training history");
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // path to music
                String deletedTracksInfo;
                try {
                    deletedTracksInfo = Helper.getTrainingHistory(baseService.getTrainings());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Log.d(TAG, deletedTracksInfo);
                Intent deletedIntent = new Intent(this, ListActivity.class);
                deletedIntent.putExtra("content", deletedTracksInfo);
                startActivity(deletedIntent);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // BaseService
    private void startBaseService() {
        Log.d(TAG, "MainActivity startBaseService()");
        Intent intent = new Intent(this, BaseService.class);
        bindService(intent, baseServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection baseServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BaseService.LocalBinder binder = (BaseService.LocalBinder) service;
            baseService = binder.getService();
            Log.d(TAG, "MainActivity baseService onServiceConnected");

            List<Training> trainings;
            try {
                trainings = baseService.getTrainings();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Log.d(TAG, "trainings " + trainings.size());
            for(Training training : trainings) {
                Log.d(TAG, training.getId() + " " + training);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(TAG, "MainActivity baseService onServiceDisconnected");
        }
    };

    // LocationService
    private void startLocationService() {
        Log.d(TAG, "MainActivity startLocationService()");
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            Log.d(TAG, "MainActivity locationService onServiceConnected");

            locationService.getLocation();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "MainActivity locationService onServiceDisconnected");
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Start onDestroy " + training.getId());

        if (training.getDuration() > MINIMUM_DURATION && distanceInMeters > MINIMUM_DISTANCE)
            saveTraining();

        DurationRunnable.running = false;

        stopService(new Intent(this, BaseService.class));
        if (baseServiceConnection != null)
            unbindService(baseServiceConnection);

        stopService(new Intent(this, LocationService.class));
        if (locationServiceConnection != null)
            unbindService(locationServiceConnection);

        currentLocation = null;
        locationList = null;
        training = null;

        super.onDestroy();
    }
}
