package com.mk.steps.data.service;

import static com.mk.steps.ui.MainActivity.start;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.mk.steps.R;
import com.mk.steps.data.dto.DatedLocation;
import com.mk.steps.data.util.Helper;
import com.mk.steps.ui.MainActivity;

public class LocationService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private final String TAG = "myLogs";

    // Location update settings
    private static final int LOCATION_CYCLE_DURATION = 1000;
    private static final float LOCATION_MIN_DISTANCE = 1;

    /**
     * Distance and accuracy settings
     */
    private static final float MIN_DIFFERENCE_METERS = 4; // condition to update currentDatedLocation

    private static final long MIN_DIFFERENCE_SECONDS = 2;
    private static final long MAX_DIFFERENCE_SECONDS = 4; // if velocity in km/h < LOW_SPEED_LIMIT
    private static final float LOW_SPEED_LIMIT = 10;      // condition for max or min seconds

    private static final float POOR_ACCURACY_LIMIT = 20;  // minimal accuracy to process location

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
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.app_name) + " is running")
                    .setSmallIcon(R.drawable.running_man);
            startForeground(1001, notification.build());
        }
    }

    public void getLocationUpdates() {
        Log.d(TAG, "getLocationUpdates() start");

        datedLocationDifferenceSeconds = MAX_DIFFERENCE_SECONDS;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = createLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle permission not granted case
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_CYCLE_DURATION, LOCATION_MIN_DISTANCE, locationListener);

        // Optionally, request network updates as a fallback after some delay (if GPS is not available)
        new Handler().postDelayed(() -> {
            if (isGpsUnavailable(locationManager)) {
                Log.d(TAG, "GPS is unavailable, falling back to NETWORK_PROVIDER.");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_CYCLE_DURATION, LOCATION_MIN_DISTANCE, locationListener);
            }
        }, 5000); // Wait for 5 seconds before switching to the network provider
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
        if (!running)
            return;

        // Skip location if accuracy is worse than 20 meters
        if (location.getAccuracy() > POOR_ACCURACY_LIMIT) {
            Log.d(TAG, "Location accuracy too low, skipping update");
            return;
        }

        Log.d(TAG, "* onLocationChanged * | accuracy  " + location.getAccuracy() + ", source: " + location.getProvider());

        if (currentDatedLocation != null) {
            processNewLocation(location);
        } else {
            currentDatedLocation = new DatedLocation(location);
        }

        setCoefficients(location);
        MainActivity.locationHandler.sendMessage(createLocationMessage(location));
    }

    private void processNewLocation(Location newLocation) {
        DatedLocation newDatedLocation = new DatedLocation(newLocation);
        long differenceSeconds = currentDatedLocation.getSecondsDifference(newDatedLocation.getDateTime());
        float differenceMeters = currentDatedLocation.getLocation().distanceTo(newDatedLocation.getLocation());

        if (shouldUpdateLocation(differenceSeconds, differenceMeters)) {
            if (start && newLocation.getSpeed() > 0) {
                distanceInMeters += currentDatedLocation.getLocation().distanceTo(newLocation);
                Log.d(TAG, "# new distanceInMeters: " + distanceInMeters + ", source: " + newLocation.getProvider());
            }
            currentDatedLocation = newDatedLocation;
        }
    }

    private boolean shouldUpdateLocation(long differenceSeconds, float differenceMeters) {
        return differenceSeconds > datedLocationDifferenceSeconds && differenceMeters > MIN_DIFFERENCE_METERS;
    }

    private void setCoefficients(Location location) {
        datedLocationDifferenceSeconds = Helper.getSpeedInKmHour(location.getSpeed()) < LOW_SPEED_LIMIT ? MAX_DIFFERENCE_SECONDS : MIN_DIFFERENCE_SECONDS;
    }

    public void clearDistance() {
        distanceInMeters = 0;
    }

    private Message createLocationMessage(Location location) {
        Bundle bundle = new Bundle();
        bundle.putFloatArray("locationInfo", new float[]{distanceInMeters, location.getAccuracy()});

        Message message = new Message();
        message.setData(bundle);
        return message;
    }

    private float getCurrentDistance(Location location) {
        return currentDatedLocation.getLocation().distanceTo(location);
    }

    private boolean isGpsUnavailable(LocationManager locationManager) {
        // Check if GPS provider is available
        return !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
