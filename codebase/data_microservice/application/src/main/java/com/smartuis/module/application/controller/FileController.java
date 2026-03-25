package com.smartuis.module.application.controller;

import com.smartuis.module.domain.repository.StorageRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final StorageRepository storageRepository;

    public FileController(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Operation(summary = "Guardar un archivo")
    @PostMapping("/save")
    void saveFile(@RequestParam MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("POST /file/save - guardando archivo: {}, tamaño: {} bytes", filename, file.getSize());
        storageRepository.saveFile(file, filename);
        log.info("Archivo '{}' almacenado exitosamente", filename);
    }
}