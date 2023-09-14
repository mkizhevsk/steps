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
import com.mk.steps.data.Helper;
import com.mk.steps.data.dto.DatedLocation;

public class LocationService extends Service {

    private final IBinder mBinder = new LocalBinder();

    private final int LOCATION_CYCLE_DURATION = 1000;
    private final float LOCATION_MIN_DISTANCE = 0;
    private final String NETWORK_PROVIDER = "network";

    private final long MIN_DIFFERENCE_SECONDS = 5;
    private final long MAX_DIFFERENCE_SECONDS = 10;
    private float datedLocationDifferenceSeconds;
    private final float DATED_LOCATION_DIFFERENCE_METERS = 7;

    private final float LOW_SPEED_LIMIT = 10;
    private final float POOR_ACCURACY_LIMIT = 20;

    private final float LOW_SPEED_COEFFICIENT = 0.9F;
    private float distanceCoefficient;

    private float distanceInMeters;
    private DatedLocation currentDatedLocation;

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

        // default for running
        datedLocationDifferenceSeconds = MAX_DIFFERENCE_SECONDS;
        distanceCoefficient = LOW_SPEED_COEFFICIENT;

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged * * * " + location.getLatitude() + " " + location.getLongitude());

                if(currentDatedLocation != null) {
                    DatedLocation tempDatedLocation = new DatedLocation(location);

                    long differenceSeconds = currentDatedLocation.getSecondsDifference(tempDatedLocation.getDateTime());
                    float differenceMeters = currentDatedLocation.getLocation().distanceTo(tempDatedLocation.getLocation());
                    Log.d(TAG, "difference " + differenceSeconds + " " + differenceMeters);

                    if(differenceSeconds > datedLocationDifferenceSeconds && differenceMeters > DATED_LOCATION_DIFFERENCE_METERS) {
                        if(start && location.getSpeed() > 0) {
                            calculateDistance(location);
                        }
                        currentDatedLocation = tempDatedLocation;
                    }
                    MainActivity.locationHandler.sendMessage(getLocationMessage(location));
                } else {
                    currentDatedLocation = new DatedLocation(location);
                }

                setCoefficients(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_CYCLE_DURATION, LOCATION_MIN_DISTANCE, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_CYCLE_DURATION, LOCATION_MIN_DISTANCE, locationListener);
    }

    private void setCoefficients(Location location) {
        if(isMaximalSecondConditions(location)) {
            datedLocationDifferenceSeconds = MAX_DIFFERENCE_SECONDS;
            distanceCoefficient = LOW_SPEED_COEFFICIENT;
        } else {
            datedLocationDifferenceSeconds = MIN_DIFFERENCE_SECONDS;
            distanceCoefficient = 1;
        }
    }

    private boolean isMaximalSecondConditions(Location location) {
        boolean isLowSpeed = Helper.getSpeedInKmHour(location.getSpeed()) < LOW_SPEED_LIMIT;
        boolean isPoorAccuracy = location.getAccuracy() > POOR_ACCURACY_LIMIT;
        return isLowSpeed || isPoorAccuracy;
    }

    public void clearDistance() {
        this.distanceInMeters = 0;
    }

    private Message getLocationMessage(Location location) {
        float tempDistance =  currentDatedLocation != null ? getCurrentDistance(location) : 0;

        Bundle bundle = new Bundle();
        bundle.putFloatArray("locationInfo", new float[] {distanceInMeters, location.getAccuracy(), location.getSpeed(), tempDistance});

        Message message = new Message();
        message.setData(bundle);

        return message;
    }

    private float getCurrentDistance(Location location) {
        float distance = currentDatedLocation.getLocation().distanceTo(location);
        //Log.d(TAG, "distance " + distance);
        return distance * distanceCoefficient;
    }

    private void calculateDistance(Location location) {
        if (location.getProvider().equals(NETWORK_PROVIDER) && location.getAccuracy() > currentDatedLocation.getLocation().getAccuracy())
            return;

        distanceInMeters += getCurrentDistance(location);
    }
}
