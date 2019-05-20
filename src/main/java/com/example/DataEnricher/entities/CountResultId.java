package com.example.DataEnricher.entities;

import java.util.Objects;

public class CountResultId {
    private int year;
    private int month;
    private int dayOfMonth;
    private int hour;
    private int fiveMinute;
    private String snifferId;


    public CountResultId() {
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

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
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

    public String getSnifferId() {
        return snifferId;
    }

    public void setSnifferId(String snifferId) {
        this.snifferId = snifferId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountResultId that = (CountResultId) o;
        return year == that.year &&
                month == that.month &&
                dayOfMonth == that.dayOfMonth &&
                hour == that.hour &&
                fiveMinute == that.fiveMinute &&
                snifferId.equals(that.snifferId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, dayOfMonth, hour, fiveMinute, snifferId);
    }
}
