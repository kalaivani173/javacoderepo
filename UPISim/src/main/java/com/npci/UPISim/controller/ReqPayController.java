package com.npci.UPISim.controller;

import com.npci.UPISim.dto.Ack;
import com.npci.UPISim.dto.ReqPay;
import com.npci.UPISim.exception.UpiValidationException;
import com.npci.UPISim.exception.UpiXmlValidationException;
import com.npci.UPISim.service.ReqPayService;
import com.npci.UPISim.util.UPILogUtil;
import com.npci.UPISim.util.XmlUtil;
import com.npci.UPISim.validation.ReqPayValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/upi")
public class ReqPayController {

    private final ReqPayService reqPayService;
    private final UPILogUtil upiLogUtil;

    public ReqPayController(ReqPayService reqPayService, UPILogUtil upiLogUtil) {
        this.reqPayService = reqPayService;
        this.upiLogUtil = upiLogUtil;
    }


    @PostMapping(
            value = "/ReqPay/2.0/urn:txnid:{txnId}",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> receiveReqPay(
            @RequestBody String xml,
            @PathVariable String txnId,
            HttpServletRequest request) {

        // inbound already stored
        upiLogUtil.logInbound("ReqPay", request.getRequestURI(), xml, txnId);

        ReqPay reqPay = XmlUtil.fromXmlWithoutXsd(xml, ReqPay.class);

        String ackXml = reqPayService.processReqPay(reqPay);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(ackXml);
    }



}
