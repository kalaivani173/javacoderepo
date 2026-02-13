package com.payee.psp.util;

import com.payee.psp.dto.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

public class RespAuthDetailsBuilder {

    public static RespAuthDetails build(ReqAuthDetails req, String txnId) {
        RespAuthDetails resp = new RespAuthDetails();

        // ---- Head ----
        Head head = new Head();
        head.setVer("2.0");
        head.setTs(OffsetDateTime.now().toString());
        head.setOrgId("157777"); // your PSP orgId
        head.setMsgId("PYE" + UUID.randomUUID().toString().replace("-","").substring(0,29));
        resp.setHead(head);

        // ---- Resp ----
        RespAuthDetails.Resp response = new RespAuthDetails.Resp();
        response.setReqMsgId(req.getHead().getMsgId());
        response.setResult("SUCCESS");
        resp.setResp(response);

        // ---- Txn ----
        Txn txn = new Txn();
        txn.setId(txnId);
        txn.setNote(req.getTxn().getNote());
        txn.setRefId(req.getTxn().getRefId());
        txn.setRefUrl(req.getTxn().getRefUrl());
        txn.setTs(OffsetDateTime.now().toString());
        txn.setType(req.getTxn().getType());
        txn.setCustRef(req.getTxn().getCustRef());
        txn.setInitiationMode(req.getTxn().getInitiationMode());
        txn.setPurpose(req.getTxn().getPurpose());
        txn.setRiskScores(req.getTxn().getRiskScores()); // copy if already in DTO
        resp.setTxn(txn);

        // ---- Payer ----
        resp.setPayer(req.getPayer());

        // ---- Payees ----

        Ac ac = new Ac();
        ac.setAddrType("ACCOUNT");

        Detail detail1 = new Detail();
        detail1.setName("ACTYPE");
        detail1.setValue("SAVINGS");

        Detail detail2 = new Detail();
        detail2.setName("ACNUM");
        detail2.setValue("223336");

        Detail detail3 = new Detail();
        detail3.setName("IFSC");
        detail3.setValue("INDB0000001");

        ac.setDetails(Arrays.asList(detail1, detail2, detail3));

        Payee payee = new Payee();
        payee.setAc(ac);
        payee.setAmount(req.getPayees().get(0).getAmount());
        payee.setAddr(req.getPayees().get(0).getAddr());
        payee.setSeqNum(req.getPayees().get(0).getSeqNum());
        payee.setCode(req.getPayees().get(0).getCode());
        payee.setType(req.getPayees().get(0).getType());

        resp.setPayees(Arrays.asList(payee));

        return resp;
    }
}
