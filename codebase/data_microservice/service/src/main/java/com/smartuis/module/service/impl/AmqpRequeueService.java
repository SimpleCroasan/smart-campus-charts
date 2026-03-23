package com.smartuis.module.service.impl;

import com.smartuis.module.domain.entity.Message;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AmqpRequeueService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private Queue anonymousQueue;

    public AmqpRequeueService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());
        this.anonymousQueue = new AnonymousQueue();
    }

    public void requeue(Message message) {
        try {
            String targetExchange = message.getHeader().getTopic();
            FanoutExchange fanoutExchange = new FanoutExchange(targetExchange);
            rabbitAdmin.declareQueue(anonymousQueue);
            rabbitAdmin.declareExchange(fanoutExchange);
            rabbitAdmin.declareBinding(BindingBuilder.bind(anonymousQueue).to(fanoutExchange));
            rabbitTemplate.convertAndSend(targetExchange, "", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
