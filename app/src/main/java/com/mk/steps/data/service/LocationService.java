package com.mk.steps.data.service;

import static com.mk.steps.MainActivity.start;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import com.mk.steps.R;
import com.mk.steps.data.Helper;
import com.mk.steps.data.dto.DatedLocation;

public class LocationService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private final String TAG = "myLogs";

    // Location update settings
    private static final int LOCATION_CYCLE_DURATION = 1000;
    private static final float LOCATION_MIN_DISTANCE = 0;
    private static final String NETWORK_PROVIDER = "network";

    // Distance and accuracy settings
    private static final long MIN_DIFFERENCE_SECONDS = 5;
    private static final long MAX_DIFFERENCE_SECONDS = 10;
    private static final float DATED_LOCATION_DIFFERENCE_METERS = 7;
    private static final float LOW_SPEED_LIMIT = 10;
    private static final float POOR_ACCURACY_LIMIT = 20;

    // State variables
    private float datedLocationDifferenceSeconds;
    private float distanceInMeters;
    private DatedLocation currentDatedLocation;

    public static boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationService onCreate");
        clearDistance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService onStartCommand");

        setupForegroundService();

        running = true;
        getLocationUpdates();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LocationService onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    private void setupForegroundService() {
        final String CHANNEL_ID = "Foreground Service";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentText("Steps is running")
                    .setContentTitle("STEPS")
                    .setSmallIcon(R.drawable.running_man);
            startForeground(1001, notification.build());
        }
    }

    public void getLocationUpdates() {
        Log.d(TAG, "getLocation start");

        datedLocationDifferenceSeconds = MAX_DIFFERENCE_SECONDS;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = createLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle permission not granted case
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_CYCLE_DURATION, LOCATION_MIN_DISTANCE, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_CYCLE_DURATION, LOCATION_MIN_DISTANCE, locationListener);
    }

    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                handleLocationChange(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    private void handleLocationChange(Location location) {
        if (!running) return;

        Log.d(TAG, "onLocationChanged * * * " + location.getLatitude() + " " + location.getLongitude());

        if (currentDatedLocation != null) {
            processNewLocation(location);
        } else {
            currentDatedLocation = new DatedLocation(location);
        }

        setCoefficients(location);
        MainActivity.locationHandler.sendMessage(createLocationMessage(location));
    }

    private void processNewLocation(Location location) {
        DatedLocation tempDatedLocation = new DatedLocation(location);
        long differenceSeconds = currentDatedLocation.getSecondsDifference(tempDatedLocation.getDateTime());
        float differenceMeters = currentDatedLocation.getLocation().distanceTo(tempDatedLocation.getLocation());

        Log.d(TAG, "difference " + differenceSeconds + " " + differenceMeters);

        if (shouldUpdateLocation(differenceSeconds, differenceMeters)) {
            if (start && location.getSpeed() > 0) {
                calculateDistance(location);
            }
            currentDatedLocation = tempDatedLocation;
        }
    }

    private boolean shouldUpdateLocation(long differenceSeconds, float differenceMeters) {
        return differenceSeconds > datedLocationDifferenceSeconds && differenceMeters > DATED_LOCATION_DIFFERENCE_METERS;
    }

    private void setCoefficients(Location location) {
        datedLocationDifferenceSeconds = isMaximalSecondConditions(location) ? MAX_DIFFERENCE_SECONDS : MIN_DIFFERENCE_SECONDS;
    }

    private boolean isMaximalSecondConditions(Location location) {
        boolean isLowSpeed = Helper.getSpeedInKmHour(location.getSpeed()) < LOW_SPEED_LIMIT;
        boolean isPoorAccuracy = location.getAccuracy() > POOR_ACCURACY_LIMIT;
        return isLowSpeed || isPoorAccuracy;
    }

    public void clearDistance() {
        distanceInMeters = 0;
    }

    private Message createLocationMessage(Location location) {
        float tempDistance = currentDatedLocation != null ? getCurrentDistance(location) : 0;
        Bundle bundle = new Bundle();
        bundle.putFloatArray("locationInfo", new float[]{distanceInMeters, location.getAccuracy(), location.getSpeed(), tempDistance});

        Message message = new Message();
        message.setData(bundle);
        return message;
    }

    private float getCurrentDistance(Location location) {
        return currentDatedLocation.getLocation().distanceTo(location);
    }

    private void calculateDistance(Location location) {
        if (isPoorNetworkAccuracy(location)) return;
        distanceInMeters += getCurrentDistance(location);
    }

    private boolean isPoorNetworkAccuracy(Location location) {
        return location.getProvider().equals(NETWORK_PROVIDER) && location.getAccuracy() > currentDatedLocation.getLocation().getAccuracy();
    }
}
