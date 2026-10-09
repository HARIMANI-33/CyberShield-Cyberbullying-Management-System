package com.cybershield.repository;

import com.cybershield.model.PlatformAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformActionRepository extends JpaRepository<PlatformAction, Long> {

    List<PlatformAction> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);

    List<PlatformAction> findAllByOrderByCreatedAtDesc();

    long countByActionStatus(String actionStatus);
}
