package com.npci.UPISim.dto;

import java.time.Instant;

public class NodeStatusDto {
    private String orgId;
    private String status;
    private Instant lastAckAt;
    private Instant lastHeartbeatAt;
    private Integer consecutiveFailures;
    private Instant updatedAt;

    // getters / setters
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
