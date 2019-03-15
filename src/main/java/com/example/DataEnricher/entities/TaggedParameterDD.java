package com.example.DataEnricher.entities;

public class TaggedParameterDD extends TaggedParameter{
    private String oui;

    public TaggedParameterDD(String tag, int length, String value, String oui) {
        super(tag, length, value);
        this.oui = oui;
    }

    public String getOui() {
        return oui;
    }

    public void setOui(String oui) {
        this.oui = oui;
    }
}
