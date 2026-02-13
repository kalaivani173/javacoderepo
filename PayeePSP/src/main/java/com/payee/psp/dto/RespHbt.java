package com.payee.psp.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RespHbt", namespace = "http://npci.org/upi/schema/")
@XmlAccessorType(XmlAccessType.FIELD)
public class RespHbt {

    @XmlElement(name = "Head")
    private Head head;

    @XmlElement(name = "Txn")
    private Txn txn;

    @XmlElement(name = "Resp")
    private Resp resp;

    // Optional Signature element may be present; ignore or add mapping if needed

    public Head getHead() { return head; }
    public void setHead(Head head) { this.head = head; }

    public Txn getTxn() { return txn; }
    public void setTxn(Txn txn) { this.txn = txn; }

    public Resp getResp() { return resp; }
    public void setResp(Resp resp) { this.resp = resp; }
}
