package com.remitter.RemitterBank.dto;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Rating {
    @XmlAttribute private String verifiedAddress;

    public Rating() {
    }

    // getters and setters
    public String getVerifiedAddress() { return verifiedAddress; }
    public void setVerifiedAddress(String verifiedAddress) { this.verifiedAddress = verifiedAddress; }
}
