package com.payee.psp.service;

import com.payee.psp.dto.*;
import com.payee.psp.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PayeeHeartbeatSender {

    private static final Logger log = LoggerFactory.getLogger(PayeeHeartbeatSender.class);

    private final HbtService hbtService;
    private final RestTemplate restTemplate;

    // e.g. http://localhost:8081/upi/ReqHbt/2.0/urn:txnid:
    @Value("${upi.switch.reqhbt.base:http://localhost:8081/upi/ReqHbt/2.0/urn:txnid:}")
    private String upiReqHbtBase;

    // this should be the orgId you registered in psp_bank table for this Payer
    @Value("${payer.orgId:157777}")
    private String payerOrgId;

    public PayeeHeartbeatSender(HbtService hbtService, RestTemplate restTemplate) {
        this.hbtService = hbtService;
        this.restTemplate = restTemplate;
    }

    // runs every 3 minutes (180000 ms)
    @Scheduled(fixedRateString = "${payer.hbt.interval.ms:180000}", initialDelay = 10000)
    public void sendHeartbeat() {
        try {
            // createReqHbt should set Head.orgId - ensure HbtService does that (see note below)
            ReqHbt req = hbtService.createReqHbt("AUTO"); // or pass custRef if needed
            // ensure head.orgId present (override if necessary)
            if (req.getHead() != null) {
                req.getHead().setOrgId(payerOrgId);
            }
            String xml = XmlUtils.toXml(req, ReqHbt.class);
            String url = upiReqHbtBase + req.getTxn().getId();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<String> entity = new HttpEntity<>(xml, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
            log.info("Sent ReqHbt txnId={} to UPI; Ack status={} body={}", req.getTxn().getId(), resp.getStatusCodeValue(), resp.getBody());
        } catch (Exception e) {
            log.error("Failed to send scheduled ReqHbt: {}", e.getMessage(), e);
        }
    }
}
