package com.npci.UPISim.service;

import com.npci.UPISim.model.PspBank;
import com.npci.UPISim.repository.PspBankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PSPBankService {

    private final PspBankRepository repo;

    public PSPBankService(PspBankRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void recordHeartbeat(String orgId) {
        if (orgId == null) return;
        Optional<PspBank> opt = repo.findByOrgId(orgId);
        if (opt.isPresent()) {
            PspBank bank = opt.get();
            bank.setLastHeartbeat(LocalDateTime.now());
            bank.setStatus("UP");
            repo.save(bank);
        }
        // if not present we do nothing here; controller handles the ACK error path
    }

    @Transactional
    public void markDown(String orgId) {
        repo.findByOrgId(orgId).ifPresent(bank -> {
            bank.setStatus("DOWN");
            repo.save(bank);
        });
    }

    @Transactional(readOnly = true)
    public List<PspBank> findAll() {
        return repo.findAll();
    }
}
