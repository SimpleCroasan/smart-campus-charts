package com.smartuis.module.application.mapper;

import com.smartuis.module.domain.entity.DataDTO;
import com.smartuis.module.domain.entity.Header;
import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.domain.entity.Metric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(source = "header.location", target= "location")
    @Mapping(source = "metric.measurement", target= "measurement")
    @Mapping(source = "metric.value", target= "value")
    @Mapping(source = "header.timeStamp", target= "time")
    DataDTO toDataDTO(Header header, Metric metric);

    default List<DataDTO> mapMessagesToDataDTOs(List<Message> messages) {
        List<DataDTO> dataDTOs = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                Header header = message.getHeader();
                if (message.getMetrics() != null) {
                    for (Metric metric : message.getMetrics()) {
                        dataDTOs.add(toDataDTO(header, metric));
                    }
                }
            }
        }
        return dataDTOs;
    }
}
