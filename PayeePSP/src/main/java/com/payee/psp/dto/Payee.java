package com.payee.psp.dto;



import jakarta.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
public class Payee {
    @XmlAttribute private String addr;
    @XmlAttribute private String code;
    @XmlAttribute private String name;
    @XmlAttribute private String seqNum;
    @XmlAttribute private String type;

    @XmlElement(name = "Ac")
    private Ac ac;


    @XmlElement(name="Amount")
    private Amount amount;

    // getters and setters

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Payee() {
    }

    public String getName() {
        return name;
    }

    public Ac getAc() {
        return ac;
    }

    public void setAc(Ac ac) {
        this.ac = ac;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }
}

