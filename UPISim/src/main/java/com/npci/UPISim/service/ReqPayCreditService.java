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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ReqPayCreditService {
    private static final Logger log = LoggerFactory.getLogger(ReqPayCreditService.class);

    private final UPILogUtil upiLogUtil;
    private final RoutingService routingService;

    public ReqPayCreditService(UPILogUtil upiLogUtil, RoutingService routingService) {
        this.upiLogUtil = upiLogUtil;
        this.routingService = routingService;
    }

    public void triggerReqPayCredit(String txnId,RespAuthDetails respAuthDetails) {
        CompletableFuture.runAsync(() -> {
            try {
                ReqPay reqPayCredit = buildReqPayCreditFull(txnId,respAuthDetails);
                String xmlPayload = XmlUtil.toXml(reqPayCredit, ReqPay.class);

                // ✅ Route dynamically using IFSC
                String ifsc = reqPayCredit.getPayees().get(0).getAc().getDetails().stream()
                        .filter(d -> d.getName().equals("IFSC"))
                        .findFirst()
                        .map(Detail::getValue)
                        .orElseThrow(() -> new RuntimeException("No IFSC found for Payee"));

                String bankUrl = routingService.getBankUrlByIfsc(ifsc);
                String url = bankUrl + "/upi/ReqPay/2.0/urn:txnid:" + txnId;

                RestTemplate rest = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_XML);
                HttpEntity<String> request = new HttpEntity<>(xmlPayload, headers);

                // ✅ Log with dynamic URL
                upiLogUtil.logOutbound("ReqPayCredit", url, xmlPayload, txnId);

                String response = rest.postForEntity(url, request, String.class).getBody();

                upiLogUtil.logAck(url, response, txnId);

                log.info("ReqPayCredit triggered successfully for txnId={}, response={}", txnId, response);

            } catch (Exception e) {
                log.error("Failed to trigger ReqPayCredit for txnId={}", txnId, e);
            }
        });
    }

    // 🔹 Builder for Credit
    private ReqPay buildReqPayCreditFull(String txnId,RespAuthDetails respAuthDetails) {
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
        txn.setNote("creditudir");
        txn.setRefId(txnId);
        txn.setRefUrl("http://idbi.in");
        txn.setTs(respAuthDetails.getTxn().getTs());
        txn.setType("CREDIT");
        txn.setCustRef("525300299401");
        txn.setInitiationMode("00");
        txn.setSubType("PAY");
        txn.setPurpose(respAuthDetails.getTxn().getPurpose());

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

        Info info = new Info();
        Identity identity = new Identity();
        identity.setType("ACCOUNT");
        identity.setVerifiedName("95994863");
        identity.setId("0460102000009621");
        info.setIdentity(identity);

        Rating rating = new Rating();
        rating.setVerifiedAddress("TRUE");
        info.setRating(rating);
        payer.setInfo(info);

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

        Info payeeInfo = new Info();
        Identity payeeIdentity = new Identity();
        payeeIdentity.setType("ACCOUNT");
        payeeIdentity.setVerifiedName("80180546");
        payeeIdentity.setId("0008104000461696");
        payeeInfo.setIdentity(payeeIdentity);

        Rating payeeRating = new Rating();
        payeeRating.setVerifiedAddress("TRUE");
        payeeInfo.setRating(payeeRating);
        payee.setInfo(payeeInfo);

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
