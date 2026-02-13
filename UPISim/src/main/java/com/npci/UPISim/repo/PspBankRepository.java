package com.npci.UPISim.repository;


import com.npci.UPISim.model.PspBank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface PspBankRepository extends JpaRepository<PspBank, Long> {


    Optional<PspBank> findByHandle(String handle);


    Optional<PspBank> findByIfsc(String ifsc);

    Optional<PspBank> findByOrgId(String orgId);
}