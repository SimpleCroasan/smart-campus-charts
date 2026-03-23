package com.smartuis.module.domain.entity;

public class Application {
    private String applicationId;
    private String name;

    public  Application() {

    }

    public Application(String applicationId, String name) {
        this.applicationId = applicationId;
        this.name = name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Application{" +
                "applicationId='" + applicationId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
