package com.example.DataEnricher.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Objects;

@Document(collection = "countedPackets")
public class CountedPackets {
    @Id
    private String id;
    private String snifferMac;
    private String snifferId;
    private String snifferName;
    private String buildingName;
    private String buildingId;
    private String roomId;
    private String roomName;
    private int totalPackets;
    private int globalPackets;
    private int localPackets;
    private int totalDistinctMacAddresses;
    private int totalDistinctFingerprints;
    private int totalEstimatedDevices;
    private long startTimestamp;
    private int year;
    private int month;
    private int weekOfYear;
    private int dayOfMonth;
    private int dayOfWeek;
    private int hour;
    private int twoHour;
    private int fourHour;
    private int sixHour;
    private int twelveHour;
    private int thirtyMinute;
    private int fifteenMinute;
    private int fiveMinute;
    private int minute;

    public CountedPackets() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSnifferMac() {
        return snifferMac;
    }

    public void setSnifferMac(String snifferMac) {
        this.snifferMac = snifferMac;
    }

    public int getTotalPackets() {
        return totalPackets;
    }

    public void setTotalPackets(int totalPackets) {
        this.totalPackets = totalPackets;
    }

    /**
     * This overload sets the number of total packets based on the values of the instance variables globalPackets and
     * localPackets that have to be set in advance.
     */
    public void setTotalPackets() {
        this.totalPackets = this.globalPackets + this.localPackets;
    }

    public int getGlobalPackets() {
        return globalPackets;
    }

    public void setGlobalPackets(int globalPackets) {
        this.globalPackets = globalPackets;
    }

    public int getLocalPackets() {
        return localPackets;
    }

    public void setLocalPackets(int localPackets) {
        this.localPackets = localPackets;
    }

    public int getTotalDistinctMacAddresses() {
        return totalDistinctMacAddresses;
    }

    public void setTotalDistinctMacAddresses(int totalDistinctMacAddresses) {
        this.totalDistinctMacAddresses = totalDistinctMacAddresses;
    }

    public int getTotalDistinctFingerprints() {
        return totalDistinctFingerprints;
    }

    public void setTotalDistinctFingerprints(int totalDistinctFingerprints) {
        this.totalDistinctFingerprints = totalDistinctFingerprints;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
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

    public int getTotalEstimatedDevices() {
        return totalEstimatedDevices;
    }

    public void setTotalEstimatedDevices(int totalEstimatedDevices) {
        this.totalEstimatedDevices = totalEstimatedDevices;
    }

    public void setTotalEstimatedDevices(){
        this.totalEstimatedDevices = this.totalDistinctFingerprints + this.totalDistinctMacAddresses;
    }

    public String getSnifferName() {
        return snifferName;
    }

    public void setSnifferName(String snifferName) {
        this.snifferName = snifferName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getTwoHour() {
        return twoHour;
    }

    public void setTwoHour(int twoHour) {
        this.twoHour = twoHour;
    }

    public int getFourHour() {
        return fourHour;
    }

    public void setFourHour(int fourHour) {
        this.fourHour = fourHour;
    }

    public int getSixHour() {
        return sixHour;
    }

    public void setSixHour(int sixHour) {
        this.sixHour = sixHour;
    }

    public int getTwelveHour() {
        return twelveHour;
    }

    public void setTwelveHour(int twelveHour) {
        this.twelveHour = twelveHour;
    }

    public int getThirtyMinute() {
        return thirtyMinute;
    }

    public void setThirtyMinute(int thirtyMinute) {
        this.thirtyMinute = thirtyMinute;
    }

    public int getFifteenMinute() {
        return fifteenMinute;
    }

    public void setFifteenMinute(int fifteenMinute) {
        this.fifteenMinute = fifteenMinute;
    }

    public String getSnifferId() {
        return snifferId;
    }

    public void setSnifferId(String snifferId) {
        this.snifferId = snifferId;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountedPackets that = (CountedPackets) o;
        return year == that.year &&
                month == that.month &&
                dayOfMonth == that.dayOfMonth &&
                hour == that.hour &&
                fiveMinute == that.fiveMinute &&
                Objects.equals(snifferId, that.snifferId);
    }

    @Override
    public int hashCode() {
        return Objects.hash( year, month, dayOfMonth, hour, fiveMinute, snifferId);
    }

    public void setTimeFrame(CountResultId crId){
        ZonedDateTime ldt = LocalDateTime.of(crId.getYear(), crId.getMonth(), crId.getDayOfMonth(), crId.getHour(), (crId.getFiveMinute()-1)*5, 0).atZone(ZoneId.of("CET"));
        this.setYear(crId.getYear());
        this.setWeekOfYear(ldt.get(WeekFields.ISO.weekOfYear()));
        this.setMonth(ldt.getMonthValue());
        this.setDayOfMonth(ldt.getDayOfMonth());
        this.setDayOfWeek(ldt.getDayOfWeek().getValue());
        this.setHour(ldt.getHour());
        this.setFiveMinute(ldt.getMinute()/5+1);
        this.setFifteenMinute(ldt.getMinute()/15+1);
        this.setThirtyMinute(ldt.getMinute()/30+1);
        this.setTwoHour(ldt.getHour()/2+1);
        this.setFourHour(ldt.getHour()/4+1);
        this.setSixHour(ldt.getHour()/6+1);
        this.setTwelveHour(ldt.getHour()/12+1);
        this.setMinute(ldt.getMinute());
        this.setStartTimestamp(ldt.toEpochSecond()*1000);
    }
}
