// FILE: D:\UPI-Hackathon-2\UPIVerse\RemitterBank\src\main\java\com\remitter\RemitterBank\dto\ReqMandate.java
package com.remitter.RemitterBank.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ReqMandate", namespace = "http://npci.org/upi/schema/")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReqMandate {

    @XmlElement(name = "Head")
    private Head head;

    @XmlElement(name = "Txn")
    private Txn txn;

    // getters / setters
    public Head getHead() { return head; }
    public void setHead(Head head) { this.head = head; }
    public Txn getTxn() { return txn; }
    public void setTxn(Txn txn) { this.txn = txn; }
}

// FILE: D:\UPI-Hackathon-2\UPIVerse\RemitterBank\src\main\java\com\remitter\RemitterBank\controller\ReqMandateController.java
package com.remitter.RemitterBank.controller;

import com.remitter.RemitterBank.dto.ReqMandate;
import com.remitter.RemitterBank.service.HbtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReqMandateController {

    @Autowired
    private HbtService hbtService;

    @PostMapping(value = "/ReqMandate", produces = MediaType.APPLICATION_XML_VALUE)
    public String createReqMandate(@RequestParam(name = "custRef", required = false, defaultValue = "NA") String custRef) throws Exception {
        // build ReqMandate XML
        ReqMandate reqMandate = hbtService.createReqMandate(custRef);
        String reqMandateXml = XmlUtils.toXml(reqMandate, ReqMandate.class);
        // Further processing...
    }
}
