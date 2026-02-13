package com.npci.UPISim.dto;



import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Data")
public class Data {

    @XmlAttribute(name = "code")
    private String code;

    @XmlAttribute(name = "ki")
    private String ki;

    @XmlValue
    private String value;

    // getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getKi() { return ki; }
    public void setKi(String ki) { this.ki = ki; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

