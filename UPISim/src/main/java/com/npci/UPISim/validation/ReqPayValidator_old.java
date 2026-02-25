package com.npci.UPISim.validation;

import com.npci.UPISim.dto.ReqPay;
import com.npci.UPISim.dto.Txn;

public final class ReqPayValidator {

    private ReqPayValidator() {}

    // ✅ RETURN error instead of throwing exception
    public static String validate(ReqPay reqPay) {

        if (reqPay.getHead() == null) {
            return "MISSING_FIELD:Head";
        }

        Txn txn = reqPay.getTxn();
        if (txn == null) {
            return "MISSING_FIELD:Txn";
        }

        if (txn.getType() == null) {
            return "MISSING_FIELD:Txn.type";
        }

        if (!ValidationRules.ALLOWED_TXN_TYPES.contains(txn.getType())) {
            return "INVALID_FIELD_VALUE:Txn.type=" + txn.getType();
        }

        if (txn.getPurpose() != null &&
                !ValidationRules.ALLOWED_PURPOSE_CODES.contains(txn.getPurpose())) {
            return "INVALID_FIELD_VALUE:Txn.purpose=" + txn.getPurpose();
        }

        if (txn.getInitiationMode() != null &&
                !ValidationRules.ALLOWED_INITIATION_MODES.contains(txn.getInitiationMode())) {
            return "INVALID_FIELD_VALUE:Txn.initiationMode=" + txn.getInitiationMode();
        }

        if (txn.getDelegate() != null &&
                !ValidationRules.REQPAY_TXN_DELEGATE_ALLOWED_VALUES.contains(txn.getDelegate())) {
            return "INVALID_FIELD_VALUE:Txn.delegate=" + txn.getDelegate();
        }

        return null; // ✅ VALID
    }
}