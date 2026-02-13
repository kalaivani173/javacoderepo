package com.Bene.BeneficiaryBank.util;

import com.Bene.BeneficiaryBank.dto.*;

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
        head.setOrgId("159057"); // Remitter Bank OrgId
        head.setMsgId("BEN" + UUID.randomUUID().toString().replace("-", "").substring(0, 29));
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
        Payee payee = req.getPayees().get(0); // assuming single payee for now
        Ref payeeRef = new Ref();
        payeeRef.setType("PAYEE");
        payeeRef.setSeqNum(payee.getSeqNum());
        payeeRef.setAddr(payee.getAddr());
        payeeRef.setSettAmount(payee.getAmount().getValue() + "");
        payeeRef.setSettCurrency(payee.getAmount().getCurr());
        payeeRef.setRespCode("00");
        payeeRef.setApprovalNum("991711");// sample respCode for payee (could be SUCCESS or error)
         // if reversal is needed
        payeeRef.setRegName(payee.getName());
        payeeRef.setOrgAmount(payee.getAmount().getValue() + "");
        payeeRef.setAcNum(payee.getAc().getDetails().stream()
                .filter(d -> "ACNUM".equalsIgnoreCase(d.getName()))
                .findFirst().map(Detail::getValue).orElse("NA"));
        payeeRef.setIFSC(payee.getAc().getDetails().stream()
                .filter(d -> "IFSC".equalsIgnoreCase(d.getName()))
                .findFirst().map(Detail::getValue).orElse("NA"));
        payeeRef.setCode(payee.getCode());
        payeeRef.setAccType("SAVINGS");
        List<Ref> refs = new ArrayList<>();
        refs.add(payeeRef);
        resp.setRefs(refs);

        respPay.setResp(resp);

        return respPay;
    }
}