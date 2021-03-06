package com.example.DataEnricher.entities;

public class TaggedParameter {
    String tag;
    int length;
    String value;


    public TaggedParameter() {
    }

    public TaggedParameter(String tag, int length, String value) {
        this.tag = tag;
        this.length = length;
        this.value = value;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
