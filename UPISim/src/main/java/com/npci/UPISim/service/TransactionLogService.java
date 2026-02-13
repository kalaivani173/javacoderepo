package com.npci.UPISim.service;

import com.npci.UPISim.dto.TxnRespPayStatusDto;
import com.npci.UPISim.model.TransactionLog;
import com.npci.UPISim.repo.TransactionLogRepository;
import com.npci.UPISim.repo.TxnRespStatusProjection;
import com.npci.UPISim.util.XmlPrettyPrinter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionLogService {

    private final TransactionLogRepository repo;

    public TransactionLogService(TransactionLogRepository repo) {
        this.repo = repo;
    }

    public void saveLog(String txnId, String api, String direction, String uri, String payload) {
        TransactionLog log = new TransactionLog();
        log.setTxnId(txnId);
        log.setApi(api);
        log.setDirection(direction);
        log.setUri(uri);
        log.setPayload(payload);
        repo.save(log);
    }

    public List<TransactionLog> getLogsByTxnId(String txnId) {
        return repo.findByTxnIdOrderByCreatedAtAsc(txnId);
    }

    public List<String> getAllTxnIds() {
        return repo.findDistinctTxnIds();
    }

    public List<String> getLogsForTxn(String txnId) {
        return repo.findByTxnIdOrderByCreatedAtAsc(txnId).stream()
                .map(log -> {
                    String p = log.getPayload();
                    return (p == null || p.isEmpty()) ? "(no payload)" : XmlPrettyPrinter.format(p);
                })
                .collect(Collectors.toList());
    }

    public void updateTxnStatus(String txnId, String status) {
        repo.updateTxnStatus(txnId, status);
    }

    public String getTxnStatus(String txnId) {
        return repo.findLatestStatus(txnId);
    }

    public List<TxnRespPayStatusDto> getAllTxnWithFinalRespPayStatus() {
        List<TxnRespStatusProjection> rows = repo.findAllTxnIdsWithLatestFinalRespPayStatus();

        return rows.stream()
                .map(p -> new TxnRespPayStatusDto(
                        p.getTxnId(),
                        p.getRespStatus() == null ? "PENDING" : p.getRespStatus(),
                        p.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }


}