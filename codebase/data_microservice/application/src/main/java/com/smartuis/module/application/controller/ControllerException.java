package com.smartuis.module.application.controller;

import com.smartuis.module.application.exceptions.CameraNullExecption;
import com.smartuis.module.application.exceptions.ConectionStorageException;
import com.smartuis.module.persistence.exceptions.UnitsTimeException;
import com.smartuis.module.persistence.exceptions.UploadFileException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerException {

    @ExceptionHandler(UnitsTimeException.class)
    public ResponseEntity handlerUnitsTimeException(UnitsTimeException unitsTimeException){
        return ResponseEntity.badRequest().body(unitsTimeException.getMessage());
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity DateTimeParseException(DateTimeParseException dateTimeParseException){
        return ResponseEntity.badRequest().body("El formato de fecha debe ser (AAAA-MM-DD)");
    }

    @ExceptionHandler(ConectionStorageException.class)
    public ResponseEntity conectionStorageException(ConectionStorageException conectionStorageException){
        return ResponseEntity.internalServerError().body(conectionStorageException.getMessage());
    }

    @ExceptionHandler(UploadFileException.class)
    public ResponseEntity uploadFileException(UploadFileException uploadFileException){
        return ResponseEntity.internalServerError().body(uploadFileException.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity badRequest(MethodArgumentNotValidException methodArgumentNotValidException){
        Map<String, String> errorMap = new HashMap<>();
        methodArgumentNotValidException.getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errorMap);
    }

    @ExceptionHandler(CameraNullExecption.class)
    public ResponseEntity CameraNullExecption(CameraNullExecption cameraNullExecption){
        return ResponseEntity.badRequest().body(cameraNullExecption.getMessage());
    }
}
