package com.smartuis.module.application.controller;

import com.smartuis.module.application.mapper.MessageMapper;
import com.smartuis.module.domain.entity.*;
import com.smartuis.module.persistence.repository.InfluxRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/influx")
public class InfluxController {

    private InfluxRepository influxRepository;
    private MessageMapper messageMapper;

    public InfluxController(InfluxRepository influxRepository, MessageMapper messageMapper) {
        this.influxRepository = influxRepository;
        this.messageMapper = messageMapper;
    }

    @Operation(summary = "Obtener las últimas mediciones",
            description = "Recupera las últimas mediciones de una métrica específica. " +
                    "Parámetros:\n" +
                    "- measurement: Nombre de la métrica a consultar.\n" +
                    "- limit: Número máximo de mediciones a recuperar (opcional, valor por defecto: 10).")
    @GetMapping("/measurement/{measurement}/last")
    public ResponseTemporaryQuery getLastMeasurements(
            @PathVariable String measurement,
            @RequestParam(defaultValue = "10") int limit) {
        List<Message> messages = influxRepository.findLastMeasurements(measurement, limit);
        Instant start = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        Instant end = messages.get(0).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(start, end);
        response.setData(dataDTOs);
        return response;
    }

    @Operation(summary = "Obtener mediciones por rango de tiempo y métrica",
            description = "Recupera las mediciones de una métrica específica dentro de un rango de tiempo determinado. " +
                    "Parámetros:\n" +
                    "- measurement: Nombre de la métrica a consultar\n" +
                    "- start: Fecha de inicio en formato YYYY-MM-DD\n" +
                    "- end: Fecha de fin en formato YYYY-MM-DD.")
    @GetMapping("/by-time-range/measurement/{measurement}")
    public ResponseTemporaryQuery getMeasurementsByTimeRangeMeasurement(
            @PathVariable String measurement,
            @RequestParam String start,
            @RequestParam String end){
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate = Instant.parse(end + "T23:59:59Z");
        List<Message> messages = influxRepository.findMeasurementsByTimeRange(measurement, startDate, endDate);
        startDate = messages.get(0).getHeader().getTimeStamp();
        endDate = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(startDate, endDate);
        response.setData(dataDTOs);
        return response;
    }

    @Operation(summary = "Obtener mediciones por rango de tiempo",
            description = "Recupera todas las mediciones dentro de un rango de tiempo especificado. " +
                    "Parámetros:\n" +
                    "- start: Fecha de inicio en formato YYYY-MM-DD\n" +
                    "- end: Fecha de fin en formato YYYY-MM-DD")
    @GetMapping("/by-time-range")
    public ResponseTemporaryQuery getMeasurementsByTimeRange(
            @RequestParam String start,
            @RequestParam String end){
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate = Instant.parse(end + "T23:59:59Z");
        List<Message> messages = influxRepository.findMessagesBetweenTwoDate(startDate, endDate);
        Instant startResponse = messages.get(0).getHeader().getTimeStamp();
        Instant endResponse = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(startResponse, endResponse);
        response.setData(dataDTOs);
        return response;
    }

    @Operation(summary = "Obtener mensajes en unidades de tiempo",
            description = "Recupera mensajes agrupados por una unidad de tiempo específica. " +
                    "Parámetros:\n" +
                    "- time: Unidad de tiempo para agrupar los mensajes. (ejemplo '1h' para una hora o '30m' para treinta minutos)")
    @GetMapping("/date/units/{time}")
    public ResponseTemporaryQuery getMessagesInUnitsTime(
            @PathVariable String time){
        List<Message> messages = influxRepository.findMessagesInUnitsTime(time);
        if(messages.size() > 0){
            Instant start = messages.get(0).getHeader().getTimeStamp();
            Instant end = messages.get(messages.size() - 1).getHeader().getTimeStamp();
            List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
            ResponseTemporaryQuery response = new ResponseTemporaryQuery(start, end);
            response.setData(dataDTOs);
            return response;
        }

        return null;
    }

    @Operation(summary = "Calcular el promedio de una métrica",
            description = "Calcula el valor promedio de una métrica específica dentro de un rango de tiempo dado. " +
                    "Parámetros:\n" +
                    "- measurement: Nombre de la métrica\n" +
                    "- start: Fecha de inicio en formato YYYY-MM-DD\n" +
                    "- end: Fecha de fin en formato YYYY-MM-DD")
    @GetMapping("/measurement/{measurement}/average")
    public Optional<Double> getMeasurementAverage(
            @PathVariable String measurement,
            @RequestParam String start,
            @RequestParam String end){
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate = Instant.parse(end + "T23:59:59Z");
        Optional<Double> average = influxRepository.findAverageValue(measurement, startDate, endDate);
        return average;
    }

    @Operation(summary = "Obtener el valor mínimo de una métrica",
            description = "Recupera el valor mínimo registrado de una métrica específica dentro de un rango de tiempo. " +
                    "Parámetros:\n" +
                    "- measurement: Nombre de la métrica\n" +
                    "- start: Fecha de inicio en formato YYYY-MM-DD\n" +
                    "- end: Fecha de fin en formato YYYY-MM-DD")
    @GetMapping("/measurement/{measurement}/min")
    public Optional<Double> getMinimum(
            @PathVariable String measurement,
            @RequestParam String start,
            @RequestParam String end) {
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate = Instant.parse(end + "T23:59:59Z");
        Optional<Double> min = influxRepository.findMinValue(measurement, startDate, endDate);
        return min;
    }

    @Operation(
            summary = "Obtener el valor máximo de una métrica",
            description = "Recupera el valor máximo registrado de una métrica específica dentro de un rango de tiempo determinado. " +
                    "Parámetros:\n" +
                    "- measurement: Nombre de la métrica a consultar\n" +
                    "- start: Fecha de inicio en formato YYYY-MM-DD\n" +
                    "- end: Fecha de fin en formato YYYY-MM-DD"
    )
    @GetMapping("/measurement/{measurement}/max")
    public Optional<Double> getMaximum(
            @PathVariable String measurement,
            @RequestParam String start,
            @RequestParam String end) {
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate = Instant.parse(end + "T23:59:59Z");
        Optional<Double> max = influxRepository.findMaxValue(measurement, startDate, endDate);
        return max;
    }
}