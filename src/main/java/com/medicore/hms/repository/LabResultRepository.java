package com.medicore.hms.repository;

import com.medicore.hms.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, UUID> {
    List<LabResult> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<LabResult> findByPatientIdAndIsAbnormalTrue(UUID patientId);
}
