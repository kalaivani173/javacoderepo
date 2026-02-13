package com.npci.UPISim.service;

import com.npci.UPISim.dto.*;
import com.npci.UPISim.util.UPILogUtil;
import com.npci.UPISim.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FinalRespPayService {

    private static final Logger log = LoggerFactory.getLogger(FinalRespPayService.class);

    private final UPILogUtil upiLogUtil;
    private final TransactionLogService txnService;

    public FinalRespPayService(UPILogUtil upiLogUtil, TransactionLogService txnService) {
        this.upiLogUtil = upiLogUtil;
        this.txnService = txnService;
    }

    public RespPay buildFinalRespPay(String txnId, RespPay debitResp, RespPay creditResp) {
        if (debitResp == null) {
            throw new IllegalStateException("Debit RespPay is missing for txnId=" + txnId);
        }
        if (creditResp == null) {
            throw new IllegalStateException("Credit RespPay is missing for txnId=" + txnId);
        }

        RespPay finalResp = new RespPay();
        finalResp.setHead(debitResp.getHead());
        finalResp.setTxn(debitResp.getTxn());

        Resp resp = new Resp();
        resp.setReqMsgId(debitResp.getResp().getReqMsgId());
        resp.setResult("SUCCESS");

        List<Ref> refs = new ArrayList<>();
        if (debitResp.getResp() != null && debitResp.getResp().getRefs() != null) {
            refs.addAll(debitResp.getResp().getRefs());
        }
        if (creditResp.getResp() != null && creditResp.getResp().getRefs() != null) {
            refs.addAll(creditResp.getResp().getRefs());
        }
        resp.setRefs(refs);

        finalResp.setResp(resp);
        return finalResp;
    }

    public void sendFinalRespPay(String txnId, RespPay debitResp, RespPay creditResp) {
        CompletableFuture.runAsync(() -> {
            String normalized = normalizeTxnId(txnId);
            try {
                RespPay finalResp = buildFinalRespPay(normalized, debitResp, creditResp);
                String xmlPayload = XmlUtil.toXml(finalResp, RespPay.class);

                String url = "http://localhost:8080/upi/RespPay/2.0/urn:txnid:" + normalized;

                RestTemplate rest = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_XML);
                HttpEntity<String> request = new HttpEntity<>(xmlPayload, headers);

                // log outbound and send
                upiLogUtil.logOutbound("FinalRespPay", url, xmlPayload, normalized);
                String response = rest.postForEntity(url, request, String.class).getBody();
                upiLogUtil.logAck(url, response, normalized);

                log.info("Final RespPay sent successfully for txnId={}, response={}", normalized, response);

                // determine result and update txn status in DB
                String result = finalResp != null && finalResp.getResp() != null ? finalResp.getResp().getResult() : null;
                if ("SUCCESS".equalsIgnoreCase(result)) {
                    log.info("Marking txn {} as SUCCESS", normalized);
                    txnService.updateTxnStatus(normalized, "SUCCESS");
                } else {
                    log.info("Marking txn {} as FAILURE (result={})", normalized, result);
                    txnService.updateTxnStatus(normalized, "FAILURE");
                }

            } catch (Exception e) {
                log.error("Failed to send Final RespPay for txnId={}", normalized, e);
                try {
                    txnService.updateTxnStatus(normalized, "FAILURE");
                } catch (Exception ex) {
                    log.error("Also failed to update txn status for txnId={}", normalized, ex);
                }
            }
        });
    }

    private String normalizeTxnId(String maybe) {
        if (maybe == null) return null;
        String t = maybe.trim();
        if (t.startsWith("urn:txnid:")) return t.substring("urn:txnid:".length());
        int idx = t.indexOf("urn:txnid:");
        if (idx >= 0) return t.substring(idx + "urn:txnid:".length());
        return t;
    }

}
