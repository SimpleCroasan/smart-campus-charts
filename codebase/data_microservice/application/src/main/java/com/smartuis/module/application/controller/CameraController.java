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

    private StorageRepository storageRepository;
    private ListCameraThread listCameraThread;
    private CameraRepository cameraRepository;
    private CameraMapper cameraMapper;
    @Value("${video.duration.minutes}")
    private long durationRecord;

    public CameraController(StorageRepository storageRepository, CameraRepository cameraRepository, CameraMapper cameraMapper) {
        this.storageRepository = storageRepository;
        this.cameraMapper = cameraMapper;
        this.listCameraThread = listCameraThread.getInstance();
        this.cameraRepository = cameraRepository;
    }


    @Operation(summary = "Inicia la transmisión en vivo de una cámara", description = "Obtiene el flujo de video en tiempo real de la cámara especificada por su ID.")
    @GetMapping(value = "/stream", produces = "multipart/x-mixed-replace;boundary=frame" )
    public void startStream(HttpServletResponse response, @RequestParam(value = "idCamera") String idCamera) {
        Camera camera = cameraRepository.findById(idCamera);

        if (camera == null){
            throw  new CameraNullExecption("Esta camara no existe");
        }

        response.setContentType("multipart/x-mixed-replace;boundary=frame");
        String rtspUrl = camera.getUrl();
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        try{
            grabber.setOption("rtsp_transport", "tcp");
            grabber.start();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }


        Java2DFrameConverter converter = new Java2DFrameConverter();

        while (true) {

            Frame frame = null;
            try {
                frame = grabber.grab();
            } catch (FFmpegFrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
            if (frame == null) {
                break;
            }

            BufferedImage bufferedImage = converter.convert(frame);
            if (bufferedImage == null) {
                continue;
            }

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

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        try {
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "Comienza la grabación de una cámara", description = "Inicia la grabación de video de la cámara especificada por su ID.")
    @GetMapping("/start")
    public ResponseEntity startStream(@RequestParam(value = "idCamera") String idCamera)   {
        Camera camera = cameraRepository.findById(idCamera);


        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        boolean existsHilo = listCameraThread.existHilo(camera.getName());
        if (existsHilo){
            return ResponseEntity.badRequest().body("Esta camara ya esta grabando");
        }

        BlockingQueue<Exception> exceptionQueue = new LinkedBlockingQueue<>();
        CameraThread cameraThread = new CameraThread(storageRepository, camera.getName(), camera.getUrl(), durationRecord, exceptionQueue);
        cameraThread.start();
        System.out.println("duracion:" + durationRecord);
        try {
            Exception exceptionHilo = exceptionQueue.poll(3,TimeUnit.SECONDS);
            if (exceptionHilo != null) {
                throw exceptionHilo;
            }

            listCameraThread.getThreads().add(cameraThread);
            camera.setState(StateCamera.Recording);
            cameraRepository.save(camera);
            CameraDTO cameraDTO = cameraMapper.mapCameraToCameraDTO(camera);
            return ResponseEntity.ok(cameraDTO);
        } catch (Exception e) {
            throw new ConectionStorageException("Hubo un erro con la conexion");
        }


    }

    @Operation(summary = "Detiene la grabación de una cámara", description = "Finaliza la grabación de video de la cámara especificada por su ID.")
    @GetMapping("/stop")
    public ResponseEntity stopStream(@RequestParam(value = "idCamera") String idCamera)   {
        Camera camera = cameraRepository.findById(idCamera);

        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        boolean existsHilo = listCameraThread.existHilo(camera.getName());
        if(!existsHilo){
            return ResponseEntity.badRequest().body("Esa camara ya esta parada");
        }

        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.stopRecord();
        listCameraThread.getThreads().remove(reproductor);

        camera.setState(StateCamera.Stopped);
        cameraRepository.save(camera);
        CameraDTO cameraDTO = cameraMapper.mapCameraToCameraDTO(camera);
        return ResponseEntity.ok().body(cameraDTO);
    }

    @Operation(summary = "Pausa la grabación de una cámara", description = "Suspende temporalmente la grabación de video de la cámara especificada por su ID.")
    @GetMapping("/pause")
    public ResponseEntity pauseStream(@RequestParam(value = "idCamera") String idCamera)   {

        Camera camera = cameraRepository.findById(idCamera);

        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        boolean existsHilo = listCameraThread.existHilo(camera.getName());
        if(!existsHilo){
            return ResponseEntity.badRequest().body("Esa camara ya esta parada");
        }

        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.pauseRecord();
        camera.setState(StateCamera.Paused);
        cameraRepository.save(camera);
        CameraDTO cameraDTO = cameraMapper.mapCameraToCameraDTO(camera);
        return ResponseEntity.ok().body(cameraDTO);
    }

    @Operation(summary = "Reanuda la grabación de una cámara", description = "Continúa la grabación de video previamente pausada de la cámara especificada por su ID.")
    @GetMapping("/resume")
    public ResponseEntity resumeStream(@RequestParam(value = "idCamera") String idCamera)   {

        Camera camera = cameraRepository.findById(idCamera);

        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        boolean existsHilo = listCameraThread.existHilo(camera.getName());
        if(!existsHilo){
            return ResponseEntity.badRequest().body("Esa camara  esta parada y no pausada. Dale Start.");
        }

        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.resumeRecord();
        camera.setState(StateCamera.Recording);
        cameraRepository.save(camera);
        CameraDTO cameraDTO = cameraMapper.mapCameraToCameraDTO(camera);
        return ResponseEntity.ok(cameraDTO);
    }

    @Operation(summary = "Añade una nueva cámara", description = "Registra una nueva cámara en el sistema con la información proporcionada.")
    @PostMapping("/add")
    public ResponseEntity addCamera(@RequestBody @Valid Camera camera){

        boolean existNombre = cameraRepository.existsByName(camera.getName());

        if (existNombre){
            return ResponseEntity.badRequest().body("Ya existe una camara con ese nombre");
        }

        boolean existUrl = cameraRepository.existsByUrl(camera.getUrl());

        if (existUrl){
            return ResponseEntity.badRequest().body("Ya existe una camara con esa url");
        }

        camera.setState(StateCamera.Stopped);
        Camera cameraSave = cameraRepository.save(camera);

        return ResponseEntity.ok(cameraMapper.mapCameraToCameraDTO(cameraSave));
    }

    @Operation(summary = "Lista todas las cámaras", description = "Recupera una lista de todas las cámaras registradas en el sistema.")
    @GetMapping("/list")
    public ResponseEntity listAllCamera(){
        List<Camera> cameras = cameraRepository.findAll();
        if(listCameraThread.getThreads().isEmpty()){
            cameras.stream().forEach(camera -> camera.setState(StateCamera.Stopped));
            cameraRepository.saveAll(cameras);
        }

        return ResponseEntity.ok(cameraMapper.mapCameraToCameraDTO(cameras));
    }

    @Operation(summary = "Elimina una cámara", description = "Borra la cámara especificada por su ID del sistema.")
    @DeleteMapping("/delete")
    public ResponseEntity deleteCamera(@RequestParam(value = "idCamera") String idCamera){
        Camera camera = cameraRepository.findById(idCamera);
        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        cameraRepository.delete(camera);
        return ResponseEntity.ok("Se ha eliminado la camara exitosamente");
    }

    
}
