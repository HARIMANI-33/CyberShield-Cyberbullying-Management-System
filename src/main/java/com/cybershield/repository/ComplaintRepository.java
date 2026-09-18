package com.cybershield.repository;

import com.cybershield.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByComplaintNumber(String complaintNumber);
    boolean existsByComplaintNumber(String complaintNumber);
}
