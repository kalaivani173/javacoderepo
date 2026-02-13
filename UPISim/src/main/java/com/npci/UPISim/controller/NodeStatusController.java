package com.npci.UPISim.controller;

import com.npci.UPISim.dto.NodeStatusDto;
import com.npci.UPISim.service.NodeStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
public class NodeStatusController {

    private final NodeStatusService service;

    public NodeStatusController(NodeStatusService service) {
        this.service = service;
    }

    /**
     * GET /api/nodes
     * returns all node statuses
     */
    @GetMapping
    public ResponseEntity<List<NodeStatusDto>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    /**
     * GET /api/nodes/status/{status}
     * filter by UP/DOWN
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<NodeStatusDto>> listByStatus(@PathVariable String status) {
        return ResponseEntity.ok(service.listByStatus(status));
    }

    /**
     * GET /api/nodes/{orgId}
     */
    @GetMapping("/{orgId}")
    public ResponseEntity<NodeStatusDto> get(@PathVariable String orgId) {
        NodeStatusDto dto = service.get(orgId);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    /**
     * POST /api/nodes/{orgId}/status
     * body: { "status": "UP" }  (JSON or form param)
     * Simple endpoint to mark node UP/DOWN (useful for testing or heartbeat ack simulation).
     */
    public static class StatusUpdate {
        public String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @PostMapping("/{orgId}/status")
    public ResponseEntity<NodeStatusDto> updateStatus(@PathVariable String orgId, @RequestBody StatusUpdate req) {
        if (req == null || req.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }
        NodeStatusDto updated = service.upsertStatus(orgId, req.getStatus().toUpperCase());
        return ResponseEntity.ok(updated);
    }
}
