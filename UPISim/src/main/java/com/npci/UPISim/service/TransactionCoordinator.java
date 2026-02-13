package com.npci.UPISim.service;

import com.npci.UPISim.util.TransactionContext;
import com.npci.UPISim.util.TransactionStore;
import org.springframework.stereotype.Component;

@Component
public class TransactionCoordinator {

    private final ReqPayDebitService debitService;
    private final ReqPayCreditService creditService;
    private final FinalRespPayService finalRespPayService;

    public static final String RESP_AUTH = "RespAuthDetails";
    public static final String REQ_PAY_DEBIT = "ReqPayDebit";
    public static final String RESP_PAY_DEBIT = "RespPayDebit";
    public static final String REQ_PAY_CREDIT = "ReqPayCredit";
    public static final String RESP_PAY_CREDIT = "RespPayCredit";
    public static final String FINAL_RESP_PAY = "FinalRespPay";

    public TransactionCoordinator(
            ReqPayDebitService debitService,
            ReqPayCreditService creditService,
            FinalRespPayService finalRespPayService
    ) {
        this.debitService = debitService;
        this.creditService = creditService;
        this.finalRespPayService = finalRespPayService;
    }

    public void markEvent(String rawTxnId, String api, String status) {
        // normalize inside TransactionContext
        TransactionContext ctx = TransactionStore.get(rawTxnId);
        if (ctx == null) {
            ctx = new TransactionContext(rawTxnId);
            TransactionStore.put(ctx.getTxnId(), ctx);
        }

        ctx.updateStatus(api, status);
        System.out.println("[Coordinator] txnId=" + ctx.getTxnId() + " -> " + api + " = " + status);

        evaluateNextStep(ctx);
    }

    private void evaluateNextStep(TransactionContext ctx) {
        String txnId = ctx.getTxnId();

        if ("SUCCESS".equals(ctx.getStatus(RESP_AUTH))
                && !"TRIGGERED".equals(ctx.getStatus(REQ_PAY_DEBIT))) {
            ctx.updateStatus(REQ_PAY_DEBIT, "TRIGGERED");
            debitService.triggerReqPayDebit(ctx.getTxnId(),ctx.getRespAuthDetails());
            return;
        }

        if ("SUCCESS".equals(ctx.getStatus(RESP_PAY_DEBIT))
                && !"TRIGGERED".equals(ctx.getStatus(REQ_PAY_CREDIT))) {
            ctx.updateStatus(REQ_PAY_CREDIT, "TRIGGERED");
            creditService.triggerReqPayCredit(ctx.getTxnId(),ctx.getRespAuthDetails());
            return;
        }

        if ("SUCCESS".equals(ctx.getStatus(RESP_PAY_CREDIT))
                && !"SENT".equals(ctx.getStatus(FINAL_RESP_PAY))) {
            ctx.updateStatus(FINAL_RESP_PAY, "SENT");
            finalRespPayService.sendFinalRespPay(
                    ctx.getTxnId(),
                    ctx.getRespPayDebit(),
                    ctx.getRespPayCredit()
            );
        }
    }
}
