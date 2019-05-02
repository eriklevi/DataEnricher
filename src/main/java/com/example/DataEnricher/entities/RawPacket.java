package com.example.DataEnricher.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("rawPackets")
public class RawPacket {
    @Id
    private String id;
    private long timestamp;
    private String snifferMac;
    private String deviceMac;
    private boolean global;
    private String rawData;
    private String fingerprint;
    private int sequenceNumber;
    private List<TaggedParameter> taggedParameters;
    private int taggedParametersLength; //attenzione è la lunghezza della stringa, bisogna fare diviso 2


    public RawPacket(){}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSnifferMac() {
        return snifferMac;
    }

    public void setSnifferMac(String snifferMac) {
        this.snifferMac = snifferMac;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public List<TaggedParameter> getTaggedParameters() {
        return taggedParameters;
    }

    public void setTaggedParameters(List<TaggedParameter> taggedParameters) {
        this.taggedParameters = taggedParameters;
    }

    public int getTaggedParametersLength() {
        return taggedParametersLength;
    }

    public void setTaggedParametersLength(int taggedParametersLength) {
        this.taggedParametersLength = taggedParametersLength;
    }
}
