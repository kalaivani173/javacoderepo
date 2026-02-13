package com.remitter.RemitterBank.dto;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "ReqPay", namespace = "http://npci.org/upi/schema/")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReqPay {

    @XmlElement(name = "Head")
    private Head head;

    @XmlElement(name = "Txn")
    private Txn txn;

    @XmlElement(name = "Payer")
    private Payer payer;

    @XmlElementWrapper(name = "Payees")
    @XmlElement(name = "Payee")
    private List<Payee> payees;

    // getters/setters


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
}