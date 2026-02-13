package com.Bene.BeneficiaryBank.util;

import java.util.UUID;

public class TxnIdGenerator {
    public static String generateTxnId() {
        return "PYR" + UUID.randomUUID().toString().replace("-", "");
    }
}
