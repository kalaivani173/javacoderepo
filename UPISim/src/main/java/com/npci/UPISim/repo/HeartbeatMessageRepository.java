package com.npci.UPISim.repo;

import com.npci.UPISim.model.HeartbeatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeartbeatMessageRepository extends JpaRepository<HeartbeatMessage, Long> {
    List<HeartbeatMessage> findByTxnIdOrderByCreatedAtAsc(String txnId);
}
