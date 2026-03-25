package com.smartuis.module.application.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.domain.repository.MessageRepository;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmqxListener implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(EmqxListener.class);

    private final List<MessageRepository> messageRepository;
    private final ObjectMapper objectMapper;
    private MqttClient client;

    public EmqxListener(List<MessageRepository> messageRepository) {
        this.messageRepository = messageRepository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void setClient(MqttClient client) {
        this.client = client;
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        log.debug("Mensaje recibido en topic MQTT: {}", topic);
        try {
            String payload = new String(mqttMessage.getPayload());
            Message message = objectMapper.readValue(payload, Message.class);
            messageRepository.forEach(repo -> repo.write(message));
            log.debug("Mensaje procesado y persistido desde topic: {}", topic);
        } catch (Exception e) {
            log.error("Error procesando mensaje MQTT en topic {}: {}", topic, e.getMessage(), e);
        }
    }

    @Override
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
        log.warn("Desconectado del broker MQTT. Razón: {}",
                mqttDisconnectResponse.getReasonString() != null
                        ? mqttDisconnectResponse.getReasonString()
                        : "desconocida");
    }

    @Override
    public void mqttErrorOccurred(MqttException e) {
        log.error("Error MQTT: {} (código: {})", e.getMessage(), e.getReasonCode(), e);
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        log.debug("Entrega MQTT completada. Topics: {}",
                token.getTopics() != null ? String.join(", ", token.getTopics()) : "N/A");
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) log.info("Reconexión exitosa al broker MQTT: {}", serverURI);
        else           log.info("Conexión establecida con broker MQTT: {}", serverURI);
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        log.debug("Paquete de autenticación MQTT recibido. Código: {}", reasonCode);
    }
}