package com.example.DataEnricher.entities;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("parsedPackets")
public class EnrichedParsedPacket {
    private long timestamp;
    private String snifferMac;
    private String deviceMac;
    private String deviceOui;
    private String completeDeviceOui;
    private boolean global;
    private int sequenceNumber;
    private String ssid;
    private int ssidLen;
    private String fingerprint;
    private int year;
    private int month;
    private int weekOfYear; //week of year
    private int dayOfMonth;
    private int dayOfWeek;
    private int hour;
    private int quarter;
    private int fiveMinute;
    private int minute;

    public EnrichedParsedPacket() {
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public int getFiveMinute() {
        return fiveMinute;
    }

    public void setFiveMinute(int fiveMinute) {
        this.fiveMinute = fiveMinute;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getDeviceOui() {
        return deviceOui;
    }

    public void setDeviceOui(String deviceOui) {
        this.deviceOui = deviceOui;
    }

    public String getCompleteDeviceOui() {
        return completeDeviceOui;
    }

    public void setCompleteDeviceOui(String completeDeviceOui) {
        this.completeDeviceOui = completeDeviceOui;
    }
}
