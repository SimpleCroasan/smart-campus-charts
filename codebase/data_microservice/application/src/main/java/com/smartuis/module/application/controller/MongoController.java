package com.smartuis.module.application.controller;

import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.persistence.repository.MongoRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/mongo")
public class MongoController {

    private MongoRepository messageRepository;

    public MongoController(MongoRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Operation(
            summary = "Buscar mensajes por ID de dispositivo",
            description = "Recupera una lista de mensajes asociados a un ID de dispositivo específico.\n  " +
                    "- El ID del dispositivo debe proporcionarse como un parámetro en la ruta."
    )
    @GetMapping("/deviceId/{deviceId}")
    public ResponseEntity<List<Message>> findMessagesByDeviceId(@PathVariable String deviceId){
        System.out.println(deviceId);
        List<Message> messages = messageRepository.findMessagesByDeviceId(deviceId);
        return ResponseEntity.ok(messages);
    }

    @Operation(
            summary = "Buscar mensajes por ubicación",
            description = "Recupera una lista de mensajes asociados a una ubicación específica. \n" +
                    "- La ubicación debe proporcionarse como un parámetro en la ruta."
    )
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Message>> findMessagesByLocation(@PathVariable String location){
        System.out.println(location);
        List<Message> messages = messageRepository.findMessagesByLocation(location);
        return ResponseEntity.ok(messages);
    }

    @Operation(
            summary = "Buscar mensajes por rango de fechas",
            description = "Recupera una lista de mensajes entre dos fechas especificadas. \n" +
                    " - Las fechas de inicio y fin deben proporcionarse como parámetros de consulta en formato 'YYYY-MM-DD'."
    )
    @GetMapping("/by-time-range")
    public ResponseEntity<List<Message>> findMessagesByDateRange(@RequestParam String start, @RequestParam String end){
        Instant startDate = Instant.parse(start + "T00:00:00Z");
        Instant endDate = Instant.parse(end + "T23:59:59Z");
        List<Message> messages = messageRepository.findMessagesBetweenTwoDate(startDate, endDate);
        return ResponseEntity.ok(messages);
    }

    @Operation(
            summary = "Buscar mensajes en unidades de tiempo específicas",
            description = "Recupera una lista de mensajes dentro de un período de tiempo específico.\n " +
                    "- El período de tiempo debe proporcionarse como un parámetro en la ruta, por ejemplo, '1h' para una hora o '30m' para treinta minutos." +
                    "- Acepta valores en hora (h), minutos (m) y segundos (s)"
    )
    @GetMapping("/date/units/{time}")
    public ResponseEntity<List<Message>> findMessageInUnitsTime(@PathVariable String time) {
        return ResponseEntity.ok(messageRepository.findMessagesInUnitsTime(time));
    }

    @Operation(
            summary = "Obtener las últimas mediciones",
            description = "Recupera una lista de las últimas mediciones de un tipo específico.\n" +
                    "- El tipo de medición se proporciona como un parámetro de consulta llamado 'measurement'. " +
                    "- Opcionalmente, se puede especificar el número máximo de resultados a devolver mediante el parámetro 'limit', que por defecto es 20."
    )
    @GetMapping("/measurement/last")
    public ResponseEntity<List<Message>> findLastMeasurements(@RequestParam String measurement, @RequestParam(required = false, defaultValue = "20") Integer limit){
        List<Message> messages = messageRepository.findLastMeasurements(measurement, limit);
        return ResponseEntity.ok(messages);
    }

    @Operation(
            summary = "Buscar mediciones por rango de tiempo",
            description = "Recupera una lista de mediciones de un tipo específico dentro de un rango de fechas. \n" +
                    "- El tipo de medición se proporciona como un parámetro de consulta llamado 'measurement'. " +
                    "- Las fechas de inicio y fin deben proporcionarse como parámetros de consulta llamados 'start' y 'end', respectivamente, en formato 'YYYY-MM-DD'."
    )
    @GetMapping("/measurement/by-time-range")
    public ResponseEntity<List<Message>> findMeasurementsByTimeRange(@RequestParam String measurement, @RequestParam String start, @RequestParam String end){
        Instant fromDate = Instant.parse(start + "T00:00:00Z");
        Instant toDate = Instant.parse(end + "T23:59:59Z");
        List<Message> messages = messageRepository.findMeasurementsByTimeRange(measurement, fromDate, toDate);
        return ResponseEntity.ok(messages);
    }

}
