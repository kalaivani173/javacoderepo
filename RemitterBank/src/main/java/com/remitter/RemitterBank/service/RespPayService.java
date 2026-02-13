package com.remitter.RemitterBank.service;

import com.remitter.RemitterBank.dto.ReqPay;
import com.remitter.RemitterBank.util.RespPayBuilder;
import com.remitter.RemitterBank.util.UpiClient;
import com.remitter.RemitterBank.util.UpiClient;
import com.remitter.RemitterBank.util.XmlUtils;
import org.springframework.stereotype.Service;



import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class RespPayService {

    private final UpiClient upiClient;

    public RespPayService(UpiClient upiClient) {
        this.upiClient = upiClient;
    }

    /**
     * Process final RespPay for a transaction.
     * Success depends on all other API stages (ReqAuth, RespAuth, etc.)
     */
    public void processReqPay(ReqPay reqPay, String txnId) {
        CompletableFuture
                .supplyAsync(() -> RespPayBuilder.build(reqPay, txnId)) // build RespPay object
                .thenAccept(respPay -> {
                    try {
                        // Log XML for debugging
                        String xml = XmlUtils.toXml(respPay);
                        System.out.println("Generated RespPay XML for txnId=" + txnId + ":\n" + xml);

                        // Send to UPI Switch
                        upiClient.sendRespPay(respPay, txnId);
                        System.out.println("✅ RespPay sent successfully for txnId=" + txnId);

                    } catch (Exception e) {
                        System.err.println("Failed to send RespPay for txnId=" + txnId + ": " + e.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error processing RespPay for txnId=" + txnId + ": " + ex.getMessage());
                    return null;
                });
    }
}

