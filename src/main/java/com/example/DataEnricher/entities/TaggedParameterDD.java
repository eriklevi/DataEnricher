package com.example.DataEnricher.entities;

public class TaggedParameterDD extends TaggedParameter{
    private String oui;
    private String completeOui;
    private String vendor;

    public TaggedParameterDD(String tag, int length, String value, String oui, String completeOui, String vendor) {
        super(tag, length, value);
        this.oui = oui;
        this.completeOui = completeOui;
        this.vendor = vendor;
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

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
