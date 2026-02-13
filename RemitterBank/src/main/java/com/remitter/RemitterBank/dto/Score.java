package com.remitter.RemitterBank.dto;


import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Score {
    @XmlAttribute private String provider;
    @XmlAttribute private String type;
    @XmlAttribute private String value;

    public Score() {
    }
// getters and setters


    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

