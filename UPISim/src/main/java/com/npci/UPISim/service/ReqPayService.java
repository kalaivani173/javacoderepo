package com.npci.UPISim.service;

import com.npci.UPISim.dto.*;
import com.npci.UPISim.util.UPILogUtil;
import com.npci.UPISim.util.XmlUtil;
import com.npci.UPISim.validation.ReqPayValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ReqPayService {

    private static final Logger log = LoggerFactory.getLogger(ReqPayService.class);

    private final TransactionCoordinator coordinator;
    private final UPILogUtil upiLogUtil;
    private final RoutingService routingService;

    public ReqPayService(TransactionCoordinator coordinator,
                         UPILogUtil upiLogUtil,
                         RoutingService routingService) {
        this.coordinator = coordinator;
        this.upiLogUtil = upiLogUtil;
        this.routingService = routingService;
    }

    @Transactional
    public String processReqPay(ReqPay reqPay) {

        String txnId = reqPay.getTxn().getId();

        // 1️⃣ Log inbound
        String inboundXml = XmlUtil.toXml(reqPay, ReqPay.class);
        upiLogUtil.logInbound(
                "ReqPay",
                "/upi/ReqPay/2.0/urn:txnid:" + txnId,
                inboundXml,
                txnId
        );

        // 2️⃣ Validate
        String validationError = ReqPayValidator.validate(reqPay);

        // 3️⃣ Build base ACK
        Ack ack = new Ack();
        ack.setApi("ReqPay");
        ack.setReqMsgId(reqPay.getHead().getMsgId());
        ack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        // 🔴 NEGATIVE ACK FLOW
        if (validationError != null) {

            ack.setErr(validationError);
            String nackXml = XmlUtil.toXml(ack, Ack.class);

            // store txn as failed
            coordinator.markEvent(txnId, "ReqPay", "FAILED_VALIDATION");

            // store ack
            upiLogUtil.logAck("ReqPay", nackXml, txnId);

            return nackXml; // ✅ HTTP 200
        }

        // 🟢 POSITIVE FLOW
        coordinator.markEvent(txnId, "ReqPay", "RECEIVED");

        String ackXml = XmlUtil.toXml(ack, Ack.class);
        upiLogUtil.logAck("ReqPay", ackXml, txnId);

        triggerReqAuthAsync(reqPay, txnId);

        return ackXml;
    }

    // ---------------- ASYNC DOWNSTREAM ----------------

    private void triggerReqAuthAsync(ReqPay reqPay, String txnId) {

        ReqAuthDetails authDetails = buildReqAuthDetails(reqPay);
        String authXml = XmlUtil.toXml(authDetails, ReqAuthDetails.class);

        CompletableFuture.runAsync(() -> {
            try {
                String payeeHandle = reqPay.getPayees().get(0).getAddr();
                String handle = payeeHandle.substring(payeeHandle.indexOf("@") + 1);

                String bankUrl = routingService.getBankUrlByHandle(handle);

                String endpoint =
                        bankUrl + "/upi/ReqAuthDetails/2.0/urn:txnid:" + txnId;

                String ack = postToPayeePSP(authXml, endpoint);

                upiLogUtil.logOutbound("ReqAuthDetails", endpoint, authXml, txnId);
                upiLogUtil.logAck(endpoint, ack, txnId);

                coordinator.markEvent(txnId, "ReqAuthDetails", "TRIGGERED");

            } catch (Exception e) {
                coordinator.markEvent(txnId, "ReqAuthDetails", "FAILED");
                log.error("ReqAuthDetails failed txnId={}", txnId, e);
            }
        });
    }

    private String postToPayeePSP(String xml, String url) {

        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        HttpEntity<String> req = new HttpEntity<>(xml, headers);
        return rt.postForEntity(url, req, String.class).getBody();
    }

    private ReqAuthDetails buildReqAuthDetails(ReqPay reqPay) {

        ReqAuthDetails auth = new ReqAuthDetails();

        Txn txn = reqPay.getTxn();
        auth.setTxn(txn);
        auth.setPayer(reqPay.getPayer());
        auth.setPayees(reqPay.getPayees());

        Head head = new Head();
        head.setMsgId("UPI" + UUID.randomUUID().toString().replace("-", "").substring(0, 29));
        head.setOrgId("NPCI");
        head.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        head.setVer("2.0");

        auth.setHead(head);
        return auth;
    }
}
