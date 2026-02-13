package com.remitter.RemitterBank.dto;



import jakarta.xml.bind.annotation.*;

import java.util.List;
@XmlAccessorType(XmlAccessType.FIELD)
public class Ac {
    @XmlAttribute private String addrType;

    public Ac() {
    }

    @XmlElement(name = "Detail")
    private List<Detail> details;

    // getters and setters
    public String getAddrType() { return addrType; }
    public void setAddrType(String addrType) { this.addrType = addrType; }

    public List<Detail> getDetails() { return details; }
    public void setDetails(List<Detail> details) { this.details = details; }
}
