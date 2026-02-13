package com.Bene.BeneficiaryBank.dto;

import java.util.List;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class Device {
    @XmlElement(name = "Tag")
    private List<Tag> tags;

    public Device() {
    }

    // getters and setters
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
}
