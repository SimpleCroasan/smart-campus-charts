package com.smartuis.module.domain.repository;

import java.time.Instant;
import java.util.Optional;

public interface StatisticsQuery {
    Optional<Double> findAverageValue(String measurement, Instant start, Instant end);
    Optional<Double> findMaxValue(String measurement, Instant start, Instant end);
    Optional<Double> findMinValue(String measurement, Instant start, Instant end);
}
