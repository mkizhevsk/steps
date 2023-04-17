package com.mk.steps.data;

import java.text.DecimalFormat;

public class Helper {

    public static String getStringDistance(double distanceInMeters) {
        DecimalFormat df = new DecimalFormat("###.#");
        return df.format(distanceInMeters /1000);
    }
}
