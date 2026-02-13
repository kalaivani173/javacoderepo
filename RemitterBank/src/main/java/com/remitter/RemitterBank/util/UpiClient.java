package com.remitter.RemitterBank.util;

import com.remitter.RemitterBank.dto.Ack;
import com.remitter.RemitterBank.dto.RespPay;
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

@Component
public class UpiClient {
    private final RestTemplate restTemplate;
    @Value("${upi.baseUrl}") private String upiBaseUrl;

    public UpiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void sendRespPay(RespPay respPay, String txnId) {
        String url = upiBaseUrl + "/RespPay/2.0/urn:txnid:" + txnId;

        try {
            // ✅ Marshal object -> XML
            JAXBContext context = JAXBContext.newInstance(RespPay.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            StringWriter sw = new StringWriter();
            marshaller.marshal(respPay, sw);
            String xml = sw.toString();

            System.out.println("---- Outgoing RespPAY DEBIT XML ----");
            System.out.println(xml);

            HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<RespPay> entity = new HttpEntity<>(respPay, headers);
            ResponseEntity<Ack> response = restTemplate.postForEntity(url, entity, Ack.class);

            System.out.println("---- Ack from UPI ----");
            System.out.println(XmlUtils.toXml(response.getBody()));

        } catch (Exception e) {
            System.err.println("Error posting RespPay DEBIT to UPI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    }

