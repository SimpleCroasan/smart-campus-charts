package com.smartuis.module.domain.repository;

import com.smartuis.module.domain.entity.Message;

import java.util.List;

public interface FilterQuery {

    List<Message> findMessagesByDeviceId(String deviceId);
    List<Message>  findMessagesByLocation(String location);
}
