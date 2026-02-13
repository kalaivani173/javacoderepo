package com.npci.UPISim.dto;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "ReqHbt", namespace = "http://npci.org/upi/schema/")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReqHbt {

    @XmlElement(name = "Head")
    private Head head;

    @XmlElement(name = "Txn")
    private Txn txn;

    @XmlElement(name = "HbtMsg")
    private HbtMsg hbtMsg;

    // getters & setters
    public Head getHead() { return head; }
    public void setHead(Head head) { this.head = head; }

    public Txn getTxn() { return txn; }
    public void setTxn(Txn txn) { this.txn = txn; }

    public HbtMsg getHbtMsg() { return hbtMsg; }
    public void setHbtMsg(HbtMsg hbtMsg) { this.hbtMsg = hbtMsg; }
}
