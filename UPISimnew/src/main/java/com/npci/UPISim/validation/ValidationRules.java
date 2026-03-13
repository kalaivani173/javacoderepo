package com.npci.UPISim.validation;

import java.util.Set;

public final class ValidationRules {

    private ValidationRules() {}

    // From NPCI circulars / XSD
    public static final Set<String> ALLOWED_TXN_TYPES =
            Set.of("PAY", "COLLECT");

    public static final Set<String> ALLOWED_PURPOSE_CODES =
            Set.of("00", "01", "02", "11","20");

    public static final Set<String> ALLOWED_INITIATION_MODES =
            Set.of("00", "01", "02");

    // Allowed values for logical attribute Payer.Device.BINDINGMODE
    public static final Set<String> REQPAY_PAYER_DEVICE_BINDINGMODE_ALLOWED_VALUES =
            Set.of("SMS", "RSMS");

    // Allowed values for logical attribute ReqPay.Payer.Device.BINDINGMODE
    public static final Set<String> REQPAY_PAYER_DEVICE_BINDINGMODE_ALLOWED_VALUES =
            Set.of("SMS", "RSMS");
}