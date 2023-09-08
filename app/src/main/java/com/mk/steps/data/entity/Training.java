package com.mk.steps.data.entity;

import java.util.Date;

public class Training {

    private int id;
    private Date date;
    private float distance;
    private int duration;
    private int type;

    public Training() {
    }

    public Training(Date date, float distance, int duration, int type) {
        this.date = date;
        this.distance = distance;
        this.duration = duration;
        this.type = type;
    }

//    public void setDistanceFromMeters(float distanceInMeters) {
//        this.distance = distanceInMeters / 1000;
//    }

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

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
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

    @Override
    public String toString() {
        return "Training{" +
                "id=" + id +
                ", date=" + date +
                ", distance=" + distance +
                ", duration=" + duration +
                ", type=" + type +
                '}';
    }
}
