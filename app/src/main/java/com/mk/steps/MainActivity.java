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
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.steps.data.Helper;
import com.mk.steps.data.Weather;
import com.mk.steps.data.service.BaseService;
import com.mk.steps.data.service.RetrofitService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    Location tempLocation;
    boolean firstLocationUnknown = true;
    double distanceInMeters = 0;
    String distanceInKm = "0";

    boolean start = false;
    boolean finish = false;

    InOut inOut;

    TextView temperatureTextView;
    TextView distanceTextView;
    Button startFinishButton;
    TextView accuracyTextView;

    String openWeatherAppId = "6e71959cff1c0c71a6049226d45c69a1";
    String openWeatherUnits = "metric";

    BaseService baseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        inOut = new InOut(this);
        //inOut.readData();

        temperatureTextView = findViewById(R.id.temperatureTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        startFinishButton = findViewById(R.id.startFinishButton);
        accuracyTextView = findViewById(R.id.accuracyTextView);

        // temperature
        Log.d(LOG_TAG, "temperature start");
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
                Log.d(LOG_TAG, " temperature " + weather.getVisibility() + " " + temperature);
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {

            }
        });

        Log.d(LOG_TAG, "firstLocationUnknown");
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

                        distanceInKm = Helper.getStringDistance(distanceInMeters);
                        distanceTextView.setText(distanceInKm);
                    }
                }

                accuracyTextView.setText(String.valueOf(location.getAccuracy()));
//                Log.d(LOG_TAG, String.valueOf(location.getProvider()) + ",  скорость: " + String.valueOf(location.getSpeed())
//                        + ",  расстояние: " + distanceInMeters + ",  точность: " + String.valueOf(location.getAccuracy()));
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

            editDistance();
        }
    }

    public void editDistance() {
        LayoutInflater di = LayoutInflater.from(this);
        View pathView = di.inflate(R.layout.distance, null);
        AlertDialog.Builder newPathDialogBuilder = new AlertDialog.Builder(this);
        newPathDialogBuilder.setView(pathView);
        final EditText distanceInput = pathView.findViewById(R.id.input_distance);
        distanceInput.setText(distanceInKm);
        newPathDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        (dialog, id) -> {
                            distanceInKm = String.valueOf(distanceInput.getText());
                            //Log.d(LOG_TAG, "from input: " + distanceInKm);
                            distanceTextView.setText(distanceInKm);
                            startFinishButton.setText("...");
                        })
                .setNegativeButton("Отмена",
                        (dialog, id) -> dialog.cancel());
        AlertDialog createDialog = newPathDialogBuilder.create();
        createDialog.show();
    }

    // BaseService
    private void startBaseService() {
        Log.d(LOG_TAG, "MainActivity startBaseService()");
        Intent intent = new Intent(this, BaseService.class);
        bindService(intent, baseServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection baseServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BaseService.LocalBinder binder = (BaseService.LocalBinder) service;
            baseService = binder.getService();
            Log.d(LOG_TAG, "MainActivity baseService onServiceConnected");


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(LOG_TAG, "MainActivity baseService onBaseServiceDisconnected");
        }
    };

    @Override
    protected void onDestroy() {
        Date currentTime = Calendar.getInstance().getTime();

        SimpleDateFormat simpleDate =  new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDate.format(currentTime);

        String info = date + "/_" + distanceInKm;
        //Log.d(LOG_TAG, info);
        InOut.lines.add(info);
        //inOut.writeData();

        Toast.makeText(this, "result was saved", Toast.LENGTH_SHORT).show();

        stopService(new Intent(this, BaseService.class));
        if (baseServiceConnection != null) {
            unbindService(baseServiceConnection);
        }

        super.onDestroy();
    }
}
