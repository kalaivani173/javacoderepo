package com.payee.psp.service;

import com.payee.psp.dto.ReqAuthDetails;
import com.payee.psp.util.RespAuthDetailsBuilder;
import com.payee.psp.util.UPIClient;
import com.payee.psp.util.XmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class RespAuthDetailsService {

    @Autowired
    private final UPIClient upiClient;

    public RespAuthDetailsService(UPIClient upiClient) {
        this.upiClient = upiClient;
    }

    /**
     * Process ReqAuthDetails from UPI and send RespAuthDetails back asynchronously
     */
    public void processReqAuthDetails(ReqAuthDetails req, String txnId) {
        CompletableFuture
                .supplyAsync(() -> RespAuthDetailsBuilder.build(req, txnId)) // build response
                .thenAccept(resp -> {
                    try {
                        System.out.println(XmlUtils.toXml(resp));
                        upiClient.sendRespAuthDetails(resp, txnId);
                        System.out.println("RespAuthDetails sent for txnId=" + txnId);
                    } catch (Exception e) {
                        System.err.println("Failed to send RespAuthDetails for txnId=" + txnId + ": " + e.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error processing ReqAuthDetails for txnId=" + txnId + ": " + ex.getMessage());
                    return null;
                });
    }
}
