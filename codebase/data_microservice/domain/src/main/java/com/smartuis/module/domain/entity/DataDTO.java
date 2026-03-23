package com.smartuis.module.domain.entity;

import java.time.Instant;

public class DataDTO {
    private String location;

    private String measurement;

    private Double value;

    private Instant time;

    public DataDTO(String location, String measurement, Double value, Instant time) {
        this.location = location;
        this.measurement = measurement;
        this.value = value;
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
