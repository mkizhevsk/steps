package com.mk.steps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    Location tempLocation;
    boolean firstLocationUnknown = true;
    float distanceInMeters = 0;
    String distanceInKm = "0";

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

        df = new DecimalFormat("###.#");

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

                        distanceInKm = df.format(distanceInMeters /1000);
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
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                distanceInKm = String.valueOf(distanceInput.getText());
                                //Log.d(LOG_TAG, "from input: " + distanceInKm);
                                distanceTextView.setText(distanceInKm);
                                startFinishButton.setText("...");
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog createDialog = newPathDialogBuilder.create();
        createDialog.show();
    }

    @Override
    protected void onDestroy() {
        Date currentTime = Calendar.getInstance().getTime();

        SimpleDateFormat simpleDate =  new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDate.format(currentTime);

        String info = date + "/_" + distanceInKm;
        //Log.d(LOG_TAG, info);
        InOut.lines.add(info);
        inOut.writeData();

        Toast.makeText(this, "result was saved", Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }
}
