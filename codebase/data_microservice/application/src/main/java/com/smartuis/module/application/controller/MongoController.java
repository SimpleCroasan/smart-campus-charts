package com.smartuis.module.application.controller;

import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.persistence.repository.MongoRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/mongo")
public class MongoController {

    private static final Logger log = LoggerFactory.getLogger(MongoController.class);

    private final MongoRepository messageRepository;

    public MongoController(MongoRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Operation(summary = "Buscar mensajes por ID de dispositivo")
    @GetMapping("/deviceId/{deviceId}")
    public ResponseEntity<List<Message>> findMessagesByDeviceId(@PathVariable String deviceId) {
        log.info("GET /mongo/deviceId/{}", deviceId);
        return ResponseEntity.ok(messageRepository.findMessagesByDeviceId(deviceId));
    }

    @Operation(summary = "Buscar mensajes por ubicación")
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Message>> findMessagesByLocation(@PathVariable String location) {
        log.info("GET /mongo/location/{}", location);
        return ResponseEntity.ok(messageRepository.findMessagesByLocation(location));
    }

    @Operation(summary = "Buscar mensajes por rango de fechas")
    @GetMapping("/by-time-range")
    public ResponseEntity<List<Message>> findMessagesByDateRange(
            @RequestParam String start, @RequestParam String end) {
        log.info("GET /mongo/by-time-range - start: {}, end: {}", start, end);
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate   = Instant.parse(end   + "T23:59:59Z");
        return ResponseEntity.ok(messageRepository.findMessagesBetweenTwoDate(startDate, endDate));
    }

    @Operation(summary = "Buscar mensajes en unidades de tiempo")
    @GetMapping("/date/units/{time}")
    public ResponseEntity<List<Message>> findMessageInUnitsTime(@PathVariable String time) {
        log.info("GET /mongo/date/units/{}", time);
        return ResponseEntity.ok(messageRepository.findMessagesInUnitsTime(time));
    }

    @Operation(summary = "Obtener las últimas mediciones")
    @GetMapping("/measurement/last")
    public ResponseEntity<List<Message>> findLastMeasurements(
            @RequestParam String measurement,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        log.info("GET /mongo/measurement/last - measurement: {}, limit: {}", measurement, limit);
        return ResponseEntity.ok(messageRepository.findLastMeasurements(measurement, limit));
    }

    @Operation(summary = "Buscar mediciones por rango de tiempo")
    @GetMapping("/measurement/by-time-range")
    public ResponseEntity<List<Message>> findMeasurementsByTimeRange(
            @RequestParam String measurement,
            @RequestParam String start,
            @RequestParam String end) {
        log.info("GET /mongo/measurement/by-time-range - measurement: {}, start: {}, end: {}", measurement, start, end);
        Instant fromDate = Instant.parse(start + "T00:00:00Z");
        Instant toDate   = Instant.parse(end   + "T23:59:59Z");
        return ResponseEntity.ok(messageRepository.findMeasurementsByTimeRange(measurement, fromDate, toDate));
    }
}