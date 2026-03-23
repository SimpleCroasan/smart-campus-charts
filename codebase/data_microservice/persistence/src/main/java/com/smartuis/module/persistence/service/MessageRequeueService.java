package com.smartuis.module.persistence.service;

import com.smartuis.module.service.impl.AmqpRequeueService;
import com.smartuis.module.service.impl.MqttRequeueService;
import com.smartuis.module.domain.entity.Application;
import com.smartuis.module.domain.entity.Device;
import com.smartuis.module.domain.entity.Header;
import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.persistence.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageRequeueService {

    private MqttRequeueService mqttRequeueService;
    private DeviceRepository deviceRepository;
    private AmqpRequeueService amqpRequeueService;

    public MessageRequeueService(MqttRequeueService mqttRequeueService,
                                 AmqpRequeueService amqpRequeueService,
                                 DeviceRepository deviceRepository) {
        this.mqttRequeueService = mqttRequeueService;
        this.amqpRequeueService = amqpRequeueService;
        this.deviceRepository = deviceRepository;
    }

    public void requeueMessage(Message message) {
        String deviceId = message.getHeader().getDeviceId();
        Device deviceOpt = deviceRepository.findDeviceByDeviceId(deviceId).orElse(null);

        if(deviceOpt == null){
            return;
        }

        List<Application> applications = deviceOpt.getApplications();

        for(Application application : applications){
            if(message.getHeader().getTopic().equals(application.getName())){
                Header header = (Header) message.getHeader().clone();
                Message  messageRequeue = new Message();
                messageRequeue.setHeader(header);
                messageRequeue.setMetrics(message.getMetrics());

                String newTopic = messageRequeue.getHeader().getTopic() + "/" + application.getApplicationId();
                messageRequeue.getHeader().setTopic(newTopic);
                mqttRequeueService.requeue(messageRequeue);
                amqpRequeueService.requeue(messageRequeue);
            }
        }
    }
}


