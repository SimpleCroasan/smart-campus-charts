package com.smartuis.module.application.controller;

import com.smartuis.module.application.exceptions.CameraNullExecption;
import com.smartuis.module.application.thread.ListCameraThread;
import com.smartuis.module.application.thread.CameraThread;
import com.smartuis.module.application.exceptions.ConectionStorageException;
import com.smartuis.module.application.mapper.CameraMapper;
import com.smartuis.module.domain.entity.Camera;
import com.smartuis.module.domain.entity.CameraDTO;
import com.smartuis.module.domain.entity.StateCamera;
import com.smartuis.module.domain.repository.CameraRepository;
import com.smartuis.module.domain.repository.StorageRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/camera")
public class CameraController {

    private static final Logger log = LoggerFactory.getLogger(CameraController.class);

    private final StorageRepository storageRepository;
    private final ListCameraThread listCameraThread;
    private final CameraRepository cameraRepository;
    private final CameraMapper cameraMapper;

    @Value("${video.duration.minutes}")
    private long durationRecord;

    public CameraController(StorageRepository storageRepository,
                            CameraRepository cameraRepository,
                            CameraMapper cameraMapper) {
        this.storageRepository = storageRepository;
        this.cameraMapper = cameraMapper;
        this.listCameraThread = ListCameraThread.getInstance();
        this.cameraRepository = cameraRepository;
    }

    @Operation(summary = "Inicia la transmisión en vivo de una cámara")
    @GetMapping(value = "/stream", produces = "multipart/x-mixed-replace;boundary=frame")
    public void startStream(HttpServletResponse response,
                            @RequestParam(value = "idCamera") String idCamera) {
        log.info("GET /camera/stream - iniciando stream para cámara: {}", idCamera);
        Camera camera = cameraRepository.findById(idCamera);
        if (camera == null) {
            log.warn("Cámara no encontrada para stream: {}", idCamera);
            throw new CameraNullExecption("Esta camara no existe");
        }
        response.setContentType("multipart/x-mixed-replace;boundary=frame");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(camera.getUrl());
        try {
            grabber.setOption("rtsp_transport", "tcp");
            grabber.start();
            log.info("Stream RTSP iniciado para cámara: {}", idCamera);
        } catch (FFmpegFrameGrabber.Exception e) {
            log.error("Error iniciando stream RTSP para cámara {}: {}", idCamera, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        int frameCount = 0;
        while (true) {
            Frame frame;
            try {
                frame = grabber.grab();
            } catch (FFmpegFrameGrabber.Exception e) {
                log.error("Error leyendo frame de cámara {}: {}", idCamera, e.getMessage(), e);
                throw new RuntimeException(e);
            }
            if (frame == null) {
                log.info("Stream finalizado para cámara: {} (frames: {})", idCamera, frameCount);
                break;
            }
            BufferedImage bufferedImage = converter.convert(frame);
            if (bufferedImage == null) continue;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();
                response.getOutputStream().write(("--frame\r\n" +
                        "Content-Type: image/jpeg\r\n" +
                        "Content-Length: " + imageBytes.length + "\r\n\r\n").getBytes());
                response.getOutputStream().write(imageBytes);
                response.getOutputStream().write("\r\n".getBytes());
                response.getOutputStream().flush();
                frameCount++;
            } catch (IOException e) {
                log.error("Error enviando frame al cliente para cámara {}: {}", idCamera, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        try {
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            log.error("Error deteniendo grabber para cámara {}: {}", idCamera, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "Comienza la grabación de una cámara")
    @GetMapping("/start")
    public ResponseEntity startStream(@RequestParam(value = "idCamera") String idCamera) {
        log.info("GET /camera/start - iniciando grabación para cámara: {}", idCamera);
        Camera camera = cameraRepository.findById(idCamera);
        if (camera == null) {
            log.warn("Cámara no encontrada para iniciar grabación: {}", idCamera);
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }
        if (listCameraThread.existHilo(camera.getName())) {
            log.warn("La cámara ya está grabando: {}", camera.getName());
            return ResponseEntity.badRequest().body("Esta camara ya esta grabando");
        }
        log.debug("Duración configurada: {} minutos para cámara: {}", durationRecord, idCamera);
        BlockingQueue<Exception> exceptionQueue = new LinkedBlockingQueue<>();
        CameraThread cameraThread = new CameraThread(
                storageRepository, camera.getName(), camera.getUrl(), durationRecord, exceptionQueue);
        cameraThread.start();
        try {
            Exception exceptionHilo = exceptionQueue.poll(3, TimeUnit.SECONDS);
            if (exceptionHilo != null) {
                log.error("Error al iniciar grabación para cámara {}: {}", idCamera, exceptionHilo.getMessage());
                throw exceptionHilo;
            }
            listCameraThread.getThreads().add(cameraThread);
            camera.setState(StateCamera.Recording);
            cameraRepository.save(camera);
            log.info("Grabación iniciada exitosamente para cámara: {}", idCamera);
            return ResponseEntity.ok(cameraMapper.mapCameraToCameraDTO(camera));
        } catch (Exception e) {
            log.error("Error de conexión al iniciar grabación para cámara {}: {}", idCamera, e.getMessage(), e);
            throw new ConectionStorageException("Hubo un erro con la conexion");
        }
    }

    @Operation(summary = "Detiene la grabación de una cámara")
    @GetMapping("/stop")
    public ResponseEntity stopStream(@RequestParam(value = "idCamera") String idCamera) {
        log.info("GET /camera/stop - deteniendo grabación para cámara: {}", idCamera);
        Camera camera = cameraRepository.findById(idCamera);
        if (camera == null) {
            log.warn("Cámara no encontrada para detener: {}", idCamera);
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }
        if (!listCameraThread.existHilo(camera.getName())) {
            log.warn("La cámara ya está detenida: {}", camera.getName());
            return ResponseEntity.badRequest().body("Esa camara ya esta parada");
        }
        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.stopRecord();
        listCameraThread.getThreads().remove(reproductor);
        camera.setState(StateCamera.Stopped);
        cameraRepository.save(camera);
        log.info("Grabación detenida exitosamente para cámara: {}", idCamera);
        return ResponseEntity.ok().body(cameraMapper.mapCameraToCameraDTO(camera));
    }

    @Operation(summary = "Pausa la grabación de una cámara")
    @GetMapping("/pause")
    public ResponseEntity pauseStream(@RequestParam(value = "idCamera") String idCamera) {
        log.info("GET /camera/pause - pausando grabación para cámara: {}", idCamera);
        Camera camera = cameraRepository.findById(idCamera);
        if (camera == null) {
            log.warn("Cámara no encontrada para pausar: {}", idCamera);
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }
        if (!listCameraThread.existHilo(camera.getName())) {
            log.warn("La cámara no está grabando: {}", camera.getName());
            return ResponseEntity.badRequest().body("Esa camara ya esta parada");
        }
        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.pauseRecord();
        camera.setState(StateCamera.Paused);
        cameraRepository.save(camera);
        log.info("Grabación pausada exitosamente para cámara: {}", idCamera);
        return ResponseEntity.ok().body(cameraMapper.mapCameraToCameraDTO(camera));
    }

    @Operation(summary = "Reanuda la grabación de una cámara")
    @GetMapping("/resume")
    public ResponseEntity resumeStream(@RequestParam(value = "idCamera") String idCamera) {
        log.info("GET /camera/resume - reanudando grabación para cámara: {}", idCamera);
        Camera camera = cameraRepository.findById(idCamera);
        if (camera == null) {
            log.warn("Cámara no encontrada para reanudar: {}", idCamera);
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }
        if (!listCameraThread.existHilo(camera.getName())) {
            log.warn("La cámara está detenida (no pausada): {}", camera.getName());
            return ResponseEntity.badRequest().body("Esa camara  esta parada y no pausada. Dale Start.");
        }
        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.resumeRecord();
        camera.setState(StateCamera.Recording);
        cameraRepository.save(camera);
        log.info("Grabación reanudada exitosamente para cámara: {}", idCamera);
        return ResponseEntity.ok(cameraMapper.mapCameraToCameraDTO(camera));
    }

    @Operation(summary = "Añade una nueva cámara")
    @PostMapping("/add")
    public ResponseEntity addCamera(@RequestBody @Valid Camera camera) {
        log.info("POST /camera/add - añadiendo cámara: {}", camera.getName());
        if (cameraRepository.existsByName(camera.getName())) {
            log.warn("Ya existe una cámara con el nombre: {}", camera.getName());
            return ResponseEntity.badRequest().body("Ya existe una camara con ese nombre");
        }
        if (cameraRepository.existsByUrl(camera.getUrl())) {
            log.warn("Ya existe una cámara con la URL: {}", camera.getUrl());
            return ResponseEntity.badRequest().body("Ya existe una camara con esa url");
        }
        camera.setState(StateCamera.Stopped);
        Camera cameraSave = cameraRepository.save(camera);
        log.info("Cámara añadida exitosamente: {} (id: {})", cameraSave.getName(), cameraSave.getId());
        return ResponseEntity.ok(cameraMapper.mapCameraToCameraDTO(cameraSave));
    }

    @Operation(summary = "Lista todas las cámaras")
    @GetMapping("/list")
    public ResponseEntity listAllCamera() {
        log.info("GET /camera/list");
        List<Camera> cameras = cameraRepository.findAll();
        if (listCameraThread.getThreads().isEmpty()) {
            cameras.forEach(camera -> camera.setState(StateCamera.Stopped));
            cameraRepository.saveAll(cameras);
        }
        log.debug("Retornando {} cámaras", cameras.size());
        return ResponseEntity.ok(cameraMapper.mapCameraToCameraDTO(cameras));
    }

    @Operation(summary = "Elimina una cámara")
    @DeleteMapping("/delete")
    public ResponseEntity deleteCamera(@RequestParam(value = "idCamera") String idCamera) {
        log.info("DELETE /camera/delete - eliminando cámara: {}", idCamera);
        Camera camera = cameraRepository.findById(idCamera);
        if (camera == null) {
            log.warn("Cámara no encontrada para eliminar: {}", idCamera);
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }
        cameraRepository.delete(camera);
        log.info("Cámara eliminada exitosamente: {}", idCamera);
        return ResponseEntity.ok("Se ha eliminado la camara exitosamente");
    }
}