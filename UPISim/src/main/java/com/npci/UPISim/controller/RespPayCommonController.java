package com.npci.UPISim.controller;

import com.npci.UPISim.dto.Ack;
import com.npci.UPISim.dto.RespPay;
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
public class RespPayCommonController {

    private static final Logger log = LoggerFactory.getLogger(RespPayCommonController.class);

    private final TransactionCoordinator coordinator;
    private final UPILogUtil upiLogUtil; // ✅ inject UPILogUtil bean

    public RespPayCommonController(TransactionCoordinator coordinator, UPILogUtil upiLogUtil) {
        this.coordinator = coordinator;
        this.upiLogUtil = upiLogUtil;
    }

    @PostMapping(
            value = "/RespPay/2.0/urn:txnid:{txnId}",
            consumes = "application/xml",
            produces = "application/xml"
    )
    public ResponseEntity<Ack> receiveRespPay(@PathVariable String txnId,
                                              @RequestBody RespPay resp,
                                              HttpServletRequest request) {
        try {
            // 🔹 Convert inbound RespPay to XML string for traffic log
            String rawXml = XmlUtil.toXml(resp, RespPay.class);
            upiLogUtil.logInbound("RespPay", request.getRequestURI(), rawXml, txnId);

            // --- Ensure context exists ---
            TransactionContext ctx = TransactionStore.get(txnId);
            if (ctx == null) {
                ctx = new TransactionContext(txnId);
                TransactionStore.put(txnId, ctx);
            }

            // --- Capture type from Txn ---
            String type = resp.getTxn().getType(); // DEBIT / CREDIT / PAY
            String result = resp.getResp().getResult();

            switch (type) {
                case "DEBIT" -> {
                    ctx.setRespPayDebit(resp);
                    coordinator.markEvent(txnId, TransactionCoordinator.RESP_PAY_DEBIT, result);
                    log.info("RespPay DEBIT received, txnId={}, result={}", txnId, result);
                }
                case "CREDIT" -> {
                    ctx.setRespPayCredit(resp);
                    coordinator.markEvent(txnId, TransactionCoordinator.RESP_PAY_CREDIT, result);
                    log.info("RespPay CREDIT received, txnId={}, result={}", txnId, result);
                }
                case "PAY" -> {
                    ctx.setFinalRespPay(resp);
                    coordinator.markEvent(txnId, TransactionCoordinator.FINAL_RESP_PAY, result);
                    log.info("Final RespPay (PAY) received, txnId={}, result={}", txnId, result);
                }
                default -> {
                    upiLogUtil.logInbound("RespPay", request.getRequestURI(),
                            "Unknown RespPay type: " + type, txnId);
                    log.warn("Unknown RespPay type={}, txnId={}", type, txnId);
                }
            }

            // --- Always return Ack ---
            Ack ack = new Ack();
            ack.setApi("RespPay");
            ack.setReqMsgId(resp.getResp().getReqMsgId());
            ack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            // 🔹 Log Ack
            String ackXml = XmlUtil.toXml(ack, Ack.class);
            upiLogUtil.logAck(request.getRequestURI(), ackXml, txnId);

            log.info("Ack sent for RespPay, txnId={}, reqMsgId={}", txnId, ack.getReqMsgId());

            return ResponseEntity.ok(ack);

        } catch (Exception e) {
            upiLogUtil.logInbound("RespPay", request.getRequestURI(),
                    "Failed to process RespPay: " + e.getMessage(), txnId);
            log.error("Failed to process RespPay for txnId={}", txnId, e);
            throw new RuntimeException(e);
        }
    }
}
