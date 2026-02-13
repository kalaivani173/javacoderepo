package com.npci.UPISim.service;

import com.npci.UPISim.model.NodeStatusEntity;
import com.npci.UPISim.repo.NodeStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class HealthCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private final NodeStatusRepository nodeRepo;
    private final Duration ackThreshold;
    private final Duration hbThreshold;

    public HealthCheckScheduler(NodeStatusRepository nodeRepo, Environment env) {
        this.nodeRepo = nodeRepo;
        long ackMinutes = Long.parseLong(env.getProperty("upisim.respAck.threshold.minutes", "3"));
        this.ackThreshold = Duration.ofMinutes(ackMinutes);
        // you can decide heartbeats threshold separately if you want
        this.hbThreshold = Duration.ofMinutes(Long.parseLong(env.getProperty("upisim.hb.threshold.minutes", "3")));
    }

    /**
     * Run periodically (configurable); default every 60s.
     * A node is UP only if last_ack_at (or last_heartbeat_at) is within threshold.
     */
    @Scheduled(fixedRateString = "${upisim.healthcheck.interval.ms:60000}")
    @Transactional
    public void checkNodes() {
        Instant now = Instant.now();
        List<NodeStatusEntity> nodes = nodeRepo.findAll();

        for (NodeStatusEntity node : nodes) {
            Instant lastAck = node.getLastAckAt();
            Instant lastHb  = node.getLastHeartbeatAt();
            String prevStatus = node.getStatus() == null ? "UNKNOWN" : node.getStatus();

            boolean ackRecent = lastAck != null && Duration.between(lastAck, now).compareTo(ackThreshold) <= 0;
            boolean hbRecent  = lastHb != null && Duration.between(lastHb, now).compareTo(hbThreshold) <= 0;

            // Choose your rule: require both ACK and HB recent, or ACK only.
            // Here we require last ACK within threshold OR last HB within threshold -> mark UP.
            boolean shouldBeUp = ackRecent || hbRecent;
            String newStatus = shouldBeUp ? "UP" : "DOWN";

            log.debug("HealthCheck scan: orgId={} lastAck={} lastHb={} prevStatus={} -> candidate={}",
                    node.getOrgId(), lastAck, lastHb, prevStatus, newStatus);

            if (!newStatus.equals(prevStatus)) {
                node.setStatus(newStatus);
                node.setUpdatedAt(Instant.now());
                nodeRepo.save(node);
                log.info("HealthCheck: orgId={} changed {} -> {}", node.getOrgId(), prevStatus, newStatus);
            }
        }
    }
}
