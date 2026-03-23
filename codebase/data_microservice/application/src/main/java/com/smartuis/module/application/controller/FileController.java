package com.smartuis.module.application.controller;

import com.smartuis.module.domain.repository.StorageRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    private StorageRepository storageRepository;

    public FileController(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Operation(
            summary = "Guardar un archivo",
            description = "Permite subir y almacenar un archivo en el sistema. \n" +
                    "- El archivo se enviará en el cuerpo de la solicitud."
    )
    @PostMapping("/save")
    void saveFile(@RequestParam MultipartFile file)  {
        System.out.println(file.getOriginalFilename());

        storageRepository.saveFile(file, file.getOriginalFilename());
    }
}
