package com.npci.UPISim.service;

import com.npci.UPISim.model.PspBank;
import com.npci.UPISim.repository.PspBankRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoutingService {

    private final PspBankRepository repository;

    public RoutingService(PspBankRepository repository) {
        this.repository = repository;
    }

    /**
     * Route based on Handle (@okaxis, @upi etc)
     */
    public String getBankUrlByHandle(String handle) {
        return repository.findByHandle(handle)
                .map(PspBank::getBankUrl)
                .orElseThrow(() -> new RuntimeException("No Bank mapping found for handle: " + handle));
    }

    /**
     * Route based on IFSC
     */
    public String getBankUrlByIfsc(String ifsc) {
        String primaryifsc=ifsc.substring(0,4);
        return repository.findByIfsc(primaryifsc)
                .map(PspBank::getBankUrl)
                .orElseThrow(() -> new RuntimeException("No Bank mapping found for IFSC: " + primaryifsc));
    }

    /**
     * Route based on orgId (Head.orgId)
     */
    public String getBankUrlByOrgId(String orgId) {
        return repository.findByOrgId(orgId)
                .map(PspBank::getBankUrl)
                .orElseThrow(() -> new RuntimeException("No Bank mapping found for orgId: " + orgId));
    }

    /**
     * Safe lookup that returns null if no mapping exists (controller-friendly)
     */
    public String findBankUrlByOrgIdOrNull(String orgId) {
        return repository.findByOrgId(orgId).map(PspBank::getBankUrl).orElse(null);
    }

    public Optional<String> findBankUrlByHandle(String handle) {
        return repository.findByHandle(handle)
                .map(PspBank::getBankUrl);
    }
}
