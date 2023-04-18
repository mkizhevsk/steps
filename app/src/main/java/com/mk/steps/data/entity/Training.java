package com.mk.steps.data.entity;

import com.mk.steps.data.Helper;

import java.util.Date;

public class Training {

    private int id;
    private Date date;
    private double distance;
    private int duration;
    private int type;

    public Training() {
    }

    public Training(Date date, double distance, int duration, int type) {
        this.date = date;
        this.distance = distance;
        this.duration = duration;
        this.type = type;
    }

    @Override
    public String toString() {
        return Helper.getStringDate(date) + " " + distance + " " + duration + " " + type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
