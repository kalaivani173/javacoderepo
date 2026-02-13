package com.npci.UPISim.controller;

import com.npci.UPISim.dto.Ack;
import com.npci.UPISim.dto.ReqHbt;
import com.npci.UPISim.service.HeartbeatService;
import com.npci.UPISim.service.PSPBankService;
import com.npci.UPISim.service.ReqHbtService;
import com.npci.UPISim.service.RoutingService;
import com.npci.UPISim.util.TxnOriginRegistry;
import com.npci.UPISim.util.UPILogUtil;
import com.npci.UPISim.util.XmlUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/upi")
public class ReqHbtController {

    private static final Logger log = LoggerFactory.getLogger(ReqHbtController.class);

    private final ReqHbtService reqHbtService;
    private final HeartbeatService heartbeatService;
    private final UPILogUtil upiLogUtil;
    private final TxnOriginRegistry txnOriginRegistry;
    private final RoutingService routingService;
    private final PSPBankService pspBankService;

    public ReqHbtController(ReqHbtService reqHbtService,
                            UPILogUtil upiLogUtil,
                            TxnOriginRegistry txnOriginRegistry,
                            RoutingService routingService,
                            PSPBankService pspBankService,
                            HeartbeatService heartbeatService) {
        this.reqHbtService = reqHbtService;
        this.upiLogUtil = upiLogUtil;
        this.txnOriginRegistry = txnOriginRegistry;
        this.routingService = routingService;
        this.pspBankService = pspBankService;
        this.heartbeatService = heartbeatService;
    }

    @PostMapping(value = "/ReqHbt/2.0/urn:txnid:{txnId}",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Ack> receiveReqHbt(
            @PathVariable String txnId,
            @RequestBody String rawXml,            // raw body string
            HttpServletRequest request) {

        String normalizedTxn = normalizeTxnId(txnId);
        ReqHbt reqHbt = null;

        try {
            // 1) log incoming headers & raw payload (exact bytes/text received)
            log.debug("Incoming headers: X-Origin-Org={}, X-Origin-Role={}, X-Payer-Org={}, Host={}",
                    request.getHeader("X-Origin-Org"),
                    request.getHeader("X-Origin-Role"),
                    request.getHeader("X-Payer-Org"),
                    request.getHeader("Host"));
            log.debug("Raw ReqHbt payload:\n{}", rawXml);

            // 2) unmarshal raw XML into DTO using XmlUtil
            try {
                reqHbt = XmlUtil.fromXmlWithoutXsd(rawXml, ReqHbt.class);
            } catch (Exception e) {
                log.error("Failed to parse ReqHbt XML for txnId={}: {}", normalizedTxn, e.getMessage());
                Ack ack = buildErrorAck("ReqHbt", null, "INVALID_PAYLOAD");
                try { upiLogUtil.logAck(request.getRequestURI(), XmlUtil.toXml(ack, Ack.class), normalizedTxn); } catch (Exception ex) { log.debug("failed to log ack xml: {}", ex.getMessage()); }
                return ResponseEntity.ok(ack);
            }

            log.debug("Parsed ReqHbt Head.orgId = {}",
                    reqHbt != null && reqHbt.getHead() != null ? reqHbt.getHead().getOrgId() : "null");

            // log inbound using the pretty-to-xml (same as earlier behavior)
            try {
                String inboundXml = XmlUtil.toXml(reqHbt, ReqHbt.class);
                upiLogUtil.logInbound("ReqHbt", request.getRequestURI(), inboundXml, normalizedTxn);
            } catch (Exception e) {
                log.warn("Unable to re-marshall ReqHbt for logging: {}", e.getMessage());
            }

            // --- Determine origin org and role (headers override head) ---
            String overrideOrg = request.getHeader("X-Origin-Org");
            String overrideRole = request.getHeader("X-Origin-Role");
            String headOrg = (reqHbt.getHead() != null) ? reqHbt.getHead().getOrgId() : null;

            String registeringOrg = (overrideOrg != null && !overrideOrg.isBlank()) ? overrideOrg.trim() : headOrg;
            String registeringRole = (overrideRole != null && !overrideRole.isBlank())
                    ? overrideRole.trim().toUpperCase()
                    : "OTHER";

            if (registeringOrg == null || registeringOrg.isBlank()) {
                log.error("ReqHbt missing orgId (no header or Head.orgId). txn={}", normalizedTxn);
                Ack ack = buildErrorAck("ReqHbt", reqHbt, "PSP NOT REGISTERED");
                try { upiLogUtil.logAck(request.getRequestURI(), XmlUtil.toXml(ack, Ack.class), normalizedTxn); } catch (Exception ex) { log.debug("failed to log ack xml: {}", ex.getMessage()); }
                return ResponseEntity.ok(ack);
            }

            try {
                heartbeatService.persistInboundReqHbt(normalizedTxn, registeringOrg, rawXml);
            } catch (Exception ex) {
                log.warn("Failed to persist inbound ReqHbt for txn {} org {} : {}", normalizedTxn, registeringOrg, ex.getMessage());
            }

            // --- Resolve DB bank URL (if present) ---
            String dbBankUrl = routingService.findBankUrlByOrgIdOrNull(registeringOrg);
            if (dbBankUrl == null) {
                log.warn("No routing entry for orgId={} (txn={}), using incoming URL instead", registeringOrg, normalizedTxn);
            } else {
                // record heartbeat against PSP record
                pspBankService.recordHeartbeat(registeringOrg);
                log.info("Recorded heartbeat for orgId={} (txnId={})", registeringOrg, normalizedTxn);
            }

            // --- Register origin (uses current 4-arg registerOrigin) ---
            String incomingBaseUrl = getRequestBaseUrl(request);
            txnOriginRegistry.registerOrigin(normalizedTxn, registeringOrg, dbBankUrl, incomingBaseUrl);
            log.info("Registered origin for txnId={} orgId={} role={} -> {} (dbUsed={})",
                    normalizedTxn, registeringOrg, registeringRole,
                    (dbBankUrl != null ? dbBankUrl : incomingBaseUrl), dbBankUrl != null);
            log.debug("TxnOriginRegistry snapshot: {}", txnOriginRegistry.snapshot());

            // --- Build ack for caller and log it ---
            Ack success = buildSuccessAck("ReqHbt", reqHbt);
            String successXml = XmlUtil.toXml(success, Ack.class);
            upiLogUtil.logAck(request.getRequestURI(), successXml, normalizedTxn);

            // --- Resolve payerOrg (for sync ack) from header or registry fallback ---
            String payerOrg = request.getHeader("X-Payer-Org");
            if (payerOrg == null || payerOrg.isBlank()) {
                Map<String, String> origins = txnOriginRegistry.getOrigins(normalizedTxn);
                if (origins != null && origins.size() == 1) {
                    payerOrg = origins.keySet().iterator().next();
                }
            }

            if (payerOrg != null && !payerOrg.equalsIgnoreCase(registeringOrg)) {
                // send sync ack to payer (non-blocking inside service method)
                reqHbtService.sendAckToPayerSyncByOrg(reqHbt, normalizedTxn, successXml, payerOrg);
            }

            // --- Trigger async RespHbt (use existing single-arg method for compatibility) ---
            reqHbtService.processReqHbt(reqHbt);

            return ResponseEntity.ok(success);

        } catch (Exception e) {
            log.error("Exception processing ReqHbt txnId={}", txnId, e);
            Ack ack = new Ack();
            ack.setApi("ReqHbt");
            // safe guard if reqHbt is null
            String reqMsgId = null;
            if (reqHbt != null && reqHbt.getHead() != null) reqMsgId = reqHbt.getHead().getMsgId();
            ack.setReqMsgId(reqMsgId);
            ack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            return ResponseEntity.internalServerError().body(ack);
        }
    }

    private String getRequestBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        String host = request.getHeader("X-Forwarded-Host");
        if (host != null && !host.isBlank()) {
            return (scheme == null ? request.getScheme() : scheme) + "://" + host;
        }
        // fallback to Host header or serverName/serverPort
        String hostHeader = request.getHeader("Host");
        if (hostHeader != null && hostHeader.contains(":")) {
            return request.getScheme() + "://" + hostHeader;
        } else if (hostHeader != null) {
            return request.getScheme() + "://" + hostHeader + ":" + request.getServerPort();
        } else {
            return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        }
    }

    private Ack buildSuccessAck(String api, ReqHbt reqHbt) {
        Ack ack = new Ack();
        ack.setApi(api);
        ack.setReqMsgId(reqHbt != null && reqHbt.getHead() != null ? reqHbt.getHead().getMsgId() : null);
        ack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return ack;
    }

    private Ack buildErrorAck(String api, ReqHbt reqHbt, String errMsg) {
        Ack ack = new Ack();
        ack.setApi(api);
        ack.setReqMsgId(reqHbt != null && reqHbt.getHead() != null ? reqHbt.getHead().getMsgId() : null);
        ack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        ack.setErr(errMsg);
        return ack;
    }

    private String normalizeTxnId(String t) {
        if (t == null) return null;
        return t.startsWith("urn:txnid:") ? t.substring("urn:txnid:".length()) : t;
    }
}
