package com.smartuis.module.domain.entity;

import com.influxdb.annotations.Column;

import java.time.Instant;

public class Data {
    @Column(tag = true)
    private String location;

    @Column(measurement = true)
    private String measurement;

    @Column ()
    private Double value;

    @Column(timestamp = true)
    private Instant time;

    public Data () {

    }

    public Data(String location, String measurement, Double value){
        this.location = location;
        this.measurement = measurement;
        this.value = value;
        this.time = Instant.now();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    @Override
    public String toString() {
        return "Data{" +
                "location='" + location + '\'' +
                ", measurement='" + measurement + '\'' +
                ", value=" + value +
                ", time=" + time +
                '}';
    }
}
