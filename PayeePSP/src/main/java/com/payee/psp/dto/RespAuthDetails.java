package com.payee.psp.dto;



import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "RespAuthDetails", namespace = "http://npci.org/upi/schema/")
@XmlAccessorType(XmlAccessType.FIELD)
public class RespAuthDetails {
    @XmlElement(name = "Head") private Head head;
    @XmlElement(name = "Txn") private Txn txn;
    @XmlElement(name = "Payer") private Payer payer;

    @XmlElement(name = "Resp")
    private Resp resp;

    @XmlElementWrapper(name = "Payees")
    @XmlElement(name = "Payee")
    private List<Payee> payees;

    // getters and setters

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public Txn getTxn() {
        return txn;
    }

    public void setTxn(Txn txn) {
        this.txn = txn;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }

    public List<Payee> getPayees() {
        return payees;
    }

    public void setPayees(List<Payee> payees) {
        this.payees = payees;
    }

    public RespAuthDetails() {
    }

    public Resp getResp() {
        return resp;
    }

    public void setResp(Resp resp) {
        this.resp = resp;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Resp {
        @XmlAttribute(name = "reqMsgId")
        private String reqMsgId;

        @XmlAttribute(name = "result")
        private String result;

        // Getters and setters
        public String getReqMsgId() { return reqMsgId; }
        public void setReqMsgId(String reqMsgId) { this.reqMsgId = reqMsgId; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
    }
}
