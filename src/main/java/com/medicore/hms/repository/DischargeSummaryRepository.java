package com.medicore.hms.repository;

import com.medicore.hms.entity.DischargeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DischargeSummaryRepository extends JpaRepository<DischargeSummary, UUID> {
    Optional<DischargeSummary> findByPatientId(UUID patientId);
}
