package com.remitter.RemitterBank.controller;

import com.remitter.RemitterBank.dto.Ack;
import com.remitter.RemitterBank.dto.ReqPay;
import com.remitter.RemitterBank.service.RespPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/upi")
public class RemitterController {

    private final RespPayService respPayService;

    public RemitterController(RespPayService respPayService) {
        this.respPayService = respPayService;
    }

    // Step 1: Accept ReqPay from UPI
    @PostMapping(
            value = "/ReqPay/2.0/urn:txnid:{txnId}",
            consumes = "application/xml",
            produces = "application/xml"
    )
    public ResponseEntity<Ack> receiveReqPay(@PathVariable String txnId,
                                             @RequestBody ReqPay reqPay) {
        // Async debit processing
        CompletableFuture.runAsync(() -> respPayService.processReqPay(reqPay, txnId));

        // Sync Ack
        Ack ack = new Ack();
        ack.setApi("ReqPay");
        ack.setReqMsgId(reqPay.getHead().getMsgId());
        ack.setTs(OffsetDateTime.now().toString());
        return ResponseEntity.ok(ack);
    }
}


