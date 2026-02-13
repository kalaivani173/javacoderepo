package com.remitter.RemitterBank.dto;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)

public class Txn {
    @XmlAttribute
    private String custRef;
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String initiationMode;
    @XmlAttribute
    private String note;
    @XmlAttribute
    private String purpose;
    @XmlAttribute
    private String refId;
    @XmlAttribute
    private String refUrl;
    @XmlAttribute
    private String subType;
    @XmlAttribute
    private String ts;
    @XmlAttribute
    private String type;




    @XmlElement(name = "RiskScores")
    private RiskScores riskScores;

    // --- Correct getter and setter ---
    public RiskScores getRiskScores() { return riskScores; }
    public void setRiskScores(RiskScores riskScores) { this.riskScores = riskScores; }
    // --- Attributes ---

    public Txn() {
    }

    public String getCustRef() {
        return custRef;
    }
    public void setCustRef(String custRef) {
        this.custRef = custRef;
    }


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    public String getInitiationMode() {
        return initiationMode;
    }
    public void setInitiationMode(String initiationMode) {
        this.initiationMode = initiationMode;
    }


    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }


    public String getPurpose() {
        return purpose;
    }
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }


    public String getRefId() {
        return refId;
    }
    public void setRefId(String refId) {
        this.refId = refId;
    }


    public String getRefUrl() {
        return refUrl;
    }
    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }


    public String getSubType() {
        return subType;
    }
    public void setSubType(String subType) {
        this.subType = subType;
    }


    public String getTs() {
        return ts;
    }
    public void setTs(String ts) {
        this.ts = ts;
    }


    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    // --- Elements ---




}
