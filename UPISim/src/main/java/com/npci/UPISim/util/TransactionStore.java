package com.npci.UPISim.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionStore {
    private static final ConcurrentHashMap<String, TransactionContext> store = new ConcurrentHashMap<>();

    public static TransactionContext get(String txnId) {
        return store.get(txnId);
    }

    public static TransactionContext getOrCreate(String txnId) {
        return store.computeIfAbsent(txnId, TransactionContext::new);
    }

    public static void put(String txnId, TransactionContext context) {
        store.put(txnId, context);
    }

    public static Map<String, TransactionContext> getAll() {
        return store;
    }
}
