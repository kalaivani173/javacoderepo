package com.npci.UPISim.dto;



import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "RespPay", namespace = "http://npci.org/upi/schema/")
@XmlAccessorType(XmlAccessType.FIELD)
public class RespPay {

    @XmlElement(name = "Head")
    private Head head;

    @XmlElement(name = "Txn")
    private Txn txn;

    @XmlElement(name = "Resp")
    private Resp resp;

    // --- Getters & Setters ---
    public Head getHead() { return head; }
    public void setHead(Head head) { this.head = head; }

    public Txn getTxn() { return txn; }
    public void setTxn(Txn txn) { this.txn = txn; }

    public Resp getResp() { return resp; }
    public void setResp(Resp resp) { this.resp = resp; }
}