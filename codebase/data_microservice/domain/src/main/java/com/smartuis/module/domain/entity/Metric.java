package com.smartuis.module.domain.entity;

public class Metric {
    private String measurement;
    private Double value;

    public Metric(){

    }

    public Metric(String measurement, Double value) {
        this.measurement = measurement;
        this.value = value;
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

    @Override
    public String toString() {
        return "Metric{" +
                "measurement='" + measurement + '\'' +
                ", value=" + value +
                '}';
    }
}
