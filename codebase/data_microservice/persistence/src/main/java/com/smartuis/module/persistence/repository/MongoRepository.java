package com.smartuis.module.persistence.repository;

import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.domain.repository.FilterQuery;
import com.smartuis.module.domain.repository.TemporaryQuery;
import com.smartuis.module.persistence.exceptions.UnitsTimeException;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class MongoRepository implements com.smartuis.module.domain.repository.MessageRepository, FilterQuery, TemporaryQuery {

    private IMessageRepository iMessageRepository;

    public MongoRepository(IMessageRepository iMessageRepository) {
        this.iMessageRepository = iMessageRepository;
    }


    @Override
    public Message write(Message message) {
        return iMessageRepository.save(message);
    }

    @Override
    public List<Message> findMessagesByDeviceId(String deviceId) {
        return iMessageRepository.findMessagesByDeviceId(deviceId);
    }

    @Override
    public List<Message> findMessagesByLocation(String location) {
        return iMessageRepository.findMessagesByLocation(location);
    }

    @Override
    public List<Message> findMessagesBetweenTwoDate(Instant from, Instant to) {
        return iMessageRepository.findMessagesBetweenTwoDate(from, to);
    }

    @Override
    public List<Message> findMessagesInUnitsTime(String time) {
        var pattern = Pattern.compile("^(\\d+)(m|s|h)$");
        var matcher = pattern.matcher(time);
        System.out.println(time);
        System.out.println(matcher.matches());
        if(!matcher.matches()){
            throw new UnitsTimeException("El patron de unidad de tiempo de ser ^(\\d+)(m|s|h)$\n");
        }

        var number = Integer.parseInt(matcher.group(1));
        var unit = matcher.group(2).toLowerCase();

        var nowDate = Instant.now();
        Instant fromDate;
        System.out.println(time);
        System.out.println(nowDate);

        switch (unit){
            case "s":
                fromDate =  nowDate.minus(Duration.ofSeconds(number));
                break;
            case "m":
                fromDate =  nowDate.minus(Duration.ofMinutes(number));
                break;
            case "h":
                fromDate =  nowDate.minus(Duration.ofHours(number));
                break;
            default:
                throw new UnitsTimeException("No existe esa unidad de tiempo");
        };


        return iMessageRepository.findMessagesBetweenTwoDate(fromDate, nowDate);
    }

    @Override
    public List<Message> findLastMeasurements(String measurement, int limit) {
        return iMessageRepository.findLastMeasurements(measurement, limit);
    }


    @Override
    public List<Message> findMeasurementsByTimeRange(String measurement, Instant start, Instant end) {
        return iMessageRepository.findMeasurementsByTimeRange(measurement, start, end);
    }




}
