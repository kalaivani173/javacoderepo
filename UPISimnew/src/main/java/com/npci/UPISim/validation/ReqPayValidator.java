package com.npci.UPISim.validation;

import com.npci.UPISim.dto.Device;
import com.npci.UPISim.dto.ReqPay;
import com.npci.UPISim.dto.Tag;
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

        // --- Tag-based validation for Payer.Device.BINDINGMODE ---

        if (reqPay.getPayer() == null) {
            return "MISSING_FIELD:Payer";
        }

        Device device = reqPay.getPayer().getDevice();
        if (device == null) {
            return "MISSING_FIELD:Payer.Device";
        }

        String bindingModeValue = null;
        if (device.getTags() != null) {
            for (Tag tag : device.getTags()) {
                if ("BINDINGMODE".equalsIgnoreCase(tag.getName())) {
                    bindingModeValue = tag.getValue();
                    break;
                }

        // Mandatory: tag must exist and have a value
        if (bindingModeValue == null) {
            return "MISSING_FIELD:Payer.Device.BINDINGMODE";
        }

        // Allowed values: must be one of SMS / RSMS
        if (!ValidationRules.REQPAY_PAYER_DEVICE_BINDINGMODE_ALLOWED_VALUES.contains(bindingModeValue)) {
            return "INVALID_FIELD_VALUE:Payer.Device.BINDINGMODE=" + bindingModeValue;
        }

        return null; // ✅ VALID
    }