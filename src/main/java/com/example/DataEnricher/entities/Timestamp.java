package com.example.DataEnricher.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "enricherData")
public class Timestamp {
    @Id
    private String id;
    private long last_timestamp;

    public Timestamp() {
    }

    public Timestamp(String id, long timestamp) {
        this.id = id;
        this.last_timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return last_timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.last_timestamp = timestamp;
    }
}
