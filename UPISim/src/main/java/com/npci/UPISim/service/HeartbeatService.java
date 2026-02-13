package com.npci.UPISim.service;

import com.npci.UPISim.model.HeartbeatMessage;
import com.npci.UPISim.model.NodeStatusEntity;
import com.npci.UPISim.repo.HeartbeatMessageRepository;
import com.npci.UPISim.repo.NodeStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class HeartbeatService {
    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);

    private final HeartbeatMessageRepository msgRepo;
    private final NodeStatusRepository nodeRepo;

    public HeartbeatService(HeartbeatMessageRepository msgRepo, NodeStatusRepository nodeRepo) {
        this.msgRepo = msgRepo;
        this.nodeRepo = nodeRepo;
    }

    @Transactional
    public void persistInboundReqHbt(String txnId, String orgId, String rawXml) {
        HeartbeatMessage m = new HeartbeatMessage();
        m.setTxnId(txnId);
        m.setOrgId(orgId);
        m.setMessageType("REQ_HBT");
        m.setDirection("INBOUND");
        m.setApi("ReqHbt");
        m.setPayload(rawXml);
        m.setCreatedAt(Instant.now());
        msgRepo.save(m);

        NodeStatusEntity node = nodeRepo.findById(orgId).orElseGet(() -> {
            NodeStatusEntity n = new NodeStatusEntity();
            n.setOrgId(orgId);
            n.setStatus("DOWN");
            return n;
        });
        node.setLastHeartbeatAt(Instant.now());
        node.setUpdatedAt(Instant.now());
        nodeRepo.save(node);
        log.debug("persistInboundReqHbt persisted for org={} txn={}", orgId, txnId);
    }

    @Transactional
    public void persistOutboundRespHbt(String txnId, String orgId, String rawXml) {
        HeartbeatMessage m = new HeartbeatMessage();
        m.setTxnId(txnId);
        m.setOrgId(orgId);
        m.setMessageType("RESP_HBT");
        m.setDirection("OUTBOUND");
        m.setApi("RespHbt");
        m.setPayload(rawXml);
        m.setCreatedAt(Instant.now());
        msgRepo.save(m);
    }

    @Transactional
    public void persistAck(String txnId, String orgId, String rawXml, boolean success) {
        HeartbeatMessage m = new HeartbeatMessage();
        m.setTxnId(txnId);
        m.setOrgId(orgId);
        m.setMessageType("ACK");
        m.setDirection("INBOUND");
        m.setApi("Ack");
        m.setPayload(rawXml);
        m.setCreatedAt(Instant.now());
        m.setAckResult(success ? "SUCCESS" : "FAILURE");
        msgRepo.save(m);

        NodeStatusEntity node = nodeRepo.findById(orgId).orElseGet(() -> {
            NodeStatusEntity n = new NodeStatusEntity();
            n.setOrgId(orgId);
            n.setStatus("DOWN");
            return n;
        });
        if (success) {
            node.setStatus("UP");
            node.setLastAckAt(Instant.now());
            node.setConsecutiveFailures(0);
        } else {
            node.setConsecutiveFailures((node.getConsecutiveFailures() == null ? 0 : node.getConsecutiveFailures()) + 1);
        }
        node.setUpdatedAt(Instant.now());
        nodeRepo.save(node);
    }

    @Transactional
    public void markNodeFailure(String orgId) {
        NodeStatusEntity node = nodeRepo.findById(orgId).orElseGet(() -> {
            NodeStatusEntity n = new NodeStatusEntity();
            n.setOrgId(orgId);
            n.setStatus("DOWN");
            return n;
        });
        node.setConsecutiveFailures((node.getConsecutiveFailures() == null ? 0 : node.getConsecutiveFailures()) + 1);
        node.setUpdatedAt(Instant.now());
        nodeRepo.save(node);
    }

    @Transactional
    public void markNodeDown(String orgId) {
        NodeStatusEntity node = nodeRepo.findById(orgId).orElseGet(() -> {
            NodeStatusEntity n = new NodeStatusEntity();
            n.setOrgId(orgId);
            return n;
        });
        node.setStatus("DOWN");
        node.setUpdatedAt(Instant.now());
        nodeRepo.save(node);
    }
}

