package com.npci.UPISim.controller;

import com.npci.UPISim.model.PspBank;
import com.npci.UPISim.repository.PspBankRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/psp-banks")
public class PspBankController {

    private final PspBankRepository repository;

    public PspBankController(PspBankRepository repository) {
        this.repository = repository;
    }

    // Get all banks
    @GetMapping
    public List<PspBank> getAllBanks() {
        return repository.findAll();
    }



    // Insert new bank
    @PostMapping
    public PspBank createBank(@RequestBody PspBank pspBank) {
        return repository.save(pspBank);
    }

    // Update existing bank
    @PutMapping("/{id}")
    public PspBank updateBank(@PathVariable Long id, @RequestBody PspBank pspBank) {
        PspBank existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found with id: " + id));

        existing.setName(pspBank.getName());
        existing.setBankCode(pspBank.getBankCode());
        existing.setBankUrl(pspBank.getBankUrl());
        existing.setHandle(pspBank.getHandle());
        existing.setIfsc(pspBank.getIfsc());
        existing.setIin(pspBank.getIin());
        existing.setOrgId(pspBank.getOrgId());

        return repository.save(existing);
    }

    // Delete bank
    @DeleteMapping("/{id}")
    public void deleteBank(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
