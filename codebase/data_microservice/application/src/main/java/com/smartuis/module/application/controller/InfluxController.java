package com.smartuis.module.application.controller;

import com.smartuis.module.application.mapper.MessageMapper;
import com.smartuis.module.domain.entity.*;
import com.smartuis.module.persistence.repository.InfluxRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/influx")
public class InfluxController {

    private static final Logger log = LoggerFactory.getLogger(InfluxController.class);

    private final InfluxRepository influxRepository;
    private final MessageMapper messageMapper;

    public InfluxController(InfluxRepository influxRepository, MessageMapper messageMapper) {
        this.influxRepository = influxRepository;
        this.messageMapper = messageMapper;
    }

    @Operation(summary = "Obtener las últimas mediciones")
    @GetMapping("/measurement/{measurement}/last")
    public ResponseTemporaryQuery getLastMeasurements(
            @PathVariable String measurement,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /influx/measurement/{}/last - limit: {}", measurement, limit);
        List<Message> messages = influxRepository.findLastMeasurements(measurement, limit);
        Instant start = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        Instant end   = messages.get(0).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        log.debug("Retornando {} mediciones para '{}' entre [{}, {}]", dataDTOs.size(), measurement, start, end);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(start, end);
        response.setData(dataDTOs);
        return response;
    }

    @Operation(summary = "Obtener mediciones por rango de tiempo y métrica")
    @GetMapping("/by-time-range/measurement/{measurement}")
    public ResponseTemporaryQuery getMeasurementsByTimeRangeMeasurement(
            @PathVariable String measurement,
            @RequestParam String start,
            @RequestParam String end) {
        log.info("GET /influx/by-time-range/measurement/{} - start: {}, end: {}", measurement, start, end);
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate   = Instant.parse(end   + "T23:59:59Z");
        List<Message> messages = influxRepository.findMeasurementsByTimeRange(measurement, startDate, endDate);
        startDate = messages.get(0).getHeader().getTimeStamp();
        endDate   = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        log.debug("Retornando {} mediciones para '{}'", dataDTOs.size(), measurement);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(startDate, endDate);
        response.setData(dataDTOs);
        return response;
    }

    @Operation(summary = "Obtener mediciones por rango de tiempo")
    @GetMapping("/by-time-range")
    public ResponseTemporaryQuery getMeasurementsByTimeRange(
            @RequestParam String start,
            @RequestParam String end) {
        log.info("GET /influx/by-time-range - start: {}, end: {}", start, end);
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate   = Instant.parse(end   + "T23:59:59Z");
        List<Message> messages = influxRepository.findMessagesBetweenTwoDate(startDate, endDate);
        Instant startResponse = messages.get(0).getHeader().getTimeStamp();
        Instant endResponse   = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        log.debug("Retornando {} mediciones en rango [{}, {}]", dataDTOs.size(), startResponse, endResponse);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(startResponse, endResponse);
        response.setData(dataDTOs);
        return response;
    }

    @Operation(summary = "Obtener mensajes en unidades de tiempo")
    @GetMapping("/date/units/{time}")
    public ResponseTemporaryQuery getMessagesInUnitsTime(@PathVariable String time) {
        log.info("GET /influx/date/units/{}", time);
        List<Message> messages = influxRepository.findMessagesInUnitsTime(time);
        if (messages.size() > 0) {
            Instant start = messages.get(0).getHeader().getTimeStamp();
            Instant end   = messages.get(messages.size() - 1).getHeader().getTimeStamp();
            List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
            log.debug("Retornando {} mensajes para el período '{}'", dataDTOs.size(), time);
            ResponseTemporaryQuery response = new ResponseTemporaryQuery(start, end);
            response.setData(dataDTOs);
            return response;
        }
        log.warn("No se encontraron mensajes para el período '{}'", time);
        return null;
    }

    @Operation(summary = "Calcular el promedio de una métrica")
    @GetMapping("/measurement/{measurement}/average")
    public Optional<Double> getMeasurementAverage(
            @PathVariable String measurement,
            @RequestParam String start,
            @RequestParam String end) {
        log.info("GET /influx/measurement/{}/average - start: {}, end: {}", measurement, start, end);
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate   = Instant.parse(end   + "T23:59:59Z");
        Optional<Double> average = influxRepository.findAverageValue(measurement, startDate, endDate);
        log.debug("Promedio para '{}': {}", measurement, average.orElse(null));
        return average;
    }

    @Operation(summary = "Obtener el valor mínimo de una métrica")
    @GetMapping("/measurement/{measurement}/min")
    public Optional<Double> getMinimum(
            @PathVariable String measurement,
            @RequestParam String start,
            @RequestParam String end) {
        log.info("GET /influx/measurement/{}/min - start: {}, end: {}", measurement, start, end);
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate   = Instant.parse(end   + "T23:59:59Z");
        Optional<Double> min = influxRepository.findMinValue(measurement, startDate, endDate);
        log.debug("Mínimo para '{}': {}", measurement, min.orElse(null));
        return min;
    }

    @Operation(summary = "Obtener el valor máximo de una métrica")
    @GetMapping("/measurement/{measurement}/max")
    public Optional<Double> getMaximum(
            @PathVariable String measurement,
            @RequestParam String start,
            @RequestParam String end) {
        log.info("GET /influx/measurement/{}/max - start: {}, end: {}", measurement, start, end);
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate   = Instant.parse(end   + "T23:59:59Z");
        Optional<Double> max = influxRepository.findMaxValue(measurement, startDate, endDate);
        log.debug("Máximo para '{}': {}", measurement, max.orElse(null));
        return max;
    }
}