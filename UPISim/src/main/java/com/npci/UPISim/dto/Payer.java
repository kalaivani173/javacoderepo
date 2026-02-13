package com.npci.UPISim.dto;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "Payer")
@XmlAccessorType(XmlAccessType.FIELD)

public class Payer {

    @XmlAttribute(name = "addr")
    private String addr;

    @XmlAttribute(name = "code")
    private String code;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "seqNum")
    private String seqNum;

    @XmlAttribute(name = "type")
    private String type;

    @XmlElement(name = "Info")
    private Info info;

    @XmlElement(name = "Device")
    private Device device;

    public Payer() {
    }

    @XmlElement(name = "Creds")
    private Creds creds;

    public Creds getCreds() {
        return creds;
    }

    public void setCreds(Creds creds) {
        this.creds = creds;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @XmlElement(name = "Ac")
    private Ac ac;



    @XmlElement(name = "Amount")
    private Amount amount;

    // -------- Getters & Setters --------
    public String getAddr() { return addr; }
    public void setAddr(String addr) { this.addr = addr; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSeqNum() { return seqNum; }
    public void setSeqNum(String seqNum) { this.seqNum = seqNum; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Info getInfo() { return info; }
    public void setInfo(Info info) { this.info = info; }



    public Ac getAc() { return ac; }
    public void setAc(Ac ac) { this.ac = ac; }



    public Amount getAmount() { return amount; }
    public void setAmount(Amount amount) { this.amount = amount; }
}
