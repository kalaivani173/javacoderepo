package com.npci.UPISim.dto;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Device {
    @XmlElement(name = "Tag")
    private List<Tag> tags;

    @XmlAttribute(name = "BINDINGMODE")
    private String bindingMode;

    // getters and setters
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    
    public String getBindingMode() { return bindingMode; }
    public void setBindingMode(String bindingMode) { this.bindingMode = bindingMode; }
}