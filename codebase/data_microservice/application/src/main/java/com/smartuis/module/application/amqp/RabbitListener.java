package com.smartuis.module.application.amqp;

import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.domain.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RabbitListener {

    private static final Logger log = LoggerFactory.getLogger(RabbitListener.class);

    private final List<MessageRepository> messageRepository;

    public RabbitListener(List<MessageRepository> messageRepository) {
        this.messageRepository = messageRepository;
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = "#{anonQueue.name}")
    public void receiveMessage(Message message) {
        log.debug("Mensaje recibido vía RabbitMQ. Topic: {}, DeviceId: {}",
                message.getHeader() != null ? message.getHeader().getTopic() : "N/A",
                message.getHeader() != null ? message.getHeader().getDeviceId() : "N/A");
        try {
            messageRepository.forEach(repo -> repo.write(message));
            log.debug("Mensaje RabbitMQ persistido correctamente");
        } catch (Exception e) {
            log.error("Error persistiendo mensaje RabbitMQ: {}", e.getMessage(), e);
        }
    }
}