package com.example.DataEnricher.entities;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("parsedPackets")
public class ParsedPacket {
    private long timestamp;
    private String snifferMac;
    private String deviceMac;
    private boolean global;
    private int sequenceNumber;
    private String ssid;
    private int ssidLen;
    private String fingerprint;

    public ParsedPacket() {
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

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getSsidLen() {
        return ssidLen;
    }

    public void setSsidLen(int ssidLen) {
        this.ssidLen = ssidLen;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
