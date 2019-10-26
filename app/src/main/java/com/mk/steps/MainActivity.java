package com.mk.steps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    Location tempLocation;
    boolean firstLocationUnknown = true;
    float distance = 0;

    boolean start = false;
    boolean finish = false;

    InOut inOut;

    DecimalFormat df;

    TextView distanceTextView;
    Button startFinishButton;
    TextView accuracyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        inOut = new InOut(this);
        inOut.readData();

        distanceTextView = findViewById(R.id.distance);
        startFinishButton = findViewById(R.id.btnStartFinish);
        accuracyTextView = findViewById(R.id.accuracy);

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
                        distance += location.distanceTo(tempLocation);
                        tempLocation = location;
                    }
                }

                df = new DecimalFormat("###.#");
                distanceTextView.setText(df.format(distance/1000));
                accuracyTextView.setText(String.valueOf(location.getAccuracy()));
                Log.d(LOG_TAG, String.valueOf(location.getProvider()) + ",  скорость: " + String.valueOf(location.getSpeed())
                        + ",  расстояние: " + distance + ",  точность: " + String.valueOf(location.getAccuracy()));
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
            startFinishButton.setText("Finish");
            start = true;
        } else if(start && !finish) {
            startFinishButton.setText("...");
            finish = true;
        }
    }

    @Override
    protected void onDestroy() {
        Date currentTime = Calendar.getInstance().getTime();
        String date = String.valueOf(currentTime);
        Log.d(LOG_TAG, date);

        String dis = "0";
        if (distance != 0) dis = df.format(distance/1000);
        Log.d(LOG_TAG, dis);

        String info = date + "/_" + dis;
        Log.d(LOG_TAG, info);
        InOut.lines.add(info);
        inOut.writeData();
        Toast.makeText(this, "result was saved", Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }
}
