package com.smartuis.module.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartuis.module.domain.entity.Message;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MqttRequeueService {
    @Value("${mqtt.broker.url}")
    private String BROKER_URL;
    private final ObjectMapper objectMapper;

    public MqttRequeueService() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void requeue(Message message) {
        try {
            // Se crea un cliente MQTT con un ID único para el reencolamiento
            MqttClient mqttClient = new MqttClient(BROKER_URL, UUID.randomUUID().toString());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect();

            // Se obtiene el topic destino desde el header del mensaje
            String targetTopic = message.getHeader().getTopic();
            String payload = objectMapper.writeValueAsString(message);
            MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
            mqttMessage.setQos(1);

            mqttClient.publish(targetTopic, mqttMessage);
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
