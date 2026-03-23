package com.smartuis.module.domain.entity;

public class CameraDTO {

    private String id;
    private String name;
    private StateCamera state;

    public CameraDTO(String id, String name, StateCamera state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public StateCamera getState() {
        return state;
    }

    public void setState(StateCamera state) {
        this.state = state;
    }
}
