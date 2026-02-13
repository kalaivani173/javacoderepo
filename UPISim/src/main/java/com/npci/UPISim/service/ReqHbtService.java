package com.npci.UPISim.service;

import com.npci.UPISim.dto.*;
import com.npci.UPISim.util.TxnOriginRegistry;
import com.npci.UPISim.util.UPILogUtil;
import com.npci.UPISim.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ReqHbtService {

    private static final Logger log = LoggerFactory.getLogger(ReqHbtService.class);

    private final UPILogUtil upiLogUtil;
    private final TxnOriginRegistry txnOriginRegistry;
    private final RoutingService routingService;
    private final HeartbeatService heartbeatService;

    public ReqHbtService(UPILogUtil upiLogUtil,
                         TxnOriginRegistry txnOriginRegistry,
                         RoutingService routingService,
                         HeartbeatService heartbeatService) {
        this.upiLogUtil = upiLogUtil;
        this.txnOriginRegistry = txnOriginRegistry;
        this.routingService = routingService;
        this.heartbeatService = heartbeatService;
    }

    public void processReqHbt(ReqHbt reqHbt) {
        processReqHbt(reqHbt, null);
    }

    public void processReqHbt(ReqHbt reqHbt, String initiatingRole) {
        String rawTxn = (reqHbt != null && reqHbt.getTxn() != null) ? reqHbt.getTxn().getId() : null;
        String txnId = normalizeTxnId(rawTxn);

        CompletableFuture.runAsync(() -> {
            try {
                RespHbt respHbt = buildRespHbt(reqHbt);
                String respXml = XmlUtil.toXml(respHbt, RespHbt.class);

                Map<String, String> originsMap = txnOriginRegistry.getOrigins(txnId); // orgId -> baseUrl
                if (originsMap == null || originsMap.isEmpty()) {
                    log.warn("No recipients found for RespHbt txnId={}. skipping send.", txnId);
                    return;
                }

                // preserve insertion order
                for (Map.Entry<String, String> e : new LinkedHashMap<>(originsMap).entrySet()) {
                    String orgId = e.getKey();
                    String base  = e.getValue();
                    if (base == null || base.isBlank()) continue;

                    String respUrl = base.endsWith("/") ? base + "upi/RespHbt/2.0/urn:txnid:" + txnId
                            : base + "/upi/RespHbt/2.0/urn:txnid:" + txnId;

                    // persist outbound
                    try { heartbeatService.persistOutboundRespHbt(txnId, orgId, respXml); } catch (Exception ignore) {}

                    upiLogUtil.logOutbound("RespHbt", respUrl, respXml, txnId);
                    log.info("Posting RespHbt txnId='{}' to {} (org={})", txnId, respUrl, orgId);

                    Optional<String> maybeAck = postWithRetryReturnBody(respUrl, respXml, 3, 1000L);
                    if (maybeAck.isPresent()) {
                        String ackBody = maybeAck.get();

                        boolean success = false;
                        try {
                            Ack ackDto = XmlUtil.fromXmlWithoutXsd(ackBody, Ack.class);
                            success = (ackDto.getErr() == null || ackDto.getErr().isBlank());
                        } catch (Exception ex) {
                            // if cannot parse, treat as failure (tune if needed)
                            success = false;
                        }

                        try { heartbeatService.persistAck(txnId, orgId, ackBody, success); } catch (Exception ignore) {}
                        if (success) {
                            log.info("RespHbt ACK success from org={} url={}", orgId, base);
                        } else {
                            log.warn("RespHbt ACK indicated failure from org={} url={}", orgId, base);
                        }
                    } else {
                        try { heartbeatService.markNodeFailure(orgId); } catch (Exception ignore) {}
                        log.error("RespHbt failed for txnId='{}' -> org={} url={}", txnId, orgId, base);
                    }
                }

                txnOriginRegistry.remove(txnId);
            } catch (Exception e) {
                log.error("Failed to process ReqHbt (send RespHbt) for txnId=" + txnId, e);
            }
        });
    }

    public void sendAckToPayerSyncByOrg(ReqHbt reqHbt, String normalizedTxnId, String ackXml, String payerOrg) {
        if (payerOrg == null || payerOrg.isBlank()) {
            log.warn("sendAckToPayerSyncByOrg called with null/empty payerOrg for txn {}", normalizedTxnId);
            return;
        }
        try {
            String payerBase = routingService.findBankUrlByOrgIdOrNull(payerOrg);
            if (payerBase == null) {
                Map<String, String> origins = txnOriginRegistry.getOrigins(normalizedTxnId);
                if (origins != null && origins.containsKey(payerOrg.toLowerCase())) {
                    payerBase = origins.get(payerOrg.toLowerCase());
                }
            }
            if (payerBase == null) {
                log.warn("No base URL for payerOrg={} txn={} - skipping sync ack", payerOrg, normalizedTxnId);
                return;
            }
            String payerAckUrl = payerBase.endsWith("/") ? payerBase + "upi/Ack/2.0/urn:txnid:" + normalizedTxnId
                    : payerBase + "/upi/Ack/2.0/urn:txnid:" + normalizedTxnId;

            upiLogUtil.logOutbound("AckToPayer", payerAckUrl, ackXml, normalizedTxnId);

            RestTemplate rt = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(ackXml, headers);

            try {
                ResponseEntity<String> resp = rt.postForEntity(payerAckUrl, entity, String.class);
                upiLogUtil.logAck(payerAckUrl, resp.getBody(), normalizedTxnId);
                log.info("Sync ack posted to payerOrg={} txn={} status={}", payerOrg, normalizedTxnId, resp.getStatusCodeValue());
            } catch (Exception ex) {
                log.warn("Failed sync ack to payerOrg={} txn={} url={} : {}", payerOrg, normalizedTxnId, payerAckUrl, ex.getMessage());
            }
        } catch (Exception e) {
            log.error("Exception in sendAckToPayerSyncByOrg for txn=" + normalizedTxnId, e);
        }
    }

    // ---- helpers ----

    private Optional<String> postWithRetryReturnBody(String url, String xml, int maxAttempts, long initialWaitMs) {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(xml, headers);

        int attempt = 0;
        long wait = initialWaitMs;
        while (attempt < maxAttempts) {
            try {
                ResponseEntity<String> ackResponse = rt.postForEntity(url, entity, String.class);
                upiLogUtil.logAck(url, ackResponse.getBody(), null);
                if (ackResponse.getStatusCode().is2xxSuccessful()) {
                    return Optional.ofNullable(ackResponse.getBody());
                } else if (ackResponse.getStatusCode().is5xxServerError()) {
                    attempt++;
                } else {
                    return Optional.empty();
                }
            } catch (ResourceAccessException rae) {
                attempt++;
                log.warn("Attempt {}/{}: connection error posting to {} : {}. Retrying in {}ms",
                        attempt, maxAttempts, url, rae.getMessage(), wait);
            } catch (HttpStatusCodeException hse) {
                log.error("HTTP {} posting to {}: {}", hse.getRawStatusCode(), url, hse.getResponseBodyAsString());
                if (hse.getStatusCode().is5xxServerError()) {
                    attempt++;
                } else {
                    return Optional.empty();
                }
            } catch (Exception e) {
                log.error("Unexpected error posting to {}: {}", url, e.getMessage(), e);
                return Optional.empty();
            }

            try { Thread.sleep(wait); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return Optional.empty(); }
            wait *= 2;
        }
        return Optional.empty();
    }

    private RespHbt buildRespHbt(ReqHbt reqHbt) {
        RespHbt respHbt = new RespHbt();
        Head head = new Head();
        head.setMsgId("HBT" + UUID.randomUUID().toString().replace("-", "").substring(0, 29));
        head.setOrgId("NPCI");
        head.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        head.setVer("2.0");
        respHbt.setHead(head);

        respHbt.setTxn(reqHbt.getTxn());

        Resp resp = new Resp();
        resp.setResult("SUCCESS");
        resp.setErrCode("");
        resp.setReqMsgId(reqHbt.getHead() != null ? reqHbt.getHead().getMsgId() : "");
        respHbt.setResp(resp);
        return respHbt;
    }

    private String normalizeTxnId(String t) {
        if (t == null) return null;
        String s = t.trim();
        if (s.startsWith("urn:txnid:")) s = s.substring("urn:txnid:".length());
        return s;
    }
}
