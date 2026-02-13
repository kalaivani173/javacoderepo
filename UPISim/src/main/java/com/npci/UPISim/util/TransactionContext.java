package com.npci.UPISim.util;

import com.npci.UPISim.dto.ReqPay;
import com.npci.UPISim.dto.RespAuthDetails;
import com.npci.UPISim.dto.RespPay;

import java.util.HashMap;
import java.util.Map;

public class TransactionContext {
    private final String txnId;
    private final Map<String, String> apiStatus = new HashMap<>();

    private ReqPay reqPay;
    private RespAuthDetails respAuthDetails;
    private RespPay respPayDebit;
    private RespPay respPayCredit;
    private RespPay finalRespPay;   // 🔹 NEW FIELD

    public TransactionContext(String txnId) {
        this.txnId = txnId;
    }

    public String getTxnId() { return txnId; }

    public void updateStatus(String api, String status) { apiStatus.put(api, status); }

    public String getStatus(String api) { return apiStatus.get(api); }

    public Map<String, String> getAllStatuses() { return apiStatus; }

    // === Context Objects ===
    public ReqPay getReqPay() { return reqPay; }
    public void setReqPay(ReqPay reqPay) { this.reqPay = reqPay; }

    public RespAuthDetails getRespAuthDetails() { return respAuthDetails; }
    public void setRespAuthDetails(RespAuthDetails respAuthDetails) { this.respAuthDetails = respAuthDetails; }

    public RespPay getRespPayDebit() { return respPayDebit; }
    public void setRespPayDebit(RespPay respPayDebit) { this.respPayDebit = respPayDebit; }

    public RespPay getRespPayCredit() { return respPayCredit; }
    public void setRespPayCredit(RespPay respPayCredit) { this.respPayCredit = respPayCredit; }

    public RespPay getFinalRespPay() { return finalRespPay; }     // 🔹 Getter
    public void setFinalRespPay(RespPay finalRespPay) { this.finalRespPay = finalRespPay; } // 🔹 Setter
}
