package com.example.DataEnricher.entities;

public class TaggedParameterDD extends TaggedParameter{
    private String oui;
    private String completeOui;

    public TaggedParameterDD(String tag, int length, String value, String oui, String completeOui) {
        super(tag, length, value);
        this.oui = oui;
        this.completeOui = completeOui;
    }

    public String getOui() {
        return oui;
    }

    public void setOui(String oui) {
        this.oui = oui;
    }

    public String getCompleteOui() {
        return completeOui;
    }

    public void setCompleteOui(String completeOui) {
        this.completeOui = completeOui;
    }
}
