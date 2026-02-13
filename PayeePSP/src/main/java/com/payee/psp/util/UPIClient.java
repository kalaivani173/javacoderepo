package com.payee.psp.util;

import com.payee.psp.dto.Ack;
import com.payee.psp.dto.RespAuthDetails;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.time.Duration;
import java.util.Collections;

@Component
public class UPIClient {

    private final RestTemplate restTemplate;

    @Value("${upi.baseUrl}")
    private String respAuthBaseUrl;

    public UPIClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Marshal RespAuthDetails into XML and POST it to UPI
     */
    public void sendRespAuthDetails(RespAuthDetails resp, String txnId) {
        String url = respAuthBaseUrl + "/upi/RespAuthDetails/2.0/urn:txnid:" + txnId;

        try {
            // Build XML from RespAuthDetails using JAXB (reuse XmlUtils convenience method)
            String xml = XmlUtils.toXml(resp);

            System.out.println("---- Outgoing RespAuthDetails XML ----");
            System.out.println(xml);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));

            // Send as raw XML string
            HttpEntity<String> entity = new HttpEntity<>(xml, headers);

            ResponseEntity<Ack> response = restTemplate.postForEntity(url, entity, Ack.class);

            System.out.println("---- Ack from UPI ----");
            Ack ackBody = response.getBody();
            if (ackBody != null) {
                try {
                    String ackXml = XmlUtils.toXml(ackBody);
                    System.out.println(ackXml);
                } catch (Exception e) {
                    System.out.println("Received Ack but failed to marshal for logging: " + e.getMessage());
                }
            } else {
                System.out.println("Received null Ack body (status=" + response.getStatusCodeValue() + ")");
            }

        } catch (Exception e) {
            System.err.println("Error posting RespAuthDetails to UPI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
