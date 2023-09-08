package com.mk.steps.data.service;

import static com.mk.steps.MainActivity.currentLocation;
import static com.mk.steps.MainActivity.start;
import static com.mk.steps.MainActivity.training;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.mk.steps.MainActivity;

public class LocationService extends Service {

    private final IBinder mBinder = new LocalBinder();

    private float distanceInMeters;
    private final int CYCLE_DURATION = 3000;

    final String TAG = "myLogs";

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationService onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LocationService onDestroy");
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void getLocation() {
        Log.d(TAG, "getLocation start");
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                System.out.println("onLocationChanged");

                /*currentLocation = location;

                if (locationList != null && locationList.size() > 0) {
                    calculateDistance(location);
                    training.setDistanceFromMeters(distanceInMeters);
                }

                locationList.add(location);*/

                if(currentLocation != null)
                    calculateDistance(location);

                MainActivity.showDataHandler.sendEmptyMessage(1);

                Log.d(TAG, "Provider " + currentLocation.getProvider() + ",  скорость: " + currentLocation.getSpeed()
                        + ",  расстояние: " + distanceInMeters + ",  точность: " + currentLocation.getAccuracy());
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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CYCLE_DURATION, 0, locationListener);
    }

    private void calculateDistance(Location location) {
        if (start) {
            distanceInMeters += location.distanceTo(currentLocation);
            training.setDistance(distanceInMeters);
        }
//            distanceInMeters += location.distanceTo(locationList.get(locationList.size() - 1));
    }
}
