package com.npci.UPISim.dto;


import jakarta.xml.bind.annotation.*;

import java.util.List;
@XmlAccessorType(XmlAccessType.FIELD)
public class Ac {
    @XmlAttribute private String addrType;

    @XmlElement(name = "Detail")
    private List<Detail> details;

    // getters and setters
    public String getAddrType() { return addrType; }
    public void setAddrType(String addrType) { this.addrType = addrType; }

    public List<Detail> getDetails() { return details; }
    public void setDetails(List<Detail> details) { this.details = details; }

    public String getDetailValue(String name) {
        if (details == null) return null;
        return details.stream()
                .filter(d -> name.equalsIgnoreCase(d.getName()))
                .map(Detail::getValue)
                .findFirst()
                .orElse(null);
    }
}

