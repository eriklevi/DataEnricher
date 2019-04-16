package com.example.DataEnricher.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prova")
public class Prova {
    @Id
    private String id;
    private long timestamp;
    private long counterTimestamp;

    public Prova() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getCounterTimestamp() {
        return counterTimestamp;
    }

    public void setCounterTimestamp(long counterTimestamp) {
        this.counterTimestamp = counterTimestamp;
    }
}
