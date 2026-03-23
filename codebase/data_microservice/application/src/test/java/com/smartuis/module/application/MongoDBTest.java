package com.smartuis.module.application;

import com.smartuis.module.domain.entity.Header;
import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.domain.entity.Metric;
import com.smartuis.module.persistence.exceptions.UnitsTimeException;
import com.smartuis.module.persistence.repository.MongoRepository;
import com.smartuis.module.persistence.repository.IMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MongoDBTest {
    @Mock
    private IMessageRepository iMessageRepository;

    @InjectMocks
    private MongoRepository messageRepository;

    private List<Message> messages;

    @BeforeEach
    public void setUp() {
        Header header = new Header("25418", "4545", Instant.now(), "CENTIC", "Ambiente", true);
        List<Metric> oneMetrics = List.of(new Metric("Temperature", 20.0),
                new Metric("CO2", 10.0));
        List<Metric> twoMetrics = List.of(new Metric("Temperature", 22.0),
                new Metric("CO2", 8.0));

        Message oneMessage = new Message(header, oneMetrics);
        Message twoMessage = new Message(header, twoMetrics);

        this.messages = List.of(oneMessage, twoMessage);
    }

    @Test
    public void write() {
        when(iMessageRepository.save(messages.get(0))).thenReturn(messages.get(0));
        Message oneResult = messageRepository.write(messages.get(0));
        assertEquals(messages.get(0), oneResult);
        verify(iMessageRepository, times(1)).save(messages.get(0));

        when(iMessageRepository.save(messages.get(1))).thenReturn(messages.get(1));
        Message twoResult = messageRepository.write(messages.get(1));
        assertEquals(messages.get(1), twoResult);
        verify(iMessageRepository, times(1)).save(messages.get(1));
    }

    @Test
    public void findMessagesByDeviceId() {
        String deviceId = "4545";
        when(iMessageRepository.findMessagesByDeviceId(deviceId)).thenReturn(messages);
        System.out.println(messages.toString());
        List<Message> result = messageRepository.findMessagesByDeviceId(deviceId);

        assertEquals(messages, result);
        verify(iMessageRepository, times(1)).findMessagesByDeviceId(deviceId);
    }

    @Test
    public void findMessagesByLocation() {
        String location = "CENTIC";
        when(iMessageRepository.findMessagesByLocation(location)).thenReturn(messages);

        List<Message> result = messageRepository.findMessagesByLocation(location);

        assertEquals(messages, result);
        verify(iMessageRepository, times(1)).findMessagesByLocation(location);
    }

    @Test
    public void findMessagesBetweenTwoDate() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        when(iMessageRepository.findMessagesBetweenTwoDate(from, to)).thenReturn(messages);

        List<Message> result = messageRepository.findMessagesBetweenTwoDate(from, to);

        assertEquals(messages, result);
        verify(iMessageRepository, times(1)).findMessagesBetweenTwoDate(from, to);
    }

    @Test
    public void findMessagesInUnitsTime() {
        String time = "10m";
        when(iMessageRepository.findMessagesBetweenTwoDate(any(Instant.class), any(Instant.class)))
                .thenReturn(messages);

        List<Message> result = messageRepository.findMessagesInUnitsTime(time);

        assertEquals(messages, result);
        verify(iMessageRepository, times(1)).findMessagesBetweenTwoDate(any(Instant.class), any(Instant.class));
    }

    @Test
    public void findMessagesInUnitsTime_Invalid() {
        String invalidTime = "10x";

        assertThrows(UnitsTimeException.class, () -> {
            messageRepository.findMessagesInUnitsTime(invalidTime);
        });


        verify(iMessageRepository, never()).findMessagesBetweenTwoDate(any(), any());
    }

    @Test
    public void findLastMeasurements() {
        String measurement = "Temperature";
        int limit = 5;
        when(iMessageRepository.findLastMeasurements(measurement, limit)).thenReturn(messages);

        List<Message> result = messageRepository.findLastMeasurements(measurement, limit);

        assertEquals(messages, result);
        verify(iMessageRepository, times(1)).findLastMeasurements(measurement, limit);
    }

    @Test
    public void findMeasurementsByTimeRange() {
        String measurement = "CO2";
        Instant start = Instant.now().minusSeconds(1800);
        Instant end = Instant.now();
        when(iMessageRepository.findMeasurementsByTimeRange(measurement, start, end)).thenReturn(messages);

        List<Message> result = messageRepository.findMeasurementsByTimeRange(measurement, start, end);

        assertEquals(messages, result);
        verify(iMessageRepository, times(1)).findMeasurementsByTimeRange(measurement, start, end);
    }
}
