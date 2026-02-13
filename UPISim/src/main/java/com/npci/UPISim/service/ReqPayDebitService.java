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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ReqPayDebitService {
    private static final Logger log = LoggerFactory.getLogger(ReqPayDebitService.class);

    private final UPILogUtil upiLogUtil;
    private final RoutingService routingService;

    public ReqPayDebitService(UPILogUtil upiLogUtil, RoutingService routingService) {
        this.upiLogUtil = upiLogUtil;
        this.routingService = routingService;
    }

    public void triggerReqPayDebit(String txnId, RespAuthDetails respAuthDetails) {
        CompletableFuture.runAsync(() -> {
            try {
                ReqPay reqPayDebit = buildReqPayDebitFull(txnId,respAuthDetails);
                String xmlPayload = XmlUtil.toXml(reqPayDebit, ReqPay.class);

                // 🔹 Extract IFSC from Payer (Remitter)
                String ifsc = reqPayDebit.getPayer().getAc().getDetails().stream()
                        .filter(d -> "IFSC".equals(d.getName()))
                        .findFirst()
                        .map(Detail::getValue)
                        .orElseThrow(() -> new RuntimeException("IFSC not found in Payer account details"));

                // 🔹 Resolve Bank URL dynamically
                String bankUrl = routingService.getBankUrlByIfsc(ifsc);
                String url = bankUrl + "/upi/ReqPay/2.0/urn:txnid:" + txnId;

                RestTemplate rest = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_XML);
                HttpEntity<String> request = new HttpEntity<>(xmlPayload, headers);

                upiLogUtil.logOutbound("ReqPayDebit", url, xmlPayload, txnId);

                String response = rest.postForEntity(url, request, String.class).getBody();

                upiLogUtil.logAck(url, response, txnId);

                log.info("ReqPayDebit triggered successfully for txnId={}, response={}", txnId, response);

            } catch (Exception e) {
                log.error("Failed to trigger ReqPayDebit for txnId={}", txnId, e);
            }
        });
    }

    // 🔹 Builder for Debit
    private ReqPay buildReqPayDebitFull(String txnId,RespAuthDetails respAuthDetails) {
        ReqPay reqPay = new ReqPay();

        // --- Head ---
        Head head = new Head();
        head.setVer("2.0");
        head.setTs(respAuthDetails.getHead().getTs());
        head.setOrgId("NPCI");
        head.setMsgId("NPC" + UUID.randomUUID().toString().replace("-", "").substring(0, 29));
        reqPay.setHead(head);

        // --- Txn ---
        Txn txn = new Txn();
        txn.setId(txnId);
        txn.setNote("debit leg");
        txn.setRefId(txnId);
        txn.setRefUrl("http://idbi.in");
        txn.setTs(respAuthDetails.getTxn().getTs());
        txn.setType("DEBIT");
        txn.setCustRef("525300299401");
        txn.setInitiationMode("00");
        txn.setSubType("PAY");
        txn.setPurpose("00");

        RiskScores riskScores = new RiskScores();
        List<Score> scores = new ArrayList<>();
        Score score = new Score();
        score.setProvider("psp1");
        score.setType("TXNRISK");
        score.setValue("00030");
        scores.add(score);
        riskScores.setScores(scores);
        txn.setRiskScores(riskScores);
        reqPay.setTxn(txn);

        // --- Payer ---
        Payer payer = new Payer();

        payer.setAddr(respAuthDetails.getPayer().getAddr());
        payer.setName(respAuthDetails.getPayer().getName());
        payer.setSeqNum(respAuthDetails.getPayer().getSeqNum());
        payer.setType(respAuthDetails.getPayer().getType());
        payer.setCode(respAuthDetails.getPayer().getCode());

        // Ac with IFSC
        Ac ac = new Ac();
        ac.setAddrType("ACCOUNT");
        List<Detail> details = new ArrayList<>();
        details.add(new Detail("ACTYPE", respAuthDetails.getPayer().getAc().getDetailValue("ACTYPE")));
        details.add(new Detail("IFSC", respAuthDetails.getPayer().getAc().getDetailValue("IFSC")));
        details.add(new Detail("ACNUM", respAuthDetails.getPayer().getAc().getDetailValue("ACNUM")));
        ac.setDetails(details);
        payer.setAc(ac);


        Data data = new Data();
        data.setCode("NPCI");
        data.setKi("202828");
        data.setValue("ravi123");

        Cred cred = new Cred();
        cred.setType("MPIN");
        cred.setSubType("NPCI");
        cred.setData(data);
        Creds creds = new Creds();
        creds.setCreds(Collections.singletonList(cred)); // or creds.setCreds(...), depending on DTO
        payer.setCreds(creds);


        Amount payerAmount = new Amount();
        payerAmount.setValue(respAuthDetails.getPayer().getAmount().getValue());
        payerAmount.setCurr("INR");
        payer.setAmount(payerAmount);

        reqPay.setPayer(payer);

        // --- Payee ---
        Payee payee = new Payee();
        payee.setAddr(respAuthDetails.getPayees().get(0).getAddr());
        payee.setName(respAuthDetails.getPayees().get(0).getName());
        payee.setSeqNum(respAuthDetails.getPayees().get(0).getSeqNum());
        payee.setType(respAuthDetails.getPayees().get(0).getType());
        payee.setCode(respAuthDetails.getPayees().get(0).getCode());

        Ac payeeAc = new Ac();
        payeeAc.setAddrType("ACCOUNT");
        List<Detail> payeeDetails = new ArrayList<>();
        payeeDetails.add(new Detail("IFSC", respAuthDetails.getPayees().get(0).getAc().getDetailValue("IFSC")));
        payeeDetails.add(new Detail("ACTYPE", respAuthDetails.getPayees().get(0).getAc().getDetailValue("ACTYPE")));
        payeeDetails.add(new Detail("ACNUM", respAuthDetails.getPayees().get(0).getAc().getDetailValue("ACNUM")));
        payeeAc.setDetails(payeeDetails);
        payee.setAc(payeeAc);

        Amount payeeAmount = new Amount();
        payeeAmount.setValue(respAuthDetails.getPayees().get(0).getAmount().getValue());
        payeeAmount.setCurr("INR");
        payee.setAmount(payeeAmount);

        List<Payee> payees = new ArrayList<>();
        payees.add(payee);
        reqPay.setPayees(payees);

        return reqPay;
    }
}