package com.remitter.RemitterBank.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Amount {
    @XmlAttribute
    private String curr;
    @XmlAttribute private double value;

    public Amount() {
    }

    // getters and setters
    public String getCurr() { return curr; }
    public void setCurr(String curr) { this.curr = curr; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
