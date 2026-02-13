package com.Bene.BeneficiaryBank.service;

import com.Bene.BeneficiaryBank.dto.ReqPay;
import com.Bene.BeneficiaryBank.util.RespPayBuilder;
import com.Bene.BeneficiaryBank.util.UpiClient;
import com.Bene.BeneficiaryBank.util.XmlUtils;
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
                        System.out.println("✅ RespPay Credit sent successfully for txnId=" + txnId);

                    } catch (Exception e) {
                        System.err.println("Failed to send RespPay Credit for txnId=" + txnId + ": " + e.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error processing RespPay Credit for txnId=" + txnId + ": " + ex.getMessage());
                    return null;
                });
    }
}

