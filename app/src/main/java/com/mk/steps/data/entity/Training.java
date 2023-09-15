package com.mk.steps.data.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Training {

    /**
     * sqlite id
     */
    private int id;

    /**
     * unique code
     */
    @SerializedName("internalCode")
    private String internalCode;

    /**
     * training date
     */
    @SerializedName("date")
    private Date date;

    /**
     * distance in km
     */
    @SerializedName("distance")
    private float distance;

    /**
     * duration in km
     */
    @SerializedName("duration")
    private int duration;

    /**
     * 1 - running
     */
    @SerializedName("type")
    private int type;

    public Training() {
    }

    public Training(Date date, float distance, int duration, int type) {
        this.date = date;
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
