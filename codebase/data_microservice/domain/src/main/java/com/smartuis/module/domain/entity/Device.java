package com.smartuis.module.domain.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

    @Document(collection = "devices")
    public class Device {
        private String deviceId;
        private String name;
        private List<Application> applications;

        public Device() {}

        public Device(String deviceId, String name, List<Application> applications) {
            this.deviceId = deviceId;
            this.name = name;
            this.applications = applications;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public List<Application> getApplications() {
            return applications;
        }

        public void setApplications(List<Application> applications) {
            this.applications = applications;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "deviceId='" + deviceId + '\'' +
                    ", name='" + name + '\'' +
                    ", applications=" + applications +
                    '}';
        }
    }
