package com.smartuis.module.application.thread;

import com.smartuis.module.application.exceptions.ConectionStorageException;
import com.smartuis.module.domain.repository.StorageRepository;
import org.bytedeco.ffmpeg.global.avcodec;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

public class CameraThread extends Thread {

    private StorageRepository storageRepository;
    private String idThread;
    private String urlConnect;
    private Boolean paused;
    private final String extension;
    private BlockingQueue<Exception> exceptionQueue;
    private long duration;

    public CameraThread(StorageRepository storageRepository, String idThread, String urlConnect, long durationMin, BlockingQueue<Exception> exceptionQueue){
        this.storageRepository = storageRepository;
        this.idThread = idThread;
        this.urlConnect = urlConnect;
        this.paused = false;
        this.extension = "mp4";
        this.exceptionQueue = exceptionQueue;
        this.duration = durationMin*60*1000;
    }

    @Override
    public void run()  {

        try {
            startRecord(this.urlConnect);
        } catch (FFmpegFrameRecorder.Exception | InterruptedException | FrameGrabber.Exception e) {
            exceptionQueue.offer(new ConectionStorageException("Hubo un erro con la conexion"));
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    public void startRecord(String urlConexion) throws FrameGrabber.Exception, InterruptedException, FFmpegFrameRecorder.Exception {

        String fileTempName = "./application/" + idThread + "." + extension;
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(urlConexion);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();

        int imageWidth = grabber.getImageWidth();
        int imageHeight = grabber.getImageHeight();
        int audioChannels = grabber.getAudioChannels();
        int sampleRate = grabber.getSampleRate();
        double frameRate = grabber.getFrameRate();

        while (!isInterrupted()) {
            System.out.println("duracion dentro el hilo:" + this.duration);
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(fileTempName, imageWidth, imageHeight, audioChannels);
                recorder.setFormat(extension);
                recorder.setFrameRate(frameRate);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioBitrate(128000);
                recorder.setSampleRate(sampleRate);
                recorder.setAudioChannels(audioChannels);


                recorder.start();

                long lastTime = System.currentTimeMillis();
                long activeTimeElapsed = 0;

                while (activeTimeElapsed < this.duration && !isInterrupted()) {
                    synchronized (this) {
                        while (paused) {
                            System.out.println("pausado");
                            wait();
                            lastTime = System.currentTimeMillis();
                        }
                    }

                    System.out.println("grabando");

                    Frame img = grabber.grabFrame();
                    if (img != null) {
                        recorder.record(img);
                    }

                    /*Frame snd = grabber.grabSamples();
                    if (snd != null) {
                        recorder.record(snd);
                    }*/

                    long now = System.currentTimeMillis();
                    activeTimeElapsed += (now - lastTime);
                    lastTime = now;
                }

                recorder.stop();

                if (isInterrupted()) {
                    break;
                }

                File file = new File(fileTempName);
                String pathname = idThread + "/" + idThread + " - " + Instant.now() + "." + extension;

                storageRepository.saveFile(file, pathname);
                System.out.println(storageRepository.toString());
                System.out.println("guardando");
            }

            File file = new File(fileTempName);
            file.delete();

    }


    public void stopRecord(){
        this.interrupt();
    }


    public String getIdThread() {
        return idThread;
    }


    public synchronized void pauseRecord(){
        paused = true;
    }


    public synchronized void resumeRecord(){
        paused = false;
        this.notify();
    }

}



