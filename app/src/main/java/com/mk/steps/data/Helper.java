package com.mk.steps.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Helper {

    public static String getStringDistance(double distanceInMeters) {
        DecimalFormat df = new DecimalFormat("###.#");
        return df.format(distanceInMeters /1000);
    }

    public static boolean checkPermissions(Context context, Activity mainActivity) {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(context, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mainActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    public static String getStringFromDouble(double value) {
        return String.valueOf(upToOneDecimalPlace(value));
    }

    public static double upToOneDecimalPlace(double value) {
        long intValue = Math.round(value * 10);
        return ((double) intValue) / 10;
    }

    public static int getDuration(LocalDateTime startDateTime) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Duration duration = Duration.between(startDateTime, LocalDateTime.now());
            return (int) duration.toMinutes();
        }
        return 0;
    }
}
