package com.npci.UPISim.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "heartbeat_messages")
public class HeartbeatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txn_id", nullable = false)
    private String txnId;

    @Column(name = "org_id")
    private String orgId;

    @Column(name = "message_type", nullable = false)
    private String messageType; // REQ_HBT / RESP_HBT / ACK

    @Column(name = "direction", nullable = false)
    private String direction; // INBOUND / OUTBOUND

    @Column(name = "api", nullable = false)
    private String api;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Lob
    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    @Column(name = "ack_result")
    private String ackResult;

    @Column(name = "notes")
    private String notes;

    // getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getApi() { return api; }
    public void setApi(String api) { this.api = api; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getAckResult() { return ackResult; }
    public void setAckResult(String ackResult) { this.ackResult = ackResult; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
