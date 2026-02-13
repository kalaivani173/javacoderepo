package com.npci.UPISim.controller;

import com.npci.UPISim.dto.Ack;
import com.npci.UPISim.dto.RespAuthDetails;
import com.npci.UPISim.service.TransactionCoordinator;
import com.npci.UPISim.util.TransactionContext;
import com.npci.UPISim.util.TransactionStore;
import com.npci.UPISim.util.UPILogUtil;
import com.npci.UPISim.util.XmlUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/upi")
public class RespAuthController {

    private static final Logger log = LoggerFactory.getLogger(RespAuthController.class);

    private final TransactionCoordinator coordinator;
    private final UPILogUtil upiLogUtil; // ✅ Inject UPILogUtil bean

    public RespAuthController(TransactionCoordinator coordinator, UPILogUtil upiLogUtil) {
        this.coordinator = coordinator;
        this.upiLogUtil = upiLogUtil;
    }

    @PostMapping(
            value = "/RespAuthDetails/{version}/{txnId}",
            consumes = "application/xml",
            produces = "application/xml"
    )
    public ResponseEntity<Ack> receiveRespAuthDetails(
            @PathVariable String version,
            @PathVariable String txnId,
            @RequestBody RespAuthDetails respAuthDetails,
            HttpServletRequest request) {

        try {
            // 🔹 Convert inbound RespAuthDetails to XML for logs
            String rawXml = XmlUtil.toXml(respAuthDetails, RespAuthDetails.class);
            upiLogUtil.logInbound("RespAuthDetails", request.getRequestURI(), rawXml, txnId);

            // 🔹 Update transaction context
            TransactionContext ctx = TransactionStore.get(txnId);
            if (ctx == null) {
                ctx = new TransactionContext(txnId);
                TransactionStore.put(txnId, ctx);
            }
            ctx.setRespAuthDetails(respAuthDetails);

            // 🔹 Mark event in coordinator
            String result = respAuthDetails.getResp().getResult();
            coordinator.markEvent(txnId, TransactionCoordinator.RESP_AUTH, result);

            // 🔹 Build Ack
            Ack ack = new Ack();
            ack.setApi("RespAuthDetails");
            ack.setReqMsgId(respAuthDetails.getResp().getReqMsgId());
            ack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            // 🔹 Log Ack (traffic + application log)
            String ackXml = XmlUtil.toXml(ack, Ack.class);
            upiLogUtil.logAck(request.getRequestURI(), ackXml, txnId);

            log.info("[RespAuthController] Ack sent for txnId={}, reqMsgId={}", txnId, ack.getReqMsgId());

            return ResponseEntity.ok(ack);

        } catch (Exception e) {
            upiLogUtil.logInbound("RespAuthDetails", request.getRequestURI(),
                    "Failed to process RespAuthDetails: " + e.getMessage(), txnId);
            log.error("[RespAuthController] Failed to process RespAuthDetails for txnId={}", txnId, e);
            throw new RuntimeException(e);
        }
    }
}
