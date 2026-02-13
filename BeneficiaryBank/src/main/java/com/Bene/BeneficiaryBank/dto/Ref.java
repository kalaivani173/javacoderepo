package com.Bene.BeneficiaryBank.dto;
import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
public class Ref {
    @XmlAttribute private String type;
    @XmlAttribute private String seqNum;
    @XmlAttribute private String addr;
    @XmlAttribute private String settAmount;
    @XmlAttribute private String settCurrency;
    @XmlAttribute private String approvalNum;
    @XmlAttribute private String respCode;
    @XmlAttribute private String regName;
    @XmlAttribute private String orgAmount;
    @XmlAttribute private String acNum;
    @XmlAttribute private String IFSC;
    @XmlAttribute private String code;
    @XmlAttribute private String accType;

    // Getters & setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSeqNum() { return seqNum; }
    public void setSeqNum(String seqNum) { this.seqNum = seqNum; }

    public String getAddr() { return addr; }
    public void setAddr(String addr) { this.addr = addr; }

    public String getSettAmount() { return settAmount; }
    public void setSettAmount(String settAmount) { this.settAmount = settAmount; }

    public String getSettCurrency() { return settCurrency; }
    public void setSettCurrency(String settCurrency) { this.settCurrency = settCurrency; }

    public String getApprovalNum() { return approvalNum; }
    public void setApprovalNum(String approvalNum) { this.approvalNum = approvalNum; }

    public String getRespCode() { return respCode; }
    public void setRespCode(String respCode) { this.respCode = respCode; }

    public String getRegName() { return regName; }
    public void setRegName(String regName) { this.regName = regName; }

    public String getOrgAmount() { return orgAmount; }
    public void setOrgAmount(String orgAmount) { this.orgAmount = orgAmount; }

    public String getAcNum() { return acNum; }
    public void setAcNum(String acNum) { this.acNum = acNum; }

    public String getIFSC() { return IFSC; }
    public void setIFSC(String IFSC) { this.IFSC = IFSC; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getAccType() { return accType; }
    public void setAccType(String accType) { this.accType = accType; }
}