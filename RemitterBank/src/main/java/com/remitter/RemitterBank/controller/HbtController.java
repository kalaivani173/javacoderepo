package com.remitter.RemitterBank.controller;

import com.remitter.RemitterBank.service.*;
import com.remitter.RemitterBank.dto.*;
import com.remitter.RemitterBank.util.*;
import com.remitter.RemitterBank.util.XmlPrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/upi")
public class HbtController {

    private static final Logger log = LoggerFactory.getLogger(HbtController.class);

    @Autowired
    private HbtService hbtService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping(value = "/Hbt", produces = MediaType.APPLICATION_XML_VALUE)
    public String createReqHbt(@RequestParam(name = "custRef", required = false, defaultValue = "NA") String custRef) throws Exception {
        // build ReqHbt XML
        ReqHbt reqHbt = hbtService.createReqHbt(custRef);
        String reqHbtXml = XmlUtils.toXml(reqHbt, ReqHbt.class);
        String txnId = reqHbt.getTxn().getId();

        String upiSwitchUrl = "http://localhost:8081/upi/ReqHbt/2.0/urn:txnid:" + txnId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        HttpEntity<String> entity = new HttpEntity<>(reqHbtXml, headers);

        try {
            // post to switch
            ResponseEntity<String> ackResponse = restTemplate.postForEntity(upiSwitchUrl, entity, String.class);
            return ackResponse.getBody();
        } catch (HttpServerErrorException se) {
            // log server 500 body for easier debugging
            String serverBody = se.getResponseBodyAsString();
            log.error("UPI switch returned HTTP 5xx for ReqHbt to {}. Body: {}", upiSwitchUrl, serverBody);
            throw se; // rethrow or wrap if you want a different behavior
        } catch (Exception e) {
            log.error("Failed to post ReqHbt to {}: {}", upiSwitchUrl, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Receive the asynchronous RespHbt from UPI.
     * This method logs the raw XML exactly as received, pretty-prints it,
     * unmarshals to RespHbt DTO for inspection, then returns the Ack.
     */
    @PostMapping(
            value = "/RespHbt/2.0/urn:txnid:{txnId}",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<Ack> receiveFinalRespHbt(
            @PathVariable String txnId,
            @RequestBody String rawXmlBody
    ) {
        try {
            // 1) Log raw XML as-is
            log.info("==== RespHbt RAW RECEIVED for txnId={} ====", txnId);
            log.info(rawXmlBody);

            // 2) Try pretty-printing for readable logs (if XmlPrettyPrinter available)
            try {
                String pretty = XmlPrettyPrinter.format(rawXmlBody);
                log.info("==== RespHbt PRETTY ====\n{}", pretty);
            } catch (Exception pe) {
                // ignore pretty-print failures
                log.debug("XmlPrettyPrinter failed: {}", pe.getMessage());
            }

            // 3) Unmarshal into DTO (so you can inspect fields)
            RespHbt respHbt = null;
            try {
                respHbt = XmlUtils.fromXml(rawXmlBody, RespHbt.class);
            } catch (Exception ex) {
                log.warn("Failed to unmarshal RespHbt into DTO for txnId={}: {}", txnId, ex.getMessage());
            }

            // 4) Optional: extra info log
            if (respHbt != null && respHbt.getResp() != null) {
                log.info("Parsed RespHbt: reqMsgId={}, result={}, errCode={}",
                        respHbt.getResp().getReqMsgId(),
                        respHbt.getResp().getResult());

            }

            // 5) Build and return Ack (use Head msgId if available)
            Ack ack = new Ack();
            ack.setApi("RespHbt");
            // prefer Head.msgId if present; otherwise use Resp.reqMsgId
            String headMsgId = (respHbt != null && respHbt.getHead() != null) ? respHbt.getHead().getMsgId() : null;
            ack.setReqMsgId(headMsgId != null ? headMsgId : (respHbt != null && respHbt.getResp() != null ? respHbt.getResp().getReqMsgId() : null));
            ack.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            log.info("Returning Ack for RespHbt txnId={} reqMsgId={}", txnId, ack.getReqMsgId());
            return ResponseEntity.ok(ack);

        } catch (Exception e) {
            log.error("Error processing RespHbt for txnId={}: {}", txnId, e.getMessage(), e);
            // return 500-like Ack to UPI instead of raw exception (keeps UPI logs clean)
            Ack errorAck = new Ack();
            errorAck.setApi("RespHbt");
            errorAck.setReqMsgId(null);
            errorAck.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            return ResponseEntity.status(500).body(errorAck);
        }
    }
}
