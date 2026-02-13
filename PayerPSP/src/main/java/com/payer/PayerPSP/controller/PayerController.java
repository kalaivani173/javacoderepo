package com.payer.PayerPSP.controller;



import com.payer.PayerPSP.dto.ReqPay;
import com.payer.PayerPSP.service.PayerService;
import com.payer.PayerPSP.util.XmlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/upi")
public class PayerController {

    private static final Logger log = LoggerFactory.getLogger(PayerController.class);

    @Autowired
    private PayerService payerService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping(value = "/Pay", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> createReqPay(@RequestParam String payerVpa,
                               @RequestParam String payeeVpa,
                               @RequestParam double amount) {
        try {
            ReqPay reqPay = payerService.createReqPay(payerVpa, payeeVpa, amount);

            String reqPayXml = XmlUtil.toXml(reqPay, ReqPay.class);
            log.info("Generated ReqPay XML for payerVpa: {}, payeeVpa: {}, amount: {}", payerVpa, payeeVpa, amount);
            System.out.println(reqPayXml);
            
            String txnId = reqPay.getTxn().getId();
            String upiSwitchUrl = "http://localhost:8081/upi/ReqPay/2.0/urn:txnid:" + txnId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<String> entity = new HttpEntity<>(reqPayXml, headers);

            try {
                ResponseEntity<String> ackResponse = restTemplate.postForEntity(upiSwitchUrl, entity, String.class);
                log.info("UPISim response for txnId {}: {}", txnId, ackResponse.getBody());
                System.out.println(ackResponse.getBody());
                return ResponseEntity.ok(txnId);
            } catch (ResourceAccessException e) {
                log.error("Failed to connect to UPISim at {}: {}", upiSwitchUrl, e.getMessage());
                return ResponseEntity.status(503).body("UPISim service unavailable. Please ensure UPISim is running on port 8081.");
            } catch (HttpServerErrorException e) {
                String serverBody = e.getResponseBodyAsString();
                log.error("UPISim returned HTTP {} for txnId {}. Response: {}", e.getStatusCode(), txnId, serverBody);
                return ResponseEntity.status(502).body("UPISim returned error: " + e.getStatusCode() + ". " + serverBody);
            }
        } catch (Exception e) {
            log.error("Error processing payment request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
