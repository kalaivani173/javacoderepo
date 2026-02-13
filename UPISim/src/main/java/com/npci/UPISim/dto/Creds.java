package com.npci.UPISim.dto;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Creds {
    @XmlElement(name = "Cred")
    private List<Cred> creds;

    // getters and setters
    public List<Cred> getCreds() { return creds; }
    public void setCreds(List<Cred> creds) { this.creds = creds; }
}
