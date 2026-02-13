package com.npci.UPISim.dto;

import jakarta.xml.bind.annotation.*;
import java.time.ZonedDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
public class Head {
    @XmlAttribute private String ver;
    @XmlAttribute private String ts;
    @XmlAttribute private String orgId;
    @XmlAttribute private String msgId;

    public Head() {
    }

    // getters & setters
    public String getVer() { return ver; }
    public void setVer(String ver) { this.ver = ver; }

    public String getTs() { return ts; }
    public void setTs(String ts) { this.ts = ts; }

    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }
}