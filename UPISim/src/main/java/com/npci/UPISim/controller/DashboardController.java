package com.npci.UPISim.controller;

import com.npci.UPISim.model.PspBank;
import com.npci.UPISim.service.PSPBankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final PSPBankService pspBankService;

    public DashboardController(PSPBankService pspBankService) {
        this.pspBankService = pspBankService;
    }

    @GetMapping("/banks")
    public List<BankStatusDto> getBanks() {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return pspBankService.findAll().stream().map(b -> new BankStatusDto(
                b.getOrgId(),
                b.getHandle(),
                b.getBankUrl(),
                b.getStatus(),
                b.getLastHeartbeat() == null ? null : b.getLastHeartbeat().toString()
        )).collect(Collectors.toList());
    }

    public static class BankStatusDto {
        public String orgId;
        public String handle;
        public String bankUrl;
        public String status;
        public String lastHeartbeat;

        public BankStatusDto(String orgId, String handle, String bankUrl, String status, String lastHeartbeat) {
            this.orgId = orgId;
            this.handle = handle;
            this.bankUrl = bankUrl;
            this.status = status;
            this.lastHeartbeat = lastHeartbeat;
        }
    }
}
