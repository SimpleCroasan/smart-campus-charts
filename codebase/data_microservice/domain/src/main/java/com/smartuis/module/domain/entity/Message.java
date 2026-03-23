package com.smartuis.module.domain.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "messages")
public class Message {
    private Header header;
    private List<Metric> metrics;

    public Message() {
    }

    public Message(Header header, List<Metric> metrics) {
        this.header = header;
        this.metrics = metrics;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header headers) {
        this.header = headers;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    @Override
    public String toString() {
        return "Message{" +
                "headers=" + header +
                ", metrics=" + metrics +
                '}';
    }
}
