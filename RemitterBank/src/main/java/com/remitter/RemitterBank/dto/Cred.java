package com.remitter.RemitterBank.dto;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Cred")
public class Cred {

    @XmlAttribute(name = "type")
    private String type;

    @XmlAttribute(name = "subType")
    private String subType;

    @XmlElement(name = "Data")
    private Data data;

    // getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}