package com.Bene.BeneficiaryBank.service;

import com.Bene.BeneficiaryBank.service.*;
import com.Bene.BeneficiaryBank.util.*;
import com.Bene.BeneficiaryBank.dto.*;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class HbtService {

    public ReqHbt createReqHbt(String custRef) {
        ReqHbt reqHbt = new ReqHbt();

        // Head
        Head head = new Head();
        head.setMsgId("HBT" + UUID.randomUUID().toString().replace("-", "").substring(0, 29));
        head.setOrgId("158888");
        head.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        head.setVer("2.0");
        reqHbt.setHead(head);

        // Txn
        Txn txn = new Txn();
        txn.setId(TxnIdGenerator.generateTxnId());
        txn.setCustRef(custRef);
        txn.setType("Hbt");
        txn.setNote("ReqHbt");
        txn.setRefId(custRef);
        txn.setRefUrl("www.payer.com");
        txn.setTs(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        reqHbt.setTxn(txn);

        // HbtMsg
        HbtMsg hbtMsg = new HbtMsg();
        hbtMsg.setType("ALIVE");
        hbtMsg.setValue("NA");
        reqHbt.setHbtMsg(hbtMsg);

        return reqHbt;
    }
}
