package com.npci.UPISim.service;

import com.npci.UPISim.dto.NodeStatusDto;
import com.npci.UPISim.model.NodeStatusEntity;
import com.npci.UPISim.repo.NodeStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NodeStatusService {

    private final NodeStatusRepository repo;

    public NodeStatusService(NodeStatusRepository repo) {
        this.repo = repo;
    }

    public List<NodeStatusDto> listAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public NodeStatusDto get(String orgId) {
        return repo.findById(orgId).map(this::toDto).orElse(null);
    }

    /**
     * Convenience method to update/create a node status - useful for testing.
     * If the node doesn't exist it will be created with provided values.
     */
    public NodeStatusDto upsertStatus(String orgId, String status) {
        NodeStatusEntity node = repo.findById(orgId).orElseGet(() -> {
            NodeStatusEntity n = new NodeStatusEntity();
            n.setOrgId(orgId);
            n.setConsecutiveFailures(0);
            return n;
        });
        node.setStatus(status);
        node.setUpdatedAt(java.time.Instant.now());
        if ("UP".equalsIgnoreCase(status)) {
            node.setLastAckAt(java.time.Instant.now());
        }
        return toDto(repo.save(node));
    }

    public List<NodeStatusDto> listByStatus(String status) {
        return repo.findByStatus(status).stream().map(this::toDto).collect(Collectors.toList());
    }

    private NodeStatusDto toDto(NodeStatusEntity e) {
        if (e == null) return null;
        NodeStatusDto d = new NodeStatusDto();
        d.setOrgId(e.getOrgId());
        d.setStatus(e.getStatus());
        d.setLastAckAt(e.getLastAckAt());
        d.setLastHeartbeatAt(e.getLastHeartbeatAt());
        d.setConsecutiveFailures(e.getConsecutiveFailures());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}
