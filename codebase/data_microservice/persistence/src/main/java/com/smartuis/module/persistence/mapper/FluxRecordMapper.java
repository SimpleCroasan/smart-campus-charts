package com.smartuis.module.persistence.mapper;

import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.smartuis.module.domain.entity.Header;
import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.domain.entity.Metric;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FluxRecordMapper {

    public List<Message> mapFluxTablesToMessages(List<FluxTable> tables) {
        List<Message> messages = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String location = (String) record.getValues().get("location");
                String measurement = (String) record.getValues().get("_measurement");
                Double value = (Double) record.getValues().get("_value");
                Instant time = (Instant) record.getValues().get("_time");

                Header header = new Header(null, null, time, location, null, null);
                Metric metric = new Metric(measurement, value);

                messages.add(new Message(header, List.of(metric)));
            }
        }
        return messages;
    }


}
