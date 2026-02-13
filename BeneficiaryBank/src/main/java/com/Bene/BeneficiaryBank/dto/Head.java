package com.Bene.BeneficiaryBank.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Head", namespace = "http://npci.org/upi/schema/")
@XmlAccessorType(XmlAccessType.FIELD)
public class Head {

    @XmlAttribute(name = "msgId")
    private String msgId;

    @XmlAttribute(name = "orgId")
    private String orgId;

    @XmlAttribute(name = "ts")
    private String ts;

    @XmlAttribute(name = "ver")
    private String ver;

    // Default constructor (needed by JAXB)
    public Head() {}

    // Getters and setters
    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }

    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }

    public String getTs() { return ts; }
    public void setTs(String ts) { this.ts = ts; }

    public String getVer() { return ver; }
    public void setVer(String ver) { this.ver = ver; }
}