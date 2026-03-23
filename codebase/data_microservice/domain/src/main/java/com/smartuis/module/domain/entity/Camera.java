package com.smartuis.module.domain.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("cameras")
public class Camera {
        @Id
        private String id;
        @NotBlank(message = "El nombre no debe estar vacio")
        private String name;
        @NotBlank(message = "La url no debe estar vacia")
        @Pattern(message = "Debe ser una url compatible con protocolo rtsp" , regexp = "rtsp:\\/\\/(?:[a-zA-Z0-9\\-._~%!$&'()*+,;=]+(?::[a-zA-Z0-9\\-._~%!$&'()*+,;=]*)?@)?(?:\\[[\\da-fA-F:.]+\\]|[a-zA-Z0-9\\-._~%]+)(?::\\d+)?(?:\\/[^\\s]*)?")
        private String url;
        private StateCamera state;

    public Camera(String id, String name, String url, StateCamera state) {
        this.id = id;
        this.name = name;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public StateCamera getState() {
        return state;
    }

    public void setState(StateCamera state) {
        this.state = state;
    }
}
