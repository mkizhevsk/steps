package com.mk.steps.data.entity;

import java.util.Date;

public class Training {

    /**
     * sqlite id
     */
    private int id;

    /**
     * unique code
     */
    private String internalCode;

    /**
     * training date
     */
    private Date dateTime;

    /**
     * distance in km
     */
    private double distance;

    /**
     * duration in km
     */
    private int duration;

    /**
     * 1 - running
     */
    private int type;

    public Training() {
    }

    public Training(Date dateTime, float distance, int duration, int type) {
        this.dateTime = dateTime;
        this.distance = distance;
        this.duration = duration;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInternalCode() {
        return internalCode;
    }

    public void setInternalCode(String internalCode) {
        this.internalCode = internalCode;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
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

    public int getIntDistance() {
        return (int) Math.round(distance * 10);
    }

    public void setDistanceFromInt(int intDistance) {
        this.distance = (double) intDistance / 10;
    }

    @Override
    public String toString() {
        return "Training{" +
                "id=" + id +
                ", internalCode='" + internalCode + '\'' +
                ", dateTime=" + dateTime +
                ", distance=" + distance +
                ", duration=" + duration +
                ", type=" + type +
                '}';
    }
}
