package com.smartuis.module.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfluxService {

    private InfluxDBClient influxDBClient;


    public InfluxService(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    public List<FluxTable> queryData(String query) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(query);
    }
}
