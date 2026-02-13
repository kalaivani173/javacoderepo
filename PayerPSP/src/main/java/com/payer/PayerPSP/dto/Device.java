package com.payer.PayerPSP.dto;



import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Device {
    @XmlElement(name = "Tag")
    private List<Tag> tags;

    // getters and setters
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
}

