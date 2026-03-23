package com.smartuis.module.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.smartuis.module.service.impl.InfluxService;
import com.smartuis.module.domain.entity.Data;
import com.smartuis.module.domain.entity.Header;
import com.smartuis.module.domain.entity.Message;
import com.smartuis.module.domain.entity.Metric;
import com.smartuis.module.persistence.config.InfluxDBConfig;
import com.smartuis.module.persistence.mapper.FluxRecordMapper;
import com.smartuis.module.persistence.repository.InfluxRepository;
import com.smartuis.module.persistence.service.MessageRequeueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InfluxRepositoryTest {

    @Mock
    private InfluxDBClient influxDBClient;

    @Mock
    private InfluxDBConfig influxDBConfig;

    @Mock
    private InfluxService influxService;

    @Mock
    private MessageRequeueService messageRequeueService;

    @Mock
    private WriteApiBlocking writeApiBlocking;

    @Mock
    private FluxRecordMapper fluxRecordMapper;

    private InfluxRepository influxRepository;

    private static final String TEST_BUCKET = "messages";
    private static final String TEST_MEASUREMENT = "temperature";

    private Header header;
    private Message message;

    @BeforeEach
    void setUp() {
        header = new Header("2205", "2", Instant.now(), "CENTIC", "air_quality", true);
        List<Metric> metrics = List.of(
                new Metric("temperature", 23.0),
                new Metric("humidity", 70.0)
        );
        message = new Message(header, metrics);
        when(influxDBConfig.getBucket()).thenReturn(TEST_BUCKET);
        lenient().when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApiBlocking);

        influxRepository = new InfluxRepository(influxDBClient, influxDBConfig, influxService, messageRequeueService);

        try {
            java.lang.reflect.Field field = InfluxRepository.class.getDeclaredField("fluxRecordMapper");
            field.setAccessible(true);
            field.set(influxRepository, fluxRecordMapper);
        } catch (Exception e) {
            fail("No se pudo establecer el campo fluxRecordMapper: " + e.getMessage());
        }
    }

    @Test
    void writeShouldRequeueFalseTest() {
        Header nonRequeuableHeader = new Header(
                header.getUserUUID(),
                header.getDeviceId(),
                header.getTimeStamp(),
                header.getLocation(),
                header.getTopic(),
                false
        );

        Message testMessage = new Message(nonRequeuableHeader, message.getMetrics());
        doNothing().when(writeApiBlocking).writeMeasurement(any(WritePrecision.class), any(Data.class));

        Message result = influxRepository.write(testMessage);

        verify(writeApiBlocking, times(2)).writeMeasurement(eq(WritePrecision.NS), any(Data.class));
        verify(messageRequeueService, never()).requeueMessage(any(Message.class));
        assertEquals(testMessage, result);
    }

    @Test
    void writeShouldRequeueTrueTest() {
        doNothing().when(writeApiBlocking).writeMeasurement(any(WritePrecision.class), any(Data.class));
        doNothing().when(messageRequeueService).requeueMessage(any(Message.class));

        Message result = influxRepository.write(message);

        verify(writeApiBlocking, times(2)).writeMeasurement(eq(WritePrecision.NS), any(Data.class));
        verify(messageRequeueService).requeueMessage(message);
        assertEquals(message, result);
    }

    @Test
    void findLastMeasurementsTest() throws JsonProcessingException {
        int limit = 5;
        String expectedFluxQuery = String.format(
                "from(bucket: \"%s\") |> range(start: 0) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\") " +
                        "|> sort(columns:[\"_time\"], desc: true) " +
                        "|> limit(n: %d)",
                TEST_BUCKET, TEST_MEASUREMENT, limit);

        List<FluxTable> mockTables = new ArrayList<>();
        List<Message> expectedMessages = new ArrayList<>();

        Header resultHeader = new Header("2205", "2", Instant.now(), "CENTIC", "air_quality", false);
        expectedMessages.add(new Message(resultHeader, List.of(new Metric(TEST_MEASUREMENT, 22.0))));

        when(influxService.queryData(anyString())).thenReturn(mockTables);
        when(fluxRecordMapper.mapFluxTablesToMessages(mockTables)).thenReturn(expectedMessages);

        List<Message> result = influxRepository.findLastMeasurements(TEST_MEASUREMENT, limit);

        verify(influxService).queryData(expectedFluxQuery);
        verify(fluxRecordMapper).mapFluxTablesToMessages(mockTables);
        assertEquals(expectedMessages, result);
    }

    @Test
    void findMeasurementsByTimeRangeTest() {
        Instant start = Instant.parse("2025-04-02T20:28:18.449013800Z");
        Instant end = Instant.parse("2025-04-02T21:50:25.683879100Z");

        String expectedFluxQuery = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\")",
                TEST_BUCKET, start.toString(), end.toString(), TEST_MEASUREMENT);

        List<FluxTable> mockTables = new ArrayList<>();
        List<Message> expectedMessages = new ArrayList<>();

        Header resultHeader = new Header("2205", "2", Instant.now(), "CENTIC", "air_quality", false);
        expectedMessages.add(new Message(resultHeader, List.of(new Metric(TEST_MEASUREMENT, 22.0))));

        when(influxService.queryData(anyString())).thenReturn(mockTables);
        when(fluxRecordMapper.mapFluxTablesToMessages(mockTables)).thenReturn(expectedMessages);

        List<Message> result = influxRepository.findMeasurementsByTimeRange(TEST_MEASUREMENT, start, end);

        verify(influxService).queryData(expectedFluxQuery);
        verify(fluxRecordMapper).mapFluxTablesToMessages(mockTables);
        assertEquals(expectedMessages, result);
    }

    @Test
    void findMessagesBetweenTwoDateTest() {
        Instant from = Instant.parse("2025-04-01T00:00:00Z");
        Instant to = Instant.parse("2025-04-03T00:00:00Z");

        String expectedFluxQuery = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> sort(columns:[\"_time\"])",
                TEST_BUCKET, from.toString(), to.toString());

        List<FluxTable> mockTables = new ArrayList<>();
        List<Message> expectedMessages = new ArrayList<>();

        Header resultHeader = new Header("2205", "2", Instant.now(), "CENTIC", "air_quality", false);
        expectedMessages.add(new Message(resultHeader, List.of(new Metric(TEST_MEASUREMENT, 22.0))));

        when(influxService.queryData(anyString())).thenReturn(mockTables);
        when(fluxRecordMapper.mapFluxTablesToMessages(mockTables)).thenReturn(expectedMessages);

        List<Message> result = influxRepository.findMessagesBetweenTwoDate(from, to);

        verify(influxService).queryData(expectedFluxQuery);
        verify(fluxRecordMapper).mapFluxTablesToMessages(mockTables);
        assertEquals(expectedMessages, result);
    }

    @Test
    void findMessagesInUnitsTimeTest() {
        String time = "24h";

        String expectedFluxQuery = String.format(
                "from(bucket: \"%s\") |> range(start: -%s) |> sort(columns:[\"_time\"])",
                TEST_BUCKET, time);

        List<FluxTable> mockTables = new ArrayList<>();
        List<Message> expectedMessages = new ArrayList<>();

        Header resultHeader = new Header("2205", "2", Instant.now(), "CENTIC", "air_quality", false);
        expectedMessages.add(new Message(resultHeader, List.of(new Metric(TEST_MEASUREMENT, 22.0))));

        when(influxService.queryData(anyString())).thenReturn(mockTables);
        when(fluxRecordMapper.mapFluxTablesToMessages(mockTables)).thenReturn(expectedMessages);

        List<Message> result = influxRepository.findMessagesInUnitsTime(time);

        verify(influxService).queryData(expectedFluxQuery);
        verify(fluxRecordMapper).mapFluxTablesToMessages(mockTables);
        assertEquals(expectedMessages, result);
    }

    @Test
    void findAverageValueTest() {
        Instant start = Instant.parse("2023-01-01T00:00:00Z");
        Instant end = Instant.parse("2023-01-02T00:00:00Z");
        Double expectedAverage = 23.5;

        String expectedFluxQuery = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\" and r._field == \"value\") " +
                        "|> mean()",
                TEST_BUCKET, start.toString(), end.toString(), TEST_MEASUREMENT);

        List<FluxTable> mockTables = createMockFluxTableWithValue(expectedAverage);

        when(influxService.queryData(anyString())).thenReturn(mockTables);

        Optional<Double> result = influxRepository.findAverageValue(TEST_MEASUREMENT, start, end);

        verify(influxService).queryData(expectedFluxQuery);
        assertTrue(result.isPresent());
        assertEquals(expectedAverage, result.get());
    }

    @Test
    void findMaxValueTest() {
        Instant start = Instant.parse("2023-01-01T00:00:00Z");
        Instant end = Instant.parse("2023-01-02T00:00:00Z");
        Double expectedMax = 30.0;

        String expectedFluxQuery = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\" and r._field == \"value\") " +
                        "|> max()",
                TEST_BUCKET, start.toString(), end.toString(), TEST_MEASUREMENT);

        List<FluxTable> mockTables = createMockFluxTableWithValue(expectedMax);

        when(influxService.queryData(anyString())).thenReturn(mockTables);

        Optional<Double> result = influxRepository.findMaxValue(TEST_MEASUREMENT, start, end);

        verify(influxService).queryData(expectedFluxQuery);
        assertTrue(result.isPresent());
        assertEquals(expectedMax, result.get());
    }

    @Test
    void findMinValueTest() {
        Instant start = Instant.parse("2023-01-01T00:00:00Z");
        Instant end = Instant.parse("2023-01-02T00:00:00Z");
        Double expectedMin = 15.0;

        String expectedFluxQuery = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\" and r._field == \"value\") " +
                        "|> min()",
                TEST_BUCKET, start.toString(), end.toString(), TEST_MEASUREMENT);

        List<FluxTable> mockTables = createMockFluxTableWithValue(expectedMin);

        when(influxService.queryData(anyString())).thenReturn(mockTables);


        Optional<Double> result = influxRepository.findMinValue(TEST_MEASUREMENT, start, end);

        verify(influxService).queryData(expectedFluxQuery);
        assertTrue(result.isPresent());
        assertEquals(expectedMin, result.get());
    }

    private List<FluxTable> createMockFluxTableWithValue(Double value) {
        FluxTable mockTable = mock(FluxTable.class);
        FluxRecord mockRecord = mock(FluxRecord.class);

        List<FluxRecord> records = List.of(mockRecord);

        when(mockTable.getRecords()).thenReturn(records);
        when(mockRecord.getValue()).thenReturn(value);

        return List.of(mockTable);
    }
}