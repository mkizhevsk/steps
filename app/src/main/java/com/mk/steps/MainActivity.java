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
import android.view.Menu;
import android.view.MenuItem;
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
import java.text.ParseException;
import java.util.Date;
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
    private Date startDateTime;

    private Training training;

    boolean start = false;
    boolean finish = false;

    DecimalFormat df;

    TextView durationTextView;
    TextView distanceTextView;
    Button startFinishButton;
    TextView temperatureTextView;
    TextView accuracyTextView;

    String openWeatherAppId = "6e71959cff1c0c71a6049226d45c69a1";
    String openWeatherUnits = "metric";

    BaseService baseService;

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
        df = new DecimalFormat("###.#");

        Log.d(TAG, "onCreate " + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT >= 23) {
            if (Helper.checkPermissions(this, this)) {
                Log.d(TAG, "permission granted by default");
                startApp();
            }
        } else {
            startApp();
        }
    }

    private void startApp() {
        startDateTime = new Date(System.currentTimeMillis());
        training = new Training(new Date(System.currentTimeMillis()), 0, 0, 1);

        startBaseService();
        getTemperature();
        getLocation();
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
                temperatureTextView.setText(Helper.getStringTemperature(temperature));
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
                        durationTextView.setText(Helper.getStringDuration(startDateTime));

                        distanceInMeters += location.distanceTo(tempLocation);
                        tempLocation = location;

                        training.setDistance(distanceInMeters /1000);
                        distanceTextView.setText(Helper.getStringDistance(training.getDistance()));
                    }
                }

                accuracyTextView.setText(Helper.getStringAccuracy(location.getAccuracy()));
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

            training.setDate(new Date(System.currentTimeMillis()));
            distanceInMeters = 0;
        } else if(start && !finish) {
            //finish training
            finish = true;
            Log.d(TAG, "finish");
            training.setDuration(Helper.getDuration(startDateTime));

            editDistance();
        }
    }

    private void saveTraining() {
        Log.d(TAG, "saveTraining " + training.toString());
        if (training != null && training.getDistance() > 0) {
            training.setId((int) baseService.insertTraining(training));
            Toast.makeText(this, "training was saved: " + training.getDistance() + " | " + training.getDuration(), Toast.LENGTH_SHORT).show();
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

            Log.d(TAG, "MainActivity baseService onBaseServiceDisconnected");
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Start onDestroy " + training.getId());


        if(training != null && training.getId() == 0) {
            saveTraining();
        }

        stopService(new Intent(this, BaseService.class));
        if (baseServiceConnection != null) {
            unbindService(baseServiceConnection);
        }

        super.onDestroy();
    }
}
