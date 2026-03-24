package com.smartuis.module.application.controller;

import com.smartuis.module.application.exceptions.CameraNullExecption;
import com.smartuis.module.application.exceptions.ConectionStorageException;
import com.smartuis.module.persistence.exceptions.UnitsTimeException;
import com.smartuis.module.persistence.exceptions.UploadFileException;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerException {

    private static final Logger log = LoggerFactory.getLogger(ControllerException.class);

    @ExceptionHandler(UnitsTimeException.class)
    public ResponseEntity handlerUnitsTimeException(UnitsTimeException unitsTimeException) {
        log.warn("UnitsTimeException: {}", unitsTimeException.getMessage());
        return ResponseEntity.badRequest().body(unitsTimeException.getMessage());
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity DateTimeParseException(DateTimeParseException dateTimeParseException) {
        log.warn("DateTimeParseException: {}", dateTimeParseException.getMessage());
        return ResponseEntity.badRequest().body("El formato de fecha debe ser (AAAA-MM-DD)");
    }

    @ExceptionHandler(ConectionStorageException.class)
    public ResponseEntity conectionStorageException(ConectionStorageException conectionStorageException) {
        log.error("ConectionStorageException: {}", conectionStorageException.getMessage(), conectionStorageException);
        return ResponseEntity.internalServerError().body(conectionStorageException.getMessage());
    }

    @ExceptionHandler(UploadFileException.class)
    public ResponseEntity uploadFileException(UploadFileException uploadFileException) {
        log.error("UploadFileException: {}", uploadFileException.getMessage(), uploadFileException);
        return ResponseEntity.internalServerError().body(uploadFileException.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity badRequest(MethodArgumentNotValidException methodArgumentNotValidException) {
        Map<String, String> errorMap = new HashMap<>();
        methodArgumentNotValidException.getFieldErrors().forEach(error ->
                errorMap.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validación fallida - campos con error: {}", errorMap.keySet());
        return ResponseEntity.badRequest().body(errorMap);
    }

    @ExceptionHandler(CameraNullExecption.class)
    public ResponseEntity CameraNullExecption(CameraNullExecption cameraNullExecption) {
        log.warn("CameraNullExecption: {}", cameraNullExecption.getMessage());
        return ResponseEntity.badRequest().body(cameraNullExecption.getMessage());
    }
}