package com.payer.PayerPSP.service;


import com.payer.PayerPSP.dto.*;
import com.payer.PayerPSP.util.TxnIdGenerator;
import org.springframework.stereotype.Service;
import com.payer.PayerPSP.dto.Txn;



import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PayerService {

    public ReqPay createReqPay(String payerVpa, String payeeVpa, double amount) {
        ReqPay reqPay = new ReqPay();

        // Head
        Head head = new Head();
        head.setMsgId("PYR" + UUID.randomUUID().toString().replace("-","").substring(0,29));
        head.setOrgId("ACC101");
        head.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        head.setVer("2.0");
        reqPay.setHead(head);

        // Txn
        Txn txn = new Txn();
        txn.setId(TxnIdGenerator.generateTxnId());
        txn.setCustRef("504518672843");
        txn.setType("PAY");
        txn.setNote("PR_4");
        txn.setRefId("504518672843");
        txn.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        txn.setInitiationMode("00");
        txn.setPurpose("00");
        txn.setRefUrl("www.payer.com");
        txn.setSubType("PAY");

        RiskScores riskScores = new RiskScores();


        // RiskScores
        Score score1 = new Score();
        score1.setProvider("sp");
        score1.setType("TXNRISK");
        score1.setValue("00030");

        Score score2 = new Score();
        score2.setProvider("npci");
        score2.setType("TXNRISK");
        score2.setValue("00030");
       // riskScores.setScores(Arrays.asList(score1, score2));
        riskScores.setScores(Collections.singletonList(score1));


        txn.setRiskScores(riskScores);
        reqPay.setTxn(txn);

        // Payer (example hardcoded, you can map from VPA)
        Payer payer = new Payer();
        payer.setAddr(payerVpa);
        payer.setName("ARUN PRAJAPATI");
        payer.setType("PERSON");
        payer.setSeqNum("1");
        payer.setCode("0000");
        Amount payerAmount = new Amount();
        payerAmount.setCurr("INR");
        payerAmount.setValue(2.00);
        payer.setAmount(payerAmount);

        reqPay.setPayer(payer);

        Identity identity = new Identity();
        identity.setId("111111");
        identity.setType("ACCOUNT");
        identity.setVerifiedName("Kamal");


       Rating rating = new Rating();
       rating.setVerifiedAddress("TRUE");



        Info info = new Info();
        info.setIdentity(identity);
        info.setRating(rating);

        payer.setInfo(info);


        Device device = new Device();

        Tag tag1 = new Tag();
        tag1.setName("TYPE");
        tag1.setValue("MOB");

        Tag tag2 = new Tag();
        tag2.setName("TELECOM");
        tag2.setValue("AIRTEL");

        Tag tag3 = new Tag();
        tag3.setName("MOBILE");
        tag3.setValue("917208536035");

        Tag tag4 = new Tag();
        tag4.setName("LOCATION");
        tag4.setValue("INDIA");

        Tag tag5 = new Tag();
        tag5.setName("IP");
        tag5.setValue("10.25.205.68");

        Tag tag6 = new Tag();
        tag6.setName("ID");
        tag6.setValue("977776efdc1718d7");

        Tag tag7 = new Tag();
        tag7.setName("OS");
        tag7.setValue("Android9");

        Tag tag8 = new Tag();
        tag8.setName("APP");
        tag8.setValue("com.upi.npci");

        Tag tag9 = new Tag();
        tag9.setName("GEOCODE");
        tag9.setValue("91.9819,23.9404");

        Tag tag10 = new Tag();
        tag10.setName("CAPABILITY");
        tag10.setValue("011001");

        device.setTags(List.of(tag1, tag2, tag3, tag4, tag5, tag6, tag7, tag8, tag9, tag10));
        payer.setDevice(device);


        Data data = new Data();
        data.setCode("NPCI");
        data.setKi("202828");
        data.setValue("ravi123");



// Create Cred
        Cred cred = new Cred();
        cred.setType("MPIN");
        cred.setSubType("NPCI");
        cred.setData(data);

// Add to Payer
        payer.setCreds(Collections.singletonList(cred));


        Ac ac = new Ac();
        ac.setAddrType("ACCOUNT");

        Detail detail1 = new Detail();
        detail1.setName("ACTYPE");
        detail1.setValue("SAVINGS");

        Detail detail2 = new Detail();
        detail2.setName("ACNUM");
        detail2.setValue("1234566");

        Detail detail3 = new Detail();
        detail3.setName("IFSC");
        detail3.setValue("AABF0000001");

        ac.setDetails(Arrays.asList(detail1, detail2, detail3));
        payer.setAc(ac);






        // Payee
        Payee payee = new Payee();
        payee.setAddr(payeeVpa);
        payee.setName("Tester");
        payee.setSeqNum("1");
        payee.setType("PERSON");
        payee.setCode("0000");
        Amount payeeAmount = new Amount();
        payeeAmount.setCurr("INR");
        payeeAmount.setValue(2.00);
        payee.setAmount(payeeAmount);
        reqPay.setPayees(Collections.singletonList(payee));

        return reqPay;
    }
}
