package com.example.DataEnricher.entities;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public class CountResult {

    private CountResultId timeFrame;
    private int totPackets;
    private int totMacs;

    public CountResult() {
    }

    public CountResultId getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(CountResultId timeFrame) {
        this.timeFrame = timeFrame;
    }

    public int getTotPackets() {
        return totPackets;
    }

    public void setTotPackets(int totPackets) {
        this.totPackets = totPackets;
    }

    public int getTotMacs() {
        return totMacs;
    }

    public void setTotMacs(int totMacs) {
        this.totMacs = totMacs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountResult that = (CountResult) o;
        return timeFrame.equals(that.timeFrame);
    }

    @Override
    public int hashCode() {
        return this.timeFrame.hashCode();
    }
}
