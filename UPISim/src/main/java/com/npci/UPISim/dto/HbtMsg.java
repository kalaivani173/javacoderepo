package com.npci.UPISim.dto;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class HbtMsg {

    @XmlAttribute(name = "type")
    private String type;

    @XmlAttribute(name = "value")
    private String value;

    // getters & setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
