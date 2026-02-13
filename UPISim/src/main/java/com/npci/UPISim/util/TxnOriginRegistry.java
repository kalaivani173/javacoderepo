package com.npci.UPISim.util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * txnId -> Map<orgId, baseUrl>
 */
@Component
public class TxnOriginRegistry {

    private final Map<String, Map<String, String>> originMap = new ConcurrentHashMap<>();

    public void registerOrigin(String txnId, String orgId, String dbBaseUrl, String incomingBaseUrl) {
        if (txnId == null || orgId == null) return;
        String chosen = dbBaseUrl != null ? dbBaseUrl : incomingBaseUrl;
        originMap.computeIfAbsent(normalize(txnId), k -> new ConcurrentHashMap<>())
                .put(orgId.toLowerCase(), chosen);
    }

    public Map<String, String> getOrigins(String txnId) {
        return originMap.getOrDefault(normalize(txnId), Collections.emptyMap());
    }

    public List<String> getOriginUrls(String txnId) {
        Map<String, String> m = getOrigins(txnId);
        return new ArrayList<>(new LinkedHashSet<>(m.values()));
    }

    public void remove(String txnId) {
        originMap.remove(normalize(txnId));
    }

    public Map<String, Map<String, String>> snapshot() {
        return Collections.unmodifiableMap(originMap);
    }

    private String normalize(String txnId) {
        if (txnId == null) return null;
        String t = txnId.trim();
        if (t.startsWith("urn:txnid:")) t = t.substring("urn:txnid:".length());
        return t;
    }
}
