package com.payer.PayerPSP.controller;



import com.npci.UPISim.dto.Ack;
import com.payer.PayerPSP.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/upi")
public class FinalRespPayController {

    @PostMapping(
            value = "/RespPay/2.0/urn:txnid:{txnId}",
            consumes = "application/xml",
            produces = "application/xml"
    )
    public ResponseEntity<Ack> receiveFinalRespPay(
            @PathVariable String txnId,
            @RequestBody RespPay finalResp
    ) {
        System.out.println("✅ [Payer PSP] Final RespPay received for txnId=" + txnId);
        System.out.println(finalResp);

        // --- Extract key info ---
        String result = finalResp.getResp().getResult();
        String payerAddr = (finalResp.getTxn() != null) ? finalResp.getTxn().getId() : "NA";

        System.out.println("   Result = " + result);
        System.out.println("   TxnId  = " + payerAddr);

        // --- Build Ack response ---
        Ack ack = new Ack();
        ack.setApi("RespPay");
        ack.setReqMsgId(finalResp.getResp().getReqMsgId());
        ack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        return ResponseEntity.ok(ack);
    }
}

