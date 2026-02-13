package com.remitter.RemitterBank.util;
import com.remitter.RemitterBank.dto.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RespPayBuilder {

    public static RespPay build(ReqPay req, String txnId) {
        RespPay respPay = new RespPay();

        // ---- Head ----
        Head head = new Head();
        head.setVer("2.0");
        head.setTs(OffsetDateTime.now().toString());
        head.setOrgId("159999"); // Remitter Bank OrgId
        head.setMsgId("REM" + UUID.randomUUID().toString().replace("-", "").substring(0, 29));
        respPay.setHead(head);

        // ---- Txn ----
        Txn txn = new Txn();
        txn.setId(txnId);
        txn.setNote(req.getTxn().getNote());
        txn.setRefId(req.getTxn().getRefId());
        txn.setRefUrl(req.getTxn().getRefUrl());
        txn.setTs(req.getTxn().getTs());   // reuse original ts
        txn.setType(req.getTxn().getType()); // DEBIT
        txn.setCustRef(req.getTxn().getCustRef());
        txn.setInitiationMode(req.getTxn().getInitiationMode());
        txn.setPurpose(req.getTxn().getPurpose());
        txn.setSubType(req.getTxn().getSubType());
        txn.setRiskScores(req.getTxn().getRiskScores()); // copy if present
        respPay.setTxn(txn);

        // ---- Resp ----
        Resp resp = new Resp();
        resp.setReqMsgId(req.getHead().getMsgId());
        resp.setResult("SUCCESS"); // can be SUCCESS / FAILURE

        // ---- Ref (Approval details for Payer) ----
        Ref ref = new Ref();
        ref.setType("PAYER");
        ref.setSeqNum("1");
        ref.setAddr(req.getPayer().getAddr());
        ref.setSettAmount(req.getPayer().getAmount().getValue() + "");
        ref.setSettCurrency(req.getPayer().getAmount().getCurr());
        ref.setApprovalNum(String.valueOf((int)(Math.random() * 1000000))); // mock approval num
        ref.setRespCode("00"); // success code
        ref.setRegName(req.getPayer().getName());
        ref.setOrgAmount(req.getPayer().getAmount().getValue() + "");
        ref.setAcNum(req.getPayer().getAc().getDetails().stream()
                .filter(d -> "ACNUM".equalsIgnoreCase(d.getName()))
                .findFirst().map(Detail::getValue).orElse("NA"));
        ref.setIFSC(req.getPayer().getAc().getDetails().stream()
                .filter(d -> "IFSC".equalsIgnoreCase(d.getName()))
                .findFirst().map(Detail::getValue).orElse("NA"));
        ref.setCode(req.getPayer().getCode());
        ref.setAccType("CURRENT"); // from payer Ac detail

        List<Ref> refs = new ArrayList<>();
        refs.add(ref);
        resp.setRefs(refs);

        respPay.setResp(resp);

        return respPay;
    }
}