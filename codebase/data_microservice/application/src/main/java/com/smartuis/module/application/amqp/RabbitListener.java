package com.smartuis.module.application.amqp;


import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.domain.repository.MessageRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RabbitListener {

    List<MessageRepository> messageRepository;

    public RabbitListener(List<MessageRepository>  messageRepository) {
        this.messageRepository = messageRepository;
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = "#{anonQueue.name}")
    public void receiveMessage(Message message){
        System.out.println(message.toString());
        messageRepository.forEach(repo->repo.write(message));
    }

}
