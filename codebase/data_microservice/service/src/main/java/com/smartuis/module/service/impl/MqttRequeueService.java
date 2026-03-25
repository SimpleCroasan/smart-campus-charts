package com.smartuis.module.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartuis.module.domain.entity.Message;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MqttRequeueService {

    private static final Logger log = LoggerFactory.getLogger(MqttRequeueService.class);

    @Value("${mqtt.broker.url}")
    private String BROKER_URL;

    private final ObjectMapper objectMapper;

    public MqttRequeueService() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void requeue(Message message) {
        String targetTopic = message.getHeader().getTopic();
        log.info("Re-encolando mensaje vía MQTT. Topic: {}, DeviceId: {}",
                targetTopic, message.getHeader().getDeviceId());
        try {
            MqttClient mqttClient = new MqttClient(BROKER_URL, UUID.randomUUID().toString());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect();
            String payload = objectMapper.writeValueAsString(message);
            MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
            mqttMessage.setQos(1);
            mqttClient.publish(targetTopic, mqttMessage);
            mqttClient.disconnect();
            log.debug("Mensaje re-encolado exitosamente en topic MQTT: {}", targetTopic);
        } catch (MqttException e) {
            log.error("Error MQTT en topic {}: {} (código: {})",
                    targetTopic, e.getMessage(), e.getReasonCode(), e);
        } catch (Exception e) {
            log.error("Error inesperado re-encolando MQTT en topic {}: {}",
                    targetTopic, e.getMessage(), e);
        }
    }
}