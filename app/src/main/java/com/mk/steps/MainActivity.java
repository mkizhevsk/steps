package com.mk.steps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.mk.steps.data.StringRandomGenerator;
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

public class MainActivity extends AppCompatActivity {

    private Training training;

    public static boolean start;
    private boolean finish;

    private TextView durationTextView;
    private TextView distanceTextView;
    private TextView speedTextView;
    private Button startFinishButton;
    private TextView temperatureTextView;
    private TextView accuracyTextView;

    private double temperature;
    private float accuracy = 0;
    private float speed = 0;
    private Date startDateTime;

    private final int MINIMUM_DURATION = 1;
    private final int MINIMUM_DISTANCE = 500;

    public static Handler locationHandler;
    public static Handler durationHandler;
    public static Handler weatherHandler;
    public static Handler tinyFitnessHandler;

    private BaseService baseService;
    private LocationService locationService;

    private int DURATION_COUNTER = 0;
    private int DURATION_COUNTER_LIMIT = 60;

    final String TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        durationTextView = findViewById(R.id.durationTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        speedTextView = findViewById(R.id.speedTextView);
        speedTextView.setVisibility(View.GONE);
        startFinishButton = findViewById(R.id.startFinishButton);
        accuracyTextView = findViewById(R.id.accuracyTextView);

        Log.d(TAG, "onCreate " + Build.VERSION.SDK_INT);

        start = false;
        finish = false;

        if (checkPermissions(this, this)) {
            Log.d(TAG, "permission granted by default");
            startApp();
        }
    }

    private void startApp() {
        training = new Training(new Date(System.currentTimeMillis()), 0, 0, 1);

        locationHandler = getLocationHandler();
        durationHandler = getDurationHandler();
        weatherHandler = getWeatherHandler();
        tinyFitnessHandler = getTinyFitnessHandler();

        startBaseService();
        startLocationService();
        WeatherProvider.getInstance().getTemperature();
    }

    // handlers
    private Handler getLocationHandler() {
        return new Handler(message -> {
            Bundle bundle = message.getData();
            float[] locationInfo = bundle.getFloatArray("locationInfo");

            if(locationInfo != null && locationInfo.length > 2) {
                accuracy = locationInfo[1];
                speed = Helper.getSpeedInKmHour(locationInfo[2]);

                if(start)
                    training.setDistance(locationInfo[0] / 1000);

                Log.d(TAG, "locationHandler end: training.distance - " + training.getDistance() + " " + locationInfo[0]);
                //Toast.makeText(this, locationInfo[0] + " | " + locationInfo[3], Toast.LENGTH_SHORT).show();
            }

            showAccuracy();
            return true;
        });
    }

    private Handler getDurationHandler() {
        return new Handler(message -> {
            Log.d(TAG, "durationHandler");

            if (startDateTime != null && start && !finish)
                training.setDuration(Helper.getDuration(startDateTime));

            showLocationData();
            DURATION_COUNTER++;

            return true;
        });
    }

    private Handler getWeatherHandler() {
        return new Handler(message -> {
            Log.d(TAG, "weatherHandler");

            Bundle bundle = message.getData();
            temperature = bundle.getDouble("temperature");

            temperatureTextView.setText(Helper.getStringTemperature(temperature));

            return true;
        });
    }

    private Handler getTinyFitnessHandler() {
        return new Handler(message -> {
            Log.d(TAG, "tinyFitnessHandler");

            Bundle bundle = message.getData();
            boolean result = bundle.getBoolean("result");

            if (result) {
                String text = String.format(
                        "training was uploaded: %.1f | %d",
                        Helper.upToOneDecimalPlace(training.getDistance()),
                        training.getDuration()
                );
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "training was not uploaded", Toast.LENGTH_SHORT).show();
            }

            return true;
        });
    }

    private void showLocationData() {
        if(DURATION_COUNTER == DURATION_COUNTER_LIMIT) {
            WeatherProvider.getInstance().getTemperature();
            DURATION_COUNTER = 0;
        } else {
            temperatureTextView.setText(Helper.getStringTemperature(temperature));
        }

        durationTextView.setText(Helper.getStringDuration(training.getDuration()));
        distanceTextView.setText(Helper.getStringDistance(training.getDistance()));
        showAccuracy();

        if(speed > 0)
            speedTextView.setText(String.valueOf(Helper.upToOneDecimalPlace(speed)));
    }

    private void showAccuracy() {
        if(accuracy > 0)
            accuracyTextView.setText(Helper.getStringAccuracy(accuracy));
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
        training.setDateTime(startDateTime);
        if(locationService != null)
            locationService.clearDistance();

        Thread durationThread = new Thread(new DurationRunnable());
        durationThread.start();

        start = true;
    }

    private void finishTraining() {
        training.setDuration(Helper.getDuration(startDateTime));

        showLocationData();
        finish = true;
        LocationService.running = false;
        DurationRunnable.running = false;
        Log.d(TAG, "training.getDistance() " + training.getDistance());
        editDistance();
    }

    private void saveTraining() {
        Log.d(TAG, "saveTraining " + training.toString());
        if (training != null) {
            training.setInternalCode(StringRandomGenerator.getInstance().getValue());
            training.setId((int) baseService.insertTraining(training));

            TinyFitnessProvider.getInstance().saveTraining(training);
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

                            training.setDistance(Helper.getDoubleFromInput(distanceInput.getText()));
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
            case 1:
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            Log.d(TAG, "MainActivity locationService onServiceConnected");

            LocationService.running = true;
            locationService.getLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "MainActivity locationService onServiceDisconnected");
        }
    };

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

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 1) startApp();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Start onDestroy");

        if (training != null && training.getDuration() > MINIMUM_DURATION && training.getDistance() > MINIMUM_DISTANCE)
            saveTraining();

        DurationRunnable.running = false;

        stopService(new Intent(this, BaseService.class));
        if (baseServiceConnection != null)
            unbindService(baseServiceConnection);

        LocationService.running = false;
        stopService(new Intent(this, LocationService.class));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && locationServiceConnection != null)
            unbindService(locationServiceConnection);

        training = null;

        super.onDestroy();
    }

}
