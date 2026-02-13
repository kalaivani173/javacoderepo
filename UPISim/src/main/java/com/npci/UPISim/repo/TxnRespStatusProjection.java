package com.npci.UPISim.repo;

import java.time.LocalDateTime;

public interface TxnRespStatusProjection {
    String getTxnId();
    String getRespStatus();
    LocalDateTime getCreatedAt();
}
