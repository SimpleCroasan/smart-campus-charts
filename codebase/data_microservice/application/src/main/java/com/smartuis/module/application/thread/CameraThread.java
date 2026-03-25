package com.smartuis.module.application.thread;

import com.smartuis.module.application.exceptions.ConectionStorageException;
import com.smartuis.module.domain.repository.StorageRepository;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

public class CameraThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(CameraThread.class);

    private final StorageRepository storageRepository;
    private final String idThread;
    private final String urlConnect;
    private Boolean paused;
    private final String extension;
    private final BlockingQueue<Exception> exceptionQueue;
    private final long duration;

    public CameraThread(StorageRepository storageRepository, String idThread,
                        String urlConnect, long durationMin, BlockingQueue<Exception> exceptionQueue) {
        this.storageRepository = storageRepository;
        this.idThread          = idThread;
        this.urlConnect        = urlConnect;
        this.paused            = false;
        this.extension         = "mp4";
        this.exceptionQueue    = exceptionQueue;
        this.duration          = durationMin * 60 * 1000;
    }

    @Override
    public void run() {
        log.info("Iniciando hilo de grabación para cámara: {}", idThread);
        try {
            startRecord(this.urlConnect);
        } catch (FFmpegFrameRecorder.Exception | InterruptedException | FrameGrabber.Exception e) {
            log.error("Error de grabación en cámara {}: {}", idThread, e.getMessage(), e);
            exceptionQueue.offer(new ConectionStorageException("Hubo un error con la conexión"));
        }
    }

    public void startRecord(String urlConexion)
            throws FrameGrabber.Exception, InterruptedException, FFmpegFrameRecorder.Exception {

        String fileTempName = "./application/" + idThread + "." + extension;
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(urlConexion);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();

        int    imageWidth    = grabber.getImageWidth();
        int    imageHeight   = grabber.getImageHeight();
        int    audioChannels = grabber.getAudioChannels();
        int    sampleRate    = grabber.getSampleRate();
        double frameRate     = grabber.getFrameRate();

        log.info("Conexión RTSP establecida para cámara {}. Resolución: {}x{}, FPS: {}",
                idThread, imageWidth, imageHeight, frameRate);

        while (!isInterrupted()) {
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                    fileTempName, imageWidth, imageHeight, audioChannels);
            recorder.setFormat(extension);
            recorder.setFrameRate(frameRate);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setAudioBitrate(128000);
            recorder.setSampleRate(sampleRate);
            recorder.setAudioChannels(audioChannels);
            recorder.start();

            long lastTime = System.currentTimeMillis(), activeTimeElapsed = 0;

            while (activeTimeElapsed < this.duration && !isInterrupted()) {
                synchronized (this) {
                    while (paused) {
                        log.debug("Grabación en pausa para cámara: {}", idThread);
                        wait();
                        lastTime = System.currentTimeMillis();
                    }
                }
                Frame img = grabber.grabFrame();
                if (img != null) recorder.record(img);
                long now = System.currentTimeMillis();
                activeTimeElapsed += (now - lastTime);
                lastTime = now;
            }

            recorder.stop();
            if (isInterrupted()) break;

            File   file     = new File(fileTempName);
            String pathname = idThread + "/" + idThread + " - " + Instant.now() + "." + extension;
            log.info("Guardando segmento de cámara {} en storage: {}", idThread, pathname);
            storageRepository.saveFile(file, pathname);
            log.info("Segmento almacenado exitosamente para cámara: {}", idThread);
        }

        File file = new File(fileTempName);
        if (file.delete()) log.debug("Archivo temporal eliminado: {}", fileTempName);
        log.info("Hilo de grabación finalizado para cámara: {}", idThread);
    }

    public void stopRecord() {
        log.info("Deteniendo grabación para cámara: {}", idThread);
        this.interrupt();
    }

    public String getIdThread() { return idThread; }

    public synchronized void pauseRecord() {
        log.info("Pausando grabación para cámara: {}", idThread);
        paused = true;
    }

    public synchronized void resumeRecord() {
        log.info("Reanudando grabación para cámara: {}", idThread);
        paused = false;
        this.notify();
    }
}