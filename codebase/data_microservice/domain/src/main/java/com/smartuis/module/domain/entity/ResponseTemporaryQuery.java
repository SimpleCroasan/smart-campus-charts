package com.smartuis.module.domain.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ResponseTemporaryQuery {
    private Instant start;
    private Instant end;
    private List<DataDTO> data;

    public ResponseTemporaryQuery(Instant start, Instant end) {
        this.start = start;
        this.end = end;
        this.data = new ArrayList<>();
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public List<DataDTO> getData() {
        return data;
    }

    public void setData(List<DataDTO> data) {
        this.data = data;
    }
}
