package com.smartuis.module.domain.entity;

import java.time.Instant;

public class Header implements Cloneable{
    private String userUUID;
    private String deviceId;
    private Instant timeStamp;
    private String location;
    private String topic;
    private Boolean shouldRequeue;


    public  Header() {

    }

    public Header(String userUUID, String deviceId, Instant timeStamp ,String location, String topic, Boolean shouldRequeue){
        this.userUUID = userUUID;
        this.deviceId = deviceId;
        this.timeStamp = timeStamp;
        this.location = location;
        this.topic = topic;
        this.shouldRequeue = shouldRequeue;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Boolean getShouldRequeue() {
        return shouldRequeue;
    }

    public void setShouldRequeue(Boolean shouldRequeue) {
        this.shouldRequeue = shouldRequeue;
    }

    @Override
    public String toString() {
        return "Header{" +
                "userUUID='" + userUUID + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", timeStamp=" + timeStamp +
                ", location='" + location + '\'' +
                ", topic='" + topic + '\'' +
                ", shouldRequeue=" + shouldRequeue +
                '}';
    }

    @Override
    public Header clone() {
        try {
            Header clone = (Header) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
