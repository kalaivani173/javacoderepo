package com.npci.UPISim.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_logs")
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String txnId;
    private String api;         // ReqPay, RespAuthDetails, etc.
    private String direction;   // INBOUND / OUTBOUND / ACK
    private String uri;


    @Column(columnDefinition = "TEXT")
    private String payload;     // full XML

    private LocalDateTime createdAt = LocalDateTime.now();

    private String status;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getApi() { return api; }
    public void setApi(String api) { this.api = api; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}