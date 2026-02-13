package com.payer.PayerPSP.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "RiskScores")
@XmlAccessorType(XmlAccessType.FIELD)
public class RiskScores {

    @XmlElement(name = "Score")
    private List<Score> scores;

    public List<Score> getScores() { return scores; }
    public void setScores(List<Score> scores) { this.scores = scores; }
}