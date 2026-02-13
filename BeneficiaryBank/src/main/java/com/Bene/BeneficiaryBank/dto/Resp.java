package com.Bene.BeneficiaryBank.dto;
import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Resp {

    @XmlAttribute private String reqMsgId;
    @XmlAttribute private String result;

    @XmlElement(name = "Ref")
    private List<Ref> refs;

    // Getters & setters
    public String getReqMsgId() { return reqMsgId; }
    public void setReqMsgId(String reqMsgId) { this.reqMsgId = reqMsgId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public List<Ref> getRefs() { return refs; }
    public void setRefs(List<Ref> refs) { this.refs = refs; }
}