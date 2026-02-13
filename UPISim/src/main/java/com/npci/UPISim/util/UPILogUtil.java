package com.npci.UPISim.util;

import com.npci.UPISim.model.TransactionLog;
import com.npci.UPISim.repo.TransactionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class UPILogUtil {

    private static final Logger log = LoggerFactory.getLogger("UPILog");

    private final TransactionLogRepository repo;

    public UPILogUtil(TransactionLogRepository repo) {
        this.repo = repo;
    }

    public void logInbound(String api, String uri, String rawXml, String txnId) {
        String normalized = normalizeTxnId(txnId, uri);
        logIfNormalized(txnId, normalized, api, "INBOUND", uri);
        String msg = "INBOUND [" + normalized + "] API=" + api + ", URI=" + uri + "\n" + rawXml;
        log.info(msg);
        saveToDbIfNotDuplicate(normalized, "INBOUND", api, uri, rawXml);
    }

    public void logOutbound(String api, String url, String xmlPayload, String txnId) {
        String normalized = normalizeTxnId(txnId, url);
        logIfNormalized(txnId, normalized, api, "OUTBOUND", url);
        String msg = "OUTBOUND [" + normalized + "] API=" + api + ", URL=" + url + "\n" + xmlPayload;
        log.info(msg);
        saveToDbIfNotDuplicate(normalized, "OUTBOUND", api, url, xmlPayload);
    }

    public void logAck(String uri, String ackXml, String txnId) {
        String normalized = normalizeTxnId(txnId, uri);
        logIfNormalized(txnId, normalized, "Ack", "ACK", uri);
        String msg = "ACK [" + normalized + "] URI=" + uri + "\n" + ackXml;
        log.info(msg);
        saveToDbIfNotDuplicate(normalized, "ACK", "Ack", uri, ackXml);
    }

    // ---------- helpers ----------

    private void saveToDbIfNotDuplicate(String txnId, String direction, String api, String uri, String payload) {
        if (txnId == null) {
            // still save but mark txnId null — depends on your requirements
            saveToDbInternal(null, direction, api, uri, payload);
            return;
        }

        try {
            Optional<TransactionLog> lastOpt =
                    repo.findTopByTxnIdAndApiAndDirectionOrderByCreatedAtDesc(txnId, api, direction);

            if (lastOpt.isPresent()) {
                TransactionLog last = lastOpt.get();
                // compare payload exact match and short time window
                if (last.getPayload() != null && last.getPayload().equals(payload)) {
                    long secs = Duration.between(last.getCreatedAt(), LocalDateTime.now()).getSeconds();
                    if (secs >= 0 && secs < 3) { // skip duplicates within 3 seconds
                        log.debug("Skipped near-duplicate log for txn={}, api={}, direction={}", txnId, api, direction);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // if duplicate-check fails for any reason, fall back to saving to avoid losing logs
            log.warn("Duplicate-check failed, proceeding to save log for txnId={}. Reason: {}", txnId, e.getMessage());
        }

        saveToDbInternal(txnId, direction, api, uri, payload);
    }

    private void saveToDbInternal(String txnId, String direction, String api, String uri, String payload) {
        TransactionLog entity = new TransactionLog();
        entity.setTxnId(txnId);
        entity.setDirection(direction);
        entity.setApi(api);
        entity.setUri(uri);
        entity.setPayload(payload);
        repo.save(entity);
    }

    /**
     * Normalize txn id. Priority: explicit txnId param -> extract from uri if present -> null
     */
    private String normalizeTxnId(String maybeTxnId, String uri) {
        if (maybeTxnId != null) {
            String trimmed = maybeTxnId.trim();
            if (!trimmed.isEmpty()) {
                return stripUrnPrefix(trimmed);
            }
        }
        if (uri != null) {
            int idx = uri.indexOf("urn:txnid:");
            if (idx >= 0) {
                String candidate = uri.substring(idx + "urn:txnid:".length());
                // candidate may contain trailing path/query; strip at first non-id char if any (e.g., '/','?')
                int endIdx = findEndOfTxnId(candidate);
                return candidate.substring(0, endIdx);
            }
        }
        return maybeTxnId == null ? null : stripUrnPrefix(maybeTxnId.trim());
    }

    private String stripUrnPrefix(String s) {
        if (s.startsWith("urn:txnid:")) return s.substring("urn:txnid:".length());
        return s;
    }

    private int findEndOfTxnId(String s) {
        // txn id appears to be alphanumeric + maybe hyphen; stop at '/', '?', '&'
        int pos = s.length();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '/' || c == '?' || c == '&') {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private void logIfNormalized(String original, String normalized, String api, String dir, String uri) {
        if (original == null) return;
        String oTrim = original.trim();
        if (!oTrim.equals(normalized)) {
            log.debug("Normalized txnId from '{}' to '{}' for API={} DIR={} URI={}", original, normalized, api, dir, uri);
        }
    }
}
