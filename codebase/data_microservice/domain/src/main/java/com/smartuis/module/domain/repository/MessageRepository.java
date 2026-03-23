package com.smartuis.module.domain.repository;

import com.smartuis.module.domain.entity.Message;

public interface MessageRepository {
    Message write(Message message);
}
