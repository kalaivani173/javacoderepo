package com.npci.UPISim.repo;

import com.npci.UPISim.model.NodeStatusEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface NodeStatusRepository extends JpaRepository<NodeStatusEntity, String> {
    List<NodeStatusEntity> findByStatus(String status);
}
