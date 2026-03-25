package com.smartuis.module.persistence.service;

import com.smartuis.module.service.impl.AmqpRequeueService;
import com.smartuis.module.service.impl.MqttRequeueService;
import com.smartuis.module.domain.entity.Application;
import com.smartuis.module.domain.entity.Device;
import com.smartuis.module.domain.entity.Header;
import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.persistence.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageRequeueService {

    private static final Logger log = LoggerFactory.getLogger(MessageRequeueService.class);

    private final MqttRequeueService mqttRequeueService;
    private final DeviceRepository deviceRepository;
    private final AmqpRequeueService amqpRequeueService;

    public MessageRequeueService(MqttRequeueService mqttRequeueService,
                                 AmqpRequeueService amqpRequeueService,
                                 DeviceRepository deviceRepository) {
        this.mqttRequeueService = mqttRequeueService;
        this.amqpRequeueService = amqpRequeueService;
        this.deviceRepository = deviceRepository;
    }

    public void requeueMessage(Message message) {
        String deviceId = message.getHeader().getDeviceId();
        log.debug("Iniciando reencolar para deviceId: {}, topic: {}",
                deviceId, message.getHeader().getTopic());

        Device device = deviceRepository.findDeviceByDeviceId(deviceId).orElse(null);
        if (device == null) {
            log.warn("No se encontró dispositivo para reencolar. DeviceId: {}", deviceId);
            return;
        }

        List<Application> applications = device.getApplications();
        int reencolados = 0;
        for (Application application : applications) {
            if (message.getHeader().getTopic().equals(application.getName())) {
                Header header = (Header) message.getHeader().clone();
                Message messageRequeue = new Message();
                messageRequeue.setHeader(header);
                messageRequeue.setMetrics(message.getMetrics());
                String newTopic = messageRequeue.getHeader().getTopic() + "/" + application.getApplicationId();
                messageRequeue.getHeader().setTopic(newTopic);
                log.debug("Reencolando al topic '{}' para applicationId: {}",
                        newTopic, application.getApplicationId());
                mqttRequeueService.requeue(messageRequeue);
                amqpRequeueService.requeue(messageRequeue);
                reencolados++;
            }
        }
        log.debug("Reencole completado para deviceId: {} — {} aplicaciones notificadas", deviceId, reencolados);
    }
}