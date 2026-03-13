package com.npci.UPISim.controller;

import com.npci.UPISim.dto.TxnRespPayStatusDto;
import com.npci.UPISim.model.TransactionLog;
import com.npci.UPISim.service.TransactionLogService;
import com.npci.UPISim.util.XmlPrettyPrinter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
public class TransactionLogController {

    private final TransactionLogService service;

    public TransactionLogController(TransactionLogService service) {
        this.service = service;
    }

    @GetMapping("/txn/{txnId}")
    public List<TransactionLog> getLogsByTxnId(@PathVariable String txnId) {
        return service.getLogsByTxnId(txnId);
    }

   // @GetMapping("/all")
    //public List<String> getAllTxnIds() {
       // return service.getAllTxnIds();
   // }

    @GetMapping("/all")
    public List<TxnRespPayStatusDto> getAllTxnIdsWithFinalRespPayStatus() {
        return service.getAllTxnWithFinalRespPayStatus();
    }

    @GetMapping("/logs/{txnId}")
    public List<String> getLogs(@PathVariable String txnId) {
        return service.getLogsForTxn(txnId).stream()
                .map(XmlPrettyPrinter::format)
                .collect(Collectors.toList());
    }

    @GetMapping("/status/{txnId}")
    public ResponseEntity<String> getTxnStatus(@PathVariable String txnId) {
        String status = service.getTxnStatus(txnId);
        return ResponseEntity.ok(status == null ? "PENDING" : status);
    }

}