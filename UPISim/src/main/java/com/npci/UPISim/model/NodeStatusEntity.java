package com.npci.UPISim.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "node_status")
public class NodeStatusEntity {
    @Id
    @Column(name = "org_id")
    private String orgId;

    @Column(name = "status")
    private String status; // UP / DOWN

    @Column(name = "last_ack_at")
    private Instant lastAckAt;

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @Column(name = "consecutive_failures")
    private Integer consecutiveFailures = 0;

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    // getters & setters
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getLastAckAt() { return lastAckAt; }
    public void setLastAckAt(Instant lastAckAt) { this.lastAckAt = lastAckAt; }

    public Instant getLastHeartbeatAt() { return lastHeartbeatAt; }
    public void setLastHeartbeatAt(Instant lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }

    public Integer getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(Integer consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

