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
//    @SerializedName("internalCode")
    private String internalCode;

    /**
     * training date
     */
//    @SerializedName("date")
    private Date dateTime;

    /**
     * distance in km
     */
//    @SerializedName("distance")
    private float distance;

    /**
     * duration in km
     */
//    @SerializedName("duration")
    private int duration;

    /**
     * 1 - running
     */
//    @SerializedName("type")
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
                ", date=" + dateTime +
                ", distance=" + distance +
                ", duration=" + duration +
                ", type=" + type +
                '}';
    }
}
