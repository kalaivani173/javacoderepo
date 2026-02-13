package com.payee.psp.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class HbtMsg {

    @XmlAttribute(name = "type")
    private String type;   // e.g. ALIVE

    @XmlAttribute(name = "value")
    private String value;  // optional

    public HbtMsg() {}
    public HbtMsg(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
