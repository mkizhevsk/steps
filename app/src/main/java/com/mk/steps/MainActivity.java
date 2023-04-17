package com.mk.steps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.steps.data.Helper;
import com.mk.steps.data.Weather;
import com.mk.steps.data.entity.Training;
import com.mk.steps.data.service.BaseService;
import com.mk.steps.data.service.RetrofitService;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Location tempLocation;
    private boolean firstLocationUnknown = true;

    private double temperature;

    private double distanceInMeters = 0;
    private LocalDateTime startDateTime;

    private Training training;

    boolean start = false;
    boolean finish = false;

    DecimalFormat df;

    TextView temperatureTextView;
    TextView distanceTextView;
    Button startFinishButton;
    TextView accuracyTextView;

    String openWeatherAppId = "6e71959cff1c0c71a6049226d45c69a1";
    String openWeatherUnits = "metric";

    BaseService baseService;

    final String TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        temperatureTextView = findViewById(R.id.temperatureTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        startFinishButton = findViewById(R.id.startFinishButton);
        accuracyTextView = findViewById(R.id.accuracyTextView);
        df = new DecimalFormat("###.#");

        if(Helper.checkPermissions(this, this)) {
            Log.d(TAG, "permission granted by default");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startDateTime = LocalDateTime.now();
                training = new Training(startDateTime.toLocalDate(), 0, 0, 1);
            }

            startBaseService();
            getTemperature();
            getLocation();
        }
    }

    private void getTemperature() {
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
                temperature = weather.getMain().getTemp();
                Log.d(TAG, " temperature " + weather.getVisibility() + " " + temperature);
                temperatureTextView.setText(String.valueOf(temperature));
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {

            }
        });
    }

    private void getLocation() {
        Log.d(TAG, "firstLocationUnknown");
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if(firstLocationUnknown) {
                    tempLocation = location;
                    firstLocationUnknown = false;
                } else {
                    if(start && !finish) {
                        distanceInMeters += location.distanceTo(tempLocation);
                        tempLocation = location;

                        training.setDistance(distanceInMeters /1000);
                        distanceTextView.setText(Helper.getStringFromDouble(training.getDistance()));
                    }
                }

                accuracyTextView.setText(String.valueOf(location.getAccuracy()));
                Log.d(TAG, location.getProvider() + ",  скорость: " + location.getSpeed()
                        + ",  расстояние: " + distanceInMeters + ",  точность: " + location.getAccuracy());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
    }

    public void onClick(View view) {
        if(!start && !finish) {
            //start training
            startFinishButton.setText("Finish");
            start = true;
        } else if(start && !finish) {
            //finish training
            finish = true;

            training.setDuration(Helper.getDuration(startDateTime));

            editDistance();
        }
    }

    public void editDistance() {
        LayoutInflater di = LayoutInflater.from(this);
        View pathView = di.inflate(R.layout.distance, null);
        AlertDialog.Builder newPathDialogBuilder = new AlertDialog.Builder(this);
        newPathDialogBuilder.setView(pathView);
        final EditText distanceInput = pathView.findViewById(R.id.input_distance);
        distanceInput.setText(String.valueOf(training.getDistance()));
        newPathDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        (dialog, id) -> {
                            training.setDistance(Double.parseDouble(String.valueOf(distanceInput.getText())));
                            Log.d(TAG, "from input: " + training.getDistance());
                            distanceTextView.setText(String.valueOf(training.getDistance()));
                            startFinishButton.setText("...");
                        })
                .setNegativeButton("Отмена",
                        (dialog, id) -> dialog.cancel());
        AlertDialog createDialog = newPathDialogBuilder.create();
        createDialog.show();
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

            List<Training> trainings = baseService.getTrainings();
            Log.d(TAG, "trainings " + trainings.size());
            for(Training training : trainings) {
                Log.d(TAG, training.getId() + " " + training.getDate() + " " + training.getDistance() + " " + training.getDuration() + " " + training.getType());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(TAG, "MainActivity baseService onBaseServiceDisconnected");
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Start onDestroy");

        Log.d(TAG, "distanceInKm " + training.getDistance());
        if (training.getDistance() > 0) {
            baseService.insertTraining(training);
            Toast.makeText(this, "result was saved", Toast.LENGTH_SHORT).show();
        }

        stopService(new Intent(this, BaseService.class));
        if (baseServiceConnection != null) {
            unbindService(baseServiceConnection);
        }

        super.onDestroy();
    }
}
