package com.npci.UPISim.dto;


import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Detail {
    @XmlAttribute private String name;
    @XmlAttribute private String value;

    public Detail() {
    }

    public Detail(String name, String value) {
        this.name = name;
        this.value = value;
    }

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

