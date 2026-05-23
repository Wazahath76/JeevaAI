package com.medicore.hms.repository;

import com.medicore.hms.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, UUID> {
    List<Treatment> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<Treatment> findByPatientIdAndIsActiveTrue(UUID patientId);
    List<Treatment> findByPrescribedByIdOrderByCreatedAtDesc(UUID doctorId);
}
