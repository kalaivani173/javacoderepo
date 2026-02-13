package com.npci.UPISim.dto;

import java.time.LocalDateTime;

public class TxnRespPayStatusDto {
    private String txnId;
    private String respPayStatus;
    private LocalDateTime createdAt;

    public TxnRespPayStatusDto() {}

    public TxnRespPayStatusDto(String txnId, String respPayStatus, LocalDateTime createdAt) {
        this.txnId = txnId;
        this.respPayStatus = respPayStatus;
        this.createdAt = createdAt;
    }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getRespPayStatus() { return respPayStatus; }
    public void setRespPayStatus(String respPayStatus) { this.respPayStatus = respPayStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
