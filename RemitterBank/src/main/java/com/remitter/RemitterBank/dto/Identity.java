package com.remitter.RemitterBank.dto;


import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Identity {
    @XmlAttribute private String id;
    @XmlAttribute private String type;
    @XmlAttribute private String verifiedName;

    public Identity() {
    }

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVerifiedName() { return verifiedName; }
    public void setVerifiedName(String verifiedName) { this.verifiedName = verifiedName; }
}

