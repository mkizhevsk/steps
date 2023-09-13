package com.mk.steps.data.service;

import static com.mk.steps.MainActivity.start;

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
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.mk.steps.MainActivity;

public class LocationService extends Service {

    private final IBinder mBinder = new LocalBinder();

    private Location currentLocation;
    private float distanceInMeters;

    private final int CYCLE_DURATION = 5000;
    private final float MIN_DISTANCE = 3;
    private final String NETWORK_PROVIDER = "network";

    final String TAG = "myLogs";

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationService onCreate");
        clearDistance();
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
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged * * * " + location.getLatitude() + " " + location.getAltitude());

                if(currentLocation != null && start && location.getSpeed() > 0)
                    calculateDistance(location);

                MainActivity.locationHandler.sendMessage(getLocationMessage(location));

                if(currentLocation != null) {
                    Log.d(TAG, currentLocation.getLatitude() + " " + currentLocation.getAltitude() + " | Provider " + location.getProvider() + ",  скорость: " + location.getSpeed()
                            + ",  расстояние: " + currentLocation.distanceTo(location) + ",  точность: " + location.getAccuracy());
                }

                currentLocation = location;
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

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CYCLE_DURATION, MIN_DISTANCE, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CYCLE_DURATION, MIN_DISTANCE, locationListener);
    }

    public void clearDistance() {
        this.distanceInMeters = 0;
    }

    private Message getLocationMessage(Location location) {
        Bundle bundle = new Bundle();
        bundle.putFloatArray("locationInfo", new float[] {distanceInMeters, location.getAccuracy(), location.getSpeed()});

        Message message = new Message();
        message.setData(bundle);

        return message;
    }

    private void calculateDistance(Location location) {
        if (location.getProvider().equals(NETWORK_PROVIDER) && location.getAccuracy() > currentLocation.getAccuracy())
            return;

        float distance = currentLocation.distanceTo(location);
        Log.d(TAG, "distance " + distance);
        distanceInMeters += (distance * 0.9);
    }
}
