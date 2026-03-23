package com.smartuis.module.application.thread;

import java.util.ArrayList;
import java.util.List;

public class ListCameraThread {

    private static ListCameraThread instance;
    private List<CameraThread> threads;

    private ListCameraThread() {
        this.threads = new ArrayList<>();
    }

    public static ListCameraThread getInstance(){
        if(instance == null){
            instance = new ListCameraThread();
        }

        return instance;
    }

    public List<CameraThread> getThreads(){
        return this.threads;
    }

    public boolean existHilo(String idThread){
        CameraThread reproductor = this.getThreads().stream().
                filter(hilo -> hilo.getIdThread().equals(idThread))
                .findFirst().orElse(null);
        return reproductor != null;
    }

    public CameraThread findThread(String idThread){
        CameraThread reproductor = this.getThreads().stream()
                .filter(hilo->hilo.getIdThread().equals(idThread))
                .findFirst().orElse(null);
        return reproductor;
    }

}
