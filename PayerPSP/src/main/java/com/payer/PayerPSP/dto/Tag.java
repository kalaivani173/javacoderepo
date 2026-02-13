package com.payer.PayerPSP.dto;



import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Tag {
    @XmlAttribute private String name;
    @XmlAttribute private String value;

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

