package com.mk.steps.data.dto;

import android.location.Location;

import java.util.Date;

public class DatedLocation {

    private Location location;
    private Date dateTime;

    public DatedLocation(Location location) {
        this.location = location;
        this.dateTime = new Date();
    }

    public long getSecondsDifference(Date date) {
        long seconds = Math.abs((this.dateTime.getTime() - date.getTime()) / 1000);
        System.out.println(seconds);
        return seconds;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
