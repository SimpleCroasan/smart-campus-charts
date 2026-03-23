package com.smartuis.module.persistence.repository;

import com.smartuis.module.domain.entity.Message;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface IMessageRepository extends MongoRepository<Message, String>{

    @Query("{'headers.deviceId': ?0}")
    List<Message> findMessagesByDeviceId(String deviceId);

    @Query("{'headers.location': ?0}")
    List<Message> findMessagesByLocation(String location);

    @Query("{'headers.timeStamp': {$gte: ?0, $lt: ?1}}")
    List<Message> findMessagesBetweenTwoDate(Instant from, Instant to);

    @Aggregation(pipeline = {
            "{ '$match' : { 'metrics': { '$elemMatch': { 'measurement':  { '$regex': ?0, '$options': 'i' } } } } }",
            "{ '$limit' : ?1 }"
    })
    List<Message> findLastMeasurements(String metric, Integer limit);

    @Query("{'headers.timeStamp': {$gte: ?1, $lt: ?2}, 'metrics.measurement': { '$regex': ?0, '$options': 'i' }}")
    List<Message> findMeasurementsByTimeRange(String measurement, Instant start, Instant end);

}
