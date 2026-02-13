package com.payer.PayerPSP.dto;


import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

public class Creds {
    @XmlElement(name = "Cred")
    private List<Cred> creds;

    // getters and setters
    public List<Cred> getCreds() { return creds; }
    public void setCreds(List<Cred> creds) { this.creds = creds; }
}

