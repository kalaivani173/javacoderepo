package com.payee.psp.controller;


import com.payee.psp.dto.Ack;
import com.payee.psp.dto.ReqAuthDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import com.payee.psp.service.RespAuthDetailsService;

@RestController
@RequestMapping("/upi")
public class AuthDetailsController {

    private final RespAuthDetailsService respAuthDetailsService;

    public AuthDetailsController(RespAuthDetailsService respAuthDetailsService) {
        this.respAuthDetailsService = respAuthDetailsService;
    }


    @PostMapping(
            value = "/ReqAuthDetails/2.0/urn:txnid:{txnId}",
            consumes = "application/xml",
            produces = "application/xml"
    )
    public ResponseEntity<Ack> receiveReqAuthDetails(

            @PathVariable String txnId,
            @RequestBody ReqAuthDetails req) {

        // ✅ Async processing of ReqAuthDetails
        CompletableFuture.runAsync(() -> respAuthDetailsService.processReqAuthDetails(req, txnId));

        // ✅ Synchronous Ack back to UPI
        Ack ack = new Ack();
        ack.setApi("ReqAuthDetails");
        ack.setReqMsgId(req.getHead().getMsgId());
        ack.setTs(OffsetDateTime.now().toString());
        return ResponseEntity.ok(ack);
    }
}
