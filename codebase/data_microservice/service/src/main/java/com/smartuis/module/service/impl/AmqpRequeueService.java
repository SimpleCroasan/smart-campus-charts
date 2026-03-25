package com.smartuis.module.service.impl;

import com.smartuis.module.domain.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AmqpRequeueService {

    private static final Logger log = LoggerFactory.getLogger(AmqpRequeueService.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin    rabbitAdmin;
    private Queue anonymousQueue;

    public AmqpRequeueService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin    = new RabbitAdmin(rabbitTemplate.getConnectionFactory());
        this.anonymousQueue = new AnonymousQueue();
    }

    public void requeue(Message message) {
        String targetExchange = message.getHeader().getTopic();
        log.info("Re-encolando mensaje vía AMQP. Exchange: {}, DeviceId: {}",
                targetExchange, message.getHeader().getDeviceId());
        try {
            FanoutExchange fanoutExchange = new FanoutExchange(targetExchange);
            rabbitAdmin.declareQueue(anonymousQueue);
            rabbitAdmin.declareExchange(fanoutExchange);
            rabbitAdmin.declareBinding(BindingBuilder.bind(anonymousQueue).to(fanoutExchange));
            rabbitTemplate.convertAndSend(targetExchange, "", message);
            log.debug("Mensaje re-encolado exitosamente en exchange: {}", targetExchange);
        } catch (Exception e) {
            log.error("Error re-encolando mensaje AMQP en exchange {}: {}", targetExchange, e.getMessage(), e);
        }
    }
}